package ar.com.hipotecario.canal.homebanking.negocio;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Fecha;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Texto;

public class PlazoFijo {

	/* ========== ATRIBUTOS ========== */
	private ContextoHB contexto;
	private Objeto consolidada = new Objeto();

	/* ========== CONSTRUCTOR ========== */
	public PlazoFijo(ContextoHB contexto, Objeto productosGetConsolidada) {
		this.contexto = contexto;
		this.consolidada = productosGetConsolidada;
	}

	/* ========== SERVICIOS ========== */
	public void eliminarCachePlazosFijosWindowsGet() {
		Api.eliminarCache(contexto, "PlazosFijosWindowsGet", numero());
	}

	/* ========== METODOS ========== */
	public Boolean mostrar() {
		if (!Objeto.setOf("VEN", "CAN", "ANU").contains(consolidada.string("estadoPF"))) {
			Date vencimiento = consolidada.date("fechaVencimiento", "yyyy-MM-dd");
			if (vencimiento != null) {
				Date fechaActual = new Date();
				return vencimiento.after(fechaActual);
			}
		}
		return false;
	}

	public String id() {
		return consolidada.string("idProducto");
	}

	public String producto() {
		return "Plazo Fijo";
	}

	public String numero() {
		return consolidada.string("numeroProducto");
	}

	public String tipo() {
		return consolidada.string("tipoOperacion");
	}

	public String descripcion() {
		return tipo(consolidada.string("tipoOperacion"));
	}

	public String titularidad() {
		String titularidad = null;
		titularidad = "T".equals(consolidada.string("tipoTitularidad")) ? "Titular" : titularidad;
		titularidad = "A".equals(consolidada.string("tipoTitularidad")) ? "Cotitular" : titularidad;
		return titularidad != null ? titularidad : Texto.primeraMayuscula(consolidada.string("descTipoTitularidad"));
	}

	public String moneda() {
		return Formateador.simboloMoneda(consolidada.string("moneda"));
	}

	public String monedaActual() {
		return Formateador.simboloMonedaActual(consolidada.string("moneda"));
	}

	public String descripcionMoneda() {
		return Formateador.moneda(consolidada.string("moneda"));
	}

	public BigDecimal importeInicial() {
		return consolidada.bigDecimal("importe");
	}

	public String importeInicialFormateado() {
		return Formateador.importe(importeInicial());
	}

	public BigDecimal importeFinal() {
		try {
			return importeInicial().add(intereses());
		} catch (Exception e) {
			return null;
		}
	}

	public String importeFinalFormateado(BigDecimal impuestos) {
		return Formateador.importe(importeFinal().subtract(impuestos));
	}

	public Date fechaAlta() {
		return consolidada.date("fechaAlta", "yyyy-MM-dd");
	}

	public String fechaAlta(String formato) {
		return consolidada.date("fechaAlta", "yyyy-MM-dd", formato, "");
	}

	public Date fechaVencimiento() {
		return consolidada.date("fechaVencimiento", "yyyy-MM-dd");
	}

	public String fechaVencimiento(String formato) {
		return consolidada.date("fechaVencimiento", "yyyy-MM-dd", formato, "");
	}

	public String estado() {
		return estado(consolidada.string("estadoPF"));
	}

	public String descripcionPlazoFijo() {
		String[] tipoPF = { "0017", "0018", "0020", "0021", "0041", "0042", "0043", "0044" };

		if (descripcion().contains("Logros")) {
			return producto() + " " + tipoLogros(tipo());
		}
		if (Arrays.asList(tipoPF).contains(tipo())) {
			return producto() + " en UVA";
		}
		return producto() + " en " + descripcionMoneda();
	}

	public boolean validarPFLogros() {
		return descripcion().toUpperCase().contains("LOGROS") && tienePlanAhorro();
	}

	public Integer diasFaltantes() {
		Integer diasFaltantes = Fecha.cantidadDias(new Date(), fechaVencimiento());
		if (diasFaltantes != null) {
			diasFaltantes += 1;
		}
		return diasFaltantes;
	}

	public Integer porcentajeDiasTranscurridos() {
		return Fecha.porcentajeTranscurrido(fechaAlta(), fechaVencimiento());
	}

	public ApiResponse plazosFijosGet() {
		ApiRequest request = Api.request("PlazosFijosGet", "plazosfijos", "GET", "/v1/{idCobis}", contexto);
		request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));
		request.path("idCobis", contexto.idCobis());
		request.query("fechaInicio", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
		request.query("fechaFin", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
		request.query("certificado", numero());
		request.cacheSesion = true;
		ApiResponse response = Api.response(request, numero());
		return response;
	}

	public BigDecimal tna() {
		ApiResponse response = plazosFijosGet();

		if (response == null)
			return null;

		for (Objeto objeto : response.objetos()) {
			return objeto.bigDecimal("tasaNominal");
		}
		return null;
	}

	public String tnaFormateada() {
		return Formateador.importe(tna());
	}

	public BigDecimal tnaCancelacionAnt() {
		ApiResponse response = plazosFijosGet();

		if (response == null)
			return null;

		for (Objeto objeto : response.objetos()) {
			return objeto.bigDecimal("tnaCancelacionAnt");
		}
		return null;
	}

	public String tnaCancelacionAntFormateada() {
		return Formateador.importe(tnaCancelacionAnt());
	}

	public BigDecimal teaCancelacionAnt() {
		ApiResponse response = plazosFijosGet();

		if (response == null)
			return null;

		for (Objeto objeto : response.objetos()) {
			return objeto.bigDecimal("teaCancelacionAnt");
		}
		return null;
	}

	public String teaCancelacionAntFormateada() {
		return Formateador.importe(teaCancelacionAnt());
	}

	public String fechaDesdeCancelacionAnt(String formato) {
		ApiResponse response = plazosFijosGet();

		if (response == null)
			return null;

		for (Objeto objeto : response.objetos()) {
			return objeto.date("fechaDesdeCancelacionAnt", "yyyy-MM-dd", formato, "");
		}
		return "";
	}

	public String fechaHastaCancelacionAnt(String formato) {
		ApiResponse response = plazosFijosGet();

		if (response == null)
			return null;

		for (Objeto objeto : response.objetos()) {
			return objeto.date("fechaHastaCancelacionAnt", "yyyy-MM-dd", formato, "");
		}
		return "";
	}

	public Integer plazo() {
		ApiResponse response = plazosFijosGet();

		if (response == null)
			return null;

		for (Objeto objeto : response.objetos()) {
			return objeto.integer("plazo");
		}
		return null;
	}

	public BigDecimal intereses() {
		ApiResponse response = plazosFijosGet();

		if (response == null)
			return null;

		for (Objeto objeto : response.objetos()) {
			return objeto.bigDecimal("montoEstimado");
		}
		return null;
	}

	public String cuentaAcredita() {
		ApiResponse response = plazosFijosGet();

		if (response == null)
			return null;

		for (Objeto objeto : response.objetos()) {
			return objeto.string("nroCuenta");
		}
		return "";
	}

	public String interesesFormateado() {
		return Formateador.importe(intereses());
	}

	public BigDecimal impuestos() {
		ApiResponse response = plazosFijosGet();

		if (response == null)
			return null;

		for (Objeto objeto : response.objetos()) {
			return objeto.bigDecimal("montoXImpuestos");
		}
		return null;
	}

	public Boolean tieneRenovacionAutomatica() {
		ApiResponse response = plazosFijosGet();

		if (response == null)
			return null;

		for (Objeto objeto : response.objetos()) {
			return "S".equals(objeto.string("renovacion"));
		}
		return false;
	}

	public Boolean esRenueva() {
		ApiResponse response = plazosFijosGet();

		if (response == null)
			return null;

		for (Objeto objeto : response.objetos()) {
			return "S".equals(objeto.string("renueva"));
		}
		return false;
	}

	public Boolean esRenuevaIntereses() {
		ApiResponse response = plazosFijosGet();

		if (response == null)
			return null;

		for (Objeto objeto : response.objetos()) {
			return "S".equals(objeto.string("renuevaInteres"));
		}
		return false;
	}

	public Boolean tieneGarantiaDeposito() {
		ApiResponse response = plazosFijosGet();

		if (response == null)
			return null;

		for (Objeto objeto : response.objetos()) {
			return "S".equals(objeto.string("garantizado"));
		}
		return false;
	}

	public Boolean tieneCancelacionAnticipada() {
		ApiResponse response = plazosFijosGet();

		if (response == null)
			return null;

		for (Objeto objeto : response.objetos()) {
			return "S".equals(objeto.string("cancelacionAnticipada"));
		}
		return false;
	}

	public Boolean tienePlanAhorro() {
		ApiResponse response = plazosFijosGet();

		if (response == null)
			return null;

		for (Objeto objeto : response.objetos()) {
			return (objeto.string("planAhorro") != null || !"".equals(objeto.string("planAhorro")));
		}
		return false;
	}

	/* ========== UTIL ========== */
	public Boolean esPlazoFijoLogros() {
		try {
			Integer tipo = Integer.parseInt(tipo());
			return tipo >= 25 && tipo <= 35;
		} catch (Exception e) {
			return false;
		}
	}

	public Boolean esProcrearJoven() {
		return "0020".equals(tipo());
	}
	
	public Boolean esCedip() {
		return "1000".equals(tipo());
	}

	public Boolean esPesos() {
		return "80".equals(consolidada.string("moneda"));
	}

	public Boolean esDolares() {
		return "2".equals(consolidada.string("moneda"));
	}

	public Boolean esValido() {
		return esValidoEstado(estado()) && Fecha.esFuturo(fechaVencimiento());
	}

	public static Boolean esValidoEstado(String estado) {
		return !Objeto.setOf("VEN", "CAN", "ANU").contains(estado);
	}

	public Boolean esUva() {
		return esUva(tipo());
	}

	public Boolean esUvaPrecancelable() {
		return esUvaPrecancelable(tipo());
	}

	public static Boolean esUva(String id) {
		return esUvaNoCancelable(id) || esUvaPrecancelable(id);
	}

	public static Boolean esUvaNoCancelable(String id) {
		Integer codigo = Integer.valueOf(id);
		return Objeto.setOf(17, 18, 20, 21).contains(codigo);
	}

	public static Boolean esUvaPrecancelable(String id) {
		Integer codigo = Integer.valueOf(id);
		return Objeto.setOf(41, 42, 43, 44).contains(codigo);
	}

	public String tipoLogros(String idTipo) {
		String tipo = "";
		tipo = "0025".equals(idTipo) ? "Logros en Pesos" : tipo;
		tipo = "0026".equals(idTipo) ? "Logros en Dólares" : tipo;
		tipo = "0027".equals(idTipo) ? "Logros en UVA" : tipo;
		tipo = "0028".equals(idTipo) ? "Logros en Dólares" : tipo;
		tipo = "0029".equals(idTipo) ? "Logros en Pesos" : tipo;
		tipo = "0030".equals(idTipo) ? "Logros en Dólares" : tipo;
		tipo = "0031".equals(idTipo) ? "Logros en UVA" : tipo;
		tipo = "0032".equals(idTipo) ? "Logros en UVA" : tipo;
		tipo = "0033".equals(idTipo) ? "Logros en Pesos" : tipo;
		tipo = "0034".equals(idTipo) ? "Logros en Dólares" : tipo;
		tipo = "0035".equals(idTipo) ? "Logros en UVA" : tipo;
		return tipo;
	}

	/* ========== METODOS ESTATICOS ========== */
	public static String tipo(String idTipo) {
		String tipo = "";
		tipo = "0001".equals(idTipo) ? "Transferible" : tipo;
		tipo = "0002".equals(idTipo) ? "Transferible con Intereses Periódicos" : tipo;
		tipo = "0003".equals(idTipo) ? "Intransferible" : tipo;
		tipo = "0004".equals(idTipo) ? "Intransferible con Intereses Periódicos" : tipo;
		tipo = "0005".equals(idTipo) ? "Intransferible con Seguro" : tipo;
		tipo = "0006".equals(idTipo) ? "Transferible Ajustable por CER" : tipo;
		tipo = "0007".equals(idTipo) ? "Intransferible Ajustable por CER" : tipo;
		tipo = "0008".equals(idTipo) ? "Transferible Precancelable" : tipo;
		tipo = "0010".equals(idTipo) ? "Intransferible Empleado" : tipo;
		tipo = "0011".equals(idTipo) ? "Tradicional" : tipo;
		tipo = "0012".equals(idTipo) ? "Intransferible con Intereses Periódicos" : tipo;
		tipo = "0013".equals(idTipo) ? "Empleado Intransferible" : tipo;
		tipo = "0017".equals(idTipo) ? "Intransferible Ajustable por UVA" : tipo;
		tipo = "0018".equals(idTipo) ? "Ajustable por UVA" : tipo;
		tipo = "0020".equals(idTipo) ? "Procrear joven ajustable por UVA" : tipo;
		tipo = "0021".equals(idTipo) ? "Empleado Ajustable por UVA" : tipo;
		tipo = "0025".equals(idTipo) ? "Logros $" : tipo;
		tipo = "0026".equals(idTipo) ? "Logros USD" : tipo;
		tipo = "0027".equals(idTipo) ? "Logros UVA" : tipo;
		tipo = "0028".equals(idTipo) ? "LOGROS U$D CARTERA GRAL SUC" : tipo;
		tipo = "0029".equals(idTipo) ? "LOGROS $ EMPLEADOS SUCURSAL" : tipo;
		tipo = "0030".equals(idTipo) ? "LOGROS U$D EMPLEADOS SUCURSAL" : tipo;
		tipo = "0031".equals(idTipo) ? "LOGROS UVA EMP SUCURSAL" : tipo;
		tipo = "0032".equals(idTipo) ? "LOGROS UVA CARTERA GRAL SUC" : tipo;
		tipo = "0033".equals(idTipo) ? "Logros $" : tipo;
		tipo = "0034".equals(idTipo) ? "Logros USD" : tipo;
		tipo = "0035".equals(idTipo) ? "Logros UVA" : tipo;
		tipo = "0040".equals(idTipo) ? "Plazo Fijo WEB" : tipo;
		tipo = "0041".equals(idTipo) ? "Ajustable por UVA Precancelable $" : tipo; // Sucursal
		tipo = "0042".equals(idTipo) ? "Ajustable por UVA Precancelable $" : tipo;
		tipo = "0043".equals(idTipo) ? "Empleado Ajustable por UVA Precancelable $" : tipo;
		tipo = "0044".equals(idTipo) ? "Ajustable por UVA Precancelable $" : tipo; // WEB
		return tipo;
	}

	public static String estado(String idEstado) {
		String estado = "";
		estado = "V".equals(idEstado) ? "Vigente" : estado;
		estado = "ACT".equals(idEstado) ? "Activado" : estado;
		estado = "ING".equals(idEstado) ? "Ingresado" : estado;
		estado = "REN".equals(idEstado) ? "Renovado" : estado;
		estado = "VEN".equals(idEstado) ? "Vencido" : estado;
		estado = "CAN".equals(idEstado) ? "Cancelado" : estado;
		estado = "ANU".equals(idEstado) ? "Anulado" : estado;
		return estado;
	}

	public static String estadoSolicitudCancelacionAnticipada(String idEstado) {
		String estado = "";
		estado = "ACT".equals(idEstado) ? "ACTIVA" : estado;
		estado = "ANU".equals(idEstado) ? "ANULADA" : estado;
		estado = "CON".equals(idEstado) ? "EN PROCESO" : estado;
		estado = "SOK".equals(idEstado) ? "CANCELACIÓN PROCESADA" : estado;
		estado = "SFA".equals(idEstado) ? "CANCELACIÓN NO PROCESADA" : estado;
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
		PlazoFijo other = (PlazoFijo) obj;
		if (id == null) {
			if (other.id() != null)
				return false;
		} else if (!id.equals(other.id()))
			return false;
		return true;
	}

	// TODO DLV-50929 nueva tasa 120 dias
	public BigDecimal tnaCancelacionAnt120() {
		ApiResponse response = plazosFijosGet();

		if (response == null)
			return null;

		for (Objeto objeto : response.objetos()) {
			return objeto.bigDecimal("tnaCancelacionAnt120");
		}
		return null;
	}

	public BigDecimal teaCancelacionAnt120() {
		ApiResponse response = plazosFijosGet();

		if (response == null)
			return null;

		for (Objeto objeto : response.objetos()) {
			return objeto.bigDecimal("teaCancelacionAnt120");
		}
		return null;
	}

	public String tnaCancelacionAnt120Formateada() {
		return Formateador.importe(tnaCancelacionAnt120());
	}

	public String teaCancelacionAnt120Formateada() {
		return Formateador.importe(teaCancelacionAnt120());
	}

//	public String fechaCancelacionAnt120() {
//		ApiResponse response = plazosFijosGet();
//
//		if (response == null)
//			return null;
//
//		for (Objeto objeto : response.objetos()) {
//			return objeto.string("fechaCancelacionAnt120");
//		}
//		return null;
//	}

	public Date fechaCancelacionAnt120() {
		ApiResponse response = plazosFijosGet();
		try {
			if (response == null)
				return null;

			for (Objeto objeto : response.objetos()) {
				return objeto.date("fechaCancelacionAnt120", "dd/MM/yyyy");
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

	public String fechaCancelacionAnt120(String formato) {
		ApiResponse response = plazosFijosGet();
		try {
			if (response == null)
				return null;

			for (Objeto objeto : response.objetos()) {
				return objeto.date("fechaCancelacionAnt120", "dd/MM/yyyy", formato, "");
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		return "";
	}

	public BigDecimal sellos() {
		ApiResponse response = plazosFijosGet();

		if (response == null)
			return null;

		for (Objeto objeto : response.objetos()) {
			return objeto.bigDecimal("sellos");
		}
		return null;
	}

}

//	ProductosGetConsolidada
//	{
//	    "muestraPaquete" : false,
//	    "tipoProducto" : "PFI",
//	    "numeroProducto" : "01808000110309047",
//	    "idProducto" : "60379505",
//	    "sucursal" : 18,
//	    "descSucursal" : "PARANA",
//	    "descEstado" : "VIGENTE",
//	    "estado" : "V",
//	    "fechaAlta" : "2015-01-12",
//	    "tipoTitularidad" : "T",
//	    "descTipoTitularidad" : "TITULAR",
//	    "tipoOperacion" : "0011",
//	    "adicionales" : true,
//	    "moneda" : "80",
//	    "descMoneda" : "PESOS",
//	    "fechaVencimiento" : "2015-03-13",
//	    "importe" : 200000.0,
//	    "estadoPF" : "CAN"
//	  }

//	PlazosFijosWindowsGet
//	[ {
//		  "operacion" : 3520209,
//		  "tipoCertificado" : "0011",
//		  "certificado" : "PLAZO FIJO TRADICIONAL - CANALES",
//		  "estado" : "CAN",
//		  "descEstado" : "CANCELADOS",
//		  "moneda" : 80,
//		  "tasaNominal" : 21.75,
//		  "tasaEfectiva" : 0.0,
//		  "monto" : 180000.00,
//		  "montoEstimado" : 3861.37,
//		  "renovacion" : "N",
//		  "fechaDesde" : "2018-01-22T00:00:00",
//		  "fechaFin" : "2018-02-27T00:00:00",
//		  "nroCertificado" : "00408000110129488",
//		  "oficina" : 4,
//		  "rol" : "-",
//		  "fechaCarga" : "2018-01-22T00:00:00",
//		  "plazo" : 36,
//		  "renueva" : "N",
//		  "nroRenovacion" : 0,
//		  "montoXImpuestos" : 0.0,
//		  "nroCuenta" : "400400013297479",
//		  "renuevaInteres" : "N",
//		  "cotizacion" : 1.0,
//		  "montoPesos" : 180000.0,
//		  "renovacionesPend" : null,
//		  "garantizado" : "S",
//		  "indiceAjusteUVA" : "",
//		  "valorUVA" : null,
//		  "fechaIndiceUVA" : null,
//		  "valorIndiceUVA" : 0.0,
//		  "planAhorro" : "",
//		  "cuota" : "",
//		"cancelacionAnticipada" : "N",
//		"tnaCancelacionAnt" : 0,
//		"teaCancelacionAnt" : 0,
//		"fechaDesdeCancelacionAnt" : null,
//		"fechaHastaCancelacionAnt" : null,
//		  "descripcionMoneda" : "PESOS"
//		} ]
