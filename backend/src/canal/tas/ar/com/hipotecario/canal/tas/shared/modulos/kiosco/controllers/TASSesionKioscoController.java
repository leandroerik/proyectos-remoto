package ar.com.hipotecario.canal.tas.shared.modulos.kiosco.controllers;

import java.util.Date;
import java.util.Map;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.auditor.controllers.TASApiAuditor;
import ar.com.hipotecario.canal.tas.shared.modulos.kiosco.modelos.TASKiosco;
import ar.com.hipotecario.canal.tas.shared.modulos.kiosco.modelos.TASKioscoEstadoOperativo;
import ar.com.hipotecario.canal.tas.shared.modulos.kiosco.servicios.TASSqlKiosco;
import ar.com.hipotecario.canal.tas.shared.utils.models.strings.TASMensajesString;

/**
 * TASSesionKioscoController
 */
public class TASSesionKioscoController {

  public static Objeto ordenIngresoKiosco(ContextoTAS contexto) {
    try {
      String ipKiosco = contexto.parametros.string("direccionIp");
      if (ipKiosco.isEmpty())
        return RespuestaTAS.sinParametros(contexto, "Uno o mas parametros no ingresados");
      TASKiosco kiosco = TASSqlKiosco.completarDatosKioscoByIp(contexto, ipKiosco);
      if (!kiosco.string(TASMensajesString.error()).isEmpty())
        return RespuestaTAS.error(contexto, "TASSesionKioscoController - completarDatosKioscoByIp()", (Exception) kiosco.get("ERROR"));
      Objeto reporte = TASApiAuditor.generarReporteReinicio(contexto, kiosco);
      kiosco.setReporte(reporte.string("estado").equals("OK") ? "true" : "false");
      ContextoTAS.crearSesionKiosco(kiosco);
      generarLogReinicio(contexto, kiosco);
      return armarRespuestaOrdenIngreso(contexto, kiosco);
    } catch (Exception e) {
      return RespuestaTAS.error(contexto, "[TASSesionKioscoController] - ordenIngresoKiosco()", e);
    }
  }

  private static void generarLogReinicio(ContextoTAS contexto, TASKiosco kiosco){
    Objeto datos = new Objeto();
    contexto.sesion().setIdTas(kiosco.getKioscoId().toString());
    datos.set("kioscoId", kiosco.getKioscoId());
    datos.set("kioscoIp", kiosco.getKioscoIp());
    datos.set("hora_inicio", kiosco.getHoraInicio());
    datos.set("hora_fin", kiosco.getHoraFin());
    LogTAS.evento(contexto, "REINICIO_KIOSCO", datos);
  }

  private static Objeto armarRespuestaOrdenIngreso(ContextoTAS contexto, TASKiosco kiosco) {
    try {
      Objeto objeto = new Objeto();
      objeto.setRaw("kioscoId", kiosco.getKioscoId());
      objeto.setRaw("Kiosco.terminal", kiosco.getKioscoNombre());
      objeto.setRaw("Kiosco.agency", kiosco.getSucursalId());
      objeto.setRaw("Kiosco.agency.name", kiosco.getKioscoDescripcion());
      objeto.setRaw("Kiosco.name", kiosco.getKioscoNombre());
      objeto.setRaw("Kiosco.IP", kiosco.getKioscoIp());
      objeto.setRaw("Address.agency", kiosco.getUbicacionId());
      objeto.setRaw("Address.agency.name", kiosco.getNombreUbicacion());
      objeto.setRaw("Address.address.1", kiosco.getDireccion1());
      objeto.setRaw("Address.address.2", kiosco.getDireccion2());
      objeto.setRaw("Address.address.3", kiosco.getDireccion3());
      objeto.setRaw("Address.telephone", kiosco.getTelefono());
      objeto.setRaw("Hora.Inicio", kiosco.getHoraInicio());
      objeto.setRaw("Hora.Fin", kiosco.getHoraFin());
      objeto.setRaw("esInteligente", kiosco.getEsInteligente());
      objeto.setRaw("FlagHabilitado", kiosco.getFlagHabilitado());
      objeto.setRaw("reporte", !kiosco.getReporte().contains("false") ? true : false);
      return objeto;
    } catch (Exception e) {
      return RespuestaTAS.error(contexto, "TASSesionKioscoController - armarRespuestaOrdenIngreso()", e);
    }
  }

  public static Objeto estadoKiosco(ContextoTAS contexto) {
    try {
      TASKioscoEstadoOperativo estadoOperativo = new TASKioscoEstadoOperativo();
      String idTas = contexto.parametros.string("idTas", "-1");
      String initOperational = contexto.parametros.string("initOperational", "");
      String box = contexto.parametros.string("boxString", "");
      String ticket = contexto.parametros.string("ticket", "");
      String msr = contexto.parametros.string("msr", "");
      String cim = contexto.parametros.string("cim", "");
      Fecha fecha = contexto.parametros.fecha("fecha", "yyyy/MM/dd HH:mm:ss.SSS", new Fecha(new Date()));
      Date date = fecha.FechaDate();

      boolean vinieronTodos = TASKioscoEstadoOperativo.verificaParams(Integer.valueOf(idTas), initOperational, box,
          ticket, msr, cim);
      if (vinieronTodos)
        return RespuestaTAS.sinParametros(contexto, "Uno o mas parametros no ingresados");

      estadoOperativo = new TASKioscoEstadoOperativo(Integer.valueOf(idTas), initOperational, box, ticket, msr, cim,
          date);

      Objeto rta = TASSqlKiosco.guardarEstadoOperativo(contexto, estadoOperativo);
      if(rta.string("estado").contains("ERROR")) return RespuestaTAS.error(contexto, "TASSesionKioscoController - estadoKiosco()", (Exception) rta.get("error"));
      if(rta.string("estado").contains("false")) return RespuestaTAS.error(contexto, "No se pudo actualizar estado operativo");
      return rta;
    } catch (Exception e) {
      return RespuestaTAS.error(contexto, "TASSesionKioscoController - estadoKiosco()", e);
    }

  }

  // cargar datos de terminal en contexto
  public static Objeto cargarSesionKiosco(ContextoTAS contexto) {
    String ipKiosco = contexto.parametros.string("direccionIp");
    if (ipKiosco.isEmpty())
      return RespuestaTAS.sinParametros(contexto, "IP terminal no ingresados");
    TASKiosco kiosco = TASSqlKiosco.completarDatosKioscoByIp(contexto, ipKiosco);
    Map<Integer, TASKiosco> kioscos = ContextoTAS.getKioscos();
    if (!kioscos.isEmpty()) {
      for (Integer tasId : kioscos.keySet()) {
        if (tasId.equals(kiosco.getKioscoId()))
          return RespuestaTAS.sesionActiva(contexto, "existe sesion para tas ID: " + tasId);
        // ContextoTAS.crearSesionKiosco(kiosco);
        return kiosco.toClass(Objeto.class);
      }
    }
    // ContextoTAS.crearSesionKiosco(kiosco);
    return kiosco.toClass(Objeto.class);
  }

  public static Objeto eliminarSesionKiosco(ContextoTAS contexto) {
    Objeto rta = new Objeto();
    Integer idKiosco = Integer.valueOf(contexto.parametros.string("tasId"));
    Map<Integer, TASKiosco> kioscos = ContextoTAS.getKioscos();
    for (Integer tasId : kioscos.keySet()) {
      if (tasId.equals(idKiosco))
        kioscos.remove(tasId);
      rta.set("Terminal ID", idKiosco);
      rta.set("sesion_delete", true);
    }
    rta.set("sesiones_actuales", ContextoTAS.getKioscos());
    return rta;
  }

  // Obtengo todas las sesiones de terminales en contexto
  public static Objeto getSesionesKiosco(ContextoTAS contexto) {
    Map<Integer, TASKiosco> kioscos = ContextoTAS.getKioscos();
    Objeto sesiones = new Objeto();
    sesiones.set("sesiones", kioscos);
    return sesiones;
  }

  // Obtengo una sesion de una terminal especifica
  public static Objeto getSesionKioscoById(ContextoTAS contexto) {
    Objeto kiosco = new Objeto();
    String tasIp = contexto.parametros.string("direccionIp", "");
    Integer idKiosco = !tasIp.isEmpty() ? null : Integer.valueOf(contexto.parametros.string("tasId"));
    if (idKiosco == null)
      return RespuestaTAS.sinParametros(contexto, "ID Tas vacio");
    Map<Integer, TASKiosco> kioscos = ContextoTAS.getKioscos();
    for (Integer tasId : kioscos.keySet()) {
      kiosco = tasId.equals(idKiosco) ? kioscos.get(tasId)
          : RespuestaTAS.sinResultados(contexto, "TasID no encontrado");
    }
    return kiosco;
  }
}