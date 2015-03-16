package br.ufsc.inf.leb.projetos.recursos;

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

import br.ufsc.inf.leb.projetos.AmbienteProjetos;
import br.ufsc.inf.leb.projetos.dominio.ArquivosPermitidos;
import br.ufsc.inf.leb.projetos.dominio.ArvoreDoProjeto;
import br.ufsc.inf.leb.projetos.dominio.ExcecaoDeArquivoCompactadoNoFormatoInvalido;
import br.ufsc.inf.leb.projetos.entidades.Nodo;
import br.ufsc.inf.leb.projetos.entidades.Projeto;
import br.ufsc.inf.leb.projetos.persistencia.BancoDeDocumentos;
import br.ufsc.inf.leb.projetos.persistencia.RepositorioDeProjetos;

@Path("/projeto/{identificador}/arquivos")
public class RecursoProjetoArquivos {

	@GET
	@Produces("application/json")
	public Response obter(@PathParam("identificador") String identificador) {
		try {
			Nodo arquivosEmArvore = new ArvoreDoProjeto().construirArvore(identificador);
			if (arquivosEmArvore == null) {
				return Response.status(404).build();
			}
			return Response.status(200).entity(arquivosEmArvore).build();
		} catch (IOException excecao) {
			return Response.status(400).build();
		}
	}

	@PUT
	@Consumes("multipart/form-data")
	@Produces("applicaion/json")
	public Response colocar(@PathParam("identificador") String identificador, @FormDataParam("arquivos") InputStream arquivoCompactado, @FormDataParam("arquivos") FormDataContentDisposition metadadosDoArquivo) {
		AmbienteProjetos ambienteProjetos = new AmbienteProjetos();
		BancoDeDocumentos bancoDeDocumentos = ambienteProjetos.obterBancoDeDocumentos();
		RepositorioDeProjetos repositorioDeProjetos = bancoDeDocumentos.obterRepositorioDeProjetos();
		List<Projeto> projetos = repositorioDeProjetos.obterPorNome(identificador);
		if (projetos.size() > 1) {
			return Response.status(409).build();
		}
		if (projetos.size() < 1) {
			return Response.status(404).build();
		}
		try {
			ArquivosPermitidos arquivosPermitidos = new ArquivosPermitidos()
					.permitirArquivo(".classpath")
					.permitirArquivo(".project")
					.permitirDiretorio("src", new ArquivosPermitidos()
							.permitirArquivosComExtensao("java")
							.recursivo())
					.permitirDiretorio("resources", new ArquivosPermitidos()
							.permitirTodosArquivos()
							.recursivo())
					.permitirDiretorio("libs", new ArquivosPermitidos()
							.permitirArquivosComExtensao("jar"));
			arquivosPermitidos.salvarArquivos(identificador, arquivoCompactado);
			Projeto projeto = projetos.get(0);
			projeto.importarArquivos();
			bancoDeDocumentos.atualizarDocumento(projeto);
		} catch (IOException | ExcecaoDeArquivoCompactadoNoFormatoInvalido | ZipException excecao) {
			excecao.printStackTrace();
			return Response.status(400).build();
		}
		return Response.status(200).build();
	}

}
