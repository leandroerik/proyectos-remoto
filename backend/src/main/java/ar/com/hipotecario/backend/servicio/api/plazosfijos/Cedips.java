package ar.com.hipotecario.backend.servicio.api.plazosfijos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.Parametros;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.Cedips.Cedip;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.Cedips.Cedip.CedipNuevo;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.Cedips.Cedip.ResponsePostTransmision;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.CedipAccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.CedipOB;

public class Cedips extends ApiObjetos<Cedip> {

	/* ========== ATRIBUTOS ========== */
	public class Cedip extends ApiObjeto {
	    public String cedipId;
	    public String tipoCertificado;
	    public String numeroCertificado;
	    public String codigoBanco;
	    public Date fechaEmision;
	    public String lugarEmision;
	    public String codigoRuta;
	    public String tipoMoneda; //esta en la docu pero no en el swagger
	    public Integer plazo;
	    public BigDecimal tna;
	    public BigDecimal tea;
	    public BigDecimal cotizacionUva;
	    public Date fechaVencimiento;
	    public String estado;
	    public boolean fraccionado;
	    public Integer fraccionNumero;
	    public BigDecimal montoDepositado;
	    public String montoDepositadoLetras;
	    public BigDecimal montoDepositadoIntereses;
	    public BigDecimal montoIntereses;
	    public BigDecimal montoRetencion;
	    public BigDecimal montoCobrar;
	    public Date fechaDeposito; // esta solo en el swagger
	    public Date fechaPresentacion; //esta solo en el swagger
	    public String tenedorTipoDocumento;
	    public String tenedorDocumento;
	    //public String tenedorCuit; estaba en la docu no en el swagger
	    //public String tenedorCuit;
	    public String tenedorNombre;
	    public String cbuAcreditar; //entiendo que esta tambien es la de core ce_cbu_acred
	    public String tipoAcreditacion;
	    public String fechaAcreditacionRechazo;
	    
	    public List<Transmision> transmisiones;
	    public List<Ajuste> ajustes;
	    public List<Leyenda> leyendas;
	    public List<Cotitular> cotitulares;

	    public Boolean representacion;
	    public String motivoRechazo;
	    public String tipoRenovacion;
	    public Boolean embargo;
	    public Boolean pagoJudicial;
	    
	    public static class Transmision {
	        private String fecha;
	        private String estado;
	        private String tipo;
	        private String cuitTransmisor;
	        private String razonSocialTransmisor;
	        private String cuitBeneficiario;
	        private String razonSocialBeneficiario;
	        private BigDecimal monto;
			
	        public String getEstado() {
				return estado;
			}

			public String getCuitBeneficiario() {
				return cuitBeneficiario;
			}

			public BigDecimal getMonto() {
				return monto;
			}
	    }
	    
	    public static class Ajuste {
	    	
	    }
	    
	    public static class Leyenda {
	    	private Integer codigo;
	    	private String leyenda;
	    }
	    
	    public static class Cotitular {
	    
	    }

		public static class RespuestaOk extends ApiObjeto {
			public Boolean ok;
		}
		
		public static class ResponsePostTransmision extends ApiObjeto {
			String status;
			String descripcion;
			String codigo;
			String mensajeAlUsuario;
			String masInformacion;
			String mensajeAlDesarrollador;
			String detalle;
		}

		public List<Transmision> getTransmisiones() {
			return transmisiones;
		}
		
		public static class CedipNuevo extends ApiObjeto {
			  private BigDecimal interesEstimado;
			  private BigDecimal totalInteresEstimado;
			  private BigDecimal impuestos;
			  private Date fechaPagoIntereses;
			  private String diaDePago;
			  private Date fechaVencimiento;
			  private Integer cuotas;
			  private Date fechaActual;
			  private String tipoOperacion;
			  private String producto;
			  private BigDecimal capital;
			  private Double tasa;
			  private Integer plazo;
			  private String cuenta;
			  private BigDecimal monto;
			  private Integer moneda;
			  private Integer idOperacion;
			  private String nroPlazoFijo;
			  private String cubiertoPorGarantia;
			  private String cancelacionAnticipada;
			  private Double tasaCancelacionAnt;
			  private Double tnaCancelacionAnt;
			  private Double teaCancelacionAnt;
			  private Date fechaDesdeCancelacionAnt;
			  private Date fechaHastaCancelacionAnt;
			  private Date tasaCancelacionLeliq120;
			  private Date tnaCancelacionLeliq120;
			  private Date teaCancelacionLeliq120;
			  private String fechaCancelacionLeliq120;
		}

	}


	/* ========== SERVICIOS ========== */	
	public static Cedips get(Contexto contexto, String cuit) {
		ApiRequest request = new ApiRequest("CedipsGet", "plazosfijos", "GET", "/v1/cedips/{cuit}", contexto);
		
		Parametros parametros = contexto.parametros;
		
		String cantPag = parametros.string("cantPag", null);
		String destino = parametros.string("destino", null);
		String filtros = parametros.string("filtros");
		String pag = parametros.string("pag", null);
		String orderBy = parametros.string("orderBy", null);
		//String estado = parametros.string("estado");
		//String filtros = "$documento_tenedor$ [eq] __30612929455__";
		//String filtros = "$estado$ [eq] __ACTIVO__";
		
		request.path("cuit", cuit);
		if (cantPag != null)
			request.query("cantPag", cantPag);
		if (destino != null) 
			request.query("destino", destino);			
		if (pag != null)
			request.query("pag", pag);
		if (orderBy != null)
			request.query("orderBy", orderBy);
		request.query("filtros", filtros);
		//request.query("estado", estado);
		//request.query("orderBy", orderBy);
		
		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Cedips.class);
	}
	
	public static Cedips getRecibidos(Contexto contexto, String cuit) {
		//String filtro = "filtros=%2522estado%2522%2520%255Beq%255D%2520__ACTIVO-PENDIENTE__";
		String url = "/v1/cedips/"+ cuit;
		
//        url += (!url.contains("?") ? "?" : "&") + "pag=" + contexto.;
//        //url += (!url.contains("?") ? "?" : "&") + "pag=" + 2;
//        url += (!url.contains("?") ? "?" : "&") + "orderBy=fecha_emision%20!";
		
		ApiRequest request = new ApiRequest("CedipsGetRecibidos", "plazosfijos", "GET", url, contexto);
		
		Parametros parametros = contexto.parametros;
		
		String cantPag = parametros.string("cantPag", null);
		String destino = parametros.string("destino", null);
		String filtros = parametros.string("filtros");
		String pag = parametros.string("pag", null);
		//String estado = parametros.string("estado");
		//String filtros = "$documento_tenedor$ [eq] __30612929455__";
		//String filtros = "$estado$ [eq] __ACTIVO__";
		
		request.path("cuit", cuit);
		if (cantPag != null)
			request.query("cantPag", cantPag);
		if (destino != null) 
			request.query("destino", destino);			
		if (pag != null)
			request.query("pag", pag);
		request.query("filtros", filtros);
		//request.query("estado", estado);
		//request.query("orderBy", orderBy);
		
		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Cedips.class);
	}
	
	public static Cedip getDetalleCedip(Contexto contexto, String cedipId, String cuit, Integer fraccion) {
		ApiRequest request = new ApiRequest("CedipGetDetalle", "plazosfijos", "GET", "/v1/cedips/{cedipId}/{cuit}/{fraccion}", contexto);
		
		Parametros parametros = contexto.parametros;
		
		String destino = parametros.string("destino", null);

		request.path("cedipId", cedipId);
		request.path("cuit", cuit);
		request.path("fraccion", fraccion.toString());
		
		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Cedip.class);
	}

	public static CedipNuevo post(Contexto contexto, CedipOB cedip) {
		ApiRequest request = new ApiRequest("CedipPost", "plazosfijos", "POST", "/v1/plazoFijos", contexto);
        
		Objeto objeto = new Objeto();
        objeto.set("canal", cedip.canal);
        objeto.set("cedip", cedip.cedip);
        objeto.set("cedipCBUAcred", cedip.cedipCBUAcred);
        objeto.set("cedipTipoAcred", cedip.cedipTipoAcred);
        objeto.set("capInteres", cedip.capInteres);
        objeto.set("cuenta", cedip.cuenta);
        objeto.set("idPlanAhorro", cedip.idPlanAhorro);
        objeto.set("idcliente", cedip.idcliente);
        objeto.set("moneda", cedip.monedaCedip);
        objeto.set("monto", cedip.monto);
        objeto.set("nroOperacion", cedip.nroOperacion);
        objeto.set("periodo", cedip.periodo);
        objeto.set("plazo", cedip.plazo);
        objeto.set("renova", cedip.renova);
        objeto.set("reverso", cedip.reverso);
        objeto.set("tipoCuenta", cedip.tipoCuenta);
        objeto.set("tipoOperacion", cedip.tipoOperacion);
        objeto.set("usuarioAlta", cedip.usuarioAlta);
		
        request.body(objeto);

		ApiResponse response = request.ejecutar();
		
		// Validar el error 404
		String mensajeError = "0";
		if(response.http(404) && response.body.contains("codigo")) {
			JSONObject jsonObject = new JSONObject(response.body);
			mensajeError = (String) jsonObject.get("mensajeAlUsuario");
		}
		
		ApiException.throwIf(mensajeError, !response.http(200), request, response);
		return response.crear(CedipNuevo.class);
	}
	
	public static ResponsePostTransmision postTransmitir(Contexto contexto, CedipAccionesOB cedipA) {
		ApiRequest request = new ApiRequest("CedipTransmision", "plazosfijos", "POST", "/v1/cedip/solicitar", contexto);
		
		Objeto objeto = new Objeto();
		objeto.set("cedipId", cedipA.cedipId);
        objeto.set("codigoBanco", cedipA.codigoBanco);
        objeto.set("fraccionId", cedipA.fraccionId);
        objeto.set("fraccionado", cedipA.fraccionado);
        objeto.set("tenedorDocumento", cedipA.tenedorDocumento);
        objeto.set("tenedorTipoDocumento", cedipA.tenedorTipoDocumento);
        ArrayList<Objeto> firmantesArray = new ArrayList();
        ArrayList<Objeto> transmisionArray = new ArrayList();
        Objeto firmanteObject = new Objeto();
        firmanteObject.set("documentoFirmante", "30612929455");
        firmanteObject.set("tipoDocumentoFirmante", "CUIT");
        firmantesArray.add(firmanteObject);
        Objeto transmisionObject = new Objeto();
        transmisionObject.set("beneficiarioDocumento", cedipA.beneficiarioDocumento);
        transmisionObject.set("beneficiarioNombre", cedipA.beneficiarioNombre);
        transmisionObject.set("beneficiarioTipoDocumento",cedipA.beneficiarioTipoDocumento);
        transmisionObject.set("monto", cedipA.montoCedip);
        transmisionObject.set("tipoTransmision", cedipA.tipoTransmision);
        transmisionArray.add(transmisionObject);
        objeto.set("firmantes", firmantesArray);
        objeto.set("transmisiones", transmisionArray);

		request.body(objeto);

		ApiResponse response = request.ejecutar();
		//ApiException.throwIf(!response.http(200), request, response);
		return response.crear(ResponsePostTransmision.class);
	}
	
	public static ResponsePostTransmision postAdmitir(Contexto contexto, Parametros parametros) {
		ApiRequest request = new ApiRequest("CedipAdmision", "plazosfijos", "POST", "/v1/cedip/admitir", contexto);
		request.body(parametros);

		ApiResponse response = request.ejecutar();
		//ApiException.throwIf(!response.http(200), request, response);
		return response.crear(ResponsePostTransmision.class);
	}
	
	public static ResponsePostTransmision postRepudiar(Contexto contexto, Parametros parametros) {
		ApiRequest request = new ApiRequest("CedipRechazo", "plazosfijos", "POST", "/v1/cedip/repudiar", contexto);
		request.body(parametros);

		ApiResponse response = request.ejecutar();
		//ApiException.throwIf(!response.http(200), request, response);
		return response.crear(ResponsePostTransmision.class);
	}
	
	public static ResponsePostTransmision postAnularTransmision(Contexto contexto, CedipAccionesOB cedipA) {
		ApiRequest request = new ApiRequest("CedipAnularTransmision", "plazosfijos", "POST", "/v1/cedip/anular", contexto);
		
		Objeto objeto = new Objeto();
		objeto.set("cedipId", cedipA.cedipId);
        objeto.set("codigoBanco", cedipA.codigoBanco);
        objeto.set("fraccionId", cedipA.fraccionId);
        objeto.set("ejecutorDocumento", cedipA.ejecutorDocumento);
        objeto.set("ejecutorTipoDocumento", cedipA.ejecutorTipoDocumento);
        ArrayList<Objeto> firmantesArray = new ArrayList();
        Objeto firmanteObject = new Objeto();
        firmanteObject.set("documentoFirmante", cedipA.ejecutorDocumento);
        firmanteObject.set("tipoDocumentoFirmante", "CUIT");
        firmantesArray.add(firmanteObject);
        objeto.set("firmantes", firmantesArray);
		
		request.body(objeto);

		ApiResponse response = request.ejecutar();
		//ApiException.throwIf(!response.http(200), request, response);
		return response.crear(ResponsePostTransmision.class);
	}
	

	public static ResponsePostTransmision putModificarAcreditacionCbu(Contexto contexto, CedipAccionesOB cedipA) {
		ApiRequest request = new ApiRequest("CedipModificarAcreditarCbu", "plazosfijos", "PUT", "/v1/acreditacion/modificar", contexto);

		Objeto objeto = new Objeto();
		objeto.set("cbuAcreditar", cedipA.cbuAcreditar);
		objeto.set("cedipId", cedipA.cedipId);
        objeto.set("codigoBanco", cedipA.codigoBanco);
        objeto.set("fechaVencimiento", cedipA.fechaVencimiento);
        objeto.set("fraccionId", cedipA.fraccionId);
        objeto.set("tenedorDocumento", cedipA.tenedorDocumento);
        objeto.set("tenedorTipoDocumento", cedipA.tenedorTipoDocumento);
        objeto.set("tipoAcreditacion", cedipA.tipoAcreditacion);
        ArrayList<Objeto> firmantesArray = new ArrayList();
        Objeto firmanteObject = new Objeto();
        firmanteObject.set("documentoFirmante", cedipA.documentoFirmante);
        firmanteObject.set("tipoDocumentoFirmante", cedipA.tipoDocumentoFirmante);
        firmantesArray.add(firmanteObject);
        objeto.set("firmantes", firmantesArray);
        Objeto montoObject = new Objeto();
        montoObject.set("montoACobrar", cedipA.montoACobrar);
        montoObject.set("montoDepositado", cedipA.montoDepositado);
        montoObject.set("montoIntereses", cedipA.montoIntereses);
        montoObject.set("montoRetencion", cedipA.montoRetencion);
        objeto.set("montosAdmitirCore", montoObject);
		
		request.body(objeto);

		ApiResponse response = request.ejecutar();
		return response.crear(ResponsePostTransmision.class);
	}
	
	public static ResponsePostTransmision postDepositarCedip(Contexto contexto, CedipAccionesOB cedipA) {
		ApiRequest request = new ApiRequest("CedipDepositar", "plazosfijos", "POST", "/v1/acreditacion/depositar", contexto);

		Objeto objeto = new Objeto();
		objeto.set("cbuAcreditar", cedipA.cbuAcreditar);
		objeto.set("cedipId", cedipA.cedipId);
        objeto.set("codigoBanco", cedipA.codigoBanco);
        ArrayList<Objeto> firmantesArray = new ArrayList();
        Objeto firmanteObject = new Objeto();
        firmanteObject.set("documentoFirmante", cedipA.documentoFirmante);
        firmanteObject.set("tipoDocumentoFirmante", cedipA.tipoDocumentoFirmante);
        firmantesArray.add(firmanteObject);
        objeto.set("firmantes", firmantesArray);
        objeto.set("fraccionId", cedipA.fraccionId);
        objeto.set("tenedorDocumento", cedipA.tenedorDocumento);
        objeto.set("tenedorTipoDocumento", cedipA.tenedorTipoDocumento);

        request.body(objeto);
        
		ApiResponse response = request.ejecutar();
		return response.crear(ResponsePostTransmision.class);
	}

	/* ========== TEST ========== */
//	public static void main(String[] args) {
//		String test = "getPF";
//		Contexto contexto = contexto("HB", "homologacion");
//
//		if ("get".equals(test)) {
//			Cedip datos = get(contexto, "135706", "00408000110233518");
//			System.out.println(datos.get(0).fechaCarga());
//			imprimirResultado(contexto, datos);
//		}
//
//		if ("post".equals(test)) {
//			RequestPost requestPost = new RequestPost();
//			requestPost.canal = "3";
//			requestPost.capInteres = "";
//			requestPost.cuenta = "";
//			requestPost.idPlanAhorro = 1;
//			requestPost.idcliente = 1;
//			requestPost.moneda = 80;
//			requestPost.monto = new BigDecimal(5);
//			requestPost.nroOperacion = null;
//			requestPost.periodo = null;
//			requestPost.plazo = null;
//			requestPost.renova = "N";
//			requestPost.reverso = null;
//			requestPost.tipoCuenta = "AHO";
//			requestPost.tipoOperacion = "1";
//			requestPost.usuarioAlta = "1";
//			post(contexto, requestPost);
//		}
//
//		if ("historicos".equals(test)) {
//			Cedip datos = getHistoricos(contexto, "135706", "00408000110233518");
//			imprimirResultado(contexto, datos);
//		}
//
//		if ("getPF".equals(test)) {
//			PlazoFijoPF datos = getPf(contexto, "04308000420000012");
//			imprimirResultado(contexto, datos);
//		}
//	}
}
