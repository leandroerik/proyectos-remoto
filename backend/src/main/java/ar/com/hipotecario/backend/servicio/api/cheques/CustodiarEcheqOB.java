package ar.com.hipotecario.backend.servicio.api.cheques;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cheques.ListadoChequesOB.cheques;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.OBEcheq;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.utils.firmante;

public class CustodiarEcheqOB extends ApiObjetos<CustodiarEcheqOB.result> {
        public result result;
    class result extends ApiObjeto {
        private List<Respuesta> respuesta;
    }

    class Respuesta extends ApiObjeto {
        private String cheque_id;
        private RespuestaEcheqBody respuesta;
    }

    class RespuestaEcheqBody extends ApiObjeto {
        private String codigo;
        private String descripcion;
    }

    private static class AutorizacionParametro extends ApiObjeto {
        Integer modulo;
        Integer transaccion;
        String usuario;
    }
    
    private static class Cliente extends ApiObjeto {
        String cbu;
        String clienteCodigo;
        String clienteRazonSocial;
        String cuenta;
        String cuentaMail;
        String ivaPosicionCodigo;
        String numeroDocumento;
        String personaCodigo;
        String sectorCodigo;
        Integer tipoDocumento;
        String bancaCodigo;
    }
    
    private static class EcheqBeneficiarioActual extends ApiObjeto {
        String beneficiarioCuentaMail;
        String beneficiarioNombre;
        String numeroDocumento;
        Integer tipoDocumento;
    }
    
    private static class EcheqBeneficiarioOriginal extends ApiObjeto {
    	String beneficiarioCuentaMail;
        String beneficiarioNombre;
        String numeroDocumento;
        Integer tipoDocumento;
    }
    
    private static class EndosoCustodia extends ApiObjeto {
    	String beneficiarioDocumento;
    	Integer beneficiarioTipoDocument;
        String endosoEstado;
        String fechaHora;
        String endosoTipo;
    }
    
    private static class LibradorCuentaElectronica extends ApiObjeto {
        String cuentaMail;
    }
    
    private static class Autorizante extends ApiObjeto {
        String autorizanteNombre;
        String numeroDocumento;
        Integer tipoDocumento;
    }
    
    private static class Parametros extends ApiObjeto {
        List<Autorizante> autorizantes;
        Integer moneda;
        Integer producto;
        Integer sucursal;
    }
    
    private static class EcheqFactoring extends ApiObjeto {
        String chequeCmc7;
        String chequeCodigoPostalGirado;
        String chequeCuentaCbu;
        String chequeCuentaGirada;
        BigDecimal chequeImporte;
        int echeqAgrupadorId;
        String echeqEstado;
        EcheqBeneficiarioActual echeqBeneficiarioActual;
        EcheqBeneficiarioOriginal echeqBeneficiarioOriginal;
        String echeqCodigoVisualizacion;
        String echeqConcepto;
        String echeqFechaDeposito;
        String echeqFechaEmision;
        String echeqId;
        String echeqMotivoPago;
        String echeqTipoLibramiento;
        List<EndosoCustodia> endosos;
        List<LibradorCuentaElectronica> libradorCuentasElectronicas;
        String libradorDomicilioCodigoPostal;
        String libradorNumeroDocumento;
        String libradorRazonSocial;
    }


    public static CustodiarEcheqOB custodiarEcheq(ContextoOB contexto, cheques cheque, String cuentaDeposito, String cbuDeposito, String emailBeneficiario, String sucursal) {
        ApiRequest request = new ApiRequest("Depósito eCheq", "cheques", "POST", "/v1/factoring/transacciones/custodia/echeq", contexto);

        AutorizacionParametro autorizacionParametro = new AutorizacionParametro();
        autorizacionParametro.modulo = 802;
        autorizacionParametro.transaccion = 997;
        autorizacionParametro.usuario = "OFB";

        Cliente cliente = new Cliente();
        cliente.tipoDocumento = 11;
        cliente.numeroDocumento = contexto.sesion().empresaOB.cuit.toString();
        cliente.cuenta = cuentaDeposito;
        cliente.cbu = cbuDeposito;
        cliente.clienteCodigo = contexto.sesion().empresaOB.idCobis.toString();
        cliente.clienteRazonSocial = OBEcheq.normalizarCadena(contexto.sesion().empresaOB.razonSocial.toString());
        cliente.ivaPosicionCodigo = "INSC";
        cliente.personaCodigo = "J";
        cliente.sectorCodigo = "306";
        cliente.cuentaMail = !emailBeneficiario.isEmpty() ? emailBeneficiario : " ";

        EcheqBeneficiarioActual echeqBeneficiarioActual = new EcheqBeneficiarioActual();
        echeqBeneficiarioActual.beneficiarioCuentaMail = emailBeneficiario;
        echeqBeneficiarioActual.beneficiarioNombre = OBEcheq.normalizarCadena(contexto.sesion().empresaOB.razonSocial.toString());
        echeqBeneficiarioActual.numeroDocumento = contexto.sesion().empresaOB.cuit.toString();
        echeqBeneficiarioActual.tipoDocumento = 11;

        EcheqBeneficiarioOriginal echeqBeneficiarioOriginal = new EcheqBeneficiarioOriginal();
        echeqBeneficiarioOriginal.beneficiarioNombre = OBEcheq.normalizarCadena(cheque.cuenta_emisora.emisor_razon_social);
        echeqBeneficiarioOriginal.numeroDocumento = cheque.cuenta_emisora.emisor_cuit;
        echeqBeneficiarioOriginal.tipoDocumento = 11;

        EndosoCustodia endosoCustodia = new EndosoCustodia();
        endosoCustodia.beneficiarioDocumento = "";
        endosoCustodia.endosoTipo = "";
        endosoCustodia.endosoEstado = "";
        endosoCustodia.fechaHora = "";
        endosoCustodia.beneficiarioDocumento = "";
        List<EndosoCustodia> endosos = new ArrayList<EndosoCustodia>();
        endosos.add(endosoCustodia);

        LibradorCuentaElectronica librador = new LibradorCuentaElectronica();
        List<LibradorCuentaElectronica> libradores = new ArrayList<LibradorCuentaElectronica>();
        libradores.add(librador);

        Autorizante autorizante = new Autorizante();
        autorizante.autorizanteNombre = OBEcheq.normalizarCadena(contexto.sesion().usuarioOB.nombre);
        autorizante.numeroDocumento = contexto.sesion().usuarioOB.cuil.toString();
        autorizante.tipoDocumento = 8;
        List<Autorizante> autorizantes = new ArrayList<Autorizante>();
        autorizantes.add(autorizante);

        Parametros parametros = new Parametros();
        parametros.autorizantes = autorizantes;
        parametros.moneda = 80;
        parametros.sucursal = Integer.valueOf(sucursal);

        request.body("autorizacionParametro", autorizacionParametro);
        request.body("cliente",cliente);

        EcheqFactoring echeqFactoring = new EcheqFactoring();
        echeqFactoring.chequeCmc7 = cheque.cmc7;
        echeqFactoring.chequeCodigoPostalGirado =cheque.cuenta_emisora.emisor_cp;
        echeqFactoring.chequeCuentaCbu = cheque.cuenta_emisora.emisor_cbu;
        echeqFactoring.chequeCuentaGirada = cheque.cuenta_emisora.emisor_cuenta;
        echeqFactoring.chequeImporte =new BigDecimal(cheque.monto);
        echeqFactoring.echeqAgrupadorId = cheque.agrupador_id;
        echeqFactoring.echeqEstado = cheque.estado;
        echeqFactoring.echeqBeneficiarioActual = echeqBeneficiarioActual;
        echeqFactoring.echeqBeneficiarioOriginal = echeqBeneficiarioOriginal;
        echeqFactoring.echeqCodigoVisualizacion = cheque.cod_visualizacion;
        echeqFactoring.echeqConcepto = cheque.cheque_concepto;
        echeqFactoring.echeqFechaDeposito = new Fecha(cheque.fecha_pago, "yyyy-MM-dd").string("yyyyMMdd");
        echeqFactoring.echeqFechaEmision = new Fecha(cheque.fecha_emision, "yyyy-MM-dd").string("yyyyMMdd");
        echeqFactoring.echeqId = cheque.cheque_id;
        echeqFactoring.echeqMotivoPago = cheque.cheque_motivo_pago;
        echeqFactoring.echeqTipoLibramiento = cheque.cheque_caracter;
        echeqFactoring.endosos = endosos;
        echeqFactoring.libradorCuentasElectronicas = libradores;
        echeqFactoring.libradorDomicilioCodigoPostal = cheque.cuenta_emisora.emisor_cp;
        echeqFactoring.libradorNumeroDocumento = cheque.cuenta_emisora.emisor_cuit;
        echeqFactoring.libradorRazonSocial = cheque.cuenta_emisora.emisor_razon_social;

        List<EcheqFactoring> cheques = new ArrayList<EcheqFactoring>();
        cheques.add(echeqFactoring);

        request.body("eCheqs", cheques);

        request.body("parametros", parametros);

        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(CustodiarEcheqOB.class);
    }


}
