package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo;

import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(schema = "dbo", name = "OB_Archivo_Rendiciones")
public class ArchivosRendicionesOB implements Serializable {
    @Id
    @Column(name = "convenio", nullable = false)
    public String convenio;

    @Id
    @Column(name = "fecha", nullable = false)
    public LocalDate fecha;

    @Id
    @Column(name = "producto", nullable = false)
    public EnumTipoProductoOB producto;

}
