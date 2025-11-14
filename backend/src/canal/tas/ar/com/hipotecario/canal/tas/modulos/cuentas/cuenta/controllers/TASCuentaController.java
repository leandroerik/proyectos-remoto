package ar.com.hipotecario.canal.tas.modulos.cuentas.cuenta.controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.modulos.cuentas.cuenta.modelos.TASCajasAhorro;
import ar.com.hipotecario.canal.tas.modulos.cuentas.cuenta.modelos.TASCuentasCorriente;
import ar.com.hipotecario.canal.tas.modulos.cuentas.cuenta.modelos.TASDatosBasicosCuenta;
import ar.com.hipotecario.canal.tas.modulos.cuentas.cuenta.modelos.TASSeguimientoPaquetes;
import ar.com.hipotecario.canal.tas.modulos.cuentas.cuenta.services.TASRestCuenta;
import ar.com.hipotecario.canal.tas.modulos.cuentas.cuenta.services.TASSqlCuenta;
import ar.com.hipotecario.canal.tas.modulos.cuentas.cuenta.utils.UtilesCuenta;
import ar.com.hipotecario.canal.tas.modulos.cuentas.depositos.utils.UtilesDepositos;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.formularios.controllers.TASFormulariosController;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.controllers.TASApiPersona;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASClientePersona;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASTelefono;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.servicios.TASRestPersona;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.productos.utiles.UtilesProductos;
import ar.com.hipotecario.canal.tas.shared.utils.models.strings.TASMensajesString;

public class TASCuentaController {
    public static Objeto getCuentas(ContextoTAS contexto) {
        try {
            String idCliente = contexto.parametros.string("idCliente");
           TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            boolean cancelados = contexto.parametros.bool("cancelados", false);
            boolean firmaConjunta = contexto.parametros.bool("firmaConjunta", false);
            boolean firmantes = contexto.parametros.bool("firmantes", false);
            Objeto cuentas = TASRestCuenta.getCuentas(contexto, idCliente, cancelados, firmaConjunta, firmantes);
            return cuentas.isEmpty() ? RespuestaTAS.sinResultados(contexto, "Cuentas no encontradas para el cliente")
                    : cuentas;
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "TASCuentasController - getCuentas()", e);
        }
    }

    public static Objeto getDetalleCA(ContextoTAS contexto) {
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String idCuenta = contexto.parametros.string("cuentaId");
            if (idCuenta.isEmpty())
                return RespuestaTAS.sinParametros(contexto, "Cuenta sin especificar");
            boolean historico = contexto.parametros.bool("historico", false);
            boolean validaCuentaEmpleado = contexto.parametros.bool("cuentaEmpleado", false);
            boolean consultaAlias = contexto.parametros.bool("consultaAlias", true);
            Fecha fechaDesde = contexto.parametros.fecha("fecha", "yyyy-MM-dd", new Fecha(new Date()));

            Objeto detalleCA = getCajaAhorroPorId(contexto, idCuenta, fechaDesde, historico, validaCuentaEmpleado);
            if (detalleCA == null)
                return RespuestaTAS.sinResultados(contexto, "Sin datos para la cuentaID");
            if (detalleCA.string("estado").contains("ERROR"))
                return detalleCA;
            String cbu = detalleCA.objetos().size() > 1 ? TASMensajesString.multipleResultados()
                    : detalleCA.string("cbu");
            Objeto response = new Objeto();
            response.set("detalleCA", detalleCA);
            if (cbu.equals(TASMensajesString.multipleResultados())) {
                response.set("Alias", "No se pudo obtener por multiples CBU encontrados");
            } else {
                Objeto alias = getAlias(contexto, cbu, consultaAlias);
                response.set("nuevoAlias", alias.string("estado").contains("ERROR") ? "ERROR"
                        : alias.string("nuevoAlias"));
            }
            return response;
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "TASCuentasController - getDetalleCA()", e);
        }
    }

    private static Objeto getCajaAhorroPorId(ContextoTAS contexto, String idCuenta, Fecha fechaDesde, boolean historico,
            boolean validaCuentaEmpleado) {
        try {
            List<TASCajasAhorro> detalleCA = TASRestCuenta.getCajaAhorroById(contexto, idCuenta, fechaDesde, historico,
                    validaCuentaEmpleado);
            return detalleCA.isEmpty() ? null : TASCajasAhorro.toObjeto(detalleCA);
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "Error en TASCuentaController - getCajaAhorroPorId()", e);
        }
    }

    private static Objeto getAlias(ContextoTAS contexto, String cbu, boolean consultaAlias) {
        try {
            Objeto response = TASRestCuenta.getAlias(contexto, cbu, consultaAlias);
            return response.isEmpty() ? null : response;
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "Error en TASCuentaController - getAlias()", e);
        }

    }

    public static Objeto getDetalleCC(ContextoTAS contexto) {
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String idCuenta = contexto.parametros.string("cuentaId");
            if (idCuenta.isEmpty())
                return RespuestaTAS.sinParametros(contexto, "Cuenta sin especificar");
            boolean historico = contexto.parametros.bool("historico", false);
            boolean validaCuentaEmpleado = contexto.parametros.bool("cuentaEmpleado", false);
            boolean consultaAlias = contexto.parametros.bool("consultaAlias", true);
            Fecha fechaDesde = contexto.parametros.fecha("fecha", "yyyy-MM-dd", new Fecha(new Date()));

            Objeto detalleCC = getCuentasCorrientesPorId(contexto, idCuenta, historico, fechaDesde,
                    validaCuentaEmpleado);
            if (detalleCC == null)
                return RespuestaTAS.sinResultados(contexto, "Sin datos para la cuentaID");
            if (detalleCC.string("estado").contains("ERROR"))
                return detalleCC;
            String cbu = detalleCC.objetos().size() > 1 ? TASMensajesString.multipleResultados()
                    : detalleCC.string("cbu");
            String cuenta = detalleCC.objetos().size() > 1 ? TASMensajesString.multipleResultados()
                    : detalleCC.string("cuenta");
            Objeto response = new Objeto();
            response.set("detalleCC", detalleCC);
            if (cbu.equals(TASMensajesString.multipleResultados())) {
                response.set("ERROR", "No se pudo obtener el resto de los datos, multiples CBU encontrados");
                // todo: agregar estados para las consultas fallidas de echeq
            } else {
                Futuro<Objeto> futuroAlias = new Futuro<>(() -> getAlias(contexto, cbu, consultaAlias));

                Futuro<Objeto> futuroChequesPendientes = new Futuro<>(
                        () -> getConsultaCheques(contexto, cuenta, "Pendientes"));
                Futuro<Objeto> futuroChequesRechazados = new Futuro<>(
                        () -> getConsultaCheques(contexto, idCuenta, "Rechazados"));
                response.set("nuevoAlias",
                        futuroAlias.get().string("estado").contains("ERROR")
                                ? "ERROR"
                                : futuroAlias.get().string("nuevoAlias"));
                response.set("chequesPendientes",
                		futuroChequesPendientes.get().string("estado").contains("ERROR")
                                ? "ERROR"
                                : futuroChequesPendientes.get().objeto("respuesta"));
                response.set("chequesRechazados",
                		futuroChequesRechazados.get().string("estado").contains("ERROR")
                                ? "ERROR"
                                : futuroChequesRechazados.get().objeto("respuesta"));


            LogTAS.evento(contexto, "CONSULTA_CHEQUES_PENDIENTES", futuroChequesPendientes.get());   
            LogTAS.evento(contexto, "CONSULTA_CHEQUES_RECHAZADOS", futuroChequesRechazados.get());                  
            }
            LogTAS.evento(contexto, "CONSULTA_DETALLE_CC", response);
            return response;
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "TASCuentasController - getDetalleCC()", e);
        }
    }

    public static Objeto getCuentasCorrientesPorId(ContextoTAS contexto, String idCuenta, boolean historico,
            Fecha fechaDesde, boolean validaCuentaEmpleado) {
        try {
            List<TASCuentasCorriente> detalleCC = TASRestCuenta.getCuentaCorrienteById(contexto, idCuenta, fechaDesde,
                    historico, validaCuentaEmpleado);
            return detalleCC.isEmpty() ? null : TASCuentasCorriente.toObjeto(detalleCC);
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "Error en TASCuentaController - getCuentasCorrientesPorId()", e);
        }
    }

    // todo: identificar casos positivos para ver q responde la api y poder armar el
    // response...
    public static Objeto getConsultaCheques(ContextoTAS contexto, String datosCuenta, String operacion) {
        try {
            Objeto consultaCheques = TASRestCuenta.getConsultaCheques(contexto, datosCuenta, operacion);
            return consultaCheques.isEmpty()
                    ? consultaCheques.set("detalle", "NO EXISTEN DATOS PARA LA CONSULTA SOLICITADA")
                    : consultaCheques;
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "Error en TASCuentaController - getConsultaCheques()", e);
        }
    }

    public static Objeto getUltimosMovimientosCa(ContextoTAS contexto) {
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String idCuenta = contexto.parametros.string("cuentaId");
            if (idCuenta.isEmpty())
                return RespuestaTAS.sinParametros(contexto, "Cuenta sin especificar");
            Objeto parametrosUrl = UtilesCuenta.armarParametrosUrl("CA");
            Objeto movimientos = TASRestCuenta.getUltimosMovimientosCa(contexto, idCuenta, parametrosUrl);
            return movimientos.isEmpty() ? movimientos.set("detalle", "NO EXISTEN DATOS PARA LA FECHA SOLICITADA")
                    : UtilesCuenta.armarRespuestaMovimientos(movimientos);
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "Error en TASCuentaController - getUltimosMovimientosCa()", e);
        }
    }

    public static Objeto getUltimosMovimientosCc(ContextoTAS contexto) {
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String idCuenta = contexto.parametros.string("cuentaId");
            if (idCuenta.isEmpty())
                return RespuestaTAS.sinParametros(contexto, "Cuenta sin especificar");
            Objeto parametrosUrl = UtilesCuenta.armarParametrosUrl("CC");
            Objeto movimientos = TASRestCuenta.getUltimosMovimientosCc(contexto, idCuenta, parametrosUrl);
            return movimientos.isEmpty() ? movimientos.set("detalle", "NO EXISTEN DATOS PARA LA FECHA SOLICITADA")
                    : UtilesCuenta.armarRespuestaMovimientos(movimientos);
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "Error en TASCuentaController - getUltimosMovimientosCc()", e);
        }
    }

    public static Objeto getTitulares(ContextoTAS contexto){
        try{
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String idCuenta = contexto.parametros.string("cuentaId");
            if (idCuenta.isEmpty())
                return RespuestaTAS.sinParametros(contexto, "Cuenta sin especificar");
            String tipoCta = contexto.parametros.string("tipo", "AHO");
            Objeto titulares = UtilesProductos.getTitularesProducto(contexto, idCuenta, tipoCta);
            if (titulares.string("estado").contains("ERROR")) return RespuestaTAS.error(contexto, "TASCuentaController - guardarSolicitudBaja()", (Exception) titulares.get("error"));
            Objeto response = new Objeto();
            return response.set("titulares", titulares.objeto("estado"));
        }catch(Exception e){
            return RespuestaTAS.error(contexto, "Error en TASCuentaController - getTitulares()", e);
        }
        
    }

    public static Objeto getBloqueosCa(ContextoTAS contexto) {
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String idCuenta = contexto.parametros.string("cuentaId");
            String idCuentaCobis = contexto.parametros.string("cuentaIdCobis");
            String tipoCuenta = contexto.parametros.string("tipoCuenta", "AHO");
            if (idCuenta.isEmpty() || idCuentaCobis.isEmpty())
                return RespuestaTAS.sinParametros(contexto, "Cuenta sin especificar");
            Objeto validaciones = UtilesCuenta.validarDatosParaBaja(contexto, idCliente, tipoCuenta, idCuenta, idCuentaCobis);
            return validaciones;
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "Error en TASCuentaController - getBloqueosCa()", e);
        }
    }

    public static Objeto validarCuentaDeposito(ContextoTAS contexto) {
        try {
            String idCuenta = contexto.parametros.string("cuentaId");
            if (idCuenta.isEmpty())
                return RespuestaTAS.sinParametros(contexto, "Cuenta sin especificar");
            Objeto validacionCuentaDeposito = TASRestCuenta.validarCuentaDeposito(contexto, idCuenta);
            if(validacionCuentaDeposito == null) return RespuestaTAS.sinResultados(contexto, "cuenta no encontrada");
            return armarRespuestaValidacionCtaDeposito(validacionCuentaDeposito);
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "TASCuentaController - validarCuentaDeposito()", e);
        }
    }

    private static Objeto armarRespuestaValidacionCtaDeposito(Objeto validacionCtaDeposito) {
        Objeto response = new Objeto();
        response.set("bloqueoDepositos", validacionCtaDeposito.string("bloqueoDepositos"));
        response.set("codigoBanca", validacionCtaDeposito.string("codigoBanca"));
        response.set("descripcionBanca", validacionCtaDeposito.string("descripcionBanca"));
        response.set("idProducto", validacionCtaDeposito.string("idProducto"));
        response.set("nroCuentaCobis", validacionCtaDeposito.string("nroCuentaCobis"));
        response.set("titular", validacionCtaDeposito.string("titular"));
        response.set("tipo", validacionCtaDeposito.string("tipoProducto"));
        response.set("codigoCliente", validacionCtaDeposito.string("codigoCliente"));
        response.set("numero", validacionCtaDeposito.string("cuenta"));


        return response;
    }

    public static Objeto bajaCa(ContextoTAS contexto) {
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String idCuenta = contexto.parametros.string("cuentaId");
            if (idCuenta.isEmpty())
                return RespuestaTAS.sinParametros(contexto, "Cuenta sin especificar");
            String servicio = contexto.parametros.string("servicio", "rest");

            Objeto cuentas = TASRestCuenta.getCuentas(contexto, idCliente, false, true, true);
            if(servicio.equals("sql")){
                Objeto datosParaBaja = armarDatosParaBajaCA(contexto, idCliente, clienteSesion, idCuenta, cuentas);
                Objeto bajaCASQL = TASSqlCuenta.guardarSolicitudBaja(contexto, datosParaBaja);
                if(!bajaCASQL.string("estado").isEmpty()) return RespuestaTAS.error(contexto, "TASCuentaController - bajaCa()", (Exception) bajaCASQL.get("error"));
                return bajaCASQL;
            }
            boolean historico = contexto.parametros.bool("historico", false);
            boolean validaCuentaEmpleado = contexto.parametros.bool("cuentaEmpleado", false);
            Fecha fechaDesde = contexto.parametros.fecha("fecha", "yyyy-MM-dd", new Fecha(new Date()));
            List<TASCajasAhorro> detalleCA = TASRestCuenta.getCajaAhorroById(contexto, idCuenta, fechaDesde, historico, validaCuentaEmpleado);
            Objeto datosParaBajaAutomatica = armarDatosParaBajaAutomaticaCA(clienteSesion, idCuenta, cuentas, detalleCA);
            if(datosParaBajaAutomatica == null) return RespuestaTAS.sinResultados(contexto, "cuenta no encontrada");
            if(!datosParaBajaAutomatica.string("estado").isEmpty()) throw new Exception((Exception)datosParaBajaAutomatica.get(("error")));
            
            LogTAS.evento(contexto, "INICIO_SOLICITUD_BAJA_CA", datosParaBajaAutomatica );
            Objeto bajaCA = TASRestCuenta.solicitarBajaCa(contexto, datosParaBajaAutomatica);

            if (!bajaCA.string("estado").isEmpty()) return armarResponseError(contexto, bajaCA);
            LogTAS.evento(contexto, "FIN_SOLICITUD_BAJA_CA", new Objeto().set("baja_ca", "true"));
            return new Objeto().set("baja_ca", true);
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "TASCuentaController - bajaCa()", e);
        }
    }

    private static Objeto armarDatosParaBajaCA(ContextoTAS contexto, String idCliente, TASClientePersona cliente,
            String idCuenta, Objeto cuentas) {
        Objeto datosParaBaja = new Objeto();
        Objeto cuentaSeleccionada = cuentas.objetos().size() > 1
                ? cuentas.objetos().stream().filter(cuenta -> cuenta.string("numeroProducto").equals(idCuenta))
                        .findFirst().orElse(null)
                : cuentas.objetos().get(0);

        String fechaSolicitud = new Fecha(new Date()).string("yyyy-MM-dd HH:mm:ss.sss");

        datosParaBaja.set("idCliente", idCliente);
        datosParaBaja.set("tipoOperacion", "BAJA");
        datosParaBaja.set("fechaSolicitud", fechaSolicitud);
        datosParaBaja.set("tipoProducto", cuentaSeleccionada.string("tipoProducto"));
        datosParaBaja.set("numeroProducto", cuentaSeleccionada.string("numeroProducto"));
        datosParaBaja.set("sucursal", contexto.subCanal());
        datosParaBaja.set("apellido", cliente.getApellido());
        datosParaBaja.set("nombre", cliente.getNombre());
        datosParaBaja.set("sexo", cliente.getSexo());
        datosParaBaja.set("tipoDocumento", cliente.getTipoDocumento());
        datosParaBaja.set("numeroDocumento", cliente.getNumeroDocumento());
        datosParaBaja.set("canal", contexto.canal());
        return datosParaBaja;
    }

    private static Objeto armarDatosParaBajaAutomaticaCA(TASClientePersona cliente, String idCuenta, Objeto cuentas, List<TASCajasAhorro> cajasAhorros){
        try {
            Objeto datosParaBaja = new Objeto();
            Objeto cuentaSeleccionada = obtenerCuentaSeleccionada(idCuenta,cuentas);
            if(cuentaSeleccionada == null) return null;
            TASCajasAhorro caja = cajasAhorros.size() > 1 ? obtenerCajaAhorro(cuentaSeleccionada, cajasAhorros) : cajasAhorros.get(0);
            if(caja == null) return null;
            String simboloMoneda = UtilesCuenta.formatearSimboloMoneda(caja.getMoneda());
            datosParaBaja.set("codigo_tipificacion", "BAJACA_PEDIDO");
            datosParaBaja.set("idCliente", cliente.getIdCliente());
            datosParaBaja.set("CodigoTributarioCliente", cliente.getNumeroIdentificacionTributaria());
            datosParaBaja.set("saldo", simboloMoneda + " " + caja.getDisponible());
            datosParaBaja.set("titulo", idCuenta +" - "+ "AHO");// 1
            datosParaBaja.set("fechaAlta", cuentaSeleccionada.string("fechaAlta"));// 2
            datosParaBaja.set("cuenta", idCuenta);
            datosParaBaja.set("usoFirma", caja.getUsoFirma());
            datosParaBaja.set("tipoTitularidad", caja.getTipoTitularidad());
            datosParaBaja.set("categoria", caja.getCategoria());
            return datosParaBaja;
        }catch (Exception e){
            Objeto error  = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }
    private static Objeto armarResponseError(ContextoTAS contexto, Objeto response){
        Exception e = (Exception) response.get("error");
        if(e instanceof ApiException){
            ApiException apiException = (ApiException) e;
            Objeto body = apiException.response.objeto("Errores");
            if(body.objetos().size() > 1){
                return RespuestaTAS.error(contexto, "Multiples API Exceptions", apiException);
            }
            apiException.response.set("codigo", body.objetos().get(0).string("Codigo"));
            apiException.response.set("mensajeAlUsuario",body.objetos().get(0).string("MensajeDesarrollador"));
            apiException.response.set("masInformacion", body.objetos().get(0).string("MasInfo"));
            apiException.response.set("ubicacion", "TASRestCuenta - solicitarBajaCa()");
            return RespuestaTAS.error(contexto, "TASCuentaController - bajaCa()", apiException);
        }
        return RespuestaTAS.error(contexto, "TASCuentaController - bajaCa()", e);
    }
    private static Objeto obtenerCuentaSeleccionada(String idCuenta,Objeto cuentas) {
        if (cuentas != null) {
            Objeto cuentaSeleccionada = cuentas.objetos().size() > 1
                    ? cuentas.objetos().stream().filter(cuenta -> cuenta.string("numeroProducto").equals(idCuenta))
                    .findFirst().orElse(null)
                    : cuentas.objetos().get(0);
            return cuentaSeleccionada;
        }
        return null;
    }
    private static TASCajasAhorro obtenerCajaAhorro(Objeto cuenta, List<TASCajasAhorro> cajasAhorros ){
        if(cuenta != null) {
            String cbu = cuenta.string("cbu");
            TASCajasAhorro caja = cajasAhorros.stream().filter(cajaAhorro -> cajaAhorro.getCbu().equals(cbu)).findFirst().orElse(null);
            return caja;
        }
        return null;
    }

    public static Objeto getSaldosHistoricos(ContextoTAS contexto){
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            Objeto cuentasPeso = contexto.parametros.objeto("cuentasPeso", new Objeto());
            Objeto cuentasDolar = contexto.parametros.objeto("cuentasDolar", new Objeto());
            if (cuentasPeso.toList().size() < 1 && cuentasDolar.toList().size() < 1 ) return RespuestaTAS.sinParametros(contexto, "sin tarjetas para consultar");
            boolean soloDolares = contexto.parametros.bool("soloDolares", false);  
            List<TASDatosBasicosCuenta> cuentasABuscar = soloDolares ?  UtilesCuenta.buscarCuentas(contexto, idCliente, cuentasDolar) : UtilesCuenta.buscarCuentas(contexto, idCliente, cuentasPeso);
            if(!soloDolares){
                Objeto responseHistoricoSaldos = UtilesCuenta.getSaldosHistoricos(contexto, cuentasABuscar);
                return responseHistoricoSaldos;
            }else{
                return new Objeto().set("categoriaId", "MOV");
            }
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "TASCuentaController - getSaldosHistoricos()", e);
        }
    }

    public static Objeto getSeguimientoPaquetes(ContextoTAS contexto){
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            Objeto responseSeguimiento = TASRestCuenta.getSeguimientoPaquetes(contexto, idCliente);
            boolean tienePaquete = false;
            if(responseSeguimiento.objetos().size() > 0){
                List<TASSeguimientoPaquetes> paquetes = new ArrayList<>();
                for(Objeto response : responseSeguimiento.objetos()){
                    TASSeguimientoPaquetes paquete = new TASSeguimientoPaquetes();
                    paquete.setEstado(response.string("Estado"));
                    String fecha = response.string("FECHA_INICIO");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = sdf.parse(fecha);
                    paquete.setFechaInicio(date);
                    paquetes.add(paquete);
                }
                Optional<TASSeguimientoPaquetes> maxPaquete = paquetes.stream()
                .max(Comparator.comparing(TASSeguimientoPaquetes::getFechaInicio));
                if (maxPaquete.isPresent() && 
                (maxPaquete.get().getEstado().equals(TASSeguimientoPaquetes.EN_CURSO) || 
                 maxPaquete.get().getEstado().equals(TASSeguimientoPaquetes.INICIADA))) tienePaquete = true;
            }
            return armarResponseSeguimiento(contexto, tienePaquete);            
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "TASCuentaController - getSeguimientoPaquetes()", e);        
        }
    }

    private static Objeto armarResponseSeguimiento(ContextoTAS contexto, boolean tienePaquete){
        Objeto response = new Objeto();
        String leyenda_so = contexto.config.string("tas_leyenda_so");
        String leyenda_pep = contexto.config.string("tas_leyenda_pep");
        String leyenda_ocde = contexto.config.string("tas_leyenda_ocde");
        String tdv = contexto.config.string("tas_flag_tdvirtual");
        response.set("leyPep", leyenda_pep);
        response.set("leySo", leyenda_so);
        response.set("leyOcde", leyenda_ocde);
        response.set("flagTDVirtual", tdv.equals("1"));
        response.set("tienePaquete", tienePaquete);
        return response;
    }

    public static Objeto postEnvioSolicitudCA(ContextoTAS contexto){
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            Objeto solicitudParams = armarParametrosSolicitud(contexto);
            LogTAS.evento(contexto, "INICIO_SOLICITUD_CA", solicitudParams);
            Objeto solicitudResponse = TASRestCuenta.postEnvioSolicitudCA(contexto, solicitudParams);            
            Objeto datos = solicitudResponse.objeto("Datos");
            Objeto response = new Objeto();
            String nroSolicitud = "";
            if(datos.objetos().size() > 0){                
                response.set("nroSolicitud", datos.objetos().get(0).string("IdSolicitud"));
                nroSolicitud = datos.objetos().get(0).string("IdSolicitud");
            }else{
                return armarResponseErrorSolicitud(contexto, solicitudResponse);
            }
            Objeto integrantesParams = armarParametrosIntegrantes(contexto, clienteSesion.getNumeroIdentificacionTributaria(), nroSolicitud);
            LogTAS.evento(contexto,"ENVIO_INTEGRANTES", integrantesParams);
            Objeto integrantesResponse = TASRestCuenta.postEnviarIntegrantes(contexto, integrantesParams);
            Objeto datosIntegrantes = integrantesResponse.objeto("Datos");
            if (datosIntegrantes.objetos().size() != 0) {
                LogTAS.evento(contexto, "FIN_SOLICITUD_CA", response);
                return response;
            } else {
                return armarResponseErrorSolicitud(contexto, integrantesResponse);                
            }            
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "TASCuentaController - postEnvioSolicitudCA()", e);
        }
    }

   
    private static Objeto armarParametrosSolicitud(ContextoTAS contexto){
            Objeto params = new Objeto();
            params.set("tipoOperacion", contexto.config.string("tas_api_tipoOperacion"));
            params.set("oficina", contexto.config.string("tas_api_oficina"));
            params.set("canalOriginacion1", Integer.valueOf(contexto.config.string("tas_api_canaloriginacion1")));
            params.set("canalOriginacion2", Integer.valueOf(contexto.config.string("tas_api_canaloriginacion2")));
            params.set("canalOriginacion3", contexto.config.string("tas_api_canaloriginacion3"));
            params.set("canalVenta1", contexto.config.string("tas_api_canalventa1"));
            params.set("canalVenta2", contexto.config.string("tas_api_canalventa2"));
            params.set("canalVenta3", contexto.config.string("tas_api_canalventa3"));
            params.set("canalVenta4", Integer.valueOf(contexto.config.string("tas_api_canalventa4")));
            return params;
    }

    private static Objeto armarParametrosIntegrantes(ContextoTAS contexto, String nroTributario, String nroSolicitud){
        Objeto params = new Objeto();
        params.set("numeroTributario", nroTributario);
        params.set("secuencia", contexto.config.string("tas_api_secuencia"));        
        params.set("tipoOperacion", contexto.config.string("tas_api_tipoOperacion"));
        params.set("nroSolicitud", nroSolicitud);
        return params;
    }

    private static Objeto armarResponseErrorSolicitud(ContextoTAS contexto, Objeto response){
        try{
            Objeto e = response.objeto("Errores");
            Exception exception = new Exception(e.objetos().get(0).string("MensajeCliente"));
            LogTAS.error(contexto, exception);
        if(e.objetos().size() >= 1){            
            String mensaje = e.objetos().get(0).string("MensajeDesarrollador").contains("FUERA_HORARIO") 
            || e.objetos().get(0).string("MensajeCliente").contains("FUERA_HORARIO") ? "FUERA_HORARIO" : "ERROR";
            if(mensaje.equals("FUERA_HORARIO")) return RespuestaTAS.error(contexto, mensaje, "El horario permitido para realizar la operación es de 4 a 22 hs.", "TASCuentaController - postEnvioSolicitudCA()" );
            return RespuestaTAS.error(contexto,"ERROR_FUNCIONAL", "Para continuar con la solicitud de apertura de tu Caja de Ahorro acercate a un Oficial de Negocios.<br><br>Muchas gracias." , "TASCuentaController - postEnvioSolicitudCA()");
        }else{
            return RespuestaTAS.error(contexto,"ERROR_FUNCIONAL", "Para continuar con la solicitud de apertura de tu Caja de Ahorro acercate a un Oficial de Negocios.<br><br>Muchas gracias." , "TASCuentaController - postEnvioSolicitudCA()");
            }
        }catch(Exception ex){
            return RespuestaTAS.error(contexto, "TASCuentaController - armarResponseErrorSolicitud()", ex);
            }
        }

    public static Objeto postGenerarCA(ContextoTAS contexto){
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            boolean termYCond = contexto.parametros.bool("termYCond", false);
            String nroSolicitud = contexto.parametros.string("nroSolicitud", "-1");
            String idMoneda = contexto.parametros.string("idMoneda", "80");
            String idTas = contexto.parametros.string("tasId"); 
            String correoElectronico = contexto.parametros.string("correoElectronico");
            String tarjetaDebitoVinculada = contexto.parametros.string("tarjetaDebitoVinculada", null);
            int sucursal = contexto.getKioscoContexto(Integer.valueOf(idTas)).getSucursalId();
            LogTAS.evento(contexto, "INICIO_GENERACION_CA", contexto.parametrosOfuscados());

            if(!termYCond) return devolverTerminosYCondiciones(contexto);
            List<TASTelefono> telefonosResponse = TASRestPersona.getTelefonosPersona(contexto, clienteSesion.getNumeroIdentificacionTributaria());
            Objeto cliente = TASApiPersona.getConsultarDatosPersonales(contexto);
            int secuencialDomicilio = cliente.objeto("domicilioPostal").integer("secuencialDomicilio");
            List<TASTelefono> telefonosFiltrados = filtrarTelefonos(telefonosResponse, secuencialDomicilio);
            if(telefonosFiltrados == null) return RespuestaTAS.error(contexto, "ERROR_FUNCIONAL", "Para continuar con la solicitud de apertura de tu Caja de Ahorro acercate a un Oficial de Negocios.<br><br>Muchas gracias.", "TASCuentaController - postGenerarCA()");
            
            Objeto datosCA = generarDatosCA(contexto, idMoneda, clienteSesion, secuencialDomicilio);
            
            Objeto responseCodResolucion = new Objeto();
            if(!nroSolicitud.equals("-1")){
                String tipoOperacion = contexto.config.string("tas_api_cajaahorro_tipooperacion");
                LogTAS.evento(contexto, "ENVIO_RESOLUCION_CA", new Objeto().set("nroSolicitud", nroSolicitud).toSimpleJson());
                responseCodResolucion = TASRestCuenta.putEnviarResolucion(contexto, nroSolicitud, tipoOperacion);
                if(responseCodResolucion.string("estado").contains("ERROR")){
                    Exception error = (Exception) responseCodResolucion.get("error");
                    return RespuestaTAS.error(contexto, "TASCuentaController - putEnviarResolucion()", error);
                } else if(responseCodResolucion.objeto("respuesta").objeto("Errores").objetos().size() > 0){
                    return armarResponseErrorVinculacion(contexto, responseCodResolucion.objeto("respuesta"));
                }
            }
            Objeto datosResolucion = responseCodResolucion.objeto("respuesta").objeto("Datos");
            String codigoResolucion = datosResolucion.objetos().get(0).string("ResolucionId");
            Objeto response = new Objeto();
            response.set("nroSolicitud", nroSolicitud);
            response.set("codigoResolucion", codigoResolucion);

            if(codigoResolucion.equals("AV")){
                Objeto datosAlta = armarDatosParaAlta(contexto, clienteSesion, cliente, datosCA, telefonosFiltrados, correoElectronico, String.valueOf(sucursal), tarjetaDebitoVinculada, nroSolicitud, codigoResolucion);
                LogTAS.evento(contexto,"INSERT_SOLICITUD_ALTA", datosAlta);
                Objeto insertSolicitud = TASSqlCuenta.guardarSolicitudAlta(contexto, datosAlta);
                if(!insertSolicitud.string("estado").equals("true")) return RespuestaTAS.error(contexto, "no se pudo hacer INSERT en la tabla de solicitudes");
                LogTAS.evento(contexto, "FINALIZAR_SOLICITUD_CA", new Objeto().set("nroSolicitud", nroSolicitud).toSimpleJson());
                Objeto finalizarResponse = TASRestCuenta.postFinalizarSolicitudCA(contexto, nroSolicitud);
                if(finalizarResponse.string("estado").contains("ERROR")){                    
                    Exception error = (Exception) finalizarResponse.get("error");
                    return RespuestaTAS.error(contexto, "TASCuentaController - postFinalizarSolicitudCA()", error);
                } else if(finalizarResponse.objeto("respuesta").objeto("Errores").objetos().size() > 0){
                    return armarResponseErrorVinculacion(contexto, finalizarResponse.objeto("respuesta"));

                }
            String nroTicket = generarNroTicket(idTas);
            LogTAS.evento(contexto, "UPDATE_SOLICITUD_ALTA", new Objeto().set("nroSolicitud", nroSolicitud).toSimpleJson());
            Objeto updateEstadoSolicitud = TASSqlCuenta.actualizarEstadoSolicitud(contexto, nroSolicitud, "OK");
            if(!updateEstadoSolicitud.string("estado").equals("true"))return RespuestaTAS.error(contexto, "no se pudo hacer INSERT en la tabla de solicitudes");
            Objeto ticket = new Objeto();
            ticket.set("nroTicket", nroTicket);
            String fecha = new Fecha(new Date()).string("dd/MM/yyyy");
            ticket.set("fechaOperacion", fecha);
            response.set("ticket", ticket); 
        } 
          LogTAS.evento(contexto,"FIN_GENERACION_CA", response);    
          return response;
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "TASCuentaController - getGenerarCA()", e);
        }
    }

    private static String generarNroTicket(String idTas){
        String nroTicket = UtilesDepositos.getNumeroTicket(idTas);
        return nroTicket;
    }

    private static List<TASTelefono> filtrarTelefonos(List<TASTelefono> telefonos, int secuencialDomicilio){
        List<TASTelefono> telefonosFiltrados = new ArrayList<>();
        for(TASTelefono telefono : telefonos){
            if(telefono.getIdDireccion() != null && telefono.getIdDireccion() == secuencialDomicilio){
                telefonosFiltrados.add(telefono);
            }            
        }
        return telefonosFiltrados.isEmpty() ? null : telefonosFiltrados;
    }

    private static Objeto generarDatosCA(ContextoTAS contexto, String idMoneda, TASClientePersona cliente, int secuencialDomicilio){
        Objeto datosCA = new Objeto();
        datosCA.set("categoria", contexto.parametros.string("idCategoria", "MOV"));
        datosCA.set("idMoneda", idMoneda);
        datosCA.set("nombre", cliente.getNombre() + " " + cliente.getApellido());
        datosCA.set("domicilio", secuencialDomicilio);
        datosCA.set("origenCuenta", contexto.config.string("tas_cajaahorro_crigencuenta"));
        datosCA.set("cobroPrimerMantenimiento", contexto.config.string("tas_cajaahorro_cobroprimermantenimiento"));
        datosCA.set("ciclo", contexto.config.string("tas_cajaahorro_ciclo"));
        datosCA.set("tipoPromedio", contexto.config.string("tas_cajaahorro_tipopromedio"));
        datosCA.set("capitalizacion", contexto.config.string("tas_cajaahorro_capitalizacion"));
        datosCA.set("usoFirma", contexto.config.string("tas_cajaahorro_usofirma"));
        datosCA.set("resumenMagnetico", contexto.config.string("tas_cajaahorro_resumenmagnetico"));
        datosCA.set("depositoInicial", contexto.config.string("tas_cajaahorro_depositoInicial"));
        datosCA.set("cuentaGastosPropia", contexto.config.string("tas_cajaahorro_cuentagastospropia"));
        datosCA.set("tipoCuentaGastos", contexto.config.string("tas_cajaahorro_tipocuentagastos"));
        datosCA.set("numeroCuentaGastos", contexto.config.string("tas_cajaahorro_numerocuentagastos"));
        datosCA.set("transfiereHaberesOtrasCuentas", contexto.config.string("tas_cajaahorro_transfierehaberesotrascuentas"));
        return datosCA;
    }

    private static Objeto armarDatosParaAlta(ContextoTAS contexto, TASClientePersona clienteSesion, Objeto cliente, Objeto datosCA, List<TASTelefono> telefonos, String mail, String sucursal, String tarjetaDebitoVinculada, String nroSolicitud, String codResolucion){
        Objeto parametrosAlta = new Objeto();
        parametrosAlta.set("tipoOperacion","ALTA");
        parametrosAlta.set("idCobis", clienteSesion.getIdCliente());
        String fechaSolicitud = new Fecha(new Date()).string("yyyy-MM-dd HH:mm:ss.sss");
        parametrosAlta.set("fechaSolicitud", fechaSolicitud);
        parametrosAlta.set("tipoProducto","AHO");
        parametrosAlta.set("moneda", datosCA.string("idMoneda"));
        parametrosAlta.set("sucursal", sucursal);
        parametrosAlta.set("nombre", clienteSesion.getNombre());
        parametrosAlta.set("apellido", clienteSesion.getApellido());
        parametrosAlta.set("sexo", clienteSesion.getSexo());
        parametrosAlta.set("tipoDocumento", clienteSesion.getTipoDocumento());
        parametrosAlta.set("numeroDocumento", clienteSesion.getNumeroDocumento());
        parametrosAlta.set("situacionLaboral", cliente.objeto("datosPersonales").string("situacionLaboral"));
        parametrosAlta.set("idTributario", clienteSesion.getNumeroIdentificacionTributaria());
        parametrosAlta.set("calle", cliente.objeto("domicilioPostal").string("calle"));
        parametrosAlta.set("altura", cliente.objeto("domicilioPostal").integer("altura"));
        parametrosAlta.set("piso", cliente.objeto("domicilioPostal").string("piso"));
        parametrosAlta.set("departamento", cliente.objeto("domicilioPostal").string("departamento"));
        parametrosAlta.set("codigoPostal", cliente.objeto("domicilioPostal").string("CodPostal"));
        parametrosAlta.set("provinciaCod", cliente.objeto("domicilioPostal").string("provincia"));
        parametrosAlta.set("provinciaDesc", cliente.objeto("domicilioPostal").string("descProvincia"));
        parametrosAlta.set("tipoTelefonoCod", "P");
        parametrosAlta.set("tipoTelefonoDesc", "PARTICULAR");
        parametrosAlta.set("ddiTelefono", telefonos.get(0).getCodigoArea());
        parametrosAlta.set("ddnTelefono", telefonos.get(0).getCodigoPais());
        parametrosAlta.set("caracteristicaTelefono", telefonos.get(0).getCaracteristica());
        parametrosAlta.set("numeroTelefono", telefonos.get(0).getNumero());
        parametrosAlta.set("tipoMail","EMP");
        parametrosAlta.set("mail", mail);
        parametrosAlta.set("pep",cliente.objeto("datosPersonales").string("pep"));
        parametrosAlta.set("so",cliente.objeto("datosPersonales").string("sujetoObligado"));
        parametrosAlta.set("fatca","A");
        parametrosAlta.set("licitudFondos","on");
        parametrosAlta.set("ocde",armarOcde(cliente));
        parametrosAlta.set("canal",contexto.canal());
        parametrosAlta.set("idSolicitud", nroSolicitud);
        parametrosAlta.set("estadoSolicitud","ERROR");
        parametrosAlta.set("tarjetaDebitoVinculada", tarjetaDebitoVinculada);
        if(codResolucion.equals("AV")) parametrosAlta.set("ttcc", "on");
        return parametrosAlta;
    }

    private static String armarOcde(Objeto cliente){
        String ocde = "";
        Objeto datos = cliente.objeto("regimenInformativoCliente");
        if(!datos.string("pais").isEmpty()) ocde += datos.string("pais") + "|";
        if(!cliente.objeto("datosPersonales").string("nroIdTrib").isEmpty()) ocde += cliente.objeto("datosPersonales").string("nroIdTrib") + "|";
        if(!datos.string("codResTrib1").isEmpty()) ocde += datos.string("codResTrib1") + "|";
        if(!datos.string("nroContPais1").isEmpty()) ocde += datos.string("nroContPais1") + "|";
        if(!datos.string("fechaDesdePais1").isEmpty()) ocde += datos.string("fechaDesdePais1") + "|";
        if(!datos.string("codResTrib2").isEmpty()) ocde += datos.string("codResTrib2") + "|";
        if(!datos.string("nroContPais2").isEmpty()) ocde += datos.string("nroContPais2") + "|";
        if(!datos.string("fechaDesdePais2").isEmpty()) ocde += datos.string("fechaDesdePais2") + "|";
        return ocde;
    }


    private static Objeto devolverTerminosYCondiciones(ContextoTAS contexto){
        try {
            Objeto responseTextos = TASFormulariosController.getBuscarTextos(contexto);
            LogTAS.evento(contexto,"FIN_GENERACION_TYC");
            return responseTextos;
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "TASCuentaController - devolverTerminosYCondiciones()", e);
        }
    }

    public static Objeto postVincularTD(ContextoTAS contexto){
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");

            String nroSolicitud = contexto.parametros.string("nroSolicitud");
            String nroTarjeta = contexto.parametros.string("numeroTarjeta", "-1");
            String idMoneda = contexto.parametros.string("idMoneda", "80");
            boolean esVirtual = contexto.parametros.bool("esVirtual", false);
            LogTAS.evento(contexto, "INICIO_VINCULAR_TD", contexto.parametrosOfuscados());
            Objeto parametrosEnviarCA = armarParametrosEnvioCA(contexto, clienteSesion, nroSolicitud, nroTarjeta, idMoneda,"DP");
            LogTAS.evento(contexto, "ENVIO_CA", parametrosEnviarCA);
            Objeto enviarCAResponse = TASRestCuenta.postEnviarCA(contexto, parametrosEnviarCA);

            if(enviarCAResponse.string("estado").contains("ERROR")){
                Exception error = (Exception) enviarCAResponse.get("error");
                return RespuestaTAS.error(contexto, "TASCuentaController - postVincularTD()", error);
            } else if (enviarCAResponse.objeto("respuesta").objeto("Errores").objetos().size() > 0) {
                return armarResponseErrorVinculacion(contexto, enviarCAResponse.objeto("respuesta"));
            }
            Objeto respuestaEnvioCA = enviarCAResponse.objeto("respuesta").objeto("Datos");
            String idCajaAhorro = respuestaEnvioCA.objetos().get(0).string("Id");

            Objeto response = new Objeto();
            response.set("cajaAhorro", Integer.valueOf(idCajaAhorro));
            String idTarjetaDebito = "0";
            if(nroTarjeta.equals("-1") || nroTarjeta.equals("")){
                Objeto parametrosEnviarTD = armarParametrosEnvioTD(contexto, clienteSesion, nroSolicitud, idMoneda, "DP", esVirtual);
                LogTAS.evento(contexto, "ENVIO_TD", parametrosEnviarTD);
                Objeto enviarTDResponse = TASRestCuenta.postEnviarTD(contexto, parametrosEnviarTD);
                if(enviarTDResponse.string("estado").contains("ERROR")){
                    Exception error = (Exception) enviarTDResponse.get("error");
                    return RespuestaTAS.error(contexto, "TASCuentaController - postVincularTD()", error);
                }else if (enviarTDResponse.objeto("respuesta").objeto("Errores").objetos().size() > 0) {
                    return armarResponseErrorVinculacion(contexto, enviarTDResponse.objeto("respuesta"));
                }

                Objeto respuestaEnvioTD = enviarTDResponse.objeto("respuesta").objeto("Datos");
                idTarjetaDebito = respuestaEnvioTD.objetos().get(0).string("Id");                
            }
            response.set("tarjetaDebito", idTarjetaDebito.equals("0") ? 0 : Integer.valueOf(idTarjetaDebito));
            String leyTDVirtual = "";
            if(esVirtual){
                leyTDVirtual = contexto.config.string("tas_leyenda_tdvirtual");
                response.set("leyTDVirtual", leyTDVirtual);
            }
            LogTAS.evento(contexto, "FIN_VINCULAR_TD", response);
            return response;
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "TASCuentaController - postVincularTD()", e);
        }
    }

    private static Objeto armarParametrosEnvioCA(ContextoTAS contexto, TASClientePersona cliente, String nroSolicitud, String nroTarjeta, String idMoneda, String tipoDomicilio){
        Objeto parametros = new Objeto();
        parametros.set("tipoOperacion", contexto.config.string("tas_api_cajaahorro_tipooperacion"));
        parametros.set("productoBancario", contexto.config.string("tas_api_producto_bancario"));
        parametros.set("categoria", contexto.config.string("tas_cajaahorro_categoria"));
        parametros.set("oficial", contexto.config.string("tas_cajaahorro_oficial"));
        parametros.set("cobroPrimerMantenimiento", contexto.config.string("tas_cajaahorro_cobroprimermantenimiento").equals("1") ? true : false);
        parametros.set("origen", contexto.config.string("tas_cajaahorro_crigencuenta"));
        parametros.set("usoFirma", contexto.config.string("tas_cajaahorro_usofirma"));
        parametros.set("ciclo", contexto.config.string("tas_cajaahorro_ciclo"));
        parametros.set("resumenMagnetico", contexto.config.string("tas_cajaahorro_resumenmagnetico").equals("1") ? true : false);
        parametros.set("transfiereAcredHab", contexto.config.string("tas_cajaahorro_transfierehaberesotrascuentas").equals("1") ? true : false);

        //todo ver si puedo mandar todo como params
        Objeto cuentasLegales = new Objeto();
        cuentasLegales.set("uso", contexto.config.string("tas_cajaahorro_cuentalegalesuso"));
        cuentasLegales.set("realizaTransferencias", contexto.config.string("tas_cajaahorro_cuentalegalesrealizatransferencias").equals("1") ? true : false);
        parametros.set("cuentasLegales", cuentasLegales);

        Objeto domicilioResumen = new Objeto();
        domicilioResumen.set("tipo", tipoDomicilio);
        parametros.set("domicilioResumen", domicilioResumen);
        parametros.set("moneda", idMoneda);

        //todo ver si puedo mandar todo como params
        Objeto integrantes = new Objeto();
        integrantes.set("numeroDocumentoTributario", cliente.getNumeroIdentificacionTributaria());
        integrantes.set("rol", contexto.config.string("tas_cajaahorro_integranterol"));
        integrantes.set("numeroTarjetaDebito", nroTarjeta);
        parametros.set("integrantes", integrantes);

        parametros.set("nroSolicitud", nroSolicitud);
       
        return parametros;
    }

    private static Objeto armarParametrosEnvioTD(ContextoTAS contexto,TASClientePersona clienteSesion, String nroSolicitud, String idMoneda, String tipoDomicilio, boolean esVirtual){
        Objeto params = new Objeto();
        params.set("tipoOperacion", contexto.config.string("tas_api_cajaahorro_tipooperacion"));
        params.set("tipo", contexto.config.string("tas_tarjetadebito_tipo"));
        params.set("grupo", contexto.config.string("tas_tarjetadebito_grupo"));
        params.set("tipoCuentaComision", contexto.config.string("tas_tarjetadebito_tipocuentacomision"));
        params.set("numeroCuentaComision", contexto.config.string("tas_tarjetadebito_numerocuentacomision"));

        Objeto domicilioResumen = new Objeto();
        domicilioResumen.set("tipo", tipoDomicilio);
        params.set("domicilio", domicilioResumen);

        Objeto integrantes = new Objeto();
        integrantes.set("numeroDocumentoTributario", clienteSesion.getNumeroIdentificacionTributaria());
        integrantes.set("rol", contexto.config.string("tas_cajaahorro_integranterol"));
        params.set("integrantes", integrantes);

        Objeto tarjetasDebitoCuentasOperativas = new Objeto();
        tarjetasDebitoCuentasOperativas.set("cuenta", contexto.config.string("tas_tarjetadebito_cuentasoperativascuenta"));
        tarjetasDebitoCuentasOperativas.set("producto", contexto.config.string("tas_tarjetadebito_cuentasoperativasproducto"));
        tarjetasDebitoCuentasOperativas.set("principal", contexto.config.string("tas_tarjetadebito_cuentasoperativasprincipal").equals("1") ? true : false);
        tarjetasDebitoCuentasOperativas.set("moneda", idMoneda);

        params.set("tarjetasDebitoCuentasOperativas", tarjetasDebitoCuentasOperativas);
        params.set("esVirtual", esVirtual);
        params.set("nroSolicitud", nroSolicitud);
        return params;
    }

    private static Objeto armarResponseErrorVinculacion(ContextoTAS contexto, Objeto response){
        try{
            Objeto e = response.objeto("Errores");
            Exception exception = new Exception(e.objetos().get(0).string("MensajeCliente"));
            LogTAS.error(contexto, exception);
        if(e.objetos().size() >= 1){            
            String mensaje = e.objetos().get(0).string("MensajeDesarrollador").contains("FUERA_HORARIO") 
            || e.objetos().get(0).string("MensajeCliente").contains("FUERA_HORARIO") ? "FUERA_HORARIO" : "ERROR";
            if(mensaje.equals("FUERA_HORARIO")) return RespuestaTAS.error(contexto, mensaje, "El horario permitido para realizar la operación es de 4 a 22 hs.", "TASCuentaController - postEnvioSolicitudCA()" );
            return RespuestaTAS.error(contexto,"ERROR_FUNCIONAL", "En este momento no podemos procesar tu solicitud. Por favor reintentá más tarde.<br><br>Muchas gracias." , "TASCuentaController - postEnvioSolicitudCA()");
        }else{
            return RespuestaTAS.error(contexto,"ERROR_FUNCIONAL", "En este momento no podemos procesar tu solicitud. Por favor reintentá más tarde.<br><br>Muchas gracias." , "TASCuentaController - postEnvioSolicitudCA()");
            }
        }catch(Exception ex){
            return RespuestaTAS.error(contexto, "TASCuentaController - armarResponseErrorSolicitud()", ex);
            }
        }

}
