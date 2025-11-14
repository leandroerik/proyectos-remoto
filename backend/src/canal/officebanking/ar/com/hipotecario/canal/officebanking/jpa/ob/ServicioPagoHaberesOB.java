package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoPagosHaberesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.EstadoPagosHaberesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.PagoDeHaberesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.PagoDeHaberesOBRepositorio;

import java.math.BigDecimal;
import java.sql.Blob;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ServicioPagoHaberesOB extends ServicioOB {
    private static PagoDeHaberesOBRepositorio repo;

    private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
    private static ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
    private static ServicioEstadoPagosHaberesOB servicioEstadoPagos = new ServicioEstadoPagosHaberesOB(contexto);
    private static ServicioMonedaOB servicioMoneda = new ServicioMonedaOB(contexto);

    public ServicioPagoHaberesOB(ContextoOB contexto) {
        super(contexto);
        repo = new PagoDeHaberesOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<PagoDeHaberesOB> crear(ContextoOB contexto, String cuentaOrigen, BigDecimal importe, String nombreArchivo, Blob archivo, Integer cantidadRegistros, Integer convenio, TipoProductoFirmaOB productoFirma, LocalDate fechaArchivo, Boolean fcl) {
        PagoDeHaberesOB pago = new PagoDeHaberesOB();

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

        if (productoFirma.descripcion.equalsIgnoreCase("PLAN SUELDO")) {
            pago.estado = servicioEstadoPagos.find(EnumEstadoPagosHaberesOB.EN_BANDEJA.getCodigo()).get();
            pago.estadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
        } else if (productoFirma.descripcion.equalsIgnoreCase("NOMINA")) {
            pago.estadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()).get();
            pago.estado = servicioEstadoPagos.find(EnumEstadoPagosHaberesOB.A_PROCESAR.getCodigo()).get();
        }

        pago.tipoProductoFirma = productoFirma;
        pago.fechaCreacion = LocalDateTime.now();
        pago.ultimaModificacion = LocalDateTime.now();
        pago.fechaUltActulizacion = LocalDateTime.now();
        pago.usuarioModificacion = contexto.sesion().usuarioOB.cuil.toString();
        pago.archivo = archivo;
        pago.cantidadRegistros = cantidadRegistros;
        pago.fechaCargaArchivo = fechaArchivo;
        pago.fcl = fcl;
        return futuro(() -> repo.create(pago));
    }

    public Futuro<PagoDeHaberesOB> find(Integer id) {
        return futuro(() -> repo.find(id));
    }

    public Futuro<PagoDeHaberesOB> update(PagoDeHaberesOB pago) {
        pago.ultimaModificacion = LocalDateTime.now();
        pago.fechaUltActulizacion = LocalDateTime.now();
        return futuro(() -> repo.update(pago));
    }

    public Futuro<List<PagoDeHaberesOB>> filtrarMovimientosHistorial(EmpresaOB empresa, Fecha fechaDesde, Fecha fechaHasta, Integer convenio, String producto, EstadoPagosHaberesOB estado) {
        return futuro(() -> repo.filtrarMovimientosHistorial(empresa, fechaDesde, fechaHasta, convenio, producto, estado));
    }

    public Futuro<List<PagoDeHaberesOB>> buscarAcreditacionesSinFirmaAFechaArchivo() {
        EstadoPagosHaberesOB estadoEnBandeja = servicioEstadoPagos.find(EnumEstadoPagosHaberesOB.EN_BANDEJA.getCodigo()).get();
        return futuro(() -> repo.buscarAcreditacionesSinFirmaAFechaArchivo(estadoEnBandeja));
    }

    public Futuro<List<PagoDeHaberesOB>> buscarArchivo(String archivo, Integer emp_codigo) {
        return futuro(() -> repo.buscarArchivo(archivo, emp_codigo));
    }
    public Futuro<List<PagoDeHaberesOB>> buscarArchivoContains(String archivo, Integer emp_codigo) {
        return futuro(() -> repo.buscarArchivoContains(archivo, emp_codigo));
    }

    public Futuro<PagoDeHaberesOB> buscarByNombre(String nombreArchivo) {
        return futuro(() -> repo.buscarByNombre(nombreArchivo));
    }

}
