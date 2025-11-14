package ar.com.hipotecario.canal.officebanking;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.ApiCatalogo;
import ar.com.hipotecario.backend.servicio.api.catalogo.Bancos;
import ar.com.hipotecario.backend.servicio.api.catalogo.SucursalesOB;
import ar.com.hipotecario.backend.servicio.api.cuentas.ApiCuentas;
import ar.com.hipotecario.backend.servicio.api.cuentas.CajasAhorrosV1;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentaCoelsa;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentasCorrientes;
import ar.com.hipotecario.backend.servicio.api.debin.*;
import ar.com.hipotecario.backend.servicio.api.empresas.ApiEmpresas;
import ar.com.hipotecario.backend.servicio.api.empresas.CompletaFirmaOB;
import ar.com.hipotecario.backend.servicio.api.productos.ApiProductos;
import ar.com.hipotecario.backend.servicio.api.productos.CuentasOB;
import ar.com.hipotecario.backend.servicio.api.productos.Productos;
import ar.com.hipotecario.canal.officebanking.dto.ErrorGenericoOB;
import ar.com.hipotecario.canal.officebanking.enums.*;
import ar.com.hipotecario.canal.officebanking.enums.debin.EnumConceptosDebinOB;
import ar.com.hipotecario.canal.officebanking.enums.debin.EnumDebinVencimientoOB;
import ar.com.hipotecario.canal.officebanking.enums.debin.EnumEstadoDebinEnviadasOB;
import ar.com.hipotecario.canal.officebanking.enums.debin.EnumEstadoDebinRecibidasOB;
import ar.com.hipotecario.canal.officebanking.jpa.dto.InfoCuentaDTO;
import ar.com.hipotecario.canal.officebanking.jpa.ob.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.PermisoOperadorOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.TipoCuentaOB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.servicio.RestDebin;
import jakarta.persistence.EntityNotFoundException;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static ar.com.hipotecario.canal.officebanking.OBPagos.validarSaldoYCuenta;

public class OBDebin extends ModuloOB {
    private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
    static ServicioDebinOB servicioDebinOB = new ServicioDebinOB(contexto);
    static ServicioDebinProgramadoOB servicioDebinProgramadoOB = new ServicioDebinProgramadoOB(contexto);
    static ServicioEstadoDebinRecibidasOB servicioEstadoDebinRecibidasOB = new ServicioEstadoDebinRecibidasOB(contexto);
    static ServicioEstadoDebinEnviadasOB servicioEstadoDebinEnviadasOB = new ServicioEstadoDebinEnviadasOB(contexto);
    static ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
    static ServicioHistorialDebinOB servicioHistorialDebinOB = new ServicioHistorialDebinOB(contexto);
    static ServicioAccionesOB servicioAccionesOB = new ServicioAccionesOB(contexto);
    static ServicioBancoOB servicioBancoOB = new ServicioBancoOB(contexto);
    static ServicioBeneficiarioOB servicioBeneficiarioOB = new ServicioBeneficiarioOB(contexto);
    static ServicioMonedaOB servicioMoneda = new ServicioMonedaOB(contexto);
    static ServicioConceptoDebinOB servicioConcepto = new ServicioConceptoDebinOB(contexto);
    static ServicioBandejaOB servicioBandejaOB = new ServicioBandejaOB(contexto);
    static ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
    static EstadoDebinRecibidasOB estadoDebinRecibidaVencida = servicioEstadoDebinRecibidasOB.find(EnumEstadoDebinRecibidasOB.VENCIDO.getCodigo()).get();
    static EstadoDebinEnviadasOB estadoDebinEnviadaVencida = servicioEstadoDebinEnviadasOB.find(EnumEstadoDebinEnviadasOB.VENCIDO.getCodigo()).get();
    static EstadoDebinEnviadasOB estadoDebinEnviadaIniciada = servicioEstadoDebinEnviadasOB.find(EnumEstadoDebinEnviadasOB.INICIADO.getCodigo()).get();
    static EstadoDebinRecibidasOB estadoDebinRecibidaPendiente = servicioEstadoDebinRecibidasOB.find(EnumEstadoDebinRecibidasOB.ACEPTAR_O_RECHAZAR_DEBIN.getCodigo()).get();
    static EstadoDebinRecibidasOB estadoDebinRecibidaEnBandeja = servicioEstadoDebinRecibidasOB.find(EnumEstadoDebinRecibidasOB.EN_BANDEJA.getCodigo()).get();
    static EstadoBandejaOB estadoBandejaRechazada = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();

    public static Object listarCuentas(ContextoOB contexto) {
        LogOB.evento(contexto, "listarCuentas", "INICIO");
        String idCobis = contexto.sesion().empresaOB.idCobis;
        SesionOB sesion = contexto.sesion();
        List<Objeto> productos = new ArrayList<>();


        Productos listaProductos = ApiProductos.productos(contexto, idCobis).tryGet();
        LogOB.evento(contexto, "listarCuentas", "Cuentas cargadas");
        if (listaProductos == null) return respuesta("datos", productos);
        else {
            for (Productos.Producto producto : listaProductos) {
                if (!producto.tipo.equals("CTE") && !producto.tipo.equals("AHO")) {
                    continue;
                }
                if (!producto.estado.codigo.equals("B") && !producto.estado.codigo.equals("C")) {
                    Objeto cuenta = new Objeto();
                    EndpointVendedores cuentasAdheridas = ApiDebin.getVendedor(contexto, String.valueOf(sesion.empresaOB.cuit)).tryGet();
                    LogOB.evento(contexto, "listarCuentas", "Cargar cuentas adheridas");
                    cuenta.set("producto", producto.tipo);
                    cuenta.set("nroCuenta", producto.numero);

                    boolean cuentaHabilitada = false;

                    if (producto.tipo.equals("CTE")) {
                        CuentasCorrientes.CuentaCorriente cuentaCC = ApiCuentas.cuentaCorriente(contexto, producto.numero, Fecha.hoy(), false, false).tryGet();
                        LogOB.evento(contexto, "listarCuentas", "Carga cuentas corrientes");

                        if (cuentaCC != null) {
                            cuenta.set("cbu", cuentaCC.cbu);
                            cuenta.set("saldo", cuentaCC.saldoGirar);
                            cuenta.set("moneda", cuentaCC.moneda);
                            if (cuentasAdheridas != null) {
                                for (Cuenta v : cuentasAdheridas.cuentas) {
                                    if (v.cbu.equals(cuentaCC.cbu)) {
                                        cuentaHabilitada = true;
                                        break;
                                    }
                                }
                            }
                        }
                    } else if (producto.tipo.equals("AHO")) {
                        CajasAhorrosV1.CajaAhorroV1 cajaAhorro = ApiCuentas.cajaAhorroV1(contexto, producto.numero, Fecha.hoy()).tryGet();
                        LogOB.evento(contexto, "listarCuentas", "Cargar cajas de ahorro");

                        if (cajaAhorro != null) {
                            cuenta.set("cbu", cajaAhorro.cbu);
                            cuenta.set("saldo", cajaAhorro.saldoGirar);
                            cuenta.set("moneda", cajaAhorro.moneda);
                            if (cuentasAdheridas != null) {
                                for (Cuenta v : cuentasAdheridas.cuentas) {
                                    if (v.cbu.equals(cajaAhorro.cbu)) {
                                        cuentaHabilitada = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    cuenta.set("estadoCuenta", cuentaHabilitada ? "habilitada" : "deshabilitada");
                    productos.add(cuenta);
                }

            }

        }
        LogOB.evento(contexto, "listarCuentas", "FIN");

        return respuesta("datos", productos);
    }

    public static Object validarInfoDebito(ContextoOB contexto) {
        LogOB.evento(contexto, "validarInfoDebito", "INICIO");

        String cbuDebito = contexto.parametros.string("cbu", null);
        String alias = contexto.parametros.string("alias", null);

        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, contexto.sesion().empresaOB, contexto.sesion().usuarioOB);

        ServicioPermisoOB servicioPermisoOB = new ServicioPermisoOB(contexto);
        if (!contexto.sesion().esOperadorInicial()) {
            ServicioPermisoOperadorOB servicioPermisoOperadorOB = new ServicioPermisoOperadorOB(contexto);
            PermisoOperadorOB permiso = servicioPermisoOperadorOB.buscarPermiso(empresaUsuario, servicioPermisoOB.find(17).get()).tryGet();
            if (empty(permiso)) {
                return new ErrorGenericoOB().setErrores("OPERACION_INVALIDA", "La operación es inválida");
            }
        }

        if (cbuDebito != null && alias != null) {
            return new ErrorGenericoOB().setErrores("Cbu o alias", "Debe ingresar cbu o alias.");
        }


        if (!cbuValido(cbuDebito) && cbuDebito != null) {
            return new ErrorGenericoOB().setErrores("CBU_INVALIDO", "El cbu es inválido");
        }

        if (alias != null && !aliasValido(alias)) {
            return new ErrorGenericoOB().setErrores("ALIAS_INVALIDO", "El alias es inválido");
        } else if (cbuDebito != null && cbuDebito.startsWith("000")) {
            return new ErrorGenericoOB().setErrores("CVU_NO_PERMITIDO", "No se permite cvu.");
        }

        InfoCuentaDTO infoCuentaComprador = servicioBeneficiarioOB.infoCBUAlias(contexto, cbuDebito != null ? cbuDebito : alias);

        if (empty(infoCuentaComprador.cuenta)) {
            return new ErrorGenericoOB().setErrores("CBU_INVALIDO", "El cbu es inválido.");
        }

        CuentaCoelsa cuenta = infoCuentaComprador.cuenta;
        if (!cuenta.ctaActiva) {
            return new ErrorGenericoOB().setErrores("CUENTA_INACTIVA", "La cuenta está inactiva.");
        }

        Objeto datos = new Objeto();
        datos.set("nombre", cuenta.nombreTitular);
        datos.set("cbu", cuenta.cbu);
        datos.set("alias", infoCuentaComprador.cuenta.nuevoAlias);
        datos.set("cuit", cuenta.cuit);

        Bancos.Banco datosBanco = ApiCatalogo.bancos(contexto, cuenta.nroBco).get();
        datos.set("bancoDestino", datosBanco.Descripcion);
        LogOB.evento(contexto, "validarInfoDebito", "Carga de datos banco");


        Objeto lstMonedas = new Objeto();
        for (MonedaOB moneda : infoCuentaComprador.monedas) {
            Objeto mon = new Objeto();
            mon.set("codigo", moneda.codigoCobis);
            mon.set("simbolo", moneda.simbolo);
            mon.set("descripcion", moneda.descripcion);
            lstMonedas.add(mon);
        }
        datos.set("monedas", lstMonedas);

        // MAXI MIRAR TIPO CUNETA 2
        TipoCuentaOB tipoCuenta = cuenta.tipoCuenta.equals("CTE") || cuenta.tipoCuenta.equals("CC") ? TipoCuentaOB.CC : TipoCuentaOB.CA;

        Objeto tcv = new Objeto();
        tcv.set("id", tipoCuenta.ordinal());
        tcv.set("descripcionCorta", tipoCuenta.name());
        tcv.set("descripcionLarga", tipoCuenta.getDescripcionLarga());
        datos.set("tipoCuenta", tcv);

        LogOB.evento(contexto, "validarInfoDebito", "FIN");

        return respuesta("datos", datos);
    }


    public static Object solicitarDebin(ContextoOB contexto) {
        LogOB.evento(contexto, "solicitarDebin", "INICIO");

        Objeto datos = new Objeto();
        String cuentaCredito = contexto.parametros.string("cuentaCredito");
        String cbuDebito = contexto.parametros.string("cbu");
        Integer idMoneda = contexto.parametros.integer("moneda");
        BigDecimal monto = contexto.parametros.bigDecimal("monto");
        Integer vencimiento = contexto.parametros.integer("vencimiento");
        String referencia = contexto.parametros.string("referencia", null);
        Integer idConcepto = contexto.parametros.integer("concepto");

        SesionOB sesion = contexto.sesion();

        if (monto.signum() != 1) {
            return new ErrorGenericoOB().setErrores("MONTO_INVALIDO", "El monto es inválido");
        }

        ServicioEstadoBandejaOB servicioEstadoBandejaOB = new ServicioEstadoBandejaOB(contexto);
        ServicioEstadoDebinRecibidasOB servicioEstadoDebinRecibidasOB = new ServicioEstadoDebinRecibidasOB(contexto);
        EstadoBandejaOB estadoInicialBandeja = servicioEstadoBandejaOB.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
        EstadoDebinRecibidasOB estadoRecibidaAceptarORechazar = servicioEstadoDebinRecibidasOB.find(EnumEstadoDebinRecibidasOB.ACEPTAR_O_RECHAZAR_DEBIN.getCodigo()).get();

        ConceptoDebinOB concepto = servicioConcepto.find(idConcepto).get();
        MonedaOB moneda = servicioMoneda.find(idMoneda).get();

        CuentasOB cuentas = ApiCuentas.cuentas(contexto, contexto.sesion().empresaOB.idCobis).get();
        LogOB.evento(contexto, "solicitarDebin", "Carga de cuentas");
        CuentasOB.CuentaOB cuentaOBCredito = cuentas.stream().filter(c -> c.numeroProducto.equals(cuentaCredito)).findFirst().get();


        SucursalesOB sucursales = ApiCatalogo.sucursalesOB(contexto, null, cuentaOBCredito.sucursal, null).get();
        LogOB.evento(contexto, "solicitarDebin", "Carga de sucursales");

        Integer tiempoExpiracion = vencimiento * 60;

        String descMoneda = idMoneda == 80 ? "Pesos" : "Dolar";
        String signoMoneda = idMoneda == 80 ? "$" : "U$D";
        Moneda m = new Moneda(idMoneda.toString(), descMoneda, signoMoneda);

        InfoCuentaDTO infoCuentaComprador = servicioBeneficiarioOB.infoCBUAlias(contexto, cbuDebito);
        InfoCuentaDTO infoCuentaVendedor = servicioBeneficiarioOB.infoCBUAlias(contexto, cuentaOBCredito.cbu);

        try {
            if (infoCuentaVendedor == null) throw new Exception("InfoCuentaVendedor null");
            if (!Arrays.stream(EnumDebinVencimientoOB.values()).map(EnumDebinVencimientoOB::getCodigo).toList().contains(vencimiento))
                throw new Exception("Vencimiento incorrecto");
            AltaDEBINResponse altaDebin = ApiDebin.altaDebin(contexto,
                    cbuDebito,
                    infoCuentaComprador.cuenta.cuit,
                    Normalizer.normalize(concepto.descripcion.substring(0, 3).toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""),
                    "044",
                    sucursales.get(0).CodSucursal,
                    sucursales.get(0).DesSucursal,
                    monto,
                    m,
                    false,
                    tiempoExpiracion.toString(),
                    cuentaOBCredito.cbu,
                    sesion.empresaOB.cuit.toString()
            ).get();
            LogOB.evento(contexto, "solicitarDebin", new Objeto().set("altaDebin: ", altaDebin));

            DebinOB debinAGuardar = new DebinOB();
            debinAGuardar.estadoEnviada = estadoDebinEnviadaIniciada;
            debinAGuardar.idDebin = altaDebin.debin.id;
            debinAGuardar.cbuComprador = cbuDebito;
            debinAGuardar.cuentaComprador = infoCuentaComprador.numero;
            debinAGuardar.idTributarioComprador = infoCuentaComprador.cuenta.cuit;
            debinAGuardar.nombreComprador = infoCuentaComprador.cuenta.nombreTitular;
            debinAGuardar.tipoCuentaComprador = infoCuentaComprador.cuenta.tipoCuenta;
            debinAGuardar.sucursalDescVendedor = sucursales.get(0).DesSucursal;
            debinAGuardar.sucursalIdVendedor = sucursales.get(0).CodSucursal;

            DebinOB debinOB = servicioDebinOB.enviarSolicitud(contexto, altaDebin.debin.fechaExpiracion, moneda, monto, concepto, referencia, debinAGuardar, altaDebin.debin.fechaAlta, infoCuentaComprador, cuentaOBCredito.numeroProducto, infoCuentaVendedor.cuenta.cuit).get();

            AccionesOB accionCrear = servicioAccionesOB.find(EnumAccionesOB.CREAR.getCodigo()).get();
            BandejaOB bandeja = servicioBandejaOB.find(debinOB.id).get();
            EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, contexto.sesion().empresaOB, contexto.sesion().usuarioOB);

            servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionCrear, estadoInicialBandeja, estadoInicialBandeja);
            servicioHistorialDebinOB.cambiaEstado(debinOB, accionCrear, empresaUsuario, estadoDebinEnviadaIniciada, estadoDebinEnviadaIniciada, estadoRecibidaAceptarORechazar, estadoRecibidaAceptarORechazar);

            datos.set("idOperacion", bandeja.id);
            datos.set("importe", bandeja.monto);
            datos.set("nombreComprador", debinOB.nombreComprador.trim());
            datos.set("cuentaDebito", debinOB.cuentaComprador);
            datos.set("cbuDebito", cbuDebito);
            datos.set("bancoDebito", servicioBancoOB.find(Integer.valueOf(infoCuentaComprador.cuenta.nroBco)).get().denominacion);
            datos.set("cuentaCredito", cuentaOBCredito.numeroProducto);
            datos.set("cbuCredito", cuentaOBCredito.cbu);
            datos.set("bancoCredito", "BANCO HIPOTECARIO S.A.");

        } catch (Exception e) {
            LogOB.evento(contexto, "altaDebin", new Objeto().set("error", e.getMessage()));
            return new ErrorGenericoOB().setErrores("Error al crear el debin.", "Fallo la carga del debin.");
        }
        LogOB.evento(contexto, "solicitarDebin", "FIN");
        return respuesta("datos", datos);
    }

    public static Object habilitarCuenta(ContextoOB contexto) {
        LogOB.evento(contexto, "habilitarCuenta", "INICIO");
        String numeroCuenta = contexto.parametros.string("numeroCuenta");
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        try {
            ApiProductos.productos(contexto, contexto.sesion().empresaOB.idCobis)
                    .get()
                    .stream()
                    .filter(producto -> producto.numero.equals(numeroCuenta))
                    .findFirst().ifPresentOrElse(producto -> {
                        String cbu = producto.tipo.equals("CTE") ? ApiCuentas.cuentaCorriente(contexto, producto.numero, Fecha.hoy(), false, false).get().cbu : ApiCuentas.cajaAhorroV1(contexto, producto.numero, Fecha.hoy()).get().cbu;
                        ApiDebin.altaVendedor(contexto, "email", cbu, producto.sucursal.codigo, producto.sucursal.descripcion, cuit).get();
                    }, () -> {
                        throw new EntityNotFoundException();
                    });
            LogOB.evento(contexto, "habilitarCuenta", "Cuenta habilitada");
        } catch (EntityNotFoundException e) {
            return new ErrorGenericoOB().setErrores("Error al habilitar la cuenta.", "No se encontro la cuenta solicitada");
        } catch (Exception e) {
            LogOB.evento(contexto, "habilitarCuenta", new Objeto().set("error", e.getMessage()));
            return new ErrorGenericoOB().setErrores("Error al habilitar la cuenta.", e.getMessage());

        }
        LogOB.evento(contexto, "habilitarCuenta", "FIN");

        return respuesta("0", "datos", "Cuenta habilitada");
    }

    public static Object deshabilitarCuenta(ContextoOB contexto) {
        LogOB.evento(contexto, "deshabilitarCuenta", "INICIO");

        String cbu = contexto.parametros.string("cbu");

        Objeto datos = new Objeto();

        EndpointVendedores.RespuestaOk bajaAdhesionCuenta = ApiDebin.deleteVendedor(contexto, contexto.sesion().empresaOB.cuit.toString(), cbu).tryGet();
        LogOB.evento(contexto, "deshabilitarCuenta", "Baja adhesion cuenta");

        if (bajaAdhesionCuenta != null) {
            datos.set("CuentaDeshabilitada", bajaAdhesionCuenta);
        }
        LogOB.evento(contexto, "deshabilitarCuenta", "FIN");

        return respuesta("0", "datos", datos);

    }


    public static Object rechazarDebin(ContextoOB contexto) {
        LogOB.evento(contexto, "rechazarDebin", "INICIO");

        String idDebin = contexto.parametros.string("idDebin");


        Objeto datos = new Objeto();
        DebinOB debinFinal;

        DebinOB debin = servicioDebinOB.findByIdDebin(idDebin).tryGet();

        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, contexto.sesion().empresaOB, contexto.sesion().usuarioOB);

        if (debin != null) {
            debinFinal = debin;
        } else {
            try {
                debinFinal = guardarDebinExterno(contexto, idDebin, null);
            } catch (Exception e) {
                return new ErrorGenericoOB().setErrores("Error al consultar debin", e.getMessage());
            }
        }

        Debin debinRequest = new Debin();
        debinRequest.setId(debinFinal.idDebin);
        debinRequest.setImporte(debinFinal.monto);

        Comprador compradorRequest = new Comprador();
        Cliente clienteRequest = new Cliente();
        clienteRequest.setIdTributario(debinFinal.idTributarioComprador);
        clienteRequest.setNombreCompleto(debinFinal.nombreComprador);
        Cuenta cuentaRequest = new Cuenta();
        cuentaRequest.setCbu(debinFinal.cbuComprador);
        cuentaRequest.setNumero(debinFinal.cuentaComprador);
        cuentaRequest.setTipo(debinFinal.tipoCuentaComprador);
        cuentaRequest.setSucursal(new Sucursal(debinFinal.sucursalIdVendedor, debinFinal.sucursalDescVendedor));
        cuentaRequest.setMoneda(new Moneda(debinFinal.moneda.id.toString(), debinFinal.moneda.descripcion, debinFinal.moneda.simbolo));
        clienteRequest.setCuenta(cuentaRequest);
        compradorRequest.setCliente(clienteRequest);
        debinRequest.comprador = compradorRequest;

        try {
            ApiDebin.postAutorizar(contexto, debinRequest, "RECHAZAR");
            LogOB.evento(contexto, "rechazarDebin", "PostAutorizar ejecutada");
        } catch (Exception e) {
            LogOB.evento(contexto, "rechazarDebin", new Objeto().set("error", e.getMessage()));
            return new ErrorGenericoOB().setErrores("Error al rechazar el debin.", e.getMessage());
        }

        AccionesOB accionRechazar = servicioAccionesOB.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();
        EstadoBandejaOB estadoRechazadoEnFirma = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        EstadoDebinRecibidasOB estadoPendiente = servicioEstadoDebinRecibidasOB.find(EnumEstadoDebinRecibidasOB.ACEPTAR_O_RECHAZAR_DEBIN.getCodigo()).get();
        EstadoDebinRecibidasOB estadoRechazado = servicioEstadoDebinRecibidasOB.find(EnumEstadoDebinRecibidasOB.RECHAZADO.getCodigo()).get();
        EstadoDebinEnviadasOB estadoPendienteEnv = servicioEstadoDebinEnviadasOB.find(EnumEstadoDebinEnviadasOB.INICIADO.getCodigo()).get();
        EstadoDebinEnviadasOB estadoRechazadoEnv = servicioEstadoDebinEnviadasOB.find(EnumEstadoDebinEnviadasOB.RECHAZADO.getCodigo()).get();

        BandejaOB bandeja = servicioBandejaOB.find(debinFinal.id).get();
        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionRechazar, estadoRechazadoEnFirma, estadoRechazadoEnFirma).get();

        servicioHistorialDebinOB.cambiaEstado(debinFinal, accionRechazar, empresaUsuario, estadoPendienteEnv, estadoRechazadoEnv, estadoPendiente, estadoRechazado).get();

        debinFinal.estadoRecibida = estadoRechazado;
        debinFinal.estadoEnviada = estadoRechazadoEnv;
        debinFinal.estadoBandeja = estadoRechazadoEnFirma;
        servicioDebinOB.update(debinFinal);

        datos.set("monto", debinFinal.monto);
        datos.set("comprador", debinFinal.nombreComprador);
        LogOB.evento(contexto, "rechazarDebin", "FIN");

        return respuesta().set("datos", datos);
    }


    public static Object verDetalles(ContextoOB contexto) {
        LogOB.evento(contexto, "verDetalles", "INICIO");
        Integer idOperacion = contexto.parametros.integer("idOperacion", null);
        String idDebin = contexto.parametros.string("idDebin", null);
        String filtro = contexto.parametros.string("tipoSolicitud",null);

        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);

        Objeto dato = new Objeto();
        DebinOB debin = null;

        if (idDebin==null) {
            debin = servicioDebinOB.find(idOperacion).get();
            if (debin==null) return new ErrorGenericoOB().setErrores("Error al buscar Debin", "Debin inexistente");
            idDebin = debin.idDebin;
        }
        try{
            String regexNumerico = "^[-+]?\\d*(\\.\\d+)?$";
            ConsultarDebin.ConsultaDebinResponse detalle = ApiDebin.consultarDebin(contexto, idDebin).tryGet();
            DecimalFormat decimalFormat = new DecimalFormat("#,##0.###");
            LogOB.evento(contexto, "verDetalles", "consultar debin ejecutada");
            dato.set("nombreVendedor", detalle.vendedor.cliente.nombreCompleto);
            dato.set("importe", detalle.detalle.importe.contains("E")?decimalFormat.format(new BigDecimal(detalle.detalle.importe)).toString():detalle.detalle.importe);
            dato.set("cuitVendedor", detalle.vendedor.cliente.idTributario);
            dato.set("cuentaDebito", detalle.comprador.cliente.cuenta.numero==null? servicioBeneficiarioOB.infoCBUAlias(contexto, detalle.comprador.cliente.cuenta.cbu).numero:detalle.comprador.cliente.cuenta.numero);
            dato.set("bancoDebito", ApiCatalogo.bancos(contexto,detalle.comprador.cliente.cuenta.banco).get().Descripcion);
            CuentasOB cuentasOB = ApiCuentas.cuentas(contexto, contexto.sesion().empresaOB.idCobis).get();
            dato.set("esUltimoFirmante",false);
            cuentasOB.stream().filter(cuenta->cuenta.cbu.equals(detalle.comprador.cliente.cuenta.cbu)).forEach(cuenta->{
                dato.set("saldoCuenta",cuenta.acuerdo!=null&&cuenta.acuerdo.matches(regexNumerico)?String.valueOf(Double.valueOf(cuenta.disponible)+Double.valueOf(cuenta.acuerdo)):cuenta.disponible);
                try{
                    CompletaFirmaOB grupoOB = ApiEmpresas.completaFirma(contexto, contexto.sesion().empresaOB.cuit.toString(), cuenta.numeroProducto, detalle.detalle.importe, contexto.sesion().usuarioOB.cuil.toString(), "", String.valueOf(EnumTipoProductoOB.DEBIN.getCodigo())).get();
                    dato.set("esUltimoFirmante",grupoOB.codigo.equals("250"));
                }catch (ApiException ignored){
                }

            });

            dato.set("nombreCredito", detalle.vendedor.cliente.nombreCompleto);
            dato.set("bancoCredito", ApiCatalogo.bancos(contexto,detalle.vendedor.cliente.cuenta.banco).get().Descripcion);
            dato.set("cbuCredito", detalle.vendedor.cliente.cuenta.cbu);
            dato.set("vendedor-cuenta", detalle.vendedor.cliente.cuenta.numero);
            dato.set("cbuDebito",detalle.comprador.cliente.cuenta.cbu);
            switch (detalle.detalle.moneda.id) {
                case "2" ->  dato.set("moneda", EnumMonedasOB.DOLARES.name());
                case "80" -> dato.set("moneda", EnumMonedasOB.PESOS.name());
            }

            if (detalle.detalle.concepto != null) {
                switch (detalle.detalle.concepto) {
                    case "ALQ" -> dato.set("concepto", "ALQUILERES");
                    case "CUO" -> dato.set("concepto", "CUOTAS");
                    case "EXP" -> dato.set("concepto", "EXPENSAS");
                    case "FAC" -> dato.set("concepto", "FACTURAS");
                    case "HON" -> dato.set("concepto", "HONORARIOS");
                    case "PRE" -> dato.set("concepto", "PRESTAMOS");
                    case "SEG" -> dato.set("concepto", "SEGUROS");
                    case "HAB" -> dato.set("concepto", "HABERES");
                }
            }
            if (idOperacion==null&&idDebin!=null){
                String estado = null;
                switch (detalle.estado.codigo) {
                    case "ACREDITADO" -> estado = "Realizado";
                    case "VENCIDO" -> estado = "Vencido";
                    case "RECHAZO DE CLIENTE", "SIN GARANTIA", "ERROR DATOS", "ERROR DEBITO", "ERROR ACREDITACION" -> estado = "Rechazado";
                    case "INICIADO" -> estado = "ACEPTAR O RECHAZAR DEBIN";
                    default -> estado=detalle.estado.codigo;
                }
                dato.set("estado",estado);
            }else{
                dato.set("estado", detalle.estado.codigo);
            }

            if (idOperacion != null) {
                BandejaOB bandeja = servicioBandeja.find(idOperacion).get();

                //if(filtro.equalsIgnoreCase("recibidas")){

                dato.set("firmas", OBFirmas.obtenerDatosFirmantes(contexto, bandeja));
                //}

                DebinOB debinOB = servicioDebinOB.find(idOperacion).get();
                dato.set("cuentaDebito", debinOB.cuentaComprador);
                dato.set("vendedor-cuenta", debinOB.cuentaVendedor);
                dato.set("concepto", debinOB.concepto.descripcion);
                dato.set("idOperacion",idOperacion);
                if (filtro!=null){
                    dato.set("estado", filtro.equalsIgnoreCase("recibidas")
                            ?mapearEstadoDebin(debinOB)
                            :
                            switch (detalle.estado.codigo) {
                                case "ACREDITADO" -> "ACREDITADO";
                                case "VENCIDO" -> "Vencido";
                                case "RECHAZO DE CLIENTE", "SIN GARANTIA", "ERROR DATOS", "ERROR DEBITO", "ERROR ACREDITACION" -> "Rechazado";
                                case "INICIADO" -> "INICIADO";
                                default ->detalle.estado.codigo;
                            }
                    );//debinOB.estadoEnviada.descripcion);
                } else{
                    dato.set("estado",mapearEstadoDebin(debinOB));
                }
            }

        } catch (Exception e) {
            LogOB.evento(contexto, "verDetale", new Objeto().set("error", e.getMessage()));
            return new ErrorGenericoOB().setErrores("Error al ver detalle, idDebin invalido.", e.getMessage());        }

        LogOB.evento(contexto, "verDetalles", "FIN");

        return respuesta("datos", dato);
    }

    public static Object vencimientos(ContextoOB contexto) {
        LogOB.evento(contexto, "vencimientos", "INICIO");
        Objeto datos = new Objeto();
        for (EnumDebinVencimientoOB v : EnumDebinVencimientoOB.values()) {
            datos.add(v.getCodigo());
        }
        LogOB.evento(contexto, "vencimientos", "FIN");
        return respuesta("datos", datos);
    }

    public static Object listarRecibidosGenerados(ContextoOB contexto) {
        LogOB.evento(contexto, "listarRecibidosGenerados", "INICIO");

        String filtro = contexto.parametros.string("tipoSolicitud");

        String banco = "044";
        String idTributarioComprador = null;
        String idTributarioVendedor = null;

        switch (filtro.toUpperCase()) {
            case "RECIBIDAS" -> idTributarioComprador = contexto.sesion().empresaOB.cuit.toString();
            case "ENVIADAS" -> idTributarioVendedor = contexto.sesion().empresaOB.cuit.toString();
        }

        String fechaDesde = LocalDate.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fechaHasta = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        Objeto datos = new Objeto();
        try {
            ListarDebin.ListarDebinResponse listaDebin = ApiDebin.listarDebin(contexto, idTributarioComprador, banco, fechaDesde, fechaHasta, idTributarioVendedor).tryGet();
            LogOB.evento(contexto, "listarRecibidosGenerados", "Listar Debines");
            if (listaDebin != null) {
                List<Debin> listaOrdenada = listaDebin.debins.stream().sorted(Comparator.comparing(Debin::getFechaExpiracion).reversed().thenComparing(Debin::getEstadoCodigo)).toList();
                for (Debin d : listaOrdenada) {
                    Objeto dato = new Objeto();
                    DebinOB debinOB = servicioDebinOB.findByIdDebin(d.id).tryGet();
                    String estado = null;
                    String moneda = null;
                    String idBandeja = null;
                    String id;
                    String vencimiento;
                    String cuitVendedor;

                    if (debinOB != null) {
                        if (debinOB.sucursalIdVendedor != null && debinOB.tipoCuentaComprador != null) {
                            if (debinOB.sucursalIdVendedor.equals("-1") && debinOB.tipoCuentaComprador.equals("PP")) {
                                continue;
                            }
                        }
                        if (!LocalDateTime.now().isBefore(OffsetDateTime.parse(debinOB.vencimiento).toLocalDateTime()) && debinOB.estadoRecibida.id != EnumEstadoDebinRecibidasOB.REALIZADO.getCodigo()) {
                            vencerDebin(debinOB);
                        }
                        estado = filtro.equalsIgnoreCase("enviadas") ?
                                switch (d.estado.codigo) {
                                    case "ACREDITADO" -> estado = "ACREDITADO";
                                    case "VENCIDO" -> estado = "Vencido";
                                    case "RECHAZO DE CLIENTE", "SIN GARANTIA", "ERROR DATOS", "ERROR DEBITO", "ERROR ACREDITACION" ->
                                            estado = "Rechazado";
                                    case "INICIADO" -> estado = "INICIADO";
                                    default -> d.estado.codigo;
                                }
                                : mapearEstadoDebin(debinOB);
                        idBandeja = String.valueOf(debinOB.id);
                        vencimiento = debinOB.vencimiento.substring(0, 16).replaceAll("T", " ");
                        cuitVendedor = filtro.toUpperCase().equals("RECIBIDAS") ? debinOB.cuitVendedor : d.comprador.cliente.idTributario;
                        id = debinOB.idDebin;
                    } else {
                        if (filtro.toUpperCase().equals("RECIBIDAS")) {
                            switch (d.estado.codigo) {
                                case "ACREDITADO" -> estado = "Realizado";
                                case "VENCIDO" -> estado = "Vencido";
                                case "RECHAZO DE CLIENTE", "SIN GARANTIA", "ERROR DATOS", "ERROR DEBITO", "ERROR ACREDITACION" ->
                                        estado = "Rechazado";
                                case "INICIADO" -> estado = "ACEPTAR O RECHAZAR DEBIN";
                                default -> estado = d.estado.codigo;
                            }
                        } else {
                            if (d.estado.codigo.equalsIgnoreCase("RECHAZO DE CLIENTE")
                                    || d.estado.codigo.equalsIgnoreCase("SIN GARANTIA")
                                    || d.estado.codigo.equalsIgnoreCase("ERROR DATOS")
                                    || d.estado.codigo.equalsIgnoreCase("ERROR DEBITO")) {
                                estado = "Rechazado";
                            } else estado = d.estado.codigo;

                        }

                        cuitVendedor = filtro.toUpperCase().equals("RECIBIDAS") ? d.vendedor.cliente.idTributario : d.comprador.cliente.idTributario;
                        vencimiento = d.fechaExpiracion.substring(0, 16).replaceAll("T", " ");
                        id = d.id;
                    }
                    if (d.vendedor.cliente.nombreCompleto != null)
                        dato.set("nombreVendedor", filtro.toUpperCase().equals("RECIBIDAS")
                                ? d.vendedor.cliente.nombreCompleto.trim()
                                : d.comprador.cliente.nombreCompleto != null ?
                                d.comprador.cliente.nombreCompleto.trim()
                                : "");

                    dato.set("estado", estado);
                    dato.set("importe", d.importe);
                    if (d.moneda.signo != null) {
                        moneda = d.moneda.signo;

                    } else if (d.moneda.id != null) {
                        switch (d.moneda.id) {
                            case "2" -> moneda = EnumMonedasOB.DOLARES.name();
                            case "80" -> moneda = EnumMonedasOB.PESOS.name();
                        }

                    }
                    dato.set("idDebin", id);
                    dato.set("idOperacion", idBandeja);
                    dato.set("moneda", moneda);
                    dato.set("vencimiento", vencimiento);
                    dato.set("cuitVendedor", cuitVendedor);
                    datos.add(dato);
                }
            }
        } catch (EntityNotFoundException e) {
            return new ErrorGenericoOB().setErrores("Error al listar Debin", "Lista Debin vacia");
        } catch (Exception e) {
            LogOB.evento(contexto, "listarRecibidosGenerados", new Objeto().set("error", e.getMessage()));
            return new ErrorGenericoOB().setErrores("Error al listar debines.", e.getMessage());
        }
        LogOB.evento(contexto, "listarRecibidosGenerados", "FIN");

        return respuesta("datos", datos);
    }

    public static Object listarRecibidosGeneradosProgramados(ContextoOB contexto) {
        LogOB.evento(contexto, "listarRecibidosGenerados", "INICIO");

        String filtro = contexto.parametros.string("tipoSolicitud");

        String banco = "044";
        String idTributarioComprador = null;
        String idTributarioVendedor = null;

        switch (filtro.toUpperCase()) {
            case "RECIBIDAS" -> idTributarioComprador = contexto.sesion().empresaOB.cuit.toString();
            case "ENVIADAS" -> idTributarioVendedor = contexto.sesion().empresaOB.cuit.toString();
        }

        String fechaDesde ="1900/01/01";
        String fechaHasta = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        Objeto datos = new Objeto();
        try {
            ListarDebinProgramado listaDebin = ApiDebin.listarDebinProgramado(contexto, idTributarioComprador, banco, fechaDesde, fechaHasta, idTributarioVendedor).tryGet();

            LogOB.evento(contexto, "listarRecibidosGenerados", "Listar Debines");
            if (listaDebin != null) {
                List<ListarDebinProgramado.Recurrencia> listaOrdenada = listaDebin.result.recurrencia.stream()
                        .sorted(Comparator.comparing(ListarDebinProgramado.Recurrencia::getFechaCreacion).reversed())
                        .toList();
                Map<String, Futuro<DebinProgramadoOB>> debinesEnBandeja = new HashMap<>();
                ServicioDebinProgramadoOB servicioDebinOB = new ServicioDebinProgramadoOB(contexto);
                listaOrdenada.stream().forEach(
                        d -> {
                            Futuro<DebinProgramadoOB> debinOB = servicioDebinOB.findByIdDebin(d.id);
                            if (debinOB != null) {
                                debinesEnBandeja.put(d.id, debinOB);
                            }
                        }
                );
                for (ListarDebinProgramado.Recurrencia d : listaOrdenada) {
                    DebinProgramadoOB debin = debinesEnBandeja.get(d.id).tryGet();
                    Objeto dato = new Objeto();
                    if (debin != null && (debin.estadoBandeja.id != EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo() && debin.estadoBandeja.id != EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo())) {
                        dato.set("estado", debin.estadoBandeja.descripcion);
                    } else {
                        String estado = d.estado.equals("INACTIVA") && d.autorizado.equals("RECHAZADO") ? "Rechazado" :
                                d.estado.equals("INACTIVA") && d.autorizado.equals("PENDIENTE") ? "ACEPTAR O RECHAZAR DEBIN" :
                                        d.estado.equals("ACTIVA") && d.autorizado.equals("AUTORIZADO") ? "ACEPTADO":
                                                d.estado.equals("INACTIVA") && d.autorizado.equals("AUTORIZADO") ?
                                                        "DADO DE BAJA" : d.estado;
                        dato.set("estado", estado);
                    }
                    dato.set("idDebin", d.id);
                    dato.set("moneda", d.debin.moneda);
                    dato.set("nombreVendedor", "");
                    dato.set("importe", d.debin.importe);
                    dato.set("cuitVendedor", d.vendedor.cuit);
                    dato.set("vencimiento", d.fecha_creacion);
                    dato.set("cuotas", d.debin.limite_cuotas);
                    dato.set("cbuComprador", d.comprador.cbu);
                    dato.set("autorizado", d.autorizado);
                    dato.set("prestacion", d.debin.prestacion);
                    String conceptoNombre = switch (d.debin.concepto) {
                        case "ALQ" -> "ALQUILERES";
                        case "CUO" -> "CUOTAS";
                        case "EXP" -> "EXPENSAS";
                        case "FAC" -> "FACTURAS";
                        case "HON" -> "HONORARIOS";
                        case "PRE" -> "PRESTAMOS";
                        case "SEG" -> "SEGUROS";
                        case "HAB" -> "HABERES";
                        default -> "VARIOS";
                    };
                    dato.set("concepto",conceptoNombre);
                    dato.set("detalle", d.debin.detalle);
                    dato.set("detalle", d.debin.detalle);
                    dato.set("referencia", d.debin.referencia);
                    dato.set("compradorCuit", d.comprador.cuit);
                    datos.add(dato);

                }
            }
        } catch (EntityNotFoundException e) {
            return new ErrorGenericoOB().setErrores("Error al listar Debin", "Lista Debin vacia");
        } catch (Exception e) {
            LogOB.evento(contexto, "listarRecibidosGenerados", new Objeto().set("error", e.getMessage()));
            return new ErrorGenericoOB().setErrores("Error al listar debines.", e.getMessage());
        }
        LogOB.evento(contexto, "listarRecibidosGenerados", "FIN");

        return respuesta("datos", datos);
    }
   /* public static Object firmarBajaDebinProgramado(ContextoOB contexto) {
        LogOB.evento(contexto, "bajaDebinProgramado", "INICIO");
        String idDebin = contexto.parametros.string("idDebin");
        ContextoOB contextoOB =  contexto.clonar();
        contextoOB.parametros.set("tipoSolicitud","Recibidas");
        Objeto debinBaja = null;
        try {
        Objeto lista  = Objeto.fromJson(listarRecibidosGeneradosProgramados(contextoOB).toString());
        if(lista!=null){
            List<Object> listaDebines = Objeto.fromJson(lista.get("datos").toString()).toList();
            for (Object d : listaDebines) {
                Map<String, Object> debin = (Map<String, Object>) d;
               if (debin.get("idDebin").equals(idDebin) ) {
                    debinBaja = Objeto.fromMap(debin);
                    break;
                }
            }
        }
        }catch (Exception e){
            return new ErrorGenericoOB().setErrores("Error debin no encontrado: ", e.getMessage());
        }

        BajaRecurrencia baja= null;
        try {
          if(debinBaja!=null){
                baja = ApiDebin.bajaRecurrencia(contexto,debinBaja.get("idDebin").toString(),debinBaja).get();
            }else{
              return new ErrorGenericoOB().setErrores("Error: ", "No se encontro el debin a dar de baja");
          }


        }catch (Exception e){
            return new ErrorGenericoOB().setErrores("Error en la baja: ", e.getMessage());
        }

        LogOB.evento(contexto, "bajaDebinProgramado", "FIN");
        return respuesta("0", "datos", baja);
    }*/

    public static Object listadoConceptos(ContextoOB contexto) {
        LogOB.evento(contexto, "listadoConceptos", "INICIO");

        ServicioConceptoDebinOB servicioConceptoDebinOB = new ServicioConceptoDebinOB(contexto);
        List<ConceptoDebinOB> listaConcepto = servicioConceptoDebinOB.findAll().get();


        List<Map<String, Object>> listaConceptos = new ArrayList<>();
        listaConcepto.forEach(concepto -> {
            Map<String, Object> conceptoMap = new HashMap<>();
            conceptoMap.put("codigo", concepto.id);
            conceptoMap.put("descripcion", concepto.descripcion);
            listaConceptos.add(conceptoMap);
        });
        LogOB.evento(contexto, "listadoConceptos", "FIN");
        return respuesta("0", "datos", listaConceptos);
    }

    protected static void vencerDebinesEnBandeja(List<BandejaOB> pendientesDeFirmaDebin) {
        for (BandejaOB b : pendientesDeFirmaDebin) {
            if (b.tipoProductoFirma.codProdFirma == 7) {
                DebinOB debin = servicioDebinOB.find(b.id).get();
                boolean esProgramado = false;
                try {
                    if (debin.sucursalIdVendedor != null && debin.tipoCuentaComprador != null) {
                        if (debin.sucursalIdVendedor.equals("-1") && debin.tipoCuentaComprador.equals("PP")) {
                            esProgramado = true;
                        }
                    }
                } catch (Exception e) {

                }

                if (esProgramado) {
                    break;
                } else {
                    if (!LocalDateTime.now().isBefore(OffsetDateTime.parse(debin.vencimiento).toLocalDateTime())) {
                        debin.estadoRecibida = estadoDebinRecibidaVencida;
                        debin.estadoEnviada = estadoDebinEnviadaVencida;
                        debin.estadoBandeja = estadoBandejaRechazada;
                        servicioDebinOB.update(debin);
                    }
                }

            }

        }
    }

    private static void vencerDebin(DebinOB debin) {
        debin.estadoRecibida = estadoDebinRecibidaVencida;
        debin.estadoEnviada = estadoDebinEnviadaVencida;
        debin.estadoBandeja = estadoBandejaRechazada;
        servicioDebinOB.update(debin);
    }

    public static Object aceptarDebin(ContextoOB contexto) {
        LogOB.evento(contexto, "aceptarDebin", "INICIO");

        String idDebin = contexto.parametros.string("idDebin");
        String referencia = contexto.parametros.string("referencia", null);

        Objeto datos = new Objeto();
        DebinOB debinFinal;
        DebinOB debin = servicioDebinOB.findByIdDebin(String.valueOf(idDebin)).tryGet();

        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, contexto.sesion().empresaOB, contexto.sesion().usuarioOB);

        if (debin != null) {
            debinFinal = debin;
            debinFinal.emp_codigo = contexto.sesion().empresaOB;
        } else {
            try {
                debinFinal = guardarDebinExterno(contexto, idDebin, referencia);
            } catch (Exception e) {
                LogOB.evento(contexto, "aceptarDebin", new Objeto().set("error", e.getMessage()));
                return new ErrorGenericoOB().setErrores("Error al consultar debin", e.getMessage());
            }

        }
        servicioHistorialDebinOB.cambiaEstado(debinFinal, servicioAccionesOB.find(EnumAccionesOB.NO_APLICA.getCodigo()).get(), empresaUsuario, estadoDebinEnviadaIniciada,
                estadoDebinEnviadaIniciada, estadoDebinRecibidaPendiente, estadoDebinRecibidaEnBandeja);

        debinFinal.estadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
        debinFinal.estadoRecibida = estadoDebinRecibidaEnBandeja;
        debinFinal.referenciaAceptacion = referencia;
        servicioDebinOB.update(debinFinal).tryGet();

        datos.set("idOperacion", debinFinal.id);
        LogOB.evento(contexto, "aceptarDebin", "FIN");

        return respuesta("datos", datos);
    }
    public static Object bajaDebinProgramado(ContextoOB contexto) {
        LogOB.evento(contexto, "bajaDebinProgramado", "INICIO");
        String idDebin = contexto.parametros.string("idDebin");
        String fechaCreacion = contexto.parametros.string("fechaCreacion", null);
        DebinProgramadoOB SolicituDebin;
        Objeto datos = new Objeto();
        SolicituDebin =  servicioDebinProgramadoOB.findByIdDebin(idDebin).tryGet();
        if(SolicituDebin!=null && SolicituDebin.estado.equals(EnumEstadosDebinProgramado.BAJA_SUSCRIPCION.getCodigo())  &&
                (SolicituDebin.estadoBandeja.id == EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo() || SolicituDebin.estadoBandeja.id  == EnumEstadoBandejaOB.PARCIALMENTE_FIRMADA.getCodigo())){
            datos.set("idOperacion", SolicituDebin.id);
            LogOB.evento(contexto, "aceptarDebin", "FIN ENCONTRADO");
            return respuesta("datos", datos);
        }
        String banco = "044";
        String fechaDesde = "1900/01/01";
        String fechaHasta = LocalDateTime.parse(fechaCreacion).toLocalDate().plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
       String cuitcomprador = contexto.sesion().empresaOB.cuit.toString();
        ListarDebinProgramado listaDebin = ApiDebin.buscarDebinRecurrente(contexto,cuitcomprador, banco, fechaDesde, fechaHasta, null,"",null).tryGet();
        ListarDebinProgramado.Recurrencia debin = listaDebin.result.recurrencia.stream().filter(recurrencia -> recurrencia.id.equals(idDebin)).findFirst().orElse(null);
        if (debin != null) {
            SolicituDebin = ListarDebinProgramado.mapToDebinProgramadoOB(debin, contexto);
            try {
                Objeto cuentas = (Objeto) OBCuentas.cuentas(contexto);
                Objeto listaCuentas = (Objeto) cuentas.get("datos.cuentas");
                final String cbu = SolicituDebin.compradorCbu.toString();
                Optional<String> numeroCuenta = listaCuentas.objetos().stream()
                        .filter(c -> cbu.equals(c.get("cbu")))
                        .map(c -> (String) c.get("numeroProducto"))
                        .findFirst();
                if (!numeroCuenta.isPresent()) {
                    return new ErrorGenericoOB().setErrores("ERROR", "CUENTA NO ENCONTRADA");
                }
                SolicituDebin.cuentaOrigen = numeroCuenta.get();
                SolicituDebin.estado = EnumEstadosDebinProgramado.BAJA_SUSCRIPCION.getCodigo();
                SolicituDebin.estadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
                SolicituDebin.id = null;
                SolicituDebin.debinConcepto =debin.debin.concepto;
                SolicituDebin = guardarDebinProgramado(SolicituDebin);

            } catch (Exception e) {
                LogOB.evento(contexto, "aceptarDebin", new Objeto().set("error", e.getMessage()));
                return new ErrorGenericoOB().setErrores("Error al consultar debin", e.getMessage());
            }
            datos.set("idOperacion", SolicituDebin.id);
            LogOB.evento(contexto, "aceptarDebin", "FIN");
            return respuesta("datos", datos);
        }
     else {
        datos.set("descripcion", "No se encontro el debin");
        return respuesta("error", datos);
    }
    }
    public static Object aceptarDebinProgramado(ContextoOB contexto) {
        LogOB.evento(contexto, "aceptarDebin", "INICIO");

        String idDebin = contexto.parametros.string("idDebin");
        String fechaCreacion = contexto.parametros.string("fechaCreacion", null);
        DebinProgramadoOB SolicituDebin;
        SolicituDebin =  servicioDebinProgramadoOB.findByIdDebin(idDebin).tryGet();
        Objeto datos = new Objeto();
       if(SolicituDebin!=null && SolicituDebin.estado.equals(EnumEstadosDebinProgramado.ALTA_SUSCRIPCION.getCodigo())  &&
               (SolicituDebin.estadoBandeja.id == EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo() || SolicituDebin.estadoBandeja.id  == EnumEstadoBandejaOB.PARCIALMENTE_FIRMADA.getCodigo())){
            datos.set("idOperacion", SolicituDebin.id);
            LogOB.evento(contexto, "aceptarDebin", "FIN ENCONTRADO");
            return respuesta("datos", datos);
        }
        String banco = "044";
        String fechaDesde = LocalDateTime.parse(fechaCreacion).toLocalDate().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fechaHasta = LocalDateTime.parse(fechaCreacion).toLocalDate().plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        ListarDebinProgramado listaDebin = ApiDebin.buscarDebinRecurrente(contexto, contexto.sesion().empresaOB.cuit.toString(), banco, fechaDesde, fechaHasta, null,"","INACTIVA").tryGet();
        ListarDebinProgramado.Recurrencia debin = listaDebin.result.recurrencia.stream().filter(recurrencia -> recurrencia.id.equals(idDebin)).findFirst().orElse(null);
        if (debin != null) {
            SolicituDebin = ListarDebinProgramado.mapToDebinProgramadoOB(debin,contexto);
            try {
                Objeto cuentas = (Objeto) OBCuentas.cuentas(contexto);
                Objeto listaCuentas = (Objeto) cuentas.get("datos.cuentas");
                final String cbu = SolicituDebin.compradorCbu.toString();
                Optional<String> numeroCuenta = listaCuentas.objetos().stream()
                        .filter(c -> cbu.equals(c.get("cbu")))
                        .map(c -> (String) c.get("numeroProducto"))
                        .findFirst();
                if(!numeroCuenta.isPresent()){
                    return new ErrorGenericoOB().setErrores("ERROR","CUENTA NO ENCONTRADA");
                }
                SolicituDebin.cuentaOrigen = numeroCuenta.get();
                SolicituDebin = guardarDebinProgramado(SolicituDebin);
            } catch (Exception e) {
                LogOB.evento(contexto, "aceptarDebin", new Objeto().set("error", e.getMessage()));
                return new ErrorGenericoOB().setErrores("Error al consultar debin", e.getMessage());
            }
            SolicituDebin.estadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
            SolicituDebin.estado = EnumEstadosDebinProgramado.ALTA_SUSCRIPCION.getCodigo();
            servicioDebinProgramadoOB.update(SolicituDebin);

            datos.set("idOperacion", SolicituDebin.id);
            LogOB.evento(contexto, "aceptarDebin", "FIN");
            return respuesta("datos", datos);
        } else {
            datos.set("descripcion", "No se encontro el debin");
            return respuesta("error", datos);
        }
    }

    public static Object rechazarDebinProgramado(ContextoOB contexto) {
        LogOB.evento(contexto, "aceptarDebin", "INICIO");

        String idDebin = contexto.parametros.string("idDebin");
        String fechaCreacion = contexto.parametros.string("fechaCreacion", null);
        Objeto datos = new Objeto();
        String banco = "044";
        String fechaDesde = LocalDateTime.parse(fechaCreacion).toLocalDate().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fechaHasta = LocalDateTime.parse(fechaCreacion).toLocalDate().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        ListarDebinProgramado listaDebin = ApiDebin.buscarDebinRecurrente(contexto, contexto.sesion().empresaOB.cuit.toString(), banco, fechaDesde, fechaHasta, null,"",null).tryGet();
        ListarDebinProgramado.Recurrencia debin = listaDebin.result.recurrencia.stream().filter(recurrencia -> recurrencia.id.equals(idDebin)).findFirst().orElse(null);
        if (debin != null) {
            try {
                DebinProgramadoOB debinFinal = ListarDebinProgramado.mapToDebinProgramadoOB(debin, contexto);
                AdherirRecurrencia adherirRecurrencia = ApiDebin.adherirRecurrencia(contexto, debinFinal, false).get();
                datos.set("monto", debinFinal.monto);
                datos.set("comprador", debinFinal.compradorCuit);
                datos.set("idOperacion", debinFinal.id);
                return respuesta("datos", datos);
            } catch (Exception e) {
                datos.set("descripcion", "Se produjo un error en el rechazo");
                return respuesta("error", "datos", datos);
            }
        } else {
            datos.set("descripcion", "No se encontro el debin");
            return respuesta("error", "datos", datos);
        }
    }

    private static DebinOB guardarDebinExterno(ContextoOB contexto, String idDebin, String referencia) {
        ConsultarDebin.ConsultaDebinResponse detalle = ApiDebin.consultarDebin(contexto, String.valueOf(idDebin)).tryGet();
        DebinOB debinAGuardar = new DebinOB();
        debinAGuardar.estadoEnviada = estadoDebinEnviadaIniciada;
        debinAGuardar.idDebin = detalle.id;
        debinAGuardar.cbuComprador = detalle.comprador.cliente.cuenta.cbu;
        debinAGuardar.cuentaComprador = detalle.comprador.cliente.cuenta.numero;
        debinAGuardar.cuentaOrigen = detalle.comprador.cliente.cuenta.numero;
        debinAGuardar.idTributarioComprador = detalle.comprador.cliente.idTributario;
        debinAGuardar.nombreComprador = detalle.comprador.cliente.nombreCompleto;
        debinAGuardar.tipoCuentaComprador = detalle.comprador.cliente.cuenta.tipo;
        debinAGuardar.sucursalDescVendedor = detalle.vendedor.cliente.cuenta.sucursal.descripcion;
        debinAGuardar.sucursalIdVendedor = detalle.vendedor.cliente.cuenta.sucursal.id;
        debinAGuardar.emp_codigo = contexto.sesion().empresaOB;

        InfoCuentaDTO infoCuentaComprador = servicioBeneficiarioOB.infoCBUAlias(contexto, detalle.comprador.cliente.cuenta.cbu);
        InfoCuentaDTO infoCuentaVendedor = servicioBeneficiarioOB.infoCBUAlias(contexto, detalle.vendedor.cliente.cuenta.cbu);
        MonedaOB moneda = servicioMoneda.find(Integer.valueOf(detalle.detalle.moneda.id)).get();

        String conceptoNombre = switch (detalle.detalle.concepto) {
            case "ALQ" -> "ALQUILERES";
            case "CUO" -> "CUOTAS";
            case "EXP" -> "EXPENSAS";
            case "FAC" -> "FACTURAS";
            case "HON" -> "HONORARIOS";
            case "PRE" -> "PRESTAMOS";
            case "SEG" -> "SEGUROS";
            case "HAB" -> "HABERES";
            default -> "VARIOS";
        };

        ConceptoDebinOB concepto = servicioConcepto.find(EnumConceptosDebinOB.valueOf(conceptoNombre).getCodigo()).get();

        return servicioDebinOB.enviarSolicitud(contexto, detalle.detalle.fechaExpiracion, moneda, new BigDecimal(detalle.detalle.importe), concepto, referencia, debinAGuardar, detalle.detalle.fecha, infoCuentaComprador, infoCuentaVendedor.numero, infoCuentaVendedor.cuenta.cuit).get();
    }

    private static DebinProgramadoOB guardarDebinProgramado(DebinProgramadoOB debinFinal) {
        return servicioDebinProgramadoOB.create(debinFinal).get();
       }

    public static Object tieneFondosDebin(ContextoOB contexto) {
        String idDebin = contexto.parametros.string("idDebin");
        ConsultarDebin.ConsultaDebinResponse detalle = ApiDebin.consultarDebin(contexto, idDebin).tryGet();
        Long importe = Long.valueOf(detalle.detalle.importe);
        CuentasOB cuentasOB = ApiCuentas.cuentas(contexto, contexto.sesion().empresaOB.idCobis).get();
        Objeto respuesta = new Objeto();
        cuentasOB.stream().filter(cuenta -> cuenta.cbu.equals(detalle.comprador.cliente.cuenta.cbu)).forEach(cuenta -> {

            respuesta.set("tieneFondos", validarSaldoYCuenta(contexto, BigDecimal.valueOf(importe), cuenta.numeroProducto));
            CompletaFirmaOB grupoOB = ApiEmpresas.completaFirma(contexto, contexto.sesion().empresaOB.cuit.toString(), cuenta.numeroProducto, detalle.detalle.importe, contexto.sesion().usuarioOB.cuil.toString(), "", String.valueOf(EnumTipoProductoOB.DEBIN.getCodigo())).get();
            respuesta.set("esUltimoFirmante", (grupoOB.codigo.equals("250")));
        });

        return respuesta("0", "DATOS", respuesta);
    }

    private static String mapearEstadoDebin(DebinOB debin) {
        String respuesta = null;
        if (debin.estadoBandeja != null) {
            if ((debin.estadoBandeja.descripcion.equals("FIRMADO COMPLETO") || debin.estadoRecibida.descripcion.equals("VENCIDO"))) {
                respuesta = debin.estadoRecibida.descripcion;
            } else {
                if(debin.estadoBandeja.descripcion.equals("FIRMADO COMPLETO")){

                }
                respuesta = debin.estadoBandeja.descripcion;
            }
        } else {
            respuesta = debin.estadoRecibida.descripcion;
        }
        return respuesta;
    }
}
