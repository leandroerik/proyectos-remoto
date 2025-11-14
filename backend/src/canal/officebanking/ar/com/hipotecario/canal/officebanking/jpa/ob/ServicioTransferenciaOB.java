package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.servicio.api.CVU.ApiCVU;
import ar.com.hipotecario.backend.servicio.api.catalogo.ApiCatalogo;
import ar.com.hipotecario.backend.servicio.api.catalogo.Bancos.Banco;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentaCoelsa;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.SesionOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoTRNOB;
import ar.com.hipotecario.canal.officebanking.jpa.dto.InfoCuentaDTO;
import ar.com.hipotecario.canal.officebanking.jpa.dto.PaginaTransferenciaDTO;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.TransferenciaOBRepositorio;

public class ServicioTransferenciaOB extends ServicioOB {

    private static TransferenciaOBRepositorio repo;

    public ServicioTransferenciaOB(ContextoOB contexto) {
        super(contexto);
        repo = new TransferenciaOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<TransferenciaOB> find(Integer codigo) {
        return futuro(() -> repo.find(codigo));
    }

    public Futuro<TransferenciaOB> update(TransferenciaOB transferenciaOB) {
        transferenciaOB.ultimaModificacion = LocalDateTime.now();
        return futuro(() -> repo.update(transferenciaOB));
    }

    public Futuro<List<TransferenciaOB>> buscarPorDosEstadosEntreFechaAplicacion(EmpresaOB empresa, EstadoTRNOB estado1, EstadoTRNOB estado2, Fecha fechaDesde, Fecha fechaHasta) {
        return futuro(() -> repo.buscarPorDosEstadosEntreFechaAplicacion(empresa, estado1, estado2, fechaDesde, fechaHasta));
    }

    public Futuro<List<TransferenciaOB>> buscarPorEstadoYFechaDeAplicacion(EstadoTRNOB estado, LocalDate fechaAplicacion) {
        return futuro(() -> repo.buscarPorEstadoYFechaDeAplicacion(estado, fechaAplicacion));
    }

    public Futuro<TransferenciaOB> enviarTransferencia(ContextoOB contexto, InfoCuentaDTO infoCredito, String cuentaDebito, String cbuDebito, String tipoCuentaDebito, Integer idMoneda, String cbuCredito, LocalDate fechaAplicacion, BigDecimal monto, Integer idConcepto, String referencia, String email, boolean cuentaSueldo, boolean validadoXRUC, boolean altaBeneficiario, EstadoTRNOB estado, TipoProductoFirmaOB tipoProductoFirmaOB, EstadoBandejaOB estadoBandejaOB, boolean esCVU) {
        SesionOB sesion = contexto.sesion();

        EmpresaOB empresaOB = sesion.empresaOB;

        // Debito
        TipoCuentaOB tipoCuentaD = tipoCuentaDebito.equals("CC") ? TipoCuentaOB.CC : TipoCuentaOB.CA;

        // Credito
        CuentaCoelsa cuentaCoelsa = infoCredito.cuenta;
        TipoCuentaOB tipoCuentaC = cuentaCoelsa.tipoCuenta.equals("CC") ? TipoCuentaOB.CC : TipoCuentaOB.CA;
        Long cuitCredito = Long.parseLong(cuentaCoelsa.cuit);

        LocalDateTime ahora = LocalDateTime.now();

        // Datos transferencia
        //int idTipoTransferencia = cuentaSueldo ? 2 : cuitCredito.equals(empresaOB.cuit) ? 1 : 63; // 1 propia 2 sueldos 3 proveedores 63 terceros
        int idTipoTransferencia = cuentaSueldo ? 2 : cuitCredito.equals(empresaOB.cuit) ? 1 : 3; // 1 propia 2 sueldos 3 proveedores 63 terceros
        ServicioTipoTransferenciaOB servicio = new ServicioTipoTransferenciaOB(contexto);
        TipoTransferenciaOB tipoTransferenciaOB = servicio.find(idTipoTransferencia).get();

        ServicioMonedaOB servicioMoneda = new ServicioMonedaOB(contexto);
        MonedaOB moneda = servicioMoneda.find(idMoneda).get();

        ServicioConceptoOB servicioConcepto = new ServicioConceptoOB(contexto);
        ConceptoOB concepto = servicioConcepto.find(idConcepto).get();

        TipoCamaraOB camara = null;
        ServicioTipoCamaraOB servicioCamara = new ServicioTipoCamaraOB(contexto);
        camara = servicioCamara.find(Integer.valueOf(infoCredito.cuenta.transaccion)).get();


        Banco datosBanco = ApiCatalogo.bancos(contexto, esCVU ? ApiCVU.getAliasCVU(contexto, cbuCredito).get().cuenta.nro_bco : cbuCredito.substring(0, 3)).get();
        ServicioBancoOB servicioBanco = new ServicioBancoOB(contexto);
        int idBanco = Integer.parseInt(datosBanco.codigo);
        BancoOB bancoGuardado = servicioBanco.find(idBanco).tryGet();

        SecuenciaOB secuencia = new SecuenciaOB();
        secuencia.descripcion = "TR";

        DebitoTranfOB debito = new DebitoTranfOB();
        debito.cbu = cbuDebito;
        debito.cuit = empresaOB.cuit.toString();
        debito.descripcion = empresaOB.razonSocial;
        debito.tipoCuenta = tipoCuentaD;
        debito.nroCuenta = cuentaDebito;
        debito.denominacion = null;

        CreditoTranfOB credito = new CreditoTranfOB();
        credito.cbu = cbuCredito;
        credito.email = email;
        credito.cuit = cuitCredito.toString();
        credito.descripcion = "A configurar";
        credito.tipoCuenta = tipoCuentaC;
        credito.nroCuenta = infoCredito.numero != null ? infoCredito.numero : "";
        credito.validadoXRUC = validadoXRUC;
        credito.comentario = referencia == null ? "" : referencia;
        credito.titular = cuentaCoelsa.nombreTitular.trim();

        if (empty(bancoGuardado)) {
            credito.banco = servicioBanco.create(new BancoOB(idBanco, datosBanco.Descripcion)).get();
        } else {
            credito.banco = bancoGuardado;
        }

        if (altaBeneficiario) {
            ServicioBeneficiarioOB servicioAB = new ServicioBeneficiarioOB(contexto);
            BeneficiarioOB beneficiario = servicioAB.findByCBU(empresaOB, cbuCredito).tryGet();
            if (empty(beneficiario)) {
                infoCredito.monedas = infoCredito.monedas.stream()
                        .collect(Collectors.toMap(m -> m.id, m -> m, (m1, m2) -> m1)).values().stream().toList();
                servicioAB.altaBeneficiario(contexto, infoCredito, email, referencia);
            }
        }


        TransferenciaOB transferencia = esCVU? new TransferenciaCredinOB() : new TransferenciaOB();
        transferencia.emp_codigo = empresaOB;
        transferencia.tipo = tipoTransferenciaOB;
        transferencia.concepto = concepto;
        transferencia.camara = camara;
        transferencia.fechaAplicacion = fechaAplicacion;
        transferencia.debitoConsolidado = false;
        transferencia.idDeCarga = secuencia;
        transferencia.usuario = sesion.usuarioOB;
        transferencia.fechaCreacion = ahora;
        transferencia.cuentaSueldo = cuentaSueldo;
        transferencia.ultimaModificacion = null;
        transferencia.usuarioModificacion = null;
        transferencia.debito = debito;
        transferencia.credito = credito;
        transferencia.estado = estado;

        transferencia.tipoProductoFirma = tipoProductoFirmaOB;
        transferencia.empresa = empresaOB;
        transferencia.estadoBandeja = estadoBandejaOB;
        transferencia.fechaUltActulizacion = LocalDateTime.now();
        transferencia.cuentaOrigen = debito.nroCuenta;
        transferencia.monto = monto;
        transferencia.moneda = moneda;
        return futuro(() -> repo.create(transferencia));


    }

    public Futuro<PaginaTransferenciaDTO> find(EmpresaOB empresaOB, int numeroPagina, int registrosPorPagina, String beneficiario, Integer idEstado, Fecha fechaDesde, Fecha fechaHasta) {
        return futuro(() -> repo.find(empresaOB, numeroPagina, registrosPorPagina, beneficiario, idEstado, fechaDesde, fechaHasta));
    }

    public Futuro<List<TransferenciaOB>> buscarSinFirmaPorVencer(ContextoOB contexto) {
        ServicioEstadoTRNOB servicioEstadoTRNOB = new ServicioEstadoTRNOB(contexto);
        int estadoEnBandeja = servicioEstadoTRNOB.find(EnumEstadoTRNOB.EN_BANDEJA.getCodigo()).get().id;
        LocalDate fechaHoy = LocalDate.now();
        return futuro(() -> repo.buscarSinFirmaPorVencer(estadoEnBandeja, fechaHoy));
    }

    public Futuro<List<TransferenciaOB>> yaSeTransfirioACBUOB(EmpresaOB empresa, String cbu) {
        return futuro(() -> repo.existsTransferenciaByEmpresaAndCbu(empresa, cbu));
    }

    public Futuro<List<TransferenciaOB>> findTransfersForTodayByEmpresaAndEstadoRechazado(int empCodigo) {
        return futuro(() -> repo.findTransfersForTodayByEmpresaAndEstadoRechazado(empCodigo));
    }
}