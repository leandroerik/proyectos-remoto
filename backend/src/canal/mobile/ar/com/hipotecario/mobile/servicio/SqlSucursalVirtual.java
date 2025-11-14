package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SqlSucursalVirtual {

    /*
        SP generico. Dentro se encuentras las distintas consultas.
        aceptar_solicitud - get_cobis - get_nroSolicitud - leer_notificacion - rollback_aceptacion - todos_aceptaron - update_estado_solicitud
     */
    public static RespuestaMB SPGeneric(String tipo, String nroSolicitud, String cobis, String estado) {
        try {
            SqlRequestMB sqlRequest = SqlMB.request("generic", "homebanking");
            sqlRequest.sql = ("EXEC [homebanking].[dbo].[sp_aceptacion_digital_generic] @tipo = ?, @nroSolicitud = ?, @cobis = ?, @estado = ?");
            sqlRequest.add(tipo);
            sqlRequest.add(nroSolicitud);
            sqlRequest.add(cobis);
            sqlRequest.add(estado);
            SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
            return getFinalReturnSql(sqlResponse);
        } catch (Exception e) {
            return RespuestaMB.error().set("mensaje", e.getMessage());
        }
    }

    public static RespuestaMB insertGestionCRM(String idCobis, String estado_solicitud, Date fecha_aceptado, String nro_solicitud, String motivo, String ids, Integer aceptado, Integer titularidad, String usuario_crm, String canal, String nombreCompleto, String documento, String cuit) {
        try {
            SqlRequestMB sqlRequest = SqlMB.request("InsertGestionAceptacionDigital", "homebanking");
            sqlRequest.sql = ("EXEC [homebanking].[dbo].[sp_aceptacion_digital_insert] @cobis = ?, @estado_solicitud = ?, @fecha_aceptado = ?, @nro_solicitud = ?, @motivo = ?, @id_producto = ?, @aceptado = ?, @titularidad = ?, @usuario_crm = ?, @canal = ?, @nombre_completo = ?, @documento = ?, @cuit = ?");
            sqlRequest.add(idCobis);
            sqlRequest.add(estado_solicitud);
            sqlRequest.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fecha_aceptado));
            sqlRequest.add(nro_solicitud);
            sqlRequest.add(motivo);
            sqlRequest.add(ids);
            sqlRequest.add(aceptado.toString());
            sqlRequest.add(titularidad.toString());
            sqlRequest.add(usuario_crm);
            sqlRequest.add(canal);
            sqlRequest.add(nombreCompleto);
            sqlRequest.add(documento);
            sqlRequest.add(cuit);
            SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
            return getFinalReturnSql(sqlResponse);

        } catch (Exception e) {
            return RespuestaMB.error().set("mensaje", e.getMessage());
        }
    }

    private static RespuestaMB getFinalReturnSql(SqlResponseMB sqlResponse) {
        if (sqlResponse.hayError) {
            return RespuestaMB.error();
        }

        if(sqlResponse.registros == null || sqlResponse.registros.isEmpty()){
            return RespuestaMB.error().set("detalle", "Sin registros");
        }


        if (!sqlResponse.registros.get(0).existe("codigo")) {
            RespuestaMB resp = RespuestaMB.exito();
            sqlResponse.registros.forEach(r -> {
                resp.add("registro", r);
            });
            return resp;
        }

        if (sqlResponse.registros.get(0).string("codigo").equalsIgnoreCase("1")) {
            return RespuestaMB
                    .error()
                    .set("mensaje", sqlResponse.registros.get(0).string("mensaje"))
                    .set("detalle", sqlResponse.registros.get(0).string("detalle"));
        }
        return RespuestaMB.exito()
                .set("mensaje", sqlResponse.registros.get(0).string("mensaje"));
    }

}