package ar.com.hipotecario.canal.buhobank;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.sql.SqlBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.buhobank.*;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBContenidosDinamicoBuhobank.BBContenidoDinamicoBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPaquetesBuhobank.BBPaqueteBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPromosGeoBuhobank.BBPromoGeoBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasBeneficioBuhobank.BBTarjetaBeneficioBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasBeneficioLandingBuhobank.BBTarjetaBeneficioLandingBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasBuhobank.BBTarjetaBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasLandingBuhobank.BBTarjetaLandingBuhobank;
import ar.com.hipotecario.backend.servicio.sql.esales.ContenidosBBEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.ContenidosBBEsales.ContenidoBBEsales;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPlantillasFlujoBuhobank.BBPlantillaFlujoBuhobank;
import ar.com.hipotecario.backend.util.HTMLEncoder;

public class BBLanding extends Modulo {

	public static Object ofertas(ContextoBB contexto) {
		String numeroPaquete = contexto.parametros.string(":numeropaquete");

		BBPaqueteBuhobank paqueteBuhobank = SqlBuhoBank.buscarNumeroPaqueteBuhobank(contexto, Integer.parseInt(numeroPaquete)).tryGet();
		BBTarjetaBuhobank contenidoTarjeta = SqlBuhoBank.obtenerTarjetaBuhobank(contexto, paqueteBuhobank.id, false).tryGet();
		if (contenidoTarjeta == null) {
			return respuesta("PAQUETE_NO_ENCONTRADO");
		}

		BBTarjetasBeneficioBuhobank paqueteBeneficios = contenidoTarjeta.beneficios;
		if (paqueteBeneficios == null) {
			return respuesta("BENEFICIOS_NO_ENCONTRADOS");
		}

		Objeto respuesta = respuesta();

		respuesta.set("id", contenidoTarjeta.id);
		respuesta.set("numeroPaquete", contenidoTarjeta.numero_paquete);
		respuesta.set("titulo", contenidoTarjeta.titulo);
		respuesta.set("descripcion", contenidoTarjeta.descripcion);

		Objeto respuestaBeneficios = respuesta.set("beneficios");

		for (int i = 0; i < paqueteBeneficios.size(); i++) {
			BBTarjetaBeneficioBuhobank beneficioBuhobank = paqueteBeneficios.get(i);

			Objeto beneficio = respuestaBeneficios.add();
			beneficio.set("id", beneficioBuhobank.id);
			beneficio.set("descripcion", beneficioBuhobank.desc_beneficio);
			beneficio.set("descripcion_html", HTMLEncoder.decodeHTML(beneficioBuhobank.desc_beneficio_html));
		}

		return respuesta;
	}

	public static Object todasOfertas(ContextoBB contexto) {

		String flujo = contexto.parametros.string("flujo", GeneralBB.FLUJO_ONBOARDING);
		String tipo = contexto.parametros.string("tipo", "");

		SesionBB sesion = contexto.sesion();
		sesion.setFlujo(contexto, flujo, "");

		if (empty(tipo)) {
			tipo = flujo;
		}

		Boolean esNyp = GeneralBB.NYP.equals(tipo);

		BBPaquetesBuhobank paquetesBuhobank = SqlBuhoBank.obtenerPaquetes(contexto, flujo).tryGet();
		BBPaqueteBuhobank paqueteStandalone = BBPaquetesBuhobank.buscarStandalone(paquetesBuhobank);
		BBPaquetesBuhobank paquetes = esNyp ? BBPaquetesBuhobank.buscarPaquetesEmprendedor(paquetesBuhobank) : BBPaquetesBuhobank.buscarPaquetes(paquetesBuhobank, true);

		BBTarjetasLandingBuhobank contenidoTarjetas = new BBTarjetasLandingBuhobank();

		Boolean esTdVirtual = paqueteStandalone.td_virtual;
		BBTarjetaLandingBuhobank contenidoTarjetaStandalone = SqlBuhoBank.obtenerTarjetaLandingBuhobank(contexto, tipo, paqueteStandalone.id, esTdVirtual).tryGet();

		if (contenidoTarjetaStandalone != null) {
			contenidoTarjetas.add(contenidoTarjetaStandalone);
		}

		for (BBPaqueteBuhobank paquete : paquetes) {
			BBTarjetaLandingBuhobank contenidoTarjeta = SqlBuhoBank.obtenerTarjetaLandingBuhobank(contexto, tipo, paquete.id, false).tryGet();
			if (contenidoTarjeta != null) {
				contenidoTarjetas.add(contenidoTarjeta);
			}
		}

		contenidoTarjetas = BBTarjetasLandingBuhobank.ordernar(contenidoTarjetas);

		Objeto respuesta = respuesta();
		Objeto respuestaPaquetes = respuesta.set("paquetes");

		for (int i = 0; i < contenidoTarjetas.size(); i++) {
			BBTarjetaLandingBuhobank contenidoTarjeta = contenidoTarjetas.get(i);
			Objeto paquete = respuestaPaquetes.add();

			paquete.set("id", contenidoTarjeta.id);
			paquete.set("numeroPaquete", contenidoTarjeta.numero_paquete);
			paquete.set("titulo", contenidoTarjeta.titulo_landing);
			paquete.set("descripcion", contenidoTarjeta.descripcion);
			paquete.set("tarjeta", contenidoTarjeta.tarjeta);
			paquete.set("box", getDecodeSuperscript(contenidoTarjeta.box));
			paquete.set("boton", contenidoTarjeta.boton);

			BBTarjetasBeneficioLandingBuhobank contenidoBeneficios = contenidoTarjeta.beneficios;
			if (contenidoBeneficios == null) {
				return respuesta("BENEFICIOS_NO_ENCONTRADOS");
			}

			for (int j = 0; j < contenidoBeneficios.size(); j++) {
				BBTarjetaBeneficioLandingBuhobank contenidoBeneficio = contenidoBeneficios.get(j);

				Objeto beneficio = paquete.add("beneficios");
				beneficio.set("id", contenidoBeneficio.id);
				beneficio.set("descripcion", contenidoBeneficio.desc_beneficio);
				beneficio.set("descripcion_html", HTMLEncoder.decodeHTML(contenidoBeneficio.desc_beneficio_html));
				beneficio.set("icono", contenidoBeneficio.icono_id);
				beneficio.set("prioridad", contenidoBeneficio.prioridad);
			}
		}

		return respuesta;
	}

	private static String getDecodeSuperscript(String texto) {

		if (empty(texto)) {
			return "";
		}

		return !texto.contains("U+") ? texto : texto.replace("U+208D", "⁽").replace("U+207E", "⁾");
	}

	public static Object obtenerContenido(ContextoBB contexto) {

		String tipo = contexto.parametros.string("tipo", null);
		ContenidosBBEsales contenidos = null;

		if (empty(tipo)) {
			contenidos = SqlEsales.getContenidoOnboarding(contexto).tryGet();
		} else {
			contenidos = SqlEsales.obtenerContenidoByTipo(contexto, tipo).tryGet();
		}

		if (contenidos == null) {
			return respuesta("NO_ENCONTRADO");
		}

		Objeto respuesta = respuesta();
		Objeto respuestaPromos = respuesta.set("contenido");

		for (int i = 0; i < contenidos.size(); i++) {
			ContenidoBBEsales contenidoEsales = contenidos.get(i);
			Objeto contenido = respuestaPromos.add();

			contenido.set("id", contenidoEsales.id);
			contenido.set("tipo", contenidoEsales.tipo);
			contenido.set("logo", contenidoEsales.imagen);
			contenido.set("titulo", contenidoEsales.titular);
			contenido.set("subtitulo", contenidoEsales.bajada);
			contenido.set("subtituloLegal", contenidoEsales.bajada_legales);
			contenido.set("titulo_html", contenidoEsales.titular_html);
			contenido.set("subtitulo_html", contenidoEsales.bajada_html);
			contenido.set("subtituloLegal_html", contenidoEsales.bajada_legales_html);
		}

		return respuesta;
	}

	public static Object obtenerPromosLanding(ContextoBB contexto) {
		ContenidosBBEsales promos = SqlEsales.getPromosLanding(contexto).tryGet();
		if (promos == null) {
			return respuesta("NO_ENCONTRADO");
		}

		Objeto respuesta = respuesta();
		Objeto respuestaPromos = respuesta.set("promos");

		for (int i = 0; i < promos.size(); i++) {
			ContenidoBBEsales promoEsales = promos.get(i);
			Objeto promo = respuestaPromos.add();

			promo.set("id", promoEsales.id);
			promo.set("logo", promoEsales.imagen);
			promo.set("titulo", promoEsales.titular);
			promo.set("titulo_html", promoEsales.titular_html);
			promo.set("subtitulo", promoEsales.bajada);
			promo.set("subtitulo_html", promoEsales.bajada_html);
			promo.set("subtituloLegal", promoEsales.bajada_legales);
			promo.set("subtituloLegal_html", promoEsales.bajada_legales_html);
		}

		return respuesta;
	}

	public static Object obtenerContenidoPromos(ContextoBB contexto) {

		String flujo = contexto.parametros.string("flujo", GeneralBB.FLUJO_ONBOARDING);
		Integer cantidad = contexto.parametros.integer("cant", 9);
		// String latitud = contexto.parametros.string("latitud", "-34.584831");
		// String longitud = contexto.parametros.string("longitud", "-58.41577");
		String latitud = contexto.parametros.string("latitud", null);
		String longitud = contexto.parametros.string("longitud", null);

		if (empty(latitud) || empty(longitud)) {
			return respuesta("promos", obtenerPromosDefault(contexto, flujo));
		}

		BBPlantillaFlujoBuhobank plantillaFlujo = SqlBuhoBank.obtenerPlantillaPorFlujo(contexto, flujo).tryGet();
		if (empty(plantillaFlujo)) {
			return respuesta("promos", obtenerPromosDefault(contexto, flujo));
		}

		String radioKmMax = "10";
		String radioKmMed = "5";
		String radioKmMin = "1";
		BBPromosGeoBuhobank promosMax = SqlBuhoBank.obtenerPromosPorGeo(contexto, latitud, longitud, radioKmMax).tryGet();
		if (promosMax == null || promosMax.size() == 0) {
			return respuesta("promos", obtenerPromosDefault(contexto, flujo));
		}

		BBPromosGeoBuhobank promos = obtenerPromosPorRadio(promosMax, radioKmMin, plantillaFlujo.id, cantidad);
		if (promos.size() < cantidad) {
			promos = obtenerPromosPorRadio(promosMax, radioKmMed, plantillaFlujo.id, cantidad);
		}

		if (promos.size() < cantidad) {
			promos = obtenerPromosPorRadio(promosMax, radioKmMax, plantillaFlujo.id, cantidad);
		}

		if (promos == null || promos.size() < 3) {
			return respuesta("promos", obtenerPromosDefault(contexto, flujo));
		}

		return respuesta("promos", promos.subList(0, Math.min(cantidad, promos.size())));
	}

	private static BBPromosGeoBuhobank obtenerPromosPorRadio(BBPromosGeoBuhobank promos, String radioKm, Integer idPlantilla, Integer cantidad) {

		BBPromosGeoBuhobank promosAux = new BBPromosGeoBuhobank();
		List<String> titulos = new ArrayList<String>();
		List<String> categorias = new ArrayList<String>();

		for (BBPromoGeoBuhobank promo : promos) {

			if (empty(promo.id_plantilla_flujo)) {
				continue;
			}

			if (empty(promo.logo) || empty(promo.titulo) || empty(promo.categoria) || empty(promo.beneficio)) {
				continue;
			}

			try {

				BigDecimal radioPromo = new BigDecimal(promo.DistanciaKm);
				BigDecimal radioKmNum = new BigDecimal(radioKm);
				if (radioPromo.compareTo(radioKmNum) > 0) {
					continue;
				}
			} catch (Exception e) {
				continue;
			}

			if (idPlantilla.equals(promo.id_plantilla_flujo) && !titulos.contains(promo.titulo) && !categorias.contains(promo.categoria)) {
				promosAux.add(promo);
				titulos.add(promo.titulo);
				promo.beneficio = corregirCodificacion(promo.beneficio);
				promo.categoria = corregirCodificacion(promo.categoria);
				categorias.add(promo.categoria);
			}
		}

		if (promosAux.size() >= cantidad) {
			return promosAux;
		}

		for (BBPromoGeoBuhobank promo : promos) {

			if (!empty(promo.id_plantilla_flujo)) {
				continue;
			}

			if (empty(promo.logo) || empty(promo.titulo) || empty(promo.categoria) || empty(promo.beneficio)) {
				continue;
			}

			try {

				BigDecimal radioPromo = new BigDecimal(promo.DistanciaKm);
				BigDecimal radioKmNum = new BigDecimal(radioKm);
				if (radioPromo.compareTo(radioKmNum) > 0) {
					continue;
				}
			} catch (Exception e) {
				continue;
			}

			if (!titulos.contains(promo.titulo) && !categorias.contains(promo.categoria)) {
				promosAux.add(promo);
				titulos.add(promo.titulo);
				promo.beneficio = corregirCodificacion(promo.beneficio);
				promo.categoria = corregirCodificacion(promo.categoria);
				categorias.add(promo.categoria);
			}
		}

		return promosAux;
	}

	public static String corregirCodificacion(String input) {
		byte[] bytes = input.getBytes(StandardCharsets.ISO_8859_1);
		return new String(bytes, StandardCharsets.UTF_8);
	}

	private static BBPromosGeoBuhobank obtenerPromosDefault(ContextoBB contexto, String flujo) {

		BBContenidosDinamicoBuhobank contenidosDinamico = SqlBuhoBank.obtenerContenidosDinamicoByTipo(contexto, flujo, BBContenidosDinamicoBuhobank.TIPO_LANDING_PROMOCIONES).tryGet();
		BBPromosGeoBuhobank promos = new BBPromosGeoBuhobank();

		for (BBContenidoDinamicoBuhobank contenido : contenidosDinamico) {
			BBPromoGeoBuhobank promo = new BBPromoGeoBuhobank();
			promo.logo = contenido.imagen;
			promo.titulo = "";
			promo.highlight = contenido.titulo;
			promo.beneficio = contenido.texto;
			promo.DistanciaKm = "0";
			promo.categoria = "";
			promo.legales = contenido.texto_legales;
			promo.tyc = contenido.texto_tyc;
			promos.add(promo);
		}

		return promos;
	}

}
