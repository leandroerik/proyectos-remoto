package ar.com.hipotecario.backend.servicio.api.productos;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.productos.PrestamosV4.PrestamoV4;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.negocio.CuotaPrestamo;
import ar.com.hipotecario.canal.homebanking.servicio.RestPrestamo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.*;

public class PrestamosV4 extends ApiObjetos<PrestamoV4> {

	/* ========== ATRIBUTOS ========== */
	public static class PrestamoV4 extends ApiObjeto {
		public String codigoProducto;
		public String codigoTitularidad;
		public String descripcionPaquete;
		public String descTitularidad;
		public String estado;
		public Fecha fechaAlta = Fecha.nunca();
		public Boolean muestraPaquete;
		public String numeroProducto;
		public String tipo;
		public String cuentaAsociada;
		public String codMoneda;
		public String descProducto;
		public BigDecimal detProducto;
		public BigDecimal importe;
		public Fecha pfFechaVencimiento;
		public String esProCrear;
		public String hipotecarioNSP;
		public String fechaProximoVenc;
		public String formaPago;
		public String plazoOriginal;
		public String esPrecoudeu;
		public String categoria;
		public Integer cantCuotasMora;
		public BigDecimal montoCuotaActual;
		public String descEstado;
		private List<ar.com.hipotecario.canal.homebanking.conector.ApiResponse> cuotas;
		private ar.com.hipotecario.canal.homebanking.conector.ApiResponse detalle;

		public String descripcion() {
			return "Prestamo";
		}

		public String tipo(ContextoHB contexto, String idPrestamo) {
			String codProducto = detalle(contexto, idPrestamo).string("tipoOperacion");
			String descripcion = tipos(codProducto);
			if (descripcion.isEmpty() && esProCrear.equals("S") && codProducto.equals("PPPROMATE2")) {
				descripcion = "Préstamo Mejoramiento Materiales";
			}
			return descripcion;
		}

		public String titularidad() {
			String descripcion = titularidades(codigoTitularidad);
			return descripcion;
		}

		public String descripcionMoneda() {
			return Modulo.moneda(codMoneda);
		}

		public String simboloMoneda() {
			return Modulo.simboloMonedaActual(codMoneda);
		}

		public String montoAprobadoFormateado() {
			return Modulo.importe(importe);
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

		public ar.com.hipotecario.canal.homebanking.conector.ApiResponse detalle(ContextoHB contexto, String idPrestamo) {
			detalle = detalle != null ? detalle : RestPrestamo.detalle(contexto, idPrestamo);
			return detalle;
		}

		public Integer cuotaActual(ContextoHB contexto, String idPrestamo) {
			// return detalle().integer("cuotaActual") - 1;
			if (detalle(contexto, idPrestamo).integer("cuotaActual") != null)
				return detalle(contexto, idPrestamo).integer("cuotaActual") - 1;
			else
				return null;
		}

		public Integer cantidadCuotas(ContextoHB contexto, String idPrestamo) {
			return detalle(contexto, idPrestamo).integer("plazoOriginal");
		}

		public List<CuotaPrestamo> cuotas(ContextoHB contexto, PrestamosV4.PrestamoV4 prestamo) {
			ar.com.hipotecario.canal.homebanking.base.Objeto datos = new ar.com.hipotecario.canal.homebanking.base.Objeto();
			List<CuotaPrestamo> lista = new ArrayList<>();
			if (cuotas == null) {
				cuotas = RestPrestamo.cuotas(contexto, prestamo.numeroProducto, cuotaActual(contexto, prestamo.numeroProducto) != null
						&& !cuotaActual(contexto, prestamo.numeroProducto).equals(0) ? cuotaActual(contexto, prestamo.numeroProducto) : cantidadCuotas(contexto, prestamo.numeroProducto));
				for (ar.com.hipotecario.canal.homebanking.conector.ApiResponse response : cuotas) {
					if (response.hayError()) {
						throw new ar.com.hipotecario.canal.homebanking.excepcion.ApiException(response);
					}
					for (ar.com.hipotecario.canal.homebanking.base.Objeto item : response.objetos()) {
						item.set("orden", String.format("%05d", item.integer("numero")));
						datos.add(item);
					}
				}
				for (ar.com.hipotecario.canal.homebanking.base.Objeto item : datos.ordenar("orden").objetos()) {
					lista.add(new CuotaPrestamo(item));
				}
				Collections.reverse(lista);
			}
			return lista;
		}

		public Boolean enConstruccion(ContextoHB contexto, String idPrestamo) {
			return "CONSTRUCCION".equals(detalle(contexto, idPrestamo).string("etapaConstruccion"));
		}

		public Boolean debitoAutomatico(ContextoHB contexto, String idPrestamo) {
			return detalle(contexto, idPrestamo).bool("esDebitoAutomatico");
		}

		public Boolean habilitadoCambioFormaPago(PrestamosV4.PrestamoV4 prestamo) {
			try {
				List<String> invalidLoanTypes = new ArrayList<String>();
				for (String cod : ConfigHB.string("codigos_invalidos_prestamo").split("_")) {
					invalidLoanTypes.add(cod);
				}
				return !invalidLoanTypes.contains(prestamo.codigoProducto) && titularidad().equals("D") && !"HIPOTECARIO".equalsIgnoreCase(prestamo.categoria);
			} catch (Exception e) {
				return false;
			}
		}

		public Boolean habilitadoMenuPago(PrestamosV4.PrestamoV4 prestamo) {
			try {
				Boolean habilitadoMenuPago = "true".equals(ConfigHB.string("prendido_menu_pago"));
				if (ConfigHB.string("prendido_menu_pago_codigos") != null && habilitadoMenuPago) {
					Set<String> nemonicos = ar.com.hipotecario.canal.homebanking.base.Objeto.setOf(ConfigHB.string("prendido_menu_pago_codigos").split("_"));
					return nemonicos.contains(prestamo.codigoProducto);
				}
				return false;
			} catch (Exception e) {
				return false;
			}
		}

		public String ultimos4digitos(String cuentaAsociada) {
			return Formateador.ultimos4digitos(cuentaAsociada);
		}

		public Integer cuotasPendientes(ContextoHB contexto, String idPrestamo) {
			if (detalle(contexto, idPrestamo).integer("plazoRestante") != null)
				return detalle(contexto, idPrestamo).integer("plazoRestante") + 1;
			else
				return null;
		}

		public Integer cuotasVencidas(ContextoHB contexto, String idPrestamo) {
			return detalle(contexto, idPrestamo).integer("cantidadCuotasMora");
		}

		public BigDecimal montoUltimaCuota(ContextoHB contexto, String idPrestamo) {
			return detalle(contexto, idPrestamo).bigDecimal("montoUltimaCuota");
		}

		public String montoUltimaCuotaFormateado(ContextoHB contexto, String idPrestamo) {
			return Formateador.importe(montoUltimaCuota(contexto, idPrestamo));
		}
	}

	public static List<PrestamoV4> prestamos(Contexto contexto) {
		List<PrestamoV4> prestamos = new ArrayList<>();
		PosicionConsolidadaV4 productos = ApiProductos.posicionConsolidadaV4(contexto, contexto.sesion().idCobis, true, false, false).get();
		for (PrestamoV4 item : productos.prestamos) {
			if (!item.tipo.equals("NSP")) {
				prestamos.add(item);
			}
		}
		return prestamos;
	}

	public static List<Objeto> prestamosByIdCliente(ContextoHB contexto) {
		ar.com.hipotecario.backend.conector.api.ApiRequest request = new ApiRequest("ConsultaConsolidadaPrestamos", "prestamos", "GET", "/v2/prestamos", contexto);

		request.query("buscansp", false);
		request.query("estado", true);
		request.query("idcliente", contexto.idCobis());

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);

		if(response.http(204)) {
			throw new RuntimeException("SIN_PRESTAMOS");
		}

		try {
			List<Objeto> prestamos = response.objetos();
			return prestamos;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error deserializando la respuesta a una lista de Prestamos", e);
		}
	}
}
