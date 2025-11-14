package ar.com.hipotecario.canal.officebanking;

import ar.com.hipotecario.backend.util.ArchivoParser;
import ar.com.hipotecario.backend.util.DateUtils;
import ar.com.hipotecario.canal.officebanking.enums.EnumMulticausalAcreditacionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.dto.haberes.ArchivoAcreditacionHeader;
import ar.com.hipotecario.canal.officebanking.jpa.dto.haberes.ArchivoAcreditacionItem;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioPagoHaberesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.PagoDeHaberesOB;
import ar.com.hipotecario.canal.officebanking.util.CbuUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class OBValidacionesAcreditaciones extends ModuloOB {

    public static void validarArchivo(ContextoOB contexto, String archivo) throws Exception {
        List<PagoDeHaberesOB> archivos = new ServicioPagoHaberesOB(contexto).buscarArchivo(archivo, contexto.sesion().empresaOB.emp_codigo).get();
        if (archivos != null && !archivos.isEmpty()) {
            throw new Exception("El archivo ya fue cargado.");
        }

        String[] partes = archivo.split("_");
        String resultado = String.join("_", Arrays.copyOf(partes, partes.length - 1));

        List<PagoDeHaberesOB> archivosConvertidos = new ServicioPagoHaberesOB(contexto).buscarArchivoContains("%"+resultado+"%", contexto.sesion().empresaOB.emp_codigo).get();

        if (archivosConvertidos != null && !archivosConvertidos.isEmpty()) {
            throw new Exception("Ya existe un archivo con ese convenio, fecha y hora");
        }
    }
    public static void validarArchivoNomina(ContextoOB contexto, String archivo) throws Exception {
        List<PagoDeHaberesOB> archivos = new ServicioPagoHaberesOB(contexto).buscarArchivo(archivo, contexto.sesion().empresaOB.emp_codigo).get();

        if (archivos != null && !archivos.isEmpty()) {
            throw new Exception("El archivo ya fue cargado.");
        }

    }


    public static void validarNombreArchivo(String nombreArchivo, Integer convenio) throws Exception {
        if (!nombreArchivo.startsWith("ent") && !nombreArchivo.startsWith("enx")) {
            throw new Exception("El nombre del archivo debe iniciar con los caracteres 'ent' o 'enx'");
        }
        String[] nombreArray = null;
        String entConvenio = null;
        String fecha = null;
        String hora = null;
        try{
            nombreArray = nombreArchivo.split("_");
            entConvenio = nombreArray[0];
            fecha = nombreArray[1];
            hora = nombreArray[2];
        }catch (Exception e){

        }
        String _convenio = null;
        try{
             _convenio = entConvenio.substring(3, entConvenio.length());
            if (!(convenio == Integer.parseInt(_convenio))) {
                throw new Exception("El convenio del archivo no coincide con el seleccionado.");
            }

        } catch (Exception e){
            throw new Exception("El convenio del archivo no coincide con el seleccionado.");
        }
        String delimitador = String.valueOf(nombreArchivo.substring(entConvenio.length(),entConvenio.length()+1).charAt(0));
        if (!delimitador.equals("_")) {
            throw new Exception("El nombre del archivo debe tener un delimitador '_' luego del convenio");
        }


        String delimitador1 = String.valueOf(nombreArchivo.charAt(entConvenio.length() + fecha.length() + 1));
        if (!delimitador1.equals("_")) {
            throw new Exception("El nombre del archivo debe tener un delimitador '_' luego de la fecha");
        }
    	

    	try {
    		if (Integer.valueOf(hora.substring(0, 2))<0 || Integer.valueOf(hora.substring(0, 2))>23
        		|| Integer.valueOf(hora.substring(2, 4))<0 || Integer.valueOf(hora.substring(2, 4))>59
        		|| Integer.valueOf(hora.substring(4, 6))<0 || Integer.valueOf(hora.substring(4, 6))>59) {
    			throw new Exception("La hora del archivo debe ser en formato 24hs.");
        }
        }catch(Exception ex){
        	throw new Exception("La hora del archivo debe ser en formato 24hs.");
        }
    	
//    	String delimitador2 = nombreArchivo.substring(entConvenio.length() + fecha.length() + hora.length()+2,entConvenio.length() + fecha.length() + hora.length()+5);
//    	if (!delimitador2.equals("_of")) {
//            throw new Exception("El nombre del archivo debe tener un delimitador '_of' luego de la hora");
//        }
        if (nombreArchivo.contains("(")||nombreArchivo.contains(")")){
            throw new Exception("El nombre del archivo no debe tener parentesis");
        }
    	    	    	
    	//String[] sucursal = nombreArchivo.substring(25, nombreArchivo.length()).split(".txt");
    	/*try {
    		if(sucursal[0].startsWith("000") || (sucursal[0].startsWith("000") && sucursal[0].length() == 5)) {
    			Integer.parseInt(sucursal[0].toString());
    		}else {
    			throw new Exception("El número de sucursal es incorrecto");
    		}
    	}catch(Exception ex){
    		throw new Exception("El número de sucursal es incorrecto");
    	}
    	 */
    }

    public static void validarNombreArchivoNomina(String nombreArchivo) throws Exception {

    	/*String inicio = nombreArchivo.substring(0, 10);
    	if (!inicio.equals("AltaMasiva")) {
            throw new Exception("El nombre del archivo debe iniciar con la palabra 'AltaMasiva'");
        }*/

    	/*String delimitador = nombreArchivo.substring(10, 11);
    	if (!delimitador.equals("_")) {
            throw new Exception("El nombre del archivo debe tener un delimitador '_' luego de la palabra 'AltaMasiva'");
        }*/

        String fecha = nombreArchivo.substring(11, 17);
        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyMMdd");
        LocalDate fechaIngresada = LocalDate.parse(fecha, formato);

        if (fechaIngresada.isBefore(fechaActual) || !fechaIngresada.isEqual(fechaActual)) {
            throw new Exception("El archivo no tiene la fecha de hoy");
        }
    }


    public static void validarHeader(ArchivoAcreditacionHeader headerFileData, Integer convenio) throws Exception {
        // validacion convenio
        if (!(convenio == Integer.parseInt(headerFileData.getConvenio()))) {
            throw new Exception("El convenio ingresado: " + convenio + " no coincide con el del archivo : " + headerFileData.getConvenio());
        }

        // si la fecha de generacion es menor a la fecha actual
//        if (DateUtils.esFechaMenorAlDia(DateUtils.getDateWithFormat("yyyyMMdd", headerFileData.getFechaGeneracionArchivo(), Locale.ENGLISH))) {
//            throw new Exception("La fecha del archivo no puede ser menor a la fecha actual.");
//        }

        // formato 24 hs campo hora
        Integer hora = Integer.valueOf(headerFileData.getHoraGeneracionArchivo().substring(0, 2));
        Integer minutos = Integer.valueOf(headerFileData.getHoraGeneracionArchivo().substring(2, 4));

        if (hora < 0 || hora > 23 || minutos < 0 || minutos > 59) {
            throw new Exception("La hora del archivo debe ser en formato 24hs.");
        }
    }

    public static ArchivoAcreditacionItem validarDetail(ArchivoAcreditacionItem detail, String fechaGeneracionArchivo, String tipoArchivo) {
        List<OBErrorMessage> errors = new ArrayList<>();
        ArchivoParser parser = new ArchivoParser();
        String posibles = "";

        // validacion fecha mayor a hoy
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate fechaAcreditacion = LocalDate.parse(detail.getFechaAcreditacion(), formatter);
        if (fechaAcreditacion.isBefore(LocalDate.now())) {
            detail.setCorrupted(true);
            errors.add(parser.setError("La fecha de acreditación no puede ser menor a la fecha actual.", new Object[]{posibles}));
            errors.add(parser.setError("Fecha", new Object[]{posibles}));

            detail.setErrores(errors);
            return detail;
        }

        LocalDate fechaGeneracion = LocalDate.parse(fechaGeneracionArchivo, formatter);
        if (fechaAcreditacion.isBefore(fechaGeneracion)) {
            detail.setCorrupted(true);
            errors.add(parser.setError("La fecha de acreditación no puede ser menor a la fecha del archivo.", new Object[]{posibles}));
            errors.add(parser.setError("Fecha", new Object[]{posibles}));

            detail.setErrores(errors);
            return detail;
        }

        String importeEntero = detail.getSueldo().toString();
        String entero = importeEntero.length()>2?importeEntero.substring(0, importeEntero.length() - 2):"0";
        //validacion importe
        if (tipoArchivo.equals("ent")) {

            if (detail.getSueldo().compareTo(BigDecimal.ZERO) < 0 || Integer.valueOf(entero).compareTo(9999999) > 0) {
                detail.setCorrupted(true);
                errors.add(parser.setError("El monto del importe debe ser mayor a 0 y menor a 9999999,99.", new Object[]{posibles}));
                errors.add(parser.setError("Sueldo", new Object[]{posibles}));

                detail.setErrores(errors);
                return detail;
            }
        } else if (tipoArchivo.equals("enx")) {
            BigDecimal maximo = new BigDecimal("99999999999");
            if (detail.getSueldo().compareTo(BigDecimal.ZERO) < 0 || new BigDecimal(entero).compareTo(maximo) > 0) {
                detail.setCorrupted(true);
                errors.add(parser.setError("El monto del importe debe ser mayor a 0 y menor a 99999999999,99.", new Object[]{posibles}));
                errors.add(parser.setError("Sueldo", new Object[]{posibles}));

                detail.setErrores(errors);
                return detail;
            }

        }

        // validacion nombre titular
        if (detail.getNombreCuenta() != null && detail.getCuil() == null) {
            detail.setCorrupted(true);
            errors.add(parser.setError("El nombre del titular es requerido cuando se provee un numero de CUIL/CUIT.", new Object[]{posibles}));
            errors.add(parser.setError("Cuenta", new Object[]{posibles}));

            detail.setErrores(errors);
            return detail;
        }

        // validar cbu-cuenta
        if (armarCBU(detail.getNumeroCuenta()) == null) {
            detail.setCorrupted(true);
            errors.add(parser.setError("La cuenta ingresada es inválida.", new Object[]{posibles}));
            errors.add(parser.setError("CBU", new Object[]{posibles}));

            detail.setErrores(errors);
            return detail;
        }

        // validar cuil-cuit
        if (detail.getNumeroCuenta().length() == 22 && detail.getCuil() == null) {
            detail.setCorrupted(true);

            errors.add(parser.setError("El cuil/cuit ingresado es inválido.", new Object[]{posibles}));
            errors.add(parser.setError("CUIT", new Object[]{posibles}));

            detail.setErrores(errors);
            return detail;
        }

        //validar multicausal
        if (detail.getMultiCausal().isBlank()) {
            if (detail.getNumeroCuenta().length() != 22) {
                detail.setCorrupted(true);
                errors.add(parser.setError("El multicausal debe venir en blanco cuando se ingresa un cbu.", new Object[]{posibles}));
                errors.add(parser.setError("Multicausal", new Object[]{posibles}));

                detail.setErrores(errors);
                return detail;
            }
        } else {
            boolean existe = false;
            for (EnumMulticausalAcreditacionesOB e : EnumMulticausalAcreditacionesOB.values()) {
                if (e.getCodigo().equals(detail.getMultiCausal())) {
                    existe = true;
                    break;
                }
            }
            if (!existe) {
                detail.setCorrupted(true);
                errors.add(parser.setError("El multicausal debe ser válido.", new Object[]{posibles}));
                errors.add(parser.setError("Multicausal", new Object[]{posibles}));

                detail.setErrores(errors);
                return detail;
            }
        }
        return detail;
    }

    protected static String armarCBU(String numeroCuenta) {

        if (numeroCuenta.length() == 15) {
            return numeroCuenta;
        } else if (numeroCuenta.length() == 22) {
            if (numeroCuenta.startsWith("044")) {
                if (!CbuUtil.isValidCBU(numeroCuenta)) {
                    return null;
                }
                return numeroCuenta.substring(7, 22);
            }
            return numeroCuenta;
        } else
            return null;
    }

    /*
        // validacion de causales
        if (detail.getNumeroCuenta() != null && detail.getMultiCausal()==null){
            List<ErrorMessage> errors = new ArrayList<ErrorMessage>();
            errors.add(parser.setError("error.dynamic.field.null", new Object[] { detail.getNumLinea(), "MultiCausal"} ));
            detail.setCorrupted(true);
            detail.setErrores(errors);
            return;
        }

        //Valida Cuenta interna con causales
        if(detail.getNumeroCuenta() != null && detail.getMultiCausal()!=null){
            if(errorCausales(detail, parser)){
                return;
            }
        }else{  //Valida Causales de los CBU Banco BH
            if(detail.getNumeroCuenta() == null && detail.getMultiCausal() != null){
                CBU cbu = GenericUtils.splitCBU(convertirBigDecimalToStringPadding(detail.getCbu(),22,"0"));
                if(cbu.getNumeroEntidad().equals("044")){//El CBU para el banco BH tiene que tener los multicausales correctos
                    if(errorCausales(detail, parser))
                        return;
                }
            }
        }


    }
     */


}

