package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.pagosMasivos.EnumEstadoPagosAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.EstadosPagosAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.PagoAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.PagoAProveedoresOBRepositorio;

import java.math.BigDecimal;
import java.sql.Blob;
import java.time.LocalDateTime;
import java.util.List;

public class ServicioPagoAProveedoresOB extends ServicioOB {

    private static PagoAProveedoresOBRepositorio repo;
    private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
    private static ServicioMonedaOB servicioMoneda = new ServicioMonedaOB(contexto);
    private static ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
    private static ServicioEstadosPagoAProveedoresOB servicioEstadosPagoAProveedores = new ServicioEstadosPagoAProveedoresOB(contexto);

    public ServicioPagoAProveedoresOB(ContextoOB contexto) {
        super(contexto);
        repo = new PagoAProveedoresOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<PagoAProveedoresOB> crear(ContextoOB contexto, String cuentaOrigen, BigDecimal importe, String nombreArchivo, Blob archivo, Integer cantidadRegistros, Integer convenio, TipoProductoFirmaOB productoFirma, Integer subconvenio, String nroAdherente, BigDecimal transferencias, BigDecimal cheques, Boolean controlDual) {
        PagoAProveedoresOB pago = new PagoAProveedoresOB();

        pago.emp_codigo = contexto.sesion().empresaOB;
        pago.ultimaModificacion = LocalDateTime.now();
        pago.usuario = contexto.sesion().usuarioOB;
        pago.nombreArchivo = nombreArchivo;
        pago.tipoProducto = productoFirma.descripcion;
        pago.cuentaOrigen = cuentaOrigen;
        pago.convenio = convenio;
        pago.fechaCreacion = LocalDateTime.now();
        pago.empresa = contexto.sesion().empresaOB;
        pago.monto = importe;
        pago.moneda = servicioMoneda.find(80).get();
        pago.estado =
        		(controlDual != null && !controlDual) ?
        				servicioEstadosPagoAProveedores.find(EnumEstadoPagosAProveedoresOB.EN_BANDEJA.getCodigo()).get()
        				:
        				servicioEstadosPagoAProveedores.find(EnumEstadoPagosAProveedoresOB.PENDIENTE_AUTORIZACION.getCodigo()).get();        	
        pago.estadoBandeja =
        		(controlDual != null && !controlDual) ?
        				servicioEstadoBandeja.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get()
        				:
        				servicioEstadoBandeja.find(EnumEstadoBandejaOB.PENDIENTE_AUTORIZACION.getCodigo()).get();
        pago.tipoProductoFirma = productoFirma;
        pago.fechaCreacion = LocalDateTime.now();
        pago.ultimaModificacion = LocalDateTime.now();
        pago.fechaUltActulizacion = LocalDateTime.now();
        pago.usuarioModificacion = contexto.sesion().usuarioOB.cuil.toString();
        pago.archivo = archivo;
        pago.cantidadRegistros = cantidadRegistros;
        pago.subconvenio = subconvenio;
        pago.nroAdherente = nroAdherente;
        pago.transferencias = transferencias;
        pago.cheques = cheques;

        return futuro(() -> repo.create(pago));
    }

    public Futuro<PagoAProveedoresOB> find(Integer id) {
        return futuro(() -> repo.find(id));
    }

    public Futuro<List<PagoAProveedoresOB>> buscarPorEstadosYEmpresa(EstadosPagosAProveedoresOB estado, EstadoBandejaOB estadoBandeja, EmpresaOB empresa) {
        return futuro(() -> repo.filtrarEstadoYBandeja(estado, estadoBandeja, empresa));
    }

    public Futuro<PagoAProveedoresOB> update(PagoAProveedoresOB pago) {
        pago.ultimaModificacion = LocalDateTime.now();
        pago.fechaUltActulizacion = LocalDateTime.now();
        return futuro(() -> repo.update(pago));
    }

    public Futuro<List<PagoAProveedoresOB>> filtrarMovimientosHistorial(EmpresaOB empresa, Fecha fechaDesde, Fecha fechaHasta, Integer convenio, Integer subconvenio, String adherente, EstadosPagosAProveedoresOB estado,boolean previsualizacion) {
        return futuro(() -> repo.filtrarMovimientosHistorial(empresa, fechaDesde, fechaHasta, convenio, subconvenio, adherente, estado,previsualizacion));
    }

    public Futuro<PagoAProveedoresOB> buscarPorNroLoteYEmpresa(EmpresaOB empresa, String nroLote) {
        return futuro(() -> repo.buscarPorNroLoteYEmpresa(empresa, nroLote));
    }
    
    public Futuro<List<PagoAProveedoresOB>> buscarPorEstado(EstadosPagosAProveedoresOB estado) {
        return futuro(() -> repo.buscarPorEstado(estado));
    }

    public Futuro<List<PagoAProveedoresOB>> buscarPorArchivo(String nombreArchivo){
        return futuro(()->repo.existeArchivo(nombreArchivo));
    }
}
