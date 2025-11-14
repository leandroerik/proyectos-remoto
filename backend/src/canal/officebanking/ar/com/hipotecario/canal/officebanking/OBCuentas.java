package ar.com.hipotecario.canal.officebanking;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.base.Archivo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.catalogo.ApiCatalogo;
import ar.com.hipotecario.backend.servicio.api.catalogo.SucursalesOB;
import ar.com.hipotecario.backend.servicio.api.cuentas.ApiCuentas;
import ar.com.hipotecario.backend.servicio.api.cuentas.CajasAhorrosV1.CajaAhorroV1;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentasCorrientes.CuentaCorriente;
import ar.com.hipotecario.backend.servicio.api.cuentas.DeudasCuenta;
import ar.com.hipotecario.backend.servicio.api.cuentas.MovimientosCajaAhorros;
import ar.com.hipotecario.backend.servicio.api.cuentas.MovimientosCuenta;
import ar.com.hipotecario.backend.servicio.api.cuentas.MovimientosCuentaCorriente;
import ar.com.hipotecario.backend.servicio.api.cuentas.ResumenCuenta;
import ar.com.hipotecario.backend.servicio.api.empresas.ApiEmpresas;
import ar.com.hipotecario.backend.servicio.api.empresas.ConsultaPreConsorciosResponse;
import ar.com.hipotecario.backend.servicio.api.empresas.ConsultaPreConsorciosResponse.ConsultaPreConsorcio;
import ar.com.hipotecario.backend.servicio.api.notificaciones.ApiNotificaciones;
import ar.com.hipotecario.backend.servicio.api.notificaciones.EnvioEmail;
import ar.com.hipotecario.backend.servicio.api.productos.CuentasOB;
import ar.com.hipotecario.backend.servicio.api.productos.CuentasOB.CuentaOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumCuentasOB;
import ar.com.hipotecario.canal.officebanking.jpa.dto.InfoCuentaDTO;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioBeneficiarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioModoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioTerminosYCondicionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.CuentaOperadorOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.ModoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.TerminosYCondicionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;

public class OBCuentas extends ModuloOB {

	private static final String ULTIMOS_MOVIMIENTOS_DOC = "officebanking/movimientos-cuenta.docx";
	private static final String ULTIMOS_MOVIMIENTOS_XLS = "officebanking/movimientos-cuenta.xlsx";
	
	public static Object obtenerQRCuenta (ContextoOB contexto){
		SesionOB sesion = contexto.sesion();
		String cbu = contexto.parametros.string("cbu");
		String tipoCuenta = contexto.parametros.string("tipoCuenta");
		String cuit =String.valueOf(sesion.empresaOB.cuit);
		Objeto consultaQR;
		ServicioModoOB servicioModo = new ServicioModoOB(contexto);
        ModoOB modoOB = servicioModo.findByEmpCodigo(sesion.empresaOB.emp_codigo).tryGet();
		if(modoOB == null || (modoOB != null && modoOB.altaComercio == false)){
			 if(!OBEmpresas.altaComercioModoYActualizacionBandera(contexto,modoOB)) {
				 consultaQR = new Objeto(); 
				 return  consultaQR.set("datos",new Objeto().set("error",true));
		     }
		}		
		consultaQR = ApiCuentas.obtenerQRCuenta(contexto, cbu, cuit, tipoCuenta).get();
		return respuesta("datos", consultaQR);
	}

	public static  Object aceptarTerminosYCondiucionesQR (ContextoOB contexto) {
		SesionOB sesion = contexto.sesion();
		ServicioTerminosYCondicionesOB servivcioTerminosYCondiciones = new ServicioTerminosYCondicionesOB(contexto);
		String numeroCuenta = contexto.parametros.string("numeroCuenta");
		String producto = contexto.parametros.string("producto");
		TerminosYCondicionesOB terminosYCondiciones = new TerminosYCondicionesOB();

		terminosYCondiciones.empCodigo = sesion.empresaOB;
		terminosYCondiciones.numeroCuenta = numeroCuenta;
		terminosYCondiciones.producto = producto;
		terminosYCondiciones.tyc = true;

		TerminosYCondicionesOB resultadoGuardar = servivcioTerminosYCondiciones.crear(contexto, terminosYCondiciones).get();

		Objeto data = new Objeto();

		if(!empty(resultadoGuardar)){
			data.set("firmaTyC",true);
		}else {
			data.set("firmaTyC",true);
		}

		return respuesta("datos", data);
	}

	public static Object obtenerInfoAliasTyC (ContextoOB contexto) {
		String cbu = contexto.parametros.string("cbu");
		String numeroCuenta = contexto.parametros.string("numeroCuenta");
		SesionOB sesion = contexto.sesion();
		Objeto datoAlias = ApiCuentas.infoAlias(contexto, cbu, sesion.empresaOB.cuit.toString()).get();

		ServicioTerminosYCondicionesOB servivcioTerminosYCondiciones = new ServicioTerminosYCondicionesOB(contexto);
		Boolean resultadoTyC = servivcioTerminosYCondiciones.buscarPorEmpresaYCuenta(sesion.empresaOB, numeroCuenta).get();

		Objeto aliasTyC = new Objeto();
		aliasTyC.set("alias",datoAlias);
		aliasTyC.set("firmaTyC",resultadoTyC);

		return respuesta("datos", aliasTyC);
	}

	public static  Object modificarAliasCuenta (ContextoOB contexto){
		String alias = contexto.parametros.string("alias");
		String nuevoAlias = contexto.parametros.string("nuevoAlias");
		String cbu = contexto.parametros.string("cbu");
		String cuit = contexto.sesion().empresaOB.cuit.toString();
		Boolean tieneAlias = true;

		if (empty(cbu) || empty(alias)) {
			return respuesta("PARAMETROS_INVALIDOS");
		}
		//Se obtiene el alias asociado al cbu anterior
		ServicioBeneficiarioOB servicioInfo = new ServicioBeneficiarioOB(contexto);
		InfoCuentaDTO info = servicioInfo.infoCBUAlias(contexto, cbu);

		if(empty(info)) {
			tieneAlias = false;
		}

		Objeto valor = ApiCuentas.modificarOAsignarAlias(contexto, cbu, alias, nuevoAlias, cuit, tieneAlias).get();
		return respuesta("datos", valor);
	}

	public static Object CompartirCbuAlias(ContextoOB contexto){
		String email = contexto.parametros.string("email");
		String alias = contexto.parametros.string("alias");
		String cbu = contexto.parametros.string("cbu");
		String cuenta = contexto.parametros.string("cuenta");
		String tipoCuenta = contexto.parametros.string("tipoCuenta");
		String cuit = contexto.parametros.string("cuit");
		String titular = contexto.sesion().empresaOB.razonSocial;

		EnvioEmail resultadoEnvioEmail = ApiNotificaciones.envioDatosCuentaOB(contexto, email, cbu, alias, cuenta, titular, cuit, tipoCuenta).get();
		Objeto datos = new Objeto();
		if (resultadoEnvioEmail.codigoHttp() == 200) {
			datos.set("estado", true);
		}else {
			datos.set("estado", false);
		}
		return  datos;
	}

	public static Object cuentas(ContextoOB contexto) {
		SesionOB sesion = contexto.sesion();
		List<ConsultaPreConsorcio> nombresCuentas=null;
		Map<String, String> cuentaNombreMap = null;
		
		if (!contexto.esProduccion()) {
			LogOB.evento(contexto, "LOG_EMPRESA", new Objeto().set("EMPRESA", sesion.empresaOB.toString()));
		}
		
		Objeto tipoCuenta = (Objeto) validar_CuentaUnipersonal(contexto, sesion.empresaOB.cuit.toString());
		
		ConsultaPreConsorciosResponse consultaPreConsorciosResponse = ApiEmpresas.consultaPreConsorcios(contexto, "", "", contexto.sesion().empresaOB.idCobis).tryGet();
		
	    if (consultaPreConsorciosResponse != null && !consultaPreConsorciosResponse.isEmpty()) {
	    	try
	    	{
		        nombresCuentas = consultaPreConsorciosResponse.stream().toList();
		        cuentaNombreMap = nombresCuentas.stream().collect(Collectors.toMap(nc -> nc.cuenta, nc -> nc.nombreCuenta));
	    	}catch (Exception ignored) {}
	    }
		
		CuentasOB cuentas_OB = ApiCuentas.cuentas(contexto, sesion.empresaOB.idCobis).get();
		List<CuentaOB> cuentasOB = cuentas_OB.stream().toList();
		
		//Si la cuenta es unipersonal, se filtran solo las cuentas de ese usuario, para q no visualice las cuentas de las empresas
		if(tipoCuenta.get("estado").equals(EnumCuentasOB.CUENTA_UNIPERSONAL.getCodigo())) {
			cuentasOB = cuentasOB.stream().filter(c -> c.tipoTitularidad.equals(EnumCuentasOB.TIPO_TITULARIDAD.getCodigo())).toList();
		}

		if (!contexto.esProduccion()) {
			LogOB.evento(contexto, "TIENE_SESION", new Objeto().set("SESION", sesion.esOperador().toString()).set("ROL", sesion.rol.toString()));
		}

		UsuarioOB usuarioOB = sesion.usuarioOB;
		if (empty(usuarioOB)) {
			return respuesta("DNI_INVALIDO");
		}

		if (!contexto.esProduccion()) {
			LogOB.evento(contexto, "cuentasOB", new Objeto().set("cuentasOB", cuentasOB));
		}

		DeudasCuenta deudas = ApiCuentas.deudasCuenta(contexto, sesion.empresaOB.idCobis, true, "0").tryGet();

		Objeto cuentas = new Objeto();
		for (CuentaOB c : cuentasOB) {
			Objeto cuenta = new Objeto();
			if (c.tipoProducto.equals("CTE")) {
				CuentaCorriente detalleCuenta = ApiCuentas.cuentaCorriente(contexto, c.numeroProducto, Fecha.ahora(), false, false).get();
				cuenta.set("cbu", detalleCuenta.cbu);
				cuenta.set("saldoGirar", detalleCuenta.saldoGirar);
				cuenta.set("saldoContable",detalleCuenta.disponible);
				cuenta.set("saldoDisponible", detalleCuenta.disponible);
				cuenta.set("saldoAcuerdo", detalleCuenta.acuerdo);

				List<Objeto> listadoDeAcuerdos = new ArrayList<>();

				if(deudas != null){
					deudas.forEach(deuda -> {
						String[] numeroDeCuentaAcuerdo = deuda.numeroProducto.split("-");
						Objeto acuerdo = new Objeto();
						if(numeroDeCuentaAcuerdo.length > 1 && c.numeroProducto.equals(numeroDeCuentaAcuerdo[0])){
							acuerdo.set("moneda",deuda.moneda);
							acuerdo.set("fechaVencimiento",deuda.fechaVencimiento);
							acuerdo.set("monto",deuda.monto);
							acuerdo.set("titulo",deuda.descripcionGarantias);
							listadoDeAcuerdos.add(acuerdo);
						}
					});
				}

				cuenta.set("listadoDeAcuerdos",listadoDeAcuerdos);
			} else {
				CajaAhorroV1 cajaAhorro = ApiCuentas.cajaAhorroV1(contexto, c.numeroProducto, Fecha.ahora()).get();
				cuenta.set("cbu", cajaAhorro.cbu);
				cuenta.set("saldoGirar", cajaAhorro.saldoGirar);
				cuenta.set("saldoContable",cajaAhorro.disponible);
				cuenta.set("saldoDisponible", cajaAhorro.disponible);
				cuenta.set("saldoContable",cajaAhorro.saldoGirar);
			}
					
			cuenta.set("sucursal",c.sucursal);
			cuenta.set("descTipoTitularidad",c.descTipoTitularidad);
			cuenta.set("tipoTitularidad",c.tipoTitularidad);
			cuenta.set("tipoProducto", c.tipoProducto);
			cuenta.set("numeroProducto", c.numeroProducto);
			cuenta.set("descMoneda", c.descMoneda);
			cuenta.set("moneda", c.moneda);
			cuenta.set("descEstado", c.descEstado);
			cuenta.set("estado", c.estado);
			cuenta.set("tipoTitularidad", c.tipoTitularidad);
			
			if (cuentaNombreMap != null) {
			    cuenta.set("nombreCuenta", cuentaNombreMap.get(c.numeroProducto));
			}
			
			cuentas.add(cuenta);
		}

		Objeto datos = new Objeto();
		datos.set("cuentas", cuentas);
		return respuesta("datos", datos);
	}

	public static Object cuentasHabilitadas(ContextoOB contexto) {
		SesionOB sesion = contexto.sesion();
		List<ConsultaPreConsorcio> nombresCuentas=null;
		Map<String, String> cuentaNombreMap = null;
		
		if (!contexto.esProduccion()) {
			LogOB.evento(contexto, "LOG_EMPRESA", new Objeto().set("EMPRESA", sesion.empresaOB.toString()));
		}
		
		Objeto tipoCuenta = (Objeto) validar_CuentaUnipersonal(contexto, sesion.empresaOB.cuit.toString());
		
		ConsultaPreConsorciosResponse consultaPreConsorciosResponse = ApiEmpresas.consultaPreConsorcios(contexto, "", "", contexto.sesion().empresaOB.idCobis).tryGet();
		
	    if (consultaPreConsorciosResponse != null && !consultaPreConsorciosResponse.isEmpty()) {
	    	try
	    	{
		        nombresCuentas = consultaPreConsorciosResponse.stream().toList();
		        cuentaNombreMap = nombresCuentas.stream().collect(Collectors.toMap(nc -> nc.cuenta, nc -> nc.nombreCuenta));
	    	}catch (Exception ignored) {}
	    }
		
		CuentasOB cuentas_OB = ApiCuentas.cuentas(contexto, sesion.empresaOB.idCobis).get();
		List<CuentaOB> cuentasOB = cuentas_OB.stream().toList();
		
		//Si la cuenta es unipersonal, se filtran solo las cuentas de ese usuario, para q no visualice las cuentas de las empresas
		if(tipoCuenta.get("estado").equals(EnumCuentasOB.CUENTA_UNIPERSONAL.getCodigo())) {
			cuentasOB = cuentasOB.stream().filter(c -> c.tipoTitularidad.equals(EnumCuentasOB.TIPO_TITULARIDAD.getCodigo())).toList();
		}

		if (!contexto.esProduccion()) {
			LogOB.evento(contexto, "TIENE_SESION", new Objeto().set("SESION", sesion.esOperador().toString()).set("ROL", sesion.rol.toString()));
		}

		UsuarioOB usuarioOB = sesion.usuarioOB;
		if (empty(usuarioOB)) {
			return respuesta("DNI_INVALIDO");
		}

		Set<String> cuentasHabilitadas = null;
		if (sesion.esOperador()) {
			EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, usuarioOB);

			if (empty(empresaUsuario)) {
				if (!contexto.esProduccion()) {
					LogOB.evento(contexto, "OPERADOR INVALIDO");
				}
				return respuesta("OPERADOR_INVALIDO");
			}

			List<CuentaOperadorOB> cuentasOperador = empresaUsuario.cuentas;
			List<CuentaOperadorOB> cuentas = cuentasOperador.stream().collect(Collectors.toList());
			cuentasHabilitadas = cuentas.stream().map(c -> c.numeroCuenta.toString()).collect(Collectors.toSet());

			if (!contexto.esProduccion()) {
				LogOB.evento(contexto, "cuentasHabilitadas", new Objeto().set("cuentasHabilitadas", cuentasHabilitadas));
			}
		}
		if (!contexto.esProduccion()) {
			LogOB.evento(contexto, "cuentasOB", new Objeto().set("cuentasOB", cuentasOB));
		}

		DeudasCuenta deudas = ApiCuentas.deudasCuenta(contexto, sesion.empresaOB.idCobis, true, "0").tryGet();

		Objeto cuentas = new Objeto();
		for (CuentaOB c : cuentasOB) {
			Boolean cuentaHabilitada = sesion.esOperadorInicial() || cuentasHabilitadas.contains(c.numeroProducto);
			if (cuentaHabilitada) {
				Objeto cuenta = new Objeto();
				if (c.tipoProducto.equals("CTE")) {
					CuentaCorriente detalleCuenta = ApiCuentas.cuentaCorriente(contexto, c.numeroProducto, Fecha.ahora(), false, false).get();
					cuenta.set("cbu", detalleCuenta.cbu);
					cuenta.set("saldoGirar", detalleCuenta.saldoGirar);
					cuenta.set("saldoContable",detalleCuenta.disponible);
					cuenta.set("saldoDisponible", detalleCuenta.disponible);
					cuenta.set("saldoAcuerdo", detalleCuenta.acuerdo);

					List<Objeto> listadoDeAcuerdos = new ArrayList<>();

					if(deudas != null){
						deudas.forEach(deuda -> {
							String[] numeroDeCuentaAcuerdo = deuda.numeroProducto.split("-");
							Objeto acuerdo = new Objeto();
							if(numeroDeCuentaAcuerdo.length > 1 && c.numeroProducto.equals(numeroDeCuentaAcuerdo[0])){
								acuerdo.set("moneda",deuda.moneda);
								acuerdo.set("fechaVencimiento",deuda.fechaVencimiento);
								acuerdo.set("monto",deuda.monto);
								acuerdo.set("titulo",deuda.descripcionGarantias);
								listadoDeAcuerdos.add(acuerdo);
							}
						});
					}

					cuenta.set("listadoDeAcuerdos",listadoDeAcuerdos);
				} else {
					CajaAhorroV1 cajaAhorro = ApiCuentas.cajaAhorroV1(contexto, c.numeroProducto, Fecha.ahora()).get();
					cuenta.set("cbu", cajaAhorro.cbu);
					cuenta.set("saldoGirar", cajaAhorro.saldoGirar);
					cuenta.set("saldoContable",cajaAhorro.disponible);
					cuenta.set("saldoDisponible", cajaAhorro.disponible);
					cuenta.set("saldoContable",cajaAhorro.saldoGirar);
				}
					
				cuenta.set("sucursal",c.sucursal);
				cuenta.set("descTipoTitularidad",c.descTipoTitularidad);
				cuenta.set("tipoTitularidad",c.tipoTitularidad);
				cuenta.set("tipoProducto", c.tipoProducto);
				cuenta.set("numeroProducto", c.numeroProducto);
				cuenta.set("descMoneda", c.descMoneda);
				cuenta.set("moneda", c.moneda);
				cuenta.set("descEstado", c.descEstado);
				cuenta.set("estado", c.estado);
				cuenta.set("tipoTitularidad", c.tipoTitularidad);
			
			if (cuentaNombreMap != null) {
			    cuenta.set("nombreCuenta", cuentaNombreMap.get(c.numeroProducto));
			}
				cuenta.set("estadoCuenta", c.estadoCuenta);
				cuentas.add(cuenta);
			}
		}

		Objeto datos = new Objeto();
		datos.set("cuentas", cuentas);
		return respuesta("datos", datos);
	}


	public static Objeto cuenta(ContextoOB contexto, String numeroProducto) {
		Objeto datos = (Objeto) OBCuentas.cuentas(contexto);
		Objeto cuentas = (Objeto) datos.get("datos.cuentas");
		Optional<Objeto> cuenta = cuentas.objetos().stream().filter(c -> c.get("numeroProducto").toString().equals(numeroProducto)).findFirst();
		if (cuenta.isPresent()) {
			return cuenta.get();
		}
		return null;
	}

	public static Object cuentaCorriente(ContextoOB contexto) {
		String numeroCuenta = contexto.parametros.string("numeroCuenta");

		if (!OBCuentas.habilitada(contexto, numeroCuenta)) {
			return respuesta("CUENTA_INVALIDA");
		}

		CuentaCorriente detalle = ApiCuentas.cuentaCorriente(contexto, numeroCuenta, Fecha.ahora(), false, false).get();

		Objeto cuenta = new Objeto();
		cuenta.set("cbu", detalle.cbu);
		cuenta.set("moneda", detalle.moneda);
		cuenta.set("descMoneda", detalle.descMoneda);
		cuenta.set("saldoGirar", detalle.saldoGirar);

		Objeto datos = new Objeto();
		datos.set("cuenta", cuenta);
		return respuesta("datos", datos);
	}

	public static Object cajaAhorro(ContextoOB contexto) {
		String numeroCuenta = contexto.parametros.string("numeroCuenta");

		if (!OBCuentas.habilitada(contexto, numeroCuenta)) {
			return respuesta("CUENTA_INVALIDA");
		}

		CajaAhorroV1 cajaAhorro = ApiCuentas.cajaAhorroV1(contexto, numeroCuenta, Fecha.ahora()).get();

		Objeto cuenta = new Objeto();
		cuenta.set("cbu", cajaAhorro.cbu);
		cuenta.set("moneda", cajaAhorro.moneda);
		cuenta.set("descMoneda", cajaAhorro.descMoneda);
		cuenta.set("saldoGirar", cajaAhorro.saldoGirar);

		Objeto datos = new Objeto();
		datos.set("cuenta", cuenta);
		return respuesta("datos", datos);
	}

	public static Object ultimosMovimientos(ContextoOB contexto) {
		Objeto respuesta = (Objeto) OBCuentas.cuentas(contexto);
		List<Objeto> cuentas = ((Objeto) respuesta.get("datos.cuentas")).objetos();
		MovimientosCuenta movimientos = new MovimientosCuenta();
		if (cuentas.size() > 0) {
			String strCuentas = cuentas.stream()
					.map(c -> c.get("numeroProducto").toString())
					.filter(numeroCuenta -> habilitada(contexto, numeroCuenta))
					.collect(Collectors.joining(","));
			if(!strCuentas.equals(",")){
			movimientos = ApiCuentas.movimientoCuentas(contexto, strCuentas, "N", "N", "N").get();
			}
			}

		return respuesta("datos", movimientos);
	}

	public static Object filtroMovimientos(ContextoOB contexto) {
		String numeroCuenta = contexto.parametros.string("numeroCuenta");
		Fecha fechaDesde = contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd");
		Fecha fechaHasta = contexto.parametros.fecha("fechaHasta", "yyyy-MM-dd");
		String numeroPagina = contexto.parametros.string("numeroPagina", "1");
		Character orden = contexto.parametros.string("orden", "D").charAt(0);
		String concepto = contexto.parametros.string("concepto", null);
		if (!OBCuentas.habilitada(contexto, numeroCuenta)) {
			return respuesta("CUENTA_INVALIDA");
		}

		if (Math.abs(fechaDesde.diasTranscurridos(Fecha.ahora())) >= 365) {
			return respuesta("NO SE PUEDEN CONSULTAR MOVIMIENTOS ANTERIORES AL DÍA " + Fecha.ahora().restarAños(1).sumarDias(1).removerHora());
		}
		Integer pendientes = 0;
		Character tipoMovimiento = 'T';
		Boolean validActaEmpleado = false;
		Character diaHabil = 'N';
		Integer nPagina = Integer.valueOf(String.valueOf(numeroPagina));
		int fromIndex = (nPagina - 1) * 15;
		if (numeroCuenta != null && numeroCuenta.startsWith("3")) {
			MovimientosCuentaCorriente cuentasCorrientesMovimientos = ApiCuentas.movimientosCuentaCorriente(contexto, numeroCuenta, fechaDesde, fechaHasta, numeroPagina, orden, pendientes, tipoMovimiento, validActaEmpleado, diaHabil, concepto).get();
			List<MovimientosCuentaCorriente.MovimientoCuentaCorriente> listadoDeMovimientosSinFiltrado = cuentasCorrientesMovimientos.stream().toList();

			List<MovimientosCuentaCorriente.MovimientoCuentaCorriente> ultimosMovimientos = concepto != null ? cuentasCorrientesMovimientos.stream().filter(movimiento -> String.valueOf(movimiento.referencia).toLowerCase().contains(concepto.toLowerCase())
														|| String.valueOf(movimiento.sucursalCP).toLowerCase().contains(concepto.toLowerCase())
														|| String.valueOf(movimiento.descCausa).toLowerCase().contains(concepto.toLowerCase())
														|| String.valueOf(movimiento.descripcionMovimiento).toLowerCase().contains(concepto.toLowerCase())
														|| String.valueOf(movimiento.valor).toLowerCase().contains(concepto.toLowerCase())).collect(Collectors.toList()) : listadoDeMovimientosSinFiltrado;
			ultimosMovimientos = ultimosMovimientos.stream().filter(movimiento-> movimiento.fecha.fechaDate().compareTo(fechaDesde.fechaDate())>=0&&movimiento.fecha.fechaDate().compareTo(fechaHasta.fechaDate())<=0).toList();
			if(fromIndex > ultimosMovimientos.size()){
				fromIndex = 0;
			}

			int toIndex = Math.min(fromIndex + 15, ultimosMovimientos.size());
			List<MovimientosCuentaCorriente.MovimientoCuentaCorriente> listadoDeMovimientoPagina = ultimosMovimientos.subList(fromIndex, toIndex);

			Objeto paginado = new Objeto();
			List<Objeto> movimientos = new ArrayList<>();

			if(concepto == null){
				paginado.set("cantPaginas", cuentasCorrientesMovimientos.cantPaginas);
				paginado.set("numPagina", cuentasCorrientesMovimientos.numPagina);
			}else{
				int cantPaginas = (int) Math.ceil((double) ultimosMovimientos.size() / 15);
				paginado.set("cantPaginas", cantPaginas);
				paginado.set("numPagina", Integer.valueOf(numeroPagina));
			}

			for (MovimientosCuentaCorriente.MovimientoCuentaCorriente cc : listadoDeMovimientoPagina) {
				Objeto movimiento = new Objeto();
				movimiento.set("referencia", cc.referencia);
				movimiento.set("sucursalCP", cc.sucursalCP);
				movimiento.set("descCausa", cc.descCausa);
				movimiento.set("descripcion", cc.descripcionMovimiento);
				movimiento.set("fecha", cc.fecha.string("dd/MM/yyyy"));
				movimiento.set("importe", cc.valor);
				movimiento.set("saldo", !empty(cc.saldo) ? cc.saldo : "");

				movimientos.add(movimiento);
			}
			Objeto respuesta = new Objeto();
			respuesta.set("paginado", paginado);
			respuesta.set("movimientos", movimientos);
			respuesta.set("tipoCuenta", "CTE");
			return respuesta("datos", respuesta);
		} else {
			MovimientosCajaAhorros cajasAhorrosMovimientos = ApiCuentas.movimientosCajaAhorros(contexto, numeroCuenta, fechaDesde, fechaHasta, numeroPagina, orden, pendientes, tipoMovimiento, validActaEmpleado, diaHabil, concepto).get();
			List<MovimientosCajaAhorros.MovimientoCajaAhorro> listadoDeMovimientosSinFiltrado = cajasAhorrosMovimientos.stream().toList();

			List<MovimientosCajaAhorros.MovimientoCajaAhorro> ultimosMovimientos = concepto != null ? cajasAhorrosMovimientos.stream().filter(movimiento -> String.valueOf(movimiento.referencia).toLowerCase().contains(concepto.toLowerCase())
					|| String.valueOf(movimiento.sucursalCP).toLowerCase().contains(concepto.toLowerCase())
					|| String.valueOf(movimiento.descCausa).toLowerCase().contains(concepto.toLowerCase())
					|| String.valueOf(movimiento.descripcionMovimiento).toLowerCase().contains(concepto.toLowerCase())
					|| String.valueOf(movimiento.valor).toLowerCase().contains(concepto.toLowerCase())).collect(Collectors.toList()) : listadoDeMovimientosSinFiltrado;
			ultimosMovimientos = ultimosMovimientos.stream().filter(movimiento-> movimiento.fecha.fechaDate().compareTo(fechaDesde.fechaDate())>=0&&movimiento.fecha.fechaDate().compareTo(fechaHasta.fechaDate())<=0).toList();

			if(fromIndex > ultimosMovimientos.size()){
				fromIndex = 0;
			}

			int toIndex = Math.min(fromIndex + 15, ultimosMovimientos.size());
			List<MovimientosCajaAhorros.MovimientoCajaAhorro> listadoDeMovimientoPagina = ultimosMovimientos.subList(fromIndex, toIndex);

			Objeto paginado = new Objeto();
			List<Objeto> movimientos = new ArrayList<>();

			if(concepto == null){
				paginado.set("cantPaginas", cajasAhorrosMovimientos.cantPaginas);
				paginado.set("numPagina", cajasAhorrosMovimientos.numPagina);
			}else{
				int cantPaginas = (int) Math.ceil((double) ultimosMovimientos.size() / 15);
				paginado.set("cantPaginas", cantPaginas);
				paginado.set("numPagina", Integer.valueOf(numeroPagina));
			}

			for (MovimientosCajaAhorros.MovimientoCajaAhorro ca : listadoDeMovimientoPagina) {
				Objeto movimiento = new Objeto();
				movimiento.set("referencia", ca.referencia);
				movimiento.set("sucursalCP", ca.sucursalCP);
				movimiento.set("descCausa", ca.descCausa);
				movimiento.set("descripcion", ca.descripcionMovimiento);
				movimiento.set("fecha", ca.fecha.string("dd/MM/yyyy"));
				movimiento.set("importe", ca.valor);
				movimiento.set("saldo", ca.saldo);
				movimientos.add(movimiento);
			}
			Objeto respuesta = new Objeto();
			respuesta.set("paginado", paginado);
			respuesta.set("movimientos", movimientos);
			respuesta.set("tipoCuenta", "CA");
			return respuesta("datos", respuesta);
		}
	}

	public static Object descargaMovimientos(ContextoOB contexto) {
		String formatoArchivo = contexto.parametros.type("formato", "xls", "pdf", "txt");
		String numeroCuenta = contexto.parametros.string("numeroCuenta");
		Fecha fechaDesde = contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd");
		Fecha fechaHasta = contexto.parametros.fecha("fechaHasta", "yyyy-MM-dd");
		String concepto = contexto.parametros.string("concepto", null);
		Character orden = contexto.parametros.string("orden", "D").charAt(0);
		
		SesionOB sesion = contexto.sesion();
		
		Integer pendientes = 0;
		Character tipoMovimiento = 'T';
		Boolean validActaEmpleado = false;
		Character diaHabil = 'N';
		String tipoCuenta = null;
		
		if (empty(sesion.empresaOB.idCobis)) {
			return respuesta("IDCOBIS_INVALIDO");
		}

		if (!OBCuentas.habilitada(contexto, numeroCuenta)) {
			return respuesta("CUENTA_INVALIDA");
		}

		Objeto lstMovimientos = new Objeto();

		SucursalesOB sucursales = ApiCatalogo.sucursalesOB(contexto, null, null, null).get();
		
		int fromIndex = 0;
		if (numeroCuenta != null && numeroCuenta.startsWith("3")) {
			TreeMap<Integer, List<MovimientosCuentaCorriente.MovimientoCuentaCorriente>> mapa = new TreeMap<>();
			
			tipoCuenta = "CTE";
			MovimientosCuentaCorriente cuentasCorrientesMovimientos = ApiCuentas.movimientosCuentaCorriente(contexto, numeroCuenta, fechaDesde, fechaHasta, "0", orden, pendientes, tipoMovimiento, validActaEmpleado, diaHabil, concepto).get();
			List<MovimientosCuentaCorriente.MovimientoCuentaCorriente> listadoDeMovimientosSinFiltrado = cuentasCorrientesMovimientos.stream().toList();

			List<MovimientosCuentaCorriente.MovimientoCuentaCorriente> ultimosMovimientos = concepto != null ? cuentasCorrientesMovimientos.stream().filter(movimiento -> String.valueOf(movimiento.referencia).toLowerCase().contains(concepto.toLowerCase())
														|| String.valueOf(movimiento.sucursalCP).toLowerCase().contains(concepto.toLowerCase())
														|| String.valueOf(movimiento.descCausa).toLowerCase().contains(concepto.toLowerCase())
														|| String.valueOf(movimiento.descripcionMovimiento).toLowerCase().contains(concepto.toLowerCase())
														|| String.valueOf(movimiento.valor).toLowerCase().contains(concepto.toLowerCase())).collect(Collectors.toList()) : listadoDeMovimientosSinFiltrado;

			ultimosMovimientos = ultimosMovimientos.stream().filter(movimiento-> movimiento.fecha.fechaDate().compareTo(fechaDesde.fechaDate())>=0&&movimiento.fecha.fechaDate().compareTo(fechaHasta.fechaDate())<=0).toList();
			int toIndex = ultimosMovimientos.size() >= 15 ? 15 : ultimosMovimientos.size();
						
			int cantPaginasTotal = (int) Math.ceil((double) ultimosMovimientos.size() / 15);
			int cantPaginas = cantPaginasTotal > 600 ? 600 : cantPaginasTotal;
			for (Integer i = 1; i <= cantPaginas; ++i) {				
				mapa.put(i, ultimosMovimientos.subList(fromIndex, toIndex));				
				
				fromIndex = fromIndex + 15;
				toIndex = ultimosMovimientos.size() - toIndex >= 15 ? (toIndex + 15) : (toIndex + (ultimosMovimientos.size() - toIndex));
			}

				mapa.values().stream().forEach(pagina->{
					List<Objeto> movimientosFila = new ArrayList<Objeto>();
					pagina.stream().forEach(m->{
						Objeto o = new Objeto();
						String desSucursal = "";

						if (!m.oficina.isEmpty()) {
							Optional<SucursalesOB.SucursalOB> sucursalOpt = sucursales.stream()
									.filter(sucu -> sucu.CodSucursal.equals(m.oficina))
									.findFirst();
							if (sucursalOpt.isPresent()) {
								SucursalesOB.SucursalOB sucursal = sucursalOpt.get();
								desSucursal = sucursal.DesSucursal;
							}
						}

						o.set("descripcion", m.descripcionMovimiento);
						o.set("fecha", m.fecha.string("dd/MM/yyyy"));
						o.set("credito", m.valor.compareTo(BigDecimal.ZERO) > 0 ? m.valor : 0);
						o.set("debito", m.valor.compareTo(BigDecimal.ZERO) < 0 ? m.valor.multiply(BigDecimal.valueOf(-1)) : 0);
						o.set("importe", m.valor);
						o.set("saldo", m.saldo);
						o.set("referencia", m.referencia);
						o.set("sucursal", m.oficina);

						movimientosFila.add(o);
					});
					List<Objeto> movimientos = (List<Objeto>) movimientosFila;
					movimientos.stream().forEach(mov -> lstMovimientos.add(mov));
				});



		}else {
			tipoCuenta = "CA";
			TreeMap<Integer, List<MovimientosCajaAhorros.MovimientoCajaAhorro>> mapa = new TreeMap<>();
			MovimientosCajaAhorros cajasAhorrosMovimientos = ApiCuentas.movimientosCajaAhorros(contexto, numeroCuenta, fechaDesde, fechaHasta, "0", orden, pendientes, tipoMovimiento, validActaEmpleado, diaHabil, concepto).get();
			List<MovimientosCajaAhorros.MovimientoCajaAhorro> listadoDeMovimientosSinFiltrado = cajasAhorrosMovimientos.stream().toList();

			List<MovimientosCajaAhorros.MovimientoCajaAhorro> ultimosMovimientos = concepto != null ? cajasAhorrosMovimientos.stream().filter(movimiento -> String.valueOf(movimiento.referencia).toLowerCase().contains(concepto.toLowerCase())
						|| String.valueOf(movimiento.sucursalCP).toLowerCase().contains(concepto.toLowerCase())
						|| String.valueOf(movimiento.descCausa).toLowerCase().contains(concepto.toLowerCase())
						|| String.valueOf(movimiento.descripcionMovimiento).toLowerCase().contains(concepto.toLowerCase())
						|| String.valueOf(movimiento.valor).toLowerCase().contains(concepto.toLowerCase())).collect(Collectors.toList()) : listadoDeMovimientosSinFiltrado;

			ultimosMovimientos = ultimosMovimientos.stream().filter(movimiento-> movimiento.fecha.fechaDate().compareTo(fechaDesde.fechaDate())>=0&&movimiento.fecha.fechaDate().compareTo(fechaHasta.fechaDate())<=0).toList();
		
			int toIndex = ultimosMovimientos.size() >= 15 ? 15 : ultimosMovimientos.size();
			
			int cantPaginasTotal = (int) Math.ceil((double) ultimosMovimientos.size() / 15);
			int cantPaginas = cantPaginasTotal > 600 ? 600 : cantPaginasTotal;
			for (Integer i = 1; i <= cantPaginas; ++i) {				
				mapa.put(i, ultimosMovimientos.subList(fromIndex, toIndex));				
				
				fromIndex = fromIndex + 15;
				toIndex = ultimosMovimientos.size() - toIndex >= 15 ? (toIndex + 15) : (toIndex + (ultimosMovimientos.size() - toIndex));
			}

				mapa.values().stream().forEach(pagina->{
					List<Objeto> movimientosFila = new ArrayList<Objeto>();
					pagina.stream().forEach(m->{
						Objeto o = new Objeto();
						String desSucursal = "";

						if (!m.oficina.isEmpty()) {
							Optional<SucursalesOB.SucursalOB> sucursalOpt = sucursales.stream()
									.filter(sucu -> sucu.CodSucursal.equals(m.oficina))
									.findFirst();
							if (sucursalOpt.isPresent()) {
								SucursalesOB.SucursalOB sucursal = sucursalOpt.get();
								desSucursal = sucursal.DesSucursal;
							}
						}

						o.set("descripcion", m.descripcionMovimiento);
						o.set("fecha", m.fecha.string("dd/MM/yyyy"));
						o.set("credito", m.valor.compareTo(BigDecimal.ZERO) > 0 ? m.valor : 0);
						o.set("debito", m.valor.compareTo(BigDecimal.ZERO) < 0 ? m.valor.multiply(BigDecimal.valueOf(-1)) : 0);
						o.set("importe", m.valor);
						o.set("saldo", m.saldo);
						o.set("referencia", m.referencia);
						o.set("sucursal", m.oficina);

						movimientosFila.add(o);
					});
					List<Objeto> movimientos = (List<Objeto>) movimientosFila;
					movimientos.stream().forEach(mov -> lstMovimientos.add(mov));
				});

		}

		BigDecimal sumaTotalesDebito = lstMovimientos.objetos().stream().map(m -> new BigDecimal(m.get("debito").toString())).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal sumaTotalesCredito = lstMovimientos.objetos().stream().map(m -> new BigDecimal(m.get("credito").toString())).reduce(BigDecimal.ZERO, BigDecimal::add);
		
		CuentasOB cuentas = ApiCuentas.cuentas(contexto, sesion.empresaOB.idCobis).get();
		Optional<CuentaOB> cuenta = cuentas.stream().filter(c -> c.numeroProducto.equals(numeroCuenta)).findFirst();
		String moneda = cuenta.isPresent() ? cuenta.get().moneda.replace("80", "$").replace("2", "U$D") : "";

		Map<String, Object> parametros = new HashMap<>();
		parametros.put("TITULO", "Movimientos del " + fechaDesde.string("dd/MM/yyyy") + " al " + fechaHasta.string("dd/MM/yyyy"));
		parametros.put("MOVIMIENTOS", lstMovimientos);
		parametros.put("TOTAL_DEBITO", sumaTotalesDebito);
		parametros.put("TOTAL_CREDITO", sumaTotalesCredito);
		parametros.put("CUENTA", tipoCuenta + " " + moneda + " ****" + numeroCuenta.substring(11));
		parametros.put("SUCURSAL", "SUCURSAL");
		parametros.put("REFERENCIA", "REFERENCIA");
		parametros.put("DEBITO_MONEDA", "DEBITO EN " + moneda);
		parametros.put("CREDITO_MONEDA", "CREDITO EN " + moneda);
		parametros.put("SALDO_MONEDA", "SALDO EN " + moneda);

		Archivo archivo = null;
		String nombreArchivo = "mov_" + Fecha.hoy().string("yyyyMMdd") + "." + (formatoArchivo.equalsIgnoreCase("xls")?"csv":formatoArchivo);
		if (formatoArchivo.equals("xls")) {
			archivo = new Archivo(nombreArchivo,CsvOB.descargaMovimientos(parametros));
			contexto.response.header("Content-Disposition", "attachment; " + nombreArchivo);
			contexto.response.type("application/csv");
		} else if (formatoArchivo.equals("pdf")) {
			archivo = pdf(ULTIMOS_MOVIMIENTOS_DOC, nombreArchivo, parametros);
		} else if (formatoArchivo.equals("txt")) {
			archivo = txt(nombreArchivo, parametros);
		}
		return archivo;

	}

	private static Archivo xls(String plantilla, String nombreArchivo, Map<String, Object> parametros) {
		byte[] excel = ExcelOB.descargaMovimientos(plantilla, parametros);
		return new Archivo(nombreArchivo, excel);
	}

	public static Archivo pdf(String plantilla, String nombreArchivo, Map<String, Object> parametros) {
		byte[] pdf = PdfOB.generarPdf(plantilla, parametros);
		return new Archivo(nombreArchivo, pdf);
	}

	private static Archivo txt(String nombreArchivo, Map<String, Object> parametros) {
		byte[] txt = TxtOB.txtDescargaMovimientos(parametros);
		return new Archivo(nombreArchivo, txt);
	}

	public static Object descargaResumen(ContextoOB contexto) {

		String numeroCuenta = contexto.parametros.string("numeroCuenta");
		Fecha fechaDesde = contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd");
		Fecha fechaHasta = contexto.parametros.fecha("fechaHasta", "yyyy-MM-dd");
		String producto = contexto.parametros.string("producto");

		if (!OBCuentas.habilitada(contexto, numeroCuenta)) {
			return respuesta("CUENTA_INVALIDA");
		}

		ResumenCuenta cuentasResumen = ApiCuentas.resumenCuenta(contexto, numeroCuenta, fechaDesde, fechaHasta, producto).get();
		return new Archivo("resumen.pdf", cuentasResumen.file);
	}

	public static Boolean habilitada(ContextoOB contexto, String numeroCuenta) {
		SesionOB sesion = contexto.sesion();
		if (sesion.esOperadorInicial()) {
			CuentasOB cuentasOB = ApiCuentas.cuentas(contexto, sesion.empresaOB.idCobis).get();
			Optional<CuentaOB> cuentaOperador = cuentasOB.filter(cuenta -> cuenta.numeroProducto.equals(numeroCuenta)).findFirst();
			return cuentaOperador.isPresent() ? true : false;
		} else {
			EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);
			List<CuentaOperadorOB> cuentasOperador = empresaUsuario.cuentas;
			List<CuentaOperadorOB> cuentas = cuentasOperador.stream().collect(Collectors.toList());
			Set<String> cuentasHabilitadas = cuentas.stream().map(c -> c.numeroCuenta.toString()).collect(Collectors.toSet());
			return cuentasHabilitadas.contains(numeroCuenta);
		}
	}
		
	public static Object validarCuentaUnipersonal(ContextoOB contexto) {
		String cuit = contexto.parametros.string("cuit");
		try {
			return validar_CuentaUnipersonal(contexto, cuit);
		} catch (Exception e) {
			LogOB.evento(contexto, "validarCuentaUnipersonal", e.getMessage());
		}
		return respuesta("0");
	}
	
	public static Object validar_CuentaUnipersonal(ContextoOB contexto, String cuit) {
		if(cuit.startsWith("20") || cuit.startsWith("23") || cuit.startsWith("27")) {
			return respuesta(EnumCuentasOB.CUENTA_UNIPERSONAL.getCodigo());
		}else {
			return respuesta(EnumCuentasOB.CUENTA_EMPRESA.getCodigo());
		}
	}
	
	public static Object cuentasPorMoneda(ContextoOB contexto) {
		int idMoneda = contexto.parametros.integer("idMoneda");
		SesionOB sesion = contexto.sesion();
		List<ConsultaPreConsorcio> nombresCuentas=null;
		Map<String, String> cuentaNombreMap = null;
		
		if (!contexto.esProduccion()) {
			LogOB.evento(contexto, "LOG_EMPRESA", new Objeto().set("EMPRESA", sesion.empresaOB.toString()));
		}
		
		Objeto tipoCuenta = (Objeto) validar_CuentaUnipersonal(contexto, sesion.empresaOB.cuit.toString());
		CuentasOB cuentas_OB = ApiCuentas.cuentas(contexto, sesion.empresaOB.idCobis).get();
		List<CuentaOB> cuentasOB;

		if(idMoneda!=0){
			cuentasOB = cuentas_OB.stream().filter(c -> c.moneda.equals(String.valueOf(idMoneda))).toList();
		}else{
			cuentasOB = cuentas_OB.stream().toList();
		}

		
		//Si la cuenta es unipersonal, se filtran solo las cuentas de ese usuario, para q no visualice las cuentas de las empresas
		if(tipoCuenta.get("estado").equals(EnumCuentasOB.CUENTA_UNIPERSONAL.getCodigo())) {
			cuentasOB = cuentasOB.stream()
					.filter(c -> c.tipoTitularidad.equals(EnumCuentasOB.TIPO_TITULARIDAD.getCodigo()))
					.toList();
		}

		if (!contexto.esProduccion()) {
			LogOB.evento(contexto, "TIENE_SESION", new Objeto().set("SESION", sesion.esOperador().toString()).set("ROL", sesion.rol.toString()));
		}

		UsuarioOB usuarioOB = sesion.usuarioOB;
		if (empty(usuarioOB)) {
			return respuesta("DNI_INVALIDO");
		}

		if (!contexto.esProduccion()) {
			LogOB.evento(contexto, "cuentasOB", new Objeto().set("cuentasOB", cuentasOB));
		}

		
		ConsultaPreConsorciosResponse consultaPreConsorciosResponse = ApiEmpresas.consultaPreConsorcios(contexto, "", "", contexto.sesion().empresaOB.idCobis).tryGet();
		
	    if (consultaPreConsorciosResponse != null && !consultaPreConsorciosResponse.isEmpty()) {
	    	try
	    	{
		        nombresCuentas = consultaPreConsorciosResponse.stream().toList();
		        cuentaNombreMap = nombresCuentas.stream().collect(Collectors.toMap(nc -> nc.cuenta, nc -> nc.nombreCuenta));
	    	}catch (Exception ignored) {}
	    }
		
		Objeto cuentas = new Objeto();
		for (CuentaOB c : cuentasOB) {
			Objeto cuenta = new Objeto();
			if (c.tipoProducto.equals("CTE")) {
				CuentaCorriente detalleCuenta = ApiCuentas.cuentaCorriente(contexto, c.numeroProducto, Fecha.ahora(), false, false).get();
				cuenta.set("cbu", detalleCuenta.cbu);
				cuenta.set("saldoGirar", detalleCuenta.saldoGirar);
				cuenta.set("saldoContable",detalleCuenta.disponible);
				cuenta.set("saldoDisponible", detalleCuenta.disponible);
				cuenta.set("saldoAcuerdo", detalleCuenta.acuerdo);

			} else {
				CajaAhorroV1 cajaAhorro = ApiCuentas.cajaAhorroV1(contexto, c.numeroProducto, Fecha.ahora()).get();
				cuenta.set("cbu", cajaAhorro.cbu);
				cuenta.set("saldoGirar", cajaAhorro.saldoGirar);
				cuenta.set("saldoContable",cajaAhorro.disponible);
				cuenta.set("saldoDisponible", cajaAhorro.disponible);
				cuenta.set("saldoContable",cajaAhorro.saldoGirar);
			}
					
			cuenta.set("sucursal",c.sucursal);
			cuenta.set("descTipoTitularidad",c.descTipoTitularidad);
			cuenta.set("tipoTitularidad",c.tipoTitularidad);
			cuenta.set("tipoProducto", c.tipoProducto);
			cuenta.set("numeroProducto", c.numeroProducto);
			cuenta.set("descMoneda", c.descMoneda);
			cuenta.set("moneda", c.moneda);
			cuenta.set("descEstado", c.descEstado);
			cuenta.set("estado", c.estado);
			cuenta.set("tipoTitularidad", c.tipoTitularidad);
			
			if (cuentaNombreMap != null) {
			    cuenta.set("nombreCuenta", cuentaNombreMap.get(c.numeroProducto));
			}
			
			cuentas.add(cuenta);
		}

		Objeto datos = new Objeto();
		datos.set("cuentas", cuentas);
		return respuesta("datos", datos);
	}
	
}