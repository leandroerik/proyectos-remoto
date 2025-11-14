package ar.com.hipotecario.backend.servicio.api.catalogo;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.servicio.api.catalogo.Bancos.Banco;
import ar.com.hipotecario.backend.servicio.api.catalogo.CategoriasCausal.CategoriaCausal;
import ar.com.hipotecario.backend.servicio.api.catalogo.SubcategoriasCausal.SubcategoriaCausal;
import ar.com.hipotecario.backend.servicio.api.catalogo.SucursalesCliente.SucursalCliente;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

// http://api-catalogo-microservicios-homo.appd.bh.com.ar/swagger-ui.html
public class ApiCatalogo {

    /* ========== Api de catálogo ========== */

    // GET /v1/calendario/{fecha}
    public static Futuro<DiaBancario> diaBancario(Contexto contexto, Fecha fecha) {
        return Util.futuro(() -> DiaBancario.get(contexto, fecha));
    }

    // GET /v1/codigos/actividades
    public static Futuro<CatalogoActividades> actividades(Contexto contexto) {
        return Util.futuro(() -> CatalogoActividades.get(contexto));
    }

    // GET /v1/codigos/documentosrewards
    public static Futuro<DocumentosRewards> documentosRewards(Contexto contexto) {
        return Util.futuro(() -> DocumentosRewards.get(contexto));
    }

    // TODO: GET /v1/codigos/estadosciviles

    // TODO: GET /v1/codigos/provincias

    // GET /v1/codigos/rechazos_ar
    public static Futuro<RechazosAr> rechazosAr(Contexto contexto) {
        return Util.futuro(() -> RechazosAr.get(contexto));
    }

    // GET /v1/codigos/rechazos_ed
    public static Futuro<RechazosEd> rechazosEd(Contexto contexto) {
        return Util.futuro(() -> RechazosEd.get(contexto));
    }

    // GET /v1/codigos/sexos
    public static Futuro<Sexos> sexos(Contexto contexto) {
        return Util.futuro(() -> Sexos.get(contexto));
    }

    // GET /v1/codigosBancos
    public static Futuro<Bancos> bancos(Contexto contexto) {
        return Util.futuro(() -> Bancos.get(contexto));
    }

    public static Futuro<Banco> bancos(Contexto contexto, String codigo) {
        return Util.futuro(() -> Bancos.get(contexto, codigo));
    }

    /* ========== Catalogo ========== */

    // GET /v1/cpSucursal
    public static Futuro<SucursalCliente> sucursalesCliente(Contexto contexto, String idCobis) {
        return Util.futuro(() -> SucursalesCliente.get(contexto, idCobis));
    }

    // GET /v1/estadoCiviles
    public static Futuro<EstadosCiviles> estadosCiviles(Contexto contexto) {
        return Util.futuro(() -> EstadosCiviles.get(contexto));
    }

    // GET /v1/nivelEstudios
    public static Futuro<NivelesEstudio> nivelEstudios(Contexto contexto) {
        return Util.futuro(() -> NivelesEstudio.get(contexto));
    }

    // GET /v1/obraSociales
    public static Futuro<ObrasSociales> obraSociales(Contexto contexto) {
        return Util.futuro(() -> ObrasSociales.get(contexto));
    }

    // GET /v1/situacionViviendas
    public static Futuro<SituacionesVivienda> situacionesVivienda(Contexto contexto) {
        return Util.futuro(() -> SituacionesVivienda.get(contexto));
    }

    // GET /v1/sucursales
    public static Futuro<Sucursales> sucursales(Contexto contexto) {
        return Util.futuro(() -> Sucursales.get(contexto));
    }

    public static Futuro<Sucursales> sucursales(Contexto contexto, String provincia,
            String sucursal, String tipoSucursal) {
        return Util.futuro(() -> Sucursales.get(contexto, provincia, sucursal, tipoSucursal));
    }

    public static Futuro<SucursalesOB> sucursalesOB(Contexto contexto, String provincia,
            String sucursal, String tipoSucursal) {
        return Util.futuro(() -> SucursalesOB.get(contexto, provincia, sucursal, tipoSucursal));
    }

    public static Futuro<SucursalesOBV2> sucursalesOBV2(ContextoOB contexto, String sucursal) {
        return Util.futuro(() -> SucursalesOBV2.post(contexto, sucursal));
    }

    // TODO: GET /v1/sucursalesPorTipoTurno

    // GET /v1/unionesCiviles
    public static Futuro<UnionesCiviles> unionesCiviles(Contexto contexto, String estadoCivil) {
        return Util.futuro(() -> UnionesCiviles.get(contexto, estadoCivil));
    }

    /* ========== Catalogo Ramos ========== */

    // GET /v1/ramos
    public static Futuro<Ramos> ramos(Contexto contexto) {
        return Util.futuro(() -> Ramos.get(contexto));
    }

    // GET /v1/ramos/{idRamo}/profesiones
    public static Futuro<Profesiones> profesiones(Contexto contexto, String idRamo) {
        return Util.futuro(() -> Profesiones.get(contexto, idRamo));
    }

    // GET /v1/ramos/profesiones/{idProfesion}/cargos
    public static Futuro<Cargos> cargos(Contexto contexto, String idProfesion) {
        return Util.futuro(() -> Cargos.get(contexto, idProfesion));
    }

    /* ========== Categoria causal ========== */

    // GET /v1/CategoriaCausal/{codigo}
    public static Futuro<CategoriaCausal> categoriaCausal(Contexto contexto, String codigo) {
        return Util.futuro(() -> CategoriasCausal.get(contexto, codigo));
    }

    // GET /v1/CategoriasCausales
    public static Futuro<CategoriasCausal> categoriasCausal(Contexto contexto) {
        return Util.futuro(() -> CategoriasCausal.get(contexto));
    }

    // GET /v1/SubcategoriaCausal/{codigo}/{codigoCategoria}
    public static Futuro<SubcategoriaCausal> subcategoriaCausal(Contexto contexto, String causal,
            String categoria) {
        return Util.futuro(() -> SubcategoriasCausal.get(contexto, causal, categoria));
    }

    // GET /v1/SubcategoriasCausales
    public static Futuro<SubcategoriasCausal> subcategoriasCausal(Contexto contexto) {
        return Util.futuro(() -> SubcategoriasCausal.get(contexto));
    }

    /* ========== Catálogo de Paises, Provincias y Ciudades ========== */

    // TODO: GET /v1/ciudades

    // GET /v1/cp
    public static Futuro<CodigosPostales> codigosPostales(Contexto contexto, String codigoPostal,
            String ciudad, String provincia) {
        return Util.futuro(() -> CodigosPostales.get(contexto, codigoPostal, ciudad, provincia));
    }

    // GET /v1/paises
    public static Futuro<Paises> paises(Contexto contexto) {
        return Util.futuro(() -> Paises.get(contexto));
    }

    // GET /v1/paises/{idPais}/provincias
    public static Futuro<Provincias> provincias(Contexto contexto) {
        return Util.futuro(() -> Provincias.get(contexto));
    }

    // GET /v1/paises/{idPais}/provincias/{idProvincia}/ciudades
    public static Futuro<Ciudades> ciudades(Contexto contexto, String idProvincia) {
        return Util.futuro(() -> Ciudades.get(contexto, idProvincia));
    }

    /* ========== Débito Automático Catalogos ========== */

    // GET /v1/consulta/{typeQuery}
    public static Futuro<RubrosDebitosAutomaticos> rubrosDebitosAutomaticos(Contexto contexto,
            Integer numeroPagina, Integer cantidadRegistros) {
        return Util.futuro(
                () -> RubrosDebitosAutomaticos.get(contexto, numeroPagina, cantidadRegistros));
    }

    // GET /v1/consulta/{typeQuery}
    public static Futuro<EmpresasDebitosAutomaticos> empresasDebitosAutomaticos(Contexto contexto,
            String codigoRubro, Integer numeroPagina, Integer cantidadRegistros) {
        return Util.futuro(() -> EmpresasDebitosAutomaticos.get(contexto, codigoRubro, numeroPagina,
                cantidadRegistros));
    }

    // GET /v1/consulta/{typeQuery}
    public static Futuro<ServiciosDebitosAutomaticos> debitoAutomaticoConsultasServicios(
            Contexto contexto, String codigoRubro, String cuit, Integer numeroPagina,
            Integer cantidadRegistros) {
        return Util.futuro(() -> ServiciosDebitosAutomaticos.get(contexto, codigoRubro, cuit,
                numeroPagina, cantidadRegistros));
    }

    /* ========== Plazo Fijo Catálogos ========== */

    // GET /v1/plazoFijos/parametrias
    public static Futuro<ParametriasPlazosFijos> parametriasPlazosFijos(Contexto contexto,
            String idCobis) {
        return Util.futuro(() -> ParametriasPlazosFijos.get(contexto, idCobis));
    }

    /* ========== TC Catalogo ========== */

    // GET /v1/formapago
    public static Futuro<FormasPagos> formasPagos(Contexto contexto, String marca) {
        return Util.futuro(() -> FormasPagos.get(contexto, marca));
    }

    /* ========== TD Catálogos ========== */

    // GET /v1/cobis/reposicion
    public static Futuro<MotivosReposicionTD> motivosReposicionTD(Contexto contexto) {
        return Util.futuro(() -> MotivosReposicionTD.get(contexto));
    }
}
