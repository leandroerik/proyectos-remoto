package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(schema = "dbo", name = "OB_Archivos_Comex")
public class ArchivosComexOB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    public Integer id;

    @ManyToOne()
    @JoinColumn(name = "id_op", nullable = false)
    public OrdenPagoComexOB idBandeja;

    @Column(name = "fecha_creacion", nullable = false)
    public LocalDateTime fechaCreacion;

    @Column(name = "nombre_archivo", nullable = false)
    public String nombreArchivo;

    @Column(name = "url", nullable = false)
    public String url;

    public ArchivosComexOB() {
    }

    public ArchivosComexOB(int id, OrdenPagoComexOB idBandeja, LocalDateTime fechaCreacion, String nombreArchivo, String url) {
        this.id=id;
        this.idBandeja = idBandeja;
        this.fechaCreacion=fechaCreacion;
        this.nombreArchivo=nombreArchivo;
        this.url=url;
    }
}
