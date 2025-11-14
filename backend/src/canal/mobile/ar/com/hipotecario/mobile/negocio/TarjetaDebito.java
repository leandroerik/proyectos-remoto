package ar.com.hipotecario.mobile.negocio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Concurrencia;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Momento;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.servicio.TarjetaDebitoService;

public class TarjetaDebito {

	/* ========== ATRIBUTOS ========== */
	public Boolean existenErrores = false;
	private ContextoMB contexto;
	private Objeto consolidada;
	private Objeto detalle; // emm-20190508--> se había comentado, lo reviví

	/* ========== CONSTRUCTORES ========== */
	public TarjetaDebito(ContextoMB contexto, Objeto tarjetaDebito) {
		this.contexto = contexto;
		this.consolidada = tarjetaDebito;
	}

	/* ========== SERVICIOS ========== */
	public static Map<TarjetaDebito, ApiResponseMB> cargarDetalle(ContextoMB contexto) {
		Map<TarjetaDebito, ApiResponseMB> mapa = new ConcurrentHashMap<>();
		List<TarjetaDebito> tarjetasDebito = contexto.tarjetasDebito();
		ExecutorService executorService = Concurrencia.executorService(tarjetasDebito);
		for (TarjetaDebito tarjetaDebito : tarjetasDebito) {
			executorService.submit(() -> {
				if (!tarjetaDebito.existenErrores) {
					ApiResponseMB apiResponse = TarjetaDebitoService.tarjetaDebitoGet(contexto, tarjetaDebito.numero());
					tarjetaDebito.existenErrores |= apiResponse.hayError();
//					tarjetaDebito.detalle = apiResponse;
					mapa.put(tarjetaDebito, apiResponse);
				}
			});
		}
		Concurrencia.esperar(executorService, null);
		return mapa;
	}

	// emm-20190508-desde--> Hice este fix para que aparezcan los limites de
	// extracción y de compra, porque dejaron de aparecer con la función anterior.
//							Lo ideal es que se deje de ver la anterior.
	public static void cargarDetalle2(ContextoMB contexto, TarjetaDebito tarjetaDebitoSeleccionada) {
		List<TarjetaDebito> tarjetasDebito = contexto.tarjetasDebito();
		ExecutorService executorService = Concurrencia.executorService(tarjetasDebito);
		for (TarjetaDebito tarjetaDebito : tarjetasDebito) {
			executorService.submit(() -> {
				if (!tarjetaDebito.existenErrores) {
					ApiResponseMB apiResponse = TarjetaDebitoService.tarjetaDebitoGet(contexto, tarjetaDebito.numero());
					tarjetaDebito.existenErrores |= apiResponse.hayError();
					if (tarjetaDebitoSeleccionada.numero().equals(tarjetaDebito.numero())) {
						tarjetaDebitoSeleccionada.detalle = apiResponse;
					}

//					tarjetaDebito.detalle = apiResponse;
				}
			});
		}
		Concurrencia.esperar(executorService, null);
	}

	/* ========== METODOS ========== */
	public String id() {
		return consolidada.string("idProducto");
	}

	public String producto() {
		return "Tarjeta de Débito";
	}

	public String numero() {
		return consolidada.string("numeroProducto");
	}

	public String ultimos4digitos() {
		return Formateador.ultimos4digitos(numero());
	}

	public Boolean esTitular() {
		return "T".equals(consolidada.string("tipoTitularidad")) || "P".equals(consolidada.string("tipoTitularidad"));
	}

	public String titularidad() {
		String titularidad = "";
		titularidad = "P".equals(consolidada.string("tipoTitularidad")) ? "Principal" : titularidad;
		titularidad = "T".equals(consolidada.string("tipoTitularidad")) ? "Titular" : titularidad;
		titularidad = "A".equals(consolidada.string("tipoTitularidad")) ? "Adicional" : titularidad;
		return titularidad;
	}

	public String codigo() {
		ApiResponseMB response = cargarDetalle(contexto).get(this);
		try {
			return response.string("numeroTarjeta");
		} catch (Exception e) {
			return null;
		}
	}

	public BigDecimal limiteCompra() {
//		cargarDetalle(contexto);
		ApiResponseMB response = cargarDetalle(contexto).get(this);
		try {
			return response.bigDecimal("limiteExtraccion").multiply(new BigDecimal("6"));
		} catch (Exception e) {
			return null;
		}
	}

	public BigDecimal limiteCompra2() {
//		cargarDetalle(contexto);
		cargarDetalle2(contexto, this);
		try {
			return detalle.bigDecimal("limiteExtraccion").multiply(new BigDecimal("6"));
		} catch (Exception e) {
			return null;
		}
	}

	public BigDecimal limiteExtraccion() {
//		cargarDetalle(contexto);
		ApiResponseMB response = cargarDetalle(contexto).get(this);
		try {
			return response.bigDecimal("limiteExtraccion");
		} catch (Exception e) {
			return null;
		}
	}

	public BigDecimal limiteExtraccion2() {
		cargarDetalle2(contexto, this);
		try {
			return detalle.bigDecimal("limiteExtraccion");
		} catch (Exception e) {
			return null;
		}
	}

	public List<Cuenta> cuentasAsociadas() {
		List<Cuenta> cuentas = new ArrayList<>();
		ApiResponseMB response = TarjetaDebitoService.tarjetaDebitoGetCuentasAsociadas(contexto, id());
		for (Objeto item : response.objetos()) {
			String numero = item.string("numero");
			Cuenta cuenta = contexto.cuenta(numero);
			if (cuenta != null) {
				cuentas.add(cuenta);
			}
		}
		return cuentas;
	}

	public List<CuentaAsociada> cuentasAsociadasPorIdTarjeta() {
		List<CuentaAsociada> cuentasAsociadas = new ArrayList<>();
		ApiResponseMB response = TarjetaDebitoService.tarjetaDebitoGetCuentasAsociadas(contexto, id());
		for (Objeto item : response.objetos()) {
			CuentaAsociada cuenta = new CuentaAsociada();
			cuenta.setNumero(item.string("numero"));
			cuenta.setCodigo(item.string("codigo"));
			cuenta.setTipo(item.string("tipo"));
			cuenta.setPrincipal(item.bool("principal"));
			cuenta.setPrincipalExt(item.bool("principalExt"));
			cuentasAsociadas.add(cuenta);
		}
		return cuentasAsociadas;
	}

	public List<Cuenta> cuentasAsociadasPrincipales() {
		List<Cuenta> cuentas = new ArrayList<>();
		ApiResponseMB response = TarjetaDebitoService.tarjetaDebitoGetCuentasAsociadas(contexto, id());
		if (response.hayError()) {
			return null;
		}
		for (String patron : Objeto.listOf("AHO:80", "CTE:80", "AHO:2")) {
			for (Objeto item : response.objetos()) {
				String numero = item.string("numero");
				Boolean esPrincipal = item.bool("principal");
				if (esPrincipal) {
					String tipo = patron.split(":")[0];
					String idMoneda = patron.split(":")[1];
					Cuenta cuenta = contexto.cuenta(numero);
					if (cuenta != null && cuenta.idTipo().equals(tipo) && cuenta.idMoneda().equals(idMoneda)) {
						cuentas.add(cuenta);
					}
				}
			}
		}
		return cuentas;
	}

	public List<Cuenta> cuentasAsociadasPrincipalesExterior() {
		List<Cuenta> cuentas = new ArrayList<>();
		ApiResponseMB response = TarjetaDebitoService.tarjetaDebitoGetCuentasAsociadas(contexto, id());
		if (response.hayError()) {
			return null;
		}
		for (String patron : Objeto.listOf("AHO:80", "CTE:80", "AHO:2")) {
			for (Objeto item : response.objetos()) {
				String numero = item.string("numero");
				Boolean esPrincipal = item.bool("principalExt");
				if (esPrincipal) {
					String tipo = patron.split(":")[0];
					String idMoneda = patron.split(":")[1];
					Cuenta cuenta = contexto.cuenta(numero);
					if (cuenta != null && cuenta.idTipo().equals(tipo) && cuenta.idMoneda().equals(idMoneda)) {
						cuentas.add(cuenta);
					}
				}
			}
		}
		return cuentas;
	}

	public String fechaVencimiento(String formato) {
		return fechaVencimiento(formato, null);
	}

	public String fechaVencimiento(String formato, String valorPorDefecto) {
		ApiResponseMB response = TarjetaDebitoService.detalle(contexto, numero());
		try {
			return response.date("fechaExpiracion", "yyyy-MM-dd", formato, valorPorDefecto);
		} catch (Exception e) {
			return valorPorDefecto;
		}
	}

	public String fechaVencimiento2(String formato) {
		cargarDetalle2(contexto, this);
		try {
			return detalle.date("fechaExpiracion", "yyyy-MM-dd", formato);
		} catch (Exception e) {
			return null;
		}
	}

	public String idPaquete() {
		return consolidada.string("idPaquete");
	}

	public String getEstado() {
		return consolidada.string("estado");
	}

	public String idTipoTarjeta() {
		cargarDetalle2(contexto, this);
		return detalle.string("tipoTarjeta").trim();
	}

	public String estadoLink() {
		String estado = TarjetaDebitoService.tarjetaDebitoGetEstado(contexto, numero()).string("estadoTarjeta");
		return estado;
	}

	public Boolean habilitadaLink() {
		return estadoLink().equals("HABILITADA");
	}

	public Date fechaAlta() {
		return consolidada.date("fechaAlta", "yyyy-MM-dd");
	}

	public Boolean activacionTemprana() {
		Boolean marca = consolidada.bool("activacionTemprana");
		try {
			if (marca) {
				Momento fechaAlta = new Momento(consolidada.string("fechaAlta"), "yyyy-MM-dd");
				Momento fechaCorte = Momento.hoy().restarDias(ConfigMB.integer("dias_vigencia_activacion_temprana", 90));
				if (fechaAlta.esAnterior(fechaCorte)) {
					marca = false;
				}
			}
		} catch (Exception e) {
		}
		return marca;
	}

	public String getTipo() {
		return consolidada.string("tipoProducto");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id() == null) ? 0 : id().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TarjetaDebito other = (TarjetaDebito) obj;
		if (id() == null) {
			if (other.id() != null)
				return false;
		} else if (!id().equals(other.id()))
			return false;
		return true;
	}

	public Boolean virtual() {
		return consolidada.bool("virtual");
	}

//	@Override
//	public boolean equals(Object obj) {
//		if (obj != null && obj instanceof TarjetaDebito) {
//			return this.id().equals(((TarjetaDebito)obj).id());
//		}
//		return false;
//	}

//	{
//	    "muestraPaquete" : false,
//	    "tipoProducto" : "ATM",
//	    "numeroProducto" : "4998590170254805",
//	    "idProducto" : "1000002364",
//	    "sucursal" : 16,
//	    "descSucursal" : "MENDOZA",
//	    "descEstado" : "VIGENTE",
//	    "estado" : "V",
//	    "fechaAlta" : "2018-08-02",
//	    "idDomicilio" : 1,
//	    "tipoTitularidad" : "P",
//	    "adicionales" : false,
//	    "moneda" : "80",
//	    "descMoneda" : "PESOS"
//	  }

//	DETALLE
//	{
//	  "numeroTarjeta": "1056908",
//	  "tipoTarjeta": "NV ",
//	  "descTipoTarjeta": "TARJETA NORMAL",
//	  "estadoTarjeta": "B",
//	  "descEstadoTarjeta": "ELIMINADA",
//	  "limiteExtraccion": 8000,
//	  "cantidadAdicionales": 1,
//	  "descSucursal": "(51) VENADO TUERTO",
//	  "fechaExpiracion": "2017-12-31"
//	}	

}
