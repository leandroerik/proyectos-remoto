package ar.com.hipotecario.mobile.api;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.LongAdder;

import ar.com.hipotecario.backend.base.Version;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.lib.Encriptador;
import ar.com.hipotecario.mobile.CanalMobile;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.servicio.RestPersona;
import ar.com.hipotecario.mobile.servicio.SqlRegistroDispositivo;

public class MBAplicacion {

    public static Objeto error(ContextoMB contexto) {
        throw new RuntimeException();
    }

    public static Objeto estado(ContextoMB contexto) {
        Objeto objeto = new Objeto();
        objeto.set("estado", "0");
        objeto.set("status", "UP");
        contexto.setHeader("Content-Type", "application/json");
        return objeto;
    }

    public static ar.com.hipotecario.backend.base.Objeto version(ContextoMB contexto) {
        return Version.get();
    }

    public static Objeto headers(ContextoMB contexto) {
        Objeto objeto = new Objeto();
        Map<String, String> mapa = contexto.headers();
        for (String clave : mapa.keySet()) {
            objeto.set(clave, mapa.get(clave));
        }
        return objeto;
    }

    public static Object env(ContextoMB contexto) {
        String token = contexto.parametros.string("token");
        String dev = contexto.requestHeader("dev");

        if (!"true".equals(dev) || !token.equals(Encriptador.nextSha())) {
            return null;
        }

        Map<String, Object> map = new TreeMap<>();
        map.putAll(System.getenv());
        return Objeto.fromMap(map).set("estado", "0");
    }

    public static RespuestaMB configuracion(ContextoMB contexto) {
        Objeto config = new Objeto();
        config.set("mantenimiento", ConfigMB.bool("mantenimiento", false));
        config.set("mantenimiento_titulo", ConfigMB.string("mantenimiento_titulo", "Servicio momentáneamente suspendido."));
        config.set("mantenimiento_mensaje", ConfigMB.string("mantenimiento_mensaje", "Disculpá las molestias ocasionadas, intentá más tarde por favor."));
        config.set("fachu", !ConfigMB.bool("deshabilitar_isva", false));
        if (ConfigMB.string("mostrar_aviso_mantenimiento", "false").equals("true")) {
            config.set("mantenimiento_futuro", ConfigMB.string("aviso_mantenimiento_futuro", null));
        }

        // puede ser desarrollo, homologacion o produccion
        config.set("ambiente", ConfigMB.string("ambiente", ""));

        // DRS
        config.set("prendidoModoEscuchaLogin", funcionalidadPrendida("prendido_modo_escucha_login"));
        config.set("prendidoModoEscuchaNuevoBeneficiario", funcionalidadPrendida("prendido_modo_escucha_nuevo_beneficiario"));
        config.set("prendidoModoEscuchaDebin", funcionalidadPrendida("prendido_modo_escucha_debin"));
        config.set("prendidoModoEscuchaVentaDolares", funcionalidadPrendida("prendido_modo_escucha_venta_dolares"));
        config.set("prendidoModoEscuchaRescateFondos", funcionalidadPrendida("prendido_modo_escucha_rescate_fondos"));
        config.set("prendidoModoEscuchaDatosPersonales", funcionalidadPrendida("prendido_modo_escucha_datos_personales"));
        config.set("prendidoModoEscuchaAltaProducto", funcionalidadPrendida("prendido_modo_escucha_alta_producto"));
        config.set("prendidoModoEscuchaAltaSegundoFactor", funcionalidadPrendida("prendido_modo_escucha_alta_segundo_factor"));
        config.set("prendidoModoEscuchaAltaNuevoDispositivo", funcionalidadPrendida("prendido_modo_escucha_alta_nuevo_dispositivo"));

        return RespuestaMB.exito("config", config);
    }

    public static RespuestaMB configuracionUsuario(ContextoMB contexto, String idCobis) {
        Objeto configuracion = new Objeto();
        Boolean habilitadoPagoTarjeta = "true".equals(ConfigMB.string("prendido_pago_tarjetas"));
        Set<String> cobisHabilitadoPagoTarjeta = Objeto.setOf(ConfigMB.string("prendido_pago_tarjetas_cobis").split("_"));
        configuracion.set("prendidoPagoTarjetas", habilitadoPagoTarjeta || cobisHabilitadoPagoTarjeta.contains(contexto.idCobis()) ? "true" : "false");
        Boolean habilitadoPaquetes = "true".equals(ConfigMB.string("prendido_paquetes"));
        Boolean habilitadoLicitaciones = "true".equals(ConfigMB.string("prendido_licitaciones"));
        Boolean habilitadoBlanqueoPil = "true".equals(ConfigMB.string("prendido_blanqueo_pil"));
        Boolean habilitadoBlanqueoPin = "true".equals(ConfigMB.string("prendido_blanqueo_pin"));
        Boolean habilitadoPlataNueva = "true".equals(ConfigMB.string("prendido_plata_nueva"));
        Boolean habilitadoTodopagoVisahome = "true".equals(ConfigMB.string("prendido_todopago_visahome"));
        Boolean habilitadoMailSms = "true".equals(ConfigMB.string("prendido_habilitacion_mail_sms"));
        Boolean habilitadoPrestamos = "true".equals(ConfigMB.string("prendido_prestamos"));
        Boolean habilitadoPaquetesOriginacion = "true".equals(ConfigMB.string("prendido_paquetes_originacion"));
        Boolean habilitadoPrestamosOriginacion = "true".equals(ConfigMB.string("prendido_prestamos_originacion"));
        Boolean habilitadoCambioPasswordLogueado = "true".equals(ConfigMB.string("prendido_cambio_password_logueado"));
        Boolean habilitadoCambioUsuarioLogueado = "true".equals(ConfigMB.string("prendido_cambio_usuario_logueado"));
        Boolean habilitadoAltaCajaAhorro = "true".equals(ConfigMB.string("prendido_alta_caja_ahorro"));
        Boolean habilitadoConsultaComprobantes = "true".equals(ConfigMB.string("prendido_consulta_comprobantes"));
        Boolean habilitadoBajaProductos = "true".equals(ConfigMB.string("prendido_baja_productos"));
        Boolean habilitadoDebin = "true".equals(ConfigMB.string("prendido_debin"));
        Boolean habilitadoBuhoPuntos = "true".equals(ConfigMB.string("prendido_buho_puntos"));
        Boolean habilitadoResumenCuenta = "true".equals(ConfigMB.string("prendido_resumen_cuenta"));
        Boolean habilitadoPagoPrestamos = "true".equals(ConfigMB.string("prendido_pago_prestamos"));
        Boolean prendidoPruebaCambioClave = "true".equals(ConfigMB.string("prendido_prueba_cambio_clave"));
        Boolean prendidoIdentificaCbuCVu = "true".equals(ConfigMB.string("prendidoIdentificaCbuCVu"));
        Integer concurrenciaPagos = ConfigMB.integer("concurrencia_pagos");
        Boolean prendidoLogicaFechaTC = "true".equals(ConfigMB.string("prendidoLogicaFechaTC"));
        Boolean prendidoSellos = "true".equals(ConfigMB.string("prendido_sellos"));
        Boolean prendidoMejoraEcheq = "true".equals(ConfigMB.string("prendido_mejora_echeq"));
        Boolean prendidoAumentoLimiteEmpleados = "true".equals(ConfigMB.string("prendidoAumentoLimiteEmpleados"));
        Boolean prendidoTdvCarrusel = "true".equals(ConfigMB.string("mb_prendido_tdv_carrusel"));
        Boolean prendidoPausadoTd = "true".equals(ConfigMB.string("prendido_pausado_td"));
        Boolean prendidoPlanV = "true".equals(ConfigMB.string("prendido_plan_v"));
        Boolean prendidoSeccionModo = "true".equals(ConfigMB.string("prendido_seccion_modo"));
        
        configuracion.set("prendidoFciSuscripcion", funcionalidadPrendida(contexto.idCobis(), "prendido_Fci_Suscripcion", "prendido_Fci_Suscripcion_cobis"));

        configuracion.set("prendidoFciRescate", funcionalidadPrendida(contexto.idCobis(), "prendido_Fci_Rescate", "prendido_Fci_Rescate_cobis"));

        configuracion.set("prendidoHaceRendirTuDinero", MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_hace_rendir_tu_dinero", "prendido_hace_rendir_tu_dinero_cobis"));

        configuracion.set("prendidoTdvCarrusel", prendidoTdvCarrusel);

        configuracion.set("prendidoTransferenciasHaberes", funcionalidadPrendida(contexto.idCobis(), "prendido_transferencias_haberes", "prendido_transferencias_haberes_cobis"));

        configuracion.set("prendidoPaquetes", habilitadoPaquetes);
        if (ConfigMB.string("prendido_paquetes_cobis") != null && !habilitadoPaquetes) {
            Set<String> cobisHabilitadoPaquetes = Objeto.setOf(ConfigMB.string("prendido_paquetes_cobis").split("_"));
            configuracion.set("prendidoPaquetes", habilitadoPaquetes || cobisHabilitadoPaquetes.contains(contexto.idCobis()) ? true : false);
        }
        configuracion.set("prendidoLicitaciones", habilitadoLicitaciones);
        if (ConfigMB.string("prendido_licitaciones_cobis") != null && !habilitadoLicitaciones) {
            Set<String> cobisHabilitadoLicitaciones = Objeto.setOf(ConfigMB.string("prendido_licitaciones_cobis").split("_"));
            configuracion.set("prendidoLicitaciones", habilitadoLicitaciones || cobisHabilitadoLicitaciones.contains(contexto.idCobis()) ? true : false);
        }
        configuracion.set("prendidoBlanqueoPil", habilitadoBlanqueoPil);
        if (ConfigMB.string("prendido_blanqueo_pil_cobis") != null && !habilitadoBlanqueoPil) {
            Set<String> cobisHabilitadoBlanqueoPil = Objeto.setOf(ConfigMB.string("prendido_blanqueo_pil_cobis").split("_"));
            configuracion.set("prendidoBlanqueoPil", habilitadoBlanqueoPil || cobisHabilitadoBlanqueoPil.contains(contexto.idCobis()) ? true : false);
        }
        configuracion.set("prendidoBlanqueoPin", habilitadoBlanqueoPin);
        if (ConfigMB.string("prendido_blanqueo_pin_cobis") != null && !habilitadoBlanqueoPin) {
            Set<String> cobisHabilitadoBlanqueoPin = Objeto.setOf(ConfigMB.string("prendido_blanqueo_pin_cobis").split("_"));
            configuracion.set("prendidoBlanqueoPin", habilitadoBlanqueoPin || cobisHabilitadoBlanqueoPin.contains(contexto.idCobis()) ? true : false);
        }
        configuracion.set("prendidoPlataNueva", habilitadoPlataNueva);
        if (ConfigMB.string("prendido_plata_nueva_cobis") != null && !habilitadoPlataNueva) {
            Set<String> cobisHabilitadoPlataNueva = Objeto.setOf(ConfigMB.string("prendido_plata_nueva_cobis").split("_"));
            configuracion.set("prendidoPlataNueva", habilitadoPlataNueva || cobisHabilitadoPlataNueva.contains(contexto.idCobis()) ? true : false);
        }
        configuracion.set("habilitadoTodopagoVisahome", habilitadoTodopagoVisahome);
        if (ConfigMB.string("prendido_todopago_visahome_cobis") != null && !habilitadoTodopagoVisahome) {
            Set<String> cobisHabilitadoTodopagoVisahome = Objeto.setOf(ConfigMB.string("prendido_plata_nueva_cobis").split("_"));
            configuracion.set("prendidoTodopagoVisahome", habilitadoTodopagoVisahome || cobisHabilitadoTodopagoVisahome.contains(contexto.idCobis()) ? true : false);
        }
        configuracion.set("prendidoHabilitacionMailSms", habilitadoMailSms);
        if (ConfigMB.string("prendido_habilitacion_mail_sms_cobis") != null && !habilitadoMailSms) {
            Set<String> cobisHabilitadoMailSms = Objeto.setOf(ConfigMB.string("prendido_habilitacion_mail_sms_cobis").split("_"));
            configuracion.set("prendidoHabilitacionMailSms", habilitadoMailSms || cobisHabilitadoMailSms.contains(contexto.idCobis()) ? true : false);
        }

        Integer valorOtpSegundoFactor = RestPersona.sugerirOtpSegundoFactor(idCobis);
        configuracion.set("sugerirOtpSegundoFactor", valorOtpSegundoFactor == null);

        configuracion.set("otpSegundoFactorHabilitadoPorUsuario", valorOtpSegundoFactor != null && valorOtpSegundoFactor.equals(1));

        configuracion.set("prendidoPrestamos", habilitadoPrestamos);
        if (ConfigMB.string("prendido_prestamos_cobis") != null && !habilitadoPrestamos) {
            Set<String> cobisHabilitadoPrestamos = Objeto.setOf(ConfigMB.string("prendido_prestamos_cobis").split("_"));
            configuracion.set("prendidoPrestamos", habilitadoPrestamos || cobisHabilitadoPrestamos.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoPrestamosOriginacion", habilitadoPrestamosOriginacion);
        if (ConfigMB.string("prendido_prestamos_originacion_cobis") != null && !habilitadoPrestamosOriginacion) {
            Set<String> cobisHabilitadoPrestamosOriginacion = Objeto.setOf(ConfigMB.string("prendido_prestamos_originacion_cobis").split("_"));
            configuracion.set("prendidoPrestamosOriginacion", habilitadoPrestamosOriginacion || cobisHabilitadoPrestamosOriginacion.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoPaquetesOriginacion", habilitadoPaquetesOriginacion);
        if (ConfigMB.string("prendido_paquetes_originacion_cobis") != null && !habilitadoPaquetesOriginacion) {
            Set<String> cobisHabilitadoPaquetesOriginacion = Objeto.setOf(ConfigMB.string("prendido_paquetes_originacion_cobis").split("_"));
            configuracion.set("prendidoPaquetesOriginacion", habilitadoPaquetesOriginacion || cobisHabilitadoPaquetesOriginacion.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoCambioPasswordLogueado", habilitadoCambioPasswordLogueado);
        if (ConfigMB.string("prendido_cambio_password_logueado_cobis") != null && !habilitadoCambioPasswordLogueado) {
            Set<String> cobisHabilitadoCambioPasswordLogueado = Objeto.setOf(ConfigMB.string("prendido_cambio_password_logueado_cobis").split("_"));
            configuracion.set("prendidoCambioPasswordLogueado", habilitadoCambioPasswordLogueado || cobisHabilitadoCambioPasswordLogueado.contains(contexto.idCobis()) ? true : false);
        }
        configuracion.set("prendidoCambioUsuarioLogueado", habilitadoCambioUsuarioLogueado);
        if (ConfigMB.string("prendido_cambio_usuario_logueado_cobis") != null && !habilitadoCambioUsuarioLogueado) {
            Set<String> cobisHabilitadoCambioUsuarioLogueado = Objeto.setOf(ConfigMB.string("prendido_cambio_usuario_logueado_cobis").split("_"));
            configuracion.set("prendidoCambioUsuarioLogueado", habilitadoCambioUsuarioLogueado || cobisHabilitadoCambioUsuarioLogueado.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoAltaCA", habilitadoAltaCajaAhorro);
        if (ConfigMB.string("prendido_alta_caja_ahorro_cobis") != null && !habilitadoAltaCajaAhorro) {
            Set<String> cobisHabilitadoAltaCA = Objeto.setOf(ConfigMB.string("prendido_alta_caja_ahorro_cobis").split("_"));
            configuracion.set("prendidoAltaCA", habilitadoAltaCajaAhorro || cobisHabilitadoAltaCA.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoConsultaComprobantes", habilitadoConsultaComprobantes);
        if (ConfigMB.string("prendido_consulta_comprobantes_cobis") != null && !habilitadoConsultaComprobantes) {
            Set<String> cobisHabilitadoConsultaComprobantes = Objeto.setOf(ConfigMB.string("prendido_consulta_comprobantes_cobis").split("_"));
            configuracion.set("prendidoConsultaComprobantes", habilitadoConsultaComprobantes || cobisHabilitadoConsultaComprobantes.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoBajaProductos", habilitadoBajaProductos);
        if (ConfigMB.string("prendido_baja_productos_cobis") != null && !habilitadoBajaProductos) {
            Set<String> cobisHabilitadoBajaProductos = Objeto.setOf(ConfigMB.string("prendido_baja_productos_cobis").split("_"));
            configuracion.set("prendidoBajaProductos", habilitadoBajaProductos || cobisHabilitadoBajaProductos.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoDebin", habilitadoDebin);
        if (ConfigMB.string("prendido_debin_cobis") != null && !habilitadoDebin) {
            Set<String> cobisHabilitadoDebin = Objeto.setOf(ConfigMB.string("prendido_debin_cobis").split("_"));
            configuracion.set("prendidoDebin", habilitadoDebin || cobisHabilitadoDebin.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoBuhoPuntos", habilitadoBuhoPuntos);
        if (ConfigMB.string("prendido_buho_puntos_cobis") != null && !habilitadoBuhoPuntos) {
            Set<String> cobisHabilitadoBuhoPuntos = Objeto.setOf(ConfigMB.string("prendido_buho_puntos_cobis").split("_"));
            configuracion.set("prendidoBuhoPuntos", habilitadoBuhoPuntos || cobisHabilitadoBuhoPuntos.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoResumenCuenta", habilitadoResumenCuenta);
        if (ConfigMB.string("prendido_resumen_cuenta_cobis") != null && !habilitadoResumenCuenta) {
            Set<String> cobisHabilitadoResumenCuenta = Objeto.setOf(ConfigMB.string("prendido_resumen_cuenta_cobis").split("_"));
            configuracion.set("prendidoResumenCuenta", habilitadoResumenCuenta || cobisHabilitadoResumenCuenta.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("prendidoPagoPrestamos", habilitadoPagoPrestamos);
        if (ConfigMB.string("prendido_pago_prestamos_cobis") != null && !habilitadoPagoPrestamos) {
            Set<String> cobisHabilitadoPagoPrestamos = Objeto.setOf(ConfigMB.string("prendido_pago_prestamos_cobis").split("_"));
            configuracion.set("prendidoPagoPrestamos", habilitadoPagoPrestamos || cobisHabilitadoPagoPrestamos.contains(contexto.idCobis()) ? true : false);
        }
        configuracion.set("prendidoPruebaCambioClave", prendidoPruebaCambioClave);
        configuracion.set("prendidoIdentificaCbuCVu", prendidoIdentificaCbuCVu);
        configuracion.set("prendidoSellos", prendidoSellos);
        configuracion.set("prendidoMejoraEcheq", prendidoMejoraEcheq);
        configuracion.set("prendidoSeccionModo", prendidoSeccionModo);

        configuracion.set("prendidoBajaProductosPaquetes", funcionalidadPrendida(contexto.idCobis(), "prendido_baja_productos_paquetes", "prendido_baja_productos_paquetes_cobis"));

        configuracion.set("prendidoBajaProductosTarjetaCredito", funcionalidadPrendida(contexto.idCobis(), "prendido_baja_productos_tc", "prendido_baja_productos_tc_cobis"));

        configuracion.set("prendidoCambioFormaPagoTC", funcionalidadPrendida(contexto.idCobis(), "prendido_cambio_forma_pago_tc", "prendido_cambio_forma_pago_tc_cobis"));

        configuracion.set("prendidoHBviejo", funcionalidadPrendida(contexto.idCobis(), "prendido_hb_viejo", "prendido_hb_viejo_cobis"));

        configuracion.set("prendidoTCAdicional", funcionalidadPrendida(contexto.idCobis(), "prendido_tc_adicional", "prendido_tc_adicional_cobis"));

        configuracion.set("prendidoAlertaNotificaciones", funcionalidadPrendida(contexto.idCobis(), "prendido_alerta_notificaciones", "prendido_alerta_notificaciones_cobis"));

        configuracion.set("prendidoHistorialActividades", funcionalidadPrendida(contexto.idCobis(), "prendido_historial_actividades", "prendido_historial_actividades_cobis"));

        configuracion.set("prendidoCuentaPrincipal", funcionalidadPrendida(contexto.idCobis(), "prendido_cuenta_principal", "prendido_cuenta_principal_cobis"));

        configuracion.set("prendidoCuentaPrincipalExterior", funcionalidadPrendida(contexto.idCobis(), "prendido_cuenta_principal_exterior", "prendido_cuenta_principal_exterior_cobis"));

        configuracion.set("prendidoTCAdicionalModificacionLimite", funcionalidadPrendida(contexto.idCobis(), "prendido_tc_adicional_modificacion_limite", "prendido_tc_adicional_modificacion_limite_cobis"));

        configuracion.set("prendidoRentaFinanciera", funcionalidadPrendida(contexto.idCobis(), "prendido_renta_financiera", "prendido_renta_financiera_cobis"));

        configuracion.set("prendidoPrestamoNSP", funcionalidadPrendida(contexto.idCobis(), "prendido_prestamo_nsp", "prendido_prestamo_nsp_cobis"));

        configuracion.set("prendidoCheques", funcionalidadPrendida(contexto.idCobis(), "prendido_cheques", "prendido_cheques_cobis"));

        configuracion.set("prendidoDetalleMovTarjetasComercio", funcionalidadPrendida(contexto.idCobis(), "prendido_detalle_mov_tarjetas_comercio", "prendido_detalle_mov_tarjetas_comercio_cobis"));

        configuracion.set("prendidoLicitacionesByma", funcionalidadPrendida(contexto.idCobis(), "prendido_licitaciones_byma", "prendido_licitaciones_byma_cobis"));

        configuracion.set("prendidoCotizacionesByma", funcionalidadPrendida(contexto.idCobis(), "prendido_cotizaciones_byma", "prendido_cotizaciones_byma_cobis"));

        configuracion.set("prendidoLogicaFechaTC", prendidoLogicaFechaTC);
        if (ConfigMB.string("prendidoLogicaFechaTC") != null && !prendidoLogicaFechaTC) {
            Set<String> cobisPrendidoLogicaFechaTC = Objeto.setOf(ConfigMB.string("prendidoLogicaFechaTC_cobis").split("_"));
            configuracion.set("prendidoLogicaFechaTC", prendidoLogicaFechaTC || cobisPrendidoLogicaFechaTC.contains(contexto.idCobis()) ? true : false);
        }

        // prendo la variable para manejo de error del 302.
        if (ConfigMB.string("prendido_manejo_302") == null || "".equals(ConfigMB.string("prendido_manejo_302"))) {
            configuracion.set("prendidoManejo302", true);
        } else {
            configuracion.set("prendidoManejo302", funcionalidadPrendida(contexto.idCobis(), "prendido_manejo_302", "prendido_manejo_302_cobis"));
        }

        Boolean prendidoMockRescate = "true".equals(ConfigMB.string("prendido_mock_rescate"));
        configuracion.set("prendidoMockRescate", prendidoMockRescate);
        if (ConfigMB.string("prendido_mock_rescate_cobis") != null && !prendidoMockRescate) {
            Set<String> cobis = Objeto.setOf(ConfigMB.string("prendido_mock_rescate_cobis").split("_"));
            configuracion.set("prendidoMockRescate", prendidoMockRescate || cobis.contains(contexto.idCobis()) ? true : false);
        }

        configuracion.set("concurrencia_pagos", concurrenciaPagos);
        configuracion.set("leyendaImpuestoCompraVentaDolares", ConfigMB.string("leyenda_impuesto_compra_venta_dolares"));
        configuracion.set("leyendaImpuestoCompraVentaDolaresTarjeta", ConfigMB.string("leyenda_impuesto_compra_venta_dolares_tarjeta"));
        configuracion.set("leyendaImpuestoCompraVentaDolaresTarjetaApagado", ConfigMB.string("leyenda_impuesto_compra_venta_dolares_tarjeta_apagado"));

        configuracion.set("mensajeTooltipFechaMov", ConfigMB.string("mensaje_tooltip_fecha_mov"));

        BigDecimal impuestoCompraVentaDolar = ConfigMB.bigDecimal("porcentaje_impuesto_compra_venta_dolares", new BigDecimal(0));
        configuracion.set("impuestoCompraVentaDolar", impuestoCompraVentaDolar);
        configuracion.set("impuestoCompraVentaDolarFormateado", Formateador.importe(impuestoCompraVentaDolar));

        configuracion.set("leyendaNormativo7072TransferenciaLink", ConfigMB.string("leyenda_normativo_7072_transferencia_link"));
        configuracion.set("leyendaNormativo7072TransferenciaIntraBH", ConfigMB.string("leyenda_normativo_7072_transferencia_intra_bh"));

        configuracion.set("montoMinimoPlanSueldoSinPaquete", Formateador.importe(ConfigMB.bigDecimal("monto_minimo_plan_sueldo_sin_paquete")));
        configuracion.set("metaBonificacionPlanSueldoFacilPackFormateado", Formateador.importe(ConfigMB.bigDecimal("meta_bonificacion_plan_sueldo_facil_pack")));
        configuracion.set("metaBonificacionPlanSueldoBuhoPackFormateado", Formateador.importe(ConfigMB.bigDecimal("meta_bonificacion_plan_sueldo_buho_pack")));
        configuracion.set("metaBonificacionPlanSueldoGoldPackFormateado", Formateador.importe(ConfigMB.bigDecimal("meta_bonificacion_plan_sueldo_gold_pack")));
        configuracion.set("metaBonificacionPlanSueldoPlatinumPackFormateado", Formateador.importe(ConfigMB.bigDecimal("meta_bonificacion_plan_sueldo_platinum_pack")));
        configuracion.set("metaBonificacionPlanSueldoBlackPackFormateado", Formateador.importe(ConfigMB.bigDecimal("meta_bonificacion_plan_sueldo_black_pack")));

        try {
            Boolean prendidoPickUp = false;
            prendidoPickUp |= ConfigMB.string("prendido_pickup").equals("empleado") ? contexto.persona().esEmpleado() : ConfigMB.bool("prendido_pickup");
            prendidoPickUp |= Objeto.linkedSetOf(ConfigMB.string("prendido_pickup_cobis").split("_")).contains(contexto.idCobis());
            configuracion.set("prendidoPickUp", prendidoPickUp);
        } catch (Exception e) {
            configuracion.set("prendidoPickUp", false);
        }

        configuracion.set("leyendaVentaBonosDolares", ConfigMB.string("leyenda_venta_bonos_dolares"));
        configuracion.set("leyendaVentaBonosPesos", ConfigMB.string("leyenda_venta_bonos_pesos"));
        configuracion.set("leyendaCompraBonosDolares", ConfigMB.string("leyenda_compra_bonos_dolares"));
        configuracion.set("leyendaCompraBonosPesos", ConfigMB.string("leyenda_compra_bonos_pesos"));

        configuracion.set("prendidoAumentoLimiteEmpleados", prendidoAumentoLimiteEmpleados);

        try {
            Boolean prendidoPickUp = false;
            prendidoPickUp |= ConfigMB.string("prendido_pickup").equals("empleado") ? contexto.persona().esEmpleado()
                    : ConfigMB.bool("prendido_pickup");
            prendidoPickUp |= Objeto.linkedSetOf(ConfigMB.string("prendido_pickup_cobis").split("_"))
                    .contains(contexto.idCobis());
            configuracion.set("prendidoPickUp", prendidoPickUp);
        } catch (Exception e) {
            configuracion.set("prendidoPickUp", false);
        }

        configuracion.set("leyendaVentaBonosDolares", ConfigMB.string("leyenda_venta_bonos_dolares"));
        configuracion.set("leyendaVentaBonosPesos", ConfigMB.string("leyenda_venta_bonos_pesos"));
        configuracion.set("leyendaCompraBonosDolares", ConfigMB.string("leyenda_compra_bonos_dolares"));
        configuracion.set("leyendaCompraBonosPesos", ConfigMB.string("leyenda_compra_bonos_pesos"));

        configuracion.set("prendidoAumentoLimiteEmpleados", prendidoAumentoLimiteEmpleados);

        // TODO GB DEPRECAR: INV-692 TODO03092417 se reemplazan por las 2 siguientes
        // xx_escalar...
        configuracion.set("invComisionMercadoVenta", ConfigMB.bigDecimal("inv_comision_mercado_venta").abs());
        configuracion.set("invComisionMercadoCompra", ConfigMB.bigDecimal("inv_comision_mercado_compra").abs());
        // Mandamos el escalar positivo a FE.
        configuracion.set("escMercAccionesCompra", ConfigMB.bigDecimal("escalar_precio_mercado_compra_acciones_bonos").abs());
        configuracion.set("escMercAccionesVenta", ConfigMB.bigDecimal("escalar_precio_mercado_venta_acciones_bonos").abs());

        configuracion.set("invComisionGeneralBonos", ConfigMB.bigDecimal("inv_comision_general_bonos"));
        configuracion.set("invComisionGeneralAccionesCedears", ConfigMB.bigDecimal("inv_comision_general_acciones_cedears"));

        Boolean prendidoScanDni = "true".equals(ConfigMB.string("prendido_scan_dni"));
        configuracion.set("prendidoScanDni", prendidoScanDni);
        if (ConfigMB.string("prendido_scan_dni_cobis") != null && !prendidoScanDni) {
            Set<String> cobisHabilitadoScan = Objeto.setOf(ConfigMB.string("prendido_scan_dni_cobis").split("_"));
            configuracion.set("prendidoScanDni", prendidoScanDni || cobisHabilitadoScan.contains(contexto.idCobis()));
        }

        Boolean prendidoRegistroDispositivo = "true".equals(ConfigMB.string("prendido_registro_dispositivo"));
        configuracion.set("prendidoRegistroDispositivo", prendidoRegistroDispositivo);
        if (!prendidoRegistroDispositivo)
            configuracion.set("prendidoRegistroDispositivo", !SqlRegistroDispositivo.obtenerForzadoRegistroDispositivo(idCobis).registros.isEmpty());

        configuracion.set("prendidoProfundidadMercado", funcionalidadPrendida(contexto.idCobis(), "prendido_profundidad_mercado", "prendido_profundidad_mercado_cobis"));

        configuracion.set("prendidoPlanV", prendidoPlanV);

        configuracion.set("prendidoPausadoTd", prendidoPausadoTd);

        configuracion.set("prendidoConsentimiento", funcionalidadPrendida(contexto.idCobis(), "prendidoConsentimiento", "mb_prendidoConsentimiento_cobis"));

        configuracion.set("prendidoNuevoFlujoPp", funcionalidadPrendida(contexto.idCobis(), "mb_prendido_nuevo_flujo_pp"));

        configuracion.set("prendidoBeneficiosEnUnSoloLugar", funcionalidadPrendida(contexto.idCobis(), "prendido_beneficios", "prendido_beneficios_cobis"));
        configuracion.set("prendidoSDK", funcionalidadPrendida(contexto.idCobis(), "prendido_sdk", "prendido_sdk_cobis"));
        configuracion.set("prendidoObPay", funcionalidadPrendida(contexto.idCobis(), "mb_prendido_ob_pay"));
        configuracion.set("sdkPagoModo", funcionalidadPrendida(contexto.idCobis(), "mb_sdk_pago_modo"));

        configuracion.set("prendidoPausadoTc", funcionalidadPrendida(contexto.idCobis(), "prendido_pausado_tc"));

        configuracion.set("montoMinimoOrdenExtraccion", ConfigMB.integer("monto_minimo_orden_extraccion", 500));
        configuracion.set("montoMaximoOrdenExtraccion", ConfigMB.integer("monto_maximo_orden_extraccion", 10000));

        configuracion.set("prendidoPlazoFijoLogroConsolidada", "true".equals(ConfigMB.string("prendido_plazo_fijo_logro_consolidada")));

        configuracion.set("prendidoCotizacionCompraVentaDolarMep", MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_cotizacion_mep_v3"));

        configuracion.set("prendidoSeguros", "true".equals(ConfigMB.string("prendido_seguros")));
        configuracion.set("prendidoSegurosAndroid", "true".equals(ConfigMB.string("prendido_seguros_android")));
        configuracion.set("prendidoSegurosMascotas", "true".equals(ConfigMB.string("prendido_seguros_mascotas")));
        configuracion.set("prendidoSegurosMascotasAndroid", "true".equals(ConfigMB.string("prendido_seguros_mascotas_android")));
        configuracion.set("prendidoSeguroAuto", "true".equals(ConfigMB.string("prendido_seguro_auto")));
        configuracion.set("prendidoSeguroAutoAndroid", "true".equals(ConfigMB.string("prendido_seguro_auto_android")));
        configuracion.set("prendidoSeguroMovilidadIos", "true".equals(ConfigMB.string("prendido_seguro_movilidad_ios")));

        configuracion.set("prendidoSeguroMovilidadAndroid", "true".equals(ConfigMB.string("prendido_seguro_movilidad_android")));
        configuracion.set("prendidoSeguroBienesMovilesIos", "true".equals(ConfigMB.string("prendido_seguro_bienes_moviles_ios")));
        configuracion.set("prendidoSeguroBienesMovilesAndroid", "true".equals(ConfigMB.string("prendido_seguro_bienes_moviles_android")));

        configuracion.set("prendidoSeguroHogarIos", "true".equals(ConfigMB.string("prendido_seguro_hogar_ios")));
        configuracion.set("prendidoSeguroHogarAndroid", "true".equals(ConfigMB.string("prendido_seguro_hogar_android")));

        configuracion.set("prendidoSeguroAPIos", "true".equals(ConfigMB.string("prendido_seguro_ap_ios")));
        configuracion.set("prendidoSeguroAPAndroid", "true".equals(ConfigMB.string("prendido_seguro_ap_android")));

        configuracion.set("prendidoSeguroHogarMascotasAndroid", "true".equals(ConfigMB.string("prendido_seguro_hogar_mascotas_android")));
        configuracion.set("prendidoSeguroHogarMascotasIos", "true".equals(ConfigMB.string("prendido_seguro_hogar_mascotas_ios")));

        configuracion.set("prendidoSeguroSaludSeniorAndroid", "true".equals(ConfigMB.string("prendido_seguro_salud_senior_android")));
        configuracion.set("prendidoSeguroSaludSeniorIos", "true".equals(ConfigMB.string("prendido_seguro_salud_senior_ios")));

        configuracion.set("prendidoSeguroAPSeniorAndroid", "true".equals(ConfigMB.string("prendido_seguro_ap_senior_android")));
        configuracion.set("prendidoSeguroAPSeniorIos", "true".equals(ConfigMB.string("prendido_seguro_ap_senior_ios")));

        configuracion.set("prendidoSeguroCaucionAndroid", "true".equals(ConfigMB.string("prendido_seguro_caucion_android")));
        configuracion.set("prendidoSeguroCaucionIos", "true".equals(ConfigMB.string("prendido_seguro_caucion_ios")));

        configuracion.set("cantidadCuotasSeguroCaucion", ConfigMB.integer("cantidad_cuotas_seguro_caucion"));
        configuracion.set("porcentajeDescuentoSeguroCaucion", ConfigMB.integer("porcetaje_descuento_seguro_caucion"));

        configuracion.set("prendidoPagoVep", "true".equals(ConfigMB.string("prendido_pago_vep")));

        configuracion.set("prendidoDebinRecurrente", funcionalidadPrendida(contexto.idCobis(), "prendido_debin_recurrente"));

        configuracion.set("prendidoObtenerCvvTc", funcionalidadPrendida(contexto.idCobis(), "prendido_obtener_cvv_tc"));

        configuracion.set("prendidoDebinProgramado", funcionalidadPrendida(contexto.idCobis(), "prendido_debin_programado"));

        configuracion.set("prendidoCaTdAltaOnline", funcionalidadPrendida("prendido_alta_ca_td_online"));
        configuracion.set("prendidoDataValidOtp", funcionalidadPrendida(contexto.idCobis(), "prendido_data_valid_otp"));

        configuracion.set("prendidoCuotificacion", funcionalidadPrendida("prendido_cuotificacion"));
        configuracion.set("importeMinimoCuotificacion", ConfigMB.bigDecimal("cuotificacion_importe_minimo", BigDecimal.ZERO));
        configuracion.set("importeMaximoCuotificacion", ConfigMB.bigDecimal("cuotificacion_importe_maximo", BigDecimal.ZERO));
        configuracion.set("prendidoCuotificacionTooltip", funcionalidadPrendida("prendido_cuotificacion_tooltip"));

        configuracion.set("prendidoLibreDeuda", funcionalidadPrendida(contexto.idCobis(), "prendido_libre_deuda"));
        configuracion.set("prendidoAgendaTransferencia", funcionalidadPrendida(contexto.idCobis(), "prendido_agenda_transferencia"));
        configuracion.set("prendidoModalTcv", funcionalidadPrendida(contexto.idCobis(), "prendido_modal_tcv"));
        configuracion.set("prendidoOfertaCajaAhorroDolar", funcionalidadPrendida(contexto.idCobis(), "prendido_oferta_caja_ahorro_dolar"));
        configuracion.set("prendidoGestionesv3", funcionalidadPrendida(contexto.idCobis(), "prendido_gestiones_v3"));

        return RespuestaMB.exito("configuracion", configuracion);
    }

    public static boolean funcionalidadPrendida(String idCobis, String flag, String flagCobis) {
        Boolean habilitado = "true".equals(ConfigMB.string(flag));
        if (ConfigMB.string(flagCobis) != null && !habilitado) {
            Set<String> cobisHabilitado = Objeto.setOf(ConfigMB.string(flagCobis).split("_"));
            habilitado = cobisHabilitado.contains(idCobis) ? true : false;
        }
        return habilitado;
    }

    public static boolean funcionalidadPrendida(String idCobis, String flag) {
        String flagCobis = flag + "_cobis";
        Boolean habilitado = "true".equals(ConfigMB.string(flag));
        if (ConfigMB.string(flagCobis) != null && !habilitado) {
            Set<String> cobisHabilitado = Objeto.setOf(ConfigMB.string(flagCobis).split("_"));
            habilitado = cobisHabilitado.contains(idCobis) ? true : false;
        }
        return habilitado;
    }

    public static RespuestaMB log(ContextoMB contexto) {
        if (ConfigMB.string("logs_ip_admitidas", "").contains(contexto.ip()) || !ConfigMB.esOpenShift()) {
            String idCobis = contexto.parametros.string("idCobis");

            if (Objeto.anyEmpty(idCobis)) {
                return RespuestaMB.parametrosIncorrectos();
            }

            RespuestaMB respuesta = new RespuestaMB();
            if (true) {
                SqlRequestMB request = SqlMB.request("SelectAuditorCompraVentaDolares", "hbs");
                request.sql = "SELECT * FROM [hbs].[dbo].[auditor_compra_venta_dolares] WHERE cobis = ?";
                request.add(idCobis);
                SqlResponseMB response = SqlMB.response(request);
                for (Objeto item : response.registros) {
                    item.set("momento", item.string("momento"));
                }
                respuesta.set("compraVentaDolares", response.registros);
            }
            if (true) {
                SqlRequestMB request = SqlMB.request("SelectAuditorPagoServicio", "hbs");
                request.sql = "SELECT * FROM [hbs].[dbo].[auditor_pago_servicio] WHERE cobis = ?";
                request.add(idCobis);
                SqlResponseMB response = SqlMB.response(request);
                for (Objeto item : response.registros) {
                    item.set("momento", item.string("momento"));
                }
                respuesta.set("pagoServicio", response.registros);
            }
            if (true) {
                SqlRequestMB request = SqlMB.request("SelectAuditorPagoTarjeta", "hbs");
                request.sql = "SELECT * FROM [hbs].[dbo].[auditor_pago_tarjeta] WHERE cobis = ?";
                request.add(idCobis);
                SqlResponseMB response = SqlMB.response(request);
                for (Objeto item : response.registros) {
                    item.set("momento", item.string("momento"));
                }
                respuesta.set("pagoTarjeta", response.registros);
            }
            if (true) {
                SqlRequestMB request = SqlMB.request("SelectAuditorPagoVep", "hbs");
                request.sql = "SELECT * FROM [hbs].[dbo].[auditor_pago_vep] WHERE cobis = ?";
                request.add(idCobis);
                SqlResponseMB response = SqlMB.response(request);
                for (Objeto item : response.registros) {
                    item.set("momento", item.string("momento"));
                }
                respuesta.set("pagosVep", response.registros);
            }
            if (true) {
                SqlRequestMB request = SqlMB.request("SelectAuditorTransferencia", "hbs");
                request.sql = "SELECT * FROM [hbs].[dbo].[auditor_transferencia] WHERE cobis = ?";
                request.add(idCobis);
                SqlResponseMB response = SqlMB.response(request);
                for (Objeto item : response.registros) {
                    item.set("momento", item.string("momento"));
                }
                respuesta.set("transferencias", response.registros);
            }
            if (true) {
                SqlRequestMB request = SqlMB.request("SelectAuditorTitulosValores", "hbs");
                request.sql = "SELECT * FROM [hbs].[dbo].[auditor_titulos_valores] WHERE cobis = ?";
                request.add(idCobis);
                SqlResponseMB response = SqlMB.response(request);
                for (Objeto item : response.registros) {
                    item.set("momento", item.string("momento"));
                }
                respuesta.set("titulosValores", response.registros);
            }
            contexto.setHeader("Content-Type", "application/json");
            return respuesta;
        }
        return null;
    }

    public static Object estadisticas(ContextoMB contexto) {
        if (!"true".equals(contexto.requestHeader("x-estadisticas"))) {
            return null;
        }
        Objeto respuesta = new Objeto();
        List<Map.Entry<String, LongAdder>> listaOrdenada = new ArrayList<>(CanalMobile.mapaContador.entrySet());
        listaOrdenada.sort((a, b) -> Long.compare(b.getValue().sum(), a.getValue().sum()));
        for (Map.Entry<String, LongAdder> item : listaOrdenada) {
            String endpoint = item.getKey();
            long cantidad = item.getValue().sum();
            respuesta.set(endpoint, cantidad);
        }
        contexto.responseHeader("Content-Type", "application/json");
        return new RespuestaMB().set("datos", respuesta);
    }

    public static Object eliminarEstadisticas(ContextoMB contexto) {
        CanalMobile.mapaContador.clear();
        contexto.responseHeader("Content-Type", "application/json");
        return new RespuestaMB();
    }

    public static Object estadisticasErrores(ContextoMB contexto) {
        if (!"true".equals(contexto.requestHeader("x-estadisticas"))) {
            return null;
        }

        Objeto respuesta = new Objeto();

        Set<String> todasLasClaves = new HashSet<>();
        todasLasClaves.addAll(CanalMobile.mapaContadorOK.keySet());
        todasLasClaves.addAll(CanalMobile.mapaContadorErrores.keySet());

        List<Map.Entry<String, Double>> listaErrores = new ArrayList<>();
        for (String clave : todasLasClaves) {
            long cantidadOK = CanalMobile.mapaContadorOK.getOrDefault(clave, new LongAdder()).sum();
            long cantidadErrores = CanalMobile.mapaContadorErrores.getOrDefault(clave, new LongAdder()).sum();
            long total = cantidadOK + cantidadErrores;
            double porcentajeError = (total > 0) ? ((double) cantidadErrores / total) * 100 : 0.0;
            listaErrores.add(new AbstractMap.SimpleEntry<>(clave, porcentajeError));
        }

        listaErrores.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));
        for (Map.Entry<String, Double> entry : listaErrores) {
            String clave = entry.getKey();
            Double porcentajeError = entry.getValue();
            respuesta.set(clave, porcentajeError);
        }
        contexto.responseHeader("Content-Type", "application/json");
        return new RespuestaMB().set("datos", respuesta);
    }

    public static boolean funcionalidadPrendida(String funcionalidad) {
        return "true".equals(ConfigMB.string(funcionalidad));
    }

}
