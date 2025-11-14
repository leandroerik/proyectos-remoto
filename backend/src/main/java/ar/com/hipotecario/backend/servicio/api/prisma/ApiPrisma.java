package ar.com.hipotecario.backend.servicio.api.prisma;

import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.servicio.api.prisma.Reclamos.ReclamosBody;
import ar.com.hipotecario.backend.servicio.api.prisma.Reclamos.ReclamosParams;

//http://api-prisma-microservicios-homo.appd.bh.com.ar/swagger-ui.html
public class ApiPrisma extends Api {

	/* ========== Reclamos Api Controller ========== */

	// GET /v1/consultaTema
	public static Futuro<Consultas> get(Contexto contexto) {
		return futuro(() -> Consultas.get(contexto));
	}

	public static Futuro<Consultas> getByIdTema(Contexto contexto, String idTema) {
		return futuro(() -> Consultas.get(contexto, idTema));
	}

	public static Futuro<Consultas> get(Contexto contexto, String idTema, String descripcionTema, String idProducto, List<String> vigente, String codTema, String maxResults) {
		return futuro(() -> Consultas.get(contexto, idTema, descripcionTema, idProducto, vigente, codTema, maxResults));
	}

	// POST /v1/reclamo
	public static Futuro<Reclamos.ResponsePost> post(Contexto contexto, ReclamosBody reclamo) {
		return futuro(() -> Reclamos.post(contexto, reclamo));
	}

	// TODO: PATCH /v1/reclamo

	// GET /v1/reclamosContainer
	public static Futuro<Reclamos> getReclamosContainer(Contexto contexto, String idCobis, ReclamosParams params) {
		return futuro(() -> Reclamos.getReclamosContainer(contexto, idCobis, params));
	}

	public static Futuro<Reclamos> getReclamosContainer(Contexto contexto, String idCobis) {
		return futuro(() -> Reclamos.getReclamosContainer(contexto, idCobis));
	}

	// GET /v1/reclamosSAC
	public static Futuro<Reclamos> getReclamosSac(Contexto contexto, String idCobis, String maxResults) {
		return futuro(() -> Reclamos.getReclamosSac(contexto, idCobis, maxResults));
	}

	public static Futuro<Reclamos> getReclamosSac(Contexto contexto, String idCobis) {
		return futuro(() -> Reclamos.getReclamosSac(contexto, idCobis));
	}

}
