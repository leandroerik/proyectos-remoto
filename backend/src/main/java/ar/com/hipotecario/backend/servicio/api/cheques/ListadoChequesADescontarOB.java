package ar.com.hipotecario.backend.servicio.api.cheques;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.LogOB;
import ar.com.hipotecario.canal.officebanking.util.ConstantesOB;

import java.util.List;

public class ListadoChequesADescontarOB extends ApiObjetos<ListadoChequesADescontarOB> {

	public Cliente cliente;
	public Paginacion paginacion;
    public Resultado resultado;

    // Clase que representa el "cliente"
    public class Cliente extends ApiObjeto {
        public Integer tipoDocumento;
        public String numeroDocumento;
    }

    // Clase que representa la "paginacion"
    public class Paginacion extends ApiObjeto {
        public Integer numeroPagina;
        public String ordenamiento;
        public Integer totalElementos;
    }

    // Clase que representa "resultado" que contiene la lista de cheques
    public class Resultado extends ApiObjeto {
        public List<Cheque> cheques;
    }
//
//    // Clase que representa cada cheque
    public class Cheque extends ApiObjeto {
        public int chequeId;
        public int moneda;
        public String chequeCMC7;
        public int chequeBancoGiradoCodigo;
        public String chequeBancoGiradoDescripcion;
        public int chequeSucursalGirada;
        public int chequeCodigoPostalGirado;
        public String chequeNumero;
        public String chequeCuentaGirada;
        public double chequeImporte;
        public String chequeFechaPresentacion;
        public int libradorTipoDocumento;
        public String libradorNumeroDocumento;
        public String libradorRazonSocial;
        public String linea;
    }

    public static ListadoChequesADescontarOB get(ContextoOB contexto, String pagina, Integer moneda, String ordenamiento, Integer totalElementos, String fechaDesde, String fechaHasta) {
        ApiRequest request = new ApiRequest("Listado cheque a Descontar", "cheques", "GET", "/v1/factoring/consultas/cheques/a-descontar", contexto);

        request.query("autorizacionParametro.modulo", ConstantesOB.FACTORING_AUTORIZACION_PARAMETRO_MODULO);
        request.query("autorizacionParametro.transaccion", ConstantesOB.FACTORING_AUTORIZACION_PARAMETRO_TRANSACCION);
        request.query("autorizacionParametro.usuario", ConstantesOB.FACTORING_AUTORIZACION_PARAMETRO_USUARIO);

        request.query("cliente.numeroDocumento", contexto.sesion().empresaOB.cuit.toString());
        request.query("cliente.tipoDocumento", ConstantesOB.FACTORING_CLIENTE_TIPO_DOCUMENTO);

        request.query("paginacion.numeroPagina", pagina);
        request.query("paginacion.ordenamiento", ordenamiento);
        request.query("paginacion.totalElementos", totalElementos);

        request.query("parametros.fechaSolicitadaDesde", fechaDesde);
    	request.query("parametros.fechaSolicitadaHasta", fechaHasta);
        
        request.query("parametros.moneda", moneda);
        request.query("parametros.productoTipo", ConstantesOB.FACTORING_PARAMETROS_PRODUCTO_TIPO);

        ApiResponse response = request.ejecutar();
        LogOB.evento(contexto, "API RESPONSE", response.body);
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(ListadoChequesADescontarOB.class);
    }

}
