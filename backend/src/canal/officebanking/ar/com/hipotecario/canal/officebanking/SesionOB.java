package ar.com.hipotecario.canal.officebanking;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.Sesion;
import ar.com.hipotecario.backend.base.Base;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.personas.PreguntasRiesgoNet;
import ar.com.hipotecario.backend.servicio.api.tarjetasCredito.TarjetaEmpresa;
import ar.com.hipotecario.canal.libreriariesgofraudes.application.dto.RecommendationDTO;
import ar.com.hipotecario.canal.officebanking.dto.prisma.TarjetaPrismaSesionOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.TarjetaVirtualOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;

public class SesionOB extends Sesion {

	private static final long serialVersionUID = 1L;

	/* ========== ATRIBUTOS =========== */
	public String rol;
	public Captcha captcha = new Captcha();
	public Transmit transmit = new Transmit();
	public Token token = new Token();
	public String sessionId;
	public RecommendationDTO resultadoRecomendacion = null ;
	public TokenCorreo tokenCorreo = new TokenCorreo();
	public TokenSMS tokenInvitacionSMS = new TokenSMS();
	public TokenSMS tokenActualizarSMS = new TokenSMS();
	public TokenActivarSoftToken tokenActivarSoftToken = new TokenActivarSoftToken();
	public TokenCorreo tokenInvitacionCorreo = new TokenCorreo();
	public PreguntasRiesgoNet preguntasRiesgoNet;
	public String userAgent = "";
	public List<TarjetaVirtualOB> tarjetasVirtuales = null;
	
	public LocalDateTime ultimoAccesoHoy = null;
	public Boolean hasSoftToken = false;

	public String uuid = null;

	public UsuarioOB usuarioOB = null;
	public EmpresaOB empresaOB = null;
	public List<TarjetaEmpresa.Card> tarjetasPrisma;
	public String challenge = null;


	/* ========== CLASES ========== */
	public static class Captcha extends Base {
		public String texto;
		public Fecha fechaValidacion;
	}

	public static class Transmit extends Base {
		public String checksum;
		public String csmIdAuth;
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

	public static class TokenSMS extends Base {
		public String codigo;
		public String telefono;
		public Fecha fechaValidacion = Fecha.nunca();
	}

	public static class TokenActivarSoftToken extends Base {
		public String codigo;
		public String estado;
		public String cookie;
		public Fecha fechaValidacion = Fecha.nunca();
	}

	/* ========== INICIALIZACION =========== */
	public void crearSesion() {
		Sesion sesion = contexto.sesion(Sesion.class);
		sesion.sucursal = this.sucursal;
		sesion.fechaLogin = this.fechaLogin;
		sesion.fechaUltimaActividad = this.fechaLogin;
		sesion.save();
		this.fechaUltimaActividad = Fecha.ahora();
		this.save();
	}

	/* ========== MANEJO DE SESION =========== */
	public Boolean usuarioLogueado() {
		return !fechaLogin.isNull() && usuarioOB.cuil != null;
	}

	public Boolean usuarioPseudoLogueado() {
		return usuarioOB.cuil != null;
	}

	public void actualizarFechaUltimaActividad() {
		fechaUltimaActividad = Fecha.ahora();
		save();
	}

	public Boolean expirada() {
		if (usuarioLogueado()) {
			Fecha fechaExpiracion = fechaUltimaActividad.sumarMinutos(contexto.config.integer("ob_maximo_inactividad"));
			Boolean sesionExpirada = fechaExpiracion.esAnterior(Fecha.ahora());

			String key = "SESION_OB_" + this.usuarioOB.codigo;
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

	/* ========== METODOS =========== */
	public String rol() {
		String rol = this.rol;
		rol = "1".equals(rol) ? "OPERADOR_INICIAL" : rol;
		rol = "2".equals(rol) ? "OPERADOR" : rol;
		return rol;
	}

	public Boolean esOperadorInicial() {
		return "1".equals(rol);
	}

	public Boolean esOperador() {
		return "2".equals(rol);
	}

}