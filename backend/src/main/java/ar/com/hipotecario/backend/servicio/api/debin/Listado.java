package ar.com.hipotecario.backend.servicio.api.debin;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class Listado extends ApiObjeto {
    public Integer pagina;
    public Integer paginasTotales;
    public Integer paginas_totales;
    public Integer tamano;

    public Listado(Integer pagina, Integer paginasTotales, Integer paginas_totales, Integer tamano) {
        this.pagina = pagina;
        this.paginasTotales = paginasTotales;
        this.paginas_totales = paginas_totales;
        this.tamano = tamano;
    }
}
