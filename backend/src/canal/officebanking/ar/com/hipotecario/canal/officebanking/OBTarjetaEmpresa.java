package ar.com.hipotecario.canal.officebanking;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ar.com.hipotecario.backend.servicio.api.tarjetasCredito.*;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Mock;
import ar.com.hipotecario.canal.officebanking.jpa.ob.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.mocks.MockTCOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.tarjetaCredito.StopDebitOB;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cheques.ApiCheques;
import ar.com.hipotecario.backend.servicio.api.tarjetasCredito.TarjetaEmpresa.Card;
import ar.com.hipotecario.canal.officebanking.negocio.Cotizacion;
import ar.com.hipotecario.canal.officebanking.util.ConstantesOB;
import ar.com.hipotecario.canal.officebanking.dto.ErrorGenericoOB;
import ar.com.hipotecario.canal.officebanking.dto.prisma.TransaccionesRequest;
import ar.com.hipotecario.canal.officebanking.enums.EnumAccionesOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoCedipOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;
import ar.com.hipotecario.canal.officebanking.enums.pagoTarjeta.EnumEstadoPagoTarjetaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EcheqDescuentoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.EstadoCedipOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.PlazoFijoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.tarjetaCredito.EstadoPagoTarjetaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.tarjetaCredito.PagoTarjetaOB;

public class OBTarjetaEmpresa extends ModuloOB {
	
	public static Object descargaResumenPDF(ContextoOB contexto) {
		String numeroCuenta = contexto.parametros.string("numeroCuenta");
		String keyvalue = contexto.parametros.string("keyvalue");
		Resumen resumen;
		try {
			resumen = TarjetasCredito.descargarResumenPDF(contexto, numeroCuenta, keyvalue);
		} catch (ApiException e) {
			if (e.response.codigoHttp.equals(404) ) {
				return respuesta("SIN_RESUMEN");
			} else {
				return respuesta("DATOS_INVALIDOS");
			}
		}

		return respuesta("datos", resumen.file);
    }
	public static Object postStopDebit(ContextoOB contexto) {
		String numeroCuenta = contexto.parametros.string("numeroCuenta");
		String idcuenta = contexto.parametros.string("numeroCuenta");
		StopDebit response;

		if (!contexto.esProduccion() && (numeroCuenta == null || numeroCuenta.isEmpty())) {

			ServicioMockTCOB servicioMock = new ServicioMockTCOB(contexto);
			MockTCOB mock = servicioMock.obtenerRespuestaStopDebit(numeroCuenta);
			if (mock != null) {
				try {
					ObjectMapper mapper = new ObjectMapper();
					Map<String, Object> bodyJson = mapper.readValue((String) mock.getHttpBody(), Map.class);
					return respuesta("datos", bodyJson);
				} catch (Exception e) {
					e.printStackTrace();
					return respuesta("Error al parsear JSON doble");
				}
			}
		}

		try {
			response = TarjetasCredito.postStopDebit(contexto, numeroCuenta, idcuenta);
		} catch (ApiException e) {
			return respuesta("ERROR", "descripcion", e.getMessage());
		}
		return respuesta("datos", response);
	}
	
	public static Object obtenerTarjetasCredito(ContextoOB contexto) {
		Boolean adicionales = Boolean.parseBoolean(contexto.parametros.string("adicionales"));
		String idCobis = contexto.sesion().empresaOB.idCobis;
		Object resumen;
		try {
			resumen = TarjetasCredito.obtenerTarjetasCredito(contexto, idCobis, adicionales);
		} catch (ApiException e) {
			return respuesta("ERROR", e.response.body.toString());
		}
		return respuesta("datos", resumen);
	}
	
	public static Object obtenerCuentas(ContextoOB contexto) {
		String idPrisma = contexto.sesion().idPrisma; 
		Object listadoCuentas;

		if (!contexto.esProduccion()) {

			ServicioMockTCOB servicioMock = new ServicioMockTCOB(contexto);
			MockTCOB mock = servicioMock.obtenerRespuestaPorIdPrisma(idPrisma);
			if (mock != null) {
				try {
					ObjectMapper mapper = new ObjectMapper();
					Map<String, Object> bodyJson = mapper.readValue((String) mock.getHttpBody(), Map.class);
					return respuesta("datos", bodyJson);
				} catch (Exception e) {
					e.printStackTrace();
					return respuesta("Error al parsear JSON doble");
				}
			}
		}
		try {
			listadoCuentas = TarjetasCredito.obtenerCuentasPrisma(contexto, idPrisma);
		} catch (ApiException e) {
			return respuesta("ERROR", "descripcion", e.response.body);
		}
		return respuesta("datos",listadoCuentas);
	}
	
	public static Object obtenerVencimientos(ContextoOB contexto) {
		String idPrisma = null;
		String cuenta = contexto.parametros.string("cuenta");
		idPrisma = contexto.sesion().idPrisma;
		Object vencimientos;

		if (!contexto.esProduccion()) {
			ServicioMockTCOB servicioMock = new ServicioMockTCOB(contexto);
			MockTCOB mock = servicioMock.obtenerVencimientoPorIdPrismaYCuenta(idPrisma, cuenta);
			if (mock != null) {
				try {
					ObjectMapper mapper = new ObjectMapper();
					Map<String, Object> bodyJson = mapper.readValue((String) mock.getHttpBody(), Map.class);
					return respuesta("datos", bodyJson);
				} catch (Exception e) {
					e.printStackTrace();
					return respuesta("Error al parsear JSON doble");
				}
			}
		}
			try {
				vencimientos = TarjetasCredito.obtenerVencimientos(contexto, cuenta, idPrisma);
			} catch (ApiException e) {
				return respuesta("ERROR", "descripcion", e.response.body);
			}
			return respuesta("datos", vencimientos);
		}

	
	public static Object obtenerListadoTarjetas(ContextoOB contexto) {
		String idPrisma = null;
		String cuenta = contexto.parametros.string("cuenta");
		idPrisma = contexto.sesion().idPrisma;
		TarjetaEmpresa tarjetas;

		if (!contexto.esProduccion()) {
			ServicioMockTCOB servicioMock = new ServicioMockTCOB(contexto);
			MockTCOB mock = servicioMock.obtenerListadoTarjetasPorIdPrismaYCuenta(idPrisma,cuenta);
			if (mock != null) {
				try {
					ObjectMapper mapper = new ObjectMapper();
					TarjetaEmpresa bodyJson = mapper.readValue((String) mock.getHttpBody(), TarjetaEmpresa.class);

					SesionOB sesion = contexto.sesion();
					sesion.tarjetasPrisma = bodyJson.account.cards;
					sesion.save();

					return respuesta("datos", bodyJson);
				} catch (Exception e) {
					e.printStackTrace();
					return respuesta("Error al parsear JSON doble");
				}
			}
		}
		try {
			tarjetas = TarjetasCredito.obtenerListadoTarjetas(contexto, cuenta, idPrisma);
			if(tarjetas.account != null) {
				SesionOB sesion = contexto.sesion();
				sesion.tarjetasPrisma = tarjetas.account.cards;				
				sesion.save();
			}
			
		} catch (ApiException e) {
			return respuesta("ERROR", "descripcion", e.response.body);
		}
		return respuesta("datos", tarjetas);
	}

	public static Object obtenerTransacciones(ContextoOB contexto) {
		String idPrisma = contexto.sesion().idPrisma;
		Gson gson = new Gson();
		Type listType = new TypeToken<List<String>>() {}.getType();

		//List<String> requestList = gson.fromJson(contexto.request.body(), listType);
		Objeto jsonBody = Objeto.fromJson(contexto.request.body());
		List<String> requestList = gson.fromJson(jsonBody.string("lista"), listType);

		List<TransaccionesPrisma> transaccionesList = new ArrayList<>();

		for (String t : requestList) {
			TarjetaEmpresa.Card infoCard = new TarjetaEmpresa.Card();
			infoCard = contexto.sesion().tarjetasPrisma.stream().filter(i -> t.equals(i.card_number)).findFirst().orElse(null);

			if (infoCard == null) {
				continue;
			}
			try {
				TransaccionesRequest transaccionesRequest = new TransaccionesRequest();
				TransaccionesRequest.CardInfo cardInfo = new TransaccionesRequest.CardInfo();
				TransaccionesRequest.ClientPrisma clientPrisma = new TransaccionesRequest.ClientPrisma();

				String[] bin = infoCard.card_number.split("\\*+");
				cardInfo.setBin(bin[0]);
				cardInfo.setLastFourDigits(bin[1]);

				clientPrisma.setDocumentType(infoCard.client.document_type);
				clientPrisma.setDocumentNumber(infoCard.client.document_number);
				clientPrisma.setGender(infoCard.client.gender);

				transaccionesRequest.setCard(cardInfo);
				transaccionesRequest.setClient(clientPrisma);
				transaccionesRequest.setCompanyId(idPrisma);
				validarTransaccionesRequest(transaccionesRequest);
				String requestBody = new Gson().toJson(transaccionesRequest);

				if (!contexto.esProduccion()) {
					ServicioMockTCOB servicioMock = new ServicioMockTCOB(contexto);
					MockTCOB mock = servicioMock.obtenerTransaccionesPorIdPrismaYRequest(idPrisma, requestBody);
					if (mock != null) {
						try {
							ObjectMapper mapper = new ObjectMapper();
							TransaccionesPrisma bodyJson = mapper.readValue((String) mock.getHttpBody(), TransaccionesPrisma.class);
							transaccionesList.add(bodyJson);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else {
					try {
						TransaccionesPrisma transacciones = TarjetasCredito.obtenerTransacciones(contexto, transaccionesRequest);
						transaccionesList.add(transacciones);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				return respuesta("ERROR", "descripcion", e);
			}
		}
		return respuesta("datos", transaccionesList);
	}

	private static void validarTransaccionesRequest(TransaccionesRequest request) {
		if (request == null ||
				request.getCard() == null ||
				request.getCard().getBin() == null ||
				request.getCard().getLastFourDigits() == null ||
				request.getClient() == null ||
				request.getClient().getDocumentType() == null ||
				request.getClient().getDocumentNumber() == null ||
				request.getClient().getGender() == null ||
				request.getCompanyId() == null) {
			throw new IllegalArgumentException("El TransaccionesRequest está incompleto o tiene campos nulos.");
		}
	}

	public static Object obtenerDatosPago(ContextoOB contexto) {
		String idTarjeta = contexto.parametros.string("id");
		BigDecimal dolarVenta = Cotizacion.dolarVenta(contexto);
        BigDecimal dolarCompra = Cotizacion.dolarCompra(contexto);
        
        if (dolarVenta == null) {          
            return respuesta("estado","NO EXISTE MONEDA");
        }
        
        DatosPagoTC pago;
        //cuenta en dolar solo paga deuda en dolar.
        //cuenta en pesos puede pagar deuda en pesos / dolar.
        BigDecimal montoPagoTotalPesos = new BigDecimal(0);
        BigDecimal montoPagoTotalDolares = new BigDecimal(0);
        BigDecimal sumaTotalDolaresMasPesosEnPesos = new BigDecimal(0);
        
		try {
			pago = TarjetasCredito.obtenerDatosPago(contexto, idTarjeta);
			montoPagoTotalPesos = BigDecimal.valueOf(pago.debitosEnCursoPesos);
			montoPagoTotalDolares = BigDecimal.valueOf(pago.debitosEnCursoDolares);
		} catch (ApiException e) {
			return respuesta("ERROR", "descripcion", e.response.body);
		}
		
		sumaTotalDolaresMasPesosEnPesos = montoPagoTotalPesos.add(montoPagoTotalDolares.multiply(dolarVenta));
		
		sumaTotalDolaresMasPesosEnPesos = sumaTotalDolaresMasPesosEnPesos.setScale(2, RoundingMode.HALF_UP);
		
		pago.sumaTotalDolaresMasPesosEnPesos = sumaTotalDolaresMasPesosEnPesos.doubleValue();
		pago.cotizacionActualCompra = dolarCompra.doubleValue();
		pago.cotizacionActualVenta = dolarVenta.doubleValue();
		pago.totalDolaresEnPesos = montoPagoTotalDolares.multiply(montoPagoTotalDolares.compareTo(new BigDecimal(0)) >= 0 ? dolarVenta : dolarCompra).doubleValue();
		
		// agregar posición consolidada para firmantes
		// INDIVIDUOS: ApiResponse response = ProductosService.productos(contexto);
		
		return respuesta("datos", pago);
	}
	
	public static PagoTarjeta pagarTarjeta(ContextoOB contexto) {

        HashMap<String, String> dto = new HashMap<>();
        PagoTarjeta pago = new PagoTarjeta();
        
        if (contexto.sesion().idCobis == null || contexto.sesion().empresaOB.idCobis == null) {
        	pago.error.setErrores("Sesión inválida.", "SIN_PSEUDO_SESION");
        }
        
        if (contexto.parametros.bigDecimal("monto").compareTo(BigDecimal.ZERO) < 0) {
        	pago.error.setErrores("El pago es menor a 0.", "PAGO_MENOR_A_CERO");
        }
        
        BigDecimal importe = contexto.parametros.bigDecimal("monto");
        dto.put("cuentaTarjeta", contexto.parametros.string("cuentaTarjeta"));
        dto.put("numeroTarjeta", ""); // Solo lo usa en el comprobante. No es necesario enviarlo si pagamos toda la cuenta visa.
        dto.put("cuenta", contexto.parametros.string("cuenta"));
        dto.put("moneda", contexto.parametros.string("moneda"));
        dto.put("tipoTarjeta", contexto.parametros.string("tipoTarjeta"));
        dto.put("tipoCuenta", contexto.parametros.string("tipoCuenta"));
        
        String canal = "HB_BE"; // Cambiar a HB_BE cuando se habilite para nuestro canal y se vincule con la causal
        
        try {
			pago = TarjetasCredito.pagoTarjeta(contexto, dto, importe, canal);
		} catch (ApiException e) {	
			pago.error = new ErrorGenericoOB("Error al pagar la tarjeta.", e.getMessage());
			return pago;
		}
        
		return pago;
	}

	/**
	 * Inicia el proceso de registrar un Stop Debit en la bandeja de operaciones.
	 * @param contexto Contexto de la operación, contiene sesión y parámetros.
	 * @return Respuesta con el id de la operación creada o mensaje de error.
	 */
	public static Object bandejaStopDebit (ContextoOB contexto) {
		Object respuestaFinal = null;
		// Obtiene la sesión y el usuario de empresa actual
		SesionOB sesion = contexto.sesion();
		EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);

		try {
			// Obtiene los parámetros necesarios para la operación
			String cuenta = contexto.parametros.string("cuenta");
			Integer moneda = contexto.parametros.integer("moneda");
			BigDecimal monto = contexto.parametros.bigDecimal("importe");

			// Servicio para registrar el pago de tarjeta
			ServicioStopDebitOB servicio = new ServicioStopDebitOB(contexto);

			List<BandejaOB> bandejas = servicio.findByNumeroCuenta(cuenta).get();
			BandejaOB bandejaActual = bandejas != null && !bandejas.isEmpty() ? bandejas.get(0) : null;
			// Verifica si ya existe una bandeja para la cuenta en el mes actual
			if (bandejaActual != null) {
				LocalDateTime fecha = bandejaActual.fechaUltActulizacion;
				LocalDateTime fechaActual = fecha != null ? fecha : LocalDateTime.now();
				if (fecha.getMonth() == fechaActual.getMonth()) {

				}
			}

			// Servicios para obtener los estados y tipos necesarios
			ServicioEstadoBandejaOB servicioEstadoBandejaOB = new ServicioEstadoBandejaOB(contexto);

			// Estado inicial de la bandeja y estado de pago en bandeja
			EstadoBandejaOB estadoInicialBandeja = servicioEstadoBandejaOB.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();

			// Tipo de producto para la firma
			ServicioTipoProductoFirmaOB servicioTipoProductoFirmaOB = new ServicioTipoProductoFirmaOB(contexto);
			TipoProductoFirmaOB tipoProductoFirmaOB = servicioTipoProductoFirmaOB.findByCodigo(EnumTipoProductoOB.STOP_DEBIT.getCodigo()).get();


			// Envía el pago a la bandeja y obtiene el objeto de pago
			BandejaOB bandejaStopDebitOB = servicio.enviarStopDebitBandeja(contexto, cuenta, monto, moneda, sesion.usuarioOB, sesion.empresaOB, tipoProductoFirmaOB).get();

			// Obtiene la acción de crear y la registra en la bandeja
			ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
			AccionesOB accionCrear = servicioAcciones.find(EnumAccionesOB.CREAR.getCodigo()).get();

			ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);

			// Crea la acción en la bandeja
			servicioBandejaAcciones.crear(bandejaStopDebitOB, empresaUsuario, accionCrear, estadoInicialBandeja, estadoInicialBandeja);

			// Guarda el id del pago en los parámetros del contexto
			contexto.parametros.set("idPagoTarjet a", bandejaStopDebitOB.id);

			// Arma la respuesta con el id de la operación
			Objeto respuestaPago = new Objeto();
			respuestaPago.set("idOperacion", bandejaStopDebitOB.id);
			respuestaFinal = respuesta("0", "datos", respuestaPago);

		} catch (RuntimeException rte) {
			// Devuelve mensaje de error si ocurre una excepción
			return respuesta(rte.getMessage());
		}

		return respuestaFinal;
	}
	
	public static Object bandejaPagoTarjeta (ContextoOB contexto) {
		Object respuestaFinal = null;
    	SesionOB sesion = contexto.sesion();
        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);

        try {
        	Integer moneda = contexto.parametros.integer("moneda");
        	BigDecimal monto = contexto.parametros.bigDecimal("importe");     	
         
            ServicioEstadoBandejaOB servicioEstadoBandejaOB = new ServicioEstadoBandejaOB(contexto);
        	ServicioEstadoPagoTarjetaOB servicioEstadoPagoTarjetaOB = new ServicioEstadoPagoTarjetaOB(contexto);
        	
            EstadoBandejaOB estadoInicialBandeja = servicioEstadoBandejaOB.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
            EstadoPagoTarjetaOB estadoEnBandeja = servicioEstadoPagoTarjetaOB.find(EnumEstadoPagoTarjetaOB.EN_BANDEJA.getCodigo()).get();
            
            ServicioTipoProductoFirmaOB servicioTipoProductoFirmaOB = new ServicioTipoProductoFirmaOB(contexto);
            TipoProductoFirmaOB tipoProductoFirmaOB = servicioTipoProductoFirmaOB.findByCodigo(EnumTipoProductoOB.PAGO_TARJETA.getCodigo()).get();
            
            ServicioPagoTarjetaOB servicio = new ServicioPagoTarjetaOB(contexto);

            PagoTarjetaOB pagoTarjetaOB = servicio.enviarPagoTarjeta(contexto, moneda, monto, estadoEnBandeja, tipoProductoFirmaOB, estadoInicialBandeja).get();            	
        	ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
        	BandejaOB bandeja = servicioBandeja.find(pagoTarjetaOB.id).get();

            ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
            AccionesOB accionCrear = servicioAcciones.find(EnumAccionesOB.CREAR.getCodigo()).get();

            ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
            
            servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionCrear, estadoInicialBandeja, estadoInicialBandeja);
            
            contexto.parametros.set("idPagoTarjeta", pagoTarjetaOB.id);
                    	
            Objeto respuestaPago = new Objeto();
            respuestaPago.set("idOperacion", bandeja.id);
            respuestaFinal = respuesta("0", "datos", respuestaPago);

        } catch (RuntimeException rte) {
            return respuesta(rte.getMessage());
        }

        return respuestaFinal;
	}
	
	public static Object detallesPagoTarjetaBandeja(ContextoOB contexto){
        LogOB.evento(contexto,"detallesPagoTarjetaBandeja", "INICIO");

        int idBandeja = contexto.parametros.integer("idOperacion");
        ServicioPagoTarjetaOB servicioPagoTarjetaOB = new ServicioPagoTarjetaOB(contexto);
        PagoTarjetaOB pagoTarjetaOB = servicioPagoTarjetaOB.find(idBandeja).get();
        if (empty(pagoTarjetaOB)){
            return new ErrorGenericoOB().setErrores("Operacion pago tarjeta inexistente", "El id ingresado no existe");
        }
        Objeto respuesta = new Objeto();
        respuesta.set("monto",pagoTarjetaOB.monto);
        respuesta.set("estado",pagoTarjetaOB.estado.descripcion);
        respuesta.set("cuentaOrigen",pagoTarjetaOB.cuentaOrigen);
        respuesta.set("tipoCuenta", pagoTarjetaOB.tipoCuenta);
        respuesta.set("cuentaTarjeta", pagoTarjetaOB.cuentaTarjeta);
        respuesta.set("estadoBandeja",pagoTarjetaOB.estadoBandeja.descripcion);
        
        LogOB.evento(contexto,"detallesPagoTarjetaBandeja", "FIN");
        
        return respuesta("0", "datos", respuesta);
    }
	
	 public static Object pagoHabilitado(ContextoOB contexto) {
	        Objeto respuesta = new Objeto();
	        String cuit = contexto.sesion().empresaOB.cuit.toString();
	        Set<String> cuitsHabilitados = Set.of(
	                "30500011072",
	                "30717516288",
	                "33708648359",
	                "33541373859",
	                "30715822500"	                
	        );
	        boolean habilitado = cuitsHabilitados.contains(cuit);
	        respuesta.set("estado", "0");
	        respuesta.set("datos", habilitado);
	        return respuesta;
	    }
	
	
}
