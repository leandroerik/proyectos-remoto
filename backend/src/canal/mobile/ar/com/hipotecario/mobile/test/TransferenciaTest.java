package ar.com.hipotecario.mobile.test;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.api.MBAplicacion;
import ar.com.hipotecario.mobile.api.MBTransferencia;
import ar.com.hipotecario.mobile.lib.Objeto;

public class TransferenciaTest {

	public static void main(String[] args) {
		try {
			if (!MBAplicacion.funcionalidadPrendida("123457", "prendido_aceptar_cvu")) {
				System.out.println("APAGADO");
			} else {
				transferenciaTest();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void transferenciaTest() {
		try {
			String idCobis = "16260";
			String cuentaOrigen = "401800025981938";
			String cuentaDestino = "400000030826050";
			String monto = "200000";
			String forzarEmailOrigen = "lrossettibasigaluz@hipotecario.com.ar";
			Boolean empleadoDomestico = false;
			Boolean aceptaDDJJ = false;
			Boolean agendar = false;
			String concepto = "VAR";

			ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
			contexto.parametros.set("cuentaOrigen", cuentaOrigen);
			contexto.parametros.set("cuentaDestino", cuentaDestino);
			contexto.parametros.set("monto", monto);

			contexto.parametros.set("forzarEmailOrigen", forzarEmailOrigen);
			contexto.parametros.set("empleadoDomestico", empleadoDomestico);
			contexto.parametros.set("aceptaDDJJ", aceptaDDJJ);
			contexto.parametros.set("agendar", agendar);
			contexto.parametros.set("concepto", concepto);

			Objeto r = MBTransferencia.transferir(contexto);
			System.out.println(r);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
