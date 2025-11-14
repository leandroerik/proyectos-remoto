package ar.com.hipotecario.canal.homebanking.negocio;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.lib.Encriptador;
import org.apache.commons.lang3.StringUtils;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Texto;
import ar.com.hipotecario.canal.homebanking.servicio.TarjetaCreditoService;

public class TarjetaCredito {
    private ContextoHB contexto;
    private Objeto consolidada;

    public TarjetaCredito(ContextoHB contexto, Objeto tarjetaCredito) {
        this.contexto = contexto;
        this.consolidada = tarjetaCredito;
    }

    public String id() {
        return consolidada.string("numero");
    }
    public String idEncriptado() {
        return "true".equals(ConfigHB.string("prendido_encriptacion_tc")) ?
                Encriptador.encriptarPBEBH(consolidada.string("numero"))
                : consolidada.string("numero");
    }

    public String producto() {
        return "Tarjeta de Crédito";
    }

    public String idTipo() {
        String codigoTipo = consolidada.string("tipoTarjeta");
        return codigoTipo != null ? codigoTipo : "";
    }

    public String tipo() {
        String tipo = null;
        tipo = "I".equals(consolidada.string("tipoTarjeta")) ? "Visa Internacional" : tipo;
        tipo = "N".equals(consolidada.string("tipoTarjeta")) ? "Visa Nacional" : tipo;
        tipo = "M".equals(consolidada.string("tipoTarjeta")) ? "Mastercard" : tipo;
        tipo = "B".equals(consolidada.string("tipoTarjeta")) ? "Visa Business" : tipo;
        tipo = "O".equals(consolidada.string("tipoTarjeta")) ? "Visa Corporate" : tipo;
        tipo = "P".equals(consolidada.string("tipoTarjeta")) ? "Visa Gold" : tipo;
        tipo = "R".equals(consolidada.string("tipoTarjeta")) ? "Visa Purchasing" : tipo;
        tipo = "L".equals(consolidada.string("tipoTarjeta")) ? "Platinum" : tipo;
        tipo = "S".equals(consolidada.string("tipoTarjeta")) ? "Signature" : tipo;
        return tipo != null ? tipo : Texto.primeraMayuscula(consolidada.string("descTipoTarjeta"));
    }

    public static Integer peso(String letra) {
        Integer peso = 1;
        if (letra != null) {
            peso = letra.equals("N") ? 2 : peso;
            peso = letra.equals("I") ? 3 : peso;
            peso = letra.equals("P") ? 4 : peso;
            peso = letra.equals("L") ? 5 : peso;
            peso = letra.equals("S") ? 6 : peso;
        }
        return peso;
    }

    public String numero() {
        return consolidada.string("numero");
    }

    public String numeroBin() {
        return StringUtils.isNotBlank(consolidada.string("numero")) ? consolidada.string("numero").substring(0, ConfigHB.integer("cantidad_digitos_bin_tarjeta_credito", 8)) : "";
    }

    public String cuenta() {
        return consolidada.string("cuenta");
    }

    public String ultimos4digitos() {
        return Formateador.ultimos4digitos(numero());
    }

    public String numeroEnmascaradoAsteriscos() {
        String numeroFormateado = "";
        String numero = Formateador.ultimos4digitos(numero());
        if (numero != null && numero.length() == 4) {
            numeroFormateado += "****";
            numeroFormateado += numero;
        }
        return numeroFormateado;
    }

    public String numeroEnmascarado() {
        String numeroFormateado = "";
        String numero = Formateador.ultimos4digitos(numero());
        if (numero != null && numero.length() == 4) {
            numeroFormateado += "XXX-";
            numeroFormateado += numero;
        }
        return numeroFormateado;
    }

    public String numeroCuenta() {
        return consolidada.string("cuenta");
    }

    public String idEstado() {
        return consolidada.string("estado");
    }

    public String estado() {
        String estado = "";
        estado = "20".equals(consolidada.string("estado")) ? "Tarjeta Normal" : estado;
        estado = "22".equals(consolidada.string("estado")) ? "Tarjeta con No Renovar" : estado;
        estado = "23".equals(consolidada.string("estado")) ? "Tarjeta Internacional por Viaje" : estado;
        estado = "24".equals(consolidada.string("estado")) ? "Tarjeta con Orden de Baja" : estado;
        estado = "25".equals(consolidada.string("estado")) ? "Tarjeta con Problemas" : estado;
        estado = "29".equals(consolidada.string("estado")) ? "Tarjeta Dada de Baja" : estado;
        return estado;
    }

    public Boolean esTitular() {
        return "T".equals(consolidada.string("tipoTitularidad")) || "P".equals(consolidada.string("tipoTitularidad"));
    }

    public String titularidad() {
        String titularidad = "";
        titularidad = "P".equals(consolidada.string("tipoTitularidad")) ? "Principal" : titularidad;
        titularidad = "T".equals(consolidada.string("tipoTitularidad")) ? "Titular" : titularidad;
        titularidad = "A".equals(consolidada.string("tipoTitularidad")) ? "Adicional" : titularidad;
        return titularidad;
    }

    public String fechaVencimiento(String formato) {
        return consolidada.date("fechaVencActual", "yyyy-MM-dd", formato);
    }

    public String fechaCierre(String formato) {
        return consolidada.date("cierreActual", "yyyy-MM-dd", formato);
    }

    public String fechaAlta(String formato) {
        return consolidada.date("fechaAlta", "yyyy-MM-dd", formato);
    }

    public Date fechaAltaDate() {
        return consolidada.date("fechaAlta", "yyyy-MM-dd");
    }

    // TODO usar el dato de visa
    public BigDecimal debitosPesos() {
        return consolidada.bigDecimal("debitosEnCursoPesos");
    }

    public String debitosPesosFormateado() {
        return Formateador.importe(debitosPesos());
    }

    // TODO usar el dato de visa
    public BigDecimal debitosDolares() {
        return consolidada.bigDecimal("debitosEnCursoDolares");
    }

    public String debitosDolaresFormateado() {
        return Formateador.importe(debitosDolares());
    }

    public String idFormaPago() {
        return consolidada.string("formaPago");
    }

    public String formaPago() {
        String formaPago = "";
        Map<String, String> mapa = formasPago();
        for (String clave : mapa.keySet()) {
            if (clave.equals(consolidada.string("formaPago"))) {
                formaPago = mapa.get(clave);
            }
        }
        return formaPago;
    }

    public String formaPagoPV(boolean tieneCuenta) {
        String formaPago = "";
        Map<String, String> mapa = formasPagoProximoVencimiento();
        for (String clave : mapa.keySet()) {
            if (clave.equals(consolidada.string("formaPago"))) {
                formaPago = mapa.get(clave);
                if (!tieneCuenta) {
                    formaPago = StringUtils.substringBefore(formaPago, " desde");
                }
            }
        }
        return formaPago;
    }

    public static Map<String, String> formasPago() {
        Map<String, String> mapa = new LinkedHashMap<>();
        mapa.put("01", "Efectivo");
        mapa.put("02", "Débito Automático Pago Mínimo");
        mapa.put("03", "Débito Automático Pago Total");
        mapa.put("04", "Débito Automático Pago Mínimo");
        mapa.put("05", "Débito Automático Pago Total");
        mapa.put("07", "Débito Saldo Pesos en CA");
        mapa.put("15", "Débito Saldo Actual en Cuenta");
        mapa.put("45", "Débito Pago Mínimo en Cuenta");
        mapa.put("46", "Débito Saldo Actual en Cuenta");
        mapa.put("72", "Débito Importe Acordado en CC");
        mapa.put("92", "Débito Importe Acordado en CA");
        return mapa;
    }

    public static Map<String, String> formasPagoProximoVencimiento() {
        Map<String, String> mapa = new LinkedHashMap<>();
        mapa.put("01", "Efectivo");
        mapa.put("02", "Se debitará el pago mínimo desde tu");
        mapa.put("03", "Se debitará el pago total desde tu");
        mapa.put("04", "Se debitará el pago mínimo desde tu");
        mapa.put("05", "Se debitará el pago total desde tu");
        mapa.put("07", "Se debitará el saldo en pesos desde tu");
        mapa.put("15", "Se debitará el saldo actual desde tu");
        mapa.put("45", "Se debitará el pago mínimo desde tu");
        mapa.put("46", "Se debitará el saldo actual desde tu");
        mapa.put("72", "Se debitará el importe acordado desde tu");
        mapa.put("92", "Se debitará el importe acordado desde tu");
        return mapa;
    }

    /* ========== DETALLE ========== */
    public Boolean esPagoMinimo() {
        String[] formasDePago = {"02", "04", "45"};
        return Arrays.asList(formasDePago).contains(consolidada.string("formaPago"));
    }

    public String bancaCuentaNumero() {
        ApiResponse response = TarjetaCreditoService.consultaTarjetaCreditoCache(contexto, numero());
        return response.objetos().get(0).string("bancaCuentaNumero");
    }

    public BigDecimal pagoMinimo() {
        ApiResponse response = TarjetaCreditoService.consultaTarjetaCreditoCache(contexto, numero());
        return response.objetos().get(0).bigDecimal("pagoMinimoActual");
    }

    public String bancaCuentaTipo() {
        ApiResponse response = TarjetaCreditoService.consultaTarjetaCreditoCache(contexto, numero());
        return response.objetos().get(0).string("bancaCuentaTipo");
    }

    public String stopDebit() {
        ApiResponse response = TarjetaCreditoService.consultaTarjetaCreditoCache(contexto, numero());
        return response.objetos().get(0).string("stopDebit");
    }

    public String idPaquete() {
        return consolidada.string("idPaquete");
    }

    public String denominacionTarjeta() {
        return consolidada.string("denominacionTarjeta");
    }

    public String sucursal() {
        return consolidada.string("sucursal");
    }

    public Boolean esHML() {
        String numero = numero();
        return numero.startsWith("400103") || numero.startsWith("400104");
    }

    public String grupoAfinidad() {
        ApiResponse response = TarjetaCreditoService.consultaTarjetaCreditoCache(contexto, numero());
        return response.objetos().get(0).string("grupoAfinidad");
    }

    public BigDecimal limiteCompra() {
        ApiResponse response = TarjetaCreditoService.consultaTarjetaCreditoCache(contexto, numero());
        return response.objetos().get(0).bigDecimal("limiteCompraAcordado");
    }

    public BigDecimal limiteCompraCuotas() {
        ApiResponse response = TarjetaCreditoService.consultaTarjetaCreditoCache(contexto, numero());
        return response.objetos().get(0).bigDecimal("limiteFinanciacionAcordado");
    }

    public boolean esPagableUS() {
        ApiResponse response = TarjetaCreditoService.consultaTarjetaCreditoCache(contexto, numero());
        return response.objetos().get(0).bool("esPagableUS", false);
    }

    public String modelo() {
        return consolidada.string("descModeloLiquidacion");
    }

    public String modeloLiquidacion() {
        return consolidada.string("modeloLiquidacion");
    }

    public String idTitularidad() {
        return consolidada.string("tipoTitularidad");
    }

    public String habilitada() {
        return consolidada.string("tarjetaHabilitada");
    }

    public String esPrefijoVisa() {
        Predicate<String> esTarjetaVisa = (esVisa) -> esVisa.startsWith("Visa");
        return esTarjetaVisa.test(consolidada.string("descTipoTarjeta")) ? "2" : "1";
    }

    public boolean adheridoResumenElectronico() {
        ApiResponse response = TarjetaCreditoService.consultaTarjetaCreditoCache(contexto, numero());
        return response.objetos().get(0).bool("adheridoResumenElectronico");
    }

    public String descTipoTarjeta() {
        return consolidada.string("descTipoTarjeta");
    }

    public String grupoCarteraTc() {
        ApiResponse response = TarjetaCreditoService.consultaTarjetaCreditoCache(contexto, numero());
        return response.objetos().get(0).string("grupo");
    }

    public Boolean firmaContrato() {
        ApiResponse response = TarjetaCreditoService.consultaTarjetaCreditoCache(contexto, numero());
        return response.objetos().get(0).bool("firmaContrato");
    }

    public String fechaProximoCierre(String formato) {
        return consolidada.date("fechaProximoCierre", "yyyy-MM-dd", formato);
    }
    
	public Boolean altaPuntoVenta() {
		return "V".equalsIgnoreCase(consolidada.string("altaPuntoVenta"));
	}

}
