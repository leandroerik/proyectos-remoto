package ar.com.hipotecario.canal.homebanking.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Fecha;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.negocio.Paquete;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaCredito;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaDebito;
import ar.com.hipotecario.canal.homebanking.servicio.PaquetesService;
import ar.com.hipotecario.canal.homebanking.servicio.ProductosService;

public class HBPaquetes {

	public static Respuesta consolidadaPaquetes(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();
		String numeroPaquete = "";
		String codigoPaquete = "";
		ApiResponse response = ProductosService.productos(contexto, false);
		String tituloCuentaCobro = "";
		String cuentaCobroFormateada = "";

		Boolean existePaquete = false;
		String descPaquete = "";

		for (Objeto item : response.objetos("productos")) {
			if ("PAQ".equals(item.string("tipo"))) {
				codigoPaquete = item.string("codigoPaquete");
				descPaquete = Paquete.mapaDescripciones().get(item.string("codigoPaquete"));
				existePaquete = true;
			}
		}

		if (existePaquete) {
			Objeto productosPaquete = new Objeto();
			for (Cuenta cuenta : contexto.cuentas()) {
				if (cuenta.idPaquete() != null && !"".equals(cuenta.idPaquete())) {
					if (!"80".equals(cuenta.idMoneda()) || cuenta.esCuentaCorriente()) {
						numeroPaquete = cuenta.idPaquete();
						Objeto productoPaquete = new Objeto();
						//productoPaquete.set("id", cuenta.id());
						productoPaquete.set("id", cuenta.idEncriptado());
						productoPaquete.set("titulo", cuenta.producto());
						productoPaquete.set("descripcion", cuenta.simboloMoneda() + " " + cuenta.numeroEnmascarado());
						productosPaquete.add(productoPaquete);
					}
				}
			}

			for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
				if (tarjetaDebito.activacionTemprana()) {
					continue;
				}

				if (tarjetaDebito.idPaquete() != null && !"".equals(tarjetaDebito.idPaquete())) {
					numeroPaquete = tarjetaDebito.idPaquete();
					Objeto productoPaquete = new Objeto();
					//productoPaquete.set("id", tarjetaDebito.id());
					productoPaquete.set("id", tarjetaDebito.idEncriptado());
					productoPaquete.set("titulo", "Tarjeta de Débito");
					productoPaquete.set("descripcion", "VISA XXXX-" + tarjetaDebito.ultimos4digitos());
					productosPaquete.add(productoPaquete);
				}
			}

			for (TarjetaCredito tarjetaCredito : contexto.tarjetasCreditoTitularConAdicionalesTercero()) {
				if (tarjetaCredito.idPaquete() != null && !"".equals(tarjetaCredito.idPaquete())) {

					// TODO: problema con idPaquete reportado en casos de prod, se esta quedado con
					// paquetes de adionales
					if (tarjetaCredito.esTitular()) {
						numeroPaquete = tarjetaCredito.idPaquete();
					}

					Objeto productoPaquete = new Objeto();
					productoPaquete.set("id", tarjetaCredito.idEncriptado());
					productoPaquete.set("titulo", "Tarjeta de Crédito");
					productoPaquete.set("descripcion", tarjetaCredito.tipo() + " " + "XXXX-" + tarjetaCredito.ultimos4digitos());
					productosPaquete.add(productoPaquete);
				}
			}

			if ("".equals(numeroPaquete)) {
				TarjetaCredito tarjetaCredito = contexto.tarjetaCreditoTitular();
				Respuesta respuestaError = new Respuesta();
				respuestaError.setEstado("TIENE_PAQUETE_PERO_NO_TIENE_ID");
				respuestaError.set("esHML", tarjetaCredito != null ? tarjetaCredito.esHML() : false);
				return respuestaError;
			} else {
				ApiResponse responseConsolidadaPaquetes = PaquetesService.consolidadaPaquetes(contexto, numeroPaquete);
				if (response.hayError()) {
					if ("40003".equals(response.string("codigo"))) {
						return Respuesta.estado("ERROR_CORRIENDO_BATCH");
					}
					return Respuesta.estado("ERROR_CONSOLIDADA");
				}

				for (Objeto item : responseConsolidadaPaquetes.objetos()) {
					if ("AHO".equals(item.string("tipo")) || "CTE".equals(item.string("tipo"))) {
						if (item.bool("cuentaCobro")) {
							Cuenta cuenta = contexto.cuenta(item.string("numero"));
							if (cuenta == null) {
								return Respuesta.estado("ERROR_SIN_CUENTA_COBRO");
							}
							cuentaCobroFormateada = cuenta.simboloMoneda() + " " + cuenta.numeroEnmascarado();
							if ("AHO".equals(item.string("tipo"))) {
								tituloCuentaCobro = "Caja de ahorro";
							}
							if ("CTE".equals(item.string("tipo"))) {
								tituloCuentaCobro = "Cuenta Corriente";
							}
						}
					}
				}

				Objeto datosActuales = new Objeto();

				boolean mostrarBonificaciones = true;
				boolean mostrarHistorial = true;

				if (contexto.persona().esEmpleado() || "34".equals(codigoPaquete) || "35".equals(codigoPaquete) || "36".equals(codigoPaquete) || "37".equals(codigoPaquete) || "38".equals(codigoPaquete)) {
					mostrarBonificaciones = false;
					mostrarHistorial = false;
				}

				if (!mostrarBonificaciones && !mostrarHistorial) {
					Objeto datosPrincipales = new Objeto();
					datosPrincipales.set("numeroPaquete", numeroPaquete);
					datosPrincipales.set("descPaquete", descPaquete);
					datosPrincipales.set("tituloCuentaCobro", tituloCuentaCobro);
					datosPrincipales.set("cuentaCobroFormateada", cuentaCobroFormateada);
					datosPrincipales.set("productos", productosPaquete);
					respuesta.set("datosPrincipales", datosPrincipales);
					return respuesta;
				}

				// BONIFICACIONES-DESDE
				Objeto bonificaciones = new Objeto();
				ApiResponse responseBonificacionesVigentes = PaquetesService.bonificacionesVigentesPorPaquetes(contexto, codigoPaquete, numeroPaquete);
				if (!responseBonificacionesVigentes.hayError()) {
					for (Objeto item : responseBonificacionesVigentes.objetos()) {
						Objeto bonificacion = new Objeto();
						// bonificacion.set("tipo", item.string("tipo"));
						String tipoBonificacion = item.string("tipoBonificacion").toLowerCase();
						tipoBonificacion = tipoBonificacion.substring(0, 1).toUpperCase() + tipoBonificacion.substring(1);
						bonificacion.set("tipo", item.string("tipoBonificacion"));
						bonificacion.set("descripcion", tipoBonificacion);
						bonificacion.set("porcentajeFormateado", item.string("porcentaje"));
						bonificacion.set("fechaVencimiento", item.date("fechaVencimiento", "yyyy-MM-dd", "dd/MM/YYYY"));

						bonificaciones.add(bonificacion);
					}
					datosActuales.set("bonificaciones", bonificaciones);
				}
				// BONIFICACIONES-HASTA

				// OBJETIVOS-DESDE
				Objeto bonificacionesObjetivo = new Objeto();
				ApiResponse responseBonificacionesObjetivo = PaquetesService.bonificacionesHistoricoUltimoCobro(contexto, numeroPaquete);
				if (!responseBonificacionesObjetivo.hayError()) {
					for (Objeto item : responseBonificacionesObjetivo.objetos()) {
						if (numeroPaquete.equals(item.string("idPaquete"))) {
							for (Objeto itemCobro : item.objetos("cobros")) {
								Objeto bonificacionObjetivo = new Objeto();
								switch (itemCobro.string("bonificacionPor")) {
								case "CONSUMO TD":
									bonificacionObjetivo.set("descripcion", "Consumir con tarjeta de débito");
									break;
								case "DA EN CUENTA":
									bonificacionObjetivo.set("descripcion", "Tener en cuenta");
									break;
								case "SALDO PROMEDIO":
									bonificacionObjetivo.set("descripcion", "Tener en la caja de ahorro");
									break;
								case "INVERSION PF":
									bonificacionObjetivo.set("descripcion", "Invertir en plazos fijos");
									break;
								case "BIENVENIDA":
									bonificacionObjetivo.set("descripcion", "Bienvenida");
									break;
								case "CONSUMO TC":
									bonificacionObjetivo.set("descripcion", "Consumir con tarjeta de crédito");
									break;
								case "CONSUMO TOTAL":
									bonificacionObjetivo.set("descripcion", "Consumo total");
									break;
								default:
									bonificacionObjetivo.set("descripcion", "");
								}

								// if (itemCobro.string("objetivo", "").contains(".")) {
								if ("DA EN CUENTA".equals(itemCobro.string("bonificacionPor"))) {
									bonificacionObjetivo.set("objetivoFormateado", itemCobro.integer("objetivo").toString() + " débitos autom.");
									bonificacionObjetivo.set("objetivo", itemCobro.integer("objetivo"));
									if (itemCobro.integer("movimientosMesActual") == 1) {
										bonificacionObjetivo.set("movimientosActualFormateado", "Tenés " + itemCobro.integer("movimientosMesActual").toString() + " débito automático.");
									} else {
										bonificacionObjetivo.set("movimientosActualFormateado", "Tenés " + itemCobro.integer("movimientosMesActual").toString() + " débitos automáticos.");
									}
									bonificacionObjetivo.set("movimientosActual", itemCobro.integer("movimientosMesActual"));
								} else {
									bonificacionObjetivo.set("objetivoFormateado", "$ " + Formateador.importe(itemCobro.bigDecimal("objetivo")));
									if ("SALDO PROMEDIO".equals(itemCobro.string("bonificacionPor"))) {
										bonificacionObjetivo.set("objetivoFormateado", "$ " + Formateador.importe(itemCobro.bigDecimal("objetivo")) + " en promedio");
									}
									bonificacionObjetivo.set("objetivo", itemCobro.bigDecimal("objetivo"));
									bonificacionObjetivo.set("movimientosActualFormateado", "$ " + Formateador.importe(itemCobro.bigDecimal("movimientosMesActual")));
									bonificacionObjetivo.set("movimientosActual", itemCobro.bigDecimal("movimientosMesActual"));
								}

								BigDecimal barraObjetivo = new BigDecimal(0);
								try {
									if (!itemCobro.bigDecimal("objetivo").equals(new BigDecimal(0))) {
										barraObjetivo = itemCobro.bigDecimal("movimientosMesActual").multiply(new BigDecimal(100)).divide(itemCobro.bigDecimal("objetivo"), RoundingMode.UP);
										if (barraObjetivo.compareTo(new BigDecimal(100)) > 0) {
											barraObjetivo = new BigDecimal(100);
										}
									}
								} catch (Exception e) {
									// Si da un error prefiero que no explote la consolidada
								}
								bonificacionObjetivo.set("barraObjetivo", barraObjetivo.intValue());

								String porcentajeBonificacion = itemCobro.string("porcentajeBonificacion");
								/*
								 * if ("".equals(porcentajeBonificacion) || porcentajeBonificacion == null) {
								 * porcentajeBonificacion = "0%"; }
								 */
								bonificacionObjetivo.set("porcentajeBonificacion", porcentajeBonificacion);
								bonificacionesObjetivo.add(bonificacionObjetivo);
							}
						}
					}
				}
				datosActuales.set("objetivos", bonificacionesObjetivo);
				// OBJETIVOS-HASTA

				// BONIFICACIONES HISTORICO DESDE
				String ultimoPorcentajeBonificacionHistorico = "";
				String ultimoMesBonificacionHistorico = "";
				Objeto historico = new Objeto();
				List<Objeto> bonificacionesHistorico = new ArrayList<>();
				ApiResponse responseHistorico = PaquetesService.bonificacionesHistorico(contexto, numeroPaquete, "", "");
				if (!responseHistorico.hayError() && responseHistorico.objetos() != null) {
					for (Objeto item : responseHistorico.objetos()) {
						if (numeroPaquete.equals(item.string("idPaquete"))) {
							String numeroMes = item.date("fechaCobro", "yyyy-MM-dd", "MM");
							String mes = Fecha.mes(numeroMes);

							Objeto mesBonificacion = new Objeto();
							mesBonificacion.set("id", item.string("idPaquete") + "_" + item.date("fechaCobro", "yyyy-MM-dd", "dd/MM/YYYY") + "_" + item.string("idServicio") + "_" + item.string("idRubro"));
							mesBonificacion.set("mes", item.date("fechaCobro", "yyyy-MM-dd", "MM/YY"));
							mesBonificacion.set("fechaCobro", item.date("fechaCobro", "yyyy-MM-dd", "dd/MM/YYYY"));
							mesBonificacion.set("porcentajeBonificacion", item.string("porcentajeBonificacion"));

							BigDecimal montoAbonado = item.bigDecimal("costoNeto", "0.0"); // - costoneto
							BigDecimal montoOriginal = item.bigDecimal("costoOriginal", "0.0");
							BigDecimal montoBonificado = montoOriginal.subtract(montoAbonado);

							if (montoAbonado == null)
								montoAbonado = new BigDecimal(0);
							mesBonificacion.set("montoBonificado", montoBonificado);
							mesBonificacion.set("montoBonificadoFormateado", "$ " + Formateador.importe(montoBonificado));
							mesBonificacion.set("montoAbonado", item.bigDecimal("costoNeto"));
							mesBonificacion.set("montoAbonadoFormateado", "$" + Formateador.importe(item.bigDecimal("costoNeto")));
							mesBonificacion.set("montoMantenimiento", montoOriginal);
							mesBonificacion.set("montoMantenimientoFormateado", "$ " + Formateador.importe(montoOriginal));
							ultimoMesBonificacionHistorico = mes.toLowerCase();
							ultimoPorcentajeBonificacionHistorico = item.string("porcentajeBonificacion");
							bonificacionesHistorico.add(mesBonificacion);
						}
					}
				}
				while (bonificacionesHistorico.size() > 3) {
					bonificacionesHistorico.remove(0);
				}

				// Pongo los datos del histórico en forma descendente
				int x = bonificacionesHistorico.size();
				List<Objeto> bonificacionesHistoricoDesc = new ArrayList<>();
				while (x > 0) {
					x--;
					bonificacionesHistoricoDesc.add(bonificacionesHistorico.get(x));
				}
				historico.set("bonificacionesHistorico", bonificacionesHistoricoDesc);

				String textoBonificacion = "";
				if (ultimoMesBonificacionHistorico != null && !"".equals(ultimoMesBonificacionHistorico) && ultimoPorcentajeBonificacionHistorico != null && !"".equals(ultimoPorcentajeBonificacionHistorico)) {
					textoBonificacion = "¡En " + ultimoMesBonificacionHistorico + " bonificaste el " + ultimoPorcentajeBonificacionHistorico + " del mantenimiento!";
				}
				// BONIFICACIONES HISTORICO HASTA

				if ("".equals(textoBonificacion)) {
					mostrarHistorial = false;
				}

				Objeto datosPrincipales = new Objeto();
				datosPrincipales.set("numeroPaquete", numeroPaquete);
				datosPrincipales.set("descPaquete", descPaquete);
				datosPrincipales.set("textoBonificacion", textoBonificacion);
				datosPrincipales.set("tituloCuentaCobro", tituloCuentaCobro);
				datosPrincipales.set("cuentaCobroFormateada", cuentaCobroFormateada);
				datosPrincipales.set("mostrarBonificaciones", mostrarBonificaciones);
				datosPrincipales.set("mostrarHistorial", mostrarHistorial);
				datosPrincipales.set("mostrarTextoBonificacion", ultimoPorcentajeBonificacionHistorico != null ? !"0".equals(ultimoPorcentajeBonificacionHistorico.replace(" ", "").replace("%", "")) : false);

				datosPrincipales.set("productos", productosPaquete);
				respuesta.set("datosPrincipales", datosPrincipales);
				respuesta.set("datosActuales", datosActuales);
				respuesta.set("historico", historico);
			}
		} else {
			TarjetaCredito tarjetaCredito = contexto.tarjetaCreditoTitular();
			Respuesta respuestaError = new Respuesta();
			respuestaError.setEstado("NO_TIENE_PAQUETE");
			respuestaError.set("esHML", tarjetaCredito != null ? tarjetaCredito.esHML() : false);
			return respuestaError;
		}

		return respuesta;
	}

	public static Respuesta historicoBonificaciones(ContextoHB contexto) {
		String fechaDesde = contexto.parametros.date("fechaDesde", "d/M/yyyy", "dd/MM/yyyy", null);
		String fechaHasta = contexto.parametros.date("fechaHasta", "d/M/yyyy", "dd/MM/yyyy", null);

		if (Objeto.anyEmpty(fechaDesde, fechaHasta)) {
			return Respuesta.parametrosIncorrectos();
		}

		String numeroPaquete = "";
		numeroPaquete = traerNumeroPaquete(contexto);
		if ("".equals(numeroPaquete)) {
			return Respuesta.error();
		}

		ApiResponse responseHistorico = PaquetesService.bonificacionesHistorico(contexto, numeroPaquete, fechaDesde, fechaHasta);
		if (responseHistorico.hayError()) {
			if ("40003".equals(responseHistorico.string("codigo"))) {
				return Respuesta.estado("ERROR_CORRIENDO_BATCH");
			}
			Respuesta.error();
		}
		Objeto historico = new Objeto();
		Objeto bonificacionesHistorico = new Objeto();
		for (Objeto item : responseHistorico.objetos()) {
			if (numeroPaquete.equals(item.string("idPaquete"))) {
				Objeto mesBonificacion = new Objeto();
				mesBonificacion.set("id", item.string("idPaquete") + "_" + item.date("fechaCobro", "yyyy-MM-dd", "dd/MM/YYYY") + "_" + item.string("idServicio") + "_" + item.string("idRubro"));
				mesBonificacion.set("mes", item.date("fechaCobro", "yyyy-MM-dd", "MM/YY"));
				mesBonificacion.set("fechaCobro", item.date("fechaCobro", "yyyy-MM-dd", "dd/MM/YYYY"));
				mesBonificacion.set("porcentajeBonificacion", item.string("porcentajeBonificacion"));
				BigDecimal montoAbonado = item.bigDecimal("costoNeto", "0.0"); // - costoneto
				BigDecimal montoOriginal = item.bigDecimal("costoOriginal", "0.0");
				BigDecimal montoBonificado = montoOriginal.subtract(montoAbonado);

				if (montoAbonado == null)
					montoAbonado = new BigDecimal(0);
				mesBonificacion.set("montoBonificado", montoBonificado);
				mesBonificacion.set("montoBonificadoFormateado", "$ " + Formateador.importe(montoBonificado));
				mesBonificacion.set("montoAbonado", montoAbonado);
				mesBonificacion.set("montoAbonadoFormateado", "$ " + Formateador.importe(montoAbonado));
				mesBonificacion.set("montoMantenimiento", montoOriginal);
				mesBonificacion.set("montoMantenimientoFormateado", "$ " + Formateador.importe(montoOriginal));

				// emm-desde-->Esto es con el fin de ver si puedo encontrar un detalle. Sacarlo,
				// porque no tiene sentido hacerlo.
				/*
				 * try { ApiResponse responseHistoricoDetallado =
				 * PaquetesService.bonificacionesHistoricoDetallado(contexto, numeroPaquete,
				 * item.date("fechaCobro", "yyyy-MM-dd", "dd/MM/yyyy"),
				 * item.string("idServicio"),item.string("idRubro")); } catch(Exception e) {
				 * 
				 * }
				 */
				// emm-hasta

				bonificacionesHistorico.add(mesBonificacion);
			}
		}

		Respuesta respuesta = new Respuesta();
		historico.set("bonificacionesHistorico", bonificacionesHistorico);
		respuesta.set("historico", historico);
		return respuesta;
	}

	public static Respuesta historicoBonificacionesDetalle(ContextoHB contexto) {
		// String fecha = contexto.parametros.date("fecha", "d/M/yyyy", "dd/MM/yyyy",
		// null);
		String id = contexto.parametros.string("id");

		String numeroPaquete = "";

		if (Objeto.anyEmpty(id)) {
			return Respuesta.parametrosIncorrectos();
		}
		Respuesta respuesta = new Respuesta();

		numeroPaquete = traerNumeroPaquete(contexto);
		if ("".equals(numeroPaquete)) {
			return Respuesta.error();
		}
		Objeto datos = new Objeto();
		Objeto bonificacionesObjetivo = new Objeto();
		String partes[] = id.split("_");

		String fecha = partes[1];
		String servicio = partes[2];
		String rubro = partes[3];

		Objeto bonificaciones = new Objeto();
		ApiResponse responseBonificaciones = PaquetesService.bonificacionesHistoricoDetallado(contexto, numeroPaquete, fecha, servicio, rubro);
		if (!responseBonificaciones.hayError()) {
			for (Objeto item : responseBonificaciones.objetos()) {
				Objeto bonificacion = new Objeto();
				// bonificacion.set("tipo", item.string("tipo"));
				if ("N/A".equals(item.string("objetivo"))) {
					String tipoBonificacion = item.string("tipoBonificacion").toLowerCase();
					tipoBonificacion = tipoBonificacion.substring(0, 1).toUpperCase() + tipoBonificacion.substring(1);
					bonificacion.set("tipo", item.string("tipoBonificacion"));
					bonificacion.set("descripcion", tipoBonificacion);
					bonificacion.set("porcentajeFormateado", item.string("porcentajeBonificacion"));
					// bonificacion.set("fecha", item.date("fecha", "yyyy-MM-dd", "dd/MM/YYYY"));
					bonificaciones.add(bonificacion);
				} else {
					Objeto bonificacionObjetivo = new Objeto();
					switch (item.string("tipoBonificacion")) {
					case "CONSUMO TD":
						bonificacionObjetivo.set("descripcion", "Consumos con Tarjeta de Débito"); // Consumido con Tarjeta de Débito
						break;
					case "DA EN CUENTA":
						bonificacionObjetivo.set("descripcion", "Débitos automáticos en cuenta");
						break;
					case "SALDO PROMEDIO":
						bonificacionObjetivo.set("descripcion", "Saldo Promedio de Caja de Ahorro");
						break;
					case "INVERSION PF":
						bonificacionObjetivo.set("descripcion", "Inversiones en Plazos Fijos");
						break;
					case "BIENVENIDA":
						bonificacionObjetivo.set("descripcion", "Bienvenida");
						break;
					default:
						bonificacionObjetivo.set("descripcion", "");
					}

					// if (itemCobro.string("objetivo", "").contains(".")) {
					if ("DA EN CUENTA".equals(item.string("tipoBonificacion"))) {
						bonificacionObjetivo.set("objetivoFormateado", item.integer("objetivo").toString() + " débitos autom.");
						bonificacionObjetivo.set("objetivo", item.integer("objetivo"));
						if (item.integer("movimientos") == 1) {
							bonificacionObjetivo.set("movimientosActualFormateado", "Tenés " + item.integer("movimientos").toString() + " débito automático.");
						} else {
							bonificacionObjetivo.set("movimientosActualFormateado", "Tenés " + item.integer("movimientos").toString() + " débitos automáticos.");
						}
						bonificacionObjetivo.set("movimientosActual", item.integer("movimientos"));
					} else {
						bonificacionObjetivo.set("objetivoFormateado", "$ " + Formateador.importe(item.bigDecimal("objetivo")));
						bonificacionObjetivo.set("objetivo", item.bigDecimal("objetivo"));
						bonificacionObjetivo.set("movimientosActualFormateado", "$ " + Formateador.importe(item.bigDecimal("movimientos")));
						bonificacionObjetivo.set("movimientosActual", item.bigDecimal("movimientos"));
					}
					try {
						BigDecimal barraObjetivo = new BigDecimal(0);
						if (!item.bigDecimal("objetivo").equals(new BigDecimal(0))) {
							barraObjetivo = item.bigDecimal("movimientos").multiply(new BigDecimal(100)).divide(item.bigDecimal("objetivo"), RoundingMode.UP);
							if (barraObjetivo.compareTo(new BigDecimal(100)) > 0) {
								barraObjetivo = new BigDecimal(100);
							}
						}
						bonificacionObjetivo.set("barraObjetivo", barraObjetivo.intValue());
					} catch (Exception e) {
						// en caso de error prefiero que no tire excepción
					}

					String porcentajeBonificacion = item.string("porcentajeBonificacion");
					bonificacionObjetivo.set("porcentajeBonificacion", porcentajeBonificacion);
					bonificacionesObjetivo.add(bonificacionObjetivo);
				}
			}
		}

		datos.set("objetivos", bonificacionesObjetivo);
		datos.set("bonificaciones", bonificaciones);
		respuesta.set("datos", datos);

		return respuesta;
	}

	private static String traerNumeroPaquete(ContextoHB contexto) {
		Boolean existePaquete = false;
		String numeroPaquete = "";

		ApiResponse response = ProductosService.productos(contexto);
		for (Objeto item : response.objetos("productos")) {
			if ("PAQ".equals(item.string("tipo"))) {
				existePaquete = true;
			}
		}

		if (!existePaquete) {
			return "";
		}

		if (existePaquete) {
			for (Cuenta cuenta : contexto.cuentas()) {
				if (cuenta.idPaquete() != null && !"".equals(cuenta.idPaquete())) {
					numeroPaquete = cuenta.idPaquete();
				}
			}

			for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
				if (tarjetaDebito.activacionTemprana()) {
					continue;
				}

				if (tarjetaDebito.idPaquete() != null && !"".equals(tarjetaDebito.idPaquete())) {
					numeroPaquete = tarjetaDebito.idPaquete();
				}
			}

			for (TarjetaCredito tarjetaCredito : contexto.tarjetasCreditoTitularConAdicionalesTercero()) {
				if (tarjetaCredito.idPaquete() != null && !"".equals(tarjetaCredito.idPaquete())) {
					numeroPaquete = tarjetaCredito.idPaquete();
				}
			}

			if ("".equals(numeroPaquete)) {
				return "";
			}
		}

		return numeroPaquete;
	}
}
