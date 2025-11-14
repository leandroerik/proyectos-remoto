package ar.com.hipotecario.canal.tas.shared.modulos.depositos.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.exception.SqlException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;
import ar.com.hipotecario.canal.tas.shared.modulos.depositos.models.DepositoCuentaDTO;
import ar.com.hipotecario.canal.tas.shared.modulos.depositos.models.DepositoValores;
import ar.com.hipotecario.canal.tas.shared.modulos.depositos.models.ResponseInsertValores;
import ar.com.hipotecario.canal.tas.shared.modulos.depositos.models.ResponseSpAltaDepositos;
import ar.com.hipotecario.canal.tas.shared.utils.models.enums.TASCodigoMonedaEnum;
import ar.com.hipotecario.canal.tas.shared.modulos.depositos.models.DepositoCuentaDTO.DepositosCuentaDTO;

public class TasSqlDepositos {
    public static Objeto getPrecintosLotes(Contexto contexto, String idTas) {
        Objeto rta = new Objeto();
        String sql = "";
        sql += "SELECT TOP 1 Precinto1, Precinto2, Lote ";
        sql += "FROM [hipotecarioTAS].[dbo].[TASKioscosAuditorias] ";
        sql += "WHERE KioscoId = ? ORDER BY FechaUltimaAuditoria DESC";
        rta = Sql.select(contexto, "hipotecariotas", sql, idTas);

        SqlException.throwIf("ERROR_Parametros", (rta == null || rta.isEmpty()));
        return rta != null && rta.isEmpty() ? null : rta.objetos().get(0);
    }

    // 1- Ejecutar SP de inicializacion de orden deposito: [altaDeposito]
    public static Map<String, Object> saveOrdenDeposito(Contexto contexto, DepositoCuentaDTO deposito) {
        try {
            String sqlIns = "EXEC [hipotecarioTAS].[dbo].[altaDeposito] ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?";

            ResponseSpAltaDepositos numeroTicket = new ResponseSpAltaDepositos(java.sql.Types.VARCHAR);
            ResponseSpAltaDepositos depositoId = new ResponseSpAltaDepositos(java.sql.Types.INTEGER);

            Map<String, Object> rtaSql = Sql.executeSpAltaDepositoTasi(contexto, "hipotecariotas", sqlIns,
                    deposito.getTasOriginaria().intValue(),
                    numeroTicket, // Parametro de salida
                    deposito.getNumeroSobre(),
                    new Timestamp(new Date().getTime()),
                    "P",
                    new Timestamp(new Date().getTime()),
                    deposito.getNumeroOperacionCanal(),
                    deposito.getSucursal(),
                    deposito.getTipoDocumento(),
                    deposito.getNumeroDocumento(),
                    deposito.getTipoDestino(),
                    deposito.getDestinoDescripcion(),
                    deposito.getMoneda(),
                    new BigDecimal(0.0),
                    new BigDecimal(0.0),
                    new BigDecimal(0.0),
                    "S",
                    deposito.getLote(),
                    deposito.getPrecinto(),
                    deposito.getProcessId(),
                    deposito.getDescripcionBanca(),
                    deposito.getCodigoBanca(),
                    depositoId // Parametro de salida
            );

            SqlException.throwIf("ERROR_Parametros", (rtaSql != null && rtaSql.isEmpty()));
            return rtaSql;

        } catch (Exception e) {
            LogTAS.error((ContextoTAS) contexto, e);
            return Collections.emptyMap();
        }
    }

    public static boolean deleteOrdenDeposito(Contexto contexto, Long depositoId, String numeroTicket) {
        String sql = "";
        sql += "DELETE FROM [dbo].[TASKioscosDepositos] ";
        sql += "WHERE  DepositoId = ? and NumeroTicket = ?";

        return Sql.update(contexto, "hipotecariotas", sql, depositoId, numeroTicket) > 0;
    }

    public static DepositoCuentaDTO getDepositoByNumeroTicket(Contexto contexto, String numeroTicket) {
        String sql = "SELECT TOP 1 * ";
        sql += "FROM [hipotecarioTAS].[dbo].[TASKioscosDepositos] ";
        sql += "WHERE NumeroTicket = ?";
        Objeto registros = Sql.select(contexto, "hipotecariotas", sql, numeroTicket);
        SqlException.throwIf("ERROR_Parametros", (registros == null));
        DepositoCuentaDTO deposito = SqlObjetos.map(registros, DepositosCuentaDTO.class, DepositoCuentaDTO.class)
                .first();
        deposito.setCodigoMoneda(deposito.getCodigoMoneda().equals(TASCodigoMonedaEnum.PESOS.getCodigoMoneda()) ? "80"  :"2");
        return deposito;
    }

    // 2- Insertar en la tabla [TASKioscosDepositosValores] los valores que recibo
    public static ResponseInsertValores insertValores(Contexto contexto,
            List<DepositoValores> depositoValoresList) {

        boolean hasErrors = false;
        BigDecimal total = BigDecimal.ZERO;
              		
        try {
            String sql = "";
            sql += "INSERT INTO [dbo].[TASKioscosDepositosValores]";

            int count = 0;

            for (DepositoValores valor : depositoValoresList) {
                valor.setOrden(count);
                count++;
                
                String sqlOrden="";
                sqlOrden += "SELECT orden ";
                sqlOrden += "FROM [hipotecarioTAS].[dbo].[TASKioscosDepositosValores] ";
                sqlOrden += "WHERE KioscoId = ? AND DepositoId=?";
                
                Objeto rtaOrden =  Sql.select(contexto, "hipotecariotas", sqlOrden, valor.getKioscoId(), valor.getDepositoId());

                
                Objeto rta = new Objeto();
                String sqlExiste = "";
                sqlExiste += "SELECT * ";
                sqlExiste += "FROM [hipotecarioTAS].[dbo].[TASKioscosDepositosValores] ";
                sqlExiste += "WHERE KioscoId = ? AND DepositoId=? and Importe=?";
                rta = Sql.select(contexto, "hipotecariotas", sqlExiste, valor.getKioscoId(), valor.getDepositoId(), valor.getImporte());

                if (rta != null && rta.isEmpty()) {
                	 if (rtaOrden != null && !rtaOrden.isEmpty()) valor.setOrden(rtaOrden.objetos().size());
	                Integer result = Sql.insertGenerico(contexto, "hipotecariotas", sql, valor);
	                if (result == 1) {
	                    BigDecimal cantidad = BigDecimal.valueOf(valor.getCantidad());
	                    BigDecimal denominacion = valor.getImporte();
	                    BigDecimal importe = cantidad.multiply(denominacion);
	                    total = total.add(importe);
	                } else {
	                    hasErrors = true;
	                }
                } else {
                	 String sqlUpdate = "";
                	 sqlUpdate += "UPDATE [TASKioscosDepositosValores] SET ";
                	 sqlUpdate += "Cantidad = ? ";
                	 sqlUpdate += "WHERE KioscoId = ? AND DepositoId=? and Importe=? ";
                     Sql.update(contexto, "hipotecariotas", sqlUpdate, valor.getCantidad(), valor.getKioscoId(), valor.getDepositoId(), valor.getImporte());
                     BigDecimal cantidad = BigDecimal.valueOf(valor.getCantidad());
	                 BigDecimal denominacion = valor.getImporte();
	                 BigDecimal importe = cantidad.multiply(denominacion);
	                 total = total.add(importe);
                }
            }
            return new ResponseInsertValores(hasErrors, total);
        } catch (Exception e) {
            return new ResponseInsertValores(hasErrors, total);
        }
    }

    // 3- Update del deposito [TASKioscosDepositos]
    public static boolean updateImporteTotalEfectivo(Contexto contexto, String numeroTicket, BigDecimal total) {
        try {
            String sql = "";
            sql += "UPDATE [TASKioscosDepositos] SET ";
            sql += "ImporteTotalEfectivo = ? ";
            sql += "WHERE NumeroTicket = ? ";
            return Sql.update(contexto, "hipotecariotas", sql, total, numeroTicket) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static Objeto updateEstadoDeposito(ContextoTAS contexto, Objeto datos, String estadoDeposito){
        try{
            String estDeposito = estadoDeposito;
            String numeroOperacion = datos.string("numeroOperacion");
            Timestamp fechaEstado = new Timestamp(new Date().getTime());
            String personaEstado = datos.string("personaEstado");
            int cantRetenidos = datos.integer("cantidadRetenidos");
            int kioscoId = datos.integer("kioscoId");
            int depositoId = datos.integer("depositoId");
            String sql = "";
            sql += "UPDATE [TASKioscosDepositos] SET ";
            sql += "EstadoDeposito = ?, NumeroOperacion = ?, ";
            sql += "FechaEstadoDeposito = ?, PersonaEstadoDeposito = ?, CantidadRetenidos = ? ";
            sql += "WHERE KioscoId = ? AND DepositoId = ?";
            int response = Sql.update(contexto, "hipotecariotas", sql, estDeposito, numeroOperacion, fechaEstado, personaEstado, cantRetenidos, kioscoId, depositoId);
            return new Objeto().set("estado",response > 0);
        }catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

	public static Objeto updateCodigoRetornoTimeout(ContextoTAS contexto, String numeroTicket, Integer codigoRetorno) {
	     try{
	           
	            String sql = "";
	            sql += "UPDATE [TASKioscosDepositos] ";
	            sql += "SET codigoRetorno=? WHERE numeroTicket = ?";
	                int response = Sql.update(contexto, "hipotecariotas", sql, codigoRetorno, numeroTicket);
	            return new Objeto().set("codigoRetorno",response > 0);
	        }catch (Exception e){
	            Objeto error = new Objeto();
	            error.set("estado", "ERROR");
	            error.set("error", e);
	            return error;
	        }
	}

    public static Objeto updateDepositoToReversado(ContextoTAS contexto, int kioscoId){
        Connection conn = null;
        Objeto obj = new Objeto();
        try {
            String sqlSelect = "";
            sqlSelect += "SELECT FechaUltimaAuditoria FROM [hipotecarioTAS].[dbo].[TASKioscosAuditorias] ";
            sqlSelect += "WHERE KioscoId = ? ORDER BY FechaUltimaAuditoria DESC";
            Objeto rtaSelect = Sql.select(contexto, "hipotecariotas",sqlSelect , kioscoId);
            String fechaAuditoria = rtaSelect.objetos().get(0).string("FechaUltimaAuditoria");
            long dateLong = Long.parseLong(fechaAuditoria);
            Date fechaDate = new Date(dateLong);
            Timestamp fechaCierreUltimaAuditoria = new Timestamp(fechaDate.getTime());

            conn = contexto.dataSource("hipotecariotas").getConnection();
            conn.setAutoCommit(false);
            int rta = 0;

            String sqlUpdate = "UPDATE TASKioscosDepositos SET EstadoDeposito='A' ";
            sqlUpdate += "WHERE KioscoId = ? and FechaDeposito >= ? ";
            sqlUpdate += "AND EstadoDeposito='R' AND CodigoRetorno=100" ;

            PreparedStatement ps = conn.prepareStatement(sqlUpdate);
            ps.setInt(1, kioscoId);
            ps.setTimestamp(2, fechaCierreUltimaAuditoria);
            rta = ps.executeUpdate();
            conn.commit();
            return obj.set("estado", rta != 0 ? "true" : "false");
        }catch (SQLException e){
            LogTAS.error(contexto, e);
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                obj.set("estado", "ERROR");
                obj.set("error", e);
                return obj;
            }
            obj.set("estado", "ERROR");
            obj.set("error", e);
            return obj;
        }finally {
            try {
                conn.close();
            } catch (SQLException e) {
                obj.set("estado", "ERROR");
                obj.set("error", e);
                return obj;
            }
        }
    }

}
