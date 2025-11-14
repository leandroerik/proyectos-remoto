package ar.com.hipotecario.backend.servicio.api.inversiones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.List;

public class ComprobantePrestamosOB extends ApiObjeto {
    public List<ConceptoAplicado> conceptosAplicados;
    public List<DatoOperacion> datosOperacion;
    public List<DetallePago> detallePago;
    public List<DatosComplementariosOperacion> datosComplementariosOperacion;

    public static class ConceptoAplicado {
        public String concepto;
        public String descon;
        public BigDecimal valcon;
        public int cuota;
        public BigDecimal tna;
        public BigDecimal tem;
        public BigDecimal tea;
        public BigDecimal sig;
        public BigDecimal cotizacion;
        public BigDecimal montomn;
        public int moneda;
        public String simbolo;
    }

    public static class DatoOperacion {
        public String banco;
        public int tramite;
        public String linCredito;
        public int cliente;
        public String toperacion;
        public String descToperacion;
        public int moneda;
        public String descMoneda;
        public int oficina;
        public int oficial;
        public String fechaIni;
        public String fechaFin;
        public BigDecimal monto;
        public String tplazo;
        public String desc_tplazo;
        public int plazo;
        public String tdividendo;
        public String descTdividendo;
        public int periodoCap;
        public int periodoInt;
        public int periodosGracia;
        public int periodosGraciasInt;
        public String primeraCuota;
        public String destino;
        public String descDestino;
        public int ciudad;
        public String descCiudad;
        public String nemProducto;
        public String descProducto;
        public String cuenta;
        public String reajuste;
        public String reajustePeriodo;
        public String renovacion;
        public String precancelacion;
        public String redescuento;
        public int diasAnio;
        public String fechaFija;
        public int diaPago;
        public String descOfi;
        public String nombre;
        public BigDecimal tasa;
        public String nomOficial;
        public String desSector;
        public String migrada;
        public String fechaLiq;
        public String cuotaAdic;
        public BigDecimal tem;
        public BigDecimal tea;
        public BigDecimal costo;
        public int diasPrecancelar;
        public String cuilCuit;
        public String amortizacion;
        public int numCuotas;
        public String direccion;
        public String postal;
        public String situacion;
        public String desSituacion;
        public String desSubMercado;
        public BigDecimal saldoCapital;
        public String fechaven1;
        public String fechaven2;
    }

    public static class DetallePago {
        public String fecha;
        public String producto;
        public String cuenta;
        public String benefic;
        public String descmon;
        public BigDecimal valor;
        public int moneda;
        public String descfPago;
        public String pesiticada;
    }

    public static class DatosComplementariosOperacion {
        public String situacion;
        public String descSituacion;
        public String nroHipotecario;
        public String plazoObjetivo;
        public String esPreventa;
        public String esFsp;
    }

    /* ========== SERVICIOS ========== */
    // API-Comprobantes_ConsultaDatosComprobantePrestamo
    public static ComprobantePrestamosOB get(Contexto contexto, String estado, String operacion, String recibo, String siguiente ) {
        ApiRequest request = new ApiRequest("ConsultaDatosComprobantePrestamo", "comprobantes", "GET", "/v1/prestamos", contexto);
        request.query("estado", estado);
        request.query("operacion", operacion);
        request.query("recibo",recibo);
        request.query("siguiente",siguiente);

        String mensajeError = "0";

        ApiResponse response = request.ejecutar();
        if(response.http(404) && response.body.contains("codigo")) {
            JSONObject jsonObject = new JSONObject(response.body);
            mensajeError = (String) jsonObject.get("mensajeAlUsuario");
        }
        ApiException.throwIf(mensajeError, !response.http(200,204), request, response);
        ApiException.throwIf(!response.http(200, 204), request, response);
        ComprobantePrestamosOB r = response.crear(ComprobantePrestamosOB.class);
        return r;
    }
}
