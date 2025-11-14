package ar.com.hipotecario.canal.officebanking;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Parametros;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Pdf;
import ar.com.hipotecario.backend.exception.ParametrosIncorrectosException;
import ar.com.hipotecario.backend.servicio.api.catalogo.ApiCatalogo;
import ar.com.hipotecario.backend.servicio.api.catalogo.DiaBancario;
import ar.com.hipotecario.backend.servicio.api.catalogo.SucursalesOB;
import ar.com.hipotecario.backend.servicio.api.cuentas.ApiCuentas;
import ar.com.hipotecario.backend.servicio.api.cuentas.DebitosCreditosOB;
import ar.com.hipotecario.backend.servicio.api.cuentas.DeudasCuenta;
import ar.com.hipotecario.backend.servicio.api.cuentas.DeudasCuenta.DeudaCuenta;
import ar.com.hipotecario.backend.servicio.api.firmas.TipoFirma;
import ar.com.hipotecario.backend.servicio.api.inversiones.*;
import ar.com.hipotecario.backend.servicio.api.inversiones.CuentaComitenteEspeciesOB.CuentaComitenteEspecie;
import ar.com.hipotecario.backend.servicio.api.inversiones.CuentaComitentesLicitacionesOB.CuentaComitenteLicitacion;
import ar.com.hipotecario.backend.servicio.api.inversiones.CuentasComitentes.CuentaComitente;
import ar.com.hipotecario.backend.servicio.api.inversiones.Cuotapartistas.Cuotapartista;
import ar.com.hipotecario.backend.servicio.api.inversiones.Cuotapartistas.CuotapartistaCuentaBancariaModel;
import ar.com.hipotecario.backend.servicio.api.inversiones.FondosDeInversion.FondoInversion;
import ar.com.hipotecario.backend.servicio.api.inversiones.Liquidaciones.Liquidacion;
import ar.com.hipotecario.backend.servicio.api.inversiones.PosicionesCuotapartista.PosicionCuotapartista;
import ar.com.hipotecario.backend.servicio.api.inversiones.TitulosProducto.ProductosOperablesOrdenados;
import ar.com.hipotecario.backend.servicio.api.personas.ApiPersonas;
import ar.com.hipotecario.backend.servicio.api.personas.Cuils;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.*;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.Cedips.Cedip;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.Cedips.Cedip.CedipNuevo;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.Cedips.Cedip.ResponsePostTransmision;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.Cedips.Cedip.Transmision;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.PlazosFijos.PlazoFijo;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.libreriariesgofraudes.application.dto.RecommendationDTO;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.be.TransactionBEBankProcess;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.generics.TransactionBankProcess;
import ar.com.hipotecario.canal.officebanking.dto.comitente.ReporteHistorialComitenteCSVDTO;
import ar.com.hipotecario.canal.officebanking.enums.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.PermisoOperadorOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.CedipAccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.CedipOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.EstadoCedipOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.EstadoSolicitudInversionOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.FondoAceptadoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.FondosComunesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.ParametriaFciOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.PlazoFijoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.PreguntasPerfilInversorOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.RespuestasPerfilInversorOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.SolicitudPerfilInversorOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;
import ar.com.hipotecario.canal.officebanking.transmit.TransmitOB;
import ar.com.hipotecario.canal.officebanking.util.FormateadorOB;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;

public class OBInversiones extends ModuloOB {

    private static final String TIPO_SUSCRIPCION = "SU";
    private static final String ID_TIPO_DOCUMENTO_CUIT = "59";
    private static final String ID_TIPO_DOCUMENTO_CUIL = "96";
    private static final String ID_TIPO_PERSONA_JURIDICA = "j";
    private static final String ID_TIPO_PERSONA_FISICA = "f";
    private static final String CLASE_FONDO_OB = "Clase B";
    private static final String CLASE_FONDO_OBPF = "Clase A";
    private static ContextoOB contextoOB = new ContextoOB("OB", Config.ambiente(), "1");
    private static final ServicioParametroOB servicioParametro = new ServicioParametroOB(contextoOB);
    private static final ServicioMonedaOB monedaServicio = new ServicioMonedaOB(contextoOB);
    private static final List<String> habilitaBandejaFirmas = servicioParametro.split("transferencia.bandeja.firma").get();

    public static Object cuentasComitentes(ContextoOB contexto) {
        SesionOB sesion = contexto.sesion();
        CuentasComitentes cuentas = ApiInversiones.cuentasComitentes(contexto, sesion.empresaOB.idCobis).get();

        if (cuentas.isEmpty()) {
            return respuesta("NO_TIENE_CUENTAS_COMITENTES");
        }

        Objeto datos = new Objeto();
        for (CuentaComitente cc : cuentas) {

            Objeto dato = new Objeto();
            dato.set("numeroProducto", cc.numeroProducto);
            dato.set("tipoProducto", cc.tipoProducto);
            dato.set("moneda", cc.monedaDesc);
            datos.add(dato);
        }

        return respuesta("datos", datos);
    }

    public static Object plazosFijos(ContextoOB contexto) {
        Boolean paginado = contexto.parametros.bool("paginado", false);
        SesionOB sesion = contexto.sesion();
        DeudasCuenta deudas = ApiCuentas.deudasCuenta(contexto, sesion.empresaOB.idCobis, paginado, "0").get();
        List<DeudaCuenta> plazosFijos = deudas.filter(d -> d.tipoProducto.equals("PFI")).collect(Collectors.toList());

        if (plazosFijos.isEmpty()) {
            return respuesta("NO_TIENE_PLAZOS_FIJOS");
        }

        Objeto datos = new Objeto();
        for (DeudaCuenta plazo : plazosFijos) {

            String descripcionMoneda = !empty(plazo.moneda) ? plazo.moneda.replace("80", "$").replace("2", "U$D") : "";

            Objeto dato = new Objeto();
            dato.set("numeroProducto", plazo.numeroProducto);
            dato.set("tipoProducto", plazo.tipoProducto);

            dato.set("moneda", descripcionMoneda);
            dato.set("fechaInicio", plazo.fechaInicio);
            dato.set("fechaVencimiento", plazo.fechaVencimiento);
            dato.set("monto", plazo.monto);
            dato.set("montoIntereses", plazo.montoIntereses);
            dato.set("montoComisiones", plazo.montoComisiones);
            datos.add(dato);
        }

        return respuesta("datos", datos);
    }
    
    public static Object getPlazosFijos(ContextoOB contexto) {
    	String idcliente = contexto.sesion().empresaOB.idCobis;
    	
    	PlazoFijoList plazoFijoList = ApiPlazosFijos.getPlazoFijoList(contexto, idcliente).get();
    	
    	if (plazoFijoList == null) {
            return respuesta("NO_TIENE_PLAZOS_FIJOS");
        }
    	
    	return respuesta("plazoFijoList", plazoFijoList);
    
    }
    
    public static Object getPlazosFijosDetalle(ContextoOB contexto) {
    	String numeroBanco = contexto.parametros.string(":numerobanco");
    	
    	PlazoFijoDetalle plazoFijoDetalle = ApiPlazosFijos.getPlazoFijoDetalle(contexto, numeroBanco).get();
    	
    	if (plazoFijoDetalle == null) {
            return respuesta("NO_EXISTE_EL_PLAZO_FIJO");
        }
    	
    	return respuesta("plazoFijoDetalle", plazoFijoDetalle);
    
    }

    public static Object comprobantePlazoFijoHabilitado(ContextoOB contexto) {
        Objeto respuesta = new Objeto();
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        Set<String> cuitsHabilitados = Set.of(
                "30999168953",
                "30624957942",
                "27295443079"
        );
        boolean habilitado = cuitsHabilitados.contains(cuit);
        respuesta.set("estado", "0");
        respuesta.set("habilitado", habilitado);
        return respuesta;
    }
    public static Object plazoFijoDolarHabilitado(ContextoOB contexto) {
        Objeto respuesta = new Objeto();
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        Set<String> cuitsHabilitados = Set.of(
                "30624957942",
                "30686308525",
                "30556649568",
                "27295443079",
                "20399115168"
        );
        boolean habilitado = cuitsHabilitados.contains(cuit);
        respuesta.set("estado", "0");
        respuesta.set("habilitado", habilitado);
        return respuesta;
    }


    public static Object getComprobantePlazoFijo(ContextoOB contexto) {
        String idcliente = contexto.sesion().empresaOB.idCobis;
        String numeroBanco = contexto.parametros.string(":numerobanco");

        PlazoFijoList ListadoPlazoFijo = ApiPlazosFijos.getPlazoFijoList(contexto, idcliente).get();
        PlazoFijoDetalle plazoFijoDetalle = ApiPlazosFijos.getPlazoFijoDetalle(contexto, numeroBanco).get();

        if (plazoFijoDetalle == null && ListadoPlazoFijo == null) {
            return respuesta("NO_EXISTE_EL_PLAZO_FIJO");
        }
        Map<String, String> comprobante = new HashMap<>();

        PlazoFijoList.PlazoFijo plazoFijo = ListadoPlazoFijo.stream().filter(det -> det.numeroProducto != null && det.numeroProducto.equals(numeroBanco)).findFirst().orElse(null);
        PlazoFijoDetalle.PlazoFijoDet detalle = plazoFijoDetalle.get(0);

        String tipoCuenta = "";
        if (detalle.cuenta != null || "0".equals(detalle.cuenta)) {
            if (detalle.cuenta.startsWith("2") || detalle.cuenta.startsWith("4")) {
                tipoCuenta = "CAJA DE AHORRO";
            } else if (detalle.cuenta.startsWith("3")) {
                tipoCuenta = "CUENTA CORRIENTE";
            }
        }
        comprobante.put("TIPO_CUENTA", tipoCuenta);

        SucursalesOB sucursales = ApiCatalogo.sucursalesOB(contexto, null, null, null).get();
        Optional<SucursalesOB.SucursalOB> sucursalOpt = sucursales.stream()
                .sorted(Comparator.comparing(sucu -> sucu.CodSucursal))
                .filter(sucu -> sucu.CodSucursal.equals(String.valueOf(detalle.oficina)))
                .findFirst();
        if (sucursalOpt.isPresent()) {
            SucursalesOB.SucursalOB sucursal = sucursalOpt.get();
            comprobante.put("OFICINA_PF", sucursal.DesSucursal);
        }




        comprobante.put("FECHA_HOY", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        comprobante.put("TIPO_DEPOSITO", detalle.descOperacion);
        comprobante.put("N_DEPOSITO", detalle.numeroBanco);
        comprobante.put("TITULAR_PF", contexto.sesion().empresaOB.razonSocial);

        comprobante.put("CATEGORIA_PF", "A LA ORDEN");
        comprobante.put("MONEDA_PF", plazoFijo.descMoneda);

        comprobante.put("CAPITAL_INICIAL", String.format("%,.2f", detalle.importe));
        comprobante.put("INTERESES_PF", String.format("%,.2f", detalle.interesEstimado));
        comprobante.put("IMP_SELLOS", detalle.sellos != null ? String.format("%,.2f", detalle.sellos) : "0.00");
        comprobante.put("IMP_GANANCIAS", detalle.impuesto != null ? String.format("%,.2f", detalle.impuesto) : "0.00");

        comprobante.put("PLAZO_DIAS", String.valueOf(detalle.cantidadDias));
        comprobante.put("FORMA_DE_PAGO", "AL VENCIMIENTO");
        comprobante.put("TNA_PF", String.format("%,.2f", detalle.tasa));

        comprobante.put("FECHA_ACTIVACION", new SimpleDateFormat("dd/MM/yyyy").format(detalle.fechaActivacion));
        comprobante.put("FECHA_INGRESO", new SimpleDateFormat("dd/MM/yyyy").format(detalle.fechaIngreso));
        comprobante.put("FECHA_VENCIMIENTO", new SimpleDateFormat("dd/MM/yyyy").format(detalle.fechaVencimiento));

        comprobante.put("ROL_TIT", plazoFijo.descTipoTitularidad);
        comprobante.put("NOMBRE_TIT", contexto.sesion().empresaOB.razonSocial +(" ")+contexto.sesion().empresaOB.cuit);

        comprobante.put("N_CUENTA", (detalle.cuenta != null && !"0".equals(detalle.cuenta)) ? detalle.cuenta : "");
        comprobante.put("CAPITAL_FINAL", String.format("%,.2f", detalle.importe + detalle.interesEstimado));

        String template = "comprobante-plazo-fijo-"+detalle.numeroBanco; //
        byte[] pdfComprobante = Pdf.generarPdfPlazoFijo(template, comprobante);

        return respuesta("comprobante", pdfComprobante);
    }

    public static Object getTasas(ContextoOB contexto) {
    	//String idcliente = contexto.parametros.string(":idcliente");
    	String idcliente = contexto.sesion().empresaOB.idCobis;
    	String secuencial = "0";
    	String canal = "HB_BE";
    	
    	Tasas tasas = ApiPlazosFijos.getTasas(contexto, idcliente, secuencial, canal).get();
    	
    	if (tasas == null) {
            return respuesta("NO_TIENE_TASAS");
        }
    	
    	return respuesta("tasas", tasas);
    
    }
    
    public static Object cedips(ContextoOB contexto) {
    	String cuit = contexto.parametros.string(":cuit");
        Cedips cedips = ApiPlazosFijos.getCedips(contexto, cuit).get();
        
        if (cedips == null) {
            return respuesta("NO_TIENE_CEDIPS");
        }
        
        return respuesta("cedips", cedips);
    }

    public static Object obtenerResumenCuentaCuotapartista(ContextoOB contexto) {
        CuentaCuotapartistaResumenRequest request = new CuentaCuotapartistaResumenRequest();
        request.setCodCuotapartista(Integer.parseInt(contexto.parametros.get("codCuotapartista").toString()));
        request.setCodFondo(contexto.parametros.get("codFondo").toString());
        request.setFechaDesde(contexto.parametros.get("fechaDesde").toString());
        request.setFechaHasta(contexto.parametros.get("fechaHasta").toString());
        request.setSoloCtasAct(Boolean.parseBoolean(contexto.parametros.get("soloCtasAct").toString()));

        CuentaCuotapartistaResumen resumen = ApiInversiones.resumenCuentaCuotapartista(contexto, request).get();
        return respuesta("resumen", resumen.Response);
    }

    public static Object cedipsRecibidos(ContextoOB contexto) {
    	String cuit = contexto.parametros.string(":cuit");

        Cedips cedipsRecibidos = ApiPlazosFijos.getCedipsRecibidos(contexto, cuit).get();
        
        if (cedipsRecibidos == null) {
            return respuesta("NO_TIENE_CEDIPS_RECIBIDOS");
        }
        
        List<Cedip> cedipsList = cedipsRecibidos.list();
        List<Cedip> cedipsConTransmisionesPendientes = new ArrayList<>();
        
        for (Cedip cedip : cedipsList) {
            // Verificar si el Cedip tiene transmisiones pendientes
            boolean tieneTransmisionesPendientes = false;
            for (Transmision transmision : cedip.getTransmisiones()) {
                if ("PENDIENTE".equals(transmision.getEstado()) && cuit.equals(transmision.getCuitBeneficiario())) {
                    tieneTransmisionesPendientes = true;
                    break;
                }
            }
            // Si el Cedip tiene transmisiones pendientes, lo agregamos a la nueva lista
            if (tieneTransmisionesPendientes) {
                cedipsConTransmisionesPendientes.add(cedip);
            }
        } 
        return respuesta("cedipsRecibidos", cedipsConTransmisionesPendientes);
	}
    
    public static Object detalleCedip(ContextoOB contexto) {
    	String cedipId = contexto.parametros.string(":cedipid");
    	String cuit = contexto.parametros.string(":cuit");
    	Integer fraccion = contexto.parametros.integer(":fraccion");
    	
        Cedip cedip = ApiPlazosFijos.getDetalleCedip(contexto, cedipId, cuit, fraccion).get();
        
        if (cedip == null) {
            return respuesta("NO_EXISTE_CEDIP");
        }
        
        return respuesta("detalleCedip", cedip);
    }

    public static Object simularPlazoFijo(ContextoOB contexto) {
        String tipoOperacion = contexto.parametros.string("tipoOperacion");
        String plazoStr = contexto.parametros.string("plazo", null);
        Integer plazo = (plazoStr == null || plazoStr.isEmpty()) ? null : Integer.valueOf(plazoStr);
        BigDecimal monto = contexto.parametros.bigDecimal("monto");
        String cuenta = contexto.parametros.string("cuenta");
        Integer idCliente = contexto.parametros.integer("idcliente");
        String capInteres = contexto.parametros.string("capInteres");
        String moneda = contexto.parametros.string("moneda");

        if (tipoOperacion.isEmpty() || monto.toString().isEmpty() || cuenta.toString().isEmpty() || idCliente.toString().isEmpty() || capInteres.toString().isEmpty()) {
            return Respuesta.parametrosIncorrectos();
        }

        if (plazo == null || plazo == 0) {
            List<SimulacionCedip> simulaciones = new ArrayList<>();
            int[] plazos = {30, 60, 90, 365};
            for (int p : plazos) {
                List<SimulacionCedip> simulacion = ApiPlazosFijos.simulacionCedip(
                        contexto, monto, p, cuenta, tipoOperacion, idCliente, capInteres, moneda
                ).get();
                simulaciones.add(simulacion.get(0));
            }
            return Respuesta.exito("simulaciones", simulaciones);
        }

        List<SimulacionCedip> simulacionCedip = ApiPlazosFijos.simulacionCedip(contexto, monto, plazo, cuenta, tipoOperacion, idCliente, capInteres, moneda).get();
        return Respuesta.exito("simulaciones", simulacionCedip);
    }
    
    public static Object cedipNuevo(ContextoOB contexto) {
    	CedipOB cedip = new CedipOB();
    	
    	//ServicioCedipOB servicioCedip = new ServicioCedipOB(contexto);
    	
    	CedipNuevo nuevoCedip = ApiPlazosFijos.nuevoCedip(contexto, cedip).get();
    	
    	return Respuesta.exito("nuevoCedip", nuevoCedip);
    }
    
    public static Object transmitir(ContextoOB contexto) {
    	CedipAccionesOB cedipA = new CedipAccionesOB();
    	
    	ResponsePostTransmision cedipTransmitido = ApiPlazosFijos.transmitirCedip(contexto, cedipA).get();
    	
    	respuesta("cedipTransmitido", cedipTransmitido);
    	
    	return Respuesta.exito("cedipTransmitido", cedipTransmitido);
    }
    
    public static Object admitir(ContextoOB contexto) {
    	Parametros parametros = contexto.parametros;
    	
    	ResponsePostTransmision cedipAceptado = ApiPlazosFijos.aceptarCedip(contexto, parametros).get();
    	
    	respuesta("cedipAceptado", cedipAceptado);
    	
    	return Respuesta.exito("cedipAceptado", cedipAceptado);
    }
    
    public static Object repudiar(ContextoOB contexto) {
    	Parametros parametros = contexto.parametros;
    	
    	ResponsePostTransmision cedipRechazado = ApiPlazosFijos.rechazarCedip(contexto, parametros).get();
    	
    	respuesta("cedipRechazado", cedipRechazado);
    	
    	return Respuesta.exito("cedipRechazado", cedipRechazado);
    }
    
    public static Object anularTransmision(ContextoOB contexto) {
    	CedipAccionesOB cedipA = new CedipAccionesOB();
    	
    	ResponsePostTransmision transmisionCedipAnulado = ApiPlazosFijos.anularTransmisionCedip(contexto, cedipA).get();
    	
    	respuesta("transmisionCedipAnulado", transmisionCedipAnulado);
    	
    	return Respuesta.exito("transmisionCedipAnulado", transmisionCedipAnulado);
    }
    
    public static Object modificarAcreditacionCbu(ContextoOB contexto) {
    	CedipAccionesOB cedipA = new CedipAccionesOB();
    	
    	ResponsePostTransmision cedipModificado = ApiPlazosFijos.modificarAcreditacionCbuCedip(contexto, cedipA).get();
    	
    	respuesta("cedipModificado", cedipModificado);
    	
    	return Respuesta.exito("cedipModificado", cedipModificado);
    }
    
    public static Object depositarCedip(ContextoOB contexto) {
    	CedipAccionesOB cedipA = new CedipAccionesOB();
    	
    	ResponsePostTransmision cedipDepositado = ApiPlazosFijos.depositarCedip(contexto, cedipA).get();
    	
    	respuesta("cedipDepositado", cedipDepositado);
    	
    	return Respuesta.exito("cedipDepositado", cedipDepositado);
    }
    
    public static Object consultaCuit(ContextoOB contexto) {
    	String cuit = contexto.parametros.string(":cuit");
        Cuils cuils = ApiPersonas.cuilsNroDoc(contexto, cuit).get();
        
        if (cuils.list().size() == 0) {
            return respuesta("NO_EXISTE_EMPRESA");
        }
        
        return respuesta("cuils", cuils);
    }
    
    public static Object cargarCedip(ContextoOB contexto) {
    	Object respuestaFinal = null;    	
    	SesionOB sesion = contexto.sesion();
    	Parametros parametros = contexto.parametros;
    	String accion = contexto.parametros.string(":accion"); 
    	String cuenta = contexto.parametros.string("cuenta");
        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);
        
        try {
        	ServicioEstadoBandejaOB servicioEstadoBandejaOB = new ServicioEstadoBandejaOB(contexto);
        	ServicioEstadoCedipOB servicioEstadoCedipOB = new ServicioEstadoCedipOB(contexto);
        	
            EstadoBandejaOB estadoInicialBandeja = servicioEstadoBandejaOB.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
            EstadoCedipOB estadoEnBandeja = servicioEstadoCedipOB.find(EnumEstadoCedipOB.EN_BANDEJA.getCodigo()).get();
            
            ServicioTipoProductoFirmaOB servicioTipoProductoFirmaOB = new ServicioTipoProductoFirmaOB(contexto);
            TipoProductoFirmaOB tipoProductoFirmaOB = servicioTipoProductoFirmaOB.findByCodigo(EnumTipoProductoOB.CEDIP.getCodigo()).get();
            
            ServicioCedipOB servicio = new ServicioCedipOB(contexto);
            
            if (accion.equals("nuevo")) {
            	Integer moneda = contexto.parametros.integer("moneda");
            	BigDecimal monto = contexto.parametros.bigDecimal("monto");
            	CedipOB cedipOB = servicio.enviarCedip(contexto, accion, moneda, monto, estadoEnBandeja, tipoProductoFirmaOB, estadoInicialBandeja).get();            	
            	ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
            	BandejaOB bandeja = servicioBandeja.find(cedipOB.id).get();

	            ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
	            AccionesOB accionCrear = servicioAcciones.find(EnumAccionesOB.CREAR.getCodigo()).get();
	
	            ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
	            
	            servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionCrear, estadoInicialBandeja, estadoInicialBandeja);
	            
	            contexto.parametros.set("idCedip", cedipOB.id);
	            // Objeto detalle = (Objeto) detalle(contexto);
            }
            
            if (accion.equals("transmitir") || accion.equals("anular") || accion.equals("modificar") || accion.equals("depositar")) {
            	Object transmisiones =  parametros.get("transmisiones");
            	Object firmantes =  parametros.get("firmantes");
            	Object montos = parametros.get("montosAdmitirCore");
   	
            	Integer moneda = 80;
            	
            	CedipAccionesOB cedipOB = servicio.cedipAcciones(contexto, accion, cuenta, moneda, transmisiones, firmantes, montos, estadoEnBandeja, tipoProductoFirmaOB, estadoInicialBandeja).get();
            	ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
            	BandejaOB bandeja = servicioBandeja.find(cedipOB.id).get();

	            ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
	            AccionesOB accionCrear = servicioAcciones.find(EnumAccionesOB.CREAR.getCodigo()).get();
	
	            ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
	            
	            servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionCrear, estadoInicialBandeja, estadoInicialBandeja);
	            
	            contexto.parametros.set("idCedip", cedipOB.id);
	            // Objeto detalle = (Objeto) detalle(contexto);
            	
            }
            
        	respuestaFinal = respuesta("datos", "Paso a Bandeja de Firmas");
        	
        } catch (RuntimeException rte) {
            return respuesta(rte.getMessage());
        }
        
        return respuestaFinal;
    }
    
    public static Object plazoFijoNuevo(ContextoOB contexto) {
    	PlazoFijoOB plazoFijo = new PlazoFijoOB();
    	
    	PlazoFijo nuevoPlazoFijo = ApiPlazosFijos.nuevoPlazoFijo(contexto, plazoFijo).get();
    	
    	return Respuesta.exito("nuevoPlazoFijo", nuevoPlazoFijo);
    }
    
    public static Object bandejaPlazoFijo(ContextoOB contexto) {
    	Object respuestaFinal = null;    	
    	SesionOB sesion = contexto.sesion();
        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);
        
        try {
        	ServicioEstadoBandejaOB servicioEstadoBandejaOB = new ServicioEstadoBandejaOB(contexto);
        	ServicioEstadoCedipOB servicioEstadoCedipOB = new ServicioEstadoCedipOB(contexto);
        	
            EstadoBandejaOB estadoInicialBandeja = servicioEstadoBandejaOB.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
            EstadoCedipOB estadoEnBandeja = servicioEstadoCedipOB.find(EnumEstadoCedipOB.EN_BANDEJA.getCodigo()).get();
            
            ServicioTipoProductoFirmaOB servicioTipoProductoFirmaOB = new ServicioTipoProductoFirmaOB(contexto);
            TipoProductoFirmaOB tipoProductoFirmaOB = servicioTipoProductoFirmaOB.findByCodigo(EnumTipoProductoOB.PLAZO_FIJO.getCodigo()).get();
            
            ServicioPlazoFijoOB servicio = new ServicioPlazoFijoOB(contexto);
	
            Integer moneda = contexto.parametros.integer("moneda");
        	BigDecimal monto = contexto.parametros.bigDecimal("monto");
        	PlazoFijoOB plazoFijoOB = servicio.enviarPlazoFijo(contexto, moneda, monto, estadoEnBandeja, tipoProductoFirmaOB, estadoInicialBandeja).get();            	
        	ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
        	BandejaOB bandeja = servicioBandeja.find(plazoFijoOB.id).get();

            ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
            AccionesOB accionCrear = servicioAcciones.find(EnumAccionesOB.CREAR.getCodigo()).get();

            ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
            
            servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionCrear, estadoInicialBandeja, estadoInicialBandeja);
            
            contexto.parametros.set("idPlazoFijo", plazoFijoOB.id);
            // Objeto detalle = (Objeto) detalle(contexto);
            
            respuestaFinal = respuesta("idOperacion", bandeja.id);
        	
        } catch (RuntimeException rte) {
            return respuesta(rte.getMessage());
        }
        
        return respuestaFinal;
    }
    
    public static Object cuentasCuotapartistas(ContextoOB contexto) {
        SesionOB sesion = contexto.sesion();
        Cuotapartistas cuotapartistas;
        if (empty(sesion.empresaOB)) {
            return respuesta("EMPRESA_INVALIDA");
        }

        cuotapartistas = ApiInversiones.cuotapartista(contexto, ID_TIPO_DOCUMENTO_CUIT, sesion.empresaOB.cuit.toString(), "", "", false, "").tryGet();
        if (empty(cuotapartistas)) {
        	cuotapartistas = ApiInversiones.cuotapartista(contexto, ID_TIPO_DOCUMENTO_CUIL, sesion.empresaOB.cuit.toString(), "", "", false, "").tryGet();
        }
        
        if (empty(cuotapartistas)) {
        	return respuesta("DATOS_INVALIDOS");
        }
                
        Set<String> idsUnicos = new HashSet<>();
        List<Cuotapartista>  cuotapartistasaux =  cuotapartistas.stream()
                .filter(cuotapartista -> {
                    if (idsUnicos.contains(cuotapartista.IDCuotapartista)) {
                        return false;
                    } else {
                        idsUnicos.add(cuotapartista.IDCuotapartista);
                        return true;
                    }
                }).toList();
        Objeto datosCuotapartista = new Objeto();
        for (Cuotapartista cu : cuotapartistasaux) {
            Objeto c = new Objeto();
            c.set("id", cu.IDCuotapartista);
            Objeto datosCuentas = new Objeto();
            if(cu.CuentasBancarias != null) {
            	for (CuotapartistaCuentaBancariaModel cb : cu.CuentasBancarias.CuotapartistaCuentaBancariaModel) {
            		Objeto cbm = new Objeto();
            		cbm.set("descripcion", cb.Descripcion);
            		cbm.set("numeroCuenta", cb.NumeroCuenta);
            		cbm.set("idCuentaBancaria", cb.IDCuentaBancaria);
            		cbm.set("cbu", cb.CBU);
            		cbm.set("moneda", cb.Moneda.Description);
            		datosCuentas.add(cbm);
            	}
            	c.set("cuentasBancarias", datosCuentas);
            	datosCuotapartista.add(c);
            }
        }

        Objeto datos = new Objeto();
        datos.set("cuotapartista", datosCuotapartista);
        return respuesta("datos", datos);

    }

    public static Object fondosParaOperar(ContextoOB contexto) {
        Integer idCuotapartista = contexto.parametros.integer("idCuotapartista");
        Integer limite = contexto.parametros.integer("limite", null);
        boolean fondoAbierto;
        Cuotapartistas cuotapartistas;	
        ServicioParametriaFciOB servicioParametria = new ServicioParametriaFciOB(contexto);
        String tipoPersona;
        
        SesionOB sesion = contexto.sesion();
        if (empty(sesion.empresaOB)) {
            return respuesta("EMPRESA_INVALIDA");
        }

        cuotapartistas = ApiInversiones.cuotapartista(contexto, ID_TIPO_DOCUMENTO_CUIT, sesion.empresaOB.cuit.toString(), "", "", false, "").tryGet();
        tipoPersona=ID_TIPO_PERSONA_JURIDICA;
        
        if (empty(cuotapartistas)) {
        	cuotapartistas = ApiInversiones.cuotapartista(contexto, ID_TIPO_DOCUMENTO_CUIL, sesion.empresaOB.cuit.toString(), "", "", false, "").tryGet();
        	tipoPersona=ID_TIPO_PERSONA_FISICA;
        }
        
        if (empty(cuotapartistas)) {
        	return respuesta("DATOS_INVALIDOS");
        }

        final String claseFondo= tipoPersona.equals(ID_TIPO_PERSONA_JURIDICA)
        		?CLASE_FONDO_OB
        		:CLASE_FONDO_OBPF;

        Optional<Cuotapartista> cuotapartista = cuotapartistas.filter(c -> Integer.parseInt(c.IDCuotapartista) == idCuotapartista.intValue()).findFirst();
        if (!cuotapartista.isPresent()) {
            return respuesta("CUOTAPARTISTA_INVALIDO");
        }

        FondosDeInversion fondosDeInversion = ApiInversiones.fondosDeInversion(contexto, idCuotapartista, TIPO_SUSCRIPCION).tryGet();

        List<FondoInversion> fondos = fondosDeInversion.filter(f -> f.TipoVCPDescripcion.equals(claseFondo)).collect(Collectors.toList());
        fondos.sort(Comparator.comparing(FondoInversion::getTpRiesgoNivelRiesgo).thenComparing(FondoInversion::getIndex));

        if (empty(fondosDeInversion)) {
            return respuesta("SIN_FONDOS");
        }

        Objeto datosFondos = new Objeto();
        for (FondoInversion fondo : fondos) {
            Objeto f = new Objeto();
            f.set("id", fondo.FondoID);
            f.set("numero", fondo.FondoNumero);
            f.set("nombre", fondo.getFondoNombre());
            f.set("nombreCompleto", fondo.FondoNombre);
            f.set("nombreAbreviada", fondo.FondoNombreAbr);
            f.set("descripcion", "Suscribe y rescata en " + descripcionFondo(fondo.MonedaDescripcion));
            f.set("tipoVCPDescripcion", fondo.TipoVCPDescripcion);
            f.set("tipoVCPAbreviatura", fondo.TipoVCPAbreviatura);
            f.set("nivelDeRiesgo", fondo.TpRiesgoNivelRiesgo);
            f.set("riesgo", fondo.TpRiesgoDescripcion);
            f.set("condicionIngresoEgresoID", fondo.CondicionIngresoEgresoID);
            f.set("tipoVCPID", fondo.TipoVCPID);
            f.set("monedaId", fondo.MonedaID);
            f.set("plazoLiquidacion", fondo.PlazoLiquidacionFondo);

            ParametriaFciOB parametriaFCI = servicioParametria.buscarPorFondoId(fondo.FondoID).tryGet();
            boolean habilitadoCanales = !empty(parametriaFCI) && parametriaFCI != null && !parametriaFCI.habilitadoCanales.equals(false);

            if (habilitadoCanales) {
                ServicioParametroOB servicioParametros = new ServicioParametroOB(contexto);
                String horaInicio = servicioParametros.find("fci.hora.inicio").get().valor;
                String horaFin = servicioParametros.find("fci.hora.fin").get().valor;

                if (!empty(parametriaFCI) && !empty(parametriaFCI.horaInicio)) {
                    horaInicio = parametriaFCI.horaInicio;
                    horaFin = parametriaFCI.horaFin;

                } else if (!empty(fondo.HoraInicio)) {
                    horaInicio = fondo.HoraInicio;
                    horaFin = fondo.HoraCierre;

                }
                LocalTime hora = LocalTime.now();

                fondoAbierto = hora.isAfter(LocalTime.parse(horaInicio));
                if (fondoAbierto) {
                    fondoAbierto = hora.isBefore(LocalTime.parse(horaFin));
                }


                Objeto horario = new Objeto();
                horario.set("horaInicio", horaInicio);
                horario.set("horaFin", horaFin);
                horario.set("fondoAbierto", fondoAbierto);
                horario.set("diaHabil", diaHabil(contexto));
                f.set("horario", horario);

                Objeto parametros = new Objeto();
                if (!empty(parametriaFCI)) {
                    parametros.set("minAoperar", parametriaFCI.minAoperar);
                    parametros.set("maxAoperar", parametriaFCI.maxAoperar);
                    parametros.set("descripcionLarga", parametriaFCI.descLarga);
                    f.set("parametriaBanco", parametros);
                }

                datosFondos.add(f);
                if (limite != null && datosFondos.toList().size() == limite) {
                    break;
                }
            }
        }

        Objeto datos = new Objeto();
        datos.set("fondos", datosFondos);

        return respuesta("datos", datos);
    }
    
    public static Object fondosParaRescatar(ContextoOB contexto) {
        Integer idCuotapartista = contexto.parametros.integer("idCuotapartista");
        Integer limite = contexto.parametros.integer("limite", null);
        boolean fondoAbierto;
        Cuotapartistas cuotapartistas;	
        ServicioParametriaFciOB servicioParametria = new ServicioParametriaFciOB(contexto);
        String tipoPersona;
        
        SesionOB sesion = contexto.sesion();
        if (empty(sesion.empresaOB)) {
            return respuesta("EMPRESA_INVALIDA");
        }

        cuotapartistas = ApiInversiones.cuotapartista(contexto, ID_TIPO_DOCUMENTO_CUIT, sesion.empresaOB.cuit.toString(), "", "", false, "").tryGet();
        tipoPersona=ID_TIPO_PERSONA_JURIDICA;
        
        if (empty(cuotapartistas)) {
        	cuotapartistas = ApiInversiones.cuotapartista(contexto, ID_TIPO_DOCUMENTO_CUIL, sesion.empresaOB.cuit.toString(), "", "", false, "").tryGet();
        	tipoPersona=ID_TIPO_PERSONA_FISICA;
        }
        
        if (empty(cuotapartistas)) {
        	return respuesta("DATOS_INVALIDOS");
        }

        final String claseFondo= tipoPersona.equals(ID_TIPO_PERSONA_JURIDICA)
        		?CLASE_FONDO_OB
        		:CLASE_FONDO_OBPF;

        Optional<Cuotapartista> cuotapartista = cuotapartistas.filter(c -> Integer.parseInt(c.IDCuotapartista) == idCuotapartista.intValue()).findFirst();
        if (!cuotapartista.isPresent()) {
            return respuesta("CUOTAPARTISTA_INVALIDO");
        }

        FondosDeInversion fondosDeInversion = ApiInversiones.fondosDeInversion(contexto, idCuotapartista, TIPO_SUSCRIPCION).tryGet();

        List<FondoInversion> fondos = fondosDeInversion.list();
        fondos.sort(Comparator.comparing(FondoInversion::getTpRiesgoNivelRiesgo).thenComparing(FondoInversion::getIndex));

        if (empty(fondosDeInversion)) {
            return respuesta("SIN_FONDOS");
        }

        Objeto datosFondos = new Objeto();
        for (FondoInversion fondo : fondos) {
            Objeto f = new Objeto();
            f.set("id", fondo.FondoID);
            f.set("numero", fondo.FondoNumero);
            f.set("nombre", fondo.getFondoNombre());
            f.set("nombreCompleto", fondo.FondoNombre);
            f.set("nombreAbreviada", fondo.FondoNombreAbr);
            f.set("descripcion", "Suscribe y rescata en " + descripcionFondo(fondo.MonedaDescripcion));
            f.set("tipoVCPDescripcion", fondo.TipoVCPDescripcion);
            f.set("tipoVCPAbreviatura", fondo.TipoVCPAbreviatura);
            f.set("nivelDeRiesgo", fondo.TpRiesgoNivelRiesgo);
            f.set("riesgo", fondo.TpRiesgoDescripcion);
            f.set("condicionIngresoEgresoID", fondo.CondicionIngresoEgresoID);
            f.set("tipoVCPID", fondo.TipoVCPID);
            f.set("monedaId", fondo.MonedaID);
            f.set("plazoLiquidacion", fondo.PlazoLiquidacionFondo);

            ParametriaFciOB parametriaFCI = servicioParametria.buscarPorFondoId(fondo.FondoID).tryGet();
            boolean habilitadoCanales = !empty(parametriaFCI) && parametriaFCI != null && !parametriaFCI.habilitadoCanales.equals(false);

            if (habilitadoCanales) {
                ServicioParametroOB servicioParametros = new ServicioParametroOB(contexto);
                String horaInicio = servicioParametros.find("fci.hora.inicio").get().valor;
                String horaFin = servicioParametros.find("fci.hora.fin").get().valor;

                if (!empty(parametriaFCI) && !empty(parametriaFCI.horaInicio)) {
                    horaInicio = parametriaFCI.horaInicio;
                    horaFin = parametriaFCI.horaFin;

                } else if (!empty(fondo.HoraInicio)) {
                    horaInicio = fondo.HoraInicio;
                    horaFin = fondo.HoraCierre;

                }
                LocalTime hora = LocalTime.now();

                fondoAbierto = hora.isAfter(LocalTime.parse(horaInicio));
                if (fondoAbierto) {
                    fondoAbierto = hora.isBefore(LocalTime.parse(horaFin));
                }


                Objeto horario = new Objeto();
                horario.set("horaInicio", horaInicio);
                horario.set("horaFin", horaFin);
                horario.set("fondoAbierto", fondoAbierto);
                horario.set("diaHabil", diaHabil(contexto));
                f.set("horario", horario);

                Objeto parametros = new Objeto();
                if (!empty(parametriaFCI)) {
                    parametros.set("minAoperar", parametriaFCI.minAoperar);
                    parametros.set("maxAoperar", parametriaFCI.maxAoperar);
                    parametros.set("descripcionLarga", parametriaFCI.descLarga);
                    f.set("parametriaBanco", parametros);
                }

                datosFondos.add(f);
                if (limite != null && datosFondos.toList().size() == limite) {
                    break;
                }
            }
        }

        Objeto datos = new Objeto();
        datos.set("fondos", datosFondos);

        return respuesta("datos", datos);
    }

    private static String descripcionFondo(String monedaFondo) {
        String desc = monedaFondo.equals("PESO") ? monedaFondo + "s" : monedaFondo;
        if (desc.equals(monedaFondo)) {
            desc = monedaFondo.equals("DOLAR MEP") ? "dólares" : monedaFondo;
        }
        return desc.toLowerCase();
    }

    public static Object perfil(ContextoOB contexto) {

        SesionOB sesion = contexto.sesion();

        if (empty(sesion.empresaOB)) {
            return respuesta("EMPRESA_INVALIDA");
        }

        PerfilInversor perfilInversor = ApiPersonas.perfilInversor(contexto, sesion.empresaOB.idCobis).tryGet();
        if (perfilInversor == null || empty(perfilInversor) || empty(perfilInversor.perfilInversor)) {
            return respuesta("SIN_PERFIL");
        }

        // TODO.: validar fechas

        Objeto dato = new Objeto();
        dato.set("idCliente", perfilInversor.idCliente);
        dato.set("tipoPersona", perfilInversor.tipoPersona);
        dato.set("estado", perfilInversor.estado);
        dato.set("perfil", EnumPerfilInversorOB.get(perfilInversor.perfilInversor).toString());
        dato.set("fechaAM", perfilInversor.fechaAM);
        dato.set("fechaFin", perfilInversor.fechaFin);

        return respuesta("datos", dato);
    }

    public static Object preguntasPerfilInversor(ContextoOB contexto) {
        ServicioFCI servicioFCI = new ServicioFCI(contexto);
        List<PreguntasPerfilInversorOB> preguntas = servicioFCI.findAll().get();

        Objeto datos = new Objeto();
        for (PreguntasPerfilInversorOB p : preguntas) {
            Objeto datosRespuesta = new Objeto();
            for (RespuestasPerfilInversorOB r : p.respuestas) {
                datosRespuesta.add(new Objeto().set("id", r.id).set("pregunta", r.respuesta));
            }
            datos.add(new Objeto().set("id", p.id).set("pregunta", p.pregunta).add("respuestas", datosRespuesta));
        }

        return respuesta("datos", datos);
    }

    public static Object yaExisteSolicitudPerfilInversorEnBandeja(ContextoOB contexto) {
        SesionOB sesion = contexto.sesion();
        Objeto datos = new Objeto();
        ServicioPerfilInversorOB servicioPerfilInversor = new ServicioPerfilInversorOB(contexto);

        if (empty(sesion.empresaOB)) {
            return respuesta("EMPRESA_INVALIDA");
        }

        List<SolicitudPerfilInversorOB> solicitudes = servicioPerfilInversor.buscarPorEmpresaYEstado(sesion.empresaOB).get();

        if (!solicitudes.isEmpty()) {
            datos.add("perfilYaSolicitado", solicitudes.get(0).nombrePerfil);
            return respuesta("YA EXISTE UNA SOLICITUD DE PERFIL INVERSOR EN BANDEJA PARA ESTA EMPRESA", datos);
        } else return false;
    }

    public static Object formulario(ContextoOB contexto) {
        Objeto respuestas = contexto.parametros.objeto("idsRespuestas");

        SesionOB sesion = contexto.sesion();
        if (empty(sesion.empresaOB)) {
            return respuesta("EMPRESA_INVALIDA");
        }

        List<Integer> idsRespuestas = respuestas.toList().stream().map(value -> Integer.parseInt(value.toString())).collect(Collectors.toList());

        ServicioFCI servicioFCI = new ServicioFCI(contexto);
        EnumPerfilInversorOB perfil = servicioFCI.obtenerPerfil(idsRespuestas);
        if (empty(perfil) || perfil == null) {
            return respuesta("DATOS_INCORRECTOS");
        }

        contexto.parametros.set("perfil", perfil.name());

        if (habilitaBandejaFirmas.get(0).equals("0")) {
            return seleccionarPerfilBandejaOff(contexto, perfil);
        } else if (habilitaBandejaFirmas.get(0).equals("1")) {
            return seleccionarPerfil(contexto);
        }
        return respuesta("ERROR");
    }

    public static Object seleccionarPerfil(ContextoOB contexto) {
        Objeto datos = new Objeto();
        SesionOB sesion = contexto.sesion();
        String perfil = contexto.parametros.string("perfil");

        ServicioPerfilInversorOB servicioPerfilInversor = new ServicioPerfilInversorOB(contexto);

        if (empty(sesion.empresaOB)) {
            return respuesta("EMPRESA_INVALIDA");
        }

        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);
        EnumPerfilInversorOB perfilSeleccionado;

        try {
            perfilSeleccionado = EnumPerfilInversorOB.valueOf(perfil.toUpperCase());
        } catch (Exception e) {
            return respuesta("PERFIL_INVALIDO");
        }

        if (habilitaBandejaFirmas.get(0).equals("0")) {
            datos = seleccionarPerfilBandejaOff(contexto, perfilSeleccionado);
        } else if (habilitaBandejaFirmas.get(0).equals("1")) {

            ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
            ServicioEstadoInversionOB servicioEstadoSolicitud = new ServicioEstadoInversionOB(contexto);

            EstadoBandejaOB estadoInicialBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
            EstadoSolicitudInversionOB estadoSolicitudEnBandeja = servicioEstadoSolicitud.find(EnumEstadoSolicitudFCIOB.PENDIENTE.getCodigo()).get();

            ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);

            SolicitudPerfilInversorOB perfilNuevo = new SolicitudPerfilInversorOB();

            perfilNuevo.nombrePerfil = perfilSeleccionado.name();
            perfilNuevo.idPerfil = perfilSeleccionado.getCodigo().toString();
            perfilNuevo.estado = estadoSolicitudEnBandeja;
            perfilNuevo.empresa = sesion.empresaOB;

            perfilNuevo = servicioPerfilInversor.crear(contexto, perfilNuevo).get();

            BandejaOB bandeja = servicioBandeja.find(perfilNuevo.id).get();

            ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
            AccionesOB accionCrear = servicioAcciones.find(EnumAccionesOB.CREAR.getCodigo()).get();

            ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
            ServicioHistorialSolicitudPI servicioHistorialSolicitudPI = new ServicioHistorialSolicitudPI(contexto);

            servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionCrear, estadoInicialBandeja, estadoInicialBandeja);
            servicioHistorialSolicitudPI.crear(contexto, perfilNuevo, accionCrear, empresaUsuario);

            datos.set("perfil", new Objeto().set("codigo", perfilSeleccionado.getCodigo()).set("nombre", perfilSeleccionado.name()).set("idOperacion", perfilNuevo.id));

        }

        return respuesta("datos", datos);
    }

    private static Objeto seleccionarPerfilBandejaOff(ContextoOB contexto, EnumPerfilInversorOB perfilSeleccionado) {
        SesionOB sesion = contexto.sesion();
        String funcionalidadOB = String.valueOf(EnumTipoProductoOB.PERFIL_INVERSOR.getCodigo());
        try {
            FirmaOB firma = FirmaOB.tipoFirma(contexto, sesion.empresaOB.cuit.toString(), OBFirmas.puedeFirmarPerfilInversor(contexto).toString(), "0", sesion.usuarioOB.cuil.toString(), null, funcionalidadOB);
            if (firma.tipoFirma == TipoFirma.FIRMA_INDISTINTA) {

                ServicioPerfilInversorOB servicioPerfilInversor = new ServicioPerfilInversorOB(contexto);
                SolicitudPerfilInversorOB perfilNuevo = new SolicitudPerfilInversorOB();
                ServicioEstadoInversionOB servicioEstadoSolicitud = new ServicioEstadoInversionOB(contexto);
                ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
                ServicioHistorialSolicitudPI servicioHistorialSolicitudPI = new ServicioHistorialSolicitudPI(contexto);

                EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);

                perfilNuevo.nombrePerfil = perfilSeleccionado.name();
                perfilNuevo.idPerfil = perfilSeleccionado.getCodigo().toString();
                perfilNuevo.estado = servicioEstadoSolicitud.find(EnumEstadoSolicitudFCIOB.EN_PROCESO.getCodigo()).get();
                perfilNuevo.empresa = sesion.empresaOB;

                ServicioFCI servicioFCI = new ServicioFCI(contexto);
                servicioFCI.setPerfil(contexto, sesion.empresaOB.idCobis, EnumPerfilInversorOB.valueOf(perfilNuevo.nombrePerfil));

                perfilNuevo = servicioPerfilInversor.crear(contexto, perfilNuevo).get();

                AccionesOB accionCrear = servicioAcciones.find(EnumAccionesOB.CREAR.getCodigo()).get();

                servicioHistorialSolicitudPI.crear(contexto, perfilNuevo, accionCrear, empresaUsuario);

                return respuesta("perfil", new Objeto().set("codigo", perfilSeleccionado.getCodigo()).set("nombre", perfilSeleccionado.name()));
            }
        } catch (RuntimeException rte) {
            return respuesta(rte.getMessage());
        }

        return respuesta("SIN_FIRMA_INDISTINTA");
    }

    public static Object posicionCuotapartista(ContextoOB contexto) {
        Integer idCuotapartista = contexto.parametros.integer("idCuotapartista");
        String tipoSolicitud = contexto.parametros.string("tipoSolicitud");
        Cuotapartistas cuotapartistas;
        EnumTipoSolicitudFondoOB tipo;
        
        ServicioParametriaFciOB servicioParametria = new ServicioParametriaFciOB(contexto);

        SesionOB sesion = contexto.sesion();
        if (empty(sesion.empresaOB)) {
            return respuesta("EMPRESA_INVALIDA");
        }

        try{
            tipo = EnumTipoSolicitudFondoOB.getTipoSolicitud(tipoSolicitud);
        }catch (IllegalArgumentException e){
            return respuesta("No existe el tipo de solicitud");
        }

        cuotapartistas = ApiInversiones.cuotapartista(contexto, ID_TIPO_DOCUMENTO_CUIT, sesion.empresaOB.cuit.toString(), "", "", false, "").tryGet();
        if (empty(cuotapartistas)) {
        	cuotapartistas = ApiInversiones.cuotapartista(contexto, ID_TIPO_DOCUMENTO_CUIL, sesion.empresaOB.cuit.toString(), "", "", false, "").tryGet();
        }
        
        if (empty(cuotapartistas)) {
        	return respuesta("SIN_CUENTAS_CUOTAPARTISTAS");
        }
        

        Optional<Cuotapartista> cuotapartista = cuotapartistas.filter(c -> Integer.parseInt(c.IDCuotapartista) == idCuotapartista.intValue()).findFirst();
        if (!cuotapartista.isPresent()) {
            return respuesta("CUOTAPARTISTA_INVALIDO");
        }

        PosicionesCuotapartista posicionCuotapartista = ApiInversiones.posicionCuotapartista(contexto, Fecha.hoy().string("yyyy-MM-dd"), "", idCuotapartista, "").tryGet();
        if (empty(posicionCuotapartista)) {
            return respuesta("SIN_POSICION");
        }

        List<String> monedas = posicionCuotapartista.stream().map(p -> p.MonedaDescripcion.trim()).distinct().collect(Collectors.toList());

        Objeto datosTenencia = new Objeto();
        monedas.forEach(mon -> {
            List<PosicionCuotapartista> posiciones = posicionCuotapartista.filter(p -> p.MonedaDescripcion.equals(mon)).collect(Collectors.toList());
            Objeto fondos = new Objeto();
            posiciones.forEach(f -> {
                Objeto fondo = new Objeto();
                fondo.set("moendaSimbolo", f.MonedaSimbolo);
                fondo.set("valor", f.CuotapartesValuadas);
                fondo.set("idFondo", f.FondoID);
                fondo.set("nombre", obtenerFondoNombre(f.FondoNombre));
                fondo.set("nombreCompleto", f.FondoNombre);
                fondo.set("tipoFondo", f.TipoVCPDescripcion);
                fondo.set("descripcion", f.TipoVCPAbreviatura);
                fondo.set("monedaId", f.IDMoneda);
                fondo.set("CondicionIngEgrId", f.IDCondicionIngEgr);
                fondo.set("tipoVCPID", f.TipoVCPID);
                fondo.set("valorCuotaparte", f.UltimoVCPValor);
                fondo.set("cuotapartesTotales", f.CuotapartesTotales);

                ParametriaFciOB parametria = servicioParametria.buscarPorFondoIdTipoYOperacion(Integer.valueOf(f.FondoID), CLASE_FONDO_OB, tipo.getTipoSolicitud()).get();
                Objeto horario = new Objeto();
                horario.set("horaInicio", parametria.horaInicio);
                horario.set("horaFin", parametria.horaFin);

                if ((LocalTime.now().isBefore(LocalTime.parse(parametria.horaFin))) && (LocalTime.now().isAfter(LocalTime.parse(parametria.horaInicio)))) {
                    horario.set("fondoAbierto", true);
                } else horario.set("fondoAbierto", false);

                fondo.set("horario", horario);

                Objeto parametriaBanco = new Objeto();
                parametriaBanco.set("minAoperar", parametria.minAoperar);
                parametriaBanco.set("maxAoperar", parametria.maxAoperar);
                parametriaBanco.set("descripcion", parametria.descLarga);
                
                fondo.set("parametriaBanco", parametriaBanco);               

                fondos.add(fondo);
            });

            BigDecimal sumTenencia = posiciones.stream().map(m -> m.CuotapartesValuadas).reduce(BigDecimal.ZERO, BigDecimal::add);
            datosTenencia.add(new Objeto().set("monedaDescripcion", mon).set("valor", sumTenencia).set("fondos", fondos));
        });

        Objeto respuesta = new Objeto();
        respuesta.set("tenencias", datosTenencia);

        return respuesta("datos", respuesta);
    }

    private static String obtenerFondoNombre(String fondoNombreCompleto) {
        int indice = fondoNombreCompleto.indexOf("-");
        if (indice > 0) {
            return fondoNombreCompleto.substring(indice + 2);
        }
        return fondoNombreCompleto;
    }

    public static Object aceptarFondo(ContextoOB contexto) {
        Integer idFondo = contexto.parametros.integer("idFondo");

        ServicioParametriaFciOB servicioParametria = new ServicioParametriaFciOB(contexto);
        ServicioFondoAceptadoOB servicioFondoAceptado = new ServicioFondoAceptadoOB(contexto);

        SesionOB sesion = contexto.sesion();
        if (empty(sesion.empresaOB)) {
            return respuesta("EMPRESA_INVALIDA");
        }

        FondoAceptadoOB fondoAceptado = new FondoAceptadoOB();
        fondoAceptado.empresa = sesion.empresaOB;
        fondoAceptado.version = Integer.valueOf(servicioParametro.find("fci.version").get().valor);
        fondoAceptado.parametria = servicioParametria.buscarPorFondoId(idFondo).get();

        if (servicioFondoAceptado.buscarPorFondoYEmpresa(fondoAceptado.parametria.id, fondoAceptado.version, sesion.empresaOB).get() != null) {
            return respuesta("FONDO " + idFondo + " YA ACEPTADO POR LA EMPRESA " + sesion.empresaOB.razonSocial + ", VERSION " + fondoAceptado.version);
        }

        try {
            servicioFondoAceptado.save(fondoAceptado);
        } catch (Exception e) {
            return respuesta("ERROR", "NO SE PUDO ACEPTAR EL FONDO");
        }
        return respuesta("FONDO ACEPTADO REGISTRADO");
    }

    public static Object fondoAceptado(ContextoOB contexto) {
        Integer idFondo = contexto.parametros.integer("idFondo");

        ServicioParametriaFciOB servicioParametria = new ServicioParametriaFciOB(contexto);

        SesionOB sesion = contexto.sesion();
        if (empty(sesion.empresaOB)) {
            return respuesta("EMPRESA_INVALIDA");
        }

        Objeto cuentasCuotapartistas = (Objeto) cuentasCuotapartistas(contexto);
        Objeto cuotapartista = (Objeto) cuentasCuotapartistas.get("datos.cuotapartista");
        Object listaCuotapartista = cuotapartista.toList().get(0);
        @SuppressWarnings("rawtypes")
        Object idCuotapartista = ((LinkedHashMap) listaCuotapartista).get("id");

        contexto.parametros.set("idCuotapartista", idCuotapartista);

        String version = servicioParametro.find("fci.version").get().valor;

        ParametriaFciOB parametria = servicioParametria.buscarPorFondoId(idFondo).get();

        Objeto datos = new Objeto();

        ServicioFondoAceptadoOB servicioFondo = new ServicioFondoAceptadoOB(contexto);
        try {
            FondoAceptadoOB fondoAceptado = servicioFondo.buscarPorFondoYEmpresa(parametria.id, Integer.parseInt(version), sesion.empresaOB).get();
            datos.set("fondoAceptado", !empty(fondoAceptado));
        } catch (Exception e) {
            return e;
        }

        return respuesta("datos", datos);
    }

    public static Object suscribir(ContextoOB contexto) {
        Objeto datos = new Objeto();
        String tipoVCPDescripcion=contexto.parametros.string("tipoVCPDescripcion");
        Integer idFondo = contexto.parametros.integer("idFondo");
        String idCuotapartista = contexto.parametros.string("idCuotapartista");
        String idCuentaBancaria = contexto.parametros.string("idCuentaBancaria");
        String cuentaBancaria = contexto.parametros.string("cuentaBancaria");
        String moneda = contexto.parametros.string("moneda");
        BigDecimal importe = contexto.parametros.bigDecimal("importe");

        SesionOB sesion = contexto.sesion();

        if (empty(sesion.empresaOB)) {
            return respuesta("EMPRESA_INVALIDA");
        }

        if (!OBCuentas.habilitada(contexto, cuentaBancaria)) {
            return respuesta("CUENTA_INVALIDA");
        }

        String tipoCuenta = (String) OBCuentas.cuenta(contexto, cuentaBancaria).get("tipoProducto");

        Objeto fondosParaOperar = (Objeto) fondosParaOperar(contexto);
        Objeto datosFondosParaOperar = (Objeto) fondosParaOperar.get("datos.fondos");
        if (datosFondosParaOperar == null || empty(datosFondosParaOperar)) {
            return respuesta("DATOS_INVALIDOS");
        }

        Objeto fondoSeleccionado = datosFondosParaOperar.objetos().stream()
        		.filter(f -> f.get("id").equals(idFondo))
        		.filter(f -> f.get("tipoVCPDescripcion").equals(tipoVCPDescripcion))
        		.findAny().get();
        if (empty(fondoSeleccionado)) {
            return respuesta("FONDO_INVALIDO");
        }

        String plazoLiquidacion = fondoSeleccionado.get("plazoLiquidacion").toString();

        ServicioParametriaFciOB servicioParametria = new ServicioParametriaFciOB(contexto);
        ParametriaFciOB parametria = servicioParametria.buscarPorFondoIdYTipo(idFondo, tipoVCPDescripcion).get();

        ServicioParametroOB servicioParametros = new ServicioParametroOB(contexto);
        String version = servicioParametros.find("fci.version").get().valor;

        FondoAceptadoOB fondoAceptadoOB = new FondoAceptadoOB();
        fondoAceptadoOB.empresa = sesion.empresaOB;
        fondoAceptadoOB.parametria = parametria;
        fondoAceptadoOB.version = Integer.parseInt(version);
        String tipoValorCuotaParte = (String) fondoSeleccionado.get("tipoVCPID");

        if ((importe.signum() != 1) || (importe.compareTo(parametria.minAoperar) < 0) || (importe.compareTo(parametria.maxAoperar) > 0)) {
            return respuesta("MONTO_INVALIDO");
        }

        if (habilitaBandejaFirmas.get(0).equals("0")) {
            datos = invertirBandejaOff(contexto, parametria, cuentaBancaria, importe, moneda, idCuentaBancaria, idCuotapartista, tipoValorCuotaParte);
        } else if (habilitaBandejaFirmas.get(0).equals("1")) {

            ServicioFCIOB servicio = new ServicioFCIOB(contexto);
            FondosComunesOB fci = servicio.crear(contexto, parametria, cuentaBancaria, importe, moneda, idCuentaBancaria, idCuotapartista, tipoValorCuotaParte, "Suscripcion", null, null, tipoCuenta, plazoLiquidacion).get();
            ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
            BandejaOB bandeja = servicioBandeja.find(fci.id).get();

            ServicioEstadoBandejaOB servicioEstadoBandejaOB = new ServicioEstadoBandejaOB(contexto);
            EstadoBandejaOB estadoInicialBandeja = servicioEstadoBandejaOB.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();

            ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
            AccionesOB accionCrear = servicioAcciones.find(EnumAccionesOB.CREAR.getCodigo()).get();

            ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
            ServicioHistorialFCI servicioHistorialFCI = new ServicioHistorialFCI(contexto);

            EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);

            servicioBandejaAcciones.crear(bandeja, empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB), accionCrear, estadoInicialBandeja, estadoInicialBandeja);
            servicioHistorialFCI.crear(fci, accionCrear, empresaUsuario);


            datos.set("idOperacion", bandeja.id);
            datos.set("importe", importe);
            datos.set("monedaSimbolo", convertirMonedaFci(contexto, fondoSeleccionado.get("monedaId").toString()));
            datos.set("cuentaBancaria", cuentaBancaria);
            datos.set("nombre", fondoSeleccionado.get("nombre"));
            datos.set("tipoVCPAbreviatura", fondoSeleccionado.get("tipoVCPAbreviatura"));
            datos.set("fondoAbreviatura", fondoSeleccionado.get("tipoVCPDescripcion"));
        }

        return respuesta("datos", datos);
    }

    private static Objeto invertirBandejaOff(ContextoOB contexto, ParametriaFciOB parametria, String cuentaBancaria, BigDecimal importe, String moneda, String idCuentaBancaria, String idCuotapartista, String tipoValorCuotaParte) {
        SesionOB sesion = contexto.sesion();
        Integer idFondo = contexto.parametros.integer("idFondo");

        if (OBFirmas.puedeFirmarPerfilInversor(contexto).toString().contains("false")) {
            return respuesta("NO_POSEE_CUENTA_CON_FIRMA_PARA_SUSCRIBIR");
        }

        try {
            FirmaOB firma = FirmaOB.tipoFirma(contexto, sesion.empresaOB.cuit.toString(), OBFirmas.puedeFirmarPerfilInversor(contexto).toString(), "0", sesion.usuarioOB.cuil.toString(), null, String.valueOf(EnumTipoProductoOB.FCI.getCodigo()));
            if (firma.tipoFirma == TipoFirma.FIRMA_INDISTINTA) {

                Objeto datos = new Objeto();
                ServicioEstadoBandejaOB servicioEstadoBandejaOB = new ServicioEstadoBandejaOB(contexto);

                ServicioFCIOB servicio = new ServicioFCIOB(contexto);
                FondosComunesOB fci = servicio.crearBandejaOff(contexto, parametria, cuentaBancaria, importe, moneda, idCuentaBancaria, idCuotapartista, tipoValorCuotaParte, "Suscripcion", null, null).get();
                ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
                BandejaOB bandeja = servicioBandeja.find(fci.id).get();

                ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
                AccionesOB accionCrear = servicioAcciones.find(EnumAccionesOB.CREAR.getCodigo()).get();

                ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
                ServicioHistorialFCI servicioHistorialFCI = new ServicioHistorialFCI(contexto);
                ServicioEstadoInversionOB servicioEstadoInversionOB = new ServicioEstadoInversionOB(contexto);

                EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);

                servicioBandejaAcciones.crear(bandeja, empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB), accionCrear, servicioEstadoBandejaOB.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get(), servicioEstadoBandejaOB.find(EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()).get());
                servicioHistorialFCI.cambiaEstado(fci, accionCrear, empresaUsuario, servicioEstadoInversionOB.find(EnumEstadoSolicitudFCIOB.PENDIENTE.getCodigo()).get(), servicioEstadoInversionOB.find(EnumEstadoSolicitudFCIOB.EN_PROCESO.getCodigo()).get());

                Objeto fondosParaOperar = (Objeto) fondosParaOperar(contexto);
                Objeto datosFondosParaOperar = (Objeto) fondosParaOperar.get("datos.fondos");
                if (datosFondosParaOperar == null || empty(datosFondosParaOperar)) {
                    return respuesta("DATOS_INVALIDOS");
                }

                Objeto fondoSeleccionado = datosFondosParaOperar.objetos().stream().filter(f -> f.get("id").equals(idFondo)).findAny().get();
                if (empty(fondoSeleccionado)) {
                    return respuesta("FONDO_INVALIDO");
                }

                SuscripcionSL suscripcion = ApiInversiones.suscripcionSL(contexto, fci.idCuentaBancaria, OBInversiones.obtenerMonedaFci(bandeja.moneda), bandeja.cuentaOrigen, fci.idCuotapartista.toString(), String.valueOf(bandeja.monto), fci.idFondo.idFondo.toString(), fci.idFondo.condIngresoEgreso, fci.tipoVCPid).get();
                if (suscripcion == null || empty(suscripcion)) {
                    return respuesta("DATOS_INVALIDOS");
                }
                datos.set("fechaConcertacion", suscripcion.FechaConcertacion.string("yyyy-MM-dd"));
                datos.set("solicitud", suscripcion.NumSolicitud);
                datos.set("moneda", suscripcion.Moneda.Description);
                datos.set("fondo", suscripcion.InversionFondo.Fondo.ID);
                datos.set("nombre", fondoSeleccionado.get("nombre"));
                datos.set("importe", importe);
                datos.set("cuentaBancaria", cuentaBancaria);

                return respuesta("datos", datos);
            }

        } catch (RuntimeException rte) {
            return respuesta(rte.getMessage());
        }

        return respuesta("SIN_FIRMA_INDISTINTA");
    }

    public static Object rescatar(ContextoOB contexto) {
    	String tipoVCPDescripcion=contexto.parametros.string("tipoVCPDescripcion");
        Integer idFondo = contexto.parametros.integer("idFondo");
        Boolean esTotal = contexto.parametros.bool("esTotal");
        String idCuotapartista = contexto.parametros.string("idCuotapartista");
        String idCuentaBancaria = contexto.parametros.string("idCuentaBancaria");
        String cuentaBancaria = contexto.parametros.string("cuentaBancaria");
        BigDecimal importe = contexto.parametros.bigDecimal("importe");
        Integer cantCuotapartes = contexto.parametros.integer("cantCuotapartes");
        Objeto datos = new Objeto();

        SesionOB sesion = contexto.sesion();

        if (empty(sesion.empresaOB)) {
            return respuesta("EMPRESA_INVALIDA");
        }

        PosicionesCuotapartista posicionesCuotapartista = ApiInversiones.posicionCuotapartista(contexto, Fecha.hoy().string("yyyy-MM-dd"), "", Integer.valueOf(idCuotapartista), "").tryGet();
        if (empty(posicionesCuotapartista)) {
            return respuesta("SIN_POSICION");
        }

        Objeto fondosParaOperar = (Objeto) fondosParaRescatar(contexto);
        Objeto datosFondosParaOperar = (Objeto) fondosParaOperar.get("datos.fondos");
        if (datosFondosParaOperar == null || empty(datosFondosParaOperar)) {
            return respuesta("DATOS INVALIDOS");
        }

        Objeto fondo = datosFondosParaOperar.objetos().stream()
        		.filter(f -> f.get("id").equals(idFondo))
        		.filter(f -> f.get("tipoVCPDescripcion").equals(tipoVCPDescripcion))
        		.findAny().get();
        if (empty(fondo)) {
            return respuesta("FONDO_INVALIDO");
        }

        String plazoLiquidacion = fondo.get("plazoLiquidacion").toString();

        if (!OBCuentas.habilitada(contexto, cuentaBancaria)) {
            return respuesta("CUENTA_INVALIDA");
        }


        String tipoCuenta = (String) OBCuentas.cuenta(contexto, cuentaBancaria).get("tipoProducto");

        if (habilitaBandejaFirmas.get(0).equals("0")) {
            datos = rescatarBandejaOff(contexto, fondo);
        } else if (habilitaBandejaFirmas.get(0).equals("1")) {

            String tipoValorCuotaParte = (String) fondo.get("tipoVCPID");
            String moneda = fondo.get("monedaId").toString();

            ServicioParametriaFciOB servicioParametria = new ServicioParametriaFciOB(contexto);
            ParametriaFciOB parametria = servicioParametria.buscarPorFondoIdYTipo(idFondo, tipoVCPDescripcion).get();
            ParametriaFciOB montoOperable = servicioParametria.buscarPorFondoIdTipoYOperacion(idFondo, tipoVCPDescripcion, "Rescate").get();

            if ((importe.signum() != 1) || (importe.compareTo(montoOperable.minAoperar) < 0) || (importe.compareTo(montoOperable.maxAoperar) > 0)) {
                return respuesta("MONTO_INVALIDO");
            }

            ServicioEstadoBandejaOB servicioEstadoBandejaOB = new ServicioEstadoBandejaOB(contexto);

            EstadoBandejaOB estadoInicialBandeja = servicioEstadoBandejaOB.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();

            ServicioFCIOB servicio = new ServicioFCIOB(contexto);
            FondosComunesOB fci = servicio.crear(contexto, parametria, cuentaBancaria, importe, moneda, idCuentaBancaria, idCuotapartista, tipoValorCuotaParte, "Rescate", cantCuotapartes, esTotal, tipoCuenta, plazoLiquidacion).get();
            ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
            BandejaOB bandeja = servicioBandeja.find(fci.id).get();

            ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
            AccionesOB accionCrear = servicioAcciones.find(EnumAccionesOB.CREAR.getCodigo()).get();

            ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
            ServicioHistorialFCI servicioHistorialFCI = new ServicioHistorialFCI(contexto);

            EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);

            servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionCrear, estadoInicialBandeja, estadoInicialBandeja);
            servicioHistorialFCI.crear(fci, accionCrear, empresaUsuario);

            datos.set("idOperacion", bandeja.id);
            datos.set("importe", importe);
            datos.set("esTotal", esTotal);
            datos.set("cuentaBancaria", cuentaBancaria);
            datos.set("nombre", fondo.get("nombre"));
            datos.set("tipoVCPAbreviatura", parametria.tipoVcpAbreviatura);
            datos.set("fondoAbreviatura", parametria.tipoVcpDescripcion);

        }
        return respuesta("datos", datos);

    }

    private static Objeto rescatarBandejaOff(ContextoOB contexto, Objeto fondo) {
        SesionOB sesion = contexto.sesion();
        Objeto datos = new Objeto();
        String cuentaBancaria = contexto.parametros.string("cuentaBancaria");
        BigDecimal importe = contexto.parametros.bigDecimal("importe");
        Integer idFondo = contexto.parametros.integer("idFondo");
        Boolean esTotal = contexto.parametros.bool("esTotal");
        String moneda = fondo.get("monedaId").toString();


        try {

            FirmaOB firma = FirmaOB.tipoFirma(contexto, sesion.empresaOB.cuit.toString(), cuentaBancaria, importe.toString(), sesion.usuarioOB.cuil.toString(), null, String.valueOf(EnumTipoProductoOB.FCI.getCodigo()));
            if (firma.tipoFirma == TipoFirma.FIRMA_INDISTINTA) {

                String tipoValorCuotaParte = (String) fondo.get("tipoVCPID");



                String idCuotapartista = contexto.parametros.string("idCuotapartista");
                String idCuentaBancaria = contexto.parametros.string("idCuentaBancaria");
                Integer cantCuotapartes = contexto.parametros.integer("cantCuotapartes");
                String condicionIngresoEgreso = (String) fondo.get("condicionIngresoEgresoID");

                ServicioParametriaFciOB servicioParametria = new ServicioParametriaFciOB(contexto);
                ParametriaFciOB parametria = servicioParametria.buscarPorFondoId(idFondo).get();

                ServicioEstadoBandejaOB servicioEstadoBandejaOB = new ServicioEstadoBandejaOB(contexto);
                ServicioEstadoInversionOB servicioEstadoInversionOB = new ServicioEstadoInversionOB(contexto);

                ServicioFCIOB servicio = new ServicioFCIOB(contexto);
                FondosComunesOB fci = servicio.crearBandejaOff(contexto, parametria, cuentaBancaria, importe, moneda, idCuentaBancaria, idCuotapartista, tipoValorCuotaParte, "Rescate", cantCuotapartes, esTotal).get();
                ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
                BandejaOB bandeja = servicioBandeja.find(fci.id).get();

                try{

                    LogOB.evento(contexto, "rescatarBandejaOff", "antes de transmit");
                    LogOB.evento(contexto, "rescatarBandejaOff", "entra transmit");
                    LogOB.evento(contexto, "rescatarBandejaOff", "session entra"+ contexto.sesion().sessionId);
                    TransactionBankProcess.Payer peyeer = new TransactionBankProcess.Payer(contexto.sesion().usuarioOB.cuil.toString(), cuentaBancaria, "044", "OB");
                    TransactionBankProcess.Payee payee = new TransactionBankProcess.Payee(sesion.empresaOB.cuit.toString(), idFondo.toString(),esTotal.toString());

                    TransactionBEBankProcess transactionProcess = new TransactionBEBankProcess(sesion.empresaOB.idCobis, contexto.sesion().sessionId, importe, moneda, "RESCATE", peyeer, payee);

                    RecommendationDTO recommendationDTO = TransmitOB.obtenerRecomendacion(contexto, transactionProcess);


                    if (recommendationDTO.getRecommendationType().equals("DENY")) {
                        LogOB.evento(contexto, "rescatarBandejaOff", "RECOMENDACION: " + recommendationDTO.getRecommendationType());
                        datos.set("estado", "DENY");
                        datos.set("idBandeja", bandeja.id);
                        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
                        EstadoBandejaOB estado = servicioEstadoBandeja.find(EnumEstadoBandejaOB.DENY.getCodigo()).get();
                        EstadoSolicitudInversionOB estadotrn = new EstadoSolicitudInversionOB();
                        estadotrn.id = EnumEstadoSolicitudFCIOB.RECHAZADA.getCodigo();
                        fci.estadoBandeja = estado;
                        fci.estado = estadotrn;
                        servicio.update(fci);
                        return datos;
                    }

                    LogOB.evento(contexto, "rescatarBandejaOff", "fin transmit");
                }catch (Exception e) {
                    LogOB.evento(contexto, "rescatarBandejaOff", "Error al obtener recomendacion: " + e.getMessage());
                }


                ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
                AccionesOB accionCrear = servicioAcciones.find(EnumAccionesOB.CREAR.getCodigo()).get();

                ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
                ServicioHistorialFCI servicioHistorialFCI = new ServicioHistorialFCI(contexto);

                EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);

                servicioBandejaAcciones.crear(bandeja, empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB), accionCrear, servicioEstadoBandejaOB.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get(), servicioEstadoBandejaOB.find(EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()).get());

                try {
                    RescateSL rescate = ApiInversiones.rescate(contexto, cantCuotapartes, cuentaBancaria, idCuotapartista.toString(), esTotal, moneda, importe, idFondo.toString(), condicionIngresoEgreso, tipoValorCuotaParte).tryGet();

                    datos.set("idSolicitud", rescate.IDSolicitud);
                    datos.set("esTotal", rescate.EsTotal);
                    datos.set("fechaAcreditacion", rescate.FechaAcreditacion);
                    datos.set("hora", rescate.Hora.string("HH:mm:ss"));
                    datos.set("idMoneda", rescate.Moneda.ID);
                } catch (Exception e) {
                    return respuesta("No se pudo realizar el rescate.");
                }

                servicioHistorialFCI.cambiaEstado(fci, accionCrear, empresaUsuario, servicioEstadoInversionOB.find(EnumEstadoSolicitudFCIOB.PENDIENTE.getCodigo()).get(), servicioEstadoInversionOB.find(EnumEstadoSolicitudFCIOB.EN_PROCESO.getCodigo()).get());

                datos.set("importe", importe);
                datos.set("esTotal", esTotal);
                datos.set("cuentaBancaria", cuentaBancaria);
                datos.set("nombre", fondo.get("nombre"));

                return respuesta("datos", datos);
            }
        } catch (RuntimeException rte) {
            return respuesta(rte.getMessage());
        }

        return respuesta("SIN_FIRMA_INDISTINTA");
    }

    private static Boolean puedeInvertir(ContextoOB contexto, EmpresaUsuarioOB empresaUsuario) {
        ServicioPermisoOB servicioPermisoOB = new ServicioPermisoOB(contexto);
        if (!contexto.sesion().esOperadorInicial()) {
            ServicioPermisoOperadorOB servicioPermisoOperadorOB = new ServicioPermisoOperadorOB(contexto);
            PermisoOperadorOB permiso = servicioPermisoOperadorOB.buscarPermiso(empresaUsuario, servicioPermisoOB.find(5).get()).tryGet();
            return !empty(permiso);
        }
        return true;
    }

    public static Object detalle(ContextoOB contexto) {
        Integer idFCI = contexto.parametros.integer("idFCI");
        SesionOB sesion = contexto.sesion();
        Cuotapartistas cuotapartistas;
        
        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);
        if (!puedeInvertir(contexto, empresaUsuario)) {
            return respuesta("OPERACION_INVALIDA");
        }

        ServicioFCIOB servicio = new ServicioFCIOB(contexto);
        FondosComunesOB fci = servicio.find(idFCI).get();
        if (empty(fci) || !fci.empresa.idCobis.equals(sesion.empresaOB.idCobis)) {
            return respuesta("DATOS_INVALIDOS");
        }

        Objeto fciDetalle = new Objeto();
        fciDetalle.set("id", fci.id);
        fciDetalle.set("fondo", fci.abreviaturaFondo);
        if (fci.fechaConcertacion != null) {
            fciDetalle.set("fechaConcertacion", fci.fechaConcertacion.toString());
        }
        if (fci.fechaInicio != null) {
            fciDetalle.set("fechaInicio", fci.fechaInicio.toString());
        }
        if (fci.fechaUltActulizacion != null) {
            fciDetalle.set("fechaUltActulizacion", fci.fechaUltActulizacion.toString());
        }

        fciDetalle.set("esTotal", fci.esTotal);
        fciDetalle.set("moneda", fci.moneda.simbolo);
        fciDetalle.set("monto", fci.monto);
        fciDetalle.set("cuentaOrigen", fci.cuentaOrigen);
       
        cuotapartistas = ApiInversiones.cuotapartista(contexto, ID_TIPO_DOCUMENTO_CUIT, sesion.empresaOB.cuit.toString(), "", "", false, "").tryGet();
        if (empty(cuotapartistas)) {
        	cuotapartistas = ApiInversiones.cuotapartista(contexto, ID_TIPO_DOCUMENTO_CUIL, sesion.empresaOB.cuit.toString(), "", "", false, "").tryGet();
        }
        
        if (empty(cuotapartistas)) {
        	return respuesta("DATOS_INVALIDOS");
        }
        
        Optional<Cuotapartista> cuotapartista = cuotapartistas.filter(c -> (c.IDCuotapartista).equals(fci.idCuotapartista)).findFirst();
        String cuentaCuotapartista = cuotapartista.get().CuentasBancarias.CuotapartistaCuentaBancariaModel.get(0).NumeroCuenta;

        fciDetalle.set("cuentaCuotapartista", cuentaCuotapartista);
        fciDetalle.set("creadaPor", fci.usuario.nombre + " " + fci.usuario.apellido);

        Objeto estado = new Objeto();

        if (fci.estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {
            EstadoSolicitudInversionOB solicitudEstado = fci.estado;
            estado.set("id", solicitudEstado.id);
            estado.set("descripcionCorta", solicitudEstado.descripcion);
            fciDetalle.set("estado", estado);
        } else {
            estado.set("id", fci.estadoBandeja.id);
            estado.set("descripcionCorta", fci.estadoBandeja.descripcion);
            fciDetalle.set("estado", estado);
        }

        return respuesta("datos", fciDetalle);
    }

    public static Object detallePerfilInversor(ContextoOB contexto) {
        Integer id = contexto.parametros.integer("idPlazoFijo");
        SesionOB sesion = contexto.sesion();

        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);
        if (!puedeInvertir(contexto, empresaUsuario)) {
            return respuesta("OPERACION_INVALIDA");
        }

        ServicioPerfilInversorOB servicio = new ServicioPerfilInversorOB(contexto);
        SolicitudPerfilInversorOB solicitud = servicio.find(id).get();
        if (empty(solicitud) || !solicitud.empresa.idCobis.equals(sesion.empresaOB.idCobis)) {
            return respuesta("DATOS_INVALIDOS");
        }

        Objeto detalleSolicitud = new Objeto();
        detalleSolicitud.set("id", solicitud.id);
        detalleSolicitud.set("idPerfil", solicitud.idPerfil);
        detalleSolicitud.set("perfilSolicitado", solicitud.nombrePerfil);
        if (solicitud.fechaUltActulizacion != null)
            detalleSolicitud.set("fechaUltActualizacion", solicitud.fechaUltActulizacion.toString());
        detalleSolicitud.set("creadaPor", solicitud.usuario.nombre + " " + solicitud.usuario.apellido);

        if (solicitud.estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {
            detalleSolicitud.set("estado", solicitud.estado.descripcion);
        } else {
            detalleSolicitud.set("estado", solicitud.estadoBandeja.descripcion);
        }

        return respuesta("datos", detalleSolicitud);
    }
    
    public static Object detallePlazoFijoBandeja(ContextoOB contexto) {
        Integer id = contexto.parametros.integer("idPlazoFijo");
        SesionOB sesion = contexto.sesion();

        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);
        if (!puedeInvertir(contexto, empresaUsuario)) {
            return respuesta("OPERACION_INVALIDA");
        }

        ServicioPlazoFijoOB servicio = new ServicioPlazoFijoOB(contexto);
        PlazoFijoOB plazoFijo = servicio.find(id).get();
        if (empty(plazoFijo) || !plazoFijo.empresa.idCobis.equals(sesion.empresaOB.idCobis)) {
            return respuesta("DATOS_INVALIDOS");
        }

        Objeto detallePlazoFijo = new Objeto();
        detallePlazoFijo.set("id", plazoFijo.id);
        detallePlazoFijo.set("monto", plazoFijo.monto);
        detallePlazoFijo.set("moneda", plazoFijo.moneda.simbolo);

        if (plazoFijo.estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {
        	detallePlazoFijo.set("estado", plazoFijo.estado_plazo_fijo.descripcion);
        } else {
        	detallePlazoFijo.set("estado", plazoFijo.estadoBandeja.descripcion);
        }

        return respuesta("datos", detallePlazoFijo);
    }

    public static Object historial(ContextoOB contexto) {
        Fecha fechaDesde = contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd", null);
        Fecha fechaHasta = contexto.parametros.fecha("fechaHasta", "yyyy-MM-dd", null);
        String cuenta = contexto.parametros.string("cuenta", null);
        String tipoSolicitud = contexto.parametros.string("tipoSolicitud", null);
        Integer idMoneda = contexto.parametros.integer("idMoneda", null);

        SesionOB sesion = contexto.sesion();
        ServicioMonedaOB servicioMoneda = new ServicioMonedaOB(contexto);
        ServicioFCIOB servicioFCI = new ServicioFCIOB(contexto);

        if (empty(sesion.empresaOB)) {
            return respuesta("EMPRESA_INVALIDA");
        }

        MonedaOB moneda = servicioMoneda.find(idMoneda).tryGet();
        if (!empty(idMoneda) && idMoneda != EnumMonedasOB.PESOS.getMoneda() && idMoneda != EnumMonedasOB.DOLARES.getMoneda()) {
            return respuesta("MONEDA_INVALIDA");
        }


        Objeto respuesta = new Objeto();
        List<FondosComunesOB> movimientos = servicioFCI.filtrarMovimientosHistorial(sesion.empresaOB, fechaDesde, fechaHasta, cuenta, tipoSolicitud, moneda).get();

        for (FondosComunesOB m : movimientos) {
            Objeto datos = new Objeto();

            datos.set("tipoSolicitud", m.tipoSolicitud);
            datos.set("descripcion", obtenerFondoNombre(m.idFondo.fondoNombre));
            if (!m.estadoBandeja.id.equals(EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo())) {
                datos.set("estado", m.estadoBandeja.descripcion);
            } else datos.set("estado", m.estado.descripcion);

            datos.set("estadoBandeja", m.estadoBandeja.descripcion);
            datos.set("monto", m.monto);
            datos.set("moneda", m.moneda.id);
            datos.set("fechaCreacion", m.fechaInicio.toLocalDate().toString());

            respuesta.add(datos);
        }

        return respuesta("datos", respuesta);
    }


    public static Object validarMontoInversion(ContextoOB contexto) {
        Integer idFondo = contexto.parametros.integer("idFondo");
        BigDecimal importe = contexto.parametros.bigDecimal("importe");

        ServicioParametriaFciOB servicioParametria = new ServicioParametriaFciOB(contexto);

        ParametriaFciOB fondo = servicioParametria.buscarPorFondoId(idFondo).get();
        if (fondo == null) {
            return respuesta("No se encontró ningún fondo con id " + idFondo);
        }

        if ((importe.signum() != 1) || (importe.compareTo(fondo.minAoperar) < 0) || (importe.compareTo(fondo.maxAoperar) > 0)) {
            return false;
        }
        return true;
    }

    public static Object diaHabil(ContextoOB contexto) {

        Fecha fecha = Fecha.hoy();
        DiaBancario dia = ApiCatalogo.diaBancario(contexto, fecha).get();

        return dia.esDiaHabil() ? true : false;
    }

    protected static Object ejecutarSuscripcion(ContextoOB contexto, FondosComunesOB fci, BandejaOB bandeja) {
        Objeto suscripcion;
        String idTransaccion;
        LogOB.evento(contexto, "ejecutarSuscripcion","INICIO");
        try {
            LogOB.evento(contexto, "ejecutarSuscripcion","DEBITO");
            DebitosCreditosOB debito = ApiCuentas.debitos(contexto, "1300", fci.cuentaOrigen, fci.moneda.id, fci.monto, contexto.sesion().usuarioOB.numeroDocumento.toString(), fci.tipoCuenta).get();
            idTransaccion = debito.idTransaccion;
        } catch (Exception e) {
            LogOB.evento(contexto, "ejecutarSuscripcion","FALLO EL DEBITO: "+e.getMessage());
            throw new RuntimeException("FALLO EL DEBITO: " + e.getMessage());
        }
        LogOB.evento(contexto, "ejecutarSuscripcion","idTransaccion: "+idTransaccion);
        try {
            LogOB.evento(contexto, "ejecutarSuscripcion","SUSCRIPCION: ");
            suscripcion = ApiInversiones.suscripcionSL(contexto, fci.idCuentaBancaria, OBInversiones.obtenerMonedaFci(bandeja.moneda), bandeja.cuentaOrigen, fci.idCuotapartista.toString(), String.valueOf(bandeja.monto), fci.idFondo.idFondo.toString(), fci.idFondo.condIngresoEgreso, fci.tipoVCPid).get().objeto();
        } catch (Exception e) {
            try {
                ApiCuentas.reversaDebitos(contexto, "1300", fci.cuentaOrigen, fci.moneda.id, fci.monto, contexto.sesion().usuarioOB.numeroDocumento.toString(), fci.tipoCuenta, idTransaccion).get();
            } catch (Exception ex) {
                LogOB.evento(contexto, "ejecutarSuscripcion","FALLO EL REVERSO DEL DEBITO: "+ex.getMessage());
                return respuesta("FALLO EL REVERSO DEL DEBITO", ex.getMessage());
            }
            LogOB.evento(contexto, "ejecutarSuscripcion","FALLO LA SUSCRIPCION: "+e.getMessage());
            throw new RuntimeException("FALLO LA SUSCRIPCION: " + e.getMessage());

        }
        LogOB.evento(contexto, "ejecutarSuscripcion","SUSCRIPCION OK");
        return suscripcion;

    }

    protected static Objeto ejecutarRescate(ContextoOB contexto, FondosComunesOB fci, BandejaOB bandeja) {
        Objeto rescate;
        String idTransaccion = null;
        LogOB.evento(contexto, "ejecutarRescate","INICIO");
        LogOB.evento(contexto, "ejecutarRescate","fci.esTotal: "+fci.esTotal.toString());
        LogOB.evento(contexto, "ejecutarRescate","fci.plazoLiquidacion:" +fci.plazoLiquidacion);
        if ((fci.esTotal.equals(false)) && (fci.plazoLiquidacion.equals("0")) && (fci.idFondo.idFondo==9 || fci.idFondo.idFondo==20)) {
            try {
                LogOB.evento(contexto, "ejecutarRescate","CREDITO:");
                DebitosCreditosOB credito = ApiCuentas.creditos(contexto, "1301", fci.cuentaOrigen, fci.moneda.id, fci.monto, contexto.sesion().usuarioOB.numeroDocumento.toString(), fci.tipoCuenta).get();
                idTransaccion = credito.idTransaccion;
            } catch (Exception e) {
                LogOB.evento(contexto, "ejecutarRescate","FALLO EL CREDITO: " + e.getMessage());
                throw new RuntimeException("FALLO EL CREDITO: " + e.getMessage());
            }
        }
        LogOB.evento(contexto, "ejecutarRescate","idTransaccion: " +idTransaccion);
        try {
            LogOB.evento(contexto, "ejecutarRescate","RESCATE");
            rescate = ApiInversiones.rescate(contexto, fci.cantidadCuotaPartes, fci.idCuentaBancaria, fci.idCuotapartista.toString(), fci.esTotal, OBInversiones.obtenerMonedaFci(bandeja.moneda), bandeja.monto, fci.idFondo.idFondo.toString(), fci.idFondo.condIngresoEgreso, fci.tipoVCPid).get().objeto();
        } catch (Exception e) {
            if ((fci.esTotal.equals(false)) && (fci.plazoLiquidacion.equals("0")) && (fci.idFondo.idFondo==9 || fci.idFondo.idFondo==20)) {
                try {
                    LogOB.evento(contexto, "ejecutarRescate","REVERSA");
                    ApiCuentas.reversaCreditos(contexto, "1301", fci.cuentaOrigen, fci.moneda.id, fci.monto, contexto.sesion().usuarioOB.numeroDocumento.toString(), fci.tipoCuenta, idTransaccion).get();
                } catch (Exception ex) {
                    LogOB.evento(contexto, "ejecutarRescate","FALLO EL REVERSO DEL CREDITO", ex.getMessage());
                    return respuesta("FALLO EL REVERSO DEL CREDITO", ex.getMessage());
                }
            }
            LogOB.evento(contexto, "ejecutarRescate","FALLO EL RESCATE: " + e.getMessage());
            throw new RuntimeException("FALLO EL RESCATE: " + e.getMessage());
        }
        LogOB.evento(contexto, "ejecutarRescate","RESCATE OK");
        return rescate;
    }

    protected static String obtenerMonedaFci(MonedaOB moneda) {
        String monedaFci = null;
        if (moneda.descripcion.equalsIgnoreCase("PESOS"))
            monedaFci = "1";
        if (moneda.codigoCobis.equalsIgnoreCase("USD"))
            monedaFci = "2";
        return monedaFci;
    }

    private static Integer convertirMonedaFci(ContextoOB contexto, String idMoneda) {
        ServicioMonedaOB servicioMoneda = new ServicioMonedaOB(contexto);
        Integer monedaId = null;
        if (idMoneda.equals("2")) {
            monedaId = servicioMoneda.find(EnumMonedasOB.DOLARES.getMoneda()).get().id;
        } else if (idMoneda.equals("1")) {
            monedaId = servicioMoneda.find(EnumMonedasOB.PESOS.getMoneda()).get().id;
        }
        return monedaId;
    }

    public static void rechazarSinFirmaCompleta(ContextoOB contexto, List<FondosComunesOB> fcisARechazar) {
        ServicioFCIOB servicioFCI = new ServicioFCIOB(contexto);
        ServicioHistorialFCI servicioHistorialFCI = new ServicioHistorialFCI(contexto);

        for (FondosComunesOB f : fcisARechazar) {
            ServicioEstadoInversionOB servicioEstadoInversion = new ServicioEstadoInversionOB(contexto);

            f.estado = servicioEstadoInversion.find(EnumEstadoSolicitudFCIOB.RECHAZADA.getCodigo()).get();
            servicioFCI.update(f);

            ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
            BandejaOB bandeja = servicioBandeja.find(f.id).get();

            ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
            EmpresaUsuarioOB empresaUsuario = servicioEmpresaUsuario.findByUsuarioEmpresa(f.usuario, f.empresa).get();

            ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
            AccionesOB accionRechazar = servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get();

            ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
            EstadoBandejaOB estadoRechazado = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();

            ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
            servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionRechazar, bandeja.estadoBandeja, estadoRechazado).get();

            bandeja.estadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
            servicioBandeja.update(bandeja);

            EstadoSolicitudInversionOB estadoPendiente = servicioEstadoInversion.find(EnumEstadoSolicitudFCIOB.PENDIENTE.getCodigo()).get();
            EstadoSolicitudInversionOB estadoRechazada = servicioEstadoInversion.find(EnumEstadoSolicitudFCIOB.RECHAZADA.getCodigo()).get();

            servicioHistorialFCI.cambiaEstado(f, accionRechazar, empresaUsuario, estadoPendiente, estadoRechazada).get();
        }
    }
    
    public static Object posicionCuotapartistaConsolidada(ContextoOB contexto) {
        Integer idCuotapartista = contexto.parametros.integer("idCuotapartista");
        //Se deja el parámetro comentado hasta el evolutivo
        Fecha fechaHast = contexto.parametros.fecha("fechaHasta", "yyyy-MM-dd", null);       
        Fecha fechaHasta = (fechaHast!=null && !fechaHast.isNull()) ? fechaHast : Fecha.hoy();
        Cuotapartistas cuotapartistas;
        ServicioParametriaFciOB servicioParametria = new ServicioParametriaFciOB(contexto);
        List fondosEnPosicion = new ArrayList();
        BigDecimal totalCalculado=BigDecimal.ZERO;
        SesionOB sesion = contexto.sesion();
        List<String> monedasSolicitudes=null;
        
        Fecha fechaDesde;
        DiaBancario dia = ApiCatalogo.diaBancario(contexto, fechaHasta).tryGet();
        if(dia!=null) {
        	fechaDesde = dia.diaHabilAnterior;
        }else {
        	fechaDesde=fechaHasta;
        }
        
        
        if (empty(sesion.empresaOB)) {
            return respuesta("EMPRESA_INVALIDA");
        }
     
        cuotapartistas = ApiInversiones.cuotapartista(contexto, ID_TIPO_DOCUMENTO_CUIT, sesion.empresaOB.cuit.toString(), "", "", false, "").tryGet();
        if (empty(cuotapartistas)) {
        	cuotapartistas = ApiInversiones.cuotapartista(contexto, ID_TIPO_DOCUMENTO_CUIL, sesion.empresaOB.cuit.toString(), "", "", false, "").tryGet();
        }
        
        if (empty(cuotapartistas)) {
        	return respuesta("SIN_CUENTAS_CUOTAPARTISTAS");
        }
        
        
        Optional<Cuotapartista> cuotapartista = cuotapartistas.filter(c -> Integer.parseInt(c.IDCuotapartista) == idCuotapartista.intValue()).findFirst();
        if (!cuotapartista.isPresent()) {
            return respuesta("CUOTAPARTISTA_INVALIDO");
        }

        Solicitudes solicitudesOB = ApiInversiones.getSolicitudes(contexto, fechaDesde.toString(), fechaHasta.toString(), null, null, null, null, null, Integer.valueOf(idCuotapartista), null).tryGet();
        PosicionesCuotapartista posicionCuotapartista = ApiInversiones.posicionCuotapartista(contexto, fechaHasta.string("yyyy-MM-dd"),"", idCuotapartista, "").tryGet();
        
        if (empty(posicionCuotapartista) && empty(solicitudesOB)) {
            return respuesta("SIN_POSICION");
        }
        
        List<String> monedas;
        
        if(posicionCuotapartista!=null) {
        	monedas = posicionCuotapartista.stream().map(p -> p.MonedaDescripcion.trim()).distinct().collect(Collectors.toList());
        }else {
        	monedas=new ArrayList<>();
        }
      
           
        if(solicitudesOB!=null) {
        	monedasSolicitudes = solicitudesOB.stream().map(p -> p.MonedaDescripcion.trim()).distinct().collect(Collectors.toList());
        }
        	
        
        if (monedasSolicitudes != null) {
            monedas.addAll(monedasSolicitudes);
            monedas = monedas.stream().distinct().collect(Collectors.toList());
        }
        
        Objeto datosTenencia = new Objeto();
        monedas.forEach(mon -> {
        	//Antes de iterar verifico si tiene posición, sino lo declaro como vacio para que pase a solocitudes
            List<PosicionCuotapartista> posiciones = 
            		posicionCuotapartista!=null ?
            				posicionCuotapartista.filter(p -> p.MonedaDescripcion.equals(mon)).collect(Collectors.toList()):
            					new ArrayList<>();
            Objeto fondos = new Objeto();
            posiciones.forEach(f -> {
            	fondosEnPosicion.add(f.FondoID);
                Objeto fondo = new Objeto();
                fondo.set("moendaSimbolo", f.MonedaSimbolo);
                fondo.set("valor", f.CuotapartesValuadas);
                fondo.set("idFondo", f.FondoID);
                fondo.set("nombre", obtenerFondoNombre(f.FondoNombre));
                fondo.set("nombreCompleto", f.FondoNombre);
                fondo.set("tipoFondo", f.TipoVCPDescripcion);
                fondo.set("descripcion", f.TipoVCPAbreviatura);
                fondo.set("monedaId", f.IDMoneda);
                fondo.set("CondicionIngEgrId", f.IDCondicionIngEgr);
                fondo.set("tipoVCPID", f.TipoVCPID);
                fondo.set("cuotapartes", f.CuotapartesTotales);
                fondo.set("valorVCP", f.UltimoVCPValor);
                
                List<Objeto> movimientos = obtenerSolicitudes(solicitudesOB, f.FondoID);
                if(movimientos.size()>0 && movimientos.get(0).string("totalCalculado")!="0") {
                	BigDecimal nuevoMonto = BigDecimal.valueOf(Double.parseDouble(movimientos.get(0).string("totalCalculado")));
                	if(movimientos.get(0).string("rescateTotal").equals("true")) {
                		BigDecimal totalCalculadoConRT = totalCalculado.add(nuevoMonto);
                		fondo.set("valor", totalCalculadoConRT);
                	}else {
                		BigDecimal totalCalculadoConSRT = totalCalculado.add(f.CuotapartesValuadas.add(nuevoMonto));
                		fondo.set("valor", totalCalculadoConSRT);
                	}
                	
                	
                	fondo.set("nuevoMonto", String.valueOf(nuevoMonto));
                	fondo.set("CuotapartesValuadas", String.valueOf(f.CuotapartesValuadas));
                }
                Objeto horario = new Objeto();
                try {
                    ParametriaFciOB parametria = servicioParametria.buscarPorFondoId(Integer.valueOf(f.FondoID)).get();
                    if (parametria != null) {
                        horario.set("horaInicio", parametria.horaInicio);
                        horario.set("horaFin", parametria.horaFin);

                        if (LocalTime.now().isBefore(LocalTime.parse(parametria.horaFin))
                                && LocalTime.now().isAfter(LocalTime.parse(parametria.horaInicio))) {
                            horario.set("fondoAbierto", true);
                        } else {
                            horario.set("fondoAbierto", false);
                        }
                    } else {
                        horario.set("horaInicio", null);
                        horario.set("horaFin", null);
                        horario.set("fondoAbierto", false);
                    }
                } catch (Exception ex) {
                    // cuando no hay parametría o falla el get()
                    horario.set("horaInicio", null);
                    horario.set("horaFin", null);
                    horario.set("fondoAbierto", false);
                }

                fondo.set("horario", horario);
                
                fondos.add(fondo);
            });

            
            List<Objeto> nuevasSuscripciones = obtenerNuevasSuscripciones(solicitudesOB, fondosEnPosicion, mon);
            if(nuevasSuscripciones!=null) {
            	for (Objeto suscripcion : nuevasSuscripciones) {
            		fondos.add(suscripcion);
            	}
            }
            //Calculo el total invertido con lo guardado en fondos
            BigDecimal sumTenencia = BigDecimal.ZERO;
            for (Objeto fondo : fondos.objetos()) {
                BigDecimal valor = new BigDecimal(fondo.get("valor").toString());
                sumTenencia = sumTenencia.add(valor);
            }
            
            datosTenencia.add(new Objeto().set("monedaDescripcion", mon).set("valor", sumTenencia).set("fondos", fondos));
        });
        
      
        
        Objeto respuesta = new Objeto();
        respuesta.set("tenencias", datosTenencia);

        return respuesta("datos", respuesta);
    }

    
    public static Object historialFci(ContextoOB contexto) {
        Fecha fechaDesde = contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd", null);
        Fecha fechaHasta = contexto.parametros.fecha("fechaHasta", "yyyy-MM-dd", null);
        String cuenta = contexto.parametros.string("cuenta", null);
        String tipoSolicitud = contexto.parametros.string("tipoSolicitud", null);
        Integer idMoneda = contexto.parametros.integer("idMoneda", null);

        SesionOB sesion = contexto.sesion();
        ServicioMonedaOB servicioMoneda = new ServicioMonedaOB(contexto);
        ServicioFCIOB servicioFCI = new ServicioFCIOB(contexto);

        if (empty(sesion.empresaOB)) {
            return respuesta("EMPRESA_INVALIDA");
        }

        MonedaOB moneda = servicioMoneda.find(idMoneda).tryGet();
        if (!empty(idMoneda) && idMoneda != EnumMonedasOB.PESOS.getMoneda() && idMoneda != EnumMonedasOB.DOLARES.getMoneda()) {
            return respuesta("MONEDA_INVALIDA");
        }

        String fDsd=fechaDesde.toString();
        String fHst=fechaHasta.toString();
        Integer cta = Integer.valueOf(cuenta);
        //Objeto respuesta = new Objeto();
        List<Objeto> datosList = new ArrayList<>();
        
        //Servicios del historial OB + Servicios 
        List<FondosComunesOB> movimientos = servicioFCI.filtrarMovimientosHistorial(sesion.empresaOB, fechaDesde, fechaHasta, cuenta, tipoSolicitud, moneda).tryGet();
        Solicitudes solicitudes = ApiInversiones.getSolicitudes(contexto, fDsd, fHst, null, null, null, null, null, Integer.valueOf(cuenta), null).tryGet();
        Liquidaciones liquidaciones = ApiInversiones.getLiquidaciones(contexto, fDsd, fHst, Integer.valueOf(cuenta), null).tryGet();
       
     	
        if(movimientos!=null) {
        for (FondosComunesOB m : movimientos) {
            Objeto datos = new Objeto();
           
            if(m.estado.id<EnumEstadoSolicitudFCIOB.REALIZADA.getCodigo()) {
            	datos.set("id", m.idTransaccion!=null? m.idTransaccion:m.id);
            	datos.set("tipoSolicitud", m.tipoSolicitud);
                datos.set("descripcion", obtenerFondoNombre(m.idFondo.fondoNombre));
                datos.set("estado", m.estadoBandeja.descripcion);
                datos.set("estadoBandeja", m.estadoBandeja.descripcion);
                datos.set("monto", m.monto);
                datos.set("moneda", m.moneda.id);
                datos.set("monedaSimbolo", (m.moneda.id==1 || m.moneda.id==80) ? "$" : "USD");
                datos.set("fechaCreacion", m.fechaInicio.toLocalDate().toString());
                datos.set("cuotapartes", null);
                datos.set("valorCuotapartes", null);
                datosList.add(datos);
                
            }
        }
        }
        
        if(solicitudes!=null) {
        for (Solicitudes.Solicitud s : solicitudes) {
        	  Objeto datos = new Objeto();
              
              if(!s.EstadoSolicitud.equals(EnumEstadosLiquidacionFCI.ESTADO_FINAL.getRespuesta())) {;
                    
            	  datos.set("id", s.NumSolicitud);
            	  datos.set("tipoSolicitud", s.TipoSolicitud);
                  datos.set("descripcion", obtenerFondoNombre(s.FondoNombre));
                  datos.set("estado", 
                		         (
                				  s.EstadoSolicitud.equalsIgnoreCase(EnumEstadosLiquidacionFCI.NO_REQUIERE_AUTORIZACION.getRespuesta())
                                  ||  s.EstadoSolicitud.equalsIgnoreCase(EnumEstadosLiquidacionFCI.AUTORIZADO.getRespuesta())
                                  ||  s.EstadoSolicitud.equalsIgnoreCase(EnumEstadosLiquidacionFCI.PENDIENTE_DE_AUTORIZACION.getRespuesta()))
                				  ? EnumEstadosLiquidacionFCI.PENDIENTE_LIQUIDACION.getRespuesta() 
                  				  : s.EstadoSolicitud);
                  datos.set("estadoBandeja", EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo());
                  datos.set("monto", s.Importe);
                  datos.set("moneda", s.MonedaID);
                  datos.set("monedaSimbolo", (s.MonedaID.equals("1") || s.MonedaID.equals("80")) ? "$" : "USD");
                  datos.set("fechaCreacion", s.FechaConcertacion);
                  datos.set("cuotapartes", null);
                  datos.set("valorCuotapartes", null);
                  datosList.add(datos);
              }
         }
        }
        
        if(liquidaciones!=null) {
         for (Liquidaciones.Liquidacion l : liquidaciones) {
       	  Objeto datos = new Objeto();
                          
    	     datos.set("id", l.LiquidacionSolicitud);
       	     datos.set("tipoSolicitud", l.LiquidacionTipo);
             datos.set("descripcion", obtenerFondoNombre(l.FondoNombre));
             datos.set("estado", EnumEstadosLiquidacionFCI.LIQUIDADA.getRespuesta());
             datos.set("estadoBandeja", EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo());
             datos.set("monto", l.ImporteNeto);
             datos.set("moneda", l.MonedaID);
             datos.set("monedaSimbolo", (l.MonedaID==1 || l.MonedaID==80) ? "$" : "USD");
             datos.set("fechaCreacion", l.FechaConcertacion);
             datos.set("cuotapartes", l.Cuotapartes);
             datos.set("valorCuotapartes", l.VCPValor);
             datosList.add(datos);
          }
        }
        
        
        datosList.sort((o1, o2) -> o2.get("fechaCreacion").toString().compareTo(o1.get("fechaCreacion").toString()));

        Objeto respuesta = new Objeto();
        for (Objeto datos : datosList) {
            respuesta.add(datos);
        }
        
        
        return respuesta("datos", respuesta);
    }
    
    public static Object extractoFci(ContextoOB contexto) {
        Fecha fechaDesde = contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd", null);
        Fecha fechaHasta = contexto.parametros.fecha("fechaHasta", "yyyy-MM-dd", null);
        String cuenta = contexto.parametros.string("cuenta", null);
        List fondosConMovimientos = new ArrayList();
        
        SesionOB sesion = contexto.sesion();

        if (empty(sesion.empresaOB)) {
            return respuesta("EMPRESA_INVALIDA");
        }

        String fDsd=fechaDesde.toString();
        String fHst=fechaHasta.toString();
        String fechaHastaSaldo = fechaDesde.restarDias(1).toString();
        Objeto respuesta = new Objeto();
        
        //cabecera : seteo los datos generales
        respuesta.set("razonSocial", sesion.empresaOB.razonSocial);
        respuesta.set("cuentaCuotapartista", cuenta);
        respuesta.set("fechaDesde", fechaDesde);
        respuesta.set("fechaHasta", fechaHasta);
        respuesta.set("fechaConsulta", Fecha.ahora());
        respuesta.set("fechaHastaSaldo", fechaHastaSaldo);

        //obtengo las liquidaciones en el rango de fechas
        Liquidaciones liquidaciones = ApiInversiones.getLiquidaciones(contexto, fDsd, fHst, Integer.valueOf(cuenta), null).tryGet();
       
        //obtengo los movimientos por fondo
        if (liquidaciones != null) {
        	 Map<Integer, List<Objeto>> datosPorFondos = new HashMap<>();
        	    Map<Integer, String> nombresFondos = new HashMap<>(); 
        	    Map<Integer, Integer> monedasFondos = new HashMap<>();
  
            for (Liquidaciones.Liquidacion l : liquidaciones) {
                Objeto datosFondo = new Objeto();
                datosFondo.set("id", l.LiquidacionSolicitud);
                datosFondo.set("tipoSolicitud", l.LiquidacionTipo);
                datosFondo.set("descripcion", obtenerFondoNombre(l.FondoNombre));
                datosFondo.set("montoPesos", l.MonedaID.equals(1) ? l.ImporteNeto : 0);
                datosFondo.set("montoDolares", l.MonedaID.equals(1) ? 0 : l.ImporteNeto);
                datosFondo.set("moneda", l.MonedaID);
                datosFondo.set("fechaCreacion", l.FechaConcertacion);
                datosFondo.set("cuotapartes", l.Cuotapartes);
                datosFondo.set("valorCuotapartes", l.VCPValor);

                int fondoID = l.FondoID;
                if (!datosPorFondos.containsKey(fondoID)) {
                    datosPorFondos.put(fondoID, new ArrayList<>());
                    fondosConMovimientos.add(fondoID);
                }

                if (!nombresFondos.containsKey(fondoID)) {
                    nombresFondos.put(fondoID, obtenerFondoNombre(l.FondoNombre));
                }
                if (!monedasFondos.containsKey(fondoID)) {
                    monedasFondos.put(fondoID, l.MonedaID);
                }

                datosPorFondos.get(fondoID).add(datosFondo);
            }

            Objeto datosFondos = new Objeto();
           
            datosPorFondos.forEach((fondoID, liquidacionesFondo) -> {
            	 String nombreFondo = nombresFondos.get(fondoID);
                 int monedaId = monedasFondos.get(fondoID);

                  Objeto fondoData = new Objeto();
                  fondoData.set("numero", fondoID);
                  fondoData.set("nombre", nombreFondo);
                  fondoData.set("monedaId", monedaId);
                  fondoData.set("movimientos", liquidacionesFondo);
            	    datosFondos.set("fondo " + String.valueOf(fondoID), fondoData);
            });
            respuesta.set("fondos", datosFondos);
        }
        return respuesta("datos", respuesta);
    }
    
    
    private static Objeto obtenerSaldoFondos (ContextoOB contexto, String fondo, String fechaHasta, int idCuotapartista) {
    	Objeto fondosEnPosicion = new Objeto();
    	PosicionesCuotapartista posicionCuotapartista = ApiInversiones.posicionCuotapartista(contexto, fechaHasta, "", idCuotapartista, "").tryGet();
    	 Objeto fondoPosicion = new Objeto();
    	
    	
    	if(posicionCuotapartista==null) {
    		 
              fondoPosicion.set("valorEnPesos", 0);
              fondoPosicion.set("valorEnDolares", 0);
              fondoPosicion.set("monedaId", "");
              fondoPosicion.set("cuotapartes", 0);
              return fondoPosicion;
    	}
   
    	  List<PosicionCuotapartista> posiciones = posicionCuotapartista.list();
         
    	  
          posiciones.forEach(f -> {
        	  if(f.FondoID.equals(String.valueOf(fondo))) {
	              //Objeto fondoPosicion = new Objeto();
	              fondoPosicion.set("valorEnPesos", f.IDMoneda==1 ? f.CuotapartesValuadas : 0);
	              fondoPosicion.set("valorEnDolares", f.IDMoneda==1 ? 0 : f.CuotapartesValuadas);
	              fondoPosicion.set("monedaId", f.IDMoneda);
	              fondoPosicion.set("cuotapartes", f.CuotapartesTotales);
	              
	              //fondosEnPosicion.add(fondoPosicion);
        	  }
        	 });
          
          
	          if(fondoPosicion.isEmpty()) {
	        	  //Objeto fondoPosicion = new Objeto();
		              fondoPosicion.set("valorEnPesos",  0);
		              fondoPosicion.set("valorEnDolares", 0);
		              fondoPosicion.set("monedaId", "");
		              fondoPosicion.set("cuotapartes", 0);
		              //fondosEnPosicion.add(fondoPosicion);
	          }
    	  return fondoPosicion;
    }
    
    
    private static Objeto obtenerTotales (ContextoOB contexto, String fondo, Fecha fechaDesde, Fecha fechaHasta, int idCuotapartista) {
    	List<PosicionCuotapartista> posiciones;
    	List<PosicionCuotapartista> vcp;
    	BigDecimal totalLiq = BigDecimal.ZERO;
        BigDecimal totaCutapartes = BigDecimal.ZERO;
    	BigDecimal posicionAFecha=BigDecimal.ZERO;
    	BigDecimal cuotapartesAFecha=BigDecimal.ZERO;
    	BigDecimal valorCuotaparteAFecha=BigDecimal.ZERO;
    	int idMoneda=1;
    	Objeto totales = new Objeto();
    	
    	String fechaHastaSaldo = fechaDesde.restarDias(1).toString();
    	//Posicion al corte de fecha de los movimientos (desde - 1)
    	PosicionesCuotapartista posicionCuotapartista = ApiInversiones.posicionCuotapartista(contexto, fechaHastaSaldo, "", idCuotapartista, "").tryGet();
    	
    	//Posicion al corte de fecha hasta de los movimientos para obtener el valor de la cuotaparte
    	PosicionesCuotapartista valorCuotaparteAFechaPosicion = ApiInversiones.posicionCuotapartista(contexto, fechaHasta.toString(), "", idCuotapartista, "").tryGet();
    	
    	//Movimientos en el rango de fecha
    	Liquidaciones liquidaciones = ApiInversiones.getLiquidaciones(contexto, fechaDesde.toString(), fechaHasta.toString(), idCuotapartista, null).tryGet();
    	
    	
    	if(liquidaciones==null) {
    	      return null;
    	}
   
    	if(posicionCuotapartista!=null) {
    		posiciones = posicionCuotapartista.list();  		    
  	    }else {
  	    	posiciones=new ArrayList();
  	    }
    	
    	if(valorCuotaparteAFechaPosicion!=null) {
    		vcp = valorCuotaparteAFechaPosicion.list();  		    
  	    }else {
  	    	vcp=new ArrayList();
  	    }
    	 
    	  List<Liquidacion> liquidadas = liquidaciones.list();
    	  
          
          for (PosicionCuotapartista p : posiciones) {
			  if(p.FondoID.equals(fondo)) {
				  BigDecimal importe = p.CuotapartesValuadas != null ? p.CuotapartesValuadas : BigDecimal.ZERO;
				  BigDecimal cuotapartes = p.CuotapartesTotales != null ? p.CuotapartesTotales : BigDecimal.ZERO;
				  posicionAFecha = importe; 
				  cuotapartesAFecha=cuotapartes;
				  idMoneda=p.IDMoneda;
			   }
          }
          
          for (PosicionCuotapartista v : vcp) {
			  if(v.FondoID.equals(fondo)) {
				  BigDecimal vc = v.UltimoVCPValor != null ? v.UltimoVCPValor : BigDecimal.ZERO;
				  valorCuotaparteAFecha = vc; 
			   }
          }
              
          for (Liquidacion liquidacion : liquidadas) {
        	  if(liquidacion.FondoID.toString().equals(fondo)) {
        	  BigDecimal importe = liquidacion.ImporteBruto != null ? liquidacion.ImporteBruto : BigDecimal.ZERO;
        	  BigDecimal  cuotapartes= liquidacion.Cuotapartes != null ? BigDecimal.valueOf(Double.valueOf(liquidacion.Cuotapartes)) : BigDecimal.ZERO;
              totalLiq= totalLiq.add(liquidacion.LiquidacionTipo.equalsIgnoreCase("Suscripción") ? importe : importe.negate());
              totaCutapartes = totaCutapartes.add(cuotapartes);
              idMoneda=liquidacion.MonedaID;
              
        	  }
		}
      	  	
          BigDecimal saldoPCP=(cuotapartesAFecha.add(totaCutapartes)).multiply(valorCuotaparteAFecha);
          BigDecimal resultado= saldoPCP.add(posicionAFecha.add(totalLiq).negate());
          
          totales.set("totalInvertidoPesos", idMoneda==1 ? posicionAFecha.add(totalLiq) :0);
          totales.set("totalInvertidoDolares", idMoneda==1 ? 0 :posicionAFecha.add(totalLiq));
          totales.set("cantidadCuotapartes", cuotapartesAFecha.add(totaCutapartes));
          totales.set("valorCuotaparte", valorCuotaparteAFecha);
          totales.set("saldoCuotaparteEnPesos", idMoneda==1 ? saldoPCP : 0);
          totales.set("saldoCuotaparteEnDoalres", idMoneda==1 ? 0 : saldoPCP);
          totales.set("resultadoEnPesos", idMoneda==1 ? resultado : 0);
          totales.set("resultadoEnDolares",  idMoneda==1 ? 0 : resultado);
          
    	  return totales;
    }
    
    
    public static Object obtenerDetallesSolicitud(ContextoOB contexto) {
        String solicitud = contexto.parametros.string("solicitud");
        SesionOB sesion = contexto.sesion();
        Objeto respuesta = new Objeto();
        String cuentaOrigen=null;
        String creadoPor=null;
        String creadoPorFecha=null;
        String creadoPorHora=null;
        String cbuCuenta;
        Objeto obtenerCbu;
        ServicioBandejaOB servBDF = new ServicioBandejaOB(contexto);
        FondosComunesOB datosSol=null;
        FondosComunesOB datosSolBDF=null;
        ServicioFCIOB servicioFCI = new ServicioFCIOB(contexto);
        String cbu=null;
        if (empty(sesion.empresaOB)) {
            return respuesta("EMPRESA_INVALIDA");
        }
        Integer empCodigo=sesion.empresaOB.emp_codigo;
        
        datosSol = servicioFCI.buscarIdSolicitud(solicitud).tryGet();
        datosSolBDF = servicioFCI.find(Integer.valueOf(solicitud)).tryGet();
        
        if(datosSol!=null && datosSol.empCodigo.emp_codigo.intValue()==empCodigo) {
       	    cuentaOrigen= datosSol.cuentaOrigen;
            creadoPor=datosSol.usuario.nombre + " " + datosSol.usuario.apellido;
            creadoPorFecha=datosSol.fechaInicio.toLocalDate().toString();
            creadoPorHora=datosSol.fechaInicio.toLocalTime().withSecond(0).withNano(0).toString();
        }
        
        if(datosSolBDF!=null && datosSolBDF.empCodigo.emp_codigo.intValue()==empCodigo) {
       	    cuentaOrigen= datosSolBDF.cuentaOrigen;
            creadoPor=datosSolBDF.usuario.nombre + " " + datosSolBDF.usuario.apellido;
            creadoPorFecha=datosSolBDF.fechaInicio.toLocalDate().toString();
            creadoPorHora=datosSolBDF.fechaInicio.toLocalTime().withSecond(0).withNano(0).toString();
        }
        
        
        
        if(datosSol!=null || datosSolBDF!=null) {	
        obtenerCbu = OBCuentas.cuenta(contexto, cuentaOrigen);
        cbu = obtenerCbu != null ?
        		obtenerCbu.get("cbu").toString() 
        		:null;
        }
        
        
        respuesta.set("cuenta", cuentaOrigen);
        respuesta.set("cbu", cbu);
        respuesta.set("creadoPor", creadoPor);
        respuesta.set("creadoPorFecha", creadoPorFecha);
        respuesta.set("creadoPorHora",  creadoPorHora);
        
        return respuesta("datos", respuesta);
    }
    
private static List<Objeto> obtenerSolicitudes (Solicitudes solicitudesOB, String fondo) {
    	
    	boolean tieneTotal = obtenerRescateTotal(solicitudesOB, fondo);
    	
    	List<Objeto> movimientos = new ArrayList<>(); 
    	Map<String, BigDecimal> totalPorFondo = new HashMap<>();
    	if(solicitudesOB==null) {
    		 Objeto o = new Objeto();
             o.set("idFondo", "");
             o.set("totalCalculado", 0);
             movimientos.add(o);
    		return movimientos;
    	}
    	String esTotal="s";
          solicitudesOB.stream()
              .filter(solicitud ->
                  solicitud.EstadoSolicitud.equalsIgnoreCase("No Requiere Autorización")
                      || solicitud.EstadoSolicitud.equalsIgnoreCase("Autorizado")
                      || solicitud.EstadoSolicitud.equalsIgnoreCase("Pendiente de Autorización"))
              .filter(solicitud -> fondo.isEmpty() || solicitud.FondoID.equals(fondo))
              .filter(solicitud -> { 
            	   if (tieneTotal) {
                       return solicitud.TipoSolicitud.equalsIgnoreCase("Suscripcion");
                   } else {
                       return true;
                   }
              })
              .forEach(i -> {
                  BigDecimal importe = i.Importe != null ? BigDecimal.valueOf(Double.valueOf(i.Importe)) : BigDecimal.ZERO;
                  String fondoID = i.FondoID;
                  BigDecimal totalCalculado = i.TipoSolicitud.equalsIgnoreCase("Suscripcion") ? importe : importe.negate();
                  totalPorFondo.put(fondoID, totalPorFondo.getOrDefault(fondoID, BigDecimal.ZERO).add(totalCalculado));
              });

          totalPorFondo.forEach((fondoID, total) -> {
              Objeto o = new Objeto();
              o.set("idFondo", fondoID);
              o.set("totalCalculado", total);
              o.set("rescateTotal", tieneTotal?"true":"false");
              movimientos.add(o);
          });
  		

        return movimientos;
    }
    

    private static List<Objeto> obtenerNuevasSuscripciones(Solicitudes solicitudesOB, List<String> fondosExistentes, String moneda) {
        List<Objeto> movimientos = new ArrayList<>();
        
        if (solicitudesOB == null) {
            return null;
        }

        Map<String, BigDecimal> totalPorFondo = new HashMap<>();

        solicitudesOB.stream()
                .filter(solicitud ->
                        solicitud.EstadoSolicitud.equalsIgnoreCase("No Requiere Autorización")
                                || solicitud.EstadoSolicitud.equalsIgnoreCase("Autorizado")
                                || solicitud.EstadoSolicitud.equalsIgnoreCase("Pendiente de Autorización"))
                .filter(solicitud -> solicitud.MonedaDescripcion.equals(moneda))
                .forEach(solicitud -> {
                    BigDecimal importe = solicitud.Importe != null ? BigDecimal.valueOf(Double.parseDouble(solicitud.Importe)) : BigDecimal.ZERO;
                    String fondoID = solicitud.FondoID;
                    BigDecimal totalCalculado = solicitud.TipoSolicitud.equalsIgnoreCase("Suscripcion") ? importe : importe.negate();

                    if (!fondosExistentes.contains(fondoID)) {
                        totalPorFondo.put(fondoID, totalPorFondo.getOrDefault(fondoID, BigDecimal.ZERO).add(totalCalculado));
                    }
                });

        totalPorFondo.forEach((fondoID, total) -> {
            Solicitudes.Solicitud solicitudFondo = solicitudesOB.stream()
                                                    .filter(s -> s.FondoID.equals(fondoID))
                                                    .findFirst()
                                                    .orElse(null);

            if (solicitudFondo != null) {
                Objeto f = new Objeto();
                f.set("moendaSimbolo", solicitudFondo.MonedaSimbolo);
                f.set("valor", total); 
                f.set("idFondo", fondoID);
                f.set("nombre", obtenerFondoNombre(solicitudFondo.FondoNombre));
                f.set("nombreCompleto", solicitudFondo.FondoNombre);
                f.set("tipoFondo", solicitudFondo.TipoVCPDescripcion);
                f.set("descripcion", solicitudFondo.TipoVCPAbreviatura);
                f.set("monedaId", solicitudFondo.MonedaID);
                f.set("CondicionIngEgrId", "");
                f.set("tipoVCPID", solicitudFondo.TipoVCPID);
                f.set("cuotapartes", "Aguardando valor de cierre");
                f.set("valorVCP", "Aguardando valor de cierre");
                
                Objeto horario = new Objeto();
                horario.set("horaInicio", "00:00:00");
                horario.set("horaFin", "00:00:00");
                horario.set("fondoAbierto", false);
                f.set("horario", horario);
                
                movimientos.add(f);
            }
        });

        return movimientos;
    }


    
    private static BigDecimal obtenerAdicionesAlTotal (Solicitudes solicitudesOB, String moneda) {
    	
    	if(solicitudesOB==null) {
    		return BigDecimal.ZERO;
    	}
    	 return solicitudesOB.stream()
    	            .filter(solicitud ->
    	                    solicitud.EstadoSolicitud.equalsIgnoreCase("No Requiere Autorización")
    	                            || solicitud.EstadoSolicitud.equalsIgnoreCase("Autorizado")
    	                            || solicitud.EstadoSolicitud.equalsIgnoreCase("Pendiente de Autorización"))
    	            .filter(solicitud -> solicitud.MonedaDescripcion.equals(moneda))
    	            .map(solicitud -> {
    	                BigDecimal importe = solicitud.Importe != null ? BigDecimal.valueOf(Double.valueOf(solicitud.Importe)) : BigDecimal.ZERO;
    	                return solicitud.TipoSolicitud.equalsIgnoreCase("Suscripcion") ? importe : importe.negate();
    	            })
    	            .reduce(BigDecimal.ZERO, BigDecimal::add);
    	};
    	
    	
    	private static boolean obtenerRescateTotal(Solicitudes solicitudesOB, String fondo) {
    	    if (solicitudesOB == null) {
    	        return false;
    	    }
    	    return solicitudesOB.stream()
    	            .filter(solicitud ->
    	                    solicitud.EstadoSolicitud.equalsIgnoreCase("No Requiere Autorización")
    	                            || solicitud.EstadoSolicitud.equalsIgnoreCase("Autorizado")
    	                            || solicitud.EstadoSolicitud.equalsIgnoreCase("Pendiente de Autorización"))
    	            .filter(solicitud -> fondo.isEmpty() || solicitud.FondoID.equals(fondo))
    	            .anyMatch(solicitud -> solicitud.EsTotal);
    	}
    	
    	public static Object cuentasComitentesActivas(ContextoOB contexto) {
	        SesionOB sesion = contexto.sesion();
	        CuentasComitentes cuentas = ApiInversiones.cuentasComitentesActivas(contexto, sesion.empresaOB.idCobis, "vigente").get();

	        if (cuentas.isEmpty()) {
	            return respuesta("NO_TIENE_CUENTAS_COMITENTES");
	        }

	        Objeto datos = new Objeto();
	        for (CuentaComitente cc : cuentas) {

	            Objeto dato = new Objeto();
	            dato.set("numeroProducto", cc.numeroProducto);
	            dato.set("tipoProducto", cc.tipoProducto);
	            dato.set("moneda", cc.monedaDesc);
	            datos.add(dato);
	        }

	        return respuesta("datos", datos);
	    }

	
	
	 public static Object cuentasComitentesTotalInvertido(ContextoOB contexto) {
	    	BigDecimal monto=BigDecimal.ZERO;
	    	String idCuentaComitente = contexto.parametros.string("cuentaComitente");
	    	BigDecimal saldoEnCuentaPesos=BigDecimal.ZERO;;
	    	BigDecimal saldoEnCuentaDolares=BigDecimal.ZERO;;
	    	BigDecimal totalInvertido=BigDecimal.ZERO;;
	    	
	        SesionOB sesion = contexto.sesion();
	        if (empty(sesion.empresaOB)) {
	            return respuesta("EMPRESA_INVALIDA");
	        }

	         //CuentaComitenteEspeciesOB posicionComitente = ApiInversiones.cuentaComitenteEspecieOB(contexto, sesion.empresaOB.idCobis, idCuentaComitente).get();

	         CuentaComitenteEspeciesOB posicionComitente = ApiInversiones.cuentaComitenteEspecieOB(contexto, "1125598", "2-000112759").get();

	         if (empty(posicionComitente)) {
	            return respuesta("SIN_POSICION");
	        }
	        
	        
	        for (int i = 0; i < posicionComitente.size(); i++) {
	            monto = monto.add(posicionComitente.get(i).valorizacion);
	        }
	        
	        monto = monto.setScale(2, RoundingMode.HALF_UP);
	         
	        
	        totalInvertido = posicionComitente.stream()
	                .filter(item -> !"MON".equals(item.tipoProducto))
	                .map(item->item.valorizacion)
	                .reduce(BigDecimal.ZERO, BigDecimal::add);

	        saldoEnCuentaPesos = posicionComitente.stream()
	                .filter(item -> "PESOS".equals(item.monedaCotizacion))
	               	.filter(item -> "MON".equals(item.tipoProducto))
	                .map(item->item.valorizacion)
	                .reduce(BigDecimal.ZERO, BigDecimal::add);
	        
	        saldoEnCuentaDolares = posicionComitente.stream()
	        	    .filter(item -> "USD".equals(item.monedaCotizacion))
	                .filter(item -> "MON".equals(item.tipoProducto))
	                .map(item->item.valorizacion)
	                .reduce(BigDecimal.ZERO, BigDecimal::add);
	        
	        totalInvertido = totalInvertido.setScale(2, RoundingMode.HALF_UP);
	        saldoEnCuentaPesos = saldoEnCuentaPesos.setScale(2, RoundingMode.HALF_UP);
	        saldoEnCuentaDolares = saldoEnCuentaDolares.setScale(2, RoundingMode.HALF_UP);
	        
	       
	    	
	        Objeto datos = new Objeto();
	        datos.set("total", monto);
	        datos.set("totalInvertido", totalInvertido);
	        datos.set("saldoEnCuentaPesos", saldoEnCuentaPesos);
	        datos.set("saldoEnCuentaDolares", saldoEnCuentaDolares);

	        return respuesta("datos", datos);
	    }

    public static Object obtenerTenenciaEspecies(ContextoOB contexto) {
        String idCuentaComitente = contexto.parametros.string("cuentaComitente");
        String tipoActivo = contexto.parametros.string("tipoActivo");
        Boolean previsualizacion = contexto.parametros.bool("previsualizacion");
        String fechaHst = contexto.parametros.string("fechaHasta", null);

        LocalDate hoy = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String fecha = hoy.format(formatter);

        BigDecimal totalFondosPesos = BigDecimal.ZERO;
        BigDecimal totalSaldoPesos = BigDecimal.ZERO;
        BigDecimal totalSaldoDolares = BigDecimal.ZERO;
        int cantidadTitulosValoresPesos = 0;

        Fecha fe = (fechaHst == null) ? Fecha.ahora() : new Fecha(fechaHst, "yyyy-MM-dd");
        Objeto tenencias = new Objeto();
        SesionOB sesion = contexto.sesion();
        String cantidad = "1000";

        if (idCuentaComitente == null) {
            return respuesta("CUENTA_COMITENTE_NO_EXISTE");
        }

        CuentaComitentesLicitacionesOB posicionesNegociables = ApiInversiones.cuentaComitentesLicitacionesV2OB(contexto, sesion.empresaOB.idCobis.toString(), idCuentaComitente, fe, cantidad, "1").tryGet();
        if (posicionesNegociables == null || posicionesNegociables.posicionesNegociablesOrdenadas == null || posicionesNegociables.posicionesNegociablesOrdenadas.isEmpty() || posicionesNegociables.codigoHttp() == 404) {
            return respuesta("SIN_TENENCIA");
        }

        Map<String, ProductosOperablesOrdenados> productosOperables = obtenerProductosOperablesMapByCodigo(contexto, fecha);
        Map<String, ProductosOperablesOrdenados> productosOperablesbyProducto = null;
        Map<String, ProductosOperablesOrdenados> productosOperablesbyProductoPesos = null;
        int count = 0;

        for (CuentaComitenteLicitacion posicion : posicionesNegociables.posicionesNegociablesOrdenadas) {
            String codigo = posicion.codigo;
            BigDecimal saldoNominal = posicion.saldoDisponible;
            ProductosOperablesOrdenados productoOperable = productosOperables.get(codigo);

            if (previsualizacion != null && previsualizacion && count >= 5) break;

            if (!"Todos".equalsIgnoreCase(tipoActivo)) {
                if ((tipoActivo != null && !tipoActivo.isEmpty()) && !posicion.clasificacion.equalsIgnoreCase(tipoActivo)) {
                    if (!posicion.clasificacion.equalsIgnoreCase("") || !tipoActivo.equals("Titulo Publico")) continue;
                }
            }

            Objeto datos = new Objeto();

            if (productoOperable == null) {
                if (productosOperablesbyProducto == null) {
                    productosOperablesbyProducto = obtenerProductosOperablesMapByProducto(contexto, fecha);
                }
                productoOperable = productosOperablesbyProducto.get(codigo);

                if (productoOperable == null && !"moneda".equalsIgnoreCase(posicion.clasificacion) && !"".equalsIgnoreCase(posicion.clasificacion)) {
                    datos.set("id", codigo);
                    datos.set("descripcion", codigo + " - " + posicion.descripcionTenencia);
                    datos.set("tipoActivo", posicion.clasificacion);
                    datos.set("cantidadNominal", saldoNominal);
                    datos.set("cantidadNominalFormateada", FormateadorOB.importe(saldoNominal).replace(",00", ""));
                    datos.set("fecha", posicion.fechaPrecioConsulta);
                    datos.set("valorPesos", 0);
                    datos.set("valorPesosFormateado", "0");
                    datos.set("saldoValuadoPesos", 0);
                    datos.set("saldoValuadoPesosFormateado", "0");
                    datos.set("variacion", FormateadorOB.importe(BigDecimal.ZERO));
                    datos.set("tipoCotizacion", "SC");
                    datos.set("monedaDescripcion", "");
                    datos.set("monedaSimbolo", "");
                    tenencias.add(datos);
                    count++;
                    cantidadTitulosValoresPesos++;
                    continue;
                }
            }

            if (productoOperable != null && !"PESOS".equalsIgnoreCase(productoOperable.descMoneda)) {
                if (productosOperablesbyProductoPesos == null) {
                    productosOperablesbyProductoPesos = obtenerProductosOperablesMapByProductoPesos(contexto, fecha);
                }
                productoOperable = productosOperablesbyProductoPesos.get(codigo);
            }

            BigDecimal precio = BigDecimal.ZERO;
            Objeto cotizacionFallback = null;

            try {
                if (posicion.precioConsulta != null && !posicion.precioConsulta.trim().isEmpty()) {
                    precio = new BigDecimal(posicion.precioConsulta);
                }
            } catch (NumberFormatException e) {
                precio = BigDecimal.ZERO;
            }

            BigDecimal variacion = BigDecimal.ZERO;
            try {
                if (posicion.variacion != null && !posicion.variacion.isEmpty()) {
                    variacion = new BigDecimal(posicion.variacion);
                }
            } catch (NumberFormatException e) {
                variacion = BigDecimal.ZERO;
            }

            String monedaDescripcion = (productoOperable != null) ? productoOperable.descMoneda : "PESOS";
            String monedaSimbolo = "PESOS".equals(monedaDescripcion) ? "$" : monedaDescripcion;
            BigDecimal saldoValuado ;

            if ("usd".equalsIgnoreCase(codigo)) {
                saldoValuado = saldoNominal;
            } else {
                saldoValuado = precio.multiply(saldoNominal);
            }

            datos.set("id", codigo);
            datos.set("descripcion", codigo + " - " + posicion.descripcionTenencia);
            datos.set("tipoActivo", posicion.clasificacion);
            datos.set("cantidadNominal", saldoNominal);
            datos.set("cantidadNominalFormateada", FormateadorOB.importe(saldoNominal).replace(",00", ""));
            datos.set("fecha", posicion.fechaPrecioConsulta);
            datos.set("valorPesos", precio.setScale(4, RoundingMode.HALF_UP));
            datos.set("valorPesosFormateado", FormateadorOB.importeCantDecimales(precio, 4));
            datos.set("monto", saldoValuado);
            datos.set("saldoValuadoPesosFormateado", FormateadorOB.importe(saldoValuado));
            datos.set("variacion", FormateadorOB.importe(variacion));
            datos.set("tipoCotizacion", precio.signum() != 0 ? "BYMA" : "SC");
            datos.set("monedaDescripcion", monedaDescripcion);
            datos.set("monedaSimbolo", monedaSimbolo);
            tenencias.add(datos);

            cantidadTitulosValoresPesos++;

            if ("moneda".equalsIgnoreCase(posicion.clasificacion)) {
                if ("usd".equalsIgnoreCase(codigo)) {
                    totalSaldoDolares = totalSaldoDolares.add(posicion.saldoDisponible);
                } else {
                    totalSaldoPesos = totalSaldoPesos.add(saldoValuado);
                }
            } else {
                totalFondosPesos = totalFondosPesos.add(saldoValuado);
            }

            count++;
        }

        Objeto resp = new Objeto();
        resp.set("totalSaldoPesos", totalSaldoPesos);
        resp.set("totalSaldoDolares", totalSaldoDolares);
        resp.set("totalFondosPesos", totalFondosPesos);
        resp.set("totalFondosPesosFormateado", FormateadorOB.importe(totalFondosPesos));
        resp.set("cantidadTitulosValoresPesos", cantidadTitulosValoresPesos);
        resp.set("tenencias", tenencias);

        return respuesta("datos", resp);
    }

    private static Objeto buscarPrecioCotizacion(ContextoOB contexto, ProductosOperablesOrdenados productoOperable, String idVencimiento) {
			BigDecimal precio = BigDecimal.ZERO;
			BigDecimal precioAnterior = BigDecimal.ZERO;
			BigDecimal variacion = BigDecimal.ZERO;
			boolean esByma = false;
			boolean buscarPrecioReferencia = productoOperable!=null?true:false;
			Fecha fecha = null;
			String tipoCotizacion = null;
			Objeto realTime=null;
			Objeto referencia=null;
			IntraDiarias indicesRealTime=null;
			if(productoOperable!=null) {
				indicesRealTime = ApiInversiones.intraDiarias(contexto, null, productoOperable.codigo, idVencimiento).get();
			}
			
			if (indicesRealTime!=null && indicesRealTime.size()>0 && indicesRealTime.codigoHttp() != 204) {
			
				//IntraDiarias.IntraDiaria primerIntraDiaria = indicesRealTime.get(0);
				int ultimaPosicion = indicesRealTime.size() - 1;
				IntraDiarias.IntraDiaria ultimoIntraDiaria = indicesRealTime.get(ultimaPosicion);
				realTime = ultimoIntraDiaria.objeto();
				if (realTime != null) {
					//precio = realTime.bigDecimal("previousClose");
					precio = realTime.bigDecimal("trade");
					if (precio.signum() != 0) {
						if (Arrays.asList("Titulo Publico", "CHA").contains(productoOperable.clasificacion)) {
							precio = precio.divide(new BigDecimal(100));
						}
						variacion = realTime.bigDecimal("imbalance");
						fecha = realTime.fecha("fechaModificacion", "yyyy-MM-dd hh:mm:ss");
						tipoCotizacion = "BYMA";
						esByma = true;
						buscarPrecioReferencia = false;
					}
				}
			}
			if (buscarPrecioReferencia) {
				Fecha hoy = Fecha.ahora();
				
				TitulosPrecio precioReferencia = ApiInversiones.titulosPrecio(contexto, hoy, productoOperable.descMoneda, productoOperable.codigo).get();
				if (precioReferencia.codigoHttp()!=204 && precioReferencia.precioReferencia!=null) {
					precio = precioReferencia.precioReferencia;
					fecha = precioReferencia.fechaPrecio;
				}
			}
			Objeto objeto = new Objeto();
			objeto.set("esByma", esByma);
			objeto.set("precio", precio);
			objeto.set("precioAnterior", precioAnterior);
			objeto.set("variacion", variacion);
			objeto.set("fecha", fecha != null ? fecha.dia()+"/"+fecha.mes()+"/"+fecha.año() : "");
			objeto.set("tipoCotizacion", tipoCotizacion);
			return objeto;
		}
	    
	    
	    private static Objeto buscarPrecioCotizacion(ContextoOB contexto, Objeto productoOperable, String idVencimiento) {
			BigDecimal precio = BigDecimal.ZERO;
			BigDecimal precioAnterior = BigDecimal.ZERO;
			BigDecimal variacion = BigDecimal.ZERO;
			boolean esByma = false;
			boolean buscarPrecioReferencia = productoOperable!=null?true:false;
			Fecha fecha = null;
			String tipoCotizacion = null;
			Objeto realTime=null;
			Objeto referencia=null;
			IntraDiarias indicesRealTime=null;
			if(productoOperable!=null) {
				indicesRealTime = ApiInversiones.intraDiarias(contexto, null, productoOperable.get("codigo").toString(), idVencimiento).get();
			}
			if (indicesRealTime!=null && indicesRealTime.size()>0 && indicesRealTime.codigoHttp() != 204) {
			
				//IntraDiarias.IntraDiaria primerIntraDiaria = indicesRealTime.get(0);
				int ultimaPosicion = indicesRealTime.size() - 1;
				IntraDiarias.IntraDiaria ultimoIntraDiaria = indicesRealTime.get(ultimaPosicion);
				realTime = ultimoIntraDiaria.objeto();
				if (realTime != null) {
					//precio = realTime.bigDecimal("previousClose");
					precio = realTime.bigDecimal("trade");
					if (precio.signum() != 0) {
						if (Arrays.asList("Titulo Publico", "CHA").contains(productoOperable.get("tipoCotizacion").toString())) {
							precio = precio.divide(new BigDecimal(100));
						}
						variacion = realTime.bigDecimal("imbalance");
						fecha = realTime.fecha("fechaModificacion", "yyyy-MM-dd hh:mm:ss");
						tipoCotizacion = "BYMA";
						esByma = true;
						buscarPrecioReferencia = false;
					}
				}
			}
			if (buscarPrecioReferencia) {
				Fecha hoy = Fecha.ahora();
				
				TitulosPrecio precioReferencia = ApiInversiones.titulosPrecio(contexto, hoy, productoOperable.get("descMoneda").toString(), productoOperable.get("codigo").toString()).tryGet();
				if (precioReferencia!= null && precioReferencia.precioReferencia!=null && precioReferencia.codigoHttp()!=204) {
					precio = precioReferencia.precioReferencia;
					fecha = precioReferencia.fechaPrecio;
				}
			}
			Objeto objeto = new Objeto();
			objeto.set("esByma", esByma);
			objeto.set("precio", precio);
			objeto.set("precioAnterior", precioAnterior);
			objeto.set("variacion", variacion);
            objeto.set("fecha", fecha != null ? fecha.dia()+"/"+fecha.mes()+"/"+fecha.año() : "");
            objeto.set("tipoCotizacion", tipoCotizacion);
			return objeto;
		}
	    
	    
	    private static Object buscarPosicionesNegociables(ContextoOB contexto, String cuentaComitente, String fecha) {
			String fechaActual = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			Respuesta respuesta = new Respuesta();
			Fecha fe = Fecha.ahora();
			//f.hoy();
			if (!fecha.isEmpty()) {
				fechaActual = fecha;
			}

			CuentaComitentesLicitacionesOB posicionesNegociables = ApiInversiones.cuentaComitentesLicitacionesOB(contexto, contexto.sesion().empresaOB.idCobis, cuentaComitente,  fe, "1000", "1").get();


			if (!posicionesNegociables.isEmpty()) {
				if (posicionesNegociables.codigoHttp() == 204 || posicionesNegociables.codigoHttp() == 404) {
					return respuesta("estado", "SIN_TENENCIA");
				}
			} else {
				if (posicionesNegociables.codigoHttp() == 504 || posicionesNegociables.codigoHttp() == 500) {
					return respuesta("estado", "OPERA_MANUAL");
				}

				return respuesta("estado","SIN_TENENCIA");
			}
			return posicionesNegociables;
		}
	    
		public static Map<String, ProductosOperablesOrdenados> obtenerProductosOperablesMapByCodigo(ContextoOB contexto, String fecha) {
			    Map<String, TitulosProducto.ProductosOperablesOrdenados> productosOperables = new HashMap<>();
			    Fecha fe = Fecha.ahora();
			    TitulosProducto titulosValores = ApiInversiones.titulosProducto(contexto, "1", "1000", fe).get();
			    for (TitulosProducto.ProductosOperablesOrdenados objeto : titulosValores.productosOperablesOrdenados) {
			        productosOperables.put(objeto.codigo, objeto);
			    }
			return productosOperables;
		   }
		
			public static Map<String, ProductosOperablesOrdenados> obtenerProductosOperablesMapByProducto(ContextoOB contexto, String fecha) {
			    Map<String, TitulosProducto.ProductosOperablesOrdenados> productosOperables = new HashMap<>();
			    Fecha fe = Fecha.ahora();
			    TitulosProducto titulosValores = ApiInversiones.titulosProducto(contexto, "1", "1000", fe).get();
			    for (TitulosProducto.ProductosOperablesOrdenados objeto : titulosValores.productosOperablesOrdenados) {
			        productosOperables.put(objeto.producto, objeto);
			    }
			return productosOperables;
	       }
			
			public static Map<String, ProductosOperablesOrdenados> obtenerProductosOperablesMapByProductoPesos(ContextoOB contexto, String fecha) {
				 Map<String, TitulosProducto.ProductosOperablesOrdenados> productosOperables = new HashMap<>();
				 Fecha fe = Fecha.ahora();
				 TitulosProducto titulosValores = ApiInversiones.titulosProducto(contexto, "1", "1000", fe).get();
				for (TitulosProducto.ProductosOperablesOrdenados objeto : titulosValores.productosOperablesOrdenados) {
					if (objeto.descMoneda.equalsIgnoreCase("PESOS")) {
						productosOperables.put(objeto.producto, objeto);
					}
				}
				return productosOperables;
			}


    public static Object historialComitente(ContextoOB contexto) {
        String cuentaComitente = contexto.parametros.string("cuentaComitente");
        Fecha fechaDesde = contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd", null);
        Fecha fechaHasta = contexto.parametros.fecha("fechaHasta", "yyyy-MM-dd", null);
        Boolean previsualizacion = contexto.parametros.bool("previsualizacion");

        Objeto respuesta = new Objeto();
        SesionOB sesion = contexto.sesion();
        Integer cantidad = 10;
        int count = 0;

        if (empty(sesion.empresaOB)) {
            return respuesta("EMPRESA_INVALIDA");
        }

        final Integer agrupado = 1;

        int secuencial = 1;
        boolean iteracion;


        // 1) Pedimos la primera página (WRAPPER)
        ExtractoComitenteOB wrapper = ApiInversiones.extractoComitenteOB(
                contexto, cuentaComitente, fechaDesde, fechaHasta,
                sesion.empresaOB.idCobis.toString(),
                String.valueOf(secuencial),
                String.valueOf(cantidad),
                agrupado).tryGet();

        // 2) Desempaquetamos el contenido real
        ExtractoComitenteOB.ExtractoComitente extractoCtte = (wrapper != null) ? wrapper.extractoComitente : null;


        if (wrapper == null || wrapper.codigoHttp() == 404 || extractoCtte == null || extractoCtte.operaciones == null) {
            return respuesta("SIN_MOVIMIENTOS");
        }

        List<ExtractoComitenteOB.ExtractoComitente.Operaciones> ops = new ArrayList<>();
        if (extractoCtte.operaciones != null) ops.addAll(extractoCtte.operaciones);

        iteracion = extractoCtte.paginacion != null
                && "SI".equalsIgnoreCase(extractoCtte.paginacion.existenMasRegistrosPaginacion);

        while (iteracion) {
            iteracion = false;

            String ultimo = (extractoCtte.paginacion != null) ? extractoCtte.paginacion.ultimoSecuencialPaginacion : null;
            if (ultimo != null && !ultimo.isBlank()) {
                try {
                    secuencial = Integer.parseInt(ultimo) + 1;
                } catch (NumberFormatException nfe) {
                    secuencial += cantidad;
                }
            } else {
                secuencial += cantidad;
            }

            // 3) Pedimos la siguiente página (WRAPPER) y volvemos a desempaquetar
            ExtractoComitenteOB nextWrapper = ApiInversiones.extractoComitenteOB(
                    contexto, cuentaComitente, fechaDesde, fechaHasta,
                    sesion.empresaOB.idCobis.toString(),
                    String.valueOf(secuencial),
                    String.valueOf(cantidad),
                    agrupado).tryGet();

            ExtractoComitenteOB.ExtractoComitente next = (nextWrapper != null) ? nextWrapper.extractoComitente : null;

            if (next != null && next.operaciones != null) {
                ops.addAll(next.operaciones);
                iteracion = next.paginacion != null
                        && "SI".equalsIgnoreCase(next.paginacion.existenMasRegistrosPaginacion);
                extractoCtte = next;
                wrapper = nextWrapper;
            }
        }

        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        ops.sort(
                java.util.Comparator
                        .comparing(
                                (ExtractoComitenteOB.ExtractoComitente.Operaciones e) -> {
                                    try {
                                        return java.time.LocalDate.parse(
                                                e.fechaConcertacion == null ? "" : e.fechaConcertacion.trim(), fmt
                                        );
                                    } catch (Exception ex) {
                                        return java.time.LocalDate.MIN; // si viene null o mal formada
                                    }
                                }
                        )
                        .reversed() // más reciente primero
        );




        for (ExtractoComitenteOB.ExtractoComitente.Operaciones e : ops) {
            Objeto datos = new Objeto();

            String tipOP = extraerTipoOp(e.tipo);

            String codEsp = (e.especie != null ? e.especie.codigo : "");
            String descEsp = (e.especie != null ? e.especie.descripcion : "");

            datos.set("numeroOrdenInterno", e.numeroOrdenInterno);
            datos.set("idOperacion", e.idOperacion);

            datos.set("tipo", e.tipo.toLowerCase());
            datos.set("tipoOperacion", e.tipo);
            datos.set("tipOP", tipOP);
            datos.set("codigo", codEsp);

            datos.set("codDescripcion", codEsp + " - " + descEsp);
            datos.set("descripcion", tipOP + descEsp);

            datos.set("fechaConcertacion", e.fechaConcertacion);
            datos.set("fechaConsulta", Fecha.ahora());

            datos.set("monto", e.bruto);
            datos.set("moneda", e.moneda);

            datos.set("cuentaCustodia", e.cuentaCustodia);
            datos.set("cantidadNominal", e.cantidadNominal);

            datos.set("cuentaLiquidacionML", e.cuentaLiquidacionML);
            datos.set("cuentaLiquidacionME", e.cuentaLiquidacionME);

            datos.set("precio", e.precio);
            datos.set("plazo", e.plazo);
            datos.set("comision", e.comision);
            datos.set("cuentaComitente", cuentaComitente);

            datos.set("fechaOrden", e.fechaOrden);
            datos.set("fechaPago", e.fechaPago);
            datos.set("fechaVencimiento", e.fechaVencimiento);

            datos.set("aranceles", e.aranceles);
            datos.set("totalLetrasML", e.totalLetrasML);
            datos.set("totalLetrasME", e.totalLetrasME);

            datos.set("valorResidual", e.valorResidual);

            datos.set("agenteLiquidacionYCompensacion", extractoCtte.agenteLiquidacionYCompensacion);
            datos.set("depositanteCajaDeValores",       extractoCtte.depositanteCajaDeValores);
            datos.set("numeroAgenteMercado",            extractoCtte.numeroAgenteMercado);

            datos.set("ordenSecuencial", e.ordenSecuencial);
            datos.set("numeroMinuta", e.numeroMinuta);
            datos.set("numeroBoleto", e.numeroBoleto);
            datos.set("unidadPlazo", e.unidadPlazo);
            datos.set("horaOrden", e.horaOrden);
            datos.set("contraparte", e.contraparte);
            datos.set("mercado", e.mercado);
            datos.set("totalML", e.totalML);
            datos.set("totalME", e.totalME);
            datos.set("derechos", e.derechos);

            datos.set("amortizacion", e.amortizacion);
            datos.set("renta", e.renta);
            datos.set("amortizacionME", e.amortizacionME);
            datos.set("amortizacionML", e.amortizacionML);
            datos.set("rentaML", e.rentaML);
            datos.set("rentaME", e.rentaME);
            datos.set("cantidadResidualActual", e.cantidadResidualActual);

            datos.set("cantidadResidual", (e.cantidadResidual));

            respuesta.add(datos);
            count++;
            if (previsualizacion != null && previsualizacion && count >= 5) {
                return respuesta("datos", respuesta);
            }
        }

        return respuesta("datos", respuesta);
    }

    public static Object detalleTenenciaEspecies(ContextoOB contexto) {
        String idTenencia = contexto.parametros.string("idTenencia");
        String cuentaComitente = contexto.parametros.string("cuentaComitente");
        Fecha fe = Fecha.ahora();
        LocalDate hoy = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String fecha = hoy.format(formatter);
        SesionOB sesion = contexto.sesion();
        String cantidad = "1000";

        try {
            CuentaComitenteLicitacion posicionNegociable = ApiInversiones
                    .cuentaComitentesLicitacionesV2OB(contexto, sesion.empresaOB.idCobis.toString(), cuentaComitente, fe, cantidad, "1")
                    .get().posicionesNegociablesOrdenadas.stream()
                    .filter(a -> a.codigo.equals(idTenencia))
                    .findFirst()
                    .orElse(null);

            if (posicionNegociable == null) {
                return respuesta("DATOS_INVALIDOS");
            }

            Map<String, ProductosOperablesOrdenados> productosOperables = obtenerProductosOperablesMapByCodigo(contexto, fecha);
            Map<String, ProductosOperablesOrdenados> productosOperablesbyProducto = null;
            ProductosOperablesOrdenados productoOperable = productosOperables.get(posicionNegociable.codigo);

            if (productoOperable == null) {
                if (productosOperablesbyProducto == null) {
                    productosOperablesbyProducto = obtenerProductosOperablesMapByProducto(contexto, fecha);
                }
                productoOperable = productosOperablesbyProducto.get(posicionNegociable.codigo);
            }

            BigDecimal precio = BigDecimal.ZERO;
            try {
                if (posicionNegociable.precioConsulta != null && !posicionNegociable.precioConsulta.trim().isEmpty()) {
                    precio = new BigDecimal(posicionNegociable.precioConsulta);
                }
            } catch (NumberFormatException e) {
                precio = BigDecimal.ZERO;
            }

            BigDecimal saldoNominal = posicionNegociable.saldoDisponible;
            BigDecimal saldoValuado = precio.multiply(saldoNominal);
            String monedaDescripcion = (productoOperable != null) ? productoOperable.descMoneda : "PESOS";
            String monedaSimbolo = "PESOS".equals(monedaDescripcion) ? "$" : monedaDescripcion;

            BigDecimal variacion = BigDecimal.ZERO;
            try {
                if (posicionNegociable.variacion != null && !posicionNegociable.variacion.isEmpty()) {
                    variacion = new BigDecimal(posicionNegociable.variacion);
                }
            } catch (NumberFormatException e) {
                variacion = BigDecimal.ZERO;
            }

            Objeto detalle = new Objeto();
            detalle.set("tipoActivo", posicionNegociable.clasificacion);
            detalle.set("moneda", monedaDescripcion);
            detalle.set("monto", saldoValuado);
            detalle.set("descripcion", posicionNegociable.codigo + " - " + posicionNegociable.descripcionTenencia);
            detalle.set("cantidadNominal", saldoNominal);
            detalle.set("variacion", FormateadorOB.importe(variacion));
            detalle.set("valorEstimado", precio);
            detalle.set("fecha", posicionNegociable.fechaPrecioConsulta);

            return respuesta("datos", detalle);
        } catch (Exception ex) {
            if ("No value present".equals(ex.getMessage())) {
                return respuesta("DATOS_INVALIDOS");
            } else {
                return respuesta("ERROR");
            }
        }
    }

    public static Object detalleComitente(ContextoOB contexto) {
        String idOperacion = contexto.parametros.string("idOperacion");
        String cuentaComitente = contexto.parametros.string("cuentaComitente");
        String tipoOperacionFiltro = contexto.parametros.string("tipoOperacion", null); // (aún sin uso)
        SesionOB sesion = contexto.sesion();
        String cantidad = "1000";

        Fecha fechaHasta = Fecha.ahora();
        Fecha fechaDesde = fechaHasta.restarMeses(48);
        final Integer agrupado = 1;

        try {
            if (empty(sesion.empresaOB)) {
                return respuesta("EMPRESA_INVALIDA");
            }

            // 1) Pedimos página (WRAPPER) y desempaquetamos
            ExtractoComitenteOB wrapper = ApiInversiones.extractoComitenteOB(
                    contexto, cuentaComitente, fechaDesde, fechaHasta,
                    sesion.empresaOB.idCobis.toString(), "1", cantidad, agrupado
            ).tryGet();

            ExtractoComitenteOB.ExtractoComitente body =
                    (wrapper != null) ? wrapper.extractoComitente : null;

            if (body == null || body.operaciones == null || body.operaciones.isEmpty()) {
                return respuesta("DATOS_INVALIDOS");
            }

            // 2) Buscamos la operación sin lanzar NoSuchElementException
            ExtractoComitenteOB.ExtractoComitente.Operaciones operacion =
                    body.operaciones.stream()
                            .filter(op -> idOperacion != null && idOperacion.equals(op.idOperacion))
                            .findFirst()
                            .orElse(null);

            if (operacion == null) {
                return respuesta("DATOS_INVALIDOS");
            }

            // 3) Moneda robusta (evita NPE al hacer contains sobre null)
            String moneda =
                    (operacion.moneda != null && !operacion.moneda.isBlank()) ? operacion.moneda :
                            (operacion.cuentaLiquidacionME != null && operacion.cuentaLiquidacionME.contains("USD")) ? "USD" :
                                    (operacion.cuentaLiquidacionML != null ? "$" : (operacion.moneda != null ? operacion.moneda : "$"));

            // 4) Armar respuesta (con null-safety en especie/descripcion)
            Objeto detalle = new Objeto();
            detalle.set("tipoOperacion", operacion.tipo);
            detalle.set("moneda", moneda);
            detalle.set("monto", operacion.bruto);
            detalle.set("fecha", operacion.fechaConcertacion);
            detalle.set("idOperacion", operacion.idOperacion);
            detalle.set("descripcion", operacion.especie != null ? operacion.especie.descripcion : "");
            detalle.set("cantidadNominal", operacion.cantidadNominal);
            detalle.set("plazo", operacion.plazo);
            detalle.set("comision", operacion.comision);
            detalle.set("cuentaComitente", cuentaComitente);

            return respuesta("datos", detalle);

        } catch (Exception ex) {
            // Ya no dependemos del "No value present", pero lo dejamos por compatibilidad.
            if ("No value present".equals(ex.getMessage())) {
                return respuesta("DATOS_INVALIDOS");
            }
            return respuesta("ERROR");
        }
    }

    public static String extraerTipoOp(String tipoOp) {
        if (tipoOp == null) return "";

        String resp;
        switch (tipoOp) {
            case "Compra de Especies":
                resp = "Compra ";
                break;
            case "Venta de Especie":
                resp = "Venta ";
                break;
            case "Liquidación de Cupón":
                resp = "Liquidación ";
                break;
            case "Dividendos en Acciones":
                resp = "Dividendo en acciones ";
                break;
            case "Dividendos en Efectivo":
                resp = "Dividendo en efectivo ";
                break;
            case "Ingreso de Especies":
                resp = "Ingreso ";
                break;
            case "Egreso de Especies":
                resp = "Egreso ";
                break;
            default:
                resp = "";
        }
        return resp;
    }


		    public static Object extractoTenencias(ContextoOB contexto) {
    	    	String idCuentaComitente = contexto.parametros.string("cuentaComitente");
    	    	Objeto extracto = new Objeto();  
    	    	Objeto datosGenerales = new Objeto();
    	    	Objeto tenencias = new Objeto();
    	        SesionOB sesion = contexto.sesion();
    	        if (empty(sesion.empresaOB)) {
    	            return respuesta("EMPRESA_INVALIDA");
    	        }

    	         CuentaComitenteEspeciesOB posicionComitente = ApiInversiones.cuentaComitenteEspecieOB(contexto, sesion.empresaOB.idCobis, idCuentaComitente).get();


    	         if (empty(posicionComitente)) {
    	            return respuesta("SIN_POSICION");
    	        }
    	     
    	         //datos generales
    	         datosGenerales.set("cuentaComitente", idCuentaComitente);
    	         datosGenerales.set("fechaConsulta", Fecha.ahora());
    	         datosGenerales.set("razonSocial", sesion.empresaOB.razonSocial);   
    	         tenencias.set("datosGenerales", datosGenerales);
    	         //listado de tenencias
    	     for (CuentaComitenteEspecie cuentaComitenteEspecie : posicionComitente) {
    	    	 Objeto datos = new Objeto();   
       	    	  datos.set("entidadCustodia", cuentaComitenteEspecie.entidadCustodia);
    	  	      datos.set("tipoProducto", cuentaComitenteEspecie.tipoProducto);
    	  	      datos.set("especie", cuentaComitenteEspecie.codigoEspecie);
       	  	      datos.set("saldoFisico", cuentaComitenteEspecie.saldoDisponibleNominal);
           	  	  datos.set("saldoFisicoR", cuentaComitenteEspecie.saldoDisponibleResidual);
       	  	      datos.set("saldoBloqueado", cuentaComitenteEspecie.saldoBloqueadoNominal);
           	  	  datos.set("saldoBloqueadoR", cuentaComitenteEspecie.saldoBloqueadoResidual);
    	  	        extracto.add(datos);
			      }

    	     	tenencias.set("tenencias", extracto);
  
    	        return respuesta("datos", tenencias);
    	    }
		    
		    public static Respuesta obtenerTokenVFNet(ContextoOB contexto) {
		    	SesionOB sesion = contexto.sesion();
		    	TokenFVNetOB obtenerTokenVFNet = ApiInversiones.obtenerTokenVFNet(contexto, sesion.empresaOB.idCobis).tryGet();
	
				
				if (obtenerTokenVFNet==null ||  obtenerTokenVFNet.objeto().get("token")==null) {
					return Respuesta.error();
				}
		    	

				Respuesta respuesta = new Respuesta();
				respuesta.set("url", ConfigHB.string("vfhome_url"));
				respuesta.set("token", obtenerTokenVFNet.objeto().get("token"));
				return respuesta;
		    }

    private static String nfc(String s) {
        if (s == null) return null;
        s = s.replace('\u00A0', ' '); // no-break space → espacio normal
        return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFC);
    }


    public static Object descargarHistorialComitente(ContextoOB contexto) {
        Object raw = contexto.parametros.get("data");
        List<Objeto> operaciones;

        if (raw instanceof String) {
            Objeto parsed = Objeto.fromJson((String) raw);
            operaciones = parsed.objetos();
        } else {
            operaciones = contexto.parametros.objetos("data");
        }

        if (operaciones == null || operaciones.isEmpty()) {
            return respuesta("SIN_MOVIMIENTOS");
        }

        ArrayList<ReporteHistorialComitenteCSVDTO> filas = new ArrayList<>();

        for (int i = operaciones.size() - 1; i >= 0; i--) {
            Objeto op = operaciones.get(i);

            String fechaConcertacion = nfc(op.string("fechaConcertacion"));
            String tipoOrden         = nfc(op.string("tipoOperacion"));
            String codigo            = nfc(op.string("codigo"));
            String vr                = nfc(op.string("valorResidual"));


            // --- Moneda y cuenta liquidadora según moneda ---
            String monedaRaw = op.string("moneda");
            boolean esPesos = false;
            boolean esDolares = false;
            if (monedaRaw != null) {
                String m = monedaRaw.toUpperCase();
                esPesos   = m.contains("PESO") || m.contains("$");
                esDolares = m.contains("DOLAR") || m.contains("USD") || m.contains("U$S");
            }
            if (!esPesos && !esDolares) {
                BigDecimal totalME = op.bigDecimal("totalME");
                esDolares = totalME != null && totalME.compareTo(BigDecimal.ZERO) > 0;
                esPesos = !esDolares;
            }

            //  cuenta liquidadora según moneda
            String cuentaLiquidadora = esPesos ? op.string("cuentaLiquidacionML")
                    : op.string("cuentaLiquidacionME");

            BigDecimal montoCampo = op.bigDecimal("monto");
            if (montoCampo == null) montoCampo = BigDecimal.ZERO;
            montoCampo = montoCampo.setScale(2, RoundingMode.HALF_UP);

            BigDecimal montoPesos   = esPesos   ? montoCampo : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            BigDecimal montoDolares = esDolares ? montoCampo : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

            BigDecimal cantidadNominal = op.bigDecimal("cantidadNominal");
            if (cantidadNominal == null) cantidadNominal = BigDecimal.ZERO;
            cantidadNominal = cantidadNominal.setScale(0, RoundingMode.HALF_UP);


            BigDecimal precioUnit = op.bigDecimal("precio");
            if (precioUnit == null || precioUnit.compareTo(BigDecimal.ZERO) == 0) {
                precioUnit = BigDecimal.ZERO;
            }
            precioUnit = precioUnit.setScale(4, RoundingMode.HALF_UP);

            filas.add(new ReporteHistorialComitenteCSVDTO(
                    fechaConcertacion,
                    tipoOrden,
                    codigo,
                    cuentaLiquidadora,
                    vr,
                    cantidadNominal,
                    precioUnit,
                    montoPesos,
                    montoDolares
            ));
        }

        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            stream.write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});

            OutputStreamWriter streamWriter =
                    new OutputStreamWriter(stream, java.nio.charset.StandardCharsets.UTF_8);

            CSVWriter writer = new CSVWriter(
                    streamWriter, ';', CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END
            );

            var mappingStrategy = new OBManejoArchivos.CustomColumnPositionStrategy<ReporteHistorialComitenteCSVDTO>();
            mappingStrategy.setType(ReporteHistorialComitenteCSVDTO.class);

            StatefulBeanToCsv<ReporteHistorialComitenteCSVDTO> builder = new StatefulBeanToCsvBuilder<ReporteHistorialComitenteCSVDTO>(writer)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .withSeparator(';')
                    .withMappingStrategy(mappingStrategy)
                    .build();

            builder.write(filas);
            streamWriter.flush();
            writer.close();

            byte[] file = stream.toByteArray();
            contexto.response.header("Content-Disposition",
                    "attachment; filename=historial-comitente-" + Fecha.hoy() + ".csv"
            );
            contexto.response.type("text/csv; charset=utf-8");
            return file;

        } catch (Exception e) {
            return respuesta("ERROR", "descripcion", "No se pudo descargar el archivo.");
        }
    }
    private static final java.util.Locale LOCALE_AR = new java.util.Locale("es","AR");
    private static final ThreadLocal<java.text.DecimalFormat> DF_ES_AR_2 =
            ThreadLocal.withInitial(() -> {
                var syms = new java.text.DecimalFormatSymbols(LOCALE_AR);
                syms.setDecimalSeparator(',');
                syms.setGroupingSeparator('.');
                var df = new java.text.DecimalFormat("#,##0.00", syms);
                df.setRoundingMode(java.math.RoundingMode.HALF_UP);
                return df;
            });

    private static final ThreadLocal<DecimalFormat> DF_ES_AR_4 =
            ThreadLocal.withInitial(() -> {
                DecimalFormatSymbols sym = new DecimalFormatSymbols(new Locale("es","AR"));
                sym.setDecimalSeparator(',');
                sym.setGroupingSeparator('.');
                DecimalFormat df = new DecimalFormat("#,##0.0000", sym);
                df.setRoundingMode(RoundingMode.HALF_UP);
                return df;
            });

    private static String fmt4(java.math.BigDecimal n) {
        if (n == null) n = java.math.BigDecimal.ZERO;
        return DF_ES_AR_4.get().format(n);
    }

    private static BigDecimal toBigDecimalSafe(String s) {
        if (s == null) return BigDecimal.ZERO;
        s = s.trim();
        if (s.isEmpty()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(s);
        } catch (Exception ignore) {
            // soporte "1.234,56" → "1234.56"
            try {
                String normalized = s.replace(".", "").replace(",", ".");
                return new BigDecimal(normalized);
            } catch (Exception e) {
                return BigDecimal.ZERO;
            }
        }
    }


    private static String toScale4(String s) {
        if (s == null || s.trim().isEmpty()) return "0.0000";
        try {
            s = s.replace(',', '.');
            return new java.math.BigDecimal(s)
                    .setScale(4, java.math.RoundingMode.HALF_UP)
                    .toPlainString();
        } catch (Exception e) {
            return "0.0000";
        }
    }

    private static String formatImporte2(String raw) {
        java.math.BigDecimal bd = toBigDecimalSafe(raw);
        bd = bd.setScale(2, java.math.RoundingMode.HALF_UP);
        return fmt2(bd);
    }



    private static String fmt2(java.math.BigDecimal n) {
        if (n == null) return "0,00";
        return DF_ES_AR_2.get().format(n);
    }

    private static String normalizarMoneda(String monedaRaw) {
        if (monedaRaw == null) return "";
        String m = monedaRaw.trim().toUpperCase();

        // quitar tildes (por si viniera "DÓLAR")
        m = java.text.Normalizer.normalize(m, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");

        if (m.contains("ARS") || m.contains("PESO"))   return "PESOS";
        if (m.contains("USD") || m.contains("DOLAR"))  return "DOLAR";
        if (m.contains("EUR") || m.contains("EURO"))   return "EUR";
        if (m.contains("UVA"))                         return "UVA";

        int coma = m.indexOf(',');
        if (coma > -1) m = m.substring(0, coma).trim();

        return m.isEmpty() ? "" : m;
    }

    private static String p(ContextoOB ctx, String key) {
        try{
            String v = ctx.parametros.string(key);
            if (v == null) return "";
            v = v.trim();
            return "null".equalsIgnoreCase(v) ? "" : v;
        } catch (ParametrosIncorrectosException e) {
            return "";
        }
    }
    private static boolean esMonedaPesos(String moneda) {
        if (moneda == null) return false;
        String m = moneda.trim().toUpperCase();
        return m.contains("PESO") || m.contains("ARS");
    }

    public static byte[] comprobanteMovimientos(ContextoOB contexto) {
        Map<String, String> parametros = new HashMap<>();
        if (contexto.sesion().empresaOB.cuit == null) {
            return null;
        }


        parametros.put("AGENTE_LIQUIDACION", p(contexto, "nroRegistro"));
        parametros.put("DEPOSITANTE_CAJA", p(contexto, "cajaValores"));
        parametros.put("AGENTE_MERCADO", p(contexto, "agenteMercado"));
        parametros.put("NUMERO_ORDEN", p(contexto, "nroOrden"));
        parametros.put("FECHA_ORDEN", p(contexto, "fechaOrden"));
        parametros.put("CUENTA_COMITENTE", p(contexto, "comitente"));
        parametros.put("RAZON_SOCIAL", contexto.sesion().empresaOB.razonSocial);
        parametros.put("NUMERO_CUIT", contexto.sesion().empresaOB.cuit.toString());
        parametros.put("TIPO_OPERACION", p(contexto, "tipoOperacion"));
        parametros.put("FECHA_LIQUIDACION", p(contexto, "fechaLiquidacion"));
        parametros.put("PLAZO_OP", p(contexto, "plazo"));
        parametros.put("ESPECIE_OP", p(contexto, "especie"));
        parametros.put("MERCADO_OP", p(contexto, "mercado"));
        parametros.put("CANT_NOMINAL", p(contexto, "cantidadNominal"));
        parametros.put("CANT_RESIDUAL", p(contexto, "cantidadResidual"));
        parametros.put("PRECIO_OP", p(contexto, "precio"));
        parametros.put("IMPORTE_OP", formatImporte2(p(contexto, "importe")));
        parametros.put("ARANCELES_OP", p(contexto, "aranceles"));
        parametros.put("DERECHOS_OP", p(contexto, "derechos"));
        parametros.put("COMISIONES_OP", p(contexto, "comisiones"));
        parametros.put("TOTAL_PESOS", p(contexto, "totalPesos"));
        parametros.put("TOTAL_ME", p(contexto, "totalDolares"));
        parametros.put("CUENTA_PESOS", p(contexto, "cuentaLiquidacionML"));
        parametros.put("CUENTA_DOLAR", p(contexto, "cuentaLiquidacionME"));
        parametros.put("TIPO_LEYENDA", p(contexto, "leyenda"));


        String tipoOp     = p(contexto, "tipoOperacion");
        String monedaRaw  = p(contexto, "moneda");
        String monedaNorm = normalizarMoneda(monedaRaw);
        boolean esPesos   = esMonedaPesos(monedaNorm);

        parametros.put("MONEDA_OP",  monedaNorm);


        if (tipoOp.trim().toLowerCase().startsWith("liquidación")) {
            parametros.put("TENENCIA_NOMINAL_OP", p(contexto,"cantidadNominal"));
            parametros.put("AMORTIZACION_POR_OP", p(contexto, "amortizacion"));
            parametros.put("REENTA_POR_OP", fmt4(toBigDecimalSafe(p(contexto, "renta"))));


            //parametros.put("TENENCIA_RESIDUAL_ANTERIOR",p(contexto, "cantidadResidual"));


            String tenResActFmt = fmt2(contexto.parametros.bigDecimal("cantidadResidualActual"));
            parametros.put("TENENCIA_RESIDUAL_ACTUAL", tenResActFmt);

            String amortizacionSel = esPesos
                    ? p(contexto, "amortizacionML"):p(contexto,"amortizacionME");
            if (amortizacionSel.isEmpty()) amortizacionSel = "0";


            String rentaSel = esPesos
                    ? p(contexto, "rentaML"):p(contexto, "rentaME");
            if (rentaSel.isEmpty()) rentaSel = "0";

            parametros.put("AMORTIZACION_OP", amortizacionSel);
            parametros.put("RENTA_OP", rentaSel);

            BigDecimal tenResActBD = contexto.parametros.bigDecimal("cantidadResidualActual");
            if (tenResActBD == null) tenResActBD = BigDecimal.ZERO;
            BigDecimal amortSelBD = toBigDecimalSafe(amortizacionSel);
            BigDecimal tenResAntBD = tenResActBD.add(amortSelBD);

            parametros.put("TENENCIA_RESIDUAL_ACTUAL",   fmt2(tenResActBD));
            parametros.put("TENENCIA_RESIDUAL_ANTERIOR", fmt2(tenResAntBD));

            contexto.responseHeader("Content-Type", "application/pdf; name=comprobante.pdf");
            contexto.responseHeader("Content-Disposition", "attachment; filename=\"comprobante_boleto_definitivo.pdf\"");


            return ar.com.hipotecario.canal.officebanking.lib.Pdf.generar("comprobante_boleto_definitivo", parametros);

        }else{
            contexto.responseHeader("Content-Type", "application/pdf; name=comprobante.pdf");
            contexto.responseHeader("Content-Disposition", "attachment; filename=\"comprobante_movimiento.pdf\"");
            return ar.com.hipotecario.canal.officebanking.lib.Pdf.generar("comprobante_movimientos", parametros);
        }
    }

    public static Boolean nuevoBoletoHabilitado (ContextoOB contexto) {
    	//Habilitado solo para Angie y Ramiro
    	if(contexto.esProduccion() && (contexto.sesion().empresaOB.cuit.toString().equals("27295443079") ||
                                       contexto.sesion().empresaOB.cuit.toString().equals("20399115168") ||
                                       contexto.sesion().empresaOB.cuit.toString().equals("33541373859")
        )) {
    		return true;
    	} else {
    		return false;
    	}
    	
    }

    public static Boolean habilitaFCIInformes (ContextoOB contexto) {
        //Habilitado solo para Angie y Ramiro
        if(contexto.esProduccion() && (contexto.sesion().empresaOB.cuit.toString().equals("27295443079") ||
                contexto.sesion().empresaOB.cuit.toString().equals("20399115168")
        )) {
            return true;
        } else {
            return false;
        }

    }


    public static Object cuentasPFHabilitadas(ContextoOB contexto) {
        Objeto respuesta = new Objeto();
        String cuit = contexto.sesion().empresaOB.cuit.toString();
        Set<String> cuitsHabilitados = Set.of(
                "30624957942",
                "30686308525",
                "30556649568",
                "27295443079",
                "20399115168"
        );
        boolean habilitado = cuitsHabilitados.contains(cuit);
        respuesta.set("estado", "0");
        respuesta.set("habilitado", habilitado);
        return respuesta;
    }
}
