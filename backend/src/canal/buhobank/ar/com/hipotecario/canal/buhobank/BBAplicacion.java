package ar.com.hipotecario.canal.buhobank;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.UnauthorizedException;
import ar.com.hipotecario.backend.servicio.api.catalogo.ApiCatalogo;
import ar.com.hipotecario.backend.servicio.api.catalogo.DiaBancario;
import ar.com.hipotecario.backend.servicio.sql.SqlBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.buhobank.*;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBContenidosDinamicoBuhobank.BBContenidoDinamicoBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPaquetesBuhobank.BBPaqueteBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasBuhobank.BBTarjetaBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPlantillasFlujoBuhobank.BBPlantillaFlujoBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBVistasBuhobank.BBVistaBuhobank;
import ar.com.hipotecario.backend.servicio.sql.esales.SucursalesOnboardingEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.SucursalesOnboardingEsales.SucursalOnboardingEsales;

public class BBAplicacion extends Modulo {

	public static Object health(ContextoBB contexto) {
		Objeto respuesta = new Objeto();
		respuesta.set("estado", "BB_OK");
		respuesta.set("ambiente", Config.ambiente());

		return respuesta;
	}

	public static Object config(ContextoBB contexto) {
		UnauthorizedException.ifNot(contexto.requestHeader("Authorization"));

		String urlQr = contexto.parametros.string("url_qr", null);
		String plataforma = contexto.parametros.string("plataforma", null);

		SesionBB sesion = new SesionBB();
		String flujo = sesion.inicializarFlujo(contexto, urlQr);
		sesion.setFlujo(contexto, flujo, plataforma);

		String valorKeyBackOfficeVU = sesion.getParamKeyPublicaVU(contexto);

		Objeto config = new Objeto();
		config.set("flujo", flujo);
		config.set("urlBackOfficeVU", sesion.getUrlVU(contexto));
		config.set("apiKeyBackOfficeVU", contexto.getApiKeyBackOfficeVU(valorKeyBackOfficeVU));
		config.set("api_key_vu", valorKeyBackOfficeVU);
		config.set("url_tyc_inicio", "https://www.hipotecario.com.ar/media/datos-personales-alta-online.pdf");
		config.set("url_tyc_vu", "https://www.hipotecario.com.ar/media/datos-personales-alta-online.pdf");
		config.set("codigo_errores_vu", "1911|1912|1913|1914|1920|1921|1922|1930|1907|2001");
		config.set("existe_indisponibilidad", false);
		config.set("url_encuesta_error", "https://t.maze.co/180894267");
		config.set("max_intentos_vu", "2");
		config.set("url_tyc_flujo", "");
		config.set("prendido_modo_escucha", contexto.config.bool("buhobank_prendido_modo_escucha", false));
		config.set("prendido_modo_transaccional", contexto.config.bool("buhobank_prendido_modo_transaccional", false));
		config.set("drs_client_id", contexto.config.string("bb_app_drs_client_id",""));
		config.set("drs_secret_id", contexto.config.string("bb_app_drs_secret_id",""));
		config.set("val_bic", contexto.config.string("buhobank_val_bic", contexto.config.string("val_bic","")));
		config.set("val_emulador", contexto.config.bool("buhobank_prendido_val_emulador",false));
		return respuesta("config", config);
	}

	public static Object obtenerSucursalesOnboarding(ContextoBB contexto) {
		return respuesta("sucursales", SqlEsales.obtenerSucursalesOnboarding(contexto).tryGet());
	}

	public static Object actualizarAgregarSucursalOnboarding(ContextoBB contexto) {

		Integer id = contexto.parametros.integer("id", null);
		String nombre = contexto.parametros.string("nombre", null);
		String urlQr = contexto.parametros.string("url_qr", null);
		String flujo = contexto.parametros.string("flujo", null);
		Boolean habilitado = contexto.parametros.bool("habilitado", null);

		SucursalesOnboardingEsales sucursales = SqlEsales.obtenerSucursalesOnboarding(contexto).tryGet();
		if (sucursales == null || sucursales.size() == 0) {
			return respuesta("SIN_SUCURSALES");
		}

		if (id != null) {

			SucursalOnboardingEsales sucursalById = sucursales.buscarSucursalById(id);
			if (sucursalById == null) {
				return respuesta("ID_INVALIDO");
			}

			SucursalOnboardingEsales sucursal = sucursales.buscarSucursalByQr(sucursalById.url_qr);
			if (sucursal != null && sucursal.habilitado && sucursal.id != id) {
				return respuesta("YA_EXISTE_URL_QR");
			}

			sucursalById.nombre = nombre == null ? sucursalById.nombre : nombre;
			sucursalById.flujo = flujo == null ? sucursalById.flujo : flujo;
			sucursalById.url_qr = urlQr == null ? sucursalById.url_qr : urlQr;
			sucursalById.habilitado = habilitado == null ? sucursalById.habilitado : habilitado;

			SqlEsales.actualizarSucursalesOnboarding(contexto, sucursalById);
			return respuesta("sucursales", SqlEsales.obtenerSucursalesOnboarding(contexto).tryGet());
		}

		if (empty(nombre) || empty(flujo) || empty(urlQr) || empty(habilitado)) {
			return respuesta("PARAMETROS_INCORRECTOS");
		}

		SucursalOnboardingEsales sucursal = sucursales.buscarSucursalByQr(urlQr);
		if (sucursal != null && sucursal.habilitado) {
			return respuesta("YA_EXISTE_URL_QR");
		}

		SucursalOnboardingEsales nuevaSucursal = new SucursalOnboardingEsales();
		nuevaSucursal.nombre = nombre;
		nuevaSucursal.flujo = flujo;
		nuevaSucursal.url_qr = urlQr;
		nuevaSucursal.habilitado = habilitado;

		SqlEsales.crearSucursalesOnboarding(contexto, nuevaSucursal).tryGet();
		LogBB.evento(contexto, GeneralBB.TELEMARKETING_MANUAL_POST, contexto.parametros.toString());

		return respuesta("sucursales", SqlEsales.obtenerSucursalesOnboarding(contexto).tryGet());
	}

	public static Object obtenerContenidoFinalizar(ContextoBB contexto) {
		String flujo = contexto.parametros.string("flujo", null);
		Boolean esBatch = contexto.parametros.bool("esBatch", null);
		Boolean tieneClaveActiva = contexto.parametros.bool("tieneClaveActiva", null);

		SesionBB sesion = contexto.sesion();

		String letraTc = sesion.letraTC;
		Integer numeroPaquete = sesion.numeroPaquete();
		Boolean esVirtual = sesion.esTdVirtual();
		Boolean esTdFisica = sesion.esTdFisica();
		Boolean tieneInversor = sesion.buhoInversorAceptada();
		Boolean estaEnHorarioInversor = BBAplicacion.estaEnHorarioInversor(contexto);

		if (flujo == null) {
			flujo = sesion.getFlujo();
		}

		if (esBatch == null) {
			esBatch = sesion.esBatch();
		}

		if (esBatch) {
			return obtenerFinalizarBatch(contexto, flujo);
		}

		if (tieneClaveActiva == null) {
			tieneClaveActiva = BBSeguridad.tieneClaveActivaBool(contexto, sesion.cuil);
		}

		String claveActivaRes = tieneClaveActiva ? "TIENE_CLAVE_ACTIVA" : "0";

		Objeto respuestaDefault = respuesta();
		Objeto contenidoDefault = BBTarjetasFinalizarBatchBuhobank.obtenerContenidoDefault(flujo, esVirtual, esBatch, tieneClaveActiva, contexto.sesion().esStandalone(), esTdFisica, tieneInversor, estaEnHorarioInversor);
		respuestaDefault.set("claveActiva", claveActivaRes);
		respuestaDefault.set("contenido", contenidoDefault);

		BBPaquetesBuhobank paquetesBuhobank = SqlBuhoBank.obtenerPaquetes(contexto, flujo).tryGet();
		BBPaqueteBuhobank paqueteBuhobank = BBPaquetesBuhobank.buscarPaquete(paquetesBuhobank, letraTc, numeroPaquete);
		if (paqueteBuhobank == null) {
			LogBB.evento(contexto, "CONTENIDO_FINALIZAR", respuestaDefault);
			return respuestaDefault;
		}

		if (!empty(paqueteBuhobank.paq_base)) {
			paqueteBuhobank = BBPaquetesBuhobank.buscarPaquete(paquetesBuhobank, paqueteBuhobank.letra_tc, paqueteBuhobank.paq_base);
		}

		BBTarjetaBuhobank tarjetaBuhobank = SqlBuhoBank.obtenerTarjetaBuhobank(contexto, paqueteBuhobank.id, esVirtual).tryGet();
		if (tarjetaBuhobank == null) {
			LogBB.evento(contexto, "CONTENIDO_FINALIZAR", respuestaDefault);
			return respuestaDefault;
		}

		BBTarjetasFinalizarBuhobank tarjetasFinalizarBuhobank = SqlBuhoBank.obtenerTarjetasFinalizarBuhobank(contexto).tryGet();
		
		Integer tarjetaId = tarjetaBuhobank.id;

		BBTarjetasFinalizarBuhobank tarjetasFinalizar = BBTarjetasFinalizarBuhobank.buscarIdTarjeta(tarjetasFinalizarBuhobank, tarjetaId);
		if (tarjetasFinalizar == null) {
			LogBB.evento(contexto, "CONTENIDO_FINALIZAR", respuestaDefault);
			return respuestaDefault;
		}

		String titular = "¡Te damos la bienvenida al Banco del Hogar!";
		
		List<Objeto> items = BBTarjetasFinalizarBuhobank.obtenerItems(tarjetasFinalizar, tieneInversor, esVirtual, esTdFisica, numeroPaquete, estaEnHorarioInversor);

		BBContenidosDinamicoBuhobank logoPrincipalObj = SqlBuhoBank.obtenerContenidosDinamicoByTipo(contexto, flujo, BBContenidosDinamicoBuhobank.TIPO_FINALIZAR_LOGO_PRINCIPAL).tryGet();
		String logoPrincipal = logoPrincipalObj != null ? logoPrincipalObj.first().imagen : null;

		String tipoClave = tieneClaveActiva ? BBContenidosDinamicoBuhobank.TIPO_FINALIZAR_CLAVE_ACTIVA : BBContenidosDinamicoBuhobank.TIPO_FINALIZAR_SIN_CLAVE_ACTIVA;
		BBContenidosDinamicoBuhobank contenidoClaveObj = SqlBuhoBank.obtenerContenidosDinamicoByTipo(contexto, flujo, tipoClave).tryGet();
		Objeto textBox = BBTarjetasFinalizarBuhobank.obtenerTextBox(contenidoClaveObj);
		String textoBoton = BBTarjetasFinalizarBuhobank.obtenerTextoBoton(contenidoClaveObj);

		if (empty(titular) || empty(textoBoton) || empty(logoPrincipal) || items == null || items.size() == 0 || textBox == null) {
			LogBB.evento(contexto, "CONTENIDO_FINALIZAR", respuestaDefault);
			return respuestaDefault;
		}

		Objeto contenido = new Objeto();
		contenido.set("esBatch", esBatch);
		contenido.set("logo_principal", logoPrincipal);
		contenido.set("titular_principal", "");
		contenido.set("titular", titular);
		contenido.set("titular_html", BBTarjetasFinalizarBuhobank.htmlNegrita(titular));
		contenido.set("items", items);
		contenido.set("textBox", textBox);
		contenido.set("textoBoton", textoBoton);
		contenido.set("urlBoton", "");

		Objeto respuesta = respuesta();
		respuesta.set("claveActiva", claveActivaRes);
		respuesta.set("contenido", contenido);

		LogBB.evento(contexto, "CONTENIDO_FINALIZAR", respuesta);
		return respuesta;
	}
	
	public static boolean estaEnHorarioInversor(ContextoBB contexto) {
		try {
			String horario = "08:00-19:00";
			String apertura = horario.split("-")[0]; //08:00
			String cierre = horario.split("-")[1]; //19:00

			LocalTime time = LocalTime.now();
			Boolean esAntes = time.isBefore(LocalTime.parse(apertura));
			Boolean esDespues = time.isAfter(LocalTime.parse(cierre));

			if(esAntes || esDespues) {
				return false;
			}

		}catch(Exception e) { }
			
		DiaBancario diaBancario = ApiCatalogo.diaBancario(contexto, Fecha.ahora()).tryGet();
		return diaBancario != null && diaBancario.esDiaHabil() && !diaBancario.esFinDeSemana();
	}

	private static Object obtenerFinalizarBatch(ContextoBB contexto, String flujo) {

		SesionBB sesion = contexto.sesion();
		String letraTc = sesion.letraTC;
		Integer numeroPaquete = sesion.numeroPaquete();
		Boolean esVirtual = sesion.tieneDomVirtual();
		Boolean esTdFisica = sesion.esTdFisica();
		Boolean tieneInversor = sesion.buhoInversorAceptada();
		Boolean estaEnHorarioInversor = BBAplicacion.estaEnHorarioInversor(contexto);

		Objeto respuestaDefault = respuesta();
		Objeto contenidoDefault = BBTarjetasFinalizarBatchBuhobank.obtenerContenidoDefault(flujo, esVirtual, true, true, contexto.sesion().esStandalone(), esTdFisica, tieneInversor, estaEnHorarioInversor);
		respuestaDefault.set("claveActiva", "VER PROMOCIONES");
		respuestaDefault.set("contenido", contenidoDefault);

		BBPaquetesBuhobank paquetesBuhobank = SqlBuhoBank.obtenerPaquetes(contexto, flujo).tryGet();
		BBPaqueteBuhobank paqueteBuhobank = BBPaquetesBuhobank.buscarPaquete(paquetesBuhobank, letraTc, numeroPaquete);
		if (paqueteBuhobank == null) {
			LogBB.evento(contexto, "CONTENIDO_FINALIZAR", respuestaDefault);
			return respuestaDefault;
		}

		BBTarjetaBuhobank tarjetaBuhobank = SqlBuhoBank.obtenerTarjetaBuhobank(contexto, paqueteBuhobank.id, esVirtual).tryGet();
		if (tarjetaBuhobank == null) {
			LogBB.evento(contexto, "CONTENIDO_FINALIZAR", respuestaDefault);
			return respuestaDefault;
		}

		BBTarjetasFinalizarBatchBuhobank tarjetasFinalizarBuhobank = SqlBuhoBank.obtenerTarjetasFinalizarBatchBuhobank(contexto).tryGet();
		BBTarjetasFinalizarBatchBuhobank tarjetasFinalizar = BBTarjetasFinalizarBatchBuhobank.buscarIdTarjeta(tarjetasFinalizarBuhobank, tarjetaBuhobank.id);
		if (tarjetasFinalizar == null) {
			LogBB.evento(contexto, "CONTENIDO_FINALIZAR", respuestaDefault);
			return respuestaDefault;
		}

		String titular = BBTarjetasFinalizarBatchBuhobank.obtenerTitular(tarjetasFinalizar);
		List<Objeto> items = BBTarjetasFinalizarBatchBuhobank.obtenerItems(tarjetasFinalizar);

		BBContenidosDinamicoBuhobank logoPrincipalObj = SqlBuhoBank.obtenerContenidosDinamicoByTipo(contexto, flujo, BBContenidosDinamicoBuhobank.TIPO_FINALIZAR_LOGO_PRINCIPAL).tryGet();
		String logoPrincipal = logoPrincipalObj != null ? logoPrincipalObj.first().imagen : null;

		BBContenidosDinamicoBuhobank batchTextboxObj = SqlBuhoBank.obtenerContenidosDinamicoByTipo(contexto, flujo, BBContenidosDinamicoBuhobank.TIPO_FINALIZAR_BATCH_TEXT_BOX).tryGet();
		Objeto textBox = BBTarjetasFinalizarBatchBuhobank.obtenerTextBox(batchTextboxObj);

		BBContenidosDinamicoBuhobank batchTextoBotonObj = SqlBuhoBank.obtenerContenidosDinamicoByTipo(contexto, flujo, BBContenidosDinamicoBuhobank.TIPO_FINALIZAR_BATCH_TEXTO_BOTON).tryGet();
		String textoBoton = BBContenidosDinamicoBuhobank.obtenerPrimerTitulo(batchTextoBotonObj);
		String urlBoton = BBContenidosDinamicoBuhobank.obtenerPrimerImagen(batchTextoBotonObj);

		if (empty(titular) || empty(logoPrincipal) || items == null || items.size() == 0) {
			LogBB.evento(contexto, "CONTENIDO_FINALIZAR", respuestaDefault);
			return respuestaDefault;
		}

		Objeto contenido = new Objeto();
		contenido.set("esBatch", true);
		contenido.set("logo_principal", logoPrincipal);
		contenido.set("titular_principal", "¡Felicitaciones!");
		contenido.set("titular", titular);
		contenido.set("titular_html", BBTarjetasFinalizarBatchBuhobank.htmlNegrita(titular));
		contenido.set("items", items);
		contenido.set("textBox", textBox);
		contenido.set("textoBoton", textoBoton);
		contenido.set("urlBoton", urlBoton);

		Objeto respuesta = respuesta();
		respuesta.set("claveActiva", "");
		respuesta.set("contenido", contenido);

		LogBB.evento(contexto, "CONTENIDO_FINALIZAR", respuesta);
		return respuesta;
	}

	public static Object obtenerSiguienteVista(ContextoBB contexto) {
		String vistaActual = contexto.parametros.string("vista_actual", null);
		String urlQr = contexto.parametros.string("url_qr", null);
		String flujo = contexto.parametros.string("flujo", null);

		SesionBB sesion = contexto.sesion();
		if (empty(flujo) && !empty(urlQr)) {
			flujo = contexto.sesion().inicializarFlujo(contexto, urlQr);
			if(GeneralBB.FLUJO_TCV.equals(flujo)){
				flujo = GeneralBB.FLUJO_ONBOARDING;
			}
		} else if (empty(flujo) && empty(urlQr)) {
			flujo = sesion.getFlujo();
		}

		BBPlantillaFlujoBuhobank plantillaFlujo = SqlBuhoBank.obtenerPlantillaPorFlujo(contexto, flujo).tryGet();
		if (plantillaFlujo == null) {
			LogBB.evento(contexto, "SIGUIENTE_VISTA", "ERROR_FLUJO_NO_ENCONTRADO");
			return respuesta("ERROR_FLUJO_NO_ENCONTRADO");
		}

		BBVistasBuhobank vistas = obtenerVistasRedis(contexto, plantillaFlujo.id);
		if (vistas == null) {
			LogBB.evento(contexto, "SIGUIENTE_VISTA", "ERROR_BUSCAR_VISTAS");
			return respuesta("ERROR_BUSCAR_VISTAS");
		}

		BBVistaBuhobank siguienteVista = BBVistasBuhobank.obtenerSiguienteVista(contexto, vistas, vistaActual);
		if (siguienteVista == null) {
			LogBB.evento(contexto, "SIGUIENTE_VISTA", "ERROR_SIGUIENTE_VISTA");
			return respuesta("ERROR_SIGUIENTE_VISTA");
		}

		Objeto respuesta = respuesta();
		if (vistas.size() == siguienteVista.orden_vista) {
			respuesta.set("estado", "ULTIMA_VISTA");
		}

		respuesta.set("vista_actual", vistaActual);
		respuesta.set("siguiente_vista", siguienteVista.codigo_vista);
		respuesta.set("evento", siguienteVista.evento);

		respuesta.set("flujo.nombre", flujo);
		respuesta.set("flujo.logo_principal", sesion.getLogoPrincipal(flujo));
		respuesta.set("flujo.logo_secundario", sesion.getLogoSecundario(flujo));
		respuesta.set("flujo.color_principal", sesion.getColorPrincipal(flujo));
		respuesta.set("flujo.color_secundario", sesion.getColorSecundario(flujo));

		BBVistasBuhobank vistasHabilitadas = BBVistasBuhobank.obtenerHabilitados(vistas);
		respuesta.set("total_step", vistasHabilitadas.size());
		respuesta.set("step", BBVistasBuhobank.calcularOrden(vistasHabilitadas, siguienteVista.codigo_vista));

		respuesta.set("sesion", contexto.sesion().getSesion());
		respuesta.set("sesion.intentos_vu", BBSeguridad.getIntentosVU(contexto));
		respuesta.set("contenidos", getContenidoVista(contexto, siguienteVista.contenido));

		LogBB.evento(contexto, "SIGUIENTE_VISTA", respuesta);

		return respuesta;
	}

	private static Object getContenidoVista(ContextoBB contexto, String contenidos) {

		if (empty(contenidos)) {
			return null;
		}

		List<Object> aux = new ArrayList<>(List.of(contenidos.split("\\|")));
		return getDatosContenidos(contexto, contexto.sesion().getFlujo(), aux);
	}

	private static BBVistasBuhobank obtenerVistasRedis(ContextoBB contexto, Integer idPlantilla) {

		try {

			String clave = GeneralBB.REDIS_BB_VISTAS;

			String bbVistasRedisStr = contexto.get(clave);
			if (!empty(bbVistasRedisStr)) {

				BBVistaBuhobank[] bbVistasRedis = fromJson(bbVistasRedisStr, BBVistaBuhobank[].class);
				BBVistasBuhobank bbVistas = new BBVistasBuhobank();

				for (BBVistaBuhobank bbVistaRedis : bbVistasRedis) {
					bbVistas.add(bbVistaRedis);
				}

				return BBVistasBuhobank.buscarIdPlantilla(bbVistas, idPlantilla);
			}

			BBVistasBuhobank vistasBuhobank = SqlBuhoBank.obtenerVistasBuhobank(contexto).tryGet();
			contexto.set(clave, vistasBuhobank.toString(), GeneralBB.BB_REDIS_EXPIRACION);

			return BBVistasBuhobank.buscarIdPlantilla(vistasBuhobank, idPlantilla);

		} catch (Exception e) {

			BBVistasBuhobank vistasBuhobank = SqlBuhoBank.obtenerVistasBuhobank(contexto).tryGet();
			return BBVistasBuhobank.buscarIdPlantilla(vistasBuhobank, idPlantilla);
		}
	}

	public static Object obtenerContenidoDinamico(ContextoBB contexto) {

		if (contexto.sesion().usuarioLogueado()) {
			LogBB.evento(contexto, "REQUEST_CONTENIDO_DINAMICO", contexto.parametros);
		}

		String flujo = contexto.parametros.string("flujo", GeneralBB.FLUJO_ONBOARDING);
		String tipo = contexto.parametros.string("tipo", null);

		if (empty(tipo)) {
			if (contexto.sesion().usuarioLogueado()) {
				LogBB.evento(contexto, "CONTENIDO_DINAMICO", "ERROR_TIPO_VACIO");
			}
			return respuesta("ERROR_TIPO_VACIO");
		}

        Objeto respuesta = respuesta();
        respuesta.set("contenido", new ArrayList<>());

        BBContenidosDinamicoBuhobank contenidosBuhobank = SqlBuhoBank.obtenerContenidosDinamicoByTipo(contexto, flujo, tipo).tryGet();
        if (contenidosBuhobank == null) {
            if (contexto.sesion().usuarioLogueado()) {
                LogBB.evento(contexto, "CONTENIDO_DINAMICO", "ERROR_CONTENIDO_VACIO");
            }
            return respuesta;
        }

        Boolean esTipoPromo = BBContenidosDinamicoBuhobank.TIPO_LANDING_PROMOCIONES.equals(tipo);
        String textoTycInicio = "";
        String textoTycPromos = "";
        String textoTycFinal = "";

        if (esTipoPromo) {
            BBContenidosDinamicoBuhobank contenidosTycBuhobank = SqlBuhoBank.obtenerContenidosDinamicoByTipo(contexto, flujo, BBContenidosDinamicoBuhobank.TIPO_LANDING_PROMOCIONES_TYC).tryGet();
            if (contenidosTycBuhobank != null && contenidosTycBuhobank.size() > 0) {

                respuesta.set("contenido_tyc.id", contenidosTycBuhobank.get(0).id);
                respuesta.set("contenido_tyc.imagen", contenidosTycBuhobank.get(0).imagen);
                respuesta.set("contenido_tyc.titulo", contenidosTycBuhobank.get(0).titulo);
                respuesta.set("contenido_tyc.texto_items", null);
                respuesta.set("contenido_tyc.texto_legales", null);
                respuesta.set("contenido_tyc.texto_legales_items", null);
                textoTycInicio = contenidosTycBuhobank.get(0).texto;
                textoTycFinal = contenidosTycBuhobank.get(0).texto_legales;
            } else {
                esTipoPromo = false;
                respuesta.set("contenido_tyc", null);
            }
        }

        contenidosBuhobank = BBContenidosDinamicoBuhobank.filtrarPlantillasHabilitadas(contenidosBuhobank);

        Objeto contenidos = respuesta.set("contenido");

        for (int i = 0; i < contenidosBuhobank.size(); i++) {

            BBContenidoDinamicoBuhobank contenidoBuhobank = contenidosBuhobank.get(i);

            Objeto contenido = contenidos.add();

            contenido.set("id", contenidoBuhobank.id);
            contenido.set("imagen", contenidoBuhobank.imagen);
            contenido.set("titulo", contenidoBuhobank.titulo);

            if (esTipoPromo && !empty(contenidoBuhobank.texto_tyc)) {
                textoTycPromos += contenidoBuhobank.texto_tyc;
            }

            if (contenidoBuhobank.texto != null && contenidoBuhobank.texto.contains("|")) {
                contenido.set("texto", null);
                contenido.set("texto_items", getTextoItems(contenidoBuhobank.texto));
            } else {
                contenido.set("texto", contenidoBuhobank.texto);
                contenido.set("texto_items", null);
            }

            if (contenidoBuhobank.texto_legales != null && contenidoBuhobank.texto_legales.contains("|")) {
                contenido.set("texto_legales", null);
                contenido.set("texto_legales_items", getTextoItems(contenidoBuhobank.texto_legales));
            } else {
                contenido.set("texto_legales", contenidoBuhobank.texto_legales);
                contenido.set("texto_legales_items", null);
            }
        }

        if (esTipoPromo) {
            String textoTyc = textoTycInicio + textoTycPromos + textoTycFinal;
            respuesta.set("contenido_tyc.texto", !empty(textoTyc) ? textoTyc : null);
        }

        if (contexto.sesion().usuarioLogueado()) {
            LogBB.evento(contexto, "CONTENIDO_DINAMICO", respuesta);
        }

        return respuesta;
    }

    private static Object getTextoItems(String texto) {
        return texto.split("\\|");
    }

    public static Object obtenerContenidoDinamicoPaquete(ContextoBB contexto) {

        if (contexto.sesion().usuarioLogueado()) {
            LogBB.evento(contexto, "REQUEST_CONTENIDO_DINAMICO_PAQUETE", contexto.parametros);
        }

        String idPaquete = contexto.parametros.string("id_paquete");
        String tipo = contexto.parametros.string("tipo");

        if (empty(tipo)) {
            return respuesta("ERROR_TIPO_VACIO");
        }

        BBContenidosDinamicoPaqueteBuhobank contenido = SqlBuhoBank.obtenerContenidoDinamicoPaquete(contexto, idPaquete, tipo).tryGet();

        if (contexto.sesion().usuarioLogueado()) {
            LogBB.evento(contexto, "CONTENIDO_DINAMICO_PAQUETE", contenido.toString());
        }

        return respuesta("contenido", contenido != null ? contenido : new ArrayList<>());
    }

    public static Object obtenerContenidoDinamicoTipos(ContextoBB contexto) {

        String flujo = contexto.parametros.string("flujo", GeneralBB.FLUJO_ONBOARDING);
        Objeto tipos = contexto.parametros.objeto("tipos", null);

        if (empty(tipos)) {
            return respuesta("ERROR_TIPOS_VACIO");
        }

        List<Object> tiposAux = tipos.toList();
        if (tiposAux.size() == 0) {
            return respuesta("ERROR_TIPOS_VACIO");
        }

        return respuesta("datos", getDatosContenidos(contexto, flujo, tiposAux));
    }

    public static List<Object> getDatosContenidos(ContextoBB contexto, String flujo, List<Object> tiposAux) {

        List<Object> datos = new ArrayList<Object>();

        for (Object tipoAux : tiposAux) {

            String tipo = (String) tipoAux;

            Objeto dato = new Objeto();
            dato.set("tipo", tipo);
            dato.set("contenido", new ArrayList<>());

            BBContenidosDinamicoBuhobank contenidosBuhobank = SqlBuhoBank.obtenerContenidosDinamicoByTipo(contexto, flujo, tipo).tryGet();
            if (contenidosBuhobank == null || contenidosBuhobank.size() == 0) {
                continue;
            }

            contenidosBuhobank = BBContenidosDinamicoBuhobank.filtrarPlantillasHabilitadas(contenidosBuhobank);

            Objeto contenidos = dato.set("contenido");

            for (int i = 0; i < contenidosBuhobank.size(); i++) {

                BBContenidoDinamicoBuhobank contenidoBuhobank = contenidosBuhobank.get(i);

                Objeto contenido = contenidos.add();

                contenido.set("id", contenidoBuhobank.id);
                contenido.set("imagen", contenidoBuhobank.imagen);
                contenido.set("titulo", contenidoBuhobank.titulo);

                if (contenidoBuhobank.texto != null && contenidoBuhobank.texto.contains("|")) {
                    contenido.set("texto", null);
                    contenido.set("texto_items", getTextoItems(contenidoBuhobank.texto));
                } else {
                    contenido.set("texto", contenidoBuhobank.texto);
                    contenido.set("texto_items", null);
                }

                if (contenidoBuhobank.texto_legales != null && contenidoBuhobank.texto_legales.contains("|")) {
                    contenido.set("texto_legales", null);
                    contenido.set("texto_legales_items", getTextoItems(contenidoBuhobank.texto_legales));
                } else {
                    contenido.set("texto_legales", contenidoBuhobank.texto_legales);
                    contenido.set("texto_legales_items", null);
                }
            }

            datos.add(dato);
        }

        return datos;
    }

}
