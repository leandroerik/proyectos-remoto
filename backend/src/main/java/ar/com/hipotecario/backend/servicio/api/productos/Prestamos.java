package ar.com.hipotecario.backend.servicio.api.productos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.servicio.api.productos.Prestamos.Prestamo;

public class Prestamos extends ApiObjetos<Prestamo> {

	/* ========== ATRIBUTOS ========== */
	public static class Prestamo extends ApiObjeto {
		public Boolean muestraPaquete;
		public String tipoProducto;
		public String numeroProducto;
		public String idProducto;
		public String sucursal;
		public String descSucursal;
		public String descEstado;
		public String estado;
		public Fecha fechaAlta = Fecha.nunca();
		public String idDomicilio;
		public String tipoTitularidad;
		public String descTipoTitularidad;
		public Boolean adicionales;
		public String moneda;
		public String descMoneda;
		public String hipotecarioNSP;
		public String codigoProducto;
		public BigDecimal montoAprobado;
		public Fecha fechaProxVencimiento;
		public String formaPago;
		public String esPrecodeu;
		public String esProCrear;
		public String categoria;
		public Integer cantCuotasMora;
		public BigDecimal montoCuotaActual;
		public Integer plazoOriginal;

		public String descripcion() {
			return "Prestamo";
		}

		public String tipo() {
			String descripcion = tipos(codigoProducto);
			if (descripcion.isEmpty() && esProCrear.equals("S") && codigoProducto.equals("PPPROMATE2")) {
				descripcion = "Préstamo Mejoramiento Materiales";
			}
			return descripcion;
		}

		public String titularidad() {
			String descripcion = titularidades(tipoTitularidad);
			return descripcion;
		}

		public String descripcionMoneda() {
			return Modulo.moneda(moneda);
		}

		public String simboloMoneda() {
			return Modulo.simboloMonedaActual(moneda);
		}

		public String formaPago() {
			String codigo = formaPago;
			String descripcion = formasPago(codigo);
			return descripcion;
		}

		public String montoAprobadoFormateado() {
			return Modulo.importe(montoAprobado);
		}

		/* ========== MAPAS ========== */
		public static String tipos(String codigo) {
			Map<String, String> mapa = new HashMap<>();
			mapa.put("CALLANSES$", "Hipotecario");
			mapa.put("HADNUANSES", "Hipotecario");
			mapa.put("HADUSANSES", "Hipotecario");
			mapa.put("HAMTEANSES", "Hipotecario");
			mapa.put("HCHAADNU", "Hipotecario");
			mapa.put("HCHAADNURP", "Hipotecario");
			mapa.put("HCHAADUS", "Hipotecario");
			mapa.put("HCHAADUSRP", "Hipotecario");
			mapa.put("HCHAAMTE", "Hipotecario");
			mapa.put("HCHACONS", "Hipotecario");
			mapa.put("HCHACONSRP", "Hipotecario");
			mapa.put("HCHAESCAYT", "Hipotecario");
			mapa.put("HCHAESCCON", "Hipotecario");
			mapa.put("HCONSANSES", "Hipotecario");
			mapa.put("HESCAYT", "Hipotecario");
			mapa.put("HESCCONS", "Hipotecario");
			mapa.put("HICONSEMVA", "Hipotecario");
			mapa.put("HICONSUVI", "Hipotecario UVA");
			mapa.put("HIPLOCFI", "Hipotecario");
			mapa.put("HIPLOCVA", "Hipotecario");
			mapa.put("HIPO$", "Hipotecario");
			mapa.put("HIPO$EMP", "Hipotecario");
			mapa.put("HIPO$EMPVA", "Hipotecario");
			mapa.put("HIPO$VAR", "Hipotecario");
			mapa.put("HIPOBH0609", "Hipotecario");
			mapa.put("HIPOCASAPR", "Hipotecario");
			mapa.put("HIPOCOMB", "Hipotecario");
			mapa.put("HIPOCONEMP", "Hipotecario");
			mapa.put("HIPOCONS$", "Hipotecario");
			mapa.put("HIPOCONSUE", "Hipotecario");
			mapa.put("HIPOCONSVA", "Hipotecario");
			mapa.put("HIPOHPLUSF", "Hipotecario");
			mapa.put("HIPOHPLUSV", "Hipotecario");
			mapa.put("HIPOINM", "Hipotecario");
			mapa.put("HIPOINMEMP", "Hipotecario");
			mapa.put("HIPOMIG01", "Hipotecario");
			mapa.put("HIPOMIGCER", "Hipotecario");
			mapa.put("HIPOMIGCOM", "Hipotecario");
			mapa.put("HIPOMIGEMP", "Hipotecario");
			mapa.put("HIPOMIGFA", "Hipotecario");
			mapa.put("HIPOMIGMB", "Hipotecario");
			mapa.put("HIPOMIGME", "Hipotecario");
			mapa.put("HIPOMIGVJA", "Hipotecario");
			mapa.put("HIPOPROC1", "C. Propia y A. Joven");
			mapa.put("HIPOREGU", "Hipotecario");
			mapa.put("HIPOSINCEN", "Hipotecario");
			mapa.put("HIPOSTEPUP", "Hipotecario");
			mapa.put("HIPOSUELDO", "Hipotecario");
			mapa.put("HIPOSVIDA", "Hipotecario");
			mapa.put("HIPOTERR$", "Hipotecario");
			mapa.put("HIPOU$S", "Hipotecario");
			mapa.put("HIPOUVI", "Hipotecario UVA");
			mapa.put("HIPYMEALF", "Hipotecario");
			mapa.put("HIPYMEALV", "Hipotecario");
			mapa.put("PERSP", "Personal");
			mapa.put("PERSPADEBA", "Personal");
			mapa.put("PERSPAMPL", "Personal");
			mapa.put("PERSPCC", "Personal");
			mapa.put("PERSPEMP", "Personal");
			mapa.put("PERSPFGS", "Personal");
			mapa.put("PERSPFGS2", "Personal");
			mapa.put("PERSPFGS2A", "Personal");
			mapa.put("PERSPREGU", "Personal");
			mapa.put("PERSPSMF", "Personal");
			mapa.put("PERSPUVA", "Personal");
			mapa.put("PERSPVAR", "Personal");
			mapa.put("PMOGARPRE", "Personal");
			mapa.put("PMOGTIAHIP", "Personal");
			mapa.put("PMOSIND", "Personal");
			mapa.put("PMOSINDOL", "Personal");
			mapa.put("PMOSPYMES", "Personal");
			mapa.put("PMOTASADIP", "Personal");
			mapa.put("PMOTASADIR", "Personal");
			mapa.put("PMOU$SPROP", "Personal");
			mapa.put("PMTASADIP2", "Personal");
			mapa.put("PPANSESTUR", "Hipotecario");
			mapa.put("PPCHACONV", "Personal");
			mapa.put("PPCHAPREND", "Personal");
			mapa.put("PPEMPFGS", "Personal");
			mapa.put("PPEMPFGS2", "Personal");
			mapa.put("PPEMPFGS2A", "Personal");
			mapa.put("PPPROACUVA", "Micro Crédito");
			mapa.put("PPPROCREA1", "Personal");
			mapa.put("PPPROCREA2", "Personal");
			mapa.put("PPPROCREA3", "Personal");
			mapa.put("PPPROINFRA", "Procrear Gas");
			mapa.put("PPPROMATE2", "Procrear Materiales");
			mapa.put("PPPROMATER", "Procrear Materiales");
			mapa.put("PPROMATER", "Procrear Gas");
			mapa.put("PPROSARIO", "Hipotecario");
			mapa.put("PPTDCOMER", "Personal");
			mapa.put("PRECODEMP", "Personal");
			mapa.put("PRECODEU", "Personal");
			mapa.put("PRECODEUFH", "Personal");
			mapa.put("PRECONSFIN", "Personal");
			mapa.put("PREFI", "Personal");
			mapa.put("PRENDAU$S", "Prendario");
			mapa.put("PRENDAUTOP", "Prendario");
			mapa.put("PRENDAUTOV", "Prendario");
			mapa.put("PRESCHA", "Personal");
			mapa.put("PRESHML", "Personal");
			mapa.put("PRESLARIO", "Personal");
			mapa.put("PRESMENDO", "Personal");
			mapa.put("PRESMENFGS", "Personal");
			mapa.put("PRESRETAIL", "Personal");
			mapa.put("PREVENTA1", "PREVENTA 1");
			mapa.put("PREVENTA2", "PREVENTA 2");
			mapa.put("PREVENTA3", "PREVENTA 3");
			mapa.put("PREVENTA4", "PREVENTA 4");
			mapa.put("PREVENTA5", "PREVENTA 5");
			mapa.put("PROADQNU1", "Hipotecario");
			mapa.put("PROADQNU2", "Hipotecario");
			mapa.put("PROADQNU3", "Hipotecario");
			mapa.put("PROADQNU4", "Hipotecario");
			mapa.put("PROADQNU5", "Hipotecario");
			mapa.put("PROADQROS1", "Hipotecario");
			mapa.put("PROADQROS2", "Hipotecario");
			mapa.put("PROC3D1", "Hipotecario");
			mapa.put("PROC3D2", "Hipotecario");
			mapa.put("PROC3D3", "Hipotecario");
			mapa.put("PROC3D4", "Hipotecario");
			mapa.put("PROC3D5", "Hipotecario");
			mapa.put("PROCAYUDA1", "Personal");
			mapa.put("PROCAYUDA2", "Personal");
			mapa.put("PROCAYUDA3", "Personal");
			mapa.put("PROCAYUDA4", "Personal");
			mapa.put("PROCAYUDA5", "Personal");
			mapa.put("PROCINUND", "Personal");
			mapa.put("PROCINUND2", "Personal");
			mapa.put("PROCLOTSE1", "Hipotecario");
			mapa.put("PROCLOTSE2", "Hipotecario");
			mapa.put("PROCLOTSE3", "Hipotecario");
			mapa.put("PROCLOTSE4", "Hipotecario");
			mapa.put("PROCLOTSE5", "Hipotecario");
			mapa.put("PROCOMB1", "Hipotecario");
			mapa.put("PROCOMB2", "Hipotecario");
			mapa.put("PROCOMB3", "Hipotecario");
			mapa.put("PROCOMB4", "Hipotecario");
			mapa.put("PROCOMB5", "Hipotecario");
			mapa.put("PROCOMPUVI", "P. Compl UVA");
			mapa.put("PROCOMUVI2", "P. Compl UVA");
			mapa.put("PROCONROS1", "Hipotecario");
			mapa.put("PROCONROS2", "Hipotecario");
			mapa.put("PROCREACE1", "Hipotecario");
			mapa.put("PROCREACE2", "Hipotecario");
			mapa.put("PROCREACE3", "Hipotecario");
			mapa.put("PROCREACE4", "Hipotecario");
			mapa.put("PROCREACE5", "Hipotecario");
			mapa.put("PROCREAR1", "Hipotecario");
			mapa.put("PROCREAR2", "Hipotecario");
			mapa.put("PROCREAR3", "Hipotecario");
			mapa.put("PROCREAR4", "Hipotecario");
			mapa.put("PROCREAR5", "Hipotecario");
			mapa.put("PROCREAYT1", "Hipotecario");
			mapa.put("PROCREAYT2", "Hipotecario");
			mapa.put("PROCREAYT3", "Hipotecario");
			mapa.put("PROCREAYT4", "Hipotecario");
			mapa.put("PROCREFAC1", "Crédito Refacción");
			mapa.put("PROCREFAC2", "Crédito Refacción");
			mapa.put("PROEMPHOG1", "Hipotecario");
			mapa.put("PROEMPHOG2", "Hipotecario");
			mapa.put("PROEMPHOG3", "Hipotecario");
			mapa.put("PROEMPREN1", "Hipotecario");
			mapa.put("PROEMPREN2", "Hipotecario");
			mapa.put("PROEMPREN3", "Hipotecario");
			mapa.put("PROEMPREN4", "Hipotecario");
			mapa.put("PROEMPREN5", "Hipotecario");
			mapa.put("PROEMPUVA1", "Hipotecario UVA");
			mapa.put("PROEMPUVA2", "Hipotecario UVA");
			mapa.put("PROLOCOHOG", "Hipotecario");
			mapa.put("PROLOCOHOG2", "Hipotecario");
			mapa.put("PROCAMPHOG", "Hipotecario");
			mapa.put("PROCCONHOG", "Hipotecario");
			mapa.put("RHPLUSSINF", "Personal");
			mapa.put("RHPLUSSINV", "Personal");
			mapa.put("TASADIPSC", "Personal");
			String valor = mapa.get(codigo);
			return valor != null ? valor : "";
		}

		public static String titularidades(String codigo) {
			Map<String, String> mapa = new HashMap<>();
			mapa.put("A", "Cotitular");
			mapa.put("C", "Codeudor");
			mapa.put("D", "Deudor Principal");
			mapa.put("E", "Empleador");
			mapa.put("F", "Firmante");
			mapa.put("R", "Representante Legal");
			mapa.put("T", "Titular");
			mapa.put("U", "Autorizado ATM");
			String valor = mapa.get(codigo);
			return valor != null ? valor : "";
		}

		public static String formasPago(String codigo) {
			Map<String, String> mapa = new HashMap<>();
			mapa.put("1", "Chequera");
			mapa.put("2", "Por Ventanilla");
			mapa.put("3", "Por Tarjeta");
			mapa.put("5", "Débito en cuenta");
			mapa.put("8", "Otros");
			mapa.put("7", "Sueldos Externos");
			mapa.put("EFMN", "Efectivo");
			mapa.put("NDMNCA", "Se debitará de tu");
			mapa.put("NDMNCC", "Se debitará de tu");
			mapa.put("EFMNC", "Efectivo");
			String valor = mapa.get(codigo);
			return valor != null ? valor : "";
		}
	}

	public static List<Prestamo> prestamos(Contexto contexto) {
		List<Prestamo> prestamos = new ArrayList<>();
		PosicionConsolidada productos = ApiProductos.posicionConsolidada(contexto, contexto.sesion().idCobis, true, false, false).get();
		for (Prestamo item : productos.prestamos) {
			if (!item.tipoProducto.equals("NSP")) {
				prestamos.add(item);
			}
		}
		return prestamos;
	}
}
