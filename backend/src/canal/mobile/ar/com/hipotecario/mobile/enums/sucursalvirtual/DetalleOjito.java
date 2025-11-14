package ar.com.hipotecario.mobile.enums.sucursalvirtual;

public enum DetalleOjito {
    PRODUCTO("Aceptación digital"),
    MOTIVO_GENERICO("Alta de Productos"),
    USUARIO_GENERICO("generic_user"),
    RESOLUCION_FAVORABLE("Tu Paquete de Productos y Préstamo Personal están aprobados, pronto los verás activos en Home Banking o App BH."),
    RESOLUCION_DESFAVORABLE("La solicitud ha sido dada de baja por falta de aceptación en los plazos establecidos."),
    PIE_RESOLUCION_FAVORABLE("En caso de haber recibido un archivo adjunto vas a poder descargarlo desde tu mail personal."),
    PIE_RESOLUCION_DESFAVORABLE("Cualquier duda o consulta contacta a tu oficial de negocios"),
    RESOLUCION_DESFAVORABLE_SISTEMA("No aprobamos tu solicitud ya que no cumple con alguna de nuestras políticas vigentes, acércate a una de nuestras sucursales para conocer más."),
    TITULO("Alta de Productos"),
    ;

    private final String mensaje;

    DetalleOjito(String mensaje) {
        this.mensaje = mensaje;
    }

    @Override
    public String toString() {
        return this.mensaje;
    }
}
