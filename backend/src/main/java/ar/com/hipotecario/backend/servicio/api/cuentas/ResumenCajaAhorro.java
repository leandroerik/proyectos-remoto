package ar.com.hipotecario.backend.servicio.api.cuentas;

import java.math.BigDecimal;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class ResumenCajaAhorro extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public static class Subtotal {
		public BigDecimal Creditos;
		public BigDecimal Debitos;
		public BigDecimal IVA;
	}

	public static class Cabecera {
		public String nombreCompleto;
		public String Direccion;
		public String TipoDeDocumento;
		public String Letras;
		public String Garantia;
		public String Clave;
		public String ProdBancDescripcion;
		public String FechaValor;
		public String Localidad;
		public String Provincia;
		public String SucursalCuenta;
		public String DireccionSucursalCuenta;
		public String LocalidadSucursalCuenta;
		public String ProvinciaSucursalCuenta;
		public String CtaGtiaHip;
		public String MODescripcion;
		public Fecha FechaUltMov;
		public Fecha FechaDesde;
		public BigDecimal SaldoFinal;
		public BigDecimal SaldoInicial;
		public BigDecimal AHSaldoUltCorte;
		public BigDecimal AHPromedio;
		public BigDecimal AHRemesas;
		public BigDecimal AHDisponible;
		public BigDecimal IVA1;
		public BigDecimal TNA;
		public BigDecimal TEA;
		public Integer NumeroDeDocumento;
		public Integer CantidadDeTitulares;
		public Integer AHOficial;
		public Integer AHParroquia;
		public Integer IDSucursalCuenta;
		public Integer AHPaquete;
		public Integer AHSolidaria;
	}

	public static class DevolucionesIVA {

	}

	public static class Impuestos {
		public String Descripcion;
		public BigDecimal Valor;
	}

	public static class IngresoBruto {

	}

	public Subtotal Subtotal;
	public Cabecera cabecera;
	public List<DevolucionesIVA> devolucionesIVA;
	public List<Impuestos> impuestos;
	public List<IngresoBruto> ingresoBruto;

	/* ========== SERVICIOS ========== */
	// API-Cuentas_ResumenElectronicoCajaDeAhorro
	static ResumenCajaAhorro get(Contexto contexto, String idCuenta, Fecha fechaDesde, Fecha fechaHasta) {
		ApiRequest request = new ApiRequest("CajasAhorroByIdCuenta", "cuentas", "GET", "/v1/cajasahorros/{idcuenta}/resumen", contexto);
		request.path("idcuenta", idCuenta);
		request.query("fechaDesde", fechaDesde.string("yyyy-MM-dd"));
		request.query("fechaHasta", fechaHasta.string("yyyy-MM-dd"));
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(ResumenCajaAhorro.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Fecha fechaDesde = new Fecha("2020-01-13", "yyyy-MM-dd");
		Fecha fechaHasta = new Fecha("2020-04-13", "yyyy-MM-dd");
		ResumenCajaAhorro datos = get(contexto, "400400011740843", fechaDesde, fechaHasta);
		imprimirResultado(contexto, datos);
	}
}
