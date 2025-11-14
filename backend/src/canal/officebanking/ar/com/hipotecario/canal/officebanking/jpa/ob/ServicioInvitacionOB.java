package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.time.LocalDateTime;
import java.util.List;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.notificaciones.ApiNotificaciones;
import ar.com.hipotecario.backend.servicio.api.notificaciones.EnvioEmail;
import ar.com.hipotecario.backend.servicio.api.personas.ApiPersonas;
import ar.com.hipotecario.backend.servicio.api.personas.Cuils;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoInvitacionOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.InvitacionOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.InvitacionOBRepositorio;

public class ServicioInvitacionOB extends ServicioOB {

	private InvitacionOBRepositorio repo;

	public ServicioInvitacionOB(ContextoOB contexto) {
		super(contexto);
		repo = new InvitacionOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<List<InvitacionOB>> find(Contexto contexto, EmpresaOB empresa) {
		return futuro(() -> repo.findByField("empresa", empresa));
	}

	public Futuro<InvitacionOB> findByToken(Contexto contexto, String token) {
		return futuro(() -> repo.findByFieldUnique("token", token));
	}

	public Futuro<InvitacionOB> update(InvitacionOB invitacion) {
		return futuro(() -> repo.update(invitacion));
	}

	public Futuro<InvitacionOB> enviarInvitacion(ContextoOB contexto, Long numeroDocumento, String nombre, String apellido, String correo) {
		String token = Config.encriptar(correo + LocalDateTime.now());
		EnvioEmail result = ApiNotificaciones.envioInvitacionOB(contexto, correo, nombre, apellido, contexto.sesion().empresaOB.razonSocial, token).get();
		if (result.codigoHttp() == 200) {
			InvitacionOB enviada = this.findEnviada(contexto, Long.valueOf(numeroDocumento), contexto.sesion().empresaOB).tryGet();
			if (empty(enviada)) {
				InvitacionOB nueva = new InvitacionOB();
				nueva.nombre = nombre;
				nueva.apellido = apellido;
				nueva.correo = correo;
				nueva.numeroDocumento = numeroDocumento;
				nueva.empresa = contexto.sesion().empresaOB;
				nueva.token = token;
				return futuro(() -> repo.create(nueva));
			} else {
				enviada.token = token;
				enviada.estado = EnumEstadoInvitacionOB.REENVIADA;
				return futuro(() -> repo.update(enviada));
			}
		}
		return null;
	}

	public Futuro<InvitacionOB> cancelarInvitacion(ContextoOB contexto, Long numeroDocumento) {
		InvitacionOB enviada = this.findEnviada(contexto, numeroDocumento, contexto.sesion().empresaOB).tryGet();
		if (!empty(enviada)) {
			enviada.estado = EnumEstadoInvitacionOB.CANCELADA;
			return futuro(() -> repo.update(enviada));
		}
		return futuro(() -> null);
	}

	public Futuro<InvitacionOB> findEnviada(Contexto contexto, Long numeroDocumento, EmpresaOB empresa) {
		return futuro(() -> repo.enviada(numeroDocumento, empresa));
	}

	public Futuro<InvitacionOB> findByToken(Contexto contexto, Long numeroDocumento, String token) {
		return futuro(() -> repo.findByToken(numeroDocumento, token));
	}

	public Objeto obtenerNombre(ContextoOB contexto, Long numeroDocumento) {
		Cuils datosPersona = ApiPersonas.cuils(contexto, String.valueOf(numeroDocumento)).get();
		if (datosPersona.size() > 0) {
			Objeto datos = new Objeto();
			try {
				String nombreApellido = datosPersona.get(0).apellidoYNombre.trim();
				String[] splitStr = nombreApellido.split("\\s", 2);
				if (splitStr.length > 0) {
					datos.set("nombre", splitStr[1]);
					datos.set("apellido", splitStr[0]);
				} else {
					datos.set("nombre", nombreApellido);
				}
			} catch (Exception e) {
				return null;
			}
			return datos;
		}
		return null;
	}
	
	public Objeto obtenerNombrePorDocumentoyCuil(ContextoOB contexto, Long documento_cuil) {		
		Long numeroDocumento = null;
		boolean esCuil = false;
		
		if(documento_cuil.toString().length() > 9) {
			esCuil = true;
			numeroDocumento = Long.parseLong(documento_cuil.toString().substring(2, 10));
		}else {
			numeroDocumento = documento_cuil;
		}
		
		Cuils datosPersona = ApiPersonas.cuils(contexto, String.valueOf(numeroDocumento)).get();
				
		if (datosPersona.size() > 0) {
			Objeto datos = new Objeto();
			try {
				String nombreApellido = 
						esCuil == false ? datosPersona.get(0).apellidoYNombre.trim() : 
							datosPersona.stream().filter(m -> m.cuil.equals(documento_cuil.toString())).findFirst().get().apellidoYNombre.trim();
				String[] splitStr = nombreApellido.split("\\s", 2);
				if (splitStr.length > 0) {
					datos.set("nombre", splitStr[1]);
					datos.set("apellido", splitStr[0]);
				} else {
					datos.set("nombre", nombreApellido);
				}
			} catch (Exception e) {
				return null;
			}
			return datos;
		}
		return null;
	}

	public Objeto obtenerNombreMultipleCuils(ContextoOB contexto, Long numeroDocumento) {
		Objeto datos = new Objeto();
		
		Cuils datosPersona = ApiPersonas.cuils(contexto, String.valueOf(numeroDocumento)).get();
		try {
			String nombreApellido = datosPersona.get(0).apellidoYNombre.trim();
			String[] splitStr = nombreApellido.split("\\s", 2);
			if (splitStr.length > 0) {
				datos.set("nombre", splitStr[1]);
				datos.set("apellido", splitStr[0]);
			} else {
				datos.set("nombre", nombreApellido);
			}
		} catch (Exception e) {
			return null;
		}
				
		return datos;
	}
	
}