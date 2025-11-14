package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.tyc;

import java.lang.reflect.Field;
import java.util.Date;

import ar.com.hipotecario.backend.base.Objeto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

@Entity
@Table(name = "OB_Tyc_Producto", schema = "dbo")
@NamedQuery(name = "TycProducto.obtenerPorEmpresaVersionYProducto",
query = "SELECT tp FROM TycProducto tp WHERE tp.version = :version AND tp.idProducto = :productoId")
public class TycProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_producto")
    private Integer idProducto;

    private Integer version;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_creacion")
    private Date fechaCreacion;

    @Column(name = "detalle", nullable = false, columnDefinition = "TEXT")
    private String detalle;
    
    @Transient
    private Boolean relacionEmpresa = false;

    public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getIdProducto() {
		return idProducto;
	}

	public void setIdProducto(Integer idProducto) {
		this.idProducto = idProducto;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getDetalle() {
		return detalle;
	}

	public void setDetalle(String detalle) {
		this.detalle = detalle;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public Boolean getRelacionEmpresa() {
		return relacionEmpresa;
	}

	public void setRelacionEmpresa(Boolean relacionEmpresa) {
		this.relacionEmpresa = relacionEmpresa;
	}
	
	public Objeto getObjeto() {
		//Objeto objeto = new Objeto();
		Objeto tyc = new Objeto();
		Class<?> clazz = getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
		       field.setAccessible(true); // Hacer el campo accesible si es privado
		       try {
		           Object valor = field.get(this); // Obtener el valor del campo
		           if (valor instanceof Date) { // Verificar si el valor es de tipo Date
		                // Convertir el valor de Date a String
		                valor = valor.toString();
		            }
		           tyc.set(field.getName(), valor); // Agregar el campo al objeto
		       } catch (IllegalAccessException e) {
		           e.printStackTrace(); // Manejo del error de acceso ilegal
		       }
		}
		//objeto.add("tyc", tyc);
		return tyc;
	}

}