package ar.com.hipotecario.canal.buhobank;

import java.math.BigDecimal;
import java.util.UUID;

import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.Sesion;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.catalogo.ApiCatalogo;
import ar.com.hipotecario.backend.servicio.api.catalogo.DiaBancario;
import ar.com.hipotecario.backend.servicio.api.catalogo.Paises.Pais;
import ar.com.hipotecario.backend.servicio.api.catalogo.Provincias.Provincia;
import ar.com.hipotecario.backend.servicio.api.cuentas.ApiCuentas;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentaLink;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentaLink.Titular;
import ar.com.hipotecario.backend.servicio.api.debin.ApiDebin;
import ar.com.hipotecario.backend.servicio.api.debin.DebinPlazoFijoWeb;
import ar.com.hipotecario.backend.servicio.api.personas.ApiPersonas;
import ar.com.hipotecario.backend.servicio.api.personas.Cliente;
import ar.com.hipotecario.backend.servicio.api.personas.Cuils.Cuil;
import ar.com.hipotecario.backend.servicio.api.personas.Domicilios;
import ar.com.hipotecario.backend.servicio.api.personas.Domicilios.Domicilio;
import ar.com.hipotecario.backend.servicio.api.personas.Domicilios.NuevoDomicilio;
import ar.com.hipotecario.backend.servicio.api.personas.Emails;
import ar.com.hipotecario.backend.servicio.api.personas.Emails.Email;
import ar.com.hipotecario.backend.servicio.api.personas.Informado;
import ar.com.hipotecario.backend.servicio.api.personas.Persona;
import ar.com.hipotecario.backend.servicio.api.personas.Relaciones;
import ar.com.hipotecario.backend.servicio.api.personas.Relaciones.Relacion;
import ar.com.hipotecario.backend.servicio.api.personas.Telefonos;
import ar.com.hipotecario.backend.servicio.api.personas.Telefonos.NuevoTelefono;
import ar.com.hipotecario.backend.servicio.api.personas.Telefonos.Telefono;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.ApiPlazosFijos;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.PlazoFijoWeb;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.SimulacionPlazoFijoWeb;
import ar.com.hipotecario.backend.servicio.api.productos.ApiProductos;
import ar.com.hipotecario.backend.servicio.api.productos.Productos;
import ar.com.hipotecario.backend.servicio.api.ventas.ApiVentas;
import ar.com.hipotecario.backend.servicio.api.ventas.Indicador;
import ar.com.hipotecario.backend.servicio.sql.SqlCampaniasWF;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.CiudadesWF.CiudadWF;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.SucursalesWF.SucursalWF;
import ar.com.hipotecario.backend.util.TasasUtil;

public class BBPlazoFijoWeb extends Modulo {

	public static Object configuracion(ContextoBB contexto) {
		contexto.deleteSesion();
		if (!contexto.esOpenShift()) {
			Objeto respuesta = new Objeto();
			respuesta.set("url", "http://localhost:8080");
			respuesta.set("prendidoPlazoFijoWeb", false);
			respuesta.set("prendidoPaquetes", false);
			respuesta.set("prendidoVU", false);
			return respuesta;
		}
		return null;
	}

	public static Object diasFeriados(ContextoBB contexto) {
		Objeto respuesta = new Objeto();
		respuesta.set("formatoFecha", "DD/MM/YYYY");
		respuesta.setList("listaFeriados");
		respuesta.set("statusCode", "200");

		if (contexto.modo("feriado")) {
			return respuesta.add("listaFeriados", Fecha.hoy().string("dd/MM/yyyy"));
		} else if (contexto.modoOffline()) {
			return respuesta;
		}

		DiaBancario diaCalendario = ApiCatalogo.diaBancario(contexto, Fecha.hoy()).tryGet();
		if (Fecha.hoy().esFinDeSemana() || (diaCalendario != null && !diaCalendario.esDiaHabil())) {
			respuesta.add("listaFeriados", Fecha.hoy().string("dd/MM/yyyy"));
		}
		return respuesta;
	}

	public static Object parametria(ContextoBB contexto) {
		Integer plazoMinimoPesos = contexto.config.integer("buhobank_plazofijoweb_plazoMinimoPesos", 36);
		Integer plazoMaximoPesos = contexto.config.integer("buhobank_plazofijoweb_plazoMaximoPesos", 2000);
		BigDecimal montoMinimoPesos = contexto.config.bigDecimal("buhobank_plazofijoweb_montoMinimoPesos", "5000");
		BigDecimal montoMaximoPesos = contexto.config.bigDecimal("buhobank_plazofijoweb_montoMaximoPesos", "1000000");

		Integer plazoMinimoUVAs = contexto.config.integer("buhobank_plazofijoweb_plazoMinimoUVAs", 180);
		Integer plazoMaximoUVAs = contexto.config.integer("buhobank_plazofijoweb_plazoMaximoUVAs", 2000);
		BigDecimal montoMinimoUVAs = contexto.config.bigDecimal("buhobank_plazofijoweb_montoMinimoUVAs", "10000");
		BigDecimal montoMaximoUVAs = contexto.config.bigDecimal("buhobank_plazofijoweb_montoMaximoUVAs", "1000000");

		if (contexto.modoOffline()) {
			Objeto respuesta = new Objeto();
			respuesta.set("tna", 20.40);
			respuesta.set("tea", TasasUtil.tea(20.40));
			respuesta.set("plazoMinimoSimulacion", plazoMinimoPesos);
			respuesta.set("plazoMaximoSimulacion", plazoMaximoPesos);
			respuesta.set("montoMinimoSimulacion", montoMinimoPesos);
			respuesta.set("montoMaximoSimulacion", montoMaximoPesos);
			respuesta.set("tnaUVA", 23.50);
			respuesta.set("teaUVA", TasasUtil.tea(23.50));
			respuesta.set("plazoMinimoSimulacionUVA", plazoMinimoUVAs);
			respuesta.set("plazoMaximoSimulacionUVA", plazoMaximoUVAs);
			respuesta.set("montoMinimoSimulacionUVA", montoMinimoUVAs);
			respuesta.set("montoMaximoSimulacionUVA", montoMaximoUVAs);
			respuesta.set("inicioVigencia", Fecha.hoy().string("yyyy-MM-dd'T'HH:mm:ss"));
			respuesta.set("finVigencia", Fecha.mañana().string("yyyy-MM-dd'T'HH:mm:ss"));
			respuesta.set("bb-parametria", "backend-unificado");
			return respuesta;
		}

		Futuro<SimulacionPlazoFijoWeb> futuroSimulacionPlazoFijoWebPesos = ApiPlazosFijos.simulacionPlazoFijoWeb(contexto, montoMinimoPesos, plazoMinimoPesos, PlazoFijoWeb.PESOS);
		Futuro<SimulacionPlazoFijoWeb> futuroSimulacionPlazoFijoWebUVAs = ApiPlazosFijos.simulacionPlazoFijoWeb(contexto, montoMinimoUVAs, plazoMinimoUVAs, PlazoFijoWeb.UVAS);

		SimulacionPlazoFijoWeb simulacionPlazoFijoWebPesos = futuroSimulacionPlazoFijoWebPesos.tryGet();
		SimulacionPlazoFijoWeb simulacionPlazoFijoWebUVAs = futuroSimulacionPlazoFijoWebUVAs.tryGet();

		Objeto respuesta = new Objeto();
		respuesta.set("tna", simulacionPlazoFijoWebPesos != null ? simulacionPlazoFijoWebPesos.tasa : "***");
		respuesta.set("tea", simulacionPlazoFijoWebPesos != null ? TasasUtil.tea(simulacionPlazoFijoWebPesos.tasa) : "***");
		respuesta.set("plazoMinimoSimulacion", plazoMinimoPesos);
		respuesta.set("plazoMaximoSimulacion", plazoMaximoPesos);
		respuesta.set("montoMinimoSimulacion", montoMinimoPesos);
		respuesta.set("montoMaximoSimulacion", montoMaximoPesos);
		respuesta.set("tnaUVA", simulacionPlazoFijoWebUVAs != null ? simulacionPlazoFijoWebUVAs.tasa : 0);
		respuesta.set("teaUVA", simulacionPlazoFijoWebUVAs != null ? TasasUtil.tea(simulacionPlazoFijoWebUVAs.tasa) : 0);
		respuesta.set("plazoMinimoSimulacionUVA", plazoMinimoUVAs);
		respuesta.set("plazoMaximoSimulacionUVA", plazoMaximoUVAs);
		respuesta.set("montoMinimoSimulacionUVA", montoMinimoUVAs);
		respuesta.set("montoMaximoSimulacionUVA", montoMaximoUVAs);
		respuesta.set("inicioVigencia", Fecha.hoy().string("yyyy-MM-dd'T'HH:mm:ss"));
		respuesta.set("finVigencia", Fecha.mañana().string("yyyy-MM-dd'T'HH:mm:ss"));
		return respuesta;
	}

	public static Object inicializarDatosBasicos(ContextoBB contexto) {
		Objeto respuesta = new Objeto();
		respuesta.set("generos.M", "Masculino");
		respuesta.set("generos.F", "Femenino");
		respuesta.add("tipoTributarios").set("id", "08").set("descripcion", "C.U.I.L.").set("estado", "V").set("residencia", "L").set("opcion", "P");
		respuesta.add("tipoTributarios").set("id", "11").set("descripcion", "C.U.I.T").set("estado", "V").set("residencia", "L").set("opcion", null);
		respuesta.add("versionDocumentos").set("id", "A").set("descripcion", "Ejemplar A").set("vdcEstado", "V");
		respuesta.add("versionDocumentos").set("id", "B").set("descripcion", "Ejemplar B").set("vdcEstado", "V");
		respuesta.add("versionDocumentos").set("id", "C").set("descripcion", "Ejemplar C").set("vdcEstado", "V");
		respuesta.add("versionDocumentos").set("id", "D").set("descripcion", "Ejemplar D").set("vdcEstado", "V");
		respuesta.add("versionDocumentos").set("id", "E").set("descripcion", "Ejemplar E").set("vdcEstado", "V");
		respuesta.add("versionDocumentos").set("id", "F").set("descripcion", "Ejemplar F").set("vdcEstado", "V");
		respuesta.add("versionDocumentos").set("id", "G").set("descripcion", "Ejemplar G").set("vdcEstado", "V");
		return respuesta;
	}

	public static Object obtenerCuil(ContextoBB contexto) {
		String dni = contexto.parametros.string("dni");
		String apYNom = contexto.parametros.string("apYNom", null);

		if (contexto.modoOffline()) {
			return "20335561733";
		}

		String respuesta = null;
		if (dni.length() >= 7) {
			for (Cuil cuil : ApiPersonas.cuils(contexto, dni, apYNom).tryGet()) {
				respuesta = cuil.cuil;
				break;
			}
		}

		return respuesta != null ? respuesta : "";
	}

	public static Object iniciar(ContextoBB contexto) {
		String apellido = contexto.parametros.string("apellido");
		String dni = contexto.parametros.string("dni");
		String idTributario = contexto.parametros.string("idTributario");
		String tipo = contexto.parametros.string("tipo");
		String genero = contexto.parametros.string("genero");

		Sesion sesion = contexto.sesion();
		sesion.cuil = idTributario;
		sesion.save();

		LogBB.evento(contexto, "PFW_INICIO", contexto.parametros);

		if (contexto.modoOffline()) {
			Objeto respuesta = new Objeto();
			respuesta.set("codigo", "SUCCESS");
			respuesta.set("uuid", UUID.randomUUID().toString());
			return respuesta;
		}

		Futuro<Informado> futuroInformado = futuro(() -> Informado.get(contexto, dni, apellido));
		Futuro<Persona> futuroPersona = ApiPersonas.persona(contexto, idTributario);
		Futuro<Indicador> futuroIndicador = ApiVentas.sujetoPasibleCredito(contexto, dni, idTributario, genero, "01", tipo);

		Informado informado = futuroInformado.tryGet();
		if (informado != null && informado.esTerrorista) {
			LogBB.evento(contexto, "PFW_RECHAZO_TERRORISTA", informado);
			Objeto respuesta = new Objeto();
			respuesta.set("codigo", "IN");
			respuesta.set("codigoError", 0);
			respuesta.set("mensaje", "INFORMADO");
			return respuesta;
		}

		Persona persona = futuroPersona.tryGet();
		if (persona != null && persona.esCliente()) {
			Productos productos = ApiProductos.productosVigentes(contexto, persona.idCliente).get();
			if (productos != null && !productos.isEmpty()) {
				LogBB.evento(contexto, "PFW_RECHAZO_TIENE_PRODUCTOS", productos);
				Objeto respuesta = new Objeto();
				respuesta.set("codigo", "PR");
				respuesta.set("codigoError", 0);
				respuesta.set("mensaje", "TIENE PRODUCTOS");
				return respuesta;
			}
		}

		Indicador indicador = futuroIndicador.get();
		if (!"SC".equals(indicador.resolucionCodigo)) {
			LogBB.evento(contexto, "PFW_RECHAZO_MOTOR", indicador);
			Objeto respuesta = new Objeto();
			respuesta.set("codigo", "SCORING");
			respuesta.set("codigoError", 0);
			respuesta.set("mensaje", "RECHAZO MOTOR");
			return respuesta;
		}

		Objeto respuesta = new Objeto();
		respuesta.set("codigo", "SUCCESS");
		respuesta.set("uuid", UUID.randomUUID().toString());
		return respuesta;
	}

	public static Object catalogoPais(ContextoBB contexto) {
		if (contexto.modoOffline()) {
			Objeto respuesta = new Objeto();
			respuesta.add().set("id", "80").set("descripcion", "ARGENTINA").set("nacionalidad", "ARGENTINO").set("estado", "V");
			return respuesta;
		}

		Objeto respuesta = new Objeto();
		for (Pais pais : ApiCatalogo.paises(contexto).get()) {
			Objeto item = respuesta.add();
			item.set("id", pais.id);
			item.set("descripcion", pais.descripcion);
			item.set("nacionalidad", pais.nacionalidad);
			item.set("estado", pais.estado);
		}
		return respuesta;
	}

	public static Object catalogoProvincias(ContextoBB contexto) {
		if (contexto.modoOffline()) {
			Objeto respuesta = new Objeto();
			respuesta.add().set("id", 1).set("descripcion", "CDAD AUTONOMA BS AS").set("idEstado", 80);
			respuesta.add().set("id", 2).set("descripcion", "BUENOS AIRES").set("idEstado", 80);
			return respuesta;
		}

		Objeto respuesta = new Objeto();
		for (Provincia provincia : ApiCatalogo.provincias(contexto).get()) {
			Objeto item = respuesta.add();
			item.set("id", provincia.id);
			item.set("descripcion", provincia.descripcion);
			item.set("idEstado", provincia.idEstado);
		}
		return respuesta;
	}

	public static Object situacionlaboral(ContextoBB contexto) {
		Objeto respuesta = new Objeto();
		respuesta.add().set("id", "1").set("descripcion", "OCUPADO").set("estado", "V").set("orden", 1);
		respuesta.add().set("id", "2").set("descripcion", "JUBILADO").set("estado", "V").set("orden", 2);
		respuesta.add().set("id", "3").set("descripcion", "DESEMPLEADO").set("estado", "V").set("orden", 3);
		return respuesta;
	}

	public static Object catalogoEstadosCiviles(ContextoBB contexto) {
		Objeto respuesta = new Objeto();
		respuesta.add().set("id", "S").set("descripcion", "Soltero/a").set("estado", "V");
		respuesta.add().set("id", "C").set("descripcion", "Casado/a").set("estado", "V");
		respuesta.add().set("id", "O").set("descripcion", "Separado/a").set("estado", "V");
		respuesta.add().set("id", "D").set("descripcion", "Divorciado/a").set("estado", "V");
		respuesta.add().set("id", "V").set("descripcion", "Viudo/a").set("estado", "V");
		return respuesta;
	}

	public static Object localidad(ContextoBB contexto) {
		String codigoPostal = contexto.parametros.string("codigoPostal");

		if (contexto.modoOffline()) {
			Objeto respuesta = new Objeto();
			Objeto item = respuesta.add();
			item.set("id", 7337);
			item.set("provinciaFrontEnd", "2");
			item.set("ciuDescripcion", "MORON");
			return respuesta;
		}

		Objeto respuesta = new Objeto();
		for (CiudadWF ciudadWF : SqlCampaniasWF.ciudades(contexto, codigoPostal).get()) {
			Objeto item = respuesta.add();
			item.set("id", ciudadWF.CIU_Id);
			item.set("provinciaFrontEnd", ciudadWF.CIU_PRV_Id);
			item.set("ciuDescripcion", ciudadWF.CIU_Descripcion);
		}
		return respuesta;
	}

	public static Object verificarCBU(ContextoBB contexto) {
		String cbu = contexto.parametros.string("cbu");

		LogBB.evento(contexto, "PFW_CBU", contexto.parametros);

		if (contexto.modoOffline()) {
			Objeto respuesta = new Objeto();
			respuesta.set("cbu", "0440051640000127235642");
			respuesta.set("codigoBancoDestino", "0044");
			respuesta.set("cuenta", "405100012723564");
			respuesta.set("cuentaPBF", "405100012723564");
			respuesta.set("estadoCuenta", "HTL");
			respuesta.set("moneda", "80");
			respuesta.set("nombreBancoDestino", "BANCO HIPOTECARIO S.A.");
			respuesta.set("nombreTitular", "ARGUMEDO SEBASTIAN");
			respuesta.set("redDestino", "L");
			respuesta.set("tipoPersona", "F");
			respuesta.set("tipoProducto", "AHO");
			respuesta.set("titulares.0.denominacion", "ARGUMEDO SEBASTIAN");
			respuesta.set("titulares.0.idTributario", "20269726718");
			respuesta.set("esTransaccional", false);
			return respuesta;
		}

		CuentaLink cuentaLink = ApiCuentas.cuentaLink(contexto, cbu).get();

		Objeto respuesta = new Objeto();
		respuesta.set("cbu", cuentaLink.cbu);
		respuesta.set("codigoBancoDestino", cuentaLink.codigoBancoDestino);
		respuesta.set("cuenta", cuentaLink.cuenta);
		respuesta.set("cuentaPBF", cuentaLink.cuentaPBF);
		respuesta.set("estadoCuenta", cuentaLink.estadoCuenta);
		respuesta.set("moneda", cuentaLink.moneda);
		respuesta.set("nombreBancoDestino", cuentaLink.nombreBancoDestino);
		respuesta.set("nombreTitular", cuentaLink.titulares.get(0).denominacion);
		respuesta.set("redDestino", cuentaLink.redDestino);
		respuesta.set("tipoPersona", cuentaLink.tipoPersona);
		respuesta.set("tipoProducto", cuentaLink.tipoProducto);
		respuesta.set("esTransaccional", cuentaLink.esTransaccional);
		for (Titular titular : cuentaLink.titulares) {
			Objeto item = respuesta.add("titulares");
			item.set("denominacion", titular.denominacion);
			item.set("idTributario", titular.idTributario);
		}
		return respuesta;
	}

	public static Object simular(ContextoBB contexto) {
		BigDecimal monto = contexto.parametros.bigDecimal("monto");
		Integer plazo = contexto.parametros.integer("plazo");
		String tipoPlazoFijo = contexto.parametros.type("tipo", "PESOS", "UVAS");

		LogBB.evento(contexto, "PFW_SIMULACION", contexto.parametros);

		if (contexto.modoOffline()) {
			Objeto respuesta = new Objeto();
			respuesta.set("capital", 5000.00);
			respuesta.set("diaDePago", "AL VENCIMIENTO");
			respuesta.set("fechaActivacion", "2020-06-25");
			respuesta.set("fechaPagoInteres", "2020-07-31");
			respuesta.set("fechaVencimiento", "2020-07-31");
			respuesta.set("garantiaDeLosDepositos", false);
			respuesta.set("moneda", "80");
			respuesta.set("plazo", 36);
			respuesta.set("tasa", 50.00);
			respuesta.set("tipo", "PLAZO FIJO WEB");
			respuesta.set("totalInteresEstimado", 246.58);
			respuesta.set("cancelacionAnticipada", "N");
			respuesta.set("tasaCancelacionAnt", 0.0);
			respuesta.set("tnaCancelacionAnt", 0.0);
			respuesta.set("teaCancelacionAnt", 0.0);
			respuesta.set("fechaDesdeCancelacionAnt", "2021-02-26");
			respuesta.set("fechaHastaCancelacionAnt", "2021-04-26");
			respuesta.set("montoUVA", 14.95);
			respuesta.set("cotizacionUVA", 0.00);
			respuesta.set("fechaCotizacionUVA", Fecha.hoy().string("yyyy-MM-dd"));
			return respuesta;
		}

		String tipoOperacion = tipoPlazoFijo.equals("UVAS") ? PlazoFijoWeb.UVAS : PlazoFijoWeb.PESOS;
		SimulacionPlazoFijoWeb simulacionPlazoFijoWeb = ApiPlazosFijos.simulacionPlazoFijoWeb(contexto, monto, plazo, tipoOperacion).get();

		Objeto respuesta = new Objeto();
		respuesta.set("capital", simulacionPlazoFijoWeb.capital);
		respuesta.set("diaDePago", simulacionPlazoFijoWeb.diaDePago);
		respuesta.set("fechaActivacion", simulacionPlazoFijoWeb.fechaActivacion.string("yyyy-MM-dd"));
		respuesta.set("fechaPagoInteres", simulacionPlazoFijoWeb.fechaPagoInteres.string("yyyy-MM-dd"));
		respuesta.set("fechaVencimiento", simulacionPlazoFijoWeb.fechaVencimiento.string("yyyy-MM-dd"));
		respuesta.set("garantiaDeLosDepositos", simulacionPlazoFijoWeb.garantiaDeLosDepositos);
		respuesta.set("moneda", simulacionPlazoFijoWeb.moneda);
		respuesta.set("plazo", simulacionPlazoFijoWeb.plazo);
		respuesta.set("tasa", simulacionPlazoFijoWeb.tasa);
		respuesta.set("tipo", simulacionPlazoFijoWeb.tipo);
		respuesta.set("totalInteresEstimado", simulacionPlazoFijoWeb.totalInteresEstimado);
		respuesta.set("cancelacionAnticipada", simulacionPlazoFijoWeb.cancelacionAnticipada);
		respuesta.set("tasaCancelacionAnt", simulacionPlazoFijoWeb.tasaCancelacionAnt);
		respuesta.set("tnaCancelacionAnt", simulacionPlazoFijoWeb.tnaCancelacionAnt);
		respuesta.set("teaCancelacionAnt", simulacionPlazoFijoWeb.teaCancelacionAnt);
		respuesta.set("fechaDesdeCancelacionAnt", simulacionPlazoFijoWeb.fechaDesdeCancelacionAnt.string("yyyy-MM-dd"));
		respuesta.set("fechaHastaCancelacionAnt", simulacionPlazoFijoWeb.fechaHastaCancelacionAnt.string("yyyy-MM-dd"));
		respuesta.set("montoUVA", simulacionPlazoFijoWeb.montoUVA);
		respuesta.set("cotizacionUVA", simulacionPlazoFijoWeb.cotizacionUVA);
		respuesta.set("fechaCotizacionUVA", simulacionPlazoFijoWeb.fechaCotizacionUVA.string("yyyy-MM-dd"));
		return respuesta;
	}

	public static Object constituir(ContextoBB contexto) {
		BigDecimal monto = contexto.parametros.bigDecimal("plazoFijo.monto");
		Integer plazo = contexto.parametros.integer("plazoFijo.plazo");
		String tipo = contexto.parametros.type("tipo", "PESOS", "UVAS").replace("PESOS", "0040").replace("UVAS", "0044");
		String cbu = contexto.parametros.string("cuenta.cbu");

		String idSexo = contexto.parametros.string("persona.sexo");
		String apellidos = contexto.parametros.string("persona.apellido");
		String nombres = contexto.parametros.string("persona.nombre");
		String idTipoIDTributario = contexto.parametros.string("persona.tipoIdTributario");
		String idVersionDocumento = contexto.parametros.string("persona.ejemplarDocumento");
		String cuit = contexto.parametros.string("persona.idTributario");
		String idSituacionLaboral = contexto.parametros.string("persona.idSituacionLaboral");
		String idPaisNacimiento = contexto.parametros.string("persona.idPaisNacimiento");
		String idNacionalidad = contexto.parametros.string("persona.idNacionalidad");
		String ciudadNacimiento = contexto.parametros.string("persona.ciudadNacimiento");
		Fecha fechaNacimiento = contexto.parametros.fecha("persona.fechaNacimiento", "dd/MM/yyyy");
		String idEstadoCivil = contexto.parametros.string("persona.estadoCivil");
		Integer cantidadNupcias = contexto.parametros.integer("persona.cantidadNupcias");

		String email = contexto.parametros.string("persona.email");

		String direccionCalle = contexto.parametros.string("persona.direccion.0.calle");
		String direccionNumero = contexto.parametros.string("persona.direccion.0.numero");
		String direccionDepto = contexto.parametros.string("persona.direccion.0.depto");
		String direccionPiso = contexto.parametros.string("persona.direccion.0.piso");
		String direccionProvinciaid = contexto.parametros.string("persona.direccion.0.provinciaId");
		String direccionLocalidadid = contexto.parametros.string("persona.direccion.0.localidadId");
		String direccionCodigopostal = contexto.parametros.string("persona.direccion.0.codigoPostal");

		String telefonoCaracteristica = contexto.parametros.string("persona.telefono.0.telCelCarac");
		String telefonoDDN = contexto.parametros.string("persona.telefono.0.telCelDdn");
		String telefonoNumero = contexto.parametros.string("persona.telefono.0.telCelNumero");

		String conyugueNombre = contexto.parametros.string("persona.datosConyugue.nombre");
		String conyugueApellido = contexto.parametros.string("persona.datosConyugue.apellido");
		String conyugueEjemplardocumento = contexto.parametros.string("persona.datosConyugue.ejemplarDocumento");
		String conyugueIdtributario = contexto.parametros.string("persona.datosConyugue.idTributario");
		String conyugueTipoidtributario = contexto.parametros.string("persona.datosConyugue.tipoIdTributario");
		String conyugueSexo = contexto.parametros.string("persona.datosConyugue.sexo");

		Sesion sesion = contexto.sesion();
		sesion.cuil = cuit;
		sesion.save();

		LogBB.evento(contexto, "PFW_INICIO_CONSTITUCION", contexto.parametros);

		if (contexto.modoOffline()) {
			return "";
		}

		String tipoNupcias = "";
		tipoNupcias = cantidadNupcias == 1 ? "P" : tipoNupcias;
		tipoNupcias = cantidadNupcias == 2 ? "S" : tipoNupcias;
		tipoNupcias = cantidadNupcias == 3 ? "T" : tipoNupcias;
		tipoNupcias = cantidadNupcias >= 4 ? "C" : tipoNupcias;

		Futuro<Persona> futuroPersona = ApiPersonas.persona(contexto, cuit);
		Futuro<Domicilios> futuroDomicilios = ApiPersonas.domicilios(contexto, cuit);
		Futuro<Telefonos> futuroTelefonos = ApiPersonas.telefonos(contexto, cuit);
		Futuro<Emails> futuroEmail = ApiPersonas.emails(contexto, cuit);
		Futuro<SucursalWF> futuroSucursalWF = SqlCampaniasWF.sucursal(contexto, direccionCodigopostal);
		Futuro<Persona> futuroConyugue = !conyugueIdtributario.isEmpty() ? ApiPersonas.persona(contexto, conyugueIdtributario) : null;
		Futuro<Relaciones> futuroRelaciones = !conyugueIdtributario.isEmpty() ? ApiPersonas.relaciones(contexto, cuit) : null;
		Futuro<CuentaLink> futuroCuentaLink = ApiCuentas.cuentaLink(contexto, cbu);

		sesion.sucursal = futuroSucursalWF.get().codigoSucursal;
		sesion.save();

		// Persona
		Persona persona = futuroPersona.tryGet();
		if (persona == null) {
			LogBB.evento(contexto, "PFW_CREAR_PERSONA");
			persona = ApiPersonas.crearPersona(contexto, cuit).get();
		}
		if (!persona.esCliente()) {
			LogBB.evento(contexto, "PFW_ACTUALIZAR_PERSONA");
			persona.idNacionalidad = idNacionalidad;
			persona.idSexo = idSexo;
			persona.idTipoDocumento = "01";
			persona.nombres = nombres;
			persona.numeroDocumento = cuit.substring(2, 10);
			persona.idTipoIDTributario = idTipoIDTributario;
			persona.idIva = idTipoIDTributario.equals("08") ? "CONF" : "MONO";
			persona.apellidos = apellidos;
			persona.fechaNacimiento = fechaNacimiento;
			persona.idSucursalAsignada = futuroSucursalWF.get().codigoSucursal;
			persona.idVersionDocumento = idVersionDocumento;
			persona.idSituacionLaboral = idSituacionLaboral;
			persona.idPaisNacimiento = idPaisNacimiento;
			persona.ciudadNacimiento = ciudadNacimiento;
			persona.idEstadoCivil = idEstadoCivil;
			persona.idSubtipoEstadoCivil = idEstadoCivil.equalsIgnoreCase("C") ? "Y" : "";
			persona.cantidadNupcias = idEstadoCivil.equalsIgnoreCase("C") ? tipoNupcias : "";
			persona = ApiPersonas.actualizarPersona(contexto, persona).get();
		}

		// Conyugue
		if (!conyugueIdtributario.isEmpty()) {
			Persona conyugue = futuroConyugue != null ? futuroConyugue.tryGet() : null;
			if (conyugue == null) {
				LogBB.evento(contexto, "PFW_CREAR_CONYUGUE");
				conyugue = ApiPersonas.crearPersona(contexto, conyugueIdtributario).get();
			}
			if (!conyugue.esCliente()) {
				LogBB.evento(contexto, "PFW_ACTUALIZAR_CONYUGUE");
				conyugue.nombres = conyugueNombre;
				conyugue.apellidos = conyugueApellido;
				conyugue.idVersionDocumento = conyugueEjemplardocumento;
				conyugue.idTipoIDTributario = conyugueTipoidtributario;
				conyugue.idSexo = conyugueSexo;
				ApiPersonas.actualizarPersona(contexto, conyugue).get();
			}
			Relaciones relaciones = futuroRelaciones.get();
			if (relaciones.conyugue() == null) {
				LogBB.evento(contexto, "PFW_CREAR_RELACION");
				ApiPersonas.crearRelacion(contexto, cuit, conyugueIdtributario, Relacion.CONYUGUE).get();
			}
		}

		// Domicilios
		if (!persona.esCliente()) {
			LogBB.evento(contexto, "PFW_ACTUALIZAR_DOMICILIOS");
			Domicilios domicilios = futuroDomicilios.get();
			NuevoDomicilio domicilio = new NuevoDomicilio();
			domicilio.calle = direccionCalle;
			domicilio.idCiudad = direccionLocalidadid;
			domicilio.idProvincia = direccionProvinciaid;
			domicilio.idCodigoPostal = direccionCodigopostal;
			domicilio.numero = direccionNumero;
			domicilio.piso = direccionPiso;
			domicilio.departamento = direccionDepto;
			domicilios.crearActualizar(contexto, cuit, domicilio, Domicilio.LEGAL);
			domicilios.crearActualizar(contexto, cuit, domicilio, Domicilio.POSTAL);
		}

		// Telefonos
		if (!persona.esCliente()) {
			LogBB.evento(contexto, "PFW_ACTUALIZAR_TELEFONOS");
			Telefonos telefonos = futuroTelefonos.get();
			NuevoTelefono telefono = new NuevoTelefono();
			telefono.codigoPais = "054";
			telefono.codigoArea = "0" + telefonoDDN;
			telefono.prefijo = null;
			telefono.caracteristica = telefonoCaracteristica;
			telefono.numero = telefonoNumero;
			telefonos.crearActualizar(contexto, cuit, telefono, Telefono.PARTICULAR);
			telefonos.crearActualizar(contexto, cuit, telefono, Telefono.LABORAL);
		}

		// Emails
		if (!persona.esCliente()) {
			LogBB.evento(contexto, "PFW_ACTUALIZAR_EMAIL");
			Emails emails = futuroEmail.get();
			emails.crearActualizar(contexto, cuit, email, Email.PERSONAL);
		}

		// Cliente
		String idCobis = persona.idCliente;
		if (!persona.esCliente()) {
			LogBB.evento(contexto, "PFW_CREAR_CLIENTE", new Objeto().set("idCobis", idCobis));
			Cliente cliente = ApiPersonas.crearCliente(contexto, cuit).get();
			persona.idCliente = cliente.idCliente;
			idCobis = cliente.idCliente;
		}

		// PlazoFijoWeb
		CuentaLink cuentaLink = futuroCuentaLink.get();
		PlazoFijoWeb plazoFijoWeb = ApiPlazosFijos.crearPlazoFijoWeb(contexto, persona, cuentaLink, monto, plazo, tipo).get();
		LogBB.evento(contexto, "PFW_ALTA", plazoFijoWeb);

		// Debin
		String numeroTransaccion = plazoFijoWeb.numeroTransaccion;
		DebinPlazoFijoWeb debin = ApiDebin.crearDebinPlazoFijoWeb(contexto, idCobis, cuit, cbu, monto, numeroTransaccion).get();
		LogBB.evento(contexto, "PFW_DEBIN", debin);
		LogBB.evento(contexto, "PFW_FIN");
		return "";
	}
}
