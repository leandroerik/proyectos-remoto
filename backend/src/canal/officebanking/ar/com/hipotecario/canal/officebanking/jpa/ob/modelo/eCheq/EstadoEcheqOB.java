package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq;

import jakarta.persistence.*;

@Entity
@Table(schema = "dbo", name = "OB_Estados_Echeq")
public class EstadoEcheqOB {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    public Integer id;

    @Column(name = "descripcion", nullable = false)
    public String descripcion;
}
