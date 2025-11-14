package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Historial_Rectificacion")
public class HistorialRectificacionComexOB {
    @Id
    @Column(name = "id_inicial")
    public Long idInicial;
    @Column(name = "id_nuevo")
    public Long idNuevo;
    public Character rectificacion;

    public HistorialRectificacionComexOB(Long idInicial, Long idNuevo, Character rectificacion) {
        this.idInicial = idInicial;
        this.idNuevo = idNuevo;
        this.rectificacion = rectificacion;
    }
}
