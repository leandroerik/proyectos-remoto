package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;
import ar.com.hipotecario.canal.officebanking.enums.echeq.EnumAccionesEcheqOB;
import ar.com.hipotecario.canal.officebanking.enums.echeq.EnumEstadoEcheqChequeraOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EstadoEcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.ECheqOBRepositorio;

public class ServicioEcheqOB extends ServicioOB {
    private static ECheqOBRepositorio repo;
    private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
    private static ServicioEstadoEcheqOB servicioEstadoEcheqOB = new ServicioEstadoEcheqOB(contexto);
    private static ServicioEstadoBandejaOB servicioEstadoBandejaOB = new ServicioEstadoBandejaOB(contexto);
    private static ServicioMonedaOB servicioMonedaOB = new ServicioMonedaOB(contexto);
    private static ServicioTipoProductoFirmaOB servicioTipoProductoFirmaOB = new ServicioTipoProductoFirmaOB(contexto);

    public ServicioEcheqOB(ContextoOB contexto) {
        super(contexto);
        repo = new ECheqOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<EcheqOB> crear(String numeroChequera, BigDecimal monto, String documentoBeneficiario, String tipoDocumentoBeneficiario, String emailBeneficiario, boolean aLaOrden, String motivo, String concepto, boolean cruzado, String cuentaBanco, String beneficiarioRazonSocial, EmpresaOB empresa, LocalDateTime fechaPago, String tipo, EnumAccionesEcheqOB accion, String documentoEmisor, UsuarioOB usuario) {
        EcheqOB echeq = new EcheqOB();
        echeq.numeroChequera = numeroChequera;
        echeq.monto = monto;
        echeq.version = 001;
        echeq.documentoBeneficiario = documentoBeneficiario;
        echeq.tipoDocumentoBeneficiario = tipoDocumentoBeneficiario;
        echeq.documentoEmisor = documentoEmisor;
        echeq.tipoDocumentoEmisor = "cuit";
        echeq.emailBeneficiario = emailBeneficiario;
        echeq.aLaOrden = aLaOrden;
        echeq.motivoPago = motivo;
        echeq.concepto = concepto;
        echeq.cruzado = cruzado;
        echeq.cuentaOrigen = cuentaBanco;
        echeq.moneda = servicioMonedaOB.find(80).get();
        echeq.fechaUltActulizacion = LocalDateTime.now();
        echeq.tipoProductoFirma = servicioTipoProductoFirmaOB.findByCodigo(EnumTipoProductoOB.ECHEQ.getCodigo()).get();
        echeq.razonSocialBeneficiario = beneficiarioRazonSocial;
        echeq.empresa = empresa;
        echeq.estado = servicioEstadoEcheqOB.find(EnumEstadoEcheqChequeraOB.EN_BANDEJA.getCodigo()).get();
        echeq.estadoBandeja = servicioEstadoBandejaOB.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
        echeq.fechaPago = fechaPago;
        echeq.tipo = tipo;
        echeq.accion = accion;
        echeq.usuario = usuario;

        return futuro(() -> repo.create(echeq));
    }

    public Futuro<EcheqOB> cargarCreado(String numeroChequera, BigDecimal monto, String documentoBeneficiario, String tipoDocumentoBeneficiario, String emailBeneficiario, boolean aLaOrden, String motivo, String concepto, boolean cruzado, String cuentaBanco, String beneficiarioRazonSocial, EmpresaOB empresa, LocalDateTime fechaPago, String tipo, EnumAccionesEcheqOB accion, String documentoEmisor, String idCheque, String numeroCheque, String tipoEndoso, String cesionarioNombre, String cesionarioDomicilio, String cuentaDeposito,UsuarioOB usuario) {
        EcheqOB echeq = new EcheqOB();
        echeq.numeroChequera = numeroChequera;
        echeq.monto = monto;
        echeq.version = 001;
        echeq.documentoBeneficiario = documentoBeneficiario;
        echeq.tipoDocumentoBeneficiario = tipoDocumentoBeneficiario;
        echeq.documentoEmisor = documentoEmisor;
        echeq.tipoDocumentoEmisor = "cuit";
        echeq.emailBeneficiario = emailBeneficiario;
        echeq.aLaOrden = aLaOrden;
        echeq.motivoPago = motivo;
        echeq.concepto = concepto;
        echeq.cruzado = cruzado;
        echeq.cuentaOrigen = cuentaDeposito == null?"":cuentaDeposito;
        echeq.moneda = servicioMonedaOB.find(80).get();
        echeq.fechaUltActulizacion = LocalDateTime.now();
        echeq.tipoProductoFirma = servicioTipoProductoFirmaOB.findByCodigo(EnumTipoProductoOB.ECHEQ.getCodigo()).get();
        echeq.razonSocialBeneficiario = beneficiarioRazonSocial;
        echeq.empresa = empresa;
        echeq.estado = servicioEstadoEcheqOB.find(EnumEstadoEcheqChequeraOB.EN_BANDEJA.getCodigo()).get();
        echeq.estadoBandeja = servicioEstadoBandejaOB.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
        echeq.fechaPago = fechaPago;
        echeq.tipo = tipo;
        echeq.accion = accion;
        echeq.idCheque = idCheque;
        echeq.numeroCheque = numeroCheque;
        echeq.tipoEndoso = tipoEndoso;
        echeq.cesionarioNombre = cesionarioNombre;
        echeq.cesionarioDomicilio = cesionarioDomicilio;
        echeq.cuentaDeposito = cuentaDeposito;
        echeq.usuario = usuario;

        return futuro(() -> repo.create(echeq));
    }

    public static Futuro<EcheqOB> find(int id) {
        return futuro(() -> repo.find(id));
    }

    public Futuro<EcheqOB> update(EcheqOB echeq) {
        echeq.fechaUltActulizacion = LocalDateTime.now();
        return futuro(() -> repo.update(echeq));
    }

    public Futuro<EcheqOB> findByField(String field, String valor, String accion, String documentoConsultante) {
        return futuro(() -> {
            List<EcheqOB> cheques = repo.findByField(field, valor);
            if (cheques.isEmpty()) {
                return null;
            } else {
                Optional<EcheqOB> cheq = null;
                switch (accion.toUpperCase()) {
                    case "EMITIDOS" ->
                            cheq = cheques.stream().filter(cheque -> cheque.accion.equals(EnumAccionesEcheqOB.EMISION)).findFirst();
                    case "RECIBIDOS" ->
                            cheq = cheques.stream().filter(cheque -> cheque.documentoBeneficiario.equals(documentoConsultante)).findFirst();
                    case "ENDOSADOS" ->
                            cheq = cheques.stream().filter(cheque -> cheque.accion.equals(EnumAccionesEcheqOB.ENDOSO) && cheque.documentoEmisor.equals(documentoConsultante)).findFirst();
                    case "CEDIDOS" ->
                            cheq = cheques.stream().filter(cheque -> cheque.accion.equals(EnumAccionesEcheqOB.CESION) && cheque.documentoEmisor.equals(documentoConsultante)).findFirst();
                }
                return cheq.isEmpty() ? null : cheq.get();
            }

        });
    }

    public Futuro<List<EcheqOB>> buscarPorEstado(EstadoEcheqOB estado){
        return futuro(()->repo.buscarPorEstado(estado));
    }

    public Futuro<EcheqOB> findById(int idCheque){
        return futuro(()-> repo.find(idCheque));
    }

    public Futuro<EcheqOB> findByFieldAndDocumento(String field, String valor, String documentoConsultante) {
        return futuro(() -> {
            List<EcheqOB> cheques = repo.findByField(field, valor);
            if (cheques.isEmpty()) {
                return null;
            } else {
                Optional<EcheqOB> cheq = null;
                cheq = cheques.stream().filter(cheque -> cheque.documentoBeneficiario.equals(documentoConsultante)).findFirst();
                return cheq == null ? null : cheq.get();
            }

        });
    }
    public Futuro<List<EcheqOB>> findByFieldIn(List<String> valores){
        return futuro(()->repo.findByFieldIn(valores));
    }
    public Futuro<List<EcheqOB>> findChequeByEmisorAndIdAndEstado(String emisorDocumento){
        List<Integer> estados = Arrays.asList(3, 5);
        return futuro(()->repo.findChequeByEmisorAndIdAndEstado(emisorDocumento,estados));
    }

}
