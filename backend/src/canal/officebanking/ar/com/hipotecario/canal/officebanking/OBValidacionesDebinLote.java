package ar.com.hipotecario.canal.officebanking;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.officebanking.dto.ErrorArchivoOB;
import ar.com.hipotecario.canal.officebanking.dto.debinLote.ArchivoDebinLoteDTO;
import ar.com.hipotecario.canal.officebanking.util.StringUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OBValidacionesDebinLote {
    public static Objeto validarNombreArchivo(String submittedFileName) {
        return new Objeto().set("estado","0");
    }

    public static Objeto validarHeader( ArchivoDebinLoteDTO.Header header,String convenioSeleccionado) {
        ErrorArchivoOB errores = new ErrorArchivoOB();
        if (!StringUtil.eliminarCerosIzquierda(header.nroConvenio).equals(StringUtil.eliminarCerosIzquierda(convenioSeleccionado))){
            return errores.setErroresArchivo("El convenio del header no coincide con el seleccionado","Los convenios deben coincidir",0,null);
        }
        if (!header.nroEmpresa.matches("^[0]+$")){
            return errores.setErroresArchivo("Nro Empresa invalido","Debe informar unicamente ceros",0,null);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        if (!header.fechaGeneracion.isBlank()){
            try {
                LocalDate fecha = LocalDate.parse(header.fechaGeneracion,formatter);
                if (fecha.isBefore(LocalDate.now())){
                    return errores.setErroresArchivo("Fecha invalida","La fecha de generacion no puede ser anterior a la actual",0,null);
                }
            }
            catch (Exception e){
                return errores.setErroresArchivo("Fecha invalida","El formato de la fecha de generacion debe ser AAAAMMDD",0,null);
            }
        }
        else {
            return errores.setErroresArchivo("Fecha invalida","Debe ingresar fecha de generacion",0,null);
        }

        if (!header.moneda.equals("080")&&!header.moneda.equals("002")){
            return errores.setErroresArchivo("Moneda invalida","La moneda debe coincidir con la del convenio",0,null);
        }

        if (!header.tipoMov.equals("07")){
            return errores.setErroresArchivo("Tipo movimiento invalido","Debe ingresar '07'",0,null);
        }
        return new Objeto().set("estado","0");
    }

    public static Objeto validarDetalle(ArchivoDebinLoteDTO.Body item, int numLinea, String convenioHeader) {
        ErrorArchivoOB errores = new ErrorArchivoOB();

        if (!StringUtil.eliminarCerosIzquierda(item.nroConvenio).equals(StringUtil.eliminarCerosIzquierda(convenioHeader))){
            return errores.setErroresArchivo("El convenio del item no coincide con la cabecera","Los convenios deben coincidir",numLinea,"nroConvenio");
        }
        if (!item.moneda.equals("080")&&!item.moneda.equals("002")){
            return errores.setErroresArchivo("Moneda invalida","La moneda debe coincidir con la del convenio",numLinea,"moneda");
        }
        if (Long.parseLong(item.importe)==0){
            return errores.setErroresArchivo("Importe invalido","El importe debe ser mayor a 0",numLinea,"moneda");
        }
        List<String> vigenciasPosibles = List.of("01","03","06","12","24","48","72");
        if (!vigenciasPosibles.contains(item.vigencia)){
            return errores.setErroresArchivo("Vigencia invalida","Vigencia invalida",numLinea,"Vigencia");
        }
        List<String> conceptosPosibles = List.of("ALQ","CUO","EXP","FAC","PRE","SEG","HON","HAB","VAR");
        if (!conceptosPosibles.contains(item.concepto)){
            return errores.setErroresArchivo("Concepto invalido","Concepto invalido",numLinea,"Concepto");
        }
        return new Objeto().set("estado","0");
    }
}
