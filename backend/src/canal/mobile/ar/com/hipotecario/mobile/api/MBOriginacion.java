package ar.com.hipotecario.mobile.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.servicio.api.transmit.JourneyTransmitEnum;
import ar.com.hipotecario.backend.util.Transmit;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.bm.mb.AltaPrestamoMBBMBankProcess;
import ar.com.hipotecario.mobile.servicio.*;
import com.fasterxml.jackson.core.JsonParseException;
import org.apache.commons.lang3.StringUtils;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.helper.OriginacionHelper;
import ar.com.hipotecario.mobile.lib.Fecha;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.lib.Texto;
import ar.com.hipotecario.mobile.lib.Util;
import ar.com.hipotecario.mobile.negocio.Constantes;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.Integrante;
import ar.com.hipotecario.mobile.negocio.Prestamo;
import ar.com.hipotecario.mobile.negocio.ResolucionMotor;
import ar.com.hipotecario.mobile.negocio.SituacionLaboral;
import ar.com.hipotecario.mobile.negocio.Solicitud;
import ar.com.hipotecario.mobile.negocio.SolicitudPrestamo;
import ar.com.hipotecario.mobile.negocio.TarjetaCredito;
import ar.com.hipotecario.mobile.negocio.TarjetaDebito;

public class MBOriginacion {

    private static final String TOPE_SEPARADOR_COMPRA_ONLINE_5_ESPACIOS = "     ";
    private static final String TOPE_SEPARADOR_DETALLE_LEGAL_4_ESPACIOS = "    ";
    private static final String TOPE_SEPARADOR_DESCRIPCIONES_3_ESPACIOS = "   ";

    public static RespuestaMB crearPaquete(ContextoMB contexto) {
        if (ConfigMB.esProduccion()) {
            return RespuestaMB.error();
        }

        String idCobis = contexto.parametros.string("idCobis");

//		RestVenta.sanarCaso(contexto); // TODO: eliminar

        String codigoPaquete = contexto.parametros.string("codigoPaquete", null);
        String idCajaAhorroPesos = contexto.parametros.string("idCajaAhorroPesos", null);
        String idCajaAhorroDolares = contexto.parametros.string("idCajaAhorroDolares", null);
        String idCuentaCorriente = contexto.parametros.string("idCuentaCorriente", null);
        String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito", null);
        String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", null);
        Boolean generarPrestamo = contexto.parametros.bool("generarPrestamo", false);
        Boolean desistir = contexto.parametros.bool("desistir", true);

        contexto = new ContextoMB(idCobis, contexto.idSesion(), contexto.ip());

        // eliminar
        ApiResponseMB response = RestVenta.consultarSolicitudes(contexto);
        for (Objeto item : response.objetos("Datos")) {
            if (!item.string("Estado").equals("D")) {
                String id = item.string("IdSolicitud");
                if (desistir)
                    RestVenta.desistirSolicitud(contexto, id);
            }
        }

        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito, contexto.tarjetaCreditoTitular());

        // SOLICITUD
        ApiResponseMB solicitud = RestVenta.generarSolicitud(contexto);
        if (solicitud.hayError() || !solicitud.objetos("Errores").isEmpty()) {
            return new RespuestaMB().setEstado("ERROR").set("error", solicitud.objetos("Errores").get(0).string("MensajeCliente"));
        }
        String idSolicitud = solicitud.objetos("Datos").get(0).string("IdSolicitud");

        // INTEGRANTE
        ApiResponseMB integrante = RestVenta.generarIntegrante(contexto, idSolicitud);
        if (integrante.hayError() || !integrante.objetos("Errores").isEmpty()) {
            if (desistir)
                RestVenta.desistirSolicitud(contexto, idSolicitud);
            return new RespuestaMB().setEstado("ERROR").set("error", integrante.objetos("Errores").get(0).string("MensajeCliente"));
        }

        if (generarPrestamo) {
            TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();
            Objeto integrante2 = new Objeto();
            integrante2.set("numeroDocumentoTributario", contexto.persona().cuit());
            integrante2.set("idCobis", Long.valueOf(contexto.idCobis()));
            integrante2.set("numeroTarjetaDebito", tarjetaDebito != null ? tarjetaDebito.numero() : null);
            integrante2.set("rol", "D");
            ApiRequestMB request = ApiMB.request("VentasAgregarPrestamoPersonal", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/prestamoPersonal", contexto);
            request.headers.put("X-Handle", idSolicitud);
            request.path("SolicitudId", idSolicitud);
            request.body("Amortizacion", "01");
            request.body("TipoTasa", "01");
            request.body("DestinoBien", ConfigMB.esDesarrollo() ? "143" : "20");
            request.body("DescripcionDestinoFondos", "Libre destino");
            request.add("Integrantes", integrante2);
            request.body("Moneda", "80");
            // request.body("SubProducto", "21");
            request.body("TipoOperacion", "02");
            ApiMB.response(request, contexto.idCobis());
        }

        // PAQUETE CON TC EXISTENTE
        if (tarjetaCredito != null) {
            ApiResponseMB generarPaquete = RestVenta.generarPaquete(contexto, idSolicitud, codigoPaquete, idCajaAhorroPesos, idCajaAhorroDolares, idCuentaCorriente, idTarjetaDebito, idTarjetaCredito);
            if (generarPaquete.hayError() || !generarPaquete.objetos("Errores").isEmpty()) {
                if (desistir)
                    RestVenta.desistirSolicitud(contexto, idSolicitud);
                return new RespuestaMB().setEstado("ERROR").set("error", generarPaquete.objetos("Errores").get(0).string("MensajeCliente"));
            }
            if (!generarPaquete.objetos("Datos").get(0).string("Resolucion.ResolucionCodigo").equals("AV")) {
                ApiResponseMB evaluarSolicitud = RestVenta.evaluarSolicitud(contexto, idSolicitud);
                if (evaluarSolicitud.hayError() || !evaluarSolicitud.objetos("Errores").isEmpty()) {
                    if (desistir)
                        RestVenta.desistirSolicitud(contexto, idSolicitud);
                    return RespuestaMB.error().set("error", evaluarSolicitud.objetos("Errores").get(0).string("MensajeCliente"));
                }
                if (!evaluarSolicitud.objetos("Datos").get(0).string("ResolucionId").equals("AV")) {
                    String estado = "ERROR";
                    estado = evaluarSolicitud.objetos("Datos").get(0).string("ResolucionId").equals("AA") ? "AMARILLO" : estado;
                    estado = evaluarSolicitud.objetos("Datos").get(0).string("ResolucionId").equals("CT") ? "AMARILLO" : estado;
                    estado = evaluarSolicitud.objetos("Datos").get(0).string("ResolucionId").equals("RE") ? "ROJO" : estado;
                    if (desistir)
                        RestVenta.desistirSolicitud(contexto, idSolicitud);
                    return new RespuestaMB().setEstado(estado).set("error", evaluarSolicitud.objetos("Datos").get(0).string("Explicacion"));
                }
            }
        }

        // PAQUETE SIN TC EXISTENTE
        if (tarjetaCredito == null) {
            ApiResponseMB ofertaTarjetaCreditoPaquete = RestVenta.ofertaTarjetaCreditoPaquete(contexto, idSolicitud);
            if (ofertaTarjetaCreditoPaquete.hayError() || !ofertaTarjetaCreditoPaquete.objetos("Errores").isEmpty()) {
                if (desistir)
                    RestVenta.desistirSolicitud(contexto, idSolicitud);
                return new RespuestaMB().setEstado("ERROR").set("error", ofertaTarjetaCreditoPaquete.objetos("Errores").get(0).string("MensajeCliente"));
            }

            ApiResponseMB evaluarSolicitud = RestVenta.evaluarSolicitud(contexto, idSolicitud);
            if (evaluarSolicitud.hayError() || !evaluarSolicitud.objetos("Errores").isEmpty()) {
                if (desistir)
                    RestVenta.desistirSolicitud(contexto, idSolicitud);
                return RespuestaMB.error().set("error", evaluarSolicitud.objetos("Errores").get(0).string("MensajeCliente"));
            }
            if (!evaluarSolicitud.objetos("Datos").get(0).string("ResolucionId").equals("AV")) {
                String estado = "ERROR";
                estado = evaluarSolicitud.objetos("Datos").get(0).string("ResolucionId").equals("AA") ? "AMARILLO" : estado;
                estado = evaluarSolicitud.objetos("Datos").get(0).string("ResolucionId").equals("CT") ? "AMARILLO" : estado;
                estado = evaluarSolicitud.objetos("Datos").get(0).string("ResolucionId").equals("RE") ? "ROJO" : estado;
                if (desistir)
                    RestVenta.desistirSolicitud(contexto, idSolicitud);
                return new RespuestaMB().setEstado(estado).set("error", evaluarSolicitud.objetos("Datos").get(0).string("Explicacion"));
            }

            String idPaquete = ofertaTarjetaCreditoPaquete.objetos("Datos").get(0).string("Paquete.Id");
            String idTarjetaCreditoGenerada = ofertaTarjetaCreditoPaquete.objetos("Datos").get(0).string("Paquete.ProductosNuevos.TarjetaCredito.Id");
//			ApiResponse consultarTarjetaCredito = RestVenta.consultarTarjetaCredito(contexto, idSolicitud, idTarjetaCreditoGenerada);
//			String letraTarjetaCredito = consultarTarjetaCredito.objetos("Datos").get(0).string("Letra");
            RestVenta.actualizarPaquete(contexto, idSolicitud, codigoPaquete, idPaquete, idCajaAhorroPesos, idCajaAhorroDolares, idCuentaCorriente, idTarjetaDebito, idTarjetaCreditoGenerada);
            RestVenta.evaluarSolicitud(contexto, idSolicitud);
        }

        // FINALIZAR SOLICITUD
//		ApiResponse finalizacionSolicitud = RestVenta.finalizarSolicitud(contexto, idSolicitud);
//		if (finalizacionSolicitud.hayError() || !finalizacionSolicitud.objetos("Errores").isEmpty()) {
//			return new Respuesta().setEstado("ERROR").set("error", finalizacionSolicitud.objetos("Errores").get(0).string("MensajeCliente"));
//		}

        if (desistir)
            RestVenta.desistirSolicitud(contexto, idSolicitud);

        return RespuestaMB.exito();
    }

    public static RespuestaMB consultarOfertaPaquetes(ContextoMB contexto) {
        if (ConfigMB.esOpenShift()) {
            return RespuestaMB.error();
        }

        if (contexto.persona().esEmpleado()) {
            return RespuestaMB.estado("ES_EMPLEADO");
        }

        TarjetaCredito tarjetaCredito = contexto.tarjetaCreditoTitular();
        if (tarjetaCredito == null) {
            return RespuestaMB.error(); // TODO: Hay que hacer la lógica si es que no tiene tarjeta
        }

        String letra = tarjetaCredito.idTipo();
        ApiResponseMB responseTarjeta = TarjetaCreditoService.consultaTarjetaCreditoCache(contexto, tarjetaCredito.numero());
        if (responseTarjeta.hayError()) {
            return RespuestaMB.error();
        }
        BigDecimal limite = responseTarjeta.objetos().get(0).bigDecimal("limiteCompraAcordado");
        if (limite == null) {
            return RespuestaMB.error();
        }
        // String id= tarjetaCredito.id();

        RespuestaMB respuesta = new RespuestaMB();
        // DESDE - CON ESTO ME TRAIGO LA OFERTA DE LA TARJETA DE CREDITO QUE NECESITA
        // TIENE EL USUARIO
        Objeto ofertaPaqueteTcActual = ConsultarOfertaPaqueteLetra(contexto, letra);
        if (ofertaPaqueteTcActual != null) {
            ofertaPaqueteTcActual.set("ofertaTarjetaActual", true);
            respuesta.add("paquetes", ofertaPaqueteTcActual);
        }
        // HASTA - CON ESTO ME TRAIGO LA OFERTA DE LA TARJETA DE CREDITO QUE NECESITA
        // TIENE EL USUARIO

        // DESDE - CON ESTO BUSCO LA MEJOR OFERTA PARA EL USUARIO
        String idSegmento = "";
        ApiResponseMB responseSegmentacion = RestPersona.segmentacion(contexto);
        if (responseSegmentacion.hayError()) {
            return RespuestaMB.error();
        }
        idSegmento = responseSegmentacion.objetos().get(0).string("idSegmentoRenta"); // TODO: va este valor idRxReglaSegmentoRenta!!!!! pasa que lo hardcodie con
        // otro para que eli pueda probar
        if ("".equals(idSegmento)) {
            return RespuestaMB.error();
        }
        ApiResponseMB responseTiposTarjetas = TarjetaCreditoService.consultaTiposTarjeta(contexto, limite.intValue(), idSegmento);
        if (responseTiposTarjetas.hayError()) {
            return RespuestaMB.error();
        }
        String mejorLetra = responseTiposTarjetas.objetos().get(0).string("tarjeTipo");

        if (mejorLetra != null && !mejorLetra.equals(letra)) {

            Objeto mejorOfertaPaquete = ConsultarOfertaPaqueteLetra(contexto, mejorLetra);
            if (mejorOfertaPaquete != null) {
                mejorOfertaPaquete.set("mejorOferta", true);
                respuesta.add("paquetes", mejorOfertaPaquete);
            }

        }
        // HASTA - CON ESTO BUSCO LA MEJOR OFERTA PARA EL USUARIO

        return respuesta;
    }

    private static Objeto ConsultarOfertaPaqueteLetra(ContextoMB contexto, String letra) {
        ApiRequestMB request = ApiMB.request("ConsultarOfertasPaquetes", "paquetes", "GET", "/v1/infoParametrias/paquetes/consolidados", contexto);
        request.header("x-usuario", ConfigMB.string("configuracion_usuario"));
        request.query("canal", "HB");
        request.query("empleado", contexto.idCobis());
        request.query("letraTarjeta", letra);
        request.query("numeroSucursal", "6"); // TODO: preguntar qué sucursal va

        boolean esClientePackSueldo = contexto.persona().esPackSueldo();
        boolean esClienteEmprendedor = contexto.persona().esPersonaJuridica();

        ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
        if (response.hayError()) {
            throw new RuntimeException();
        }
        Objeto paquete = null;
        boolean sirveOferta = true;
        for (Objeto itemPaquete : response.objetos()) {
            paquete = new Objeto();
            boolean esPaqueteEmprendedor = false;
            boolean esPaquetePlanSueldo = false;

            paquete.set("codigo", itemPaquete.string("codigo"));
            paquete.set("descripcion", RestCatalogo.mapaPaquetes().get(itemPaquete.integer("codigo")));
            paquete.set("mostrarBuhoPuntos", itemPaquete.string("programaRecompensa").contains("1"));
            paquete.set("mostrarAerolineaPlus", itemPaquete.string("programaRecompensa").contains("2"));
            paquete.set("montoAerolineasPlus", ConfigMB.bigDecimal("monto_aerolineas_plus"));
            paquete.set("montoAerolineasPlusFormateado", Formateador.importe(ConfigMB.bigDecimal("monto_aerolineas_plus")));

            paquete.set("bonificadoBuhoPuntos", itemPaquete.string("programaRecompensaCanal").contains("1"));
            paquete.set("bonificadoAerolineaPlus", itemPaquete.string("programaRecompensaCanal").contains("2"));

            sirveOferta = true; // La oferta puede llegar a no servir porque es plan sueldo y no corresponde la
            // caja de ahorro en pesos
            // o es emprendedor pero en la descripcion del paquete no dice "emprendedor"
            // o no es ni emprendedor, ni plan sueldo entonces no tengo que contemplar
            // ninguno de los casos anteriores

            // TODO: Tengo que refactorizar esto, lo ideal es hacer una función que
            // determine que tipo de paquete es
            // Si es emprendedor, paquete sueldo, o resto
            if (itemPaquete.string("descripcion").toLowerCase().contains("emprendedor"))
                esPaqueteEmprendedor = true;

            if (esClienteEmprendedor && !esPaqueteEmprendedor) {
                // si el cliente es emprendedor, pero el paquete NO es emprendedor lo que hago
                // es
                // ignoro este paquete y busco otro
                sirveOferta = false;
                paquete = null;
                continue;
            }
            if (!esClienteEmprendedor && esPaqueteEmprendedor) {
                // si el cliente NO es emprendedor, pero el paquete SI lo es, ignoro este
                // paquete y busco otro
                sirveOferta = false;
                paquete = null;
                continue;
            }

            /* DESDE - COSTO */
            BigDecimal costo = new BigDecimal(0);
            if (!itemPaquete.objetos("costos").isEmpty()) {
                costo = itemPaquete.objetos("costos").get(0).bigDecimal("sinIVA");
            }
            /* HASTA - COSTO */

            /* DESDE - BONIFICACIONES */
            Integer cantidadMesesBonificado = null;
            BigDecimal porcentajeBonificado = null;
            for (Objeto bonificacion : itemPaquete.objetos("bonificaciones")) {
                if (bonificacion.string("tipo").contains("BIENVENIDA") && bonificacion.string("bonificacion").split(" ").length >= 6) {
                    cantidadMesesBonificado = Integer.parseInt(bonificacion.string("bonificacion").split(" ")[6]);
                    String porcentajeBonificadoAux = bonificacion.string("bonificacion").split(" ")[4];
                    if (porcentajeBonificadoAux.length() > 0) {
                        porcentajeBonificadoAux = porcentajeBonificadoAux.substring(0, porcentajeBonificadoAux.length() - 1); // con esto le saco el ultimo caracter que es el porcentaje
                        porcentajeBonificado = new BigDecimal(porcentajeBonificadoAux);
                    }
                }
            }

            // TODO: borrar esto que es para que eli tenga caso
            String dummy = "BONIFICACION DE BIENVENIDA DEL 100% DURANTE 3 MESES";
            cantidadMesesBonificado = Integer.valueOf(dummy.split(" ")[6]);
            String porcentajeBonificadoAux = dummy.split(" ")[4];
            if (porcentajeBonificadoAux.length() > 0) {
                porcentajeBonificadoAux = porcentajeBonificadoAux.substring(0, porcentajeBonificadoAux.length() - 1); // con esto le saco el ultimo caracter que es el porcentaje
                porcentajeBonificado = new BigDecimal(porcentajeBonificadoAux);
            }
            /* HASTA - BONIFICACIONES */

            // esto no tiene sentido que lo hayan hecho asi, pero lo hicieron.
            // Agregaron un array de paquetes adentro del paquete. Pregunté por esto
            // y me dijeron que siempre viene uno. Así que lo que hago es tomar el primero
            // siempre
            if (itemPaquete.objetos("productos").isEmpty()) {
                return RespuestaMB.error();
            }

            for (Objeto itemProducto : itemPaquete.objetos("productos").get(0).objetos("productos")) {
                String descripcion = "";
                if ("CTE".equals(itemProducto.string("tipo"))) {
                    descripcion = "Cuenta Corriente";
                } else if ("AHO".equals(itemProducto.string("tipo"))) {
                    if ("80".equals(itemProducto.objeto("moneda").string("id"))) {
                        esPaquetePlanSueldo = false;
                        if ("K".equals(itemProducto.string("categoriaDefault")) || "EV".equals(itemProducto.string("categoriaDefault")) || "M".equals(itemProducto.string("categoriaDefault")) || "D".equals(itemProducto.string("categoriaDefault"))) // TODO: este no va!!! es solo si es K, EV, M. Lo dejé para que eli tenga casos
                        {
                            esPaquetePlanSueldo = true;
                        }
                        if (esClientePackSueldo && !esPaquetePlanSueldo) {
                            sirveOferta = false;
                            break;
                        }
                        if (!esClientePackSueldo && esPaquetePlanSueldo) {
                            sirveOferta = false;
                            break;
                        }
                    }
                    descripcion = "Caja de Ahorros";
                } else if ("ATM".equals(itemProducto.string("tipo"))) {
                    descripcion = "Tarjeta de débito";
                } else if ("SMA".equals(itemProducto.string("tipo"))) {
                    if (!"".equals(itemProducto.string("categoriaDefault")))
                        descripcion = itemProducto.string("descripcion");
                    else
                        continue; // no es tarjeta de crédito default, no la muestro
                } else {
                    continue; // no es ningún producto que pueda mostrar
                }

                Objeto producto = new Objeto();
                producto.set("idMoneda", itemProducto.objeto("moneda").string("id"));
                producto.set("simboloMoneda", Formateador.simboloMoneda(itemProducto.objeto("moneda").string("id")));
                producto.set("moneda", Formateador.moneda(itemProducto.objeto("moneda").string("id")));
                producto.set("tipo", itemProducto.string("tipo"));
                producto.set("descripcion", descripcion);
                paquete.add("productos", producto);
            }

            paquete.set("tipoCliente", esClientePackSueldo ? "PACK SUELDO" : (esClienteEmprendedor ? "EMPRENDEDOR" : "RESTO"));
            paquete.set("tipoPaquete", esPaquetePlanSueldo ? "PACK SUELDO" : (esPaqueteEmprendedor ? "EMPRENDEDOR" : "RESTO"));

            if (cantidadMesesBonificado != null)
                paquete.set("cantidadMesesBonificado", cantidadMesesBonificado);
            paquete.set("costoMantenimientoMensual", costo);
            paquete.set("costoMantenimientoMensualFormateado", Formateador.importe(costo));
            if (porcentajeBonificado != null)
                paquete.set("mantenimientoPorcentajeBonificado", porcentajeBonificado);
            if (porcentajeBonificado != null)
                paquete.set("mantenimientoPorcentajeBonificadoFormateado", Formateador.importe(porcentajeBonificado));

            if (sirveOferta)
                break;
            else
                paquete = null;
        }

        SqlResponseMB sqlBeneficios = RestCatalogo.paquetesBeneficios(contexto, paquete.string("codigo"));
        Objeto beneficios = new Objeto();
        for (Objeto registro : sqlBeneficios.registros) {
            Objeto item = new Objeto();
            item.set("codigo", registro.string("numero_paquete"));
            item.set("descripcionBeneficio", registro.string("desc_beneficio"));
            item.set("descripcionBeneficioHtml", registro.string("desc_beneficio_html"));
            beneficios.add(item);
        }
        paquete.set("beneficios", beneficios);
        return paquete;
    }

    public static RespuestaMB desistirSolicitud(ContextoMB contexto) {
        String idSolicitud = contexto.parametros.string("idSolicitud");
        ApiResponseMB response = RestVenta.desistirSolicitud(contexto, idSolicitud);
        if (response.hayError()) {
            return RespuestaMB.error();
        }
        return new RespuestaMB();
    }

    public static RespuestaMB consultarSolicitudes(ContextoMB contexto) {
        ApiResponseMB response = RestVenta.consultarSolicitudes(contexto);
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        RespuestaMB respuesta = new RespuestaMB();
        Objeto datos = new Objeto();
        for (Objeto datoSolicitud : response.objetos("Datos")) {
            Objeto dato = new Objeto();
            if ("O".equals(datoSolicitud.string("Estado"))) { // Si es una solicitud desestimada no la muestro
                dato.set("id", datoSolicitud.string("Id"));
                dato.set("estado", datoSolicitud.string("Estado"));
                dato.set("fechaAlta", datoSolicitud.string("FechaAlta"));

                Objeto productos = new Objeto();
                for (Objeto productoSolicitud : datoSolicitud.objetos("Productos")) {
                    Objeto producto = new Objeto();
                    producto.set("tipoProducto", productoSolicitud.string("tipoProducto"));
                    producto.set("producto", productoSolicitud.string("Producto"));
                    producto.set("idPaqueteProducto", productoSolicitud.string("IdPaqueteProductos"));
                    producto.set("id", productoSolicitud.string("Id"));
                    productos.add(producto);
                }
                dato.add("productos", productos);
                datos.add(dato);

            }
        }
        respuesta.set("datos", datos);
        return respuesta;
    }

    public static RespuestaMB mejorarPrimerOfertaPrestamo(ContextoMB contexto) {
        contexto.parametros.set("mejorarOferta", true);
        contexto.parametros.set("forzarGeneracionOfertaNueva", true);
        String vienePrimerOferta = contexto.sesion().cache("primeraOferta");

        if (vienePrimerOferta == null || !ConfigMB.bool("prendido_canal_amarillo_pp")) {
            return RespuestaMB.estado("OPERACION_INHABILITADA_CANAL_AMARILLO");
        }

        if (tieneParametrosCanalAmarillo(contexto)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        return solicitarPrimerOfertaPrestamo(contexto);
    }

    public static RespuestaMB solicitarPrimerOfertaPrestamo(ContextoMB contexto) {
        Boolean forzarGeneracionOfertaNueva = contexto.parametros.bool("forzarGeneracionOfertaNueva", false);
        Boolean esAdelanto = contexto.parametros.bool("esAdelanto", false);
        String idSolicitud = contexto.parametros.string("idSolicitud", "");
        Boolean esAptoRecurrencia = !tieneAdelantoRecurrente(contexto) && ConfigMB.bool("prendido_adelanto_recurrente", false);
        String idPrestamo = "";
        Boolean tieneSeguro = false;
        Boolean primerOferta = false; // es para saber si es la primera vez que se le oferta el préstamo o estoy
        // tomando otro prestamo

        Boolean esCuotificacion = contexto.parametros.bigDecimal("montoCuotificacion") != null;
        contexto.sesion().delCache("primeraOferta");

        if (!ConfigMB.bool("prendido_alta_prestamos")) {
            return RespuestaMB.estado("OPERACION_INHABILITADA");
        }

        try {
            Solicitud solicitud = null;
            String finalIdSolicitud = idSolicitud;
            if ("".equals(idSolicitud) && forzarGeneracionOfertaNueva) {
                if (ConfigMB.esOpenShift()) {
                    RestOmnicanalidad.limpiarSolicitudes(contexto, ConfigMB.longer("solicitud_dias_vigente", 30L), true, false, false);
                }

                solicitud = Solicitud.generarSolicitud(contexto);
                idSolicitud = solicitud.Id;
                String cuitConyuge = contexto.persona().idEstadoCivil().equals("C") ? RestPersona.cuitConyuge(contexto) : null;
                String cuit = contexto.persona().cuit();
                solicitud = solicitud.generarIntegrantes(contexto, cuit, cuitConyuge);

                String tarjetaDebito = contexto.tarjetaDebitoPorDefecto() != null ? contexto.tarjetaDebitoPorDefecto().numero() : null;
                SolicitudPrestamo solicitudPrestamo = solicitud.generarPrestamoPersonal(contexto, tarjetaDebito);
                idPrestamo = solicitudPrestamo.Id;

                if (!esAdelanto) {
                    String seguroDesempleo = OriginacionHelper.validaSeguroDesempleoSegunEdad(contexto, idSolicitud, idPrestamo);
                    if ("false".equalsIgnoreCase(seguroDesempleo)) {
                        Solicitud.logOriginacion(contexto, idSolicitud, "validaSeguroDesempleoSegunEdad", null, "ERROR_POST_SEGURO_DESEMPLEO");
                        return RespuestaMB.estado("ERROR_POST_SEGURO_DESEMPLEO");
                    }
                    tieneSeguro = "true".equals(seguroDesempleo);
                }

                primerOferta = true;
            }

            if (idPrestamo == null || "".equals(idPrestamo)) {
                Solicitud.logOriginacion(contexto, idSolicitud, "statusPrestamo", null, "ERROR_PRESTAMO_VACIO");
                return RespuestaMB.estado("ERROR_PRESTAMO_VACIO");
            }

            contexto.parametros.set("esCuotificacion", esCuotificacion);

            // EVALUAR SOLICITUD
            RespuestaMB respuesta = new RespuestaMB();

            ResolucionMotor evaluacionSolicitud = solicitud.ejecutarMotor(contexto);
            if (esCuotificacion)
                new Futuro<>(() -> MBOmnicanalidad.insertarLogMotor(contexto, finalIdSolicitud, evaluacionSolicitud.ResolucionId, evaluacionSolicitud.Explicacion));

            if (evaluacionSolicitud.esAprobadoAmarillo()) {
                if (esCuotificacion)
                    new Futuro<>(() -> RestVenta.desistirSolicitud(contexto, finalIdSolicitud));
                return casoCanalAmarillo(contexto, solicitud, idPrestamo, tieneSeguro, evaluacionSolicitud, true, null);
            }
            if (evaluacionSolicitud.esAmarillo() || evaluacionSolicitud.esSAmarillo()) {
                if (esCuotificacion)
                    new Futuro<>(() -> RestVenta.desistirSolicitud(contexto, finalIdSolicitud));
                Solicitud.logOriginacion(contexto, idSolicitud, "ejecutarMotor", null, "AMARILLO");
                respuesta.set("color", "AMARILLO");
                return respuesta;
            }
            if (evaluacionSolicitud.esRojo()) {
                if (esCuotificacion)
                    new Futuro<>(() -> RestVenta.desistirSolicitud(contexto, finalIdSolicitud));
                Solicitud.logOriginacion(contexto, idSolicitud, "ejecutarMotor", null, "ROJO");
                respuesta.set("color", "ROJO");
                return respuesta;
            }

            if (esCuotificacion) {
                SqlMovimientosCuotificacion.delete(contexto.idCobis());

                contexto.parametros.objeto("idsMovimientos").toList().forEach(m -> {
                    SqlMovimientosCuotificacion.insertar(contexto.idCobis(), m.toString(), false);
                });
            }

            contexto.parametros.set("idSolicitud", idSolicitud);
            contexto.parametros.set("idPrestamo", idPrestamo);
            contexto.parametros.set("tieneSeguroDesempleo", tieneSeguro);
            if (primerOferta) { // si le ofertan el prestamo por 1ra vez, tengo que guardarme el monto del
                // prestamo, ya que es el maximo que el usuario puede pedir
                contexto.parametros.set("primeraOferta", true);
                contexto.sesion().setCache("primeraOferta", "true");
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
            respuesta.set("mostrarBotonMejorarOferta", !esAdelanto && ConfigMB.bool("prendido_canal_amarillo_pp"));
            Solicitud.logOriginacion(contexto, idSolicitud, "FIN_solicitarPrimerOfertaPrestamo", null, respuesta.toJson());
            return respuesta;
        } catch (Exception e) {
            if (e instanceof JsonParseException)
                return RespuestaMB.timeOut();
            return RespuestaMB.error();
        }
    }

    private static RespuestaMB casoCanalAmarillo(ContextoMB contexto, Solicitud solicitud, String idPrestamo, Boolean tieneSeguro, ResolucionMotor evaluacionSolicitud, Boolean primeraOferta, Cuenta cuenta) {
        Boolean mejorarOferta = contexto.parametros.bool("mejorarOferta", false);
        Boolean esAdelanto = contexto.parametros.bool("esAdelanto", false);
        RespuestaMB respuesta = new RespuestaMB();
        if (mejorarOferta && ConfigMB.bool("prendido_canal_amarillo_pp")) {
            contexto.parametros.set("idSolicitud", solicitud.IdSolicitud);
            contexto.parametros.set("idPrestamo", idPrestamo);
            contexto.parametros.set("tieneSeguroDesempleo", tieneSeguro);
            contexto.parametros.set("primeraOferta", primeraOferta);
            respuesta = consultarPrestamo(contexto);
            respuesta.set("declaracionCuota", Constantes.TEXTO_DECLARACION_CUOTA);
            respuesta.set("legales", Constantes.TEXTO_LEGALES);

            if (!primeraOferta) {
                RespuestaMB cambioMonto = OriginacionHelper.validaMontoModifSolicitud(contexto, solicitud, cuenta);
                if (cambioMonto.hayError()) {
                    Solicitud.logOriginacion(contexto, solicitud.IdSolicitud, "simular_validaMontoModifSolicitud", null, cambioMonto.toJson());
                    return cambioMonto;
                }
                guardaMejoraOferta(contexto, solicitud.IdSolicitud);
            }
        }
        Solicitud.logOriginacion(contexto, solicitud.IdSolicitud, "ejecutarMotor", null, "AMARILLO");
        respuesta.set("color", "AMARILLO");
        respuesta.set("explicacion", evaluacionSolicitud.ResolucionDesc);
        respuesta.set("mostrarBotonMejorarOferta", !esAdelanto && ConfigMB.bool("prendido_canal_amarillo_pp"));
        return respuesta;
    }

    private static Boolean guardaMejoraOferta(ContextoMB contexto, String idSolicitud) {
        String idSituacionLaboral = contexto.parametros.string("idSituacionLaboral");
        BigDecimal ingresoNeto = contexto.parametros.bigDecimal("ingresoNeto");
        String cuitEmpleador = (idSituacionLaboral.equals("11")) ? Solicitud.cuitAnses : contexto.parametros.string("cuit", "");
        Date fecha = contexto.parametros.date("fecha", null);
        String categoriaMonotributista = contexto.parametros.string("letra", "").toUpperCase();

        SituacionLaboral situacionActual = SituacionLaboral.situacionLaboralPrincipal(contexto);

        if (Objeto.empty(ingresoNeto) && categoriaMonotributista != "") {
            ingresoNeto = new BigDecimal(MBCatalogo.montoMonotributo(contexto, categoriaMonotributista));
        }

        return SqlPrestamos.guardaSolicitudCanalAmarillo(contexto, idSolicitud, situacionActual.id, idSituacionLaboral, categoriaMonotributista, ingresoNeto, fecha, cuitEmpleador);
    }

    public static RespuestaMB consultarPrestamo(ContextoMB contexto) {
        String idSolicitud = contexto.parametros.string("idSolicitud");
        String idPrestamo = contexto.parametros.string("idPrestamo");
        Boolean esAdelanto = contexto.parametros.bool("esAdelanto", false);
        Boolean mejorarOferta = contexto.parametros.bool("mejorarOferta", false);
        Boolean tieneSeguroDesempleo = contexto.parametros.bool("tieneSeguroDesempleo", false);
        Boolean buscarSiTieneSeguro = contexto.parametros.bool("buscarSiTieneSeguro", false);
        Boolean primeraOferta = contexto.parametros.bool("primeraOferta", false);

        String idSeguroDesempleo = "";

        if (tieneSeguroDesempleo)
            buscarSiTieneSeguro = true; // si tiene seguro de desempleo no tengo alternativa que buscar el id y fijarme
        // cuál es la prima.

        if (buscarSiTieneSeguro) {
            tieneSeguroDesempleo = false;
            ApiResponseMB solicitudGet = RestVenta.consultarSolicitudes(contexto);
            if (solicitudGet.hayError()) {
                Solicitud.logOriginacion(contexto, idSolicitud, "consultarSolicitudes", solicitudGet, "");
                return RespuestaMB.error();
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
            ApiResponseMB seguroDesempleoGet = RestVenta.consultarMontoSeguroDesempleo(contexto, idSolicitud, idSeguroDesempleo);
            if (seguroDesempleoGet.hayError()) {
                Solicitud.logOriginacion(contexto, idSolicitud, "consultarMontoSeguroDesempleo", seguroDesempleoGet, "");
                return RespuestaMB.error();
            }
            if (!seguroDesempleoGet.objetos("Datos").isEmpty()) {
                primaSeguro = new BigDecimal(seguroDesempleoGet.objetos("Datos").get(0).string("Valor1").replace(",", "."));
                sumaAsegurada = new BigDecimal(seguroDesempleoGet.objetos("Datos").get(0).string("Valor2").replace(",", "."));
            }
        }

        RespuestaMB respuesta = new RespuestaMB();
        BigDecimal montoPrimeraOferta = new BigDecimal(0);
        ApiResponseMB prestamoGet = RestVenta.consultarSolicitudPrestamoPersonal(contexto, idSolicitud, idPrestamo);
        if (prestamoGet.hayError()) {
            Solicitud.logOriginacion(contexto, idSolicitud, "consultarSolicitudPrestamoPersonal", prestamoGet, "");
            return RespuestaMB.error();
        }

        if (prestamoGet.objetos("Datos") != null && prestamoGet.objetos("Datos").size() > 0) {
            Objeto item = prestamoGet.objetos("Datos").get(0);
            respuesta.set("idSolicitud", idSolicitud);
            respuesta.set("idPrestamo", idPrestamo);

            if (primeraOferta) {
                if (mejorarOferta) {
                    montoPrimeraOferta = contexto.sesion().montoMaximoPrestamo() != null ? contexto.sesion().montoMaximoPrestamo() : new BigDecimal(0);
                }
                contexto.sesion().setMontoMaximoPrestamo(item.bigDecimal("MontoAprobado"));
            }

            if (contexto.sesion().montoMaximoPrestamo() != null) {
                respuesta.set("montoMaximo", contexto.sesion().montoMaximoPrestamo());
                respuesta.set("montoMaximoFormateado", Formateador.importe(contexto.sesion().montoMaximoPrestamo()));
            } else {
                RespuestaMB modalPP = MBPrestamo.ofertaPreAprobadaPP(contexto);
                BigDecimal montoMaximo = modalPP != null && modalPP.existe("montoPP") && modalPP.string("montoPP") != "" ? modalPP.bigDecimal("montoPP") : item.bigDecimal("MontoAprobado");
                respuesta.set("montoMaximo", montoMaximo);
                respuesta.set("montoMaximoFormateado", Formateador.importe(montoMaximo));
                contexto.sesion().setMontoMaximoPrestamo(montoMaximo);
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
            respuesta.set("montoMinimo", esAdelanto ? ConfigMB.bigDecimal("monto_minimo_PP_adelanto") : ConfigMB.bigDecimal("monto_minimo_prestamo_personal"));
            respuesta.set("montoMinimoFormateado", esAdelanto ? Formateador.importe(ConfigMB.bigDecimal("monto_minimo_PP_adelanto")) : Formateador.importe(ConfigMB.bigDecimal("monto_minimo_prestamo_personal")));
            respuesta.set("plazo", item.integer("Plazo"));
            respuesta.set("plazoDescripcion", esAdelanto && item.integer("Plazo") == 1 ? "Única Cuota" : null);
            respuesta.set("plazoMaximo", esAdelanto && item.integer("Plazo") == 1 ? 1 : ConfigMB.integer("plazo_maximo_prestamo_personal"));
            respuesta.set("plazoMinimo", esAdelanto && item.integer("Plazo") == 1 ? 1 : ConfigMB.integer("plazo_minimo_prestamo_personal"));
            if (item.integer("DiaCobro") != null) {
                respuesta.set("diaCobro", item.integer("DiaCobro"));
            } else {
                respuesta.set("diaCobro", 1);
            }
            respuesta.set("seguroDesempleo", esAdelanto ? false : tieneSeguroDesempleo);
            respuesta.set("importeCuotaPura", item.bigDecimal("importeCuota"));
            respuesta.set("importeCuotaPuraFormateado", Formateador.importe(item.bigDecimal("importeCuota")));
            respuesta.set("importePrimeraCuota", item.bigDecimal("importeCuota").add(importeSeguro));
            respuesta.set("importePrimeraCuotaFormateado", Formateador.importe(item.bigDecimal("importeCuota").add(importeSeguro)));
            respuesta.set("importeSeguro", importeSeguro);
            respuesta.set("importeSeguroFormateado", Formateador.importe(importeSeguro));
            respuesta.set("cft", item.bigDecimal("CFT"));
            respuesta.set("cftFormateado", Formateador.importe(item.bigDecimal("CFT")));
            respuesta.set("tna", item.bigDecimal("Tasa"));
            respuesta.set("tnaFormateado", Formateador.importe(item.bigDecimal("Tasa")));
            respuesta.set("jubilado", contexto.esJubilado());
            respuesta.set("ofrecerSeguroDesempleo", esAdelanto ? false : contexto.persona().edad() < ConfigMB.integer("edad_limite_seguro_desempleo") || !contexto.esJubilado());

            Boolean vieneDePrimeraOferta = mejorarOferta && monto.compareTo(montoPrimeraOferta) > 0 && (monto.subtract(montoPrimeraOferta).intValue() >= ConfigMB.integer("monto_tope_mejorado_canal_amarillo"));
            Boolean vieneDelSimular = mejorarOferta && monto.compareTo(montoPrimeraOferta) == 0 && !primeraOferta;
            if (vieneDePrimeraOferta || vieneDelSimular) {
                respuesta.set("ofertaMejorada", true);
            }

        } else {
            Solicitud.logOriginacion(contexto, idSolicitud, "consultarPrestamo", prestamoGet, "");
            return RespuestaMB.error();
        }

        Solicitud.logOriginacion(contexto, idSolicitud, "FIN_consultarPrestamo", null, respuesta.toJson());
        return respuesta;
    }

    public static RespuestaMB simularOfertaPrestamo(ContextoMB contexto) {
        String idSolicitud = contexto.parametros.string("idSolicitud");
        String idPrestamo = contexto.parametros.string("idPrestamo");
        BigDecimal monto = contexto.parametros.bigDecimal("monto");
        String idCuenta = contexto.parametros.string("idCuenta", null);
        Integer plazo = contexto.parametros.integer("plazo");
        Integer diaVencimiento = contexto.parametros.integer("diaVencimiento");
        boolean esCuotificacion = false;
        Boolean esAdelanto = contexto.parametros.bool("esAdelanto", false);
        Boolean quiereSeguroDesempleo = !esAdelanto ? contexto.parametros.bool("seguroDesempleo") : false;

        List<String> nemonicos = new ArrayList<>();
        Objeto solicitudDatos = new Objeto();
        Solicitud solicitud = null;

        try {
            if (tieneParametrosCanalAmarillo(contexto)) {
                return RespuestaMB.parametrosIncorrectos();
            }

            if (quiereSeguroDesempleo && contexto.esJubilado()) {
                Solicitud.logOriginacion(contexto, idSolicitud, "simularOfertaPrestamo", null, "JUBILADO_QUERIENDO_SEGURO");
                return RespuestaMB.estado("JUBILADO_QUERIENDO_SEGURO");
            }

            if (Objeto.anyEmpty(idSolicitud, idPrestamo, monto, plazo, diaVencimiento, quiereSeguroDesempleo)) {
                Solicitud.logOriginacion(contexto, idSolicitud, "simularOfertaPrestamo", null, "PARAMETROS_INCORRECTOS");
                return RespuestaMB.parametrosIncorrectos();
            }

            if (monto == null) { // significa que es una solicitud que dio amarillo o rojo y no alcanzó a pasarme
                // el monto
                return solicitarPrimerOfertaPrestamo(contexto);
            }

            RespuestaMB personaEdad = OriginacionHelper.validaPersonaEdad(contexto);
            if (personaEdad.hayError()) {
                Solicitud.logOriginacion(contexto, idSolicitud, "simularOfertaPrestamo", null, personaEdad.toJson());
                return personaEdad;
            }

            Cuenta cuenta = null;
            String idCuentaRecuperada = OriginacionHelper.validaCuentaExistente(contexto, idCuenta);
            if (idCuentaRecuperada == null) {
                return RespuestaMB.estado("CUENTA_INEXISTENTE");

            } else if (!"".equals(idCuentaRecuperada)) {
                cuenta = contexto.cuenta(idCuentaRecuperada);
                if (cuenta == null) {
                    return RespuestaMB.estado("CUENTA_INEXISTENTE");
                }
                if ("I".equals(cuenta.idEstado())) {
                    return RespuestaMB.estado("CUENTA_INACTIVA");
                }
            }

            solicitud = Solicitud.solicitud(contexto, idSolicitud);
            String estadoValidacion = OriginacionHelper.validaCuentaYTarjetas(cuenta, solicitud, contexto, idSolicitud, idPrestamo, solicitudDatos);
            if (!"".equals(estadoValidacion)) {
                Solicitud.logOriginacion(contexto, idSolicitud, "simular_validaCuentaYTarjetas", null, "ERROR_" + estadoValidacion);
                return RespuestaMB.estado(estadoValidacion);
            }

            if (!esAdelanto) {
                String estadoSeguroDesempleo = OriginacionHelper.actualizaSeguroDesempleo(contexto, idSolicitud, idPrestamo, solicitudDatos, quiereSeguroDesempleo);
                if (!"".equals(estadoSeguroDesempleo)) {
                    Solicitud.logOriginacion(contexto, idSolicitud, "simnular_actualizaSeguroDesempleo", null, "ERROR_" + estadoSeguroDesempleo);
                    return RespuestaMB.estado(estadoSeguroDesempleo);
                }
            }

            if (cuenta == null) {
                nemonicos.add("CASOLIC");
            }

            for (Solicitud.SolicitudProducto producto : solicitud.Productos) {
                String tipoProducto = producto.IdProductoFrontEnd;
                String nemonico = producto.Nemonico;

                if (tipoProducto.equals("2") && nemonico.equals("PPCUOTIFSG")) {
                    esCuotificacion = true;
                    break;
                }
            }


            contexto.parametros.set("esCuotificacion", esCuotificacion);

            // Primero tengo que modificar la solicitud con 02 para poder evaluarla
            ApiResponseMB prestamoModificar = RestVenta.modificarSolicitudPrestamoPersonal(contexto, idSolicitud, idPrestamo, monto, plazo, diaVencimiento, cuenta == null ? "0" : cuenta.numero(), "02");
            Solicitud.logOriginacion(contexto, idSolicitud, "simular_modificarSolicitudPrestamoPersonal", prestamoModificar, "");
            if (prestamoModificar.hayError() || prestamoModificar.objetos("Errores") != null && prestamoModificar.objetos("Errores").size() > 0) {
                Solicitud.logOriginacion(contexto, idSolicitud, "simular_modificarSolicitudPrestamoPersonal", null, "ERROR");
                return RespuestaMB.error();
            }

            if (!esAdelanto && solicitudDatos.bool("tieneSeguroSolicitado")) {
                ApiResponseMB seguro = RestVenta.modificarSeguroDesempleo(contexto, idSolicitud, solicitudDatos.string("idSeguroSolicitado"), idPrestamo);
                Solicitud.logOriginacion(contexto, idSolicitud, "simular_modificarSeguroDesempleo", seguro, "");
                if (seguro.hayError() || !seguro.objetos("Errores").isEmpty()) {
                    Solicitud.logOriginacion(contexto, idSolicitud, "simular_modificarSeguroDesempleo", null, "ERROR_PUT_SEGURO_DESEMPLEO");
                    return RespuestaMB.estado("ERROR_PUT_SEGURO_DESEMPLEO");
                }
            }

            // EVALUAR SOLICITUD
            ResolucionMotor evaluacionSolicitud = solicitud.ejecutarMotor(contexto);
            if (evaluacionSolicitud.esAprobadoAmarillo()) {
                return casoCanalAmarillo(contexto, solicitud, idPrestamo, quiereSeguroDesempleo, evaluacionSolicitud, false, cuenta);
            }
            if (evaluacionSolicitud.esRojo()) {
                Solicitud.logOriginacion(contexto, idSolicitud, "simular_evaluacionSolicitud", null, "ROJO");
                return new RespuestaMB().set("color", "ROJO");
            }
            if (evaluacionSolicitud.esAmarillo() || evaluacionSolicitud.esSAmarillo()) {
                Solicitud.logOriginacion(contexto, idSolicitud, "simular_evaluacionSolicitud", null, "AMARILLO");
                return new RespuestaMB().set("color", "AMARILLO");
            }

            RespuestaMB cambioMonto = OriginacionHelper.validaMontoModifSolicitud(contexto, solicitud, cuenta);
            if (cambioMonto.hayError()) {
                Solicitud.logOriginacion(contexto, idSolicitud, "simular_validaMontoModifSolicitud", null, cambioMonto.toJson());
                return cambioMonto;
            }

            contexto.parametros.set("idPrestamo", solicitud.idPrestamo());
            contexto.parametros.set("tieneSeguroDesempleo", quiereSeguroDesempleo);
            RespuestaMB respuesta = consultarPrestamo(contexto);

            if (!plazo.equals(respuesta.integer("plazo"))) {
                respuesta.set("plazoModificadoPorMotor", true);
                respuesta.set("plazoMinimo", respuesta.integer("plazo"));
            } else {
                respuesta.set("plazoModificadoPorMotor", false);
            }

            contexto.sesion().setPlazoPrestamoAprobado(respuesta.integer("plazo"));
            contexto.sesion().setMontoPrestamoAprobado(respuesta.bigDecimal("monto"));
            contexto.sesion().setCuentaPrestamoAprobado(cuenta == null ? null : cuenta.numero());
            nemonicos.add("PPVERDE");
            respuesta.set("nemonicos", nemonicos);
            respuesta.set("color", "VERDE");
            if (esAdelanto && contexto.esJubilado() && ConfigMB.bool("prendido_adelanto_recurrente", false)) {
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
        } catch (Exception e) {
            if (e instanceof JsonParseException)
                return RespuestaMB.timeOut();
            return RespuestaMB.error();
        }
    }

    public static RespuestaMB finalizarSolicitudPrestamo(ContextoMB contexto) {
        String idSolicitud = contexto.parametros.string("idSolicitud");
        Boolean mejorarOferta = contexto.parametros.bool("mejorarOferta", false);
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        try {

            if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_modo_transaccional_alta_producto",
                    "prendido_modo_transaccional_alta_producto_cobis") && !TransmitMB.isChallengeOtp(contexto, "prestamo-personal")) {
                try {
                    String sessionToken = contexto.parametros.string(Transmit.getSessionToken(), null);
                    String idCuenta = contexto.parametros.string("idCuenta", null);
                    if (Objeto.empty(sessionToken, idCuenta))
                        return RespuestaMB.parametrosIncorrectos();

                    Futuro<ApiResponseMB> futuroResponse = new Futuro<>(() -> RestOmnicanalidad.consultarSolicitud(contexto, idSolicitud));

                    ApiResponseMB response = futuroResponse.tryGet();
                    if (!response.hayError()) {
                        Objeto datos = response.objetos("Datos").get(0);
                        if (!Objeto.empty(datos)) {
                            Futuro<Objeto> futuroProductos = new Futuro<>(() -> MBOmnicanalidad.productoPrestamo(contexto, idSolicitud, datos));

                            Objeto productos = futuroProductos.tryGet();
                            Cuenta cuenta = contexto.cuenta(idCuenta);

                            if (!Objeto.empty(productos, cuenta)) {
                                String tipo = "";
                                if (!Objeto.empty(datos.objeto("Productos")) && !datos.objeto("Productos").objetos().isEmpty())
                                    tipo = datos.objeto("Productos").objetos().get(0).string("Producto");

                                String monto = productos.objeto("prestamoPersonal").string("monto");

                                AltaPrestamoMBBMBankProcess altaPrestamoMBBMBankProcess = new AltaPrestamoMBBMBankProcess(contexto.idCobis(),
                                        sessionToken,
                                        new BigDecimal(StringUtils.isNotBlank(monto) ? monto : "0"),
                                        Util.obtenerDescripcionMonedaTransmit("80"),
                                        StringUtils.isNotBlank(tipo) ? tipo : TransmitMB.REASON_TRANSFERENCIA,
                                        new AltaPrestamoMBBMBankProcess.Payer(contexto.persona().cuit(), cuenta.numero(), Util.getBhCodigo(), TransmitMB.CANAL),
                                        new AltaPrestamoMBBMBankProcess.Payee("", "", ""));

                                RespuestaMB respuesta = TransmitMB.recomendacionTransmit(contexto, altaPrestamoMBBMBankProcess, "prestamo-personal");
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
                return RespuestaMB.parametrosIncorrectos();

            RespuestaMB valida = Solicitud.validarFinalizarSolicitud(contexto);
            if (valida.hayError())
                return valida;

            // desde-consulto la solicitud para asegurarme que es la del que pidió
            // finalizarla
            Solicitud solicitud = Solicitud.solicitud(contexto, idSolicitud);
            if (solicitud == null) {
                Solicitud.logOriginacion(contexto, idSolicitud, "finalizarSolicitud", null, "ERROR_CONSULTA_SOLICITUD");
                return RespuestaMB.estado("ERROR_CONSULTA_SOLICITUD");
            }

            boolean encuentraIdCobis = false;
            for (Integrante integrante : solicitud.Integrantes) {
                if (contexto.idCobis().equals(integrante.IdCobis))
                    encuentraIdCobis = true;
            }

            if (!encuentraIdCobis) {
                Solicitud.logOriginacion(contexto, idSolicitud, "finalizarSolicitud", null, "ERROR_ID_COBIS");
                return RespuestaMB.estado("ERROR_ID_COBIS");
            }

            boolean esCuotificacion = false;

            for (Solicitud.SolicitudProducto producto : solicitud.Productos) {
                String tipoProducto = producto.IdProductoFrontEnd;
                String nemonico = producto.Nemonico;

                if (tipoProducto.equals("2") && nemonico.equals("PPCUOTIFSG")) {
                    esCuotificacion = true;
                    break;
                }
            }

            Futuro<SqlResponseMB> futuroSqlResponse = null;
            if (esCuotificacion)
                futuroSqlResponse = new Futuro<>(() -> SqlMovimientosCuotificacion.get(contexto.idCobis(), false));

            contexto.parametros.set("esCuotificacion", esCuotificacion);

            String actualiza = validaSolicitudCanalAmarillo(contexto, solicitud);
            if (actualiza != "") {
                return RespuestaMB.estado(actualiza);
            }

            //Antes de finalizar la solicitud, obtengo la info que se envia a SALESFORCE
            contexto.parametros.set("idPrestamo", solicitud.idPrestamo());
            RespuestaMB rSalesforce = consultarPrestamo(contexto);
            Objeto salesforceAltaPP = new Objeto();
            salesforceAltaPP.set("MONTO_PRESTAMO", rSalesforce.string("montoFormateado"));
            salesforceAltaPP.set("FECHA_VENCIMIENTO_CUOTA", rSalesforce.string("diaCobro"));
            salesforceAltaPP.set("MONTO_CUOTA", rSalesforce.string("importePrimeraCuotaFormateado"));
            contexto.parametros.set(ConfigMB.string("salesforce_alta_prestamo"), salesforceAltaPP);


            // hasta-consulto la solicitud para asegurarme que es la del que pidió
            // finalizarla
            ApiResponseMB response = RestVenta.finalizarSolicitud(contexto, solicitud.IdSolicitud);
            Solicitud.logApiVentas(contexto, idSolicitud, "finalizarSolicitud", response);
            if (response.hayError() || !response.objetos("Errores").isEmpty()) {
                String mensajeCliente = "";
                try {
                    mensajeCliente = response.objetos("Errores").get(0).string("MensajeCliente");
                } catch (Exception e) {
                }
                // "Producto: PrestamoPersonal El dia de cobro debe estar entre el 1 y 30 del
                // mes",
                if (mensajeCliente.contains("seleccione otra fecha de cobro fija")) {
                    Solicitud.logOriginacion(contexto, solicitud.IdSolicitud, "finalizarSolicitud", response, "SELECCIONAR_OTRA_FECHA_COBRO");
                    return RespuestaMB.estado("SELECCIONAR_OTRA_FECHA_COBRO");
                }
                if (response.objetos("Errores").get(0).string("MensajeDesarrollador").contains("FAULTCODE:40003 FAULTMSJ:Producto bancario deshabilitado")) {
                    Solicitud.logOriginacion(contexto, solicitud.IdSolicitud, "finalizarSolicitud", response, "ERROR_CORRIENDO_BATCH");
                    return new RespuestaMB().setEstado("ERROR_CORRIENDO_BATCH");
                }
                Solicitud.logOriginacion(contexto, solicitud.IdSolicitud, "ERROR_finalizarSolicitud", response, "ERROR");
                return RespuestaMB.error();
            }

            if (mejorarOferta && ConfigMB.bool("prendido_canal_amarillo_pp")) {
                SqlPrestamos.finalizaSolicitudCanalAmarillo(contexto, solicitud.IdSolicitud);
                // SqlPrestamos.eliminarCacheActividades(contexto);
            }

            ProductosService.eliminarCacheProductos(contexto);

            String idSolicitudProxima = "";
            if (ConfigMB.esOpenShift()) {
                idSolicitudProxima = RestOmnicanalidad.limpiarSolicitudes(contexto, ConfigMB.longer("solicitud_dias_vigente", 30L), true, false, true);
            }

            if (esCuotificacion) {
                SqlResponseMB sqlResponse = futuroSqlResponse.tryGet();
                if (sqlResponse != null && !sqlResponse.hayError && !sqlResponse.registros.isEmpty()) {
                    sqlResponse.registros.forEach(m -> {
                        SqlMovimientosCuotificacion.update(m.string("id"), true);
                    });
                }
            }

            RespuestaMB respuesta = new RespuestaMB();
            respuesta.set("idSolicitud", idSolicitud);
            respuesta.set("idSolicitudProxima", idSolicitudProxima);

            BigDecimal montoAprobado = contexto.sesion().montoPrestamoAprobado();
            Integer plazoAprobado = contexto.sesion().plazoPrestamoAprobado();
            String cuentaAprobado = contexto.sesion().cuentaPrestamoAprobado();

            contexto.parametros.set("nemonico", "FINALIZA_PP");
            Util.contador(contexto);
            contexto.parametros.set("idSolicitud", solicitud);
            notificaEmailPP(contexto, solicitud);
            contexto.sesion().setAceptaTyC(false);
            contexto.sesion().setValidaRiesgoNet(false);
            contexto.sesion().setAdjuntaDocumentacion(false);
            contexto.limpiarSegundoFactor();
            Solicitud.logOriginacion(contexto, idSolicitud, "FIN_finalizarSolicitud", null, respuesta.toJson());
            contexto.insertarLogPrestamos(contexto, montoAprobado, plazoAprobado, cuentaAprobado);
            return respuesta;
        } catch (Exception e) {
            if (e instanceof JsonParseException)
                return RespuestaMB.timeOut();
            return RespuestaMB.error();
        }
    }

    private static String validaSolicitudCanalAmarillo(ContextoMB contexto, Solicitud solicitud) {
        Boolean mejorarOferta = contexto.parametros.bool("mejorarOferta", false);

        if (mejorarOferta && ConfigMB.bool("prendido_canal_amarillo_pp")) {
            Objeto solicitudCanalAmarillo = SqlPrestamos.solicitudCanalAmarillo(contexto, solicitud.IdSolicitud);

            if (!Objeto.empty(solicitudCanalAmarillo) && solicitudCanalAmarillo.existe("solicitud")) {
                solicitudCanalAmarillo = solicitudCanalAmarillo.objeto("solicitud");
                String ingresos_nuevos = solicitudCanalAmarillo.string("ingresos_nuevos").trim();
                ApiResponseMB response = RestPersona.actualizarActividad(contexto, Integer.parseInt(solicitudCanalAmarillo.string("id_actividad").trim()), solicitudCanalAmarillo.string("situacion_labora_nueva").trim(), solicitudCanalAmarillo.string(""), // idProfesion
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

    public static RespuestaMB datosPersonalesFaltantes(ContextoMB contexto) {
        String tipoProducto = contexto.parametros.string("tipoProducto");

        boolean simularTodoTrue = false; // esto es sólo para ayudar al front end a que le traiga casos
        boolean irSucursal = false;
        boolean validaDatosPersonalesOriginacion = true;

        boolean esMonoProductoTC = contexto.esMonoProductoTC();
        boolean poseeCuentasUnipersonales = contexto.poseeCuentasUnipersonales();

        if (Objeto.anyEmpty(tipoProducto)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (!"paquete".equals(tipoProducto) && !"prestamo".equals(tipoProducto) && !"prestamo_paquete".equals(tipoProducto)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        RespuestaMB respuesta = new RespuestaMB();
        ApiResponseMB clientes = RestPersona.consultarClienteEspecifico(contexto, contexto.idCobis());
        if (clientes.hayError()) {
            return RespuestaMB.error();
        }

        Objeto cliente = clientes.objetos().get(0);
        Objeto datosPrincipales = new Objeto();
        Objeto datosPoliticos = new Objeto();
        irSucursal = OriginacionHelper.validaDatosPrincipales(cliente);

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

        if (cliente.string("idEstadoCivil") == null || cliente.string("idEstadoCivil").isEmpty() || simularTodoTrue) {

            datosPrincipales.add("estadoCivil");
            validaDatosPersonalesOriginacion = false;
        }
        if (cliente.integer("idNivelEstudios") == null || simularTodoTrue) {
            datosPrincipales.add("nivelEstudios");
            validaDatosPersonalesOriginacion = false;
        }

        if (cliente.string("idSituacionVivienda").isEmpty() || cliente.string("idSituacionVivienda") == null || simularTodoTrue) {
            datosPrincipales.add("situacionVivienda");
            validaDatosPersonalesOriginacion = false;
        }
        /* DATOS LABORALES - HASTA */

        /* DOMICILIO LEGAL - DESDE */
        Objeto domicilioLegal = OriginacionHelper.validaDomicilioLegal(contexto);
        if (domicilioLegal.bool("faltan_datos")) {
            validaDatosPersonalesOriginacion = false;
        }
        /* DOMICILIO LEGAL - HASTA */

        /* DOMICILIO POSTAL - DESDE */
        Objeto domicilioPostal = OriginacionHelper.validaDomicilioPostal(contexto, esMonoProductoTC, poseeCuentasUnipersonales);
        if (domicilioPostal.bool("faltan_datos")) {
            validaDatosPersonalesOriginacion = false;
            if (esMonoProductoTC || !poseeCuentasUnipersonales) {
                irSucursal = true;
            }
        }
        /* DOMICILIO POSTAL - HASTA */

        OriginacionHelper.seteaDatosPoliticos(datosPoliticos, cliente);

        respuesta.set("irSucursal", irSucursal);
        respuesta.set("monoProducto", esMonoProductoTC);
        respuesta.set("poseeCuentasUnipersonales", poseeCuentasUnipersonales);

        if (datosPrincipales.esLista())
            respuesta.set("datosPrincipales", datosPrincipales);
        else
            respuesta.add("datosPrincipales", datosPrincipales);

        respuesta.set("datosPoliticos", datosPoliticos);
        respuesta.set("domicilioLegal", domicilioLegal);
        respuesta.set("domicilioPostal", domicilioPostal);

        // PROFESION DESDE
        OriginacionHelper.validaProfesion(contexto);
        // PROFESION HASTA
        contexto.sesion().setValidaDatosPersonalesOriginacion(validaDatosPersonalesOriginacion);

        return respuesta;
    }

    public static RespuestaMB datosPersonales(ContextoMB contexto) {
        String tipoProducto = contexto.parametros.string("tipoProducto");

        if (Objeto.anyEmpty(tipoProducto)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        RespuestaMB respuesta = new RespuestaMB();
        // respuesta.set("requiereCompletarDatosPrincipales", false);
        // respuesta.set("requiereCompletarDatosPoliticos", false);
        // respuesta.set("requiereCompletarDatosLaborales", false);

        ApiResponseMB clientes = RestPersona.consultarClienteEspecifico(contexto, contexto.idCobis());
        if (clientes.hayError()) {
            return RespuestaMB.error();
        }

        Objeto cliente = clientes.objetos().get(0);

        if ("paquete".equals(tipoProducto) || "prestamo".equals(tipoProducto)) {
            Objeto datosPrincipales = new Objeto();
            Objeto datosPoliticos = new Objeto();
            Objeto datosLaborales = new Objeto();

            /* DATOS PRINCIPALES - DESDE */
            datosPrincipales.set("nombres", Texto.primerasMayuscula(cliente.string("nombres")));
            datosPrincipales.set("apellidos", Texto.primerasMayuscula(cliente.string("apellidos")));
            datosPrincipales.set("tipoCuil", (cliente.bool("esPersonaFisica")) ? "CUIT" : "CUIL");
            datosPrincipales.set("cuit", cliente.string("cuit"));
            datosPrincipales.set("numeroDocumento", cliente.string("numeroDocumento"));
            datosPrincipales.set("tipoDocumento", tipoDocumento(cliente.integer("idTipoDocumento")));
            datosPrincipales.set("numeroDocumento", cliente.string("numeroDocumento"));
            datosPrincipales.set("fechaNacimiento", cliente.date("fechaNacimiento", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
            if ("M".equals(cliente.string("idSexo")))
                datosPrincipales.set("genero", "MASCULINO");
            else
                datosPrincipales.set("genero", "FEMENINO");
            datosPrincipales.set("idPaisNacimiento", "");
            datosPrincipales.set("paisNacimiento", "");
            if (cliente.integer("idPaisNacimiento") != null) {
                datosPrincipales.set("idPaisNacimiento", cliente.integer("idPaisNacimiento"));
                datosPrincipales.set("paisNacimiento", RestCatalogo.mapaPaises(contexto).get(cliente.integer("idPaisNacimiento")));
            }
            datosPrincipales.set("idNacionalidad", "");
            datosPrincipales.set("nacionalidad", "");
            if (cliente.integer("idNacionalidad") != null) {
                datosPrincipales.set("idNacionalidad", cliente.integer("idNacionalidad"));
                datosPrincipales.set("nacionalidad", RestCatalogo.mapaPaises(contexto).get(cliente.integer("idNacionalidad")));
            }
            /* DATOS PRINCIPALES - HASTA */

            /* DATOS POLITICOS - DESDE */
            datosPoliticos.set("sujetoObligado", cliente.bool("esSO"));
            datosPoliticos.set("expuestoPoliticamente", cliente.bool("esPEP"));
            datosPoliticos.set("ciudadanoEstadounidense", cliente.bool("indicioFatca"));
            datosPoliticos.set("residenciaFiscalOtroPais", false);
            if (cliente.integer("idPaisResidencia") != null && !"".equals(cliente.string("idPaisResidencia")) && cliente.integer("idPaisResidencia") != 80) {
                datosPoliticos.set("residenciaFiscalOtroPais", true);
            }
            /* DATOS POLITICOS - HASTA */

            /* DATOS LABORALES - DESDE */
            datosLaborales.set("idSituacionLaboral", "");
            datosLaborales.set("situacionLaboral", "");
            datosLaborales.set("idNivelEstudios", "");
            datosLaborales.set("nivelEstudios", "");
            datosLaborales.set("idEstadoCivil", "");
            datosLaborales.set("estadoCivil", "");
            datosLaborales.set("idSituacionVivienda", "");
            if (cliente.integer("idSituacionLaboral") != null) {
                datosLaborales.set("idSituacionLaboral", cliente.integer("idSituacionLaboral"));
                datosLaborales.set("situacionLaboral", RestCatalogo.mapaSituacionesLaborales(contexto).get(cliente.integer("idSituacionLaboral")));
            }
            if (cliente.integer("idNivelEstudios") != null) {
                datosLaborales.set("idNivelEstudios", cliente.integer("idNivelEstudios"));
                datosLaborales.set("nivelEstudios", RestCatalogo.mapaNivelesEstudios(contexto).get(cliente.integer("idNivelEstudios")));
            }
            if (cliente.string("idEstadoCivil") != null) {
                datosLaborales.set("idEstadoCivil", cliente.string("idEstadoCivil"));
                datosLaborales.set("estadoCivil", RestCatalogo.mapaEstadosCiviles(contexto).get(cliente.string("idEstadoCivil")));
            }
            datosLaborales.set("nombreConyuge", cliente.string("nombreConyuge"));
            datosLaborales.set("apellidoConyuge", cliente.string("apellidoConyuge"));

            /* DATOS LABORALES - HASTA */

            respuesta.set("datosPrincipales", datosPrincipales);
            respuesta.set("datosPoliticos", datosPoliticos);
            respuesta.set("datosLaborales", datosLaborales);
        } else { // no paso ningún tipo de producto
            respuesta.set("requiereCompletarDatosPrincipales", true);
            respuesta.set("requiereCompletarDatosPoliticos", true);
            respuesta.set("requiereCompletarDatosLaborales", true);
        }

        return respuesta;
    }

    public static RespuestaMB datosPersonalesModificar(ContextoMB contexto) {
        String idNivelEstudios = contexto.parametros.string("nivelEstudios");
        String idSituacionVivienda = contexto.parametros.string("idSituacionVivienda");
        String idEstadoCivil = contexto.parametros.string("idEstadoCivil");
        Boolean esSujetoObligado = contexto.parametros.bool("sujetoObligado", null);
        Boolean esExpuestoPoliticamente = contexto.parametros.bool("expuestoPoliticamente", null);
        Boolean estadounidenseOResidenciaFiscalOtroPais = contexto.parametros.bool("estadounidenseOresidenciaFiscalOtroPais", null);
        String calleParticular = contexto.parametros.string("domicilioPostal.calle");
        String alturaParticular = contexto.parametros.string("domicilioPostal.altura");
        String pisoParticular = contexto.parametros.string("domicilioPostal.piso");
        String departamentoParticular = contexto.parametros.string("domicilioPostal.departamento");
        String idLocalidadParticular = contexto.parametros.string("domicilioPostal.idLocalidad");
        String codigoPostalParticular = contexto.parametros.string("domicilioPostal.codigoPostal");
        String calleUnicaEntrega = contexto.parametros.string("domicilioLegal.calle");
        String alturaUnicaEntrega = contexto.parametros.string("domicilioLegal.altura");
        String pisoUnicaEntrega = contexto.parametros.string("domicilioLegal.piso");
        String departamentoUnicaEntrega = contexto.parametros.string("domicilioLegal.departamento");
        String idLocalidadUnicaEntrega = contexto.parametros.string("domicilioLegal.idLocalidad");
        String codigoPostalUnicaEntrega = contexto.parametros.string("domicilioLegal.codigoPostal");
        String cuitEmpleador = contexto.parametros.string("cuitEmpleador");

        if (Objeto.allEmpty(idNivelEstudios, idSituacionVivienda, idEstadoCivil, calleParticular, alturaParticular, pisoParticular, departamentoParticular, idLocalidadParticular, codigoPostalParticular, calleUnicaEntrega, alturaUnicaEntrega, pisoUnicaEntrega, departamentoUnicaEntrega, idLocalidadUnicaEntrega, codigoPostalUnicaEntrega, esSujetoObligado, esExpuestoPoliticamente, estadounidenseOResidenciaFiscalOtroPais, cuitEmpleador)) {

            return RespuestaMB.parametrosIncorrectos();
        }

        if (OriginacionHelper.validezDatosObligatorios(contexto)) {
            return RespuestaMB.estado("DATOS_POLITICOS_TRUE");
        }

        if (!contexto.validaSegundoFactor("originacion-datos-personales")) {
            return RespuestaMB.estado("REQUIERE_SEGUNDO_FACTOR");
        }

        String obligatoriosModificados = RestPersona.actualizaDatosObligatorios(contexto);
        if (obligatoriosModificados.contains("ERROR")) {
            return RespuestaMB.estado(obligatoriosModificados);
        }

        String datoModificado = OriginacionHelper.validaActualizaDatosDomicilio(contexto);
        if (datoModificado.contains("ERROR")) {
            return datoModificado.contains("ERROR") ? RespuestaMB.estado(datoModificado) : RespuestaMB.estado("ERROR_MODIF_DOMICILIO");

        }

        String actualiza = OriginacionHelper.validaActualizaDomicilioEntrega(contexto, datoModificado);
        if (actualiza.contains("ERROR")) {
            return RespuestaMB.estado(actualiza);
        }

        ApiMB.eliminarCache(contexto, "Cliente", contexto.idCobis());
        RestPersona.enviarMailActualizacionDatosPersonales(contexto, datoModificado, null, null);
        contexto.limpiarSegundoFactor();
        return RespuestaMB.exito();
    }

    public static String tipoDocumento(Integer idTipoDocumento) {
        String tipoDocumento = "DNI";
        switch (idTipoDocumento) {
            case 1:
                tipoDocumento = "DNI";
                break;
            case 134:
                tipoDocumento = "DNI";
                break;
            case 2:
                tipoDocumento = "LE";
                break;
            case 3:
                tipoDocumento = "LC";
                break;
            case 101:
            case 102:
            case 103:
            case 104:
            case 105:
            case 106:
            case 107:
            case 108:
            case 109:
            case 110:
            case 111:
            case 112:
            case 113:
            case 114:
            case 115:
            case 116:
            case 117:
            case 118:
            case 119:
            case 120:
            case 121:
            case 123:
            case 140:
            case 122:
                tipoDocumento = "CI";
                break;
            case 125:
                ;
            case 126:
                tipoDocumento = "PAS";
                break;
        }
        return tipoDocumento;
    }

    public static RespuestaMB consultarConsumosSugeridos(ContextoMB contexto) {
        String idCobis = contexto.idCobis();
        if (StringUtils.isEmpty(idCobis)) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        RespuestaMB respuesta = new RespuestaMB();
        ApiResponseMB response = RestPersona.consumosSugeridos(contexto, idCobis);
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        boolean encontro = false;
        List<Objeto> listaSugerenciasConDestacados = new ArrayList<Objeto>();

        if (response.codigo == 204) {
            respuesta.set("consumosSugeridos", listaSugerenciasConDestacados);
            return respuesta;
        }

        List<Objeto> listaSugerencias = response.objetos();
        consumosActivosMesActual(listaSugerencias);
        consumosSinDuplicados(listaSugerencias);
        ordenarXPuntaje(listaSugerencias, "score");

        int ordenPuntaje = 0;
        for (Objeto sugerencia : listaSugerencias) {
            Objeto consumoSugerido = new Objeto();
            String direccion = String.join(" ", sugerencia.string("merchantAdress1txt"), sugerencia.string("merchantAdress2txt"));
            consumoSugerido.set("orden", ordenPuntaje++);
            consumoSugerido.set("idEstablecimiento", sugerencia.string("recomendedMerchant"));
            consumoSugerido.set("fechaCorrida", Fecha.formato(sugerencia.string("date"), "yyyy-MM-dd", "dd/MM/yyyy"));
            consumoSugerido.set("rubroVisa", sugerencia.string("merchantGroupDesc"));
            consumoSugerido.set("tagPromocion", sugerencia.string("promocion"));
            consumoSugerido.set("descuento", limpiarDecimalDescuento(sugerencia.string("descuento")));
            consumoSugerido.set("tope", procesarTope(sugerencia.string("tope")));
            consumoSugerido.set("nombreComercio", sugerencia.string("merchantDesc"));
            consumoSugerido.set("direccion", validaCampoNull(direccion));
            consumoSugerido.set("ciudad", validaCampoNull(sugerencia.string("merchantState")));
            consumoSugerido.set("provincia", validaCampoNull(sugerencia.string("merchantCity")));
            consumoSugerido.set("puntaje", sugerencia.string("score"));
            consumoSugerido.set("idCobis", sugerencia.string("partyId"));
            consumoSugerido.set("codigoPostal", validaCampoNull(sugerencia.string("merchantPostalCodeNum")));
            consumoSugerido.set("nombreComercioVisualizacion", sugerencia.string("visualizationName"));
            consumoSugerido.set("diaSemanaDescuento", sugerencia.string("Dia"));
            consumoSugerido.set("googleMapsDireccion", validaDireccionGoogle(sugerencia, direccion));
            boolean destacado = consumoSugeridoDestacado(sugerencia.string("Dia"));
            if (destacado && !encontro) {
                encontro = true;
                consumoSugerido.set("destacado", destacado);
            }
            listaSugerenciasConDestacados.add(consumoSugerido);
        }

        if (!encontro) {
            destacadoDiaSiguiente(listaSugerenciasConDestacados);
        }

        respuesta.set("consumosSugeridos", listaSugerenciasConDestacados);
        return respuesta;
    }

    private static String validaDireccionGoogle(Objeto sugerencia, String direccion) {
        if (validaCampoNull(direccion) != "" && validaCampoNull(sugerencia.string("merchantPostalCodeNum")) != "" && validaCampoNull(sugerencia.string("merchantState")) != "" && validaCampoNull(sugerencia.string("merchantCity")) != "") {
            return String.join(", ", validaCampoNull(direccion), validaCampoNull(sugerencia.string("merchantPostalCodeNum")), validaCampoNull(sugerencia.string("merchantState")), validaCampoNull(sugerencia.string("merchantCity")));
        }
        return "";
    }

    private static String validaCampoNull(String campo) {
        if (campo.equalsIgnoreCase("NO DEFINIDA") || campo.equalsIgnoreCase("No Identificado") || campo.equals(" ") || campo.equals("")) {
            return "";
        }
        return campo;
    }

    private static void destacadoDiaSiguiente(List<Objeto> listaSugerencia) {
        DayOfWeek fechaManana = LocalDate.now().plusDays(1L).getDayOfWeek();
        String fechaTextoManana = Fecha.diaES(fechaManana);
        listaSugerencia.forEach(item -> {
            if (item.string("diaSemanaDescuento").contains(fechaTextoManana)) {
                item.set("destacado", true);
                return;
            }
        });
    }

    private static boolean consumoSugeridoDestacado(String diaConsumo) {
        DayOfWeek fechaHoy = LocalDate.now().getDayOfWeek();
        String fechaTextoHoy = Fecha.diaES(fechaHoy);

        if (diaConsumo.toUpperCase().contains(fechaTextoHoy) || diaConsumo.contains("Todos")) {
            return true;
        }

        return false;
    }

    private static String limpiarDecimalDescuento(String descuento) {
        String porcentaje = descuento.indexOf(".0") > 0 ? StringUtils.substring(descuento, 0, descuento.indexOf(".0")) : descuento;
        return porcentaje + "%";
    }

    private static String procesarSplit(String tope, String valorSplit) {
        String[] topeList = tope.split(valorSplit);
        return (topeList.length > 1) ? topeList[topeList.length - 1] : "";
    }

    private static Objeto procesarTope(String tope) {
        Objeto topes = new Objeto();

        String esCompraOnline = procesarSplit(tope, TOPE_SEPARADOR_COMPRA_ONLINE_5_ESPACIOS);
        topes.set("esCompraOnline", esCompraOnline.equalsIgnoreCase("no") ? false : true);
        tope = StringUtils.substringBefore(tope, TOPE_SEPARADOR_COMPRA_ONLINE_5_ESPACIOS);

        String detalleLegal = procesarSplit(tope, TOPE_SEPARADOR_DETALLE_LEGAL_4_ESPACIOS);
        topes.set("detalleLegal", detalleLegal);
        tope = StringUtils.substringBefore(tope, TOPE_SEPARADOR_DETALLE_LEGAL_4_ESPACIOS);

        String[] topeList = tope.split(TOPE_SEPARADOR_DESCRIPCIONES_3_ESPACIOS);
        int num = 0;
        while (num < topeList.length) {
            topes.set("descripcion" + (num + 1), topeList[num]);
            num++;
        }
        return topes;
    }

    private static void ordenarXPuntaje(List<Objeto> lista, String campo) {
        Collections.sort(lista, Collections.reverseOrder(new Comparator<Objeto>() {
            public int compare(Objeto o1, Objeto o2) {
                BigDecimal a = o1.bigDecimal(campo);
                BigDecimal b = o2.bigDecimal(campo);
                return a.compareTo(b);
            }
        }));
    }

    private static void consumosSinDuplicados(List<Objeto> lista) {
        Fecha.ordenarPorFechaDesc(lista, "date", "yyyy-MM-dd");
        HashSet<Object> seen = new HashSet<>();
        lista.removeIf(e -> !seen.add(e.get("recomendedMerchant")));
    }

    private static void consumosActivosMesActual(List<Objeto> lista) {
        int totalDiasMesActual = LocalDate.now().lengthOfMonth();
        int diaHoy = LocalDate.now().getDayOfMonth();
        DayOfWeek diaHoySemana = LocalDate.now().getDayOfWeek();
        String diaHoySemanaTexto = Fecha.diaES(diaHoySemana);

        int diaInicioRango = Math.subtractExact(totalDiasMesActual, 6);
        String diaInicioRangoTexto = conocerDiaSemana(diaInicioRango);

        if (diaHoy >= diaInicioRango && diaHoy <= totalDiasMesActual && (diaHoy - diaInicioRango) >= 1) {
            List<String> diasSemana = Arrays.asList("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO");
            int pos1 = diasSemana.indexOf(diaInicioRangoTexto);
            int pos2 = diasSemana.indexOf(diaHoySemanaTexto);

            List<String> diasSemanaRestante = new ArrayList<>();
            if (pos2 < pos1) {
                diasSemanaRestante.addAll(diasSemana.subList(pos1, diasSemana.size()));
                diasSemanaRestante.addAll(diasSemana.subList(0, pos2));
            } else {
                diasSemanaRestante = diasSemana.subList(pos1, pos2);
            }

            for (String dia : diasSemanaRestante) {
                lista.removeIf(consumo -> consumo.string("Dia").toUpperCase().contains(dia));
            }
        }
    }

    private static String conocerDiaSemana(int diaABuscar) {
        YearMonth mesDelAno = YearMonth.of(LocalDate.now().getYear(), LocalDate.now().getMonthValue());
        int totalDiasDelMes = mesDelAno.lengthOfMonth();

        ArrayList<LocalDate> dias = new ArrayList<LocalDate>();
        for (int i = 1; i <= totalDiasDelMes; i++) {
            dias.add(mesDelAno.atDay(i));
        }

        return Fecha.diaES(dias.get(diaABuscar - 1).getDayOfWeek());
    }

    private static RespuestaMB notificaEmailPP(ContextoMB contexto, Solicitud solicitud) {
        Boolean esAdelanto = contexto.parametros.bool("esAdelanto", false) && MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_adelanto_bh");
        Boolean mejorarOferta = contexto.parametros.bool("mejorarOferta", false);

        BigDecimal montoAprobado = contexto.sesion().montoPrestamoAprobado();
        String cuenta = contexto.sesion().cuentaPrestamoAprobado();

        if (solicitud.esAdelanto() && esAdelanto && MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_adelanto_bh")) {
            contexto.parametros.set("monto", montoAprobado);
            contexto.parametros.set("cuenta", cuenta);
            return MBNotificaciones.envioEmailDesembolsoAdelanto(contexto);
        }

        if (mejorarOferta && solicitud.Estado.equalsIgnoreCase("AA")) {
            return null;
        }

        return MBNotificaciones.envioEmailPP(contexto);
    }

    private static Boolean tieneParametrosCanalAmarillo(ContextoMB contexto) {
        String idSituacionLaboral = contexto.parametros.string("idSituacionLaboral");
        String ingresoNeto = contexto.parametros.string("ingresoNeto");
        String categoriaMonotributista = contexto.parametros.string("letra", "");
        Boolean mejorarOferta = contexto.parametros.bool("mejorarOferta", false);

        if (!mejorarOferta || !ConfigMB.bool("prendido_canal_amarillo_pp")) {
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

    public static RespuestaMB eliminaSolicitudFront(ContextoMB contexto) {
        String idSolicitud = contexto.parametros.string("idSolicitud");
        Boolean insertarDatos = SqlPrestamos.insertEliminaSolicitud(contexto, idSolicitud);

        if (insertarDatos) {
            return RespuestaMB.exito();
        }

        return RespuestaMB.error();
    }

    public static boolean esSolicitudEliminada(ContextoMB contexto, String idSolicitud) {
        Objeto eliminaSolicitud = SqlPrestamos.selectEliminaSolicitud(contexto, idSolicitud);

        if (eliminaSolicitud != null) {
            return true;
        }

        return false;
    }

    private static Boolean tieneAdelantoRecurrente(ContextoMB contexto) {
        Boolean tieneAdelantoRecurrente = false;
        for (Prestamo prestamo : contexto.prestamos()) {
            if (prestamo.detalle().hayError()) {
                AuditorLogService.prestamosLogVisualizador(contexto, "API-Ventas_ConsolidadaPrestamos", null, prestamo.detalle().json);
                return false;
            }

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


