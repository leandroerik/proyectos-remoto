package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.ArchivosComexOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.OrdenPagoComexOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.ArchivosComexOBRepositorio;
import org.apache.commons.lang3.arch.Processor;

import java.time.LocalDateTime;
import java.util.List;

public class ServicioArchivosComexOB extends ServicioOB{
    private static ArchivosComexOBRepositorio repo;
    private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");

    public ServicioArchivosComexOB(ContextoOB contexto) {
        super(contexto);
        repo = new ArchivosComexOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<ArchivosComexOB> find(OrdenPagoComexOB ordenPagoComexOB){
        return futuro(() -> repo.find(ordenPagoComexOB));
    }

    public Futuro<ArchivosComexOB>find(Integer id){
        return futuro(()->repo.find(id));
    }

    public Futuro<List<ArchivosComexOB>> findByOP(OrdenPagoComexOB ordenPagoComexOB){
        return futuro(() -> repo.findByField("idBandeja", ordenPagoComexOB.id));
    }

    public Futuro<ArchivosComexOB> crear(ContextoOB contexto,
                                          OrdenPagoComexOB idBandeja,
                                         LocalDateTime fechaCracion,
                                         String nombreArchivo,
                                         String url

    ) {
        ArchivosComexOB archivo = new ArchivosComexOB();
        archivo.idBandeja=idBandeja;
        archivo.fechaCreacion = fechaCracion;
        archivo.nombreArchivo = nombreArchivo;
        archivo.url = url;
        return futuro(() -> repo.create(archivo));
    }

    public void delete(ArchivosComexOB archivo){
         repo.delete(archivo);
    }

    public void update(ArchivosComexOB archivo){
        repo.update(archivo);}
}
