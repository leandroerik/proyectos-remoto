package ar.com.hipotecario.canal.tas.modulos.cuentas.cuenta.services;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;
import java.util.ArrayList;
import java.util.List;

public class TASSqlCuenta {

    // "SELECT sp_fechaSolicitud FROM solicitudProducto WHERE sp_idCobis = ? and sp_tipoProducto=? and sp_numeroProducto = ? and sp_canal=? ";
    //
    public static List<String> getSolicitudesEnCurso(ContextoTAS contexto, String idCliente, String tipoCuenta, String idCuenta){
        String sql = "";
        sql += "SELECT sp_fechaSolicitud FROM [hbs].[dbo].[solicitudProducto] ";
        sql += "WHERE sp_idCobis = ? ";
        sql += "AND sp_tipoProducto = ? ";
        sql += "AND sp_numeroProducto = ? ";
        sql += "AND sp_canal = ?";

        Objeto rtaSql = Sql.select(contexto,"tashbs",sql, idCliente, tipoCuenta, idCuenta, "TAS");
        if(rtaSql.isEmpty()) return null;
        List<String> fechas = new ArrayList<>();
        for(Objeto obj : rtaSql.objetos()){
            fechas.add(obj.string("sp_fechaSolicitud"));
        }
        return fechas;
    }

    public static Objeto guardarSolicitudBaja(ContextoTAS contexto, Objeto datosParaBaja){
        try {
            String tipoOperacion = datosParaBaja.string("tipoOperacion");
            String idCobis = datosParaBaja.string("idCliente");
            String fechaSolicitud = datosParaBaja.string("fechaSolicitud");
            String tipoProducto = datosParaBaja.string("tipoProducto");
            String numeroProducto = datosParaBaja.string("numeroProducto");
            String sucursal = datosParaBaja.string("sucursal");
            String apellido = datosParaBaja.string("apellido");
            String nombre = datosParaBaja.string("nombre");
            String sexo = datosParaBaja.string("sexo");
            String tipoDocumento = datosParaBaja.string("tipoDocumento");
            String numeroDocumento = datosParaBaja.string("numeroDocumento");
            String canal = datosParaBaja.string("canal");

            String sql = "";
            sql += "INSERT INTO [hbs].[dbo].[solicitudProducto] (";
            sql += "sp_tipoOperacion, ";
            sql += "sp_idCobis, ";
            sql += "sp_fechaSolicitud, ";
            sql += "sp_tipoProducto, ";
            sql += "sp_numeroProducto, ";
            sql += "sp_sucursal, ";
            sql += "sp_apellido, ";
            sql += "sp_nombre, ";
            sql += "sp_sexo, ";
            sql += "sp_tipoDocumento, ";
            sql += "sp_numeroDocumento, ";
            sql += "sp_canal) ";
            sql += "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

            Integer rtaSql = Sql.update(contexto, "tashbs", sql, tipoOperacion, idCobis, fechaSolicitud, tipoProducto, numeroProducto,
                    sucursal, apellido, nombre, sexo, tipoDocumento, numeroDocumento, canal);

            return rtaSql != 1 ? new Objeto().set("baja_ca", false) : new Objeto().set("baja_ca", true);
        }catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado","ERROR");
            error.set("error", e);
            return error;
        }
    }

    public static Objeto guardarSolicitudAlta(ContextoTAS contexto, Objeto datosAlta){
        try {
            String tipoOperacion = datosAlta.string("tipoOperacion");
            String idCobis = datosAlta.string("idCobis");
            String fechaSolicitud = datosAlta.string("fechaSolicitud");       
            String secuencial = datosAlta.string("secuencial", null);
            String tipoProducto = datosAlta.string("tipoProducto");
            String numeroProducto = datosAlta.string("numeroProducto", null);
            String moneda = datosAlta.string("moneda");
            String sucursal = datosAlta.string("sucursal");
            String apellido = datosAlta.string("apellido");
            String nombre = datosAlta.string("nombre");
            String sexo = datosAlta.string("sexo");
            String tipoDocumento = datosAlta.string("tipoDocumento");
            String numeroDocumento = datosAlta.string("numeroDocumento");
            String fechaNacimiento = datosAlta.string("fechaNacimiento", null);
            String paisNacimientoCod = datosAlta.string("paisNacimientoCod", null);
            String paisNacimientoDesc = datosAlta.string("paisNacimientoDesc", null);
            String nacionalidadCod = datosAlta.string("nacionalidadCod", null);
            String nacionalidadDesc = datosAlta.string("nacionalidadDesc", null);
            String tipoIdTributario = datosAlta.string("tipoIdTributario", null);
            String idTributario = datosAlta.string("idTributario");
            String estadoCivil = datosAlta.string("estadoCivil");
            String profesionCod = datosAlta.string("profesionCod", null);
            String profesionDesc = datosAlta.string("profesionDesc", null);
            String situacionLaboral = datosAlta.string("situacionLaboral");
            String fechaIngreso = datosAlta.string("fechaIngreso", null);
            String sueldo = datosAlta.string("sueldo", null);
            String calle = datosAlta.string("calle");
            String altura = datosAlta.string("altura");
            String piso = datosAlta.string("piso", null);
            String departamento = datosAlta.string("departamento", null);
            String codigoPostal = datosAlta.string("codigoPostal");
            String localidadCod = datosAlta.string("localidadCod");
            String provinciaCod = datosAlta.string("provinciaCod");
            String provinciaDesc = datosAlta.string("provinciaDesc");
            String tipoTelefonoCod = datosAlta.string("tipoTelefonoCod");
            String tipoTelefonoDesc = datosAlta.string("tipoTelefonoDesc");
            String ddiTelefono = datosAlta.string("ddiTelefono");
            String ddnTelefono = datosAlta.string("ddnTelefono");
            String caracteristicaTelefono = datosAlta.string("caracteristicaTelefono");
            String numeroTelefono = datosAlta.string("numeroTelefono");
            String tipoMail = datosAlta.string("tipoMail");
            String mail = datosAlta.string("mail");
            String pep = datosAlta.string("pep");
            String so = datosAlta.string("so");
            String ocde = datosAlta.string("ocde");
            String fatca = datosAlta.string("fatca");
            String licitudFondos = datosAlta.string("licitudFondos");
            String ttcc = datosAlta.string("ttcc");
            String canal = datosAlta.string("canal");
            String idSolicitud = datosAlta.string("idSolicitud");
            String estadoSolicitud = datosAlta.string("estadoSolicitud");
            String tarjetaDebito_vinculada = datosAlta.string("tarjetaDebitoVinculada");


            String sqlString = "";
            sqlString += "INSERT INTO [hbs].[dbo].[solicitudProducto] (";
            sqlString += "sp_tipoOperacion, " ;
            sqlString += "sp_idCobis, " ;
            sqlString += "sp_fechaSolicitud, " ;
            sqlString += "sp_secuencial, " ;
            sqlString += "sp_tipoProducto, ";
            sqlString += "sp_numeroProducto, " ;
            sqlString += "sp_moneda, ";
            sqlString += "sp_sucursal, " ;
            sqlString += "sp_apellido, " ;
            sqlString += "sp_nombre, " ;
            sqlString += "sp_sexo, " ;
            sqlString += "sp_tipoDocumento, " ;
            sqlString += "sp_numeroDocumento, " ;
            sqlString += "sp_fechaNacimiento, " ;
            sqlString += "sp_paisNacimientoCod, " ;
            sqlString += "sp_paisNacimientoDesc, " ;
            sqlString += "sp_nacionalidadCod, " ;
            sqlString += "sp_nacionalidadDesc, " ;
            sqlString += "sp_tipoIdTributario, " ;
            sqlString += "sp_idTributario, " ;
            sqlString += "sp_estadoCivil, " ;
            sqlString += "sp_profesionCod, " ;
            sqlString += "sp_profesionDesc, " ;
            sqlString += "sp_situacionLaboral, " ;
            sqlString += "sp_fechaIngreso, " ;
            sqlString += "sp_sueldo, " ;
            sqlString += "sp_calle, " ;
            sqlString += "sp_altura, " ;
            sqlString += "sp_piso, " ;
            sqlString += "sp_departamento, " ;
            sqlString += "sp_codigoPostal, " ;
            sqlString += "sp_localidadCod, " ;
            sqlString += "sp_provinciaCod, " ;
            sqlString += "sp_provinciaDesc, " ;
            sqlString += "sp_tipoTelefonoCod, " ;
            sqlString += "sp_tipoTelefonoDesc, " ;
            sqlString += "sp_ddiTelefono, " ;
            sqlString += "sp_ddnTelefono, " ;
            sqlString += "sp_caracteristicaTelefono, " ;
            sqlString += "sp_numeroTelefono, " ;
            sqlString += "sp_tipoMail, " ;
            sqlString += "sp_mail, " ;
            sqlString += "sp_pep, " ;
            sqlString += "sp_so, " ;
            sqlString += "sp_ocde, " ;
            sqlString += "sp_fatca, " ;
            sqlString += "sp_licitudFondos, " ;
            sqlString += "sp_ttcc, " ;
            sqlString += "sp_canal, ";
            sqlString += "sp_idSolicitud, ";
            sqlString += "sp_estadoSolicitud, ";
            sqlString += "sp_tarjetaDebito_vinculada) " ;
            sqlString += "VALUES ";
            sqlString += "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            
            Integer rta = Sql.update(contexto, "tashbs", sqlString, tipoOperacion, idCobis, fechaSolicitud, secuencial, 
            tipoProducto, numeroProducto, moneda, sucursal, apellido, nombre, sexo, tipoDocumento, numeroDocumento, fechaNacimiento, 
            paisNacimientoCod, paisNacimientoDesc, nacionalidadCod, nacionalidadDesc, tipoIdTributario, idTributario, estadoCivil, 
            profesionCod, profesionDesc, situacionLaboral, fechaIngreso, sueldo, calle, altura, piso, departamento, codigoPostal, 
            localidadCod, provinciaCod, provinciaDesc, tipoTelefonoCod, tipoTelefonoDesc, ddiTelefono, ddnTelefono, caracteristicaTelefono, 
            numeroTelefono, tipoMail, mail, pep, so, ocde, fatca, licitudFondos, ttcc, canal, idSolicitud, estadoSolicitud, tarjetaDebito_vinculada);            
            
            return rta != 1 ? new Objeto().set("estado", "false") : new Objeto().set("estado", "true");
        } catch (Exception e) {
            Objeto error = new Objeto();
            error.set("estado","ERROR");
            error.set("error", e);
            LogTAS.error(contexto, e);
            return error;
        }
    }
    
    public static Objeto actualizarEstadoSolicitud(ContextoTAS contexto, String nroSolicitud, String estado){
        try {
            String sql = "";
            sql += "UPDATE solicitudProducto SET sp_estadoSolicitud=? ";
            sql += "WHERE sp_idSolicitud=?";
            Integer rta = Sql.update(contexto, "tashbs", sql, estado, nroSolicitud);
            return rta != 1 ? new Objeto().set("estado", "false") : new Objeto().set("estado", "true");
        } catch (Exception e) {
            Objeto error = new Objeto();
            error.set("estado","ERROR");
            error.set("error", e);
            LogTAS.error(contexto, e);
            return error;
        }
    }
}
