package ar.com.hipotecario.mobile.test;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.lib.Archivo;
import ar.com.hipotecario.mobile.negocio.Cuenta;

public abstract class GeneradorArchivoCoelsa {
	private static String rutaCobis = "C:\\Users\\C05302\\Desktop\\cobise.txt";
	private static String rutaSalida = "C:\\Users\\C05302\\Desktop\\salida.txt";
	private static Integer i = 1;

	public static List<String> lista() {
		String cobis = Archivo.leer(rutaCobis);
		List<String> lista = Arrays.asList(cobis.replace("\r", "").split("\\n"));
		return lista;
	}

	public static void main(String[] args) throws InterruptedException {
		Archivo.escribir(rutaSalida, "");
		inicio();
//		ExecutorService executorService = Concurrencia.executorService(5);
		for (String cobis : lista()) {
//			executorService.submit(() -> {
			try {
				String idCobis = cobis;
				ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
//				contexto.parametros.set("dummy", true);
				for (Cuenta cuenta : contexto.cuentas()) {
					agregarCuenta(contexto, cuenta);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
//			});
		}
		fin();
//		Concurrencia.esperar(executorService, null, Integer.MAX_VALUE);
	}

	public static void inicio() {
		String linea = "";
		linea += "002000000001H044";
		linea += new SimpleDateFormat("yyyyMMdd").format(new Date());
		linea += "10000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
		Archivo.agregar(rutaSalida, linea);
	}

	public static void agregarCuenta(ContextoMB contexto, Cuenta cuenta) {
		String cbu = cuenta.cbu();
		String cuit = contexto.persona().cuit();
		if (cbu == null || cbu.length() != 22) {
			return;
		}
		if (cuit == null || cuit.length() != 11) {
			return;
		}
//		if (!cuenta.unipersonal()) {
//			return;
//		}
		++i;
		String linea = "";
		linea += llenarIzquierda(i.toString(), "0", 9);
		linea += "D044";
		linea += cbu;
		linea += cuenta.esPesos() ? "032" : "840";
		linea += cuenta.esPesos() ? (cuenta.esCajaAhorro() ? "10" : "20") : (cuenta.esCajaAhorro() ? "11" : "21");
		linea += "AF01";
		linea += cuit;
		linea += llenarDerecha(contexto.persona().nombreCompleto().toUpperCase(), " ", 40);
		linea += "00000000000                                        00000000000                                        000";
		Archivo.agregar(rutaSalida, linea);
	}

	public static void fin() {
		++i;
		String linea = "";
		linea += llenarIzquierda(i.toString(), "0", 9);
		linea += "T044";
		linea += new SimpleDateFormat("yyyyMMdd").format(new Date());
		linea += llenarIzquierda(i.toString(), "0", 9);
		linea += "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
		Archivo.agregar(rutaSalida, linea);
	}

	public static String llenarIzquierda(String valor, String relleno, Integer cantidad) {
		while (valor.length() < cantidad) {
			valor = relleno + valor;
		}
		return valor;
	}

	public static String llenarDerecha(String valor, String relleno, Integer cantidad) {
		while (valor.length() < cantidad) {
			valor = valor + relleno;
		}
		return valor;
	}
}
