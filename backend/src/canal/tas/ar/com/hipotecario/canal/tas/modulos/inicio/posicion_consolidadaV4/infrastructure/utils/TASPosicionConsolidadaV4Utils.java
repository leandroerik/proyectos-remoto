package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.infrastructure.utils;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.ApiException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class TASPosicionConsolidadaV4Utils {

    //obtener descripcion de moneda
    public static String verificarMoneda(Integer codigoMoneda){
        String monedaString = "PESOS";
        if(codigoMoneda != 80){
            monedaString = codigoMoneda == 2 ? "USD" : null;
        }
        return monedaString;
    }

    //parsear de dd/mm/yyyy a yyyy-mm-dd
    public static String formatearFechaApiV4(String fecha){
        try{
            DateTimeFormatter formatoEntrada = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter formatoSalida = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate fechaParse = LocalDate.parse(fecha, formatoEntrada);
            return fechaParse.format(formatoSalida);
        }catch (Exception e){
            return null;
        }
    }
    public static <T> List<T> validarLista(List<T> lista) {
        return lista.isEmpty() ? null : lista;
    }

    public static Objeto mapearError(ApiException apiException){
        Objeto errorDesc = new Objeto();
        errorDesc.set("url", apiException.request.url());
        errorDesc.set("codigoHttp", apiException.response.codigoHttp);
        errorDesc.set("codigo", apiException.response.get("codigo"));
        errorDesc.set("detalle", apiException.response.get("detalle"));
        errorDesc.set("mensajeAlUsuario", apiException.response.get("mensajeAlUsuario"));
        errorDesc.set("mensajeAlDesarrollador", apiException.response.get("mensajeAlDesarrollador"));
        errorDesc.set("masInformacion", apiException.response.get("masInformacion"));
        return errorDesc;
    }

}
