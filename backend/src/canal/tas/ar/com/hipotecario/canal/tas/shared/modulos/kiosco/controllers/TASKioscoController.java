package ar.com.hipotecario.canal.tas.shared.modulos.kiosco.controllers;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.shared.modulos.kiosco.modelos.TASKiosco;
import ar.com.hipotecario.canal.tas.shared.modulos.kiosco.servicios.TASSqlKiosco;

public class TASKioscoController {

    public static Objeto getKiosco(ContextoTAS contexto) {
        try {
            Objeto estaEnContexto = TASSesionKioscoController.getSesionKioscoById(contexto);
            return estaEnContexto.isEmpty()
                    || estaEnContexto.string("estado").contains("SIN_RESULTADOS")
                    || estaEnContexto.string("estado").contains("SIN_PARAMETROS")
                            ? buscarTas(contexto)
                            : estaEnContexto;
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "TASKioscoController - getKiosco()", e);
        }
    }

    private static Objeto buscarTas(ContextoTAS contexto) {
            String tasIp = contexto.parametros.string("direccionIp", "");
            String tasId = contexto.parametros.string("tasId", "");
            if (!tasId.isEmpty())
                return getById(contexto, tasId);
            return (!tasIp.isEmpty()) ? getByIp(contexto, tasIp)
                    : RespuestaTAS.sinParametros(contexto, "Parametros no ingresados");
    }

    private static Objeto getById(ContextoTAS contexto, String tasId) {
        TASKiosco kiosco = TASSqlKiosco.completarDatosKioscoById(contexto, tasId);
        if (!kiosco.string("ERROR").isEmpty())
            return RespuestaTAS.error(contexto,"TASKioscoController - getById()", (Exception) kiosco.get("ERROR"));
        contexto.crearSesionKiosco(kiosco);
        return TASKiosco.armarModeloObjeto(kiosco);
    }

    private static Objeto getByIp(ContextoTAS contexto, String tasIp) {
        TASKiosco kiosco = TASSqlKiosco.completarDatosKioscoByIp(contexto, tasIp);
        if (!kiosco.string("ERROR").isEmpty())
            return RespuestaTAS.error(contexto,"TASKioscoController - getByIp()" ,(Exception) kiosco.get("ERROR"));
        contexto.crearSesionKiosco(kiosco);
        return TASKiosco.armarModeloObjeto(kiosco);
    }

    public static Objeto getFlagsKiosco(ContextoTAS contexto) {
        try {
            String tasIp = contexto.parametros.string("direccionIp");
            if (tasIp.isEmpty())
                return RespuestaTAS.sinParametros(contexto, "Uno o mas parametros no ingresados");
            Futuro<Objeto> futuroFlagsSQL = new Futuro<>(() -> TASSqlKiosco.obtenerFlagsKiosco(contexto, tasIp));
            Futuro<Objeto> futuroFlagsVariables = new Futuro<>(() -> getFlagsVariables());
            Objeto response = new Objeto();
            response.set("flag_sql", futuroFlagsSQL.get());
            response.set("flag_variables", futuroFlagsVariables.get());
            return response;
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "TASKioscoController - getFlagsKiosco()", e);
        }
    }

    // *! METODO FUNCIONAL PARA DESA Y HOMO... CORREGIR PARA PRODUCCION
    private static Objeto getFlagsVariables() {
        final Config config = new Config();
        Objeto flagVariables = new Objeto();
        flagVariables.set("flagDIC", !config.string("tas_flag_dic").equals("1") ? false : true);
        flagVariables.set("flagVentaCA", !config.string("tas_flag_venta_ca").equals("1") ? false : true);
        flagVariables.set("flagPaquete", !config.string("tas_flag_paquete").equals("1") ? false : true);
        flagVariables.set("flagCompraVentaUSD", !config.string("tas_flag_compra_venta_usd").equals("1") ? false : true);
        flagVariables.set("flagPFLogros", !config.string("tas_flag_plazo_fijo_logros").equals("1") ? false : true);
        flagVariables.set("flagCambioFormaPago",
                !config.string("tas_flag_cambio_forma_pago").equals("1") ? false : true);
        flagVariables.set("flagKioscoHorarioPagosYDepositos", true);
        return flagVariables;
    }
}