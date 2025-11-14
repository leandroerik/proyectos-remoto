package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.infrastructure.adapter;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.port.TASCuentasV4Port;

public class TASRestCuentasV4Adapter implements TASCuentasV4Port {

    @Override
    public Objeto getDatosCuentas(ContextoTAS contexto, String idCliente, boolean acuerdo, boolean firmaConjunta, boolean consultaAlias, boolean firmantes, String tipoEstado) {
        ApiRequest request = new ApiRequest("ConsultaCuentaIdcliente", "cuentas", "GET", "/v2/cuentas", contexto);
        //"/v1/cuentas?idcliente={idCliente}&cancelados={cancelados}&firmaconjunta={firmaconjunta}"
        //v2/cuentas?acuerdo=false&consultaalias=false&firmaconjunta=false&idcliente=135706&tipoestado=vigente
        request.query("idcliente", idCliente);
        request.query("acuerdo", acuerdo);
        request.query("firmaconjunta", firmaConjunta);
        request.query("consultaalias", consultaAlias);
        request.query("firmantes", firmantes);
        request.query("tipoestado", tipoEstado);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200,202, 204), request, response);
        Objeto respFinal = new Objeto();
        for(Objeto obj :  response.objetos()){
            if(!obj.string("tipoTitularidad").equals("F")) respFinal.add(obj);
        }
        respFinal.set("codigoHTTP", response.codigoHttp);
        return respFinal;
    }
}
