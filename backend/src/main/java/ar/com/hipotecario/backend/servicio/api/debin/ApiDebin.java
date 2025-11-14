package ar.com.hipotecario.backend.servicio.api.debin;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.DebinProgramadoOB;

import java.math.BigDecimal;

// http://api-debin-microservicios-homo.appd.bh.com.ar/swagger-ui.html
public class ApiDebin {

    /* ========== Banco Controller ========== */

    // TODO: POST /v1/altaCuenta

    // TODO: POST /v1/coelsa/banco

    // TODO: GET /v1/cuenta

    // TODO: POST /v1/echo

    // TODO: POST /v1/endPoint

    // TODO: GET /v1/listaEndPoint

    /* ========== Coelsa Controller ========== */

    // TODO: POST /v1/coelsa/AvisoAdhesionRecurrencia

    // TODO: POST /v1/coelsa/AvisoDebinFinalizado

    // TODO: POST /v1/coelsa/AvisoDebinPendiente

    // TODO: GET /v1/coelsa/AvisoEcho

    // TODO: POST /v1/coelsa/AvisoEcho

    // TODO: POST /v1/coelsa/AvisoOperacionFinalizada

    // TODO: POST /v1/coelsa/AvisoResultadoRecurrencia

    // TODO: POST /v1/coelsa/Credito

    // TODO: POST /v1/coelsa/Debito

    /* ========== Comprador Controller ========== */

    // TODO: POST /v1/compradores

    // TODO: DELETE /v1/compradores

    // TODO: GET /v1/compradores/{idTributario}

    // TODO: POST /v1/compradores/contracargo

    // TODO: POST /v1/compradores/recurrencias

    // TODO: DELETE /v1/compradores/recurrencias

    // TODO: GET /v1/compradores/recurrencias/{idTributario}/{id}

    // TODO: POST /v1/compradores/recurrencias/listas

    /* ========== Credin Controller ========== */

   public static Futuro<AutorizarCredin> autorizarCredin(ContextoOB contexto, String destinatarioCvu, String destinatarioBanco, String destinatarioIdTributario, String destinatarioNombre, String concepto, String importe, String monedaId, String monedaSigno, String monedaDescripcion, String originanteCbu, String originanteNumeroCuenta, String originanteSucursalId, String originanteTipoCuenta, String originanteIdTributario, String originanteMail, String originanteNombre, Long nroOperacion){
       return Util.futuro(()-> AutorizarCredin.post(contexto,destinatarioCvu,destinatarioBanco,destinatarioIdTributario,destinatarioNombre,concepto,importe,monedaId,monedaSigno,monedaDescripcion,originanteCbu,originanteNumeroCuenta,originanteSucursalId,originanteTipoCuenta,originanteIdTributario,originanteMail,originanteNombre,nroOperacion));
   }

    // TODO: POST /v1/credin/autorizar

    // TODO: POST /v1/credin/sueldos

    /* ========== Debin Controller ========== */

    // POST /v1/debin
	public static Futuro<AltaDEBINResponse> altaDebin(Contexto contexto, String cbuComprador, String idTributarioComprador, String concepto, String bancoVendedor, String idSucursalVendedor, String descSucursalVendedor, BigDecimal importe, Moneda moneda, Boolean recurrencia, String tiempoExpiracion, String cbuVendedor, String idTributarioVendedor) {
        return Util.futuro(() -> AltaDEBINResponse.post(contexto, cbuComprador, idTributarioComprador, concepto, bancoVendedor, idSucursalVendedor, descSucursalVendedor, importe, moneda, recurrencia, tiempoExpiracion, cbuVendedor, idTributarioVendedor));
    }
	
    // TODO: DELETE /v1/debin

    // GET /v1/debin/{id}
    public static Futuro<ConsultarDebin.ConsultaDebinResponse> consultarDebin(Contexto contexto, String id) {
        return Util.futuro(() -> ConsultarDebin.get(contexto, id));
    }

    // POST /v1/debin/autorizar
    public static Futuro<Autorizar> postAutorizar(Contexto contexto, Debin debin, String operacion) {
        return Util.futuro(() -> Autorizar.post(contexto, debin, operacion));
    }

    // POST /v1/debin/listas
    public static Futuro<ListarDebin.ListarDebinResponse> listarDebin(Contexto contexto, String idTributarioComprador, String bancoComprador, String fechaDesde, String fechaHasta,String idTributarioVendedor) {
        return Util.futuro(() -> ListarDebin.post(contexto, idTributarioComprador, bancoComprador, fechaDesde, fechaHasta,idTributarioVendedor));
    }

    /* ========== DebinPlazoFijo Controller ========== */

    // POST /v1/debin/plazofijo
    public static Futuro<DebinPlazoFijoWeb> crearDebinPlazoFijoWeb(Contexto contexto, String idCliente, String cuit, String cbu, BigDecimal monto, String numeroPlazoFijoWeb) {
        return Util.futuro(() -> DebinPlazoFijoWeb.post(contexto, idCliente, cuit, cbu, monto, numeroPlazoFijoWeb));
    }

    /* ========== DebinQR Controller ========== */

    // TODO: GET /v1/billetera

    // TODO: POST /v1/billetera

    // TODO: POST /v1/cbu

    // TODO: POST /v1/coelsa/QROperacionFinalizada

    // TODO: POST /v1/preautorizar

    // TODO: GET /v1/QR/{qr_id_trx}/{id_psp}

    // TODO: POST /v1/QR/contracargo

    // TODO: POST /v1/QR/debinqr

    /* ========== Vendedor Controller ========== */


    // TODO: GET /v1/QR/{qr_id_trx}/{id_psp}

    // TODO: POST /v1/QR/contracargo

    // TODO: POST /v1/QR/debinqr

    /* ========== Vendedor Controller ========== */

    // POST /v1/vendedores
    public static Futuro<EndpointVendedores.RespuestaOk> altaVendedor(Contexto contexto, String correo, String cbu, String idSucursal, String descSucursal, String cuit) {
        return Util.futuro(() -> EndpointVendedores.post(contexto, correo, cbu, idSucursal, descSucursal, cuit));
    }

    // DELETE /v1/vendedores
    public static Futuro<EndpointVendedores.RespuestaOk> deleteVendedor(Contexto contexto, String idTributario, String cbu) {
        return Util.futuro(() -> EndpointVendedores.delete(contexto, idTributario, cbu));
    }

    // GET /v1/vendedores/{idTributario}
    public static Futuro<EndpointVendedores> getVendedor(Contexto contexto, String idTributario) {
        return Util.futuro(() -> EndpointVendedores.get(contexto, idTributario));
    }

    // TODO: POST /v1/vendedores/consultaLista

    // TODO: POST /v1/vendedores/limites

    // TODO: GET /v1/vendedores/limites/{idTributario}

    // TODO: POST /v1/vendedores/prestacion

    // TODO: GET /v1/vendedores/prestacion/{cuit}

    // TODO: DELETE /v1/vendedores/prestacion/{cuit}/{prestacion}

    // TODO: POST /v1/vendedores/recurrencias

    // TODO: DELETE /v1/vendedores/recurrencias

    // TODO: GET /v1/vendedores/recurrencias/{idTributario}/{id}

    // TODO: POST /v1/vendedores/recurrencias/listas



    // DEBIN PROGRAMADO
    public static Futuro<ListarDebinProgramado> listarDebinProgramado(Contexto contexto, String idTributarioComprador, String bancoComprador, String fechaDesde, String fechaHasta,String idTributarioVendedor) {
        return Util.futuro(() -> ListarDebinProgramado.post(contexto, idTributarioComprador, bancoComprador, fechaDesde, fechaHasta,idTributarioVendedor));
    }
    public static Futuro<ListarDebinProgramado> buscarDebinRecurrente(Contexto contexto, String idTributarioComprador, String bancoComprador, String fechaDesde, String fechaHasta,String idTributarioVendedor,String bancoVendedor,String estado) {
        return Util.futuro(() -> ListarDebinProgramado.buscarDebinRecurrente(contexto, idTributarioComprador, bancoComprador, fechaDesde, fechaHasta,idTributarioVendedor,bancoVendedor,estado));
    }
    public static Futuro<AdherirRecurrencia> adherirRecurrencia(ContextoOB contexto, DebinProgramadoOB debin, Boolean aceptarORechar) {
        return Util.futuro(() -> AdherirRecurrencia.post(contexto, debin,aceptarORechar));
    }
    public static Futuro<BajaRecurrencia> bajaRecurrencia(ContextoOB contexto, String id, DebinProgramadoOB debin) {
        return Util.futuro(() -> BajaRecurrencia.post(contexto, id,debin));
    }
}
