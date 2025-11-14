package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.math.BigDecimal;
import java.util.UUID;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

/**
 * @author C06861
 *
 */
public class RescateSL extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */

	public Moneda Moneda;
	public String Importe;
	public String FactorConversion;
	public CodSucursal CodSucursal;
	public Boolean EsTotal;
	public TpOrigenSol TpOrigenSol;
	public FormaCobro FormaCobro;
	public InversionFondo InversionFondo;
	public CodAgenteColocador CodAgenteColocador;
	public String COD;
	public String ValorCuotaparte;
	public UsuarioAut UsuarioAut;
	public CuentaBancaria CuentaBancaria;
	public CuentaFondo CuentaFondo;
	public TpEstadoSol TpEstadoSol;
	public String FechaAut;
	public Cuotapartista Cuotapartista;
	public String CodigoAuditoria;
	public Fecha FechaConcertacion;
	public String PorcGastos;
	public Boolean EstaAnulado;
	public Long NumSolicitud;
	public String IDSolicitud;
	public Fecha Hora;
	public CodTpOrigenSol CodTpOrigenSol;
	public Radicacion Radicacion;
	public String FechaAcreditacion;
	public CtaBancaria CtaBancaria;
	public UsuarioIngreso UsuarioIngreso;

	public static class Moneda extends ApiObjeto {
		public String Description;
		public String COD;
		public String ID;
		public String CODRelacionado;
	}

	public static class CodSucursal extends ApiObjeto {

	}

	public static class TpOrigenSol extends ApiObjeto {
		public String ID;
	}

	public static class FormaCobro extends ApiObjeto {
		public String COD;
		public String ID;
	}

	public static class InversionFondo extends ApiObjeto {

		public TipoValorCuotaParte TipoValorCuotaParte;
		public CondicionIngresoEgreso CondicionIngresoEgreso;
		public Fondo Fondo;

		public static class TipoValorCuotaParte extends ApiObjeto {
			public String Description;
			public String COD;
			public String ID;
			public String CODRelacionado;
		}

		public static class CondicionIngresoEgreso extends ApiObjeto {
			public String COD;
			public String ID;
		}

		public static class Fondo extends ApiObjeto {
			public String COD;
			public String ID;
		}

	}

	public static class CodAgenteColocador extends ApiObjeto {
	}

	public static class UsuarioAut extends ApiObjeto {
	}

	public static class CuentaBancaria extends ApiObjeto {
	}

	public static class CuentaFondo extends ApiObjeto {
		public String NumeroCuenta;
	}

	public static class TpEstadoSol extends ApiObjeto {
		public String ID;
	}

	public static class Cuotapartista extends ApiObjeto {
		public String COD;
		public String ID;
	}

	public static class CodTpOrigenSol extends ApiObjeto {
	}

	public static class Radicacion extends ApiObjeto {
		public AgenteColocador AgenteColocador;
		public Sucursal Sucursal;
		public CanalVenta CanalVenta;
		public OficialCuenta OficialCuenta;

		public static class AgenteColocador extends ApiObjeto {
			public String COD;
			public String ID;
		}

		public static class Sucursal extends ApiObjeto {
			public String COD;
			public String ID;
		}

		public static class CanalVenta extends ApiObjeto {

		}

		public static class OficialCuenta extends ApiObjeto {
			public String COD;
		}
	}

	public static class CtaBancaria extends ApiObjeto {
		public String COD;
		public String ID;
	}

	public static class UsuarioIngreso extends ApiObjeto {
		public String COD;
		public String ID;
	}

	/* ========== SERVICIOS ========== */

	// API-Inversiones_RescateSLInsert
	public static RescateSL post(Contexto contexto, Integer cantCuotapartes, String ctaBancaria, String cuotapartista, Boolean esTotal, String moneda, BigDecimal importe, String fondo, String condicionIngresoEgreso, String tipoValorCuotaParte) {
		ApiRequest request = new ApiRequest("RescateSL", "inversiones", "POST", "/v1/rescateSL", contexto);
		request.body("pSolicitudRescate.CantCuotapartes", cantCuotapartes);
		request.body("pSolicitudRescate.CtaBancaria", ctaBancaria);
		request.body("pSolicitudRescate.Cuotapartista", cuotapartista);
		request.body("pSolicitudRescate.EsTotal", esTotal ? 1 : 0);
		request.body("pSolicitudRescate.FechaAcreditacion", Fecha.ahora().string("yyyy-MM-dd"));
		request.body("pSolicitudRescate.FechaConcertacion", Fecha.ahora().string("yyyy-MM-dd"));
		request.body("pSolicitudRescate.FormaCobro", "28");
		request.body("pSolicitudRescate.IDSolicitud", UUID.randomUUID().toString());
		request.body("pSolicitudRescate.Importe", importe.toString());
		request.body("pSolicitudRescate.InversionFondo.Fondo", fondo);
		request.body("pSolicitudRescate.InversionFondo.CondicionIngresoEgreso", condicionIngresoEgreso);
		request.body("pSolicitudRescate.InversionFondo.TipoValorCuotaParte", tipoValorCuotaParte);
		request.body("pSolicitudRescate.Moneda", moneda);
		request.body("pSolicitudRescate.PorcGastos", 0);
		request.body("pSolicitudRescate.PorcGtoBancario", 0);
		request.body("pSolicitudRescate.TpOrigenSol", "IE");
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("El cuotapartista no tiene ese importe disponible.", response.contains("El Cuotapartista no tiene ese Importe disponible"), request, response);
		ApiException.throwIf("Solicitud a Cta. Monetaria Inexistente.", response.contains("Solicitud a Cta. Monetaria Inexistente"), request, response);
		ApiException.throwIf("Es necesario indicar el importe o la cantidad de cuotapartes del Rescate.", response.contains("Es necesario indicar el importe o la cantidad de cuotapartes del Rescate"), request, response);
		ApiException.throwIf("El Cuotapartista no tiene Cuotapartes disponibles.", response.contains("El Cuotapartista no tiene Cuotapartes disponibles"), request, response);
		ApiException.throwIf(!response.http(200, 204), request, response);

		return response.crear(RescateSL.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");

		String idCuentaBancaria = "22195";
		String moneda = "1";
		String numeroCuenta = "407100015287054";
		String cuotapartista = "16220";
		String importe = "2000";
		String fondo = "9";
		String condicionIngresoEgreso = "UNICO";
		String tipoValorCuotaParte = "9C";

		SuscripcionSL suscripcionSL = SuscripcionSL.post(contexto, idCuentaBancaria, moneda, numeroCuenta, cuotapartista, importe, fondo, condicionIngresoEgreso, tipoValorCuotaParte);
		if (suscripcionSL != null) {

			int cantCuotapartes = 0;
			BigDecimal importeRescate = new BigDecimal(importe);
			Boolean esTotal = Boolean.FALSE;

			RescateSL rescateSL = post(contexto, cantCuotapartes, idCuentaBancaria, cuotapartista, esTotal, moneda, importeRescate, fondo, condicionIngresoEgreso, tipoValorCuotaParte);

			imprimirResultado(contexto, rescateSL);
		}
	}

}