package ar.com.hipotecario.canal.homebanking.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Fecha;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Pdf;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.negocio.PlazoFijoLogro;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaCredito;
import ar.com.hipotecario.canal.homebanking.servicio.*;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;

import static ar.com.hipotecario.canal.homebanking.negocio.Constantes.SEPARADOR_COMPROBANTE_AMEX;

public class HBArchivo {

    public static byte[] comprobante(ContextoHB contexto) {
        String id = contexto.parametros.string("id");
        contexto.responseHeader("Content-Type", "application/pdf; name=Comprobante HB.pdf");

        if (id.contains(SEPARADOR_COMPROBANTE_AMEX))
            return generarComprobanteMultiple(contexto);

        String template = id.split("_")[0];
        Map<String, String> parametros = contexto.sesion.comprobantes.get(id);

        if (template.equals("debinRecurrente")) {
            return comprobanteDebinRecurrente(contexto, id);
        }

        if (template.equals("debin")) {
            String idDebin = id.split("_")[1];
            ApiResponse detalle = RestDebin.detalleDebin(contexto, idDebin);
            if (detalle.hayError()) {
                throw new RuntimeException();
            }

            parametros = new HashMap<>();
            parametros.put("ID", idDebin);
            parametros.put("VENDEDOR_CBU", detalle.string("vendedor.cliente.cuenta.cbu"));
            parametros.put("VENDEDOR_ALIAS", detalle.string("vendedor.cliente.cuenta.alias"));
            parametros.put("VENDEDOR_CUIT", detalle.string("vendedor.cliente.idTributario"));
            parametros.put("VENDEDOR_NOMBRE", detalle.string("vendedor.cliente.nombreCompleto").trim());
            parametros.put("COMPRADOR_CBU", detalle.string("comprador.cliente.cuenta.cbu"));
            parametros.put("COMPRADOR_ALIAS", detalle.string("comprador.cliente.cuenta.alias"));
            parametros.put("COMPRADOR_CUIT", detalle.string("comprador.cliente.idTributario"));
            parametros.put("COMPRADOR_NOMBRE", detalle.string("comprador.cliente.nombreCompleto").trim());

            parametros.put("IMPORTE", (detalle.string("detalle.moneda.id").equals("2") ? "USD" : "$") + " " + Formateador.importe(detalle.bigDecimal("detalle.importe")));
            parametros.put("FECHA_HORA", detalle.date("fechaNegocio", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
            parametros.put("CONCEPTO", TransferenciaService.conceptos().get(detalle.string("detalle.concepto")));
            parametros.put("DESCRIPCION", detalle.string("detalle.descripcion"));
            parametros.put("ESTADO", detalle.string("estado.codigo"));
			
        }

        return Pdf.generar(template, parametros);
    }

    public static Respuesta terminosCondiciones(ContextoHB contexto) {
        Respuesta respuesta = new Respuesta();

        ApiResponse response = RestArchivo.digitalizacionGetDocumentos(contexto);
        if (response.hayError()) {
            return Respuesta.error();
        }

        Objeto documentos = new Objeto();

        // Quitar documentos duplicados en base a la fecha
//		for (Objeto documento : response.objetos()) {
//			String nroTramiteWKF = documento.string("nroTramiteWKF");
//			Date fechaCreacion = documento.date("fechaCreacion", "yyyy-MM-dd'T'HH:mm:ss.sss");
//			if (fechaCreacion != null && !nroTramiteWKF.isEmpty()) {
//				Boolean existeMasNuevo = false;
//				for (Objeto documentoB : response.objetos()) {
//					if (nroTramiteWKF.equals(documentoB.string("nroTramiteWKF"))) {
//						existeMasNuevo |= fechaCreacion.getTime() < documentoB.date("fechaCreacion", "yyyy-MM-dd'T'HH:mm:ss.sss").getTime();
//					}
//				}
//				if (existeMasNuevo) {
//					continue;
//				}
//			}
//			documentos.add(documento);
//		}

        // Quitar documentos duplicados en base al orden
        Set<String> numerosTramiteWorkflow = new HashSet<>();
        List<Objeto> datos = response.objetos();
        Collections.reverse(datos);
        for (Objeto documento : datos) {
            String nroTramiteWKF = documento.string("nroTramiteWKF");
            String clave = nroTramiteWKF + documento.string("descripcionClase");
            if (!numerosTramiteWorkflow.contains(clave) || "".equals(nroTramiteWKF)) {
                documentos.add(documento);
            }
            numerosTramiteWorkflow.add(clave);
        }

        for (Objeto documento : documentos.ordenar("tituloDocumento").objetos()) {
            // Caja Ahorro
            if (documento.string("id").startsWith("FormOrigHBCA") || documento.string("descripcionClase").equals("FormOrigHBCA")) {
                Objeto item = new Objeto();
                item.set("id", documento.string("id"));
                item.set("tipo", "Caja Ahorro");
                item.set("descripcion", documento.string("tituloDocumento"));
                item.set("canal", traducirNombreCanalTyc(documento.string("canal").equals("") ? "N/D" : documento.string("canal")));
                item.set("fecha", documento.date("fechaCreacion", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
                item.set("solicitarDocumentacionDigital", false);
                respuesta.add("documentos", item);
            }
            if (documento.string("producto").equals("CA")) {
                if (documento.string("id").startsWith("FormOrigTyC") || documento.string("descripcionClase").equals("FormOrigTyC")) {
                    Objeto item = new Objeto();
                    item.set("id", documento.string("id"));
                    item.set("tipo", "Caja Ahorro");
                    item.set("descripcion", documento.string("tituloDocumento"));
                    item.set("canal", traducirNombreCanalTyc(documento.string("canal").equals("") ? "N/D" : documento.string("canal")));
                    item.set("fecha", documento.date("fechaCreacion", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
                    item.set("solicitarDocumentacionDigital", false);
                    respuesta.add("documentos", item);
                }
            }

            // Prestamos Personales
            if (documento.string("id").startsWith("FormOrigHBPP") || documento.string("descripcionClase").equals("FormOrigHBPP")) {
                Objeto item = new Objeto();
                item.set("id", documento.string("id"));
                item.set("tipo", "Préstamo Personal");
                item.set("descripcion", documento.string("tituloDocumento"));
                item.set("canal", traducirNombreCanalTyc(documento.string("canal").equals("") ? "N/D" : documento.string("canal")));
                item.set("fecha", documento.date("fechaCreacion", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
                item.set("solicitarDocumentacionDigital", false);
                respuesta.add("documentos", item);
            }

            // Paquetes
            if (documento.string("id").startsWith("FormOrigPaquete") || documento.string("descripcionClase").equals("FormOrigPaquete")) {
                Objeto item = new Objeto();
                item.set("id", documento.string("id"));
                item.set("tipo", "Paquete de productos");
                item.set("descripcion", documento.string("tituloDocumento"));
                item.set("canal", traducirNombreCanalTyc(documento.string("canal").equals("") ? "N/D" : documento.string("canal")));
                item.set("fecha", documento.date("fechaCreacion", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
                item.set("solicitarDocumentacionDigital", false);
                respuesta.add("documentos", item);
            }
            if (documento.string("producto").equals("PAQ")) {
                if (documento.string("id").startsWith("FormOrigTyC") || documento.string("descripcionClase").equals("FormOrigTyC")) {
                    Objeto item = new Objeto();
                    item.set("id", documento.string("id"));
                    item.set("tipo", "Paquete de productos");
                    item.set("descripcion", documento.string("tituloDocumento"));
                    item.set("canal", traducirNombreCanalTyc(documento.string("canal").equals("") ? "N/D" : documento.string("canal")));
                    item.set("fecha", documento.date("fechaCreacion", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
                    item.set("solicitarDocumentacionDigital", false);
                    respuesta.add("documentos", item);
                }
            }

            // Sueldo Duho
            if (documento.string("producto").equals("SueldoDUO")) {
                if (documento.string("id").startsWith("FormOrigTyC") || documento.string("descripcionClase").equals("FormOrigTyC")) {
                    Objeto item = new Objeto();
                    item.set("id", documento.string("id"));
                    item.set("tipo", "Sueldo Dúho");
                    item.set("descripcion", documento.string("tituloDocumento"));
                    item.set("canal", traducirNombreCanalTyc(documento.string("canal").equals("") ? "N/D" : documento.string("canal")));
                    item.set("fecha", documento.date("fechaCreacion", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
                    item.set("solicitarDocumentacionDigital", false);
                    respuesta.add("documentos", item);
                }
            }

            // Procrear refaccion
//			if (documento.string("producto").equals("PROCREFAC1") || documento.string("producto").equals("PROCREFAC2")) {
//				if (documento.string("id").startsWith("FormOrigTyC") || documento.string("descripcionClase").equals("FormOrigTyC")) {
//					Objeto item = new Objeto();
//					item.set("id", documento.string("id"));
//					item.set("tipo", "Procrear Refacción");
//					item.set("descripcion", documento.string("tituloDocumento"));
//					item.set("canal", traducirNombreCanalTyc(documento.string("canal").equals("") ? "N/D" : documento.string("canal")));
//					item.set("fecha", documento.date("fechaCreacion", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
//					respuesta.add("documentos", item);
//				}
//			}

            // Procrear Materiales
            if (documento.string("producto").equals("PPPROMATE2")) {
                if (documento.string("id").startsWith("FormOrigTyC") || documento.string("descripcionClase").equals("FormOrigTyC")) {
                    Objeto item = new Objeto();
                    item.set("id", documento.string("id"));
                    item.set("tipo", "Mejoramiento Materiales");
                    item.set("descripcion", documento.string("tituloDocumento"));
                    item.set("canal", traducirNombreCanalTyc(documento.string("canal").equals("") ? "N/D" : documento.string("canal")));
                    item.set("fecha", documento.date("fechaCreacion", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
                    item.set("solicitarDocumentacionDigital", false);
                    respuesta.add("documentos", item);
                }
            }

            // Procrear
            if (documento.string("producto").equals("GREFA2021")) {
                if (documento.string("id").startsWith("FormOrigTyC") || documento.string("descripcionClase").equals("FormOrigTyC")) {
                    Objeto item = new Objeto();
                    item.set("id", documento.string("id"));
                    item.set("tipo", "Préstamo Procrear");
                    item.set("descripcion", documento.string("tituloDocumento"));
                    item.set("canal", traducirNombreCanalTyc(documento.string("canal").equals("") ? "N/D" : documento.string("canal")));
                    item.set("fecha", documento.date("fechaCreacion", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
                    item.set("solicitarDocumentacionDigital", false);
                    respuesta.add("documentos", item);
                }
            }

            // Generico (cuenta comiente, cuenta cuotapartista, etc)
            if (documento.string("id").equalsIgnoreCase("Formulario") || documento.string("descripcionClase").equalsIgnoreCase("Formulario")) {
                Objeto item = new Objeto();
                item.set("id", documento.string("id"));
                item.set("tipo", traducirTipoTyc(documento.string("producto")));
                item.set("descripcion", documento.string("tituloDocumento"));
                item.set("canal", traducirNombreCanalTyc(documento.string("canal").equals("") ? "N/D" : documento.string("canal")));
                item.set("fecha", documento.date("fechaCreacion", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
                item.set("solicitarDocumentacionDigital", true);
                if (documento.string("producto").isEmpty()) {
                    try {
                        String numeroSolicitud = documento.objeto("nroSolicitud").toList().get(0).toString();
                        ApiResponse solicitud = RestOmnicanalidad.consultarSolicitud(contexto, numeroSolicitud);
                        for (Objeto producto : solicitud.objetos("Datos").get(0).objetos("Productos")) {
                            if (producto.string("Nemonico").equals("PROREFHOG1") || producto.string("Nemonico").equals("PROREFHOG2")) {
                                item.set("tipo", "Préstamo Procrear");
                            }
                        }
                    } catch (Exception e) {
                    }
                }
                respuesta.add("documentos", item);
            }
        }

        // Plazo Fijo Logros
        for (PlazoFijoLogro plazoFijoLogro : contexto.plazosFijosLogros()) {
            try {
                String fecha = "2018-09-06";
                fecha = plazoFijoLogro.fechaAlta().after(new SimpleDateFormat("yyyy-MM-dd").parse("2018-12-28")) ? "2018-12-28" : fecha;
                fecha = plazoFijoLogro.fechaAlta().after(new SimpleDateFormat("yyyy-MM-dd").parse("2019-03-08")) ? "2019-03-08" : fecha;

                Objeto item = new Objeto();
                item.set("id", "plazo-fijo-logro-" + fecha);
                item.set("tipo", "Plazo Fijo Logro: " + plazoFijoLogro.nombre());
                item.set("descripcion", "Plazo Fijo Logro");
                item.set("canal", traducirNombreCanalTyc(plazoFijoLogro.canal().equals("") ? "N/D" : plazoFijoLogro.canal()));
                item.set("fecha", plazoFijoLogro.fechaAlta("dd/MM/yyyy"));
                respuesta.add("documentos", item);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        Objeto items = respuesta.objeto("documentos");
        respuesta.set("documentos", Fecha.ordenarPorFechaDesc(items, "fecha", "dd/MM/yyyy"));

        return respuesta;
    }

    public static String traducirTipoTyc(String tipo) {
        tipo = (tipo == null) ? "" : tipo;
        tipo = tipo.equals("PAQ") ? tipo.replace("PAQ", "Paquete de productos") : tipo;
        return tipo;
    }

    public static String traducirNombreCanalTyc(String canal) {
        canal = (canal == null) ? "" : canal;
        canal = canal.equals("TAS") ? canal.replace("TAS", "Terminal de autoservicio") : canal;
        canal = canal.equals("HB") ? canal.replace("HB", "Home Banking") : canal;
        canal = canal.equals("BB") ? canal.replace("BB", "Buho Bank") : canal;
        canal = canal.equals("TMK") ? canal.replace("TMK", "Telemarketing") : canal;
        canal = canal.equals("MB") ? canal.replace("MB", "Mobile Banking") : canal;
        return canal;
    }

    public static byte[] archivoDigitalizado(ContextoHB contexto) {
        String id = contexto.parametros.string("id");

        if (Objeto.anyEmpty(id)) {
            return null;
        }

        if (id.startsWith("plazo-fijo-logro-")) {
            ByteArrayOutputStream buffer = null;
            try {
                InputStream is = ConfigHB.class.getResourceAsStream("/terminosycondiciones/" + id + ".pdf");
                buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            byte[] archivo = buffer.toByteArray();
            contexto.responseHeader("Content-Type", "application/pdf; name=" + id + ".pdf");
            return archivo;
        }

        if (id.equals("CuentaCuotapartistaF2704")) {
            ByteArrayOutputStream buffer = null;
            try {
                InputStream is = ConfigHB.class.getResourceAsStream("/terminosycondiciones/F2704_Constancia_reglamento_FCI_Banco_de_Valores.pdf");
                buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            byte[] archivo = buffer.toByteArray();
            contexto.responseHeader("Content-Type", "application/pdf; name=" + id + ".pdf");
            return archivo;
        }

        if (id.equals("CuentaCuotapartistaF2705")) {
            ByteArrayOutputStream buffer = null;
            try {
                InputStream is = ConfigHB.class.getResourceAsStream("/terminosycondiciones/F2705_Constancia_reglamento_FCI_Deutsche_Bank.pdf");
                buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            byte[] archivo = buffer.toByteArray();
            contexto.responseHeader("Content-Type", "application/pdf; name=" + id + ".pdf");
            return archivo;
        }

        if (id.equals("CuentaComitente")) {
            ByteArrayOutputStream buffer = null;
            try {
                InputStream is = ConfigHB.class.getResourceAsStream("/terminosycondiciones/CuentaComitente.pdf");
                buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            byte[] archivo = buffer.toByteArray();
            contexto.responseHeader("Content-Type", "application/pdf; name=" + id + ".pdf");
            return archivo;
        }

        if (id.startsWith("inversor-comisiones")) {
            ByteArrayOutputStream buffer = null;
            try {
                InputStream is = ConfigHB.class.getResourceAsStream("/cuentainversor/F2784_Comisiones_cargos_y_tasas_cta_comitente_042024_v1.pdf");
                buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            byte[] archivo = buffer.toByteArray();
            contexto.responseHeader("Content-Type", "application/pdf; name=F2784_Comisiones_y_tasas_para_cuenta_comitente.pdf");
            return archivo;
        }

        if (id.startsWith("inversor-solicitud-apertura")) {
            ByteArrayOutputStream buffer = null;
            try {
                InputStream is = ConfigHB.class.getResourceAsStream("/cuentainversor/F2786_Solicitud_Apertura_Cuenta_Comitente.pdf");
                buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            byte[] archivo = buffer.toByteArray();
            contexto.responseHeader("Content-Type", "application/pdf; name=F2786_Solicitud_Apertura_Cuenta_Comitente.pdf");
            return archivo;
        }

        if (id.startsWith("inversor-constancia-recibo")) {
            ByteArrayOutputStream buffer = null;
            try {
                InputStream is = ConfigHB.class.getResourceAsStream("/cuentainversor/F3004_Constancia_recibo_de_entrega_Reglamento_de_Gestion_BACS_02_2022.pdf");
                buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            byte[] archivo = buffer.toByteArray();
            contexto.responseHeader("Content-Type", "application/pdf; name=F3004_Constancia_recibo_de_entrega_Reglamento_de_Gestion_BACS_02_2022.pdf");
            return archivo;
        }

        if (id.equals("terminosycondiciones")) {
            ByteArrayOutputStream buffer = null;
            try {
                InputStream is = ConfigHB.class.getResourceAsStream("/terminosycondiciones/F3507-T&C.pdf");
                buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            byte[] archivo = buffer.toByteArray();
            contexto.responseHeader("Content-Type", "application/pdf; name=" + id + ".pdf");
            return archivo;
        }

        ApiResponse response = RestArchivo.digitalizacionGetArchivo(contexto, id);
        String base64 = response.string("bytesDocumento");
        byte[] archivo = Base64.getDecoder().decode(base64);
        try {
            archivo = Base64.getDecoder().decode(new String(archivo));
        } catch (Exception e) {
        }
        contexto.responseHeader("Content-Type", response.string("propiedades.MimeType", "application/pdf") + "; name=" + id + "." + response.string("propiedades.ExtArchivo", "pdf"));
        return archivo;
    }

    public static Respuesta simularLibreDeuda(ContextoHB contexto) {
        String idProducto = contexto.parametros.string("idProducto");

        if (Objeto.anyEmpty(idProducto)) {
            return Respuesta.parametrosIncorrectos();
        }

        Cuenta cuenta = contexto.cuenta(idProducto);
        if (cuenta != null && cuenta.esCuentaCorriente()) {
            if (cuenta.saldo().longValue() < 0L) {
                return Respuesta.estado("DEUDA");
            }

            Objeto parametros = new Objeto();
            parametros.add(new Objeto().set("key", "NOMBRE_PERSONA").set("value", contexto.persona().nombreCompleto()));
            parametros.add(new Objeto().set("key", "NUMERO_CUENTA").set("value", cuenta.numero()));
            parametros.add(new Objeto().set("key", "FECHA").set("value", new SimpleDateFormat("dd/MM/yyyy").format(new Date())));
            parametros.add(new Objeto().set("key", "DNI_PERSONA").set("value", contexto.persona().numeroDocumento()));

            ApiRequest request = Api.request("LibreDeudaCuentaCorriente", "comprobantes", "POST", "/v1/comprobante/{idComprobante}", contexto);
            request.path("idComprobante", ConfigHB.string("id_comprobante_cc"));
            request.body("parametros", parametros);

            ApiResponse response = Api.response(request, contexto.idCobis(), cuenta.numero());
            if (response.hayError()) {
                return Respuesta.error();
            }
            return Respuesta.exito();
        }

        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idProducto);
        if (tarjetaCredito != null) {
            ApiRequest requestMora = Api.request("Moras", "moras", "GET", "/v1/productosEnMora", contexto);
            requestMora.query("idClienteCobis", contexto.idCobis());
            ApiResponse responseMora = Api.response(requestMora, contexto.idCobis());
            if (!responseMora.hayError()) {
                for (Objeto item : responseMora.objetos()) {
                    if (item.string("pro_cod").trim().equals("203")) {
                        return Respuesta.estado("DEUDA");
                    }
                }
            } else {
                return Respuesta.error();
            }

            Objeto parametros = new Objeto();
            parametros.add(new Objeto().set("key", "NOMBRE_PERSONA").set("value", contexto.persona().nombreCompleto()));
            parametros.add(new Objeto().set("key", "NUMERO_TARJETA").set("value", tarjetaCredito.numero()));
            parametros.add(new Objeto().set("key", "FECHA").set("value", new SimpleDateFormat("dd/MM/yyyy").format(new Date())));
            parametros.add(new Objeto().set("key", "DNI_PERSONA").set("value", contexto.persona().numeroDocumento()));

            ApiRequest request = Api.request("LibreDeudaTarjetaCredito", "comprobantes", "POST", "/v1/comprobante/{idComprobante}", contexto);
            request.path("idComprobante", ConfigHB.string("id_comprobante_tc"));
            request.body("parametros", parametros);

            ApiResponse response = Api.response(request, contexto.idCobis(), tarjetaCredito.numero());
            if (response.hayError()) {
                return Respuesta.error();
            }
            return Respuesta.exito();
        }

        return Respuesta.estado("PRODUCTO_NO_ENCONTRADO");
    }

    public static byte[] libreDeuda(ContextoHB contexto) {
        String idProducto = contexto.parametros.string("idProducto");

        if (Objeto.anyEmpty(idProducto)) {
            return null;
        }

        Cuenta cuenta = contexto.cuenta(idProducto);
        if (cuenta != null && cuenta.esCuentaCorriente()) {
            if (cuenta.saldo().longValue() < 0L) {
                return null;
            }

            Objeto parametros = new Objeto();
            parametros.add(new Objeto().set("key", "NOMBRE_PERSONA").set("value", contexto.persona().nombreCompleto()));
            parametros.add(new Objeto().set("key", "NUMERO_CUENTA").set("value", cuenta.numero()));
            parametros.add(new Objeto().set("key", "FECHA").set("value", new SimpleDateFormat("dd/MM/yyyy").format(new Date())));
            parametros.add(new Objeto().set("key", "DNI_PERSONA").set("value", contexto.persona().numeroDocumento()));

            ApiRequest request = Api.request("LibreDeudaCuentaCorriente", "comprobantes", "POST", "/v1/comprobante/{idComprobante}", contexto);
            request.path("idComprobante", ConfigHB.string("id_comprobante_cc"));
            request.body("parametros", parametros);

            ApiResponse response = Api.response(request, contexto.idCobis(), cuenta.numero());
            String base64 = response.string("comprobante");
            byte[] archivo = Base64.getDecoder().decode(base64);
            try {
                archivo = Base64.getDecoder().decode(new String(archivo));
            } catch (Exception e) {
            }
            contexto.responseHeader("Content-Type", "application/pdf; name=LibreDeuda-" + cuenta.numero() + ".pdf");
            return archivo;
        }

        TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idProducto);
        if (tarjetaCredito != null) {
            ApiRequest requestMora = Api.request("Moras", "moras", "GET", "/v1/productosEnMora", contexto);
            requestMora.query("idClienteCobis", contexto.idCobis());
            ApiResponse responseMora = Api.response(requestMora, contexto.idCobis());
            if (!responseMora.hayError()) {
                for (Objeto item : responseMora.objetos()) {
                    if (item.string("pro_cod").trim().equals("203")) {
                        return null;
                    }
                }
            }

            Objeto parametros = new Objeto();
            parametros.add(new Objeto().set("key", "NOMBRE_PERSONA").set("value", contexto.persona().nombreCompleto()));
            parametros.add(new Objeto().set("key", "NUMERO_TARJETA").set("value", tarjetaCredito.numero()));
            parametros.add(new Objeto().set("key", "FECHA").set("value", new SimpleDateFormat("dd/MM/yyyy").format(new Date())));
            parametros.add(new Objeto().set("key", "DNI_PERSONA").set("value", contexto.persona().numeroDocumento()));

            ApiRequest request = Api.request("LibreDeudaTarjetaCredito", "comprobantes", "POST", "/v1/comprobante/{idComprobante}", contexto);
            request.path("idComprobante", ConfigHB.string("id_comprobante_tc"));
            request.body("parametros", parametros);

            ApiResponse response = Api.response(request, contexto.idCobis(), tarjetaCredito.numero());
            String base64 = response.string("comprobante");
            byte[] archivo = Base64.getDecoder().decode(base64);
            try {
                archivo = Base64.getDecoder().decode(new String(archivo));
            } catch (Exception e) {
            }
            contexto.responseHeader("Content-Type", "application/pdf; name=LibreDeuda-" + tarjetaCredito.numero() + ".pdf");
            return archivo;
        }

        return null;
    }

    public static Respuesta descargaAdjunto(ContextoHB contexto) {
        Respuesta respuesta = new Respuesta();
        try {
            String idDoc = contexto.parametros.string("idDoc");
            if (idDoc.isEmpty()) {
                return Respuesta.parametrosIncorrectos();
            }

            idDoc = "{" + idDoc + "}";

            ApiResponse response = RestArchivo.digitalizacionGetArchivo(contexto, idDoc);

            String base64 = response.string("bytesDocumento");
            byte[] archivo = Base64.getDecoder().decode(base64);
            try {
                archivo = Base64.getDecoder().decode(new String(archivo));
            } catch (Exception e) {
            }

            contexto.responseHeader("Content-Type", response.string("propiedades.MimeType", "application/pdf") + "; name=" + response.string("propiedades.Name", "docName") + "." + response.string("propiedades.ExtArchivo", "pdf"));
            respuesta.set("data", archivo);
            respuesta.set("type", "application/pdf");
            respuesta.set("description", response.string("propiedades.Name", "docName"));
            return respuesta;
        } catch (Exception e) {
            return Respuesta.error();
        }
    }

    private static byte[] generarComprobanteMultiple(ContextoHB contexto) {
        try {
            List<String> idsComprobantes = Arrays.asList(contexto.parametros.string("id").split(SEPARADOR_COMPROBANTE_AMEX));
            List<byte[]> pdfs = new ArrayList<>();

            if (!idsComprobantes.isEmpty()) {
                String template = idsComprobantes.get(0).split("_")[0];
                for (String id : idsComprobantes) {
                    pdfs.add(Pdf.generar(template, contexto.sesion.comprobantes.get(id)));
                }

                return unirPDFsBytes(pdfs);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] unirPDFsBytes(List<byte[]> archivosBytes) throws IOException {
        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (byte[] pdfBytes : archivosBytes) {
            pdfMerger.addSource(new ByteArrayInputStream(pdfBytes));
        }

        pdfMerger.setDestinationStream(outputStream);
        pdfMerger.mergeDocuments(null);

        return outputStream.toByteArray();
    }

    private static byte[] comprobanteDebinRecurrente(ContextoHB contexto, String id) {
        String template = id.split("_")[0];
        Map<String, String> parametros = contexto.sesion.comprobantes.get(id);
        contexto.responseHeader("Content-Type", "application/pdf; name=Comprobante BH.pdf");

        if (template.equals("debinRecurrente")) {
            String idDebin = id.split("_")[1];

            //TODO: ACA busqueda de la recurrecia a aceptar/rechazar.
            Date fechaDesde = new Date("01/01/1900");
            Date currentDate = new Date(System.currentTimeMillis());

            Futuro<ApiResponse> responseLista = new Futuro<>(() -> RestDebin.consultaRecurrencias(contexto, fechaDesde, currentDate));

            Objeto objRecurrencia = new Objeto();
            for (Objeto item : responseLista.get().objetos("result.recurrencia")) {
                if (item.string("id").equals(idDebin)) {
                    objRecurrencia = item;
                }
            }
            //TODO: FIN BUSQUEDA

            String documento = objRecurrencia.string("vendedor.cuit").substring(2, 10).replaceFirst("^0+", "");
            String nombreVendedor = "";
            String aliasComprador = "";
            String nombreComprador = "";

            Futuro<ApiResponse> responsePersona = new Futuro<>(() -> RestPersona.consultarPersonaCuitPadron(contexto, documento));
            Objeto finalObjRecurrencia = objRecurrencia;
            Futuro<ApiResponse> responseCoelsa = new Futuro<>(() -> CuentasService.cuentaCoelsa(contexto, finalObjRecurrencia.string("comprador.cbu")));

            if (!responsePersona.get().hayError()) {
                aliasComprador = responseCoelsa.get().string("aliasValorOriginal");
                nombreComprador = responseCoelsa.get().string("nombreTitular");
            }

            if (!responsePersona.get().hayError()) {
                for (Objeto itemPersona : responsePersona.get().objetos()) {
                    if (itemPersona.string("cuil").equals(objRecurrencia.string("vendedor.cuit")))
                        nombreVendedor = itemPersona.string("apellidoYNombre");
                }
            }

            if (responseLista.get().hayError()) {
                throw new RuntimeException();
            }

            parametros = new HashMap<>();
            parametros.put("ID", idDebin);
            parametros.put("VENDEDOR_CUIT", objRecurrencia.string("vendedor.cuit"));
            parametros.put("VENDEDOR_NOMBRE", nombreVendedor);
            parametros.put("COMPRADOR_CBU", objRecurrencia.string("comprador.cbu"));
            parametros.put("COMPRADOR_ALIAS", aliasComprador);
            parametros.put("COMPRADOR_CUIT", objRecurrencia.string("comprador.cuit"));
            parametros.put("COMPRADOR_NOMBRE", RestCatalogo.banco(objRecurrencia.string("comprador.cbu").substring(0, 3)).toUpperCase());
        }

        return Pdf.generar(template, parametros);
    }
}
