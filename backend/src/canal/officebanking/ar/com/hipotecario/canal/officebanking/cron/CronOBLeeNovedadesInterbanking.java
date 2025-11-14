package ar.com.hipotecario.canal.officebanking.cron;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Cron.CronJob;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.notificaciones.ApiNotificaciones;
import ar.com.hipotecario.backend.servicio.sql.SqlHB_BE;
import ar.com.hipotecario.backend.servicio.sql.intercambioate.NovedadesATE;
import ar.com.hipotecario.backend.servicio.sql.intercambioate.PeticionesOB;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.LogOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumAccionesOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoTRNATE;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoTRNOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioAccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEjecucionBatchOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadoTRNOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioHistorialTrnOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioParametroOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioTransferenciaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.EjecucionBatchOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.EstadoTRNOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.TransferenciaOB;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CronOBLeeNovedadesInterbanking extends CronJob {

    private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
    private static final String CRON = CronOBLeeNovedadesInterbanking.class.getSimpleName().toUpperCase();
    private static final ServicioTransferenciaOB servicioTransferencias = new ServicioTransferenciaOB(contexto);
    private static final ServicioEstadoTRNOB servicioEstadoTransferencia = new ServicioEstadoTRNOB(contexto);
    private static final ServicioHistorialTrnOB servicioHistorialTransferencia = new ServicioHistorialTrnOB(contexto);
    private static final ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
    private static final ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
    private static final ServicioParametroOB servicioParametros = new ServicioParametroOB(contexto);
    private static final List<String> estadosFinales = servicioParametros.split("transferencia.estados.finales").get();
    private static final List<String> estadosRechazados = servicioParametros.split("transferencia.estados.rechazados").get();
    private static final List<String> estadosFaltaDeFondos = servicioParametros.split("transferencia.estados.faltaDeFondos").get();
    public void run() {
        boolean ejecutar = true;
        if (!ejecutar) {
            LogOB.evento(contexto, "CRON_DESHABILITADO_EJEC_CONFIG", "", CRON);
            return;
        }

        LogOB.evento(contexto, "INICIO CRON NOVE-ATE", LocalDateTime.now().toString(), CRON);
		

        ServicioEjecucionBatchOB servicioEjecucionBatch = new ServicioEjecucionBatchOB(contexto);
        EjecucionBatchOB ejecucion = servicioEjecucionBatch.buscarPorCron(CRON, LocalDate.now()).tryGet();
        if (ejecucion == null) {
        	LogOB.evento(contexto, "FIN - FIND CRON NOVE-ATE ", new Objeto().set("buscarPorCRON", CRON).set("ejecucion", "null").toString(), CRON);
            return;
        }

        int inicio = ejecucion.ultimaNovedad + 1;
        int trnPorEjecucion = contexto.config.integer("ob_cron_trn_por_ejecucion", 500);
        int maxPorLeer = inicio + trnPorEjecucion;

        String strEstadosFinales = CronOBLeeNovedadesInterbanking.getEstadosFinalesOB(contexto);
        String strEstadoFaltaDeFondos = servicioParametros.split("transferencia.estados.faltaDeFondos").get().get(0);

        NovedadesATE novedades = SqlHB_BE.novedadesATE(contexto, PeticionesOB.CANAL, inicio, maxPorLeer, strEstadosFinales, strEstadoFaltaDeFondos).get();
        if (novedades == null || novedades.size() < 1) {
            return;
        }
        
		LogOB.evento(contexto, "CronOBLeeNovedadesInterbanking", "novedadoes: "+ novedades.size() , CRON);
        Objeto trnActualizadas = new Objeto();
        for (NovedadesATE.NovedadATE novedad : novedades) {
			LogOB.evento(contexto, "CronOBLeeNovedadesInterbanking", "id de la novedad: "+ novedad.IdOpe , CRON);
			LogOB.evento(contexto, "CronOBLeeNovedadesInterbanking", "estado de la novedad: "+ novedad.Estado , CRON);
        
			
			try {
            	TransferenciaOB trnActual = servicioTransferencias.find(Integer.valueOf(novedad.IdOpe)).get();
                Boolean fueNotificada = trnActual.estado.id == EnumEstadoTRNOB.EXITO.getCodigo();
                
                if(trnActual.estado.id == EnumEstadoTRNOB.RECHAZADO.getCodigo()) {
                	fueNotificada = true;
                }
                                
				if (trnActual != null && !fueNotificada) {
					LogOB.evento(contexto, "CronOBLeeNovedadesInterbanking", "trnActual ID: "+ trnActual.id + " " + trnActual.estado.descripcion, CRON);
										
					TransferenciaOB trnModificada = actualizarEstado(trnActual, novedad.Estado, novedad.HorSnd);
					if (trnModificada != null && trnModificada.estado.id == EnumEstadoTRNOB.EXITO.getCodigo()) {
						LogOB.evento(contexto, "CronOBLeeNovedadesInterbanking", "ingreso a notificacion: "+ trnActual.id, CRON);
						String importe = trnActual.moneda.simbolo + " " + trnActual.monto.toString();
						String numeroOperacion = String.valueOf(trnActual.id);
						String cuentaOrigen = trnActual.debito.nroCuenta;
						String cuentaDestino = trnActual.credito.nroCuenta;
						if(fueNotificada == false) {
							LogOB.evento(contexto, "CronOBLeeNovedadesInterbanking", "Envio mail trn: "+ trnActual.id, CRON);
							ApiNotificaciones.envioAvisoTRNEnviadaOB(contexto, trnActual.usuario.email, trnActual.credito.titular, importe, numeroOperacion, cuentaOrigen, cuentaDestino);
						}
						if (trnActual.credito.email != null && !trnActual.credito.email.isBlank()) {
							LogOB.evento(contexto, "CronOBLeeNovedadesInterbanking", "Envio mail trn dest: "+ trnActual.id, CRON);
							ApiNotificaciones.envioAvisoTRNRecibidaOB(contexto, trnActual.credito.email, trnActual.empresa.razonSocial, importe, numeroOperacion, cuentaOrigen, cuentaDestino);
						}
					}

					Objeto actualizacionTRN = new Objeto().set("idTRN", trnModificada.id).set("inicial", trnActual.estado.descripcion).set("final", trnModificada.estado.descripcion);
					trnActualizadas.add(actualizacionTRN);
				}else {
					if(novedad.Estado.equals(Integer.toString(EnumEstadoTRNATE.RECHAZO_BCO_CREDITO.getCodigo()))) {
						ServicioEstadoTRNOB servicioEstadoTRNOB = new ServicioEstadoTRNOB(contexto);
                    	EstadoTRNOB estadoTrn = servicioEstadoTRNOB.find(EnumEstadoTRNOB.RECHAZADO.getCodigo()).get();
                    	
                    	trnActual.estado = estadoTrn;                    
                    	servicioTransferencias.update(trnActual);	                	
	                }
				}
			} catch (Exception e) {
				LogOB.evento(contexto, "CATCH NOVE-ATE", new Objeto().set("Exception", e.getMessage()).toString(), CRON);
			}
		}

		ejecucion.ultimaNovedad = novedades.get(novedades.size() - 1).RowNum;
		servicioEjecucionBatch.update(ejecucion);

		Objeto resultado = new Objeto().set("fecha", LocalDateTime.now().toString()).set("inicio", inicio).set("fin", (novedades.isEmpty() ? 0 : novedades.get((novedades.size() - 1)).RowNum)).set("busco", maxPorLeer).set("trnActualizados", trnActualizadas);
		LogOB.evento(contexto, "FIN NOVE-ATE", resultado.toString(), CRON);

	}

	private TransferenciaOB actualizarEstado(TransferenciaOB transferencia, String estadoNovedad, Fecha fechaEjecucion) {
		Integer idEstado = null;
		String comentario = null;

		if (estadosFinales.contains(estadoNovedad)) {
			idEstado = EnumEstadoTRNOB.EXITO.getCodigo();
			comentario = "";
		} else if (estadosRechazados.contains(estadoNovedad)) {
			idEstado = EnumEstadoTRNOB.RECHAZADO.getCodigo();
		} else if (estadosFaltaDeFondos.contains(estadoNovedad)) {
			comentario = "FALTA_FONDOS";
		}

		boolean update = false;
		if (idEstado != null) {
			transferencia.estado = servicioEstadoTransferencia.find(idEstado).get();
			update = true;
		}

		if (comentario != null) {
			transferencia.comentario = comentario;
			update = true;
		}

		if (update) {
			transferencia.fechaEjecucion = LocalDateTime.of(fechaEjecucion.año(), fechaEjecucion.mes(), fechaEjecucion.dia(), fechaEjecucion.hora(), fechaEjecucion.minuto(), fechaEjecucion.segundo());
			servicioHistorialTransferencia.cambiaEstado(transferencia, servicioAcciones.find(EnumAccionesOB.RECHAZAR.getCodigo()).get(), servicioEmpresaUsuario.findByUsuarioEmpresa(transferencia.usuario, transferencia.empresa).get(), transferencia.estado, servicioEstadoTransferencia.find(EnumEstadoTRNOB.RECHAZADO.getCodigo()).get());
			return servicioTransferencias.update(transferencia).get();
		}

		return null;

	}

	public static String getEstadosFinalesOB(ContextoOB contexto) {
		ServicioParametroOB servicioParametros = new ServicioParametroOB(contexto);

		List<String> estadosFinales = servicioParametros.split("transferencia.estados.finales").get();
		List<String> estadosRechazados = servicioParametros.split("transferencia.estados.rechazados").get();

		String joinEstadosFinales = estadosFinales.stream().collect(Collectors.joining("','"));
		String joinEstadosRechazados = estadosRechazados.stream().collect(Collectors.joining("','"));

		return "'" + joinEstadosFinales + "','" + joinEstadosRechazados + "'";
	}

}