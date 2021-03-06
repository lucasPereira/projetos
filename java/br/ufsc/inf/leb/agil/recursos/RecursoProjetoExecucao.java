package br.ufsc.inf.leb.agil.recursos;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import br.ufsc.inf.leb.agil.AmbienteAgil;
import br.ufsc.inf.leb.agil.ConfiguracoesAgil;
import br.ufsc.inf.leb.agil.dominio.NomeadorDoProjetoNoSistemaDeArquivos;
import br.ufsc.inf.leb.agil.entidades.Projeto;
import br.ufsc.inf.leb.agil.persistencia.BancoDeDocumentos;
import br.ufsc.inf.leb.agil.persistencia.RepositorioDeProjetos;

@Path("/projeto/{identificador: .+}/execucao")
public class RecursoProjetoExecucao {

	@GET
	@Path("{arquivo: .+}")
	public Response obterArquivo(@PathParam("identificador") String identificador, @PathParam("arquivo") String arquivo) {
		ConfiguracoesAgil configuracoesProjeto = new AmbienteAgil().obterConfiguracoes();
		String nomeDoProjetoNoSistemaDeArquivos = new NomeadorDoProjetoNoSistemaDeArquivos().gerar(identificador);
		File arquivoSolicitado = configuracoesProjeto.obterArquivoDeExecucaoDoProjeto(nomeDoProjetoNoSistemaDeArquivos, arquivo);
		if (!arquivoSolicitado.exists()) {
			return Response.status(404).build();
		}
		return Response.status(200).entity(arquivoSolicitado).build();
	}

	@GET
	@Produces("text/html")
	public Response obter(@PathParam("identificador") String identificador) {
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
		ConfiguracoesAgil configuracoes = ambiente.obterConfiguracoes();
		String uriDeExecucao = ambiente.obterConfiguracoes().coonstruirUri(RecursoProjetoExecucao.class, identificador).toASCIIString();
		String uriJarDoProjeto = configuracoes.obterArquivoJarDoProjeto(identificador).getName();
		String urisDasBibliotecasJarDoProjeto = "";
		File uriDasBibliotecas = configuracoes.obterDiretorioDasBibliotecasDeExecucaoDoProjeto(identificador);
		if (uriDasBibliotecas.exists() && uriDasBibliotecas.isDirectory()) {
			for (File biblioteca : uriDasBibliotecas.listFiles()) {
				urisDasBibliotecasJarDoProjeto += ", ";
				urisDasBibliotecasJarDoProjeto += UriBuilder.fromPath(uriDasBibliotecas.getName()).path(biblioteca.getName()).build().getPath();
			}
		}
		Long versaoDoProjeto = new Date().getTime();
		String nomeDaClassePrincipal = projetos.get(0).obterClassePrincipal();

		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE html>");
		html.append("\n");
		html.append("<html>");
		html.append("\n");
		html.append("<head>");
		html.append("\n");
		html.append("	<meta charset=\"utf-8\" />");
		html.append("\n");
		html.append(String.format("	<title>%s</title>", identificador));
		html.append("\n");
		html.append("</head>");
		html.append("\n");
		html.append("<body>");
		html.append("\n");
		html.append("<applet");
		html.append("\n");
		html.append(String.format("codebase=\"%s\"", uriDeExecucao));
		html.append("\n");
		html.append(String.format("archive=\"%s?versao=%d%s\"", uriJarDoProjeto, versaoDoProjeto, urisDasBibliotecasJarDoProjeto));
		html.append("\n");
		html.append("code=\"br.ufsc.inf.leb.agil.dominio.Principal.class\"");
		html.append("\n");
		html.append(String.format("><param name=\"classePrincipal\" value=\"%s\" /></applet>", nomeDaClassePrincipal));
		html.append("\n");
		html.append("</body>");
		html.append("\n");
		html.append("</html>");
		return Response.status(200).entity(html.toString()).build();
	}

}
