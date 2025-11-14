package ar.com.hipotecario.canal.tas;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.shared.modulos.kiosco.modelos.TASKiosco;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import spark.Request;
import spark.Response;

import javax.sql.DataSource;

public class ContextoTAS extends Contexto {

	/* ========== ATRIBUTOS ========== */
	public String _uuid_;
	private static Map<String, DataSource> dataSourcesMap = new HashMap<>();
	private SesionTAS sesion;
	private static Map<Integer, TASKiosco> kioscos = new ConcurrentHashMap<>();

	/* ========== CONSTRUCTORES ========== */
	public ContextoTAS(Request request, Response response, String canal, String ambiente) {
		super(request, response, canal, ambiente);
	}

	/* ========== METODOS ========== */
	public String parametrosOfuscados() {
		Objeto parametrosOfuscados = new Objeto();
		for (String clave : parametros.keys()) {
			if (clave.contains("pass") || clave.contains("clave")) {
				parametrosOfuscados.set(clave, "***");
			} else {
				parametrosOfuscados.set(clave, parametros.get(clave));
			}
		}
		return parametrosOfuscados.toSimpleJson();
	}

	public String idSesion(){
		if (request != null) {
			String uuid = request.headers("uuid");
			if (_uuid_ != null) {
				uuid = _uuid_;
			}
			return (uuid != null && !uuid.isEmpty()) ? uuid : request.session().id();
		}
		return "1";
	}

	public SesionTAS sesion() {
		if (sesion == null) {
			sesion = super.sesion(SesionTAS.class);
		}
		return sesion;
	}
	@Override
	public String subCanal(){
		return this.sesion.idTas;
	}

	public static void crearSesionKiosco(TASKiosco kiosco) {
		kioscos.put(kiosco.getKioscoId(), kiosco);
	}

	public SesionTAS getSesion() {
		return sesion;
	}

	public void setSesion(SesionTAS sesion) {
		this.sesion = sesion;
	}

	public static Map<Integer, TASKiosco> getKioscos() {
		return kioscos;
	}

	public TASKiosco getKioscoContexto(Integer key){
		Map<Integer, TASKiosco> kioscos= this.getKioscos();
		TASKiosco kiosco = kioscos.get(key);
		return kiosco;
	}

	public static void setKioscos(Map<Integer, TASKiosco> kioscos) {
		ContextoTAS.kioscos = kioscos;
	}

	public DataSource dataSourceCamposSeparados(String baseDatos) {
		String clave = "backend_sql_" + baseDatos + "_url";
		if (!dataSourcesMap.containsKey(clave)) {
			String url = this.config.string("backend_sql_" + baseDatos + "_url");
			if(baseDatos.equals("tashbs")){
				baseDatos = "hipotecariotas";
			}
			String user = this.config.string("backend_sql_" + baseDatos + "_usuario");
			String password = this.config.string("backend_sql_" + baseDatos + "_clave");

			HikariConfig config = new HikariConfig();
			if (url.startsWith("jdbc:sqlserver")) {
				config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			}
			if (url.startsWith("jdbc:teradata")) {
				config.setDriverClassName("com.teradata.jdbc.TeraDriver");
			}
			config.setJdbcUrl(url);
			config.setUsername(user);
			config.setPassword(password);

			DataSource dataSource = new HikariDataSource(config);
			dataSourcesMap.put(clave, dataSource);
			return dataSource;
		}
		return dataSourcesMap.get(clave);
	}
}
