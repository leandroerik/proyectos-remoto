package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq;

import ar.com.hipotecario.canal.officebanking.enums.echeq.EnumAccionesEcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import jakarta.persistence.*;


import java.time.LocalDateTime;

@Entity
@Table(schema = "dbo", name = "OB_Echeq")
@NamedQueries({
        @NamedQuery(name = "EcheqOB.buscarPorEstado",query = "SELECT e " +
                "FROM EcheqOB e " +
                "WHERE e.estado = :estado"),
        @NamedQuery(
                name = "EcheqOB.findChequeByEmisorAndIdAndEstado",
                query = "SELECT o FROM EcheqOB o WHERE o.documentoEmisor = :emisorDocumento AND o.estadoBandeja.id IN :estados"
        )})
public class EcheqOB extends BandejaOB {
    @Column(name = "numero_chequera",nullable = false)
    public String numeroChequera;

    @Column(name = "fecha_pago")
    public LocalDateTime fechaPago;
    @Column(name = "tipo_documento_beneficiario",nullable = false)
    public String tipoDocumentoBeneficiario;
    @Column(name = "documento_beneficiario",nullable = false)
    public String documentoBeneficiario;
    @Column(name = "email_beneficiario")
    public String emailBeneficiario;
    @Column(name = "razon_social_beneficiario",nullable = false)
    public String razonSocialBeneficiario;

    @Column(name = "emisor_documento")
    public String documentoEmisor;
    @Column(name = "tipo_documento_emisor")
    public String tipoDocumentoEmisor;

    public int version;
    @Column(name = "a_la_orden",nullable = false)
    public boolean aLaOrden;
    @Column(name = "motivo_pago")
    public String motivoPago;
    @Column(name = "concepto")
    public String concepto;
    @Column(name = "cruzado",nullable = false)
    public boolean cruzado;
    @Column(name = "id_cheque")
    public String idCheque;
    @Column(name = "numero_cheque")
    public String numeroCheque;

    @Column(name = "tipo")
    public String tipo;

    @ManyToOne()
    @JoinColumn(name = "estado", nullable = false)
    public EstadoEcheqOB estado;

    @Column(name = "accion")
    public EnumAccionesEcheqOB accion;

    @Column(name = "tipo_endoso")
    public String tipoEndoso;
    @Column(name = "cesionario_domicilio")
    public String cesionarioDomicilio;
    @Column(name = "cesionario_nombre")
    public String cesionarioNombre;
    @Column(name = "cuenta_deposito")
    public String cuentaDeposito;
    @ManyToOne
    @JoinColumn(name = "usu_codigo")
    public UsuarioOB usuario;




}
