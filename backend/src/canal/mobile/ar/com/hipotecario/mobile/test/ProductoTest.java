package ar.com.hipotecario.mobile.test;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.api.MBPago;
import ar.com.hipotecario.mobile.api.MBProducto;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;

public class ProductoTest {

	public static void main(String[] args) {
		try {
			// bajaTC();
			// bajadebitoAutomatico();
			// pruebaBajaEnCurso();
			// debitosAutomaticos();
			// consolidada();
			bajaCA();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private static void bajaTC() {
		// cobis= 4655491 titular "4304970011782207"; - //adicional "4304970011782215";
		String idCobis = "6198739";
		String idTarjeta = "4304960039939756";
		ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
		contexto.parametros.set("idTarjeta", idTarjeta);
		RespuestaMB respuesta = MBProducto.bajaTarjetaCreditoAdicional(contexto);
		System.out.println("#############################");
		System.out.println(respuesta);
		// RestPostventa.bajaPaquete(contexto, "BAJA_PAQUETES", "HB", "1", "1");
	}

	@SuppressWarnings("unused")
	private static void bajadebitoAutomatico() {
		String idCobis = "135706";
		String numeroCuenta = "300400000318855";
		String codigoAdhesion = "DD90003211";// "11155";
		ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
		contexto.parametros.set("numeroCuenta", numeroCuenta);
		contexto.parametros.set("codigoAdhesion", codigoAdhesion);
		RespuestaMB respuesta = MBPago.eliminarDebitoAutomatico(contexto);
		System.out.println("#############################");
		System.out.println(respuesta);
	}

	@SuppressWarnings("unused")
	private static void pruebaBajaEnCurso() {
		String idCobis = "133366";
		String numeroCuenta = "2404500010244503";
		String codigoAdhesion = "211155";
		String tipificacion = "BAJA_ADHESION_CA_PEDIDO";
		ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
		contexto.parametros.set("numeroCuenta", numeroCuenta);
		contexto.parametros.set("codigoAdhesion", codigoAdhesion);

		Boolean isEnCurso = false;

		if (MBProducto.tieneSolicitudEnCurso(contexto, "BAJA_SERV", codigoAdhesion, numeroCuenta)) {
			isEnCurso = true;
			System.out.println("SOLICITUD_EN_CURSO");
		}
		if (!isEnCurso) {
			// SqlResponse sqlResponse = ApiProducto.insertarReclamo(contexto, "BAJA_SERV",
			// numeroCuenta, codigoAdhesion, "1", "");
			SqlResponseMB sqlResponse = MBProducto.insertarReclamo(contexto, "BAJA_SERV", numeroCuenta, codigoAdhesion, "", "");
			if (sqlResponse.hayError) {
				System.out.println("ERROR_GENERANDO_RECLAMO");
			}
		}

	}

	@SuppressWarnings("unused")
	private static void debitosAutomaticos() {
		String idCobis = "133366";
		String numeroCuenta = "404500308269070";
		String codigoAdhesion = "0002081";
		ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
		contexto.parametros.set("numeroCuenta", numeroCuenta);
		contexto.parametros.set("codigoAdhesion", codigoAdhesion);
		RespuestaMB respuesta = MBPago.consultaDebitosAutomaticos(contexto);
		System.out.println("#############################");
		System.out.println(respuesta.get("servicios"));
		// System.out.println(ApiPago.getCuitEmpresa(contexto));
	}

	@SuppressWarnings("unused")
	private static void consolidada() {
		String idCobis = "113683";
		ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
		RespuestaMB respuesta = MBProducto.cuentas(contexto);
		System.out.println("#############################");
		System.out.println(respuesta);
	}

	private static void bajaCA() {
		String idCobis = "40253";
		String idCuenta = "405200308272629";
		ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
		contexto.parametros.set("idCuenta", idCuenta);
		RespuestaMB respuesta = MBProducto.bajaCajaAhorro(contexto);
		System.out.println("#############################");
		System.out.println(respuesta);
	}

}
