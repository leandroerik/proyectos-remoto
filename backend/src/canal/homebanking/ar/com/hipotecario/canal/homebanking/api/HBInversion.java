package ar.com.hipotecario.canal.homebanking.api;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.util.Transmit;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.lib.Concurrencia;
import ar.com.hipotecario.canal.homebanking.lib.Fecha;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Mock;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.negocio.CuentaComitente;
import ar.com.hipotecario.canal.homebanking.negocio.CuentaTercero;
import ar.com.hipotecario.canal.homebanking.negocio.Domicilio;
import ar.com.hipotecario.canal.homebanking.negocio.Persona;
import ar.com.hipotecario.canal.homebanking.negocio.PlazoFijo;
import ar.com.hipotecario.canal.homebanking.negocio.PlazoFijoLogro;
import ar.com.hipotecario.canal.homebanking.negocio.Telefono;
import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.Banco;
import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.CuentaInversor;
import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.CuentasLiquidacion;
import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.CuotasBancarias;
import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.DatosUIF;
import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.Dolares;
import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.DomicilioCuentaInversor;
import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.Moneda;
import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.Origen;
import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.Pesos;
import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.Radicacion;
import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.TipoCuentaBancaria;
import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.cuentacomitente.AltaCuentaComitenteRequestUnificado;
import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.cuentacomitente.CNV.account.CuentaInversorCNV;
import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.cuentacomitente.CNV.investor.InversorCNV;
import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.cuentacomitente.ctaunitrade.CuentaComitenteUnitrade;
import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.cuentafondos.AltaCuentaFondosRequestUnificado;
import ar.com.hipotecario.canal.homebanking.servicio.*;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.bm.hb.RescateHBBMBankProcess;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.generics.TransactionBankProcess;

public class HBInversion {

    public static Integer C_RES_VAL_INVERSION_PERFIL_ADECUADO = 0;
    public static Integer C_RES_VAL_INVERSION_PERFIL_NO_ADECUADO = 1;
    public static Integer C_RES_VAL_INVERSION_OPERA_BAJO_PROPIO_RIESGO = 2;
    private static Integer C_DELAY_ESPERA_PROCESOS_BYMA = ConfigHB.integer("delay_espera_ms_procesos_byma", 1800);
    private static final Pattern PREFIJO_NUM_GUION = Pattern.compile("^\\s*\\d+\\s*-\\s*");

    private static String nombreFondoLimpio(String raw) {
        if (raw == null) return null;
        return PREFIJO_NUM_GUION.matcher(raw).replaceFirst("").trim();
    }

    public static Respuesta consolidadaInversiones(ContextoHB contexto) {
        Respuesta respuesta = new Respuesta();

        String fechaTitulosValores = contexto.parametros.string("fecha");
        Futuro<ApiResponse> futuroTitulosValores = new Futuro<>(() -> RestInversiones.titulosValores(contexto, null, fechaTitulosValores));

        Futuro<List<PlazoFijoLogro>> plazosFijosLogrosFuturo = new Futuro<>(() -> contexto.plazosFijosLogros());

        Futuro<Map<PlazoFijoLogro, List<ApiResponse>>> plazoFijoLogrosDetalle = new Futuro<>(
                () -> PlazoFijoLogro.plazosFijosLogroDetalle(contexto));

        Futuro<Map<CuentaComitente, ApiResponse>> cuentasComitentesEspecies = new Futuro<>(
                () -> cuentasComitentesEspecies(contexto, respuesta));

        Futuro<ApiResponse> cuotapartistaResponseFuturo = new Futuro<>(
                () -> RestInversiones.cuotapartista(contexto, null, tipoDocEsco(contexto.persona()), null, false,
                        contexto.persona().esPersonaJuridica() ? contexto.persona().cuit()
                                : contexto.persona().numeroDocumento()));

        ApiResponse cuotapartistaResponse = cuotapartistaResponseFuturo.get();
        Map<String, Futuro<Respuesta>> mapaPosicion = new ConcurrentHashMap<>();
        if (!cuotapartistaResponse.hayError()) {
            if (cuotapartistaResponse.hayError() && !"1008".equals(cuotapartistaResponse.string("codigo"))) {
                respuesta.setEstadoExistenErrores();
            } else {
                for (Objeto cuotapartista : cuotapartistaResponse.objetos("CuotapartistaModel")) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String fecha = sdf.format(new Date());
                    Boolean cuotapartistaFisica = false;
                    cuotapartistaFisica = (!contexto.persona().esPersonaJuridica() && cuotapartista.bool("EsFisico"));
                    if (cuotapartistaFisica) {
                        String idCuotapartista = cuotapartista.string("IDCuotapartista");
                        Integer numeroCuotapartista = cuotapartista.integer("IDCuotapartista");
                        ContextoHB contextoClon = contexto.clonar();
                        contextoClon.parametros.set("fecha", fecha);
                        contextoClon.parametros.set("idCuotapartista", idCuotapartista);
                        contextoClon.parametros.set("numeroCuotapartista", String.valueOf(numeroCuotapartista));
                        Futuro<Respuesta> posicionFCIFuturo = new Futuro<>(() -> posicionCuotapartista(contextoClon));
                        mapaPosicion.put(cuotapartista.string("NumeroCuotapartista"), posicionFCIFuturo);
                    }
                }
            }
        }

        Map<CuentaComitente, ApiResponse> mapaResponse = cuentasComitentesEspecies.get();
        Map<String, Futuro<Respuesta>> mapaRespuesta = new ConcurrentHashMap<>();
        ApiResponse titulosValores = futuroTitulosValores.get();
        contexto.parametros.set("titulosValores", titulosValores);
        for (CuentaComitente cuentaComitente : mapaResponse.keySet()) {
            ContextoHB contextoClon = contexto.clonar();
            contextoClon.parametros.set("idCuentaComitente", cuentaComitente.id());
            contextoClon.parametros.set("titulosValores", titulosValores);
            Futuro<Respuesta> tenencias = new Futuro<>(() -> HBTitulosValores.tenenciaPosicionNegociableV2(contextoClon));
            mapaRespuesta.put(cuentaComitente.id(), tenencias);
        }

        int cantidadLogrosPesos = 0;
        int cantidadLogrosDolares = 0;
        int cantidadPlazosFijosPesos = 0;
        int cantidadPlazosFijosDolares = 0;
        int cantidadFciPesos = 0;
        int cantidadFciDolares = 0;

        BigDecimal sumaLogrosPesos = new BigDecimal(0);
        BigDecimal sumaLogrosDolares = new BigDecimal(0);
        BigDecimal porcentajeLogrosPesos = new BigDecimal(0);
        BigDecimal porcentajeLogrosDolares = new BigDecimal(0);
        BigDecimal sumaPlazosFijosPesos = new BigDecimal(0);
        BigDecimal sumaPlazosFijosDolares = new BigDecimal(0);
        BigDecimal porcentajePlazosFijosPesos = new BigDecimal(0);
        BigDecimal porcentajePlazosFijosDolares = new BigDecimal(0);
        BigDecimal sumaFciPesos = new BigDecimal(0);
        BigDecimal sumaFciDolares = new BigDecimal(0);
        BigDecimal porcentajeFciPesos = new BigDecimal(0);
        BigDecimal porcentajeFciDolares = new BigDecimal(0);

//		int cantidadLicitacionesPrimariasPesos = 0;
        BigDecimal sumaLicitacionesPrimariasPesos = new BigDecimal(0);
        BigDecimal porcentajeLicitacionesPrimariasPesos = new BigDecimal(0);

        int cantidadTitulosValoresPesos = 0;
        BigDecimal sumaTitulosValoresPesos = new BigDecimal(0);
        BigDecimal porcentajeTitulosValoresPesos = new BigDecimal(0);

        BigDecimal sumaTotalPesos = new BigDecimal(0);
        BigDecimal sumaTotalDolares = new BigDecimal(0);

        Objeto informacion = new Objeto();
        List<Objeto> erroresRespuesta = new ArrayList<Objeto>();

        Map<PlazoFijoLogro, List<ApiResponse>> detalle = plazoFijoLogrosDetalle.get();

        for (PlazoFijoLogro plazoFijoLogro : plazosFijosLogrosFuturo.get()) {
            String estado = plazoFijoLogro.idEstado();
            if ("V".equals(estado) || "A".equals(estado)) {
                for (Integer id = 1; id <= plazoFijoLogro.cantidadPlazosFijos(); ++id) {
                    if ("A".equals(plazoFijoLogro.itemIdEstado(id, detalle))) {
                        if ("80".equals(plazoFijoLogro.idMoneda())) {
                            sumaLogrosPesos = sumaLogrosPesos.add(plazoFijoLogro.itemMontoInicial(id, detalle));
                            cantidadLogrosPesos++;
                        }
                        if ("2".equals(plazoFijoLogro.idMoneda())) {
                            sumaLogrosDolares = sumaLogrosDolares.add(plazoFijoLogro.itemMontoInicial(id, detalle));
                            cantidadLogrosDolares++;
                        }
                    }
                }
            }
        }

        for (Objeto item : ProductosService.productos(contexto).objetos("errores")) {
            if ("plazosFijos".equals(item.string("codigo"))) {
                // return Respuesta.estado("ERROR_CONSOLIDADA");
            }
        }

        for (PlazoFijo item : contexto.plazosFijos()) {
            if (item.esValido() && !item.esPlazoFijoLogros()) {
                if (item.esPesos()) {
                    sumaPlazosFijosPesos = sumaPlazosFijosPesos.add(item.importeInicial());
                    cantidadPlazosFijosPesos++;
                }
                if (item.esDolares()) {
                    sumaPlazosFijosDolares = sumaPlazosFijosDolares.add(item.importeInicial());
                    cantidadPlazosFijosDolares++;
                }
            }
        }

        for (CuentaComitente cuentaComitente : mapaResponse.keySet()) {
            // TODO: DLV-50998
            try {
                //contexto.parametros.set("idCuentaComitente", cuentaComitente.id());
                //Respuesta tenencias = HBTitulosValores.tenenciaPosicionNegociableV2(contexto);
                Respuesta tenencias = mapaRespuesta.get(cuentaComitente.id()).get();

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

//			ApiResponse response = mapaResponse.get(cuentaComitente);
//			if (response.hayError()) {
//				respuesta.setEstadoExistenErrores();
//				continue;
//			}
//			
//			for (Objeto item : response.objetos()) {
//				if (!"".equals(item.string("codigoEspecie"))) {
//					BigDecimal saldoValuado = item.bigDecimal("valorizacion");
//					if(HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_inversiones_byma")) {
//						saldoValuado = licitacionesByma(contexto, item);		
//						if (saldoValuado == BigDecimal.ZERO) {
//							saldoValuado = item.bigDecimal("valorizacion");
//						}
//					}
//					cantidadTitulosValoresPesos++;
//					sumaTitulosValoresPesos = sumaTitulosValoresPesos.add(saldoValuado);
//				}
//			}
        }


        if (cuotapartistaResponse.hayError()) {
            if (cuotapartistaResponse.codigo == 504 || cuotapartistaResponse.codigo == 500) {
                erroresRespuesta.add(crearError("ERR_FCI", null, "Error en servicio"));
                // continue
            }
        } else {
            if (cuotapartistaResponse.hayError() && !"1008".equals(cuotapartistaResponse.string("codigo"))) {
                respuesta.setEstadoExistenErrores();
            } else {
                // Por las dudas nos aseguramos que no se usen estos nombres de parametros (usar
                // otro nombre
                // en caso de ser necesario).
                if (contexto.parametros.existe("fecha") || contexto.parametros.existe("IDCuotapartista")) {
                    return Respuesta.estado("ERROR_CONSOLIDADA");
                }
                Boolean cuotapartistaFisica = false;

                for (Objeto cuotapartista : cuotapartistaResponse.objetos("CuotapartistaModel")) {
                    cuotapartistaFisica = (!contexto.persona().esPersonaJuridica() && cuotapartista.bool("EsFisico"));
                    // Contamos solo cuotapartistas del cliente.
                    if (cuotapartistaFisica) {
                        String idCuotapartista = cuotapartista.string("IDCuotapartista");
                        //Integer numeroCuotapartista = cuotapartista.integer("IDCuotapartista");
                        //contexto.parametros.set("fecha", fecha);
                        //contexto.parametros.set("idCuotapartista", idCuotapartista);
                        //contexto.parametros.set("numeroCuotapartista", String.valueOf(numeroCuotapartista));
                        //Respuesta posicionFCI = posicionCuotapartista(contexto);
                        Respuesta posicionFCI = mapaPosicion.get(cuotapartista.string("NumeroCuotapartista")).get();

                        if (posicionFCI.string("estado").equals("ERROR")) {
                            erroresRespuesta.add(crearError("ERR_FCI", idCuotapartista, ""));
                            continue;
                        }

                        if (!posicionFCI.string("estado").equals("SIN_POSICION")) {
                            sumaFciPesos = sumaFciPesos.add(posicionFCI.bigDecimal("totalFondosPesos"));
                            sumaFciDolares = sumaFciDolares.add(posicionFCI.bigDecimal("totalFondosDolares"));
                            cantidadFciPesos += posicionFCI.integer("cantidadFciPesos");
                            cantidadFciDolares += posicionFCI.integer("cantidadFciDolares");
                        }
                    }
                }
            }
        }

        sumaTotalPesos = sumaTotalPesos.add(sumaLogrosPesos);
        sumaTotalPesos = sumaTotalPesos.add(sumaPlazosFijosPesos);
        sumaTotalPesos = sumaTotalPesos
                .add(sumaTitulosValoresPesos == null ? new BigDecimal(0) : sumaTitulosValoresPesos);
        sumaTotalPesos = sumaTotalPesos.add(sumaLicitacionesPrimariasPesos);
        sumaTotalPesos = sumaTotalPesos.add(sumaFciPesos);
        sumaTotalDolares = sumaTotalDolares.add(sumaLogrosDolares);
        sumaTotalDolares = sumaTotalDolares.add(sumaPlazosFijosDolares);
        sumaTotalDolares = sumaTotalDolares.add(sumaFciDolares);

        porcentajeLogrosPesos = porcentaje(sumaLogrosPesos, sumaTotalPesos);
        porcentajeLogrosDolares = porcentaje(sumaLogrosDolares, sumaTotalDolares);
        porcentajePlazosFijosPesos = porcentaje(sumaPlazosFijosPesos, sumaTotalPesos);
        porcentajePlazosFijosDolares = porcentaje(sumaPlazosFijosDolares, sumaTotalDolares);
        porcentajeLicitacionesPrimariasPesos = porcentaje(sumaLicitacionesPrimariasPesos, sumaTotalPesos);
        porcentajeTitulosValoresPesos = porcentaje(
                sumaTitulosValoresPesos == null ? new BigDecimal(0) : sumaTitulosValoresPesos, sumaTotalPesos);
        porcentajeFciPesos = porcentaje(sumaFciPesos, sumaTotalPesos);
        porcentajeFciDolares = porcentaje(sumaFciDolares, sumaTotalDolares);

        informacion.set("cantidadLogrosPesos", cantidadLogrosPesos);
        informacion.set("logrosPesos", sumaLogrosPesos);
        informacion.set("logrosPesosFormateado", Formateador.importe(sumaLogrosPesos));
        informacion.set("cantidadLogrosDolares", cantidadLogrosDolares);
        informacion.set("logrosDolares", sumaLogrosDolares);
        informacion.set("logrosDolaresFormateado", Formateador.importe(sumaLogrosDolares));
        informacion.set("cantidadPlazosFijosPesos", cantidadPlazosFijosPesos);
        informacion.set("plazosFijosPesos", sumaPlazosFijosPesos);
        informacion.set("plazosFijosPesosFormateado", Formateador.importe(sumaPlazosFijosPesos));
        informacion.set("cantidadPlazosFijosDolares", cantidadPlazosFijosDolares);
        informacion.set("plazosFijosDolares", sumaPlazosFijosDolares);
        informacion.set("plazosFijosDolaresFormateado", Formateador.importe(sumaPlazosFijosDolares));
        informacion.set("cantidadTitulosValoresPesos", cantidadTitulosValoresPesos);
        informacion.set("titulosValoresPesos",
                sumaTitulosValoresPesos == null ? new BigDecimal(0) : sumaTitulosValoresPesos);
        informacion.set("titulosValoresPesosFormateado",
                Formateador.importe(sumaTitulosValoresPesos == null ? new BigDecimal(0) : sumaTitulosValoresPesos));
        informacion.set("cantidadFciPesos", cantidadFciPesos);
        informacion.set("fciPesos", sumaFciPesos);
        informacion.set("fciPesosFormateado", Formateador.importe(sumaFciPesos));
        informacion.set("cantidadFciDolares", cantidadFciDolares);
        informacion.set("fciDolares", sumaFciDolares);
        informacion.set("fciDolaresFormateado", Formateador.importe(sumaFciDolares));
        informacion.set("totalPesos", sumaTotalPesos);
        informacion.set("totalPesosFormateado", Formateador.importe(sumaTotalPesos));
        informacion.set("totalDolares", sumaTotalDolares);
        informacion.set("totalDolaresFormateado", Formateador.importe(sumaTotalDolares));

        informacion.set("porcentajeLogrosPesos", porcentajeLogrosPesos);
        informacion.set("porcentajeLogrosDolares", porcentajeLogrosDolares);
        informacion.set("porcentajePlazosFijosPesos", porcentajePlazosFijosPesos);
        informacion.set("porcentajePlazosFijosDolares", porcentajePlazosFijosDolares);
        informacion.set("porcentajeLicitacionesPrimariasPesos", porcentajeLicitacionesPrimariasPesos);
        informacion.set("porcentajeTitulosValoresPesos", porcentajeTitulosValoresPesos);
        informacion.set("porcentajeFciPesos", porcentajeFciPesos);
        informacion.set("porcentajeFciDolares", porcentajeFciDolares);

        Date hoy = new Date();
        informacion.set("fechaActual", new SimpleDateFormat("dd/MM/yyyy").format(hoy));

        respuesta.set("informacion", informacion);
        respuesta.set("errores", erroresRespuesta);
        return respuesta;
    }

    public static Respuesta licitaciones(ContextoHB contexto) {
        ApiResponse response = InversionesService.inversionesGetLicitaciones(contexto);
        if (response.hayError()) {
            return Respuesta.error();
        }
        Respuesta respuesta = new Respuesta();
        Objeto datoFinal = new Objeto();
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
                    if ("TASA".equals(especie.string("Criterio.Tipo")) && "TCO".equals(tramo.string("Tipo"))) {
                        itemEspecie.set("precioMaximo", especie.bigDecimal("Criterio.Maximo"));
                        itemEspecie.set("precioMinimo", especie.bigDecimal("Criterio.Minimo"));
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

    public static Respuesta altaSuscripcionLicitacion(ContextoHB contexto) {
        String idCuenta = contexto.parametros.string("idCuenta");
        String idCuentaComitente = contexto.parametros.string("idCuentaComitente");
        String idEspecie = contexto.parametros.string("idEspecie");
        Integer cantidad = contexto.parametros.integer("cantidad");
        BigDecimal valor = contexto.parametros.bigDecimal("valor");
        boolean operaFueraDePerfil = contexto.parametros.bool("operaFueraDePerfil");
        String telefonoIngresado = contexto.parametros.string("telefono", null);

        Cuenta cuenta = contexto.cuenta(idCuenta);
        if (cuenta == null) {
            return Respuesta.estado("CUENTA_INCORRECTA");
        }

        CuentaComitente cuentaComitente = contexto.cuentaComitente(idCuentaComitente);
        if (cuentaComitente == null) {
            return Respuesta.estado("CUENTA_COMITENTE_INCORRECTA");
        }

        Respuesta resultado = validarPerfilInversor(
                contexto,
                Optional.of(HBInversion.EnumPerfilInversor.ARRIESGADO),
                operaFueraDePerfil
        );
        if (resultado.hayError()) {
            return Respuesta.estado("ERROR_FUNCIONAL")
                    .set("mensaje", "Perfil inversor incorrecto para operar");
        }
        boolean operaBajoPropioRiesgo = resultado.bool("operaBajoPropioRiesgo");


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

        ApiRequest request = Api.request("licitacionesAltaSuscripcion", "inversiones", "POST", "/v1/cuentascomitentes/licitaciones", contexto);

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
        request.body("operaFueraDePerfil", operaBajoPropioRiesgo);
        request.body("sucursal", cuenta.sucursal());
        request.body("telefonoContacto", telefono);
        request.body("tipoInversor", "HB");
        request.body("tipoPersona", "F");
        request.body("tramo", tramo);

        ApiResponse response = Api.response(request);
        if (response.hayError()) {
            if ("202".equals(response.string("codigo"))) {
                Respuesta respuesta = Respuesta.estado("PERFIL_INVERSOR_VENCIDO");
                respuesta.set("mensaje", response.string("mensajeAlUsuario"));
                return respuesta;
            }
            if (response.string("codigo").equals("2023")) {
                return Respuesta.estado("OPERACION_ARRIESGADA");
            }
            if (response.string("codigo").equals("201")) {
                return Respuesta.estado("OPERACION_ARRIESGADA");
            }
            if (!response.string("mensajeAlUsuario").isEmpty()) {
                Respuesta respuesta = Respuesta.estado("ERROR_FUNCIONAL");
                respuesta.set("mensaje", response.string("mensajeAlUsuario"));
                return respuesta;
            }
            return Respuesta.error();
        }

        Respuesta respuesta = new Respuesta();
        respuesta.set("numero", response.string("numero"));

        generarComprobanteLicitacion(contexto, response.string("numero"), new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()), codigoLicitacion, descripcionLicitacion, codigoEspecie, descripcionEspecie, cantidad, valor, tramo, cuentaComitente.numero(), cuenta.numero());
        return respuesta;
    }

    public static String generarComprobanteLicitacion(ContextoHB contexto, String numero, String fecha, String codigoLicitacion, String descripcionLicitacion, String codigoEspecie, String descripcionEspecie, Integer cantidad, BigDecimal valor, String tramo, String cuentaComitenteNumero, String cuentaNumero) {
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
        contexto.sesion.comprobantes.put(idComprobante, comprobante);
        return idComprobante;
    }

    public static Map<CuentaComitente, ApiResponse> cuentasComitentesEspecies(ContextoHB contexto, Respuesta respuesta) {
        List<CuentaComitente> cuentasComitentes = contexto.cuentasComitentes();
        Map<CuentaComitente, ApiResponse> mapResponse = new HashMap<>();

        ExecutorService executorService = Concurrencia.executorService(cuentasComitentes);
        for (CuentaComitente cuentaComitente : cuentasComitentes) {
            executorService.submit(() -> {
                ApiRequest request = Api.request("CuentaComitenteEspecies", "inversiones", "GET", "/v1/cuentascomitentes/{id}/especies", contexto);
                request.path("id", cuentaComitente.numero());
                request.query("idcliente", contexto.idCobis());
                request.cacheSesion = true;
                ApiResponse response = Api.response(request, cuentaComitente.id());
                mapResponse.put(cuentaComitente, response);
            });
        }
        Concurrencia.esperar(executorService, respuesta);

        return mapResponse;
    }

    public static Respuesta perfilInversor(ContextoHB contexto) {
        Respuesta respuesta = new Respuesta();
        ApiResponse response = RestPersona.perfilInversor(contexto);
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
            return Respuesta.estado(error);

        }
        for (Objeto item : response.objetos()) {
            respuesta = datosPerfilInversor(item, respuesta);
        }
        return respuesta;
    }

    private static Respuesta datosPerfilInversor(Objeto item, Respuesta respuesta) {
        if ("".equals(item.string("perfilInversor", ""))) {
            respuesta.set("tienePerfilInversor", false);
        } else {
            respuesta.set("tienePerfilInversor", true);
            respuesta.set("vencido", "V".equals(item.string("estado")));
            respuesta.set("idPerfil", item.string("perfilInversor"));
            respuesta.set("descripcionPerfil", descripcionesPerfiles().get(item.string("perfilInversor")));
        }
        return respuesta;
    }

    public static Respuesta integrantesCuentaPerfilInversor(ContextoHB contexto) {
        // 1. Obtengo la cuenta comitente
        String idCuentaComitente = contexto.parametros.string("idCuentaComitente", null);
        CuentaComitente cuentaComitente = (idCuentaComitente == null)
                ? contexto.cuentaComitentePorDefecto()
                : contexto.cuentaComitente(idCuentaComitente);

        if (cuentaComitente == null) {
            return Respuesta.estado("SIN_CUENTA_COMITENTE");
        }

        // 2. Forzar expiración de cache de integrantes
        Api.eliminarCache(contexto, "integrantesProducto", cuentaComitente.numero());

        // 3. Llamada para traer los integrantes (ya sin cache)
        ApiResponse integrantesResponse =
                ProductosService.integrantesProductoCuentaCotitular(contexto, cuentaComitente.numero());
        if (integrantesResponse.hayError()) {
            return Respuesta.error();
        }

        List<Objeto> titulares = integrantesResponse.objetos();
        String idLogueado = contexto.idCobis();
        Map<String, Futuro<ApiResponse>> futuros = new HashMap<>();

        // 4. Disparo en paralelo cada consulta de perfil (con contexto clonado)
        for (Objeto t : titulares) {
            String idCliente = t.string("idCliente");
            ContextoHB ctx2 = contexto.clonar();

            Futuro<ApiResponse> f = idLogueado.equals(idCliente)
                    ? new Futuro<>(() -> RestPersona.consultarPerfilInversor(ctx2, idCliente))
                    : new Futuro<>(() -> RestPersona.consultarPerfilInversorCuentaCotitular(ctx2, idCliente));

            futuros.put(idCliente, f);
        }

        // 5. Armo la respuesta, marcando “sin perfil” pero sin abortar si falla una llamada
        Respuesta respuesta = new Respuesta();
        for (Objeto t : titulares) {
            String idCliente = t.string("idCliente");
            Objeto item = new Objeto();
            item.set("idCliente", idCliente)
                    .set("nombre", t.string("nombre"))
                    .set("usuarioLogeado", idLogueado.equals(idCliente))
                    .set("titularidad", t.objeto("rol").string("descripcion"));

            try {
                ApiResponse pr = futuros.get(idCliente).get();
                if (!pr.hayError() && !pr.objetos().isEmpty()) {
                    Objeto perf = pr.objetos().get(0);
                    String pid = perf.string("perfilInversor", "");
                    if (!pid.isEmpty()) {
                        item.set("tienePerfilInversor", true)
                                .set("vencido", "V".equals(perf.string("estado")))
                                .set("idPerfil", pid)
                                .set("descripcionPerfil", descripcionesPerfiles().get(pid));
                    } else {
                        item.set("tienePerfilInversor", false);
                    }
                } else {
                    item.set("tienePerfilInversor", false);
                }
            } catch (Exception ex) {
                // si falla pido perfil, lo dejo sin perfil pero sigo con los demás
                item.set("tienePerfilInversor", false);
            }

            respuesta.add("titulares", item);
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

    public static Respuesta perfilInversorPropioRiesgo(ContextoHB contexto) {
        Respuesta respuesta = new Respuesta();
        ApiResponse responseConsulta = RestPersona.perfilInversor(contexto);
        if (responseConsulta.hayError()) {
            String error = "ERROR";
            return Respuesta.estado(error);
        }
        String operacion = "I";
        for (Objeto item : responseConsulta.objetos()) {
            if (!"".equals(item.string("perfilInversor", ""))) {
                operacion = "U";
            }
        }
        ApiResponse response = RestPersona.actualizaPerfilInversor(contexto, "4", operacion);
        if (response.hayError()) {
            String error = "ERROR";
            return Respuesta.estado(error);
        }
        return respuesta;
    }

    public static BigDecimal porcentaje(BigDecimal num, BigDecimal total) {
        BigDecimal cero = new BigDecimal(0);
        if (total.compareTo(cero) <= 0) {
            return cero;
        }
        return num.multiply(new BigDecimal(100)).divide(total, RoundingMode.CEILING);
    }

    public static BigDecimal porcentaje(BigDecimal num, BigDecimal total, int decimales, Optional<RoundingMode> modoRedondeo) {
        BigDecimal cero = new BigDecimal(0);
        if (total.compareTo(cero) <= 0) {
            return cero;
        }
        return num.multiply(new BigDecimal(100)).divide(total, decimales
                , modoRedondeo.isPresent() ? modoRedondeo.get() : RoundingMode.CEILING);
    }


    public static Respuesta altaCuentaInversorContingencia(ContextoHB contexto) {
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

        for (String tipoProducto : Objeto.listOf("UNI", "CTAPART")) {
            SqlRequest sqlRequest = Sql.request("AltaCuentaInversor", "homebanking");
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
            sqlRequest.add(HBCatalogo.mapaProfesiones().get(idProfesion) != null ? HBCatalogo.mapaProfesiones().get(idProfesion) : ""); // sp_profesionDesc
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
            sqlRequest.add("HB"); // sp_canal

            SqlResponse sqlResponse = Sql.response(sqlRequest);
            if (sqlResponse.hayError) {
                return Respuesta.error();
            }
        }

        return new Respuesta();
    }

    public static Respuesta horarioCuentaInversor(ContextoHB contexto) {
        LocalTime time = LocalTime.now();

        String apertura = ConfigHB.string("hb_hora_inicio_cuenta_inversor");
        String cierre = ConfigHB.string("hb_hora_cierre_cuenta_inversor");

        Boolean esAntes = time.isBefore(LocalTime.parse(apertura));
        Boolean esDespues = time.isAfter(LocalTime.parse(cierre));
        if (!Util.isDiaHabil(contexto) || esAntes || esDespues) {
            return new Respuesta().setEstado("FUERA_DE_HORARIO").set("apertura", apertura).set("cierre", cierre);
        }
        return Respuesta.exito();
    }

    public static Respuesta altaCuentaComitente(ContextoHB contexto, Telefono telefono, Cuenta cuentaPesos, Cuenta cuentaDolar, Domicilio domicilio, Persona persona) {
        List<InversorCNV> inversorCNVList = new ArrayList<>();
        InversorCNV inversorCNVTitular = new InversorCNV(); ///paquetecuentacomitente aun no esta preparado para recibir cotitulares.
        CuentaComitenteUnitrade ctaUnitrade;
        try {
            ctaUnitrade = CuentaComitenteUnitrade.getValue(contexto.persona(), domicilio, telefono, cuentaPesos, cuentaDolar);
        } catch (Exception e) {
            return Respuesta.error().set("mensaje", "Error al obtener datos");
        }

        inversorCNVTitular = InversorCNV.getInversorCNVTitular(contexto, persona, domicilio);
        if (inversorCNVTitular == null) {
            return Respuesta.error().set("mensaje", "Error al obtener el titular.");
        }

//		Servicio que busca un inversor en caja nacional de valores
//		ApiRestInversiones.getInversorCNVByCuit(contexto ,inversorCNVList.get(0).getNumIdentificador().toString());

        //llamar al servicio para filtrar los que aun no estan dados de alta
        inversorCNVList.add(inversorCNVTitular);
        CuentaInversorCNV account = CuentaInversorCNV.getValue(contexto.persona());

        AltaCuentaComitenteRequestUnificado comitenteUnificado = new AltaCuentaComitenteRequestUnificado();
        comitenteUnificado.setCuentaComitente(ctaUnitrade);
        comitenteUnificado.setInversorCNVList(inversorCNVList);
        //todo: agregar en account los cotitulares cuando el servicio lo permita
        comitenteUnificado.setCuentaInversorCNV(account);


        ApiResponse responseInversorCNV = RestInversiones.altaCuentaComitente(contexto, comitenteUnificado);
        if (responseInversorCNV.hayError()) {
            return Respuesta.error().set("cuenta", responseInversorCNV.objetos());
        }
        return Respuesta.exito().set("cuenta", responseInversorCNV);
    }

    public static Respuesta altaCuentaFondos(ContextoHB contexto, Telefono telefono, Cuenta cuentaPesos, Cuenta cuentaDolar, Domicilio domicilio, Persona persona) {
        AltaCuentaFondosRequestUnificado cuentaFondos = new AltaCuentaFondosRequestUnificado();
        try {
            cuentaFondos = AltaCuentaFondosRequestUnificado.getValue(contexto, telefono, domicilio, persona, cuentaPesos, cuentaDolar);
        } catch (Exception e) {
            return Respuesta.error().set("mensaje", "Error al obtener datos cuentaFondos");
        }

        String valor = tipoDocPersonaESCOv2(persona.idTipoDocumentoString());
        ApiResponse responseSelectByDocPersona = RestInversiones.selectPersonaByDoc(contexto, cuentaFondos.getNumDoc(), valor);
        if (responseSelectByDocPersona.hayError()) {
            return Respuesta.error();
        }
        String idPersonaFondo = responseSelectByDocPersona.bool("EstaAnulado") ? "" : responseSelectByDocPersona.string("IDPersona", null);
        cuentaFondos.setIdPersonaFondo(idPersonaFondo);

        ApiResponse responseCuentaFondos = RestInversiones.altaCuentasFondos(contexto, cuentaFondos);
        if (responseCuentaFondos.hayError()) {
            return Respuesta.error().set("cuenta", responseCuentaFondos.objetos());
        }

        return Respuesta.exito().set("cuenta", responseCuentaFondos);
    }

    public static Respuesta altaCuentaInversorV2(ContextoHB contexto) {
        Respuesta horarioCuentaInversor = horarioCuentaInversor(contexto);
        if (!horarioCuentaInversor.string("estado").equalsIgnoreCase("0")) {
            return horarioCuentaInversor;
        }
        //Llamado de constingencia
        try {
            new Futuro<>(() -> altaCuentaInversorContingencia(contexto));
        } catch (Exception e) {
            return Respuesta.error();
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

        if (cuentaPesos == null) {
            return Respuesta.error().set("mensaje", "Cuenta " + idCuentaAsociada + " no encontrada");
        }

        Futuro<Respuesta> responseCuentaComitenteF = new Futuro<>(() -> altaCuentaComitente(contexto, telefonoF.get(), cuentaPesos, cuentaDolar, domicilioF.get(), personaF.get()));
        Futuro<Respuesta> responseCuentaFondosF = new Futuro<>(() -> altaCuentaFondos(contexto, telefonoF.get(), cuentaPesos, cuentaDolar, domicilioF.get(), personaF.get()));

        Respuesta respuestaCuentacomitente = responseCuentaComitenteF.get();
        Respuesta respuestaCuentaFondos = responseCuentaFondosF.get();

        if (respuestaCuentacomitente.hayError() && respuestaCuentaFondos.hayError()) { //
            return Respuesta.error();
        }
        try {
            ProductosService.eliminarCacheProductos(contexto);
            Api.eliminarCache(contexto, "Cuotapartista", contexto.idCobis());
        } catch (Exception e) {
        }

        if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_espera_procesos_byma")) {
            // Lanzamos verificacion de estados no bloqueante para actualizar estados.
            new Futuro<>(()
                    -> esperarYActualizarEstadosProcesosBYMA(contexto, respuestaCuentacomitente));
        }

        return Respuesta.exito();
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
     * @return
     */
    private static Void esperarYActualizarEstadosProcesosBYMA(ContextoHB contexto, Respuesta respuestaCuentacomitente) {
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

    private static Void consultarYActualizarEstadoAltaInconclusaInversorBYMA(ContextoHB contexto, String taskId) {
        List<String> taskIds = new ArrayList<>();
        taskIds.add(taskId);
        ApiResponse response = RestInversiones.consultaEstadoInversorByma(contexto, taskIds);

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

    private static Void consultarYActualizarEstadoAltaInconclusaCuentaInversorBYMA(ContextoHB contexto, String taskId) {
        ApiResponse response = RestInversiones.consultaEstadoCuentaByma(contexto, taskId);

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
    private static boolean actualizarErrorAltaEntidadBYMA(ContextoHB contexto, String taskId, String estado) {
        // TODO: GB - Agregar columnas nuevas a esta tabla para manejar estado, taskId, etc.
        String sql = "UPDATE [hbs].[dbo].[error_cuentaInversor]"
                + " SET [cv] = CONCAT('status: ',?,'-', [cv])"
                + " WHERE"
                + " 	   [detalle] = 'ctaComitente'"
                + " AND [id_cobis] = ?"
                + " AND [cv] like CONCAT('%', ?)";

        SqlRequest sqlRequest = Sql.request("ActualizarErrorByma", "hbs");
        sqlRequest.sql = sql;
        String idCobis = contexto.idCobis();
        sqlRequest.add(estado);
        sqlRequest.add(idCobis);
        sqlRequest.add(taskId);

        SqlResponse sqlResponse = Sql.response(sqlRequest);
        if (sqlResponse.hayError) {
            return false;
        }
        return true;
    }

    public static Respuesta altaCuentaInversor(ContextoHB contexto) {

        if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "deshabilitar_alta_inversor")) {
            return Respuesta.error();
        }

        if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_alta_ci_v2", "prendido_alta_ci_v2_cobis")) {
            return altaCuentaInversorV2(contexto);
        }

        return altaCuentaInversorV1(contexto);
    }

    public static Respuesta altaCuentaInversorV1(ContextoHB contexto) {//Todo: Servicio deprecated!
        Respuesta horarioCuentaInversor = horarioCuentaInversor(contexto);
        if (!horarioCuentaInversor.string("estado").equalsIgnoreCase("0")) {
            return horarioCuentaInversor;
        }
        //Llamado de constingencia
        try {
            new Futuro<>(() -> altaCuentaInversorContingencia(contexto));
        } catch (Exception e) {
            return Respuesta.error();
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
            return Respuesta.error();
        }
        CuentaInversor cuentaInversor = getValueAltaCuentaInvor(contexto, cuentaPesos, cuentaDolar, persona, domicilio, telefono);

        String valor = tipoDocPersonaESCO(cuentaInversor.getTipoDoc());
        ApiResponse responseSelectByDocPersona = RestInversiones.selectPersonaByDoc(contexto, cuentaInversor.getNumDoc(), valor);
        if (responseSelectByDocPersona.hayError()) {
            return Respuesta.error();
        }

        String idPersonaFondo = responseSelectByDocPersona.bool("EstaAnulado") ? "" : responseSelectByDocPersona.string("IDPersona", null);
        cuentaInversor.setIdPersonaFondo(idPersonaFondo);

        ApiResponse responseAltaCuentas = RestInversiones.altaCuentaInversor(contexto, cuentaInversor);
        if (responseAltaCuentas.hayError()) {
            return Respuesta.error();
        }

        Respuesta respuesta = new Respuesta();
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
            ApiResponse relacionUnitrade = ProductosService.relacionClienteProducto(contexto, relacion);
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
            ApiResponse relacionCuotapartista = ProductosService.relacionClienteProducto(contexto, relacion);
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
            Api.eliminarCache(contexto, "Cuotapartista", contexto.idCobis());
        } catch (Exception e) {
        }
        return respuesta;
    }

    public static CuentaInversor getValueAltaCuentaInvor(ContextoHB contexto, Cuenta cuentaPesos, Cuenta cuentaDolar, Persona persona, Domicilio domicilio, Telefono telefono) {
        Integer idPaisNacimiento = contexto.parametros.integer("idPaisNacimiento", contexto.persona().idPaisNacimiento());
//		String ciudadNacimiento = contexto.parametros.string("ciudadNacimiento");
        Date fechaNacimiento = contexto.parametros.date("fechaNacimiento", "dd/MM/yyyy", contexto.persona().fechaNacimiento());

        String codigoPostal = contexto.parametros.string("codigoPostal", null);
        String calleDomicilio = contexto.parametros.string("calleDomicilio", null);
        String alturaDomicilio = contexto.parametros.string("alturaDomicilio", null);
        String dptoDomicilio = contexto.parametros.string("dptoDomicilio", null);
        String pisoDomicilio = contexto.parametros.string("pisoDomicilio", null);
        String codigoAreaCelular = contexto.parametros.string("codigoArea", null);
        String caracteristicaCelular = contexto.parametros.string("caracteristicaCelular", null);
        String numeroCelular = contexto.parametros.string("numeroCelular", null);
        String email = contexto.parametros.string("email", null);

//		Date fechaIngresoPais = contexto.parametros.date("fechaIngresoPais", "d/M/yyyy", contexto.persona().fechaResidencia());

        Boolean esPEP = contexto.parametros.bool("esPersonaExpuestaPoliticamente", contexto.persona().esPersonaExpuestaPoliticamente());
//		Boolean esSO = contexto.parametros.bool("esSujetoObligado", contexto.persona().esSujetoObligado());
//		Integer idSituacionLaboral = contexto.parametros.integer("idSituacionLaboral", 0);
//		Integer idProfesion = contexto.parametros.integer("idProfesion", 0);
//		BigDecimal sueldo = contexto.parametros.bigDecimal("sueldo", "0");

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
//		cuentaInversor.setCondicionIva(persona.string("idSituacionImpositiva", "CONF"));
        cuentaInversor.setCondicionIva("CONF");

        cuentaInversor.setDireccion(domicilio.string("idCore", ""));

        String telefonoUnitrade = telefono.string("idCore", "") + ", " + telefono.string("idDireccion", "");
        String vfnet = "(" + telefono.string("codigoPais", "") + ")" + "(" + codigoAreaCelular + ")" + caracteristicaCelular + "-" + numeroCelular;
        cuentaInversor.setTelefonoUnitrade(telefonoUnitrade);
        cuentaInversor.setTelefonoVFNet(vfnet);
        cuentaInversor.setEmail(email.toLowerCase());
        cuentaInversor.setNombre(fixCaracteresEspeciales(persona.nombres()));
        cuentaInversor.setApellido(fixCaracteresEspeciales(persona.apellidos()));
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
//        cuentaInversor.setIdBanco(contexto.idCobis()); // es idCobis segun doc persona
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

    public static String generarIDUnico() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException();
        }
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");// todo: 17caracteres ver esto
        return ahora.format(formatter).substring(2);
    }

    public static Respuesta testPerfilInversor(ContextoHB contexto) {
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
        pregunta.set("id", 2).set("pregunta", "2. ¿Ha invertido usted alguna vez en los siguientes instrumentos del mercado de capitales?");
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
        pregunta.add("respuestas", new Objeto().set("id", 1).set("No estaría dispuesto a realizar ninguna inversión que implicara arriesgar mi capital."));
        pregunta.add("respuestas", new Objeto().set("id", 2).set("Aceptaría un mínimo riesgo si con ello puedo obtener una mayor rentabilidad."));
        pregunta.add("respuestas", new Objeto().set("id", 3).set("Estaría dispuesto a asumir una pérdida del 10% si espero tener a mediano / largo plazo una mayor rentabilidad."));
        pregunta.add("respuestas", new Objeto().set("id", 4).set("Acepto asumir un alto riesgo para obtener una mayor rentabilidad."));
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

        return Respuesta.exito("preguntas", preguntas);
    }

    public static Respuesta completarTestPerfilInversor(ContextoHB contexto) {
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
            return Respuesta.parametrosIncorrectos();
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

        Respuesta respuesta = new Respuesta();
        ApiResponse responseConsulta = RestPersona.perfilInversor(contexto);
        if (responseConsulta.hayError()) {
            String error = "ERROR";
            return Respuesta.estado(error);
        }
        String operacion = "I";
        for (Objeto item : responseConsulta.objetos()) {
            if (!"".equals(item.string("perfilInversor", ""))) {
                operacion = "U";
            }
        }
        ApiResponse response = RestPersona.actualizaPerfilInversor(contexto, idPerfilInversor, operacion);
        if (response.hayError()) {
            String error = "ERROR";
            return Respuesta.estado(error);
        }
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

    public static Respuesta cuentaInversor(ContextoHB contexto) {
        Boolean poseeCuenta = contexto.cuentaUnipersonalPesos() != null;
        Boolean poseeCuentaComitente = contexto.cuentaComitentePorDefecto() != null;
        Boolean poseeCuentaCuotapartista = false;

        ApiResponse response = ProductosService.productos(contexto);
        if (response.hayError()) {
            return Respuesta.error();
        }

        for (Objeto objeto : response.objetos("productos")) {
            if (objeto.string("tipo").equals("RJA")) {
                poseeCuentaCuotapartista = true;
            }
        }

        Respuesta respuesta = new Respuesta();
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

            int pivot = Integer.parseInt(temp[i]);
            if (i == 0) {
                if (pivot == 0)
                    listaSalida = "9";
                else if (pivot == 1)
                    listaSalida = "6";
                else if (pivot == 2)
                    listaSalida = "3";
                else if (pivot == 3)
                    listaSalida = "0";
            }

            if (i == 1) {
                if (pivot == 0)
                    listaSalida = listaSalida + "-0";
                else if (pivot == 1)
                    listaSalida = listaSalida + "-9";
                else if (pivot == 2)
                    listaSalida = listaSalida + "-6";
                else if (pivot == 3)
                    listaSalida = listaSalida + "-9";
                else if (pivot == 4)
                    listaSalida = listaSalida + "-0";
            }

            if (i == 2) {
                if (pivot == 0)
                    listaSalida = listaSalida + "-6";
                else if (pivot == 1)
                    listaSalida = listaSalida + "-0";
                else if (pivot == 2)
                    listaSalida = listaSalida + "-9";
            }

            if (i == 3) {
                if (pivot == 0)
                    listaSalida = listaSalida + "-9";
                else if (pivot == 1)
                    listaSalida = listaSalida + "-6";
            }

            if (i == 4) {
                if (pivot == 0)
                    listaSalida = listaSalida + "-0";
                else if (pivot == 1)
                    listaSalida = listaSalida + "-6";
                else if (pivot == 2)
                    listaSalida = listaSalida + "-9";
            }

            if (i == 5) {
                if (pivot == 0)
                    listaSalida = listaSalida + "-0";
                else if (pivot == 1)
                    listaSalida = listaSalida + "-3";
                else if (pivot == 2)
                    listaSalida = listaSalida + "-6";
                else if (pivot == 3)
                    listaSalida = listaSalida + "-9";
            }

            if (i == 6) {
                if (pivot == 0)
                    listaSalida = listaSalida + "-0";
                else if (pivot == 1)
                    listaSalida = listaSalida + "-3";
                else if (pivot == 2)
                    listaSalida = listaSalida + "-6";
                else if (pivot == 3)
                    listaSalida = listaSalida + "-9";
            }

            if (i == 7) {
                if (pivot == 0)
                    listaSalida = listaSalida + "-0";
                else if (pivot == 1)
                    listaSalida = listaSalida + "-6";
                else if (pivot == 2)
                    listaSalida = listaSalida + "-9";
            }

            if (i == 8) {
                if (pivot == 0)
                    listaSalida = listaSalida + "-0";
                else if (pivot == 1)
                    listaSalida = listaSalida + "-3";
                else if (pivot == 2)
                    listaSalida = listaSalida + "-6";
                else if (pivot == 3)
                    listaSalida = listaSalida + "-9";
            }

            if (i == 9) {
                if (pivot == 0)
                    listaSalida = listaSalida + "-0";
                else if (pivot == 1)
                    listaSalida = listaSalida + "-3";
                else if (pivot == 2)
                    listaSalida = listaSalida + "-6";
                else if (pivot == 3)
                    listaSalida = listaSalida + "-9";
            }

            if (i == 10) {
                if (pivot == 0)
                    listaSalida = listaSalida + "-0";
                else if (pivot == 1)
                    listaSalida = listaSalida + "-3";
                else if (pivot == 2)
                    listaSalida = listaSalida + "-6";
                else if (pivot == 3)
                    listaSalida = listaSalida + "-9";
            }

            if (i == 11) {
                if (pivot == 0)
                    listaSalida = listaSalida + "-0";
                else if (pivot == 1)
                    listaSalida = listaSalida + "-6";
                else if (pivot == 2)
                    listaSalida = listaSalida + "-9";
            }

            if (i == 12) {
                if (pivot == 0)
                    listaSalida = listaSalida + "-0";
                else if (pivot == 1)
                    listaSalida = listaSalida + "-0";
                else if (pivot == 2)
                    listaSalida = listaSalida + "-3";
                else if (pivot == 3)
                    listaSalida = listaSalida + "-6";
                else if (pivot == 4)
                    listaSalida = listaSalida + "-9";
                else if (pivot == 5)
                    listaSalida = listaSalida + "-9";
            }
        }

        return listaSalida;
    }
    // FIN: COPIA DEL HB VIEJO

    public static Respuesta cuotapartista(ContextoHB contexto) {
        Boolean mostrarCuentasAnuladas = contexto.parametros.bool("mostrarCuentasAnuladas");
        ApiResponse response = RestInversiones.cuotapartista(contexto, null, tipoDocEsco(contexto.persona()), null, mostrarCuentasAnuladas, contexto.persona().esPersonaJuridica() ? contexto.persona().cuit() : contexto.persona().numeroDocumento());
        if (response.hayError() && !"1008".equals(response.string("codigo"))) {
            return Respuesta.error();
        }

//		Respuesta productoCuotapartista = HBProducto.cuentasCuotapartistas(contexto);
//		if (!productoCuotapartista.string("estado").equalsIgnoreCase("0")) {
//			return Respuesta.error();
//		}

        Respuesta respuesta = new Respuesta();
        Objeto itemConTenencias = new Objeto();
        Objeto itemSinTenencias = new Objeto();

        for (Objeto cuotapartista : response.objetos("CuotapartistaModel")) {

//			Boolean encontrado = false;
            if (cuotapartista.bool("EsFisico")) {
                Objeto item = new Objeto();
                item.set("iDCuotapartista", cuotapartista.string("NumeroCuotapartista"));

                item.set("tieneTenencia", false);
                ApiResponse posicionCuotapartista = RestInversiones.posicionCuotapartista(contexto, Fecha.fechaActual(),
                        "", item.integer("iDCuotapartista"));
                if (!posicionCuotapartista.hayError()) {
                    item.set("tieneTenencia", true);
                }

                BigDecimal cuotapartesValuadas = posicionCuotapartista
                        .objetos("PosicionCuotapartista").stream()
                        .map(o -> o.bigDecimal("CuotapartesValuadas"))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                item.set("cuotapartesValuadasTotales", cuotapartesValuadas);

                item.set("estaInhibido", estaInhibido(contexto, item));

//				List<Objeto> productoList = productoCuotapartista.objetos("cuentasCuotapartistas");
//
//				for (Objeto producto : productoList) {
//					if (encontrado) {
//						break;
//					}
//					String productoId = producto.string("id").split("-")[1];
//					if (productoId.equalsIgnoreCase(item.string("iDCuotapartista"))) {
//						encontrado = true;
//					}
//				}
//
//				if (encontrado.equals(Boolean.FALSE)) {
//					continue;
//				}

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

    public static Boolean estaInhibido(ContextoHB contexto, Objeto item) {
        Boolean inhibido = false;
        ApiResponse fondoResponse = RestInversiones.fondos(contexto, item.integer("iDCuotapartista"), 0, null, "RE");
        if (!fondoResponse.hayError()) {
            if (fondoResponse.objetos("Table").get(0).integer("EstaInhibido") == -1) {
                inhibido = true;
            }
        }

        return inhibido;
    }

    public static Boolean tieneTenencia(ContextoHB contexto, Objeto item) {
        Boolean tenencia = false;
        ApiResponse posicionCuotapartista = RestInversiones.posicionCuotapartista(contexto, Fecha.fechaActual(), "", item.integer("iDCuotapartista"));
        if (!posicionCuotapartista.hayError()) {
            tenencia = true;
        }
        return tenencia;
    }

    private static String getTipoPersona(ContextoHB contexto) {
        String clase = "";
        if (contexto.persona().esPersonaFisica()) {
            if (contexto.persona().esEmpleado()) {
                clase = "Empleados";
            } else {
                clase = "Individuos - Particulares";
            }
        } else if (contexto.persona().esPersonaJuridica()) {
            clase = "Personas Jurídicas";
        }
        return clase;
    }

    public static Respuesta posicionCuotapartista(ContextoHB contexto) {
        if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_fondos_fuera_de_horario", "prendido_fondos_fuera_de_horario_cobis")) {
            return posicionCuotapartistaV3(contexto);
        } else {
            String fecha = contexto.parametros.string("fecha");
            String idCuotapartista = contexto.parametros.string("idCuotapartista");
            Integer numeroCuotapartista = contexto.parametros.integer("numeroCuotapartista");
            BigDecimal totalFondosPesos = new BigDecimal(BigInteger.ZERO);
            BigDecimal totalFondosDolares = new BigDecimal(BigInteger.ZERO);

            String tipoPersona = getTipoPersona(contexto);
            List<Objeto> tablaParametria = SqlHomebanking.getFondosParametria("RE", tipoPersona);

            ApiResponse responseCalendario = RestCatalogo.calendarioFechaActual(contexto);
            if (responseCalendario.hayError()) {
                return null;
            }
            List<Objeto> solicitudes;
            String fechaHasta = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String fechaDesde = responseCalendario.objetos().get(0).string("diaHabilAnterior");
            List<Objeto> ordenesAgendadas = SqlHomebanking.getOrdenesAgendadosFCIPorNumCuotaPartista(String.valueOf(numeroCuotapartista));
            ApiResponse responseSolicitudes = RestInversiones.solicitudes(contexto, fechaDesde, fechaHasta, null, null, null, null, null, numeroCuotapartista);

            if ((responseSolicitudes.hayError())) {
                if (!("1".equals(responseSolicitudes.string("codigo")))) {
                    return Respuesta.error();
                }
            }
            solicitudes = responseSolicitudes.objetos("SolicitudGenericoModel").stream().filter(solicitud -> solicitud.string("EstadoSolicitud").equalsIgnoreCase("No Requiere Autorización")).collect(Collectors.toList());
            ApiResponse response = RestInversiones.posicionCuotapartista(contexto, fecha, idCuotapartista, numeroCuotapartista);
            if (response.hayError()) {
                if ("1016".equals(response.string("codigo"))) {
                    return Respuesta.estado("SIN_POSICION");
                }
                return Respuesta.error();
            }
            Set<String> fechasSet = new HashSet<>();
            Respuesta respuesta = new Respuesta();
            respuesta.set("esDiaHabil", Util.isDiaHabil(contexto));
            int posicionCuota = 0;
            for (Objeto objeto : response.objetos("PosicionCuotapartista")) {

                if (!objeto.existe("FondoID")) {
                    continue;
                }

                Objeto posicion = new Objeto();
                posicion.set("rescateTotal", false);
                DoubleSummaryStatistics rescateAgenda = ordenesAgendadas.stream().filter(ordenAgendad -> ordenAgendad.string("tipo_solicitud").equalsIgnoreCase("rescate") && ordenAgendad.string("fondo_id").equalsIgnoreCase(objeto.string("FondoNumero"))).mapToDouble(ordenAgendada -> {
                    if (ordenAgendada.string("importe").equals("0.00")) {
                        posicion.set("rescateTotal", true);
                        return 0;
                    } else {
                        return Double.parseDouble(ordenAgendada.string("importe"));
                    }
                }).summaryStatistics();

                DoubleSummaryStatistics suscripcionAgenda = ordenesAgendadas.stream().filter(ordenAgendad -> ordenAgendad.string("tipo_solicitud").equalsIgnoreCase("Suscripcion") && ordenAgendad.string("fondo_id").equalsIgnoreCase(objeto.string("FondoNumero"))).mapToDouble(ordenAgendada -> Double.parseDouble(ordenAgendada.string("importe"))).summaryStatistics();
                DoubleSummaryStatistics rescateSinProcesar = solicitudes.stream().filter(solicitud -> solicitud.string("TipoSolicitud").equalsIgnoreCase("rEsCate") && solicitud.string("FondoID").equalsIgnoreCase(objeto.string("FondoNumero"))).mapToDouble(ordenAgendada -> {
                    if (ordenAgendada.bool("EsTotal")) {
                        posicion.set("rescateTotal", true);
                        return 0;
                    } else {
                        return Double.parseDouble(ordenAgendada.string("Importe"));
                    }
                }).summaryStatistics();

                String[] parts = objeto.string("FondoNombre").split("-");

                posicion.set("orden", Integer.parseInt(parts[0].replace(" ", "")));
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
                posicion.set("operacionRescateAgenda", rescateAgenda.getSum());
                posicion.set("operacionSuscripcionAgenda", suscripcionAgenda.getSum()); //
                posicion.set("operacionRescateSinProcesar", rescateSinProcesar.getSum()); //
                posicion.set("cuotapartesValuadasFormateado", Formateador.importe(objeto.bigDecimal("CuotapartesValuadas")));
                posicion.set("ultimoVCPValor", objeto.bigDecimal("UltimoVCPValor"));
                posicion.set("ultimoVCPValorFormateado", Formateador.importeCantDecimales(objeto.bigDecimal("UltimoVCPValor"), 6));
                String fechaUltimoVCP;
                if (!ConfigHB.esProduccion().booleanValue()) {
                    fechaUltimoVCP = Mock.aumentarFecha(objeto.date("UltimoVCPFecha", "yyyy-MM-dd", "dd/MM/yyyy"), posicionCuota);
                    posicionCuota++;
                } else {
                    fechaUltimoVCP = objeto.date("UltimoVCPFecha", "yyyy-MM-dd", "dd/MM/yyyy");
                }
                posicion.set("ultimoVCPFecha", fechaUltimoVCP);
                if (posicion.get("ultimoVCPFecha") != null) {
                    fechasSet.add(posicion.get("ultimoVCPFecha").toString());
                }
                posicion.set("iDMoneda", objeto.string("IDMoneda"));
                posicion.set("monedaSimbolo", objeto.string("MonedaSimbolo"));
                posicion.set("monedaDescripcion", objeto.string("MonedaDescripcion"));
                for (Objeto fondos : tablaParametria) {
                    if (Integer.parseInt(objeto.string("FondoID")) == fondos.integer("id_fondo") && fondos.string("cond_ingreso_egreso").equals(objeto.string("IDCondicionIngEgr"))) {
                        posicion.set("montoMinimo", fondos.bigDecimal("min_a_operar"));
                        posicion.set("montoMinimoFormateado", Formateador.importe(fondos.bigDecimal("min_a_operar")));
                        if (!fondos.string("hora_inicio").isEmpty() && !fondos.string("hora_fin").isEmpty()) {
                            posicion.set("estaFueraDeHorario", comprobacionFueraDeHorario(fondos.string("hora_inicio"), fondos.string("hora_fin")));
                            posicion.set("hora_inicio", fondos.string("hora_inicio"));
                            posicion.set("hora_fin", fondos.string("hora_fin"));
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

            List<Objeto> posicionesCuotapartista = respuesta.objetos("posicionCuotapartista");

            posicionesCuotapartista.sort((o1, o2) -> {
                return o1.integer("orden").compareTo(o2.integer("orden"));
            });

            respuesta.set("posicionCuotapartista", posicionesCuotapartista);

            respuesta.set("fechaServiceLayer", Collections.max(fechasSet));
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
            case "13" -> "Horizonte: 3 años en adelante.";
            case "14" -> "Horizonte: de 1 a 3 años.";
            case "15" -> "Horizonte: 3 años en adelante.";
            case "16" -> "Horizonte: 3 años en adelante.";
            case "18" -> "Horizonte: de 1 a 3 años.";
            case "19" -> "Horizonte: de 0 a 1 año.";
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

    public static void agregarDetalleFondo(
            Objeto posicion,
            Respuesta respPerfil,
            List<Objeto> fondoList,
            String idFondo,      // para matchear 'FondoID'
            String numFondo) {   // para los switch

        String claveSwitch = (numFondo != null && !numFondo.isBlank()) ? numFondo : idFondo;
        posicion.set("horizonteFondo",   getHorizonteFondo(claveSwitch));
        posicion.set("descripcionFondo", getDescripcionFondo(claveSwitch));
        posicion.set("composicionFondo", getUrlDetalleFondo(claveSwitch));

        if (idFondo == null || idFondo.isBlank() || fondoList == null || fondoList.isEmpty()) return;

        Objeto fondoSrv = fondoList.stream()
                .filter(f -> Objects.equals(String.valueOf(getOrNullInt(f, "FondoID")), idFondo))
                .findFirst()
                .orElse(null);

        if (fondoSrv != null) {
            posicion.set("plazoLiquidacionFondo", getOrNullInt(fondoSrv, "PlazoLiquidacionFondo"));

            String desc = getOrEmpty(fondoSrv, "TpRiesgoDescripcion");
            String riesgo = switch (desc) {
                case "Perfil Conservador" -> "Bajo/Conservador";
                case "Perfil Moderado"    -> "Medio/Moderado";
                case "Perfil Arriesgado", "Perfl Arriesgado" -> "Alto/Arriesgado"; // contempla typo
                default -> "";
            };
            posicion.set("tpRiesgoDescripcion", riesgo);
            posicion.set("numeroCuentaFondo", getOrNullInt(fondoSrv, "NumeroCuentaFondo"));
            posicion.set("fondoNombreAbr", getOrEmpty(fondoSrv, "FondoNombreAbr"));

            // Código de riesgo tolerante: intenta varias claves y evita parseo directo
            int codRiesgo = parseIntOrZero(getOrEmpty(fondoSrv, "TpRiesgoCodInterfaz"));
            if (codRiesgo == 0) {
                int alt = parseIntOrZero(getOrEmpty(fondoSrv, "TpRiesgoCodigo"));
                if (alt == 0) alt = parseIntOrZero(getOrEmpty(fondoSrv, "tpRiesgoCodigo"));
                if (alt != 0) codRiesgo = alt;
            }
            if (codRiesgo == 0) {
                String d = getOrEmpty(fondoSrv, "TpRiesgoDescripcion").toLowerCase();
                if (d.contains("bajo"))       codRiesgo = 1;
                else if (d.contains("medio")) codRiesgo = 2;
                else if (d.contains("alto"))  codRiesgo = 3;
            }
            if (codRiesgo < 1 || codRiesgo > 3) codRiesgo = 2; // default válido

            posicion.set("codigoMensajePerfilInversor", validacionOperacionConPerfil(respPerfil, codRiesgo));
            posicion.set("tpRiesgoCodigo", String.valueOf(codRiesgo));

        }
    }





    public static Respuesta posicionCuotapartistaV3(ContextoHB contexto) {
        String fecha = contexto.parametros.string("fecha");

        Integer numeroCuotapartista = contexto.parametros.integer("numeroCuotapartista");
        // TODO GB BUG15052024-3 - Ver porque lo mandan vacio.
        String idCuotapartista = (contexto.parametros.string("idCuotapartista") == null
                || contexto.parametros.string("idCuotapartista").isBlank()) && numeroCuotapartista != null
                ? numeroCuotapartista.toString()
                : contexto.parametros.string("idCuotapartista");


        boolean FFvariacionRendimiento = HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "FFvariacionRendimiento", "FFvariacionRendimiento_cobis");

        Map<String, Objeto> mapaVariaciones = Collections.emptyMap();
        if (FFvariacionRendimiento) {
            try {
                ApiResponse respVar = RestInversiones.variacionFondos(contexto, fecha);
                if (!respVar.hayError()) {
                    List<Objeto> listaVar = respVar.objetos();
                    if (listaVar != null && !listaVar.isEmpty()) {
                        mapaVariaciones = listaVar.stream().collect(Collectors.toMap(
                                v -> v.string("numFondo") + "|" + v.string("abreviatura"),
                                v -> v
                        ));
                    }
                }
            } catch (Exception ignore) {
            }
        }

        BigDecimal totalPesos = new BigDecimal(BigInteger.ZERO);
        BigDecimal totalDolares = new BigDecimal(BigInteger.ZERO);
        BigDecimal totalRendimientoPesos = BigDecimal.ZERO;
        BigDecimal totalRendimientoDolares = BigDecimal.ZERO;
        boolean hayRendimientoPesos = false;
        boolean hayRendimientoDolares = false;

        int cantidadFciPesos = 0;
        int cantidadFciDolares = 0;

        String tipoPersona = getTipoPersona(contexto);

        List<Objeto> tablaParametria = SqlHomebanking.getFondosParametria("RE", tipoPersona);

        ApiResponse responseCalendario = RestCatalogo.calendarioFechaActual(contexto);
        if (responseCalendario.hayError()) {
            return null;
        }
        List<Objeto> solicitudesDesdeUltimoDiaHabil;
        String fechaHasta = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        String fechaDesdeUltimoDiaHabil = responseCalendario.objetos().get(0).string("diaHabilAnterior");
        List<Objeto> ordenesAgendadas = SqlHomebanking
                .getOrdenesAgendadosFCIPorNumCuotaPartista(String.valueOf(numeroCuotapartista));
        ApiResponse responseSolicitudes = RestInversiones.solicitudes(contexto, fechaDesdeUltimoDiaHabil, fechaHasta,
                null, null, null, null, null, numeroCuotapartista);

        Respuesta respuesta = new Respuesta();

        if (responseSolicitudes.codigo == 504) {
            respuesta.set("codigo", responseSolicitudes.codigo);
        }

        if ((responseSolicitudes.hayError()) && responseSolicitudes.codigo != 504) {
            if (!("1".equals(responseSolicitudes.string("codigo")))) {
                return Respuesta.error();
            }
        }

        solicitudesDesdeUltimoDiaHabil = responseSolicitudes.objetos("SolicitudGenericoModel").stream()
                .filter(solicitud -> solicitud.string("EstadoSolicitud").equalsIgnoreCase("No Requiere Autorización")
                        || solicitud.string("EstadoSolicitud").equalsIgnoreCase("Autorizado")
                        || solicitud.string("EstadoSolicitud").equalsIgnoreCase("Pendiente de Autorización"))
                .collect(Collectors.toList());
        ApiResponse responsePosicionCuotapartista = RestInversiones.posicionCuotapartista(contexto, fecha,
                idCuotapartista, numeroCuotapartista);
        // Ignoramos 1016 - SIN POSICION y evaluamos op. del dia.
        if (responsePosicionCuotapartista.hayError()
                && !"1016".equals(responsePosicionCuotapartista.string("codigo"))) {
            return Respuesta.error();
        }

        Set<String> fechasSet = new HashSet<>();

        respuesta.set("esDiaHabil", Util.isDiaHabil(contexto));
        int posicionCuota = 0;

        List<Objeto> consolidadoMovsUltimoDHabil = obtenerMovimientosFondosPorFecha(contexto, fecha,
                fechaDesdeUltimoDiaHabil);
        List<Objeto> posiciones = new ArrayList<>();

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
            if (!ConfigHB.esProduccion().booleanValue()) {
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
            String numFondoNumero = itPosicion.string("FondoNumero");
            posicion.set("descripcionFondo", getDescripcionFondo(numFondoNumero));
            posicion.set("horizonteFondo",   getHorizonteFondo(numFondoNumero));
            posicion.set("composicionFondo", getUrlDetalleFondo(numFondoNumero));

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
                        posicion.set("esDiaHabil", Util.isDiaHabil(contexto));
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
                    suscripcionesNuevas.add(consolidadaMov);
                }
            }

        });

        Map<String, Objeto> finalMapaVariaciones = mapaVariaciones;
        suscripcionesNuevas.forEach(x -> {
            String numFondo = x.string("fondoNumero");
            x.set("descripcionFondo", getDescripcionFondo(numFondo));
            x.set("horizonteFondo",   getHorizonteFondo(numFondo));
            x.set("composicionFondo", getUrlDetalleFondo(numFondo));

            if (FFvariacionRendimiento) {
                String abrev = inferirAbreviatura(numFondo, x.string("tipoVCPAbreviatura"), posiciones, finalMapaVariaciones);
                if (abrev != null && !abrev.isBlank()) x.set("tipoVCPAbreviatura", abrev);
                setTnaSiMoneyMarket(x, numFondo, abrev, finalMapaVariaciones); // SOLO TNA para 9/20
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
            EnumMonedaInversion moneda = EnumMonedaInversion.id(posicion.integer("iDMoneda"));
            if (moneda.esPeso()) {
                cantidadFciPesos++;
                totalPesos = totalPesos.add(posicion.bigDecimal("totalCalculado"));
            } else if (moneda.esDolar()) {
                cantidadFciDolares++;
                totalDolares = totalDolares.add(posicion.bigDecimal("totalCalculado"));
            }
        }

        for (Objeto posicion : posiciones) {
            EnumMonedaInversion moneda = EnumMonedaInversion.id(posicion.integer("iDMoneda"));
            // Valores nulos en agenda.
            posicion.set("porcentaje", porcentaje(posicion.bigDecimal("totalCalculado"),
                    moneda.esPeso() ? totalPesos : totalDolares, 2, Optional.of(RoundingMode.HALF_UP)));
        }

        List<Objeto> posicionesOrdenadas = Optional
                .ofNullable(respuesta.objetos("posicionCuotapartista"))
                .orElse(Collections.emptyList())
                .stream()
                .sorted(Comparator.comparingInt((Objeto p) -> {
                            int n = Integer.parseInt(p.string("fondoNumero"));
                            return (n == 9 || n == 20) ? 0 : 1;})
                        .thenComparing((Objeto p) -> {
                            BigDecimal tc = p.bigDecimal("totalCalculado");return tc != null ? tc : BigDecimal.ZERO;}, Comparator.reverseOrder()
                        ).thenComparing(
                                (Objeto p) -> nombreFondoLimpio(p.string("fondoNombre")),
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
                )
                .collect(Collectors.toList());

        respuesta.setNull("posicionCuotapartista");
        for (Objeto posicion : posicionesOrdenadas) {
            respuesta.add("posicionCuotapartista", posicion);
        }

        if (posicionesOrdenadas.isEmpty()) {
            Respuesta r = Respuesta.estado("SIN_POSICION");
            r.set("nuevoFlujoFCI", FFvariacionRendimiento);
            return r;
        }

        BigDecimal porcentajeTotalPesos = BigDecimal.ZERO;
        BigDecimal porcentajeTotalDolares = BigDecimal.ZERO;

        if (totalPesos.compareTo(BigDecimal.ZERO) > 0) {
            porcentajeTotalPesos = totalRendimientoPesos
                    .divide(totalPesos, 6, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }

        if (totalDolares.compareTo(BigDecimal.ZERO) > 0) {
            porcentajeTotalDolares = totalRendimientoDolares
                    .divide(totalDolares, 6, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }

        respuesta.set("cantidadFciPesos", cantidadFciPesos);
        respuesta.set("cantidadFciDolares", cantidadFciDolares);

        if (fechasSet.size() > 0) {
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

    public static Boolean onboardingFci(ContextoHB contexto) {
        try {
            contexto.parametros.set("nemonico", "ONBOARDING_FCI");
            new Futuro<>(() -> Util.contador(contexto)); // inserta y no espera
            return true; // ya que lo único que te interesa es disparar el insert
        } catch (Exception ex) {
            return false;
        }
    }

    private static String inferirAbreviatura(String numFondo, String abrevEnMovimiento, List<Objeto> posiciones, Map<String, Objeto> mapaVariaciones) {
        if (abrevEnMovimiento != null && !abrevEnMovimiento.isBlank()) return abrevEnMovimiento;
        for (Objeto p : posiciones) {
            if (numFondo.equals(p.string("fondoNumero"))) {
                String a = p.string("tipoVCPAbreviatura");
                if (a != null && !a.isBlank()) return a;
            }
        }

        final String prefijo = numFondo + "|";
        for (String k : mapaVariaciones.keySet()) {
            if (k.startsWith(prefijo)) {
                String a = k.substring(prefijo.length());
                if (a != null && !a.isBlank()) return a;
            }
        }
        return null;
    }

    private static void setTnaSiMoneyMarket(Objeto target, String numFondo, String abreviatura, Map<String, Objeto> mapaVariaciones) {
        if (abreviatura == null || abreviatura.isBlank()) return;

        int n;
        try { n = Integer.parseInt(numFondo); } catch (NumberFormatException e) { n = -1; }
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

    public static List<Objeto> obtenerMovimientosFondosPorFecha(ContextoHB contexto, String fecha, String fechaDesde) {

        ApiResponse cuotapartistaResponse = RestInversiones.cuotapartista(contexto, null,
                tipoDocEsco(contexto.persona()), null, false,
                contexto.persona().esPersonaJuridica() ? contexto.persona().cuit()
                        : contexto.persona().numeroDocumento());

        List<Objeto> movimientos = new ArrayList<>();
        List<Objeto> consolidado = new ArrayList<>();
        List<Objeto> fondosOrdenesAgendadas = SqlHomebanking.getOrdenesAgendadasFCIPorEstadoAndCobis("Agendada",
                contexto.idCobis());

        fondosOrdenesAgendadas.stream().forEach(i -> {
            Objeto o = new Objeto();
            o.set("fondoNumero", i.string("fondo_id"));
            o.set("cuotapartistaID", i.string("cuotapartista"));
            o.set("tipoSolicitud", i.string("tipo_solicitud"));
            o.set("totalCalculado", i.string("tipo_solicitud").equalsIgnoreCase("Suscripcion") ? i.bigDecimal("importe")
                    : i.bigDecimal("importe").negate());
            movimientos.add(o);
        });

        try {
            for (Objeto cuotapartista : cuotapartistaResponse.objetos("CuotapartistaModel")) {

                ApiResponse responseFondosCuotapartista = RestInversiones.fondos(contexto,
                        cuotapartista.integer("NumeroCuotapartista"), 0, null, "SU");
                List<Objeto> tablaParametria = SqlHomebanking.getFondosParametria("SU", getTipoPersona(contexto));
                List<Objeto> fondos = new ArrayList<>();
                // Información del fondo
                for (Objeto detallesFondo : responseFondosCuotapartista.objetos("Table")) {
                    if (detallesFondo.string("TipoVCPDescripcion")
                            .equals(tablaParametria.get(0).string("tipo_vcp_descripcion"))) {
                        Objeto fondo = new Objeto();
                        for (Objeto fondoParametria : tablaParametria) {
                            if (detallesFondo.integer("FondoID").equals(fondoParametria.integer("id_fondo"))
                                    && detallesFondo.string("CondicionIngresoEgresoID")
                                    .equals(fondoParametria.string("cond_ingreso_egreso"))) {
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

                ApiResponse responseSolicitudes = RestInversiones.solicitudes(contexto, fechaDesde, fecha, null, null,
                        null, null, null, cuotapartista.integer("NumeroCuotapartista"));
                List<Objeto> solicitudes = responseSolicitudes.objetos("SolicitudGenericoModel").stream().filter(
                                solicitud -> solicitud.string("EstadoSolicitud").equalsIgnoreCase("No Requiere Autorización")
                                        || solicitud.string("EstadoSolicitud").equalsIgnoreCase("Autorizado")
                                        || solicitud.string("EstadoSolicitud").equalsIgnoreCase("Pendiente de Autorización"))
                        .collect(Collectors.toList());

                solicitudes.forEach(solicitud -> {
                    Objeto movimiento = new Objeto();
                    BigDecimal importe = solicitud.bigDecimal("Importe") != null ? solicitud.bigDecimal("Importe")
                            : BigDecimal.ZERO;
                    movimiento.set("fondoNumero", solicitud.string("FondoID"));
                    movimiento.set("cuotapartistaID", solicitud.string("CuotapartistaID"));
                    movimiento.set("tipoSolicitud", solicitud.string("TipoSolicitud"));
                    movimiento.set("totalCalculado",
                            solicitud.string("TipoSolicitud").equalsIgnoreCase("Suscripcion") ? importe
                                    : importe.negate());
                    movimientos.add(movimiento);
                });

                consolidado = movimientos.stream()
                        .collect(Collectors.groupingBy(su -> su.integer("fondoNumero") + "_"
                                + su.string("cuotapartistaID") + "_" + su.string("tipoSolicitud")))
                        .entrySet().stream().map(e -> e.getValue().stream().reduce((s1, s2) -> {
                            Objeto x = new Objeto();
                            x.set("fondoNumero", s1.string("fondoNumero"));
                            x.set("cuotapartistaID", s1.string("cuotapartistaID"));
                            x.set("tipoSolicitud", s1.string("tipoSolicitud"));
                            x.set("totalCalculado",
                                    s1.bigDecimal("totalCalculado").add(s2.bigDecimal("totalCalculado")));
                            return x;
                        })).map(i -> i.get()).toList();

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

    public static Respuesta solicitudesFci(ContextoHB contexto) {
        String fechaDesde = contexto.parametros.string("fechaDesde");
        String fechaHasta = contexto.parametros.string("fechaHasta");
        Integer numeroCuotapartista = contexto.parametros.integer("numeroCuotapartista");

        ApiResponse response = RestInversiones.solicitudes(contexto, fechaDesde, fechaHasta, null, null, null, null, null, numeroCuotapartista);

        if (response.hayError()) {
            if ("1".equals(response.string("codigo"))) {
                return Respuesta.estado("SIN_SOLICITUDES");
            }
            return Respuesta.error();
        }
        Respuesta respuesta = new Respuesta();
        List<Objeto> solicitudes = new ArrayList<>();

        for (Objeto item : response.objetos("SolicitudGenericoModel")) {
            Objeto solicitud = new Objeto();
            solicitud.set("cuotapartistaNombre", item.string("CuotapartistaNombre"));
            solicitud.set("cuotapartistaNumero", item.integer("CuotapartistaNumero"));
            solicitud.set("esTotal", item.bool("EsTotal"));
            solicitud.set("estadoSolicitud", item.string("EstadoSolicitud"));
            solicitud.set("fechaConcertacion", item.date("FechaConcertacion", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yy"));
            solicitud.set("fondoID", item.string("FondoID"));
            solicitud.set("id_solicitud", item.string("IDSolicitud"));
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

    public static Respuesta fondos(ContextoHB contexto) {

        Futuro<String> tipoPersona = new Futuro<>(() -> getTipoPersona(contexto));
        String fecha = contexto.parametros.string("fecha");
        boolean fueraDeHorario = false;
        boolean diaHabil = true;
        int idcuotapartista = contexto.parametros.integer("idcuotapartista");
        int idtipoValorCuotaParte = contexto.parametros.integer("idtipoValorCuotaParte");
        Integer numeroDeFondo = contexto.parametros.integer("numeroDeFondo");
        String tipoSolicitud = contexto.parametros.string("tipoSolicitud");
        contexto.sesion.setChallengeOtp(false);

        boolean FFvariacionRendimiento = HBAplicacion.funcionalidadPrendida(
                contexto.idCobis(), "FFvariacionRendimiento", "FFvariacionRendimiento_cobis");

        Map<String, Objeto> mapaVar = Collections.emptyMap();
        if (FFvariacionRendimiento) {
            try {
                ApiResponse respVar = RestInversiones.variacionFondos(contexto, fecha);
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

        Futuro<ApiResponse> responseFuturo = new Futuro<>(() ->
                RestInversiones.fondos(contexto, idcuotapartista, idtipoValorCuotaParte, numeroDeFondo, tipoSolicitud));
        Futuro<Objeto> horaMaximaMinimaFuturo = new Futuro<>(() ->
                SqlHomebanking.getHorarioMaximoMinimo(tipoSolicitud));

        ApiResponse response = responseFuturo.get();
        Objeto horaMaximaMinima = horaMaximaMinimaFuturo.get();

        if (response.hayError()) {
            return Respuesta.error();
        }

        List<Objeto> tabla = response.objetos("Table");
        if (tabla == null || tabla.isEmpty()) {
            return Respuesta.estado("SIN_FONDOS");
        }

        Integer inhPrimero = getOrNullInt(tabla.get(0), "EstaInhibido");
        if (inhPrimero != null && inhPrimero == -1) {
            return Respuesta.estado("CUENTA_INHIBIDA");
        }

        String horaIni = getOrEmpty(horaMaximaMinima, "hora_inicio");
        String horaFin = getOrEmpty(horaMaximaMinima, "hora_fin");
        if (comprobacionFueraDeHorario(horaIni, horaFin)) {
            fueraDeHorario = true;
        }

        if (!Util.isDiaHabil(contexto)) {
            fueraDeHorario = true;
            diaHabil = false;
        }

        List<Objeto> tablaParametria = SqlHomebanking.getFondosParametria(tipoSolicitud, tipoPersona.get());
        if (tablaParametria == null || tablaParametria.isEmpty()) {
            return Respuesta.estado("SIN_PARAMETRIA");
        }

        List<Objeto> fondos_fci = new ArrayList<>();
        Respuesta respuesta;

        if (fueraDeHorario) {
            respuesta = Respuesta.estado("FUERA_DE_HORARIO");
            respuesta.set("horaInicio", horaIni);
            respuesta.set("horaFin", horaFin);
        } else {
            respuesta = new Respuesta();
        }

        if (!diaHabil) {
            String diaNoHabil = " de lunes a viernes, siendo d&iacute;as h&aacute;biles";
            respuesta.set("diaHabil", diaNoHabil);
        }
        respuesta.set("esDiaHabil", diaHabil);

        Respuesta responsePerfilInversor = perfilInversor(contexto);

        String tipoVcpTarget = getOrEmpty(tablaParametria.get(0), "tipo_vcp_descripcion");

        for (Objeto item : tabla) {

            if (item == null || !item.existe("FondoID")) continue;

            if (strEq(getOrEmpty(item, "TipoVCPDescripcion"), tipoVcpTarget)) {

                Objeto fondo = new Objeto();

                for (Objeto fondos : tablaParametria) {
                    if (fondos == null) continue;

                    Integer itemFondoId = getOrNullInt(item, "FondoID");
                    Integer paramFondoId = getOrNullInt(fondos, "id_fondo");
                    String  itemCond = getOrEmpty(item, "CondicionIngresoEgresoID");
                    String  paramCond = getOrEmpty(fondos, "cond_ingreso_egreso");

                    if (!Objects.equals(itemFondoId, paramFondoId) || !strEq(itemCond, paramCond)) continue;

                    fondo.set("fondoNumero", getOrNullInt(item, "FondoNumero"));
                    fondo.set("fondoNombre", nombreFondoLimpio(getOrEmpty(item, "FondoNombre")));
                    fondo.set("fondoNombreAbr", getOrEmpty(item, "FondoNombreAbr"));
                    fondo.set("monedaID", getOrNullInt(item, "MonedaID"));
                    fondo.set("tipoVCPAbreviatura", getOrEmpty(item, "TipoVCPAbreviatura"));
                    fondo.set("tipoVCPID", getOrEmpty(item, "TipoVCPID"));
                    fondo.set("condicionIngresoEgresoID", getOrEmpty(item, "CondicionIngresoEgresoID"));
                    fondo.set("monedaDescripcion", getOrEmpty(item, "MonedaDescripcion"));
                    fondo.set("monedaSimbolo", getOrEmpty(item, "MonedaSimbolo"));
                    fondo.set("numeroCuentaFondo", getOrNullInt(item, "NumeroCuentaFondo"));
                    fondo.set("plazoLiquidacionFondo", getOrNullInt(item, "PlazoLiquidacionFondo"));
                    fondo.set("tpRiesgoDescripcion", getOrEmpty(item, "TpRiesgoDescripcion"));

                    // --- parseo tolerante: evita NumberFormatException cuando viene "" o no viene
                    int riesgo = parseIntOrZero(getOrEmpty(item, "TpRiesgoCodInterfaz"));
                    if (riesgo == 0) {
                        int alt = parseIntOrZero(getOrEmpty(item, "TpRiesgoCodigo"));
                        if (alt == 0) alt = parseIntOrZero(getOrEmpty(item, "tpRiesgoCodigo"));
                        if (alt != 0) riesgo = alt;
                    }

                    if (riesgo == 0) {
                        String desc = getOrEmpty(item, "TpRiesgoDescripcion").toLowerCase();
                        if (desc.contains("bajo"))       riesgo = 1;
                        else if (desc.contains("medio")) riesgo = 2;
                        else if (desc.contains("alto"))  riesgo = 3;
                    }
                    if (riesgo < 1 || riesgo > 3) riesgo = 2;

                    fondo.set("codigoMensajePerfilInversor",
                            validacionOperacionConPerfil(responsePerfilInversor, riesgo));


                    BigDecimal min = getOrZeroBD(fondos, "min_a_operar");
                    fondo.set("montoMinimo", min);
                    fondo.set("montoMinimoFormateado", Formateador.importe(min));

                    Integer inh = getOrNullInt(item, "EstaInhibido");
                    fondo.set("estaInhibido", inh != null && inh != 0);

                    String hiF = getOrEmpty(fondos, "hora_inicio");
                    String hfF = getOrEmpty(fondos, "hora_fin");
                    fondo.set("estaFueraDeHorario", comprobacionFueraDeHorario(hiF, hfF));
                    fondo.set("hora_inicio", hiF);
                    fondo.set("hora_fin", hfF);

                    agregarDetalleFondo(
                            fondo,
                            responsePerfilInversor,
                            tabla,
                            String.valueOf(itemFondoId),                      // FondoID como string
                            String.valueOf(getOrNullInt(item, "FondoNumero")) // número para switches
                    );

                    if (FFvariacionRendimiento) {
                        String claveVar = getOrEmpty(fondo, "fondoNumero") + "|" + getOrEmpty(fondo, "tipoVCPAbreviatura");
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
                                    return (v == 9 || v == 20) ? 0 : 1;
                                })
                                .thenComparingInt(f -> {
                                    Integer n = f.integer("fondoNumero");
                                    return (n != null && (n == 9 || n == 20)) ? n : Integer.MAX_VALUE;
                                })
                                .thenComparing(
                                        f -> nombreFondoLimpio(getOrEmpty(f, "fondoNombre")),
                                        Comparator.nullsLast(Comparator.naturalOrder())
                                )
                )
                .collect(Collectors.toList());

        respuesta.set("fondos", fondosOrdenados);
        respuesta.set("nuevoFlujoFCI", FFvariacionRendimiento);
        return respuesta;
    }


    public static Respuesta obtenerSolicitudes(ContextoHB contexto){
        Respuesta respuesta = new Respuesta();
        ArrayList<Objeto> notificacionesAL = new ArrayList<>();
        try {

            ApiResponse responseCalendario = RestCatalogo.calendarioFechaActual(contexto);
            if (responseCalendario.hayError()) {
                return Respuesta.error();
            }
            String fechaHasta = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String fechaDesde = responseCalendario.objetos().get(0).string("diaHabilAnterior");

            Respuesta cuotapartista = cuotapartista(contexto);

            cuotapartista.objetos("cuotapartistas").forEach(c -> {
                List<Objeto> solis = solicitudes(contexto, c.integer("iDCuotapartista"), fechaDesde,fechaHasta );
                List<Objeto> notificaciones = Util.getOrUpdateNotificacionSolicitudes(contexto, solis, c.string("iDCuotapartista"));

                notificaciones.stream().filter(x -> x.integer("borrado").equals(0)).forEach(n -> {
                    Objeto notif = new Objeto();
                    notif.set("type", "INV");
                    notif.set("read", n.integer("leido").equals(1));
                    notif.set("numeroCaso", n.get("solicitud"));
                    notif.set("title", ConfigHB.string("hb_inv_title"));
                    notif.set("parte1", String.format(ConfigHB.string("hb_inv_parte1"), n.string("estado"), n.bigDecimal("importe"), n.bool("estadoSolicitud") ? "agendada" : "ingresada"));
                    notif.set("estado", "SUSCRIPCION");
                    notificacionesAL.add(notif);
                });
            });
            respuesta.set("notificacionesAL", notificacionesAL);
        } catch (Exception e) {
            return Respuesta.error();
        }

        return respuesta;
    }

    public static List<Objeto> solicitudes (ContextoHB contexto, Integer numeroCuotapartista, String fechaDesde, String fechaHasta){
        List<Objeto> solicitudes = new ArrayList<>();
        try{
            ApiResponse response = RestInversiones.solicitudes(contexto, fechaDesde, fechaHasta, null, null, null, null, null, numeroCuotapartista);
            List<Objeto> ordenesAgendadas = SqlHomebanking.getOrdenesAgendadosFCIPorNumCuotaPartista(String.valueOf(numeroCuotapartista));

            solicitudes.addAll(response.objetos("SolicitudGenericoModel").stream().map(s -> {
                Objeto o = new Objeto();
                o.set("llave", s.string("NumSolicitud")+"_"+s.string("CuotapartistaID")+"_"+s.string("FondoID"));
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
                o.set("llave", s.string("id")+"_"+s.string("cuotapartista")+"_"+s.string("fondo_id"));
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

        }catch (Exception e){
        }
        return solicitudes;
    }

    /**
     * Orden por el nombre del fondo donde se indica el orden.
     * e.g. "10 - TORONTO TRUST LIQUIDEZ DOLAR" (orden=10)
     * @param fondos fondos
     *
     * TODO GB - M434 - API Inversiones - Refactor - Campo orden.
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

    public static Respuesta fondosAceptados(ContextoHB contexto) {
        Integer fondo = contexto.parametros.integer("fondo");
        List<Objeto> fondosAceptados = SqlHomebanking.getFondosAceptados(contexto.idCobis(), fondo);
        Respuesta respuesta = new Respuesta();

        if (fondosAceptados.isEmpty()) {
            respuesta.set("primeraVez", true);
        } else {
            respuesta.set("primeraVez", false);
        }

        return respuesta;
    }

    private static Integer validacionOperacionConPerfil(Respuesta responsePerfilInversor, Integer idPerfilFondo) {
        Integer nivel = C_RES_VAL_INVERSION_PERFIL_ADECUADO;

        String idPerfil = responsePerfilInversor.string("idPerfil");
        Integer idPerfilInversor = ( idPerfil == null || idPerfil.equals("") ) ? null
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

    public static Respuesta rescate(ContextoHB contexto) {
        Api.eliminarCache(contexto, "PosicionCuotapartista", contexto.idCobis(), contexto.parametros.string("cuotapartista"), Fecha.fechaActual());

        if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_fondos_fuera_de_horario", "prendido_fondos_fuera_de_horario_cobis")) {
            if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_fondos_fuera_de_horario_v2", "prendido_fondos_fuera_de_horario_v2_cobis")) {
                return rescateV3(contexto);
            } else {
                return rescateV2(contexto);
            }
        } else {
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
            inversionFondo.set("CondicionIngresoEgreso", contexto.parametros.objeto("inversionFondo").string("condicionIngresoEgreso"));
            inversionFondo.set("TipoValorCuotaParte", contexto.parametros.objeto("inversionFondo").string("tipoValorCuotaParte"));
            String IDSolicitud = UUID.randomUUID().toString();
            BigDecimal valor = new BigDecimal(importe);

            String tipoPersona = getTipoPersona(contexto);
            List<Objeto> tablaParametria = SqlHomebanking.getFondosParametria("RE", tipoPersona);
            for (Objeto fondos : tablaParametria) {
                if (Integer.parseInt(inversionFondo.string("Fondo")) == fondos.integer("id_fondo") && inversionFondo.string("CondicionIngresoEgreso").equals(fondos.string("cond_ingreso_egreso"))) {
                    if (fondos.bigDecimal("min_a_operar").compareTo(valor) == 1 && esTotal.equals("0")) {
                        return Respuesta.estado("ERROR_MONTO_MINIMO");
                    }
                }
            }

            Objeto rowFileTablaParametria = tablaParametria.stream().filter(tp -> Integer.parseInt(inversionFondo.string("Fondo")) == tp.integer("id_fondo") && inversionFondo.string("CondicionIngresoEgreso").equals(tp.string("cond_ingreso_egreso"))).toList().get(0);

            /* Parametros fuera de horario */
            String horarioInicio = rowFileTablaParametria.string("hora_inicio");
            String horaInicio = horarioInicio.split(":")[0];
            String minutoInicio = horarioInicio.split(":")[1];
            Boolean esDiaHabil = Util.isDiaHabil(contexto);
            Boolean esAntesDeApertura = LocalTime.now().isBefore(LocalTime.parse(horarioInicio));

            String tipoCuentaBancaria = "";
            ApiResponse responseRescate = null;
            ApiResponse responseAplicaCredito;
            Respuesta responseCuotapartista = cuotapartista(contexto);
            ApiResponse responseFondos = RestInversiones.fondos(contexto, Integer.parseInt(cuotapartista), 0, Integer.parseInt(inversionFondo.string("Fondo")), "RE");

            List<Objeto> cuentasBancarias = null;

            for (Objeto cpItem : responseCuotapartista.objetos("cuotapartistas")) {
                if (cuotapartista.equals(cpItem.string("iDCuotapartista"))) {
                    cuentasBancarias = cpItem.objetos("ctasBancarias");
                    break;
                }
            }

            if (cuentasBancarias == null || cuentasBancarias.isEmpty()) {
                return Respuesta.estado("SIN_CUOTAPARTISTA_CUENTA");
            }

            if (responseFondos.objetos("Table").get(0).integer("PlazoLiquidacionFondo") == 0 && esTotal.equals("0") && !importe.equals("0")) {
                /* Ruta Uno */
                for (Objeto cuentas : cuentasBancarias) {
                    if (cuentas.string("idCuentaBancaria").equals(ctaBancaria)) {
                        tipoCuentaBancaria = cuentas.string("descripcionCorta", "");
                        Cuenta cuenta = contexto.cuenta(cuentas.string("numeroCuenta"));
                        responseAplicaCredito = CuentasService.aplicaCreditoDebito(contexto, "creditos", cuenta, valor, "1301", fechaConcertacion);
                        if (responseAplicaCredito.hayError()) {
                            Respuesta respuesta = Respuesta.estado("ERROR_FUNCIONAL");
                            respuesta.set("mensaje", responseAplicaCredito.string("mensajeAlUsuario"));
                            return respuesta;
                        }

                        if (esFueraHorarioTablaParametria(rowFileTablaParametria) || !Util.isDiaHabil(contexto)) {
                            String fechaSQL = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            SqlHomebanking.agendarOperacionFCI(contexto.idCobis(), IDSolicitud, cuotapartista, fechaSQL, importe, fondoId, "Agendada", "HB", "Rescate", tipoCuentaBancaria);
                            if (esDiaHabil && esAntesDeApertura) {
                                // Se ejecuta el dia de hoy al inicio del horario!
                                String anioACtual = String.valueOf(LocalDate.now().getYear());
                                String mesActual = fechaSQL.split("-")[1];
                                String diaActual = String.valueOf(LocalDate.now().getDayOfMonth());
                                String sRequest = createJsonRequestRescate(cantCuotapartes, ctaBancaria, cuotapartista, esTotal, fechaAcreditacion, fechaConcertacion, importe, moneda, inversionFondo, IDSolicitud);
                                RestScheduler.crearTareaFIC(contexto, anioACtual, diaActual, horaInicio, mesActual, minutoInicio, "*", sRequest, "/v1/rescateAgenda");
                            } else {
                                // Se ejecuta el proximo dia habil al inicio del horario!
                                fechaConcertacion = getProximoDiaHabil(contexto);
                                fechaAcreditacion = fechaConcertacion;
                                String anioPosterior = fechaConcertacion.split("-")[0];
                                String mesPosterior = fechaConcertacion.split("-")[1];
                                String diaPosterior = fechaConcertacion.split("-")[2];
                                String sRequest = createJsonRequestRescate(cantCuotapartes, ctaBancaria, cuotapartista, esTotal, fechaAcreditacion, fechaConcertacion, importe, moneda, inversionFondo, IDSolicitud);
                                RestScheduler.crearTareaFIC(contexto, anioPosterior, diaPosterior, horaInicio, mesPosterior, minutoInicio, "*", sRequest, "/v1/rescateAgenda");
                            }

                        } else {
                            // Rescate en horario
                            responseRescate = RestInversiones.rescate(contexto, cantCuotapartes, ctaBancaria, cuotapartista, esTotal, fechaAcreditacion, fechaConcertacion, "28", IDSolicitud, importe, inversionFondo, moneda, "0", "0", "IE");
                            if (!ConfigHB.esProduccion()) {
                                if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_mock_rescate")) {
                                    if (!validarMockRescate()) {
                                        respuestaMockRescateError();
                                    }
                                }
                            }
                            if (responseRescate.hayError()) {
                                CuentasService.reservaAplicaCreditoDebito(contexto, "creditos", cuenta, responseAplicaCredito.string("idTransaccion"), valor, "1301", fechaConcertacion);
                                String mensaje = responseRescate.string("mensajeAlUsuario");
                                return respuestaRescateError(mensaje);
                            }
                        }
                        break;
                    }
                }
            } else {
                /* Ruta Dos */
                if (esFueraHorarioTablaParametria(rowFileTablaParametria) || !Util.isDiaHabil(contexto)) {
                    tipoCuentaBancaria = cuentasBancarias.stream().filter(item -> item.string("idCuentaBancaria").equalsIgnoreCase(ctaBancaria)).toList().get(0).string("descripcionCorta");
                    String fechaSQL = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    SqlHomebanking.agendarOperacionFCI(contexto.idCobis(), IDSolicitud, cuotapartista, fechaSQL, importe, fondoId, "Agendada", "HB", "Rescate", tipoCuentaBancaria);
                    if (esDiaHabil && esAntesDeApertura) {
                        // Se ejecuta el proximo dia habil al inicio del horario!
                        String anioACtual = String.valueOf(LocalDate.now().getYear());
                        String mesActual = fechaSQL.split("-")[1];
                        String diaActual = String.valueOf(LocalDate.now().getDayOfMonth());
                        String sRequest = createJsonRequestRescate(cantCuotapartes, ctaBancaria, cuotapartista, esTotal, fechaAcreditacion, fechaConcertacion, importe, moneda, inversionFondo, IDSolicitud);
                        RestScheduler.crearTareaFIC(contexto, anioACtual, diaActual, horaInicio, mesActual, minutoInicio, "*", sRequest, "/v1/rescateAgenda");
                    } else {
                        // Se ejecuta el proximo dia habil al inicio del horario!
                        fechaConcertacion = getProximoDiaHabil(contexto);
                        fechaAcreditacion = fechaConcertacion;
                        String anioPosterior = fechaConcertacion.split("-")[0];
                        String mesPosterior = fechaConcertacion.split("-")[1];
                        String diaPosterior = fechaConcertacion.split("-")[2];
                        String sRequest = createJsonRequestRescate(cantCuotapartes, ctaBancaria, cuotapartista, esTotal, fechaAcreditacion, fechaConcertacion, importe, moneda, inversionFondo, IDSolicitud);
                        RestScheduler.crearTareaFIC(contexto, anioPosterior, diaPosterior, horaInicio, mesPosterior, minutoInicio, "*", sRequest, "/v1/rescateAgenda");

                    }
                } else {
                    // Rescate en horario
                    responseRescate = RestInversiones.rescate(contexto, cantCuotapartes, ctaBancaria, cuotapartista, esTotal, fechaAcreditacion, fechaConcertacion, "28", IDSolicitud, importe, inversionFondo, moneda, "0", "0", "IE");
                    if (!ConfigHB.esProduccion()) {
                        if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_mock_rescate")) {
                            if (!validarMockRescate()) {
                                return respuestaMockRescateError();
                            }
                        }
                    }
                    if (responseRescate.hayError()) {
                        String mensaje = responseRescate.string("mensajeAlUsuario");
                        return respuestaRescateError(mensaje);
                    }
                }
            }

            if (HBSalesforce.prendidoSalesforce(contexto.idCobis())) {
                String salesforce_suscripcion_y_rescate_fondos = ConfigHB.string("salesforce_suscripcion_y_rescate_fondos");
                Objeto parametros = new Objeto();
                parametros.set("IDCOBIS", contexto.idCobis());
                parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
                parametros.set("NOMBRE", contexto.persona().nombre());
                parametros.set("APELLIDO", contexto.persona().apellido());
                parametros.set("CANAL", "Home Banking");
                new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, salesforce_suscripcion_y_rescate_fondos, parametros));
            }

            contexto.limpiarSegundoFactor();
            ProductosService.eliminarCacheProductos(contexto);
            Respuesta respuesta = new Respuesta();
            respuesta.set("numSolicitud", responseRescate != null ? responseRescate.string("NumSolicitud") : null);

            return respuesta;
        }

    }

    public static Objeto validarRescate(ContextoHB contexto, String fecha, BigDecimal importe, String cuotapartista, String fondoId) {
        contexto.parametros.set("fecha", fecha);
        contexto.parametros.set("idCuotapartista", "");
        contexto.parametros.set("numeroCuotapartista", cuotapartista);
        Objeto validacion = new Objeto();
        Boolean estado = true;
        validacion.set("estado", estado);
        try {
            Objeto objeto = posicionCuotapartistaV3(contexto);

            if ("504".equals(objeto.string("codigo"))) {
                return validacion.set("estado", false).set("mensaje", "TIMEOUT");
            }

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

    public static Respuesta rescateV2(ContextoHB contexto) {
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
        inversionFondo.set("CondicionIngresoEgreso", contexto.parametros.objeto("inversionFondo").string("condicionIngresoEgreso"));
        inversionFondo.set("TipoValorCuotaParte", contexto.parametros.objeto("inversionFondo").string("tipoValorCuotaParte"));
        String IDSolicitud = UUID.randomUUID().toString();
        boolean aceptaTyCAgenda = contexto.parametros.existe("aceptaTyCAgenda") ? contexto.parametros.bool("aceptaTyCAgenda") : false;

        BigDecimal valor = new BigDecimal(importe);
        String tipoCuentaBancaria = "";

        Objeto validacion = validarRescate(contexto, fechaConcertacion, new BigDecimal(importe), cuotapartista, fondoId);
        if (!validacion.bool("estado")) {
            return Respuesta.estado(validacion.string("mensaje").toUpperCase());
        }

        String tipoPersona = getTipoPersona(contexto);
        List<Objeto> tablaParametria = SqlHomebanking.getFondosParametria("RE", tipoPersona);
        for (Objeto fondos : tablaParametria) {
            if (Integer.parseInt(inversionFondo.string("Fondo")) == fondos.integer("id_fondo") && inversionFondo.string("CondicionIngresoEgreso").equals(fondos.string("cond_ingreso_egreso"))) {
                if (fondos.bigDecimal("min_a_operar").compareTo(valor) == 1 && esTotal.equals("0")) {
                    return Respuesta.estado("ERROR_MONTO_MINIMO");
                }
            }
        }

        Objeto rowFileTablaParametria = tablaParametria.stream().filter(tp -> Integer.parseInt(inversionFondo.string("Fondo")) == tp.integer("id_fondo") && inversionFondo.string("CondicionIngresoEgreso").equals(tp.string("cond_ingreso_egreso"))).toList().get(0);

        /* Parametros fuera de horario */
        String horarioInicio = rowFileTablaParametria.string("hora_inicio");
        String horaInicio = horarioInicio.split(":")[0];
        String minutoInicio = horarioInicio.split(":")[1];
        Boolean esDiaHabil = Util.isDiaHabil(contexto);
        Boolean esAntesDeApertura = LocalTime.now().isBefore(LocalTime.parse(horarioInicio));

        ApiResponse responseRescate = null;
        ApiResponse responseAplicaCredito = null;
        ApiResponse responseFondos = RestInversiones.fondos(contexto, Integer.parseInt(cuotapartista), 0, Integer.parseInt(inversionFondo.string("Fondo")), "RE");

        Respuesta responseCuotapartista = cuotapartista(contexto);
        List<Objeto> cuentasBancarias = null;

        for (Objeto cpItem : responseCuotapartista.objetos("cuotapartistas")) {
            if (cuotapartista.equals(cpItem.string("iDCuotapartista"))) {
                cuentasBancarias = cpItem.objetos("ctasBancarias");
                break;
            }
        }

        if (cuentasBancarias == null || cuentasBancarias.isEmpty()) {
            return Respuesta.estado("SIN_CUOTAPARTISTA_CUENTA");
        }

        Boolean esOperacionFueraDeHorario = esFueraHorarioTablaParametria(rowFileTablaParametria) || !Util.isDiaHabil(contexto);

        if (responseFondos.objetos("Table").get(0).integer("PlazoLiquidacionFondo") == 0 && esTotal.equals("0") && !importe.equals("0")) {
            /* Ruta Uno */
            for (Objeto cuentas : cuentasBancarias) {
                if (cuentas.string("idCuentaBancaria").equals(ctaBancaria)) {
                    Cuenta cuenta = contexto.cuenta(cuentas.string("numeroCuenta"));

                    if (!esOperacionFueraDeHorario || (esOperacionFueraDeHorario && aceptaTyCAgenda)) {
                        tipoCuentaBancaria = cuentas.string("descripcionCorta", "");
                        responseAplicaCredito = CuentasService.aplicaCreditoDebito(contexto, "creditos", cuenta, valor, "1301", fechaConcertacion);
                        if (responseAplicaCredito.hayError()) {
                            Respuesta respuesta = Respuesta.estado("ERROR_FUNCIONAL");
                            respuesta.set("mensaje", responseAplicaCredito.string("mensajeAlUsuario"));
                            return respuesta;
                        }
                    }

                    if (esOperacionFueraDeHorario) {
                        // TODO GB - Hacer refactor - este codigo se repite abajo. Extract.
                        if (!aceptaTyCAgenda) {
                            return Respuesta.estado("HORARIO_AGENDA_REQ_TYC");
                        } else {
                            String fechaSQL = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            SqlHomebanking.agendarOperacionFCI(contexto.idCobis(), IDSolicitud, cuotapartista, fechaSQL, importe, fondoId, "Agendada", "HB", "Rescate", tipoCuentaBancaria);
                            if (esDiaHabil && esAntesDeApertura) {
                                // Se ejecuta el dia de hoy al inicio del horario!
                                String anioACtual = String.valueOf(LocalDate.now().getYear());
                                String mesActual = fechaSQL.split("-")[1];
                                String diaActual = String.valueOf(LocalDate.now().getDayOfMonth());
                                String sRequest = createJsonRequestRescate(cantCuotapartes, ctaBancaria, cuotapartista, esTotal, fechaAcreditacion, fechaConcertacion, importe, moneda, inversionFondo, IDSolicitud);
                                RestScheduler.crearTareaFIC(contexto, anioACtual, diaActual, horaInicio, mesActual, minutoInicio, "*", sRequest, "/v1/rescateAgenda");
                            } else {
                                // Se ejecuta el proximo dia habil al inicio del horario!
                                fechaConcertacion = getProximoDiaHabil(contexto);
                                fechaAcreditacion = fechaConcertacion;
                                String anioPosterior = fechaConcertacion.split("-")[0];
                                String mesPosterior = fechaConcertacion.split("-")[1];
                                String diaPosterior = fechaConcertacion.split("-")[2];
                                String sRequest = createJsonRequestRescate(cantCuotapartes, ctaBancaria, cuotapartista, esTotal, fechaAcreditacion, fechaConcertacion, importe, moneda, inversionFondo, IDSolicitud);
                                RestScheduler.crearTareaFIC(contexto, anioPosterior, diaPosterior, horaInicio, mesPosterior, minutoInicio, "*", sRequest, "/v1/rescateAgenda");
                            }
                        }
                    } else {
                        // Rescate en horario
                        responseRescate = RestInversiones.rescate(contexto, cantCuotapartes, ctaBancaria, cuotapartista, esTotal, fechaAcreditacion, fechaConcertacion, "28", IDSolicitud, importe, inversionFondo, moneda, "0", "0", "IE");
                        if (!ConfigHB.esProduccion()) {
                            if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_mock_rescate")) {
                                if (!validarMockRescate()) {
                                    respuestaMockRescateError();
                                }
                            }
                        }
                        if (responseRescate.hayError()) {
                            CuentasService.reservaAplicaCreditoDebito(contexto, "creditos", cuenta, responseAplicaCredito.string("idTransaccion"), valor, "1301", fechaConcertacion);
                            String mensaje = responseRescate.string("mensajeAlUsuario");
                            return respuestaRescateError(mensaje);
                        }
                    }
                    break;
                }
            }
        } else {
            /* Ruta Dos */
            if (esOperacionFueraDeHorario) {
                if (!aceptaTyCAgenda) {
                    return Respuesta.estado("HORARIO_AGENDA_REQ_TYC");
                } else {
                    tipoCuentaBancaria = cuentasBancarias.stream().filter(item -> item.string("idCuentaBancaria").equalsIgnoreCase(ctaBancaria)).toList().get(0).string("descripcionCorta");
                    String fechaSQL = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    SqlHomebanking.agendarOperacionFCI(contexto.idCobis(), IDSolicitud, cuotapartista, fechaSQL, importe, fondoId, "Agendada", "HB", "Rescate", tipoCuentaBancaria);
                    if (esDiaHabil && esAntesDeApertura) {
                        // Se ejecuta el proximo dia habil al inicio del horario!
                        String anioACtual = String.valueOf(LocalDate.now().getYear());
                        String mesActual = fechaSQL.split("-")[1];
                        String diaActual = String.valueOf(LocalDate.now().getDayOfMonth());
                        String sRequest = createJsonRequestRescate(cantCuotapartes, ctaBancaria, cuotapartista, esTotal, fechaAcreditacion, fechaConcertacion, importe, moneda, inversionFondo, IDSolicitud);
                        RestScheduler.crearTareaFIC(contexto, anioACtual, diaActual, horaInicio, mesActual, minutoInicio, "*", sRequest, "/v1/rescateAgenda");
                    } else {
                        // Se ejecuta el proximo dia habil al inicio del horario!
                        fechaConcertacion = getProximoDiaHabil(contexto);
                        fechaAcreditacion = fechaConcertacion;
                        String anioPosterior = fechaConcertacion.split("-")[0];
                        String mesPosterior = fechaConcertacion.split("-")[1];
                        String diaPosterior = fechaConcertacion.split("-")[2];
                        String sRequest = createJsonRequestRescate(cantCuotapartes, ctaBancaria, cuotapartista, esTotal, fechaAcreditacion, fechaConcertacion, importe, moneda, inversionFondo, IDSolicitud);
                        RestScheduler.crearTareaFIC(contexto, anioPosterior, diaPosterior, horaInicio, mesPosterior, minutoInicio, "*", sRequest, "/v1/rescateAgenda");

                    }
                }
            } else {
                // Rescate en horario
                responseRescate = RestInversiones.rescate(contexto, cantCuotapartes, ctaBancaria, cuotapartista, esTotal, fechaAcreditacion, fechaConcertacion, "28", IDSolicitud, importe, inversionFondo, moneda, "0", "0", "IE");
                if (!ConfigHB.esProduccion()) {
                    if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_mock_rescate")) {
                        if (!validarMockRescate()) {
                            return respuestaMockRescateError();
                        }
                    }
                }
                if (responseRescate.hayError()) {
                    String mensaje = responseRescate.string("mensajeAlUsuario");
                    return respuestaRescateError(mensaje);
                }
            }
        }

        if (HBSalesforce.prendidoSalesforce(contexto.idCobis())) {
            String salesforce_suscripcion_y_rescate_fondos = ConfigHB.string("salesforce_suscripcion_y_rescate_fondos");
            Objeto parametros = new Objeto();
            parametros.set("IDCOBIS", contexto.idCobis());
            parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
            parametros.set("NOMBRE", contexto.persona().nombre());
            parametros.set("APELLIDO", contexto.persona().apellido());
            parametros.set("CANAL", "Home Banking");
            new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, salesforce_suscripcion_y_rescate_fondos, parametros));
        }

        contexto.limpiarSegundoFactor();
        ProductosService.eliminarCacheProductos(contexto);
        Respuesta respuesta = new Respuesta();
        respuesta.set("numSolicitud", responseRescate != null ? responseRescate.string("NumSolicitud") : null);

        return respuesta;

    }

    public static Respuesta rescateV3(ContextoHB contexto) {
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
            return Respuesta.estado(validacion.string("mensaje").toUpperCase());
        }

        String tipoPersona = getTipoPersona(contexto);
        List<Objeto> tablaParametria = SqlHomebanking.getFondosParametria("RE", tipoPersona);
        for (Objeto fondos : tablaParametria) {
            if (Integer.parseInt(inversionFondo.string("Fondo")) == fondos.integer("id_fondo")
                    && inversionFondo.string("CondicionIngresoEgreso").equals(fondos.string("cond_ingreso_egreso"))) {
                if (fondos.bigDecimal("min_a_operar").compareTo(valor) == 1 && esTotal.equals("0")) {
                    return Respuesta.estado("ERROR_MONTO_MINIMO");
                }
            }
        }

        Objeto rowFileTablaParametria = tablaParametria.stream()
                .filter(tp -> Integer.parseInt(inversionFondo.string("Fondo")) == tp.integer("id_fondo")
                        && inversionFondo.string("CondicionIngresoEgreso").equals(tp.string("cond_ingreso_egreso")))
                .toList().get(0);

        /* Parametros fuera de horario */
        String horarioInicio = rowFileTablaParametria.string("hora_inicio");
        String horarioFin = rowFileTablaParametria.string("hora_fin");
        String horaInicio = horarioInicio.split(":")[0];
        String minutoInicio = horarioInicio.split(":")[1];
        Boolean esDiaHabil = Util.isDiaHabil(contexto);
        Boolean esAntesDeApertura = LocalTime.now().isBefore(LocalTime.parse(horarioInicio));
        String horaInicioBatchFCI = ConfigHB.string("hora_inicio_batch_fci", "22:00");
        String horaFinBatchFCI = ConfigHB.string("hora_fin_batch_fci", "00:00");
        LocalTime inicioBatch = LocalTime.parse(horaInicioBatchFCI);
        LocalTime finBatch = LocalTime.parse(horaFinBatchFCI);
        LocalTime ahora = LocalTime.now();
        LocalTime inicioFondo = LocalTime.parse(horarioInicio);
        LocalTime finFondo = LocalTime.parse(horarioFin);

        ApiResponse responseRescate = null;
        ApiResponse responseAplicaCredito = null;
        ApiResponse responseFondos = RestInversiones.fondos(contexto, Integer.parseInt(cuotapartista), 0,
                Integer.parseInt(inversionFondo.string("Fondo")), "RE");

        Respuesta responseCuotapartista = cuotapartista(contexto);
        List<Objeto> cuentasBancarias = null;

        for (Objeto cpItem : responseCuotapartista.objetos("cuotapartistas")) {
            if (cuotapartista.equals(cpItem.string("iDCuotapartista"))) {
                cuentasBancarias = cpItem.objetos("ctasBancarias");
                break;
            }
        }

        if (cuentasBancarias == null || cuentasBancarias.isEmpty()) {
            return Respuesta.estado("SIN_CUOTAPARTISTA_CUENTA");
        }

        if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_modo_transaccional_rescate_fondos",
                "prendido_modo_transaccional_rescate_fondos_cobis") && !TransmitHB.isChallengeOtp(contexto, "rescate-fondos")) {
            try {
                String sessionToken = contexto.parametros.string(Transmit.getSessionToken(), null);
                if (Objeto.empty(sessionToken))
                    return Respuesta.parametrosIncorrectos();

                Cuenta cuentaOrigen = null;
                for (Objeto cuentas : cuentasBancarias) {
                    if (cuentas.string("idCuentaBancaria").equals(ctaBancaria))
                        cuentaOrigen = contexto.cuenta(cuentas.string("numeroCuenta"));
                }

                if (!Objeto.empty(cuentaOrigen)) {
                    RescateHBBMBankProcess rescateHBBMBankProcess = new RescateHBBMBankProcess(contexto.idCobis(),
                            sessionToken,
                            valor,
                            Util.obtenerDescripcionMonedaTransmit(cuentaOrigen.idMoneda()),
                            TransmitHB.REASON_RESCATE,
                            new RescateHBBMBankProcess.Payer(contexto.persona().cuit(), cuentaOrigen.numero(), Util.BH_CODIGO, TransmitHB.CANAL),
                            new RescateHBBMBankProcess.Payee(contexto.persona().cuit(), "", ""));

                    Respuesta respuesta = TransmitHB.recomendacionTransmit(contexto, rescateHBBMBankProcess, "rescate-fondos");
                    if (respuesta.hayError())
                        return respuesta;
                }
            } catch (Exception e) {
            }
        }

        Boolean esOperacionFueraDeHorario = esFueraHorarioTablaParametria(rowFileTablaParametria)
                || !Util.isDiaHabil(contexto);

        if (esOperacionFueraDeHorario && !aceptaTyCAgenda) {
            return Respuesta.estado("HORARIO_AGENDA_REQ_TYC");
        }

        String causalCredito = "1301";
        if (!esOperacionFueraDeHorario) {
            causalCredito = "1301";
        } else {
            if (!Util.isDiaHabil(contexto)) {
                causalCredito = "1322";
            } else {
                if (ahora.isBefore(inicioFondo) && ahora.isAfter(finBatch)){
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
                    Cuenta cuenta = contexto.cuenta(cuentas.string("numeroCuenta"));
                    // FIXME GB - Aca no es "cuenta"?
                    tipoCuentaBancaria = cuentas.string("descripcionCorta", "");
                    if (!esOperacionFueraDeHorario || (esOperacionFueraDeHorario && aceptaTyCAgenda)) {
                        responseAplicaCredito = CuentasService.aplicaCreditoDebito(contexto, "creditos", cuenta, valor,
                                causalCredito, fechaConcertacion);
                        if (responseAplicaCredito.hayError()) {
                            Respuesta respuesta = Respuesta.estado("ERROR_FUNCIONAL");
                            respuesta.set("mensaje", responseAplicaCredito.string("mensajeAlUsuario"));
                            return respuesta;
                        }
                    }

                    if (!esOperacionFueraDeHorario) {
                        // Rescate en horario
                        responseRescate = RestInversiones.rescate(contexto, cantCuotapartes, ctaBancaria, cuotapartista,
                                esTotal, fechaAcreditacion, fechaConcertacion, "28", IDSolicitud, importe,
                                inversionFondo, moneda, "0", "0", "IE");
                        if (!ConfigHB.esProduccion()) {
                            if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_mock_rescate")) {
                                if (!validarMockRescate()) {
                                    respuestaMockRescateError();
                                }
                            }
                        }
                        if (responseRescate.hayError()) {
                            CuentasService.reservaAplicaCreditoDebito(contexto, "creditos", cuenta,
                                    responseAplicaCredito.string("idTransaccion"), valor, causalCredito, fechaConcertacion);
                            String mensaje = responseRescate.string("mensajeAlUsuario");
                            return respuestaRescateError(mensaje);
                        }
                    }
                    break;
                }
            }
        } else {
            /* Ruta Dos */
            tipoCuentaBancaria = cuentasBancarias.stream()
                    .filter(item -> item.string("idCuentaBancaria").equalsIgnoreCase(ctaBancaria)).toList().get(0)
                    .string("descripcionCorta");

            if (!esOperacionFueraDeHorario) {
                // Rescate en horario
                responseRescate = RestInversiones.rescate(contexto, cantCuotapartes, ctaBancaria, cuotapartista,
                        esTotal, fechaAcreditacion, fechaConcertacion, "28", IDSolicitud, importe, inversionFondo,
                        moneda, "0", "0", "IE");
                if (!ConfigHB.esProduccion()) {
                    if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_mock_rescate")) {
                        if (!validarMockRescate()) {
                            return respuestaMockRescateError();
                        }
                    }
                }
                if (responseRescate.hayError()) {
                    String mensaje = responseRescate.string("mensajeAlUsuario");
                    return respuestaRescateError(mensaje);
                }
            }
        }

        if (esOperacionFueraDeHorario) {
            ApiResponse respuestaAgenda = agendarRescate(contexto, cantCuotapartes, ctaBancaria, cuotapartista, esTotal,
                    fechaConcertacion, fechaAcreditacion, importe, moneda, inversionFondo, fondoId, IDSolicitud,
                    tipoCuentaBancaria, horaInicio, minutoInicio, esDiaHabil, esAntesDeApertura);

            if (respuestaAgenda.hayError()) {
                return Respuesta.estado("ERROR_AGENDA");
            }
        }

        if (HBSalesforce.prendidoSalesforce(contexto.idCobis())) {
            String salesforce_suscripcion_y_rescate_fondos = ConfigHB.string("salesforce_suscripcion_y_rescate_fondos");
            Objeto parametros = new Objeto();
            parametros.set("IDCOBIS", contexto.idCobis());
            parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
            parametros.set("NOMBRE", contexto.persona().nombre());
            parametros.set("APELLIDO", contexto.persona().apellido());
            parametros.set("CANAL", "Home Banking");
            new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, salesforce_suscripcion_y_rescate_fondos, parametros));
        }

        contexto.limpiarSegundoFactor();
        ProductosService.eliminarCacheProductos(contexto);
        return new Respuesta().set("numSolicitud",
                responseRescate != null ? responseRescate.string("NumSolicitud") : null);
    }



    private static ApiResponse agendarRescate(ContextoHB contexto, Integer cantCuotapartes, String ctaBancaria,
                                              String cuotapartista, String esTotal, String fechaConcertacion, String fechaAcreditacion, String importe,
                                              String moneda, Objeto inversionFondo, String fondoId, String IDSolicitud, String tipoCuentaBancaria,
                                              String horaInicio, String minutoInicio, Boolean esDiaHabil, Boolean esAntesDeApertura) {
        LocalDateTime fechaHoraActual = LocalDateTime.now();

        SqlHomebanking.agendarOperacionFCI(contexto.idCobis(), IDSolicitud, cuotapartista,
                fechaHoraActual.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), importe, fondoId,
                "Agendada", "HB", "Rescate", tipoCuentaBancaria);

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

        Objeto body = RestInversiones.crearBodyRequestRescate(cantCuotapartes, ctaBancaria, cuotapartista, esTotal,
                fechaAcreditacion, fechaConcertacion, "28", IDSolicitud, importe, inversionFondo, moneda, "0", "0",
                "IE");

        // TODO GB Agenda - registrar estado error en tabla
        return RestInversiones.rescateAgenda(contexto, fechaHoraEjecucion, body);
    }

    private static ApiResponse agendarSuscripcion(ContextoHB contexto, String cuotapartista, String fechaConcertacion,
                                                  String importe, Objeto inversionFondo, String fondoId, String moneda, String tipoCuentaBancaria,
                                                  String IDSolicitud, String fechaAcreditacion, Objeto cuentaBancaria, Objeto formasPagoCuentaBancaria,
                                                  String horaInicio, String minutoInicio, Boolean esDiaHabil, Boolean esAntesDeApertura) {
        LocalDateTime fechaHoraActual = LocalDateTime.now();
        SqlHomebanking.agendarOperacionFCI(contexto.idCobis(), IDSolicitud, cuotapartista,
                fechaHoraActual.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), importe, fondoId,
                "Agendada", "HB", "Suscripcion", tipoCuentaBancaria);

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

        Objeto body = RestInversiones.crearBodyRequestSuscripcion(cuentaBancaria, cuotapartista, fechaAcreditacion,
                fechaConcertacion, formasPagoCuentaBancaria, IDSolicitud, inversionFondo, moneda, "IE");

        return RestInversiones.suscripcionAgenda(contexto, fechaHoraEjecucion, body);
    }

    private static String createJsonRequestRescate(Integer cantCuotapartes, String ctaBancaria, String cuotapartista, String esTotal, String fechaAcreditacion, String fechaConcertacion, String importe, String moneda, Objeto inversionFondo, String IDSolicitud) {
        String sRequest = "{\"pSolicitudRescate\":{ " + "\"IDSolicitud\":\"" + IDSolicitud + "\"," + "\"FechaAcreditacion\":\"" + fechaAcreditacion + "\"," + "\"CantCuotapartes\":" + cantCuotapartes + "," + "\"CtaBancaria\":\"" + ctaBancaria + "\"," + "\"Cuotapartista\":\"" + cuotapartista + "\"," + "\"EsTotal\":\"" + esTotal + "\"," + "\"FechaConcertacion\":\"" + fechaConcertacion + "\"," + "\"FormaCobro\":\"" + "28" + "\"," + "\"Importe\":\"" + importe + "\"," + "\"InversionFondo\":{ " + "\"Fondo\":\"" + inversionFondo.string("Fondo") + "\"," + "\"CondicionIngresoEgreso\":\"" + inversionFondo.string("CondicionIngresoEgreso") + "\"," + "\"TipoValorCuotaParte\":\"" + inversionFondo.string("TipoValorCuotaParte") + "\"}," + "\"Moneda\":\"" + moneda + "\"," + "\"PorcGastos\":\"" + "0" + "\","
                + "\"PorcGtoBancario\":\"" + "0" + "\"," + "\"TpOrigenSol\":" + "\"IE\"" + "}}";
        return sRequest;
    }

    private static Respuesta respuestaMockRescateError() {
        Respuesta respuesta = new Respuesta();
        respuesta.setEstado("ERROR_CUOTAPARTES");
        return respuesta;
    }

    private static Respuesta respuestaRescateError(String mensaje) {
        Respuesta respuesta = new Respuesta();
        if (mensaje.contains("El Cuotapartista no tiene Cuotapartes disponibles")) {
            respuesta.setEstado("ERROR_CUOTAPARTES");
        } else {
            respuesta = Respuesta.estado("ERROR_FUNCIONAL");
            respuesta.set("mensaje", mensaje);
        }
        return respuesta;
    }

    private static boolean validarMockRescate() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    public static Respuesta validarPerfilInversor(ContextoHB contexto, Optional<EnumPerfilInversor> perfilInstrumento,
                                                  boolean operarBajoPropioRiesgo) {
        // TODO GB T369 - Validacion de perfiles para fondos.
        Respuesta resultado = new Respuesta();
        resultado.set("operaBajoPropioRiesgo", false);

        Respuesta responseConsultaPerfilInversor = perfilInversor(contexto);

        if (responseConsultaPerfilInversor.hayError()) {
            return Respuesta.error();
        }

        if (responseConsultaPerfilInversor.bool("tienePerfilInversor")
                && !responseConsultaPerfilInversor.bool("vencido") ) {
            resultado.set("operaBajoPropioRiesgo", EnumPerfilInversor
                    .codigo(responseConsultaPerfilInversor.integer("idPerfil")).esOperaBajoPropioRiesgo()
                    || operarBajoPropioRiesgo);
        } else if (operarBajoPropioRiesgo) {
            Respuesta respuestaAltaPerfilPropioRiesgo = HBInversion.perfilInversorPropioRiesgo(contexto);
            if (respuestaAltaPerfilPropioRiesgo.hayError()) {
                return Respuesta.error();
            }
            resultado.set("operaBajoPropioRiesgo", true);
        } else {
            return Respuesta.error();
        }

        if (perfilInstrumento.isPresent() && !resultado.bool("operaBajoPropioRiesgo")
                && validacionOperacionConPerfil(responseConsultaPerfilInversor,
                perfilInstrumento.get().getCodigo()) == C_RES_VAL_INVERSION_PERFIL_NO_ADECUADO) {
            return Respuesta.error();
        }

        return resultado;
    }

    public static Respuesta fondosAgendados(ContextoHB contexto) {
        Respuesta respuesta = new Respuesta();
        String cuotapartista = contexto.parametros.string("cuotapartista");
        String estado = "Agendada";
        List<Objeto> fondosAgendados = SqlHomebanking.getAgendaSuscripcionFCI(contexto.idCobis(), cuotapartista, estado);
        respuesta.set("fondosAdendadosFCI", fondosAgendados);
        return respuesta;
    }

    public static Respuesta procesoCron(ContextoHB contexto) {
        Respuesta respuesta = new Respuesta();
        ApiResponse responseCalendario = RestCatalogo.calendarioFechaActual(contexto);
        if (responseCalendario.hayError()) {
            return null;
        }

        String cuotapartista = contexto.parametros.string("cuotapartista");
//		String id_cuenta_bancaria = contexto.parametros.string("IDCuentaBancaria");
//		String estado = "Agendada";

//		List<Objeto> fondosAgendados = SqlHomebanking.getOrdenesAgendadasFCIPorEstado(estado); // Obtengo de la bbd

        String fechaDesde = responseCalendario.objetos().get(0).string("diaHabilAnterior"); // DIA HOY - 3
        String fechaHasta = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // DIA DE HOY
        Integer id_cuotaPartista = Integer.parseInt(cuotapartista);

        ApiResponse response = RestInversiones.solicitudes(contexto, fechaDesde, fechaHasta, null, null, null, null, null, id_cuotaPartista);

        List<Objeto> solicitudesSL = response.objetos("SolicitudGenericoModel");
        return respuesta.set("solicitudesSL", solicitudesSL);
        /*
         * // Ahora hace la logica List<Objeto> listaMatch = new ArrayList<>();
         * List<Objeto> listaNoMatch;
         *
         * listaMatch = fondosAgendados.stream() .filter(fondoAgendado ->
         * solicitudesSL.stream() .anyMatch(solicitudSL ->
         * fondoAgendado.string("id_solicitud").contains(solicitudSL.string(
         * "IDSolicitud")))) .collect(Collectors.toList());
         *
         * listaNoMatch = fondosAgendados.stream() .filter(fondoAgendado ->
         * solicitudesSL.stream() .noneMatch(solicitudSL ->
         * fondoAgendado.string("id_solicitud").contains(solicitudSL.string(
         * "IDSolicitud")))) .collect(Collectors.toList());
         *
         *
         * //listaMatch.forEach(lista ->
         * SqlHomebanking.updateAgendadoFCI(lista.string("id_solicitud"), "Procesado"));
         * //listaNoMatch.forEach(lista ->
         * SqlHomebanking.updateAgendadoFCI(lista.string("id_solicitud"), "Error"));
         * return respuesta.set("listaMatch", listaMatch).set("listaNoMatch",
         * listaNoMatch);
         */
    } // Todo: eliminar

    public static Respuesta actualizaOrdenesFCIOnDemand(ContextoHB contexto) {
        if (!HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_onDeman_FCI")) {
            return new Respuesta().setEstado("NO_HAY_ORDENES_FCI_AGENDADAS");
        }
        ApiResponse responseCalendario = RestCatalogo.calendarioFechaActual(contexto);
        String fechaDesde = responseCalendario.objetos().get(0).string("diaHabilAnterior"); // DIA HOY - 2
        String fechaHasta = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // DIA DE HOY

        // Validacion fuera Horario
        String tipoSolicitud = contexto.parametros.string("tipoSolicitud");
        Objeto horaMaximaMinima = SqlHomebanking.getHorarioMaximoMinimo(tipoSolicitud);
        String horaInicio = horaMaximaMinima.string("hora_inicio");
        String horaFin = horaMaximaMinima.string("hora_fin");
        List<Objeto> operacionesAgendadas = SqlHomebanking.getOrdenesAgendadasFCIPorEstadoAndCobis("Agendada", contexto.idCobis()); // Lista SuscipcionBD

        if (operacionesAgendadas.isEmpty()) {
            return new Respuesta().setEstado("NO_HAY_ORDENES_FCI_AGENDADAS"); // No hay nada que updatear
        }
        if (comprobacionFueraDeHorario(horaInicio, horaFin) || !Util.isDiaHabil(contexto)) {
            return new Respuesta().setEstado("FUERA_DE_HORARIO").set("operacionesAgendadas", operacionesAgendadas);
        }

        List<Objeto> solicitudesSL = new ArrayList<>();
        List<String> listaCuotaPartista = operacionesAgendadas // CuotaPartistas Unicos
                .stream().map(fondoAgendado -> fondoAgendado.string("cuotapartista")).distinct().collect(Collectors.toList());

        listaCuotaPartista.forEach(cuotaPartista -> {
            ApiResponse responsex = RestInversiones.solicitudespruebaServiceLayer(contexto, fechaDesde, fechaHasta, null, null, null, null, null, Integer.parseInt(cuotaPartista));
            solicitudesSL.addAll(responsex.objetos("SolicitudGenericoModel"));
        });

        List<Objeto> listaMatch; // Esta lista actualiza a procesada la bbdd
        List<Objeto> listaNoMatch; // Esta lista actualiza a error la bbdd

        listaMatch = operacionesAgendadas.stream().filter(operacionAgendada -> solicitudesSL.stream().anyMatch(solicitudSL -> operacionAgendada.string("id_solicitud").contains(solicitudSL.string("IDSolicitud")) && operacionAgendada.string("cuotapartista").equals(solicitudSL.string("CuotapartistaNumero")))).collect(Collectors.toList());
        listaMatch.forEach(itemLista -> SqlHomebanking.updateEstadoOrdenesAgendadasFCI(itemLista.string("id_solicitud"), "Procesado"));

        listaNoMatch = operacionesAgendadas.stream().filter(operacionAgendada -> solicitudesSL.stream().noneMatch(solicitudSL -> operacionAgendada.string("id_solicitud").contains(solicitudSL.string("IDSolicitud")) && operacionAgendada.string("cuotapartista").equals(solicitudSL.string("CuotapartistaNumero")))).collect(Collectors.toList());

//		List<Objeto> fondosAgendadosError = SqlHomebanking.getOrdenesAgendadasFCIPorEstadoAndCobis("Error", contexto.idCobis());
        return new Respuesta().setEstado("ORDENES_FCI_ACTUALIZADAS").set("listaMatch", listaMatch).set("listaNoMatch", listaNoMatch);
    }

    public static Respuesta updateAgendadoFCI(ContextoHB contexto) { // TODO: eliminar
        String idSolicitud = contexto.parametros.string("idSolicitud");
        String estado = contexto.parametros.string("estado");

        SqlHomebanking.updateEstadoOrdenesAgendadasFCI(idSolicitud, estado);
        return null;
    }

    public static Respuesta suscripcionV3(ContextoHB contexto) {
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

        Respuesta responseCuotapartista = cuotapartista(contexto);
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
                        return Respuesta.estado("ERROR_MONEDA_NO_COINCIDE");
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
                    return Respuesta.estado("ERROR_MONTO_MAXIMO");
                }
            }
        }

        Objeto rowFileTablaParametria = tablaParametria.stream()
                .filter(tp -> tp.integer("id_fondo") == Integer.parseInt(inversionFondo.string("Fondo"))).toList()
                .get(0);

        Respuesta respuesta = new Respuesta();
        // Parametros fuera Horario
        String horarioInicio = rowFileTablaParametria.string("hora_inicio");
        String horaInicio = horarioInicio.split(":")[0];
        String minutoInicio = horarioInicio.split(":")[1];
        String horarioFin = rowFileTablaParametria.string("hora_fin");
        Boolean esDiaHabil = Util.isDiaHabil(contexto);
        Boolean esAntesDeApertura = LocalTime.now().isBefore(LocalTime.parse(horarioInicio));

        String horaInicioBatchFCI = ConfigHB.string("hora_inicio_batch_fci", "22:00");
        String horaFinBatchFCI = ConfigHB.string("hora_fin_batch_fci", "01:30");

        LocalTime inicioBatch = LocalTime.parse(horaInicioBatchFCI);
        LocalTime finBatch = LocalTime.parse(horaFinBatchFCI);
        LocalTime ahora = LocalTime.now();
        LocalTime inicioFondo = LocalTime.parse(horarioInicio);
        LocalTime finFondo = LocalTime.parse(horarioFin);

        Boolean esOperacionFueraDeHorario = esFueraHorarioTablaParametria(rowFileTablaParametria)
                || !Util.isDiaHabil(contexto);

        String causalDebito = "1300";
        if (!esOperacionFueraDeHorario) {
            causalDebito = "1300";
        } else {
            if (!Util.isDiaHabil(contexto)) {
                causalDebito = "1321";
            } else {
                if (ahora.isBefore(inicioFondo) && ahora.isAfter(finBatch)){
                    causalDebito = "1300";
                } else {
                    if (ahora.isBefore(inicioBatch) && ahora.isAfter(finFondo)) {
                        causalDebito = "1321";
                    }
                }
            }
        }

        ApiResponse responseAplicaDebito = null;
        if (!esOperacionFueraDeHorario || (esOperacionFueraDeHorario && aceptaTyCAgenda)) {
            responseAplicaDebito = CuentasService.aplicaCreditoDebito(contexto, "debitos",
                    contexto.cuenta(cuentaBancaria.string("NumeroCuenta")), valor, causalDebito, fechaConcertacion);
            if (responseAplicaDebito.hayError()) {
                respuesta = Respuesta.estado("ERROR_FUNCIONAL");
                respuesta.set("mensaje", responseAplicaDebito.string("mensajeAlUsuario"));
                return respuesta;
            }
        }

        if (esOperacionFueraDeHorario) {
            if (!aceptaTyCAgenda) {
                return Respuesta.estado("HORARIO_AGENDA_REQ_TYC");
            } else {
                ApiResponse responseAgenda = agendarSuscripcion(contexto, cuotapartista, fechaConcertacion, importe, inversionFondo, fondoId, moneda,
                        tipoCuentaBancaria, IDSolicitud, fechaAcreditacion, cuentaBancaria, formasPagoCuentaBancaria,
                        horaInicio, minutoInicio, esDiaHabil, esAntesDeApertura);
                if (responseAgenda.hayError()) {
                    return Respuesta.error();
                }
                respuesta.setEstado(("FUERA_DE_HORARIO"));
            }
        } else {
            // Flujo normal Suscripcion en Horario
            ApiResponse responseSuscripcion = RestInversiones.suscripcion(contexto, cuentaBancaria, cuotapartista,
                    fechaAcreditacion, fechaConcertacion, formasPagoCuentaBancaria, IDSolicitud, inversionFondo, moneda,
                    "IE");
            if (responseSuscripcion.hayError()) {
                CuentasService.reservaAplicaCreditoDebito(contexto, "debitos",
                        contexto.cuenta(cuentaBancaria.string("NumeroCuenta")),
                        responseAplicaDebito.string("idTransaccion"), valor, causalDebito, fechaConcertacion);
                respuesta = Respuesta.estado("ERROR_FUNCIONAL");
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

        if (HBSalesforce.prendidoSalesforce(contexto.idCobis())) {
            String salesforce_suscripcion_y_rescate_fondos = ConfigHB.string("salesforce_suscripcion_y_rescate_fondos");
            Objeto parametros = new Objeto();
            parametros.set("IDCOBIS", contexto.idCobis());
            parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
            parametros.set("NOMBRE", contexto.persona().nombre());
            parametros.set("APELLIDO", contexto.persona().apellido());
            parametros.set("CANAL", "Home Banking");
            new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, salesforce_suscripcion_y_rescate_fondos, parametros));
        }

        ProductosService.eliminarCacheProductos(contexto);
        Api.eliminarCache(contexto, "PosicionCuotapartista", contexto.idCobis(),
                contexto.parametros.string("cuotapartista"), Fecha.fechaActual());
        return respuesta;
    }

    public static Respuesta suscripcionV2(ContextoHB contexto) {
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

        String tipoCuentaBancaria = "";
        String IDSolicitud = UUID.randomUUID().toString();
        String fechaAcreditacion = fechaConcertacion;
        Objeto cuentaBancaria = new Objeto();
        Objeto formasPagoCuentaBancaria = new Objeto();
        BigDecimal valor = new BigDecimal(importe);

        // List<Cuenta> responseCuentas = contexto.cuentas();
        Respuesta responseCuotapartista = cuotapartista(contexto);
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
                    return Respuesta.estado("ERROR_MONTO_MAXIMO");
                }
            }
        }

        Objeto rowFileTablaParametria = tablaParametria.stream().filter(tp -> tp.integer("id_fondo") == Integer.parseInt(inversionFondo.string("Fondo"))).toList().get(0);

        Respuesta respuesta = new Respuesta();
        // Parametros fuera Horario
        String horarioInicio = rowFileTablaParametria.string("hora_inicio");
        String horaInicio = horarioInicio.split(":")[0];
        String minutoInicio = horarioInicio.split(":")[1];

        Boolean esDiaHabil = Util.isDiaHabil(contexto);
        Boolean esAntesDeApertura = LocalTime.now().isBefore(LocalTime.parse(horarioInicio));

        Boolean esOperacionFueraDeHorario = esFueraHorarioTablaParametria(rowFileTablaParametria) || !Util.isDiaHabil(contexto);

        ApiResponse responseAplicaDebito = null;
        if (!esOperacionFueraDeHorario || (esOperacionFueraDeHorario && aceptaTyCAgenda)) {
            responseAplicaDebito = CuentasService.aplicaCreditoDebito(contexto
                    , "debitos", contexto.cuenta(cuentaBancaria.string("NumeroCuenta"))
                    , valor, "1300", fechaConcertacion);
            if (responseAplicaDebito.hayError()) {
                respuesta = Respuesta.estado("ERROR_FUNCIONAL");
                respuesta.set("mensaje", responseAplicaDebito.string("mensajeAlUsuario"));
                return respuesta;
            }
        }

        if (esOperacionFueraDeHorario) {
            if (!aceptaTyCAgenda) {
                return Respuesta.estado("HORARIO_AGENDA_REQ_TYC");
            } else {
                String fechaSQL = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                SqlHomebanking.agendarOperacionFCI(contexto.idCobis(), IDSolicitud, cuotapartista, fechaSQL, importe, fondoId, "Agendada", "HB", "Suscripcion", tipoCuentaBancaria);

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
            ApiResponse responseSuscripcion = RestInversiones.suscripcion(contexto, cuentaBancaria, cuotapartista, fechaAcreditacion, fechaConcertacion, formasPagoCuentaBancaria, IDSolicitud, inversionFondo, moneda, "IE");
            if (responseSuscripcion.hayError()) {
                CuentasService.reservaAplicaCreditoDebito(contexto, "debitos", contexto.cuenta(cuentaBancaria.string("NumeroCuenta")), responseAplicaDebito.string("idTransaccion"), valor, "1300", fechaConcertacion);
                respuesta = Respuesta.estado("ERROR_FUNCIONAL");
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

        if (HBSalesforce.prendidoSalesforce(contexto.idCobis())) {
            String salesforce_suscripcion_y_rescate_fondos = ConfigHB.string("salesforce_suscripcion_y_rescate_fondos");
            Objeto parametros = new Objeto();
            parametros.set("IDCOBIS", contexto.idCobis());
            parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
            parametros.set("NOMBRE", contexto.persona().nombre());
            parametros.set("APELLIDO", contexto.persona().apellido());
            parametros.set("CANAL", "Home Banking");
            new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, salesforce_suscripcion_y_rescate_fondos, parametros));
        }

        ProductosService.eliminarCacheProductos(contexto);
        Api.eliminarCache(contexto, "PosicionCuotapartista", contexto.idCobis(), contexto.parametros.string("cuotapartista"), Fecha.fechaActual());
        return respuesta;
    }

    public static Respuesta suscripcion(ContextoHB contexto) {
        Api.eliminarCache(contexto, "PosicionCuotapartista", contexto.idCobis(), contexto.parametros.string("cuotapartista"), Fecha.fechaActual());

        Respuesta resultado = validarPerfilInversor(contexto, Optional.empty(),
                contexto.parametros.bool("operaFueraPerfil", false));
        if (resultado.hayError()) {
            Respuesta respuesta = Respuesta.estado("ERROR_FUNCIONAL");
            respuesta.set("mensaje", "Perfil inversor no válido para operar");
            return respuesta;
        }

        if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_fondos_fuera_de_horario", "prendido_fondos_fuera_de_horario_cobis")) {
            if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_fondos_fuera_de_horario_v2", "prendido_fondos_fuera_de_horario_v2_cobis")) {
                return suscripcionV3(contexto);
            } else {
                return suscripcionV2(contexto);
            }
        } else {
            Futuro<Respuesta> responseCuotapartistaFuturo = new Futuro<>(() -> cuotapartista(contexto));
            String tipoPersona = getTipoPersona(contexto);
            Futuro<List<Objeto>> tablaParametriaFuturo = new Futuro<>(() -> SqlHomebanking.getFondosParametria("SU", tipoPersona));

            String IDCuentaBancaria = contexto.parametros.string("IDCuentaBancaria");
            String cuotapartista = contexto.parametros.string("cuotapartista");
            String fechaAcreditacion = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
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

            Respuesta responseCuotapartista = responseCuotapartistaFuturo.get();

            Futuro<ApiResponse> responseAplicaDebitoFuturo = new Futuro<>(() -> CuentasService.aplicaCreditoDebito(contexto, "debitos", contexto.cuenta(cuentaBancaria.string("NumeroCuenta")), valor, "1300", fechaConcertacion));

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

            Futuro<ApiResponse> responseSuscripcionFuturo = new Futuro<>(() -> RestInversiones.suscripcion(contexto, cuentaBancaria, cuotapartista, fechaAcreditacion, fechaConcertacion, formasPagoCuentaBancaria, IDSolicitud, inversionFondo, moneda, "IE"));

            List<Objeto> tablaParametria = tablaParametriaFuturo.get();
            for (Objeto fondos : tablaParametria) {
                if (Integer.parseInt(inversionFondo.string("Fondo")) == fondos.integer("id_fondo") && inversionFondo.string("CondicionIngresoEgreso").equals(fondos.string("cond_ingreso_egreso"))) {
                    if (fondos.bigDecimal("max_a_operar").compareTo(valor) == 0) {
                        return Respuesta.estado("ERROR_MONTO_MAXIMO");
                    }
                }
            }

            ApiResponse responseAplicaDebito = responseAplicaDebitoFuturo.get();
            if (responseAplicaDebito.hayError()) {
                Respuesta respuesta = Respuesta.estado("ERROR_FUNCIONAL");
                respuesta.set("mensaje", responseAplicaDebito.string("mensajeAlUsuario"));
                return respuesta;
            }
            Respuesta respuesta = new Respuesta();

            ApiResponse responseSuscripcion = responseSuscripcionFuturo.get();
            if (responseSuscripcion.hayError()) {
                CuentasService.reservaAplicaCreditoDebito(contexto, "debitos", contexto.cuenta(cuentaBancaria.string("NumeroCuenta")), responseAplicaDebito.string("idTransaccion"), valor, "1300", fechaConcertacion);
                respuesta = Respuesta.estado("ERROR_FUNCIONAL");
                respuesta.set("mensaje", responseSuscripcion.string("mensajeAlUsuario"));
                return respuesta;
            }
            // Retorno respuesta con Solicitud
            respuesta.set("numSolicitud", responseSuscripcion.string("NumSolicitud"));

            List<Objeto> fondosAceptados = SqlHomebanking.getFondosAceptados(contexto.idCobis(), Integer.parseInt(inversionFondo.string("Fondo")));
            if (fondosAceptados.size() == 0) {
                SqlHomebanking.registrarFondo(contexto.idCobis(), contexto.persona().cuit(), new Date(), 24, Integer.parseInt(inversionFondo.string("Fondo")));
            }

            if (HBSalesforce.prendidoSalesforce(contexto.idCobis())) {
                String salesforce_suscripcion_y_rescate_fondos = ConfigHB.string("salesforce_suscripcion_y_rescate_fondos");
                Objeto parametros = new Objeto();
                parametros.set("IDCOBIS", contexto.idCobis());
                parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
                parametros.set("NOMBRE", contexto.persona().nombre());
                parametros.set("APELLIDO", contexto.persona().apellido());
                parametros.set("CANAL", "Home Banking");
                new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, salesforce_suscripcion_y_rescate_fondos, parametros));
            }

            ProductosService.eliminarCacheProductos(contexto);
            return respuesta;
        }
    }

    private static String createJsonRequestSuscripcion(Objeto cuentaBancaria, String cuotapartista, String fechaConcertacion, String fechaAcreditacion, Objeto formasPagoCuentaBancaria, String IDSolicitud, Objeto inversionFondo, String moneda) {
        String sRequest = "{\"SuscripcionSL\":{ " + "" + "\"AceptacionDocumentacionWEB\": null," + "\"CuentaBancaria\":{" + "\"IDCuentaBancaria\":\"" + cuentaBancaria.string("IDCuentaBancaria") + "\"," + "\"Moneda\":\"" + cuentaBancaria.string("Moneda") + "\"," + "\"NumeroCuenta\":\"" + cuentaBancaria.string("NumeroCuenta") + "\"}," + "\"Cuotapartista\":\"" + cuotapartista + "\"," + "\"FechaAcreditacion\":\"" + fechaAcreditacion + "\"," + "\"FechaConcertacion\":\"" + fechaConcertacion + "\"," + "\"FormasPagoCuentaBancaria\":{" + "\"CuentaBancaria\":\"" + formasPagoCuentaBancaria.string("CuentaBancaria") + "\"," + "\"FormaPago\":\"" + formasPagoCuentaBancaria.string("FormaPago") + "\"," + "\"Importe\":\"" + formasPagoCuentaBancaria.string("Importe") + "\"}," + "\"IDSolicitud\":\""
                + IDSolicitud + "\"," + "\"InversionFondo\":{" + "\"CondicionIngresoEgreso\":\"" + inversionFondo.string("CondicionIngresoEgreso") + "\"," + "\"Fondo\":\"" + inversionFondo.string("Fondo") + "\"," + "\"TipoValorCuotaParte\":\"" + inversionFondo.string("TipoValorCuotaParte") + "\"}," + "\"Moneda\":\"" + moneda + "\"," + "\"TpOrigenSol\":\"IE\"}}";
        return sRequest;

    }

    private static String getProximoDiaHabil(ContextoHB contexto) {
        ApiResponse responseCalendario = RestCatalogo.calendarioFechaActual(contexto);
        if (responseCalendario.hayError()) {
            return null;
        }
        String diaHabilPosterior = responseCalendario.objetos().get(0).string("diaHabilPosterior");
        return diaHabilPosterior;
    }

    /*
     * private static Respuesta programarOperacionFueraHorario(ContextoHB contexto,
     * String sRequest, String horarioInicio) {
     *
     * ApiResponse responseCalendario =
     * RestCatalogo.calendarioFechaActual(contexto); if
     * (responseCalendario.hayError()) { return null; } LocalTime horaActual =
     * LocalTime.now(); String horaInicio = horarioInicio.split(":")[0]; String
     * minutoInicio = horarioInicio.split(":")[1]; Boolean esDiaHabil =
     * Util.isDiaHabil(contexto); Boolean esAntesDeApertura =
     * horaActual.isBefore(LocalTime.parse(horarioInicio));
     *
     * if (esDiaHabil && esAntesDeApertura) { //Agendo para el dia de hoy al inicio
     * del horario! String anioACtual = String.valueOf(LocalDate.now().getYear());
     * String mesActual = String.valueOf(LocalDate.now().getMonth().getValue());
     * String diaActual = String.valueOf(LocalDate.now().getDayOfMonth());
     * RestScheduler.crearTareaFIC(contexto, anioACtual, diaActual, horaInicio,
     * mesActual, minutoInicio, "*", sRequest, "/v1/suscripcionAgenda"); } else {
     * //Agendo para el proximo dia habil String diaHabilPosterior =
     * responseCalendario.objetos().get(0).string("diaHabilPosterior"); String
     * anioPosterior = diaHabilPosterior.split("-")[0]; String mesPosterior =
     * diaHabilPosterior.split("-")[1]; String diaPosterior =
     * diaHabilPosterior.split("-")[2]; RestScheduler.crearTareaFIC(contexto,
     * anioPosterior, diaPosterior, horaInicio, mesPosterior, minutoInicio, "*",
     * sRequest, "/v1/suscripcionAgenda"); } return
     * Respuesta.estado("FUERA_DE_HORARIO"); }
     *
     * private static Respuesta programarRescateFueraHorario(ContextoHB contexto,
     * String sRequest, String horarioInicio) {
     *
     * ApiResponse responseCalendario =
     * RestCatalogo.calendarioFechaActual(contexto); if
     * (responseCalendario.hayError()) { return null; } LocalTime horaActual =
     * LocalTime.now(); String horaInicio = horarioInicio.split(":")[0]; String
     * minutoInicio = horarioInicio.split(":")[1]; Boolean esDiaHabil =
     * Util.isDiaHabil(contexto); Boolean esAntesDeApertura =
     * horaActual.isBefore(LocalTime.parse(horarioInicio));
     *
     * if (esDiaHabil && esAntesDeApertura) { //Agendo para el dia de hoy al inicio
     * del horario! String anioACtual = String.valueOf(LocalDate.now().getYear());
     * String mesActual = String.valueOf(LocalDate.now().getMonth().getValue());
     * String diaActual = String.valueOf(LocalDate.now().getDayOfMonth());
     * RestScheduler.crearTareaFIC(contexto, anioACtual, diaActual, horaInicio,
     * mesActual, minutoInicio, "*", sRequest, "/v1/rescateAgenda"); } else {
     * //Agendo para el proximo dia habil String diaHabilPosterior =
     * responseCalendario.objetos().get(0).string("diaHabilPosterior"); String
     * anioPosterior = diaHabilPosterior.split("-")[0]; String mesPosterior =
     * diaHabilPosterior.split("-")[1]; String diaPosterior =
     * diaHabilPosterior.split("-")[2]; RestScheduler.crearTareaFIC(contexto,
     * anioPosterior, diaPosterior, horaInicio, mesPosterior, minutoInicio, "*",
     * sRequest, "/v1/rescateAgenda"); } return
     * Respuesta.estado("FUERA_DE_HORARIO"); }
     *
     */

    public static Respuesta poseeCuotapartista(ContextoHB contexto) {
        Respuesta respuesta = new Respuesta();

        ApiResponse cuotapartistaResponse = RestInversiones.cuotapartista(contexto, null, HBInversion.tipoDocEsco(contexto.persona()), null, false, contexto.persona().esPersonaJuridica() ? contexto.persona().cuit() : contexto.persona().numeroDocumento());

        if (!cuotapartistaResponse.hayError() && !cuotapartistaResponse.string("mensajeAlUsuario").contains("No existe la Persona")) {
            respuesta.set("poseeCuentaCuotapartista", true);
        } else {
            if (cuotapartistaResponse.codigo == 504 || cuotapartistaResponse.codigo == 500) {
                return Respuesta.estado("OPERA_MANUAL");
            }

            respuesta.set("poseeCuentaCuotapartista", false);
        }

        return respuesta.ordenar("estado", "poseeCuentaCuotapartista");
    }

    private static String tipoDocEsco(Persona persona) {
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

    private static Boolean esFueraHorarioTablaParametria(Objeto rowFileTablaparametria) {
        Boolean resultado = false;
        LocalTime time = LocalTime.now();
        Boolean esAntes = time.isBefore(LocalTime.parse(rowFileTablaparametria.string("hora_inicio")));
        Boolean esDespues = time.isAfter(LocalTime.parse(rowFileTablaparametria.string("hora_fin")));
        if (esAntes || esDespues) {
            resultado = true;
        }
        return resultado;
    }

    private static String tipoDocPersonaESCO(String tipoDoc) {
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

    public static Respuesta evaluarPropuestaInversionPreTransferencia(ContextoHB contexto, CuentaTercero cuentaTercero) {
        Respuesta respuesta = new Respuesta();
        EnumPropuestasInversion propuesta = EnumPropuestasInversion.NINGUNA;
        try {
            boolean propuestasInversionBHHabilitadas = HBAplicacion
                    .funcionalidadPrendida(contexto.idCobis(), "hb_mostrar_propuestas_inversion") ;

            if (propuestasInversionBHHabilitadas && cuentaTercero != null) {
                if (cuentaTercero.esCvu() && cuentaTercero.mismoTitularColesa(contexto.persona().cuit())) {
                    propuesta = EnumPropuestasInversion.FONDO_MONEY_MARKET;
                } else if (cuentaTercero.esCuentaBroker()) {
                    propuesta = EnumPropuestasInversion.ACCIONES_BONOS;
                }

                if (propuesta.alguna()) {
                    SqlResponse response = Util.getContador(contexto, propuesta.getCodigoNemonicoPropuesta(), new Date());
                    boolean propuestoHoy = (!response.hayError && !response.registros.isEmpty());
                    if (propuestoHoy) {
                        propuesta = EnumPropuestasInversion.NINGUNA;
                    }
                }
            }

            respuesta.set("propuesta", propuesta);
        } catch (Exception e) {
            return Respuesta.error();
        }

        return respuesta;
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