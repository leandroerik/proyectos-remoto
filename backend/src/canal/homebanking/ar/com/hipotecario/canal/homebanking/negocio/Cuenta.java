package ar.com.hipotecario.canal.homebanking.negocio;

import java.math.BigDecimal;
import java.util.Date;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.lib.Encriptador;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Texto;
import ar.com.hipotecario.canal.homebanking.servicio.CuentasService;

public class Cuenta {

	/* ========== ATRIBUTOS ========== */
	private ContextoHB contexto;
	private Objeto consolidada;

	/* ========== CONSTRUCTOR ========== */
	public Cuenta(ContextoHB contexto, Objeto cuenta) {
		this.contexto = contexto;
		this.consolidada = cuenta;
	}

	/* ========== UTIL ========== */
	public String string(String clave, String valorPorDefecto) {
		return consolidada.string(clave, valorPorDefecto);
	}

	public Boolean mostrar() {
		Boolean mostrar = true;
		mostrar &= !(consolidada.string("tipoTitularidad").equals("F"));
		mostrar &= !(consolidada.string("categoria").equals("RFB"));
		mostrar &= !(consolidada.string("categoria").equals("FCL"));
		mostrar &= !(consolidada.string("categoria").equals("R"));
		return mostrar;
	}

	public Boolean esCajaAhorro() {
		return "AHO".equals(consolidada.string("tipoProducto"));
	}

	public Boolean esCuentaCorriente() {
		return "CTE".equals(consolidada.string("tipoProducto"));
	}

	public Boolean esAdelanto() {
		return "ADE".equals(consolidada.string("tipoProducto"));
	}

	public Boolean esTitular() {
		return "T".equals(consolidada.string("tipoTitularidad"));
	}

	public Boolean esPesos() {
		return "80".equals(consolidada.string("moneda"));
	}

	public Boolean esDolares() {
		return "2".equals(consolidada.string("moneda"));
	}

	/* ========== GET ========== */
	public String id() {
		return consolidada.string("idProducto");
	}
	public String idEncriptado() {
		return "true".equals(ConfigHB.string("prendido_encriptacion_cuenta")) ?
				Encriptador.encriptarPBEBH(consolidada.string("idProducto"))
				: consolidada.string("idProducto");
	}

	public String numero() {
		return consolidada.string("numeroProducto");
	}

	public String numeroFormateado() {
		String numeroFormateado = "";
		String numero = consolidada.string("numeroProducto");
		if (numero != null && numero.length() == 15) {
			numeroFormateado += numero.substring(0, 1) + "-";
			numeroFormateado += numero.substring(1, 4) + "-";
			numeroFormateado += numero.substring(4, 14) + "-";
			numeroFormateado += numero.substring(14, 15);
		}
		return numeroFormateado;
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

	public String numeroEnmascaradoAsteriscos() {
		String numeroFormateado = "";
		String numero = consolidada.string("numeroProducto");
		if (numero != null && numero.length() == 15) {
			numeroFormateado += "****";
			numeroFormateado += numero.substring(11, 15);
		} else if (numero != null && numero.length() > 4) {
			numeroFormateado += "****";
			numeroFormateado += numero.substring(numero.length() - 4);
		}
		return numeroFormateado;
	}

	public String categoria() {
		return consolidada.string("categoria");
	}

//	public String numeroEnmascarado() {
//		String numeroFormateado = "";
//		String numero = consolidada.string("numeroProducto");
//		if (numero != null && numero.length() == 15) {
//			numeroFormateado += "XXXX";
//			numeroFormateado += numero.substring(11, 14) + "-";
//			numeroFormateado += numero.substring(14, 15);
//			
//		}
//		return numeroFormateado;
//	}

	public static String numeroEnmascarado(String numero) {
		String numeroFormateado = "";
		if (numero != null && numero.length() == 15) {
			numeroFormateado += "XXXX";
			numeroFormateado += numero.substring(11, 14) + "-";
			numeroFormateado += numero.substring(14, 15);

		}
		return numeroFormateado;
	}

	public String idTipo() {
		return consolidada.string("tipoProducto");
	}

	public String producto() {
		String descripcion = "";
		descripcion = "AHO".equals(consolidada.string("tipoProducto")) ? "Caja de Ahorro" : descripcion;
		descripcion = "CTE".equals(consolidada.string("tipoProducto")) ? "Cuenta Corriente" : descripcion;
		descripcion = "ADE".equals(consolidada.string("tipoProducto")) ? "Adelanto BH" : descripcion;
		return descripcion;
	}

	public String descripcionCorta() {
		String descripcion = "";
		descripcion = "AHO".equals(consolidada.string("tipoProducto")) ? "CA" : descripcion;
		descripcion = "CTE".equals(consolidada.string("tipoProducto")) ? "CC" : descripcion;
		descripcion = "ADE".equals(consolidada.string("tipoProducto")) ? "ADE" : descripcion;
		return descripcion;
	}

	public String ultimos4digitos() {
		return Formateador.ultimos4digitos(numero());
	}

	public String idTitularidad() {
		return detalle().string("tipoTitularidad", "");
	}

	public Boolean unipersonal() {
		return "U".equalsIgnoreCase(detalle().string("usoFirma"));
	}

	public String titularidad() {
		String titularidad = null;
		titularidad = "T".equals(consolidada.string("tipoTitularidad")) ? "Titular" : titularidad;
		titularidad = "A".equals(consolidada.string("tipoTitularidad")) ? "Adicional" : titularidad;
		titularidad = "C".equals(consolidada.string("tipoTitularidad")) ? "Compañia" : titularidad;
		titularidad = "P".equals(consolidada.string("tipoTitularidad")) ? "Persona" : titularidad;
		titularidad = "D".equals(consolidada.string("tipoTitularidad")) ? "Deudor Principal Prestamo" : titularidad;
		titularidad = "M".equals(consolidada.string("tipoTitularidad")) ? "Menor Autorizado" : titularidad;
		return titularidad != null ? titularidad : Texto.primeraMayuscula(consolidada.string("descTipoTitularidad"));
	}

	public String idMoneda() {
		return consolidada.string("moneda");
	}

	public String moneda() {
		return Formateador.moneda(consolidada.string("moneda"));
	}

	public String simboloMoneda() {
		return Formateador.simboloMoneda(consolidada.string("moneda"));
	}

	public String simboloMonedaActual() {
		return Formateador.simboloMonedaActual(consolidada.string("moneda"));
	}

	public String idEstado() {
		return consolidada.string("estadoCuenta");
	}

	public Boolean estaActiva() {
		return Objeto.setOf("A", "V").contains(consolidada.string("estadoCuenta"));
	}

	public String descripcionEstado() {
		String estado = "";
		estado = "A".equals(consolidada.string("estadoCuenta")) ? "Activa" : estado;
		estado = "C".equals(consolidada.string("estadoCuenta")) ? "Cancelada" : estado;
		estado = "I".equals(consolidada.string("estadoCuenta")) ? "Inmovilizada" : estado;
		estado = "M".equals(consolidada.string("estadoCuenta")) ? "Mantenimiento de Firmas" : estado;
		estado = "R".equals(consolidada.string("estadoCuenta")) ? "Cerrada" : estado;
		estado = "V".equals(consolidada.string("estadoCuenta")) ? "Vigente" : estado;
		return estado;
	}

	public BigDecimal saldo() {
		return consolidada.bigDecimal("disponible");
	}

	public String saldoFormateado() {
		return Formateador.importe(saldo());
	}

	public BigDecimal acuerdo() {
		return consolidada.bigDecimal("acuerdo");
	}

	public String acuerdoFormateado() {
		return Formateador.importe(acuerdo());
	}

	public String sucursal() {
		return consolidada.string("sucursal");
	}

	public String idPaquete() {
		return consolidada.string("idPaquete");
	}

	public String fechaAlta(String formato) {
		return consolidada.date("fechaAlta", "yyyy-MM-dd", formato);
	}

	public Date fechaAltaDate() {
		return consolidada.date("fechaAlta", "yyyy-MM-dd");
	}

	/* ========== GET PESADOS ========== */
	public String comentario() {
		SqlResponse response = CuentasService.selectComentarioCuenta(contexto);
		for (Objeto registro : response.registros) {
			if (numero().equals(registro.string("cuenta"))) {
				return registro.string("comentario");
			}
		}
		return "";
	}

	public String cbu() {
		ApiResponse response = CuentasService.cuentaBH(contexto, numero());
		return response != null ? response.string("cbu") : "";
	}

	public boolean esCvu(){
		return CuentasService.esCvu(cbu());
	}

	public String numeroCorto() {
		ApiResponse response = CuentasService.cuentaBH(contexto, numero());
		return response != null ? response.string("cuenta") : "";
	}

	public String cbuFormateado() {
		return cbu();
	}

	public String alias() {
		ApiResponse response = CuentasService.cuentaCoelsa(contexto, cbu());
		return response != null ? response.string("nuevoAlias") : "";
	}

	/* ========== TRANSFORMACIONES ========== */
	public static String tipo(String numero) {
		String tipo = null;
		tipo = numero.startsWith("2") ? "AHO" : tipo;
		tipo = numero.startsWith("3") ? "CTE" : tipo;
		tipo = numero.startsWith("4") ? "AHO" : tipo;
		return tipo;
	}

	public static String idMoneda(String numero) {
		String moneda = null;
		moneda = numero.startsWith("2") ? "2" : moneda;
		moneda = numero.startsWith("3") ? "80" : moneda;
		moneda = numero.startsWith("4") ? "80" : moneda;
		return moneda;
	}

	public static String descripcionCuentaComprobante(String tipoCuenta, String moneda, String cuenta) {
		String tipoCuentaOrigenDescripcion = "AHO".equals(tipoCuenta) ? "CA" : ("CTE".equals(tipoCuenta) ? "CC" : tipoCuenta);
		tipoCuentaOrigenDescripcion = tipoCuentaOrigenDescripcion + " " + Formateador.simboloMoneda(moneda) + " " + cuenta;
		return tipoCuentaOrigenDescripcion;
	}

	/* ========== EQUALS Y HASHCODE ========== */
	@Override
	public int hashCode() {
		return id().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (object != null && getClass() == object.getClass()) {
			return id().equals(((Cuenta) object).id());
		}
		return false;
	}

	public String usoFirma() {
		String usoFirma = "";
		
		ApiResponse response = CuentasService.cajaAhorroBH(contexto, numero());
		if(response.hayError()) {
			return "";
		}
		
		for (Objeto objeto : response.objetos()) {
			usoFirma = objeto.string("usoFirma");
		}

		return usoFirma;
	}

	public String desripcionUsoFirma() {
		return detalle().string("descUsoFirma");
	}

	public Integer oficina() {
		return detalle().integer("oficina");
	}

	public BigDecimal adelantoDisponible() {
		return detalle().bigDecimal("adelantoDisponible");
	}

	public BigDecimal adelantoUtilizado() {
		return detalle().bigDecimal("adelantoUtilizado");
	}

	public BigDecimal adelantoInteresesDevengados() {
		return detalle().bigDecimal("adelantoInteresesDevengados");
	}

	public String adelantoCuentaAsociada() {
		return detalle().string("adelantoCuentaAsociada");
	}

	public String descEstado() {
		return detalle().string("descEstado").replace("ACTIVA", "VIGENTE");
	}

	public String estado() {
		return detalle().string("estado").replace("A", "V");
	}

	public Objeto detalle() {
		ApiRequest request = Api.request("DetalleCuenta", "cuentas", "GET", esCajaAhorro() ? "/v1/cajasahorros/{idcuenta}" : "/v1/cuentascorrientes/{idcuenta}", contexto);
		request.path("idcuenta", numero());
		request.query("fechadesde", Fecha.hoy().string("yyyy-MM-dd"));
		request.query("validacuentaempleado", "false");
		request.cacheSesion = true;
		ApiResponse response = Api.response(request, numero());
		return !response.hayError() ? response.objetos().get(0) : null;
	}

	public String cbuV4(String numeroProducto) {
		ApiResponse response = CuentasService.cuentaBH(contexto, numeroProducto);
		return response != null ? response.string("cbu") : "";
	}

	public String aliasV4(String cbu) {
		ApiResponse response = CuentasService.cuentaCoelsa(contexto, cbu);
		return response != null ? response.string("nuevoAlias") : "";
	}

//	{
//	    "idPaquete" : 33384,
//	    "muestraPaquete" : true,
//	    "tipoProducto" : "CTE",
//	    "numeroProducto" : "303100000326087",
//	    "idProducto" : "52530493",
//	    "sucursal" : 31,
//	    "descSucursal" : "SAN RAFAEL",
//	    "descEstado" : "VIGENTE",
//	    "estado" : "V",
//	    "fechaAlta" : "2007-06-12",
//	    "idDomicilio" : 7,
//	    "tipoTitularidad" : "T",
//	    "descTipoTitularidad" : "TITULAR",
//	    "adicionales" : true,
//	    "moneda" : "80",
//	    "descMoneda" : "PESOS",
//	    "estadoCuenta" : "A",
//	    "disponible" : -15229.33,
//	    "acuerdo" : 3190.88
//	  }
}
