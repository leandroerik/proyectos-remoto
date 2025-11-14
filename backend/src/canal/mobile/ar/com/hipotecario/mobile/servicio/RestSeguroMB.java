package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.Persona;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class RestSeguroMB {

    private static final String RAMO_PRODUCTO_EMPLEADO = ConfigMB.string("ramo_producto_mascotas_empleado");
    private static final String RAMO_PRODUCTO_INDIVIDUAL = ConfigMB.string("ramo_producto_mascotas_individual");
    private static final String RAMO_PRODUCTO_EMPLEADO_SEGURO_MOVILIDAD = ConfigMB.string("ramo_producto_movilidad_empleado");
    private static final String RAMO_PRODUCTO_INDIVIDUAL_SEGURO_MOVILIDAD = ConfigMB.string("ramo_producto_movilidad_individual");

    private static final String RAMO_PRODUCTO_BIENES_MOVILES_EMPLEADO = ConfigMB.string("ramo_producto_bienes_moviles_empleado");
    private static final String RAMO_PRODUCTO_BIENES_MOVILES_CLIENTE = ConfigMB.string("ramo_producto_bienes_moviles_cliente");

    public static List<Objeto> productos(ContextoMB contexto, String cuit) {

        if (contexto.sesion() != null)
            contexto.sesion().cobisCaido = (false);

        ApiResponseMB response = obtenerProductos(contexto, cuit);
        if (response.hayError() && !"101146".equals(response.string("codigo"))) {
            if ("Server fuera de linea".equals(response.string("mensajeAlUsuario")) && contexto.sesion() != null) {
                contexto.sesion().cobisCaido = (true);
            }
            return null;
        }
        List<Objeto> lista = new ArrayList<>();
        if (!response.hayError()) {
            for (Objeto producto : response.objetos()) {
                lista.add(producto);
            }
        }
        return lista;
    }

    public static Objeto token(ContextoMB contexto) {

        Objeto respuesta = null;
        if (contexto.sesion() != null)
            contexto.sesion().cobisCaido = (false);
        ApiResponseMB response = obtenerToken(contexto);
        if (response.hayError() && !"101146".equals(response.string("codigo"))) {
            if ("Server fuera de linea".equals(response.string("mensajeAlUsuario")) && contexto.sesion() != null) {
                contexto.sesion().cobisCaido = (true);
            }
            return null;
        }
        respuesta = Objeto.fromJson(response.json);
        return respuesta;
    }
    public static Objeto insertEmisionOnlineV2(ContextoMB contexto) {

        Objeto respuesta = null;
        if (contexto.sesion() != null)
            contexto.sesion().cobisCaido = (false);
        ApiResponseMB response = insertarEmision(contexto);
        if (response.hayError() && !"101146".equals(response.string("codigo"))) {
            if ("Server fuera de linea".equals(response.string("mensajeAlUsuario")) && contexto.sesion() != null) {
                contexto.sesion().cobisCaido = (true);
            }
            return null;
        }
        respuesta = Objeto.fromJson(response.json);
        return respuesta;
    }

    public static List<Objeto> ofertas(ContextoMB contexto, String sessionId) {

        if (contexto.sesion() != null)
            contexto.sesion().cobisCaido = (false);

        ApiResponseMB response = obtenerOferta(contexto, sessionId);
        if (response.hayError() && !"101146".equals(response.string("codigo"))) {
            if ("Server fuera de linea".equals(response.string("mensajeAlUsuario")) && contexto.sesion() != null) {
                contexto.sesion().cobisCaido = (true);
            }
            return null;
        }
        List<Objeto> lista = new ArrayList<>();
        if (!response.hayError()) {
            if (ConfigMB.esDesarrollo()){
                String ofertasStr = "";
                try {
                    ofertasStr = new String(Files.readAllBytes(Paths.get("src/main/resources/ofertasHomoJulioHS.txt")));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                    lista.add(Objeto.fromJson(ofertasStr));
            }
            else {
                for (Objeto producto : response.objetos())
                    lista.add(producto);
            }
        }
        return lista;
    }

    private static String getPreguntaDos(String ramoProducto ) {
        if (ramoProducto.equals("9-401") || ramoProducto.equals("9-402")) return "S";// MOVILIDAD
        return "N";
    }

    private static void setDatosMovilidad(ApiRequestMB request, ContextoMB contexto ) {
        String marca = contexto.parametros.string("marca");
        String modelo = contexto.parametros.string("modelo");
        String marcaModelo = "";

        if( !marca.isEmpty() && !modelo.isEmpty() )
            marcaModelo = marca + " " + modelo;

        String tipoMovilidad = contexto.parametros.string("tipoMovilidad");

        String tipoBien;
        switch (tipoMovilidad) {
            case "1":
                tipoBien = "BICICLETA";
                break;
            case "2":
                tipoBien = "MONOPATIN";
                break;
            default:
                tipoBien = "";
                break;
        }

        request.body("tipoBien", tipoBien);
        request.body("marcaModeloBien", marcaModelo);
        request.body("detalleBien", marcaModelo);
        request.body("numeroSerieBien", contexto.parametros.string("numeroSerie"));
    }


    public static ApiResponseMB insertarEmision(ContextoMB contexto) {
        Boolean esCliente = !contexto.persona().esEmpleado();
        String ramoProducto = esCliente ? RAMO_PRODUCTO_INDIVIDUAL : RAMO_PRODUCTO_EMPLEADO;
        String[] ramoProductoMascotas = ramoProducto.split("-");
        ApiRequestMB request = ApiMB.request("Insertar Emision", "seguro", "POST", "/v1/emision", contexto);


        request.body("especie", contexto.parametros.string("tipoMascota"));// PERRO O GATO EN MAYUSCULAS
        request.body("nombreMascotas", contexto.parametros.string("nombre"));
        request.body("raza", contexto.parametros.string("raza"));
        request.body("fecNacMascotas", contexto.parametros.string("fecha_nac"));
        request.body("emiPregunta1Emi", "S"); // TODO MEJORAR
        request.body("emiPregunta2Emi", "N"); // TODO MEJORAR

        request.body("premioOrigen", contexto.parametros.string("premioOrigen"));// precio_mensual de la oferta(plan) FORMATEADO

        request.body("sumaAseg01", contexto.parametros.string("sumaAseg01").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg01"));
        request.body("sumaAseg02", contexto.parametros.string("sumaAseg02").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg02"));
        request.body("sumaAseg03", contexto.parametros.string("sumaAseg03").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg03"));
        request.body("sumaAseg04", contexto.parametros.string("sumaAseg04").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg04"));
        request.body("sumaAseg05", contexto.parametros.string("sumaAseg05").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg05"));
        request.body("sumaAseg06", contexto.parametros.string("sumaAseg06").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg06"));

        Boolean esTC = contexto.parametros.bool("esTC");
        String numeroTarjetaOCuenta = contexto.parametros.string("emiNumTarjetaEmi");
        request.body("emiNombreTitularTCEmi", limpiarString(contexto.parametros.string("emiNombreTitularTCEmi")));
        request.body("emiApellidoTitularTCEmi", limpiarString(contexto.parametros.string("emiApellidoTitularTCEmi")));
        request.body("emiVencTCEmi", contexto.parametros.string("emiVencTCEmi"));


        request.body("nombreConyuge", limpiarString(contexto.persona().nombreConyuge()));// PROBAR
        request.body("feNacConyuge", contexto.parametros.string("feNacConyuge"));// PROBAR
        request.body("dniConyuge", contexto.parametros.string("dniConyuge"));// PROBAR


        request.body("tipoVivienda", contexto.parametros.string("tipoVivienda")); //PROBAR SI NO VIENE EN MASCOTAS
        request.body("viviendaPermanente", contexto.parametros.string("viviendaPermanente")); //PROBAR SI NO VIENE EN MASCOTAS
        request.body("desarrollaActividadesComercialesNTV", contexto.parametros.string("desarrollaActividadesComercialesNTV"));//PROBAR SI NO VIENE EN MASCOTAS
        request.body("paredesMaterial", contexto.parametros.string("paredesMaterial"));//PROBAR SI NO VIENE EN MASCOTAS


        Objeto domicilioPostal = RestPersona.domicilioPostal(contexto, contexto.persona().cuit());
        request.body("emiDrNumeroEmi", domicilioPostal.string("numero"));// (numero direccion)
        request.body("emiDrLocalidadEmi", domicilioPostal.string("idCiudad").isEmpty()
                ? "0" : domicilioPostal.string("idCiudad")); //  (id localidad)
        request.body("emiDrProvEmi", domicilioPostal.string("idProvincia").isEmpty()
                ? "0" : domicilioPostal.string("idProvincia")); // (id provincia)
        request.body("emiDrPisoEmi", contexto.parametros.string("emiDePisoEmi")); // NOSE (piso direccion)
        request.body("emiDrDtoEmi", contexto.parametros.string("emiDeDtoEmi")); // NOSE (departamento)
        request.body("emiDrCpEmi", domicilioPostal.string("idCodigoPostal")); // (codigo postal)
        String descLocalidad = limpiarString(domicilioPostal.string("ciudad"));// NOSE (nombre localidad)
        String calle = limpiarString(domicilioPostal.string("calle"));// NOSE
        request.body("emiDeNumeroEmi", domicilioPostal.string("numero"));
        request.body("emiDePisoEmi", contexto.parametros.string("emiDePisoEmi"));
        request.body("emiDeDtoEmi", contexto.parametros.string("emiDeDtoEmi"));
        request.body("emiDeProvEmi", domicilioPostal.string("idProvincia").isEmpty()
                ? "0" : domicilioPostal.string("idProvincia"));
        request.body("emiDeLocalidadEmi", domicilioPostal.string("idCiudad").isEmpty()
                ? "0" : domicilioPostal.string("idCiudad"));
        request.body("emiDeCpEmi", domicilioPostal.string("idCodigoPostal"));
        request.body("emiDbNumeroEmi", domicilioPostal.string("numero"));
        request.body("emiDbPisoEmi", contexto.parametros.string("emiDePisoEmi"));
        request.body("emiDbDtoEmi", contexto.parametros.string("emiDeDtoEmi"));
        request.body("emiDbProvEmi", domicilioPostal.string("idProvincia").isEmpty()
                ? "0" : domicilioPostal.string("idProvincia"));
        request.body("emiDbLocalidadEmi", domicilioPostal.string("idCiudad").isEmpty()
                ? "0" : domicilioPostal.string("idCiudad"));
        request.body("emiDbCpEmi", domicilioPostal.string("idCodigoPostal"));


        request.body("emiCelNumEmi", contexto.persona().celular());
        request.body("emiMailEmi", contexto.persona().email());
        request.body("clienteCuit", contexto.persona().cuit());
        request.body("emiApellido", limpiarString(contexto.persona().apellido()));
        request.body("emiNombreEmi", limpiarString(contexto.persona().nombre()));
        Persona persona = contexto.persona();
        request.body("emiTipoDocEmi", "01");// TODO HACERLO GENERICO
        request.body("emiNumDocEmi", contexto.persona().numeroDocumento());
        SimpleDateFormat formateador = new SimpleDateFormat("dd/MM/yyyy");
        request.body("emiFechaNacEmi", formateador.format(contexto.persona().fechaNacimiento()));
        request.body("emiSexoEmi", contexto.persona().cuit().startsWith("27") ? "F" : "M");
        request.body("emiPaisNacimientoEmi", contexto.persona().idNacionalidad().toString());
        request.body("emiPlanEmi", "001");// TODO, HACERLO GENERICO
        request.body("ramo", ramoProductoMascotas[0]);
        request.body("producto", ramoProductoMascotas[1]);
        request.body("emiPregunta3Emi", "S");// TODO HACERLO GENERICO
        request.body("emiPregunta4Emi", contexto.parametros.string("emiPregunta4Emi"));
        request.body("sumaAseg07", contexto.parametros.string("sumaAseg07").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg07"));
        request.body("sumaAseg08", contexto.parametros.string("sumaAseg08").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg08"));
        request.body("sumaAseg09", contexto.parametros.string("sumaAseg09").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg09"));
        request.body("sumaAseg10", contexto.parametros.string("sumaAseg10").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg10"));
        request.body("sumaAseg11", contexto.parametros.string("sumaAseg11").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg11"));
        request.body("sumaAseg12", contexto.parametros.string("sumaAseg12").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg12"));
        request.body("sumaAseg13", contexto.parametros.string("sumaAseg13").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg13"));
        request.body("sumaAseg14", contexto.parametros.string("sumaAseg14").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg14"));
        request.body("sumaAseg15", contexto.parametros.string("sumaAseg15").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg15"));
        request.body("sumaAseg16", contexto.parametros.string("sumaAseg16").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg16"));
        request.body("sumaAseg17", contexto.parametros.string("sumaAseg17").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg17"));
        request.body("sumaAseg18", contexto.parametros.string("sumaAseg18").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg18"));
        request.body("sumaAseg19", contexto.parametros.string("sumaAseg19").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg19"));
        request.body("sumaAseg20", contexto.parametros.string("sumaAseg20").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg20"));



        setDatosMovilidad( request, contexto );
        request.body("emiBenApellido1Emi", limpiarString(contexto.parametros.string("emiBenApellido1Emi")));
        request.body("emiBenNombre1Emi", limpiarString(contexto.parametros.string("emiBenNombre1Emi")));
        request.body("emiBenTipoDoc1Emi", contexto.parametros.string("emiBenTipoDoc1Emi"));
        request.body("emiBenDesTipoDoc1Emi", contexto.parametros.string("emiBenDesTipoDoc1Emi"));
        request.body("emiBenNumDoc1Emi", contexto.parametros.string("emiBenNumDoc1Emi"));
        request.body("emiBenSexo1Emi", contexto.parametros.string("emiBenSexo1Emi"));
        request.body("emiBenIdRel1Emi", contexto.parametros.string("emiBenIdRel1Emi"));
        request.body("emiBenDesRel1Emi", contexto.parametros.string("emiBenDesRel1Emi"));
        request.body("emiBenParticipacion1Emi", contexto.parametros.string("emiBenParticipacion1Emi"));
        request.body("emiBenTelefono1Emi", contexto.parametros.string("emiBenTelefono1Emi"));
        request.body("emiBenApellido2Emi", limpiarString(contexto.parametros.string("emiBenApellido2Emi")));
        request.body("emiBenNombre2Emi", limpiarString(contexto.parametros.string("emiBenNombre2Emi")));
        request.body("emiBenTipodoc2Emi", contexto.parametros.string("emiBenTipodoc2Emi"));
        request.body("emiDesTipoDoc2Emi", contexto.parametros.string("emiDesTipoDoc2Emi"));
        request.body("emiBenNumDoc2Emi", contexto.parametros.string("emiBenNumDoc2Emi"));
        request.body("emiBenSexo2Emi", contexto.parametros.string("emiBenSexo2Emi"));
        request.body("emiBenRel2Emi", contexto.parametros.string("emiBenRel2Emi"));
        request.body("emiBenDesRel2Emi", contexto.parametros.string("emiBenDesRel2Emi"));
        request.body("emiBenParticipacion2Emi", contexto.parametros.string("emiBenParticipacion2Emi"));
        request.body("emiBenTelefono2Emi", contexto.parametros.string("emiBenTelefono2Emi"));
        request.body("emiBenApellido3Emi", limpiarString(contexto.parametros.string("emiBenApellido3Emi")));
        request.body("emiBenNombre3Emi", limpiarString(contexto.parametros.string("emiBenNombre3Emi")));
        request.body("emiBenTipDoc3Emi", contexto.parametros.string("emiBenTipDoc3Emi"));
        request.body("emiBenDesTipoDoc3Emi", contexto.parametros.string("emiBenDesTipoDoc3Emi"));
        request.body("emiBenNumDoc3Emi", contexto.parametros.string("emiBenNumDoc3Emi"));
        request.body("emiBenSexo3Emi", contexto.parametros.string("emiBenSexo3Emi"));
        request.body("emiBenIdRel3Emi", contexto.parametros.string("emiBenIdRel3Emi"));
        request.body("emiBenDesRel3Emi", contexto.parametros.string("emiBenDesRel3Emi"));
        request.body("emiBenParticipacion3Emi", contexto.parametros.string("emiBenParticipacion3Emi"));
        request.body("emiBenTelefono3Emi", contexto.parametros.string("emiBenTelefono3Emi"));
        request.body("emiBenApellido4Emi", limpiarString(contexto.parametros.string("emiBenApellido4Emi")));
        request.body("emiBenNombre4Emi", limpiarString(contexto.parametros.string("emiBenNombre4Emi")));
        request.body("emiBentipoDoc4Emi", contexto.parametros.string("emiBentipoDoc4Emi"));
        request.body("emiBenDesTipoDoc4Emi", contexto.parametros.string("emiBenDesTipoDoc4Emi"));
        request.body("emiBenNumdoc4Emi", contexto.parametros.string("emiBenNumdoc4Emi"));
        request.body("emiBenSexo4Emi", contexto.parametros.string("emiBenSexo4Emi"));
        request.body("emiBenIdRel4Emi", contexto.parametros.string("emiBenIdRel4Emi"));
        request.body("emiBenDesRel4Emi", contexto.parametros.string("emiBenDesRel4Emi"));
        request.body("emiBenParticipacion4Emi", contexto.parametros.string("emiBenParticipacion4Emi"));
        request.body("emiBenTelefono4Emi", contexto.parametros.string("emiBenTelefono4Emi"));
        request.body("emiDbDesLocalidadEmi", descLocalidad);
        request.body("emiDeCalleEmi", calle);
        request.body("emiDeDesLocalidadEmi", descLocalidad);
        request.body("emiDbCalleEmi", calle);
        String prestamoCBUOTC = numeroTarjetaOCuenta.replace("-", "");
        request.body("emiddjjPeps", "");
        request.body("ddjj", "");
        request.body("rddjj", "");
        request.body("alturaCliente", "");
        request.body("pesoCliente", "");
        request.body("cimc", "");
        request.body("premioDestino", "");
        request.body("emiNumeroSerieNubicam", "");
        request.body("idProductor", "");
        request.body("metrosCuadrados", "");
        request.body("cantidadObjetos", "");
        request.body("promocion", "");
        request.body("poPromocion", "");
        request.body("emiRFTIPOEmi", "0");
        request.body("emiRFDESTIPOEmi", "BH");
        request.body("emiRFIDPRODEmi", "BH");
        request.body("emiRFIDOPEmi", "");
        request.body("emiUsuarioEmi", "BH");
        request.body("emiCanal1DesEmi", "1019-MOBILEBANKING");
        request.body("emiCanal2DesEmi", "0 - BUENOS AIRES");
        request.body("emiCanal4DesEmi", "MB");

        if (esTC) {
            request.body("emiCodigoMedio", "2");
            request.body("emiCodigoOrigen", "05");
        }
        else {
            request.body("emiCodigoMedio", "4");
            request.body("emiCodigoOrigen", "01");
        }

        if ( esCliente && esTC ) {
            request.body("idBonificacion", "0");
            request.body("cuotasBonificacion", "0");
            request.body("porcentajeBonificacion", "0");
            request.body("importeBonificacion", "0");
            request.body("importePremioReferencia", "0");
            request.body("importeBonificacionReferencia", "0");
        } else {
            request.body("idBonificacion", "");
            request.body("cuotasBonificacion", "");
            request.body("porcentajeBonificacion", "");
            request.body("importeBonificacion", "");
            request.body("importePremioReferencia", "");
            request.body("importeBonificacionReferencia", "");
        }
        request.body("combinatoria", "");
        request.body("emiDrDesLocalidadEmi", descLocalidad);
        request.body("emiDrCalleEmi", calle);
        request.body("emiNumTarjetaEmi", prestamoCBUOTC );
        request.body("emiNumCuenntaTarjetaEmi", "");
        request.body("emiNumCuentaEncriptado", "");
        request.body("emiIsDesencripta", "N");
        request.body("emiCanal1Emi", "1019");
        request.body("emiCanal2Emi", "0");
        request.body("emiCanal4Emi", "MB");
        request.body("emiTePaisEmi", "");
        request.body("emiTeAreaEmi", "");
        request.body("emiTeRefEmi", "");
        request.body("emiTeCaracEmi", "");
        request.body("emiTeNumEmi", "");
        request.body("emiCelPaisEmi", "");
        request.body("emiCelAreaEmi", "");
        request.body("emiCelRefEmi", "");
        request.body("emiCelCaracEmi", "");
        request.body("emiActividadEmi", "");
        request.body("poliza", "0");
        request.body("certificado", "0");
        request.body("estado", "P");
        request.body("observaciones", "");
        request.body("tipoTramite", "73");
        request.body("tipoNovedad", "EMI");
        request.body("canal1", "1019");
        request.body("canal2", "0");
        request.body("canal4", "MB");
        request.body("resultado", "0");
        request.body("usuario", "BH");
        request.body("lista", "");
        request.body("emiEstadoCivilEmi", "");
        request.body("emiCodProfesionEmi", "");
        request.permitirSinLogin = false;
        request.requiereIdCobis = true;
        return ApiMB.response(request);
    }

    public static ApiRequestMB getRequest(ContextoMB contexto, Boolean esCliente ) {
        Boolean esTC = contexto.parametros.bool("esTC");
        ApiRequestMB request = ApiMB.request("Insertar Emision", "seguro", "POST", "/v1/emision", contexto);
        Objeto domicilioPostal = RestPersona.domicilioPostal(contexto, contexto.persona().cuit());

        request.body("nombreConyuge", limpiarString(contexto.persona().nombreConyuge()));
        request.body("emiDrNumeroEmi", domicilioPostal.string("numero"));// (numero direccion)
        request.body("emiDrLocalidadEmi", domicilioPostal.string("idCiudad").isEmpty()
                ? "0" : domicilioPostal.string("idCiudad")); //  (id localidad)
        request.body("emiDrProvEmi", domicilioPostal.string("idProvincia").isEmpty()
                ? "0" : domicilioPostal.string("idProvincia")); // (id provincia)
        request.body("emiDrCpEmi", domicilioPostal.string("idCodigoPostal"));
        String descLocalidad = limpiarString(domicilioPostal.string("ciudad"));// NOSE (nombre localidad)
        String calle = limpiarString(domicilioPostal.string("calle"));
        request.body("emiDeNumeroEmi", domicilioPostal.string("numero"));
        request.body("emiDeProvEmi", domicilioPostal.string("idProvincia").isEmpty()
                ? "0" : domicilioPostal.string("idProvincia"));
        request.body("emiDeLocalidadEmi", domicilioPostal.string("idCiudad").isEmpty()
                ? "0" : domicilioPostal.string("idCiudad"));
        request.body("emiDeCpEmi", domicilioPostal.string("idCodigoPostal"));
        request.body("emiDbNumeroEmi", domicilioPostal.string("numero"));
        request.body("emiDbProvEmi", domicilioPostal.string("idProvincia").isEmpty()
                ? "0" : domicilioPostal.string("idProvincia"));
        request.body("emiDbLocalidadEmi", domicilioPostal.string("idCiudad").isEmpty()
                ? "0" : domicilioPostal.string("idCiudad"));
        request.body("emiDbCpEmi", domicilioPostal.string("idCodigoPostal"));
        request.body("emiCelNumEmi", contexto.persona().celular());
        request.body("emiMailEmi", contexto.persona().email());
        request.body("clienteCuit", contexto.persona().cuit());
        request.body("emiApellido", limpiarString(contexto.persona().apellido()));
        request.body("emiNombreEmi", limpiarString(contexto.persona().nombre()));
        Persona persona = contexto.persona();
        request.body("emiNumDocEmi", contexto.persona().numeroDocumento());
        SimpleDateFormat formateador = new SimpleDateFormat("dd/MM/yyyy");
        request.body("emiFechaNacEmi", formateador.format(contexto.persona().fechaNacimiento()));
        request.body("emiSexoEmi", contexto.persona().cuit().startsWith("27") ? "F" : "M");
        request.body("emiPaisNacimientoEmi", contexto.persona().idNacionalidad().toString());
        request.body("premioOrigen", contexto.parametros.string("premioOrigen"));// precio_mensual de la oferta(plan) FORMATEADO
        request.body("emiNombreTitularTCEmi", limpiarString(contexto.parametros.string("emiNombreTitularTCEmi")));
        request.body("emiApellidoTitularTCEmi", limpiarString(contexto.parametros.string("emiApellidoTitularTCEmi")));
        request.body("emiVencTCEmi", contexto.parametros.string("emiVencTCEmi"));
        request.body("feNacConyuge", contexto.parametros.string("feNacConyuge"));
        request.body("dniConyuge", contexto.parametros.string("dniConyuge"));
        request.body("tipoVivienda", contexto.parametros.string("tipoVivienda"));
        request.body("viviendaPermanente", contexto.parametros.string("viviendaPermanente"));
        request.body("desarrollaActividadesComercialesNTV", contexto.parametros.string("desarrollaActividadesComercialesNTV"));
        request.body("paredesMaterial", contexto.parametros.string("paredesMaterial"));
        request.body("emiDrPisoEmi", contexto.parametros.string("emiDePisoEmi"));
        request.body("emiDrDtoEmi", contexto.parametros.string("emiDeDtoEmi"));
        request.body("emiDePisoEmi", contexto.parametros.string("emiDePisoEmi"));
        request.body("emiDeDtoEmi", contexto.parametros.string("emiDeDtoEmi"));
        request.body("emiDbPisoEmi", contexto.parametros.string("emiDePisoEmi"));
        request.body("emiDbDtoEmi", contexto.parametros.string("emiDeDtoEmi"));
        request.body("emiPregunta4Emi", "S");
        request.body("sumaAseg01", contexto.parametros.string("sumaAseg01").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg01"));
        request.body("sumaAseg02", contexto.parametros.string("sumaAseg02").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg02"));
        request.body("sumaAseg03", contexto.parametros.string("sumaAseg03").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg03"));
        request.body("sumaAseg04", contexto.parametros.string("sumaAseg04").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg04"));
        request.body("sumaAseg05", contexto.parametros.string("sumaAseg05").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg05"));
        request.body("sumaAseg06", contexto.parametros.string("sumaAseg06").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg06"));
        request.body("sumaAseg07", contexto.parametros.string("sumaAseg07").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg07"));
        request.body("sumaAseg08", contexto.parametros.string("sumaAseg08").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg08"));
        request.body("sumaAseg09", contexto.parametros.string("sumaAseg09").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg09"));
        request.body("sumaAseg10", contexto.parametros.string("sumaAseg10").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg10"));
        request.body("sumaAseg11", contexto.parametros.string("sumaAseg11").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg11"));
        request.body("sumaAseg12", contexto.parametros.string("sumaAseg12").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg12"));
        request.body("sumaAseg13", contexto.parametros.string("sumaAseg13").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg13"));
        request.body("sumaAseg14", contexto.parametros.string("sumaAseg14").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg14"));
        request.body("sumaAseg15", contexto.parametros.string("sumaAseg15").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg15"));
        request.body("sumaAseg16", contexto.parametros.string("sumaAseg16").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg16"));
        request.body("sumaAseg17", contexto.parametros.string("sumaAseg17").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg17"));
        request.body("sumaAseg18", contexto.parametros.string("sumaAseg18").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg18"));
        request.body("sumaAseg19", contexto.parametros.string("sumaAseg19").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg19"));
        request.body("sumaAseg20", contexto.parametros.string("sumaAseg20").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg20"));
        request.body("emiBenApellido1Emi", limpiarString(contexto.parametros.string("emiBenApellido1Emi")));
        request.body("emiBenNombre1Emi", limpiarString(contexto.parametros.string("emiBenNombre1Emi")));
        request.body("emiBenTipoDoc1Emi", contexto.parametros.string("emiBenTipoDoc1Emi"));
        request.body("emiBenDesTipoDoc1Emi", contexto.parametros.string("emiBenDesTipoDoc1Emi"));
        request.body("emiBenNumDoc1Emi", contexto.parametros.string("emiBenNumDoc1Emi"));
        request.body("emiBenSexo1Emi", contexto.parametros.string("emiBenSexo1Emi"));
        request.body("emiBenIdRel1Emi", contexto.parametros.string("emiBenIdRel1Emi"));
        request.body("emiBenDesRel1Emi", contexto.parametros.string("emiBenDesRel1Emi"));
        request.body("emiBenParticipacion1Emi", contexto.parametros.string("emiBenParticipacion1Emi"));
        request.body("emiBenTelefono1Emi", contexto.parametros.string("emiBenTelefono1Emi"));
        request.body("emiBenApellido2Emi", limpiarString(contexto.parametros.string("emiBenApellido2Emi")));
        request.body("emiBenNombre2Emi", limpiarString(contexto.parametros.string("emiBenNombre2Emi")));
        request.body("emiBenTipodoc2Emi", contexto.parametros.string("emiBenTipodoc2Emi"));
        request.body("emiDesTipoDoc2Emi", contexto.parametros.string("emiDesTipoDoc2Emi"));
        request.body("emiBenNumDoc2Emi", contexto.parametros.string("emiBenNumDoc2Emi"));
        request.body("emiBenSexo2Emi", contexto.parametros.string("emiBenSexo2Emi"));
        request.body("emiBenRel2Emi", contexto.parametros.string("emiBenRel2Emi"));
        request.body("emiBenDesRel2Emi", contexto.parametros.string("emiBenDesRel2Emi"));
        request.body("emiBenParticipacion2Emi", contexto.parametros.string("emiBenParticipacion2Emi"));
        request.body("emiBenTelefono2Emi", contexto.parametros.string("emiBenTelefono2Emi"));
        request.body("emiBenApellido3Emi", limpiarString(contexto.parametros.string("emiBenApellido3Emi")));
        request.body("emiBenNombre3Emi", limpiarString(contexto.parametros.string("emiBenNombre3Emi")));
        request.body("emiBenTipDoc3Emi", contexto.parametros.string("emiBenTipDoc3Emi"));
        request.body("emiBenDesTipoDoc3Emi", contexto.parametros.string("emiBenDesTipoDoc3Emi"));
        request.body("emiBenNumDoc3Emi", contexto.parametros.string("emiBenNumDoc3Emi"));
        request.body("emiBenSexo3Emi", contexto.parametros.string("emiBenSexo3Emi"));
        request.body("emiBenIdRel3Emi", contexto.parametros.string("emiBenIdRel3Emi"));
        request.body("emiBenDesRel3Emi", contexto.parametros.string("emiBenDesRel3Emi"));
        request.body("emiBenParticipacion3Emi", contexto.parametros.string("emiBenParticipacion3Emi"));
        request.body("emiBenTelefono3Emi", contexto.parametros.string("emiBenTelefono3Emi"));
        request.body("emiBenApellido4Emi", limpiarString(contexto.parametros.string("emiBenApellido4Emi")));
        request.body("emiBenNombre4Emi", limpiarString(contexto.parametros.string("emiBenNombre4Emi")));
        request.body("emiBentipoDoc4Emi", contexto.parametros.string("emiBentipoDoc4Emi"));
        request.body("emiBenDesTipoDoc4Emi", contexto.parametros.string("emiBenDesTipoDoc4Emi"));
        request.body("emiBenNumdoc4Emi", contexto.parametros.string("emiBenNumdoc4Emi"));
        request.body("emiBenSexo4Emi", contexto.parametros.string("emiBenSexo4Emi"));
        request.body("emiBenIdRel4Emi", contexto.parametros.string("emiBenIdRel4Emi"));
        request.body("emiBenDesRel4Emi", contexto.parametros.string("emiBenDesRel4Emi"));
        request.body("emiBenParticipacion4Emi", contexto.parametros.string("emiBenParticipacion4Emi"));
        request.body("emiBenTelefono4Emi", contexto.parametros.string("emiBenTelefono4Emi"));
        request.body("emiddjjPeps", "");
        request.body("ddjj", "");
        request.body("rddjj", "");
        request.body("alturaCliente", "");
        request.body("pesoCliente", "");
        request.body("cimc", "");
        request.body("premioDestino", "");
        request.body("emiNumeroSerieNubicam", "");
        request.body("metrosCuadrados", "");
        request.body("cantidadObjetos", "");
        request.body("promocion", "");
        request.body("poPromocion", "");
        request.body("emiRFTIPOEmi", "0");
        request.body("emiRFDESTIPOEmi", "BH");
        request.body("emiRFIDPRODEmi", "BH");
        request.body("emiRFIDOPEmi", "");
        request.body("emiUsuarioEmi", "BH");
        request.body("emiCanal1DesEmi", "1019-MOBILEBANKING");
        request.body("emiCanal2DesEmi", "0 - BUENOS AIRES");
        request.body("emiCanal4DesEmi", "MB");

        if (esTC) {
            request.body("emiCodigoMedio", "2");
            request.body("emiCodigoOrigen", "05");
        }
         else {
            request.body("emiCodigoMedio", "4");
            request.body("emiCodigoOrigen", "01");
        }

        if ( esCliente && esTC ) {
            request.body("idBonificacion", "0");
            request.body("cuotasBonificacion", "0");
            request.body("porcentajeBonificacion", "0");
            request.body("importeBonificacion", "0");
            request.body("importePremioReferencia", "0");
            request.body("importeBonificacionReferencia", "0");
        } else {
            request.body("idBonificacion", "");
            request.body("cuotasBonificacion", "");
            request.body("porcentajeBonificacion", "");
            request.body("importeBonificacion", "");
            request.body("importePremioReferencia", "");
            request.body("importeBonificacionReferencia", "");
        }
        request.body("combinatoria", "");
        request.body("emiNumCuenntaTarjetaEmi", "");
        request.body("emiNumCuentaEncriptado", "");
        request.body("emiIsDesencripta", "N");
        request.body("emiCanal1Emi", "1019");
        request.body("emiCanal2Emi", "0");
        request.body("emiCanal4Emi", "MB");
        request.body("emiTePaisEmi", "");
        request.body("emiTeAreaEmi", "");
        request.body("emiTeRefEmi", "");
        request.body("emiTeCaracEmi", "");
        request.body("emiTeNumEmi", "");
        request.body("emiCelPaisEmi", "");
        request.body("emiCelAreaEmi", "");
        request.body("emiCelRefEmi", "");
        request.body("emiCelCaracEmi", "");
        request.body("emiActividadEmi", "");
        request.body("poliza", "0");
        request.body("certificado", "0");
        request.body("estado", "P");
        request.body("observaciones", "");
        request.body("tipoTramite", "73");
        request.body("tipoNovedad", "EMI");
        request.body("canal1", "1019");
        request.body("canal2", "0");
        request.body("canal4", "MB");
        request.body("resultado", "0");
        request.body("usuario", "BH");
        request.body("lista", "");
        request.body("emiEstadoCivilEmi", "");
        request.body("emiCodProfesionEmi", "");
        String numeroTarjetaOCuenta = contexto.parametros.string("emiNumTarjetaEmi");
        request.body("emiDbDesLocalidadEmi", descLocalidad);
        request.body("emiDeCalleEmi", calle);
        request.body("emiDeDesLocalidadEmi", descLocalidad);
        request.body("emiDbCalleEmi", calle);
        String prestamoCBUOTC = numeroTarjetaOCuenta.replace("-", "");
        request.body("emiDrDesLocalidadEmi", descLocalidad);
        request.body("emiDrCalleEmi", calle);
        request.body("emiNumTarjetaEmi", prestamoCBUOTC );
        request.body("emiPregunta3Emi", "S"); // TODO TERMINOS Y CONDICIONES
        request.body("emiTipoDocEmi", "01"); // TODO SE COMPROBO EN LOS SEGUROS DE MOVILIDAD Y MASCOTAS Y ES ESTATICO, COMPROBAR EN NUEVOS SEGUROS

        String marca = contexto.parametros.string("marca");
        String modelo = contexto.parametros.string("modelo");
        String marcaModelo = marca + " " + modelo;
        request.body("tipoBien", contexto.parametros.string("tipoMovilidad"));// BICICLETA O MONOPATIN EN MAYUSCULAS
        request.body("marcaModeloBien", marcaModelo);
        request.body("detalleBien", marcaModelo);
        request.body("numeroSerieBien", contexto.parametros.string("numeroSerie"));

        request.body("especie", contexto.parametros.string("tipoMascota"));// PERRO O GATO EN MAYUSCULAS
        request.body("nombreMascotas", contexto.parametros.string("nombre"));
        request.body("raza", contexto.parametros.string("raza"));
        request.body("fecNacMascotas", contexto.parametros.string("fecha_nac"));

        request.permitirSinLogin = false;
        request.requiereIdCobis = true;
        return request;
    }

    public static ApiRequestMB getRequestHogar(ContextoMB contexto, Boolean esCliente ) {
        Boolean esTC = contexto.parametros.bool("esTC");
        ApiRequestMB request = ApiMB.request("Insertar Emision", "seguro", "POST", "/v1/emision", contexto);
        Objeto domicilioPostal = RestPersona.domicilioPostal(contexto, contexto.persona().cuit());

        request.body("nombreConyuge", limpiarString(contexto.persona().nombreConyuge()));
        request.body("emiDrNumeroEmi", contexto.parametros.string("emiDeNumeroEmi"));// (numero direccion)
        request.body("emiDrLocalidadEmi", domicilioPostal.string("idCiudad").isEmpty()
                ? "0" : domicilioPostal.string("idCiudad")); //  (id localidad)
        request.body("emiDrProvEmi", domicilioPostal.string("idProvincia").isEmpty()
                ? "0" : domicilioPostal.string("idProvincia")); // (id provincia)
        request.body("emiDrCpEmi", contexto.parametros.string("emiDeCpEmi"));
        String descLocalidad = limpiarString(contexto.parametros.string("emiDeDesLocalidadEmi"));// NOSE (nombre localidad)
        String calle = limpiarString(contexto.parametros.string("emiDeCalleEmi"));
        request.body("emiDeNumeroEmi", contexto.parametros.string("emiDeNumeroEmi"));
        request.body("emiDeProvEmi", domicilioPostal.string("idProvincia").isEmpty()
                ? "0" : domicilioPostal.string("idProvincia"));
        request.body("emiDeLocalidadEmi", domicilioPostal.string("idCiudad").isEmpty()
                ? "0" : domicilioPostal.string("idCiudad"));
        request.body("emiDeCpEmi", contexto.parametros.string("emiDeCpEmi"));
        request.body("emiDbNumeroEmi", contexto.parametros.string("emiDeNumeroEmi"));
        request.body("emiDbProvEmi", domicilioPostal.string("idProvincia").isEmpty()
                ? "0" : domicilioPostal.string("idProvincia"));
        request.body("emiDbLocalidadEmi", domicilioPostal.string("idCiudad").isEmpty()
                ? "0" : domicilioPostal.string("idCiudad"));
        request.body("emiDbCpEmi", domicilioPostal.string("idCodigoPostal"));
        request.body("emiCelNumEmi", contexto.persona().celular());
        request.body("emiMailEmi", contexto.persona().email());
        request.body("clienteCuit", contexto.persona().cuit());
        request.body("emiApellido", limpiarString(contexto.persona().apellido()));
        request.body("emiNombreEmi", limpiarString(contexto.persona().nombre()));
        Persona persona = contexto.persona();
        request.body("emiNumDocEmi", contexto.persona().numeroDocumento());
        SimpleDateFormat formateador = new SimpleDateFormat("dd/MM/yyyy");
        request.body("emiFechaNacEmi", formateador.format(contexto.persona().fechaNacimiento()));
        request.body("emiSexoEmi", contexto.persona().cuit().startsWith("27") ? "F" : "M");
        request.body("emiPaisNacimientoEmi", contexto.persona().idNacionalidad().toString());
        request.body("premioOrigen", contexto.parametros.string("premioOrigen"));// precio_mensual de la oferta(plan) FORMATEADO
        request.body("emiNombreTitularTCEmi", limpiarString(contexto.parametros.string("emiNombreTitularTCEmi")));
        request.body("emiApellidoTitularTCEmi", limpiarString(contexto.parametros.string("emiApellidoTitularTCEmi")));
        request.body("emiVencTCEmi", contexto.parametros.string("emiVencTCEmi"));
        request.body("feNacConyuge", contexto.parametros.string("feNacConyuge"));
        request.body("dniConyuge", contexto.parametros.string("dniConyuge"));
        request.body("tipoVivienda", contexto.parametros.string("tipoVivienda"));
        request.body("viviendaPermanente", contexto.parametros.string("viviendaPermanente"));
        request.body("desarrollaActividadesComercialesNTV", contexto.parametros.string("desarrollaActividadesComercialesNTV"));
        request.body("paredesMaterial", contexto.parametros.string("paredesMaterial"));
        request.body("emiDrPisoEmi", contexto.parametros.string("emiDePisoEmi"));
        request.body("emiDrDtoEmi", contexto.parametros.string("emiDeDtoEmi"));
        request.body("emiDePisoEmi", contexto.parametros.string("emiDePisoEmi"));
        request.body("emiDeDtoEmi", contexto.parametros.string("emiDeDtoEmi"));
        request.body("emiDbPisoEmi", contexto.parametros.string("emiDePisoEmi"));
        request.body("emiDbDtoEmi", contexto.parametros.string("emiDeDtoEmi"));
        request.body("emiPregunta4Emi", contexto.parametros.string("emiPregunta4Emi"));
        request.body("sumaAseg01", contexto.parametros.string("sumaAseg01").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg01"));
        request.body("sumaAseg02", contexto.parametros.string("sumaAseg02").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg02"));
        request.body("sumaAseg03", contexto.parametros.string("sumaAseg03").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg03"));
        request.body("sumaAseg04", contexto.parametros.string("sumaAseg04").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg04"));
        request.body("sumaAseg05", contexto.parametros.string("sumaAseg05").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg05"));
        request.body("sumaAseg06", contexto.parametros.string("sumaAseg06").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg06"));
        request.body("sumaAseg07", contexto.parametros.string("sumaAseg07").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg07"));
        request.body("sumaAseg08", contexto.parametros.string("sumaAseg08").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg08"));
        request.body("sumaAseg09", contexto.parametros.string("sumaAseg09").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg09"));
        request.body("sumaAseg10", contexto.parametros.string("sumaAseg10").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg10"));
        request.body("sumaAseg11", contexto.parametros.string("sumaAseg11").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg11"));
        request.body("sumaAseg12", contexto.parametros.string("sumaAseg12").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg12"));
        request.body("sumaAseg13", contexto.parametros.string("sumaAseg13").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg13"));
        request.body("sumaAseg14", contexto.parametros.string("sumaAseg14").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg14"));
        request.body("sumaAseg15", contexto.parametros.string("sumaAseg15").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg15"));
        request.body("sumaAseg16", contexto.parametros.string("sumaAseg16").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg16"));
        request.body("sumaAseg17", contexto.parametros.string("sumaAseg17").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg17"));
        request.body("sumaAseg18", contexto.parametros.string("sumaAseg18").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg18"));
        request.body("sumaAseg19", contexto.parametros.string("sumaAseg19").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg19"));
        request.body("sumaAseg20", contexto.parametros.string("sumaAseg20").isEmpty()
                ? "0" : contexto.parametros.string("sumaAseg20"));
        request.body("emiBenApellido1Emi", limpiarString(contexto.parametros.string("emiBenApellido1Emi")));
        request.body("emiBenNombre1Emi", limpiarString(contexto.parametros.string("emiBenNombre1Emi")));
        request.body("emiBenTipoDoc1Emi", contexto.parametros.string("emiBenTipoDoc1Emi"));
        request.body("emiBenDesTipoDoc1Emi", contexto.parametros.string("emiBenDesTipoDoc1Emi"));
        request.body("emiBenNumDoc1Emi", contexto.parametros.string("emiBenNumDoc1Emi"));
        request.body("emiBenSexo1Emi", contexto.parametros.string("emiBenSexo1Emi"));
        request.body("emiBenIdRel1Emi", contexto.parametros.string("emiBenIdRel1Emi"));
        request.body("emiBenDesRel1Emi", contexto.parametros.string("emiBenDesRel1Emi"));
        request.body("emiBenParticipacion1Emi", contexto.parametros.string("emiBenParticipacion1Emi"));
        request.body("emiBenTelefono1Emi", contexto.parametros.string("emiBenTelefono1Emi"));
        request.body("emiBenApellido2Emi", limpiarString(contexto.parametros.string("emiBenApellido2Emi")));
        request.body("emiBenNombre2Emi", limpiarString(contexto.parametros.string("emiBenNombre2Emi")));
        request.body("emiBenTipodoc2Emi", contexto.parametros.string("emiBenTipodoc2Emi"));
        request.body("emiDesTipoDoc2Emi", contexto.parametros.string("emiDesTipoDoc2Emi"));
        request.body("emiBenNumDoc2Emi", contexto.parametros.string("emiBenNumDoc2Emi"));
        request.body("emiBenSexo2Emi", contexto.parametros.string("emiBenSexo2Emi"));
        request.body("emiBenRel2Emi", contexto.parametros.string("emiBenRel2Emi"));
        request.body("emiBenDesRel2Emi", contexto.parametros.string("emiBenDesRel2Emi"));
        request.body("emiBenParticipacion2Emi", contexto.parametros.string("emiBenParticipacion2Emi"));
        request.body("emiBenTelefono2Emi", contexto.parametros.string("emiBenTelefono2Emi"));
        request.body("emiBenApellido3Emi", limpiarString(contexto.parametros.string("emiBenApellido3Emi")));
        request.body("emiBenNombre3Emi", limpiarString(contexto.parametros.string("emiBenNombre3Emi")));
        request.body("emiBenTipDoc3Emi", contexto.parametros.string("emiBenTipDoc3Emi"));
        request.body("emiBenDesTipoDoc3Emi", contexto.parametros.string("emiBenDesTipoDoc3Emi"));
        request.body("emiBenNumDoc3Emi", contexto.parametros.string("emiBenNumDoc3Emi"));
        request.body("emiBenSexo3Emi", contexto.parametros.string("emiBenSexo3Emi"));
        request.body("emiBenIdRel3Emi", contexto.parametros.string("emiBenIdRel3Emi"));
        request.body("emiBenDesRel3Emi", contexto.parametros.string("emiBenDesRel3Emi"));
        request.body("emiBenParticipacion3Emi", contexto.parametros.string("emiBenParticipacion3Emi"));
        request.body("emiBenTelefono3Emi", contexto.parametros.string("emiBenTelefono3Emi"));
        request.body("emiBenApellido4Emi", limpiarString(contexto.parametros.string("emiBenApellido4Emi")));
        request.body("emiBenNombre4Emi", limpiarString(contexto.parametros.string("emiBenNombre4Emi")));
        request.body("emiBentipoDoc4Emi", contexto.parametros.string("emiBentipoDoc4Emi"));
        request.body("emiBenDesTipoDoc4Emi", contexto.parametros.string("emiBenDesTipoDoc4Emi"));
        request.body("emiBenNumdoc4Emi", contexto.parametros.string("emiBenNumdoc4Emi"));
        request.body("emiBenSexo4Emi", contexto.parametros.string("emiBenSexo4Emi"));
        request.body("emiBenIdRel4Emi", contexto.parametros.string("emiBenIdRel4Emi"));
        request.body("emiBenDesRel4Emi", contexto.parametros.string("emiBenDesRel4Emi"));
        request.body("emiBenParticipacion4Emi", contexto.parametros.string("emiBenParticipacion4Emi"));
        request.body("emiBenTelefono4Emi", contexto.parametros.string("emiBenTelefono4Emi"));
        request.body("emiddjjPeps", "");
        request.body("ddjj", "");
        request.body("rddjj", "");
        request.body("alturaCliente", "");
        request.body("pesoCliente", "");
        request.body("cimc", "");
        request.body("premioDestino", "");
        request.body("emiNumeroSerieNubicam", "");
        request.body("idProductor", "");
        request.body("metrosCuadrados", "");
        request.body("cantidadObjetos", "");
        request.body("promocion", "");
        request.body("poPromocion", "");
        request.body("emiRFTIPOEmi", "0");
        request.body("emiRFDESTIPOEmi", "BH");
        request.body("emiRFIDPRODEmi", "BH");
        request.body("emiRFIDOPEmi", "");
        request.body("emiUsuarioEmi", "BH");
        request.body("emiCanal1DesEmi", "1019-MOBILEBANKING");
        request.body("emiCanal2DesEmi", "0 - BUENOS AIRES");
        request.body("emiCanal4DesEmi", "MB");

        if (esTC) {
            request.body("emiCodigoMedio", "2");
            request.body("emiCodigoOrigen", "05");
        }
        else {
            request.body("emiCodigoMedio", "4");
            request.body("emiCodigoOrigen", "01");
        }

        if ( esCliente && esTC ) {
            request.body("idBonificacion", "0");
            request.body("cuotasBonificacion", "0");
            request.body("porcentajeBonificacion", "0");
            request.body("importeBonificacion", "0");
            request.body("importePremioReferencia", "0");
            request.body("importeBonificacionReferencia", "0");
        } else {
            request.body("idBonificacion", "");
            request.body("cuotasBonificacion", "");
            request.body("porcentajeBonificacion", "");
            request.body("importeBonificacion", "");
            request.body("importePremioReferencia", "");
            request.body("importeBonificacionReferencia", "");
        }
        request.body("combinatoria", "");
        request.body("emiNumCuenntaTarjetaEmi", "");
        request.body("emiNumCuentaEncriptado", "");
        request.body("emiIsDesencripta", "N");
        request.body("emiCanal1Emi", "1019");
        request.body("emiCanal2Emi", "0");
        request.body("emiCanal4Emi", "MB");
        request.body("emiTePaisEmi", "");
        request.body("emiTeAreaEmi", "");
        request.body("emiTeRefEmi", "");
        request.body("emiTeCaracEmi", "");
        request.body("emiTeNumEmi", "");
        request.body("emiCelPaisEmi", "");
        request.body("emiCelAreaEmi", "");
        request.body("emiCelRefEmi", "");
        request.body("emiCelCaracEmi", "");
        request.body("emiActividadEmi", "");
        request.body("poliza", "0");
        request.body("certificado", "0");
        request.body("estado", "P");
        request.body("observaciones", "");
        request.body("tipoTramite", "73");
        request.body("tipoNovedad", "EMI");
        request.body("canal1", "1019");
        request.body("canal2", "0");
        request.body("canal4", "MB");
        request.body("resultado", "0");
        request.body("usuario", "BH");
        request.body("lista", "");
        request.body("emiEstadoCivilEmi", "");
        request.body("emiCodProfesionEmi", "");
        String numeroTarjetaOCuenta = contexto.parametros.string("emiNumTarjetaEmi");
        request.body("emiDbDesLocalidadEmi", descLocalidad);
        request.body("emiDeCalleEmi", calle);
        request.body("emiDeDesLocalidadEmi", descLocalidad);
        request.body("emiDbCalleEmi", calle);
        String prestamoCBUOTC = numeroTarjetaOCuenta.replace("-", "");
        request.body("emiDrDesLocalidadEmi", descLocalidad);
        request.body("emiDrCalleEmi", calle);
        request.body("emiNumTarjetaEmi", prestamoCBUOTC );
        request.body("emiPregunta3Emi", "S"); // TODO TERMINOS Y CONDICIONES
        request.body("emiTipoDocEmi", "01"); // TODO SE COMPROBO EN LOS SEGUROS DE MOVILIDAD Y MASCOTAS Y ES ESTATICO, COMPROBAR EN NUEVOS SEGUROS

        String marca = contexto.parametros.string("marca");
        String modelo = contexto.parametros.string("modelo");
        String marcaModelo = marca + " " + modelo;
        request.body("tipoBien", contexto.parametros.string("tipoMovilidad"));// BICICLETA O MONOPATIN EN MAYUSCULAS
        request.body("marcaModeloBien", marcaModelo);
        request.body("detalleBien", marcaModelo);
        request.body("numeroSerieBien", contexto.parametros.string("numeroSerie"));

        request.body("especie", contexto.parametros.string("tipoMascota"));// PERRO O GATO EN MAYUSCULAS
        request.body("nombreMascotas", contexto.parametros.string("nombre"));
        request.body("raza", contexto.parametros.string("raza"));
        request.body("fecNacMascotas", contexto.parametros.string("fecha_nac"));
        request.body("desarrollaActividadesComercialesNTV", "S");
        request.body("viviendaPermanente", "S");
        request.body("paredesMaterial", "S");
        request.body("tipoVivienda", contexto.parametros.string("tipoVivienda"));
        request.body("emiPregunta1Emi", contexto.parametros.string("emiPregunta1Emi"));
        request.body("emiPregunta2Emi", contexto.parametros.string("emiPregunta2Emi"));
        request.body("emiPlanEmi", "002");

        request.permitirSinLogin = false;
        request.requiereIdCobis = true;
        return request;
    }

    public static String limpiarString(String cadena) {
        String stringLimpio = Normalizer.normalize(cadena, Normalizer.Form.NFD);
        // Quitar caracteres no ASCII excepto la enie, interrogacion que abre,
        // exclamacion que abre, grados, U con dieresis.
        stringLimpio = stringLimpio.replaceAll("[^\\p{ASCII}(N\u0303)(n\u0303)(\u00A1)(\u00BF)(\u00B0)(U\u0308)(u\u0308)]", "");
        // Regresar a la forma compuesta, para poder comparar la enie con la tabla de
        // valores
        stringLimpio = Normalizer.normalize(stringLimpio, Normalizer.Form.NFC);
        return stringLimpio;
    }

    public static ApiResponseMB obtenerToken(ContextoMB contexto) {
        ApiRequestMB request = ApiMB.request("Obtener token", "seguro", "GET", "/v1/token-salesforce", contexto);
        request.permitirSinLogin = false;
        request.requiereIdCobis = true;
        request.cacheSesion = true;
        return ApiMB.response(request);
    }

    public static ApiResponseMB obtenerProductos(ContextoMB contexto, String cuit) {
        ApiRequestMB request = ApiMB.request("Obtener productos", "seguro", "GET", "/v1/{cuit}/productos-consolidada", contexto);
        request.path("idCliente", contexto.idCobis());
        request.path("cuit", cuit);
        request.permitirSinLogin = false;
        request.requiereIdCobis = true;
        request.cacheSesion = true;
        return ApiMB.response(request, cuit);
    }

    private static ApiResponseMB obtenerOferta(ContextoMB contexto, String sessionId) {
        ApiRequestMB request = ApiMB.request("Obtener ofertas", "seguro", "GET", "/v1/ofertas/{sessionId}/", contexto);
        request.path("sessionId", sessionId);
        request.permitirSinLogin = false;
        request.requiereIdCobis = true;
        request.cacheSesion = true;
        return ApiMB.response(request);
    }

}
