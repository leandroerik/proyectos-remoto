package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.PagoBeneficiariosOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.PagoBeneficiariosOBRepositorio;

import java.sql.Blob;
import java.time.LocalDateTime;
import java.util.List;

public class ServicioPagoBeneficiariosOB extends ServicioOB {
    private static PagoBeneficiariosOBRepositorio repo;
    private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");

    public ServicioPagoBeneficiariosOB(ContextoOB contexto) {
        super(contexto);
        repo = new PagoBeneficiariosOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<PagoBeneficiariosOB> crear(ContextoOB contexto, String cuentaOrigen, String nombreArchivo, Blob archivo, Integer cantidadRegistros, Integer convenio, Integer subconvenio) {
        PagoBeneficiariosOB pagoBeneficiario = new PagoBeneficiariosOB();

        pagoBeneficiario.emp_codigo = contexto.sesion().empresaOB;
        pagoBeneficiario.ultimaModificacion = LocalDateTime.now();
        pagoBeneficiario.usuario = contexto.sesion().usuarioOB;
        pagoBeneficiario.nombreArchivo = nombreArchivo;
        pagoBeneficiario.cuentaOrigen = cuentaOrigen;
        pagoBeneficiario.convenio = convenio;
        pagoBeneficiario.fechaCreacion = LocalDateTime.now();
        pagoBeneficiario.fechaCreacion = LocalDateTime.now();
        pagoBeneficiario.ultimaModificacion = LocalDateTime.now();
        pagoBeneficiario.fechaUltActulizacion = LocalDateTime.now();
        pagoBeneficiario.usuarioModificacion = contexto.sesion().usuarioOB.cuil.toString();
        pagoBeneficiario.archivo = archivo;
        pagoBeneficiario.cantidadRegistros = cantidadRegistros;
        pagoBeneficiario.subconvenio = subconvenio;

        return futuro(() -> repo.create(pagoBeneficiario));
    }

    public Futuro<PagoBeneficiariosOB> find(Integer id) {
        return futuro(() -> repo.find(id));
    }

    public Futuro<PagoBeneficiariosOB> update(PagoBeneficiariosOB pago) {
        pago.ultimaModificacion = LocalDateTime.now();
        pago.fechaUltActulizacion = LocalDateTime.now();
        return futuro(() -> repo.update(pago));
    }

    public Futuro<List<PagoBeneficiariosOB>> buscarPorArchivo(String nombreArchivo){
        return futuro(()->repo.existeArchivo(nombreArchivo));
    }

}
