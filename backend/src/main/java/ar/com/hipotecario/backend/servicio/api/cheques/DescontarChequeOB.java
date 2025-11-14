package ar.com.hipotecario.backend.servicio.api.cheques;

import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.util.ConstantesOB;

public class DescontarChequeOB extends ApiObjeto {

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
        public SolicitudDescuento solicitud;
    }
    
    public class SolicitudDescuento {
        public int solicitudNumero;
        public List<OperacionDescuento> operaciones;
    }
    
    public class OperacionDescuento {
        public String operacionNumero;
        public String operacionCodigoEstado;
        public String operacionDescripcionEstado;
        public int operacionImporteTotal;
        public int operacionChequesCantidad;
        public String linea;
    }

    //PARA EL REQUEST
    private static class AutorizacionParametro extends ApiObjeto {
        Integer modulo;
        Integer transaccion;
        String usuario;
    }
    
    // Clase que representa el "cliente"
    private static class Cliente extends ApiObjeto {
        public Integer tipoDocumento;
        public String numeroDocumento;
    }
    
    private static class Autorizantes extends ApiObjeto {
        String numeroDocumento;
        Integer tipoDocumento;
    }

    private static class Parametros extends ApiObjeto {
    	List<Autorizantes> autorizantes;
        String modificarEstadoCodigo;
        String solicitudNumero;
    }
    
    public static DescontarChequeOB post(ContextoOB contexto, String modificarEstadoCodigo, String solicitudNumero) {
        ApiRequest request = new ApiRequest("Simulacion cheque a Descontar", "cheques", "PATCH", "/v1/factoring/transacciones/solicitud", contexto);

        AutorizacionParametro autorizacionParametro = new AutorizacionParametro();
        autorizacionParametro.modulo = ConstantesOB.FACTORING_AUTORIZACION_PARAMETRO_MODULO;
        autorizacionParametro.transaccion = ConstantesOB.FACTORING_AUTORIZACION_PARAMETRO_TRANSACCION;
        autorizacionParametro.usuario = ConstantesOB.FACTORING_AUTORIZACION_PARAMETRO_USUARIO;
        
        List<Autorizantes> autorizantesDescuento = new ArrayList<Autorizantes>();
        Autorizantes autorizante = new Autorizantes();
    	autorizante.numeroDocumento = contexto.sesion().empresaOB.cuit.toString();
    	autorizante.tipoDocumento = ConstantesOB.FACTORING_CLIENTE_TIPO_DOCUMENTO;
    	autorizantesDescuento.add(autorizante);
        
        Cliente cliente = new Cliente();
        cliente.tipoDocumento = ConstantesOB.FACTORING_CLIENTE_TIPO_DOCUMENTO;
        cliente.numeroDocumento = contexto.sesion().empresaOB.cuit.toString();
        
        Parametros parametros = new Parametros();
        parametros.autorizantes = autorizantesDescuento;
        parametros.modificarEstadoCodigo = modificarEstadoCodigo;
        parametros.solicitudNumero = solicitudNumero;
        
        request.body("autorizacionParametro", autorizacionParametro);
        request.body("cliente", cliente);
        request.body("parametros", parametros);
        
        request.cache = false;

        ApiResponse response = request.ejecutar();
        
        ApiException.throwIf(!response.http(200), request, response);

        return response.crear(DescontarChequeOB.class);
    }

}
