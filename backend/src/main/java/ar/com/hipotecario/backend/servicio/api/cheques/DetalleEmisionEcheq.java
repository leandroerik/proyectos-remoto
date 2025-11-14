package ar.com.hipotecario.backend.servicio.api.cheques;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.util.DateUtils;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.utils.firmante;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.utils.numeracion;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.utils.chq_referencias_pago;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class DetalleEmisionEcheq extends ApiObjetos<DetalleEmisionEcheq.result> {

    public result result;
    public static class respuesta extends ApiObjeto {
        public String codigo;
        public String descripcion;
    }
    public static class cheques extends ApiObjeto{
        public String cheque_id;
        public String cheque_numero;
        public int agrupador_id;
        public LocalDateTime fecha_emision;
    }
    public static class result extends ApiObjeto
    {
        public DetalleEmisionEcheq.respuesta respuesta;
        public List<DetalleEmisionEcheq.cheques> cheques;
    }


    public static DetalleEmisionEcheq post(ContextoOB contexto, EcheqOB echeq, String cbuEmisor, String numeroSucursal, String cpSucursal){
        ApiRequest request = new ApiRequest("Emision_Echeq","cheques","POST","/v1/cheque",contexto);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSS");


        request.body("numero_chequera",String.valueOf(echeq.numeroChequera));
        request.body("emisor_cuit",contexto.sesion().empresaOB.cuit.toString());
        request.body("emisor_cbu",cbuEmisor);
        request.body("fecha_pago",echeq.fechaPago.format(formatter)+"Z");  //VENIR POR PARAMETRO
        request.body("monto",echeq.monto);
        request.body("beneficiario_documento_tipo",echeq.tipoDocumentoBeneficiario);
        request.body("beneficiario_documento",echeq.documentoBeneficiario);

        Optional.ofNullable(echeq.emailBeneficiario).ifPresentOrElse(email->{
            request.query("enviarMail",true);
            request.body("beneficiario_email",echeq.emailBeneficiario);
        },()->{
            request.query("enviarMail",false);
        });
        request.body("cheque_version","001");
        request.body("cheque_instrumento_tipo","ECHEQ");
        request.body("cheque_caracter",echeq.aLaOrden?"a la orden":"no a la orden");
        request.body("cheque_motivo_pago",echeq.motivoPago);
        request.body("cheque_concepto",echeq.concepto);
        request.body("cheque_cruzado",echeq.cruzado);
        request.body("cheque_tipo",echeq.tipo);
        firmante firmante = new firmante();
        firmante.documento_tipo = "CUIL";
        firmante.documento = contexto.sesion().usuarioOB.cuil.toString();
        List<firmante> listaFirmantes = new ArrayList<>();
        listaFirmantes.add(firmante);
        numeracion numeracion = new numeracion();
        numeracion.cheque_numero="";
        numeracion.cmc7="";
        List<numeracion> listaNumeracion = new ArrayList<>();
        listaNumeracion.add(numeracion);
        chq_referencias_pago chq_referencias_pago = new chq_referencias_pago();
        chq_referencias_pago.referencia="";
        chq_referencias_pago.valor="";
        List<chq_referencias_pago> chqReferenciasPagoList = new ArrayList<>();
        chqReferenciasPagoList.add(chq_referencias_pago);
        request.body("numeracion",listaNumeracion);
        request.body("chq_referencias_pago",chqReferenciasPagoList);
        request.body("firmantes",listaFirmantes);
        request.body("cta_banco",echeq.cuentaOrigen);
        request.body("moneda",80);
        request.body("id_ente_cobis",contexto.sesion().empresaOB.idCobis);
        request.body("sucursal",numeroSucursal);
        request.body("cp_sucursal",cpSucursal);
        request.body("beneficiario_nombre_apellidos",echeq.razonSocialBeneficiario);
        request.body("beneficiario_cuit",echeq.documentoBeneficiario);
        request.body("emisor_nombre_apellidos",contexto.sesion().empresaOB.razonSocial);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(DetalleEmisionEcheq.class);
    }
}
