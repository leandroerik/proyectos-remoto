package ar.com.hipotecario.canal.officebanking;

import static ar.com.hipotecario.canal.officebanking.OBPagos.validarSaldoYCuenta;
import static ar.com.hipotecario.canal.officebanking.util.StringUtil.agregarCerosAIzquierda;
import static ar.com.hipotecario.canal.officebanking.util.StringUtil.reemplazarTXT;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.servicio.api.debin.*;
import ar.com.hipotecario.backend.servicio.api.notificaciones.ApiNotificaciones;

import ar.com.hipotecario.backend.servicio.api.tarjetasCredito.StopDebit;
import ar.com.hipotecario.canal.libreriariesgofraudes.application.dto.RecommendationDTO;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.be.TransactionBEBankProcess;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.generics.TransactionBankProcess;
import ar.com.hipotecario.canal.officebanking.enums.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.*;
import ar.com.hipotecario.backend.servicio.sql.intercambioate.PeticionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.tarjetaCredito.StopDebitOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.DebinProgramadoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EnumEstadosDebinLoteOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.ServicioHistorialDebinLoteOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.ArchivosComexOB;
import ar.com.hipotecario.canal.officebanking.transmit.TransmitOB;
import ar.com.hipotecario.canal.officebanking.util.CotizacionesDivisas;
import org.apache.commons.lang3.StringUtils;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.ApiCatalogo;
import ar.com.hipotecario.backend.servicio.api.catalogo.DiaBancario;
import ar.com.hipotecario.backend.servicio.api.cheques.ApiCheques;
import ar.com.hipotecario.backend.servicio.api.cheques.DetalleEmisionEcheq;
import ar.com.hipotecario.backend.servicio.api.cuentas.ApiCuentas;
import ar.com.hipotecario.backend.servicio.api.empresas.ApiEmpresas;
import ar.com.hipotecario.backend.servicio.api.empresas.CompletaFirmaOB;
import ar.com.hipotecario.backend.servicio.api.firmas.TipoFirma;
import ar.com.hipotecario.backend.servicio.api.inversiones.ApiInversiones;
import ar.com.hipotecario.backend.servicio.api.inversiones.Solicitudes;
import ar.com.hipotecario.backend.servicio.api.link.ApiLink;
import ar.com.hipotecario.backend.servicio.api.link.Pagos;
import ar.com.hipotecario.backend.servicio.api.linkPagosVep.ApiLinkPagosVep;
import ar.com.hipotecario.backend.servicio.api.linkPagosVep.VepsPagados;
import ar.com.hipotecario.backend.servicio.api.linkPagosVep.VepsPendientes;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.ApiPlazosFijos;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.Cedips.Cedip.CedipNuevo;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.Cedips.Cedip.ResponsePostTransmision;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.PlazosFijos.PlazoFijo;
import ar.com.hipotecario.backend.servicio.api.productos.Cuentas.Cuenta;
import ar.com.hipotecario.backend.servicio.api.productos.CuentasOB;
import ar.com.hipotecario.backend.servicio.api.productos.CuentasOB.CuentaOB;
import ar.com.hipotecario.backend.servicio.api.tarjetasCredito.PagoTarjeta;
import ar.com.hipotecario.backend.servicio.sql.hb_be.TokensOB;
import ar.com.hipotecario.canal.officebanking.dto.ErrorGenericoOB;

import ar.com.hipotecario.canal.officebanking.enums.EnumAccionesOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumCuentasOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoDebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoOrdenPagoComexOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoPagosDeServicioYVepsOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoPagosHaberesOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoSolicitudFCIOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoTRNOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadosEcheqOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumMonedasOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumPerfilInversorOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;

import ar.com.hipotecario.canal.officebanking.enums.debin.EnumEstadoDebinEnviadasOB;
import ar.com.hipotecario.canal.officebanking.enums.debin.EnumEstadoDebinRecibidasOB;
import ar.com.hipotecario.canal.officebanking.enums.echeq.EnumAccionesEcheqOB;
import ar.com.hipotecario.canal.officebanking.enums.echeq.EnumEstadoEcheqChequeraOB;
import ar.com.hipotecario.canal.officebanking.enums.pagoTarjeta.EnumEstadoPagoTarjetaOB;
import ar.com.hipotecario.canal.officebanking.enums.pagosMasivos.EnumEstadoPagosAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.dto.FirmarRechazarDTO;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioAccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioBandejaAccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioCedipOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioChequeraOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioCobranzaIntegralOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioDebinOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioDebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEcheqDescuentoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadoCedipOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadoChequeraOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadoComexOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadoDebinEnviadasOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadoDebinRecibidasOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadoEcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadoInversionOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadoPagoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadoPagosHaberesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadoTRNOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadosCobranzaIntegral;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadosDebinPorLote;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadosDebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadosPagoAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioFCI;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioFCIOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioFirmasProductosOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioHistorialChequeraOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioHistorialCobranzaIntegralOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioHistorialDebinOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioHistorialDebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioHistorialEcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioHistorialFCI;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioHistorialOPComex;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioHistorialPagoAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioHistorialPagoDeHaberesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioHistorialPagoDeServiciosOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioHistorialPagoVepsOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioHistorialSolicitudPI;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioHistorialTrnOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioMonedaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioOPComexOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioPagoAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioPagoDeServiciosOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioPagoHaberesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioPagosVepOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioParametriaFciOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioPerfilInversorOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioPlazoFijoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioTarjetaVirtualOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioTipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioTransferenciaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.TarjetaVirtualOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaAccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.FirmasProductoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.CobranzaIntegralOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.EstadosCobranzaIntegralOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.EstadoOPComexOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.HistorialOPComexOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.OrdenPagoComexOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.DebinOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.EstadoDebinEnviadasOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.EstadoDebinRecibidasOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto.DebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto.EstadosDebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto.HistorialCobranzaIntegral;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto.HistorialDebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.ChequeraOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EcheqDescuentoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EstadoChequeraOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EstadoEcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.HistorialChequeraOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.CedipAccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.CedipOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.EstadoCedipOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.EstadoSolicitudInversionOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.FondosComunesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.HistorialFCIOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.HistorialSolicitudPerfilInversorOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.ParametriaFciOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.PlazoFijoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.SolicitudPerfilInversorOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.EstadoPagoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.EstadosPagosAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.HistorialPagoAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.PagoAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoDeServicios.HistorialPagoDeServiciosOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoDeServicios.PagoDeServiciosOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.EstadoPagosHaberesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.HistorialPagoDeHaberesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.PagoDeHaberesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagosVep.HistorialPagosVepOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagosVep.PagosVepOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.tarjetaCredito.EstadoPagoTarjetaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.tarjetaCredito.PagoTarjetaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EnumEstadosCobranzaIntegralOB;
import ar.com.hipotecario.canal.officebanking.util.ConstantesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.ServicioDebinLoteOB;

import static ar.com.hipotecario.canal.officebanking.OBDebitoDirecto.renombrarArchivoDebitoDirecto;
import static ar.com.hipotecario.canal.officebanking.OBPagoHaberes.convertirNombreHaberes;

public class OBFirmas extends ModuloOB {
    private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
    static ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
    static ServicioDebinOB servicioDebinOB = new ServicioDebinOB(contexto);
    static ServicioDebinProgramadoOB servicioDebinProgramadoOB = new ServicioDebinProgramadoOB(contexto);
    static ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
    static ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
    static ServicioHistorialDebinOB servicioHistorialDebinOB = new ServicioHistorialDebinOB(contexto);
    static ServicioEstadoDebinRecibidasOB servicioEstadoDebinRecibidasOB = new ServicioEstadoDebinRecibidasOB(contexto);
    static ServicioEstadoDebinEnviadasOB servicioEstadoDebinEnviadasOB = new ServicioEstadoDebinEnviadasOB(contexto);
    static EstadoDebinRecibidasOB estadoEnBandeja = servicioEstadoDebinRecibidasOB.find(EnumEstadoDebinRecibidasOB.EN_BANDEJA.getCodigo()).get();
    static EstadoDebinEnviadasOB estadoIniciadoEnv = servicioEstadoDebinEnviadasOB.find(EnumEstadoDebinEnviadasOB.INICIADO.getCodigo()).get();
    static AccionesOB accionFirmar = servicioAcciones.find(EnumAccionesOB.FIRMAR.getCodigo()).get();

    public static Object firmar(ContextoOB contexto) {
        LogOB.evento(contexto, "firmar", "INICIA LA FIRMA");
        Objeto objetoIds = contexto.parametros.objeto("idsOperaciones");
        List<Object> idsOperaciones = objetoIds.toList();
        List<Object> datosOperaciones = new ArrayList<>();

        SesionOB sesion = contexto.sesion();
        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);

        List<BandejaOB> pendientesDeFirmaBandejaPorCuenta = new ArrayList<>();
        List<BandejaOB> pendientesDeFirmaSinEmpresa = servicioBandeja.buscarPendientesDeFirma(null, null, null, null, null, null, null).get();
        List<BandejaOB> pendientesDeFirma = servicioBandeja.buscarPendientesDeFirma(contexto.sesion().empresaOB, null, null, null, null, null, null).get();

        Objeto cuentas = (Objeto) OBCuentas.cuentas(contexto);
        Objeto listaCuentas = (Objeto) cuentas.get("datos.cuentas");
        List<String> lstCuentas = listaCuentas.objetos().stream().map(c -> c.get("numeroProducto").toString()).toList();
        for (String c : lstCuentas) {
            pendientesDeFirmaBandejaPorCuenta.addAll(pendientesDeFirmaSinEmpresa.stream().filter(p -> p.cuentaOrigen.equals(c)).toList());
        }
        Set<Integer> idsExistentes = pendientesDeFirmaBandejaPorCuenta.stream().map(bandejaOB -> bandejaOB.id).collect(Collectors.toSet());

        pendientesDeFirma.forEach(bandeja -> {
            if (!idsExistentes.contains(bandeja.id)) {
                pendientesDeFirmaBandejaPorCuenta.add(bandeja);
                idsExistentes.add(bandeja.id);
            }
        });
        LogOB.evento(contexto, "firmar", "BUSCA OPERACIONES EN BANDEJA");

        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);

        boolean pagoMultiple;
        pagoMultiple = contexto.parametros.existe("multiple") ? true : false;
        LogOB.evento(contexto, "firmar", "es multiple: " + pagoMultiple);
        if (pagoMultiple) {
            String uuid = sesion.uuid;
            Fecha fechaExpiracion = Fecha.ahora().sumarMinutos(5);
            TokensOB.actualizarFechaExpiracion(contexto, uuid, fechaExpiracion);
        }
        
        int i = 0;
        Boolean response = false;
        
        for (Object id : idsOperaciones) {

            Optional<BandejaOB> bandejaPendienteDeFirma = pendientesDeFirmaBandejaPorCuenta.stream().filter(t -> t.id.toString().equals(id.toString())).findFirst();
            LogOB.evento(contexto, "firmar", "VA A FIRMAR OPERACION: " + id);
            LogOB.evento(contexto, "firmar", "TRAE LA OPERACION: " + id);
            if (!bandejaPendienteDeFirma.isPresent()) {
                LogOB.evento(contexto, "firmar", "DATOS_INVALIDOS: " + id);
                return respuesta("DATOS_INVALIDOS");
            }
            BandejaOB bandeja = bandejaPendienteDeFirma.get();


            EnumTipoProductoOB productos = EnumTipoProductoOB.getByCodigo(bandeja.tipoProductoFirma.codProdFirma);
            Object datos = null;
            LogOB.evento(contexto, "firmar", "VA FIRMAR UNA OPERACION: " + Objects.requireNonNull(productos));
            switch (Objects.requireNonNull(productos)) {
                case COMERCIO_EXTERIOR:
                	datos = firmarOrdenPagoComex(contexto, bandeja, empresaUsuario);
                    if (datos.toString().contains("ERROR")) {
                        datosOperaciones.add(respuesta("ERROR", "datos", datos));
                    } else {
                        datosOperaciones.add(respuesta("datos", datos));
                    }
            		break;
           		case TRANSFERENCIAS:
                    if(sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
            			if (sesion.token.fechaValidacion.isNull()) {
            				LogOB.evento(contexto, "firmar", "TRANSFERENCIAS FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}else {            			
            			if(i==0) {
            				response = OBTransmit.lecturaCsmIdAuth(contexto);
            			}
            			
            			if (!response) {
            				LogOB.evento(contexto, "firmar", "TRANSFERENCIAS_TRANSMIT FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}            			
            		}
                    
                    datos = firmarTransferencia(contexto, bandeja, empresaUsuario);
                    if (!datos.toString().contains("CHALLENGE") && !datos.toString().contains("DENY")) {
                        if (datos.toString().contains("ERROR")) {
                            datosOperaciones.add(respuesta("ERROR", "datos", datos));
                        } else {
                            datosOperaciones.add(respuesta("datos", datos));
                        }
                    }
                    break;
                case PERFIL_INVERSOR:
                    datos = firmarPerfilInversor(contexto, bandeja, empresaUsuario);
                    datosOperaciones.add(respuesta("datos", datos));
                    break;
                case FCI:
                    try {
                        datos = firmarFCI(contexto, bandeja, empresaUsuario);
                    } catch (Exception e) {
                        LogOB.evento(contexto, "firmar", "FCI ERROR: " + e.getMessage());
                        return (e.getMessage());
                    }
                    if (!datos.toString().contains("CHALLENGE") && !datos.toString().contains("DENY")) {
                        if (datos.toString().contains("ERROR")) {
                            LogOB.evento(contexto, "firmar", "FCI DATOS CONTAINS ERROR: " + datos);
                            datosOperaciones.add(datos);
                        } else datosOperaciones.add(respuesta("datos", datos));
                    }
                    break;
                case PAGO_SERVICIOS:
                    if (!pagoMultiple) {
                    	if(sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
                			if (sesion.token.fechaValidacion.isNull()) {
                				LogOB.evento(contexto, "firmar", "PAGO_SERVICIOS FACTOR_NO_VALIDADO");
                				return respuesta("FACTOR_NO_VALIDADO");
                			}
                		}else {
                			if(i==0) {
                				response = OBTransmit.lecturaCsmIdAuth(contexto);
                			}
                			
                			if (!response) {
                				LogOB.evento(contexto, "firmar", "PAGO_SERVICIOS_TRANSMIT FACTOR_NO_VALIDADO");
                				return respuesta("FACTOR_NO_VALIDADO");
                			}
                		}
                    }
                    
                    try {

                        datos = firmarPagoDeServicios(contexto, bandeja, empresaUsuario);
                    } catch (Exception e) {
                        LogOB.evento(contexto, "firmar", "ERROR PAGO_SERVICIOS: " + e.getMessage());
                        return (e.getMessage());
                    }
                   // if (!datos.toString().contains("CHALLENGE") && !datos.toString().contains("DENY")) {
                        if (datos.toString().contains("ERROR")) {
                            LogOB.evento(contexto, "firmar", "ERROR PAGO_SERVICIOS");
                            datosOperaciones.add(respuesta("ERROR", "datos", datos));
                        } else datosOperaciones.add(respuesta("datos", datos));
                  //  }
                    break;

                case PAGOS_VEP:
                    if(sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
            			if (sesion.token.fechaValidacion.isNull()) {
            				LogOB.evento(contexto, "firmar", "PAGOS_VEP FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}else {
            			if(i==0) {
            				response = OBTransmit.lecturaCsmIdAuth(contexto);
            			}
            			
            			if (!response) {
            				LogOB.evento(contexto, "firmar", "PAGOS_VEP_TRANSMIT FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}

                    try {
                        datos = firmarPagosVep(contexto, bandeja, empresaUsuario);
                    } catch (Exception e) {
                        return (e.getMessage());
                    }
                    if (!datos.toString().contains("CHALLENGE") && !datos.toString().contains("DENY")) {
                        if (datos.toString().contains("ERROR")) {
                            datosOperaciones.add(respuesta("ERROR", "datos", datos));
                        } else datosOperaciones.add(respuesta("datos", datos));
                    }
                    break;

                case PLAN_SUELDO:
                case NOMINA:
                    if(sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
            			if (sesion.token.fechaValidacion.isNull()) {
            				LogOB.evento(contexto, "firmar", "NOMINA FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}else {
            			if(i==0) {
            				response = OBTransmit.lecturaCsmIdAuth(contexto);
            			}
            			
            			if (!response) {
            				LogOB.evento(contexto, "firmar", "NOMINA_TRANSMIT FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}
                    
                    datos = firmarHaberes(contexto, bandeja, empresaUsuario);
                    if (!datos.toString().contains("CHALLENGE") && !datos.toString().contains("DENY")) {
                        if (!datos.toString().contains("CHALLENGE") && !datos.toString().contains("DENY")) {
                            datosOperaciones.add(respuesta("datos", datos));
                        }
                    }
                    break;
                case PAGO_PROVEEDORES:
                    if(sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
            			if (sesion.token.fechaValidacion.isNull()) {
            				LogOB.evento(contexto, "firmar", "PAGO_PROVEEDORES FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}else {
            			if(i==0) {
            				response = OBTransmit.lecturaCsmIdAuth(contexto);
            			}
            			
            			if (!response) {
            				LogOB.evento(contexto, "firmar", "PAGO_PROVEEDORES_TRANSMIT FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}
                    
                    datos = firmarPap(contexto, bandeja, empresaUsuario);
                    if (!datos.toString().contains("CHALLENGE") && !datos.toString().contains("DENY")) {
                        datosOperaciones.add(respuesta("datos", datos));
                    }
                    break;
                case DEBITO_DIRECTO:
                    if(sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
            			if (sesion.token.fechaValidacion.isNull()) {
            				LogOB.evento(contexto, "firmar", "DEBITO_DIRECTO FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}else {
            			if(i==0) {
            				response = OBTransmit.lecturaCsmIdAuth(contexto);
            			}
            			
            			if (!response) {
            				LogOB.evento(contexto, "firmar", "DEBITO_DIRECTO_TRANSMIT FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}
                    
                    datos = firmarDebitoDirecto(contexto, bandeja, empresaUsuario);
                  //  if (!datos.toString().contains("CHALLENGE") && !datos.toString().contains("DENY")) {
                        datosOperaciones.add(respuesta("datos", datos));
                    //}
                    break;
                case COBRANZA_INTEGRAL:
                    if(sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
            			if (sesion.token.fechaValidacion.isNull()) {
            				LogOB.evento(contexto, "firmar", "COBRANZA_INTEGRAL FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}else {
            			if(i==0) {
            				response = OBTransmit.lecturaCsmIdAuth(contexto);
            			}
            			
            			if (!response) {
            				LogOB.evento(contexto, "firmar", "COBRANZA_INTEGRAL_TRANSMIT FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}
                    
                    datos = firmarCobranzaIntegral(contexto, bandeja, empresaUsuario);
                   // if (!datos.toString().contains("CHALLENGE") && !datos.toString().contains("DENY")) {
                        datosOperaciones.add(respuesta("datos", datos));
                   // }
                    break;
                case CEDIP:
                    if(sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
            			if (sesion.token.fechaValidacion.isNull()) {
            				LogOB.evento(contexto, "firmar", "CEDIP FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}else {
            			if(i==0) {
            				response = OBTransmit.lecturaCsmIdAuth(contexto);
            			}
            			
            			if (!response) {
            				LogOB.evento(contexto, "firmar", "CEDIP_TRANSMIT FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}
                    
                    if (bandeja.getClass().getName() == "ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.CedipOB") {
                        datos = firmarCedip(contexto, bandeja, empresaUsuario);
                        datosOperaciones.add(respuesta("datos", datos));
                    } else {
                        datos = firmarCedipAcciones(contexto, bandeja, empresaUsuario);
                        datosOperaciones.add(respuesta("datos", datos));
                    }
                    break;
                case CHEQUERA_ELECTRONICA:
                    if(sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
            			if (sesion.token.fechaValidacion.isNull()) {
            				LogOB.evento(contexto, "firmar", "CHEQUERA_ELECTRONICA FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}else {
            			if(i==0) {
            				response = OBTransmit.lecturaCsmIdAuth(contexto);
            			}
            			
            			if (!response) {
            				LogOB.evento(contexto, "firmar", "CHEQUERA_ELECTRONICA_TRANSMIT FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}
                    
                    datos = firmarChequeraElectronica(contexto, bandeja, empresaUsuario);
                   // if (!datos.toString().contains("CHALLENGE") && !datos.toString().contains("DENY")) {
                        datosOperaciones.add(respuesta("datos", datos));
                   // }

                    break;

                case ECHEQ:
                    if(sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
            			if (sesion.token.fechaValidacion.isNull()) {
            				LogOB.evento(contexto, "firmar", "ECHEQ FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}else {
            			if(i==0) {
            				response = OBTransmit.lecturaCsmIdAuth(contexto);
            			}
            			
            			if (!response) {
            				LogOB.evento(contexto, "firmar", "ECHEQ_TRANSMIT FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}
                    
                    ServicioEcheqOB servicioEcheqOB = new ServicioEcheqOB(contexto);
                    EcheqOB echeqOB = servicioEcheqOB.find(bandeja.id).get();
                    if (empty(echeqOB)) {
                        return respuesta("DATOS_INVALIDOS");
                    } else if (echeqOB.accion.equals(EnumAccionesEcheqOB.EMISION)) {
                        datos = firmarEcheq(contexto, bandeja, empresaUsuario);
                     //   if (!datos.toString().contains("CHALLENGE") && !datos.toString().contains("DENY")) {
                            datosOperaciones.add(respuesta("datos", datos));
                    //    }
                    } else {
                        datos = firmarAccionesEcheq(contexto, bandeja, empresaUsuario);
                        if (!datos.toString().contains("CHALLENGE") && !datos.toString().contains("DENY")) {
                            if (datos.toString().contains("ERROR")) {
                                datosOperaciones.add(respuesta("ERROR", "datos", datos));
                            } else {
                                datosOperaciones.add(respuesta("datos", datos));
                            }
                        }
                    }
                    break;
                case DEBIN:
                    if(sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
            			if (sesion.token.fechaValidacion.isNull()) {
            				LogOB.evento(contexto, "firmar", "DEBIN FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}else {
            			if(i==0) {
            				response = OBTransmit.lecturaCsmIdAuth(contexto);
            			}
            			
            			if (!response) {
            				LogOB.evento(contexto, "firmar", "DEBIN_TRANSMIT FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}
                    
                    datos = firmarDebin(contexto, bandeja, empresaUsuario);
                    if (!datos.toString().contains("CHALLENGE") && !datos.toString().contains("DENY")) {
                        if (datos.toString().contains("ERROR")) {
                            datosOperaciones.add(respuesta("ERROR", "datos", datos));
                        } else datosOperaciones.add(respuesta("datos", datos));
                    }
                    break;
                case DEBIN_PROGRAMADO:
                    if(sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
            			if (sesion.token.fechaValidacion.isNull()) {
            				LogOB.evento(contexto, "firmar", "DEBIN_PROGRAMADO FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}else {
            			if(i==0) {
            				response = OBTransmit.lecturaCsmIdAuth(contexto);
            			}
            			
            			if (!response) {
            				LogOB.evento(contexto, "firmar", "DEBIN_PROGRAMADO_TRANSMIT FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}
                    
                    datos = firmarDebinProgrmado(contexto, bandeja, empresaUsuario, listaCuentas);
                    if (datos.toString().contains("ERROR")) {
                        datosOperaciones.add(respuesta("ERROR", "datos", datos));
                    } else datosOperaciones.add(respuesta("datos", datos));
                    break;
                case PLAZO_FIJO:
                    if(sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
            			if (sesion.token.fechaValidacion.isNull()) {
            				LogOB.evento(contexto, "firmar", "PLAZO_FIJO FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}else {
            			if(i==0) {
            				response = OBTransmit.lecturaCsmIdAuth(contexto);
            			}
            			
            			if (!response) {
            				LogOB.evento(contexto, "firmar", "PLAZO_FIJO_TRANSMIT FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}
                    
                    try {
                        datos = firmarPlazoFijo(contexto, bandeja, empresaUsuario);
                        datosOperaciones.add(respuesta("datos", datos));
                    } catch (Exception e) {
                        if (e.getMessage().contains("CUENTA CORRIENTE NO ESTA VIGENTE")) {
                            return respuesta("CC_NO_ESTA_VIGENTE");
                        }
                        return respuesta("ERROR_PLAZO_FIJO");
                    }
                    break;
                case DEBIN_LOTE:
                    if(sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
            			if (sesion.token.fechaValidacion.isNull()) {
            				LogOB.evento(contexto, "firmar", "DEBIN_LOTE_FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}else {
            			if(i==0) {
            				response = OBTransmit.lecturaCsmIdAuth(contexto);
            			}
            			
            			if (!response) {
            				LogOB.evento(contexto, "firmar", "DEBIN_LOTE_FACTOR_NO_VALIDADO_TRANSMIT");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}
                    
                    datos = firmarDebinLote(contexto, bandeja, empresaUsuario);
                    datosOperaciones.add(respuesta("datos", datos));
                    break;
                case ECHEQ_DESCUENTO:
                    if(sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
            			if (sesion.token.fechaValidacion.isNull()) {
            				LogOB.evento(contexto, "firmar", "ECHEQ_DESCUENTO_FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}else {
            			if(i==0) {
            				response = OBTransmit.lecturaCsmIdAuth(contexto);
            			}
            			
            			if (!response) {
            				LogOB.evento(contexto, "firmar", "ECHEQ_DESCUENTO_FACTOR_NO_VALIDADO_TRANSMIT");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}
                    
                    ServicioEcheqDescuentoOB servicioEcheqDescuentoOB = new ServicioEcheqDescuentoOB(contexto);
                    EcheqDescuentoOB echeqDescuentoOB = servicioEcheqDescuentoOB.find(bandeja.id).get();
                    if (empty(echeqDescuentoOB)) {
                        return respuesta("DATOS_INVALIDOS");
                    } else {
                        datos = firmarEcheqDescuento(contexto, bandeja, empresaUsuario);
                        if (datos.toString().contains("ERROR")) {
                            datosOperaciones.add(respuesta("ERROR", "datos", datos));
                        } else {
                            try {
                                // Solo puede mandar a Factoring la actualización del estado "AUT" solo si tiene la firma completa
                                Objeto obj = (Objeto) datos;
                                if (obj.get("estadoBandeja").equals("FIRMADO COMPLETO")) {
                                    LogOB.evento(contexto, "solicitarDescuentoFactoring" + " Firmado completo");
                                    ApiCheques.descontarChequeFactoring(contexto, ConstantesOB.FACTORING_DESCUENTO_ESTADO_AUTORIZAR, echeqDescuentoOB.solicitudNumero).get();
                                }

                            } catch (ApiException e) {
                                LogOB.evento(contexto, "solicitarDescuentoFactoring", e.response.body);
                                rechazarEcheqDescuentoSinFirma(contexto, echeqDescuentoOB);
                                return respuesta("ERROR");
                            }

                            datosOperaciones.add(respuesta("datos", datos));
                        }
                    }
                    break;
                case PAGO_TARJETA:
	                if(sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
            			if (sesion.token.fechaValidacion.isNull()) {
            				LogOB.evento(contexto, "firmar", "PAGO_TARJETA_FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}else {
            			if(i==0) {
            				response = OBTransmit.lecturaCsmIdAuth(contexto);
            			}
            			
            			if (!response) {
            				LogOB.evento(contexto, "firmar", "PAGO_TARJETA_FACTOR_NO_VALIDADO_TRANSMIT");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}
	                
	                ServicioPagoTarjetaOB servicioPagoTarjetaOB = new ServicioPagoTarjetaOB(contexto);
	                PagoTarjetaOB pagoTarjetaOB = servicioPagoTarjetaOB.find(bandeja.id).get();
	                if (empty(pagoTarjetaOB)) {
	                    return respuesta("DATOS_INVALIDOS");
	                } else {
	                    datos = firmarPagoTarjeta(contexto, pagoTarjetaOB, bandeja, empresaUsuario);
	                    if (datos.toString().contains("ERROR")) {
	                        datosOperaciones.add(respuesta("ERROR", "datos", datos));
	                    } else {
	                    	datosOperaciones.add(respuesta("datos", datos));
	                    }
	                }
	                break;

                case STOP_DEBIT:
                    if(sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
            			if (sesion.token.fechaValidacion.isNull()) {
            				LogOB.evento(contexto, "firmar", "STOP_DEBIT_FACTOR_NO_VALIDADO");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}else {
            			if(i==0) {
            				response = OBTransmit.lecturaCsmIdAuth(contexto);
            			}
            			
            			if (!response) {
            				LogOB.evento(contexto, "firmar", "STOP_DEBIT_FACTOR_NO_VALIDADO_TRANSMIT");
            				return respuesta("FACTOR_NO_VALIDADO");
            			}
            		}
                    // Servicio para registrar el pago de tarjeta

                    ServicioStopDebitOB servicio = new ServicioStopDebitOB(contexto);
                    BandejaOB stopDebitOB = servicio.find(bandeja.id).get();

                    if (empty(stopDebitOB)) {
                        return respuesta("DATOS_INVALIDOS");
                    } else {
                        datos = firmarStopDebit(contexto, stopDebitOB, empresaUsuario);
                        if (datos.toString().contains("ERROR")) {
                            datosOperaciones.add(respuesta("ERROR", "datos", datos));
                        } else {
                            datosOperaciones.add(respuesta("datos", datos));
                        }
                    }
                    break;
            }
            if (datos != null) {
                if (datos.toString().contains("CHALLENGE")) {
                    contexto.sesion().challenge = id.toString();
                    sesion.save();
                    datosOperaciones.add(respuesta("CHALLENGE", "datos", datos));
                    return respuesta("datosOperaciones", datosOperaciones);
                }
                if (datos.toString().contains("DENY")) {
                    datosOperaciones.add(respuesta("DENY", "datos", datos));
                }
            }            
            i++;
        }

        sesion.token.fechaValidacion = Fecha.nunca();
        sesion.save();
        LogOB.evento(contexto, "firmar", "TERMINA FIRMAR");
        return respuesta("datosOperaciones", datosOperaciones);
    }

    private static Object firmarDebin(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        EstadoDebinRecibidasOB estadoFinalRecibida;
        EstadoDebinEnviadasOB estadoFinalEnviada;
        String funcionalidadOB = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.DEBIN.getCodigo()).get().codProdFirma.toString();
        String firmasRegistradas = firmas(contexto, bandeja);
        String firmante = contexto.sesion().usuarioOB.cuil.toString();
        ServicioEstadoBandejaOB servicioEstadoBandejaOB = new ServicioEstadoBandejaOB(contexto);
        DebinOB debin = servicioDebinOB.find(bandeja.id).get();
        Objeto datos = new Objeto();
        EstadoBandejaOB estadoBandeja = OBFirmas.validarFirmantesBandeja(contexto, bandeja.cuentaOrigen, bandeja.monto, firmante, firmasRegistradas, funcionalidadOB);
        String estado = "0";
        String errores = null;

        if (!bandeja.id.toString().equals(contexto.sesion().challenge)) {
            TransactionBankProcess.Payer companyPayer = new TransactionBankProcess.Payer(contexto.sesion().usuarioOB.cuil.toString(), debin.cbuComprador, debin.nombreComprador, "OB");
            TransactionBankProcess.Payee supplierPayee = new TransactionBankProcess.Payee(debin.cuitVendedor, debin.cuentaVendedor, debin.sucursalDescVendedor);

        TransactionBEBankProcess transactionProcess = new TransactionBEBankProcess(contexto.sesion().empresaOB.idCobis, contexto.sesion().sessionId, debin.monto, debin.moneda.descripcion, "DEBIN", companyPayer, supplierPayee);

        RecommendationDTO recommendationDTO = TransmitOB.obtenerRecomendacion(contexto, transactionProcess);
        if (recommendationDTO.getRecommendationType().equals("CHALLENGE")) {
            LogOB.evento(contexto, "firmarDebin", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
            contexto.sesion().resultadoRecomendacion = recommendationDTO;
            contexto.sesion().save();
            datos.set("estado", "CHALLENGE");
            datos.set("idBandeja", bandeja.id);
            return datos;
        }
        if (recommendationDTO.getRecommendationType().equals("DENY")) {
            LogOB.evento(contexto, "firmarDebin", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
            datos.set("estado", "DENY");
            datos.set("idBandeja", bandeja.id);
            datos.set("idOperacion", bandeja.id);
            datos.set("monto", debin.monto);
            datos.set("comprador", debin.nombreComprador);
            ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
            EstadoBandejaOB estadoDeBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.DENY.getCodigo()).get();
            debin.estadoBandeja = estadoDeBandeja;
            debin.estadoRecibida = servicioEstadoDebinRecibidasOB.find(EnumEstadoDebinRecibidasOB.RECHAZADO.getCodigo()).get();
            debin.estadoEnviada = servicioEstadoDebinEnviadasOB.find(EnumEstadoDebinEnviadasOB.RECHAZADO.getCodigo()).get();
            servicioDebinOB.update(debin);
            return datos;
        }
        }
        else{
            if(contexto.sesion().resultadoRecomendacion != null){
                TransmitOB.confirmarChallenge(contexto,contexto.sesion().resultadoRecomendacion);
            }
            contexto.sesion().challenge = "";
        }
        if (estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {
            if (!(OBPagos.validarSaldoYCuenta(contexto, bandeja.monto, bandeja.cuentaOrigen))) {
                LogOB.evento(contexto, "firmarDEBIN", "SALDO_INSUFICIENTE");
                return respuesta("ERROR", "ERROR", "SALDO_INSUFICIENTE");
            }
            try {
                Debin debinRequest = new Debin();
                debinRequest.setId(debin.idDebin);
                debinRequest.setImporte(debin.monto);

                Comprador compradorRequest = new Comprador();
                Cliente clienteRequest = new Cliente();
                clienteRequest.setIdTributario(debin.idTributarioComprador);
                clienteRequest.setNombreCompleto(debin.nombreComprador.trim());
                ar.com.hipotecario.backend.servicio.api.debin.Cuenta cuentaRequest = new ar.com.hipotecario.backend.servicio.api.debin.Cuenta();
                cuentaRequest.setCbu(debin.cbuComprador);
                cuentaRequest.setNumero(debin.cuentaComprador);
                cuentaRequest.setTipo(debin.tipoCuentaComprador);
                cuentaRequest.setSucursal(new Sucursal(debin.sucursalIdVendedor, debin.sucursalDescVendedor));
                cuentaRequest.setMoneda(new Moneda(debin.moneda.id.toString(), debin.moneda.descripcion, debin.moneda.simbolo));
                clienteRequest.setCuenta(cuentaRequest);
                compradorRequest.setCliente(clienteRequest);
                debinRequest.comprador = compradorRequest;

                Autorizar autorizar = ApiDebin.postAutorizar(contexto, debinRequest, "ACEPTAR").get();

                estadoFinalRecibida = servicioEstadoDebinRecibidasOB.find(EnumEstadoDebinRecibidasOB.REALIZADO.getCodigo()).get();
                estadoFinalEnviada = servicioEstadoDebinEnviadasOB.find(EnumEstadoDebinEnviadasOB.ACREDITADO.getCodigo()).get();

            } catch (ApiException e) {
                LogOB.evento(contexto, "FIRMA COMPLETA - ACEPTAR DEBIN", new Objeto().set("Exception", e.getMessage()).toString());
                estadoFinalRecibida = servicioEstadoDebinRecibidasOB.find(EnumEstadoDebinRecibidasOB.EN_BANDEJA.getCodigo()).get();
                estadoFinalEnviada = servicioEstadoDebinEnviadasOB.find(EnumEstadoDebinEnviadasOB.INICIADO.getCodigo()).get();
                datos.set("estado", "ERROR");
                errores = e.response.get("mensajeAlUsuario").toString();
                estado = "ERROR";
            }
        } else {
            estadoFinalRecibida = servicioEstadoDebinRecibidasOB.find(EnumEstadoDebinRecibidasOB.EN_BANDEJA.getCodigo()).get();
            estadoFinalEnviada = servicioEstadoDebinEnviadasOB.find(EnumEstadoDebinEnviadasOB.INICIADO.getCodigo()).get();
        }

        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionFirmar, bandeja.estadoBandeja, estadoBandeja).get();
        servicioHistorialDebinOB.cambiaEstado(debin, accionFirmar, empresaUsuario, estadoIniciadoEnv, estadoFinalEnviada, estadoEnBandeja, estadoFinalRecibida).get();

        debin.estadoRecibida = estadoFinalRecibida;
        debin.estadoEnviada = estadoFinalEnviada;
        debin.estadoBandeja = estadoBandeja;
        servicioDebinOB.update(debin);

        datos.set("monto", debin.monto);
        datos.set("comprador", debin.nombreComprador);

        FirmarRechazarDTO respuesta = new FirmarRechazarDTO(
                estado,
                accionFirmar.descripcion,
                bandeja.id,
                estadoBandeja.descripcion,
                debin.moneda.id.toString(),
                bandeja.cuentaOrigen,
                bandeja.monto,
                null,
                null,
                errores);

        return respuesta.armarRespuesta();
    }

    private static Object firmarDebinProgrmado(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario, Objeto listaCuentas) {
        String funcionalidadOB = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.DEBIN.getCodigo()).get().codProdFirma.toString();
        String firmasRegistradas = firmas(contexto, bandeja);
        String firmante = contexto.sesion().usuarioOB.cuil.toString();
        DebinProgramadoOB debin = servicioDebinProgramadoOB.find(bandeja.id).get();
        EstadoBandejaOB estadoBandeja;
        String estado = "0";
        String errores = null;
        //  Objeto cuenta = listaCuentas.objetos().stream().filter(c -> c.get("cbu").equals(bandeja.cuentaOrigen)).findFirst().get();
        estadoBandeja = OBFirmas.validarFirmantesBandeja(contexto, bandeja.cuentaOrigen, bandeja.monto, firmante, firmasRegistradas, funcionalidadOB);
        if (estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {
            try {
                if (debin.estado.equals(EnumEstadosDebinProgramado.BAJA_SUSCRIPCION.getCodigo())) {
                    ApiDebin.bajaRecurrencia(contexto, debin.idDebin, debin).get();
                } else {
                    AdherirRecurrencia adherirRecurrencia = ApiDebin.adherirRecurrencia(contexto, debin, true).get();
                }

            } catch (ApiException e) {
                LogOB.evento(contexto, "FIRMA COMPLETA - firmarDebinProgrmado", new Objeto().set("Exception", e.getMessage()).toString());
                errores = e.response.get("mensajeAlUsuario").toString();
                estado = "ERROR";
            }
        }
        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionFirmar, bandeja.estadoBandeja, estadoBandeja).get();
        debin.estadoBandeja = estadoBandeja;
        servicioDebinProgramadoOB.update(debin);


        FirmarRechazarDTO respuesta = new FirmarRechazarDTO(
                estado,
                accionFirmar.descripcion,
                bandeja.id,
                estadoBandeja.descripcion,
                debin.moneda.id.toString(),
                bandeja.cuentaOrigen,
                bandeja.monto,
                null,
                null,
                errores);
        Object respuestaFinal = respuesta.armarRespuesta();
        if (respuestaFinal instanceof Objeto) {
            ((Objeto) respuestaFinal).set("cuotas", debin.debinLimiteCuotas);
        }
        return respuestaFinal;

    }

    private static Object firmarAccionesEcheq(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        SesionOB sesion = contexto.sesion();
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioEcheqOB servicioEcheqOB = new ServicioEcheqOB(contexto);
        ServicioEstadoEcheqOB servicioEstadoEcheqOB = new ServicioEstadoEcheqOB(contexto);


        EcheqOB echeq = servicioEcheqOB.find(bandeja.id).get();
        String funcionalidadOB = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.ECHEQ.getCodigo()).get().codProdFirma.toString();

        String firmasRegistradas = firmas(contexto, bandeja);
        String firmante = sesion.usuarioOB.cuil.toString();
        String cuenta = puedeFirmarEcheqSinCuenta(contexto, bandeja);
        if (cuenta.equals("false")) {
            return respuesta("DATOS_INVALIDOS");
        }


        EstadoBandejaOB estadoBandeja = OBFirmas.validarFirmantesBandeja(contexto, cuenta, BigDecimal.valueOf(0.1), firmante, firmasRegistradas, funcionalidadOB);
        EstadoEcheqOB estadoEcheq;
        Objeto datos = new Objeto();
        String detalle = null;
/*
        String reason = null;
        String accountNumber = null;

        switch (echeq.accion) {
            case ENDOSO:
                reason = "ENDOSO";
                accountNumber = "";
                break;
            case CESION:
                reason = "CESION";
                accountNumber = "";
                break;
        }
        if (reason != null) {
            TransactionBankProcess.Payer companyPayer = new TransactionBankProcess.Payer(echeq.empresa.cuit.toString(), accountNumber, "044", "OB");

            TransactionBankProcess.Payee supplierPayee = new TransactionBankProcess.Payee(echeq.documentoBeneficiario, echeq.razonSocialBeneficiario, echeq.idCheque);

            TransactionBEBankProcess transactionProcess = new TransactionBEBankProcess(sesion.empresaOB.idCobis, contexto.sesion().sessionId, echeq.monto, echeq.moneda.descripcion, reason, companyPayer, supplierPayee);


            RecommendationDTO recommendationDTO = TransmitOB.obtenerRecomendacion(contexto, transactionProcess);
            if (recommendationDTO.getRecommendationType().equals("CHALLENGE")) {
                LogOB.evento(contexto, "firmarAccionesEcheq", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
                datos.set("estado", "CHALLENGE");
                datos.set("idBandeja", bandeja.id);
                return datos;
            }
            if (recommendationDTO.getRecommendationType().equals("DENY")) {
                LogOB.evento(contexto, "firmarAccionesEcheq", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
                datos.set("estado", "DENY");
                datos.set("idBandeja", bandeja.id);
                ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
                 EstadoBandejaOB estado = servicioEstadoBandeja.find(EnumEstadoBandejaOB.DENY.getCodigo()).get();
                EstadoEcheqOB estadotrn = new EstadoEcheqOB();
                estadotrn.id = EnumEstadosEcheqOB.RECHAZADO.getCodigo();
                echeq.estadoBandeja = estado;
                echeq.estado = estadotrn;
                servicioEcheqOB.update(echeq);
                return datos;
            }
        }
        */
        if (estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {
            estadoEcheq = servicioEstadoEcheqOB.find(EnumEstadoEcheqChequeraOB.PENDIENTE.getCodigo()).get();


            try {
                Cuenta c = new Cuenta();
                c.numeroProducto = echeq.cuentaOrigen;
                c.moneda = echeq.moneda.id.toString();

                datos.set("cuenta", cuenta);
                datos.set("saldo", "0.1");
                contexto.parametros.set("idOperacion", bandeja.id);
                switch (echeq.accion) {
                    case ENDOSO -> OBEcheq.endosarEcheq(contexto);
                    case CESION -> OBEcheq.emitirCesionEcheq(contexto);
                    case DEPOSITO -> OBEcheq.depositarCheque(contexto);
                    case SOLICITAR_DEVOLUCION_EMISOR -> OBEcheq.solicitarDevolucionEcheqEmisor(contexto);
                    case SOLICITAR_DEVOLUCION_ENDOSANTE -> OBEcheq.solicitarDevolucionEcheqEndosante(contexto);
                    case SOLICITAR_DEVOLUCION_AVALISTA -> OBEcheq.solicitarDevolucionEcheqAvalista(contexto);
                    case SOLICITAR_DEVOLUCION_MANDATARIO -> OBEcheq.solicitarDevolucionEcheqMandatario(contexto);
                    case ACEPTACION_DEVOLUCION -> OBEcheq.aceptarDevolucionEcheq(contexto);
                    case ANULACION -> OBEcheq.anular(contexto);
                    case CUSTODIAR -> OBEcheq.custodiarEcheq(contexto);
                    case RESCATAR -> OBEcheq.rescatarEcheq(contexto);
                    case MANDATO_NEG -> OBEcheq.mandatoEcheq(contexto);
                    case ACEPTACION_MANDATO_NEG -> OBEcheq.aceptarMandaroEcheq(contexto);
                    case REVOCATORIA_MANDATO -> OBEcheq.revocarMandatoNegociacionEcheq(contexto);
                    case SOLICITAR_AVAL -> OBEcheq.avalarEcheq(contexto, servicioEcheqOB);
                    case ADMITIR_AVAL -> OBEcheq.admitiarAvalEcheq(contexto, echeq.idCheque);
                }

            } catch (ApiException e) {
                detalle = e.response.get("mensajeAlUsuario").toString();
                estadoEcheq = servicioEstadoEcheqOB.find(EnumEstadoEcheqChequeraOB.RECHAZADO.getCodigo()).get();
                datos.set("estado", "error");
            } catch (Exception e) {
                estadoEcheq = servicioEstadoEcheqOB.find(EnumEstadoEcheqChequeraOB.RECHAZADO.getCodigo()).get();
                datos.set("estado", "error");
            }
        } else {
            estadoEcheq = servicioEstadoEcheqOB.find(EnumEstadoEcheqChequeraOB.EN_BANDEJA.getCodigo()).get();
        }

        AccionesOB accionFirmar = servicioAcciones.find(EnumAccionesOB.FIRMAR.getCodigo()).get();
        assert estadoBandeja != null;
        datos.set("estadoBandeja", estadoBandeja.descripcion);
        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionFirmar, bandeja.estadoBandeja, estadoBandeja).get();
        String estado = "0";
        if (estadoEcheq.descripcion.equals("RECHAZADO")) {
            estado = "ERROR";
        }

        FirmarRechazarDTO respuesta = new FirmarRechazarDTO(estado, accionFirmar.descripcion, bandeja.id, estadoBandeja.descripcion.toString(), echeq.moneda.id.toString(), bandeja.cuentaOrigen, bandeja.monto, null, null, detalle);

        datos.set("cuenta", bandeja.cuentaOrigen);
        datos.set("accion", "FIRMAR");
        datos.set("idOperacion", bandeja.id);
        echeq.estado = estadoEcheq;
        echeq.estadoBandeja = estadoBandeja;
        servicioEcheqOB.update(echeq);


        return respuesta.armarRespuesta();
    }

    private static Object firmarEcheq(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        SesionOB sesion = contexto.sesion();
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioEcheqOB servicioEcheqOB = new ServicioEcheqOB(contexto);
        ServicioEstadoEcheqOB servicioEstadoEcheqOB = new ServicioEstadoEcheqOB(contexto);
        ServicioHistorialEcheqOB servicioHistorialEcheqOB = new ServicioHistorialEcheqOB(contexto);
        ApiNotificaciones noti = new ApiNotificaciones();
        EcheqOB echeq = servicioEcheqOB.find(bandeja.id).get();
        String funcionalidadOB = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.ECHEQ.getCodigo()).get().codProdFirma.toString();

        String firmasRegistradas = firmas(contexto, bandeja);
        String firmante = sesion.usuarioOB.cuil.toString();

        EstadoBandejaOB estadoBandeja = OBFirmas.validarFirmantesBandeja(contexto, bandeja.cuentaOrigen, bandeja.monto, firmante, firmasRegistradas, funcionalidadOB);
        EstadoEcheqOB estadoEcheq;
        Objeto datos = new Objeto();
        String detalle = null;

/*
        TransactionBankProcess.Payer companyPayer = new TransactionBankProcess.Payer(echeq.empresa.cuit.toString(), echeq.cuentaOrigen, "044", "OB");

        TransactionBankProcess.Payee supplierPayee = new TransactionBankProcess.Payee(echeq.documentoBeneficiario, echeq.razonSocialBeneficiario, echeq.numeroChequera);

        TransactionBEBankProcess transactionProcess = new TransactionBEBankProcess(sesion.empresaOB.idCobis, contexto.sesion().sessionId, echeq.monto, echeq.moneda.descripcion, "Pago a proveedores", companyPayer, supplierPayee);

        RecommendationDTO recommendationDTO = TransmitOB.obtenerRecomendacion(contexto, transactionProcess);
        if (recommendationDTO.getRecommendationType().equals("CHALLENGE")) {
            LogOB.evento(contexto, "firmarEcheq", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
            datos.set("estado", "CHALLENGE");
            datos.set("idBandeja", bandeja.id);
            return datos;
        }
        if (recommendationDTO.getRecommendationType().equals("DENY")) {
            LogOB.evento(contexto, "firmarEcheq", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
            datos.set("estado", "DENY");
            datos.set("idBandeja", bandeja.id);
            ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
             EstadoBandejaOB estado = servicioEstadoBandeja.find(EnumEstadoBandejaOB.DENY.getCodigo()).get();
            EstadoEcheqOB estadotrn = new EstadoEcheqOB();
            estadotrn.id = EnumEstadosEcheqOB.RECHAZADO.getCodigo();
            echeq.estadoBandeja = estado;
            echeq.estado = estadotrn;
            servicioEcheqOB.update(echeq);
            return datos;
        }*/

        if (estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {
            estadoEcheq = servicioEstadoEcheqOB.find(EnumEstadoEcheqChequeraOB.PENDIENTE.getCodigo()).get();
            Objeto cuenta = OBCuentas.cuenta(contexto, bandeja.cuentaOrigen);

            try {
                Cuenta c = new Cuenta();
                c.numeroProducto = echeq.cuentaOrigen;
                c.moneda = echeq.moneda.id.toString();

                datos.set("cuenta", cuenta.get("numeroProducto"));
                datos.set("saldo", cuenta.get("saldoGirar"));
                contexto.parametros.set("idOperacion", bandeja.id);
                DetalleEmisionEcheq.result result = (DetalleEmisionEcheq.result) OBEcheq.emisionEcheq(contexto);
                echeq.idCheque = result.cheques.get(0).cheque_id;
                echeq.numeroCheque = result.cheques.get(0).cheque_numero;
                if (!echeq.emailBeneficiario.equals("")) {
                    noti.EnvioCheques(contexto, echeq.emailBeneficiario.toString(), echeq);
                }
            } catch (ApiException e) {
                detalle = e.response.get("mensajeAlUsuario").toString();
                estadoEcheq = servicioEstadoEcheqOB.find(EnumEstadoEcheqChequeraOB.RECHAZADO.getCodigo()).get();
                datos.set("estado", "error");
            }
        } else {
            estadoEcheq = servicioEstadoEcheqOB.find(EnumEstadoEcheqChequeraOB.EN_BANDEJA.getCodigo()).get();
        }

        AccionesOB accionFirmar = servicioAcciones.find(EnumAccionesOB.FIRMAR.getCodigo()).get();
        assert estadoBandeja != null;
        datos.set("estadoBandeja", estadoBandeja.descripcion);
        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionFirmar, bandeja.estadoBandeja, estadoBandeja).get();
        String estado = "0";
        if (estadoEcheq.descripcion.equals("RECHAZADO")) {
            estado = "ERROR";
        }

        FirmarRechazarDTO respuesta = new FirmarRechazarDTO(estado, accionFirmar.descripcion, bandeja.id, estadoBandeja.descripcion, echeq.moneda.id.toString(), bandeja.cuentaOrigen, bandeja.monto, null, null, estado.equals("ERROR") ? detalle : echeq.idCheque);


        echeq.estado = estadoEcheq;
        echeq.estadoBandeja = estadoBandeja;
        servicioEcheqOB.update(echeq);


        return respuesta.armarRespuesta();
    }

    private static Object firmarEcheqDescuento(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        SesionOB sesion = contexto.sesion();
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioEcheqDescuentoOB servicioEcheqDescuentoOB = new ServicioEcheqDescuentoOB(contexto);
        ServicioEstadoEcheqOB servicioEstadoEcheqOB = new ServicioEstadoEcheqOB(contexto);

        EcheqDescuentoOB echeqDescuentoOB = servicioEcheqDescuentoOB.find(bandeja.id).get();
        String funcionalidadOB = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.ECHEQ.getCodigo()).get().codProdFirma.toString();

        String firmasRegistradas = firmas(contexto, bandeja);
        String firmante = sesion.usuarioOB.cuil.toString();

        EstadoBandejaOB estadoBandeja = OBFirmas.validarFirmantesBandeja(contexto, bandeja.cuentaOrigen, bandeja.monto, firmante, firmasRegistradas, funcionalidadOB);
        EstadoEcheqOB estadoEcheq;
        Objeto datos = new Objeto();
        String detalle = null;


        if (estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {
            estadoEcheq = servicioEstadoEcheqOB.find(EnumEstadoEcheqChequeraOB.PENDIENTE.getCodigo()).get();
            Objeto cuenta = OBCuentas.cuenta(contexto, bandeja.cuentaOrigen);

            try {
                Cuenta c = new Cuenta();
                c.numeroProducto = echeqDescuentoOB.cuentaOrigen;
                c.moneda = echeqDescuentoOB.moneda.id.toString();

                datos.set("cuenta", cuenta.get("numeroProducto"));
                datos.set("saldo", cuenta.get("saldoGirar"));
                contexto.parametros.set("idOperacion", bandeja.id);

            } catch (ApiException e) {
                detalle = e.response.get("mensajeAlUsuario").toString();
                estadoEcheq = servicioEstadoEcheqOB.find(EnumEstadoEcheqChequeraOB.RECHAZADO.getCodigo()).get();
                datos.set("estado", "error");
            }
        } else {
            estadoEcheq = servicioEstadoEcheqOB.find(EnumEstadoEcheqChequeraOB.EN_BANDEJA.getCodigo()).get();
        }

        AccionesOB accionFirmar = servicioAcciones.find(EnumAccionesOB.FIRMAR.getCodigo()).get();
        assert estadoBandeja != null;
        datos.set("estadoBandeja", estadoBandeja.descripcion);
        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionFirmar, bandeja.estadoBandeja, estadoBandeja).get();
        String estado = "0";
        if (estadoEcheq.descripcion.equals("RECHAZADO")) {
            estado = "ERROR";
        }

        FirmarRechazarDTO respuesta = new FirmarRechazarDTO(estado, accionFirmar.descripcion, bandeja.id, estadoBandeja.descripcion, echeqDescuentoOB.moneda.id.toString(), bandeja.cuentaOrigen, bandeja.monto, null, null, estado.equals("ERROR") ? detalle : echeqDescuentoOB.solicitudNumero);


        echeqDescuentoOB.estado = estadoEcheq;
        echeqDescuentoOB.estadoBandeja = estadoBandeja;
        servicioEcheqDescuentoOB.update(echeqDescuentoOB);


        return respuesta.armarRespuesta();
    }

    private static Object firmarChequeraElectronica(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        SesionOB sesion = contexto.sesion();
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioChequeraOB servicioChequeraOB = new ServicioChequeraOB(contexto);
        ServicioEstadoChequeraOB servicioEstadoChequeraOB = new ServicioEstadoChequeraOB(contexto);
        ServicioHistorialChequeraOB servicioHistorialChequeraOB = new ServicioHistorialChequeraOB(contexto);

        ChequeraOB chequera = servicioChequeraOB.find(bandeja.id).get();
        String funcionalidadOB = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.CHEQUERA_ELECTRONICA.getCodigo()).get().codProdFirma.toString();

        String firmasRegistradas = firmas(contexto, bandeja);
        String firmante = sesion.usuarioOB.cuil.toString();

        EstadoBandejaOB estadoBandeja = OBFirmas.validarFirmantesBandeja(contexto, bandeja.cuentaOrigen, bandeja.monto, firmante, firmasRegistradas, funcionalidadOB);
        EstadoChequeraOB estadoChequera;
        Objeto datos = new Objeto();

/*
        TransactionBankProcess.Payer companyPayer = new TransactionBankProcess.Payer(chequera.empresa.cuit.toString(), chequera.cuentaOrigen, "044", "OB");

        TransactionBEBankProcess transactionProcess = new TransactionBEBankProcess(sesion.empresaOB.idCobis, contexto.sesion().sessionId, chequera.monto, chequera.moneda.descripcion, "Chequera", companyPayer, null);

        RecommendationDTO recommendationDTO = TransmitOB.obtenerRecomendacion(contexto, transactionProcess);
        if (recommendationDTO.getRecommendationType().equals("CHALLENGE")) {
            LogOB.evento(contexto, "firmarChequeraElectronica", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
            datos.set("estado", "CHALLENGE");
            datos.set("idBandeja", bandeja.id);
            return datos;
        }
        if (recommendationDTO.getRecommendationType().equals("DENY")) {
            LogOB.evento(contexto, "firmarChequeraElectronica", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
            datos.set("estado", "DENY");
            datos.set("idBandeja", bandeja.id);
            ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
             EstadoBandejaOB estado = servicioEstadoBandeja.find(EnumEstadoBandejaOB.DENY.getCodigo()).get();
            EstadoChequeraOB estadotrn = new EstadoChequeraOB();
            estadotrn.id = EnumEstadoEcheqChequeraOB.RECHAZADO.getCodigo();
            chequera.estadoBandeja = estado;
            chequera.estado = estadotrn;
            servicioChequeraOB.update(chequera);
            return datos;
        }
*/

        if (estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {
            estadoChequera = servicioEstadoChequeraOB.find(EnumEstadoEcheqChequeraOB.PENDIENTE.getCodigo()).get();
            Objeto cuenta = OBCuentas.cuenta(contexto, bandeja.cuentaOrigen);

            try {
                Cuenta c = new Cuenta();
                c.numeroProducto = chequera.cuentaOrigen;
                c.moneda = chequera.moneda.id.toString();

                datos.set("cuenta", cuenta.get("numeroProducto"));
                datos.set("saldo", cuenta.get("saldoGirar"));
                int nroChequera = OBEcheq.altaChequera(contexto, chequera);
                chequera.numeroChequera = String.valueOf(nroChequera);
                datos.set("numeroChequera", nroChequera);

            } catch (Exception e) {
                estadoChequera = servicioEstadoChequeraOB.find(EnumEstadoEcheqChequeraOB.RECHAZADO.getCodigo()).get();
                datos.set("Error", e.getMessage());
            }
        } else {
            estadoChequera = servicioEstadoChequeraOB.find(EnumEstadoEcheqChequeraOB.EN_BANDEJA.getCodigo()).get();
        }

        AccionesOB accionFirmar = servicioAcciones.find(EnumAccionesOB.FIRMAR.getCodigo()).get();
        assert estadoBandeja != null;
        datos.set("estadoBandeja", estadoBandeja.descripcion);
        HistorialChequeraOB historial = servicioHistorialChequeraOB.cambiaEstado(chequera, accionFirmar, empresaUsuario, chequera.estado, estadoChequera).get();
        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionFirmar, bandeja.estadoBandeja, estadoBandeja).get();
        String estado = "0";
        if (estadoChequera.descripcion.equals("RECHAZADO")) {
            estado = "ERROR";
        }
        FirmarRechazarDTO respuesta = new FirmarRechazarDTO(estado, accionFirmar.descripcion, bandeja.id, estadoBandeja.descripcion.toString(), chequera.moneda.id.toString(), bandeja.cuentaOrigen, bandeja.monto, null, null, null);

        datos.set("cuenta", bandeja.cuentaOrigen);
        datos.set("accion", historial.accion.descripcion);
        datos.set("idOperacion", bandeja.id);
        chequera.estado = estadoChequera;
        chequera.estadoBandeja = estadoBandeja;
        servicioChequeraOB.update(chequera);
        datos.add("respuesta", respuesta.armarRespuesta());

        return respuesta.armarRespuesta();
    }

    private static Object firmarCobranzaIntegral(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        SesionOB sesion = contexto.sesion();
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioCobranzaIntegralOB servicioCobranzaIntegral = new ServicioCobranzaIntegralOB(contexto);
        ServicioEstadosCobranzaIntegral servicioEstadosCobranzaIntegral = new ServicioEstadosCobranzaIntegral(contexto);
        ServicioHistorialCobranzaIntegralOB servicioHistorialCobranzaIntegral = new ServicioHistorialCobranzaIntegralOB(contexto);

        CobranzaIntegralOB cobranza = servicioCobranzaIntegral.find(bandeja.id).get();
        String funcionalidadOB = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.TRANSFERENCIAS.getCodigo()).get().codProdFirma.toString();

        String firmasRegistradas = firmas(contexto, bandeja);
        String firmante = sesion.usuarioOB.cuil.toString();

        EstadoBandejaOB estadoBandeja = OBFirmas.validarFirmantesBandeja(contexto, bandeja.cuentaOrigen, bandeja.monto, firmante, firmasRegistradas, funcionalidadOB);
        EstadosCobranzaIntegralOB estadosCobranzaIntegral;
        Objeto datos = new Objeto();


      /*  TransactionBankProcess.Payer companyPayer = new TransactionBankProcess.Payer(cobranza.empresa.cuit.toString(), cobranza.cuentaOrigen, "044", "OB");

        TransactionBankProcess.Payee supplierPayee = new TransactionBankProcess.Payee(cobranza.nombreArchivo, cobranza.convenio.toString(), cobranza.gcr);

        TransactionBEBankProcess transactionProcess = new TransactionBEBankProcess(sesion.empresaOB.idCobis, contexto.sesion().sessionId, cobranza.monto, cobranza.moneda.descripcion, "Pago a proveedores", companyPayer, supplierPayee);

        RecommendationDTO recommendationDTO = TransmitOB.obtenerRecomendacion(contexto,transactionProcess);
        if (recommendationDTO.getRecommendationType().equals("CHALLENGE")) {
            LogOB.evento(contexto, "firmarCobranzaIntegral", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
            datos.set("estado", "CHALLENGE");
            datos.set("idBandeja", bandeja.id);
            return datos;
        }
        if (recommendationDTO.getRecommendationType().equals("DENY")) {
            LogOB.evento(contexto, "firmarCobranzaIntegral", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
            datos.set("estado", "DENY");
            datos.set("idBandeja", bandeja.id);
            ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
             EstadoBandejaOB estado = servicioEstadoBandeja.find(EnumEstadoBandejaOB.DENY.getCodigo()).get();
            EstadosCobranzaIntegralOB estadotrn = new EstadosCobranzaIntegralOB();
            estadotrn.id = EnumEstadosCobranzaIntegralOB.RECHAZADO.getCodigo();
            cobranza.estadoBandeja = estado;
            cobranza.estado = estadotrn;
            servicioCobranzaIntegral.update(cobranza);
            return datos;
        }*/

        if (estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {
            estadosCobranzaIntegral = servicioEstadosCobranzaIntegral.find(EnumEstadosCobranzaIntegralOB.A_PROCESAR.getCodigo()).get();
            Objeto cuenta = OBCuentas.cuenta(contexto, bandeja.cuentaOrigen);

            try {
                Cuenta c = new Cuenta();
                c.numeroProducto = cobranza.cuentaOrigen;
                c.tipoProducto = cobranza.tipoProducto;
                c.moneda = cobranza.moneda.id.toString();

                moverArchivoFirmaCompleta(EnumTipoProductoOB.COBRANZA_INTEGRAL, contexto, cobranza.nombreArchivo);
                cobranza.fechaUltActulizacion = LocalDateTime.now();

                datos.set("cuenta", cuenta.get("numeroProducto"));
                datos.set("saldo", cuenta.get("saldoGirar"));


            } catch (Exception e) {
                estadosCobranzaIntegral = servicioEstadosCobranzaIntegral.find(EnumEstadosCobranzaIntegralOB.RECHAZADO.getCodigo()).get();
                datos.set("Error", "Fallo el pago");
            }
        } else {
            estadosCobranzaIntegral = servicioEstadosCobranzaIntegral.find(EnumEstadosCobranzaIntegralOB.EN_BANDEJA.getCodigo()).get();
        }
        AccionesOB accionFirmar = servicioAcciones.find(EnumAccionesOB.FIRMAR.getCodigo()).get();
        assert estadoBandeja != null;
        datos.set("estadoBandeja", estadoBandeja.descripcion);

        HistorialCobranzaIntegral historial = servicioHistorialCobranzaIntegral.cambiaEstado(cobranza, accionFirmar, empresaUsuario, cobranza.estado, estadosCobranzaIntegral).get();
        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionFirmar, bandeja.estadoBandeja, estadoBandeja).get();

        FirmarRechazarDTO respuesta = new FirmarRechazarDTO("0", accionFirmar.descripcion, bandeja.id, estadoBandeja.descripcion, cobranza.moneda.id.toString(), bandeja.cuentaOrigen, bandeja.monto, null, null, null);

        datos.set("cuenta", bandeja.cuentaOrigen);
        datos.set("accion", historial.accion.descripcion);
        datos.set("idOperacion", bandeja.id);
        cobranza.estado = estadosCobranzaIntegral;
        cobranza.estadoBandeja = estadoBandeja;
        servicioCobranzaIntegral.update(cobranza);


        return respuesta.armarRespuesta();

    }

    private static Object firmarDebinLote(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        SesionOB sesion = contexto.sesion();
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioDebinLoteOB servicioDebinLoteOB = new ServicioDebinLoteOB(contexto);
        ServicioEstadosDebinPorLote servicioEstadosDebinPorLote = new ServicioEstadosDebinPorLote(contexto);
        ServicioHistorialDebinLoteOB servicioHistorialDebinLoteOB = new ServicioHistorialDebinLoteOB(contexto);

        DebinLoteOB debinLoteOB = servicioDebinLoteOB.find(bandeja.id).get();
        String funcionalidadOB = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.COBRANZA_INTEGRAL.getCodigo()).get().codProdFirma.toString();

        String firmasRegistradas = firmas(contexto, bandeja);
        String firmante = sesion.usuarioOB.cuil.toString();

        EstadoBandejaOB estadoBandeja = OBFirmas.validarFirmantesBandeja(contexto, bandeja.cuentaOrigen, bandeja.monto, firmante, firmasRegistradas, funcionalidadOB);
        EstadosDebinLoteOB estados;
        EstadosDebinLoteOB estadosDebinLoteOB;
        Objeto datos = new Objeto();

        if (estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {
            estadosDebinLoteOB = servicioEstadosDebinPorLote.find(EnumEstadosDebinLoteOB.A_PROCESAR.getCodigo()).get();
            Objeto cuenta = OBCuentas.cuenta(contexto, bandeja.cuentaOrigen);

            try {
                Cuenta c = new Cuenta();
                c.numeroProducto = debinLoteOB.cuentaOrigen;
                c.tipoProducto = debinLoteOB.tipoProducto;
                c.moneda = debinLoteOB.moneda.id.toString();

                moverArchivoFirmaCompleta(EnumTipoProductoOB.COBRANZA_INTEGRAL, contexto, debinLoteOB.nombreArchivo);
                debinLoteOB.fechaUltActulizacion = LocalDateTime.now();

                datos.set("cuenta", cuenta.get("numeroProducto"));
                datos.set("saldo", cuenta.get("saldoGirar"));


            } catch (Exception e) {
                estadosDebinLoteOB = servicioEstadosDebinPorLote.find(EnumEstadosCobranzaIntegralOB.RECHAZADO.getCodigo()).get();
                datos.set("Error", "Fallo el pago");
            }
        } else {
            estadosDebinLoteOB = servicioEstadosDebinPorLote.find(EnumEstadosCobranzaIntegralOB.EN_BANDEJA.getCodigo()).get();
        }
        AccionesOB accionFirmar = servicioAcciones.find(EnumAccionesOB.FIRMAR.getCodigo()).get();
        assert estadoBandeja != null;
        datos.set("estadoBandeja", estadoBandeja.descripcion);

        HistorialDebinLote historial = servicioHistorialDebinLoteOB.cambiaEstado(debinLoteOB, accionFirmar, empresaUsuario, debinLoteOB.estado, estadosDebinLoteOB).get();
        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionFirmar, bandeja.estadoBandeja, estadoBandeja).get();

        FirmarRechazarDTO respuesta = new FirmarRechazarDTO("0", accionFirmar.descripcion, bandeja.id, estadoBandeja.descripcion, debinLoteOB.moneda.id.toString(), bandeja.cuentaOrigen, bandeja.monto, null, null, null);

        datos.set("cuenta", bandeja.cuentaOrigen);
        datos.set("accion", historial.accion.descripcion);
        datos.set("idOperacion", bandeja.id);
        debinLoteOB.estado = estadosDebinLoteOB;
        debinLoteOB.estadoBandeja = estadoBandeja;
        servicioDebinLoteOB.update(debinLoteOB);


        return respuesta.armarRespuesta();

    }

    private static Object firmarDebitoDirecto(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        SesionOB sesion = contexto.sesion();
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioDebitoDirectoOB servicioDebitoDirecto = new ServicioDebitoDirectoOB(contexto);
        ServicioEstadosDebitoDirectoOB servicioEstadosDD = new ServicioEstadosDebitoDirectoOB(contexto);
        ServicioHistorialDebitoDirectoOB servicioHistorialDD = new ServicioHistorialDebitoDirectoOB(contexto);

        DebitoDirectoOB debitoD = servicioDebitoDirecto.find(bandeja.id).get();
        String funcionalidadOB = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.TRANSFERENCIAS.getCodigo()).get().codProdFirma.toString();
        String firmasRegistradas = firmas(contexto, bandeja);
        String firmante = sesion.usuarioOB.cuil.toString();

        EstadoBandejaOB estadoBandeja = OBFirmas.validarFirmantesBandeja(contexto, bandeja.cuentaOrigen, BigDecimal.valueOf(0.1), firmante, firmasRegistradas, funcionalidadOB);
        EstadosDebitoDirectoOB estadoDebitoDirecto;
        Objeto datos = new Objeto();

       /* TransactionBankProcess.Payer companyPayer = new TransactionBankProcess.Payer(debitoD.empresa.cuit.toString(), debitoD.cuentaOrigen, "044", "OB");

        TransactionBankProcess.Payee supplierPayee = new TransactionBankProcess.Payee(debitoD.nombreArchivo, debitoD.gcr, debitoD.scr);

        TransactionBEBankProcess transactionProcess = new TransactionBEBankProcess(sesion.empresaOB.idCobis, contexto.sesion().sessionId, debitoD.monto, debitoD.moneda.descripcion, "Debito Directo", companyPayer, supplierPayee);

        RecommendationDTO recommendationDTO = TransmitOB.obtenerRecomendacion(contexto,transactionProcess);
        if (recommendationDTO.getRecommendationType().equals("CHALLENGE")) {
            LogOB.evento(contexto, "firmarDebitoDirecto", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
            datos.set("estado", "CHALLENGE");
            datos.set("idBandeja", bandeja.id);
            return datos;
        }
        if (recommendationDTO.getRecommendationType().equals("DENY")) {
            LogOB.evento(contexto, "firmarDebitoDirecto", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
            datos.set("estado", "DENY");
            datos.set("idBandeja", bandeja.id);
            ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
             EstadoBandejaOB estado = servicioEstadoBandeja.find(EnumEstadoBandejaOB.DENY.getCodigo()).get();
            EstadosDebitoDirectoOB estadotrn = new EstadosDebitoDirectoOB();
            estadotrn.id = EnumEstadoDebitoDirectoOB.RECHAZADO.getCodigo();
            debitoD.estadoBandeja = estado;
            debitoD.estado = estadotrn;
            servicioDebitoDirecto.update(debitoD);
            return datos;
        }*/


        if (estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) { //si el esquema d firmas esta completo pago queda en pendiente
            estadoDebitoDirecto = servicioEstadosDD.find(EnumEstadoDebitoDirectoOB.A_PROCESAR.getCodigo()).get();
            Objeto cuenta = OBCuentas.cuenta(contexto, bandeja.cuentaOrigen);

            try {
                Cuenta c = new Cuenta();
                c.numeroProducto = debitoD.cuentaOrigen;
                c.tipoProducto = debitoD.tipoProducto;
                c.moneda = debitoD.moneda.id.toString();

                moverArchivoFirmaCompleta(EnumTipoProductoOB.DEBITO_DIRECTO, contexto, renombrarArchivoDebitoDirecto(debitoD.convenio.toString(), debitoD.nombreArchivo, debitoD.fechaCreacion.toLocalDate()));
                debitoD.fechaUltActulizacion = LocalDateTime.now();

                datos.set("cuenta", cuenta.get("numeroProducto"));
                datos.set("saldo", cuenta.get("saldoGirar"));

            } catch (Exception e) {
                estadoDebitoDirecto = servicioEstadosDD.find(EnumEstadoDebitoDirectoOB.RECHAZADO.getCodigo()).get();
                datos.set("Error", "Fallo el pago");
            }
        } else {
            estadoDebitoDirecto = servicioEstadosDD.find(EnumEstadoDebitoDirectoOB.EN_BANDEJA.getCodigo()).get();
        }

        AccionesOB accionFirmar = servicioAcciones.find(EnumAccionesOB.FIRMAR.getCodigo()).get();

        assert estadoBandeja != null;
        datos.set("estadoBandeja", estadoBandeja.descripcion);

        HistorialDebitoDirectoOB historial = servicioHistorialDD.cambiaEstado(debitoD, accionFirmar, empresaUsuario, debitoD.estado, estadoDebitoDirecto).get();
        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionFirmar, bandeja.estadoBandeja, estadoBandeja).get();

        FirmarRechazarDTO respuesta = new FirmarRechazarDTO("0", accionFirmar.descripcion, bandeja.id, estadoBandeja.descripcion, debitoD.moneda.id.toString(), bandeja.cuentaOrigen, bandeja.monto, null, null, null);

        datos.set("cuenta", bandeja.cuentaOrigen);
        datos.set("accion", historial.accion.descripcion);
        datos.set("idOperacion", bandeja.id);
        debitoD.estado = estadoDebitoDirecto;
        debitoD.estadoBandeja = estadoBandeja;
        servicioDebitoDirecto.update(debitoD);

        return respuesta.armarRespuesta();

    }

    private static Object firmarPap(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        SesionOB sesion = contexto.sesion();
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioPagoAProveedoresOB servicioPagoAProveedores = new ServicioPagoAProveedoresOB(contexto);
        ServicioEstadosPagoAProveedoresOB servicioEstadosPAP = new ServicioEstadosPagoAProveedoresOB(contexto);
        ServicioHistorialPagoAProveedoresOB servicioHistorialPAP = new ServicioHistorialPagoAProveedoresOB(contexto);

        PagoAProveedoresOB pago = servicioPagoAProveedores.find(bandeja.id).get();
        String funcionalidadOB = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.PLAN_SUELDO.getCodigo()).get().codProdFirma.toString();

        String firmasRegistradas = firmas(contexto, bandeja);
        String firmante = sesion.usuarioOB.cuil.toString();

        EstadoBandejaOB estadoBandeja = OBFirmas.validarFirmantesBandeja(contexto, bandeja.cuentaOrigen, bandeja.monto, firmante, firmasRegistradas, funcionalidadOB);
        EstadosPagosAProveedoresOB estadoPago;
        Objeto datos = new Objeto();
        String estadoRespuesta = "0";
/*
        TransactionBankProcess.Payer companyPayer = new TransactionBankProcess.Payer(pago.empresa.cuit.toString(), pago.cuentaOrigen, "044", "OB");

        TransactionBankProcess.Payee supplierPayee = new TransactionBankProcess.Payee(pago.nombreArchivo, pago.convenio.toString(), pago.subconvenio.toString());

        TransactionBEBankProcess transactionProcess = new TransactionBEBankProcess(sesion.empresaOB.idCobis, contexto.sesion().sessionId, pago.monto, pago.moneda.descripcion, "Pago a proveedores", companyPayer, supplierPayee);

        RecommendationDTO recommendationDTO = TransmitOB.obtenerRecomendacion(contexto, transactionProcess);
        if (recommendationDTO.getRecommendationType().equals("CHALLENGE")) {
            LogOB.evento(contexto, "firmarPap", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
            datos.set("estado", "CHALLENGE");
            datos.set("idBandeja", bandeja.id);
            return datos;
        }
        if (recommendationDTO.getRecommendationType().equals("DENY")) {
            LogOB.evento(contexto, "firmarPap", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
            datos.set("estado", "DENY");
            datos.set("idBandeja", bandeja.id);
            ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
             EstadoBandejaOB estado = servicioEstadoBandeja.find(EnumEstadoBandejaOB.DENY.getCodigo()).get();
            EstadosPagosAProveedoresOB estadotrn = new EstadosPagosAProveedoresOB();
            estadotrn.id = EnumEstadoPagosDeServicioYVepsOB.RECHAZADO.getCodigo();
            pago.estadoBandeja = estado;
            pago.estado = estadotrn;
            servicioPagoAProveedores.update(pago);
            return datos;
        }

*/
        if (estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {
            estadoPago = servicioEstadosPAP.find(EnumEstadoPagosAProveedoresOB.PENDIENTE.getCodigo()).get();

            Objeto cuenta = OBCuentas.cuenta(contexto, bandeja.cuentaOrigen);

            try {
                Cuenta c = new Cuenta();
                c.numeroProducto = pago.cuentaOrigen;
                c.tipoProducto = pago.tipoProducto;
                c.moneda = pago.moneda.id.toString();

                ApiEmpresas.validaIntegridadArchivo(contexto, reemplazarTXT(pago.nombreArchivo), pago.convenio.toString(), pago.subconvenio.toString());
                moverArchivoFirmaCompleta(EnumTipoProductoOB.PAGO_PROVEEDORES, contexto, reemplazarTXT(pago.nombreArchivo));
                pago.fechaUltActulizacion = LocalDateTime.now();

                datos.set("cuenta", cuenta.get("numeroProducto"));
                datos.set("saldo", cuenta.get("saldoGirar"));

            } catch (Exception e) {
                estadoPago = servicioEstadosPAP.find(EnumEstadoPagosAProveedoresOB.RECHAZADO.getCodigo()).get();
                datos.set("Error", "Fallo el pago");
                estadoRespuesta = "ERROR";
            }

        } else {
            estadoPago = servicioEstadosPAP.find(EnumEstadoPagosAProveedoresOB.EN_BANDEJA.getCodigo()).get();
        }

        AccionesOB accionFirmar = servicioAcciones.find(EnumAccionesOB.FIRMAR.getCodigo()).get();

        assert estadoBandeja != null;
        datos.set("estadoBandeja", estadoBandeja.descripcion);

        HistorialPagoAProveedoresOB historial = servicioHistorialPAP.cambiaEstado(pago, accionFirmar, empresaUsuario, pago.estado, estadoPago).get();
        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionFirmar, bandeja.estadoBandeja, estadoBandeja).get();

        FirmarRechazarDTO respuesta = new FirmarRechazarDTO(estadoRespuesta, accionFirmar.descripcion, bandeja.id, estadoBandeja.descripcion, pago.moneda.id.toString(), bandeja.cuentaOrigen, bandeja.monto, null, null, null);

        datos.set("cuenta", bandeja.cuentaOrigen);
        datos.set("accion", historial.accion.descripcion);
        datos.set("idOperacion", bandeja.id);
        pago.estado = estadoPago;
        pago.estadoBandeja = estadoBandeja;
        servicioPagoAProveedores.update(pago);

        return respuesta.armarRespuesta();
    }

    private static Object firmarHaberes(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        SesionOB sesion = contexto.sesion();
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioPagoHaberesOB servicioPagoHaberes = new ServicioPagoHaberesOB(contexto);
        ServicioEstadoPagosHaberesOB servicioEstadoPagoHaberes = new ServicioEstadoPagosHaberesOB(contexto);
        ServicioHistorialPagoDeHaberesOB servicioHistorialPagoHaberes = new ServicioHistorialPagoDeHaberesOB(contexto);

        PagoDeHaberesOB pago = servicioPagoHaberes.find(bandeja.id).get();
        String funcionalidadOB = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.PLAN_SUELDO.getCodigo()).get().codProdFirma.toString();

        String firmasRegistradas = firmas(contexto, bandeja);
        String firmante = sesion.usuarioOB.cuil.toString();

        EstadoBandejaOB estadoBandeja = OBFirmas.validarFirmantesBandeja(contexto, bandeja.cuentaOrigen, bandeja.monto, firmante, firmasRegistradas, funcionalidadOB);
        EstadoPagosHaberesOB estadoPago;

        Objeto datos = new Objeto();
/*
        TransactionBankProcess.Payer companyPayer = new TransactionBankProcess.Payer(pago.empresa.cuit.toString(), pago.cuentaOrigen, "044", "OB");

        TransactionBEBankProcess transactionProcess = new TransactionBEBankProcess(sesion.empresaOB.idCobis, contexto.sesion().sessionId, pago.monto, pago.moneda.descripcion, "Pago Haberes", companyPayer, null);

        RecommendationDTO recommendationDTO = TransmitOB.obtenerRecomendacion(contexto, transactionProcess);
        if (recommendationDTO.getRecommendationType().equals("CHALLENGE")) {
            LogOB.evento(contexto, "firmarHaberes", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
            datos.set("estado", "CHALLENGE");
            datos.set("idBandeja", bandeja.id);
            return datos;
        }
        if (recommendationDTO.getRecommendationType().equals("DENY")) {
            LogOB.evento(contexto, "firmarHaberes", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
            datos.set("estado", "DENY");
            datos.set("idBandeja", bandeja.id);
            ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
             EstadoBandejaOB estado = servicioEstadoBandeja.find(EnumEstadoBandejaOB.DENY.getCodigo()).get();
            EstadoPagosHaberesOB estadotrn = new EstadoPagosHaberesOB();
            estadotrn.id = EnumEstadoPagosHaberesOB.RECHAZADO.getCodigo();
            pago.estadoBandeja = estado;
            pago.estado = estadotrn;
            servicioPagoHaberes.update(pago);
            return datos;
        }
*/

        if (estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {
            estadoPago = servicioEstadoPagoHaberes.find(EnumEstadoPagosHaberesOB.A_PROCESAR.getCodigo()).get();
            Objeto cuenta = OBCuentas.cuenta(contexto, bandeja.cuentaOrigen);

            try {
                Cuenta c = new Cuenta();
                c.numeroProducto = pago.cuentaOrigen;
                c.tipoProducto = pago.tipoProducto;
                c.moneda = pago.moneda.id.toString();
                String nombreArchivo = convertirNombreHaberes(pago.nombreArchivo);
                moverArchivoFirmaCompleta(EnumTipoProductoOB.PLAN_SUELDO, contexto, nombreArchivo);
                pago.fechaCargaLote = LocalDateTime.now();

                datos.set("cuenta", cuenta.get("numeroProducto"));
                datos.set("saldo", cuenta.get("saldoGirar"));

            } catch (Exception e) {
                estadoPago = servicioEstadoPagoHaberes.find(EnumEstadoPagosHaberesOB.RECHAZADO.getCodigo()).get();
                datos.set("Error", "Fallo el pago");
            }
        } else {
            estadoPago = servicioEstadoPagoHaberes.find(EnumEstadoPagosDeServicioYVepsOB.EN_BANDEJA.getCodigo()).get();
        }
        AccionesOB accionFirmar = servicioAcciones.find(EnumAccionesOB.FIRMAR.getCodigo()).get();

        assert estadoBandeja != null;
        datos.set("estadoBandeja", estadoBandeja.descripcion);

        HistorialPagoDeHaberesOB historial = servicioHistorialPagoHaberes.cambiaEstado(pago, accionFirmar, empresaUsuario, pago.estado, estadoPago).get();
        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionFirmar, bandeja.estadoBandeja, estadoBandeja).get();

        datos.set("cuenta", bandeja.cuentaOrigen);
        datos.set("accion", historial.accion.descripcion);
        datos.set("idOperacion", bandeja.id);
        pago.estado = estadoPago;
        pago.estadoBandeja = estadoBandeja;
        servicioPagoHaberes.update(pago);

        return datos;
    }

    private static Object firmarPagosVep(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        LogOB.evento(contexto, "firmarPagosVep", "INICIO");
        SesionOB sesion = contexto.sesion();
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioPagosVepOB servicioPagoVeps = new ServicioPagosVepOB(contexto);
        ServicioEstadoPagoOB servicioEstadoPago = new ServicioEstadoPagoOB(contexto);
        ServicioHistorialPagoVepsOB servicioHistorialPagoVeps = new ServicioHistorialPagoVepsOB(contexto);
        ServicioTarjetaVirtualOB servicioTarjetaVirtualOB = new ServicioTarjetaVirtualOB(contexto);
        PagosVepOB pago = servicioPagoVeps.find(bandeja.id).get();
        String funcionalidadOB = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.PAGOS_VEP.getCodigo()).get().codProdFirma.toString();

        String firmasRegistradas = firmas(contexto, bandeja);
        String firmante = sesion.usuarioOB.cuil.toString();
        LogOB.evento(contexto, "firmarPagosVep", "DATOS CARGADOS");
        EstadoBandejaOB estadoBandeja = OBFirmas.validarFirmantesBandeja(contexto, bandeja.cuentaOrigen, bandeja.monto, firmante, firmasRegistradas, funcionalidadOB);
        EstadoPagoOB estadoPago;

        Objeto datos = new Objeto();

        /*if (!(OBPagos.validarSaldoYCuenta(contexto, bandeja.monto, bandeja.cuentaOrigen))) {
            return respuesta("SALDO_INSUFICIENTE");
        }*/

/*
        TransactionBankProcess.Payer companyPayer = new TransactionBankProcess.Payer(pago.empresa.cuit.toString(), pago.cuentaOrigen, "044", "OB");

        TransactionBankProcess.Payee supplierPayee = new TransactionBankProcess.Payee(pago.descripcion, pago.numeroVep, null);

        TransactionBEBankProcess transactionProcess = new TransactionBEBankProcess(sesion.empresaOB.idCobis, contexto.sesion().sessionId, pago.monto, pago.moneda.descripcion, "Pago Vep", companyPayer, supplierPayee);

        RecommendationDTO recommendationDTO = TransmitOB.obtenerRecomendacion(contexto,transactionProcess);
        if (recommendationDTO.getRecommendationType().equals("CHALLENGE")) {
            LogOB.evento(contexto, "firmarPagosVep", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
            datos.set("estado", "CHALLENGE");
            datos.set("idBandeja", bandeja.id);
            return datos;
        }
        if (recommendationDTO.getRecommendationType().equals("DENY")) {
            LogOB.evento(contexto, "firmarPagosVep", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
            datos.set("estado", "DENY");
            datos.set("idBandeja", bandeja.id);
            ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
             EstadoBandejaOB estado = servicioEstadoBandeja.find(EnumEstadoBandejaOB.DENY.getCodigo()).get();
            EstadoPagoOB estadotrn = new EstadoPagoOB();
            estadotrn.id = EnumEstadoPagosDeServicioYVepsOB.RECHAZADO.getCodigo();
            pago.estadoBandeja = estado;
            pago.estado = estadotrn;
            ServicioPagosVepOB.update(pago);
            return datos;
        }*/

        LogOB.evento(contexto, "firmarPagosVep", "estadoBandeja.id:" + estadoBandeja.id);
        if (estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {
            Objeto cuenta = OBCuentas.cuenta(contexto, bandeja.cuentaOrigen);

            try {
                Cuenta c = new Cuenta();
                c.numeroProducto = pago.cuentaOrigen;
                c.tipoProducto = pago.tipoProducto;
                c.moneda = pago.moneda.id.toString();

                String token;

                VepsPendientes vepPendientes = new VepsPendientes();

                List<TarjetaVirtualOB> tarjetasVirtuales = sesion.tarjetasVirtuales;

                if (tarjetasVirtuales == null) {
                    LogOB.evento(contexto, "firmarPagoDeServicios", "SESION SIN TV");
                    tarjetasVirtuales = servicioTarjetaVirtualOB.buscarPorEmpresa(sesion.empresaOB).get();
                }

                if (tarjetasVirtuales != null && empty(tarjetasVirtuales)) {
                    return respuesta("TARJETA_VIRTUAL_INVALIDA");
                }

                String tarjetaVirtual = tarjetasVirtuales.get(0).nroTarjeta;

                LogOB.evento(contexto, "firmarPagosVep", "pago.numeroVep:" + pago.numeroVep);
                String vep = pago.numeroVep;
                int pagina = 1;

                if (pago.tipoConsultaLink.equals("3")) {

                    if (vep.length() != 12) {
                        vep = StringUtils.leftPad(pago.numeroVep, 12, "0");
                    }

                    vepPendientes = ApiLinkPagosVep.vepPendientesNuevo(contexto, null, null, contexto.sesion().empresaOB.cuit.toString(),
                            pago.idTributarioContribuyente, String.valueOf(sesion.empresaOB.cuit), pago.idTributarioOriginante, null,
                            tarjetaVirtual, vep, String.valueOf(pagina), pago.tipoConsultaLink).get();

                } else if (pago.tipoConsultaLink.equals("1") || pago.tipoConsultaLink.equals("2")) {

                    vepPendientes = ApiLinkPagosVep.vepPendientesNuevo(contexto, null, null, contexto.sesion().empresaOB.cuit.toString(),
                            pago.idTributarioContribuyente, String.valueOf(sesion.empresaOB.cuit), pago.idTributarioOriginante, null,
                            tarjetaVirtual, vep, String.valueOf(pagina), pago.tipoConsultaLink).get();
                }

                try {

                    while (vepPendientes.veps.stream()
                            .noneMatch(v -> v.informacionVep.nroVEP.equals(pago.numeroVep))) {
                        pagina++;


                        if (pago.tipoConsultaLink.equals("3")) {

                            if (pago.numeroVep.length() != 12) {
                                vep = StringUtils.leftPad(pago.numeroVep, 12, "0");
                            }

                            vepPendientes = ApiLinkPagosVep.vepPendientesNuevo(contexto, null, null, contexto.sesion().empresaOB.cuit.toString(),
                                    pago.idTributarioContribuyente, String.valueOf(sesion.empresaOB.cuit), pago.idTributarioOriginante, null,
                                    tarjetaVirtual, vep, String.valueOf(pagina), pago.tipoConsultaLink).get();

                        } else if (pago.tipoConsultaLink.equals("1") || pago.tipoConsultaLink.equals("2")) {

                            vepPendientes = ApiLinkPagosVep.vepPendientesNuevo(contexto, null, null, contexto.sesion().empresaOB.cuit.toString(),
                                    pago.idTributarioContribuyente, String.valueOf(sesion.empresaOB.cuit), pago.idTributarioOriginante, null,
                                    tarjetaVirtual, vep, String.valueOf(pagina), pago.tipoConsultaLink).get();
                        }
                    }

                    token = vepPendientes.veps.stream()
                            .filter(v -> v.informacionVep.nroVEP.equals(pago.numeroVep))
                            .filter(v -> v.token != null)
                            .findFirst().get().token;

                } catch (Exception e) {
                    return (respuesta("ERROR", "descripcion", "EL VEP NO DISPONIBLE PARA PAGAR " + pago.numeroVep));
                }

                VepsPagados.Vep pagoRealizado = ApiLinkPagosVep.pagarVep(contexto, pago.idTributarioCliente, pago.idTributarioEmpresa, pago.numeroTarjeta,
                        pago.numeroVep, pago.idTributarioContribuyente, pago.monto, token, c).get();

                datos.set("pago", pagoRealizado);
                datos.set("cuenta", cuenta.get("numeroProducto"));
                datos.set("saldo", cuenta.get("saldoGirar"));

                pago.fechaPago = LocalDateTime.now();
                estadoPago = servicioEstadoPago.find(EnumEstadoPagosDeServicioYVepsOB.PAGADO.getCodigo()).get();

            } catch (Exception e) {
                LogOB.evento(contexto, "firmarPagosVep", "ERROR:" + e.getMessage());
                datos.set("estado", "ERROR");
                datos.set("numeroVep", pago.numeroVep);
                datos.set("importe", pago.monto);
                datos.set("descripcion", pago.descripcion);
                estadoPago = servicioEstadoPago.find(EnumEstadoPagosDeServicioYVepsOB.RECHAZADO.getCodigo()).get();
            }
        } else {
            estadoPago = servicioEstadoPago.find(EnumEstadoPagosDeServicioYVepsOB.EN_BANDEJA.getCodigo()).get();
        }
        AccionesOB accionFirmar = servicioAcciones.find(EnumAccionesOB.FIRMAR.getCodigo()).get();

        assert estadoBandeja != null;
        datos.set("estadoBandeja", estadoBandeja.descripcion);

        HistorialPagosVepOB historial = servicioHistorialPagoVeps.cambiaEstado(pago, accionFirmar, empresaUsuario, pago.estado, estadoPago).get();
        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionFirmar, bandeja.estadoBandeja, estadoBandeja).get();

        datos.set("cuenta", bandeja.cuentaOrigen);
        datos.set("accion", historial.accion.descripcion);
        datos.set("idOperacion", bandeja.id);
        pago.estado = estadoPago;
        pago.estadoBandeja = estadoBandeja;
        servicioPagoVeps.update(pago);
        LogOB.evento(contexto, "firmarPagosVep", "FIN:");
        return datos;
    }

    private static Object firmarPagoDeServicios(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        LogOB.evento(contexto, "firmarPagoDeServicios", "INICIO");
        SesionOB sesion = contexto.sesion();
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioPagoDeServiciosOB servicioPagoDeServicios = new ServicioPagoDeServiciosOB(contexto);
        ServicioHistorialPagoDeServiciosOB servicioHistorialPagoDeServicios = new ServicioHistorialPagoDeServiciosOB(contexto);
        ServicioEstadoPagoOB servicioEstadoPago = new ServicioEstadoPagoOB(contexto);
        ServicioTarjetaVirtualOB servicioTarjetaVirtualOB = new ServicioTarjetaVirtualOB(contexto);
        PagoDeServiciosOB pago = servicioPagoDeServicios.find(bandeja.id).get();
        String funcionalidadOB = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.PAGO_SERVICIOS.getCodigo()).get().codProdFirma.toString();

        String firmasRegistradas = firmas(contexto, bandeja);
        String firmante = sesion.usuarioOB.cuil.toString();
        LogOB.evento(contexto, "firmarPagoDeServicios", "DATOS CARGADOS");
        EstadoBandejaOB estadoBandeja = OBFirmas.validarFirmantesBandeja(contexto, bandeja.cuentaOrigen, bandeja.monto, firmante, firmasRegistradas, funcionalidadOB);
        EstadoPagoOB estadoPago;
        String tarjetaVirtual = null;

        Objeto datos = new Objeto();

        datos.set("ente", pago.descripcionEnte);
        datos.set("enteId", pago.ente);
        datos.set("importe", pago.monto);
        datos.set("codigoLink", pago.codigoLink);
        datos.set("rubro", pago.rubro);

        if (!(OBPagos.validarSaldoYCuenta(contexto, bandeja.monto, bandeja.cuentaOrigen))) {
            LogOB.evento(contexto, "firmarPagoDeServicios", "SALDO_INSUFICIENTE");
            return respuesta("SALDO_INSUFICIENTE");
        }


     /*   TransactionBankProcess.Payer companyPayer = new TransactionBankProcess.Payer(pago.empresa.cuit.toString(), pago.cuentaOrigen, "044", "OB");

        TransactionBankProcess.Payee supplierPayee = new TransactionBankProcess.Payee(pago.descripcionEnte, pago.codigoLink, pago.rubro);

        TransactionBEBankProcess transactionProcess = new TransactionBEBankProcess(sesion.empresaOB.idCobis, contexto.sesion().sessionId, pago.monto, pago.moneda.descripcion, "Pago Servicios", companyPayer, supplierPayee);

        RecommendationDTO recommendationDTO = TransmitOB.obtenerRecomendacion(contexto, transactionProcess);
        if (recommendationDTO.getRecommendationType().equals("CHALLENGE")) {
            LogOB.evento(contexto, "firmarPagoDeServicios", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
            datos.set("estado", "CHALLENGE");
            datos.set("idBandeja", bandeja.id);
            return datos;
        }
        if (recommendationDTO.getRecommendationType().equals("DENY")) {
            LogOB.evento(contexto, "firmarPagoDeServicios", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
            datos.set("estado", "DENY");
            datos.set("idBandeja", bandeja.id);
            ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
             EstadoBandejaOB estado = servicioEstadoBandeja.find(EnumEstadoBandejaOB.DENY.getCodigo()).get();
            EstadoPagoOB estadotrn = new EstadoPagoOB();
            estadotrn.id = EnumEstadoPagosDeServicioYVepsOB.RECHAZADO.getCodigo();
            pago.estadoBandeja = estado;
            pago.estado = estadotrn;
            servicioPagoDeServicios.update(pago);
            return datos;
        }*/

        if (estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {
            estadoPago = servicioEstadoPago.find(EnumEstadoPagosDeServicioYVepsOB.PAGADO.getCodigo()).get();
            LogOB.evento(contexto, "firmarPagoDeServicios", "estadoPago: " + estadoPago);
            Objeto cuenta = OBCuentas.cuenta(contexto, bandeja.cuentaOrigen);
            String tipoCuenta = cuenta.get("tipoProducto").toString();

            try {
                LogOB.evento(contexto, "firmarPagoDeServicios", "PAGO");

                List<TarjetaVirtualOB> tarjetasVirtuales = sesion.tarjetasVirtuales;

                if (tarjetasVirtuales == null) {
                    LogOB.evento(contexto, "firmarPagoDeServicios", "SESION SIN TV");
                    tarjetasVirtuales = servicioTarjetaVirtualOB.buscarPorEmpresa(sesion.empresaOB).get();
                }

                Objeto tvAdh = (Objeto) OBPagos.obtenerTarjetaAdhesion(contexto, pago.codigoLink, tarjetasVirtuales, pago.ente.toString());

                if (tvAdh != null && !empty(tvAdh.get("datos.tarjeta"))) {
                    tarjetaVirtual = tvAdh.get("datos.tarjeta").toString();
                } else {
                    LogOB.evento(contexto, "firmarPagoDeServicios", "TV SIN ADHESION");
                    tarjetaVirtual = OBTarjetaVirtual.obtenerTarjetaVirtual(contexto, tarjetasVirtuales);
                }

                Pagos.ResponsePost pagoRealizado = ApiLink.crearPago(contexto, tarjetaVirtual, "80", bandeja.cuentaOrigen, tipoCuenta, String.valueOf(pago.conceptoId), pago.ente, pago.identificadorPago, pago.referencia, pago.usuarioLP, pago.monto.toString(), pago.idDeuda).get();
                datos.set("pago", pagoRealizado);
                datos.set("cuenta", cuenta.get("numeroProducto"));
                datos.set("saldo", cuenta.get("saldoGirar"));
                LogOB.evento(contexto, "firmarPagoDeServicios", "PAGO OK:" + pagoRealizado.numeroSecuencial);
            } catch (Exception e) {
                LogOB.evento(contexto, "firmarPagoDeServicios", "PAGO ERROR:" + e.getMessage());
                estadoPago = servicioEstadoPago.find(EnumEstadoPagosDeServicioYVepsOB.RECHAZADO.getCodigo()).get();
                LogOB.evento(contexto, "firmarPagoDeServicios", "PAGO ERROR PAGO ESTADO:" + estadoPago.descripcion);
                datos.set("estado", "ERROR");

            }
        } else {
            estadoPago = servicioEstadoPago.find(EnumEstadoPagosDeServicioYVepsOB.EN_BANDEJA.getCodigo()).get();
        }
        AccionesOB accionFirmar = servicioAcciones.find(EnumAccionesOB.FIRMAR.getCodigo()).get();

        assert estadoBandeja != null;
        datos.set("estadoBandeja", estadoBandeja.descripcion);

        HistorialPagoDeServiciosOB historial = servicioHistorialPagoDeServicios.cambiaEstado(pago, accionFirmar, empresaUsuario, pago.estado, estadoPago).get();
        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionFirmar, bandeja.estadoBandeja, estadoBandeja).get();

        datos.set("cuenta", bandeja.cuentaOrigen);

        datos.set("accion", historial.accion.descripcion);
        datos.set("idOperacion", bandeja.id);
        LogOB.evento(contexto, "firmarPagoDeServicios", "idOperacion:" + bandeja.id);
        LogOB.evento(contexto, "firmarPagoDeServicios", "estadoBandeja:" + estadoBandeja.descripcion);
        pago.estado = estadoPago;
        pago.estadoBandeja = estadoBandeja;
        servicioPagoDeServicios.update(pago);
        LogOB.evento(contexto, "firmarPagoDeServicios", "FIN:");
        return datos;
    }

    private static Object firmarFCI(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        LogOB.evento(contexto, "firmarFCI", "INICIO");
        SesionOB sesion = contexto.sesion();
        ServicioFCIOB servicioFCIOB = new ServicioFCIOB(contexto);
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        ServicioEstadoInversionOB servicioEstadoInversion = new ServicioEstadoInversionOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioHistorialFCI servicioHistorialFCI = new ServicioHistorialFCI(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        Objeto datos = new Objeto();
        FondosComunesOB fci = servicioFCIOB.find(bandeja.id).get();

        Object horarioValidoParaFirmar = validarHorarioFirma(contexto, fci.idFondo.idFondo, bandeja, empresaUsuario);
        LogOB.evento(contexto, "firmarFCI", "DATOS CARGADOS");
        LogOB.evento(contexto, "firmarFCI", "horarioValidoParaFirmar: " + horarioValidoParaFirmar);
        if (horarioValidoParaFirmar.equals(true)) {

            String funcionalidadOB = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.FCI.getCodigo()).get().codProdFirma.toString();
            String firmasRegistradas = firmas(contexto, bandeja);
            String firmante = sesion.usuarioOB.cuil.toString();
            LogOB.evento(contexto, "firmarFCI", "firmasRegistradas: " + firmasRegistradas);
            EstadoBandejaOB estadoBandeja = OBFirmas.validarFirmantesBandeja(contexto, bandeja.cuentaOrigen, bandeja.monto, firmante, firmasRegistradas, funcionalidadOB);
            EstadoSolicitudInversionOB estadoSolicitud = null;
            LogOB.evento(contexto, "firmarFCI", "estadoBandeja: " + estadoBandeja);
            assert estadoBandeja != null;
            try{
                LogOB.evento(contexto, "firmarFCI", "antes de transmit");
            if (fci.tipoSolicitud.equals("Rescate")) {
                LogOB.evento(contexto, "firmarFCI", "entra transmit");
                LogOB.evento(contexto, "firmarFCI", "session entra"+ contexto.sesion().sessionId);
                TransactionBankProcess.Payer peyeer = new TransactionBankProcess.Payer(contexto.sesion().usuarioOB.cuil.toString(), fci.cuentaOrigen, "044", "OB");
                TransactionBankProcess.Payee payee = new TransactionBankProcess.Payee(fci.empresa.cuit.toString(),  fci.idFondo.idFondo.toString(), fci.esTotal.toString());

                TransactionBEBankProcess transactionProcess = new TransactionBEBankProcess(sesion.empresaOB.idCobis, contexto.sesion().sessionId, fci.monto, fci.moneda.descripcion, fci.tipoSolicitud, peyeer, payee);

                RecommendationDTO recommendationDTO = TransmitOB.obtenerRecomendacion(contexto, transactionProcess);

                if (recommendationDTO.getRecommendationType().equals("DENY")) {
                    LogOB.evento(contexto, "firmarFCI", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
                    datos.set("estado", "DENY");
                    datos.set("idBandeja", bandeja.id);
                    ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
                    EstadoBandejaOB estado = servicioEstadoBandeja.find(EnumEstadoBandejaOB.DENY.getCodigo()).get();
                    EstadoSolicitudInversionOB estadotrn = new EstadoSolicitudInversionOB();
                    estadotrn.id = EnumEstadoSolicitudFCIOB.RECHAZADA.getCodigo();
                    fci.estadoBandeja = estado;
                    fci.estado = estadotrn;
                    servicioFCIOB.update(fci);
                    return datos;
                }
            }
                LogOB.evento(contexto, "firmarFCI", "fin transmit");
            }catch (Exception e) {
                LogOB.evento(contexto, "firmarFCI", "Error al obtener recomendacion: " + e.getMessage());
            }
            LogOB.evento(contexto, "firmarFCI", "sale transmit");
            if (estadoBandeja.id == (EnumEstadoBandejaOB.FIRMADO_COMPLETO).getCodigo()) {
                LogOB.evento(contexto, "firmarFCI", "fci.tipoSolicitud: " + fci.tipoSolicitud);
                if (fci.tipoSolicitud.equals("Suscripcion")) {
                    try {
                        Objeto suscripcion = (Objeto) OBInversiones.ejecutarSuscripcion(contexto, fci, bandeja);
                        estadoSolicitud = servicioEstadoInversion.find(EnumEstadoSolicitudFCIOB.EN_PROCESO.getCodigo()).get();

                        Solicitudes solicitudes = ApiInversiones.getSolicitudes(contexto, Fecha.ayer().toString(), Fecha.mañana().toString(), null, null, null, null, null, Integer.valueOf(fci.idCuotapartista), null).get();

                        for (Solicitudes.Solicitud solicitud : solicitudes) {
                            if (solicitud.NumSolicitud.toString().equals(suscripcion.get("NumSolicitud").toString())) {
                                estadoSolicitud = servicioEstadoInversion.find(EnumEstadoSolicitudFCIOB.REALIZADA.getCodigo()).get();
                                fci.idTransaccion = solicitud.NumSolicitud.toString();
                            }
                        }

                        datos.set("fechaConcertacion", suscripcion.get("FechaConcertacion"));
//                        datos.set("idSolicitud", suscripcion.get("NumSolicitud"));
                        datos.set("moneda", suscripcion.get("Moneda.Description"));
                        datos.set("fondo", suscripcion.get("InversionFondo.Fondo.ID"));

                    } catch (ApiException apiException) {
                        return (apiException.response);
                    } catch (NullPointerException nullException) {
                        return respuesta("DATOS_INVALIDOS");
                    } catch (RuntimeException rte) {
                        return respuesta("ERROR", "descripcion", rte.getMessage());
                    }

                } else if (fci.tipoSolicitud.equals("Rescate")) {
                    try {
                        Objeto rescate = OBInversiones.ejecutarRescate(contexto, fci, bandeja);
                        estadoSolicitud = servicioEstadoInversion.find(EnumEstadoSolicitudFCIOB.EN_PROCESO.getCodigo()).get();

                        Solicitudes solicitudes = ApiInversiones.getSolicitudes(contexto, Fecha.ayer().toString(), Fecha.mañana().toString(), null, null, null, null, null, Integer.valueOf(fci.idCuotapartista), null).get();

                        for (Solicitudes.Solicitud solicitud : solicitudes) {
                            if (solicitud.NumSolicitud.toString().equals(rescate.get("NumSolicitud").toString())) {
                                estadoSolicitud = servicioEstadoInversion.find(EnumEstadoSolicitudFCIOB.REALIZADA.getCodigo()).get();
                                fci.idTransaccion = solicitud.NumSolicitud.toString();
                            }
                        }

                        servicioFCIOB.update(fci);

//                        datos.set("idSolicitud", rescate.get("NumSolicitud"));
                        datos.set("esTotal", rescate.get("EsTotal"));
                        datos.set("fechaAcreditacion", rescate.get("FechaAcreditacion"));
                        //datos.set("idMoneda", rescate.get("Moneda.ID"));
                        datos.set("moneda", rescate.get("Moneda.Description").toString().split(" ")[0]);

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        LocalDateTime fecha = LocalDateTime.parse(rescate.get("Hora").toString(), formatter);
                        datos.set("hora", fecha.toLocalTime().toString().trim());

                    } catch (ApiException apiException) {
                        return (apiException.response);
                    } catch (NullPointerException e) {
                        return respuesta("DATOS_INVALIDOS");
                    } catch (RuntimeException rte) {
                        return respuesta("ERROR", "descripcion", rte.getMessage());
                    }
                }

            } else if (estadoBandeja.id == (EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo())) {
                estadoSolicitud = servicioEstadoInversion.find(EnumEstadoSolicitudFCIOB.RECHAZADA.getCodigo()).get();
            } else {
                estadoSolicitud = servicioEstadoInversion.find(EnumEstadoSolicitudFCIOB.PENDIENTE.getCodigo()).get();
            }

            if (estadoBandeja.id == (EnumEstadoBandejaOB.FIRMADO_COMPLETO).

                    getCodigo() || estadoBandeja.id == EnumEstadoBandejaOB.PARCIALMENTE_FIRMADA.getCodigo() || estadoBandeja.id == EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()) {
                AccionesOB accionFirmar = servicioAcciones.find(EnumAccionesOB.FIRMAR.getCodigo()).get();

                datos.set("estadoBandeja", estadoBandeja.descripcion);

                HistorialFCIOB historial = servicioHistorialFCI.cambiaEstado(fci, accionFirmar, empresaUsuario, fci.estado, estadoSolicitud).get();
                servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionFirmar, bandeja.estadoBandeja, estadoBandeja).get();

                datos.set("accion", historial.accion.descripcion);
                datos.set("idSolicitudFCI", bandeja.id);

                fci.estado = estadoSolicitud;
                fci.estadoBandeja = estadoBandeja;
                servicioFCIOB.update(fci);
            }
        } else {
            datos.set("FONDO FUERA DE HORARIO", horarioValidoParaFirmar);
        }

        return datos;

    }

    private static Object firmarPerfilInversor(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        LogOB.evento(contexto, "firmarPerfilInversor", "INICIO");
        SesionOB sesion = contexto.sesion();
        ServicioFCI servicioFCI = new ServicioFCI(contexto);
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        ServicioEstadoInversionOB servicioEstadoInversion = new ServicioEstadoInversionOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioPerfilInversorOB servicioPerfilInversor = new ServicioPerfilInversorOB(contexto);
        ServicioHistorialSolicitudPI servicioHistorialSolicitudPi = new ServicioHistorialSolicitudPI(contexto);
        SolicitudPerfilInversorOB perfil = servicioPerfilInversor.find(bandeja.id).get();
        String funcionalidadOB = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.PERFIL_INVERSOR.getCodigo()).get().codProdFirma.toString();

        String firmasRegistradas = firmas(contexto, bandeja);
        String firmante = sesion.usuarioOB.cuil.toString();
        String cuenta = (String) OBFirmas.puedeFirmarPerfilInversor(contexto);
        LogOB.evento(contexto, "firmarPerfilInversor", "DATOS CARGADOS");
        EstadoBandejaOB estadoBandeja = OBFirmas.validarFirmantesBandeja(contexto, cuenta, bandeja.monto, firmante, firmasRegistradas, funcionalidadOB);
        EstadoSolicitudInversionOB estadoInversion;
        LogOB.evento(contexto, "firmarPerfilInversor", "ESTADO BANDEJA: " + estadoBandeja);
        Objeto datos = new Objeto();

        assert estadoBandeja != null;
        if (estadoBandeja.id == (EnumEstadoBandejaOB.FIRMADO_COMPLETO).getCodigo()) {
            servicioFCI.setPerfil(contexto, sesion.empresaOB.idCobis, EnumPerfilInversorOB.valueOf(perfil.nombrePerfil));
            estadoInversion = servicioEstadoInversion.find(EnumEstadoSolicitudFCIOB.REALIZADA.getCodigo()).get();
        } else if (estadoBandeja.id == (EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo())) {
            estadoInversion = servicioEstadoInversion.find(EnumEstadoSolicitudFCIOB.RECHAZADA.getCodigo()).get();
        } else {
            estadoInversion = servicioEstadoInversion.find(EnumEstadoSolicitudFCIOB.PENDIENTE.getCodigo()).get();
        }
        LogOB.evento(contexto, "firmarPerfilInversor", "ESTADO INVERSION: " + estadoInversion);
        if (estadoBandeja.id == (EnumEstadoBandejaOB.FIRMADO_COMPLETO).getCodigo() || estadoBandeja.id == EnumEstadoBandejaOB.PARCIALMENTE_FIRMADA.getCodigo() || estadoBandeja.id == EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()) {
            LogOB.evento(contexto, "firmarPerfilInversor", "ID: " + bandeja.id);
            AccionesOB accionFirmar = servicioAcciones.find(EnumAccionesOB.FIRMAR.getCodigo()).get();

            datos.set("estadoBandeja", estadoBandeja.descripcion);

            HistorialSolicitudPerfilInversorOB historial = servicioHistorialSolicitudPi.cambiaEstado(perfil, accionFirmar, empresaUsuario, perfil.estado, estadoInversion).get();
            servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionFirmar, bandeja.estadoBandeja, estadoBandeja).get();

            datos.set("accion", historial.accion.descripcion);
            datos.set("idSolicitudPerfilInversor", bandeja.id);

            perfil.estado = estadoInversion;
            perfil.estadoBandeja = estadoBandeja;
            servicioPerfilInversor.update(perfil);
        }
        LogOB.evento(contexto, "firmarPerfilInversor", "FIRMADA: ESTADO-" + estadoInversion);
        return datos;
    }

    private static Object firmarTransferencia(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        LogOB.evento(contexto, "firmarTransferencia", "INICIO");
        SesionOB sesion = contexto.sesion();
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioTransferenciaOB servicioTransferencia = new ServicioTransferenciaOB(contexto);
        ServicioHistorialTrnOB servicioHistorialTransferencia = new ServicioHistorialTrnOB(contexto);
        TransferenciaOB transferencia = servicioTransferencia.find(bandeja.id).get();
        ServicioCamaraHorarioOB servicioCamaraHorarioOB = new ServicioCamaraHorarioOB(contexto);
        String funcionalidadOB = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.TRANSFERENCIAS.getCodigo()).get().codProdFirma.toString();
        Objeto datos = new Objeto();
        LogOB.evento(contexto, "ids operaciones:", bandeja.id.toString()+ " - "+contexto.sesion().challenge);
        OBTransferencias.validarFechaAplicacionTransferencia(contexto, transferencia);
        try{
        if (!bandeja.id.toString().equals(contexto.sesion().challenge)) {
            TransactionBankProcess.Payer companyPayer = new TransactionBankProcess.Payer(contexto.sesion().usuarioOB.cuil.toString(), transferencia.debito.cbu, "044", "OB");

            TransactionBankProcess.Payee supplierPayee = new TransactionBankProcess.Payee(transferencia.credito.cuit, transferencia.credito.cbu, transferencia.credito.banco.codigo.toString());

            TransactionBEBankProcess transactionProcess = new TransactionBEBankProcess(sesion.empresaOB.idCobis, contexto.sesion().sessionId, transferencia.monto, transferencia.moneda.descripcion, "transferencia", companyPayer, supplierPayee);

            RecommendationDTO recommendationDTO = TransmitOB.obtenerRecomendacion(contexto, transactionProcess);

            if (recommendationDTO.getRecommendationType().equals("CHALLENGE")) {
                LogOB.evento(contexto, "firmarTransferencia", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
                contexto.sesion().resultadoRecomendacion = recommendationDTO;
                contexto.sesion().save();
                datos.set("estado", "CHALLENGE");
                datos.set("idBandeja", bandeja.id);
                return datos;
            }
            if (recommendationDTO.getRecommendationType().equals("DENY")) {
                LogOB.evento(contexto, "firmarTransferencia", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
                datos.set("estado", "DENY");
                datos.set("idBandeja", bandeja.id);
                ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
                EstadoBandejaOB estado = servicioEstadoBandeja.find(EnumEstadoBandejaOB.DENY.getCodigo()).get();
                EstadoTRNOB estadotrn = new EstadoTRNOB();
                estadotrn.id = EnumEstadoTRNOB.RECHAZADO.getCodigo();
                bandeja.estadoBandeja = estado;
                transferencia.estadoBandeja = estado;
                transferencia.estado = estadotrn;
                servicioTransferencia.update(transferencia);
                return datos;
            }
        }else{
            if(contexto.sesion().resultadoRecomendacion != null){
                TransmitOB.confirmarChallenge(contexto,contexto.sesion().resultadoRecomendacion);
            }
            contexto.sesion().challenge = "";
        }
        }catch (Exception e){
            LogOB.evento(contexto,"pedir recomendacion","rompio pedir recomendacion");
        }

        if (transferencia.estadoBandeja.id != EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()) {
            String firmasRegistradas = firmas(contexto, bandeja);
            String firmante = sesion.usuarioOB.cuil.toString();
            LogOB.evento(contexto, "firmarTransferencia", "CARGADOS LOS DATOS");
            EstadoBandejaOB estadoBandeja = OBFirmas.validarFirmantesBandeja(contexto, bandeja.cuentaOrigen, bandeja.monto, firmante, firmasRegistradas, funcionalidadOB);
            LogOB.evento(contexto, "firmarTransferencia", "IdOperacion: " + bandeja.id);
            LogOB.evento(contexto, "firmarTransferencia", "estado en bandeja: " + bandeja.estadoBandeja.descripcion);

            LocalDate fechaAplicacion = transferencia.fechaAplicacion;
            LogOB.evento(contexto, "firmarTransferencia", "fechaAplicacion: " + fechaAplicacion);
            boolean diferida = fechaAplicacion.isAfter(LocalDate.now());
            LogOB.evento(contexto, "firmarTransferencia", "DIFERIDA: " + diferida);
            LogOB.evento(contexto, "firmarTransferencia", "ESTADO EN BANDEJA INICIO: " + estadoBandeja.id);
            boolean vaPorApi = false;
            if (estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {
                if (empty(transferencia.fechaEjecucion) || transferencia.fechaEjecucion == null) {
                    LogOB.evento(contexto, "FIRMAR TRANSFERENCIA ID", new Objeto().set("id_transferencia", transferencia.id.toString()));
                    LogOB.evento(contexto, "FIRMAR TRANSFERENCIA Fecha Aplicacion", new Objeto().set("aplicacion", transferencia.fechaAplicacion.toString()));
                } else {
                    LogOB.evento(contexto, "FIRMAR TRANSFERENCIA ID", new Objeto().set("id_transferencia", transferencia.id.toString()));
                    LogOB.evento(contexto, "FIRMAR TRANSFERENCIA Fecha Aplicacion", new Objeto().set("aplicacion", transferencia.fechaAplicacion.toString()));
                    LogOB.evento(contexto, "FIRMAR TRANSFERENCIA Fecha Aplicacion", new Objeto().set("fechaEjecucion", transferencia.fechaEjecucion.toString()));
                }

                boolean camaraCerrada = transferencia.camara.id == 99 ? true : OBTransferencias.camaraCerrada(new ServicioCamaraHorarioOB(contexto).find(transferencia.camara.id).get());
                if (empty(fechaAplicacion) || fechaAplicacion == null) {
                    Objeto horario = (Objeto) OBTransferencias.horarioCamara(contexto);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    vaPorApi = (transferencia.camara.id == 99 || camaraCerrada) && transferencia.tipo.id != 2;
                    fechaAplicacion = vaPorApi || !camaraCerrada ? LocalDate.now() : LocalDate.parse(horario.get("datos.habilPosterior").toString(), formatter);
                } else {
                    Fecha fechaEjecucion = new Fecha(fechaAplicacion.toString(), "yyyy-MM-dd");
                    DiaBancario dia = ApiCatalogo.diaBancario(contexto, fechaEjecucion).get();
                    Boolean camaraAbierta = !camaraCerrada;
                    boolean fueProgramada = fechaAplicacion.isAfter(LocalDate.now());
                    if (transferencia.camara.id != 99) {
                        HorarioCamaraOB horarioCamaraOB = servicioCamaraHorarioOB.find(transferencia.camara.id).get();
                        if (fueProgramada) {
                            if (dia.esDiaHabil()) {
                                fechaAplicacion = fechaEjecucion.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            } else {
                                fechaAplicacion = dia.diaHabilPosterior.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            }
                            vaPorApi = false;
                        } else {
                            if (dia.esDiaHabil() && camaraAbierta) {
                                fechaAplicacion = fechaEjecucion.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                                vaPorApi = false;
                            } else if (dia.esDiaHabil() && !camaraAbierta && LocalDateTime.now().toLocalTime().isBefore(horarioCamaraOB.horaInicio.toLocalTime())) {
                                fechaAplicacion = fechaEjecucion.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                                vaPorApi = transferencia.tipo.id != 2;
                                diferida = !vaPorApi;
                            } else if (!dia.esDiaHabil() || camaraCerrada) {
                                vaPorApi = transferencia.tipo.id != 2;
                                diferida = !vaPorApi;
                                fechaAplicacion = diferida ? dia.diaHabilPosterior.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : fechaEjecucion.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                            }
                        }
                    } else {
                        vaPorApi = true;
                    }


//                    if (diferida){
//                        vaPorApi = false;
//                        fechaAplicacion = dia.esDiaHabil()? fechaEjecucion.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate():dia.diaHabilPosterior.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//                    }else{
//                        vaPorApi = (transferencia.camara.id == 99|| camaraCerrada) && transferencia.tipo.id!=2;
//                        fechaAplicacion = LocalDate.now();
//                    }

                }
            }

            String json = "monto: " + transferencia.monto + " cuenta debito: " + transferencia.cuentaOrigen + " cbu: " + transferencia.debito.cbu + " emisor: " + transferencia.debito.descripcion +
                    " cuenta credito: " + transferencia.credito.nroCuenta + " cbu : " + transferencia.credito.cbu + " receptor: " + transferencia.credito.titular + " cuit/cuil: " + transferencia.credito.cuit;
            LogOB.evento(contexto, "firmarTransferencia", json);
            EstadoTRNOB estadoTrn = OBFirmas.validarFirmantesTrn(contexto, diferida, estadoBandeja);
            LogOB.evento(contexto, "firmarTransferencia", "estadoTrn: " + estadoTrn.descripcion);
            assert estadoTrn != null;
            if (estadoTrn.id == EnumEstadoTRNOB.EN_PROCESO.getCodigo() || estadoTrn.id == EnumEstadoTRNOB.PROGRAMADA.getCodigo() || estadoBandeja.id == EnumEstadoBandejaOB.PARCIALMENTE_FIRMADA.getCodigo() || estadoBandeja.id == EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()) {

                AccionesOB accionFirmar = servicioAcciones.find(EnumAccionesOB.FIRMAR.getCodigo()).get();
                assert estadoBandeja != null;
                transferencia.estado = estadoTrn;
                transferencia.estadoBandeja = estadoBandeja;
                datos.set("estadoBandeja", estadoBandeja.descripcion);
                LogOB.evento(contexto, "firmarTransferencia", "ID: " + bandeja.id);
                LogOB.evento(contexto, "firmarTransferencia", "ESTADO EN BANDEJA FIN: " + estadoBandeja.descripcion);
                HistorialTrnOB historial = servicioHistorialTransferencia.cambiaEstado(transferencia, accionFirmar, empresaUsuario, transferencia.estado, estadoTrn).get();
                servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionFirmar, bandeja.estadoBandeja, estadoBandeja).get();

                datos.set("accion", historial.accion.descripcion);
                datos.set("idTransferencia", bandeja.id);
                ServicioEstadoTRNOB servicioEstadoTRNOB = new ServicioEstadoTRNOB(contexto);
                transferencia.fechaAplicacion = fechaAplicacion;
                if (estadoTrn.id == EnumEstadoTRNOB.EN_PROCESO.getCodigo()) {
                    LogOB.evento(contexto, "Nueva transferencia CREDIN", "vaPorAPI:" + vaPorApi);
                    if (!vaPorApi) {
                        try {
                            PeticionesOB.PeticionOB peticionOB = new PeticionesOB.PeticionOB(transferencia);
                            PeticionesOB.post(contexto, peticionOB);
                            LogOB.evento(contexto, "registrarEnCore", "REGISTRADO EN ATE");
                            transferencia.fechaEjecucion = fechaAplicacion.atStartOfDay();
                            transferencia.fechaAplicacion = fechaAplicacion;
                            LogOB.evento(contexto, "registrarEnCore", "idTransferencia: " + transferencia.id);
                            LogOB.evento(contexto, "registrarEnCore", "fechaAplicacion: " + transferencia.fechaAplicacion);
                            LogOB.evento(contexto, "registrarEnCore", "fechaEjecucion: " + transferencia.fechaEjecucion);
                        } catch (Exception e) {
                            datos.set("estado", "ERROR");
                            LogOB.evento(contexto, "CAMBIO_ESTADO_TRANSFERENCIA", new Objeto().set("idTransferencia", transferencia.id).set("error", e.getMessage()));
                            transferencia.estado = servicioEstadoTRNOB.find(EnumEstadoTRNOB.RECHAZADO.getCodigo()).get();
                        }
                    } else {

                        try {
                            if (!(transferencia instanceof TransferenciaCredinOB)) {
                                transferencia = new ServicioTransferenciaCredinOB(contexto).crearDesdeTransferenciaOB(transferencia.id);
                                LogOB.evento(contexto, "Nueva transferencia CREDIN", "transferencia id:" + transferencia.id);
                            }
                            transferencia.estado = estadoTrn;
                            transferencia.estadoBandeja = estadoBandeja;
                            String sucursal = agregarCerosAIzquierda(OBCuentas.cuenta(contexto, transferencia.cuentaOrigen).get("sucursal").toString(), 4);
                            AutorizarCredin autorizarCredin = ApiDebin.autorizarCredin(contexto, transferencia.credito.cbu, agregarCerosAIzquierda(transferencia.credito.banco.codigo.toString(), 3), transferencia.credito.cuit, transferencia.credito.titular, transferencia.concepto.codigo, transferencia.monto.toString(), transferencia.moneda.id.toString(), transferencia.moneda.simbolo, transferencia.moneda.descripcion, transferencia.debito.cbu, transferencia.debito.nroCuenta, sucursal, transferencia.debito.tipoCuenta.descripcionLarga, transferencia.emp_codigo.cuit.toString(), "", transferencia.emp_codigo.razonSocial, Long.valueOf(transferencia.id)).get();
                            transferencia.fechaEjecucion = fechaAplicacion.atStartOfDay();
                            transferencia.fechaAplicacion = fechaAplicacion;
                            LogOB.evento(contexto, "registrarEnCore", "idTransferencia: " + transferencia.id);
                            LogOB.evento(contexto, "registrarEnCore", "fechaAplicacion: " + transferencia.fechaAplicacion);
                            LogOB.evento(contexto, "registrarEnCore", "fechaEjecucion: " + transferencia.fechaEjecucion);
                            if (!autorizarCredin.descripcion.equals("GARANTIA CORRECTA")) {
                                datos.set("estado", "ERROR");
                                LogOB.evento(contexto, "Nueva transferencia CREDIN ERROR", "descripcion: " + autorizarCredin.descripcion);
                                transferencia.estado = servicioEstadoTRNOB.find(EnumEstadoTRNOB.RECHAZADO.getCodigo()).get();
                            }
                            ((TransferenciaCredinOB) transferencia).idDebin = autorizarCredin.idCoelsa;
                        } catch (Exception e) {
                            datos.set("estado", "ERROR");
                            LogOB.evento(contexto, "CAMBIO_ESTADO_TRANSFERENCIA", new Objeto().set("idTransferencia", transferencia.id).set("error", e.getMessage()));
                            transferencia.estado = servicioEstadoTRNOB.find(EnumEstadoTRNOB.RECHAZADO.getCodigo()).get();
                        }
                    }

                }
                servicioTransferencia.update(transferencia);
                LogOB.evento(contexto, "firmarTransferencia", "ESTADO TRN : " + estadoTrn.descripcion);


            }
        } else {
            datos.set("ERROR", "SE RECHAZA TRANSFERENCIA POR FECHA DE APLICACION PASADA");
        }

        LogOB.evento(contexto, "firmarTransferencia", "FIN");
        return datos;
    }

    private static Object firmarCedip(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        LogOB.evento(contexto, "firmarCedip", "INICIO");
        SesionOB sesion = contexto.sesion();
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioCedipOB servicioCedip = new ServicioCedipOB(contexto);

        String funcionalidadOB = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.PERFIL_INVERSOR.getCodigo()).get().codProdFirma.toString();

        String firmasRegistradas = firmas(contexto, bandeja);
        String firmante = sesion.usuarioOB.cuil.toString();

        EstadoBandejaOB estadoBandeja = OBFirmas.validarFirmantesBandeja(contexto, bandeja.cuentaOrigen, bandeja.monto, firmante, firmasRegistradas, funcionalidadOB);
        LogOB.evento(contexto, "firmarCedip", "ESTADO EN BANDEJA INICIO " + estadoBandeja.id);
        CedipOB cedip = new CedipOB();
        cedip = servicioCedip.find(bandeja.id).get();
        LocalDateTime fechaAplicacion = cedip.fecha_accion;

        Objeto datos = new Objeto();


        if (estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {

            AccionesOB accionFirmar = servicioAcciones.find(EnumAccionesOB.FIRMAR.getCodigo()).get();
            assert estadoBandeja != null;

            servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionFirmar, bandeja.estadoBandeja, estadoBandeja).get();

            if (cedip != null) {
                LogOB.evento(contexto, "firmarCedip", "GENERA CEDIP");
                CedipNuevo nuevoCedip = ApiPlazosFijos.nuevoCedip(contexto, cedip).get();

                LogOB.evento(contexto, "firmarCedip", "RESPUESTA: " + nuevoCedip.codigoHttp());
                cedip.estadoBandeja = estadoBandeja;
                cedip.estado_firma = "Firmado";
                cedip.fechaUltActulizacion = cedip.fecha_accion.now();
                servicioCedip.update(cedip);

                datos.set("idCedip", bandeja.id);
                datos.set("estadoBandeja", estadoBandeja.descripcion);

            }
        }
        LogOB.evento(contexto, "firmarCedip", "FIN");
        return datos;
    }

    private static Object firmarCedipAcciones(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        SesionOB sesion = contexto.sesion();
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioCedipOB servicioCedip = new ServicioCedipOB(contexto);

        String funcionalidadOB = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.PERFIL_INVERSOR.getCodigo()).get().codProdFirma.toString();

        String firmasRegistradas = firmas(contexto, bandeja);
        String firmante = sesion.usuarioOB.cuil.toString();

        EstadoBandejaOB estadoBandeja = OBFirmas.validarFirmantesBandeja(contexto, bandeja.cuentaOrigen, bandeja.monto, firmante, firmasRegistradas, funcionalidadOB);

        CedipAccionesOB cedipA = new CedipAccionesOB();

        cedipA = servicioCedip.findAcc(bandeja.id).get();
        LocalDateTime fechaAplicacion = cedipA.fecha_accion;

        Objeto datos = new Objeto();

        if (estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {

            AccionesOB accionFirmar = servicioAcciones.find(EnumAccionesOB.FIRMAR.getCodigo()).get();
            assert estadoBandeja != null;

            servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionFirmar, bandeja.estadoBandeja, estadoBandeja).get();

            ResponsePostTransmision response = new ResponsePostTransmision();

            if (cedipA.accion.equals("transmitir")) {
                response = ApiPlazosFijos.transmitirCedip(contexto, cedipA).get();
            }
            if (cedipA.accion.equals("anular")) {
                response = ApiPlazosFijos.anularTransmisionCedip(contexto, cedipA).get();
            }
            if (cedipA.accion.equals("modificar")) {
                response = ApiPlazosFijos.modificarAcreditacionCbuCedip(contexto, cedipA).get();
            }
            if (cedipA.accion.equals("depositar")) {
                response = ApiPlazosFijos.depositarCedip(contexto, cedipA).get();
            }


            if (response.codigoHttp() == 500 || response.codigoHttp() == 400 || response.codigoHttp() == 405) {
                estadoBandeja.id = EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo();
                estadoBandeja.descripcion = "RECHAZADO_EN_FIRMA";
                cedipA.estadoBandeja = estadoBandeja;
            }
            if (response.codigoHttp() == 200) {
                estadoBandeja.id = EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo();
                estadoBandeja.descripcion = "FIRMADO_COMPLETO";
                cedipA.estadoBandeja = estadoBandeja;
            }
            cedipA.estado_firma = estadoBandeja.descripcion;
            cedipA.fechaUltActulizacion = cedipA.fecha_accion.now();
            servicioCedip.update(cedipA);

            datos.set("response", response);
            datos.set("estadoBandeja", estadoBandeja.descripcion);
            datos.set("idCedip", bandeja.id);
        }
        return datos;
    }

    private static Object firmarPlazoFijo(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        SesionOB sesion = contexto.sesion();
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioPlazoFijoOB servicioPlazoFijoOB = new ServicioPlazoFijoOB(contexto);

        String funcionalidadOB = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.PERFIL_INVERSOR.getCodigo()).get().codProdFirma.toString();

        String firmasRegistradas = firmas(contexto, bandeja);
        String firmante = sesion.usuarioOB.cuil.toString();

        EstadoBandejaOB estadoBandeja = OBFirmas.validarFirmantesBandeja(contexto, bandeja.cuentaOrigen, bandeja.monto, firmante, firmasRegistradas, funcionalidadOB);

        PlazoFijoOB plazoFijo = new PlazoFijoOB();
        plazoFijo = servicioPlazoFijoOB.find(bandeja.id).get();
        LocalDateTime fechaAplicacion = plazoFijo.fecha_accion;

        Objeto datos = new Objeto();

        try{
            if (estadoBandeja.id == EnumEstadoBandejaOB.PARCIALMENTE_FIRMADA.getCodigo()) {

                AccionesOB accionFirmar = servicioAcciones.find(EnumAccionesOB.FIRMAR.getCodigo()).get();
                assert estadoBandeja != null;
                datos.set("estadoBandeja", estadoBandeja.descripcion);
                LogOB.evento(contexto, "firmarPF", "ID: " + bandeja.id);
                LogOB.evento(contexto, "firmarPF", "ESTADO EN BANDEJA FIN: " + estadoBandeja.descripcion);
                servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionFirmar, bandeja.estadoBandeja, estadoBandeja).get();

                if (plazoFijo != null) {
                    //PlazoFijo nuevoPlazoFijo = ApiPlazosFijos.nuevoPlazoFijo(contexto, plazoFijo).get();

                    plazoFijo.estadoBandeja = estadoBandeja;
                    plazoFijo.estado_firma = "Firmado Parcial";
                    plazoFijo.fechaUltActulizacion = plazoFijo.fecha_accion.now();
                    plazoFijo.nroPlazoFijo = plazoFijo.nroPlazoFijo;
                    servicioPlazoFijoOB.update(plazoFijo);

                    datos.set("idPlazoFijoBandeja", bandeja.id);
                    datos.set("nroPlazoFijo", plazoFijo.nroPlazoFijo);
                    datos.set("estadoBandeja", estadoBandeja.descripcion);

                }
            }


            if (estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {

                AccionesOB accionFirmar = servicioAcciones.find(EnumAccionesOB.FIRMAR.getCodigo()).get();
                assert estadoBandeja != null;

                servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionFirmar, bandeja.estadoBandeja, estadoBandeja).get();

                if (plazoFijo != null) {
                    PlazoFijo nuevoPlazoFijo = ApiPlazosFijos.nuevoPlazoFijo(contexto, plazoFijo).get();

                    plazoFijo.estadoBandeja = estadoBandeja;
                    plazoFijo.estado_firma = "Firmado";
                    plazoFijo.fechaUltActulizacion = plazoFijo.fecha_accion.now();
                    plazoFijo.nroPlazoFijo = nuevoPlazoFijo.nroPlazoFijo;
                    servicioPlazoFijoOB.update(plazoFijo);

                    datos.set("idPlazoFijoBandeja", bandeja.id);
                    datos.set("nroPlazoFijo", nuevoPlazoFijo.nroPlazoFijo);
                    datos.set("estadoBandeja", estadoBandeja.descripcion);


                }
            }
            datos.set("accion", accionFirmar.descripcion);
            datos.set("estado", estadoBandeja.descripcion);

        }catch(ApiException e){
            datos.set("estado", "ERROR");
        }

        return datos;
    }

    private static Object firmarOrdenPagoComex(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        LogOB.evento(contexto, "firmarOrdenPagoComex", "INICIO");
        SesionOB sesion = contexto.sesion();
        Objeto datos = new Objeto();

        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioOPComexOB servicioOrdenPago = new ServicioOPComexOB(contexto);
        ServicioEstadoComexOB servicioEstadoOP = new ServicioEstadoComexOB(contexto);
        ServicioHistorialOPComex servicioHistorialOPComex = new ServicioHistorialOPComex(contexto);

        OrdenPagoComexOB ordenPago = servicioOrdenPago.find(bandeja.id).get();
        String funcionalidadOB = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.COMERCIO_EXTERIOR.getCodigo()).get().codProdFirma.toString();

        String firmasRegistradas = firmas(contexto, bandeja);
        String firmante = sesion.usuarioOB.cuil.toString();

        EstadoBandejaOB estadoBandeja = OBFirmas.validarFirmantesBandejaComex(contexto, bandeja.cuentaOrigen, ordenPago.monto, firmante, firmasRegistradas, bandeja.moneda.id.toString(), funcionalidadOB);
        EstadoOPComexOB estadoInicialOPComex = ordenPago.estado;
        EstadoOPComexOB estadoOPComex;
        String connectionString = contexto.config.string("ob_azure_blob_st_url");
        String containerName = contexto.config.string("ob_azure_blob_comex_st_container");
        assert estadoBandeja != null;
        if (estadoBandeja.id == (EnumEstadoBandejaOB.FIRMADO_COMPLETO).getCodigo()) {
            try {

                estadoOPComex = servicioEstadoOP.find(EnumEstadoOrdenPagoComexOB.EXITO.getCodigo()).get();
                EstadoBandejaOB estadoFirmadoCompleto = servicioEstadoBandeja.find(EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()).get();
                bandeja.estadoBandeja = estadoFirmadoCompleto;
                servicioBandeja.update(bandeja);

                List<ArchivosComexOB> archivos = OBComex.obtenerArchivosBD(ordenPago.id);
                for (ArchivosComexOB archivo : archivos) {
                    AzureBlobStorageManager.moveBlobBetweenContainers(contexto, contexto.config.string("cx_ruta_en_bandeja") + archivo.url, contexto.config.string("ob_azure_blob_st_container"), containerName);
                }

                //enviar notificación
            } catch (Exception e) {
                LogOB.evento(contexto, "firmarOrdenPagoComex", "PAGO ERROR:" + e.getMessage());
                estadoOPComex = servicioEstadoOP.find(EnumEstadoOrdenPagoComexOB.RECHAZADO.getCodigo()).get();
                datos.set("estado", "ERROR");
            }
        } else {
            estadoOPComex = servicioEstadoOP.find(EnumEstadoOrdenPagoComexOB.EN_BANDEJA.getCodigo()).get();
        }

        AccionesOB accionFirmar = servicioAcciones.find(EnumAccionesOB.FIRMAR.getCodigo()).get();
        HistorialOPComexOB historial = servicioHistorialOPComex.cambiaEstado(ordenPago, accionFirmar, empresaUsuario, estadoInicialOPComex, estadoOPComex, null).get();
        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionFirmar, bandeja.estadoBandeja, estadoBandeja).get();
        if (estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {
        try {
            System.out.println("INGRESA CARGARPDFDETALLE");
            LogOB.evento(contexto, "firmarOrdenPagoComex", "INGRESA CARGARPDFDETALLE");
            OBComex.cargarPdfDetalle(contexto, ordenPago.id, connectionString, containerName, ordenPago.url);

        } catch (Exception e) {
            LogOB.evento(contexto, "firmarOrdenPagoComex", "PAGO ERROR:" + e.getMessage());
            estadoOPComex = servicioEstadoOP.find(EnumEstadoOrdenPagoComexOB.RECHAZADO.getCodigo()).get();
            datos.set("estado", "ERROR");
        }
        }

        if (estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {
            try {
                String html = ConstantesOB.generarHtmlComex(ordenPago.empresa.cuit.toString(), ordenPago.numeroTRR, ordenPago.montoMonedaExt.toString(), ordenPago.simboloMonedaExt.simbolo.toString(), ordenPago.concepto.descripcion.toString(), bandeja.fechaUltActulizacion.toString());
                ApiNotificaciones.envioAvisoSinPlantilla(contexto, contexto.esProduccion() ? ConstantesOB.COMEX_MAIL_PARA : "lemolina@hipotecario.com.ar", contexto.esProduccion() ? ConstantesOB.COMEX_MAIL_CC : "lemolina@hipotecario.com.ar", ConstantesOB.COMEX_MAIL_NOMBRE_PARA, ConstantesOB.COMEX_MAIL_ASUNTO, ConstantesOB.COMEX_MAIL_MENSAJE, html);
            } catch (Exception e) {
                LogOB.evento(contexto, "envioMailComex", "ERROR_ENVIO");
            }

        }
        ordenPago.estado = estadoOPComex;
        ordenPago.estadoBandeja = estadoBandeja;
        servicioOrdenPago.update(ordenPago);

        datos.set("idOperacion", bandeja.id);
        datos.set("estadoBandeja", estadoBandeja.descripcion);
        datos.set("accion", historial.accion.descripcion);

        LogOB.evento(contexto, "firmarOrdenPagoComex", "FIN");
        return datos;
    }

    private static Object firmarPagoTarjeta(ContextoOB contexto, PagoTarjetaOB pagoTarjetaOB, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        SesionOB sesion = contexto.sesion();
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        ServicioPagoTarjetaOB servicioPagoTarjetaOB = new ServicioPagoTarjetaOB(contexto);
        ServicioEstadoPagoTarjetaOB servicioEstadoPagoTarjetaOB = new ServicioEstadoPagoTarjetaOB(contexto);
        PagoTarjeta pagoRealizado = new PagoTarjeta();

        PagoTarjetaOB pago = servicioPagoTarjetaOB.find(bandeja.id).get();
        String funcionalidadOB = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.PAGO_SERVICIOS.getCodigo()).get().codProdFirma.toString();

        String firmasRegistradas = firmas(contexto, bandeja);
        String firmante = sesion.usuarioOB.cuil.toString();

        EstadoBandejaOB estadoBandeja = OBFirmas.validarFirmantesBandeja(contexto, bandeja.cuentaOrigen, bandeja.monto, firmante, firmasRegistradas, funcionalidadOB);
        EstadoPagoTarjetaOB estadoPagoTarjeta;
        Objeto datos = new Objeto();
        String detalle = null;
        if (estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {
            estadoPagoTarjeta = servicioEstadoPagoTarjetaOB.find(EnumEstadoEcheqChequeraOB.PENDIENTE.getCodigo()).get();


            try {

                contexto.parametros.set("cuenta", bandeja.cuentaOrigen);
                contexto.parametros.set("moneda", pago.moneda.id.toString());
                contexto.parametros.set("monto", bandeja.monto);
                contexto.parametros.set("cuentaTarjeta", pagoTarjetaOB.cuentaTarjeta);
                contexto.parametros.set("tipoTarjeta", pagoTarjetaOB.tipoTarjeta);
                contexto.parametros.set("tipoCuenta", pagoTarjetaOB.tipoCuenta);
                contexto.parametros.set("idOperacion", bandeja.id);

                pagoRealizado = OBTarjetaEmpresa.pagarTarjeta(contexto);
                if (pagoRealizado.error.descripcion == "ERROR") {
                    estadoPagoTarjeta = servicioEstadoPagoTarjetaOB.find(EnumEstadoPagoTarjetaOB.PAGO_RECHAZADO.getCodigo()).get();
                } else {
                    detalle = pagoRealizado.nroTicket;
                }

            } catch (ApiException e) {
                detalle = e.response.get("mensajeAlUsuario").toString();
                estadoPagoTarjeta = servicioEstadoPagoTarjetaOB.find(EnumEstadoPagoTarjetaOB.RECHAZADO_DE_FIRMA.getCodigo()).get();
                datos.set("estado", "error");
            } catch (Exception e) {
                estadoPagoTarjeta = servicioEstadoPagoTarjetaOB.find(EnumEstadoPagoTarjetaOB.RECHAZADO_DE_FIRMA.getCodigo()).get();
                datos.set("estado", "error");
            }
        } else {
            estadoPagoTarjeta = servicioEstadoPagoTarjetaOB.find(EnumEstadoPagoTarjetaOB.EN_BANDEJA.getCodigo()).get();
        }

        AccionesOB accionFirmar = servicioAcciones.find(EnumAccionesOB.FIRMAR.getCodigo()).get();
        assert estadoBandeja != null;
        datos.set("estadoBandeja", estadoBandeja.descripcion);
        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionFirmar, bandeja.estadoBandeja, estadoBandeja).get();
        String estado = "0";
        if (estadoPagoTarjeta.descripcion.equals("PAGO RECHAZADO") || estadoPagoTarjeta.descripcion.equals("RECHAZADO DE FIRMA")) {
            estado = "ERROR";
        }

        FirmarRechazarDTO respuesta = new FirmarRechazarDTO(estado, accionFirmar.descripcion, bandeja.id, estadoBandeja.descripcion.toString(), pago.moneda.id.toString(), bandeja.cuentaOrigen, bandeja.monto, null, null, detalle);

        datos.set("cuenta", bandeja.cuentaOrigen);
        datos.set("accion", "FIRMAR");
        datos.set("idOperacion", bandeja.id);
        datos.set("nroTicket", pagoRealizado.nroTicket != null ? pagoRealizado.nroTicket : "");
        pago.estado = estadoPagoTarjeta;
        pago.estadoBandeja = estadoBandeja;
        servicioPagoTarjetaOB.update(pago);


        return respuesta.armarRespuesta();
    }

  /**
     * Realiza la firma de una operación de Stop Debit.
     *
     * @param contexto ContextoOB - Contexto de la operación actual.
     * @param stopDebitBandeja StopDebitOB - Objeto que contiene los datos del Stop Debit a firmar.
     * @param empresaUsuario EmpresaUsuarioOB - Usuario de empresa que realiza la firma.
     * @return Object - Respuesta armada con el resultado de la operación.
     *
     * Proceso:
     * 1. Inicializa servicios y variables necesarias.
     * 2. Obtiene la funcionalidad de firma y las firmas registradas.
     * 3. Valida el estado de la bandeja para determinar si puede firmar.
     * 4. Si el estado es FIRMADO_COMPLETO, intenta realizar la operación de Stop Debit.
     * 5. Maneja excepciones y setea el estado de la respuesta en caso de error.
     * 6. Registra la acción de firma en la bandeja de acciones.
     * 7. Retorna un DTO con el resultado de la operación.
     */
    private static Object firmarStopDebit(ContextoOB contexto, BandejaOB stopDebitBandeja, EmpresaUsuarioOB empresaUsuario){
        // 1. Inicializa variables y servicios necesarios para la operación
        SesionOB sesion = contexto.sesion();
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        ServicioPagoTarjetaOB servicioPagoTarjetaOB = new ServicioPagoTarjetaOB(contexto);
        ServicioEstadoPagoTarjetaOB servicioEstadoPagoTarjetaOB = new ServicioEstadoPagoTarjetaOB(contexto);
        StopDebit stopDebit = new StopDebit();

        // 2. Obtiene la funcionalidad de firma para Stop Debit
        String funcionalidadOB = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.PAGO_SERVICIOS.getCodigo()).get().codProdFirma.toString();

        // 3. Obtiene las firmas ya registradas y el firmante actual
        String firmasRegistradas = firmas(contexto, stopDebitBandeja);
        String firmante = sesion.usuarioOB.cuil.toString();

        // 4. Valida el estado de la bandeja para saber si se puede firmar
        EstadoBandejaOB estadoBandeja = OBFirmas.validarFirmantesBandeja(
                        contexto, stopDebitBandeja.cuentaOrigen,
                        stopDebitBandeja.monto, firmante, firmasRegistradas,
                        funcionalidadOB);

        // 5. Inicializa objeto para la respuesta y variable de detalle
        Objeto datos = new Objeto();
        String detalle = null;

        // 6. Si el estado es FIRMADO_COMPLETO, intenta realizar la operación de Stop Debit
        //    Si ocurre un error, setea el estado en "error"
        if (estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {
            try {
                // Setea el número de cuenta en los parámetros del contexto
                contexto.parametros.set("numeroCuenta", stopDebitBandeja.cuentaOrigen);
                // Realiza la operación de Stop Debit
                stopDebit = (StopDebit) OBTarjetaEmpresa.postStopDebit(contexto);

                // Si la operación fue exitosa, guarda el detalle de la respuesta
                if(stopDebit.retcode != "ERROR") {
                    detalle = stopDebit.retdescription;
                    //estadoPagoTarjeta = servicioEstadoPagoTarjetaOB.find(EnumEstadoPagoTarjetaOB.PAGO_RECHAZADO.getCodigo()).get();
                }

            } catch (ApiException e) {
                // En caso de error de API, setea el estado en error
                datos.set("estado", "error");
            } catch (Exception e) {
                // En caso de cualquier otro error, setea el estado en error
                datos.set("estado", "error");
            }
        } else {
            // Si no está FIRMADO_COMPLETO, no realiza la operación y deja el estado en EN_BANDEJA
            //estadoPagoTarjeta = servicioEstadoPagoTarjetaOB.find(EnumEstadoPagoTarjetaOB.EN_BANDEJA.getCodigo()).get();
        }

        // 7. Registra la acción de firma en la bandeja de acciones
        AccionesOB accionFirmar = servicioAcciones.find(EnumAccionesOB.FIRMAR.getCodigo()).get();
        assert estadoBandeja != null;
        datos.set("estadoBandeja", estadoBandeja.descripcion);

        servicioBandejaAcciones.crear(
                stopDebitBandeja,
                empresaUsuario, accionFirmar,
                stopDebitBandeja.estadoBandeja,
                estadoBandeja).get();

        // 8. Arma el DTO de respuesta con los datos de la operación
        String estado = "0";
        /*
        if (estadoPagoTarjeta.descripcion.equals("PAGO RECHAZADO") || estadoPagoTarjeta.descripcion.equals("RECHAZADO DE FIRMA")) {
            estado = "ERROR";
        }
        */

        FirmarRechazarDTO respuesta = new FirmarRechazarDTO(estado,
                accionFirmar.descripcion,
                stopDebitBandeja.id,
                estadoBandeja.descripcion.toString(),
                stopDebitBandeja.moneda.toString(),
                stopDebitBandeja.cuentaOrigen,
                stopDebitBandeja.monto,
                null,
                null,
                detalle);

        // 9. Setea información adicional en el objeto de respuesta
        datos.set("cuenta", stopDebitBandeja.cuentaOrigen);
        datos.set("accion", "FIRMAR");
        datos.set("idOperacion", stopDebitBandeja.id);
        /*
        datos.set("nroTicket", pagoRealizado.nroTicket != null? pagoRealizado.nroTicket: "");
        pago.estado = estadoPagoTarjeta;
        pago.estadoBandeja = estadoBandeja;
        */

        // 10. Retorna la respuesta armada
        return respuesta.armarRespuesta();
    }

    public static Object rechazar(ContextoOB contexto) {
        LogOB.evento(contexto, "rechazar", "INICIO");
        Objeto objetoIds = contexto.parametros.objeto("idsOperaciones");
        List<Object> idsOperaciones = objetoIds.toList();

        List<Object> datosOperaciones = new ArrayList<>();

        SesionOB sesion = contexto.sesion();
        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);

        List<BandejaOB> pendientesDeFirmaBandejaPorCuenta = new ArrayList<>();
        List<BandejaOB> pendientesDeFirmaSinEmpresa = servicioBandeja.buscarPendientesDeFirma(null, null, null, null, null, null, null).get();
        List<BandejaOB> pendientesDeFirma = servicioBandeja.buscarPendientesDeFirma(contexto.sesion().empresaOB, null, null, null, null, null, null).get();

        Objeto cuentas = (Objeto) OBCuentas.cuentas(contexto);
        Objeto listaCuentas = (Objeto) cuentas.get("datos.cuentas");
        List<String> lstCuentas = listaCuentas.objetos().stream().map(c -> c.get("numeroProducto").toString()).toList();
        for (String c : lstCuentas) {
            pendientesDeFirmaBandejaPorCuenta.addAll(pendientesDeFirmaSinEmpresa.stream().filter(p -> p.cuentaOrigen.equals(c)).toList());
        }
        Set<Integer> idsExistentes = pendientesDeFirmaBandejaPorCuenta.stream().map(bandejaOB -> bandejaOB.id).collect(Collectors.toSet());

        pendientesDeFirma.forEach(bandeja -> {
            if (!idsExistentes.contains(bandeja.id)) {
                pendientesDeFirmaBandejaPorCuenta.add(bandeja);
                idsExistentes.add(bandeja.id);
            }
        });

        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);

        for (Object id : idsOperaciones) {
            LogOB.evento(contexto, "rechazar", "rechazar: " + id);
            Optional<BandejaOB> bandejaPendienteDeFirma = pendientesDeFirmaBandejaPorCuenta.stream()
                    .filter(t -> t.id.toString().equals(id.toString())).findFirst();
            if (!bandejaPendienteDeFirma.isPresent()) {
                LogOB.evento(contexto, "rechazar", "DATOS_INVALIDOS");
                return respuesta("DATOS_INVALIDOS");
            }

            BandejaOB bandeja = bandejaPendienteDeFirma.get();

            EnumTipoProductoOB producto = EnumTipoProductoOB.getByCodigo(bandeja.tipoProductoFirma.codProdFirma);
            Object datos = switch (producto) {
                case COMERCIO_EXTERIOR -> rechazarOrdenPagoComex(contexto, bandeja, empresaUsuario);
                case TRANSFERENCIAS -> rechazarTransferencia(contexto, bandeja, empresaUsuario);
                case PERFIL_INVERSOR -> rechazarPerfilInversor(contexto, bandeja, empresaUsuario);
                case FCI -> rechazarFCI(contexto, bandeja, empresaUsuario);
                case PAGO_SERVICIOS -> rechazarPagoDeServicios(contexto, bandeja, empresaUsuario);
                case PLAN_SUELDO, NOMINA -> rechazarHaberes(contexto, bandeja, empresaUsuario);
                case PAGOS_VEP -> rechazarPagoVep(contexto, bandeja, empresaUsuario);
                case PAGO_PROVEEDORES -> rechazarPap(contexto, bandeja, empresaUsuario);
                case DEBITO_DIRECTO -> rechazarDD(contexto, bandeja, empresaUsuario);
                case COBRANZA_INTEGRAL -> rechazarCI(contexto, bandeja, empresaUsuario);
                case CHEQUERA_ELECTRONICA -> rechazarChequera(contexto, bandeja, empresaUsuario);
                case ECHEQ -> rechazarEcheq(contexto, bandeja, empresaUsuario);
                case DEBIN -> rechazarDebin(contexto, bandeja, empresaUsuario);
                case CEDIP -> rechazarCedip(contexto, bandeja, empresaUsuario);
                case PLAZO_FIJO -> rechazarPF(contexto, bandeja, empresaUsuario);
                case DEBIN_LOTE -> rechazarDL(contexto, bandeja, empresaUsuario);
                case ECHEQ_DESCUENTO -> rechazarDescuento(contexto, bandeja, empresaUsuario);
                case PAGO_TARJETA -> rechazarPagoTarjeta(contexto, bandeja, empresaUsuario);
                case DEBIN_PROGRAMADO -> rechazarDebinProgramado(contexto, bandeja, empresaUsuario);
                case STOP_DEBIT -> rechazarStopDebit(contexto, bandeja, empresaUsuario);
                default -> null;
            };

            if (datos == null) {
                datosOperaciones.add(respuesta("Error", "datos", null));
            } else {
                datosOperaciones.add(respuesta("datos", datos));
            }

        }
        sesion.token.fechaValidacion = Fecha.nunca();
        sesion.save();
        return respuesta("datosOperaciones", datosOperaciones);
    }

    private static Object rechazarDebinProgramado(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        ServicioDebinProgramadoOB servicioDebinProgramadoOB = new ServicioDebinProgramadoOB(contexto);
        DebinProgramadoOB debin = servicioDebinProgramadoOB.find(bandeja.id).get();
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();
        EstadoBandejaOB estadoRechazadoEnFirma = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionRechazar, bandeja.estadoBandeja, estadoRechazadoEnFirma).get();
        debin.estadoBandeja = estadoRechazadoEnFirma;
        Objeto datos = new Objeto();
        servicioDebinProgramadoOB.update(debin);

        datos.set("monto", debin.monto);
        datos.set("comprador", debin.vendedorCuit);

        FirmarRechazarDTO respuesta = new FirmarRechazarDTO(
                "0",
                accionRechazar.descripcion,
                bandeja.id,
                estadoRechazadoEnFirma.descripcion,
                debin.moneda.id.toString(),
                bandeja.cuentaOrigen,
                bandeja.monto,
                null,
                null,
                null);

        return respuesta.armarRespuesta();
    }

    private static Object rechazarDescuento(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioEcheqDescuentoOB servicioEcheq = new ServicioEcheqDescuentoOB(contexto);
        ServicioEstadoEcheqOB servicioEstadoEcheq = new ServicioEstadoEcheqOB(contexto);
        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();
        EstadoBandejaOB estadoRechazadoEnFirma = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        EcheqDescuentoOB echeqDescuento = servicioEcheq.find(bandeja.id).get();
        EstadoEcheqOB estadoEnBandejaEcheq = servicioEstadoEcheq.find(EnumEstadosEcheqOB.EN_BANDEJA.getCodigo()).get();
        EstadoEcheqOB estadoRechazadoEcheq = servicioEstadoEcheq.find(EnumEstadosEcheqOB.RECHAZADO.getCodigo()).get();
        Objeto respuesta = new Objeto();
        if (empty(echeqDescuento)) {
            return respuesta("DATOS_INVALIDOS");
        } else {
            try {
                ApiCheques.descontarChequeFactoring(contexto, ConstantesOB.FACTORING_DESCUENTO_ESTADO_RECHAZAR, echeqDescuento.solicitudNumero);
            } catch (ApiException e) {
                LogOB.evento(contexto, "solicitarDescuentoFactoring", e.response.body);
                return respuesta("ERROR");
            }

        }


        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionRechazar, bandeja.estadoBandeja, estadoRechazadoEnFirma).get();

        if (echeqDescuento != null) {
            echeqDescuento.estado = estadoRechazadoEcheq;
            echeqDescuento.estadoCodigo = ConstantesOB.FACTORING_DESCUENTO_ESTADO_RECHAZAR;
            servicioEcheq.update(echeqDescuento);
            respuesta.set("idOperacion", echeqDescuento.id);
        }

        respuesta.set("estadoBandeja", estadoRechazadoEnFirma.descripcion);
        return respuesta;

    }

    private static Object rechazarStopDebit(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();
        EstadoBandejaOB estadoRechazadoEnFirma = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        Objeto respuesta = new Objeto();

        servicioBandejaAcciones.crear(
                bandeja,
                empresaUsuario,
                accionRechazar,
                bandeja.estadoBandeja,
                estadoRechazadoEnFirma).get();

        respuesta.set("estadoBandeja", estadoRechazadoEnFirma.descripcion);
        respuesta.set("idOperacion", bandeja.id);

        return respuesta;
    }

    private static Object rechazarDebin(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        ServicioDebinOB servicioDebinOB = new ServicioDebinOB(contexto);
        DebinOB debin = servicioDebinOB.find(bandeja.id).get();

        Debin debinRequest = new Debin();
        debinRequest.setId(debin.idDebin);
        debinRequest.setImporte(debin.monto);

        Comprador compradorRequest = new Comprador();
        Cliente clienteRequest = new Cliente();
        clienteRequest.setIdTributario(debin.idTributarioComprador);
        clienteRequest.setNombreCompleto(debin.nombreComprador.trim());
        ar.com.hipotecario.backend.servicio.api.debin.Cuenta cuentaRequest = new ar.com.hipotecario.backend.servicio.api.debin.Cuenta();
        cuentaRequest.setCbu(debin.cbuComprador);
        cuentaRequest.setNumero(debin.cuentaComprador);
        cuentaRequest.setTipo(debin.tipoCuentaComprador);
        cuentaRequest.setSucursal(new Sucursal(debin.sucursalIdVendedor, debin.sucursalDescVendedor));
        cuentaRequest.setMoneda(new Moneda(debin.moneda.id.toString(), debin.moneda.descripcion, debin.moneda.simbolo));
        clienteRequest.setCuenta(cuentaRequest);
        compradorRequest.setCliente(clienteRequest);
        debinRequest.comprador = compradorRequest;

        try {
            ApiDebin.postAutorizar(contexto, debinRequest, "RECHAZAR");
        } catch (ApiException e) {
            return new ErrorGenericoOB().setErrores("Error al rechazar el debin.", e.getMessage());
        }

        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioEstadoDebinRecibidasOB servicioEstadoDebinRecibidasOB = new ServicioEstadoDebinRecibidasOB(contexto);
        ServicioEstadoDebinEnviadasOB servicioEstadoDebinEnviadasOB = new ServicioEstadoDebinEnviadasOB(contexto);
        ServicioHistorialDebinOB servicioHistorialDebinOB = new ServicioHistorialDebinOB(contexto);

        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();
        EstadoBandejaOB estadoRechazadoEnFirma = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        EstadoDebinRecibidasOB estadoPendiente = servicioEstadoDebinRecibidasOB.find(EnumEstadoDebinRecibidasOB.EN_BANDEJA.getCodigo()).get();
        EstadoDebinRecibidasOB estadoRechazado = servicioEstadoDebinRecibidasOB.find(EnumEstadoDebinRecibidasOB.RECHAZADO.getCodigo()).get();
        EstadoDebinEnviadasOB estadoPendienteEnv = servicioEstadoDebinEnviadasOB.find(EnumEstadoDebinEnviadasOB.INICIADO.getCodigo()).get();
        EstadoDebinEnviadasOB estadoRechazadoEnv = servicioEstadoDebinEnviadasOB.find(EnumEstadoDebinEnviadasOB.RECHAZADO.getCodigo()).get();

        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionRechazar, bandeja.estadoBandeja, estadoRechazadoEnFirma).get();

        servicioHistorialDebinOB.cambiaEstado(debin, accionRechazar, empresaUsuario, estadoPendienteEnv, estadoRechazadoEnv, estadoPendiente, estadoRechazado).get();

        debin.estadoRecibida = estadoRechazado;
        debin.estadoEnviada = estadoRechazadoEnv;
        debin.estadoBandeja = estadoRechazadoEnFirma;

        Objeto datos = new Objeto();
        servicioDebinOB.update(debin);

        datos.set("monto", debin.monto);
        datos.set("comprador", debin.nombreComprador);

        FirmarRechazarDTO respuesta = new FirmarRechazarDTO(
                "0",
                accionRechazar.descripcion,
                bandeja.id,
                estadoRechazadoEnFirma.descripcion,
                debin.moneda.id.toString(),
                bandeja.cuentaOrigen,
                bandeja.monto,
                null,
                null,
                null);

        return respuesta.armarRespuesta();
    }

    private static Object rechazarEcheq(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioEcheqOB servicioEcheqOB = new ServicioEcheqOB(contexto);
        ServicioEstadoEcheqOB servicioEstadoEcheqOB = new ServicioEstadoEcheqOB(contexto);
        ServicioHistorialEcheqOB servicioHistorialEcheqOB = new ServicioHistorialEcheqOB(contexto);
        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();
        EcheqOB echeq = servicioEcheqOB.find(bandeja.id).get();

        EstadoBandejaOB estadoRechazadoEnFirma = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        EstadoEcheqOB estadoPendiente = servicioEstadoEcheqOB.find(EnumEstadoEcheqChequeraOB.EN_BANDEJA.getCodigo()).get();
        EstadoEcheqOB estadoRechazado = servicioEstadoEcheqOB.find(EnumEstadoEcheqChequeraOB.RECHAZADO.getCodigo()).get();

        EstadoBandejaOB estadoRechazadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionRechazar, bandeja.estadoBandeja, estadoRechazadoBandeja).get();

        servicioHistorialEcheqOB.cambiaEstado(echeq, accionRechazar, empresaUsuario, estadoPendiente, estadoRechazado).get();

        echeq.estado = estadoRechazado;
        echeq.estadoBandeja = estadoRechazadoBandeja;
        servicioEcheqOB.update(echeq);
        FirmarRechazarDTO respuesta = new FirmarRechazarDTO(
                "0",
                accionRechazar.descripcion.toString(),
                bandeja.id,
                estadoRechazadoEnFirma.descripcion,
                echeq.moneda.id.toString(),
                bandeja.cuentaOrigen,
                bandeja.monto,
                null,
                null,
                null);

        return respuesta.armarRespuesta();
    }

    private static Object rechazarChequera(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioChequeraOB servicioChequeraOB = new ServicioChequeraOB(contexto);
        ServicioEstadoChequeraOB servicioEstadoChequeraOB = new ServicioEstadoChequeraOB(contexto);
        ServicioHistorialChequeraOB servicioHistorialChequeraOB = new ServicioHistorialChequeraOB(contexto);
        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();
        ChequeraOB chequera = servicioChequeraOB.find(bandeja.id).get();

        EstadoBandejaOB estadoRechazadoEnFirma = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        EstadoChequeraOB estadoPendiente = servicioEstadoChequeraOB.find(EnumEstadoEcheqChequeraOB.EN_BANDEJA.getCodigo()).get();
        EstadoChequeraOB estadoRechazado = servicioEstadoChequeraOB.find(EnumEstadoEcheqChequeraOB.RECHAZADO.getCodigo()).get();

        EstadoBandejaOB estadoRechazadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionRechazar, bandeja.estadoBandeja, estadoRechazadoBandeja).get();

        servicioHistorialChequeraOB.cambiaEstado(chequera, accionRechazar, empresaUsuario, estadoPendiente, estadoRechazado).get();

        chequera.estado = estadoRechazado;
        chequera.estadoBandeja = estadoRechazadoBandeja;
        servicioChequeraOB.update(chequera);

        FirmarRechazarDTO respuesta = new FirmarRechazarDTO(
                "0",
                accionRechazar.descripcion.toString(),
                bandeja.id,
                estadoRechazadoEnFirma.descripcion,
                chequera.moneda.id.toString(),
                bandeja.cuentaOrigen,
                bandeja.monto,
                null,
                null,
                null);

        return respuesta.armarRespuesta();
    }

    private static Object rechazarCI(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioCobranzaIntegralOB servicioCobranzaIntegral = new ServicioCobranzaIntegralOB(contexto);
        ServicioEstadosCobranzaIntegral servicioEstadosCobranzaIntegral = new ServicioEstadosCobranzaIntegral(contexto);
        ServicioHistorialCobranzaIntegralOB servicioHistorialCobranzaIntegral = new ServicioHistorialCobranzaIntegralOB(contexto);
        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();
        CobranzaIntegralOB cobranza = servicioCobranzaIntegral.find(bandeja.id).get();

        EstadoBandejaOB estadoRechazadoEnFirma = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        EstadosCobranzaIntegralOB estadoPendiente = servicioEstadosCobranzaIntegral.find(EnumEstadosCobranzaIntegralOB.EN_BANDEJA.getCodigo()).get();
        EstadosCobranzaIntegralOB estadoRechazado = servicioEstadosCobranzaIntegral.find(EnumEstadosCobranzaIntegralOB.RECHAZADO.getCodigo()).get();

        EstadoBandejaOB estadoRechazadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionRechazar, bandeja.estadoBandeja, estadoRechazadoBandeja).get();

        servicioHistorialCobranzaIntegral.cambiaEstado(cobranza, accionRechazar, empresaUsuario, estadoPendiente, estadoRechazado).get();


        cobranza.estado = estadoRechazado;
        cobranza.estadoBandeja = estadoRechazadoBandeja;
        servicioCobranzaIntegral.update(cobranza);

        FirmarRechazarDTO respuesta = new FirmarRechazarDTO(
                "0",
                accionRechazar.descripcion,
                bandeja.id,
                estadoRechazadoEnFirma.descripcion,
                cobranza.moneda.id.toString(),
                bandeja.cuentaOrigen,
                bandeja.monto,
                null,
                null,
                null);

        return respuesta.armarRespuesta();
    }

    private static Object rechazarDL(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioDebinLoteOB servicioDebinLoteOB = new ServicioDebinLoteOB(contexto);
        ServicioEstadosDebinPorLote servicioEstadosDebinPorLote = new ServicioEstadosDebinPorLote(contexto);
        ServicioHistorialDebinLoteOB servicioHistorialDebinLoteOB = new ServicioHistorialDebinLoteOB(contexto);
        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();
        DebinLoteOB debinLoteOB = servicioDebinLoteOB.find(bandeja.id).get();

        EstadoBandejaOB estadoRechazadoEnFirma = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        EstadosDebinLoteOB estadoPendiente = servicioEstadosDebinPorLote.find(EnumEstadosCobranzaIntegralOB.EN_BANDEJA.getCodigo()).get();
        EstadosDebinLoteOB estadoRechazado = servicioEstadosDebinPorLote.find(EnumEstadosCobranzaIntegralOB.RECHAZADO.getCodigo()).get();

        EstadoBandejaOB estadoRechazadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionRechazar, bandeja.estadoBandeja, estadoRechazadoBandeja).get();

        servicioHistorialDebinLoteOB.cambiaEstado(debinLoteOB, accionRechazar, empresaUsuario, estadoPendiente, estadoRechazado).get();


        debinLoteOB.estado = estadoRechazado;
        debinLoteOB.estadoBandeja = estadoRechazadoBandeja;
        servicioDebinLoteOB.update(debinLoteOB);

        FirmarRechazarDTO respuesta = new FirmarRechazarDTO(
                "0",
                accionRechazar.descripcion,
                bandeja.id,
                estadoRechazadoEnFirma.descripcion,
                debinLoteOB.moneda.id.toString(),
                bandeja.cuentaOrigen,
                bandeja.monto,
                null,
                null,
                null);

        return respuesta.armarRespuesta();
    }


    private static Object rechazarDD(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioDebitoDirectoOB servicioDebitoDirecto = new ServicioDebitoDirectoOB(contexto);
        ServicioEstadosDebitoDirectoOB servicioEstadoDD = new ServicioEstadosDebitoDirectoOB(contexto);
        ServicioHistorialDebitoDirectoOB servicioHistorialDD = new ServicioHistorialDebitoDirectoOB(contexto);
        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();
        DebitoDirectoOB debitoDirecto = servicioDebitoDirecto.find(bandeja.id).get();

        EstadoBandejaOB estadoRechazadoEnFirma = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        EstadosDebitoDirectoOB estadoPendiente = servicioEstadoDD.find(EnumEstadoDebitoDirectoOB.EN_BANDEJA.getCodigo()).get();
        EstadosDebitoDirectoOB estadoRechazado = servicioEstadoDD.find(EnumEstadoDebitoDirectoOB.RECHAZADO.getCodigo()).get();

        EstadoBandejaOB estadoRechazadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionRechazar, bandeja.estadoBandeja, estadoRechazadoBandeja).get();

        servicioHistorialDD.cambiaEstado(debitoDirecto, accionRechazar, empresaUsuario, estadoPendiente, estadoRechazado).get();

        debitoDirecto.estado = estadoRechazado;
        debitoDirecto.estadoBandeja = estadoRechazadoBandeja;
        servicioDebitoDirecto.update(debitoDirecto);

        FirmarRechazarDTO respuesta = new FirmarRechazarDTO(
                "0",
                accionRechazar.descripcion,
                bandeja.id,
                estadoRechazadoEnFirma.descripcion,
                debitoDirecto.moneda.id.toString(),
                bandeja.cuentaOrigen,
                bandeja.monto,
                null,
                null,
                null);

        return respuesta.armarRespuesta();
    }

    private static Object rechazarPap(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioPagoAProveedoresOB servicioPagoAProveedores = new ServicioPagoAProveedoresOB(contexto);
        ServicioEstadosPagoAProveedoresOB servicioEstadoPap = new ServicioEstadosPagoAProveedoresOB(contexto);
        ServicioHistorialPagoAProveedoresOB servicioHistorialPAP = new ServicioHistorialPagoAProveedoresOB(contexto);
        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();

        PagoAProveedoresOB pago = servicioPagoAProveedores.find(bandeja.id).get();

        EstadoBandejaOB estadoRechazadoEnFirma = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        EstadosPagosAProveedoresOB estadoPendiente = servicioEstadoPap.find(EnumEstadoPagosAProveedoresOB.EN_BANDEJA.getCodigo()).get();
        EstadosPagosAProveedoresOB estadoRechazado = servicioEstadoPap.find(EnumEstadoPagosAProveedoresOB.RECHAZADO.getCodigo()).get();

        EstadoBandejaOB estadoRechazadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionRechazar, bandeja.estadoBandeja, estadoRechazadoBandeja).get();

        HistorialPagoAProveedoresOB historial = servicioHistorialPAP.cambiaEstado(pago, accionRechazar, empresaUsuario, estadoPendiente, estadoRechazado).get();

        pago.estado = estadoRechazado;
        pago.estadoBandeja = estadoRechazadoBandeja;
        servicioPagoAProveedores.update(pago);

        FirmarRechazarDTO respuesta = new FirmarRechazarDTO(
                "0",
                accionRechazar.descripcion,
                bandeja.id,
                estadoRechazadoEnFirma.descripcion,
                pago.moneda.id.toString(),
                bandeja.cuentaOrigen,
                bandeja.monto,
                null,
                null,
                null);

        return respuesta.armarRespuesta();
    }


    private static Object rechazarHaberes(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioPagoHaberesOB servicioPagoHaberes = new ServicioPagoHaberesOB(contexto);
        ServicioEstadoPagosHaberesOB servicioEstadoPagoHaberes = new ServicioEstadoPagosHaberesOB(contexto);
        ServicioHistorialPagoDeHaberesOB servicioHistorialPagoHaberes = new ServicioHistorialPagoDeHaberesOB(contexto);
        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();
        PagoDeHaberesOB pago = servicioPagoHaberes.find(bandeja.id).get();
        Objeto datos = new Objeto();

        EstadoBandejaOB estadoRechazadoEnFirma = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        EstadoPagosHaberesOB estadoPendiente = servicioEstadoPagoHaberes.find(EnumEstadoPagosHaberesOB.EN_BANDEJA.getCodigo()).get();
        EstadoPagosHaberesOB estadoRechazado = servicioEstadoPagoHaberes.find(EnumEstadoPagosHaberesOB.RECHAZADO.getCodigo()).get();

        EstadoBandejaOB estadoRechazadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionRechazar, bandeja.estadoBandeja, estadoRechazadoBandeja).get();

        HistorialPagoDeHaberesOB historial = servicioHistorialPagoHaberes.cambiaEstado(pago, accionRechazar, empresaUsuario, estadoPendiente, estadoRechazado).get();

        pago.estado = estadoRechazado;
        pago.estadoBandeja = estadoRechazadoBandeja;
        servicioPagoHaberes.update(pago);

        datos.set("accion", historial.accion.descripcion);
        datos.set("idSolicitudPago", pago.id);
        datos.set("estadoBandeja", estadoRechazadoEnFirma.descripcion);

        return datos;
    }

    private static Object rechazarPagoVep(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioPagosVepOB servicioPagoVeps = new ServicioPagosVepOB(contexto);
        ServicioEstadoPagoOB servicioEstadoPago = new ServicioEstadoPagoOB(contexto);
        ServicioHistorialPagoVepsOB servicioHistorialPagoVeps = new ServicioHistorialPagoVepsOB(contexto);
        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();
        EstadoBandejaOB estadoRechazadoEnFirma = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        PagosVepOB pago = servicioPagoVeps.find(bandeja.id).get();
        Objeto datos = new Objeto();

        EstadoPagoOB estadoPendiente = servicioEstadoPago.find(EnumEstadoPagosDeServicioYVepsOB.EN_BANDEJA.getCodigo()).get();
        EstadoPagoOB estadoRechazado = servicioEstadoPago.find(EnumEstadoPagosDeServicioYVepsOB.RECHAZADO.getCodigo()).get();

        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionRechazar, bandeja.estadoBandeja, estadoRechazadoEnFirma).get();

        HistorialPagosVepOB historial = servicioHistorialPagoVeps.cambiaEstado(pago, accionRechazar, empresaUsuario, estadoPendiente, estadoRechazado).get();

        pago.estado = estadoRechazado;
        pago.estadoBandeja = estadoRechazadoEnFirma;
        servicioPagoVeps.update(pago);

        datos.set("accion", historial.accion.descripcion);
        datos.set("idSolicitudPago", pago.id);
        datos.set("estadoBandeja", estadoRechazadoEnFirma.descripcion);

        return datos;
    }


    private static Object rechazarPagoDeServicios(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioPagoDeServiciosOB servicioPagoDeServicios = new ServicioPagoDeServiciosOB(contexto);
        ServicioHistorialPagoDeServiciosOB servicioHistorialPagoDeServicios = new ServicioHistorialPagoDeServiciosOB(contexto);
        ServicioEstadoPagoOB servicioEstadoPago = new ServicioEstadoPagoOB(contexto);
        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();
        EstadoBandejaOB estadoRechazadoEnFirma = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        PagoDeServiciosOB pago = servicioPagoDeServicios.find(bandeja.id).get();
        Objeto datos = new Objeto();

        EstadoPagoOB estadoPendiente = servicioEstadoPago.find(EnumEstadoPagosDeServicioYVepsOB.EN_BANDEJA.getCodigo()).get();
        EstadoPagoOB estadoRechazado = servicioEstadoPago.find(EnumEstadoPagosDeServicioYVepsOB.RECHAZADO.getCodigo()).get();

        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionRechazar, bandeja.estadoBandeja, estadoRechazadoEnFirma).get();

        HistorialPagoDeServiciosOB historial = servicioHistorialPagoDeServicios.cambiaEstado(pago, accionRechazar, empresaUsuario, estadoPendiente, estadoRechazado).get();

        pago.estado = estadoRechazado;
        pago.estadoBandeja = estadoRechazadoEnFirma;
        servicioPagoDeServicios.update(pago);

        datos.set("accion", historial.accion.descripcion);
        datos.set("idSolicitudPago", pago.id);
        datos.set("estadoBandeja", estadoRechazadoEnFirma.descripcion);

        return datos;

    }


    private static Object rechazarFCI(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioFCIOB servicioFCIOB = new ServicioFCIOB(contexto);
        ServicioEstadoInversionOB servicioEstadoInversion = new ServicioEstadoInversionOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioHistorialFCI servicioHistorialFCI = new ServicioHistorialFCI(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();
        EstadoBandejaOB estadoRechazadoEnFirma = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        FondosComunesOB fci = servicioFCIOB.find(bandeja.id).get();
        Objeto datos = new Objeto();

        EstadoSolicitudInversionOB estadoPendiente = servicioEstadoInversion.find(EnumEstadoSolicitudFCIOB.PENDIENTE.getCodigo()).get();
        EstadoSolicitudInversionOB estadoRechazada = servicioEstadoInversion.find(EnumEstadoSolicitudFCIOB.RECHAZADA.getCodigo()).get();

        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionRechazar, bandeja.estadoBandeja, estadoRechazadoEnFirma).get();

        HistorialFCIOB historial = servicioHistorialFCI.cambiaEstado(fci, accionRechazar, empresaUsuario, estadoPendiente, estadoRechazada).get();

        fci.estado = estadoRechazada;
        fci.estadoBandeja = estadoRechazadoEnFirma;
        servicioFCIOB.update(fci);

        datos.set("accion", historial.accion.descripcion);
        datos.set("idSolicitudFCI", fci.id);
        datos.set("estadoBandeja", estadoRechazadoEnFirma.descripcion);

        return datos;
    }

    private static Object rechazarPerfilInversor(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioEstadoInversionOB servicioEstadoInversion = new ServicioEstadoInversionOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioPerfilInversorOB servicioPerfilInversor = new ServicioPerfilInversorOB(contexto);
        ServicioHistorialSolicitudPI servicioHistorialSolicitudPi = new ServicioHistorialSolicitudPI(contexto);
        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();
        EstadoBandejaOB estadoRechazadoEnFirma = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        SolicitudPerfilInversorOB perfil = servicioPerfilInversor.find(bandeja.id).get();
        Objeto datos = new Objeto();

        EstadoSolicitudInversionOB estadoPendiente = servicioEstadoInversion.find(EnumEstadoSolicitudFCIOB.PENDIENTE.getCodigo()).get();
        EstadoSolicitudInversionOB estadoRechazada = servicioEstadoInversion.find(EnumEstadoSolicitudFCIOB.RECHAZADA.getCodigo()).get();

        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionRechazar, bandeja.estadoBandeja, estadoRechazadoEnFirma).get();

        HistorialSolicitudPerfilInversorOB historial = servicioHistorialSolicitudPi.cambiaEstado(perfil, accionRechazar, empresaUsuario, estadoPendiente, estadoRechazada).get();

        perfil.estado = estadoRechazada;
        perfil.estadoBandeja = estadoRechazadoEnFirma;
        servicioPerfilInversor.update(perfil);

        datos.set("accion", historial.accion.descripcion);
        datos.set("idOperacion", perfil.id);
        datos.set("estadoBandeja", estadoRechazadoEnFirma.descripcion);

        return datos;
    }

    private static Object rechazarTransferencia(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioTransferenciaOB servicioTransferencia = new ServicioTransferenciaOB(contexto);
        ServicioEstadoTRNOB servicioEstadoTransferencia = new ServicioEstadoTRNOB(contexto);
        ServicioHistorialTrnOB servicioHistorialTransferencia = new ServicioHistorialTrnOB(contexto);
        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();
        EstadoBandejaOB estadoRechazadoEnFirma = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        TransferenciaOB transferencia = servicioTransferencia.find(bandeja.id).get();
        Objeto datos = new Objeto();

        EstadoTRNOB estadoEnBandejaTRN = servicioEstadoTransferencia.find(EnumEstadoTRNOB.EN_BANDEJA.getCodigo()).get();
        EstadoTRNOB estadoRechazadoTRN = servicioEstadoTransferencia.find(EnumEstadoTRNOB.RECHAZADO.getCodigo()).get();

        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionRechazar, bandeja.estadoBandeja, estadoRechazadoEnFirma).get();

        HistorialTrnOB historial = servicioHistorialTransferencia.cambiaEstado(transferencia, accionRechazar, empresaUsuario, estadoEnBandejaTRN, estadoRechazadoTRN).get();
        transferencia.estado = estadoRechazadoTRN;
        transferencia.estadoBandeja = estadoRechazadoEnFirma;
        servicioTransferencia.update(transferencia);

        datos.set("accion", historial.accion.descripcion);
        datos.set("idOperacion", transferencia.id);
        datos.set("estadoBandeja", estadoRechazadoEnFirma.descripcion);

        return datos;
    }

    private static Object rechazarCedip(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioCedipOB servicioCedip = new ServicioCedipOB(contexto);
        ServicioEstadoCedipOB servicioEstadoCedip = new ServicioEstadoCedipOB(contexto);
        //ServicioHistorialTrnOB servicioHistorialTransferencia = new ServicioHistorialTrnOB(contexto);
        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();
        EstadoBandejaOB estadoRechazadoEnFirma = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        CedipOB cedip = servicioCedip.find(bandeja.id).get();
        CedipAccionesOB cedipA = servicioCedip.findAcc(bandeja.id).get();
        Objeto datos = new Objeto();

        EstadoCedipOB estadoEnBandejaCedip = servicioEstadoCedip.find(EnumEstadoTRNOB.EN_BANDEJA.getCodigo()).get();
        EstadoCedipOB estadoRechazadoCedip = servicioEstadoCedip.find(EnumEstadoTRNOB.RECHAZADO.getCodigo()).get();

        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionRechazar, bandeja.estadoBandeja, estadoRechazadoEnFirma).get();

        //HistorialTrnOB historial = servicioHistorialTransferencia.cambiaEstado(cedip, accionRechazar, empresaUsuario, estadoEnBandejaCedip, estadoRechazadoCedip).get();
        if (cedip != null) {
            cedip.estado_cedip = estadoRechazadoCedip;
            cedip.estado_firma = estadoRechazadoCedip.descripcion;
            cedip.estadoBandeja = estadoRechazadoEnFirma;
            servicioCedip.update(cedip);
            datos.set("idOperacion", cedip.id);
        }
        if (cedipA != null) {
            cedipA.estado_firma = estadoRechazadoCedip.descripcion;
            cedipA.estadoBandeja = estadoRechazadoEnFirma;
            servicioCedip.update(cedipA);
            datos.set("idOperacion", cedipA.id);
        }
        //datos.set("accion", historial.accion.descripcion);
        datos.set("estadoBandeja", estadoRechazadoEnFirma.descripcion);

        return datos;

    }

    private static Object rechazarPF(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioPlazoFijoOB servicioPF = new ServicioPlazoFijoOB(contexto);
        ServicioEstadoCedipOB servicioEstadoCedip = new ServicioEstadoCedipOB(contexto);
        //ServicioHistorialTrnOB servicioHistorialTransferencia = new ServicioHistorialTrnOB(contexto);
        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();
        EstadoBandejaOB estadoRechazadoEnFirma = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        PlazoFijoOB plazoFijo = servicioPF.find(bandeja.id).get();

        Objeto datos = new Objeto();

        EstadoCedipOB estadoEnBandejaCedip = servicioEstadoCedip.find(EnumEstadoTRNOB.EN_BANDEJA.getCodigo()).get();
        EstadoCedipOB estadoRechazadoCedip = servicioEstadoCedip.find(EnumEstadoTRNOB.RECHAZADO.getCodigo()).get();

        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionRechazar, bandeja.estadoBandeja, estadoRechazadoEnFirma).get();

        //HistorialTrnOB historial = servicioHistorialTransferencia.cambiaEstado(cedip, accionRechazar, empresaUsuario, estadoEnBandejaCedip, estadoRechazadoCedip).get();
        if (plazoFijo != null) {
            plazoFijo.estado_plazo_fijo = estadoRechazadoCedip;
            plazoFijo.estado_firma = estadoRechazadoCedip.descripcion;
            plazoFijo.estadoBandeja = estadoRechazadoEnFirma;
            servicioPF.update(plazoFijo);
            datos.set("idOperacion", plazoFijo.id);
        }

        //datos.set("accion", historial.accion.descripcion);
        datos.set("estadoBandeja", estadoRechazadoEnFirma.descripcion);

        return datos;

    }

    private static Object rechazarOrdenPagoComex(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioOPComexOB servicioOrdenPago = new ServicioOPComexOB(contexto);
        ServicioEstadoComexOB servicioEstadoOP = new ServicioEstadoComexOB(contexto);
        ServicioHistorialOPComex servicioHistorialOPComex = new ServicioHistorialOPComex(contexto);
        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();
        EstadoBandejaOB estadoRechazadoEnFirma = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        SesionOB sesion = contexto.sesion();
        String connectionString = contexto.config.string("ob_azure_blob_st_url");
        String containerName = contexto.config.string("ob_azure_blob_st_container");
        String pathComexBandeja = "";
        OrdenPagoComexOB ordenPago = servicioOrdenPago.find(bandeja.id).get();
        Objeto datos = new Objeto();

        EstadoOPComexOB estadoEnBandejaTRN = servicioEstadoOP.find(EnumEstadoOrdenPagoComexOB.EN_BANDEJA.getCodigo()).get();
        EstadoOPComexOB estadoRechazadoTRN = servicioEstadoOP.find(EnumEstadoOrdenPagoComexOB.RECHAZADO.getCodigo()).get();

        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionRechazar, bandeja.estadoBandeja, estadoRechazadoEnFirma).get();

        HistorialOPComexOB historial = servicioHistorialOPComex.cambiaEstado(ordenPago, accionRechazar, empresaUsuario, estadoEnBandejaTRN, estadoRechazadoTRN, null).get();
        ordenPago.estado = estadoRechazadoTRN;
        ordenPago.estadoBandeja = estadoRechazadoEnFirma;
        servicioOrdenPago.update(ordenPago);

        datos.set("accion", historial.accion.descripcion);
        datos.set("idOperacion", ordenPago.id);
        datos.set("estadoBandeja", estadoRechazadoEnFirma.descripcion);

        pathComexBandeja = contexto.config.string("cx_ruta_en_bandeja") + ordenPago.url;
        AzureBlobStorageManager az = new AzureBlobStorageManager(contexto, connectionString, containerName);
        az.deleteBlob(contexto, pathComexBandeja);

        return datos;
    }

    private static Object rechazarPagoTarjeta(ContextoOB contexto, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioPagoTarjetaOB servicioPagoTarjeta = new ServicioPagoTarjetaOB(contexto);
        ServicioEstadoPagoTarjetaOB servicioEstadoPagoTarjeta = new ServicioEstadoPagoTarjetaOB(contexto);

        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();
        EstadoBandejaOB estadoRechazadoEnFirma = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
        PagoTarjetaOB pagoTarjeta = servicioPagoTarjeta.find(bandeja.id).get();
        Objeto datos = new Objeto();

        EstadoPagoTarjetaOB estadoRechazadoPagoTarjeta = servicioEstadoPagoTarjeta.find(EnumEstadoPagoTarjetaOB.RECHAZADO_DE_FIRMA.getCodigo()).get();

        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionRechazar, bandeja.estadoBandeja, estadoRechazadoEnFirma).get();

        if (pagoTarjeta != null) {
            pagoTarjeta.estado = estadoRechazadoPagoTarjeta;
            pagoTarjeta.estadoBandeja = estadoRechazadoEnFirma;
            servicioPagoTarjeta.update(pagoTarjeta);
            datos.set("idOperacion", pagoTarjeta.id);
        }
        datos.set("estadoBandeja", estadoRechazadoEnFirma.descripcion);

        return datos;

    }

    public static Object pendientesDeFirma(ContextoOB contexto) {
        Integer idOperacionInicial = contexto.parametros.integer("idOperacionInicial", null);
        Fecha fechaDesde = contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd", null);
        Fecha fechaHasta = contexto.parametros.fecha("fechaHasta", "yyyy-MM-dd", null);
        Integer idMoneda = contexto.parametros.integer("idMoneda", null);
        Integer codProducto = contexto.parametros.integer("codProducto", null);
        Integer limite = contexto.parametros.integer("limite", null);
        String cuenta = contexto.parametros.string("cuenta", null);
        String tipoSolicitud = contexto.parametros.string("tipoSolicitud", null);

        SesionOB sesion = contexto.sesion();
        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
        ServicioFCIOB servicioFCIOB = new ServicioFCIOB(contexto);
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        ServicioPerfilInversorOB servicioPerfilInversor = new ServicioPerfilInversorOB(contexto);
        ServicioTransferenciaOB servicioTransferencia = new ServicioTransferenciaOB(contexto);
        ServicioMonedaOB servicioMoneda = new ServicioMonedaOB(contexto);
        ServicioPagoDeServiciosOB servicioPagoDeServicios = new ServicioPagoDeServiciosOB(contexto);
        ServicioPagosVepOB servicioPagoVeps = new ServicioPagosVepOB(contexto);
        ServicioPagoHaberesOB servicioPagoHaberes = new ServicioPagoHaberesOB(contexto);
        ServicioDebitoDirectoOB servicioDebitoDirecto = new ServicioDebitoDirectoOB(contexto);
        ServicioCobranzaIntegralOB servicioCobranzaIntegral = new ServicioCobranzaIntegralOB(contexto);
        ServicioPagoAProveedoresOB servicioPagoAProveedores = new ServicioPagoAProveedoresOB(contexto);
        ServicioEcheqOB servicioEcheqOB = new ServicioEcheqOB(contexto);
        ServicioEcheqDescuentoOB servicioEcheqDescuentoOB = new ServicioEcheqDescuentoOB(contexto);
        ServicioChequeraOB servicioChequeraOB = new ServicioChequeraOB(contexto);
        ServicioFirmasProductosOB servicioFirmasProductosOB = new ServicioFirmasProductosOB(contexto);
        ServicioDebinOB servicioDebinOB = new ServicioDebinOB(contexto);
        ServicioCedipOB servicioCedipOB = new ServicioCedipOB(contexto);
        ServicioPlazoFijoOB servicioPlazoFijoOB = new ServicioPlazoFijoOB(contexto);
        ServicioOPComexOB servicioOPComexOB = new ServicioOPComexOB(contexto);
        ServicioDebinLoteOB servicioDebinLote = new ServicioDebinLoteOB(contexto);
        ServicioPagoTarjetaOB servicioPagoTarjetaOB = new ServicioPagoTarjetaOB(contexto);

        if (empty(sesion.empresaOB)) {
            return respuesta("EMPRESA_INVALIDA");
        }

        MonedaOB moneda = servicioMoneda.find(idMoneda).tryGet();
        if (!empty(idMoneda) && idMoneda != EnumMonedasOB.PESOS.getMoneda() && idMoneda != EnumMonedasOB.DOLARES.getMoneda() && idMoneda != EnumMonedasOB.EURO.getMoneda()) {
            return respuesta("MONEDA_INVALIDA");
        }

        TipoProductoFirmaOB perfilInversor = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.PERFIL_INVERSOR.getCodigo()).get();
        List<BandejaOB> pendientesDeFirma = servicioBandeja.buscarPendientesDeFirma(sesion.empresaOB, fechaDesde, fechaHasta, moneda, codProducto, cuenta, tipoSolicitud).get();
        Set<Integer> idsExistentes = pendientesDeFirma.stream().map(bandeja -> bandeja.id).collect(Collectors.toSet());

        // AGREGA LOS PENDIENTES DE FIRMA DEBIN AL LISTADO
        if (codProducto == null || codProducto.equals(EnumTipoProductoOB.DEBIN.getCodigo())) {
            List<BandejaOB> pendientesDeFirmaDebin = servicioBandeja.buscarPendientesDeFirma(null, fechaDesde, fechaHasta, moneda, codProducto, cuenta, tipoSolicitud).get();
            OBDebin.vencerDebinesEnBandeja(pendientesDeFirmaDebin); //para no mostrar los debines vencidos en la BDF
            Objeto cuentas = (Objeto) OBCuentas.cuentas(contexto);
            Objeto listaCuentas = (Objeto) cuentas.get("datos.cuentas");
            List<String> lstCuentas = listaCuentas.objetos().stream().map(c -> c.get("numeroProducto").toString()).toList();
            for (String c : lstCuentas) {
                pendientesDeFirma.addAll(pendientesDeFirmaDebin.stream()
                        .filter(p -> p.cuentaOrigen.equals(c) && !idsExistentes.contains(p.id)).toList());
            }
        }

        if (!empty(idOperacionInicial)) {
            pendientesDeFirma = pendientesDeFirma.stream().filter(t -> t.id < idOperacionInicial).collect(Collectors.toList());
        }


        Objeto datos = new Objeto();
        for (BandejaOB bandeja : pendientesDeFirma) {
            boolean puedoFirmar = false;
            boolean sinFirmar = !firmada(contexto, empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB), bandeja);
            if (sinFirmar) {
                EnumTipoProductoOB productos = EnumTipoProductoOB.getByCodigo(bandeja.tipoProductoFirma.codProdFirma);
                if (Boolean.TRUE.equals(bandeja.tipoProductoFirma.multiproducto)) {
                    List<FirmasProductoOB> firmas = servicioFirmasProductosOB.buscarOperacionesSegunMonto(bandeja.id).get();

                    if (!firmas.isEmpty()) {
                        List<Map<String, Object>> permisos = firmas.stream()
                                .map(firma -> {
                                    Map<String, Object> permiso = new LinkedHashMap<>();
                                    permiso.put("funcionalidadOB", firma.codProducto);
                                    permiso.put("monto", firma.monto);
                                    return permiso;
                                })
                                .toList();
                        permisos.forEach(permiso -> contexto.parametros.add("permisos", permiso));
                        puedoFirmar = puedoFirmarMultiple(bandeja.cuentaOrigen, contexto);
                    }
                } else {
                    if (Objects.equals(bandeja.tipoProductoFirma.codProdFirma, perfilInversor.codProdFirma)) {
                        if (!puedeFirmarPerfilInversor(contexto).equals("false")) {
                            puedoFirmar = true;
                        }
                    } else if (bandeja.tipoProductoFirma.codProdFirma.equals(EnumTipoProductoOB.ECHEQ.getCodigo())) {
                        if (!puedeFirmarEcheqSinCuenta(contexto, bandeja).equals("false")) {
                            puedoFirmar = true;
                        }
                    } else if (bandeja.tipoProductoFirma.codProdFirma.equals(EnumTipoProductoOB.COMERCIO_EXTERIOR.getCodigo())) {
                        if (!puedoFirmarComex(contexto, bandeja).equals("false")) {
                            puedoFirmar = true;
                        }
                    } else if (bandeja.tipoProductoFirma.codProdFirma.equals(EnumTipoProductoOB.ECHEQ_DESCUENTO.getCodigo())) {
                        if (!puedeFirmarEcheqSinCuenta(contexto, bandeja).equals("false")) {
                            puedoFirmar = true;
                        }
                    } else if (bandeja.tipoProductoFirma.codProdFirma.equals(EnumTipoProductoOB.PAGO_TARJETA.getCodigo())) {
                        if (!puedeFirmarPagoTarjeta(contexto, bandeja).equals("false")) {
                            puedoFirmar = true;
                        }
                    } else {
                        puedoFirmar = puedoFirmar(contexto, bandeja);
                    }
                }

                if (puedoFirmar) {
                    Objeto operacion = new Objeto();
                    operacion.set("idOperacion", bandeja.id);
                    operacion.set("tipo", bandeja.tipoProductoFirma.descripcion);
                    operacion.set("codigoProducto", bandeja.tipoProductoFirma.codProdFirma);
                    operacion.set("estado", bandeja.estadoBandeja.descripcion);

                    switch (productos) {
                        case TRANSFERENCIAS:
                            TransferenciaOB transferencia = servicioTransferencia.find(bandeja.id).get();
                            if (!transferencia.fechaAplicacion.isBefore(LocalDate.now())) {
                                operacion.set("moneda", transferencia.moneda.simbolo);
                                operacion.set("fechaCreacion", transferencia.fechaCreacion.toString());
                                operacion.set("para", transferencia.credito.titular.trim());
                                operacion.set("monto", bandeja.monto);
                                operacion.set("cuenta", bandeja.cuentaOrigen);
                                operacion.set("descripcion", "Transferencia a " + transferencia.credito.titular.trim());
                                operacion.set("habilitarFirmaMultiple", !(OBBeneficiarios.esUnipersonal(transferencia.credito.cuit) && OBBeneficiarios.esNuevoBeneficiario(contexto, contexto.sesion().empresaOB, transferencia.credito.cbu)));
                            } else {
                                continue;
                            }
                            break;

                        case PERFIL_INVERSOR:
                            SolicitudPerfilInversorOB perfil = servicioPerfilInversor.find(bandeja.id).get();
                            perfil.cuentaOrigen = puedeFirmarPerfilInversor(contexto).toString();
                            operacion.set("perfil", perfil.nombrePerfil);
                            operacion.set("fechaCreacion", perfil.fechaUltActulizacion.toString());
                            operacion.set("creadoPor", perfil.usuario.nombre + " " + perfil.usuario.apellido);
                            operacion.set("descripcion", "Perfil de Inversor");
                            break;
                        case FCI:
                            FondosComunesOB fci = servicioFCIOB.find(bandeja.id).get();
                            ServicioParametriaFciOB servicioParametriaFci = new ServicioParametriaFciOB(contexto);
                            ParametriaFciOB parametros = servicioParametriaFci.buscarPorFondoId(fci.idFondo.idFondo).get();
                            operacion.set("tipoSolicitud", fci.tipoSolicitud);
                            operacion.set("fondo", fci.abreviaturaFondo);
                            operacion.set("moneda", bandeja.moneda.id);
                            operacion.set("monto", bandeja.monto);
                            operacion.set("fechaCreacion", fci.fechaInicio.toString());
                            operacion.set("cuenta", bandeja.cuentaOrigen);
                            operacion.set("cuentaCuotapartista", fci.idCuentaBancaria);
                            operacion.set("creadoPor", fci.usuario.nombre + " " + fci.usuario.apellido);
                            operacion.set("descripcion", fci.tipoSolicitud.equals("Rescate") ? "Rescate " + fci.abreviaturaFondo : "Suscripción " + fci.abreviaturaFondo);
                            operacion.set("horaInicio", parametros.horaInicio);
                            operacion.set("horaFin", parametros.horaFin);
                            break;
                        case PAGO_SERVICIOS:
                            PagoDeServiciosOB pago = servicioPagoDeServicios.find(bandeja.id).get();
                            operacion.set("moneda", bandeja.moneda.simbolo);
                            operacion.set("fechaCreacion", pago.fechaCreacion.toString());
                            operacion.set("monto", bandeja.monto);
                            operacion.set("cuenta", bandeja.cuentaOrigen);
                            operacion.set("creadoPor", pago.usuario.nombre + " " + pago.usuario.apellido);
                            operacion.set("ente", pago.ente);
                            operacion.set("descripcionEnte", pago.descripcionEnte);
                            operacion.set("codigoLink", pago.codigoLink);
                            if (pago.descripcion != null) {
                                operacion.set("descripcion", pago.descripcionEnte + " (" + pago.descripcion + ")");
                            } else {
                                operacion.set("descripcion", pago.descripcionEnte);
                            }
                            break;
                        case PAGOS_VEP:
                            PagosVepOB vep = servicioPagoVeps.find(bandeja.id).get();
                            operacion.set("moneda", bandeja.moneda.simbolo);
                            operacion.set("fechaCreacion", vep.fechaCreacion.toString());
                            operacion.set("monto", bandeja.monto);
                            operacion.set("cuenta", bandeja.cuentaOrigen);
                            operacion.set("creadoPor", vep.usuario.nombre + " " + vep.usuario.apellido);
                            operacion.set("numeroVep", vep.numeroVep);
                            operacion.set("descripcion", vep.descripcion);
                            break;
                        case PLAN_SUELDO:
                            PagoDeHaberesOB acreditacion = servicioPagoHaberes.find(bandeja.id).get();
                            operacion.set("moneda", bandeja.moneda.simbolo);
                            operacion.set("fechaCreacion", acreditacion.fechaCreacion.toString());
                            operacion.set("monto", bandeja.monto);
                            operacion.set("cuenta", bandeja.cuentaOrigen);
                            operacion.set("creadoPor", acreditacion.usuario.nombre + " " + acreditacion.usuario.apellido);
                            operacion.set("descripcion", acreditacion.nombreArchivo);
                            break;
                        case PAGO_PROVEEDORES:
                            PagoAProveedoresOB pap = servicioPagoAProveedores.find(bandeja.id).get();
                            operacion.set("moneda", bandeja.moneda.simbolo);
                            operacion.set("fechaCreacion", pap.fechaCreacion.toString());
                            operacion.set("monto", bandeja.monto);
                            operacion.set("cuenta", bandeja.cuentaOrigen);
                            operacion.set("creadoPor", pap.usuario.nombre + " " + pap.usuario.apellido);
                            operacion.set("descripcion", pap.nombreArchivo);
                            ContextoOB aux = contexto.clonar();
                            List<FirmasProductoOB> firmas = servicioFirmasProductosOB.buscarOperacionesSegunMonto(bandeja.tipoProductoFirma.codProdFirma).get();
                            aux.parametros.add("cuenta", bandeja.cuentaOrigen);
                            List<Map<String, Object>> permisos = firmas.stream()
                                    .map(firma -> {
                                        Map<String, Object> permiso = new LinkedHashMap<>();
                                        permiso.put("funcionalidadOB", firma.codProducto);
                                        permiso.put("monto", bandeja.monto);
                                        return permiso;
                                    })
                                    .toList();
                            permisos.forEach(permiso -> aux.parametros.add("permisos", permiso));
                            break;

                        case DEBITO_DIRECTO:
                            DebitoDirectoOB debito = servicioDebitoDirecto.find(bandeja.id).get();
                            operacion.set("moneda", bandeja.moneda.simbolo);
                            operacion.set("fechaCreacion", debito.fechaCreacion.toString());
                            operacion.set("monto", bandeja.monto);
                            operacion.set("cuenta", bandeja.cuentaOrigen);
                            operacion.set("creadoPor", debito.usuario.nombre + " " + debito.usuario.apellido);
                            operacion.set("descripcion", debito.nombreArchivo);
                            break;

                        case COBRANZA_INTEGRAL:
                            CobranzaIntegralOB cobranza = servicioCobranzaIntegral.find(bandeja.id).get();
                            operacion.set("moneda", bandeja.moneda.simbolo);
                            operacion.set("fechaCreacion", cobranza.fechaCreacion.toLocalDate().toString());
                            operacion.set("monto", bandeja.monto);
                            operacion.set("cuenta", bandeja.cuentaOrigen);
                            operacion.set("creadoPor", cobranza.usuario.nombreCompleto());
                            operacion.set("descripcion", cobranza.nombreArchivo);
                            break;

                        case CEDIP:
                            CedipOB cedip = servicioCedipOB.find(bandeja.id).get();
                            if (cedip != null) {
                                operacion.set("moneda", cedip.moneda.simbolo);
                                operacion.set("fechaCreacion", cedip.fecha_accion.toString());
                                operacion.set("monto", bandeja.monto);
                                operacion.set("cuenta", bandeja.cuentaOrigen);
                                operacion.set("descripcion", "Nuevo Cedip");
                            }
                            ;
                            CedipAccionesOB cedipA = servicioCedipOB.findAcc(bandeja.id).get();
                            if (cedipA != null) {
                                operacion.set("moneda", cedipA.moneda.simbolo);
                                operacion.set("fechaCreacion", cedipA.fecha_accion.toString());
                                operacion.set("cuenta", bandeja.cuentaOrigen);
                                //bandeja.monto = ...
                                operacion.set("monto", bandeja.monto);
                                String accion = cedipA.accion.toUpperCase();
                                operacion.set("descripcion", accion);
                            }
                            break;

                        case ECHEQ:
                            EcheqOB echeq = servicioEcheqOB.findById(bandeja.id).get();
                            operacion.set("moneda", bandeja.moneda.simbolo);
                            operacion.set("fechaCreacion", echeq.fechaPago.toLocalDate().toString());
                            operacion.set("monto", bandeja.monto);
                            operacion.set("cuenta", bandeja.cuentaOrigen);
                            operacion.set("creadoPor", echeq.usuario.nombreCompleto());
                            operacion.set("descripcion", echeq.razonSocialBeneficiario);
                            break;

                        case CHEQUERA_ELECTRONICA:
                            ChequeraOB chequera = servicioChequeraOB.find(bandeja.id).get();
                            operacion.set("moneda", bandeja.moneda.simbolo);
                            operacion.set("fechaCreacion", chequera.fechaCreacion.toString());
                            operacion.set("cuenta", bandeja.cuentaOrigen);
                            operacion.set("creadoPor", chequera.usuario.nombreCompleto());
                            operacion.set("descripcion", "Chequera electronica");
                            break;
                        case DEBIN:
                            DebinOB debin = servicioDebinOB.find(bandeja.id).get();
                            operacion.set("moneda", bandeja.moneda.simbolo);
                            operacion.set("fechaCreacion", debin.fechaCreacion);
                            operacion.set("monto", bandeja.monto);
                            operacion.set("cuenta", bandeja.cuentaOrigen);
                            operacion.set("creadoPor", debin.usuario.nombreCompleto());
                            if (debin != null && debin.tipoCuentaComprador != null && debin.tipoCuentaComprador.equals("PP")) {
                                operacion.set("descripcion", "DEBIN programado a " + debin.cuitVendedor);
                            } else {
                                operacion.set("descripcion", "Pagar a " + debin.cuitVendedor);
                            }
                            break;

                        case DEBIN_PROGRAMADO:
                            DebinProgramadoOB debinProgramadoOB = servicioDebinProgramadoOB.find(bandeja.id).get();
                            operacion.set("moneda", bandeja.moneda.simbolo);
                            operacion.set("fechaCreacion", debinProgramadoOB.fechaCreacion);
                            operacion.set("monto", bandeja.monto);
                            operacion.set("cuenta", debinProgramadoOB.compradorCbu);
                            operacion.set("creadoPor", debinProgramadoOB.usuario.nombreCompleto());
                            operacion.set("descripcion", "DEBIN programado a " + debinProgramadoOB.cuitCreacion);
                            operacion.set("accion", debinProgramadoOB.estado);
                            operacion.set("estado", debinProgramadoOB.estadoBandeja.descripcion);
                            operacion.set("estado", debinProgramadoOB.estadoBandeja.descripcion);
                            operacion.set("firmas", OBFirmas.obtenerDatosFirmantes(contexto, bandeja));
                            operacion.set("concepto", debinProgramadoOB.debinConcepto);

                            break;

                        case PLAZO_FIJO:
                            PlazoFijoOB plazoFijo = servicioPlazoFijoOB.find(bandeja.id).get();
                            operacion.set("moneda", plazoFijo.moneda.simbolo);
                            operacion.set("fechaCreacion", plazoFijo.fecha_accion.toString());
                            operacion.set("monto", bandeja.monto);
                            operacion.set("cuenta", bandeja.cuentaOrigen);
                            operacion.set("descripcion", "Plazo Fijo");
                            break;
                        case COMERCIO_EXTERIOR:
                            OrdenPagoComexOB ordenPagoComexOB = servicioOPComexOB.find(bandeja.id).get();
                            operacion.set("moneda", ordenPagoComexOB.moneda.simbolo);
                            operacion.set("fechaCreacion", ordenPagoComexOB.fechaCreacion.toString());
                            operacion.set("monto", bandeja.monto);
                            operacion.set("cuenta", bandeja.cuentaOrigen);
                            operacion.set("descripcion", "Orden de pago - " + ordenPagoComexOB.razonSocial);
                            break;
                        case ECHEQ_DESCUENTO:
                            EcheqDescuentoOB echeqDescuentoOB = servicioEcheqDescuentoOB.find(bandeja.id).get();
                            operacion.set("moneda", bandeja.moneda.simbolo);
                            operacion.set("fechaCreacion", echeqDescuentoOB.fechaUltActulizacion.toLocalDate().toString());
                            operacion.set("monto", bandeja.monto);
                            operacion.set("cuenta", bandeja.cuentaOrigen);
                            operacion.set("creadoPor", echeqDescuentoOB.usuario.nombreCompleto());
                            operacion.set("descripcion", "Descuento de Cheque");
                            break;

                        case DEBIN_LOTE:
                            DebinLoteOB debinLote = servicioDebinLote.find(bandeja.id).get();
                            operacion.set("moneda", debinLote.moneda.simbolo);
                            operacion.set("fechaCreacion", debinLote.fechaCreacion.toString());
                            operacion.set("monto", debinLote.monto);
                            operacion.set("descripcion", "DEBIN de " + debinLote.usuario.nombreCompleto());
                            break;
                        case PAGO_TARJETA:
                            PagoTarjetaOB pagoTarjetaOB = servicioPagoTarjetaOB.find(bandeja.id).get();
                            operacion.set("moneda", bandeja.moneda.simbolo);
                            operacion.set("fechaCreacion", pagoTarjetaOB.fechaUltActulizacion.toLocalDate().toString());
                            operacion.set("monto", bandeja.monto);
                            operacion.set("cuenta", bandeja.cuentaOrigen);
                            operacion.set("creadoPor", pagoTarjetaOB.usuario.nombreCompleto());
                            operacion.set("descripcion", "Pago de Tarjeta");
                            break;

                    }

                    datos.add(operacion);
                    if (limite != null && datos.toList().size() == limite) {
                        break;
                    }

                }
            }

        }


        return respuesta("datos", datos);
    }

    public static Object listarOperacionesActivas(ContextoOB contexto) {
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        List<TipoProductoFirmaOB> operaciones = servicioTipoProductoFirma.findByActivo().get();
        if (operaciones.isEmpty()) {
            return respuesta("NO_HAY_DATOS");
        }

        Objeto operacionesResponse = new Objeto();
        operaciones.stream().forEach(o -> {
            Objeto op = new Objeto();
            op.set("codigoProducto", o.codProdFirma);
            op.set("nombre", o.descripcion);
            op.set("estado", o.activo);
            operacionesResponse.add(op);
        });

        return respuesta("datos", operacionesResponse);
    }

    public static Object puedeFirmar(ContextoOB contexto) {
        SesionOB sesion = contexto.sesion();
        Objeto datos = new Objeto();
        String cuenta = contexto.parametros.string("cuenta");
        BigDecimal monto = contexto.parametros.bigDecimal("monto");
        String funcionalidadOB = contexto.parametros.string("funcionalidadOB");
        String firmante = sesion.usuarioOB.cuil.toString();

        TipoFirma firma = null;
        try {
            firma = OBFirmas.tipoFirma(contexto, sesion.empresaOB.cuit.toString(), cuenta, String.valueOf(monto), firmante, "", funcionalidadOB);
        } catch (Exception ignored) {

        }

        if ((firma == TipoFirma.FIRMA_INDISTINTA || firma == TipoFirma.FIRMA_CONJUNTA)) {
            datos.set("puedeFirmar", true);
        } else {
            datos.set("puedeFirmar", false);
        }
        return respuesta("datos", datos);
    }

    public static Object puedeFirmarComex(ContextoOB contexto) {
        SesionOB sesion = contexto.sesion();
        Objeto datos = new Objeto();
        String cuenta = contexto.parametros.string("cuenta");
        BigDecimal monto = contexto.parametros.bigDecimal("monto");
        String funcionalidadOB = contexto.parametros.string("funcionalidadOB");
        String moneda = contexto.parametros.string("moneda");
        String monedaNac = "80";
        String firmante = sesion.usuarioOB.cuil.toString();

        TipoFirma firma = null;
        try {
            BigDecimal montoEnPesos = (monto.multiply(CotizacionesDivisas.obtenerCotizacion(contexto, moneda, "venta"))).setScale(2, RoundingMode.HALF_UP);
            firma = OBFirmas.tipoFirma(contexto, sesion.empresaOB.cuit.toString(), cuenta, String.valueOf(montoEnPesos), firmante, "", funcionalidadOB, monedaNac);
        } catch (Exception ignored) {
        }

        if ((firma == TipoFirma.FIRMA_INDISTINTA || firma == TipoFirma.FIRMA_CONJUNTA)) {
            datos.set("puedeFirmar", true);
        } else {
            try {
                firma = OBFirmas.tipoFirma(contexto, sesion.empresaOB.cuit.toString(), cuenta, String.valueOf(monto), firmante, "", funcionalidadOB, moneda);
            } catch (Exception ignored) {
            }
            if ((firma == TipoFirma.FIRMA_INDISTINTA || firma == TipoFirma.FIRMA_CONJUNTA)) {
                datos.set("puedeFirmar", true);
            } else {
                datos.set("puedeFirmar", true);
            }
        }
        return respuesta("datos", datos);
    }

    public static Object puedeFirmarMultiple(ContextoOB contexto) {
        SesionOB sesion = contexto.sesion();
        Objeto datos = new Objeto();
        String cuenta = contexto.parametros.string("cuenta");
        Objeto permisos = contexto.parametros.objeto("permisos");
        List<Object> operaciones = permisos.toList();
        String firmante = sesion.usuarioOB.cuil.toString();
        TipoFirma firma;
        ArrayList<TipoFirma> firmas = new ArrayList<>();
        for (Object operacion : operaciones) {
            Map<String, Object> valores = (Map<String, Object>) operacion;
            firma = OBFirmas.tipoFirma(contexto, sesion.empresaOB.cuit.toString(), cuenta, String.valueOf(valores.get("monto")), firmante, "", valores.get("funcionalidadOB").toString());
            firmas.add(firma);
        }

        boolean puedefirmar = firmas.stream().allMatch(f -> f == TipoFirma.FIRMA_INDISTINTA || f == TipoFirma.FIRMA_CONJUNTA);
        datos.set("puedeFirmar", puedefirmar);
        return respuesta("datos", datos);
    }

    public static Object tiposProductos(ContextoOB contexto) {
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        List<TipoProductoFirmaOB> lstTipoProducto = servicioTipoProductoFirma.findByActivo().tryGet();

        TipoProductoFirmaOB fci = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.FCI.getCodigo()).get();
        Objeto datos = new Objeto();

        for (TipoProductoFirmaOB t : lstTipoProducto) {
            Objeto tipoProducto = new Objeto();
            if (t.codProdFirma.equals(fci.codProdFirma)) {
                Objeto tipoProducto2 = new Objeto();
                tipoProducto.set("descripcion", "Suscripcion");
                tipoProducto2.set("cod_prod_firma", t.codProdFirma);

                datos.add(tipoProducto2);

                tipoProducto2.set("descripcion", "Rescate");
            } else {
                tipoProducto.set("descripcion", t.descripcion);
            }

            tipoProducto.set("cod_prod_firma", t.codProdFirma);
            datos.add(tipoProducto);
        }

        return respuesta("datos", datos);
    }

    private static boolean puedoFirmar(ContextoOB contexto, BandejaOB bandeja) {
        SesionOB sesion = contexto.sesion();
        String firmasRegistradas = String.join(";", firmas(contexto, bandeja));
        String firmante = sesion.usuarioOB.cuil.toString();

        TipoFirma firma = null;

        boolean esReca = esDeReca(bandeja);
        boolean esCedip = esDeCedip(bandeja);
        boolean esPlazoFijo = esDePlazoFijo(bandeja);
        String codProductoFirma = null;

        if (esReca || esCedip || esPlazoFijo) {
            if (esReca) {
                codProductoFirma = String.valueOf(EnumTipoProductoOB.TRANSFERENCIAS.getCodigo());
            }
            if (esCedip || esPlazoFijo) {
                codProductoFirma = String.valueOf(EnumTipoProductoOB.PERFIL_INVERSOR.getCodigo());
            }
        } else {
            if (bandeja.tipoProductoFirma.codProdFirma == EnumTipoProductoOB.DEBIN_PROGRAMADO.getCodigo()) {
                codProductoFirma = String.valueOf(EnumTipoProductoOB.DEBIN.getCodigo());
            } else {
                codProductoFirma = bandeja.tipoProductoFirma.codProdFirma.toString();
            }

        }


        String monto = (esReca || esCedip)
                ? "0.1"
                : bandeja.monto.toString();
        try {
            firma = OBFirmas.tipoFirma(contexto, sesion.empresaOB.cuit.toString(), bandeja.cuentaOrigen, monto, firmante, firmasRegistradas, codProductoFirma); // bandeja.tipoProductoFirma.codProdFirma.toString());
        } catch (Exception ignored) {

        }

        return firma == TipoFirma.FIRMA_INDISTINTA || firma == TipoFirma.FIRMA_CONJUNTA;
    }

    public static Object puedoFirmarComex(ContextoOB contexto, BandejaOB bandeja) {
        SesionOB sesion = contexto.sesion();
        String firmasRegistradas = String.join(";", firmas(contexto, bandeja));
        String firmante = sesion.usuarioOB.cuil.toString();
        String monedaNac = "80";
        TipoFirma firma = null;
        String codProductoFirma = String.valueOf(EnumTipoProductoOB.COMERCIO_EXTERIOR.getCodigo());

        try {
            BigDecimal montoEnPesos = (bandeja.monto.multiply(CotizacionesDivisas.obtenerCotizacion(contexto, bandeja.moneda.id.toString(), "venta"))).setScale(2, RoundingMode.HALF_UP);
            firma = OBFirmas.tipoFirma(contexto, sesion.empresaOB.cuit.toString(), bandeja.cuentaOrigen, String.valueOf(montoEnPesos), firmante, firmasRegistradas, codProductoFirma, monedaNac);
        } catch (Exception ignored) {
        }

        if (!(firma == TipoFirma.FIRMA_INDISTINTA || firma == TipoFirma.FIRMA_CONJUNTA)) {
            try {
                firma = OBFirmas.tipoFirma(contexto, sesion.empresaOB.cuit.toString(), bandeja.cuentaOrigen, String.valueOf(bandeja.monto), firmante, firmasRegistradas, codProductoFirma, bandeja.moneda.id.toString());
            } catch (Exception ignored) {
            }
        }
        return (firma == TipoFirma.FIRMA_INDISTINTA || firma == TipoFirma.FIRMA_CONJUNTA) ? true : "false";
    }

    private static boolean esDeReca(BandejaOB bandeja) {
        return (bandeja.tipoProductoFirma.codProdFirma.toString().equals(String.valueOf(EnumTipoProductoOB.DEBITO_DIRECTO.getCodigo())))
                || (bandeja.tipoProductoFirma.codProdFirma.toString().equals(String.valueOf(EnumTipoProductoOB.COBRANZA_INTEGRAL.getCodigo())))
                || (bandeja.tipoProductoFirma.codProdFirma.toString().equals(String.valueOf(EnumTipoProductoOB.DEBIN_LOTE.getCodigo())));
    }

    private static boolean esDeCedip(BandejaOB bandeja) {
        return (bandeja.tipoProductoFirma.codProdFirma.toString().equals(String.valueOf(EnumTipoProductoOB.CEDIP.getCodigo())));
    }

    private static boolean esDePlazoFijo(BandejaOB bandeja) {
        return (bandeja.tipoProductoFirma.codProdFirma.toString().equals(String.valueOf(EnumTipoProductoOB.PLAZO_FIJO.getCodigo())));
    }


    private static boolean puedoFirmarMultiple(String cuenta, ContextoOB contexto) {
        SesionOB sesion = contexto.sesion();
        Objeto permisos = contexto.parametros.objeto("permisos");
        List<Object> operaciones = permisos.toList();
        String firmante = sesion.usuarioOB.cuil.toString();
        TipoFirma firma;
        ArrayList<TipoFirma> firmas = new ArrayList<>();
        for (Object operacion : operaciones) {
            Map<String, Object> valores = (Map<String, Object>) operacion;
            firma = OBFirmas.tipoFirma(contexto, sesion.empresaOB.cuit.toString(), cuenta, String.valueOf(valores.get("monto")), firmante, "", valores.get("funcionalidadOB").toString());
            firmas.add(firma);
        }
        return firmas.stream().allMatch(f -> f == TipoFirma.FIRMA_INDISTINTA || f == TipoFirma.FIRMA_CONJUNTA);
    }

    public static Object puedeFirmarPerfilInversor(ContextoOB contexto) {
        SesionOB sesion = contexto.sesion();
        CuentasOB cuentasOB = ApiCuentas.cuentas(contexto, contexto.sesion().empresaOB.idCobis).get();

        for (CuentasOB.CuentaOB cuenta : cuentasOB) {
            try {
                CompletaFirmaOB grupoOB = ApiEmpresas.completaFirma(contexto, sesion.empresaOB.cuit.toString(), cuenta.numeroProducto, "0,1", sesion.usuarioOB.cuil.toString(), "", String.valueOf(EnumTipoProductoOB.PERFIL_INVERSOR.getCodigo())).get();

                if (grupoOB.codigo.equals("200") || grupoOB.codigo.equals("250")) {
                    return cuenta.numeroProducto;
                }
            } catch (Exception e) {
                return respuesta("No posee cuentas con permisos para firmar perfil inversor", false);
            }
        }

        return "false";
    }

    public static String puedeFirmarEcheqSinCuenta(ContextoOB contexto, BandejaOB bandeja) {
        SesionOB sesion = contexto.sesion();
        CuentasOB cuentasOB = ApiCuentas.cuentas(contexto, contexto.sesion().empresaOB.idCobis).get();
        ServicioEcheqOB servicioEcheqOB = new ServicioEcheqOB(contexto);
        EcheqOB echeq = servicioEcheqOB.findById(bandeja.id).get();
        String cuentaOrigen = null;
        String monto = "0.1";
        if (!Objects.isNull(echeq) && (echeq.accion == EnumAccionesEcheqOB.EMISION || echeq.accion == EnumAccionesEcheqOB.DEPOSITO)) {
            monto = bandeja.monto.toString();
            cuentaOrigen = bandeja.cuentaOrigen;
        }
        if (cuentaOrigen != null) {
            try {
                CompletaFirmaOB grupoOB = ApiEmpresas.completaFirma(contexto, sesion.empresaOB.cuit.toString(), cuentaOrigen, monto, sesion.usuarioOB.cuil.toString(), "", String.valueOf(EnumTipoProductoOB.ECHEQ.getCodigo())).get();

                if (grupoOB.codigo.equals("200") || grupoOB.codigo.equals("250")) {
                    return cuentaOrigen;
                }
            } catch (Exception ignored) {
            }
        } else {
            for (CuentasOB.CuentaOB cuenta : cuentasOB) {
                try {
                    CompletaFirmaOB grupoOB = ApiEmpresas.completaFirma(contexto, sesion.empresaOB.cuit.toString(), cuenta.numeroProducto, monto, sesion.usuarioOB.cuil.toString(), "", String.valueOf(EnumTipoProductoOB.ECHEQ.getCodigo())).get();

                    if (grupoOB.codigo.equals("200") || grupoOB.codigo.equals("250")) {
                        return cuenta.numeroProducto;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return "false";
    }

    public static String puedeFirmarPagoTarjeta(ContextoOB contexto, BandejaOB bandeja) {
        SesionOB sesion = contexto.sesion();
        CuentasOB cuentasOB = ApiCuentas.cuentas(contexto, contexto.sesion().empresaOB.idCobis).get();
        ServicioPagoTarjetaOB servicioPagoTarjetaOB = new ServicioPagoTarjetaOB(contexto);
        PagoTarjetaOB pagoTarjeta = servicioPagoTarjetaOB.find(bandeja.id).get();
        String cuentaOrigen = null;
        String monto = "0.1";
        if (!Objects.isNull(pagoTarjeta)) {
            monto = bandeja.monto.toString();
            cuentaOrigen = bandeja.cuentaOrigen;
        }
        if (cuentaOrigen != null) {
            try {
                CompletaFirmaOB grupoOB = ApiEmpresas.completaFirma(contexto, sesion.empresaOB.cuit.toString(), cuentaOrigen, monto, sesion.usuarioOB.cuil.toString(), "", String.valueOf(EnumTipoProductoOB.PAGO_SERVICIOS.getCodigo())).get();

                if (grupoOB.codigo.equals("200") || grupoOB.codigo.equals("250")) {
                    return cuentaOrigen;
                }
            } catch (Exception ignored) {
            }
        } else {
            for (CuentasOB.CuentaOB cuenta : cuentasOB) {
                try {
                    CompletaFirmaOB grupoOB = ApiEmpresas.completaFirma(contexto, sesion.empresaOB.cuit.toString(), cuenta.numeroProducto, monto, sesion.usuarioOB.cuil.toString(), "", String.valueOf(EnumTipoProductoOB.PAGO_SERVICIOS.getCodigo())).get();

                    if (grupoOB.codigo.equals("200") || grupoOB.codigo.equals("250")) {
                        return cuenta.numeroProducto;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return "false";
    }


    private static boolean firmada(ContextoOB contexto, EmpresaUsuarioOB empresaUsuario, BandejaOB bandeja) {
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        boolean firme = false;
        List<BandejaAccionesOB> bandejaAccion = servicioBandejaAcciones.buscarPorIdEmpresaUsuarioYAccion(bandeja, empresaUsuario, null).get();

        if (!empty(bandejaAccion) && bandejaAccion.size() > 0) {
            Optional<BandejaAccionesOB> trnFirmada = bandejaAccion.stream().filter(h -> h.accion.id.equals(EnumAccionesOB.FIRMAR.getCodigo()) || h.accion.id.equals(EnumAccionesOB.RECHAZAR.getCodigo())).findFirst();
            if (trnFirmada.isPresent()) {
                firme = true;
            }
        }
        return firme;
    }

    protected static String firmas(ContextoOB contexto, BandejaOB bandeja) {
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        Set<Long> firmas = servicioBandejaAcciones.buscarPorBandeja(bandeja).get().stream().filter(h -> h.accion.id.equals(EnumAccionesOB.FIRMAR.getCodigo())).map(c -> c.empresaUsuario.usuario.cuil).collect(Collectors.toSet());

        List<String> lista = new ArrayList<>();
        for (Long i : firmas) {
            if (i != null) {
                lista.add(i.toString());
            }
        }

        return lista.stream().collect(Collectors.joining(";"));
    }

    public static Object tipoFirma(ContextoOB contexto) {
        String empresa = contexto.parametros.string("empresa");
        String cuenta = contexto.parametros.string("cuenta");
        String monto = contexto.parametros.string("monto");
        String firmante = contexto.parametros.string("firmante");
        String firmasRegistradas = "";
        String funcionalidadOB = contexto.parametros.string("funcionalidadOB");
        Objeto datos = new Objeto();

        TipoFirma tipoFirma = null;
        CompletaFirmaOB grupoOB = ApiEmpresas.completaFirma(contexto, empresa, cuenta, monto, firmante, firmasRegistradas, funcionalidadOB).get();

        switch (grupoOB.codigo) {
            case "200":
                tipoFirma = TipoFirma.FIRMA_CONJUNTA;
                break;
            case "250":
                tipoFirma = TipoFirma.FIRMA_INDISTINTA;
                break;
            case "300":
                tipoFirma = TipoFirma.SIN_FIRMA;
                break;
            default:
                break;
        }

        assert tipoFirma != null;
        datos.set("tipoFirma", tipoFirma.toString());
        return respuesta("datos", datos);

    }

    public static Object habilitaBandeja(ContextoOB contexto) {
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        Objeto datos = new Objeto();
        String empresa = contexto.parametros.string("empresa");
        String firmante = contexto.parametros.string("firmante");

        List<TipoProductoFirmaOB> operaciones = servicioTipoProductoFirma.findByActivo().get();
        if (operaciones.isEmpty()) {
            return respuesta("NO_HAY_DATOS");
        }

        CuentasOB cuentasOB = ApiCuentas.cuentas(contexto, contexto.sesion().empresaOB.idCobis).get();
        List<String> monedas = List.of("02", "80", "");
        for (CuentasOB.CuentaOB cuenta : cuentasOB) {
            try {
                for (TipoProductoFirmaOB o : operaciones) {
                    String funcionalidadOB = servicioTipoProductoFirma.findByCodigo(o.codProdFirma).get().codProdFirma.toString();
                    CompletaFirmaOB habilita;
                    if (o.codProdFirma == EnumTipoProductoOB.COMERCIO_EXTERIOR.getCodigo()) {
                        int i = 0;
                        do {
                            String moneda = monedas.get(i);
                            habilita = ApiEmpresas.completaFirma(contexto, empresa, cuenta.numeroProducto, "0,1", firmante, "", funcionalidadOB, moneda).get();
                            i++;
                        } while ((habilita.codigo.equals("200") || habilita.codigo.equals("250")) && i < monedas.size());

                    } else {
                        habilita = ApiEmpresas.completaFirma(contexto, empresa, cuenta.numeroProducto, "0,1", firmante, "", funcionalidadOB).get();
                    }
                    if (habilita.codigo.equals("200") || habilita.codigo.equals("250")) {
                        datos.set("habilitaBandeja", true);
                        datos.set("cuentaHabilitada", cuenta.numeroProducto);
                        return respuesta("datos", datos);
                    }
                }
            } catch (Exception ignored) {

            }
        }
        datos.set("habilitaBandeja", false);
        return respuesta("datos", datos);
    }

    public static Object listarCuentasEnDictamen(ContextoOB contexto) {
        Objeto datos = new Objeto();
        String empresa = contexto.parametros.string("empresa");
        String funcionalidadOB = contexto.parametros.string("funcionalidadOB");
        String firmante = contexto.parametros.string("firmante");

        Objeto tipoCuenta = (Objeto) OBCuentas.validar_CuentaUnipersonal(contexto, contexto.sesion().empresaOB.cuit.toString());

        CuentasOB cuentas_OB = ApiCuentas.cuentas(contexto, contexto.sesion().empresaOB.idCobis).get();
        List<CuentaOB> cuentasOB = cuentas_OB.stream().toList();

        if (tipoCuenta.get("estado").equals(EnumCuentasOB.CUENTA_UNIPERSONAL.getCodigo())) {
            cuentasOB = cuentasOB.stream().filter(c -> c.tipoTitularidad.equals(EnumCuentasOB.TIPO_TITULARIDAD.getCodigo())).toList();
        }

        for (CuentasOB.CuentaOB cuenta : cuentasOB) {
            try {
                CompletaFirmaOB grupoOB = ApiEmpresas.completaFirma(contexto, empresa, cuenta.numeroProducto, "0,1", firmante, "", funcionalidadOB).get();

                if (grupoOB.codigo.equals("200") || grupoOB.codigo.equals("250")) {
                    datos.add(cuenta);
                }
            } catch (Exception ignored) {

            }
        }
        return respuesta("datos", datos);

    }

    public static TipoFirma tipoFirma(Contexto contexto, String cedruc, String cuenta, String monto, String firmante, String firmasRegistradas, String funcionalidadOB) {
        TipoFirma tipoFirma;
        try {
            if (funcionalidadOB.equals("0")) {
                funcionalidadOB = "15";
            }
            CompletaFirmaOB grupoOB = ApiEmpresas.completaFirma(contexto, cedruc, cuenta, monto, firmante, firmasRegistradas, funcionalidadOB).get();
            switch (grupoOB.codigo) {
                case "200":
                    tipoFirma = TipoFirma.FIRMA_CONJUNTA;
                    break;
                case "250":
                    if (firmasRegistradas.isEmpty())
                        tipoFirma = TipoFirma.FIRMA_INDISTINTA;
                    else
                        tipoFirma = TipoFirma.FIRMA_CONJUNTA;
                    break;
                case "300":
                    tipoFirma = TipoFirma.SIN_FIRMA;
                    break;
                default:
                    tipoFirma = TipoFirma.SIN_FIRMA;
                    break;
            }
            return tipoFirma;
        } catch (ApiException apiException) {
            throw new RuntimeException(String.valueOf(apiException.response));
        }
    }

    public static TipoFirma tipoFirma(Contexto contexto, String cedruc, String cuenta, String monto, String firmante, String firmasRegistradas, String funcionalidadOB, String moneda) {
        TipoFirma tipoFirma;
        try {
            CompletaFirmaOB grupoOB = ApiEmpresas.completaFirma(contexto, cedruc, cuenta, monto, firmante, firmasRegistradas, funcionalidadOB, moneda).get();
            switch (grupoOB.codigo) {
                case "200":
                    tipoFirma = TipoFirma.FIRMA_CONJUNTA;
                    break;
                case "250":
                    if (firmasRegistradas.isEmpty())
                        tipoFirma = TipoFirma.FIRMA_INDISTINTA;
                    else
                        tipoFirma = TipoFirma.FIRMA_CONJUNTA;
                    break;
                case "300":
                    tipoFirma = TipoFirma.SIN_FIRMA;
                    break;
                default:
                    tipoFirma = TipoFirma.SIN_FIRMA;
                    break;
            }
            return tipoFirma;
        } catch (ApiException apiException) {
            throw new RuntimeException(String.valueOf(apiException.response));
        }
    }

    public static EstadoBandejaOB validarFirmantesBandeja(ContextoOB contexto, String cuentaDebito, BigDecimal monto, String firmante, String firmasRegistradas, String funcionalidadOB) {
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        try {
            EstadoBandejaOB estadoBandeja = null;
            String cuit = contexto.sesion().empresaOB.cuit.toString();

            TipoFirma tipoFirma = null;
            try {
                tipoFirma = OBFirmas.tipoFirma(contexto, cuit, cuentaDebito, monto.toString(), firmante, firmasRegistradas, funcionalidadOB);
            } catch (Exception ignored) {

            }

            if (tipoFirma == null || tipoFirma == TipoFirma.SIN_FIRMA) {
                throw new RuntimeException("Firmante no habilitado en dictamen");
            } else if (tipoFirma == TipoFirma.FIRMA_INDISTINTA) {
                estadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()).get();
            } else if (tipoFirma == TipoFirma.FIRMA_CONJUNTA) {
                {
                    CompletaFirmaOB grupoOB = ApiEmpresas.completaFirma(contexto, cuit, cuentaDebito, String.valueOf(monto), firmante, firmasRegistradas, funcionalidadOB).get();
                    switch (grupoOB.codigo) {
                        case "200":
                            estadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.PARCIALMENTE_FIRMADA.getCodigo()).get();
                            break;
                        case "250":
                            estadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()).get();
                            break;
                    }
                }
            }
            return estadoBandeja;
        } catch (Exception e) {
            return null;
        }
    }

    public static EstadoBandejaOB validarFirmantesBandejaComex(ContextoOB contexto, String cuentaDebito, BigDecimal monto, String firmante, String firmasRegistradas, String moneda, String funcionalidadOB) {
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        String monedaNac = "80";
        Boolean validoDolares = false;
        try {
            BigDecimal montoEnPesos = (monto.multiply(CotizacionesDivisas.obtenerCotizacion(contexto, moneda, "venta"))).setScale(2, RoundingMode.HALF_UP);
            EstadoBandejaOB estadoBandeja = null;
            String cuit = contexto.sesion().empresaOB.cuit.toString();

            TipoFirma tipoFirma = null;
            try {
                tipoFirma = OBFirmas.tipoFirma(contexto, cuit, cuentaDebito, String.valueOf(montoEnPesos), firmante, firmasRegistradas, funcionalidadOB, monedaNac);
            } catch (Exception ignored) {
            }

            if (!(tipoFirma == TipoFirma.FIRMA_INDISTINTA || tipoFirma == TipoFirma.FIRMA_CONJUNTA)) {
                try {
                    tipoFirma = OBFirmas.tipoFirma(contexto, cuit, cuentaDebito, monto.toString(), firmante, firmasRegistradas, funcionalidadOB, moneda);
                    validoDolares = true;
                } catch (Exception ignored) {
                }
            }

            if (tipoFirma == null || tipoFirma == TipoFirma.SIN_FIRMA) {
                throw new RuntimeException("Firmante no habilitado en dictamen");
            } else if (tipoFirma == TipoFirma.FIRMA_INDISTINTA) {
                estadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()).get();
            } else if (tipoFirma == TipoFirma.FIRMA_CONJUNTA) {
                {
                    CompletaFirmaOB grupoOB = ApiEmpresas.completaFirma(contexto, cuit, cuentaDebito, validoDolares ? String.valueOf(monto) : String.valueOf(montoEnPesos), firmante, firmasRegistradas, funcionalidadOB, validoDolares ? moneda : monedaNac).get();
                    switch (grupoOB.codigo) {
                        case "200":
                            estadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.PARCIALMENTE_FIRMADA.getCodigo()).get();
                            break;
                        case "250":
                            estadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()).get();
                            break;
                    }
                }
            }
            return estadoBandeja;
        } catch (Exception e) {
            return null;
        }
    }

    private static EstadoTRNOB validarFirmantesTrn(ContextoOB contexto, boolean diferida, EstadoBandejaOB estadoBandeja) {
        ServicioEstadoTRNOB servicioEstadoTransferencia = new ServicioEstadoTRNOB(contexto);
        try {
            EstadoTRNOB estado;

            if (estadoBandeja.id == (EnumEstadoBandejaOB.FIRMADO_COMPLETO).getCodigo()) {
                if (diferida) {
                    estado = servicioEstadoTransferencia.find(EnumEstadoTRNOB.PROGRAMADA.getCodigo()).get();
                } else {
                    estado = servicioEstadoTransferencia.find(EnumEstadoTRNOB.EN_PROCESO.getCodigo()).get();
                }
            } else {
                estado = servicioEstadoTransferencia.find(EnumEstadoTRNOB.EN_BANDEJA.getCodigo()).get();
            }
            return estado;
        } catch (Exception e) {
            return null;
        }
    }

    public static Object completaFirma(ContextoOB contexto) {
        String cedruc = contexto.parametros.string("cedruc");
        String cuenta = contexto.parametros.string("cuenta");
        String monto = contexto.parametros.string("monto");
        String firmante = contexto.parametros.string("firmante");
        String firmasRegistradas = contexto.parametros.string("firmasRegistradas", "");
        String funcionalidadOB = contexto.parametros.string("funcionalidadOB");
        Objeto datos = new Objeto();

        CompletaFirmaOB completaFirmaOB = ApiEmpresas.completaFirma(contexto, cedruc, cuenta, monto, firmante, firmasRegistradas, funcionalidadOB).get();

        datos.set("completaFirma", completaFirmaOB);
        return respuesta("datos", datos);
    }

    protected static Object obtenerDatosFirmantes(ContextoOB contexto, BandejaOB bandeja) {
        ServicioUsuarioOB servicioUsuario = new ServicioUsuarioOB(contexto);
        ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);

        Objeto respuesta = new Objeto();
        String firmas = OBFirmas.firmas(contexto, bandeja);
        String[] listaFirmantes = firmas.split(";");

        if (!firmas.equals("")) {
            for (String firmante : listaFirmantes) {
                Objeto datos = new Objeto();
                UsuarioOB usuario = servicioUsuario.findByCuil(Long.valueOf(firmante)).get();
                datos.set("nombreFirmante", usuario.nombre + " " + usuario.apellido);

                EmpresaUsuarioOB empresaUsuario = servicioEmpresaUsuario.findByUsuarioEmpresa(usuario, contexto.sesion().empresaOB).get();
                List<BandejaAccionesOB> bandejaAccionesOB = servicioBandejaAcciones.buscarPorIdEmpresaUsuarioYAccion(bandeja, empresaUsuario, EnumAccionesOB.FIRMAR.getCodigo()).get();
                if (bandejaAccionesOB != null && bandejaAccionesOB.size() > 0) {
                    BandejaAccionesOB firma = bandejaAccionesOB.get(0);
                    datos.set("fechaFirma", firma.fechaCreacion.toLocalDate().toString() + " " + firma.fechaCreacion.toLocalTime().withSecond(0).withNano(0).toString());
                }
                respuesta.add("datos", datos);
            }
        }

        return respuesta;
    }

    public static void rechazarTransferenciasSinFirma(ContextoOB contexto, List<TransferenciaOB> transferenciasARechazar) {
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();
        ServicioHistorialTrnOB servicioHistorialTransferencia = new ServicioHistorialTrnOB(contexto);
        ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
        ServicioEstadoTRNOB servicioEstadoTransferencia = new ServicioEstadoTRNOB(contexto);
        ServicioTransferenciaOB servicioTransferencia = new ServicioTransferenciaOB(contexto);

        for (TransferenciaOB t : transferenciasARechazar) {
            t.estado = servicioEstadoTransferencia.find(EnumEstadoTRNOB.RECHAZADO.getCodigo()).get();
            servicioTransferencia.update(t);

            EmpresaUsuarioOB empresaUsuario = servicioEmpresaUsuario.findByUsuarioEmpresa(t.usuario, t.empresa).get();
            rechazarOperacionEnBandeja(contexto, t.id, empresaUsuario);

            EstadoTRNOB estadoEnBandeja = servicioEstadoTransferencia.find(EnumEstadoTRNOB.EN_BANDEJA.getCodigo()).get();
            EstadoTRNOB estadoRechazadoTRN = servicioEstadoTransferencia.find(EnumEstadoTRNOB.RECHAZADO.getCodigo()).get();

            servicioHistorialTransferencia.cambiaEstado(t, accionRechazar, empresaUsuario, estadoEnBandeja, estadoRechazadoTRN).get();
        }
    }

    public static void rechazarPagoDeServiciosSinFirma(ContextoOB contexto, PagoDeServiciosOB p) {
        ServicioEstadoPagoOB servicioEstadoPago = new ServicioEstadoPagoOB(contexto);
        ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
        ServicioPagoDeServiciosOB servicioPagoDeServicios = new ServicioPagoDeServiciosOB(contexto);
        ServicioHistorialPagoDeServiciosOB servicioHistorialPagoDeServicios = new ServicioHistorialPagoDeServiciosOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();

        p.estado = servicioEstadoPago.find(EnumEstadoPagosDeServicioYVepsOB.RECHAZADO.getCodigo()).get();
        servicioPagoDeServicios.update(p);

        EmpresaUsuarioOB empresaUsuario = servicioEmpresaUsuario.findByUsuarioEmpresa(p.usuario, p.empresa).get();
        rechazarOperacionEnBandeja(contexto, p.id, empresaUsuario);

        EstadoPagoOB estadoEnBandeja = servicioEstadoPago.find(EnumEstadoPagosDeServicioYVepsOB.EN_BANDEJA.getCodigo()).get();
        EstadoPagoOB estadoRechazadoPago = servicioEstadoPago.find(EnumEstadoPagosDeServicioYVepsOB.RECHAZADO.getCodigo()).get();

        servicioHistorialPagoDeServicios.cambiaEstado(p, accionRechazar, empresaUsuario, estadoEnBandeja, estadoRechazadoPago).get();
    }


    public static void rechazarPagoVepSinFirma(ContextoOB contexto, PagosVepOB p) {
        ServicioEstadoPagoOB servicioEstadoPago = new ServicioEstadoPagoOB(contexto);
        ServicioPagosVepOB servicioPagoVeps = new ServicioPagosVepOB(contexto);
        ServicioHistorialPagoVepsOB servicioHistorialPagoVeps = new ServicioHistorialPagoVepsOB(contexto);
        ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();

        p.estado = servicioEstadoPago.find(EnumEstadoPagosDeServicioYVepsOB.RECHAZADO.getCodigo()).get();
        servicioPagoVeps.update(p);

        EmpresaUsuarioOB empresaUsuario = servicioEmpresaUsuario.findByUsuarioEmpresa(p.usuario, p.empresa).get();
        rechazarOperacionEnBandeja(contexto, p.id, empresaUsuario);

        EstadoPagoOB estadoEnBandeja = servicioEstadoPago.find(EnumEstadoPagosDeServicioYVepsOB.EN_BANDEJA.getCodigo()).get();
        EstadoPagoOB estadoRechazadoPago = servicioEstadoPago.find(EnumEstadoPagosDeServicioYVepsOB.RECHAZADO.getCodigo()).get();

        servicioHistorialPagoVeps.cambiaEstado(p, accionRechazar, empresaUsuario, estadoEnBandeja, estadoRechazadoPago).get();
    }


    public static void rechazarAcreditacionesSinFirma(ContextoOB contexto, List<PagoDeHaberesOB> acreditacionesARechazar) {
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();
        ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
        ServicioEstadoPagosHaberesOB servicioEstadoPago = new ServicioEstadoPagosHaberesOB(contexto);
        ServicioPagoHaberesOB servicioPagoHaberes = new ServicioPagoHaberesOB(contexto);
        ServicioHistorialPagoDeHaberesOB servicioHistorialPagoHaberes = new ServicioHistorialPagoDeHaberesOB(contexto);

        for (PagoDeHaberesOB p : acreditacionesARechazar) {
            p.estado = servicioEstadoPago.find(EnumEstadoPagosHaberesOB.RECHAZADO.getCodigo()).get();
            servicioPagoHaberes.update(p);

            EmpresaUsuarioOB empresaUsuario = servicioEmpresaUsuario.findByUsuarioEmpresa(p.usuario, p.empresa).get();
            rechazarOperacionEnBandeja(contexto, p.id, empresaUsuario);

            EstadoPagosHaberesOB estadoEnBandeja = servicioEstadoPago.find(EnumEstadoPagosDeServicioYVepsOB.EN_BANDEJA.getCodigo()).get();
            EstadoPagosHaberesOB estadoRechazadoPago = servicioEstadoPago.find(EnumEstadoPagosDeServicioYVepsOB.RECHAZADO.getCodigo()).get();

            servicioHistorialPagoHaberes.cambiaEstado(p, accionRechazar, empresaUsuario, estadoEnBandeja, estadoRechazadoPago).get();
        }
    }

    public static void rechazarPapSinFirma(ContextoOB contexto, PagoAProveedoresOB p) {
        ServicioEstadosPagoAProveedoresOB servicioEstadoPap = new ServicioEstadosPagoAProveedoresOB(contexto);
        ServicioPagoAProveedoresOB servicioPagoAProveedores = new ServicioPagoAProveedoresOB(contexto);
        ServicioHistorialPagoAProveedoresOB servicioHistorialPAP = new ServicioHistorialPagoAProveedoresOB(contexto);
        ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);

        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();

        p.estado = servicioEstadoPap.find(EnumEstadoPagosAProveedoresOB.RECHAZADO.getCodigo()).get();
        servicioPagoAProveedores.update(p);

        EmpresaUsuarioOB empresaUsuario = servicioEmpresaUsuario.findByUsuarioEmpresa(p.usuario, p.empresa).get();
        rechazarOperacionEnBandeja(contexto, p.id, empresaUsuario);

        EstadosPagosAProveedoresOB estadoEnBandeja = servicioEstadoPap.find(EnumEstadoPagosAProveedoresOB.EN_BANDEJA.getCodigo()).get();
        EstadosPagosAProveedoresOB estadoRechazadoPago = servicioEstadoPap.find(EnumEstadoPagosAProveedoresOB.RECHAZADO.getCodigo()).get();

        servicioHistorialPAP.cambiaEstado(p, accionRechazar, empresaUsuario, estadoEnBandeja, estadoRechazadoPago).get();
    }

    private static void rechazarOperacionEnBandeja(ContextoOB contexto, Integer idBandeja, EmpresaUsuarioOB empresaUsuario) {
        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        EstadoBandejaOB estadoRechazadoEnFirma = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();

        BandejaOB bandeja = servicioBandeja.find(idBandeja).get();

        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionRechazar, bandeja.estadoBandeja, estadoRechazadoEnFirma).get();
        bandeja.estadoBandeja = estadoRechazadoEnFirma;
        servicioBandeja.update(bandeja);
    }

    public static Object pendientePerfilInversor(ContextoOB contexto) {
        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
        SesionOB sesion = contexto.sesion();
        Objeto datos = new Objeto();

        if (empty(sesion.empresaOB)) {
            return respuesta("EMPRESA_INVALIDA");
        }

        List<BandejaOB> pendientesPerfilInversor = servicioBandeja.buscarPendientesDeFirma(sesion.empresaOB, null, null, null, 15, null, null).get();

        datos.set("hayPendientes", !pendientesPerfilInversor.isEmpty());
        return respuesta("datos", datos);

    }

    public static Object productosPendientesDeFirma(ContextoOB contexto) {

        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
        SesionOB sesion = contexto.sesion();
        Objeto datos = new Objeto();

        if (empty(sesion.empresaOB)) {
            return respuesta("EMPRESA_INVALIDA");
        }

        List<String> listaProductos = new ArrayList<>();
        List<BandejaOB> productosPendientesDeFirma = servicioBandeja.buscarPendientesDeFirma(sesion.empresaOB, null, null, null, null, null, null).get();

        TipoProductoFirmaOB perfilInversor = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.PERFIL_INVERSOR.getCodigo()).get();

        for (BandejaOB bandeja : productosPendientesDeFirma) {
            boolean sinFirmar = !firmada(contexto, empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB), bandeja);

            if (sinFirmar) {
                if ((!Objects.equals(bandeja.tipoProductoFirma.codProdFirma, perfilInversor.codProdFirma) && puedoFirmar(contexto, bandeja)) || Objects.equals(bandeja.tipoProductoFirma.codProdFirma, perfilInversor.codProdFirma) && !Objects.equals(puedeFirmarPerfilInversor(contexto), "false")) {
                    if (!listaProductos.contains(bandeja.tipoProductoFirma.descripcion)) {

                        listaProductos.add(bandeja.tipoProductoFirma.descripcion);

                    }
                }
            }
        }
        datos.set("Productos con pendientes de firma", listaProductos);

        return respuesta("datos", datos);
    }

    private static Object validarHorarioFirma(ContextoOB contexto, Integer idFondo, BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario) {
        ServicioParametriaFciOB servicioParametriaFci = new ServicioParametriaFciOB(contexto);
        ParametriaFciOB fondo = servicioParametriaFci.buscarPorFondoId(idFondo).get();

        if ((LocalTime.parse(fondo.horaInicio).isBefore(LocalTime.now())) && (LocalTime.parse(fondo.horaFin).isAfter(LocalTime.now()))) {
            return true;
        } else {
            return rechazarFCI(contexto, bandeja, empresaUsuario);
        }
    }

    public static void rechazarDDSinFirma(ContextoOB contexto, DebitoDirectoOB debitoDirecto) {
        ServicioEstadosDebitoDirectoOB servicioEstadosDebitoDirectoOB = new ServicioEstadosDebitoDirectoOB(contexto);
        ServicioDebitoDirectoOB servicioDebitoDirectoOB = new ServicioDebitoDirectoOB(contexto);
        ServicioHistorialDebitoDirectoOB servicioHistorialDebitoDirectoOB = new ServicioHistorialDebitoDirectoOB(contexto);
        ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);

        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();

        debitoDirecto.estado = servicioEstadosDebitoDirectoOB.find(EnumEstadoDebitoDirectoOB.RECHAZADO.getCodigo()).get();
        servicioDebitoDirectoOB.update(debitoDirecto);

        EmpresaUsuarioOB empresaUsuario = servicioEmpresaUsuario.findByUsuarioEmpresa(debitoDirecto.usuario, debitoDirecto.empresa).get();
        rechazarOperacionEnBandeja(contexto, debitoDirecto.id, empresaUsuario);

        EstadosDebitoDirectoOB estadoEnBandeja = servicioEstadosDebitoDirectoOB.find(EnumEstadoDebitoDirectoOB.EN_BANDEJA.getCodigo()).get();
        EstadosDebitoDirectoOB estadoRechazadoPago = servicioEstadosDebitoDirectoOB.find(EnumEstadoDebitoDirectoOB.RECHAZADO.getCodigo()).get();

        servicioHistorialDebitoDirectoOB.cambiaEstado(debitoDirecto, accionRechazar, empresaUsuario, estadoEnBandeja, estadoRechazadoPago).get();
    }


    public static Object puedeFirmarEcheq(ContextoOB contexto) {
        CuentasOB cuentasOB = ApiCuentas.cuentas(contexto, contexto.sesion().empresaOB.idCobis).get();
        Objeto datos = new Objeto();

        for (CuentasOB.CuentaOB cuenta : cuentasOB) {
            try {
                CompletaFirmaOB grupoOB = ApiEmpresas.completaFirma(contexto, contexto.sesion().empresaOB.cuit.toString(), cuenta.numeroProducto, "0,1", contexto.sesion().usuarioOB.cuil.toString(), "", String.valueOf(EnumTipoProductoOB.ECHEQ.getCodigo())).get();

                if (grupoOB.codigo.equals("200") || grupoOB.codigo.equals("250")) {
                    datos.set("numeroCuenta", cuenta.numeroProducto);
                    datos.set("puedeFirmar", true);
                    return respuesta("datos", datos);
                }
            } catch (Exception e) {
                return new ErrorGenericoOB().setErrores("No puede firmar echeq.", "No posee cuentas con permisos para firmar echeq.");
            }
        }
        datos.set("numeroCuenta", null);
        datos.set("puedeFirmar", true);
        return respuesta("datos", datos);
    }


    public static void rechazarCobranzaIntegralSinFirma(ContextoOB contexto, CobranzaIntegralOB cobranzaIntegral) {
        ServicioEstadosCobranzaIntegral servicioEstadosCobranzaIntegralOB = new ServicioEstadosCobranzaIntegral(contexto);
        ServicioCobranzaIntegralOB servicioCobranzaIntegralOB = new ServicioCobranzaIntegralOB(contexto);
        ServicioHistorialCobranzaIntegralOB servicioHistorialCobranzaIntegralOB = new ServicioHistorialCobranzaIntegralOB(contexto);
        ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);

        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();

        cobranzaIntegral.estado = servicioEstadosCobranzaIntegralOB.find(EnumEstadosCobranzaIntegralOB.RECHAZADO.getCodigo()).get();
        servicioCobranzaIntegralOB.update(cobranzaIntegral);

        EmpresaUsuarioOB empresaUsuario = servicioEmpresaUsuario.findByUsuarioEmpresa(cobranzaIntegral.usuario, cobranzaIntegral.empresa).get();
        rechazarOperacionEnBandeja(contexto, cobranzaIntegral.id, empresaUsuario);

        EstadosCobranzaIntegralOB estadoEnBandeja = servicioEstadosCobranzaIntegralOB.find(EnumEstadosCobranzaIntegralOB.EN_BANDEJA.getCodigo()).get();
        EstadosCobranzaIntegralOB estadoRechazadoPago = servicioEstadosCobranzaIntegralOB.find(EnumEstadosCobranzaIntegralOB.RECHAZADO.getCodigo()).get();

        servicioHistorialCobranzaIntegralOB.cambiaEstado(cobranzaIntegral, accionRechazar, empresaUsuario, estadoEnBandeja, estadoRechazadoPago).get();
    }

    public static void rechazarEcheqEmisionSinFirma(ContextoOB contexto, EcheqOB cheque) {
        ServicioEstadoEcheqOB servicioEstadoEcheqOB = new ServicioEstadoEcheqOB(contexto);
        ServicioEcheqOB servicioEcheqOB = new ServicioEcheqOB(contexto);
        ServicioHistorialEcheqOB servicioHistorialEcheqOB = new ServicioHistorialEcheqOB(contexto);
        ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);

        AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();

        cheque.estado = servicioEstadoEcheqOB.find(4).get();
        servicioEcheqOB.update(cheque);

        EmpresaUsuarioOB empresaUsuario = servicioEmpresaUsuario.findByUsuarioEmpresa(cheque.usuario, cheque.empresa).get();
        rechazarOperacionEnBandeja(contexto, cheque.id, empresaUsuario);

        EstadoEcheqOB estadoEnBandeja = servicioEstadoEcheqOB.find(1).get();
        EstadoEcheqOB estadoRechazadoPago = servicioEstadoEcheqOB.find(4).get();

        servicioHistorialEcheqOB.cambiaEstado(cheque, accionRechazar, empresaUsuario, estadoEnBandeja, estadoRechazadoPago).get();
    }

    public static void rechazarEcheqDescuentoSinFirma(ContextoOB contexto, EcheqDescuentoOB cheque) {
        ServicioEstadoEcheqOB servicioEstadoEcheqOB = new ServicioEstadoEcheqOB(contexto);
        ServicioEcheqDescuentoOB servicioEcheqOB = new ServicioEcheqDescuentoOB(contexto);
        ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);

        EstadoEcheqOB estadoRechazadoPago = servicioEstadoEcheqOB.find(4).get();
        cheque.estado = estadoRechazadoPago;
        servicioEcheqOB.update(cheque).get();

        EmpresaUsuarioOB empresaUsuario = servicioEmpresaUsuario.findByUsuarioEmpresa(cheque.usuario, cheque.empresa).get();
        rechazarOperacionEnBandeja(contexto, cheque.id, empresaUsuario);

        //EcheqDescuentoOB echeqDescuentoOB = new EcheqDescuentoOB();
//        echeqDescuentoOB.estado = estadoRechazadoPago;

        //      servicioEcheqOB.update(echeqDescuentoOB).get();
    }


    private static void moverArchivoFirmaCompleta(EnumTipoProductoOB producto, ContextoOB contexto, String nombreArchivo) {
        String pathBlobAProcesar = null;
        String pathBlobBandeja = null;
        switch (producto) {
            case COBRANZA_INTEGRAL -> {
                pathBlobAProcesar = contexto.config.string("ci_ruta_procesar") + nombreArchivo;
                pathBlobBandeja = contexto.config.string("ci_ruta_en_bandeja") + nombreArchivo;
            }
            case DEBITO_DIRECTO -> {
                pathBlobAProcesar = contexto.config.string("dd_ruta_procesar") + nombreArchivo;
                pathBlobBandeja = contexto.config.string("dd_ruta_en_bandeja") + nombreArchivo;
            }
            case PAGO_PROVEEDORES -> {
                pathBlobAProcesar = contexto.config.string("pap_ruta_procesar") + nombreArchivo;
                pathBlobBandeja = contexto.config.string("pap_ruta_en_bandeja") + nombreArchivo;
            }
            case PLAN_SUELDO -> {
                pathBlobAProcesar = contexto.config.string("ph_ruta_acreditaciones") + nombreArchivo;
                pathBlobBandeja = contexto.config.string("ph_ruta_en_bandeja") + nombreArchivo;
            }
            default -> {
                throw new RuntimeException("Producto incorrecto");
            }
        }
        String connectionString = contexto.config.string("ob_azure_blob_st_url");
        String containerName = contexto.config.string("ob_azure_blob_st_container");
        String containerComex = contexto.config.string("ob_azure_blob_comex_st_container");

        AzureBlobStorageManager az = new AzureBlobStorageManager(contexto, connectionString, containerName);

        az.moveBlob(contexto, pathBlobBandeja, pathBlobAProcesar);
    }

    public static Object tieneFondos(ContextoOB contexto) {
        String numeroCuenta = contexto.parametros.string("numeroCuenta");
        BigDecimal importe = contexto.parametros.bigDecimal("importe");
        int codProdFirma = contexto.parametros.integer("tipoProducto");
        CuentasOB cuentasOB = ApiCuentas.cuentas(contexto, contexto.sesion().empresaOB.idCobis).get();
        Objeto respuesta = new Objeto();
        cuentasOB.stream().filter(cuenta -> cuenta.numeroProducto.equals(numeroCuenta)).forEach(cuenta -> {

            respuesta.set("tieneFondos", validarSaldoYCuenta(contexto, importe, cuenta.numeroProducto));
            CompletaFirmaOB grupoOB = ApiEmpresas.completaFirma(contexto, contexto.sesion().empresaOB.cuit.toString(), cuenta.numeroProducto, importe.toString(), contexto.sesion().usuarioOB.cuil.toString(), "", String.valueOf(codProdFirma)).get();
            respuesta.set("esUltimoFirmante", (grupoOB.codigo.equals("250")));
        });

        return respuesta("0", "DATOS", respuesta);
    }


}
