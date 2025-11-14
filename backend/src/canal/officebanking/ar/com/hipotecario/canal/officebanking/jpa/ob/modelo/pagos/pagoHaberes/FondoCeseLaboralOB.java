package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@NamedQueries({
@NamedQuery(name = "MovimeintosFCL.buscarByConvenio", query = "SELECT fcl "
        + "FROM FondoCeseLaboralOB fcl "
        + "WHERE fcl.cobFclCoCodConv = :convenio "),

@NamedQuery(name = "MovimeintosFCL.buscarBySecuencia", query = "SELECT fcl "
        + "FROM FondoCeseLaboralOB fcl "
        + "WHERE fcl.cobFclSecuencia = :secuencia ")
})
@Entity
@Table(schema = "dbo", name = "BE_Cob_FCL_PS")
public class FondoCeseLaboralOB {
	
	@Id
	@Column(name = "cob_fcl_secuencia", nullable = false)
	public Long cobFclSecuencia;
	
	@Column(name = "cob_fcl_co_cod_conv", nullable = false)
	public Integer cobFclCoCodConv;

	@Column(name = "cob_fcl_ah_nombre")
	public String cobFclAhNombre;

	@Column(name = "cob_fcl_ti_descripcion")
	public String cobFclTiDescripcion;
	
	@Column(name = "cob_fcl_id_numero")
	public String cobFclIdNumero;
	
	@Column(name = "cob_fcl_valor")
	public String cobFclValor;
	
	@Column(name = "cob_fcl_en_ced_ruc")
	public String cobFclEnCedRuc;
	
	@Column(name = "cob_fcl_ah_cta_banco",  columnDefinition = "char(16)")
	public String cobFclAhCtaBanco;
	
	@Column(name = "cob_fcl_ah_categoria")
	public String cobFclAhCategoria;
	
	@Column(name = "cob_fcl_ah_fecha_aper")
	public LocalDateTime cobFclAhFechaAper;
	
	@Column(name = "cob_fcl_ah_fecha_ult_mov")
	public LocalDateTime cobFclAhFechaUltMov;
	
	@Column(name = "cob_fcl_ah_disponible")
	public BigDecimal cobFclAhDisponible;
	
	@Column(name = "insert_date")
	public LocalDateTime insertDate;

}
