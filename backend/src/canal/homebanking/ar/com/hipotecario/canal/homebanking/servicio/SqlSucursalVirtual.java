package ar.com.hipotecario.canal.homebanking.servicio;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.conector.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SqlSucursalVirtual {

    public static Respuesta SPGeneric(String tipo, String nroSolicitud, String cobis, String estado) {
        try {
            SqlRequest sqlRequest = Sql.request("generic", "homebanking");
            sqlRequest.sql = ("EXEC [homebanking].[dbo].[sp_aceptacion_digital_generic] @tipo = ?, @nroSolicitud = ?, @cobis = ?, @estado = ?");
            sqlRequest.add(tipo);
            sqlRequest.add(nroSolicitud);
            sqlRequest.add(cobis);
            sqlRequest.add(estado);
            SqlResponse sqlResponse = Sql.response(sqlRequest);
            return getFinalReturnSql(sqlResponse);
        } catch (Exception e) {
            return Respuesta.error().set("mensaje", e.getMessage());
        }
    }

    /* Volvemos al paso previo de la aceptacion. Permite al integrante (En teoria: ultimo) volver a aceptar la solicitud */
    public static Respuesta rollbackSolicitud(String cobis, String nroSolicitud) {
        try {
            SqlRequest sqlRequest = Sql.request("rollback_aceptacion", "homebanking");
            sqlRequest.sql = ("EXEC [homebanking].[dbo].[sp_aceptacion_digital_rollback_aceptacion] @cobis = ?, @nroSolicitud = ?");
            sqlRequest.add(cobis);
            sqlRequest.add(nroSolicitud);
            SqlResponse sqlResponse = Sql.response(sqlRequest);
            return getFinalReturnSql(sqlResponse);
        } catch (Exception e) {
            return Respuesta.error().set("mensaje", e.getMessage());
        }
    }


    public static Respuesta actualizarEstado(String estado, String nroSolicitud) {
        try {
            SqlRequest sqlRequest = Sql.request("InsertGestionAceptacionDigital", "homebanking");
            sqlRequest.sql = ("EXEC [homebanking].[dbo].[sp_aceptacion_digital_update_estado_solicitud] @estado = ?, @nroSolicitud = ?");
            sqlRequest.add(estado);
            sqlRequest.add(nroSolicitud);
            SqlResponse sqlResponse = Sql.response(sqlRequest);
            return getFinalReturnSql(sqlResponse);
        } catch (Exception e) {
            return Respuesta.error().set("mensaje", e.getMessage());
        }
    }

    public static Respuesta insertGestionCRM(String idCobis, String estado_solicitud, Date fecha_aceptado, String nro_solicitud, String motivo, String ids, Integer aceptado, Integer titularidad, String usuario_crm, String canal, String nombreCompleto, String documento, String cuit, String resolucionId, Integer aceptadoNuevaPropuesta) {
        try {
            SqlRequest sqlRequest = Sql.request("InsertGestionAceptacionDigital", "homebanking");
            sqlRequest.sql = ("EXEC [homebanking].[dbo].[sp_aceptacion_digital_insert] @cobis = ?, @estado_solicitud = ?, @fecha_aceptado = ?, @nro_solicitud = ?, @motivo = ?, @id_producto = ?, @aceptado = ?, @titularidad = ?, @usuario_crm = ?, @canal = ?, @nombre_completo = ?, @documento = ?, @cuit = ?, @resolucion_id = ?, @acepta_nueva_propuesta = ?");
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
            sqlRequest.add(resolucionId);
            sqlRequest.add(aceptadoNuevaPropuesta.toString());
            SqlResponse sqlResponse = Sql.response(sqlRequest);
            return getFinalReturnSql(sqlResponse);

        } catch (Exception e) {
            return Respuesta.error().set("mensaje", e.getMessage());
        }
    }

    public static Respuesta buscarPorCobis(String valor) {
        try {
            SqlRequest sqlRequest = Sql.request("InsertGestionAceptacionDigital", "homebanking");
            sqlRequest.sql = ("EXEC [homebanking].[dbo].[sp_aceptacion_digital_get_cobis] @cobis = ?");
            sqlRequest.add(valor);
            SqlResponse sqlResponse = Sql.response(sqlRequest);
            return getFinalReturnSql(sqlResponse);
        } catch (Exception e) {
            return Respuesta.error().set("mensaje", e.getMessage());
        }

    }


    public static Respuesta buscarPorNroSolicitud(String nroSolicitud) {
        try {
            SqlRequest sqlRequest = Sql.request("GetGestionAceptacionDigital", "homebanking");
            sqlRequest.sql = ("EXEC [homebanking].[dbo].[sp_aceptacion_digital_get_nroSolicitud] @nro_solicitud = ?");
            sqlRequest.add(nroSolicitud);
            SqlResponse sqlResponse = Sql.response(sqlRequest);
            return getFinalReturnSql(sqlResponse);

        } catch (Exception e) {
            return Respuesta.error().set("mensaje", e.getMessage());
        }
    }

    public static Respuesta todosAceptaron(String nroSolicitud){
        try{
            SqlRequest sqlRequest = Sql.request("TodosAceptaronSolicitud", "homebanking");
            sqlRequest.sql = "EXEC [homebanking].[dbo].[sp_aceptacion_digital_todos_aceptaron] @nroSolicitud = ?";
            sqlRequest.add(nroSolicitud);
            SqlResponse sqlResponse = Sql.response(sqlRequest);
            return getFinalReturnSql(sqlResponse);
        }catch (Exception e){
            return Respuesta.error().set("mensaje", e.getMessage());
        }
    }

    public static Respuesta aceptarSolicitud(String nroSolicitud, String cobis, String cuit){
        try{
            SqlRequest sqlRequest = Sql.request("UpdateAceptacion", "homebanking");
            sqlRequest.sql = "EXEC [homebanking].[dbo].[sp_aceptacion_digital_aceptar_solicitud] @nro_solicitud = ?, @cobis = ?, @cuit = ?";
            sqlRequest.add(nroSolicitud);
            sqlRequest.add(cobis);
            sqlRequest.add(cuit);
            SqlResponse sqlResponse = Sql.response(sqlRequest);
            return getFinalReturnSql(sqlResponse);
        }catch (Exception e){
            return Respuesta.error().set("mensaje", e.getMessage());
        }
    }

    public static Respuesta notificacionLeida(String cobis, String numeroCaso) {
        try{
            SqlRequest sqlRequest = Sql.request("UpdateAceptacion", "homebanking");
            sqlRequest.sql = "EXEC [homebanking].[dbo].[sp_aceptacion_digital_leer_notificacion] @cobis = ?, @nro_solicitud = ?";
            sqlRequest.add(cobis);
            sqlRequest.add(numeroCaso);
            SqlResponse sqlResponse = Sql.response(sqlRequest);
            return getFinalReturnSql(sqlResponse);
        }catch (Exception e){
            return Respuesta.error().set("mensaje", e.getMessage());
        }
    }

    private static Respuesta getFinalReturnSql(SqlResponse sqlResponse) {
        if (sqlResponse.hayError) {
            return Respuesta.error().set("mensaje", sqlResponse.exception.getMessage());
        }

        if(sqlResponse.registros == null || sqlResponse.registros.isEmpty()){
            return Respuesta.error().set("detalle", "Sin registros");
        }

        if (!sqlResponse.registros.get(0).existe("codigo")) {
            Respuesta resp = Respuesta.exito();
            sqlResponse.registros.forEach(r -> {
                resp.add("registro", r);
            });
            return resp;
        }

        if (sqlResponse.registros.get(0).string("codigo").equalsIgnoreCase("1")) {
            return Respuesta
                    .error()
                    .set("mensaje", sqlResponse.registros.get(0).string("mensaje"))
                    .set("detalle", sqlResponse.registros.get(0).string("detalle"));
        }
        return Respuesta.exito()
                .set("mensaje", sqlResponse.registros.get(0).string("mensaje"));
    }

}
