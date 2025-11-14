package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoPagosDeServicioYVepsOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.EstadoPagoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoDeServicios.PagoDeServiciosOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.PagoDeServiciosOBRepositorio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ServicioPagoDeServiciosOB extends ServicioOB {
	private static PagoDeServiciosOBRepositorio repo;
	private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
	private static ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
	private static ServicioEstadoPagoOB servicioEstadoPagosDeServicio = new ServicioEstadoPagoOB(contexto);
	private static ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
	private static ServicioMonedaOB servicioMoneda = new ServicioMonedaOB(contexto);
	private static EstadoPagoOB estadoEnBandeja = servicioEstadoPagosDeServicio.find(EnumEstadoPagosDeServicioYVepsOB.EN_BANDEJA.getCodigo()).get();

	public ServicioPagoDeServiciosOB(ContextoOB contexto) {
		super(contexto);
		repo = new PagoDeServiciosOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<PagoDeServiciosOB> crear(ContextoOB contexto, String ente, String enteDescripcion, String rubro, String codigoLink, String idConcepto, String cuentaOrigen, BigDecimal importe, String referencia, String identificadorPago, String usuarioLP, LocalDate vencimiento, EmpresaOB empresa, String idBase, String descripcion) {
		PagoDeServiciosOB pago = new PagoDeServiciosOB();
		pago.ente = ente;
		pago.descripcionEnte = enteDescripcion;
		pago.rubro = rubro;
		pago.codigoLink = codigoLink;
		pago.conceptoId = idConcepto;
		pago.cuentaOrigen = cuentaOrigen;
		pago.monto = importe;
		pago.estado = servicioEstadoPagosDeServicio.find(EnumEstadoPagosDeServicioYVepsOB.EN_BANDEJA.getCodigo()).get();
		pago.empresa = contexto.sesion().empresaOB;
		pago.estadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
		pago.tipoProductoFirma = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.PAGO_SERVICIOS.getCodigo()).get();
		pago.usuarioModificacion = contexto.sesion().usuarioOB.cuil.toString();
		pago.usuario = contexto.sesion().usuarioOB;
		pago.moneda = servicioMoneda.find(80).get();
		pago.fechaCreacion = LocalDateTime.now();
		pago.ultimaModificacion = LocalDateTime.now();
		pago.fechaUltActulizacion = LocalDateTime.now();
		pago.referencia = referencia;
		pago.identificadorPago = identificadorPago;
		pago.usuarioLP = usuarioLP;
		pago.vencimiento = vencimiento;
		pago.fechaPago = LocalDateTime.now();
		pago.emp_codigo = empresa;
		pago.idDeuda = idBase;
		pago.descripcion = descripcion;

		return futuro(() -> repo.create(pago));
	}

	public Futuro<PagoDeServiciosOB> find(Integer id) {
		return futuro(() -> repo.find(id));
	}

	public Futuro<List<PagoDeServiciosOB>> buscarPorCpeEstadoDelPagoYEmpresa(String ente, String cpe, int estado, EmpresaOB empresa) {
		return futuro(() -> repo.buscarPorCpeYEstado(ente, cpe, estado, empresa));
	}

	public Futuro<PagoDeServiciosOB> update(PagoDeServiciosOB pago) {
		pago.ultimaModificacion = LocalDateTime.now();
		pago.fechaUltActulizacion = LocalDateTime.now();
		return futuro(() -> repo.update(pago));
	}

	public Futuro<List<PagoDeServiciosOB>> buscarSinFirmaCambioDeDia() {
        return futuro(() -> repo.buscarSinFirmaCambioDeDia(estadoEnBandeja));
    }

	public Futuro<List<PagoDeServiciosOB>> buscarPorEmpresaYFiltros(EmpresaOB empresa, String ente, Fecha fechaDesde, Fecha fechaHasta, EstadoPagoOB estado, String codigoLink) {
		return futuro(() -> repo.buscarPorEmpresaYFiltros(empresa, ente, fechaDesde, fechaHasta, estado, codigoLink));
	}
	
	public Futuro<List<PagoDeServiciosOB>> buscarPorEmpresaYEnte(EmpresaOB empresa, String ente, Fecha fechaDesde, Fecha fechaHasta) {
		return futuro(() -> repo.buscarPorEmpresaYEnte(empresa, ente, fechaDesde, fechaHasta));
	}
	

	public Futuro<List<PagoDeServiciosOB>> buscarPorEmpresaYEstadoCodigoLink(EmpresaOB empresa, int estado, String codigoLink) {
		return futuro(() -> repo.buscarPorEmpresaYEstadoCodigoLink(empresa, estado, codigoLink));
	}
}
