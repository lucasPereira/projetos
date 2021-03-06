package br.ufsc.inf.leb.agil.recursos;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import net.lingala.zip4j.exception.ZipException;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import br.ufsc.inf.leb.agil.AmbienteAgil;
import br.ufsc.inf.leb.agil.dominio.ArquivosPermitidos;
import br.ufsc.inf.leb.agil.dominio.ArvoreDoProjeto;
import br.ufsc.inf.leb.agil.dominio.ExcecaoDeArquivoCompactadoNoFormatoInvalido;
import br.ufsc.inf.leb.agil.dominio.NomeadorDoProjetoNoSistemaDeArquivos;
import br.ufsc.inf.leb.agil.entidades.Nodo;
import br.ufsc.inf.leb.agil.entidades.Projeto;
import br.ufsc.inf.leb.agil.persistencia.BancoDeDocumentos;
import br.ufsc.inf.leb.agil.persistencia.RepositorioDeProjetos;

@Path("/projeto/{identificador: .+}/arquivos")
public class RecursoProjetoArquivos {

	@GET
	@Produces("application/json")
	public Response obterJson(@PathParam("identificador") String identificador) {
		try {
			String nomeDoProjetoNoSistemaDeArquivos = new NomeadorDoProjetoNoSistemaDeArquivos().gerar(identificador);
			Nodo arquivosEmArvore = new ArvoreDoProjeto().construirArvore(nomeDoProjetoNoSistemaDeArquivos);
			if (arquivosEmArvore == null) {
				return Response.status(404).build();
			}
			return Response.status(200).entity(arquivosEmArvore).build();
		} catch (IOException excecao) {
			excecao.printStackTrace();
			return Response.status(500).build();
		}
	}

	@PUT
	@Consumes("multipart/form-data")
	@Produces("applicaion/json")
	public Response colocar(@PathParam("identificador") String identificador, @FormDataParam("arquivos") InputStream arquivoCompactado, @FormDataParam("arquivos") FormDataContentDisposition metadadosDoArquivo) {
		AmbienteAgil ambiente = new AmbienteAgil();
		BancoDeDocumentos bancoDeDocumentos = ambiente.obterBancoDeDocumentos();
		RepositorioDeProjetos repositorioDeProjetos = bancoDeDocumentos.obterRepositorioDeProjetos();
		List<Projeto> projetos = repositorioDeProjetos.obterPorNome(identificador);
		if (projetos.size() > 1) {
			return Response.status(409).build();
		}
		if (projetos.size() < 1) {
			return Response.status(404).build();
		}
		try {
			ArquivosPermitidos arquivosPermitidos = obterArquivosPermitidos();
			String nomeDoProjetoNoSistemaDeArquivos = new NomeadorDoProjetoNoSistemaDeArquivos().gerar(identificador);
			arquivosPermitidos.salvarArquivos(nomeDoProjetoNoSistemaDeArquivos, arquivoCompactado);
			Projeto projeto = projetos.get(0);
			projeto.importarArquivos();
			bancoDeDocumentos.atualizarDocumento(projeto);
			return Response.status(200).build();
		} catch (IOException | ExcecaoDeArquivoCompactadoNoFormatoInvalido | ZipException excecao) {
			excecao.printStackTrace();
			return Response.status(400).build();
		}
	}

	private ArquivosPermitidos obterArquivosPermitidos() {
		return new ArquivosPermitidos()
				.permitirArquivo(".classpath")
				.permitirArquivo(".project")
				.permitirArquivo("build.xml")
				.permitirDiretorio("test", new ArquivosPermitidos()
						.permitirArquivosComExtensao("java")
						.recursivo())
				.permitirDiretorio("src", new ArquivosPermitidos()
						.permitirArquivosComExtensao("java")
						.recursivo())
				.permitirDiretorio("resources", new ArquivosPermitidos()
						.permitirTodosArquivos()
						.recursivo())
				.permitirDiretorio("nbproject", new ArquivosPermitidos()
						.permitirTodosArquivos()
						.recursivo())
				.permitirDiretorio("libs", new ArquivosPermitidos()
						.permitirArquivosComExtensao("jar"));
	}

}
