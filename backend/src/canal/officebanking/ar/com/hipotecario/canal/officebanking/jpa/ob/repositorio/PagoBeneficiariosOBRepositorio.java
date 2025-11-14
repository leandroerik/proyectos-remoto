package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.PagoBeneficiariosOB;
import java.util.List;

public class PagoBeneficiariosOBRepositorio extends RepositorioGenericoImpl<PagoBeneficiariosOB> {
    public List<PagoBeneficiariosOB> existeArchivo(String nombreArchivo) {
        return findByField("nombreArchivo", nombreArchivo);
    }
}
