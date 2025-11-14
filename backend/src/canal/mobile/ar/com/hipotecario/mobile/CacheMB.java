package ar.com.hipotecario.mobile;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.Cron.CronJob;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;

public class CacheMB extends Contexto {

	public static SqlResponseMB mockServicios;

	public static class CronMockServicios extends CronJob {
		public CronMockServicios load() {
			if (!"produccion".equals(Config.ambiente())) {
				try {
					SqlRequestMB sqlRequest = SqlMB.request("mock", "homebanking");
					sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[mock] ORDER BY condiciones DESC";
					CacheMB.mockServicios = SqlMB.response(sqlRequest);
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
			CanalMobile.mapaContadorOK.clear();
        	CanalMobile.mapaContadorErrores.clear();
			return this;
		}

		public void run() {
			load();
		}
	}

	public static class CronSuperCache extends CronJob {
		public CronSuperCache load() {
			CanalMobile.superCache.clear();
			return this;
		}
		public void run() {
			load();
		}
	}
}
