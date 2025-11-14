package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoDebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto.DebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto.EstadosDebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.PagoAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.DebitoDirectoOBRepositorio;

import java.math.BigDecimal;
import java.sql.Blob;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class ServicioDebitoDirectoOB extends ServicioOB {

    private static DebitoDirectoOBRepositorio repo;
    private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
    private static ServicioMonedaOB servicioMoneda = new ServicioMonedaOB(contexto);
    private static ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
    private static ServicioEstadosDebitoDirectoOB servicioEstadosDebitoDirecto = new ServicioEstadosDebitoDirectoOB(contexto);

    public ServicioDebitoDirectoOB(ContextoOB contexto) {
        super(contexto);
        repo = new DebitoDirectoOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<DebitoDirectoOB> crear(ContextoOB contexto, String cuentaOrigen, BigDecimal importe, String nombreArchivo, Blob archivo, Integer cantidadRegistros, Integer convenio, TipoProductoFirmaOB productoFirma, String gcr, String scr) {
    	DebitoDirectoOB debito = new DebitoDirectoOB();

    	debito.emp_codigo = contexto.sesion().empresaOB;
    	debito.ultimaModificacion = LocalDateTime.now();
    	debito.usuario = contexto.sesion().usuarioOB;
    	debito.nombreArchivo = nombreArchivo;
    	debito.tipoProducto = productoFirma.descripcion;
    	debito.cuentaOrigen = cuentaOrigen;
    	debito.convenio = convenio;
    	debito.fechaCreacion = LocalDateTime.now();
    	debito.empresa = contexto.sesion().empresaOB;
        debito.monto = importe;
        debito.moneda = servicioMoneda.find(80).get();
        debito.estado = servicioEstadosDebitoDirecto.find(EnumEstadoDebitoDirectoOB.EN_BANDEJA.getCodigo()).get();
        debito.estadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
        debito.tipoProductoFirma = productoFirma;
        debito.fechaCreacion = LocalDateTime.now();
        debito.ultimaModificacion = LocalDateTime.now();
        debito.usuarioModificacion = contexto.sesion().usuarioOB.cuil.toString();
        debito.fechaUltActulizacion = LocalDateTime.now();
        debito.archivo = archivo;
        debito.cantidadRegistros = cantidadRegistros;
        debito.gcr=gcr;
        debito.scr=scr;
        return futuro(() -> repo.create(debito));
    }

    public Futuro<DebitoDirectoOB> find(Integer id) {
        return futuro(() -> repo.find(id));
    }

    public Futuro<List<DebitoDirectoOB>> buscarPorEstadosYEmpresa(EstadosDebitoDirectoOB estado, EstadoBandejaOB estadoBandeja, EmpresaOB empresa) {
        return futuro(() -> repo.filtrarEstadoYBandeja(estado, estadoBandeja, empresa));
    }

    public Futuro<DebitoDirectoOB> update(DebitoDirectoOB debito) {
    	debito.ultimaModificacion = LocalDateTime.now();
    	debito.fechaUltActulizacion = LocalDateTime.now();
        return futuro(() -> repo.update(debito));
    }

    public Futuro<List<DebitoDirectoOB>> filtrarMovimientosHistorial(EmpresaOB empresa, Fecha fechaDesde, Fecha fechaHasta, Integer convenio, EstadosDebitoDirectoOB estado,boolean previsualizacion) {
        return futuro(() -> repo.filtrarMovimientosHistorial(empresa, fechaDesde, fechaHasta, convenio, estado,previsualizacion));
    }

    public Futuro<DebitoDirectoOB> buscarPorNroLoteYEmpresa(EmpresaOB empresa, String nroLote) {
        return futuro(() -> repo.buscarPorNroLoteYEmpresa(empresa, nroLote));
    }
    
    public Futuro<List<DebitoDirectoOB>> buscarPorEstado(EstadosDebitoDirectoOB estado) {
        return futuro(() -> repo.buscarPorEstado(estado));
    }
    public Futuro<List<DebitoDirectoOB>> buscarPorArchivo(String nombreArchivo){
        return futuro(()->repo.existeArchivo(nombreArchivo));
    }

    public Futuro<List<DebitoDirectoOB>> buscarPorFechaCreacion(LocalDate fechaCreacion){
        return futuro(()->repo.buscarPorFechaCreacion(fechaCreacion));
    }
}
