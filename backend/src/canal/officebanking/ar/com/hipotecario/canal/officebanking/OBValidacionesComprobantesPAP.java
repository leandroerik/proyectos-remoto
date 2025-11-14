package ar.com.hipotecario.canal.officebanking;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.empresas.SubConveniosOB;
import ar.com.hipotecario.canal.officebanking.dto.ErrorArchivoOB;
import ar.com.hipotecario.canal.officebanking.dto.pagosMasivos.ArchivoComprobantesDTO;
import ar.com.hipotecario.canal.officebanking.enums.pagosMasivos.EnumRetencionTipo;
import ar.com.hipotecario.canal.officebanking.util.StringUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static ar.com.hipotecario.canal.officebanking.util.StringUtil.agregarCerosIzquierda;


public class OBValidacionesComprobantesPAP extends ModuloOB{
    public static Objeto validarNombreArchivo(String nombreArchivo) {
        ErrorArchivoOB errores = new ErrorArchivoOB();
        if (!nombreArchivo.startsWith("PC_")){
            return errores.setErroresArchivo("Nombre archivo invalido invalido", "El nombre del archivo debe comenzar con 'PC_'", null, null);
        }
        return respuesta("0");
    }

    public static Objeto validarHeader(ArchivoComprobantesDTO.Header header, SubConveniosOB.SubConvenio subConvenio) {
        ErrorArchivoOB errores = new ErrorArchivoOB();
        if (!header.registroId.equals("FH")) {
            return errores.setErroresArchivo("Registro ID invalido", "Registro ID debe ser 'FH'", 0, null);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate fechaCreacion = LocalDate.parse(header.idArchivo.substring(9, 17), formatter);
        String adherente = header.idArchivo.substring(2,9);

        if (!header.idArchivo.startsWith("PC")) {
            return errores.setErroresArchivo("ID archivo invalido", "ID de archivo debe ser 'PC'", 0, null);
        }

        if (!adherente.equals(StringUtil.padLeftWithZeros(String.valueOf(subConvenio.nroAdh),7))){
            return errores.setErroresArchivo("Numero de adherente invalido", "Numero de adherente invalido", 0, null);
        }
        String nroSubConv = agregarCerosIzquierda(subConvenio.nroSubConv);
        String nroConv = agregarCerosIzquierda(subConvenio.nroConv);

        if (!header.convenio.equals(nroConv)) {

            return errores.setErroresArchivo("Numero de convenio invalido", "El numero de convenio en el archivo no coincide con el seleccionado", 0, null);
        }

        if (!header.subconvenio.equals(nroSubConv)) {
            return errores.setErroresArchivo("Numero de subconvenio invalido", "El numero de subconvenio en el archivo no coincide con el seleccionado", 0, null);
        }
        return respuesta("0", "fecha", fechaCreacion);
    }

    public static Objeto validarHeaderPagina(ArchivoComprobantesDTO.HeaderPage item, int numLinea) {
        ErrorArchivoOB errores = new ErrorArchivoOB();
        try{
            EnumRetencionTipo.fromCodigo(Integer.valueOf(item.tipoCertificado));
        }catch (Exception e){
            return errores.setErroresArchivo("Dato invalido", "Tipo de retencion invalida", numLinea, null);
        }
        return respuesta("0");
    }
}
