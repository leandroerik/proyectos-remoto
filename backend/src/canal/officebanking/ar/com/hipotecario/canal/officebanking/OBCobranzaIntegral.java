package ar.com.hipotecario.canal.officebanking;

import static ar.com.hipotecario.canal.officebanking.OBManejoArchivos.blobToString;
import static ar.com.hipotecario.canal.officebanking.OBManejoArchivos.getPage;
import static ar.com.hipotecario.canal.officebanking.util.StringUtil.eliminarCerosIzquierda;
import static ar.com.hipotecario.canal.officebanking.util.StringUtil.eliminarEspaciosDerecha;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.Part;
import javax.sql.rowset.serial.SerialBlob;

import ar.com.hipotecario.backend.servicio.api.debin.Debin;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EnumEstadosDebinLoteOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.ServicioDebinLoteOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.ServicioHistorialDebinLoteOB;
import ar.com.hipotecario.canal.officebanking.dto.debinLote.ArchivoDebinLoteDTO;
import ar.com.hipotecario.canal.officebanking.jpa.ob.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.DebinLote.DebinLoteConfigOB;
import com.azure.storage.blob.BlobClient;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.recaudaciones.ApiRecaudaciones;
import ar.com.hipotecario.backend.servicio.api.recaudaciones.ConveniosRecaOB;
import ar.com.hipotecario.backend.servicio.api.recaudaciones.DetalleEstadoHabilitacionConveniosOB;
import ar.com.hipotecario.backend.servicio.api.recaudaciones.ReporteCobranzasOB;
import ar.com.hipotecario.canal.officebanking.dto.ErrorArchivoOB;
import ar.com.hipotecario.canal.officebanking.dto.ErrorGenericoOB;
import ar.com.hipotecario.canal.officebanking.dto.cobranzaIntegral.ArchivoCobranzaIntegralDTO;
import ar.com.hipotecario.canal.officebanking.dto.cobranzaIntegral.ReporteCobranzaCSVDTO;
import ar.com.hipotecario.canal.officebanking.dto.cobranzaIntegral.ReporteCobranzaConvenioDTO;
import ar.com.hipotecario.canal.officebanking.enums.EnumAccionesOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoDebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;
import ar.com.hipotecario.canal.officebanking.enums.recaudaciones.EnumTipoPagosReca;
import ar.com.hipotecario.canal.officebanking.enums.recaudaciones.cobranzaIntegral.EnumTipoDocumentoCobranzaIntegral;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EnumEstadosCobranzaIntegralOB;


public class OBCobranzaIntegral extends ModuloOB {

    final static String GRUPO_CONVENIO = "DPPP";

    public static Object consultaConveniosDetalleHabilitacion(ContextoOB contexto) {
        String medioRecaudacion = contexto.parametros.string("medioRecaudacion");

        Objeto convenios = new Objeto();

        if (!medioRecaudacion.equals("E") && !medioRecaudacion.equals("D") && !medioRecaudacion.equals("T")) {
            return new ErrorGenericoOB().setErrores("Medio de recaudación inválido.", "El medio de recaudación debe ser 'E' (Echeq), 'D' (Debin) o 'T' (Transferencia).");
        }

        List<ConveniosRecaOB.ConvenioReca> convenioRecaudaciones = ApiRecaudaciones.convenioRecaudaciones(contexto, Integer.parseInt(contexto.sesion().empresaOB.idCobis)).get().lista2
                .stream().filter(con -> con.grupoConvenio.equals(GRUPO_CONVENIO)).toList();

        for (ConveniosRecaOB.ConvenioReca convenio : convenioRecaudaciones) {
            Objeto data = new Objeto();
            Objeto cuenta = OBCuentas.cuenta(contexto, convenio.numeroCuenta);
            if (cuenta != null) {
                data.set("idConvenio", convenio.convenio);
                data.set("numeroProducto", convenio.numeroCuenta);
                data.set("saldoGirar", cuenta.get("saldoGirar"));
                data.set("moneda", cuenta.get("moneda"));
            
                DetalleEstadoHabilitacionConveniosOB detalle = ApiRecaudaciones.detalleEstadoHabilitacionConvenios(contexto, convenio.convenio, medioRecaudacion).get();
                if (!detalle.isEmpty()) {
                	data.set("estadoEcheq", detalle.get(0).habEcheq);
                	data.set("estadoTransf", detalle.get(0).habTransf);
                	data.set("estadoDebin", detalle.get(0).habDebin);
                	convenios.add(data);
                }
            }
        }

        return respuesta("datos", convenios);
    }


    public static Object precargaCobranzaIntegral(ContextoOB contexto) {
        String cuenta = contexto.parametros.string("cuentaOrigen");

        Objeto respuesta = new Objeto();
        Objeto infoArchivo = new Objeto();
        ErrorArchivoOB errores = new ErrorArchivoOB();
        Objeto obErrores;
        ServicioCobranzaIntegralOB servicioCobranzaIntegralOB = new ServicioCobranzaIntegralOB(contexto);

        try {
            Part filePart = contexto.request.raw().getPart("archivo");
            Integer numeroPagina = contexto.parametros.integer("numeroPagina", 1);

            int registrosPorPagina = 60;
            InputStream inputStream = filePart.getInputStream();
            Blob archivo = OBManejoArchivos.inputStreamToBlobANSI(inputStream);

            Objeto respuestaValidarNombre = OBValidacionesCobranzaIntegral.validarNombreArchivo(filePart.getSubmittedFileName());
            if (!respuestaValidarNombre.get("estado").equals("0")) {
                return contexto.response(200, respuestaValidarNombre);
            }

            String[] tipoExtensionTXT = filePart.getSubmittedFileName().split("\\.");
            if (!tipoExtensionTXT[1].equals("txt")) {
                obErrores = errores.setErroresArchivo(
                        "El archivo tiene un formato inválido",
                        "Sólo aceptamos archivos con la extensión .txt.\r\n"
                                + "Por favor generá el archivo correcto desde el template y subilo nuevamente.", null, null);
                return contexto.response(200, respuesta("ERROR", "datos", obErrores));
            }

            List<CobranzaIntegralOB> cobranza = servicioCobranzaIntegralOB.buscarPorArchivo(filePart.getSubmittedFileName()).get();
            if (cobranza.size() != 0) {
                return errores.setErroresArchivo("Nombre archivo invalido invalido", "Ya se ha ingresado un archivo con este nombre", null, null);
            }
            String contenidoArchivo = blobToString(archivo);


            //validacion cuenta
            Objeto cuentaDebito = OBCuentas.cuenta(contexto, cuenta);
            if (empty(cuentaDebito) || cuentaDebito == null) {
                return new ErrorGenericoOB().setErrores("Cuenta inválida.", "La cuenta seleccionada no existe.");
            }
            ArchivoCobranzaIntegralDTO.Header nuevaCabecera = ArchivoCobranzaIntegralDTO.getHeader(contenidoArchivo.trim());
            Objeto respuestaValidarHeader = OBValidacionesCobranzaIntegral.validarHeader(contexto,nuevaCabecera);
            if (!respuestaValidarHeader.get("estado").equals("0")) {
                return contexto.response(200, respuestaValidarHeader);
            }

            List<ArchivoCobranzaIntegralDTO.Body> listDetalleItem = ArchivoCobranzaIntegralDTO.getBody(archivo);
            int numLinea = 1;
            for (ArchivoCobranzaIntegralDTO.Body item : listDetalleItem) {
                Objeto resp = OBValidacionesCobranzaIntegral.validarDetalle(item, contexto, numLinea, nuevaCabecera.fechaProceso);
                if (!resp.get("estado").equals("0")) {
                    return contexto.response(200, resp);
                }
                numLinea++;
            }
            double sumaImportes = 0;
            int cantidadDeRegistros = listDetalleItem.size();
            for (ArchivoCobranzaIntegralDTO.Body item : listDetalleItem) {
                Objeto detail = new Objeto();
                detail.set("nombreCliente", eliminarEspaciosDerecha(item.nombreDepositante));
                detail.set("numeroCliente", eliminarCerosIzquierda(item.numeroDepositante));
                detail.set("numeroComprobante", eliminarCerosIzquierda(item.numeroComprobante));
                detail.set("codigoServicio", item.codigoServicio);

                for (EnumTipoDocumentoCobranzaIntegral e : EnumTipoDocumentoCobranzaIntegral.values()) {
                    if (e.getCodigo().toString().equals(item.tipoDocumento)) {
                        detail.set("tipoDocumentoCliente", e.name());
                        break;
                    }
                }

                detail.set("numeroDocumentoCliente", eliminarCerosIzquierda(item.numeroDocumento));
                detail.set("fechaDesde", item.fechaDesde.substring(6, 8) + "/" + item.fechaDesde.substring(4, 6) + "/" + item.fechaDesde.substring(0, 4));
                detail.set("fechaHasta", item.fechaHasta.substring(6, 8) + "/" + item.fechaHasta.substring(4, 6) + "/" + item.fechaHasta.substring(0, 4));
                detail.set("importeVto1", "$" + Double.valueOf(item.importe1) / 100);
                detail.set("fechaVto1", item.fechaVencimiento1.substring(6, 8) + "/" + item.fechaVencimiento1.substring(4, 6) + "/" + item.fechaVencimiento1.substring(0, 4));
                detail.set("importeVto2", "$" + Double.valueOf(item.importe2) / 100);
                detail.set("fechaVto2", item.fechaVencimiento2.substring(6, 8) + "/" + item.fechaVencimiento2.substring(4, 6) + "/" + item.fechaVencimiento2.substring(0, 4));
                detail.set("importeVto3", "$" + Double.valueOf(item.importe3) / 100);
                detail.set("fechaVto3", item.fechaVencimiento3.substring(6, 8) + "/" + item.fechaVencimiento3.substring(4, 6) + "/" + item.fechaVencimiento3.substring(0, 4));
                detail.set("claveInterna", eliminarEspaciosDerecha(item.claveInterna));
                detail.set("division", eliminarEspaciosDerecha(item.division));
                detail.set("descripcionDivision", eliminarEspaciosDerecha(item.descripcionDivision));
                detail.set("leyenda1", eliminarEspaciosDerecha(item.leyenda1));
                detail.set("leyenda2", eliminarEspaciosDerecha(item.leyenda2));
                detail.set("leyenda3", eliminarEspaciosDerecha(item.leyenda3));
                detail.set("leyenda4", eliminarEspaciosDerecha(item.leyenda4));
                detail.set("leyenda5", eliminarEspaciosDerecha(item.leyenda5));
                detail.set("leyenda6", eliminarEspaciosDerecha(item.leyenda6));
                detail.set("leyenda7", eliminarEspaciosDerecha(item.leyenda7));
                detail.set("celularCliente", eliminarEspaciosDerecha(item.celularCliente));
                detail.set("mailCliente", eliminarEspaciosDerecha(item.mailCliente));

                respuesta.add("registros", detail);

                sumaImportes += Double.valueOf(item.importe1);
                sumaImportes += Double.valueOf(item.importe2);
                sumaImportes += Double.valueOf(item.importe3);

            }
            infoArchivo.set("cantidadRegistros", cantidadDeRegistros);
            infoArchivo.set("nombreArchivo", filePart.getSubmittedFileName());
            infoArchivo.set("numeroPagina", numeroPagina);
            infoArchivo.set("paginasTotales", Math.ceil((double) cantidadDeRegistros / registrosPorPagina));
            infoArchivo.set("montoTotal", sumaImportes / 100);
            respuesta.add("informacionArchivo", infoArchivo);

            ServicioCobranzaIntegralConfigOB servicioCobranzaIntegralConfigOB = new ServicioCobranzaIntegralConfigOB(contexto);
            List<String> columnas = new ArrayList<>(servicioCobranzaIntegralConfigOB.findAll().get().stream().parallel()
                    .filter(columna -> columna.visible.equals(true))
                    .sorted(Comparator.comparing(CobranzaIntegralConfigOB::getPosicion))
                    .map(columna -> columna.nombreColumna).toList());
            respuesta.add("columnas", columnas);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return respuesta("datos", respuesta);
    }

    public static Objeto precargaDebinLote(ContextoOB contexto){
        String cuenta = contexto.parametros.string("cuentaOrigen");
        String convenio = contexto.parametros.string("convenio");

        Objeto respuesta = new Objeto();
        Objeto infoArchivo = new Objeto();
        ErrorArchivoOB errores = new ErrorArchivoOB();
        Objeto obErrores;
        ServicioDebinLoteOB servicioDebinLoteOB = new ServicioDebinLoteOB(contexto);

        try{
            Part filePart = contexto.request.raw().getPart("archivo");
            Integer numeroPagina = contexto.parametros.integer("numeroPagina", 1);

            int registrosPorPagina = 60;
            InputStream inputStream = filePart.getInputStream();
            Blob archivo = OBManejoArchivos.inputStreamToBlobANSI(inputStream);

            Objeto respuestaValidarNombre = OBValidacionesDebinLote.validarNombreArchivo(filePart.getSubmittedFileName());
            if (!respuestaValidarNombre.get("estado").equals("0")){
                return contexto.response(200, respuestaValidarNombre);
            }

            String[] tipoExtensionTXT = filePart.getSubmittedFileName().split("\\.");
            if (!tipoExtensionTXT[1].equals("txt")) {
                obErrores = errores.setErroresArchivo(
                        "El archivo tiene un formato inválido",
                        "Sólo aceptamos archivos con la extensión .txt.\r\n"
                                + "Por favor generá el archivo correcto desde el template y subilo nuevamente.", null, null);
                return contexto.response(200, respuesta("ERROR", "datos", obErrores));
            }
            List<DebinLoteOB> debinLote = servicioDebinLoteOB.buscarPorArchivo(filePart.getSubmittedFileName()).get();
            if (debinLote.size()!=0){
                return errores.setErroresArchivo("Nombre archivo invalido invalido", "Ya se ha ingresado un archivo con este nombre", null, null);
            }
            String contenidoArchivo = blobToString(archivo);

            Objeto cuentaDebito = OBCuentas.cuenta(contexto, cuenta);
            if (empty(cuentaDebito) || cuentaDebito == null) {
                return new ErrorGenericoOB().setErrores("Cuenta inválida.", "La cuenta seleccionada no existe.");
            }
            ArchivoDebinLoteDTO.Header cabecera = ArchivoDebinLoteDTO.getHeader(contenidoArchivo.trim());
            Objeto respuestaValidarHeader = OBValidacionesDebinLote.validarHeader(cabecera,convenio);
            if (!respuestaValidarHeader.get("estado").equals("0")) {
                return contexto.response(200, respuestaValidarHeader);
            }
            List<ArchivoDebinLoteDTO.Body> listDetalleItem = ArchivoDebinLoteDTO.getBody(archivo);
            int numLinea = 1;
            for (ArchivoDebinLoteDTO.Body item : listDetalleItem){
                Objeto resp = OBValidacionesDebinLote.validarDetalle(item,numLinea,convenio);
                if (!resp.get("estado").equals("0")) {
                    return contexto.response(200, resp);
                }
                numLinea++;
            }
            double sumaImportes = 0;
            int cantidadDeRegistros = listDetalleItem.size();

            for (ArchivoDebinLoteDTO.Body item : listDetalleItem){
                Objeto detail = new Objeto();
                detail.set("NombreCliente",item.nombreApellido);
                detail.set("cbuDebito",item.cuentaBanc);
                detail.set("cuit",item.cuit);
                detail.set("monto",(Double.valueOf(item.importe)/100));
                detail.set("vigencia",item.vigencia);
                detail.set("concepto",item.concepto);
                detail.set("numeroServicio",item.nroServicio);
                detail.set("comprobante",item.idDebito);
                respuesta.add("registros",detail);
                sumaImportes+=Double.parseDouble(item.importe);
            }
            infoArchivo.set("cantidadRegistros",cantidadDeRegistros);
            infoArchivo.set("nombreArchivo",filePart.getSubmittedFileName());
            infoArchivo.set("numeroPagina",numeroPagina);
            infoArchivo.set("paginasTotales",Math.ceil((double) cantidadDeRegistros/registrosPorPagina));
            infoArchivo.set("montoTotal",sumaImportes/100);
            respuesta.add("informacionArchivo",infoArchivo);

            ServicioDebinLoteConfigOB servicioDebinLoteConfigOB = new ServicioDebinLoteConfigOB(contexto);
            List<String> columnas = new ArrayList<>(servicioDebinLoteConfigOB.findAll().get().stream().parallel()
                    .filter(columna->columna.visible.equals(true))
                    .sorted(Comparator.comparing(DebinLoteConfigOB::getPosicion))
                    .map(columna->columna.nombreColumna).toList());
            respuesta.add("columnas",columnas);
        }catch (Exception e){
            return respuesta("ERROR","ERROR",e);
        }
        return respuesta("datos",respuesta);
    }

    public static Objeto cargaDebinLote(ContextoOB contexto) throws ServletException, IOException, SQLException {
        String convenio = contexto.parametros.string("convenio");
        String cuenta = contexto.parametros.string("cuentaOrigen");
        Part filePart = contexto.request.raw().getPart("archivo");

        ServicioDebinLoteOB servicioDebinLoteOB = new ServicioDebinLoteOB(contexto);
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioHistorialDebinLoteOB servicioHistorial = new ServicioHistorialDebinLoteOB(contexto);
        String connectionString = contexto.config.string("ob_azure_blob_st_url");
        String containerName = contexto.config.string("ob_azure_blob_st_container");
        AzureBlobStorageManager az = new AzureBlobStorageManager(contexto, connectionString, containerName);

        ConveniosRecaOB resp = ApiRecaudaciones.convenioRecaudaciones(contexto, Integer.parseInt(contexto.sesion().empresaOB.idCobis)).tryGet();

        String gcr = "";
        try {
            ConveniosRecaOB.ConvenioReca convenioReca = resp.lista2.stream().filter(c -> c.convenio.toString().equals(convenio)).findFirst().get();
            gcr = convenioReca.grupoRecaudacion;
        } catch (Exception e) {
           LogOB.evento(contexto,"Error");
        }
        String nombreArchivo = filePart.getSubmittedFileName();

        InputStream inputStream = filePart.getInputStream();
        byte[] archivoBytes = inputStream.readAllBytes();
        Blob archivo = OBManejoArchivos.inputStreamToBlobANSI(new ByteArrayInputStream(archivoBytes));

        SesionOB sesion = contexto.sesion();

        // validar archivo
        String[] tipoExtension = filePart.getSubmittedFileName().split("\\.");

        if (!tipoExtension[1].equals("txt")) {
            return respuesta("EXTENSION_ARCHIVO_INVALIDA");
        }

        // Obtengo el contenido del archivo.
        String contenidoArchivo = blobToString(archivo);
        ArchivoDebinLoteDTO.Header nuevaCabecera = ArchivoDebinLoteDTO.getHeader(contenidoArchivo.trim());
        Objeto respuestaValidarHeader = OBValidacionesDebinLote.validarHeader(nuevaCabecera,convenio);
        if (!respuestaValidarHeader.get("estado").equals("0")) {
            return contexto.response(200, respuestaValidarHeader);
        }
        // Validar Cuenta
        Objeto cuentaDebito = OBCuentas.cuenta(contexto, cuenta);
        if (empty(cuentaDebito) || cuentaDebito == null) {
            return respuesta("CUENTA_DEBITO_INVALIDA");
        }
        TipoProductoFirmaOB producto = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.DEBIN_LOTE.getCodigo()).get();


        String pathMaster = contexto.config.string("ci_ruta_master_files");

        // Toma del archivo cantidad registros
        List<ArchivoDebinLoteDTO.Body> listDetalleItem = ArchivoDebinLoteDTO.getBody(archivo);
        Integer cantidadRegistros = listDetalleItem.size();

        //calculo importe
        BigDecimal importe = BigDecimal.valueOf(0);
        for (ArchivoDebinLoteDTO.Body item : listDetalleItem) {
            importe = importe.add(new BigDecimal(Integer.parseInt(item.importe) / 100));
        }

        DebinLoteOB debinPorLote = servicioDebinLoteOB.crear(contexto, cuenta, nombreArchivo, archivo, cantidadRegistros, importe.toString(), Integer.valueOf(convenio), producto,gcr).get();
        try {
            az.uploadArchivoToAzure(contexto, archivoBytes, pathMaster, debinPorLote.nombreArchivo, String.valueOf(debinPorLote.id));
        } catch (Exception e) {
            e.printStackTrace();
            return respuesta("ERROR", "descripcion", "Error al cargar el archivo a carpeta master.");
        }
        String pathDestinoBandeja = contexto.config.string("ci_ruta_en_bandeja");
        try {
            az.copyBlob(contexto, pathMaster + debinPorLote.nombreArchivo, pathDestinoBandeja + debinPorLote.nombreArchivo);
        } catch (Exception e) {
            e.printStackTrace();
            return respuesta("ERROR", "descripcion", "Error al copiar el archivo a carpeta en bandeja");
        }
        BandejaOB bandeja = servicioBandeja.find(debinPorLote.id).get();

        EstadoBandejaOB estadoInicialBandeja = servicioEstadoBandeja
                .find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
        AccionesOB accionCrear = servicioAcciones.find(EnumAccionesOB.CREAR.getCodigo()).get();
        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);

        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionCrear, estadoInicialBandeja, estadoInicialBandeja);
        servicioHistorial.crear(debinPorLote, accionCrear, empresaUsuario);

        contexto.parametros.set("idSolicitudPago", debinPorLote.id);

        Objeto debinLoteDetalle = new Objeto();
        debinLoteDetalle.set("id", debinPorLote.id);
        debinLoteDetalle.set("monto", debinPorLote.monto);
        debinLoteDetalle.set("cantidadRegistros", debinPorLote.cantidadRegistros);
        debinLoteDetalle.set("cuenta", debinPorLote.cuentaOrigen);
        debinLoteDetalle.set("tipo", debinPorLote.tipoProducto);
        debinLoteDetalle.set("moneda", debinPorLote.moneda.simbolo);
        debinLoteDetalle.set("creadoPor", debinPorLote.usuario.nombre + " " + debinPorLote.usuario.apellido);

        if (debinPorLote.estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo())
            debinLoteDetalle.set("estado", debinPorLote.estado.descripcion);
        else
            debinLoteDetalle.set("estado", debinPorLote.estadoBandeja.descripcion);

        debinLoteDetalle.set("estadoOperacion", debinPorLote.estado.descripcion);

        return respuesta("detalle", debinLoteDetalle);

    }

    public static Object cargaCobranzaIntegral(ContextoOB contexto) throws ServletException, IOException, SQLException {
        String convenio = contexto.parametros.string("convenio");
        String cuenta = contexto.parametros.string("cuentaOrigen");
        Part filePart = contexto.request.raw().getPart("archivo");

        ServicioCobranzaIntegralOB servicioCobranzaIntegral = new ServicioCobranzaIntegralOB(contexto);
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioHistorialCobranzaIntegralOB servicioHistorial = new ServicioHistorialCobranzaIntegralOB(contexto);
        String connectionString = contexto.config.string("ob_azure_blob_st_url");
        String containerName = contexto.config.string("ob_azure_blob_st_container");
        AzureBlobStorageManager az = new AzureBlobStorageManager(contexto, connectionString, containerName);

        ConveniosRecaOB resp = ApiRecaudaciones.convenioRecaudaciones(contexto, Integer.parseInt(contexto.sesion().empresaOB.idCobis)).get();

        String gcr;
        try {
            ConveniosRecaOB.ConvenioReca convenioReca = resp.lista2.stream().filter(c -> c.convenio.toString().equals(convenio)).findFirst().get();
            gcr = convenioReca.grupoRecaudacion;
        } catch (Exception e) {
            return new ErrorGenericoOB().setErrores("Convenio inválido.", "El convenio seleccionado no existe.");
        }

        String nombreArchivo = filePart.getSubmittedFileName();

        InputStream inputStream = filePart.getInputStream();
        byte[] archivoBytes = inputStream.readAllBytes();
        Blob archivo = OBManejoArchivos.inputStreamToBlobANSI(new ByteArrayInputStream(archivoBytes));

        SesionOB sesion = contexto.sesion();

        // validar archivo
        String[] tipoExtension = filePart.getSubmittedFileName().split("\\.");

        if (!tipoExtension[1].equals("txt")) {
            return respuesta("EXTENSION_ARCHIVO_INVALIDA");
        }

        // Obtengo el contenido del archivo.
        String contenidoArchivo = blobToString(archivo);
        ArchivoCobranzaIntegralDTO.Header nuevaCabecera = ArchivoCobranzaIntegralDTO.getHeader(contenidoArchivo.trim());
        Objeto respuestaValidarHeader = OBValidacionesCobranzaIntegral.validarHeader(contexto,nuevaCabecera);
        if (!respuestaValidarHeader.get("estado").equals("0")) {
            return contexto.response(200, respuestaValidarHeader);
        }
        // Validar Cuenta
        Objeto cuentaDebito = OBCuentas.cuenta(contexto, cuenta);
        if (empty(cuentaDebito) || cuentaDebito == null) {
            return respuesta("CUENTA_DEBITO_INVALIDA");
        }
        TipoProductoFirmaOB producto = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.COBRANZA_INTEGRAL.getCodigo()).get();

        String pathMaster = contexto.config.string("ci_ruta_master_files");

        // Toma del archivo cantidad registros
        List<ArchivoCobranzaIntegralDTO.Body> listDetalleItem = ArchivoCobranzaIntegralDTO.getBody(archivo);
        Integer cantidadRegistros = listDetalleItem.size();

        //calculo importe
        BigDecimal importe = BigDecimal.valueOf(0);
        for (ArchivoCobranzaIntegralDTO.Body item : listDetalleItem) {
            importe = importe.add(new BigDecimal(Integer.parseInt(item.importe1) / 100));
            importe = importe.add(new BigDecimal(Integer.parseInt(item.importe2) / 100));
            importe = importe.add(new BigDecimal(Integer.parseInt(item.importe3) / 100));
        }

        CobranzaIntegralOB cobranza = servicioCobranzaIntegral.crear(contexto, cuenta, nombreArchivo, archivo, cantidadRegistros, importe.toString(), Integer.valueOf(convenio), producto,gcr).get();
        try {
            az.uploadArchivoToAzure(contexto, archivoBytes, pathMaster, cobranza.nombreArchivo, String.valueOf(cobranza.id));
        } catch (Exception e) {
            e.printStackTrace();
            return respuesta("ERROR", "descripcion", "Error al cargar el archivo a carpeta master.");
        }
        String pathDestinoBandeja = contexto.config.string("ci_ruta_en_bandeja");
        try {
            az.copyBlob(contexto, pathMaster + cobranza.nombreArchivo, pathDestinoBandeja + cobranza.nombreArchivo);
        } catch (Exception e) {
            e.printStackTrace();
            return respuesta("ERROR", "descripcion", "Error al copiar el archivo a carpeta en bandeja");
        }
        BandejaOB bandeja = servicioBandeja.find(cobranza.id).get();

        EstadoBandejaOB estadoInicialBandeja = servicioEstadoBandeja
                .find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
        AccionesOB accionCrear = servicioAcciones.find(EnumAccionesOB.CREAR.getCodigo()).get();
        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);

        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionCrear, estadoInicialBandeja, estadoInicialBandeja);
        servicioHistorial.crear(cobranza, accionCrear, empresaUsuario);

        contexto.parametros.set("idSolicitudPago", cobranza.id);

        Objeto cobranzaDetalle = new Objeto();
        cobranzaDetalle.set("id", cobranza.id);
        cobranzaDetalle.set("monto", cobranza.monto);
        cobranzaDetalle.set("cantidadRegistros", cobranza.cantidadRegistros);
        cobranzaDetalle.set("cuenta", cobranza.cuentaOrigen);
        cobranzaDetalle.set("tipo", cobranza.tipoProducto);
        cobranzaDetalle.set("moneda", cobranza.moneda.simbolo);
        cobranzaDetalle.set("creadoPor", cobranza.usuario.nombre + " " + cobranza.usuario.apellido);

        if (cobranza.estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo())
            cobranzaDetalle.set("estado", cobranza.estado.descripcion);
        else
            cobranzaDetalle.set("estado", cobranza.estadoBandeja.descripcion);

        cobranzaDetalle.set("estadoOperacion", cobranza.estado.descripcion);

        return respuesta("detalle", cobranzaDetalle);
    }

    public static Object consultaProductos(ContextoOB contexto) {
        Objeto productos = new Objeto();

        Objeto debitoDirecto = new Objeto();
        debitoDirecto.set("id", EnumTipoProductoOB.DEBITO_DIRECTO.getCodigo());
        debitoDirecto.set("producto", "Debito Directo");
        productos.add(debitoDirecto);

        Objeto cobranzaIntegral = new Objeto();
        cobranzaIntegral.set("id", EnumTipoProductoOB.COBRANZA_INTEGRAL.getCodigo());
        cobranzaIntegral.set("producto", "Cobranza Integral");
        productos.add(cobranzaIntegral);


        return respuesta("datos", productos);
    }

    public static Object habilitarDeshabilitarConvenios(ContextoOB contexto) {
        Integer convenio = contexto.parametros.integer("convenio");
        String echeq = contexto.parametros.string("echeq", null);
        String transf = contexto.parametros.string("transf", null);
        String debin = contexto.parametros.string("debin", null);
        String tipoConsulta = contexto.parametros.string("tipoConsulta");

        if ((echeq != null && (!echeq.equals("S") && !echeq.equals("N"))) || (transf != null && (!transf.equals("S") && !transf.equals("N"))) || (debin != null && (!debin.equals("S") && !debin.equals("N"))) || (echeq == null && transf == null && debin == null)) {
            return respuesta("DATOS_INVALIDOS");
        }
        Objeto datos = new Objeto();

        try {
            DetalleEstadoHabilitacionConveniosOB detalle = ApiRecaudaciones.detalleEstadoHabilitacionConvenios(contexto, convenio, "E").get();
            echeq = (echeq != null) ? echeq : detalle.get(0).habEcheq;
            transf = (transf != null) ? transf : detalle.get(0).habTransf;
            debin = (echeq != null) ? debin : detalle.get(0).habDebin;
        } catch (Exception e) {
            return new ErrorGenericoOB().setErrores("Datos inválidos.", "No se pudo obtener detalle del convenio indicado.");
        }
        try {
            datos.set("detalleConvenios", ApiRecaudaciones.habilitarConveniosRecaudaciones(contexto, convenio, echeq, transf, debin, tipoConsulta).get());
        } catch (ApiException e) {
            return respuesta("DATOS_INVALIDOS");
        }

        return respuesta("datos", datos);

    }

    public static Object verDetalles(ContextoOB contexto) {
        int idOperacion = contexto.parametros.integer("idOperacion");
        
        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
        ServicioCobranzaIntegralOB servicioCobranzaIntegral = new ServicioCobranzaIntegralOB(contexto);
        CobranzaIntegralOB cobranza = servicioCobranzaIntegral.find(idOperacion).get();
        if (empty(cobranza) || cobranza == null) {
            return respuesta("DATOS_INVALIDOS");
        }
        Objeto cuenta = OBCuentas.cuenta(contexto, cobranza.cuentaOrigen);
        if (cuenta == null) {
            return respuesta("DATOS_INVALIDOS");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        Objeto dato = new Objeto();
        dato.set("archivo", cobranza.nombreArchivo);
        dato.set("cantidadRegistros", cobranza.cantidadRegistros);
        dato.set("creadoPor", cobranza.usuario.nombreCompleto());
        dato.set("fechaCreacion", cobranza.fechaCreacion.format(formatter));
        dato.set("convenio", cobranza.convenio);
        dato.set("cuentaOrigen", cobranza.cuentaOrigen);
        dato.set("cbu", cuenta.get("cbu").toString());
        dato.set("bancoOrigen", "Banco Hipotecario");

        Objeto estado = new Objeto();
        if (cobranza.estado.id == EnumEstadoDebitoDirectoOB.EN_BANDEJA.getCodigo()) {
        	dato.set("estadoOperacion", cobranza.estadoBandeja.descripcion);                
        } else {
            dato.set("estadoOperacion", cobranza.estado.descripcion);
        }

        estado.set("id", cobranza.estadoBandeja.id);
        estado.set("descripcionCorta", cobranza.estadoBandeja.descripcion);
        dato.set("estado", estado);

        BandejaOB bandeja = servicioBandeja.find(idOperacion).get();
        dato.set("firmas", OBFirmas.obtenerDatosFirmantes(contexto, bandeja));

        return respuesta("datos", dato);
    }

    public static Objeto detallesDebinLote(ContextoOB contexto){
        int idOperacion = contexto.parametros.integer("idOperacion");
        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
        ServicioDebinLoteOB servicioDebinLote = new ServicioDebinLoteOB(contexto);
        DebinLoteOB debinLote = servicioDebinLote.find(idOperacion).get();

        if (empty(debinLote)||debinLote==null)  return respuesta("DATOS_INVALIDOS");
        Objeto cuenta = OBCuentas.cuenta(contexto, debinLote.cuentaOrigen);
        if (cuenta == null) return respuesta("DATOS_INVALIDOS");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        Objeto dato = new Objeto();
        dato.set("archivo", debinLote.nombreArchivo);
        dato.set("cantidadRegistros", debinLote.cantidadRegistros);
        dato.set("creadoPor", debinLote.usuario.nombreCompleto());
        dato.set("fechaCreacion", debinLote.fechaCreacion.format(formatter));
        dato.set("convenio", debinLote.convenio);
        dato.set("cuentaOrigen", debinLote.cuentaOrigen);
        dato.set("cbu", cuenta.get("cbu").toString());
        dato.set("bancoOrigen", "Banco Hipotecario");

        Objeto estado = new Objeto();
        dato.set("estadoOperacion",debinLote.estado.id== EnumEstadosDebinLoteOB.EN_BANDEJA.getCodigo()?debinLote.estadoBandeja.descripcion:debinLote.estado.descripcion);
        estado.set("id",debinLote.estadoBandeja.id);
        estado.set("descripcionCorta",debinLote.estadoBandeja.descripcion);
        dato.set("estado",estado);

        BandejaOB bandeja = servicioBandeja.find(idOperacion).get();
        dato.set("firmas", OBFirmas.obtenerDatosFirmantes(contexto, bandeja));

        return respuesta("datos", dato);




    }

    private static boolean seProcesoArchivo(ContextoOB contexto, String nombreArchivo){
        String connectionString = contexto.config.string("ob_azure_blob_st_url");
        String containerName = contexto.config.string("ob_azure_blob_st_container");
        AzureBlobStorageManager az = new AzureBlobStorageManager(contexto, connectionString, containerName);

        try {
            BlobClient blobClient = az.findBlobRecaProcesado(contexto, nombreArchivo,EnumTipoProductoOB.COBRANZA_INTEGRAL);
			Blob archivo = new SerialBlob(blobClient.downloadContent().toBytes());
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public static Object historialCobranzaIntegral(ContextoOB contexto) {
        Fecha fechaDesde = contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd", null);
        Fecha fechaHasta = contexto.parametros.fecha("fechaHasta", "yyyy-MM-dd", null);
        boolean previsualizacion = contexto.parametros.bool("previsualizacion");
        Integer convenio = contexto.parametros.integer("convenio", null);
        String estadoFiltro = contexto.parametros.string("estado", null);

        ServicioCobranzaIntegralOB servicioCobranzaIntegralOB = new ServicioCobranzaIntegralOB(contexto);
        ServicioDebinLoteOB servicioDebinLoteOB = new ServicioDebinLoteOB(contexto);
        ServicioEstadosCobranzaIntegral servicioEstadosCobranzaIntegral = new ServicioEstadosCobranzaIntegral(contexto);
        EstadosCobranzaIntegralOB estadosCobranzaIntegral = null;
        ServicioEstadosDebinPorLote servicioEstadosDebinPorLote = new ServicioEstadosDebinPorLote(contexto);
        EstadosDebinLoteOB estadosDebinLoteOB = null;

        if (estadoFiltro != null) {
            estadoFiltro = estadoFiltro.replaceAll(" ", "_");

            estadosCobranzaIntegral = servicioEstadosCobranzaIntegral.find(EnumEstadosCobranzaIntegralOB.valueOf(estadoFiltro).getCodigo()).get();
        }
        List<CobranzaIntegralOB> cobranzas = servicioCobranzaIntegralOB.filtrarMovimientosHistorial(contexto.sesion().empresaOB, fechaDesde, fechaHasta, convenio, estadosCobranzaIntegral).get();
        List<DebinLoteOB> debinLotes = servicioDebinLoteOB.filtrarMovimientosHistorial(contexto.sesion().empresaOB,fechaDesde,fechaHasta,convenio,estadosDebinLoteOB).get();
        List<BandejaOB> cobranzasDebines = new ArrayList<>();
        cobranzasDebines.addAll(cobranzas);
        cobranzasDebines.addAll(debinLotes);

        Collections.sort(cobranzasDebines, new Comparator<BandejaOB>() {
            @Override
            public int compare(BandejaOB o1, BandejaOB o2) {

                if (o1 instanceof CobranzaIntegralOB) {
                     CobranzaIntegralOB cob = (CobranzaIntegralOB) o1;
                     if (o2 instanceof CobranzaIntegralOB){
                         CobranzaIntegralOB cob2 = (CobranzaIntegralOB) o2;
                         return cob2.fechaCreacion.compareTo(cob.fechaCreacion);
                     }else {
                         DebinLoteOB dbl = (DebinLoteOB) o2;
                         return dbl.fechaCreacion.compareTo(cob.fechaCreacion);
                     }
                }
                else{
                    DebinLoteOB dbl = (DebinLoteOB) o1;
                    if (o2 instanceof CobranzaIntegralOB){
                        CobranzaIntegralOB cob = (CobranzaIntegralOB) o2;
                        return cob.fechaCreacion.compareTo(dbl.fechaCreacion);
                    }
                    else {
                        DebinLoteOB dbl2 = (DebinLoteOB) o2;
                        return dbl2.fechaCreacion.compareTo(dbl.fechaCreacion);
                    }
                }
            }
        });
        Objeto respuesta = new Objeto();

        for (BandejaOB cobranzaDebin : cobranzasDebines) {
            Objeto datos = new Objeto();
            Objeto estado = new Objeto();
            datos.set("idBandeja", cobranzaDebin.id);
            CobranzaIntegralOB cobranza = null;
            DebinLoteOB debinLote = null;
            if(cobranzaDebin instanceof CobranzaIntegralOB){
                cobranza = (CobranzaIntegralOB) cobranzaDebin;
                datos.set("convenio", cobranza.convenio);
                datos.set("nombreArchivo", cobranza.nombreArchivo);
                datos.set("fechaCreacion", cobranza.fechaCreacion.toLocalDate().toString());

                if (cobranza.estado.id == EnumEstadoDebitoDirectoOB.EN_BANDEJA.getCodigo()) {
                    datos.set("estadoOperacion", cobranza.estadoBandeja.descripcion);
                } else {
                    if (cobranza.estado.id==2){
                        if (seProcesoArchivo(contexto,cobranza.nombreArchivo)){
                            EstadosCobranzaIntegralOB estadoProcesado = servicioEstadosCobranzaIntegral.find(EnumEstadosCobranzaIntegralOB.PROCESADO.getCodigo()).get();
                            datos.set("estadoOperacion", estadoProcesado.descripcion);
                            cobranza.estado = estadoProcesado;
                            servicioCobranzaIntegralOB.update(cobranza);
                        }
                        datos.set("estadoOperacion", cobranza.estado.descripcion);

                    }
                    datos.set("estadoOperacion", cobranza.estado.descripcion);
                }

                estado.set("id", cobranza.estadoBandeja.id);
                estado.set("descripcionCorta", cobranza.estadoBandeja.descripcion);
                datos.set("estado", estado);
                datos.set("tipoProducto",EnumTipoProductoOB.COBRANZA_INTEGRAL.getCodigo());
                respuesta.add(datos);

                if (previsualizacion) {
                    if (respuesta.toList().size() == 5) {
                        return respuesta("datos", respuesta);
                    }
                }

            }else{
                debinLote = (DebinLoteOB) cobranzaDebin;
                datos.set("convenio", debinLote.convenio);
                datos.set("nombreArchivo", debinLote.nombreArchivo);
                datos.set("fechaCreacion", debinLote.fechaCreacion.toLocalDate().toString());

                if (debinLote.estado.id == EnumEstadoDebitoDirectoOB.EN_BANDEJA.getCodigo()) {
                    datos.set("estadoOperacion", debinLote.estadoBandeja.descripcion);
                } else {
                    if (debinLote.estado.id==2){
                        if (seProcesoArchivo(contexto,debinLote.nombreArchivo)){
                            EstadosDebinLoteOB estadoProcesado = servicioEstadosDebinPorLote.find(EnumEstadosDebinLoteOB.PROCESADO.getCodigo()).get();
                            datos.set("estadoOperacion", estadoProcesado.descripcion);
                            debinLote.estado = estadoProcesado;
                            servicioDebinLoteOB.update(debinLote);
                        }
                        datos.set("estadoOperacion", debinLote.estado.descripcion);

                    }
                    datos.set("estadoOperacion", debinLote.estado.descripcion);
                }

                estado.set("id", debinLote.estadoBandeja.id);
                estado.set("descripcionCorta", debinLote.estadoBandeja.descripcion);
                datos.set("estado", estado);
                datos.set("tipoProducto",EnumTipoProductoOB.DEBIN_LOTE.getCodigo());

                respuesta.add(datos);

                if (previsualizacion) {
                    if (respuesta.toList().size() == 5) {
                        return respuesta("datos", respuesta);
                    }
                }

            }

        }

        return respuesta("datos", respuesta);
    }






    public static Object verTablaCI(ContextoOB contexto) {
        int idOperacion = contexto.parametros.integer("idOperacion");
        int numeroPagina = contexto.parametros.integer("numeroPagina");
        Objeto dato = new Objeto();
        ServicioCobranzaIntegralOB servicioCobranzaIntegral = new ServicioCobranzaIntegralOB(contexto);
        CobranzaIntegralOB cobranza = servicioCobranzaIntegral.find(idOperacion).get();

        if (empty(cobranza) || cobranza == null) {
            return respuesta("DATOS_INVALIDOS");
        }

        String connectionString = contexto.config.string("ob_azure_blob_st_url");
        String containerName = contexto.config.string("ob_azure_blob_st_container");

        AzureBlobStorageManager az = new AzureBlobStorageManager(contexto, connectionString, containerName);
        Blob archivo;

        try {
            BlobClient blobClient = az.findBlobByName(contexto, EnumTipoProductoOB.COBRANZA_INTEGRAL, cobranza.nombreArchivo);
            archivo = new SerialBlob(blobClient.downloadContent().toBytes());
        } catch (Exception e) {
            return respuesta("ERROR", "descripcion", "No se encontró el archivo con ese id de bandeja.");
        }
        int registrosPorPagina = 60;
        Objeto respuesta = new Objeto();

        List<ArchivoCobranzaIntegralDTO.Body> listDetalleItem = ArchivoCobranzaIntegralDTO.getBody(archivo);
        List<ArchivoCobranzaIntegralDTO.Body> listaPaginada = getPage(listDetalleItem,
                numeroPagina, registrosPorPagina);

        listaPaginada.stream().forEach(item -> {
            Objeto detail = new Objeto();
            detail.set("nombreCliente", eliminarEspaciosDerecha(item.nombreDepositante));
            detail.set("numeroCliente", eliminarCerosIzquierda(item.numeroDepositante));
            detail.set("numeroComprobante", eliminarCerosIzquierda(item.numeroComprobante));
            detail.set("codigoServicio", item.codigoServicio);
            detail.set("tipoDocumentoCliente", item.tipoDocumento);
            detail.set("numeroDocumentoCliente", eliminarCerosIzquierda(item.numeroDocumento));
            detail.set("fechaDesde", item.fechaDesde.substring(6, 8) + "/" + item.fechaDesde.substring(4, 6) + "/" + item.fechaDesde.substring(0, 4));
            detail.set("fechaHasta", item.fechaHasta.substring(6, 8) + "/" + item.fechaHasta.substring(4, 6) + "/" + item.fechaHasta.substring(0, 4));
            detail.set("importeVto1", "$" + Double.valueOf(item.importe1) / 100);
            detail.set("fechaVto1", item.fechaVencimiento1.substring(6, 8) + "/" + item.fechaVencimiento1.substring(4, 6) + "/" + item.fechaVencimiento1.substring(0, 4));
            detail.set("importeVto2", "$" + Double.valueOf(item.importe2) / 100);
            detail.set("fechaVto2", item.fechaVencimiento2.substring(6, 8) + "/" + item.fechaVencimiento2.substring(4, 6) + "/" + item.fechaVencimiento2.substring(0, 4));
            detail.set("importeVto3", "$" + Double.valueOf(item.importe3) / 100);
            detail.set("fechaVto3", item.fechaVencimiento3.substring(6, 8) + "/" + item.fechaVencimiento3.substring(4, 6) + "/" + item.fechaVencimiento3.substring(0, 4));
            detail.set("claveInterna", eliminarEspaciosDerecha(item.claveInterna));
            detail.set("division", eliminarEspaciosDerecha(item.division));
            detail.set("descripcionDivision", eliminarEspaciosDerecha(item.descripcionDivision));
            detail.set("leyenda1", eliminarEspaciosDerecha(item.leyenda1));
            detail.set("leyenda2", eliminarEspaciosDerecha(item.leyenda2));
            detail.set("leyenda3", eliminarEspaciosDerecha(item.leyenda3));
            detail.set("leyenda4", eliminarEspaciosDerecha(item.leyenda4));
            detail.set("leyenda5", eliminarEspaciosDerecha(item.leyenda5));
            detail.set("leyenda6", eliminarEspaciosDerecha(item.leyenda6));
            detail.set("leyenda7", eliminarEspaciosDerecha(item.leyenda7));
            detail.set("celularCliente", eliminarEspaciosDerecha(item.celularCliente));
            detail.set("mailCliente", eliminarEspaciosDerecha(item.mailCliente));

            respuesta.add("registros", detail);
        });


        dato.set("nombreArchivo", cobranza.nombreArchivo);
        dato.set("numeroPagina", numeroPagina);
        dato.set("cantidadRegistros", cobranza.cantidadRegistros);
        dato.set("paginasTotales", Math.ceil((double) cobranza.cantidadRegistros / registrosPorPagina));

        respuesta.add("informacionArchivo", dato);

        ServicioCobranzaIntegralConfigOB servicioCobranzaIntegralConfigOB = new ServicioCobranzaIntegralConfigOB(contexto);
        List<String> columnas = new ArrayList<>(servicioCobranzaIntegralConfigOB.findAll().get().stream().
                parallel().filter(columna -> columna.visible.equals(true)).sorted(Comparator.comparing(CobranzaIntegralConfigOB::getPosicion))
                .map(columna -> columna.nombreColumna).toList());
        respuesta.add("columnas", columnas);
        return respuesta("datos", respuesta);
    }

    public static boolean validarFormatoDeFechas(String fecha) {
        String formatoFecha = "dd/MM/yyyy";
        SimpleDateFormat validarFormatoFecha = new SimpleDateFormat(formatoFecha);
        try {
            validarFormatoFecha.setLenient(false);
            validarFormatoFecha.parse(fecha);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Object reporteCobranzas(ContextoOB contexto) {
        String fechaFin = contexto.parametros.string("fechaFin", null);
        String fechaInicio = contexto.parametros.string("fechaInicio", null);
        String idConvenio = contexto.parametros.string("idConvenio", null);
        Boolean previsualizacion = contexto.parametros.bool("previsualizacion");
        Integer tipoPago = contexto.parametros.integer("tipoPago", null);

        ArrayList<ReporteCobranzasOB.ReporteCobranza> reportes = new ArrayList<>();
        Objeto reporte = new Objeto();

        Random random = new Random(Integer.valueOf(contexto.sesion().empresaOB.idCobis));
        Integer idProceso = random.nextInt(Integer.MAX_VALUE);
        int secuencial = 0;

        if (previsualizacion) {
            fechaInicio = LocalDate.now().minusMonths(6).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            fechaFin = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            ConveniosRecaOB convenioRecaudaciones = ApiRecaudaciones.convenioRecaudaciones(contexto, Integer.parseInt(contexto.sesion().empresaOB.idCobis)).get();
            List<ConveniosRecaOB.ConvenioReca> convenios = new ArrayList<>(convenioRecaudaciones.lista2.stream()
                    .filter(con -> con.grupoConvenio.equals(GRUPO_CONVENIO))
                    .toList());

            for (ConveniosRecaOB.ConvenioReca convenio : convenios) {
                ArrayList<ReporteCobranzasOB.ReporteCobranza> reporteCobranza = ApiRecaudaciones.repCobranzas(contexto, null, fechaFin, fechaInicio, String.valueOf(convenio.convenio), tipoPago,secuencial,idProceso).get().reportes;
                if (reporteCobranza != null) {
                    reporteCobranza.forEach(rep->rep.convenio = convenio.convenio.toString());
                    reportes.addAll(reporteCobranza);
//                    ReporteCobranzaConvenioDTO reporteCobranzaConvenio = new ReporteCobranzaConvenioDTO(convenio.convenio, reporteCobranza);
//                    listReporteOB.add(reporteCobranzaConvenio);
                }
            }

        } else {
            if (fechaInicio == null || fechaFin == null) {
                fechaInicio = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                fechaFin = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } else {

                if (!validarFormatoDeFechas(fechaInicio) || !validarFormatoDeFechas(fechaFin)) {
                    return new ErrorGenericoOB().setErrores("Las fechas no cumplen con el formato esperado.",
                            "Solo se aceptan fechas en el formato: dd/MM/yyyy");
                }

                Fecha fechaI = new Fecha(fechaInicio, "dd/MM/yyyy");
                Fecha fechaF = new Fecha(fechaFin, "dd/MM/yyyy");

                if (fechaI.esAnterior(fechaF.restarAños(1))) {
                    return new ErrorGenericoOB().setErrores("Fechas inválidas.",
                            "La diferencia de fechas no puede superar el año.");
                }
            }

            if (tipoPago != null && !EnumTipoPagosReca.isValidCodigo(tipoPago)) {
                return new ErrorGenericoOB().setErrores(
                        "Tipo de pago inválido",
                        "El tipo de pago solo puede ser 2 (Efectivo), 3 (Echeq), 4 (Transferencia) 5 (Debin por lote).");
            }

            if (idConvenio == null) {
                ConveniosRecaOB convenioRecaudaciones = ApiRecaudaciones.convenioRecaudaciones(contexto, Integer.parseInt(contexto.sesion().empresaOB.idCobis)).get();
                List<ConveniosRecaOB.ConvenioReca> convenios = new ArrayList<>(convenioRecaudaciones.lista2.stream()
                        .filter(con -> con.grupoConvenio.equals(GRUPO_CONVENIO))
                        .toList());

                for (ConveniosRecaOB.ConvenioReca convenio : convenios) {
                    secuencial = 0;
                    double pagina = 0;
                    double paginaMaxima = 1;
                    double registros = 0;
                    do {
                        ReporteCobranzasOB reporteCobranzasOB = ApiRecaudaciones.repCobranzas(contexto, null, fechaFin, fechaInicio, String.valueOf(convenio.convenio), tipoPago, secuencial, idProceso).get();
                        if (reporteCobranzasOB.paginado!=null){
                            registros = Integer.valueOf(reporteCobranzasOB.paginado);
                            ArrayList<ReporteCobranzasOB.ReporteCobranza> reporteCobranza = reporteCobranzasOB.reportes;
                            if (reporteCobranza != null) {
                                reporteCobranza.forEach(rep->rep.convenio = convenio.convenio.toString());
                                reportes.addAll(reporteCobranza);
                            }
                            if (registros<=15){
                                pagina = paginaMaxima+1;
                            }else{
                                paginaMaxima = Math.ceil(registros/15);
                                if (secuencial==0) secuencial++;
                                secuencial+=15;
                            }
                            pagina++;
                        } else {
                            pagina = Integer.MAX_VALUE;
                        }

                    }while(pagina<=paginaMaxima);

                }
            } else {
                secuencial = 0;
                double pagina = 0;
                double paginaMaxima = 1;
                double registros = 0;
                do {
                    ReporteCobranzasOB reporteCobranzasOB = ApiRecaudaciones.repCobranzas(contexto, null, fechaFin, fechaInicio, idConvenio, tipoPago, secuencial, idProceso).get();
                    if (reporteCobranzasOB.paginado!=null){
                        registros = Integer.valueOf(reporteCobranzasOB.paginado);
                        ArrayList<ReporteCobranzasOB.ReporteCobranza> reporteCobranza = reporteCobranzasOB.reportes;
                        if (reporteCobranza != null) {
                            reporteCobranza.forEach(rep->rep.convenio = idConvenio);
                            reportes.addAll(reporteCobranza);
                        }
                        if (registros<=15){
                            pagina = paginaMaxima+1;
                        }else{
                            paginaMaxima = Math.ceil(registros/15);
                            if (secuencial==0) secuencial++;
                            secuencial+=15;
                        }
                        pagina++;
                    } else {
                        pagina = Integer.MAX_VALUE;
                    }

                }while(pagina<=paginaMaxima);
            }
        }
        DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        reportes.sort((r1, r2) -> {
            if (r1.fechaPago == null && r2.fechaPago == null) {
                return 0;
            } else if (r1.fechaPago == null) {
                return 1;
            } else if (r2.fechaPago == null) {
                return -1;
            } else {
                LocalDate fecha1 = LocalDate.parse(r1.fechaPago, DATE_FORMATTER);
                LocalDate fecha2 = LocalDate.parse(r2.fechaPago, DATE_FORMATTER);
                return fecha2.compareTo(fecha1);
            }
        });

        if (!reportes.isEmpty()) {
            for (ReporteCobranzasOB.ReporteCobranza cob : reportes) {
                Objeto data = new Objeto();
                switch (cob.formaPago) {
                    case "TR" -> data.set("formaPago", "Transferencia");
                    case "EC" -> {
                        if (cob.tipoCheque.equals("ECHEQDIF")) {
                            data.set("formaPago", "Echeq diferido");
                        } else if (cob.tipoCheque.equals("ECHEQ")) {
                            data.set("formaPago", "Echeq");
                        }
                    }
                    case "CH" -> data.set("formaPago", "Cheque");
                    case "EF" -> data.set("formaPago", "Pago en efectivo");
                    case "DE" -> data.set("formaPago", "Debin");
                }

                data.set("nombreDepositante", cob.nombreDepositante);

                if (cob.estadoCheque.equals("R")) {
                    data.set("estadoCheque", "Rechazado");
                } else if (!cob.estadoCheque.isBlank()) {
                    data.set("estadoCheque", cob.estadoCheque.length()>=4?cob.estadoCheque.substring(4):cob.estadoCheque);
                } else {
                    data.set("estadoCheque", null);
                }
                data.set("fechaDeposito", cob.fechaDeposito);
                data.set("fechaPago", cob.fechaPago);
                data.set("moneda", cob.moneda);
                data.set("importe", cob.importe);
                data.set("numeroCheque", cob.nuemeroCheque);

                data.set("cuitOrigen", cob.cuit);
                data.set("cuentaDestino", cob.cuenta);
                data.set("bancoDestino", "Banco Hipotecario");
                data.set("convenio", cob.convenio);
                reporte.add(data);
            }
        }

        if (previsualizacion) {
            return respuesta("datos", !reporte.isEmpty() ? reporte.toList().stream().limit(5).collect(Collectors.toList()) : reporte);
        } else return respuesta("datos", reporte);
    }

    public static Object consultaEstados(ContextoOB contexto) {
        ServicioEstadosCobranzaIntegral servicioEstadosCobranzaIntegral = new ServicioEstadosCobranzaIntegral(contexto);
        List<EstadosCobranzaIntegralOB> estadosCobranzaIntegral = servicioEstadosCobranzaIntegral.findAll().get();

        Objeto datos = new Objeto();
        for (EstadosCobranzaIntegralOB estado : estadosCobranzaIntegral) {
            if (estado.id != EnumEstadosCobranzaIntegralOB.EN_BANDEJA.getCodigo()) {
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

    public static Object tipoMovimiento(ContextoOB contexto) {

        EnumTipoPagosReca[] listaTiposPago = EnumTipoPagosReca.values();
        Objeto datos = new Objeto();

        for (EnumTipoPagosReca tipoMov : listaTiposPago) {
            Objeto e = new Objeto();
            e.set("id", tipoMov.getCodigo());
            e.set("id", tipoMov.getCodigo());
            e.set("descripcion", tipoMov.name().replaceAll("_"," "));
            datos.add(e);
        }
        return respuesta("datos", datos);
    }

    public static Object descargarCobranzaIntegral(ContextoOB contexto) throws IOException {
        String fechaFin = contexto.parametros.string("fechaFin", null);
        String fechaInicio = contexto.parametros.string("fechaInicio", null);
        String idConvenio = contexto.parametros.string("idConvenio", null);
        Integer tipoPago = contexto.parametros.integer("tipoPago", null);

        ArrayList<ReporteCobranzaConvenioDTO> listReporteOB = new ArrayList<>();
        ArrayList<ReporteCobranzaCSVDTO> listReportes = new ArrayList<>();
        ServicioEmpresaOB servEmpresas = new ServicioEmpresaOB(contexto);
        Random random = new Random(Integer.valueOf(contexto.sesion().empresaOB.idCobis));
        Integer idProceso = random.nextInt(Integer.MAX_VALUE);
        int secuencial = 0;
        ArrayList<ReporteCobranzasOB.ReporteCobranza> reportes = new ArrayList<>();
        if (fechaInicio == null || fechaFin == null) {
            fechaInicio = LocalDate.now().minusMonths(6).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            fechaFin = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } else {

            if (!validarFormatoDeFechas(fechaInicio) || !validarFormatoDeFechas(fechaFin)) {
                return new ErrorGenericoOB().setErrores("Las fechas no cumplen con el formato esperado.",
                        "Solo se aceptan fechas en el formato: dd/MM/yyyy");
            }

            Fecha fechaI = new Fecha(fechaInicio, "dd/MM/yyyy");
            Fecha fechaF = new Fecha(fechaFin, "dd/MM/yyyy");

            if (fechaI.esAnterior(fechaF.restarAños(1))) {
                return new ErrorGenericoOB().setErrores("Fechas inválidas.",
                        "La diferencia de fechas no puede superar el año.");
            }
        }

        if (tipoPago != null && !EnumTipoPagosReca.isValidCodigo(tipoPago)) {
            return new ErrorGenericoOB().setErrores(
                    "Tipo de pago inválido",
                    "El tipo de pago solo puede ser 2 (Efectivo), 3 (Echeq) o 4 (Transferencia).");
        }

        if (idConvenio == null) {
            ConveniosRecaOB convenioRecaudaciones = ApiRecaudaciones.convenioRecaudaciones(contexto, Integer.parseInt(contexto.sesion().empresaOB.idCobis)).get();
            List<ConveniosRecaOB.ConvenioReca> convenios = new ArrayList<>(convenioRecaudaciones.lista2.stream()
                    .filter(con -> con.grupoConvenio.equals(GRUPO_CONVENIO))
                    .toList());

            for (ConveniosRecaOB.ConvenioReca convenio : convenios) {
                secuencial = 0;
                double pagina = 0;
                double paginaMaxima = 1;
                double registros = 0;
                do {
                    ReporteCobranzasOB reporteCobranzasOB = ApiRecaudaciones.repCobranzas(contexto, null, fechaFin, fechaInicio, String.valueOf(convenio.convenio), tipoPago, secuencial, idProceso).get();
                    if (reporteCobranzasOB.paginado!=null){
                        registros = Integer.valueOf(reporteCobranzasOB.paginado);
                        ArrayList<ReporteCobranzasOB.ReporteCobranza> reporteCobranza = reporteCobranzasOB.reportes;
                        if (reporteCobranza != null) {
                            reporteCobranza.forEach(rep->rep.convenio = convenio.convenio.toString());
                            reportes.addAll(reporteCobranza);
                        }
                        if (registros<=15){
                            pagina = paginaMaxima+1;
                        }else{
                            paginaMaxima = Math.ceil(registros/15);
                            if (secuencial==0) secuencial++;
                            secuencial+=15;
                        }
                        pagina++;
                    } else {
                        pagina = Integer.MAX_VALUE;
                    }

                }while(pagina<=paginaMaxima);
            }
        } else {
            secuencial = 0;
            double pagina = 0;
            double paginaMaxima = 1;
            double registros = 0;
            do {
                ReporteCobranzasOB reporteCobranzasOB = ApiRecaudaciones.repCobranzas(contexto, null, fechaFin, fechaInicio, idConvenio, tipoPago, secuencial, idProceso).get();
                if (reporteCobranzasOB.paginado!=null){
                    registros = Integer.valueOf(reporteCobranzasOB.paginado);
                    ArrayList<ReporteCobranzasOB.ReporteCobranza> reporteCobranza = reporteCobranzasOB.reportes;
                    if (reporteCobranza != null) {
                        reporteCobranza.forEach(rep->rep.convenio = idConvenio);
                        reportes.addAll(reporteCobranza);
                    }
                    if (registros<=15){
                        pagina = paginaMaxima+1;
                    }else{
                        paginaMaxima = Math.ceil(registros/15);
                        if (secuencial==0) secuencial++;
                        secuencial+=15;
                    }
                    pagina++;
                } else {
                    pagina = Integer.MAX_VALUE;
                }

            }while(pagina<=paginaMaxima);
        }

        if (!reportes.isEmpty()) {
            for (ReporteCobranzasOB.ReporteCobranza cob : reportes) {

                String idCliente = " ";
                switch (cob.formaPago) {
                    case "CH" -> cob.formaPago = "Cheque";
                    case "TR" -> cob.formaPago = "Transferencia";
                    case "EC" -> {
                        if (cob.tipoCheque.equals("ECHEQDIF")) {
                            cob.formaPago = "Echeq diferido";
                        } else if (cob.tipoCheque.equals("ECHEQ")) {
                            cob.formaPago = "Echeq";
                        }
                    }
                    case "EF" -> cob.formaPago = "Pago en efectivo";
                    case "DE" -> cob.formaPago = "Debin";
                }
                switch (cob.estadoCheque) {
                    case "C1 - PENDIENTE" -> cob.estadoCheque = "Pendiente";
                    case "R - RECHAZADO" -> cob.estadoCheque = "Rechazado";
                    case "D - DEPOSITADO" -> cob.estadoCheque = "Depositado";

                }
                if (!cob.cuitCliente.isBlank()) {
                    EmpresaOB empresa = servEmpresas.findByCuit(Long.parseLong(cob.cuitCliente), null).get();
                    if (empresa != null) {
                        idCliente = empresa.emp_codigo.toString();
                    }
                }
                for (EnumTipoPagosReca tipo : EnumTipoPagosReca.values()) {
                    if (tipo.getReferencia().equals(cob.formaPago)) {
                        cob.formaPago = tipo.name();
                    }
                }
                String moneda = " ";
                if (cob.moneda == 80) {
                    moneda = "$";
                }
                listReportes.add(new ReporteCobranzaCSVDTO(cob.cuenta, cob.formaPago, moneda, cob.importe, cob.cuit, cob.tipoCheque, cob.nuemeroCheque, cob.estadoCheque,
                        cob.fechaEmision, cob.fechaPago, cob.fechaDeposito, cob.sucursal, idCliente, cob.numeroComprobante, cob.nombreDepositante, cob.cuitCliente, String.valueOf(cob.convenio)));

            }

            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                OutputStreamWriter streamWriter = new OutputStreamWriter(stream);
                CSVWriter writer = new CSVWriter(streamWriter, ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

                var mappingStrategy = new OBManejoArchivos.CustomColumnPositionStrategy<ReporteCobranzaCSVDTO>();
                mappingStrategy.setType(ReporteCobranzaCSVDTO.class);

                StatefulBeanToCsv<ReporteCobranzaCSVDTO> builder = new StatefulBeanToCsvBuilder<ReporteCobranzaCSVDTO>(writer)
                        .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                        .withSeparator(';')
                        .withMappingStrategy(mappingStrategy)
                        .build();

                builder.write(listReportes);
                streamWriter.flush();
                writer.close();

                byte[] file = stream.toByteArray();
                contexto.response.header("Content-Disposition", "attachment; filename=ordenes_de_pago-" + Fecha.hoy() + ".csv");
                contexto.response.type("application/csv");
                return file;

            } catch (Exception e) {
                return respuesta("ERROR", "descripcion", "No se pudo descargar el archivo.");
            }

        } else return respuesta("0", "descripcion", "No hay reportes de cobranza para descargar.");

    }

    public static Object consultaConveniosTodosDetalle(ContextoOB contexto) {
        String medioRecaudacion = contexto.parametros.string("medioRecaudacion");

        if (!medioRecaudacion.equals("E") && !medioRecaudacion.equals("D") && !medioRecaudacion.equals("T")) {
            return new ErrorGenericoOB().setErrores("Medio de recaudación inválido.", "El medio de recaudación debe ser 'E' (Echeq), 'D' (Debin) o 'T' (Transferencia).");
        }

        DetalleEstadoHabilitacionConveniosOB detalle = ApiRecaudaciones.detalleEstadoHabilitacionTodosConvenios(contexto, medioRecaudacion).get();

        return respuesta("datos", detalle);
    }

    public static Object descargaRendiciones(ContextoOB contexto) {
//        Integer convenio = contexto.parametros.integer("convenio");
//        String fecha = contexto.parametros.string("fecha");
//        Integer producto = contexto.parametros.integer("productoOB");
//
//        Objeto rutasFinales = new Objeto();
//
//        ConveniosRecaOB resp = ApiRecaudaciones.convenioRecaudaciones(contexto, Integer.parseInt(contexto.sesion().empresaOB.idCobis)).get();
//        try {
//            ConveniosRecaOB.ConvenioReca convenioReca = resp.lista2.stream().filter(c -> c.convenio.equals(convenio)).findFirst().get();
//            RutasOB rutas = ApiRecaudaciones.consultaRutas(contexto, String.valueOf(convenio), convenioReca.grupoRecaudacion, convenioReca.servRecaudacion).get();
//            rutasFinales.set("rutaSalida", rutas.rutaSalida);
//            rutasFinales.set("rutaListado", rutas.rutaListado);
//        } catch (Exception e) {
//            return new ErrorGenericoOB().setErrores("Convenio inválido.", "El convenio seleccionado no existe.");
//        }
//
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
//        try {
//            LocalDate.parse(fecha, formatter);
//        } catch (DateTimeParseException e) {
//            return new ErrorGenericoOB().setErrores("Fecha inválida.", "Se debe ingresar una fecha válida con formato AAAA-MM-DD");
//        }
//        AzureBlobStorageManager az = new AzureBlobStorageManager(contexto);
//        try {
//
//        	OBManejoArchivos.TipoOperacionSamba tipo = producto == EnumTipoProductoOB.DEBITO_DIRECTO.getCodigo() ? OBManejoArchivos.TipoOperacionSamba.DEBITO_DIRECTO : OBManejoArchivos.TipoOperacionSamba.RECAUDACIONES_ABM;
//            byte[] zip = az.obtenerListaArchivosPorFecha(contexto,contexto.config.string("ob_ruta_consulta_rendiciones"),fecha, tipo, convenio);
//
////            contexto.response.type("application/zip");
////            contexto.response.header("Access-Control-Expose-Headers", "Content-Disposition");
//
//
//            Archivo archivo = new Archivo("2024-06-14_0043_Rendiciones.zip", zip);
//
//          contexto.response.header("Content-Disposition", "attachment; filename=" + "rendicion_" + convenio + "_" + fecha + ".zip");
//
//            return archivo;
//

//        } catch (Exception e) {
//            return new ErrorGenericoOB().setErrores("Error al obtener archivo de rendiciones.", "No se pudo descargar.");
//        }
        return null;
    }



    public static Object verTablaDL(ContextoOB contexto){
        int idOperacion = contexto.parametros.integer("idOperacion");
        int numeroPagina = contexto.parametros.integer("numeroPagina");
        Objeto infoArchivo = new Objeto();
        ServicioDebinLoteOB servicioDebinLoteOB = new ServicioDebinLoteOB(contexto);
        DebinLoteOB debinLoteOB = servicioDebinLoteOB.find(idOperacion).get();

        if (empty(debinLoteOB) || debinLoteOB == null) {
            return respuesta("DATOS_INVALIDOS");
        }

        String connectionString = contexto.config.string("ob_azure_blob_st_url");
        String containerName = contexto.config.string("ob_azure_blob_st_container");

        AzureBlobStorageManager az = new AzureBlobStorageManager(contexto, connectionString, containerName);
        Blob archivo;

        try {
            BlobClient blobClient = az.findBlobByName(contexto, EnumTipoProductoOB.COBRANZA_INTEGRAL, debinLoteOB.nombreArchivo);
            archivo = new SerialBlob(blobClient.downloadContent().toBytes());
        } catch (Exception e) {
            return respuesta("ERROR", "descripcion", "No se encontró el archivo con ese id de bandeja.");
        }
        int registrosPorPagina = 60;
        Objeto respuesta = new Objeto();

        List<ArchivoDebinLoteDTO.Body> listDetalleItem = ArchivoDebinLoteDTO.getBody(archivo);
        List<ArchivoDebinLoteDTO.Body> listaPaginada = getPage(listDetalleItem,
                numeroPagina, registrosPorPagina);


        listaPaginada.stream().forEach(item -> {
            Objeto detail = new Objeto();
            detail.set("NombreCliente",item.nombreApellido);
            detail.set("cbuDebito",item.cuentaBanc);
            detail.set("cuit",item.cuit);
            detail.set("monto",(Double.valueOf(item.importe)/100));
            detail.set("vigencia",item.vigencia);
            detail.set("concepto",item.concepto);
            detail.set("numeroServicio",item.nroServicio);
            detail.set("comprobante",item.idDebito);

            respuesta.add("registros",detail);

        });

        infoArchivo.set("nombreArchivo",debinLoteOB.nombreArchivo);
        infoArchivo.set("numeroPagina",numeroPagina);
        infoArchivo.set("cantidadRegistros",debinLoteOB.cantidadRegistros);
        infoArchivo.set("paginasTotales",Math.ceil((double) debinLoteOB.cantidadRegistros/registrosPorPagina));

        respuesta.add("informacionArchivo",infoArchivo);

        ServicioDebinLoteConfigOB servicioDebinLoteConfigOB = new ServicioDebinLoteConfigOB(contexto);
        List<String> columnas = new ArrayList<>(servicioDebinLoteConfigOB.findAll().get().stream().
                parallel().filter(columna->columna.visible.equals(true)).sorted(Comparator.comparing(DebinLoteConfigOB::getPosicion))
                .map(columna->columna.nombreColumna).toList());
        respuesta.add("columnas",columnas);

        return respuesta("datos",respuesta);

    }
}











