package ar.com.hipotecario.backend.conector.api;

import java.lang.reflect.Type;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Base;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.sql.SqlLogsSistemas;
import ar.com.hipotecario.backend.servicio.sql.SqlVisualizador;
import ar.com.hipotecario.backend.servicio.sql.visualizador.LogsMW.LogMW;

public class ApiObjeto extends Base {

	public static Gson gson = gson(true);

	/* ========== ATRIBUTOS ========== */
	protected transient Integer codigoHttp;
	protected transient String idProceso;

	/* ========== GET ========== */
	public Integer codigoHttp() {
		return codigoHttp;
	}

	public String idProceso() {
		return idProceso;
	}

	/* ========== METODOS ========== */
	public String nombreServicioMW(Contexto contexto) {
		if (!contexto.esProduccion()) {
			LogMW logMW = SqlVisualizador.logMW(contexto, idProceso).get();
			return logMW != null ? logMW.ba_serviceId : null;
		}
		return null;
	}

	public String nombreServicioVentas(Contexto contexto) {
		if (!contexto.esProduccion()) {
			return SqlLogsSistemas.logApiVenta(contexto, idProceso).get().Metodo;
		}
		return null;
	}

	/* ========== GSON ========== */
	@SuppressWarnings("rawtypes")
	public static Gson gson(Boolean serializarNull) {
		GsonBuilder gsonBuilder = gsonBuilder();
		gsonBuilder.setPrettyPrinting();
		if (serializarNull) {
			gsonBuilder.serializeNulls();
		}

		gsonBuilder.registerTypeHierarchyAdapter(ApiObjetos.class, new JsonSerializer<ApiObjetos>() {
			public JsonElement serialize(ApiObjetos objetosApi, Type typeOfSrc, JsonSerializationContext context) {
				return gson.toJsonTree(objetosApi.list());
			}
		});

		return gsonBuilder.create();
	}

	/* ========== TOSTRING ========== */
	public Objeto objeto() {
		return Objeto.fromJson(gson.toJson(this));
	}

	public String toJson() {
		return gson.toJson(this);
	}

	public String toString() {
		return gson.toJson(this);
	}

	/* ========== TEST ========== */
	protected static Contexto contexto(String canal, String ambiente) {
		return new Contexto(canal, ambiente, "1");
	}

	protected static Contexto contexto(String canal, String ambiente, String idCobis) {
		return new Contexto(canal, ambiente, idCobis);
	}

	protected static void imprimirResultado(Contexto contexto, ApiObjeto objeto) {
		Long momentoFin = new Date().getTime();
		System.out.println(objeto.nombreServicioMW(contexto));
		System.out.println(objeto instanceof ApiObjetos ? ((ApiObjetos<?>) objeto).get(0) : objeto);
		System.out.println();
		System.out.println("TIEMPO: " + (momentoFin - contexto.momentoCreacion) + " ms");
		System.out.println();
	}

	protected static void imprimirResultadoApiVentas(Contexto contexto, ApiObjeto objeto) {
		Long momentoFin = new Date().getTime();
		System.out.println(objeto.nombreServicioVentas(contexto));
		System.out.println(objeto instanceof ApiObjetos ? ((ApiObjetos<?>) objeto).get(0) : objeto);
		System.out.println();
		System.out.println("TIEMPO: " + (momentoFin - contexto.momentoCreacion) + " ms");
		System.out.println();
	}
}
