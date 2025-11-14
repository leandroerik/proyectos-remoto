package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.SesionOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoSolicitudFCIOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumMonedasOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.FondosComunesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.ParametriaFciOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.FCIOBRepositorio;

public class ServicioFCIOB extends ServicioOB {

	private static FCIOBRepositorio repo;

	public ServicioFCIOB(ContextoOB contexto) {
		super(contexto);
		repo = new FCIOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<FondosComunesOB> find(Integer codigo) {
		return futuro(() -> repo.find(codigo));
	}

	public Futuro<FondosComunesOB> update(FondosComunesOB fci) {
		return futuro(() -> repo.update(fci));
	}

	public Futuro<FondosComunesOB> crear(ContextoOB contexto, ParametriaFciOB parametria, String cuenta, BigDecimal monto, String moneda, String idCuentaBancaria, String idCuotapartista, String tipoValorCuotaparte, String tipoSolicitud, Integer cantCuotaPartes, Boolean esTotal, String tipoCuenta, String plazoLiquidacion) {
		FondosComunesOB fci = new FondosComunesOB();
		ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
		ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
		ServicioEstadoInversionOB servicioEstadoInversionOB = new ServicioEstadoInversionOB(contexto);
		ServicioMonedaOB servicioMonedaOB = new ServicioMonedaOB(contexto);
		SesionOB sesion = contexto.sesion();

		MonedaOB monedaOB = new MonedaOB();
		if (moneda.equals("1"))
			monedaOB = servicioMonedaOB.find(EnumMonedasOB.PESOS.getMoneda()).get();
		if (moneda.equals("2"))
			monedaOB = servicioMonedaOB.find(EnumMonedasOB.DOLARES.getMoneda()).get();

		fci.abreviaturaFondo = parametria.fondoNombre;
		fci.cuentaOrigen = cuenta;
		fci.monto = monto;
		fci.moneda = monedaOB;
		fci.monedaId = monedaOB;
		fci.idFondo = parametria;
		fci.fechaInicio = LocalDateTime.now();
		fci.fechaUltActulizacion = LocalDateTime.now();
		fci.estadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
		fci.tipoProductoFirma = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.FCI.getCodigo()).get();
		fci.estado = servicioEstadoInversionOB.find(EnumEstadoSolicitudFCIOB.PENDIENTE.getCodigo()).get();
		fci.empCodigo = sesion.empresaOB;
		fci.empresa = sesion.empresaOB;
		fci.idCuentaBancaria = idCuentaBancaria;
		fci.idCuotapartista = idCuotapartista;
		fci.tipoVCPid = tipoValorCuotaparte;
		fci.tipoSolicitud = tipoSolicitud;
		fci.cantidadCuotaPartes = cantCuotaPartes;
		fci.esTotal = esTotal;
		fci.usuario = sesion.usuarioOB;
		fci.tipoCuenta = tipoCuenta;
		fci.plazoLiquidacion = plazoLiquidacion;

		return futuro(() -> repo.create(fci));
	}

	public Futuro<FondosComunesOB> crearBandejaOff(ContextoOB contexto, ParametriaFciOB parametria, String cuenta, BigDecimal monto, String moneda, String idCuentaBancaria, String idCuotapartista, String tipoValorCuotaparte, String tipoSolicitud, Integer cantCuotaPartes, Boolean esTotal) {
		FondosComunesOB fci = new FondosComunesOB();
		ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
		ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
		ServicioEstadoInversionOB servicioEstadoInversionOB = new ServicioEstadoInversionOB(contexto);
		ServicioMonedaOB servicioMonedaOB = new ServicioMonedaOB(contexto);
		SesionOB sesion = contexto.sesion();

		MonedaOB monedaOB = new MonedaOB();
		if (moneda.equals("1"))
			monedaOB = servicioMonedaOB.find(EnumMonedasOB.PESOS.getMoneda()).get();
		if (moneda.equals("2"))
			monedaOB = servicioMonedaOB.find(EnumMonedasOB.DOLARES.getMoneda()).get();

		fci.abreviaturaFondo = parametria.fondoNombre;
		fci.cuentaOrigen = cuenta;
		fci.monto = monto;
		fci.moneda = monedaOB;
		fci.idFondo = parametria;
		fci.fechaInicio = LocalDateTime.now();
		fci.fechaUltActulizacion = LocalDateTime.now();
		fci.estadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()).get();
		fci.tipoProductoFirma = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.FCI.getCodigo()).get();
		fci.estado = servicioEstadoInversionOB.find(EnumEstadoSolicitudFCIOB.EN_PROCESO.getCodigo()).get();
		fci.empresa = sesion.empresaOB;
		fci.idCuentaBancaria = idCuentaBancaria;
		fci.idCuotapartista = idCuotapartista;
		fci.tipoVCPid = tipoValorCuotaparte;
		fci.tipoSolicitud = tipoSolicitud;
		fci.cantidadCuotaPartes = cantCuotaPartes;
		fci.esTotal = esTotal;

		return futuro(() -> repo.create(fci));
	}

	public Futuro<List<FondosComunesOB>> filtrarMovimientosHistorial(EmpresaOB empresa, Fecha fechaDesde, Fecha fechaHasta, String cuenta, String tipoSolicitud, MonedaOB moneda) {
		return futuro(() -> repo.filtrarMovimientosHistorial(empresa, fechaDesde, fechaHasta, cuenta, tipoSolicitud, moneda));

	}

	public Futuro<List<FondosComunesOB>> buscarSinFirmaCompletaPorVencer(ContextoOB contexto) {
		ServicioEstadoInversionOB servicioEstadoInversion = new ServicioEstadoInversionOB(contexto);
		int estadoPendiente = servicioEstadoInversion.find(EnumEstadoSolicitudFCIOB.PENDIENTE.getCodigo()).get().id;
		return futuro(() -> repo.buscarSinFirmaCompletaPorVencer(estadoPendiente));
	}
	
	  public Futuro<FondosComunesOB> buscarIdSolicitud(String idSolicitud) {
	        return futuro(() -> repo.buscarPorIdSolicitud(idSolicitud));
	    }

	
}
