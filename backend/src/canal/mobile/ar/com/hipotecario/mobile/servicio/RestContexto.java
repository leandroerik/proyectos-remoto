package ar.com.hipotecario.mobile.servicio;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.api.MBAplicacion;
import ar.com.hipotecario.mobile.api.MBPrestamo;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.lib.Util;
import ar.com.hipotecario.mobile.negocio.Prestamo;
import ar.com.hipotecario.mobile.negocio.ProductoMora;

import static ar.com.hipotecario.mobile.api.MBProducto.getProductosEnMora;

public class RestContexto {

	public static final String FORMA_PAGO_EFECTIVO = "Efectivo";
	public static final String[] TIPOS_MORA_TEMPRANA = {"ONE", "MT"};
	public static final String CODIGO_PRODUCTO_PRESTAMO = "7";
	public static final String CUOTA_VENCIDA = "Vencida";

	public static List<Objeto> obtenerContador(String idCobis, String tipo, String inicio) {
		SqlRequestMB sqlRequest = SqlMB.request("ObtenerContador", "homebanking");
		StringBuilder str = new StringBuilder();
		str.append("SELECT [id],[idCobis],[tipo],[momento] FROM [homebanking].[dbo].[contador] WHERE idCobis = ? ");
		if (Objects.nonNull(tipo)) {
			str.append("AND tipo in ( " + tipo + " ) ");
		}
		if (Objects.nonNull(inicio)) {
			str.append("AND momento > ? ");
		}
		str.append("ORDER BY momento DESC");
		System.out.println("Query:" + str.toString());
		sqlRequest.sql = str.toString();
		sqlRequest.add(idCobis);
		if (Objects.nonNull(inicio)) {
			sqlRequest.add(inicio);
		}
		SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
		if (sqlResponse.hayError) {
			return null;
		}
		return sqlResponse.registros;
	}

	public static Boolean cambioDetectadoParaNormativoPPV2(ContextoMB contexto, Boolean eliminarCache) {

		try {

			// TODO bloquear en determinados dias y horarios la funcionalidad
			if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoHorarioFraude"))
				if (Util.fueraHorarioFraudes(contexto))
					return true;

			/* bloqueo 90 dias */
			if (bloqueado90dias(contexto))
				return true;

			if (eliminarCache) {
				ApiMB.eliminarCache(contexto, "Email", contexto.idCobis());
				ApiMB.eliminarCache(contexto, "Telefono", contexto.idCobis());
			}

			if (!SqlRegistroDispositivo.obtenerRegistroDispositivoUltimas24hsPorCobis(contexto.idCobis()).registros.isEmpty())
				return true;

		} catch (Exception e) {
			//
		}
		return false;
	}

	private static Boolean validaFechaHabil(ContextoMB contexto, String momento) {
		Integer cantidadDiasNormativoPrestamo = ConfigMB.integer("cantidad_dias_normativo_prestamo", 10) * (-1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Integer diasPlus = 0;
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(sdf.parse(momento));
			Integer count = 0;
			String momentoPlusDias = "";
			Integer countCorte = cantidadDiasNormativoPrestamo + 5;

			while (diasPlus < cantidadDiasNormativoPrestamo) {
				if (count >= countCorte) {
					return true;
				}
				count++;
				cal.setTime(sdf.parse(momento));
				cal.add(Calendar.DAY_OF_MONTH, count);
				momentoPlusDias = sdf.format(cal.getTime());

				if (Util.isDiaHabil(contexto, momentoPlusDias)) {
					diasPlus++;
				}
			}

			Calendar fechaActual = Calendar.getInstance();
			fechaActual.setTime(new Date());
			Date dFechaActual = fechaActual.getTime();
			Date fechaMomentoPlusDias = new SimpleDateFormat("yyyy-MM-dd").parse(momentoPlusDias);
			if (dFechaActual.compareTo(fechaMomentoPlusDias) >= 0) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			return true;
		}
	}

	public static Boolean bloqueado90dias(ContextoMB contexto) {

		try {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date fechaActual = new SimpleDateFormat("yyyy-MM-dd").parse(sdf.format(new Date()));
			Date fechaFinBloqueo = new SimpleDateFormat("yyyy-MM-dd").parse("2023-06-27");

			Objeto sql = SqlHomebanking.getTipoMuestreo(contexto.idCobis(), "habilitar.normativo.execpcion");
			if (sql == null) {
				return false;
			}

			if (sql.string("m_valor").equals("false")) {
				if (fechaActual.after(fechaFinBloqueo)) {
					return false;
				} else {
					return true;
				}
			}

			if (sql.string("m_valor").equals("true")) {
				return false;
			}

			Date fechaFinBloqueoVariable = new SimpleDateFormat("yyyy-MM-dd").parse(sql.string("m_valor"));

			if (fechaActual.after(fechaFinBloqueoVariable)) {
				return false;
			} else {
				return true;
			}

		} catch (Exception e) {
			//
		}
		return false;
	}

	public static RespuestaMB isRiskForChangeInformation48Horas(ContextoMB contexto) {
		String tipo = ConfigMB.string("cambio_information_no_permitido_registro_dispositivo");
		if (Objeto.anyEmpty(contexto.idCobis()))
			return RespuestaMB.estado("ERROR");

		if (!MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_disable_48hrs"))
			return RespuestaMB.exito("enableb_operator", Boolean.TRUE);

		DisableService disableService = new DisableService();

		String inicio = LocalDateTime.now().plusHours(-1 * disableService.calculateHourDelay48()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		List<Objeto> registros = ContextoMB.obtenerContador(contexto.idCobis(), tipo, inicio);
		if (Objects.isNull(registros))
			return RespuestaMB.estado("ERROR");

		return RespuestaMB.exito("enableb_operator", disableService.getEnabledToOperator(registros));
	}

	public static boolean modificoDatosPrestamoPersonal(ContextoMB contexto) {
		return obtenerContador(contexto.idCobis(), ConfigMB.string("mitigante_no_permitido_pp", ""), LocalDateTime.now().plusHours(-1 * ConfigMB.integer("mitigante_dias_no_permitido_pp", 2) * 24).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).size() != 0;
	}


	public static boolean enMora(ContextoMB contexto) {
		boolean tieneMora = false;
		List<ProductoMora> productosEnMora = getProductosEnMora(contexto);
		for (Prestamo prestamo : contexto.prestamos()) {
			if (!productosEnMora.isEmpty() && productosEnMora.stream().anyMatch(prod -> prod.numeroProducto().equals(prestamo.numero()) && Arrays.asList(TIPOS_MORA_TEMPRANA).contains(prod.tipoMora()) && CODIGO_PRODUCTO_PRESTAMO.equals(prod.prodCod().trim()))) {
				tieneMora = true;
				break;
			}
			if (!FORMA_PAGO_EFECTIVO.equals(prestamo.formaPago()) && prestamo.cuotas().stream().anyMatch(cuota -> CUOTA_VENCIDA.equals(cuota.estado()))) {
				tieneMora = true;
				break;
			}
		}
		return tieneMora;
	}

}
