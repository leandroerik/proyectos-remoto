package ar.com.hipotecario.canal.officebanking;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.empresas.SubConveniosOB;
import ar.com.hipotecario.backend.util.DateUtils;
import ar.com.hipotecario.canal.officebanking.dto.ErrorArchivoOB;
import ar.com.hipotecario.canal.officebanking.dto.pagosMasivos.ArchivoBeneficiarioDTO;
import ar.com.hipotecario.canal.officebanking.dto.pagosMasivos.ArchivoPapDTO;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioBeneficiarioOB;
import ar.com.hipotecario.canal.officebanking.util.CbuUtil;
import ar.com.hipotecario.canal.officebanking.util.StringUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static ar.com.hipotecario.canal.officebanking.util.StringUtil.eliminarCerosIzquierda;

public class OBValidacionesPB extends ModuloOB {
    public static Objeto validarNombreArchivo(String nombreArchivo) {
        ErrorArchivoOB errores = new ErrorArchivoOB();
        if (nombreArchivo.length() > 150) {
            return errores.setErroresArchivo("Nombre archivo invalido invalido", "El nombre del archivo no puede exceder los 150 caracteres", null, null);
        }
        return respuesta("0");
    }

    public static Objeto validarHeader(ArchivoBeneficiarioDTO.Header header, SubConveniosOB.SubConvenio subConvenio) {
        ErrorArchivoOB errores = new ErrorArchivoOB();

        if (!header.registroId.equals("FH")) {
            return errores.setErroresArchivo("Registro ID invalido", "Registro ID debe ser 'FH'", 0, null);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate fechaCreacion = LocalDate.parse(header.idArchivo.substring(9, 17), formatter);
        String adherente = header.idArchivo.substring(2,9);

        if (!header.idArchivo.startsWith("PB")) {
            return errores.setErroresArchivo("ID archivo invalido", "ID de archivo debe ser'PB'", 0, null);
        }

        if (header.secuencial.isBlank()) {
            return errores.setErroresArchivo("Secuencial faltante", "Debe incluir un numero de secuencial", 0, null);
        }

        String nroSubConv = agregarCerosIzquierda(subConvenio.nroSubConv);
        String nroConv = agregarCerosIzquierda(subConvenio.nroConv);

        if (!header.convenio.equals(nroConv)) {
            return errores.setErroresArchivo("Numero de convenio invalido", "El numero de convenio en el archivo no coincide con el seleccionado", 0, null);
        }

        if (!header.subconvenio.equals(nroSubConv)) {
            return errores.setErroresArchivo("Numero de subconvenio invalido", "El numero de subconvenio en el archivo no coincide con el seleccionado", 0, null);
        }

        if (!header.idContenido.equals("PADRONBENEF")) {
            return errores.setErroresArchivo("ID contenido invalido", "ID contenido debe ser padrón de beneficiarios", 0, null);
        }

        if (!adherente.equals(StringUtil.padLeftWithZeros(String.valueOf(subConvenio.nroAdh),7))){
            return errores.setErroresArchivo("Numero de adherente invalido", "Numero de adherente invalido", 0, null);
        }

        return respuesta("0", "fecha", fechaCreacion);
    }

    public static Objeto validarDetalle(ArchivoPapDTO.Body item, ContextoOB contexto, SubConveniosOB.SubConvenio subConvenio, int numLinea, LocalDate fechaCreacion) {
        ErrorArchivoOB errores = new ErrorArchivoOB();
        List<String> sucursalEnviar = new ArrayList<>(List.of(
                "001", "003", "004", "006", "007", "008", "011", "012",
                "013", "014", "015", "016", "017", "018", "021", "022",
                "023", "024", "025", "026", "027", "028", "029", "030",
                "031", "032", "033", "034", "035", "036", "037", "038",
                "040", "043", "044", "045", "046", "047", "049", "051",
                "052", "053", "054", "055", "056", "057", "058", "059",
                "060", "061", "062", "063", "064", "065", "069", "070",
                "071", "073", "075", "076", "077", "081", "082", "083",
                "099"
        ));
        ServicioBeneficiarioOB servicioBeneficiario = new ServicioBeneficiarioOB(contexto);
        ArrayList<String> mediosDeEjecucion = new ArrayList<>(Arrays.asList("002", "003", "009", "012", "013"));

        if (!item.registroId.equals("PO")) {
            return errores.setErroresArchivo("Dato invalido", "El campo registro ID debe ser 'PO'", numLinea, null);

        }
        if (item.referencia.isBlank()) {
            return errores.setErroresArchivo("Dato invalido", "Referencia incorrecta", numLinea, null);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        LocalDate fechaEjec = LocalDate.parse(item.fechaEjecucion, formatter);
        Date dateFechaEjec = Date.from(fechaEjec.atStartOfDay(ZoneId.systemDefault()).toInstant());

        if (DateUtils.esFechaMenorAlDia(dateFechaEjec)) {
            return errores.setErroresArchivo("Fecha de ejecución de la orden.", "Debe ser mayor o igual al día de hoy.", numLinea, null);
        }

        if (!mediosDeEjecucion.contains(item.medioPago)) {
            return errores.setErroresArchivo("Medio de ejecución", "\"002\",\"003\",\"009\",\"012\",\"013\"", numLinea, null);
        }

        if (item.importe.isBlank() || Long.parseLong(item.importe) <= 0) {
            return errores.setErroresArchivo("Importe", "El campo Importe a pagar es obligatorio", numLinea, null);
        }

        if ((!item.moneda.equals("ARS") && !item.moneda.equals("USD")) || item.moneda.isBlank()) {
            return errores.setErroresArchivo("Moneda", "El campo Moneda de Pago no debe estar vacio.", numLinea, null);
        }

        if (!item.comprobante.equals("ICA") && !item.comprobante.equals("NEC")) {
            return errores.setErroresArchivo("Acompaña comprobante adjunto", "'ICA' o 'NEC'", numLinea, null);
        }

        if (!item.CBUDebito.isBlank() && !CbuUtil.isValidCBU(item.CBUDebito)) {
            return errores.setErroresArchivo("CBU Adherente", "El campo CBU de la cuenta del adherente debe estar vacio", numLinea, null);
        }

        if (!item.cbuCredito.isBlank() && !CbuUtil.isValidCBU(item.cbuCredito)) {
            return errores.setErroresArchivo("CBU cuenta destino", "El campo CBU de la cuenta destino debe ser n\\u00FAmerico y de 22 caracteres.", numLinea, null);
        }

        if (!item.medioPago.equals("009")) {
            if (ingresoTodosLosCamposTransferencia(item) || ingresoAlgunCampoTransferencia(item)) {
                return errores.setErroresArchivo("Campos transferencia", "El medio de ejecución es cheque, por favor asegurese que estén vacios los campos Tipo Cta., Moneda Cta., CBU Cta., Nombre Cta.", numLinea, null);
            }
        } else {
            if (ingresoAlgunCampoTransferencia(item)) {
                return errores.setErroresArchivo("Campos transferencia", "Usted ingresó algún campo de Transferencia, por favor asegurese que estén completos los campos Tipo Cta., Moneda Cta., CBU Cta., Nombre Cta.", numLinea, null);
            }
            if (ingresoTodosLosCamposTransferencia(item)) {
                if (!item.tipoCuentaCredito.equals("CA") && !item.tipoCuentaCredito.equals("CC")) {
                    return errores.setErroresArchivo("Tipo de cuenta a acreditar", "'CA' o 'CC'", numLinea, null);
                }

                if (!item.monedaCuentaCredito.equals("ARS") && !item.monedaCuentaCredito.equals("USD")) {
                    return errores.setErroresArchivo("Moneda de la cuenta a acreditar", "'ARS' o 'USD'", numLinea, null);
                }
            }
        }
        if (item.tipoOperatoria.isBlank()
                || (!item.tipoOperatoria.equals("P") &&
                !item.tipoOperatoria.equals("G") &&
                !item.tipoOperatoria.equals("T") &&
                !item.tipoOperatoria.equals("J") &&
                !item.tipoOperatoria.equals("O") &&
                !item.tipoOperatoria.equals("S"))) {
            return errores.setErroresArchivo("Tipo de operatoria invalido", "El tipo de operatoria debe ser 'P','G','T','J','O' o 'S'", numLinea, null);
        }

        if (!item.medioPago.equals("009")) {
            if (!item.sucursalEnviar.isBlank() && item.canalEntrega.isBlank()) {
                return errores.setErroresArchivo("Canal de entrega", "El medio de ejecuci\\u00F3n es cheque por lo tanto el campo Canal de Entrega es obligatorio.", numLinea, null);
            }
            if (item.sucursalEnviar.isBlank() && !item.canalEntrega.isBlank()) {
                return errores.setErroresArchivo("Sucursal envio", "El medio de ejecución es cheque por lo tanto el campo Sucursal es obligatorio.", numLinea, null);
            }

            if (!item.medioPago.equals("012") && !mediosDeEjecucion.equals("013")) {
                if (!sucursalEnviar.contains(item.sucursalEnviar)) {
                    return errores.setErroresArchivo("Sucursal envio", "Sucursal invalida", numLinea, null);
                }
            }
        } else {
            if (!item.canalEntrega.isBlank()) {
                return errores.setErroresArchivo("Canal entrega invalido", "El medio de ejecución es transferencia por lo tanto el campo Canal de Entrega debe ser vacio.", numLinea, null);
            }
            if (!item.sucursalEnviar.isBlank()) {
                return errores.setErroresArchivo("Sucursal envio invalido", "El medio de ejecución es transferencia por lo tanto el campo Sucursal debe ser vacio.", numLinea, null);
            }
        }

        if (!item.incluirFirma.isBlank() && (!item.incluirFirma.equals("S") && !item.incluirFirma.equals("N"))) {
            return errores.setErroresArchivo("Incluir firma en la impresión de cheques", "vacio, 'S' o 'N'", numLinea, null);
        }
        if (item.medioPago.equals("012")||item.medioPago.equals("013")){
            if ((!item.cruzarCheque.equals("S") && !item.cruzarCheque.equals("N"))) {
                return errores.setErroresArchivo("Cruzar cheque", "'S' o 'N'", numLinea, null);
            }if ( (!item.clausula.equals("S") && !item.clausula.equals("N"))) {
                return errores.setErroresArchivo("Cláusula NO A LA ORDEN", "'S' o 'N'", numLinea, null);
            }

        }else{
            if (!item.cruzarCheque.isBlank()) {
                return errores.setErroresArchivo("Cruzar cheque", "Vacio", numLinea, null);
            }if (!item.clausula.isBlank()) {
                return errores.setErroresArchivo("Cláusula NO A LA ORDEN", "Vacio", numLinea, null);
            }
        }



        LocalDate fechaVencimient = null;
        if ((item.medioPago.equals("003") || item.medioPago.equals("013")) && item.fechaVencimiento.isBlank()) {
            return errores.setErroresArchivo("Vencimiento cheque", "El campo Fecha de vencimiento del cheque es obligatorio.", numLinea, null);
        } else {
            if (!item.fechaVencimiento.isBlank()) {
                try {
                    fechaVencimient = LocalDate.parse(item.fechaVencimiento, formatter);
                } catch (Exception e) {
                    return errores.setErroresArchivo("Vencimiento cheque", "El formato de fecha es incorrecto", numLinea, null);
                }
            }
        }

        if (!item.nombreApellidoAutorizado.isBlank() && item.dniAutorizado.isBlank()) {
            return errores.setErroresArchivo("Tipo y nro dni 1er autorizado", "El campo Tipo y nro. documento 1er autorizado a retirar pagos es obligatorio.", numLinea, null);
        }
        if (item.nombreApellidoAutorizado.isBlank() && !item.dniAutorizado.isBlank()) {
            return errores.setErroresArchivo("Nombre y apellido 1er autorizado", "El campo Nombre y apellido 1er autorizado a retirar pagos es obligatorio.", numLinea, null);
        }
        if (!item.nombreApellidoAutorizado2.isBlank() && item.dniAutorizado2.isBlank()) {
            return errores.setErroresArchivo("Tipo y nro dni 2do autorizado", "El campo Tipo y nro. documento 2do autorizado a retirar pagos es obligatorio.", numLinea, null);
        }
        if (item.nombreApellidoAutorizado2.isBlank() && !item.dniAutorizado2.isBlank()) {
            return errores.setErroresArchivo("Nombre y apellido 2do autorizado", "El campo Nombre y apellido 2do autorizado a retirar pagos es obligatorio.", numLinea, null);
        }
        if (!item.nombreApellidoAutorizado3.isBlank() && item.dniAutorizado3.isBlank()) {
            return errores.setErroresArchivo("Tipo y nro dni 3er autorizado", "El campo Tipo y nro. documento 3er autorizado a retirar pagos es obligatorio.", numLinea, null);
        }
        if (item.nombreApellidoAutorizado3.isBlank() && !item.dniAutorizado3.isBlank()) {
            return errores.setErroresArchivo("Nombre y apellido 3er autorizado", "El campo Nombre y apellido 3er autorizado a retirar pagos es obligatorio.", numLinea, null);
        }

        if (!item.requerirReciboOficial.isBlank() && (!item.requerirReciboOficial.equals("S") && !item.requerirReciboOficial.equals("N"))) {
            return errores.setErroresArchivo("Recibo oficial del Beneficiario", "vacio, 'S' o 'N'", numLinea, null);
        }

        SubConveniosOB.Cheque chequeFisico = null;
        SubConveniosOB.Cheque eCheq = null;
        if (subConvenio.cheques != null) {
            for (SubConveniosOB.Cheque cheq : subConvenio.cheques) {
                if (cheq.catMedioEjec == 2) {
                    chequeFisico = cheq;
                } else {
                    eCheq = cheq;
                }
            }
        }

        if (!item.medioPago.equals("009") && !item.medioPago.isBlank()) {
            if ((item.medioPago.equals("002") || item.medioPago.equals("003")) && chequeFisico != null) {

                if (chequeFisico.reqCheqCruzado.equalsIgnoreCase("S") && item.cruzarCheque.equalsIgnoreCase("N")) {
                    return errores.setErroresArchivo("Cruzar cheque", "Todos los cheques del subconvenio deben ser cruzados.", numLinea, null);
                }
                if (chequeFisico.reqCheqNoALaOrden.equalsIgnoreCase("S") && item.clausula.equalsIgnoreCase("N")) {
                    return errores.setErroresArchivo("Clausula NO a la orden", "Todos los cheques del subconvenio deben contener cláusula de no a la orden.", numLinea, null);
                }
                if (fechaCreacion != null && chequeFisico.tolCheqFechaPrevia != 0) {
                    Date fechaCrea = Date.from(fechaCreacion.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    Date fechaToleranciaPrevia = DateUtils.restarDias(fechaCrea, chequeFisico.tolCheqFechaPrevia);
                    if (DateUtils.esFecha1MayorAFecha2(fechaToleranciaPrevia, fechaCrea)) {
                        return errores.setErroresArchivo("Tolerancia fecha previa", "La fecha de ejecución debe ser igual o mayor a la fecha del día.", numLinea, null);
                    }
                }

                if (item.medioPago.equals("003") && item.fechaEjecucion != null) {
                    Date fechaVenc = Date.from(fechaVencimient.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    Date fechaPlazoMaxDiferimiento = DateUtils.sumarDias(dateFechaEjec, chequeFisico.plzMaxDifCPD);
                    if (item.fechaVencimiento != null) {

                        if (DateUtils.esFecha1IgualAFecha2(dateFechaEjec, fechaVenc) || DateUtils.esFecha1MayorAFecha2(dateFechaEjec, fechaVenc)) {
                            return errores.setErroresArchivo("Error fecha diferido - ejecución.", "Error en plazo de cheque con pago diferido.", numLinea, null);
                        }
                    }
                    if (item.fechaVencimiento == null || (item.fechaVencimiento != null && DateUtils.esFecha1MayorAFecha2(fechaVenc, fechaPlazoMaxDiferimiento))) {
                        return errores.setErroresArchivo("Error fecha diferido", "Error en plazo de cheque con pago diferido.", numLinea, null);
                    }

                }

            }
            if ((item.medioPago.equals("012") || item.medioPago.equals("013")) && eCheq != null) {

                if (eCheq.reqCheqCruzado.equalsIgnoreCase("S") && item.cruzarCheque.equalsIgnoreCase("N")) {
                    return errores.setErroresArchivo("Cruzar cheque", "Todos los eCheq del subconvenio deben ser cruzados.", numLinea, null);
                }
                if (eCheq.reqCheqNoALaOrden.equalsIgnoreCase("S") && item.clausula.equalsIgnoreCase("N")) {
                    return errores.setErroresArchivo("Clausula NO a la orden", "Todos los cheques del subconvenio deben contener cláusula de no a la orden.", numLinea, null);
                }
                if (fechaCreacion != null && eCheq.tolCheqFechaPrevia != 0) {
                    Date fechaCrea = Date.from(fechaCreacion.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    Date fechaToleranciaPrevia = DateUtils.restarDias(fechaCrea, eCheq.tolCheqFechaPrevia);
                    if (DateUtils.esFecha1MayorAFecha2(fechaToleranciaPrevia, fechaCrea)) {
                        return errores.setErroresArchivo("Tolerancia fecha previa", "La fecha de ejecución debe ser igual o mayor a la fecha del día.", numLinea, null);
                    }
                }
                if (item.medioPago.equals("013") && item.fechaEjecucion != null) {
                    Date fechaVenc = Date.from(fechaVencimient.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    Date fechaPlazoMaxDiferimiento = DateUtils.sumarDias(dateFechaEjec, eCheq.plzMaxDifCPD);
                    if (item.fechaVencimiento != null) {

                        if (DateUtils.esFecha1IgualAFecha2(dateFechaEjec, fechaVenc) || DateUtils.esFecha1MayorAFecha2(dateFechaEjec, fechaVenc)) {
                            return errores.setErroresArchivo("Error fecha diferido - ejecución", "Error en plazo de cheque con pago diferido.", numLinea, null);
                        }
                    }
                    if (item.fechaVencimiento == null || (item.fechaVencimiento != null && DateUtils.esFecha1MayorAFecha2(fechaVenc, fechaPlazoMaxDiferimiento))) {

                        return errores.setErroresArchivo("Error fecha diferido", "Error en plazo de cheque con pago diferido.", numLinea, null);
                    }
                }
            }
            if (item.fechaEjecucion != null && fechaCreacion != null) {
                Date fechaCrea = Date.from(fechaCreacion.atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date fechaMaximaAnticipoRecepcionLotes = DateUtils.sumarDias(fechaCrea, subConvenio.maxAnticLotPag);
                if (DateUtils.esFecha1MayorAFecha2(dateFechaEjec, fechaMaximaAnticipoRecepcionLotes)) {
                    return errores.setErroresArchivo("Maximo plazo cheque", "La fecha de ejecución de la orden es mayor que la máxima permitida.", numLinea, null);
                }
            }
        }

        if (!item.comprobante.isBlank() && item.comprobante.equals("ICA")) {
            if (subConvenio.envioCompxmail == 3) {
                if (!item.medioComunicacion.isBlank()) {
                    if (item.medioComunicacion.equals("003")) {
                        if (item.email.isBlank()) {
                            return errores.setErroresArchivo("Email invalido", "Debe ingresar un mail valido.", numLinea, null);
                        }
                    } else {
                        return errores.setErroresArchivo("Email medio de comunicacion error", "El medio de comunicación para el envió del comprobante de retenciones debe ser por mail de acuerdo al subconvenio.", numLinea, null);
                    }
                }
            } else {
                if (!item.medioComunicacion.isBlank() && item.medioComunicacion.equals("003") && item.email.isBlank()) {
                    return errores.setErroresArchivo("Email invalido", "Debe ingresar un mail valido.", numLinea, null);
                } else {
                    if (item.canalEntrega.isBlank()) {
                        return errores.setErroresArchivo("Canal de entrega invalido", "Debe informar el canal de entrega del comprobante.", numLinea, null);
                    }
                    if (item.sucursalEnviar.isBlank()) {
                        return errores.setErroresArchivo("Sucursal invalida", "El campo Sucursal es obligatorio debido a que se determinó la impresión de comprobantes de retenciones de acuerdo al subconvenio.", numLinea, null);
                    } else {
                        if (!sucursalEnviar.contains(item.sucursalEnviar)) {
                            return errores.setErroresArchivo("Sucursal invalida", "El valor del campo sucursal no es válido.", numLinea, null);
                        }

                    }
                }
            }
        }

        if (!item.medioComunicacion.isBlank() && (!item.medioComunicacion.equals("003") && !item.medioComunicacion.equals("999"))) {
            return errores.setErroresArchivo("Medio de comunicación invalido", "Medio de comunicación '003' o '009'", numLinea, null);
        } else {
            if (item.medioComunicacion.equals("003")) {
                if (item.email.isBlank()) {
                    return errores.setErroresArchivo("Email faltante", "Email faltante", numLinea, null);
                }

            } else {
                if (!item.email.isBlank()) {
                    return errores.setErroresArchivo("Error en mail", "vacio o debe ingresar medio de comunicación '003'", numLinea, null);
                }
            }
        }

//        if (!item.nroBeneficiario.isBlank()) {
//            BeneficiarioOB beneficiario = servicioBeneficiario.beneficiario(Integer.valueOf(item.nroBeneficiario)).get();
//            if (beneficiario == null) {
//                return errores.setErroresArchivo("Beneficiario inexistente", "El nro de beneficiario ingresado no coincide con uno existente.", numLinea, null);
//            }
//        }

        if (!item.nombreBeneficiario.isBlank()) {
            if (!item.tipoDocumento.equals("011")) {
                return errores.setErroresArchivo("Tipo de documento de beneficiario inválido.", "El tipo de documento debe ser '011'", numLinea, null);
            }
            if (!item.documento.matches("\\d+")) {
                return errores.setErroresArchivo("Documento de beneficiario inválido.", "El documento debe contener solo números.", numLinea, "Nro. documento");
            }
        } else {
            if (item.nroBeneficiario.isBlank()) {
                return errores.setErroresArchivo("Beneficiario inválido.", "Debe ingresar número o nombre de beneficiario.", numLinea, "Nro. Beneficiario");
            }
            if (!item.tipoDocumento.isBlank()) {
                return errores.setErroresArchivo("Tipo de documento de beneficiario inválido.", "No debe ingresar tipo de documento si no informó nombre de beneficiario.", numLinea, "Tipo de documento");
            }
            if (!item.documento.isBlank()) {
                return errores.setErroresArchivo("Documento de beneficiario inválido.", "No debe ingresar documento si no informó nombre de beneficiario.", numLinea, null);
            }
        }

        if (!item.mensaje.isEmpty()) {
            return errores.setErroresArchivo("Mensaje invalido", "El mensaje debe estar vacio", numLinea, null);
        }

        if (!item.canalEntrega.isBlank() && !item.canalEntrega.equals("BCO") && !item.canalEntrega.equals("ADH") && (item.medioPago.equals("002") || item.medioPago.equals("003"))) {
            return errores.setErroresArchivo("Canal de entrega invalido", "Canal de entrega debe ser vacio, 'BCO' o 'ADH' ", numLinea, null);
        }

        return respuesta("0");

    }

    public static Objeto validarFooter(ArchivoPapDTO.Trailer trailer, BigDecimal importeTotal, int totalRegistros){
        ErrorArchivoOB errores = new ErrorArchivoOB();
        if (!trailer.registroId.equals("FT")){
            return errores.setErroresArchivo("Registro Id invalido", "El registro ID debe ser 'FT'", 0, null);
        }
        if (Integer.parseInt(trailer.catidadRegistros)!=totalRegistros+2){
            return errores.setErroresArchivo("Cantidad de registros invalida", "La cantidad de registros del lote no coincide con los ingresados", 0, null);
        }
        BigDecimal importeFooter = BigDecimal.valueOf(Double.parseDouble(eliminarCerosIzquierda(trailer.importeTotal)) / 100.00);
        if (importeFooter.compareTo(importeTotal)!=0){
            return errores.setErroresArchivo("Importe total invalido", "El importe total del lote no coincide con los ingresados", 0, null);
        }
        return respuesta("0");
    }


    private static boolean ingresoTodosLosCamposTransferencia(ArchivoPapDTO.Body item) {

        return !item.tipoCuentaCredito.isBlank()
                && !item.monedaCuentaCredito.isBlank()
                && !item.cbuCredito.isBlank();
    }

    private static boolean ingresoAlgunCampoTransferencia(ArchivoPapDTO.Body item) {

        if (ingresoTodosLosCamposTransferencia(item)) {
            return false;
        }
        return !item.tipoCuentaCredito.isBlank()
                || !item.monedaCuentaCredito.isBlank()
                || !item.cbuCredito.isBlank();

    }

    private static String agregarCerosIzquierda(int numero) {
        String numeroStr = String.valueOf(numero);
        int longitudActual = numeroStr.length();
        int cerosFaltantes = 7 - longitudActual;

        if (cerosFaltantes > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cerosFaltantes; i++) {
                sb.append('0');
            }
            sb.append(numeroStr);
            return sb.toString();
        } else {
            return numeroStr;
        }
    }
}