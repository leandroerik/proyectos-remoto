package ar.com.hipotecario.mobile;

import ar.com.hipotecario.mobile.lib.Objeto;

public class RespuestaMB extends Objeto {

    /* ========== CONSTRUCTOR ========== */
    public RespuestaMB() {
        setEstado("0");
    }

    /* ========== CONSTRUCTORES ESTATICOS ========== */
    public static RespuestaMB exito() {
        return new RespuestaMB();
    }

    public static RespuestaMB exito(String csmIdAuth) {
        return new RespuestaMB().set("csmIdAuth", csmIdAuth);
    }

    public static RespuestaMB exito(String clave, Object valor) {
        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set(clave, valor);
        return respuesta;
    }

    public static RespuestaMB estado(String estado) {
        return new RespuestaMB().setEstado(estado);
    }

    public static RespuestaMB estado(String estado, String csmIdAuth) {
        return new RespuestaMB().setEstado(estado).set("csmIdAuth", csmIdAuth);
    }

    public static RespuestaMB error() {
        return RespuestaMB.estado("ERROR");
    }

    public static RespuestaMB error(String csmIdAuth) {
        return RespuestaMB.estado("ERROR").set("csmIdAuth", csmIdAuth);
    }

    public static RespuestaMB parametrosIncorrectos() {
        return RespuestaMB.estado("PARAMETROS_INCORRECTOS");
    }

    public static RespuestaMB sinPseudoSesion() {
        return RespuestaMB.estado("SIN_PSEUDO_SESION");
    }

    public static RespuestaMB existenErrores() {
        return RespuestaMB.estado("EXISTEN_ERRORES");
    }

    public static RespuestaMB requiereSegundoFactor() {
        return RespuestaMB.estado("REQUIERE_SEGUNDO_FACTOR");
    }

    public static RespuestaMB timeOut() {
        return RespuestaMB.estado("ERROR_TIMEOUT");
    }

    /* ========== METODOS ========== */
    public Boolean hayError() {
        return !string("estado").equals("0");
    }

    public RespuestaMB set(String clave, Object valor) {
        super.set(clave, valor);
        return this;
    }

    public RespuestaMB setEstado(Object valor) {
        set("estado", valor);
        return this;
    }

    public RespuestaMB setEstadoExistenErrores() {
        return setEstado("EXISTEN_ERRORES");
    }

    public RespuestaMB ordenar(String... campos) {
        super.ordenar(campos);
        return this;
    }

    public RespuestaMB unir(RespuestaMB respuestaParcial) {
        for (String clave : respuestaParcial.claves()) {
            Object valor = respuestaParcial.get(clave);
            if ("estado".equals(clave) && !"0".equals(valor)) {
                setEstado(valor);
            } else {
                set(clave, valor);
            }
        }
        return this;
    }
}
