package ar.com.hipotecario.canal.officebanking;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cheques.ApiCheques;
import ar.com.hipotecario.backend.servicio.api.cheques.ListadoChequesOB;
import ar.com.hipotecario.backend.servicio.api.productos.*;
import ar.com.hipotecario.backend.servicio.api.productos.TarjetasDebito.TarjetaDebito;
import ar.com.hipotecario.backend.servicio.api.tarjetaDebito.ApiTarjetaDebito;
import ar.com.hipotecario.backend.servicio.api.tarjetaDebito.TarjetasDebitos;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaCredito;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoTarjetaDebito;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOBLite;

public class OBTarjetas extends ModuloOB {
	public static final String REGEX_ESPACIOS = "\\s{2,}";
	private static final String REGEX_CUOTAS = "(\\s*(\\d{1,2}/\\d{1,2})\\s*)";
	public static final String[] CODIGOS_FINALIZADO = { "F", "D", "R" };
	private static final Integer multiplicadorTD = 6;
	private static final String HABILITADA="HABILITADA";

	/*
	public static Respuesta buscarTooltipConfiguracionTarjeta(ContextoHB contexto) {
		String tipoTarjeta = contexto.parametros.string("tipoTarjeta");
		Respuesta respuesta = new Respuesta();
		Objeto configuracionTarjeta = SqlHomebanking.findConfiguracionTarjeta(contexto.idCobis(), "TOOLTIP" + "_" + tipoTarjeta);
		respuesta.set("mostrarTooltipTC", configuracionTarjeta == null);
		return respuesta;
	}

	public static Respuesta agregarTooltipConfiguracionTarjeta(ContextoHB contexto) {
		String tipoTarjeta = contexto.parametros.string("tipoTarjeta");

		Objeto configuracionTarjeta = SqlHomebanking.findConfiguracionTarjeta(contexto.idCobis(), "TOOLTIP" + "_" + tipoTarjeta);
		if (configuracionTarjeta == null) {
			SqlHomebanking.saveConfiguracionTarjeta(contexto.idCobis(), "TOOLTIP" + "_" + tipoTarjeta);
		}

		return new Respuesta();
	}*/

	public static Object consolidadaTarjetasDebito(ContextoOB contexto) {
		try {
			ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);

			
			// UsuarioEmpresa que esta logueado
			EmpresaUsuarioOBLite usuarioLogin = servicioEmpresaUsuario.findByUsuarioEmpresaLite(contexto.sesion().usuarioOB, contexto.sesion().empresaOB).tryGet();
			
			// Si el usuario es administrador (rol 1), buscamos todos los usuarios de la empresa
			List<EmpresaUsuarioOBLite> usuariosEmpresa = new ArrayList<>();
			if(usuarioLogin.rol.rol_codigo == 1) {
				usuariosEmpresa = servicioEmpresaUsuario.findUsuariosByEmpresaLite(contexto.sesion().empresaOB).tryGet();
			} else {
				// Si el usuario logueado no es administrador solo devolvemos sus tarjetas
				usuariosEmpresa.add(usuarioLogin);
			}
			
			List<Objeto> tarjetasReturn = new ArrayList<>();
			
			for(EmpresaUsuarioOBLite empresaUsuario: usuariosEmpresa) {
				List<PosicionConsolidadV4> productos = ApiProductos.posicionConsolidadaV4(contexto, empresaUsuario.usuario.idCobis).tryGet();
				
				// Tracking tarjeta
				Objeto objetoTraking = OBDelivery.agregarTrackeoTarjetaDebitoDni(contexto,String.valueOf(empresaUsuario.usuario.numeroDocumento));
				Map<String, Object> mapTraking = objetoTraking != null ? (Map<String, Object>) objetoTraking.get("productsDelivery") : new HashMap<String,Object>();

				Map<PosicionConsolidadV4, Futuro<TarjetasDebitos>> listados = new HashMap<>();
				if( productos != null && !productos.isEmpty()) {
					productos.stream().forEach(p->{
						if( p.codigoProducto.equals(String.valueOf(EnumProductos.buscarPorCodigo(16).get().getCodigo()))){
						Futuro<TarjetasDebitos> tarjetas = ApiTarjetaDebito.tarjeta(contexto,p.numeroProducto);
						listados.put(p,tarjetas);
						}

					});
				}

				listados.forEach((key, value) -> {
					TarjetasDebitos tarjeta = value.get();
					PosicionConsolidadV4 p = (PosicionConsolidadV4) key;
					if(tarjeta != null && tarjeta.tipoTarjeta.contains("EM")) {
						// Validamos que sean tarjetas del tipo Empresa
						//consultamos a link si la tarjeta esta habilitada o no
						String estadoTarjetaDebitoLink = (String) consultaEstadoTarjetaDebitoLink(contexto,key.numeroProducto).get("estadoTarjeta");
						Objeto item = new Objeto();
						item.set("idProducto", p.codigoProducto);
						item.set("titularidad", p.descTitularidad);
						item.set("ultimos4digitos", Modulo.ultimos4digitos(p.numeroProducto));
						item.set("limiteCompra", tarjeta.limiteExtraccion.multiply(new BigDecimal(multiplicadorTD)));
						item.set("limiteExtraccion", tarjeta.limiteExtraccion);
						item.set("estadoATM", EnumEstadoTarjetaDebito.valueOf(tarjeta.estadoTarjeta).getDescripcion());
						item.set("estadoLink",estadoTarjetaDebitoLink);
						item.set("idProducto", p.codigoProducto);
						item.set("tracking",mapTraking.get(Modulo.ultimos4digitos(p.numeroProducto)));
						item.set("nombreTitular",empresaUsuario.usuario.nombre + " " + empresaUsuario.usuario.apellido);
						item.set("esTitular",usuarioLogin.usuario.codigo.equals(empresaUsuario.usuario.codigo));
						item.set("error", false);
						tarjetasReturn.add(item);
					} else if(tarjeta == null) {
						Objeto item = new Objeto();
						item.set("idProducto",p.codigoProducto);
						item.set("titularidad", p.descTitularidad);
						item.set("ultimos4digitos", Modulo.ultimos4digitos(p.numeroProducto));
						item.set("idProducto",p.codigoProducto);
						item.set("tracking",mapTraking.get(Modulo.ultimos4digitos(p.numeroProducto)));
						item.set("nombreTitular",empresaUsuario.usuario.nombre + " " + empresaUsuario.usuario.apellido);
						item.set("esTitular",usuarioLogin.usuario.codigo.equals(empresaUsuario.usuario.codigo));
						item.set("error", true);
						tarjetasReturn.add(item);
					}
				});
			}

			Objeto respuesta = respuesta("datos", tarjetasReturn);
	    	return respuesta;

		} catch (Exception e) {
			return Respuesta.error();
		}
	}
	

	public static Respuesta consultaAdicionalesPropias(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();
		boolean mostrarAdicionales = false;
		for (TarjetaCredito tarjetaCredito : contexto.tarjetasCreditoTitularConAdicionalesPropias()) {
			if (!tarjetaCredito.esTitular()) {
				Objeto item = new Objeto();
				item.set("id", tarjetaCredito.idEncriptado());
				item.set("tipo", tarjetaCredito.tipo());
				item.set("idTipo", tarjetaCredito.idTipo());
				item.set("ultimos4digitos", tarjetaCredito.ultimos4digitos());
				item.set("esTitular", tarjetaCredito.esTitular());
				item.set("titularidad", tarjetaCredito.titularidad());
				item.set("debitosPesosFormateado", tarjetaCredito.debitosPesosFormateado());
				item.set("debitosDolaresFormateado", tarjetaCredito.debitosDolaresFormateado());
				item.set("fechaHoy", new SimpleDateFormat("dd/MM").format(new Date()));
				item.set("fechaCierre", tarjetaCredito.fechaCierre("dd/MM"));
				item.set("fechaVencimiento", tarjetaCredito.fechaVencimiento("dd/MM"));
				item.set("formaPago", tarjetaCredito.formaPago());
				item.set("idPaquete", tarjetaCredito.idPaquete());
				item.set("nombre", tarjetaCredito.denominacionTarjeta());
				respuesta.add("tarjetasCredito", item);
				mostrarAdicionales = true;
			}
		}

		respuesta.set("mostrarAdicionales", mostrarAdicionales);

		return respuesta;
	}
	
	public static Object habilitarTarjetaDebito(ContextoOB contexto) {
		try {
			
			String idProducto = contexto.parametros.string("idProducto");
				
			String numeroTarjeta = "";
			
			if (idProducto == null || idProducto.isEmpty()) {
				return Respuesta.parametrosIncorrectos();
			}
			
			String idCobis = contexto.sesion().idCobis;
			
			// Para no llamar a la posicion consolidada ver si no se pueden poner las tarjetas en el contexto y despues tomarlas de ahi
			PosicionConsolidada productos = ApiProductos.posicionConsolidada(contexto, idCobis).get();
			
			for(TarjetaDebito tarjetaDebito : productos.tarjetasDebito) {
				if(tarjetaDebito.idProducto.equals(idProducto)) {
					numeroTarjeta = tarjetaDebito.numeroProducto;
				}
			}
	
			TarjetasDebitos tarjeta = ApiTarjetaDebito.habilitarTarjetaDebito(contexto, numeroTarjeta).get();
			Objeto respuesta = respuesta("datos", tarjeta);
	    	return respuesta;

		} catch (Exception e) {
			return Respuesta.error();
		}
	}
	
	public static Object limitesTarjetaDebito(ContextoOB contexto) {
		try {
			String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");
	
			if (idTarjetaDebito == null || idTarjetaDebito.isEmpty()) {
				return Respuesta.parametrosIncorrectos();
			}
			
			List<TarjetaDebito> tarjetas = tarjetasDebito(contexto, contexto.sesion().idCobis);
			
			Objeto limites = new Objeto();
			for(TarjetaDebito tarjetaDebito : tarjetas) {
				if(tarjetaDebito.idProducto.equals(idTarjetaDebito)) {
					TarjetasDebitos tarjeta = ApiTarjetaDebito.tarjeta(contexto, tarjetaDebito.numeroProducto).tryGet();
					
					if (tarjeta == null) {
						return Respuesta.estado("TARJETA_NO_ENCONTRADA");
					}
					
					limites.set("simboloMoneda", "$");
					limites.set("compra", tarjeta.limiteExtraccion.multiply(new BigDecimal(multiplicadorTD)));
					limites.set("compraFormateado", Formateador.importe(tarjeta.limiteExtraccion.multiply(new BigDecimal(multiplicadorTD))));
					limites.set("extraccion", tarjeta.limiteExtraccion);
					limites.set("extraccionFormateado", Formateador.importe(tarjeta.limiteExtraccion));
	
					if (tarjeta.limiteExtraccion.intValue() != 10000) {
						limites.add("opciones", new Objeto().set("id", "12").set("compraFormateado", "60.000")
								.set("extraccionFormateado", "10.000"));
					}
					if (tarjeta.limiteExtraccion.intValue() != 60000) {
						limites.add("opciones", new Objeto().set("id", "13").set("compraFormateado", "360.000")
								.set("extraccionFormateado", "60.000"));
					}
					if (tarjeta.limiteExtraccion.intValue() != 150000) {
						limites.add("opciones", new Objeto().set("id", "14").set("compraFormateado", "900.000")
								.set("extraccionFormateado", "150.000"));
					}
					if (tarjeta.limiteExtraccion.intValue() != 200000) {
						limites.add("opciones", new Objeto().set("id", "15").set("compraFormateado", "1.200.000")
								.set("extraccionFormateado", "200.000"));
					}
					if (tarjeta.limiteExtraccion.intValue() != 250000) {
						limites.add("opciones", new Objeto().set("id", "07").set("compraFormateado", "1.500.000")
								.set("extraccionFormateado", "250.000"));
					}
					if (tarjeta.limiteExtraccion.intValue() != 300000) {
						limites.add("opciones", new Objeto().set("id", "16").set("compraFormateado", "1.800.000")
								.set("extraccionFormateado", "300.000"));
					}
					if (tarjeta.limiteExtraccion.intValue() != 350000) {
						limites.add("opciones", new Objeto().set("id", "17").set("compraFormateado", "2.100.000")
								.set("extraccionFormateado", "350.000"));
					}
					if (tarjeta.limiteExtraccion.intValue() != 1000000) {
						limites.add("opciones", new Objeto().set("id", "11").set("compraFormateado", "6.000.000")
								.set("extraccionFormateado", "1.000.000"));
					}
				}
			}
	
			Objeto respuesta = respuesta("datos", limites);
	    	return respuesta;
		} catch (Exception e) {
			return Respuesta.error();
		}
	}
	
	public static Object modificarLimiteTarjetaDebito(ContextoOB contexto) {
		try {
			String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");
			String limiteExtraccion = contexto.parametros.string("limiteExtraccion");
				
			String numeroTarjeta = "";
			
			if (idTarjetaDebito == null || idTarjetaDebito.isEmpty() || limiteExtraccion == null || limiteExtraccion.isEmpty()) {
				return Respuesta.parametrosIncorrectos();
			}
			
			String idCobis = contexto.sesion().idCobis;
			
			// Para no llamar a la posicion consolidada ver si no se pueden poner las tarjetas en el contexto y despues tomarlas de ahi
			PosicionConsolidada productos = ApiProductos.posicionConsolidada(contexto, idCobis).get();
			
			for(TarjetaDebito tarjetaDebito : productos.tarjetasDebito) {
				if(tarjetaDebito.idProducto.equals(idTarjetaDebito)) {
					numeroTarjeta = tarjetaDebito.numeroProducto;
				}
			}
	
			ApiResponse responseTD = ApiTarjetaDebito.modificarLimiteTarjetaDebito(contexto, idCobis, numeroTarjeta, limiteExtraccion).get();
			
			if (responseTD.hayError()) {
				if (responseTD.string("codigo").equals("1831602")) {
					return Respuesta.estado("EXISTE_SOLICITUD");
				}
				if (responseTD.string("codigo").equals("40003")) {
					return Respuesta.estado("FUERA_HORARIO");
				}
				return Respuesta.error();
			}
			
			TarjetasDebitos tarjeta = responseTD.crear(TarjetasDebitos.class);
			
			Objeto respuesta = respuesta("datos", tarjeta);
	    	return respuesta;

		} catch (Exception e) {
			return Respuesta.error();
		}
	}
	
	public static Object blanquearPin(ContextoOB contexto) {
		String idTarjetaDebito = contexto.parametros.string("idTarjetaDebito");

		String numeroTarjeta = "";
		
		if (idTarjetaDebito == null || idTarjetaDebito.isEmpty()) {
			return Respuesta.parametrosIncorrectos();
		}
		
		String idCobis = contexto.sesion().idCobis;
		
		// Para no llamar a la posicion consolidada ver si no se pueden poner las tarjetas en el contexto y despues tomarlas de ahi
		PosicionConsolidada productos = ApiProductos.posicionConsolidada(contexto, idCobis).get();
		
		for(TarjetaDebito tarjetaDebito : productos.tarjetasDebito) {
			if(tarjetaDebito.idProducto.equals(idTarjetaDebito)) {
				numeroTarjeta = tarjetaDebito.numeroProducto;
			}
		}

		Respuesta respuestaPausado = verificarTarjetaDebitoPausada(numeroTarjeta, contexto);
		if (respuestaPausado != null)
			return respuestaPausado;

		ApiResponse response = ApiTarjetaDebito.tarjetaDebitoBlanquearPin(contexto, numeroTarjeta).get();
		if (response.hayError()) {
			if (response.string("codigo").equals("TARJETA_INVALIDA")) {
				return Respuesta.estado("TARJETA_INVALIDA");
			}
			return Respuesta.error();
		}
		return Respuesta.exito();

	}

	static List<TarjetaDebito> tarjetasDebito(ContextoOB contexto, String idCobis) {
		List<TarjetaDebito> tarjetasDebito = new ArrayList<>();
		PosicionConsolidada productos = ApiProductos.posicionConsolidada(contexto, idCobis).tryGet();
		for(TarjetaDebito tarjetaDebito : productos.tarjetasDebito) {
			if (!"C".equals(tarjetaDebito.estado)) {
				tarjetasDebito.add(tarjetaDebito);
			}
		}
		return tarjetasDebito;
	}
	
	static String adaptacionBusquedaDelivery(Long cuit) {
		//como el RM que se manda a link y/o link estan manejando mal el dni (colocan el cuit -2)
		// para poder hacer el seguimiento del delivery de tarjetas de debito, es necesario 
		// hacer estas modificaciones
		String cuil = String.valueOf(cuit);
		if (cuil.length() <= 2) {
			return "";
		}
		return cuil.substring(0, cuil.length() - 2);
	}
	
	static Respuesta verificarTarjetaDebitoPausada(String numeroTarjeta, ContextoOB contexto) {
		ApiResponse apiResponseTitularidad = ApiTarjetaDebito.obtenerTitularidadTd(contexto, numeroTarjeta, "N").get();
		if (!apiResponseTitularidad.hayError()
				&& apiResponseTitularidad.objetos("collection1").size() != 0
				&& apiResponseTitularidad.objetos("collection1").get(0).get("Pausada").equals("S"))
			return Respuesta.estado("TARJETA_PAUSADA");

		return null;
	}
	
	static Respuesta consultaEstadoTarjetaDebitoLink(ContextoOB contexto,String numeroTarjeta){
		try {
			ApiResponse response = ApiTarjetaDebito.consultaEstadoTarjetaDebitoLink(contexto,"0","0", "0", numeroTarjeta).get();
			if (response.hayError()) {
				return Respuesta.error();
			}
			return Respuesta.exito("estadoTarjeta",response.get("estadoTarjeta"));
		}catch(ApiException e) {
			return Respuesta.error();
		}
	}

}