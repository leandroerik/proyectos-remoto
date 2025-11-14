package ar.com.hipotecario.backend.cron;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.com.hipotecario.backend.Config;

public class CronConfig {

	private static Logger log = LoggerFactory.getLogger(CronConfig.class);

	/* ========== ATRIBUTOS PRIVADOS ========== */
	private static Boolean iniciado = false;
//	private static Boolean iniciadoPendientes = false;
	private static Integer intervaloSegundos = 60;
	private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	/* ========== ATRIBUTOS PUBLICOS ========== */
	public static Map<String, String> config = new HashMap<>();

	/* ========== INICIALIZACION ========== */
	synchronized public static void iniciar() {
		if (Config.esOpenShift()) {
			if (!iniciado) {
				tarea();
				Long stepMiliseconds = intervaloSegundos * 1000L;
				Long initialDelay = stepMiliseconds - new Date().getTime() % stepMiliseconds;
				scheduler.scheduleAtFixedRate(() -> tarea(), initialDelay, stepMiliseconds, TimeUnit.MILLISECONDS);
				iniciado = true;
			}
		}
	}

	/*
	 * synchronized public static void iniciarPendientesBB() { if
	 * (iniciadoPendientes) return;
	 * 
	 * if (GeneralBB.PRENDIDO_TEST_CRON) { Long stepMiliseconds =
	 * GeneralBB.INTERVALO_SEG_TEST_CRON * 1000L; Long initialDelay =
	 * stepMiliseconds - new Date().getTime() % stepMiliseconds;
	 * 
	 * ContextoBB contexto = new ContextoBB(GeneralBB.CANAL_CODIGO,
	 * Config.ambiente(), "1"); scheduler.scheduleAtFixedRate(() ->
	 * BBAlta.finalizarPendientes(contexto), initialDelay, stepMiliseconds,
	 * TimeUnit.MILLISECONDS); log.info(Fecha.ahora().string("[HH:mm:ss] ") +
	 * GeneralBB.CANAL_CODIGO +
	 * " - Proceso de prueba cron iniciado con intervalo de " +
	 * GeneralBB.INTERVALO_SEG_TEST_CRON + " segundos"); iniciadoPendientes = true;
	 * return; }
	 * 
	 * String hora = GeneralBB.HORA_PROCESO; String minuto =
	 * GeneralBB.MINUTO_PROCESO; String segundo = "00"; String horario = hora + ":"
	 * + minuto + ":" + segundo;
	 * 
	 * Fecha fechaAhora = Fecha.ahora(); Fecha fechaProxima = new Fecha(horario,
	 * "HH:mm:ss"); Boolean antesDeFecha =
	 * fechaAhora.antesDeLas(fechaProxima.hora(), fechaProxima.minuto(),
	 * fechaProxima.segundo()); Integer add = antesDeFecha ? 0 : 1;
	 * 
	 * fechaProxima = fechaProxima.sumarDias(Math.abs(fechaProxima.dia() -
	 * fechaAhora.dia()) + add).sumarAños(Math.abs(fechaProxima.año() -
	 * fechaAhora.año())).sumarMeses(Math.abs(fechaProxima.mes() -
	 * fechaAhora.mes())); Integer duration = fechaAhora.esAnterior(fechaProxima) ?
	 * fechaAhora.segundosFaltantes(fechaProxima) :
	 * fechaProxima.segundosFaltantes(fechaAhora); Long initialDelay =
	 * Long.valueOf(duration); Long delay = TimeUnit.DAYS.toSeconds(1);
	 * 
	 * ContextoBB contexto = new ContextoBB(GeneralBB.CANAL_CODIGO,
	 * Config.ambiente(), "1"); scheduler.scheduleWithFixedDelay(() ->
	 * BBAlta.finalizarPendientes(contexto), initialDelay, delay, TimeUnit.SECONDS);
	 * log.info(Fecha.ahora().string("[HH:mm:ss] ") + GeneralBB.CANAL_CODIGO +
	 * " - Proceso de alta automatica de registros de sesion iniciado con delay inicial a las "
	 * + horario + " hs e intervalo de " + delay + " segundos"); iniciadoPendientes
	 * = true; }
	 */

	/* ========== TAREA ========== */
	private static void tarea() {
		try {
			CronConfig.config = config();
		} catch (SQLException e) {
			log.error(e.getMessage());
		}
	}

	/* ========== METODOS PRIVADOS ========== */
	private static Map<String, String> config() throws SQLException {
		Map<String, String> config = new HashMap<>();
		try (Connection conexion = conexion()) {
			String sql = "SELECT clave, valor FROM [buhobank].[dbo].[_config]";
			try (PreparedStatement ps = conexion.prepareStatement(sql)) {
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						String clave = rs.getString("clave");
						String valor = rs.getString("valor");
						config.put(clave, valor);
					}
				}
			}
		}
		return config;
	}

	private static Connection conexion() throws SQLException {
		String valor = Config.basedatos();
		if (valor == null) {
			throw new SQLException();
		}

		String url = valor.split(" ")[0];
		String user = valor.split(" ")[1];
		String password = valor.split(" ")[2];

		return DriverManager.getConnection(url, user, password);
	}
}
