package ar.com.hipotecario.canal.tas.modulos.plazosfijos.utils;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.modulos.plazosfijos.modelos.TASTasas;
import ar.com.hipotecario.canal.tas.modulos.plazosfijos.modelos.TASTasasResponse;
import ar.com.hipotecario.canal.tas.modulos.plazosfijos.modelos.TASTiposPF;
import ar.com.hipotecario.canal.tas.modulos.plazosfijos.services.TASRestPlazosFijos;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.catalogo.servicios.TASRestCatalogo;
import ar.com.hipotecario.canal.tas.shared.utils.models.enums.TASCodigoMonedaEnum;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class UtilesPlazosFijos {

    public static List<TASTasas> getTasasPF(ContextoTAS contexto, String idCliente){
        try {
            int secuencial = 0;
            Objeto tasasResponse = TASRestPlazosFijos.getTasasByIdCliente(contexto, idCliente, String.valueOf(secuencial));
            if (tasasResponse.string("estado").equals("ERROR") || tasasResponse.integer("codigo") == 204)
                return null;
            int totalRegistros = Integer.valueOf(tasasResponse.objeto("respuesta").string("totalRegistros"));
            if(totalRegistros <= 0) return null;
            List<TASTasas> tasasList = armarListadoTasas(tasasResponse);
            secuencial = tasasList.size();
            while(secuencial < totalRegistros){
                Objeto tasasRespAux = TASRestPlazosFijos.getTasasByIdCliente(contexto, idCliente, String.valueOf(secuencial));
                tasasList.addAll(armarListadoTasas(tasasRespAux));
                secuencial += tasasRespAux.objeto("respuesta").objeto("tasas").objetos().size();
            }
            return tasasList;
        }catch (Exception e){
            LogTAS.error(contexto, e);
            return null;
        }
    }
    private static List<TASTasas>  armarListadoTasas(Objeto responseTasas){
        List<TASTasas> tasasList = new ArrayList<>();
        for (Objeto tasaResponse : responseTasas.objeto("respuesta").objeto("tasas").objetos()) {
            TASTasas tasa = tasaResponse.toClass(TASTasas.class);
            tasasList.add(tasa);
        }
        return tasasList;
    }

    public static Objeto discriminarTiposPF(List<TASTasas> tasasList){
        try {

            List<TASTiposPF> tiposUva = new ArrayList<>();
            List<TASTiposPF> tipos = new ArrayList<>();
            List<TASTasasResponse> tasas = new ArrayList<>();
            TASTiposPF tipoUva = new TASTiposPF();

            boolean tieneUva = false;
            int cant = 0;
            int cantTasas = 0;

            for(TASTasas tasa : tasasList){
                TASTiposPF tipo = new TASTiposPF();
                TASTasasResponse tasaH = new TASTasasResponse();
                String codigoMoneda = tasa.getIdMoneda().equals(80) ?
                        TASCodigoMonedaEnum.PESOS.getCodigoMoneda() : TASCodigoMonedaEnum.DOLARES.getCodigoMoneda();
                tasaH.setCodigoMoneda(codigoMoneda);
                tasaH.setMontoMaximo(tasa.getMontoMaximo());
                tasaH.setMontoMinimo(tasa.getMontoMinimo());
                tasaH.setOrden(tasa.getOrden());
                tasaH.setPlazoMaximo(tasa.getPlazoMaximo());
                tasaH.setPlazoMinimo(tasa.getPlazoMinimo());
                tasaH.setValorTasa(tasa.getValorTasa());
                tasaH.setIdTipoDeposito(tasa.getIdTipoDeposito());
                tipo.setCodigo(tasa.getIdTipoDeposito());
                String codigoTipo = tasa.getIdTipoDeposito();
                switch(codigoTipo){
                    case "0018", "0042", "0043":
                        tipo.setEsUva(true);
                        tieneUva = true;
                        tipoUva = tipo;
                        break;
                    case "0013","0010":
                        tipo.setEsEmpleado(true);
                        tipo.setEsUva(false);
                        break;
                    default:
                        tipo.setEsUva(false);
                        break;
                }
                Config config = new Config();
                String desc = config.string("tas_plazofijo_"+codigoTipo);
                if (desc != null){
                    desc = Integer.valueOf(tasa.getIdTipoDeposito()) <= 13 ? desc +" en "+ codigoMoneda : desc;
                    tasaH.setDescTipoDeposito(desc);
                    tipo.setDescripcion(desc);
                    tipo.setCodigoMoneda(codigoMoneda);
                }

                if(!tipos.contains(tipo) && !tipo.isEsUva() && noEsLogros(tipo)){
                    cant += 1;
                    tipos.add(tipo);
                }


                cantTasas = cantTasas + 1;
                tasas.add(tasaH);
                if(tieneUva && !tiposUva.contains(tipoUva)) tiposUva.add(tipoUva);
            }
            tipos.addAll(tiposUva);
            tipos = removerSinDescipcion(tipos);
            Objeto response = new Objeto();
            List<TASTiposPF> listaOrdenada = tipos.stream().sorted(Comparator.comparing(TASTiposPF :: getCodigo)).collect(Collectors.toList());
            response.set("tipos", listaOrdenada);
            response.set("tasas", tasas);
            response.set("cant", cant);
            response.set("cantTasas", cantTasas);// se puede volar y reemplazar por un size()??
            response.set("tiposUva", tiposUva.size());
            return response;
        }catch (Exception e){
            return null;
        }
    }
    private static boolean noEsLogros(TASTiposPF tipo){
        int codigo = Integer.valueOf(tipo.getCodigo());
        return codigo < 25 || codigo == 42 || codigo == 43;
    }

    private static List<TASTiposPF> removerSinDescipcion(List<TASTiposPF> tipos){
        List<TASTiposPF> tiposSinDesc = new ArrayList<>();
        for(TASTiposPF tipo : tipos){
            String desc = tipo.getCodigo();
            if(desc.equalsIgnoreCase("Descripción no disponible")){
                tiposSinDesc.add(tipo);
            }
        }
        tipos.removeAll(tiposSinDesc);
        return tipos;
    }

    public static Objeto verificarDiaHabil(ContextoTAS contexto, Integer plazo){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, plazo);
        String fecha = new Fecha(cal.getTime()).string("yyyy-MM-dd");
        Objeto catalogoResponse = TASRestCatalogo.getFechaCalendario(contexto, fecha);
        if(catalogoResponse.string("estado").equals("ERROR")) return catalogoResponse;
        int esDiaHabil = Integer.valueOf(catalogoResponse.objeto("respuesta").objetos().get(0).integer("esDiaHabil"));
        if(esDiaHabil == 1) return new Objeto().set("estado", "true");
        Fecha proximoDiaHabil = new Fecha(catalogoResponse.objeto("respuesta").objetos().get(0).string("diaHabilPosterior"),"yyyy-MM-dd");
        int nuevoPlazoCalculado = agregarDiasNuevoPlazo(new Fecha(fecha, "yyyy-MM-dd"), proximoDiaHabil, plazo);
        Objeto nuevoPlazo = new Objeto();
        nuevoPlazo.set("estado", "false");
        nuevoPlazo.set("nuevoPlazo", nuevoPlazoCalculado);
        return nuevoPlazo;
    }

    //FIX - 04/08/25 implemento libreria de Java para calcular cantidad de dias entre fechas
    private static int agregarDiasNuevoPlazo(Fecha fechaPlazo, Fecha proximoDiaHabil, int plazo){
//        int diaPlazo = fechaPlazo.dia();
//        int proximoDia = proximoDiaHabil.dia();
//        int nuevoPlazo = plazo + (proximoDia - diaPlazo);
        LocalDate fechaPlazoCalcular = LocalDate.of(fechaPlazo.año(), fechaPlazo.mes(), fechaPlazo.dia());
        LocalDate fechaProximoCalcular = LocalDate.of(proximoDiaHabil.año(),proximoDiaHabil.mes(),proximoDiaHabil.dia());
        long diferenciaDias = ChronoUnit.DAYS.between(fechaPlazoCalcular, fechaProximoCalcular);
        int nuevoPlazo = plazo + (int) diferenciaDias;
        return nuevoPlazo;
    }

    public static String stringMoneda(int moneda) {
        String monedaStr;
        switch (moneda) {
            case 80:
                monedaStr = "$";
                break;
            case 2:
                monedaStr = "USD";
                break;
            case 98:
                monedaStr = "EU";
                break;
            case 12:
                monedaStr = "R$";
                break;
            case 1:
                monedaStr = "£";
                break;
            case 19:
                monedaStr = "¥";
                break;
            case 5:
                monedaStr = "FR";
                break;
            case 99:
                monedaStr = "BONOS";
                break;
            case 88:
                monedaStr = "UVI";
                break;
            default:
                monedaStr = "Desconocido";
                break;
        }
        return monedaStr;
    }

    public static String stringEstado(String estado){
        String estadoStr = "";
        switch(estado){
            case "A":
                estadoStr = "Vigente";
            case "B":
                estadoStr = "Cancelad";
            case "C":
                estadoStr = "Finalizado";   
        }
        return estadoStr;
    }

}
