package ar.com.hipotecario.backend.servicio.api.productos;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

// API-Productos_PosicionConsolidada
public class PosicionConsolidadaV4 extends ApiObjeto {

	private static final Logger log = LoggerFactory.getLogger(PosicionConsolidadaV4.class);
	/* ========== ATRIBUTOS ========== */
	public CuentasV4 cuentas = new CuentasV4();
	public PlazosFijosV4 plazosFijos = new PlazosFijosV4();
	public TarjetasCreditoV4 tarjetasCredito = new TarjetasCreditoV4();
	public TarjetasDebitoV4 tarjetasDebito = new TarjetasDebitoV4();
	public CuentasComitentesV4 cuentasComitentes = new CuentasComitentesV4();
	public CuentasCuotapartistasV4 cuentasCuotapartistas = new CuentasCuotapartistasV4();
	public CajasSeguridadV4 cajasSeguridad = new CajasSeguridadV4();
	public PaquetesV4 paquetes = new PaquetesV4();
	public PrestamosV4 prestamos = new PrestamosV4();
	public SegurosV4 seguros = new SegurosV4();

	/* ========== SERVICIOS ========== */
	public static PosicionConsolidadaV4 get(Contexto contexto, String idCobis) {
		return get(contexto, idCobis, true, true, true, "vigente");
	}

	public static PosicionConsolidadaV4 get(Contexto contexto, String idCobis, String cuit) {
		return get(contexto, idCobis,true, true, true, "vigente", cuit);
	}

	public static PosicionConsolidadaV4 get(Contexto contexto, String idCobis, Boolean adicionales, Boolean firmaconjunta, Boolean firmantes) {
		return get(contexto, idCobis, adicionales, firmaconjunta, firmantes, "vigente");
	}

	public static PosicionConsolidadaV4 get(Contexto contexto, String idCobis, Boolean adicionales, Boolean firmaconjunta, Boolean firmantes, String tipoestado) {
		return get(contexto, idCobis, adicionales, firmaconjunta, firmantes, tipoestado);
	}

	public static PosicionConsolidadaV4 get(Contexto contexto, String idCobis, Boolean adicionales, Boolean firmaconjunta, Boolean firmantes, String tipoestado, String cuit) {
		ApiRequest request = new ApiRequest("PosicionConsolidada", "productos", "GET", "/v4/posicionconsolidada", contexto);
		request.query("idcliente", idCobis);
		request.query("adicionales", adicionales);
		request.query("firmaconjunta", firmaconjunta);
		request.query("firmantes", firmantes);
		request.query("tipoestado", tipoestado); // todos | vigente | cancelado
		request.query("cuit", cuit);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);

		PosicionConsolidadaV4 posicionConsolidada = new PosicionConsolidadaV4 ();

		for (Objeto item : response.objetos()) {
			if (item.string("tipo").equals("AHO") || item.string("tipo").equals("CTE")) {
				posicionConsolidada.cuentas.add(response.crear(CuentasV4.CuentaV4.class, item));
			}
			if (item.string("tipo").equals("PFI")) {
				posicionConsolidada.plazosFijos.add(response.crear(PlazosFijosV4.PlazoFijoV4.class, item));
			}

			if (item.string("tipo").equals("ATM")) {
				posicionConsolidada.tarjetasDebito.add(response.crear(TarjetasDebitoV4.TarjetaDebitoV4.class, item));
			}

			if (item.string("tipo").equals("UNI")) {
				posicionConsolidada.cuentasComitentes.add(response.crear(CuentasComitentesV4.CuentaComitenteV4.class, item));
			}

			if (item.string("tipo").equals("RJA")) {
				posicionConsolidada.cuentasCuotapartistas.add(response.crear(CuentasCuotapartistasV4.CuentaCuotapartistaV4.class, item));
			}

			if (item.string("tipo").equals("CSG")) {
				posicionConsolidada.cajasSeguridad.add(response.crear(CajasSeguridadV4.CajaSeguridadV4.class, item));
			}

			if (item.string("tipo").equals("PAQ")) {
				posicionConsolidada.paquetes.add(response.crear(PaquetesV4.PaqueteV4.class, item));
			}

			if (item.string("tipo").equals("CCA") || item.string("tipo").equals("NSP")) {
				posicionConsolidada.prestamos.add(response.crear(PrestamosV4.PrestamoV4.class, item));
			}
		}
		if(posicionConsolidada.tarjetasCredito.isEmpty()) {
			ContextoOB contextoOb = new ContextoOB(contexto.canal, contexto.ambiente, idCobis);

			List<TarjetasCreditoV4.TarjetaCreditoV4> tarjetasObtenidas = TarjetasCreditoV4.getTarjetas(contextoOb, idCobis, adicionales);
			for (TarjetasCreditoV4.TarjetaCreditoV4 card : tarjetasObtenidas) {
				posicionConsolidada.tarjetasCredito.add(card);
			}
		}

		if(posicionConsolidada.seguros.isEmpty()) {
			ContextoOB contextoOb = new ContextoOB(contexto.canal, contexto.ambiente, cuit);

			List<SegurosV4.SeguroV4> segurosObtenidos = SegurosV4.getSeguros(contextoOb, cuit);
			for (SegurosV4.SeguroV4 seguro : segurosObtenidos) {
				posicionConsolidada.seguros.add(seguro);
			}
		}
		return posicionConsolidada;
	}

	/* ========== METODOS ========== */
	public Boolean tieneProductos() {
		Boolean tieneProductos = false;
		tieneProductos |= cuentas.isEmpty();
		tieneProductos |= plazosFijos.isEmpty();
		tieneProductos |= tarjetasCredito.isEmpty();
		tieneProductos |= tarjetasDebito.isEmpty();
		tieneProductos |= cuentasComitentes.isEmpty();
		tieneProductos |= cuentasCuotapartistas.isEmpty();
		tieneProductos |= cajasSeguridad.isEmpty();
		tieneProductos |= paquetes.isEmpty();
		tieneProductos |= prestamos.isEmpty();
		tieneProductos |= seguros.isEmpty();
		return tieneProductos;
	}

	public Boolean tieneCuenta() {
		return cuentas.isEmpty();
	}

	public Boolean tieneTarjetaDebito() {
		return tarjetasDebito.isEmpty();
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		PosicionConsolidadaV4 datos = get(contexto, "133366");
		imprimirResultado(contexto, datos);
	}
}
