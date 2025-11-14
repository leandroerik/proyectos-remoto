package ar.com.hipotecario.backend.servicio.api.personas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.personas.Telefonos.Telefono;
import org.apache.commons.lang3.StringUtils;

public class Telefonos extends ApiObjetos<Telefono> {

    public static String GET_TELEFONOS = "Telefonos";
    public static String POST_TELEFONO = "CrearTelefono";
    public static String PATCH_TELEFONO = "ActualizarTelefono";
    public static String DELETE_TELEFONO = "BorrarTelefono";

    /* ========== ATRIBUTOS ========== */
    public static class Telefono extends ApiObjeto {
        // cobis: SELECT * FROM cobis.dbo.cl_catalogo WHERE tabla IN (SELECT codigo FROM
        // cobis.dbo.cl_tabla WHERE tabla = 'cl_ttelefono')
        public static String PARTICULAR = "P";
        public static String CELULAR = "E";
        public static String FAX = "F";
        public static String COMERCIAL = "C";
        public static String LABORAL = "L";
        public static String CELULAR_LABORAL = "CL";
        public static String LABORAL_ANTERIOR = "B";
        public static String REFERENCIA = "RL";
        public static String CELULAR_REFERENCIA = "R";
        public static String CONTACTO = "T";
        public static String CANAL_MODIFICACION_BATCH = "BATCH";

        public String id;
        public String idTipoTelefono;
        public String codigoPais;
        public String codigoArea;
        public String prefijo;
        public String caracteristica;
        public String numero;
        public String interno;
        public String idTelefonoPertenencia;
        public String idCore;
        public String prioridad;
        public Boolean esListaNegra;
        public String numeroNormalizado;
        public String canalModificacion;
        public Fecha fechaCreacion;
        public String usuarioModificacion;
        public Fecha fechaModificacion;
        public Boolean esParaRecibirSMS;
        public Boolean esContacto;
        public String idDireccion;
        public String etag;

        public Fecha fecha() {
            return Fecha.maxima(fechaCreacion, fechaModificacion);
        }

        public Telefono actualizar(NuevoTelefono telefono) {
            this.codigoPais = telefono.codigoPais;
            this.codigoArea = telefono.codigoArea;
            this.prefijo = telefono.prefijo;
            this.caracteristica = telefono.caracteristica;
            this.numero = telefono.numero;
            return this;
        }

        public String numero() {
            return codigoArea + prefijo + caracteristica + numero;
        }

        public String numeroMigracion() {
            String codigo = StringUtils.isNotBlank(codigoPais) && codigoPais.startsWith("0")
                    ? codigoPais.substring(1) : codigoPais;
            return "+" + codigo + "9" + (codigoArea.startsWith("0") ? codigoArea.substring(1) : codigoArea) + caracteristica + numero;
        }

        public String sms() {
            String numero = numero();
            while (numero != null && numero.startsWith("0")) {
                numero = numero.substring(1);
            }
            return numero;
        }

        public String enmascarado() {
            String numero = numero();
            return numero.substring(0, 2) + "***" + numero.substring(numero.length() - 2);
        }

        public static String getNumeroCompleto(Telefono telefono) {
            String numero = "";
            if (!empty(telefono.codigoArea) && !empty(telefono.prefijo) && !empty(telefono.caracteristica) && !empty(telefono.numero)) {
                numero = telefono.codigoArea + telefono.prefijo + telefono.caracteristica + telefono.numero;
                numero = numero.replaceFirst("^0+", "");
                numero = numero.replaceAll("\\s", "");
            }

            return numero;
        }

        public static String obtenerCodigoPais() {
            return "054";
        }

        public static String obtenerCodigoArea(String codArea) {
            return "0" + codArea;
        }

        public static String obtenerPrefijo() {
            return "15";
        }

        public static String obtenerCaracteristica(String codArea, String celular) {
            if (codArea.length() == 4) {
                return celular.length() >= 2 ? celular.substring(0, 2) : celular;
            }

            if (codArea.length() == 3) {
                return celular.length() >= 3 ? celular.substring(0, 3) : celular;
            }

            return celular.length() >= 4 ? celular.substring(0, 4) : celular;
        }

        public static String obtenerNumero(String codArea, String celular) {
            if (codArea.length() == 4) {
                return celular.length() >= 2 ? celular.substring(2) : "";
            }

            if (codArea.length() == 3) {
                return celular.length() >= 3 ? celular.substring(3) : "";
            }

            return celular.length() >= 4 ? celular.substring(4) : "";
        }

        public static Boolean esTelefonoValido(String codArea, String celular) {
            if (codArea.length() == 4 && celular.length() == 6)
                return true;
            if (codArea.length() == 3 && celular.length() == 7)
                return true;
            if (codArea.length() == 2 && celular.length() == 8)
                return true;

            return false;
        }
    }

    /* ========== CLASES ========== */
    public static class NuevoTelefono {
        public String codigoPais;
        public String codigoArea;
        public String prefijo;
        public String caracteristica;
        public String numero;
    }

    /* ========== SERVICIOS ========== */
    // API-Personas_ConsultarTelefonosDePersona
    public static Telefonos get(Contexto contexto, String cuit, Boolean cache) {
        ApiRequest request = new ApiRequest(GET_TELEFONOS, ApiPersonas.API, "GET", "/personas/{id}/telefonos", contexto);
        request.header(ApiPersonas.X_USUARIO, contexto.usuarioCanal());
        request.path("id", cuit);
        request.cache = cache;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200) && !response.contains("no arrojó resultados en BUP"), request, response);
        return response.crear(Telefonos.class);
    }

    // API-Personas_AltaTelefonosAPersona
    public static Telefono post(Contexto contexto, String cuit, NuevoTelefono telefono, String tipo) {
        ApiRequest request = new ApiRequest(POST_TELEFONO, ApiPersonas.API, "POST", "/personas/{id}/telefonos", contexto);
        request.header(ApiPersonas.X_USUARIO, contexto.usuarioCanal());
        request.path("id", cuit);
        request.body("idTipoTelefono", tipo);
        request.body("codigoPais", telefono.codigoPais);
        request.body("codigoArea", telefono.codigoArea);
        request.body("prefijo", telefono.prefijo);
        request.body("caracteristica", telefono.caracteristica);
        request.body("numero", telefono.numero);

        ApiResponse response = request.ejecutar();
        Api.eliminarCache(contexto, GET_TELEFONOS, cuit);
        ApiException.throwIf("EXISTE_TIPO_TELEFONO", response.contains("Ya existe un telefono"), request, response);
        ApiException.throwIf(!response.http(201), request, response);
        return response.crear(Telefono.class);
    }

	// API-Personas_ModificarParcialmenteTelefono
	public static Telefono patch(Contexto contexto, String cuit, Telefono telefono) {
		ApiRequest request = new ApiRequest(PATCH_TELEFONO, ApiPersonas.API, "PATCH", "/telefonos/{id}", contexto);
		request.header(ApiPersonas.X_USUARIO, contexto.usuarioCanal());
		request.path("id", telefono.id);
		request.body("codigoPais", telefono.codigoPais);
		request.body("codigoArea", telefono.codigoArea);
		request.body("prefijo", telefono.prefijo);
		request.body("caracteristica", telefono.caracteristica);
		request.body("numero", telefono.numero);
		request.body("canalModificacion", contexto.canal());
		request.body("usuarioModificacion", contexto.usuarioCanal());

        ApiResponse response = request.ejecutar();
        Api.eliminarCache(contexto, GET_TELEFONOS, cuit);
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(Telefono.class);
    }

    public static Telefono delete(Contexto contexto, String cuit, Telefono telefono) {
        ApiRequest request = new ApiRequest(DELETE_TELEFONO, ApiPersonas.API, "DELETE", "/telefonos/{id}", contexto);
        request.header(ApiPersonas.X_USUARIO, contexto.usuarioCanal());
        request.path("id", telefono.id);

        ApiResponse response = request.ejecutar();
        Api.eliminarCache(contexto, GET_TELEFONOS, cuit);
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(Telefono.class);
    }

    /* ========== METODOS ========== */
    public Telefono crearActualizar(Contexto contexto, String cuit, NuevoTelefono nuevoTelefono, String tipo) {
        Telefono telefono = buscar(tipo);
        if (telefono == null) {
            telefono = ApiPersonas.crearTelefono(contexto, cuit, nuevoTelefono, tipo).get();
        } else {
            telefono = ApiPersonas.actualizarTelefono(contexto, cuit, telefono.actualizar(nuevoTelefono)).get();
        }
        return telefono;
    }

    public Telefono crearActualizarTry(Contexto contexto, String cuit, NuevoTelefono nuevoTelefono, String tipo) {
        Telefono telefono = buscar(tipo);
        if (telefono == null) {
            telefono = ApiPersonas.crearTelefono(contexto, cuit, nuevoTelefono, tipo).get();
        } else {
            telefono = ApiPersonas.actualizarTelefono(contexto, cuit, telefono.actualizar(nuevoTelefono)).get();
        }
        return telefono;
    }

    private Telefono buscar(String tipo) {
		Telefono dato = null;
		Telefono datoBatch = null;
		for (Telefono telefono : this) {
			if (tipo.equals(telefono.idTipoTelefono)) {
				if(Telefono.CANAL_MODIFICACION_BATCH.equals(telefono.canalModificacion)){
					datoBatch = (datoBatch == null || telefono.fecha().esPosterior(datoBatch.fecha())) ? telefono : datoBatch;
				}
				else{
					dato = (dato == null || telefono.fecha().esPosterior(dato.fecha())) ? telefono : dato;
				}
			}
		}
		return dato != null ? dato : datoBatch;
	}
	
	private Telefono buscarCore(String tipo) {
		Telefono dato = null;
		for (Telefono telefono : this) {
			if (tipo.equals(telefono.idTipoTelefono) && telefono.idCore != null && telefono.idDireccion != null) {
				dato = (dato == null || telefono.fecha().esPosterior(dato.fecha())) ? telefono : dato;
			}
		}
		return dato;
	}

    public Telefono celular() {
        return buscar(Telefono.CELULAR);
    }

    public Telefono celularCore() {
        return buscarCore(Telefono.CELULAR);
    }

    public Telefono laboral() {
        return buscar(Telefono.LABORAL);
    }

    public Telefono particular() {
        return buscar(Telefono.PARTICULAR);
    }

    public Telefono particularCore() {
        return buscarCore(Telefono.PARTICULAR);
    }

    public static Boolean esBatch(Telefono telefono) {

        if (telefono == null) {
            return false;
        }

        return Telefono.CANAL_MODIFICACION_BATCH.equals(telefono.canalModificacion);
    }

    /* ========== TEST ========== */
    @SuppressWarnings("unused")
    public static void main(String[] args) throws InterruptedException {
        Contexto contexto = new Contexto("HB", "homologacion", "133366");
        String cuit = "20269726718";
        String test = "get";

        if ("get".equals(test)) {
            Telefonos datos = get(contexto, cuit, true);
            Telefono celular = datos.celular();
            System.out.println(datos.nombreServicioMW(contexto));
            System.out.println(datos.get(0));
        }
        if ("post".equals(test)) {
            NuevoTelefono telefono = new NuevoTelefono();
            telefono.codigoPais = "054";
            telefono.prefijo = "15";
            telefono.codigoArea = "03465";
            telefono.caracteristica = "41";
            telefono.numero = "8711";
            Telefono datos = post(contexto, cuit, telefono, Telefono.FAX);
            System.out.println(datos.nombreServicioMW(contexto));
            System.out.println(datos);
        }
        if ("patch".equals(test)) {
            NuevoTelefono telefono = new NuevoTelefono();
            telefono.codigoPais = "054";
            telefono.prefijo = "15";
            telefono.codigoArea = "03465";
            telefono.caracteristica = "41";
            telefono.numero = "1213";

            Telefonos telefonos = get(contexto, cuit, true);
            Telefono celular = telefonos.celular();
            Telefono datos = patch(contexto, cuit, celular.actualizar(telefono));
            imprimirResultado(contexto, datos);
        }
        if ("unwrap".equals(test)) {
            String codArea = "2320";
            String celular = "123456";

            System.out.println("Celular: " + codArea + celular);
            System.out.println("EsValido: " + Telefono.esTelefonoValido(codArea, celular));
            System.out.println("CodigoPais: " + Telefono.obtenerCodigoPais());
            System.out.println("CodigoArea: " + Telefono.obtenerCodigoArea(codArea));
            System.out.println("Prefijo: " + Telefono.obtenerPrefijo());
            System.out.println("Caracteristica: " + Telefono.obtenerCaracteristica(codArea, celular));
            System.out.println("Numero: " + Telefono.obtenerNumero(codArea, celular));
        }
    }
}
