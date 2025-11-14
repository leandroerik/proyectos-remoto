package ar.com.hipotecario.backend.servicio.api.ventas;

import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.ventas.RolIntegrantes.RolIntegrante;
import ar.com.hipotecario.backend.servicio.api.ventas.SolicitudPaquete.CuentaLegal;
import ar.com.hipotecario.backend.servicio.api.ventas.SolicitudPaquete.DomicilioResumen;

public class SolicitudTarjetaDebito extends ApiObjeto {

	public static String GET_TARJETA_DEBITO = "TarjetaDebito";
	public static String POST_TARJETA_DEBITO = "CrearTarjetaDebito";
	public static String PUT_TARJETA_DEBITO = "ActualizarTarjetaDebito";

	/* ========== ATRIBUTOS ========== */
	public String Id;
	public String tipoProducto;
	public String IdProductoFrontEnd;
	public String TipoOperacion;
	public String Oficina;
	public String Tipo;
	public DomicilioResumen Domicilio;
	public String Grupo;
	public String TipoCuentaComision;
	public String TipoCuentaComisionParam;
	public List<CuentasOperativas> TarjetaDebitoCuentasOperativas;
	public List<RolIntegrante> Integrantes;
	public List<TarjetaDebitoAdicional> TarjetaDebitoAdicionales;
	public Object NumeroCtaComision;
	public Object Advertencias;
	public String oficinaRetiro;
	public String cuentaTarjeta;
	public String categoriaCuentaComision;
	public String comentario;
	public String usoFirmaCuentaComision;
	public Boolean RechazadoMotor;
	public String IdPaqueteProductos;
	public CuentaLegal CuentaLegales;
	public Boolean EsVirtual;
	public String VisualizaVirtual;
	public String RequiereEmbozado;

	public static class CuentasOperativas extends ApiObjeto {
		public String Id;
		public String Producto;
		public String ProductoParam;
		public String Cuenta;
		public String Moneda;
		public Boolean Principal;
		public String Firma;
		public String Categoria;
		public String IdProducto;
		public String NumeroCuenta;
	}

	public static class TarjetaDebitoAdicional {
		public String Id;
		public String IdIntegrante;
		public RolIntegrante Integrante;
		public String IdCobis;
		public Integer IntegranteSecuencia;
		public String IntegranteIdNombreCompleto;
		public String Limite;
		public Boolean Renueva;
		public String TipoCosto;
		public String TipoCostoDescripcion;
		public String TipoCostoParam;
		public String NumeroPlastico;
		public Boolean Principal;
	}

	public static class NuevaSolicitudTarjetaDebito {
		public String TipoOperacion;
		public String Tipo;
		public DomicilioResumen Domicilio;
		public String Grupo;
		public String TipoCuentaComision;
		public List<CuentasOperativas> TarjetaDebitoCuentasOperativas;
		public Object NumeroCtaComision;
		public CuentaLegal CuentaLegales;
		public List<RolIntegrante> Integrantes;
		public String categoriaCuentaComision;
		public Boolean EsVirtual;
		public String VisualizaVirtual;
		public String RequiereEmbozado;
	}

	/* ========== SERVICIOS ========== */
	public static SolicitudTarjetaDebito get(Contexto contexto, String numeroSolicitud, String idProducto) {
		ApiRequest request = new ApiRequest(GET_TARJETA_DEBITO, ApiVentas.API, "GET", "/solicitudes/{numeroSolicitud}/tarjetaDebito/{idProducto}", contexto);
		request.header(ApiVentas.X_HANDLE, numeroSolicitud);
		request.path("numeroSolicitud", numeroSolicitud);
		request.path("idProducto", idProducto);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorVentas(response), request, response);
		return response.crear(SolicitudTarjetaDebito.class, response.objetos(ApiVentas.DATOS).get(0));
	}

	// tarjetaDebitoPOST
	public static SolicitudTarjetaDebito post(Contexto contexto, String idSolicitud, NuevaSolicitudTarjetaDebito tarjetaDebito) {
		ApiRequest request = new ApiRequest(POST_TARJETA_DEBITO, ApiVentas.API, "POST", "/solicitudes/{idSolicitud}/tarjetaDebito", contexto);
		request.header(ApiVentas.X_HANDLE, idSolicitud);
		request.path("idSolicitud", idSolicitud);

		request.body("TipoOperacion", tarjetaDebito.TipoOperacion);
		request.body("Tipo", tarjetaDebito.Tipo);
		request.body("Domicilio", tarjetaDebito.Domicilio);
		request.body("Grupo", tarjetaDebito.Grupo);
		request.body("TipoCuentaComision", tarjetaDebito.TipoCuentaComision);
		request.body("NumeroCtaComision", tarjetaDebito.NumeroCtaComision);
		request.body("Tipo", tarjetaDebito.Tipo);
		request.body("TarjetaDebitoCuentasOperativas", tarjetaDebito.TarjetaDebitoCuentasOperativas);
		request.body("CuentaLegales", tarjetaDebito.CuentaLegales);
		request.body("Integrantes", tarjetaDebito.Integrantes);
		request.body("EsVirtual", tarjetaDebito.EsVirtual);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorVentas(response), request, response);
		return response.crear(SolicitudTarjetaDebito.class, response.objetos(ApiVentas.DATOS).get(0));
	}

	// tarjetaDebitoStandPOST
	public static SolicitudTarjetaDebito postStand(Contexto contexto, String idSolicitud, NuevaSolicitudTarjetaDebito tarjetaDebito) {
		ApiRequest request = new ApiRequest(POST_TARJETA_DEBITO, ApiVentas.API, "POST", "/solicitudes/{idSolicitud}/tarjetaDebito", contexto);
		request.header(ApiVentas.X_HANDLE, idSolicitud);
		request.path("idSolicitud", idSolicitud);
		request.body("TipoOperacion", tarjetaDebito.TipoOperacion);
		request.body("Integrantes", tarjetaDebito.Integrantes);
		request.body("EsVirtual", tarjetaDebito.EsVirtual);
		request.body("VisualizaVirtual", tarjetaDebito.VisualizaVirtual);
		request.body("RequiereEmbozado", tarjetaDebito.RequiereEmbozado);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorVentas(response), request, response);
		return response.crear(SolicitudTarjetaDebito.class, response.objetos(ApiVentas.DATOS).get(0));
	}

	// PUT /solicitudes/{idSolicitud}/tarjetaDebito/{idProducto}
	public static SolicitudTarjetaDebito put(Contexto contexto, String idSolicitud, String idProducto, SolicitudTarjetaDebito tarjetaDebito) {
		ApiRequest request = new ApiRequest(PUT_TARJETA_DEBITO, ApiVentas.API, "PUT", "/solicitudes/{idSolicitud}/tarjetaDebito/{idProducto}", contexto);
		request.header(ApiVentas.X_HANDLE, idSolicitud);
		request.path("idSolicitud", idSolicitud);
		request.path("idProducto", idProducto);

		request.body("TipoOperacion", tarjetaDebito.TipoOperacion);
		request.body("TarjetaDebitoCuentasOperativas", tarjetaDebito.TarjetaDebitoCuentasOperativas);
		request.body("Tipo", tarjetaDebito.Tipo);
		request.body("Grupo", tarjetaDebito.Grupo);
		request.body("TipoCuentaComision", tarjetaDebito.TipoCuentaComision);
		request.body("NumeroCtaComision", tarjetaDebito.NumeroCtaComision);
		request.body("Domicilio", tarjetaDebito.Domicilio);
		request.body("Integrantes", tarjetaDebito.Integrantes);
		request.body("EsVirtual", tarjetaDebito.EsVirtual);
		request.body("VisualizaVirtual", tarjetaDebito.VisualizaVirtual);
		request.body("RequiereEmbozado", tarjetaDebito.RequiereEmbozado);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorVentas(response), request, response);
		return response.crear(SolicitudTarjetaDebito.class, response.objetos(ApiVentas.DATOS).get(0));
	}

	public static SolicitudTarjetaDebito postStandV2(Contexto contexto, String idSolicitud, SolicitudTarjetaDebito tarjetaDebito) {
		ApiRequest request = new ApiRequest(POST_TARJETA_DEBITO, ApiVentas.API, "POST", "/solicitudes/{idSolicitud}/tarjetaDebito", contexto);
		request.header(ApiVentas.X_HANDLE, idSolicitud);
		request.path("idSolicitud", idSolicitud);

		request.body("TipoOperacion", tarjetaDebito.TipoOperacion);
		request.body("TarjetaDebitoCuentasOperativas", tarjetaDebito.TarjetaDebitoCuentasOperativas);
		request.body("Tipo", tarjetaDebito.Tipo);
		request.body("Grupo", tarjetaDebito.Grupo);
		request.body("TipoCuentaComision", tarjetaDebito.TipoCuentaComision);
		request.body("NumeroCtaComision", tarjetaDebito.NumeroCtaComision);
		request.body("Domicilio", tarjetaDebito.Domicilio);
		request.body("Integrantes", tarjetaDebito.Integrantes);
		request.body("EsVirtual", tarjetaDebito.EsVirtual);
		request.body("VisualizaVirtual", tarjetaDebito.VisualizaVirtual);
		request.body("RequiereEmbozado", tarjetaDebito.RequiereEmbozado);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorVentas(response), request, response);
		return response.crear(SolicitudTarjetaDebito.class, response.objetos(ApiVentas.DATOS).get(0));
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		String test = "get";
		Contexto contexto = contexto("BB", "homologacion");

		if ("get".equals(test)) {
			SolicitudTarjetaDebito datos = get(contexto, "30440372", "12664252");
			imprimirResultadoApiVentas(contexto, datos);
		} else if ("post".equals(test)) {
			String idSolicitud = "30440017";

			DomicilioResumen domicilio = new DomicilioResumen();
			domicilio.Tipo = "DP";

			RolIntegrante rolIntegrante = new RolIntegrante();
			rolIntegrante.Rol = "T";
			rolIntegrante.NumeroDocumentoTributario = "23349761114";

			List<RolIntegrante> integrantes = new ArrayList<RolIntegrante>();
			integrantes.add(rolIntegrante);

			CuentasOperativas tdcoperativas = new CuentasOperativas();
			tdcoperativas.Producto = "4";
			tdcoperativas.Cuenta = "0";
			tdcoperativas.Moneda = "80";
			tdcoperativas.Principal = true;
			tdcoperativas.Firma = "U";

			List<CuentasOperativas> cuenta = new ArrayList<CuentasOperativas>();
			cuenta.add(tdcoperativas);

			NuevaSolicitudTarjetaDebito std = new NuevaSolicitudTarjetaDebito();
			std.TipoOperacion = "03";
			std.Tipo = "NC";
			std.Domicilio = domicilio;
			std.Grupo = "3";
			std.TipoCuentaComision = "4";
			std.NumeroCtaComision = 0;
			std.TarjetaDebitoCuentasOperativas = cuenta;
			std.Integrantes = integrantes;
			std.EsVirtual = true;

			SolicitudTarjetaDebito datos = post(contexto, idSolicitud, std);
			imprimirResultadoApiVentas(contexto, datos);
		} else if ("put".equals(test)) {
			String idSolicitud = "30440372";
			String idProducto = "12664252";

			DomicilioResumen domicilio = new DomicilioResumen();
			domicilio.Tipo = "DP";

			RolIntegrante rolIntegrante = new RolIntegrante();
			rolIntegrante.Rol = "T";
			rolIntegrante.NumeroDocumentoTributario = "23349761114";

			List<RolIntegrante> integrantes = new ArrayList<RolIntegrante>();
			integrantes.add(rolIntegrante);

			CuentasOperativas tdcoperativas = new CuentasOperativas();
			tdcoperativas.Producto = "4";
			tdcoperativas.Cuenta = "0";
			tdcoperativas.Moneda = "80";
			tdcoperativas.Principal = true;
			tdcoperativas.Firma = "U";

			List<CuentasOperativas> cuenta = new ArrayList<CuentasOperativas>();
			cuenta.add(tdcoperativas);

			SolicitudTarjetaDebito std = new SolicitudTarjetaDebito();
			std.TipoOperacion = "03";
			std.Tipo = "NC";
			std.Domicilio = domicilio;
			std.Grupo = "3";
			std.TipoCuentaComision = "4";
			std.NumeroCtaComision = 0;
			std.TarjetaDebitoCuentasOperativas = cuenta;
			std.Integrantes = integrantes;
			std.EsVirtual = true;

			SolicitudTarjetaDebito datos = put(contexto, idSolicitud, idProducto, std);
			imprimirResultadoApiVentas(contexto, datos);

		}
	}
}
