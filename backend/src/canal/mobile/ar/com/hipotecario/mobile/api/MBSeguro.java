package ar.com.hipotecario.mobile.api;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.negocio.seguro.Cobertura;
import ar.com.hipotecario.canal.homebanking.negocio.seguro.Enlatado;
import ar.com.hipotecario.canal.homebanking.negocio.seguro.Producto;
import ar.com.hipotecario.canal.homebanking.negocio.seguro.Response;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.servicio.RestSeguroMB;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MBSeguro {

  private static String enlatadoMascotasEmpleado = ConfigMB.string("enlatados_habilitados_mascotas_empleado");
  private static String enlatadoMascotasdCliente = ConfigMB.string("enlatados_habilitados_mascotas_cliente");
  private static String enlatadoMovilidadEmpleado = ConfigMB.string("enlatados_habilitados_movilidad_empleado");
  private static String enlatadoMovilidadCliente = ConfigMB.string("enlatados_habilitados_movilidad_cliente");
  private static final String RAMO_PRODUCTO_EMPLEADO = ConfigMB.string("ramo_producto_mascotas_empleado");
  private static final String RAMO_PRODUCTO_INDIVIDUAL = ConfigMB.string("ramo_producto_mascotas_individual");
  private static final String RAMO_PRODUCTO_EMPLEADO_SEGURO_MOVILIDAD = ConfigMB.string("ramo_producto_movilidad_empleado");
  private static final String RAMO_PRODUCTO_INDIVIDUAL_SEGURO_MOVILIDAD = ConfigMB.string("ramo_producto_movilidad_individual");


  private static final String RAMO_PRODUCTO_HOGAR_EMPLEADO = ConfigMB.string("ramo_producto_hogar_empleado");
  private static final String RAMO_PRODUCTO_HOGAR_CLIENTE = ConfigMB.string("ramo_producto_hogar_cliente");

  private static final String RAMO_PRODUCTO_BIENES_MOVILES_EMPLEADO = ConfigMB.string("ramo_producto_bienes_moviles_empleado");
  private static final String RAMO_PRODUCTO_BIENES_MOVILES_CLIENTE = ConfigMB.string("ramo_producto_bienes_moviles_cliente");
  private static String enlatadoBMEmpleadoBolso = ConfigMB.string("enlatados_habilitados_bienes_moviles_empleado_bolso");
  private static String enlatadoBMClienteBolso = ConfigMB.string("enlatados_habilitados_bienes_moviles_cliente_bolso");
  private static String enlatadoBMEmpleadoElectro = ConfigMB.string("enlatados_habilitados_bienes_moviles_empleado_electro");
  private static String enlatadoBMClienteElectro = ConfigMB.string("enlatados_habilitados_bienes_moviles_cliente_electro");
  private static String enlatadoBMEmpleadoFull = ConfigMB.string("enlatados_habilitados_bienes_moviles_empleado_full");
  private static String enlatadoBMClienteFull = ConfigMB.string("enlatados_habilitados_bienes_moviles_cliente_full");

    private static String enlatadoAPEmpleado = ConfigMB.string("enlatados_habilitados_ap_empleado");
    private static String enlatadoAPCliente = ConfigMB.string("enlatados_habilitados_ap_cliente");

    private static String enlatadoSaludSenior = ConfigMB.string("enlatados_habilitados_salud_senior");
    private static String enlatadoAPSenior = ConfigMB.string("enlatados_habilitados_ap_mayores");

    public static RespuestaMB obtenerProductos(ContextoMB contexto) {
        String cuit = contexto.persona().cuit();
        List<Objeto> productos = RestSeguroMB.productos(contexto, cuit);
        if (productos == null) {
            return RespuestaMB.error();
        }

        Objeto datos = new Objeto();
        Objeto producto = productos.get(0);
        String json = producto.string("result.getInfoPorductosVigente.body.listInfoProductosVigentes.infoProductoVigente");
        if (json != null && !json.isEmpty()) {
            Objeto arrayDatosAdicionales = Objeto.fromJson(json);
            for (Objeto datosAdicionales : arrayDatosAdicionales.objetos()) {

                ar.com.hipotecario.canal.homebanking.base.Objeto objeto = new ar.com.hipotecario.canal.homebanking.base.Objeto();
                objeto.set("producto", datosAdicionales.string("producto"));

                String ramoProducto = datosAdicionales.integer("ramo").toString() + "-" + datosAdicionales.string("producto");

                objeto.set("productoDesc", getProductoDesc(datosAdicionales.string("productoDesc"), ramoProducto));
                objeto.set("ramo", datosAdicionales.integer("ramo"));
                //objeto.set("mtPrima", formatearBigDecimal(datosAdicionales.bigDecimal("mtPrima"), "#,###.00"));
                objeto.set("mtPrima", Formateador.importeConVE(datosAdicionales.bigDecimal("mtPrima")));
                objeto.set("nroCuenta", datosAdicionales.string("nroCuenta"));
                objeto.set("nuPoliza", datosAdicionales.integer("nuPoliza"));
                objeto.set("medioPago", datosAdicionales.string("medioPago"));
                objeto.set("deOrigen", datosAdicionales.string("deOrigen"));
                objeto.set("feDesde", datosAdicionales.string("feDesde"));
                objeto.set("feHasta", datosAdicionales.string("feHasta"));
                objeto.set("certificado", datosAdicionales.string("certificado"));
                objeto.set("grupoCumulo", datosAdicionales.string("grupoCumulo"));
                objeto.set("sponsor", datosAdicionales.string("sponsor"));
                datos.add(objeto);

            }

        } else {
            return RespuestaMB.exito("productos", new Object[]{});
        }
        return RespuestaMB.exito("productos", datos);
    }

  private static Object getProductoDesc(String productoDesc, String ramoProducto) {
    return switch (ramoProducto) {
      case "9-222", "9-224" -> "Bienes Móviles";
      case "17-201", "17-202" -> "Compra Protegida";
      case "18-201", "18-202" -> "Accidentes Personales";
      case "9-401", "9-402" -> "Movilidad";
      case "21-002", "21-003" -> "Mascotas";
      case "18-217" -> "Accidentes Personales Senior";
      case "19-611", "19-612" -> "Vida";
      case "9-201", "9-202" -> "Robo en Cajero";
      case "2-201", "2-202" -> "Hogar";
      case "19-609" -> "Salud";
      default -> productoDesc;
    };

  }

  public static RespuestaMB obtenerOfertasMascotas(ContextoMB contexto) {
    List<String> codigosEnlatadosHabilitadosEmpleado = new ArrayList<>(Arrays.asList(enlatadoMascotasEmpleado.split("_")));
    List<String> codigosEnlatadosHabilitadosCliente = new ArrayList<>(Arrays.asList(enlatadoMascotasdCliente.split("_")));

    Boolean esEmpleado = contexto.persona().esEmpleado();
    String ramoProducto = esEmpleado ? RAMO_PRODUCTO_EMPLEADO : RAMO_PRODUCTO_INDIVIDUAL;

    String sessionId = contexto.parametros.string("sessionId");
    List<Objeto> response = RestSeguroMB.ofertas(contexto, sessionId);
    if (response == null) {
      return RespuestaMB.error();
    }

    Objeto ofertasCompleto = response.get(0);
    ar.com.hipotecario.canal.homebanking.base.Objeto ofertaFinal = new ar.com.hipotecario.canal.homebanking.base.Objeto();
    ObjectMapper mapper = new ObjectMapper();

    try {
      Response ofertas = mapper.readValue(ofertasCompleto.toString(), Response.class);
      List<Producto> productos = ofertas.getResult().getProductos();

      if (!productos.isEmpty()) {
        for (Producto producto : productos) {
          if (producto.getIdProductoBase().equals(ramoProducto)) {
            for (Enlatado enlatado : producto.getEnlatados()) {
              enlatado.setPremioFormateado(Formateador.importeConVE(enlatado.getPremio()));
              if (esEmpleado) {
                if (codigosEnlatadosHabilitadosEmpleado.contains(enlatado.getCodigoEnlatado())) {
                  for (Cobertura cobertura : enlatado.getCoberturas()) {
                    cobertura.setMontoCoberturaFormateado(Formateador.importeConVE(cobertura.getMontoCobertura()));
                  }
                  ofertaFinal.add("enlatados", enlatado);
                }
              } else {
                if (codigosEnlatadosHabilitadosCliente.contains(enlatado.getCodigoEnlatado())) {
                  for (Cobertura cobertura : enlatado.getCoberturas()) {
                    cobertura.setMontoCoberturaFormateado(Formateador.importeConVE(cobertura.getMontoCobertura()));
                  }
                  ofertaFinal.add("enlatados", enlatado);
                }
              }
            }
          }
        }
      }

    } catch (JsonProcessingException e) {
      return RespuestaMB.error();
    }

    return RespuestaMB.exito("ofertas", ofertaFinal);
  }

  public static RespuestaMB obtenerOfertasMovilidad(ContextoMB contexto) {
    List<String> codigosEnlatadosHabilitadosEmpleado = new ArrayList<>(Arrays.asList(enlatadoMovilidadEmpleado.split("_")));
    List<String> codigosEnlatadosHabilitadosCliente = new ArrayList<>(Arrays.asList(enlatadoMovilidadCliente.split("_")));

    Boolean esEmpleado = contexto.persona().esEmpleado();
    String ramoProducto = esEmpleado ? RAMO_PRODUCTO_EMPLEADO_SEGURO_MOVILIDAD : RAMO_PRODUCTO_INDIVIDUAL_SEGURO_MOVILIDAD;

    String sessionId = contexto.parametros.string("sessionId");

    List<Objeto> response = RestSeguroMB.ofertas(contexto, sessionId);
    if (response == null) {
      return RespuestaMB.error();
    }

    Objeto ofertasCompleto = response.get(0);
    ar.com.hipotecario.canal.homebanking.base.Objeto ofertaFinal = new ar.com.hipotecario.canal.homebanking.base.Objeto();
    ObjectMapper mapper = new ObjectMapper();

    try {
      Response ofertas = mapper.readValue(ofertasCompleto.toString(), Response.class);
      List<Producto> productos = ofertas.getResult().getProductos();

      if (!productos.isEmpty()) {
        for (Producto producto : productos) {
          if (producto.getIdProductoBase().equals(ramoProducto)) {
            for (Enlatado enlatado : producto.getEnlatados()) {
              enlatado.setPremioFormateado(Formateador.importeConVE(enlatado.getPremio()));
              if (esEmpleado) {
                if (codigosEnlatadosHabilitadosEmpleado.contains(enlatado.getCodigoEnlatado())) {
                  for (Cobertura cobertura : enlatado.getCoberturas()) {
                    cobertura.setMontoCoberturaFormateado(Formateador.importeConVE(cobertura.getMontoCobertura()));
                  }
                  ofertaFinal.add("enlatados", enlatado);
                }
              } else {
                enlatado.setPremio(enlatado.getPremio().setScale(2, RoundingMode.HALF_UP));
                if (codigosEnlatadosHabilitadosCliente.contains(enlatado.getCodigoEnlatado())) {
                  for (Cobertura cobertura : enlatado.getCoberturas()){
                    cobertura.setMontoCoberturaFormateado(Formateador.importeConVE(cobertura.getMontoCobertura()));
                  }
                  ofertaFinal.add("enlatados", enlatado);
                }
              }
            }
          }
        }
      }

    } catch (JsonProcessingException e) {
      return RespuestaMB.error();
    }

    return RespuestaMB.exito("ofertas", ofertaFinal);
  }

    public static RespuestaMB obtenerOfertasBienesMoviles(ContextoMB contexto) {
        String sessionId = contexto.parametros.string("sessionId");
        Boolean esEmpleado = contexto.persona().esEmpleado();
        String ramoProducto = esEmpleado ? RAMO_PRODUCTO_BIENES_MOVILES_EMPLEADO : RAMO_PRODUCTO_BIENES_MOVILES_CLIENTE;
        String tipo = contexto.parametros.string("tipo");


        List<Objeto> response = RestSeguroMB.ofertas( contexto, sessionId );
        if ( response == null ) { return RespuestaMB.error(); }

        Objeto producto = response.get(0);
        String json = producto.string("result.productos");
        Objeto datos = new Objeto();
        if (json != null && !json.isEmpty()) {
            ar.com.hipotecario.canal.homebanking.base.Objeto arrayOfertas = ar.com.hipotecario.canal.homebanking.base.Objeto.fromJson(json);
            for ( ar.com.hipotecario.canal.homebanking.base.Objeto oferta : arrayOfertas.objetos()) {

                if (oferta.string("idProductoBase").contains(ramoProducto)) {
                    datos.set("descripcionRamo", oferta.string("descripcionRamo"));
                    datos.set("descripcionProducto", oferta.string("descripcionProducto"));
                }

                String jsonEnlatado = oferta.string("enlatados");

                if( jsonEnlatado != null & !jsonEnlatado.isEmpty()) {
                    ar.com.hipotecario.canal.homebanking.base.Objeto arrayEnlatados = ar.com.hipotecario.canal.homebanking.base.Objeto.fromJson(jsonEnlatado);
                    for ( ar.com.hipotecario.canal.homebanking.base.Objeto item : arrayEnlatados.objetos()) {

                        switch (tipo) {
                            case "bolso":
                                if (esEmpleado) {
                                    List<String> idsEnlatadoBMEmpleadosBolso = new ArrayList<String>(Arrays.asList(enlatadoBMEmpleadoBolso.split("_")));
                                    if (item.string("idEnlatado").contains(ramoProducto) && idsEnlatadoBMEmpleadosBolso.contains(item.string("codigoEnlatado"))){
                                        BigDecimal premio = item.bigDecimal("premio");
                                        if (premio != null) {
                                            item.set("premio", premio.setScale(2, RoundingMode.HALF_UP));
                                            item.set("premioFormateado", Formateador.importeConVE(premio.setScale(2, RoundingMode.HALF_UP)));
                                        }
                                        String jsonCoberturas = item.string("coberturas");
                                        if( jsonCoberturas != null & !jsonCoberturas.isEmpty()) {
                                            ar.com.hipotecario.canal.homebanking.base.Objeto arrayCoberturas= ar.com.hipotecario.canal.homebanking.base.Objeto.fromJson(jsonCoberturas);
                                            ar.com.hipotecario.canal.homebanking.base.Objeto datosCoberturas = new ar.com.hipotecario.canal.homebanking.base.Objeto();
                                            for (ar.com.hipotecario.canal.homebanking.base.Objeto cobertura : arrayCoberturas.objetos()){
                                                cobertura.set("montoCobertura", cobertura.bigDecimal("montoCobertura").intValue());
                                                String descripcionLarga = cobertura.string("descripcionLarga");
                                                if (descripcionLarga != null & !descripcionLarga.isEmpty()) {
                                                    String primeraLetra = descripcionLarga.substring(0, 1); // Obtiene la primera letra
                                                    String restoTexto = descripcionLarga.substring(1).toLowerCase(); // Convierte el resto a minúsculas
                                                    descripcionLarga = primeraLetra + restoTexto;
                                                    descripcionLarga = descripcionLarga.replaceAll("1\\s+er", "1er");
                                                    descripcionLarga = descripcionLarga.replaceAll("2\\s+do", "2do");
                                                    cobertura.set("descripcionLarga", descripcionLarga);
                                                }
                                                cobertura.set("montoCoberturaFormateado",Formateador.importeConVE(cobertura.bigDecimal("montoCobertura")));
                                                datosCoberturas.add(cobertura);
                                            }
                                            item.set("coberturas", datosCoberturas);
                                            datos.add("enlatados", item);
                                        }
                                    }
                                } else {
                                    List<String> idsEnlatadoBMClientesBolso = new ArrayList<String>(Arrays.asList(enlatadoBMClienteBolso.split("_")));
                                    if (item.string("idEnlatado").contains(ramoProducto) && idsEnlatadoBMClientesBolso.contains(item.string("codigoEnlatado"))){
                                        BigDecimal premio = item.bigDecimal("premio");
                                        if (premio != null) {
                                            item.set("premio", premio.setScale(2, RoundingMode.HALF_UP));
                                            item.set("premioFormateado", Formateador.importeConVE(premio.setScale(2, RoundingMode.HALF_UP)));
                                        }
                                        String jsonCoberturas = item.string("coberturas");
                                        if( jsonCoberturas != null & !jsonCoberturas.isEmpty()) {
                                            ar.com.hipotecario.canal.homebanking.base.Objeto arrayCoberturas= ar.com.hipotecario.canal.homebanking.base.Objeto.fromJson(jsonCoberturas);
                                            ar.com.hipotecario.canal.homebanking.base.Objeto datosCoberturas = new ar.com.hipotecario.canal.homebanking.base.Objeto();
                                            for (ar.com.hipotecario.canal.homebanking.base.Objeto cobertura : arrayCoberturas.objetos()){
                                                cobertura.set("montoCobertura", cobertura.bigDecimal("montoCobertura").intValue());
                                                String descripcionLarga = cobertura.string("descripcionLarga");
                                                if (descripcionLarga != null & !descripcionLarga.isEmpty()) {
                                                    String primeraLetra = descripcionLarga.substring(0, 1); // Obtiene la primera letra
                                                    String restoTexto = descripcionLarga.substring(1).toLowerCase(); // Convierte el resto a minúsculas
                                                    descripcionLarga = primeraLetra + restoTexto;
                                                    descripcionLarga = descripcionLarga.replaceAll("1\\s+er", "1er");
                                                    descripcionLarga = descripcionLarga.replaceAll("2\\s+do", "2do");
                                                    cobertura.set("descripcionLarga", descripcionLarga);
                                                }
                                                cobertura.set("montoCoberturaFormateado",Formateador.importeConVE(cobertura.bigDecimal("montoCobertura")));

                                                if (cobertura.string("codigoCobertura").equals("010") || cobertura.string("codigoCobertura").equals("009")) {
                                                    datosCoberturas.add(cobertura);
                                                }
                                            }
                                            item.set("coberturas", datosCoberturas);
                                            datos.add("enlatados", item);
                                        }

                                    }
                                }
                                break;
                            case "electro":
                                if (esEmpleado) {
                                    List<String> idsEnlatadoBMEmpleadosElectro = new ArrayList<String>(Arrays.asList(enlatadoBMEmpleadoElectro.split("_")));

                                    if (item.string("idEnlatado").contains(ramoProducto) && idsEnlatadoBMEmpleadosElectro.contains(item.string("codigoEnlatado"))){
                                        BigDecimal premio = item.bigDecimal("premio");
                                        if (premio != null) {
                                            item.set("premio", premio.setScale(2, RoundingMode.HALF_UP));
                                            item.set("premioFormateado", Formateador.importeConVE(premio.setScale(2, RoundingMode.HALF_UP)));
                                        }
                                        String jsonCoberturas = item.string("coberturas");
                                        if( jsonCoberturas != null & !jsonCoberturas.isEmpty()) {
                                            ar.com.hipotecario.canal.homebanking.base.Objeto arrayCoberturas= ar.com.hipotecario.canal.homebanking.base.Objeto.fromJson(jsonCoberturas);
                                            ar.com.hipotecario.canal.homebanking.base.Objeto datosCoberturas = new ar.com.hipotecario.canal.homebanking.base.Objeto();
                                            for (ar.com.hipotecario.canal.homebanking.base.Objeto cobertura : arrayCoberturas.objetos()){
                                                cobertura.set("montoCobertura", cobertura.bigDecimal("montoCobertura").intValue());
                                                String descripcionLarga = cobertura.string("descripcionLarga");
                                                if (descripcionLarga != null & !descripcionLarga.isEmpty()) {
                                                    String primeraLetra = descripcionLarga.substring(0, 1); // Obtiene la primera letra
                                                    String restoTexto = descripcionLarga.substring(1).toLowerCase(); // Convierte el resto a minúsculas
                                                    descripcionLarga = primeraLetra + restoTexto;
                                                    descripcionLarga = descripcionLarga.replaceAll("1\\s+er", "1er");
                                                    descripcionLarga = descripcionLarga.replaceAll("2\\s+do", "2do");
                                                    cobertura.set("descripcionLarga", descripcionLarga);
                                                }
                                                cobertura.set("montoCoberturaFormateado",Formateador.importeConVE(cobertura.bigDecimal("montoCobertura")));
                                                datosCoberturas.add(cobertura);
                                            }
                                            item.set("coberturas", datosCoberturas);
                                            datos.add("enlatados", item);
                                        }
                                    }

                                } else {
                                    List<String> idsEnlatadoBMClientesElectro = new ArrayList<String>(Arrays.asList(enlatadoBMClienteElectro.split("_")));
                                    if (item.string("idEnlatado").contains(ramoProducto) && idsEnlatadoBMClientesElectro.contains(item.string("codigoEnlatado"))){
                                        BigDecimal premio = item.bigDecimal("premio");
                                        if (premio != null) {
                                            item.set("premio", premio.setScale(2, RoundingMode.HALF_UP));
                                            item.set("premioFormateado", Formateador.importeConVE(premio.setScale(2, RoundingMode.HALF_UP)));
                                        }
                                        String jsonCoberturas = item.string("coberturas");
                                        if( jsonCoberturas != null & !jsonCoberturas.isEmpty()) {
                                            ar.com.hipotecario.canal.homebanking.base.Objeto arrayCoberturas= ar.com.hipotecario.canal.homebanking.base.Objeto.fromJson(jsonCoberturas);
                                            ar.com.hipotecario.canal.homebanking.base.Objeto datosCoberturas = new ar.com.hipotecario.canal.homebanking.base.Objeto();
                                            for (ar.com.hipotecario.canal.homebanking.base.Objeto cobertura : arrayCoberturas.objetos()){
                                                cobertura.set("montoCobertura", cobertura.bigDecimal("montoCobertura").intValue());
                                                String descripcionLarga = cobertura.string("descripcionLarga");
                                                if (descripcionLarga != null & !descripcionLarga.isEmpty()) {
                                                    String primeraLetra = descripcionLarga.substring(0, 1); // Obtiene la primera letra
                                                    String restoTexto = descripcionLarga.substring(1).toLowerCase(); // Convierte el resto a minúsculas
                                                    descripcionLarga = primeraLetra + restoTexto;
                                                    descripcionLarga = descripcionLarga.replaceAll("1\\s+er", "1er");
                                                    descripcionLarga = descripcionLarga.replaceAll("2\\s+do", "2do");
                                                    cobertura.set("descripcionLarga", descripcionLarga);
                                                }
                                                cobertura.set("montoCoberturaFormateado",Formateador.importeConVE(cobertura.bigDecimal("montoCobertura")));
                                                datosCoberturas.add(cobertura);
                                            }
                                            item.set("coberturas", datosCoberturas);
                                            datos.add("enlatados", item);
                                        }
                                    }
                                }

                                break;
                            case "full":
                                if (esEmpleado) {
                                    List<String> idsEnlatadoBMEmpleadosFull = new ArrayList<String>(Arrays.asList(enlatadoBMEmpleadoFull.split("_")));
                                    if (item.string("idEnlatado").contains(ramoProducto) && idsEnlatadoBMEmpleadosFull.contains(item.string("codigoEnlatado"))){
                                        BigDecimal premio = item.bigDecimal("premio");
                                        if (premio != null) {
                                            item.set("premio", premio.setScale(2, RoundingMode.HALF_UP));
                                            item.set("premioFormateado", Formateador.importeConVE(premio.setScale(2, RoundingMode.HALF_UP)));
                                        }
                                        String jsonCoberturas = item.string("coberturas");
                                        if( jsonCoberturas != null & !jsonCoberturas.isEmpty()) {
                                            ar.com.hipotecario.canal.homebanking.base.Objeto arrayCoberturas= ar.com.hipotecario.canal.homebanking.base.Objeto.fromJson(jsonCoberturas);
                                            ar.com.hipotecario.canal.homebanking.base.Objeto datosCoberturas = new ar.com.hipotecario.canal.homebanking.base.Objeto();
                                            for (ar.com.hipotecario.canal.homebanking.base.Objeto cobertura : arrayCoberturas.objetos()){
                                                cobertura.set("montoCobertura", cobertura.bigDecimal("montoCobertura").intValue());
                                                String descripcionLarga = cobertura.string("descripcionLarga");
                                                if (descripcionLarga != null & !descripcionLarga.isEmpty()) {
                                                    String primeraLetra = descripcionLarga.substring(0, 1); // Obtiene la primera letra
                                                    String restoTexto = descripcionLarga.substring(1).toLowerCase(); // Convierte el resto a minúsculas
                                                    descripcionLarga = primeraLetra + restoTexto;
                                                    descripcionLarga = descripcionLarga.replaceAll("1\\s+er", "1er");
                                                    descripcionLarga = descripcionLarga.replaceAll("2\\s+do", "2do");
                                                    cobertura.set("descripcionLarga", descripcionLarga);
                                                }
                                                cobertura.set("montoCoberturaFormateado",Formateador.importeConVE(cobertura.bigDecimal("montoCobertura")));
                                                datosCoberturas.add(cobertura);
                                            }
                                            item.set("coberturas", datosCoberturas);
                                            datos.add("enlatados", item);
                                        }
                                    }
                                } else {
                                    List<String> idsEnlatadoBMClientesFull = new ArrayList<String>(Arrays.asList(enlatadoBMClienteFull.split("_")));
                                    if (item.string("idEnlatado").contains(ramoProducto) && idsEnlatadoBMClientesFull.contains(item.string("codigoEnlatado"))){
                                        BigDecimal premio = item.bigDecimal("premio");
                                        if (premio != null) {
                                            item.set("premio", premio.setScale(2, RoundingMode.HALF_UP));
                                            item.set("premioFormateado", Formateador.importeConVE(premio.setScale(2, RoundingMode.HALF_UP)));
                                        }
                                        String jsonCoberturas = item.string("coberturas");
                                        if( jsonCoberturas != null & !jsonCoberturas.isEmpty()) {
                                            ar.com.hipotecario.canal.homebanking.base.Objeto arrayCoberturas= ar.com.hipotecario.canal.homebanking.base.Objeto.fromJson(jsonCoberturas);
                                            ar.com.hipotecario.canal.homebanking.base.Objeto datosCoberturas = new ar.com.hipotecario.canal.homebanking.base.Objeto();
                                            for (ar.com.hipotecario.canal.homebanking.base.Objeto cobertura : arrayCoberturas.objetos()){
                                                cobertura.set("montoCobertura", cobertura.bigDecimal("montoCobertura").intValue());
                                                String descripcionLarga = cobertura.string("descripcionLarga");
                                                if (descripcionLarga != null & !descripcionLarga.isEmpty()) {
                                                    String primeraLetra = descripcionLarga.substring(0, 1); // Obtiene la primera letra
                                                    String restoTexto = descripcionLarga.substring(1).toLowerCase(); // Convierte el resto a minúsculas
                                                    descripcionLarga = primeraLetra + restoTexto;
                                                    descripcionLarga = descripcionLarga.replaceAll("1\\s+er", "1er");
                                                    descripcionLarga = descripcionLarga.replaceAll("2\\s+do", "2do");
                                                    cobertura.set("descripcionLarga", descripcionLarga);
                                                }
                                                cobertura.set("montoCoberturaFormateado",Formateador.importeConVE(cobertura.bigDecimal("montoCobertura")));
                                                datosCoberturas.add(cobertura);
                                            }
                                            item.set("coberturas", datosCoberturas);
                                            datos.add("enlatados", item);
                                        }
                                    }

                                }
                                break;
                            default:
                                datos.add("", item);
                        }

                    }
                }
            }
        }

        return RespuestaMB.exito( "ofertas", datos );
    }

    public static RespuestaMB obtenerOfertasAP(ContextoMB contexto) {
        String sessionId = contexto.parametros.string("sessionId");
        Boolean esEmpleado = contexto.persona().esEmpleado();
        String ramoProducto = esEmpleado ? "18-202" : "18-201";
        List<String> idsEnlatados = esEmpleado ?
                new ArrayList<String>(Arrays.asList(enlatadoAPEmpleado.split("_")))
                : new ArrayList<String>(Arrays.asList(enlatadoAPCliente.split("_")));

        List<Objeto> ofertas = RestSeguroMB.ofertas( contexto, sessionId );
        if ( ofertas == null ) { return RespuestaMB.error(); }

        ar.com.hipotecario.canal.homebanking.base.Objeto datos = new ar.com.hipotecario.canal.homebanking.base.Objeto();
        Objeto producto = ofertas.get(0);
        String json = producto.string("result.productos");
        if (json != null && !json.isEmpty()) {

            ar.com.hipotecario.canal.homebanking.base.Objeto arrayOfertas = ar.com.hipotecario.canal.homebanking.base.Objeto.fromJson(json);
            for (ar.com.hipotecario.canal.homebanking.base.Objeto oferta : arrayOfertas.objetos()) {
                if (oferta.string("idProductoBase").contains(ramoProducto)) {
                    datos.set("descripcionRamo", oferta.string("descripcionRamo"));
                    datos.set("descripcionProducto", oferta.string("descripcionProducto"));
                }
                String jsonEnlatado = oferta.string("enlatados");

                if (jsonEnlatado != null & !jsonEnlatado.isEmpty()) {
                    ar.com.hipotecario.canal.homebanking.base.Objeto arrayEnlatados = ar.com.hipotecario.canal.homebanking.base.Objeto.fromJson(jsonEnlatado);
                    for (ar.com.hipotecario.canal.homebanking.base.Objeto item : arrayEnlatados.objetos()) {

                        if (esEmpleado
                                && item.string("idEnlatado").contains("EMP")
                                && item.string("idEnlatado").contains("18-202")
                                && item.string("idEnlatado").contains(ramoProducto)
                                && idsEnlatados.contains(item.string("codigoEnlatado"))
                        ) {
                            BigDecimal premio = item.bigDecimal("premio");
                            if (premio != null) {
                                item.set("premio", premio.setScale(2, RoundingMode.HALF_UP));
                                item.set("premioFormateado", Formateador.importeConVE(premio.setScale(2, RoundingMode.HALF_UP)));
                            }

                            String jsonCoberturas = item.string("coberturas");
                            if( jsonCoberturas != null & !jsonCoberturas.isEmpty()) {
                                ar.com.hipotecario.canal.homebanking.base.Objeto arrayCoberturas= ar.com.hipotecario.canal.homebanking.base.Objeto.fromJson(jsonCoberturas);
                                ar.com.hipotecario.canal.homebanking.base.Objeto datosCoberturas = new ar.com.hipotecario.canal.homebanking.base.Objeto();
                                for (ar.com.hipotecario.canal.homebanking.base.Objeto cobertura : arrayCoberturas.objetos()){

                                    String descripcionLarga = cobertura.string("descripcionLarga");
                                    if (descripcionLarga != null & !descripcionLarga.isEmpty()) {
                                        String primeraLetra = descripcionLarga.substring(0, 1); // Obtiene la primera letra
                                        String restoTexto = descripcionLarga.substring(1).toLowerCase(); // Convierte el resto a minúsculas
                                        cobertura.set("descripcionLarga", primeraLetra + restoTexto);
                                    }

                                    cobertura.set("montoCobertura", cobertura.bigDecimal("montoCobertura").intValue());
                                    cobertura.set("montoCoberturaFormateado",Formateador.importeConVE(cobertura.bigDecimal("montoCobertura")));

                                    if (cobertura.string("codigoCobertura").equals("003")) {
                                        BigDecimal montoCobertura = cobertura.bigDecimal("montoCobertura");
                                        BigDecimal rentaDiaria = montoCobertura.divide(new BigDecimal("365"), 2, RoundingMode.HALF_UP);
                                        Integer rentaDiariaInt = rentaDiaria.intValue();
                                        //itemCobertura.set("montoCobertura", rentaDiariaInt);
                                        cobertura.set("rentaDiaria", rentaDiariaInt);
                                    }

                                    datosCoberturas.add(cobertura);
                                }
                                item.set("coberturas", datosCoberturas);
                            }
                            datos.add("enlatados", item);
                        } else if (!esEmpleado && item.string("idEnlatado").contains("BH")
                                && idsEnlatados.contains(item.string("codigoEnlatado"))
                                && item.string("idEnlatado").contains("18-201")
                                && item.string("idEnlatado").contains(ramoProducto)
                        ) {
                            BigDecimal premio = item.bigDecimal("premio");
                            if (premio != null) {
                                item.set("premio", premio.setScale(2, RoundingMode.HALF_UP));
                                item.set("premioFormateado", Formateador.importeConVE(premio.setScale(2, RoundingMode.HALF_UP)));
                            }

                            String jsonCoberturas = item.string("coberturas");
                            if( jsonCoberturas != null & !jsonCoberturas.isEmpty()) {
                                ar.com.hipotecario.canal.homebanking.base.Objeto arrayCoberturas= ar.com.hipotecario.canal.homebanking.base.Objeto.fromJson(jsonCoberturas);
                                ar.com.hipotecario.canal.homebanking.base.Objeto datosCoberturas = new ar.com.hipotecario.canal.homebanking.base.Objeto();
                                for (ar.com.hipotecario.canal.homebanking.base.Objeto cobertura : arrayCoberturas.objetos()){

                                    String descripcionLarga = cobertura.string("descripcionLarga");
                                    if (descripcionLarga != null & !descripcionLarga.isEmpty()) {
                                        String primeraLetra = descripcionLarga.substring(0, 1); // Obtiene la primera letra
                                        String restoTexto = descripcionLarga.substring(1).toLowerCase(); // Convierte el resto a minúsculas
                                        cobertura.set("descripcionLarga", primeraLetra + restoTexto);
                                    }

                                    cobertura.set("montoCobertura", cobertura.bigDecimal("montoCobertura").intValue());
                                    cobertura.set("montoCoberturaFormateado",Formateador.importeConVE(cobertura.bigDecimal("montoCobertura")));

                                    if (cobertura.string("codigoCobertura").equals("003")) {
                                        BigDecimal montoCobertura = cobertura.bigDecimal("montoCobertura");
                                        BigDecimal rentaDiaria = montoCobertura.divide(new BigDecimal("365"), 2, RoundingMode.HALF_UP);
                                        Integer rentaDiariaInt = rentaDiaria.intValue();
                                        //itemCobertura.set("montoCobertura", rentaDiariaInt);
                                        cobertura.set("rentaDiaria", rentaDiariaInt);
                                    }

                                    datosCoberturas.add(cobertura);
                                }
                                item.set("coberturas", datosCoberturas);
                            }
                            datos.add("enlatados", item);
                        }
                    }

                }

            }
        }

        return RespuestaMB.exito( "ofertas", datos );
    }

    public static RespuestaMB obtenerOfertasSaludSenior(ContextoMB contexto) {
        String sessionId = contexto.parametros.string("sessionId");
        String ramoProducto = "20-006";
        List<String> idsEnlatados = new ArrayList<String>(Arrays.asList(enlatadoSaludSenior.split("_")));

        List<Objeto> ofertas = RestSeguroMB.ofertas( contexto, sessionId );
        if ( ofertas == null ) { return RespuestaMB.error(); }

        ar.com.hipotecario.canal.homebanking.base.Objeto datos = new ar.com.hipotecario.canal.homebanking.base.Objeto();
        Objeto producto = ofertas.get(0);


        String json = producto.string("result.productos");
        if (json != null && !json.isEmpty()) {

            ar.com.hipotecario.canal.homebanking.base.Objeto arrayOfertas = ar.com.hipotecario.canal.homebanking.base.Objeto.fromJson(json);
            for (ar.com.hipotecario.canal.homebanking.base.Objeto oferta : arrayOfertas.objetos()) {
                if (oferta.string("idProductoBase").contains(ramoProducto)) {
                    datos.set("descripcionRamo", oferta.string("descripcionRamo"));
                    datos.set("descripcionProducto", oferta.string("descripcionProducto"));
                }
                String jsonEnlatado = oferta.string("enlatados");

                if (jsonEnlatado != null & !jsonEnlatado.isEmpty()) {
                    ar.com.hipotecario.canal.homebanking.base.Objeto arrayEnlatados = ar.com.hipotecario.canal.homebanking.base.Objeto.fromJson(jsonEnlatado);
                    for (ar.com.hipotecario.canal.homebanking.base.Objeto item : arrayEnlatados.objetos()) {

                        if ( item.string("idEnlatado").contains(ramoProducto)
                                && idsEnlatados.contains(item.string("codigoEnlatado"))
                        ) {
                            BigDecimal premio = item.bigDecimal("premio");
                            if (premio != null) {
                                item.set("premio", premio.setScale(2, RoundingMode.HALF_UP));
                                item.set("premioFormateado", Formateador.importeConVE(premio.setScale(2, RoundingMode.HALF_UP)));
                            }

                            String jsonCoberturas = item.string("coberturas");
                            if( jsonCoberturas != null & !jsonCoberturas.isEmpty()) {
                                ar.com.hipotecario.canal.homebanking.base.Objeto arrayCoberturas= ar.com.hipotecario.canal.homebanking.base.Objeto.fromJson(jsonCoberturas);
                                ar.com.hipotecario.canal.homebanking.base.Objeto datosCoberturas = new ar.com.hipotecario.canal.homebanking.base.Objeto();
                                for (ar.com.hipotecario.canal.homebanking.base.Objeto cobertura : arrayCoberturas.objetos()){

                                    String descripcionLarga = cobertura.string("descripcionLarga");
                                    if (descripcionLarga != null & !descripcionLarga.isEmpty()) {
                                        String primeraLetra = descripcionLarga.substring(0, 1); // Obtiene la primera letra
                                        String restoTexto = descripcionLarga.substring(1).toLowerCase(); // Convierte el resto a minúsculas
                                        cobertura.set("descripcionLarga", primeraLetra + restoTexto);
                                    }

                                    BigDecimal rentaDiaria =  cobertura.bigDecimal("montoCobertura").divide(new BigDecimal("365"), 2, RoundingMode.HALF_UP);
                                    Integer rentaDiariaInt = rentaDiaria.intValue();
                                    cobertura.set("rentaDiaria", rentaDiariaInt);

                                    cobertura.set("montoCobertura", cobertura.bigDecimal("montoCobertura").intValue());
                                    cobertura.set("montoCoberturaFormateado",Formateador.importeConVE(cobertura.bigDecimal("montoCobertura")));


                                    datosCoberturas.add(cobertura);
                                }
                                item.set("coberturas", datosCoberturas);
                            }
                            datos.add("enlatados", item);
                        }
                    }

                }

            }
        }

        return RespuestaMB.exito( "ofertas", datos );
    }

    public static RespuestaMB obtenerOfertasAPSenior(ContextoMB contexto) {
        String sessionId = contexto.parametros.string("sessionId");
        String ramoProducto = "18-217";
        List<String> idsEnlatados = new ArrayList<String>(Arrays.asList(enlatadoAPSenior.split("_")));

        List<Objeto> ofertas = RestSeguroMB.ofertas( contexto, sessionId );
        if ( ofertas == null ) { return RespuestaMB.error(); }

        ar.com.hipotecario.canal.homebanking.base.Objeto datos = new ar.com.hipotecario.canal.homebanking.base.Objeto();
        Objeto producto = ofertas.get(0);


        String json = producto.string("result.productos");
        if (json != null && !json.isEmpty()) {

            ar.com.hipotecario.canal.homebanking.base.Objeto arrayOfertas = ar.com.hipotecario.canal.homebanking.base.Objeto.fromJson(json);
            for (ar.com.hipotecario.canal.homebanking.base.Objeto oferta : arrayOfertas.objetos()) {
                if (oferta.string("idProductoBase").contains(ramoProducto)) {
                    datos.set("descripcionRamo", oferta.string("descripcionRamo"));
                    datos.set("descripcionProducto", oferta.string("descripcionProducto"));
                }
                String jsonEnlatado = oferta.string("enlatados");

                if (jsonEnlatado != null & !jsonEnlatado.isEmpty()) {
                    ar.com.hipotecario.canal.homebanking.base.Objeto arrayEnlatados = ar.com.hipotecario.canal.homebanking.base.Objeto.fromJson(jsonEnlatado);
                    for (ar.com.hipotecario.canal.homebanking.base.Objeto item : arrayEnlatados.objetos()) {

                        if ( item.string("idEnlatado").contains(ramoProducto)
                                && idsEnlatados.contains(item.string("codigoEnlatado"))
                        ) {
                            BigDecimal premio = item.bigDecimal("premio");
                            if (premio != null) {
                                item.set("premio", premio.setScale(2, RoundingMode.HALF_UP));
                                item.set("premioFormateado", Formateador.importeConVE(premio.setScale(2, RoundingMode.HALF_UP)));
                            }

                            String jsonCoberturas = item.string("coberturas");
                            if( jsonCoberturas != null & !jsonCoberturas.isEmpty()) {
                                ar.com.hipotecario.canal.homebanking.base.Objeto arrayCoberturas= ar.com.hipotecario.canal.homebanking.base.Objeto.fromJson(jsonCoberturas);
                                ar.com.hipotecario.canal.homebanking.base.Objeto datosCoberturas = new ar.com.hipotecario.canal.homebanking.base.Objeto();
                                for (ar.com.hipotecario.canal.homebanking.base.Objeto cobertura : arrayCoberturas.objetos()){

                                    cobertura.set("montoCobertura", cobertura.bigDecimal("montoCobertura").intValue());
                                    cobertura.set("montoCoberturaFormateado",Formateador.importeConVE(cobertura.bigDecimal("montoCobertura")));

                                    datosCoberturas.add(cobertura);
                                }
                                item.set("coberturas", datosCoberturas);
                            }
                            datos.add("enlatados", item);
                        }
                    }

                }

            }
        }

        return RespuestaMB.exito( "ofertas", datos );
    }

    public static RespuestaMB obtenerOfertasHogar(ContextoMB contexto) {
    String sessionId = contexto.parametros.string("sessionId");
    Boolean esEmpleado = contexto.persona().esEmpleado();
    String tipo = esEmpleado ? "empleado" : "cliente";
    String ramoProducto = esEmpleado ? RAMO_PRODUCTO_HOGAR_EMPLEADO : RAMO_PRODUCTO_HOGAR_CLIENTE;
    String ambiente = contexto.parametros.string("ambiente");

    List<String> idsEnlatados = new ArrayList<String>(Arrays.asList(ConfigMB.string("enlatados_habilitados_hogar_ambiente_" + ambiente + "_" + tipo).split("_")));

    List<Objeto> ofertas = RestSeguroMB.ofertas(contexto, sessionId);
    if (ofertas == null) {
      return RespuestaMB.error();
    }

    ar.com.hipotecario.canal.homebanking.base.Objeto datos = new ar.com.hipotecario.canal.homebanking.base.Objeto();
    Objeto producto = ofertas.get(0);
    String json = producto.string("result.productos");
    if (json != null && !json.isEmpty()) {

      ar.com.hipotecario.canal.homebanking.base.Objeto arrayOfertas = ar.com.hipotecario.canal.homebanking.base.Objeto.fromJson(json);
      for (ar.com.hipotecario.canal.homebanking.base.Objeto oferta : arrayOfertas.objetos()) {
        if (oferta.string("idProductoBase").contains(ramoProducto)) {
          datos.set("descripcionRamo", oferta.string("descripcionRamo"));
          datos.set("descripcionProducto", oferta.string("descripcionProducto"));
        }
        String jsonEnlatado = oferta.string("enlatados");

        ar.com.hipotecario.canal.homebanking.base.Objeto arrayEnlatados = ar.com.hipotecario.canal.homebanking.base.Objeto.fromJson(jsonEnlatado);
        for (ar.com.hipotecario.canal.homebanking.base.Objeto item : arrayEnlatados.objetos()) {

          if (esEmpleado && item.string("idEnlatado").contains("EMP") && idsEnlatados.contains(item.string("codigoEnlatado"))
                  && item.string("idEnlatado").contains(ramoProducto)
          ) {
            BigDecimal premio = item.bigDecimal("premio");
            if (premio != null) {
              item.set("premio", premio.setScale(2, RoundingMode.HALF_UP));
              item.set("premioFormateado", Formateador.importeConVE(premio.setScale(2, RoundingMode.HALF_UP)));
            }

            String jsonCoberturas = item.string("coberturas");
            if( jsonCoberturas != null && !jsonCoberturas.isEmpty()) {
              ar.com.hipotecario.canal.homebanking.base.Objeto arrayCoberturas= ar.com.hipotecario.canal.homebanking.base.Objeto.fromJson(jsonCoberturas);
              ar.com.hipotecario.canal.homebanking.base.Objeto datosCoberturas = new ar.com.hipotecario.canal.homebanking.base.Objeto();
              for (ar.com.hipotecario.canal.homebanking.base.Objeto cobertura : arrayCoberturas.objetos()){
                String descripcionLarga = cobertura.string("descripcionLarga");
                if (descripcionLarga != null && !descripcionLarga.isEmpty()) {
                  String primeraLetra = descripcionLarga.substring(0, 1); // Obtiene la primera letra
                  String restoTexto = descripcionLarga.substring(1).toLowerCase(); // Convierte el resto a minúsculas
                  cobertura.set("descripcionLarga", primeraLetra + restoTexto);
                }
                cobertura.set("montoCobertura", cobertura.bigDecimal("montoCobertura").intValue());
                cobertura.set("porcentajeDeAumento", cobertura.bigDecimal("porcentajeDeAumento").intValue());
                cobertura.set("porcentajeDeDisminucion", cobertura.bigDecimal("porcentajeDeDisminucion").intValue());

                cobertura.set("montoCoberturaFormateado",Formateador.importeConVE(cobertura.bigDecimal("montoCobertura")));
                datosCoberturas.add(cobertura);
              }
              item.set("coberturas", datosCoberturas);
            }
            datos.add("enlatados", item);
          } else if (!esEmpleado && item.string("idEnlatado").contains("BH") && idsEnlatados.contains(item.string("codigoEnlatado"))
                  && item.string("idEnlatado").contains(ramoProducto)
          ) {
            BigDecimal premio = item.bigDecimal("premio");
            if (premio != null) {
              item.set("premio", premio.setScale(2, RoundingMode.HALF_UP));
              item.set("premioFormateado", Formateador.importeConVE(premio.setScale(2, RoundingMode.HALF_UP)));
            }


            String jsonCoberturas = item.string("coberturas");
            if( jsonCoberturas != null && !jsonCoberturas.isEmpty()) {
              ar.com.hipotecario.canal.homebanking.base.Objeto arrayCoberturas = ar.com.hipotecario.canal.homebanking.base.Objeto.fromJson(jsonCoberturas);
              ar.com.hipotecario.canal.homebanking.base.Objeto datosCoberturas = new ar.com.hipotecario.canal.homebanking.base.Objeto();
              for (ar.com.hipotecario.canal.homebanking.base.Objeto cobertura : arrayCoberturas.objetos()){
                String descripcionLarga = cobertura.string("descripcionLarga");
                if (descripcionLarga != null && !descripcionLarga.isEmpty()) {
                  String primeraLetra = descripcionLarga.substring(0, 1); // Obtiene la primera letra
                  String restoTexto = descripcionLarga.substring(1).toLowerCase(); // Convierte el resto a minúsculas
                  cobertura.set("descripcionLarga", primeraLetra + restoTexto);
                }
                cobertura.set("montoCobertura", cobertura.bigDecimal("montoCobertura").intValue());
                cobertura.set("porcentajeDeAumento", cobertura.bigDecimal("porcentajeDeAumento").intValue());
                cobertura.set("porcentajeDeDisminucion", cobertura.bigDecimal("porcentajeDeDisminucion").intValue());
                cobertura.set("montoCoberturaFormateado", Formateador.importeConVE(cobertura.bigDecimal("montoCobertura")));
                datosCoberturas.add(cobertura);
              }
              item.set("coberturas", datosCoberturas);
            }


            datos.add("enlatados", item);
          }
        }

      }
    }

    return RespuestaMB.exito( "ofertas", datos );
  }

    public static RespuestaMB obtenerToken(ContextoMB contexto) {
        Objeto response = RestSeguroMB.token(contexto);
        if (response == null)
            return RespuestaMB.error();
        Objeto datos = new Objeto();
        String json = response.string("result");
        if (json != null && !json.isEmpty()) {
            Objeto idSession = Objeto.fromJson(json);
            datos.set("id", idSession.string("sessionId"));

        }

        return RespuestaMB.exito("sesion", datos);
    }

    public static RespuestaMB insertarEmisionOnlineV2AP(ContextoMB contexto) {
        Objeto datos = new Objeto();
        if (contexto.sesion() != null)
            contexto.sesion().cobisCaido = (false);
        Boolean esCliente = !contexto.persona().esEmpleado();
        String ramoProducto = esCliente ? "18-201" : "18-202";
        String[] ramoProductoArray = ramoProducto.split("-");
        ApiRequestMB request = RestSeguroMB.getRequest(contexto,esCliente);
        request.body("ramo", ramoProductoArray[0]);
        request.body("producto", ramoProductoArray[1]);

        ApiResponseMB response = ApiMB.response(request);
        if (response.hayError() && !"101146".equals(response.string("codigo"))) {
            if ("Server fuera de linea".equals(response.string("mensajeAlUsuario")) && contexto.sesion() != null) {
                contexto.sesion().cobisCaido = (true);
            }
            return RespuestaMB.error();
        }

        String json = Objeto.fromJson(response.json).string("result");
        if (json != null && !json.isEmpty()) {

            new Futuro<>(() -> {
                enviarSalesforceSeguro(contexto, ramoProductoArray[0], ramoProductoArray[1]);
                return true;
            });

            datos.add( Objeto.fromJson(json) );
            return RespuestaMB.exito("respuesta", datos);

        }

        json = Objeto.fromJson(response.json).string("codigo");
        if(  json != null && json.equals("504") ) {
            String jsonString = "{ \"codigo\": \"" + json + "\" }";
            datos.add(Objeto.fromJson(jsonString) );
            return RespuestaMB.exito("respuesta", datos).setEstado("2");
        }

        return RespuestaMB.error();
  }

  public static Respuesta insertarEmisionOnlineV2( ContextoMB contexto ) {
    Objeto datos = new Objeto();
    Objeto response;


    response = RestSeguroMB.insertEmisionOnlineV2(contexto);
    if (response == null)
      return Respuesta.error();

    String result = response.string("result");
    if( result != null && !result.isEmpty() ) {
      new Futuro<>(() -> {
          Boolean esCliente = !contexto.persona().esEmpleado();
          String ramoProducto = esCliente ? RAMO_PRODUCTO_INDIVIDUAL : RAMO_PRODUCTO_EMPLEADO;
          String[] ramoProductoArray = ramoProducto.split("-");
          enviarSalesforceSeguro(contexto, ramoProductoArray[0], ramoProductoArray[1]);
        return true;
      });

      datos.add( Objeto.fromJson(result) );
      return Respuesta.exito("respuesta", datos);
    }

    result = response.string("codigo");
    if(  result != null && result.equals("504") ) {
      String jsonString = "{ \"codigo\": \"" + result + "\" }";
      datos.add( Objeto.fromJson(jsonString) );
      return Respuesta.exito("respuesta", datos).setEstado("2");
    }

    return Respuesta.error();
  }

  //SALESFORCE ALTA SEGURO
  private static void enviarSalesforceSeguro(ContextoMB contexto, String ramo, String producto) {
    if (MBSalesforce.prendidoSalesforceAmbienteBajoConFF(contexto)) {
      Objeto parametros = new Objeto();
      parametros.set("IDCOBIS", contexto.idCobis());
      parametros.set("NOMBRE", contexto.persona().nombre());
      parametros.set("APELLIDO", contexto.persona().apellido());
      parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));

      Map<String, String> seguro = Map.ofEntries(
              Map.entry("2-202", "HOGAR (EMPLEADO)"),
              Map.entry("2-201", "HOGAR"),
              Map.entry("9-222", "BIENES MÓVILES (EMPLEADO)"),
              Map.entry("9-224", "BIENES MÓVILES"),
              Map.entry("9-202", "ROBO EN CAJERO (EMPLEADO)"),
              Map.entry("9-201", "ROBO EN CAJERO"),
              Map.entry("9-402", "MOVILIDAD (EMPLEADO)"),
              Map.entry("9-401", "MOVILIDAD"),
              Map.entry("17-202", "COMPRA PROTEGIDA (EMPLEADO)"),
              Map.entry("17-201", "COMPRA PROTEGIDA"),
              Map.entry("18-202", "ACCIDENTE PERSONAL (EMPLEADO)"),
              Map.entry("18-201", "ACCIDENTE PERSONAL"),
              Map.entry("18-217", "ACCIDENTE PERSONAL SENIOR"),
              Map.entry("19-612", "VIDA (EMPLEADO)"),
              Map.entry("19-611", "VIDA"),
              Map.entry("19-609", "SALUD"),
              Map.entry("21-003", "MASCOTA (EMPLEADO)"),
              Map.entry("21-002", "MASCOTA")
      );

      String ramoProducto = ramo.concat("-").concat(producto);
      String tipoBien = "";
      String nombreBien = "";
      parametros.set("NOMBRE_SEGURO", seguro.get(ramoProducto));
      if(ramo.equals("2")) {
        String domicilio = contexto.parametros.string("emiDeCalleEmi") +
                " " + contexto.parametros.string("emiDeCalleEmi") + ",";
        if(contexto.parametros.string("emiDePisoEmi") != null)
          domicilio += " " + contexto.parametros.string("emiDePisoEmi");
        if(contexto.parametros.string("emiDeDtoEmi") != null)
          domicilio += " " + contexto.parametros.string("emiDeDtoEmi");

        domicilio += " " + contexto.parametros.string("emiDeDesLocalidadEmi");
        parametros.set("DOMICILIO_DECLARADO", domicilio);
        tipoBien = contexto.parametros.string("tipoVivienda");

        nombreBien = contexto.parametros.string("tipoVivienda").equals("1") ?
                "Barrio Privado" : contexto.parametros.string("tipoVivienda").equals("2") ? "Casa" : "Departamento";
        
      }
      //Necesita desarrollo front
      parametros.set("CANTIDAD_AMBIENTES", "");

      if(ramo.equals("21")) {
        String tipoMascota = contexto.parametros.string("tipoMascota").equals("1") ? "Perro" : "Gato";
        tipoBien = contexto.parametros.string("tipoMascota");
        parametros.set("TIPO_MASCOTA",  tipoMascota);
        parametros.set("RAZA",  contexto.parametros.string("raza", ""));
        parametros.set("NOMBRE_MASCOTA",  contexto.parametros.string("nombre", ""));
      }

      if(ramo.equals("9")) {
        if(producto.equals("401") || producto.equals("402")) {
          nombreBien = contexto.parametros.string("tipoMovilidad").equals("1") ? "Bicicleta" : "Monopatín";
          tipoBien = contexto.parametros.string("tipoMovilidad");
        }

        parametros.set("MARCA",  contexto.parametros.string("marca", ""));
        parametros.set("MODELO", contexto.parametros.string("modelo", ""));
      }

      if(ramo.equals("2"))
    	  parametros.set("PLAN_ID", "002");
      else
    	  parametros.set("PLAN_ID", "001");

      
      parametros.set("NOMBRE_BIEN", nombreBien);
      parametros.set("TIPO_BIEN_ASEGURADO", tipoBien);
      
      parametros.set("PLAN_MONTO", contexto.parametros.string("premioOrigen", ""));

      Date hoy = new Date();
      parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
      parametros.set("HORA", new SimpleDateFormat("hh:mm").format(hoy));
      MBSalesforce.registrarEventoSalesforce(contexto, ConfigMB.string("salesforce_alta_seguros"), parametros);
    }

  }

  public static RespuestaMB insertarEmisionOnlineV2Movilidad(ContextoMB contexto) {
    Objeto datos = new Objeto();
    if (contexto.sesion() != null)
      contexto.sesion().cobisCaido = (false);
    Boolean esCliente = !contexto.persona().esEmpleado();
    String ramoProducto = esCliente ? RAMO_PRODUCTO_INDIVIDUAL_SEGURO_MOVILIDAD : RAMO_PRODUCTO_EMPLEADO_SEGURO_MOVILIDAD;
    String[] ramoProductoArray = ramoProducto.split("-");
    ApiRequestMB request = RestSeguroMB.getRequest(contexto,esCliente);

    request.body("emiPregunta1Emi", "S");
    request.body("emiPregunta2Emi", "S");
    request.body("emiPlanEmi", "001");
    request.body("ramo", ramoProductoArray[0]);
    request.body("producto", ramoProductoArray[1]);

    ApiResponseMB response = ApiMB.response(request);
    if (response.hayError() && !"101146".equals(response.string("codigo"))) {
      if ("Server fuera de linea".equals(response.string("mensajeAlUsuario")) && contexto.sesion() != null) {
        contexto.sesion().cobisCaido = (true);
      }
      return RespuestaMB.error();
    }

    String json = Objeto.fromJson(response.json).string("result");
    if (json != null && !json.isEmpty()) {

      new Futuro<>(() -> {
        enviarSalesforceSeguro(contexto, ramoProductoArray[0], ramoProductoArray[1]);
        return true;
      });

      datos.add( Objeto.fromJson(json) );
      return RespuestaMB.exito("respuesta", datos);

    }

    json = Objeto.fromJson(response.json).string("codigo");
    if(  json != null && json.equals("504") ) {
      String jsonString = "{ \"codigo\": \"" + json + "\" }";
      datos.add(Objeto.fromJson(jsonString) );
      return RespuestaMB.exito("respuesta", datos).setEstado("2");
    }

    return RespuestaMB.error();

  }


  public static RespuestaMB insertarEmisionOnlineV2Hogar(ContextoMB contexto) {
    Objeto datos = new Objeto();
    if (contexto.sesion() != null)
      contexto.sesion().cobisCaido = (false);
    Boolean esCliente = !contexto.persona().esEmpleado();
    String ramoProducto = esCliente ? RAMO_PRODUCTO_HOGAR_CLIENTE : RAMO_PRODUCTO_HOGAR_EMPLEADO;
    String[] ramoProductoArray = ramoProducto.split("-");
    ApiRequestMB request = RestSeguroMB.getRequestHogar(contexto,esCliente);
    request.body("idProductor", "414");
    request.body("ramo", ramoProductoArray[0]);
    request.body("producto", ramoProductoArray[1]);

    ApiResponseMB response = ApiMB.response(request);
    if (response.hayError() && !"101146".equals(response.string("codigo"))) {
      if ("Server fuera de linea".equals(response.string("mensajeAlUsuario")) && contexto.sesion() != null) {
        contexto.sesion().cobisCaido = (true);
      }
      return RespuestaMB.error();
    }

    String json = Objeto.fromJson(response.json).string("result");
    if (json != null && !json.isEmpty()) {

      new Futuro<>(() -> {
        enviarSalesforceSeguro(contexto, ramoProductoArray[0], ramoProductoArray[1]);
        return true;
      });

      datos.add( Objeto.fromJson(json) );
      return RespuestaMB.exito("respuesta", datos);

    }

    json = Objeto.fromJson(response.json).string("codigo");
    if(  json != null && json.equals("504") ) {
      String jsonString = "{ \"codigo\": \"" + json + "\" }";
      datos.add(Objeto.fromJson(jsonString) );
      return RespuestaMB.exito("respuesta", datos).setEstado("2");
    }

    return RespuestaMB.error();

  }


  public static RespuestaMB insertarEmisionOnlineV2BienesMoviles(ContextoMB contexto) {
    Objeto datos = new Objeto();
    if (contexto.sesion() != null)
      contexto.sesion().cobisCaido = (false);
    Boolean esCliente = !contexto.persona().esEmpleado();
    String idEnlatado = contexto.parametros.string("idEnlatado");
    String[] idEnlatadoArray = idEnlatado.split("-");
    ApiRequestMB request = RestSeguroMB.getRequest(contexto,esCliente);

    request.body("emiPregunta1Emi", contexto.parametros.string("emiPregunta1Emi"));
    request.body("emiPregunta2Emi", contexto.parametros.string("emiPregunta2Emi"));
    request.body("emiPlanEmi", "001");
    request.body("ramo", idEnlatadoArray[0]);
    request.body("idProductor", "");
    request.body("producto", idEnlatadoArray[1]);

    ApiResponseMB response = ApiMB.response(request);
    if (response.hayError() && !"101146".equals(response.string("codigo"))) {
      if ("Server fuera de linea".equals(response.string("mensajeAlUsuario")) && contexto.sesion() != null) {
        contexto.sesion().cobisCaido = (true);
      }
      return RespuestaMB.error();
    }

    String json = Objeto.fromJson(response.json).string("result");
    if (json != null && !json.isEmpty()) {

      new Futuro<>(() -> {
        enviarSalesforceSeguro(contexto, idEnlatadoArray[0], idEnlatadoArray[1]);
        return true;
      });

      datos.add( Objeto.fromJson(json) );
      return RespuestaMB.exito("respuesta", datos);

    }

    json = Objeto.fromJson(response.json).string("codigo");
    if(  json != null && json.equals("504") ) {
      String jsonString = "{ \"codigo\": \"" + json + "\" }";
      datos.add( ar.com.hipotecario.canal.homebanking.base.Objeto.fromJson(jsonString) );
      return RespuestaMB.exito("respuesta", datos).setEstado("2");
    }

    return RespuestaMB.error();

  }

  public static String obtenerToken2(ContextoMB contexto) {
    Objeto response = RestSeguroMB.token(contexto);
    if (response == null)
      return null;
    Objeto datos = new Objeto();
    String json = response.string("result");
    if (json != null && !json.isEmpty()) {
      Objeto idSession = Objeto.fromJson(json);
      datos.set("id", idSession.string("sessionId"));
      return idSession.string("sessionId");
    }

    return null;
  }

  private static String formatearBigDecimal(Object monto, String formato) {
    DecimalFormat df = new DecimalFormat(formato);
    return df.format(monto);
  }
}
