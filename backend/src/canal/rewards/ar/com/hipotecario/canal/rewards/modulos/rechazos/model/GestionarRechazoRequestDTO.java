package ar.com.hipotecario.canal.rewards.modulos.rechazos.model;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class GestionarRechazoRequestDTO extends ApiObjeto {

    /**
     * Archivo de gestión (nombre o identificador del archivo).
     * Campo obligatorio.
     */
    private String archivoGestion;

    /**
     * Código de transacción (no requerido)
     */
    private String transaccion;

    // Constructor vacío (requerido si usás frameworks como Spring)
    public GestionarRechazoRequestDTO() {
    }

    // Constructor completo
    public GestionarRechazoRequestDTO(String archivoGestion, String transaccion) {
        this.archivoGestion = archivoGestion;
        this.transaccion = transaccion;
    }

    public GestionarRechazoRequestDTO(String archivoGestion ) {
        this.archivoGestion = archivoGestion;
        this.transaccion = null;
    }

    // Getters y setters

    public String getArchivoGestion() {
        return archivoGestion;
    }

    public void setArchivoGestion(String archivoGestion) {
        this.archivoGestion = archivoGestion;
    }

    public String getTransaccion() {
        return transaccion;
    }

    public void setTransaccion(String transaccion) {
        this.transaccion = transaccion;
    }
}