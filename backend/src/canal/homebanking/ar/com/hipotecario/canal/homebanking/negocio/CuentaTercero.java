package ar.com.hipotecario.canal.homebanking.negocio;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.servicio.CuentasService;
import ar.com.hipotecario.canal.homebanking.servicio.RestCatalogo;
import ar.com.hipotecario.canal.homebanking.servicio.SqlHomebanking;

public class CuentaTercero {

	/* ========== ATRIBUTOS ========== */
	public String id;
	public ApiResponse cuentaBH = new ApiResponse();
	public ApiResponse cuentaLink = new ApiResponse();
	public ApiResponse cuentaCoelsa = new ApiResponse();
	public ApiResponse cuentaCbuBH = new ApiResponse();
	public ContextoHB contexto;
	public Boolean cuentaEncontrada = false;

	/* ========== CONSTRUCTOR ========== */
	public CuentaTercero(ContextoHB contexto, String id) {
		this.id = id;

		String moneda = "2";
		this.contexto = contexto;
		if (CuentasService.esCbu(id)) {
			cuentaEncontrada = true;
			// cuentaLink = CuentasService.cuentaLink(contexto, id, "2");
			cuentaLink = CuentasService.cuentaLink(contexto, id, moneda);
			// emm--> Tenemos el siguiente problema:
			// el gobierno agregó algunas validaciones que provocan que si mandamos un 2 al
			// servicio de link
			// a veces tira error y no nos devuelve un 80 como corresponde para las cuentas
			// que son solo "Pesos".
			// No me queda alternativa que volver a llamar al servicio, pero esta vez con un
			// 80.
			if (cuentaLink.hayError()) {
				ApiResponse cuentaLinkAux = CuentasService.cuentaLink(contexto, id, "80");
				if (!cuentaLinkAux.hayError()) {
					cuentaLink = cuentaLinkAux;
				}
			}
			cuentaCoelsa = CuentasService.cuentaCoelsa(contexto, id);
			if (CuentasService.esCbuBH(id)) {
				cuentaCbuBH = CuentasService.cuentaCbuBH(contexto, id);
			}
		}
		if (CuentasService.esCvu(id)) {
			cuentaEncontrada = true;
			cuentaCoelsa = CuentasService.cuentaCoelsa(contexto, id);
		}
		if (CuentasService.esCuentaBH(id)) {
			cuentaEncontrada = true;
			cuentaBH = CuentasService.cuentaBH(contexto, id);
			if (!cuentaBH.hayError()) {
				String cbu = cuentaBH.string("cbu");
				// cuentaLink = CuentasService.cuentaLink(contexto, cbu, "2");
				cuentaLink = CuentasService.cuentaLink(contexto, cbu, moneda);
				cuentaCoelsa = CuentasService.cuentaCoelsa(contexto, cbu);
			}
		}
		if (CuentasService.esAlias(id)) {
			cuentaEncontrada = true;
			cuentaCoelsa = CuentasService.cuentaCoelsa(contexto, id);
			if (!cuentaCoelsa.hayError()) {
				String cbu = cuentaCoelsa.string("cbu");
				if (!CuentasService.esCvu(cbu)) {
					// cuentaLink = CuentasService.cuentaLink(contexto, cbu, "2");
					cuentaLink = CuentasService.cuentaLink(contexto, cbu, moneda);
					// emm--> Tenemos el siguiente problema:
					// el gobierno agregó algunas validaciones que provocan que si mandamos un 2 al
					// servicio de link
					// a veces tira error y no nos devuelve un 80 como corresponde para las cuentas
					// que son solo "Pesos".
					// No me queda alternativa que volver a llamar al servicio, pero esta vez con un
					// 80.
					if (cuentaLink.hayError()) {
						ApiResponse cuentaLinkAux = CuentasService.cuentaLink(contexto, cbu, "80");
						if (!cuentaLinkAux.hayError()) {
							cuentaLink = cuentaLinkAux;
						}
					}

					if (CuentasService.esCbuBH(cbu)) {
						cuentaBH = CuentasService.cuentaBH(contexto, cbu);
					}
				}
			}
		}
	}

	public CuentaTercero(ContextoHB contexto, String id, boolean v2) {
		this.id = id;
		this.contexto = contexto;
		if (CuentasService.esCbu(id)) {
			cuentaEncontrada = true;
			cuentaCoelsa = CuentasService.cuentaCoelsa(contexto, id);
			if (CuentasService.esCbuBH(id)) {
				cuentaBH = CuentasService.cuentaBH(contexto, id);
			}
		}
		if (CuentasService.esCvu(id)) {
			cuentaEncontrada = true;
			cuentaCoelsa = CuentasService.cuentaCoelsa(contexto, id);
		}
		if (CuentasService.esCuentaBH(id)) {
			cuentaEncontrada = true;
			cuentaBH = CuentasService.cuentaBH(contexto, id);
			if (!cuentaBH.hayError()) {
				String cbu = cuentaBH.string("cbu");
				cuentaCoelsa = CuentasService.cuentaCoelsa(contexto, cbu);
			}
		}
		if (CuentasService.esAlias(id)) {
			cuentaEncontrada = true;
			cuentaCoelsa = CuentasService.cuentaCoelsa(contexto, id);
			if (!cuentaCoelsa.hayError()) {
				String cbu = cuentaCoelsa.string("cbu");
				if (!CuentasService.esCvu(cbu)) {
					if (CuentasService.esCbuBH(cbu)) {
						cuentaBH = CuentasService.cuentaBH(contexto, cbu);
					}
				}
			}
		}
	}

	/* ========== METODOS ========== */
	public String titular() {
		try {
			return cuentaLink.objetos("titulares").get(0).string("denominacion");
		} catch (Exception e) {
			try {
				if (!cuentaCoelsa.objetos("cotitulares").isEmpty()) {
					return cuentaCoelsa.objetos("cotitulares").get(0).string("nombre");
				}
				return cuentaCoelsa.string("nombreTitular").toUpperCase().trim();
			} catch (Exception ex) {
				return null;
			}
		}
	}

	public String cbu() {
		String cbu = null;
		cbu = CuentasService.esCbu(id) ? id : cbu;
		cbu = CuentasService.esCuentaBH(id) ? cuentaBH.string("cbu", null) : cbu;
		cbu = CuentasService.esAlias(id) ? cuentaCoelsa.string("cbu") : cbu;
		cbu = CuentasService.esCvu(id) ? id : cbu;
		return cbu;
	}

	public String alias() {
		try {
			return cuentaCoelsa.string("nuevoAlias");
		} catch (Exception e) {
			return null;
		}
	}

	public String cuit() {
		try {
			return cuentaLink.objetos("titulares").get(0).string("idTributario");
		} catch (Exception e) {
			try {
				return cuentaCoelsa.string("cuit").trim();
			} catch (Exception ex) {
				return null;
			}
		}
	}

	public Boolean mismoTitularColesa(String cuil) {
		try {
			return cuentaCoelsa.string("cuit").trim().equals(cuil);
		} catch (Exception ex) {
			return null;
		}
	}

	public Boolean esCotitularCoelsa(String cuil) {
		try {
			List<Objeto> listCotitulares = cuentaCoelsa.objetos("cotitulares");

			if (listCotitulares.isEmpty())
				return false;

			for (Objeto unCotitular : listCotitulares) {
				if (unCotitular.string("cuit").trim().equals(cuil))
					return true;
			}
			return false;

		} catch (Exception ex) {
			return false;
		}
	}

	public String tipo() {
		String tipo = null;
		tipo = tipo == null && CuentasService.esCbu(id) ? cuentaLink.string("tipoProducto", cuentaCoelsa.string("tipoCuenta")) : tipo;
		tipo = tipo == null && CuentasService.esCbuBH(id) ? CuentasService.tipoCuentaBH(cuentaCbuBH.string("tipoCuenta")) : tipo;
		tipo = tipo == null && CuentasService.esCvu(id) ? null : tipo;
		tipo = tipo == null && CuentasService.esCuentaBH(id) ? CuentasService.tipoCuentaBH(id) : tipo;
		tipo = tipo == null && CuentasService.esAlias(id) ? cuentaCoelsa.string("tipoCuenta") : tipo;
		return tipo;
	}

	public String numero() {
		String numero = null;
		numero = numero == null && CuentasService.esCbu(id) ? cuentaLink.string("cuenta", null) : numero;
		numero = numero == null && CuentasService.esCbuBH(id) ? cuentaCbuBH.string("cuenta", null) : numero;
		numero = numero == null && CuentasService.esCvu(id) ? null : numero;
		numero = numero == null && CuentasService.esCuentaBH(id) ? id : numero;
		numero = numero == null && CuentasService.esAlias(id) ? cuentaLink.string("cuenta", null) : numero;
		return numero;
	}

	public String idMoneda() {
		String idMoneda = null;
		idMoneda = idMoneda == null && CuentasService.esCbu(id) ? cuentaLink.string("moneda", null) : idMoneda;
		idMoneda = idMoneda == null && CuentasService.esCbuBH(id) ? cuentaCbuBH.string("moneda", null) : idMoneda;
		idMoneda = idMoneda == null && CuentasService.esCvu(id) ? "80" : idMoneda;
		idMoneda = idMoneda == null && CuentasService.esCuentaBH(id) ? CuentasService.idMonedaCuentaBH(id) : idMoneda;
		idMoneda = idMoneda == null && CuentasService.esAlias(id) ? cuentaLink.string("moneda", null) : idMoneda;
		if (idMoneda == null && CuentasService.esAlias(id)) {
			String cbu = cbu();
			if(cbu != null && cbu.startsWith("044")){
				idMoneda = cuentaBH.string("moneda", null);
			}
			else if("11".equals(cuentaCoelsa.string("tipoMoneda"))){
				idMoneda = "2";
			}
		}
		idMoneda = idMoneda == null ? "80" : idMoneda;
		return idMoneda;
	}

	public Set<String> idMonedas() {
		Set<String> idMonedas = new HashSet<>();
		idMonedas.add(idMoneda());
		if (idMonedas.contains("2")) {
			String cadena = CuentasService.esAlias(id) ? cuentaCoelsa.string("cbu") : id;
			ApiResponse response = CuentasService.cuentaLink(contexto, cadena, "80");
			if (!response.hayError()) {
				idMonedas.add(response.string("moneda"));
			}
		}

		return idMonedas;
	}

	public boolean esBiMonetaria(){
		return  idMonedas().size() > 1;
	}

	public String banco() {
		String tipo = null;
		tipo = tipo == null && CuentasService.esCbu(id) ? cuentaLink.string("nombreBancoDestino") : tipo;
		tipo = tipo == null && CuentasService.esCvu(id) ? null : tipo;
		tipo = tipo == null && CuentasService.esCuentaBH(id) ? "BANCO HIPOTECARIO S.A." : tipo;
		tipo = tipo == null && CuentasService.esAlias(id) ? cuentaLink.string("nombreBancoDestino") : tipo;
		if (tipo == null || tipo.isEmpty()) {
			tipo = RestCatalogo.banco(cuentaCoelsa.string("nroBco"));
		}
		return tipo;
	}

	public String nrobanco() {
		String tipo = null;
		tipo = CuentasService.esCbu(id) ? cuentaLink.string("codigoBancoDestino") : tipo;
		tipo = tipo == null && CuentasService.esCvu(id) ? null : tipo;
		tipo = tipo == null && CuentasService.esCuentaBH(id) ? "044" : tipo;
		tipo = tipo == null && CuentasService.esAlias(id) ? cuentaLink.string("codigoBancoDestino") : tipo;
		if (tipo == null || tipo.isEmpty()) {
			tipo = cuentaCoelsa.string("nroBco");
		}
		return tipo;
	}

	public String titularCuentaLink() {
		try {
			return cuentaLink.objetos("titulares").get(0).string("denominacion");
		} catch (Exception e) {
			return "";
		}
	}

	/* ========== UTIL ========== */
	public Boolean esCuentaBH() {
		String cuenta = CuentasService.esAlias(id) ? cuentaCoelsa.string("cbu") : id;
		return CuentasService.esCbuBH(cuenta) || CuentasService.esCuentaBH(cuenta);
	}

	public Boolean esCvu() {
		String cuenta = CuentasService.esAlias(id) ? cuentaCoelsa.string("cbu") : id;
		return CuentasService.esCvu(cuenta);
	}
	
	public boolean esCuentaBroker() {		
		return (SqlHomebanking.getBroker(this.cuit(), EstadoBroker.ACTIVO) != null) ? true : false;
	}
	
}
