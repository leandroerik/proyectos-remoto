package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.CobranzaIntegralOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.EstadosCobranzaIntegralOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto.DebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.PagoDeHaberesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.CobranzaIntegralOBRepositorio;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EnumEstadosCobranzaIntegralOB;

import java.math.BigDecimal;
import java.sql.Blob;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ServicioCobranzaIntegralOB extends ServicioOB{
    private static CobranzaIntegralOBRepositorio repo;
    private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
    private static ServicioMonedaOB servicioMoneda = new ServicioMonedaOB(contexto);

    private  static ServicioEstadosCobranzaIntegral servicioEstadosCobranzaIntegral = new ServicioEstadosCobranzaIntegral(contexto);
    private static ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
    public ServicioCobranzaIntegralOB(ContextoOB contexto) {
        super(contexto);
        repo = new CobranzaIntegralOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<CobranzaIntegralOB> crear(ContextoOB contexto, String cuentaOrigen, String nombreArchivo, Blob archivo, Integer cantidadRegistros, String importe, Integer convenio, TipoProductoFirmaOB productoFirma,String gcr) {
        CobranzaIntegralOB cobranza = new CobranzaIntegralOB();

        cobranza.emp_codigo = contexto.sesion().empresaOB;
        cobranza.ultimaModificacion = LocalDateTime.now();
        cobranza.usuario = contexto.sesion().usuarioOB;
        cobranza.nombreArchivo = nombreArchivo;
        cobranza.tipoProducto = productoFirma.descripcion;
        cobranza.cuentaOrigen = cuentaOrigen;
        cobranza.convenio = convenio;
        cobranza.fechaCreacion = LocalDateTime.now();
        cobranza.empresa = contexto.sesion().empresaOB;
        cobranza.monto = BigDecimal.valueOf(Long.parseLong(importe));
        cobranza.moneda = servicioMoneda.find(80).get();
        cobranza.estado = servicioEstadosCobranzaIntegral.find(EnumEstadosCobranzaIntegralOB.EN_BANDEJA.getCodigo()).get();
        cobranza.estadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
        cobranza.tipoProductoFirma = productoFirma;
        cobranza.fechaCreacion = LocalDateTime.now();
        cobranza.ultimaModificacion = LocalDateTime.now();
        cobranza.usuarioModificacion = contexto.sesion().usuarioOB.cuil.toString();
        cobranza.fechaUltActulizacion = LocalDateTime.now();
        cobranza.archivo = archivo;
        cobranza.cantidadRegistros = cantidadRegistros;
        cobranza.gcr = gcr;


        return futuro(() -> repo.create(cobranza));
    }

    public Futuro<CobranzaIntegralOB> find(Integer id){
        return futuro(() -> repo.find(id));
    }

    public Futuro<CobranzaIntegralOB> update(CobranzaIntegralOB cobranza) {
        cobranza.ultimaModificacion = LocalDateTime.now();
        cobranza.fechaUltActulizacion = LocalDateTime.now();
        return futuro(() -> repo.update(cobranza));
    }
    public Futuro<List<CobranzaIntegralOB>> filtrarMovimientosHistorial(EmpresaOB empresa, Fecha fechaDesde, Fecha fechaHasta, Integer convenio, EstadosCobranzaIntegralOB estado) {
        return futuro(() -> repo.filtrarMovimientosHistorial(empresa, fechaDesde, fechaHasta, convenio, estado));
    }
    public Futuro<List<CobranzaIntegralOB>> buscarPorEstado(EstadosCobranzaIntegralOB estado) {
        return futuro(() -> repo.buscarPorEstado(estado));
    }

    public Futuro<List<CobranzaIntegralOB>> buscarPorArchivo(String nombreArchivo) {
        return futuro(()->repo.existeArchivo(nombreArchivo));
    }
    public Futuro<List<CobranzaIntegralOB>> buscarPorFechaHora(LocalDateTime inicioDia,LocalDateTime finDia,Integer emp_Codigo) {
        return futuro(() -> repo.buscarPorFechaHora(inicioDia, finDia, emp_Codigo));
    }

    public Futuro<List<CobranzaIntegralOB>> buscarPorFechaCreacion(LocalDate fechaCreacion){
        return futuro(()->repo.buscarPorFechaCreacion(fechaCreacion));
    }
}
