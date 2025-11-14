package ar.com.hipotecario.canal.homebanking.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.servicio.api.transmit.JourneyTransmitEnum;
import ar.com.hipotecario.backend.util.Transmit;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Fecha;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.Constantes;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.negocio.Prestamo;
import ar.com.hipotecario.canal.homebanking.negocio.SituacionLaboral;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaDebito;
import ar.com.hipotecario.canal.homebanking.servicio.*;
import ar.com.hipotecario.canal.homebanking.ventas.Integrante;
import ar.com.hipotecario.canal.homebanking.ventas.ResolucionMotor;
import ar.com.hipotecario.canal.homebanking.ventas.Solicitud;
import ar.com.hipotecario.canal.homebanking.ventas.Solicitud.SolicitudProducto;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.bm.hb.AltaPrestamoHBBMBankProcess;
import ar.com.hipotecario.mobile.RespuestaMB;
import org.apache.commons.lang3.StringUtils;

public class HBOriginacion {

    public static Respuesta solicitarPrimerOfertaPrestamo(ContextoHB contexto) {
        Boolean forzarGeneracionOfertaNueva = contexto.parametros.bool("forzarGeneracionOfertaNueva");
        Boolean esAdelanto = contexto.parametros.bool("esAdelanto", false);
        Boolean esAptoRecurrencia = !tieneAdelantoRecurrente(contexto) && ConfigHB.bool("prendido_adelanto_recurrente", false);
        String idSolicitudConPrestamo = "";
        String idSolicitudVacia = "";
        String idSolicitud = "";
        String idPrestamo = "";
        Boolean tieneSeguro = false;
        Boolean primerOferta = false; // es para saber si es la primera vez que se le oferta el préstamo o estoy
        // tomando otro prestamo

        if (!ConfigHB.bool("prendido_alta_prestamos")) {
            return Respuesta.estado("OPERACION_INHABILITADA");
        }

        Boolean esCuotificacion = contexto.parametros.bigDecimal("montoCuotificacion") != null;
        contexto.sesion.cache.remove("primeraOferta");

        if (!contexto.parametros.string("idSolicitud").equals("")) {
            idSolicitudConPrestamo = contexto.parametros.string("idSolicitud");
            idPrestamo = contexto.parametros.string("idPrestamo");
        }

        // String idSeguroDesempleo = "";
        // Primero me fijo si ya hay una solicitud hecha de préstamos o alguna que esté
        // vacía
        ApiResponse response = RestVenta.consultarSolicitudes(contexto);
        if (response.hayError()) {
            return Respuesta.estado("ERROR_CONSULTAR_SOLICITUDES");
        }
        if (!forzarGeneracionOfertaNueva && "".equals(idSolicitudConPrestamo)) {
            for (Objeto datoSolicitud : response.objetos("Datos")) {
                tieneSeguro = false;
                if ("O".equals(datoSolicitud.string("Estado"))) {
                    boolean aplicaPrestamo = true;
                    for (Objeto productoSolicitud : datoSolicitud.objetos("Productos")) {
                        if ("2".equals(productoSolicitud.string("tipoProducto"))) {
                            idSolicitudConPrestamo = datoSolicitud.string("Id");
                            idPrestamo = productoSolicitud.string("Id");
                        } else {
                            if (!"8".equals(productoSolicitud.string("tipoProducto")) && !"11".equals(productoSolicitud.string("tipoProducto")) && !"31".equals(productoSolicitud.string("tipoProducto"))) { // 8-CA; 11-TD; 31-SEGURO DESEMPLEO
                                aplicaPrestamo = false;
                            }
                            if ("31".equals(productoSolicitud.string("tipoProducto")))
                                tieneSeguro = true;
                        }
                    }
                    if (aplicaPrestamo && !"".equals(idSolicitudConPrestamo))
                        break; // ya encontré una solicitud que tiene préstamo.
                    else
                        idSolicitudConPrestamo = ""; // tiene prestamo pero tiene más cosas que no me sirven
                    if (datoSolicitud.objetos("Productos").isEmpty()) {
                        idSolicitudVacia = datoSolicitud.string("Id");
                    }
                }
            }
        }

        if (!"".equals(idSolicitudConPrestamo)) {
            idSolicitud = idSolicitudConPrestamo;
        } else {
            if (!"".equals(idSolicitudVacia)) {
                // como está vacía la desisto, ya que tengo que volverla a armar
                String finalIdSolicitudVacia = idSolicitudVacia;
                new Futuro<>(() -> RestVenta.desistirSolicitud(contexto, finalIdSolicitudVacia));
            }
        }

        // SOLICITUD- SOLO SI NO HIZO LA SOLICITUD
        // String idPrestamo = "";
        String finalIdSolicitud = idSolicitud;
        if ("".equals(idSolicitud)) {
            if (ConfigHB.esOpenShift()) {
                new Futuro<>(() -> RestOmnicanalidad.limpiarSolicitudes(contexto, ConfigHB.longer("solicitud_dias_vigente", 30L), true, false, false));
            }

            ApiResponse solicitud = RestVenta.generarSolicitud(contexto);
            if (solicitud.hayError() || !solicitud.objetos("Errores").isEmpty()) {
                return new Respuesta().setEstado("ERROR").set("error", solicitud.objetos("Errores").get(0).string("MensajeCliente"));
            }
            idSolicitud = solicitud.objetos("Datos").get(0).string("IdSolicitud");

            // INTEGRANTE
            ApiResponse integrante = RestVenta.generarIntegrante(contexto, idSolicitud);
            if (integrante.hayError() || !integrante.objetos("Errores").isEmpty()) {
                return new Respuesta().setEstado("ERROR").set("error", integrante.objetos("Errores").get(0).string("MensajeCliente"));
            }

            // PRESTAMO
            /*
             * Calendar hoy = Calendar.getInstance(); hoy.add(Calendar.DATE, +30); Integer
             * dia = new Integer(hoy.get(Calendar.DAY_OF_MONTH));
             */
            ApiResponse prestamo = RestVenta.agregarPrestamoPersonal(contexto, idSolicitud, esAdelanto);
            if (prestamo.hayError() || !prestamo.objetos("Errores").isEmpty()) {
                return new Respuesta().setEstado("ERROR").set("error", integrante.objetos("Errores").get(0).string("MensajeCliente"));
            }

            if (prestamo.objetos("Datos") != null && !prestamo.objetos("Datos").isEmpty()) {
                idPrestamo = prestamo.objetos("Datos").get(0).string("Id");
            }

            if (esCuotificacion)
                new Futuro<>(() -> Solicitud.logApiVentas(contexto, finalIdSolicitud, "generarSolicitudCuotificacion", prestamo));

            if (!esAdelanto) {
                // Agrego seguro de desempleo
                if (!contexto.esJubilado() && contexto.persona().edad() < 65) { // Si es jubilado o es mayor de 65 no le agrego el seguro de desempleo
                    ApiResponse responseSeguroDesempleo = RestVenta.agregarSeguroDesempleo(contexto, idSolicitud, idPrestamo);
                    if (responseSeguroDesempleo.hayError() || !responseSeguroDesempleo.objetos("Errores").isEmpty()) {
                        return new Respuesta().setEstado("ERROR_POST_SEGURO_DESEMPLEO");
                    }
                    tieneSeguro = true;
                }
            }
            primerOferta = true;

        } else {
            // Si ya tiene CA o TD, eliminar la correspondiente en la solicitud, porque no
            // tiene que seleccionar una nueva

            boolean tieneCajaAhorroSolicitada = false;
            String idProductoCajaAhorro = "";
            boolean tieneTarjetaDebitoSolicitada = false;
            String idProductoTarjetaDebito = "";
            for (Objeto datoSolicitud : response.objetos("Datos")) {
                if (datoSolicitud.string("Id").equals(idSolicitud)) {
                    for (Objeto productoSolicitud : datoSolicitud.objetos("Productos")) {
                        if ("8".equals(productoSolicitud.string("tipoProducto"))) {
                            tieneCajaAhorroSolicitada = true;
                            idProductoCajaAhorro = productoSolicitud.string("Id");
                        }
                        if ("11".equals(productoSolicitud.string("tipoProducto"))) {
                            tieneTarjetaDebitoSolicitada = true;
                            idProductoTarjetaDebito = productoSolicitud.string("Id");
                        }
                    }
                }
            }
            if (contexto.cuentaPorDefecto() != null && contexto.poseeCuentasUnipersonales() && tieneCajaAhorroSolicitada) {
                RestVenta.eliminarCajaAhorroPesos(contexto, idSolicitud, idProductoCajaAhorro);
            }
            if (contexto.tarjetaDebitoPorDefecto() != null && tieneTarjetaDebitoSolicitada) {
                RestVenta.eliminarTarjetaDebito(contexto, idSolicitud, idProductoTarjetaDebito);
            }
        }

        if (idPrestamo == null || "".equals(idPrestamo)) {
            return Respuesta.estado("ERROR_PRESTAMO_VACIO"); // Hago esta validación por las dudas de que no tenga el idPrestamo. Si no lo
            // tengo no puedo seguir.
        }

        contexto.parametros.set("esCuotificacion", esCuotificacion);

        // EVALUAR SOLICITUD
        Respuesta respuesta = new Respuesta();
        Solicitud solicitud = Solicitud.solicitud(contexto, idSolicitud);
        ResolucionMotor evaluacionSolicitud = solicitud.ejecutarMotor(contexto);
        if (esCuotificacion) {
            new Futuro<>(() -> HBOmnicanalidad.insertarLogMotor(contexto, finalIdSolicitud, evaluacionSolicitud.ResolucionId, evaluacionSolicitud.Explicacion));
        }

        if (evaluacionSolicitud.esAprobadoAmarillo()) {
            if (esCuotificacion) {
                new Futuro<>(() -> RestVenta.desistirSolicitud(contexto, finalIdSolicitud));
            }
            return casoCanalAmarillo(contexto, solicitud, idPrestamo, tieneSeguro, evaluacionSolicitud, true, null);
        }
        if (evaluacionSolicitud.esAmarillo() || evaluacionSolicitud.esSAmarillo()) {
            if (esCuotificacion) {
                new Futuro<>(() -> RestVenta.desistirSolicitud(contexto, finalIdSolicitud));
            }
            new Futuro<>(() -> Solicitud.logOriginacion(contexto, finalIdSolicitud, "ejecutarMotor", null, "AMARILLO"));
            respuesta.set("color", "AMARILLO");
            return respuesta;
        }
        if (evaluacionSolicitud.esRojo()) {
            if (esCuotificacion) {
                new Futuro<>(() -> RestVenta.desistirSolicitud(contexto, finalIdSolicitud));
            }
            new Futuro<>(() -> Solicitud.logOriginacion(contexto, finalIdSolicitud, "ejecutarMotor", null, "ROJO"));
            respuesta.set("color", "ROJO");
            return respuesta;
        }

        contexto.parametros.set("idSolicitud", idSolicitud);
        contexto.parametros.set("idPrestamo", idPrestamo);
        contexto.parametros.set("tieneSeguroDesempleo", tieneSeguro);
        if (primerOferta) { // si le ofertan el prestamo por 1ra vez, tengo que guardarme el monto del
            // prestamo, ya que es el maximo que el usuario puede pedir
            contexto.parametros.set("primeraOferta", true);
            contexto.sesion.cache.put("primeraOferta", "true");
        }

        respuesta = consultarPrestamo(contexto);
        respuesta.set("color", "VERDE");
        if (esAptoRecurrencia && esAdelanto && contexto.esJubilado()) {
            respuesta.set("diasMinRec", Constantes.DIA_MIN_ADELANTO_REC);
            respuesta.set("diasMaxRec", Constantes.DIA_MAX_ADELANTO_REC);
            respuesta.set("declaracionCuotaRec", Constantes.TEXTO_ADELANTO_DECLARACION_CUOTA_REC);
            respuesta.set("mostrarBotonRecurrencia", true);
        }
        respuesta.set("diasMin", Constantes.DIA_MIN_ADELANTO);
        respuesta.set("diasMax", Constantes.DIA_MAX_ADELANTO);
        respuesta.set("declaracionCuota", esAdelanto ? Constantes.TEXTO_ADELANTO_DECLARACION_CUOTA : Constantes.TEXTO_DECLARACION_CUOTA);
        respuesta.set("legales", esAdelanto ? Constantes.TEXTO_ADELANTO_LEGALES : Constantes.TEXTO_LEGALES);
        respuesta.set("mostrarBotonMejorarOferta", !esAdelanto && ConfigHB.bool("prendido_canal_amarillo_pp"));

        return respuesta;
    }

    private static Respuesta casoCanalAmarillo(ContextoHB contexto, Solicitud solicitud, String idPrestamo, Boolean tieneSeguro, ResolucionMotor evaluacionSolicitud, Boolean primeraOferta, Cuenta cuenta) {
        Boolean mejorarOferta = contexto.parametros.bool("mejorarOferta", false);
        Boolean esAdelanto = contexto.parametros.bool("esAdelanto", false);
        Respuesta respuesta = new Respuesta();
        if (mejorarOferta && ConfigHB.bool("prendido_canal_amarillo_pp")) {
            contexto.parametros.set("idSolicitud", solicitud.IdSolicitud);
            contexto.parametros.set("idPrestamo", idPrestamo);
            contexto.parametros.set("tieneSeguroDesempleo", tieneSeguro);
            contexto.parametros.set("primeraOferta", primeraOferta);
            respuesta = consultarPrestamo(contexto);
            respuesta.set("declaracionCuota", Constantes.TEXTO_DECLARACION_CUOTA);
            respuesta.set("legales", Constantes.TEXTO_LEGALES);
        }

        if (!primeraOferta) {
            BigDecimal monto = contexto.parametros.bigDecimal("monto");
            Integer plazo = contexto.parametros.integer("plazo");
            Integer diaVencimiento = contexto.parametros.integer("diaVencimiento");
            ApiResponse prestamoModificar = RestVenta.modificarSolicitudPrestamoPersonal(contexto, solicitud.IdSolicitud, idPrestamo, monto, plazo, diaVencimiento, cuenta == null ? "0" : cuenta.numero(), "03");

            if (prestamoModificar.hayError() || prestamoModificar.objetos("Errores") != null && prestamoModificar.objetos("Errores").size() > 0) {
                Solicitud.logOriginacion(contexto, solicitud.IdSolicitud, "simular_validaMontoModifSolicitud", null, prestamoModificar.toJson());
                return Respuesta.error();
            }
            guardaMejoraOferta(contexto, solicitud.IdSolicitud);
        }

        Solicitud.logOriginacion(contexto, solicitud.IdSolicitud, "ejecutarMotor", null, "AMARILLO");
        respuesta.set("color", "AMARILLO");
        respuesta.set("explicacion", evaluacionSolicitud.ResolucionDesc);
        respuesta.set("mostrarBotonMejorarOferta", !esAdelanto && ConfigHB.bool("prendido_canal_amarillo_pp"));
        return respuesta;
    }

    public static Respuesta mejorarPrimerOfertaPrestamo(ContextoHB contexto) {
        contexto.parametros.set("mejorarOferta", true);
        contexto.parametros.set("forzarGeneracionOfertaNueva", true);
        String vienePrimerOferta = contexto.sesion.cache.get("primeraOferta");

        if (vienePrimerOferta == null || !ConfigHB.bool("prendido_canal_amarillo_pp")) {
            return Respuesta.estado("OPERACION_INHABILITADA_CANAL_AMARILLO");
        }

        if (tieneParametrosCanalAmarillo(contexto)) {
            return Respuesta.parametrosIncorrectos();
        }

        return solicitarPrimerOfertaPrestamo(contexto);
    }

    public static Respuesta consultarPrestamo(ContextoHB contexto) {
        String idSolicitud = contexto.parametros.string("idSolicitud");
        String idPrestamo = contexto.parametros.string("idPrestamo");
        Boolean esAdelanto = contexto.parametros.bool("esAdelanto", false);

        // Con el fin de no volver a consultar las solicitudes para evitar un llamado
        // más, paso por parametro
        // este valor, que define si tiene seguro o no la solicitud.
        Boolean tieneSeguroDesempleo = contexto.parametros.bool("tieneSeguroDesempleo");

        // En caso de que desde front quieran consultar si tiene seguro de desempleo,
        // tienen que pasar esta siguiente variable en true
        // para que busque entre las solicitudes el prestamo
        Boolean buscarSiTieneSeguro = contexto.parametros.bool("buscarSiTieneSeguro");

        // Desde el servicio que pide la primer oferta yo mando el siguiente parámetro
        // en true sólo cuando es la primera
        // vez que se pide la oferta (o sea cuando no tenía otra). Si la oferta ya
        // existía, no lo mando.
        // Desde cualquier otro lugar este parámetro se tiene que mandar en false, ya
        // que se usa para guardar en la sesión
        // el máximo monto.
        Boolean primeraOferta = contexto.parametros.bool("primeraOferta");

        // Esta variable es para el caso en que se quiera mejorar la primera oferta de
        // un prestamo personal
        Boolean mejorarOferta = contexto.parametros.bool("mejorarOferta", false);

        String idSeguroDesempleo = "";

        if (tieneSeguroDesempleo)
            buscarSiTieneSeguro = true; // si tiene seguro de desempleo no tengo alternativa que buscar el id y fijarme
        // cuál es la prima.

        if (buscarSiTieneSeguro) {
            tieneSeguroDesempleo = false;
            ApiResponse solicitudGet = RestVenta.consultarSolicitudes(contexto);
            if (solicitudGet.hayError()) {
                return Respuesta.error();
            }
            for (Objeto datoSolicitud : solicitudGet.objetos("Datos")) {
                if (datoSolicitud.string("Id").equals(idSolicitud)) {
                    for (Objeto productoSolicitud : datoSolicitud.objetos("Productos")) {
                        if ("31".equals(productoSolicitud.string("tipoProducto"))) {
                            tieneSeguroDesempleo = true;
                            idSeguroDesempleo = productoSolicitud.string("Id");
                        }
                    }
                }
            }
        }

        BigDecimal primaSeguro = null;
        BigDecimal sumaAsegurada = null;

        if (!"".equals(idSeguroDesempleo)) {
            ApiResponse seguroDesempleoGet = RestVenta.consultarMontoSeguroDesempleo(contexto, idSolicitud, idSeguroDesempleo);
            if (seguroDesempleoGet.hayError()) {
                return Respuesta.error();
            }
            if (!seguroDesempleoGet.objetos("Datos").isEmpty()) {
                primaSeguro = new BigDecimal(seguroDesempleoGet.objetos("Datos").get(0).string("Valor1").replace(",", "."));
                sumaAsegurada = new BigDecimal(seguroDesempleoGet.objetos("Datos").get(0).string("Valor2").replace(",", "."));
            }
        }

        Respuesta respuesta = new Respuesta();
        BigDecimal montoPrimeraOferta = new BigDecimal(0);
        ApiResponse prestamoGet = RestVenta.consultarSolicitudPrestamoPersonal(contexto, idSolicitud, idPrestamo);
        if (prestamoGet.hayError()) {
            return Respuesta.error();
        }

        if (prestamoGet.objetos("Datos") != null && prestamoGet.objetos("Datos").size() > 0) {
            Objeto item = prestamoGet.objetos("Datos").get(0);
            respuesta.set("idSolicitud", idSolicitud);
            respuesta.set("idPrestamo", idPrestamo);

            if (primeraOferta) {
                if (mejorarOferta) {
                    montoPrimeraOferta = contexto.sesion().montoMaximoPrestamo() != null ? contexto.sesion().montoMaximoPrestamo() : new BigDecimal(0);
                }
                contexto.sesion.montoMaximoPrestamo = (item.bigDecimal("MontoAprobado"));
            }

            if (contexto.sesion.montoMaximoPrestamo != null) {
                respuesta.set("montoMaximo", contexto.sesion.montoMaximoPrestamo);
                respuesta.set("montoMaximoFormateado", Formateador.importe(contexto.sesion.montoMaximoPrestamo));
            } else {
                Respuesta modalPP = HBPrestamo.getCampana(contexto);

                BigDecimal montoMaximo = modalPP != null && modalPP.existe("montoPP") && modalPP.string("montoPP") != "" ? modalPP.bigDecimal("montoPP") : item.bigDecimal("MontoAprobado");
                respuesta.set("montoMaximo", montoMaximo);
                respuesta.set("montoMaximoFormateado", Formateador.importe(montoMaximo));
                contexto.sesion.montoMaximoPrestamo = (montoMaximo);
            }
            BigDecimal monto = item.bigDecimal("MontoAprobado");
            BigDecimal importeSeguro = new BigDecimal(0);
            if (tieneSeguroDesempleo) {
                if (primaSeguro != null && sumaAsegurada != null && !sumaAsegurada.equals(new BigDecimal(0))) {
                    importeSeguro = item.bigDecimal("importeCuota").divide(sumaAsegurada, RoundingMode.CEILING).multiply(primaSeguro);
                }
            }
            respuesta.set("monto", monto);
            respuesta.set("montoFormateado", Formateador.importe(monto));

            if ("PPCUOTIFSG".equals(item.string("productoBancario")) || "PPCUOTIFSG".equals(item.string("producto"))) {
                respuesta.set("montoMinimo", new BigDecimal(1000.0));
                respuesta.set("montoMinimoFormateado", Formateador.importe(new BigDecimal(1000)));
            } else {
                respuesta.set("montoMinimo", esAdelanto ? ConfigHB.bigDecimal("monto_minimo_PP_adelanto") : ConfigHB.bigDecimal("monto_minimo_prestamo_personal"));
                respuesta.set("montoMinimoFormateado", esAdelanto ? Formateador.importe(ConfigHB.bigDecimal("monto_minimo_PP_adelanto")) : Formateador.importe(ConfigHB.bigDecimal("monto_minimo_prestamo_personal")));
            }
            respuesta.set("plazo", item.integer("Plazo"));
            respuesta.set("plazoDescripcion", esAdelanto && item.integer("Plazo") == 1 ? "Única Cuota" : null);

            if ("PPCUOTIFSG".equals(item.string("productoBancario")) || "PPCUOTIFSG".equals(item.string("producto"))) {
                respuesta.set("plazoMaximo", Integer.valueOf(12));
                respuesta.set("plazoMinimo", Integer.valueOf(3));
            } else {
                respuesta.set("plazoMaximo", esAdelanto && item.integer("Plazo") == 1 ? 1 : ConfigHB.integer("plazo_maximo_prestamo_personal"));
                respuesta.set("plazoMinimo", esAdelanto && item.integer("Plazo") == 1 ? 1 : ConfigHB.integer("plazo_minimo_prestamo_personal"));
            }

            if (item.integer("DiaCobro") != null) {
                respuesta.set("diaCobro", item.integer("DiaCobro"));
            } else {
                respuesta.set("diaCobro", 1);
            }
            respuesta.set("seguroDesempleo", tieneSeguroDesempleo);
            respuesta.set("importePrimeraCuota", item.bigDecimal("importeCuota").add(importeSeguro));
            respuesta.set("importePrimeraCuotaFormateado", Formateador.importe(item.bigDecimal("importeCuota").add(importeSeguro)));
            respuesta.set("importeSeguro", importeSeguro);
            respuesta.set("importeSeguroFormateado", Formateador.importe(importeSeguro));
            respuesta.set("cft", item.bigDecimal("CFT"));
            respuesta.set("cftFormateado", Formateador.importe(item.bigDecimal("CFT")));
            respuesta.set("tna", item.bigDecimal("Tasa"));
            respuesta.set("tnaFormateado", Formateador.importe(item.bigDecimal("Tasa")));
            respuesta.set("jubilado", "11".equals(contexto.persona().idSituacionLaboral()));
            respuesta.set("ofrecerSeguroDesempleo", esAdelanto ? false : contexto.persona().edad() < ConfigHB.integer("edad_limite_seguro_desempleo") || !contexto.esJubilado());

            Boolean vieneDePrimeraOferta = mejorarOferta && monto.compareTo(montoPrimeraOferta) > 0 && (monto.subtract(montoPrimeraOferta).intValue() >= ConfigHB.integer("monto_tope_mejorado_canal_amarillo"));
            Boolean vieneDelSimular = mejorarOferta && monto.compareTo(montoPrimeraOferta) == 0 && !primeraOferta;
            if (vieneDePrimeraOferta || vieneDelSimular) {
                respuesta.set("ofertaMejorada", true);
            }

        } else {
            return Respuesta.error();
        }

        return respuesta;
    }

    public static Respuesta simularOfertaPrestamo(ContextoHB contexto) {
        String idSolicitud = contexto.parametros.string("idSolicitud");
        String idPrestamo = contexto.parametros.string("idPrestamo");
        BigDecimal monto = contexto.parametros.bigDecimal("monto");
        String idCuenta = contexto.parametros.string("idCuenta", null);
        Integer plazo = contexto.parametros.integer("plazo");
        Integer diaVencimiento = contexto.parametros.integer("diaVencimiento");
        boolean esCuotificacion = false;
        Boolean esAdelanto = contexto.parametros.bool("esAdelanto", false);
        Boolean quiereSeguroDesempleo = !esAdelanto ? contexto.parametros.bool("seguroDesempleo") : false;

        contexto.sesion.setChallengeOtp(false);

        if (quiereSeguroDesempleo && contexto.esJubilado()) {
            return Respuesta.estado("JUBILADO_QUERIENDO_SEGURO");
        }

        List<String> nemonicos = new ArrayList<>();
        boolean tieneSeguroSolicitado = false;
        String idSeguroSolicitado = "";
        String idProductoCajaAhorro = "";
        String idProductoTarjetaDebito = "";

        if (Objeto.anyEmpty(idSolicitud, idPrestamo)) {
            return Respuesta.parametrosIncorrectos();
        }
        if (tieneParametrosCanalAmarillo(contexto)) {
            return Respuesta.parametrosIncorrectos();
        }

        if (monto == null) { // significa que es una solicitud que dio amarillo o rojo y no alcanzó a pasarme
            // el monto
            return solicitarPrimerOfertaPrestamo(contexto);
        }

        if (Objeto.anyEmpty(idSolicitud, idPrestamo, monto, plazo, diaVencimiento, quiereSeguroDesempleo)) {
            return Respuesta.parametrosIncorrectos();
        }

        Cuenta cuenta = null;

        if (contexto.persona().edad() == null)
            return Respuesta.estado("ERROR_SIN_EDAD");

        if (contexto.persona().edad() > 76)
            return Respuesta.estado("MAYOR_DE_EDAD_76");

        if (contexto.persona().edad() > 74 && plazo > 24)
            return Respuesta.estado("MAYOR_DE_EDAD_74");

        if (idCuenta == null) { // Me fijo si tiene cuenta.
            if (contexto.poseeCuentasUnipersonales()) {
                // return Respuesta.estado("SELECCIONAR_CUENTA");
                for (Cuenta itemCuenta : contexto.cuentas()) {
                    if (itemCuenta.unipersonal() && itemCuenta.idMoneda().equals("80") && !"I".equals(itemCuenta.idEstado())) {
                        idCuenta = itemCuenta.id();
                        cuenta = contexto.cuenta(idCuenta);
                        if (cuenta == null) {
                            return Respuesta.estado("CUENTA_INEXISTENTE");
                        }
                    }
                }
            }
        } else {
            cuenta = contexto.cuenta(idCuenta);
            if (cuenta == null) {
                return Respuesta.estado("CUENTA_INEXISTENTE");
            }
            if ("I".equals(cuenta.idEstado())) {
                return Respuesta.estado("CUENTA_INACTIVA");
            }
        }

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();

        // Tengo que buscar la solicitud y fijarme si ya está dada de alta la cuenta o
        // la td
        // si no está dada de alta la agrego

        // también, en caso de que venga false el seguro de desempleo, tengo que sacarlo
        // de la solicitud,
        // y si viene en true agregarlo (si es que no está agregado).
        boolean tieneCajaAhorroSolicitada = false;
        boolean tieneTarjetaDebitoSolicitada = false;
        ApiResponse response = RestVenta.consultarSolicitudes(contexto);
        if (response.hayError() || response.objetos("Datos").isEmpty()) {
            return Respuesta.error();
        }
        for (Objeto datoSolicitud : response.objetos("Datos")) {
            if (datoSolicitud.string("Id").equals(idSolicitud)) {
                for (Objeto productoSolicitud : datoSolicitud.objetos("Productos")) {
                    if ("2".equals(productoSolicitud.string("tipoProducto"))) {
                        if (!idPrestamo.equals(productoSolicitud.string("Id"))) {
                            // el idPrestamo que pasó por parámetro es diferente al encontrado en la
                            // solicitud
                            return Respuesta.error();
                        }

                        if (!productoSolicitud.string("Nemonico").isEmpty()) {
                            esCuotificacion = productoSolicitud.string("Nemonico").equals("PPCUOTIFSG");
                        }
                    }
                    if ("8".equals(productoSolicitud.string("tipoProducto"))) {
                        tieneCajaAhorroSolicitada = true;
                        idProductoCajaAhorro = productoSolicitud.string("Id");

                        // tengo que fijarme si la caja de ahorro no viene con datos nulos
                        // si viene con datos nulos la elimino.
                        String recurso = RestOmnicanalidad.recursos().get("8");
                        ApiResponse responseProducto = RestOmnicanalidad.buscarProducto(contexto, idSolicitud, recurso, idProductoCajaAhorro);
                        if (responseProducto.hayError()) {
                            return Respuesta.error();
                        }
                        Objeto datosProductoCajaAhorro = responseProducto.objetos("Datos").get(0);
                        if ("".equals(datosProductoCajaAhorro.string("ProductoBancario")) || ("0".equals(datosProductoCajaAhorro.string("Oficial")))) {
                            ApiResponse responseBajaCA = RestVenta.eliminarCajaAhorroPesos(contexto, idSolicitud, idProductoCajaAhorro);
                            if (!responseBajaCA.hayError() && responseBajaCA.objetos("Errores").isEmpty()) {
                                tieneCajaAhorroSolicitada = false;
                                idProductoCajaAhorro = "";
                            } else { // hubo un error al tratar de eliminarla, tengo que responder error
                                return Respuesta.estado("ERROR_BAJA_CA_SIN_DATOS");
                            }
                        }
                    }
                    if ("11".equals(productoSolicitud.string("tipoProducto"))) {
                        tieneTarjetaDebitoSolicitada = true;
                        idProductoTarjetaDebito = productoSolicitud.string("Id");
                        // tengo que fijarme si la caja de ahorro no viene con datos nulos
                        // si viene con datos nulos la elimino.
                        String recurso = RestOmnicanalidad.recursos().get("11");
                        ApiResponse responseProducto = RestOmnicanalidad.buscarProducto(contexto, idSolicitud, recurso, idProductoTarjetaDebito);
                        if (responseProducto.hayError()) {
                            return Respuesta.error();
                        }
                        Objeto datosProductoTarjetaDebito = responseProducto.objetos("Datos").get(0);
                        if ("".equals(datosProductoTarjetaDebito.string("Tipo")) || ("0".equals(datosProductoTarjetaDebito.string("Oficial")))) {
                            ApiResponse responseBajaTD = RestVenta.eliminarTarjetaDebito(contexto, idSolicitud, idProductoTarjetaDebito);
                            if (!responseBajaTD.hayError() && responseBajaTD.objetos("Errores").isEmpty()) {
                                tieneTarjetaDebitoSolicitada = false;
                                idProductoTarjetaDebito = "";
                            } else { // hubo un error al tratar de eliminarla, tengo que responder error
                                return Respuesta.estado("ERROR_BAJA_TD_SIN_DATOS");
                            }
                        }
                    }
                    if ("31".equals(productoSolicitud.string("tipoProducto"))) {
                        tieneSeguroSolicitado = true;
                        idSeguroSolicitado = productoSolicitud.string("Id");
                    }

                }
            }
        }
        if (tieneSeguroSolicitado && !quiereSeguroDesempleo) {
            // la solicitud tiene seguro de desempleo, pero como el usuario no lo quiere lo
            // tengo que eliminar de la solicitud
            ApiResponse eliminacionSeguroDesempleo = RestVenta.eliminarSeguroDesempleo(contexto, idSolicitud, idSeguroSolicitado);
            if (eliminacionSeguroDesempleo.hayError()) {
                return Respuesta.estado("ERROR_DELETE_SEGURO_DESEMPLEO");
            }
            tieneSeguroSolicitado = false;

        }

        if (true) {
            ApiResponse responseCanal = RestOmnicanalidad.actualizarCanalSolicitud(contexto, idSolicitud);
            if (responseCanal.hayError() || !responseCanal.objetos("Errores").isEmpty()) {
            }
        }

        if (!esAdelanto && !tieneSeguroSolicitado && quiereSeguroDesempleo) {
            // la solicitud NO tiene seguro de desempleo, pero como el usuario lo quiere lo
            // tengo que agregar a la solicitud
            ApiResponse responseSeguroDesempleo = RestVenta.agregarSeguroDesempleo(contexto, idSolicitud, idPrestamo);
            if (responseSeguroDesempleo.hayError() || !responseSeguroDesempleo.objetos("Errores").isEmpty()) {
                return new Respuesta().setEstado("ERROR_POST_SEGURO_DESEMPLEO");
            }
            tieneSeguroSolicitado = true;
            idSeguroSolicitado = responseSeguroDesempleo.objetos("Datos").get(0).string("Id");
        }

        if (!tieneCajaAhorroSolicitada && cuenta == null) {
            ApiResponse responseCajaAhorro = RestVenta.generarCajaAhorroPesos(contexto, idSolicitud);
            if (responseCajaAhorro.hayError() || responseCajaAhorro.objetos("Errores").size() > 0) {
                return Respuesta.estado("ERROR_SOLICITUD_CAJA_AHORRO");
            }
            tieneCajaAhorroSolicitada = true;
            // Sólo pido la tarjeta de débito si no tiene cuenta
            if (!tieneTarjetaDebitoSolicitada && tarjetaDebito == null) {
                ApiResponse responseTarjetaDebito = RestVenta.generarTarjetaDebito(contexto, idSolicitud, "0");
                if (responseTarjetaDebito.hayError() || responseTarjetaDebito.objetos("Errores").size() > 0) {
                    return Respuesta.estado("ERROR_SOLICITUD_TARJETA_DEBITO");
                }
                tieneTarjetaDebitoSolicitada = true;
            }
        }

        if (contexto.tarjetaDebitoPorDefecto() == null && cuenta == null && !tieneTarjetaDebitoSolicitada && tieneCajaAhorroSolicitada) {
            ApiResponse responseTarjetaDebito = RestVenta.generarTarjetaDebito(contexto, idSolicitud, "0");
            if (responseTarjetaDebito.hayError() || responseTarjetaDebito.objetos("Errores").size() > 0) {
                return Respuesta.estado("ERROR_SOLICITUD_TARJETA_DEBITO");
            }
            tieneTarjetaDebitoSolicitada = true;
        }

        if (tieneCajaAhorroSolicitada && cuenta != null) {
            ApiResponse responseBajaCA = RestVenta.eliminarCajaAhorroPesos(contexto, idSolicitud, idProductoCajaAhorro);
            if (!responseBajaCA.hayError() && responseBajaCA.objetos("Errores").isEmpty()) {
                tieneCajaAhorroSolicitada = false;
            }
            // Y le elimino también la tarjeta asociada:
            ApiResponse responseBajaTD = RestVenta.eliminarTarjetaDebito(contexto, idSolicitud, idProductoTarjetaDebito);
            if (!responseBajaTD.hayError() && responseBajaTD.objetos("Errores").isEmpty()) {
                tieneTarjetaDebitoSolicitada = false;
                idProductoTarjetaDebito = "";
            }
        }

        if (contexto.tarjetaDebitoPorDefecto() != null && tieneTarjetaDebitoSolicitada) {
            ApiResponse responseBajaTD = RestVenta.eliminarTarjetaDebito(contexto, idSolicitud, idProductoTarjetaDebito);
            if (!responseBajaTD.hayError() && responseBajaTD.objetos("Errores").isEmpty()) {
                tieneTarjetaDebitoSolicitada = false;
                idProductoTarjetaDebito = "";
            }
        }
        if (cuenta == null) {
            nemonicos.add("CASOLIC");
        }

        contexto.parametros.set("esCuotificacion", esCuotificacion);

        // Primero tengo que modificar la solicitud con 02 para poder evaluarla
        ApiResponse prestamoModificar = RestVenta.modificarSolicitudPrestamoPersonal(contexto, idSolicitud, idPrestamo, monto, plazo, diaVencimiento, cuenta == null ? "0" : cuenta.numero(), "02");
        if (prestamoModificar.hayError()) {
            return Respuesta.error();
        }
        if (prestamoModificar.objetos("Errores") != null && prestamoModificar.objetos("Errores").size() > 0) {
            return Respuesta.error();
        }

        if (tieneSeguroSolicitado) {
            ApiResponse seguro = RestVenta.modificarSeguroDesempleo(contexto, idSolicitud, idSeguroSolicitado, idPrestamo);
            if (seguro.hayError() || !seguro.objetos("Errores").isEmpty()) {
                return Respuesta.estado("ERROR_PUT_SEGURO_DESEMPLEO");
            }
        }

        // EVALUAR SOLICITUD
        Solicitud solicitud = Solicitud.solicitud(contexto, idSolicitud);
        ResolucionMotor evaluacionSolicitud = solicitud.ejecutarMotor(contexto);
        if (evaluacionSolicitud.esAprobadoAmarillo()) {
            return casoCanalAmarillo(contexto, solicitud, idPrestamo, quiereSeguroDesempleo, evaluacionSolicitud, false, cuenta);
        }

        if (evaluacionSolicitud.esAmarillo() || evaluacionSolicitud.esSAmarillo()) {
            Respuesta respuesta = new Respuesta();
            respuesta.set("color", "AMARILLO");
            return respuesta;
        }

        if (evaluacionSolicitud.esRojo()) {
            Respuesta respuesta = new Respuesta();
            respuesta.set("color", "ROJO");
            return respuesta;
        }

        // Tengo que verificar lo siguiente: si la solicitud quedó con un monto
        // diferente al que ingreso
        // el usuario, es porque no me aceptó el crédito que él pidió, sino uno de menor
        // monto.

        ApiResponse prestamoGet = RestVenta.consultarSolicitudPrestamoPersonal(contexto, idSolicitud, idPrestamo);
        if (prestamoGet.hayError()) {
            return Respuesta.error();
        }
        if (prestamoGet.objetos("Datos") != null && prestamoGet.objetos("Datos").size() > 0) {
            Objeto item = prestamoGet.objetos("Datos").get(0);
            if (item.bigDecimal("MontoAprobado").compareTo(monto) < 0) {
                Respuesta RespuestaErrorMonto = new Respuesta();
                RespuestaErrorMonto.setEstado("MONTO_APROBADO_MENOR_AL_PEDIDO");
                RespuestaErrorMonto.set("montoAprobado", item.bigDecimal("MontoAprobado"));
                RespuestaErrorMonto.set("montoAprobadoFormateado", Formateador.importe(item.bigDecimal("MontoAprobado")));
                return RespuestaErrorMonto;
            }
        } else {
            return Respuesta.estado("ERROR_CONSULTANDO_PRESTAMO");
        }

        // Ahora, cuando pase la evaluación. La vuelvo a mandar con 03.
        // No me queda alternativa que hacerlo así, ya que hay campos que sino no
        // impactan. Por ejemplo el dia de vencimiento
        // y quiero que eso se guarde para que la próxima que lo busque que lo tome.
        prestamoModificar = RestVenta.modificarSolicitudPrestamoPersonal(contexto, idSolicitud, idPrestamo, monto, plazo, diaVencimiento, cuenta == null ? "0" : cuenta.numero(), "03");

        if (prestamoModificar.hayError()) {
            return Respuesta.error();
        }
        if (prestamoModificar.objetos("Errores") != null && prestamoModificar.objetos("Errores").size() > 0) {
            return Respuesta.error();
        }

        contexto.parametros.set("idPrestamo", idPrestamo);
        contexto.parametros.set("tieneSeguroDesempleo", quiereSeguroDesempleo);

        Respuesta respuesta = consultarPrestamo(contexto);

        //Guardo en caché los valores que posteriormente se envian a salesforce
        //al finalizar el préstamo
        Objeto salesforceAltaPP = new Objeto();
        salesforceAltaPP.set("MONTO_PRESTAMO", respuesta.string("montoFormateado"));
        salesforceAltaPP.set("FECHA_VENCIMIENTO_CUOTA", respuesta.string("diaCobro"));
        salesforceAltaPP.set("MONTO_CUOTA", respuesta.string("importePrimeraCuotaFormateado"));
        contexto.sesion.cache.put(ConfigHB.string("salesforce_alta_prestamo"), salesforceAltaPP.toJson());

        if (!plazo.equals(respuesta.integer("plazo"))) {
            respuesta.set("plazoModificadoPorMotor", true);
            respuesta.set("plazoMinimo", respuesta.integer("plazo"));
        } else {
            respuesta.set("plazoModificadoPorMotor", false);
        }

        contexto.sesion.plazoPrestamoAprobado = (respuesta.integer("plazo"));
        contexto.sesion.montoPrestamoAprobado = (respuesta.bigDecimal("monto"));
        contexto.sesion.cuentaPrestamoAprobado = (cuenta == null ? null : cuenta.numero());

        nemonicos.add("PPVERDE");
        respuesta.set("nemonicos", nemonicos);
        respuesta.set("color", "VERDE");
        if (esAdelanto && contexto.esJubilado() && ConfigHB.bool("prendido_adelanto_recurrente", false)) {
            respuesta.set("diasMinRec", Constantes.DIA_MIN_ADELANTO_REC);
            respuesta.set("diasMaxRec", Constantes.DIA_MAX_ADELANTO_REC);
            respuesta.set("declaracionCuotaRec", Constantes.TEXTO_ADELANTO_DECLARACION_CUOTA_REC);
            respuesta.set("mostrarBotonRecurrencia", true);
            respuesta.set("esRecurrente", contexto.parametros.bool("esRecurrente", false));
        }
        respuesta.set("diasMin", Constantes.DIA_MIN_ADELANTO);
        respuesta.set("diasMax", Constantes.DIA_MAX_ADELANTO);
        respuesta.set("declaracionCuota", esAdelanto ? Constantes.TEXTO_ADELANTO_DECLARACION_CUOTA : Constantes.TEXTO_DECLARACION_CUOTA);
        respuesta.set("legales", esAdelanto ? Constantes.TEXTO_ADELANTO_LEGALES : Constantes.TEXTO_LEGALES);
        Solicitud.logOriginacion(contexto, idSolicitud, "FIN_simularOfertaPrestamo", null, respuesta.toJson());
        return respuesta;

    }

    public static Respuesta finalizarSolicitudPrestamo(ContextoHB contexto) {
        String idSolicitud = contexto.parametros.string("idSolicitud");
        Boolean esAdelanto = contexto.parametros.bool("esAdelanto", false);
        Boolean mejorarOferta = contexto.parametros.bool("mejorarOferta", false);
//		Boolean esRecurrente = contexto.parametros.bool("esRecurrente", false) && ConfigHB.bool("prendido_adelanto_recurrente", false);
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_modo_transaccional_alta_producto",
                "prendido_modo_transaccional_alta_producto_cobis") && !TransmitHB.isChallengeOtp(contexto, "prestamo-personal")) {
            try {
                String sessionToken = contexto.parametros.string(Transmit.getSessionToken(), null);
                String idCuenta = contexto.parametros.string("idCuenta", null);
                if (Objeto.empty(sessionToken, idCuenta))
                    return Respuesta.parametrosIncorrectos();

                Futuro<ApiResponse> futuroResponse = new Futuro<>(() -> RestOmnicanalidad.consultarSolicitud(contexto, idSolicitud));

                ApiResponse response = futuroResponse.tryGet();
                if (!response.hayError()) {
                    Objeto datos = response.objetos("Datos").get(0);
                    if (!Objeto.empty(datos)) {
                        Futuro<Objeto> futuroProductos = new Futuro<>(() -> HBOmnicanalidad.detalleSolicitud(contexto));

                        Objeto productos = futuroProductos.tryGet();
                        Cuenta cuenta = contexto.cuenta(idCuenta);

                        if (!Objeto.empty(productos, cuenta)) {
                            String tipo = "";
                            if (!Objeto.empty(datos.objeto("Productos")) && !datos.objeto("Productos").objetos().isEmpty())
                                tipo = datos.objeto("Productos").objetos().get(0).string("Producto");

                            String monto = productos.objeto("prestamoPersonal").string("monto");

                            AltaPrestamoHBBMBankProcess altaPrestamoHBBMBankProcess = new AltaPrestamoHBBMBankProcess(contexto.idCobis(),
                                    sessionToken,
                                    new BigDecimal(StringUtils.isNotBlank(monto) ? monto : "0"),
                                    Util.obtenerDescripcionMonedaTransmit("80"),
                                    StringUtils.isNotBlank(tipo) ? tipo : TransmitHB.REASON_TRANSFERENCIA,
                                    new AltaPrestamoHBBMBankProcess.Payer(contexto.persona().cuit(), cuenta.numero(), Util.BH_CODIGO, TransmitHB.CANAL),
                                    new AltaPrestamoHBBMBankProcess.Payee("", "", ""));

                            Respuesta respuesta = TransmitHB.recomendacionTransmit(contexto, altaPrestamoHBBMBankProcess, "prestamo-personal");
                            if (respuesta.hayError())
                                return respuesta;
                        }
                    }
                }
            } catch (Exception e) {
            }
        }

        boolean esMigrado = contexto.esMigrado(contexto);
        if (esMigrado && Objeto.anyEmpty(csmId, checksum))
            return Respuesta.parametrosIncorrectos();

        if (!ConfigHB.bool("prendido_alta_prestamos") || (esAdelanto && !ConfigHB.bool("prendido_adelanto_bh"))) {
            return Respuesta.estado("OPERACION_INHABILITADA");
        }

        if (Objeto.anyEmpty(idSolicitud)) {
            return Respuesta.parametrosIncorrectos();
        }

        Respuesta respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, esAdelanto ? "adelanto" : "prestamo-personal", JourneyTransmitEnum.HB_INICIO_SESION);
        if (respuestaValidaTransaccion.hayError())
            return respuestaValidaTransaccion;

        if (!esMigrado && !ConfigHB.bool("forzar_otp")) {
            if (!contexto.sesion.validaSegundoFactorSoftToken && !contexto.sesion.validaRiesgoNet && !contexto.sesion.validaSegundoFactorOtp) {
                return Respuesta.estado("REQUIERE_VALIDAR_RIESGO_NET_O_SOFTOKEN");
            }
        }
        if (!contexto.sesion.aceptaTyC()) {
            return Respuesta.estado("REQUIERE_ACEPTAR_TyC");
        }

        if (mejorarOferta && ConfigHB.bool("prendido_canal_amarillo_pp") && !contexto.sesion.adjuntaDocumentacion()) {
            return Respuesta.estado("REQUIERE_ADJUNTAR_DOCUMENTOS");
        }
        if (RestContexto.cambioDetectadoParaNormativoPPV2(contexto, true)) {
            return Respuesta.estado("CAMBIO_INFO_PERSONAL_IMPORTANTE_PRESTAMO");
        }

        // desde-consulto la solicitud para asegurarme que es la del que pidió
        // finalizarla
        Solicitud solicitud = Solicitud.solicitud(contexto, idSolicitud);
        if (solicitud == null) {
            return Respuesta.estado("ERROR_CONSULTA_SOLICITUD");
        }

        boolean encuentraIdCobis = false;
        for (Integrante integrante : solicitud.Integrantes) {
            if (contexto.idCobis().equals(integrante.IdCobis))
                encuentraIdCobis = true;
        }
        if (!encuentraIdCobis) {
            return Respuesta.estado("ERROR_ID_COBIS");
        }
        // hasta-consulto la solicitud para asegurarme que es la del que pidió
        // finalizarla

        // Para pruebas automatizadas en HOMO
        if (!ConfigHB.esProduccion() && contexto.idCobis().equals("3275531")) {
            Respuesta respuesta = new Respuesta();
            respuesta.set("idSolicitud", idSolicitud);
            respuesta.set("idSolicitudProxima", "");

            contexto.parametros.set("nemonico", "FINALIZA_PP");
            Util.contador(contexto);
            return respuesta;
        }

        boolean esCuotificacion = false;

        for (SolicitudProducto producto : solicitud.Productos) {
            String tipoProducto = producto.IdProductoFrontEnd;
            String nemonico = producto.Nemonico;

            if (tipoProducto.equals("2") && nemonico.equals("PPCUOTIFSG")) {
                esCuotificacion = true;
                break;
            }
        }
        contexto.parametros.set("esCuotificacion", esCuotificacion);

        String actualiza = validaSolicitudCanalAmarillo(contexto, solicitud.IdSolicitud);
        if (actualiza != "") {
            return Respuesta.estado(actualiza);
        }

        ApiResponse response = RestVenta.finalizarSolicitud(contexto, solicitud.IdSolicitud);
        if (response.hayError() || !response.objetos("Errores").isEmpty()) {
            contexto.sesion.setChallengeOtp(false);

            //Si el prestamo da error, elimino el registro que fue agregado en simular prestamo
            contexto.sesion.cache.remove(ConfigHB.string("salesforce_alta_prestamo"));

            String mensajeCliente = "";
            try {
                mensajeCliente = response.objetos("Errores").get(0).string("MensajeCliente");
            } catch (Exception e) {
            }
            if (mensajeCliente.contains("seleccione otra fecha de cobro fija")) {
                return Respuesta.estado("SELECCIONAR_OTRA_FECHA_COBRO");
            }
            if (response.objetos("Errores").get(0).string("MensajeDesarrollador").contains("FAULTCODE:40003 FAULTMSJ:Producto bancario deshabilitado")) {
                return new Respuesta().setEstado("ERROR_CORRIENDO_BATCH");
            }
            return Respuesta.error();
        }
        monitorearAltaPrestamo(contexto, solicitud);

        if (mejorarOferta && ConfigHB.bool("prendido_canal_amarillo_pp")) {
            SqlPrestamos.finalizaSolicitudCanalAmarillo(contexto, idSolicitud);
            // SqlPrestamos.eliminarCacheActividades(contexto);
        }

        ProductosService.eliminarCacheProductos(contexto);

        String idSolicitudProxima = "";
        if (ConfigHB.esOpenShift()) {
            idSolicitudProxima = RestOmnicanalidad.limpiarSolicitudes(contexto, ConfigHB.longer("solicitud_dias_vigente", 30L), true, false, true);
        }

        Respuesta respuesta = new Respuesta();
        respuesta.set("idSolicitud", idSolicitud);
        respuesta.set("idSolicitudProxima", idSolicitudProxima);
        contexto.parametros.set("nemonico", "FINALIZA_PP");
        Util.contador(contexto);
        notificaEmailPP(contexto, solicitud);
        Solicitud.logOriginacion(contexto, idSolicitud, "FIN_finalizarSolicitud", null, respuesta.toJson());
        contexto.sesion.setAceptaTyC(false);
        contexto.sesion.validaRiesgoNet = (false);
        contexto.sesion().setAdjuntaDocumentacion(false);
        contexto.limpiarSegundoFactor();
        contexto.insertarLogPrestamos(contexto, contexto.sesion.montoPrestamoAprobado, contexto.sesion.plazoPrestamoAprobado, contexto.sesion.cuentaPrestamoAprobado);

        return respuesta;
    }

    private static void monitorearAltaPrestamo(ContextoHB contexto, Solicitud solicitud) {
        if (!HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_monitoreo_transaccional_3")) {
            return;
        }
        List<SolicitudProducto> prestamosPersonales = solicitud.Productos.stream().filter(producto -> producto.tipoProducto.equals("2")).collect(Collectors.toList());

        if (prestamosPersonales.isEmpty()) {
            return;
        }
        String idPrestamo = prestamosPersonales.get(0).Id;
        ApiRequest request = Api.request("prestamoPersonalGET", "ventas_windows", "GET", "/solicitudes/{numeroSolicitud}/prestamoPersonal/{numeroPrestamo}", contexto);
        request.path("numeroSolicitud", solicitud.IdSolicitud);
        request.path("numeroPrestamo", idPrestamo);
        ApiResponse prestamoPersonalResponse = Api.response(request, contexto.idCobis());
        if (prestamoPersonalResponse.hayError()) {
            return;
        }
        List<Objeto> formasdesembolso = prestamoPersonalResponse.objetos("Datos").get(0).objeto("Desembolsos").objetos("FormasDesembolso");
        if (formasdesembolso == null || formasdesembolso.isEmpty()) {
            return;
        }
        String cuentaDesembolso = formasdesembolso.get(0).string("Referencia");
        if (cuentaDesembolso == null || cuentaDesembolso.isEmpty() || cuentaDesembolso.equals("0")) {
            return;
        }
        String montoAprobado = prestamoPersonalResponse.objetos("Datos").get(0).string("MontoAprobado");
        HBMonitoring monitoringApi = new HBMonitoring();
        monitoringApi.sendMonitoringAltaPrestamo(contexto, solicitud.IdSolicitud, montoAprobado, cuentaDesembolso);
    }

    public static Respuesta datosPersonalesFaltantes(ContextoHB contexto) {
        String tipoProducto = contexto.parametros.string("tipoProducto");
        boolean simularTodoTrue = false; // esto es sólo para ayudar al front end a que le traiga casos
        boolean irSucursal = false;
        boolean validaDatosPersonalesOriginacion = true;
        boolean esMonoProductoTC = contexto.esMonoProductoTC();
        boolean poseeCuentasUnipersonales = contexto.poseeCuentasUnipersonales();

        if (Objeto.anyEmpty(tipoProducto)) {
            return Respuesta.parametrosIncorrectos();
        }

        if (!"paquete".equals(tipoProducto) && !"prestamo".equals(tipoProducto) && !"prestamo_paquete".equals(tipoProducto)) {
            return Respuesta.parametrosIncorrectos();
        }

        Respuesta respuesta = new Respuesta();
        ApiResponse clientes = RestPersona.consultarClienteEspecifico(contexto, contexto.idCobis());
        if (clientes.hayError()) {
            return Respuesta.error();
        }

        Objeto cliente = clientes.objetos().get(0);

        if ("paquete".equals(tipoProducto) || "prestamo".equals(tipoProducto) || "prestamo_paquete".equals(tipoProducto)) {
            Objeto datosPrincipales = new Objeto();
            Objeto datosPoliticos = new Objeto();

            /* DATOS PRINCIPALES - DESDE */
            if (cliente.string("nombres").isEmpty() || simularTodoTrue) {
                irSucursal = true;
            }
            if (cliente.string("apellidos").isEmpty() || simularTodoTrue) {
                irSucursal = true;
            }
            if (cliente.string("numeroDocumento").isEmpty() || simularTodoTrue) {
                irSucursal = true;
            }
            if (cliente.string("idTipoDocumento").isEmpty() || simularTodoTrue) {
                irSucursal = true;
            }
            if (cliente.string("cuit").isEmpty() || simularTodoTrue) {
                irSucursal = true;
            }

            if (cliente.string("idSexo").isEmpty() || simularTodoTrue) {
                irSucursal = true;
            }

            /* DATOS PRINCIPALES - HASTA */

            /* DATOS LABORALES - DESDE */

            String direccionMail = RestPersona.direccionEmail(contexto, contexto.persona().cuit());
            if (direccionMail == null || "".equals(direccionMail) || simularTodoTrue) {
                if (esMonoProductoTC || !poseeCuentasUnipersonales)
                    irSucursal = true;
                datosPrincipales.add("direccionMail");
                validaDatosPersonalesOriginacion = false;
            }
            Objeto celular = RestPersona.celular(contexto, contexto.persona().cuit());
            if (celular == null || simularTodoTrue) {
                if (esMonoProductoTC || !poseeCuentasUnipersonales)
                    irSucursal = true;
                datosPrincipales.add("celular");
                validaDatosPersonalesOriginacion = false;
            }
            if (cliente.string("idEstadoCivil") == null || simularTodoTrue) {
                datosPrincipales.add("estadoCivil");
                validaDatosPersonalesOriginacion = false;
            }
            if (cliente.integer("idNivelEstudios") == null || simularTodoTrue) {
                datosPrincipales.add("nivelEstudios");
                validaDatosPersonalesOriginacion = false;
            }

            if (cliente.string("idSituacionVivienda").isEmpty() || simularTodoTrue) {
                datosPrincipales.add("situacionVivienda");
                validaDatosPersonalesOriginacion = false;
            }
            /* DATOS LABORALES - HASTA */

            /* DOMICILIO LEGAL - DESDE */
            Objeto domicilio = RestPersona.domicilioLegal(contexto, contexto.persona().cuit());
            if (domicilio == null) {
                domicilio = new Objeto();
            }
            Objeto domicilioLegal = new Objeto();
            domicilioLegal.set("faltan_datos", false);
            if ("".equals(domicilio.string("calle")) || "".equals(domicilio.string("numero")) || "".equals(domicilio.string("idCodigoPostal")) || "".equals(domicilio.string("idCiudad"))) {
                domicilioLegal.set("faltan_datos", true);
                validaDatosPersonalesOriginacion = false;
            }

            domicilioLegal.set("calle", domicilio.string("calle"));
            domicilioLegal.set("altura", domicilio.string("numero"));
            domicilioLegal.set("piso", domicilio.string("piso"));
            domicilioLegal.set("departamento", domicilio.string("departamento"));
            domicilioLegal.set("codigoPostal", domicilio.string("idCodigoPostal"));
            domicilioLegal.set("idPais", domicilio.integer("idPais"));
            domicilioLegal.set("idProvincia", domicilio.integer("idProvincia"));
            domicilioLegal.set("idLocalidad", domicilio.integer("idCiudad", 146));
            domicilioLegal.set("provincia", RestCatalogo.nombreProvincia(contexto, domicilio.integer("idProvincia", 1)));
            domicilioLegal.set("localidad", RestCatalogo.nombreLocalidad(contexto, domicilio.integer("idProvincia", 1), domicilio.integer("idCiudad", 146)));
            domicilioLegal.set("entreCalle1", domicilio.string("calleEntre1"));
            domicilioLegal.set("entreCalle2", domicilio.string("calleEntre2"));
            /* DOMICILIO LEGAL - HASTA */

            /* DOMICILIO POSTAL - DESDE */
            domicilio = RestPersona.domicilioPostal(contexto, contexto.persona().cuit());
            if (domicilio == null) {
                domicilio = new Objeto();
            }
            Objeto domicilioPostal = new Objeto();
            domicilioPostal.set("faltan_datos", false);
            if ("".equals(domicilio.string("calle")) || "".equals(domicilio.string("numero")) || "".equals(domicilio.string("idCodigoPostal")) || "".equals(domicilio.string("idCiudad"))) {
                if (esMonoProductoTC || !poseeCuentasUnipersonales) {
                    if (!"".equals(domicilio.string("numero"))) {
                        domicilio.set("piso", "".equals(domicilio.string("piso")) ? "-" : domicilio.string("piso"));
                        domicilio.set("departamento", "".equals(domicilio.string("departamento")) ? "-" : domicilio.string("departamento"));
                        if (!"".equals(domicilio.string("calle")) && !"".equals(domicilio.string("numero")) && !"".equals(domicilio.string("idCodigoPostal")) && !"".equals(domicilio.string("idCiudad"))) {
                            // si esto da error lo dejo pasar, le va a dar amarillo después (sólo por el
                            // piso y el depto)
                            RestPersona.actualizarDomicilio(contexto, contexto.persona().cuit(), domicilio, "DP");
                        } else {
                            irSucursal = true;
                            domicilioPostal.set("faltan_datos", true);
                            validaDatosPersonalesOriginacion = false;
                        }
                    }
                } else {
                    domicilioPostal.set("faltan_datos", true);
                    validaDatosPersonalesOriginacion = false;
                }
            }

            domicilioPostal.set("calle", domicilio.string("calle"));
            domicilioPostal.set("altura", domicilio.string("numero"));
            domicilioPostal.set("piso", domicilio.string("piso"));
            domicilioPostal.set("departamento", domicilio.string("departamento"));
            domicilioPostal.set("codigoPostal", domicilio.string("idCodigoPostal"));
            domicilioPostal.set("idPais", domicilio.integer("idPais"));
            domicilioPostal.set("idProvincia", domicilio.integer("idProvincia"));
            domicilioPostal.set("idLocalidad", domicilio.integer("idCiudad", 146));
            domicilioPostal.set("provincia", RestCatalogo.nombreProvincia(contexto, domicilio.integer("idProvincia", 1)));
            domicilioPostal.set("localidad", RestCatalogo.nombreLocalidad(contexto, domicilio.integer("idProvincia", 1), domicilio.integer("idCiudad", 146)));
            domicilioPostal.set("entreCalle1", domicilio.string("calleEntre1"));
            domicilioPostal.set("entreCalle2", domicilio.string("calleEntre2"));
            /* DOMICILIO POSTAL - HASTA */

            /* DATOS POLITICOS - DESDE */
            datosPoliticos.set("sujetoObligado", cliente.bool("esSO"));
            datosPoliticos.set("expuestoPoliticamente", cliente.bool("esPEP"));
            datosPoliticos.set("estadounidenseOresidenciaFiscalOtroPais", cliente.bool("indicioFatca") || !"L".equals(cliente.string("idResidencia")));
            /* DATOS POLITICOS - HASTA */

            respuesta.set("irSucursal", irSucursal);
            respuesta.set("monoProducto", esMonoProductoTC);
            respuesta.set("poseeCuentasUnipersonales", poseeCuentasUnipersonales);
            respuesta.set("datosPrincipales", datosPrincipales.lista != null ? datosPrincipales : null);
            respuesta.set("datosPoliticos", datosPoliticos);
            respuesta.set("domicilioLegal", domicilioLegal);
            respuesta.set("domicilioPostal", domicilioPostal);

            ApiResponse responseActividades = RestPersona.consultarActividades(contexto);
            if (!responseActividades.hayError()) {
                String idProfesion = "";
                String idRamo = "";
                String idCargo = "";
                BigDecimal ingresoNeto = null;
                Integer idActividad = null;

                boolean cargarActividad = true;
                boolean tienePrincipal = false;
                for (Objeto item : responseActividades.objetos()) {
                    if (item.bool("esPrincipal")) {
                        tienePrincipal = true;
                        break;
                    }
                }

                // La realidad es que tengo que recorrer todas las direcciones y por más que no
                // sea la principal
                // cambiarle la profesión en caso de venir 40001
                for (Objeto item : responseActividades.objetos()) {
                    if ("".equals(item.string("fechaEgresoActividad"))) {
                        // Si es una profesión actual limpio estas variables para fijarme si hay una que
                        // le falte.
                        idProfesion = "";
                        idRamo = "";
                        idCargo = "";
                        ingresoNeto = null;
                        idActividad = item.integer("id");
                        String idSituacionLaboral = item.string("idSituacionLaboral");
                        idProfesion = item.string("idProfesion");
                        idRamo = item.string("idRamo");
                        idCargo = item.string("idCargo");
                        ingresoNeto = item.bigDecimal("ingresoNeto");

                        if ("40001".equals(idProfesion) || "".equals(idProfesion)) {
                            idProfesion = "11900";
                            ApiResponse responseActividad = RestPersona.actualizarActividad(contexto, idActividad, idSituacionLaboral, idProfesion, idRamo, idCargo, ingresoNeto, null);
                            if (!responseActividad.hayError()) {
                                idProfesion = "11900";
                                item.set("idProfesion", "11900");
                            }
                        }
                    }
                }

                idProfesion = "";
                idRamo = "";
                idCargo = "";
                ingresoNeto = null;
                idActividad = null;

                if (tienePrincipal) {
                    for (Objeto item : responseActividades.objetos()) {
                        if ("".equals(item.string("fechaEgresoActividad"))) {
                            // Si es una profesión actual limpio estas variables para fijarme si hay una que
                            // le falte.
                            idProfesion = "";
                            idRamo = "";
                            idCargo = "";
                            ingresoNeto = null;
                            idActividad = item.integer("id");
                            idProfesion = item.string("idProfesion");
                            idRamo = item.string("idRamo");
                            idCargo = item.string("idCargo");
                            ingresoNeto = item.bigDecimal("ingresoNeto");

                            if ("40001".equals(idProfesion)) {
                                idProfesion = "";
                            }
                            if (!"".equals(idProfesion) && !"".equals(idRamo) && ingresoNeto != null) {
                                cargarActividad = false;
                                // si una profesión tiene los tres datos, no hace falta actualizar ni dar de
                                // alta ninguna profesión,
                                // por lo tanto hago un break
                                break;
                            }
                        }
                    }
                }

                if (cargarActividad) {
                    BigDecimal minimoSueldo = ConfigHB.bigDecimal("minimo_sueldo");
                    if (minimoSueldo == null) {
                        minimoSueldo = new BigDecimal(69500);
                    }
                    if ("".equals(idProfesion))
                        idProfesion = "11900";
                    if ("".equals(idRamo))
                        idRamo = "040307";
                    if ("".equals(idCargo))
                        idCargo = "1501";
                    if (ingresoNeto == null)
                        ingresoNeto = minimoSueldo;
                    if (tienePrincipal)
                        RestPersona.actualizarActividad(contexto, idActividad, "1", idProfesion, idRamo, idCargo, ingresoNeto, null);
                    else
                        RestPersona.actualizarActividad(contexto, idActividad, "1", idProfesion, idRamo, idCargo, ingresoNeto, true);
                }

            }

        }
        contexto.sesion.validaDatosPersonalesOriginacion = (validaDatosPersonalesOriginacion);

        return respuesta;
    }

    public static Respuesta datosPersonalesModificar(ContextoHB contexto) {
        String idNivelEstudios = contexto.parametros.string("nivelEstudios");
        String idSituacionVivienda = contexto.parametros.string("idSituacionVivienda");
        String email = contexto.parametros.string("email");
        String idEstadoCivil = contexto.parametros.string("idEstadoCivil");
        Boolean esSujetoObligado = contexto.parametros.bool("sujetoObligado", null);
        Boolean esExpuestoPoliticamente = contexto.parametros.bool("expuestoPoliticamente", null);
        Boolean estadounidenseOResidenciaFiscalOtroPais = contexto.parametros.bool("estadounidenseOresidenciaFiscalOtroPais", null);
        String celularCodigoArea = contexto.parametros.string("celular.codigoArea");
        String celularCaracteristica = contexto.parametros.string("celular.caracteristica");
        String celularNumero = contexto.parametros.string("celular.numero");
        String calleParticular = contexto.parametros.string("domicilioPostal.calle");
        String alturaParticular = contexto.parametros.string("domicilioPostal.altura");
        String pisoParticular = contexto.parametros.string("domicilioPostal.piso");
        String departamentoParticular = contexto.parametros.string("domicilioPostal.departamento");
        String idProvinciaParticular = contexto.parametros.string("domicilioPostal.idProvincia");
        String idLocalidadParticular = contexto.parametros.string("domicilioPostal.idLocalidad");
        String codigoPostalParticular = contexto.parametros.string("domicilioPostal.codigoPostal");
        String entreCalle1Particular = contexto.parametros.string("domicilioPostal.entreCalle1");
        String entreCalle2Particular = contexto.parametros.string("domicilioPostal.entreCalle2");
        String calleUnicaEntrega = contexto.parametros.string("domicilioLegal.calle");
        String alturaUnicaEntrega = contexto.parametros.string("domicilioLegal.altura");
        String pisoUnicaEntrega = contexto.parametros.string("domicilioLegal.piso");
        String departamentoUnicaEntrega = contexto.parametros.string("domicilioLegal.departamento");
        String idProvinciaUnicaEntrega = contexto.parametros.string("domicilioLegal.idProvincia");
        String idLocalidadUnicaEntrega = contexto.parametros.string("domicilioLegal.idLocalidad");
        String codigoPostalUnicaEntrega = contexto.parametros.string("domicilioLegal.codigoPostal");
        String entreCalle1UnicaEntrega = contexto.parametros.string("domicilioLegal.entreCalle1");
        String entreCalle2UnicaEntrega = contexto.parametros.string("domicilioLegal.entreCalle2");
        String cuitEmpleador = contexto.parametros.string("cuitEmpleador");

        String mailAnterior = contexto.persona().email();
        String celularAnterior = contexto.persona().celular();

        boolean esMigrado = contexto.esMigrado(contexto);

        if(!esMigrado){
            if (!contexto.validaSegundoFactor("originacion-datos-personales") && !contexto.sesion.validaDatosPersonalesOriginacion) {
                Respuesta respuestaAux = new Respuesta();
                respuestaAux.setEstado("REQUIERE_SEGUNDO_FACTOR");
                respuestaAux.set("validaSegundoFactor", contexto.validaSegundoFactor("originacion"));
                respuestaAux.set("validaDatosPersonalesOriginacion", contexto.sesion.validaDatosPersonalesOriginacion);
                return respuestaAux;
            }

            if (contexto.validaSegundoFactor("originacion-datos-personales"))
                contexto.sesion.validaDatosPersonalesOriginacion = (true);
        }else{
            if(!contexto.sesion.validaDatosPersonalesOriginacion)
                return Respuesta.requiereSegundoFactor()
                        .set("validaSegundoFactor", false)
                        .set("validaDatosPersonalesOriginacion", false);

            contexto.sesion.validaDatosPersonalesOriginacion = true;
        }
        contexto.sesion.save();

        if (Objeto.allEmpty(idNivelEstudios, idSituacionVivienda, email, idEstadoCivil, celularCodigoArea, celularCaracteristica, celularNumero, calleParticular, alturaParticular, pisoParticular, departamentoParticular, idLocalidadParticular, codigoPostalParticular, calleUnicaEntrega, alturaUnicaEntrega, pisoUnicaEntrega, departamentoUnicaEntrega, idLocalidadUnicaEntrega, codigoPostalUnicaEntrega, esSujetoObligado, esExpuestoPoliticamente, estadounidenseOResidenciaFiscalOtroPais, cuitEmpleador))
            return Respuesta.parametrosIncorrectos();

        if (esSujetoObligado != null && esExpuestoPoliticamente != null && estadounidenseOResidenciaFiscalOtroPais != null) {
            if (esSujetoObligado || esExpuestoPoliticamente || estadounidenseOResidenciaFiscalOtroPais) {
                return Respuesta.estado("DATOS_POLITICOS_TRUE"); // el servicio no está diseñado para cambiar datos políticos
            }
        }
        if (!Objeto.allEmpty(idNivelEstudios, idSituacionVivienda, idEstadoCivil, esSujetoObligado, esExpuestoPoliticamente, estadounidenseOResidenciaFiscalOtroPais)) {

            ApiRequest request = Api.request("PersonaPatch", "personas", "PATCH", "/personas/{id}", contexto);
            request.header("x-usuario", ConfigHB.string("configuracion_usuario"));
            // request.path("id", contexto.idCobis());
            request.path("id", contexto.persona().cuit());

            if (!"".equals(idNivelEstudios))
                request.body("idNivelEstudios", idNivelEstudios);
            if (!"".equals(idSituacionVivienda))
                request.body("idSituacionVivienda", idSituacionVivienda);
            if (!"".equals(idEstadoCivil))
                request.body("idEstadoCivil", idEstadoCivil);

            if (esSujetoObligado != null) {
                request.body("esSO", esSujetoObligado);
            }
            if (esExpuestoPoliticamente != null)
                request.body("esPEP", esExpuestoPoliticamente);

            if (estadounidenseOResidenciaFiscalOtroPais != null && !estadounidenseOResidenciaFiscalOtroPais) {
                // no modifico estos datos si viene true estadounidenseOResidenciaFiscalOtroPais
                // (ya estoy tirando un error antes)
                request.body("indicioFatca", false);
                request.body("idResidencia", "L");
            }

            ApiResponse response = Api.response(request, contexto.idCobis());
            if (response.hayError()) {
                if (response.string("mensajeAlUsuario").contains("Producto bancario deshabilitado")) {
                    return Respuesta.estado("ERROR_CORRIENDO_BATCH");
                }
                return Respuesta.error();
            }
            Api.eliminarCache(contexto, "personas", contexto.idCobis());
        }

        String modificacion = "";
        if (email != null && !"".equals(email)) {
            String modificacionMailAux = RestPersona.compararMailActualizado(contexto, email);
            ApiResponse responseMail = RestPersona.actualizarEmail(contexto, contexto.persona().cuit(), email);
            if (responseMail.hayError()) {
                return Respuesta.error();
            }
            contexto.insertarLogCambioMail(contexto, mailAnterior, email);
            modificacion += modificacionMailAux;
        }

        if (celularCodigoArea != null && !"".equals(celularCodigoArea) && celularNumero != null && !"".equals(celularNumero)) {
            String modificacionCelularAux = RestPersona.compararCelularActualizado(contexto, celularCodigoArea, celularCaracteristica, celularNumero);
            ApiResponse responseCelular = RestPersona.actualizarCelular(contexto, contexto.persona().cuit(), celularCodigoArea, celularCaracteristica, celularNumero);
            if (responseCelular.hayError()) {
                RestPersona.enviarMailActualizacionDatosPersonales(contexto, modificacion, mailAnterior, celularAnterior);
                return Respuesta.error();
            }
            contexto.insertarLogCambioCelular(contexto, celularAnterior, celularCodigoArea + celularCaracteristica + celularNumero);

            if (!"".equals(modificacionCelularAux)) {
                modificacion = ("".equals(modificacion) ? modificacionCelularAux : modificacion + ", " + modificacionCelularAux);
            }
        }

        if ((calleParticular != null && !"".equals(calleParticular)) || (alturaParticular != null && !"".equals(alturaParticular)) || (idLocalidadParticular != null && !"".equals(idLocalidadParticular)) || (codigoPostalParticular != null && !"".equals(codigoPostalParticular))) {
            if (pisoParticular == null || "".equals(pisoParticular))
                pisoParticular = "-";
            if (departamentoParticular == null || "".equals(departamentoParticular))
                departamentoParticular = "-";
            if (entreCalle1Particular == null || "".equals(entreCalle1Particular))
                entreCalle1Particular = "-";
            if (entreCalle2Particular == null || "".equals(entreCalle2Particular))
                entreCalle2Particular = "-";

            Objeto domicilio = new Objeto();
            domicilio.set("calle", calleParticular);
            domicilio.set("numero", alturaParticular);
            domicilio.set("piso", pisoParticular);
            domicilio.set("departamento", departamentoParticular);
            domicilio.set("idProvincia", idProvinciaParticular);
            domicilio.set("idCiudad", idLocalidadParticular);
            domicilio.set("idCodigoPostal", codigoPostalParticular);
            domicilio.set("calleEntre1", entreCalle1Particular);
            domicilio.set("calleEntre2", entreCalle2Particular);

            String modificacionDireccionAux = RestPersona.compararDomicilioActualizado(contexto, calleParticular, alturaParticular, pisoParticular, departamentoParticular, idProvinciaParticular, idLocalidadParticular, codigoPostalParticular);

            ApiResponse responseDomicilioParticular = RestPersona.actualizarDomicilio(contexto, contexto.persona().cuit(), domicilio, "DP");
            if (responseDomicilioParticular.hayError()) {
                RestPersona.enviarMailActualizacionDatosPersonales(contexto, modificacion, mailAnterior, celularAnterior);
                String error = "ERROR";
                error = responseDomicilioParticular.string("mensajeAlUsuario").contains("NO EXISTE CODIGO POSTAL") ? "NO_EXISTE_CODIGO_POSTAL_DOMICILIO_PARTICULAR" : error;
                error = responseDomicilioParticular.string("mensajeAlUsuario").contains("DEBEN SER NUMERICOS") ? "PARAMETROS_INCORRECTOS_DOMICILIO_PARTICULAR" : error;
                return Respuesta.estado(error);
            }

            if (!"".equals(modificacionDireccionAux)) {
                modificacion = ("".equals(modificacion) ? modificacionDireccionAux : modificacion + ", " + modificacionDireccionAux);
            }
        }

        if ((calleUnicaEntrega != null && !"".equals(calleUnicaEntrega)) || (alturaUnicaEntrega != null && !"".equals(alturaUnicaEntrega)) || (pisoUnicaEntrega != null && !"".equals(pisoUnicaEntrega)) || (departamentoUnicaEntrega != null && !"".equals(departamentoUnicaEntrega)) || (idLocalidadUnicaEntrega != null && !"".equals(idLocalidadUnicaEntrega)) || (codigoPostalUnicaEntrega != null && !"".equals(codigoPostalUnicaEntrega))) {
            if (pisoUnicaEntrega == null || "".equals(pisoUnicaEntrega))
                pisoUnicaEntrega = "-";
            if (departamentoUnicaEntrega == null || "".equals(departamentoUnicaEntrega))
                departamentoUnicaEntrega = "-";
            if (entreCalle1UnicaEntrega == null || "".equals(entreCalle1UnicaEntrega))
                entreCalle1UnicaEntrega = "-";
            if (entreCalle2UnicaEntrega == null || "".equals(entreCalle2UnicaEntrega))
                entreCalle2UnicaEntrega = "-";

            Objeto domicilio = new Objeto();
            domicilio.set("calle", calleUnicaEntrega);
            domicilio.set("numero", alturaUnicaEntrega);
            domicilio.set("piso", pisoUnicaEntrega);
            domicilio.set("departamento", departamentoUnicaEntrega);
            domicilio.set("idCiudad", idLocalidadUnicaEntrega);
            domicilio.set("idCodigoPostal", codigoPostalUnicaEntrega);
            domicilio.set("idProvincia", idProvinciaUnicaEntrega);
            domicilio.set("calleEntre1", entreCalle1UnicaEntrega);
            domicilio.set("calleEntre2", entreCalle2UnicaEntrega);
            ApiResponse responseDomicilioUnicaEntrega = RestPersona.actualizarDomicilio(contexto, contexto.persona().cuit(), domicilio, "LE");
            if (responseDomicilioUnicaEntrega.hayError()) {
                RestPersona.enviarMailActualizacionDatosPersonales(contexto, modificacion, mailAnterior, celularAnterior);
                String error = "ERROR";
                error = responseDomicilioUnicaEntrega.string("mensajeAlUsuario").contains("NO EXISTE CODIGO POSTAL") ? "NO_EXISTE_CODIGO_POSTAL_DOMICILIO_UNICA_ENTREGA" : error;
                error = responseDomicilioUnicaEntrega.string("mensajeAlUsuario").contains("DEBEN SER NUMERICOS") ? "PARAMETROS_INCORRECTOS_DOMICILIO_UNICA_ENTREGA" : error;
                return Respuesta.estado(error);
            }
        }

        Api.eliminarCache(contexto, "Cliente", contexto.idCobis());
        RestPersona.enviarMailActualizacionDatosPersonales(contexto, modificacion, mailAnterior, celularAnterior);

        Respuesta respuesta = new Respuesta();

        return respuesta;
    }

    private static Boolean tieneParametrosCanalAmarillo(ContextoHB contexto) {
        String idSituacionLaboral = contexto.parametros.string("idSituacionLaboral");
        String ingresoNeto = contexto.parametros.string("ingresoNeto");
        String categoriaMonotributista = contexto.parametros.string("letra", "");
        Boolean mejorarOferta = contexto.parametros.bool("mejorarOferta", false);

        if (!mejorarOferta || !ConfigHB.bool("prendido_canal_amarillo_pp")) {
            return false;
        }

        if (!Objeto.empty(idSituacionLaboral)) {
            if ("6".equals(idSituacionLaboral) || "66".equals(idSituacionLaboral)) {
                return Objeto.anyEmpty(idSituacionLaboral, categoriaMonotributista);
            }
            if ("11".equals(idSituacionLaboral) || "1".equals(idSituacionLaboral)) {
                return Objeto.anyEmpty(idSituacionLaboral, ingresoNeto);
            }
        }
        return true;
    }

    public static Respuesta reclamoDocumentacion(ContextoHB contexto) {
        String solicitud = contexto.parametros.string("solicitud", "");
        Respuesta respuesta = new Respuesta();
        try {
            if (Objeto.anyEmpty(solicitud)) {
                return Respuesta.parametrosIncorrectos();
            }

            ApiResponse response = RestOriginacion.consultarReclamosDocumentacion(contexto, solicitud);

            if (response.hayError()) {
                return Respuesta.error();
            }

            if (response.objeto("estadoReclamo").get("descripcion").equals("ESPERA_RECEPCION") && response.codigo != 204) {
                ApiResponse recepcionReclamo = RestProcesos.recepcionReclamoDocumentacion(contexto, solicitud, response.objetos("detalles").get(0).objetos("claseDocumental").get(0).string("id"));
                if (recepcionReclamo.hayError()) {
                    return Respuesta.error();
                }
            } else {
                return Respuesta.estado("SIN_RECLAMOS");
            }
        } catch (Exception e) {
            return Respuesta.error();
        }
        return respuesta;
    }

    public static Respuesta envioMailCanalAmarillo(ContextoHB contexto) {
        try {
            Objeto parametros = new Objeto();
            parametros.set("Subject", "Solicitud en tramite");
            parametros.set("NOMBRE", contexto.persona().nombre());
            parametros.set("APELLIDO", contexto.persona().apellido());
            parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
            Date hoy = new Date();
            parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
            parametros.set("HORA", new SimpleDateFormat("hh:mm").format(hoy));
            parametros.set("CANAL", "Home Banking");
            parametros.set("TITULAR_CANAL", contexto.persona().apellido());

            RestNotificaciones.envioMail(contexto, ConfigHB.string("doppler_alta_prestamo"), parametros);
        } catch (Exception e) {
        }

        return Respuesta.exito();
    }

    private static String validaSolicitudCanalAmarillo(ContextoHB contexto, String idSolicitud) {
        Boolean mejorarOferta = contexto.parametros.bool("mejorarOferta", false);

        if (mejorarOferta && ConfigHB.bool("prendido_canal_amarillo_pp")) {
            Objeto solicitudCanalAmarillo = SqlPrestamos.solicitudCanalAmarillo(contexto, idSolicitud);

            if (!Objeto.empty(solicitudCanalAmarillo) && solicitudCanalAmarillo.existe("solicitud")) {
                solicitudCanalAmarillo = solicitudCanalAmarillo.objeto("solicitud");
                String ingresos_nuevos = solicitudCanalAmarillo.string("ingresos_nuevos").trim();
                ApiResponse response = RestPersona.actualizarActividad(contexto, Integer.parseInt(solicitudCanalAmarillo.string("id_actividad").trim()), solicitudCanalAmarillo.string("situacion_labora_nueva").trim(), solicitudCanalAmarillo.string(""), // idProfesion
                        solicitudCanalAmarillo.string(""), // idRamo
                        solicitudCanalAmarillo.string(""), // idCargo
                        new BigDecimal(Formateador.importe(ingresos_nuevos)), true);
                if (response.hayError()) {
                    return "ERROR_ACTUALIZANDO_ACTIVIDAD";
                }
            } else {
                return "ERROR_VOLVER_A_SIMULAR_CANAL_AMARILLO";
            }
        }
        return "";
    }

    private static Boolean guardaMejoraOferta(ContextoHB contexto, String idSolicitud) {
        String idSituacionLaboral = contexto.parametros.string("idSituacionLaboral");
        BigDecimal ingresoNeto = contexto.parametros.bigDecimal("ingresoNeto");
        String cuitEmpleador = (idSituacionLaboral.equals("11")) ? Solicitud.cuitAnses : contexto.parametros.string("cuit", "");
        Date fecha = contexto.parametros.date("fecha", null);
        String categoriaMonotributista = contexto.parametros.string("letra", "").toUpperCase();

        SituacionLaboral situacionActual = SituacionLaboral.situacionLaboralPrincipal(contexto);

        if (Objeto.empty(ingresoNeto) && categoriaMonotributista != "") {
            ingresoNeto = new BigDecimal(HBCatalogo.montoMonotributo(contexto, categoriaMonotributista));
        }

        return SqlPrestamos.guardaSolicitudCanalAmarillo(contexto, idSolicitud, situacionActual.id, idSituacionLaboral, categoriaMonotributista, ingresoNeto, fecha, cuitEmpleador);
    }

    public static Respuesta eliminaSolicitudFront(ContextoHB contexto) {
        String idSolicitud = contexto.parametros.string("idSolicitud");
        Boolean insertarDatos = SqlPrestamos.insertEliminaSolicitud(contexto, idSolicitud);

        if (insertarDatos) {
            return Respuesta.exito();
        }

        return Respuesta.error();
    }

    public static boolean esSolicitudEliminada(ContextoHB contexto, String idSolicitud) {
        Objeto eliminaSolicitud = SqlPrestamos.selectEliminaSolicitud(contexto, idSolicitud);

        if (eliminaSolicitud != null) {
            return true;
        }

        return false;
    }

    private static Respuesta notificaEmailPP(ContextoHB contexto, Solicitud solicitud) {
        Boolean esAdelanto = contexto.parametros.bool("esAdelanto", false) && HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_adelanto_bh");
        Boolean mejorarOferta = contexto.parametros.bool("mejorarOferta", false);

        BigDecimal montoAprobado = contexto.sesion.montoPrestamoAprobado;
        String cuenta = contexto.sesion.cuentaPrestamoAprobado;

        if (solicitud.esAdelanto() && esAdelanto && HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_adelanto_bh")) {
            contexto.parametros.set("monto", montoAprobado);
            contexto.parametros.set("cuenta", cuenta);
            return HBNotificaciones.envioEmailDesembolsoAdelanto(contexto);
        }

        if (mejorarOferta && solicitud.Estado.equalsIgnoreCase("AA")) {
            return null;
        }

        return HBNotificaciones.envioEmailPP(contexto);
    }

    private static Boolean tieneAdelantoRecurrente(ContextoHB contexto) {
        Boolean tieneAdelantoRecurrente = false;
        for (Prestamo prestamo : contexto.prestamos()) {

            Objeto item = new Objeto();
            item.set("id", prestamo.id());
            item.set("idTipoProducto", prestamo.idTipo());
            item.set("descripcion", descripcionPrestamo(prestamo));
            item.set("nroPrestamo", prestamo.numero());
            item.set("idMoneda", prestamo.idMoneda());
            item.set("estado", prestamo.estado());
            item.set("simboloMoneda", prestamo.simboloMoneda());
            item.set("montoAdeudado", prestamo.codigo().equals("PPADELANTO") ? prestamo.montoUltimaCuotaFormateado() : prestamo.montoAdeudadoFormateado());
            item.set("cuotaActual", prestamo.enConstruccion() ? "0" : prestamo.cuotaActual());
            item.set("cantidadCuotas", prestamo.cuotasPendientes() + prestamo.cuotaActual());
            item.set("cantidadCuotasPendientes", !prestamo.enConstruccion() ? prestamo.cuotasPendientes() : prestamo.cantidadCuotas());
            item.set("cantidadCuotasVencidas", prestamo.cuotasVencidas());
            item.set("fechaProximoVencimiento", prestamo.fechaProximoVencimiento("dd/MM/yyyy"));
            item.set("porcentajeFechaProximoVencimiento", Fecha.porcentajeTranscurrido(31L, prestamo.fechaProximoVencimiento()));
            item.set("saldoActual", prestamo.montoUltimaCuotaFormateado());
            item.set("codigo", prestamo.codigo());
            item.set("pagable", prestamo.codigo().equals("PPADELANTO"));
            item.set("enConstruccion", prestamo.enConstruccion());
            if (prestamo.esRecurrente()) {
                tieneAdelantoRecurrente = true;
                continue;
            }
        }
        return tieneAdelantoRecurrente;
    }

    private static String descripcionPrestamo(Prestamo prestamo) {
        String descripcion = tieneCategoria(prestamo);

        if (prestamo.descripcionPrestamo().contains("Crédito Refacción")) {
            descripcion = prestamo.tipo();
        } else {
            if ("Personal".equalsIgnoreCase(prestamo.categoria())) {
                descripcion = "Préstamo " + prestamo.tipo();
            }
            if ("Hipotecario".equalsIgnoreCase(prestamo.categoria())) {
                descripcion = "Crédito " + prestamo.tipo();
            }
            if ("Personal".equalsIgnoreCase(prestamo.categoria()) && "Adelanto".equalsIgnoreCase(prestamo.tipo())) {
                descripcion = "Adelanto de Sueldo";
            }
        }

        return descripcion;
    }

    private static String tieneCategoria(Prestamo prestamo) {
        if (prestamo.categoria().trim().isEmpty()) {
            return prestamo.tipo();
        }
        return prestamo.categoria();
    }

}

