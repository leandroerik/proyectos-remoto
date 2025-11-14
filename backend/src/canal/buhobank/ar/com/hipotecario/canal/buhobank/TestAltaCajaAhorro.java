package ar.com.hipotecario.canal.buhobank;

import java.util.function.Consumer;

import ar.com.hipotecario.backend.Servidor;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.util.CuitUtil;
import spark.Spark;

public class TestAltaCajaAhorro {

	/* ========== MAIN ========== */
	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
		Servidor.main(args);
		Spark.stop();

		String cuil = CuitUtil.generarCuitAzar("M", "F");

		Objeto crearSesion = call("POST:/bb/api/sesion", x -> {
			x.parametros.set("secret", GeneralBB.VERSION_PLATAFORMA_0_0_1);
			x.parametros.set("cuil", cuil);
			x.parametros.set("numeroDocumento", cuil.substring(2, 10));
			x.parametros.set("tipo", "PESOS");
		});

		Objeto esCliente = call("POST:/bb/api/escliente", x -> {
			x.parametros.set("cuil", cuil);
		});

		System.out.println("TEST: OK");
		System.exit(0);
	}

	/* ========== CALL ========== */
	public static Objeto call(String endpoint, Consumer<ContextoBB> funcion) {
		return call(endpoint, funcion, true);
	}

	public static Objeto call(String endpoint, Consumer<ContextoBB> funcion, Boolean lanzarExcepcion) {
		ContextoBB contexto = new ContextoBB("BB", "homologacion", "0");
		funcion.accept(contexto);
		System.out.println("\nREQUEST [" + endpoint + "]: " + contexto.parametros);
		Objeto objeto = (Objeto) CanalBuhoBank.endpoints.get(endpoint).apply(contexto);
		System.out.println("\nRESPONSE [" + endpoint + "]: " + objeto);
		System.out.println("---");
		if (lanzarExcepcion && !objeto.string("estado").equals("0")) {
			throw new RuntimeException(objeto.string("estado"));
		}
		return objeto;
	}
}
