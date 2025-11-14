package ar.com.hipotecario.mobile.servicio;

import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.TarjetaDebito;

public class TarjetaDebitoService {

	public static ApiResponseMB tarjetaDebito(ContextoMB contexto, Boolean cancelados) {
		ApiRequestMB request = ApiMB.request("TarjetasDebito", "tarjetasdebito", "GET", "/v1/tarjetasdebito", contexto);
		request.query("idcliente", contexto.idCobis());
		request.query("cancelados", cancelados.toString());
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB tarjetasDebitoGet(ContextoMB contexto, Boolean cancelados) {
		ApiRequestMB request = ApiMB.request("TarjetaDebito", "tarjetasdebito", "GET", "/v1/tarjetasdebito?cancelados={_cancelados}&idcliente={_id_cobis}", contexto);
		request.path("_cancelados", cancelados.toString());
		request.path("_id_cobis", contexto.idCobis());
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB tarjetaDebitoGet(ContextoMB contexto, String numero) {
		ApiRequestMB request = ApiMB.request("TarjetaDebito", "tarjetasdebito", "GET", "/v1/tarjetasdebito/{nro_tarjeta}", contexto);
		request.path("nro_tarjeta", numero);
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis(), numero);
	}

	public static ApiResponseMB tarjetaDebitoGetCuentasAsociadas(ContextoMB contexto, String idProducto) {
		TarjetaDebito tarjetaDebito = contexto.tarjetaDebito(idProducto);
		ApiRequestMB request = ApiMB.request("CuentasAsociadasTarjetaDebito", "tarjetasdebito", "GET", "/v1/tarjetasdebito/{nro_tarjeta}/cuentas", contexto);
		request.path("nro_tarjeta", tarjetaDebitoGet(contexto, tarjetaDebito.numero()).string("numeroTarjeta"));
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis(), tarjetaDebito.numero());
	}

	public static TarjetaDebito tarjetaAsociada(ContextoMB contexto, Cuenta cuenta) {
		List<TarjetaDebito> tarjetas = new ArrayList<>();
		for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {

			if (tarjetaDebito.activacionTemprana()) {
				continue;
			}

			ApiResponseMB response = tarjetaDebitoGetCuentasAsociadas(contexto, tarjetaDebito.id());
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
			ApiResponseMB response = tarjetaDebitoGetCuentasAsociadas(contexto, tarjetaDebito.id());
			for (Objeto item : response.objetos()) {

				if (tarjetaDebito.activacionTemprana()) {
					continue;
				}

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

	public static TarjetaDebito tarjetaAsociadaHabilitadaLink(ContextoMB contexto, Cuenta cuenta, Boolean buscarTDVirtuales) {
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
			ApiResponseMB response = tarjetaDebitoGetCuentasAsociadas(contexto, tarjetaDebito.id());
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
				ApiResponseMB response = tarjetaDebitoGetCuentasAsociadas(contexto, tarjetaDebito.id());
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
			ApiResponseMB response = tarjetaDebitoGetCuentasAsociadas(contexto, tarjetaDebito.id());
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

	public static ApiResponseMB tarjetaDebitoBlanquearPil(ContextoMB contexto, String numero) {
		ApiRequestMB request = ApiMB.request("TarjetaDebitoBlanquearPil", "tarjetasdebito", "DELETE", "/v1/tarjetasdebito/{nrotarjeta}/pil", contexto);
		request.path("nrotarjeta", numero);
		request.query("digitoverificador", "0");
		request.query("numeromiembro", "0");
		request.query("numeroversion", "0");
		return ApiMB.response(request, contexto.idCobis(), numero);
	}

	public static ApiResponseMB tarjetaDebitoBlanquearPin(ContextoMB contexto, String numero) {
		ApiRequestMB request = ApiMB.request("TarjetaDebitoBlanquearPin", "tarjetasdebito", "DELETE", "/v1/tarjetasdebito/{nrotarjeta}/pin", contexto);
		request.path("nrotarjeta", numero);
		request.query("digitoverificador", "0");
		request.query("numeromiembro", "0");
		request.query("numeroversion", "0");
		return ApiMB.response(request, contexto.idCobis(), numero);
	}

	public static ApiResponseMB tarjetaDebitoGetEstado(ContextoMB contexto, String numero) {
		ApiRequestMB request = ApiMB.request("TarjetaDebitoGetEstado", "tarjetasdebito", "GET", "/v1/tarjetasdebito/{nrotarjeta}/estado", contexto);
		request.path("nrotarjeta", numero);
		request.query("digitoverificador", "0");
		request.query("numeromiembro", "0");
		request.query("numeroversion", "0");
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis(), numero);
	}

	public static ApiResponseMB habilitarTarjetaDebito(ContextoMB contexto, String numero) {
		ApiRequestMB request = ApiMB.request("TarjetaDebitoActivar", "tarjetasdebito", "PATCH", "/v1/tarjetasdebito/{nrotarjeta}/habilitar", contexto);
		request.path("nrotarjeta", numero);
		request.query("digitoverificador", "0");
		request.query("numeromiembro", "0");
		request.query("numeroversion", "0");
		return ApiMB.response(request, contexto.idCobis(), numero);
	}

	public static ApiResponseMB detalle(ContextoMB contexto, String numero) {
		ApiRequestMB request = ApiMB.request("TarjetaDebitoDetalle", "tarjetasdebito", "GET", "/v1/tarjetasdebito/{nro_tarjeta}", contexto);
		request.path("nro_tarjeta", numero);
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis(), numero);
	}

	public static ApiResponseMB tarjetaDebitoVirtualToFisica(ContextoMB contexto, TarjetaDebito td) {
		ApiRequestMB request = ApiMB.request("ActualizacionTarjetaDebito", "tarjetasdebito", "PATCH", "/v1/tarjetasdebito", contexto);

		Objeto tdModel = new Objeto();
		tdModel.set("CLIENTE", contexto.idCobis());
		tdModel.set("TARJETA", td.numero());
		tdModel.set("TIPO_TARJETA", td.idTipoTarjeta());
		tdModel.set("VIRTUAL", "N");
		request.body(tdModel);

		return ApiMB.response(request, contexto.idCobis());
	}
	
	public static ApiResponseMB pausadoTarjetaDebito(ContextoMB contexto, TarjetaDebito td, boolean pausar) {
		ApiRequestMB request = ApiMB.request("ActualizacionTarjetaDebito", "tarjetasdebito", "PATCH", "/v1/tarjetasdebito",
				contexto);

		Objeto tdModel = new Objeto();
		tdModel.set("CLIENTE", contexto.idCobis());
		tdModel.set("TARJETA", td.numero());
		tdModel.set("TIPO_TARJETA", td.idTipoTarjeta());
		tdModel.set("PAUSAR", pausar ? "S" : "N");
		request.body(tdModel);

		return ApiMB.response(request, contexto.idCobis());
	}
	
    public static ApiResponseMB avisarViajeExterior(ContextoMB contexto, String nroTarjeta) {
        ApiRequestMB request = ApiMB.request("TarjetasDebitoAvisoExterior", "tarjetasdebito", "POST",
                "/v1/tarjetasdebito/{nrotarjeta}/avisoviajeexterior", contexto);
        request.path("nrotarjeta", nroTarjeta);
        request.header("x-canal", "MB");
        request.cacheSesion = true;
        return ApiMB.response(request, contexto.idCobis(), nroTarjeta);
    }

}
