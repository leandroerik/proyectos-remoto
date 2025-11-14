package ar.com.hipotecario.mobile.servicio;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.api.MBAplicacion;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;

public class PaquetesService {

	/* ========== SERVICIOS ========== */
	public static ApiResponseMB consolidadaPaquetes(ContextoMB contexto, String numeroPaquete) {
		Boolean habilitarPaquetesApi = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_paquetes_api");

		ApiRequestMB request = null;
		if (habilitarPaquetesApi) {
			request = ApiMB.request("ConsolidadaPaquetes", "paquetes", "GET", "/v1/infoPaquetes/productos", contexto);
			request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
		} else {
			request = ApiMB.request("ConsolidadaPaquetes", "paquetes_windows", "GET", "/v1/infoPaquetes/productos", contexto);
		}
		// request.path("idcuenta", numeroCuenta);
		request.query("idPaquete", numeroPaquete);
		request.query("idCliente", contexto.idCobis());
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB bonificacionesBienvenidaPaquetes(ContextoMB contexto, String codigoPaquete, String numeroPaquete) {
		Boolean habilitarPaquetesApi = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_paquetes_api");
		ApiRequestMB request = null;
		if (habilitarPaquetesApi) {
			request = ApiMB.request("BonificacionesBienvenida", "paquetes", "GET", "/v1/bonificaciones/bienvenida", contexto);
			request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
		} else {
			request = ApiMB.request("WindowsBonificacionesBienvenida", "paquetes_windows", "GET", "/v1/bonificaciones/bienvenida", contexto);
		}
		// request.path("idcuenta", numeroCuenta);
		request.query("idPaquete", codigoPaquete);
		request.query("numeroPaquete", numeroPaquete);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB bonificacionesVigentesPorPaquetes(ContextoMB contexto, String codigoPaquete, String numeroPaquete) {
		Boolean habilitarPaquetesApi = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_paquetes_api");
		ApiRequestMB request = null;
		if (habilitarPaquetesApi) {
			request = ApiMB.request("PaquetesGetBonificacionesVigentes", "paquetes", "GET", "/v1/bonificaciones/historico", contexto);
			request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
		} else {
			request = ApiMB.request("PaquetesWindowsGetBonificacionesVigentes", "paquetes_windows", "GET", "/v1/bonificaciones/historico", contexto);
		}
		// request.path("idcuenta", numeroCuenta);
		request.query("operacion", "Z");
		// request.query("numeroPaquete", "75043");
		request.query("numeroPaquete", numeroPaquete);
		request.query("fechaInicio", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB bonificacionesHistoricoUltimoCobro(ContextoMB contexto, String numeroPaquete) {
		return bonificacionesHistorico(contexto, numeroPaquete, new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
	}

	public static ApiResponseMB bonificacionesHistorico(ContextoMB contexto, String numeroPaquete, String fechaInicio) {
		Boolean habilitarPaquetesApi = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_paquetes_api");
		ApiRequestMB request = null;
		if (habilitarPaquetesApi) {
			request = ApiMB.request("PaquetesGetBonificacionesHistoricoUltimoCobro", "paquetes", "GET", "/v1/bonificaciones/historico", contexto);
			request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
		} else {
			request = ApiMB.request("PaquetesWindowsGetBonificacionesHistoricoUltimoCobro", "paquetes_windows", "GET", "/v1/bonificaciones/historico", contexto);
		}
		// request.path("idcuenta", numeroCuenta);
		request.query("operacion", "E");
		request.query("numeroPaquete", numeroPaquete);
		request.query("fechaInicio", fechaInicio);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB bonificacionesHistorico(ContextoMB contexto, String numeroPaquete, String fechaDesde, String fechaHasta) {
		if (fechaDesde == null)
			fechaDesde = "";
		if (fechaHasta == null)
			fechaHasta = "";

		Boolean habilitarPaquetesApi = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_paquetes_api");
		ApiRequestMB request = null;
		if (habilitarPaquetesApi) {
			request = ApiMB.request("PaquetesGetBonificacionesHistorico", "paquetes", "GET", "/v1/bonificaciones/historico", contexto);
			request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
		} else {
			request = ApiMB.request("PaquetesWindowsGetBonificacionesHistorico", "paquetes_windows", "GET", "/v1/bonificaciones/historico", contexto);
		}
		Calendar cal = Calendar.getInstance();
		if ("".equals(fechaDesde)) {
			cal.setTime(new Date());
			cal.add(Calendar.MONTH, -20);
			fechaDesde = new SimpleDateFormat("dd/MM/yyyy").format(cal.getTime());
		}
		if ("".equals(fechaHasta)) {
			fechaHasta = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
		}

		request.query("operacion", "X");
		request.query("numeroPaquete", numeroPaquete);
		request.query("fechaInicio", fechaDesde);
		request.query("fechaFin", fechaHasta);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB bonificacionesHistoricoDetallado(ContextoMB contexto, String numeroPaquete, String fecha, String servicio, String rubro) {
		Boolean habilitarPaquetesApi = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_paquetes_api");
		ApiRequestMB request = null;
		if (habilitarPaquetesApi) {
			request = ApiMB.request("PaquetesGetBonificacionesHistoricoDetallado", "paquetes", "GET", "/v1/bonificaciones/historico", contexto);
			request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
		} else {
			request = ApiMB.request("PaquetesWindowsGetBonificacionesHistoricoDetallado", "paquetes_windows", "GET", "/v1/bonificaciones/historico", contexto);
		}
		request.query("operacion", "Y");
		request.query("numeroPaquete", numeroPaquete);
		request.query("fechaInicio", fecha);
		request.query("servicio", servicio);
		request.query("rubro", rubro);
		return ApiMB.response(request, contexto.idCobis());
	}

}
