package ar.com.hipotecario.canal.homebanking.negocio;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Concurrencia;
import ar.com.hipotecario.canal.homebanking.lib.Fecha;

public class PlazoFijoLogro {

	/* ========== ATRIBUTOS ========== */
	private ContextoHB contexto;
	private Objeto plazosFijosWindowsGetLogroCabecera;
//	private List<Objeto> plazosFijosWindowsGetLogroDetalle = new ArrayList<>();
	public Boolean existenErrores = false;

	/* ========== CONSTRUCTOR ========== */
	public PlazoFijoLogro(ContextoHB contexto, Objeto plazosFijosWindowsGetLogroCabecera) {
		this.contexto = contexto;
		this.plazosFijosWindowsGetLogroCabecera = plazosFijosWindowsGetLogroCabecera;
	}

	/* ========== SERVICIOS ========== */
	public static void eliminarCachePlazosFijosWindowsGetLogroCabecera(ContextoHB contexto) {
		Api.eliminarCache(contexto, "PlazosFijosWindowsGetLogroCabecera", contexto.idCobis());
		Api.eliminarCache(contexto, "CabeceraPlazoFijoLogro", contexto.idCobis());

		Api.eliminarCache(contexto, "PlazosFijosGetLogroCabecera", contexto.idCobis());

	}

	public static Map<PlazoFijoLogro, List<ApiResponse>> plazosFijosLogroDetalle(ContextoHB contexto) {
		Map<PlazoFijoLogro, List<ApiResponse>> mapa = new ConcurrentHashMap<>();
		List<PlazoFijoLogro> plazosFijosLogros = contexto.plazosFijosLogros();
		ExecutorService executorService = Concurrencia.executorService(plazosFijosLogros);
		for (PlazoFijoLogro plazoFijoLogro : plazosFijosLogros) {
			executorService.submit(() -> {
				if (!plazoFijoLogro.existenErrores) {
					Integer secuencial = 0;
					Integer secuencialAnterior = 0;
					List<ApiResponse> responses = new ArrayList<>();
					do {
						secuencialAnterior = secuencial;
						ApiResponse response = plazosFijosLogroDetalle(plazoFijoLogro, secuencial, contexto);
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
			});
		}
		Concurrencia.esperar(executorService, null);
		return mapa;
	}
	
	public static ApiResponse plazosFijosLogroDetalle(PlazoFijoLogro plazoFijoLogro, Integer secuencial, ContextoHB contexto) {
		ApiRequest request = null;
		request = Api.request("PlazosFijosGetLogroDetalle", "plazosfijos", "GET", "/v1/planAhorro/detalle", contexto);
		request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));
		request.query("operacion", "Q");
		request.query("opcion", "3");
		request.query("codCliente", contexto.idCobis());
		request.query("planContratado", plazoFijoLogro.id());
		request.query("secuencial", secuencial.toString());
		request.cacheSesion = true;
		ApiResponse response = Api.response(request, contexto.idCobis(), plazoFijoLogro.id(), secuencial);
		return response;
	}

	public void eliminarCachePlazosFijosWindowsGetLogroDetalle() {
		Api.eliminarCache(contexto, "PlazosFijosWindowsGetLogroDetalle", id(), 0);
		Api.eliminarCache(contexto, "PlazosFijosGetLogroDetalle", id(), 0);
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

	public String canal() {
		return plazosFijosWindowsGetLogroCabecera.string("canal");
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

	public Boolean esUva(Map<PlazoFijoLogro, List<ApiResponse>> detalles) {
		return montoUva(detalles) != null;
	}

	public BigDecimal montoUva(Map<PlazoFijoLogro, List<ApiResponse>> detalles) {
		Integer id = secuencialUltimoConstituido(detalles);
		Objeto detalle = detallePlazoFijo(id, detalles);
		return detalle.bigDecimal("montoUVA");
	}

	public Integer constituidos(Map<PlazoFijoLogro, List<ApiResponse>> detalles) {
		Integer cantidad = 0;
		for (Integer id = 1; id <= cantidadPlazosFijos(); ++id) {
			Objeto detalle = detallePlazoFijo(id, detalles);
			if ("A".equals(detalle.string("estado"))) {
				++cantidad;
			}
		}
		return cantidad;
	}

	public Integer vencidos(Map<PlazoFijoLogro, List<ApiResponse>> detalles) {
		Integer cantidad = 0;
		for (Integer id = 1; id <= cantidadPlazosFijos(); ++id) {
			Objeto detalle = detallePlazoFijo(id, detalles);
			if ("V".equals(detalle.string("estado"))) {
				++cantidad;
			}
		}
		return cantidad;
	}

	public Integer pendientes(Map<PlazoFijoLogro, List<ApiResponse>> detalles) {
		Integer cantidad = 0;
		for (Integer id = 1; id <= cantidadPlazosFijos(); ++id) {
			Objeto detalle = detallePlazoFijo(id, detalles);
			if ("N".equals(detalle.string("estado"))) {
				++cantidad;
			}
		}
		return cantidad;
	}

	public BigDecimal montoAcumulado(Map<PlazoFijoLogro, List<ApiResponse>> detalles) {
		BigDecimal monto = new BigDecimal("0");
		for (Integer id = 1; id <= cantidadPlazosFijos(); ++id) {
			Objeto detalle = detallePlazoFijo(id, detalles);
			if ("A".equals(detalle.string("estado"))) {
				monto = monto.add(detalle.bigDecimal("monto"));
			}
		}
		return monto;
	}

	public Integer porcentajeDiasTranscurridos() {
		return Fecha.porcentajeTranscurrido(fechaAlta(), fechaVencimiento());
	}

	public Integer diasRestantes() {
		return Fecha.cantidadDias(new Date(), fechaVencimiento());
	}

	public Integer secuencialUltimoConstituido(Map<PlazoFijoLogro, List<ApiResponse>> detalles) {
		Integer secuencial = null;
		for (Integer id = 1; id <= cantidadPlazosFijos(); ++id) {
			Objeto detalle = detallePlazoFijo(id, detalles);
			if ("A".equals(detalle.string("estado"))) {
				if (secuencial == null || detalle.integer("secuencial") > secuencial) {
					secuencial = detalle.integer("secuencial");
				}
			}
		}
		return secuencial;
	}

	/* ========== PLAZOS FIJOS ========== */
	public String descripcionPFLogros(Integer secuencial, Map<PlazoFijoLogro, List<ApiResponse>> detalles) {
		if (esUva(detalles)) {
			return "Cuota " + secuencial + " del Plazo Fijo Logros en UVA";
		}
		return "Cuota " + secuencial + " del Plazo Fijo Logros";
	}

	public String itemFechaPagoCuota(Integer secuencial, String formato, Map<PlazoFijoLogro, List<ApiResponse>> detalles) {
		Objeto detalle = detallePlazoFijo(secuencial, detalles);
		Date fecha = detalle.date("fechaConstiFin", "yyyy-MM-dd'T'HH:mm:ss");
		return fecha != null ? new SimpleDateFormat(formato).format(fecha) : null;
	}

	public String itemFechaVencimientoCuota(Integer secuencial, String formato, Map<PlazoFijoLogro, List<ApiResponse>> detalles) {
		Objeto detalle = detallePlazoFijo(secuencial, detalles);
		Date fecha = detalle.date("fechaConstiTeo", "yyyy-MM-dd'T'HH:mm:ss");
		return fecha != null ? new SimpleDateFormat(formato).format(fecha) : null;
	}

	public Date itemFechaAlta(Integer secuencial, Map<PlazoFijoLogro, List<ApiResponse>> detalles) {
		Objeto detalle = detallePlazoFijo(secuencial, detalles);
		Date fecha = detalle.date("fechaConstiFin", "yyyy-MM-dd'T'HH:mm:ss");
		fecha = fecha != null ? fecha : detalle.date("fechaConstiTeo", "yyyy-MM-dd'T'HH:mm:ss");
		return fecha;
	}

	public String itemFechaAlta(Integer secuencial, String formato, Map<PlazoFijoLogro, List<ApiResponse>> detalles) {
		return new SimpleDateFormat(formato).format(itemFechaAlta(secuencial, detalles));
	}

	public Date itemFechaVencimiento(Integer secuencial, Map<PlazoFijoLogro, List<ApiResponse>> detalles) {
		Objeto detalle = detallePlazoFijo(secuencial, detalles);
		return detalle.date("fechaVen", "yyyy-MM-dd'T'HH:mm:ss");
	}

	public String itemFechaVencimiento(Integer secuencial, String formato, Map<PlazoFijoLogro, List<ApiResponse>> detalles) {
		return new SimpleDateFormat(formato).format(itemFechaVencimiento(secuencial, detalles));
	}

	public Integer itemCantidadDias(Integer secuencial, Map<PlazoFijoLogro, List<ApiResponse>> detalles) {
		return Fecha.cantidadDias(itemFechaAlta(secuencial, detalles), itemFechaVencimiento(secuencial, detalles));
	}

	public BigDecimal itemMontoInicial(Integer secuencial, Map<PlazoFijoLogro, List<ApiResponse>> detalles) {
		Objeto detalle = detallePlazoFijo(secuencial, detalles);
		return detalle.bigDecimal("monto");
	}

	public BigDecimal itemMontoVencimiento(Integer secuencial, Map<PlazoFijoLogro, List<ApiResponse>> detalles) {
		Objeto detalle = detallePlazoFijo(secuencial, detalles);
		return detalle.bigDecimal("monto", "0").add(detalle.bigDecimal("montoInteres", "0")).subtract(detalle.bigDecimal("montoImpuestos", "0"));
	}

	public BigDecimal itemTasa(Integer secuencial, Map<PlazoFijoLogro, List<ApiResponse>> detalles) {
		Objeto detalle = detallePlazoFijo(secuencial, detalles);
		return detalle.bigDecimal("tasa");
	}

	public BigDecimal itemInteres(Integer secuencial, Map<PlazoFijoLogro, List<ApiResponse>> detalles) {
		Objeto detalle = detallePlazoFijo(secuencial, detalles);
		return detalle.bigDecimal("montoInteres");
	}

	public String itemIdEstado(Integer secuencial, Map<PlazoFijoLogro, List<ApiResponse>> detalles) {
		Objeto detalle = detallePlazoFijo(secuencial, detalles);
		return detalle.string("estado");
	}

	public String itemDescripcionEstado(Integer secuencial, Map<PlazoFijoLogro, List<ApiResponse>> detalles) {
		Objeto detalle = detallePlazoFijo(secuencial, detalles);
		return PlazoFijoLogro.estadoDetalle(detalle.string("estado"));
	}

	public String itemNumero(Integer secuencial, Map<PlazoFijoLogro, List<ApiResponse>> detalles) {
		Objeto detalle = detallePlazoFijo(secuencial, detalles);
		return detalle.string("nroCertificado");
	}

	public Boolean itemGarantiaDeposito(Integer secuencial, Map<PlazoFijoLogro, List<ApiResponse>> detalles) {
		Objeto detalle = detallePlazoFijo(secuencial, detalles);
		return "S".equals(detalle.string("garantizado"));
	}

	public Boolean forzable(Integer secuencial, Map<PlazoFijoLogro, List<ApiResponse>> detalles) {
		Integer idForzable = null;
		if ("A".equals(idEstado())) {
			for (Integer id = 1; id <= cantidadPlazosFijos(); ++id) {
				Objeto detalle = detallePlazoFijo(id, detalles);
				if (Objeto.setOf("N", "V").contains(detalle.string("estado"))) {
					idForzable = id;
					break;
				}
			}
		}
		return secuencial.equals(idForzable);
	}

	/* ========== UTIL ========== */
	public Objeto detallePlazoFijo(Integer secuencial, Map<PlazoFijoLogro, List<ApiResponse>> detalles) {
		List<ApiResponse> responses = detalles.get(this);
		for (Objeto detalle : responses) {
			for (Objeto objeto : detalle.objetos()) {
				if (secuencial.equals(objeto.integer("secuencial"))) {
					return objeto;
				}
			}
		}
		return null;
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