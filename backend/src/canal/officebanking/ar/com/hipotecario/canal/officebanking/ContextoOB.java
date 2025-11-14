package ar.com.hipotecario.canal.officebanking;

import java.util.HashMap;
import java.util.Map;

import com.zaxxer.hikari.HikariConfig;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.officebanking.jpa.JPA;
import jakarta.persistence.EntityManagerFactory;
import spark.Request;
import spark.Response;

public class ContextoOB extends Contexto {

	private static Map<String, EntityManagerFactory> entityMangerFactoryMap = new HashMap<>();

	/* ========== ATRIBUTOS ========== */
	private SesionOB sesion;
	private SesionOBAnterior sesionOBAnterior;

	/* ========== CONSTRUCTORES ========== */
	public ContextoOB(String canal, String ambiente, String idCobis) {
		super(canal, ambiente, idCobis);
	}

	public ContextoOB(Request request, Response response, String canal, String ambiente) {
		super(request, response, canal, ambiente);
	}

	/* ========== METODOS ========== */
	public ContextoOB clonar() {
		return new ContextoOB(request, response, canal, ambiente);
	}

	public SesionOB sesion() {
		if (sesion == null) {
			sesion = super.sesion(SesionOB.class);
		}
		return sesion;
	}

	public SesionOBAnterior sesionOBAnterior() {
		if (sesionOBAnterior == null) {
			sesionOBAnterior = super.sesion(SesionOBAnterior.class);
		}
		return sesionOBAnterior;
	}

	public void registrarSesion(String usuCodigo, String fingerprint) {
		try {
			String claveSesion = "SESION_OB_" + usuCodigo;
			Objeto datos = new Objeto();
			datos.set("uuid", fingerprint);
			datos.set("fechaExpiracion", Fecha.ahora().sumarMinutos(this.config.integer("ob_maximo_inactividad")));
			this.set(claveSesion, datos.toSimpleJson());
		} catch (Exception e) {
		}
	}

	/* ========== JPA ========== */
	public EntityManagerFactory entityMangerFactory(String baseDatos) {
		String clave = "backend_sql_" + baseDatos;
		if (!entityMangerFactoryMap.containsKey(clave)) {

			String valor = this.config.string(clave);
			String url = valor.split(" ")[0];
			String user = valor.split(" ")[1];
			String password = valor.split(" ")[2];

			HikariConfig config = new HikariConfig();
			if (url.startsWith("jdbc:sqlserver")) {
				config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			}
			config.setJdbcUrl(url);
			config.setUsername(user);
			config.setPassword(password);

			EntityManagerFactory emf = JPA.getInstance(clave, config).getEntityManager();

			entityMangerFactoryMap.put(clave, emf);
			return emf;
		}
		return entityMangerFactoryMap.get(clave);
	}
}
