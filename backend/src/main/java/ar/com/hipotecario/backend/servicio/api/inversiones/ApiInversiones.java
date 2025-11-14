package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentasBB.CuentaBB;
import ar.com.hipotecario.backend.servicio.api.personas.Domicilios.Domicilio;
import ar.com.hipotecario.backend.servicio.api.personas.Persona;
import ar.com.hipotecario.backend.servicio.api.personas.Telefonos.Telefono;
import ar.com.hipotecario.canal.buhobank.SesionBB;

// http://api-inversiones-microservicios-homo.appd.bh.com.ar/swagger-ui.html
public class ApiInversiones extends Api {
	
	/* ========== CONSTANTES ========== */
	public static String API = "inversiones";

	/* ========== Auditoria Preguntas Perfil Inversor Controller ========== */

	// GET /v1/deudas/{cliente}/unitrade
	public static Futuro<DeudasUnitrade> deudasUnitrade(Contexto contexto, String cliente, Boolean moneda, Boolean numProducto, Boolean tipoProducto) {
		return futuro(() -> DeudasUnitrade.get(contexto, cliente, moneda, numProducto, tipoProducto));
	}

	// TODO: POST /v1/perfiles

	/* ========== Cotizaciones BYMA Controller ========== */

	// GET /v1/caucionesdelay
	public static Futuro<Cauciones> cauciones(Contexto contexto, Integer nroMsj) {
		return futuro(() -> Cauciones.get(contexto, nroMsj));
	}

	// GET /v1/cotizacioneshistoricas
	public static Futuro<CotizacionesHistoricas> cotizacionesHistoricas(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta, String idEspecie, String idIndice, String indice, String simbolo, String idVencimiento) {
		return futuro(() -> CotizacionesHistoricas.get(contexto, fechaDesde, fechaHasta, idEspecie, idIndice, indice, simbolo, idVencimiento));
	}

	// GET /v1/cotizacionesresumen
	public static Futuro<CotizacionesResumen> cotizacionesResumen(Contexto contexto, String idEspecie, String idIndice, String idPanel, String idVencimiento) {
		return futuro(() -> CotizacionesResumen.get(contexto, idEspecie, idIndice, idPanel, idVencimiento));
	}

	// GET /v1/especies
	public static Futuro<Especies> especies(Contexto contexto, String idPanel) {
		return futuro(() -> Especies.get(contexto, idPanel));
	}

	// GET /v1/indicesbursatiles
	public static Futuro<IndicesBursatiles> indicesBursatiles(Contexto contexto, Boolean sectorial) {
		return futuro(() -> IndicesBursatiles.get(contexto, sectorial));
	}

	// GET /v1/indicesbursatilesdelay
	public static Futuro<IndicesBursatilesDelay> indicesBursatilesDelay(Contexto contexto, Boolean cierreAnterior) {
		return futuro(() -> IndicesBursatilesDelay.get(contexto, cierreAnterior));
	}

	// GET /v1/indicessectorialesdelay
	public static Futuro<IndicesSectorialesDelay> indicesSectorialesDelay(Contexto contexto) {
		return futuro(() -> IndicesSectorialesDelay.get(contexto));
	}

	// GET /v1/intradiarias
	public static Futuro<IntraDiarias> intraDiarias(Contexto contexto, Integer idPanel, String codigo, String idVencimiento) {
		return futuro(() -> IntraDiarias.get(contexto, idPanel, codigo, idVencimiento));
	}

	// GET /v1/intradiariasoferta
	public static Futuro<IntraDiariasOferta> intraDiariasOferta(Contexto contexto, Integer idPanel, String codigo, String idVencimiento, String idIntradiaria) {
		return futuro(() -> IntraDiariasOferta.get(contexto, idPanel, codigo, idVencimiento, idIntradiaria));
	}

	// GET /v1/panelcotizacionesdelay
	public static Futuro<PanelCotizacionesDelay> panelCotizacionesDelay(Contexto contexto, String idPanel) {
		return futuro(() -> PanelCotizacionesDelay.get(contexto, idPanel));
	}

	// GET /v1/panelesespecies
	public static Futuro<PanelesEspecies> panelesEspecies(Contexto contexto) {
		return futuro(() -> PanelesEspecies.get(contexto));
	}

	// GET /v1/panelfuturosdelay
	public static Futuro<PanelFuturosDelay> panelFuturosDelay(Contexto contexto, Integer nroMsg) {
		return futuro(() -> PanelFuturosDelay.get(contexto, nroMsg));
	}

	// GET /v1/vencimientosespecies
	public static Futuro<VencimientosEspecies> vencimientosEspecies(Contexto contexto) {
		return futuro(() -> VencimientosEspecies.get(contexto));
	}

	/* ========== Cuenta Rest Controller ========== */

	// GET /v1/cuentas/deudas
	public static Futuro<CuentasDeudas> cuentasDeudas(Contexto contexto, String idEmpresas, String tipoConsulta, String secuencial, Boolean paginar) {
		return futuro(() -> CuentasDeudas.get(contexto, idEmpresas, tipoConsulta, secuencial, paginar));
	}

	/* ========== Extracto Comitente Controller ========== */

	// GET /v1/{cuenta}/extractoComitente
	public static Futuro<ExtractoComitente> extractoComitente(Contexto contexto, String cuenta, Fecha fechaInicio, Fecha fechaFin, String idCobis, String numeroSecuencial, String cantidadRegistro) {
		return futuro(() -> ExtractoComitente.get(contexto, cuenta, fechaInicio, fechaFin, idCobis, numeroSecuencial, cantidadRegistro));
	}

	/* ========== BCRA Controller ========== */

	// TODO: POST /v1/bcra/importacion

	// GET /v1/bcra/verificacion/{cuil}
	public static Futuro<BcraVerificacion> bcraVerificacion(Contexto contexto, String cuil) {
		return futuro(() -> BcraVerificacion.get(contexto, cuil));
	}

	/* ========== Cotizaciones Controller ========== */

	// TODO: GET /v1/cotizaciones/{id}

	// TODO: GET /v1/cotizacionesmoneda

	// GET /v1/cuentas/{cuentacomitente}/comitente
	public static Futuro<CuentaComitentePorCuenta> cuentaComitentePorCuenta(Contexto contexto, String cuentacomitente) {
		return futuro(() -> CuentaComitentePorCuenta.get(contexto, cuentacomitente));
	}

    public static Futuro<ExtractoComitenteOB> extractoComitenteOB(Contexto contexto, String cuenta, Fecha fechaInicio, Fecha fechaFin, String idCobis, String numeroSecuencial, String cantidadRegistro,Integer agrupado) {
        return futuro(() -> ExtractoComitenteOB.get(contexto, cuenta, fechaInicio, fechaFin, idCobis, numeroSecuencial, cantidadRegistro, agrupado));
    }


	// TODO: GET /v1/cuentascomitentes
	// GET /v1/cuentascomitentes/{cuentacomitente}/especiesOB
	
	public static Futuro<CuentaComitenteEspeciesOB> cuentaComitenteEspecieOB(Contexto contexto, String idCliente, String cuentaComitente) {
		return futuro(() -> CuentaComitenteEspeciesOB.get(contexto, idCliente, cuentaComitente));
	}

	// GET /v1/cuentascomitentes/{cuentacomitente}/especies
	public static Futuro<CuentaComitenteEspecies> cuentaComitenteEspecie(Contexto contexto, String idCliente, String cuentaComitente) {
		return futuro(() -> CuentaComitenteEspecies.get(contexto, idCliente, cuentaComitente));
	}

	// GET /v1/cuentascomitentes/{cuentacomitente}/posturas
	public static Futuro<CuentaComitentePosturas> cuentaComitentePosturas(Contexto contexto, String cuentaComitente, Fecha fechaDesde, Fecha fechaHasta) {
		return futuro(() -> CuentaComitentePosturas.get(contexto, cuentaComitente, fechaDesde, fechaHasta));
	}

	// GET /v1/cuentascomitentes/{id}/licitaciones
	public static Futuro<CuentaComitentesLicitaciones> cuentaComitentesLicitaciones(Contexto contexto, String id, Fecha fecha, String cantregistros, String secuencial) {
		return futuro(() -> CuentaComitentesLicitaciones.get(contexto, id, fecha, cantregistros, secuencial));
	}

	// TODO: GET /v1/cuentascomitentes/{id}/licitaciones/{cuentaComitente}

	public static Futuro<CuentaComitentesLicitacionesOB> cuentaComitentesLicitacionesOB(Contexto contexto, String id, String cuentaComitente, Fecha fecha, String cantregistros, String secuencial) {
		return futuro(() -> CuentaComitentesLicitacionesOB.get(contexto, id, cuentaComitente, fecha, cantregistros, secuencial));
	}
	public static Futuro<CuentaComitentesLicitacionesOB> cuentaComitentesLicitacionesV2OB(Contexto contexto, String id, String cuentaComitente, Fecha fecha, String cantregistros, String secuencial) {
		return futuro(() -> CuentaComitentesLicitacionesOB.getV2(contexto, id, cuentaComitente, fecha, cantregistros, secuencial));
	}

	// GET /v1/cuentascomitentes/cliente/{id}
	public static Futuro<CuentaComitenteCliente> cuentaComitenteCliente(Contexto contexto, String id) {
		return futuro(() -> CuentaComitenteCliente.get(contexto, id));
	}

	// TODO: POST /v1/cuentascomitentes/licitaciones

	// TODO: GET /v1/licitaciones

	// GET /v2/cotizaciones
	public static Futuro<Cotizaciones> cotizaciones(Contexto contexto, String id, String mercado) {
		return futuro(() -> Cotizaciones.get(contexto, id, mercado));
	}

	public static Futuro<CotizacionesOb> cotizacionesOb(Contexto contexto, String id, String mercado) {
		return futuro(() -> CotizacionesOb.get(contexto, id, mercado));
	}

	// GET /v2/cotizaciones/{idmoneda}
	public static Futuro<CotizacionesMoneda> cotizacionesMoneda(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta, String moneda) {
		return futuro(() -> CotizacionesMoneda.get(contexto, fechaDesde, fechaHasta, moneda));
	}

	// GET /v2/cuentascomitentes
	public static Futuro<CuentasComitentes> cuentasComitentes(Contexto contexto, String idCliente) {
		return futuro(() -> CuentasComitentes.get(contexto, idCliente));
	}
	
	// GET /v2/cuentascomitentesActivas
	public static Futuro<CuentasComitentes> cuentasComitentesActivas(Contexto contexto, String idCliente, String tipoEstado) {
		return futuro(() -> CuentasComitentes.get(contexto, idCliente, tipoEstado));
	}

	// GET /v2/licitaciones
	public static Futuro<Licitaciones> licitaciones(Contexto contexto) {
		return futuro(() -> Licitaciones.get(contexto));
	}

	/* ========== Cuenta Liquidacion Controller ========== */

	// GET /v1/cuentaLiquidacion
	public static Futuro<CuentaLiquidaciones> cuentaLiquidaciones(Contexto contexto, String idcobis, String cuentacomitente, String cantidadregistrospaginacion, Integer desdesecuencialpaginacion) {
		return futuro(() -> CuentaLiquidaciones.get(contexto, idcobis, cuentacomitente, cantidadregistrospaginacion, desdesecuencialpaginacion));
	}

	/* ========== Tenencias FCI Controller ========== */

	// GET /v1/tenencias/cuotapartista
	public static Futuro<TenenciasCuotaPartista> tenenciasCuotaPartista(Contexto contexto, String cuilCuitCuotaPartista) {
		return futuro(() -> TenenciasCuotaPartista.get(contexto, cuilCuitCuotaPartista));
	}

	// GET /v1/tenencias/plazos
	public static Futuro<TenenciasPlazos> tenenciasPlazos(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta, String nombreFondo) {
		return futuro(() -> TenenciasPlazos.get(contexto, fechaDesde, fechaHasta, nombreFondo));
	}

	// GET /v1/tenencias/rendi
	public static Futuro<TenenciasRendi> tenenciasRendi(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta, String nombreFondo, String capital) {
		return futuro(() -> TenenciasRendi.get(contexto, fechaDesde, fechaHasta, nombreFondo, capital));
	}

	// GET /v1/tenencias/resumendetalle
	public static Futuro<TenenciasResumenDetalle> tenenciasResumenDetalle(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta, String numCuotaPartista) {
		return futuro(() -> TenenciasResumenDetalle.get(contexto, fechaDesde, fechaHasta, numCuotaPartista));
	}


	/* ========== Titulos Controller ========== */

	// GET /v1/titulos/precio
	public static Futuro<TitulosPrecio> titulosPrecio(Contexto contexto, Fecha fecha, String descmoneda, String tipoProducto) {
		return futuro(() -> TitulosPrecio.get(contexto, fecha, descmoneda, tipoProducto));
	}

	public static Futuro<TitulosPrecio> titulosPrecioByIdcobis(Contexto contexto, Fecha fecha, String descmoneda, String tipoProducto, String idCobis) {
		return futuro(() -> TitulosPrecio.get(contexto, fecha, descmoneda, tipoProducto, idCobis));
	}

	// GET /v1/titulos/producto
	public static Futuro<TitulosProducto> titulosProducto(Contexto contexto, String secuencial, String cantRegistros, Fecha fecha) {
		return futuro(() -> TitulosProducto.get(contexto, secuencial, cantRegistros, fecha));
	}

	/* ========== ESCO Controller ========== */

	// POST /v1/cuenta/resumen
	public static Futuro<CuentaCuotapartistaResumen> resumenCuentaCuotapartista(Contexto contexto, CuentaCuotapartistaResumenRequest request) {
		return futuro(() -> CuentaCuotapartistaResumen.post(contexto, request));
	}

	// POST /v1/cuotapartista
	public static Futuro<Cuotapartistas> cuotapartista(Contexto contexto, String idTpDocIdentidad, String numDocIdentidad, String email, String idUsuario, Boolean mostrarCuentasAnuladas, String nombre) {
		return futuro(() -> Cuotapartistas.post(contexto, idTpDocIdentidad, numDocIdentidad, email, idUsuario, mostrarCuentasAnuladas, nombre));
	}

	// POST /v1/fondos
	public static Futuro<FondosDeInversion> fondosDeInversion(Contexto contexto, Integer idCuotapartista, String tipoSolicitud) {
		return futuro(() -> FondosDeInversion.post(contexto, idCuotapartista, tipoSolicitud));
	}

	// TODO: POST /v1/formulario

	// TODO: POST /v1/liquidaciones

	// POST /v1/posicionCuotapartista
	public static Futuro<PosicionesCuotapartista> posicionCuotapartista(Contexto contexto, String fecha, String idCuotapartista, Integer numeroCuotapartista, String nombre) {
		return futuro(() -> PosicionesCuotapartista.post(contexto, fecha, idCuotapartista, numeroCuotapartista, nombre));
	}

	// POST /v1/rescateSL
	public static Futuro<RescateSL> rescate(Contexto contexto, Integer cantCuotapartes, String ctaBancaria, String cuotapartista, Boolean esTotal, String moneda, BigDecimal importe, String fondo, String condicionIngresoEgreso, String tipoValorCuotaParte) {
		return futuro(() -> RescateSL.post(contexto, cantCuotapartes, ctaBancaria, cuotapartista, esTotal, moneda, importe, fondo, condicionIngresoEgreso, tipoValorCuotaParte));
	}

	// POST /v1/solicitudes
	public static Futuro<Solicitudes> getSolicitudes(Contexto contexto, String fechaDesde, String fechaHasta, String idAgColocador, String idCuotapartista, String idFondo, String idTpValorCp, String idUsuario, Integer numeroCuotapartista, String nombre) {
		return futuro(() -> Solicitudes.obtenerSolicitudes(contexto, fechaDesde, fechaHasta, idAgColocador, idCuotapartista, idFondo, idTpValorCp, idUsuario, numeroCuotapartista, nombre));
	}
	
	public static Futuro<Liquidaciones> getLiquidaciones(Contexto contexto, String fechaDesde, String fechaHasta, Integer numeroCuotapartista, String nombre) {
		return futuro(() -> Liquidaciones.obtenerLiquidaciones(contexto, fechaDesde, fechaHasta, numeroCuotapartista, nombre));
	}


	// POST /v1/suscripcionSL
	public static Futuro<SuscripcionSL> suscripcionSL(Contexto contexto, String idCuentaBancaria, String moneda, String numeroCuenta, String cuotapartista, String importe, String fondo, String condicionIngresoEgreso, String tipoValorCuotaParte) {
		return futuro(() -> SuscripcionSL.post(contexto, idCuentaBancaria, moneda, numeroCuenta, cuotapartista, importe, fondo, condicionIngresoEgreso, tipoValorCuotaParte));
	}

	/* ========== Renta Financiera Controller ========== */

	// GET /v1/rentafinanciera
	public static Futuro<RentaFinanciera> rentaFinanciera(Contexto contexto, String cuil, String idCObis, String periodo) {
		return futuro(() -> RentaFinanciera.get(contexto, cuil, idCObis, periodo));
	}

	// GET /v1/rentafinanciera/mayorista
	public static Futuro<RentaFinancieraMayoristas> rentaFinancieraMayoristas(Contexto contexto, String idCobis, String periodo) {
		return futuro(() -> RentaFinancieraMayoristas.get(contexto, idCobis, periodo));
	}

	// GET /v1/rentafinanciera/minorista
	public static Futuro<RentaFinancieraMinoristas> rentaFinancieraMinoristas(Contexto contexto, String cuil) {
		return futuro(() -> RentaFinancieraMinoristas.get(contexto, cuil));
	}

	/* ========== Ordenes Controller ========== */

	// GET /v1/ordenes
	public static Futuro<Ordenes> ordenes(Contexto contexto, String cuentaComitente, String desdePagina, String idCobis, String registroxPagina, Fecha fechaDesde, Fecha fechaHasta) {
		return futuro(() -> Ordenes.get(contexto, cuentaComitente, desdePagina, idCobis, registroxPagina, fechaDesde, fechaHasta));
	}

	// TODO: POST /v1/ordenes

	// GET /v1/ordenes/{idorden}
	public static Futuro<OrdenesByIdOrden> ordenesByIdOrden(Contexto contexto, String idOrden, String desdePagina, String idCobis, String registroxPagina) {
		return futuro(() -> OrdenesByIdOrden.get(contexto, idOrden, desdePagina, idCobis, registroxPagina));
	}

	/* ========== Fix Controller ========== */

	// GET /v1/oferta
	public static Futuro<Ofertas> ofertasByCantidad(Contexto contexto, String id, String cantidad) {
		return futuro(() -> Ofertas.get(contexto, id, cantidad));
	}

	public static Futuro<Ofertas> ofertas(Contexto contexto, String id) {
		return futuro(() -> Ofertas.get(contexto, id));
	}

	// TODO: POST /v1/oferta

	/* ========== Simulacion Controller ========== */

	// GET /v1/simulacion/lecaps
	public static Futuro<SimulacionLecaps> simulacionLecaps(Contexto contexto) {
		return futuro(() -> SimulacionLecaps.get(contexto));
	}

	// GET /v1/simulacion/letes
	public static Futuro<SimulacionLetes> simulacionLetes(Contexto contexto) {
		return futuro(() -> SimulacionLetes.get(contexto));
	}
	
	public static Futuro<ApiResponse> altaCuentaComitente(Contexto contexto, SesionBB sesion, Domicilio domicilioPostal, Telefono telefono, Persona persona, CuentaBB cuentaPesos, CuentaBB cuentaDolares) {
		return futuro(() -> CuentaInversor.postPaqueteCC(contexto, sesion, domicilioPostal, telefono, persona, cuentaPesos, cuentaDolares));
	}
	
	public static Futuro<ApiResponse> altaCuentaCuotapartista(Contexto contexto, SesionBB sesion, Domicilio domicilioPostal, Telefono telefono, Persona persona, CuentaBB cuentaPesos, CuentaBB cuentaDolares, String idPersonaFondo) {
		return futuro(() -> CuentaInversor.postPaqueteCCuotapartista(contexto, sesion, domicilioPostal, telefono, persona, cuentaPesos, cuentaDolares, idPersonaFondo));
	}
	
	public static Futuro<ApiResponse> selectPersonaByDoc(Contexto contexto, String numDocumento, String idTipoDoc) {
		return futuro(() -> CuentaInversor.selectPersonaByDoc(contexto, numDocumento, idTipoDoc));
	}
	
	// GET /v1/tokenvfnet
	public static Futuro<TokenFVNetOB> obtenerTokenVFNet(Contexto contexto, String id) {
		return futuro(() -> TokenFVNetOB.get(contexto, id));
	}

}
