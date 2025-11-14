package ar.com.hipotecario.canal.homebanking.servicio;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.api.HBAplicacion;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;

public class PaquetesService {

	/* ========== SERVICIOS ========== */
	public static ApiResponse consolidadaPaquetes(ContextoHB contexto, String numeroPaquete) {
		Boolean habilitarPaquetesApi = HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_paquetes_api");

		ApiRequest request = null;
		if (habilitarPaquetesApi) {
			request = Api.request("ConsolidadaPaquetes", "paquetes", "GET", "/v1/infoPaquetes/productos", contexto);
			request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));
		} else {
			request = Api.request("ConsolidadaPaquetes", "paquetes_windows", "GET", "/v1/infoPaquetes/productos", contexto);
		}
		// request.path("idcuenta", numeroCuenta);
		request.query("idPaquete", numeroPaquete);
		request.query("idCliente", contexto.idCobis());
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse bonificacionesVigentesPorPaquetes(ContextoHB contexto, String codigoPaquete, String numeroPaquete) {
		Boolean habilitarPaquetesApi = HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_paquetes_api");
		ApiRequest request = null;
		if (habilitarPaquetesApi) {
			request = Api.request("PaquetesGetBonificacionesVigentes", "paquetes", "GET", "/v1/bonificaciones/historico", contexto);
			request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));
		} else {
			request = Api.request("PaquetesWindowsGetBonificacionesVigentes", "paquetes_windows", "GET", "/v1/bonificaciones/historico", contexto);
		}
		// request.path("idcuenta", numeroCuenta);
		request.query("operacion", "Z");
		// request.query("numeroPaquete", "75043");
		request.query("numeroPaquete", numeroPaquete);
		request.query("fechaInicio", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse bonificacionesHistoricoUltimoCobro(ContextoHB contexto, String numeroPaquete) {
		return bonificacionesHistorico(contexto, numeroPaquete, new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
	}

	public static ApiResponse bonificacionesHistorico(ContextoHB contexto, String numeroPaquete, String fechaInicio) {
		Boolean habilitarPaquetesApi = HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_paquetes_api");
		ApiRequest request = null;
		if (habilitarPaquetesApi) {
			request = Api.request("PaquetesGetBonificacionesHistoricoUltimoCobro", "paquetes", "GET", "/v1/bonificaciones/historico", contexto);
			request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));
		} else {
			request = Api.request("PaquetesWindowsGetBonificacionesHistoricoUltimoCobro", "paquetes_windows", "GET", "/v1/bonificaciones/historico", contexto);
		}
		// request.path("idcuenta", numeroCuenta);
		request.query("operacion", "E");
		request.query("numeroPaquete", numeroPaquete);
		request.query("fechaInicio", fechaInicio);
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse bonificacionesHistorico(ContextoHB contexto, String numeroPaquete, String fechaDesde, String fechaHasta) {
		if (fechaDesde == null)
			fechaDesde = "";
		if (fechaHasta == null)
			fechaHasta = "";

		Boolean habilitarPaquetesApi = HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_paquetes_api");
		ApiRequest request = null;
		if (habilitarPaquetesApi) {
			request = Api.request("PaquetesGetBonificacionesHistorico", "paquetes", "GET", "/v1/bonificaciones/historico", contexto);
			request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));
		} else {
			request = Api.request("PaquetesWindowsGetBonificacionesHistorico", "paquetes_windows", "GET", "/v1/bonificaciones/historico", contexto);
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
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse bonificacionesHistoricoDetallado(ContextoHB contexto, String numeroPaquete, String fecha, String servicio, String rubro) {
		Boolean habilitarPaquetesApi = HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_paquetes_api");
		ApiRequest request = null;
		if (habilitarPaquetesApi) {
			request = Api.request("PaquetesGetBonificacionesHistoricoDetallado", "paquetes", "GET", "/v1/bonificaciones/historico", contexto);
			request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));
		} else {
			request = Api.request("PaquetesWindowsGetBonificacionesHistoricoDetallado", "paquetes_windows", "GET", "/v1/bonificaciones/historico", contexto);
		}
		request.query("operacion", "Y");
		request.query("numeroPaquete", numeroPaquete);
		request.query("fechaInicio", fecha);
		request.query("servicio", servicio);
		request.query("rubro", rubro);
		return Api.response(request, contexto.idCobis());
	}

}
