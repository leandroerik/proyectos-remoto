package ar.com.hipotecario.canal.homebanking;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.Cron.CronJob;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;

public class CacheHB extends Contexto {

	public static SqlResponse mockServicios;

	public static class CronMockServicios extends CronJob {
		public CronMockServicios load() {
			if (!"produccion".equals(Config.ambiente())) {
				try {
					SqlRequest sqlRequest = Sql.request("mock", "homebanking");
					sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[mock] ORDER BY condiciones DESC";
					CacheHB.mockServicios = Sql.response(sqlRequest);
				} catch (Exception e) {
				}
			}
			return this;
		}

		public void run() {
			load();
		}
	}

	public static class CronEstadisticas extends CronJob {
		public CronEstadisticas load() {
			CanalHomeBanking.mapaContadorOK.clear();
        	CanalHomeBanking.mapaContadorErrores.clear();
			return this;
		}

		public void run() {
			load();
		}
	}
}
