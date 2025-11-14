package ar.com.hipotecario.canal.officebanking;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioParametroOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioTipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioTycEmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.tyc.EmpresaTycProducto;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.tyc.TycProducto;

public class OBTyc extends ModuloOB {

    private static ContextoOB contextoOB = new ContextoOB("OB", Config.ambiente(), "1");
    private static final ServicioParametroOB servicioParametro = new ServicioParametroOB(contextoOB);
    
    public static Object buscarTycByEmpresaAndProducto(ContextoOB contexto) {
    	TycProducto tycProducto = null;
    	Integer numeroProducto = null;
    	String producto = contexto.parametros.string("productoNombre");
    	String versionProducto = null;
    	if(producto.equals("DEPOSITO_REMOTO")) {
    		numeroProducto = EnumTipoProductoOB.DEPOSITO_REMOTO.getCodigo();
    		versionProducto = "depositoRemoto.version"; 
    	}
    	
    	if(producto.equals("ECHEQ")) {
    		numeroProducto = EnumTipoProductoOB.ECHEQ.getCodigo();
    		versionProducto = "echeq.version"; 
    	}
    	
    			
    	ServicioTycEmpresaOB servicioTycEmpresaOB = new ServicioTycEmpresaOB(contexto);
    	   
        SesionOB sesion = contexto.sesion();
        EmpresaOB empresaOB = sesion.empresaOB;
        if (empty(sesion.empresaOB)) {
            return respuesta("EMPRESA_INVALIDA");
        }
        
        try {
        	ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
            TipoProductoFirmaOB operaciones = servicioTipoProductoFirma.findByCodigo(numeroProducto).get();
        	Integer version = Integer.valueOf(servicioParametro.find(versionProducto).get().valor);
        	tycProducto = servicioTycEmpresaOB.findTycByVersioProductoEmpresa(version,operaciones.id,empresaOB.emp_codigo).get();
        }catch(Exception e) {
        	return respuesta("ERROR_OBTENER_TYC");
        }

        Objeto datos = new Objeto();
        datos.set("tyc", tycProducto!=null?tycProducto.getObjeto():null);
        datos.set("adheridoGire",sesion.usuarioOB.adheridoGire);
        return respuesta("datos", datos);
    }

    public static Object insertEmpresaProductoTyc(ContextoOB contexto) {
    	Integer idProductoTyc = contexto.parametros.integer("idProdcto");
    	ServicioTycEmpresaOB servicioTycEmpresaOB = new ServicioTycEmpresaOB(contexto);
    	SesionOB sesion = contexto.sesion();
        EmpresaOB empresaOB = sesion.empresaOB;
        if (empty(sesion.empresaOB)) {
            return respuesta("EMPRESA_INVALIDA");
        }
        
    	EmpresaTycProducto empresaTycProducto = new EmpresaTycProducto();
    	empresaTycProducto.setEmpCodigo(empresaOB.emp_codigo);
    	empresaTycProducto.setIdTycProducto(idProductoTyc);
    	try {
    		servicioTycEmpresaOB.create(empresaTycProducto).get();    
    	}catch(Exception e) {
    		return respuesta("ERROR_INSERTAR_EMPRESA_PRODUCTO_TYC");
    	}
    	return respuesta("OK");
    }
    
    public static Object deleteEmpresaProductoTyc(ContextoOB contexto, int idProductoTyc) {
        ServicioTycEmpresaOB servicioTycEmpresaOB = new ServicioTycEmpresaOB(contexto);
        SesionOB sesion = contexto.sesion();
        EmpresaOB empresaOB = sesion.empresaOB;
        if (empty(sesion.empresaOB)) {
            return respuesta("EMPRESA_INVALIDA");
        }
        
        // Cargar la entidad EmpresaTycProducto que se desea eliminar
        EmpresaTycProducto empresaTycProducto = servicioTycEmpresaOB.findEmpresaYProductoById(empresaOB.emp_codigo,idProductoTyc).get();
        if (empresaTycProducto == null) {
            return respuesta("EMPRESA_PRODUCTO_TYC_NO_ENCONTRADO");
        }

        try {
            servicioTycEmpresaOB.delete(empresaTycProducto);    
        } catch(Exception e) {
            return respuesta("ERROR_DELETE_EMPRESA_PRODUCTO_TYC");
        }
        return respuesta("OK");
    }
    
    
//	public static void main(String[] args) {
//		try {
//			ContextoOB contexto = new ContextoOB(null, null, null);
//			 SesionOB sesion = new SesionOB();
//			 //EmpresaOB empresaOB = new EmpresaOB();
//			 //empresaOB.emp_codigo = 241;
//			 //sesion.empresaOB = empresaOB;
//			 contexto.sesion = sesion;
//			Object ob = insertEmpresaProductoTyc(contexto,1);
//			System.out.println(ob);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//    
}