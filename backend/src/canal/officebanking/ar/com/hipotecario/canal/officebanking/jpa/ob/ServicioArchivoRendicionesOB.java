package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.ArchivosRendicionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.ArchivosRendicionesOBRepositorio;

import java.time.LocalDate;


public class ServicioArchivoRendicionesOB extends ServicioOB {
    private static ArchivosRendicionesOBRepositorio repo;

    public ServicioArchivoRendicionesOB(ContextoOB contexto) {
        super(contexto);
        repo = new ArchivosRendicionesOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<ArchivosRendicionesOB> crear(String convenio, LocalDate fecha, EnumTipoProductoOB producto){
        ArchivosRendicionesOB archivo = new ArchivosRendicionesOB();
        archivo.convenio = convenio;
        archivo.fecha=fecha;
        archivo.producto = producto;
        return futuro(()->repo.create(archivo));

    }
}
