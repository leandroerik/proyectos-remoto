package ar.com.hipotecario.mobile.api;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ar.com.hipotecario.backend.servicio.api.transmit.JourneyTransmitEnum;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.excepcion.ApiExceptionMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.Persona;
import ar.com.hipotecario.mobile.servicio.RestCatalogo;
import ar.com.hipotecario.mobile.servicio.RestEcheq;
import ar.com.hipotecario.mobile.servicio.RestPersona;

public class MBEcheq {

    private static String flagEcheq = "prendido_echeq_api";

    private static List<String> echeqStatus = Arrays.asList("Todos:TODOS", "Activo:ACTIVO", "Anulado:ANULADO", "Caducado:CADUCADO", "Depositado:DEPOSITADO", "Devolución pendiente:DEVOLUCION PENDIENTE", "Emitido:EMITIDO", "En custodia:EN CUSTODIA", "Endosado:ENDOSADO", "Pagado:PAGADO", "Presentado:PRESENTADO", "Rechazado:RECHAZADO", "Repudiado:REPUDIADO", "Solicitado acuerdo:SOLICITANDO ACUERDO");

    private static List<String> echeqEndorsementTypes = Arrays.asList("Nominado:NOM", "Para negociación:NEG");

    public static RespuestaMB enableAccount(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), flagEcheq)) {
            try {
                Persona persona = contexto.persona();
                String cuit = persona.cuit();
                String nombre = persona.nombreCompleto();
                Objeto cuenta = new Objeto();
                List<Objeto> localAccountList = new ArrayList<>();
                boolean encontro = false;
                String domicilio = "no encontro";
                String cp = "0000";
                for (Objeto domicilioEntry : obtenerDomicilios(contexto, cuit)) {
                    if ("LE".equals(domicilioEntry.string("idTipoDomicilio"))) {
                        domicilio = domicilioEntry.string("calle") + " " + domicilioEntry.string("numero") + ", " + domicilioEntry.string("ciudad");
                        cp = domicilioEntry.string("idCodigoPostal");
                        encontro = true;
                        break;
                    }
                }
                if (!encontro) {
                    for (Objeto domicilioEntry : obtenerDomicilios(contexto, cuit)) {
                        domicilio = domicilioEntry.string("calle") + " " + domicilioEntry.string("numero") + ", " + domicilioEntry.string("ciudad");
                        cp = domicilioEntry.string("idCodigoPostal");
                        encontro = true;
                        break;
                    }
                }
                if ("".equals(contexto.parametros.string("id_producto", ""))) {
                    for (Cuenta cuentaCliente : contexto.cuentas()) {
                        String cuentaSucursal = obtenerSucursalPorVe(cuentaCliente.sucursal(), persona.sucursal(), contexto);
                        if (cuentaCliente.esCuentaCorriente()) {
                            cuenta = new Objeto();
                            int codSucInt = Integer.valueOf(cuentaSucursal);
                            String codSuc = String.format("%04d", codSucInt);
                            cuenta.set("sucursalCodigo", codSuc);
                            /*
                             * for(Objeto sucursal : obtenerSucursalesv2(contexto)) {
                             * if(sucursal.string("CodSucursal", "").equals(persona.sucursal())) {
                             * cuenta.set("sucursalNombre",
                             * normalizarCadena(sucursal.get("DesSucursal").toString()));
                             * cuenta.set("sucursalDomicilio",
                             * normalizarCadena(sucursal.get("Domicilio").toString()));
                             * cuenta.set("sucursalCp", sucursal.get("CodigoPostalSucursal"));
                             * cuenta.set("sucursalProvincia",
                             * normalizarCadena(sucursal.get("NomProvincia").toString())); break; } }
                             */
                            if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_mejora_echeq")) {
                                Optional<Objeto> sucursalObj = obtenerSucursalPersona(contexto, cuentaSucursal);
                                if (sucursalObj.isPresent()) {
                                    cuenta.set("sucursalNombre", normalizarCadena(sucursalObj.get().get("DesSucursal").toString()));
                                    cuenta.set("sucursalDomicilio", normalizarCadena(sucursalObj.get().get("Domicilio").toString()));
                                    cuenta.set("sucursalCp", sucursalObj.get().get("CodigoPostalSucursal"));
                                    cuenta.set("sucursalProvincia", normalizarCadena(sucursalObj.get().get("NomProvincia").toString()));
                                }
                            } else {
                                try {
                                    for (Objeto sucursal : obtenerSucursales(contexto)) {
                                        if (sucursal.string("CodSucursal", "").equals(cuentaSucursal)) {
                                            cuenta.set("sucursalNombre", normalizarCadena(sucursal.get("DesSucursal").toString()));
                                            cuenta.set("sucursalDomicilio", normalizarCadena(sucursal.get("Domicilio").toString()));
                                            cuenta.set("sucursalCp", sucursal.get("CodigoPostalSucursal"));
                                            cuenta.set("sucursalProvincia", normalizarCadena(sucursal.get("NomProvincia").toString()));
                                        }
                                    }
                                } catch (Exception e) {
                                    cuenta.set("sucursalNombre", "Casa central");
                                    cuenta.set("sucursalDomicilio", "Reconquista 101");
                                    cuenta.set("sucursalCp", "1003");
                                    cuenta.set("sucursalProvincia", "caba");
                                }
                            }

                            cuenta.set("emisorCbu", cuentaCliente.cbu());
                            String accountNumber = cuentaCliente.numero();
                            accountNumber = accountNumber.substring(accountNumber.length() - 11);
                            cuenta.set("emisorCuenta", accountNumber);
                            cuenta.set("emisorMoneda", "032");
                            cuenta.set("emisorCuit", cuit);
                            cuenta.set("emisorRazonSocial", nombre);
                            cuenta.set("emisorCuentaFechaAlta", cuentaCliente.fechaAlta("yyyy-MM-dd'T'HH:mm:ss.SSS"));
                            localAccountList.add(cuenta);
                        }
                    }
                } else {
                    Cuenta cuentaCliente = contexto.cuenta(contexto.parametros.string("id_producto"));
                    String cuentaSucursal = obtenerSucursalPorVe(cuentaCliente.sucursal(), persona.sucursal(), contexto);
                    cuenta = new Objeto();
                    int codSucInt = Integer.valueOf(cuentaSucursal);
                    String codSuc = String.format("%04d", codSucInt);
                    cuenta.set("sucursalCodigo", codSuc);
                    /*
                     * try { for(Objeto sucursal : obtenerSucursalesv2(contexto)) {
                     * if(sucursal.string("CodSucursal", "").equals(persona.sucursal())) {
                     * cuenta.set("sucursalNombre",
                     * normalizarCadena(sucursal.get("DesSucursal").toString()));
                     * cuenta.set("sucursalDomicilio",
                     * normalizarCadena(sucursal.get("Domicilio").toString().split("-")[0].trim()));
                     * cuenta.set("sucursalCp", sucursal.get("CodigoPostalSucursal"));
                     * cuenta.set("sucursalProvincia",
                     * normalizarCadena(sucursal.get("NomProvincia").toString())); break; } } }
                     * catch (Exception e) { cuenta.set("sucursalNombre", "Casa central");
                     * cuenta.set("sucursalDomicilio", "Reconquista 101"); cuenta.set("sucursalCp",
                     * "1003"); cuenta.set("sucursalProvincia", "caba"); }
                     */
                    if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_mejora_echeq")) {
                        Optional<Objeto> sucursalObj = obtenerSucursalPersona(contexto, cuentaSucursal);
                        if (sucursalObj.isPresent()) {
                            cuenta.set("sucursalNombre", normalizarCadena(sucursalObj.get().get("DesSucursal").toString()));
                            cuenta.set("sucursalDomicilio", normalizarCadena(sucursalObj.get().get("Domicilio").toString()));
                            cuenta.set("sucursalCp", sucursalObj.get().get("CodigoPostalSucursal"));
                            cuenta.set("sucursalProvincia", normalizarCadena(sucursalObj.get().get("NomProvincia").toString()));
                        }
                    } else {
                        try {
                            for (Objeto sucursal : obtenerSucursales(contexto)) {
                                if (sucursal.string("CodSucursal", "").equals(cuentaSucursal)) {
                                    cuenta.set("sucursalNombre", normalizarCadena(sucursal.get("DesSucursal").toString()));
                                    cuenta.set("sucursalDomicilio", normalizarCadena(sucursal.get("Domicilio").toString()));
                                    cuenta.set("sucursalCp", sucursal.get("CodigoPostalSucursal"));
                                    cuenta.set("sucursalProvincia", normalizarCadena(sucursal.get("NomProvincia").toString()));
                                }
                            }
                        } catch (Exception e) {
                            cuenta.set("sucursalNombre", "Casa central");
                            cuenta.set("sucursalDomicilio", "Reconquista 101");
                            cuenta.set("sucursalCp", "1003");
                            cuenta.set("sucursalProvincia", "caba");
                        }
                    }

                    cuenta.set("emisorCbu", cuentaCliente.cbu());
                    String accountNumber = cuentaCliente.numero();
                    accountNumber = accountNumber.substring(accountNumber.length() - 11);
                    cuenta.set("emisorCuenta", accountNumber);
                    cuenta.set("emisorMoneda", "032");
                    cuenta.set("emisorCuit", cuit);
                    cuenta.set("emisorRazonSocial", normalizarCadena(nombre));
                    cuenta.set("emisorCuentaFechaAlta", cuentaCliente.fechaAlta("yyyy-MM-dd'T'HH:mm:ss.SSS"));
                    localAccountList.add(cuenta);
                }
                for (Objeto localAccount : localAccountList) {
                    ApiResponseMB response = RestEcheq.habilitarCuenta(contexto, localAccount, domicilio, cp);
                    if (response.hayError()) {
                        return obtenerRespuestaError(response, "");
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException();
            }
        } else {
            return RespuestaMB.estado("FUNCIONALIDAD_APAGADA");

        }
        return respuesta;
    }

    public static RespuestaMB disableAccount(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), flagEcheq)) {
            try {
                String idProducto = contexto.parametros.string("id_producto", "0");
                String cbu = "0";
                String cuit = "0";
                if (contexto.cuenta(idProducto) != null)
                    cbu = contexto.cuenta(idProducto).cbu();
                else {
                    return RespuestaMB.estado("NO_EXISTE_CUENTA");
                }
                if (contexto.persona().tieneCuit())
                    cuit = contexto.persona().cuit();
                else {
                    return RespuestaMB.estado("NO_TIENE_CUIT");
                }
                ApiResponseMB response = RestEcheq.deshabilitarCuenta(contexto, cbu, cuit);
                if (response.hayError()) {
                    return obtenerRespuestaError(response, "");
                }
            } catch (Exception e) {
                throw new RuntimeException();
            }
        } else {
            return RespuestaMB.estado("FUNCIONALIDAD_APAGADA");
        }
        return respuesta;
    }

    public static RespuestaMB getECheqs(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        ApiResponseMB response = null;
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), flagEcheq)) {
            try {
                Persona persona = contexto.persona();
                String cuitCliente = persona.cuit();
                List<Cuenta> cuentas = contexto.cuentas();
                List<String> listaDisponibles = new ArrayList<>();
                List<Objeto> totalCheques = new ArrayList<>();
                List<Map<String, Object>> listaChequesPropiosMap = new ArrayList<Map<String, Object>>();
                List<Map<String, Object>> listaChequesAjenosMap = new ArrayList<Map<String, Object>>();
                List<Map<String, Object>> listaCuentasMap = new ArrayList<Map<String, Object>>();
                List<Map<String, Object>> listaChequerasMap = new ArrayList<Map<String, Object>>();
                listaDisponibles.add("chequesEmitidos");
                listaDisponibles.add("chequesRecibidos");

                try {
                    boolean hayDatos = true;
                    int pagina = 1;
                    while (hayDatos) {
                        response = RestEcheq.obtenerEcheqsListas(contexto, pagina);
                        List<Objeto> resultados = response.objeto("result").objetos("cheques");
                        if (resultados.size() < 20) {
                            hayDatos = false;
                        } else {
                            pagina++;
                        }
                        totalCheques.addAll(resultados);
                    }
                } catch (ApiExceptionMB e) {
                    listaDisponibles.clear();
                }
                if (totalCheques.isEmpty()) {
                    listaDisponibles.clear();
                }
                if (!(listaDisponibles.isEmpty())) {
                    Map<String, Object> chequeMap = new HashMap<>();
                    List<Objeto> listaCheques = totalCheques;
                    for (Objeto cheque : listaCheques) {
                        if (contexto.parametros.string("status") != null || contexto.parametros.string("start_date", null) != null || !"CADUCADO".equalsIgnoreCase(cheque.string("estado")) && !"REPUDIADO".equalsIgnoreCase(cheque.string("estado")) && !"ANULADO".equalsIgnoreCase(cheque.string("estado")) && !"RECHAZADO".equalsIgnoreCase(cheque.string("estado"))) {
                            chequeMap = buildEcheqListaPostResponse(cheque, contexto);
                            if ("RECHAZADO".equalsIgnoreCase((String) chequeMap.get("estado"))) {
                                if ((cheque.bool("acuerdo_rechazado") && !(cheque.bool("solicitando_acuerdo"))) || (!(cheque.bool("solicitando_acuerdo")) && !(cheque.bool("cheque_acordado")))) {
                                    chequeMap.put("estado", "RECHAZADO - 1");
                                } else if (cheque.bool("solicitando_acuerdo")) {
                                    chequeMap.put("estado", "RECHAZADO - 2");
                                } else if (cheque.bool("cheque_acordado")) {
                                    chequeMap.put("estado", "RECHAZADO - 3");
                                }
                            }
                            if (cuitCliente.equals(cheque.objeto("cuenta_emisora").string("emisor_cuit"))) {
                                listaChequesPropiosMap.add(chequeMap);
                            } else if (cuitCliente.equals(cheque.objeto("emitido_a").string("beneficiario_documento"))
                                    || cuitCliente.equals(cheque.objeto("tenencia").string("beneficiario_documento"))) {

                                if ("ENDOSADO".equalsIgnoreCase((String) chequeMap.get("estado"))) {
                                    if (cuitCliente.equals(String.valueOf(chequeMap.get("emitido_a.getBeneficiario_documento")))) {
                                        chequeMap.put("estado", "ENDOSADO - 1");
                                    } else {
                                        chequeMap.put("estado", "ENDOSADO - 2");
                                    }
                                }
                                listaChequesAjenosMap.add(chequeMap);
                            }
                        }
                    }
                }
                listaDisponibles.add("cuentas");
                try {
                    response = RestEcheq.obtenerEcheqsCuentas(contexto);
                } catch (ApiExceptionMB e) {
                    listaDisponibles.remove("cuentas");
                }
                if (response != null && response.hayError()) {
                    listaDisponibles.remove("cuentas");
                }
                if (listaDisponibles.contains("cuentas")) {
                    List<Objeto> listaCuentas = new ArrayList<>();
                    if (response.objeto("result").objetos("cuentas") != null)
                        listaCuentas = response.objeto("result").objetos("cuentas");
                    Map<String, Object> cuentaMap = new HashMap<>();
                    for (Objeto cuenta : listaCuentas) {
                        if ("ACTIVA".equalsIgnoreCase(cuenta.string("cuenta_estado"))) {
                            cuentaMap = buildEcheqListaCuentasResponse(cuenta, contexto);
                            listaCuentasMap.add(cuentaMap);
                        }
                    }
                }
                for (Cuenta cuenta : cuentas) {
                    if (cuenta.esCuentaCorriente()) {
                        if (!(listaDisponibles.contains("chequeras")))
                            listaDisponibles.add("chequeras");
                        try {
                            response = RestEcheq.obtenerEcheqsChequeras(contexto, cuenta);
                        } catch (ApiExceptionMB e) {
                            listaDisponibles.remove("chequeras");
                        }
                        if (response != null && response.hayError()) {
                            listaDisponibles.remove("chequeras");
                        }
                        if (listaDisponibles.contains("chequeras")) {
                            List<Objeto> listaChequera = new ArrayList<>();
                            if (response.objetos() != null)
                                listaChequera = response.objetos();
                            Objeto chequeraMap = new Objeto();
                            for (Objeto chequera : listaChequera) {
                                chequeraMap = buildEcheqListaChequerasResponse(chequera);
                                chequeraMap.set("id_producto", cuenta.id());
                                listaChequerasMap.add(chequeraMap.toMap());
                            }
                        }
                    }
                }
                respuesta.set("datosDisponibles", listaDisponibles);
                respuesta.set("chequesEmitidos", listaChequesPropiosMap);
                respuesta.set("chequesRecibidos", listaChequesAjenosMap);
                respuesta.set("cuentas", listaCuentasMap);
                respuesta.set("chequeras", listaChequerasMap);
            } catch (Exception e) {
                throw new RuntimeException();
            }
        } else {
            return RespuestaMB.estado("FUNCIONALIDAD_APAGADA");
        }
        return respuesta;
    }


    public static RespuestaMB accept(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), flagEcheq)) {
            try {
                ApiResponseMB response = RestEcheq.aceptar(contexto);
                if (response.hayError()) {
                    return obtenerRespuestaError(response, "");
                }
            } catch (Exception e) {
                throw new RuntimeException();
            }
        } else {
            return RespuestaMB.estado("FUNCIONALIDAD_APAGADA");
        }
        return respuesta;
    }

    public static RespuestaMB reject(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), flagEcheq)) {
            try {
                ApiResponseMB response = RestEcheq.rechazar(contexto);
                if (response.hayError()) {
                    return obtenerRespuestaError(response, "");
                }

            } catch (Exception e) {
                throw new RuntimeException();
            }
        } else {
            return RespuestaMB.estado("FUNCIONALIDAD_APAGADA");
        }
        return respuesta;
    }

    public static RespuestaMB tyc(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), flagEcheq)) {
            try {
                respuesta.set("tycMessage", ConfigMB.string("echeq_tyc_message"));
            } catch (Exception e) {
                throw new RuntimeException();
            }
        } else {
            return RespuestaMB.estado("FUNCIONALIDAD_APAGADA");
        }
        return respuesta;
    }

    public static RespuestaMB deposit(ContextoMB contexto) {
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        RespuestaMB respuesta = new RespuestaMB();
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), flagEcheq)) {
            try {
                boolean esMigrado = contexto.esMigrado(contexto);

                if (esMigrado && Objeto.anyEmpty(csmId, checksum))
                    return RespuestaMB.parametrosIncorrectos();

                RespuestaMB respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "echeq", JourneyTransmitEnum.MB_INICIO_SESION);
                if (respuestaValidaTransaccion.hayError())
                    return respuestaValidaTransaccion;

                ApiResponseMB response = RestEcheq.depositarActivo(contexto);
                if (response.hayError()) {
                    return obtenerRespuestaError(response, contexto.csmIdAuth);
                } else {
                    respuesta.set("success", true);
                }
            } catch (Exception e) {
                throw new RuntimeException();
            }
        } else {
            return RespuestaMB.estado("FUNCIONALIDAD_APAGADA", contexto.csmIdAuth);
        }
        return respuesta.set("csmIdAuth", contexto.csmIdAuth);
    }

    public static RespuestaMB getCheckbookTypes(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), flagEcheq)) {
            try {
                ApiResponseMB response = RestEcheq.obtenerChequera(contexto);
                if (response.hayError()) {
                    return obtenerRespuestaError(response, "");
                }

                List<Objeto> listaChequerasMap = new ArrayList<>();
                if (response.objetos() != null && !response.objetos().isEmpty()) {
                    Objeto chequeraMapeada = new Objeto();
                    for (Objeto o : response.objetos()) {
                        chequeraMapeada = buildEcheqListaChequerasResponse(o);
                        listaChequerasMap.add(chequeraMapeada);
                    }
                }
                respuesta.set("chequera_tipos", listaChequerasMap);

            } catch (Exception e) {
                throw new RuntimeException();
            }
        } else {
            return RespuestaMB.estado("FUNCIONALIDAD_APAGADA");
        }
        return respuesta;
    }

    public static RespuestaMB postCheckbook(ContextoMB contexto) {
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        RespuestaMB respuesta = new RespuestaMB();
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), flagEcheq)) {
            try {
                boolean esMigrado = contexto.esMigrado(contexto);

                if (esMigrado && Objeto.anyEmpty(csmId, checksum))
                    return RespuestaMB.parametrosIncorrectos();

                RespuestaMB respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "echeq", JourneyTransmitEnum.MB_INICIO_SESION);
                if (respuestaValidaTransaccion.hayError())
                    return respuestaValidaTransaccion;

                ApiResponseMB response = RestEcheq.postChequera(contexto);
                if (response.hayError()) {
                    return obtenerRespuestaError(response, contexto.csmIdAuth);
                }
                respuesta.set("number", response.string("NRO_CHEQUERA"));

            } catch (Exception e) {
                throw new RuntimeException();
            }
        } else {
            return RespuestaMB.estado("FUNCIONALIDAD_APAGADA", contexto.csmIdAuth);
        }
        return respuesta.set("csmIdAuth", contexto.csmIdAuth);
    }

    public static RespuestaMB getPerson(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), flagEcheq)) {
            try {
                String documento = contexto.parametros.string("CUIT");
                String tipoDocumento = "cuit";
                ApiResponseMB response = RestEcheq.obtenerChequeBancarizado(contexto, documento, tipoDocumento);
                if (response.hayError()) {
                    return obtenerRespuestaError(response, "");
                }
                respuesta.set("nombre", response.objeto("result").string("beneficiario_razon_social"));
                respuesta.set("documento.tipo", response.objeto("result").string("beneficiario_documento_tipo"));

            } catch (Exception e) {
                throw new RuntimeException();
            }
        } else {
            return RespuestaMB.estado("FUNCIONALIDAD_APAGADA");
        }

        return respuesta;
    }

    public static RespuestaMB generate(ContextoMB contexto) {
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        RespuestaMB respuesta = new RespuestaMB();
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), flagEcheq)) {
            try {
                boolean esMigrado = contexto.esMigrado(contexto);

                if (esMigrado && Objeto.anyEmpty(csmId, checksum))
                    return RespuestaMB.parametrosIncorrectos();

                RespuestaMB respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "echeq", JourneyTransmitEnum.MB_INICIO_SESION);
                if (respuestaValidaTransaccion.hayError())
                    return respuestaValidaTransaccion;

                ApiResponseMB response = RestEcheq.generar(contexto);
                if (response.hayError()) {
                    return obtenerRespuestaError(response, contexto.csmIdAuth);
                }
            } catch (Exception e) {
                throw new RuntimeException();
            }
        } else {
            return RespuestaMB.estado("FUNCIONALIDAD_APAGADA", contexto.csmIdAuth);
        }
        return respuesta.set("csmIdAuth", contexto.csmIdAuth);
    }

    public static RespuestaMB cancel(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), flagEcheq)) {
            try {
                ApiResponseMB response = RestEcheq.cancelar(contexto);
                if (response.hayError()) {
                    return obtenerRespuestaError(response, "");
                }

            } catch (Exception e) {
                throw new RuntimeException();
            }
        } else {
            return RespuestaMB.estado("FUNCIONALIDAD_APAGADA");
        }
        return respuesta;
    }

    public static RespuestaMB returnRequest(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), flagEcheq)) {
            try {
                ApiResponseMB response = RestEcheq.devolverPeticion(contexto);
                if (response.hayError()) {
                    return obtenerRespuestaError(response, "");
                }
            } catch (Exception e) {
                throw new RuntimeException();
            }
        } else {
            return RespuestaMB.estado("FUNCIONALIDAD_APAGADA");
        }

        return respuesta;
    }

    public static RespuestaMB cancelReturn(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), flagEcheq)) {
            try {
                ApiResponseMB response = RestEcheq.cancelarRetorno(contexto);
                if (response.hayError()) {
                    return obtenerRespuestaError(response, "");
                }

            } catch (Exception e) {
                throw new RuntimeException();
            }
        } else {
            return RespuestaMB.estado("FUNCIONALIDAD_APAGADA");
        }
        return respuesta;
    }

    public static RespuestaMB rejectReturn(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), flagEcheq)) {
            try {
                ApiResponseMB response = RestEcheq.rechazarRetorno(contexto);
                if (response.hayError()) {
                    return obtenerRespuestaError(response, "");
                }

            } catch (Exception e) {
                throw new RuntimeException();
            }
        } else {
            return RespuestaMB.estado("FUNCIONALIDAD_APAGADA");
        }
        return respuesta;
    }

    public static RespuestaMB acceptReturn(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), flagEcheq)) {
            try {
                ApiResponseMB response = RestEcheq.aceptarRetorno(contexto);
                if (response.hayError()) {
                    return obtenerRespuestaError(response, "");
                }

            } catch (Exception e) {
                throw new RuntimeException();
            }
        } else {
            return RespuestaMB.estado("FUNCIONALIDAD_APAGADA");
        }
        return respuesta;
    }

    public static RespuestaMB endorse(ContextoMB contexto) {
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        RespuestaMB respuesta = new RespuestaMB();
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), flagEcheq)) {
            try {
                boolean esMigrado = contexto.esMigrado(contexto);

                if (esMigrado && Objeto.anyEmpty(csmId, checksum))
                    return RespuestaMB.parametrosIncorrectos();

                RespuestaMB respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "echeq", JourneyTransmitEnum.MB_INICIO_SESION);
                if (respuestaValidaTransaccion.hayError())
                    return respuestaValidaTransaccion;

                ApiResponseMB response = RestEcheq.endosar(contexto);
                if (response.hayError()) {
                    return obtenerRespuestaError(response, "");
                }

            } catch (Exception e) {
                throw new RuntimeException();
            }
        } else {
            return RespuestaMB.estado("FUNCIONALIDAD_APAGADA");
        }
        return respuesta.set("csmIdAuth", contexto.csmIdAuth);
    }

    public static RespuestaMB combosEcheqs(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), flagEcheq)) {
            try {
                respuesta.set("estados", simpleCombo(echeqStatus));
                respuesta.set("tipos_endoso", simpleCombo(echeqEndorsementTypes));
            } catch (Exception e) {
                throw new RuntimeException();
            }
        } else {
            return RespuestaMB.estado("FUNCIONALIDAD_APAGADA");
        }
        return respuesta;
    }

    private static Map<String, Object> buildEcheqListaCuentasResponse(Objeto cuenta, ContextoMB wkUtil) {
        Map<String, Object> result = new HashMap<>();
        result.put("cbu", cuenta.string("emisor_cbu"));
        result.put("number", cuenta.string("emisor_cuenta"));
        result.put("status", cuenta.string("cuenta_estado"));
        result.put("currency", "$");
        return result;
    }

    private static Objeto buildEcheqListaChequerasResponse(Objeto chequera) {
        Objeto result = new Objeto();
        result.set("number", chequera.string("NUMERO_CHEQUERA"));
        result.set("type", chequera.string("TIPO_CHEQUERA"));
        result.set("type.description", chequera.string("DESCRIPCION_CHEQUERA"));
        result.set("echeqs.count", chequera.string("CANT_CHEQUES_DISPONIBLE"));
        result.set("echeqs.maxCount", chequera.string("CANT_CHEQUES"));
        result.set("status", chequera.string("ESTADO"));
        result.set("status.description", RestEcheq.statusMapper(chequera.string("DES_ESTADO"), false));
        result.set("cost", chequera.string("COSTO"));
        result.set("rubro", chequera.string("ID_RUBRO"));
        if (chequera.string("CHEQUE_INICIAL") != null && chequera.string("CHEQUE_FINAL") != null) {
            result.set("serialNumber", chequera.string("CHEQUE_INICIAL") + " - " + chequera.string("CHEQUE_FINAL"));
        } else {
            result.set("serialNumber", "");
        }
        return result;
    }

    private static Map<String, Object> buildEcheqListaPostResponse(Objeto cheque, ContextoMB wkUtil) {
        Map<String, Object> result = new HashMap<>();
        result.put("emisor_razon_social", cheque.objeto("cuenta_emisora").string("emisor_razon_social"));
        result.put("emisor_cuit", cheque.objeto("cuenta_emisora").string("emisor_cuit"));
        result.put("numero_chequera", cheque.string("numero_chequera"));
        result.put("cheque_id", cheque.string("cheque_id"));
        result.put("cheque_numero", cheque.string("cheque_numero"));
        result.put("estado", RestEcheq.statusMapper(cheque.string("estado"), false));
        result.put("emisor_moneda", cheque.objeto("cuenta_emisora").string("emisor_moneda"));
        result.put("emisor_cuenta", cheque.objeto("cuenta_emisora").string("emisor_cuenta"));
        result.put("emitido_a.beneficiario_nombre", cheque.objeto("emitido_a").string("beneficiario_nombre"));
        result.put("emitido_a.beneficiario_documento_tipo", cheque.objeto("emitido_a").string("beneficiario_documento_tipo"));
        result.put("emitido_a.beneficiario_documento", cheque.objeto("emitido_a").string("beneficiario_documento"));
        result.put("tenencia.beneficiario_nombre", cheque.objeto("tenencia").string("beneficiario_nombre"));
        result.put("tenencia.beneficiario_documento_tipo", cheque.objeto("tenencia").string("beneficiario_documento_tipo"));
        result.put("tenencia.beneficiario_documento", cheque.objeto("tenencia").string("beneficiario_documento"));
        String fechaEmisionStr = cheque.string("fecha_emision", null);
        LocalDate fechaEmision = fechaEmisionStr == null ? null : LocalDate.parse(fechaEmisionStr.substring(0, 10), DateTimeFormatter.ISO_DATE);
        result.put("fecha_emision", fechaEmision.toString());
        result.put("cheque_concepto", cheque.string("cheque_concepto"));
        result.put("monto", cheque.string("monto"));
        String fechaPagoStr = cheque.string("fecha_pago", null);
        LocalDate fechaPago = fechaPagoStr == null ? null : LocalDate.parse(fechaPagoStr.substring(0, 10), DateTimeFormatter.ISO_DATE);
        result.put("fecha_pago", fechaPago.toString());
        result.put("cod_visualizacion", cheque.string("cod_visualizacion"));
        result.put("banco_codigo", cheque.objeto("cuenta_emisora").string("banco_codigo"));
        result.put("sucursal_cp", cheque.objeto("cuenta_emisora").string("sucursal_cp"));
        result.put("cheque_tipo", cheque.string("cheque_tipo"));
        result.put("cmc7", cheque.string("cmc7"));
        result.put("cheque_modo", cheque.string("cheque_modo"));
        result.put("cheque_motivo_pago", cheque.string("cheque_motivo_pago"));
        result.put("cheque_caracter", cheque.string("cheque_caracter"));
        List<Objeto> listaEndosos = new ArrayList<>();
        List<Map<String, Object>> listaEndososMap = new ArrayList<Map<String, Object>>();
        if (cheque.string("endosos", null) != null)
            listaEndosos = cheque.objetos("endosos");
        for (Objeto endoso : listaEndosos) {
            Map<String, Object> endosoMap = new HashMap<>();
            endosoMap.put("tipo_endoso", "NOM".equalsIgnoreCase(endoso.string("tipo_endoso")) ? "Nominal" : "Para negociaciï¿½n");
            String fechaStr = endoso.string("fecha_hora", null);
            LocalDate fecha = fechaStr == null || fechaStr.isEmpty() ? null : LocalDate.parse(fechaStr.substring(0, 10), DateTimeFormatter.ISO_DATE);
            endosoMap.put("fecha_hora", fecha.toString());
            endosoMap.put("documento_tipo", endoso.string("documentoTipo"));
            endosoMap.put("documento", endoso.string("documento"));
            endosoMap.put("estado_endoso", endoso.string("estadoEndoso"));
            endosoMap.put("motivo_repudio", endoso.string("motivoRepudio"));
            listaEndososMap.add(endosoMap);
        }
        result.put("endosos", listaEndososMap);
        return result;
    }

    private static RespuestaMB obtenerRespuestaError(ApiResponseMB response, String csmIdAuth) {
        RespuestaMB respuesta = new RespuestaMB();
        if (response.codigo != 306) {
            respuesta.set("estado", response.string("codigo"));
            respuesta.set("mensaje", response.string("mensajeAlUsuario"));
            respuesta.set("csmIdAuth", csmIdAuth);
        } else {
            return RespuestaMB.error(csmIdAuth);
        }
        return respuesta;
    }

    private static List<Map<String, Object>> simpleCombo(List<String> list) throws Exception {
        List<Map<String, Object>> values = new ArrayList<Map<String, Object>>();
        for (String comboElement : list) {
            Map<String, Object> element = new HashMap<>();
            String[] entryElements = comboElement.split(":");
            String key = entryElements[0];
            String value = entryElements[1];
            element.put("description", key);
            element.put("value", value);
            values.add(element);
        }
        return values;
    }

    private static List<Objeto> obtenerSucursales(ContextoMB contexto) {
        List<Objeto> ret = new ArrayList<>();
        for (Objeto sucursal : RestCatalogo.sucursales(contexto).objetos()) {
            if (sucursal.string("Domicilio").contains("CP")) {
                String domicilio = sucursal.string("Domicilio");
                int startIndex = domicilio.indexOf("CP");
                try {
                    String codigoPostalStr = domicilio.substring(startIndex).replace(" ", "").substring(2, 6);
                    sucursal.set("CodigoPostalSucursal", Integer.parseInt(codigoPostalStr));
                } catch (Exception e) {
                    sucursal.set("CodigoPostalSucursal", 0);
                }
            } else {
                sucursal.set("CodigoPostalSucursal", 0);
            }
            ret.add(sucursal);
        }
        return ret;
    }

    // Se crea un método aparte en la cual se captura la mejora que trae netamente
    // el codigopostal en un campo aparte y ya no desde domicilio
    protected static List<Objeto> obtenerSucursalesv2(ContextoMB contexto) {
        List<Objeto> ret = new ArrayList<>();
        for (Objeto sucursal : RestCatalogo.sucursales(contexto).objetos()) {
            if (!sucursal.string("CodigoPostal").isEmpty()) {
                String codigoPostal = sucursal.string("CodigoPostal");
                sucursal.set("CodigoPostalSucursal", Integer.parseInt(codigoPostal));
            } else {
                sucursal.set("CodigoPostalSucursal", 0);
            }
            ret.add(sucursal);
        }
        return ret;
    }

    private static Optional<Objeto> obtenerSucursalPersona(ContextoMB contexto, String sucursalPersona) {
        List<Objeto> sucursales = RestCatalogo.sucursales(contexto).objetos();

        sucursales.forEach(sucursal -> {
            if (!sucursal.string("CodigoPostal").isEmpty()) {
                String codigoPostal = sucursal.string("CodigoPostal");
                sucursal.set("CodigoPostalSucursal", Integer.parseInt(codigoPostal));
            } else {
                sucursal.set("CodigoPostalSucursal", 0);
            }
        });
        return Optional.of(sucursales.stream().filter((cadena) -> cadena.string("CodSucursal", "").equals(sucursalPersona)).findFirst().get());
    }

    private static String obtenerSucursalPorVe(String sucursalCuenta, String sucursalPersona, ContextoMB
            contexto) {
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "mb_prendido_cuenta_sucursal")) {
            return sucursalCuenta;
        } else {
            return sucursalPersona;
        }
    }

    private static List<Objeto> obtenerDomicilios(ContextoMB contexto, String cuit) {
        List<Objeto> ret = new ArrayList<>();
        for (Objeto domicilio : RestPersona.domicilios(contexto, cuit).objetos()) {
            ret.add(domicilio);
        }
        return ret;
    }

    private static String normalizarCadena(String in) {
        return in.replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u").replace("Á", "A").replace("É", "E").replace("Í", "I").replace("Ó", "O").replace("Ú", "U").replace("ñ", "n").replace("Ñ", "N");
    }
}
