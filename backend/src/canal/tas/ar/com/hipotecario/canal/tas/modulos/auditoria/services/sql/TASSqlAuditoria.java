package ar.com.hipotecario.canal.tas.modulos.auditoria.services.sql;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;
import ar.com.hipotecario.canal.tas.modulos.auditoria.modelos.reversas.*;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASClientePersona;
import ar.com.hipotecario.canal.tas.shared.modulos.kiosco.modelos.TASKiosco;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TASSqlAuditoria {

    public static Objeto getLoteActual(ContextoTAS contexto, String idTas, int sucursalId) {
        try {
            String sql = "";
            sql += "SELECT nroLote FROM [hipotecarioTAS].[dbo].[LotesTASi] ";
            sql += "WHERE kioscoId = ? ";
            sql += "AND sucursalId = ?";
            Objeto rtaSql = Sql.select(contexto, "hipotecariotas", sql, idTas, sucursalId);
            Objeto response = new Objeto();
            response.set("estado", "OK");
            response.set("respuesta", rtaSql);
            return response;
        } catch (Exception e) {
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

    public static Objeto getDatosCierre(ContextoTAS contexto, String idTas) {
        try {
            String sql = "";
            sql += "SELECT TOP 1 Precinto1, Precinto2, Lote, FechaUltimaAuditoria, TipoCierre FROM [hipotecarioTAS].[dbo].[TASKioscosAuditorias] ";
            sql += "WHERE KioscoId = ? ";
            sql += "ORDER BY FechaUltimaAuditoria DESC";
            Objeto rtaSql = Sql.select(contexto, "hipotecariotas", sql, idTas);
            return rtaSql;
        } catch (Exception e) {
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

    public static Objeto getFechaUltimaAuditoriaByIpPrecintos(ContextoTAS contexto, TASKiosco kiosco, String tipoCierre, String precinto1, String precinto2, String loteActual) {
        try {
            Objeto fechaUltimaAuditoria = new Objeto();
            String sql = "";
            sql += "SELECT FechaUltimaAuditoria FROM [hipotecarioTAS].[dbo].[TASKioscosAuditorias] ";
            sql += "WHERE KioscoId IN (SELECT KioscoId FROM TASKioscos WHERE DireccionIP = ?) ";
            sql += "AND TipoCierre = ?";
            Objeto rtaSql = Sql.select(contexto, "hipotecariotas", sql, kiosco.getKioscoIp(), tipoCierre);
            if (rtaSql.isEmpty() || rtaSql.string("FechaUltimaAuditoria") == null) {
                String idTas = kiosco.getKioscoId().toString();
                Date fechaHoy = new Date();
                Timestamp hoy = new Timestamp(fechaHoy.getTime());
                String sqlInsert = "";
                sqlInsert += "INSERT INTO [hipotecarioTAS].[dbo].[TASKioscosAuditorias] ";
                sqlInsert += "(KioscoId, FechaUltimaAuditoria, Precinto1, Precinto2, Lote,  TipoCierre)  ";
                sqlInsert += "VALUES (?,?,?,?,?,?)";
                int rtaSqlInsert = Sql.update(contexto, "hipotecariotas", sqlInsert, idTas, hoy, precinto1, precinto2, loteActual, tipoCierre);
                if (rtaSqlInsert != 0) {
                    fechaUltimaAuditoria.set("estado", "OK");
                    fechaUltimaAuditoria.set("fechaUltimaAuditoria", fechaHoy.getTime());
                    return fechaUltimaAuditoria;
                } else {
                    throw new Exception("no se pudo ingresar Registro de Ultima Auditoria");
                }
            } else {
                fechaUltimaAuditoria.set("estado", "OK");
                fechaUltimaAuditoria.set("fechaUltimaAuditoria", rtaSql.string("FechaUltimaAuditoria"));
                return rtaSql;
            }
        } catch (Exception e) {
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            LogTAS.error(contexto, e);
            return error;
        }
    }

    public static Objeto getFechaUltimaAuditoriaByIdTipoCierre(ContextoTAS contexto, TASKiosco kiosco, String tipoCierre) {
        try {
            Objeto rtaSql = new Objeto();
            boolean esCierreParcial = tipoCierre.equalsIgnoreCase("parcial");
            String sql = "";
            if (esCierreParcial) {
                sql += "SELECT TOP 1 FechaUltimaAuditoria, tipoCierre, (SELECT KA2.tipoCierre FROM TASKioscosAuditorias AS KA2 WHERE KA2.KioscoId IN ";
                sql += "(SELECT KioscoId FROM TASKioscos AS TK2 WHERE TK2.DireccionIP = ?) AND KA2.tipoCierre='parcial') ";
                sql += "AS existeCierreParcial, Precinto1, Precinto2, Lote ";
                sql += "FROM TASKioscosAuditorias AS KA WHERE KA.KioscoId IN ";
                sql += "(SELECT KioscoId FROM TASKioscos AS TK WHERE TK.DireccionIP = ?) ";
                sql += "ORDER BY FechaUltimaAuditoria DESC";
                rtaSql = Sql.select(contexto, "hipotecariotas", sql, kiosco.getKioscoIp(), kiosco.getKioscoIp());
            } else {
                sql += "SELECT FechaUltimaAuditoria, tipoCierre, null AS existeCierreParcial ";
                sql += "FROM TASKioscosAuditorias AS KA WHERE KA.KioscoId IN ";
                sql += "(SELECT KioscoId FROM TASKioscos AS TK WHERE TK.DireccionIP = ?) ";
                sql += "ORDER BY FechaUltimaAuditoria DESC";
                rtaSql = Sql.select(contexto, "hipotecariotas", sql, kiosco.getKioscoIp());
            }
            //todo ver si toma bien.. aca tendria q ser true
            boolean noExisteCierreParcial = rtaSql.objetos().get(0).string("existeCierreParcial").isEmpty();
            //es cierre parcial true y no existe cierre true
            if (esCierreParcial && noExisteCierreParcial) {
                String precinto1 = rtaSql.objetos().get(0).string("Precinto1");
                String precinto2 = rtaSql.objetos().get(0).string("Precinto2");
                String lote = rtaSql.objetos().get(0).string("Lote");
                String idTas = kiosco.getKioscoId().toString();
                Date fechaHoy = new Date();
                Timestamp hoy = new Timestamp(fechaHoy.getTime());
                String sqlInsert = "";
                sqlInsert += "INSERT INTO [hipotecarioTAS].[dbo].[TASKioscosAuditorias] ";
                sqlInsert += "(KioscoId, FechaUltimaAuditoria, Precinto1, Precinto2, Lote,  TipoCierre)  ";
                sqlInsert += "VALUES (?,?,?,?,?,?)";
                int rtaSqlInsert = Sql.update(contexto, "hipotecariotas", sqlInsert, idTas, hoy, precinto1, precinto2, lote, tipoCierre);
                if (rtaSqlInsert == 0) {
                    throw new Exception("no se pudo ingresar Registro de Auditoria por cierre parcial");
                }
            }
            return rtaSql;
        } catch (Exception e) {
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            LogTAS.error(contexto, e);
            return error;
        }
    }

    public static Objeto getFechaUltimaAuditoriaVuelco(ContextoTAS contexto, TASKiosco kiosco) {
        try {
            String sql = "SELECT FechaUltimaAuditoria FROM TASKioscosAuditorias AS KA ";
            sql += "WHERE KA.KioscoId IN (SELECT KioscoId FROM TASKioscos AS TK WHERE TK.DireccionIP = ?) ";
            sql += "AND KA.tipoCierre = 'diario'";
            Objeto rtaSql = Sql.select(contexto, "hipotecariotas", sql, kiosco.getKioscoIp());
            Objeto response = new Objeto();
            if (rtaSql.isEmpty() || rtaSql.string("FechaUltimaAuditoria") == null) {
                response.set("estado", "SIN_RESULTADOS");
                return response;
            } else {
                response.set("estado", "OK");
                response.set("respuesta", rtaSql);
                return response;
            }
        } catch (Exception e) {
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            LogTAS.error(contexto, e);
            return error;
        }
    }

    /*
    * "select sum(d.CantidadRetenidos) as cantidadRetenidos from TASKioscosDepositos d "
				+ "where d.KioscoId = ? and d.EstadoDeposito='B' "
				+ "and d.FechaDeposito between ? and ?";
    * */
    public static Objeto getCantBilletesRetenidos(ContextoTAS contexto, int idKiosco, Date fechaAuditoria, Date fechaUltimaAuditoria) {
        try {
            Timestamp fechaCierreActual = new Timestamp(fechaAuditoria.getTime());
            Timestamp fechaUltimoCierre = new Timestamp(fechaUltimaAuditoria.getTime());
            String sql = "";
            sql += "SELECT SUM(d.CantidadRetenidos) AS cantidadRetenidos FROM [hipotecarioTAS].[dbo].[TASKioscosDepositos] d ";
            sql += "WHERE d.KioscoId = ? and d.EstadoDeposito='B' ";
            sql += "AND d.FechaDeposito BETWEEN ? AND ?";

            Objeto rtaSql = Sql.select(contexto, "hipotecariotas", sql, idKiosco, fechaUltimoCierre, fechaCierreActual);
            Objeto response = new Objeto();
            response.set("estado", "OK");
            response.set("respuesta", rtaSql);
            return response;
        } catch (Exception e) {
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            LogTAS.error(contexto, e);
            return error;
        }
    }

    /*
    * String sqlString = "select sum(d.ImporteTotalEfectivo) as importe, count(*) as cantidad from TASKioscosDepositos d "
				+ "where d.KioscoId = ? and d.TipoDestino=? and d.CodigoMoneda=? "
				+ "and (d.FechaDeposito between ? and ?) and (d.EstadoDeposito=? or d.EstadoDeposito=?) ";
    * */
    public static Objeto getTotalDepositos(ContextoTAS contexto, int idKiosco, String tipoDestino, Date fechaUltimoCierre, Date fechaCierreActual, String estado, String moneda) {
        try {
            SimpleDateFormat spd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String fechaUltimoCierreFormat = spd.format(fechaUltimoCierre);
            String fechaCierreActualFormat = spd.format(fechaCierreActual);
            String sql = "";
            sql += "SELECT SUM(d.ImporteTotalEfectivo) AS importe, COUNT(*) AS cantidad FROM TASKioscosDepositos d ";
            sql += "WHERE d.KioscoId = ? AND d.TipoDestino= ? AND d.CodigoMoneda=? ";
            sql += "AND (d.FechaDeposito BETWEEN ? AND ?) AND (d.EstadoDeposito=? OR d.EstadoDeposito=?) ";
            String tipoEstado = estado.equals("Y") ? "D" : estado;
            Objeto rtaSql = Sql.select(contexto, "hipotecariotas", sql, idKiosco, tipoDestino, moneda, fechaUltimoCierreFormat, fechaCierreActualFormat, estado, tipoEstado);
            Objeto response = new Objeto();
            if (rtaSql.objetos().size() > 0) {
                response.set("estado", "OK");
                response.set("respuesta", rtaSql);
                return response;
            } else {
                response.set("estado", "SIN_RESULTADOS");
                return response;
            }
        } catch (Exception e) {
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            LogTAS.error(contexto, e);
            return error;
        }
    }

    /*
    * "select sum(d.ImporteTotalEfectivo) as importe, count(*) as cantidad from TASKioscosDepositos d "
				+ "join TASKioscosDepositosDestinoCuenta dc on dc.DepositoId=d.DepositoId "
				+ "where d.KioscoId = ? and (d.EstadoDeposito=? or d.EstadoDeposito=?) and d.TipoDestino=? "
				+ "and d.CodigoMoneda=? and dc.TipoCuenta = ? "
				+ "and d.FechaDeposito between ? and ?";
    * */
    public static Objeto getTotalDepositos(ContextoTAS contexto, int idKiosco, String tipoDestino, Date fechaUltimoCierre, Date fechaCierreActual, String estado, String moneda, String tipoCuenta) {
        try {
            SimpleDateFormat spd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String fechaUltimoCierreFormat = spd.format(fechaUltimoCierre);
            String fechaCierreActualFormat = spd.format(fechaCierreActual);
            String sql = "";
            sql += "SELECT SUM(d.ImporteTotalEfectivo) AS importe, COUNT(*) AS cantidad FROM TASKioscosDepositos d ";
            sql += "JOIN TASKioscosDepositosDestinoCuenta dc ON dc.DepositoId=d.DepositoId ";
            sql += "WHERE d.KioscoId = ? AND (d.EstadoDeposito= ? OR d.EstadoDeposito= ?) AND d.TipoDestino= ? ";
            sql += "AND d.CodigoMoneda= ? AND dc.TipoCuenta = ? AND d.FechaDeposito BETWEEN ? AND ?";
            Objeto rtaSql = Sql.select(contexto, "hipotecariotas", sql, idKiosco, estado, "D", tipoDestino, moneda, tipoCuenta, fechaUltimoCierreFormat, fechaCierreActualFormat);
            Objeto response = new Objeto();
            if (rtaSql.objetos().size() > 0) {
                response.set("estado", "OK");
                response.set("respuesta", rtaSql);
                return response;
            } else {
                response.set("estado", "SIN_RESULTADOS");
                return response;
            }
        } catch (Exception e) {
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            LogTAS.error(contexto, e);
            return error;
        }
    }

    //todo: ex metodo para consultar NSR ver si se usa en TASi
    public static Objeto getMontosDepositosVuelco(ContextoTAS contexto, TASKiosco kiosco, Date fechaUltimoCierre, Date fechaCierreActual, String estado, String operacion, String tipo) {
        try {
            SimpleDateFormat spd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String fechaUltimoCierreFormat = spd.format(fechaUltimoCierre);
            String fechaCierreActualFormat = spd.format(fechaCierreActual);
            String sql = "";
            sql += "SELECT SUM(ImporteTotalEfectivo) as Total FROM TASKioscosDepositos ";
            sql += "WHERE KioscoId = ? AND FechaDeposito BETWEEN ? AND ? ";
            sql += "AND (EstadoDeposito = ?) AND TipoDestino = ? AND DepositoInteligente<>'S' ";
            String tipoMoneda = tipo.equals("PESOS") ? "AND CodigoMoneda = '$'" : "AND CodigoMoneda = 'U$S'";
            sql += tipoMoneda;
            String tipoDestino = "";
            switch (operacion) {
                case "EFECTIVO":
                    tipoDestino = "C";
                    break;
                case "PAGOTARJETA":
                    tipoDestino = "T";
                    break;
                case "PAGOPRESTAMO":
                    tipoDestino = "P";
                    break;
                default:
                    tipoDestino = "";
                    break;
            }

            Objeto rtaSql = Sql.select(contexto, "hipotecariotas", sql, kiosco.getKioscoId(), fechaUltimoCierreFormat, fechaCierreActualFormat, estado, tipoDestino);
            Objeto response = new Objeto();
            if (rtaSql.objetos().size() > 0) {
                response.set("estado", "OK");
                response.set("respuesta", rtaSql);
                return response;
            } else {
                response.set("estado", "SIN_RESULTADOS");
                return response;
            }
        } catch (Exception e) {
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            LogTAS.error(contexto, e);
            return error;
        }
    }

    /*
    *sqlString = "UPDATE TASKioscosAuditorias " +
                    "SET FechaUltimaAuditoria = ?, Lote = ?, Precinto1 = ?, Precinto2 = ? " +
                    "WHERE KioscoId = ? and TipoCierre = ?";
    * */
    public static Objeto updateAuditoria(ContextoTAS contexto, TASKiosco kiosco, Date fechaCierreActual, String numeroPrecinto1, String numeroPrecinto2, String lote, String tipoCierre) {
        Connection conn = null;
        Objeto obj = new Objeto();
        try {
            conn = contexto.dataSource("hipotecariotas").getConnection();
            conn.setAutoCommit(false);

            Integer rta = 0;
            int idTas = kiosco.getKioscoId();
            Timestamp fechaCierreActualFormat = new Timestamp(fechaCierreActual.getTime());

            String sql = "";
            sql += "UPDATE TASKioscosAuditorias SET FechaUltimaAuditoria = ?, Lote = ?, Precinto1 = ?, Precinto2 = ? ";
            sql += "WHERE KioscoId = ? and TipoCierre = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setTimestamp(1, fechaCierreActualFormat);
            ps.setString(2, lote);
            ps.setString(3, numeroPrecinto1);
            ps.setString(4, numeroPrecinto2);
            ps.setInt(5, idTas);
            ps.setString(6, tipoCierre);

            rta = ps.executeUpdate();
            conn.commit();

            return obj.set("estado", rta != 0 ? "true" : "false");
        } catch (SQLException e) {
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
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                obj.set("estado", "ERROR");
                obj.set("error", e);
                return obj;
            }
        }
    }

    /*
    * sqlString = "INSERT INTO TASKioscosAuditoriasLog " +
                    "(KioscoId, FechaAuditoria, FechaAuditoriaDesde, NumeroDocumento, TipoDocumento) " +
                    "VALUES (?,?,?,?,?)";
    * */
    public static Objeto insertLogAuditoria(ContextoTAS contexto, TASKiosco kiosco, Date fechaUltimoCierre, Date fechaCierreActual, TASClientePersona cliente) {
        Connection conn = null;
        Objeto obj = new Objeto();
        try {
            conn = contexto.dataSource("hipotecariotas").getConnection();
            conn.setAutoCommit(false);

            Integer rta = 0;
            int idTas = kiosco.getKioscoId();
            Timestamp fechaUltimoCierreFormat = new Timestamp(fechaUltimoCierre.getTime());
            Timestamp fechaCierreActualFormat = new Timestamp(fechaCierreActual.getTime());
            String numeroDocumento = cliente.getNumeroDocumento();
            String tipoDocumento = cliente.getTipoDocumento();

            String sql = "";
            sql += "INSERT INTO TASKioscosAuditoriasLog ";
            sql += "(KioscoId, FechaAuditoria, FechaAuditoriaDesde, NumeroDocumento, TipoDocumento) VALUES (?,?,?,?,?)";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idTas);
            ps.setTimestamp(2, fechaCierreActualFormat);
            ps.setTimestamp(3, fechaUltimoCierreFormat);
            ps.setString(4, numeroDocumento);
            ps.setString(5, tipoDocumento);

            rta = ps.executeUpdate();
            conn.commit();

            return obj.set("estado", rta != 0 ? "true" : "false");
        } catch (SQLException e) {
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
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                obj.set("estado", "ERROR");
                obj.set("error", e);
                return obj;
            }
        }
    }

    /*
    * sqlString = "UPDATE TASKioscosAuditoriasLog " +
                    "SET Precinto1 = ?, Precinto2 = ?, Lote = ?, TipoCierre = ? " +
                    "WHERE KioscoId = ? AND FechaAuditoria = ?";
    * */
    public static Objeto updateLogAuditoria(ContextoTAS contexto, TASKiosco kiosco, String numeroPrecinto1, String numeroPrecinto2, String lote, String tipoCierre) {
        Connection conn = null;
        Objeto obj = new Objeto();
        try {
            conn = contexto.dataSource("hipotecariotas").getConnection();
            conn.setAutoCommit(false);

            Integer rta = 0;
            int idTas = kiosco.getKioscoId();
            Date fechaCierre = obtenerFechaInsertada(contexto, idTas);
            if (fechaCierre == null) throw new SQLException("Auditoria Actual no se grabo adecuadamente");
            Timestamp fechaCierreActualFormat = new Timestamp(fechaCierre.getTime());
            String sql = "";
            sql += "UPDATE TASKioscosAuditoriasLog  SET Precinto1 = ?, Precinto2 = ?, Lote = ?, TipoCierre = ? ";
            sql += "WHERE KioscoId = ? AND FechaAuditoria = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, numeroPrecinto1);
            ps.setString(2, numeroPrecinto2);
            ps.setString(3, lote);
            ps.setString(4, tipoCierre);
            ps.setInt(5, idTas);
            ps.setTimestamp(6, fechaCierreActualFormat);

            rta = ps.executeUpdate();
            conn.commit();

            return obj.set("estado", rta != 0 ? "true" : "false");
        } catch (SQLException e) {
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
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                obj.set("estado", "ERROR");
                obj.set("error", e);
                return obj;
            }
        }
    }

    private static Date obtenerFechaInsertada(ContextoTAS contexto, int kioscoId) {
        try {
            String sql = "";
            sql += "SELECT TOP(1) [KioscoId],[FechaAuditoria] FROM TASKioscosAuditoriasLog ";
            sql += "WHERE KioscoId = ? ORDER BY FechaAuditoria DESC";

            Objeto rtaSql = Sql.select(contexto, "hipotecariotas", sql, kioscoId);
            String fechaInsertada = rtaSql.objetos().get(0).string("FechaAuditoria");
            long dateLong = Long.parseLong(fechaInsertada);
            Date fechaDate = new Date(dateLong);
            return fechaDate;
        } catch (Exception e) {
            LogTAS.error(contexto, e);
            return null;
        }
    }

    public static Objeto actualizarLote(ContextoTAS contexto, TASKiosco kiosco) {
        Connection conn = null;
        Objeto obj = new Objeto();
        try {
            conn = contexto.dataSource("hipotecariotas").getConnection();
            conn.setAutoCommit(false);
            String sql = "{call obtenerLote(?,?,?)}";

            int kioscoId = kiosco.getKioscoId();
            int sucursalId = kiosco.getSucursalId();

            int rta = 0;
            CallableStatement cs = conn.prepareCall(sql);
            cs.setInt(1, kioscoId);
            cs.setInt(2, sucursalId);
            cs.registerOutParameter(3, 4);

            rta = cs.executeUpdate();
            conn.commit();
            obj.set("estado", rta != 0 ? "true" : "false");
            obj.set("loteSalida", cs.getInt(3));

            return obj;
        } catch (SQLException e) {
            LogTAS.error(contexto, e);
            obj.set("estado", "ERROR");
            obj.set("error", e);
            return obj;
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                obj.set("estado", "ERROR");
                obj.set("error", e);
                return obj;
            }
        }
    }

    /*
    * sqlString = "SELECT tk.kioscoId, C.TipoCuenta, C.NumeroCuenta, D.CodigoMoneda, D.ImporteTotalEfectivo, D.KioscoId, D.SucursalId, D.Precinto, D.Lote, D.NumeroTicket, D.TipoDestino, D.EstadoDeposito, D.ProcessId, D.FechaDeposito "
					+ "FROM TASKioscosDepositos D join TASKioscosDepositosDestinoCuenta C on C.DepositoId=d.DepositoId "
					+ "JOIN TASKioscos tk on tk.kioscoId=d.kioscoId "
					+ "WHERE (C.KioscoId = ?) "
					+ "AND (D.codigoRetorno = ?) "
					+ "AND (D.tipoDestino = ?) "
					+ "AND (D.EstadoDeposito = ?) ";
    * */
    public static List<TASDatosDepositoReversa> getDepositosEfectivoReversa(ContextoTAS contexto, int kioscoId, String codigoRetorno) {
        try {
            List<TASDatosDepositoReversa> listDepositos = new ArrayList<>();
            TASDatosDepositoReversa datosDepositoReversa = new TASDatosDepositoReversa();
            String sqlTipoC = "";
            sqlTipoC += "SELECT tk.kioscoId, C.TipoCuenta, C.NumeroCuenta, D.CodigoMoneda, D.ImporteTotalEfectivo, D.KioscoId, D.SucursalId, D.Precinto, D.Lote, D.NumeroTicket, D.TipoDestino, D.EstadoDeposito, D.ProcessId, D.FechaDeposito ";
            sqlTipoC += "FROM TASKioscosDepositos D JOIN TASKioscosDepositosDestinoCuenta C ON C.DepositoId=d.DepositoId JOIN TASKioscos tk ON tk.kioscoId=d.kioscoId ";
            sqlTipoC += "WHERE (C.KioscoId = ?) AND (D.codigoRetorno = ?) ";
            sqlTipoC += "AND (D.tipoDestino = ?) AND (D.EstadoDeposito = ?)";

            Objeto rtaTipoC = Sql.select(contexto, "hipotecariotas", sqlTipoC, kioscoId, codigoRetorno, "C", "A");
            if(rtaTipoC.objetos().size() > 0){
                for(Objeto rta : rtaTipoC.objetos()){
                    datosDepositoReversa = completarDatosDepositoEfectivo(rta);
                    listDepositos.add(datosDepositoReversa);
                }
            }else {
                return null;
            }
            return listDepositos;
        } catch (Exception e) {
            return null;
        }
    }
    private static TASDatosDepositoReversa completarDatosDepositoEfectivo(Objeto rta){
        TASDatosDepositoReversa response = new TASDatosDepositoReversa();
        response.setTipoCuenta(rta.string("TipoCuenta"));
        response.setNumeroCuenta(rta.string("NumeroCuenta"));
        response.setCodigoMoneda(rta.string("CodigoMoneda"));
        response.setImporteTotalEfectivo(rta.string("ImporteTotalEfectivo"));
        response.setKioscoId(rta.string("kioscoId"));
        response.setSucursalId(rta.string("SucursalId"));
        response.setPrecinto(rta.string("Precinto"));
        response.setLote(rta.string("Lote"));
        response.setNumeroTicket(rta.string("NumeroTicket"));
        response.setIdReversa(rta.string("ProcessId"));
        String fecha = rta.string("FechaDeposito");
        long fechaLong = Long.parseLong(fecha);
        response.setFecha(new Date(fechaLong));
        return  response;
    }
    /*
    * sqlString = "SELECT tk.kioscoId, T.NumeroCuentaTarjeta, T.TipoTarjeta, D.CodigoMoneda, D.ImporteTotalEfectivo, D.KioscoId, D.SucursalId, D.Precinto, D.Lote, D.NumeroTicket, D.TipoDestino, D.EstadoDeposito, D.ProcessId, D.FechaDeposito "
					+ "FROM TASKioscosDepositos D join TASKioscosDepositosDestinoTarjeta T on t.depositoId=d.depositoId "
					+ "JOIN TASKioscos tk on tk.kioscoId=d.kioscoId "
					+ "WHERE (D.KioscoId = ?) "
					+ "AND (D.codigoRetorno = ?) "
					+ "AND (D.tipoDestino = ?) "
					+ "AND (D.EstadoDeposito = ?) ";
    * */
    public static List<TASDatosPagoTarjetaReversa> getDepositosTarjetaReversa(ContextoTAS contexto, int kioscoId, String codigoRetorno){
        try {
            List<TASDatosPagoTarjetaReversa> listDepositos = new ArrayList<>();
            TASDatosPagoTarjetaReversa datosDepositoReversa = new TASDatosPagoTarjetaReversa();
            String sqlTipoT = "";
            sqlTipoT += "SELECT tk.kioscoId, T.NumeroCuentaTarjeta, T.TipoTarjeta, D.CodigoMoneda, D.ImporteTotalEfectivo, D.KioscoId, D.SucursalId, D.Precinto, D.Lote, D.NumeroTicket, D.TipoDestino, D.EstadoDeposito, D.ProcessId, D.FechaDeposito ";
            sqlTipoT += "FROM TASKioscosDepositos D JOIN TASKioscosDepositosDestinoTarjeta T ON t.depositoId=d.depositoId  ";
            sqlTipoT += "JOIN TASKioscos tk ON tk.kioscoId=d.kioscoId WHERE (D.KioscoId = ?) ";
            sqlTipoT += "AND (D.codigoRetorno = ?) AND (D.tipoDestino = ?) AND (D.EstadoDeposito = ?)";

            Objeto rtaTipoT = Sql.select(contexto, "hipotecariotas", sqlTipoT, kioscoId, codigoRetorno, "T", "A");
            if(rtaTipoT.objetos().size() > 0){
                for(Objeto rta : rtaTipoT.objetos()){
                    datosDepositoReversa = completarDatosDepositoTarjeta(rta);
                    listDepositos.add(datosDepositoReversa);
                }
            }else {
                return null;
            }
            return listDepositos;
        } catch (Exception e) {
            return null;
        }
    }

    private static TASDatosPagoTarjetaReversa completarDatosDepositoTarjeta(Objeto rta){
        TASDatosPagoTarjetaReversa datosPagoTarjeta = new TASDatosPagoTarjetaReversa();
        datosPagoTarjeta.setNumeroCuentaTarjeta(rta.string("NumeroCuentaTarjeta"));
        datosPagoTarjeta.setCodigoMoneda(rta.string("CodigoMoneda"));
        datosPagoTarjeta.setImporteTotalEfectivo(rta.string("ImporteTotalEfectivo"));
        datosPagoTarjeta.setKioscoId(rta.string("kioscoId"));
        datosPagoTarjeta.setSucursalId(rta.string("SucursalId"));
        datosPagoTarjeta.setPrecinto(rta.string("Precinto"));
        datosPagoTarjeta.setLote(rta.string("Lote"));
        datosPagoTarjeta.setNumeroTicket(rta.string("NumeroTicket"));
        datosPagoTarjeta.setIdReversa(rta.string("ProcessId"));
        String fecha = rta.string("FechaDeposito");
        long fechaLong = Long.parseLong(fecha);
        datosPagoTarjeta.setFecha(new Date(fechaLong));
        return datosPagoTarjeta;
    }
    /*
    *sqlString = "SELECT tk.kioscoId, D.ImporteTotalEfectivo, D.KioscoId, D.SucursalId, D.Precinto, D.Lote, D.NumeroTicket, D.TipoDestino, P.NumeroPrestamo,  D.EstadoDeposito, D.ProcessId, D.FechaDeposito "
					+ "FROM TASKioscosDepositos D join TASKioscosDepositosDestinoPrestamo P on p.depositoId=d.depositoId "
					+ "JOIN TASKioscos tk on tk.kioscoId=d.kioscoId "
					+ "WHERE (D.KioscoId = ?) "
					+ "AND (D.codigoRetorno = ?) "
					+ "AND (D.tipoDestino = ?) "
					+ "AND (D.EstadoDeposito = ?) ";
    * */
    public static List<TASDatosPagoPrestamo> getDepositosPrestamo(ContextoTAS contexto, int kioscoId, String codigoRetorno){
        try {
            List<TASDatosPagoPrestamo> listpagos = new ArrayList<>();
            TASDatosPagoPrestamo datosPagoPrestamo = new TASDatosPagoPrestamo();
            String sqlTipoP = "";
            sqlTipoP += "SELECT tk.kioscoId, D.ImporteTotalEfectivo, D.KioscoId, D.SucursalId, D.Precinto, D.Lote, D.NumeroTicket, D.TipoDestino, P.NumeroPrestamo,  D.EstadoDeposito, D.ProcessId, D.FechaDeposito ";
            sqlTipoP += "FROM TASKioscosDepositos D JOIN TASKioscosDepositosDestinoPrestamo P ON p.depositoId=d.depositoId ";
            sqlTipoP += "JOIN TASKioscos tk ON tk.kioscoId=d.kioscoId WHERE (D.KioscoId = ?) ";
            sqlTipoP += "AND (D.codigoRetorno = ?) AND (D.tipoDestino = ?) AND (D.EstadoDeposito = ?) ";

            Objeto rtaTipoP = Sql.select(contexto, "hipotecariotas", sqlTipoP, kioscoId, codigoRetorno, "P", "P");
            if(rtaTipoP.objetos().size() > 0){
                for(Objeto rta : rtaTipoP.objetos()){
                    datosPagoPrestamo = completarDatosPagoPrestamo(rta);
                    listpagos.add(datosPagoPrestamo);
                }
            }else {
                return null;
            }
            return listpagos;
        } catch (Exception e) {
            return null;
        }
    }
    private static TASDatosPagoPrestamo completarDatosPagoPrestamo(Objeto rta){
        TASDatosPagoPrestamo response = new TASDatosPagoPrestamo();
        response.setNroPrestamo(rta.string("NumeroPrestamo"));
        response.setImporte(rta.string("ImporteTotalEfectivo"));
        response.setLote(rta.string("Lote"));
        response.setPrecinto(rta.string("Precinto"));
        response.setSucursalId(response.string("SucursalId"));
        response.setTas(rta.string("kioscoId"));
        response.setNumeroTicket(rta.string("NumeroTicket"));
        response.setProcessId(rta.string("ProcessId"));
        String fecha = rta.string("FechaDeposito");
        long fechaLong = Long.parseLong(fecha);
        response.setFecha(new Date(fechaLong));
        return response;
    }

    /*
    *
			sqlString = "SELECT tk.kioscoId, D.ImporteTotalEfectivo, D.KioscoId, D.SucursalId, D.Precinto, D.Lote, D.NumeroTicket, D.TipoDestino, P.NumeroPrestamo, D.EstadoDeposito, D.ProcessId, D.FechaDeposito "
					+ "FROM TASKioscosDepositos D join TASKioscosDepositosDestinoPrestamo P on p.depositoId=d.depositoId "
					+ "JOIN TASKioscos tk on tk.kioscoId=d.kioscoId "
					+ "WHERE (D.KioscoId = ?) "
					+ "AND (D.codigoRetorno = ?) "
					+ "AND (D.tipoDestino = ?) "
					+ "AND (D.EstadoDeposito = ?) ";

    * */
    public static List<TASDatosPagoPrestamoReversa> getDepositosPrestamoReversa(ContextoTAS contexto, int kioscoId, String codigoRetorno){
        try {
            List<TASDatosPagoPrestamoReversa> listpagos = new ArrayList<>();
            TASDatosPagoPrestamoReversa datosPagoPrestamo = new TASDatosPagoPrestamoReversa();
            String sqlTipoP = "";
            sqlTipoP += "SELECT tk.kioscoId, D.ImporteTotalEfectivo, D.KioscoId, D.SucursalId, D.Precinto, D.Lote, D.NumeroTicket, D.TipoDestino, P.NumeroPrestamo, D.EstadoDeposito, D.ProcessId, D.FechaDeposito ";
            sqlTipoP += "FROM TASKioscosDepositos D JOIN TASKioscosDepositosDestinoPrestamo P ON p.depositoId=d.depositoId ";
            sqlTipoP += "JOIN TASKioscos tk ON tk.kioscoId=d.kioscoId WHERE (D.KioscoId = ?) ";
            sqlTipoP += "AND (D.codigoRetorno = ?) AND (D.tipoDestino = ?) AND (D.EstadoDeposito = ?)";

            Objeto rtaTipoP = Sql.select(contexto, "hipotecariotas", sqlTipoP, kioscoId, codigoRetorno, "P", "A");
            if(rtaTipoP.objetos().size() > 0){
                for(Objeto rta : rtaTipoP.objetos()){
                    datosPagoPrestamo = completarDatosPagoPrestamoReversa(rta);
                    listpagos.add(datosPagoPrestamo);
                }
            }else {
                return null;
            }
            return listpagos;
        } catch (Exception e) {
            return null;
        }
    }

    private static TASDatosPagoPrestamoReversa completarDatosPagoPrestamoReversa(Objeto rta){
        TASDatosPagoPrestamoReversa response = new TASDatosPagoPrestamoReversa();
        response.setNroPrestamo(rta.string("NumeroPrestamo"));
        response.setImporte(rta.string("ImporteTotalEfectivo"));
        response.setLote(rta.string("Lote"));
        response.setPrecinto(rta.string("Precinto"));
        response.setSucursalId(response.string("SucursalId"));
        response.setTas(rta.string("kioscoId"));
        response.setNumeroTicket(rta.string("NumeroTicket"));
        response.setIdReversa(rta.string("ProcessId"));
        String fecha = rta.string("FechaDeposito");
        long fechaLong = Long.parseLong(fecha);
        response.setFecha(new Date(fechaLong));
        return response;
    }

    /*
    *sqlString = "SELECT tk.kioscoId, D.ProcessId, D.ImporteTotalEfectivo, D.CodigoMoneda, D.KioscoId, D.SucursalId, D.Precinto, D.Lote, D.NumeroTicket, D.TipoDestino, P.NumeroPrestamo, D.EstadoDeposito, D.FechaDeposito "
					+ "FROM TASKioscosDepositos D join TASKioscosDepositosDestinoPrestamo P on p.depositoId=d.depositoId "
					+ "JOIN TASKioscos tk on tk.kioscoId=d.kioscoId "
					+ "WHERE (D.KioscoId = ?) "
					+ "AND (D.codigoRetorno = ?) "
					+ "AND (D.tipoDestino = ?) "
					+ "AND (D.EstadoDeposito = ?) ";
    * */
    public static List<TASDatosRegistroOperacion> getDatosRegistroOperacion(ContextoTAS contexto,int kioscoId,String codigoRetorno){
        try {
            List<TASDatosRegistroOperacion> listDatos = new ArrayList<>();
            TASDatosRegistroOperacion datosRegistroOperacion = new TASDatosRegistroOperacion();
            String sqlTipoP = "";
            sqlTipoP += "SELECT tk.kioscoId, D.ProcessId, D.ImporteTotalEfectivo, D.CodigoMoneda, D.KioscoId, D.SucursalId, D.Precinto, D.Lote, D.NumeroTicket, D.TipoDestino, P.NumeroPrestamo, D.EstadoDeposito, D.FechaDeposito ";
            sqlTipoP += "FROM TASKioscosDepositos D JOIN TASKioscosDepositosDestinoPrestamo P ON p.depositoId=d.depositoId ";
            sqlTipoP += "JOIN TASKioscos tk ON tk.kioscoId=d.kioscoId WHERE (D.KioscoId = ?) ";
            sqlTipoP += "AND (D.codigoRetorno = ?) AND (D.tipoDestino = ?) AND (D.EstadoDeposito = ?)";

            Objeto rtaTipoP = Sql.select(contexto, "hipotecariotas", sqlTipoP, kioscoId, codigoRetorno, "P", "Z");
            if(rtaTipoP.objetos().size() > 0){
                for(Objeto rta : rtaTipoP.objetos()){
                    String tipoDestino = rta.string("TipoDestino");
                    datosRegistroOperacion = completarDatosRegistroOperacion(rta, tipoDestino);
                    listDatos.add(datosRegistroOperacion);
                }
            }else {
                return null;
            }
            return listDatos;
        } catch (Exception e) {
            return null;
        }
    }

    private static TASDatosRegistroOperacion completarDatosRegistroOperacion(Objeto rta, String tipoDestino){
        TASDatosRegistroOperacion response = new TASDatosRegistroOperacion();
        if(tipoDestino.equals("T")){
            response.setNroCuenta(rta.string("NumeroCuentaTarjeta"));
            response.setProducto(rta.string("ATC"));
        }else{
            response.setNroCuenta(rta.string("NumeroPrestamo"));
            response.setProducto(rta.string("CCA"));
        }
        response.setMoneda(rta.string("CodigoMoneda"));
        response.setImporte(rta.string("ImporteTotalEfectivo"));
        response.setReversa("N");
        response.setTas(rta.string("kioscoId"));
        response.setSucursal(rta.string("SucursalId"));
        response.setIdProcessReversado(null);
        response.setLote(rta.string("Lote"));
        response.setCodigoError("251106");
        response.setIdReferenciaOrigen(rta.string("ProcessId"));
        response.setPrecinto(rta.string("Precinto"));
        response.setNumeroTicket(rta.string("NumeroTicket"));
        String fecha = rta.string("FechaDeposito");
        long fechaLong = Long.parseLong(fecha);
        response.setFecha(new Date(fechaLong));
        return response;
    }

    public static Objeto actualizarEstadoDeposito(ContextoTAS contexto, String nroTicket, String kioscoId, String estado){
        Connection conn = null;
        Objeto obj = new Objeto();
        try {
            conn = contexto.dataSource("hipotecariotas").getConnection();
            conn.setAutoCommit(false);

            Integer rta = 0;
            int idTas = Integer.valueOf(kioscoId);

            String sql = "UPDATE TASKioscosDepositos SET EstadoDeposito = ? ";
            sql += "WHERE KioscoId = ? AND numeroTicket = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, estado);
            ps.setInt(2, idTas);
            ps.setString(3, nroTicket);

            rta = ps.executeUpdate();
            conn.commit();

            return obj.set("estado", rta != 0 ? "true" : "false");
        } catch (SQLException e) {
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
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                obj.set("estado", "ERROR");
                obj.set("error", e);
                return obj;
            }
        }
    }

    public static Objeto actualizarCodigoRetornoDeposito(ContextoTAS contexto, String nroTicket, String codigoRetorno){
        Connection conn = null;
        Objeto obj = new Objeto();
        try {
            conn = contexto.dataSource("hipotecariotas").getConnection();
            conn.setAutoCommit(false);

            Integer rta = 0;

            String sql = "UPDATE TASKioscosDepositos SET codigoRetorno=? WHERE numeroTicket = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, codigoRetorno);
            ps.setString(2, nroTicket);

            rta = ps.executeUpdate();
            conn.commit();

            return obj.set("estado", rta != 0 ? "true" : "false");
        } catch (SQLException e) {
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
        } finally {
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
