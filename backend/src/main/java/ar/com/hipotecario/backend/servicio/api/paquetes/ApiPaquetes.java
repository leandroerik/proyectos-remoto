package ar.com.hipotecario.backend.servicio.api.paquetes;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.servicio.api.paquetes.Paquetes.Paquete;

public class ApiPaquetes extends Api {

	public static Futuro<Paquetes> paquetes(Contexto contexto, String letraTarjeta, String numeroPaquete, Boolean empleado) {
		return futuro(() -> Paquetes.get(contexto, letraTarjeta, numeroPaquete, empleado));
	}

	public static Futuro<Paquetes> paquetesPorLetra(Contexto contexto, String letraTarjeta, Boolean empleado) {
		return futuro(() -> Paquetes.getPorLetra(contexto, letraTarjeta, empleado));
	}

	public static Futuro<Paquete> paquetePorNumero(Contexto contexto, String numeroPaquete, Boolean empleado) {
		return futuro(() -> Paquetes.getPorNumero(contexto, numeroPaquete, empleado));
	}

	public static Futuro<Costos> costos(Contexto contexto, String numeroSucursal) {
		return futuro(() -> Costos.get(contexto, numeroSucursal));
	}
}
