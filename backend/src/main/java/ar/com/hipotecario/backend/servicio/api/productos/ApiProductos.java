package ar.com.hipotecario.backend.servicio.api.productos;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.conector.api.ApiResponse;

import java.util.List;

//http://api-productos-microservicios-homo.appd.bh.com.ar/swagger-ui.html
public class ApiProductos extends Api {

    /* ========== Impuesto Controller ========== */

    // TODO: POST /v1/impuestos

    /* ========== Tasas Activas Controller ========== */

    // TODO: POST /v1/tasas/activas

    /* ========== Campania Controller ========== */

    // TODO: GET /v1/campania

    /* ========== Garantias Controller ========== */

    // TODO: GET /v1/garantias

    /* ========== Posicion Consolidada Controller ========== */

    // TODO: GET @Deprecated /v1/posicionconsolidada

    // GET /v2/posicionconsolidada
    public static Futuro<PosicionConsolidada> posicionConsolidada(Contexto contexto, String idCobis, Boolean adicionales, Boolean firmaconjunta, Boolean firmantes, String tipoestado, String cuit) {
        return futuro(() -> PosicionConsolidada.get(contexto, idCobis, adicionales, firmaconjunta, firmantes, tipoestado, cuit));
    }

    public static Futuro<PosicionConsolidada> posicionConsolidada(Contexto contexto, String idCobis, Boolean adicionales, Boolean firmaconjunta, Boolean firmantes, String tipoestado) {
        return futuro(() -> PosicionConsolidada.get(contexto, idCobis, adicionales, firmaconjunta, firmantes, tipoestado));
    }

    public static Futuro<PosicionConsolidada> posicionConsolidada(Contexto contexto, String idCobis, Boolean adicionales, Boolean firmaconjunta, Boolean firmantes) {
        return futuro(() -> PosicionConsolidada.get(contexto, idCobis, adicionales, firmaconjunta, firmantes));
    }

    public static Futuro<PosicionConsolidada> posicionConsolidada(Contexto contexto, String idCobis) {
        return futuro(() -> PosicionConsolidada.get(contexto, idCobis));
    }
    public static Futuro<List<PosicionConsolidadV4>> posicionConsolidadaV4(Contexto contexto, String idCobis) {
        return futuro(() -> PosicionConsolidadV4.getV4(contexto, idCobis));
    }

    // GET /v4/posicionconsolidada
    public static Futuro<PosicionConsolidadaV4> posicionConsolidadaV4(Contexto contexto, String idCobis, Boolean adicionales, Boolean firmaconjunta, Boolean firmantes, String tipoestado, String cuit) {
        return futuro(() -> PosicionConsolidadaV4.get(contexto, idCobis, adicionales, firmaconjunta, firmantes, tipoestado, cuit));
    }

    public static Futuro<PosicionConsolidadaV4> posicionConsolidadaV4(Contexto contexto, String idCobis, Boolean adicionales, Boolean firmaconjunta, Boolean firmantes, String tipoestado) {
        return futuro(() -> PosicionConsolidadaV4.get(contexto, idCobis, adicionales, firmaconjunta, firmantes, tipoestado));
    }

    public static Futuro<PosicionConsolidadaV4> posicionConsolidadaV4(Contexto contexto, String idCobis, Boolean adicionales, Boolean firmaconjunta, Boolean firmantes) {
        return futuro(() -> PosicionConsolidadaV4.get(contexto, idCobis, adicionales, firmaconjunta, firmantes));
    }

    public static Futuro<PosicionConsolidadaV4> posicionConsolidadaCV4(Contexto contexto, String idCobis) {
        return futuro(() -> PosicionConsolidadaV4.get(contexto, idCobis));
    }

    public static Futuro<PosicionConsolidadaV4> posicionConsolidadaV4(Contexto contexto, String idCobis, String cuit) {
        return futuro(() -> PosicionConsolidadaV4.get(contexto, idCobis, cuit));
    }


    /* ========== Clientes Controller ========== */

    // TODO: GET /v1/clientes/{id}/garantias

    // TODO: GET /v1/clientes/{id}/impuestos

    /* ========== Producto Controller ========== */

    // TODO: GET /v1/{cuenta}/integrantes

    // TODO: GET /v1/domicilios/{id-cliente}/productos

    // GET /v1/productos
    public static Futuro<Productos> productos(Contexto contexto, String idCobis) {
        return futuro(() -> Productos.get(contexto, idCobis, true));
    }

    public static Futuro<Productos> productos(Contexto contexto, String idCobis, Boolean cache) {
        return futuro(() -> Productos.get(contexto, idCobis, cache));
    }

	public static Futuro<ApiResponse> relacionClienteProducto(Contexto contexto, Objeto relacion) {
		return futuro(() -> CuentasOB.relacionClienteProducto(contexto, relacion));
	}

	// TODO: POST /v1/productos/{id-producto}/telefono/
    public static Futuro<Productos> productosVigentes(Contexto contexto, String idCobis) {
        return futuro(() -> Productos.getVigentes(contexto, idCobis));
    }

    // TODO: POST /v1/productos/{id-producto}/telefono/

    // TODO: GET /v1/productos/{id}/clientes

    // TODO: GET /v1/productos/{id}/domicilios

    // TODO: DELETE /v1/productos/{id}/domicilios

    // TODO: POST /v1/productos/{id}/domicilios/

    // TODO: PATCH /v1/productos/{id}/domicilios/{id-domicilio}

    // TODO: GET /v1/productos/{id}/emisionvigente

    // TODO: POST /v1/productos/{id}/emisionvigente

    // TODO: POST /v1/productos/crucepagos

    // TODO: DELETE /v1/productos/telefono

    /* ========== Relacion Controller ========== */

    // TODO: POST /v1/relaciones

    // TODO: DELETE /v1/relaciones

    // TODO: PATCH /v1/relaciones

}
