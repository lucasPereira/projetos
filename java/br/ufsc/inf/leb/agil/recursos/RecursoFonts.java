package br.ufsc.inf.leb.agil.recursos;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import br.ufsc.inf.leb.agil.dominio.ManipuladorDeArquivos;

@Path("/fonts/{identificador}")
public class RecursoFonts {

	@GET
	public Response obter(@PathParam("identificador") String identificador) {
		File arquivo = new ManipuladorDeArquivos().carregarArquivo("fonts", identificador);
		if (arquivo.exists()) {
			return Response.status(200).entity(arquivo).build();
		} else {
			return Response.status(404).build();
		}
	}

}
