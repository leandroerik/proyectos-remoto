package ar.com.hipotecario.canal.homebanking.conector;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.lib.Texto;
import ar.gabrielsuarez.glib.G;

public class ApiResponse extends Objeto {

	private static final long serialVersionUID = 1L;

	private static Logger log = LoggerFactory.getLogger(ApiResponse.class);

	/* ========== ATRIBUTOS ========== */
	public Integer codigo;
	public String json;
	public Map<String, String> headers = new LinkedHashMap<>();

	/* ========== CONSTRUCTORES ========== */
	public ApiResponse() {
		this.codigo = 200;
	}

	public ApiResponse(ApiRequest request, Integer codigo, String json) {
		this.codigo = codigo;
		this.json = json;
		try {
			absorber(fromJson(json));
		} catch (Exception e) {
			if (request != null) {
				if (!ConfigHB.bool("kibana", false)) {
					log.error(Texto.hora() + " idCobis: " + request.contexto.idCobis() + "", e);
				} else {
					Objeto registro = new Objeto();
					registro.set("idCobis", request.contexto.idCobis());
					registro.set("tipo", "error");
					registro.set("idProceso", request.idProceso());
					registro.set("fecha", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
					registro.set("raw", json);
					registro.set("error", Texto.stackTrace(e));
					log.info(registro.toString().replace("\n", ""));
				}
			}
			absorber(fromJson(json));
		}
	}

	/* ========== METODOS ========== */
	public Boolean hayError() {
		return codigo < 200 || codigo > 300;
	}

	/* ========== LOG ========== */
	public String log(ApiRequest request) {
		return log(request, null);
	}

	public String log(ApiRequest request, String origen) {
		Boolean kibana = ConfigHB.bool("kibana", false);
		StringBuilder log = new StringBuilder();

		if (!kibana) {
			log.append(Texto.hora()).append(" API RESPONSE ");
			if (origen != null) {
				log.append("(").append(origen).append(")");
			}
			log.append(" [").append(request.servicio).append(", idCobis: ").append(request.idCobis()).append(", idProceso: ").append(request.idProceso()).append(", http: ").append(this.codigo).append("]\n");
			log.append(json).append("\n");
		}

		if (kibana) {
			Objeto registro = new Objeto();
			registro.set("idCobis", request.idCobis());
			registro.set("tipo", "response");
			registro.set("servicio", request.servicio);
			registro.set("http", codigo);
			registro.set("idProceso", request.idProceso());
			registro.set("origen", origen);
			registro.set("fecha", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			registro.set("response", json);
			return registro.toString().replace("\n", "");
		}

		return log.toString();
	}

	public String rawLog(ApiRequest request, Boolean encriptar) {
		StringBuilder log = new StringBuilder();

		log.append(Texto.hora()).append(" API RESPONSE ");
		log.append(" [").append(request.servicio).append(", idCobis: ").append(request.idCobis()).append(", idProceso: ").append(request.idProceso()).append(", http: ").append(this.codigo).append("]\r\n");
		log.append(json).append("\r\n");

		return log.toString();
	}

	public String serializar() {
		return G.toJson(this);
	}

	public static ApiResponse deserializar(String json) {
		return G.fromJson(json, ApiResponse.class);
	}
}
