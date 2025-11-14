package ar.com.hipotecario.canal.buhobank;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.paquetes.ApiPaquetes;
import ar.com.hipotecario.backend.servicio.api.paquetes.Paquetes;
import ar.com.hipotecario.backend.servicio.api.paquetes.Paquetes.Paquete;
import ar.com.hipotecario.backend.servicio.api.paquetes.Paquetes.Producto;
import ar.com.hipotecario.backend.servicio.api.paquetes.Paquetes.SubProducto;
import ar.com.hipotecario.backend.servicio.api.paquetes.Paquetes.TarjetaCredito;
import ar.com.hipotecario.backend.servicio.api.ventas.MotorScoringSimulacion;
import ar.com.hipotecario.backend.servicio.api.ventas.Resolucion;
import ar.com.hipotecario.backend.servicio.api.ventas.Solicitud;
import ar.com.hipotecario.backend.servicio.sql.SqlBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPaquetesBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPaquetesBuhobank.BBPaqueteBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPaquetesSubproductoBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasBuhobank.BBTarjetaBuhobank;
import ar.com.hipotecario.canal.buhobank.negocio.TarjetaBeneficio;
import ar.com.hipotecario.backend.servicio.sql.esales.PadronAfipEsales.PadronAfip;

public class BBPaquetes extends Modulo {

	public static MotorScoringSimulacion flujoVentaOptimizado(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();
		String cuil = sesion.cuil;
		if (empty(cuil)) {
			System.out.println("CUIL_VACIO");
			return null;
		}

		Solicitud solicitud = BBVentas.crearSolicitud(contexto);
		if (solicitud == null) {
			System.out.println("SOLICITUD_VACIA");
			return null;
		}

		String idSolicitud = solicitud.Id;
		sesion.idSolicitud = idSolicitud;
		sesion.saveSesion();

		return BBMotor.invocarMotor(contexto, GeneralBB.PRIMERA_LLAMADA, idSolicitud);
	}

	public static Paquete obtenerPaquetePorCodigo(ContextoBB contexto, String numeroPaquete) {

		Paquete paqueteApi = obtenerPaquetePorCodigoRedis(contexto, numeroPaquete);
		if (paqueteApi == null) {
			return null;
		}

		BBPaquetesBuhobank paquetesBuhobank = SqlBuhoBank.obtenerPaquetes(contexto, contexto.sesion().getFlujo()).tryGet();
		List<String> paquetesValidos = BBPaquetesBuhobank.obtenerNumeroPaquetes(paquetesBuhobank);
		if (paquetesValidos != null && !paquetesValidos.contains(numeroPaquete)) {
			return null;
		}

		return paqueteApi;
	}

	public static Boolean esPaqueteValido(ContextoBB contexto, String codigoPaquete, Boolean sinEmprendedor) {

		BBPaquetesBuhobank paquetesBuhobank = SqlBuhoBank.obtenerPaquetes(contexto, contexto.sesion().getFlujo()).tryGet();
		List<String> paquetesValidos = BBPaquetesBuhobank.obtenerNumeroPaquetes(paquetesBuhobank);
		if (paquetesValidos == null) {
			return false;
		}

		boolean esValido = paquetesValidos.contains(codigoPaquete);

		if (sinEmprendedor) {
			esValido &= !GeneralBB.PAQUETE_EMPRENDEDOR.toString().equals(codigoPaquete);
			esValido &= !GeneralBB.PAQUETE_EMPRENDEDOR_BLACK.toString().equals(codigoPaquete);
		}

		return esValido;
	}

	public static Paquete obtenerPaquetePorLetra(ContextoBB contexto, String letraTC) {

		Paquetes paquetesApi = obtenerPaquetesPorLetraRedis(contexto, letraTC);
		if (paquetesApi == null) {
			return null;
		}

		List<Paquete> paquetesFiltrados = paquetesApi.filter((paquete) -> esPaqueteValido(contexto, paquete.codigo, true)).collect(Collectors.toList());

		Paquete paquete = null;

		for (Paquete paqueteFiltrado : paquetesFiltrados) {
			for (int i = 0; i < paqueteFiltrado.productos.size(); i++) {
				Producto producto = paqueteFiltrado.productos.get(i);
				if (producto.id == 203 && !empty(producto.categoriaDefault)) {
					if (producto.categoriaDefault.equals(letraTC)) {
						paquete = paqueteFiltrado;
					}
				}
			}
		}

		return paquete;
	}

	private static Paquetes obtenerPaquetesPorLetraRedis(ContextoBB contexto, String letraTC) {

		try {

			String clave = GeneralBB.BB_REDIS_PAQUETE + "_" + letraTC;
			String paquetesRedis = contexto.get(clave);
			if (!empty(paquetesRedis)) {

				Paquete[] paquetesArray = fromJson(paquetesRedis, Paquete[].class);
				Paquetes paquetesAPi = new Paquetes();

				for (Paquete paqueteArray : paquetesArray) {
					paquetesAPi.add(paqueteArray);
				}

				return paquetesAPi;
			} else {

				Paquetes paquetesApi = ApiPaquetes.paquetesPorLetra(contexto, letraTC, false).tryGet();
				if (paquetesApi != null) {
					String paquetesStr = paquetesApi.toString();
					LogBB.evento(contexto, "REDIS_PAQUETE_" + letraTC, paquetesStr);
					contexto.set(clave, paquetesStr, GeneralBB.BB_REDIS_PAQUETE_EXPIRACION);
					return paquetesApi;
				}
			}
		} catch (Exception e) {
			return ApiPaquetes.paquetesPorLetra(contexto, letraTC, false).tryGet();
		}

		return null;
	}

	private static Paquete obtenerPaquetePorCodigoRedis(ContextoBB contexto, String numeroPaquete) {

		try {

			String clave = GeneralBB.BB_REDIS_PAQUETE + "_" + numeroPaquete;
			String paqueteRedis = contexto.get(clave);
			if (!empty(paqueteRedis)) {
				return fromJson(paqueteRedis, Paquete.class);
			} else {

				Paquete paqueteApi = ApiPaquetes.paquetePorNumero(contexto, numeroPaquete, false).tryGet();
				if (paqueteApi != null) {
					contexto.set(clave, paqueteApi.toString(), GeneralBB.BB_REDIS_PAQUETE_EXPIRACION);
					return paqueteApi;
				}
			}
		} catch (Exception e) {
			return ApiPaquetes.paquetePorNumero(contexto, numeroPaquete, false).tryGet();
		}

		return null;
	}

	public static Objeto armarRespuestaOferta(ContextoBB contexto, BBPaqueteBuhobank paqueteBuhobank, BBTarjetaBuhobank contenidoTarjeta, TarjetaCredito tc, List<Producto> productosApi) {

		SesionBB sesion = contexto.sesion();

		Objeto respuesta = respuesta();

		Boolean incluyeTarjetaCredito = tc != null;
		respuesta.set("paquete.id", paqueteBuhobank.id);
		respuesta.set("paquete.numeroPaquete", contenidoTarjeta.numero_paquete);
		respuesta.set("paquete.descripcion", contenidoTarjeta.descripcion);
		respuesta.set("paquete.titulo", contenidoTarjeta.titulo);
		respuesta.set("tarjetaVirtualActiva", false);
		respuesta.set("paquete.id_tarjeta", contenidoTarjeta.id);
		respuesta.set("paquete.tarjeta", contenidoTarjeta.tarjeta);
		respuesta.set("paquete.tarjetaStandalone", TarjetaBeneficio.IMAGEN_STANDALONE);
		respuesta.set("paquete.box", contenidoTarjeta.box);
		respuesta.set("paquete.boton", contenidoTarjeta.boton);
		respuesta.set("paquete.boton2", sesion.esStandalone() ? "SOLO QUIERO UNA CUENTA UNIVERSAL" : "SOLO QUIERO UNA CUENTA");
		respuesta.set("paquete.titulo_beneficios", "Incluido en esta propuesta");
		respuesta.set("paquete.beneficios", contenidoTarjeta.beneficios);

		if(sesion.esV3()){
			String letra = tc != null ? tc.letraTC : "";
			respuesta.set("paquete.beneficios", TarjetaBeneficio.beneficiosTarjetaLegacy(letra));
		}

		respuesta.set("paquete.tarjetaCredito", null);
		if (incluyeTarjetaCredito) {
			Objeto tcResponse = new Objeto();
			String nombreCorto = tc.nombre.substring(5, tc.nombre.length());
			tcResponse.set("nombre", tc.nombre);
			tcResponse.set("letraTC", !empty(contenidoTarjeta.tipo) ? tc.letraTC + "_" + contenidoTarjeta.tipo : tc.letraTC);
			tcResponse.set("nombreCorto", !empty(contenidoTarjeta.nombre_corto) ? contenidoTarjeta.nombre_corto : nombreCorto);
			tcResponse.set("limiteCompraTotal", "$" + Modulo.importe(tc.limiteCompraTotal).replace(",00", "") + " de límite.");
			respuesta.set("paquete.tarjetaCredito", tcResponse);
		}

		respuesta.set("paquete.incluyeTarjetaCredito", incluyeTarjetaCredito);
		respuesta.set("paquete.presentaDocBuhoInversor", false);
		respuesta.set("paquete.check_inversor", paqueteBuhobank.checkInversor());
		respuesta.set("paquete.check_subproductos", false);

		Boolean checkTdVirtual = paqueteBuhobank.checkTdVisualizaVirtual(contexto);
		respuesta.set("paquete.check_td_Fisica", checkTdVirtual);
		sesion.checkTdFisica = checkTdVirtual;
		respuesta.set("paquete.subproductos", null);

		respuesta.set("paquete.adicionales", null);

		if(GeneralBB.FLUJO_INVERSIONES.equals(sesion.getFlujo()) && contenidoTarjeta.beneficiosAdicionales != null && contenidoTarjeta.beneficiosAdicionales.size() > 0) {
			respuesta.set("paquete.boton", "CONTINUAR");
			respuesta.set("paquete.boton2", null);
			respuesta.set("paquete.titulo_beneficios", "Incluido en Búho Inversor");
			respuesta.set("paquete.adicionales.titulo_beneficios", "Además:");
			respuesta.set("paquete.adicionales.beneficios", contenidoTarjeta.beneficiosAdicionales);
		}

		if (paqueteBuhobank.checkCaracteristica()) {

			BBPaquetesSubproductoBuhobank paquetesSubproductosBuhobank = SqlBuhoBank.obtenerSubproductosByPaquete(contexto, paqueteBuhobank.id).tryGet();
			BBPaquetesSubproductoBuhobank paquetesSubproductos = BBPaquetesSubproductoBuhobank.buscarTipo(paquetesSubproductosBuhobank, BBPaquetesSubproductoBuhobank.TIPO_PROGRAMA_FIDELIZACION);
			paquetesSubproductos = BBPaquetesSubproductoBuhobank.filtrarHabilitados(paquetesSubproductos);
			if (paquetesSubproductos != null && paquetesSubproductos.size() > 0) {

				respuesta.set("paquete.check_subproductos", paqueteBuhobank.checkCaracteristica());
				respuesta.set("paquete.subproductos", paquetesSubproductos);
			}
		}

		respuesta.set("td_activa", false);

		Boolean checkTdVirtualCgu = getCheckTdFisicaCGU(contexto);
		respuesta.set("check_td_Fisica_cgu", checkTdVirtualCgu);
		sesion.checkTdFisicaCgu = checkTdVirtualCgu;

		sesion.actualizarEstado(EstadosBB.OFERTAS);
		LogBB.evento(contexto, EstadosBB.OFERTAS, respuesta);

		return respuesta;
	}

	private static boolean getCheckTdFisicaCGU(ContextoBB contexto) {
		BBPaqueteBuhobank paquete = SqlBuhoBank.buscarNumeroPaqueteBuhobank(contexto, GeneralBB.BUHO_CGU, GeneralBB.FLUJO_ONBOARDING).tryGet();
		return paquete.checkTdVisualizaVirtualCgu();
	}

	public static String obtenerLetraProductoPorCodigo(String codigo) {

		if (GeneralBB.PAQUETE_BUHO_PACK.toString().equals(codigo)) {
			return GeneralBB.LETRA_PAQUETE_BUHO_PACK;
		}

		if (GeneralBB.PAQUETE_GOLD.toString().equals(codigo)) {
			return GeneralBB.LETRA_PAQUETE_GOLD;
		}

		if (GeneralBB.PAQUETE_PLATINUM.toString().equals(codigo)) {
			return GeneralBB.LETRA_PAQUETE_PLATINUM;
		}

		if (GeneralBB.PAQUETE_BLACK.toString().equals(codigo)) {
			return GeneralBB.LETRA_PAQUETE_BLACK;
		}

		return null;
	}

	public static BigDecimal obtenerLimiteCompraPorCodigo(String codigo) {

		if (GeneralBB.PAQUETE_BUHO_PACK.toString().equals(codigo)) {
			return new BigDecimal(GeneralBB.LIMITE_COMPRA_PAQUETE_BUHO_PACK);
		}

		if (GeneralBB.PAQUETE_GOLD.toString().equals(codigo)) {
			return new BigDecimal(GeneralBB.LIMITE_COMPRA_PAQUETE_GOLD);
		}

		if (GeneralBB.PAQUETE_PLATINUM.toString().equals(codigo)) {
			return new BigDecimal(GeneralBB.LIMITE_COMPRA_PAQUETE_PLATINUM);
		}

		if (GeneralBB.PAQUETE_BLACK.toString().equals(codigo)) {
			return new BigDecimal(GeneralBB.LIMITE_COMPRA_PAQUETE_BLACK);
		}

		return null;
	}

	public static void obtenerEquivalenteparaCS(ContextoBB contexto) {

		SesionBB sesion = contexto.sesion();
		int ofertaElegida = Integer.parseInt(sesion.ofertaElegida);
		String tipoOferta = sesion.tipoOferta();
		if (tipoOferta.contains("EMPRENDEDOR")) {
			return;
		}

		BBPaqueteBuhobank paqCSueldo = SqlBuhoBank.obtenerCuentaSueldo(contexto, ofertaElegida).tryGet();

		if (!empty(paqCSueldo)) {
			sesion.ofertaElegida = paqCSueldo.numero_paquete.toString();
			sesion.codigoPaqueteMotor = paqCSueldo.numero_paquete.toString();
			sesion.saveSesion();
			LogBB.evento(contexto, "CUENTA_SUELDO", paqCSueldo.nombre);
		}
	}

	public static Objeto ofertaStandaloneVirtual(ContextoBB contexto) {

		SesionBB sesion = contexto.sesion();

		if(!sesion.resolucionAprobada() && sesion.getParamPrevencionStandalone(contexto)){
			return respuesta("ERROR_SA");
		}

		if(sesion.tieneCAPesos(contexto)){
			return respuesta("PAQUETE_RECHAZADO");
		}

		BBPaquetesBuhobank paquetesBuhobank = SqlBuhoBank.obtenerPaquetes(contexto, sesion.getFlujo()).tryGet();
		BBPaqueteBuhobank paqueteStandalone = BBPaquetesBuhobank.buscarStandalone(paquetesBuhobank);
		Boolean esVirtual = paqueteStandalone != null && paqueteStandalone.td_virtual;

		BBTarjetaBuhobank contenidoTarjeta = paqueteStandalone != null ? SqlBuhoBank.obtenerTarjetaBuhobank(contexto, paqueteStandalone.id, esVirtual).tryGet() : null;
		if (contenidoTarjeta == null) {
			LogBB.error(contexto, ErroresBB.STANDALONE_NO_ENCONTRADO, GeneralBB.BUHO_INICIA.toString());
			return respuesta(ErroresBB.STANDALONE_NO_ENCONTRADO);
		}

		List<Producto> productosApi = BBVentas.obtenerProductosStandalone();

		List<SubProducto> subProductos = new ArrayList<>();
		subProductos.add(Paquete.subProductoBuho());

		sesion.tdVirtual = esVirtual;
		sesion.tcVirtual = null;
		sesion.saveSesionbb2();

		return armarRespuestaOferta(contexto, paqueteStandalone, contenidoTarjeta, null, productosApi);
	}

	public static Objeto ofertaEmprendedor(ContextoBB contexto, BBPaqueteBuhobank paqueteEmprendedor, BigDecimal limite) {

		SesionBB sesion = contexto.sesion();
		String numeroPaquete = paqueteEmprendedor.numero_paquete.toString();
		String letraTc = paqueteEmprendedor.letra_tc;

		boolean esTdVirtual = paqueteEmprendedor.td_virtual;

		BBTarjetaBuhobank contenidoTarjeta = SqlBuhoBank.obtenerTarjetaBuhobank(contexto, paqueteEmprendedor.id, false).tryGet();
		if (contenidoTarjeta == null) {
			LogBB.error(contexto, ErroresBB.PAQUETE_NO_ENCONTRADO, "paquete_emprendedor: " + paqueteEmprendedor.nombre);
			return ofertaStandaloneVirtual(contexto);
		}

		Paquete paqueteApiTC = obtenerPaquetePorLetra(contexto, letraTc);
		if (paqueteApiTC == null) {
			LogBB.error(contexto, ErroresBB.PAQUETE_NO_ENCONTRADO, "emprendedor_paquete_letra: " + letraTc);
			return ofertaStandaloneVirtual(contexto);
		}

		sesion.codigoPaqueteMotor = numeroPaquete;
		sesion.tcVirtual = false;
		sesion.tdVirtual = esTdVirtual;
		sesion.save();

		TarjetaCredito tc = paqueteApiTC.tarjetaCreditoFull(contexto, letraTc, limite, numeroPaquete);

		return armarRespuestaOferta(contexto, paqueteEmprendedor, contenidoTarjeta, tc, paqueteApiTC.productos);
	}

	public static Objeto armarPaquete(ContextoBB contexto, String letraTC, BigDecimal limite, Paquete paqueteApi, BBPaqueteBuhobank paqueteBuhobank) {

		SesionBB sesion = contexto.sesion();
		BBTarjetaBuhobank contenidoTarjeta = SqlBuhoBank.obtenerTarjetaBuhobank(contexto, paqueteBuhobank.id, false).tryGet();
		if (contenidoTarjeta == null) {
			LogBB.error(contexto, ErroresBB.PAQUETE_NO_ENCONTRADO, "letra_tc: " + paqueteBuhobank.letra_tc);
			return ofertaStandaloneVirtual(contexto);
		}

		sesion.tcVirtual = false;
		sesion.tdVirtual = paqueteBuhobank.td_virtual;
		sesion.saveSesionbb2();

		TarjetaCredito tc = paqueteApi.tarjetaCreditoFull(contexto, letraTC, limite, paqueteApi.codigo);

		return armarRespuestaOferta(contexto, paqueteBuhobank, contenidoTarjeta, tc, paqueteApi.productos);
	}

	public static Objeto armarPaquetePorLetra(ContextoBB contexto, String letraTC, BigDecimal limite) {

		SesionBB sesion = contexto.sesion();

		BBPaqueteBuhobank paqueteEmprendedor = SqlBuhoBank.obtenerPaquetesLetra(contexto, sesion.getFlujo(), letraTC, true).tryGet();
		if (paqueteEmprendedor != null) {
			PadronAfip padronAfip = SqlEsales.getPadronAfip(contexto, sesion.cuil).tryGet();
			if (!empty(padronAfip) && !empty(padronAfip.monotributo)) {
				if (GeneralBB.CATEGORIAS.stream().anyMatch(categoria -> categoria.equals(padronAfip.monotributo.trim())) || GeneralBB.RESPONSABLE_INSCRIPTO.equals(padronAfip.monotributo)) {
					LogBB.evento(contexto, EstadosBB.CONSULTAR_TERADATA, EstadosBB.EXISTE_EMPRENDEDOR);
					return ofertaEmprendedor(contexto, paqueteEmprendedor, limite);
				}

			} else {
				LogBB.evento(contexto, EstadosBB.CONSULTAR_TERADATA, EstadosBB.NO_EXISTE_EN_TERADATA);
			}
		}

		BBPaqueteBuhobank paqueteBuhobank = SqlBuhoBank.obtenerPaquetesLetra(contexto, sesion.getFlujo(), letraTC, false).tryGet();
		if (paqueteBuhobank == null) {
			LogBB.error(contexto, ErroresBB.PAQUETE_NO_ENCONTRADO, "flujo: " + sesion.getFlujo() + " | letra_tc: " + letraTC);
			return ofertaStandaloneVirtual(contexto);
		}

		Paquete paqueteApi = obtenerPaquetePorCodigo(contexto, paqueteBuhobank.numero_paquete.toString());
		if (paqueteApi == null) {
			paqueteApi = obtenerPaquetePorLetra(contexto, letraTC);
		}

		if (paqueteApi == null) {
			LogBB.error(contexto, ErroresBB.PAQUETE_NO_ENCONTRADO, "paquete_letra: " + letraTC);
			return ofertaStandaloneVirtual(contexto);
		}

		sesion.codigoPaqueteMotor = paqueteApi.codigo;
		sesion.saveSesion();

		return armarPaquete(contexto, letraTC, limite, paqueteApi, paqueteBuhobank);
	}

	public static Objeto obtenerOfertaMotor(ContextoBB contexto, String codigoOfertaMotor) {

		SesionBB sesion = contexto.sesion();

		String letraTC = sesion.letraTC;
		BigDecimal limite = sesion.limite;
		String codigoPaquete = sesion.codigoPaqueteMotor;

		if (!empty(codigoOfertaMotor)) {
			letraTC = obtenerLetraProductoPorCodigo(codigoOfertaMotor);
			limite = obtenerLimiteCompraPorCodigo(codigoOfertaMotor);

			sesion.letraTC = letraTC;
			sesion.limite = limite;
			sesion.save();
		}

		if (empty(letraTC) || empty(limite)) {
			MotorScoringSimulacion respuestaMotor = flujoVentaOptimizado(contexto);
			if (respuestaMotor == null) {
				sesion.resolucionMotorDeScoring = Resolucion.RECHAZAR;
				sesion.saveSesion();
				LogBB.evento(contexto, EstadosBB.RECHAZO_MOTOR, Resolucion.RECHAZAR);
				return ofertaStandaloneVirtual(contexto);
			}

			sesion.resolucionMotorDeScoring = respuestaMotor.resolucion();
			sesion.modoAprobacion = respuestaMotor.modoAprobacion();
			sesion.ingresoNeto = null;

			if (!respuestaMotor.aprobado()) {
				sesion.save();
				LogBB.evento(contexto, EstadosBB.RECHAZO_MOTOR, respuestaMotor.resolucion());
				return ofertaStandaloneVirtual(contexto);
			}

			letraTC = respuestaMotor.letraTC();
			limite = respuestaMotor.limiteCompra();

			sesion.letraTC = letraTC;
			sesion.limite = limite;
			sesion.tarjetaOferta = sesion.crearTarjetaOferta(respuestaMotor.tarjetaOfrecida());
			sesion.save();
		}

		if (!empty(letraTC) && !empty(limite)) {
			return armarPaquetePorLetra(contexto, letraTC, limite);
		}

		Paquete paquete = obtenerPaquetePorCodigo(contexto, codigoPaquete);
		if (paquete == null) {
			return armarPaquetePorLetra(contexto, letraTC, limite);
		}

		BBPaqueteBuhobank paqueteBuhobank = SqlBuhoBank.obtenerPaquetesLetra(contexto, sesion.getFlujo(), letraTC, false).tryGet();
		if (paqueteBuhobank == null) {
			LogBB.error(contexto, ErroresBB.PAQUETE_NO_ENCONTRADO, "letra_tc: " + letraTC);
			return ofertaStandaloneVirtual(contexto);
		}

		return armarPaquete(contexto, letraTC, limite, paquete, paqueteBuhobank);
	}

	public static Objeto ofertas(ContextoBB contexto) {
		if (!contexto.sesion().valDatosPersonales) {
			LogBB.evento(contexto, EstadosBB.VALIDAR_DATOS_PERSONALES_OK, contexto.sesion().token);
		}

		if (rechazoPrevioScoring(contexto)) {
			LogBB.evento(contexto, EstadosBB.RECHAZO_PREVIO_MOTOR);
			return ofertaStandaloneVirtual(contexto);
		}

		if (contexto.sesion().esFlujoInversiones()) {
			return ofertaStandaloneVirtual(contexto);
		}

		return obtenerOfertaMotor(contexto, null);
	}

	public static Boolean rechazoPrevioScoring(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();
		Boolean maximoRechazos = BBPoliticas.maximoRechazosScoring(contexto);
		Boolean rechazoGuardadoSesion = !empty(sesion.resolucionMotorDeScoring) ? !sesion.resolucionAprobada() : false;
		return maximoRechazos || rechazoGuardadoSesion;
	}

	public static Objeto elegirOferta(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();
		String ofertaElegida = contexto.parametros.string("ofertaElegida");

		String bhInversor = contexto.parametros.string("bbInversor", "0");
		String subProducto = contexto.parametros.string("subProducto", GeneralBB.SUB_BUHO_PUNTOS);

		sesion.ofertaElegida = ofertaElegida;
		sesion.subProducto = subProducto;
		sesion.bbInversorAceptada = GeneralBB.CUENTA_INVERSOR_ACEPTADA.equals(bhInversor) ? GeneralBB.CUENTA_INVERSOR_ACEPTADA : GeneralBB.CUENTA_INVERSOR_NO_ACEPTADA;

		BBPaquetesBuhobank paquetesBuhobank = SqlBuhoBank.obtenerPaquetes(contexto, sesion.getFlujo()).tryGet();
		BBPaqueteBuhobank paqueteBuhobank = BBPaquetesBuhobank.buscarPaquete(paquetesBuhobank, sesion.letraTC, sesion.numeroPaquete());
		if (!empty(paqueteBuhobank)) {

			boolean aceptaInversor = paqueteBuhobank.checkInversor() ? GeneralBB.CUENTA_INVERSOR_ACEPTADA.equals(bhInversor) : paqueteBuhobank.cuentaInversor();

			sesion.bbInversorAceptada = aceptaInversor ? GeneralBB.CUENTA_INVERSOR_ACEPTADA : GeneralBB.CUENTA_INVERSOR_NO_ACEPTADA;
			sesion.tdVirtual = paqueteBuhobank.td_virtual;
			sesion.tcVirtual = false;
			sesion.subProducto = paqueteBuhobank.checkCaracteristica() ? subProducto : paqueteBuhobank.getCaracteristica();

		} else {

			if (sesion.esCGU()) {
				sesion.tdVirtual = true;
				sesion.tcVirtual = false;
			}
		}

		sesion.estado = EstadosBB.ELEGIR_OFERTA_OK;
		sesion.save();

		String nombreOferta = sesion.descripcionOferta(contexto);

		Objeto respuesta = respuesta();
		respuesta.set("tarjetaVirtualActiva", false);
		respuesta.set("ofertaElegida", ofertaElegida);
		respuesta.set("incluyeTarjetaCredito", incluyeTarjetaCredito(ofertaElegida));
		respuesta.set("subProducto", subProducto);
		respuesta.set("nombreOferta", nombreOferta);
		respuesta.set("presentaDoc", false);

		LogBB.evento(contexto, EstadosBB.ELEGIR_OFERTA_OK, respuesta);
		return respuesta;
	}

	public static Boolean incluyeTarjetaCredito(String ofertaElegida) {
		return !GeneralBB.BUHO_INICIA.toString().equals(ofertaElegida) && !GeneralBB.BUHO_CGU.toString().equals(ofertaElegida) && !GeneralBB.BUHO_INICIA_HML.toString().equals(ofertaElegida) && !GeneralBB.BUHO_INICIA_INVERSOR.toString().equals(ofertaElegida);
	}

	public static Object testObtenerOferta(ContextoBB contexto) {

		if (contexto.esProduccion()) {
			return null;
		}

		String cuil = contexto.parametros.string("cuil", "");

		String codArea = contexto.parametros.string("codArea", "11");
		String celular = contexto.parametros.string("celular", "28164255");
		String mail = contexto.parametros.string("mail", "usuariotestx@gmail.com");

		String idSituacionLaboral = contexto.parametros.string("idSituacionLaboral", "1");

		String ofertaElegida = contexto.parametros.string("ofertaElegida", "0");
		String bhInversor = contexto.parametros.string("bbInversor", null);

		Boolean embozado = contexto.parametros.bool("embozado", false);
		Boolean casado = contexto.parametros.bool("casado", false);
		Boolean ulrQr = contexto.parametros.bool("url_qr", false);

		// es cliente

		contexto.parametros.set("cuil", cuil);

		Objeto clienteResp = BBPersona.esCliente(contexto);

        if (!clienteResp.bool("continua")) {
			return respuesta("ES_CLIENTE");
		}

		// crear sesion

		String numeroDocumento = cuil.substring(2, 10);

		contexto.parametros.set("numeroDocumento", numeroDocumento);
		contexto.parametros.set("secret", GeneralBB.VERSION_PLATAFORMA_0_0_1);
		contexto.parametros.set("sucursalOnboarding", ulrQr);

		Objeto crearSesionRes = BBSeguridad.crearSesion(contexto);

		if (crearSesionRes == null) {
			return respuesta("ERROR_CREAR_SESION");
		}

		// guardar contacto

		contexto.parametros.set("codArea", codArea);
		contexto.parametros.set("celular", celular);
		contexto.parametros.set("mail", mail);

		Objeto guardarContactoRes = BBValidacion.guardarContacto(contexto);
		if (guardarContactoRes == null) {
			return respuesta("ERROR_GUARDAR_CONTACTO");
		}

		if (!guardarContactoRes.string("estado").equals("0")) {
			return guardarContactoRes;
		}

		// obtener vu

		Objeto obtenerVURes = BBPersona.obtenerGuardarRespuestaCompletaVU(contexto);
		if (obtenerVURes == null) {
			return respuesta("ERROR_OBTENER_VU");
		}

		// guardar vu

		String idOperacion = obtenerVURes.string("idOperacion");
		BigDecimal confidence = obtenerVURes.bigDecimal("confidence");
		BigDecimal confidenceTotal = obtenerVURes.bigDecimal("confidenceTotal");
		Objeto ocr = obtenerVURes.objeto("ocr");
		Objeto barcode = obtenerVURes.objeto("barcode", null);
		Objeto information = obtenerVURes.objeto("information", null);
		BigDecimal confidenceDocument = obtenerVURes.bigDecimal("confidenceDocument");
		Boolean identical = obtenerVURes.bool("identical");

		contexto.parametros.set("idOperacion", idOperacion);
		contexto.parametros.set("confidence", confidence);
		contexto.parametros.set("confidenceTotal", confidenceTotal);
		contexto.parametros.set("ocr", ocr);
		contexto.parametros.set("barcode", barcode);
		contexto.parametros.set("information", information);
		contexto.parametros.set("confidenceDocument", confidenceDocument);
		contexto.parametros.set("identical", identical);

		Objeto guardarVURes = BBPersona.guardarRespuestaCompletaVU(contexto);
		if (guardarVURes == null) {
			return respuesta("ERROR_GUARDAR_VU");
		}

		if (!guardarVURes.string("estado").equals("0")) {
			return obtenerVURes;
		}

		// validar datos personales

		Objeto validarDatosPersonalesRes = BBPersona.validarDatosPersonales(contexto);

		if (validarDatosPersonalesRes == null) {
			return respuesta("ERROR_VALIDAR_DATOS_PERSONALES");
		}

		if (!validarDatosPersonalesRes.string("estado").equals("0")) {
			return validarDatosPersonalesRes;
		}

		// guardar adicionales

		contexto.parametros.set("idEstadoCivil", casado ? "C" : "S");
		contexto.parametros.set("idSituacionLaboral", idSituacionLaboral);

		Objeto guardarAdicionalesRes = BBPersona.guardarAdicionales(contexto);
		if (guardarAdicionalesRes == null) {
			return respuesta("ERROR_GUARDAR_ADICIONALES");
		}

		if (!guardarAdicionalesRes.string("estado").equals("0")) {
			return guardarAdicionalesRes;
		}

		// guardar dom legal

		Objeto domLegal = respuesta();

		if(contexto.esDesarrollo()){
			domLegal.set("calle", "LOS ANDES");
			domLegal.set("numeroCalle", "2345");
			domLegal.set("piso", "2");
			domLegal.set("dpto", "A");
			domLegal.set("cp", "6300");

			domLegal.set("ciudad", "SANTA ROSA");
			domLegal.set("localidad", "SANTA ROSA");
			domLegal.set("provincia", "LA PAMPA");
			domLegal.set("pais", "ARGENTINA");
		}
		else {
			domLegal.set("calle", "SAAVEDRA");
			domLegal.set("numeroCalle", "975");
			domLegal.set("piso", "2");
			domLegal.set("dpto", "4");
			domLegal.set("cp", "8000");

			domLegal.set("ciudad", "BAHIA BLANCA");
			domLegal.set("localidad", "BAHIA BLANCA");
			domLegal.set("provincia", "BUENOS AIRES");
			domLegal.set("pais", "ARGENTINA");
		}

		contexto.parametros.set("domicilioLegal", domLegal);

		Objeto guardarDomLegal = BBFormaEntrega.guardarDomicilioLegal(contexto);
		if (guardarDomLegal == null) {
			return respuesta("ERROR_GUARDAR_DOM_LEGAL");
		}

		if (!guardarDomLegal.string("estado").equals("0")) {
			return guardarDomLegal;
		}

		// forma de entrega

		contexto.parametros.set("tipo", "D");

		Objeto formaEntregaRes = BBFormaEntrega.guardarFormaDeEntrega(contexto);
		if (formaEntregaRes == null) {
			return respuesta("ERROR_FORMA_ENTREGA");
		}

		if (!formaEntregaRes.string("estado").equals("0")) {
			return formaEntregaRes;
		}

		// obtener oferta

		//Objeto ofertasRes = BBPaquetes.ofertas(contexto);
		Objeto ofertasRes = BBPaquetes.ofertaStandaloneVirtual(contexto);

		Objeto respuestaOfertas = respuesta();

		String idSolicitud = contexto.sesion().idSolicitud;
		respuestaOfertas.set("idSolicitud", idSolicitud);
		respuestaOfertas.set("oferta", ofertasRes);

		if (ofertasRes == null) {
			respuestaOfertas.set("estado", "ERROR_OFERTAS");
			return respuestaOfertas;
		}

		if (!ofertasRes.string("estado").equals("0")) {
			respuestaOfertas.set("estado", "ERROR_OFERTAS");
			return respuestaOfertas;
		}

		// elegir oferta

		Integer numeroPaquete = ofertasRes.integer("paquete.numeroPaquete");

		contexto.parametros.set("ofertaElegida", !empty(ofertaElegida) ? ofertaElegida : numeroPaquete.toString());
		contexto.parametros.set("bbInversor", bhInversor);

		Objeto elegirOfertaRes = BBPaquetes.elegirOferta(contexto);
		if (elegirOfertaRes == null) {
			return respuesta("ERROR_ELEGIR_OFERTA");
		}

		if (!elegirOfertaRes.string("estado").equals("0")) {
			return elegirOfertaRes;
		}

		//elegir td fisica

		if(embozado) {
			Objeto resElegirFisica = BBPaquetes.elegirFisica(contexto);
			if (resElegirFisica == null) {
				return respuesta("ERROR_ELEGIR_FISICA");
			}

			if (!resElegirFisica.string("estado").equals("0")) {
				return resElegirFisica;
			}
		}

		//guardar conyuge
		if(casado) {
			contexto.parametros.set("genero", "M");
			contexto.parametros.set("numeroDocumento", "20712049");
			contexto.parametros.set("fechaNacimiento", "17/02/1969");
			contexto.parametros.set("nombres", "JUAN CARLOS");
			contexto.parametros.set("apellido", "VACA");
			contexto.parametros.set("nacionalidad", "ARGENTINA");
			contexto.parametros.set("paisResidencia", "ARGENTINA");

			Objeto resGuardarConyuge = BBPersona.guardarAdicionalConyuge(contexto);
			if (resGuardarConyuge == null) {
				return respuesta("ERROR_ELEGIR_FISICA");
			}

			if (!resGuardarConyuge.string("estado").equals("0")) {
				return "ERROR_GUARDAR_CONYUGE";
			}
		}

		//finalizar
		Object finalizarRes = BBAlta.finalizar(contexto, false, false, false, false);
		if (empty(finalizarRes) || !finalizarRes.toString().equals(respuesta().toString())) {
			Objeto respuestaError = respuesta("ERROR_FINALIZAR");
			respuestaError.set("idSolicitud", idSolicitud);
			return respuestaError;
		}

		contexto.sesion().delete();

		return respuesta("FINALIZO_OK");
	}

	public static Objeto elegirFisica(ContextoBB contexto) {
		LogBB.evento(contexto, "REQUEST_ELEGIR_FISICA", contexto.sesion().token);
		SesionBB sesion = contexto.sesion();
		sesion.tdFisica = GeneralBB.VISUALIZA_S;
		sesion.save();

		return respuesta();
	}

	public static Object elegirCuentaSueldo(ContextoBB contexto) {

		SesionBB sesion = contexto.sesion();
		if (sesion.esRelacionDependencia() && sesion.getCheckCuentaSueldo()) {
			BBPaquetes.obtenerEquivalenteparaCS(contexto);
		}

		if (!sesion.esCuentaSueldo()) {
			LogBB.evento(contexto, GeneralBB.CUENTA_SUELDO_INVALIDA);
		}

		return respuesta();
	}

	public static Object desistirSolicitud(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();
		sesion.idSolicitud = null;
		sesion.resolucionMotorDeScoring = null;
		sesion.save();
		return respuesta();
	}

	public static Objeto ofertasV2(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();

		MotorScoringSimulacion respuestaMotor = null;

		Boolean esMayorA80 = sesion.fechaNacimiento.esAnterior(Fecha.ahora().restarAños(80));
		if (!esMayorA80) {
			respuestaMotor = flujoVentaOptimizado(contexto);
		}

		if (respuestaMotor == null || !respuestaMotor.aprobado()) {
			sesion.resolucionMotorDeScoring = respuestaMotor == null ? Resolucion.RECHAZAR : respuestaMotor.resolucion();
			sesion.modoAprobacion = respuestaMotor == null ? null : respuestaMotor.modoAprobacion();
			sesion.save();
			LogBB.evento(contexto, EstadosBB.RECHAZO_MOTOR, Resolucion.RECHAZAR);
			return TarjetaBeneficio.ofertaLetra(GeneralBB.BUHO_INICIA.toString(), "", new BigDecimal("0.0"));
		}

		String letraTc = respuestaMotor.letraTC();
		boolean esEmprendedor = !letraTc.isEmpty() && esEmprendedor(contexto);
		String nroProducto = respuestaMotor.codigoTc(esEmprendedor);

		sesion.letraTC = letraTc;
		sesion.codigoPaqueteMotor = nroProducto;
		sesion.resolucionMotorDeScoring = respuestaMotor.resolucion();
		sesion.modoAprobacion = respuestaMotor.modoAprobacion();
		sesion.limite = respuestaMotor.limiteCompra();
		sesion.tarjetaOferta = sesion.crearTarjetaOferta(respuestaMotor.tarjetaOfrecida());
		sesion.estado = EstadosBB.OFERTAS;
		sesion.save();

		LogBB.evento(contexto, EstadosBB.OFERTAS, nroProducto);
		return TarjetaBeneficio.ofertaLetra(nroProducto, letraTc, respuestaMotor.limiteCompra());
	}

	private static boolean esEmprendedor(ContextoBB contexto){
		try{
			PadronAfip padronAfip = SqlEsales.getPadronAfip(contexto, contexto.sesion().cuil).tryGet();
			if (!empty(padronAfip) && !empty(padronAfip.monotributo)) {
				if (GeneralBB.CATEGORIAS.stream().anyMatch(categoria -> categoria.equals(padronAfip.monotributo.trim())) || GeneralBB.RESPONSABLE_INSCRIPTO.equals(padronAfip.monotributo)) {
					LogBB.eventoHomo(contexto, "EXISTE_EMPRENDEDOR");
					return true;
				}
			}
		}
		catch(Exception e){}
		LogBB.eventoHomo(contexto, "SIN_EMPRENDEDOR");
		return false;
	}

	public static Objeto ofertasStandalone(ContextoBB contexto) {
		LogBB.evento(contexto, "OFERTA_STANDALONE");
		boolean buscarInversor = contexto.parametros.bool("buscarInversor", false);
		return TarjetaBeneficio.ofertaLetra(buscarInversor ? "4" : "0", "", new BigDecimal("0.0"));
	}

	public static Objeto aceptarOfertaMotor(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();

		boolean solicitaAerolineas = contexto.parametros.bool("solicitaAerolineas", false);
		String nroProducto = contexto.parametros.string("nroProducto", "0");
		if(empty(nroProducto)){
			nroProducto = "0";
		}

		if(empty(sesion.codigoPaqueteMotor)){
			if(!nroProducto.equals("0") && !nroProducto.equals("1") && !nroProducto.equals("4")){
				LogBB.evento(contexto, "ERROR_ELEGIR_OFERTA");
				return respuesta("ERROR_PRODUCTO");
			}
		}
		else{
			if(!nroProducto.equals("0") && !nroProducto.equals("1") && !nroProducto.equals("4")
					&& !nroProducto.equals(sesion.codigoPaqueteMotor)){
				LogBB.evento(contexto, "ERROR_ELEGIR_OFERTA");
				return respuesta("ERROR_PRODUCTO");
			}
		}

		if(sesion.esStandalone()){
			solicitaAerolineas = false;
		}

		sesion.ofertaElegida = nroProducto;
		sesion.subProducto = solicitaAerolineas ? GeneralBB.SUB_AEROLINEAS : GeneralBB.SUB_BUHO_PUNTOS;
		sesion.bbInversorAceptada = GeneralBB.CUENTA_INVERSOR_ACEPTADA;
		sesion.tdFisica = GeneralBB.VISUALIZA_N;
		sesion.tdVirtual = true;
		sesion.tcVirtual = !sesion.esStandalone() && sesion.getParamTcOnline(contexto);
		sesion.estado = EstadosBB.ELEGIR_OFERTA_OK;
		sesion.save();

		LogBB.evento(contexto, EstadosBB.ELEGIR_OFERTA_OK, nroProducto);
		return respuesta();
	}

	public static Objeto ofertaSesion(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<?> ventaTempranaFuture = executor.submit(() -> {
			if (sesion.esStandalone()) {
				BBAlta.ventaTempranaStandalone(contexto);
			} else {
				BBAlta.ventaTempranaPaquete(contexto);
			}
		});

		try {
			ventaTempranaFuture.get(15, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			LogBB.eventoHomo(contexto, "VENTA_TEMPRANA_TIMEOUT");
			ventaTempranaFuture.cancel(true);
		} catch (Exception e) { }
		finally {
			executor.shutdownNow();
		}

		contexto.sesion().actualizarEstado("OBTENER_OFERTA_SESION_OK");

		String urlTyc = sesion.esStandalone() ?
				"https://www.hipotecario.com.ar/media/f2001-terminos-condiciones-caja-de-ahoro-homebanking-072025.pdf"
				: "https://www.hipotecario.com.ar/media/f3145-contrato-paquetes-unificado-hb-tyc-082025.pdf";

		LogBB.evento(contexto, "OBTENER_OFERTA_SESION");
		return respuesta()
				.set("urlTyc", urlTyc)
				.set("beneficios",
				sesion.esStandalone() ? TarjetaBeneficio.ofertaAceptadaStandalone(sesion.ofertaElegida)
						: TarjetaBeneficio.ofertaAceptada());
	}
}

