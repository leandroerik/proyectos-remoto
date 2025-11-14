package ar.com.hipotecario.canal.tas.shared.utils.models;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Encriptador;
import ar.com.hipotecario.canal.libreriasecurity.domain.models.*;
import ar.com.hipotecario.canal.tas.ContextoTAS;

public class TasMinoRequestBuilder {

    public static TasMinoResquest build(TasMinoRequestParams params, ContextoTAS contexto) {

        // Numero de proceso que se envia para el visualizador de logs. si llega 0, es por que fallo <3
        //if(contexto.request.headers("x-idProceso") != null){}

        String processId ="0";

        TasMinoResquest tasMinoResquest = new TasMinoResquest();

        // BodyForBuhoData
        BodyForBuhoData bodyForBuhoData = new BodyForBuhoData();
        bodyForBuhoData.setDni(params.getNroDoc());
        bodyForBuhoData.setBuhoFacil(params.getClave());
        tasMinoResquest.setBodyRequesForBuild(bodyForBuhoData);

        // MosaicCredentials
        MosaicCredentials mosaicCredentials = new MosaicCredentials();
        mosaicCredentials.setKeyApplicationId(editEncriptor(contexto,contexto.config.string("tas_client_id")));
        mosaicCredentials.setKeySecret(editEncriptor(contexto, contexto.config.string("tas_secret_id")));
        tasMinoResquest.setMosaicCredentials(mosaicCredentials);

        // DataBaseCredentials
        DataBaseCredentials dbCredentials = new DataBaseCredentials();
        dbCredentials.setBdUrl(contexto.config.string("tas_ts_bd_url"));
        dbCredentials.setBdUser(editEncriptor(contexto, contexto.config.string("tas_ts_bd_usr")));
        dbCredentials.setBdPassword(editEncriptor(contexto, contexto.config.string("tas_ts_bd_pass")));
        tasMinoResquest.setDataBaseCredentials(dbCredentials);

        // Context
        Context contextoLibTs = new Context();
        contextoLibTs.setChannel(contexto.canal());
        contextoLibTs.setSubChannel(contexto.subCanal());
        contextoLibTs.setIp(contexto.ip());
        contextoLibTs.setAuditURi(contexto.config.string("backend_api_auditor"));
        contextoLibTs.setUser(contexto.getSesion().idCobis);
        contextoLibTs.setSession(contexto.idSesion());
        contextoLibTs.setProcessId(processId);
        contextoLibTs.setJourney(params.getJourney());
        tasMinoResquest.setContext(contextoLibTs);

        return  tasMinoResquest;
    }

    private static String editEncriptor(ContextoTAS contexto, String ve){
        String variableCorregida = ve.contains("AES256") ? ve.replace("AES256", "AES") : ve;
        String desencrip = Encriptador.desencriptarAES256CBC(contexto.config.string("aes_key"),variableCorregida);
        return desencrip;
    }
}
