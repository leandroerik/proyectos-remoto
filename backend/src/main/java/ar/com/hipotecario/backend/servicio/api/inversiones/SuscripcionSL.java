package ar.com.hipotecario.backend.servicio.api.inversiones;

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
public class SuscripcionSL extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */

	public Moneda Moneda;
	public String FactorConversion;
	public TpOrigenSol TpOrigenSol;
	public InversionFondo InversionFondo;
	public String COD;
	public CuentaBancaria CuentaBancaria;
	public CuentaFondo CuentaFondo;
	public TpEstadoSol TpEstadoSol;
	public Cuotapartista Cuotapartista;
	public String CodigoAuditoria;
	public Fecha FechaConcertacion;
	public String PorcGastos;
	public Boolean EstaAnulado;
	public Long NumSolicitud;
	public String IDSolicitud;
	public Fecha Hora;
	public Radicacion Radicacion;
	public Fecha FechaAcreditacion;
	public UsuarioIngreso UsuarioIngreso;

	public static class Moneda extends ApiObjeto {
		public String Description;
		public String COD;
		public String ID;
		public String CODRelacionado;
	}

	public static class TpOrigenSol extends ApiObjeto {
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

	public static class CuentaBancaria extends ApiObjeto {
		public String NumeroCuenta;
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

	public static class UsuarioIngreso extends ApiObjeto {
		public String COD;
		public String ID;
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

	/* ========== SERVICIOS ========== */

	// API-Inversiones_SuscripcionSLInsert
	public static SuscripcionSL post(Contexto contexto, String idCuentaBancaria, String moneda, String numeroCuenta, String cuotapartista, String importe, String fondo, String condicionIngresoEgreso, String tipoValorCuotaParte) {
		ApiRequest request = new ApiRequest("SuscripcionSL", "inversiones", "POST", "/v1/suscripcionSL", contexto);
		request.body("SuscripcionSL.CuentaBancaria.IDCuentaBancaria", idCuentaBancaria);
		request.body("SuscripcionSL.CuentaBancaria.Moneda", moneda);
		request.body("SuscripcionSL.CuentaBancaria.NumeroCuenta", numeroCuenta);
		request.body("SuscripcionSL.Cuotapartista", cuotapartista);
		request.body("SuscripcionSL.FechaAcreditacion", Fecha.ahora().string("yyyy-MM-dd"));
		request.body("SuscripcionSL.FechaConcertacion", Fecha.ahora().string("yyyy-MM-dd"));
		request.body("SuscripcionSL.FormasPagoCuentaBancaria.CuentaBancaria", idCuentaBancaria);
		request.body("SuscripcionSL.FormasPagoCuentaBancaria.FormaPago", "28");
		request.body("SuscripcionSL.FormasPagoCuentaBancaria.Importe", importe);
		request.body("SuscripcionSL.IDSolicitud", UUID.randomUUID().toString());
		request.body("SuscripcionSL.InversionFondo.Fondo", fondo);
		request.body("SuscripcionSL.InversionFondo.CondicionIngresoEgreso", condicionIngresoEgreso);
		request.body("SuscripcionSL.InversionFondo.TipoValorCuotaParte", tipoValorCuotaParte);
		request.body("SuscripcionSL.Moneda", moneda);
		request.body("SuscripcionSL.TpOrigenSol", "IE");
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("No existe cuenta bancaria cuotapartista con ID 123", response.contains("No existe elemento CuotapartistaCuentaBancaria con el ID"), request, response);
		ApiException.throwIf(!response.http(200, 204), request, response);

		return response.crear(SuscripcionSL.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		SuscripcionSL datos = post(contexto, "22195", "1", "407100015287054", "16220", "2000", "9", "UNICO", "9C");
		imprimirResultado(contexto, datos);
	}

}