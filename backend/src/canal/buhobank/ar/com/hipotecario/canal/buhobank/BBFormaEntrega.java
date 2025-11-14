package ar.com.hipotecario.canal.buhobank;

import java.math.BigDecimal;
import java.util.List;

import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.servicio.api.catalogo.ApiCatalogo;
import ar.com.hipotecario.backend.servicio.api.catalogo.Paises;
import ar.com.hipotecario.backend.servicio.api.catalogo.Paises.Pais;
import ar.com.hipotecario.backend.servicio.api.catalogo.Provincias;
import ar.com.hipotecario.backend.servicio.api.catalogo.Provincias.Provincia;
import ar.com.hipotecario.backend.servicio.api.catalogo.Sucursales;
import ar.com.hipotecario.backend.servicio.api.catalogo.Sucursales.Sucursal;
import ar.com.hipotecario.backend.servicio.api.catalogo.Sucursales.SucursalGeo;
import ar.com.hipotecario.backend.servicio.sql.SqlBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.SqlCampaniasWF;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPaquetesBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPaquetesBuhobank.BBPaqueteBuhobank;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.CiudadesWF.CiudadWF;
import ar.com.hipotecario.backend.servicio.sql.esales.CodigosAreasBb;
import ar.com.hipotecario.canal.buhobank.SesionBB.DomicilioBB;

public class BBFormaEntrega extends Modulo {

	public static Objeto guardarFormaDeEntrega(ContextoBB contexto) {
		Integer idSucursal = contexto.parametros.integer("idSucursal", null);
		String tipo = contexto.parametros.string("tipo", "D").toUpperCase();

		SesionBB sesion = contexto.sesion();

		DomicilioBB domicilioLegal = sesion.domicilioLegal;
		if (domicilioLegal == null) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_FORMA_ENTREGA);
			LogBB.evento(contexto, ErroresBB.DOMICILIO_LEGAL_VACIO);
			return respuesta(ErroresBB.DOMICILIO_LEGAL_VACIO);
		}

		Boolean tipoValido = tipo.equals(GeneralBB.ENTREGA_DOMICILIO);
		tipoValido |= tipo.equals(GeneralBB.ENTREGA_SUCURSAL);
		tipoValido |= tipo.equals(GeneralBB.ENTREGA_OTRO_DOMICILIO);
		tipoValido |= tipo.equals(GeneralBB.ENTREGA_DOM_ALTERNATIVO);
		tipoValido |= tipo.equals(GeneralBB.ENTREGA_DOM_ANDREANI);

		if (!tipoValido) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_FORMA_ENTREGA);
			LogBB.error(contexto, ErroresBB.TIPO_INVALIDO);
			return respuesta(ErroresBB.TIPO_INVALIDO);
		}

		Objeto respuesta = respuesta();

		// TODO-BB: Sucursal de radicacion es siempre necesario? Se bloquea flujo o
		// como sigue?

		if (tipo.equals(GeneralBB.ENTREGA_DOMICILIO)) {
			DomicilioBB domicilioPostal = domicilioLegal.clonar();
			sesion.domicilioPostal = domicilioPostal;
			respuesta.set("sesion.domicilioPostal", domicilioPostal);
		}

		if (tipo.equals(GeneralBB.ENTREGA_SUCURSAL)) {
			DomicilioBB domicilioPostal = domicilioLegal.clonar();
			sesion.domicilioPostal = domicilioPostal;
			respuesta.set("sesion.domicilioPostal", domicilioPostal);
		}

		sesion.idSucursal = idSucursal;
		sesion.formaEntrega = tipo;
		sesion.estado = EstadosBB.FORMA_ENTREGA_OK;
		sesion.saveSesion();

		respuesta.set("sesion.idSucursal", idSucursal);
		respuesta.set("sesion.formaEntrega", tipo);

		LogBB.evento(contexto, EstadosBB.FORMA_ENTREGA_OK, respuesta);

		return respuesta;
	}

	public static Object guardarDomicilioAlternativo(ContextoBB contexto) {
		Objeto domicilioPostalObj = contexto.parametros.objeto("domicilioPostal", null);
		SesionBB sesion = contexto.sesion();

		Objeto respuesta = respuesta();

		if (domicilioPostalObj == null) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_DOMICILIO_ALTERNATIVO);
			LogBB.error(contexto, ErroresBB.NUEVO_DOMICILIO_NULO);
			return respuesta(ErroresBB.NUEVO_DOMICILIO_NULO);
		}

		DomicilioBB domicilioPostal = DomicilioBB.crear(domicilioPostalObj);

		domicilioPostal.piso = BBPersona.quitarPrimerosCeros(domicilioPostal.piso);
		domicilioPostal.dpto = BBPersona.quitarPrimerosCeros(domicilioPostal.dpto);
		domicilioPostal.numeroCalle = BBPersona.quitarPrimerosCeros(domicilioPostal.numeroCalle);
		if (empty(domicilioPostal.numeroCalle)) {
			domicilioPostal.numeroCalle = null;
		}

		CiudadWF ciudadPorCp = DomicilioBB.ciudadPorCP(contexto, domicilioPostal.cp);
		if (ciudadPorCp == null) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_DOMICILIO_ALTERNATIVO);
			LogBB.error(contexto, ErroresBB.CODIGO_POSTAl_DOM_POSTAL_INVALIDO);
			if(sesion.esFlujoTcv()){
				return respuesta("CP_INVALIDO");
			}
			return respuesta(ErroresBB.CODIGO_POSTAl_DOM_POSTAL_INVALIDO);
		}
		domicilioPostal.idCiudad = ciudadPorCp.CIU_Id;
		domicilioPostal.idProvincia = ciudadPorCp.CIU_PRV_Id;
		domicilioPostal.idPais = ciudadPorCp.CIU_PAI_Id;

		if (empty(domicilioPostal.pais)) {
			Paises paises = ApiCatalogo.paises(contexto).tryGet();
			domicilioPostal.pais = paises.buscarPaisById(domicilioPostal.idPais).descripcion;
		}

		if (empty(domicilioPostal.provincia)) {
			Provincias provincias = ApiCatalogo.provincias(contexto).tryGet();
			domicilioPostal.provincia = provincias.buscarProvinciaById(domicilioPostal.idProvincia).descripcion;
		}

		if (empty(domicilioPostal.ciudad)) {
			CiudadWF ciudadLegal = DomicilioBB.ciudadPorId(contexto, domicilioPostal.idCiudad);
			domicilioPostal.ciudad = !Util.empty(ciudadLegal) ? ciudadLegal.CIU_Descripcion : "";
		}

		if (empty(domicilioPostal.localidad)) {
			domicilioPostal.localidad = sesion.domicilioPostal.localidad;
		}

		sesion.domicilioPostal = domicilioPostal;
		sesion.formaEntrega = GeneralBB.ENTREGA_OTRO_DOMICILIO;
		sesion.estado = EstadosBB.GUARDAR_DOMICILIO_ALTERNATIVO_OK;
		sesion.saveSesion();

		respuesta.set("sesion.domicilioPostal", domicilioPostal);

		BBPersona.guardarDomicilioPostalTemprano(contexto);

		LogBB.evento(contexto, EstadosBB.GUARDAR_DOMICILIO_ALTERNATIVO_OK, respuesta);
		return respuesta;
	}

	public static Objeto guardarDomicilioLegal(ContextoBB contexto) {
		Objeto domicilioLegalObj = contexto.parametros.objeto("domicilioLegal", null);
		SesionBB sesion = contexto.sesion();

		Objeto respuesta = respuesta();

		if (domicilioLegalObj == null) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_DOMICILIO_LEGAL);
			LogBB.error(contexto, ErroresBB.DOMICILIO_LEGAL_NULO);
			return respuesta(ErroresBB.DOMICILIO_LEGAL_NULO);
		}

		DomicilioBB domicilioLegal = DomicilioBB.crear(domicilioLegalObj);

		domicilioLegal.piso = BBPersona.quitarPrimerosCeros(domicilioLegal.piso);
		domicilioLegal.dpto = BBPersona.quitarPrimerosCeros(domicilioLegal.dpto);
		domicilioLegal.numeroCalle = BBPersona.quitarPrimerosCeros(domicilioLegal.numeroCalle);
		if (empty(domicilioLegal.numeroCalle)) {
			domicilioLegal.numeroCalle = null;
		}

		CiudadWF ciudadPorCp = DomicilioBB.ciudadPorCP(contexto, domicilioLegal.cp);
		if (ciudadPorCp == null) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_DOMICILIO_LEGAL);
			LogBB.error(contexto, ErroresBB.CODIGO_POSTAl_DOM_LEGAL_INVALIDO);
			return respuesta(ErroresBB.CODIGO_POSTAl_DOM_LEGAL_INVALIDO);
		}
		domicilioLegal.idCiudad = ciudadPorCp.CIU_Id;
		domicilioLegal.idProvincia = ciudadPorCp.CIU_PRV_Id;
		domicilioLegal.idPais = ciudadPorCp.CIU_PAI_Id;

		if (empty(domicilioLegal.pais)) {
			Paises paises = ApiCatalogo.paises(contexto).tryGet();
			domicilioLegal.pais = paises.buscarPaisById(domicilioLegal.idPais).descripcion;
		}

		if (empty(domicilioLegal.provincia)) {
			Provincias provincias = ApiCatalogo.provincias(contexto).tryGet();
			domicilioLegal.provincia = provincias.buscarProvinciaById(domicilioLegal.idProvincia).descripcion;
		}

		if (empty(domicilioLegal.ciudad)) {
			CiudadWF ciudadLegal = DomicilioBB.ciudadPorId(contexto, domicilioLegal.idCiudad);
			domicilioLegal.ciudad = !Util.empty(ciudadLegal) ? ciudadLegal.CIU_Descripcion : "";
		}

		if (empty(domicilioLegal.localidad)) {
			domicilioLegal.localidad = sesion.domicilioLegal.localidad;
		}

		sesion.domicilioLegal = domicilioLegal;
		sesion.domicilioPostal = domicilioLegal;
		sesion.estado = EstadosBB.COMPLETAR_DOMICILIO_LEGAL_OK;
		sesion.saveSesion();

		respuesta.set("sesion.domicilioLegal", domicilioLegal);

		BBPersona.guardarCobisTempranoFuture(contexto);

		LogBB.evento(contexto, EstadosBB.COMPLETAR_DOMICILIO_LEGAL_OK, respuesta);
		return respuesta;
	}

	public static Object paises(ContextoBB contexto) {
		Objeto respuesta = respuesta();
		Objeto item = respuesta.set("Paises");
		for (Pais pais : ApiCatalogo.paises(contexto).get()) {
			Objeto paises = item.add();
			paises.set("id", pais.id);
			paises.set("descripcion", pais.descripcion);
		}
		return respuesta;
	}

	public static Object provincias(ContextoBB contexto) {
		Objeto respuesta = respuesta();
		Objeto item = respuesta.set("provincias");
		for (Provincia provincia : ApiCatalogo.provincias(contexto).get()) {
			if (provincia.descripcion != null && !provincia.descripcion.equals("NO DEFINIDA")) {
				Objeto provincias = item.add();
				provincias.set("id", provincia.id);
				provincias.set("descripcion", provincia.descripcion);
				provincias.set("idEstado", provincia.idEstado);
			}
		}
		return respuesta;
	}

	public static Object localidad(ContextoBB contexto) {
		String codigoPostal = contexto.parametros.string("codigoPostal");

		Objeto respuesta = respuesta();
		Objeto item = respuesta.set("localidad");
		for (CiudadWF ciudadWF : SqlCampaniasWF.ciudades(contexto, codigoPostal).get()) {
			Objeto localidad = item.add();
			localidad.set("provincia", ciudadWF.CIU_PRV_Id);
			localidad.set("provinciaDesc", ciudadWF.PRV_Descripcion);
			localidad.set("ciuDescripcion", ciudadWF.CIU_Descripcion);
			localidad.set("ciuPaiId", ciudadWF.CIU_PAI_Id);
			localidad.set("ciuDistrito", ciudadWF.CIU_Distrito);
			localidad.set("id", ciudadWF.CIU_Id);
		}
		return respuesta;
	}

	public static Object obtenerCodigosAreasPorProvincia(ContextoBB contexto) {
		String provincia = contexto.parametros.string("provincia");

		String idProvincia = null;
		if (provincia.equals("CIUDAD DE BUENOS AIRES")) {
			idProvincia = "1";
		}
		for (Provincia prov : ApiCatalogo.provincias(contexto).get()) {
			if (prov.descripcion.equals(provincia)) {
				idProvincia = prov.id;
				break;
			}
		}

		CodigosAreasBb codigos = SqlEsales.getCodigosAreasPorProvincias(contexto, provincia).tryGet();

		Objeto respuesta = respuesta();
		Objeto item = respuesta.set("codArea");
		for (int i = 0; i < codigos.size(); i++) {
			Objeto codArea = item.add();
			codArea.set("id", codigos.get(i).id);
			codArea.set("codArea", codigos.get(i).codigo);
			codArea.set("provincia", codigos.get(i).valor);
			codArea.set("idProvincia", idProvincia);
		}
		return respuesta;
	}

	public static Object obtenerCodigosAreas(ContextoBB contexto) {
		String idProvincia = null;

		CodigosAreasBb codigos = SqlEsales.getCodigosAreas(contexto).tryGet();

		Objeto respuesta = respuesta();
		Objeto item = respuesta.set("codArea");
		for (int i = 0; i < codigos.size(); i++) {
			if (codigos.get(i).valor.equals("CIUDAD DE BUENOS AIRES"))
				idProvincia = "1";
			else {
				for (Provincia prov : ApiCatalogo.provincias(contexto).get()) {
					if (prov.descripcion.equals(codigos.get(i).valor)) {
						idProvincia = prov.id;
					}
				}
			}

			Objeto codArea = item.add();
			codArea.set("id", codigos.get(i).id);
			codArea.set("codArea", codigos.get(i).codigo);
			codArea.set("provincia", codigos.get(i).valor);
			codArea.set("idProvincia", idProvincia);
		}
		return respuesta;
	}

	public static Object obtenerCodigosAreasPorCodigo(ContextoBB contexto) {
		String codigo = contexto.parametros.string("codigo", "").trim();
		if (codigo.length() < 2) {
			return respuesta(ErroresBB.ETAPA_CODIGO_AREA);
		}
		
		CodigosAreasBb codigos = SqlEsales.getCodigosAreasPorCodigo(contexto, codigo).tryGet();
		if (codigos == null || codigos.size() == 0) {			
			return respuesta(ErroresBB.ETAPA_CODIGO_AREA);
		}

		Objeto respuesta = respuesta();
		return respuesta;
	}

	public static Object sucursales(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();

		String idProvincia = contexto.parametros.string("idProvincia", null);
		String latitud = contexto.parametros.string("latitud", !empty(sesion.latitud) ? sesion.latitud.toString() : "");
		String longitud = contexto.parametros.string("longitud", !empty(sesion.longitud) ? sesion.longitud.toString() : "");

		String tipoSucursal = "1";

		Sucursales sucursalesBB = ApiCatalogo.sucursales(contexto, idProvincia, null, tipoSucursal).get();

		if (!empty(latitud) && !empty(longitud)) {

			Integer cantMinima = 3;
			BigDecimal radio = new BigDecimal("5.00");
			BigDecimal radioAux = new BigDecimal("10.00");

			List<SucursalGeo> sucursalesGeo = sucursalesBB.getSucursalesPorGeo(latitud, longitud, radio);
			if (sucursalesGeo.size() < cantMinima) {
				sucursalesGeo = sucursalesBB.getSucursalesPorGeo(latitud, longitud, radioAux);
			}

			if (sucursalesGeo.size() < cantMinima) {
				sucursalesGeo = sucursalesBB.getSucursalesPorGeo(latitud, longitud, null);
				sucursalesGeo = sucursalesGeo.subList(0, Math.min(cantMinima, sucursalesGeo.size()));
			}

			Objeto respuesta = respuesta();
			Objeto item = respuesta.set("sucursal");
			for (SucursalGeo sucursalGeo : sucursalesGeo) {
				Objeto SucursalItem = item.add();
				SucursalItem.set("id", sucursalGeo.id);
				SucursalItem.set("desSucursal", sucursalGeo.desSucursal);
				SucursalItem.set("domicilio", sucursalGeo.domicilio);
				SucursalItem.set("provincia", sucursalGeo.provincia);
			}
			return respuesta;
		}

		Objeto respuesta = respuesta();
		Objeto item = respuesta.set("sucursal");
		for (Sucursal sucursalBB : sucursalesBB) {
			Objeto SucursalItem = item.add();
			SucursalItem.set("id", sucursalBB.CodSucursal);
			SucursalItem.set("desSucursal", sucursalBB.DesSucursal);
			SucursalItem.set("domicilio", sucursalBB.Domicilio);
			SucursalItem.set("provincia", sucursalBB.NomProvincia);
		}

		return respuesta;
	}

	public static Boolean datosDomicilioVacios(DomicilioBB domicilio) {

		String calle = domicilio.calle;
		String numeroCalle = domicilio.numeroCalle;
		String cp = domicilio.cp;
		String ciudad = domicilio.ciudad;
		String provincia = domicilio.provincia;
		String pais = domicilio.pais;
		String idCiudad = domicilio.idCiudad;
		String idProvincia = domicilio.idProvincia;
		String idPais = domicilio.idPais;

		Boolean datosVacios = false;
		datosVacios |= empty(calle);
		datosVacios |= empty(numeroCalle);
		datosVacios |= empty(cp);
		datosVacios |= empty(ciudad);
		datosVacios |= empty(provincia);
		datosVacios |= empty(pais);
		datosVacios |= empty(idCiudad);
		datosVacios |= empty(idProvincia);
		datosVacios |= empty(idPais);

		return datosVacios;
	}

	public static Objeto validarDatosFormaEntrega(ContextoBB contexto) {

		SesionBB sesion = contexto.sesion();

		Boolean mostrarOpcionDomicilioActual = !datosDomicilioVacios(contexto.sesion().domicilioLegal);
		Boolean mostrarOpcionDomicilioPostal = !datosDomicilioVacios(contexto.sesion().domicilioPostal);
		Objeto respuesta = respuesta();

		BBPaquetesBuhobank paquetesBuhobank = SqlBuhoBank.obtenerPaquetes(contexto, sesion.getFlujo()).tryGet();
		BBPaqueteBuhobank paqueteBuhobank = BBPaquetesBuhobank.buscarPaquete(paquetesBuhobank, sesion.letraTC, sesion.numeroPaquete());
		Boolean mostrarOpcionSucursal = paqueteBuhobank != null ? paqueteBuhobank.envio_sucursal : false;

		if (!mostrarOpcionDomicilioActual) {
			sesion.actualizarEstadoError(ErroresBB.ETAPA_VALIDAR_FORMA_ENTREGA);
			LogBB.evento(contexto, ErroresBB.DOMICILIO_LEGAL_VACIO);
			respuesta.set("estado", ErroresBB.DOMICILIO_LEGAL_VACIO);
		}

		respuesta.set("mostrarOpcionDomicilioActual", mostrarOpcionDomicilioActual);
		respuesta.set("mostrarOpcionDomicilioPostal", mostrarOpcionDomicilioPostal);
		respuesta.set("mostrarOpcionSucursal", mostrarOpcionSucursal);
		respuesta.set("mostrarOpcionAndriani", sesion.getParamEnvioAndriani(contexto));

		String datos = "mostrarOpcionDomicilioActual: " + mostrarOpcionDomicilioActual + " | mostrarOpcionDomicilioPostal: " + mostrarOpcionDomicilioPostal + " | mostrarOpcionSucursal: " + mostrarOpcionSucursal;
		LogBB.evento(contexto, "REQUEST_VALIDAR_FORMA_ENTREGA", datos);
		return respuesta;
	}

	public static Objeto validarDomicilioV2(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();
		boolean tieneDomLegal = !BBFormaEntrega.datosDomicilioVacios(sesion.domicilioLegal);
		Objeto respuesta = respuesta("tieneDomicilioLegal", tieneDomLegal)
				.set("domicilio", null);

		if(tieneDomLegal){
			DomicilioBB domicilioLegal = sesion.domicilioLegal;
			Objeto domicilio = new Objeto();
			domicilio.set("calle", domicilioLegal.calle);
			domicilio.set("numero", domicilioLegal.numeroCalle);
			domicilio.set("piso", domicilioLegal.piso);
			domicilio.set("dpto", domicilioLegal.dpto);
			domicilio.set("cp", domicilioLegal.cp);
			domicilio.set("localidad", domicilioLegal.localidad);
			domicilio.set("provincia", domicilioLegal.provincia);
			respuesta.set("domicilio", domicilio);

			BBPersona.guardarCobisTempranoFuture(contexto);
		}
		else{
			LogBB.evento(contexto, ErroresBB.DOMICILIO_LEGAL_VACIO);
		}

		LogBB.evento(contexto, "VALIDAR_DOMICILIO");
		return respuesta;
	}
}
