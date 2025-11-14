package ar.com.hipotecario.mobile.negocio;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.api.MBAplicacion;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Concurrencia;
import ar.com.hipotecario.mobile.lib.Fecha;
import ar.com.hipotecario.mobile.lib.Objeto;

public class PlazoFijoLogro {

	/* ========== ATRIBUTOS ========== */
	private ContextoMB contexto;
	private Objeto plazosFijosWindowsGetLogroCabecera;
	public Boolean existenErrores = false;

	/* ========== CONSTRUCTOR ========== */
	public PlazoFijoLogro(ContextoMB contexto, Objeto plazosFijosWindowsGetLogroCabecera) {
		this.contexto = contexto;
		this.plazosFijosWindowsGetLogroCabecera = plazosFijosWindowsGetLogroCabecera;
	}

	/* ========== SERVICIOS ========== */
	public static ApiResponseMB plazosFijosWindowsGetLogroCabecera(ContextoMB contexto) {

		Boolean habilitarPlazosFijosApi = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_plazo_fijo_api");
		ApiRequestMB request = null;
		if (habilitarPlazosFijosApi) {
			request = ApiMB.request("PlazosFijosGetLogroCabecera", "plazosfijos", "GET", "/v1/planAhorro/cabecera", contexto);
			request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
		} else {
			request = ApiMB.request("PlazosFijosWindowsGetLogroCabecera", "plazosfijos_windows", "GET", "/v1/planAhorro/cabecera", contexto);
		}
		request.query("operacion", "Q");
		request.query("opcion", "2");
		request.query("codCliente", contexto.idCobis());
		request.cacheSesion = true;
		request.cache204 = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static void eliminarCachePlazosFijosWindowsGetLogroCabecera(ContextoMB contexto) {
		ApiMB.eliminarCache(contexto, "PlazosFijosWindowsGetLogroCabecera", contexto.idCobis());
		ApiMB.eliminarCache(contexto, "CabeceraPlazoFijoLogro", contexto.idCobis());

		ApiMB.eliminarCache(contexto, "PlazosFijosGetLogroCabecera", contexto.idCobis());

	}

	public static Map<PlazoFijoLogro, List<ApiResponseMB>> plazosFijosWindowsGetLogroDetalle(ContextoMB contexto) {
		if (contexto.cacheDetallePlazoFijo != null) {
			return contexto.cacheDetallePlazoFijo;
		}

		Map<PlazoFijoLogro, List<ApiResponseMB>> mapa = new ConcurrentHashMap<>();
		List<PlazoFijoLogro> plazosFijosLogros = contexto.plazosFijosLogros();
	//	ExecutorService executorService = Concurrencia.executorService(plazosFijosLogros);
		for (PlazoFijoLogro plazoFijoLogro : plazosFijosLogros) {
		//	executorService.submit(() -> {
				if (!plazoFijoLogro.existenErrores) {
					Integer secuencial = 0;
					Integer secuencialAnterior = 0;
					List<ApiResponseMB> responses = new ArrayList<>();
					do {
						secuencialAnterior = secuencial;
						Boolean habilitarPlazosFijosApi = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_plazo_fijo_api");
						ApiRequestMB request = null;
						if (habilitarPlazosFijosApi) {
							request = ApiMB.request("PlazosFijosGetLogroDetalle", "plazosfijos", "GET", "/v1/planAhorro/detalle", contexto);
							request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
						} else {
							request = ApiMB.request("PlazosFijosWindowsGetLogroDetalle", "plazosfijos_windows", "GET", "/v1/planAhorro/detalle", contexto);
						}
						request.query("operacion", "Q");
						request.query("opcion", "3");
						request.query("codCliente", contexto.idCobis());
						request.query("planContratado", plazoFijoLogro.id());
						request.query("secuencial", secuencial.toString());
						request.cacheSesion = true;
						ApiResponseMB response = ApiMB.response(request, plazoFijoLogro.id(), secuencial);
						if (!response.hayError()) {
//							plazoFijoLogro.plazosFijosWindowsGetLogroDetalle.add(response);
							responses.add(response);
							for (Objeto item : response.objetos()) {
								secuencial = item.integer("secuencial");
							}
						} else {
							plazoFijoLogro.existenErrores = true;
							break;
						}
					} while (!secuencial.equals(secuencialAnterior) && secuencial < plazoFijoLogro.cantidadPlazosFijos());
					mapa.put(plazoFijoLogro, responses);
				}
		//	});
		}
	//	Concurrencia.esperar(executorService, null);
		contexto.cacheDetallePlazoFijo = mapa;
		return mapa;
	}

	public void eliminarCachePlazosFijosWindowsGetLogroDetalle() {
		ApiMB.eliminarCache(contexto, "PlazosFijosWindowsGetLogroDetalle", id(), 0);
		ApiMB.eliminarCache(contexto, "PlazosFijosGetLogroDetalle", id(), 0);
	}

	/* ========== PLAZO FIJO LOGRO ========== */
	public String id() {
		return plazosFijosWindowsGetLogroCabecera.string("idPlanAhorro");
	}

	public String numeroCuenta() {
		return plazosFijosWindowsGetLogroCabecera.string("cuenta");
	}

	public String nombre() {
		return plazosFijosWindowsGetLogroCabecera.string("nombre");
	}

	public String idMoneda() {
		return plazosFijosWindowsGetLogroCabecera.string("moneda");
	}

	public Integer cantidadPlazosFijos() {
		return plazosFijosWindowsGetLogroCabecera.integer("cantidadPlazos");
	}

	public Integer diaDebito() {
		return plazosFijosWindowsGetLogroCabecera.integer("diaConstitucionPF");
	}

	public Date fechaAlta() {
		return plazosFijosWindowsGetLogroCabecera.date("fechaConstPlan", "yyyy-MM-dd'T'HH:mm:ss");
	}

	public String fechaAlta(String formato) {
		return plazosFijosWindowsGetLogroCabecera.date("fechaConstPlan", "yyyy-MM-dd'T'HH:mm:ss", formato);
	}

	public Date fechaVencimiento() {
		return plazosFijosWindowsGetLogroCabecera.date("vencimiento", "yyyy-MM-dd'T'HH:mm:ss");
	}

	public String fechaVencimiento(String formato) {
		return plazosFijosWindowsGetLogroCabecera.date("vencimiento", "yyyy-MM-dd'T'HH:mm:ss", formato);
	}

	public BigDecimal montoProximoPlazoFijo() {
		return plazosFijosWindowsGetLogroCabecera.bigDecimal("monto");
	}

	public String idEstado() {
		return plazosFijosWindowsGetLogroCabecera.string("estado");
	}

	public String descripcionEstado() {
		return PlazoFijoLogro.estadoCabecera(plazosFijosWindowsGetLogroCabecera.string("estado"));
	}

	public Boolean esUva() {
		return montoUva() != null;
	}

	public BigDecimal montoUva() {
		Integer id = secuencialUltimoConstituido();
		Objeto detalle = detallePlazoFijo(id);
		return detalle.bigDecimal("montoUVA");
	}

	public Integer constituidos() {
		Integer cantidad = 0;
		for (Integer id = 1; id <= cantidadPlazosFijos(); ++id) {
			Objeto detalle = detallePlazoFijo(id);
			if ("A".equals(detalle.string("estado"))) {
				++cantidad;
			}
		}
		return cantidad;
	}

	public Integer vencidos() {
		Integer cantidad = 0;
		for (Integer id = 1; id <= cantidadPlazosFijos(); ++id) {
			Objeto detalle = detallePlazoFijo(id);
			if ("V".equals(detalle.string("estado"))) {
				++cantidad;
			}
		}
		return cantidad;
	}

	public Integer pendientes() {
		Integer cantidad = 0;
		for (Integer id = 1; id <= cantidadPlazosFijos(); ++id) {
			Objeto detalle = detallePlazoFijo(id);
			if ("N".equals(detalle.string("estado"))) {
				++cantidad;
			}
		}
		return cantidad;
	}

	public BigDecimal montoAcumulado() {
		BigDecimal monto = new BigDecimal("0");
		for (Integer id = 1; id <= cantidadPlazosFijos(); ++id) {
			Objeto detalle = detallePlazoFijo(id);
			if ("A".equals(detalle.string("estado"))) {
				monto = monto.add(detalle.bigDecimal("monto"));
			}
		}
		return monto;
	}

	public BigDecimal montoAcumuladoConstituidos() {
		BigDecimal montoAcumuladoConstituidos = BigDecimal.ZERO;
		for (Integer id = 1; id <= cantidadPlazosFijos(); ++id) {
			Objeto detalle = detallePlazoFijo(id);
			if ("A".equals(detalle.string("estado"))) {
				montoAcumuladoConstituidos = montoAcumuladoConstituidos.add(itemMontoVencimiento(detalle));
			}
		}
		return montoAcumuladoConstituidos;
	}

	public Integer porcentajeDiasTranscurridos() {
		return Fecha.porcentajeTranscurrido(fechaAlta(), fechaVencimiento());
	}

	public Integer diasRestantes() {
		return Fecha.cantidadDias(new Date(), fechaVencimiento());
	}

	public Integer secuencialUltimoConstituido() {
		Integer secuencial = null;
		for (Integer id = 1; id <= cantidadPlazosFijos(); ++id) {
			Objeto detalle = detallePlazoFijo(id);
			if ("A".equals(detalle.string("estado"))) {
				if (secuencial == null || detalle.integer("secuencial") > secuencial) {
					secuencial = detalle.integer("secuencial");
				}
			}
		}
		return secuencial;
	}

	/* ========== PLAZOS FIJOS ========== */
	public String descripcionPFLogros(Integer secuencial) {
		if (esUva()) {
			return "Cuota " + secuencial + " del Plazo Fijo Logros en UVA";
		}
		return "Cuota " + secuencial + " del Plazo Fijo Logros";
	}

	public Date itemFechaAlta(Integer secuencial) {
		Objeto detalle = detallePlazoFijo(secuencial);
		Date fecha = detalle.date("fechaConstiFin", "yyyy-MM-dd'T'HH:mm:ss");
		fecha = fecha != null ? fecha : detalle.date("fechaConstiTeo", "yyyy-MM-dd'T'HH:mm:ss");
		return fecha;
	}

	public String itemFechaPagoCuota(Integer secuencial, String formato) {
		Objeto detalle = detallePlazoFijo(secuencial);
		Date fecha = detalle.date("fechaConstiFin", "yyyy-MM-dd'T'HH:mm:ss");
		return fecha != null ? new SimpleDateFormat(formato).format(fecha) : null;
	}

	public String itemFechaVencimientoCuota(Integer secuencial, String formato, String estado) {
		Objeto detalle = detallePlazoFijo(secuencial);
		Date fecha = detalle.date("fechaConstiTeo", "yyyy-MM-dd'T'HH:mm:ss");
		if (!"A".equals(estado)) {
			fecha = detalle.date("fechaVen", "yyyy-MM-dd'T'HH:mm:ss");
		}
		return fecha != null ? new SimpleDateFormat(formato).format(fecha) : null;
	}

	public String itemFechaVencimientoCuota(Integer secuencial, String formato) {
		Objeto detalle = detallePlazoFijo(secuencial);
		Date fecha = detalle.date("fechaConstiTeo", "yyyy-MM-dd'T'HH:mm:ss");
		return fecha != null ? new SimpleDateFormat(formato).format(fecha) : null;
	}

	public String itemFechaAlta(Integer secuencial, String formato) {
		return new SimpleDateFormat(formato).format(itemFechaAlta(secuencial));
	}

	public Date itemFechaVencimiento(Integer secuencial) {
		Objeto detalle = detallePlazoFijo(secuencial);
		return detalle.date("fechaVen", "yyyy-MM-dd'T'HH:mm:ss");
	}

	public String itemCuota(Integer secuencial) {
		Objeto detalle = detallePlazoFijo(secuencial);
		return detalle.string("cuota");
	}

	public String itemFechaVencimiento(Integer secuencial, String formato) {
		return new SimpleDateFormat(formato).format(itemFechaVencimiento(secuencial));
	}

	public Integer itemCantidadDias(Integer secuencial) {
		return Fecha.cantidadDias(itemFechaAlta(secuencial), itemFechaVencimiento(secuencial));
	}

	public String itemMensajeEstado(Integer secuencial) {
		if ("C".equals(idEstado()) || "B".equals(idEstado()) || secuencial == cantidadPlazosFijos() && itemFechaPagoCuota(secuencial, "dd/MM/yyyy") != null) {
			return "No se debitarán más cuotas";
		}
		return "";
	}

	public BigDecimal itemMontoInicial(Integer secuencial) {
		Objeto detalle = detallePlazoFijo(secuencial);
		return detalle.bigDecimal("monto");
	}

	public BigDecimal itemMontoVencimiento(Integer secuencial) {
		Objeto detalle = detallePlazoFijo(secuencial);
		return detalle.bigDecimal("monto", "0").add(detalle.bigDecimal("montoInteres", "0")).subtract(detalle.bigDecimal("montoImpuestos", "0"));
	}

	public BigDecimal itemMontoVencimiento(Objeto detalle) {
		return detalle.bigDecimal("monto", "0").add(detalle.bigDecimal("montoInteres", "0")).subtract(detalle.bigDecimal("montoImpuestos", "0"));
	}

	public BigDecimal itemTasa(Integer secuencial) {
		Objeto detalle = detallePlazoFijo(secuencial);
		return detalle.bigDecimal("tasa");
	}

	public BigDecimal itemInteres(Integer secuencial) {
		Objeto detalle = detallePlazoFijo(secuencial);
		return detalle.bigDecimal("montoInteres");
	}

	public String itemIdEstado(Integer secuencial) {
		Objeto detalle = detallePlazoFijo(secuencial);
		return detalle.string("estado");
	}

	public String itemDescripcionEstado(Integer secuencial) {
		Objeto detalle = detallePlazoFijo(secuencial);
		return PlazoFijoLogro.estadoDetalle(detalle.string("estado"));
	}

	public String itemNumero(Integer secuencial) {
		Objeto detalle = detallePlazoFijo(secuencial);
		return detalle.string("nroCertificado");
	}

	public Boolean itemGarantiaDeposito(Integer secuencial) {
		Objeto detalle = detallePlazoFijo(secuencial);
		return "S".equals(detalle.string("garantizado"));
	}

	public Boolean forzable(Integer secuencial) {
		Integer idForzable = null;
		if ("A".equals(idEstado())) {
			for (Integer id = 1; id <= cantidadPlazosFijos(); ++id) {
				Objeto detalle = detallePlazoFijo(id);
				if (Objeto.setOf("N", "V").contains(detalle.string("estado"))) {
					idForzable = id;
					break;
				}
			}
		}
		return secuencial.equals(idForzable);
	}

	/* ========== UTIL ========== */
	public Objeto detallePlazoFijo(Integer secuencial) {
		List<ApiResponseMB> responses = plazosFijosWindowsGetLogroDetalle(contexto).get(this);
		for (Objeto detalle : responses) {
			for (Objeto objeto : detalle.objetos()) {
				if (secuencial.equals(objeto.integer("secuencial"))) {
					return objeto;
				}
			}
		}
		return null;
	}

	public String estadoDescripcionPF(String idEstado, int secuencial) {
		String descripcion = "(Cuota " + itemCuota(secuencial) + ")";
		if ("B".equals(idEstado)) {
			descripcion = "(Dado de baja)";
		}
		if ("C".equals(idEstado) && LocalDate.now().isAfter(LocalDate.parse(itemFechaVencimiento(secuencial, "yyyy-MM-dd")))) {
			descripcion = "(¡Pagaste la última cuota!)";
		}
		return descripcion;
	}

	/* ========== METODOS ESTATICOS ========== */
	public static String estadoCabecera(String idEstado) {
		String estado = "";
		estado = "A".equals(idEstado) ? "Vigente" : estado;
		estado = "B".equals(idEstado) ? "Cancelado" : estado;
		estado = "C".equals(idEstado) ? "Finalizado" : estado;
		return estado;
	}

	public static String estadoDetalle(String idEstado) {
		String estado = "";
		estado = "A".equals(idEstado) ? "Constituído" : estado;
		estado = "V".equals(idEstado) ? "Vencido" : estado;
		estado = "N".equals(idEstado) ? "Pendiente" : estado;
		return estado;
	}

	@Override
	public int hashCode() {
		String id = id();
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		String id = id();
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlazoFijoLogro other = (PlazoFijoLogro) obj;
		if (id == null) {
			if (other.id() != null)
				return false;
		} else if (!id.equals(other.id()))
			return false;
		return true;
	}

}

//	PlazosFijosWindowsGetLogroCabecera
//	[ {
//	  "spread" : 0.0,
//	  "tipoPlan" : "07",
//	  "nombre" : "NOMBRE3",
//	  "idPlanAhorro" : 122,
//	  "tipoCuenta" : "AHO",
//	  "cuenta" : "401600047810429",
//	  "monto" : 3000.00,
//	  "plazo" : null,
//	  "moneda" : 80,
//	  "tasa" : 22.6875,
//	  "tasaReferencial" : "BADLARPRMY",
//	  "vencimiento" : "2019-09-02T00:00:00",
//	  "estado" : "A",
//	  "descripcionTipoPlan" : "LOGROS PESOS",
//	  "tipoPlazoFijo" : "0025",
//	  "descripcionPlazoFijo" : "LOGROS $",
//	  "cantidadPlazos" : 6,
//	  "diaConstitucionPF" : 1,
//	  "secuencialTipoPlan" : 25,
//	  "fechaConstPlan" : "2019-03-14T00:00:00"
//	} ]

//	PlazosFijosWindowsGetLogroDetalle
//	[ {
//	  "spread" : 0.0,
//	  "idPlanAhorro" : 356,
//	  "secuencial" : 1,
//	  "cuota" : "1 de 6",
//	  "numBanco" : null,
//	  "cuenta" : "401600047810429",
//	  "fechaConstiTeo" : "2019-03-22T00:00:00",
//	  "reintentos" : 0,
//	  "fechaVen" : "2019-09-02T00:00:00",
//	  "monto" : 1000.00,
//	  "moneda" : 80,
//	  "fechaConstiFin" : "2019-03-22T00:00:00",
//	  "tasa" : 22.6875,
//	  "montoUVA" : null,
//	  "cotizacionUVA" : 0.00,
//	  "fechaCotizacionUVA" : null,
//	  "montoInteres" : 101.94,
//	  "montoImpuestos" : 0.0,
//	  "estado" : "A",
//	  "nroCertificado" : "01608000250000263",
//	  "tipoCuenta" : "AHO",
//	  "tasaInteres" : 22.6875,
//	  "garantizado" : "S"
//	} ]
