package ar.com.hipotecario.mobile.api;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.servicio.RestCheques;

public class MBCheques {

	public static RespuestaMB chequesPendientes(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();
		for (Cuenta cuenta : contexto.cuentas()) {
			if (cuenta.esCuentaCorriente()) {
				String numeroCorto = cuenta.numeroCorto();
				ApiResponseMB responseAprobados = RestCheques.cheques(contexto, numeroCorto, cuenta.numero(), "Pendientes");
				if (responseAprobados.hayError()) {
					return RespuestaMB.error();
				}
				String simboloMoneda = cuenta.simboloMoneda();
				for (Objeto item : responseAprobados.objetos()) {
					Objeto chequeAprobado = new Objeto();
					chequeAprobado.set("fechaEmision", item.date("fechaEmision", "yyyy-MM-dd", "dd/MM/yyyy"));
					chequeAprobado.set("bancoEmisor", item.string("idBancoEmisor"));
					chequeAprobado.set("nroCuentaGirada", simboloMoneda + Formateador.tipoCuenta(item.string("nroCuentaGirada")) + " XXXX-" + Formateador.ultimos4digitos(item.string("nroCuentaGirada")));
					chequeAprobado.set("numeroCheque", item.string("nroCheque"));
					chequeAprobado.set("fechaAcreditacion", item.date("fechaEfectivo", "yyyy-MM-dd", "dd/MM/yyyy"));
					chequeAprobado.set("importe", item.bigDecimal("importe"));
					chequeAprobado.set("importeFormateado", Formateador.importe(item.bigDecimal("importe")));
					chequeAprobado.set("simboloMoneda", simboloMoneda);
					respuesta.add("pendientes", chequeAprobado);
				}
			}
		}
		return respuesta;
	}

	public static RespuestaMB chequesRechazados(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();
		for (Cuenta cuenta : contexto.cuentas()) {
			if (cuenta.esCuentaCorriente()) {
				String simboloMoneda = cuenta.simboloMoneda();
				String numeroCorto = cuenta.numeroCorto();
				ApiResponseMB responseRechazados = RestCheques.cheques(contexto, numeroCorto, cuenta.numero(), "Rechazados");
				if (responseRechazados.hayError()) {
					return RespuestaMB.error();
				}
				for (Objeto item : responseRechazados.objetos()) {
					Objeto chequeRechazado = new Objeto();
					chequeRechazado.set("fechaRechazo", item.date("fecha", "yyyy-MM-dd", "dd/MM/yyyy"));
					chequeRechazado.set("numeroCheque", item.string("numero"));
					chequeRechazado.set("importe", item.bigDecimal("valor"));
					chequeRechazado.set("importeFormateado", Formateador.importe(item.bigDecimal("valor")));
					chequeRechazado.set("estado", item.string("estado"));
					chequeRechazado.set("motivoRechazo", item.string("motivo"));
					chequeRechazado.set("simboloMoneda", simboloMoneda);
					respuesta.add("rechazados", chequeRechazado);
				}
			}
		}
		return respuesta;
	}

}
