package ar.com.hipotecario.canal.buhobank;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.servicio.sql.SqlBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBBeneficiosBuhobank.BBBeneficioBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBContenidosDinamicoBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBContenidosDinamicoBuhobank.BBContenidoDinamicoBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBContenidosDinamicoPaqueteBuhobank.BBContenidoDinamicoPaqueteBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPaquetesBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPaquetesBuhobank.BBPaqueteBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPaquetesSubproductoBuhobank.BBPaqueteSubproductoBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBParametriasBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBParametriasBuhobank.BBParametriaBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBParametriasGeneralBuhobank.BBParametriaGeneralBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPlantillasDopplerBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPlantillasDopplerBuhobank.BBPlantillaDopplerBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPlantillasFlujoBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPlantillasFlujoBuhobank.BBPlantillaFlujoBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBSucursalesBuhobank.BBSucursalBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasBeneficioBuhobank.BBTarjetaBeneficioBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasBeneficioLandingBuhobank.BBTarjetaBeneficioLandingBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasBuhobank.BBTarjetaBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasFinalizarBatchBuhobank.BBTarjetaFinalizarBatchBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasFinalizarBuhobank.BBTarjetaFinalizarBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasLandingBuhobank.BBTarjetaLandingBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBVistasBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBVistasBuhobank.BBVistaBuhobank;

public class BBConfigManual extends Modulo {
	
	public static Object getConfigManual(ContextoBB contexto) {
		String config = contexto.parametros.string("config");
		
		if(config.equals("plantillas_flujo")) {
			return respuesta("plantillas_flujo", SqlBuhoBank.obtenerPlantillasFlujoBuhobank(contexto).tryGet());
		}
		
		if(config.equals("parametrias")) {
			return respuesta("parametrias", SqlBuhoBank.obtenerParametriasBuhobank(contexto).tryGet());
		}
		
		if(config.equals("parametrias_general")) {
			return respuesta("parametrias_general", SqlBuhoBank.obtenerParametriasGeneral(contexto).tryGet());
		}
		
		if(config.equals("doppler")) {
			return respuesta("dopler", SqlBuhoBank.obtenerPlantillasDopplerBuhobank(contexto).tryGet());
		}
		
		if(config.equals("vistas")) {
			return respuesta("vistas", SqlBuhoBank.obtenerVistasBuhobank(contexto).tryGet());
		}
		
		if(config.equals("contenido_dinamico")) {
			return respuesta("contenido_dinamico", SqlBuhoBank.obtenerContenidosDinamicoBuhobank(contexto).tryGet());
		}
		
		if(config.equals("contenidos_dinamico_paquete")) {
			return respuesta("contenidos_dinamico_paquete", SqlBuhoBank.obtenerContenidosDinamicoPaquetesBuhobank(contexto).tryGet());
		}
		
		if(config.equals("paquetes")) {
			return respuesta("paquetes", SqlBuhoBank.obtenerPaquetesBuhobank(contexto).tryGet());
		}
		
		if(config.equals("paquetes_subproducto")) {
			return respuesta("paquetes_subproducto", SqlBuhoBank.obtenerPaquetesSubproductoBuhobank(contexto).tryGet());
		}
		
		if(config.equals("tarjetas")) {
			return respuesta("tarjetas", SqlBuhoBank.obtenerTarjetasBuhobank(contexto).tryGet());
		}
		
		if(config.equals("tarjetas_beneficio")) {
			return respuesta("tarjetas_beneficio", SqlBuhoBank.obtenerTarjetasBeneficioBuhobank(contexto).tryGet());
		}
		
		if(config.equals("tarjetas_finalizar")) {
			return respuesta("tarjetas_finalizar", SqlBuhoBank.obtenerTarjetasFinalizarBuhobank(contexto).tryGet());
		}
		
		if(config.equals("tarjetas_finalizar_batch")) {
			return respuesta("tarjetas_finalizar_batch", SqlBuhoBank.obtenerTarjetasFinalizarBatchBuhobank(contexto).tryGet());
		}
		
		if(config.equals("tarjetas_landing")) {
			return respuesta("tarjetas_landing", SqlBuhoBank.obtenerTarjetasLandingBuhobank(contexto).tryGet());
		}
		
		if(config.equals("tarjetas_landing_beneficio")) {
			return respuesta("tarjetas_landing_beneficio", SqlBuhoBank.obtenerTarjetasBeneficioLandingBuhobank(contexto).tryGet());
		}
		
		if(config.equals("sucursales")) {
			return respuesta("sucursales", SqlBuhoBank.obtenerSucursalesBuhobank(contexto).tryGet());
		}
		
		if(config.equals("beneficios")) {
			return respuesta("beneficios", SqlBuhoBank.obtenerBeneficiosBuhobank(contexto).tryGet());
		}
		
		return respuesta("ERROR_NO_ENCONTRADO");
	}
	
	public static Object deleteConfigManual(ContextoBB contexto) {
		String config = contexto.parametros.string("config");
		Integer id = contexto.parametros.integer("id");
		
		Boolean resDelete = false;
		
		if(config.equals("plantillas_flujo")) {
			resDelete = SqlBuhoBank.borrarPlantillaFlujoBuhobank(contexto, id).tryGet();
		}
		
		if(config.equals("parametrias")) {
			resDelete = SqlBuhoBank.borrarParametriasBuhobank(contexto, id).tryGet();
		}
		
		if(config.equals("parametrias_general")) {
			resDelete = SqlBuhoBank.borrarParametriasGeneralBuhobank(contexto, id).tryGet();
		}
		
		if(config.equals("doppler")) {
			resDelete = SqlBuhoBank.borrarPlantillaDopplerBuhobank(contexto, id).tryGet();
		}
		
		if(config.equals("vistas")) {
			resDelete = SqlBuhoBank.borrarVistaBuhobank(contexto, id).tryGet();
		}
		
		if(config.equals("contenido_dinamico")) {
			resDelete = SqlBuhoBank.borrarContenidoDinamicoBuhobank(contexto, id).tryGet();
		}
		
		if(config.equals("contenidos_dinamico_paquete")) {
			resDelete = SqlBuhoBank.borrarContenidoDinamicoPaqueteBuhobank(contexto, id).tryGet();
		}
		
		if(config.equals("paquetes")) {
			resDelete = SqlBuhoBank.borrarPaqueteBuhobank(contexto, id).tryGet();
		}
		
		if(config.equals("paquetes_subproducto")) {
			resDelete = SqlBuhoBank.borrarPaqueteSubproductoBuhobank(contexto, id).tryGet();
		}
		
		if(config.equals("tarjetas")) {
			resDelete = SqlBuhoBank.borrarTarjetaBuhobank(contexto, id).tryGet();
		}
		
		if(config.equals("tarjetas_beneficio")) {
			resDelete = SqlBuhoBank.borrarTarjetaBeneficioBuhobank(contexto, id).tryGet();
		}
		
		if(config.equals("tarjetas_finalizar")) {
			resDelete = SqlBuhoBank.borrarTarjetaFinalizarBuhobank(contexto, id).tryGet();
		}
		
		if(config.equals("tarjetas_finalizar_batch")) {
			resDelete = SqlBuhoBank.borrarTarjetaFinalizarBatchBuhobank(contexto, id).tryGet();
		}
		
		if(config.equals("tarjetas_landing")) {
			resDelete = SqlBuhoBank.borrarTarjetaLandingBuhobank(contexto, id).tryGet();
		}
		
		if(config.equals("tarjetas_landing_beneficio")) {
			resDelete = SqlBuhoBank.borrarTarjetaBeneficioLandingBuhobank(contexto, id).tryGet();
		}
		
		if(config.equals("sucursales")) {
			resDelete = SqlBuhoBank.borrarSucursalBuhobank(contexto, id).tryGet();
		}
		
		if(config.equals("beneficios")) {
			resDelete = SqlBuhoBank.borrarBeneficioBuhobank(contexto, id).tryGet();
		}
		
		if (resDelete) {
			return respuesta();
		}
		
		return respuesta("ERROR");
	}
	
	public static Object postConfigManual(ContextoBB contexto) {
		String config = contexto.parametros.string("config");

		if(config.equals("plantillas_flujo")) {
			return crearPlantillaFlujoBuhobank(contexto);
		}
		
		if(config.equals("parametrias")) {
			return crearParametriaBuhobank(contexto);
		}
		
		if(config.equals("parametrias_general")) {
			return crearParametriaGeneralBuhobank(contexto);
		}
		
		if(config.equals("doppler")) {
			return crearPlantillaDopplerBuhobank(contexto);
		}
		
		if(config.equals("vistas")) {
			return crearVistaBuhobank(contexto);
		}
		
		if(config.equals("contenido_dinamico")) {
			return crearContenidoDinamicoBuhobank(contexto);
		}
		
		if(config.equals("contenidos_dinamico_paquete")) {
			return crearContenidoDinamicoPaqueteBuhobank(contexto);
		}
		
		if(config.equals("paquetes")) {
			return crearPaqueteBuhobank(contexto);
		}
		
		if(config.equals("paquetes_subproducto")) {
			return actualizarPaqueteSubproductoBuhobank(contexto);
		}
		
		if(config.equals("tarjetas")) {
			return crearTarjetaBuhobank(contexto);
		}
		
		if(config.equals("tarjetas_beneficio")) {
			return crearTarjetaBeneficioBuhobank(contexto);
		}
		
		if(config.equals("tarjetas_finalizar")) {
			return crearTarjetaFinalizarBuhobank(contexto);
		}
		
		if(config.equals("tarjetas_finalizar_batch")) {
			return crearTarjetaFinalizarBatchBuhobank(contexto);
		}
		
		if(config.equals("tarjetas_landing")) {
			return actualizarTarjetaLandingBuhobank(contexto);
		}
		
		if(config.equals("tarjetas_landing_beneficio")) {
			return crearTarjetaBeneficioLandingBuhobank(contexto);
		}
		
		if(config.equals("sucursales")) {
			return crearSucursalesBuhobank(contexto);
		}
		
		if(config.equals("beneficios")) {
			return crearBeneficiosBuhobank(contexto);
		}
	
		return respuesta("ERROR_NO_ENCONTRADO");
	}
	
	public static Object putConfigManual(ContextoBB contexto) {
		String config = contexto.parametros.string("config");

		if(config.equals("plantillas_flujo")) {
			return actualizarPlantillaFlujoBuhobank(contexto);
		}
		
		if(config.equals("parametrias")) {
			return actualizarParametriaBuhobank(contexto);
		}
		
		if(config.equals("parametrias_general")) {
			return actualizarParametriaGeneralBuhobank(contexto);
		}
		
		if(config.equals("doppler")) {
			return actualizarPlantillaDopplerBuhobank(contexto);
		}
		
		if(config.equals("vistas")) {
			return actualizarVistaBuhobank(contexto);
		}
		
		if(config.equals("contenido_dinamico")) {
			return actualizarContenidoDinamicoBuhobank(contexto);
		}
		
		if(config.equals("contenidos_dinamico_paquete")) {
			return actualizarContenidoDinamicoPaqueteBuhobank(contexto);
		}
		
		if(config.equals("paquetes")) {
			return actualizarPaqueteBuhobank(contexto);
		}
		
		if(config.equals("paquetes_subproducto")) {
			return actualizarPaqueteSubproductoBuhobank(contexto);
		}
		
		if(config.equals("tarjetas")) {
			return actualizarTarjetaBuhobank(contexto);
		}
		
		if(config.equals("tarjetas_beneficio")) {
			return actualizarBeneficioTarjetaBuhobank(contexto);
		}
		
		if(config.equals("tarjetas_finalizar")) {
			return actualizarFinalizarTarjetaBuhobank(contexto);
		}
		
		if(config.equals("tarjetas_finalizar_batch")) {
			return actualizarFinalizarTarjetaBatchBuhobank(contexto);
		}
		
		if(config.equals("tarjetas_landing")) {
			return crearTarjetaLandingBuhobank(contexto);
		}
		
		if(config.equals("tarjetas_landing_beneficio")) {
			return actualizarBeneficioTarjetaLandingBuhobank(contexto);
		}
		
		if(config.equals("sucursales")) {
			return actualizarSucursalesBuhobank(contexto);
		}
		
		if(config.equals("beneficios")) {
			return actualizarBeneficiosBuhobank(contexto);
		}
	
		return respuesta("ERROR_NO_ENCONTRADO");
	}

	public static Object crearPlantillaFlujoBuhobank(ContextoBB contexto) {
		String nombrePlantilla = contexto.parametros.string("plantilla");
		Integer idPlantillaBase = contexto.parametros.integer("id_plantilla_base");

		BBPlantillasFlujoBuhobank plantillasFlujo = SqlBuhoBank.obtenerPlantillasFlujoBuhobank(contexto).tryGet();
		if (BBPlantillasFlujoBuhobank.buscarNombre(plantillasFlujo, nombrePlantilla) != null) {
			return respuesta("ERROR_YA_EXISTE_PLANTILLA");
		}

		if (!BBPlantillasFlujoBuhobank.existeIdPlantilla(plantillasFlujo, idPlantillaBase)) {
			return respuesta("ERROR_ID_PLANTILLA_NO_ENCONTRADA");
		}

		BBPlantillaFlujoBuhobank nuevaPlantillaFlujo = new BBPlantillaFlujoBuhobank();
		nuevaPlantillaFlujo.plantilla = nombrePlantilla;

		Boolean resPost = SqlBuhoBank.crearPlantillasFlujoBuhobank(contexto, nuevaPlantillaFlujo).tryGet();
		if (!resPost) {
			return respuesta("ERROR_POST");
		}

		plantillasFlujo = SqlBuhoBank.obtenerPlantillasFlujoBuhobank(contexto).tryGet();
		nuevaPlantillaFlujo = BBPlantillasFlujoBuhobank.buscarNombre(plantillasFlujo, nombrePlantilla);

		if (nuevaPlantillaFlujo != null) {
			clonarPlantillaFlujo(contexto, nuevaPlantillaFlujo.id, idPlantillaBase);
		}

		return respuesta("plantillas_flujo", plantillasFlujo);
	}

	private static void clonarPlantillaFlujo(ContextoBB contexto, Integer idNuevaPlantillaFlujo, Integer idPlantillaBase) {
		BBParametriasBuhobank.clonar(contexto, idPlantillaBase, idNuevaPlantillaFlujo);
		BBPlantillasDopplerBuhobank.clonar(contexto, idPlantillaBase, idNuevaPlantillaFlujo);
		BBVistasBuhobank.clonar(contexto, idPlantillaBase, idNuevaPlantillaFlujo);
		BBContenidosDinamicoBuhobank.clonar(contexto, idPlantillaBase, idNuevaPlantillaFlujo);
		BBPaquetesBuhobank.clonarTotal(contexto, idPlantillaBase, idNuevaPlantillaFlujo);
	}

	public static Object actualizarPlantillaFlujoBuhobank(ContextoBB contexto) {
		Integer id = contexto.parametros.integer("id");
		String nombrePlantilla = contexto.parametros.string("plantilla");

		BBPlantillaFlujoBuhobank plantilla = new BBPlantillaFlujoBuhobank();
		plantilla.id = id;
		plantilla.plantilla = nombrePlantilla;

		Boolean resPut = SqlBuhoBank.actualizarPlantillasFlujoBuhobank(contexto, plantilla).tryGet();
		if (!resPut) {
			return respuesta("ERROR_PUT");
		}

		return respuesta();
	}

	public static Object crearPaqueteBuhobank(ContextoBB contexto) {
		Integer idPlantillaFlujo = contexto.parametros.integer("id_plantilla_flujo");
		Integer numeroPaquete = contexto.parametros.integer("numero_paquete");
		String letraTc = contexto.parametros.string("letra_tc", null);
		String nombre = contexto.parametros.string("nombre");
		Boolean esStandalone = contexto.parametros.bool("es_standalone");
		Boolean esEmprendedor = contexto.parametros.bool("es_emprendedor");
		Boolean tdVirtual = contexto.parametros.bool("td_virtual");
		Boolean tcVirtual = contexto.parametros.bool("tc_virtual");
		Boolean envioSucursal = contexto.parametros.bool("envio_sucursal");
		Boolean cuentaInversor = contexto.parametros.bool("cuenta_inversor", null);
		Integer afinidad = contexto.parametros.integer("afinidad", null);
		Integer modeloLiquidacion = contexto.parametros.integer("modelo_liquidacion", null);
		Integer codigoDistribucion = contexto.parametros.integer("codigo_distribucion", null);
		String ciclo = contexto.parametros.string("ciclo", null);
		String caracteristica = contexto.parametros.string("caracteristica", null);

		BBPaqueteBuhobank nuevoPaquete = new BBPaqueteBuhobank();
		nuevoPaquete.id_plantilla_flujo = idPlantillaFlujo;
		nuevoPaquete.numero_paquete = numeroPaquete;
		nuevoPaquete.letra_tc = letraTc;
		nuevoPaquete.nombre = nombre;
		nuevoPaquete.es_standalone = esStandalone;
		nuevoPaquete.es_emprendedor = esEmprendedor;
		nuevoPaquete.td_virtual = tdVirtual;
		nuevoPaquete.tc_virtual = tcVirtual;
		nuevoPaquete.envio_sucursal = envioSucursal;
		nuevoPaquete.cuenta_inversor = cuentaInversor;
		nuevoPaquete.afinidad = afinidad;
		nuevoPaquete.modelo_liquidacion = modeloLiquidacion;
		nuevoPaquete.codigo_distribucion = codigoDistribucion;
		nuevoPaquete.ciclo = ciclo;
		nuevoPaquete.caracteristica = caracteristica;

		Boolean resPost = SqlBuhoBank.crearPaquetesBuhobank(contexto, nuevoPaquete).tryGet();
		if (!resPost) {
			return respuesta("ERROR_POST");
		}

		return respuesta();
	}

	public static Object actualizarPaqueteBuhobank(ContextoBB contexto) {
		Integer id = contexto.parametros.integer("id");
		Integer idPlantillaFlujo = contexto.parametros.integer("id_plantilla_flujo");
		Integer numeroPaquete = contexto.parametros.integer("numero_paquete");
		String letraTc = contexto.parametros.string("letra_tc", null);
		String nombre = contexto.parametros.string("nombre");
		Boolean esStandalone = contexto.parametros.bool("es_standalone");
		Boolean esEmprendedor = contexto.parametros.bool("es_emprendedor");
		Boolean tdVirtual = contexto.parametros.bool("td_virtual");
		Boolean tcVirtual = contexto.parametros.bool("tc_virtual");
		Boolean envioSucursal = contexto.parametros.bool("envio_sucursal");
		Boolean cuentaInversor = contexto.parametros.bool("cuenta_inversor", null);
		Integer afinidad = contexto.parametros.integer("afinidad", null);
		Integer modeloLiquidacion = contexto.parametros.integer("modelo_liquidacion", null);
		Integer codigoDistribucion = contexto.parametros.integer("codigo_distribucion", null);
		String ciclo = contexto.parametros.string("ciclo", null);
		String caracteristica = contexto.parametros.string("caracteristica", null);		
		Boolean tdVisualizaVirtual = contexto.parametros.bool("td_visualiza_virtual", null);
		Integer paqBase = contexto.parametros.integer("paq_base", null);

		BBPaqueteBuhobank paquete = new BBPaqueteBuhobank();
		paquete.id = id;
		paquete.id_plantilla_flujo = idPlantillaFlujo;
		paquete.numero_paquete = numeroPaquete;
		paquete.letra_tc = letraTc;
		paquete.nombre = nombre;
		paquete.es_standalone = esStandalone;
		paquete.es_emprendedor = esEmprendedor;
		paquete.td_virtual = tdVirtual;
		paquete.tc_virtual = tcVirtual;
		paquete.envio_sucursal = envioSucursal;
		paquete.cuenta_inversor = cuentaInversor;
		paquete.afinidad = afinidad;
		paquete.modelo_liquidacion = modeloLiquidacion;
		paquete.codigo_distribucion = codigoDistribucion;
		paquete.ciclo = ciclo;
		paquete.caracteristica = caracteristica;
		paquete.td_visualiza_virtual = tdVisualizaVirtual;
		paquete.paq_base = paqBase;

		Boolean resPut = SqlBuhoBank.actualizarPaquetesBuhobank(contexto, paquete).tryGet();
		if (!resPut) {
			return respuesta("ERROR_PUT");
		}

		return respuesta();
	}

	public static Object crearParametriaBuhobank(ContextoBB contexto) {
		Integer idPlantillaFlujo = contexto.parametros.integer("id_plantilla_flujo");
		String nombre = contexto.parametros.string("nombre");
		String valorAndroid = contexto.parametros.string("valor_android");
		String valorIos = contexto.parametros.string("valor_ios");

		BBParametriaBuhobank parametria = new BBParametriaBuhobank();
		parametria.id_plantilla_flujo = idPlantillaFlujo;
		parametria.nombre = nombre;
		parametria.valor_android = valorAndroid;
		parametria.valor_ios = valorIos;

		Boolean resPost = SqlBuhoBank.crearParametriasBuhobank(contexto, parametria).tryGet();
		if (!resPost) {
			return respuesta("ERROR_POST");
		}

		return respuesta();
	}

	public static Object actualizarParametriaBuhobank(ContextoBB contexto) {
		Integer id = contexto.parametros.integer("id");
		Integer idPlantillaFlujo = contexto.parametros.integer("id_plantilla_flujo");
		String nombre = contexto.parametros.string("nombre");
		String valorAndroid = contexto.parametros.string("valor_android");
		String valorIos = contexto.parametros.string("valor_ios");

		BBParametriaBuhobank parametria = new BBParametriaBuhobank();
		parametria.id = id;
		parametria.id_plantilla_flujo = idPlantillaFlujo;
		parametria.nombre = nombre;
		parametria.valor_android = valorAndroid;
		parametria.valor_ios = valorIos;

		Boolean resPut = SqlBuhoBank.actualizarParametriasBuhobank(contexto, parametria).tryGet();
		if (!resPut) {
			return respuesta("ERROR_PUT");
		}

		return respuesta();
	}

	public static Object crearParametriaGeneralBuhobank(ContextoBB contexto) {
		String nombre = contexto.parametros.string("nombre");
		String valor = contexto.parametros.string("valor");

		BBParametriaGeneralBuhobank nuevaParametria = new BBParametriaGeneralBuhobank();
		nuevaParametria.nombre = nombre;
		nuevaParametria.valor = valor;

		Boolean resPost = SqlBuhoBank.crearParametriasGeneralBuhobank(contexto, nuevaParametria).tryGet();
		if (!resPost) {
			return respuesta("ERROR_POST");
		}

		return respuesta();
	}

	public static Object actualizarParametriaGeneralBuhobank(ContextoBB contexto) {
		Integer id = contexto.parametros.integer("id");
		String nombre = contexto.parametros.string("nombre");
		String valor = contexto.parametros.string("valor");
		
		BBParametriaGeneralBuhobank parametria = new BBParametriaGeneralBuhobank();
		parametria.id = id;
		parametria.nombre = nombre;
		parametria.valor = valor;

		Boolean resPut = SqlBuhoBank.actualizarParametriasGeneralBuhobank(contexto, parametria).tryGet();
		if (!resPut) {
			return respuesta("ERROR_PUT");
		}

		return respuesta();
	}

	public static Object crearPlantillaDopplerBuhobank(ContextoBB contexto) {
		Integer idPlantillaFlujo = contexto.parametros.integer("id_plantilla_flujo");
		String tipo = contexto.parametros.string("tipo");
		String codigo = contexto.parametros.string("codigo");
		String asunto = contexto.parametros.string("asunto");

		BBPlantillaDopplerBuhobank plantillaDoppler = new BBPlantillaDopplerBuhobank();
		plantillaDoppler.id_plantilla_flujo = idPlantillaFlujo;
		plantillaDoppler.tipo = tipo;
		plantillaDoppler.codigo = codigo;
		plantillaDoppler.asunto = asunto;

		Boolean resPost = SqlBuhoBank.crearPlantillaDopplerBuhobank(contexto, plantillaDoppler).tryGet();
		if (!resPost) {
			return respuesta("ERROR_POST");
		}

		return respuesta();
	}

	public static Object actualizarPlantillaDopplerBuhobank(ContextoBB contexto) {
		Integer id = contexto.parametros.integer("id");
		Integer idPlantillaFlujo = contexto.parametros.integer("id_plantilla_flujo");
		String tipo = contexto.parametros.string("tipo");
		String codigo = contexto.parametros.string("codigo");
		String asunto = contexto.parametros.string("asunto");

		BBPlantillaDopplerBuhobank plantillaDoppler = new BBPlantillaDopplerBuhobank();
		plantillaDoppler.id = id;
		plantillaDoppler.id_plantilla_flujo = idPlantillaFlujo;
		plantillaDoppler.tipo = tipo;
		plantillaDoppler.codigo = codigo;
		plantillaDoppler.asunto = asunto;

		Boolean resPut = SqlBuhoBank.actualizarPlantillaDopplerBuhobank(contexto, plantillaDoppler).tryGet();
		if (!resPut) {
			return respuesta("ERROR_PUT");
		}

		return respuesta();
	}

	public static Object crearVistaBuhobank(ContextoBB contexto) {
		Integer idPlantillaFlujo = contexto.parametros.integer("id_plantilla_flujo");
		Integer ordenVista = contexto.parametros.integer("orden_vista", null);
		String codigoVista = contexto.parametros.string("codigo_vista");
		Boolean retomaSesion = contexto.parametros.bool("retoma_sesion");
		String contenido = contexto.parametros.string("contenido");
		Boolean habilitado = contexto.parametros.bool("habilitado");

		BBPlantillasFlujoBuhobank plantillasFlujo = SqlBuhoBank.obtenerPlantillasFlujoBuhobank(contexto).tryGet();
		if (!BBPlantillasFlujoBuhobank.existeIdPlantilla(plantillasFlujo, idPlantillaFlujo)) {
			return respuesta("ERROR_ID_PLANTILLA_NO_ENCONTRADA");
		}

		BBVistasBuhobank vistasBuhobank = SqlBuhoBank.obtenerVistasBuhobank(contexto).tryGet();
		BBVistasBuhobank vistasById = BBVistasBuhobank.buscarIdPlantilla(vistasBuhobank, idPlantillaFlujo);
		if (vistasById == null) {
			return respuesta("ERROR");
		}

		if (BBVistasBuhobank.buscarCodigo(vistasById, codigoVista) != null) {
			return respuesta("ERROR_YA_EXISTE_VISTA");
		}

		if (ordenVista != null && ordenVista > vistasById.size() + 1) {
			return respuesta("ERROR_ORDEN");
		}

		if (ordenVista == null) {
			ordenVista = vistasById.size() + 1;
		}

		BBVistaBuhobank nuevaVista = new BBVistaBuhobank();
		nuevaVista.id_plantilla_flujo = idPlantillaFlujo;
		nuevaVista.orden_vista = vistasById.size() + 1;
		nuevaVista.codigo_vista = codigoVista;
		nuevaVista.retoma_sesion = retomaSesion;
		nuevaVista.contenido = contenido;
		nuevaVista.habilitado = habilitado;

		Boolean resPost = SqlBuhoBank.crearVistaBuhobank(contexto, nuevaVista).tryGet();
		if (!resPost) {
			return respuesta("ERROR_POST");
		}

		vistasBuhobank = SqlBuhoBank.obtenerVistasBuhobank(contexto).tryGet();
		vistasById = BBVistasBuhobank.buscarIdPlantilla(vistasBuhobank, idPlantillaFlujo);
		BBVistaBuhobank vista = BBVistasBuhobank.buscarCodigo(vistasById, codigoVista);
		if (vista == null) {
			return respuesta("ERROR_VISTA_NO_ENCONTRADA");
		}

		cambiarOrdenVista(contexto, vistasById, vista, ordenVista);

		vistasBuhobank = SqlBuhoBank.obtenerVistasBuhobank(contexto).tryGet();
		return respuesta("vistas", BBVistasBuhobank.buscarIdPlantilla(vistasBuhobank, idPlantillaFlujo));
	}

	private static void cambiarOrdenVista(ContextoBB contexto, BBVistasBuhobank vistas, BBVistaBuhobank vistaAux, Integer nuevoOrden) {
		vistaAux.orden_vista = nuevoOrden;
		SqlBuhoBank.actualizarVistaBuhobank(contexto, vistaAux);

		int ordenActual = 1;
		for (BBVistaBuhobank vista : vistas) {
			if (!vistaAux.id.equals(vista.id)) {

				if (nuevoOrden.equals(ordenActual)) {
					ordenActual++;
				}

				vista.orden_vista = ordenActual;
				SqlBuhoBank.actualizarVistaBuhobank(contexto, vista);
				ordenActual++;
			}
		}

	}

	public static Object actualizarVistaBuhobank(ContextoBB contexto) {
		Integer id = contexto.parametros.integer("id");
		Integer idPlantillaFlujo = contexto.parametros.integer("id_plantilla_flujo");
		Integer ordenVista = contexto.parametros.integer("orden_vista");
		String codigoVista = contexto.parametros.string("codigo_vista");
		Boolean retomaSesion = contexto.parametros.bool("retoma_sesion");
		String contenido = contexto.parametros.string("contenido");
		Boolean habilitado = contexto.parametros.bool("habilitado");
		
		BBPlantillasFlujoBuhobank plantillasFlujo = SqlBuhoBank.obtenerPlantillasFlujoBuhobank(contexto).tryGet();
		if (!BBPlantillasFlujoBuhobank.existeIdPlantilla(plantillasFlujo, idPlantillaFlujo)) {
			return respuesta("ERROR_ID_PLANTILLA_NO_ENCONTRADA");
		}

		BBVistasBuhobank vistasBuhobank = SqlBuhoBank.obtenerVistasBuhobank(contexto).tryGet();
		BBVistaBuhobank vistaBuhobank = BBVistasBuhobank.buscarId(vistasBuhobank, id);
		if (vistaBuhobank == null) {
			return respuesta("ERROR_VISTA_NO_ENCONTRADA");
		}

		if (!idPlantillaFlujo.equals(vistaBuhobank.id_plantilla_flujo)) {
			return respuesta("ERROR_ID_PLANTILLA_NO_COINCIDE");
		}

		BBVistasBuhobank vistasById = BBVistasBuhobank.buscarIdPlantilla(vistasBuhobank, idPlantillaFlujo);
		if (vistasById == null) {
			return respuesta("ERROR");
		}

		if (ordenVista > vistasById.size() + 1) {
			return respuesta("ERROR_ORDEN");
		}

		BBVistaBuhobank vista = new BBVistaBuhobank();
		vista.id = id;
		vista.id_plantilla_flujo = idPlantillaFlujo;
		vista.orden_vista = vistaBuhobank.orden_vista;
		vista.codigo_vista = codigoVista;
		vista.retoma_sesion = retomaSesion;
		vista.contenido = contenido;
		vista.habilitado = habilitado;

		Boolean resPut = SqlBuhoBank.actualizarVistaBuhobank(contexto, vista).tryGet();
		if (!resPut) {
			return respuesta("ERROR_PUT");
		}

		if (!vista.orden_vista.equals(ordenVista)) {
			cambiarOrdenVista(contexto, vistasById, vista, ordenVista);
		}

		vistasBuhobank = SqlBuhoBank.obtenerVistasBuhobank(contexto).tryGet();
		return respuesta("vistas", BBVistasBuhobank.buscarIdPlantilla(vistasBuhobank, idPlantillaFlujo));
	}

	public static Object crearTarjetaBuhobank(ContextoBB contexto) {
		Integer idPaquete = contexto.parametros.integer("id_paquete");
		String tipo = contexto.parametros.string("tipo");
		String nombreCorto = contexto.parametros.string("nombre_corto", null);
		String descripcion = contexto.parametros.string("descripcion");
		Boolean virtual = contexto.parametros.bool("virtual");
		String tarjeta = contexto.parametros.string("tarjeta");
		String titulo = contexto.parametros.string("titulo");
		String tituloLanding = contexto.parametros.string("titulo_landing", null);
		String box = contexto.parametros.string("box");
		String boton = contexto.parametros.string("boton");

		BBTarjetaBuhobank nuevaTarjeta = new BBTarjetaBuhobank();
		nuevaTarjeta.id_paquete = idPaquete;
		nuevaTarjeta.tipo = tipo;
		nuevaTarjeta.nombre_corto = nombreCorto;
		nuevaTarjeta.descripcion = descripcion;
		nuevaTarjeta.virtual = virtual;
		nuevaTarjeta.tarjeta = tarjeta;
		nuevaTarjeta.titulo = titulo;
		nuevaTarjeta.titulo_landing = tituloLanding;
		nuevaTarjeta.box = box;
		nuevaTarjeta.boton = boton;

		Boolean resPost = SqlBuhoBank.crearTarjetaBuhobank(contexto, nuevaTarjeta).tryGet();
		if (!resPost) {
			return respuesta("ERROR_POST");
		}

		return respuesta();
	}

	public static Object actualizarTarjetaBuhobank(ContextoBB contexto) {
		Integer id = contexto.parametros.integer("id");
		Integer idPaquete = contexto.parametros.integer("id_paquete");
		String tipo = contexto.parametros.string("tipo");
		String nombreCorto = contexto.parametros.string("nombre_corto", null);
		String descripcion = contexto.parametros.string("descripcion");
		Boolean virtual = contexto.parametros.bool("virtual");
		String tarjeta = contexto.parametros.string("tarjeta");
		String titulo = contexto.parametros.string("titulo");
		String tituloLanding = contexto.parametros.string("titulo_landing", null);
		String box = contexto.parametros.string("box");
		String boton = contexto.parametros.string("boton");

		BBTarjetaBuhobank nuevaTarjeta = new BBTarjetaBuhobank();
		nuevaTarjeta.id = id;
		nuevaTarjeta.id_paquete = idPaquete;
		nuevaTarjeta.tipo = tipo;
		nuevaTarjeta.nombre_corto = nombreCorto;
		nuevaTarjeta.descripcion = descripcion;
		nuevaTarjeta.virtual = virtual;
		nuevaTarjeta.tarjeta = tarjeta;
		nuevaTarjeta.titulo = titulo;
		nuevaTarjeta.titulo_landing = tituloLanding;
		nuevaTarjeta.box = box;
		nuevaTarjeta.boton = boton;

		Boolean resPut = SqlBuhoBank.actualizarTarjetaBuhobank(contexto, nuevaTarjeta).tryGet();
		if (!resPut) {
			return respuesta("ERROR_PUT");
		}

		return respuesta();
	}

	public static Object crearTarjetaBeneficioBuhobank(ContextoBB contexto) {
		Integer idTarjeta = contexto.parametros.integer("id_tarjeta");
		String descBeneficio = contexto.parametros.string("desc_beneficio");
		String descBeneficio_html = contexto.parametros.string("desc_beneficio_html");
		String iconoId = contexto.parametros.string("icono_id");
		String iconoDesc = contexto.parametros.string("icono_desc");
		String prioridad = contexto.parametros.string("prioridad");

		BBTarjetaBeneficioBuhobank nuevaTarjetaBeneficio = new BBTarjetaBeneficioBuhobank();
		nuevaTarjetaBeneficio.id_tarjeta = idTarjeta;
		nuevaTarjetaBeneficio.desc_beneficio = descBeneficio;
		nuevaTarjetaBeneficio.desc_beneficio_html = descBeneficio_html;
		nuevaTarjetaBeneficio.icono_id = iconoId;
		nuevaTarjetaBeneficio.icono_desc = iconoDesc;
		nuevaTarjetaBeneficio.prioridad = prioridad;

		Boolean resPost = SqlBuhoBank.crearTarjetaBeneficioBuhobank(contexto, nuevaTarjetaBeneficio).tryGet();
		if (!resPost) {
			return respuesta("ERROR_POST");
		}

		return respuesta();
	}

	public static Object actualizarBeneficioTarjetaBuhobank(ContextoBB contexto) {
		Integer id = contexto.parametros.integer("id");
		Integer idTarjeta = contexto.parametros.integer("id_tarjeta");
		String descBeneficio = contexto.parametros.string("desc_beneficio");
		String descBeneficio_html = contexto.parametros.string("desc_beneficio_html");
		String iconoId = contexto.parametros.string("icono_id");
		String iconoDesc = contexto.parametros.string("icono_desc");
		String prioridad = contexto.parametros.string("prioridad");

		BBTarjetaBeneficioBuhobank tarjetaBeneficio = new BBTarjetaBeneficioBuhobank();
		tarjetaBeneficio.id = id;
		tarjetaBeneficio.id_tarjeta = idTarjeta;
		tarjetaBeneficio.desc_beneficio = descBeneficio;
		tarjetaBeneficio.desc_beneficio_html = descBeneficio_html;
		tarjetaBeneficio.icono_id = iconoId;
		tarjetaBeneficio.icono_desc = iconoDesc;
		tarjetaBeneficio.prioridad = prioridad;

		Boolean resPut = SqlBuhoBank.actualizarTarjetaBeneficioBuhobank(contexto, tarjetaBeneficio).tryGet();
		if (!resPut) {
			return respuesta("ERROR_PUT");
		}

		return respuesta();
	}

	public static Object crearContenidoDinamicoBuhobank(ContextoBB contexto) {
		Integer idPlantillaFlujo = contexto.parametros.integer("id_plantilla_flujo");
		String tipo = contexto.parametros.string("tipo");
		String imagen = contexto.parametros.string("imagen", null);
		String titulo = contexto.parametros.string("titulo", null);
		String texto = contexto.parametros.string("texto", null);
		String textoLegales = contexto.parametros.string("texto_legales", null);
		String textoTyc = contexto.parametros.string("texto_tyc", null);
		Boolean habilitado = contexto.parametros.bool("habilitado");

		BBContenidoDinamicoBuhobank nuevoContenido = new BBContenidoDinamicoBuhobank();
		nuevoContenido.id_plantilla_flujo = idPlantillaFlujo;
		nuevoContenido.tipo = tipo;
		nuevoContenido.imagen = imagen;
		nuevoContenido.titulo = titulo;
		nuevoContenido.texto = texto;
		nuevoContenido.texto_legales = textoLegales;
		nuevoContenido.texto_tyc = textoTyc;
		nuevoContenido.habilitado = habilitado;

		Boolean resPost = SqlBuhoBank.crearContenidoDinamicoBuhobank(contexto, nuevoContenido).tryGet();
		if (!resPost) {
			return respuesta("ERROR_POST");
		}

		return respuesta();
	}

	public static Object actualizarContenidoDinamicoBuhobank(ContextoBB contexto) {
		Integer id = contexto.parametros.integer("id");
		Integer idPlantillaFlujo = contexto.parametros.integer("id_plantilla_flujo");
		String tipo = contexto.parametros.string("tipo");
		String imagen = contexto.parametros.string("imagen", null);
		String titulo = contexto.parametros.string("titulo", null);
		String texto = contexto.parametros.string("texto", null);
		String textoLegales = contexto.parametros.string("texto_legales", null);
		String textoTyc = contexto.parametros.string("texto_tyc", null);
		Boolean habilitado = contexto.parametros.bool("habilitado");

		BBContenidoDinamicoBuhobank contenido = new BBContenidoDinamicoBuhobank();
		contenido.id = id;
		contenido.id_plantilla_flujo = idPlantillaFlujo;
		contenido.tipo = tipo;
		contenido.imagen = imagen;
		contenido.titulo = titulo;
		contenido.texto = texto;
		contenido.texto_legales = textoLegales;
		contenido.texto_tyc = textoTyc;
		contenido.habilitado = habilitado;

		Boolean resPut = SqlBuhoBank.actualizarContenidoDinamicoBuhobank(contexto, contenido).tryGet();
		if (!resPut) {
			return respuesta("ERROR_PUT");
		}

		return respuesta();
	}

	public static Object crearTarjetaFinalizarBuhobank(ContextoBB contexto) {
		Integer idTarjeta = contexto.parametros.integer("id_tarjeta");
		String tipo = contexto.parametros.string("tipo");
		String subtipo = contexto.parametros.string("subtipo");
		String titulo = contexto.parametros.string("titulo");
		String texto = contexto.parametros.string("texto");
		String logo = contexto.parametros.string("logo");

		BBTarjetaFinalizarBuhobank nuevaTarjetaFinalizar = new BBTarjetaFinalizarBuhobank();
		nuevaTarjetaFinalizar.id_tarjeta = idTarjeta;
		nuevaTarjetaFinalizar.tipo = tipo;
		nuevaTarjetaFinalizar.subtipo = subtipo;
		nuevaTarjetaFinalizar.titulo = titulo;
		nuevaTarjetaFinalizar.texto = texto;
		nuevaTarjetaFinalizar.logo = logo;

		Boolean resPost = SqlBuhoBank.crearTarjetaFinalizarBuhobank(contexto, nuevaTarjetaFinalizar).tryGet();
		if (!resPost) {
			return respuesta("ERROR_POST");
		}

		return respuesta();
	}

	public static Object actualizarFinalizarTarjetaBuhobank(ContextoBB contexto) {
		Integer id = contexto.parametros.integer("id");
		Integer idTarjeta = contexto.parametros.integer("id_tarjeta");
		String tipo = contexto.parametros.string("tipo");
		String subtipo = contexto.parametros.string("subtipo");
		String titulo = contexto.parametros.string("titulo");
		String texto = contexto.parametros.string("texto");
		String logo = contexto.parametros.string("logo");

		BBTarjetaFinalizarBuhobank tarjetaFinalizar = new BBTarjetaFinalizarBuhobank();
		tarjetaFinalizar.id = id;
		tarjetaFinalizar.id_tarjeta = idTarjeta;
		tarjetaFinalizar.tipo = tipo;
		tarjetaFinalizar.subtipo = subtipo;
		tarjetaFinalizar.titulo = titulo;
		tarjetaFinalizar.texto = texto;
		tarjetaFinalizar.logo = logo;

		Boolean resPut = SqlBuhoBank.actualizarTarjetaFinalizarBuhobank(contexto, tarjetaFinalizar).tryGet();
		if (!resPut) {
			return respuesta("ERROR_PUT");
		}

		return respuesta();
	}

	public static Object crearTarjetaFinalizarBatchBuhobank(ContextoBB contexto) {
		Integer idTarjeta = contexto.parametros.integer("id_tarjeta");
		String tipo = contexto.parametros.string("tipo");
		String subtipo = contexto.parametros.string("subtipo");
		String titulo = contexto.parametros.string("titulo");
		String texto = contexto.parametros.string("texto");
		String logo = contexto.parametros.string("logo");

		BBTarjetaFinalizarBatchBuhobank nuevaTarjetaFinalizar = new BBTarjetaFinalizarBatchBuhobank();
		nuevaTarjetaFinalizar.id_tarjeta = idTarjeta;
		nuevaTarjetaFinalizar.tipo = tipo;
		nuevaTarjetaFinalizar.subtipo = subtipo;
		nuevaTarjetaFinalizar.titulo = titulo;
		nuevaTarjetaFinalizar.texto = texto;
		nuevaTarjetaFinalizar.logo = logo;

		Boolean resPost = SqlBuhoBank.crearTarjetaFinalizarBatchBuhobank(contexto, nuevaTarjetaFinalizar).tryGet();
		if (!resPost) {
			return respuesta("ERROR_POST");
		}

		return respuesta();
	}

	public static Object actualizarFinalizarTarjetaBatchBuhobank(ContextoBB contexto) {
		Integer id = contexto.parametros.integer("id");
		Integer idTarjeta = contexto.parametros.integer("id_tarjeta");
		String tipo = contexto.parametros.string("tipo");
		String subtipo = contexto.parametros.string("subtipo");
		String titulo = contexto.parametros.string("titulo");
		String texto = contexto.parametros.string("texto");
		String logo = contexto.parametros.string("logo");

		BBTarjetaFinalizarBatchBuhobank tarjetaFinalizar = new BBTarjetaFinalizarBatchBuhobank();
		tarjetaFinalizar.id = id;
		tarjetaFinalizar.id_tarjeta = idTarjeta;
		tarjetaFinalizar.tipo = tipo;
		tarjetaFinalizar.subtipo = subtipo;
		tarjetaFinalizar.titulo = titulo;
		tarjetaFinalizar.texto = texto;
		tarjetaFinalizar.logo = logo;

		Boolean resPut = SqlBuhoBank.actualizarTarjetaFinalizarBatchBuhobank(contexto, tarjetaFinalizar).tryGet();
		if (!resPut) {
			return respuesta("ERROR_PUT");
		}

		return respuesta();
	}

	public static Object actualizarPaqueteSubproductoBuhobank(ContextoBB contexto) {
		Integer id = contexto.parametros.integer("id");
		Integer idPaquete = contexto.parametros.integer("id_paquete");
		String tipo = contexto.parametros.string("tipo");
		String codigo = contexto.parametros.string("codigo", null);
		String titulo = contexto.parametros.string("titulo");
		String descripcion = contexto.parametros.string("descripcion");
		String url_legales = contexto.parametros.string("url_legales");
		String imagen = contexto.parametros.string("imagen");
		Boolean habilitado = contexto.parametros.bool("habilitado", null);

		BBPaqueteSubproductoBuhobank paqueteSubproducto = new BBPaqueteSubproductoBuhobank();
		paqueteSubproducto.id = id;
		paqueteSubproducto.id_paquete = idPaquete;
		paqueteSubproducto.tipo = tipo;
		paqueteSubproducto.codigo = codigo;
		paqueteSubproducto.titulo = titulo;
		paqueteSubproducto.descripcion = descripcion;
		paqueteSubproducto.url_legales = url_legales;
		paqueteSubproducto.imagen = imagen;
		paqueteSubproducto.habilitado = habilitado;

		Boolean resPut = SqlBuhoBank.actualizarPaqueteSubproductoBuhobank(contexto, paqueteSubproducto).tryGet();
		if (!resPut) {
			return respuesta("ERROR_PUT");
		}

		return respuesta();
	}

	public static Object crearTarjetaLandingBuhobank(ContextoBB contexto) {
		Integer idPaquete = contexto.parametros.integer("id_paquete");
		String tipo = contexto.parametros.string("tipo");
		String nombreCorto = contexto.parametros.string("nombre_corto", null);
		String descripcion = contexto.parametros.string("descripcion");
		Boolean virtual = contexto.parametros.bool("virtual");
		String tarjeta = contexto.parametros.string("tarjeta");
		String titulo = contexto.parametros.string("titulo");
		String tituloLanding = contexto.parametros.string("titulo_landing", null);
		String box = contexto.parametros.string("box");
		String boton = contexto.parametros.string("boton");
		Integer prioridad = contexto.parametros.integer("prioridad");

		BBTarjetaLandingBuhobank nuevaTarjeta = new BBTarjetaLandingBuhobank();
		nuevaTarjeta.id_paquete = idPaquete;
		nuevaTarjeta.tipo = tipo;
		nuevaTarjeta.nombre_corto = nombreCorto;
		nuevaTarjeta.descripcion = descripcion;
		nuevaTarjeta.virtual = virtual;
		nuevaTarjeta.tarjeta = tarjeta;
		nuevaTarjeta.titulo = titulo;
		nuevaTarjeta.titulo_landing = tituloLanding;
		nuevaTarjeta.box = box;
		nuevaTarjeta.boton = boton;
		nuevaTarjeta.prioridad = prioridad;

		Boolean resPost = SqlBuhoBank.crearTarjetaLandingBuhobank(contexto, nuevaTarjeta).tryGet();
		if (!resPost) {
			return respuesta("ERROR_POST");
		}

		return respuesta();
	}

	public static Object actualizarTarjetaLandingBuhobank(ContextoBB contexto) {
		Integer id = contexto.parametros.integer("id");
		Integer idPaquete = contexto.parametros.integer("id_paquete");
		String tipo = contexto.parametros.string("tipo");
		String nombreCorto = contexto.parametros.string("nombre_corto", null);
		String descripcion = contexto.parametros.string("descripcion");
		Boolean virtual = contexto.parametros.bool("virtual");
		String tarjeta = contexto.parametros.string("tarjeta");
		String titulo = contexto.parametros.string("titulo");
		String tituloLanding = contexto.parametros.string("titulo_landing", null);
		String box = contexto.parametros.string("box");
		String boton = contexto.parametros.string("boton");
		Integer prioridad = contexto.parametros.integer("prioridad");

		BBTarjetaLandingBuhobank nuevaTarjeta = new BBTarjetaLandingBuhobank();
		nuevaTarjeta.id = id;
		nuevaTarjeta.id_paquete = idPaquete;
		nuevaTarjeta.tipo = tipo;
		nuevaTarjeta.nombre_corto = nombreCorto;
		nuevaTarjeta.descripcion = descripcion;
		nuevaTarjeta.virtual = virtual;
		nuevaTarjeta.tarjeta = tarjeta;
		nuevaTarjeta.titulo = titulo;
		nuevaTarjeta.titulo_landing = tituloLanding;
		nuevaTarjeta.box = box;
		nuevaTarjeta.boton = boton;
		nuevaTarjeta.prioridad = prioridad;

		Boolean resPut = SqlBuhoBank.actualizarTarjetaLandingBuhobank(contexto, nuevaTarjeta).tryGet();
		if (!resPut) {
			return respuesta("ERROR_PUT");
		}

		return respuesta();
	}

	public static Object crearTarjetaBeneficioLandingBuhobank(ContextoBB contexto) {
		Integer idTarjeta = contexto.parametros.integer("id_tarjeta");
		String descBeneficio = contexto.parametros.string("desc_beneficio");
		String descBeneficio_html = contexto.parametros.string("desc_beneficio_html");
		String iconoId = contexto.parametros.string("icono_id");
		String iconoDesc = contexto.parametros.string("icono_desc");
		String prioridad = contexto.parametros.string("prioridad");

		BBTarjetaBeneficioLandingBuhobank nuevaTarjetaBeneficio = new BBTarjetaBeneficioLandingBuhobank();
		nuevaTarjetaBeneficio.id_tarjeta = idTarjeta;
		nuevaTarjetaBeneficio.desc_beneficio = descBeneficio;
		nuevaTarjetaBeneficio.desc_beneficio_html = descBeneficio_html;
		nuevaTarjetaBeneficio.icono_id = iconoId;
		nuevaTarjetaBeneficio.icono_desc = iconoDesc;
		nuevaTarjetaBeneficio.prioridad = prioridad;

		Boolean resPost = SqlBuhoBank.crearTarjetaBeneficioLandingBuhobank(contexto, nuevaTarjetaBeneficio).tryGet();
		if (!resPost) {
			return respuesta("ERROR_POST");
		}

		return respuesta();
	}

	public static Object actualizarBeneficioTarjetaLandingBuhobank(ContextoBB contexto) {
		Integer id = contexto.parametros.integer("id");
		Integer idTarjeta = contexto.parametros.integer("id_tarjeta");
		String descBeneficio = contexto.parametros.string("desc_beneficio");
		String descBeneficio_html = contexto.parametros.string("desc_beneficio_html");
		String iconoId = contexto.parametros.string("icono_id");
		String iconoDesc = contexto.parametros.string("icono_desc");
		String prioridad = contexto.parametros.string("prioridad");

		BBTarjetaBeneficioLandingBuhobank tarjetaBeneficio = new BBTarjetaBeneficioLandingBuhobank();
		tarjetaBeneficio.id = id;
		tarjetaBeneficio.id_tarjeta = idTarjeta;
		tarjetaBeneficio.desc_beneficio = descBeneficio;
		tarjetaBeneficio.desc_beneficio_html = descBeneficio_html;
		tarjetaBeneficio.icono_id = iconoId;
		tarjetaBeneficio.icono_desc = iconoDesc;
		tarjetaBeneficio.prioridad = prioridad;

		Boolean resPut = SqlBuhoBank.actualizarTarjetaBeneficioLandingBuhobank(contexto, tarjetaBeneficio).tryGet();
		if (!resPut) {
			return respuesta("ERROR_PUT");
		}

		return respuesta();
	}

	public static Object crearContenidoDinamicoPaqueteBuhobank(ContextoBB contexto) {
		Integer idPaquete = contexto.parametros.integer("id_paquete");
		String tipo = contexto.parametros.string("tipo");
		String imagen = contexto.parametros.string("imagen");
		String titulo = contexto.parametros.string("titulo");
		String descripcion = contexto.parametros.string("descripcion");
		String texto = contexto.parametros.string("texto");
		String textoLegales = contexto.parametros.string("texto_legales");
		Boolean habilitado = contexto.parametros.bool("habilitado");

		BBContenidoDinamicoPaqueteBuhobank nuevoContenido = new BBContenidoDinamicoPaqueteBuhobank();
		nuevoContenido.id_paquete = idPaquete;
		nuevoContenido.tipo = tipo;
		nuevoContenido.imagen = imagen;
		nuevoContenido.titulo = titulo;
		nuevoContenido.descripcion = descripcion;
		nuevoContenido.texto = texto;
		nuevoContenido.texto_legales = textoLegales;
		nuevoContenido.habilitado = habilitado;

		Boolean resPost = SqlBuhoBank.crearContenidoDinamicoPaqueteBuhobank(contexto, nuevoContenido).tryGet();
		if (!resPost) {
			return respuesta("ERROR_POST");
		}

		return respuesta();
	}

	public static Object actualizarContenidoDinamicoPaqueteBuhobank(ContextoBB contexto) {
		Integer id = contexto.parametros.integer("id");
		Integer idPaquete = contexto.parametros.integer("id_paquete");
		String tipo = contexto.parametros.string("tipo");
		String imagen = contexto.parametros.string("imagen");
		String titulo = contexto.parametros.string("titulo");
		String descripcion = contexto.parametros.string("descripcion");
		String texto = contexto.parametros.string("texto");
		String textoLegales = contexto.parametros.string("texto_legales");
		Boolean habilitado = contexto.parametros.bool("habilitado");

		BBContenidoDinamicoPaqueteBuhobank contenido = new BBContenidoDinamicoPaqueteBuhobank();
		contenido.id = id;
		contenido.id_paquete = idPaquete;
		contenido.tipo = tipo;
		contenido.imagen = imagen;
		contenido.titulo = titulo;
		contenido.descripcion = descripcion;
		contenido.texto = texto;
		contenido.texto_legales = textoLegales;
		contenido.habilitado = habilitado;

		Boolean resPut = SqlBuhoBank.actualizarContenidoDinamicoPaqueteBuhobank(contexto, contenido).tryGet();
		if (!resPut) {
			return respuesta("ERROR_PUT");
		}

		return respuesta();
	}

	public static Object crearSucursalesBuhobank(ContextoBB contexto) {
		Integer postId = contexto.parametros.integer("post_id");
		String titulo = contexto.parametros.string("titulo");
		String postStatus = contexto.parametros.string("post_status");
		BigDecimal lat = contexto.parametros.bigDecimal("lat");
		BigDecimal lng = contexto.parametros.bigDecimal("lng");
		String direccion = contexto.parametros.string("direccion");
		String localidad = contexto.parametros.string("localidad");
		String provincia = contexto.parametros.string("provincia");

		BBSucursalBuhobank sucursal = new BBSucursalBuhobank();
		sucursal.post_id = postId;
		sucursal.titulo = titulo;
		sucursal.post_status = postStatus;
		sucursal.lat = lat;
		sucursal.lng = lng;
		sucursal.direccion = direccion;
		sucursal.localidad = localidad;
		sucursal.provincia = provincia;

		Boolean resPost = SqlBuhoBank.crearSucursalBuhobank(contexto, sucursal).tryGet();
		if (!resPost) {
			return respuesta("ERROR_POST");
		}

		return respuesta();
	}

	public static Object actualizarSucursalesBuhobank(ContextoBB contexto) {
		Integer id = contexto.parametros.integer("id");
		Integer postId = contexto.parametros.integer("post_id");
		String titulo = contexto.parametros.string("titulo");
		String postStatus = contexto.parametros.string("post_status");
		BigDecimal lat = contexto.parametros.bigDecimal("lat");
		BigDecimal lng = contexto.parametros.bigDecimal("lng");
		String direccion = contexto.parametros.string("direccion");
		String localidad = contexto.parametros.string("localidad");
		String provincia = contexto.parametros.string("provincia");

		BBSucursalBuhobank sucursal = new BBSucursalBuhobank();
		sucursal.id = id;
		sucursal.post_id = postId;
		sucursal.titulo = titulo;
		sucursal.post_status = postStatus;
		sucursal.lat = lat;
		sucursal.lng = lng;
		sucursal.direccion = direccion;
		sucursal.localidad = localidad;
		sucursal.provincia = provincia;

		Boolean resPut = SqlBuhoBank.actualizarSucursalBuhobank(contexto, sucursal).tryGet();
		if (!resPut) {
			return respuesta("ERROR_PUT");
		}

		return respuesta();
	}

	public static Object crearBeneficiosBuhobank(ContextoBB contexto) {
		Integer postId = contexto.parametros.integer("post_id");
		String titulo = contexto.parametros.string("titulo");
		String postStatus = contexto.parametros.string("post_status");
		String legales = contexto.parametros.string("legales");
		String tyc = contexto.parametros.string("tyc");

		BBBeneficioBuhobank beneficio = new BBBeneficioBuhobank();
		beneficio.post_id = postId;
		beneficio.titulo = titulo;
		beneficio.post_status = postStatus;
		beneficio.legales = legales;
		beneficio.tyc = tyc;

		Boolean resPost = SqlBuhoBank.crearBeneficiosBuhobank(contexto, beneficio).tryGet();
		if (!resPost) {
			return respuesta("ERROR_POST");
		}

		return respuesta();
	}

	public static Object actualizarBeneficiosBuhobank(ContextoBB contexto) {
		Integer id = contexto.parametros.integer("id");
		Integer postId = contexto.parametros.integer("post_id");
		String titulo = contexto.parametros.string("titulo");
		String postStatus = contexto.parametros.string("post_status");
		String legales = contexto.parametros.string("legales");
		String tyc = contexto.parametros.string("tyc");

		BBBeneficioBuhobank beneficio = new BBBeneficioBuhobank();
		beneficio.id = id;
		beneficio.post_id = postId;
		beneficio.titulo = titulo;
		beneficio.post_status = postStatus;
		beneficio.legales = legales;
		beneficio.tyc = tyc;

		Boolean resPut = SqlBuhoBank.actualizarBeneficiosBuhobank(contexto, beneficio).tryGet();
		if (!resPut) {
			return respuesta("ERROR_PUT");
		}

		return respuesta();
	}
}