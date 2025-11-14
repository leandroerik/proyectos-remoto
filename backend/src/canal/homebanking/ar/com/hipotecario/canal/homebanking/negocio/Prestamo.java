package ar.com.hipotecario.canal.homebanking.negocio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.excepcion.ApiException;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.servicio.RestPrestamo;

public class Prestamo {

	/* ========== ATRIBUTOS ========== */
	private ContextoHB contexto;
	private Objeto consolidada;
	private ApiResponse detalle;
	private List<ApiResponse> cuotas;

	/* ========== CONSTRUCTORES ========== */
	public Prestamo(ContextoHB contexto, Objeto prestamo) {
		this.contexto = contexto;
		this.consolidada = prestamo;
	}

	/* ========== CONSOLIDADA ========== */
	public Objeto consolidada() {
		return consolidada;
	}

	public String producto() {
		return "Prestamo";
	}

	public String id() {
		return consolidada.string("idProducto");
	}

	public String numero() {
		return consolidada.string("numeroProducto");
	}

	public String numeroEnmascarado() {
		String numeroFormateado = "";
		String numero = consolidada.string("numeroProducto");
		if (numero != null && numero.length() == 15) {
			numeroFormateado += "XXX-";
			numeroFormateado += numero.substring(11, 15);
		} else if (numero != null && numero.length() > 4) {
			numeroFormateado += "XXX-";
			numeroFormateado += numero.substring(numero.length() - 4);
		}
		return numeroFormateado;
	}

	public String codigo() {
		return consolidada.string("codigoProducto");
	}

	public String idTipo() {
		return consolidada.string("tipoProducto");
	}

	public String tipo() {
		String codigo = codigo();
		String descripcion = RestPrestamo.tipo(codigo);
		if (descripcion.isEmpty() && consolidada.string("esProCrear").equals("S") && consolidada.string("codigoProducto").equals("PPPROMATE2")) {
			descripcion = "Préstamo Mejoramiento Materiales";
		}
		return descripcion;
	}

	public Boolean esProcrear() {
		return consolidada.string("esProCrear").equals("S");
	}

	public String idEstado() {
		return consolidada.string("estado");
	}

	public String estado() {
		String codigo = idEstado();
		String descripcion = RestPrestamo.estado(codigo);
		return descripcion;
	}

	public String idTitularidad() {
		return consolidada.string("tipoTitularidad");
	}

	public String titularidad() {
		String codigo = idTitularidad();
		String descripcion = RestPrestamo.titularidad(codigo);
		return descripcion;
	}

	public String idFormaPago() {
		return consolidada.string("formaPago");
	}

	public String descripcionTipoPrestamo() {
		return detalle().string("descripcionTipoPrestamo");
	}

	public String descripcionFormaPago() {
		return detalle().string("descripcionFormaPago");
	}

	// TODO
	public String formaPago() {
		String codigo = idFormaPago();
		String descripcion = RestPrestamo.formaPago(codigo);
		return descripcion;
	}

	public String descripcionPrestamo() {
		Integer cuota = cuotaActual() != null ? cuotaActual() : 0;

		if (esProcrear() && Arrays.asList("HIPOPROC1", "PPPROACUVA", "PPPROINFRA", "PPPROMATER", "PPROMATER", "PROCOMPUVI", "PROCOMUVI2", "PPPROMATE2", "PROCREFAC1", "PROCREFAC2").contains(codigo())) {
			return "Cuota " + cuota + " del Crédito Refacción";
		}

		if ("HIPOTECARIO".equals(categoria()) && Arrays.asList("HICONSUVI", "HIPOUVI", "PROCOMPUVI", "PROCOMUVI2").contains(codigo())) {
			return "Cuota " + cuota + " del Crédito " + StringUtils.capitalize(StringUtils.lowerCase(categoria())) + " UVA";
		}

		if ("HIPOTECARIO".equals(categoria())) {
			return "Cuota " + cuota + " del Crédito " + StringUtils.capitalize(StringUtils.lowerCase(categoria()));
		}

		return "Cuota " + cuota + " del Préstamo " + StringUtils.capitalize(StringUtils.lowerCase(categoria()));
	}

	public String idMoneda() {
		return consolidada.string("moneda");
	}

	public String simboloMoneda() {
		return Formateador.simboloMoneda(idMoneda());
	}

	public String simboloMonedaActual() {
		return Formateador.simboloMonedaActual(idMoneda());
	}

	public String descripcionMoneda() {
		return Formateador.moneda(idMoneda());
	}

	public String fechaAlta(String formato) {
		return consolidada.date("fechaAlta", "yyyy-MM-dd", formato);
	}

	public Date fechaProximoVencimiento() {
		return consolidada.date("fechaProxVencimiento", "yyyy-MM-dd");
	}

	public String fechaProximoVencimiento(String formato) {
		return consolidada.date("fechaProxVencimiento", "yyyy-MM-dd", formato);
	}

	public BigDecimal montoAprobado() {
		return consolidada.bigDecimal("montoAprobado");
	}

	public String montoAprobadoFormateado() {
		return Formateador.importe(montoAprobado());
	}

	/* ========== DETALLE ========== */
	public ApiResponse detalle() {
		detalle = detalle != null ? detalle : RestPrestamo.detalle(contexto, numero());
		return detalle;
	}

	public BigDecimal montoAdeudado() {
		return detalle().bigDecimal("saldoRestante");
	}

	public String montoAdeudadoFormateado() {
		return Formateador.importe(montoAdeudado());
	}

	public Integer cuotaActual() {
		// return detalle().integer("cuotaActual") - 1;
		if (detalle().integer("cuotaActual") != null)
			return detalle().integer("cuotaActual") - 1;
		else
			return null;
	}

	public Integer cuotasPendientes() {
		if (detalle().integer("plazoRestante") != null)
			return detalle().integer("plazoRestante") + 1;
		else
			return null;
	}

	public Integer cuotasVencidas() {
		return detalle().integer("cantidadCuotasMora");
	}

	public Integer cantidadCuotas() {
		return detalle().integer("plazoOriginal");
	}
	
	public String cuentaAsociada() {
		return detalle().string("cbuAsociado");
	}
	
	public String ultimos4digitos() {
		return Formateador.ultimos4digitos(cuentaAsociada());
	}

	public BigDecimal montoUltimaCuota() {
		return detalle().bigDecimal("montoUltimaCuota");
	}

	public String montoUltimaCuotaFormateado() {
		return Formateador.importe(montoUltimaCuota());
	}

	public Cuenta cuentaPago() {
		String numeroCuenta = detalle().string("cbuAsociado");
		Cuenta cuenta = contexto.cuenta(numeroCuenta);
		return cuenta;
	}

	public String descripcionTipoTasa() {
		return detalle().string("descuentoTasaInteresReferencial");
	}

	public BigDecimal tasa() {
		return detalle().bigDecimal("tasaInteres");
	}

	public String tasaFormateada() {
		return Formateador.importe(tasa());
	}

	public Boolean pagable() {
		return detalle().bool("pagable");
	}

	public Boolean debitoAutomatico() {
		return detalle().bool("esDebitoAutomatico");
	}

	public String categoria() {
		return detalle().string("categoria");
	}

	public Boolean enConstruccion() {
		return "CONSTRUCCION".equals(detalle().string("etapaConstruccion"));
	}

	/* ========== CUOTAS ========== */
	public List<CuotaPrestamo> cuotas() {
		Objeto datos = new Objeto();
		List<CuotaPrestamo> lista = new ArrayList<>();
		if (cuotas == null) {
			cuotas = RestPrestamo.cuotas(contexto, numero(), cuotaActual() != null && !cuotaActual().equals(0) ? cuotaActual() : cantidadCuotas());
			for (ApiResponse response : cuotas) {
				if (response.hayError()) {
					throw new ApiException(response);
				}
				for (Objeto item : response.objetos()) {
					item.set("orden", String.format("%05d", item.integer("numero")));
					datos.add(item);
				}
			}
			for (Objeto item : datos.ordenar("orden").objetos()) {
				lista.add(new CuotaPrestamo(item));
			}
			Collections.reverse(lista);
		}
		return lista;
	}

	/* ========== CACHE ========== */
	public void eliminarCacheDetalle() {
		RestPrestamo.eliminarCacheDetalle(contexto, numero());
	}

	public void eliminarCacheCuotas() {
		RestPrestamo.eliminarCacheCuotas(contexto, numero(), cuotaActual() != null ? cuotaActual() : cantidadCuotas() != null ? cantidadCuotas() : 1000);
	}

	public Boolean habilitadoCambioFormaPago() {
		try {
			List<String> invalidLoanTypes = new ArrayList<String>();
			for (String cod : ConfigHB.string("codigos_invalidos_prestamo").split("_")) {
				invalidLoanTypes.add(cod);
			}
			return !invalidLoanTypes.contains(codigo()) && idTitularidad().equals("D") && !"HIPOTECARIO".equalsIgnoreCase(categoria());
		} catch (Exception e) {
			return false;
		}
	}

	public Boolean habilitadoMenuPago() {
		try {
			Boolean habilitadoMenuPago = "true".equals(ConfigHB.string("prendido_menu_pago"));
			if (ConfigHB.string("prendido_menu_pago_codigos") != null && habilitadoMenuPago) {
				Set<String> nemonicos = Objeto.setOf(ConfigHB.string("prendido_menu_pago_codigos").split("_"));
				return nemonicos.contains(codigo());
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public Boolean esRecurrente() {
		return consolidada.bool("esRecurrente");
	}

//	/* ========== CONSOLIDADA ========== */
//	{
//	    "muestraPaquete" : false,
//		"tipoProducto" : "CCA",
//		"numeroProducto" : "0290147302",
//		"idProducto" : "58562016",
//		"sucursal" : 29,
//		"descSucursal" : "SAN JUAN",
//		"descEstado" : "VIGENTE",
//		"estado" : "V",
//		"fechaAlta" : "2013-09-04",
//		"idDomicilio" : "2",
//		"tipoTitularidad" : "H",
//		"descTipoTitularidad" : "HIPOTECANTE NO DEUDOR",
//		"adicionales" : false,
//		"moneda" : "80",
//		"descMoneda" : "PESOS",
//		"hipotecarioNSP" : "",
//		"codigoProducto" : "PROCREAR1",
//		"montoAprobado" : 200000.0,
//		"fechaProxVencimiento" : "2018-05-10",
//		"formaPago" : "NDMNCA"
//	  }

//	/* ========== DETALLE ========== */
//	{
//		"nroCuenta": "0450317864",
//		"tipoPrestamo": "CCA",
//		"tipoOperacion": "PERSP",
//		"montoAprobado": 71000,
//		"moneda": "80",
//		"fechaProxVencimiento": "2018-05-07",
//		"saldoRestante": 69286.92,
//		"montoUltimaCuota": 3874.54,
//		"plazoRestante": "44",
//		"cuotaActual": 4,
//		"pagable": true,
//		"plazoOriginal": "48",
//		"tasaInteres": 45.5,
//		"formaPago": "NDMNCA",
//		"cantidadCuotasMora": "0",
//		"descuentoTasaInteresReferencial": "TASA FIJA PERSONALES PESOS",
//		"tipoMicrocredito": "",
//		"esDebitoAutomatico": true,
//		"cbuAsociado": "404500015306017",
//		"desembolsosRealizados": "1",
//		"descripcionTipoPrestamo": "PRESTAMOS PERSONALES PESOS TASA FIJA",
//		"nroTamiteEmpresa": "",
//		"destinoPrestamo": "047",
//		"fechaVencimientoMasUno": "2018-06-07",
//		"descripcionFormaPago": "NOTA DEBITO CAJA AHORRO MN",
//		"desembolsosPlanificados": "0",
//		"dniBeneficiario": "",
//		"canal": "",
//		"comCancel": 5,
//		"razonSocialProvArtefacto": "",
//		"montoCuotaAnterior": 3879.19,
//		"cantCotitulares": "1",
//		"montoAcordado": 71000,
//		"nombreBeneficiario": "FERNANDEZ  STELLA MARIS",
//		"nombreMatriculado": "",
//		"fechaUltimoPago": "2018-04-09",
//		"tipoTasa": "TPERFIJA",
//		"montoPagoMinimo": 3874.54,
//		"fechaVencimientoCuotaActual": "2018-05-07",
//		"esMiCroCredito": false,
//		"formaPagoUltimoPagoDescuento": "NOTA DEBITO CAJA AHORRO MN",
//		"idPrestamoServicio": "",
//		"sistemaAmortizacion": "SISTEMA FRANCES",
//		"formaUltimoPago": "NDMNCA",
//		"fechaVencimientoActual": "2022-01-07",
//		"fechaAcuerdo": "2018-01-08",
//		"esProcrear": false,
//		"tieneRestricciones": false,
//		"montoCuotaActual": 3874.54,
//		"cuotaTope": false,
//		"dniMatriculado": "",
//		"razonSocialPrestServicio": "",
//		"cliente": "FERNANDEZ  STELLA MARIS",
//		"hipotecarioNsp": "",
//		"sucursalDesc": "RIO GALLEGOS",
//		"numeroSolicitud": "5107816",
//		"idBeneficiario": "142761",
//		"subCanal": "0",
//		"valorReposicion": 0,
//		"titular": "FERNANDEZ STELLA MARIS",
//		"cotitular": "",
//		"idProvArtefacto": "",
//		"descripcionDestinoPrestamo": "CONSUMO PERSONAL",
//		"montoACancelar": 72547.2,
//		"montoUltimoPago": 3879.19,
//		"titulizado": false,
//		"montoReembolso": 71000,
//		"fechaLiquidacion": "2018-01-08",
//		"estado": "NORMAL",
//		"categoria": "PERSONAL",
//		"fechaReembolso": "2018-01-08"
//	}
}
