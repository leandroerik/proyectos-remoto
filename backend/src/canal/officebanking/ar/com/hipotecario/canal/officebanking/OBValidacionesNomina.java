package ar.com.hipotecario.canal.officebanking;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.servicio.api.catalogo.ApiCatalogo;
import ar.com.hipotecario.backend.servicio.api.catalogo.CodigosPostales;
import ar.com.hipotecario.backend.servicio.api.catalogo.Paises;
import ar.com.hipotecario.backend.servicio.api.catalogo.Paises.Pais;
import ar.com.hipotecario.backend.servicio.api.catalogo.Sucursales;
import ar.com.hipotecario.backend.util.ArchivoParser;
import ar.com.hipotecario.backend.util.DateUtils;
import ar.com.hipotecario.canal.officebanking.enums.EnumPagoHaberesNominaOB;
import ar.com.hipotecario.canal.officebanking.jpa.dto.nomina.ArchivoNominaItem;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OBValidacionesNomina extends ModuloOB {
    private static final Logger log = LoggerFactory.getLogger(DateUtils.class);

    public static void validarNombreArchivo(String nombreArchivo, Integer convenio) throws Exception {

        String inicio = nombreArchivo.substring(0, 10);
        if (!inicio.equals("AltaMasiva")) {
            throw new Exception("El nombre del archivo debe iniciar con los caracteres 'AltaMasiva_'");
        }

        String delimitador1 = nombreArchivo.substring(11, 12);
        if (!delimitador1.equals("_")) {
            throw new Exception("El nombre del archivo debe tener un delimitador '_' antes de la fecha");
        }

        String fecha = nombreArchivo.substring(13, 19);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        try {
            LocalDate.parse(fecha, formatter);
        } catch (Exception e) {
            throw new Exception("La fecha del archivo no puede ser menor a la fecha actual.");
        }
    }


    public static ArchivoNominaItem validarDetail(ContextoOB contexto, ArchivoNominaItem detail) throws Exception {
        if (detail.isCorrupted()) return detail;

        //validacion fecha mayor a hoy
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
//        LocalDate fechaAcreditacion = LocalDate.parse(detail.getFechaIngreso(), formatter);
//        if (fechaAcreditacion.isBefore(LocalDate.now())) {
//            throw new Exception("La fecha de acreditación no puede ser menor a la fecha actual.");
//        }

        if (detail.getSecuencial() == null) {
            throw new Exception("El secuencial no puede ser null.");
        }
        detail.setNumLinea(detail.getSecuencial());

        String posibles = "";
        List<OBErrorMessage> errors = new ArrayList<>();
        ArchivoParser parser = new ArchivoParser();

        // valido sexo
        if (EnumPagoHaberesNominaOB.Sexo.get(convertirStringToCharacter(detail.getSexo())) == null) {
            for (EnumPagoHaberesNominaOB.Sexo s : EnumPagoHaberesNominaOB.Sexo.values()) {
                if (!posibles.equals("")) posibles = posibles + ", ";
                posibles = posibles + s.getCodigo() + "-" + s.getDescripcion();
            }
            detail.setCorrupted(true);
            errors.add(parser.setError("Sexo", new Object[]{posibles}));
            detail.setErrores(errors);
            return detail;
        }

        // Tipo CUIT /CUIL
        String tipoCuit = detail.getTipoCuil() != null ? StringUtils.leftPad(detail.getTipoCuil(), 2, "0") : null;
        if (EnumPagoHaberesNominaOB.TipoCuitCuil.get(tipoCuit) == null) {
            for (EnumPagoHaberesNominaOB.TipoCuitCuil s : EnumPagoHaberesNominaOB.TipoCuitCuil.values()) {
                if (!posibles.equals("")) posibles = posibles + ", ";
                posibles = posibles + s.getCodigo() + "-" + s.getDescripcion();
            }
            detail.setCorrupted(true);
            errors.add(parser.setError("Tipo CUIT", new Object[]{posibles}));
            detail.setErrores(errors);
            return detail;
        }

        // Estado civil
        if (detail.getEstadoCivil() != null && !detail.getEstadoCivil().trim().equals("")) {
            if (EnumPagoHaberesNominaOB.EstadoCivil.get(detail.getEstadoCivil()) == null) {
                for (EnumPagoHaberesNominaOB.EstadoCivil s : EnumPagoHaberesNominaOB.EstadoCivil.values()) {
                    if (!posibles.equals("")) posibles = posibles + ", ";
                    posibles = posibles + s.getCodigo() + "-" + s.getDescripcion();
                }
                detail.setCorrupted(true);
                errors.add(parser.setError("Estado Civil", new Object[]{posibles}));
                detail.setErrores(errors);
                return detail;

            }
        }

        //Persona Expuesta Politicamente
        if (EnumPagoHaberesNominaOB.esPep.get(detail.getEsPEP()) == null) {
            for (EnumPagoHaberesNominaOB.esPep s : EnumPagoHaberesNominaOB.esPep.values()) {
                if (!posibles.equals("")) posibles = posibles + ", ";
                posibles = posibles + s.getCodigo() + "-" + s.getDescripcion();
            }
            detail.setCorrupted(true);
            errors.add(parser.setError("esPEP", new Object[]{posibles}));
            detail.setErrores(errors);
            return detail;

        }

        //Situacion Laboral
        if (detail.getSituacionLaboral() != null) {

            if (EnumPagoHaberesNominaOB.SituacionLaboral.get(String.valueOf(detail.getSituacionLaboral())) == null) {
                for (EnumPagoHaberesNominaOB.SituacionLaboral s : EnumPagoHaberesNominaOB.SituacionLaboral.values()) {
                    if (!posibles.equals("")) posibles = posibles + ", ";
                    posibles = posibles + s.getCodigo() + "-" + s.getDescripcion();
                }
                detail.setCorrupted(true);
                errors.add(parser.setError("Situación Laboral", new Object[]{posibles}));
                detail.setErrores(errors);
                return detail;
            }
        }
        // Tipo Telefono
        if (EnumPagoHaberesNominaOB.TipoTel.get(detail.getTipoTel()) == null) {
            for (EnumPagoHaberesNominaOB.TipoTel s : EnumPagoHaberesNominaOB.TipoTel.values()) {
                if (!posibles.equals("")) posibles = posibles + ", ";
                posibles = posibles + s.getCodigo() + "-" + s.getDescripcion();
            }
            detail.setCorrupted(true);
            errors.add(parser.setError("Tipo Tel", new Object[]{posibles}));
            detail.setErrores(errors);
            return detail;

        }

        // Solo informa prefijo celular si informa celular
        if (detail.getPreFijoCel() != null) {
            // tiene que ser un celular
            if (
                    EnumPagoHaberesNominaOB.TipoTel.LABORAL_CELULAR.equals(detail.getTipoTel()) ||
                            EnumPagoHaberesNominaOB.TipoTel.PARTICULAR_CELULAR.equals(detail.getTipoTel()) ||
                            EnumPagoHaberesNominaOB.TipoTel.REFERENCIA_CELULAR.equals(detail.getTipoTel())

            ) {
                // Si no es un celular
                if (!EnumPagoHaberesNominaOB.TipoTel.LABORAL_CELULAR.equals(detail.getTipoTel()) && !EnumPagoHaberesNominaOB.TipoTel.PARTICULAR_CELULAR.equals(detail.getTipoTel()) && !EnumPagoHaberesNominaOB.TipoTel.REFERENCIA_CELULAR.equals(detail.getTipoTel())) {
                    errors.add(parser.setError("Prefijo Celular", new Object[]{"vacio para el tipo de Telefono " + EnumPagoHaberesNominaOB.TipoTel.get(detail.getTipoTel()).getDescripcion()}));
//					detail.setCorrupted(true);
//					detail.setErrores(errors);
//					return detail;
                }
                detail.setCorrupted(true);
                errors.add(parser.setError("Tipo Telefono", new Object[]{"El tipo Telefono debe ser un celular"}));
                detail.setErrores(errors);

                return detail;
//				return "El tipo Telefono debe ser un celular";
            }

        }

        if (EnumPagoHaberesNominaOB.TipoMail.get(detail.getTipoCorreoElectronico()) == null) {
            for (EnumPagoHaberesNominaOB.TipoMail s : EnumPagoHaberesNominaOB.TipoMail.values()) {
                if (!posibles.equals("")) posibles = posibles + ", ";
                posibles = posibles + s.getCodigo() + "-" + s.getDescripcion();
            }
            detail.setCorrupted(true);
            errors.add(parser.setError("Tipo Mail", new Object[]{posibles}));
            detail.setErrores(errors);
            return detail;
        }

        // TipoDocPS
        String tipoDocPs = detail.getTipoDocumento() != null ? StringUtils.leftPad(detail.getTipoDocumento(), 2, "0") : null;
        if (EnumPagoHaberesNominaOB.TipoDoc.get(tipoDocPs) == null) {
            for (EnumPagoHaberesNominaOB.TipoDoc s : EnumPagoHaberesNominaOB.TipoDoc.values()) {
                if (!posibles.equals("")) posibles = posibles + ", ";
                posibles = posibles + s.getCodigo() + "-" + s.getDescripcion();
            }
            detail.setCorrupted(true);
            errors.add(parser.setError("Tipo Doc", new Object[]{posibles}));
            detail.setErrores(errors);
            return detail;
        }

        // sucursal //se deja una validación leve por no tener el dato de la prov en el archivo
        try {
            Sucursales sucursal;
            sucursal = ApiCatalogo.sucursales(contexto, "", detail.getSucursal(), "1").tryGet();
            if (sucursal == null) {
                detail.setCorrupted(true);
                errors.add(parser.setError("Numero Sucursal", new Object[]{"No puede ser null "}));
                detail.setErrores(errors);
                return detail;
            }
        } catch (Exception e) {
            throw new Exception("Numero Sucursal - Exception");
        }

        // pais nacimiento
        if (detail.getPaisNacimiento() == null) {
            detail.setCorrupted(true);
            errors.add(parser.setError("Pais Nacimiento", new Object[]{"Pais de nacimiento no informado"}));
            detail.setErrores(errors);
            return detail;
        }

        // valido pais
        try {
            Paises paises = ApiCatalogo.paises(contexto).tryGet();
            Pais pais = paises.buscarPaisById(String.valueOf(detail.getPaisNacimiento()));

            if (pais == null) {
                detail.setCorrupted(true);
                errors.add(parser.setError("Codigo Pais", new Object[]{"No puede ser null"}));
                detail.setErrores(errors);
                return detail;
            }
            //valido nacionalidad
            if (!pais.id.equals(String.valueOf(detail.getNacionalidad()))) {
                detail.setCorrupted(true);
                errors.add(parser.setError("Codigo Nacionalidad", new Object[]{"Dato inválida"}));
                detail.setErrores(errors);
                return detail;
            }
        } catch (Exception e) {
            throw new Exception("Codigo Pais/Nacionalidad Exception");
        }

        // codigo postal
        if (detail.getCp() == null) {
            detail.setCorrupted(true);
            errors.add(parser.setError("Codigo Postal", new Object[]{"No puede ser null"}));
            detail.setErrores(errors);
            return detail;
        }
        try {
            CodigosPostales codigoPostales;
            codigoPostales = ApiCatalogo.codigosPostales(contexto, detail.getCp(), "", "").tryGet();
            if (codigoPostales == null) {
                detail.setCorrupted(true);
                errors.add(parser.setError("Codigo Postal", new Object[]{"No existe, valide nuevamnte"}));
                detail.setErrores(errors);
                return detail;
            }
        } catch (Exception e) {
            throw new Exception("Codigo Pais/Nacionalidad Exception");
        }

        // Localidad
        if (detail.getLocalidad() == null) {
            detail.setCorrupted(true);
            errors.add(parser.setError("Localidad", new Object[]{"El campo Localidad es obligatorio"}));
            detail.setErrores(errors);
            return detail;
        }
        try {
            Futuro<CodigosPostales> codigosPostales = ApiCatalogo.codigosPostales(contexto, detail.getCp(), detail.getLocalidad(), detail.getProvincia().toString());
            if (codigosPostales == null) {
                detail.setCorrupted(true);
                errors.add(parser.setError("Localidad", new Object[]{"El campo Localidad es obligatorio"}));
                detail.setErrores(errors);
                return detail;
            }
        } catch (Exception e) {
            throw new Exception("Codigo Postal / Localidad Exception");

        }
        try {
            // Provincia
            if (detail.getProvincia() == null) {
                errors.add(parser.setError("Provincia", new Object[]{"El campo Localidad es obligatorio"}));
                detail.setCorrupted(true);
                detail.setErrores(errors);
                return detail;
            }

            CodigosPostales codigoPostal = ApiCatalogo.codigosPostales(contexto, detail.getCp(), "", "").tryGet();
            if (codigoPostal == null) {
                detail.setCorrupted(true);
                errors.add(parser.setError("Codigo Postal", new Object[]{"No existe, valide nuevamnte"}));
                detail.setErrores(errors);
                return detail;
            }
        } catch (Exception e) {
            throw new Exception("Codigo Pais/Nacionalidad Exception");
        }

        // Provincia
        if (detail.getProvincia() == null) {
            detail.setCorrupted(true);
            errors.add(parser.setError("Provincia", new Object[]{"El campo Provincia es obligatorio"}));
            detail.setErrores(errors);
            return detail;
        }
        return detail;

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

    /*

        public Boolean validoTamanoLinea(DefinitionItem item, String linea,
                                         int numLinea, RegistroImportado reg) {
            Integer totalTamLinea = item.getLenghtLine();
            Integer minTamLinea = item.getMinLenghtLine();

            if (linea.length() != totalTamLinea) {
                List<ErrorMessage> errores = new ArrayList<ErrorMessage>();
                if (minTamLinea==null || minTamLinea==totalTamLinea){
                    errores.add(setError("error.maxlenght", new Object[] {
                            numLinea, totalTamLinea }));
                }else{
                    if (linea.length()<minTamLinea){
                        errores.add(setError("error.minlenght", new Object[] {
                                numLinea,minTamLinea, totalTamLinea }));
                    }
                }
                if (!errores.isEmpty()){
                    reg.setCorrupted(true);
                    reg.setErrores(errores);
                    reg.setNumLinea(numLinea);
                    return false;
                }
            }
            return true;
        }




    
     */

    private static Character convertirStringToCharacter(String value) {
        try {
            if (value != null) {
                value = value.trim();
                return value.charAt(0);
            }
        } catch (Exception e) {
            log.error("No se pudo tranformar el dato [" + value + "] error=" + e.getMessage());
        }
        return null;
    }

}
