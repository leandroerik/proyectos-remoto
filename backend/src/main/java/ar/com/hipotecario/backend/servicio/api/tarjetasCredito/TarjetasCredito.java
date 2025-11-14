package ar.com.hipotecario.backend.servicio.api.tarjetasCredito;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.dto.prisma.TransaccionesRequest;

public class TarjetasCredito {

	// GET /v1/tarjetascredito/ultimaliquidacion
	public static Resumen descargarResumenPDF(ContextoOB contexto, String numerocuenta, String keyvalue) {	
		return Resumen.getResumen(contexto, numerocuenta, keyvalue);
	}

	// POST v1/cuentas/{idcuenta}/stopdebit
	public static StopDebit postStopDebit(ContextoOB contexto, String numeroCuenta, String idcuenta) {
		return StopDebit.postStopDebit(contexto, numeroCuenta, idcuenta);
	}
	
	// GET /v1/tarjetascredito
	public static List<Tarjetas> obtenerTarjetasCredito (ContextoOB contexto, String idCobis, Boolean adicionales) {
		return Tarjetas.getTarjetas(contexto, idCobis, adicionales);
	}
	
	// GET /v1/tarjetascredito/obtenerIdPrisma
	public static List<ClienteSO> obtenerDatosClienteSO (ContextoOB contexto, String cuenta) {
		return ClienteSO.get(contexto, cuenta);
	}
		
	// GET /v1/tarjetascredito/cuentas
	public static Object obtenerCuentasPrisma (ContextoOB contexto, String idPrisma) {
		return CuentasPrisma.get(contexto, idPrisma);
	}

	// GET /v1/tarjetascredito/vencimientos
	public static Object obtenerVencimientos (ContextoOB contexto, String cuenta,String idPrisma) {
		return Vencimientos.get(contexto, cuenta, idPrisma);
	}
		
	// GET /v1/tarjetascredito/tarjetaEmpresa
	public static TarjetaEmpresa obtenerListadoTarjetas (ContextoOB contexto, String cuenta, String idPrisma) {
		return TarjetaEmpresa.get(contexto, cuenta, idPrisma);
	}
	
	// POST /v1/tarjetascredito/transacciones
	public static TransaccionesPrisma obtenerTransacciones (ContextoOB contexto, TransaccionesRequest request) {
		return TransaccionesPrisma.post(contexto, request);
	}
	
	// GET /v1/tarjetascredito/{idtarjeta}
	public static DatosPagoTC obtenerDatosPago (ContextoOB contexto, String idTarjeta) {
		return DatosPagoTC.get(contexto, idTarjeta).get(0);
	}
	
	public static PagoTarjeta pagoTarjeta(ContextoOB contexto, HashMap<String, String> dto, BigDecimal importe, String canal) {
		return PagoTarjeta.post(contexto, dto, importe, canal);
	}
}