package ar.com.hipotecario.backend.servicio.api.recaudaciones;

import java.util.ArrayList;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

public class ConveniosSugeridosEcheqOB extends ApiObjetos<ConveniosSugeridosEcheqOB> {

	public Number limit;
	public Number pag;
	public Number cantTotal;

    public ArrayList<ConsultaEcheqReca> listaEcheqs;

    public static class ConsultaEcheqReca extends ApiObjeto {

        public CuentaEmisora cuentaEmisora;
        public String numeroChequera;
        public String chequeId;
        public String cmc7;
        public String chequeNumero;
        public String estado;
        public EmitidoA emitidoA;
        public Tenencia tenencia;
        public String monto;
        public String fechaPago;
        public String fechaEmision;
        public String chequeTipo;
        public String chequeCaracter;
        public String chequeModo;
        public String chequeConcepto;
        public String chequeMotivoPago;
        public Integer agrupadorId;
        public String codVisualizacion;
        public String fechaUltModif;
        public Boolean fechaPagoVencida;
        public Boolean chequeAcordado;
        public Boolean solicitandoAcuerdo;
        public Boolean rePresentar;
        public Boolean repudioEndoso;
        public Boolean certificadoEmitido;
        public ArrayList<Endoso> endosos;
        public Boolean onp;
        
        public static class CuentaEmisora extends ApiObjeto {
            public String bancoCodigo;
            public String bancoNombre;
            public String sucursalCodigo;
            public String sucursalNombre;
            public String sucursalDomicilio;
            public String sucursalCp;
            public String sucursalProvincia;
            public String emisorCuit;
            public String emisorRazonSocial;
            public String emisorCbu;
            public String emisorCuenta;
            public String emisorMoneda;
            public String emisorDomicilio;
        }
        
        public static class EmitidoA extends ApiObjeto {
            public String beneficiarioDocumentoTipo;
            public String beneficiarioDocumento;
            public String beneficiarioNombre;
        }
        
        public static class Tenencia extends ApiObjeto {
        	public String beneficiarioDocumentoTipo;
            public String beneficiarioDocumento;
            public String beneficiarioNombre;
        }
        
        public static class Endoso extends ApiObjeto {
            public String benefDocumento;
            public String benefDocumentoTipo;
            public String benefRazonSocial;
            public String emisorDocumento;
            public String emisorDocumentoTipo;
            public String emisorRazonSocial;
            public String estadoEndoso;
            public String fechaHora;
            public String tipoEndoso;
        }
    }

    public static ConveniosSugeridosEcheqOB get(ContextoOB contexto, String convenio, String tipo, String numeroCheque, String estado, String razonSocial, String fechaDesde, String fechaHasta, String pagina, String limite) {
        ApiRequest request = new ApiRequest("ListaEcheqsRelaNominaConvenio", "recaudaciones", "GET", "/v1/convenios/echeqs/sugeridos", contexto);
        request.query("cuitEmpresa", contexto.sesion().empresaOB.cuit);
        request.query("idEmpresa", contexto.sesion().empresaOB.idCobis);
        request.query("convenio", convenio);
        request.query("estado", estado);
        request.query("numeroCheque", numeroCheque);
        request.query("cuitUsuario", String.valueOf(contexto.sesion().cuil));
        request.query("tipo", tipo);
        request.query("razonSocial", razonSocial);
        request.query("fechaDesde", fechaDesde);
        request.query("fechaHasta", fechaHasta);
        request.query("pag", pagina);
        request.query("limit", limite);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);

        return response.crear(ConveniosSugeridosEcheqOB.class);
    }
}
