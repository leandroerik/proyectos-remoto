package ar.com.hipotecario.backend.servicio.api.empresas;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BeneficiariosOB  extends ApiObjeto {


    @SerializedName("beneficiarios")
    public Beneficiarios beneficiarios;


    public static class Beneficiarios {
        @SerializedName("beneficiario")
        public ArrayList<Beneficiario> beneficiario;
    }

    public static class Beneficiario {
        public Integer altFormEntrega;
        public Integer beneficiario;
        public String cbuCtaExt;
        public String cbuCtaInt;
        public String celular;
        public Integer codMonCtaExt;
        public Integer codMonCtaInt;
        public String codigoPostal;
        public String descAltFormEntrega;
        public String descCodMonCtaExt;
        public String descCodMonCtaInt;
        public String descMedComBenef;
        public String descNroSubConv;
        public String descSistCtaExt;
        public String descSistCtaInt;
        public String descSucDest;
        public String descTipoDoc;
        public String direccion;
        public Date fecAlta;
        public Date fecBaja;
        public BigDecimal impMaxPago;
        public String mail;
        public Integer medComBeneficiario;
        public String nomBeneficiario;
        public String nomContacto;
        public String nomCuentaExt;
        public String nomCuentaInt;
        public Integer nroBcoCtaExt;
        public Integer nroBcoCtaInt;
        public String nroBeneficiarioSist;
        public Integer nroConv;
        public Long nroCuilCuit;
        public Integer nroSubConv;
        public String origen;
        public String simboloMonCtaExt;
        public String simboloMonCtaInt;
        public Integer sistCtaExt;
        public Integer sistCtaInt;
        public String sucDest;
        public String telefono;
        public String tipoCtaExt;
        public String tipoCtaInt;
        public Integer tipoDoc;


    }


    public static BeneficiariosOB get(ContextoOB contexto, Integer nroConv, Integer nroSubConv)  {
    try {


        ApiRequest request = new ApiRequest("API-Empresas_ConsultaBenficiariosPP", "empresas", "GET", "/v1/sat/beneficiariosPP", contexto);
        request.header("Content-Type", "application/json");

        SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String dateString = timestamp.format(new Date());


        request.query("cantRegMostrar", "500");
        request.query("codigoEntidad", "044");
        request.query("fechaHoraConsulta", dateString);
        request.query("frontEnd", "2");
        request.query("frontEndID", "OB");
        request.query("id", contexto.sesion().empresaOB.idCobis+"-"+ contexto.sesion().empresaOB.razonSocial);
        request.query("idUsrLog", contexto.sesion().empresaOB.idCobis);
        request.query("maxReg", "500");
        request.query("nroConv", nroConv);
        request.query("nroPagina", "1");
        request.query("nroSubConv", nroSubConv);
        request.query("tipoID", "1");


        ApiResponse resultado = request.ejecutar();
        ApiException.throwIf(!resultado.http(200, 204, 404), request, resultado);
        return resultado.crear(BeneficiariosOB.class);
    }
    catch (Exception e ){
        System.out.println("ERROR:" + e.getMessage());
    }
        return new BeneficiariosOB();
    }



}
