package ar.com.hipotecario.backend.servicio.api.cuentas;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.servicio.api.cuentas.CajasAhorrosV1.CajaAhorroV1;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentasCorrientes.CuentaCorriente;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentasAcuerdos.CuentasAcuerdo;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentasAlias.CuentaAlia;
import ar.com.hipotecario.backend.servicio.api.productos.CuentasOB;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.ModuloOB;
import ar.com.hipotecario.canal.officebanking.jpa.dto.InfoCuentaDTO;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioMonedaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// https://api-cuentas-microservicios-homo.appd.bh.com.ar/swagger-ui.html
public class ApiCuentas {

	/*
	 * ========== Consulta de bloqueo, ya sea cuenta corriente o caja de ahorro
	 * ==========
	 */

	// TODO: GET /v1/cajasahorros2/{idcuenta}/tarjetasdebito

	/* ========== Operatoria de Adhesión a Débito Automático ========== */

	// TODO: GET /v1/debitoautomatico

	// TODO: POST /v1/debitoautomatico

	// TODO: DELETE /v1/debitoautomatico

	// TODO: PATCH /v1/debitoautomatico

	/* ========== Operatoria de Créditos ========== */

	// TODO: PATCH /v1/creditos/{id}

	/* ========== Operatoria de caja de ahorro ========== */

	// GET /v1/cajasahorros/{idcuenta}
	public static Futuro<CajaAhorroV1> cajaAhorroV1(Contexto contexto, String idCuenta, Fecha fechaDesde) {
		return Util.futuro(() -> CajasAhorrosV1.get(contexto, idCuenta, fechaDesde));
	}

	public static Futuro<CajaAhorroV1> cajasAhorroV1(Contexto contexto, String idCuenta, Fecha fechaDesde, Boolean historico, Boolean validacuentaempleado) {
		return Util.futuro(() -> CajasAhorrosV1.get(contexto, idCuenta, fechaDesde, historico, validacuentaempleado));
	}

	// TODO: GET /v1/cajasahorros/{idcuenta}/bloqueos

	// TODO: POST /v1/cajasahorros/{idcuenta}/bloqueos

	// TODO: POST /v1/cajasahorros/{idcuenta}/boletaDeposito

	// TODO: POST /v1/cajasahorros/{idcuenta}/credito

	// TODO: POST /v1/cajasahorros/{idcuenta}/debito

	// GET /v1/cajasahorros/{idcuenta}/debitosadheridos
	public static Futuro<DebitosAdheridosCajaAhorro> debitosAdheridosCajaAhorro(Contexto contexto, String idCuenta) {
		return Util.futuro(() -> DebitosAdheridosCajaAhorro.get(contexto, idCuenta));
	}

	// TODO: POST /v1/cajasahorros/{idcuenta}/desbloqueos

	// TODO: POST /v1/cajasahorros/{idcuenta}/desbloqueosparciales

	// TODO: PATCH /v1/cajasahorros/{idcuenta}/marcasDigitales

	// GET /v1/cajasahorros/{idcuenta}/movimientos
	public static Futuro<MovimientosCajaAhorros> movimientosCajaAhorrosDiasHabiles(Contexto contexto, String idCuenta, Fecha fechaDesde, Fecha fechaHasta, String numeroPagina, Character orden, Integer pendientes, Character tipoMovimiento, Boolean validactaempleado, String concepto) {
		return Util.futuro(() -> MovimientosCajaAhorros.get(contexto, idCuenta, fechaDesde, fechaHasta, numeroPagina, orden, pendientes, tipoMovimiento, validactaempleado, concepto));
	}

	public static Futuro<MovimientosCajaAhorros> movimientosCajaAhorros(Contexto contexto, String idCuenta, Fecha fechaDesde, Fecha fechaHasta, String numeroPagina, Character orden, Integer pendientes, Character tipoMovimiento, Boolean validactaempleado, Character diaHabil, String concepto) {
		return Util.futuro(() -> MovimientosCajaAhorros.get(contexto, idCuenta, fechaDesde, fechaHasta, numeroPagina, orden, pendientes, tipoMovimiento, validactaempleado, diaHabil, concepto));
	}

	// GET /v1/cajasahorros/{idcuenta}/resumen
	public static Futuro<ResumenCajaAhorro> resumenCajaAhorro(Contexto contexto, String idCuenta, Fecha fechaDesde, Fecha fechaHasta) {
		return Util.futuro(() -> ResumenCajaAhorro.get(contexto, idCuenta, fechaDesde, fechaHasta));
	}

	// GET /v1/cajasahorros/{idcuenta}/tarjetasdebito
	public static Futuro<TarjetasDebitoAsociadasCajaAhorro> tarjetasDebitoAsociadasCajaAhorro(Contexto contexto, String idCuenta) {
		return Util.futuro(() -> TarjetasDebitoAsociadasCajaAhorro.get(contexto, idCuenta));
	}

	// TODO: GET /v2/cajasahorros/{idcuenta}

	// GET /v2/cajasahorros/{idcuenta}/bloqueos
	public static Futuro<BloqueosCajaAhorro> bloqueosCajaAhorro(Contexto contexto, String idCuenta) {
		return Util.futuro(() -> BloqueosCajaAhorro.get(contexto, idCuenta));
	}

	// GET /v2/cajasahorros/{idcuenta}/saldosbloqueos
	public static Futuro<BloqueoSaldosCajaAhorro> bloqueoSaldosCajaAhorro(Contexto contexto, String idCuenta) {
		return Util.futuro(() -> BloqueoSaldosCajaAhorro.get(contexto, idCuenta));
	}

	public static Futuro<BloqueoSaldosCajaAhorro> bloqueoSaldosCajaAhorro(Contexto contexto, String idCuenta, Fecha fecha) {
		return Util.futuro(() -> BloqueoSaldosCajaAhorro.get(contexto, idCuenta, fecha));
	}

	/* ========== Operatoria de cuenta corriente ========== */

	// TODO: GET /v1/cuentacorriente/resumen/{idcuenta}

	// GET /v1/cuentascorrientes/{idcuenta}
	public static Futuro<CuentaCorriente> cuentaCorriente(Contexto contexto, String idCuenta, Fecha fechaDesde, Boolean historico, Boolean validaCuentaEmpleado) {
		return Util.futuro(() -> CuentasCorrientes.get(contexto, idCuenta, fechaDesde, historico, validaCuentaEmpleado));
	}

	// GET /v1/acuerdos/cuenta?cuenta={idcuenta}
	public static Futuro<List<Objeto>> cuentaAcuerdos(Contexto contexto, String idCuenta) {
		return Util.futuro(() -> CuentasAcuerdos.get(contexto, idCuenta));
	}

	public static  Futuro<Objeto> modificarOAsignarAlias(Contexto contexto, String cbu, String alias, String nuevoAlias, String cuit, Boolean tieneAlias){
		return  Util.futuro(() -> CuentasAlias.update(contexto, cbu, alias, nuevoAlias, cuit, tieneAlias));
	}

	public static  Futuro<Objeto> infoAlias(Contexto contexto, String cbuAlias, String cuitCuenta){
		return  Util.futuro(() -> CuentasAlias.infoAlias(contexto, cbuAlias, cuitCuenta));
	}

	public static  Futuro<Objeto> obtenerQRCuenta(Contexto contexto, String cbu, String cuit, String tipoCuenta){
		return Util.futuro(() -> CuentasQR.obtenerQRCuenta(contexto, cbu, cuit, tipoCuenta));
	}

	// TODO: GET /v1/cuentascorrientes/{idcuenta}/bloqueos

	// TODO: POST /v1/cuentascorrientes/{idcuenta}/bloqueos

	// TODO: POST /v1/cuentascorrientes/{idcuenta}/boletaDeposito

	// GET /v1/cuentascorrientes/{idcuenta}/chequeras
	public static Futuro<Chequeras> chequeras(Contexto contexto, String idCuenta) {
		return Util.futuro(() -> Chequeras.get(contexto, idCuenta));
	}

	// TODO: POST /v1/cuentascorrientes/{idcuenta}/debito

	// GET /v1/cuentascorrientes/{idcuenta}/debitosadheridos
	public static Futuro<DebitosAdheridosCuentaCorriente> debitosAdheridosCuentaCorriente(Contexto contexto, String idCuenta) {
		return Util.futuro(() -> DebitosAdheridosCuentaCorriente.get(contexto, idCuenta));
	}

	// TODO: POST /v1/cuentascorrientes/{idcuenta}/desbloqueos

	// TODO: POST /v1/cuentascorrientes/{idcuenta}/desbloqueosparciales

	// GET /v1/cuentascorrientes/{idcuenta}/movimientos
	public static Futuro<MovimientosCuentaCorriente> movimientosCuentasCorrienteDiasHabiles(Contexto contexto, String idCuenta, Fecha fechaDesde, Fecha fechaHasta, String numeroPagina, Character orden, Integer pendientes, Character tipoMovimiento, Boolean validactaempleado, String concepto) {
		return Util.futuro(() -> MovimientosCuentaCorriente.get(contexto, idCuenta, fechaDesde, fechaHasta, numeroPagina, orden, pendientes, tipoMovimiento, validactaempleado, concepto));
	}

	public static Futuro<MovimientosCuentaCorriente> movimientosCuentaCorriente(Contexto contexto, String idCuenta, Fecha fechaDesde, Fecha fechaHasta, String numeroPagina, Character orden, Integer pendientes, Character tipoMovimiento, Boolean validactaempleado, Character diaHabil, String concepto) {
		return Util.futuro(() -> MovimientosCuentaCorriente.get(contexto, idCuenta, fechaDesde, fechaHasta, numeroPagina, orden, pendientes, tipoMovimiento, validactaempleado, diaHabil, concepto));
	}

	// GET /v1/cuentascorrientes/{idcuenta}/tarjetasdebito
	public static Futuro<TarjetasDebitoAsociadasCuentaCorriente> tarjetasDebitoAsociadasCuentaCorriente(Contexto contexto, String idCuenta) {
		return Util.futuro(() -> TarjetasDebitoAsociadasCuentaCorriente.get(contexto, idCuenta));
	}

	// TODO: GET /v2/cuentascorrientes/{idcuenta}

	// GET /v2/cuentascorrientes/{idcuenta}/bloqueos
	public static Futuro<BloqueosCuentaCorriente> bloqueosCuentaCorriente(Contexto contexto, String idCuenta) {
		return Util.futuro(() -> BloqueosCuentaCorriente.get(contexto, idCuenta));
	}

	// GET /v2/cuentascorrientes/{idcuenta}/saldosbloqueos
	public static Futuro<BloqueoSaldoCuentaCorriente> bloqueoSaldoCuentaCorriente(Contexto contexto, String idCuenta) {
		return Util.futuro(() -> BloqueoSaldoCuentaCorriente.get(contexto, idCuenta));
	}

	/* ========== Operatoria de cuentas, Alias ========== */

	// TODO: PUT /v1/alias

	// TODO: PATCH /v1/alias

	/*
	 * ========== Operatoria de cuentas, ya sea cuenta corriente o caja de ahorro
	 * ==========
	 */

	// GET /v1/cuentas

	public static Futuro<CuentaCoelsa> cuentaCoelsa(Contexto contexto, String cbu, String alias) {
		return Util.futuro(() -> CuentaCoelsa.get(contexto, cbu, alias));
	}
	
	public static Futuro<CuentaCoelsa> cuentaCoelsa(Contexto contexto, String cbu) {
		return Util.futuro(() -> CuentaCoelsa.get(contexto, cbu));
	}

	public static Futuro<CuentaLink> cuentaLink(Contexto contexto, String cbu) {
		return Util.futuro(() -> CuentaLink.get(contexto, cbu));
	}

	public static Futuro<CuentaLink> cuentaLink(Contexto contexto, String cbu, String idMoneda) {
		return Util.futuro(() -> CuentaLink.get(contexto, cbu, idMoneda));
	}

	public static Futuro<CuentaLink> cuentaLink(Contexto contexto, String cbu, String idMoneda, String numeroTarjetaDebito) {
		return Util.futuro(() -> CuentaLink.get(contexto, cbu, idMoneda, numeroTarjetaDebito));
	}

	public static Futuro<CuentasOB> cuentas(Contexto contexto, String idCliente) {
		return Util.futuro(() -> CuentasOB.get(contexto, idCliente));
	}

	// GET /v1/cuentas/{id}/bloqueos
	public static Futuro<BloqueosCuenta> bloqueosCuenta(Contexto contexto, String id, String tipoCuenta) {
		return Util.futuro(() -> BloqueosCuenta.get(contexto, id, tipoCuenta));
	}

	// GET /v1/cuentas/{id}/depositos
	public static Futuro<DepositoCuenta> depositoCuenta(Contexto contexto, String id) {
		return Util.futuro(() -> DepositoCuenta.get(contexto, id));
	}

	// GET /v1/cuentas/{id}/firmaDigital
	public static Futuro<FirmaDigitalCuenta> firmaDigitalCuenta(Contexto contexto, String id, String idCuil) {
		return Util.futuro(() -> FirmaDigitalCuenta.get(contexto, id, idCuil));
	}

	// POST /v1/cuentas/{idcuenta}/creditos
	public static Futuro<DebitosCreditosOB> creditos(Contexto contexto, String codigoConcepto, String numeroCuenta, Integer moneda, BigDecimal importe, String usuario, String tipoCuenta) {
		return Util.futuro(() -> DebitosCreditosOB.postCredito(contexto, codigoConcepto, numeroCuenta, moneda, importe, usuario, tipoCuenta));
	}

	// PATCH /v1/cuentas/{idcuenta}/creditos/{idtransaccion}
	public static Futuro<DebitosCreditosOB> reversaCreditos(Contexto contexto, String codigoConcepto, String numeroCuenta, Integer moneda, BigDecimal importe, String usuario, String tipoCuenta, String idTransaccion) {
		return Util.futuro(() -> DebitosCreditosOB.patchCredito(contexto, codigoConcepto, numeroCuenta, moneda, importe, usuario, tipoCuenta, idTransaccion));
	}

	// POST /v1/cuentas/{idcuenta}/debitos
	public static Futuro<DebitosCreditosOB> debitos(Contexto contexto, String codigoConcepto, String numeroCuenta, Integer moneda, BigDecimal importe, String usuario, String tipoCuenta) {
		return Util.futuro(() -> DebitosCreditosOB.postDebito(contexto, codigoConcepto, numeroCuenta, moneda, importe, usuario, tipoCuenta));
	}

	// PATCH /v1/cuentas/{idcuenta}/debitos/{idtransaccion}
	public static Futuro<DebitosCreditosOB> reversaDebitos(Contexto contexto, String codigoConcepto, String numeroCuenta, Integer moneda, BigDecimal importe, String usuario, String tipoCuenta, String idTransaccion) {
		return Util.futuro(() -> DebitosCreditosOB.patchDebito(contexto, codigoConcepto, numeroCuenta, moneda, importe, usuario, tipoCuenta, idTransaccion));
	}

	// GET /v1/cuentas/{idcuenta}/limites
	public static Futuro<LimitesCuenta> limitesCuentas(Contexto contexto, String idcuenta, String idCliente, Fecha fechaDesde, Fecha fechaHasta, String idMoneda) {
		return Util.futuro(() -> LimitesCuenta.get(contexto, idcuenta, idCliente, fechaDesde, fechaHasta, idMoneda));
	}

	// TODO: POST /v1/cuentas/{idcuenta}/limites

	// GET /v1/cuentas/{idcuenta}/limites/disponible
	public static Futuro<LimitesCuentaDisponible> limitesCuentaDisponible(Contexto contexto, String idcuenta, String idCliente, Fecha fecha, String idMoneda, String importe) {
		return Util.futuro(() -> LimitesCuentaDisponible.get(contexto, idcuenta, idCliente, fecha, idMoneda, importe));
	}

	// GET /v1/cuentas/{idcuenta}/resumen
	public static Futuro<ResumenCuenta> resumenCuenta(Contexto contexto, String numeroCuenta, Fecha fechaDesde, Fecha fechaHasta, String producto) {
		return Util.futuro(() -> ResumenCuenta.get(contexto, numeroCuenta, fechaDesde, fechaHasta, producto));
	}

	// TODO: POST /v1/cuentas/{idcuenta}/transferencias

	// GET /v1/cuentas/{idcuenta}/valoresensuspenso
	public static Futuro<ValoresEnSuspenso> valoresEnSuspenso(Contexto contexto, String id, String operacion, String secuencial) {
		return Util.futuro(() -> ValoresEnSuspenso.get(contexto, id, operacion, secuencial));
	}

	// TODO: POST /v1/cuentas/cierre

	// TODO: GET /v1/cuentas/cotitularidadfuncionario

	// GET /v1/cuentas/deudas
	public static Futuro<DeudasCuenta> deudasCuenta(Contexto contexto, String idEmpresas, Boolean paginar, String secuencial) {
		return Util.futuro(() -> DeudasCuenta.get(contexto, idEmpresas, paginar, secuencial));
	}

	// GET /v1/cuentas/estado
	public static Futuro<EstadoCuenta> estadoCuenta(Contexto contexto, String cbu, String idTributario) {
		return Util.futuro(() -> EstadoCuenta.get(contexto, cbu, idTributario));
	}

	// GET /v1/cuentas/motivoscierre
	public static Futuro<MotivosCierreCuenta> motivosCierreCuenta(Contexto contexto) {
		return Util.futuro(() -> MotivosCierreCuenta.get(contexto));
	}

	// GET /v1/cuentas/movimientos
	public static Futuro<MovimientosCuenta> movimientoCuentas(Contexto contexto, String cuenta, String file, String from, String validaCuenta) {
		return Util.futuro(() -> MovimientosCuenta.get(contexto, cuenta, file, from, validaCuenta));
	}

	// TODO: POST /v1/cuentas/precierre

	// TODO: POST /v1/cuentas/validacompradivisas

	// TODO: GET /v2/cuentas

	// TODO: POST /v2/cuentas/{idcuenta}/transferencias

	/* ========== Operatoria de topes de operación ========== */

	// TODO: POST /v1/topes

	/* ========== Operatoria de transferencia ========== */

	// GET /v1/transferencias
	public static Futuro<PescadoraTransferencia> pescadoraTransferencia(Contexto contexto, String idRequerimiento, String numeroTarjeta, String timeStampTransferencia) {
		return Util.futuro(() -> PescadoraTransferencia.get(contexto, idRequerimiento, numeroTarjeta, timeStampTransferencia));
	}

	// TODO: POST /v1/transferencias

	// TODO: POST /v1/transferencias/{idcuenta}/cuentaspropias

	// TODO: POST /v1/transferencias/{idcuenta}/entrecuentas

	// TODO: POST /v1/transferencias/{idcuenta}/externas
}
