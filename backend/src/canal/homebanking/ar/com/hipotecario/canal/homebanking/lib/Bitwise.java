package ar.com.hipotecario.canal.homebanking.lib;

/**
 * Libreria cortita y al pie para operaciones sobre bits.
 * 
 * https://es.wikipedia.org/wiki/Operador_a_nivel_de_bits
 * 
 * @author C06470
 *
 */
public class Bitwise {

	/**
	 * Chequea si el resultado de una operación bitwise es verdadero (> 0) o falso
	 * (== 0)
	 * 
	 * @param x el valor a corroborar
	 * @return true si x es mayor a cero
	 */
	public static boolean esVerdadero(int x) {
		return x > 0;
	}

	/**
	 * corrobora si la bandera x está activada dentro de xs
	 * 
	 * @param xs banderas
	 * @param x  bandera a chequear
	 * @return true si la bandera x se encuentra activa en xs
	 */
	public static boolean banderaActiva(int xs, int x) {
		return esVerdadero(xs & x);
	}

	/**
	 * activa la bandera x dentro de xs y devuelve el resultado
	 * 
	 * @param xs banderas
	 * @param x  bandera a activar
	 * @return xs con x activo
	 */
	public static int activarBandera(int xs, int x) {
		return (xs | x);
	}

	/**
	 * desactiva la bandera x dentro de xs y devuelve el resultado
	 * 
	 * @param xs banderas
	 * @param x  bandera a desactivar
	 * @return xs con x desactivado
	 */
	public static int desactivarBandera(int xs, int x) {
		return (xs & (~x));
	}
}
