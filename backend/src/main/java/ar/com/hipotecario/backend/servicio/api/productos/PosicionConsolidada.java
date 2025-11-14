package ar.com.hipotecario.backend.servicio.api.productos;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

// API-Productos_PosicionConsolidada
public class PosicionConsolidada extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public Cuentas cuentas;
	public PlazosFijos plazosFijos;
	public TarjetasCredito tarjetasCredito;
	public TarjetasDebito tarjetasDebito;
	public CuentasComitentes cuentasComitentes;
	public CuentasCuotapartistas cuentasCuotapartistas;
	public CajasSeguridad cajasSeguridad;
	public Paquetes paquetes;
	public Prestamos prestamos;
	public Seguros seguros;
	public Boolean existenErrores;

	/* ========== SERVICIOS ========== */
	public static PosicionConsolidada get(Contexto contexto, String idCobis) {
		return get(contexto, idCobis, true, true, true, "vigente", null);
	}

	public static PosicionConsolidada get(Contexto contexto, String idCobis, Boolean adicionales, Boolean firmaconjunta, Boolean firmantes) {
		return get(contexto, idCobis, adicionales, firmaconjunta, firmantes, "vigente", null);
	}

	public static PosicionConsolidada get(Contexto contexto, String idCobis, Boolean adicionales, Boolean firmaconjunta, Boolean firmantes, String tipoestado) {
		return get(contexto, idCobis, adicionales, firmaconjunta, firmantes, tipoestado, null);
	}

	public static PosicionConsolidada get(Contexto contexto, String idCobis, Boolean adicionales, Boolean firmaconjunta, Boolean firmantes, String tipoestado, String cuit) {
		ApiRequest request = new ApiRequest("PosicionConsolidada", "productos", "GET", "/v2/posicionconsolidada", contexto);
		request.query("idcliente", idCobis);
		request.query("adicionales", adicionales);
		request.query("firmaconjunta", firmaconjunta);
		request.query("firmantes", firmantes);
		request.query("tipoestado", tipoestado); // todos | vigente | cancelado
		request.query("cuit", cuit);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);

		PosicionConsolidada posicionConsolidada = response.crear(PosicionConsolidada.class);
		posicionConsolidada.cuentas = response.crear(Cuentas.class, response.objetos("cuentas"));
		posicionConsolidada.plazosFijos = response.crear(PlazosFijos.class, response.objetos("plazosFijos"));
		posicionConsolidada.tarjetasCredito = response.crear(TarjetasCredito.class, response.objetos("tarjetasCredito"));
		posicionConsolidada.tarjetasDebito = response.crear(TarjetasDebito.class, response.objetos("tarjetasDebito"));
		posicionConsolidada.cuentasComitentes = response.crear(CuentasComitentes.class, response.objetos("inversiones"), item -> item.string("tipoProducto").equals("UNI"));
		posicionConsolidada.cuentasCuotapartistas = response.crear(CuentasCuotapartistas.class, response.objetos("productos"), item -> item.string("tipo").equals("RJA"));
		posicionConsolidada.cajasSeguridad = response.crear(CajasSeguridad.class, response.objetos("cajasSeguridad"));
		posicionConsolidada.paquetes = response.crear(Paquetes.class, response.objetos("productos"), item -> item.string("tipo").equals("PAQ"));
		posicionConsolidada.prestamos = response.crear(Prestamos.class, response.objetos("prestamos"));
		posicionConsolidada.seguros = response.crear(Seguros.class, response.objetos("segurosProductos"));

		posicionConsolidada.existenErrores = false;

		for (Objeto item : response.objetos("errores")) {
			String codigo = item.string("codigo");
			posicionConsolidada.cuentas = codigo.equals("cuentas") ? null : posicionConsolidada.cuentas;
			posicionConsolidada.plazosFijos = codigo.equals("plazosFijos") ? null : posicionConsolidada.plazosFijos;
			posicionConsolidada.tarjetasCredito = codigo.equals("tarjetasCredito") ? null : posicionConsolidada.tarjetasCredito;
			posicionConsolidada.tarjetasDebito = codigo.equals("tarjetasDebito") ? null : posicionConsolidada.tarjetasDebito;
			posicionConsolidada.cajasSeguridad = codigo.equals("cajasSeguridad") ? null : posicionConsolidada.cajasSeguridad;
			posicionConsolidada.prestamos = codigo.equals("prestamos") ? null : posicionConsolidada.prestamos;
			posicionConsolidada.seguros = codigo.equals("segurosProductos") ? null : posicionConsolidada.seguros;
			posicionConsolidada.existenErrores = true;
		}

		return posicionConsolidada;
	}

	/* ========== METODOS ========== */
	public Boolean tieneProductos() {
		Boolean tieneProductos = false;
		tieneProductos |= cuentas.size() > 0;
		tieneProductos |= plazosFijos.size() > 0;
		tieneProductos |= tarjetasCredito.size() > 0;
		tieneProductos |= tarjetasDebito.size() > 0;
		tieneProductos |= cuentasComitentes.size() > 0;
		tieneProductos |= cuentasCuotapartistas.size() > 0;
		tieneProductos |= cajasSeguridad.size() > 0;
		tieneProductos |= paquetes.size() > 0;
		tieneProductos |= prestamos.size() > 0;
		tieneProductos |= seguros.size() > 0;
		return tieneProductos;
	}

	public Boolean tieneCuenta() {
		return cuentas.size() > 0;
	}

	public Boolean tieneTarjetaDebito() {
		return tarjetasDebito.size() > 0;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		PosicionConsolidada datos = get(contexto, "133366");
		imprimirResultado(contexto, datos);
	}
}

