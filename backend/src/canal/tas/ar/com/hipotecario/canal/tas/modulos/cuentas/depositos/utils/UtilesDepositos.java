package ar.com.hipotecario.canal.tas.modulos.cuentas.depositos.utils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.modulos.cuentas.depositos.models.TasDepositoEfectivo;
import ar.com.hipotecario.canal.tas.modulos.cuentas.depositos.service.TasSqlDepositoEfectivo;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASClientePersona;
import ar.com.hipotecario.canal.tas.shared.modulos.depositos.models.DepositoCuentaDTO;
import ar.com.hipotecario.canal.tas.shared.modulos.depositos.models.DepositoValores;
import ar.com.hipotecario.canal.tas.shared.modulos.depositos.service.TasSqlDepositos;

public class UtilesDepositos {

	public static String getNumeroTicket(String tasId) {
		Fecha fecha = new Fecha(new Date());
		String fechaFormateada = fecha.string("yyMMddHHmmss");
		return tasId + fechaFormateada;
	}

	public static List<DepositoValores> armaListaDepositos(DepositoCuentaDTO depositoRecuperado, List<Object> valores) {
		return valores.stream()
				.filter(LinkedHashMap.class::isInstance)
				.map(obj -> {
					@SuppressWarnings("unchecked")
					LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) obj;
					DepositoValores deposito = new DepositoValores();
					deposito.setKioscoId(depositoRecuperado.getKioscoId());
					deposito.setDepositoId(depositoRecuperado.DepositoId);
					deposito.setOrden(0);
					deposito.setTipoValor((String) map.get("tipoValor"));
					deposito.setImporte(map.get("denominacion") instanceof BigDecimal
							? (BigDecimal) map.get("denominacion")
							: new BigDecimal(map.get("denominacion").toString()));
					deposito.setCantidad((Long) map.get("cantidad"));
					return deposito;
				})
				.collect(Collectors.toList());
	}

	public static Objeto actualizarEstadoDeposito(ContextoTAS contexto,Objeto datos, String estadoDeposito){
		Objeto estadoActualizado = TasSqlDepositos.updateEstadoDeposito(contexto, datos, estadoDeposito);
		return estadoActualizado;
	}
	
	public static Objeto actualizarCodigoRetornoTimeout(ContextoTAS contexto,String numeroTicket, Integer codigoRetorno){
		Objeto estadoActualizado = TasSqlDepositos.updateCodigoRetornoTimeout(contexto, numeroTicket, codigoRetorno);
		return estadoActualizado;
	}

}