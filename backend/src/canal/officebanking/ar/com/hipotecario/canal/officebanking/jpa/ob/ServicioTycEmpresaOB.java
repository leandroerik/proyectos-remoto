package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.tyc.EmpresaTycProducto;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.tyc.TycProducto;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EmpresaTycProductoOBRepositorio;

public class ServicioTycEmpresaOB extends ServicioOB {

    private static EmpresaTycProductoOBRepositorio repo;

    public ServicioTycEmpresaOB(ContextoOB contexto) {
        super(contexto);
        repo = new EmpresaTycProductoOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<TycProducto> findTycByVersioProductoEmpresa(Integer version, Integer producto, Integer empresa) {
        return futuro(() -> repo.findTycByVersioProductoEmpresa(version, producto, empresa));
    }
    
    public Futuro<EmpresaTycProducto> create(EmpresaTycProducto empresaTycProducto) {
		return futuro(() -> repo.create(empresaTycProducto));
	}

    public void delete(EmpresaTycProducto empresaTycProducto) {
		 repo.delete(empresaTycProducto);
	}
//    
//    public Futuro<DepositoRemotoOBTyC> update(DepositoRemotoOBTyC depositoRemoto) {
//        return futuro(() -> repo.update(depositoRemoto));
//    }
//
//    public Futuro<DepositoRemotoOBTyC> crear(ContextoOB contexto, DepositoRemotoOBTyC parametria, int version) {
//    	DepositoRemotoOBTyC depositoRemoto = new DepositoRemotoOBTyC();
//        SesionOB sesion = contexto.sesion();
//
//        //depositoRemoto.id = ;
//        depositoRemoto.fechaCreacion = LocalDateTime.now();
//        depositoRemoto.version = version;
//        depositoRemoto.idEmpresa = sesion.empresaOB.emp_codigo;
//
//        return futuro(() -> repo.create(depositoRemoto));
//    }

	public Futuro<EmpresaTycProducto> findEmpresaYProductoById(Integer empCodigo, int idProductoTyc) {
		return futuro(() -> repo.findEmpresaYProductoById(empCodigo, idProductoTyc));
	}

}