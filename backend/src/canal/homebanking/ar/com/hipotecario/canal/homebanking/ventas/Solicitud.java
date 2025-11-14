package ar.com.hipotecario.canal.homebanking.ventas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.google.gson.Gson;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.api.HBCatalogo;
import ar.com.hipotecario.canal.homebanking.api.HBOmnicanalidad;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.lib.Fecha;
import ar.com.hipotecario.canal.homebanking.lib.Texto;
import ar.com.hipotecario.canal.homebanking.negocio.SituacionLaboral;
import ar.com.hipotecario.canal.homebanking.servicio.RestOmnicanalidad;
import ar.com.hipotecario.canal.homebanking.servicio.RestVenta;
import ar.com.hipotecario.canal.homebanking.ventas.SolicitudPrestamo.FormasDesembolso;

public class Solicitud {

	/* ========== ATRIBUTOS ========== */
	public String Id;
	public String IdSolicitud;
//	public String TipoOperacion;
//	public String Oficina;
//	public String CanalOriginacion1;
//	public String CanalOriginacion2;
//	public String CanalOriginacion3;
//	public CanalOriginacion CanalOriginacionNivel3;
//	public String CanalVenta1;
//	public String CanalVenta2;
//	public String CanalVenta3;
//	public String CanalVenta4;
//	public CanalVenta CanalVentaNivel4;
//	public Object Observaciones;
	public String Estado;
//	public String FechaAlta;
//	public Object Advertencias;
	public List<SolicitudProducto> Productos;
//	public Object FlagSimulacion;
//	public Object Simulacion;
//	public Object Formularios;
//	public Boolean PermiteImpresionDDJJAnses;
//	public String CanalVenta;
//	public String OficialVenta;
	public List<Integrante> Integrantes;
//	public Boolean Finalizada;
//	public Object OrigenDocumentacion;
	public String ResolucionCodigo;
	public String DerivarA;

//	public static class CanalOriginacion {
//		public String Id;
//		public String Nombre;
//		public String Apellido;
//		public String Nivel2;
//		public String IdDocbis;
//		public Object Advertencias;
//	}

//	public static class CanalVenta {
//		public String Id;
//		public String Nombre;
//		public String Apellido;
//		public String Empresa;
//		public String Funcionario;
//		public String Estado;
//		public Object CBU;
//		public String Nivel3;
//		public Object Advertencias;
//	}

	public static class SolicitudProducto {
		public String Id;
		public String tipoProducto;
		public String IdProductoFrontEnd;
//		public String TipoOperacion;
//		public String Oficina;
//		public String Producto;
//		public Boolean Validado;
//		public String Oficial;
//		public Object Moneda;
//		public List<Object> Integrantes;
//		public Object Advertencias;
//		public Boolean RechazadoMotor;
//		public String IdPaqueteProductos;
//		public Object MontoAprobado;
//		public Object ModoAprobacionId;
//		public Object ConSeguroVida;
//		public Object IdProductoPadre;
//		public Boolean EsMicrocredito;
		public String Nemonico;
		public Integer NroDesembolsosPropuestos;
		public Integer NroUltimoDesembolsoLiquidado;
		public List<DesembolsoPropuesto> DesembolsosPropuestos;
	}

	public static class DesembolsoPropuesto {
		public String Id;
		public Integer NroDesembolso;
		public String IdPrestamo;
		public BigDecimal Monto;
	}

	public static final String cuitAnses = "33637617449";

	/* ========== OBTENER SOLICITUDES EXISTENTES ========== */
	public static Solicitud solicitud(ContextoHB contexto, String idSolicitud) {
		ApiRequest request = Api.request("VentasConsultarSolicitud", "ventas_windows", "GET", "/solicitudes/{idSolicitud}", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("idSolicitud", idSolicitud);

		ApiResponse response = Api.response(request, idSolicitud);
		if (response.hayError() || !response.objetos("Errores").isEmpty()) {
			throw new ApiVentaException(response);
		}

		Objeto datos = response.objetos("Datos").get(0);
		Solicitud solicitud = (new Gson()).fromJson(datos.toJson(), Solicitud.class);
		return solicitud;
	}

	public static List<Solicitud> solicitudes(ContextoHB contexto, String cuit, String estado) {
		ApiRequest request = Api.request("VentasConsultarSolicitudes", "ventas_windows", "GET", "/solicitudes", contexto);
		request.query("cuil", cuit);

		if (!"segundodesembolso".equals(estado)) {
			request.query("fechadesde", Fecha.restarDias(new Date(), 90L, "yyyyMMdd"));
		}
		if (estado != null) {
			request.query("estado", estado);
		}

		ApiResponse response = Api.response(request, cuit);
		if (response.hayError() || !response.objetos("Errores").isEmpty()) {
			throw new ApiVentaException(response);
		}

		List<Solicitud> solicitudes = new ArrayList<>();
		for (Objeto datos : response.objetos("Datos")) {
			Solicitud solicitud = (new Gson()).fromJson(datos.toJson(), Solicitud.class);
			solicitudes.add(solicitud);
		}
		return solicitudes;
	}

	public static Solicitud solicitudProcrearRefaccion(ContextoHB contexto, String cuit) {
		List<Solicitud> solicitudes = solicitudes(contexto, cuit, null);
		for (Solicitud solicitud : solicitudes) {
			if (solicitud.contienePrestamo()) {
				SolicitudPrestamo prestamo = solicitud.prestamo(contexto);
				if (prestamo.esProcrearRefaccion()) {
					return solicitud;
				}
			}
		}
		return null;
	}

	public static Solicitud solicitudSegundoDesembolsoProcrearRefaccion(ContextoHB contexto, String cuit) {
		List<Solicitud> solicitudes = solicitudes(contexto, cuit, "segundodesembolso");
		for (Solicitud solicitud : solicitudes) {
			if (solicitud.contienePrestamo()) {
				SolicitudPrestamo prestamo = solicitud.prestamo(contexto);
				if (prestamo.esProcrearRefaccion()) {
					return solicitud;
				}
			}
		}
		return null;
	}

	public static Solicitud solicitudSegundoDesembolsoPrestamoHipotecario(ContextoHB contexto, String cuit, String numeroDesembolso) {
		// List<Solicitud> solicitudes = solicitudes(contexto, cuit,
		// "segundodesembolso", numero);

		ApiRequest request = Api.request("VentasSolicitudesSegundoDesembolsoHipotecario", "ventas_windows", "GET", "/solicitudes", contexto);
		request.query("cuil", cuit);
		request.query("estado", "desembolso");
		if (!numeroDesembolso.equals("")) {
			request.query("nro", numeroDesembolso);
		}

		ApiResponse response = Api.response(request, cuit);
		if (response.hayError() || !response.objetos("Errores").isEmpty()) {
			throw new ApiVentaException(response);
		}

		List<Solicitud> solicitudes = new ArrayList<>();
		for (Objeto datos : response.objetos("Datos")) {
			Solicitud solicitud = (new Gson()).fromJson(datos.toJson(), Solicitud.class);
			solicitudes.add(solicitud);
		}
		/*
		 * for (Solicitud solicitud : solicitudes) { if
		 * (solicitud.contienePrestamoHipotecario()) { SolicitudPrestamo prestamo =
		 * solicitud.prestamo(contexto); if (prestamo.esProcrearRefaccion()) { return
		 * solicitud; } } }
		 */

		if (solicitudes != null && solicitudes.size() > 0) {
			return solicitudes.get(0);
		}
		return null;
	}

	/* ========== ID PRODUCTOS ========== */
	public String idProducto(String idProducto) {
		if (this.Productos != null) {
			for (SolicitudProducto producto : this.Productos) {
				if (idProducto.equals(producto.IdProductoFrontEnd)) {
					return producto.Id;
				}
			}
		}
		return null;
	}

	public String idPrestamo() {
		return idProducto("2");
	}

	/* ========== PRODUCTOS CONTENIDOS EN LA SOLICITUD ========== */
	public Boolean contieneCajaAhorroPesos() {
		return idProducto("8") != null;
	}

	public Boolean contienePrestamo() {
		return idProducto("2") != null;
	}

	/* ========== DETALLES DE PRODUCTOS ========== */
	public SolicitudPrestamo prestamo(ContextoHB contexto) {
		if (contienePrestamo()) {
			ApiRequest request = Api.request("VentasConsultarPrestamo", "ventas_windows", "GET", "/solicitudes/{idSolicitud}/prestamoPersonal/{idProducto}", contexto);
			request.headers.put("X-Handle", this.IdSolicitud);
			request.path("idSolicitud", this.IdSolicitud);
			request.path("idProducto", idPrestamo());

			ApiResponse response = Api.response(request, contexto.idCobis());
			if (response.hayError() || !response.objetos("Errores").isEmpty()) {
				throw new ApiVentaException(response);
			}

			Objeto datos = response.objetos("Datos").get(0);
			SolicitudPrestamo prestamo = (new Gson()).fromJson(datos.toJson(), SolicitudPrestamo.class);
			return prestamo;
		}
		return null;
	}

	/* ========== GENERAR SOLICITUD ========== */
	public static Solicitud generarSolicitud(ContextoHB contexto) {
		ApiRequest request = Api.request("VentasGenerarSolicitud", "ventas_windows", "POST", "/solicitudes", contexto);
		request.headers.put("X-Handle", "0");
		request.body("TipoOperacion", "03");
		request.body("CanalOriginacion1", ConfigHB.integer("api_venta_canalOriginacion1"));
		request.body("CanalOriginacion2", ConfigHB.integer("api_venta_canalOriginacion2"));
		request.body("CanalOriginacion3", ConfigHB.string("api_venta_canalOriginacion3"));
		request.body("CanalVenta1", ConfigHB.string("api_venta_canalVenta1"));
		request.body("CanalVenta2", ConfigHB.string("api_venta_canalVenta2"));
		request.body("CanalVenta3", ConfigHB.string("api_venta_canalVenta3"));
		request.body("CanalVenta4", ConfigHB.string("api_venta_canalVenta4"));
		request.body("Oficina", "0");

		ApiResponse response = Api.response(request, contexto.idCobis());
		if (response.hayError() || !response.objetos("Errores").isEmpty()) {
			logApiVentas(contexto, "-", "generarSolicitud", response);
			throw new ApiVentaException(response);
		}
		logApiVentas(contexto, response.objetos("Datos").get(0).string("IdSolicitud"), "generarSolicitud", response);

		Objeto datos = response.objetos("Datos").get(0);
		Solicitud solicitud = (new Gson()).fromJson(datos.toJson(), Solicitud.class);
		return solicitud;
	}

	/* ========== GENERAR INTEGRANTE ========== */
	public Solicitud generarIntegrantes(ContextoHB contexto, String... cuits) {
		for (String cuit : cuits) {
			if (cuit != null) {
				ApiRequest request = Api.request("VentasGenerarIntegrante", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/integrantes", contexto);
				request.headers.put("X-Handle", this.IdSolicitud);
				request.path("SolicitudId", this.IdSolicitud);
				request.body("NumeroTributario", Long.valueOf(cuit));
				request.body("TipoOperacion", "03");

				ApiResponse response = Api.response(request, contexto.idCobis());
				logApiVentas(contexto, this.IdSolicitud, "generarIntegrantes", response);
				if (response.hayError() || !response.objetos("Errores").isEmpty()) {
					throw new ApiVentaException(response);
				}
			}
		}
		Solicitud solicitud = solicitud(contexto, this.IdSolicitud);
		return solicitud;
	}

	/* ========== GENERAR PRESTAMO ========== */
	public Solicitud actualizarPrestamo(ContextoHB contexto, String numeroCuenta, String idEmpresaAseguradora, BigDecimal monto, String detalleTrabajo, Boolean esBatch) {
		String nombreCompleto = this.Integrantes.get(0).Apellido + " " + this.Integrantes.get(0).Nombres;
		nombreCompleto = nombreCompleto.substring(0, nombreCompleto.length() < 40 ? nombreCompleto.length() - 1 : 39);

		SolicitudPrestamo prestamo = prestamo(contexto);
		prestamo.TipoOperacion = "03";
		prestamo.Mercado = "01";
		prestamo.FormaCobroTipo = numeroCuenta.startsWith("3") ? "NDMNCC" : "NDMNCA";
		prestamo.FormaCobroCuenta = numeroCuenta;
		prestamo.Domicilio.Tipo = "DP";
		prestamo.EmpresaAseguradora = Integer.valueOf(idEmpresaAseguradora);
		prestamo.DetalleTrabajo = detalleTrabajo;
		prestamo.Desembolsos.NroDesembolso = 1;
		prestamo.Desembolsos.FormasDesembolso.add(new FormasDesembolso());
		prestamo.Desembolsos.FormasDesembolso.get(0).NroDesembolso = 1;
		prestamo.Desembolsos.FormasDesembolso.get(0).Forma = numeroCuenta.startsWith("3") ? "NCMNCC" : "NCMNCA";
		prestamo.Desembolsos.FormasDesembolso.get(0).Referencia = numeroCuenta;
		prestamo.Desembolsos.FormasDesembolso.get(0).Beneficiario = nombreCompleto;
		prestamo.Desembolsos.FormasDesembolso.get(0).Valor = monto;
		prestamo.Desembolsos.FormasDesembolso.get(0).EsDesembolsoBatch = esBatch;
		prestamo.MontoManoObra = monto.divide(new BigDecimal("2"), RoundingMode.UP);
		prestamo.MontoMateriales = monto.divide(new BigDecimal("2"), RoundingMode.DOWN);
		prestamo.MontoArtefactos = new BigDecimal("0");
		prestamo.MailAvisos = prestamo.new MailAvisos();
		prestamo.MailAvisos.Tipo = "EMP";
		prestamo.AvisosCorreoTradicional = false;
		prestamo.AvisosViaMail = true;

		ApiRequest request = Api.request("VentasActualizarPrestamoProcrearRefaccion", "ventas_windows", "PUT", "/solicitudes/{numeroSolicitud}/prestamoPersonal/{idProducto}", contexto);
		request.headers.put("X-Handle", this.IdSolicitud);
		request.path("numeroSolicitud", this.IdSolicitud);
		request.path("idProducto", prestamo.Id);
		request.body(Objeto.fromJson((new Gson()).toJson(prestamo)));

		ApiResponse response = Api.response(request, contexto.idCobis());
		logApiVentas(contexto, this.IdSolicitud, "actualizarPrestamo", response);
		if (response.hayError() || !response.objetos("Errores").isEmpty()) {
			throw new ApiVentaException(response);
		}

		Solicitud solicitud = solicitud(contexto, this.IdSolicitud);
		return solicitud;
	}

	public SolicitudPrestamo consultarPrestamoPersonal(ContextoHB contexto, String idProducto) {
		ApiRequest request = Api.request("VentasConsultarSolicitudPrestamoPersonal", "ventas_windows", "GET", "/solicitudes/{SolicitudId}/prestamoPersonal/{ProductoId}", contexto);
		request.headers.put("X-Handle", this.IdSolicitud);
		request.path("SolicitudId", this.IdSolicitud);
		request.path("ProductoId", idProducto);
		ApiResponse response = Api.response(request, contexto.idCobis());
		logApiVentas(contexto, this.IdSolicitud, "consultaPrestamoPersonal", response);
		if (response.hayError() || !response.objetos("Errores").isEmpty()) {
			// ApiOmnicanalidad.insertarLogApiVentas(contexto, this.IdSolicitud,
			// "VentasConsultaPrestamo",
			// response.objetos("Errores").get(0).string("MensajeCliente"),
			// response.objetos("Errores").get(0).string("MensajeDesarrollador"),
			// response.objetos("Errores").get(0).string("Codigo"));
			logApiVentas(contexto, IdSolicitud, "VentasConsultaPrestamo", response);
			throw new ApiVentaException(response);
		}

		Objeto datos = response.objetos("Datos").get(0);
		SolicitudPrestamo solicitudPrestamo = (new Gson()).fromJson(datos.toJson(), SolicitudPrestamo.class);
		return solicitudPrestamo;
	}

	/* ========== MOTOR ========== */
	public ResolucionMotor ejecutarMotor(ContextoHB contexto) {
		Boolean mejorarOferta = contexto.parametros.bool("mejorarOferta", false);

		if (mejorarOferta && ConfigHB.bool("prendido_canal_amarillo_pp")) {
			return ejecutarMotorMejorarOferta(contexto);
		} else {
			return ejecutarMotor(contexto, null);
		}
	}

	/*
	 * public ResolucionMotor ejecutarMotor(ContextoHB contexto) { return
	 * ejecutarMotor(contexto, null); }
	 */

	public ResolucionMotor consultarMotor(ContextoHB contexto, Boolean solicitaComprobarIngresos) {
		ApiRequest request = Api.request("VentasConsultarMotor", "ventas_windows", "GET", "/solicitudes/{SolicitudId}/resoluciones", contexto);
		request.headers.put("X-Handle", this.IdSolicitud);
		request.path("SolicitudId", this.IdSolicitud);

		ApiResponse response = Api.response(request, contexto.idCobis());
		logMotor(contexto, this.IdSolicitud, response);
		if (response.hayError() || !response.objetos("Errores").isEmpty()) {
			throw new ApiVentaException(response);
		}

		Objeto datos = response.objetos("Datos").get(0);
		ResolucionMotor resolucionMotor = (new Gson()).fromJson(datos.toJson(), ResolucionMotor.class);
		return resolucionMotor;
	}

	public ResolucionMotor ejecutarMotor(ContextoHB contexto, Boolean solicitaComprobarIngresos) {
		RestOmnicanalidad.actualizarCanalSolicitud(contexto, this.IdSolicitud);
		ApiRequest request = Api.request("VentasEvaluarSolicitud", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}/resoluciones", contexto);
		request.headers.put("X-Handle", this.IdSolicitud);
		request.path("SolicitudId", this.IdSolicitud);
		request.body("TipoOperacion", "03");
		request.body("FlagSolicitaComprobarIngresos", solicitaComprobarIngresos);

		if (contexto.esJubilado() && contexto.tieneCuentaCategoriaB()) {
			request.body("EsPlanSueldo", true);
			request.body("situacionLaboral", "11");
		}

		ApiResponse response = Api.response(request, contexto.idCobis());
		logMotor(contexto, this.IdSolicitud, response);
		if (response.hayError() || !response.objetos("Errores").isEmpty()) {
			throw new ApiVentaException(response);
		}

		Objeto datos = response.objetos("Datos").get(0);
		ResolucionMotor resolucionMotor = (new Gson()).fromJson(datos.toJson(), ResolucionMotor.class);
		return resolucionMotor;
	}

	public ResolucionMotor ejecutarMotorMejorarOferta(ContextoHB contexto) {

		ApiRequest request = Api.request("VentasEvaluarSolicitud", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}/resoluciones", contexto);
		request.headers.put("X-Handle", this.IdSolicitud);
		request.path("SolicitudId", this.IdSolicitud);
		request.body("TipoOperacion", "03");
		request.body("FlagSolicitaComprobarIngresos", 1);
		request.body("FlagSolicitaAprobacionCentralizada", 1);

		requestCanalAmarillo(contexto, request);

		ApiResponse response = Api.response(request, contexto.idCobis());
		logMotor(contexto, this.IdSolicitud, response);
		if (response.hayError() || !response.objetos("Errores").isEmpty()) {
			logMotor(contexto, this.IdSolicitud, response);
			throw new ApiVentaException(response);
		}

		Objeto datos = response.objetos("Datos").get(0);
		ResolucionMotor resolucionMotor = (new Gson()).fromJson(datos.toJson(), ResolucionMotor.class);
		return resolucionMotor;
	}

	private static void requestCanalAmarillo(ContextoHB contexto, ApiRequest request) {
		String idSituacionLaboral = contexto.parametros.string("idSituacionLaboral");
		String ingresoNeto = contexto.parametros.string("ingresoNeto");
		String cuitEmpleador = contexto.parametros.string("cuit");
		String fecha = contexto.parametros.string("fecha", "");
		String cuitAnses = "33637617449";
		String categoriaMonotributista = contexto.parametros.string("letra", "").toUpperCase();

		SituacionLaboral situacionLaboralCobis = SituacionLaboral.situacionLaboralPrincipal(contexto);

		if (Objeto.empty(fecha)) {
			fecha = situacionLaboralCobis.fechaInicioActividad;
		}

		if ("6".equals(idSituacionLaboral) || "66".equals(idSituacionLaboral)) {
			request.body("SituacionLaboral", idSituacionLaboral);
			request.body("FechaCategoriaMonotributo", fecha);
			request.body("FechaInicio", fecha);
			request.body("CategoriaMonotributo", HBCatalogo.idCategoriaMonotributo(contexto, categoriaMonotributista));
			request.body("IngresosMensuales", HBCatalogo.montoMonotributo(contexto, categoriaMonotributista));
			request.body("CuitEmpleador", contexto.persona().cuit());

		}
		if ("11".equals(idSituacionLaboral) || "1".equals(idSituacionLaboral)) {
			request.body("IngresosMensuales", ingresoNeto);
			request.body("SituacionLaboral", idSituacionLaboral);
			request.body("FechaInicio", fecha);
			request.body("RazonSocial", "11".equals(idSituacionLaboral) ? "" : situacionLaboralCobis.razonSocialEmpleador);
			request.body("CuitEmpleador", "11".equals(idSituacionLaboral) ? cuitAnses : Objeto.empty(cuitEmpleador) ? situacionLaboralCobis.cuitEmpleador : cuitEmpleador); // para jubilados enviar por defecto Anses
		}

		request.body("cargo", "Empresa - Administrativo");
		request.body("profesion", "No tiene");
		request.body("ramo", "OTROS");
		request.body("FlagSolicitaEvaluacionCanalAmarillo", 1);
		return;

	}

	/* ========== FINALIZAR ========== */
	public Solicitud simularFinalizar(ContextoHB contexto) {
		ApiRequest request = Api.request("VentasSimularFinalizarSolicitud", "ventas_windows", "GET", "/solicitudes/{SolicitudId}", contexto);
		request.headers.put("X-Handle", this.IdSolicitud);
		request.path("SolicitudId", this.IdSolicitud);
		request.query("estado", "validar");

		ApiResponse response = Api.response(request, contexto.idCobis());
		logApiVentas(contexto, this.IdSolicitud, "simularFinalizar", response);
		if (response.hayError() || !response.objetos("Errores").isEmpty()) {
			throw new ApiVentaException(response);
		}

		Solicitud solicitud = solicitud(contexto, this.IdSolicitud);
		return solicitud;
	}

	public Solicitud finalizar(ContextoHB contexto) {
		ApiRequest request = Api.request("VentasFinalizarSolicitud", "ventas_windows", "GET", "/solicitudes/{SolicitudId}", contexto);
		request.headers.put("X-Handle", this.IdSolicitud);
		request.path("SolicitudId", this.IdSolicitud);
		request.query("estado", "finalizar");

		ApiResponse response = Api.response(request, contexto.idCobis());
		logApiVentas(contexto, this.IdSolicitud, "finalizar", response);
		if (response.hayError() || !response.objetos("Errores").isEmpty()) {
			throw new ApiVentaException(response);
		}

		Solicitud solicitud = solicitud(contexto, this.IdSolicitud);
		return solicitud;
	}

	/* ========== UTIL ========== */
	public static Boolean logApiVentas(ContextoHB contexto, String numeroSolicitud, String servicio, ApiResponse response) {
		String mensajeCliente = "";
		String mensajeDesarrollador = "";
		String http = "";

		try {
			mensajeCliente = response.objetos("Errores").get(0).string("MensajeCliente");
			mensajeDesarrollador = response.objetos("Errores").get(0).string("MensajeDesarrollador");
			http = response.objetos("Errores").get(0).string("Codigo");
		} catch (Exception e) {
		}

		try {
			String sql = "";
			sql += " INSERT INTO [Homebanking].[dbo].[log_api_ventas] (momento,idCobis,numeroDocumento,numeroSolicitud,servicio,resolucionMotor,explicacionMotor,mensajeCliente,mensajeDesarrollador,canal)";
			sql += " VALUES (GETDATE(), ?, ?, ?, ?, ?, ?, ?, ?, ?)";

			SqlRequest sqlRequest = Sql.request("InsertLogApiVentas", "homebanking");
			sqlRequest.sql = sql;
			sqlRequest.parametros.add(Texto.substring(contexto.idCobis(), 250));
			sqlRequest.parametros.add(Texto.substring(contexto.persona().numeroDocumento(), 250));
			sqlRequest.parametros.add(Texto.substring(numeroSolicitud, 250));
			sqlRequest.parametros.add(Texto.substring(servicio, 250));
			sqlRequest.parametros.add(null);
			sqlRequest.parametros.add(null);
			sqlRequest.parametros.add(Texto.substring(http + " - " + mensajeCliente, 990));
			sqlRequest.parametros.add(Texto.substring(mensajeDesarrollador, 990));
			sqlRequest.parametros.add("HB");
			Sql.response(sqlRequest);
		} catch (Exception e) {
		}
		return true;
	}

	public static void logMotor(ContextoHB contexto, String numeroSolicitud, ApiResponse response) {
		String resolucion = "";
		String explicacion = "";

		try {
			resolucion = response.objetos("Datos").get(0).string("ResolucionId");
			explicacion = response.objetos("Datos").get(0).string("Explicacion");
		} catch (Exception e) {
		}

		try {
			String sql = "";
			sql += " INSERT INTO [Homebanking].[dbo].[log_api_ventas] (momento,idCobis,numeroDocumento,numeroSolicitud,servicio,resolucionMotor,explicacionMotor,mensajeCliente,mensajeDesarrollador,canal)";
			sql += " VALUES (GETDATE(), ?, ?, ?, ?, ?, ?, ?, ?, ?)";

			SqlRequest sqlRequest = Sql.request("InsertLogApiVentas", "homebanking");
			sqlRequest.sql = sql;
			sqlRequest.parametros.add(Texto.substring(contexto.idCobis(), 250));
			sqlRequest.parametros.add(Texto.substring(contexto.persona().numeroDocumento(), 250));
			sqlRequest.parametros.add(Texto.substring(numeroSolicitud, 250));
			sqlRequest.parametros.add(Texto.substring("motor", 250));
			sqlRequest.parametros.add(Texto.substring(resolucion, 990));
			sqlRequest.parametros.add(Texto.substring(explicacion, 990));
			sqlRequest.parametros.add(null);
			sqlRequest.parametros.add(null);
			sqlRequest.parametros.add("HB");
			Sql.response(sqlRequest);
		} catch (Exception e) {
		}
	}

	public static Solicitud solicitudPrestamoComplementario(ContextoHB contexto, String cuit) {
		List<Solicitud> solicitudes = solicitudes(contexto, cuit, null);
		for (Solicitud solicitud : solicitudes) {
			if (solicitud.contienePrestamo()) {
				SolicitudPrestamo prestamo = solicitud.prestamo(contexto);
				if (prestamo.esPrestamoComplementario()) {
					return solicitud;
				}
			}
		}
		return null;
	}

	public Solicitud generarPrestamoComplementario(ContextoHB contexto, BigDecimal montoSolicitado, Integer plazo, String... cuitsIntegrantes) {
		String subProducto = ConfigHB.esDesarrollo() ? "43" : "30";
		if (subProducto.isEmpty()) {
			throw new RuntimeException();
		}

		ApiRequest request = Api.request("VentasGenerarPrestamoComplementario", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/prestamoPersonal", contexto);
		request.headers.put("X-Handle", this.IdSolicitud);
		request.path("SolicitudId", this.IdSolicitud);
		request.body(new Objeto());

		Objeto datos = request.body();
		datos.set("TipoOperacion", "02");
		datos.set("SubProducto", subProducto);
		datos.set("MontoSolicitado", montoSolicitado);
		datos.set("Amortizacion", "01");
		datos.set("TipoTasa", "01");
		datos.set("DestinoBien", ConfigHB.esDesarrollo() ? 167 : 42);

		datos.set("DestinoBienVivienda", "01");
		datos.set("DescripcionDestinoFondos", "Mejoramiento Materiales");
		datos.set("Mercado", "01");
		datos.set("destinoVivienda", "0");
		datos.set("destino", "044");
		datos.set("Domicilio", new Objeto().set("Tipo", "DP"));

		if (Objects.nonNull(plazo)) {
			datos.set("PlazoSolicitado", plazo);
		}

		for (Integer i = 0; i < cuitsIntegrantes.length; ++i) {
			if (Objects.nonNull(cuitsIntegrantes[i])) {
				Objeto integrante = new Objeto();
				integrante.set("numeroDocumentoTributario", cuitsIntegrantes[i]);
				integrante.set("rol", i == 0 ? "D" : "C");
				request.add("Integrantes", integrante);
			}
		}

		ApiResponse response = Api.response(request, contexto.idCobis());
		logApiVentas(contexto, this.IdSolicitud, "generarPrestamoComplementario", response);
		if (response.hayError() || !response.objetos("Errores").isEmpty()) {
			HBOmnicanalidad.insertarLogApiVentas(contexto, this.IdSolicitud, "VentasGenerarPrestamoComplementario", response.objetos("Errores").get(0).string("MensajeCliente"), response.objetos("Errores").get(0).string("MensajeDesarrollador"), response.objetos("Errores").get(0).string("Codigo"));
			throw new ApiVentaException(response);
		}

		Solicitud solicitud = solicitud(contexto, this.IdSolicitud);
		return solicitud;
	}

	public Solicitud actualizarPrestamoComplementario(ContextoHB contexto, BigDecimal montoSolicitado, Integer plazo, String... cuitsIntegrantes) {
		String subProducto = ConfigHB.esDesarrollo() ? "43" : "30";
		if (subProducto.isEmpty()) {
			throw new RuntimeException();
		}

		ApiRequest request = Api.request("VentasActualizarPrestamoComplementario", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}/prestamoPersonal/{PrestamoId}", contexto);
		request.headers.put("X-Handle", this.IdSolicitud);
		request.path("SolicitudId", this.IdSolicitud);
		request.path("PrestamoId", idPrestamo());
		request.body(new Objeto());

		Objeto datos = request.body();
		datos.set("TipoOperacion", "02");
		datos.set("SubProducto", subProducto);
		datos.set("MontoSolicitado", montoSolicitado);
		datos.set("Amortizacion", "01");
		datos.set("TipoTasa", "01");
		datos.set("DestinoBien", ConfigHB.esDesarrollo() ? 167 : 42);

		datos.set("DestinoBienVivienda", "01");
		datos.set("DescripcionDestinoFondos", "Mejoramiento Materiales");
		datos.set("Mercado", "01");
		datos.set("destinoVivienda", "0");
		datos.set("destino", "044");
		datos.set("Domicilio", new Objeto().set("Tipo", "DP"));

		if (Objects.nonNull(plazo)) {
			datos.set("PlazoSolicitado", plazo);
		}
		for (Integer i = 0; i < cuitsIntegrantes.length; ++i) {
			if (Objects.nonNull(cuitsIntegrantes[i])) {
				Objeto integrante = new Objeto();
				integrante.set("numeroDocumentoTributario", cuitsIntegrantes[i]);
				integrante.set("rol", i == 0 ? "D" : "C");
				request.add("Integrantes", integrante);
			}
		}

		ApiResponse response = Api.response(request, contexto.idCobis());
		logApiVentas(contexto, this.IdSolicitud, "actualizarPrestamoComplementario", response);
		if (response.hayError() || !response.objetos("Errores").isEmpty()) {
			HBOmnicanalidad.insertarLogApiVentas(contexto, this.IdSolicitud, "VentasActualizarPrestamoComplementario", response.objetos("Errores").get(0).string("MensajeCliente"), response.objetos("Errores").get(0).string("MensajeDesarrollador"), response.objetos("Errores").get(0).string("Codigo"));
			throw new ApiVentaException(response);
		}

		Solicitud solicitud = solicitud(contexto, this.IdSolicitud);
		return solicitud;
	}

	public static Boolean logOriginacion(ContextoHB contexto, String numeroSolicitud, String servicio, ApiResponse response, String mensaje) {
		String mensajeCliente = "";
		String mensajeDesarrollador = "";
		String http = "";

		try {
			mensajeCliente = mensaje;
			if (response != null) {
				mensajeCliente = response.objetos("Errores").get(0).string("MensajeCliente");
				mensajeDesarrollador = response.objetos("Errores").get(0).string("MensajeDesarrollador");
				http = response.objetos("Errores").get(0).string("Codigo");
			}

		} catch (Exception e) {
		}

		try {
			String sql = "";
			sql += " INSERT INTO [Homebanking].[dbo].[log_originacion] (momento,idCobis,numeroDocumento,numeroSolicitud,servicio,resolucionMotor,explicacionMotor,mensajeCliente,mensajeDesarrollador,canal)";
			sql += " VALUES (GETDATE(), ?, ?, ?, ?, ?, ?, ?, ?, ?)";

			SqlRequest sqlRequest = Sql.request("InsertLogApiVentas", "homebanking");
			sqlRequest.sql = sql;
			sqlRequest.parametros.add(Texto.substring(contexto.idCobis(), 250));
			sqlRequest.parametros.add(Texto.substring(contexto.persona().numeroDocumento(), 250));
			sqlRequest.parametros.add(Texto.substring(numeroSolicitud, 250));
			sqlRequest.parametros.add(Texto.substring(servicio, 250));
			sqlRequest.parametros.add(null);
			sqlRequest.parametros.add(null);
			sqlRequest.parametros.add(Texto.substring(http + " - " + mensajeCliente, 990));
			sqlRequest.parametros.add(Texto.substring(mensajeDesarrollador, 990));
			sqlRequest.parametros.add("HB");
			Sql.response(sqlRequest);
		} catch (Exception e) {
		}

		return true;
	}

	public SolicitudCuentaCorriente generarAdelanto(ContextoHB contexto, String idSolicitud, String numeroCajaAhorro) {

		ApiResponse response = RestVenta.generarCuentaCorriente(contexto, idSolicitud, "ADE", numeroCajaAhorro);
		logApiVentas(contexto, this.IdSolicitud, "generarAdelanto", response);
		if (response.hayError() || !response.objetos("Errores").isEmpty()) {
			logApiVentas(contexto, this.IdSolicitud, "VentasGenerarAdelanto", response);
			throw new ApiVentaException(response);
		}

		Objeto datos = response.objetos("Datos").get(0);
		SolicitudCuentaCorriente solicitudAdelanto = (new Gson()).fromJson(datos.toJson(), SolicitudCuentaCorriente.class);
		return solicitudAdelanto;
	}

	public Boolean tipoNemonico(String nemonico) {
		if (this.Productos != null) {
			for (SolicitudProducto producto : this.Productos) {
				return nemonico.equalsIgnoreCase(producto.Nemonico);
			}
		}
		return false;
	}

	public Boolean esAdelanto() {
		return tipoNemonico("PPADELANTO");
	}
}
