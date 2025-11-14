package ar.com.hipotecario.canal.officebanking;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.catalogo.ApiCatalogo;
import ar.com.hipotecario.backend.servicio.api.catalogo.Paises;
import ar.com.hipotecario.backend.servicio.api.catalogo.Provincias;
import ar.com.hipotecario.backend.servicio.api.catalogo.Sucursales;
import ar.com.hipotecario.backend.servicio.api.empresas.AltaConsultaCapitaOB;
import ar.com.hipotecario.backend.servicio.api.empresas.AltaConsultaCapitaRequest;
import ar.com.hipotecario.backend.servicio.api.empresas.ApiEmpresas;
import ar.com.hipotecario.backend.servicio.api.notificaciones.ApiNotificaciones;
import ar.com.hipotecario.backend.servicio.api.notificaciones.EnvioEmail;
import ar.com.hipotecario.backend.servicio.api.recaudaciones.ApiRecaudaciones;
import ar.com.hipotecario.backend.servicio.api.recaudaciones.ConveniosOB;
import ar.com.hipotecario.backend.servicio.api.recaudaciones.ConveniosOB.Convenio;
import ar.com.hipotecario.backend.servicio.api.recaudaciones.DetalleLotesOB;
import ar.com.hipotecario.backend.servicio.api.recaudaciones.LotesOB;
import ar.com.hipotecario.canal.officebanking.dto.ErrorArchivoOB;
import ar.com.hipotecario.canal.officebanking.dto.cobranzaIntegral.ReporteCobranzaCSVDTO;
import ar.com.hipotecario.canal.officebanking.dto.cobranzaIntegral.ReporteCobranzaConvenioDTO;
import ar.com.hipotecario.canal.officebanking.dto.haberes.AcreditacionesCSVDTO;
import ar.com.hipotecario.canal.officebanking.dto.haberes.NominaCSVDTO;
import ar.com.hipotecario.canal.officebanking.dto.pagosMasivos.OrdenDePagoDTO;
import ar.com.hipotecario.canal.officebanking.enums.EnumAccionesOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumBancosOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoPagosHaberesOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadosRegistrosAcreditacionesOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumMulticausalAcreditacionesOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;
import ar.com.hipotecario.canal.officebanking.enums.planSueldo.nomina.EnumEstadoCivilNominaOB;
import ar.com.hipotecario.canal.officebanking.enums.planSueldo.nomina.EnumOcupacionNominaOB;
import ar.com.hipotecario.canal.officebanking.enums.planSueldo.nomina.EnumSituacionLaboralNominaOB;
import ar.com.hipotecario.canal.officebanking.enums.planSueldo.nomina.EnumTipoCuitCuilNominaOB;
import ar.com.hipotecario.canal.officebanking.jpa.dto.haberes.ArchivoAcreditacionHeader;
import ar.com.hipotecario.canal.officebanking.jpa.dto.haberes.ArchivoAcreditacionItem;
import ar.com.hipotecario.canal.officebanking.jpa.dto.nomina.ArchivoNominaItem;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioAccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioAcreditacionesConfigOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioBandejaAccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioErroresArchivoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadoPagosHaberesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioHistorialPagoDeHaberesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioMovimientosFCL;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioNominaConfidencial;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioNominaConfigOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioNominaCuentasCreadasOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioPagoHaberesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioTipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.AcreditacionesConfigOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.ErroresArchivosOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.EstadoPagosHaberesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.FondoCeseLaboralOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.NominaConfidencialOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.NominaConfigOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.NominaCuentasCreadasOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.PagoDeHaberesOB;
import ar.com.hipotecario.canal.officebanking.util.CbuUtil;
import ar.com.hipotecario.canal.officebanking.util.StringUtil;
import com.azure.storage.blob.BlobClient;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import net.bytebuddy.asm.Advice;
import org.apache.commons.lang3.EnumUtils;

import javax.servlet.ServletException;
import javax.servlet.http.Part;
import javax.sql.rowset.serial.SerialBlob;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.security.SecureRandom;
import static ar.com.hipotecario.canal.officebanking.OBManejoArchivos.blobToString;
import static ar.com.hipotecario.canal.officebanking.OBManejoArchivos.inputStreamToBlobANSI;
import static ar.com.hipotecario.canal.officebanking.OBManejoArchivos.inputStreamToBlobUTF8;

public class OBPagoHaberes extends ModuloOB {
    final static String OPERACION = "S";
    final static String CANAL_OB = "02";
    private static final String CLASE = OBPagoHaberes.class.getSimpleName().toUpperCase();

    public static Object consultaCovenios(ContextoOB contexto) {
        SesionOB sesion = contexto.sesion();
        List<Objeto> convenios = new ArrayList<>();

        ConveniosOB listConveniosOB = ApiRecaudaciones.convenios(contexto, String.valueOf(sesion.empresaOB.cuit), OPERACION).get();

        convenios.addAll(listConveniosOB.stream().map(conv -> {
                    Objeto data = new Objeto();
                    Objeto cuenta = OBCuentas.cuenta(contexto, conv.cuenta);

                    if (!(empty(cuenta) || cuenta == null)) {
                        data.set("idConvenio", conv.codigoConvenio);
                        data.set("numeroProducto", conv.cuenta);
                        data.set("saldoGirar", cuenta.get("saldoGirar"));
                        data.set("moneda", cuenta.get("moneda"));
                    }
                    return data;
                }).filter(data -> Objects.nonNull(data.get("numeroProducto")))
                .toList());
        return respuesta("datos", convenios);
    }

    public static Object consultaLotes(ContextoOB contexto) {
        String convenio = contexto.parametros.string("convenio");
        String fechadesde = contexto.parametros.string("fechadesde");
        String fechahasta = contexto.parametros.string("fechahasta");

        List<Objeto> lotes = getLotes(contexto, convenio, fechadesde, fechahasta);

        return respuesta("datos", lotes);
    }

    public static List<Objeto> getLotes(ContextoOB contexto, String convenio, String fechadesde, String fechahasta) {

        List<Objeto> lotes = new ArrayList<>();

        LotesOB listLotesOB = ApiRecaudaciones.consultaLotes(contexto, CANAL_OB, convenio, fechadesde, fechahasta).get();

        lotes.addAll(listLotesOB.stream().map(conv -> {
            Objeto data = new Objeto();

            data.set("idConvenio", conv.convenio);
            data.set("fechaOrigen", conv.fechaCarga);
            data.set("nombreArchivo", conv.nombreArchivo);
            data.set("numeroLote", conv.numeroLote);
            data.set("estado", conv.estado);
            data.set("importe", conv.importe);
            data.set("observaciones", conv.observaciones);
            data.set("cantidad", conv.cantidad);

            return data;
        }).toList());

        return lotes;
    }

    public static Object detalleSolicitudAcreditaciones(ContextoOB contexto) {
        Integer idPago = contexto.parametros.integer("idSolicitudPago");

        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
        Objeto pagoDetalle = new Objeto();
        SesionOB sesion = contexto.sesion();
        ServicioPagoHaberesOB servicioPagoHaberes = new ServicioPagoHaberesOB(contexto);
        ServicioEstadoPagosHaberesOB servicioEstadoPagoHaberes = new ServicioEstadoPagosHaberesOB(contexto);

        PagoDeHaberesOB pago = servicioPagoHaberes.find(idPago).get();
        if (pago == null || !pago.empresa.idCobis.equals(sesion.empresaOB.idCobis)) {
            return respuesta("DATOS_INVALIDOS");
        }

        pagoDetalle.set("id", pago.id);
        pagoDetalle.set("monto", pago.monto);
        pagoDetalle.set("cantidadRegistros", pago.cantidadRegistros);
        pagoDetalle.set("cuenta", pago.cuentaOrigen);
        pagoDetalle.set("tipo", pago.tipoProducto);
        pagoDetalle.set("moneda", pago.moneda.simbolo);
        pagoDetalle.set("creadoPor", pago.usuario.nombre + " " + pago.usuario.apellido);
        pagoDetalle.set("estado", pago.estadoBandeja.descripcion);
        
        BandejaOB bandeja = servicioBandeja.find(pago.id).get();
        pagoDetalle.set("firmas", OBFirmas.obtenerDatosFirmantes(contexto, bandeja));

        if (pago.estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String fechadesde = String.valueOf(LocalDate.parse(pago.fechaCargaLote.toLocalDate().minusDays(1).toString(), formato));
            String fechahasta;

            if (pago.fechaCargaLote.plusDays(10).isAfter(LocalDate.now().plusDays(1).atStartOfDay())) {
                fechahasta = String.valueOf(LocalDate.parse(LocalDate.now().toString(), formato));
            } else {
                fechahasta = String.valueOf(LocalDate.parse(pago.fechaCargaLote.plusDays(10).toString(), formato));
            }

            List<Objeto> lotes = getLotes(contexto, pago.convenio.toString(), fechadesde, fechahasta);

            for (Objeto str : lotes) {
                if (str.get("nombreArchivo").equals(pago.nombreArchivo)) {
                    pagoDetalle.set("id", pago.id);
                    pagoDetalle.set("idConvenio", str.get("idConvenio"));
                    pagoDetalle.set("fechaOrigen", str.get("fechaOrigen"));
                    pagoDetalle.set("nombreArchivo", str.get("nombreArchivo"));
                    pagoDetalle.set("numeroLote", str.get("numeroLote"));
                    pagoDetalle.set("importe", str.get("importe"));
                    pagoDetalle.set("observaciones", str.get("observaciones"));
                    pagoDetalle.set("cantidad", str.get("cantidad"));

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    pago.fechaCargaLote = LocalDateTime.parse(str.get("fechaOrigen").toString(), formatter);
                    pago.numeroLote = str.get("numeroLote").toString();
                    servicioPagoHaberes.update(pago);

                    String estadoLote = str.get("estado").toString();
                    if (!estadoLote.equals(pago.estado.descripcion)) {
                        EstadoPagosHaberesOB estadoPagoHaberes;

                        estadoPagoHaberes = servicioEstadoPagoHaberes.find(EnumEstadoPagosHaberesOB.EN_BANDEJA.getCodigo()).get();
                        if (estadoLote.toUpperCase().equals(estadoPagoHaberes.descripcion.replaceAll("_", " "))) {
                            pago.estado = estadoPagoHaberes;
                        }

                        estadoPagoHaberes = servicioEstadoPagoHaberes.find(EnumEstadoPagosHaberesOB.A_PROCESAR.getCodigo()).get();
                        if (estadoLote.toUpperCase().equals(estadoPagoHaberes.descripcion.replaceAll("_", " ")) || estadoLote.equalsIgnoreCase("Ingreso Anticipado")) {
                            pago.estado = estadoPagoHaberes;
                        }

                        estadoPagoHaberes = servicioEstadoPagoHaberes.find(EnumEstadoPagosHaberesOB.PROCESADO.getCodigo()).get();
                        if (estadoLote.toUpperCase().equals(estadoPagoHaberes.descripcion)) {
                            pago.estado = estadoPagoHaberes;
                        }

                        estadoPagoHaberes = servicioEstadoPagoHaberes.find(EnumEstadoPagosHaberesOB.RECHAZADO.getCodigo()).get();
                        if (estadoLote.toUpperCase().equals(estadoPagoHaberes.descripcion)) {
                            pago.estado = estadoPagoHaberes;
                        }

                        estadoPagoHaberes = servicioEstadoPagoHaberes.find(EnumEstadoPagosHaberesOB.PROCESADO_PARCIALMENTE.getCodigo()).get();
                        if (estadoLote.toUpperCase().equals(estadoPagoHaberes.descripcion.replaceAll("_", " "))) {
                            pago.estado = estadoPagoHaberes;
                        }

                        estadoPagoHaberes = servicioEstadoPagoHaberes.find(EnumEstadoPagosHaberesOB.ESPERANDO_FONDEO.getCodigo()).get();
                        if (estadoLote.toUpperCase().equals(estadoPagoHaberes.descripcion.replaceAll("_", " ")) || estadoLote.equalsIgnoreCase("SIN SALDO")) {
                            pago.estado = estadoPagoHaberes;
                        }

                        servicioPagoHaberes.update(pago);
                    }
                }
            }
            pagoDetalle.set("estadoOperacion", pago.estado.descripcion);
        }

        return respuesta("datos", pagoDetalle);
    }

    public static String convertirNombreHaberes(String nombreArchivo){
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
        _convenio = entConvenio.substring(3, entConvenio.length());
        if (_convenio.length()==4) entConvenio = entConvenio.substring(0,3)+"0"+_convenio;
        return entConvenio+"_"+fecha+"_"+hora+"_of00000.txt";
    }

    public static Object historialAcreditaciones(ContextoOB contexto) {
        Fecha fechaDesde = contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd", null);
        Fecha fechaHasta = contexto.parametros.fecha("fechaHasta", "yyyy-MM-dd", null);
        Integer convenio = contexto.parametros.integer("convenio", null);
        Boolean previsualizacion = contexto.parametros.bool("previsualizacion");
        String estadoFiltro = contexto.parametros.string("estado", null);
        String tipoProducto = "Plan Sueldo";

        ServicioPagoHaberesOB servicioPagoHaberes = new ServicioPagoHaberesOB(contexto);
        ServicioEstadoPagosHaberesOB servicioEstadoPagosHaberes = new ServicioEstadoPagosHaberesOB(contexto);
        Objeto respuesta = new Objeto();
        EstadoPagosHaberesOB estadoAFiltrar = null;

        String estadoFiltroEnum = null;
        if (estadoFiltro != null) {
            estadoFiltroEnum = estadoFiltro.replaceAll(" ", "_");
            if (!EnumUtils.isValidEnum(EnumEstadoPagosHaberesOB.class, estadoFiltroEnum)) { //verifica si el estadoFiltroEnum (versión modificada de estadoFiltro) es un valor válido dentro del enumerador
                if (estadoFiltroEnum.equals(EnumEstadoBandejaOB.PENDIENTE_FIRMA.name()) || estadoFiltro.equals(EnumEstadoBandejaOB.PARCIALMENTE_FIRMADA.name())) {
                    estadoAFiltrar = servicioEstadoPagosHaberes.find(EnumEstadoPagosHaberesOB.EN_BANDEJA.getCodigo()).get();
                }
            } else {
                estadoAFiltrar = servicioEstadoPagosHaberes.find(EnumEstadoPagosHaberesOB.valueOf(estadoFiltroEnum).getCodigo()).get();
            }
        }

        List<PagoDeHaberesOB> movimientos = servicioPagoHaberes.filtrarMovimientosHistorial(contexto.sesion().empresaOB, fechaDesde, fechaHasta, convenio, tipoProducto, estadoAFiltrar).get();

        if (estadoFiltro != null && estadoFiltroEnum.equals(EnumEstadoBandejaOB.PENDIENTE_FIRMA.name())) {
            movimientos = movimientos.stream().filter(m -> m.estadoBandeja.id.equals(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo())).toList();
        }

        if (estadoFiltro != null && estadoFiltroEnum.equals(EnumEstadoBandejaOB.PARCIALMENTE_FIRMADA.name())) {
            movimientos = movimientos.stream().filter(m -> m.estadoBandeja.id.equals(EnumEstadoBandejaOB.PARCIALMENTE_FIRMADA.getCodigo())).toList();
        }

        for (PagoDeHaberesOB m : movimientos) {
            Objeto datos = new Objeto();
            Objeto estado = new Objeto();


            if (m.estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo() && (m.estado.id == EnumEstadoPagosHaberesOB.A_PROCESAR.getCodigo() || m.estado.id == EnumEstadoPagosHaberesOB.ESPERANDO_FONDEO.getCodigo() || m.estado.id == EnumEstadoPagosHaberesOB.PROCESADO_PARCIALMENTE.getCodigo())) {

                DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String fechadesde = String.valueOf(LocalDate.parse(m.fechaCargaArchivo.toString(), formato));
                Fecha fechaHast = new Fecha(m.fechaCargaArchivo.toString(), "yyyy-MM-dd");
                String fechaHastaa;
                try {
                    fechaHastaa = ApiCatalogo.diaBancario(contexto, fechaHast).tryGet().diaHabilPosterior.sumarDias(2).toString();
                } catch (Exception e) {
                    fechaHastaa = m.fechaCargaLote.plusDays(3).toString();
                }

                List<Objeto> lotes = getLotes(contexto, m.convenio.toString(), fechadesde, fechaHastaa);
                String nombreArchivo = null;
                try{
                    nombreArchivo = convertirNombreHaberes(m.nombreArchivo);
                }catch (Exception e){

                }
                for (Objeto str : lotes) {
                    boolean hayR = false;
                    if (str.get("nombreArchivo").equals(nombreArchivo)) {
                        m.numeroLote = str.get("numeroLote").toString();
                        if (str.get("estado").equals("Sin Saldo")) {
                            EstadoPagosHaberesOB estadoPagoHaberes = servicioEstadoPagosHaberes.find(EnumEstadoPagosHaberesOB.ESPERANDO_FONDEO.getCodigo()).get();
                            m.estado = estadoPagoHaberes;
                            servicioPagoHaberes.update(m);
                            datos.set("estadoOperacion", m.estado.descripcion);
                        } else if (str.get("estado").equals("Aceptado")||str.get("estado").equals("Procesado")) {
                            DetalleLotesOB detalle = ApiRecaudaciones.consultaDetalleLotes(contexto, m.numeroLote, String.valueOf(m.convenio)).get();
                            int i = 0;
                            boolean estaAceptado = false;
                            while (i < detalle.size()) {
                                if (!detalle.get(i).estado.equals("R")) {
                                    estaAceptado = true;
                                }
                                if (detalle.get(i).estado.equals("R")){
                                    hayR = true;
                                }
                                i++;
                            }
                            if (hayR){
                                m.estado = estaAceptado ? servicioEstadoPagosHaberes.find(EnumEstadoPagosHaberesOB.PROCESADO_PARCIALMENTE.getCodigo()).get() : servicioEstadoPagosHaberes.find(EnumEstadoPagosHaberesOB.RECHAZADO.getCodigo()).get();
                            }

                        }

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate date = LocalDate.parse(str.get("fechaOrigen").toString(), formatter);
                        m.fechaCargaLote = date.atStartOfDay();
                        servicioPagoHaberes.update(m);

                        String estadoLote = str.get("estado").toString();
                        if (!estadoLote.equals(m.estado.descripcion)) {
                            EstadoPagosHaberesOB estadoPagoHaberes;

                            estadoPagoHaberes = servicioEstadoPagosHaberes.find(EnumEstadoPagosHaberesOB.EN_BANDEJA.getCodigo()).get();
                            if (estadoLote.toUpperCase().equals(estadoPagoHaberes.descripcion.replaceAll("_", " "))) {
                                m.estado = estadoPagoHaberes;
                            }

//                            estadoPagoHaberes = servicioEstadoPagosHaberes.find(EnumEstadoPagosHaberesOB.A_PROCESAR.getCodigo()).get();
//                            if (estadoLote.toUpperCase().equals(estadoPagoHaberes.descripcion.replaceAll("_", " ")) || estadoLote.equalsIgnoreCase("Ingreso Anticipado")) {
//                                m.estado = estadoPagoHaberes;
//                            }

                            estadoPagoHaberes = servicioEstadoPagosHaberes.find(EnumEstadoPagosHaberesOB.PROCESADO.getCodigo()).get();
                            if (estadoLote.toUpperCase().equals(estadoPagoHaberes.descripcion)&&!hayR) {
                                m.estado = estadoPagoHaberes;
                            }

                            estadoPagoHaberes = servicioEstadoPagosHaberes.find(EnumEstadoPagosHaberesOB.RECHAZADO.getCodigo()).get();
                            if (estadoLote.toUpperCase().equals(estadoPagoHaberes.descripcion)) {
                                m.estado = estadoPagoHaberes;
                            }

                            estadoPagoHaberes = servicioEstadoPagosHaberes.find(EnumEstadoPagosHaberesOB.PROCESADO_PARCIALMENTE.getCodigo()).get();
                            if (estadoLote.toUpperCase().equals(estadoPagoHaberes.descripcion.replaceAll("_", " "))) {
                                m.estado = estadoPagoHaberes;
                            }

                            estadoPagoHaberes = servicioEstadoPagosHaberes.find(EnumEstadoPagosHaberesOB.ESPERANDO_FONDEO.getCodigo()).get();
                            if (estadoLote.toUpperCase().equals(estadoPagoHaberes.descripcion.replaceAll("_", " ")) || estadoLote.equalsIgnoreCase("SIN SALDO")) {
                                m.estado = estadoPagoHaberes;
                            }

                            servicioPagoHaberes.update(m);
                        }

                    }
                }
                datos.set("estadoOperacion", m.estado.descripcion);
            } else
                datos.set("estadoOperacion", m.estado.descripcion);

            datos.set("idBandeja", m.id);
            datos.set("convenio", m.convenio);
            datos.set("descripcion", m.nombreArchivo);

            estado.set("id", m.estadoBandeja.id);
            estado.set("descripcionCorta", m.estadoBandeja.descripcion);
            datos.set("estado", estado);

            datos.set("monto", m.monto);
            datos.set("fechaCreacion", m.fechaCreacion.toLocalDate().toString());

            respuesta.add(datos);

            if (previsualizacion) {
                if (respuesta.toList().size() == 5) {
                    return respuesta("datos", respuesta);
                }
            }

        }

        return respuesta("datos", respuesta);
    }


    public static Object historialNominas(ContextoOB contexto) {
        Fecha fechaDesde = contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd", null);
        Fecha fechaHasta = contexto.parametros.fecha("fechaHasta", "yyyy-MM-dd", null);
        Integer convenio = contexto.parametros.integer("convenio", null);
        Boolean previsualizacion = contexto.parametros.bool("previsualizacion");
        String estadoFiltro = contexto.parametros.string("estado", null);
        String tipoProducto = "Nomina";

        ServicioPagoHaberesOB servicioPagoHaberes = new ServicioPagoHaberesOB(contexto);
        ServicioEstadoPagosHaberesOB servicioEstadoPagosHaberes = new ServicioEstadoPagosHaberesOB(contexto);
        Objeto respuesta = new Objeto();
        EstadoPagosHaberesOB estadoAFiltrar = null;
        String estadoFiltroEnum;

        if (estadoFiltro != null) {
            estadoFiltroEnum = estadoFiltro.replaceAll(" ", "_");
            estadoAFiltrar = servicioEstadoPagosHaberes.find(EnumEstadoPagosHaberesOB.valueOf(estadoFiltroEnum).getCodigo()).get();
        }

        List<PagoDeHaberesOB> movimientos = servicioPagoHaberes.filtrarMovimientosHistorial(contexto.sesion().empresaOB, fechaDesde, fechaHasta, convenio, tipoProducto, estadoAFiltrar).get();

        for (PagoDeHaberesOB m : movimientos) {
            Objeto datos = new Objeto();
            Objeto estado = new Objeto();

            datos.set("idBandeja", m.id);
            datos.set("convenio", m.convenio);
            datos.set("descripcion", m.nombreArchivo);
            datos.set("estadoOperacion", m.estado.descripcion);

            estado.set("id", m.estado.id);
            estado.set("descripcionCorta", m.estado.descripcion);
            datos.set("estado", estado);

            datos.set("monto", null);
            datos.set("fechaCreacion", m.fechaCreacion.toLocalDate().toString());

            respuesta.add(datos);

            if (previsualizacion) {
                if (respuesta.toList().size() == 5) {
                    return respuesta("datos", respuesta);
                }
            }
        }

        return respuesta("datos", respuesta);
    }

    public static Object precargaAcreditaciones(ContextoOB contexto) {
        Objeto dato = new Objeto();
        Objeto respuesta = new Objeto();
        List<String> columnas = new ArrayList<>();
        ErrorArchivoOB errores = new ErrorArchivoOB();

        boolean tieneRestriccion = false;
        SesionOB sesionOB = contexto.sesion();
        ServicioNominaConfidencial servicioNominaConfidencial = new ServicioNominaConfidencial(contexto);
        NominaConfidencialOB resultado = servicioNominaConfidencial.findByUsuarioEmpresa(sesionOB.usuarioOB, sesionOB.empresaOB).tryGet();
        if (resultado != null && resultado.acreditacion) {
            tieneRestriccion = true;
        }

        try {
            Part filePart = contexto.request.raw().getPart("archivo");
            Integer convenio = contexto.parametros.integer("convenio");
            Integer tipoProducto = contexto.parametros.integer("tipoProducto");
            String cuentaOrigen = contexto.parametros.string("cuentaOrigen");
            Integer numeroPagina = contexto.parametros.integer("numeroPagina", 1);
            int registrosPorPagina = 60;
            InputStream inputStream = filePart.getInputStream();
            Blob archivo = inputStreamToBlobANSI(inputStream);

            String[] tipoExtensionTXT = filePart.getSubmittedFileName().split("\\.");

            if (!tipoExtensionTXT[1].equalsIgnoreCase("txt")) {
                return errores.setErroresArchivo(
                        "El archivo tiene un formato inválido",
                        "Sólo aceptamos archivos con la extensión .txt.\r\n"
                                + "Por favor generá el archivo correcto desde el template y subilo nuevamente.", null, null);
            }

            //validar si el archivo ya fue cargado
            try {
                OBValidacionesAcreditaciones.validarArchivo(contexto, filePart.getSubmittedFileName());
            } catch (Exception e) {
                return errores.setErroresArchivo("El archivo tiene errores", e.getMessage(), null, null);
            }

            //validar nombre de archivo
            try {
                OBValidacionesAcreditaciones.validarNombreArchivo(filePart.getSubmittedFileName(), convenio);
            } catch (Exception e) {
                if (e.getMessage().equals("El convenio del archivo no coincide con el seleccionado.")) {
                    return errores.setErroresArchivo(
                            "El convenio del archivo no coincide con el seleccionado.",
                            "Por favor revisá que el convenio dentro del archivo coincida con el seleccionado.", null, null);
                }

                if (e.getMessage().equals("El archivo no tiene la fecha de hoy.")) {
                    return errores.setErroresArchivo(
                            "El archivo no tiene la fecha de hoy.",
                            "Por favor modificá la fecha del archivo y subilo nuevamente.", null, null);
                }
                return errores.setErroresArchivo("El nombre del archivo es inválido.", e.getMessage(), null, null);
            }

            // Obtengo el contenido del archivo
            String contenidoArchivo = blobToString(archivo);
            ArchivoAcreditacionHeader nuevaCabecera = ArchivoAcreditacionHeader.readAcreditacionHeaderFromFile(contenidoArchivo.trim());
            Integer cantidadRegistros = nuevaCabecera.getCantidad();

            try {
                OBValidacionesAcreditaciones.validarHeader(nuevaCabecera, convenio);
            } catch (Exception e) {
                return errores.setErroresArchivo("El archivo tiene errores.", e.getMessage(), 0, null);
            }

            String importeEntero = nuevaCabecera.getSumatoriaTotalImportes().replaceFirst("^0+(?!$)", "");
            BigDecimal importeHeader = BigDecimal.valueOf(Double.valueOf(importeEntero)/100.00);

            List<ArchivoAcreditacionItem> listDetalleItem = null;
            BigDecimal importeSumado = BigDecimal.ZERO;
            if (filePart.getSubmittedFileName().startsWith("ent")) {

                listDetalleItem = ArchivoAcreditacionItem.readAcreditacionItemFromFileEnt(archivo);

                if (cantidadRegistros != listDetalleItem.size()) {
                    return errores.setErroresArchivo("El archivo tiene errores.", "La cantidad de registros en el archivo no coincide con la cantidad indicada en el header.", 0, null);
                }

                int row = 1;

                for (ArchivoAcreditacionItem item : listDetalleItem) {
                    item = OBValidacionesAcreditaciones.validarDetail(item, nuevaCabecera.getFechaGeneracionArchivo(), "ent");
                    importeSumado = importeSumado.add(item.getSueldo().divide(BigDecimal.valueOf(100)));
                    if (item.isCorrupted()) {
                        return errores.setErroresArchivo(
                                "El archivo tiene errores",
                                "El archivo que subiste tiene algunos errores a partir de la fila " + row
                                        + ". " + item.getErrores().get(0).getBundleMessage()
                                        + " Por favor revisalo y subilo nuevamente.",
                                row,
                                item.getErrores().get(0).getBundleMessage());
                    }
                    row++;
                }

            } else if (filePart.getSubmittedFileName().startsWith("enx")) {
                listDetalleItem = ArchivoAcreditacionItem.readAcreditacionItemFromFileEnx(archivo);

                if (cantidadRegistros != listDetalleItem.size()) {
                    return errores.setErroresArchivo("El archivo tiene errores", "La cantidad de registros en el archivo no coincide con la cantidad indicada en el header.", 0, null);
                }

                int row = 1;
                for (ArchivoAcreditacionItem item : listDetalleItem) {
                    item = OBValidacionesAcreditaciones.validarDetail(item, nuevaCabecera.getFechaGeneracionArchivo(), "enx");
                    importeSumado = importeSumado.add(item.getSueldo().divide(BigDecimal.valueOf(100)));
                    if (item.isCorrupted()) {
                        return errores.setErroresArchivo(
                                "El archivo tiene errores",
                                "El archivo que subiste tiene algunos errores a partir de la fila " + row
                                        + ". " + item.getErrores().get(0).getBundleMessage()
                                        + " Por favor revisalo y subilo nuevamente.",
                                row,
                                item.getErrores().get(0).getBundleMessage());
                    }
                    row++;
                }
            }

            if (importeHeader.compareTo(importeSumado)!=0){
                return errores.setErroresArchivo("El archivo tiene errores", "El importe del header no coincide con la suma de los registros", 0, null);
            }

            List<ArchivoAcreditacionItem> listaPaginada = getPage(listDetalleItem, numeroPagina, registrosPorPagina);

            for (ArchivoAcreditacionItem item : listaPaginada) {
                Objeto detail = new Objeto();

                detail.set("nombreApellido", tieneRestriccion ? "****" : item.getNombreCuenta());

                String cuentaCobis = " ";
                String cbu = " ";

                if ((StringUtil.eliminarCerosIzquierda(item.getNumeroCuenta()).length() == 15)) {
                    cuentaCobis = StringUtil.eliminarCerosIzquierda(item.getNumeroCuenta());
                } else {
                    cbu = item.getNumeroCuenta();
                }
                detail.set("cuentaCobis", tieneRestriccion ? "****" : cuentaCobis);
                detail.set("cbu", tieneRestriccion ? "****" : cbu);
                detail.set("cuitCuil", tieneRestriccion ? "****" : item.getCuil().toString());

                String fechaProceso = item.getFechaAcreditacion().substring(6, 8) + "/" + item.getFechaAcreditacion().substring(4, 6) + "/" + item.getFechaAcreditacion().substring(0, 4);
                detail.set("fechaDeProceso", fechaProceso);

                String sueldoEntero = item.getSueldo().toString();
//                String sueldo = sueldoEntero.substring(0, sueldoEntero.length() - 2);
//                String sueldoCentavos = sueldoEntero.substring(sueldoEntero.length() - 2);
                detail.set("sueldoAcreditar", tieneRestriccion ? "$ ****" : Double.valueOf(item.getSueldo().toString())/100);

                if (item.getMultiCausal().isBlank()) {
                    if (!cbu.isBlank()&&cbu.startsWith("044")){
                        detail.set("multicausal", "SUELDOS");
                    }else{
                        detail.set("multicausal", "");
                    }
                } else {
                    for (EnumMulticausalAcreditacionesOB e : EnumMulticausalAcreditacionesOB.values()) {
                        if (e.getCodigo().equals(item.getMultiCausal())) {
                            detail.set("multicausal", e.name().replace("_", " "));
                            break;
                        }
                    }
                }

                respuesta.add("registros", detail);
            }

            dato.set("nombreArchivo", filePart.getSubmittedFileName());
            dato.set("convenio", convenio);
            dato.set("tipoProducto", tipoProducto);
            dato.set("cuentaOrigen", cuentaOrigen);
            dato.set("importeTotal", importeHeader.toString());
            dato.set("numeroPagina", numeroPagina);
            dato.set("cantidadRegistros", cantidadRegistros);
            dato.set("paginasTotales", Math.ceil((double) cantidadRegistros / registrosPorPagina));
            dato.set("restringidoVerDatos", tieneRestriccion);
            respuesta.add("informacionArchivo", dato);

            //Armo listado de nombres de columnas visibles
            ServicioAcreditacionesConfigOB servicioAcreditacionesConfig = new ServicioAcreditacionesConfigOB(contexto);
            columnas.addAll(servicioAcreditacionesConfig.findAll().get().stream().parallel()
                    .filter(columna -> columna.visible.equals(true))
                    .sorted(Comparator.comparing(AcreditacionesConfigOB::getPosicion))
                    .map(columna -> columna.nombreColumna).toList());

            respuesta.add("columnas", columnas);

        } catch (Exception e) {
            return e.getMessage();
        }

        return respuesta("datos", respuesta);
    }


    private static <T> List<T> getPage(List<T> sourceList, int page, int pageSize) {
        if (pageSize <= 0 || page <= 0) {
            throw new IllegalArgumentException("invalid page size: " + pageSize);
        }

        int fromIndex = (page - 1) * pageSize;
        if (sourceList == null || sourceList.size() <= fromIndex) {
            return Collections.emptyList();
        }

        // toIndex exclusive
        return sourceList.subList(fromIndex, Math.min(fromIndex + pageSize, sourceList.size()));
    }


    public static Object cargarHaberes(ContextoOB contexto) {

        Integer productoCodigo = contexto.parametros.integer("tipoProducto");
        Integer convenio = contexto.parametros.integer("convenio");
        String cuenta = contexto.parametros.string("cuentaOrigen");

        ServicioPagoHaberesOB servicioPagoHaberes = new ServicioPagoHaberesOB(contexto);
        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioHistorialPagoDeHaberesOB servicioHistorial = new ServicioHistorialPagoDeHaberesOB(contexto);
        ServicioTipoProductoFirmaOB servicioTipoProducto = new ServicioTipoProductoFirmaOB(contexto);

        String connectionString = contexto.config.string("ob_azure_blob_st_url");
        String containerName = contexto.config.string("ob_azure_blob_st_container");
        //Se mueve el archivo en azure storage de carpeta en bandeja a carpeta a procesar
        AzureBlobStorageManager az = new AzureBlobStorageManager(contexto, connectionString, containerName);
        try {

            Part filePart = contexto.request.raw().getPart("archivo");
            InputStream inputStream = filePart.getInputStream();
            SesionOB sesion = contexto.sesion();

            // validar archivo

            String[] tipoExtension = filePart.getSubmittedFileName().split("\\.");

            if (!tipoExtension[1].equalsIgnoreCase("txt")) {
                return respuesta("EXTENSION_ARCHIVO_INVALIDA");
            }

            if (inputStream.available() > 0) {
                byte[] archivoBytes = inputStream.readAllBytes();
                StringBuilder nuevoArchivo = new StringBuilder();
                String text = new String(archivoBytes,Charset.forName("Cp1252"));
                String[] lineas = text.split("\n");
                for (String line : lineas) {
                    String numeroCuenta = filePart.getSubmittedFileName().startsWith("ent") ? line.substring(17, 39).trim() : line.substring(21,43);
                    boolean esCBU;
                    if ((StringUtil.eliminarCerosIzquierda(numeroCuenta).length() == 15)) {
                        esCBU = false;
                    } else {
                        esCBU = true;
                    }
                    if (esCBU&&numeroCuenta.startsWith("044")){
                        if (contexto.sesion().empresaOB.cuit!=30522211563l&&convenio!=9362){
                            String primerParte = line.substring(0,line.length()-4);
                            line = primerParte+"001";
                        }
                    }
                    nuevoArchivo.append(line).append("\n");

                }
                byte[] nuevoTextoBytes = nuevoArchivo.toString()
                        .replaceAll("\u00A0", " ")
                        .replaceAll("\r","\n")
                        .replaceAll("\n\n","\n")
                        .getBytes(Charset.forName("Cp1252"));

                // Obtengo el contenido del archivo.
                String contenidoArchivo = blobToString(new SerialBlob(archivoBytes));
                String cabecera = contenidoArchivo.substring(0, 38);
                String convenioCabecera = cabecera.substring(0, 5);

                LocalDate dateArchivo = LocalDate.now();


                // validar convenio que viene en el combo contra el del archivo
                if (convenio == null || convenio != Integer.parseInt(convenioCabecera)) {
                    return respuesta("CONVENIO_INVALIDO");
                }

                // Toma del archivo cantidad registros
                Integer cantidadRegistros = Integer.parseInt(cabecera.substring(18, 24));

                // Toma del archivo importe total
                String importeEntero = cabecera.substring(24, 38).replaceFirst("^0+(?!$)", "");
                BigDecimal importe = BigDecimal.valueOf(Double.valueOf(importeEntero)/100.00);

                Objeto cuentaDebito = OBCuentas.cuenta(contexto, cuenta);
                if (empty(cuentaDebito) || cuentaDebito == null) {
                    return respuesta("CUENTA_DEBITO_INVALIDA");
                }

                TipoProductoFirmaOB producto = servicioTipoProducto.findByCodigo(productoCodigo).get();

                String pathMaster = contexto.config.string("ph_ruta_master_files");

                PagoDeHaberesOB pago = servicioPagoHaberes.crear(contexto, cuenta, importe, filePart.getSubmittedFileName(),
                        null, cantidadRegistros, convenio, producto, dateArchivo, null).get();

                String nombreArchivo = convertirNombreHaberes(pago.nombreArchivo);

                try {
                    az.uploadArchivoToAzure(contexto, nuevoTextoBytes, pathMaster, nombreArchivo, String.valueOf(pago.id));
                } catch (Exception e) {
                    e.printStackTrace();
                    return respuesta("ERROR", "descripcion", "Error al cargar el archivo a carpeta master.");
                }
                String pathDestinoBandeja = contexto.config.string("ph_ruta_en_bandeja");
                try {
                    az.copyBlob(contexto, pathMaster + nombreArchivo, pathDestinoBandeja + nombreArchivo);
                } catch (Exception e) {
                    e.printStackTrace();
                    return respuesta("ERROR", "descripcion", "Error al copiar el archivo a carpeta en bandeja");
                }

                BandejaOB bandeja = servicioBandeja.find(pago.id).get();
                EstadoBandejaOB estadoInicialBandeja = servicioEstadoBandeja
                        .find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
                AccionesOB accionCrear = servicioAcciones.find(EnumAccionesOB.CREAR.getCodigo()).get();
                EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);

                servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionCrear, estadoInicialBandeja, estadoInicialBandeja);
                servicioHistorial.crear(pago, accionCrear, empresaUsuario);

                contexto.parametros.set("idSolicitudPago", pago.id);

            } else {
                // Manejar el caso donde el InputStream está vacío
                return respuesta("ERROR", "descripcion", "ARCHIVO_VACIO");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return respuesta("detalle", detalleSolicitudAcreditaciones(contexto));
    }

    public static Object cargarNomina(ContextoOB contexto) throws ServletException, IOException, SQLException {
        Integer productoCodigo = contexto.parametros.integer("tipoProducto");
        Integer convenio = contexto.parametros.integer("convenio");
        String cuenta = contexto.parametros.string("cuentaOrigen");
        Boolean fcl = contexto.parametros.bool("fcl");
        Part filePart = contexto.request.raw().getPart("archivo");

        ServicioPagoHaberesOB servicioPagoHaberes = new ServicioPagoHaberesOB(contexto);
        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioHistorialPagoDeHaberesOB servicioHistorial = new ServicioHistorialPagoDeHaberesOB(contexto);
        ServicioHistorialPagoDeHaberesOB servicioHistorialActualizar = new ServicioHistorialPagoDeHaberesOB(contexto);
        ServicioTipoProductoFirmaOB servicioTipoProducto = new ServicioTipoProductoFirmaOB(contexto);
        ServicioEstadoPagosHaberesOB servicioEstadoPagos = new ServicioEstadoPagosHaberesOB(contexto);
        Objeto pagoDetalle = new Objeto();

        InputStream inputStream = filePart.getInputStream();
        Blob archivo = inputStreamToBlobANSI(inputStream);
        SesionOB sesion = contexto.sesion();

        // validar archivo

        String[] tipoExtension = filePart.getSubmittedFileName().split("\\.");

        if (!tipoExtension[1].equals("txt")) {
            return respuesta("EXTENSION_ARCHIVO_INVALIDA");
        }

        Objeto cuentaDebito = OBCuentas.cuenta(contexto, cuenta);
        if (empty(cuentaDebito) || cuentaDebito == null) {
            return respuesta("CUENTA_DEBITO_INVALIDA");
        }

        TipoProductoFirmaOB producto = servicioTipoProducto.findByCodigo(productoCodigo).get();

        // Agregar cantidad de registros
        List<ArchivoNominaItem> listDetalleItem = null;
        Integer cantidadRegistros;
        try{
            listDetalleItem = ArchivoNominaItem.readNominaItemFromFile(archivo);
            cantidadRegistros = listDetalleItem.size();
        }catch (Exception e){
            return respuesta("ERROR",e);
        }

        PagoDeHaberesOB pago = servicioPagoHaberes.crear(contexto, cuenta, BigDecimal.ZERO, filePart.getSubmittedFileName(),
                archivo, cantidadRegistros, convenio, producto, LocalDate.now(), fcl).get();

        BandejaOB bandeja = servicioBandeja.find(pago.id).get();
        EstadoBandejaOB estadoInicialBandeja = servicioEstadoBandeja
                .find(EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()).get();
        AccionesOB accionCrear = servicioAcciones.find(EnumAccionesOB.CREAR.getCodigo()).get();
        AccionesOB accionFirmar = servicioAcciones.find(EnumAccionesOB.FIRMAR.getCodigo()).get();
        AccionesOB accionNoAplica = servicioAcciones.find(EnumAccionesOB.NO_APLICA.getCodigo()).get();
        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);

        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionCrear, estadoInicialBandeja, estadoInicialBandeja);
        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionFirmar, estadoInicialBandeja, estadoInicialBandeja);
        servicioHistorial.crear(pago, accionCrear, empresaUsuario);
        servicioHistorial.crear(pago, accionFirmar, empresaUsuario);

        contexto.parametros.set("idSolicitudPago", pago.id);

        PagoDeHaberesOB pagoCreado = servicioPagoHaberes.find(pago.id).get();
        if (pagoCreado == null || !pagoCreado.empresa.idCobis.equals(contexto.sesion().empresaOB.idCobis)) {
            return respuesta("DATOS_INVALIDOS");
        }

        Integer error = 0;
        int estadoNomina;
        String mensajeAlUsuario = "";
        String estado = "";
        AltaConsultaCapitaOB altaCapita;
        int linea = 0;

        for (ArchivoNominaItem registro : listDetalleItem) {
            linea++;

            //creo request
            AltaConsultaCapitaRequest request = armarRequestAltaNomina(contexto, registro);

            if (fcl) {
                request.setEsfcl("S");
            } else request.setEsfcl("N");

            request.setArchivo(pago.nombreArchivo);
            request.setConvenio(pago.convenio);
            request.setCuitEmpresa(String.valueOf(sesion.empresaOB.cuit));

            //llamar api
            altaCapita = ApiEmpresas.altaConsultaNomina(contexto, request).get();
            Objeto respuestaServicio = altaCapita.objeto();

            try {
                mensajeAlUsuario = respuestaServicio.get("mensajeAlUsuario") != null ? respuestaServicio.get("mensajeAlUsuario").toString() : null;
                estado = respuestaServicio.get("estado") != null ? respuestaServicio.get("estado").toString() : null;
            } catch (Exception e) {
                System.out.println(e);
            }


            if (mensajeAlUsuario != null || estado == null || altaCapita.cta == null || altaCapita.cta.equals(" ")) {
                String mensajeError = null;
                if (mensajeAlUsuario != null) {
                    mensajeError = mensajeAlUsuario;//"Operacion no ejecutada";
                } else if (estado != null) {
                    mensajeError = respuestaServicio.get("descripcion").toString();//"Operacion no ejecutada";
                }

                ErrorArchivoOB errorRegistro = new ErrorArchivoOB(mensajeError, mensajeError, linea, null);
                ServicioErroresArchivoOB servicioErroresArchivo = new ServicioErroresArchivoOB(contexto);
                servicioErroresArchivo.crear(errorRegistro, bandeja);
                error++;
            } else {

                NominaCuentasCreadasOB cuentaCreada = new NominaCuentasCreadasOB();
                ServicioNominaCuentasCreadasOB servicioNominaCuentasCreadas = new ServicioNominaCuentasCreadasOB(contexto);

                if (altaCapita.cta != null && !altaCapita.cta.isEmpty() && !altaCapita.cta.equals(" ")) {
                    cuentaCreada.nomina = pago;
                    cuentaCreada.linea = linea;
                    cuentaCreada.cuentaSueldo = altaCapita.cta;
                    cuentaCreada.cbuCuentaSueldo = altaCapita.cbuCajaAho;
                    if (pago.fcl) {
                        cuentaCreada.cuentaFCL = altaCapita.ctaFcl;
                        cuentaCreada.cbuCuentaFCL = altaCapita.cbuFcl;
                    } else {
                        cuentaCreada.cuentaFCL = "No requerido";
                        cuentaCreada.cbuCuentaFCL = "No requerido";
                    }
                }
                if (altaCapita.cta == null) {
                    cuentaCreada.nomina = pago;
                    cuentaCreada.linea = linea;
                    cuentaCreada.cuentaSueldo = altaCapita.cta;
                    cuentaCreada.cbuCuentaSueldo = altaCapita.cbuCajaAho;
                    if (pago.fcl) {
                        cuentaCreada.cuentaFCL = altaCapita.ctaFcl;
                        cuentaCreada.cbuCuentaFCL = altaCapita.cbuFcl;
                    } else {
                        cuentaCreada.cuentaFCL = "No requerido";
                        cuentaCreada.cbuCuentaFCL = "No requerido";
                    }
                }

                servicioNominaCuentasCreadas.crear(cuentaCreada);

                try {
                    Boolean mail = envioMailNomina(contexto);
                    if (!mail) {
                        LogOB.evento(contexto, "ENVIO_MAIL_NOMINA", new Objeto().set("Cuit", sesion.empresaOB.cuit).set("error", "Error en envío de mail"));
                    }
                } catch (Exception ex) {
                    LogOB.evento(contexto, "ENVIO_MAIL_NOMINA", new Objeto().set("Cuit", sesion.empresaOB.cuit).set("error", ex.getMessage()));
                }
            }
        }

        estadoNomina = (error == 0)
                ? EnumEstadoPagosHaberesOB.PROCESADO.getCodigo()
                : (error.equals(pago.cantidadRegistros))
                ? EnumEstadoPagosHaberesOB.RECHAZADO.getCodigo()
                : EnumEstadoPagosHaberesOB.PROCESADO_PARCIALMENTE.getCodigo();

        pago.estado = servicioEstadoPagos.find(estadoNomina).get();

        try {
            servicioPagoHaberes.update(pago);

            servicioHistorialActualizar.cambiaEstado
                    (
                            pago,
                            accionNoAplica,
                            empresaUsuario,
                            servicioEstadoPagos.find(EnumEstadoPagosHaberesOB.A_PROCESAR.getCodigo()).get(),
                            pago.estado
                    ).get();
        } catch (Exception ex) {
            servicioHistorialActualizar.cambiaEstado
                    (
                            pago,
                            accionNoAplica,
                            empresaUsuario,
                            servicioEstadoPagos.find(EnumEstadoPagosHaberesOB.A_PROCESAR.getCodigo()).get(),
                            servicioEstadoPagos.find(EnumEstadoPagosHaberesOB.RECHAZADO.getCodigo()).get()
                    ).get();
        }

        pagoDetalle.set("id", pago.id);
        pagoDetalle.set("monto", pago.monto);
        pagoDetalle.set("cantidadRegistros", pago.cantidadRegistros);
        pagoDetalle.set("cuenta", pago.cuentaOrigen);
        pagoDetalle.set("tipo", pago.tipoProducto);
        pagoDetalle.set("moneda", pago.moneda.simbolo);
        pagoDetalle.set("creadoPor", pago.usuario.nombre + " " + pago.usuario.apellido);

        if (pago.estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo())
            pagoDetalle.set("estado", pago.estado.descripcion);
        else
            pagoDetalle.set("estado", pago.estadoBandeja.descripcion);

        pagoDetalle.set("estadoOperacion", pago.estado.descripcion);

        return respuesta("detalle", pagoDetalle);
    }


    private static AltaConsultaCapitaRequest armarRequestAltaNomina(ContextoOB contexto, ArchivoNominaItem registroNomina) {
        AltaConsultaCapitaRequest request = new AltaConsultaCapitaRequest();
        request.setAlcance(0);
        request.setApellido(registroNomina.getApellido());
        request.setBancaAux("");
        request.setBase("");
        request.setCalle(registroNomina.getCalle());
        request.setCantidadFilas(0);
        request.setCaracteristica(registroNomina.getCaracteristica());
        request.setCiudadNac(registroNomina.getCiudadDeNacimiento());
        request.setCliente(0);
        request.setCodConv(0);
        request.setCodConvHasta(0);
        request.setCodigo("");
        request.setCodigoHasta("");
        request.setCodigoPostal(registroNomina.getCp());
        request.setCorrectos("");
        request.setCuenta("");
        request.setCuitEmpresa(contexto.sesion().empresaOB.cuit.toString());
        request.setDdi(registroNomina.getDDI());
        request.setDdn(registroNomina.getDDN());
        request.setDepto(registroNomina.getDpto());
        request.setDesc("");
        request.setDescConv("");
        request.setDirElec(registroNomina.getCorreoElectronico());
        request.setDocumento("");
        request.seteCivilDefault("S");
        request.setErrMigracion(0);
        request.setErrValExt(0);
        request.setErrValInt(0);
        request.setErrValidacion(0);
        request.setEstCivil(registroNomina.getEstadoCivil());
        request.setEstado("");
        request.setEstadoConv("");
        request.setFechaDesde(String.valueOf(LocalDateTime.now()));
        request.setFechaHasta(String.valueOf(LocalDateTime.now()));
        request.setFechaIngreso(registroNomina.getFechaIngreso());
        request.setFechaNac(registroNomina.getFechaNacimiento());
        request.setFormatoFecha(0);
        request.setFuncionarioHasta(0);
        request.setGrupoHasta(0);
        request.setIdPersonal(registroNomina.getNumeroDocumento());
        request.setIdTributaria(registroNomina.getNumeroCuil());
        request.setIvaAux("");
        request.setIvaExen((double) 0);
        request.setIvaReduc((double) 0);
        request.setLocalidad(registroNomina.getLocalidad());
        request.setLote(0);
        request.setLoteHasta(0);
        request.setModulo(0);
        request.setModuloHasta(0);
        request.setMonedaHasta(0);
        request.setNacionalidad(registroNomina.getNacionalidad());
        request.setNombre(registroNomina.getNombres());
        request.setNroTelefono(registroNomina.getNumeroTel());
        request.setNumero(registroNomina.getAltura());
        request.setObservaciones("");
        request.setOficialHasta(0);
        request.setOficina(registroNomina.getSucursal());
        request.setOpcion(3);
        request.setOperacion("I");
        request.setPais(registroNomina.getPaisNacimiento());
        request.setParametro("");
        request.setParametroHasta("");
        request.setPep(registroNomina.getEsPEP());
        request.setPiso(registroNomina.getPiso());
        request.setPrefijoTelefono(registroNomina.getPreFijoCel());
        request.setProfesion(registroNomina.getOcupacion().toString());
        request.setProvincia(registroNomina.getProvincia());
        request.setQuienLlama("");
        request.setrSocial(registroNomina.getApellido());
        request.setRegMigrados(0);
        request.setRegTotal(0);
        request.setResidenciaAux("");
        request.setSectorAux("");
        request.setSecuencial(registroNomina.getSecuencial());
        request.setSecuencialHasta(0);
        request.setSeleccionados("");
        request.setSet(3);
        request.setSetHasta(0);
        request.setSexo(registroNomina.getSexo());
        request.setSituacionLaboral(registroNomina.getSituacionLaboral().toString());
        request.setSolicitud(0);
        request.setSp("");
        request.setSpHasta("");
        request.setSueldo((int) registroNomina.getSueldo());
        request.setTidPersonal(registroNomina.getTipoDocumento());
        request.setTidTributaria(registroNomina.getTipoCuil());
        request.setTipoDirElec(registroNomina.getTipoCorreoElectronico());
        request.setTipoPersona(registroNomina.getTipoCuil());
        request.setTipoTarjeta("");
        request.setTipoTarjetaHasta("");
        request.setTipoTelefono(registroNomina.getTipoTel());
        request.setTipoTitularAux("");
        request.setTtelefono("");
        request.setType("");
        request.setValidacion("S");
        request.setValor("");

        return request;

    }

    public static Boolean envioMailNomina(ContextoOB contexto) {
        EnvioEmail result = ApiNotificaciones.envioAltaNominaOB(contexto, contexto.sesion().usuarioOB.email, contexto.sesion().usuarioOB.nombre, contexto.sesion().usuarioOB.apellido, contexto.sesion().empresaOB.razonSocial).get();
        return result.codigoHttp() == 200;
    }

    public static Object acreditacionDetalle(ContextoOB contexto) {
        Integer idOperacion = contexto.parametros.integer("idOperacion");

        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
        ServicioPagoHaberesOB servicioPagoHaberes = new ServicioPagoHaberesOB(contexto);
        ServicioEstadoPagosHaberesOB servicioEstadoPagoHaberes = new ServicioEstadoPagosHaberesOB(contexto);
        PagoDeHaberesOB pago = servicioPagoHaberes.find(idOperacion).get();
        Objeto dato = new Objeto();
        
        SesionOB sesionOB = contexto.sesion();
        
        boolean tieneRestriccion = false;

        ServicioNominaConfidencial servicioNominaConfidencial = new ServicioNominaConfidencial(contexto);
        NominaConfidencialOB resultado = servicioNominaConfidencial.findByUsuarioEmpresa(sesionOB.usuarioOB, sesionOB.empresaOB).tryGet();
        
        if (resultado != null && resultado.acreditacion) {
            tieneRestriccion = true;
        }
        
        if (empty(pago) || pago == null) {
            return respuesta("DATOS_INVALIDOS");
        }

        dato.set("archivo", pago.nombreArchivo);
        dato.set("cantidadRegistros", pago.cantidadRegistros);
        dato.set("creadaPor", pago.usuario.nombre + " " + pago.usuario.apellido);
        dato.set("fechaHoraCreacion", pago.fechaCreacion.toLocalDate().toString() + " "
                + pago.fechaCreacion.toLocalTime().withSecond(0).withNano(0).toString());

        Objeto estado = new Objeto();
        estado.set("id", pago.estadoBandeja.id);
        estado.set("descripcionCorta", pago.estadoBandeja.descripcion);
        dato.set("estado", estado);
        
        BandejaOB bandeja = servicioBandeja.find(pago.id).get();
        dato.set("firmas", OBFirmas.obtenerDatosFirmantes(contexto, bandeja));

        if (!pago.tipoProducto.equals("Nomina") && pago.estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo() && (pago.estado.id == EnumEstadoPagosHaberesOB.A_PROCESAR.getCodigo() || pago.estado.id == EnumEstadoPagosHaberesOB.PROCESADO_PARCIALMENTE.getCodigo())) {

            DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String fechadesde = String.valueOf(LocalDate.parse(pago.fechaCargaArchivo.toString(), formato));
            Fecha fechaHast = new Fecha(pago.fechaCargaArchivo.toString(), "yyyy-MM-dd");
            String fechaHasta;
            try {
                fechaHasta = ApiCatalogo.diaBancario(contexto, fechaHast).tryGet().diaHabilPosterior.sumarDias(2).toString();
            } catch (Exception e) {
                fechaHasta = pago.fechaCargaLote.plusDays(3).toString();
            }
            List<Objeto> lotes = getLotes(contexto, pago.convenio.toString(), fechadesde, fechaHasta);

            for (Objeto str : lotes) {
                boolean hayR = false;
                if (str.get("nombreArchivo").equals(pago.nombreArchivo)) {
                    pago.numeroLote = str.get("numeroLote").toString();
                    if (str.get("estado").equals("Sin Saldo")) {
                        EstadoPagosHaberesOB estadoPagoHaberes = servicioEstadoPagoHaberes.find(EnumEstadoPagosHaberesOB.ESPERANDO_FONDEO.getCodigo()).get();
                        pago.estado = estadoPagoHaberes;
                        servicioPagoHaberes.update(pago);
                        dato.set("estadoOperacion", pago.estado.descripcion);
                    } else if (str.get("estado").equals("Aceptado")||str.get("estado").equals("Procesado")) {
                        DetalleLotesOB detalle = ApiRecaudaciones.consultaDetalleLotes(contexto, pago.numeroLote, String.valueOf(pago.convenio)).get();
                        int i = 0;
                        boolean estaAceptado = false;

                        while (i < detalle.size()) {
                            if (!detalle.get(i).estado.equals("R")) {
                                estaAceptado = true;
                            }
                            if (detalle.get(i).estado.equals("R")){
                                hayR = true;
                            }
                            i++;
                        }
                        if (hayR){
                            pago.estado = estaAceptado ? servicioEstadoPagoHaberes.find(EnumEstadoPagosHaberesOB.PROCESADO_PARCIALMENTE.getCodigo()).get() : servicioEstadoPagoHaberes.find(EnumEstadoPagosHaberesOB.RECHAZADO.getCodigo()).get();
                        }

                    }
                    dato.set("numeroLote", str.get("numeroLote"));
                    dato.set("importe", str.get("importe"));
                    dato.set("observaciones", str.get("observaciones"));
                    dato.set("cantidad", str.get("cantidad"));

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate date = LocalDate.parse(str.get("fechaOrigen").toString(), formatter);
                    pago.fechaCargaLote = date.atStartOfDay();
                    servicioPagoHaberes.update(pago);

                    String estadoLote = str.get("estado").toString();
                    if (!estadoLote.equals(pago.estado.descripcion)) {
                        EstadoPagosHaberesOB estadoPagoHaberes;

//                        estadoPagoHaberes = servicioEstadoPagoHaberes.find(EnumEstadoPagosHaberesOB.EN_BANDEJA.getCodigo()).get();
//                        if (estadoLote.toUpperCase().equals(estadoPagoHaberes.descripcion.replaceAll("_", " "))) {
//                            pago.estado = estadoPagoHaberes;
//                        }

                        estadoPagoHaberes = servicioEstadoPagoHaberes.find(EnumEstadoPagosHaberesOB.A_PROCESAR.getCodigo()).get();
                        if (estadoLote.toUpperCase().equals(estadoPagoHaberes.descripcion.replaceAll("_", " ")) || estadoLote.equalsIgnoreCase("Ingreso Anticipado")) {
                            pago.estado = estadoPagoHaberes;
                        }

                        estadoPagoHaberes = servicioEstadoPagoHaberes.find(EnumEstadoPagosHaberesOB.PROCESADO.getCodigo()).get();
                        if (estadoLote.toUpperCase().equals(estadoPagoHaberes.descripcion)&&!hayR) {
                            pago.estado = estadoPagoHaberes;
                        }

                        estadoPagoHaberes = servicioEstadoPagoHaberes.find(EnumEstadoPagosHaberesOB.RECHAZADO.getCodigo()).get();
                        if (estadoLote.toUpperCase().equals(estadoPagoHaberes.descripcion)) {
                            pago.estado = estadoPagoHaberes;
                        }

//                        estadoPagoHaberes = servicioEstadoPagoHaberes.find(EnumEstadoPagosHaberesOB.PROCESADO_PARCIALMENTE.getCodigo()).get();
////                        if (estadoLote.toUpperCase().equals(estadoPagoHaberes.descripcion.replaceAll("_", " "))) {
////                            pago.estado = estadoPagoHaberes;
////                        }

                        estadoPagoHaberes = servicioEstadoPagoHaberes.find(EnumEstadoPagosHaberesOB.ESPERANDO_FONDEO.getCodigo()).get();
                        if (estadoLote.toUpperCase().equals(estadoPagoHaberes.descripcion.replaceAll("_", " ")) || estadoLote.equalsIgnoreCase("SIN SALDO")) {
                            pago.estado = estadoPagoHaberes;
                        }

                        servicioPagoHaberes.update(pago);
                    }

                }
            }
            dato.set("estadoOperacion", pago.estado.descripcion);
        } else
            dato.set("estadoOperacion", pago.estado.descripcion);

        Objeto cuenta = OBCuentas.cuenta(contexto, pago.cuentaOrigen);
        dato.set("cuentaOrigen", pago.cuentaOrigen);
        dato.set("banco", EnumBancosOB.BH.getNombre());
        dato.set("cbu", cuenta.get("cbu"));
        dato.set("restringidoVerDatos", tieneRestriccion);
        return respuesta("datos", dato);
    }

    public static Object precargaNomina(ContextoOB contexto) {

        Objeto dato = new Objeto();
        Objeto respuesta = new Objeto();
        List<String> columnas = new ArrayList<>();
        ErrorArchivoOB errores = new ErrorArchivoOB();

        try {

            Part filePart = contexto.request.raw().getPart("archivo");
            Integer convenio = contexto.parametros.integer("convenio");
            Integer tipoProducto = contexto.parametros.integer("tipoProducto");
            String cuentaOrigen = contexto.parametros.string("cuentaOrigen");
            Integer numeroPagina = contexto.parametros.integer("numeroPagina", 1);
            int registrosPorPagina = 60;
            InputStream inputStream = filePart.getInputStream();
            Blob archivo = inputStreamToBlobUTF8(inputStream);

            String[] tipoExtensionTXT = filePart.getSubmittedFileName().split("\\.");

            if (!(tipoExtensionTXT[1].equals("txt") || (tipoExtensionTXT[1].equals("csv")))) {
                return errores.setErroresArchivo(
                        "El archivo tiene un formato inválido",
                        "Sólo aceptamos archivos con la extensión .txt.\r\n"
                                + "Por favor generá el archivo correcto desde el template y subilo nuevamente.", null, null);

            }

            //validar si el archivo ya fue cargado
            try {
                OBValidacionesAcreditaciones.validarArchivoNomina(contexto, filePart.getSubmittedFileName());
            } catch (Exception e) {
                return errores.setErroresArchivo("El archivo tiene errores", e.getMessage(), null, null);

            }

            //validar nombre de archivo
            try {
                OBValidacionesAcreditaciones.validarNombreArchivoNomina(filePart.getSubmittedFileName());
            } catch (Exception e) {
                if (e.getMessage().equals("El archivo no tiene la fecha de hoy")) {
                    return errores.setErroresArchivo(
                            "El archivo no tiene la fecha de hoy",
                            "Por favor modificá la fecha del archivo y subilo nuevamente.", null, null);

                }

                return errores.setErroresArchivo(
                        "El nombre del archivo tiene un formato inválido",
                        "Por favor cambiá el nombre del archivo teniendo en cuenta el siguiente formato: AltaMasiva_AAMMDD_HHMMSS y subilo nuevamente.\r\n"
                                + "Ejemplo de nombre: “AltaMasiva_240315_131545”"
                        , null, null);

            }

            // Obtengo el contenido del archivo
            List<ArchivoNominaItem> listDetalleItem = null;
            Integer cantidadRegistros;
            try{
                 listDetalleItem = ArchivoNominaItem.readNominaItemFromFile(archivo);
                 cantidadRegistros = listDetalleItem.size();
            }catch (Exception e){
                return errores.setErroresArchivo("El archivo tiene errores",e.getMessage(),null,null);
            }


            //Valido cantidad de registros

            if (listDetalleItem.size() < 1) {
                return errores.setErroresArchivo("El archivo tiene errores", "El archivo no tiene registros para procesar", null, null);

            }

            for (ArchivoNominaItem item : listDetalleItem) {
                item = OBValidacionesNomina.validarDetail(contexto, item);
                if (item.isCorrupted()) {
                    //item.getErrores();
                    return errores.setErroresArchivo(
                            "El archivo tiene errores",
                            "El archivo que subiste tiene algunos errores a partir de la fila " + item.getNumLinea().toString()
                                    + ". " + item.getErrores().get(0).getBundleMessage().toUpperCase()
                                    //( item.getErrores().get(0).getParametros()[0] != null ? (" - Info: " + item.getErrores().get(0).getParametros()[0]) : "")
                                    //+ "."
                                    + " Por favor revisalo y subilo nuevamente.",
                            item.getNumLinea(),
                            item.getErrores().get(0).getBundleMessage());

                }
            }

            List<ArchivoNominaItem> listaPaginada = getPage(listDetalleItem, numeroPagina, registrosPorPagina);
            Paises getPais = ApiCatalogo.paises(contexto).tryGet();
            Sucursales getSucursal = ApiCatalogo.sucursales(contexto).tryGet();
            Provincias getProvincias = ApiCatalogo.provincias(contexto).tryGet();
            String tipoTel;
            String tipoCorreoElectronico;


            for (ArchivoNominaItem item : listaPaginada) {
                Objeto detail = new Objeto();

                detail.set("apellido", item.getApellido());
                detail.set("nombres", item.getNombres());
                detail.set("numeroDocumento", item.getNumeroDocumento());
                detail.set("sexo", item.getSexo());
                detail.set("fechaNacimiento", item.getFechaNacimiento());
                detail.set("paisNacimiento", getPais.buscarPaisById(String.valueOf(item.getPaisNacimiento())).descripcion);
                detail.set("nacionalidad", getPais.buscarPaisById(String.valueOf(item.getPaisNacimiento())).nacionalidad);
                detail.set("ciudadDeNacimiento", item.getCiudadDeNacimiento());
                detail.set("sucursal", getSucursal.stream().filter(s -> s.CodSucursal.equals(item.getSucursal())).findFirst().get().DesSucursal);
                detail.set("tipoCuil", EnumTipoCuitCuilNominaOB.obtenerNamePorId(item.getTipoCuil()));
                detail.set("numeroCuil", item.getNumeroCuil());
                detail.set("estadoCivil", EnumEstadoCivilNominaOB.obtenerNamePorId(item.getEstadoCivil()));
                detail.set("esPEP", item.getEsPEP());
                detail.set("ocupacion", EnumOcupacionNominaOB.obtenerNamePorId(item.getOcupacion()).replaceAll("_", " "));
                detail.set("situacionLaboral", EnumSituacionLaboralNominaOB.obtenerNamePorId(item.getSituacionLaboral()));
                detail.set("fechaIngreso", item.getFechaIngreso());
                detail.set("sueldo", item.getSueldo());
                detail.set("calle", item.getCalle());
                detail.set("altura", item.getAltura());
                detail.set("piso", item.getPiso());
                detail.set("dpto", item.getDpto());
                detail.set("cp", item.getCp());
                detail.set("localidad", item.getLocalidad());
                detail.set("provincia", getProvincias.buscarProvinciaById(String.valueOf(item.getProvincia())).descripcion);
                if (item.getTipoTel().equals("P")) {
                    tipoTel = "Particular";
                } else if (item.getTipoTel().equals("E")) {
                    tipoTel = "Celular";
                } else {
                    tipoTel = "";
                }
                detail.set("tipoTel", tipoTel);
                detail.set("DDN", item.getDDN());
                detail.set("numeroTel", item.getCaracteristica() + item.getNumeroTel()); //Se concatena para retornar el tel completo
                if (item.getTipoCorreoElectronico().equals("EML")) {
                    tipoCorreoElectronico = "direccion_e_mail Laboral";
                } else if (item.getTipoCorreoElectronico().equals("EMP")) {
                    tipoCorreoElectronico = "direccion_e_mail Particular";
                } else {
                    tipoCorreoElectronico = "";
                }
                detail.set("tipoCorreoElectronico", tipoCorreoElectronico);
                detail.set("correoElectronico", item.getCorreoElectronico());
                detail.set("observaciones", "Sin observaciones");
                detail.set("cuentaSueldo", "Aún no se creó la cuenta sueldo");
                detail.set("CBU", "Aún no se creó la cuenta sueldo");
                detail.set("cuentaFCL", "Aún no se creó la cuenta FCL");
                detail.set("cbuFCL", "Aún no se creó la cuenta FCL");

                respuesta.add("registros", detail);
            }

            dato.set("nombreArchivo", filePart.getSubmittedFileName());
            dato.set("convenio", convenio);
            dato.set("tipoProducto", tipoProducto);
            dato.set("cuentaOrigen", cuentaOrigen);
            dato.set("numeroPagina", numeroPagina);
            dato.set("cantidadRegistros", cantidadRegistros);
            dato.set("paginasTotales", Math.ceil((double) cantidadRegistros / registrosPorPagina));

            respuesta.add("informacionArchivo", dato);

            //Armo listado de nombres de columnas visibles
            ServicioNominaConfigOB servicioNominaConfig = new ServicioNominaConfigOB(contexto);
            columnas.addAll(servicioNominaConfig.findAll().get().stream().parallel()
                    .filter(columna -> columna.visible.equals(true))
                    .sorted(Comparator.comparing(NominaConfigOB::getPosicion))
                    .map(columna -> columna.nombreColumna).toList());

            respuesta.add("columnas", columnas);

        } catch (Exception e) {
            return e.getMessage();
        }

        return respuesta("datos", respuesta);
    }

    public static Object verTablaAcreditaciones(ContextoOB contexto) throws SQLException, IOException {
        Integer idOperacion = contexto.parametros.integer("idOperacion");
        Integer numeroPagina = contexto.parametros.integer("numeroPagina");

        ServicioPagoHaberesOB servicioPagoHaberes = new ServicioPagoHaberesOB(contexto);
        PagoDeHaberesOB pago = servicioPagoHaberes.find(idOperacion).get();
        Objeto dato = new Objeto();
        SesionOB sesionOB = contexto.sesion();
        boolean tieneRestriccion = false;

        ServicioNominaConfidencial servicioNominaConfidencial = new ServicioNominaConfidencial(contexto);
        NominaConfidencialOB resultado = servicioNominaConfidencial.findByUsuarioEmpresa(sesionOB.usuarioOB, sesionOB.empresaOB).tryGet();
        if (resultado != null && resultado.acreditacion) {
            tieneRestriccion = true;
        }

        if (empty(pago) || pago == null) {
            return respuesta("DATOS_INVALIDOS");
        }

        List<String> columnas = new ArrayList<>();
        String connectionString = contexto.config.string("ob_azure_blob_st_url");
        String containerName = contexto.config.string("ob_azure_blob_st_container");

        AzureBlobStorageManager az = new AzureBlobStorageManager(contexto, connectionString, containerName);
        Blob archivo;
        String nombreArchivo = null;
        LocalDate fechaCambioRenombramientoArchivos = LocalDate.of(2024,8,contexto.esProduccion()?26:25);
        try{
            nombreArchivo = pago.fechaCargaArchivo.isAfter(fechaCambioRenombramientoArchivos)?convertirNombreHaberes(pago.nombreArchivo):pago.nombreArchivo;
        }catch (Exception e){}

        try {
            BlobClient blobClient = az.findBlobByName(contexto, EnumTipoProductoOB.PLAN_SUELDO, nombreArchivo);
            archivo = new SerialBlob(blobClient.downloadContent().toBytes());
        } catch (Exception e) {
            return respuesta("ERROR", "descripcion", "No se encontró el archivo con ese id de bandeja.");
        }

        Integer registrosPorPagina = 60;
        Objeto respuesta = new Objeto();

        // Obtengo el contenido del archivo
        String contenidoArchivo = blobToString(archivo);
        ArchivoAcreditacionHeader nuevaCabecera = ArchivoAcreditacionHeader.readAcreditacionHeaderFromFile(contenidoArchivo.trim());

        List<ArchivoAcreditacionItem> listDetalleItem = null;
        if (pago.nombreArchivo.startsWith("ent")) {
            listDetalleItem = ArchivoAcreditacionItem.readAcreditacionItemFromFileEnt(archivo);
        } else if (pago.nombreArchivo.startsWith("enx")) {
            listDetalleItem = ArchivoAcreditacionItem.readAcreditacionItemFromFileEnx(archivo);
        }

        Integer cantidadRegistros = nuevaCabecera.getCantidad();


        List<ArchivoAcreditacionItem> listaPaginada = getPage(listDetalleItem, numeroPagina, registrosPorPagina);
        int cantItems = listaPaginada.size();
        boolean hayObservacion = false;
        int baseSecuencial = (numeroPagina - 1) * registrosPorPagina + (numeroPagina - 1);
        String secuencialAnterior = "null";
        DetalleLotesOB detalle=null;
        int cantLlamadasAPI = 0;
        int indiceConsultar = 0;
        Random random = new Random(Integer.valueOf(contexto.sesion().empresaOB.idCobis));
        Integer idProceso = random.nextInt();

        for (ArchivoAcreditacionItem item : listaPaginada) {
            int i = listaPaginada.indexOf(item);
            Objeto detail = new Objeto();
            int secuencial = baseSecuencial + (i / 20) * 21;
            if (numeroPagina == 1 && i == 0) {
                secuencial = 0;
            }
            String stringSecuencial = String.valueOf(secuencial);
            if (secuencial !=0){
                stringSecuencial = stringSecuencial.substring(0, stringSecuencial.length() - 1) + "1";
            }
            detail.set("nombreApellido", tieneRestriccion ? "****" : item.getNombreCuenta());
            String cuentaCobis = " ";
            String cbu = " ";

            if ((StringUtil.eliminarCerosIzquierda(item.getNumeroCuenta()).length() == 15)) {
                cuentaCobis = StringUtil.eliminarCerosIzquierda(item.getNumeroCuenta());
            } else {
                cbu = item.getNumeroCuenta();
            }
            detail.set("cuentaCobis", tieneRestriccion ? "****" : cuentaCobis);
            detail.set("cbu", tieneRestriccion ? "****" : cbu);
            detail.set("cuitCuil", tieneRestriccion ? "****" : item.getCuil().toString());

            String fechaProceso = item.getFechaAcreditacion().substring(6, 8) + "/" + item.getFechaAcreditacion().substring(4, 6) + "/" + item.getFechaAcreditacion().substring(0, 4);
            detail.set("fechaDeProceso", fechaProceso);

            String sueldoEntero = item.getSueldo().toString();
            String sueldo = sueldoEntero.substring(0, sueldoEntero.length() - 2);
            String sueldoCentavos = sueldoEntero.substring(sueldoEntero.length() - 2);
            detail.set("sueldoAcreditar", tieneRestriccion ? "$ ****" : Double.valueOf(item.getSueldo().toString())/100);

            if (item.getMultiCausal().isBlank()) {
                detail.set("multicausal", "Haberes");
            } else {
                for (EnumMulticausalAcreditacionesOB e : EnumMulticausalAcreditacionesOB.values()) {
                    if (e.getCodigo().equals(item.getMultiCausal())) {
                        detail.set("multicausal", e.name().replace("_", " "));
                        break;
                    }
                }
            }
            if (!secuencialAnterior.equals(stringSecuencial)){
                try {
                     detalle = ApiRecaudaciones.consultaDetalleLotes(contexto, pago.numeroLote, String.valueOf(pago.convenio),stringSecuencial,idProceso).get();
                    secuencialAnterior = stringSecuencial;
                    cantLlamadasAPI++;
                } catch (Exception e){
                    LogOB.evento(contexto, "Obtener detalle de lote en ver tabla acreditaciones", String.valueOf(e.toString()), CLASE);
                }
            }


            if (pago.estado.id.equals(EnumEstadoPagosHaberesOB.RECHAZADO.getCodigo())) {
                detail.set("estado", "RECHAZADA");
            } else {
                detail.set("estado", "pendiente");
            }
            if (pago.estado.id.equals(EnumEstadoPagosHaberesOB.PROCESADO.getCodigo()) || pago.estado.id.equals(EnumEstadoPagosHaberesOB.PROCESADO_PARCIALMENTE.getCodigo())) {
                try {
                    DetalleLotesOB.DetallePlanSueldo det = null;
                    String estado = null;
                    if (cantLlamadasAPI>1){
                        indiceConsultar = i-((cantLlamadasAPI-1)*20);
                    }else{
                        indiceConsultar = i;
                    }
                    if (detalle != null) {
                        //if (detalle.size() == cantItems) {
                            det = detalle.get(indiceConsultar);
                            if (det.estado.equalsIgnoreCase("W")){
                                estado = "RECHAZADA";
                            }else{
                                estado = EnumEstadosRegistrosAcreditacionesOB.obtenerPorCodigo(det.estado);
                            }

//                        } else {
//                            det = detalle.get(0);
//                            if (det.estado.equalsIgnoreCase("W")){
//                                estado = "RECHAZADA";
//                            }else{
//                                estado = EnumEstadosRegistrosAcreditacionesOB.obtenerPorCodigo(det.estado);
//                            }
//                        }
                    }

                    detail.set("estado", estado);
                } catch (Exception e) {
                    LogOB.evento(contexto, "Obtener detalle de lote en ver tabla acreditaciones", String.valueOf(e.toString()), CLASE);
                    return respuesta("ERROR", "descripcion", "No se encontró el detalle del lote. ");
                }
            } else if (pago.estado.id.equals(EnumEstadoPagosHaberesOB.ESPERANDO_FONDEO.getCodigo())){
                detail.set("estado","ESPERANDO FONDEO");
            }
            if (detalle!=null){
                if (cantLlamadasAPI>1){
                    indiceConsultar = i-((cantLlamadasAPI-1)*20);
                }else{
                    indiceConsultar = i;
                }
                    if (detalle.get(indiceConsultar).descripcion!=null){
                        hayObservacion=true;
                        if (!detalle.get(indiceConsultar).descripcion.isBlank()){
                            int mostrar = (i+1+((numeroPagina-1)*60));
                            detail.set("Observaciones",detalle.stream().filter(deta->deta.secuencial.equals(String.valueOf(mostrar))).findFirst().get().descripcion);
                        }
                    }

            }

            respuesta.add("registros", detail);
        }

        dato.set("nombreArchivo", pago.nombreArchivo);
        dato.set("numeroPagina", numeroPagina);
        dato.set("cantidadRegistros", cantidadRegistros);
        dato.set("paginasTotales", Math.ceil((double) cantidadRegistros / registrosPorPagina));
        dato.set("restringidoVerDatos", tieneRestriccion);

        respuesta.add("informacionArchivo", dato);

        //Armo listado de nombres de columnas visibles
        ServicioAcreditacionesConfigOB servicioAcreditacionesConfig = new ServicioAcreditacionesConfigOB(contexto);
        columnas.addAll(servicioAcreditacionesConfig.findAll().get().stream().parallel()
                .filter(columna -> columna.visible.equals(true))
                .sorted(Comparator.comparing(AcreditacionesConfigOB::getPosicion))
                .map(columna -> columna.nombreColumna).toList());
        if (hayObservacion){
            columnas.add("observaciones");
        }
        respuesta.add("columnas", columnas);

        return respuesta("datos", respuesta);

    }

    public static Object verTablaNomina(ContextoOB contexto) throws SQLException, IOException {
        Integer idOperacion = contexto.parametros.integer("idOperacion");
        Integer numeroPagina = contexto.parametros.integer("numeroPagina");

        ServicioPagoHaberesOB servicioPagoHaberes = new ServicioPagoHaberesOB(contexto);
        PagoDeHaberesOB pago = servicioPagoHaberes.find(idOperacion).get();
        Objeto dato = new Objeto();
        if (empty(pago) || pago == null) {
            return respuesta("DATOS_INVALIDOS");
        }

        ServicioErroresArchivoOB servicioErroresArchivo = new ServicioErroresArchivoOB(contexto);
        ServicioNominaCuentasCreadasOB servicioNominaCuentasCreadas = new ServicioNominaCuentasCreadasOB(contexto);
        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
        BandejaOB bandeja = servicioBandeja.find(pago.id).get();
        List<ErroresArchivosOB> errores = servicioErroresArchivo.buscarPorIdOperacion(bandeja).get();
        List<NominaCuentasCreadasOB> cuentasCreadas = servicioNominaCuentasCreadas.buscarPorIdOperacion(pago).get();

        List<String> columnas = new ArrayList<>();
        Blob archivo = pago.archivo;
        Integer registrosPorPagina = 60;
        Objeto respuesta = new Objeto();
        List<ArchivoNominaItem> listDetalleItem = null;
        try {
            listDetalleItem = ArchivoNominaItem.readNominaItemFromFile(archivo);
        }catch (Exception e){
            return respuesta("ERROR",e);
        }


        Integer cantidadRegistros = listDetalleItem.size();

        List<ArchivoNominaItem> listaPaginada = getPage(listDetalleItem, numeroPagina, registrosPorPagina);
        Integer linea = 0;
        Paises getPais = ApiCatalogo.paises(contexto).tryGet();
        Sucursales getSucursal = ApiCatalogo.sucursales(contexto).tryGet();
        Provincias getProvincias = ApiCatalogo.provincias(contexto).tryGet();
        String tipoCuil;
        String tipoTel;
        String tipoCorreoElectronico;

        for (ArchivoNominaItem item : listaPaginada) {
            linea = linea + 1;
            Objeto detail = new Objeto();

            detail.set("apellido", item.getApellido());
            detail.set("nombres", item.getNombres());
            detail.set("numeroDocumento", item.getNumeroDocumento());
            detail.set("sexo", item.getSexo());
            detail.set("fechaNacimiento", item.getFechaNacimiento());
            detail.set("paisNacimiento", getPais.buscarPaisById(String.valueOf(item.getPaisNacimiento())).descripcion);
            detail.set("nacionalidad", getPais.buscarPaisById(String.valueOf(item.getPaisNacimiento())).nacionalidad);
            detail.set("ciudadDeNacimiento", item.getCiudadDeNacimiento());
            detail.set("sucursal", getSucursal.stream().filter(s -> s.CodSucursal.equals(item.getSucursal())).findFirst().get().DesSucursal);
            detail.set("tipoCuil", EnumTipoCuitCuilNominaOB.obtenerNamePorId(item.getTipoCuil()));
            detail.set("numeroCuil", item.getNumeroCuil());
            detail.set("estadoCivil", EnumEstadoCivilNominaOB.obtenerNamePorId(item.getEstadoCivil()));
            detail.set("esPEP", item.getEsPEP());
            detail.set("ocupacion", EnumOcupacionNominaOB.obtenerNamePorId(item.getOcupacion()).replaceAll("_", " "));
            detail.set("situacionLaboral", EnumSituacionLaboralNominaOB.obtenerNamePorId(item.getSituacionLaboral()));
            detail.set("fechaIngreso", item.getFechaIngreso());
            detail.set("sueldo", item.getSueldo());
            detail.set("calle", item.getCalle());
            detail.set("altura", item.getAltura());
            detail.set("piso", item.getPiso());
            detail.set("dpto", item.getDpto());
            detail.set("cp", item.getCp());
            detail.set("localidad", item.getLocalidad());
            detail.set("provincia", getProvincias.buscarProvinciaById(String.valueOf(item.getProvincia())).descripcion);
            if (item.getTipoTel().equals("P")) {
                tipoTel = "Particular";
            } else if (item.getTipoTel().equals("E")) {
                tipoTel = "Celular";
            } else {
                tipoTel = "";
            }
            detail.set("tipoTel", tipoTel);
            detail.set("DDN", item.getDDN());
            detail.set("numeroTel", item.getCaracteristica() + item.getNumeroTel()); //Se concatena para retornar el tel completo
            if (item.getTipoCorreoElectronico().equals("EML")) {
                tipoCorreoElectronico = "direccion_e_mail Laboral";
            } else if (item.getTipoCorreoElectronico().equals("EMP")) {
                tipoCorreoElectronico = "direccion_e_mail Particular";
            } else {
                tipoCorreoElectronico = "";
            }
            detail.set("tipoCorreoElectronico", tipoCorreoElectronico);
            detail.set("correoElectronico", item.getCorreoElectronico());

            Integer finalLinea = linea + (60 * (numeroPagina - 1));
            if (pago.estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo() && (pago.estado.id == EnumEstadoPagosHaberesOB.RECHAZADO.getCodigo() || pago.estado.id == EnumEstadoPagosHaberesOB.PROCESADO_PARCIALMENTE.getCodigo())) {

                Optional<ErroresArchivosOB> error = errores.stream().filter(e -> e.linea.equals(finalLinea)).findFirst();
                if (error == null || error.isEmpty()) {
                    detail.set("observaciones", "Sin observaciones");
                } else {
                    detail.set("observaciones", error.get().titulo);
                    detail.set("cuentaSueldo", "Operación no ejecutada");
                    detail.set("CBU", "Operación no ejecutada");
                    if (pago.fcl) {
                        detail.set("cuentaFCL", "Operación no ejecutada");
                        detail.set("cbuFCL", "Operación no ejecutada");
                    } else {
                        detail.set("cuentaFCL", "No requerido");
                        detail.set("cbuFCL", "No requerido");
                    }
                }
            }
            if (pago.estado.id == EnumEstadoPagosHaberesOB.PROCESADO.getCodigo() || pago.estado.id == EnumEstadoPagosHaberesOB.PROCESADO_PARCIALMENTE.getCodigo()) {
                Optional<NominaCuentasCreadasOB> cuentaCreada = cuentasCreadas.stream().filter(c -> c.linea.equals(finalLinea)).findFirst();
                if (cuentaCreada == null || cuentaCreada.isEmpty()) {
                    detail.set("cuentaSueldo", "Operación no ejecutada");
                    detail.set("CBU", "Operación no ejecutada");
                    if (pago.fcl) {
                        detail.set("cuentaFCL", "Operación no ejecutada");
                        detail.set("cbuFCL", "Operación no ejecutada");
                    } else {
                        detail.set("cuentaFCL", "No requerido");
                        detail.set("cbuFCL", "No requerido");
                    }
                } else {
                    detail.set("observaciones", "Sin observaciones");
                    detail.set("cuentaSueldo", cuentaCreada.get().cuentaSueldo);
                    detail.set("CBU", cuentaCreada.get().cbuCuentaSueldo);
                    if (pago.fcl) {
                        detail.set("cuentaFCL", cuentaCreada.get().cuentaFCL);
                        detail.set("cbuFCL", cuentaCreada.get().cbuCuentaFCL);
                    } else {
                        detail.set("cuentaFCL", "No requerido");
                        detail.set("cbuFCL", "No requerido");
                    }
                }
            }

            respuesta.add("registros", detail);
        }

        dato.set("nombreArchivo", pago.nombreArchivo);
        dato.set("numeroPagina", numeroPagina);
        dato.set("cantidadRegistros", cantidadRegistros);
        dato.set("paginasTotales", Math.ceil((double) cantidadRegistros / registrosPorPagina));

        respuesta.add("informacionArchivo", dato);

        //Armo listado de nombres de columnas visibles
        ServicioNominaConfigOB servicioNominaConfig = new ServicioNominaConfigOB(contexto);
        columnas.addAll(servicioNominaConfig.findAll().get().stream().parallel()
                .filter(columna -> columna.visible.equals(true))
                .sorted(Comparator.comparing(NominaConfigOB::getPosicion))
                .map(columna -> columna.nombreColumna).toList());

        respuesta.add("columnas", columnas);

        return respuesta("datos", respuesta);

    }


    public static Object consultaEstados(ContextoOB contexto) {
        ServicioEstadoPagosHaberesOB servicioEstadoPagosHaberes = new ServicioEstadoPagosHaberesOB(contexto);
        List<EstadoPagosHaberesOB> estadosHaberes = servicioEstadoPagosHaberes.findAll().get();

        Objeto datos = new Objeto();
        for (EstadoPagosHaberesOB estado : estadosHaberes) {
            if (estado.id != EnumEstadoPagosHaberesOB.EN_BANDEJA.getCodigo()) {
                Objeto est = new Objeto();
                est.set("id", estado.id);
                est.set("descripcion", estado.descripcion);
                datos.add(est);
            } else {
                Objeto pendiente = new Objeto();
                pendiente.set("id", 7);
                pendiente.set("descripcion", "PENDIENTE FIRMA");
                datos.add(pendiente);

                Objeto parcial = new Objeto();
                parcial.set("id", 8);
                parcial.set("descripcion", "PARCIALMENTE FIRMADA");
                datos.add(parcial);
            }
        }

        return respuesta("datos", datos);
    }


    public static Object consultaEstadosNomina(ContextoOB contexto) {
        ServicioEstadoPagosHaberesOB servicioEstadoPagosHaberes = new ServicioEstadoPagosHaberesOB(contexto);
        List<EstadoPagosHaberesOB> estadosHaberes = servicioEstadoPagosHaberes.findAll().get();

        Objeto datos = new Objeto();
        for (EstadoPagosHaberesOB estado : estadosHaberes) {
            if (estado.id != EnumEstadoPagosHaberesOB.EN_BANDEJA.getCodigo()) {
                Objeto est = new Objeto();
                est.set("id", estado.id);
                est.set("descripcion", estado.descripcion);
                datos.add(est);
            }
        }

        return respuesta("datos", datos);
    }

    public static Object obenerOperadores(ContextoOB contexto) {
        SesionOB sesion = contexto.sesion();

        if (!sesion.esOperadorInicial()) {
            return respuesta("OPERACION_INVALIDA");
        }

        if (!sesion.esOperadorInicial()) {
            return respuesta("OPERACION_INVALIDA");
        }

        ServicioEmpresaUsuarioOB servicio = new ServicioEmpresaUsuarioOB(contexto);
        List<EmpresaUsuarioOB> empresaUsuarioOB = servicio.findByEmpresa(sesion.empresaOB).tryGet();
        ServicioNominaConfidencial servicioNominaConfidencial = new ServicioNominaConfidencial(contexto);
        ServicioUsuarioOB usuarioOB = new ServicioUsuarioOB(contexto);
        EmpresaOB empresa = sesion.empresaOB;
        boolean empresaConRestriccion = false;
        Objeto datos = new Objeto();
        for (EmpresaUsuarioOB empresaUsuario : empresaUsuarioOB) {
            if (empresaUsuario.rol.rol_codigo == 2) {
                Objeto op = new Objeto();
                op.set("nombre", empresaUsuario.usuario.nombre);
                op.set("apellido", empresaUsuario.usuario.apellido);
                op.set("numeroDocumento", empresaUsuario.usuario.numeroDocumento);
                op.set("rol", empresaUsuario.rol.nombre);
                op.set("usuCodigo", empresaUsuario.usuario.codigo);
                op.set("nominaConfidencial", null);
                UsuarioOB usuario = usuarioOB.find(empresaUsuario.usuario.codigo).get();
                if (servicioNominaConfidencial.findByUsuarioEmpresa(usuario, empresa).tryGet() == null) {
                    op.set("nominaConfidencial", false);
                    empresaConRestriccion = false;
                } else {
                    if (servicioNominaConfidencial.findByUsuarioEmpresa(usuario, empresa).tryGet().acreditacion != false) {
                        op.set("nominaConfidencial", true);
                        empresaConRestriccion = true;
                    } else {
                        op.set("nominaConfidencial", false);
                    }
                }
                //listaOperadores.add(op);
                datos.add("operadores", op);
            }
        }

        datos.set("empresaConRestriccion", empresaConRestriccion);

        return respuesta("datos", datos);
    }

    public static Object guardarNominaConfidencial(ContextoOB contexto) {
        SesionOB sesion = contexto.sesion();
        Objeto usu_codigo = contexto.parametros.objeto("usu_codigo", null);
        List<Object> usuariosConPermisos = usu_codigo.toList();
        Objeto datos = new Objeto();
        NominaConfidencialOB nominaConf;

        if (!sesion.esOperadorInicial()) {
            return respuesta("OPERACION_INVALIDA");
        }

        ServicioEmpresaUsuarioOB servicio = new ServicioEmpresaUsuarioOB(contexto);
        List<EmpresaUsuarioOB> empresaUsuarioOB = servicio.findByEmpresa(sesion.empresaOB).tryGet();
        ServicioNominaConfidencial servicioNominaConfidencial = new ServicioNominaConfidencial(contexto);
        EmpresaOB empresa = sesion.empresaOB;


        for (EmpresaUsuarioOB usuario : empresaUsuarioOB) {
            if (usuario.rol.rol_codigo == 2) {

                if (usuariosConPermisos.stream().anyMatch(e -> Long.valueOf(e.toString()).equals(usuario.usuario.codigo.longValue()))) {
                    if (servicioNominaConfidencial.findByUsuarioEmpresa(usuario.usuario, empresa).tryGet() != null) {
                        nominaConf = servicioNominaConfidencial.findByUsuarioEmpresa(usuario.usuario, empresa).tryGet();
                        nominaConf.acreditacion = false;
                        nominaConf.fechaCreacion = LocalDateTime.now();
                        servicioNominaConfidencial.update(nominaConf);
                    } else {
                        NominaConfidencialOB nuevaNomina = new NominaConfidencialOB();
                        nuevaNomina.emp_codigo = usuario.empresa;
                        nuevaNomina.usuario = usuario.usuario;
                        nuevaNomina.acreditacion = false;
                        nuevaNomina.fechaCreacion = LocalDateTime.now();
                        servicioNominaConfidencial.crearUsuario(nuevaNomina);
                    }
                } else {
                    if (servicioNominaConfidencial.findByUsuarioEmpresa(usuario.usuario, empresa).tryGet() != null) {
                        nominaConf = servicioNominaConfidencial.findByUsuarioEmpresa(usuario.usuario, empresa).tryGet();
                        nominaConf.acreditacion = true;
                        nominaConf.fechaCreacion = LocalDateTime.now();
                        servicioNominaConfidencial.update(nominaConf);
                    } else {
                        NominaConfidencialOB nuevaNomina = new NominaConfidencialOB();
                        nuevaNomina.emp_codigo = usuario.empresa;
                        nuevaNomina.usuario = usuario.usuario;
                        nuevaNomina.acreditacion = true;
                        nuevaNomina.fechaCreacion = LocalDateTime.now();
                        servicioNominaConfidencial.crearUsuario(nuevaNomina);
                    }
                }
            }
        }
        String mensaje = "operacion realizada con exito";
        datos.set("mensaje", mensaje);

        return respuesta("datos", datos);
    }

    public static Object descargaTemplate(ContextoOB contexto) {
        Integer producto = contexto.parametros.integer("producto");
        byte[] file;
        file = OBManejoArchivos.descargarTemplate(contexto, producto);

        if (file == null) {
            return respuesta("ERROR", "descripcion", "No se pudo descargar el template");
        }
        return file;

    }

    public static Object descargarNomina(ContextoOB contexto) {
        Integer idOperacion = contexto.parametros.integer("idOperacion");

        ArrayList<NominaCSVDTO> list_ordenes = new ArrayList<>();
        
        ServicioPagoHaberesOB servicioPagoHaberes = new ServicioPagoHaberesOB(contexto);
        PagoDeHaberesOB pago = servicioPagoHaberes.find(idOperacion).get();
        Objeto dato = new Objeto();
        if (empty(pago) || pago == null) {
            LogOB.evento(contexto,"descargarNomina", "Se cargan datos por IdOperacion");
            return respuesta("ERROR","Datos Invalidos");
        }

        ServicioErroresArchivoOB servicioErroresArchivo = new ServicioErroresArchivoOB(contexto);
        ServicioNominaCuentasCreadasOB servicioNominaCuentasCreadas = new ServicioNominaCuentasCreadasOB(contexto);
        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
        BandejaOB bandeja = servicioBandeja.find(pago.id).get();
        List<ErroresArchivosOB> errores = servicioErroresArchivo.buscarPorIdOperacion(bandeja).get();
        List<NominaCuentasCreadasOB> cuentasCreadas = servicioNominaCuentasCreadas.buscarPorIdOperacion(pago).get();

        Blob archivo = pago.archivo;
        Objeto respuesta = new Objeto();

        List<ArchivoNominaItem> listDetalleItem = null;
        try {
            listDetalleItem = ArchivoNominaItem.readNominaItemFromFile(archivo);
        }catch (Exception e){
            return respuesta("ERROR",e);
        }


        Integer linea = 0;
        Paises getPais = ApiCatalogo.paises(contexto).tryGet();
        Sucursales getSucursal = ApiCatalogo.sucursales(contexto).tryGet();
        Provincias getProvincias = ApiCatalogo.provincias(contexto).tryGet();
        String tipoCuil;
        String tipoTel;
        String tipoCorreoElectronico;

        for (ArchivoNominaItem item : listDetalleItem) {
            linea = linea + 1;
            Objeto detail = new Objeto();

            detail.set("apellido", item.getApellido());
            detail.set("nombres", item.getNombres());
            detail.set("numeroDocumento", item.getNumeroDocumento());
            detail.set("sexo", item.getSexo());
            detail.set("fechaNacimiento", item.getFechaNacimiento());
            detail.set("paisNacimiento", getPais.buscarPaisById(String.valueOf(item.getPaisNacimiento())).descripcion);
            detail.set("nacionalidad", getPais.buscarPaisById(String.valueOf(item.getPaisNacimiento())).nacionalidad);
            detail.set("ciudadDeNacimiento", item.getCiudadDeNacimiento());
            detail.set("sucursal", getSucursal.stream().filter(s -> s.CodSucursal.equals(item.getSucursal())).findFirst().get().DesSucursal);
            detail.set("tipoCuil", EnumTipoCuitCuilNominaOB.obtenerNamePorId(item.getTipoCuil()));
            detail.set("numeroCuil", item.getNumeroCuil());
            detail.set("estadoCivil", EnumEstadoCivilNominaOB.obtenerNamePorId(item.getEstadoCivil()));
            detail.set("esPEP", item.getEsPEP());
            detail.set("ocupacion", EnumOcupacionNominaOB.obtenerNamePorId(item.getOcupacion()).replaceAll("_", " "));
            detail.set("situacionLaboral", EnumSituacionLaboralNominaOB.obtenerNamePorId(item.getSituacionLaboral()));
            detail.set("fechaIngreso", item.getFechaIngreso());
            detail.set("sueldo", item.getSueldo());
            detail.set("calle", item.getCalle());
            detail.set("altura", item.getAltura());
            detail.set("piso", item.getPiso());
            detail.set("dpto", item.getDpto());
            detail.set("cp", item.getCp());
            detail.set("localidad", item.getLocalidad());
            detail.set("provincia", getProvincias.buscarProvinciaById(String.valueOf(item.getProvincia())).descripcion);
            if (item.getTipoTel().equals("P")) {
                tipoTel = "Particular";
            } else if (item.getTipoTel().equals("E")) {
                tipoTel = "Celular";
            } else {
                tipoTel = "";
            }
            detail.set("tipoTel", tipoTel);
            detail.set("DDN", item.getDDN());
            detail.set("numeroTel", item.getCaracteristica() + item.getNumeroTel()); //Se concatena para retornar el tel completo
            if (item.getTipoCorreoElectronico().equals("EML")) {
                tipoCorreoElectronico = "direccion_e_mail Laboral";
            } else if (item.getTipoCorreoElectronico().equals("EMP")) {
                tipoCorreoElectronico = "direccion_e_mail Particular";
            } else {
                tipoCorreoElectronico = "";
            }
            detail.set("tipoCorreoElectronico", tipoCorreoElectronico);
            detail.set("correoElectronico", item.getCorreoElectronico());
            Integer finalLinea = linea;

            //Integer finalLinea = linea + (60 * (numeroPagina - 1));
            if (pago.estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo() && (pago.estado.id == EnumEstadoPagosHaberesOB.RECHAZADO.getCodigo() || pago.estado.id == EnumEstadoPagosHaberesOB.PROCESADO_PARCIALMENTE.getCodigo())) {

                Optional<ErroresArchivosOB> error = errores.stream().filter(e -> e.linea.equals(finalLinea)).findFirst();
                if (error == null || error.isEmpty()) {
                    detail.set("observaciones", "Sin observaciones");
                } else {
                    detail.set("observaciones", error.get().titulo);
                    detail.set("motivoDeRechazo", error.get().descripcion);
                    detail.set("cuentaSueldo", "Operación no ejecutada");
                    detail.set("CBU", "Operación no ejecutada");
                    if (pago.fcl) {
                        detail.set("cuentaFCL", "Operación no ejecutada");
                        detail.set("cbuFCL", "Operación no ejecutada");
                    } else {
                        detail.set("cuentaFCL", "No requerido");
                        detail.set("cbuFCL", "No requerido");
                    }
                }
            }
            if (pago.estado.id == EnumEstadoPagosHaberesOB.PROCESADO.getCodigo() || pago.estado.id == EnumEstadoPagosHaberesOB.PROCESADO_PARCIALMENTE.getCodigo()) {
                Optional<NominaCuentasCreadasOB> cuentaCreada = cuentasCreadas.stream().filter(c -> c.linea.equals(finalLinea)).findFirst();
                if (cuentaCreada == null || cuentaCreada.isEmpty()) {
                    detail.set("cuentaSueldo", " ");
                    detail.set("CBU", " ");
                    if (pago.fcl) {
                        detail.set("cuentaFCL", " ");
                        detail.set("cbuFCL", " ");
                    } else {
                        detail.set("cuentaFCL", " ");
                        detail.set("cbuFCL", " ");
                    }
                } else {
                    detail.set("observaciones", "Sin observaciones");
                    detail.set("cuentaSueldo", cuentaCreada.get().cuentaSueldo);
                    detail.set("CBU", cuentaCreada.get().cbuCuentaSueldo);
                    if (pago.fcl) {
                        detail.set("cuentaFCL", "'"+cuentaCreada.get().cuentaFCL);
                        detail.set("cbuFCL", "'"+cuentaCreada.get().cbuCuentaFCL);
                    } else {
                        detail.set("cuentaFCL", " ");
                        detail.set("cbuFCL", " ");
                    }
                }
            }

            String cuilT ="'"+item.getNumeroCuil();
            String nombreCompleto = item.getNombres() + " " + item.getApellido();
            String cuenta = "'"+detail.string("cuentaSueldo");
            String cbu = "'"+detail.string("CBU");
            String ctaFcl = detail.string("cuentaFCL");
            String cbuFcl = detail.string("cbuFCL");
            String estado = detail.string("observaciones");
            String motivoDeRechazo = detail.string("motivoDeRechazo");

            list_ordenes.add(new NominaCSVDTO(cuilT, nombreCompleto, cuenta, cbu, ctaFcl, cbuFcl, estado, motivoDeRechazo));

            respuesta.add("registros", detail);
        }
            if (!list_ordenes.isEmpty()) {
                try {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    OutputStreamWriter streamWriter = new OutputStreamWriter(stream, Charset.forName("UTF-8"));
                    CSVWriter writer = new CSVWriter(streamWriter, ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

                    var mappingStrategy = new OBManejoArchivos.CustomColumnPositionStrategy<NominaCSVDTO>();
                    mappingStrategy.setType(NominaCSVDTO.class);

                    StatefulBeanToCsv<NominaCSVDTO> builder = new StatefulBeanToCsvBuilder<NominaCSVDTO>(writer)
                            .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                            .withSeparator(';')
                            .withMappingStrategy(mappingStrategy)
                            .build();

                    builder.write(list_ordenes);
                    streamWriter.flush();
                    writer.close();

                    byte[] file = stream.toByteArray();
                    contexto.response.header("Content-Disposition", "attachment; filename=nomina" + Fecha.hoy() + ".csv");
                    contexto.response.type("application/csv");
                    return file;

                } catch (Exception e) {
                    return respuesta("ERROR", "descripcion", "No se pudo descargar el archivo.");
                }
            } else return respuesta("ERROR", "descripcion", "No hay datos disponibles para exportar.");
        }


    public static Object descargarAcreditaciones(ContextoOB contexto) throws SQLException, IOException {
        Integer idOperacion = contexto.parametros.integer("idOperacion");

        ArrayList<AcreditacionesCSVDTO> list_acreditaciones = new ArrayList<>();

        ServicioPagoHaberesOB servicioPagoHaberes = new ServicioPagoHaberesOB(contexto);
        PagoDeHaberesOB pago = servicioPagoHaberes.find(idOperacion).get();
        Objeto dato = new Objeto();
        SesionOB sesionOB = contexto.sesion();
        boolean tieneRestriccion = false;

        ServicioNominaConfidencial servicioNominaConfidencial = new ServicioNominaConfidencial(contexto);
        NominaConfidencialOB resultado = servicioNominaConfidencial.findByUsuarioEmpresa(sesionOB.usuarioOB, sesionOB.empresaOB).tryGet();
        if (resultado != null && resultado.acreditacion) {
            tieneRestriccion = true;
        }

        if (empty(pago) || pago == null) {
            LogOB.evento(contexto,"descargarAcreditaciones", "Se cargan datos por IdOperacion");
            return respuesta("ERROR","Datos Invalidos");
        }

        String connectionString = contexto.config.string("ob_azure_blob_st_url");
        String containerName = contexto.config.string("ob_azure_blob_st_container");

        AzureBlobStorageManager az = new AzureBlobStorageManager(contexto, connectionString, containerName);
        Blob archivo;
        String nombreArchivo = null;
        LocalDate fechaCambioRenombramientoArchivos = LocalDate.of(2024,8,contexto.esProduccion()?26:25);
        try{
            nombreArchivo = pago.fechaCargaArchivo.isAfter(fechaCambioRenombramientoArchivos)?convertirNombreHaberes(pago.nombreArchivo):pago.nombreArchivo;
        }catch (Exception e){}
        try {
            BlobClient blobClient = az.findBlobByName(contexto, EnumTipoProductoOB.PLAN_SUELDO, nombreArchivo);
            archivo = new SerialBlob(blobClient.downloadContent().toBytes());
            LogOB.evento(contexto,"descargarAcreditaciones", "Se cargan datos por Azure");

        } catch (Exception e) {
            return respuesta("ERROR", "descripcion", "No se encontró el archivo con ese id de bandeja.");
        }

        Objeto respuesta = new Objeto();

        // Obtengo el contenido del archivo
        String contenidoArchivo = blobToString(archivo);

        List<ArchivoAcreditacionItem> listDetalleItem = null;
        if (pago.nombreArchivo.startsWith("ent")) {
            listDetalleItem = ArchivoAcreditacionItem.readAcreditacionItemFromFileEnt(archivo);
        } else if (pago.nombreArchivo.startsWith("enx")) {
            listDetalleItem = ArchivoAcreditacionItem.readAcreditacionItemFromFileEnx(archivo);
        }


        int cantItems = listDetalleItem.size();
        Random random = new Random(Integer.valueOf(contexto.sesion().empresaOB.idCobis));
        Integer idProceso = Math.abs(random.nextInt());
        boolean hayObservacion = false;
        int cantLlamadasAPI = 0;
        DetalleLotesOB detalle=null;
        int indiceConsultar = 0;
        int secuencialAnterior = -1;
        int secuencial = 0;
        for (ArchivoAcreditacionItem item : listDetalleItem) {
            int i = listDetalleItem.indexOf(item);
           if (i < 20) {
                secuencial = 0;
            } else {
                secuencial = ((i / 20) * 20) + 1;
            }
            Objeto detail = new Objeto();

            detail.set("nombreApellido", tieneRestriccion ? "****" : item.getNombreCuenta());
            String cuentaCobis = " ";
            String cbu = " ";

            if ((StringUtil.eliminarCerosIzquierda(item.getNumeroCuenta()).length() == 15)) {
                cuentaCobis = StringUtil.eliminarCerosIzquierda(item.getNumeroCuenta());
            } else {
                cbu = item.getNumeroCuenta();
            }
            detail.set("cuentaCobis", tieneRestriccion ? "****" : cuentaCobis);
            detail.set("cbu", tieneRestriccion ? "****" : cbu);
            detail.set("cuitCuil", tieneRestriccion ? "****" : item.getCuil().toString());

            String fechaProceso = item.getFechaAcreditacion().substring(6, 8) + "/" + item.getFechaAcreditacion().substring(4, 6) + "/" + item.getFechaAcreditacion().substring(0, 4);
            detail.set("fechaDeProceso", fechaProceso);

            String sueldoEntero = item.getSueldo().toString();
            String sueldo = sueldoEntero.substring(0, sueldoEntero.length() - 2);
            String sueldoCentavos = sueldoEntero.substring(sueldoEntero.length() - 2);
            detail.set("sueldoAcreditar", tieneRestriccion ? "$ ****" : sueldo + "," + sueldoCentavos);

            if (item.getMultiCausal().isBlank()) {
                detail.set("multicausal", "Haberes");
            } else {
                for (EnumMulticausalAcreditacionesOB e : EnumMulticausalAcreditacionesOB.values()) {
                    if (e.getCodigo().equals(item.getMultiCausal())) {
                        detail.set("multicausal", e.name().replace("_", " "));
                        break;
                    }
                }
            }
            if (secuencial!=secuencialAnterior){
                try {
                    detalle = ApiRecaudaciones.consultaDetalleLotes(contexto, pago.numeroLote, String.valueOf(pago.convenio),String.valueOf(secuencial),idProceso).get();
                    secuencialAnterior = secuencial;
                    cantLlamadasAPI++;
                } catch (Exception e){
                    LogOB.evento(contexto, "Obtener detalle de lote en ver tabla acreditaciones", String.valueOf(e.toString()), CLASE);
                }
            }


            if (pago.estado.id.equals(EnumEstadoPagosHaberesOB.RECHAZADO.getCodigo())) {
                detail.set("estado", "RECHAZADA");
            } else {
                detail.set("estado", "pendiente");
            }
            if (pago.estado.id.equals(EnumEstadoPagosHaberesOB.PROCESADO.getCodigo()) || pago.estado.id.equals(EnumEstadoPagosHaberesOB.PROCESADO_PARCIALMENTE.getCodigo())) {
                try {
                    DetalleLotesOB.DetallePlanSueldo det = null;
                    String estado = null;
                    if (cantLlamadasAPI>1){
                        indiceConsultar = i-((cantLlamadasAPI-1)*20);
                    }else{
                        indiceConsultar = i;
                    }
                    if (detalle != null) {
                        //if (detalle.size() == cantItems) {
                            det = detalle.get(indiceConsultar);
                            if (det.estado.equalsIgnoreCase("W")){
                                estado = "RECHAZADA";
                            }else{
                                estado = EnumEstadosRegistrosAcreditacionesOB.obtenerPorCodigo(det.estado);
                            }

//                        } else {
//                            det = detalle.get(0);
//                            if (det.estado.equalsIgnoreCase("W")){
//                                estado = "RECHAZADA";
//                            }else{
//                                estado = EnumEstadosRegistrosAcreditacionesOB.obtenerPorCodigo(det.estado);
//                            }
//                        }
                    }

                    detail.set("estado", estado);
                } catch (Exception e) {
                    LogOB.evento(contexto, "Obtener detalle de lote en ver tabla acreditaciones", String.valueOf(e.toString()), CLASE);
                    return respuesta("ERROR", "descripcion", "No se encontró el detalle del lote. ");
                }
            } else if (pago.estado.id.equals(EnumEstadoPagosHaberesOB.ESPERANDO_FONDEO.getCodigo())){
                detail.set("estado","ESPERANDO FONDEO");
            }
            if (detalle!=null){
                if (detalle.get(indiceConsultar).descripcion!=null){
                    hayObservacion=true;
                    if (!detalle.get(indiceConsultar).descripcion.isBlank()){
                        detail.set("Observaciones",detalle.get(indiceConsultar).descripcion);

                    }
                }
            }

            String numeroCuenta = detail.string("cuentaCobis");
            if (numeroCuenta.isBlank()){
                numeroCuenta = detail.string("cbu");
            }
            Long cuil = Long.parseLong(detail.string("cuitCuil"));
            String nombreCompleto = detail.string("nombreApellido");
            String estado = detail.string("estado");
            BigDecimal importe = new BigDecimal(detail.string("sueldoAcreditar").replace("$", "").replace(",", "."));
            String descripcion = detail.string("Observaciones");
            String causal = detail.string("multicausal");

            // Crear y añadir el DTO a la lista
            AcreditacionesCSVDTO dto = new AcreditacionesCSVDTO(numeroCuenta, cuil, nombreCompleto, estado, importe, descripcion, causal);
            list_acreditaciones.add(dto);

            respuesta.add("registros", detail);
        }

        if (!list_acreditaciones.isEmpty()) {
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                OutputStreamWriter streamWriter = new OutputStreamWriter(stream, Charset.forName("UTF-8"));
                CSVWriter writer = new CSVWriter(streamWriter, ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

                var mappingStrategy = new OBManejoArchivos.CustomColumnPositionStrategy<AcreditacionesCSVDTO>();
                mappingStrategy.setType(AcreditacionesCSVDTO.class);

                StatefulBeanToCsv<AcreditacionesCSVDTO> builder = new StatefulBeanToCsvBuilder<AcreditacionesCSVDTO>(writer)
                        .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                        .withSeparator(';')
                        .withMappingStrategy(mappingStrategy)
                        .build();

                builder.write(list_acreditaciones);
                streamWriter.flush();
                writer.close();

                String csvOutput = new String(stream.toByteArray(), Charset.forName("UTF-8"));

                byte[] file = stream.toByteArray();
                contexto.response.header("Content-Disposition", "attachment; filename=acreditaciones" + Fecha.hoy() + ".csv");
                contexto.response.type("application/csv");
                return file;

            } catch (Exception e) {
                return respuesta("ERROR", "descripcion", "No se pudo descargar el archivo.");
            }
        } else  return respuesta("ERROR", "descripcion", "No hay datos disponibles para exportar.");
    }
    
    public static Object historiaFCL(ContextoOB contexto) {
        Integer convenio = contexto.parametros.integer("convenio", null);
        Boolean previsualizacion = contexto.parametros.bool("previsualizacion", null);
        Objeto respuesta = new Objeto();
        SesionOB sesionOB = contexto.sesion();
        List<FondoCeseLaboralOB> movimientos = new ArrayList<FondoCeseLaboralOB>();
        
        ConveniosOB listConveniosOB = ApiRecaudaciones.convenios(contexto, String.valueOf(sesionOB.empresaOB.cuit), OPERACION).get();
        List<Convenio> convenios = listConveniosOB.list();;
        
        ServicioMovimientosFCL servicioMovimientosFCL = new ServicioMovimientosFCL(contexto);
        
        if(convenio==null) {
        	for (Convenio c : convenios) {
        		movimientos.addAll(servicioMovimientosFCL.buscarPorConvenio(c.codigoConvenio).get());
        	}
        } else {
        	movimientos = servicioMovimientosFCL.buscarPorConvenio(convenio).get();
        }
        for (FondoCeseLaboralOB m : movimientos) {
            Objeto datos = new Objeto();
            Objeto estado = new Objeto();
            datos.set("secuencia", m.cobFclSecuencia);
            datos.set("convenio", m.cobFclCoCodConv);
            datos.set("titular", m.cobFclAhNombre);
            datos.set("cuil", m.cobFclEnCedRuc);
            datos.set("cuentaFCL", m.cobFclAhCtaBanco);
            datos.set("disponible", m.cobFclAhDisponible);
            estado.set("id", null);
            estado.set("descripcionCorta", null);
            datos.set("estado", estado);

            respuesta.add(datos);
            if (previsualizacion) {
                if (respuesta.toList().size() == 5) {
                    return respuesta("datos", respuesta);
                }
            }
        }
        return respuesta("datos", respuesta);
    }

    public static Object detalleMovimeintoFCL(ContextoOB contexto) {
        Long secuencial = contexto.parametros.longer("secuencial", null);

        ServicioMovimientosFCL servicioMovimientosFCL = new ServicioMovimientosFCL(contexto);
        
        FondoCeseLaboralOB fcl = servicioMovimientosFCL.buscarPorSecuencia(secuencial).get();
        
            Objeto datos = new Objeto();
            datos.set("secuencia", fcl.cobFclSecuencia);
            datos.set("convenio", fcl.cobFclCoCodConv);
            datos.set("titular", fcl.cobFclAhNombre);
            datos.set("dni", fcl.cobFclIdNumero);
            datos.set("cuil", fcl.cobFclEnCedRuc);
            datos.set("categoria", fcl.cobFclAhCategoria);
            datos.set("cuentaFCL", fcl.cobFclAhCtaBanco);
            datos.set("disponible", fcl.cobFclAhDisponible);
            datos.set("apertura", fcl.cobFclAhFechaAper.toString());
            datos.set("uModficacion", fcl.cobFclAhFechaUltMov.toString());

        return respuesta("datos", datos);
    }

}
