package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoPagosDeServicioYVepsOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.EstadoPagoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagosVep.PagosVepOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.PagoVepsOBRepositorio;

public class ServicioPagosVepOB extends ServicioOB {
	private static PagoVepsOBRepositorio repo;
	private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
	private static ServicioEstadoPagoOB servicioEstadoPago = new ServicioEstadoPagoOB(contexto);
	private static ServicioMonedaOB servicioMoneda = new ServicioMonedaOB(contexto);
	private static ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
	private static ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);

	public ServicioPagosVepOB(ContextoOB contexto) {
		super(contexto);
		repo = new PagoVepsOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<PagosVepOB> crear(ContextoOB contexto, String idTributarioCliente, String idTributarioEmpresa, String idTributarioContribuyente, BigDecimal importe, String token, String numeroCuenta, String tipoProducto, String tarjetaVirtual, String numeroVep, EmpresaOB empresa, String descripcion, Fecha vencimiento, String tipoConsultaLink, String idTributarioOriginante) {
		PagosVepOB pago = new PagosVepOB();
		pago.idTributarioCliente = idTributarioCliente;
		pago.idTributarioEmpresa = idTributarioEmpresa;
		pago.idTributarioContribuyente = idTributarioContribuyente;
		pago.token = null;
		pago.tipoProducto = tipoProducto;
		pago.numeroTarjeta = tarjetaVirtual;
		pago.numeroVep = numeroVep;
		pago.usuario = contexto.sesion().usuarioOB;
		pago.emp_codigo = empresa;
		pago.fechaCreacion = LocalDateTime.now();
		pago.estado = servicioEstadoPago.find(EnumEstadoPagosDeServicioYVepsOB.EN_BANDEJA.getCodigo()).get();
		pago.monto = importe;
		pago.moneda = servicioMoneda.find(80).get();
		pago.tipoProductoFirma = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.PAGOS_VEP.getCodigo()).get();
		pago.empresa = contexto.sesion().empresaOB;
		pago.cuentaOrigen = numeroCuenta;
		pago.ultimaModificacion = LocalDateTime.now();
		pago.fechaUltActulizacion = LocalDateTime.now();
		pago.estadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
		pago.descripcion = descripcion;
		pago.fechaVencimiento = LocalDate.parse(vencimiento.toString());
		pago.tipoConsultaLink = tipoConsultaLink;
		pago.idTributarioOriginante = idTributarioOriginante;

		return futuro(() -> repo.create(pago));
	}

	public Futuro<PagosVepOB> find(Integer id) {
		return futuro(() -> repo.find(id));
	}

	public static Futuro<PagosVepOB> update(PagosVepOB pago) {
		pago.ultimaModificacion = LocalDateTime.now();
		pago.fechaUltActulizacion = LocalDateTime.now();

		return futuro(() -> repo.update(pago));
	}

	public Futuro<List<PagosVepOB>> buscarPorEmpresaYFiltros(EmpresaOB empresa, Fecha fechaDesde, Fecha fechaHasta, EstadoPagoOB estado, String numeroVep) {
		return futuro(() -> repo.buscarPorEmpresaYFiltros(empresa, fechaDesde, fechaHasta, estado, numeroVep));
	}
	
	public Futuro<List<PagosVepOB>> buscarPorEmpresaYEnte(EmpresaOB empresa, Fecha fechaDesde, Fecha fechaHasta, String ente) {
		return futuro(() -> repo.buscarPorEmpresaYEnte(empresa, fechaDesde, fechaHasta, ente));
	}

	public Futuro<List<PagosVepOB>> buscarPorEstado(EstadoPagoOB estado) {
		return futuro(() -> repo.buscarPorEstado(estado));
	}
}
