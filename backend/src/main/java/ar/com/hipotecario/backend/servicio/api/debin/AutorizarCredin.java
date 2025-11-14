package ar.com.hipotecario.backend.servicio.api.debin;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.OBTransferencias;

import java.util.Random;

public class AutorizarCredin extends ApiObjeto {

    public String descripcion;
    public String fechaEjecucion;
    public String idCoelsa;
    public String idBanco;
    public String importe;
    public class detalleDestinatario extends ApiObjeto{
        public String idTributario;
        public cuenta cuental;
    }
    public class cuenta extends ApiObjeto{
        public String cbu;
        public String banco;
        public sucursal sucursal;
    }
    public class sucursal extends ApiObjeto{
        public String id;
    }

    public static AutorizarCredin post(ContextoOB contexto, String destinatarioCvu, String destinatarioBanco, String destinatarioIdTributario, String destinatarioNombre, String concepto, String importe, String monedaId, String monedaSigno, String monedaDescripcion, String originanteCbu, String originanteNumeroCuenta, String originanteSucursalId, String originanteTipoCuenta, String originanteIdTributario, String originanteMail, String originanteNombre, Long nroOperacion) {
        ApiRequest request = new ApiRequest("CREDIN Alta","debin","POST","/v1/credin/autorizar",contexto);
        ar.com.hipotecario.backend.base.Objeto cuentaDestinatario = new ar.com.hipotecario.backend.base.Objeto();
        cuentaDestinatario.set("cvu", destinatarioCvu);
        cuentaDestinatario.set("banco", destinatarioBanco);
        if (!OBTransferencias.esCVU(destinatarioCvu)){
            ar.com.hipotecario.backend.base.Objeto sucursal = new ar.com.hipotecario.backend.base.Objeto();
            sucursal.set("id",destinatarioCvu.substring(3,7));
            cuentaDestinatario.set("sucursal",sucursal);
        }


        ar.com.hipotecario.backend.base.Objeto destinatario = new ar.com.hipotecario.backend.base.Objeto();
        destinatario.set("cuenta", cuentaDestinatario);
        destinatario.set("idTributario", destinatarioIdTributario);
        destinatario.set("nombreCompleto", destinatarioNombre);

        ar.com.hipotecario.backend.base.Objeto moneda = new ar.com.hipotecario.backend.base.Objeto();
        moneda.set("id", monedaId);
        moneda.set("signo", monedaSigno);
        moneda.set("descripcion", monedaDescripcion);

        ar.com.hipotecario.backend.base.Objeto detalle = new ar.com.hipotecario.backend.base.Objeto();
        detalle.set("concepto", concepto);
        detalle.set("importe", importe);
        detalle.set("moneda", moneda);

        ar.com.hipotecario.backend.base.Objeto sucursal = new ar.com.hipotecario.backend.base.Objeto();
        sucursal.set("id", originanteSucursalId);

        ar.com.hipotecario.backend.base.Objeto cuentaOriginante = new ar.com.hipotecario.backend.base.Objeto();
        cuentaOriginante.set("cbu", originanteCbu);
        cuentaOriginante.set("numero", originanteNumeroCuenta);
        cuentaOriginante.set("sucursal", sucursal);
        cuentaOriginante.set("tipo", originanteTipoCuenta);

        ar.com.hipotecario.backend.base.Objeto originante = new ar.com.hipotecario.backend.base.Objeto();
        originante.set("cuenta", cuentaOriginante);
        originante.set("idTributario", originanteIdTributario);
        originante.set("mail", contexto.sesion().usuarioOB.email);
        originante.set("nombreCompleto", originanteNombre);

        request.body("destinatario", destinatario);
        request.body("detalle", detalle);
        request.body("originante", originante);
        Random random = new Random(nroOperacion.hashCode());
        request.query("secuencial",String.format("%011d", Math.abs(random.nextLong()) % 1_000_000_000_00L));

        request.cache = false;
        ApiResponse response = request.ejecutar();
        if (response.contains("FONDOS INSUFICIENTES")){
            AutorizarCredin autorizarCredin = new AutorizarCredin();
            autorizarCredin.descripcion = "FONDOS INSUFICIENTES";
            return autorizarCredin;
        }
        ApiException.throwIf(!response.http(200, 201), request, response);
        return response.crear(AutorizarCredin.class);
    }
}

