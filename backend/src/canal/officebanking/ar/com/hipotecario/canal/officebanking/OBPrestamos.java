package ar.com.hipotecario.canal.officebanking;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.inversiones.ComprobantePrestamosOB;
import ar.com.hipotecario.backend.servicio.api.prestamos.ApiPrestamos;
import ar.com.hipotecario.backend.servicio.api.prestamos.Prestamos;
import ar.com.hipotecario.backend.servicio.api.prestamos.PrestamosCuotas;
import ar.com.hipotecario.backend.servicio.api.prestamos.Pagos;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.officebanking.dto.ErrorGenericoOB;
import ar.com.hipotecario.canal.officebanking.lib.Pdf;
import com.github.jknack.handlebars.internal.lang3.StringUtils;

import java.text.SimpleDateFormat;

public class OBPrestamos extends ModuloOB {
    public static Object obtenerPrestamo(ContextoOB contexto) {
        String id = contexto.parametros.string("numeroProducto");
        Boolean detalle = true;
        Prestamos.PrestamosPorId prestamo = ApiPrestamos.prestamosPorId(contexto, id, detalle).tryGet();
        return respuesta("datos", prestamo);
    }

    public static Object obtenerPosicionConsolidada(ContextoOB contexto) {
        String tipoEstado = contexto.parametros.string("tipoEstado", "todos");
        Boolean buscansp = contexto.parametros.bool("buscansp");
        buscansp = false;

        // Validar tipoEstado
        List<String> valoresPermitidos = Arrays.asList("todos", "vigente", "cancelado");
        if (!valoresPermitidos.contains(tipoEstado)) {
            throw new IllegalArgumentException("El parámetro tipoestado debe ser 'todos', 'vigente' o 'cancelado'");
        }
        Prestamos prestamo = ApiPrestamos.prestamosPorEstados(contexto, contexto.sesion().empresaOB.idCobis, tipoEstado, buscansp).tryGet();
        return respuesta("datos", prestamo);
    }

    public static Object obtenercuotas(ContextoOB contexto) {
        String numeroProducto = contexto.parametros.string("numeroProducto");
        String cuota = "0";

        List<PrestamosCuotas.PrestamoCuota> todasLasCuotas = new ArrayList<>();
        boolean continuar = true;

        while (continuar) {
            PrestamosCuotas cuotasParciales = ApiPrestamos.prestamosCuotasPorCuota(contexto, numeroProducto, cuota).tryGet();

            if (cuotasParciales == null || cuotasParciales.isEmpty()) {
                continuar = false;
            } else {
                List<PrestamosCuotas.PrestamoCuota> lista = cuotasParciales.list();
                todasLasCuotas.addAll(lista);

                PrestamosCuotas.PrestamoCuota ultimaCuota = lista.get(lista.size() - 1);
                cuota = ultimaCuota.numero;
            }
        }
        return respuesta("datos", todasLasCuotas);
    }

    public static Object obtenercuotasPorFecha(ContextoOB contexto) {
        String numeroProducto = contexto.parametros.string("numeroProducto");
        Fecha fechaDesde = contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd");
        Fecha fechaHasta = contexto.parametros.fecha("fechaHasta", "yyyy-MM-dd");

        if (!validarFormatoDeFechas(fechaDesde.toString()) || !validarFormatoDeFechas(fechaHasta.toString())) {
            return new ErrorGenericoOB().setErrores("Las fechas no cumplen con el formato esperado.",
                    "Solo se aceptan fechas en el formato: dd/MM/yyyy");
        }

        PrestamosCuotas cuotas = ApiPrestamos.prestamosCuotasPorFechas(contexto, numeroProducto, fechaDesde, fechaHasta).tryGet();
        return respuesta("datos", cuotas);
    }

    public static boolean validarFormatoDeFechas(String fecha) {
        String formatoFecha = "yyyy-MM-dd";
        SimpleDateFormat validarFormatoFecha = new SimpleDateFormat(formatoFecha);
        try {
            validarFormatoFecha.setLenient(false);
            validarFormatoFecha.parse(fecha);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Object obtenerlistadoPagosRealizados(ContextoOB contexto) {
        String idPrestamo = contexto.parametros.string("nroProducto");
        Fecha fechaDesde = contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd");
        Fecha fechaHasta = Fecha.ahora();

        if (!validarFormatoDeFechas(fechaDesde.toString()) || !validarFormatoDeFechas(fechaHasta.toString())) {
            return new ErrorGenericoOB().setErrores("Las fechas no cumplen con el formato esperado.",
                    "Solo se aceptan fechas en el formato: dd/MM/yyyy");
        }
        Pagos pagos = ApiPrestamos.pagosPrestamos(contexto, idPrestamo, fechaDesde, fechaHasta).tryGet();

        return respuesta("datos", pagos);
    }

    public static Object generarComprobantePrestamoPDF(ContextoOB contexto) {
        String estado = contexto.parametros.string("estado", "A");
        String operacion = contexto.parametros.string("operacion");
        String recibo = contexto.parametros.string("recibo");
        String siguiente = contexto.parametros.string("siguiente", String.valueOf(0));

        //String siguiente = "0";
        boolean hayMasConceptos = true;


        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("#,##0.00", symbols);
        DecimalFormat df3 = new DecimalFormat("#,##0.000", symbols);

        List<ComprobantePrestamosOB.ConceptoAplicado> conceptosTotales = new ArrayList<>();
        ComprobantePrestamosOB response = null;


        ComprobantePrestamosOB responseParcial;
        do {
            responseParcial = ApiPrestamos.comprobantePrestamo(contexto, estado, operacion, recibo, siguiente).tryGet();

        if (responseParcial == null || responseParcial.codigoHttp() == 404 || empty(responseParcial)) {
            return respuesta("No hay datos disponibles");
        }

        if (response == null) {
            response = responseParcial; //primera, datos generales
        }
        List<ComprobantePrestamosOB.ConceptoAplicado> conceptos = responseParcial.conceptosAplicados;
        if (conceptos != null && !conceptos.isEmpty()) {
            conceptosTotales.addAll(conceptos);

            if (conceptos.size() < 20) {
                hayMasConceptos = false; // fin
            }else{
                siguiente = String.valueOf(conceptos.get(conceptos.size() - 1).sig);
            }
        } else {
            hayMasConceptos = false;
        }
    }while (hayMasConceptos);

        List<Map<String, String>> conceptosList = new ArrayList<>();
        Map<String, String> parametros = new HashMap<>();
        parametros.put("FECHA_HOY", Fecha.hoy().string("dd-MM-yyyy"));
        parametros.put("NRO_RECIBO", recibo);

        if (response.datosOperacion != null && !response.datosOperacion.isEmpty()) {
            ComprobantePrestamosOB.DatoOperacion op = response.datosOperacion.get(0);
            parametros.put("TIPO_OPERACION", op.descToperacion);
            parametros.put("OPMONEDA", op.descMoneda);
            parametros.put("NRO_OP", operacion);
            parametros.put("NRO_TRAMITE", String.valueOf(op.tramite));
            parametros.put("OFICIAL_OP", op.nomOficial);
            parametros.put("SUCURSAL_OP", op.descOfi);
            parametros.put("UBICACIÓN_OP", op.descCiudad);
            parametros.put("NRO_CLIENTE", String.valueOf(op.cliente));
            String cuit = op.cuilCuit;
            if (cuit != null && cuit.length() == 11) {
                String cuitFormateado = cuit.substring(0, 2) + "-" + cuit.substring(2, 10) + "-" + cuit.substring(10);
                parametros.put("CUIT_CTE", cuitFormateado);
            } else {
                parametros.put("CUIT_CTE", cuit);
            }
            parametros.put("IVA_CTE", op.desSituacion);
            parametros.put("RAZON_SOCIAL", op.nombre);
            parametros.put("DIRECCION_CTE", op.direccion);
            parametros.put("CP_CTE", op.postal);
            parametros.put("FECHA_VCIA", op.fechaIni);
            parametros.put("FECHA_VTO", op.fechaFin);
            parametros.put("PLAZO_OP", String.valueOf(op.plazo));
            parametros.put("TIPO_PLAZO", op.desc_tplazo);
            parametros.put("TIPO_CUOTA", op.descTdividendo);
            parametros.put("MONTO_OP", df.format(op.monto));
            parametros.put("CFT_TNA", df3.format(op.tasa));
            parametros.put("SALDO_CAPITAL", df.format(op.saldoCapital));
        }

        if (response.detallePago != null && !response.detallePago.isEmpty()) {
            ComprobantePrestamosOB.DetallePago dp = response.detallePago.get(0);
            parametros.put("FECHA_PG", dp.fecha);
            parametros.put("FORMA_PG", dp.producto);
            parametros.put("DETALLE_PG", response.detallePago.get(0).descfPago);
            parametros.put("REF_PF", dp.cuenta);
            parametros.put("BEN_PF", dp.benefic);
            parametros.put("MON_PG", response.datosOperacion.get(0).descMoneda);
            parametros.put("MONTO_PG", df.format(dp.valor));
            parametros.put("MONTODESCR_PG", NumeroEnLetras.convertir(dp.valor));
            parametros.put("SIMBOLO", response.conceptosAplicados.get(0).simbolo);
        }
        if (!conceptosTotales.isEmpty()) {
            String fechaAplicacion = response.detallePago.get(0).fecha;
            for (ComprobantePrestamosOB.ConceptoAplicado concepto : conceptosTotales) {
                Map<String, String> item = new HashMap<>();
                item.put("FECHA_APL", fechaAplicacion);
                item.put("RUBRO", concepto.concepto);
                item.put("DETALLE", concepto.descon);
                item.put("CUOTA", String.valueOf(concepto.cuota));
                item.put("TNA", String.valueOf(concepto.tna));
                item.put("TEA", String.valueOf(concepto.tea));
                item.put("TEM", String.valueOf(concepto.tem));
                item.put("SIMBOLO", String.valueOf(concepto.simbolo));
                item.put("MONTO", df.format(concepto.valcon));
                if ( response.datosOperacion.get(0).moneda != 80) {
                    item.put("MONTMIN", df.format(concepto.montomn));
                }else{
                    item.put("MONTMIN", " ");
                }
                conceptosList.add(item);
            }
        }
        contexto.responseHeader("Content-Disposition", "attachment; filename=Comprobante_Prestamo_" + recibo + ".pdf");

        try {
            return Pdf.generarPDFPrestamos("comprobante_prestamo", parametros, conceptosList);
        } catch (Exception e) {
            return respuesta("Error interno generando el comprobante");
        }
    }
}


