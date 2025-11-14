package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.math.BigDecimal;
import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.Liquidaciones.Liquidacion;;


public class Liquidaciones extends ApiObjetos<Liquidacion>{
	 public static class Liquidacion extends ApiObjeto {
	        public Integer CodLiquidacion;
	        public String ID;
	        public String IDTIPO;
	        public Integer CuotapartistaID;
	        public Integer CuotapartistaNumero;
	        public String CuotapartistaNombre;
	        public Integer CodAgColocador;
	        public Integer AgenteColocadorID;
	        public Integer AgenteColocadorNumero;
	        public String AgenteColocadorDescripcion;
	        public Integer SucursalID;
	        public Integer SucursalNumero;
	        public String SucursalDescripcion;
	        public Integer FondoID;
	        public Integer FondoNumero;
	        public String FondoNombre;
	        public String TipoVCPID;
	        public String TipoVCPAbreviatura;
	        public String TipoVCPDescripcion;
	        public String CondicionIngresoEgresoID;
	        public String CondicionIngresoEgresoDescripcion;
	        public String OficialCuentaDescripcion;
	        public String LiquidacionTipoID;
	        public String LiquidacionTipo;
	        public Integer LiquidacionNumero;
	        public Integer LiquidacionSolicitud;
	        public String FechaConcertacion;
	        public String FechaLiquidacion;
	        public Integer MonedaID;
	        public String MonedaSimbolo;
	        public String MonedaDescripcion;
	        public BigDecimal ImporteNeto;
	        public BigDecimal ImporteBruto;
	        public BigDecimal Gastos;
	        public String Cuotapartes;
	        public String VCPValor;
	        public String TimeStamp;
	        public Integer TipoVCPIDCafci;
	    }

	    /* ========== SERVICIOS ========== */
	    // API-Inversiones_ConsultaSolicitudes
	    public static Liquidaciones obtenerLiquidaciones(Contexto contexto, String fechaDesde, String fechaHasta, Integer numeroCuotapartista, String nombre) {
	        ApiRequest request = new ApiRequest("obtenerLiquidaciones", "inversiones", "POST", "/v1/liquidaciones", contexto);
	        request.body("pLiquidaciones.FechaDesde", fechaDesde);
	        request.body("pLiquidaciones.FechaHasta", fechaHasta);
	        request.body("pLiquidaciones.NumeroCuotapartista", numeroCuotapartista);
	        request.body("nombre", nombre);

	        ApiResponse response = request.ejecutar();
	        ApiException.throwIf(!response.http(200, 204), request, response);
	        return response.crear(Liquidaciones.class, response.objeto("Liquidaciones").objetos());

	    }

}
