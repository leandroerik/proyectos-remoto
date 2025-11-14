package ar.com.hipotecario.backend.servicio.api.prestamos;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.servicio.api.inversiones.ComprobantePrestamosOB;

//http://api-prestamos-microservicios-homo.appd.bh.com.ar/swagger-ui.html
public class ApiPrestamos extends Api {

	/* ========== Credito Tasa Cero Controller ========== */

	// GET /v1/prestamos/creditos/{cuil}
	public static Futuro<Prestamos.CreditosTasaCero> creditosTasaCero(Contexto contexto, String cuil) {
		return futuro(() -> Prestamos.getCreditosTasaCero(contexto, cuil));
	}

	/* ========== Cuenta Rest Controller ========== */

	// GET /v1/cuentas/deudas
	public static Futuro<CuentasDeudas> cuentasDeudas(Contexto contexto, String idEmpresas, Boolean paginar, String secuencial, String tipoConsulta) {
		return futuro(() -> CuentasDeudas.get(contexto, idEmpresas, paginar, secuencial, tipoConsulta));
	}

	/* ========== Microcreditos Controller ========== */

	// GET /v1/prestamos/{codSolicitud}/detalles
	public static Futuro<Microcreditos.Detalles> detalles(Contexto contexto, Integer codSolicitud) {
		return futuro(() -> Microcreditos.getDetalles(contexto, codSolicitud));
	}

	// GET /v1/prestamos/{cuil}/solicitudes
	public static Futuro<Microcreditos.Solicitudes> solicitudes(Contexto contexto, String cuil) {
		return futuro(() -> Microcreditos.getSolicitudes(contexto, cuil));
	}

	// GET /v1/prestamos/{cuit}/informeveraz
	public static Futuro<Microcreditos.InformesVeraz> informesVeraz(Contexto contexto, String cuit) {
		return futuro(() -> Microcreditos.getInformeVeraz(contexto, cuit));
	}

	// GET /v1/prestamos/{id}/beneficiario
	public static Futuro<Microcreditos.Beneficiarios> beneficiarios(Contexto contexto, String idCobis, String tipo) {
		return futuro(() -> Microcreditos.getBeneficiarios(contexto, idCobis, tipo));
	}

	// TODO: PATCH /v1/prestamos/aviso

	// GET /v1/prestamos/procrearestadistica
	public static Futuro<Microcreditos.ProcrearEstadisticas> procrearEstadisticas(Contexto contexto) {
		return futuro(() -> Microcreditos.getProcrearEstadisticas(contexto));
	}

	/* ========== Pago Prestamo Controller ========== */

	// TODO: POST /v1/pagoprestamo

	// GET /v1/pagosprestamo
	public static Futuro<Pagos> pagosPrestamos(Contexto contexto, String idPrestamo, Fecha fechaDesde, Fecha fechaHasta) {
		return futuro(() -> Pagos.get(contexto, idPrestamo, fechaDesde, fechaHasta));
	}

	// GET /v1/pagosprestamo/{id}/rubros
	public static Futuro<Pagos.PagosPrestamosRubros> pagosPrestamosRubros(Contexto contexto, String idPrestamo, String id) {
		return futuro(() -> Pagos.getPrestamosRubros(contexto, idPrestamo, id));
	}

	/* ========== Prestamo Controller ========== */

	// TODO: GET /v1/prestamos

	// GET /v1/prestamos/{id}
	public static Futuro<Prestamos.PrestamosPorId> prestamosPorId(Contexto contexto, String id, Boolean detalle) {
		return futuro(() -> Prestamos.get(contexto, id, detalle));
	}

	// GET /v1/prestamos/{id}/cuotas
	public static Futuro<PrestamosCuotas> prestamosCuotasPorCuota(Contexto contexto, String id, String cuota) {
		return futuro(() -> PrestamosCuotas.get(contexto, id, cuota));
	}

	public static Futuro<PrestamosCuotas> prestamosCuotasPorFechas(Contexto contexto, String id, Fecha fechaDesde, Fecha fechaHasta) {
		return futuro(() -> PrestamosCuotas.get(contexto, id, fechaDesde, fechaHasta));
	}

	public static Futuro<ComprobantePrestamosOB> comprobantePrestamo(Contexto contexto, String estado, String operacion, String recibo, String siguiente) {
		return futuro(() -> ComprobantePrestamosOB.get(contexto, estado, operacion, recibo,siguiente));
	}


	public static Futuro<PrestamosCuotas> prestamosCuotasPorFechasYCuotas(Contexto contexto, String id, Fecha fechaDesde, Fecha fechaHasta, String cuota) {
		return futuro(() -> PrestamosCuotas.get(contexto, id, fechaDesde, fechaHasta, cuota));
	}

	// TODO: DELETE /v1/prestamos/{id}/gasto

	// TODO: DELETE /v1/prestamos/{id}/negociacion

	// GET /v1/prestamos/{id}/novedades
	public static Futuro<Prestamos.Novedades> prestamosNovedades(Contexto contexto, String id) {
		return futuro(() -> Prestamos.getNovedades(contexto, id));
	}

	// TODO: POST /v1/prestamos/{id}/pagos/electronicos

	// TODO: GET /v1/prestamos/{id}/resumen

	// GET /v1/prestamos/{id}/resumennsp
	public static Futuro<Prestamos.ResumenNsp> prestamosResumenNsp(Contexto contexto, String id, String tipoPagoPrestamo) {
		return futuro(() -> Prestamos.getResumenNsp(contexto, id, tipoPagoPrestamo));
	}

	// TODO: PUT /v1/prestamos/{id}/sustituciongarantia

	// GET /v1/prestamos/{numCuenta}/movimientos
	public static Futuro<PrestamosMovimientos> prestamosMovimientos(Contexto contexto, String numCuenta, Fecha fechaMovimiento, String productoCobis, String secuencial) {
		return futuro(() -> PrestamosMovimientos.get(contexto, numCuenta, fechaMovimiento, productoCobis, secuencial));
	}

	// GET /v1/prestamos/{numOperacion}/detallepagos
	public static Futuro<PrestamosDetallesPagos> prestamosDetallesPagos(Contexto contexto, String numOperacion, String secPago) {
		return futuro(() -> PrestamosDetallesPagos.get(contexto, numOperacion, secPago));
	}

	// GET /v1/prestamos/{numOperacion}/formacobro
	public static Futuro<Prestamos.FormasCobros> prestamosFormasCobros(Contexto contexto, String numOperacion) {
		return futuro(() -> Prestamos.getFormasCobro(contexto, numOperacion));
	}

	// GET /v1/prestamos/{numoperacion}/pagos
	public static Futuro<PrestamosPagos> prestamosPagos(Contexto contexto, String numOperacion, Fecha fechaDesde, Fecha fechaHasta) {
		return futuro(() -> PrestamosPagos.get(contexto, numOperacion, fechaDesde, fechaHasta));
	}

	// TODO: POST /v1/prestamos/aplicacion

	// TODO: POST /v1/prestamos/aplicapago

	// GET /v1/prestamos/consulta
	public static Futuro<PrestamosConsulta> prestamosConsulta(Contexto contexto, String numOperacion, Fecha fecha, String tipoCancelacion) {
		return futuro(() -> PrestamosConsulta.get(contexto, numOperacion, fecha, tipoCancelacion));
	}

	// TODO: POST /v1/prestamos/formacobro

	// GET /v1/prestamos/gasto
	public static Futuro<PrestamosGastos> prestamosGastos(Contexto contexto, String numOperacion, Fecha fecha) {
		return futuro(() -> PrestamosGastos.get(contexto, numOperacion, fecha));
	}

	// TODO: POST /v1/prestamos/gasto

	// GET /v1/prestamos/impuestosellos/{idcliente}
	public static Futuro<Prestamos.ImpuestosSellos> prestamosImpuestosSellos(Contexto contexto, String idcliente, String iddestinoprestamo, String iddestinovivienda, String idmoneda, String montoaprobado, String plazo, String sucursal, String tipoPrestamo) {
		return futuro(() -> Prestamos.getImpuestosSellos(contexto, idcliente, iddestinoprestamo, iddestinovivienda, idmoneda, montoaprobado, plazo, sucursal, tipoPrestamo));
	}

	// TODO: POST /v1/prestamos/negociacion

	// TODO: POST /v1/prestamos/reversapago

	// GET /v1/prestamosnsp/{id}
	public static Futuro<PrestamosNsp> prestamosNsp(Contexto contexto, String id, String producto) {
		return futuro(() -> PrestamosNsp.get(contexto, id, producto));
	}

	// GET /v2/prestamos
	public static Futuro<Prestamos> prestamos(Contexto contexto, String idCliente, Boolean estado, Boolean buscansp) {
		return futuro(() -> Prestamos.get(contexto, idCliente, estado, buscansp));
	}

	public static Futuro<Prestamos> prestamos(Contexto contexto, String idCliente) {
		return futuro(() -> Prestamos.get(contexto, idCliente));
	}

	public static Futuro<Prestamos> prestamosPorEstados(Contexto contexto, String idCliente, String tipoEstado, Boolean buscansp) {
		return futuro(() -> Prestamos.getPorEstado(contexto, idCliente,tipoEstado,buscansp));
	}


	// TODO: POST /v2/prestamos/{id}/pagos

	// TODO: POST /v2/prestamos/{id}/pagos/{idReversa}

	/* ========== Prestamo Empresarial Controller ========== */

	// GET /v1/prestamosempresarial/{id}
	public static Futuro<Empresarial> prestamosEmpresarial(Contexto contexto, String id) {
		return futuro(() -> Empresarial.get(contexto, id));
	}

	/* ========== Preventa Controller ========== */

	// GET /v1/preventas/{id}
	public static Futuro<Preventas> preventas(Contexto contexto, String id) {
		return futuro(() -> Preventas.getPreventas(contexto, id));
	}

	// GET /v1/preventas/{id}/anticipos
	public static Futuro<Preventas.PreventasAnticipos> preventasAnticipos(Contexto contexto, String id, Fecha fechaDesde, Fecha fechaHasta) {
		return futuro(() -> Preventas.getPreventasAnticipos(contexto, id, fechaDesde, fechaHasta));
	}

	/* ========== Relacion Garantia Controller ========== */

	// TODO: POST /v1/relacionesgarantia

	// TODO: DELETE /v1/relacionesgarantia

	/* ========== Simulacion Controller ========== */

	// TODO: POST /v1/simulaciones/prestamospersonales

	/* ========== Unidad Funcional Controller ========== */

	// TODO: POST /v1/prestamos/{ufuIdentificacion}/unidadFuncional

	// TODO: POST /v1/prestamos/DDJJProcrear

	/* ========== Vencimientos Controller ========== */

	// GET /v1/vencimientos/{plantilla}/prestamos
	public static Futuro<Vencimientos> vencimientos(Contexto contexto, String plantilla, String cuota, String numeroOperacion) {
		return futuro(() -> Vencimientos.get(contexto, plantilla, cuota, numeroOperacion));
	}

	// TODO: POST /v1/vencimientos/{plantilla}/prestamos

}
