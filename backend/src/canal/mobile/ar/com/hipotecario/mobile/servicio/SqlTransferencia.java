package ar.com.hipotecario.mobile.servicio;

import java.util.Date;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;

public class SqlTransferencia {

	public final static String INSERT_QUERY = "INSERT INTO [hbs].[dbo].[auditor_aumento_transferencia] ([momento],[diaSolicitud],[cobis],[idProceso],[ip],[canal],[codigoError],[descripcionError],[cuenta],[limite],[moneda],[fechaProgramada]) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
	public final static String GET_BY_CREATION_DAY_QUERY = "SELECT [id],[momento],[cobis],[idProceso],[ip],[canal],[codigoError],[descripcionError],[cuenta],[limite],[moneda],[fechaProgramada] FROM [hbs].[dbo].[auditor_aumento_transferencia] WHERE COBIS = ? AND diaSolicitud = ? AND cuenta = ? ";
	public final static String DELETE_AGENDA_BENEFICIARIO = "DELETE FROM [Hbs].[dbo].[agenda_transferencias] WHERE nro_cliente = ? AND (cbu_destino = ? OR nro_cuenta_destino = ?)";
	public final static String GET_AGENDA_BENEFICIARIO = "SELECT * FROM [Hbs].[dbo].[agenda_transferencias] WHERE nro_cliente = ? AND (cbu_destino = ? OR nro_cuenta_destino = ?)";
	public final static String GET_SCHECULED_CONTACTS = "SELECT * FROM [Hbs].[dbo].[agenda_transferencias] WHERE nro_cliente = ? ORDER BY id DESC";

	public SqlResponseMB insertSolicitudLimiteTransferencia(Date momento, Date diaSolicitud, String idCobis, String idProceso, String ip, String canal, String codigoError, String descError, String numeroCuenta, Double limite, String codigoMoneda, Date fechaProgramada) {
		SqlRequestMB sqlRequest = SqlMB.request("InsertAuditorLimiteTransferenciaHB", "hbs");
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
		return SqlMB.response(sqlRequest);
	}

	public SqlResponseMB obtenerSolicitudPorDiaCreacion(String cobis, String fechaCreacion, String idCuenta) {
		SqlRequestMB sqlRequest = SqlMB.request("ObtenetAuditorLimiteTransferencia", "hbs");
		sqlRequest.sql = GET_BY_CREATION_DAY_QUERY;
		sqlRequest.add(cobis);
		sqlRequest.add(fechaCreacion);
		sqlRequest.add(idCuenta);
		return SqlMB.response(sqlRequest);
	}

	public SqlResponseMB eliminarAgendaTransferencia(String cobis, String idAgenda) {
		SqlRequestMB sqlRequest = SqlMB.request("DeleteAgendaTransferencias", "hbs");
		sqlRequest.sql = DELETE_AGENDA_BENEFICIARIO;
		sqlRequest.parametros.add(cobis);
		sqlRequest.parametros.add(idAgenda);
		sqlRequest.parametros.add(idAgenda);
		return SqlMB.response(sqlRequest);
	}

	public SqlResponseMB obtenerAgendaTransferencia(String cobis, String idAgenda) {
		SqlRequestMB sqlRequest = SqlMB.request("Select2AgendaTransferencias", "hbs");
		sqlRequest.sql = GET_AGENDA_BENEFICIARIO;
		sqlRequest.parametros.add(cobis);
		sqlRequest.parametros.add(idAgenda);
		sqlRequest.parametros.add(idAgenda);
		return SqlMB.response(sqlRequest);
	}

	public SqlResponseMB obtenerAgendaCuit(String cobis, String cuit) {
		SqlRequestMB sqlRequest = SqlMB.request("Select2AgendaTransferencias", "hbs");
		sqlRequest.sql = "SELECT * FROM [Hbs].[dbo].[agenda_transferencias] " +
				"WHERE nro_cliente = ? " +
				"AND documento_beneficiario = ?";
		sqlRequest.parametros.add(cobis);
		sqlRequest.parametros.add(cuit);

		return SqlMB.response(sqlRequest);
	}

	public SqlResponseMB contactosAgendados(String cobis) {
		SqlRequestMB sqlRequest = SqlMB.request("SelectAgendaTransferencias", "hbs");
		sqlRequest.sql = GET_SCHECULED_CONTACTS;
		sqlRequest.parametros.add(cobis);
		return SqlMB.response(sqlRequest);
	}

	public SqlResponseMB actualizarComentarioContactoAgendado(String titular, String id) {
		SqlRequestMB sqlRequest = SqlMB.request("UpdateAgendaTransferenciasComentario", "hbs");
		sqlRequest.sql = "UPDATE [Hbs].[dbo].[agenda_transferencias] SET comentario = ? WHERE id = ?";
		sqlRequest.parametros.add(titular);
		sqlRequest.parametros.add(id);
		return SqlMB.response(sqlRequest);

	}

	public SqlResponseMB obtenerContactoAgendadoById(String cobis, String cbuDestino, String cuentaDestino) {
		SqlRequestMB sqlRequestConsulta = SqlMB.request("Select2AgendaTransferencias", "hbs");
		sqlRequestConsulta.sql = "SELECT * FROM [Hbs].[dbo].[agenda_transferencias] WHERE nro_cliente = ? AND (cbu_destino = ? OR nro_cuenta_destino = ?)";
		sqlRequestConsulta.parametros.add(cobis);
		sqlRequestConsulta.parametros.add(cbuDestino);
		sqlRequestConsulta.parametros.add(cuentaDestino);
		return SqlMB.response(sqlRequestConsulta);
	}

	public void insertarDatosComprobanteTransferencia(ContextoMB contexto, String idComprobante, String nombreBanco, String cuentaNumero) {
		SqlRequestMB sqlRequest = SqlMB.request("InsertarDatosComprobante", "homebanking");

		sqlRequest.sql = "EXEC [homebanking].[dbo].[InsertarDatosComprobanteTransferencia] @id_cobis = ?, @id_comprobante= ?, @nombre_banco = ?, @cuenta_numero = ?";

		sqlRequest.parametros.add(contexto.idCobis());
		sqlRequest.parametros.add(idComprobante);
		sqlRequest.parametros.add(nombreBanco);
		sqlRequest.parametros.add(cuentaNumero);

		new Futuro<>(() -> SqlMB.response(sqlRequest));
	}

	public SqlResponseMB obtenerDatosComprobanteTransferencia(ContextoMB contexto, String idComprobante) {
		SqlRequestMB sqlRequest = SqlMB.request("ObtenerDatosComprobante", "homebanking");

		sqlRequest.sql = "EXEC [homebanking].[dbo].[ObtenerDatosComprobanteTransferencia] @id_cobis = ?, @id_comprobante= ?";

		sqlRequest.parametros.add(contexto.idCobis());
		sqlRequest.parametros.add(idComprobante);

		return SqlMB.response(sqlRequest);
	}

	public void actualizarFaltantesAgenda(String cobis, String idAgenda, String cuil, String nombre, String cbu, String idMoneda) {
		String query = "UPDATE [Hbs].[dbo].[agenda_transferencias] ";
		query += "SET documento_beneficiario = ? , titular = ? , cbu_destino = ? , ";
		query += "moneda_cuenta_destino = ? ";
		query += "WHERE nro_cliente = ? AND id = ? ";

		SqlRequestMB sqlRequest = SqlMB.request("UpdateFaltantesAgenda", "hbs");
		sqlRequest.sql = query;
		sqlRequest.parametros.add(cuil);
		sqlRequest.parametros.add(nombre);
		sqlRequest.parametros.add(cbu);
		sqlRequest.parametros.add(idMoneda);
		sqlRequest.parametros.add(cobis);
		sqlRequest.parametros.add(idAgenda);
		SqlMB.response(sqlRequest);
	}

	public void actualizarAlias(String cobis, String idAgenda, String alias) {
		String query = "UPDATE [Hbs].[dbo].[agenda_transferencias] ";
		query += "SET alias = ? ";
		query += "WHERE nro_cliente = ? AND id = ? ";

		SqlRequestMB sqlRequest = SqlMB.request("UpdateFaltantesAgenda", "hbs");
		sqlRequest.sql = query;
		sqlRequest.parametros.add(alias);
		sqlRequest.parametros.add(cobis);
		sqlRequest.parametros.add(idAgenda);
		SqlMB.response(sqlRequest);
	}

	public SqlResponseMB actualizarApodoAgendado(String cobis, String cuil, String apodo) {
		String query = "UPDATE [Hbs].[dbo].[agenda_transferencias] SET apodo = ? ";
		query += "WHERE nro_cliente = ? AND documento_beneficiario = ? ";

		SqlRequestMB sqlRequest = SqlMB.request("UpdateAgendaApodo", "hbs");
		sqlRequest.sql = query;
		sqlRequest.parametros.add(apodo);
		sqlRequest.parametros.add(cobis);
		sqlRequest.parametros.add(cuil);
		return SqlMB.response(sqlRequest);
	}

}
