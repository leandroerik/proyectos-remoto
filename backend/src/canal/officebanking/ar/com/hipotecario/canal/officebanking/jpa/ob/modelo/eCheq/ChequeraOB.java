package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(schema = "dbo", name = "OB_Echeq_Chequera")
public class ChequeraOB extends BandejaOB {
    @Column(name = "fecha_creacion",nullable = false)
    public LocalDate fechaCreacion;
    @Column(name = "tipo_chequera",nullable = false)
    public String tipoChequera;

    @ManyToOne()
    @JoinColumn(name = "estado", nullable = false)
    public EstadoChequeraOB estado;
    @Column(name = "numero_chequera")
    public String numeroChequera;
    @ManyToOne
    @JoinColumn(name = "usu_codigo")
    public UsuarioOB usuario;

}
