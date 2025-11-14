package ar.com.hipotecario.canal.buhobank;

import java.util.function.Consumer;

import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.ServerSentEvents;
import ar.com.hipotecario.backend.Servidor;
import ar.com.hipotecario.backend.base.Objeto;
import spark.Spark;

public class TestCGU extends Modulo {

	/* ========== MAIN ========== */
	public static void main(String[] args) throws Exception {
		Servidor.main(args);
		Spark.stop();

		String cuil = "20285779732";

		call("POST:/bb/api/sesion", x -> {
			x.parametros.set("secret", GeneralBB.VERSION_PLATAFORMA_0_0_1);
			x.parametros.set("cuil", cuil);
			x.parametros.set("numeroDocumento", cuil.substring(2, 10));
		});

		Objeto address = new Objeto();
		address.set("zipCode", "8000");
		address.set("country", "ARGENTINA");
		address.set("numberStreet", "2345");
		address.set("city", "BAHIA BLANCA");
		address.set("municipality", "BAHIA BLANCA");
		address.set("province", "BUENOS AIRES");
		address.set("streetAddress", "SAAVEDRA");
		address.set("floor", "1");
		address.set("department", "A");

		Objeto obtenervucompleto = call("POST:/bb/api/obtenerguardarvucompleto", x -> {
			x.parametros.set("cuil", cuil);
			x.parametros.set("address", address);
			x.parametros.set("birthdate", "1983-07-02");
			x.parametros.set("birthPlace", "SANTA FE");
			x.parametros.set("nationality", "ARGENTINA");
			x.parametros.set("identical", true);
			x.parametros.set("ocr", true);
			x.parametros.set("information", true);
			x.parametros.set("informationCuil", true);
		});

		call("POST:/bb/api/guardarvucompleto", x -> {
			x.parametros.set("idOperacion", obtenervucompleto.get("idOperacion"));
			x.parametros.set("confidence", obtenervucompleto.get("confidence"));
			x.parametros.set("confidenceTotal", obtenervucompleto.get("confidenceTotal"));
			x.parametros.set("ocr", obtenervucompleto.get("ocr"));
			x.parametros.set("barcode", obtenervucompleto.get("barcode"));
			x.parametros.set("confidenceDocument", obtenervucompleto.get("confidenceDocument"));
			x.parametros.set("information", obtenervucompleto.get("information"));
			x.parametros.set("identical", obtenervucompleto.get("identical"));
		});

		call("POST:/bb/api/enviarotpmail", x -> {
			x.parametros.set("mail", "agbarua@hipotecario.com.ar");
			x.parametros.set("codArea", "11");
			x.parametros.set("celular", "62597839");
		});

		call("GET:/bb/api/ofertas", x -> {

		});

		call("POST:/bb/api/elegiroferta", x -> {
			x.parametros.set("ofertaElegida", "1");
		});

		call("POST:/bb/api/guardaradicionales", x -> {
			x.parametros.set("idEstadoCivil", "S");
			x.parametros.set("idCantidadNupcias", "");
			x.parametros.set("idSubtipoEstadoCivil", "");
			x.parametros.set("idSituacionLaboral", "6");
		});

		call("POST:/bb/api/formaentrega", x -> {
			x.parametros.set("tipo", "D");
			x.parametros.set("idSucursal", 0);
		});

		call("GET:/bb/api/finalizar", x -> {

		});

		System.out.println("TEST: OK");
		System.exit(0);
	}

	/* ========== CALL ========== */
	public static Objeto call(String endpoint, Consumer<ContextoBB> funcion) {
		return call(endpoint, funcion, true);
	}

	public static Objeto call(String endpoint, Consumer<ContextoBB> funcion, Boolean lanzarExcepcion) {
		Objeto objeto = (Objeto) callObject(endpoint, funcion);
		if (lanzarExcepcion && !objeto.string("estado").equals("0")) {
			throw new RuntimeException(objeto.string("estado"));
		}
		return objeto;
	}

	public static String callString(String endpoint, Consumer<ContextoBB> funcion) {
		return (String) callObject(endpoint, funcion);
	}

	@SuppressWarnings("unchecked")
	public static ServerSentEvents<ContextoBB> callServerSentEvents(String endpoint, Consumer<ContextoBB> funcion) {
		return (ServerSentEvents<ContextoBB>) callObject(endpoint, funcion);
	}

	public static Object callObject(String endpoint, Consumer<ContextoBB> funcion) {
		ContextoBB contexto = new ContextoBB("BB", "homologacion", "0");
		funcion.accept(contexto);
		System.out.println("\nREQUEST [" + endpoint + "]: " + contexto.parametros);
		Object object = CanalBuhoBank.endpoints.get(endpoint).apply(contexto);
		System.out.println("\nRESPONSE [" + endpoint + "]: " + object);
		System.out.println("---");
		return object;
	}
}
