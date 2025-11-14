package ar.com.hipotecario.canal.officebanking;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import ar.com.hipotecario.backend.Parametros;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.ApiCatalogo;
import ar.com.hipotecario.backend.servicio.api.catalogo.SucursalesOBV2;
import ar.com.hipotecario.backend.servicio.api.cheques.*;
import ar.com.hipotecario.backend.servicio.api.cheques.AdmitirAvalEcheqOB;
import ar.com.hipotecario.backend.servicio.api.cheques.AdmitirEcheqOB;
import ar.com.hipotecario.backend.servicio.api.cheques.AdmitirRepudiarCesionOB;
import ar.com.hipotecario.backend.servicio.api.cheques.AnularAvalEcheqOB;
import ar.com.hipotecario.backend.servicio.api.cheques.AnularCesionEcheqOB;
import ar.com.hipotecario.backend.servicio.api.cheques.AnularEcheqOB;
import ar.com.hipotecario.backend.servicio.api.cheques.AnularEndosoEcheqOB;
import ar.com.hipotecario.backend.servicio.api.cheques.ApiCheques;
import ar.com.hipotecario.backend.servicio.api.cheques.AvalEcheqOB;
import ar.com.hipotecario.backend.servicio.api.cheques.CuentaEcheqOB;
import ar.com.hipotecario.backend.servicio.api.cheques.CustodiarEcheqOB;
import ar.com.hipotecario.backend.servicio.api.cheques.DepositarEcheqOB;
import ar.com.hipotecario.backend.servicio.api.cheques.DescontarChequeOB;
import ar.com.hipotecario.backend.servicio.api.cheques.DetalleAltaChequeraOB;
import ar.com.hipotecario.backend.servicio.api.cheques.DetalleChequeraActivaOB;
import ar.com.hipotecario.backend.servicio.api.cheques.DetalleChequeraOB;
import ar.com.hipotecario.backend.servicio.api.cheques.DetalleEmisionEcheq;
import ar.com.hipotecario.backend.servicio.api.cheques.DetalleRazonSocialOB;
import ar.com.hipotecario.backend.servicio.api.cheques.DevolucionEcheqOB;
import ar.com.hipotecario.backend.servicio.api.cheques.EmitirCesionEcheqOB;
import ar.com.hipotecario.backend.servicio.api.cheques.EndosoEcheqOB;
import ar.com.hipotecario.backend.servicio.api.cheques.EntidadesMercadoOB;
import ar.com.hipotecario.backend.servicio.api.cheques.ListadoChequesADescontarOB;
import ar.com.hipotecario.backend.servicio.api.cheques.ListadoChequesOB;
import ar.com.hipotecario.backend.servicio.api.cheques.RechazarEcheqOB;
import ar.com.hipotecario.backend.servicio.api.cheques.RepudioAvalEcheqOB;
import ar.com.hipotecario.backend.servicio.api.cheques.RescatarEcheqOB;
import ar.com.hipotecario.backend.servicio.api.cheques.SimularDescuentoOB;
import ar.com.hipotecario.backend.servicio.api.cuentas.ApiCuentas;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentasCorrientes;
import ar.com.hipotecario.backend.servicio.api.productos.CuentasOB;
import ar.com.hipotecario.backend.servicio.api.recaudaciones.ApiRecaudaciones;
import ar.com.hipotecario.backend.servicio.api.recaudaciones.ConveniosSugeridosEcheqOB;
import ar.com.hipotecario.backend.servicio.api.recaudaciones.RelacionEcheqConvenioOB;
import ar.com.hipotecario.backend.util.DateUtils;
import ar.com.hipotecario.canal.officebanking.dto.ErrorGenericoOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumAccionesOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumSucursalesOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;
import ar.com.hipotecario.canal.officebanking.enums.echeq.EnumAccionesEcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.*;
import ar.com.hipotecario.canal.officebanking.util.ConstantesOB;
import ar.com.hipotecario.canal.officebanking.util.StringUtil;


public class OBEcheq extends ModuloOB {

    public static final String DATOS_INVALIDOS = "DATOS_INVALIDOS";
    public static final String NUMERO_CUENTA = "numeroCuenta";
    public static final String DATOS = "datos";
    public static final String FECHA_PAGO = "fechaPago";
    public static final String ID_CHEQUE = "idCheque";

    public static final String RESPUESTA = "respuesta";

    private static final String FILTER_RECIBIDOS = "cuit eq __%s__" +
            " and (cheques.emitido_a.beneficiario_documento eq __%1$s__" +
            " or cheques.tenencia.beneficiario_documento eq __%1$s__" +
            " or cheques.ultimo_avalista eq __%1$s__" +
            " or cheques.avalado_por eq __%1$s__" +
            " or cheques.ultimo_cesionario eq __%1$s__" +
            " or cheques.ult_mandatario eq __%1$s__" +
            " or cheques.mandato_por eq __%1$s__" +
            ") and cheques.estado ne __REPUDIADO__";

    private static final String FILTER_EMITIDOS = "cuit eq __%s__" +
            " and cheques.cuenta_emisora.emisor_cuit eq __%1$s__";

    private static final String FILTER_ENDOSADOS = "cuit eq __%s__" +
            " and cheques.endosado_por eq __%1$s__";

    private static final String FILTER_CEDIDOS = "cuit eq __%s__" +
            " and cheques.cedido_por eq __%1$s__";

    public static final String ID_OPERACION = "idOperacion";
    public static final String ACCION = "accion";
    public static final String ERROR = "ERROR";
    public static final String EMITIDO_PENDIENTE = "EMITIDO-PENDIENTE";
    public static final String ACTIVO_PENDIENTE = "ACTIVO-PENDIENTE";
    public static final String ACTIVO = "ACTIVO";
    public static final String RECHAZADO = "RECHAZADO";
    public static final String DEVOLUCION_PENDIENTE = "DEVOLUCION-PENDIENTE";
    public static final String CESION_PENDIENTE = "CESION-PENDIENTE";
    public static final String AVAL_PENDIENTE = "AVAL-PENDIENTE";
    public static final String CUSTODIA = "CUSTODIA";
    public static final String MANDATO_PENDIENTE = "MANDATO-PENDIENTE";
    public static final String NEGOCIACION_PENDIENTE = "NEGOCIACION-PENDIENTE";


    public static Object listadoCuentasCtes(ContextoOB contexto) {
        LogOB.evento(contexto,"listadoCuentasCtes", "INICIO");
        Objeto respuesta = new Objeto();
        CuentasOB totalCuentas = ApiCuentas.cuentas(contexto, contexto.sesion().empresaOB.idCobis).get();
        //CARGA D DATOS
        Objeto cuentas = new Objeto();
        totalCuentas.stream().filter(c -> c.tipoProducto.equals("CTE") && c.descMoneda.equals("PESOS")).forEach(c -> {
            CuentasCorrientes.CuentaCorriente detalleCuenta = ApiCuentas.cuentaCorriente(contexto, c.numeroProducto, Fecha.ahora(), false, false).get();
            Objeto cuentaCorriente = new Objeto();
            cuentaCorriente.set("monto", detalleCuenta.saldoGirar);
            cuentaCorriente.set(NUMERO_CUENTA, c.numeroProducto);
            cuentas.add("cuentaCorriente", cuentaCorriente);
        });

        LogOB.evento(contexto,"listadoCuentasCtes", "FIN");
        respuesta.set("cuentas", cuentas);
        return respuesta("0", DATOS, respuesta);
    }

    public static Object obtenerRazonSocial(ContextoOB contexto) {
        LogOB.evento(contexto,"obtenerRazonSocial", "INICIO");
        String cuit = contexto.parametros.string("cuit");

        DetalleRazonSocialOB detalleRazonSocial;
        Objeto datos = new Objeto();

        try {
            detalleRazonSocial = ApiCheques.razonSocial(contexto, cuit).get();
        } catch (Exception e) {
            return new ErrorGenericoOB().setErrores("Cuit inválido.", "No se encontró la razón social para ese cuit.");
        }

        LogOB.evento(contexto,"listadoCuentasCtes", "Ok");
        datos.set("datos", detalleRazonSocial.result.beneficiario_razon_social.trim());
        LogOB.evento(contexto,"obtenerRazonSocial", "FIN");
        return respuesta(DATOS, datos);
    }

    public static Object obtenerRazonSocialIMF(ContextoOB contexto) {
        LogOB.evento(contexto,"obtenerRazonSocial", "INICIO");
        String cuit = contexto.parametros.string("cuit");

        DetalleRazonSocialOB detalleRazonSocial;
        Objeto datos = new Objeto();
        boolean esIMF = true;
        try {
            detalleRazonSocial = ApiCheques.razonSocial(contexto, cuit).get();
        } catch (Exception e) {
            return new ErrorGenericoOB().setErrores("Cuit inválido.", "No se encontró la razón social para ese cuit.");
        }
        EntidadesMercadoOB entidadesMercadoOB= ApiCheques.entidadesMercado(contexto).get();
        LogOB.evento(contexto,"solicitudEndosoEcheq", "ApiCheques.entidadesMercado ejecutada");
        Optional<EntidadesMercadoOB.infraestructurasMercado> entidadMercado = entidadesMercadoOB.result.infraestructurasMercado.stream().filter(entidad -> entidad.documento.equals(cuit)).findFirst();
        if (entidadMercado.isEmpty()) {
           esIMF = false;
        }


        LogOB.evento(contexto,"listadoCuentasCtes", "Ok");
        datos.set("datos", detalleRazonSocial.result.beneficiario_razon_social.trim());
        datos.set("esIMF",esIMF);
        LogOB.evento(contexto,"obtenerRazonSocial", "FIN");
        return respuesta(DATOS, datos);
    }

    public static Object guardarBeneficiario(ContextoOB contexto) {
        String cuit = contexto.parametros.string("cuit");
        String razonSocial = contexto.parametros.string("razonSocial");
        String email = contexto.parametros.string("email",null);

        if (cuit.length() != 11 || razonSocial.isBlank()) {
            return new ErrorGenericoOB().setErrores("Cuit o razón social invalidos", "Cuit o razón social invalidos");
        }

        ServicioRazonesPorEmpresaOB servicioRazonesPorEmpresa = new ServicioRazonesPorEmpresaOB(contexto);
        if (servicioRazonesPorEmpresa.findByEmpresa(contexto.sesion().empresaOB).get().stream().anyMatch(razonPorEmpresa->razonPorEmpresa.cuit.equals(cuit))){
            return new ErrorGenericoOB().setErrores("Beneficiario invalido", "Ya esta registrado el beneficiario");
        }
        RazonesPorEmpresaOB razonesPorEmpresa = servicioRazonesPorEmpresa.create(contexto.sesion().empresaOB, cuit, razonSocial,email).get();


        if (empty(razonesPorEmpresa) || razonesPorEmpresa == null) {
            return new ErrorGenericoOB().setErrores("Error guardando el beneficiario", "Error guardando el beneficiario");
        }

        return respuesta("0");
    }

    public static Object obtenerBeneficiarios(ContextoOB contexto) {
        ServicioRazonesPorEmpresaOB servicioRazonesPorEmpresa = new ServicioRazonesPorEmpresaOB(contexto);
        List<RazonesPorEmpresaOB> razonesPorEmpresa = servicioRazonesPorEmpresa.findByEmpresa(contexto.sesion().empresaOB).get();
        Objeto respuesta = new Objeto();
        razonesPorEmpresa.stream().forEach(razon -> {
            Objeto beneficiario = new Objeto();
            beneficiario.set("cuit", razon.cuit);
            beneficiario.set("razonSocial", razon.razonSocial);
            beneficiario.set("email",razon.email);
            respuesta.add(beneficiario);
        });
        return respuesta("0", "beneficiarios", respuesta);
    }

    public static Object chequerasDisponibles(ContextoOB contexto) {
        LogOB.evento(contexto,"chequerasDisponibles", "INICIO");
        String operacion = contexto.parametros.string("operacion");
        String numeroCuenta = contexto.parametros.string(NUMERO_CUENTA);

        switch (operacion) {
            case "O" -> {
                DetalleChequeraOB detalleChequeras = ApiCheques.chequerasDisponibles(contexto, numeroCuenta, operacion).get();
                return respuesta("0", "chequeras", detalleChequeras);

            }
            case "C" -> {
                DetalleChequeraActivaOB detalleChequeraActivas = ApiCheques.chequerasActivas(contexto, numeroCuenta, operacion).get();
                return respuesta("0", "chequeras", detalleChequeraActivas);
            }
            default -> {
                return new ErrorGenericoOB().setErrores("Valor de operacion incorrecto", "Operacion debe ser 'O' o 'C'");
            }
        }
    }

    public static Object puedoEmitirchequera(ContextoOB contexto) {
        String numeroCuenta = contexto.parametros.string(NUMERO_CUENTA);
        DetalleChequeraActivaOB detalleChequeraActivas;
        try{
            detalleChequeraActivas = ApiCheques.chequerasActivas(contexto, numeroCuenta, "C").get();
            LogOB.evento(contexto, "puedoEmitirchequera", new Objeto().set("Chequeras activas:",detalleChequeraActivas.size()));
        } catch (Exception e) {
            LogOB.evento(contexto, "puedoEmitirchequera", new Objeto().set("error", e.getMessage()));
            return new ErrorGenericoOB().setErrores("Error", "No se encontraron chequeras acivas.");
        }
        int chequesDisponibles = detalleChequeraActivas.stream().mapToInt(chequera -> chequera.CANT_CHEQUES_DISPONIBLE).sum();
        if (chequesDisponibles < 5) {
            LogOB.evento(contexto,"puedoEmitirchequera", "Ok");
            return respuesta("0", DATOS, true);
        } else {
            Objeto devolucion = new Objeto();
            devolucion.add("chequesDisponibles", chequesDisponibles);
            devolucion.add("puedeEmitir", false);
            LogOB.evento(contexto,"puedoEmitirchequera", "Ok");

            return respuesta("0", DATOS, devolucion);
        }

    }

    public static Object solicitudChequera(ContextoOB contexto) {
        String cuentaBanco = contexto.parametros.string("cuentaBanco");
        String tipoChequera = contexto.parametros.string("tipoChequera");

        SesionOB sesion = contexto.sesion();

        ServicioChequeraOB servicioChequeraOB = new ServicioChequeraOB(contexto);
        ServicioTipoProductoFirmaOB servicioTipoProductoFirmaOB = new ServicioTipoProductoFirmaOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        TipoProductoFirmaOB producto = servicioTipoProductoFirmaOB.findByCodigo(EnumTipoProductoOB.CHEQUERA_ELECTRONICA.getCodigo()).get();
        AccionesOB accionCrear = servicioAcciones.find(EnumAccionesOB.CREAR.getCodigo()).get();
        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);
        ChequeraOB chequera = servicioChequeraOB.crear(contexto, tipoChequera, cuentaBanco, producto,contexto.sesion().usuarioOB).get();
        ServicioHistorialChequeraOB servicioHistorial = new ServicioHistorialChequeraOB(contexto);
        servicioHistorial.crear(chequera, accionCrear, empresaUsuario);
        Objeto respuesta = new Objeto();
        respuesta.set(ID_OPERACION, chequera.id);
        return respuesta("0", DATOS, respuesta);
    }

    public static int altaChequera(ContextoOB contexto, ChequeraOB chequera) {
        String razonSocial = contexto.sesion().empresaOB.razonSocial;
        razonSocial = razonSocial.length() > 60 ? razonSocial.substring(0, 59) : razonSocial;
        LogOB.evento(contexto,"altaChequera", "INICIO");
        CuentaEcheqOB cuentas = ApiCheques.consultaCuentaEcheq(contexto,chequera.empresa.cuit.toString()).get();
        CuentasOB cuentasOB = ApiCuentas.cuentas(contexto, contexto.sesion().empresaOB.idCobis).get();
        Optional<CuentasOB.CuentaOB> cuentaSeleccionada = cuentasOB.stream().filter(cuenta->cuenta.numeroProducto.equals(chequera.cuentaOrigen)).findFirst();
        boolean existeCBU = false;
        int i =0;
        while(i<cuentas.result.cuentas.size()&&!existeCBU){
            if (cuentas.result.cuentas.get(i).emisor_cbu.equals(cuentaSeleccionada.get().cbu)&&cuentas.result.cuentas.get(i).cuenta_estado.equals("ACTIVA")){
                existeCBU = true;
            }
            i++;
        }
        if (!existeCBU){
            SucursalesOBV2 sucursal = ApiCatalogo.sucursalesOBV2(contexto,cuentaSeleccionada.get().sucursal).get();
            ApiCheques.altaCuentaEcheq(contexto,StringUtil.agregarCerosAIzquierda(String.valueOf(sucursal.codSucursal),4),sucursal.desSucursal,sucursal.domicilio,sucursal.codigoPostal, String.valueOf(sucursal.codProvincia),contexto.sesion().empresaOB.cuit.toString(),razonSocial,cuentaSeleccionada.get().cbu,cuentaSeleccionada.get().numeroProducto.substring(cuentaSeleccionada.get().numeroProducto.length()-11),"032",sucursal.domicilio,sucursal.codigoPostal).get();
        }
        DetalleAltaChequeraOB.DetalleAltaChequera detalleAltaChequeras = ApiCheques.altaChequera(contexto, chequera.cuentaOrigen, chequera.tipoChequera).get();
        LogOB.evento(contexto,"altaChequera", "FIN");
        return detalleAltaChequeras.NRO_CHEQUERA;
    }

    public static Object solicitarEmisionEcheq(ContextoOB contexto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSS");
        String numeroChequera = contexto.parametros.string("numeroChequera");
        BigDecimal monto = new BigDecimal(contexto.parametros.string("monto"));
        String documentoBeneficiario = contexto.parametros.string("documentoBeneficiario");
        String tipoDocumentoBeneficiario = "cuit";
        String emailBeneficiario = contexto.parametros.string("emailBeneficiario", null);
        boolean aLaOrden = contexto.parametros.bool("aLaOrden");
        String motivo = contexto.parametros.string("motivo","");
        String concepto = contexto.parametros.string("concepto");
        boolean cruzado = true;
        String cuentaBanco = contexto.parametros.string("cuentaBanco");
        String beneficiarioRazonSocial = contexto.parametros.string("beneficiarioRazonSocial");
        LocalDateTime fechaPago = LocalDateTime.parse(contexto.parametros.string(FECHA_PAGO) + "T00:00:00.000", formatter);
        Date date = DateUtils.getFechaInicioDia(new Date());
        Date date1 = Date.from(fechaPago.atZone(ZoneId.systemDefault()).toInstant());

        if (monto.longValue() > 9999999999.99) {
            return new ErrorGenericoOB().setErrores("Monto incorrecto", "El monto no puede ser mayor a 9999999999.99");
        }

        Date fechaVencimiento = DateUtils.getFechaInicioDia(date1);
        String tipo;
        if (fechaVencimiento.compareTo(date) > 0) {
            tipo = "CPD";
        } else if (fechaVencimiento.compareTo(date) == 0) {
            tipo = "CC";
        } else {
            return new ErrorGenericoOB().setErrores("Fecha de pago incorrecta", "La fecha de pago no puede ser menor a la actual");
        }

        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioHistorialEcheqOB servicioHistorial = new ServicioHistorialEcheqOB(contexto);
        SesionOB sesion = contexto.sesion();
        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);
        EmpresaOB empresa = contexto.sesion().empresaOB;


        ServicioEcheqOB servicioEcheqOB = new ServicioEcheqOB(contexto);
        String documentoEmisor = contexto.sesion().empresaOB.cuit.toString();
        EcheqOB echeqOB = servicioEcheqOB.crear(numeroChequera, monto, documentoBeneficiario, tipoDocumentoBeneficiario, emailBeneficiario, aLaOrden, motivo, concepto, cruzado, cuentaBanco, beneficiarioRazonSocial, empresa, fechaPago, tipo, EnumAccionesEcheqOB.EMISION, documentoEmisor,sesion.usuarioOB).get();
        servicioHistorial.crear(echeqOB, servicioAcciones.find(EnumAccionesOB.CREAR.getCodigo()).get(), empresaUsuario).get();
        Objeto respuestaEcheq = new Objeto();
        respuestaEcheq.set(ID_OPERACION, echeqOB.id);
        return respuesta("0", DATOS, respuestaEcheq);
    }

    public static Object emisionEcheq(ContextoOB contexto) {
        LogOB.evento(contexto,"emisionEcheq", "INICIO");

        int id = contexto.parametros.integer(ID_OPERACION);
        EcheqOB echeq = ServicioEcheqOB.find(id).get();

        if (empty(echeq) || echeq == null) {
            return respuesta(DATOS_INVALIDOS);
        }
        CuentasOB cuentas = ApiCuentas.cuentas(contexto, contexto.sesion().empresaOB.idCobis).get();
        LogOB.evento(contexto,"emisionEcheq", "Cargado de cuentas");
        List<CuentasOB.CuentaOB> listaCuenta = cuentas.stream().filter(c -> c.numeroProducto.equals(echeq.cuentaOrigen)).toList();
        CuentasOB.CuentaOB cuenta = listaCuenta.get(0);
        LogOB.evento(contexto,"emisionEcheq", "Cargado de sucursales");
        String codigoPostal = EnumSucursalesOB.getCodigoPostalBySucursal(Integer.valueOf(cuenta.sucursal));
        DetalleEmisionEcheq detalleEmisionEcheq = ApiCheques.altaEcheq(contexto, echeq, cuenta.cbu, StringUtil.agregarCerosAIzquierda(cuenta.sucursal, 3), codigoPostal).get();
        LogOB.evento(contexto,"emisionEcheq", "Alta Echeq");
        LogOB.evento(contexto,"emisionEcheq", "FIN");
        return detalleEmisionEcheq.result;
    }

    public static Object listadoCheques(ContextoOB contexto) {
        LogOB.evento(contexto,"listadoCheques", "INICIO");
        String cantRegistros = contexto.parametros.string("cantRegistros");
        String accion = contexto.parametros.string(ACCION);
        if (!accion.equalsIgnoreCase("recibidos") && !accion.equalsIgnoreCase("emitidos") && !accion.equalsIgnoreCase("endosados") && !accion.equalsIgnoreCase("cedidos")) {
            return new ErrorGenericoOB().setErrores("Accion incorrecta", "La accion debe ser 'recibidos', 'emitidos', 'endosados' o 'cedidos'");
        }
        String filter = null;
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        switch (accion){
            case "recibidos" ->filter = String.format(FILTER_RECIBIDOS, cuit);
            case "emitidos" ->filter = String.format(FILTER_EMITIDOS, cuit);
            case "endosados" ->filter = String.format(FILTER_ENDOSADOS, cuit);
            case "cedidos" ->filter = String.format(FILTER_CEDIDOS,cuit);
        }
        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
        ServicioEcheqOB servicioEcheqOB = new ServicioEcheqOB(contexto);
        ListadoChequesOB listado = ApiCheques.listadoCheques(contexto, cuit, cantRegistros, filter,"1").get();
        List<EcheqOB> chequesEnBandeja = listado.result.cheques.size()>0? servicioEcheqOB.findChequeByEmisorAndIdAndEstado(contexto.sesion().empresaOB.cuit.toString()).get():Collections.emptyList();
        LogOB.evento(contexto,"listadoCheques", "listado de cheques");

        Objeto respuesta = new Objeto();
        listado.result.cheques.stream().forEach(item -> {
            if (item.fecha_pago_vencida) {
                caducarCheque(contexto, cuit, item);
            }
            EnumAccionesEcheqOB accionRecepcion = null;
            if (accion.equals("recibidos")){
                accionRecepcion = mapearMetodoRecepcion(item,cuit);
            }
            EcheqOB chequeBase = servicioEcheqOB.findByField("idCheque", item.cheque_id,accion,cuit).get();
            Objeto cheque = new Objeto();
            cheque.set("importe", item.monto);
            cheque.set("estado", item.estado);
            cheque.set("numeroCheque", item.cheque_numero);
            cheque.set("numeroChequera", item.numero_chequera);
            cheque.set("emailBeneficiario", chequeBase == null ? null : chequeBase.emailBeneficiario);
            cheque.set(ID_CHEQUE, item.cheque_id);
            cheque.set("caracter", item.cheque_caracter);
            cheque.set("tipo", item.cheque_tipo);
            cheque.set("fechaEmision", item.fecha_emision.substring(0, item.fecha_emision.indexOf('T')));
            cheque.set(FECHA_PAGO, item.fecha_pago.substring(0, item.fecha_pago.indexOf('T')));
            cheque.set("bancoEmisor", item.cuenta_emisora.banco_nombre);
            cheque.set("codigoBanco", item.cuenta_emisora.banco_codigo);
            cheque.set("codigoSucursal", item.cuenta_emisora.sucursal_codigo);
            cheque.set("codigoPostalSucursal", item.cuenta_emisora.sucursal_cp);
            cheque.set("cuentaOrigen", "3"+item.cuenta_emisora.sucursal_codigo.substring(item.cuenta_emisora.sucursal_codigo.length()-3)+item.cuenta_emisora.emisor_cuenta);
            if (accion!=null){
                if (item.estado.equalsIgnoreCase("rechazado")||(item.estado.equalsIgnoreCase("devolucion-pendiente")&&item.rechazos!=null)){
                    cheque.set("detalleRechazo",mapearMensajeRechazo(item,accion));
                    if (cheque.get("detalleRechazo")==null){
                        cheque.del("detalleRechazo");
                    }
                }
            }
            if (accion.equals("endosados")&&item.endosos !=null){
                cheque.set("emisorCuit", item.endosos.get(0).emisor_documento);
                cheque.set("emisorRazonSocial", item.endosos.get(0).emisor_razon_social);
                cheque.set("beneficiarioRazonSocial", item.endosos.get(0).benef_razon_social);
                cheque.set("beneficiarioCuit", item.endosos.get(0).benef_documento);
            }else if(accion.equals("cedidos")&&item.cesiones!=null){
                cheque.set("emisorCuit", item.cesiones.get(0).cedente_documento);
                cheque.set("emisorRazonSocial", item.cesiones.get(0).cedente_nombre);
                cheque.set("beneficiarioRazonSocial", item.cesiones.get(0).cesionario_nombre);
                cheque.set("beneficiarioCuit", item.cesiones.get(0).cesionario_documento);
            }else if (accionRecepcion!=null){
                if(accionRecepcion.equals(EnumAccionesEcheqOB.ENDOSO)&&item.endosos!=null){
                    int indice = obtenerIndiceEndosoRecepcion(item.endosos,cuit);
                    cheque.set("emisorCuit", item.endosos.get(indice).emisor_documento);
                    cheque.set("emisorRazonSocial", item.endosos.get(indice).emisor_razon_social);
                    cheque.set("beneficiarioRazonSocial", item.endosos.get(indice).benef_razon_social);
                    cheque.set("beneficiarioCuit", item.endosos.get(indice).benef_documento);
                    cheque.set("accionRecepcion","ENDOSO");
                } else if (accionRecepcion.equals(EnumAccionesEcheqOB.CESION)&&item.cesiones!=null){
                    int indice = obtenerIndiceCesionRecepcion(item.cesiones,cuit);
                    cheque.set("emisorCuit", item.cesiones.get(indice).cedente_documento);
                    cheque.set("emisorRazonSocial", item.cesiones.get(indice).cedente_nombre);
                    cheque.set("beneficiarioRazonSocial", item.cesiones.get(indice).cesionario_nombre);
                    cheque.set("beneficiarioCuit", item.cesiones.get(indice).cesionario_documento);
                    cheque.set("accionRecepcion","CESION");
                }
                else if (accionRecepcion.equals(EnumAccionesEcheqOB.EMISION)){
                    cheque.set("emisorCuit", item.cuenta_emisora.emisor_cuit);
                    cheque.set("emisorRazonSocial", item.cuenta_emisora.emisor_razon_social);
                    cheque.set("beneficiarioRazonSocial", item.emitido_a.beneficiario_nombre);
                    cheque.set("beneficiarioCuit", item.emitido_a.beneficiario_documento);
                    cheque.set("accionRecepcion","EMISION");
                }
            }  else {
                cheque.set("emisorCuit", item.cuenta_emisora.emisor_cuit);
                cheque.set("emisorRazonSocial", item.cuenta_emisora.emisor_razon_social);
                cheque.set("beneficiarioRazonSocial", item.emitido_a.beneficiario_nombre);
                cheque.set("beneficiarioCuit", item.emitido_a.beneficiario_documento);
            }
            cheque.set("emisorOriginalCuit",item.cuenta_emisora.emisor_cuit);
            cheque.set("emisorOriginal",item.cuenta_emisora.emisor_razon_social);
            cheque.set("cbuCustodia",item.cbu_custodia);
            if (accion.equalsIgnoreCase("emitidos")) {
                if (item.cheque_motivo_pago!=null){
                    cheque.set("referencia",item.cheque_motivo_pago);
                }
                 }
            List<EcheqOB> chequeEnBandejaPorId = chequesEnBandeja.stream().filter(cheq -> item.cheque_id.equals(cheq.idCheque)).toList();
            if (chequeEnBandejaPorId.isEmpty()){
                try {
                    Object acciones = accionesPosibles(contexto, item, accion);
                    Objeto lista = (Objeto) acciones;
                    if (lista.existe(ACCION)) {
                        Objeto a = (Objeto) lista.get(ACCION);
                        for (Object elemento : a.toList()) {
                            cheque.add("acciones", elemento);
                        }
                    }
                    if (lista.existe("ESTADO")){
                        cheque.set("estado",lista.get("ESTADO"));
                    }

                } catch (Exception e) {
                    respuesta.set(ERROR, e.toString());
                }
            } else{
                cheque.set("estado",chequeEnBandejaPorId.get(0).estadoBandeja.descripcion);
            }
            try{
                if(chequeBase!=null){
                    cheque.set("firmas", OBFirmas.obtenerDatosFirmantes(contexto, chequeBase));
                }
            }catch (Exception e){}
            respuesta.add("cheque", cheque);
        });
        LogOB.evento(contexto,"listadoCheques", "Ok");
        return respuesta("0", DATOS, respuesta);
    }

    public static Object listadoChequesADescontar(ContextoOB contexto) {
        Parametros parametros = contexto.parametros;

        String pagina = parametros.string("paginacion.numeroPagina");
        String ordenamiento = parametros.string("paginacion.ordenamiento");
        Integer totalElementos = Integer.valueOf(parametros.string("paginacion.totalElementos"));
        Integer moneda = Integer.valueOf(parametros.string("parametros.moneda"));

        String fechaDesde = parametros.string("parametros.fechaSolicitadaDesde");
        String fechaHasta = parametros.string("parametros.fechaSolicitadaHasta");

        ListadoChequesADescontarOB listado = ApiCheques.listadoChequesADescontar(contexto, pagina, moneda, ordenamiento, totalElementos, fechaDesde, fechaHasta).get();

        Objeto respuesta = new Objeto();

        // Mapear el contenido del bodyResponse de la API a la respuesta
        if (listado != null ) {
        	Objeto resultObj = new Objeto();

            Objeto cliente = new Objeto();
            cliente.set("tipoDocumento", listado.cliente.tipoDocumento);
            cliente.set("numeroDocumento", listado.cliente.numeroDocumento);

            Objeto paginacion = new Objeto();
            paginacion.set("numeroPagina", listado.paginacion.numeroPagina);
            paginacion.set("ordenamiento", listado.paginacion.ordenamiento);
            paginacion.set("totalElementos", listado.paginacion.totalElementos);


            boolean habilitadoParaDescuento = horarioDescuento(contexto);

            Objeto resultado = new Objeto();

            if (listado.resultado.cheques != null && !listado.resultado.cheques.isEmpty()) {

            	listado.resultado.cheques.forEach(cheque -> {
	                Objeto chequeObj = new Objeto();
	                chequeObj.set("chequeId", cheque.chequeId);
	                chequeObj.set("moneda", cheque.moneda);
	                chequeObj.set("chequeCMC7", cheque.chequeCMC7);
	                chequeObj.set("chequeBancoGiradoCodigo", cheque.chequeBancoGiradoCodigo);
	                chequeObj.set("chequeBancoGiradoDescripcion", cheque.chequeBancoGiradoDescripcion);
	                chequeObj.set("chequeSucursalGirada", cheque.chequeSucursalGirada);
	                chequeObj.set("chequeCodigoPostalGirado", cheque.chequeCodigoPostalGirado);
	                chequeObj.set("chequeNumero", cheque.chequeNumero);
	                chequeObj.set("chequeCuentaGirada", cheque.chequeCuentaGirada);
	                chequeObj.set("chequeImporte", cheque.chequeImporte);
	                chequeObj.set("chequeFechaPresentacion", cheque.chequeFechaPresentacion);
	                chequeObj.set("libradorTipoDocumento", cheque.libradorTipoDocumento);
	                chequeObj.set("libradorNumeroDocumento", cheque.libradorNumeroDocumento);
	                chequeObj.set("libradorRazonSocial", cheque.libradorRazonSocial);
	                chequeObj.set("linea", cheque.linea);
                    chequeObj.set("habilitadoParaDescuento", habilitadoParaDescuento);

                    resultado.add("cheques", chequeObj);
        		});
        	} else {
                resultado.set("cheques", new ArrayList<>());
        	}

            resultObj.set("cliente", cliente);
            resultObj.set("paginacion", paginacion);
            resultObj.set("resultado", resultado);

            // Asignar resultObj al objeto de respuesta final
            respuesta.set("result", resultObj);
        }
        LogOB.evento(contexto, "listadoChequesADescontar", "FIN OK");
        return respuesta;
    }


    public static Object simularDescuento(ContextoOB contexto) {

        // Verificar horario
        if (!horarioDescuento(contexto)) {
            LogOB.evento(contexto, "simularDescuento", "ERROR: Intento de descuento fuera del horario permitido");
            return respuesta("ERROR: Descuento no permitido en este horario");
        }

        String sucursal = contexto.parametros.string("sucursal");
        String clienteCuenta = contexto.parametros.string("cuenta");
        String parametrosChequesId = contexto.parametros.string("chequesId");
        parametrosChequesId = parametrosChequesId.replace("[", "").replace("]", "").replace("'", "").replace("\n", "").replace("\"", "");
        parametrosChequesId.trim();
        String[] chequesArray = parametrosChequesId.split(",");

        SimularDescuentoOB simulacion = null;
    	try {
    		simulacion = ApiCheques.simularDescuento(contexto, clienteCuenta, chequesArray, sucursal).get();
    	} catch (ApiException e) {
    		LogOB.evento(contexto, "simularDescuento",e.response.body);
			return respuesta("ERROR");
    	}

    	Objeto respuesta = new Objeto();
    	respuesta.set("estado", "0");
    	respuesta.add("datos", simulacion);
    	return respuesta;
    }

    public static Boolean horarioDescuento(ContextoOB contexto) {
        // Validar horario
        LocalTime now = LocalTime.now();
        LocalTime inicioPermitido = LocalTime.of(00, 30);
        LocalTime finPermitido = LocalTime.of(16, 00);


        if (!(now.isAfter(inicioPermitido) && now.isBefore(finPermitido))) {
            LogOB.evento(contexto, "simularDescuento", "ERROR: Intento de descuento fuera del horario permitido");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    public static Object descuentoHabilitado(ContextoOB contexto) {
        Objeto respuesta = new Objeto();
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        Set<String> cuitsHabilitados = Set.of(
                "27295443079",
                "30504005085",
                "30716196832",
                "30551497492",
                "30714385719",
                "30716085240"
        );
        boolean habilitado = cuitsHabilitados.contains(cuit);
        respuesta.set("estado", "0");
        respuesta.set("habilitado", habilitado);
        return respuesta;
    }

    public static Object eliminarDescuentoFactoring(ContextoOB contexto) {

        String parametrosSolicitudNumero = contexto.parametros.string("solicitudNumero");

        DescontarChequeOB descontarCheque = null;
    	try {
    		descontarCheque = ApiCheques.descontarChequeFactoring(contexto, ConstantesOB.FACTORING_DESCUENTO_ESTADO_ELIMINAR, parametrosSolicitudNumero).get();
    	} catch (ApiException e) {
    		LogOB.evento(contexto, "eliminarDescuentoFactoring",e.response.body);
			return respuesta("ERROR");
    	}

    	Objeto respuesta = new Objeto();
    	respuesta.set("estado", "0");
    	respuesta.add("datos", descontarCheque);
    	return respuesta;
    }

    private static Object mapearMensajeRechazo(ListadoChequesOB.cheques item, String accion) {
        if (accion.equalsIgnoreCase("recibidos")){
            if ((item.rechazos.get(0).codigo_rechazo.equalsIgnoreCase("R010")||item.rechazos.get(0).codigo_rechazo.equalsIgnoreCase("R10"))){
                Objeto rechazo = new Objeto();
                rechazo.set("titulo","Rechazado por falta de fondos");
                rechazo.set("subtitulo","Por favor, resolvé tu situación con quien te emitió o endosó este eCheq y devolvelo.");
                return rechazo;
            }
            if ((item.rechazos.get(0).codigo_rechazo.equalsIgnoreCase("R093")||item.rechazos.get(0).codigo_rechazo.equalsIgnoreCase("R93"))){
                Objeto rechazo = new Objeto();
                rechazo.set("titulo","No pudimos depositar este eCheq");
                rechazo.set("subtitulo","Por favor comunicáte con tu ejecutivo de cuentas para reintentar el deposito de este eCheq.");
                return rechazo;
            }

        }

        if (accion.equalsIgnoreCase("emitidos")){
            if (item.estado.equalsIgnoreCase("rechazado")){
                if ((item.rechazos.get(0).codigo_rechazo.equalsIgnoreCase("R010")||item.rechazos.get(0).codigo_rechazo.equalsIgnoreCase("R10"))&&!item.certificado_emitido&&!item.cheque_acordado){
                    Objeto rechazo = new Objeto();
                    rechazo.set("titulo","Rechazado por falta de fondos");
                    rechazo.set("subtitulo","Por favor, resolvé tu situación con quien posea el eCheq y solicitá la devolución.");
                    return rechazo;
                } else if ((item.rechazos.get(0).codigo_rechazo.equalsIgnoreCase("R010")||item.rechazos.get(0).codigo_rechazo.equalsIgnoreCase("R10"))&&item.certificado_emitido){
                    Objeto rechazo = new Objeto();
                    rechazo.set("titulo","Se presentaron acciones civiles");
                    rechazo.set("subtitulo","El beneficiario de este eCheq presento un certificado de acciones civiles.");
                    return rechazo;
                }
                else if ((item.rechazos.get(0).codigo_rechazo.equalsIgnoreCase("R010")||item.rechazos.get(0).codigo_rechazo.equalsIgnoreCase("R10"))&&item.cheque_acordado){
                    Objeto rechazo = new Objeto();
                    rechazo.set("titulo","Este eCheq fue acordado");
                    rechazo.set("subtitulo","Fue rechazado por falta de fondo, y el beneficiario lo devolvió");
                    return rechazo;
                }
                if ((item.rechazos.get(0).codigo_rechazo.equalsIgnoreCase("R093")||item.rechazos.get(0).codigo_rechazo.equalsIgnoreCase("R93"))){
                    Objeto rechazo = new Objeto();
                    rechazo.set("titulo","El banco depositante no se encuentra disponible");
                    rechazo.set("subtitulo","Quién posea el eCheq tendrá que comunicarse con su banco para intentar el deposito nuevamente.");
                    return rechazo;
                }
            }
            if (item.estado.equalsIgnoreCase("devolucion-pendiente")){
                if (item.rechazos!=null){
                    if ((item.rechazos.get(0).codigo_rechazo.equalsIgnoreCase("R010")||item.rechazos.get(0).codigo_rechazo.equalsIgnoreCase("R10"))&&item.solicitando_acuerdo){
                        Objeto rechazo = new Objeto();
                        rechazo.set("titulo","Rechazado por falta de fondos");
                        rechazo.set("subtitulo","Por favor, resolvé tu situación con quien posea el eCheq y solicitá la devolución.");
                        return rechazo;
                    }
                }
            }

        }

        if (accion.equalsIgnoreCase("endosados")){
            if (item.estado.equalsIgnoreCase("rechazado")){
                if ((item.rechazos.get(0).codigo_rechazo.equalsIgnoreCase("R010")||item.rechazos.get(0).codigo_rechazo.equalsIgnoreCase("R10"))&&item.cheque_acordado){
                    Objeto rechazo = new Objeto();
                    rechazo.set("titulo","Este eCheq fue acordado");
                    rechazo.set("subtitulo","Fue rechazado por falta de fondo, y el beneficiario lo devolvió");
                    return rechazo;
                }
                else if ((item.rechazos.get(0).codigo_rechazo.equalsIgnoreCase("R010")||item.rechazos.get(0).codigo_rechazo.equalsIgnoreCase("R10"))&&item.certificado_emitido){
                    Objeto rechazo = new Objeto();
                    rechazo.set("titulo","Se presentaron acciones civiles");
                    rechazo.set("subtitulo","El beneficiario de este eCheq presento un certificado de acciones civiles.");
                    return rechazo;
                }
                if ((item.rechazos.get(0).codigo_rechazo.equalsIgnoreCase("R093")||item.rechazos.get(0).codigo_rechazo.equalsIgnoreCase("R93"))){
                    Objeto rechazo = new Objeto();
                    rechazo.set("titulo","El banco depositante no se encuentra disponible");
                    rechazo.set("subtitulo","Quién posea el eCheq tendrá que comunicarse con su banco para intentar el deposito nuevamente.");
                    return rechazo;
                }
            }
            if (item.estado.equalsIgnoreCase("devolucion-pendiente")){
                if (item.rechazos!=null){
                    if ((item.rechazos.get(0).codigo_rechazo.equalsIgnoreCase("R010")||item.rechazos.get(0).codigo_rechazo.equalsIgnoreCase("R10"))&&item.solicitando_acuerdo){
                        Objeto rechazo = new Objeto();
                        rechazo.set("titulo","Rechazado por falta de fondos");
                        rechazo.set("subtitulo","Por favor, resolvé tu situación con quien posea el eCheq y solicitá la devolución.");
                        return rechazo;
                    }
                }
            }

        }
        return null;
    }

    private static int obtenerIndiceEndosoRecepcion(List<ListadoChequesOB.endoso> endosos,String cuitConsultante){
        boolean encontrado = false;
        int respuesta = 0;
        int indice = 0;
        while (!encontrado&&indice<endosos.size()){
            if (endosos.get(indice).benef_documento.equals(cuitConsultante)&&endosos.get(indice).estado_endoso.equalsIgnoreCase("aceptado")){
                encontrado=true;
                respuesta = indice;
            }
            indice++;
        }
        return respuesta;
    }
    private static int obtenerIndiceCesionRecepcion(List<ListadoChequesOB.cesion> cesiones,String cuitConsultante){
        boolean encontrado = false;
        int respuesta = 0;
        int indice = 0;
        while (!encontrado&&indice<cesiones.size()){
            if (cesiones.get(indice).cesionario_documento.equals(cuitConsultante)&&cesiones.get(indice).estado_cesion.equalsIgnoreCase("aceptada")){
                encontrado=true;
                respuesta = indice;
            }
            indice++;
        }
        return respuesta;
    }

    private static EnumAccionesEcheqOB mapearMetodoRecepcion(ListadoChequesOB.cheques cheque,String cuitConsultante) {
        EnumAccionesEcheqOB accionRecepcion = null;
        EnumAccionesEcheqOB accionSalida = huboSalida(cheque.cesiones,cheque.endosos,cuitConsultante);
        boolean recibiPorCesion = recibiPorCesion(cheque.cesiones,cuitConsultante);
        boolean recibiPorEndoso = recibiPorEndoso(cheque.endosos,cuitConsultante);
        //Caso emitido
        if (cheque.emitido_a.beneficiario_documento.equals(cuitConsultante)){
            if (cheque.endosos==null&&cheque.cesiones==null){
                accionRecepcion =  EnumAccionesEcheqOB.EMISION;
            } else {
                if (cheque.endosos!=null&&cheque.cesiones==null) {
                    if (!recibiPorEndoso){
                        accionRecepcion =  EnumAccionesEcheqOB.EMISION;
                    }
                }else {
                    if(cheque.endosos!=null&&cheque.cesiones!=null){
                        if (!recibiPorEndoso&&!recibiPorCesion){
                            accionRecepcion =  EnumAccionesEcheqOB.EMISION;
                        }
                    } else if (cheque.cesiones!=null){
                        if (!recibiPorCesion){
                            accionRecepcion =  EnumAccionesEcheqOB.EMISION;
                        }
                    }
                }
            }
        }

        //Caso endosado
        if (accionRecepcion==null && cheque.endosos!=null&&!accionSalida.equals(EnumAccionesEcheqOB.ENDOSO)){

            if (cheque.cesiones==null&&recibiPorEndoso){
                accionRecepcion = EnumAccionesEcheqOB.ENDOSO;
            }
            if (cheque.cesiones!=null){
                if (!recibiPorCesion){
                    accionRecepcion = EnumAccionesEcheqOB.ENDOSO;
                }
            }
        }
        //Caso cedido
        if (accionRecepcion==null && cheque.cesiones!=null&&!accionSalida.equals(EnumAccionesEcheqOB.CESION)){
            if (recibiPorCesion){
                accionRecepcion = EnumAccionesEcheqOB.CESION;
            }
        }


        return accionRecepcion==null? EnumAccionesEcheqOB.EMISION : accionRecepcion;
    }

    private static boolean recibiPorEndoso(List<ListadoChequesOB.endoso> endosos,String cuitConsultante){
        boolean recibiEndoso = false;
        if (endosos!=null){
            int i = 0;
            while (i<endosos.size()&&!recibiEndoso){
                if (endosos.get(i).benef_documento.equals(cuitConsultante)&&(endosos.get(i).estado_endoso.equals("aceptado")||endosos.get(i).estado_endoso.equals("pendiente"))){
                    recibiEndoso = true;
                }
                i++;
            }
        }
        return recibiEndoso;
    }
    private static boolean recibiPorCesion(List<ListadoChequesOB.cesion> cesiones, String cuitConsultante) {
        boolean recibiCesion = false;
        if (cesiones!=null){
            int i = 0;
            while (i<cesiones.size()&&!recibiCesion){
                if (cesiones.get(i).cesionario_documento.equals(cuitConsultante)&&(cesiones.get(i).estado_cesion.equals("aceptada")||cesiones.get(i).estado_cesion.equals("pendiente"))){
                    recibiCesion = true;
                }
                i++;
            }
        }
        return recibiCesion;
    }
    private static EnumAccionesEcheqOB huboSalida (List<ListadoChequesOB.cesion> cesiones, List<ListadoChequesOB.endoso> endosos,String cuitConsultante){
        EnumAccionesEcheqOB accionSalida = EnumAccionesEcheqOB.EMISION;
        if (cesiones!=null){
            ListadoChequesOB.cesion cesion = cesiones.get(0);
            if (cesion.cedente_documento.equals(cuitConsultante)&&cesion.estado_cesion.equalsIgnoreCase("aceptada")&&!cesion.cesionario_documento.equals(cuitConsultante)){
                accionSalida = EnumAccionesEcheqOB.CESION;
            }
        }
        if (endosos!=null){
            ListadoChequesOB.endoso endoso = endosos.get(0);
            if (endoso.emisor_documento.equals(cuitConsultante)&&endoso.estado_endoso.equalsIgnoreCase("aceptado")&&!endoso.benef_documento.equals(cuitConsultante)){
                accionSalida = EnumAccionesEcheqOB.ENDOSO;
            }
        }
        return accionSalida;
    }

    public static Object detalleCheque(ContextoOB contexto) {
        LogOB.evento(contexto,"detalleCheque", "INICIO");
        String idCheque = contexto.parametros.string(ID_CHEQUE);
        String accion = contexto.parametros.string(ACCION,null);
        String cuit = contexto.sesion().empresaOB.cuit.toString();

        ListadoChequesOB listado = ApiCheques.getChequeById(contexto, idCheque, cuit).get();
        LogOB.evento(contexto,"detalleCheque", "apiCheques.getchequebyId");

        Objeto respuesta = new Objeto();

        ServicioEcheqOB servicioEcheqOB = new ServicioEcheqOB(contexto);

        if (listado.result.cheques.isEmpty()) {
            respuesta.add(DATOS, listado.result);
            return respuesta("0", DATOS, "No hay datos");
        }
        ListadoChequesOB.cheques cheq = listado.result.cheques.get(0);
        EcheqOB chequeBase = servicioEcheqOB.findByFieldAndDocumento("idCheque", cheq.emitido_a.beneficiario_documento,cuit).get();
        Objeto cheque = new Objeto();
        List<EcheqOB> echeqsEnBandeja = servicioEcheqOB.findChequeByEmisorAndIdAndEstado(contexto.sesion().empresaOB.cuit.toString()).get();
        cheque.set("importe", cheq.monto);
        cheque.set("estado", echeqsEnBandeja.size()==0? cheq.estado:echeqsEnBandeja.get(0).estadoBandeja.descripcion);
        cheque.set("numeroCheque", cheq.cheque_numero);
        cheque.set("numeroChequera", cheq.numero_chequera);
        cheque.set("emailBeneficiario", chequeBase == null ? null : chequeBase.emailBeneficiario);
        cheque.set(ID_CHEQUE, cheq.cheque_id);
        cheque.set("caracter", cheq.cheque_caracter);
        cheque.set("tipo", cheq.cheque_tipo);
        cheque.set("fechaEmision", cheq.fecha_emision.substring(0, cheq.fecha_emision.indexOf('T')));
        cheque.set(FECHA_PAGO, cheq.fecha_pago.substring(0, cheq.fecha_pago.indexOf('T')));
        cheque.set("bancoEmisor", cheq.cuenta_emisora.banco_nombre);
        cheque.set("codigoBanco", cheq.cuenta_emisora.banco_codigo);
        cheque.set("codigoSucursal", cheq.cuenta_emisora.sucursal_codigo);
        cheque.set("codigoPostalSucursal", cheq.cuenta_emisora.sucursal_cp);
        cheque.set("cuentaOrigen", "3"+cheq.cuenta_emisora.sucursal_codigo.substring(cheq.cuenta_emisora.sucursal_codigo.length()-3)+cheq.cuenta_emisora.emisor_cuenta);
        if (accion!=null){
            if (cheq.estado.equalsIgnoreCase("rechazado")||(cheq.estado.equalsIgnoreCase("devolucion-pendiente")&&cheq.rechazos!=null)){
                cheque.set("detalleRechazo",mapearMensajeRechazo(cheq,accion));
                if (cheque.get("detalleRechazo")==null){
                    cheque.del("detalleRechazo");
                }
            }
            if (cheq.cesiones!=null&&accion.equalsIgnoreCase("cesion")){
            cheque.set("emisorRazonSocial", cheq.cesiones.get(0).cedente_nombre);
            cheque.set("emisorCuit", cheq.cesiones.get(0).cedente_documento);
            cheque.set("beneficiarioRazonSocial", cheq.cesiones.get(0).cesionario_nombre);
            cheque.set("beneficiarioCuit", cheq.cesiones.get(0).cesionario_documento);
        } else if (cheq.endosos!=null&&accion.equalsIgnoreCase("endoso")){
            cheque.set("emisorRazonSocial",cheq.endosos.get(0).emisor_razon_social);
            cheque.set("emisorCuit", cheq.endosos.get(0).emisor_documento);
            cheque.set("beneficiarioRazonSocial", cheq.endosos.get(0).benef_razon_social);
            cheque.set("beneficiarioCuit", cheq.endosos.get(0).benef_documento);
        } else {
            cheque.set("emisorRazonSocial", cheq.cuenta_emisora.emisor_razon_social);
            cheque.set("emisorCuit", cheq.cuenta_emisora.emisor_cuit);
            cheque.set("beneficiarioRazonSocial", cheq.emitido_a.beneficiario_nombre);
            cheque.set("beneficiarioCuit", cheq.emitido_a.beneficiario_documento);
        }
        } else {
            cheque.set("emisorRazonSocial", cheq.cuenta_emisora.emisor_razon_social);
            cheque.set("emisorCuit", cheq.cuenta_emisora.emisor_cuit);
            cheque.set("beneficiarioRazonSocial", cheq.emitido_a.beneficiario_nombre);
            cheque.set("beneficiarioCuit", cheq.emitido_a.beneficiario_documento);
        }
        cheque.set("emisorOriginalCuit",cheq.cuenta_emisora.emisor_cuit);
        cheque.set("emisorOriginal",cheq.cuenta_emisora.emisor_razon_social);
        if (accion!=null&&accion.equalsIgnoreCase("emitidos")){
            if (cheq.cheque_motivo_pago!=null){
                cheque.set("referencia",cheq.cheque_motivo_pago);
            }
        }


        LogOB.evento(contexto,"detalleCheque", "FIN");
        return respuesta("0", DATOS, cheque);

    }

    private static Objeto admitirEcheq(ContextoOB contexto, ListadoChequesOB.cheques cheque) {
        LogOB.evento(contexto,"admitirEcheq", "INICIO");

        String idCheque = contexto.parametros.string(ID_CHEQUE);
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        Objeto respuesta = new Objeto();
        if (cheque.estado.equals(EMITIDO_PENDIENTE)) {
            try {
                AdmitirEcheqOB admisionEcheq = ApiCheques.admitirEcheqOb(contexto, idCheque, cuit, cheque.emitido_a.beneficiario_documento_tipo).get();
                respuesta.add(RESPUESTA, admisionEcheq.result);
                respuesta.set("estado", "0");
            } catch (ApiException e) {
                respuesta.set(ERROR, true);
                respuesta.set(DATOS, e.response.get("mensajeAlUsuario"));
            }
        }
        if (cheque.estado.equals(ACTIVO_PENDIENTE)) {
            if (cheque.endosos.isEmpty()) {
                return respuesta(DATOS_INVALIDOS);
            } else {
                try {
                    AdmitirEcheqOB admisionEcheq = ApiCheques.admitirEcheqOb(contexto, idCheque, cuit, "cuit").get();
                    respuesta.add(RESPUESTA, admisionEcheq.result);
                    respuesta.set("estado", "0");
                } catch (ApiException e) {
                    respuesta.set(ERROR, true);
                    respuesta.set(DATOS, e.response.get("mensajeAlUsuario"));
                }
            }
        }
        LogOB.evento(contexto,"admitirEcheq", "FIN");
        return respuesta;
    }

    private static Objeto repudiarEcheq(ContextoOB contexto, ListadoChequesOB.cheques cheque) {
        LogOB.evento(contexto,"repudiarEcheq", "INICIO");
        String idCheque = contexto.parametros.string(ID_CHEQUE);
        String motivoRepudio = contexto.parametros.string("motivoRepudio");
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        Objeto respuesta = new Objeto();
        if (cheque.estado.equalsIgnoreCase(EMITIDO_PENDIENTE)) {
            try {
                RechazarEcheqOB rechazoEcheq = ApiCheques.rechazarEcheqOB(contexto, idCheque, cuit, cheque.emitido_a.beneficiario_documento_tipo, motivoRepudio).get();
                respuesta.add(RESPUESTA, rechazoEcheq.result);
                respuesta.set("estado", "0");
                LogOB.evento(contexto,"repudiarEcheq", "Rechazado");
            } catch (ApiException e) {
                respuesta.set(ERROR, true);
                respuesta.set(DATOS, e.response.get("mensajeAlUsuario"));
            }
        } else if (cheque.estado.equalsIgnoreCase(ACTIVO_PENDIENTE)) {
            try {
                RechazarEcheqOB rechazoEcheq = ApiCheques.rechazarEcheqOB(contexto, idCheque, cuit, "cuit", motivoRepudio).get();
                respuesta.add(RESPUESTA, rechazoEcheq.result);
                respuesta.set("estado", "0");
                LogOB.evento(contexto,"repudiarEcheq", "Rechazado");
            } catch (ApiException e) {
                respuesta.set(ERROR, true);
                respuesta.set(DATOS, e.response.get("mensajeAlUsuario"));
            }
        }
        LogOB.evento(contexto,"repudiarEcheq", "FIN");
        return respuesta;
    }

    private static Objeto anularEcheq(ContextoOB contexto) {
        LogOB.evento(contexto,"anularEcheq", "INICIO");

        String idCheque = contexto.parametros.string(ID_CHEQUE);
        String motivoAnulacion = " ";
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        Objeto respuesta = new Objeto();
        try {
            AnularEcheqOB anulacionEcheq = ApiCheques.anularEcheqOB(contexto, idCheque, cuit, motivoAnulacion).get();
            respuesta.set(RESPUESTA, anulacionEcheq.result);
            respuesta.set("estado", "0");
            LogOB.evento(contexto,"anularEcheq", "Anulado");

        } catch (ApiException e) {
            respuesta.set(ERROR, true);
            respuesta.set(DATOS, e.response.get("mensajeAlUsuario"));
        }
        LogOB.evento(contexto, "anularEcheq", "FIN");
        return respuesta;
    }

    public static Object precargaDevolucionEcheq(ContextoOB contexto){
        LogOB.evento(contexto,"precargaDevolucionEcheq", "INICIO");

        String idCheque = contexto.parametros.string(ID_CHEQUE);
        String tipoDevolucion = contexto.parametros.string("tipoDevolucion");
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        ListadoChequesOB listado = ApiCheques.getChequeById(contexto, idCheque, cuit).get();
        LogOB.evento(contexto,"precargaDevolucionEcheq", "Cheque traido por Id");

        ListadoChequesOB.cheques cheque = listado.result.cheques.get(0);
        ServicioEcheqOB servicioEcheqOB = new ServicioEcheqOB(contexto);
        if (!tipoDevolucion.equals("emisor")&&!tipoDevolucion.equals("endosante")){
            return new ErrorGenericoOB().setErrores("Parametro incorrecto", "Tipo de devolucion debe ser 'emisor' o 'endosante'");
        }
        if (listado.result.cheques.isEmpty()) {

            return new ErrorGenericoOB().setErrores("Cheque inexistente", "El cheque ingresado no existe");
        }
        if (!cheque.estado.equalsIgnoreCase("ACTIVO") && !cheque.estado.equalsIgnoreCase("RECHAZADO")) {

            return new ErrorGenericoOB().setErrores("Estado del cheque incorrecto", "El cheque posee un estado donde no se puede solicitar una devolucion");
        }
        if (tipoDevolucion.equals("emisor")){
            if (!cuit.equals(cheque.cuenta_emisora.emisor_cuit)&&cheque.avalistas!=null&&cuit.equals(cheque.avalistas.get(0).aval_documento)){
                tipoDevolucion = "avalista";
            }
        }
        EnumAccionesEcheqOB tipoDevol;
        try{
            tipoDevol = switch (tipoDevolucion){
                case "emisor"-> EnumAccionesEcheqOB.SOLICITAR_DEVOLUCION_EMISOR;
                case "avalista"-> EnumAccionesEcheqOB.SOLICITAR_DEVOLUCION_AVALISTA;
                case "endosante"-> EnumAccionesEcheqOB.SOLICITAR_DEVOLUCION_ENDOSANTE;
                case "mandatario"->EnumAccionesEcheqOB.SOLICITAR_DEVOLUCION_MANDATARIO;
                default ->  throw new Exception();
            };
        }catch (Exception e){
            return respuesta("DATOS_INVALIDOS");
        }

        boolean cruzado = cheque.cheque_modo.equals("0");
        boolean caracter = cheque.cheque_caracter.equals("a la orden");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
        LocalDateTime fechaPago = LocalDateTime.parse(cheque.fecha_pago, formatter);

        EcheqOB echeq = servicioEcheqOB.cargarCreado(cheque.numero_chequera, BigDecimal.valueOf(cheque.monto), cuit, "CUIT", null, caracter, cheque.cheque_motivo_pago, cheque.cheque_concepto != null ? cheque.cheque_concepto : " ", cruzado, cheque.cuenta_emisora.emisor_cuenta, contexto.sesion().empresaOB.razonSocial, contexto.sesion().empresaOB, fechaPago, cheque.cheque_tipo, tipoDevol, contexto.sesion().empresaOB.cuit.toString(), cheque.cheque_id, cheque.cheque_numero, null, null, null, null,contexto.sesion().usuarioOB).get();
        Objeto respuestaEcheq = new Objeto();
        respuestaEcheq.set("idOperacion", echeq.id);
        LogOB.evento(contexto,"precargaDevolucionEcheq", "FIN");
        return respuesta("0",DATOS,respuestaEcheq);
    }

    public static Object solicitarDevolucionEcheqEmisor(ContextoOB contexto) throws Exception {
        LogOB.evento(contexto,"solicitarDevolucionEcheqEmisor", "INICIO");
        String idOperacion = contexto.parametros.string(ID_OPERACION);
        EcheqOB echeq = ServicioEcheqOB.find(Integer.parseInt(idOperacion)).get();
        if (empty(echeq)) {
            throw new Exception();
        }
        String motivoDevolucion = " ";
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        ListadoChequesOB listado = ApiCheques.getChequeById(contexto, echeq.idCheque, cuit).get();
        if (listado.result.cheques.isEmpty()) {
            return new ErrorGenericoOB().setErrores("Cheque inexistente", "No existe el cheque ingresado");
        }
        DevolucionEcheqOB devolucionEcheqOB = ApiCheques.solicitarDevolucionEcheqOB(contexto, echeq.idCheque, listado.result.cheques.get(0).cuenta_emisora.emisor_cuit, motivoDevolucion,echeq.emailBeneficiario).get();
        LogOB.evento(contexto,"solicitarDevolucionEcheqEmisor",new Objeto().set("Devolucion Solicitada"));
        Objeto respuesta = new Objeto();
        respuesta.set(DATOS, devolucionEcheqOB.result);

        LogOB.evento(contexto,"solicitarDevolucionEcheqEmisor", "FIN");
        return respuesta("0", DATOS, respuesta);
    }

    public static Object solicitarDevolucionEcheqEndosante(ContextoOB contexto) throws Exception {
        LogOB.evento(contexto,"solicitarDevolucionEcheqEndosante", "INICIO");

        String idOperacion = contexto.parametros.string(ID_OPERACION);
        EcheqOB echeq = ServicioEcheqOB.find(Integer.parseInt(idOperacion)).get();
        if (empty(echeq)) {
            throw new Exception();
        }
        String motivoDevolucion = " ";
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        ListadoChequesOB listado = ApiCheques.getChequeById(contexto, echeq.idCheque, cuit).get();
        LogOB.evento(contexto,"solicitarDevolucionEcheqEndosante","ApiCheques.getChequeById ejecutada");

        if (listado.result.cheques.isEmpty()||listado.result.cheques.get(0).endosos.isEmpty()) {
            return new ErrorGenericoOB().setErrores("Cheque inexistente", "No existe el cheque ingresado");
        }
        if (listado.result.cheques.get(0).endosos.isEmpty()) {
            return new ErrorGenericoOB().setErrores("Sin endosos", "El cheque ingresado no cuenta con endosos");
        }
        if (!listado.result.cheques.get(0).estado.equalsIgnoreCase("RECHAZADO")&&!listado.result.cheques.get(0).es_ultimo_endosante){
            return new ErrorGenericoOB().setErrores("No se puede solicitar el endoso", "No se puede solicitar el endoso");
        }
        ListadoChequesOB.endoso endoso = listado.result.cheques.get(0).endosos.get(0);
        DevolucionEcheqOB devolucionEcheqOB = ApiCheques.solicitarDevolucionEcheqOB(contexto, echeq.idCheque, endoso.emisor_documento, motivoDevolucion,null).get();
        LogOB.evento(contexto,"solicitarDevolucionEcheqEndosante","Devolucion solicitada");
        Objeto respuesta = new Objeto();
        respuesta.set(DATOS, devolucionEcheqOB.result);
        LogOB.evento(contexto,"solicitarDevolucionEcheqEndosante", "FIN");
        return respuesta("0", DATOS, respuesta);
    }
    public static Object solicitarDevolucionEcheqAvalista(ContextoOB contexto) throws Exception {
        LogOB.evento(contexto,"solicitarDevolucionEcheqAvalista", "INICIO");

        String idOperacion = contexto.parametros.string(ID_OPERACION);
        EcheqOB echeq = ServicioEcheqOB.find(Integer.parseInt(idOperacion)).get();
        if (empty(echeq)) {
            throw new Exception();
        }
        String motivoDevolucion = " ";
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        ListadoChequesOB listado = ApiCheques.getChequeById(contexto, echeq.idCheque, cuit).get();
        LogOB.evento(contexto,"solicitarDevolucionEcheqAvalista","ApiCheques.getChequeById ejecutada");

        if (listado.result.cheques.isEmpty()) {
            return new ErrorGenericoOB().setErrores("Cheque inexistente", "No existe el cheque ingresado");
        }
        if (listado.result.cheques.get(0).avalistas.isEmpty()) {
            return new ErrorGenericoOB().setErrores("Sin avales", "El cheque ingresado no cuenta con avales");
        }

        ListadoChequesOB.avalistas aval = listado.result.cheques.get(0).avalistas.get(0);
        DevolucionEcheqOB devolucionEcheqOB = ApiCheques.solicitarDevolucionEcheqOB(contexto, echeq.idCheque, aval.aval_documento, motivoDevolucion,null).get();
        LogOB.evento(contexto,"solicitarDevolucionEcheqAvalista","Devolucion solicitada");
        Objeto respuesta = new Objeto();
        respuesta.set(DATOS, devolucionEcheqOB.result);
        LogOB.evento(contexto,"solicitarDevolucionEcheqAvalista", "FIN");
        return respuesta("0", DATOS, respuesta);
    }

    public static Object solicitarDevolucionEcheqMandatario(ContextoOB contexto) throws Exception {
        LogOB.evento(contexto,"solicitarDevolucionEcheqMandatario", "INICIO");

        String idOperacion = contexto.parametros.string(ID_OPERACION);
        EcheqOB echeq = ServicioEcheqOB.find(Integer.parseInt(idOperacion)).get();
        if (empty(echeq)) {
            throw new Exception();
        }
        String motivoDevolucion = " ";
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        ListadoChequesOB listado = ApiCheques.getChequeByIdMandatario(contexto, echeq.idCheque, cuit).get();
        LogOB.evento(contexto,"solicitarDevolucionEcheqMandatario","ApiCheques.getChequeById ejecutada");

        if (listado.result.cheques.isEmpty()) {
            return new ErrorGenericoOB().setErrores("Cheque inexistente", "No existe el cheque ingresado");
        }

        DevolucionEcheqOB devolucionEcheqOB = ApiCheques.solicitarDevolucionEcheqOB(contexto, echeq.idCheque, contexto.sesion().empresaOB.cuit.toString(), motivoDevolucion,null).get();
        LogOB.evento(contexto,"solicitarDevolucionEcheqMandatario","Devolucion solicitada");
        Objeto respuesta = new Objeto();
        respuesta.set(DATOS, devolucionEcheqOB.result);
        LogOB.evento(contexto,"solicitarDevolucionEcheqMandatario", "FIN");
        return respuesta("0", DATOS, respuesta);
    }

    public static Object solicitarAceptacionDevolucionEcheq(ContextoOB contexto){
        LogOB.evento(contexto,"solicitarAceptacionDevolucionEcheq", "INICIO");

        String idCheque = contexto.parametros.string(ID_CHEQUE);
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        ListadoChequesOB listado = ApiCheques.getChequeById(contexto, idCheque, cuit).get();
        LogOB.evento(contexto,"solicitarAceptacionDevolucionEcheq", "ApiCheques.getChequeById ejecutada");
        ListadoChequesOB.cheques cheque = listado.result.cheques.get(0);
        if (listado.result.cheques.isEmpty()) {

            return new ErrorGenericoOB().setErrores("Cheque inexistente", "El cheque ingresado no existe");
        }
        if (!cheque.estado.equalsIgnoreCase("DEVOLUCION-PENDIENTE")&&!cheque.solicitando_acuerdo) {

            return new ErrorGenericoOB().setErrores("Estado del cheque incorrecto", "El cheque posee un estado donde no se puede aceptar una devolucion");
        }

        boolean cruzado = cheque.cheque_modo.equals("0");
        boolean caracter = cheque.cheque_caracter.equals("a la orden");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
        LocalDateTime fechaPago = LocalDateTime.parse(cheque.fecha_pago, formatter);
        ServicioEcheqOB servicioEcheqOB = new ServicioEcheqOB(contexto);
        EcheqOB echeq = servicioEcheqOB.cargarCreado(cheque.numero_chequera, BigDecimal.valueOf(cheque.monto), cuit, "CUIT", null, caracter, cheque.cheque_motivo_pago, cheque.cheque_concepto != null ? cheque.cheque_concepto : " ", cruzado, cheque.cuenta_emisora.emisor_cuenta, contexto.sesion().empresaOB.razonSocial, contexto.sesion().empresaOB, fechaPago, cheque.cheque_tipo, EnumAccionesEcheqOB.ACEPTACION_DEVOLUCION, contexto.sesion().empresaOB.cuit.toString(), cheque.cheque_id, cheque.cheque_numero, null, null, null, null,contexto.sesion().usuarioOB).get();
        Objeto respuestaEcheq = new Objeto();
        respuestaEcheq.set("idOperacion", echeq.id);
        LogOB.evento(contexto,"solicitarAceptacionDevolucionEcheq", "FIN");
        return respuesta("0",DATOS,respuestaEcheq);
    }

    public static Objeto aceptarDevolucionEcheq(ContextoOB contexto) throws Exception {
        LogOB.evento(contexto,"aceptarDevolucionEcheq", "INICIO");

        String idOperacion = contexto.parametros.string(ID_OPERACION);
        EcheqOB echeq = ServicioEcheqOB.find(Integer.parseInt(idOperacion)).get();
        if (empty(echeq)) {
            throw new Exception();
        }
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        Objeto respuesta = new Objeto();
        DevolucionEcheqOB devolucionEcheqOB = ApiCheques.aceptarDevolucionEcheqOB(contexto, echeq.idCheque, cuit, "CUIT").get();
        LogOB.evento(contexto,"aceptarDevolucionEcheq", "ApiCheques.aceptarDevolucionEcheqOB ejecutada");

        respuesta.add(DATOS, devolucionEcheqOB.result);

        respuesta.set("estado", "0");
        LogOB.evento(contexto,"aceptarDevolucionEcheq", "FIN");
        return respuesta;
    }

    private static Objeto rechazarDevolucionECheq(ContextoOB contexto) {
        LogOB.evento(contexto,"rechazarDevolucionECheq", "INICIO");

        String idCheque = contexto.parametros.string(ID_CHEQUE);
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        Objeto respuesta = new Objeto();

        try {
            DevolucionEcheqOB devolucionEcheqOB = ApiCheques.rechazarDevolucionEcheqOB(contexto, idCheque, cuit, "CUIT").get();
            respuesta.add(DATOS, devolucionEcheqOB.result);
            respuesta.set("estado", "0");
            LogOB.evento(contexto,"rechazarDevolucionECheq","ApiCheques.rechazarDevolucionEcheqOB ejecutada");
        } catch (ApiException e) {
            respuesta.set(ERROR, true);
            respuesta.set(DATOS, e.response.get("mensajeAlUsuario"));
            LogOB.evento(contexto, "rechazarDevolucionECheq", new Objeto().set("error", e.getMessage()));
        }
        LogOB.evento(contexto,"rechazarDevolucionECheq", "FIN");
        return respuesta;
    }

    private static Objeto anularDevolucionEcheq(ContextoOB contexto) {
        LogOB.evento(contexto,"anularDevolucionEcheq", "INICIO");

        String idCheque = contexto.parametros.string(ID_CHEQUE);
        String motivoDevolucion = " ";
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        Objeto respuesta = new Objeto();
        try {
            DevolucionEcheqOB devolucionEcheqOB = ApiCheques.anularDevolucionEcheqOB(contexto, idCheque, cuit, motivoDevolucion).get();
            respuesta.set(DATOS, devolucionEcheqOB.result);
            respuesta.set("estado", "0");
            LogOB.evento(contexto,"anularDevolucionEcheq", "ApiCheques.anularDevolucionEcheqOB ejecutada");
        } catch (ApiException e) {
            respuesta.set(ERROR, true);
            respuesta.set(DATOS, e.response.get("mensajeAlUsuario"));
        }
        LogOB.evento(contexto,"anularDevolucionEcheq", "FIN");
        return respuesta;
    }

    public static Object solicitudEndosoEcheq(ContextoOB contexto) {
        LogOB.evento(contexto,"solicitudEndosoEcheq", "INICIO");

        List<Object> idCheques = contexto.parametros.objeto("idsCheque").toList();

        String documentoBeneficiario = contexto.parametros.string("documentoBeneficiario");
        String tipoDocumentoBeneficiario = contexto.parametros.string("tipoDocumentoBeneficiario");
        String tipoEndoso = contexto.parametros.string("tipoEndoso");
        String emailBeneficiario = contexto.parametros.string("emailBeneficiario", null);

        if (!tipoEndoso.equals("NOM") && !tipoEndoso.equals("NEG")) {
            return new ErrorGenericoOB().setErrores("Tipo de endoso inválido.", "El tipo de endoso debe ser 'NOM' o 'NEG'.");
        }

        if (!tipoDocumentoBeneficiario.equals("CUIT") && !tipoDocumentoBeneficiario.equals("CUIL")) {
            return new ErrorGenericoOB().setErrores("Tipo de documento inválido.", "El tipo de documento debe ser 'CUIT' o 'CUIL'.");
        }

        Objeto respuestaEcheq = new Objeto();
        for (Object idChequeObjeto:idCheques){
            String idCheque = idChequeObjeto.toString();
            Objeto respuestaChequeIndividual = new Objeto();
            respuestaChequeIndividual.set("idCheque",idCheque);
            ListadoChequesOB listado = null;

            try {
                listado = ApiCheques.getChequeById(contexto, idCheque, contexto.sesion().empresaOB.cuit.toString()).get();
            } catch (Exception e) {
                respuestaChequeIndividual.set("Descripcion error","No se encontraron cheques para esta consulta.");
                respuestaChequeIndividual.set("estado",ERROR);
                respuestaEcheq.add("echeqs",respuestaChequeIndividual);
                LogOB.evento(contexto,"buscar cheque por id "+idCheque,e.toString());
                continue;
            }

            if (listado.result.cheques.isEmpty()) {
                respuestaChequeIndividual.set("Descripcion error","No se encontraron cheques para esta consulta.");
                respuestaChequeIndividual.set("estado",ERROR);
                respuestaEcheq.add("echeqs",respuestaChequeIndividual);
                continue;
            }
            DetalleRazonSocialOB detalleRazonSocial = ApiCheques.razonSocial(contexto, documentoBeneficiario).get();
            LogOB.evento(contexto,"solicitudEndosoEcheq", "ApiCheques.razonSocial ejecutada");

            ListadoChequesOB.cheques cheque = listado.result.cheques.get(0);
            boolean cruzado = cheque.cheque_modo.equals("0");
            boolean caracter = cheque.cheque_caracter.equals("a la orden");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
            LocalDateTime fechaPago = LocalDateTime.parse(cheque.fecha_pago, formatter);

            if (tipoEndoso.equals("NEG")) {
                EntidadesMercadoOB entidadesMercadoOB= ApiCheques.entidadesMercado(contexto).get();
                LogOB.evento(contexto,"solicitudEndosoEcheq", "ApiCheques.entidadesMercado ejecutada");
                Optional<EntidadesMercadoOB.infraestructurasMercado> entidadMercado = entidadesMercadoOB.result.infraestructurasMercado.stream().filter(entidad -> entidad.documento.equals(documentoBeneficiario)).findFirst();
                if (entidadMercado.isEmpty()) {
                    respuestaChequeIndividual.set("estado",ERROR);
                    respuestaChequeIndividual.set("Descripcion error","No se encontraron beneficiarios con este documento.");
                    respuestaEcheq.add("echeqs",respuestaChequeIndividual);
                    continue;
                }
            }
            ServicioEcheqOB servicioEcheqOB = new ServicioEcheqOB(contexto);
            EcheqOB echeq = servicioEcheqOB.cargarCreado(cheque.numero_chequera, BigDecimal.valueOf(cheque.monto), documentoBeneficiario, tipoDocumentoBeneficiario, emailBeneficiario, caracter, cheque.cheque_motivo_pago, cheque.cheque_concepto != null ? cheque.cheque_concepto : " ", cruzado, cheque.cuenta_emisora.emisor_cuenta, detalleRazonSocial.result.beneficiario_razon_social, contexto.sesion().empresaOB, fechaPago, cheque.cheque_tipo, EnumAccionesEcheqOB.ENDOSO, contexto.sesion().empresaOB.cuit.toString(), cheque.cheque_id, cheque.cheque_numero, tipoEndoso, null, null, null,contexto.sesion().usuarioOB).get();
            respuestaChequeIndividual.set("idOperacion",echeq.id);
            respuestaChequeIndividual.set("estado","0");
            respuestaEcheq.add("echeqs",respuestaChequeIndividual);
        }

        LogOB.evento(contexto,"solicitudEndosoEcheq", "FIN");
        return respuesta("datos", respuestaEcheq);
    }


    public static Object endosarEcheq(ContextoOB contexto) throws Exception {
        LogOB.evento(contexto,"endosarEcheq", "INICIO");
        String idOperacion = contexto.parametros.string(ID_OPERACION);
        EcheqOB echeq = ServicioEcheqOB.find(Integer.parseInt(idOperacion)).get();
        if (empty(echeq)) {
            throw new Exception();
        }
        String idCheque = echeq.idCheque;
        String documentoBeneficiario = echeq.documentoBeneficiario;
        String tipoDocumentoBeneficiario = echeq.tipoDocumentoBeneficiario;
        String tipoEndoso = echeq.tipoEndoso;
        String emailBeneficiario = echeq.emailBeneficiario;
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        ListadoChequesOB listado= ApiCheques.getChequeById(contexto, idCheque, cuit).get();
        LogOB.evento(contexto,"endosarEcheq", new Objeto().set("ApiCheques.getChequeById ejecutada"));
        if (listado.result.cheques.isEmpty()) {
            return new ErrorGenericoOB().setErrores("Cheque invalido", "El cheque ingresado no existe");
        }
        String documentoEmisor = echeq.documentoEmisor;
        String tipoDocumentoEmisor = echeq.tipoDocumentoEmisor;
        EndosoEcheqOB endoso = ApiCheques.endosarEcheq(contexto, idCheque, tipoDocumentoEmisor, documentoEmisor, tipoDocumentoBeneficiario, documentoBeneficiario, tipoEndoso, emailBeneficiario).get();
        LogOB.evento(contexto,"endosarEcheq", "ApiCheques.endosarEcheq ejecutada");
        Objeto respuesta = new Objeto();
        respuesta.add(DATOS, endoso.result);

        LogOB.evento(contexto,"endosarEcheq", "FIN");
        return respuesta("0", DATOS, respuesta);

    }

    private static Object accionesPosibles(ContextoOB contexto, ListadoChequesOB.cheques cheque, String accionConsulta) throws ParseException {
        ServicioEcheqOB servicioEcheqOB = new ServicioEcheqOB(contexto);
        String cuitConsultante = contexto.sesion().empresaOB.cuit.toString();
//        List<EcheqOB> echeqsEnBandeja = servicioEcheqOB.findChequeByEmisorAndIdAndEstado(cuitConsultante).get();
        String cuitTenencia = cheque.tenencia.beneficiario_documento;
        Objeto acciones = new Objeto();
//        if (echeqsEnBandeja.size()>0){
//            return acciones.set("ESTADO",echeqsEnBandeja.get(0).estadoBandeja.descripcion);
//        }
        String estado = cheque.estado;
        SimpleDateFormat formatOrigin = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date fechaPago = formatOrigin.parse(cheque.fecha_pago);
        int indiceCesionPendiente = 0;
        if (cheque.cesiones != null) indiceCesionPendiente = hayAlgunaCesionPendiente(cheque.cesiones);

        if (!cheque.re_presentar&&!cheque.certificado_emitido){
            boolean tieneTenencia = !cuitTenencia.isBlank();
            if (estado.equalsIgnoreCase(RECHAZADO)) {
                if (!cheque.solicitando_acuerdo && !cheque.cheque_acordado && !cheque.certificado_emitido && !cuitConsultante.equalsIgnoreCase(cuitTenencia)&&!cheque.cesion_pendiente) {
                    acciones.add(ACCION, "Solicitar devolucion");
                }
                if (cheque.solicitando_acuerdo && cheque.cuit_solic_devol.equalsIgnoreCase(cuitConsultante)) {
                    if (tieneTenencia && cuitTenencia.equalsIgnoreCase(cuitConsultante)) {
                        acciones.add(ACCION, "Admitir devolucion");
                        acciones.add(ACCION, "Repudiar devolucion");
                    }
                    acciones.add(ACCION, "Anular devolucion");
                }
            }

            switch (accionConsulta) {
                case "recibidos", "RECIBIDOS" -> {
                    if (tieneTenencia) {
                        if (cuitConsultante.equalsIgnoreCase(cuitTenencia)) {
                            switch (estado) {
                                case ACTIVO, "activo" -> {
                                    acciones.add(ACCION, "Solicitar cesion");
                                    acciones.add(ACCION,"Solicitar aval");

                                }
                                case RECHAZADO, "rechazado" -> {
                                    if (!cheque.re_presentar && !cheque.certificado_emitido && !cheque.cesion_pendiente && !cheque.solicitando_acuerdo&&!cheque.cheque_acordado) {
                                        acciones.add(ACCION, "Solicitar cesion");
                                    }
                                    if (cheque.cesion_pendiente) {
                                        acciones.add(ACCION, "Anular cesion");
                                    }
                                    if (cheque.solicitando_acuerdo){
                                        acciones.add(ACCION, "Admitir devolucion");
                                        acciones.add(ACCION, "Repudiar devolucion");
                                    }
                                }
                                case CESION_PENDIENTE, "cesion-pendiente" -> acciones.add(ACCION, "Anular cesion");
                                case MANDATO_PENDIENTE -> acciones.add(ACCION,"Anular Mandato");
                                case AVAL_PENDIENTE -> acciones.add(ACCION,"Anular solicitud aval");
                            }
                        }
                    } else {
                        switch (estado) {
                            case RECHAZADO, "rechazado" -> {
                                if (cheque.cesion_pendiente) {
                                    acciones.add(ACCION, "Admitir cesion");
                                    acciones.add(ACCION, "Repudiar cesion");
                                }
                                if (cheque.avalistas!=null && cheque.avalistas.get(0).aval_documento.equals(cuitConsultante)){
                                    acciones.add(ACCION, "Solicitar devolucion");
                                }
                            }
                            case CESION_PENDIENTE, "cesion-pendiente" -> {
                                if (indiceCesionPendiente != -1) {
                                    acciones.add(ACCION, "Admitir cesion");
                                    acciones.add(ACCION, "Repudiar cesion");
                                }
                            }
                            case AVAL_PENDIENTE -> {
                                acciones.add(ACCION,"Admitir Aval");
                                acciones.add(ACCION,"Repudiar Aval");
                            }
                            case ACTIVO -> {
                                if (cheque.avalistas!=null && cheque.avalistas.get(0).aval_documento.equals(cuitConsultante)){
                                    acciones.add(ACCION, "Solicitar devolucion");
                                }
                            }
                        }
                    }
                    switch (estado) {
                        case EMITIDO_PENDIENTE, "emitido-pendiente" -> {
                            acciones.add(ACCION, "Admitir");
                            acciones.add(ACCION, "Repudiar");
                        }
                        case ACTIVO_PENDIENTE, "activo-pendiente" -> {
                            if (cuitTenencia.equalsIgnoreCase(cuitConsultante)) {
                                acciones.add(ACCION, "Admitir");
                                acciones.add(ACCION, "Repudiar");
                            }
                            if (cheque.es_ultimo_endosante) {
                                acciones.add(ACCION, "Anular endoso");
                            }
                        }
                        case ACTIVO, "activo" -> {
                            if (tieneTenencia && cuitTenencia.equalsIgnoreCase(cuitConsultante)) {
                                if (fechaPago.compareTo(new Date()) < 0) {
                                    acciones.add(ACCION, "Depositar");
                                }
                                acciones.add(ACCION, "Endosar");
                                
                                Date dosDias = DateUtils.sumarDias(new Date(),2);
                                boolean fechaPagoMayorDosDias = DateUtils.esFecha1MayorAFecha2(fechaPago,dosDias);
                                if(fechaPagoMayorDosDias) {
                                	acciones.add(ACCION, "Custodiar");	
                                }
                                acciones.add(ACCION,"Solicitar Mandato");
                            }
                            if (cuitTenencia.isBlank()&&cheque.es_ultimo_mandante){
                                acciones.add(ACCION,"Revocar Mandato");
                            }
                        }
                        case MANDATO_PENDIENTE->{
                            if (cuitTenencia.isBlank()){
                                acciones.add(ACCION, "Admitir mandato");
                                acciones.add(ACCION, "Repudiar mandato");
                            }

                        }
                        case "NEGOCIACION-PENDIENTE", "negociacion-pendiente" -> {
                            if (cheque.es_ultimo_endosante) {
                                acciones.add(ACCION, "Anular endoso");
                            }
                        }
                        case DEVOLUCION_PENDIENTE, "devolucion-pendiente" -> {
                            if (tieneTenencia && cuitTenencia.equalsIgnoreCase(cuitConsultante)) {
                                acciones.add(ACCION, "Admitir devolucion");
                                acciones.add(ACCION, "Repudiar devolucion");
                            }
                        }
                        case CUSTODIA, "custodia" -> {
                        	LogOB.evento(contexto, "rescate", "entra al case de CUSTODIA accionesPosibles()");
                            if (!cuitTenencia.isBlank() && cuitTenencia.equalsIgnoreCase(cuitConsultante)) {
                            	LocalDate hoy = LocalDate.now();
                            	Date date = Date.from(hoy.atStartOfDay(ZoneId.systemDefault()).toInstant());
                                boolean fechaPagoMayor1Dia = DateUtils.esFecha1MayorAFecha2(fechaPago,date);
                                LogOB.evento(contexto, "validacion fecha rescate" + fechaPagoMayor1Dia);

                                	acciones.add(ACCION, "Rescatar");

                            }
                        }
                    }
                }

                case "ENDOSADOS", "endosados" -> {
                    switch (estado) {
                        case ACTIVO -> {
                            if (!cheque.endosos.isEmpty()) {
                                if (cheque.cheque_caracter.equals("a la orden") && cheque.endosos.get(0).emisor_documento.equalsIgnoreCase(cuitConsultante) && !cheque.tenencia.beneficiario_documento.equalsIgnoreCase(cuitConsultante)&&cheque.es_ultimo_endosante) {
                                    acciones.add(ACCION, "Solicitar devolucion");
                                }
                            }
                        }
                        case ACTIVO_PENDIENTE, "activo-pendiente", "NEGOCIACION-PENDIENTE", "negociacion-pendiente" -> {
                            if (cheque.es_ultimo_endosante) {
                                acciones.add(ACCION, "Anular endoso");
                            }
                        }
                        case RECHAZADO ->{
                            cheque.endosos.stream().filter(endoso -> endoso.emisor_documento.equalsIgnoreCase(cuitConsultante) && endoso.estado_endoso.equalsIgnoreCase("aceptado")).findFirst().ifPresent(endo -> acciones.add(ACCION, "Solicitar devolucion"));
                        }


                    }
                }
                case "CEDIDOS","cedidos"->{
                    if (estado.equalsIgnoreCase("cesion-pendiente")){
                        acciones.add(ACCION,"Anular cesion");
                    }
                }

                case "EMITIDOS", "emitidos" -> {
                    switch (estado) {
                        case ACTIVO, "activo" -> acciones.add(ACCION, "Solicitar devolucion");
                        case EMITIDO_PENDIENTE, "emitido-pendiente" -> acciones.add(ACCION, "Anular");
                        case DEVOLUCION_PENDIENTE, "devolucion-pendiente" -> acciones.add(ACCION, "Anular devolucion");

                    }
                }
            }
        }


        return acciones;
    }

    private static int hayAlgunaCesionPendiente(List<ListadoChequesOB.cesion> cesiones) {
        for (int i = 0; i < cesiones.size(); i++) {
            if (cesiones.get(i).estado_cesion.equalsIgnoreCase("PENDIENTE")) {
                return i;
            }
        }
        return -1;
    }

    public static Object validarEntidadesMercado(ContextoOB contexto) {
        LogOB.evento(contexto,"validarEntidadesMercado", "INICIO");
        String cuit = contexto.parametros.string("cuit");
        EntidadesMercadoOB entidadesMercadoOB = ApiCheques.entidadesMercado(contexto).get();
        LogOB.evento(contexto,"validarEntidadesMercado","ApiCheques.entidadesMercado Ejecutada");
        Objeto respuesta = new Objeto();
        Optional<EntidadesMercadoOB.infraestructurasMercado> entidadMercado = entidadesMercadoOB.result.infraestructurasMercado.stream().filter(entidad -> entidad.documento.equals(cuit)).findFirst();
        respuesta.add("esEntidad", entidadMercado.isPresent());
        LogOB.evento(contexto,"validarEntidadesMercado", "FIN");
        return respuesta("0", DATOS, respuesta);
    }

    private static Objeto anularEndosoEcheq(ContextoOB contexto, ListadoChequesOB.cheques cheque) {
        LogOB.evento(contexto,"anularEndosoEcheq", "INICIO");
        String idCheque = contexto.parametros.string(ID_CHEQUE);
        String motivoAnulacion = " ";
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        List<ListadoChequesOB.endoso> endosos = cheque.endosos;
        if (endosos.get(0) == null || !endosos.get(0).emisor_documento.equalsIgnoreCase(cuit)) {

            return new ErrorGenericoOB().setErrores("Endoso invalido", "El cheque ingresado no cuenta con endosos");
        }
        Objeto respuesta = new Objeto();
        try {
            AnularEndosoEcheqOB anulacionEndoso = ApiCheques.anularEndosoEcheq(contexto, idCheque, cuit, motivoAnulacion).get();
            respuesta.set(RESPUESTA, anulacionEndoso.result);
            respuesta.set("estado", "0");
            LogOB.evento(contexto,"anularEndosoEcheq", "ApiCheques.anularEndosoEcheq Ejecutada");
        } catch (ApiException e) {
            respuesta.set(ERROR, true);
            respuesta.set(DATOS, e.response.get("mensajeAlUsuario"));
            LogOB.evento(contexto, "anularEndosoEcheq", new Objeto().set("error", e.getMessage()));
        }
        LogOB.evento(contexto,"anularEndosoEcheq", "FIN");
        return respuesta;
    }

    public static Object emitirCesionEcheq(ContextoOB contexto) throws Exception {
        LogOB.evento(contexto,"emitirCesionEcheq", "INICIO");
        String idOperacion = contexto.parametros.string(ID_OPERACION);
        EcheqOB echeq = ServicioEcheqOB.find(Integer.parseInt(idOperacion)).get();
        if (empty(echeq)) {
            throw new Exception();
        }
        String idCheque = echeq.idCheque;
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        ListadoChequesOB listado = ApiCheques.getChequeById(contexto, idCheque, cuit).get();
        LogOB.evento(contexto,"emitirCesionEcheq", "ApiCheques.getChequeById ejecutada");
        if (listado.result.cheques.isEmpty()) {
            return respuesta(DATOS_INVALIDOS);
        }
        String cesionarioDocumento = echeq.documentoBeneficiario;
        String cesionarioDocumentoTipo = echeq.tipoDocumentoBeneficiario;
        String cesionarioDomicilio = echeq.cesionarioDomicilio;
        String cesionarioNombre = echeq.cesionarioNombre;

        Objeto respuesta = new Objeto();
        EmitirCesionEcheqOB cesion= ApiCheques.emitirCesionEcheq(contexto, idCheque, cuit, cesionarioDocumento, cesionarioDocumentoTipo, cesionarioDomicilio, cesionarioNombre).get();
        LogOB.evento(contexto,"emitirCesionEcheq", "ApiCheques.emitirCesionEcheq ejecuatda");
        respuesta.add(DATOS, cesion.result);
        LogOB.evento(contexto,"emitirCesionEcheq", "FIN");
        return respuesta("0", DATOS, respuesta);
    }

    public static Object solicitudCesionEcheq(ContextoOB contexto) {
        LogOB.evento(contexto,"solicitudCesionEcheq", "INICIO");
        String idCheque = contexto.parametros.string(ID_CHEQUE);
        String cesionarioDocumento = contexto.parametros.string("cesionarioDocumento");
        String cesionarioDomicilio = contexto.parametros.string("cesionarioDomicilio");
        String cesionarioNombre = contexto.parametros.string("cesionarioNombre");
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        ListadoChequesOB listado= ApiCheques.getChequeById(contexto, idCheque, cuit).get();
        LogOB.evento(contexto,"solicitudCesionEcheq", "ApiCheques.getChequeById");
        String cesionarioDocumentoTipo = "cuit";
        if (listado.result.cheques.isEmpty()) {
            return new ErrorGenericoOB().setErrores("Cheque inexistente", "El cheque ingresado no existe");
        }
        if (cesionarioDomicilio.isBlank()){
            return new ErrorGenericoOB().setErrores("Domicilio invalido", "Debe ingresar un domicilio");
        }
        DetalleRazonSocialOB detalleRazonSocial= ApiCheques.razonSocial(contexto, cesionarioDocumento).get();
        LogOB.evento(contexto,"solicitudCesionEcheq", "ApiCheques.razonSocial EJECUTADA");
        ServicioEcheqOB servicioEcheqOB = new ServicioEcheqOB(contexto);
        ListadoChequesOB.cheques cheque = listado.result.cheques.get(0);
        boolean cruzado = cheque.cheque_modo.equals("0");
        boolean caracter = cheque.cheque_caracter.equals("a la orden");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
        LocalDateTime fechaPago = LocalDateTime.parse(cheque.fecha_pago, formatter);
        EcheqOB echeq = servicioEcheqOB.cargarCreado(cheque.numero_chequera==null?"0":cheque.numero_chequera, BigDecimal.valueOf(cheque.monto), cesionarioDocumento, cesionarioDocumentoTipo, null, caracter, cheque.cheque_motivo_pago, cheque.cheque_concepto, cruzado, cheque.cuenta_emisora.emisor_cuenta, detalleRazonSocial.result.beneficiario_razon_social, contexto.sesion().empresaOB, fechaPago, cheque.cheque_tipo, EnumAccionesEcheqOB.CESION, contexto.sesion().empresaOB.cuit.toString(), cheque.cheque_id, cheque.cheque_numero, null, cesionarioNombre, cesionarioDomicilio, null,contexto.sesion().usuarioOB).get();
        Objeto respuestaEcheq = new Objeto();
        respuestaEcheq.set("idOperacion", echeq.id);
        LogOB.evento(contexto,"solicitudCesionEcheq", "FIN");
        return respuesta("0", DATOS, respuestaEcheq);
    }

    private static Objeto anularCesionEcheq(ContextoOB contexto, ListadoChequesOB.cheques cheque) {
        LogOB.evento(contexto,"anularCesionEcheq", "INICIO");
        String idCheque = contexto.parametros.string(ID_CHEQUE);
        String cuit = contexto.sesion().empresaOB.cuit.toString();

        List<ListadoChequesOB.cesion> cesiones = cheque.cesiones;
        if (cesiones.get(0) == null || !cesiones.get(0).cedente_documento.equalsIgnoreCase(cuit)) {

            return new ErrorGenericoOB().setErrores("Cesion invalida", "El cheque ingresado no cuenta con cesiones");
        }
        String cesionId = cesiones.get(0).cesion_id;
        Objeto respuesta = new Objeto();
        try {
            AnularCesionEcheqOB anulacion = ApiCheques.anularCesionEcheq(contexto, idCheque, cuit, cesionId).get();
            respuesta.set("anulacion", anulacion.result);
            respuesta.set("estado", "0");
            LogOB.evento(contexto,"anularCesionEcheq", "anularCesionEcheq ejecutada");
        } catch (ApiException e) {
            respuesta.set(ERROR, true);
            respuesta.set(DATOS, e.response.get("mensajeAlUsuario"));
            LogOB.evento(contexto, "anularCesionEcheq", new Objeto().set("error", e.getMessage()));
        }
        LogOB.evento(contexto,"anularCesionEcheq", "FIN");
        return respuesta;
    }

    private static Objeto anularAvalEcheq(ContextoOB contexto, ListadoChequesOB.cheques cheque){
        LogOB.evento(contexto,"anularAvalEcheq", "INICIO");
        String cuit = contexto.sesion().empresaOB.cuit.toString();

        Objeto respuesta = new Objeto();
        try{
            AnularAvalEcheqOB anulacion = ApiCheques.anularAvalCheque(contexto,cheque.cheque_id,cheque.avalistas.get(0).aval_documento,cuit).get();
            respuesta.set("anulacion", anulacion.result);
            respuesta.set("estado", "0");
            LogOB.evento(contexto,"anularAvalEcheq", "anularAvalEcheq ejecutada");
        }
        catch (ApiException e) {
            respuesta.set(ERROR, true);
            respuesta.set(DATOS, e.response.get("mensajeAlUsuario"));
            LogOB.evento(contexto, "anularAvalEcheq", new Objeto().set("error", e.getMessage()));
        }
        LogOB.evento(contexto,"anularAvalEcheq", "FIN");
        return respuesta;
    }

    private static Objeto admitirCesionEcheq(ContextoOB contexto, ListadoChequesOB.cheques cheque) {
        LogOB.evento(contexto,"admitirCesionEcheq", "INICIO");
        String idCheque = contexto.parametros.string(ID_CHEQUE);
        String cuit = contexto.sesion().empresaOB.cuit.toString();

        List<ListadoChequesOB.cesion> cesiones = cheque.cesiones;
        if (cesiones.get(0) == null || !cesiones.get(0).cesionario_documento.equalsIgnoreCase(cuit)) {
            return new ErrorGenericoOB().setErrores("Cesion invalida", "El cheque ingresado no cuenta con cesiones");
        }

        Objeto respuesta = new Objeto();
        try {
            AdmitirRepudiarCesionOB admitirCesion = ApiCheques.admitirCesionEcheq(contexto, idCheque, cuit, cesiones.get(0).cesionario_documento_tipo, cesiones.get(0).cesion_id).get();
            respuesta.add("datos", admitirCesion.result);
            respuesta.set("estado", "0");
            LogOB.evento(contexto,"admitirCesionEcheq","admitirCesionEcheq Ejecutada");
        } catch (ApiException e) {
            respuesta.set(ERROR, true);
            respuesta.set(DATOS, e.response.get("mensajeAlUsuario"));
            LogOB.evento(contexto, "admitirCesionEcheq", new Objeto().set("error", e.getMessage()));
        }
        LogOB.evento(contexto,"admitirCesionEcheq", "FIN");
        return respuesta;
    }

    private static Objeto repudiarCesionEcheq(ContextoOB contexto, ListadoChequesOB.cheques cheque) {
        LogOB.evento(contexto,"repudiarCesionEcheq", "INICIO");
        String idCheque = contexto.parametros.string(ID_CHEQUE);
        String motivoRepudio = contexto.parametros.string("motivoRepudio");
        String cuit = contexto.sesion().empresaOB.cuit.toString();

        List<ListadoChequesOB.cesion> cesiones = cheque.cesiones;
        if (cesiones.get(0) == null || !cesiones.get(0).cesionario_documento.equalsIgnoreCase(cuit)) {

            return new ErrorGenericoOB().setErrores("Cesion invalida", "El cheque ingresado no cuenta con cesiones");
        }

        Objeto respuesta = new Objeto();
        try {
            AdmitirRepudiarCesionOB repudioCesion = ApiCheques.repudiarCesionEcheq(contexto, idCheque, cuit, cesiones.get(0).cesionario_documento_tipo, cesiones.get(0).cesion_id, motivoRepudio).get();
            respuesta.add("datos", repudioCesion.result);
            respuesta.set("estado", "0");
            LogOB.evento(contexto,"repudiarCesionEcheq", "repudiarCesionEcheq ejecutada");
        } catch (ApiException e) {
            respuesta.set(ERROR, true);
            respuesta.set(DATOS, e.response.get("mensajeAlUsuario"));
            LogOB.evento(contexto, "listadoCuentasCtes", new Objeto().set("error", e.getMessage()));
        }
        LogOB.evento(contexto,"repudiarCesionEcheq", "FIN");
        return respuesta;
    }


    public static Object solicitudDepositoCheque(ContextoOB contexto) {
        LogOB.evento(contexto,"solicitudDepositoCheque", "INICIO");
        String idCheque = contexto.parametros.string(ID_CHEQUE);
        String cuentaDeposito = contexto.parametros.string("cuentaDeposito");
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        String convenioReca = contexto.parametros.string("convenio",null);

        CuentasOB cuentas= ApiCuentas.cuentas(contexto, contexto.sesion().empresaOB.idCobis).get();
        LogOB.evento(contexto,"solicitudDepositoCheque", "Cargado de cuentas");
        List<CuentasOB.CuentaOB> listaCuenta = cuentas.stream().filter(c -> c.numeroProducto.equals(cuentaDeposito)).toList();
        ListadoChequesOB listado= ApiCheques.getChequeById(contexto, idCheque, cuit).get();
        LogOB.evento(contexto,"solicitudDepositoCheque", "getChequeById Ejecutada");
        if (listaCuenta.isEmpty()) {
            return new ErrorGenericoOB().setErrores("Cuenta inexistente", "La cuenta ingresada no existe");
        }
        DetalleRazonSocialOB detalleRazonSocial= ApiCheques.razonSocial(contexto, cuit).get();
        LogOB.evento(contexto,"solicitudDepositoCheque", "razonSocial ejecutada");
        ServicioEcheqOB servicioEcheqOB = new ServicioEcheqOB(contexto);
        ListadoChequesOB.cheques cheque = listado.result.cheques.get(0);
        boolean cruzado = cheque.cheque_modo.equals("0");
        boolean caracter = cheque.cheque_caracter.equals("a la orden");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
        LocalDateTime fechaPago = LocalDateTime.parse(cheque.fecha_pago, formatter);
        if (cheque.numero_chequera == null) cheque.numero_chequera = "S/N";
        EcheqOB echeq = servicioEcheqOB.cargarCreado(cheque.numero_chequera, BigDecimal.valueOf(cheque.monto), cuit, "cuit", null, caracter, cheque.cheque_motivo_pago, cheque.cheque_concepto, cruzado, cheque.cuenta_emisora.emisor_cuenta, detalleRazonSocial.result.beneficiario_razon_social, contexto.sesion().empresaOB, fechaPago, cheque.cheque_tipo, EnumAccionesEcheqOB.DEPOSITO, contexto.sesion().empresaOB.cuit.toString(), cheque.cheque_id, cheque.cheque_numero, null, null, convenioReca, cuentaDeposito,contexto.sesion().usuarioOB).get();
        Objeto respuestaEcheq = new Objeto();
        respuestaEcheq.set("idOperacion", echeq.id);
        LogOB.evento(contexto,"solicitudDepositoCheque", "FIN");
        return respuesta("0", DATOS, respuestaEcheq);
    }

    public static Object depositarCheque(ContextoOB contexto) {
        LogOB.evento(contexto,"depositarCheque", "INICIO");

        String idOperacion = contexto.parametros.string(ID_OPERACION);
        EcheqOB echeq = ServicioEcheqOB.find(Integer.parseInt(idOperacion)).get();
        CuentasOB cuentas= ApiCuentas.cuentas(contexto, contexto.sesion().empresaOB.idCobis).get();
        LogOB.evento(contexto,"depositarCheque", "Traer cuentas");
        List<CuentasOB.CuentaOB> listaCuenta = cuentas.stream().filter(c -> c.numeroProducto.equals(echeq.cuentaDeposito)).toList();
        CuentasOB.CuentaOB cuenta = listaCuenta.get(0);
        Boolean recaudaciones = false;
        Object convenios = null;
        String numeroConvenio=null;
        if (echeq.cesionarioDomicilio!=null){
            LogOB.evento(contexto, "listaconvenios", "Se ingresa en busqueda convenios reca");
            contexto.parametros.set("medioRecaudacion","T");
            convenios  = OBCobranzaIntegral.consultaConveniosDetalleHabilitacion(contexto);
            Objeto responseMap = (Objeto) convenios;
            try {
                LogOB.evento(contexto, "listaconvenios", "convenio: "+responseMap.toString());
                if(responseMap.get("estado").equals("0")){
                    Objeto lista = (Objeto) responseMap.get("datos");
                    LogOB.evento(contexto, "listaconvenios", "cantidad de convenios: "+lista.toMap().size());
                    if(lista.toList().size()>0) {
                        List<Object> listaconvenios = lista.toList();
                        LogOB.evento(contexto, "listaconvenios", listaconvenios.toString());
                        for (Object convenio : listaconvenios) {
                            Map<String, Object> c = (Map<String, Object>) convenio;
                            if (c.get("numeroProducto").equals(cuenta.numeroProducto.toString()) &&c.get("idConvenio").toString().equals(echeq.cesionarioDomicilio) && c.get("estadoEcheq").equals("S")) {
                                recaudaciones = true;
                                numeroConvenio = c.get("idConvenio").toString();
                                LogOB.evento(contexto, "listaconvenios", "El convenio enviado tiene echeq habilitado y coincide con la cuenta");
                                break;
                            }
                        }
                    }
                }
            }catch (Exception e){
                LogOB.evento(contexto, "listaconvenios", "error: "+e.getMessage());
            }
            LogOB.evento(contexto,"listaconvenios","fin convenios");
        }

        String cuit = contexto.sesion().empresaOB.cuit.toString();
        LogOB.evento(contexto,"depositarCheque","Traer sucursales");
        ListadoChequesOB listado= ApiCheques.getChequeById(contexto, echeq.idCheque, cuit).get();
        LogOB.evento(contexto,"depositarCheque", "getChequeById ejecutada");
        ListadoChequesOB.cheques cheque = listado.result.cheques.get(0);
        if (listado.result.cheques.isEmpty()) {
            
            return new ErrorGenericoOB().setErrores("Cheque inexistente", "El cheque ingresado no existe");
        }
        String idCheque = cheque.cheque_id;
        String cuentaDeposito = echeq.cuentaDeposito;
        String codigoVisualizacion = cheque.cod_visualizacion;
        String codigoBancoDeposito = cheque.cuenta_emisora.banco_codigo;
        String codigoPostal = EnumSucursalesOB.getCodigoPostalBySucursal(Integer.valueOf(cuenta.sucursal));
        String cuentaGirada = cheque.cuenta_emisora.emisor_cuenta;
        String importe = String.valueOf(cheque.monto);
        String tipo = cheque.cheque_tipo;
        String fechaPago = cheque.fecha_pago;
        String cmc7 = cheque.cmc7;
        String numeroCheque = cheque.cheque_numero;
        String modo = cheque.cheque_modo;
        String motivo = cheque.cheque_motivo_pago;
        String concepto = cheque.cheque_concepto;
        String caracter = cheque.cheque_caracter;
        String moneda = "80";
        String fechaPresentacion = LocalDateTime.now().toString();
        String usuario = cuenta.cbu;
        int producto = cuenta.tipoProducto.equals("CTE") ? 3 : 4;

        DepositarEcheqOB deposito = ApiCheques.depositoCheque(contexto,cuenta.sucursal ,codigoVisualizacion, codigoBancoDeposito, codigoPostal, cuentaGirada, importe, tipo, fechaPago, cmc7, numeroCheque, modo, motivo, concepto, caracter, moneda, fechaPresentacion, usuario, cuentaDeposito, idCheque,producto).get();
        if(recaudaciones){
            try {
                ApiRecaudaciones.relacionarEcheqConConvenio(contexto,idCheque,numeroConvenio,"ECHEQ",contexto.sesion().empresaOB.cuit.toString(),contexto.sesion().empresaOB.razonSocial.toString());
                LogOB.evento(contexto,"vincular al convenio","no rompio");
            }
            catch (ApiException e ){
                LogOB.evento(contexto,"vincular al convenio","ERROR");
            }

        }
        LogOB.evento(contexto,"depositarCheque", "Cheque depositado");
        Objeto respuesta = new Objeto();
        //respuesta.add("datos", deposito.result);
        LogOB.evento(contexto,"depositarCheque", "FIN");
        return respuesta("0", DATOS, respuesta);
    }


    public static Object repudiar(ContextoOB contexto) {
        LogOB.evento(contexto,"repudiar", "INICIO");

        String idCheque = contexto.parametros.string(ID_CHEQUE);
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        ListadoChequesOB listado= ApiCheques.getChequeById(contexto, idCheque, cuit).get();
        LogOB.evento(contexto,"repudiar", "getChequeById ejecutada");

        if (listado.result.cheques.isEmpty()) {

            return new ErrorGenericoOB().setErrores("Cheque inexistente", "El cheque ingresado no existe");
        }
        ListadoChequesOB.cheques cheque = listado.result.cheques.get(0);
        Objeto respuesta;
        switch (cheque.estado) {
            case EMITIDO_PENDIENTE, ACTIVO_PENDIENTE -> respuesta = repudiarEcheq(contexto, cheque);
            case DEVOLUCION_PENDIENTE -> respuesta = rechazarDevolucionECheq(contexto);
            case CESION_PENDIENTE -> respuesta = repudiarCesionEcheq(contexto, cheque);
            case RECHAZADO -> {respuesta = cheque.cesion_pendiente?repudiarCesionEcheq(contexto,cheque):rechazarDevolucionECheq(contexto);}
            case AVAL_PENDIENTE -> respuesta = repudiarAvalEcheq(contexto);
            case MANDATO_PENDIENTE -> respuesta = repudiarMandatoNegocicacionEcheq(contexto);
            default -> {
                return respuesta(DATOS_INVALIDOS);
            }
        }
        LogOB.evento(contexto,"repudiar", "FIN");
        return respuesta;
    }

    private static Objeto repudiarAvalEcheq(ContextoOB contexto) {
        LogOB.evento(contexto,"repudiarAvalEcheq", "INICIO");
        String idCheque = contexto.parametros.string(ID_CHEQUE);
        String motivoRepudio = contexto.parametros.string("motivoRepudio");
        String cuit = contexto.sesion().empresaOB.cuit.toString();

        Objeto respuesta = new Objeto();
        try {
            RepudioAvalEcheqOB repudioAval = ApiCheques.repudiarAvalEcheq(contexto, idCheque, cuit, motivoRepudio).get();
            respuesta.add("datos", repudioAval.result);
            respuesta.set("estado", "0");
            LogOB.evento(contexto,"repudiarAvalEcheq", "repudiarAvalEcheq ejecutada");
        } catch (ApiException e) {
            respuesta.set(ERROR, true);
            respuesta.set(DATOS, e.response.get("mensajeAlUsuario"));
            LogOB.evento(contexto, "RepudioAvalEcheq", new Objeto().set("error", e.getMessage()));
        }
        LogOB.evento(contexto,"repudiarAvalEcheq", "FIN");
        return respuesta;
    }

    private static Objeto repudiarMandatoNegocicacionEcheq(ContextoOB contexto) {
        LogOB.evento(contexto,"repudiarMandatoNegociacionEcheq","INICIO");
        String idCheque = contexto.parametros.string(ID_CHEQUE);
        String motivoRepudio = contexto.parametros.string("motivoRepudio");

        Objeto respuesta = new Objeto();
        try{
            MandatoNegociacionOB repudioMandato = ApiCheques.repudiarMandatoNegociacion(contexto,idCheque,motivoRepudio).get();
            respuesta.add("datos", repudioMandato.result);
            respuesta.set("estado", "0");
            LogOB.evento(contexto,"repudiarMandatoNegociacionEcheq", "repudiarMandatoNegociacionEcheq ejecutada");
        }catch (ApiException e){
            respuesta.set(ERROR, true);
            respuesta.set(DATOS, e.response.get("mensajeAlUsuario"));
            LogOB.evento(contexto, "repudiarMandatoNegociacionEcheq", new Objeto().set("error", e.getMessage()));
        }
        return respuesta;
    }

    public static Object admitir(ContextoOB contexto) {
        LogOB.evento(contexto,"admitir", "INICIO");

        String idCheque = contexto.parametros.string(ID_CHEQUE);
        String cuit = contexto.sesion().empresaOB.cuit.toString();

        ListadoChequesOB listado= ApiCheques.getChequeById(contexto, idCheque, cuit).get();
        LogOB.evento(contexto,"admitir", "getChequeById Ejecutada");

        if (listado.result.cheques.isEmpty()) {

            return new ErrorGenericoOB().setErrores("Cheque inexistente", "El cheque ingresado no existe");
        }
        ListadoChequesOB.cheques cheque = listado.result.cheques.get(0);
        Objeto respuesta = null;
        if (cheque.estado.equals("RECHAZADO")){
            try {
                Objeto acciones = (Objeto) accionesPosibles(contexto,cheque,"recibidos");
                if (acciones.existe(ACCION)) {
                    Objeto a = (Objeto) acciones.get(ACCION);
                    for (Object elemento : a.toList()) {
                        if (elemento.toString().equals("Admitir cesion")){
                            return respuesta = admitirCesionEcheq(contexto, cheque);
                        }
                    }
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        switch (cheque.estado) {
            case EMITIDO_PENDIENTE, ACTIVO_PENDIENTE -> respuesta = admitirEcheq(contexto, cheque);
            case CESION_PENDIENTE -> respuesta = admitirCesionEcheq(contexto, cheque);
            default -> {
                return respuesta(DATOS_INVALIDOS);
            }
        }
        LogOB.evento(contexto,"admitir", "FIN");
        return respuesta;
    }

    public static Objeto solicitarAdmisionAvalEcheq(ContextoOB contexto,ServicioEcheqOB servicioEcheqOB){
        String idCheque = contexto.parametros.string("idCheque");
        ListadoChequesOB listado;
        try {
            listado = ApiCheques.getChequeById(contexto, idCheque, contexto.sesion().empresaOB.cuit.toString()).get();
        } catch (Exception e) {
            return new ErrorGenericoOB().setErrores("Consulta inválida.", "No se encontraron cheques para esta consulta.");
        }
        if (listado.result.cheques.isEmpty()) {
            return new ErrorGenericoOB().setErrores("Consulta inválida.", "No se encontraron cheques para esta consulta.");
        }

        ListadoChequesOB.cheques cheque = listado.result.cheques.get(0);
        boolean cruzado = cheque.cheque_modo.equals("0");
        boolean caracter = cheque.cheque_caracter.equals("a la orden");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
        LocalDateTime fechaPago = LocalDateTime.parse(cheque.fecha_pago, formatter);
        EcheqOB echeq = servicioEcheqOB.cargarCreado(cheque.numero_chequera.isEmpty()?"":cheque.numero_chequera,BigDecimal.valueOf(cheque.monto),contexto.sesion().empresaOB.cuit.toString(),"cuit",null,caracter,cheque.cheque_motivo_pago,cheque.cheque_concepto,cruzado,null,"",contexto.sesion().empresaOB, fechaPago,cheque.cheque_tipo,EnumAccionesEcheqOB.ADMITIR_AVAL,cheque.cuenta_emisora.emisor_cuit,cheque.cheque_id,cheque.cheque_numero,null,null,null,null,contexto.sesion().usuarioOB).get();

        Objeto respuestaEcheq = new Objeto();
        respuestaEcheq.set("idOperacion", echeq.id);

        LogOB.evento(contexto,"SolicitudAdmisionAvalEcheq", "FIN");
        return respuesta(DATOS, respuestaEcheq);
    }


    public static Objeto admitiarAvalEcheq(ContextoOB contexto,String idCheque) {
        LogOB.evento(contexto,"admitirAvalEcheq", "INICIO");

        String cuit = contexto.sesion().empresaOB.cuit.toString();
        Objeto respuesta = new Objeto();
            try {
                AdmitirAvalEcheqOB admisionEcheq = ApiCheques.admitirAvalEcheqOb(contexto, idCheque, cuit).get();
                respuesta.add(RESPUESTA, admisionEcheq.result);
                respuesta.set("estado", "0");
            } catch (ApiException e) {
                respuesta.set(ERROR, true);
                respuesta.set(DATOS, e.response.get("mensajeAlUsuario"));
            }
        LogOB.evento(contexto,"admitirAvalEcheq", "FIN");
        return respuesta;
    }

    public static Object solicitarAnulacion(ContextoOB contexto){
        LogOB.evento(contexto,"solicitarAnulacion", "INICIO");

        String idCheque = contexto.parametros.string(ID_CHEQUE);
        String cuit = contexto.sesion().empresaOB.cuit.toString();

        ListadoChequesOB listado= ApiCheques.getChequeById(contexto, idCheque, cuit).get();
        LogOB.evento(contexto,"solicitarAnulacion","getChequeById Ejecutada");
        ListadoChequesOB.cheques cheque = listado.result.cheques.get(0);
        if (listado.result.cheques.isEmpty()) {

            return new ErrorGenericoOB().setErrores("Cheque inexistente", "El cheque ingresado no existe");
        }
        boolean cruzado = cheque.cheque_modo.equals("0");
        boolean caracter = cheque.cheque_caracter.equals("a la orden");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
        LocalDateTime fechaPago = LocalDateTime.parse(cheque.fecha_pago, formatter);
        ServicioEcheqOB servicioEcheqOB = new ServicioEcheqOB(contexto);
        EcheqOB echeq = servicioEcheqOB.cargarCreado(cheque.numero_chequera, BigDecimal.valueOf(cheque.monto), cuit, "CUIT", null, caracter, cheque.cheque_motivo_pago, cheque.cheque_concepto != null ? cheque.cheque_concepto : " ", cruzado, cheque.cuenta_emisora.emisor_cuenta, contexto.sesion().empresaOB.razonSocial, contexto.sesion().empresaOB, fechaPago, cheque.cheque_tipo, EnumAccionesEcheqOB.ANULACION, contexto.sesion().empresaOB.cuit.toString(), cheque.cheque_id, cheque.cheque_numero, null, null, null, null,contexto.sesion().usuarioOB).get();
        Objeto respuestaEcheq = new Objeto();
        respuestaEcheq.set("idOperacion", echeq.id);
        LogOB.evento(contexto,"solicitarAnulacion", "FIN");
        return respuesta("0",DATOS,respuestaEcheq);
    }

    public static Object anular(ContextoOB contexto) throws Exception {
        LogOB.evento(contexto,"anular", "INICIO");

        String idOperacion = contexto.parametros.string(ID_OPERACION);
        EcheqOB echeq = ServicioEcheqOB.find(Integer.parseInt(idOperacion)).get();
        if (empty(echeq)) {
            throw new Exception();
        }
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        ListadoChequesOB listado= ApiCheques.getChequeById(contexto, echeq.idCheque, cuit).get();
        LogOB.evento(contexto,"anular", "getChequeById Ejecutada");

        if (listado.result.cheques.isEmpty()) {

            return new ErrorGenericoOB().setErrores("Cheque inexistente", "El cheque ingresado no existe");
        }
        ListadoChequesOB.cheques cheque = listado.result.cheques.get(0);
        contexto.parametros.set(ID_CHEQUE,cheque.cheque_id);
        Objeto respuesta;
        switch (cheque.estado) {
            case EMITIDO_PENDIENTE -> respuesta = anularEcheq(contexto);
            case ACTIVO_PENDIENTE -> respuesta = anularEndosoEcheq(contexto, cheque);
            case DEVOLUCION_PENDIENTE -> respuesta = anularDevolucionEcheq(contexto);
            case CESION_PENDIENTE -> respuesta = anularCesionEcheq(contexto, cheque);
            case AVAL_PENDIENTE -> respuesta = anularAvalEcheq(contexto,cheque);
            case MANDATO_PENDIENTE -> respuesta = anularMandatoNegociacionEcheq(contexto,cheque);
            case NEGOCIACION_PENDIENTE -> respuesta = anularEndosoEcheq(contexto, cheque);
            default -> {
                return respuesta(DATOS_INVALIDOS);
            }
        }
        if (respuesta.get(ERROR)!=null) {
            return respuesta(ERROR, DATOS, respuesta.get(DATOS));
        }
        LogOB.evento(contexto,"anular", "FIN");
        return respuesta;
    }

    private static Objeto anularMandatoNegociacionEcheq(ContextoOB contexto, ListadoChequesOB.cheques cheque) {
        LogOB.evento(contexto,"anularMandatoEcheq", "INICIO");
        Objeto respuesta = new Objeto();
        try{
            MandatoNegociacionOB anulacion = ApiCheques.anularMandatoNegociacion(contexto,cheque.cheque_id).get();
            respuesta.set("anulacion", anulacion.result);
            respuesta.set("estado", "0");
            LogOB.evento(contexto,"anularMandatoEcheq", "anularMandatoEcheq ejecutada");
        } catch (ApiException e){
            respuesta.set(ERROR, true);
            respuesta.set(DATOS, e.response.get("mensajeAlUsuario"));
            LogOB.evento(contexto, "anularMandatoEcheq", new Objeto().set("error", e.getMessage()));
        }
        LogOB.evento(contexto,"anularMandatoEcheq", "FIN");
        return respuesta;
    }

    public static Object detallesBandejaChequera(ContextoOB contexto){
        LogOB.evento(contexto,"detallesBandejaChequera", "INICIO");
        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
        int idOperacion = contexto.parametros.integer("idOperacion");
        ServicioChequeraOB servicioChequeraOB = new ServicioChequeraOB(contexto);
        ChequeraOB chequera = servicioChequeraOB.find(idOperacion).get();
        if (empty(chequera)){
            return new ErrorGenericoOB().setErrores("chequera inexistente", "LA chequera ingresada no existe");
        }
        BandejaOB bandeja = servicioBandeja.find(chequera.id).get();

        Objeto respuesta = new Objeto();
        respuesta.set("id",  chequera.id);
        respuesta.set("cuenta",  chequera.cuentaOrigen);
        respuesta.set("fechaCreacion", chequera.fechaCreacion.toString());
        respuesta.set("cuentaOrigen",chequera.cuentaOrigen);
        respuesta.set("estadoBandeja",chequera.estadoBandeja.descripcion);
        respuesta.set("firmas", OBFirmas.obtenerDatosFirmantes(contexto, bandeja));
        LogOB.evento(contexto,"detallesBandejaChequera", "FIN");

        return respuesta("0",DATOS,respuesta);
    }
    public static Object detallesBandeja(ContextoOB contexto){
        LogOB.evento(contexto,"detallesBandeja", "INICIO");

        int idCheque = contexto.parametros.integer("idOperacion");
        ServicioEcheqOB servicioEcheqOB = new ServicioEcheqOB(contexto);
        EcheqOB echeqOB = servicioEcheqOB.findById(idCheque).get();
        if (empty(echeqOB)){
            return new ErrorGenericoOB().setErrores("Cheque inexistente", "El cheque ingresado no existe");
        }
        Objeto respuesta = new Objeto();
        respuesta.set("monto",echeqOB.monto);
        respuesta.set("numeroChequera",echeqOB.numeroChequera);
        respuesta.set("fechaPago",echeqOB.fechaPago.toString().substring(0,echeqOB.fechaPago.toString().indexOf('T')));
        respuesta.set("emailBeneficiario",echeqOB.emailBeneficiario);
        respuesta.set("version",echeqOB.version);
        respuesta.set("aLaOrden",echeqOB.aLaOrden);
        respuesta.set("motivoPago",echeqOB.motivoPago);
        respuesta.set("concepto",echeqOB.concepto);
        respuesta.set("cruzado",echeqOB.cruzado);
        respuesta.set("idCheque",echeqOB.idCheque);
        respuesta.set("numeroCheque",echeqOB.numeroCheque);
        respuesta.set("tipo",echeqOB.tipo);
        respuesta.set("estado",echeqOB.estado.descripcion);
        respuesta.set("accion",echeqOB.accion.toString());
        respuesta.set("tipoEndoso",echeqOB.tipoEndoso);
        respuesta.set("cesionarioDomicilio",echeqOB.cesionarioDomicilio);
        respuesta.set("cesionarioNombre",echeqOB.cesionarioNombre);
        respuesta.set("cuentaDeposito",echeqOB.cuentaDeposito);
        respuesta.set("cuentaOrigen",echeqOB.cuentaOrigen);
        respuesta.set("documentoBeneficiario",echeqOB.documentoBeneficiario);
        respuesta.set("razonSocialBeneficiario",echeqOB.razonSocialBeneficiario);
        respuesta.set("documentoEmisor",echeqOB.documentoEmisor);
        respuesta.set("estadoBandeja",echeqOB.estadoBandeja.descripcion);
        respuesta.set("accion",echeqOB.accion.toString().equals("MANDATO_NEG")?"Mandato Negociacion":echeqOB.accion.toString());
        LogOB.evento(contexto,"detallesBandeja", "FIN");
        return respuesta("0",DATOS,respuesta);
    }

    public static Object detallesDescuentoBandeja(ContextoOB contexto){
        LogOB.evento(contexto,"detallesDescuentoBandeja", "INICIO");

        int idCheque = contexto.parametros.integer("idOperacion");
        ServicioEcheqDescuentoOB servicioEcheqOB = new ServicioEcheqDescuentoOB(contexto);
        EcheqDescuentoOB echeqDescuentoOB = servicioEcheqOB.find(idCheque).get();
        if (empty(echeqDescuentoOB)){
            return new ErrorGenericoOB().setErrores("Cheque inexistente", "El cheque ingresado no existe");
        }
        Objeto respuesta = new Objeto();
        respuesta.set("monto",echeqDescuentoOB.monto);
        respuesta.set("estado",echeqDescuentoOB.estado.descripcion);
        respuesta.set("accion",echeqDescuentoOB.accion.toString());
        respuesta.set("cuentaOrigen",echeqDescuentoOB.cuentaOrigen);
        respuesta.set("estadoBandeja",echeqDescuentoOB.estadoBandeja.descripcion);
        respuesta.set("accion",echeqDescuentoOB.accion.toString());
        LogOB.evento(contexto,"detallesDescuentoBandeja", "FIN");
        return respuesta("0",DATOS,respuesta);
    }

    public static Object historialEcheq(ContextoOB contexto) {
        LogOB.evento(contexto,"historialEcheq", "INICIO");

        String fechaDesde = contexto.parametros.string("fechaDesde",null);
        String fechaHasta = contexto.parametros.string("fechaHasta",null);
        boolean esFechaEmision = contexto.parametros.bool("esFechaEmision",null);
        String estado = contexto.parametros.string("estado",null);
        String accion = contexto.parametros.string(ACCION);
        if(fechaDesde ==null){
            fechaDesde = LocalDate.now().toString();
        }
        if(fechaHasta ==null){
            fechaHasta = LocalDate.now().toString();
        }
        final String filtroD=fechaDesde;
        final String filtroH=fechaHasta;

        String cuit = contexto.sesion().empresaOB.cuit.toString();
        String filter;
        switch (accion.toLowerCase()){
            case "recibidos" ->filter = String.format(FILTER_RECIBIDOS, cuit);
            case "emitidos" ->filter = String.format(FILTER_EMITIDOS, cuit);
            case "endosados" ->filter = String.format(FILTER_ENDOSADOS, cuit);
            case "cedidos" ->filter = String.format(FILTER_CEDIDOS,cuit);
            default -> {
                return new ErrorGenericoOB().setErrores("Accion incorrecta", "La accion debe ser 'recibidos', 'emitidos', 'endosados' o 'cedidos'");
            }
        }
        if (estado != null) {
            if (estado.equals("activo")) {
                filter = filter + "%20and%20cheques.estado%20eq%20__activo__";
            }
            if (estado.equals("activo-pendiente")){
                filter = filter + "%20and%20cheques.estado%20eq%20__activo-pendiente__";
            }
            if (estado.equals("custodia")) {
                filter = filter + "%20and%20cheques.estado%20eq%20__custodia__";
            }
            if (estado.equals("depositado")) {
                filter = filter + "%20and%20cheques.estado%20eq%20__depositado__";
            }
            if (estado.equals("devolucion-pendiente")) {
                filter = filter + "%20and%20cheques.estado%20eq%20__devolucion-pendiente__";
            }
            if (estado.equals("emitido-pendiente")) {
                filter = filter + "%20and%20cheques.estado%20eq%20__emitido-pendiente__";
            }
            if (estado.equals("presentado")) {
                filter = filter + "%20and%20cheques.estado%20eq%20__presentado__";
            }
            if (estado.equals("rechazado")) {
                filter = filter + "%20and%20cheques.estado%20eq%20__rechazado__";
            }
            if (estado.equals("anulado")) {
                filter = filter + "%20and%20cheques.estado%20eq%20__anulado__";
            }
            if (estado.equals("caducado")) {
                filter = filter + "%20and%20cheques.estado%20eq%20__caducado__";
            }
            if (estado.equals("pagado")) {
                filter = filter + "%20and%20cheques.estado%20eq%20__pagado__";
            }
            if (estado.equals("repudiado")) {
                filter = filter + "%20and%20cheques.estado%20eq%20__repudiado__";
            }
            if (estado.equals("cesion-pendiente")) {
                filter = filter + "%20and%20cheques.estado%20eq%20__cesion-pendiente__";
            }
            if (estado.equals("aval-pendiente")) {
                filter = filter + "%20and%20cheques.estado%20eq%20__aval-pendiente__";
            }
            if (estado.equals("negociacion-pendiente")) {
                filter = filter + "%20and%20cheques.estado%20eq%20__NEGOCIACION-PENDIENTE__";
            }
            if (estado.equals("mandato-pendiente")) {
                filter = filter + "%20and%20cheques.estado%20eq%20__mandato-pendiente__";
            }
        }
        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate fechaD = LocalDate.parse(fechaDesde, formatter);
        LocalDate fechaH = LocalDate.parse(fechaHasta, formatter);
        fechaD = fechaD.minusDays(1);
        fechaH=fechaH.plusDays(1);
        fechaDesde = fechaD.format(formatter);
        fechaHasta = fechaH.format(formatter);
        try {
            if (fechaDesde != null){
                if (esFechaEmision){
                    filter += "%20and%20cheques.fecha_emision%20ge%20__" + format.format(originalFormat.parse(fechaDesde)) + "__";
                } else
                {
                    filter += "%20and%20cheques.fecha_pago%20ge%20__" + format.format(originalFormat.parse(fechaDesde)) + "__";
                }

            }
            if (fechaHasta != null){
                if (esFechaEmision){
                    filter += "%20and%20cheques.fecha_emision%20le%20__" + format.format(originalFormat.parse(fechaHasta)) + "__";
                }else
                {
                    filter += "%20and%20cheques.fecha_pago%20le%20__" + format.format(originalFormat.parse(fechaHasta)) + "__";
                }

            }
        } catch (ParseException e){
            return new ErrorGenericoOB().setErrores("Parametros incorrectos", "El formato de fecha debe ser yyyy-MM-dd");
        }
        int pag = 1;
        int[] cantidad = {1};
        cantidad[0] =0;
        List<String> idscheques=new ArrayList<>();
        ListadoChequesOB listado = ApiCheques.listadoCheques(contexto, cuit, "200", filter,String.valueOf(pag)).get();
        LogOB.evento(contexto,"historialEcheq", "Listado de Cheques cargado");
        Objeto respuesta = new Objeto();
        ServicioEcheqOB servicioEcheqOB = new ServicioEcheqOB(contexto);
        Map<String, Futuro<ListadoChequesOB>> listados = new HashMap<>();
        if( listado.result.cheques.size()>0){
            cantidad[0] =listado.result.cheques.size();
            if( listado.result.total_cheques>20){
                while (pag*20 <listado.result.total_cheques){
                    pag +=1;
                    ContextoOB nuevoContexto = contexto.clonar();
                    Futuro<ListadoChequesOB> mapaCheques = ApiCheques.listadoCheques(nuevoContexto, cuit, "200", filter,String.valueOf(pag));
                    listados.put(String.valueOf(pag),mapaCheques);
                }
            }
        }
        listados.forEach((key, value) -> {
            ListadoChequesOB listaCheques = value.get();
            LogOB.evento(contexto,"historialEcheq","Listado de Cheques cargado");
            listaCheques.result.cheques.stream().forEach(item -> {
                cantidad[0] +=1;
                listado.result.cheques.add(item);
            });

        });
        listado.result.cheques.stream().forEach(item -> {
            idscheques.add(item.cheque_id.toString());
        });

        respuesta.add("cantidad",cantidad[0]);
        List<EcheqOB> chequesEnBandeja = listado.result.cheques.size()>0? servicioEcheqOB.findChequeByEmisorAndIdAndEstado(contexto.sesion().empresaOB.cuit.toString()).get():Collections.emptyList();
        List<EcheqOB> chequesenBase = servicioEcheqOB.findByFieldIn(idscheques).get();
        listado.result.cheques.stream().forEach(item -> {
            if (item.fecha_pago_vencida) caducarCheque(contexto, cuit, item);
            EnumAccionesEcheqOB accionRecepcion = null;
            if (accion.equals("recibidos")){
                accionRecepcion = mapearMetodoRecepcion(item,cuit);
            }
            Optional<EcheqOB> chequeBase = null;
            chequeBase = chequesenBase.stream().filter(cheque -> cheque.idCheque.equals(item.cheque_id.toString())).findFirst();
            chequeBase = chequeBase.isEmpty() ? null : chequeBase;
            Objeto cheque = new Objeto();
            cheque.set("importe", item.monto);
            cheque.set("estado", item.estado);
            cheque.set("numeroCheque", item.cheque_numero);
            cheque.set("numeroChequera", item.numero_chequera);
            cheque.set("emailBeneficiario", chequeBase == null ? null : chequeBase.get().emailBeneficiario);
            cheque.set(ID_CHEQUE, item.cheque_id);
            cheque.set("caracter", item.cheque_caracter);
            cheque.set("tipo", item.cheque_tipo);
            cheque.set("fechaEmision", item.fecha_emision.substring(0, item.fecha_emision.indexOf('T')));
            cheque.set(FECHA_PAGO, item.fecha_pago.substring(0, item.fecha_pago.indexOf('T')));
            cheque.set("bancoEmisor", item.cuenta_emisora.banco_nombre);
            cheque.set("codigoBanco", item.cuenta_emisora.banco_codigo);
            cheque.set("codigoSucursal", item.cuenta_emisora.sucursal_codigo);
            cheque.set("codigoPostalSucursal", item.cuenta_emisora.sucursal_cp);
            cheque.set("cuentaOrigen", item.cuenta_emisora.emisor_cuenta);
            cheque.set("cbuCustodia", item.cbu_custodia);
            if (accion!=null){
                if (item.estado.equalsIgnoreCase("rechazado")||(item.estado.equalsIgnoreCase("devolucion-pendiente")&&item.rechazos!=null)){
                    cheque.set("detalleRechazo",mapearMensajeRechazo(item,accion));
                    if (cheque.get("detalleRechazo")==null){
                        cheque.del("detalleRechazo");
                    }
                }
            }

            if (accion.equals("endosados")&&item.endosos !=null){
                cheque.set("emisorCuit", item.endosos.get(0).emisor_documento);
                cheque.set("emisorRazonSocial", item.endosos.get(0).emisor_razon_social);
                cheque.set("beneficiarioRazonSocial", item.endosos.get(0).benef_razon_social);
                cheque.set("beneficiarioCuit", item.endosos.get(0).benef_documento);
            }else if(accion.equals("cedidos")&&item.cesiones!=null){
                cheque.set("emisorCuit", item.cesiones.get(0).cedente_documento);
                cheque.set("emisorRazonSocial", item.cesiones.get(0).cedente_nombre);
                cheque.set("beneficiarioRazonSocial", item.cesiones.get(0).cesionario_nombre);
                cheque.set("beneficiarioCuit", item.cesiones.get(0).cesionario_documento);
            }else if (accionRecepcion!=null){
                if(accionRecepcion.equals(EnumAccionesEcheqOB.ENDOSO)&&item.endosos!=null){
                    cheque.set("emisorCuit", item.endosos.get(0).emisor_documento);
                    cheque.set("emisorRazonSocial", item.endosos.get(0).emisor_razon_social);
                    cheque.set("beneficiarioRazonSocial", item.endosos.get(0).benef_razon_social);
                    cheque.set("beneficiarioCuit", item.endosos.get(0).benef_documento);
                    cheque.set("accionRecepcion","ENDOSO");
                } else if (accionRecepcion.equals(EnumAccionesEcheqOB.CESION)&&item.cesiones!=null){
                    cheque.set("emisorCuit", item.cesiones.get(0).cedente_documento);
                    cheque.set("emisorRazonSocial", item.cesiones.get(0).cedente_nombre);
                    cheque.set("beneficiarioRazonSocial", item.cesiones.get(0).cesionario_nombre);
                    cheque.set("beneficiarioCuit", item.cesiones.get(0).cesionario_documento);
                    cheque.set("accionRecepcion","CESION");
                }
                else if (accionRecepcion.equals(EnumAccionesEcheqOB.EMISION)){
                    cheque.set("emisorCuit", item.cuenta_emisora.emisor_cuit);
                    cheque.set("emisorRazonSocial", item.cuenta_emisora.emisor_razon_social);
                    cheque.set("beneficiarioRazonSocial", item.emitido_a.beneficiario_nombre);
                    cheque.set("beneficiarioCuit", item.emitido_a.beneficiario_documento);
                    cheque.set("accionRecepcion","EMISION");
                }
            }  else {
                cheque.set("emisorCuit", item.cuenta_emisora.emisor_cuit);
                cheque.set("emisorRazonSocial", item.cuenta_emisora.emisor_razon_social);
                cheque.set("beneficiarioRazonSocial", item.emitido_a.beneficiario_nombre);
                cheque.set("beneficiarioCuit", item.emitido_a.beneficiario_documento);
            }
            if (accion.equalsIgnoreCase("emitidos")) {
                if (item.cheque_motivo_pago!=null){
                    cheque.set("referencia",item.cheque_motivo_pago);
                }
            }
            cheque.set("emisorOriginalCuit",item.cuenta_emisora.emisor_cuit);
            cheque.set("emisorOriginal",item.cuenta_emisora.emisor_razon_social);

            List<EcheqOB> chequeEnBandejaPorId = chequesEnBandeja.stream().filter(cheq -> item.cheque_id.equals(cheq.idCheque)).toList();
            if (chequeEnBandejaPorId.isEmpty()){
                try {
                    Object acciones = accionesPosibles(contexto, item, accion);
                    Objeto lista = (Objeto) acciones;
                    if (lista.existe(ACCION)) {
                        Objeto a = (Objeto) lista.get(ACCION);
                        for (Object elemento : a.toList()) {
                            cheque.add("acciones", elemento);
                        }
                    }
                    if (lista.existe("ESTADO")){
                        cheque.set("estado",lista.get("ESTADO"));
                    }

                } catch (Exception e) {
                    respuesta.set(ERROR, e.toString());
                }
            } else{
                cheque.set("estado",chequeEnBandejaPorId.get(0).estadoBandeja.descripcion);
            }



            if(filtroD.equals(filtroH)){
                if(esFechaEmision){
                    if(cheque.get("fechaEmision").equals(filtroD)){
                        respuesta.add("cheque", cheque);
                    }
                }else{
                    if(cheque.get("fechaPago").equals(filtroD)){
                        respuesta.add("cheque", cheque);
                    }
                }
            }else{
                respuesta.add("cheque", cheque);
            }

        });


        LogOB.evento(contexto,"historialEcheq", "FIN");
        return respuesta("0", DATOS, respuesta);
    }
    
    public static Object solicitudCustodiarEcheq(ContextoOB contexto) {
    	//Se llama a este metodo cuando presionan los botones o de "Firmar Ahora" o de "Enviar a Bandeja de firmas"
    	Objeto respuestaEcheq = new Objeto();
    	EcheqOB echeqOB;
    	ServicioEcheqOB servicioEcheqOB = new ServicioEcheqOB(contexto);
    	ServicioEcheqConvenioOB servicioEcheqConvenioOB = new ServicioEcheqConvenioOB(contexto);

    	LogOB.evento(contexto,"solicitudCustodiarEcheq", "INICIO");

        String idCheque = contexto.parametros.string("idCheque");
        String documentoBeneficiario = contexto.parametros.string("documentoBeneficiario");
        String tipoDocumentoBeneficiario = contexto.parametros.string("tipoDocumentoBeneficiario");
        String emailBeneficiario = contexto.parametros.string("emailBeneficiario", null);
        String cbuDeposito = contexto.parametros.string("cbuDeposito");
        String convenio = contexto.parametros.string("convenio",null);

        ListadoChequesOB listado;

        if (empty(cbuDeposito) || cbuDeposito == null) {
            return respuesta(DATOS_INVALIDOS);
        }
        
        if (!tipoDocumentoBeneficiario.equals("CUIT") && !tipoDocumentoBeneficiario.equals("CUIL")) {
            return new ErrorGenericoOB().setErrores("Tipo de documento inválido.", "El tipo de documento debe ser 'CUIT' o 'CUIL'.");
        }

        try {
            listado = ApiCheques.getChequeById(contexto, idCheque, contexto.sesion().empresaOB.cuit.toString()).get();
        } catch (Exception e) {
            return new ErrorGenericoOB().setErrores("Consulta inválida.", "No se encontraron cheques para esta consulta.");
        }

        if (listado.result.cheques.isEmpty()) {
            return new ErrorGenericoOB().setErrores("Consulta inválida.", "No se encontraron cheques para esta consulta.");
        }
        DetalleRazonSocialOB detalleRazonSocial = ApiCheques.razonSocial(contexto, documentoBeneficiario).get();
        LogOB.evento(contexto,"solicitudCustodiarEcheq", "ApiCheques.razonSocial ejecutada");

        ListadoChequesOB.cheques cheque = listado.result.cheques.get(0);
        boolean cruzado = cheque.cheque_modo.equals("0");
        boolean caracter = cheque.cheque_caracter.equals("a la orden");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
        LocalDateTime fechaPago = LocalDateTime.parse(cheque.fecha_pago, formatter);

        try {
        	echeqOB = servicioEcheqOB.cargarCreado(cheque.numero_chequera, BigDecimal.valueOf(cheque.monto), documentoBeneficiario, tipoDocumentoBeneficiario, emailBeneficiario, caracter, cheque.cheque_motivo_pago, null , cruzado, cheque.cuenta_emisora.emisor_cuenta, detalleRazonSocial.result.beneficiario_razon_social, contexto.sesion().empresaOB, fechaPago, cheque.cheque_tipo, EnumAccionesEcheqOB.CUSTODIAR, contexto.sesion().empresaOB.cuit.toString(), cheque.cheque_id, cheque.cheque_numero, null, null, null, cbuDeposito, contexto.sesion().usuarioOB).get();
            respuestaEcheq.set("idOperacion", echeqOB.id);
        } catch (Exception e) {
    		return new ErrorGenericoOB().setErrores("Error al insertar registro.", "No se pudo insertar el registro en Ob_Echeq.");
    	}

        if (convenio != null) {
        	try {
        		//throw new Exception();
        		servicioEcheqConvenioOB.crear(echeqOB.id, convenio);
        	} catch (Exception e) {
        		echeqOB.estado.descripcion = "RECHAZADO";
        		echeqOB.estado.id = 4;
        		echeqOB.estadoBandeja.id = 4;
        		servicioEcheqOB.update(echeqOB);
        		return new ErrorGenericoOB().setErrores("Error al insertar registro.", "No se pudo insertar la relacion echeq / convenio");
        	}
        }

        LogOB.evento(contexto,"solicitudCustodiarEcheq", "FIN");
        return respuesta("datos", respuestaEcheq);
    }
    
    public static Object custodiarEcheq(ContextoOB contexto) {
    	//Se llama a este metodo al firmar la operacion (en pantalla de Custodia o en pantalla de Bandeja de firmas)
        LogOB.evento(contexto,"custodiarEcheq", "INICIO");

        int id = contexto.parametros.integer(ID_OPERACION);
        //va siempre "E" por ser un producto de Echeq
        contexto.parametros.set("medioRecaudacion", "E");

        EcheqOB echeqOB = ServicioEcheqOB.find(id).get();

        if (empty(echeqOB) || echeqOB == null) {
            return respuesta(DATOS_INVALIDOS);
        }
        
        ListadoChequesOB listado;
        Long cuitt = contexto.sesion().empresaOB.cuit;
        String razonSocial = contexto.sesion().empresaOB.razonSocial;

        try {
            listado = ApiCheques.getChequeById(contexto, echeqOB.idCheque, contexto.sesion().empresaOB.cuit.toString()).get();
        } catch (Exception e) {
            return new ErrorGenericoOB().setErrores("Consulta inválida.", "No se encontraron cheques para esta consulta.");
        }

        if (listado.result.cheques.isEmpty()) {
            return new ErrorGenericoOB().setErrores("Consulta inválida.", "No se encontraron cheques para esta consulta.");
        }
        
        // Obtenemos el cbu de la cuenta deposito
        CuentasOB cuentas= ApiCuentas.cuentas(contexto, contexto.sesion().empresaOB.idCobis).get();
        LogOB.evento(contexto,"depositarCheque", "Traer cuentas");
        Optional<CuentasOB.CuentaOB> cuenta = cuentas.stream().filter(c -> c.numeroProducto.equals(echeqOB.cuentaDeposito)).findFirst();
        

        if (!cuenta.isPresent()) {
            return new ErrorGenericoOB().setErrores("Cuenta no encontrada", "No se encontro cuenta que concuerde");
        }

        ServicioEcheqConvenioOB servicioEcheqConvenioOB = new ServicioEcheqConvenioOB(contexto);
    	EcheqConvenioOB convenioExistente = servicioEcheqConvenioOB.findById(echeqOB.id).get();

		Boolean recaudaciones = false;
    	Object convenios = null;
    	Integer convenio = null;
    	if(convenioExistente != null) {
    		convenio = Integer.valueOf(convenioExistente.getConvenio());
    	}

    	if(convenioExistente !=null) {
    		LogOB.evento(contexto, "listaconvenios", "Se ingresa en busqueda convenios reca");
            convenios  = OBCobranzaIntegral.consultaConveniosDetalleHabilitacion(contexto);
            Objeto responseMap = (Objeto) convenios;
	        try {
	                LogOB.evento(contexto, "listaconvenios", "convenio: "+responseMap.toString());
	                if(responseMap.get("estado").equals("0")){
	                    Objeto lista = (Objeto) responseMap.get("datos");
	                    LogOB.evento(contexto, "listaconvenios", "cantidad de convenios: "+lista.toMap().size());
	                    if(lista.toList().size()>0) {
	                        List<Object> listaconvenios = lista.toList();
	                        LogOB.evento(contexto, "listaconvenios", listaconvenios.toString());
	                        for (Object convenioAux : listaconvenios) {
	                            Map<String, Object> c = (Map<String, Object>) convenioAux;
	                            if (c.get("numeroProducto").equals(cuenta.get().numeroProducto.toString()) &&c.get("idConvenio").equals(convenio) && c.get("estadoEcheq").equals("S")) {
	                                recaudaciones = true;
	                                LogOB.evento(contexto, "listaconvenios", "El convenio enviado tiene echeq habilitado y coincide con la cuenta");
	                                break;
	                            }
	                        }
	                    }
	                }
	            }catch (Exception e){
	                LogOB.evento(contexto, "listaconvenios", "error: "+e.getMessage());
	            }
            LogOB.evento(contexto,"listaconvenios","fin convenios");
    	}
    	///POST CUSTODIA
		CustodiarEcheqOB custodiarEcheqOB = ApiCheques.custodiarCheque(contexto, listado.result.cheques.get(0), echeqOB.cuentaDeposito, cuenta.get().cbu, echeqOB.emailBeneficiario,cuenta.get().sucursal).get();

		///POST RELACION
		if(recaudaciones){
	        try {
	        	RelacionEcheqConvenioOB relacionarEcheqConvenioOB = ApiRecaudaciones.relacionarEcheqConvenio(contexto, echeqOB.idCheque, convenio, "ECHEQDIF", cuitt, razonSocial).get();
	            LogOB.evento(contexto,"vincular al convenio","no rompio");
	        }
	        catch (ApiException e ){
	            LogOB.evento(contexto,"vincular al convenio","ERROR");
	        }

	    }

        LogOB.evento(contexto,"custodiarEcheq", "FIN");
        return custodiarEcheqOB.result;
    }
    
    public static Object consultaConveniosSugeridosEcheq(ContextoOB contexto) {
        String convenio = contexto.parametros.string("convenio");
        String estado = contexto.parametros.string("estado");
        String numeroCheque = contexto.parametros.string("numeroCheque");
        String tipo = contexto.parametros.string("tipo");
        String razonSocial = contexto.parametros.string("razonSocial");
        String fechaDesde = contexto.parametros.string("fechaDesde");
        String fechaHasta = contexto.parametros.string("fechaHasta");
        String pagina = contexto.parametros.string("pagina");
        String limite = contexto.parametros.string("limite");

        ConveniosSugeridosEcheqOB conveniosRecaudaciones = ApiRecaudaciones.consultaConveniosSugeridos(contexto, convenio, tipo, numeroCheque, estado, razonSocial, fechaDesde, fechaHasta, pagina, limite).get();

        return respuesta("datos", conveniosRecaudaciones);
    }
    
    public static Object solicitudRescatarEcheq(ContextoOB contexto) {
    	LogOB.evento(contexto,"solicitudRescatarEcheq", "INICIO");

        String idCheque = contexto.parametros.string("idCheque");
        String documentoBeneficiario = contexto.parametros.string("documentoBeneficiario");
        String tipoDocumentoBeneficiario = contexto.parametros.string("tipoDocumentoBeneficiario");
        String cbuDeposito = contexto.parametros.string("cbuDeposito");

        ListadoChequesOB listado;

        if (empty(cbuDeposito) || cbuDeposito == null) {
            return respuesta(DATOS_INVALIDOS);
        }
        
        if (!tipoDocumentoBeneficiario.equals("CUIT") && !tipoDocumentoBeneficiario.equals("CUIL")) {
            return new ErrorGenericoOB().setErrores("Tipo de documento inválido.", "El tipo de documento debe ser 'CUIT' o 'CUIL'.");
        }

        try {
            listado = ApiCheques.getChequeById(contexto, idCheque, contexto.sesion().empresaOB.cuit.toString()).get();
        } catch (Exception e) {
            return new ErrorGenericoOB().setErrores("Consulta inválida.", "No se encontraron cheques para esta consulta.");
        }

        if (listado.result.cheques.isEmpty()) {
            return new ErrorGenericoOB().setErrores("Consulta inválida.", "No se encontraron cheques para esta consulta.");
        }
        DetalleRazonSocialOB detalleRazonSocial = ApiCheques.razonSocial(contexto, documentoBeneficiario).get();
        LogOB.evento(contexto,"solicitudRescatarEcheq", "ApiCheques.razonSocial ejecutada");

        ListadoChequesOB.cheques cheque = listado.result.cheques.get(0);
        boolean cruzado = cheque.cheque_modo.equals("0");
        boolean caracter = cheque.cheque_caracter.equals("a la orden");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
        LocalDateTime fechaPago = LocalDateTime.parse(cheque.fecha_pago, formatter);

        ServicioEcheqOB servicioEcheqOB = new ServicioEcheqOB(contexto);
        EcheqOB echeq = servicioEcheqOB.cargarCreado(cheque.numero_chequera, BigDecimal.valueOf(cheque.monto), documentoBeneficiario, tipoDocumentoBeneficiario, "", caracter, cheque.cheque_motivo_pago, cheque.cheque_concepto != null ? cheque.cheque_concepto : " ", cruzado, cheque.cuenta_emisora.emisor_cuenta, detalleRazonSocial.result.beneficiario_razon_social, contexto.sesion().empresaOB, fechaPago, cheque.cheque_tipo, EnumAccionesEcheqOB.RESCATAR, contexto.sesion().empresaOB.cuit.toString(), cheque.cheque_id, cheque.cheque_numero, null, null, null, cbuDeposito, contexto.sesion().usuarioOB).get();
        Objeto respuestaEcheq = new Objeto();
        respuestaEcheq.set("idOperacion", echeq.id);

        LogOB.evento(contexto,"solicitudRescatarEcheq", "FIN");
        return respuesta("datos", respuestaEcheq);
    }
    
    public static Object rescatarEcheq(ContextoOB contexto) {
        LogOB.evento(contexto,"rescatarEcheq", "INICIO");

        int id = contexto.parametros.integer(ID_OPERACION);
        EcheqOB echeq = ServicioEcheqOB.find(id).get();

        if (empty(echeq) || echeq == null) {
            return respuesta(DATOS_INVALIDOS);
        }
        
        ListadoChequesOB listado;

        try {
            listado = ApiCheques.getChequeById(contexto, echeq.idCheque, contexto.sesion().empresaOB.cuit.toString()).get();
        } catch (Exception e) {
            return new ErrorGenericoOB().setErrores("Consulta inválida.", "No se encontraron cheques para esta consulta.");
        }

        if (listado.result.cheques.isEmpty()) {
            return new ErrorGenericoOB().setErrores("Consulta inválida.", "No se encontraron cheques para esta consulta.");
        }
        
        // Obtenemos el cbu de la cuenta deposito
        CuentasOB cuentas= ApiCuentas.cuentas(contexto, contexto.sesion().empresaOB.idCobis).get();
        LogOB.evento(contexto,"rescatarEcheq", "Traer cuentas");
        List<CuentasOB.CuentaOB> listaCuenta = cuentas.stream().filter(c -> c.numeroProducto.equals(echeq.cuentaDeposito)).toList();
        CuentasOB.CuentaOB cuenta = listaCuenta.get(0);
        
        RescatarEcheqOB rescatarEcheqOB = ApiCheques.rescatarCheque(contexto, listado.result.cheques.get(0), echeq.cuentaDeposito, cuenta.cbu).get();
        LogOB.evento(contexto,"rescatarEcheq", "Rescatar Echeq");
        LogOB.evento(contexto,"rescatarEcheq", "FIN");
        return rescatarEcheqOB.result;
    }

    private static void caducarCheque(ContextoOB contexto, String cuit, ListadoChequesOB.cheques item) {
        LogOB.evento(contexto,"caducarCheque", "INICIO");
        if (item.estado.equals("EMITIDO-PENDIENTE") || item.estado.equals("ACTIVO") || item.estado.equals("ACTIVO-PENDIENTE") || item.estado.equals(DEVOLUCION_PENDIENTE)) {
            try {
                ApiCheques.caducarCheque(contexto, item.cheque_id).get();
                LogOB.evento(contexto,"caducarCheque","Ok");
                ListadoChequesOB cheque = ApiCheques.getChequeById(contexto, item.cheque_id, cuit).get();
                LogOB.evento(contexto,"caducarCheque", new Objeto().set("IdCheque: ",item.cheque_id));
                item.estado = cheque.result.cheques.get(0).estado;
            } catch (Exception e) {
                LogOB.evento(contexto, "caducarCheque", new Objeto().set("error", e.getMessage()));
            }
        }
        LogOB.evento(contexto,"caducarCheque", "FIN");
    }

    public static Object bandejaDescuentoCheque(ContextoOB contexto) {
    	Object respuestaFinal = null;
    	SesionOB sesion = contexto.sesion();
        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);

        try {
        	ServicioEstadoBandejaOB servicioEstadoBandejaOB = new ServicioEstadoBandejaOB(contexto);

            EstadoBandejaOB estadoInicialBandeja = servicioEstadoBandejaOB.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();

            ServicioEcheqDescuentoOB servicio = new ServicioEcheqDescuentoOB(contexto);

            String numeroDocumento = contexto.sesion().empresaOB.cuit.toString();
        	Integer tipoDocumento = ConstantesOB.FACTORING_CLIENTE_TIPO_DOCUMENTO;
        	String solicitudNumero = contexto.parametros.string("solicitudNumero");
        	String cuenta = contexto.parametros.string("cuenta");
        	BigDecimal monto = contexto.parametros.bigDecimal("monto");

        	EcheqDescuentoOB echeqDescuentoOB = servicio.crear(cuenta, monto, numeroDocumento, tipoDocumento, ConstantesOB.FACTORING_DESCUENTO_ESTADO_PREAUTORIZAR, solicitudNumero, sesion.usuarioOB, sesion.empresaOB).get();

        	ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
        	BandejaOB bandeja = servicioBandeja.find(echeqDescuentoOB.id).get();

            ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
            AccionesOB accionCrear = servicioAcciones.find(EnumAccionesOB.CREAR.getCodigo()).get();

            ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
            servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionCrear, estadoInicialBandeja, estadoInicialBandeja);

        	try {
        		ApiCheques.descontarChequeFactoring(contexto, ConstantesOB.FACTORING_DESCUENTO_ESTADO_PREAUTORIZAR, solicitudNumero).get();
        	} catch (ApiException e) {
        		LogOB.evento(contexto, "bandejaDescuentoCheque",e.response.body);
            	OBFirmas.rechazarEcheqDescuentoSinFirma(contexto, echeqDescuentoOB);
    			return respuesta("ERROR");
        	}

            contexto.parametros.set("idEcheqDescuento", echeqDescuentoOB.id);
            
            Objeto respuestaEcheq = new Objeto();
            respuestaEcheq.set("idOperacion", bandeja.id);

            respuestaFinal = respuesta("datos", respuestaEcheq);

        } catch (RuntimeException rte) {
            return respuesta(rte.getMessage());
        }

        return respuestaFinal;
    }

    public static Object detalleFirmantes(ContextoOB contexto) {
        String idCheque = contexto.parametros.string("idCheque",null);
        String accion = contexto.parametros.string("accion",null);
        ServicioEcheqOB servicioEcheqOB = new ServicioEcheqOB(contexto);
        Objeto cheque =  new Objeto();
        EcheqOB chequeBase = servicioEcheqOB.findByField("idCheque", idCheque,accion,contexto.sesion().empresaOB.cuit.toString()).get();

           try{
                if(chequeBase!=null){
                    cheque.set("firmas", OBFirmas.obtenerDatosFirmantes(contexto, chequeBase));
                }else {
                    return    respuesta("ERROR","datos",null);
                }
                }catch (Exception e){}
        return  respuesta("0","datos",cheque);
    }

    public static Objeto precargaSolicitudAval(ContextoOB contexto,ServicioEcheqOB servicioEcheqOB){
        String cuitAvalista = contexto.parametros.string("cuitAvalista");
        String nombreAvalista = contexto.parametros.string("nombreAvalista");
        String domicilioAvalista = contexto.parametros.string("domicilioAvalista");
        String idCheque = contexto.parametros.string("idCheque");

        ListadoChequesOB listado;
        try {
            listado = ApiCheques.getChequeById(contexto, idCheque, contexto.sesion().empresaOB.cuit.toString()).get();
        } catch (Exception e) {
            return new ErrorGenericoOB().setErrores("Consulta inválida.", "No se encontraron cheques para esta consulta.");
        }

        if (listado.result.cheques.isEmpty()) {
            return new ErrorGenericoOB().setErrores("Consulta inválida.", "No se encontraron cheques para esta consulta.");
        }
        ListadoChequesOB.cheques cheque = listado.result.cheques.get(0);
        boolean cruzado = cheque.cheque_modo.equals("0");
        boolean caracter = cheque.cheque_caracter.equals("a la orden");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
        LocalDateTime fechaPago = LocalDateTime.parse(cheque.fecha_pago, formatter);
        EcheqOB echeq = servicioEcheqOB.cargarCreado(cheque.numero_chequera.isEmpty()?"":cheque.numero_chequera,BigDecimal.valueOf(cheque.monto),cuitAvalista,"cuit",null,caracter,cheque.cheque_motivo_pago,cheque.cheque_concepto,cruzado,null,nombreAvalista,contexto.sesion().empresaOB, fechaPago,cheque.cheque_tipo,EnumAccionesEcheqOB.SOLICITAR_AVAL,contexto.sesion().empresaOB.cuit.toString(),cheque.cheque_id,cheque.cheque_numero,null,null,domicilioAvalista,null,contexto.sesion().usuarioOB).get();

        Objeto respuestaEcheq = new Objeto();
        respuestaEcheq.set("idOperacion", echeq.id);

        LogOB.evento(contexto,"SolicitudAvalEcheq", "FIN");
        return respuesta(DATOS, respuestaEcheq);

    }

    public static Objeto avalarEcheq(ContextoOB contexto,ServicioEcheqOB servicioEcheqOB){
        LogOB.evento(contexto,"avalarEcheq", "INICIO");
        String idOperacion = contexto.parametros.string(ID_OPERACION);
        EcheqOB echeq = servicioEcheqOB.find(Integer.parseInt(idOperacion)).get();

        ListadoChequesOB listado = ApiCheques.getChequeById(contexto, echeq.idCheque, contexto.sesion().empresaOB.cuit.toString()).get();
        LogOB.evento(contexto,"avalarEcheq", "ApiCheques.getChequeById ejecutada");
        if (listado.result.cheques.isEmpty()) {
            return respuesta(DATOS_INVALIDOS);
        }
        ListadoChequesOB.cheques cheque = listado.result.cheques.get(0);
        AvalEcheqOB aval = ApiCheques.avalarCheque(contexto, echeq.documentoBeneficiario, echeq.cesionarioDomicilio, echeq.razonSocialBeneficiario, cheque).get();
        LogOB.evento(contexto,"avalarEcheq", "ApiCheques.avalarCheque ejecuatda");
        Objeto respuesta = new Objeto();
        respuesta.add(DATOS,aval.result);
        LogOB.evento(contexto,"avalarEcheq", "FIN");
        return respuesta("0",DATOS,respuesta);
    }

    
public static Objeto solicitudAceptacionMandato(ContextoOB contexto){
        LogOB.evento(contexto,"SolicitudAceptacionMandatoEcheq","INICIO");
        String idCheque = contexto.parametros.string(ID_CHEQUE);
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        ListadoChequesOB listado = ApiCheques.getChequeById(contexto, idCheque, cuit).get();
        LogOB.evento(contexto,"SolicitudAceptacionMandatoEcheq", "ApiCheques.getChequeById ejecutada");
        ListadoChequesOB.cheques cheque = listado.result.cheques.get(0);
        if (listado.result.cheques.isEmpty()) {

            return new ErrorGenericoOB().setErrores("Cheque inexistente", "El cheque ingresado no existe");
        }
        if (!cheque.estado.equalsIgnoreCase("MANDATO-PENDIENTE")&&!cheque.solicitando_acuerdo) {

            return new ErrorGenericoOB().setErrores("Estado del cheque incorrecto", "El cheque posee un estado donde no se puede aceptar un mandato");
        }
        boolean cruzado = cheque.cheque_modo.equals("0");
        boolean caracter = cheque.cheque_caracter.equals("a la orden");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
        LocalDateTime fechaPago = LocalDateTime.parse(cheque.fecha_pago, formatter);
        ServicioEcheqOB servicioEcheqOB = new ServicioEcheqOB(contexto);
        EcheqOB echeq = servicioEcheqOB.cargarCreado(cheque.numero_chequera,BigDecimal.valueOf(cheque.monto),cuit,"CUIT",null,caracter,cheque.cheque_motivo_pago,cheque.cheque_concepto != null ? cheque.cheque_concepto : " ",cruzado,cheque.cuenta_emisora.emisor_cuenta,contexto.sesion().empresaOB.razonSocial,contexto.sesion().empresaOB, fechaPago,cheque.cheque_tipo,EnumAccionesEcheqOB.ACEPTACION_MANDATO_NEG,contexto.sesion().empresaOB.cuit.toString(),cheque.cheque_id,cheque.cheque_numero,null,null,null,null,contexto.sesion().usuarioOB).get();
        Objeto respuestaEcheq = new Objeto();
        respuestaEcheq.set("idOperacion", echeq.id);
        LogOB.evento(contexto,"SolicitudAceptacionMandatoEcheq", "FIN");
        return respuesta("0",DATOS,respuestaEcheq);
    }

    public static Objeto aceptarMandaroEcheq(ContextoOB contexto){
        LogOB.evento(contexto,"aceptarMandatoEcheq","INICIO");
        String idOperacion = contexto.parametros.string(ID_OPERACION);
        EcheqOB echeq = ServicioEcheqOB.find(Integer.parseInt(idOperacion)).get();
        if (empty(echeq)) {
            return new ErrorGenericoOB().setErrores("Cheque inexistente", "El cheque ingresado no existe");
        }
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        Objeto respuesta = new Objeto();
        MandatoNegociacionOB aceptacionMandatoEcheqOB = ApiCheques.aceptarMandatoNegociacionOB(contexto,echeq.idCheque,cuit).get();
        LogOB.evento(contexto,"aceptarMandatoEcheq", "ApiCheques.aceptarMandatoNegociacion ejecutada");

        respuesta.add(DATOS, aceptacionMandatoEcheqOB.result);
        respuesta.set("estado", "0");

        LogOB.evento(contexto,"aceptarMandatoEcheq","FIN");
        return respuesta;
    }

    public static Objeto solicitudMandatoEcheq(ContextoOB contexto){
        LogOB.evento(contexto,"SolicitudMandatoEcheq","INICIO");
        String idCheque = contexto.parametros.string(ID_CHEQUE);
        String mandatarioDocumento = contexto.parametros.string("mandatarioDocumento");
        String domicilio = contexto.parametros.string("mandatarioDomicilio");
        String email = contexto.parametros.string("email",null);
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        ListadoChequesOB listado= ApiCheques.getChequeById(contexto, idCheque, cuit).get();
        LogOB.evento(contexto,"SolicitudMandatoEcheq", "ApiCheques.getChequeById");
        if (listado.result.cheques.isEmpty()) {

            return new ErrorGenericoOB().setErrores("Cheque inexistente", "El cheque ingresado no existe");
        }
        if (domicilio.isBlank()){
            return new ErrorGenericoOB().setErrores("Domicilio invalido", "Debe ingresar un domicilio");
        }
        if (!listado.result.cheques.get(0).tenencia.beneficiario_documento.equals(cuit)||!listado.result.cheques.get(0).estado.equals(ACTIVO))  return new ErrorGenericoOB().setErrores("Estado invalido", "Debe tener tenencia del cheque y el mismo debe estar activo");
        EntidadesMercadoOB entidadesMercadoOB= ApiCheques.entidadesMercado(contexto).get();
        LogOB.evento(contexto,"solicitudEndosoEcheq", "ApiCheques.entidadesMercado ejecutada");
        Optional<EntidadesMercadoOB.infraestructurasMercado> entidadMercado = entidadesMercadoOB.result.infraestructurasMercado.stream().filter(entidad -> entidad.documento.equals(mandatarioDocumento)).findFirst();
        if (entidadMercado.isEmpty()) {
            return new ErrorGenericoOB().setErrores("El mandatario seleccionado no es valido.", "El mandatario seleccionado no es valido.");
        }
        DetalleRazonSocialOB detalleRazonSocial = ApiCheques.razonSocial(contexto, mandatarioDocumento).get();
        LogOB.evento(contexto,"SolicitudMandatoEcheq", "ApiCheques.razonSocial ejecutada");
        ServicioEcheqOB servicioEcheqOB = new ServicioEcheqOB(contexto);
        ListadoChequesOB.cheques cheque = listado.result.cheques.get(0);
        boolean cruzado = cheque.cheque_modo.equals("0");
        boolean caracter = cheque.cheque_caracter.equals("a la orden");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
        LocalDateTime fechaPago = LocalDateTime.parse(cheque.fecha_pago, formatter);
        EcheqOB echeq = servicioEcheqOB.cargarCreado(cheque.numero_chequera==null?"0":cheque.numero_chequera,BigDecimal.valueOf(cheque.monto),mandatarioDocumento,"cuit",email,caracter,cheque.cheque_motivo_pago,cheque.cheque_concepto,cruzado,cheque
                .cuenta_emisora.emisor_cuenta,detalleRazonSocial.result.beneficiario_razon_social,contexto.sesion().empresaOB,fechaPago,cheque.cheque_tipo,EnumAccionesEcheqOB.MANDATO_NEG,cuit,cheque.cheque_id,cheque.cheque_numero,null,null,domicilio,null,contexto.sesion().usuarioOB).get();
        Objeto respuestaEcheq = new Objeto();
        respuestaEcheq.set("idOperacion", echeq.id);
        LogOB.evento(contexto,"SolicitudMandatoEcheq", "FIN");
        return respuesta("0", DATOS, respuestaEcheq);
    }

    public static Objeto mandatoEcheq(ContextoOB contexto){
        LogOB.evento(contexto,"mandatoEcheq","INICIO");

        int id = contexto.parametros.integer(ID_OPERACION);
        EcheqOB echeq = ServicioEcheqOB.find(id).get();

        if (empty(echeq) || echeq == null) {
            return respuesta(DATOS_INVALIDOS);
        }
        String idCheque = echeq.idCheque;
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        ListadoChequesOB listado = ApiCheques.getChequeById(contexto, idCheque, cuit).get();
        LogOB.evento(contexto,"mandatoEcheq", "ApiCheques.getChequeById ejecutada");
        if (listado.result.cheques.isEmpty()) {
            return respuesta(DATOS_INVALIDOS);
        }
        Objeto respuesta = new Objeto();
        MandatoNegociacionOB mandatoNegociacionOB = ApiCheques.mandatoNegociacion(contexto,idCheque,echeq.documentoBeneficiario,echeq.cesionarioDomicilio).get();
        LogOB.evento(contexto,"mandatoEcheq", "ApiCheques.emitirMandatoNegociacion ejecuatda");
        respuesta.add(DATOS,mandatoNegociacionOB.result);
        LogOB.evento(contexto,"mandatoEcheq", "FIN");
        return respuesta("0", DATOS, respuesta);
    }

    public static Objeto solicitudRevocacionMandatoNegociacionEcheq(ContextoOB contexto){
        LogOB.evento(contexto,"SolicitudRevocacionMandatoEcheq","INICIO");
        String idCheque = contexto.parametros.string(ID_CHEQUE);
        String motivoRevocatoria = "a";
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        ListadoChequesOB listado = ApiCheques.getChequeById(contexto, idCheque, cuit).get();
        LogOB.evento(contexto,"SolicitudRevocacionMandatoEcheq", "ApiCheques.getChequeById ejecutada");
        if (listado.result.cheques.isEmpty()) {
            return respuesta(DATOS_INVALIDOS);
        }
        DetalleRazonSocialOB detalleRazonSocial = ApiCheques.razonSocial(contexto, cuit).get();
        LogOB.evento(contexto,"SolicitudRevocacionMandatoEcheq", "ApiCheques.razonSocial ejecutada");
        ServicioEcheqOB servicioEcheqOB = new ServicioEcheqOB(contexto);
        ListadoChequesOB.cheques cheque = listado.result.cheques.get(0);
        boolean cruzado = cheque.cheque_modo.equals("0");
        boolean caracter = cheque.cheque_caracter.equals("a la orden");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
        LocalDateTime fechaPago = LocalDateTime.parse(cheque.fecha_pago, formatter);
        EcheqOB echeq = servicioEcheqOB.cargarCreado(cheque.numero_chequera==null?"0":cheque.numero_chequera,BigDecimal.valueOf(cheque.monto),cuit,"cuit","",caracter,cheque.cheque_motivo_pago,cheque.cheque_concepto,cruzado,cheque.cuenta_emisora.emisor_cuenta,detalleRazonSocial.result.beneficiario_razon_social,contexto.sesion().empresaOB,fechaPago,cheque.cheque_tipo,EnumAccionesEcheqOB.REVOCATORIA_MANDATO,cuit,idCheque,cheque.cheque_numero,null,null,motivoRevocatoria,null,contexto.sesion().usuarioOB).get();
        Objeto respuestaEcheq = new Objeto();
        respuestaEcheq.set("idOperacion",echeq.id);
        LogOB.evento(contexto,"SolicitudRevocacionMandatoEcheq", "FIN");
        return respuesta("0", DATOS, respuestaEcheq);
    }

    public static Objeto revocarMandatoNegociacionEcheq(ContextoOB contexto){
        LogOB.evento(contexto,"revocarMandatoEcheq","INICIO");
        int id = contexto.parametros.integer(ID_OPERACION);
        EcheqOB echeq = ServicioEcheqOB.find(id).get();

        if (empty(echeq) || echeq == null) {
            return respuesta(DATOS_INVALIDOS);
        }
        String idCheque = echeq.idCheque;
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        ListadoChequesOB listado = ApiCheques.getChequeById(contexto, idCheque, cuit).get();
        LogOB.evento(contexto,"revocarMandatoEcheq", "ApiCheques.getChequeById ejecutada");
        if (listado.result.cheques.isEmpty()) {
            return respuesta(DATOS_INVALIDOS);
        }
        Objeto respuesta = new Objeto();
        MandatoNegociacionOB revocarMandato = ApiCheques.revocarMandatoNegociacion(contexto,idCheque,cuit,echeq.cesionarioDomicilio).get();
        LogOB.evento(contexto,"revocarMandatoEcheq", "ApiCheques.revocarMandatoNegociacionj ejecuatda");
        respuesta.add(DATOS,revocarMandato.result);
        LogOB.evento(contexto,"revocarMandatoEcheq","FIN");
        return respuesta("0",DATOS,respuesta);
    }
    public static String normalizarCadena(String in) {
        return in.replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u").replace("Á", "A").replace("É", "E").replace("Í", "I").replace("Ó", "O").replace("Ú", "U").replace("ñ", "n").replace("Ñ", "N").replace("&","y").replace("＆","y");
    }
}
