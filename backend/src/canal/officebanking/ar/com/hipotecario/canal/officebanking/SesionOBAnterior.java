package ar.com.hipotecario.canal.officebanking;

import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.Sesion;
import ar.com.hipotecario.backend.base.Base;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.personas.PreguntasRiesgoNet;

public class SesionOBAnterior extends Sesion {

	private static final long serialVersionUID = 1L;

	/* ========== ATRIBUTOS =========== */
	public String codigoUsuario;
	public String cuitEmpresa;
	public Captcha captcha = new Captcha();
	public Token token = new Token();
	public TokenCorreo tokenCorreo = new TokenCorreo();
	public PreguntasRiesgoNet preguntasRiesgoNet;
	public List<String> validadores = new ArrayList<String>();

	/* ========== CLASES ========== */
	public static class Captcha extends Base {
		public String texto;
		public Fecha fechaValidacion;
	}

	public static class Token extends Base {
		public String codigo;
		public String estado;
		public String cookie;
		public Fecha fechaValidacion = Fecha.nunca();
	}

	public static class TokenCorreo extends Base {
		public String codigo;
		public String correo;
		public Fecha fechaValidacion = Fecha.nunca();
	}

	/* ========== INICIALIZACION =========== */
	public void crearSesion() {
		Sesion sesion = contexto.sesion(Sesion.class);
		sesion.cuil = this.cuil;
		sesion.idCobis = this.idCobis;
		sesion.numeroDocumento = this.numeroDocumento;
		sesion.usuario = this.usuario;
		sesion.sucursal = this.sucursal;
		sesion.fechaLogin = this.fechaLogin;
		sesion.fechaUltimaActividad = this.fechaLogin;
		sesion.save();
		this.fechaUltimaActividad = Fecha.ahora();
		this.save();
	}

	public Boolean usuarioLogueado() {
		return cuitEmpresa != null;
	}

	public void actualizarFechaUltimaActividad() {
		fechaUltimaActividad = Fecha.ahora();
		save();
	}

	public Boolean expirada() {
		if (usuarioLogueado()) {
			Fecha fechaExpiracion = fechaUltimaActividad.sumarMinutos(contexto.config.integer("ob_maximo_inactividad"));
			Boolean sesionExpirada = fechaExpiracion.esAnterior(Fecha.ahora());

			String key = "SESION_OB_" + this.codigoUsuario;
			if (sesionExpirada) {
				contexto.del(key);
			} else {
				try {
					String jsonSesionOB = contexto.get(key);
					Objeto datos = Objeto.fromJson(jsonSesionOB);
					datos.set("fechaExpiracion", Fecha.ahora().sumarMinutos(contexto.config.integer("ob_maximo_inactividad")));
					contexto.set(key, datos.toSimpleJson());
				} catch (Exception e) {
					return false;
				}
			}

			return sesionExpirada;
		}
		return false;
	}

}
