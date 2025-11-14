package ar.com.hipotecario.backend.servicio.api.cheques;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.util.ConstantesOB;

public class SimularDescuentoOB extends ApiObjeto {

	public Procesado procesado;
	public Salida salida;

    public class Procesado extends ApiObjeto {
        public String codigoError;
    }
    
    public class Result extends ApiObjeto {
        public String resultado;
    }
    
    public class Salida extends ApiObjeto {
    	public Resultado resultado;
    }
        
    //PARA LA RESPONSE
    public class Resultado {
        public ChequesSimulados chequesSimulados;
        public OperacionesSimuladas operacionesSimuladas;
        public int moneda;
        public int solicitudChequesCantidad;
        public String solicitudEstadoCodigo;
        public String solicitudEstadoDescripcion;
        public double solicitudImporteTotal;
        public int solicitudNumero;
        public String solicitudTasaUnica;
    }
    
    public class ChequesSimulados {
        public ChequeSimulado chequeSimulado;
    }
    
    public class ChequeSimulado {
        public int chequeSimuladoCantidad;
        public String chequeSimuladoEstadoCodigo;
        public String chequeSimuladoEstadoDescripcion;
    }
    
    public class OperacionesSimuladas {
        public OperacionSimulada operacionSimulada;
    }
    
    public class OperacionSimulada {
        public int cantidadADescontar;
        public double importeADescontar;
        public double importeALiquidar;
        public String linea;
        public int operacionSimuladaCantidad;
        public double operacionSimuladaGastoGlobalPorc;
        public double operacionSimuladaTNA;
        public int plazoPromedioPonderado;
    }

    //PARA EL REQUEST
    private static class AutorizacionParametro extends ApiObjeto {
        Integer modulo;
        Integer transaccion;
        String usuario;
    }
    
    // Clase que representa el "cliente"
    private static class Cliente extends ApiObjeto {
    	public String cuenta;
        public Integer tipoDocumento;
        public String numeroDocumento;
    }
    
    private static class Paginacion extends ApiObjeto {
    	public Integer cantidadPaginas;
        public Integer numeroPagina;
        public Integer totalElementos;
    }

    private static class Parametros extends ApiObjeto {
//        List<ChequesID> chequesID;
    	String[] chequesId;
        String sucursal;
    }
    
    private static class ChequesID extends ApiObjeto {
    	String echeqId;
    }
    
    public static SimularDescuentoOB post(ContextoOB contexto, String cuenta, String[] chequesID, String sucursal) {
        ApiRequest request = new ApiRequest("Simulacion cheque a Descontar", "cheques", "POST", "/v1/factoring/transacciones/descuento/simular", contexto);

        AutorizacionParametro autorizacionParametro = new AutorizacionParametro();
        autorizacionParametro.modulo = ConstantesOB.FACTORING_AUTORIZACION_PARAMETRO_MODULO;
        autorizacionParametro.transaccion = ConstantesOB.FACTORING_AUTORIZACION_PARAMETRO_TRANSACCION;
        autorizacionParametro.usuario = ConstantesOB.FACTORING_AUTORIZACION_PARAMETRO_USUARIO;
        
        Cliente cliente = new Cliente();
        cliente.tipoDocumento = ConstantesOB.FACTORING_CLIENTE_TIPO_DOCUMENTO;
        cliente.numeroDocumento = contexto.sesion().empresaOB.cuit.toString();
        cliente.cuenta = cuenta;
        
        Paginacion paginacion = new Paginacion();
        paginacion.cantidadPaginas = ConstantesOB.FACTORING_PAGINACION_CANTIDAD_PAGINAS;
        paginacion.numeroPagina = ConstantesOB.FACTORING_PAGINACION_NUMERO_PAGINA;
        paginacion.totalElementos = chequesID.length;
        
        Parametros parametros = new Parametros();
        parametros.chequesId = chequesID;
        
        parametros.sucursal = sucursal;
        
        request.body("autorizacionParametro", autorizacionParametro);
        request.body("cliente", cliente);
        request.body("paginacion", paginacion);
        request.body("parametros", parametros);
        
        request.cache = false;

        ApiResponse response = request.ejecutar();
        
        ApiException.throwIf(!response.http(200), request, response);

        return response.crear(SimularDescuentoOB.class);
    }

}
