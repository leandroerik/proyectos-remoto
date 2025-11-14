package ar.com.hipotecario.backend.servicio.sql;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBBeneficiosBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBBeneficiosBuhobank.BBBeneficioBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBContenidosDinamicoBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBContenidosDinamicoBuhobank.BBContenidoDinamicoBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBContenidosDinamicoPaqueteBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBContenidosDinamicoPaqueteBuhobank.BBContenidoDinamicoPaqueteBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPaquetesBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPaquetesBuhobank.BBPaqueteBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPaquetesSubproductoBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPaquetesSubproductoBuhobank.BBPaqueteSubproductoBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBParametriasBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBParametriasBuhobank.BBParametriaBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBParametriasGeneralBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBParametriasGeneralBuhobank.BBParametriaGeneralBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPlantillasDopplerBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPlantillasDopplerBuhobank.BBPlantillaDopplerBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPlantillasFlujoBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPlantillasFlujoBuhobank.BBPlantillaFlujoBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPromosGeoBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBSucursalesBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBSucursalesBuhobank.BBSucursalBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasBeneficioBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasBeneficioBuhobank.BBTarjetaBeneficioBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasBeneficioLandingBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasBeneficioLandingBuhobank.BBTarjetaBeneficioLandingBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasBuhobank.BBTarjetaBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasFinalizarBatchBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasFinalizarBatchBuhobank.BBTarjetaFinalizarBatchBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasFinalizarBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasFinalizarBuhobank.BBTarjetaFinalizarBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasLandingBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasLandingBuhobank.BBTarjetaLandingBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBVistasBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBVistasBuhobank.BBVistaBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.LogsBuhoBank;
import ar.com.hipotecario.canal.buhobank.ContextoBB;

public class SqlBuhoBank extends Sql {

	/* ========== SERVICIOS ========== */

	public static String SQL = "buhobank";

	public static Futuro<LogsBuhoBank> log(Contexto contexto) {
		return futuro(() -> LogsBuhoBank.get(contexto));
	}

	public static Futuro<LogsBuhoBank> log(Contexto contexto, Fecha fecha) {
		return futuro(() -> LogsBuhoBank.get(contexto, fecha));
	}

	public static Futuro<LogsBuhoBank> log(Contexto contexto, String cuit) {
		return futuro(() -> LogsBuhoBank.get(contexto, cuit));
	}

	public static Futuro<LogsBuhoBank> logFinalizados(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta, Boolean esBatch) {
		return futuro(() -> LogsBuhoBank.getFinalizadosByFecha(contexto, fechaDesde, fechaHasta, esBatch));
	}

	public static Futuro<LogsBuhoBank> logCasosVu(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta) {
		return futuro(() -> LogsBuhoBank.getCasosVuByFecha(contexto, fechaDesde, fechaHasta));
	}

	public static Futuro<LogsBuhoBank> logCasosError(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta) {
		return futuro(() -> LogsBuhoBank.getCasosErrorByFecha(contexto, fechaDesde, fechaHasta));
	}

	public static Futuro<LogsBuhoBank> logSesionesFinalizadas(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta, String flujo) {
		return futuro(() -> LogsBuhoBank.obtenerSesionesFinalizadas(contexto, fechaDesde, fechaHasta, flujo));
	}

	public static Futuro<LogsBuhoBank> getSesiones(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta) {
		return futuro(() -> LogsBuhoBank.obtenerSesiones(contexto, fechaDesde, fechaHasta));
	}

	public static Futuro<LogsBuhoBank> getSesionesEstado(Contexto contexto, String estado, Fecha fechaDesde, Fecha fechaHasta) {
		return futuro(() -> LogsBuhoBank.obtenerSesionesEstado(contexto, estado, fechaDesde, fechaHasta));
	}

	public static Futuro<LogsBuhoBank> getAuditoriaSP(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta, String evento) {
		return futuro(() -> LogsBuhoBank.obtenerAuditoriaSP(contexto, fechaDesde, fechaHasta, evento));
	}

	public static Futuro<LogsBuhoBank> getSolicitudesSD(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta) {
		return futuro(() -> LogsBuhoBank.getSolicitudesSD(contexto, fechaDesde, fechaHasta));
	}

	public static Futuro<LogsBuhoBank> getAnonimizarVU(Contexto contexto) {
		return futuro(() -> LogsBuhoBank.obtenerAnonimizarVU(contexto));
	}

	public static Futuro<LogsBuhoBank> getCantidadErrorAnonimizarVU(Contexto contexto) {
		return futuro(() -> LogsBuhoBank.obtenerCantidadErrorAnonimizarVU(contexto));
	}

	public static Futuro<LogsBuhoBank> getCantidadProcesadosAnonimizarVU(Contexto contexto) {
		return futuro(() -> LogsBuhoBank.obtenerCantidadProcesadosAnonimizarVU(contexto));
	}

	public static Futuro<LogsBuhoBank> getCantidadNoProcesadosAnonimizarVU(Contexto contexto) {
		return futuro(() -> LogsBuhoBank.obtenerCantidadNoProcesadosAnonimizarVU(contexto));
	}

	public static Futuro<LogsBuhoBank> obtenerRegistros(Contexto contexto, String cuit, Fecha fechadesde) {
		return futuro(() -> LogsBuhoBank.obtenerRegistros(contexto, cuit, fechadesde));
	}

	public static Futuro<Boolean> logBB(Contexto contexto, String cuit, String endpoint, String evento, String datos, String error, String idProceso) {
		return futuro(() -> LogsBuhoBank.post(contexto, cuit, endpoint, evento, datos, error, idProceso));
	}

	public static Futuro<LogsBuhoBank> captarAbandono(Contexto contexto, String cuil, String estado, Fecha fechaDesde) {
		return futuro(() -> LogsBuhoBank.getAbandonos(contexto, cuil, estado, fechaDesde));
	}

	public static Futuro<BBPlantillaFlujoBuhobank> obtenerPlantillaPorFlujo(Contexto contexto, String flujo) {
		return futuro(() -> BBPlantillasFlujoBuhobank.getFlujo(contexto, flujo));
	}

	public static Futuro<BBPlantillasFlujoBuhobank> obtenerPlantillasFlujoBuhobank(Contexto contexto) {
		return futuro(() -> BBPlantillasFlujoBuhobank.get(contexto));
	}

	public static Futuro<Boolean> crearPlantillasFlujoBuhobank(Contexto contexto, BBPlantillaFlujoBuhobank nuevaPlantillaFlujo) {
		return futuro(() -> BBPlantillasFlujoBuhobank.post(contexto, nuevaPlantillaFlujo));
	}

	public static Futuro<Boolean> actualizarPlantillasFlujoBuhobank(Contexto contexto, BBPlantillaFlujoBuhobank plantilla) {
		return futuro(() -> BBPlantillasFlujoBuhobank.put(contexto, plantilla));
	}

	public static Futuro<Boolean> borrarPlantillaFlujoBuhobank(Contexto contexto, Integer id) {
		return futuro(() -> BBPlantillasFlujoBuhobank.delete(contexto, id));
	}

	// bb_parametrias_general
	public static Futuro<BBParametriasGeneralBuhobank> obtenerParametriasGeneral(Contexto contexto) {
		return futuro(() -> BBParametriasGeneralBuhobank.get(contexto));
	}

	public static Futuro<Boolean> crearParametriasGeneralBuhobank(Contexto contexto, BBParametriaGeneralBuhobank nuevaParametria) {
		return futuro(() -> BBParametriasGeneralBuhobank.post(contexto, nuevaParametria));
	}

	public static Futuro<Boolean> actualizarParametriasGeneralBuhobank(Contexto contexto, BBParametriaGeneralBuhobank parametria) {
		return futuro(() -> BBParametriasGeneralBuhobank.put(contexto, parametria));
	}

	public static Futuro<Boolean> actualizarNombreParametriaGeneralBuhobank(Contexto contexto, String nombre, String valor) {
		return futuro(() -> BBParametriasGeneralBuhobank.putByNombre(contexto, nombre, valor));
	}

	public static Futuro<Boolean> borrarParametriasGeneralBuhobank(Contexto contexto, Integer id) {
		return futuro(() -> BBParametriasGeneralBuhobank.delete(contexto, id));
	}

	// bb_parametrias
	public static Futuro<BBParametriasBuhobank> obtenerParametrias(Contexto contexto, String flujo) {
		return futuro(() -> BBParametriasBuhobank.getByFlujo(contexto, flujo));
	}

	public static Futuro<BBParametriasBuhobank> obtenerParametriasBuhobank(Contexto contexto) {
		return futuro(() -> BBParametriasBuhobank.get(contexto));
	}

	public static Futuro<Boolean> crearParametriasBuhobank(Contexto contexto, BBParametriaBuhobank nuevaParametria) {
		return futuro(() -> BBParametriasBuhobank.post(contexto, nuevaParametria));
	}

	public static Futuro<Boolean> actualizarParametriasBuhobank(Contexto contexto, BBParametriaBuhobank parametria) {
		return futuro(() -> BBParametriasBuhobank.put(contexto, parametria));
	}

	public static Futuro<Boolean> borrarParametriasBuhobank(Contexto contexto, Integer id) {
		return futuro(() -> BBParametriasBuhobank.delete(contexto, id));
	}

	public static Futuro<Boolean> actualizarParametriasFullNombreBuhobank(Contexto contexto, String nombre, String valor) {
		return futuro(() -> BBParametriasBuhobank.putFullNombre(contexto, nombre, valor));
	}

	// bb_plantillas_doppler
	public static Futuro<BBPlantillasDopplerBuhobank> obtenerPlantillasDoppler(Contexto contexto, String flujo) {
		return futuro(() -> BBPlantillasDopplerBuhobank.getByFlujo(contexto, flujo));
	}

	public static Futuro<BBPlantillasDopplerBuhobank> obtenerPlantillasDopplerBuhobank(Contexto contexto) {
		return futuro(() -> BBPlantillasDopplerBuhobank.get(contexto));
	}

	public static Futuro<Boolean> crearPlantillaDopplerBuhobank(Contexto contexto, BBPlantillaDopplerBuhobank nuevaPlantillaDoppler) {
		return futuro(() -> BBPlantillasDopplerBuhobank.post(contexto, nuevaPlantillaDoppler));
	}

	public static Futuro<Boolean> actualizarPlantillaDopplerBuhobank(Contexto contexto, BBPlantillaDopplerBuhobank plantillaDoppler) {
		return futuro(() -> BBPlantillasDopplerBuhobank.put(contexto, plantillaDoppler));
	}

	public static Futuro<Boolean> borrarPlantillaDopplerBuhobank(Contexto contexto, Integer id) {
		return futuro(() -> BBPlantillasDopplerBuhobank.delete(contexto, id));
	}

	// bb_paquetes
	public static Futuro<BBPaqueteBuhobank> obtenerPaquetesLetra(Contexto contexto, String flujo, String letraTc, Boolean esEmprendedor) {
		return futuro(() -> BBPaquetesBuhobank.getByLetra(contexto, flujo, letraTc, esEmprendedor));
	}

	public static Futuro<BBPaquetesBuhobank> obtenerPaquetes(Contexto contexto, String flujo) {
		return futuro(() -> BBPaquetesBuhobank.getByFlujo(contexto, flujo));
	}

	public static Futuro<BBPaquetesBuhobank> obtenerPaquetesBuhobank(Contexto contexto) {
		return futuro(() -> BBPaquetesBuhobank.get(contexto));
	}

	public static Futuro<Boolean> crearPaquetesBuhobank(Contexto contexto, BBPaqueteBuhobank nuevoPaquete) {
		return futuro(() -> BBPaquetesBuhobank.post(contexto, nuevoPaquete));
	}

	public static Futuro<Boolean> actualizarPaquetesBuhobank(Contexto contexto, BBPaqueteBuhobank paquete) {
		return futuro(() -> BBPaquetesBuhobank.put(contexto, paquete));
	}

	public static Futuro<Boolean> borrarPaqueteBuhobank(Contexto contexto, Integer id) {
		return futuro(() -> BBPaquetesBuhobank.delete(contexto, id));
	}

	public static Futuro<BBPaqueteBuhobank> obtenerCuentaSueldo(Contexto contexto, Integer paqBase) {
		return futuro(() -> BBPaquetesBuhobank.getByPaqBase(contexto, paqBase));
	}

	public static Futuro<BBPaqueteBuhobank> buscarNumeroPaqueteBuhobank(Contexto contexto, Integer numeroPaquete) {
		return futuro(() -> BBPaquetesBuhobank.getByNumero(contexto, numeroPaquete));
	}
	
	public static Futuro<BBPaqueteBuhobank> buscarNumeroPaqueteBuhobank(Contexto contexto, Integer numeroPaquete, String flujo) {
		return futuro(() -> BBPaquetesBuhobank.getByNumero(contexto, numeroPaquete, flujo));
	}

	// bb_vistas
	public static Futuro<BBVistasBuhobank> obtenerVistasBuhobank(Contexto contexto) {
		return futuro(() -> BBVistasBuhobank.get(contexto));
	}

	public static Futuro<Boolean> crearVistaBuhobank(Contexto contexto, BBVistaBuhobank nuevaVista) {
		return futuro(() -> BBVistasBuhobank.post(contexto, nuevaVista));
	}

	public static Futuro<Boolean> actualizarVistaBuhobank(Contexto contexto, BBVistaBuhobank vista) {
		return futuro(() -> BBVistasBuhobank.put(contexto, vista));
	}

	public static Futuro<Boolean> borrarVistaBuhobank(Contexto contexto, Integer id) {
		return futuro(() -> BBVistasBuhobank.delete(contexto, id));
	}

	// bb_tarjetas
	public static Futuro<BBTarjetaBuhobank> obtenerTarjetaBuhobank(Contexto contexto, Integer idPaquete, Boolean esVirtual) {
		return futuro(() -> BBTarjetasBuhobank.getTarjeta(contexto, idPaquete, esVirtual));
	}

	public static Futuro<BBTarjetasBuhobank> obtenerTarjetasBuhobank(Contexto contexto) {
		return futuro(() -> BBTarjetasBuhobank.get(contexto));
	}

	public static Futuro<Boolean> crearTarjetaBuhobank(Contexto contexto, BBTarjetaBuhobank nuevaTarjeta) {
		return futuro(() -> BBTarjetasBuhobank.post(contexto, nuevaTarjeta));
	}

	public static Futuro<Boolean> actualizarTarjetaBuhobank(Contexto contexto, BBTarjetaBuhobank tarjeta) {
		return futuro(() -> BBTarjetasBuhobank.put(contexto, tarjeta));
	}

	public static Futuro<Boolean> borrarTarjetaBuhobank(Contexto contexto, Integer id) {
		return futuro(() -> BBTarjetasBuhobank.delete(contexto, id));
	}

	// bb_tarjetas_beneficio
	public static Futuro<BBTarjetasBeneficioBuhobank> obtenerTarjetasBeneficioBuhobank(Contexto contexto) {
		return futuro(() -> BBTarjetasBeneficioBuhobank.get(contexto));
	}

	public static Futuro<Boolean> crearTarjetaBeneficioBuhobank(Contexto contexto, BBTarjetaBeneficioBuhobank nuevaTarjetaBeneficio) {
		return futuro(() -> BBTarjetasBeneficioBuhobank.post(contexto, nuevaTarjetaBeneficio));
	}

	public static Futuro<Boolean> actualizarTarjetaBeneficioBuhobank(Contexto contexto, BBTarjetaBeneficioBuhobank tarjetaBeneficio) {
		return futuro(() -> BBTarjetasBeneficioBuhobank.put(contexto, tarjetaBeneficio));
	}

	public static Futuro<Boolean> borrarTarjetaBeneficioBuhobank(Contexto contexto, Integer id) {
		return futuro(() -> BBTarjetasBeneficioBuhobank.delete(contexto, id));
	}

	// bb_contenidos_dinamico
	public static Futuro<BBContenidosDinamicoBuhobank> obtenerContenidosDinamicoByTipo(Contexto contexto, String flujo, String tipo) {
		return futuro(() -> BBContenidosDinamicoBuhobank.getByTipo(contexto, flujo, tipo));
	}

	public static Futuro<BBContenidosDinamicoBuhobank> obtenerContenidosDinamicoBuhobank(Contexto contexto) {
		return futuro(() -> BBContenidosDinamicoBuhobank.get(contexto));
	}

	public static Futuro<Boolean> crearContenidoDinamicoBuhobank(Contexto contexto, BBContenidoDinamicoBuhobank nuevoContenido) {
		return futuro(() -> BBContenidosDinamicoBuhobank.post(contexto, nuevoContenido));
	}

	public static Futuro<Boolean> actualizarContenidoDinamicoBuhobank(Contexto contexto, BBContenidoDinamicoBuhobank contenido) {
		return futuro(() -> BBContenidosDinamicoBuhobank.put(contexto, contenido));
	}

	public static Futuro<Boolean> borrarContenidoDinamicoBuhobank(Contexto contexto, Integer id) {
		return futuro(() -> BBContenidosDinamicoBuhobank.delete(contexto, id));
	}

	// bb_tarjetas_finalizar
	public static Futuro<BBTarjetasFinalizarBuhobank> obtenerTarjetasFinalizarBuhobank(Contexto contexto) {
		return futuro(() -> BBTarjetasFinalizarBuhobank.get(contexto));
	}

	public static Futuro<Boolean> crearTarjetaFinalizarBuhobank(Contexto contexto, BBTarjetaFinalizarBuhobank nuevaTarjetaFinalizar) {
		return futuro(() -> BBTarjetasFinalizarBuhobank.post(contexto, nuevaTarjetaFinalizar));
	}

	public static Futuro<Boolean> actualizarTarjetaFinalizarBuhobank(Contexto contexto, BBTarjetaFinalizarBuhobank tarjetaFinalizar) {
		return futuro(() -> BBTarjetasFinalizarBuhobank.put(contexto, tarjetaFinalizar));
	}

	public static Futuro<Boolean> borrarTarjetaFinalizarBuhobank(Contexto contexto, Integer id) {
		return futuro(() -> BBTarjetasFinalizarBuhobank.delete(contexto, id));
	}

	// bb_tarjetas_finalizar_batch
	public static Futuro<BBTarjetasFinalizarBatchBuhobank> obtenerTarjetasFinalizarBatchBuhobank(Contexto contexto) {
		return futuro(() -> BBTarjetasFinalizarBatchBuhobank.get(contexto));
	}

	public static Futuro<Boolean> crearTarjetaFinalizarBatchBuhobank(Contexto contexto, BBTarjetaFinalizarBatchBuhobank nuevaTarjetaFinalizar) {
		return futuro(() -> BBTarjetasFinalizarBatchBuhobank.post(contexto, nuevaTarjetaFinalizar));
	}

	public static Futuro<Boolean> actualizarTarjetaFinalizarBatchBuhobank(Contexto contexto, BBTarjetaFinalizarBatchBuhobank tarjetaFinalizar) {
		return futuro(() -> BBTarjetasFinalizarBatchBuhobank.put(contexto, tarjetaFinalizar));
	}

	public static Futuro<Boolean> borrarTarjetaFinalizarBatchBuhobank(Contexto contexto, Integer id) {
		return futuro(() -> BBTarjetasFinalizarBatchBuhobank.delete(contexto, id));
	}

	// bb_paquetes_subproducto
	public static Futuro<BBPaquetesSubproductoBuhobank> obtenerSubproductosByPaquete(Contexto contexto, Integer idPaquete) {
		return futuro(() -> BBPaquetesSubproductoBuhobank.getByPaquete(contexto, idPaquete));
	}

	public static Futuro<BBPaquetesSubproductoBuhobank> obtenerPaquetesSubproductoBuhobank(Contexto contexto) {
		return futuro(() -> BBPaquetesSubproductoBuhobank.get(contexto));
	}

	public static Futuro<Boolean> actualizarPaqueteSubproductoBuhobank(Contexto contexto, BBPaqueteSubproductoBuhobank paqueteSubproducto) {
		return futuro(() -> BBPaquetesSubproductoBuhobank.put(contexto, paqueteSubproducto));
	}

	public static Futuro<Boolean> borrarPaqueteSubproductoBuhobank(Contexto contexto, Integer id) {
		return futuro(() -> BBPaquetesSubproductoBuhobank.delete(contexto, id));
	}

	// bb_tarjetas_landing
	public static Futuro<BBTarjetaLandingBuhobank> obtenerTarjetaLandingBuhobank(Contexto contexto, String tipo, Integer idPaquete, Boolean esVirtual) {
		return futuro(() -> BBTarjetasLandingBuhobank.getTarjeta(contexto, tipo, idPaquete, esVirtual));
	}

	public static Futuro<BBTarjetasLandingBuhobank> obtenerTarjetasLandingBuhobank(Contexto contexto) {
		return futuro(() -> BBTarjetasLandingBuhobank.get(contexto));
	}

	public static Futuro<Boolean> crearTarjetaLandingBuhobank(Contexto contexto, BBTarjetaLandingBuhobank nuevaTarjeta) {
		return futuro(() -> BBTarjetasLandingBuhobank.post(contexto, nuevaTarjeta));
	}

	public static Futuro<Boolean> actualizarTarjetaLandingBuhobank(Contexto contexto, BBTarjetaLandingBuhobank tarjeta) {
		return futuro(() -> BBTarjetasLandingBuhobank.put(contexto, tarjeta));
	}

	public static Futuro<Boolean> borrarTarjetaLandingBuhobank(Contexto contexto, Integer id) {
		return futuro(() -> BBTarjetasLandingBuhobank.delete(contexto, id));
	}

	// bb_tarjetas_beneficio_landing
	public static Futuro<BBTarjetasBeneficioLandingBuhobank> obtenerTarjetasBeneficioLandingBuhobank(Contexto contexto) {
		return futuro(() -> BBTarjetasBeneficioLandingBuhobank.get(contexto));
	}

	public static Futuro<Boolean> crearTarjetaBeneficioLandingBuhobank(Contexto contexto, BBTarjetaBeneficioLandingBuhobank nuevaTarjetaBeneficio) {
		return futuro(() -> BBTarjetasBeneficioLandingBuhobank.post(contexto, nuevaTarjetaBeneficio));
	}

	public static Futuro<Boolean> actualizarTarjetaBeneficioLandingBuhobank(Contexto contexto, BBTarjetaBeneficioLandingBuhobank tarjetaBeneficio) {
		return futuro(() -> BBTarjetasBeneficioLandingBuhobank.put(contexto, tarjetaBeneficio));
	}

	public static Futuro<Boolean> borrarTarjetaBeneficioLandingBuhobank(Contexto contexto, Integer id) {
		return futuro(() -> BBTarjetasBeneficioLandingBuhobank.delete(contexto, id));
	}

	// bb_contenidos_dinamico_paquete
	public static Futuro<BBContenidosDinamicoPaqueteBuhobank> obtenerContenidosDinamicoPaquetesBuhobank(Contexto contexto) {
		return futuro(() -> BBContenidosDinamicoPaqueteBuhobank.get(contexto));
	}

	public static Futuro<Boolean> crearContenidoDinamicoPaqueteBuhobank(Contexto contexto, BBContenidoDinamicoPaqueteBuhobank nuevoContenido) {
		return futuro(() -> BBContenidosDinamicoPaqueteBuhobank.post(contexto, nuevoContenido));
	}

	public static Futuro<Boolean> actualizarContenidoDinamicoPaqueteBuhobank(Contexto contexto, BBContenidoDinamicoPaqueteBuhobank contenido) {
		return futuro(() -> BBContenidosDinamicoPaqueteBuhobank.put(contexto, contenido));
	}

	public static Futuro<Boolean> borrarContenidoDinamicoPaqueteBuhobank(Contexto contexto, Integer id) {
		return futuro(() -> BBContenidosDinamicoPaqueteBuhobank.delete(contexto, id));
	}

	public static Futuro<BBContenidosDinamicoPaqueteBuhobank> obtenerContenidoDinamicoPaquete(Contexto contexto, String idPaquete, String tipo) {
		return futuro(() -> BBContenidosDinamicoPaqueteBuhobank.getByTipo(contexto, idPaquete, tipo));
	}

	// historico push
	public static Futuro<Boolean> ejecutarHistoricoPush(Contexto contexto, Fecha fechaDesde) {
		return futuro(() -> LogsBuhoBank.ejecutarHistorico(contexto, fechaDesde));
	}

	// beneficios por geo
	public static Futuro<BBPromosGeoBuhobank> obtenerPromosPorGeo(Contexto contexto, String latitud, String longitud, String radio) {
		return futuro(() -> BBPromosGeoBuhobank.get(contexto, latitud, longitud, radio));
	}

	// buhobank sucursales
	public static Futuro<BBSucursalesBuhobank> obtenerSucursalesBuhobank(Contexto contexto) {
		return futuro(() -> BBSucursalesBuhobank.get(contexto));
	}

	public static Futuro<Boolean> crearSucursalBuhobank(Contexto contexto, BBSucursalBuhobank sucursal) {
		return futuro(() -> BBSucursalesBuhobank.post(contexto, sucursal));
	}

	public static Futuro<Boolean> actualizarSucursalBuhobank(Contexto contexto, BBSucursalBuhobank sucursal) {
		return futuro(() -> BBSucursalesBuhobank.put(contexto, sucursal));
	}

	public static Futuro<Boolean> borrarSucursalBuhobank(Contexto contexto, Integer id) {
		return futuro(() -> BBSucursalesBuhobank.delete(contexto, id));
	}

	// buhobank beneficios
	public static Futuro<BBBeneficiosBuhobank> obtenerBeneficiosBuhobank(Contexto contexto) {
		return futuro(() -> BBBeneficiosBuhobank.get(contexto));
	}

	public static Futuro<Boolean> crearBeneficiosBuhobank(Contexto contexto, BBBeneficioBuhobank beneficio) {
		return futuro(() -> BBBeneficiosBuhobank.post(contexto, beneficio));
	}

	public static Futuro<Boolean> actualizarBeneficiosBuhobank(Contexto contexto, BBBeneficioBuhobank beneficio) {
		return futuro(() -> BBBeneficiosBuhobank.put(contexto, beneficio));
	}

	public static Futuro<Boolean> borrarBeneficioBuhobank(Contexto contexto, Integer id) {
		return futuro(() -> BBBeneficiosBuhobank.delete(contexto, id));
	}

	// borrado de sesiones no finalizadas, solo se conservan los ultimos 30 dias
	public static Futuro<Object> ejecutarBorradoDeSesiones(ContextoBB contexto, Fecha fechaCorte) {
		return futuro(() -> LogsBuhoBank.ejecutarBorradoDeSesiones(contexto, fechaCorte));
	}

	public static Futuro<Object> obtenerClientesAnonimizarVu(ContextoBB contexto) {
		return futuro(() -> LogsBuhoBank.obtenerClientesAnonimizarVu(contexto));
	}

	public static Futuro<Object> actualizarClientesAnonimizarVu(ContextoBB contexto, Integer id, Boolean borrado, String respuestaVu) {
		return futuro(() -> LogsBuhoBank.actualizarClientesAnonimizarVu(contexto, id, borrado, respuestaVu));
	}

	public static Futuro<Boolean> armarLock(ContextoBB contexto, String proceso) {
		return futuro(() -> LogsBuhoBank.armarLock(contexto, proceso));
	}

	public static Futuro<Boolean> desarmarLock(ContextoBB contexto, String proceso) {
		return futuro(() -> LogsBuhoBank.desarmarLock(contexto, proceso));
	}

	public static Futuro<Boolean> logCron(ContextoBB contexto, String evento, String paso, String detalle) {
		return futuro(() -> LogsBuhoBank.logCron(contexto, evento, paso, detalle));
	}
}
