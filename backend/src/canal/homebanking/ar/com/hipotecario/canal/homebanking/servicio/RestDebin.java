package ar.com.hipotecario.canal.homebanking.servicio;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.excepcion.ApiException;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.negocio.CuentaTercero;

public class RestDebin {

	public static ApiResponse listaDebinesEnviados(ContextoHB contexto, Date fechaDesde, Date fechaHasta) {
		Objeto body = new Objeto();
		body.set("listado").set("tamano", ConfigHB.string("debin_maximo_listado", "300")).set("pagina", "0");
		body.set("vendedor").set("cliente").set("idTributario", contexto.persona().cuit()).set("cuenta").set("banco", "044");
		body.set("comprador").set("cliente").set("cuenta").set("dummy");
		body.set("debin").set("creacion").set("fechaDesde", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss").format(fechaDesde) + "Z").set("fechaHasta", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss").format(fechaHasta) + "Z");
		body.objeto("debin").set("estado").set("dummy");

		ApiRequest request = Api.request("ListaDebinesEnviados", "debin", "POST", "/v1/debin/listas", contexto);
		request.body(body);

		ApiResponse response = Api.response(request, contexto.idCobis());
		return response;
	}

	public static ApiResponse listaDebinesRecibidos(ContextoHB contexto, Date fechaDesde, Date fechaHasta) {
		Objeto body = new Objeto();
		body.set("listado").set("tamano", ConfigHB.string("debin_maximo_listado", "300")).set("pagina", "0");
		body.set("vendedor").set("cliente").set("cuenta").set("dummy");
		body.set("comprador").set("cliente").set("idTributario", contexto.persona().cuit()).set("cuenta").set("banco", "044");
		body.set("debin").set("creacion").set("fechaDesde", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss").format(fechaDesde) + "Z").set("fechaHasta", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss").format(fechaHasta) + "Z");
		body.objeto("debin").set("estado").set("dummy");

		ApiRequest request = Api.request("ListaDebinesRecibidos", "debin", "POST", "/v1/debin/listas", contexto);
		request.body(body);

		ApiResponse response = Api.response(request, contexto.idCobis());
		return response;
	}

	public static ApiResponse detalleDebin(ContextoHB contexto, String id) {
		ApiRequest request = Api.request("DetalleDebin", "debin", "GET", "/v1/debin/{id}", contexto);
		request.path("id", id);
		request.cacheSesion = true;

		ApiResponse response = Api.response(request, contexto.idCobis(), id);
		return response;
	}

	public static void eliminarCacheDetalleDebin(ContextoHB contexto, String id) {
		Api.eliminarCache(contexto, "DetalleDebin", contexto.idCobis(), id);
	}

	public static Boolean cuentaActivaVendedor(ContextoHB contexto, Cuenta cuenta) {
		String idTributario = contexto.persona().cuit();

		ApiRequest request = Api.request("EstadoCuentaCoelsa", "debin", "GET", "/v1/vendedores/{idTributario}", contexto);
		request.path("idTributario", idTributario);
		request.cacheSesion = true;

		ApiResponse response = Api.response(request, contexto.idCobis());
		if (response.hayError() && !response.string("codigo").equals("1302")) {
			throw new ApiException(response);
		}

		String cbu = cuenta.cbu();
		for (Objeto item : response.objetos("cliente.cuentas")) {
			if (item.string("cbu").equals(cbu)) {
				return true;
			}
		}

		return false;
	}

	public static ApiResponse activarCuentaVendedor(ContextoHB contexto, Cuenta cuentaUsuario) {
		Objeto cuenta = new Objeto();
		cuenta.set("cbu", cuentaUsuario.cbu());
		cuenta.set("sucursal").set("id", cuentaUsuario.sucursal()).set("description", cuentaUsuario.sucursal());

		Objeto cliente = new Objeto();
		cliente.set("rubro", "VARIOS");
		cliente.set("idTributario", contexto.persona().cuit());
		cliente.set("contacto").set("dummy");
		cliente.set("cuenta", cuenta);

		ApiRequest request = Api.request("ActivarCuentaDebin", "debin", "POST", "/v1/vendedores", contexto);
		request.body("cliente", cliente);
		ApiResponse response = Api.response(request, contexto.idCobis());
		if (!response.hayError()) {
//			enviarEmailAdhesion(contexto, contexto.persona().email());
		}
		return response;
	}

	public static ApiResponse desactivarCuenta(ContextoHB contexto, Cuenta cuentaUsuario) {
		String cbu = cuentaUsuario.cbu();
		ApiRequest request = Api.request("DesactivarCuentaDebin", "debin", "DELETE", "/v1/vendedores", contexto);
		request.query("idTributario", contexto.persona().cuit());
		request.query("cbu", cbu);
		return Api.response(request, contexto.idCobis(), cbu);
	}

	public static String descripcionEstado(String estado) {
		Map<String, String> mapa = new HashMap<>();
		mapa.put("INICIADO", "Iniciado");
		mapa.put("ERROR DEBITO", "Error");
		mapa.put("SIN SALDO", "Sin Saldo");
		mapa.put("RECHAZO DE CLIENTE", "Rechazado");
		mapa.put("ELIMINADO", "Cancelado");
		mapa.put("VENCIDO", "Vencido");
		mapa.put("ERROR DATOS", "Error");
		mapa.put("ERROR DATOS OPERACION", "Error");
		mapa.put("ERROR DATOS VENDEDOR", "Error");
		mapa.put("ERROR DATOS COMPRADOR", "Error");
		mapa.put("ERROR ACREDITACION", "Error");
		mapa.put("SIN GARANTIA", "Sin Garantía");
		mapa.put("EN CURSO", "En Curso");
		mapa.put("ACREDITADO", "Acreditado");
		mapa.put("ACTIVA", "Activa");
		mapa.put("INACTIVA", "Inactiva");
		String descripcionEstado = mapa.get(estado);
		descripcionEstado = descripcionEstado != null ? descripcionEstado : estado;
		return descripcionEstado;
	}

	public static String descripcionRespuestaDebinRecurrente(String codigoRespuesta) {
		Map<String, String> mapa = new HashMap<>();
		mapa.put("INICIADO", "Iniciado");
		mapa.put("ERROR DEBITO", "Error");
		mapa.put("SIN SALDO", "Sin Saldo");
		mapa.put("RECHAZO DE CLIENTE", "Rechazado");
		mapa.put("ELIMINADO", "Cancelado");
		mapa.put("VENCIDO", "Vencido");
		mapa.put("ERROR DATOS", "Error");
		mapa.put("ERROR DATOS OPERACION", "Error");
		mapa.put("ERROR DATOS VENDEDOR", "Error");
		mapa.put("ERROR DATOS COMPRADOR", "Error");
		mapa.put("ERROR ACREDITACION", "Error");
		mapa.put("SIN GARANTIA", "Sin Garantía");
		mapa.put("EN CURSO", "En Curso");
		mapa.put("ACREDITADO", "Acreditado");
		mapa.put("ACTIVA", "Activa");
		mapa.put("INACTIVA", "Inactiva");
		String descripcionRespuestaDebin = mapa.get(codigoRespuesta);
		descripcionRespuestaDebin = descripcionRespuestaDebin != null ? descripcionRespuestaDebin : codigoRespuesta;
		return descripcionRespuestaDebin;
	}

	public static Boolean existePrestacion(ContextoHB contexto, String prestacion) {
		ApiRequest request = Api.request("DebinConsultaPrestacion", "debin", "GET", "/v1/vendedores/prestacion/{cuit}", contexto);
		request.path("cuit", contexto.persona().cuit());

		ApiResponse response = Api.response(request, contexto.idCobis(), contexto.persona().cuit());
		if (response.hayError() && !response.string("codigo").equals("2702")) {
			throw new RuntimeException();
		}

		for (Objeto item : response.objetos("prestaciones")) {
			if (item.string("nombre").equals(prestacion)) {
				return true;
			}
		}

		return false;
	}

	public static ApiResponse crearPrestacion(ContextoHB contexto, String prestacion) {
		Objeto vendedor = new Objeto();
		vendedor.set("cuit", contexto.persona().cuit());
		Objeto prestaciones = vendedor.set("prestaciones");
		prestaciones.set("nombre", prestacion);
		prestaciones.set("ayuda_referencia", prestacion);
		prestaciones.set("min", 0);
		prestaciones.set("max", 0);

		ApiRequest request = Api.request("DebinCrearPrestacion", "debin", "POST", "/v1/vendedores/prestacion", contexto);
		request.body("vendedor", vendedor);

		return Api.response(request, contexto.idCobis(), contexto.persona().cuit());
	}

	public static String idRecurrencia(ContextoHB contexto, Cuenta cuenta, CuentaTercero cuentaTercero, String prestacion) {
		Objeto datos = new Objeto();

		Objeto listado = datos.set("listado");
		listado.set("tamano", "50");
		listado.set("pagina", "1");

		Objeto vendedor = datos.set("vendedor");
		Objeto vendedorCliente = vendedor.set("cliente");
		vendedorCliente.set("idTributario", contexto.persona().cuit());
		Objeto vendedorClienteCuenta = vendedorCliente.set("cuenta");
		vendedorClienteCuenta.set("banco", "044");
		vendedorClienteCuenta.set("cbu", cuenta.cbu());

		Objeto cuentaComprador = new Objeto();
		cuentaComprador.set("banco", cuentaTercero.cbu().substring(0, 3));
		cuentaComprador.set("cbu", cuentaTercero.cbu());

		Objeto comprador = datos.set("comprador");
		Objeto compradorCliente = comprador.set("cliente");
		compradorCliente.set("idTributario", contexto.persona().cuit());
		compradorCliente.set("cuenta", cuentaComprador);

		datos.set("fechaDesde", "2015-01-01T00:00:00.000");
		datos.set("fechaHasta", "2099-01-01T00:00:00.000");
		datos.set("estado").set("codigo", "");

		ApiRequest request = Api.request("DebinRecurrencias", "debin", "POST", "/v1/vendedores/recurrencias/listas", contexto);
		request.body(datos);

		ApiResponse response = Api.response(request, contexto.idCobis(), contexto.persona().cuit());
		if (response.hayError() && !response.string("codigo").equals("5109") /* && !response.string("codigo").equals("5199") */) {
			if (ConfigHB.esDesarrollo() && response.string("codigo").equals("5199")) {
			} else {
				throw new RuntimeException();
			}
		}

		for (Objeto item : response.objetos("recurrencias")) {
			if (item.string("estado").equals("ACTIVA")) {
//				if (item.string("debin.detalle").equals(prestacion)) {
//					return item.string("idRecurrencia");
//				}
				if (item.string("debin.detalle").equals(prestacion) && item.string("comprador.cliente.cuenta.cbu").equals(cuentaTercero.cbu())) {
					return item.string("idRecurrencia");
				}
			}
		}

		return null;
	}

	public static ApiResponse crearRecurrencia(ContextoHB contexto, Cuenta cuenta, CuentaTercero cuentaTercero, String prestacion) {
		Objeto datos = new Objeto();
		datos.set("activo", true);
		datos.set("idRecurrencia", null);

		Objeto vendedor = datos.set("vendedor");
		Objeto vendedorCliente = vendedor.set("cliente");
		vendedorCliente.set("idTributario", contexto.persona().cuit());
		Objeto vendedorClienteCuenta = vendedorCliente.set("cuenta");
		vendedorClienteCuenta.set("banco", "044");
		vendedorClienteCuenta.set("cbu", cuenta.cbu());

		Objeto comprador = datos.set("comprador");
		Objeto compradorCliente = comprador.set("cliente");
		compradorCliente.set("idTributario", contexto.persona().cuit());
		Objeto compradorClienteCuenta = compradorCliente.set("cuenta");
		compradorClienteCuenta.set("banco", cuentaTercero.cbu().substring(0, 3));
		compradorClienteCuenta.set("cbu", cuentaTercero.cbu());

		Objeto debin = datos.set("debin");
		Objeto moneda = debin.set("moneda");
		moneda.set("id", "80");
		moneda.set("descripcion", "Pesos");
		moneda.set("signo", "$");
		debin.set("detalle", prestacion);
		debin.set("prestacion", prestacion);
		debin.set("referencia", prestacion);
		debin.set("concepto", "VAR");

		ApiRequest request = Api.request("DebinCrearRecurrencia", "debin", "POST", "/v1/vendedores/recurrencias", contexto);
		request.body(datos);

		ApiResponse response = Api.response(request, contexto.idCobis(), contexto.persona().cuit());
		return response;
	}

	public static ApiResponse consultaRecurrencias(ContextoHB contexto, Date fechaDesde, Date fechaHasta) {
		Objeto body = new Objeto();
		body.set("listado").set("tamano", ConfigHB.string("debin_maximo_listado", "20")).set("pagina", "1");

		Objeto vendedor = body.set("vendedor");
		Objeto vendedorCliente = vendedor.set("cliente");
		Objeto vendedorClienteCuenta = vendedorCliente.set("cuenta");
		vendedorClienteCuenta.set("");

		Objeto comprador = body.set("comprador");
		Objeto compradorCliente = comprador.set("cliente");
		compradorCliente.set("idTributario", contexto.persona().cuit());
		Objeto compradorClienteCuenta = compradorCliente.set("cuenta");
		compradorClienteCuenta.set("");

		body.set("estado").set("codigo", "");
		body.set("fechaDesde", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss").format(fechaDesde) + "Z")
				.set("fechaHasta", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss").format(fechaHasta) + "Z");

		ApiRequest request = Api.request("ConsultaListaRecurrencias", "debin", "POST", "/v1/compradores/compradorRecurrenciaLista", contexto);
		request.body(body);

		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse confirmarRecurrecia(ContextoHB contexto, String id, Boolean aceptaRecurrencia, Objeto objRecurrencia) {
		Objeto body = new Objeto();

		Objeto comprador = body.set("comprador");
		Objeto compradorCliente = comprador.set("cliente");
		compradorCliente.set("idTributario", objRecurrencia.string("comprador.cuit"));
		Objeto compradorClienteCuenta = compradorCliente.set("cuenta");
		compradorClienteCuenta.set("cbu", objRecurrencia.string("comprador.cbu"));

		Objeto vendedor = body.set("vendedor");
		Objeto vendedorCliente = vendedor.set("cliente");
		vendedorCliente.set("idTributario", objRecurrencia.string("vendedor.cuit"));

		Objeto debin = body.set("debin");
		Objeto moneda = debin.set("moneda");
		moneda.set("id", objRecurrencia.string("debin.moneda"));
		moneda.set("descripcion", objRecurrencia.string("debin.moneda").equals("032")?"Pesos":"Dolar");
		moneda.set("signo", objRecurrencia.string("debin.moneda").equals("032")?"$":"u$s");
		debin.set("prestacion", objRecurrencia.string("debin.prestacion"));
		debin.set("referencia", objRecurrencia.string("debin.referencia"));

		body.set("id", id);
		body.set("autorizacion", aceptaRecurrencia);

		ApiRequest request = Api.request("ConfirmarRecurrencia", "debin", "POST", "/v1/compradores/confirmarrecurrencia", contexto);
		request.body(body);

		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse bajaRecurrencia(ContextoHB contexto, String idRecurrencia, Objeto objRecurrencia) {
		Objeto datos = new Objeto();
		datos.set("activo", false);
		datos.set("idRecurrencia", idRecurrencia);

		Objeto vendedor = datos.set("vendedor");
		Objeto vendedorCliente = vendedor.set("cliente");
		vendedorCliente.set("idTributario", objRecurrencia.string("vendedor.cuit"));
		Objeto vendedorClienteCuenta = vendedorCliente.set("cuenta");
		vendedorClienteCuenta.set("banco", "");
		vendedorClienteCuenta.set("cbu", "");

		Objeto comprador = datos.set("comprador");
		Objeto compradorCliente = comprador.set("cliente");
		compradorCliente.set("idTributario", objRecurrencia.string("comprador.cuit"));
		Objeto compradorClienteCuenta = compradorCliente.set("cuenta");
		compradorClienteCuenta.set("banco", "044");
		compradorClienteCuenta.set("cbu", objRecurrencia.string("comprador.cbu"));

		Objeto debin = datos.set("debin");
		Objeto moneda = debin.set("moneda");
		moneda.set("id", objRecurrencia.string("debin.moneda").equals("032")?"80":"2");
		moneda.set("descripcion", "Pesos");
		moneda.set("signo", "$");
		debin.set("detalle", objRecurrencia.string("debin.detalle"));
		debin.set("prestacion", objRecurrencia.string("debin.prestacion"));
		debin.set("referencia", objRecurrencia.string("debin.referencia"));
		debin.set("concepto", objRecurrencia.string("debin.concepto"));

		datos.set("detalle", "");
		datos.set("concepto", objRecurrencia.string("debin.concepto"));
		datos.set("periodo", "");
		datos.set("cantidad", "");
		datos.set("tipo_adhesion", 0);

		ApiRequest request = Api.request("DebinBajaRecurrencia", "debin", "DELETE", "/v1/compradores/recurrencias", contexto);
		request.body(datos);

		return Api.response(request, contexto.idCobis(), contexto.persona().cuit());
	}
}
