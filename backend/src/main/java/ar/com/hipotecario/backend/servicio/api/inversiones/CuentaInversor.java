package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.ApiCatalogo;
import ar.com.hipotecario.backend.servicio.api.catalogo.Paises;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentasBB.CuentaBB;
import ar.com.hipotecario.backend.servicio.api.personas.Domicilios.Domicilio;
import ar.com.hipotecario.backend.servicio.api.personas.Persona;
import ar.com.hipotecario.backend.servicio.api.personas.Telefonos.Telefono;
import ar.com.hipotecario.backend.servicio.api.ventas.ApiVentas;
import ar.com.hipotecario.canal.buhobank.SesionBB;

public class CuentaInversor extends ApiObjeto {

	public static ApiResponse postPaqueteCC(Contexto contexto, SesionBB sesion, Domicilio domicilioPostal, Telefono telefono, Persona persona, CuentaBB cuentaPesos, CuentaBB cuentaDolares) {
		ApiRequest request = new ApiRequest("altaCuentaComitente", "altacuentas", "POST", "/v1/paquetecuentacomitente", contexto);
		request.header("x-canal", "BBANK");
		request.header("x-Sistema", "BBANK");

		Objeto inversorCNVList = new Objeto();
		Objeto inversorCNV = new Objeto();
		inversorCNV.set("tipoActivacion", "ACTIVE");
		inversorCNV.set("codigoAdministrador", "");
		inversorCNV.set("fechaNacimiento", persona.fechaNacimiento + ".000-0400");
		inversorCNV.set("nacionalidad", sesion.nacionalidad);
		inversorCNV.set("paisRegistro", "ARGENTINA");
		inversorCNV.set("paisResidencia", "ARGENTINA");
		inversorCNV.set("nombre", persona.nombres);
		inversorCNV.set("apellido", persona.apellidos);
		inversorCNV.set("numIdentificador", Long.parseLong(persona.cuit));
		inversorCNV.set("codigoInversor", Long.parseLong(persona.cuit));
		inversorCNV.set("esParteInteresada", false);
		inversorCNV.set("esInversorParticipante", true);
		inversorCNV.set("tipoInversor", "Individual");
		inversorCNV.set("paisDomicilioFiscal", "ARGENTINA");
		inversorCNV.set("ext.estadoIVA", persona.idIva);
		inversorCNV.set("ext.estadoImpuesoSobreRenta", "NON_TAX_REGISTERED");
		inversorCNV.set("ext.tipoIdentificador", persona.idTipoIDTributario);
		
		Objeto detallesContacto = new Objeto();
		detallesContacto.set("email", sesion.mail);
		detallesContacto.set("nombreApellido", sesion.apellido + " " + sesion.nombre);
		inversorCNV.add("detallesContacto", detallesContacto);
		
		Objeto identificadorInteresado = new Objeto();
		identificadorInteresado.set("numeroIdentificador", Long.parseLong(persona.idCliente));
		identificadorInteresado.set("paisDomicilioInversor", "ARGENTINA");
		identificadorInteresado.set("paisEntidadEmisora", "ARGENTINA");
		identificadorInteresado.set("tipo", "01");
		inversorCNV.add("identificadorInteresado", identificadorInteresado);
		
		Objeto detallesDireccion = new Objeto();
		detallesDireccion.set("codigoPostal", Long.parseLong(domicilioPostal.idCodigoPostal));
		detallesDireccion.set("cuidad", domicilioPostal.ciudad);
		detallesDireccion.set("direccion", domicilioPostal.calle + " " + domicilioPostal.numero);
		detallesDireccion.set("pais", "ARGENTINA");
		detallesDireccion.set("provincia", domicilioPostal.idProvincia);
		detallesDireccion.set("tipoDireccion", "MAIN");
		inversorCNV.add("detallesDireccion", detallesDireccion);
		
		inversorCNVList.add(inversorCNV);
		
		Objeto cuentaInversorCNV = new Objeto();
		cuentaInversorCNV.set("paisEmision", "ARGENTINA");
		cuentaInversorCNV.set("entidadEmisora", "ARGENTINA");
		cuentaInversorCNV.set("esAccionCorporativa", false);
		cuentaInversorCNV.set("fechaCaducidad", Fecha.ahora() + ".000-0400");
		cuentaInversorCNV.set("fechaRegistro", Fecha.ahora() + ".000-0400");
		cuentaInversorCNV.set("identificadorCuenta", 1);
		cuentaInversorCNV.set("identificadorOperador", "");
		cuentaInversorCNV.set("nombreCuenta", sesion.nombre + " " + sesion.apellido);
		cuentaInversorCNV.set("tipoActivacion", "ACTIVE");
		cuentaInversorCNV.set("tipoIdentificadorCuenta", "PROPRIETARY");
		cuentaInversorCNV.set("tipoOperador", "PCODE");
		cuentaInversorCNV.set("grupoCuentas.fechaDesactivacion", Fecha.ahora() + ".000-0400");
		cuentaInversorCNV.set("grupoCuentas.nombre", "Allow");
		
		Objeto coPropietarios = new Objeto();
		coPropietarios.set("propietario.identificador", persona.idCliente);
		coPropietarios.set("propietario.tipo", "01");
		coPropietarios.set("propietarioPrincipal", true);
		cuentaInversorCNV.add("coPropietarios", coPropietarios);
		
		Objeto valoresCuentas = new Objeto();
		valoresCuentas.set("id", 0);
		valoresCuentas.set("idAlternativo", "");
		valoresCuentas.set("comentario", "");
		valoresCuentas.set("esCuentaEspejoCsdEmisorVinculadoPorDefecto", false);
		valoresCuentas.set("esNotificacionISOAccionCorporativa", false);
		valoresCuentas.set("referenciaFacturacion", "");
		valoresCuentas.set("tipoCuentaComitente", "CO_OWNER_ACCOUNT");
		valoresCuentas.set("caCuentaDistribucion.identificador", "");
		valoresCuentas.set("caCuentaDistribucion.tipoIdentificadorCuenta", "PROPRIETARY");
		valoresCuentas.set("emisor.paisEmision", "ARGENTINA");
		valoresCuentas.set("emisor.entidadEmisora", "ARGENTINA");
		valoresCuentas.set("emisor.fechaRegistro", Fecha.ahora() + ".000-0400");
		valoresCuentas.set("emisor.fechaVencimiento", Fecha.ahora() + ".000-0400");
		valoresCuentas.set("emisor.identificador", "");
		valoresCuentas.set("emisor.tipo", "PCODE");
		valoresCuentas.set("valoresCuentaExtension.declaracionTrimestralPorPublicacion", false);
		valoresCuentas.set("valoresCuentaExtension.requiereAprobacionEntregas", false);
		valoresCuentas.set("valoresCuentaExtension.valoresPrestamo", false);
		cuentaInversorCNV.add("valoresCuentas", valoresCuentas);

		String calificacion = persona.idTipoCliente.equals("PS") ? "PA" : persona.idTipoCliente.isEmpty() || persona.idTipoCliente == null ? "" : persona.idTipoCliente;

		Objeto cuentaComitente = new Objeto();
		cuentaComitente.set("actividad", "000");
		cuentaComitente.set("calificacion", calificacion);
		cuentaComitente.set("cobis", persona.idCliente);
		cuentaComitente.set("condicionIva", persona.idIva);
		cuentaComitente.set("cuit", persona.cuit);
		cuentaComitente.set("direccion", domicilioPostal.idCore);
		cuentaComitente.set("razonSocial", sesion.nombre + " " + sesion.apellido);
		cuentaComitente.set("situacionGanancia", persona.idGanancias);
		cuentaComitente.set("sucursal", persona.idSucursalAsignada);
		cuentaComitente.set("tipoIdTributario", persona.idTipoIDTributario);
		cuentaComitente.set("telefono", telefono.idDireccion + ", " + telefono.idCore);
		cuentaComitente.set("tipoSujeto", persona.idSector); 
		
		cuentaComitente.set("cuentasLiquidacion.peso.moneda", "80");
		cuentaComitente.set("cuentasLiquidacion.peso.tipoCuenta", "AHO");
		cuentaComitente.set("cuentasLiquidacion.peso.numero", cuentaPesos.numeroProducto);
		cuentaComitente.set("cuentasLiquidacion.peso.sucursal", cuentaPesos.sucursal);
		
		if(cuentaDolares != null) {
			cuentaComitente.set("cuentasLiquidacion.dolares.moneda", "2");
			cuentaComitente.set("cuentasLiquidacion.dolares.tipoCuenta", "AHO");
			cuentaComitente.set("cuentasLiquidacion.dolares.numero", cuentaDolares.numeroProducto);
			cuentaComitente.set("cuentasLiquidacion.dolares.sucursal", cuentaDolares.sucursal);
		}
		
		Objeto datos = new Objeto();
		datos.set("inversorCNVList", inversorCNVList);
		datos.set("cuentaInversorCNV", cuentaInversorCNV);
		datos.set("cuentaComitente", cuentaComitente);
		
		Objeto body = new Objeto();
		body.add(datos);
		request.body(body);
		
		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorInversiones(response), request, response);
		return response;
	}
	
	public static ApiResponse postPaqueteCCuotapartista(Contexto contexto, SesionBB sesion, Domicilio domicilioPostal, Telefono telefono, Persona persona, CuentaBB cuentaPesos, CuentaBB cuentaDolares, String idPersonaFondo) {
		ApiRequest request = new ApiRequest("altaCuentaCuotapartista", "altacuentas", "POST", "/v1/paquetecuentacuotapartista", contexto);
		request.header("x-canal", "BBANK");
		request.header("x-Sistema", "BBANK");
		
		Objeto datos = new Objeto();
		datos.set("categoria", "1");
		datos.set("numCuotapartista", "1");
		datos.set("segmentoInversion", "1");
		datos.set("actividadPrincipal", "");
		datos.set("tipoCuotapartista", "");
		datos.set("tipoInversor", "");
		datos.set("tipoPerfilRiesgo", "");
		datos.set("representantes", "");
		datos.set("tarjetasCredito", "");
		datos.set("tipoContribuyente", "");
		datos.set("fechaIngreso", null);
		datos.set("idBanco", null);
		datos.set("patrimonio", "0.00");
		datos.set("promedioMensual", "0.00");
		datos.set("esPEP", false);
		datos.set("esFisico", persona.esPersonaFisica);
		datos.set("imprimeResumenCuenta", true);
		datos.set("poseeInstrPagoPerm", true);
		datos.set("requiereFirmaConjunta", false);
		datos.set("perfil", persona.idCliente);
		datos.set("idPersonaFondo", idPersonaFondo);
		datos.set("tipoDoc",  persona.idTipoDocumento);
		datos.set("numDoc", sesion.numeroDocumento);
		datos.set("cuit", sesion.cuil);
		datos.set("nombre", persona.nombres);
		datos.set("apellido", persona.apellidos);
		datos.set("email", sesion.mail);
		String vfnet = "(" + telefono.codigoPais + ")" + "(" + telefono.codigoArea + ")" + telefono.caracteristica + "-" + telefono.numero;
		datos.set("telefono", vfnet);
		datos.set("esmasculino", "M".equals(sesion.genero));
		datos.set("cuotaPartista", generarIDUnico());
		datos.set("idPersonaCondomino", persona.idCliente);
		datos.set("idPersona", persona.idCliente);
		
		try {
			String fechaOriginal = persona.fechaNacimiento.toString();
			SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat formatoNuevo = new SimpleDateFormat("dd/MM/yyyy");
			Date fechaOriginalFormateada = formatoOriginal.parse(fechaOriginal);
			datos.set("fechaNacimiento", formatoNuevo.format(fechaOriginalFormateada));
		}catch(Exception e) {}
		
		Paises paises = ApiCatalogo.paises(contexto).tryGet();
		datos.set("paisNacimiento", paises.buscarPaisById(persona.idPaisNacimiento).descripcion);
		datos.set("paisNacionalidad", paises.buscarPaisById(persona.idNacionalidad).descripcion);
		
		datos.set("tipoEstadoCivil", sesion.idEstadoCivil);
		datos.set("datosUIF.monedaImporteEstimado", "");
		datos.set("origen.agenteColocador", "8");
		datos.set("origen.sucursal", persona.idSucursalAsignada);
		datos.set("radicacion.canalVivienda", "");
		datos.set("radicacion.oficinaCuenta", "");
		datos.set("radicacion.agenteColocador", "8");
		datos.set("radicacion.sucursal", persona.idSucursalAsignada);
		datos.set("domicilio.alturaCalle", domicilioPostal.numero);
		datos.set("domicilio.calle", domicilioPostal.calle);
		datos.set("domicilio.cp", domicilioPostal.idCodigoPostal);
		datos.set("domicilio.localidad", domicilioPostal.ciudad);
		datos.set("domicilio.pais", domicilioPostal.pais);
		datos.set("domicilio.provincia", domicilioPostal.idProvincia);
		datos.set("domicilio.piso", domicilioPostal.piso);
		datos.set("domicilio.departamento", domicilioPostal.departamento);
		
		Objeto cuotasBancariasPesos = new Objeto();
		cuotasBancariasPesos.set("requiereFirmaConjunta", "");
		cuotasBancariasPesos.set("idCuentaBancariaSec", "");
		cuotasBancariasPesos.set("iban", "");
		cuotasBancariasPesos.set("swift", "");
		cuotasBancariasPesos.set("descripcion", "CA");
		cuotasBancariasPesos.set("alias", cuentaPesos.alias(contexto));
		cuotasBancariasPesos.set("cbu", cuentaPesos.cbu);
		cuotasBancariasPesos.set("cuitTitular", sesion.cuil);
		cuotasBancariasPesos.set("fechaApertura", cuentaPesos.fechaAlta);
		cuotasBancariasPesos.set("idCuentaBancaria", generarIDUnico());
		cuotasBancariasPesos.set("numeroCuenta", cuentaPesos.numeroProducto);
		cuotasBancariasPesos.set("numeroSucursal", persona.idSucursalAsignada);
		cuotasBancariasPesos.set("banco.id", "00044");
		cuotasBancariasPesos.set("moneda.id", "80");
		cuotasBancariasPesos.set("tipoCuentaBancaria.id", "AHO");
		datos.add("cuotasBancarias", cuotasBancariasPesos);
		
		if(cuentaDolares != null) {
			Objeto cuotasBancariasDolares = new Objeto();
			cuotasBancariasDolares.set("requiereFirmaConjunta", "");
			cuotasBancariasDolares.set("idCuentaBancariaSec", "");
			cuotasBancariasDolares.set("iban", "");
			cuotasBancariasDolares.set("swift", "");
			cuotasBancariasDolares.set("descripcion", "CA");
			cuotasBancariasDolares.set("alias", cuentaDolares.alias(contexto));
			cuotasBancariasDolares.set("cbu", cuentaDolares.cbu);
			cuotasBancariasDolares.set("cuitTitular", sesion.cuil);
			cuotasBancariasDolares.set("fechaApertura", cuentaDolares.fechaAlta);
			cuotasBancariasDolares.set("idCuentaBancaria", generarIDUnico());
			cuotasBancariasDolares.set("numeroCuenta", cuentaDolares.numeroProducto);
			cuotasBancariasDolares.set("numeroSucursal", persona.idSucursalAsignada);
			cuotasBancariasDolares.set("banco.id", "00044");
			cuotasBancariasDolares.set("moneda.id", "2");
			cuotasBancariasDolares.set("tipoCuentaBancaria.id", "AHO");
			datos.add("cuotasBancarias", cuotasBancariasDolares);
		}
		
		Objeto body = new Objeto();
		body.add(datos);
		request.body(body);
		
		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorInversiones(response), request, response);
		return response;
	}
	
	public static String generarIDUnico() {
		LocalDateTime ahora = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
		return ahora.format(formatter);
	}
	
	public static ApiResponse selectPersonaByDoc(Contexto contexto, String numDocumento, String idTipoDoc) {
		ApiRequest request = new ApiRequest("selectByDocPersona", "inversiones", "POST", "/v1/SelectByDocPersona", contexto);
		Objeto body = new Objeto();
		body.set("numDocIdentidad", numDocumento);
		body.set("codTpDocIdentidad", idTipoDoc);
		request.body(body);
		
		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorInversiones(response), request, response);
		return response;
	}
}
