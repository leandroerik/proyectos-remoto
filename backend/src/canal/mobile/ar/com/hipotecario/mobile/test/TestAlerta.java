package ar.com.hipotecario.mobile.test;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.api.MBNotificaciones;

public class TestAlerta {

	public static void main(String[] args) {
		try {

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// OK
	public static void configuracionAlerta() {
		String idCobis = "133366";
		ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
		RespuestaMB res = MBNotificaciones.configuracionAlertas(contexto);
		System.out.println(res);
	}

//	HACE FALTA LOGUEARSE PRIMERO

//	public static void modificarConfiguracionAlerta() {
//		
//		String idCobis = "133366";
//		Boolean ingresos = false;
//		Boolean cambioClave = false;
//		Boolean desbloqueoClave = false;
//		Boolean agendarBeneficiario = false;
//		
//		Boolean buscarEmail = true;
//		Boolean buscarCelular = true;
//		Boolean buscarDomicilio = true;
//
//		Contexto contexto = new Contexto(idCobis, "1", "127.0.0.1");
////		
//		contexto.parametros.set("ingresos", ingresos);
//		contexto.parametros.set("cambioClave", cambioClave);
//		contexto.parametros.set("desbloqueoClave", desbloqueoClave);
//		contexto.parametros.set("agendarBeneficiario", agendarBeneficiario);
//
//		contexto.parametros.set("buscarEmail", buscarEmail);
//		contexto.parametros.set("buscarCelular", buscarCelular);
//		contexto.parametros.set("buscarDomicilio", buscarDomicilio);
//		
//		
//		ApiPersona.cliente(contexto);
//		Respuesta res = ApiNotificaciones.modificarConfiguracionAlertas(contexto);
//		System.out.println(res);
//		
//}

}
