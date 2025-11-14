package ar.com.hipotecario.backend.servicio.sql;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.servicio.sql.hb_be.*;
import ar.com.hipotecario.backend.servicio.sql.hb_be.TokensOB.TokenOB;
import ar.com.hipotecario.backend.servicio.sql.hb_be.UsuariosOBAnterior.UsuarioOBAnterior;
import ar.com.hipotecario.backend.servicio.sql.intercambioate.NovedadesATE;

public class SqlHB_BE extends Sql {

	/* ========== SERVICIOS ========== */
	public static Futuro<Boolean> logOB(Contexto contexto, String empresa, String usuario, String endpoint, String evento, String datos, String idProceso, String error) {
		return futuro(() -> LogsOfficeBanking.post(contexto, empresa, usuario, endpoint, evento, datos, idProceso, error));
	}

	public static Futuro<UsuarioOBAnterior> usuarioEmpresaOBAnteriorPorUsuarioYCuit(Contexto contexto, String usuario, Long cuit) {
		return futuro(() -> UsuariosOBAnterior.getPorUsuarioYCuit(contexto, usuario, cuit));
	}

	public static Futuro<UsuarioOBAnterior> usuarioOBAnteriorPorCuil(Contexto contexto, String cuil) {
		return futuro(() -> UsuariosOBAnterior.getPorCuil(contexto, cuil));
	}

	public static Futuro<UsuarioOBAnterior> usuarioOBAnteriorPorCuityCuil(Contexto contexto, String cuil, Long cuit) {
		return futuro(() -> UsuariosOBAnterior.getPorCuilYCuit(contexto, cuil, cuit));
	}

	public static Futuro<UsuariosEmpresasOBAnterior> usuarioEmpresasOBAnteriorPorCuil(Contexto contexto, String cuil) {
		return futuro(() -> UsuariosEmpresasOBAnterior.getPorCuil(contexto, cuil));
	}

	public static Futuro<TokenOB> tokenOB(Contexto contexto, String uuid) {
		return futuro(() -> TokensOB.get(contexto, uuid));
	}

	public static Futuro<Boolean> crearTokenOB(Contexto contexto, String uuid, Long cuit, String cuil, Fecha fechaExpiracion, String usu_login) {
		return futuro(() -> TokensOB.crear(contexto, uuid, cuit.toString(), cuil, fechaExpiracion, usu_login));
	}

	public static Futuro<Boolean> eliminarTokenOB(Contexto contexto, Long cuit, String cuil) {
		return futuro(() -> TokensOB.eliminar(contexto, cuit.toString(), cuil));
	}

	public static Futuro<CuentasOBAnterior> cuentaOBPorCuit(Contexto contexto, String cuil) {
		return futuro(() -> CuentasOBAnterior.getPorCuit(contexto, cuil));
	}

	public static Futuro<LogsOfficeBanking> selectPorFecha(Contexto contexto, Fecha fecha) {
		return futuro(() -> LogsOfficeBanking.selectPorFecha(contexto, fecha));
	}
	public static Futuro<LogsOfficeBanking> selectPorFecha(Contexto contexto, String fecha1,String fecha2,String cuitEmpresa) {
		return futuro(() -> LogsOfficeBanking.selectPorFecha(contexto, fecha1,fecha2,cuitEmpresa));
	}

	public static Futuro<LogsOfficeBanking> selectPorUsuarioOrEmpresa(Contexto contexto, String usuario, String empresa) {
		return futuro(() -> LogsOfficeBanking.selectPorUsuarioOrEmpresa(contexto, usuario, empresa));
	}

	public static Futuro<Boolean> registrarIntentoRiesgonet(Contexto contexto, Long emp_cuit, String usu_cuil, int exitoso) {
		return futuro(() -> PreguntasRiesgoNetOB.registrarIntento(contexto, emp_cuit.toString(), usu_cuil, exitoso));
	}

	public static Futuro<Boolean> consultarUsuarioBloqueado(Contexto contexto, String usu_cuil) {
		return futuro(() -> PreguntasRiesgoNetOB.consultarUsuarioBloqueado(contexto, usu_cuil));
	}

	public static Futuro<NovedadesATE> novedadesATE(Contexto contexto, String canal, int inicio, int fin, String estadosFinales, String estadoFaltaDeFondos) {
		return futuro(() -> NovedadesATE.get(contexto, canal, inicio, fin, estadosFinales, estadoFaltaDeFondos));
	}
	public static Futuro<Boolean> logCrm(Contexto contexto, int empresa, int usuario, String operacion,String usuario_crm) {
		return futuro(() -> LogsCrm.post(contexto, empresa, usuario, operacion, usuario_crm));
	}

}