package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia;

import jakarta.persistence.*;

@NamedNativeQuery(
        name = "TransferenciaCredinOB.insertDesdeTransferencia",
        query = "INSERT INTO dbo.OB_Transferencias_Credin (id, id_debin) VALUES (:id, NULL)"
)
@Entity
@Table(schema = "dbo", name = "OB_Transferencias_Credin")
public class TransferenciaCredinOB extends TransferenciaOB{

    @Column(name = "id_debin",nullable = true)
    public String idDebin;
}
