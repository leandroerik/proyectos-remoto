package ar.com.hipotecario.backend.servicio.api.cheques;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.servicio.api.cheques.ListadoChequesOB.cheques;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EcheqOB;


public class ApiCheques extends Api {

    public static Futuro<DetalleRazonSocialOB> razonSocial(ContextoOB contexto, String cuit){
        return futuro(()->DetalleRazonSocialOB.get(contexto,cuit));
    }

    public static Futuro<DetalleChequeraOB> chequerasDisponibles(ContextoOB contexto,String nroCuenta,String operacion){
        return futuro(()->DetalleChequeraOB.get(contexto,nroCuenta,operacion));
    }
    public static Futuro<DetalleChequeraActivaOB> chequerasActivas(ContextoOB contexto,String nroCuenta,String operacion){
        return futuro(()->DetalleChequeraActivaOB.get(contexto,nroCuenta,operacion));
    }

    public static Futuro<DetalleAltaChequeraOB.DetalleAltaChequera> altaChequera(ContextoOB contexto, String cuentaBanco, String tipoChequera){
        return futuro(()->DetalleAltaChequeraOB.post(contexto,cuentaBanco,tipoChequera));
    }

    public static Futuro<DetalleEmisionEcheq> altaEcheq(ContextoOB contexto, EcheqOB echeq,String cbuEmisor,String numeroSucursal,String cpSucursal){
        return futuro(()->DetalleEmisionEcheq.post(contexto,echeq,cbuEmisor,numeroSucursal,cpSucursal));
    }

    public static Futuro<ListadoChequesOB> listadoCheques(ContextoOB contexto,String cuit,String cantRegistros,String filter,String pagina){
        return futuro(()->ListadoChequesOB.get(contexto,cuit,cantRegistros,filter,pagina));
    }
    
    public static Futuro<ListadoChequesOB> getChequeById(ContextoOB contexto, String id,String cuit){
        return futuro(()-> ListadoChequesOB.getById(contexto,id,cuit));
    }
    public static Futuro<ListadoChequesOB> getChequeByIdMandatario(ContextoOB contexto, String id,String cuit){
        return futuro(()-> ListadoChequesOB.getByIdMandatario(contexto,id,cuit));
    }

    public static Futuro<ListadoChequesADescontarOB> listadoChequesADescontar(ContextoOB contexto, String pagina, Integer moneda, String ordenamiento, Integer totalElementos, String fechaDesde, String fechaHasta){
    	return futuro(()->ListadoChequesADescontarOB.get(contexto, pagina, moneda, ordenamiento, totalElementos, fechaDesde, fechaHasta));
    }
    
    public static Futuro<SimularDescuentoOB> simularDescuento(ContextoOB contexto, String cuenta, String[] chequesID, String sucursal){
    	return futuro(()->SimularDescuentoOB.post(contexto,cuenta, chequesID, sucursal));
    }
    
    public static Futuro<DescontarChequeOB> descontarChequeFactoring(ContextoOB contexto, String modificarEstadoCodigo, String solicitudNumero){
    	return futuro(()->DescontarChequeOB.post(contexto, modificarEstadoCodigo, solicitudNumero));
    }
    
    public static Futuro<AdmitirEcheqOB> admitirEcheqOb(ContextoOB contexto, String idCheque, String documentoBeneficiario,String tipoDocumentoBeneficiario){
        return futuro(()->AdmitirEcheqOB.post(contexto,idCheque,documentoBeneficiario,tipoDocumentoBeneficiario));
    }

    public static Futuro<RechazarEcheqOB> rechazarEcheqOB(ContextoOB contexto, String idCheque, String documentoBeneficiario,String tipoDocumentoBeneficiario,String motivoRepudio){
        return futuro(()->RechazarEcheqOB.post(contexto,idCheque,documentoBeneficiario,tipoDocumentoBeneficiario,motivoRepudio));
    }

    public static Futuro<AnularEcheqOB> anularEcheqOB(ContextoOB contextoOB,String idCheque,String documentoEmisor,String motivoAnulacion){
        return futuro(()->AnularEcheqOB.post(contextoOB,idCheque,documentoEmisor,motivoAnulacion));
    }

    public static Futuro<DevolucionEcheqOB> solicitarDevolucionEcheqOB(ContextoOB contexto, String idCheque,String documentoEmisor,String motivoDevolucion,String mailBeneficiario){
        return futuro(()->DevolucionEcheqOB.solicitarDevolucion(contexto,idCheque,documentoEmisor,motivoDevolucion,mailBeneficiario));
    }

    public static Futuro<DevolucionEcheqOB> aceptarDevolucionEcheqOB(ContextoOB contexto, String idCheque, String documentoBeneficiario, String tipoDocumentoBeneficiario){
        return futuro(()->DevolucionEcheqOB.aceptarDevolucion(contexto, idCheque,documentoBeneficiario,tipoDocumentoBeneficiario));
    }

    public static Futuro<DevolucionEcheqOB> rechazarDevolucionEcheqOB(ContextoOB contexto, String idCheque, String documentoBeneficiario, String tipoDocumentoBeneficiario){
        return futuro(()->DevolucionEcheqOB.rechazarDevolucion(contexto, idCheque,documentoBeneficiario,tipoDocumentoBeneficiario));
    }

    public static Futuro<DevolucionEcheqOB> anularDevolucionEcheqOB(ContextoOB contexto, String idCheque,String documentoEmisor,String motivoDevolucion){
        return futuro(()->DevolucionEcheqOB.anularDevolucion(contexto,idCheque,documentoEmisor,motivoDevolucion));
    }

    public static Futuro<EndosoEcheqOB> endosarEcheq(ContextoOB contexto, String idCheque, String tipoDocumentoEmisor, String documentoEmisor, String tipoDocumentoBeneficiario, String documentoBeneficiario, String tipoEndoso,String emailBeneficiario){
        return futuro(()->EndosoEcheqOB.endosarEcheq(contexto,idCheque,tipoDocumentoEmisor,documentoEmisor,tipoDocumentoBeneficiario,documentoBeneficiario,tipoEndoso,emailBeneficiario));
    }
    public static Futuro<EntidadesMercadoOB> entidadesMercado(ContextoOB contexto){
        return futuro(()->EntidadesMercadoOB.get(contexto));
    }
    public static Futuro<AnularEndosoEcheqOB> anularEndosoEcheq(ContextoOB contexto,String idCheque, String endosanteDocumento,String motivoAnulacion){
        return futuro(()->AnularEndosoEcheqOB.post(contexto,idCheque,endosanteDocumento,motivoAnulacion));
    }
    public static Futuro<EmitirCesionEcheqOB> emitirCesionEcheq(ContextoOB contexto,String idCheque,String cedenteDocumento,String cesionarioDocumento,String cesionarioDocumentoTipo,String cesionarioDomicilio,String cesionarioNombre){
        return futuro(()->EmitirCesionEcheqOB.post(contexto, idCheque, cedenteDocumento, cesionarioDocumento, cesionarioDocumentoTipo, cesionarioDomicilio, cesionarioNombre));
    }
    public static Futuro<AnularCesionEcheqOB> anularCesionEcheq(ContextoOB contexto,String idCheque,String cedenteDocumento,String cesionId){
        return futuro(()->AnularCesionEcheqOB.post(contexto, idCheque, cedenteDocumento, cesionId));
    }
    public static Futuro<AdmitirRepudiarCesionOB> admitirCesionEcheq(ContextoOB contexto, String idCheque, String cesionarioDocumento, String cesionarioDocumentoTipo, String cesionId){
        return futuro(()-> AdmitirRepudiarCesionOB.admitir(contexto, idCheque, cesionarioDocumento, cesionarioDocumentoTipo, cesionId));
    }
    public static Futuro<AdmitirRepudiarCesionOB> repudiarCesionEcheq(ContextoOB contexto, String idCheque, String cesionarioDocumento, String cesionarioDocumentoTipo, String cesionId,String motivoRepudio){
        return futuro(()-> AdmitirRepudiarCesionOB.repudiar(contexto, idCheque, cesionarioDocumento, cesionarioDocumentoTipo, cesionId,motivoRepudio));
    }

    public static Futuro<DepositarEcheqOB> depositoCheque(ContextoOB contexto,String oficinaDeposito, String codigoVisualizacion, String codigoBancoDeposito, String codigoPostal, String cuentaGirada, String importe, String tipo, String fechaPago, String cmc7, String numeroCheque, String modo, String motivo, String concepto, String caracter, String moneda, String fechaPresentacion, String cbu,String cuentaDeposito,String idCheque, int producto) {
        return futuro(()->DepositarEcheqOB.depositarEcheq(contexto, Integer.parseInt(oficinaDeposito),cuentaDeposito,producto,numeroCheque,idCheque,codigoVisualizacion,codigoBancoDeposito,codigoPostal,cuentaGirada,Double.valueOf(importe),tipo,fechaPago,cmc7,modo,motivo,concepto,caracter,Integer.valueOf(moneda),fechaPresentacion,cbu));
    }
    public static Futuro<CaducarEcheqOB> caducarCheque(ContextoOB contexto, String idCheque){
        return futuro(()->CaducarEcheqOB.post(contexto, idCheque));
    }

    public static Futuro<CuentaEcheqOB> consultaCuentaEcheq(ContextoOB contexto,String cuit){
        return futuro(()->CuentaEcheqOB.get(contexto,cuit));
    }

    public static Futuro<AltaCuentaEcheqOB> altaCuentaEcheq(ContextoOB contexto, String sucursalCodigo,String sucursalNombre,String sucursalDomicilio,String sucursalCP,String sucursalProvincia,String emisorCuit,String emisorRazonSocial,String emisorCBU,String emisorCuenta,String emisorMoneda,String emisorDomicilio,String emisorCP){
        return futuro(()->AltaCuentaEcheqOB.post(contexto,sucursalCodigo,sucursalNombre,sucursalDomicilio,sucursalCP,sucursalProvincia,emisorCuit,emisorRazonSocial,emisorCBU,emisorCuenta,emisorMoneda,emisorDomicilio,emisorCP));
    }
    
    public static Futuro<CustodiarEcheqOB> custodiarCheque(ContextoOB contexto, cheques cheque, String cuentaDeposito, String cbuDeposito, String emailBeneficiario, String sucursal) {
        return futuro(()->CustodiarEcheqOB.custodiarEcheq(contexto, cheque, cuentaDeposito, cbuDeposito, emailBeneficiario, sucursal));
    }
    
    public static Futuro<RescatarEcheqOB> rescatarCheque(ContextoOB contexto, cheques cheque, String cuentaDeposito, String cbuDeposito) {
        return futuro(()->RescatarEcheqOB.rescatarEcheq(contexto, cheque, cuentaDeposito, cbuDeposito));
    }

    public static Futuro<AvalEcheqOB> avalarCheque(ContextoOB contexto, String avalDocumento,String avalDomicilio,String avalNombre,ListadoChequesOB.cheques cheque){
        return futuro(()->AvalEcheqOB.avalarEcheq(contexto,avalDocumento,avalDomicilio,avalNombre,cheque));
    }

    public static Futuro<AnularAvalEcheqOB> anularAvalCheque(ContextoOB contexto, String idCheque, String avalistaDocumento,String tenedorDocumento){
        return futuro(()->AnularAvalEcheqOB.post(contexto,idCheque,avalistaDocumento,tenedorDocumento));
    }

    public static Futuro<AdmitirAvalEcheqOB> admitirAvalEcheqOb(ContextoOB contexto, String idCheque, String cuit) {
        return futuro(()->AdmitirAvalEcheqOB.post(contexto,idCheque,cuit));
    }

    public static Futuro<RepudioAvalEcheqOB> repudiarAvalEcheq(ContextoOB contexto, String idCheque, String cuit, String motivoRepudio) {
        return futuro(()->RepudioAvalEcheqOB.repudiar(contexto,idCheque,cuit,motivoRepudio));
    }
    public static Futuro<MandatoNegociacionOB> mandatoNegociacion(ContextoOB contexto,String idCheque,String documentoMandatario,String domicilioMandante){
        return futuro(()->MandatoNegociacionOB.mandatoNegociacion(contexto,idCheque,documentoMandatario,domicilioMandante));
    }

    public static Futuro<MandatoNegociacionOB> aceptarMandatoNegociacionOB(ContextoOB contexto, String idCheque, String cuit) {
        return futuro(()->MandatoNegociacionOB.admitirMandatoNegociacion(contexto,idCheque,cuit));
    }

    public static Futuro<MandatoNegociacionOB> repudiarMandatoNegociacion(ContextoOB contexto, String idCheque, String motivoRepudio) {
        return futuro(()->MandatoNegociacionOB.repudiarMandatoNegociacion(contexto,idCheque,motivoRepudio));
    }

    public static Futuro<MandatoNegociacionOB> anularMandatoNegociacion(ContextoOB contexto, String chequeId) {
        return futuro(()->MandatoNegociacionOB.anularMandatoNegociacion(contexto,chequeId));
    }

    public static Futuro<MandatoNegociacionOB> revocarMandatoNegociacion(ContextoOB contexto, String idCheque, String cuit, String motivoRevocatoria) {
        return futuro(()->MandatoNegociacionOB.revocarMandatoNegociacion(contexto,idCheque,cuit,motivoRevocatoria));
    }
}
