package ar.com.hipotecario.canal.officebanking;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.catalogo.ApiCatalogo;
import ar.com.hipotecario.backend.servicio.api.recaudaciones.ConveniosRecaOB;
import ar.com.hipotecario.backend.util.DateUtils;
import ar.com.hipotecario.canal.officebanking.dto.ErrorArchivoOB;
import ar.com.hipotecario.canal.officebanking.dto.debitoDirecto.ArchivoDebitoDirectoDTO;
import net.bytebuddy.asm.Advice;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

import static ar.com.hipotecario.canal.officebanking.util.StringUtil.eliminarCerosIzquierda;

public class OBValidacionesDebitoDirecto extends ModuloOB {
    public static Objeto validarNombreArchivo(String nombreArchivo,String convenio){
        ErrorArchivoOB errores = new ErrorArchivoOB();
        String entConvenio;
        String fecha;
        String sec;
        String numeroSec;
        try{
            String []arrayNombre =nombreArchivo.split("_");
            entConvenio = arrayNombre[0];
            fecha = arrayNombre[1];
            sec = arrayNombre[2];
            numeroSec = arrayNombre[3].toLowerCase().replace(".txt","");
        }catch (Exception e){
            return errores.setErroresArchivo("Nombre archivo inválido", "Formato de archivo invalido", null, null);
        }
        String ent = null;
        try{
            ent = entConvenio.substring(0,3);
        } catch (Exception e){
            return errores.setErroresArchivo("Nombre archivo inválido", "El nombre del archivo debe empezar con 'ent'", null, null);
        }
        if (!ent.equals("ent")){
            return errores.setErroresArchivo("Nombre archivo inválido", "El nombre del archivo debe empezar con 'ent'", null, null);
        }
        String convenioNombre = null;
        try{
             convenioNombre =  entConvenio.substring(3,nombreArchivo.split("_")[0].length());
        } catch (Exception e){
            return errores.setErroresArchivo("Nombre archivo inválido", "El convenio del archivo no coincide con el ingresado", null, null);
        }

        if (!Integer.valueOf(convenioNombre).toString().equals(Integer.valueOf(convenio).toString())){
            return errores.setErroresArchivo("Nombre archivo inválido", "El convenio del archivo no coincide con el ingresado", null, null);
        }
//        try {
//            String fecha = nombreArchivo.substring(8,14);
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
//            LocalDate.parse(fecha,formatter);
//        } catch (Exception e){
//            return errores.setErroresArchivo("Nombre archivo inválido", "Debe ingresar una fecha valida en en formato DDMMAA", null, null);
//        }
//
//        if (DateUtils.esFechaMenorAlDia(DateUtils.getDateWithFormat("yyMMdd", nombreArchivo.substring(8,14), Locale.ENGLISH))) {
//        	return errores.setErroresArchivo("Nombre archivo inválido", "La fecha del archivo no puede ser menor a la fecha actual.", null, null);
//        }
        

        if (!sec.equals("sec")){
            return errores.setErroresArchivo("Nombre archivo inválido", "Debe ingresar sec", null, null);
        }

        try {
            Integer.parseInt(numeroSec);
        } catch (Exception e){
            return errores.setErroresArchivo("Nombre archivo inválido", "Debe ingresar un numero de secuencial de 3 digitos", null, null);
        }
        return respuesta("0");
    }

    public static Objeto validarHeader(ArchivoDebitoDirectoDTO.Header header, ConveniosRecaOB.ConvenioReca convenio, String secuencial_nombre_archivo) {
        ErrorArchivoOB errores = new ErrorArchivoOB();

        if (!header.filler.equals("1")) {
            return errores.setErroresArchivo("Filler inválido", "El primer caracter debe ser '1'.", 0, "Filler");
        }

        if (!eliminarCerosIzquierda(header.convenio).equals(String.valueOf(convenio.convenio))) {
            return errores.setErroresArchivo("Numero de convenio inválido.", "El número de convenio en el archivo no coincide con el seleccionado.", 0, "Convenio");
        }
        
//        if (!header.secuencial.equals(secuencial_nombre_archivo)) {
//            return errores.setErroresArchivo("Secuencial inválido.", "El secuencial del nombre del archivo y el header no coinciden.", 0, "Secuencial");
//        }

        if (!header.servicio.isBlank()) {
            return errores.setErroresArchivo("Servicio inválido.", "El servicio debe ser informado en blancos.", 0, "Servicio");
        }

        if (!header.empresa.equals("00000")) {
            return errores.setErroresArchivo("Empresa inválida.", "La empresa debe ser informada como '00000'.", 0, "Empresa");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);
        try {
            LocalDate.parse(header.fechaGeneracion, formatter);
        } catch (DateTimeParseException e) {
            return errores.setErroresArchivo("Fecha de generación inválida.", "Se debe ingresar una fecha válida con formato AAAAMMDD", 0, "Fecha generación");
        }

        if (convenio.moneda != Integer.parseInt(header.monedaConvenio)) {
            return errores.setErroresArchivo("Moneda inválida.", "La moneda debe coincidir con la del convenio elegido.", 0, "Moneda convenio");
        }
        if (!header.monedaConvenio.equals("080") && !header.monedaConvenio.equals("002")) {
            return errores.setErroresArchivo("Moneda inválida.", "La moneda debe ser pesos o dólares.", 0, "Moneda convenio");
        }

        if (!header.tipoMovimiento.equals("01") && !header.tipoMovimiento.equals("05")) {
            return errores.setErroresArchivo("Tipo de movimiento inválido.", "El tipo de movimiento debe ser '01' (débitos) o '05' (reversiones de débitos).", 0, "Tipo movimiento");
        }

        if (!header.infoMonetaria.replaceAll("0", " ").trim().isBlank()) {
            return errores.setErroresArchivo("Info monetaria inválida.", "Deben informarse ceros", 0, "Inf. monetaria");
        }

//        if (!header.sinUso.trim().isBlank()) {
//            return errores.setErroresArchivo("Caracteres sin uso.", "Se deben informar blancos.", 0, "Caracteres sin uso");
//        }

        if (!header.fillerFinal.equals("0")) {
            return errores.setErroresArchivo("Filler final.", "Se debe informar cero.", 0, "Filler final");
        }

        return respuesta("0");
    }

    public static Objeto validarDetalle(ArchivoDebitoDirectoDTO.Body item, ContextoOB contexto, ConveniosRecaOB.ConvenioReca convenio, int numLinea) {
        ErrorArchivoOB errores = new ErrorArchivoOB();

        if (!item.filler.equals("0")) {
            return errores.setErroresArchivo("Filler inválido.", "El filler inicial debe ser '0'", numLinea, "Filler");
        }

        if (!eliminarCerosIzquierda(item.convenio).equals(String.valueOf(convenio.convenio))) {
            return errores.setErroresArchivo("Convenio inválido.", "El convenio debe coincidir con el informado.", numLinea, "Convenio");
        }


        //validacion empresa son todos 0000
        //if (!item.empresa.equals(convenio.))

        if (item.cuentaBancaria.length() == 22) {
            if (!item.cuentaBancaria.substring(0, 3).equals(item.codigoBanco)) {
                return errores.setErroresArchivo("Código de banco inválido.", "El código de banco debe coincidir con el de la cuenta informada para el débito.", numLinea, "Código de banco");
            }
        }

//        if (item.codigoBanco.equals("044")) {
////            if (!item.tipoCuenta.equals("3") && !item.tipoCuenta.equals("4")) {
////                return errores.setErroresArchivo("Tipo de cuenta inválida.", "Para cuenta BH el tipo de cuenta debe ser 3 (cuenta corriente) o 4 (caja de ahorro).", numLinea, "Tipo de cuenta");
////            }
//            try {
//                ApiCatalogo.sucursales(contexto).get().stream().filter(s -> s.CodSucursal.equals(item.codigoSucursalCuenta.equals("0000")?"0":eliminarCerosIzquierda(item.codigoSucursalCuenta))).findFirst().get();
//            } catch (Exception e) {
//                return errores.setErroresArchivo("Código de sucursal inválido.", "Para cuenta BH se debe ingresar una sucursal válida.", numLinea, "Código de sucursal");
//            }
//        } else {
////            if (!item.codigoSucursalCuenta.equals(item.cuentaBancaria.substring(3, 7))) {
////                return errores.setErroresArchivo("Código de sucursal inválido.", "Para cuentas no BH se deben informar los dígitos 4 a 7 del número de cuenta.", numLinea, "Código de sucursal");
////            }
////            if (!item.tipoCuenta.trim().isBlank()) {
////                return errores.setErroresArchivo("Tipo de cuenta inválida.", "Para cuentas no BH el tipo de cuenta debe ser informado en blanco.", numLinea, "Tipo de cuenta");
////            }
////            if (!item.cuentaBancaria.startsWith("0")) {
////                return errores.setErroresArchivo("Cuenta bancaria inválida.", "Para cuentas no BH se debe rellenar con un 0 a la izquierda y luego el segundo bloque del CBU.", numLinea, "Cuenta bancaria");
////            }
//        }

        if (!item.funcionMovimiento.trim().isBlank()) {
            return errores.setErroresArchivo("Función movimiento inválido.", "Se deben informar blancos.", numLinea, "Función movimiento");
        }

        if (!item.codigoMotivoRechazo.trim().isBlank()) {
            return errores.setErroresArchivo("Código motivo de rechazo inválido.", "Se deben informar blancos.", numLinea, "Código motivo de rechazo");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);
        try {
            LocalDate fechaVencimiento = LocalDate.parse(item.fechaVencimiento, formatter);
            if (item.codigoBanco.equals("044")){
                if (fechaVencimiento.isBefore(LocalDate.now())||fechaVencimiento.isEqual(LocalDate.now())){
                    return errores.setErroresArchivo("Fecha vencimiento invalida", "Para cuentas BH debe haber minimo 24hs de vencimiento", numLinea, "Fecha vencimiento");
                }
            }else{
                if (fechaVencimiento.isBefore(LocalDate.now().plusDays(2))){
                    return errores.setErroresArchivo("Fecha vencimiento invalida", "Para cuentas no BH debe haber minimo 48hs de vencimiento", numLinea, "Fecha vencimiento");
                }
            }
        } catch (DateTimeParseException e) {
            return errores.setErroresArchivo("Fecha de vencimiento inválida.", "Se debe ingresar una fecha válida con formato AAAAMMDD", numLinea, "Fecha de vencimiento");
        }

        if (convenio.moneda != Integer.parseInt(item.monedaConvenio)) {
            return errores.setErroresArchivo("Moneda inválida.", "La moneda debe coincidir con la del convenio elegido.", numLinea, "Moneda convenio");
        }
        if (!item.monedaConvenio.equals("080") && !item.monedaConvenio.equals("002")) {
            return errores.setErroresArchivo("Moneda inválida.", "La moneda debe ser pesos o dólares.", numLinea, "Moneda convenio");
        }

//        if (!item.fechaReintentoTopeDevolucion.replaceAll("0", "").trim().isBlank()) {
//            return errores.setErroresArchivo("Fecha de reintento/tope devolución inválida.", "Se deben informar ceros.", numLinea, "Fecha de reintento/tope devolución");
//        }
        try {
            int fecha = Integer.parseInt(item.fechaReintentoTopeDevolucion);
            if (fecha!=0){
                try {
                    LocalDate fechaReintentoTopeDevolucion = LocalDate.parse(item.fechaReintentoTopeDevolucion, formatter);

                } catch (DateTimeParseException e) {
                    return errores.setErroresArchivo("Fecha de reintento/tope devolución inválida.", "Se debe ingresar una fecha válida con formato AAAAMMDD o 0", numLinea, "Fecha de reintento/tope devolución");
                }
            }
        }catch (Exception e){
            return errores.setErroresArchivo("Fecha de reintento/tope devolución inválida.", "Se debe ingresar una fecha válida con formato AAAAMMDD o 0", numLinea, "Fecha de reintento/tope devolución");
        }


        try {
            int importe = Integer.parseInt(item.importeADebitar);
            if (importe<0){
                return errores.setErroresArchivo("Importe a debitar invalido.", "Debe ser mayor a 0", numLinea, "Importe a debitar");
            }
        }catch (Exception e){
            return errores.setErroresArchivo("Importe a debitar invalido.", "Debe ser mayor a 0", numLinea, "Importe a debitar");
        }
        if (!item.importeDebitado.replaceAll("0", "").trim().isBlank()) {
            return errores.setErroresArchivo("Importe debitado inválido.", "Se deben informar ceros.", numLinea, "Importe debitado");
        }

        if (!item.nuevaSucursalBanco.replaceAll("0", "").trim().isBlank()) {
            return errores.setErroresArchivo("Nueva sucursal banco inválida.", "Se deben informar ceros.", numLinea, "Nueva sucursal banco");
        }

        if (!item.nuevoTipoCuenta.replaceAll("0", "").trim().isBlank()) {
            return errores.setErroresArchivo("Nuevo tipo de cuenta inválido.", "Se deben informar ceros.", numLinea, "Nuevo tipo de cuenta");
        }

        if (!item.nuevaCuentaBancaria.replaceAll("0", "").trim().isBlank()) {
            return errores.setErroresArchivo("Nueva cuenta bancaria inválida.", "Se deben informar ceros.", numLinea, "Nueva cuenta bancaria");
        }

        if (!item.nuevoIdCliente.replaceAll("0", "").trim().isBlank()) {
            return errores.setErroresArchivo("Nuevo id cliente inválido.", "Se deben informar ceros.", numLinea, "Nuevo id cliente");
        }

        if (!item.sinUso.trim().isBlank()) {
            return errores.setErroresArchivo("Campo sin uso inválido.", "Se deben informar blancos.", numLinea, "Sin uso");
        }

        if (!item.fillerFinal.equals("0")) {
            return errores.setErroresArchivo("Filler final inválido.", "Se debe informar cero.", numLinea, "Filler final");
        }

        return respuesta("0");

    }

}
