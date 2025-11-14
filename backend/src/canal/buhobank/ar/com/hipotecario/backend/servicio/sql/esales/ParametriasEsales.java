package ar.com.hipotecario.backend.servicio.sql.esales;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.exception.SqlException;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.ParametriasEsales.ParametriaEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsales.SesionEsales;

@SuppressWarnings("serial")
public class ParametriasEsales extends SqlObjetos<ParametriaEsales> {

	/* ========== ATRIBUTOS ========== */
	public static class ParametriaEsales extends SqlObjeto {
		public String s_param_id;
		public String s_param_valor;
		public String s_param_descripcion;
	}

	/* ========== CLASES ========== */
	public static class PoliticasValidator extends SqlObjeto {
		public String cuilIntentos;
		public String cuilMinutos;
		public String ipIntentos;
		public String ipMinutos;
		public String rechazoScoringMinutos;
		public HorarioOperativo horarioOperativoDesde = new HorarioOperativo(0, 0, 0);
		public HorarioOperativo horarioOperativoHasta = new HorarioOperativo(23, 59, 59);
	}

	public static class HorarioOperativo extends SqlObjeto {
		public Integer hora;
		public Integer minuto;
		public Integer segundo;

		public HorarioOperativo(Integer hora, Integer minuto, Integer segundo) {
			this.hora = hora;
			this.minuto = minuto;
			this.segundo = segundo;
		}
	}

	/* ========== SERVICIO ========== */
	public static ParametriaEsales get(Contexto contexto, String id) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [esales].[dbo].[parametria] WITH (NOLOCK) ";
		sql += "WHERE s_param_id = ?";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, id);
		SqlException.throwIf("PARAMETRIA_NO_ENCONTRADA", datos.isEmpty());

		return map(datos, ParametriasEsales.class, ParametriaEsales.class).first();
	}

	public static Integer resultadosScoring(Contexto contexto, String cuil, Fecha fechaDeInicio, String tipo) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [esales].[dbo].[Sesion] WITH (NOLOCK) ";
		sql += "WHERE cuil = ? ";
		sql += "AND fecha_inicio >= ? ";
		sql += "AND resolucion_scoring = ? ";
		sql += "ORDER BY fecha_inicio DESC";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, cuil, fechaDeInicio, tipo);
		SqlException.throwIf("RESOLUCIONES_NO_ENCONTRADOS", datos == null);

		SesionesEsales datosMapped = map(datos, SesionesEsales.class, SesionEsales.class);
		return datosMapped.size();
	}

	public static Integer resultadosIp(Contexto contexto, String ip, Fecha fechaDeInicio) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [esales].[dbo].[Sesion] s WITH (NOLOCK) ";
		sql += "WHERE s.ip = ? ";
		sql += "AND fecha_inicio >= ?";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, ip, fechaDeInicio);
		SqlException.throwIf("RESULTADOS_IP_NO_ENCONTRADOS", datos == null);

		SesionesEsales datosMapped = map(datos, SesionesEsales.class, SesionEsales.class);
		return datosMapped.size();
	}

	public static Integer resultadosCuil(Contexto contexto, String cuil, Fecha fechaDeInicio) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [esales].[dbo].[Sesion] s WITH (NOLOCK) ";
		sql += "WHERE s.cuil = ? ";
		sql += "AND fecha_inicio >= ?";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, cuil, fechaDeInicio);
		SqlException.throwIf("RESULTADOS_CUIL_NO_ENCONTRADOS", datos == null);

		SesionesEsales datosMapped = map(datos, SesionesEsales.class, SesionEsales.class);
		return datosMapped.size();
	}

	public static PoliticasValidator politicas(Contexto contexto) {
		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [esales].[dbo].[parametria] WITH (NOLOCK) ";
		sql += "WHERE s_param_id LIKE 'politicas.%'";

		Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql);
		SqlException.throwIf("PARAMETRIAS_NO_ENCONTRADAS", datos.isEmpty());

		ParametriasEsales parametrias = map(datos, ParametriasEsales.class, ParametriaEsales.class);

		PoliticasValidator politicas = new PoliticasValidator();
		for (ParametriaEsales param : parametrias) {
			String paramId = param.s_param_id;
			String paramVal = param.s_param_valor;

			if ("politicas.hist.cuil.intentos".equals(paramId)) {
				politicas.cuilIntentos = paramVal;
			}

			if ("politicas.hist.cuil.minutos".equals(paramId)) {
				politicas.cuilMinutos = paramVal;
			}

			if ("politicas.hist.ip.intentos".equals(paramId)) {
				politicas.ipIntentos = paramVal;
			}

			if ("politicas.hist.ip.minutos".equals(paramId)) {
				politicas.ipMinutos = paramVal;
			}

			if ("politicas.hist.re.minutos".equals(paramId)) {
				politicas.rechazoScoringMinutos = paramVal;
			}

			if ("politicas.horario.desde".equals(paramId)) {
				String[] desdeArr = paramVal.split(":");
				if (desdeArr != null && desdeArr.length >= 2) {
					Integer horaDesde = Util.integer(desdeArr[0]);
					Integer minutoDesde = Util.integer(desdeArr[1]);
					Integer segundoDesde = desdeArr.length == 3 ? Util.integer(desdeArr[3]) : 0;
					politicas.horarioOperativoDesde = new HorarioOperativo(horaDesde, minutoDesde, segundoDesde);
				}
			}

			if ("politicas.horario.hasta".equals(paramId)) {
				String[] hastaArr = paramVal.split(":");
				if (hastaArr != null && hastaArr.length >= 2) {
					Integer horaHasta = Util.integer(hastaArr[0]);
					Integer minutoHasta = Util.integer(hastaArr[1]);
					Integer segundoHasta = hastaArr.length == 3 ? Util.integer(hastaArr[3]) : 0;
					politicas.horarioOperativoHasta = new HorarioOperativo(horaHasta, minutoHasta, segundoHasta);
				}
			}
		}

		return politicas;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("BB", "homologacion");
		String test = "politicas";

		if ("get".equals(test)) {
			ParametriaEsales datos = get(contexto, "preaprobado_fecha_tope");
			imprimirResultado(contexto, datos);
		}

		if ("politicas".equals(test)) {
			PoliticasValidator datos = politicas(contexto);
			imprimirResultado(contexto, datos);
		}

		if ("ipRegistros".equals(test)) {
			Integer registros = resultadosIp(contexto, "10.99.87.114", Fecha.ahora().restarMeses(3));
			System.out.println("RegistrosIp: " + registros);
		}

		if ("cuilRegistros".equals(test)) {
			Integer registros = resultadosCuil(contexto, "27345231973", Fecha.ahora().restarMeses(3));
			System.out.println("RegistrosCuil: " + registros);
		}

		if ("resultadosScoring".equals(test)) {
			Integer registros = resultadosScoring(contexto, "27345231973", Fecha.ahora().restarMeses(3), "RE");
			System.out.println("ResultadosScoring: " + registros);
		}
	}
}
