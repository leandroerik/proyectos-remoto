package ar.com.hipotecario.canal.homebanking.servicio;

import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaDebito;

public class TarjetaDebitoService {

	public static ApiResponse tarjetaDebitoGet(ContextoHB contexto, String numero) {
		ApiRequest request = Api.request("TarjetaDebitoGet", "tarjetasdebito", "GET",
				"/v1/tarjetasdebito/{nro_tarjeta}", contexto);
		request.path("nro_tarjeta", numero);
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis(), numero);
	}

	public static ApiResponse tarjetaDebitoGetCuentasAsociadas(ContextoHB contexto, String idProducto) {
		TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idProducto);
		ApiRequest request = Api.request("CuentasAsociadasTarjetaDebito", "tarjetasdebito", "GET",
				"/v1/tarjetasdebito/{nro_tarjeta}/cuentas", contexto);
		request.path("nro_tarjeta", tarjetaDebitoGet(contexto, tarjetaDebito.numero()).string("numeroTarjeta"));
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis(), tarjetaDebito.numero());
	}

	public static TarjetaDebito tarjetaAsociada(ContextoHB contexto, Cuenta cuenta) {
		List<TarjetaDebito> tarjetas = new ArrayList<>();
		for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
			if (tarjetaDebito.activacionTemprana()) {
				continue;
			}
			ApiResponse response = tarjetaDebitoGetCuentasAsociadas(contexto, tarjetaDebito.id());
			for (Objeto item : response.objetos()) {
				if (item.bool("principal") && item.string("numero").equals(cuenta.numero())) {
					tarjetas.add(tarjetaDebito);
				}
			}
		}

		if (!tarjetas.isEmpty()) {
			return contexto.tarjetaDebitoPorDefecto(tarjetas);
		}

		for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
			if (tarjetaDebito.activacionTemprana()) {
				continue;
			}
			ApiResponse response = tarjetaDebitoGetCuentasAsociadas(contexto, tarjetaDebito.id());
			for (Objeto item : response.objetos()) {
				if (item.string("numero").equals(cuenta.numero())) {
					tarjetas.add(tarjetaDebito);
				}
			}
		}

		if (!tarjetas.isEmpty()) {
			return contexto.tarjetaDebitoPorDefecto(tarjetas);
		}

		return null;
	}

	public static TarjetaDebito tarjetaAsociadaHabilitadaLink(ContextoHB contexto, Cuenta cuenta,
			Boolean buscarTDVirtuales) {
		List<TarjetaDebito> tarjetas = new ArrayList<>();
		for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
			if (tarjetaDebito.activacionTemprana()) {
				continue;
			}
			if (buscarTDVirtuales && !tarjetaDebito.virtual()) {
				continue;
			}
			if (!buscarTDVirtuales && tarjetaDebito.virtual()) {
				continue;
			}
			ApiResponse response = tarjetaDebitoGetCuentasAsociadas(contexto, tarjetaDebito.id());
			for (Objeto item : response.objetos()) {
				if (item.string("numero").equals(cuenta.numero())) {
					if (tarjetaDebito.habilitadaLink()) {
						tarjetas.add(tarjetaDebito);
					}
				}
			}
		}

		if (tarjetas.isEmpty()) {
			for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
				if (tarjetaDebito.activacionTemprana()) {
					continue;
				}
				if (buscarTDVirtuales && !tarjetaDebito.virtual()) {
					continue;
				}
				if (!buscarTDVirtuales && tarjetaDebito.virtual()) {
					continue;
				}
				ApiResponse response = tarjetaDebitoGetCuentasAsociadas(contexto, tarjetaDebito.id());
				for (Objeto item : response.objetos()) {
					if (item.bool("principal") && item.string("numero").equals(cuenta.numero())) {
						tarjetas.add(tarjetaDebito);
					}
				}
			}
		}

		if (!tarjetas.isEmpty()) {
			return contexto.tarjetaDebitoPorDefecto(tarjetas);
		}

		for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
			if (tarjetaDebito.activacionTemprana()) {
				continue;
			}
			if (buscarTDVirtuales && !tarjetaDebito.virtual()) {
				continue;
			}
			if (!buscarTDVirtuales && tarjetaDebito.virtual()) {
				continue;
			}
			ApiResponse response = tarjetaDebitoGetCuentasAsociadas(contexto, tarjetaDebito.id());
			for (Objeto item : response.objetos()) {
				if (item.string("numero").equals(cuenta.numero())) {
					tarjetas.add(tarjetaDebito);
				}
			}
		}

		if (!tarjetas.isEmpty()) {
			return contexto.tarjetaDebitoPorDefecto(tarjetas);
		}

		return null;
	}

	public static ApiResponse tarjetaDebitoBlanquearPil(ContextoHB contexto, String numero) {
		ApiRequest request = Api.request("TarjetaDebitoBlanquearPil", "tarjetasdebito", "DELETE",
				"/v1/tarjetasdebito/{nrotarjeta}/pil", contexto);
		request.path("nrotarjeta", numero);
		request.query("digitoverificador", "0");
		request.query("numeromiembro", "0");
		request.query("numeroversion", "0");
		return Api.response(request, contexto.idCobis(), numero);
	}

	public static ApiResponse tarjetaDebitoBlanquearPin(ContextoHB contexto, String numero) {
		ApiRequest request = Api.request("TarjetaDebitoBlanquearPin", "tarjetasdebito", "DELETE",
				"/v1/tarjetasdebito/{nrotarjeta}/pin", contexto);
		request.path("nrotarjeta", numero);
		request.query("digitoverificador", "0");
		request.query("numeromiembro", "0");
		request.query("numeroversion", "0");
		return Api.response(request, contexto.idCobis(), numero);
	}

	public static ApiResponse tarjetaDebitoGetEstado(ContextoHB contexto, String numero) {
		ApiRequest request = Api.request("TarjetaDebitoGetEstado", "tarjetasdebito", "GET",
				"/v1/tarjetasdebito/{nrotarjeta}/estado", contexto);
		request.path("nrotarjeta", numero);
		request.query("digitoverificador", "0");
		request.query("numeromiembro", "0");
		request.query("numeroversion", "0");
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis(), numero);
	}

	public static ApiResponse habilitarTarjetaDebito(ContextoHB contexto, String numero) {
		ApiRequest request = Api.request("TarjetaDebitoActivar", "tarjetasdebito", "PATCH",
				"/v1/tarjetasdebito/{nrotarjeta}/habilitar", contexto);
		request.path("nrotarjeta", numero);
		request.query("digitoverificador", "0");
		request.query("numeromiembro", "0");
		request.query("numeroversion", "0");
		return Api.response(request, contexto.idCobis(), numero);
	}

	public static ApiResponse tarjetaDebitoVirtualToFisica(ContextoHB contexto, TarjetaDebito td) {
		ApiRequest request = Api.request("ActualizacionTarjetaDebito", "tarjetasdebito", "PATCH", "/v1/tarjetasdebito",
				contexto);

		Objeto tdModel = new Objeto();
		tdModel.set("CLIENTE", contexto.idCobis());
		tdModel.set("TARJETA", td.numero());
		tdModel.set("TIPO_TARJETA", td.idTipoTarjeta());
		tdModel.set("VIRTUAL", "N");
		request.body(tdModel);

		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse pausadoTarjetaDebito(ContextoHB contexto, TarjetaDebito td, boolean pausar) {
		ApiRequest request = Api.request("ActualizacionTarjetaDebito", "tarjetasdebito", "PATCH", "/v1/tarjetasdebito",
				contexto);

		Objeto tdModel = new Objeto();
		tdModel.set("CLIENTE", contexto.idCobis());
		tdModel.set("TARJETA", td.numero());
		tdModel.set("TIPO_TARJETA", td.idTipoTarjeta());
		tdModel.set("PAUSAR", pausar ? "S" : "N");
		request.body(tdModel);

		return Api.response(request, contexto.idCobis());
	}
	
	public static ApiResponse avisarViajeExterior(ContextoHB contexto, String nroTarjeta) {
		ApiRequest request = Api.request("TarjetasDebitoAvisoExterior", "tarjetasdebito", "POST",
                "/v1/tarjetasdebito/{nrotarjeta}/avisoviajeexterior", contexto);

        request.path("nrotarjeta", nroTarjeta);
        request.header("x-canal", "HB");
        request.cacheSesion = true;

		return Api.response(request, contexto.idCobis(), nroTarjeta);
	}
}
