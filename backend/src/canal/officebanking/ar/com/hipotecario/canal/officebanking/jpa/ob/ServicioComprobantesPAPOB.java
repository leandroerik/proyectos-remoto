package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.ComprobantePAPOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.ComprobantesPAPOBRepositorio;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import net.bytebuddy.asm.Advice;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ServicioComprobantesPAPOB extends ServicioOB{
    private static ComprobantesPAPOBRepositorio repo;
    public ServicioComprobantesPAPOB(ContextoOB contexto) {
        super(contexto);
        repo = new ComprobantesPAPOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<ComprobantePAPOB> crear(EmpresaOB empresa, UsuarioOB usuario, String nombreArchivo, int convenio, int subConvenio, Integer cantidadFilas,String estado){
        ComprobantePAPOB comprobantePAPOB = new ComprobantePAPOB();
        comprobantePAPOB.empresa = empresa;
        comprobantePAPOB.usuario = usuario;
        comprobantePAPOB.fechaCreacion = LocalDateTime.now();
        comprobantePAPOB.ultimaModificacion = comprobantePAPOB.fechaCreacion;
        comprobantePAPOB.nombreArchivo = nombreArchivo;
        comprobantePAPOB.convenio = convenio;
        comprobantePAPOB.subconvenio = subConvenio;
        comprobantePAPOB.cantidad_filas = cantidadFilas;
        comprobantePAPOB.estado = estado;
        return futuro(()->repo.create(comprobantePAPOB));
    }

    public Futuro<List<ComprobantePAPOB>> listar(ContextoOB contexto, int nroAdh, int nroConv, int nroSubConv, LocalDateTime fechaD, LocalDateTime fechaH){
       return futuro(()->repo.listar(contexto, nroAdh,  nroConv,  nroSubConv,  fechaD,  fechaH));
    }

    public Futuro<ComprobantePAPOB> find(Long id) {
        return futuro(() -> repo.find(id));
    }

}
