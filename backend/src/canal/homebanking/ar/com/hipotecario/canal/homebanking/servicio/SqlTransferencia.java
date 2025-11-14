package ar.com.hipotecario.canal.homebanking.servicio;

import java.util.Date;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;

public class SqlTransferencia {
    public final static String INSERT_QUERY = "INSERT INTO [hbs].[dbo].[auditor_aumento_transferencia] ([momento],[diaSolicitud],[cobis],[idProceso],[ip],[canal],[codigoError],[descripcionError],[cuenta],[limite],[moneda],[fechaProgramada]) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
    public final static String GET_BY_CREATION_DAY_QUERY = "SELECT [id],[momento],[cobis],[idProceso],[ip],[canal],[codigoError],[descripcionError],[cuenta],[limite],[moneda],[fechaProgramada] FROM [hbs].[dbo].[auditor_aumento_transferencia] WHERE COBIS = ? AND diaSolicitud = ? AND cuenta = ? ";
    public final static String GET_SCHECULED_CONTACTS = "SELECT * FROM [Hbs].[dbo].[agenda_transferencias] WHERE nro_cliente = ? ORDER BY id DESC";
    public final static String GET_AGENDA_BENEFICIARIO = "SELECT * FROM [Hbs].[dbo].[agenda_transferencias] WHERE nro_cliente = ? AND (cbu_destino = ? OR nro_cuenta_destino = ?)";

    public SqlResponse insertSolicitudLimiteTransferencia(Date momento, Date diaSolicitud, String idCobis, String idProceso, String ip, String canal, String codigoError, String descError, String numeroCuenta, Double limite, String codigoMoneda, Date fechaProgramada) {
        SqlRequest sqlRequest = Sql.request("InsertAuditorLimiteTransferenciaHB", "hbs");
        sqlRequest.sql = INSERT_QUERY;
        sqlRequest.add(momento);// momento
        sqlRequest.add(diaSolicitud);// diaSolicitud
        sqlRequest.add(idCobis);// cobis
        sqlRequest.add(idProceso);// idProceso
        sqlRequest.add(ip);// ip
        sqlRequest.add(canal);// canal
        sqlRequest.add(codigoError);// codigoError
        sqlRequest.add(descError);// descripcion error
        sqlRequest.add(numeroCuenta);// cuenta
        sqlRequest.add(limite);// limite
        sqlRequest.add(codigoMoneda);// moneda
        sqlRequest.add(fechaProgramada);// fecha programada
        return Sql.response(sqlRequest);
    }

    public SqlResponse obtenerSolicitudPorDiaCreacion(String cobis, String fechaCreacion, String idCuenta) {
        SqlRequest sqlRequest = Sql.request("ObtenetAuditorLimiteTransferencia", "hbs");
        sqlRequest.sql = GET_BY_CREATION_DAY_QUERY;
        sqlRequest.add(cobis);
        sqlRequest.add(fechaCreacion);
        sqlRequest.add(idCuenta);
        return Sql.response(sqlRequest);
    }

    public SqlResponse contactosAgendados(String cobis) {
        SqlRequest sqlRequest = Sql.request("SelectAgendaTransferencias", "hbs");
        sqlRequest.sql = GET_SCHECULED_CONTACTS;
        sqlRequest.parametros.add(cobis);
        return Sql.response(sqlRequest);
    }

    public SqlResponse obtenerAgendaCuit(String cobis, String cuit) {
        SqlRequest sqlRequest = Sql.request("Select2AgendaTransferencias", "hbs");
        sqlRequest.sql = "SELECT * FROM [Hbs].[dbo].[agenda_transferencias] " +
                "WHERE nro_cliente = ? " +
                "AND documento_beneficiario = ?";
        sqlRequest.parametros.add(cobis);
        sqlRequest.parametros.add(cuit);
        return Sql.response(sqlRequest);
    }

    public SqlResponse obtenerAgendaTransferencia(String cobis, String idAgenda) {
        SqlRequest sqlRequest = Sql.request("Select2AgendaTransferencias", "hbs");
        sqlRequest.sql = GET_AGENDA_BENEFICIARIO;
        sqlRequest.parametros.add(cobis);
        sqlRequest.parametros.add(idAgenda);
        sqlRequest.parametros.add(idAgenda);
        return Sql.response(sqlRequest);
    }

    public void actualizarAlias(String cobis, String idAgenda, String alias) {
        String query = "UPDATE [Hbs].[dbo].[agenda_transferencias] ";
        query += "SET alias = ? ";
        query += "WHERE nro_cliente = ? AND id = ? ";

        SqlRequest sqlRequest = Sql.request("UpdateFaltantesAgenda", "hbs");
        sqlRequest.sql = query;
        sqlRequest.parametros.add(alias);
        sqlRequest.parametros.add(cobis);
        sqlRequest.parametros.add(idAgenda);
        Sql.response(sqlRequest);
    }


    public SqlResponse actualizarComentarioContactoAgendado(String titular, String id) {
        SqlRequest sqlRequest = Sql.request("UpdateAgendaTransferenciasComentario", "hbs");
        sqlRequest.sql = "UPDATE [Hbs].[dbo].[agenda_transferencias] SET comentario = ? WHERE id = ?";
        sqlRequest.parametros.add(titular);
        sqlRequest.parametros.add(id);
        return Sql.response(sqlRequest);

    }

    public SqlResponse actualizarContactoAgendado(String cobis, String idAgenda, String comentario, String descripcion, String email) {
        SqlRequest sqlRequest = Sql.request("UpdateAgendaTransferencias", "hbs");
        sqlRequest.sql = "UPDATE [Hbs].[dbo].[agenda_transferencias] ";
        if (comentario != null) {
            sqlRequest.sql += "SET comentario = ? ";
            sqlRequest.parametros.add(comentario);
        }
        if (descripcion != null) {
            sqlRequest.sql += ", descripcion = ? ";
            sqlRequest.parametros.add(descripcion);
        }
        if (email != null) {
            sqlRequest.sql += ", email_destinatario = ? ";
            sqlRequest.parametros.add(email);
        }
        sqlRequest.sql += "WHERE nro_cliente = ? AND (cbu_destino = ? OR nro_cuenta_destino = ?)";
        sqlRequest.parametros.add(cobis);
        sqlRequest.parametros.add(idAgenda);
        sqlRequest.parametros.add(idAgenda);
        return Sql.response(sqlRequest);
    }

    public void actualizarFaltantesAgenda(String cobis, String idAgenda, String cuil, String nombre, String cbu, String idMoneda) {
        String query = "UPDATE [Hbs].[dbo].[agenda_transferencias] ";
        query += "SET documento_beneficiario = ? , titular = ? , cbu_destino = ? , ";
        query += "moneda_cuenta_destino = ? ";
        query += "WHERE nro_cliente = ? AND id = ? ";

        SqlRequest sqlRequest = Sql.request("UpdateFaltantesAgenda", "hbs");
        sqlRequest.sql = query;
        sqlRequest.parametros.add(cuil);
        sqlRequest.parametros.add(nombre);
        sqlRequest.parametros.add(cbu);
        sqlRequest.parametros.add(idMoneda);
        sqlRequest.parametros.add(cobis);
        sqlRequest.parametros.add(idAgenda);
        Sql.response(sqlRequest);
    }

    public SqlResponse actualizarApodoAgendado(String cobis, String cuil, String apodo) {
        String query = "UPDATE [Hbs].[dbo].[agenda_transferencias] SET apodo = ? ";
        query += "WHERE nro_cliente = ? AND documento_beneficiario = ? ";

        SqlRequest sqlRequest = Sql.request("UpdateAgendaApodo", "hbs");
        sqlRequest.sql = query;
        sqlRequest.parametros.add(apodo);
        sqlRequest.parametros.add(cobis);
        sqlRequest.parametros.add(cuil);
        return Sql.response(sqlRequest);
    }

    public SqlResponse obtenerContactoAgendadoById(String cobis, String cbuDestino, String cuentaDestino) {
        SqlRequest sqlRequestConsulta = Sql.request("Select2AgendaTransferencias", "hbs");
        sqlRequestConsulta.sql = "SELECT * FROM [Hbs].[dbo].[agenda_transferencias] WHERE nro_cliente = ? AND (cbu_destino = ? OR nro_cuenta_destino = ?)";
        sqlRequestConsulta.parametros.add(cobis);
        sqlRequestConsulta.parametros.add(cbuDestino);
        sqlRequestConsulta.parametros.add(cuentaDestino);
        return Sql.response(sqlRequestConsulta);
    }

    public SqlResponse eliminarAgendaContactos(String cobis, String idAgenda) {
        SqlRequest sqlRequest = Sql.request("DeleteAgendaTransferencias", "hbs");
        sqlRequest.sql = "DELETE FROM [Hbs].[dbo].[agenda_transferencias] WHERE nro_cliente = ? AND (cbu_destino = ? OR nro_cuenta_destino = ?)";
        sqlRequest.parametros.add(cobis);
        sqlRequest.parametros.add(idAgenda);
        sqlRequest.parametros.add(idAgenda);
        return Sql.response(sqlRequest);
    }

    public void insertarDatosComprobanteTransferencia(ContextoHB contexto, String idComprobante, String nombreBanco, String cuentaNumero) {
        SqlRequest sqlRequest = Sql.request("InsertarDatosComprobante", "homebanking");

        sqlRequest.sql = "EXEC [homebanking].[dbo].[InsertarDatosComprobanteTransferencia] @id_cobis = ?, @id_comprobante= ?, @nombre_banco = ?, @cuenta_numero = ?";

        sqlRequest.parametros.add(contexto.idCobis());
        sqlRequest.parametros.add(idComprobante);
        sqlRequest.parametros.add(nombreBanco);
        sqlRequest.parametros.add(cuentaNumero);

        new Futuro<>(() -> Sql.response(sqlRequest));
    }

    public SqlResponse obtenerDatosComprobanteTransferencia(ContextoHB contexto, String idComprobante) {
        SqlRequest sqlRequest = Sql.request("ObtenerDatosComprobante", "homebanking");

        sqlRequest.sql = "EXEC [homebanking].[dbo].[ObtenerDatosComprobanteTransferencia] @id_cobis = ?, @id_comprobante= ?";

        sqlRequest.parametros.add(contexto.idCobis());
        sqlRequest.parametros.add(idComprobante);

        return Sql.response(sqlRequest);
    }
}
