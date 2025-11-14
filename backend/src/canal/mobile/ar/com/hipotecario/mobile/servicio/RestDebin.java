package ar.com.hipotecario.mobile.servicio;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.excepcion.ApiExceptionMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.CuentaTercero;

public class RestDebin {

	public static ApiResponseMB listaDebinesEnviados(ContextoMB contexto, Date fechaDesde, Date fechaHasta) {
		Objeto body = new Objeto();
		body.set("listado").set("tamano", ConfigMB.string("debin_maximo_listado", "300")).set("pagina", "0");
		body.set("vendedor").set("cliente").set("idTributario", contexto.persona().cuit()).set("cuenta").set("banco", "044");
		body.set("comprador").set("cliente").set("cuenta").set("dummy");
		body.set("debin").set("creacion").set("fechaDesde", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss").format(fechaDesde) + "Z").set("fechaHasta", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss").format(fechaHasta) + "Z");
		body.objeto("debin").set("estado").set("dummy");

		ApiRequestMB request = ApiMB.request("ListaDebinesEnviados", "debin", "POST", "/v1/debin/listas", contexto);
		request.body(body);

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		return response;
	}

	public static ApiResponseMB listaDebinesRecibidos(ContextoMB contexto, Date fechaDesde, Date fechaHasta) {
		Objeto body = new Objeto();
		body.set("listado").set("tamano", ConfigMB.string("debin_maximo_listado", "300")).set("pagina", "0");
		body.set("vendedor").set("cliente").set("cuenta").set("dummy");
		body.set("comprador").set("cliente").set("idTributario", contexto.persona().cuit()).set("cuenta").set("banco", "044");
		body.set("debin").set("creacion").set("fechaDesde", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss").format(fechaDesde) + "Z").set("fechaHasta", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss").format(fechaHasta) + "Z");
		body.objeto("debin").set("estado").set("dummy");

		ApiRequestMB request = ApiMB.request("ListaDebinesRecibidos", "debin", "POST", "/v1/debin/listas", contexto);
		request.body(body);

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		return response;
	}

	// nunca se probó este servicio
	public static ApiResponseMB listaDebinesRecibidosRecurrentes(ContextoMB contexto, Date fechaDesde, Date fechaHasta) {
		Objeto body = new Objeto();
		body.set("listado").set("tamano", ConfigMB.string("debin_maximo_listado", "300")).set("pagina", "0");
		body.set("vendedor").set("cliente").set("cuenta").set("dummy");
		body.set("comprador").set("cliente").set("idTributario", contexto.persona().cuit()).set("cuenta").set("banco", "044");
		body.set("debin").set("creacion").set("fechaDesde", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss").format(fechaDesde) + "Z").set("fechaHasta", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss").format(fechaHasta) + "Z");
		body.objeto("debin").set("estado").set("dummy");

		ApiRequestMB request = ApiMB.request("ListaDebinesRecibidos", "debin", "POST", "/v1/debin/listas", contexto);
		request.body(body);

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		return response;
	}

	public static ApiResponseMB detalleDebin(ContextoMB contexto, String id) {
		ApiRequestMB request = ApiMB.request("DetalleDebin", "debin", "GET", "/v1/debin/{id}", contexto);
		request.path("id", id);
		request.cacheSesion = true;

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis(), id);
		return response;
	}

	public static void eliminarCacheDetalleDebin(ContextoMB contexto, String id) {
		ApiMB.eliminarCache(contexto, "DetalleDebin", contexto.idCobis(), id);
	}

	public static Boolean cuentaActivaVendedor(ContextoMB contexto, Cuenta cuenta) {
		String idTributario = contexto.persona().cuit();

		ApiRequestMB request = ApiMB.request("EstadoCuentaCoelsa", "debin", "GET", "/v1/vendedores/{idTributario}", contexto);
		request.path("idTributario", idTributario);

		request.cacheSesion = true;

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (response.hayError() && !response.string("codigo").equals("1302")) {
			throw new ApiExceptionMB(response);
		}

		String cbu = cuenta.cbu();
		for (Objeto item : response.objetos("cliente.cuentas")) {
			if (item.string("cbu").equals(cbu)) {
				return true;
			}
		}

		return false;
	}

	public static Boolean cuentaActivaComprador(ContextoMB contexto, String idTributario, String cbu) {
		ApiRequestMB request = ApiMB.request("EstadoCuentaCompradorCoelsa", "debin", "GET", "/v1/compradores/{idTributario}", contexto);
		request.path("idTributario", idTributario);

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (response.hayError() && !response.string("codigo").equals("05")) {
			throw new ApiExceptionMB(response);
		}

		for (Objeto item : response.objetos("cliente.cuentas")) {
			if (item.string("cbu").equals(cbu)) {
				return true;
			}
		}

		return false;
	}

	public static ApiResponseMB activarCuentaVendedor(ContextoMB contexto, Cuenta cuentaUsuario) {
		Objeto cuenta = new Objeto();
		cuenta.set("cbu", cuentaUsuario.cbu());
		cuenta.set("sucursal").set("id", cuentaUsuario.sucursal()).set("description", cuentaUsuario.sucursal());

		Objeto cliente = new Objeto();
		cliente.set("rubro", "VARIOS");
		cliente.set("idTributario", contexto.persona().cuit());
		cliente.set("contacto").set("dummy");
		cliente.set("cuenta", cuenta);

		ApiRequestMB request = ApiMB.request("ActivarCuentaDebin", "debin", "POST", "/v1/vendedores", contexto);
		request.body("cliente", cliente);
		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (!response.hayError()) {
//			enviarEmailAdhesion(contexto, contexto.persona().email());
		}
		return response;
	}

	public static ApiResponseMB activarCuentaComprador(ContextoMB contexto, String idTributario, String cbu) {
		Objeto cuenta = new Objeto();
		cuenta.set("cbu", cbu);
		cuenta.set("sucursal").set("id", "").set("description", "");

		Objeto cliente = new Objeto();
		cliente.set("rubro", "VARIOS");
		cliente.set("idTributario", idTributario);
		cliente.set("contacto").set("dummy");
		cliente.set("cuenta", cuenta);

		ApiRequestMB request = ApiMB.request("ActivarCuentaDebin", "debin", "POST", "/v1/compradores", contexto);
		request.body("cliente", cliente);
		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (!response.hayError()) {
//			enviarEmailAdhesion(contexto, contexto.persona().email());
		}
		return response;
	}

	public static ApiResponseMB desactivarCuenta(ContextoMB contexto, Cuenta cuentaUsuario) {
		String cbu = cuentaUsuario.cbu();
		ApiRequestMB request = ApiMB.request("DesactivarCuentaDebin", "debin", "DELETE", "/v1/vendedores", contexto);
		request.query("idTributario", contexto.persona().cuit());
		request.query("cbu", cbu);
		return ApiMB.response(request, contexto.idCobis(), cbu);
	}

	public static String tipoCuenta(ContextoMB contexto, String codigo) {
		String descripcion = "";
		descripcion = codigo.equals("10") ? "CA" : descripcion; // TODO: REVISAR
		return descripcion;
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
		String descripcionEstado = mapa.get(estado);
		descripcionEstado = descripcionEstado != null ? descripcionEstado : estado;
		return descripcionEstado;
	}

	public static void enviarEmailAdhesion(ContextoMB contexto, String email) {
		try {
			if (email != null && !email.isEmpty()) {
				ApiRequestMB requestMail = ApiMB.request("NotificacionesPostCorreoElectronico", "notificaciones", "POST", "/v1/correoelectronico", contexto);
				requestMail.body("de", "aviso@mail-hipotecario.com.ar");
				requestMail.body("para", email);
				requestMail.body("plantilla", ConfigMB.string("doppler_debin_adhesion"));
				Objeto parametros = requestMail.body("parametros");
				parametros.set("Subject", "Aviso Adhesion Cuenta BH");
				ApiMB.response(requestMail, new Date().getTime());
			}
		} catch (Exception e) {
		}
	}

	public static Boolean existePrestacion(ContextoMB contexto, String prestacion) {
		ApiRequestMB request = ApiMB.request("DebinConsultaPrestacion", "debin", "GET", "/v1/vendedores/prestacion/{cuit}", contexto);
		request.path("cuit", contexto.persona().cuit());

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis(), contexto.persona().cuit());
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

	public static ApiResponseMB crearPrestacion(ContextoMB contexto, String prestacion) {
		Objeto vendedor = new Objeto();
		vendedor.set("cuit", contexto.persona().cuit());
		Objeto prestaciones = vendedor.set("prestaciones");
		prestaciones.set("nombre", prestacion);
		prestaciones.set("ayuda_referencia", prestacion);
		prestaciones.set("min", 0);
		prestaciones.set("max", 0);

		ApiRequestMB request = ApiMB.request("DebinCrearPrestacion", "debin", "POST", "/v1/vendedores/prestacion", contexto);
		request.body("vendedor", vendedor);

		return ApiMB.response(request, contexto.idCobis(), contexto.persona().cuit());
	}

	public static String idRecurrencia(ContextoMB contexto, Cuenta cuenta, CuentaTercero cuentaTercero, String prestacion) {
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
		compradorCliente.set("idTributario", cuentaTercero.cuit());
		compradorCliente.set("cuenta", cuentaComprador);

		datos.set("fechaDesde", "2015-01-01T00:00:00.000");
		datos.set("fechaHasta", "2099-01-01T00:00:00.000");
		datos.set("estado").set("codigo", "");

		ApiRequestMB request = ApiMB.request("DebinRecurrencias", "debin", "POST", "/v1/vendedores/recurrencias/listas", contexto);
		request.body(datos);

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis(), contexto.persona().cuit());
		if (response.hayError() && !response.string("codigo").equals("5109") /* && !response.string("codigo").equals("5199") */) {
			if (ConfigMB.esDesarrollo() && response.string("codigo").equals("5199")) {
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

	public static ApiResponseMB crearRecurrencia(ContextoMB contexto, Cuenta cuenta, CuentaTercero cuentaTercero, String prestacion) {
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
		compradorCliente.set("idTributario", cuentaTercero.cuitCoelsa());
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

		ApiRequestMB request = ApiMB.request("DebinCrearRecurrencia", "debin", "POST", "/v1/vendedores/recurrencias", contexto);
		request.body(datos);

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis(), contexto.persona().cuit());
		return response;
	}

	public static ApiResponseMB eliminarRecurrencia(ContextoMB contexto, Cuenta cuenta, CuentaTercero cuentaTercero, String prestacion, String idRecurrencia) {
		Objeto datos = new Objeto();
		datos.set("activo", false);
		datos.set("idRecurrencia", idRecurrencia);

		Objeto vendedor = datos.set("vendedor");
		Objeto vendedorCliente = vendedor.set("cliente");
		vendedorCliente.set("idTributario", contexto.persona().cuit());
		Objeto vendedorClienteCuenta = vendedorCliente.set("cuenta");
		vendedorClienteCuenta.set("banco", "044");
		vendedorClienteCuenta.set("cbu", cuenta.cbu());

		Objeto comprador = datos.set("comprador");
		Objeto compradorCliente = comprador.set("cliente");
		compradorCliente.set("idTributario", cuentaTercero.cuitCoelsa());
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

		ApiRequestMB request = ApiMB.request("DebinEliminarRecurrencia", "debin", "DELETE", "/v1/vendedores/recurrencias", contexto);
		request.body(datos);

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis(), contexto.persona().cuit());
		return response;
	}

	public static ApiResponseMB consultaRecurrencias(ContextoMB contexto, Date fechaDesde, Date fechaHasta) {
		Objeto body = new Objeto();

		body.set("listado").set("tamano", ConfigMB.string("debin_maximo_listado", "20")).set("pagina", "1");

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

		ApiRequestMB request = ApiMB.request("ConsultaListaRecurrencias", "debin", "POST", "/v1/compradores/compradorRecurrenciaLista", contexto);
		request.body(body);

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

		return response;
	}

	public static ApiResponseMB confirmarRecurrecia(ContextoMB contexto, String id, Boolean aceptaRecurrencia, Objeto objRecurrencia) {
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

		ApiRequestMB request = ApiMB.request("ConfirmarRecurrencia", "debin", "POST", "/v1/compradores/confirmarrecurrencia", contexto);
		request.body(body);

		return ApiMB.response(request, contexto.idCobis());
	}

    public static ApiResponseMB bajaRecurrencia(ContextoMB contextoMB, String idRecurrencia, Objeto objRecurrencia) {
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

		ApiRequestMB request = ApiMB.request("DebinBajaRecurrencia", "debin", "DELETE", "/v1/compradores/recurrencias", contextoMB);
		request.body(datos);

		return ApiMB.response(request, contextoMB.idCobis(), contextoMB.persona().cuit());
    }

    public static ApiResponseMB consultaDebinesProgramados(ContextoMB contextoMB, Date fechaDesde, Date fechaHasta) {
		Objeto body = new Objeto();

		body.set("listado").set("tamano", ConfigMB.string("debin_maximo_listado", "20")).set("pagina", "1");

		Objeto vendedor = body.set("vendedor");
		Objeto vendedorCliente = vendedor.set("cliente");
		Objeto vendedorClienteCuenta = vendedorCliente.set("cuenta");
		vendedorClienteCuenta.set("");

		Objeto comprador = body.set("comprador");
		Objeto compradorCliente = comprador.set("cliente");
		compradorCliente.set("idTributario", contextoMB.persona().cuit());
		Objeto compradorClienteCuenta = compradorCliente.set("cuenta");
		compradorClienteCuenta.set("");

		body.set("estado").set("codigo", "");
		body.set("fechaDesde", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss").format(fechaDesde) + "Z")
				.set("fechaHasta", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss").format(fechaHasta) + "Z");

		ApiRequestMB request = ApiMB.request("ConsultaListaDebinesProgramados", "debin", "POST", "/v1/debinprogramado/compradorRecurrenciaLista", contextoMB);
		request.body(body);

		ApiResponseMB response = ApiMB.response(request, contextoMB.idCobis());

		return response;
    }

	public static ApiResponseMB confirmarDebinProgramado(ContextoMB contexto, String id, Boolean aceptaDebinProgramado, Objeto objDebinProgramado) {
		Objeto body = new Objeto();

		Objeto comprador = body.set("comprador");
		Objeto compradorCliente = comprador.set("cliente");
		compradorCliente.set("idTributario", objDebinProgramado.string("comprador.cuit"));
		Objeto compradorClienteCuenta = compradorCliente.set("cuenta");
		compradorClienteCuenta.set("cbu", objDebinProgramado.string("comprador.cbu"));

		Objeto vendedor = body.set("vendedor");
		Objeto vendedorCliente = vendedor.set("cliente");
		vendedorCliente.set("idTributario", objDebinProgramado.string("vendedor.cuit"));

		Objeto debin = body.set("debin");
		Objeto moneda = debin.set("moneda");
		moneda.set("id", objDebinProgramado.string("debin.moneda"));
		moneda.set("descripcion", objDebinProgramado.string("debin.moneda").equals("032")?"Pesos":"Dolar");
		moneda.set("signo", objDebinProgramado.string("debin.moneda").equals("032")?"$":"u$s");
		debin.set("prestacion", objDebinProgramado.string("debin.prestacion"));
		debin.set("referencia", objDebinProgramado.string("debin.referencia"));

		body.set("id", id);
		body.set("autorizacion", aceptaDebinProgramado);

		ApiRequestMB request = ApiMB.request("ConfirmarDebinProgramado", "debin", "POST", "/v1/compradores/confirmarrecurrencia", contexto);
		request.body(body);

		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB bajaDebinProgramado(ContextoMB contextoMB, String idDebinProgramado, Objeto objDebinProgramado) {
		Objeto datos = new Objeto();
		datos.set("activo", false);
		datos.set("idRecurrencia", idDebinProgramado);

		Objeto vendedor = datos.set("vendedor");
		Objeto vendedorCliente = vendedor.set("cliente");
		vendedorCliente.set("idTributario", objDebinProgramado.string("vendedor.cuit"));
		Objeto vendedorClienteCuenta = vendedorCliente.set("cuenta");
		vendedorClienteCuenta.set("banco", "");
		vendedorClienteCuenta.set("cbu", "");

		Objeto comprador = datos.set("comprador");
		Objeto compradorCliente = comprador.set("cliente");
		compradorCliente.set("idTributario", objDebinProgramado.string("comprador.cuit"));
		Objeto compradorClienteCuenta = compradorCliente.set("cuenta");
		compradorClienteCuenta.set("banco", "044");
		compradorClienteCuenta.set("cbu", objDebinProgramado.string("comprador.cbu"));

		Objeto debin = datos.set("debin");
		Objeto moneda = debin.set("moneda");
		moneda.set("id", objDebinProgramado.string("debin.moneda").equals("032")?"80":"2");
		moneda.set("descripcion", objDebinProgramado.string("debin.moneda").equals("032")?"Pesos":"Dolar");
		moneda.set("signo", objDebinProgramado.string("debin.moneda").equals("032")?"$":"u$s");
		debin.set("detalle", objDebinProgramado.string("debin.detalle"));
		debin.set("prestacion", objDebinProgramado.string("debin.prestacion"));
		debin.set("referencia", objDebinProgramado.string("debin.referencia"));
		debin.set("concepto", objDebinProgramado.string("debin.concepto"));

		datos.set("detalle", "");
		datos.set("concepto", objDebinProgramado.string("debin.concepto"));
		datos.set("periodo", "");
		datos.set("cantidad", "");
		datos.set("tipo_adhesion", 0);

		ApiRequestMB request = ApiMB.request("BajaDebinProgramado", "debin", "DELETE", "/v1/compradores/recurrencias", contextoMB);
		request.body(datos);

		return ApiMB.response(request, contextoMB.idCobis(), contextoMB.persona().cuit());
	}

	public static String descripcionRespuestaCodigoDebin(String codigoRespuestaDebin) {
		String descripcionCodigoDebin = "";
		Map<String, String> mapa = new HashMap<>();
		mapa.put("5100","RECURRENCIAS_ENCONTRADAS");
		mapa.put("5101","ESTADO_INCORRECTO");
		mapa.put("5102","VENDEDOR_CUIT_INCORRECTO");
		mapa.put("5103","COMPRADOR_CUIT_INCORRECTO");
		mapa.put("5104","COMPRADOR_CBU_INCORRECTO");
		mapa.put("5105","PAGINA_LISTADO_INCORRECTO");
		mapa.put("5106","TAMAÑO_LISTADO_INCORRECTO");
		mapa.put("5107","COMPRADOR_CBU_INCORRECTO");
		mapa.put("5108","COMPRADOR_CBU_INCORRECTO");
		mapa.put("5109","RECURRENCIAS_NO_ENCONTRADAS");
		mapa.put("5110","TOKEN_INVALIDO");
		mapa.put("5198","ERROR");
		mapa.put("5199","ERROR");
		// Confirmacion
		mapa.put("6300","¡Listo!\nAceptaste el DEBIN");
		mapa.put("6308","El DEBIN es inexistente"); // W
		mapa.put("6320","Ya aceptaste este DEBIN"); // W
		mapa.put("6321","¡Listo!\nRechazaste el DEBIN");
		mapa.put("6314","Solo el titular puede aceptar o rechazar la solicitud"); // W
		// Adhesión
		mapa.put("5023","RECURRENCIA_PROGRAMADA_NO_DISPONIBLE_PARA_COMPRADOR");
		mapa.put("5024","LIMITE_DE_CUOTAS_INCORRECTO");
		mapa.put("5025","IMPORTE_INCORRECTO");
		descripcionCodigoDebin = mapa.get(codigoRespuestaDebin);

		if(ConfigMB.string("debin_programado_codigos_error").contains(codigoRespuestaDebin) || descripcionCodigoDebin == null){
			descripcionCodigoDebin = "Acercate a una de nuestras sucursales o comunicate con nosotros";
		}

		return descripcionCodigoDebin;
	}

	public static String nombreVendedor(ContextoMB contexto, String idTributario) {

		String nombreVendedor = "";
		ApiRequestMB request = ApiMB.request("EstadoCuentaCoelsa", "debin", "GET", "/v1/vendedores/{idTributario}", contexto);
		request.path("idTributario", idTributario);
		request.cacheSesion = true;

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (response.hayError() && !response.string("codigo").equals("1302")) {
			return nombreVendedor;
		}

		return response.string("cliente.nombreCompleto");
	}
}
