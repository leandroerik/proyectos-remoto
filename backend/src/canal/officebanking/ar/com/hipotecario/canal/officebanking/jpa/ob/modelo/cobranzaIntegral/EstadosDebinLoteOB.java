package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral;

import jakarta.persistence.*;

@Entity
@Table(schema = "dbo",name = "OB_Estados_Debin_Por_Lote")
public class EstadosDebinLoteOB {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    public Integer id;

    @Column(name = "descripcion", nullable = false)
    public String descripcion;
}

