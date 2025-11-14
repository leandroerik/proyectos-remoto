package ar.com.hipotecario.canal.homebanking.servicio;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class RestSeguro {

	public static ApiResponse obtenerProductos(ContextoHB contexto, String cuit) {
		ApiRequest request = Api.request("Obtener productos", "seguro", "GET", "/v1/{cuit}/productos-consolidada", contexto);
		request.path("idCliente", contexto.idCobis());
		request.path("cuit", cuit);
		request.permitirSinLogin = false;
		request.requiereIdCobis = true;
		request.cacheSesion = true;
		return Api.response(request, cuit);
	}

	public static ApiResponse obtenerRamoProductos(ContextoHB contexto, String cuit) {
		ApiRequest request = Api.request("Obtener ramo productos", "seguro", "GET", "/v1/{cuit}/ramo-productos", contexto);
		request.path("idCliente", contexto.idCobis());
		request.path("cuit", cuit);
		request.permitirSinLogin = false;
		request.requiereIdCobis = true;
		request.cacheSesion = true;
		return Api.response(request, cuit);
	}

	public static ApiResponse obtenerToken(ContextoHB contexto) {
		ApiRequest request = Api.request("Obtener token", "seguro", "GET", "/v1/token-salesforce", contexto);
		request.permitirSinLogin = false;
		request.requiereIdCobis = true;
		request.cacheSesion = true;
		return Api.response(request);
	}

	private static ApiResponse obtenerOferta(ContextoHB contexto, String sessionId) {
		ApiRequest request = Api.request("Obtener ofertas", "seguro", "GET", "/v1/ofertas/{sessionId}/", contexto);
		request.path("sessionId", sessionId);
		request.permitirSinLogin = false;
		request.requiereIdCobis = true;
		request.cacheSesion = true;
		return Api.response(request);
	}

	public static ApiResponse insertarEmision(ContextoHB contexto) {
		ApiRequest request = Api.request("Insertar Emision", "seguro", "POST", "/v1/emision", contexto);
		Boolean esCliente = contexto.parametros.bool("esCliente");
		Boolean esTC = contexto.parametros.bool("esTC");

		String calle = limpiarString(contexto.parametros.string("emiDeCalleEmi"));
		String descLocalidad = limpiarString(contexto.parametros.string("emiDeDesLocalidadEmi"));
		String numeroTarjetaOCuenta = contexto.parametros.string("emiNumTarjetaEmi");
		String prestamoCBUOTC = numeroTarjetaOCuenta.replace("-", "");
		String ramo = contexto.parametros.string("ramo");
		String producto = contexto.parametros.string("producto");

		request.body("clienteCuit", contexto.parametros.string("clienteCuit"));
		request.body("ramo", ramo);
		request.body("poliza", "0");
		request.body("certificado", "0");
		request.body("estado", "P");
		request.body("observaciones", "");
		request.body("tipoTramite", "73");
		request.body("tipoNovedad", "EMI");
		request.body("canal1", "1010");
		request.body("canal2", "0");
		request.body("canal4", "BH");
		request.body("resultado", "0");
		request.body("usuario", "HB");
		request.body("lista", "");
		request.body("producto", producto);
		request.body("emiApellido", limpiarString(contexto.parametros.string("emiApellido")));
		request.body("emiNombreEmi", limpiarString(contexto.parametros.string("emiNombreEmi")));
		request.body("emiTipoDocEmi", contexto.parametros.string("emiTipoDocEmi"));
		request.body("emiNumDocEmi", contexto.parametros.string("emiNumDocEmi"));
		request.body("emiFechaNacEmi", contexto.parametros.string("emiFechaNacEmi"));
		request.body("emiSexoEmi", contexto.parametros.string("emiSexoEmi"));
		request.body("emiEstadoCivilEmi", "");
		request.body("emiCodProfesionEmi", "");
		request.body("emiPaisNacimientoEmi", contexto.parametros.string("emiPaisNacimientoEmi"));
		request.body("emiActividadEmi", "");
		request.body("emiDrCalleEmi", calle);
		request.body("emiDrNumeroEmi", contexto.parametros.string("emiDeNumeroEmi"));
		request.body("emiDrPisoEmi", contexto.parametros.string("emiDePisoEmi"));
		request.body("emiDrDtoEmi", contexto.parametros.string("emiDeDtoEmi"));
		request.body("emiDrProvEmi", contexto.parametros.string("emiDeProvEmi"));
		request.body("emiDrLocalidadEmi", contexto.parametros.string("emiDeLocalidadEmi"));
		request.body("emiDrDesLocalidadEmi", descLocalidad);
		request.body("emiDrCpEmi", contexto.parametros.string("emiDeCpEmi"));
		request.body("emiTePaisEmi", "");
		request.body("emiTeAreaEmi", "");
		request.body("emiTeRefEmi", "");
		request.body("emiTeCaracEmi", "");
		request.body("emiTeNumEmi", "");
		request.body("emiCelPaisEmi", "");
		request.body("emiCelAreaEmi", "");
		request.body("emiCelRefEmi", "");
		request.body("emiCelCaracEmi", "");
		request.body("emiCelNumEmi", contexto.parametros.string("emiCelNumEmi"));
		request.body("emiMailEmi", contexto.parametros.string("emiMailEmi"));
		request.body("emiPlanEmi", contexto.parametros.string("emiPlanEmi"));
		request.body("emiCanal1Emi", "1010");
		request.body("emiCanal2Emi", "0");
		request.body("emiCanal4Emi", "BH");
		request.body("emiCodigoMedio", contexto.parametros.string("emiCodigoMedio"));
		request.body("emiCodigoOrigen", contexto.parametros.string("emiCodigoOrigen"));
		request.body("emiNumTarjetaEmi", prestamoCBUOTC );
		request.body("emiNumCuenntaTarjetaEmi", "");
		request.body("emiNumCuentaEncriptado", "");
		request.body("emiIsDesencripta", "N");
		request.body("emiNombreTitularTCEmi", limpiarString(contexto.parametros.string("emiNombreTitularTCEmi")));
		request.body("emiApellidoTitularTCEmi", limpiarString(contexto.parametros.string("emiApellidoTitularTCEmi")));
		request.body("emiVencTCEmi", contexto.parametros.string("emiVencTCEmi"));
		request.body("emiDeCalleEmi", calle);
		request.body("emiDeNumeroEmi", contexto.parametros.string("emiDeNumeroEmi"));
		request.body("emiDePisoEmi", contexto.parametros.string("emiDePisoEmi"));
		request.body("emiDeDtoEmi", contexto.parametros.string("emiDeDtoEmi"));
		request.body("emiDeProvEmi", contexto.parametros.string("emiDeProvEmi"));
		request.body("emiDeLocalidadEmi", contexto.parametros.string("emiDeLocalidadEmi"));
		request.body("emiDeDesLocalidadEmi", descLocalidad);
		request.body("emiDeCpEmi", contexto.parametros.string("emiDeCpEmi"));
		request.body("emiDbCalleEmi", calle);
		request.body("emiDbNumeroEmi", contexto.parametros.string("emiDeNumeroEmi"));
		request.body("emiDbPisoEmi", contexto.parametros.string("emiDePisoEmi"));
		request.body("emiDbDtoEmi", contexto.parametros.string("emiDeDtoEmi"));
		request.body("emiDbProvEmi", contexto.parametros.string("emiDeProvEmi"));
		request.body("emiDbLocalidadEmi", contexto.parametros.string("emiDeLocalidadEmi"));
		request.body("emiDbDesLocalidadEmi", descLocalidad);
		request.body("emiDbCpEmi", contexto.parametros.string("emiDeCpEmi"));
		request.body("emiBenApellido1Emi", limpiarString(contexto.parametros.string("emiBenApellido1Emi")));
		request.body("emiBenNombre1Emi", limpiarString(contexto.parametros.string("emiBenNombre1Emi")));
		request.body("emiBenTipoDoc1Emi", contexto.parametros.string("emiBenTipoDoc1Emi"));
		request.body("emiBenDesTipoDoc1Emi", contexto.parametros.string("emiBenDesTipoDoc1Emi"));
		request.body("emiBenNumDoc1Emi", contexto.parametros.string("emiBenNumDoc1Emi"));
		request.body("emiBenSexo1Emi", contexto.parametros.string("emiBenSexo1Emi"));
		request.body("emiBenIdRel1Emi", contexto.parametros.string("emiBenIdRel1Emi"));
		request.body("emiBenDesRel1Emi", contexto.parametros.string("emiBenDesRel1Emi"));
		request.body("emiBenParticipacion1Emi", contexto.parametros.string("emiBenParticipacion1Emi"));
		request.body("emiBenTelefono1Emi", contexto.parametros.string("emiBenTelefono1Emi"));
		request.body("emiBenApellido2Emi", limpiarString(contexto.parametros.string("emiBenApellido2Emi")));
		request.body("emiBenNombre2Emi", limpiarString(contexto.parametros.string("emiBenNombre2Emi")));
		request.body("emiBenTipodoc2Emi", contexto.parametros.string("emiBenTipodoc2Emi"));
		request.body("emiDesTipoDoc2Emi", contexto.parametros.string("emiDesTipoDoc2Emi"));
		request.body("emiBenNumDoc2Emi", contexto.parametros.string("emiBenNumDoc2Emi"));
		request.body("emiBenSexo2Emi", contexto.parametros.string("emiBenSexo2Emi"));
		request.body("emiBenRel2Emi", contexto.parametros.string("emiBenRel2Emi"));
		request.body("emiBenDesRel2Emi", contexto.parametros.string("emiBenDesRel2Emi"));
		request.body("emiBenParticipacion2Emi", contexto.parametros.string("emiBenParticipacion2Emi"));
		request.body("emiBenTelefono2Emi", contexto.parametros.string("emiBenTelefono2Emi"));
		request.body("emiBenApellido3Emi", limpiarString(contexto.parametros.string("emiBenApellido3Emi")));
		request.body("emiBenNombre3Emi", limpiarString(contexto.parametros.string("emiBenNombre3Emi")));
		request.body("emiBenTipDoc3Emi", contexto.parametros.string("emiBenTipDoc3Emi"));
		request.body("emiBenDesTipoDoc3Emi", contexto.parametros.string("emiBenDesTipoDoc3Emi"));
		request.body("emiBenNumDoc3Emi", contexto.parametros.string("emiBenNumDoc3Emi"));
		request.body("emiBenSexo3Emi", contexto.parametros.string("emiBenSexo3Emi"));
		request.body("emiBenIdRel3Emi", contexto.parametros.string("emiBenIdRel3Emi"));
		request.body("emiBenDesRel3Emi", contexto.parametros.string("emiBenDesRel3Emi"));
		request.body("emiBenParticipacion3Emi", contexto.parametros.string("emiBenParticipacion3Emi"));
		request.body("emiBenTelefono3Emi", contexto.parametros.string("emiBenTelefono3Emi"));
		request.body("emiBenApellido4Emi", limpiarString(contexto.parametros.string("emiBenApellido4Emi")));
		request.body("emiBenNombre4Emi", limpiarString(contexto.parametros.string("emiBenNombre4Emi")));
		request.body("emiBentipoDoc4Emi", contexto.parametros.string("emiBentipoDoc4Emi"));
		request.body("emiBenDesTipoDoc4Emi", contexto.parametros.string("emiBenDesTipoDoc4Emi"));
		request.body("emiBenNumdoc4Emi", contexto.parametros.string("emiBenNumdoc4Emi"));
		request.body("emiBenSexo4Emi", contexto.parametros.string("emiBenSexo4Emi"));
		request.body("emiBenIdRel4Emi", contexto.parametros.string("emiBenIdRel4Emi"));
		request.body("emiBenDesRel4Emi", contexto.parametros.string("emiBenDesRel4Emi"));
		request.body("emiBenParticipacion4Emi", contexto.parametros.string("emiBenParticipacion4Emi"));
		request.body("emiBenTelefono4Emi", contexto.parametros.string("emiBenTelefono4Emi"));
		request.body("emiPregunta1Emi", contexto.parametros.string("emiPregunta1Emi"));
		request.body("emiPregunta2Emi", contexto.parametros.string("emiPregunta2Emi"));
		request.body("emiPregunta3Emi", contexto.parametros.string("emiPregunta3Emi"));
		request.body("emiPregunta4Emi", contexto.parametros.string("emiPregunta4Emi"));
		request.body("emiRFTIPOEmi", "0");
		request.body("emiRFDESTIPOEmi", "BH");
		request.body("emiRFIDPRODEmi", "BH");
		request.body("emiRFIDOPEmi", "");
		request.body("emiUsuarioEmi", "HB");
		request.body("emiCanal1DesEmi", "1010-HOMEBANKING");
		request.body("emiCanal2DesEmi", "0 - BUENOS AIRES");
		request.body("emiCanal4DesEmi", "BH");
		if ( esCliente && esTC ) {
			request.body("idBonificacion", "0");
			request.body("cuotasBonificacion", "0");
			request.body("porcentajeBonificacion", "0");
			request.body("importeBonificacion", "0");
			request.body("importePremioReferencia", "0");
			request.body("importeBonificacionReferencia", "0");
		} else {
			request.body("idBonificacion", "");
			request.body("cuotasBonificacion", "");
			request.body("porcentajeBonificacion", "");
			request.body("importeBonificacion", "");
			request.body("importePremioReferencia", "");
			request.body("importeBonificacionReferencia", "");
		}
		request.body("combinatoria", "");
		request.body("sumaAseg01", contexto.parametros.string("sumaAseg01"));
		request.body("sumaAseg02", contexto.parametros.string("sumaAseg02"));
		request.body("sumaAseg03", contexto.parametros.string("sumaAseg03"));
		request.body("sumaAseg04", contexto.parametros.string("sumaAseg04"));
		request.body("sumaAseg05", contexto.parametros.string("sumaAseg05"));
		request.body("sumaAseg06", contexto.parametros.string("sumaAseg06"));
		request.body("sumaAseg07", contexto.parametros.string("sumaAseg07"));
		request.body("sumaAseg08", contexto.parametros.string("sumaAseg08"));
		request.body("sumaAseg09", contexto.parametros.string("sumaAseg09"));
		request.body("sumaAseg10", contexto.parametros.string("sumaAseg10"));
		request.body("sumaAseg11", contexto.parametros.string("sumaAseg11"));
		request.body("sumaAseg12", contexto.parametros.string("sumaAseg12"));
		request.body("sumaAseg13", contexto.parametros.string("sumaAseg13"));
		request.body("sumaAseg14", contexto.parametros.string("sumaAseg14"));
		request.body("sumaAseg15", contexto.parametros.string("sumaAseg15"));
		request.body("sumaAseg16", contexto.parametros.string("sumaAseg16"));
		request.body("sumaAseg17", contexto.parametros.string("sumaAseg17"));
		request.body("sumaAseg18", contexto.parametros.string("sumaAseg18"));
		request.body("sumaAseg19", contexto.parametros.string("sumaAseg19"));
		request.body("sumaAseg20", contexto.parametros.string("sumaAseg20"));
		request.body("tipoVivienda", contexto.parametros.string("tipoVivienda"));
		request.body("viviendaPermanente", contexto.parametros.string("viviendaPermanente"));
		request.body("desarrollaActividadesComercialesNTV", contexto.parametros.string("desarrollaActividadesComercialesNTV"));
		request.body("paredesMaterial", contexto.parametros.string("paredesMaterial"));
		request.body("metrosCuadrados", "");
		request.body("cantidadObjetos", "");
		request.body("promocion", "");
		request.body("poPromocion", "");
		request.body("premioOrigen", contexto.parametros.string("premioOrigen"));
		request.body("premioDestino", "");
		request.body("emiNumeroSerieNubicam", "");
		if (ramo.equals("9") && (producto.equals("401") || producto.equals("402")))
			request.body("idProductor", "414");
		else
			request.body("idProductor", "");
		request.body("nombreConyuge", limpiarString(contexto.parametros.string("nombreConyuge")));
		request.body("feNacConyuge", contexto.parametros.string("feNacConyuge"));
		request.body("dniConyuge", contexto.parametros.string("dniConyuge"));
		request.body("emiddjjPeps", "");
		request.body("ddjj", "");
		request.body("rddjj", "");
		request.body("alturaCliente", "");
		request.body("pesoCliente", "");
		request.body("cimc", "");

		setDatosMovilidad( request, contexto );
		setDatosMascotas( request, contexto );

		request.permitirSinLogin = false;
		request.requiereIdCobis = true;
		return Api.response(request);
	}

	public static ApiResponse lead(ContextoHB contexto, String cuit, String sessionId) {
		ApiRequest request = Api.request("Obtener ofertas", "seguro", "POST", "/v1/{cuit}/leads/{sessionId}/", contexto);
		request.path("cuit", cuit);
		request.path("sessionId", sessionId);
		request.body("FirstName", contexto.parametros.string("FirstName"));
		request.body("LastName", contexto.parametros.string("LastName"));
		request.body("Tipo_de_Documento__c", contexto.parametros.string("Tipo_de_Documento__c"));
		request.body("DNI__c", contexto.parametros.string("DNI__c"));
		request.body("Sexo__c", contexto.parametros.string("Sexo__c"));
		request.body("Estado_Civil__c", contexto.parametros.string("Estado_Civil__c"));
		request.body("FECHA_NACIMIENTO__c", contexto.parametros.string("FECHA_NACIMIENTO__c"));
		request.body("NACIONALIDAD__c", contexto.parametros.string("NACIONALIDAD__c"));
		request.body("Pais_Nacimiento__c", contexto.parametros.string("Pais_Nacimiento__c"));
		request.body("Codigo_de_area__c", contexto.parametros.string("Codigo_de_area__c"));
		request.body("Phone", contexto.parametros.bigDecimal("Phone"));
		request.body("Email", contexto.parametros.string("Email"));
		request.body("Street", contexto.parametros.string("Street"));
		request.body("NUMERO_DOMICILIO__c", contexto.parametros.string("NUMERO_DOMICILIO__c"));
		request.body("PISO_DOMICILIO__c", contexto.parametros.string("PISO_DOMICILIO__c"));
		request.body("DPTO_DOMICILIO__c", contexto.parametros.string("DPTO_DOMICILIO__c"));
		request.body("Numero_Lote__c", contexto.parametros.string("Numero_Lote__c"));
		request.body("Tipo_Vivienda__c", contexto.parametros.string("Tipo_Vivienda__c"));
		request.body("PostalCode", contexto.parametros.string("PostalCode"));
		request.body("domicilio_localidad__c", contexto.parametros.string("domicilio_localidad__c"));
		request.body("Domicilio_Provincia__c", contexto.parametros.string("Domicilio_Provincia__c"));
		request.body("LeadSource", contexto.parametros.string("LeadSource"));
		request.body("Sponsor__c", contexto.parametros.string("Sponsor__c"));
		request.body("Paso_de_Venta__c", contexto.parametros.string("Paso_de_Venta__c"));
		request.body("utm_Campaign__c", contexto.parametros.string("utm_Campaign__c"));
		request.body("utm_Content__c", contexto.parametros.string("utm_Content__c"));
		request.body("utm_Medium__c", contexto.parametros.string("utm_Medium__c"));
		request.body("utm_Source__c", contexto.parametros.string("utm_Source__c"));
		request.body("Cod_Ramo__c", contexto.parametros.string("Cod_Ramo__c"));
		request.body("Cod_Producto__c", contexto.parametros.string("Cod_Producto__c"));
		request.body("Desc_plan__c", contexto.parametros.string("Desc_plan__c"));
		request.body("Desc_Ramo__c", contexto.parametros.string("Desc_Ramo__c"));
		request.body("Cod_Plan__c", contexto.parametros.string("Cod_Plan__c"));
		request.body("Desc_Producto__c", contexto.parametros.string("Desc_Producto__c"));
		request.body("COMBINATORIA__c", contexto.parametros.string("COMBINATORIA__c"));
		request.body("Premio__c", contexto.parametros.string("Premio__c"));
		request.body("Edad_Hijos__c", contexto.parametros.string("Edad_Hijos__c"));
		request.body("Cuota_Colegio_Promedio__c", contexto.parametros.bigDecimal("Cuota_Colegio_Promedio__c"));
		request.body("Altura_IMC__c", contexto.parametros.string("Altura_IMC__c"));
		request.body("Peso_IMC__c", contexto.parametros.string("Peso_IMC__c"));
		request.body("Suma_Robo__c", contexto.parametros.string("Suma_Robo__c"));
		request.body("AddOns__c", contexto.parametros.string("AddOns__c"));
		request.body("Oferta__c", contexto.parametros.string("Oferta__c"));
		request.body("Ambiente__c", contexto.parametros.integer("Ambiente__c"));
		request.body("TC_Titular_Apellido__c", contexto.parametros.string("TC_Titular_Apellido__c"));
		request.body("TC_Titular_Nombre__c", contexto.parametros.string("TC_Titular_Nombre__c"));
		request.body("TC_Vencimiento__c", contexto.parametros.string("TC_Vencimiento__c"));
		request.body("Origen_de_Pago__c", contexto.parametros.string("Origen_de_Pago__c"));
		request.body("Medio_de_Pago__c", contexto.parametros.string("Medio_de_Pago__c"));
		request.body("Pago__c", contexto.parametros.string("Pago__c"));
		request.body("Respuestas_Declaracion_Jurada__c", contexto.parametros.string("Respuestas_Declaracion_Jurada__c"));

		request.permitirSinLogin = false;
		request.requiereIdCobis = true;
		return Api.response(request);
	}

	public static ApiResponse actualizarLead(ContextoHB contexto, String cuit, String sessionId, String leadId) {
		ApiRequest request = Api.request("Actualizar Lead", "seguro", "PATCH", "/v1/{cuit}/leads/{sessionId}/{leadId}", contexto);
		request.path("cuit", cuit);
		request.path("sessionId", sessionId);
		request.path("leadId", leadId);
		request.body("FirstName", contexto.parametros.string("FirstName"));
		request.body("LastName", contexto.parametros.string("LastName"));
		request.body("Tipo_de_Documento__c", contexto.parametros.string("Tipo_de_Documento__c"));
		request.body("DNI__c", contexto.parametros.string("DNI__c"));
		request.body("Sexo__c", contexto.parametros.string("Sexo__c"));
		request.body("Estado_Civil__c", contexto.parametros.string("Estado_Civil__c"));
		request.body("FECHA_NACIMIENTO__c", contexto.parametros.string("FECHA_NACIMIENTO__c"));
		request.body("NACIONALIDAD__c", contexto.parametros.string("NACIONALIDAD__c"));
		request.body("Pais_Nacimiento__c", contexto.parametros.string("Pais_Nacimiento__c"));
		request.body("Codigo_de_area__c", contexto.parametros.string("Codigo_de_area__c"));
		request.body("Phone", contexto.parametros.bigDecimal("Phone"));
		request.body("Email", contexto.parametros.string("Email"));
		request.body("Street", contexto.parametros.string("Street"));
		request.body("NUMERO_DOMICILIO__c", contexto.parametros.string("NUMERO_DOMICILIO__c"));
		request.body("PISO_DOMICILIO__c", contexto.parametros.string("PISO_DOMICILIO__c"));
		request.body("DPTO_DOMICILIO__c", contexto.parametros.string("DPTO_DOMICILIO__c"));
		request.body("Numero_Lote__c", contexto.parametros.string("Numero_Lote__c"));
		request.body("Tipo_Vivienda__c", contexto.parametros.string("Tipo_Vivienda__c"));
		request.body("PostalCode", contexto.parametros.string("PostalCode"));
		request.body("domicilio_localidad__c", contexto.parametros.string("domicilio_localidad__c"));
		request.body("Domicilio_Provincia__c", contexto.parametros.string("Domicilio_Provincia__c"));
		request.body("LeadSource", contexto.parametros.string("LeadSource"));
		request.body("Sponsor__c", contexto.parametros.string("Sponsor__c"));
		request.body("Paso_de_Venta__c", contexto.parametros.string("Paso_de_Venta__c"));
		request.body("utm_Campaign__c", contexto.parametros.string("utm_Campaign__c"));
		request.body("utm_Content__c", contexto.parametros.string("utm_Content__c"));
		request.body("utm_Medium__c", contexto.parametros.string("utm_Medium__c"));
		request.body("utm_Source__c", contexto.parametros.string("utm_Source__c"));
		request.body("Cod_Ramo__c", contexto.parametros.string("Cod_Ramo__c"));
		request.body("Cod_Producto__c", contexto.parametros.string("Cod_Producto__c"));
		request.body("Desc_plan__c", contexto.parametros.string("Desc_plan__c"));
		request.body("Desc_Ramo__c", contexto.parametros.string("Desc_Ramo__c"));
		request.body("Cod_Plan__c", contexto.parametros.string("Cod_Plan__c"));
		request.body("Desc_Producto__c", contexto.parametros.string("Desc_Producto__c"));
		request.body("COMBINATORIA__c", contexto.parametros.string("COMBINATORIA__c"));
		request.body("Premio__c", contexto.parametros.string("Premio__c"));
		request.body("Edad_Hijos__c", contexto.parametros.string("Edad_Hijos__c"));
		request.body("Cuota_Colegio_Promedio__c", contexto.parametros.bigDecimal("Cuota_Colegio_Promedio__c"));
		request.body("Altura_IMC__c", contexto.parametros.string("Altura_IMC__c"));
		request.body("Peso_IMC__c", contexto.parametros.string("Peso_IMC__c"));
		request.body("Suma_Robo__c", contexto.parametros.string("Suma_Robo__c"));
		request.body("AddOns__c", contexto.parametros.string("AddOns__c"));
		request.body("Oferta__c", contexto.parametros.string("Oferta__c"));
		request.body("Ambiente__c", contexto.parametros.integer("Ambiente__c"));
		request.body("TC_Titular_Apellido__c", contexto.parametros.string("TC_Titular_Apellido__c"));
		request.body("TC_Titular_Nombre__c", contexto.parametros.string("TC_Titular_Nombre__c"));
		request.body("TC_Vencimiento__c", contexto.parametros.string("TC_Vencimiento__c"));
		request.body("Origen_de_Pago__c", contexto.parametros.string("Origen_de_Pago__c"));
		request.body("Medio_de_Pago__c", contexto.parametros.string("Medio_de_Pago__c"));
		request.body("Pago__c", contexto.parametros.string("Pago__c"));
		request.body("Respuestas_Declaracion_Jurada__c", contexto.parametros.string("Respuestas_Declaracion_Jurada__c"));

		request.permitirSinLogin = false;
		request.requiereIdCobis = true;
		return Api.response(request);
	}

	public static List<Objeto> productos(ContextoHB contexto, String cuit) {

		if (contexto.sesion != null) {
			contexto.sesion.cobisCaido = (false);
		}
		ApiResponse response = obtenerProductos(contexto, cuit);
		if (response.hayError() && !"101146".equals(response.string("codigo"))) {
			if ("Server fuera de linea".equals(response.string("mensajeAlUsuario")) && contexto.sesion != null) {
				contexto.sesion.cobisCaido = (true);
			}
			return null;
		}
		List<Objeto> lista = new ArrayList<>();
		if (!response.hayError()) {
			for (Objeto producto : response.objetos()) {
				lista.add(producto);
			}
		}
		return lista;
	}

	public static List<Objeto> ramoProductos(ContextoHB contexto, String cuit) {

		if (contexto.sesion != null) {
			contexto.sesion.cobisCaido = (false);
		}
		ApiResponse response = obtenerRamoProductos(contexto, cuit);
		if (response.hayError() && !"101146".equals(response.string("codigo"))) {
			if ("Server fuera de linea".equals(response.string("mensajeAlUsuario")) && contexto.sesion != null) {
				contexto.sesion.cobisCaido = (true);
			}
			return null;
		}
		List<Objeto> lista = new ArrayList<>();
		if (!response.hayError()) {
			for (Objeto productos : response.objetos()) {
				lista.add(productos);
			}
		}
		return lista;
	}

	public static Objeto token(ContextoHB contexto) {

		Objeto respuesta = null;
		if (contexto.sesion != null) {
			contexto.sesion.cobisCaido = (false);
		}
		ApiResponse response = obtenerToken(contexto);
		if (response.hayError() && !"101146".equals(response.string("codigo"))) {
			if ("Server fuera de linea".equals(response.string("mensajeAlUsuario")) && contexto.sesion != null) {
				contexto.sesion.cobisCaido = (true);
			}
			return null;
		}
		respuesta = Objeto.fromJson(response.json);
		return respuesta;
	}

	public static List<Objeto> ofertas(ContextoHB contexto, String sessionId) {
		if (contexto.sesion != null) {
			contexto.sesion.cobisCaido = (false);
		}
		ApiResponse response = obtenerOferta(contexto, sessionId);
		if (response.hayError() && !"101146".equals(response.string("codigo"))) {
			if ("Server fuera de linea".equals(response.string("mensajeAlUsuario")) && contexto.sesion != null) {
				contexto.sesion.cobisCaido = (true);
			}
			return null;
		}
		List<Objeto> lista = new ArrayList<>();
		if (!response.hayError()) {
			for (Objeto oferta : response.objetos()) {
				lista.add(oferta);
			}
		}
		return lista;
	}

	public static Objeto insertEmisionOnline(ContextoHB contexto) {

		Objeto respuesta = null;
		if (contexto.sesion != null) {
			contexto.sesion.cobisCaido = (false);
		}
		ApiResponse response = insertarEmision(contexto);
		if (response.hayError() && !"101146".equals(response.string("codigo"))) {
			if ("Server fuera de linea".equals(response.string("mensajeAlUsuario")) && contexto.sesion != null) {
				contexto.sesion.cobisCaido = (true);
			}
			return null;
		}
		respuesta = Objeto.fromJson(response.json);
		return respuesta;
	}

	public static Objeto insertEmisionOnlineV2(ContextoHB contexto) {

		Objeto respuesta = null;
		if (contexto.sesion != null) {
			contexto.sesion.cobisCaido = (false);
		}
		ApiResponse response = insertarEmision(contexto);
		if (response.hayError() && !"101146".equals(response.string("codigo"))) {
			if ("Server fuera de linea".equals(response.string("mensajeAlUsuario")) && contexto.sesion != null) {
				contexto.sesion.cobisCaido = (true);
			}
			if( response.codigo == 504 ){
				respuesta = new Objeto();
				respuesta.set("codigo", 504);
				return respuesta;
			}
			return null;
		}
		respuesta = Objeto.fromJson(response.json);
		return respuesta;
	}

	public static Objeto leads(ContextoHB contexto, String cuit, String sessionId) {

		Objeto respuesta = null;
		if (contexto.sesion != null) {
			contexto.sesion.cobisCaido = (false);
		}
		ApiResponse response = lead(contexto, cuit, sessionId);
		if (response.hayError() && !"101146".equals(response.string("codigo"))) {
			if ("Server fuera de linea".equals(response.string("mensajeAlUsuario")) && contexto.sesion != null) {
				contexto.sesion.cobisCaido = (true);
			}
			return null;
		}
		respuesta = Objeto.fromJson(response.json);
		return respuesta;
	}

	public static Objeto actualizarLeads(ContextoHB contexto, String cuit, String sessionId, String leadId) {

		Objeto respuesta = null;
		if (contexto.sesion != null) {
			contexto.sesion.cobisCaido = (false);
		}
		ApiResponse response = actualizarLead(contexto, cuit, sessionId, leadId);
		if (response.hayError() && !"101146".equals(response.string("codigo"))) {
			if ("Server fuera de linea".equals(response.string("mensajeAlUsuario")) && contexto.sesion != null) {
				contexto.sesion.cobisCaido = (true);
			}
			return null;
		}
		respuesta = Objeto.fromJson(response.json);
		return respuesta;
	}

/////////////* UTILS */////////////////////////////////

	public static String limpiarString(String cadena) {
		String stringLimpio = Normalizer.normalize(cadena, Normalizer.Form.NFD);
		// Quitar caracteres no ASCII excepto la enie, interrogacion que abre,
		// exclamacion que abre, grados, U con dieresis.
		stringLimpio = stringLimpio.replaceAll("[^\\p{ASCII}(N\u0303)(n\u0303)(\u00A1)(\u00BF)(\u00B0)(U\u0308)(u\u0308)]", "");
		// Regresar a la forma compuesta, para poder comparar la enie con la tabla de
		// valores
		stringLimpio = Normalizer.normalize(stringLimpio, Normalizer.Form.NFC);
		return stringLimpio;
	}

	private static void setDatosMovilidad( ApiRequest request, ContextoHB contexto ) {
		String marca = contexto.parametros.string("marca");
		String modelo = contexto.parametros.string("modelo");
		String marcaModelo = "";

		if( !marca.isEmpty() && !modelo.isEmpty() )
			marcaModelo = marca + " " + modelo;

		String tipoMovilidad = contexto.parametros.string("tipoMovilidad");

		String tipoBien;
		switch (tipoMovilidad) {
			case "1":
				tipoBien = "BICICLETA";
				break;
			case "2":
				tipoBien = "MONOPATIN";
				break;
			default:
				tipoBien = "";
				break;
		}

		request.body("tipoBien", tipoBien);
		request.body("marcaModeloBien", marcaModelo);
		request.body("detalleBien", marcaModelo);
		request.body("numeroSerieBien", contexto.parametros.string("numeroSerie"));
	}

	private static void setDatosMascotas( ApiRequest request, ContextoHB contexto ) {
		String tipoMascota = contexto.parametros.string("tipoMascota");

		switch (tipoMascota) {
			case "1":
				tipoMascota = "PERRO";
				break;
			case "2":
				tipoMascota = "GATO";
				break;
			default:
				tipoMascota = "";
				break;
		}

		request.body("nombreMascotas", contexto.parametros.string("nombre"));
		request.body("raza", contexto.parametros.string("raza"));
		request.body("fecNacMascotas", contexto.parametros.string("fecha_nac"));
		request.body("especie", tipoMascota);
	}

}
