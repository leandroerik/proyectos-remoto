package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.servicio.api.debin.Debin;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.CobranzaIntegralOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.DebinLoteOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.EstadosDebinLoteOB;

import java.math.BigDecimal;
import java.sql.Blob;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ServicioDebinLoteOB extends ServicioOB {
    private static DebinLoteOBRepositorio repo;
    private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
    private static ServicioMonedaOB servicioMoneda = new ServicioMonedaOB(contexto);

    private  static ServicioEstadosDebinPorLote servicioEstadosDebinPorLote= new ServicioEstadosDebinPorLote(contexto);
    private static ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);

    public ServicioDebinLoteOB(ContextoOB contexto) {
        super(contexto);
        repo=new DebinLoteOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }
    
    public Futuro<DebinLoteOB> find(Integer codigo) {
		return futuro(() -> repo.find(codigo));
	}

    public Futuro<DebinLoteOB> crear(ContextoOB contexto, String cuentaOrigen, String nombreArchivo, Blob archivo, Integer cantidadRegistros, String importe, Integer convenio, TipoProductoFirmaOB productoFirma,String gcr) {
        DebinLoteOB debinPorLote = new DebinLoteOB();

        debinPorLote.emp_codigo = contexto.sesion().empresaOB;
        debinPorLote.ultimaModificacion = LocalDateTime.now();
        debinPorLote.usuario = contexto.sesion().usuarioOB;
        debinPorLote.nombreArchivo = nombreArchivo;
        debinPorLote.tipoProducto = productoFirma.descripcion;
        debinPorLote.cuentaOrigen = cuentaOrigen;
        debinPorLote.convenio = convenio;
        debinPorLote.fechaCreacion = LocalDateTime.now();
        debinPorLote.empresa = contexto.sesion().empresaOB;
        debinPorLote.monto = BigDecimal.valueOf(Long.parseLong(importe));
        debinPorLote.moneda = servicioMoneda.find(80).get();
        debinPorLote.estado = servicioEstadosDebinPorLote.find(EnumEstadosDebinLoteOB.EN_BANDEJA.getCodigo()).get();
        debinPorLote.estadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
        debinPorLote.tipoProductoFirma = productoFirma;
        debinPorLote.fechaCreacion = LocalDateTime.now();
        debinPorLote.ultimaModificacion = LocalDateTime.now();
        debinPorLote.usuarioModificacion = contexto.sesion().usuarioOB.cuil.toString();
        debinPorLote.fechaUltActulizacion = LocalDateTime.now();
        debinPorLote.archivo = archivo;
        debinPorLote.cantidadRegistros = cantidadRegistros;
        debinPorLote.gcr = gcr;

        return futuro(() -> repo.create(debinPorLote));
    }

    public Futuro<DebinLoteOB> update(DebinLoteOB debinLoteOB) {
        debinLoteOB.ultimaModificacion = LocalDateTime.now();
        debinLoteOB.fechaUltActulizacion = LocalDateTime.now();
        return futuro(() -> repo.update(debinLoteOB));
    }

    public Futuro<List<DebinLoteOB>> filtrarMovimientosHistorial(EmpresaOB empresa, Fecha fechaDesde, Fecha fechaHasta, Integer convenio, EstadosDebinLoteOB estado){
        return futuro(()->repo.filtrarMovimientosHistorial(empresa,fechaDesde,fechaHasta,convenio,estado));
    }

    public Futuro<List<DebinLoteOB>> buscarPorArchivo(String nombreArchivo) {
        return futuro(()->repo.existeArchivo(nombreArchivo));
    }

    public Futuro<List<DebinLoteOB>> buscarPorFechaCreacion(LocalDate fechaCreacion){
        return futuro(()->repo.buscarPorFechaCreacion(fechaCreacion));
    }
}
