package ar.com.hipotecario.mobile.test;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.api.MBTarjetas;

public class TarjetaDebitoTest {

	public static void main(String[] args) {
		try {
			// consolidadaTarjetasFullTest();
			// limitesTDTest();
			// modificarlimitesTDTest();
			// stopDebitTC();
			repoTD();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected static void consolidadaTarjetasFullTest() {
		try {
			// String idCobis = "3636393";
			String idCobis = "133366";
			ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
			RespuestaMB r = MBTarjetas.consolidadaTarjetasFull(contexto);
			System.out.println(r);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected static void limitesTDTest() {
		try {
			// "4900353"; - "4998590237644709";
			// "133366"; - //"4998590262290303" o "4998590214453603";
			// "4601104"; - //4998590237644808
			String idCobis = "133366";
			String numeroTD = "4998590262290303";
			ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
			contexto.parametros.set("idTarjetaDebito", numeroTD);
			RespuestaMB r = MBTarjetas.limitesTarjetaDebito(contexto);
			System.out.println(r);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected static void modificarlimitesTDTest() {
		try {
			// "4900353"; - "4998590237644709";
			// "133366"; - //"4998590262290303" o "4998590214453603";
			String idCobis = "133366";
			String numeroTD = "4998590262290303";
			String idLimiteExtraccion = "11";
			ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
			contexto.parametros.set("idTarjetaDebito", numeroTD);
			contexto.parametros.set("limiteExtraccion", idLimiteExtraccion);
			RespuestaMB r = MBTarjetas.modificarLimiteTarjetaDebito(contexto);
			System.out.println(r);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected static void stopDebitTC() {
		String idCobis = "133366";
		String cuenta = "0132144247";
		ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
		contexto.parametros.set("cuenta", cuenta);
		RespuestaMB r = MBTarjetas.stopDebit(contexto);
		System.out.println(r);
	}

	private static void repoTD() {
		// IDCobis 3061214 - Nro TD F - 4998590301080301 - Nro TD F inactiva
		// 4998590089568121
		String idCobis = "886336";
		String id = "4998590239075100";
		ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
		contexto.parametros.set("idTarjetaDebito", id);
		RespuestaMB r = MBTarjetas.reposicionTD(contexto);
		System.out.println(r);
	}

}
