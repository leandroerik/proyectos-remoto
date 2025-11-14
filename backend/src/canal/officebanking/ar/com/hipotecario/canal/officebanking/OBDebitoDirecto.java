package ar.com.hipotecario.canal.officebanking;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Archivo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.recaudaciones.ApiRecaudaciones;
import ar.com.hipotecario.backend.servicio.api.recaudaciones.ConveniosRecaOB;
import ar.com.hipotecario.backend.servicio.api.recaudaciones.RutasOB;
import ar.com.hipotecario.canal.officebanking.dto.ErrorArchivoOB;
import ar.com.hipotecario.canal.officebanking.dto.ErrorGenericoOB;
import ar.com.hipotecario.canal.officebanking.dto.debitoDirecto.ArchivoDebitoDirectoDTO;
import ar.com.hipotecario.canal.officebanking.enums.EnumAccionesOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoDebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto.DebitoDirectoConfigOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto.DebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto.EstadosDebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.util.StringUtil;
import com.azure.storage.blob.BlobClient;

import javax.servlet.ServletException;
import javax.servlet.http.Part;
import javax.sql.rowset.serial.SerialBlob;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;

import static ar.com.hipotecario.canal.officebanking.OBManejoArchivos.blobToString;
import static ar.com.hipotecario.canal.officebanking.util.StringUtil.eliminarCerosIzquierda;
import static ar.com.hipotecario.canal.officebanking.util.StringUtil.reemplazarTXT;

public class OBDebitoDirecto extends ModuloOB {
    final static String GRUPO_CONVENIO = "REBH";

    public static Object consultaCovenios(ContextoOB contexto) {
        List<Objeto> convenios = ApiRecaudaciones.convenioRecaudaciones(contexto, Integer.parseInt(contexto.sesion().empresaOB.idCobis)).get().lista2.stream()
                .filter(con -> con.grupoConvenio.equals(GRUPO_CONVENIO))
                .map(convenio -> {
                    Objeto data = new Objeto();
                    Objeto cuenta = OBCuentas.cuenta(contexto, convenio.numeroCuenta);
                    if (!(empty(cuenta) || cuenta == null)) {
                        data.set("idConvenio", convenio.convenio);
                        data.set("numeroProducto", convenio.numeroCuenta);
                        data.set("saldoGirar", cuenta.get("saldoGirar"));
                        data.set("moneda", cuenta.get("moneda"));
                    }
                    return data;
                }).filter(convenio -> !convenio.isEmpty())
                .toList();
        return respuesta("datos", convenios);
    }

    public static Object precargaDebitoDirecto(ContextoOB contexto) {
        Integer convenio = contexto.parametros.integer("convenio");
        String cuenta = contexto.parametros.string("cuentaOrigen");

        Objeto respuesta = new Objeto();
        Objeto infoArchivo = new Objeto();
        List<String> columnas = new ArrayList<>();
        ErrorArchivoOB errores = new ErrorArchivoOB();
        Objeto obErrores;
        ServicioDebitoDirectoOB servicioDebitoDirecto = new ServicioDebitoDirectoOB(contexto);

        try {
            Part filePart = contexto.request.raw().getPart("archivo");
            Integer numeroPagina = contexto.parametros.integer("numeroPagina", 1);

            int registrosPorPagina = 60;
            InputStream inputStream = filePart.getInputStream();
            Blob archivo = OBManejoArchivos.inputStreamToBlobANSI(inputStream);

            String[] tipoExtensionTXT = filePart.getSubmittedFileName().split("\\.");
            if (!tipoExtensionTXT[1].equalsIgnoreCase("txt")) {
                obErrores = errores.setErroresArchivo(
                        "El archivo tiene un formato inválido",
                        "Sólo aceptamos archivos con la extensión .txt.\r\n"
                                + "Por favor generá el archivo correcto desde el template y subilo nuevamente.", null, null);
                return contexto.response(200, respuesta("ERROR", "datos", obErrores));
            }
            List<DebitoDirectoOB> pago = servicioDebitoDirecto.buscarPorArchivo(filePart.getSubmittedFileName()).get();
            if (pago.size()!=0){
                return errores.setErroresArchivo("Nombre archivo invalido invalido", "Ya se ha ingresado un archivo con este nombre", null, null);
            }
            
            Objeto respuestaValidarNombre = OBValidacionesDebitoDirecto.validarNombreArchivo(filePart.getSubmittedFileName(),convenio.toString());
            String secuencial_nombre_archivo = filePart.getSubmittedFileName().split("_")[3];
            
            if (!respuestaValidarNombre.get("estado").equals("0")) {
                return contexto.response(200, respuestaValidarNombre);
            }
            String contenidoArchivo = blobToString(archivo);
            String cabecera = contenidoArchivo.substring(0, 220);
            String convenioCabecera = eliminarCerosIzquierda(cabecera.substring(1, 6));
            
            List<ConveniosRecaOB.ConvenioReca> respuestaConvenio = ApiRecaudaciones.convenioRecaudaciones(contexto, Integer.valueOf(contexto.sesion().empresaOB.idCobis)).get().lista2;
            ConveniosRecaOB.ConvenioReca convenioOBSeleccionado = respuestaConvenio.stream().filter(c -> c.convenio.toString().equals(convenio.toString())).findFirst().orElse(null);
            if (Optional.ofNullable(convenioOBSeleccionado).isEmpty()) {
                return new ErrorGenericoOB().setErrores("Convenio inválido.", "El convenio seleccionado no existe.");
            }
            // validar convenio que viene en el combo contra el del archivo
            if (convenio == null || convenio != Integer.parseInt(convenioCabecera)) {
                return new ErrorArchivoOB().setErroresArchivo("Convenio inválido.", "El convenio elegido debe coincidir con el del archivo.", 0, "Convenio");
            }

            // validacion de cuenta
            Objeto cuentaDebito = OBCuentas.cuenta(contexto, cuenta);
            if (empty(cuentaDebito) || cuentaDebito == null) {
                return new ErrorGenericoOB().setErrores("Cuenta inválida.", "La cuenta seleccionada no existe.");
            }

            ArchivoDebitoDirectoDTO.Header nuevaCabecera = ArchivoDebitoDirectoDTO.getHeader(contenidoArchivo.trim());
            Objeto respuestaValidarHeader = OBValidacionesDebitoDirecto.validarHeader(nuevaCabecera, convenioOBSeleccionado, secuencial_nombre_archivo);
            if (!respuestaValidarHeader.get("estado").equals("0")) {
                return contexto.response(200, respuestaValidarHeader);
            }

            List<ArchivoDebitoDirectoDTO.Body> listDetalleItem = ArchivoDebitoDirectoDTO.getBody(archivo);
            int numLinea = 1;
            for (ArchivoDebitoDirectoDTO.Body item : listDetalleItem) {
                Objeto resp = OBValidacionesDebitoDirecto.validarDetalle(item, contexto, convenioOBSeleccionado, numLinea);
                if (!resp.get("estado").equals("0")) {
                    return contexto.response(200, resp);
                }
                numLinea++;
            }

            List<ArchivoDebitoDirectoDTO.Body> listaPaginada = getPage(listDetalleItem, numeroPagina, registrosPorPagina);
            BigDecimal sumaImportes = BigDecimal.ZERO;
            for (ArchivoDebitoDirectoDTO.Body item : listDetalleItem) {
                sumaImportes = sumaImportes.add(new BigDecimal(item.importeADebitar));
            }
            int cantidadDeRegistros = 0;
            try {
                for (ArchivoDebitoDirectoDTO.Body item : listaPaginada) {
                    if (!item.filler.startsWith("1")) {
                        Objeto detail = new Objeto();
                        detail.set("IdCliente", item.idActualCliente);
                        detail.set("IdDebito", item.idDebito);
                        detail.set("Servicio", item.servicio);
                        detail.set("FechaVto", item.fechaVencimiento.substring(6, 8) + "/" + item.fechaVencimiento.substring(4, 6) + "/" + item.fechaVencimiento.substring(0, 4));
                        detail.set("cuentaCobis", item.cuentaBancaria);
                        //detail.set("primeros7Cbu", item.tipoCuenta + item.codigoSucursalCuenta);

                        /*
                        String segundoBloqueCbu;
                        if (item.codigoBanco.equals("044")) {
                            segundoBloqueCbu = OBCuentas.cuenta(contexto, item.cuentaBancaria).get("cbu").toString().substring(12);
                        } else segundoBloqueCbu = item.codigoBanco + item.codigoSucursalCuenta;
                        detail.set("segundoBloqueCbu", segundoBloqueCbu);*/

                        //sumaImportes = sumaImportes.add(new BigDecimal(item.importeADebitar));
                        detail.set("importeADebitar", Double.valueOf(item.importeADebitar)/100);
                        detail.set("concepto", item.datosRetorno);

                        respuesta.add("registros", detail);
                        //cantidadDeRegistros++;
                    }
                }

                if (!new BigDecimal(eliminarCerosIzquierda(nuevaCabecera.importeTotal)).equals(sumaImportes)) {
                    return contexto.response(200, respuesta("ERROR", "datos", "El importe total informado no coincide con la suma de los importes de los registros."));
                }

                infoArchivo.set("cantidadRegistros", listDetalleItem.size());
                infoArchivo.set("importeTotal", Double.parseDouble(eliminarCerosIzquierda(nuevaCabecera.importeTotal))/100);
                infoArchivo.set("nombreArchivo", filePart.getSubmittedFileName());
                infoArchivo.set("numeroPagina", numeroPagina);
                infoArchivo.set("paginasTotales", Math.ceil((double) listDetalleItem.size() / registrosPorPagina));
                respuesta.add("informacionArchivo", infoArchivo);

                //Armo listado de nombres de columnas visibles
                ServicioDebitoDirectoConfigOB servicioDebitoDirectoConfig = new ServicioDebitoDirectoConfigOB(contexto);
                columnas.addAll(servicioDebitoDirectoConfig.findAll().get().stream().parallel()
                        .filter(columna -> columna.visible.equals(true))
                        .sorted(Comparator.comparing(DebitoDirectoConfigOB::getPosicion))
                        .map(columna -> columna.nombreColumna).toList());
                respuesta.add("columnas", columnas);

            } catch (Exception e) {
                throw new Exception(e);
            }
        } catch (Exception e) {
            return contexto.response(200, respuesta("ERROR", "datos", e.getMessage()));
        }
        return respuesta("datos", respuesta);
    }

    public static Object cargarDebitoDirecto(ContextoOB contexto) throws SQLException, IOException, ServletException {
        Integer convenio = contexto.parametros.integer("convenio");
        String cuenta = contexto.parametros.string("cuentaOrigen");
        Part filePart = contexto.request.raw().getPart("archivo");

        ServicioDebitoDirectoOB servicioDebitoDirectoOB = new ServicioDebitoDirectoOB(contexto);
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioHistorialDebitoDirectoOB servicioHistorial = new ServicioHistorialDebitoDirectoOB(contexto);

        String connectionString = contexto.config.string("ob_azure_blob_st_url");
        String containerName = contexto.config.string("ob_azure_blob_st_container");
        AzureBlobStorageManager az = new AzureBlobStorageManager(contexto, connectionString, containerName);

        // Obtengo src y grc
        String gcr;
        String scr;
        ConveniosRecaOB resp = ApiRecaudaciones.convenioRecaudaciones(contexto, Integer.parseInt(contexto.sesion().empresaOB.idCobis)).get();
        try {
            ConveniosRecaOB.ConvenioReca convenioReca = resp.lista2.stream().filter(c -> c.convenio.equals(convenio)).findFirst().get();
            gcr = convenioReca.grupoRecaudacion;
            scr = convenioReca.servRecaudacion;
        } catch (Exception e) {
            return new ErrorGenericoOB().setErrores("Convenio inválido.", "El convenio seleccionado no existe.");
        }
        String nombreArchivo = filePart.getSubmittedFileName();

        InputStream inputStream = filePart.getInputStream();
        byte[] archivoBytes = inputStream.readAllBytes();
        String contenido = new String(archivoBytes, Charset.forName("Cp1252"));
        contenido = contenido.replaceAll("ñ", "n")
                .replaceAll("Ñ", "N")
                .replaceAll("á", "a")
                .replaceAll("é", "e")
                .replaceAll("í", "i")
                .replaceAll("ó", "o")
                .replaceAll("ú", "u")
                .replaceAll("Á", "A")
                .replaceAll("É", "E")
                .replaceAll("Í", "I")
                .replaceAll("Ó", "O")
                .replaceAll("Ú", "U")
                .replaceAll("ü", "u")
                .replaceAll("Ü", "U");
        archivoBytes = contenido.getBytes(Charset.forName("Cp1252"));
        Blob archivo = OBManejoArchivos.inputStreamToBlobANSI(new ByteArrayInputStream(archivoBytes));

        SesionOB sesion = contexto.sesion();

        // validar archivo
        String[] tipoExtension = filePart.getSubmittedFileName().split("\\.");

        if (!tipoExtension[1].equalsIgnoreCase("txt")) {
            return respuesta("EXTENSION_ARCHIVO_INVALIDA");
        }

        // Obtengo el contenido del archivo.
        String contenidoArchivo = blobToString(archivo);
        String cabecera = contenidoArchivo.substring(0, 220);
        String convenioCabecera = cabecera.substring(2, 6);

        // validar convenio que viene en el combo contra el del archivo
        if (convenio == null || convenio != Integer.parseInt(convenioCabecera)) {
            return respuesta("CONVENIO_INVALIDO");
        }
        // Validar Cuenta
        Objeto cuentaDebito = OBCuentas.cuenta(contexto, cuenta);
        if (empty(cuentaDebito) || cuentaDebito == null) {
            return respuesta("CUENTA_DEBITO_INVALIDA");
        }

        TipoProductoFirmaOB producto = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.DEBITO_DIRECTO.getCodigo()).get();

        ArchivoDebitoDirectoDTO.Header detalleHeader = ArchivoDebitoDirectoDTO.getHeader(cabecera);

        // Toma del archivo cantidad registros
        List<ArchivoDebitoDirectoDTO.Body> listDetalleItem = ArchivoDebitoDirectoDTO.getBody(archivo);
        Integer cantidadRegistros = listDetalleItem.size();

        // Agregar Importe
        BigDecimal importe = BigDecimal.valueOf(Long.parseLong(detalleHeader.importeTotal)/100);

        /*Creo el DD en la tabla DD en BD*/

        DebitoDirectoOB debito = servicioDebitoDirectoOB.crear(contexto, cuenta,
                importe, nombreArchivo, archivo, cantidadRegistros, convenio, producto, gcr, scr).get();

        String pathMaster = contexto.config.string("dd_ruta_master_files");
        nombreArchivo = renombrarArchivoDebitoDirecto(convenio.toString(),debito.nombreArchivo,debito.fechaCreacion.toLocalDate());
        try {

            az.uploadArchivoToAzure(contexto, archivoBytes, pathMaster, nombreArchivo, String.valueOf(debito.id));

        } catch (Exception e) {
            e.printStackTrace();
            return respuesta("ERROR", "descripcion", "Error al cargar el archivo a carpeta master.");
        }
        String pathDestinoBandeja = contexto.config.string("dd_ruta_en_bandeja");
        try {
            az.copyBlob(contexto, pathMaster + nombreArchivo, pathDestinoBandeja + nombreArchivo);
        } catch (Exception e) {
            e.printStackTrace();
            return respuesta("ERROR", "descripcion", "Error al copiar el archivo a carpeta en bandeja");
        }

        BandejaOB bandeja = servicioBandeja.find(debito.id).get();


        EstadoBandejaOB estadoInicialBandeja = servicioEstadoBandeja
                .find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
        AccionesOB accionCrear = servicioAcciones.find(EnumAccionesOB.CREAR.getCodigo()).get();
        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);

        /* Creo el registro en la Bandeja BD */

        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionCrear, estadoInicialBandeja, estadoInicialBandeja);

        servicioHistorial.crear(debito, accionCrear, empresaUsuario);

        contexto.parametros.set("idSolicitudPago", debito.id);


        Objeto debitoDetalle = new Objeto();
        debitoDetalle.set("id", debito.id);
        debitoDetalle.set("monto", debito.monto);
        debitoDetalle.set("cantidadRegistros", debito.cantidadRegistros);
        debitoDetalle.set("cuenta", debito.cuentaOrigen);
        debitoDetalle.set("tipo", debito.tipoProducto);
        debitoDetalle.set("moneda", debito.moneda.simbolo);
        debitoDetalle.set("creadoPor", debito.usuario.nombre + " " + debito.usuario.apellido);

        if (debito.estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo())
            debitoDetalle.set("estado", debito.estado.descripcion);
        else
            debitoDetalle.set("estado", debito.estadoBandeja.descripcion);

        debitoDetalle.set("estadoOperacion", debito.estado.descripcion);

        return respuesta("detalle", debitoDetalle);
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


    public static Object verTablaDD(ContextoOB contexto) throws SQLException, IOException {
        int idOperacion = contexto.parametros.integer("idOperacion");
        int numeroPagina = contexto.parametros.integer("numeroPagina");
        Objeto dato = new Objeto();
        ServicioDebitoDirectoOB servicioDD = new ServicioDebitoDirectoOB(contexto);
        DebitoDirectoOB debito = servicioDD.find(idOperacion).get();

        if (empty(debito) || debito == null) {
            return respuesta("DATOS_INVALIDOS");
        }

        String connectionString = contexto.config.string("ob_azure_blob_st_url");
        String containerName = contexto.config.string("ob_azure_blob_st_container");

        AzureBlobStorageManager az = new AzureBlobStorageManager(contexto, connectionString, containerName);
        Blob archivo;
        LocalDate fechaCambioRenombramientoArchivos = LocalDate.of(2024,8,contexto.esProduccion()?29:28);
        String nombreArchivo = debito.fechaCreacion.toLocalDate().isAfter(fechaCambioRenombramientoArchivos)?renombrarArchivoDebitoDirecto(debito.convenio.toString(),debito.nombreArchivo,debito.fechaCreacion.toLocalDate()):debito.nombreArchivo;
        try {
            BlobClient blobClient = az.findBlobByName(contexto, EnumTipoProductoOB.DEBITO_DIRECTO, nombreArchivo);
            archivo = new SerialBlob(blobClient.downloadContent().toBytes());
        } catch (Exception e) {
            return respuesta("ERROR", "descripcion", "No se encontró el archivo con ese id de bandeja.");
        }
        Integer registrosPorPagina = 60;
        Objeto respuesta = new Objeto();

        List<ArchivoDebitoDirectoDTO.Body> listDetalleItem = ArchivoDebitoDirectoDTO.getBody(archivo);

        List<ArchivoDebitoDirectoDTO.Body> listaPaginada = getPage(listDetalleItem,
                numeroPagina, registrosPorPagina);
        listaPaginada.forEach(item -> {
            Objeto detail = new Objeto();
            detail.set("idCliente", item.idActualCliente);
            detail.set("idDebito", item.idDebito);
            detail.set("servicio", item.servicio);
            detail.set("fechaVto", item.fechaVencimiento);
            detail.set("ctaCobis", item.codigoSucursalCuenta + item.tipoCuenta + item.cuentaBancaria);
            detail.set("importeADebitar", Double.valueOf(item.importeADebitar)/100);
            detail.set("concepto", item.datosRetorno);
            respuesta.add("registros", detail);
        });

        dato.set("nombreArchivo", debito.nombreArchivo);
        dato.set("numeroPagina", numeroPagina);
        dato.set("cantidadRegistros", debito.cantidadRegistros);
        dato.set("paginasTotales", Math.ceil((double) debito.cantidadRegistros / registrosPorPagina));

        respuesta.add("informacionArchivo", dato);

        ServicioDebitoDirectoConfigOB servicioDebitoDirectoConfigOB = new ServicioDebitoDirectoConfigOB(contexto);
        List<String> columnas = new ArrayList<>(servicioDebitoDirectoConfigOB.findAll().get().stream().
                parallel().filter(columna -> columna.visible.equals(true)).sorted(Comparator.comparing(DebitoDirectoConfigOB::getPosicion))
                .map(columna -> columna.nombreColumna).toList());
        respuesta.add("columnas",
                columnas);
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

    public static Object consultaEstados(ContextoOB contexto) {
        ServicioEstadosDebitoDirectoOB servicioEstadoDebitoDirecto = new ServicioEstadosDebitoDirectoOB(contexto);
        List<EstadosDebitoDirectoOB> estadosDebitoDirecto = servicioEstadoDebitoDirecto.findAll().get();

        Objeto datos = new Objeto();
        for (EstadosDebitoDirectoOB estado : estadosDebitoDirecto) {

            if (estado.id != EnumEstadoDebitoDirectoOB.EN_BANDEJA.getCodigo()) {
                Objeto est = new Objeto();
                est.set("id", estado.id);
                est.set("descripcion", estado.descripcion);
                datos.add(est);
            } else {
                Objeto pendiente = new Objeto();
                pendiente.set("id", 2);
                pendiente.set("descripcion", "PENDIENTE FIRMA");
                datos.add(pendiente);

                Objeto parcial = new Objeto();
                parcial.set("id", 4);
                parcial.set("descripcion", "PARCIALMENTE FIRMADA");
                datos.add(parcial);
            }
        }
        return respuesta("datos", datos);
    }

    public static Object historialDebitoDirecto(ContextoOB contexto) {
        Fecha fechaDesde = contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd", null);
        Fecha fechaHasta = contexto.parametros.fecha("fechaHasta", "yyyy-MM-dd", null);
        boolean previsualizacion = contexto.parametros.bool("previsualizacion");
        Integer convenio = contexto.parametros.integer("convenio", null);
        String estadoFiltro = contexto.parametros.string("estado", null);

        ServicioDebitoDirectoOB servicioDebitoDirectoOB = new ServicioDebitoDirectoOB(contexto);
        ServicioEstadosDebitoDirectoOB servicioEstadosDebitoDirectoOB = new ServicioEstadosDebitoDirectoOB(contexto);
        EstadosDebitoDirectoOB estadosDebitoDirecto = null;

        if (estadoFiltro != null) {
            estadoFiltro = estadoFiltro.replaceAll(" ", "_");

            estadosDebitoDirecto = servicioEstadosDebitoDirectoOB.find(EnumEstadoDebitoDirectoOB.valueOf(estadoFiltro).getCodigo()).get();
        }
        List<DebitoDirectoOB> debitos = servicioDebitoDirectoOB.filtrarMovimientosHistorial(contexto.sesion().empresaOB, fechaDesde, fechaHasta, convenio, estadosDebitoDirecto, previsualizacion).get();
        Objeto respuesta = new Objeto();

        for (DebitoDirectoOB debito : debitos) {
            Objeto datos = new Objeto();
            Objeto estado = new Objeto();
            datos.set("idBandeja", debito.id);
            datos.set("convenio", debito.convenio);
            datos.set("nombreArchivo", debito.nombreArchivo);
            datos.set("fechaCreacion", debito.fechaCreacion.toLocalDate().toString());
            datos.set("estadoOperacion",debito.estado.descripcion);

            if (debito.estado.id == EnumEstadoDebitoDirectoOB.EN_BANDEJA.getCodigo()) {
            	datos.set("estadoOperacion",  debito.estadoBandeja.descripcion);//EnumEstadoDebitoDirectoOB.A_PROCESAR.toString().replaceAll("_", " "));                               
            } else {
                if (debito.estado.id==2){
                    if (seProcesoArchivo(contexto,renombrarArchivoDebitoDirecto(debito.convenio.toString(),debito.nombreArchivo,debito.fechaCreacion.toLocalDate()))){
                      EstadosDebitoDirectoOB estadoProcesado = servicioEstadosDebitoDirectoOB.find(EnumEstadoDebitoDirectoOB.PROCESADO.getCodigo()).get();
                        datos.set("estadoOperacion", estadoProcesado.descripcion);
                        debito.estado = estadoProcesado;
                        servicioDebitoDirectoOB.update(debito);
                    }
                }
            	datos.set("estadoOperacion", debito.estado.descripcion);              
            }
            
            estado.set("id", debito.estadoBandeja.id);
            estado.set("descripcionCorta", debito.estadoBandeja.descripcion);  
            datos.set("estado", estado);
            datos.set("tipoProducto",EnumTipoProductoOB.DEBITO_DIRECTO.getCodigo());
                        
            respuesta.add(datos);
        }
        return respuesta("datos", respuesta);
    }

    public static Object verDetalles(ContextoOB contexto) {
        int idOperacion = contexto.parametros.integer("idOperacion");
        
        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
        ServicioDebitoDirectoOB servicioDebitoDirectoOB = new ServicioDebitoDirectoOB(contexto);
        DebitoDirectoOB debitoDirecto = servicioDebitoDirectoOB.find(idOperacion).get();
        if (empty(debitoDirecto) || debitoDirecto == null) {
            return respuesta("DATOS_INVALIDOS");
        }
        Objeto cuenta = OBCuentas.cuenta(contexto, debitoDirecto.cuentaOrigen);
        if (cuenta == null) {
            return respuesta("DATOS_INVALIDOS");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        Objeto dato = new Objeto();
        dato.set("archivo", debitoDirecto.nombreArchivo);
        dato.set("cantidadRegistros", debitoDirecto.cantidadRegistros);
        dato.set("creadoPor", debitoDirecto.usuario.nombreCompleto());
        dato.set("fechaCreacion", debitoDirecto.fechaCreacion.format(formatter));
        dato.set("convenio", debitoDirecto.convenio);
        dato.set("cuentaOrigen", debitoDirecto.cuentaOrigen);
        dato.set("cbu", cuenta.get("cbu").toString());
        dato.set("bancoOrigen", "Banco Hipotecario");

        Objeto estado = new Objeto();
        if (debitoDirecto.estado.id == EnumEstadoDebitoDirectoOB.EN_BANDEJA.getCodigo()) {
        	dato.set("estadoOperacion", debitoDirecto.estadoBandeja.descripcion);                
        } else {
        	dato.set("estadoOperacion", debitoDirecto.estado.descripcion);              
        }
        
        estado.set("id", debitoDirecto.estadoBandeja.id);
        estado.set("descripcionCorta", debitoDirecto.estadoBandeja.descripcion);  
        dato.set("estado", estado);
        
        BandejaOB bandeja = servicioBandeja.find(idOperacion).get();
        dato.set("firmas", OBFirmas.obtenerDatosFirmantes(contexto, bandeja));

        return respuesta("datos", dato);
    }

    public static Object descargaRendiciones(ContextoOB contexto) {
        Integer convenio = contexto.parametros.integer("convenio");
        String fechaDesde = contexto.parametros.string("fechaDesde");
        String fechaHasta = contexto.parametros.string("fechaHasta");
        Integer producto = contexto.parametros.integer("productoOB");

        Objeto rutasFinales = new Objeto();
        List<String> fechas = new ArrayList<>();
        fechas = obtenerFechasEntre(fechaDesde,fechaHasta);
        if (fechas.size()>5) return respuesta("ERROR","datos","maximo 5 dias habiles");


        ConveniosRecaOB resp = ApiRecaudaciones.convenioRecaudaciones(contexto, Integer.parseInt(contexto.sesion().empresaOB.idCobis)).get();
        ConveniosRecaOB.ConvenioReca convenioReca = null;
        try {
              convenioReca = resp.lista2.stream().filter(c -> c.convenio.equals(convenio)).findFirst().get();
//            RutasOB rutas = ApiRecaudaciones.consultaRutas(contexto, String.valueOf(convenio), convenioReca.grupoRecaudacion, convenioReca.servRecaudacion).get();
//            rutasFinales.set("rutaSalida", rutas.rutaSalida);
//            rutasFinales.set("rutaListado", rutas.rutaListado);
        } catch (Exception e) {
            return new ErrorGenericoOB().setErrores("Convenio inválido.", "El convenio seleccionado no existe.");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
        try {
            LocalDate.parse(fechaDesde, formatter);
        } catch (DateTimeParseException e) {
            return new ErrorGenericoOB().setErrores("Fecha inválida.", "Se debe ingresar una fecha válida con formato AAAA-MM-DD");
        }
        AzureBlobStorageManager az = new AzureBlobStorageManager(contexto);
        try {
            LogOB.evento(contexto,"Descarga de rendiciones","Fecha consultada: "+fechaDesde);

        	OBManejoArchivos.TipoOperacionSamba tipo = producto == EnumTipoProductoOB.DEBITO_DIRECTO.getCodigo() ? OBManejoArchivos.TipoOperacionSamba.DEBITO_DIRECTO : OBManejoArchivos.TipoOperacionSamba.RECAUDACIONES_ABM;
            byte[] zip = az.obtenerListaArchivosPorFecha(contexto,producto == EnumTipoProductoOB.DEBITO_DIRECTO.getCodigo()?contexto.config.string("ob_ruta_consulta_rendiciones_dd"):contexto.config.string("ob_ruta_consulta_rendiciones_ci"),fechas, tipo,convenioReca);
           
            contexto.response.type("application/zip");
            contexto.response.header("Access-Control-Expose-Headers", "Content-Disposition");


            if (zip==null){
                return new ErrorGenericoOB().setErrores("Error al obtener archivo de rendiciones.", "No se encontro el archivo de rendiciones");
            }
            LogOB.evento(contexto,"descargoRendiciones convenio:"+" "+convenio,String.valueOf(zip==null));
            Archivo archivo = new Archivo("rendicion_" + convenio + "_" + fechaDesde +"-"+fechaHasta+ ".zip", zip);
            contexto.response.header("Content-Disposition", "attachment; filename=" + "rendicion_" + convenio + "_" + fechaDesde +"-"+fechaHasta+ ".zip");
            return archivo;		
            
            
        }catch (RuntimeException r){
            return new ErrorGenericoOB().setErrores("No se encontraron archivos.", "No se encontraron archivos en las fechas seleccionadas.");
        }catch (Exception e) {
            return new ErrorGenericoOB().setErrores("Error al obtener archivo de rendiciones.", "No se pudo descargar.");
        }
    }

    public static List<String> obtenerFechasEntre(String fechaInicioStr, String fechaFinStr){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate fechaInicio = LocalDate.parse(fechaInicioStr, formatter);
        LocalDate fechaFin = LocalDate.parse(fechaFinStr, formatter);

        List<String> fechas = new ArrayList<>();
        SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd");

        try{
            while (!fechaInicio.isAfter(fechaFin)) {
                Fecha fecha = new Fecha(formatter2.parse(fechaInicio.toString()));
                if (!fecha.esFinDeSemana()) fechas.add(fechaInicio.toString());
                fechaInicio = fechaInicio.plusDays(1);
            }
        }catch (Exception e){

        }

        return fechas;
    }

    private static boolean seProcesoArchivo(ContextoOB contexto, String nombreArchivo){
        String connectionString = contexto.config.string("ob_azure_blob_st_url");
        String containerName = contexto.config.string("ob_azure_blob_st_container");
        AzureBlobStorageManager az = new AzureBlobStorageManager(contexto, connectionString, containerName);
        try {
            BlobClient blobClient = az.findBlobRecaProcesado(contexto, nombreArchivo,EnumTipoProductoOB.DEBITO_DIRECTO);
            Blob archivo = new SerialBlob(blobClient.downloadContent().toBytes());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String renombrarArchivoDebitoDirecto(String convenio,String nombreArchivo,LocalDate fechaCarga){
        String fechaFormateada = fechaCarga.format(DateTimeFormatter.ofPattern("yyMMdd"));
        Pattern pattern = Pattern.compile("sec_(\\d{3})");
        Matcher matcher = pattern.matcher(nombreArchivo);
        if (!matcher.find()) {
            throw new IllegalArgumentException("El nombre del archivo no contiene la secuencia 'sec_' seguida de tres dígitos.");
        }
        String secuencia = matcher.group(1);
        return "ent" + convenio + "_" + fechaFormateada + "_sec_" + secuencia + ".txt";
    }
}
