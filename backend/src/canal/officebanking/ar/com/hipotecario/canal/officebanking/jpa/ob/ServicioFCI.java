package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;
import java.util.Optional;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.servicio.api.inversiones.PerfilInversor;
import ar.com.hipotecario.backend.servicio.api.inversiones.PerfilInversor.EnumOperacionPI;
import ar.com.hipotecario.backend.servicio.api.personas.ApiPersonas;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumPerfilInversorOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.PreguntasPerfilInversorOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.RespuestasPerfilInversorOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.PreguntaOBRepositorio;

public class ServicioFCI extends ServicioOB {

	private PreguntaOBRepositorio repo;

	public ServicioFCI(ContextoOB contexto) {
		super(contexto);
		repo = new PreguntaOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<List<PreguntasPerfilInversorOB>> findAll() {
		return futuro(() -> repo.findAll());
	}

	public EnumPerfilInversorOB obtenerPerfil(List<Integer> misRespuestas) {
		List<PreguntasPerfilInversorOB> preguntas = repo.findAll();
		if (preguntas.size() != misRespuestas.size()) {
			return null;
		}

		int suma = 0;
		int indice = 0;

		for (PreguntasPerfilInversorOB pregunta : preguntas) {
			Integer idRespuesta = misRespuestas.get(indice);
			Optional<RespuestasPerfilInversorOB> res = pregunta.respuestas.stream().filter(r -> r.id == idRespuesta).findFirst();
			if (res.isPresent()) {
				suma = suma + res.get().escala;
			} else {
				return null;
			}
			indice++;
		}

		if (suma < 3) {
			return null;
		} else {
			// SI E37-E40 == 0 (5), SI E48-E51 == 0 (7), SI E61-E63 == 0 (9), SI E67-E72 ==
			// 0 (10)
			boolean esConservador = misRespuestas.get(4) == 0 || misRespuestas.get(6) == 0 || misRespuestas.get(8) == 0 || misRespuestas.get(9) == 0;
			if (esConservador) {
				return EnumPerfilInversorOB.CONSERVADOR;
			} else {
				// SI E87(SUMA) > E95 (54)
				boolean esArriesgado = suma > 54;
				if (esArriesgado) {
					return EnumPerfilInversorOB.ARRIESGADO;
				} else {
					// SI E87(SUMA) > E96 ()
					boolean esModerado = suma > 36;
					if (esModerado) {
						return EnumPerfilInversorOB.MODERADO;
					}
				}
			}
			return EnumPerfilInversorOB.CONSERVADOR;
		}
	}

	public PerfilInversor setPerfil(ContextoOB contexto, String idCliente, EnumPerfilInversorOB perfilInversor) {

		PerfilInversor perfilActual = ApiPersonas.perfilInversor(contexto, idCliente).tryGet();
		EnumOperacionPI operacion;
		if (perfilActual.perfilInversor == null || empty(perfilActual)) {
			operacion = EnumOperacionPI.INSERTAR;
		} else
			operacion = EnumOperacionPI.ACTUALIZAR;

		return ApiPersonas.administrarPerfil(contexto, idCliente, operacion, perfilInversor.getCodigo()).get();
	}

}