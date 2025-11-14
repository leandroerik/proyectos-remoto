package ar.com.hipotecario.backend.servicio.api.cheques;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

import java.util.List;

public class ListadoChequesOB extends ApiObjetos<ListadoChequesOB.result> {

    public result result;

    public class result extends ApiObjeto {
        public respuesta respuesta;
        public List<cheques> cheques;
        public int total_cheques;


    }

    public class respuesta extends ApiObjeto {
        public String codigo;
        public String descripcion;
    }

   public class cheques extends ApiObjeto {
        public cuenta_emisora cuenta_emisora;

        public List<endoso> endosos;
        public List<cesion> cesiones;
        public List<rechazo> rechazos;
        public List<avalistas> avalistas;
        public String numero_chequera;
        public String cheque_id;
        public String cmc7;
        public String cheque_numero;
        public String estado;
        public emitido_a emitido_a;
        public tenencia tenencia;
        public double monto;
        public String fecha_pago;
        public String fecha_emision;
        public String cheque_tipo;
        public String cheque_caracter;
        public String cheque_modo;
        public String cheque_concepto;
        public String cheque_motivo_pago;
        public int agrupador_id;
        public String cod_visualizacion;
        public String fecha_ult_modif;
        public boolean fecha_pago_vencida;
        public boolean cheque_acordado;
        public boolean solicitando_acuerdo;
        public boolean re_presentar;
        public boolean repudio_endoso;
        public boolean certificado_emitido;
        public boolean onp;
        public boolean cesion_pendiente;
        public String cuit_solic_devol;
        public boolean es_ultimo_endosante;
        public String cbu_custodia;
        public boolean es_ultimo_mandante;

    }

   public class cuenta_emisora extends ApiObjeto {
        public String banco_codigo;
        public String banco_nombre;
        public String sucursal_codigo;
        public String sucursal_nombre;
        public String sucursal_domicilio;
        public String sucursal_cp;
        public String sucursal_provincia;
        public String emisor_cuit;
        public String emisor_razon_social;
        public String emisor_cbu;
        public String emisor_cuenta;
        public String emisor_subcuenta;
        public String emisor_moneda;
        public String emisor_domicilio;
        public String emisor_cp;

    }

   public class emitido_a extends ApiObjeto {
        public String beneficiario_documento_tipo;
        public String beneficiario_documento;
        public String beneficiario_nombre;


    }

   public class tenencia extends ApiObjeto {
        public String beneficiario_documento_tipo;
        public String beneficiario_documento;
        public String beneficiario_nombre;

    }

    public class endoso {
        public String benef_documento;
        public String benef_documento_tipo;
        public String benef_razon_social;
        public String emisor_documento;
        public String emisor_documento_tipo;
        public String emisor_razon_social;
        public String estado_endoso;
        public String fecha_hora;
        public String tipo_endoso;
    }

    public class avalistas{
        public String aval_entidad;
        public String aval_documento_tipo;
        public String aval_documento;
        public String aval_razon_social;
        public String aval_caracter;
        public String aval_sujeto_avalado;
        public String aval_fecha;
        public String aval_domicilio;
        public String aval_estado;
        public String aval_importe_avalado;
    }
    public class cesion {
        public String cesion_id;
        public String estado_cesion;
        public String cedente_documento_tipo;
        public String cedente_documento;
        public String cedente_nombre;
        public String cesionario_documento_tipo;
        public String cesionario_documento;
        public String cesionario_nombre;
        public String cesionario_domicilio;
        public String entidad_responsable_admision;
        public String cesion_motivo_repudio;
        public String fecha_emision_cesion;
        public String fecha_ult_modificacion_cesion;
        public String entidad_resp_registracion;
    }
    public class rechazo {
        public String fecha_hora;
        public String codigo_rechazo;
        public String motivo_rechazo;
    }



    public static ListadoChequesOB get (ContextoOB contexto,String cuit,String cantRegistros,String filter,String pagina){
        ApiRequest request = new ApiRequest("Listado cheque","cheques","GET","/v1/cheque/lista",contexto);
        request.query("$cant",cantRegistros);
        request.query("$filter",filter);
        request.query("$orderby","cheques.fecha_emision!");
        request.query("$pag",pagina==null? "1":pagina);
        request.query("$select","cheques.cheque_numero,cheques.agrupador_id,cheques.certificado_emitido,cheques.cheque_acordado,cheques.cheque_caracter,cheques.cheque_concepto," +
                "cheques.cheque_id,cheques.cheque_modo,cheques.cheque_motivo_pago,cheques.cheque_tipo,cheques.cmc7,cheques.cod_visualizacion,cheques.codigo_emision,cheques.estado," +
                "cheques.fecha_emision,cheques.fecha_pago,cheques.fecha_pago_vencida,cheques.monto,cheques.motivo_anulacion,cheques.motivo_repudio_emision,cheques.numero_chequera," +
                "cheques.re_presentar,cheques.repudio_endoso,cheques.solicitando_acuerdo,cheques.onp,cheques.cuenta_emisora.banco_codigo,cheques.cuenta_emisora.banco_nombre," +
                "cheques.cuenta_emisora.emisor_cbu,cheques.cuenta_emisora.emisor_cp,cheques.cuenta_emisora.emisor_cuenta,cheques.cuenta_emisora.emisor_cuit,cheques.cuenta_emisora.emisor_domicilio," +
                "cheques.cuenta_emisora.emisor_moneda,cheques.cuenta_emisora.emisor_razon_social,cheques.cuenta_emisora.emisor_subcuenta,cheques.cuenta_emisora.sucursal_codigo," +
                "cheques.cuenta_emisora.sucursal_cp,cheques.cuenta_emisora.sucursal_domicilio,cheques.cuenta_emisora.sucursal_nombre,cheques.cuenta_emisora.sucursal_provincia," +
                "cheques.emitido_a.beneficiario_documento,cheques.emitido_a.beneficiario_documento_tipo,cheques.emitido_a.beneficiario_nombre,cheques.tenencia.beneficiario_documento," +
                "cheques.tenencia.beneficiario_documento_tipo,cheques.tenencia.beneficiario_nombre,cheques.fecha_ult_modif,cheques.endosos,cheques.cuit_solic_devol,cheques.cbu_custodia," +
                "cheques.cesion_pendiente,cheques.cesiones,cheques.cuit_solic_devol,cheques.es_ultimo_endosante,cheques.rechazos,cheques.avalado,cheques.avalistas,cheques.mandato_neg,cheques.es_ultimo_mandante");
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(ListadoChequesOB.class);

    }

    public static ListadoChequesOB getById (ContextoOB contexto,String id,String cuit){
        ApiRequest request = new ApiRequest("Listado cheque","cheques","GET","/v1/cheque/lista",contexto);
        request.query("$cant","1");
        request.query("$filter","cuit eq __"+cuit+"__ and cheques.cheque_id eq __"+id+"__");
        request.query("$orderby","cheques.fecha_pago");
        request.query("$pag","1");
        request.query("$select","cheques.cheque_numero,cheques.agrupador_id,cheques.certificado_emitido,cheques.cheque_acordado,cheques.cheque_caracter,cheques.cheque_concepto," +
                "cheques.cheque_id,cheques.cheque_modo,cheques.cheque_motivo_pago,cheques.cheque_tipo,cheques.cmc7,cheques.cod_visualizacion,cheques.codigo_emision,cheques.estado," +
                "cheques.fecha_emision,cheques.fecha_pago,cheques.fecha_pago_vencida,cheques.monto,cheques.motivo_anulacion,cheques.motivo_repudio_emision,cheques.numero_chequera," +
                "cheques.re_presentar,cheques.repudio_endoso,cheques.solicitando_acuerdo,cheques.onp,cheques.cuenta_emisora.banco_codigo,cheques.cuenta_emisora.banco_nombre," +
                "cheques.cuenta_emisora.emisor_cbu,cheques.cuenta_emisora.emisor_cp,cheques.cuenta_emisora.emisor_cuenta,cheques.cuenta_emisora.emisor_cuit," +
                "cheques.cuenta_emisora.emisor_domicilio,cheques.cuenta_emisora.emisor_moneda,cheques.cuenta_emisora.emisor_razon_social," +
                "cheques.cuenta_emisora.emisor_subcuenta,cheques.cuenta_emisora.sucursal_codigo,cheques.cuenta_emisora.sucursal_cp,cheques.cuenta_emisora.sucursal_domicilio," +
                "cheques.cuenta_emisora.sucursal_nombre,cheques.cuenta_emisora.sucursal_provincia,cheques.emitido_a.beneficiario_documento,cheques.emitido_a.beneficiario_documento_tipo," +
                "cheques.emitido_a.beneficiario_nombre,cheques.tenencia.beneficiario_documento,cheques.tenencia.beneficiario_documento_tipo,cheques.tenencia.beneficiario_nombre," +
                "cheques.fecha_ult_modif,cheques.endosos,cheques.cuit_solic_devol,cheques.cbu_custodia,cheques.cesion_pendiente,cheques.cesiones,cheques.cuit_solic_devol,cheques.es_ultimo_endosante,cheques.rechazos,cheques.avalado,cheques.avalistas,cheques.mandato_neg,cheques.es_ultimo_mandante");
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(ListadoChequesOB.class);

    }

    public static ListadoChequesOB getByIdMandatario (ContextoOB contexto,String id,String cuit){
        ApiRequest request = new ApiRequest("Listado cheque","cheques","GET","/v1/cheque/lista",contexto);
        request.query("$cant","1");
        request.query("$filter","cuit eq __"+cuit+"__ and cheques.cheque_id eq __"+id+"__ and cheques.ult_mandatario eq __"+cuit+"__");
        request.query("$orderby","cheques.fecha_pago");
        request.query("$pag","1");
        request.query("$select","cheques.cheque_numero,cheques.agrupador_id,cheques.certificado_emitido,cheques.cheque_acordado,cheques.cheque_caracter,cheques.cheque_concepto," +
                "cheques.cheque_id,cheques.cheque_modo,cheques.cheque_motivo_pago,cheques.cheque_tipo,cheques.cmc7,cheques.cod_visualizacion,cheques.codigo_emision,cheques.estado," +
                "cheques.fecha_emision,cheques.fecha_pago,cheques.fecha_pago_vencida,cheques.monto,cheques.motivo_anulacion,cheques.motivo_repudio_emision,cheques.numero_chequera," +
                "cheques.re_presentar,cheques.repudio_endoso,cheques.solicitando_acuerdo,cheques.onp,cheques.cuenta_emisora.banco_codigo,cheques.cuenta_emisora.banco_nombre," +
                "cheques.cuenta_emisora.emisor_cbu,cheques.cuenta_emisora.emisor_cp,cheques.cuenta_emisora.emisor_cuenta,cheques.cuenta_emisora.emisor_cuit," +
                "cheques.cuenta_emisora.emisor_domicilio,cheques.cuenta_emisora.emisor_moneda,cheques.cuenta_emisora.emisor_razon_social," +
                "cheques.cuenta_emisora.emisor_subcuenta,cheques.cuenta_emisora.sucursal_codigo,cheques.cuenta_emisora.sucursal_cp,cheques.cuenta_emisora.sucursal_domicilio," +
                "cheques.cuenta_emisora.sucursal_nombre,cheques.cuenta_emisora.sucursal_provincia,cheques.emitido_a.beneficiario_documento,cheques.emitido_a.beneficiario_documento_tipo," +
                "cheques.emitido_a.beneficiario_nombre,cheques.tenencia.beneficiario_documento,cheques.tenencia.beneficiario_documento_tipo,cheques.tenencia.beneficiario_nombre," +
                "cheques.fecha_ult_modif,cheques.endosos,cheques.cuit_solic_devol,cheques.cbu_custodia,cheques.cesion_pendiente,cheques.cesiones,cheques.cuit_solic_devol,cheques.es_ultimo_endosante,cheques.rechazos,cheques.avalado,cheques.avalistas,cheques.mandato_neg,cheques.es_ultimo_mandante");
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(ListadoChequesOB.class);

    }

}
