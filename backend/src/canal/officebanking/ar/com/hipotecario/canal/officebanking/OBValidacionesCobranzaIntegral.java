package ar.com.hipotecario.canal.officebanking;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.util.CuitUtil;
import ar.com.hipotecario.canal.officebanking.dto.ErrorArchivoOB;
import ar.com.hipotecario.canal.officebanking.dto.cobranzaIntegral.ArchivoCobranzaIntegralDTO;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioCobranzaIntegralOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.CobranzaIntegralOB;
import net.bytebuddy.asm.Advice;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static ar.com.hipotecario.canal.officebanking.OBManejoArchivos.blobToString;

public class OBValidacionesCobranzaIntegral extends ModuloOB {
    public static Objeto validarNombreArchivo(String nombreArchivo) {
        ErrorArchivoOB errores = new ErrorArchivoOB();
        if (nombreArchivo.contains(" ") || nombreArchivo.contains("(")) {
            return errores.setErroresArchivo("Nombre archivo inválido.", "El nombre del archivo no puede contener espacios ni paréntesis.", null, null);
        }
        if (!nombreArchivo.startsWith("CLI")) {
            return errores.setErroresArchivo("Nombre archivo inválido.", "El nombre del archivo debe empezar en CLI.", null, null);
        }

        return respuesta("0");
    }

    public static Objeto validarHeader(ContextoOB contexto, ArchivoCobranzaIntegralDTO.Header header) {
        ErrorArchivoOB errores = new ErrorArchivoOB();
        if (!header.codigoRegistro.equals("H")||header.codigoRegistro.isBlank()) {
            return errores.setErroresArchivo("Codigo registro invalido", "Codigo registro debe ser 'H'", 0, null);
        }
        if (!header.gcr.isBlank()&&!header.gcr.substring(0,3).equals("GCR")){
            return errores.setErroresArchivo("GCR invalido","GCR debe estar vacio",0,null);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        if (!header.fechaProceso.isBlank())
        {
            try {
                LocalDate.parse(header.fechaProceso,formatter);
            }
            catch (Exception e){
                return errores.setErroresArchivo("Fecha invalida","Fecha invalida",0,null);
            }
        }
        else
        {
            return errores.setErroresArchivo("Fecha invalida","Fecha invalida",0,null);
        }

        if (!header.horaProceso.isBlank()&&header.horaProceso.length()>5){
            int horas = Integer.parseInt(header.horaProceso.substring(0, 2));
            int minutos = Integer.parseInt(header.horaProceso.substring(2, 4));
            int segundos = Integer.parseInt(header.horaProceso.substring(4, 6));
            try {LocalTime horaLocal = LocalTime.of(horas, minutos, segundos);
            } catch (Exception e){
                return errores.setErroresArchivo("Hora invalida","Hora invalida",0,null);
            }
            try {
                validarHora(contexto, header.fechaProceso, header.horaProceso);
            } catch (Exception e) {
                return errores.setErroresArchivo("Hora duplicada", e.getMessage(), 0, null);
            }

        }
        else
        {
            return errores.setErroresArchivo("Hora invalida","Hora invalida",0,null);

        }

        if (!header.filler.isBlank())
        {
            return errores.setErroresArchivo("Filler invalido","Filler invalido",0,null);
        }


        return new Objeto().set("estado","0");
    }

    public static Objeto validarDetalle(ArchivoCobranzaIntegralDTO.Body item, ContextoOB contexto, int numLinea,String fechaProceso) {
        ErrorArchivoOB errores = new ErrorArchivoOB();
        if (item.codigoRegistro.isBlank()||!item.codigoRegistro.equals("D"))
        {
            return errores.setErroresArchivo("Codigo registro invalido","Codigo registro invalido",numLinea,null);
        }
        if (item.codigoServicio.isBlank())
        {
            return errores.setErroresArchivo("Codigo servicio invalido","Codigo servicio invalido",numLinea,null);
        }
        if (item.numeroDepositante.isBlank())
        {
            return errores.setErroresArchivo("Numero de depositante invalido","Numero de depositante invalido",numLinea,null);

        }
        if (item.numeroComprobante.isBlank())
        {
            return errores.setErroresArchivo("Numero de comprobante invalido","Numero de comprobante invalido",numLinea,null);
        }
        if (item.tipoDocumento.isBlank()){
            return errores.setErroresArchivo("Tipo de documento invalido","Tipo de documento invalido",numLinea,null);
        }
        if (item.numeroDocumento.isBlank()){
            return errores.setErroresArchivo("Numero de documento invalido","Numero de documento invalido",numLinea,null);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        if (item.fechaDesde.isBlank())
        {
            return errores.setErroresArchivo("Fecha desde invalida","Fecha desde invalida",numLinea,null);
        }
        else if (!item.fechaDesde.equals("00000000"))
        {

           try {
               LocalDate fechaDesde = LocalDate.parse(item.fechaDesde, formatter);
               if (fechaDesde.isBefore(LocalDate.parse(fechaProceso,formatter)))
               {
                   return errores.setErroresArchivo("Fecha desde invalida","Fecha desde invalida",numLinea,null);
               }
           }
           catch (Exception e)
           {
               return errores.setErroresArchivo("Fecha desde invalida","Fecha desde invalida",numLinea,null);
           }
        }
        if (item.fechaHasta.isBlank())
        {
            return errores.setErroresArchivo("Fecha hasta invalida","Fecha hasta invalida",numLinea,null);
        }
        else if (!item.fechaHasta.equals("00000000"))
        {

            try {
                LocalDate fechaHasta = LocalDate.parse(item.fechaHasta, formatter);
                if (fechaHasta.isBefore(LocalDate.parse(item.fechaDesde,formatter)))
                {
                    return errores.setErroresArchivo("Fecha hasta invalida","Fecha hasta invalida",numLinea,null);
                }
            }
            catch (Exception e)
            {
                return errores.setErroresArchivo("Fecha hasta invalida","Fecha hasta invalida",numLinea,null);
            }
        }

        if (item.importe1.isBlank())
        {
            return errores.setErroresArchivo("Importe 1 invalido","Importe 1 invalido",numLinea,null);
        }
        else{
            try {
                int importe1 = Integer.parseInt(item.importe1);
                if (importe1<1)
                {
                    return errores.setErroresArchivo("Importe 1 invalido","Importe 1 invalido",numLinea,null);
                }
            }catch (Exception e)
            {
                return errores.setErroresArchivo("Importe 1 invalido","Importe 1 invalido",numLinea,null);
            }
        }
        if (!item.fechaVencimiento1.isBlank())
        {
            try {
                LocalDate fechaVenc1 = LocalDate.parse(item.fechaVencimiento1,formatter);
                if (fechaVenc1.isBefore(LocalDate.parse(item.fechaDesde,formatter))){
                    return errores.setErroresArchivo("Fecha vencimiento 1 invalida","Fecha vencimiento 1 invalida",numLinea,null);
                }
            }
            catch (Exception e){
                return errores.setErroresArchivo("Fecha vencimiento 1 invalida","Fecha vencimiento 1 invalida",numLinea,null);
            }
        }
        else
        {
            return errores.setErroresArchivo("Fecha vencimiento 1 invalida","Fecha vencimiento 1 invalida",numLinea,null);
        }

        if (item.importe2.isBlank())
        {
            return errores.setErroresArchivo("Importe 2 invalido","Importe 2 invalido",numLinea,null);
        }
        else{
            try {
                Integer.parseInt(item.importe2);
            }catch (Exception e)
            {
                return errores.setErroresArchivo("Importe 2 invalido","Importe 2 invalido",numLinea,null);
            }
        }

        if (!item.fechaVencimiento2.isBlank())
        {
            if (Integer.parseInt(item.importe2)>0)
            {
                try {
                    LocalDate fechaVenc2 = LocalDate.parse(item.fechaVencimiento2, formatter);
                    if (fechaVenc2.isBefore(LocalDate.parse(item.fechaDesde,formatter))||fechaVenc2.isBefore(LocalDate.parse(item.fechaVencimiento1,formatter))){
                        return errores.setErroresArchivo("Fecha vencimiento 2 invalida","Fecha vencimiento 2 invalida",numLinea,null);
                    }
                }
            catch (Exception e){
                return errores.setErroresArchivo("Fecha vencimiento 2 invalida","Fecha vencimiento 2 invalida",numLinea,null);
            }
            }

        }
        else if (Integer.parseInt(item.importe2)>0)
        {
            return errores.setErroresArchivo("Fecha vencimiento 2 invalida","Fecha vencimiento 2 invalida",numLinea,null);
        }

        if (item.importe3.isBlank())
        {
            return errores.setErroresArchivo("Importe 3 invalido","Importe 3 invalido",numLinea,null);
        }
        else{
            try {
                Integer.parseInt(item.importe3);
            }catch (Exception e)
            {
                return errores.setErroresArchivo("Importe 3 invalido","Importe 3 invalido",numLinea,null);
            }
        }

        if (!item.fechaVencimiento3.isBlank())
        {
            if (Integer.parseInt(item.importe3)>0)
            {
                try {
                    LocalDate fechaVenc3 = LocalDate.parse(item.fechaVencimiento3, formatter);
                    if (fechaVenc3.isBefore(LocalDate.parse(item.fechaDesde,formatter))||fechaVenc3.isBefore(LocalDate.parse(item.fechaVencimiento1,formatter))||fechaVenc3.isBefore(LocalDate.parse(item.fechaVencimiento2,formatter))){
                        return errores.setErroresArchivo("Fecha vencimiento 3 invalida","Fecha vencimiento 3 invalida",numLinea,null);
                    }
                }
                catch (Exception e){
                    return errores.setErroresArchivo("Fecha vencimiento 3 invalida","Fecha vencimiento 3 invalida",numLinea,null);
                }
            }

        }
        else if (Integer.parseInt(item.importe3)>0)
        {
            return errores.setErroresArchivo("Fecha vencimiento 3 invalida","Fecha vencimiento 3 invalida",numLinea,null);
        }

        if (item.nombreDepositante.isBlank())
        {
            return errores.setErroresArchivo("Nombre depositante invalido","Nombre depositante invalido",numLinea,null);
        }

        if (item.moneda.isBlank())
        {
            return errores.setErroresArchivo("Moneda invalida","Moneda invalida",numLinea,null);
        }
        if (item.cuitEmpresa.isBlank())
        {
            return errores.setErroresArchivo("CUIT empresa invalido","CUIT empresa invalido",numLinea,null);
        }
        return new Objeto().set("estado","0");

    }
    public static void validarHora(ContextoOB contexto, String fechaProceso, String horaProceso) throws Exception {
        LocalDateTime fechaHora = LocalDateTime.parse(fechaProceso + horaProceso, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        LocalDateTime inicioDia = fechaHora.toLocalDate().atStartOfDay();
        LocalDateTime finDia = inicioDia.plusDays(1);

        List<CobranzaIntegralOB> registros = new ServicioCobranzaIntegralOB(contexto).buscarPorFechaHora(inicioDia, finDia, contexto.sesion().empresaOB.emp_codigo).tryGet();

        for (CobranzaIntegralOB registro : registros) {
            String contenidoArchivo = blobToString(registro.archivo);
            ArchivoCobranzaIntegralDTO.Header nuevaCabecera = ArchivoCobranzaIntegralDTO.getHeader(contenidoArchivo.trim());
            LocalDateTime fechaArchivoBase = LocalDateTime.parse(nuevaCabecera.fechaProceso + nuevaCabecera.horaProceso, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            if (fechaHora.isEqual(fechaArchivoBase)) {
                throw new Exception("Ya existe un archivo con la misma fecha y hora.");
            }
        }
    }

}
