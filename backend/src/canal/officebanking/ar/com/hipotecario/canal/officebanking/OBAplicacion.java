package ar.com.hipotecario.canal.officebanking;

import java.util.Date;
import java.util.Set;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Version;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.servicio.api.cuentas.MovimientosCajaAhorros;
import ar.com.hipotecario.backend.servicio.sql.hb_be.UsuariosOBAnterior;
import ar.com.hipotecario.backend.servicio.sql.hb_be.UsuariosOBAnterior.UsuarioOBAnterior;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioParametroOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.ParametroOB;

import static ar.com.hipotecario.backend.util.LoginLDAP.loginLDAP;

public class OBAplicacion extends ModuloOB {

	public static Object health(ContextoOB contexto) {
		Boolean enMantenimiento = contexto.config.bool("ob_en_mantenimiento", false);

		Objeto respuesta = new Objeto();
		respuesta.set("status", "UP");
		respuesta.set("enMantenimiento", enMantenimiento);

		return respuesta;
	}

	public static Object config(ContextoOB contexto) {
		Objeto datos = new Objeto();
		datos.set("fecha", Fecha.hoy().string("yyyy-MM-dd"));
//		MavenXpp3Reader reader = new MavenXpp3Reader();
		try {
//			Model model = reader.read(new FileReader("pom.xml"));
			ServicioParametroOB parametroOB = new ServicioParametroOB(contexto);
			ParametroOB param = null;
			try{
				param = parametroOB.find("isTransmitOn").tryGet();
			}catch (Exception e ){}
			if(param!=null){
				datos.set("isTransmitOn",Boolean.valueOf(param.valor.toString()));
			}
			datos.set("version", "1.0.1");
			datos.set("versionOB1023", "1.0.2");
		} catch (Exception e) {
			datos.set("version", "");
		}
		return respuesta("datos", datos);
	}

	public static Object headers(ContextoOB contexto) {
		Objeto datos = new Objeto();
		Set<String> claves = contexto.request.headers();
		for (String clave : claves) {
			datos.set(clave, contexto.request.headers(clave));
		}
		return datos;
	}

	public static Object version(Contexto contexto) {
		return Version.get();
	}

	public static String status(ContextoOB contexto) {
		Date x = new Date();
		StringBuilder reporte = new StringBuilder();

		try {
			Date inicio = new Date();
			contexto.set("prueba", "prueba");
			reporte.append("REDIS ESCRITURA: OK " + (new Date().getTime() - inicio.getTime()) + "ms").append("<br/>");
		} catch (Exception e) {
			reporte.append("REDIS: EXCEPTION").append("<br/>");
		}

		try {
			Date inicio = new Date();
			String estado = "prueba".equals(contexto.get("prueba")) ? "OK" : "ERROR";
			reporte.append("REDIS LECTURA: " + estado + " " + (new Date().getTime() - inicio.getTime()) + "ms").append("<br/>");
		} catch (Exception e) {
			reporte.append("REDIS LECTURA: EXCEPTION").append("<br/>");
		}

		try {
			Date inicio = new Date();
			UsuarioOBAnterior usuario = UsuariosOBAnterior.getPorCuil(contexto, "20135752003");
			String estado = "20135752003".equals(usuario.usu_cuil) ? "OK" : "ERROR";
			reporte.append("SQL SELECT: " + estado + " " + (new Date().getTime() - inicio.getTime()) + "ms").append("<br/>");
		} catch (Exception e) {
			reporte.append("SQL SELECT: EXCEPTION").append("<br/>");
		}

		try {
			Date inicio = new Date();
			ApiRequest request = new ApiRequest("CuentasGetMovimientos", "cuentas", "GET", "/v1/cajasahorros/{idcuenta}/movimientos", contexto);
			MovimientosCajaAhorros.get(contexto, "400400011740843", Fecha.hoy().restarDias(50), Fecha.hoy(), "1", 'A', 0, 'T', false, null);
			reporte.append("API CUENTAS: " + "OK" + " " + (new Date().getTime() - inicio.getTime()) + "ms").append(" " + request.fullUrl()).append("<br/>");
		} catch (Exception e) {
			reporte.append("API CUENTAS: EXCEPTION").append("<br/>");
		}

		try {
			Date inicio = new Date();
			contexto.parametros.set("numeroDocumento", "13575200");
			contexto.parametros.set("usuario", "0");
			contexto.parametros.set("clave", "0");
			OBLogin.login(contexto);
			reporte.append("LOGIN: " + "OK" + " " + (new Date().getTime() - inicio.getTime()) + "ms").append("<br/>");
		} catch (Exception e) {
			reporte.append("LOGIN: EXCEPTION").append("<br/>");
		}

		try {
			Date inicio = new Date();
			OBCuentas.ultimosMovimientos(contexto);
			reporte.append("ULTIMOS MOVIMIENTOS BACKEND: " + "OK" + " " + (new Date().getTime() - inicio.getTime()) + "ms").append("<br/>");
		} catch (Exception e) {
			reporte.append("ULTIMOS MOVIMIENTOS BACKEND: EXCEPTION").append("<br/>");
		}

		try {
			Date inicio = new Date();
			OBCuentas.cuentas(contexto);
			reporte.append("OBTENER CUENTAS: " + "OK" + " " + (new Date().getTime() - inicio.getTime()) + "ms").append("<br/>");
		} catch (Exception e) {
			reporte.append("OBTENER CUENTAS: EXCEPTION").append("<br/>");
		}

		reporte.append("TOTAL " + (new Date().getTime() - x.getTime()) + "ms").append("<br/>");

		return reporte.toString();
	}
}