package ar.com.hipotecario.mobile.api;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.servicio.api.transmit.JourneyTransmitEnum;
import com.fasterxml.jackson.core.JsonProcessingException;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.util.Transmit;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.bm.mb.RescateMBBMBankProcess;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Concurrencia;
import ar.com.hipotecario.mobile.lib.Fecha;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Mock;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.lib.Util;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.CuentaComitente;
import ar.com.hipotecario.mobile.negocio.CuentaTercero;
import ar.com.hipotecario.mobile.negocio.Domicilio;
import ar.com.hipotecario.mobile.negocio.Persona;
import ar.com.hipotecario.mobile.negocio.PlazoFijo;
import ar.com.hipotecario.mobile.negocio.PlazoFijoLogro;
import ar.com.hipotecario.mobile.negocio.Telefono;
import ar.com.hipotecario.mobile.negocio.cuentainversor.Banco;
import ar.com.hipotecario.mobile.negocio.cuentainversor.CuentaInversor;
import ar.com.hipotecario.mobile.negocio.cuentainversor.CuentasLiquidacion;
import ar.com.hipotecario.mobile.negocio.cuentainversor.CuotasBancarias;
import ar.com.hipotecario.mobile.negocio.cuentainversor.DatosUIF;
import ar.com.hipotecario.mobile.negocio.cuentainversor.Dolares;
import ar.com.hipotecario.mobile.negocio.cuentainversor.DomicilioCuentaInversor;
import ar.com.hipotecario.mobile.negocio.cuentainversor.Moneda;
import ar.com.hipotecario.mobile.negocio.cuentainversor.Origen;
import ar.com.hipotecario.mobile.negocio.cuentainversor.Pesos;
import ar.com.hipotecario.mobile.negocio.cuentainversor.Radicacion;
import ar.com.hipotecario.mobile.negocio.cuentainversor.TipoCuentaBancaria;
import ar.com.hipotecario.mobile.negocio.cuentainversor.cuentacomitente.AltaCuentaComitenteRequestUnificado;
import ar.com.hipotecario.mobile.negocio.cuentainversor.cuentacomitente.CNV.account.CuentaInversorCNV;
import ar.com.hipotecario.mobile.negocio.cuentainversor.cuentacomitente.CNV.investor.InversorCNV;
import ar.com.hipotecario.mobile.negocio.cuentainversor.cuentacomitente.ctaunitrade.CuentaComitenteUnitrade;
import ar.com.hipotecario.mobile.negocio.cuentainversor.cuentafondos.AltaCuentaFondosRequestUnificado;
import ar.com.hipotecario.mobile.servicio.CuentasService;
import ar.com.hipotecario.mobile.servicio.InversionesService;
import ar.com.hipotecario.mobile.servicio.ProductosService;
import ar.com.hipotecario.mobile.servicio.RestCatalogo;
import ar.com.hipotecario.mobile.servicio.RestInversiones;
import ar.com.hipotecario.mobile.servicio.RestPersona;
import ar.com.hipotecario.mobile.servicio.RestScheduler;
import ar.com.hipotecario.mobile.servicio.SqlHomebanking;
import ar.com.hipotecario.mobile.servicio.TransmitMB;

public class MBInversion {

    public static Integer C_RES_VAL_INVERSION_PERFIL_ADECUADO = 0;
    public static Integer C_RES_VAL_INVERSION_PERFIL_NO_ADECUADO = 1;
    public static Integer C_RES_VAL_INVERSION_OPERA_BAJO_PROPIO_RIESGO = 2;
    private static Integer C_DELAY_ESPERA_PROCESOS_BYMA = ConfigMB.integer("delay_espera_ms_procesos_byma", 1800);
    private static final Pattern PREFIJO_NUM_GUION = Pattern.compile("^\\s*\\d+\\s*-\\s*");

    private static String nombreFondoLimpio(String raw) {
        if (raw == null) return null;
        return PREFIJO_NUM_GUION.matcher(raw).replaceFirst("").trim();
    }


    public static RespuestaMB consolidadaInversiones(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();

        Long inicio = System.currentTimeMillis();

        // SERVICIOS
        Futuro<List<PlazoFijoLogro>> plazosFijosLogrosFuturo = new Futuro<>(() -> contexto.plazosFijosLogros());
        Futuro<List<Objeto>> productosFuturo = new Futuro<>(() -> ProductosService.productos(contexto).objetos("errores"));
        Futuro<Map<CuentaComitente, ApiResponseMB>> mapaResponseFuturo = new Futuro<>(() -> cuentasComitentesEspecies(contexto, respuesta));
        Futuro<ApiResponseMB> cuotapartistaResponseFuturo = new Futuro<>(() -> RestInversiones.cuotapartista(contexto, null, tipoDocEsco(contexto.persona()), null, false, contexto.persona().esPersonaJuridica() ? contexto.persona().cuit() : contexto.persona().numeroDocumento()));

        Map<String, Futuro<RespuestaMB>> mapaTenencias = new HashMap<>();
        for (CuentaComitente cuentaComitente : mapaResponseFuturo.get().keySet()) {
            ContextoMB clon = contexto.clonar();
            clon.parametros.set("idCuentaComitente", cuentaComitente.id());
            Futuro<RespuestaMB> futuroTenencias = new Futuro<>(() -> MBTitulosValores.tenenciaPosicionNegociable(clon));
            mapaTenencias.put(cuentaComitente.id(), futuroTenencias);
        }

        contexto.setHeader("A0", String.valueOf(System.currentTimeMillis() - inicio));

        Map<String, Futuro<RespuestaMB>> mapaFCI = new HashMap<>();
        ApiResponseMB cuotapartistaResponse = cuotapartistaResponseFuturo.get();
        if (!cuotapartistaResponse.hayError()) {
            if (!(cuotapartistaResponse.hayError() && !"1008".equals(cuotapartistaResponse.string("codigo")))) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String fecha = sdf.format(new Date());
                Boolean cuotapartistaFisica = false;
                for (Objeto cuotapartista : cuotapartistaResponse.objetos("CuotapartistaModel")) {
                    cuotapartistaFisica = (!contexto.persona().esPersonaJuridica() && cuotapartista.bool("EsFisico"));
                    if (cuotapartistaFisica) {
                        String idCuotapartista = cuotapartista.string("IDCuotapartista");
                        Integer numeroCuotapartista = cuotapartista.integer("IDCuotapartista");
                        ContextoMB clon = contexto.clonar();
                        clon.parametros.set("fecha", fecha);
                        clon.parametros.set("idCuotapartista", idCuotapartista);
                        clon.parametros.set("numeroCuotapartista", String.valueOf(numeroCuotapartista));
                        Futuro<RespuestaMB> posicionFCIFuturo = new Futuro<>(() -> posicionCuotapartista(clon));
                        mapaFCI.put(idCuotapartista, posicionFCIFuturo);
                    }
                }
            }
        }

        // ENDPOINT
        contexto.setHeader("A1", String.valueOf(System.currentTimeMillis() - inicio));

        // 1. PLAZOS FIJOS
        // 1.1 Plazos fijos - Tradicional y UVA
        BigDecimal sumaPlazosFijosPesos = new BigDecimal(0);
        BigDecimal sumaPlazosFijosDolares = new BigDecimal(0);
        BigDecimal sumaPlazosFijosTradicionalPesos = new BigDecimal(0);
        BigDecimal sumaPlazosFijosUvaPesos = new BigDecimal(0);
        BigDecimal porcentajePlazosFijosPesos = new BigDecimal(0);
        BigDecimal porcentajePlazosFijosDolares = new BigDecimal(0);
        BigDecimal porcentajePlazosFijosTradicionalPesos = new BigDecimal(0);
        BigDecimal porcentajePlazosFijosUvaPesos = new BigDecimal(0);
        int cantidadPlazosFijosPesos = 0;
        int cantidadPlazosFijosDolares = 0;
        int cantidadPlazosFijosUvaPesos = 0;
        int cantidadPlazosFijosTradicionalPesos = 0;

        // 1.2 Plazos fijos - Logros
        BigDecimal sumaLogrosPesos = new BigDecimal(0);
        BigDecimal sumaLogrosDolares = new BigDecimal(0);
        BigDecimal porcentajeLogrosPesos = new BigDecimal(0);
        BigDecimal porcentajeLogrosDolares = new BigDecimal(0);
        int cantidadLogrosPesos = 0;
        int cantidadLogrosDolares = 0;

        // 1.3 Totales unificado - Incluimos todos los plazos fijos (Tradicional, UVA, Logros, etc).
        BigDecimal sumaPlazosFijosUniPesos = new BigDecimal(0);
        BigDecimal porcentajePlazosFijosUniPesos = new BigDecimal(0);
        int cantidadPlazosFijosUniPesos = 0;
        BigDecimal sumaPlazosFijosUniDolares = new BigDecimal(0);
        BigDecimal porcentajePlazosFijosUniDolares = new BigDecimal(0);
        int cantidadPlazosFijosUniDolares = 0;

        // FCI's
        BigDecimal sumaFciPesos = new BigDecimal(0);
        BigDecimal sumaFciDolares = new BigDecimal(0);
        BigDecimal porcentajeFciPesos = new BigDecimal(0);
        BigDecimal porcentajeFciDolares = new BigDecimal(0);
        int cantidadFciPesos = 0;
        int cantidadFciDolares = 0;

        BigDecimal sumaLicitacionesPrimariasPesos = new BigDecimal(0);
        BigDecimal porcentajeLicitacionesPrimariasPesos = new BigDecimal(0);

        int cantidadTitulosValoresPesos = 0;
        BigDecimal sumaTitulosValoresPesos = new BigDecimal(0);
        BigDecimal porcentajeTitulosValoresPesos = new BigDecimal(0);

        // Total general
        BigDecimal sumaTotalPesos = new BigDecimal(0);
        BigDecimal sumaTotalDolares = new BigDecimal(0);

        Objeto informacion = new Objeto();

        List<Objeto> erroresRespuesta = new ArrayList<Objeto>();

        List<PlazoFijoLogro> plazosFijosLogros = plazosFijosLogrosFuturo.get();

        for (PlazoFijoLogro plazoFijoLogro : plazosFijosLogros) {
            String estado = plazoFijoLogro.idEstado();
            if ("V".equals(estado) || "A".equals(estado)) {
                for (Integer id = 1; id <= plazoFijoLogro.cantidadPlazosFijos(); ++id) {
                    if ("A".equals(plazoFijoLogro.itemIdEstado(id))) {
                        if ("80".equals(plazoFijoLogro.idMoneda())) {
                            sumaLogrosPesos = sumaLogrosPesos.add(plazoFijoLogro.itemMontoInicial(id));
                            cantidadLogrosPesos++;
                        }
                        if ("2".equals(plazoFijoLogro.idMoneda())) {
                            sumaLogrosDolares = sumaLogrosDolares.add(plazoFijoLogro.itemMontoInicial(id));
                            cantidadLogrosDolares++;
                        }
                    }
                }
            }
        }

        for (Objeto item : productosFuturo.get()) {
            if ("plazosFijos".equals(item.string("codigo"))) {
                // return Respuesta.estado("ERROR_CONSOLIDADA");
            }
        }

        // 1.1 Plazos fijos - Tradicional y UVA
        for (PlazoFijo item : contexto.plazosFijos()) {
            if (item.esValido() && !item.esPlazoFijoLogros()) {
                if (item.esPesos()) {
                    if (item.esUva()) {
                        sumaPlazosFijosUvaPesos =
                                sumaPlazosFijosUvaPesos.add(item.importeInicial());
                        cantidadPlazosFijosUvaPesos++;
                    } else {
                        sumaPlazosFijosTradicionalPesos =
                                sumaPlazosFijosTradicionalPesos.add(item.importeInicial());
                        cantidadPlazosFijosTradicionalPesos++;
                    }
                    sumaPlazosFijosPesos = sumaPlazosFijosPesos.add(item.importeInicial());
                    cantidadPlazosFijosPesos++;
                } else if (item.esDolares()) {
                    sumaPlazosFijosDolares = sumaPlazosFijosDolares.add(item.importeInicial());
                    cantidadPlazosFijosDolares++;
                }
            }
        }

        // 1.3 Plazos fijos - Totales unificado
        cantidadPlazosFijosUniPesos = cantidadPlazosFijosPesos + cantidadLogrosPesos;
        sumaPlazosFijosUniPesos = sumaPlazosFijosPesos.add(sumaLogrosPesos);
        cantidadPlazosFijosUniDolares = cantidadPlazosFijosDolares + cantidadLogrosDolares;
        sumaPlazosFijosUniDolares = sumaPlazosFijosDolares.add(sumaLogrosDolares);

        Map<CuentaComitente, ApiResponseMB> mapaResponse = mapaResponseFuturo.get();

        contexto.setHeader("A2", String.valueOf(System.currentTimeMillis() - inicio));

        for (CuentaComitente cuentaComitente : mapaResponse.keySet()) {
            try {
                contexto.parametros.set("idCuentaComitente", cuentaComitente.id());
                RespuestaMB tenencias = mapaTenencias.get(cuentaComitente.id()).get();

                if (tenencias.hayError()) {
                    erroresRespuesta.add(crearError("ERR_TEN_AYB", cuentaComitente.id(), tenencias.string("estado")));
                    continue;
                }

                sumaTitulosValoresPesos = sumaTitulosValoresPesos.add(tenencias.bigDecimal("totalFondosPesos", "0"));
                cantidadTitulosValoresPesos += tenencias.integer("cantidadTitulosValoresPesos", 0);
            } catch (Exception e) {
                erroresRespuesta.add(crearError("ERR_TEN_AYB", cuentaComitente.id(), "Error en servicio"));
                e.printStackTrace();
                continue;
            }
        }

        contexto.setHeader("A3", String.valueOf(System.currentTimeMillis() - inicio));

        //ApiResponseMB cuotapartistaResponse = cuotapartistaResponseFuturo.get();
        if (cuotapartistaResponse.hayError()) {
            if (cuotapartistaResponse.codigo == 504 || cuotapartistaResponse.codigo == 500) {
                erroresRespuesta.add(crearError("ERR_FCI", null, "Error en servicio"));
                // continue
            }
        } else {
            if (cuotapartistaResponse.hayError() && !"1008".equals(cuotapartistaResponse.string("codigo"))) {
                respuesta.setEstadoExistenErrores();
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String fecha = sdf.format(new Date());
                // Por las dudas nos aseguramos que no se usen estos nombres de parametros (usar
                // otro nombre
                // en caso de ser necesario).
                if (contexto.parametros.existe("fecha") || contexto.parametros.existe("IDCuotapartista")) {
                    return RespuestaMB.estado("ERROR_CONSOLIDADA");
                }
                Boolean cuotapartistaFisica = false;
                contexto.setHeader("A4", String.valueOf(System.currentTimeMillis() - inicio));
                for (Objeto cuotapartista : cuotapartistaResponse.objetos("CuotapartistaModel")) {
                    cuotapartistaFisica = (!contexto.persona().esPersonaJuridica() && cuotapartista.bool("EsFisico"));
                    // Contamos solo cuotapartistas del cliente.
                    if (cuotapartistaFisica) {
                        String idCuotapartista = cuotapartista.string("IDCuotapartista");
                        Integer numeroCuotapartista = cuotapartista.integer("IDCuotapartista");
                        contexto.parametros.set("fecha", fecha);
                        contexto.parametros.set("idCuotapartista", idCuotapartista);
                        contexto.parametros.set("numeroCuotapartista", String.valueOf(numeroCuotapartista));
                        RespuestaMB posicionFCI = mapaFCI.get(idCuotapartista).get();

                        // TODO GB - Poner esta condicion en HB tambien.
                        if (posicionFCI.string("estado").equals("ERROR")) {
                            erroresRespuesta.add(crearError("ERR_FCI", idCuotapartista, ""));
                            continue;
                        }

                        if (!posicionFCI.string("estado").equals("SIN_POSICION") && !posicionFCI.string("estado").equals("TIMEOUT")) {
                            sumaFciPesos = sumaFciPesos.add(posicionFCI.bigDecimal("totalFondosPesos"));
                            sumaFciDolares = sumaFciDolares.add(posicionFCI.bigDecimal("totalFondosDolares"));
                            cantidadFciPesos += posicionFCI.integer("cantidadFciPesos");
                            cantidadFciDolares += posicionFCI.integer("cantidadFciDolares");
                        }
                    }
                }
                contexto.setHeader("A5", String.valueOf(System.currentTimeMillis() - inicio));
            }
        }

        // Calculos
        // Totales
        sumaTotalPesos = sumaTotalPesos.add(sumaLogrosPesos);
        sumaTotalPesos = sumaTotalPesos.add(sumaPlazosFijosPesos);
        sumaTotalPesos = sumaTotalPesos
                .add(sumaTitulosValoresPesos == null ? new BigDecimal(0) : sumaTitulosValoresPesos);
        sumaTotalPesos = sumaTotalPesos.add(sumaLicitacionesPrimariasPesos);
        sumaTotalPesos = sumaTotalPesos.add(sumaFciPesos);
        sumaTotalDolares = sumaTotalDolares.add(sumaLogrosDolares);
        sumaTotalDolares = sumaTotalDolares.add(sumaPlazosFijosDolares);
        sumaTotalDolares = sumaTotalDolares.add(sumaFciDolares);

        // Porcentajes
        porcentajeLogrosPesos = porcentajeV2(sumaLogrosPesos, sumaTotalPesos);
        porcentajeLogrosDolares = porcentajeV2(sumaLogrosDolares, sumaTotalDolares);
        porcentajePlazosFijosPesos = porcentajeV2(sumaPlazosFijosPesos, sumaTotalPesos);
        porcentajePlazosFijosTradicionalPesos = porcentajeV2(sumaPlazosFijosTradicionalPesos, sumaTotalPesos);
        porcentajePlazosFijosUvaPesos = porcentajeV2(sumaPlazosFijosUvaPesos, sumaTotalPesos);
        porcentajePlazosFijosDolares = porcentajeV2(sumaPlazosFijosDolares, sumaTotalDolares);
        porcentajePlazosFijosUniPesos = porcentajeV2(sumaPlazosFijosUniPesos, sumaTotalPesos);
        porcentajePlazosFijosUniDolares = porcentajeV2(sumaPlazosFijosUniDolares, sumaTotalDolares);

        porcentajeLicitacionesPrimariasPesos = porcentajeV2(sumaLicitacionesPrimariasPesos, sumaTotalPesos);
        porcentajeTitulosValoresPesos = porcentajeV2(
                sumaTitulosValoresPesos == null ? new BigDecimal(0) : sumaTitulosValoresPesos, sumaTotalPesos);
        porcentajeFciPesos = porcentajeV2(sumaFciPesos, sumaTotalPesos);
        porcentajeFciDolares = porcentajeV2(sumaFciDolares, sumaTotalDolares);


        List<BigDecimal> porcentajesPesos = new ArrayList<>(Arrays.asList(
                porcentajePlazosFijosUniPesos,
                porcentajeTitulosValoresPesos,
                porcentajeFciPesos
        ));
        List<BigDecimal> porcentajesDolares = new ArrayList<>(Arrays.asList(
                porcentajePlazosFijosUniDolares,
                porcentajeFciDolares
        ));

        porcentajesPesos = normalizarPorcentajes(porcentajesPesos);
        porcentajesDolares = normalizarPorcentajes(porcentajesDolares);

        porcentajePlazosFijosUniPesos = porcentajesPesos.get(0);
        porcentajeTitulosValoresPesos = porcentajesPesos.get(1);
        porcentajeFciPesos = porcentajesPesos.get(2);

        porcentajePlazosFijosUniDolares = porcentajesDolares.get(0);
        porcentajeFciDolares = porcentajesDolares.get(1);

        // RESPUESTA
        // Logros
        informacion.set("logrosPesos", sumaLogrosPesos);
        informacion.set("logrosPesosFormateado", Formateador.importe(sumaLogrosPesos));
        informacion.set("logrosDolares", sumaLogrosDolares);
        informacion.set("logrosDolaresFormateado", Formateador.importe(sumaLogrosDolares));

        // Subtotales
        informacion.set("plazosFijosPesos", sumaPlazosFijosPesos);
        informacion.set("plazosFijosPesosFormateado", Formateador.importe(sumaPlazosFijosPesos));
        informacion.set("plazosFijosTradicionalPesos", sumaPlazosFijosTradicionalPesos);
        informacion.set("plazosFijosTradicionalPesosFormateado", Formateador.importe(sumaPlazosFijosTradicionalPesos));
        informacion.set("plazosFijosUvaPesos", sumaPlazosFijosUvaPesos);
        informacion.set("plazosFijosUvaPesosFormateado", Formateador.importe(sumaPlazosFijosUvaPesos));
        informacion.set("plazosFijosPesosUvaFormateado", Formateador.importe(sumaPlazosFijosUvaPesos));
        informacion.set("plazosFijosDolares", sumaPlazosFijosDolares);
        informacion.set("plazosFijosDolaresFormateado", Formateador.importe(sumaPlazosFijosDolares));
        informacion.set("plazosFijosUniPesos", sumaPlazosFijosUniPesos);
        informacion.set("plazosFijosUniPesosFormateado", Formateador.importe(sumaPlazosFijosUniPesos));
        informacion.set("plazosFijosUniDolares", sumaPlazosFijosUniDolares);
        informacion.set("plazosFijosUniDolaresFormateado", Formateador.importe(sumaPlazosFijosUniDolares));

        informacion.set("titulosValoresPesos",
                sumaTitulosValoresPesos == null ? new BigDecimal(0) : sumaTitulosValoresPesos);
        informacion.set("titulosValoresPesosFormateado",
                Formateador.importe(sumaTitulosValoresPesos == null ? new BigDecimal(0) : sumaTitulosValoresPesos));

        informacion.set("fciPesos", sumaFciPesos);
        informacion.set("fciPesosFormateado", Formateador.importe(sumaFciPesos));
        informacion.set("fciDolares", sumaFciDolares);
        informacion.set("fciDolaresFormateado", Formateador.importe(sumaFciDolares));

        // Cantidades
        informacion.set("cantidadLogrosPesos", cantidadLogrosPesos);
        informacion.set("cantidadLogrosDolares", cantidadLogrosDolares);
        informacion.set("cantidadPlazosFijosPesos", cantidadPlazosFijosPesos);
        informacion.set("cantidadPlazosFijosUvaPesos", cantidadPlazosFijosUvaPesos);
        informacion.set("cantidadPlazosFijosTradicionalPesos", cantidadPlazosFijosTradicionalPesos);
        informacion.set("cantidadPlazosFijosDolares", cantidadPlazosFijosDolares);
        informacion.set("cantidadPlazosFijosUniPesos", cantidadPlazosFijosUniPesos);
        informacion.set("cantidadPlazosFijosUniDolares", cantidadPlazosFijosUniDolares);
        informacion.set("cantidadTitulosValoresPesos", cantidadTitulosValoresPesos);
        informacion.set("cantidadFciPesos", cantidadFciPesos);
        informacion.set("cantidadFciDolares", cantidadFciDolares);

        // Porcentajes
        informacion.set("porcentajeLogrosPesos", porcentajeLogrosPesos);
        informacion.set("porcentajeLogrosDolares", porcentajeLogrosDolares);
        informacion.set("porcentajePlazosFijosPesos", porcentajePlazosFijosPesos);
        informacion.set("porcentajePlazosFijosTradicionalPesos", porcentajePlazosFijosTradicionalPesos);
        informacion.set("porcentajePlazosFijosUvaPesos", porcentajePlazosFijosUvaPesos);
        informacion.set("porcentajePlazosFijosDolares", porcentajePlazosFijosDolares);
        informacion.set("porcentajePlazosFijosUniPesos", porcentajePlazosFijosUniPesos);
        informacion.set("porcentajePlazosFijosUniDolares", porcentajePlazosFijosUniDolares);
        informacion.set("porcentajeLicitacionesPrimariasPesos", porcentajeLicitacionesPrimariasPesos);
        informacion.set("porcentajeTitulosValoresPesos", porcentajeTitulosValoresPesos);
        informacion.set("porcentajeFciPesos", porcentajeFciPesos);
        informacion.set("porcentajeFciDolares", porcentajeFciDolares);

        // Totales
        informacion.set("totalPesos", sumaTotalPesos);
        informacion.set("totalPesosFormateado", Formateador.importe(sumaTotalPesos));
        informacion.set("totalDolares", sumaTotalDolares);
        informacion.set("totalDolaresFormateado", Formateador.importe(sumaTotalDolares));

        Date hoy = new Date();
        informacion.set("fechaActual", new SimpleDateFormat("dd/MM/yyyy").format(hoy));

        respuesta.set("informacion", informacion);
        respuesta.set("errores", erroresRespuesta);

        return respuesta;
    }

    private static List<BigDecimal> normalizarPorcentajes(List<BigDecimal> porcentajes) {
        // Sumar los porcentajes
        BigDecimal sumaTotal = porcentajes.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Si la suma es 0, devolver todos los porcentajes como 0.00
        if (sumaTotal.compareTo(BigDecimal.ZERO) == 0) {
            return porcentajes.stream()
                    .map(p -> BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .collect(Collectors.toList());
        }

        // Calcular el factor de ajuste
        BigDecimal factorAjuste = sumaTotal.divide(new BigDecimal("100.00"), 10, RoundingMode.HALF_UP);

        // Normalizar cada porcentaje
        for (int i = 0; i < porcentajes.size(); i++) {
            porcentajes.set(i, porcentajes.get(i).divide(factorAjuste, 2, RoundingMode.HALF_UP));
        }

        return porcentajes;
    }

    protected static BigDecimal licitacionesByma(ContextoMB contexto, Objeto tenencia) {
        BigDecimal saldoValuado = BigDecimal.ZERO;
        String codigo = tenencia.string("codigoEspecie").split("-")[0].trim();

        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_licitaciones_byma")) {
            BigDecimal valor = BigDecimal.ZERO;
            ApiResponseMB responseIndicesRealTime = RestInversiones.indicesRealTime(contexto, codigo, "", "3");

            if (!responseIndicesRealTime.hayError()) {
                for (Objeto item : responseIndicesRealTime.objetos()) {
                    Objeto tituloValor = RestInversiones.tituloValor(contexto, codigo);

                    if (item.bigDecimal("trade") != null && item.bigDecimal("trade").compareTo(new BigDecimal(0)) != 0) {
                        valor = item.bigDecimal("trade");
                        if ("Titulo Publico".equals(tituloValor.string("clasificacion")) || "CHA".equals(tituloValor.string("clasificacion"))) {
                            valor = valor.divide(new BigDecimal(100));
                        }
                        saldoValuado = valor.multiply(tenencia.bigDecimal("saldoDisponibleNominal"));
                    }
                }
            }
        }

        return saldoValuado;
    }

    public static RespuestaMB licitaciones(ContextoMB contexto) {
        Futuro<ApiResponseMB> responseFuturo = new Futuro<>(() -> InversionesService.inversionesGetLicitaciones(contexto));
        ApiResponseMB response = responseFuturo.get();
        RespuestaMB respuesta = new RespuestaMB();
        Objeto datoFinal = new Objeto();

        if (response.hayError()) {
            return RespuestaMB.error();
        }

        for (Objeto licitacion : response.objetos()) {
            Objeto itemLicitacion = new Objeto();
            itemLicitacion.set("idLicitacion", licitacion.string("Codigo"));
            itemLicitacion.set("codigoLicitacion", licitacion.string("Codigo"));
            itemLicitacion.set("descripcionLicitacion", licitacion.string("Descripcion"));
            itemLicitacion.set("fechaFinColaboradoresHB", licitacion.string("FechaFinColocadoresHB"));
            itemLicitacion.set("horaFinColocadoresHB", licitacion.string("HoraFinColocadoresHB"));
            Objeto especies = new Objeto();
            for (Objeto especie : licitacion.objetos("Especies.Especie")) {
                for (Objeto tramo : especie.objetos("Tramos.Tramo")) {
                    Objeto itemEspecie = new Objeto();
                    String monedaLiquidacion = especie.string("MonedaLiquidacion");
                    String monedaSimbolo = "";
                    if (monedaLiquidacion == null) {
                        monedaLiquidacion = "";
                    }
                    if ("PESOS".equals(monedaLiquidacion.toUpperCase())) {
                        monedaSimbolo = Formateador.simboloMoneda("80");
                    }
                    if ("UVA".equals(monedaLiquidacion.toUpperCase())) {
                        monedaSimbolo = Formateador.simboloMoneda("88");
                    }
                    if ("USD".equals(monedaLiquidacion.toUpperCase())) {
                        monedaSimbolo = Formateador.simboloMoneda("2");
                    }

                    itemEspecie.set("codigoLicitacion", licitacion.string("Codigo"));
                    itemEspecie.set("monedaSimbolo", monedaSimbolo);
                    itemEspecie.set("monedaDescripcion", monedaLiquidacion);
                    itemEspecie.set("idEspecie", licitacion.string("Codigo") + "_" + especie.string("Codigo") + "_" + tramo.string("Tipo") + "_" + especie.string("Criterio.Tipo") + "_" + licitacion.string("Descripcion") + "_" + especie.string("Descripcion"));
                    itemEspecie.set("codigoEspecie", especie.string("Codigo"));
                    itemEspecie.set("descripcionEspecie", especie.string("Descripcion") + "-" + tramo.string("Tipo"));
                    itemEspecie.set("tipo", tramo.string("Tipo"));
                    itemEspecie.set("criterio", especie.string("Criterio.Tipo"));
                    itemEspecie.set("valorNominalMaximo", tramo.integer("Cantidad.MaximaPorPostura"));
                    itemEspecie.set("valorNominalMinimo", tramo.integer("Cantidad.MinimaPorPostura"));
                    itemEspecie.set("valorNominalMaximoFormateado", tramo.integer("Cantidad.MaximaPorPostura"));
                    itemEspecie.set("valorNominalMinimoFormateado", tramo.integer("Cantidad.MinimaPorPostura"));

                    String criterioTipo = especie.string("Criterio.Tipo");
                    String tipo = tramo.string("Tipo");

                    switch (criterioTipo) {
                        case "PRECIO":
                            if (tipo.equals("TCO")) {
                                itemEspecie.set("precioMaximo", especie.bigDecimal("Criterio.Maximo"));
                                itemEspecie.set("precioMinimo", especie.bigDecimal("Criterio.Minimo"));
                                itemEspecie.set("precioMaximoFormateado", Formateador.importeCantDecimales(especie.bigDecimal("Criterio.Maximo"), 4));
                                itemEspecie.set("precioMinimoFormateado", Formateador.importeCantDecimales(especie.bigDecimal("Criterio.Minimo"), 4));
                            } else if (tipo.equals("TNC")) {
                                itemEspecie.set("precioBloqueo", especie.bigDecimal("PrecioBloqueo"));
                                itemEspecie.set("precioBloqueoFormateado", Formateador.importeCantDecimales(especie.bigDecimal("PrecioBloqueo"), 4));
                            }
                            break;
                        case "TASA":
                            if (tipo.equals("TCO")) {
                                // precio es una tasa
                                itemEspecie.set("precioBloqueo", especie.bigDecimal("PrecioBloqueo"));
                                itemEspecie.set("precioBloqueoFormateado", Formateador.importeCantDecimales(especie.bigDecimal("PrecioBloqueo"), 2));
                                itemEspecie.set("precioMaximo", especie.bigDecimal("Criterio.Maximo"));
                                itemEspecie.set("precioMinimo", especie.bigDecimal("Criterio.Minimo"));
                            } else if (tipo.equals("TNC")) {
                                itemEspecie.set("precioBloqueo", especie.bigDecimal("PrecioBloqueo"));
                                itemEspecie.set("precioBloqueoFormateado", Formateador.importeCantDecimales(especie.bigDecimal("PrecioBloqueo"), 4));
                            }
                            break;
                        case "RENDIMIENTO":
                            if (tipo.equals("TCO")) {
                                // precio es una tasa
                                itemEspecie.set("precioBloqueo", especie.bigDecimal("PrecioBloqueo"));
                                itemEspecie.set("precioBloqueoFormateado", Formateador.importeCantDecimales(especie.bigDecimal("PrecioBloqueo"), 2));
                                itemEspecie.set("precioMaximo", especie.bigDecimal("Criterio.Maximo"));
                                itemEspecie.set("precioMinimo", especie.bigDecimal("Criterio.Minimo"));
                            } else if (tipo.equals("TNC")) {
                                itemEspecie.set("precioBloqueo", especie.bigDecimal("PrecioBloqueo"));
                                itemEspecie.set("precioBloqueoFormateado", Formateador.importeCantDecimales(especie.bigDecimal("PrecioBloqueo"), 4));
                            }
                            break;
                        default:
                            itemEspecie.set("precioBloqueo", especie.bigDecimal("PrecioBloqueo"));
                            itemEspecie.set("precioBloqueoFormateado", Formateador.importeCantDecimales(especie.bigDecimal("PrecioBloqueo"), 4));
                            break;
                    }
                    especies.add(itemEspecie);
                }
            }
            itemLicitacion.set("especies", especies);
            datoFinal.add(itemLicitacion);
        }
        respuesta.set("licitaciones", datoFinal);
        // respuesta.set("pdf", response.string("file"));
        return respuesta;
    }

    public static RespuestaMB altaSuscripcionLicitacion(ContextoMB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");
        String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
        String idEspecie = contexto.parametros.string("idEspecie");
        Integer cantidad = contexto.parametros.integer("cantidad");
        BigDecimal valor = contexto.parametros.bigDecimal("valor");
        boolean operaFueraDePerfil = contexto.parametros.bool("operaFueraDePerfil");
        String telefonoIngresado = contexto.parametros.string("telefono", null);

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return RespuestaMB.estado("CUENTA_INCORRECTA");
        }

        CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
        if (cuentaComitente == null) {
            return RespuestaMB.estado("CUENTA_COMITENTE_INCORRECTA");
        }

        Futuro<ApiRequestMB> requestFuturo = new Futuro<>(() -> ApiMB.request("licitacionesAltaSuscripcion", "inversiones", "POST", "/v1/cuentascomitentes/licitaciones", contexto));

        String telefono = "";
        if (telefonoIngresado != null) {
            telefono = telefonoIngresado;
        } else {
            Objeto celular = RestPersona.celular(contexto, contexto.persona().cuit());
            if (celular != null) {
                Objeto objeto = new Objeto();
                objeto.set("codigoArea", celular.string("codigoArea"));
                objeto.set("caracteristica", celular.string("caracteristica"));
                objeto.set("numero", celular.string("numero"));
                telefono = celular.string("codigoArea") + "-" + celular.string("caracteristica") + "-" + celular.string("numero");
            }
        }
        String codigoLicitacion = idEspecie.split("_")[0];
        String codigoEspecie = idEspecie.split("_")[1];
        String tramo = idEspecie.split("_")[2];
        String criterio = idEspecie.split("_")[3];
        String descripcionLicitacion = idEspecie.split("_")[4];
        String descripcionEspecie = idEspecie.split("_")[5];

        ApiRequestMB request = requestFuturo.get();

        request.body("cantidad", cantidad);
        request.body("colocador", "00");
        request.body("comitente", cuentaComitente.numero());
        request.body("cuenta", cuenta.numero());
        request.body("cuit", contexto.persona().cuit());
        request.body("especie", codigoEspecie);
        if ("TCO".equals(tramo)) {
            if ("PRECIO".equals(criterio)) {
                request.body("precio", valor);
            }
            if ("TASA".equals(criterio)) {
                request.body("tasa", valor);
            }
            if ("RENDIMIENTO".equals(criterio)) {
                request.body("rendimiento", valor);
            }
        }
        Date fechaPago = new Date();
        request.body("fechaPago", new SimpleDateFormat("yyyy-MM-dd").format(fechaPago));
        request.body("Hora", new SimpleDateFormat("hh:mm").format(fechaPago));
        request.body("formaPago", "EFE");
        request.body("idCobis", contexto.idCobis());
        request.body("licitacion", codigoLicitacion);
        request.body("motivoInversion", "");
        request.body("operaFueraDePerfil", operaFueraDePerfil);
        request.body("sucursal", cuenta.sucursal());
        request.body("telefonoContacto", telefono);
        request.body("tipoInversor", "HB");
        request.body("tipoPersona", "F");
        request.body("tramo", tramo);

        ApiResponseMB response = ApiMB.response(request);
        if (response.hayError()) {
            if ("202".equals(response.string("codigo"))) {
                RespuestaMB respuesta = RespuestaMB.estado("PERFIL_INVERSOR_VENCIDO");
                respuesta.set("mensaje", response.string("mensajeAlUsuario"));
                return respuesta;
            }
            if (response.string("codigo").equals("2023")) {
                return RespuestaMB.estado("OPERACION_ARRIESGADA");
            }
            if (response.string("codigo").equals("201")) {
                return RespuestaMB.estado("OPERACION_ARRIESGADA");
            }
            if (!response.string("mensajeAlUsuario").isEmpty()) {
                RespuestaMB respuesta = RespuestaMB.estado("ERROR_FUNCIONAL");
                respuesta.set("mensaje", response.string("mensajeAlUsuario"));
                return respuesta;
            }
            return RespuestaMB.error();
        }

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("numero", response.string("numero"));

        generarComprobanteLicitacion(contexto, response.string("numero"), new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()), codigoLicitacion, descripcionLicitacion, codigoEspecie, descripcionEspecie, cantidad, valor, tramo, cuentaComitente.numero(), cuenta.numero());
        /*
         * Map<String, String> comprobante = new HashMap<>();
         * comprobante.put("COMPROBANTE", response.string("numero"));
         * comprobante.put("FECHA_HORA", new
         * SimpleDateFormat("dd/MM/yyyy HH:ss").format(new Date()));
         * comprobante.put("LICITACION", codigoLicitacion + " - " +
         * descripcionLicitacion); comprobante.put("IMPORTE",
         * Formateador.importe(valor.multiply(new BigDecimal(cantidad))));
         * comprobante.put("ESPECIE", codigoEspecie + " - " + descripcionEspecie);
         * comprobante.put("CANTIDAD", new Integer(cantidad.intValue()).toString());
         * comprobante.put("PRECIO", Formateador.importe(valor));
         * comprobante.put("TRAMO", tramo); comprobante.put("CUENTA_COMITENTE",
         * cuentaComitente.numero()); comprobante.put("CUENTA", cuenta.numero());
         *
         * String idComprobante = ""; if ("TCO".equals(tramo)) { idComprobante =
         * "suscripcion-licitacion-tco_" + response.string("numero"); } else {
         * idComprobante = "suscripcion-licitacion-tnc_" + response.string("numero"); }
         * contexto.sesion().setComprobante(idComprobante, comprobante);
         */
        return respuesta;
    }

    public static String generarComprobanteLicitacion(ContextoMB contexto, String numero, String fecha, String codigoLicitacion, String descripcionLicitacion, String codigoEspecie, String descripcionEspecie, Integer cantidad, BigDecimal valor, String tramo, String cuentaComitenteNumero, String cuentaNumero) {

        if (valor == null) {
            valor = new BigDecimal(0);
        }
        if (cantidad == null) {
            cantidad = Integer.valueOf(0);
        }
        Map<String, String> comprobante = new HashMap<>();
        comprobante.put("COMPROBANTE", numero);
        comprobante.put("FECHA_HORA", fecha);
        comprobante.put("LICITACION", codigoLicitacion + " - " + descripcionLicitacion);
        comprobante.put("IMPORTE", Formateador.importe(valor.multiply(new BigDecimal(cantidad))));
        comprobante.put("ESPECIE", codigoEspecie + " - " + descripcionEspecie);
        comprobante.put("CANTIDAD", Integer.valueOf(cantidad.intValue()).toString());
        comprobante.put("PRECIO", Formateador.importe(valor));
        comprobante.put("TRAMO", tramo);
        comprobante.put("CUENTA_COMITENTE", cuentaComitenteNumero);
        comprobante.put("CUENTA", cuentaNumero);

        String idComprobante = "";
        if ("TCO".equals(tramo)) {
            idComprobante = "suscripcion-licitacion-tco_" + numero;
        } else {
            idComprobante = "suscripcion-licitacion-tnc_" + numero;
        }
        contexto.sesion().setComprobante(idComprobante, comprobante);
        return idComprobante;
    }

    public static Map<CuentaComitente, ApiResponseMB> cuentasComitentesEspecies(ContextoMB contexto, RespuestaMB respuesta) {
        List<CuentaComitente> cuentasComitentes = contexto.cuentasComitentes();
        Map<CuentaComitente, ApiResponseMB> mapResponse = new HashMap<>();

        ExecutorService executorService = Concurrencia.executorService(cuentasComitentes);
        for (CuentaComitente cuentaComitente : cuentasComitentes) {
            executorService.submit(() -> {
                ApiRequestMB request = ApiMB.request("CuentaComitenteEspecies", "inversiones", "GET", "/v1/cuentascomitentes/{id}/especies", contexto);
                request.path("id", cuentaComitente.numero());
                request.query("idcliente", contexto.idCobis());
                request.cacheSesion = true;
                ApiResponseMB response = ApiMB.response(request, cuentaComitente.id());
                mapResponse.put(cuentaComitente, response);
//				Api.eliminarCache(contexto, "CuentaComitenteEspecies", cuentaComitente.id());
            });
        }
        Concurrencia.esperar(executorService, respuesta);

        return mapResponse;
    }

    public static RespuestaMB activosVigentes(ContextoMB contexto) {

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.add("activosVigentes", new Objeto().set("id", "Titulo Publico").set("descripcion", "Título Público"));
        respuesta.add("activosVigentes", new Objeto().set("id", "Accion").set("descripcion", "Acción"));
        respuesta.add("activosVigentes", new Objeto().set("id", "Cedear").set("descripcion", "Cedear"));
        respuesta.add("activosVigentes", new Objeto().set("id", "CHA").set("descripcion", "Cédula Hipotecaria"));
        return respuesta;
    }

    public static RespuestaMB perfilInversor(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        ApiResponseMB response = RestPersona.perfilInversor(contexto);
        if (response.hayError()) {
            String error = "";
            if (response.string("codigo").equals("500") && response.string("mensajeAlUsuario").contains("400")) {
                respuesta.set("tienePerfilInversor", false);
                respuesta.set("estado", "FUERA_HORARIO");

                try {
                    contexto.insertarContador("INVERSOR_EN_BATCH");
                } catch (Exception e) {
                    // TODO: handle exception
                }

                return respuesta;
            } else {
                error = "ERROR";
            }
            return RespuestaMB.estado(error);
        }
        for (Objeto item : response.objetos()) {
            respuesta = datosPerfilInversor(item, respuesta);
        }
        return respuesta;
    }

    private static RespuestaMB datosPerfilInversor(Objeto item, RespuestaMB respuesta) {
        if ("".equals(item.string("perfilInversor", ""))) {
            respuesta.set("tienePerfilInversor", false);
        } else {
            respuesta.set("tienePerfilInversor", true);
            respuesta.set("vencido", "V".equals(item.string("estado")));
            respuesta.set("idPerfil", item.string("perfilInversor"));
            respuesta.set("descripcionPerfil", descripcionesPerfiles().get(item.string("perfilInversor")));
            respuesta.set("fechaTest", item.string("fechaAM"));
            respuesta.set("fechaFin", item.string("fechaFin"));
        }
        return respuesta;
    }

    private static Map<String, String> descripcionesPerfiles() {
        Map<String, String> descripciones = new HashMap<>();
        descripciones.put("1", "Conservador");
        descripciones.put("2", "Moderado");
        descripciones.put("3", "Arriesgado");
        descripciones.put("4", "Opera bajo su propio riesgo");
        return descripciones;
    }

    public static RespuestaMB perfilInversorPropioRiesgo(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        ApiResponseMB responseConsulta = RestPersona.perfilInversor(contexto);
        if (responseConsulta.hayError()) {
            String error = "ERROR";
            return RespuestaMB.estado(error);
        }
        String operacion = "I";
        for (Objeto item : responseConsulta.objetos()) {
            if (!"".equals(item.string("perfilInversor", ""))) {
                operacion = "U";
            }
        }
        ApiResponseMB response = RestPersona.actualizaPerfilInversor(contexto, "4", operacion);
        if (response.hayError()) {
            String error = "ERROR";
            return RespuestaMB.estado(error);
        }
        ApiMB.eliminarCache(contexto, "Cuotapartista", contexto.idCobis());
        return respuesta;
    }

    public static void eliminarCacheCuentasComitentesEspecies(ContextoMB contexto) {
        for (CuentaComitente cuentaComitente : contexto.cuentasComitentes()) {
            ApiMB.eliminarCache(contexto, "CuentaComitenteEspecies", cuentaComitente.id());
        }
    }

    public static BigDecimal porcentaje(BigDecimal num, BigDecimal total) {
        BigDecimal cero = new BigDecimal(0);
        if (total.compareTo(cero) > 0) {
            return num.multiply(new BigDecimal(100)).divide(total, RoundingMode.CEILING);
        } else {
            return (cero);
        }
    }

    public static BigDecimal porcentajeV2(BigDecimal num, BigDecimal total) {
        BigDecimal cero = new BigDecimal(0);
        if (total.compareTo(cero) > 0) {
            return num.multiply(new BigDecimal(100)).divide(total, 2, RoundingMode.HALF_UP);
        } else {
            return (cero);
        }
    }

    public static BigDecimal porcentaje(BigDecimal num, BigDecimal total, int decimales, Optional<RoundingMode> modoRedondeo) {
        BigDecimal cero = new BigDecimal(0);
        if (total.compareTo(cero) <= 0) {
            return cero;
        }
        return num.multiply(new BigDecimal(100)).divide(total, decimales
                , modoRedondeo.isPresent() ? modoRedondeo.get() : RoundingMode.CEILING);
    }

    public static RespuestaMB altaCuentaInversorContingencia(ContextoMB contexto) {
        Integer idPaisNacimiento = contexto.parametros.integer("idPaisNacimiento", contexto.persona().idPaisNacimiento());
        String ciudadNacimiento = contexto.parametros.string("ciudadNacimiento");
        Date fechaNacimiento = contexto.parametros.date("fechaNacimiento", "d/M/yyyy", contexto.persona().fechaNacimiento());

        String codigoPostal = contexto.parametros.string("codigoPostal", null);
        String calleDomicilio = contexto.parametros.string("calleDomicilio", null);
        String alturaDomicilio = contexto.parametros.string("alturaDomicilio", null);
        String dptoDomicilio = contexto.parametros.string("dptoDomicilio", null);
        String pisoDomicilio = contexto.parametros.string("pisoDomicilio", null);
        String codigoAreaCelular = contexto.parametros.string("codigoAreaCelular", null);
        String caracteristicaCelular = contexto.parametros.string("caracteristicaCelular", null);
        String numeroCelular = contexto.parametros.string("numeroCelular", null);

        String email = contexto.parametros.string("email", null);

        String idCuentaAsociada = contexto.parametros.string("idCuentaAsociada", contexto.cuentaPorDefecto().id());
        Date fechaIngresoPais = contexto.parametros.date("fechaIngresoPais", "d/M/yyyy", contexto.persona().fechaResidencia());

        Boolean esPEP = contexto.parametros.bool("esPersonaExpuestaPoliticamente", contexto.persona().esPersonaExpuestaPoliticamente());
        Boolean esSO = contexto.parametros.bool("esSujetoObligado", contexto.persona().esSujetoObligado());
        Integer idSituacionLaboral = contexto.parametros.integer("idSituacionLaboral", 0);
        Integer idProfesion = contexto.parametros.integer("idProfesion", 0);
        BigDecimal sueldo = contexto.parametros.bigDecimal("sueldo", "0");

        if (contexto.idCobis() == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        for (String tipoProducto : Objeto.listOf("UNI", "CTAPART")) {
            SqlRequestMB sqlRequest = SqlMB.request("AltaCuentaInversor", "homebanking");
            sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[solicitudProducto] (";
            sqlRequest.sql += "[sp_tipoOperacion]";
            sqlRequest.sql += ",[sp_idCobis]";
            sqlRequest.sql += ",[sp_fechaSolicitud]";
            sqlRequest.sql += ",[sp_secuencial]";
            sqlRequest.sql += ",[sp_tipoProducto]";
            sqlRequest.sql += ",[sp_numeroProducto]";
            sqlRequest.sql += ",[sp_moneda]";
            sqlRequest.sql += ",[sp_tipoCuentaAsociada]";
            sqlRequest.sql += ",[sp_numeroCuentaAsociada]";
            sqlRequest.sql += ",[sp_sucursal]";
            sqlRequest.sql += ",[sp_apellido]";
            sqlRequest.sql += ",[sp_nombre]";
            sqlRequest.sql += ",[sp_sexo]";
            sqlRequest.sql += ",[sp_tipoDocumento]";
            sqlRequest.sql += ",[sp_numeroDocumento]";
            sqlRequest.sql += ",[sp_fechaNacimiento]";
            sqlRequest.sql += ",[sp_paisNacimientoCod]";
            sqlRequest.sql += ",[sp_paisNacimientoDesc]";
            sqlRequest.sql += ",[sp_nacionalidadCod]";
            sqlRequest.sql += ",[sp_nacionalidadDesc]";
            sqlRequest.sql += ",[sp_ciudadNacimiento]";
            sqlRequest.sql += ",[sp_tipoIdTributario]";
            sqlRequest.sql += ",[sp_idTributario]";
            sqlRequest.sql += ",[sp_estadoCivil]";
            sqlRequest.sql += ",[sp_profesionCod]";
            sqlRequest.sql += ",[sp_profesionDesc]";
            sqlRequest.sql += ",[sp_situacionLaboral]";
            sqlRequest.sql += ",[sp_fechaIngreso]";
            sqlRequest.sql += ",[sp_sueldo]";
            sqlRequest.sql += ",[sp_calle]";
            sqlRequest.sql += ",[sp_altura]";
            sqlRequest.sql += ",[sp_piso]";
            sqlRequest.sql += ",[sp_departamento]";
            sqlRequest.sql += ",[sp_codigoPostal]";
            sqlRequest.sql += ",[sp_localidadCod]";
            sqlRequest.sql += ",[sp_localidadDesc]";
            sqlRequest.sql += ",[sp_provinciaCod]";
            sqlRequest.sql += ",[sp_provinciaDesc]";
            sqlRequest.sql += ",[sp_tipoTelefonoCod]";
            sqlRequest.sql += ",[sp_tipoTelefonoDesc]";
            sqlRequest.sql += ",[sp_ddiTelefono]";
            sqlRequest.sql += ",[sp_ddnTelefono]";
            sqlRequest.sql += ",[sp_caracteristicaTelefono]";
            sqlRequest.sql += ",[sp_numeroTelefono]";
            sqlRequest.sql += ",[sp_tipoMail]";
            sqlRequest.sql += ",[sp_mail]";
            sqlRequest.sql += ",[sp_pep]";
            sqlRequest.sql += ",[sp_so]";
            sqlRequest.sql += ",[sp_ocde]";
            sqlRequest.sql += ",[sp_fatca]";
            sqlRequest.sql += ",[sp_licitudFondos]";
            sqlRequest.sql += ",[sp_ttcc]";
            sqlRequest.sql += ",[sp_resolucion]";
            sqlRequest.sql += ",[sp_cbuCuentaAsociada]";
            sqlRequest.sql += ",[sp_canal]";
            sqlRequest.sql += ") VALUES (";
            for (int i = sqlRequest.sql.split(",").length; i > 0; --i) {
                sqlRequest.sql += sqlRequest.sql.endsWith("(") ? "?" : ", ?";
            }
            sqlRequest.sql += ")";

            Cuenta cuenta = contexto.cuenta(idCuentaAsociada);
            if (!cuenta.unipersonal()) {
                return RespuestaMB.error();
            }

            Persona persona = contexto.persona();
            Domicilio domicilio = new Domicilio(contexto, "DP");
            Telefono telefono = new Telefono(contexto, "E");
//			Email email = new Email(contexto, "EMP");

            sqlRequest.add("ALTA"); // sp_tipoOperacion
            sqlRequest.add(contexto.idCobis()); // sp_idCobis
            sqlRequest.add(new Date()); // sp_fechaSolicitud
            sqlRequest.add(null); // sp_secuencial
            sqlRequest.add(tipoProducto); // sp_tipoProducto
            sqlRequest.add(null); // sp_numeroProducto
            sqlRequest.add(null); // sp_moneda
            sqlRequest.add(cuenta.idTipo()); // sp_tipoCuentaAsociada
            sqlRequest.add(cuenta.numero()); // sp_numeroCuentaAsociada
            sqlRequest.add(cuenta.sucursal()); // sp_sucursal
            sqlRequest.add(persona.apellidos().toUpperCase()); // sp_apellido
            sqlRequest.add(persona.nombres().toUpperCase()); // sp_nombre
            sqlRequest.add(persona.idSexo()); // sp_sexo
            sqlRequest.add(persona.idTipoDocumento()); // sp_tipoDocumento
            sqlRequest.add(persona.numeroDocumento()); // sp_numeroDocumento
            sqlRequest.add(new SimpleDateFormat("dd/MM/yyyy").format(fechaNacimiento)); // sp_fechaNacimiento
            sqlRequest.add(idPaisNacimiento); // sp_paisNacimientoCod
            sqlRequest.add(RestCatalogo.nombrePais(contexto, idPaisNacimiento)); // sp_paisNacimientoDesc
            sqlRequest.add(persona.idNacionalidad()); // sp_nacionalidadCod
            sqlRequest.add(RestCatalogo.nombreNacionalidad(contexto, persona.idNacionalidad())); // sp_nacionalidadDesc
            sqlRequest.add(ciudadNacimiento.isEmpty() ? persona.ciudadNacimiento() : ciudadNacimiento); // sp_ciudadNacimiento
            sqlRequest.add(persona.tipoTributario()); // sp_tipoIdTributario
            sqlRequest.add(persona.cuit()); // sp_idTributario
            sqlRequest.add(persona.idEstadoCivil()); // sp_estadoCivil
            sqlRequest.add(idProfesion.equals(0) ? "" : idProfesion); // sp_profesionCod
            sqlRequest.add(MBCatalogo.mapaProfesiones().get(idProfesion) != null ? MBCatalogo.mapaProfesiones().get(idProfesion) : ""); // sp_profesionDesc
            sqlRequest.add(idSituacionLaboral.equals(0) ? persona.idSituacionLaboral() : idSituacionLaboral); // sp_situacionLaboral
            sqlRequest.add(new SimpleDateFormat("dd/MM/yyyy").format(fechaIngresoPais)); // sp_fechaIngreso
            sqlRequest.add(sueldo.intValue() == 0 ? "" : sueldo); // sp_sueldo
            sqlRequest.add(calleDomicilio != null ? calleDomicilio : domicilio.calle()); // sp_calle
            sqlRequest.add(alturaDomicilio != null ? alturaDomicilio : domicilio.altura()); // sp_altura
            sqlRequest.add(pisoDomicilio != null ? pisoDomicilio : domicilio.piso()); // sp_piso
            sqlRequest.add(dptoDomicilio != null ? dptoDomicilio : domicilio.dpto()); // sp_departamento
            sqlRequest.add(codigoPostal != null ? codigoPostal : domicilio.codigoPostal()); // sp_codigoPostal
            sqlRequest.add(domicilio.idLocalidad()); // sp_localidadCod
            sqlRequest.add(domicilio.localidad()); // sp_localidadDesc
            sqlRequest.add(domicilio.idProvincia()); // sp_provinciaCod
            sqlRequest.add(domicilio.provincia()); // sp_provinciaDesc
            sqlRequest.add("E"); // sp_tipoTelefonoCod
            sqlRequest.add("Celular"); // sp_tipoTelefonoDesc
            sqlRequest.add("54"); // sp_ddiTelefono
            sqlRequest.add(codigoAreaCelular != null ? codigoAreaCelular : telefono.codigoArea()); // sp_ddnTelefono
            sqlRequest.add(caracteristicaCelular != null ? caracteristicaCelular : telefono.caracteristica()); // sp_caracteristicaTelefono
            sqlRequest.add(numeroCelular != null ? numeroCelular : telefono.finalNumero()); // sp_numeroTelefono
            sqlRequest.add("EMP"); // sp_tipoMail
            sqlRequest.add(email); // sp_mail
            sqlRequest.add(esPEP ? "S" : "N"); // sp_pep
            sqlRequest.add(esSO ? "S" : "N"); // sp_so
            sqlRequest.add("[" + persona.cuit() + "|" + persona.idPaisResidencia() + "|" + persona.fechaResidencia("yyyyMMdd") + "]"); // sp_ocde
            sqlRequest.add("A"); // sp_fatca
            sqlRequest.add("on"); // sp_licitudFondos
            sqlRequest.add("on"); // sp_ttcc
            sqlRequest.add(null); // sp_resolucion
            sqlRequest.add(cuenta.cbu()); // sp_cbuCuentaAsociada
            sqlRequest.add("MB"); // sp_canal

            SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
            if (sqlResponse.hayError) {
                return RespuestaMB.error();
            }
        }

        return new RespuestaMB();
    }

    /* */
    public static RespuestaMB altaCuentaComitente(ContextoMB contexto, Telefono telefono, Cuenta cuentaPesos, Cuenta cuentaDolar, Domicilio domicilio, Persona persona) throws JsonProcessingException {
        List<InversorCNV> inversorCNVList = new ArrayList<>();
        InversorCNV inversorCNVTitular = new InversorCNV();
        CuentaComitenteUnitrade ctaUnitrade;
        try {
            ctaUnitrade = CuentaComitenteUnitrade.getValue(contexto.persona(), domicilio, telefono, cuentaPesos, cuentaDolar);
        } catch (Exception e) {
            return RespuestaMB.error().set("mensaje", "Error al obtener datos");
        }

        inversorCNVTitular = InversorCNV.getInversorCNVTitular(contexto, persona, domicilio);
        if (inversorCNVTitular == null) {
            return RespuestaMB.error().set("mensaje", "Error al obtener el titular.");
        }

        if (!cuentaPesos.unipersonal()) {
            inversorCNVList = InversorCNV.getInversorCNVCoTitulares(contexto, cuentaPesos);
            if (inversorCNVList == null) {
                return RespuestaMB.error().set("mensaje", "No se encontraron los cotitulares de la cuenta.");
            }
        }

//		ApiRestInversiones.getInversorCNVByCuit(contexto ,inversorCNVList.get(0).getNumIdentificador().toString());

        //llamar al servicio para filtrar los que aun no estan dados de alta
        inversorCNVList.add(inversorCNVTitular);
        CuentaInversorCNV account = CuentaInversorCNV.getValue(contexto.persona());

        AltaCuentaComitenteRequestUnificado comitenteUnificado = new AltaCuentaComitenteRequestUnificado();
        comitenteUnificado.setCuentaComitente(ctaUnitrade);
        comitenteUnificado.setInversorCNVList(inversorCNVList);
        //todo: agregar en account los cotitulares
        comitenteUnificado.setCuentaInversorCNV(account);


        ApiResponseMB responseInversorCNV = RestInversiones.altaCuentaComitente(contexto, comitenteUnificado);
        if (responseInversorCNV.hayError()) {//mulsti-status es 207 no se considera como error. api-inversiones => 400 bad request!
            return RespuestaMB.error().set("cuenta", responseInversorCNV.objetos());
        }
        return RespuestaMB.exito().set("cuenta", responseInversorCNV);
    }

    public static RespuestaMB altaCuentaFondos(ContextoMB contexto, Telefono telefono, Cuenta cuentaPesos, Cuenta cuentaDolar, Domicilio domicilio, Persona persona) throws JsonProcessingException {
        AltaCuentaFondosRequestUnificado cuentaFondos = new AltaCuentaFondosRequestUnificado();
        try {
            cuentaFondos = AltaCuentaFondosRequestUnificado.getValue(contexto, telefono, domicilio, persona, cuentaPesos, cuentaDolar);
        } catch (Exception e) {
            return RespuestaMB.error().set("mensaje", "Error al obtener datos cuentaFondos");
        }

        String valor = tipoDocPersonaESCOv2(cuentaFondos.getTipoDoc());
        ApiResponseMB responseSelectByDocPersona = RestInversiones.selectPersonaByDoc(contexto, cuentaFondos.getNumDoc(), valor);
        if (responseSelectByDocPersona.hayError()) {
            return RespuestaMB.error();
        }
        String idPersonaFondo = responseSelectByDocPersona.bool("EstaAnulado") ? "" : responseSelectByDocPersona.string("IDPersona", null);
        cuentaFondos.setIdPersonaFondo(idPersonaFondo);

        ApiResponseMB responseCuentaFondos = RestInversiones.altaCuentasFondos(contexto, cuentaFondos);
        if (responseCuentaFondos.hayError()) {
            return RespuestaMB.error().set("cuenta", responseCuentaFondos.objetos());
        }

        return RespuestaMB.exito().set("cuenta", responseCuentaFondos.objetos());
    }

    public static RespuestaMB altaCuentaInversorV2(ContextoMB contexto) {
        RespuestaMB horarioCuentaInversor = horarioCuentaInversor(contexto);
        if (!horarioCuentaInversor.string("estado").equalsIgnoreCase("0")) {
            return horarioCuentaInversor;
        }
        //Llamado de constingencia
        try {
            new Futuro<>(() -> altaCuentaInversorContingencia(contexto));
        } catch (Exception e) {
            return RespuestaMB.error();
        }
        String idCuentaAsociada = contexto.parametros.string("idCuentaAsociada", contexto.cuentaPorDefecto().id());

        Futuro<Domicilio> domicilioF = new Futuro<>(() -> new Domicilio(contexto, "DP"));
        Futuro<Telefono> telefonoF = new Futuro<>(() -> new Telefono(contexto, "E"));// Celular
        Futuro<Persona> personaF = new Futuro<>(contexto::persona);
        Futuro<List<Cuenta>> cuentasListF = new Futuro<>(contexto::cuentas);// Celular

        Futuro<Cuenta> cuentaPesosF = new Futuro<>(() -> cuentasListF.get()
                .stream()
                .filter(cuenta -> cuenta.string("idProducto", "")
                        .equalsIgnoreCase(idCuentaAsociada) && cuenta.estado().equalsIgnoreCase("V"))
                .findFirst()
                .orElse(null));

        Futuro<Cuenta> cuentaDolarF = new Futuro<>(() -> cuentasListF.get() // Cuenta Dolar mas antigua
                .stream()
                .filter(valor -> !valor.esPesos() && valor.estado().equalsIgnoreCase("V") && valor.unipersonal())
                .min(Comparator.comparing(o -> LocalDate.parse(o.string("fechaAlta", ""))))
                .orElse(null));

        Cuenta cuentaPesos = cuentaPesosF.get();
        Cuenta cuentaDolar = cuentaDolarF.get();

        if (!cuentaPesos.unipersonal()) {
            return RespuestaMB.error().set("mensaje", "No se permiten cuenta co-tituales");
        }

        if (cuentaPesos == null) {
            return RespuestaMB.error().set("mensaje", "Cuenta " + idCuentaAsociada + " no encontrada");
        }

        Futuro<RespuestaMB> responseCuentaComitenteF = new Futuro<>(() -> altaCuentaComitente(contexto, telefonoF.get(), cuentaPesos, cuentaDolarF.get(), domicilioF.get(), personaF.get()));
        Futuro<RespuestaMB> responseCuentaFondosF = new Futuro<>(() -> altaCuentaFondos(contexto, telefonoF.get(), cuentaPesos, cuentaDolar, domicilioF.get(), personaF.get()));

        RespuestaMB respuestaCuentacomitente = responseCuentaComitenteF.get();
        RespuestaMB respuestaCuentaFondos = responseCuentaFondosF.get();

        if (respuestaCuentacomitente.hayError() || respuestaCuentaFondos.hayError()) {
            return RespuestaMB.error();
        }

        try {
            ProductosService.eliminarCacheProductos(contexto);
            ApiMB.eliminarCache(contexto, "Cuotapartista", contexto.idCobis());
        } catch (Exception e) {
        }

        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_espera_procesos_byma")) {
            // Lanzamos verificacion de estados no bloqueante para actualizar estados.
            new Futuro<>(()
                    -> esperarYActualizarEstadosProcesosBYMA(contexto, respuestaCuentacomitente));
        }

        return RespuestaMB.exito();
    }

    public static RespuestaMB altaCuentaInversor(ContextoMB contexto) {

        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "deshabilitar_alta_inversor")) {
            return RespuestaMB.error();
        }

        // Verificar si la funcionalidad está activada
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_alta_ci_v2")) {
            return altaCuentaInversorV2(contexto);
        }

        return altaCuentaInversorV1(contexto);
    }

    @Deprecated
    public static RespuestaMB altaCuentaInversorV1(ContextoMB contexto) {//todo: Servicio deprecated

        RespuestaMB horarioCuentaInversor = horarioCuentaInversor(contexto);
        if (!horarioCuentaInversor.string("estado").equalsIgnoreCase("0")) {
            return horarioCuentaInversor;
        }

        try {
            new Futuro<>(() -> altaCuentaInversorContingencia(contexto));
        } catch (Exception e) {
            return RespuestaMB.error();
        }

        String idCuentaAsociada = contexto.parametros.string("idCuentaAsociada", contexto.cuentaPorDefecto().id());

        Persona persona = contexto.persona();
        Domicilio domicilio = new Domicilio(contexto, "DP"); // Direccion Postal
        Telefono telefono = new Telefono(contexto, "E"); // Celular
        List<Cuenta> cuentas = contexto.cuentas();
        Cuenta cuentaDolar = cuentas // Cuenta Dolar mas antigua
                .stream().filter(valor -> !valor.esPesos() && valor.estado().equalsIgnoreCase("V")).min(Comparator.comparing(o -> LocalDate.parse(o.string("fechaAlta", "")))).orElse(null);

        Cuenta cuentaPesos = cuentas.stream().filter(cuenta -> cuenta.string("idProducto", "").equalsIgnoreCase(idCuentaAsociada)).findFirst().orElse(null);

        if (cuentaPesos == null || !cuentaPesos.unipersonal()) {
            return RespuestaMB.error();
        }
        CuentaInversor cuentaInversor = getValueAltaCuentaInvor(contexto, cuentaPesos, cuentaDolar, persona, domicilio, telefono);

        String valor = tipoDocPersonaESCO(cuentaInversor.getTipoDoc());
        ApiResponseMB responseSelectByDocPersona = RestInversiones.selectPersonaByDoc(contexto, cuentaInversor.getNumDoc(), valor);
        if (responseSelectByDocPersona.hayError()) {
            return RespuestaMB.error();
        }

        String idPersonaFondo = responseSelectByDocPersona.bool("EstaAnulado") ? "" : responseSelectByDocPersona.string("IDPersona", null);
        cuentaInversor.setIdPersonaFondo(idPersonaFondo);

        ApiResponseMB responseAltaCuentas = RestInversiones.altaCuentaInversor(contexto, cuentaInversor);
        if (responseAltaCuentas.hayError()) {
            return RespuestaMB.error();
        }

        RespuestaMB respuesta = new RespuestaMB();
        Objeto finalReturnCtaComitente = new Objeto();
        Objeto finalReturnCtaCuotapartista = new Objeto();
        Objeto relacion;
        Objeto sqlError = new Objeto();
        sqlError.set("id_cobis", contexto.idCobis());
        sqlError.set("error", 0);

        boolean esExitoCtaComitente = responseAltaCuentas.objeto("ctaComitente").integer("CodigoError") == 0;
        boolean esExitoCajaValores = responseAltaCuentas.objeto("cajaValores").string("respuesta").equalsIgnoreCase("OK");

        boolean esExitoPersonasFondos = responseAltaCuentas.objeto("personas").integer("codigo") == 200;
        boolean esExitoCuentaFondos = responseAltaCuentas.objeto("cuentas").integer("codigo") == 200;

        String resultPersonaFondos = !esExitoPersonasFondos ? responseAltaCuentas.objeto("personas").string("Message") : responseAltaCuentas.objeto("personas").string("mensaje");
        String resultCuentaFondos = !esExitoCuentaFondos ? responseAltaCuentas.objeto("cuentas").string("Message") : responseAltaCuentas.objeto("cuentas").string("numeroCuotapartista");
        String ResultUnitrade = esExitoCtaComitente ? responseAltaCuentas.objeto("ctaComitente").string("NumeroCuentaComitente") : responseAltaCuentas.objeto("ctaComitente").string("DescripcionError");

        if (esExitoCtaComitente) {
            relacion = new Objeto();
            relacion.set("cuenta", "2-" + responseAltaCuentas.objeto("ctaComitente").string("NumeroCuentaComitente")).set("idCliente", contexto.idCobis()).set("moneda", cuentaPesos.idMoneda()).set("nroDocumento", persona.numeroDocumento()).set("oficial", -1).set("oficina", persona.sucursal()).set("secuencualDireccion", domicilio.string("idCore", "")).set("secuencualTelefono", telefono.string("idCore", "")).set("tipoProducto", "UNI").set("tipoTitularidad", "T");
            ApiResponseMB relacionUnitrade = ProductosService.relacionClienteProducto(contexto, relacion);
            if (relacionUnitrade.hayError()) {
                sqlError.set("error", 1);
                finalReturnCtaComitente.set("codigo", 400);
                finalReturnCtaComitente.set("mensaje", "No se pudo crear la RCP ctaComitente");
                sqlError.set("unitrade", ResultUnitrade);
                sqlError.set("cv", responseAltaCuentas.objeto("cajaValores").string("mensajeAdicional"));
                sqlError.set("rcp_cta_comitente", finalReturnCtaComitente.string("mensaje"));
                respuesta.set("relacionCuentaComitente", finalReturnCtaComitente);
            } else {
                finalReturnCtaComitente.set("codigo", 200);
                finalReturnCtaComitente.set("mensaje", "Relacion UNITRADE con exito");
                respuesta.set("relacionCuentaComitente", finalReturnCtaComitente);
            }
            if (!esExitoCajaValores) {
                sqlError.set("error", 1);
                sqlError.set("unitrade", ResultUnitrade);
                sqlError.set("cv", responseAltaCuentas.objeto("cajaValores").string("mensajeAdicional"));
            }
        } else {
            finalReturnCtaComitente.set("codigo", 400);
            finalReturnCtaComitente.set("mensaje", "No se pudo crear la RCP ctaComitente");
            sqlError.set("error", 1);
            sqlError.set("unitrade", responseAltaCuentas.objeto("ctaComitente").string("DescripcionError"));
            sqlError.set("cv", responseAltaCuentas.objeto("cajaValores").string("mensajeAdicional"));
            sqlError.set("rcp_cta_comitente", "No se pudo crear la RCP ctaComitente");
            respuesta.set("relacionCuentaComitente", finalReturnCtaComitente);
        }

        if (esExitoCuentaFondos && esExitoPersonasFondos) {
            relacion = new Objeto();
            relacion.set("cuenta", "TO-" + responseAltaCuentas.objeto("cuentas").string("numeroCuotapartista")).set("idCliente", contexto.idCobis()).set("moneda", cuentaPesos.idMoneda()).set("nroDocumento", persona.numeroDocumento()).set("oficial", -1).set("oficina", persona.sucursal()).set("secuencualDireccion", domicilio.string("idCore", "")).set("secuencualTelefono", telefono.string("idCore", "")).set("tipoProducto", "RJA").set("tipoTitularidad", "T");
            ApiResponseMB relacionCuotapartista = ProductosService.relacionClienteProducto(contexto, relacion);
            if (relacionCuotapartista.hayError()) {
                sqlError.set("error", 1);
                sqlError.set("fondos_persona", resultPersonaFondos);
                sqlError.set("fondos_cuenta", resultCuentaFondos);
                finalReturnCtaCuotapartista.set("codigo", 400);
                finalReturnCtaCuotapartista.set("mensaje", "No se pudo crear la RCP cuenta cuotapartista");
                sqlError.set("rcp_cta_cuotapartista", finalReturnCtaCuotapartista.string("mensaje"));
                respuesta.set("relacionCuotapartista", finalReturnCtaCuotapartista);
            } else {
                finalReturnCtaCuotapartista.set("codigo", 200);
                finalReturnCtaCuotapartista.set("mensaje", "Relacion CUOTAPARTISTA con exito");
                respuesta.set("relacionCuotapartista", finalReturnCtaCuotapartista);
            }
        } else {
            sqlError.set("error", 1);
            sqlError.set("fondos_persona", resultPersonaFondos);
            sqlError.set("fondos_cuenta", resultCuentaFondos);
            sqlError.set("rcp_cta_cuotapartista", "No se pudo crear la RCP cuotapartista");
            finalReturnCtaCuotapartista.set("codigo", 400);
            finalReturnCtaCuotapartista.set("mensaje", "No se pudo crear la RCP cuenta cuotapartista");
            respuesta.set("relacionCuotapartista", finalReturnCtaComitente);
        }

        if (sqlError.integer("error") != 0) {
            SqlHomebanking.saveErrorAltaCuentaInversor(sqlError);
        }
        try {
            ProductosService.eliminarCacheProductos(contexto);
            ApiMB.eliminarCache(contexto, "Cuotapartista", contexto.idCobis());
        } catch (Exception e) {
        }
        return respuesta;

    }

    public static CuentaInversor getValueAltaCuentaInvor(ContextoMB contexto, Cuenta cuentaPesos, Cuenta cuentaDolar, Persona persona, Domicilio domicilio, Telefono telefono) {
        Integer idPaisNacimiento = contexto.parametros.integer("idPaisNacimiento", contexto.persona().idPaisNacimiento());
        //String ciudadNacimiento = contexto.parametros.string("ciudadNacimiento");
        Date fechaNacimiento = contexto.parametros.date("fechaNacimiento", "dd/MM/yyyy", contexto.persona().fechaNacimiento());

        String codigoPostal = contexto.parametros.string("codigoPostal", null);
        String calleDomicilio = contexto.parametros.string("calleDomicilio", null);
        String alturaDomicilio = contexto.parametros.string("alturaDomicilio", null);
        String dptoDomicilio = contexto.parametros.string("dptoDomicilio", null);
        String pisoDomicilio = contexto.parametros.string("pisoDomicilio", null);
        String codigoAreaCelular = contexto.parametros.string("codigoArea", null);
        String caracteristicaCelular = contexto.parametros.string("caracteristicaCelular", null);
        String numeroCelular = contexto.parametros.string("numeroCelular", null);
        String email = contexto.parametros.string("email");

        email = email == null || email.isEmpty() ? persona.email() : email;

        if (email == null || email.isEmpty()) {
            email = "notiene@notienemail.com.ar";
        }
        //Date fechaIngresoPais = contexto.parametros.date("fechaIngresoPais", "d/M/yyyy", contexto.persona().fechaResidencia());

        Boolean esPEP = contexto.parametros.bool("esPersonaExpuestaPoliticamente", contexto.persona().esPersonaExpuestaPoliticamente());
        //Boolean esSO = contexto.parametros.bool("esSujetoObligado", contexto.persona().esSujetoObligado());
        //Integer idSituacionLaboral = contexto.parametros.integer("idSituacionLaboral", 0);
        //Integer idProfesion = contexto.parametros.integer("idProfesion", 0);
        //BigDecimal sueldo = contexto.parametros.bigDecimal("sueldo", "0");

        // Email email = new Email(contexto, "EMP");

        CuentaInversor cuentaInversor = new CuentaInversor();
        DomicilioCuentaInversor domicilioCuentaInversor = new DomicilioCuentaInversor();
        CuentasLiquidacion cuentasLiquidacion = new CuentasLiquidacion();
        Dolares dolar = new Dolares();
        Pesos pesos = new Pesos();
        Origen origen = new Origen();
        Radicacion radicacion = new Radicacion();
        DatosUIF datosUIF = new DatosUIF();

        cuentaInversor.setCobis(contexto.idCobis());
        cuentaInversor.setSucursal(persona.sucursal());
        cuentaInversor.setRazonSocial(fixCaracteresEspeciales(contexto.persona().nombreCompleto()));
        cuentaInversor.setTipoIdTributario(persona.tipoTributario());
        cuentaInversor.setCuit(persona.cuit());
        cuentaInversor.setTipoSujeto(persona.string("idSector", ""));
        String calificacion = persona.string("idTipoCliente", "").equals("PS") ? "PA" : persona.string("idTipoCliente", "");
        cuentaInversor.setCalificacion(calificacion);
        cuentaInversor.setActividad("000");
        cuentaInversor.setSituacionGanancia(persona.string("idGanancias", ""));
        //cuentaInversor.setCondicionIva(persona.string("idSituacionImpositiva", "CONF"));
        cuentaInversor.setCondicionIva("CONF");

        cuentaInversor.setDireccion(domicilio.string("idCore", ""));

        String telefonoUnitrade = telefono.string("idCore", "") + ", " + telefono.string("idDireccion", "");
        String vfnet = "(" + telefono.string("codigoPais", "") + ")" + "(" + codigoAreaCelular + ")" + caracteristicaCelular + "-" + numeroCelular;
        cuentaInversor.setTelefonoUnitrade(telefonoUnitrade);
        cuentaInversor.setTelefonoVFNet(vfnet);
        cuentaInversor.setEmail(email.toLowerCase());
        cuentaInversor.setNombre(fixCaracteresEspeciales(persona.nombre()));
        cuentaInversor.setApellido(fixCaracteresEspeciales(persona.apellido()));
        cuentaInversor.setTipoPersona(persona.esPersonaJuridica() ? "JU" : "FI");
        cuentaInversor.setIdPersona(contexto.idCobis());
        domicilioCuentaInversor.setAlturaCalle(alturaDomicilio);
        domicilioCuentaInversor.setCalle(fixCaracteresEspeciales(calleDomicilio));
        domicilioCuentaInversor.setCp(codigoPostal);
        domicilioCuentaInversor.setLocalidad(fixCaracteresEspeciales(domicilio.localidad()));
        domicilioCuentaInversor.setPais(RestCatalogo.nombrePais(contexto, persona.idPaisResidencia()));
        domicilioCuentaInversor.setProvincia(domicilio.string("idProvincia", ""));
        domicilioCuentaInversor.setPiso(pisoDomicilio);
        domicilioCuentaInversor.setDepartamento(dptoDomicilio);
        cuentaInversor.setDomicilio(domicilioCuentaInversor);
        cuentaInversor.setFechaNacimiento(new SimpleDateFormat("dd/MM/yyyy").format(fechaNacimiento));
        cuentaInversor.setPaisNacimiento(RestCatalogo.nombrePais(contexto, idPaisNacimiento));
        cuentaInversor.setPaisNacionalidad(RestCatalogo.nombrePais(contexto, persona.idNacionalidad()));

        pesos.setMoneda(cuentaPesos.string("moneda", ""));
        pesos.setSucursal(cuentaPesos.sucursal());
        pesos.setTipoCuenta(cuentaPesos.idTipo());
        pesos.setNumero(cuentaPesos.numero());
        cuentasLiquidacion.setPeso(pesos);
        if (cuentaDolar != null) {
            dolar.setMoneda(cuentaDolar.string("moneda", ""));
            dolar.setSucursal(cuentaDolar.sucursal());
            dolar.setTipoCuenta(cuentaDolar.idTipo());
            dolar.setNumero(cuentaDolar.numero());
            cuentasLiquidacion.setDolares(dolar);
        }
        cuentaInversor.setCuentasLiquidacion(cuentasLiquidacion);

        origen.setAgenteColocador("8");
        origen.setSucursal(persona.sucursal());
        cuentaInversor.setOrigen(origen);

        radicacion.setAgenteColocador("8");
        radicacion.setSucursal(persona.sucursal());
        radicacion.setCanalVivienda("");
        radicacion.setOficinaCuenta("");
        cuentaInversor.setRadicacion(radicacion);

        List<CuotasBancarias> cuotasBancarias = new ArrayList<>();
        CuotasBancarias cuentaBancaria = new CuotasBancarias();
        cuentaBancaria.setAlias(cuentaPesos.alias());
        cuentaBancaria.setBanco(new Banco("00044"));
        cuentaBancaria.setCbu(cuentaPesos.cbu());
        cuentaBancaria.setCuitTitular(persona.cuit());
        cuentaBancaria.setDescripcion(cuentaPesos.descripcionCorta());
        cuentaBancaria.setFechaApertura(cuentaPesos.fechaAlta("yyyy-MM-dd"));
        cuentaBancaria.setIban("");
        cuentaBancaria.setIdCuentaBancaria(generarIDUnico());
        cuentaBancaria.setMoneda(new Moneda(cuentaPesos.idMoneda()));
        cuentaBancaria.setNumeroCuenta(cuentaPesos.numero());
        cuentaBancaria.setNumeroSucursal(cuentaPesos.sucursal());
        cuentaBancaria.setTipoCuentaBancaria(new TipoCuentaBancaria(cuentaPesos.idTipo()));
        cuotasBancarias.add(cuentaBancaria);

        if (cuentaDolar != null) {
            cuentaBancaria = new CuotasBancarias();
            cuentaBancaria.setAlias(cuentaDolar.alias());
            cuentaBancaria.setBanco(new Banco("00044"));
            cuentaBancaria.setCbu(cuentaDolar.cbu());
            cuentaBancaria.setCuitTitular(persona.cuit());
            cuentaBancaria.setDescripcion(cuentaDolar.descripcionCorta());
            cuentaBancaria.setFechaApertura(cuentaDolar.fechaAlta("yyyy-MM-dd"));
            cuentaBancaria.setIban("");
            cuentaBancaria.setIdCuentaBancaria(generarIDUnico());
            cuentaBancaria.setMoneda(new Moneda(cuentaDolar.idMoneda()));
            cuentaBancaria.setNumeroCuenta(cuentaDolar.numero());
            cuentaBancaria.setNumeroSucursal(cuentaDolar.sucursal());
            cuentaBancaria.setTipoCuentaBancaria(new TipoCuentaBancaria(cuentaDolar.idTipo()));
            cuotasBancarias.add(cuentaBancaria);
        }

        cuentaInversor.setCuotasBancarias(cuotasBancarias);

        datosUIF.setMonedaImporteEstimado("");
        cuentaInversor.setDatosUIF(datosUIF);

        Boolean esMasculino = persona.idSexo() == "M";

        cuentaInversor.setEsFisico(persona.esPersonaFisica());
        cuentaInversor.setEsmasculino(esMasculino);
        cuentaInversor.setEsPEP(esPEP);
        cuentaInversor.setImprimeResumenCuenta(true);
        cuentaInversor.setPoseeInstrPagoPerm(true);
        cuentaInversor.setRequiereFirmaConjunta(false);
        cuentaInversor.setTipoDoc(persona.tipoDocumento());

        cuentaInversor.setNumDoc(persona.numeroDocumento());
        cuentaInversor.setCuotaPartista(generarIDUnico());
        cuentaInversor.setActividadPrincipal("");
        cuentaInversor.setCategoria("1");
        cuentaInversor.setNumCuotapartista("1");

        cuentaInversor.setSegmentoInversion(persona.esPersonaJuridica() ? "3" : "1");

        cuentaInversor.setTipoCuotapartista("");
        cuentaInversor.setTipoInversor("");
        cuentaInversor.setTipoPerfilRiesgo("");
        cuentaInversor.setRepresentantes("");
        cuentaInversor.setTarjetasCredito("");
        cuentaInversor.setIdPersonaCondomino(cuentaInversor.getIdPersona());
        cuentaInversor.setPerfil(contexto.idCobis());
        cuentaInversor.setPatrimonio("0.00");
        //cuentaInversor.setIdBanco(contexto.idCobis()); // es idCobis segun doc persona
        cuentaInversor.setPromedioMensual("0.00");
        cuentaInversor.setTipoContribuyente("");
        cuentaInversor.setTipoEstadoCivil(persona.idEstadoCivil());
        String identTributario = domicilioCuentaInversor.getPais().equalsIgnoreCase("ARGENTINA") ? "CUIL" : "EXT";
        cuentaInversor.setIdentTributario(identTributario);
        cuentaInversor.setNumIdentificador(persona.string("cuit", ""));
        cuentaInversor.setLugarRegistracion("");
        cuentaInversor.setProvExpedicionCI("");

        String residenteExterior = cuentaInversor.getTipoPersona().equalsIgnoreCase("JU") || domicilioCuentaInversor.getPais().equalsIgnoreCase("ARGENTINA") ? "0" : "1";
        cuentaInversor.setResidenteExterior(residenteExterior);
        cuentaInversor.setPaisResidenciaFiscal("");
        cuentaInversor.setIdentificacionFiscal("");

        return cuentaInversor;
    }

    public static RespuestaMB horarioCuentaInversor(ContextoMB contexto) {
        LocalTime time = LocalTime.now();

        String apertura = ConfigMB.string("mb_hora_inicio_cuenta_inversor");
        String cierre = ConfigMB.string("mb_hora_cierre_cuenta_inversor");

        Boolean esAntes = time.isBefore(LocalTime.parse(apertura));
        Boolean esDespues = time.isAfter(LocalTime.parse(cierre));
        if (!Util.isDiaHabil(contexto) || esAntes || esDespues) {
            return new RespuestaMB().setEstado("FUERA_DE_HORARIO").set("apertura", apertura).set("cierre", cierre);
        }
        return RespuestaMB.exito();
    }

    public static String generarIDUnico() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException();
        }
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");// todo: 17caracteres ver esto
        return ahora.format(formatter);
    }

    public static RespuestaMB testPerfilInversor(ContextoMB contexto) {
        Objeto pregunta = null;
        Objeto preguntas = new Objeto();

        pregunta = new Objeto();
        pregunta.set("id", 1).set("pregunta", "1. Su edad se encuentra dentro del rango de:");
        pregunta.add("respuestas", new Objeto().set("id", 1).set("respuesta", "Menos de 25 años."));
        pregunta.add("respuestas", new Objeto().set("id", 2).set("respuesta", "De 25 a 35 años."));
        pregunta.add("respuestas", new Objeto().set("id", 3).set("respuesta", "De 36 a 55 años."));
        pregunta.add("respuestas", new Objeto().set("id", 4).set("respuesta", "De 56 Años o más."));
        preguntas.add(pregunta);

        pregunta = new Objeto();
        pregunta.set("id", 2).set("pregunta", "2. ¿En cuál de los siguientes instrumentos del mercado de capitales invierte con mayor frecuencia? (Si su respuesta incluiría más de una opción, elija la que considere más arriesgada)");
        pregunta.add("respuestas", new Objeto().set("id", 1).set("respuesta", "Fondos Comunes de Inversion de Liquidez."));
        pregunta.add("respuestas", new Objeto().set("id", 2).set("respuesta", "Fondos Comunes de Inversion Mixtos o de Acciones."));
        pregunta.add("respuestas", new Objeto().set("id", 3).set("respuesta", "Bonos."));
        pregunta.add("respuestas", new Objeto().set("id", 4).set("respuesta", "Acciones."));
        pregunta.add("respuestas", new Objeto().set("id", 5).set("respuesta", "Nunca he invertido en instrumentos del mercado de capitales."));
        preguntas.add(pregunta);

        pregunta = new Objeto();
        pregunta.set("id", 3).set("pregunta", "3. ¿Cuál es el objetivo de las inversiones que está realizando?");
        pregunta.add("respuestas", new Objeto().set("id", 1).set("respuesta", "Preservar mi capital invertido."));
        pregunta.add("respuestas", new Objeto().set("id", 2).set("respuesta", "Preservar mi capital, pero percibiendo intereses."));
        pregunta.add("respuestas", new Objeto().set("id", 3).set("respuesta", "Lograr el crecimiento de mi capital invertido."));
        preguntas.add(pregunta);

        pregunta = new Objeto();
        pregunta.set("id", 4).set("pregunta", "4. ¿Ha realizado alguna vez una inversión en el Mercado de Capitales?");
        pregunta.add("respuestas", new Objeto().set("id", 1).set("respuesta", "Sí."));
        pregunta.add("respuestas", new Objeto().set("id", 2).set("respuesta", "No."));
        preguntas.add(pregunta);

        pregunta = new Objeto();
        pregunta.set("id", 5).set("pregunta", "5. ¿Cuenta con alguna reserva para cubrir un imprevisto?");
        pregunta.add("respuestas", new Objeto().set("id", 1).set("respuesta", "No o es muy pequeña."));
        pregunta.add("respuestas", new Objeto().set("id", 2).set("respuesta", "Sí, representa más o menos 5 meses de mis ingresos."));
        pregunta.add("respuestas", new Objeto().set("id", 3).set("respuesta", "Sí, es una cantidad significativa."));
        preguntas.add(pregunta);

        pregunta = new Objeto();
        pregunta.set("id", 6).set("pregunta", "6. ¿Qué porcentaje de sus ahorros esta dispuesto a destinar a las inversiones en el mercado de capitales?");
        pregunta.add("respuestas", new Objeto().set("id", 1).set("respuesta", "Menos del 25%."));
        pregunta.add("respuestas", new Objeto().set("id", 2).set("respuesta", "Entre el 25% y el 40%."));
        pregunta.add("respuestas", new Objeto().set("id", 3).set("respuesta", "Entre el 41% y el 65%."));
        pregunta.add("respuestas", new Objeto().set("id", 4).set("respuesta", "Más del 65%"));
        preguntas.add(pregunta);

        pregunta = new Objeto();
        pregunta.set("id", 7).set("pregunta", "7. ¿Cuál sería la canasta de inversiones que más lo identifica?");
        pregunta.add("respuestas", new Objeto().set("id", 1).set("respuesta", "100% en activos de renta fija a corto plazo y con mucha liquidez."));
        pregunta.add("respuestas", new Objeto().set("id", 2).set("respuesta", "60% en activos de renta fija altamente líquidos a corto plazo y 40% en activos de renta variable de mediano plazo."));
        pregunta.add("respuestas", new Objeto().set("id", 3).set("respuesta", "40% en activos de renta fija altamente líquidos a corto plazo y 60% en activos de renta variable a largo plazo."));
        pregunta.add("respuestas", new Objeto().set("id", 4).set("respuesta", "100% en activos de renta variable a largo plazo."));
        preguntas.add(pregunta);

        pregunta = new Objeto();
        pregunta.set("id", 8).set("pregunta", "8. ¿Cuál es el plazo máximo que usted estaría dispuesto a mantener sus inversiones en el mercado de capitales?");
        pregunta.add("respuestas", new Objeto().set("id", 1).set("respuesta", "Menos de un año."));
        pregunta.add("respuestas", new Objeto().set("id", 2).set("respuesta", "Entre uno y tres años."));
        pregunta.add("respuestas", new Objeto().set("id", 3).set("respuesta", "Más de tres años."));
        preguntas.add(pregunta);

        pregunta = new Objeto();
        pregunta.set("id", 9).set("pregunta", "9. Seleccione de las siguientes afirmaciones cuál identifica mejor su actitud hacia las inversiones:");
        pregunta.add("respuestas", new Objeto().set("id", 1).set("respuesta", "No estaría dispuesto a realizar ninguna inversión que implicara arriesgar mi capital."));
        pregunta.add("respuestas", new Objeto().set("id", 2).set("respuesta", "Aceptaría un mínimo riesgo si con ello puedo obtener una mayor rentabilidad."));
        pregunta.add("respuestas", new Objeto().set("id", 3).set("respuesta", "Estaría dispuesto a asumir una pérdida del 10% si espero tener a mediano / largo plazo una mayor rentabilidad."));
        pregunta.add("respuestas", new Objeto().set("id", 4).set("respuesta", "Acepto asumir un alto riesgo para obtener una mayor rentabilidad."));
        preguntas.add(pregunta);

        pregunta = new Objeto();
        pregunta.set("id", 10).set("pregunta", "10. Ante una baja importante en su portfolio de inversiones, usted:");
        pregunta.add("respuestas", new Objeto().set("id", 1).set("respuesta", "Recuperaría el total de mis activos."));
        pregunta.add("respuestas", new Objeto().set("id", 2).set("respuesta", "Rescataría una parte de mis activos."));
        pregunta.add("respuestas", new Objeto().set("id", 3).set("respuesta", "Mantendría la totalidad de mis activos esperando una suba."));
        pregunta.add("respuestas", new Objeto().set("id", 4).set("respuesta", "Adicionaría más capital esperando comprar barato."));
        preguntas.add(pregunta);

        pregunta = new Objeto();
        pregunta.set("id", 11).set("pregunta", "11. Si usted tuviera que contratar un seguro para auto, optaría por:");
        pregunta.add("respuestas", new Objeto().set("id", 1).set("respuesta", "Póliza contra todo riesgo, sin importar que sea la más cara."));
        pregunta.add("respuestas", new Objeto().set("id", 2).set("respuesta", "Póliza únicamente de seguro contra terceros."));
        pregunta.add("respuestas", new Objeto().set("id", 3).set("respuesta", "La póliza más barata, aunque su cobertura sea muy pobre."));
        pregunta.add("respuestas", new Objeto().set("id", 4).set("respuesta", "No contrato ninguna póliza."));
        preguntas.add(pregunta);

        pregunta = new Objeto();
        pregunta.set("id", 12).set("pregunta", "12. En el momento de realizar una inversión, cuál de las siguientes opciones prefiere?");
        pregunta.add("respuestas", new Objeto().set("id", 1).set("respuesta", "Preservar el dinero que se invirtió con una rentabilidad mínima."));
        pregunta.add("respuestas", new Objeto().set("id", 2).set("respuesta", "Tener una ganancia apenas superior a la de un plazo fijo, aunque este sujeta a una variación mínima del mercado."));
        pregunta.add("respuestas", new Objeto().set("id", 3).set("respuesta", "Obtener una ganancia significativa, corriendo el riesgo de perder más de la mitad de la inversión inicial."));
        preguntas.add(pregunta);

        pregunta = new Objeto();
        pregunta.set("id", 13).set("pregunta", "13. Usted estaría dispuesto a asumir una baja en el valor de sus activos:");
        pregunta.add("respuestas", new Objeto().set("id", 1).set("respuesta", "No estoy dispuesto aceptar ninguna pérdida."));
        pregunta.add("respuestas", new Objeto().set("id", 2).set("respuesta", "Máximo del 5%."));
        pregunta.add("respuestas", new Objeto().set("id", 3).set("respuesta", "Entre 6% y 15%."));
        pregunta.add("respuestas", new Objeto().set("id", 4).set("respuesta", "Entre 16% y 25%."));
        pregunta.add("respuestas", new Objeto().set("id", 5).set("respuesta", "Entre 26% y 35%."));
        pregunta.add("respuestas", new Objeto().set("id", 6).set("respuesta", "Más de 36%."));
        preguntas.add(pregunta);

        return RespuestaMB.exito("preguntas", preguntas);
    }

    public static RespuestaMB completarTestPerfilInversor(ContextoMB contexto) {
        List<Object> respuestas = new ArrayList<>();

        try {
            respuestas = contexto.parametros.toList("respuestas");
        } catch (Exception e) {
        }

        if (respuestas.isEmpty()) {
            for (Integer i = 1; i <= 13; ++i) {
                respuestas.add(contexto.parametros.integer("respuesta" + i) - 1);
            }
        }

        if (respuestas.size() != 13) {
            return RespuestaMB.parametrosIncorrectos();
        }

        String idPerfilInversor = "";

        String listaValores = "";
        for (Object respuesta : respuestas) {
            listaValores += listaValores.isEmpty() ? respuesta.toString() : "-" + respuesta.toString();
        }

        // INICIO: COPIA DEL HB VIEJO
        boolean ConservadorForzado = false;
        int resultado = 0;
        String perfil = "1";

        String delimiter = "-";
        String[] temp;

        temp = listaValores.split(delimiter);
        // chequea si en las preguntas 7-9-12-13 se seleccionó la primera opción. en
        // este caso es forzosamente 'Conservador'
        for (int i = 0; i < temp.length; i++) {
            if (i == 6 && Integer.parseInt(temp[i]) == 0)
                ConservadorForzado = true;
            if (i == 8 && Integer.parseInt(temp[i]) == 0)
                ConservadorForzado = true;
            if (i == 11 && Integer.parseInt(temp[i]) == 0)
                ConservadorForzado = true;
            if (i == 12 && Integer.parseInt(temp[i]) == 0)
                ConservadorForzado = true;
        }

        // Le pone a cada valor seleccionado el puntaje correspondiente
        listaValores = listaSalidaValores(listaValores);

        temp = listaValores.split(delimiter);
        for (int i = 0; i < temp.length; i++) {
            resultado = resultado + Integer.parseInt(temp[i]);
        }

        int lim_conservador_inf = 0;
        int lim_conservador_sup = 47;
        int lim_moderado_inf = 48;
        int lim_moderado_sup = 70;
        int lim_arriesgado_inf = 71;

        if (resultado >= lim_conservador_inf && resultado <= lim_conservador_sup)
            perfil = "1";
        else if (resultado >= lim_moderado_inf && resultado <= lim_moderado_sup)
            perfil = ConservadorForzado ? "1" : "2";
        else if (resultado >= lim_arriesgado_inf)
            perfil = ConservadorForzado ? "1" : "3";

        idPerfilInversor = perfil;
        // FIN: COPIA DEL HB VIEJO

        RespuestaMB respuesta = new RespuestaMB();
        ApiResponseMB responseConsulta = RestPersona.perfilInversor(contexto);
        if (responseConsulta.hayError()) {
            String error = "ERROR";
            return RespuestaMB.estado(error);
        }
        String operacion = "I";
        for (Objeto item : responseConsulta.objetos()) {
            if (!"".equals(item.string("perfilInversor", ""))) {
                operacion = "U";
            }
        }
        ApiResponseMB response = RestPersona.actualizaPerfilInversor(contexto, idPerfilInversor, operacion);
        if (response.hayError()) {
            String error = "ERROR";
            return RespuestaMB.estado(error);
        }
        ApiMB.eliminarCache(contexto, "Cuotapartista", contexto.idCobis());
        Map<String, String> descripciones = new HashMap<>();
        descripciones.put("1", "Conservador");
        descripciones.put("2", "Moderado");
        descripciones.put("3", "Arriesgado");
        descripciones.put("4", "Opera bajo su propio riesgo");

        Objeto datosPerfil = new Objeto();
        datosPerfil.set("idPerfil", perfil);
        datosPerfil.set("descripcionPerfil", descripciones.get(perfil));
        respuesta.set("perfil", datosPerfil);

        return respuesta;
    }

    public static RespuestaMB cuentaInversor(ContextoMB contexto) {
        Boolean poseeCuenta = contexto.cuentaUnipersonalPesos() != null;
        Boolean poseeCuentaComitente = contexto.cuentaComitentePorDefecto() != null;
        Boolean poseeCuentaCuotapartista = false;

        ApiResponseMB response = ProductosService.productos(contexto);
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        for (Objeto objeto : response.objetos("productos")) {
            if (objeto.string("tipo").equals("RJA")) {
                poseeCuentaCuotapartista = true;
            }
        }

        RespuestaMB respuesta = new RespuestaMB();
        Objeto operaciones = new Objeto();
        operaciones.set("permirAltaCuentaInversor", !poseeCuentaComitente || !poseeCuentaCuotapartista);
        operaciones.set("requiereAltaCajaAhorro", !poseeCuenta);
        respuesta.set("operaciones", operaciones);
        return respuesta;
    }

    // INICIO: COPIA DEL HB VIEJO
    private static String listaSalidaValores(String listaEntrada) {
        String listaSalida = "";

        String delimiter = "-";
        String[] temp;
        temp = listaEntrada.split(delimiter);
        for (int i = 0; i < temp.length; i++) {

            if (i == 0) {
                if (Integer.parseInt(temp[i]) == 0)
                    listaSalida = "9";
                else if (Integer.parseInt(temp[i]) == 1)
                    listaSalida = "6";
                else if (Integer.parseInt(temp[i]) == 2)
                    listaSalida = "3";
                else if (Integer.parseInt(temp[i]) == 3)
                    listaSalida = "0";
            }

            if (i == 1) {
                if (Integer.parseInt(temp[i]) == 0)
                    listaSalida = listaSalida + "-0";
                else if (Integer.parseInt(temp[i]) == 1)
                    listaSalida = listaSalida + "-9";
                else if (Integer.parseInt(temp[i]) == 2)
                    listaSalida = listaSalida + "-6";
                else if (Integer.parseInt(temp[i]) == 3)
                    listaSalida = listaSalida + "-9";
                else if (Integer.parseInt(temp[i]) == 4)
                    listaSalida = listaSalida + "-0";
            }

            if (i == 2) {
                if (Integer.parseInt(temp[i]) == 0)
                    listaSalida = listaSalida + "-6";
                else if (Integer.parseInt(temp[i]) == 1)
                    listaSalida = listaSalida + "-0";
                else if (Integer.parseInt(temp[i]) == 2)
                    listaSalida = listaSalida + "-9";
            }

            if (i == 3) {
                if (Integer.parseInt(temp[i]) == 0)
                    listaSalida = listaSalida + "-9";
                else if (Integer.parseInt(temp[i]) == 1)
                    listaSalida = listaSalida + "-6";
            }

            if (i == 4) {
                if (Integer.parseInt(temp[i]) == 0)
                    listaSalida = listaSalida + "-0";
                else if (Integer.parseInt(temp[i]) == 1)
                    listaSalida = listaSalida + "-6";
                else if (Integer.parseInt(temp[i]) == 2)
                    listaSalida = listaSalida + "-9";
            }

            if (i == 5) {
                if (Integer.parseInt(temp[i]) == 0)
                    listaSalida = listaSalida + "-0";
                else if (Integer.parseInt(temp[i]) == 1)
                    listaSalida = listaSalida + "-3";
                else if (Integer.parseInt(temp[i]) == 2)
                    listaSalida = listaSalida + "-6";
                else if (Integer.parseInt(temp[i]) == 3)
                    listaSalida = listaSalida + "-9";
            }

            if (i == 6) {
                if (Integer.parseInt(temp[i]) == 0)
                    listaSalida = listaSalida + "-0";
                else if (Integer.parseInt(temp[i]) == 1)
                    listaSalida = listaSalida + "-3";
                else if (Integer.parseInt(temp[i]) == 2)
                    listaSalida = listaSalida + "-6";
                else if (Integer.parseInt(temp[i]) == 3)
                    listaSalida = listaSalida + "-9";
            }

            if (i == 7) {
                if (Integer.parseInt(temp[i]) == 0)
                    listaSalida = listaSalida + "-0";
                else if (Integer.parseInt(temp[i]) == 1)
                    listaSalida = listaSalida + "-6";
                else if (Integer.parseInt(temp[i]) == 2)
                    listaSalida = listaSalida + "-9";
            }

            if (i == 8) {
                if (Integer.parseInt(temp[i]) == 0)
                    listaSalida = listaSalida + "-0";
                else if (Integer.parseInt(temp[i]) == 1)
                    listaSalida = listaSalida + "-3";
                else if (Integer.parseInt(temp[i]) == 2)
                    listaSalida = listaSalida + "-6";
                else if (Integer.parseInt(temp[i]) == 3)
                    listaSalida = listaSalida + "-9";
            }

            if (i == 9) {
                if (Integer.parseInt(temp[i]) == 0)
                    listaSalida = listaSalida + "-0";
                else if (Integer.parseInt(temp[i]) == 1)
                    listaSalida = listaSalida + "-3";
                else if (Integer.parseInt(temp[i]) == 2)
                    listaSalida = listaSalida + "-6";
                else if (Integer.parseInt(temp[i]) == 3)
                    listaSalida = listaSalida + "-9";
            }

            if (i == 10) {
                if (Integer.parseInt(temp[i]) == 0)
                    listaSalida = listaSalida + "-0";
                else if (Integer.parseInt(temp[i]) == 1)
                    listaSalida = listaSalida + "-3";
                else if (Integer.parseInt(temp[i]) == 2)
                    listaSalida = listaSalida + "-6";
                else if (Integer.parseInt(temp[i]) == 3)
                    listaSalida = listaSalida + "-9";
            }

            if (i == 11) {
                if (Integer.parseInt(temp[i]) == 0)
                    listaSalida = listaSalida + "-0";
                else if (Integer.parseInt(temp[i]) == 1)
                    listaSalida = listaSalida + "-6";
                else if (Integer.parseInt(temp[i]) == 2)
                    listaSalida = listaSalida + "-9";
            }

            if (i == 12) {
                if (Integer.parseInt(temp[i]) == 0)
                    listaSalida = listaSalida + "-0";
                else if (Integer.parseInt(temp[i]) == 1)
                    listaSalida = listaSalida + "-0";
                else if (Integer.parseInt(temp[i]) == 2)
                    listaSalida = listaSalida + "-3";
                else if (Integer.parseInt(temp[i]) == 3)
                    listaSalida = listaSalida + "-6";
                else if (Integer.parseInt(temp[i]) == 4)
                    listaSalida = listaSalida + "-9";
                else if (Integer.parseInt(temp[i]) == 5)
                    listaSalida = listaSalida + "-9";
            }
        }

        return listaSalida;
    }
    // FIN: COPIA DEL HB VIEJO

    public static RespuestaMB cuotapartista(ContextoMB contexto) {
        boolean diaHabil = true;
        Boolean mostrarCuentasAnuladas = contexto.parametros.bool("mostrarCuentasAnuladas");

        // SERVICIOS EN PARALELO
        Futuro<Boolean> futuroEsDiaHabil = new Futuro<>(() -> Util.isDiaHabil(contexto));
        Futuro<ApiResponseMB> futuroResponse = new Futuro<>(() -> RestInversiones.cuotapartista(contexto, null, tipoDocEsco(contexto.persona()), null, mostrarCuentasAnuladas, contexto.persona().esPersonaJuridica() ? contexto.persona().cuit() : contexto.persona().numeroDocumento()));

        Map<String, Futuro<ApiResponseMB>> futurosPosicionCuotapartista = new HashMap<>();
        Map<String, Futuro<Boolean>> futurosEstaInhibido = new HashMap<>();
        for (Objeto cuotapartista : futuroResponse.get().objetos("CuotapartistaModel")) {
            if (cuotapartista.bool("EsFisico")) {
                String clave = cuotapartista.string("NumeroCuotapartista");

                Objeto item = new Objeto();
                item.set("iDCuotapartista", clave);

                Futuro<ApiResponseMB> futuroPosicionCuotapartista = new Futuro<>(() -> RestInversiones.posicionCuotapartista(contexto, Fecha.fechaActual(), "", item.integer("iDCuotapartista")));
                futurosPosicionCuotapartista.put(clave, futuroPosicionCuotapartista);

                Futuro<Boolean> futuroEstaInhibido = new Futuro<>(() -> estaInhibido(contexto, item));
                futurosEstaInhibido.put(clave, futuroEstaInhibido);
            }
        }
        // FIN SERVICIOS EN PARALELO

        ApiResponseMB response = futuroResponse.get();
        if (response.hayError() && !"1008".equals(response.string("codigo"))) {
            return RespuestaMB.error();
        }

        RespuestaMB respuesta = new RespuestaMB();
        Objeto itemConTenencias = new Objeto();
        Objeto itemSinTenencias = new Objeto();
        if (!futuroEsDiaHabil.get()) {
            diaHabil = false;
        }
        if (!diaHabil) {
            String diaNoHabil = " de lunes a viernes, siendo d&iacute;as h&aacute;biles";
            respuesta.set("diaHabil", diaNoHabil);
        }
        respuesta.set("esDiaHabil", diaHabil);

        for (Objeto cuotapartista : response.objetos("CuotapartistaModel")) {
            if (cuotapartista.bool("EsFisico")) {
                String clave = cuotapartista.string("NumeroCuotapartista");

                Objeto item = new Objeto();
                item.set("iDCuotapartista", cuotapartista.string("NumeroCuotapartista"));

                item.set("tieneTenencia", false);
                //ApiResponseMB posicionCuotapartista = RestInversiones.posicionCuotapartista(contexto, Fecha.fechaActual(), "", item.integer("iDCuotapartista"));
                ApiResponseMB posicionCuotapartista = futurosPosicionCuotapartista.get(clave).get();
                if (!posicionCuotapartista.hayError()) {
                    item.set("tieneTenencia", true);
                }

                BigDecimal cuotapartesValuadas = posicionCuotapartista
                        .objetos("PosicionCuotapartista").stream()
                        .map(o -> o.bigDecimal("CuotapartesValuadas"))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                item.set("cuotapartesValuadasTotales", cuotapartesValuadas);

                //item.set("estaInhibido", estaInhibido(contexto, item));
                item.set("estaInhibido", futurosEstaInhibido.get(clave).get());

                for (Objeto cuentas : cuotapartista.objeto("CuentasBancarias").objetos("CuotapartistaCuentaBancariaModel")) {
                    Cuenta cuenta = contexto.cuenta(cuentas.string("NumeroCuenta"));
                    if (cuenta != null) {
                        if (esMonedaValidaPorIDySimbolo(cuenta, cuentas)) {
                            Objeto itemCta = new Objeto();
                            itemCta.set("descripcion", cuentas.string("Descripcion"));
                            itemCta.set("idCuentaBancaria", cuentas.string("IDCuentaBancaria"));
                            itemCta.set("numeroCuenta", cuentas.string("NumeroCuenta"));
                            itemCta.set("simboloMoneda", cuenta.simboloMoneda());
                            itemCta.set("saldo", cuenta.saldo());
                            itemCta.set("saldoFormateado", cuenta.saldoFormateado());
                            itemCta.set("descripcionCorta", cuenta.descripcionCorta());
                            itemCta.set("numeroEnmascarado", cuenta.numeroEnmascarado());
                            itemCta.set("descripcionMoneda", cuentas.objeto("Moneda").string("Description"));
                            itemCta.set("idMoneda", cuentas.objeto("Moneda").string("ID"));
                            itemCta.set("descripcionTipoCuentaBancaria", cuentas.objeto("TipoCuentaBancaria").string("Description"));
                            item.add("ctasBancarias", itemCta);
                        }
                    }
                }
                item.set("esFisico", cuotapartista.bool("EsFisico"));
                item.set("fechaIngreso", cuotapartista.string("FechaIngreso"));
                item.set("estaAnulado", cuotapartista.bool("EstaAnulado"));
                item.set("nombre", cuotapartista.string("Nombre"));
                item.set("numeroCuotapartista", cuotapartista.string("NumeroCuotapartista"));
                item.set("perfil", cuotapartista.string("Perfil"));
                item.set("esDiaHabil", diaHabil);
                Objeto agenteColocador = new Objeto();
                agenteColocador.set("COD", cuotapartista.objeto("Radicacion").objeto("AgenteColocador").string("COD"));
                agenteColocador.set("descripcion", cuotapartista.objeto("Radicacion").objeto("AgenteColocador").string("Description"));
                item.add("agente colocador", agenteColocador);
                // respuesta.add("cuotapartistas", item);
                if (item.bool("tieneTenencia")) {
                    itemConTenencias.add(item);
                } else {
                    itemSinTenencias.add(item);
                }
            }
        }

        // Ordenar desc.
        List<Objeto> itemsConTenencias = itemConTenencias.objetos();
        Comparator<Objeto> comparator = new Comparator<Objeto>() {
            @Override
            public int compare(Objeto p1, Objeto p2) {
                return p2.bigDecimal("cuotapartesValuadasTotales")
                        .compareTo(p1.bigDecimal("cuotapartesValuadasTotales"));
            }
        };
        Collections.sort(itemsConTenencias, comparator);
        Objeto itemConTenenciasOrdenado = new Objeto();
        for (Objeto cuenta : itemsConTenencias) {
            itemConTenenciasOrdenado.add(cuenta);
        }

        itemSinTenencias.ordenar("iDCuotapartista");

        for (Objeto cuotapartista : itemConTenenciasOrdenado.objetos()) {
            respuesta.add("cuotapartistas", cuotapartista);
        }
        for (Objeto cuotapartista : itemSinTenencias.objetos()) {
            respuesta.add("cuotapartistas", cuotapartista);
        }

        return respuesta;
    }

    private static boolean esMonedaValidaPorIDySimbolo(Cuenta cuenta, Objeto cuentas) {
        String simboloMoneda = cuenta.simboloMoneda();
        String idMoneda = cuentas.objeto("Moneda").string("ID");

        return (simboloMoneda.equals("USD") && idMoneda.equals("2")) ||
                (simboloMoneda.equals("$") && idMoneda.equals("1"));
    }

    public static Boolean estaInhibido(ContextoMB contexto, Objeto item) {
        Boolean inhibido = false;
        ApiResponseMB fondoResponse = RestInversiones.fondos(contexto, item.integer("iDCuotapartista"), 0, null, "RE");
        if (!fondoResponse.hayError()) {
            if (fondoResponse.objetos("Table").get(0).integer("EstaInhibido") == -1) {
                inhibido = true;
            }
        }

        return inhibido;
    }

    public static Boolean tieneTenencia(ContextoMB contexto, Objeto item) {
        Boolean tenencia = false;
        ApiResponseMB posicionCuotapartista = RestInversiones.posicionCuotapartista(contexto, Fecha.fechaActual(), "", item.integer("iDCuotapartista"));
        if (!posicionCuotapartista.hayError()) {
            tenencia = true;
        }
        return tenencia;
    }

    public static RespuestaMB formulario(ContextoMB contexto) {
        String idAgColocador = contexto.parametros.string("idAgColocador");
        String idFondo = contexto.parametros.string("idFondo");
        String idSucursal = contexto.parametros.string("idSucursal");
        String idTpFormulario = contexto.parametros.string("idTpFormulario");
        String pCodLiquidacion = contexto.parametros.string("pCodLiquidacion");
        String pNumSolicitud = contexto.parametros.string("pNumSolicitud");

        ApiResponseMB response = RestInversiones.formulario(contexto, idAgColocador, idFondo, idSucursal, idTpFormulario, pCodLiquidacion, pNumSolicitud);
        if (response.hayError()) {
            return RespuestaMB.error();
        }
        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("pdf", response.objeto("GetFormularioResult").string("Response"));

        return respuesta;
    }

    public static RespuestaMB liquidaciones(ContextoMB contexto) {
        String fechaDesde = contexto.parametros.string("fechaDesde");
        String fechaHasta = contexto.parametros.string("fechaHasta");
        String iDCuotapartista = contexto.parametros.string("iDCuotapartista");
        String iDFondo = contexto.parametros.string("iDFondo");
        String iDTipoValorCuotaParte = contexto.parametros.string("iDTipoValorCuotaParte");
        Integer numeroCuotapartista = contexto.parametros.integer("numeroCuotapartista");

        ApiResponseMB response = RestInversiones.liquidaciones(contexto, fechaDesde, fechaHasta, iDCuotapartista, iDFondo, iDTipoValorCuotaParte, numeroCuotapartista);

        if (response.hayError()) {
            return RespuestaMB.error();
        }
        RespuestaMB respuesta = new RespuestaMB();
        for (Objeto objeto : response.objetos("Liquidaciones")) {
            Objeto liquidacion = new Objeto();
            liquidacion.set("tipoVCPAbreviatura", objeto.string("ttpoVCPAbreviatura"));
            liquidacion.set("idTipo", objeto.string("IDTIPO"));
            liquidacion.set("cuotapartistaNumero", objeto.string("CuotapartistaNumero"));
            liquidacion.set("cuotapartistaNombre", objeto.string("CuotapartistaNombre"));
            liquidacion.set("fondoNumero", objeto.string("FondoNumero"));
            liquidacion.set("fondoNombre", objeto.string("FondoNombre"));
            liquidacion.set("tipoVCPAbreviatura", objeto.string("TipoVCPAbreviatura"));
            liquidacion.set("tipoVCPDescripcion", objeto.string("TipoVCPDescripcion"));
            liquidacion.set("liquidacionTipoID", objeto.string("LiquidacionTipoID"));
            liquidacion.set("liquidacionNumero", objeto.string("LiquidacionNumero"));
            liquidacion.set("monedaSimbolo", objeto.string("MonedaSimbolo"));
            liquidacion.set("monedaDescripcion", objeto.string("MonedaDescripcion"));
            liquidacion.set("importeNeto", objeto.string("ImporteNeto"));
            liquidacion.set("importeBruto", objeto.string("ImporteBruto"));
            liquidacion.set("importeSolicitud", objeto.string("ImporteSolicitud"));
            liquidacion.set("cuotapartes", objeto.string("Cuotapartes"));
            liquidacion.set("vCPValor", objeto.string("VCPValor"));

            respuesta.set("liquidacion", liquidacion);
        }
        return respuesta;
    }

    private static String getTipoPersona(ContextoMB contexto) {
        String clase = "";
        if (contexto.persona().esPersonaFisica()) {
            if (contexto.persona().esEmpleado()) {
                clase = "Empleados ";
            } else {
                clase = "Individuos - Particulares";
            }
        } else if (contexto.persona().esPersonaJuridica()) {
            clase = "Personas Jurídicas";
        }
        return clase;
    }

    public static RespuestaMB posicionCuotapartista(ContextoMB contexto) {
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_fondos_fuera_de_horario", "prendido_fondos_fuera_de_horario_cobis")) {
            return posicionCuotapartistaV3(contexto);
        } else {
            String fecha = contexto.parametros.string("fecha");
            String idCuotapartista = contexto.parametros.string("idCuotapartista");
            Integer numeroCuotapartista = contexto.parametros.integer("numeroCuotapartista");
            BigDecimal totalFondosPesos = new BigDecimal(BigInteger.ZERO);
            BigDecimal totalFondosDolares = new BigDecimal(BigInteger.ZERO);

            String tipoPersona = getTipoPersona(contexto);

            List<Objeto> tablaParametria = SqlHomebanking.getFondosParametria("RE", tipoPersona);
            ApiResponseMB response = RestInversiones.posicionCuotapartista(contexto, fecha, idCuotapartista, numeroCuotapartista);

            if (response.hayError()) {
                if ("1016".equals(response.string("codigo"))) {
                    return RespuestaMB.estado("SIN_POSICION");
                }
                return RespuestaMB.error();
            }

            RespuestaMB respuesta = new RespuestaMB();
            for (Objeto objeto : response.objetos("PosicionCuotapartista")) {
                Objeto posicion = new Objeto();
                posicion.set("fondoNumero", objeto.string("FondoNumero"));
                posicion.set("fondoNombre", objeto.string("FondoNombre"));
                posicion.set("tipoVCPAbreviatura", objeto.string("TipoVCPAbreviatura"));
                posicion.set("tipoVCPDescripcion", objeto.string("TipoVCPDescripcion"));
                posicion.set("tipoVCPID", objeto.string("TipoVCPID"));
                posicion.set("IDCondicionIngEgr", objeto.string("IDCondicionIngEgr"));
                posicion.set("cuotapartistaID", objeto.string("CuotapartistaID"));
                posicion.set("cuotapartesTotales", objeto.bigDecimal("CuotapartesTotales"));
                posicion.set("cuotapartesTotalesFormateado", Formateador.importe(objeto.bigDecimal("CuotapartesTotales")));
                posicion.set("cuotapartesBloqueadas", objeto.bigDecimal("CuotapartesBloqueadas"));
                posicion.set("cuotapartesValuadas", objeto.bigDecimal("CuotapartesValuadas"));
                posicion.set("cuotapartesValuadasFormateado", Formateador.importe(objeto.bigDecimal("CuotapartesValuadas")));
                posicion.set("ultimoVCPValor", objeto.bigDecimal("UltimoVCPValor"));
                posicion.set("ultimoVCPValorFormateado", Formateador.importeCantDecimales(objeto.bigDecimal("UltimoVCPValor"), 6));
                posicion.set("ultimoVCPFecha", objeto.date("UltimoVCPFecha"));
                posicion.set("iDMoneda", objeto.string("IDMoneda"));
                posicion.set("monedaSimbolo", objeto.string("MonedaSimbolo"));
                posicion.set("monedaDescripcion", objeto.string("MonedaDescripcion"));

                Optional<Objeto> parametria = tablaParametria.stream().filter(p -> objeto.integer("FondoNumero").equals(p.integer("id_fondo"))).findFirst();
                if (parametria.isPresent()) {
                    posicion.set("hora_inicio", parametria.get().string("hora_inicio"));
                    posicion.set("hora_fin", parametria.get().string("hora_fin"));
                }

                for (Objeto fondos : tablaParametria) {
                    if (Integer.parseInt(objeto.string("FondoID")) == fondos.integer("id_fondo") && fondos.string("cond_ingreso_egreso").equals(objeto.string("IDCondicionIngEgr"))) {
                        posicion.set("montoMinimo", fondos.bigDecimal("min_a_operar"));
                        posicion.set("montoMinimoFormateado", Formateador.importe(fondos.bigDecimal("min_a_operar")));
                        if (!fondos.string("hora_inicio").isEmpty() && !fondos.string("hora_fin").isEmpty()) {
                            posicion.set("estaFueraDeHorario", comprobacionFueraDeHorario(fondos.string("hora_inicio"), fondos.string("hora_fin")));
                        }
                    }
                }

                respuesta.add("posicionCuotapartista", posicion);
                if ("PESO".equals(objeto.string("MonedaDescripcion"))) {
                    totalFondosPesos = totalFondosPesos.add(objeto.bigDecimal("CuotapartesValuadas"));
                } else {
                    totalFondosDolares = totalFondosDolares.add(objeto.bigDecimal("CuotapartesValuadas"));
                }

            }

            for (Objeto posicion : respuesta.objetos("posicionCuotapartista")) {
                if ("PESO".equals(posicion.string("monedaDescripcion"))) {
                    posicion.set("porcentaje", porcentaje(posicion.bigDecimal("cuotapartesValuadas"), totalFondosPesos));
                } else {
                    posicion.set("porcentaje", porcentaje(posicion.bigDecimal("cuotapartesValuadas"), totalFondosDolares));
                }
            }
            respuesta.set("totalFondosPesos", totalFondosPesos);
            respuesta.set("totalFondosPesosFormateado", Formateador.importe(totalFondosPesos));
            respuesta.set("totalFondosDolares", totalFondosDolares);
            respuesta.set("totalFondosDolaresFormateado", Formateador.importe(totalFondosDolares));

            return respuesta;
        }
    }

    private static String getDescripcionFondo(String idFondo) {
        return switch (idFondo) {
            case "1" ->
                    "Permite obtener un <strong>rendimiento superior al de fondos de renta fija a corto plazo</strong> gracias a su administración activa.";
            case "5" ->
                    "<strong> Rendimiento estable con riesgo bajo. </strong> Busca obtener una renta cercana a la tasa de interés para plazos fijos mayoristas a 30 días.";
            case "7" ->
                    "Busca la <strong>revalorización de las acciones de empresas argentinas.</strong> Ideal para inversiones de mediano a largo plazo.";
            case "9" ->
                    "Ideal para <strong>inversiones de corto plazo</strong> en pesos con rendimiento <strong>sin riesgo</strong>";
            case "10" ->
                    "Busca <strong>brindar cobertura</strong> frente a los movimientos del <strong>tipo de cambio.</strong>";
            case "11" ->
                    "Es el fondo en dólares más conservador de la familia Toronto Trust. A la fecha invierte principalmente en Bonos Chilenos (Soberanos/Corporativos), Bonos del Tesoro americano y Bonos de Brasil (Soberanos/Corporativos).";
            case "13" ->
                    "Invierte en <strong>activos argentinos de mediano o largo plazo.</strong> Opción de renta mixta en pesos.";
            case "14" ->
                    "Busca obtener <strong>rendimiento por encima de la inflación.</strong> Inversiones en títulos ajustables por CER y UVA.";
            case "15" ->
                    "Busca generar <strong>rendimiento en dólares</strong> invirtiendo principalmente en títulos de deuda soberana.";
            case "16" ->
                    "Su objetivo es obtener un rendimiento en pesos superior a la tasa de mercado de dinero con muy baja volatilidad, invirtiendo mayormente en instrumentos de deuda privados de corto plazo y de excelente calidad crediticia.";
            case "18" ->
                    "Busca <strong>generar rendimiento sobre la liquidez en dólares</strong> asumiendo una volatilidad moderada.";
            case "19" ->
                    "El fondo tiene como objetivo el financiamiento de proyectos de infraestructura en Argentina. El fondo invierte en sectores clave como transporte, energía, telecomunicaciones y servicios públicos, con el objetivo de mejorar la infraestructura nacional y promover el desarrollo económico sostenible.";
            case "20" ->
                    "Ideal para <strong>remunerar la liquidez en dólares sin riesgo</strong> de mercado y con <strong>liquidez inmediata</strong>, incluso días no hábiles.";
            default -> "";
        };
    }

    private static String getHorizonteFondo(String idFondo) {
        return switch (idFondo) {
            case "1" -> "Horizonte: de 1 a 3 años.";
            case "5" -> "Horizonte: de 0 a 1 año.";
            case "7" -> "Horizonte: 3 años en adelante.";
            case "9" -> "Horizonte: de 0 a 1 año.";
            case "10" -> "Horizonte: de 1 a 3 años.";
            case "11" -> "Horizonte: de 0 a 1 año.";
            case "13" -> "Horizonte: 3 años en adelante.";
            case "14" -> "Horizonte: de 1 a 3 años.";
            case "15" -> "Horizonte: 3 años en adelante.";
            case "16" -> "Horizonte: de 1 a 3 años.";
            case "18" -> "Horizonte: de 1 a 3 años.";
            case "19" -> "Horizonte: de 1 a 3 años.";
            case "20" -> "Horizonte: de 0 a 30 días.";
            default -> "";
        };
    }

    private static String getUrlDetalleFondo(String idFondo) {
        return switch (idFondo) {
            case "1" -> "https://www.torontotrust.com.ar/Bursatil/Fondo/4656/Toronto+Trust+-+Clase+B";
            case "5" -> "https://www.torontotrust.com.ar/Bursatil/Fondo/4528/Toronto+Trust+Renta+Fija+-+Clase+B";
            case "7" -> "https://www.torontotrust.com.ar/Bursatil/Fondo/4935/Toronto+Trust+Multimercado+-+Clase+B";
            case "9" -> "https://www.torontotrust.com.ar/Bursatil/Fondo/4422/Toronto+Trust+Ahorro+-+Clase+B";
            case "10" -> "https://www.torontotrust.com.ar/Bursatil/Fondo/5680/Toronto+Trust+Global+Capital+-+Clase+B";
            case "11" -> "https://www.torontotrust.com.ar/Bursatil/Fondo/5658/Toronto+Trust+Liquidez+Dolar+-+Clase+A";
            case "13" -> "https://www.torontotrust.com.ar/Bursatil/Fondo/7108/Toronto+Trust+Argentina+2021+-+Clase+B";
            case "14" -> "https://www.torontotrust.com.ar/Bursatil/Fondo/5151/Toronto+Trust+Retorno+Total+-+Clase+B";
            case "15" -> "https://www.torontotrust.com.ar/Bursatil/Fondo/5033/Toronto+Trust+Crecimiento+-+Clase+B";
            case "16" -> "https://www.torontotrust.com.ar/Bursatil/Fondo/7571/Toronto+Trust+Balanceado+-+Clase+a";
            case "18" -> "https://www.torontotrust.com.ar/Bursatil/Fondo/7745/Toronto+Trust+Renta+Dolar+-+Clase+B";
            case "19" -> "https://www.torontotrust.com.ar/Bursatil/Fondo/7748/Toronto+Trust+Infraestructura+-+Clase+B";
            case "20" ->
                    "https://www.torontotrust.com.ar/Bursatil/Fondo/7753/Toronto+Trust+Money+Market+D%C3%B3lar+-+Clase+B";
            default -> "";
        };
    }

    // ---- Helpers mínimos y genéricos ----
    private static String getOrEmpty(Objeto o, String k) {
        try { String v = (o == null) ? null : o.string(k); return (v == null) ? "" : v; }
        catch (Exception e) { return ""; }
    }
    private static Integer getOrNullInt(Objeto o, String k) {
        try { return (o == null) ? null : o.integer(k); } catch (Exception e) { return null; }
    }
    private static BigDecimal getOrZeroBD(Objeto o, String k) {
        try { BigDecimal b = (o == null) ? null : o.bigDecimal(k); return (b == null) ? BigDecimal.ZERO : b; }
        catch (Exception e) { return BigDecimal.ZERO; }
    }
    private static int parseIntOrZero(String s) {
        if (s == null) return 0;
        s = s.trim();
        if (s.isEmpty()) return 0;
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }
    private static boolean strEq(String a, String b) {
        String x = (a == null) ? "" : a.trim();
        String y = (b == null) ? "" : b.trim();
        return x.equals(y);
    }


    public static void agregarDetalleFondo(Objeto posicion, RespuestaMB responsePerfilInversor, List<Objeto> fondoList, String numFondo) {
        if (numFondo == null || numFondo.isBlank() || fondoList == null || fondoList.isEmpty()) {
            // completá igual textos base por número (si lo tenés)
            posicion.set("horizonteFondo",   getHorizonteFondo(numFondo));
            posicion.set("descripcionFondo", getDescripcionFondo(numFondo));
            posicion.set("composicionFondo", getUrlDetalleFondo(numFondo));
            return;
        }

        Objeto fondo = fondoList.stream()
                .filter(f -> Objects.equals(getOrEmpty(f, "FondoNumero"), numFondo))
                .findFirst()
                .orElse(null);

        if (fondo != null) {
            posicion.set("plazoLiquidacionFondo", getOrNullInt(fondo, "PlazoLiquidacionFondo"));

            String tpRiesgoDescripcionFondo = getOrEmpty(fondo, "TpRiesgoDescripcion");
            String tpRiesgoDescripcion = switch (tpRiesgoDescripcionFondo) {
                case "Perfil Conservador" -> "Bajo/Conservador";
                case "Perfil Moderado"    -> "Medio/Moderado";
                case "Perfl Arriesgado"   -> "Alto/Arriesgado";
                default -> "";
            };
            posicion.set("tpRiesgoDescripcion", tpRiesgoDescripcion);
            posicion.set("numeroCuentaFondo", getOrNullInt(fondo, "NumeroCuentaFondo"));
            posicion.set("fondoNombreAbr", getOrEmpty(fondo, "FondoNombreAbr"));

            // --- riesgo con fallbacks (evita IllegalArgumentException: 0)
            int codRiesgo = parseIntOrZero(getOrEmpty(fondo, "TpRiesgoCodInterfaz"));
            if (codRiesgo == 0) {
                int alt = parseIntOrZero(getOrEmpty(fondo, "TpRiesgoCodigo"));
                if (alt == 0) alt = parseIntOrZero(getOrEmpty(fondo, "tpRiesgoCodigo"));
                if (alt != 0) codRiesgo = alt;
            }
            if (codRiesgo == 0) {
                String d = tpRiesgoDescripcionFondo.toLowerCase();
                if (d.contains("bajo"))       codRiesgo = 1;
                else if (d.contains("medio")) codRiesgo = 2;
                else if (d.contains("alto"))  codRiesgo = 3;
            }
            if (codRiesgo < 1 || codRiesgo > 3) codRiesgo = 2; // default válido (Moderado)

            posicion.set("codigoMensajePerfilInversor",
                    validacionOperacionConPerfil(responsePerfilInversor, codRiesgo));
            posicion.set("tpRiesgoCodigo", String.valueOf(codRiesgo));
        }
        // Textos base (siempre)
        posicion.set("horizonteFondo",   getHorizonteFondo(numFondo));
        posicion.set("descripcionFondo", getDescripcionFondo(numFondo));
        posicion.set("composicionFondo", getUrlDetalleFondo(numFondo));
    }


    public static RespuestaMB posicionCuotapartistaV3(ContextoMB contexto) {
        String fecha = contexto.parametros.string("fecha");
        Integer numeroCuotapartista = contexto.parametros.integer("numeroCuotapartista");
        // TODO GB BUG15052024-3 - Ver porque lo mandan vacio.
        String idCuotapartista = (contexto.parametros.string("idCuotapartista") == null
                || contexto.parametros.string("idCuotapartista").isBlank()) && numeroCuotapartista != null
                ? numeroCuotapartista.toString()
                : contexto.parametros.string("idCuotapartista");


        boolean FFvariacionRendimiento = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "FFvariacionRendimiento", "FFvariacionRendimiento_cobis");

        BigDecimal totalPesos = new BigDecimal(BigInteger.ZERO);
        BigDecimal totalDolares = new BigDecimal(BigInteger.ZERO);
        BigDecimal totalRendimientoPesos = BigDecimal.ZERO;
        BigDecimal totalRendimientoDolares = BigDecimal.ZERO;
        boolean hayRendimientoPesos = false;
        boolean hayRendimientoDolares = false;

        int cantidadFciPesos = 0;
        int cantidadFciDolares = 0;

        // LLAMADAS EN PARALELO
        Futuro<ApiResponseMB> futuroRespVar = new Futuro<>(() -> RestInversiones.variacionFondos(contexto, fecha));
        Futuro<String> futuroTipoPersona = new Futuro<>(() -> getTipoPersona(contexto));
        Futuro<ApiResponseMB> futuroResponseCalendario = new Futuro<>(() -> RestCatalogo.calendarioFechaActual(contexto));
        Futuro<List<Objeto>> futuroOrdenesAgendadas = new Futuro<>(() -> SqlHomebanking.getOrdenesAgendadosFCIPorNumCuotaPartista(String.valueOf(numeroCuotapartista)));
        Futuro<ApiResponseMB> futuroResponsePosicionCuotapartista = new Futuro<>(() -> RestInversiones.posicionCuotapartista(contexto, fecha, idCuotapartista, numeroCuotapartista));
        Futuro<Boolean> futuroEsDiaHabil = new Futuro<>(() -> Util.isDiaHabil(contexto));
        String tipoPersona = futuroTipoPersona.get();
        Futuro<List<Objeto>> futuroTablaParametria = new Futuro<>(() -> SqlHomebanking.getFondosParametria("RE", tipoPersona));

        ApiResponseMB responseCalendario = futuroResponseCalendario.get();
        String fechaHasta = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String fechaDesdeUltimoDiaHabil = responseCalendario.objetos().get(0).string("diaHabilAnterior");
        Futuro<ApiResponseMB> futuroResponseSolicitudes = new Futuro<>(() -> RestInversiones.solicitudes(contexto, fechaDesdeUltimoDiaHabil, fechaHasta, null, null, null, null, null, numeroCuotapartista));
        Futuro<List<Objeto>> futuroConsolidadoMovsUltimoDHabil = new Futuro<>(() -> obtenerMovimientosFondosPorFecha(contexto, fecha, fechaDesdeUltimoDiaHabil));

        ApiResponseMB responseSolicitudes = futuroResponseSolicitudes.get();
        List<Objeto> consolidadoMovsUltimoDHabil = futuroConsolidadoMovsUltimoDHabil.get();

        List<Objeto> tablaParametria = futuroTablaParametria.get();
        List<Objeto> ordenesAgendadas = futuroOrdenesAgendadas.get();
        ApiResponseMB responsePosicionCuotapartista = futuroResponsePosicionCuotapartista.get();
        Boolean esDiaHabil = futuroEsDiaHabil.get();
        //VARIACION DE FONDOS


        Map<String, Objeto> mapaVariaciones = Collections.emptyMap();
        if (FFvariacionRendimiento) {
            try {
                ApiResponseMB respVar = futuroRespVar.get();
                if (!respVar.hayError()) {
                    List<Objeto> listaVariaciones = respVar.objetos();
                    if (listaVariaciones != null && !listaVariaciones.isEmpty()) {
                        mapaVariaciones = listaVariaciones.stream().collect(Collectors.toMap(
                                v -> v.string("numFondo") + "|" + v.string("abreviatura"),
                                v -> v
                        ));
                    }
                }
            } catch (Exception ignore) {
            }
        }

        // FIN LLAMADAS EN PARALELO

        if (responseCalendario.hayError()) {
            return null;
        }
        List<Objeto> solicitudesDesdeUltimoDiaHabil;

        if (responseSolicitudes.codigo == 504) {
            return RespuestaMB.estado("TIMEOUT");
        }

        if ((responseSolicitudes.hayError())) {
            if (!("1".equals(responseSolicitudes.string("codigo")))) {
                return RespuestaMB.error();
            }
        }

        solicitudesDesdeUltimoDiaHabil = responseSolicitudes.objetos("SolicitudGenericoModel").stream()
                .filter(solicitud -> solicitud.string("EstadoSolicitud").equalsIgnoreCase("No Requiere Autorización")
                        || solicitud.string("EstadoSolicitud").equalsIgnoreCase("Autorizado")
                        || solicitud.string("EstadoSolicitud").equalsIgnoreCase("Pendiente de Autorización"))
                .collect(Collectors.toList());

        // Ignoramos 1016 - SIN POSICION y evaluamos op. del dia.
        if (responsePosicionCuotapartista.hayError()
                && !"1016".equals(responsePosicionCuotapartista.string("codigo"))) {
            return RespuestaMB.error();
        }

        RespuestaMB responsePerfilInversor = perfilInversor(contexto);
        Set<String> fechasSet = new HashSet<>();
        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("esDiaHabil", esDiaHabil);
        int posicionCuota = 0;

        List<Objeto> posiciones = new ArrayList<>();
        int idtipoValorCuotaParte = 0;
        String tipoSolicitud = "RE";
        ApiResponseMB responseFondos = RestInversiones.fondos(
                contexto, Integer.parseInt(idCuotapartista), idtipoValorCuotaParte, null, tipoSolicitud);

        List<Objeto> fondoList = responseFondos.objetos("Table");
        BigDecimal totalRendimiento = BigDecimal.ZERO;
        BigDecimal sumaImportesDesde = BigDecimal.ZERO;

        for (Objeto itPosicion : responsePosicionCuotapartista.objetos("PosicionCuotapartista")) {
            Objeto posicion = new Objeto();
            posicion.set("rescateTotal", false);

            // Procesamos ordenes agendadas
            // Calculo de attr. operacionRescateAgenda
            DoubleSummaryStatistics rescateAgenda = ordenesAgendadas.stream()
                    .filter(ordenAgendada -> ordenAgendada.string("tipo_solicitud").equalsIgnoreCase("rescate")
                            && ordenAgendada.string("fondo_id").equalsIgnoreCase(itPosicion.string("FondoNumero")))
                    .mapToDouble(ordenAgendada -> {
                        if (ordenAgendada.string("importe").startsWith("0")) {
                            posicion.set("rescateTotal", true);
                            return 0;
                        } else {
                            return Double.parseDouble(ordenAgendada.string("importe"));
                        }
                    }).summaryStatistics();

            // Calculo de attr. operacionSuscripcionAgenda
            DoubleSummaryStatistics suscripcionAgenda = ordenesAgendadas.stream()
                    .filter(ordenAgendad -> ordenAgendad.string("tipo_solicitud").equalsIgnoreCase("Suscripcion")
                            && ordenAgendad.string("fondo_id").equalsIgnoreCase(itPosicion.string("FondoNumero")))
                    .mapToDouble(ordenAgendada -> Double.parseDouble(ordenAgendada.string("importe")))
                    .summaryStatistics();

            // Calculo de attr. operacionRescateSinProcesar
            DoubleSummaryStatistics rescateSinProcesar = solicitudesDesdeUltimoDiaHabil.stream()
                    .filter(solicitud -> solicitud.string("TipoSolicitud").equalsIgnoreCase("Rescate")
                            && solicitud.string("FondoID").equalsIgnoreCase(itPosicion.string("FondoNumero")))
                    .mapToDouble(ordenAgendada -> {
                        if (ordenAgendada.bool("EsTotal")) {
                            // Seteamos rescate total agendado que será descontado adelante de las
                            // cuotapartes totales junto con eventual rescate total por operacion
                            // en proceso.
                            posicion.set("rescateTotal", true);
                            return 0;
                        } else {
                            return Double.parseDouble(ordenAgendada.string("Importe"));
                        }
                    }).summaryStatistics();

            agregarDetalleFondo(posicion, responsePerfilInversor, fondoList, itPosicion.string("FondoNumero"));

            posicion.set("fondoNumero", itPosicion.string("FondoNumero"));
            posicion.set("fondoNombre", nombreFondoLimpio(itPosicion.string("FondoNombre")));
            posicion.set("tipoVCPAbreviatura", itPosicion.string("TipoVCPAbreviatura"));
            posicion.set("tipoVCPDescripcion", itPosicion.string("TipoVCPDescripcion"));
            posicion.set("tipoVCPID", itPosicion.string("TipoVCPID"));
            posicion.set("IDCondicionIngEgr", itPosicion.string("IDCondicionIngEgr"));
            posicion.set("cuotapartistaID", itPosicion.string("CuotapartistaID"));
            posicion.set("cuotapartesTotales", itPosicion.bigDecimal("CuotapartesTotales"));
            posicion.set("cuotapartesTotalesFormateado",
                    Formateador.importe(itPosicion.bigDecimal("CuotapartesTotales")));
            posicion.set("cuotapartesBloqueadas", itPosicion.bigDecimal("CuotapartesBloqueadas"));
            posicion.set("operacionRescateAgenda", rescateAgenda.getSum());
            posicion.set("operacionSuscripcionAgenda", suscripcionAgenda.getSum()); //
            // Cuotapartes sin descuentos de rescates.
            posicion.set("cuotapartesValuadasEfectivas", itPosicion.bigDecimal("CuotapartesValuadas"));
            posicion.set("cuotapartesValuadas", itPosicion.bigDecimal("CuotapartesValuadas"));
            posicion.set("cuotapartesValuadasFormateado",
                    Formateador.importe(itPosicion.bigDecimal("CuotapartesValuadas")));
            // Inicialización de totales. Luego sumamos suscripciones.
            posicion.set("totalCalculado", itPosicion.bigDecimal("CuotapartesValuadas"));
            posicion.set("totalCalculadoFormateado", Formateador.importe(itPosicion.bigDecimal("CuotapartesValuadas")));
            // Valor que muestra el FE.
            posicion.set("operacionRescateSinProcesar", rescateSinProcesar.getSum()); //
            // Lo calculamos adelante.
            posicion.set("operacionSuscripcionSinProcesar", BigDecimal.ZERO);
            posicion.set("ultimoVCPValor", itPosicion.bigDecimal("UltimoVCPValor"));
            posicion.set("ultimoVCPValorFormateado",
                    Formateador.importeCantDecimales(itPosicion.bigDecimal("UltimoVCPValor"), 6));
            String fechaUltimoVCP;
            if (!ConfigMB.esProduccion().booleanValue()) {
                fechaUltimoVCP = Mock.aumentarFecha(itPosicion.date("UltimoVCPFecha", "yyyy-MM-dd", "dd/MM/yyyy"),
                        posicionCuota);
                posicionCuota++;
            } else {
                fechaUltimoVCP = itPosicion.date("UltimoVCPFecha", "yyyy-MM-dd", "dd/MM/yyyy");
            }
            posicion.set("ultimoVCPFecha", fechaUltimoVCP);
            if (posicion.get("ultimoVCPFecha") != null) {
                fechasSet.add(posicion.get("ultimoVCPFecha").toString());
            }
            posicion.set("iDMoneda", itPosicion.string("IDMoneda"));
            posicion.set("monedaSimbolo", itPosicion.string("MonedaSimbolo"));
            posicion.set("monedaDescripcion", itPosicion.string("MonedaDescripcion"));
            for (Objeto fondos : tablaParametria) {
                if (Integer.parseInt(itPosicion.string("FondoID")) == fondos.integer("id_fondo")
                        && fondos.string("cond_ingreso_egreso").equals(itPosicion.string("IDCondicionIngEgr"))) {
                    posicion.set("montoMinimo", fondos.bigDecimal("min_a_operar"));
                    posicion.set("montoMinimoFormateado", Formateador.importe(fondos.bigDecimal("min_a_operar")));
                    if (!fondos.string("hora_inicio").isEmpty() && !fondos.string("hora_fin").isEmpty()) {
                        posicion.set("estaFueraDeHorario",
                                comprobacionFueraDeHorario(fondos.string("hora_inicio"), fondos.string("hora_fin")));
                        posicion.set("hora_inicio", fondos.string("hora_inicio"));
                        posicion.set("hora_fin", fondos.string("hora_fin"));
                        posicion.set("esDiaHabil", esDiaHabil);
                    }
                }
            }

            if (FFvariacionRendimiento) {
                String claveVariacion = itPosicion.string("FondoNumero") + "|" + itPosicion.string("TipoVCPAbreviatura");
                Objeto datosVar = mapaVariaciones.get(claveVariacion);
                BigDecimal variacion = datosVar != null ? datosVar.bigDecimal("variacion") : BigDecimal.ZERO;
                posicion.set("variacion", variacion);
                posicion.set("variacionFormateada", Formateador.importe(variacion, 2));

                int numFondoInt = Integer.parseInt(itPosicion.string("FondoNumero"));
                if (datosVar != null && (numFondoInt == 9 || numFondoInt == 20)) {
                    BigDecimal tna = datosVar.bigDecimal("tna");
                    posicion.set("TNA", tna);
                    posicion.set("TNAFormateado", Formateador.importe(tna, 2));
                } else {
                    posicion.set("TNA", null);
                    posicion.set("TNAFormateado", null);
                }

                BigDecimal valorInvertido = posicion.bigDecimal("totalCalculado");
                BigDecimal rendimientoDiario = valorInvertido
                        .multiply(variacion)
                        .divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
                posicion.set("rendimientoDiario", rendimientoDiario);
                posicion.set("rendimientoDiarioFormateado", Formateador.importe(rendimientoDiario, 2));

                EnumMonedaInversion moneda = EnumMonedaInversion.id(posicion.integer("iDMoneda"));
                if (moneda.esPeso()) {
                    totalRendimientoPesos = totalRendimientoPesos.add(rendimientoDiario);
                    hayRendimientoPesos |= rendimientoDiario.compareTo(BigDecimal.ZERO) != 0;
                } else {
                    totalRendimientoDolares = totalRendimientoDolares.add(rendimientoDiario);
                    hayRendimientoDolares |= rendimientoDiario.compareTo(BigDecimal.ZERO) != 0;
                }
            }

            posiciones.add(posicion);
        }

        List<Objeto> suscripcionesNuevas = new ArrayList<>();

        consolidadoMovsUltimoDHabil.forEach(consolidadaMov -> {
            if (consolidadaMov.string("cuotapartistaID").equals(idCuotapartista)) {


                // Suscripciones/Rescates de FCI's con tenencias previas.
                posiciones.stream().forEach(posicion -> {
                    if (posicion.string("fondoNumero").equals(consolidadaMov.string("fondoNumero"))
                            && posicion.string("cuotapartistaID").equals(consolidadaMov.string("cuotapartistaID"))) {
                        // Suscripcion
                        if (consolidadaMov.string("tipoSolicitud").equalsIgnoreCase("Suscripcion")) {
                            posicion.set("totalCalculado", posicion.bigDecimal("totalCalculado")
                                    .add(consolidadaMov.bigDecimal("totalCalculado")));
                            posicion.set("operacionSuscripcionSinProcesar",
                                    posicion.bigDecimal("operacionSuscripcionSinProcesar")
                                            .add(consolidadaMov.bigDecimal("totalCalculado")));
                        }

                        // Rescates PARCIALES: notar que sumamos porque viene el valor negativo.
                        // Rescate TOTAL: procesado despues.
                        if (consolidadaMov.string("tipoSolicitud").equalsIgnoreCase("Rescate")) {
                            posicion.set("totalCalculado", posicion.bigDecimal("totalCalculado")
                                    .add(consolidadaMov.bigDecimal("totalCalculado"))); // agregado
                            posicion.set("cuotapartesValuadas", posicion.bigDecimal("cuotapartesValuadas")
                                    .add(consolidadaMov.bigDecimal("totalCalculado")));
                            posicion.set("cuotapartesValuadasFormateado",
                                    Formateador.importe(posicion.bigDecimal("cuotapartesValuadas")));
                        }
                    }
                });

                // Suscripciones de FCI's nuevos.
                if (!posiciones.stream()
                        .anyMatch(p -> p.string("fondoNumero").equals(consolidadaMov.string("fondoNumero"))
                                && p.string("cuotapartistaID").equals(consolidadaMov.string("cuotapartistaID")))
                        && consolidadaMov.string("tipoSolicitud").equalsIgnoreCase("Suscripcion")) {
                    agregarDetalleFondo(consolidadaMov, responsePerfilInversor, fondoList, consolidadaMov.string("fondoNumero"));
                    suscripcionesNuevas.add(consolidadaMov);
                }

            }
        });

        Map<String, Objeto> finalMapaVariaciones = mapaVariaciones;
        suscripcionesNuevas.forEach(x -> {
            String numFondo = x.string("fondoNumero");
            x.set("descripcionFondo", getDescripcionFondo(numFondo));
            x.set("horizonteFondo", getHorizonteFondo(numFondo));
            x.set("composicionFondo", getUrlDetalleFondo(numFondo));
            if (FFvariacionRendimiento) {
                String abrev = inferirAbreviatura(numFondo, x.string("tipoVCPAbreviatura"), posiciones, finalMapaVariaciones);
                if (abrev != null && !abrev.isBlank()) {
                    x.set("tipoVCPAbreviatura", abrev);
                }
                setTnaSiMoneyMarket(x, numFondo, abrev, finalMapaVariaciones);
            }
            posiciones.add(x);
        });


        posiciones.forEach(posicion -> {
            // Evaluamos rescates totales.
            if (posicion.bool("rescateTotal", false)) {
                posicion.set("cuotapartesValuadas", BigDecimal.ZERO);
                posicion.set("cuotapartesValuadasFormateado", Formateador.importe(BigDecimal.ZERO));
                // Si hubo rescate total, el total será lo pendiente de procesar
                // (o cero si no hubo suscripciones).
                BigDecimal totalConRescateTotal = posicion.bigDecimal("operacionSuscripcionSinProcesar")
                        .add(posicion.bigDecimal("operacionSuscripcionAgenda"));
                posicion.set("totalCalculado", totalConRescateTotal);
            }
            posicion.set("totalCalculadoFormateado", Formateador.importe(posicion.bigDecimal("totalCalculado")));
            posicion.set("porcentajeDisponible",
                    porcentaje(posicion.bigDecimal("cuotapartesValuadas") != null
                            ? posicion.bigDecimal("cuotapartesValuadas")
                            : BigDecimal.ZERO, posicion.bigDecimal("totalCalculado")));
            respuesta.add("posicionCuotapartista", posicion);
        });

        for (Objeto posicion : posiciones) {
            Integer idMon = posicion.integer("iDMoneda", 1); // default seguro
            EnumMonedaInversion moneda = EnumMonedaInversion.id(idMon);
            if (moneda.esPeso()) {
                cantidadFciPesos++;
                totalPesos = totalPesos.add(posicion.bigDecimal("totalCalculado"));
            } else if (moneda.esDolar()) {
                cantidadFciDolares++;
                totalDolares = totalDolares.add(posicion.bigDecimal("totalCalculado"));
            }
        }

        for (Objeto posicion : posiciones) {
            Integer idMon = posicion.integer("iDMoneda", 1); // default seguro
            EnumMonedaInversion moneda = EnumMonedaInversion.id(idMon);
            posicion.set("porcentaje", porcentaje(posicion.bigDecimal("totalCalculado"),
                    moneda.esPeso() ? totalPesos : totalDolares, 2, Optional.of(RoundingMode.HALF_UP)));
        }

        List<Objeto> posicionesOrdenadas = Optional
                .ofNullable(respuesta.objetos("posicionCuotapartista"))
                .orElse(Collections.emptyList())
                .stream()
                .sorted(
                        Comparator
                                .comparingInt((Objeto p) -> {
                                    int n = Integer.parseInt(p.string("fondoNumero"));
                                    return (n == 9 || n == 20) ? 0 : 1;
                                })
                                .thenComparing(
                                        (Objeto p) -> {
                                            BigDecimal tc = p.bigDecimal("totalCalculado");
                                            return tc != null ? tc : BigDecimal.ZERO;
                                        },
                                        Comparator.reverseOrder()
                                )
                )
                .collect(Collectors.toList());

        respuesta.setNull("posicionCuotapartista");
        for (Objeto posicion : posicionesOrdenadas) {
            respuesta.add("posicionCuotapartista", posicion);
        }

        if (posicionesOrdenadas.isEmpty()) {
            RespuestaMB r = RespuestaMB.estado("SIN_POSICION");
            r.set("nuevoFlujoFCI", FFvariacionRendimiento);
            return r;
        }


        BigDecimal porcentajeTotalPesos = totalPesos.compareTo(BigDecimal.ZERO) > 0
                ? totalRendimientoPesos.divide(totalPesos, 6, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;
        BigDecimal porcentajeTotalDolares = totalDolares.compareTo(BigDecimal.ZERO) > 0
                ? totalRendimientoDolares.divide(totalDolares, 6, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        respuesta.set("cantidadFciPesos", cantidadFciPesos);
        respuesta.set("cantidadFciDolares", cantidadFciDolares);
        // BUG-INV-13052024-392	fechaServiceLayer en posicionConsolidadaV2
        // se devuelve con fecha incorrecta (calculo incorrecto de fecha maxima)
        if (!fechasSet.isEmpty()) {
            respuesta.set("fechaServiceLayer", Collections.max(fechasSet));
        }
        respuesta.set("fechaConsulta", fecha);
        respuesta.set("totalFondosPesos", totalPesos);
        respuesta.set("totalFondosPesosFormateado", Formateador.importe(totalPesos));
        respuesta.set("totalFondosDolares", totalDolares);
        respuesta.set("totalFondosDolaresFormateado", Formateador.importe(totalDolares));

        if (FFvariacionRendimiento) {
            if (hayRendimientoPesos) {
                respuesta.set("rendimientoTotalPesos", totalRendimientoPesos);
                respuesta.set("rendimientoTotalPesosFormateado", Formateador.importe(totalRendimientoPesos.abs(), 2));
                respuesta.set("porcentajeRendimientoTotalPesos", porcentajeTotalPesos);
                respuesta.set("porcentajeRendimientoTotalPesosFormateado", Formateador.importe(porcentajeTotalPesos.abs(), 2));
            }

            if (hayRendimientoDolares) {
                respuesta.set("rendimientoTotalDolares", totalRendimientoDolares);
                respuesta.set("rendimientoTotalDolaresFormateado", Formateador.importe(totalRendimientoDolares.abs(), 2));
                respuesta.set("porcentajeRendimientoTotalDolares", porcentajeTotalDolares);
                respuesta.set("porcentajeRendimientoTotalDolaresFormateado", Formateador.importe(porcentajeTotalDolares.abs(), 2));
            }
        }

        respuesta.set("nuevoFlujoFCI", FFvariacionRendimiento);

        return respuesta;
    }


    private static String inferirAbreviatura(
            String numFondo,
            String abrevEnMovimiento,
            List<Objeto> posiciones,
            Map<String, Objeto> mapaVariaciones
    ) {
        if (abrevEnMovimiento != null && !abrevEnMovimiento.isBlank()) return abrevEnMovimiento;

        for (Objeto p : posiciones) {
            if (numFondo.equals(p.string("fondoNumero"))) {
                String a = p.string("tipoVCPAbreviatura");
                if (a != null && !a.isBlank()) return a;
            }
        }

        String prefijo = numFondo + "|";
        for (String k : mapaVariaciones.keySet()) {
            if (k.startsWith(prefijo)) {
                String a = k.substring(prefijo.length());
                if (a != null && !a.isBlank()) return a;
            }
        }
        return null;
    }

    // Pone SOLO TNA si el fondo es Money Market (9 o 20). Si no hay dato en variaciones, deja nulls.
    private static void setTnaSiMoneyMarket(
            Objeto target,
            String numFondo,
            String abreviatura,
            Map<String, Objeto> mapaVariaciones
    ) {
        if (abreviatura == null || abreviatura.isBlank()) return;

        int n;
        try {
            n = Integer.parseInt(numFondo);
        } catch (NumberFormatException e) {
            n = -1;
        }
        if (n != 9 && n != 20) return;

        Objeto var = mapaVariaciones.get(numFondo + "|" + abreviatura);
        if (var != null && var.existe("tna")) {
            BigDecimal tna = var.bigDecimal("tna");
            target.set("TNA", tna);
            target.set("TNAFormateado", Formateador.importe(tna, 2));
        } else {
            target.set("TNA", null);
            target.set("TNAFormateado", null);
        }
    }


    public static List<Objeto> obtenerMovimientosFondosPorFecha(ContextoMB contexto, String fecha, String fechaDesde) {

        ApiResponseMB cuotapartistaResponse = RestInversiones.cuotapartista(contexto, null, tipoDocEsco(contexto.persona()), null, false, contexto.persona().esPersonaJuridica() ? contexto.persona().cuit() : contexto.persona().numeroDocumento());

        List<Objeto> movimientos = new ArrayList<>();
        List<Objeto> consolidado = new ArrayList<>();
        List<Objeto> fondosOrdenesAgendadas = SqlHomebanking.getOrdenesAgendadasFCIPorEstadoAndCobis("Agendada",
                contexto.idCobis());

        fondosOrdenesAgendadas.stream()
                .forEach(i -> {
                    Objeto o = new Objeto();
                    o.set("fondoNumero", i.string("fondo_id"));
                    o.set("cuotapartistaID", i.string("cuotapartista"));
                    o.set("tipoSolicitud", i.string("tipo_solicitud"));
                    o.set("totalCalculado",
                            i.string("tipo_solicitud").equalsIgnoreCase("Suscripcion") ? i.bigDecimal("importe")
                                    : i.bigDecimal("importe").negate());
                    movimientos.add(o);
                });

        try {
            for (Objeto cuotapartista : cuotapartistaResponse.objetos("CuotapartistaModel")) {

                ApiResponseMB responseFondosCuotapartista = RestInversiones.fondos(contexto, cuotapartista.integer("NumeroCuotapartista"), 0,
                        null, "SU");
                List<Objeto> tablaParametria = SqlHomebanking.getFondosParametria("SU", getTipoPersona(contexto));
                List<Objeto> fondos = new ArrayList<>();
                // Información del fondo
                for (Objeto detallesFondo : responseFondosCuotapartista.objetos("Table")) {
                    if (detallesFondo.string("TipoVCPDescripcion")
                            .equals(tablaParametria.get(0).string("tipo_vcp_descripcion"))) {
                        Objeto fondo = new Objeto();
                        for (Objeto fondoParametria : tablaParametria) {
                            if (detallesFondo.integer("FondoID").equals(fondoParametria.integer("id_fondo")) && detallesFondo
                                    .string("CondicionIngresoEgresoID").equals(fondoParametria.string("cond_ingreso_egreso"))) {
                                fondo.set("fondoNumero", detallesFondo.integer("FondoNumero"));
                                fondo.set("fondoNombre", nombreFondoLimpio(detallesFondo.string("FondoNombre")));
                                fondo.set("fondoNombreAbr", detallesFondo.string("FondoNombreAbr"));
                                fondo.set("tipoVCPAbreviatura", detallesFondo.string("TipoVCPAbreviatura"));
                                fondo.set("tipoVCPID", detallesFondo.string("TipoVCPID"));
                                fondo.set("hora_inicio", fondoParametria.string("hora_inicio"));
                                fondo.set("hora_fin", fondoParametria.string("hora_fin"));
                                fondo.set("monedaDescripcion", fondoParametria.string("moneda_descripcion"));
                            }
                        }
                        if (fondo.existe("fondoNumero")) {
                            fondo.set("tipoVCPDescripcion", detallesFondo.string("TipoVCPDescripcion"));
                            fondos.add(fondo);
                        }
                    }
                }

                ApiResponseMB responseSolicitudes = RestInversiones.solicitudes(contexto, fechaDesde, fecha, null, null,
                        null, null, null, cuotapartista.integer("NumeroCuotapartista"));
                List<Objeto> solicitudes = responseSolicitudes.objetos("SolicitudGenericoModel").stream().filter(
                                solicitud -> solicitud.string("EstadoSolicitud").equalsIgnoreCase("No Requiere Autorización")
                                        || solicitud.string("EstadoSolicitud").equalsIgnoreCase("Autorizado")
                                        || solicitud.string("EstadoSolicitud").equalsIgnoreCase("Pendiente de Autorización"))
                        .collect(Collectors.toList());

                solicitudes.forEach(solicitud -> {
                    Objeto movimiento = new Objeto();
                    BigDecimal importe = solicitud.bigDecimal("Importe") != null ? solicitud.bigDecimal("Importe") : BigDecimal.ZERO;
                    movimiento.set("fondoNumero", solicitud.string("FondoID"));
                    movimiento.set("cuotapartistaID", solicitud.string("CuotapartistaID"));
                    movimiento.set("tipoSolicitud", solicitud.string("TipoSolicitud"));
                    movimiento.set("totalCalculado",
                            solicitud.string("TipoSolicitud").equalsIgnoreCase("Suscripcion") ? importe : importe.negate());
                    movimientos.add(movimiento);
                });

                consolidado = movimientos.stream()
                        .collect(Collectors.groupingBy(su -> su.integer("fondoNumero") + "_"
                                + su.string("cuotapartistaID") + "_" + su.string("tipoSolicitud")))
                        .entrySet()
                        .stream()
                        .map(e ->
                                e.getValue().stream().reduce((s1, s2) -> {
                                    Objeto x = new Objeto();
                                    x.set("fondoNumero", s1.string("fondoNumero"));
                                    x.set("cuotapartistaID", s1.string("cuotapartistaID"));
                                    x.set("tipoSolicitud", s1.string("tipoSolicitud"));
                                    x.set("totalCalculado",
                                            s1.bigDecimal("totalCalculado").add(s2.bigDecimal("totalCalculado")));
                                    return x;
                                }))
                        .map(i -> i.get())
                        .toList();

                consolidado.forEach(x -> {
                    x.set("totalCalculadoFormateado", Formateador.importe(x.bigDecimal("totalCalculado")));
                    Optional<Objeto> fondo = fondos.stream()
                            .filter(f -> f.integer("fondoNumero").equals(x.integer("fondoNumero"))).findFirst();
                    if (fondo.isPresent()) {
                        x.set("fondoNombre", fondo.get().string("fondoNombre"));
                        //
                        x.set("tipoVCPAbreviatura", fondo.get().string("tipoVCPAbreviatura"));
                        x.set("tipoVCPDescripcion", fondo.get().string("tipoVCPDescripcion"));
                        x.set("IDCondicionIngEgr", "");
                        x.set("cuotapartesBloqueadas", BigDecimal.ZERO);
                        x.set("operacionRescateAgenda", BigDecimal.ZERO);
                        x.set("operacionSuscripcionAgenda", BigDecimal.ZERO);
                        x.set("operacionRescateSinProcesar", BigDecimal.ZERO);
                        x.set("ultimoVCPValor", BigDecimal.ZERO);
                        x.set("ultimoVCPValorFormateado", Formateador.importe(BigDecimal.ZERO));
                        x.set("montoMinimo", BigDecimal.ZERO);
                        x.set("montoMinimoFormateado", Formateador.importe(BigDecimal.ZERO));
                        if ("PESO".equals(fondo.get().string("monedaDescripcion"))) {
                            x.set("iDMoneda", "1");
                            x.set("monedaSimbolo", "$");
                        } else {
                            x.set("iDMoneda", "2");
                            x.set("monedaSimbolo", "DOLAR");
                        }
                        x.set("estaFueraDeHorario", comprobacionFueraDeHorario(fondo.get().string("hora_inicio"),
                                fondo.get().string("hora_fin")));
                        x.set("esDiaHabil", Util.isDiaHabil(contexto));
                        x.set("porcentaje", BigDecimal.ZERO);
                        x.set("cuotapartesTotales", BigDecimal.ZERO);
                        x.set("cuotapartesTotalesFormateado", Formateador.importe(BigDecimal.ZERO));
                        //
                        x.set("monedaDescripcion", fondo.get().string("monedaDescripcion"));
                        x.set("tipoVCPID", fondo.get().string("tipoVCPID"));
                        x.set("hora_inicio", fondo.get().string("hora_inicio"));
                        x.set("hora_fin", fondo.get().string("hora_fin"));
                        x.set("cuotapartesValuadas", BigDecimal.ZERO);
                        x.set("cuotapartesValuadasFormateado", Formateador.importe(BigDecimal.ZERO));
                        x.set("rescateTotal", Boolean.FALSE);
                    }
                });

            }
        } catch (Exception e) {
        }
        return consolidado;

    }

    public static RespuestaMB solicitudesFci(ContextoMB contexto) {
        String fechaDesde = contexto.parametros.string("fechaDesde");
        String fechaHasta = contexto.parametros.string("fechaHasta");
        Integer numeroCuotapartista = contexto.parametros.integer("numeroCuotapartista");

        ApiResponseMB response = RestInversiones.solicitudes(contexto, fechaDesde, fechaHasta, null, null, null, null, null, numeroCuotapartista);

        if (response.hayError()) {
            if ("1".equals(response.string("codigo"))) {
                return RespuestaMB.estado("SIN_SOLICITUDES");
            }
            return RespuestaMB.error();
        }
        RespuestaMB respuesta = new RespuestaMB();
        List<Objeto> solicitudes = new ArrayList<>();

        for (Objeto item : response.objetos("SolicitudGenericoModel")) {
            Objeto solicitud = new Objeto();
            solicitud.set("cuotapartistaNombre", item.string("CuotapartistaNombre"));
            solicitud.set("cuotapartistaNumero", item.integer("CuotapartistaNumero"));
            solicitud.set("esTotal", item.bool("EsTotal"));
            solicitud.set("estadoSolicitud", item.string("EstadoSolicitud"));
            solicitud.set("fechaConcertacion", item.date("FechaConcertacion", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yy"));
            solicitud.set("fondoID", item.string("FondoID"));
            solicitud.set("fondoNombre", item.string("FondoNombre"));
            solicitud.set("fondoNombreAbr", item.string("FondoNombreAbr"));
            solicitud.set("importe", item.bigDecimal("Importe"));
            solicitud.set("importeFormateado", Formateador.importeCantDecimales(item.bigDecimal("Importe"), 2));
            solicitud.set("monedaDescripcion", item.string("MonedaDescripcion"));
            solicitud.set("monedaSimbolo", item.string("MonedaSimbolo"));
            solicitud.set("numSolicitud", item.string("NumSolicitud"));
            solicitud.set("origenSolicitud", item.string("OrigenSolicitud"));
            solicitud.set("tipoSolicitud", item.string("TipoSolicitud"));

            solicitudes.add(solicitud);
        }

        Fecha.ordenarPorFechaDesc(solicitudes, "fechaConcertacion", "dd/MM/yy");
        respuesta.set("solicitudes", solicitudes);
        return respuesta;
    }

    public static RespuestaMB obtenerSolicitudes(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        ArrayList<Objeto> notificacionesAL = new ArrayList<>();
        try {

            ApiResponseMB responseCalendario = RestCatalogo.calendarioFechaActual(contexto);
            if (responseCalendario.hayError()) {
                return RespuestaMB.error();
            }
            String fechaHasta = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String fechaDesde = responseCalendario.objetos().get(0).string("diaHabilAnterior");

            RespuestaMB cuotapartista = cuotapartista(contexto);

            cuotapartista.objetos("cuotapartistas").forEach(c -> {
                List<Objeto> solis = solicitudes(contexto, c.integer("iDCuotapartista"), fechaDesde, fechaHasta);
                //Filtrar ordenes agendadas por fecha, setear fechadesde de acuerdo a lo que pidan
                List<Objeto> notificaciones = Util.getOrUpdateNotificacionSolicitudes(contexto, solis, c.string("iDCuotapartista"));

                notificaciones.stream().filter(x -> x.integer("borrado").equals(0)).forEach(n -> {
                    Objeto notif = new Objeto();
                    notif.set("type", "INV");
                    notif.set("read", n.integer("leido").equals(1));
                    notif.set("numeroCaso", n.get("solicitud"));
                    notif.set("title", ConfigMB.string("mb_inv_title"));
                    notif.set("parte1", String.format(ConfigMB.string("mb_inv_parte1"), n.string("estado"), n.bigDecimal("importe"), n.bool("estadoSolicitud") ? "agendada" : "ingresada"));
                    notif.set("estado", "SUSCRIPCION");
                    notificacionesAL.add(notif);
                });
            });
            respuesta.set("notificacionesAL", notificacionesAL);
        } catch (Exception e) {
            return RespuestaMB.error();
        }

        return respuesta;
    }

    public static List<Objeto> solicitudes(ContextoMB contexto, Integer numeroCuotapartista, String fechaDesde, String fechaHasta) {
        List<Objeto> solicitudes = new ArrayList<>();
        try {
            ApiResponseMB response = RestInversiones.solicitudes(contexto, fechaDesde, fechaHasta, null, null, null, null, null, numeroCuotapartista);
            List<Objeto> ordenesAgendadas = SqlHomebanking.getOrdenesAgendadosFCIPorNumCuotaPartista(String.valueOf(numeroCuotapartista));

            solicitudes.addAll(response.objetos("SolicitudGenericoModel").stream().map(s -> {
                Objeto o = new Objeto();
                o.set("llave", s.string("NumSolicitud") + "_" + s.string("CuotapartistaID") + "_" + s.string("FondoID"));
                o.set("idSolicitud", s.string("IDSolicitud"));
                o.set("cobis_id", contexto.idCobis());
                o.set("cuotapartista", s.string("CuotapartistaID"));
                o.set("fecha", s.date("FechaConcertacion", "yyyy-MM-dd'T'hh:mm:ss", "yyyy-MM-dd"));
                o.set("importe", s.string("Importe"));
                o.set("fondo", s.string("FondoID"));
                o.set("estadoSolicitud", s.string("EstadoSolicitud"));
                o.set("tipoSolicitud", s.string("TipoSolicitud"));
                return o;
            }).toList());

            solicitudes.addAll(ordenesAgendadas.stream().map(s -> {
                Objeto o = new Objeto();
                o.set("llave", s.string("id") + "_" + s.string("cuotapartista") + "_" + s.string("fondo_id"));
                o.set("idSolicitud", s.string("id_solicitud"));
                o.set("cobis_id", contexto.idCobis());
                o.set("cuotapartista", s.string("cuotapartista"));
                o.set("fecha", s.string("fecha_solicitud"));
                o.set("fecha", s.date("fecha_solicitud", "yyyy-MM-dd HH:mm:ss.S", "yyyy-MM-dd"));
                o.set("importe", s.string("importe"));
                o.set("fondo", s.string("fondo_id"));
                o.set("estadoSolicitud", s.string("estado"));
                o.set("tipoSolicitud", s.string("tipo_solicitud"));
                return o;
            }).toList());

        } catch (Exception e) {
        }
        return solicitudes;
    }

    public static RespuestaMB fondos(ContextoMB contexto) {
        String fecha = contexto.parametros.string("fecha");
        boolean fueraDeHorario = false;
        boolean diaHabil = true;
        int idcuotapartista = contexto.parametros.integer("idcuotapartista");
        int idtipoValorCuotaParte = contexto.parametros.integer("idtipoValorCuotaParte");
        Integer numeroDeFondo = contexto.parametros.integer("numeroDeFondo");
        String tipoSolicitud = contexto.parametros.string("tipoSolicitud");

        contexto.sesion().limpiarChallengeDrs();

        boolean FFvariacionRendimiento = MBAplicacion.funcionalidadPrendida(
                contexto.idCobis(), "FFvariacionRendimiento", "FFvariacionRendimiento_cobis");

        Map<String, Objeto> mapaVar = Collections.emptyMap();
        if (FFvariacionRendimiento) {
            try {
                ApiResponseMB respVar = RestInversiones.variacionFondos(contexto, fecha);
                if (!respVar.hayError()) {
                    List<Objeto> listaVar = respVar.objetos();
                    if (listaVar != null && !listaVar.isEmpty()) {
                        mapaVar = listaVar.stream().collect(Collectors.toMap(
                                v -> getOrEmpty(v, "numFondo") + "|" + getOrEmpty(v, "abreviatura"),
                                v -> v
                        ));
                    }
                }
            } catch (Exception ignore) {
                // nos quedamos con emptyMap()
            }
        }

        // SERVICIOS EN PARALELO
        Futuro<String> futuroTipoPersona = new Futuro<>(() -> getTipoPersona(contexto));
        Futuro<ApiResponseMB> futuroResponse = new Futuro<>(() ->
                RestInversiones.fondos(contexto, idcuotapartista, idtipoValorCuotaParte, numeroDeFondo, tipoSolicitud));
        Futuro<Objeto> futuroHoraMaximaMinima = new Futuro<>(() ->
                SqlHomebanking.getHorarioMaximoMinimo(tipoSolicitud));
        Futuro<Boolean> futuroIsDiaHabil = new Futuro<>(() -> Util.isDiaHabil(contexto));
        Futuro<RespuestaMB> FuturoResponsePerfilInversor = new Futuro<>(() -> perfilInversor(contexto));
        Futuro<List<Objeto>> futuroTablaParametria = new Futuro<>(() ->
                SqlHomebanking.getFondosParametria(tipoSolicitud, futuroTipoPersona.get()));
        // FIN SERVICIOS EN PARALELO

        ApiResponseMB response = futuroResponse.get();
        Objeto horaMaximaMinima = futuroHoraMaximaMinima.get();

        if (response.hayError()) {
            return RespuestaMB.error();
        }

        List<Objeto> tabla = response.objetos("Table");
        if (tabla == null || tabla.isEmpty()) {
            return RespuestaMB.estado("SIN_FONDOS");
        }

        Integer inhPrimero = getOrNullInt(tabla.get(0), "EstaInhibido");
        if (inhPrimero != null && inhPrimero == -1) {
            return RespuestaMB.estado("CUENTA_INHIBIDA");
        }

        String horaIni = getOrEmpty(horaMaximaMinima, "hora_inicio");
        String horaFin = getOrEmpty(horaMaximaMinima, "hora_fin");
        if (comprobacionFueraDeHorario(horaIni, horaFin)) {
            fueraDeHorario = true;
        }

        if (!futuroIsDiaHabil.get()) {
            fueraDeHorario = true;
            diaHabil = false;
        }

        List<Objeto> tablaParametria = futuroTablaParametria.get();
        if (tablaParametria == null || tablaParametria.isEmpty()) {
            return RespuestaMB.estado("SIN_PARAMETRIA");
        }

        List<Objeto> fondos_fci = new ArrayList<>();
        RespuestaMB respuesta;

        if (fueraDeHorario) {
            respuesta = RespuestaMB.estado("FUERA_DE_HORARIO");
            respuesta.set("horaInicio", horaIni);
            respuesta.set("horaFin", horaFin);
        } else {
            respuesta = new RespuestaMB();
        }

        if (!diaHabil) {
            String diaNoHabil = " de lunes a viernes, siendo d&iacute;as h&aacute;biles";
            respuesta.set("diaHabil", diaNoHabil);
        }
        respuesta.set("esDiaHabil", diaHabil);

        RespuestaMB responsePerfilInversor = FuturoResponsePerfilInversor.get();

        String tipoVcpTarget = getOrEmpty(tablaParametria.get(0), "tipo_vcp_descripcion");

        // Iteramos sobre cada fondo devuelto por la API
        for (Objeto item : tabla) {
            if (item == null || !item.existe("FondoID")) continue;

            // Filtrar según la descripción requerida en parametría (tolerante)
            if (strEq(getOrEmpty(item, "TipoVCPDescripcion"), tipoVcpTarget)) {
                Objeto fondo = new Objeto();

                for (Objeto fondosParam : tablaParametria) {
                    if (fondosParam == null) continue;

                    Integer itemFondoId  = getOrNullInt(item, "FondoID");
                    Integer paramFondoId = getOrNullInt(fondosParam, "id_fondo");
                    String  itemCond     = getOrEmpty(item, "CondicionIngresoEgresoID");
                    String  paramCond    = getOrEmpty(fondosParam, "cond_ingreso_egreso");

                    if (!(Objects.equals(itemFondoId, paramFondoId) && strEq(itemCond, paramCond))) continue;

                    fondo.set("fondoNumero", getOrNullInt(item, "FondoNumero"));
                    fondo.set("fondoNombre", nombreFondoLimpio(getOrEmpty(item, "FondoNombre")));
                    fondo.set("monedaID", getOrNullInt(item, "MonedaID"));
                    fondo.set("tipoVCPAbreviatura", getOrEmpty(item, "TipoVCPAbreviatura"));
                    fondo.set("tipoVCPID", getOrEmpty(item, "TipoVCPID"));
                    fondo.set("condicionIngresoEgresoID", getOrEmpty(item, "CondicionIngresoEgresoID"));
                    fondo.set("monedaDescripcion", getOrEmpty(item, "MonedaDescripcion"));
                    fondo.set("monedaSimbolo", getOrEmpty(item, "MonedaSimbolo"));
                    BigDecimal min = getOrZeroBD(fondosParam, "min_a_operar");
                    fondo.set("montoMinimo", min);
                    fondo.set("montoMinimoFormateado", Formateador.importe(min));
                    Integer inh = getOrNullInt(item, "EstaInhibido");
                    fondo.set("estaInhibido", inh != null && inh != 0);
                    String hiF = getOrEmpty(fondosParam, "hora_inicio");
                    String hfF = getOrEmpty(fondosParam, "hora_fin");
                    fondo.set("estaFueraDeHorario", comprobacionFueraDeHorario(hiF, hfF));
                    fondo.set("hora_inicio", hiF);
                    fondo.set("hora_fin", hfF);
                    fondo.set("esDiaHabil", diaHabil);

                    // Enriquecimiento + perfil inversor (con fallbacks de riesgo)
                    agregarDetalleFondo(fondo, responsePerfilInversor, tabla, fondo.string("fondoNumero"));

                    // Variaciones (tolerante)
                    if (FFvariacionRendimiento) {
                        String claveVar = fondo.string("fondoNumero") + "|" + fondo.string("tipoVCPAbreviatura");
                        Objeto datosVar = (mapaVar == null) ? null : mapaVar.get(claveVar);
                        if (datosVar != null) {
                            BigDecimal varDiaria = getOrZeroBD(datosVar, "variacion");
                            fondo.set("variacion", varDiaria);
                            fondo.set("variacionFormateada", Formateador.importe(varDiaria, 2));
                            if (datosVar.existe("variacionMensual")) {
                                BigDecimal vm = getOrZeroBD(datosVar, "variacionMensual");
                                fondo.set("variacionMensual", vm);
                                fondo.set("variacionMensualFormateada", Formateador.importe(vm, 2));
                            }
                            if (datosVar.existe("variacionAnual")) {
                                BigDecimal va = getOrZeroBD(datosVar, "variacionAnual");
                                fondo.set("variacionAnual", va);
                                fondo.set("variacionAnualFormateada", Formateador.importe(va, 2));
                            }
                        }
                    }
                }

                if (fondo.existe("fondoNumero")) {
                    fondos_fci.add(fondo);
                }
            }
        }

        List<Objeto> fondosOrdenados = Optional.ofNullable(fondos_fci)
                .orElse(Collections.emptyList())
                .stream()
                .sorted(
                        Comparator
                                .comparingInt((Objeto f) -> {
                                    Integer n = f.integer("fondoNumero");
                                    int v = (n != null) ? n : Integer.MAX_VALUE;
                                    return (v == 9 || v == 20) ? 0 : 1; // MM primero
                                })
                                .thenComparingInt(f -> Optional.ofNullable(f.integer("fondoNumero")).orElse(Integer.MAX_VALUE))
                )
                .collect(Collectors.toList());

        respuesta.set("fondos", fondosOrdenados);
        respuesta.set("nuevoFlujoFCI", FFvariacionRendimiento);
        return respuesta;
    }



    /**
     * Orden por el nombre del fondo donde se indica el orden.
     * e.g. "10 - TORONTO TRUST LIQUIDEZ DOLAR" (orden=10)
     *
     * @param fondos fondos
     *               <p>
     *                                                                                                                                                                          TODO GB - M434 - API Inversiones - Refactor - Campo orden.
     */
    private static List<Objeto> ordenarFondoPorNombre(List<Objeto> fondos) {
        Collections.sort(fondos, new Comparator<Objeto>() {
            public int compare(Objeto o1, Objeto o2) {
                String pref1 = o1.string("fondoNombre").split(" -")[0];
                String pref2 = o2.string("fondoNombre").split(" -")[0];
                // Si viene sin prefijo de orden, asignamos 999.
                Integer a = (pref1 == null || !pref1.trim().matches("\\d+") ? 999 : Integer.parseInt(pref1.trim()));
                Integer b = (pref2 == null || !pref2.trim().matches("\\d+") ? 999 : Integer.parseInt(pref2.trim()));
                return a.compareTo(b);
            }
        });
        return fondos;
    }

    private static Boolean comprobacionFueraDeHorario(String horaInicio, String horaFin) {
        LocalTime horaActual = LocalTime.now();
        Boolean enHorario = (horaActual.isAfter(LocalTime.parse(horaInicio)) && horaActual.isBefore(LocalTime.parse(horaFin)));
        if (enHorario) {
            return false;
        }

        return true;
    }

    public static RespuestaMB fondosAceptados(ContextoMB contexto) {
        Integer fondo = contexto.parametros.integer("fondo");
        List<Objeto> fondosAceptados = SqlHomebanking.getFondosAceptados(contexto.idCobis(), fondo);
        RespuestaMB respuesta = new RespuestaMB();

        if (fondosAceptados.isEmpty()) {
            respuesta.set("primeraVez", true);
        } else {
            respuesta.set("primeraVez", false);
        }

        return respuesta;
    }

    private static Integer validacionOperacionConPerfil(RespuestaMB responsePerfilInversor, Integer idPerfilFondo) {
        Integer nivel = C_RES_VAL_INVERSION_PERFIL_ADECUADO;

        String idPerfil = responsePerfilInversor.string("idPerfil");
        Integer idPerfilInversor = (idPerfil == null || idPerfil.equals("")) ? null
                : Integer.parseInt(idPerfil);

        EnumPerfilInversor perfilCliente = idPerfilInversor == null ?
                EnumPerfilInversor.NO_TIENE : EnumPerfilInversor.codigo(idPerfilInversor);
        EnumPerfilInversor perfilFCI = EnumPerfilInversor.codigo(idPerfilFondo);

        if ((perfilCliente.esConservador() && (perfilFCI.esModerado() || perfilFCI.esArriesgado()))
                || (perfilCliente.esModerado() && perfilFCI.esArriesgado())) {
            nivel = C_RES_VAL_INVERSION_PERFIL_NO_ADECUADO;
        } else if (responsePerfilInversor.bool("vencido") || !perfilCliente.tiene()) {
            nivel = C_RES_VAL_INVERSION_OPERA_BAJO_PROPIO_RIESGO;
        }
        return nivel;
    }

    public static RespuestaMB rescate(ContextoMB contexto) {
        ApiMB.eliminarCache(contexto, "PosicionCuotapartista", contexto.idCobis(), LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), "", contexto.parametros.string("cuotapartista"));

        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_fondos_fuera_de_horario", "prendido_fondos_fuera_de_horario_cobis")) {
            if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_fondos_fuera_de_horario_v2", "prendido_fondos_fuera_de_horario_v2_cobis")) {
                return rescateV3(contexto);
            } else {
                return rescateV2(contexto);
            }
        } else {
            Integer cantCuotapartes = contexto.parametros.integer("cantCuotapartes");
            String ctaBancaria = contexto.parametros.string("ctaBancaria");
            String cuotapartista = contexto.parametros.string("cuotapartista");
            String esTotal = contexto.parametros.string("esTotal");
            String fechaAcreditacion = contexto.parametros.string("fechaAcreditacion");
            String fechaConcertacion = fechaAcreditacion;
            String importe = contexto.parametros.string("importe");
            String moneda = contexto.parametros.string("moneda");
            Objeto inversionFondo = new Objeto();
            inversionFondo.set("Fondo", contexto.parametros.objeto("inversionFondo").string("fondo"));
            inversionFondo.set("CondicionIngresoEgreso", contexto.parametros.objeto("inversionFondo").string("condicionIngresoEgreso"));
            inversionFondo.set("TipoValorCuotaParte", contexto.parametros.objeto("inversionFondo").string("tipoValorCuotaParte"));
            String IDSolicitud = UUID.randomUUID().toString();
            BigDecimal valor = new BigDecimal(importe);

            Boolean isDisable48hrs = ConfigMB.bool("prendido_doble_factor_rescate_fondos", false);
            if (isDisable48hrs && !contexto.validaSegundoFactor("rescate-fondos")) {
                return RespuestaMB.estado("REQUIERE_SEGUNDO_FACTOR");
            }

            String tipoPersona = getTipoPersona(contexto);
            List<Objeto> tablaParametria = SqlHomebanking.getFondosParametria("RE", tipoPersona);
            for (Objeto fondos : tablaParametria) {
                if (Integer.parseInt(inversionFondo.string("Fondo")) == fondos.integer("id_fondo") && inversionFondo.string("CondicionIngresoEgreso").equals(fondos.string("cond_ingreso_egreso"))) {
                    if (fondos.bigDecimal("min_a_operar").compareTo(valor) == 1 && esTotal.equals("0")) {
                        return RespuestaMB.estado("ERROR_MONTO_MINIMO");
                    }
                }
            }

            ApiResponseMB responseRescate = null;
            ApiResponseMB responseAplicaCredito;
            ApiResponseMB responseFondos = RestInversiones.fondos(contexto, Integer.parseInt(cuotapartista), 0, Integer.parseInt(inversionFondo.string("Fondo")), "RE");
            if (responseFondos.objetos("Table").get(0).integer("PlazoLiquidacionFondo") == 0 && esTotal.equals("0") && !importe.equals("0")) {
                RespuestaMB responseCuotapartista = cuotapartista(contexto);
                // List<Objeto> cuentasBancarias =
                // responseCuotapartista.objetos("cuotapartistas").get(0).objetos("ctasBancarias");
                List<Objeto> cuentasBancarias = null;

                for (Objeto cpItem : responseCuotapartista.objetos("cuotapartistas")) {
                    if (cuotapartista.equals(cpItem.string("iDCuotapartista"))) {
                        cuentasBancarias = cpItem.objetos("ctasBancarias");
                        break;
                    }
                }

                if (cuentasBancarias == null || cuentasBancarias.isEmpty()) {
                    return RespuestaMB.estado("SIN_CUOTAPARTISTA_CUENTA");
                }

                for (Objeto cuentas : cuentasBancarias) {
                    if (cuentas.string("idCuentaBancaria").equals(ctaBancaria)) {
                        Cuenta cuenta = contexto.cuenta(cuentas.string("numeroCuenta"));
                        responseAplicaCredito = CuentasService.aplicaCreditoDebito(contexto, "creditos", cuenta, valor, "1301", fechaConcertacion);
                        if (responseAplicaCredito.hayError()) {
                            RespuestaMB respuesta = RespuestaMB.estado("ERROR_FUNCIONAL");
                            respuesta.set("mensaje", responseAplicaCredito.string("mensajeAlUsuario"));
                            return respuesta;
                        }
                        responseRescate = RestInversiones.rescate(contexto, cantCuotapartes, ctaBancaria, cuotapartista, esTotal, fechaAcreditacion, fechaConcertacion, "28", IDSolicitud, importe, inversionFondo, moneda, "0", "0", "IE");
                        if (!ConfigMB.esProduccion()) {
                            if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_mock_rescate")) {
                                if (!validarMockRescate()) {
                                    respuestaMockRescateError(contexto.csmIdAuth);
                                }
                            }
                        }
                        if (responseRescate.hayError()) {
                            CuentasService.reservaAplicaCreditoDebito(contexto, "creditos", cuenta, responseAplicaCredito.string("idTransaccion"), valor, "1301", fechaConcertacion);
                            String mensaje = responseRescate.string("mensajeAlUsuario");
                            return respuestaRescateError(mensaje, contexto.csmIdAuth);
                        }
                        break;
                    }
                }
            } else {
                responseRescate = RestInversiones.rescate(contexto, cantCuotapartes, ctaBancaria, cuotapartista, esTotal, fechaAcreditacion, fechaConcertacion, "28", IDSolicitud, importe, inversionFondo, moneda, "0", "0", "IE");
                if (responseRescate.hayError()) {
                    String mensaje = responseRescate.string("mensajeAlUsuario");
                    return respuestaRescateError(mensaje, contexto.csmIdAuth);
                }
            }

            if (MBSalesforce.prendidoSalesforce(contexto.idCobis())) {
                String salesforce_suscripcion_y_rescate_fondos = ConfigMB.string("salesforce_suscripcion_y_rescate_fondos");
                Objeto parametros = new Objeto();
                parametros.set("IDCOBIS", contexto.idCobis());
                parametros.set("NOMBRE", contexto.persona().nombre());
                parametros.set("APELLIDO", contexto.persona().apellido());
                parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
                new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_suscripcion_y_rescate_fondos, parametros));
            }

            RespuestaMB respuesta = new RespuestaMB();
            respuesta.set("numSolicitud", responseRescate != null ? responseRescate.string("NumSolicitud") : null);
            contexto.sesion().limpiarSegundoFactor();
            ProductosService.eliminarCacheProductos(contexto);
            ApiMB.eliminarCache(contexto, "PosicionCuotapartista", contexto.idCobis(), LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), "", cuotapartista);

            return respuesta;
        }
    }

    public static Objeto validarRescate(ContextoMB contexto, String fecha, BigDecimal importe, String cuotapartista, String fondoId) {
        contexto.parametros.set("fecha", fecha);
        contexto.parametros.set("idCuotapartista", "");
        contexto.parametros.set("numeroCuotapartista", cuotapartista);
        Objeto validacion = new Objeto();
        Boolean estado = true;
        validacion.set("estado", estado);
        try {
            Objeto objeto = posicionCuotapartistaV3(contexto);
            List<Objeto> posiciones = objeto.objetos("posicionCuotapartista");
            Optional<Objeto> posicion = posiciones.stream().filter(p -> p.string("fondoNumero").equalsIgnoreCase(fondoId)).findFirst();
            if (posicion.isPresent()) {
                if (importe.compareTo(posicion.get().bigDecimal("cuotapartesValuadas")) > 0) {
                    estado = false;
                    validacion.set("estado", estado);
                    validacion.set("mensaje", "Importe mayor a lo que el cliente tiene");
                }
                if (posicion.get().bool("rescateTotal", false)) {
                    estado = false;
                    validacion.set("estado", estado);
                    validacion.set("mensaje", "Ya se ha realizado un rescate total");
                }
            } else {
                estado = false;
                validacion.set("estado", estado);
                validacion.set("mensaje", "Fondo no encontrado en posiciones del cliente");
            }
        } catch (Exception e) {
            estado = false;
            validacion.set("estado", estado);
            validacion.set("mensaje", "Error al validar importe de rescate");
        }
        return validacion;
    }

    public static RespuestaMB rescateV3(ContextoMB contexto) {
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_modo_transaccional_rescate_fondos",
                "prendido_modo_transaccional_rescate_fondos_cobis") && !TransmitMB.isChallengeOtp(contexto, "rescate-fondos")) {
            try {
                Futuro<RespuestaMB> futuroResponseCuotapartista = new Futuro<>(() -> cuotapartista(contexto));
                String sessionToken = contexto.parametros.string(Transmit.getSessionToken(), null);
                if (Objeto.empty(sessionToken))
                    return RespuestaMB.parametrosIncorrectos();

                String cuotapartista = contexto.parametros.string("cuotapartista");
                String ctaBancaria = contexto.parametros.string("ctaBancaria");
                BigDecimal valor = contexto.parametros.bigDecimal("importe");

                if (Objeto.empty(cuotapartista, ctaBancaria, valor))
                    return RespuestaMB.parametrosIncorrectos();

                List<Objeto> cuentasBancarias = futuroResponseCuotapartista.tryGet().objetos("cuotapartistas").stream()
                        .filter(cpItem -> cuotapartista.equals(cpItem.string("iDCuotapartista")))
                        .findFirst()
                        .map(cpItem -> cpItem.objetos("ctasBancarias"))
                        .orElse(null);

                if (cuentasBancarias == null || cuentasBancarias.isEmpty())
                    return RespuestaMB.estado("SIN_CUOTAPARTISTA_CUENTA");

                Cuenta cuentaOrigen = null;
                for (Objeto cuentas : cuentasBancarias)
                    if (cuentas.string("idCuentaBancaria").equals(ctaBancaria))
                        cuentaOrigen = contexto.cuenta(cuentas.string("numeroCuenta"));

                if (!Objeto.empty(cuentaOrigen)) {
                    RescateMBBMBankProcess rescateMBBMBankProcess = new RescateMBBMBankProcess(contexto.idCobis(),
                            sessionToken,
                            valor,
                            Util.obtenerDescripcionMonedaTransmit(cuentaOrigen.idMoneda()),
                            TransmitMB.REASON_RESCATE,
                            new RescateMBBMBankProcess.Payer(contexto.persona().cuit(), cuentaOrigen.numero(), Util.getBhCodigo(), TransmitMB.CANAL),
                            new RescateMBBMBankProcess.Payee(contexto.persona().cuit(), "", ""));

                    RespuestaMB respuesta = TransmitMB.recomendacionTransmit(contexto, rescateMBBMBankProcess, "rescate-fondos");
                    if (respuesta.hayError())
                        return respuesta;
                }
            } catch (Exception e) {
            }
        }

        Integer cantCuotapartes = contexto.parametros.integer("cantCuotapartes");
        String ctaBancaria = contexto.parametros.string("ctaBancaria");
        String cuotapartista = contexto.parametros.string("cuotapartista");
        String esTotal = contexto.parametros.string("esTotal");
        String fechaConcertacion = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String fechaAcreditacion = fechaConcertacion;
        String importe = contexto.parametros.string("importe");
        String moneda = contexto.parametros.string("moneda");
        Objeto inversionFondo = new Objeto();
        String fondoId = contexto.parametros.objeto("inversionFondo").string("fondo");
        inversionFondo.set("Fondo", contexto.parametros.objeto("inversionFondo").string("fondo"));
        inversionFondo.set("CondicionIngresoEgreso",
                contexto.parametros.objeto("inversionFondo").string("condicionIngresoEgreso"));
        inversionFondo.set("TipoValorCuotaParte",
                contexto.parametros.objeto("inversionFondo").string("tipoValorCuotaParte"));
        String IDSolicitud = UUID.randomUUID().toString();
        Boolean aceptaTyCAgenda = contexto.parametros.existe("aceptaTyCAgenda")
                ? contexto.parametros.bool("aceptaTyCAgenda")
                : false;

        BigDecimal valor = new BigDecimal(importe);
        String tipoCuentaBancaria = "";

        Objeto validacion = validarRescate(contexto, fechaConcertacion, new BigDecimal(importe), cuotapartista,
                fondoId);
        if (!validacion.bool("estado")) {
            return RespuestaMB.estado(validacion.string("mensaje").toUpperCase());
        }

        String tipoPersona = getTipoPersona(contexto);
        List<Objeto> tablaParametria = SqlHomebanking.getFondosParametria("RE", tipoPersona);
        for (Objeto fondos : tablaParametria) {
            if (Integer.parseInt(inversionFondo.string("Fondo")) == fondos.integer("id_fondo")
                    && inversionFondo.string("CondicionIngresoEgreso").equals(fondos.string("cond_ingreso_egreso"))) {
                if (fondos.bigDecimal("min_a_operar").compareTo(valor) == 1 && esTotal.equals("0")) {
                    return RespuestaMB.estado("ERROR_MONTO_MINIMO");
                }
            }
        }

        Objeto rowFileTablaParametria = tablaParametria.stream()
                .filter(tp -> Integer.parseInt(inversionFondo.string("Fondo")) == tp.integer("id_fondo")
                        && inversionFondo.string("CondicionIngresoEgreso").equals(tp.string("cond_ingreso_egreso")))
                .toList().get(0);

        /* Parametros fuera de horario */
        String horarioInicio = rowFileTablaParametria.string("hora_inicio");
        String horaInicio = horarioInicio.split(":")[0];
        String minutoInicio = horarioInicio.split(":")[1];
        Boolean esDiaHabil = Util.isDiaHabil(contexto);
        Boolean esAntesDeApertura = LocalTime.now().isBefore(LocalTime.parse(horarioInicio));
        String horarioFin = rowFileTablaParametria.string("hora_fin");
        String horaInicioBatchFCI = ConfigMB.string("hora_inicio_batch_fci", "22:00");
        String horaFinBatchFCI = ConfigMB.string("hora_fin_batch_fci", "00:00");

        LocalTime inicioBatch = LocalTime.parse(horaInicioBatchFCI);
        LocalTime finBatch = LocalTime.parse(horaFinBatchFCI);
        LocalTime ahora = LocalTime.now();
        LocalTime inicioFondo = LocalTime.parse(horarioInicio);
        LocalTime finFondo = LocalTime.parse(horarioFin);

        ApiResponseMB responseRescate = null;
        ApiResponseMB responseAplicaCredito = null;
        ApiResponseMB responseFondos = RestInversiones.fondos(contexto, Integer.parseInt(cuotapartista), 0,
                Integer.parseInt(inversionFondo.string("Fondo")), "RE");

        RespuestaMB responseCuotapartista = cuotapartista(contexto);
        List<Objeto> cuentasBancarias = null;

        for (Objeto cpItem : responseCuotapartista.objetos("cuotapartistas")) {
            if (cuotapartista.equals(cpItem.string("iDCuotapartista"))) {
                cuentasBancarias = cpItem.objetos("ctasBancarias");
                break;
            }
        }

        if (cuentasBancarias == null || cuentasBancarias.isEmpty())
            return RespuestaMB.estado("SIN_CUOTAPARTISTA_CUENTA");

        Boolean esOperacionFueraDeHorario = esFueraHorarioTablaParametria(rowFileTablaParametria)
                || !Util.isDiaHabil(contexto);

        if (esOperacionFueraDeHorario && !aceptaTyCAgenda)
            return RespuestaMB.estado("HORARIO_AGENDA_REQ_TYC");

        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        boolean esMigrado = contexto.esMigrado(contexto);

        if (esMigrado && Objeto.anyEmpty(csmId, checksum))
            return RespuestaMB.parametrosIncorrectos();

        if (esMigrado) {
            RespuestaMB respuestaValidaTransaccion = contexto.validarTransaccion(contexto, true, "", JourneyTransmitEnum.MB_INICIO_SESION);
            if (respuestaValidaTransaccion.hayError())
                return respuestaValidaTransaccion;
        }

        String causalCredito = "1301";
        if (!esOperacionFueraDeHorario) {
            causalCredito = "1301";
        } else {
            if (!Util.isDiaHabil(contexto)) {
                causalCredito = "1322";
            } else {
                if (ahora.isBefore(inicioFondo) && ahora.isAfter(finBatch)) {
                    causalCredito = "1301";
                } else {
                    if (ahora.isBefore(inicioBatch) && ahora.isAfter(finFondo)) {
                        causalCredito = "1322";
                    }
                }
            }
        }


        Objeto fondoConsultado = responseFondos.objetos("Table").stream()
                .filter(item -> item.string("FondoID").equals(fondoId)).toList().get(0);
        if (fondoConsultado.integer("PlazoLiquidacionFondo") == 0 && esTotal.equals("0")
                && !importe.equals("0")) {
            /* Ruta Uno */
            for (Objeto cuentas : cuentasBancarias) {
                if (cuentas.string("idCuentaBancaria").equals(ctaBancaria)) {
                    String monedaCuenta = cuentas.string("idMoneda");
                    if (!moneda.equals(monedaCuenta)) {
                        return RespuestaMB.estado("ERROR_MONEDA_NO_COINCIDE", contexto.csmIdAuth);
                    }

                    Cuenta cuenta = contexto.cuenta(cuentas.string("numeroCuenta"));
                    tipoCuentaBancaria = cuentas.string("descripcionCorta", "");
                    if (!esOperacionFueraDeHorario || (esOperacionFueraDeHorario && aceptaTyCAgenda)) {
                        responseAplicaCredito = CuentasService.aplicaCreditoDebito(contexto, "creditos", cuenta, valor,
                                causalCredito, fechaConcertacion);
                        if (responseAplicaCredito.hayError()) {
                            RespuestaMB respuesta = RespuestaMB.estado("ERROR_FUNCIONAL");
                            respuesta.set("mensaje", responseAplicaCredito.string("mensajeAlUsuario"));
                            return respuesta.set("csmIdAuth", contexto.csmIdAuth);
                        }
                    }

                    if (!esOperacionFueraDeHorario) {
                        // Rescate en horario
                        responseRescate = RestInversiones.rescate(contexto, cantCuotapartes, ctaBancaria, cuotapartista,
                                esTotal, fechaAcreditacion, fechaConcertacion, "28", IDSolicitud, importe,
                                inversionFondo, moneda, "0", "0", "IE");
                        if (!ConfigMB.esProduccion()) {
                            if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_mock_rescate")) {
                                if (!validarMockRescate()) {
                                    respuestaMockRescateError(contexto.csmIdAuth);
                                }
                            }
                        }
                        if (responseRescate.hayError()) {
                            CuentasService.reservaAplicaCreditoDebito(contexto, "creditos", cuenta,
                                    responseAplicaCredito.string("idTransaccion"), valor, causalCredito, fechaConcertacion);
                            String mensaje = responseRescate.string("mensajeAlUsuario");
                            return respuestaRescateError(mensaje, contexto.csmIdAuth);
                        }
                    }
                    break;
                }
            }
        } else {
            /* Ruta Dos */

            for (Objeto cuentas : cuentasBancarias) {
                if (cuentas.string("idCuentaBancaria").equals(ctaBancaria)) {
                    String monedaCuenta = cuentas.string("idMoneda");
                    if (!moneda.equals(monedaCuenta)) {
                        return RespuestaMB.estado("ERROR_MONEDA_NO_COINCIDE", contexto.csmIdAuth);
                    }
                    break;
                }
            }

            tipoCuentaBancaria = cuentasBancarias.stream()
                    .filter(item -> item.string("idCuentaBancaria").equalsIgnoreCase(ctaBancaria)).toList().get(0)
                    .string("descripcionCorta");


            if (!esOperacionFueraDeHorario) {
                // Rescate en horario
                responseRescate = RestInversiones.rescate(contexto, cantCuotapartes, ctaBancaria, cuotapartista,
                        esTotal, fechaAcreditacion, fechaConcertacion, "28", IDSolicitud, importe, inversionFondo,
                        moneda, "0", "0", "IE");
                if (!ConfigMB.esProduccion()) {
                    if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_mock_rescate")) {
                        if (!validarMockRescate()) {
                            return respuestaMockRescateError(contexto.csmIdAuth);
                        }
                    }
                }
                if (responseRescate.hayError()) {
                    String mensaje = responseRescate.string("mensajeAlUsuario");
                    return respuestaRescateError(mensaje, contexto.csmIdAuth);
                }
            }
        }

        if (esOperacionFueraDeHorario) {
            ApiResponseMB respuestaAgenda = agendarRescate(contexto, cantCuotapartes, ctaBancaria, cuotapartista, esTotal,
                    fechaConcertacion, fechaAcreditacion, importe, moneda, inversionFondo, fondoId, IDSolicitud,
                    tipoCuentaBancaria, horaInicio, minutoInicio, esDiaHabil, esAntesDeApertura);

            if (respuestaAgenda.hayError()) {
                return RespuestaMB.estado("ERROR_AGENDA", contexto.csmIdAuth);
            } else {
                contexto.limpiarSegundoFactor();
                ProductosService.eliminarCacheProductos(contexto);
                return RespuestaMB.estado("FUERA_DE_HORARIO", contexto.csmIdAuth);
            }
        }

        if (MBSalesforce.prendidoSalesforce(contexto.idCobis())) {
            String salesforce_suscripcion_y_rescate_fondos = ConfigMB.string("salesforce_suscripcion_y_rescate_fondos");
            Objeto parametros = new Objeto();
            parametros.set("IDCOBIS", contexto.idCobis());
            parametros.set("NOMBRE", contexto.persona().nombre());
            parametros.set("APELLIDO", contexto.persona().apellido());
            parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
            new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_suscripcion_y_rescate_fondos, parametros));
        }

        contexto.limpiarSegundoFactor();
        ProductosService.eliminarCacheProductos(contexto);
        return new RespuestaMB().set("numSolicitud",
                responseRescate != null ? responseRescate.string("NumSolicitud") : null).set("csmIdAuth", contexto.csmIdAuth);
    }


    private static ApiResponseMB agendarRescate(ContextoMB contexto, Integer cantCuotapartes, String ctaBancaria,
                                                String cuotapartista, String esTotal, String fechaConcertacion, String fechaAcreditacion, String importe,
                                                String moneda, Objeto inversionFondo, String fondoId, String IDSolicitud, String tipoCuentaBancaria,
                                                String horaInicio, String minutoInicio, Boolean esDiaHabil, Boolean esAntesDeApertura) {
        LocalDateTime fechaHoraActual = LocalDateTime.now();

        SqlHomebanking.agendarOperacionFCI(contexto.idCobis(), IDSolicitud, cuotapartista,
                fechaHoraActual.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), importe, fondoId,
                "Agendada", "MB", "Rescate", tipoCuentaBancaria);

        Integer dia, mes, anio, hora, minuto, segundo = 0;
        hora = Integer.parseInt(horaInicio);
        minuto = Integer.parseInt(minutoInicio);
        if (esDiaHabil && esAntesDeApertura) {
            // Se ejecuta el dia de hoy al inicio del horario!
            anio = fechaHoraActual.getYear();
            mes = fechaHoraActual.getMonthValue();
            dia = fechaHoraActual.getDayOfMonth();
        } else {
            // Se ejecuta el proximo dia habil al inicio del horario!
            fechaConcertacion = getProximoDiaHabil(contexto);
            fechaAcreditacion = fechaConcertacion;

            anio = Integer.parseInt(fechaConcertacion.split("-")[0]);
            mes = Integer.parseInt(fechaConcertacion.split("-")[1]);
            dia = Integer.parseInt(fechaConcertacion.split("-")[2]);
        }

        LocalDateTime fechaHoraEjecucion = LocalDateTime.of(anio, mes, dia, hora, minuto, segundo);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String fechaHoraEjecucionFormateado = fechaHoraEjecucion.format(formatter);

        Objeto body = RestInversiones.crearBodyRequestRescate(cantCuotapartes, ctaBancaria, cuotapartista, esTotal,
                fechaAcreditacion, fechaConcertacion, "28", IDSolicitud, importe, inversionFondo, moneda, "0", "0",
                "IE");

        // TODO GB Agenda - registrar estado error en tabla
        return RestInversiones.rescateAgenda(contexto, fechaHoraEjecucionFormateado, body);
    }

    private static ApiResponseMB agendarSuscripcion(ContextoMB contexto, String cuotapartista, String fechaConcertacion,
                                                    String importe, Objeto inversionFondo, String fondoId, String moneda, String tipoCuentaBancaria,
                                                    String IDSolicitud, String fechaAcreditacion, Objeto cuentaBancaria, Objeto formasPagoCuentaBancaria,
                                                    String horaInicio, String minutoInicio, Boolean esDiaHabil, Boolean esAntesDeApertura) {
        LocalDateTime fechaHoraActual = LocalDateTime.now();
        SqlHomebanking.agendarOperacionFCI(contexto.idCobis(), IDSolicitud, cuotapartista,
                fechaHoraActual.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), importe, fondoId,
                "Agendada", "MB", "Suscripcion", tipoCuentaBancaria);

        Integer dia, mes, anio, hora, minuto, segundo = 0;
        hora = Integer.parseInt(horaInicio);
        minuto = Integer.parseInt(minutoInicio);

        if (esDiaHabil && esAntesDeApertura) {
            // Se ejecuta el dia de hoy al inicio del horario!
            anio = fechaHoraActual.getYear();
            mes = fechaHoraActual.getMonthValue();
            dia = fechaHoraActual.getDayOfMonth();
        } else {
            // Se ejecuta el proximo dia habil al inicio del horario!
            fechaConcertacion = getProximoDiaHabil(contexto);
            fechaAcreditacion = fechaConcertacion;

            anio = Integer.parseInt(fechaConcertacion.split("-")[0]);
            mes = Integer.parseInt(fechaConcertacion.split("-")[1]);
            dia = Integer.parseInt(fechaConcertacion.split("-")[2]);
        }

        LocalDateTime fechaHoraEjecucion = LocalDateTime.of(anio, mes, dia, hora, minuto, segundo);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String fechaHoraEjecucionFormateado = fechaHoraEjecucion.format(formatter);

        Objeto body = RestInversiones.crearBodyRequestSuscripcion(cuentaBancaria, cuotapartista, fechaAcreditacion,
                fechaConcertacion, formasPagoCuentaBancaria, IDSolicitud, inversionFondo, moneda, "IE");

        return RestInversiones.suscripcionAgenda(contexto, fechaHoraEjecucionFormateado, body);
    }


    public static RespuestaMB rescateV2(ContextoMB contexto) {
        Integer cantCuotapartes = contexto.parametros.integer("cantCuotapartes");
        String ctaBancaria = contexto.parametros.string("ctaBancaria");
        String cuotapartista = contexto.parametros.string("cuotapartista");
        String esTotal = contexto.parametros.string("esTotal");
        String fechaConcertacion = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String fechaAcreditacion = fechaConcertacion;
        // String importe = contexto.parametros.string("importe");
        String importe = esTotal.equals("0") ? contexto.parametros.string("importe") : "0";
        String moneda = contexto.parametros.string("moneda");
        boolean aceptaTyCAgenda = contexto.parametros.existe("aceptaTyCAgenda") ? contexto.parametros.bool("aceptaTyCAgenda") : false;

        Objeto inversionFondo = new Objeto();
        String fondoId = contexto.parametros.objeto("inversionFondo").string("fondo");
        inversionFondo.set("Fondo", contexto.parametros.objeto("inversionFondo").string("fondo"));
        inversionFondo.set("CondicionIngresoEgreso", contexto.parametros.objeto("inversionFondo").string("condicionIngresoEgreso"));
        inversionFondo.set("TipoValorCuotaParte", contexto.parametros.objeto("inversionFondo").string("tipoValorCuotaParte"));
        String IDSolicitud = UUID.randomUUID().toString();
        BigDecimal valor = new BigDecimal(importe);
        String tipoCuentaBancaria = "";

        Objeto validacion = validarRescate(contexto, fechaConcertacion, new BigDecimal(importe), cuotapartista, fondoId);
        if (!validacion.bool("estado")) {
            return RespuestaMB.estado(validacion.string("mensaje").toUpperCase());
        }

        String tipoPersona = getTipoPersona(contexto);
        List<Objeto> tablaParametria = SqlHomebanking.getFondosParametria("RE", tipoPersona);
        for (Objeto fondos : tablaParametria) {
            if (Integer.parseInt(inversionFondo.string("Fondo")) == fondos.integer("id_fondo") && inversionFondo.string("CondicionIngresoEgreso").equals(fondos.string("cond_ingreso_egreso"))) {
                if (fondos.bigDecimal("min_a_operar").compareTo(valor) == 1 && esTotal.equals("0")) {
                    return RespuestaMB.estado("ERROR_MONTO_MINIMO");
                }
            }
        }

        Objeto rowFileTablaParametria = tablaParametria.stream().filter(tp -> Integer.parseInt(inversionFondo.string("Fondo")) == tp.integer("id_fondo") && inversionFondo.string("CondicionIngresoEgreso").equals(tp.string("cond_ingreso_egreso"))).toList().get(0);

        RespuestaMB respuesta = new RespuestaMB();
        /* Parametros fuera de horario */
        String horarioInicio = rowFileTablaParametria.string("hora_inicio");
        String horaInicio = horarioInicio.split(":")[0];
        String minutoInicio = horarioInicio.split(":")[1];
        Boolean esDiaHabil = Util.isDiaHabil(contexto);
        Boolean esAntesDeApertura = LocalTime.now().isBefore(LocalTime.parse(horarioInicio));

        ApiResponseMB responseRescate = null;
        ApiResponseMB responseAplicaCredito = null;
        ApiResponseMB responseFondos = RestInversiones.fondos(contexto, Integer.parseInt(cuotapartista), 0, Integer.parseInt(inversionFondo.string("Fondo")), "RE");

        // TODO: Se saco del IF Ruta uno
        RespuestaMB responseCuotapartista = cuotapartista(contexto);
        List<Objeto> cuentasBancarias = null;

        for (Objeto cpItem : responseCuotapartista.objetos("cuotapartistas")) {
            if (cuotapartista.equals(cpItem.string("iDCuotapartista"))) {
                cuentasBancarias = cpItem.objetos("ctasBancarias");
                break;
            }
        }

        if (cuentasBancarias == null || cuentasBancarias.isEmpty()) {
            return RespuestaMB.estado("SIN_CUOTAPARTISTA_CUENTA");
        }

        Boolean esOperacionFueraDeHorario = esFueraHorarioTablaParametria(rowFileTablaParametria) || !Util.isDiaHabil(contexto);

        if (responseFondos.objetos("Table").get(0).integer("PlazoLiquidacionFondo") == 0 && esTotal.equals("0") && !importe.equals("0")) {
            /* Ruta Uno */

            for (Objeto cuentas : cuentasBancarias) {
                if (cuentas.string("idCuentaBancaria").equals(ctaBancaria)) {
                    tipoCuentaBancaria = cuentas.string("descripcionCorta", "");
                    Cuenta cuenta = contexto.cuenta(cuentas.string("numeroCuenta"));

                    if (!esOperacionFueraDeHorario || (esOperacionFueraDeHorario && aceptaTyCAgenda)) {
                        tipoCuentaBancaria = cuentas.string("descripcionCorta", "");
                        responseAplicaCredito = CuentasService.aplicaCreditoDebito(contexto, "creditos", cuenta, valor, "1301", fechaConcertacion);
                        if (responseAplicaCredito.hayError()) {
                            respuesta = RespuestaMB.estado("ERROR_FUNCIONAL");
                            respuesta.set("mensaje", responseAplicaCredito.string("mensajeAlUsuario"));
                            return respuesta;
                        }
                    }

                    if (esOperacionFueraDeHorario) {
                        // TODO GB - Hacer refactor - este codigo se repite abajo. Extract.
                        if (!aceptaTyCAgenda) {
                            return RespuestaMB.estado("HORARIO_AGENDA_REQ_TYC");
                        } else {
                            String fechaSQL = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            SqlHomebanking.agendarOperacionFCI(contexto.idCobis(), IDSolicitud, cuotapartista, fechaSQL, importe, fondoId, "Agendada", "MB", "Rescate", tipoCuentaBancaria);
                            if (esDiaHabil && esAntesDeApertura) {
                                // Se ejecuta el dia de hoy al inicio del horario!
                                String anioACtual = String.valueOf(LocalDate.now().getYear());
                                String mesActual = fechaSQL.split("-")[1];
                                String diaActual = String.valueOf(LocalDate.now().getDayOfMonth());
                                String sRequest = createJsonRequestRescate(cantCuotapartes, ctaBancaria, cuotapartista, esTotal, fechaAcreditacion, fechaConcertacion, importe, moneda, inversionFondo, IDSolicitud);
                                RestScheduler.crearTareaFIC(contexto, anioACtual, diaActual, horaInicio, mesActual, minutoInicio, "*", sRequest, "/v1/rescateAgenda");
                                respuesta.setEstado(("FUERA_DE_HORARIO"));
                            } else {
                                // Se ejecuta el proximo dia habil al inicio del horario!
                                fechaConcertacion = getProximoDiaHabil(contexto);
                                fechaAcreditacion = fechaConcertacion;
                                String anioPosterior = fechaConcertacion.split("-")[0];
                                String mesPosterior = fechaConcertacion.split("-")[1];
                                String diaPosterior = fechaConcertacion.split("-")[2];
                                String sRequest = createJsonRequestRescate(cantCuotapartes, ctaBancaria, cuotapartista, esTotal, fechaAcreditacion, fechaConcertacion, importe, moneda, inversionFondo, IDSolicitud);
                                RestScheduler.crearTareaFIC(contexto, anioPosterior, diaPosterior, horaInicio, mesPosterior, minutoInicio, "*", sRequest, "/v1/rescateAgenda");
                                respuesta.setEstado(("FUERA_DE_HORARIO"));
                            }
                        }
                    } else {
                        // Rescate en horario
                        responseRescate = RestInversiones.rescate(contexto, cantCuotapartes, ctaBancaria, cuotapartista, esTotal, fechaAcreditacion, fechaConcertacion, "28", IDSolicitud, importe, inversionFondo, moneda, "0", "0", "IE");
                        if (!ConfigMB.esProduccion()) {
                            if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_mock_rescate")) {
                                if (!validarMockRescate()) {
                                    respuestaMockRescateError(contexto.csmIdAuth);
                                }
                            }
                        }
                        if (responseRescate.hayError()) {
                            CuentasService.reservaAplicaCreditoDebito(contexto, "creditos", cuenta, responseAplicaCredito.string("idTransaccion"), valor, "1301", fechaConcertacion);
                            String mensaje = responseRescate.string("mensajeAlUsuario");
                            return respuestaRescateError(mensaje, contexto.csmIdAuth);
                        }
                    }
                    break;
                }
            }
        } else {
            /* Ruta Dos */
            if (esOperacionFueraDeHorario) {
                if (!aceptaTyCAgenda) {
                    return RespuestaMB.estado("HORARIO_AGENDA_REQ_TYC");
                } else {
                    tipoCuentaBancaria = cuentasBancarias.stream().filter(item -> item.string("idCuentaBancaria").equalsIgnoreCase(ctaBancaria)).toList().get(0).string("descripcionCorta");
                    String fechaSQL = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    SqlHomebanking.agendarOperacionFCI(contexto.idCobis(), IDSolicitud, cuotapartista, fechaSQL, importe, fondoId, "Agendada", "MB", "Rescate", tipoCuentaBancaria);
                    if (esDiaHabil && esAntesDeApertura) {
                        // Se ejecuta el proximo dia habil al inicio del horario!
                        String anioACtual = String.valueOf(LocalDate.now().getYear());
                        String mesActual = fechaSQL.split("-")[1];
                        String diaActual = String.valueOf(LocalDate.now().getDayOfMonth());
                        String sRequest = createJsonRequestRescate(cantCuotapartes, ctaBancaria, cuotapartista, esTotal, fechaAcreditacion, fechaConcertacion, importe, moneda, inversionFondo, IDSolicitud);
                        RestScheduler.crearTareaFIC(contexto, anioACtual, diaActual, horaInicio, mesActual, minutoInicio, "*", sRequest, "/v1/rescateAgenda");
                        respuesta.setEstado(("FUERA_DE_HORARIO"));
                    } else {
                        // Se ejecuta el proximo dia habil al inicio del horario!
                        fechaConcertacion = getProximoDiaHabil(contexto);
                        fechaAcreditacion = fechaConcertacion;
                        String anioPosterior = fechaConcertacion.split("-")[0];
                        String mesPosterior = fechaConcertacion.split("-")[1];
                        String diaPosterior = fechaConcertacion.split("-")[2];
                        String sRequest = createJsonRequestRescate(cantCuotapartes, ctaBancaria, cuotapartista, esTotal, fechaAcreditacion, fechaConcertacion, importe, moneda, inversionFondo, IDSolicitud);
                        RestScheduler.crearTareaFIC(contexto, anioPosterior, diaPosterior, horaInicio, mesPosterior, minutoInicio, "*", sRequest, "/v1/rescateAgenda");
                        respuesta.setEstado(("FUERA_DE_HORARIO"));
                    }
                }
            } else {
                // Rescate en horario
                responseRescate = RestInversiones.rescate(contexto, cantCuotapartes, ctaBancaria, cuotapartista, esTotal, fechaAcreditacion, fechaConcertacion, "28", IDSolicitud, importe, inversionFondo, moneda, "0", "0", "IE");
                if (!ConfigMB.esProduccion()) {
                    if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_mock_rescate")) {
                        if (!validarMockRescate()) {
                            return respuestaMockRescateError(contexto.csmIdAuth);
                        }
                    }
                }
                if (responseRescate.hayError()) {
                    String mensaje = responseRescate.string("mensajeAlUsuario");
                    return respuestaRescateError(mensaje, contexto.csmIdAuth);
                }
            }
        }

        if (MBSalesforce.prendidoSalesforce(contexto.idCobis())) {
            String salesforce_suscripcion_y_rescate_fondos = ConfigMB.string("salesforce_suscripcion_y_rescate_fondos");
            Objeto parametros = new Objeto();
            parametros.set("IDCOBIS", contexto.idCobis());
            parametros.set("NOMBRE", contexto.persona().nombre());
            parametros.set("APELLIDO", contexto.persona().apellido());
            parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
            new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_suscripcion_y_rescate_fondos, parametros));
        }

        contexto.limpiarSegundoFactor();
        ProductosService.eliminarCacheProductos(contexto);
        respuesta.set("numSolicitud", responseRescate != null ? responseRescate.string("NumSolicitud") : null);

        return respuesta;
    }

    private static String createJsonRequestRescate(Integer cantCuotapartes, String ctaBancaria, String cuotapartista, String esTotal, String fechaAcreditacion, String fechaConcertacion, String importe, String moneda, Objeto inversionFondo, String IDSolicitud) {
        String sRequest = "{\"pSolicitudRescate\":{ " + "\"IDSolicitud\":\"" + IDSolicitud + "\"," + "\"FechaAcreditacion\":\"" + fechaAcreditacion + "\"," + "\"CantCuotapartes\":" + cantCuotapartes + "," + "\"CtaBancaria\":\"" + ctaBancaria + "\"," + "\"Cuotapartista\":\"" + cuotapartista + "\"," + "\"EsTotal\":\"" + esTotal + "\"," + "\"FechaConcertacion\":\"" + fechaConcertacion + "\"," + "\"FormaCobro\":\"" + "28" + "\"," + "\"Importe\":\"" + importe + "\"," + "\"InversionFondo\":{ " + "\"Fondo\":\"" + inversionFondo.string("Fondo") + "\"," + "\"CondicionIngresoEgreso\":\"" + inversionFondo.string("CondicionIngresoEgreso") + "\"," + "\"TipoValorCuotaParte\":\"" + inversionFondo.string("TipoValorCuotaParte") + "\"}," + "\"Moneda\":\"" + moneda + "\"," + "\"PorcGastos\":\"" + "0" + "\","
                + "\"PorcGtoBancario\":\"" + "0" + "\"," + "\"TpOrigenSol\":" + "\"IE\"" + "}}";
        return sRequest;
    }

    private static RespuestaMB respuestaMockRescateError(String csmIdAuth) {
        RespuestaMB respuesta = new RespuestaMB();
        respuesta.setEstado("ERROR_CUOTAPARTES");
        respuesta.set("csmIdAuth", csmIdAuth);
        return respuesta;
    }

    private static RespuestaMB respuestaRescateError(String mensaje, String csmIdAuth) {
        RespuestaMB respuesta = new RespuestaMB();
        if (mensaje.contains("El Cuotapartista no tiene Cuotapartes disponibles")) {
            respuesta.setEstado("ERROR_CUOTAPARTES");
        } else {
            respuesta = RespuestaMB.estado("ERROR_FUNCIONAL");
            respuesta.set("mensaje", mensaje);
        }
        respuesta.set("csmIdAuth", csmIdAuth);
        return respuesta;
    }

    private static boolean validarMockRescate() {
        return ThreadLocalRandom.current().nextBoolean();
    }


    public static RespuestaMB validarPerfilInversor(ContextoMB contexto
            , Optional<EnumPerfilInversor> perfilInstrumento
            , boolean operarBajoPropioRiesgo) {
        // TODO GB T369 - Validacion de perfiles para fondos.
        RespuestaMB resultado = new RespuestaMB();
        resultado.set("operaBajoPropioRiesgo", false);

        RespuestaMB responseConsultaPerfilInversor = perfilInversor(contexto);

        if (responseConsultaPerfilInversor.hayError()) {
            return RespuestaMB.error();
        }

        if (responseConsultaPerfilInversor.bool("tienePerfilInversor")
                && !responseConsultaPerfilInversor.bool("vencido")) {
            resultado.set("operaBajoPropioRiesgo", EnumPerfilInversor
                    .codigo(responseConsultaPerfilInversor.integer("idPerfil")).esOperaBajoPropioRiesgo()
                    || operarBajoPropioRiesgo);
        } else if (operarBajoPropioRiesgo) {
            RespuestaMB respuestaAltaPerfilPropioRiesgo = MBInversion.perfilInversorPropioRiesgo(contexto);
            if (respuestaAltaPerfilPropioRiesgo.hayError()) {
                return RespuestaMB.error();
            }
            resultado.set("operaBajoPropioRiesgo", true);
        } else {
            return RespuestaMB.error();
        }

        if (perfilInstrumento.isPresent()
                && !resultado.bool("operaBajoPropioRiesgo")
                && validacionOperacionConPerfil(responseConsultaPerfilInversor, perfilInstrumento.get().getCodigo())
                == C_RES_VAL_INVERSION_PERFIL_NO_ADECUADO) {
            return RespuestaMB.error();
        }

        return resultado;
    }

    public static RespuestaMB suscripcion(ContextoMB contexto) {
        ApiMB.eliminarCache(contexto, "PosicionCuotapartista", contexto.idCobis(), LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), "", contexto.parametros.string("cuotapartista"));

        RespuestaMB resultado = validarPerfilInversor(contexto
                , Optional.empty()
                , contexto.parametros.bool("operaFueraPerfil", false));
        if (resultado.hayError()) {
            RespuestaMB respuesta = RespuestaMB.estado("ERROR_FUNCIONAL");
            respuesta.set("mensaje", "Perfil inversor no válido para operar");
            return respuesta;
        }

        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_fondos_fuera_de_horario", "prendido_fondos_fuera_de_horario_cobis")) {
            if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_fondos_fuera_de_horario_v2", "prendido_fondos_fuera_de_horario_v2_cobis")) {
                return suscripcionV3(contexto);
            } else {
                return suscripcionV2(contexto);
            }
        } else {
            String IDCuentaBancaria = contexto.parametros.string("IDCuentaBancaria");
            String cuotapartista = contexto.parametros.string("cuotapartista");
            String fechaAcreditacion = contexto.parametros.string("fechaAcreditacion"); // validar!
            String importe = contexto.parametros.string("importe");
            Objeto inversionFondo = new Objeto();
            inversionFondo.set("Fondo", contexto.parametros.objeto("inversionFondo").string("fondo"));
            inversionFondo.set("CondicionIngresoEgreso", contexto.parametros.objeto("inversionFondo").string("condicionIngresoEgreso"));
            inversionFondo.set("TipoValorCuotaParte", contexto.parametros.objeto("inversionFondo").string("tipoValorCuotaParte"));
            String moneda = contexto.parametros.string("moneda");

            String IDSolicitud = UUID.randomUUID().toString();
            String fechaConcertacion = fechaAcreditacion;
            Objeto cuentaBancaria = new Objeto();
            Objeto formasPagoCuentaBancaria = new Objeto();
            BigDecimal valor = new BigDecimal(importe);

            RespuestaMB responseCuotapartista = cuotapartista(contexto);
            List<Objeto> cuentasBancarias = responseCuotapartista.objetos("cuotapartistas");

            Boolean isIDCuentaBancaria = false;
            for (Objeto cuentas : cuentasBancarias) {
                if (isIDCuentaBancaria) {
                    break;
                }
                for (Objeto cuenta : cuentas.objetos("ctasBancarias")) {
                    if (cuenta.string("idCuentaBancaria").equals(IDCuentaBancaria)) {
                        cuentaBancaria.set("IDCuentaBancaria", IDCuentaBancaria);
                        cuentaBancaria.set("Moneda", cuenta.string("idMoneda"));
                        cuentaBancaria.set("NumeroCuenta", cuenta.string("numeroCuenta"));
                        formasPagoCuentaBancaria.set("CuentaBancaria", IDCuentaBancaria);
                        formasPagoCuentaBancaria.set("FormaPago", "28");
                        formasPagoCuentaBancaria.set("Importe", importe);
                        isIDCuentaBancaria = true;
                        break;
                    }
                }
            }

            String tipoPersona = getTipoPersona(contexto);
            List<Objeto> tablaParametria = SqlHomebanking.getFondosParametria("SU", tipoPersona);
            for (Objeto fondos : tablaParametria) {
                if (Integer.parseInt(inversionFondo.string("Fondo")) == fondos.integer("id_fondo") && inversionFondo.string("CondicionIngresoEgreso").equals(fondos.string("cond_ingreso_egreso"))) {
                    if (fondos.bigDecimal("max_a_operar").compareTo(valor) == 0) {
                        return RespuestaMB.estado("ERROR_MONTO_MAXIMO");
                    }
                }
            }

            ApiResponseMB responseAplicaDebito = CuentasService.aplicaCreditoDebito(contexto, "debitos", contexto.cuenta(cuentaBancaria.string("NumeroCuenta")), valor, "1300", fechaConcertacion);
            if (responseAplicaDebito.hayError()) {
                RespuestaMB respuesta = RespuestaMB.estado("ERROR_FUNCIONAL");
                respuesta.set("mensaje", responseAplicaDebito.string("mensajeAlUsuario"));
                return respuesta;
            }
            RespuestaMB respuesta = new RespuestaMB();

            ApiResponseMB responseSuscripcion = RestInversiones.suscripcion(contexto, cuentaBancaria, cuotapartista, fechaAcreditacion, fechaConcertacion, formasPagoCuentaBancaria, IDSolicitud, inversionFondo, moneda, "IE");
            if (responseSuscripcion.hayError()) {
                CuentasService.reservaAplicaCreditoDebito(contexto, "debitos", contexto.cuenta(cuentaBancaria.string("NumeroCuenta")), responseAplicaDebito.string("idTransaccion"), valor, "1300", fechaConcertacion);
                respuesta = RespuestaMB.estado("ERROR_FUNCIONAL");
                respuesta.set("mensaje", responseSuscripcion.string("mensajeAlUsuario"));
                return respuesta;
            }

            List<Objeto> fondosAceptados = SqlHomebanking.getFondosAceptados(contexto.idCobis(), Integer.parseInt(inversionFondo.string("Fondo")));
            if (fondosAceptados.size() == 0) {
                SqlHomebanking.registrarFondo(contexto.idCobis(), contexto.persona().cuit(), new Date(), 24, Integer.parseInt(inversionFondo.string("Fondo")));
            }

            if (MBSalesforce.prendidoSalesforce(contexto.idCobis())) {
                String salesforce_suscripcion_y_rescate_fondos = ConfigMB.string("salesforce_suscripcion_y_rescate_fondos");
                Objeto parametros = new Objeto();
                parametros.set("IDCOBIS", contexto.idCobis());
                parametros.set("NOMBRE", contexto.persona().nombre());
                parametros.set("APELLIDO", contexto.persona().apellido());
                parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
                new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_suscripcion_y_rescate_fondos, parametros));
            }

            respuesta.set("numSolicitud", responseSuscripcion.string("NumSolicitud"));
            ProductosService.eliminarCacheProductos(contexto);
            ApiMB.eliminarCache(contexto, "PosicionCuotapartista", contexto.idCobis(), LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), "", cuotapartista);
            return respuesta;
        }
    }

    public static RespuestaMB suscripcionV3(ContextoMB contexto) {
        String IDCuentaBancaria = contexto.parametros.string("IDCuentaBancaria");
        String cuotapartista = contexto.parametros.string("cuotapartista");
        String fechaConcertacion = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String importe = contexto.parametros.string("importe");
        boolean aceptaTyCAgenda = contexto.parametros.existe("aceptaTyCAgenda")
                ? contexto.parametros.bool("aceptaTyCAgenda")
                : false;

        Objeto inversionFondo = new Objeto();
        inversionFondo.set("Fondo", contexto.parametros.objeto("inversionFondo").string("fondo"));
        inversionFondo.set("CondicionIngresoEgreso",
                contexto.parametros.objeto("inversionFondo").string("condicionIngresoEgreso"));
        inversionFondo.set("TipoValorCuotaParte",
                contexto.parametros.objeto("inversionFondo").string("tipoValorCuotaParte"));
        String fondoId = contexto.parametros.objeto("inversionFondo").string("fondo");
        String moneda = contexto.parametros.string("moneda");

        String tipoCuentaBancaria = "";
        String IDSolicitud = UUID.randomUUID().toString();
        String fechaAcreditacion = fechaConcertacion;
        Objeto cuentaBancaria = new Objeto();
        Objeto formasPagoCuentaBancaria = new Objeto();
        BigDecimal valor = new BigDecimal(importe);

        RespuestaMB responseCuotapartista = cuotapartista(contexto);
        List<Objeto> cuentasBancarias = responseCuotapartista.objetos("cuotapartistas");

        Boolean isIDCuentaBancaria = false;
        for (Objeto cuentas : cuentasBancarias) {
            if (isIDCuentaBancaria) {
                break;
            }
            for (Objeto cuenta : cuentas.objetos("ctasBancarias")) {
                if (cuenta.string("idCuentaBancaria").equals(IDCuentaBancaria)) {
                    String monedaCuenta = cuenta.string("idMoneda");
                    if (!moneda.equals(monedaCuenta)) {
                        return RespuestaMB.estado("ERROR_MONEDA_NO_COINCIDE");
                    }
                    cuentaBancaria.set("IDCuentaBancaria", IDCuentaBancaria);
                    cuentaBancaria.set("Moneda", cuenta.string("idMoneda"));
                    cuentaBancaria.set("NumeroCuenta", cuenta.string("numeroCuenta"));
                    formasPagoCuentaBancaria.set("CuentaBancaria", IDCuentaBancaria);
                    formasPagoCuentaBancaria.set("FormaPago", "28");
                    formasPagoCuentaBancaria.set("Importe", importe);
                    tipoCuentaBancaria = cuenta.string("descripcionCorta", "");
                    isIDCuentaBancaria = true;
                    break;
                }
            }
        }

        String tipoPersona = getTipoPersona(contexto);
        List<Objeto> tablaParametria = SqlHomebanking.getFondosParametria("SU", tipoPersona);
        for (Objeto fondos : tablaParametria) {
            if (Integer.parseInt(inversionFondo.string("Fondo")) == fondos.integer("id_fondo")
                    && inversionFondo.string("CondicionIngresoEgreso").equals(fondos.string("cond_ingreso_egreso"))) {
                if (fondos.bigDecimal("max_a_operar").compareTo(valor) == 0) {
                    return RespuestaMB.estado("ERROR_MONTO_MAXIMO");
                }
            }
        }

        Objeto rowFileTablaParametria = tablaParametria.stream()
                .filter(tp -> tp.integer("id_fondo") == Integer.parseInt(inversionFondo.string("Fondo"))).toList()
                .get(0);

        RespuestaMB respuesta = new RespuestaMB();
        // Parametros fuera Horario
        String horarioInicio = rowFileTablaParametria.string("hora_inicio");
        String horaInicio = horarioInicio.split(":")[0];
        String minutoInicio = horarioInicio.split(":")[1];
        String horarioFin = rowFileTablaParametria.string("hora_fin");

        String horaInicioBatchFCI = ConfigMB.string("hora_inicio_batch_fci", "22:00");
        String horaFinBatchFCI = ConfigMB.string("hora_fin_batch_fci", "00:00");

        LocalTime inicioBatch = LocalTime.parse(horaInicioBatchFCI);
        LocalTime finBatch = LocalTime.parse(horaFinBatchFCI);
        LocalTime ahora = LocalTime.now();
        LocalTime inicioFondo = LocalTime.parse(horarioInicio);
        LocalTime finFondo = LocalTime.parse(horarioFin);

        Boolean esDiaHabil = Util.isDiaHabil(contexto);
        Boolean esAntesDeApertura = LocalTime.now().isBefore(LocalTime.parse(horarioInicio));

        Boolean esOperacionFueraDeHorario = esFueraHorarioTablaParametria(rowFileTablaParametria)
                || !Util.isDiaHabil(contexto);


        String causalDebito = "1300";
        if (!esOperacionFueraDeHorario) {
            causalDebito = "1300";
        } else {
            if (!Util.isDiaHabil(contexto)) {
                causalDebito = "1321";
            } else {
                if (ahora.isBefore(inicioFondo) && ahora.isAfter(finBatch)) {
                    causalDebito = "1300";
                } else {
                    if (ahora.isBefore(inicioBatch) && ahora.isAfter(finFondo)) {
                        causalDebito = "1321";
                    }
                }
            }
        }


        ApiResponseMB responseAplicaDebito = null;
        if (!esOperacionFueraDeHorario || (esOperacionFueraDeHorario && aceptaTyCAgenda)) {
            responseAplicaDebito = CuentasService.aplicaCreditoDebito(contexto, "debitos",
                    contexto.cuenta(cuentaBancaria.string("NumeroCuenta")), valor, causalDebito, fechaConcertacion);
            if (responseAplicaDebito.hayError()) {
                respuesta = RespuestaMB.estado("ERROR_FUNCIONAL");
                respuesta.set("mensaje", responseAplicaDebito.string("mensajeAlUsuario"));
                return respuesta;
            }
        }

        if (esOperacionFueraDeHorario) {
            if (!aceptaTyCAgenda) {
                return RespuestaMB.estado("HORARIO_AGENDA_REQ_TYC");
            } else {
                ApiResponseMB responseAgenda = agendarSuscripcion(contexto, cuotapartista, fechaConcertacion, importe, inversionFondo, fondoId, moneda,
                        tipoCuentaBancaria, IDSolicitud, fechaAcreditacion, cuentaBancaria, formasPagoCuentaBancaria,
                        horaInicio, minutoInicio, esDiaHabil, esAntesDeApertura);
                if (responseAgenda.hayError()) {
                    return RespuestaMB.error();
                }
                respuesta.setEstado(("FUERA_DE_HORARIO"));
            }
        } else {
            // Flujo normal Suscripcion en Horario
            ApiResponseMB responseSuscripcion = RestInversiones.suscripcion(contexto, cuentaBancaria, cuotapartista,
                    fechaAcreditacion, fechaConcertacion, formasPagoCuentaBancaria, IDSolicitud, inversionFondo, moneda,
                    "IE");
            if (responseSuscripcion.hayError()) {
                CuentasService.reservaAplicaCreditoDebito(contexto, "debitos",
                        contexto.cuenta(cuentaBancaria.string("NumeroCuenta")),
                        responseAplicaDebito.string("idTransaccion"), valor, causalDebito, fechaConcertacion);
                respuesta = RespuestaMB.estado("ERROR_FUNCIONAL");
                respuesta.set("mensaje", responseSuscripcion.string("mensajeAlUsuario"));
                return respuesta;
            }
            // Retorno respuesta con Solicitud
            respuesta.set("numSolicitud", responseSuscripcion.string("NumSolicitud"));
        }

        List<Objeto> fondosAceptados = SqlHomebanking.getFondosAceptados(contexto.idCobis(),
                Integer.parseInt(inversionFondo.string("Fondo")));
        if (fondosAceptados.size() == 0) {
            SqlHomebanking.registrarFondo(contexto.idCobis(), contexto.persona().cuit(), new Date(), 24,
                    Integer.parseInt(inversionFondo.string("Fondo")));
        }

        if (MBSalesforce.prendidoSalesforce(contexto.idCobis())) {
            String salesforce_suscripcion_y_rescate_fondos = ConfigMB.string("salesforce_suscripcion_y_rescate_fondos");
            Objeto parametros = new Objeto();
            parametros.set("IDCOBIS", contexto.idCobis());
            parametros.set("NOMBRE", contexto.persona().nombre());
            parametros.set("APELLIDO", contexto.persona().apellido());
            parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
            new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_suscripcion_y_rescate_fondos, parametros));
        }

        ProductosService.eliminarCacheProductos(contexto);
        ApiMB.eliminarCache(contexto, "PosicionCuotapartista", contexto.idCobis(),
                contexto.parametros.string("cuotapartista"), Fecha.fechaActual());
        return respuesta;
    }

    public static RespuestaMB suscripcionV2(ContextoMB contexto) {
        String IDCuentaBancaria = contexto.parametros.string("IDCuentaBancaria");
        String cuotapartista = contexto.parametros.string("cuotapartista");
        String fechaConcertacion = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String importe = contexto.parametros.string("importe");
        boolean aceptaTyCAgenda = contexto.parametros.existe("aceptaTyCAgenda") ? contexto.parametros.bool("aceptaTyCAgenda") : false;

        Objeto inversionFondo = new Objeto();
        inversionFondo.set("Fondo", contexto.parametros.objeto("inversionFondo").string("fondo"));
        inversionFondo.set("CondicionIngresoEgreso", contexto.parametros.objeto("inversionFondo").string("condicionIngresoEgreso"));
        inversionFondo.set("TipoValorCuotaParte", contexto.parametros.objeto("inversionFondo").string("tipoValorCuotaParte"));
        String fondoId = contexto.parametros.objeto("inversionFondo").string("fondo");
        String moneda = contexto.parametros.string("moneda");

        String IDSolicitud = UUID.randomUUID().toString();
        String tipoCuentaBancaria = "";
        String fechaAcreditacion = fechaConcertacion;
        Objeto cuentaBancaria = new Objeto();
        Objeto formasPagoCuentaBancaria = new Objeto();
        BigDecimal valor = new BigDecimal(importe);

        RespuestaMB responseCuotapartista = cuotapartista(contexto);
        List<Objeto> cuentasBancarias = responseCuotapartista.objetos("cuotapartistas");

        Boolean isIDCuentaBancaria = false;
        for (Objeto cuentas : cuentasBancarias) {
            if (isIDCuentaBancaria) {
                break;
            }
            for (Objeto cuenta : cuentas.objetos("ctasBancarias")) {
                if (cuenta.string("idCuentaBancaria").equals(IDCuentaBancaria)) {
                    cuentaBancaria.set("IDCuentaBancaria", IDCuentaBancaria);
                    cuentaBancaria.set("Moneda", cuenta.string("idMoneda"));
                    cuentaBancaria.set("NumeroCuenta", cuenta.string("numeroCuenta"));
                    formasPagoCuentaBancaria.set("CuentaBancaria", IDCuentaBancaria);
                    formasPagoCuentaBancaria.set("FormaPago", "28");
                    formasPagoCuentaBancaria.set("Importe", importe);
                    tipoCuentaBancaria = cuenta.string("descripcionCorta", "");
                    isIDCuentaBancaria = true;
                    break;
                }
            }
        }

        String tipoPersona = getTipoPersona(contexto);
        List<Objeto> tablaParametria = SqlHomebanking.getFondosParametria("SU", tipoPersona);
        for (Objeto fondos : tablaParametria) {
            if (Integer.parseInt(inversionFondo.string("Fondo")) == fondos.integer("id_fondo") && inversionFondo.string("CondicionIngresoEgreso").equals(fondos.string("cond_ingreso_egreso"))) {
                if (fondos.bigDecimal("max_a_operar").compareTo(valor) == 0) {
                    return RespuestaMB.estado("ERROR_MONTO_MAXIMO");
                }
            }
        }

        Objeto rowFileTablaParametria = tablaParametria.stream().filter(tp -> tp.integer("id_fondo") == Integer.parseInt(inversionFondo.string("Fondo"))).toList().get(0);

        RespuestaMB respuesta = new RespuestaMB();
        // Parametros fuera Horario
        String horarioInicio = rowFileTablaParametria.string("hora_inicio");
        String horaInicio = horarioInicio.split(":")[0];
        String minutoInicio = horarioInicio.split(":")[1];

        Boolean esDiaHabil = Util.isDiaHabil(contexto);
        Boolean esAntesDeApertura = LocalTime.now().isBefore(LocalTime.parse(horarioInicio));

        Boolean esOperacionFueraDeHorario = esFueraHorarioTablaParametria(rowFileTablaParametria) || !Util.isDiaHabil(contexto);

        ApiResponseMB responseAplicaDebito = null;
        if (!esOperacionFueraDeHorario || (esOperacionFueraDeHorario && aceptaTyCAgenda)) {
            responseAplicaDebito = CuentasService.aplicaCreditoDebito(contexto
                    , "debitos", contexto.cuenta(cuentaBancaria.string("NumeroCuenta"))
                    , valor, "1300", fechaConcertacion);
            if (responseAplicaDebito.hayError()) {
                respuesta = RespuestaMB.estado("ERROR_FUNCIONAL");
                respuesta.set("mensaje", responseAplicaDebito.string("mensajeAlUsuario"));
                return respuesta;
            }
        }

        if (esOperacionFueraDeHorario) {
            if (!aceptaTyCAgenda) {
                return RespuestaMB.estado("HORARIO_AGENDA_REQ_TYC");
            } else {
                String fechaSQL = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                SqlHomebanking.agendarOperacionFCI(contexto.idCobis(), IDSolicitud, cuotapartista, fechaSQL, importe, fondoId, "Agendada", "MB", "Suscripcion", tipoCuentaBancaria);

                if (esDiaHabil && esAntesDeApertura) {
                    // Agendo para el dia de hoy al inicio del horario!
                    String anioACtual = String.valueOf(LocalDate.now().getYear());
                    String mesActual = fechaSQL.split("-")[1];
                    String diaActual = String.valueOf(LocalDate.now().getDayOfMonth());
                    String sRequest = createJsonRequestSuscripcion(cuentaBancaria, cuotapartista, fechaConcertacion, fechaAcreditacion, formasPagoCuentaBancaria, IDSolicitud, inversionFondo, moneda);

                    RestScheduler.crearTareaFIC(contexto, anioACtual, diaActual, horaInicio, mesActual, minutoInicio, "*", sRequest, "/v1/suscripcionAgenda");
                    respuesta.setEstado(("FUERA_DE_HORARIO"));
                } else {
                    fechaConcertacion = getProximoDiaHabil(contexto);
                    fechaAcreditacion = fechaConcertacion;
                    String anioPosterior = fechaConcertacion.split("-")[0];
                    String mesPosterior = fechaConcertacion.split("-")[1];
                    String diaPosterior = fechaConcertacion.split("-")[2];
                    String sRequest = createJsonRequestSuscripcion(cuentaBancaria, cuotapartista, fechaConcertacion, fechaAcreditacion, formasPagoCuentaBancaria, IDSolicitud, inversionFondo, moneda);
                    RestScheduler.crearTareaFIC(contexto, anioPosterior, diaPosterior, horaInicio, mesPosterior, minutoInicio, "*", sRequest, "/v1/suscripcionAgenda");
                    respuesta.setEstado(("FUERA_DE_HORARIO"));
                }
            }
        } else {
            // Flujo normal Suscripcion en Horario
            ApiResponseMB responseSuscripcion = RestInversiones.suscripcion(contexto, cuentaBancaria, cuotapartista, fechaAcreditacion, fechaConcertacion, formasPagoCuentaBancaria, IDSolicitud, inversionFondo, moneda, "IE");
            if (responseSuscripcion.hayError()) {
                CuentasService.reservaAplicaCreditoDebito(contexto, "debitos", contexto.cuenta(cuentaBancaria.string("NumeroCuenta")), responseAplicaDebito.string("idTransaccion"), valor, "1300", fechaConcertacion);
                respuesta = RespuestaMB.estado("ERROR_FUNCIONAL");
                respuesta.set("mensaje", responseSuscripcion.string("mensajeAlUsuario"));
                return respuesta;
            }
            // Retorno respuesta con Solicitud
            respuesta.set("numSolicitud", responseSuscripcion.string("NumSolicitud"));
        }

        List<Objeto> fondosAceptados = SqlHomebanking.getFondosAceptados(contexto.idCobis(), Integer.parseInt(inversionFondo.string("Fondo")));
        if (fondosAceptados.size() == 0) {
            SqlHomebanking.registrarFondo(contexto.idCobis(), contexto.persona().cuit(), new Date(), 24, Integer.parseInt(inversionFondo.string("Fondo")));
        }

        if (MBSalesforce.prendidoSalesforce(contexto.idCobis())) {
            String salesforce_suscripcion_y_rescate_fondos = ConfigMB.string("salesforce_suscripcion_y_rescate_fondos");
            Objeto parametros = new Objeto();
            parametros.set("IDCOBIS", contexto.idCobis());
            parametros.set("NOMBRE", contexto.persona().nombre());
            parametros.set("APELLIDO", contexto.persona().apellido());
            parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
            new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_suscripcion_y_rescate_fondos, parametros));
        }

        ProductosService.eliminarCacheProductos(contexto);
        ApiMB.eliminarCache(contexto, "PosicionCuotapartista", contexto.idCobis(), LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), "", cuotapartista);
        return respuesta;
    }

    public static RespuestaMB poseeCuotapartista(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();

        ApiResponseMB cuotapartistaResponse = RestInversiones.cuotapartista(contexto, null, MBInversion.tipoDocEsco(contexto.persona()), null, false, contexto.persona().esPersonaJuridica() ? contexto.persona().cuit() : contexto.persona().numeroDocumento());
        if (!cuotapartistaResponse.hayError()) {
            respuesta.set("poseeCuentaCuotapartista", true);
        } else {

            if (cuotapartistaResponse.codigo == 504 || cuotapartistaResponse.codigo == 500) {
                return RespuestaMB.estado("OPERA_MANUAL");
            }
            respuesta.set("poseeCuentaCuotapartista", false);
        }

        return respuesta.ordenar("estado", "poseeCuentaCuotapartista");
    }

    public static String tipoDocEsco(Persona persona) {
        if (persona.esPersonaJuridica()) {
            return "59";
        }

        switch (persona.idTipoDocumento()) {
            case 1:
                return "96";
            case 2:
                return "89";
            case 3:
                return "90";
            case 122:
                return "0";
            case 126:
                return "94";
            case 134:
                return "39";
            default:
                return "96";
        }
    }

    public static RespuestaMB integrantesCuentaPerfilInversor(ContextoMB contexto) {
        String idCuentaComitente = contexto.parametros.string("idCuentaComitente", null);
        CuentaComitente cuentaComitente = (idCuentaComitente == null) ? contexto.cuentaComitentePorDefecto() : contexto.cuentaComitente(idCuentaComitente);

        if (cuentaComitente == null) {
            return RespuestaMB.estado("SIN_CUENTA_COMITENTE");
        }

        RespuestaMB respuesta = new RespuestaMB();
        ApiResponseMB response = ProductosService.integrantesProducto(contexto, cuentaComitente.numero());
        if (response.hayError()) {
            return RespuestaMB.error();
        } else {
            for (Objeto titular : response.objetos()) {
                Objeto item = new Objeto();
                item.set("idCliente", titular.string("idCliente"));
                item.set("nombre", titular.string("nombre"));
                item.set("usuarioLogeado", contexto.idCobis().equals(titular.string("idCliente")));
                ApiResponseMB perfilInversorResponse = RestPersona.consultarPerfilInversor(contexto, titular.string("idCliente"));
                if (response.hayError()) {
                    return RespuestaMB.estado("ERROR_PERFIL");
                }
                for (Objeto perfil : perfilInversorResponse.objetos()) {

                    if ("".equals(perfil.string("perfilInversor", ""))) {
                        item.set("tienePerfilInversor", false);
                    } else {
                        item.set("tienePerfilInversor", true);
                        item.set("vencido", "V".equals(perfil.string("estado")));
                        item.set("idPerfil", perfil.string("perfilInversor"));
                        Objeto titularidad = titular.objeto("rol");
                        item.set("titularidad", titularidad.string("descripcion"));
                        item.set("descripcionPerfil", descripcionesPerfiles().get(perfil.string("perfilInversor")));
                    }
                    respuesta.add("titulares", item);
                }
            }
        }
        return respuesta;
    }

    private static Boolean esFueraHorarioTablaParametria(Objeto rowFileTablaparametria) {
        LocalTime time = LocalTime.now();
        Boolean esAntes = time.isBefore(LocalTime.parse(rowFileTablaparametria.string("hora_inicio")));
        Boolean esDespues = time.isAfter(LocalTime.parse(rowFileTablaparametria.string("hora_fin")));
        Boolean resultado = false;
        if (esAntes || esDespues) {
            resultado = true;
        }
        return resultado;
    }

    private static String createJsonRequestSuscripcion(Objeto cuentaBancaria, String cuotapartista, String fechaConcertacion, String fechaAcreditacion, Objeto formasPagoCuentaBancaria, String IDSolicitud, Objeto inversionFondo, String moneda) {
        String sRequest = "{\"SuscripcionSL\":{ " + "" + "\"AceptacionDocumentacionWEB\": null," + "\"CuentaBancaria\":{" + "\"IDCuentaBancaria\":\"" + cuentaBancaria.string("IDCuentaBancaria") + "\"," + "\"Moneda\":\"" + cuentaBancaria.string("Moneda") + "\"," + "\"NumeroCuenta\":\"" + cuentaBancaria.string("NumeroCuenta") + "\"}," + "\"Cuotapartista\":\"" + cuotapartista + "\"," + "\"FechaAcreditacion\":\"" + fechaAcreditacion + "\"," + "\"FechaConcertacion\":\"" + fechaConcertacion + "\"," + "\"FormasPagoCuentaBancaria\":{" + "\"CuentaBancaria\":\"" + formasPagoCuentaBancaria.string("CuentaBancaria") + "\"," + "\"FormaPago\":\"" + formasPagoCuentaBancaria.string("FormaPago") + "\"," + "\"Importe\":\"" + formasPagoCuentaBancaria.string("Importe") + "\"}," + "\"IDSolicitud\":\""
                + IDSolicitud + "\"," + "\"InversionFondo\":{" + "\"CondicionIngresoEgreso\":\"" + inversionFondo.string("CondicionIngresoEgreso") + "\"," + "\"Fondo\":\"" + inversionFondo.string("Fondo") + "\"," + "\"TipoValorCuotaParte\":\"" + inversionFondo.string("TipoValorCuotaParte") + "\"}," + "\"Moneda\":\"" + moneda + "\"," + "\"TpOrigenSol\":\"IE\"}}";
        return sRequest;

    }

    private static String getProximoDiaHabil(ContextoMB contexto) {
        ApiResponseMB responseCalendario = RestCatalogo.calendarioFechaActual(contexto);
        if (responseCalendario.hayError()) {
            return null;
        }
        String diaHabilPosterior = responseCalendario.objetos().get(0).string("diaHabilPosterior");
        return diaHabilPosterior;
    }

    public static RespuestaMB evaluarPropuestaInversionPreTransferencia(ContextoMB contexto, CuentaTercero cuentaTercero) {
        RespuestaMB respuesta = new RespuestaMB();
        EnumPropuestasInversion propuesta = EnumPropuestasInversion.NINGUNA;

        try {
            boolean propuestasInversionBHHabilitadas = MBAplicacion
                    .funcionalidadPrendida(contexto.idCobis(), "mb_mostrar_propuestas_inversion");

            boolean prendidoSuscripcion = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "mb_prendido_Fci_Suscripcion");

            if (propuestasInversionBHHabilitadas && cuentaTercero != null) {
                if (cuentaTercero.esCvu() && cuentaTercero.mismoTitularColesa(contexto.persona().cuit())) {
                    if (prendidoSuscripcion) {
                        propuesta = EnumPropuestasInversion.FONDO_MONEY_MARKET;
                    } else {
                        propuesta = EnumPropuestasInversion.NINGUNA;
                    }
                } else if (cuentaTercero.esCuentaBroker()) {
                    propuesta = EnumPropuestasInversion.ACCIONES_BONOS;
                }

                if (propuesta.alguna()) {
                    SqlResponseMB response = Util.getContador(contexto, propuesta.getCodigoNemonicoPropuesta(), new Date());
                    boolean propuestoHoy = (!response.hayError && !response.registros.isEmpty());
                    if (propuestoHoy) {
                        propuesta = EnumPropuestasInversion.NINGUNA;
                    }
                }
            }

            respuesta.set("propuesta", propuesta);
        } catch (Exception e) {
            return RespuestaMB.error();
        }

        return respuesta;
    }

    private static String tipoDocPersonaESCO(String tipoDoc) { //Todo: Servicio deprecated
        switch (tipoDoc) {
            case "LE":
                return "1";
            case "89":
                return "1";
            case "90":
                return "2";
            case "LC":
                return "2";
            case "94":
                return "3";
            case "Pasaporte":
                return "3";
            case "96":
                return "4";
            case "DNI":
                return "4";
            default:
                return "4";
        }
    }

    private static String tipoDocPersonaESCOv2(String tipoDoc) {
        switch (tipoDoc) {
            case "01":
                return "4";//D.N.I
            case "02":
                return "1";//L.E.
            case "03":
                return "2";//L.C
            case "134":
                return "39";//DNI EXT RES PAIS
            default:
                return "4";
        }
    }

    public static String fixCaracteresEspeciales(String valor) {
        String REGEX = "[^a-zA-Z]";
        String valueReturn;
        valueReturn = valor.replaceAll("Ñ", "N").replaceAll("ñ", "n");
        valueReturn = valueReturn.replaceAll(REGEX, " ");
        return valueReturn;
    }


    /**
     * El proceso de alta de cuenta inversor e inversor en BYMA demora mas de lo esperado en PROD (no asi en HOMO) quedando errores
     * en tabla [hbs].[dbo].[error_cuentaInversor] registrados por el CMP-CAJAVALORES.
     * El proceso total de alta de cuenta comitente demora mas de 10s lo que provoca errores por timeout. Por eso, se agrega delay aqui.
     * Para evitar que esos registros lleguen al proceso manual innecesariamente, volvemos a consultar el estado en BYMA unos segundos
     * despues y actualizamos el/los registro/s.
     * <p>
     * FIXME: queda pendiente optimizacion de proceso en CMP-VALORES (i.e. reutilización de TOKEN byma).
     *
     * @param contexto                 contexto
     * @param respuestaCuentacomitente Respuesta del alta de cuenta comitente
     * @return null
     */
    private static Void esperarYActualizarEstadosProcesosBYMA(ContextoMB contexto, RespuestaMB respuestaCuentacomitente) {
        boolean huboAltaInversor = respuestaCuentacomitente.existe("investorResponse"); // != null
        boolean huboAltaCuentaInversor = respuestaCuentacomitente.existe("accountResponse"); // != null

        boolean altaInversorDemoradaOConError = false;
        if (huboAltaInversor) {
            altaInversorDemoradaOConError = respuestaCuentacomitente
                    .objetos("investorResponse")
                    .stream()
                    .anyMatch(response -> !response.string("codigo").equals("200")
                            && (respuestaCuentacomitente.objeto("accountResponse").string("detalle")) != null);
        }

        boolean altaCuentaInversorDemoradaOConError = false;
        if (huboAltaCuentaInversor) {
            altaCuentaInversorDemoradaOConError = (!respuestaCuentacomitente.objeto("accountResponse").string("codigo").equals("200")
                    && (respuestaCuentacomitente.objeto("accountResponse").string("detalle")) != null);
        }

        if ((huboAltaCuentaInversor && altaCuentaInversorDemoradaOConError)
                || (huboAltaInversor && altaInversorDemoradaOConError)) {
            // Aplicamos delay solo una vez.
            ar.com.hipotecario.backend.base.Util.delay(C_DELAY_ESPERA_PROCESOS_BYMA);
        }

        if (huboAltaInversor && altaInversorDemoradaOConError) {
            // Si algún inversor asociado no se finalizo, volvemos a consultar
            // estado y corregimos error si ya lo hizo para evitar proceso manual.
            respuestaCuentacomitente
                    .objetos("investorResponse")
                    .stream()
                    .filter(response -> !response.string("codigo").equals("200"))
                    .forEach(resInversorNoFinalizado -> new Futuro<>(()
                            -> consultarYActualizarEstadoAltaInconclusaInversorBYMA(contexto, resInversorNoFinalizado.string("detalle"))));
        }

        if (huboAltaCuentaInversor && altaCuentaInversorDemoradaOConError) {
            // Si la cuenta inversor no se finalizo, volvemos a consultar
            // estado y corregimos error si ya lo hizo para evitar proceso manual.
            final String taskIdCuenta = respuestaCuentacomitente.objeto("accountResponse").string("detalle");
            new Futuro<>(()
                    -> consultarYActualizarEstadoAltaInconclusaCuentaInversorBYMA(contexto, taskIdCuenta));
        }
        return null;
    }

    private static Void consultarYActualizarEstadoAltaInconclusaInversorBYMA(ContextoMB contexto, String taskId) {
        List<String> taskIds = new ArrayList<>();
        taskIds.add(taskId);
        ApiResponseMB response = RestInversiones.consultaEstadoInversorByma(contexto, taskIds);

        // En caso de haber finalizado, actualizar estado en tabla hbs.dbo.error_cuentainversor
        if (!response.hayError()
                && response.objetos() != null
                && response.objetos().size() > 0
                && response.objetos().get(0).integer("statusCode")
                .equals(EnumEstadoAltaEntidadBYMA.EXITO.getStatusCode())) {
            new Futuro<>(()
                    -> actualizarErrorAltaEntidadBYMA(contexto, taskId, "FINALIZADO"));
        }
        return null;
    }

    private static Void consultarYActualizarEstadoAltaInconclusaCuentaInversorBYMA(ContextoMB contexto, String taskId) {
        ApiResponseMB response = RestInversiones.consultaEstadoCuentaByma(contexto, taskId);

        // En caso de haber finalizado, actualizar estado en tabla hbs.dbo.error_cuentainversor
        if (!response.hayError()
                && response.objetos("data") != null
                && response.objetos("data").size() > 0
                && response.objetos("data").get(0).integer("statusCode")
                .equals(EnumEstadoAltaEntidadBYMA.EXITO.getStatusCode())) {
            new Futuro<>(()
                    -> actualizarErrorAltaEntidadBYMA(contexto, taskId, "FINALIZADO"));
        }
        return null;
    }

    /**
     * Modifica el valor de columna [CV] con prefijo "status: {ESTADO}"
     *
     * @param contexto contexto
     * @param taskId   id de tarea devuelvo por BYMA
     * @param estado   estado - valores:
     *                 1: finalizado con exito en BYMA.
     * @return true: sin errores, sino false
     */
    private static boolean actualizarErrorAltaEntidadBYMA(ContextoMB contexto, String taskId, String estado) {
        // TODO: GB - Agregar columnas nuevas a esta tabla para manejar estado, taskId, etc.
        String sql = "UPDATE [hbs].[dbo].[error_cuentaInversor]"
                + " SET [cv] = CONCAT('status: ',?,'-', [cv])"
                + " WHERE"
                + " 	   [detalle] = 'ctaComitente'"
                + " AND [id_cobis] = ?"
                + " AND [cv] like CONCAT('%', ?)";

        SqlRequestMB sqlRequest = SqlMB.request("ActualizarErrorByma", "hbs");
        sqlRequest.sql = sql;
        String idCobis = contexto.idCobis();
        sqlRequest.add(estado);
        sqlRequest.add(idCobis);
        sqlRequest.add(taskId);

        SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
        if (sqlResponse.hayError) {
            return false;
        }
        return true;
    }


    /************************** INNER PUBLIC STRUCTS *************************/

    public enum EnumPropuestasInversion {
        NINGUNA("INV_PROP_000"),
        FONDO_MONEY_MARKET("INV_PROP_001"),
        ACCIONES_BONOS("INV_PROP_002");

        public String codigoNemonicoPropuesta;

        EnumPropuestasInversion(String codigoNemonicoPropuesta) {
            this.codigoNemonicoPropuesta = codigoNemonicoPropuesta;
        }

        public String getCodigoNemonicoPropuesta() {
            return codigoNemonicoPropuesta;
        }

        public boolean alguna() {
            return !this.equals(NINGUNA);
        }
    }

    public enum EnumMonedaInversion {
        PESO(1, "$"), DOLAR(2, "DOLAR");

        private final int id;
        private final String simbolo;

        EnumMonedaInversion(int id, String simbolo) {
            this.id = id;
            this.simbolo = simbolo;
        }

        public static EnumMonedaInversion id(int id) {
            for (EnumMonedaInversion e : values()) {
                if (e.getId() == id) {
                    return e;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        public int getId() {
            return id;
        }

        public String getSimbolo() {
            return simbolo;
        }

        public boolean esPeso() {
            return this.equals(PESO);
        }

        public boolean esDolar() {
            return this.equals(DOLAR);
        }
    }

    public enum EnumPerfilInversor {
        CONSERVADOR(1, "Perfil conservador"),
        MODERADO(2, "Perfil moderado"),
        ARRIESGADO(3, "Perfil arriesgado"),
        BAJO_PROPIO_RIESGO(4, "Opera bajo su propio riesgo"),
        NO_TIENE(5, "No tiene");

        public Integer codigo;
        public String descripcion;

        EnumPerfilInversor(Integer codigo, String descripcion) {
            this.codigo = codigo;
            this.descripcion = descripcion;
        }

        public static EnumPerfilInversor codigo(int codigo) {
            for (EnumPerfilInversor e : values()) {
                if (e.getCodigo() == codigo) {
                    return e;
                }
            }
            throw new IllegalArgumentException(String.valueOf(codigo));
        }

        public boolean esConservador() {
            return this.equals(CONSERVADOR);
        }

        public boolean esArriesgado() {
            return this.equals(ARRIESGADO);
        }

        public boolean esModerado() {
            return this.equals(MODERADO);
        }

        public boolean esOperaBajoPropioRiesgo() {
            return this.equals(BAJO_PROPIO_RIESGO);
        }

        public boolean tiene() {
            return !this.equals(NO_TIENE);
        }

        public Integer getCodigo() {
            return codigo;
        }

        public String getDescripcion() {
            return descripcion;
        }

    }

    public enum EnumEstadoAltaEntidadBYMA {
        EXITO(1, "Detailed msg not provided"),
        ERROR_2_ACCOUNT_HOLDER_MISSING(2, "Securities account holder is missing."),
        ERROR_3_UNPROCESSED_BY_CSD(3, "The provided ID does not exist or has not been processed by the CSD yet");

        public Integer statusCode;
        public String statusText;

        EnumEstadoAltaEntidadBYMA(Integer statusCode, String statusText) {
            this.statusCode = statusCode;
            this.statusText = statusText;
        }

        public static EnumEstadoAltaEntidadBYMA codigo(int statusCode) {
            for (EnumEstadoAltaEntidadBYMA e : values()) {
                if (e.getStatusCode() == statusCode) {
                    return e;
                }
            }
            throw new IllegalArgumentException(String.valueOf(statusCode));
        }

        public boolean esExito() {
            return this.equals(EXITO);
        }

        public Integer getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(Integer statusCode) {
            this.statusCode = statusCode;
        }

        public String getStatusText() {
            return statusText;
        }

        public void setStatusText(String statusText) {
            this.statusText = statusText;
        }
    }

    private static Objeto crearError(String codigo, String cuenta, String mensaje) {
        return new Objeto()
                .set("codigo", codigo)
                .set("cuenta", cuenta)
                .set("mensaje", mensaje);
    }


}