package ar.com.hipotecario.canal.officebanking;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.empresas.ApiEmpresas;
import ar.com.hipotecario.backend.servicio.api.empresas.CrearComercioRequest;
import ar.com.hipotecario.backend.servicio.api.empresas.GrupoEconomicoOB;
import ar.com.hipotecario.backend.servicio.api.empresas.PosicionConsolidadaOB;
import ar.com.hipotecario.backend.servicio.api.personas.ApiPersonas;
import ar.com.hipotecario.backend.servicio.api.personas.Cliente;
import ar.com.hipotecario.backend.servicio.api.personas.Domicilios;
import ar.com.hipotecario.backend.servicio.api.personas.Domicilios.Domicilio;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadoUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioModoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioRolOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.*;

public class OBEmpresas extends ModuloOB {

    private static final int ESTADO_USUARIO_HABILITADO = 1;
    private static final int ROL_ADMIN = 1;

    public static Object posicionConsolidada(ContextoOB contexto) {
        SesionOB sesion = contexto.sesion();
        PosicionConsolidadaOB pc = ApiEmpresas.posicionConsolidada(contexto, sesion.empresaOB.idCobis).get();
        return respuesta("datos", pc);
    }

    public static Object grupoEconomico(ContextoOB contexto) {
        SesionOB sesion = contexto.sesion();
        GrupoEconomicoOB grupo = ApiEmpresas.grupoEconomico(contexto, sesion.empresaOB.idCobis).get();
        return respuesta("datos", grupo.clientes);
    }

    public static Object empresasConAdmin(ContextoOB contexto) {
        Objeto lstEmpresas = new Objeto();
        ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
        ServicioRolOB servicioRol = new ServicioRolOB(contexto);

        ServicioEstadoUsuarioOB servicioEstado = new ServicioEstadoUsuarioOB(contexto);
        RolOB rolOB = servicioRol.find(ROL_ADMIN).tryGet();
        EstadoUsuarioOB estadoOB = servicioEstado.find(ESTADO_USUARIO_HABILITADO).tryGet();

        List<EmpresaUsuarioOB> empresasUsuario = servicioEmpresaUsuario.findByRolEstado(rolOB, estadoOB).get();
        for (EmpresaUsuarioOB empresaUsuario : empresasUsuario) {
            Objeto emp = new Objeto();
            completarEmpresa(emp, empresaUsuario);
            Objeto admin = new Objeto();
            completarAdmin(admin, empresaUsuario);
            emp.set("admin", admin);
            lstEmpresas.add(emp);
        }
        return respuesta("empresas", lstEmpresas);
    }

    public static Object adminEmpresa(ContextoOB contexto) {
         Long cuit = contexto.parametros.longer("cuit");
        String idCobis = null;

        if (contexto.parametros.existe("idCobis")) {
            idCobis = contexto.parametros.string("idCobis");
        }


        ServicioEmpresaOB servicioEmpresa = new ServicioEmpresaOB(contexto);
        EmpresaOB empresaOB = servicioEmpresa.findByCuit(cuit, idCobis).get();
        if (empty(empresaOB)) {
            return respuesta("NO_EXISTE_EMPRESA");
        }

        ServicioRolOB servicioRol = new ServicioRolOB(contexto);
        ServicioEstadoUsuarioOB servicioEstado = new ServicioEstadoUsuarioOB(contexto);
        RolOB rol = servicioRol.find(ROL_ADMIN).tryGet();
        EstadoUsuarioOB estado = servicioEstado.find(ESTADO_USUARIO_HABILITADO).tryGet();
        ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
        List<EmpresaUsuarioOB> empresaUsuarios = servicioEmpresaUsuario.findByRolEmpresa(rol, empresaOB, estado).get();

        Objeto admins =  new Objeto();
        for (EmpresaUsuarioOB empresaUsuario : empresaUsuarios) {
            Objeto admin = new Objeto();
            completarAdmin(admin, empresaUsuario);
            admins.add(admin);
        }

        Objeto emp =  new Objeto();
        emp.set("cuit", empresaOB.cuit);
        emp.set("idCobis", empresaOB.idCobis);
        emp.set("razonSocial", empresaOB.razonSocial);
        if (admins.isEmpty()) {
        	emp.set("admins", new ArrayList<>());
        } else {
            emp.set("admins", admins);
        }

        return respuesta("empresa", emp);
    }

    public static Object usuariosEmpresa(ContextoOB contexto) {
        Long cuitEmpresa = contexto.parametros.longer(":cuitempresa");
        String usuarioCrm = contexto.request.headers("x-usuario");

        ServicioEmpresaOB servicioEmpresaOB = new ServicioEmpresaOB(contexto);
        EmpresaOB empresaOB = servicioEmpresaOB.findByCuit(cuitEmpresa,null).get();
        if (empty(empresaOB)) {
            return respuesta("EMPRESA_INEXISTENTE");
        }

        ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
        //List<EmpresaUsuarioOB> listaEmpresaUsuarioOBS = servicioEmpresaUsuario.findUsuariosByIdCobisEmpresa(idCobisEmpresa).get();
        List<EmpresaUsuarioOBLite> listaEmpresaUsuarioOBS = servicioEmpresaUsuario.findUsuariosByEmpresaLite(empresaOB).get();
        if (empty(listaEmpresaUsuarioOBS)) {
            return respuesta("NO_EXISTEN_USUARIOS_ASOCIADOS_A_EMPRESA");
        }

        Objeto usuariosEmpresa = new Objeto();
        for (EmpresaUsuarioOBLite a:listaEmpresaUsuarioOBS){
            Objeto usuario = new Objeto();
            usuario.set("idCobis",a.usuario.idCobis);
            usuario.set("codigo",a.usuario.codigo);
            usuario.set("numeroDocumento",a.usuario.numeroDocumento);
            usuario.set("cuil",a.usuario.cuil);
            usuario.set("nombre",a.usuario.nombre);
            usuario.set("apellido",a.usuario.apellido);
            usuario.set("email",a.usuario.email);
            usuario.set("telefonoMovil",a.usuario.telefonoMovil);
            usuario.set("telefonoLaboral",a.usuario.telefonoLaboral);
            usuario.set("estado",a.usuario.estado.codigo);
            usuario.set("descripcionEstado",a.usuario.estado.descripcion);
            usuario.set("usuarioConcat",a.usuario.nombre +" "+a.usuario.apellido+" | "+a.usuario.cuil);
            usuariosEmpresa.add(usuario);
        }
        LogCrmOB.evento(contexto,empresaOB.emp_codigo,0,"Se consultaron los usuarios de la empresa: "+cuitEmpresa,usuarioCrm);
        return respuesta("usuariosEmpresa", usuariosEmpresa);
    }

    private static void completarEmpresa(Objeto emp, EmpresaUsuarioOB empresaUsuario) {
        emp.set("cuit", empresaUsuario.empresa.cuit);
        emp.set("idCobis", empresaUsuario.empresa.idCobis);
        emp.set("razonSocial", empresaUsuario.empresa.razonSocial);
    }

    private static void completarAdmin(Objeto admin, EmpresaUsuarioOB empresaUsuario) {
        admin.set("dni", empresaUsuario.usuario.numeroDocumento);
        admin.set("cuil", empresaUsuario.usuario.cuil);
        admin.set("nombre", empresaUsuario.usuario.nombre);
        admin.set("apellido", empresaUsuario.usuario.apellido);
        admin.set("email", empresaUsuario.usuario.email);
        admin.set("telefonoLaboral", empresaUsuario.usuario.telefonoLaboral);
        admin.set("telefonoMovil", empresaUsuario.usuario.telefonoMovil);
        admin.set("idCobis", empresaUsuario.usuario.idCobis);
    }

    public static Object crearComercio(ContextoOB contexto) {
    	SesionOB sesion = contexto.sesion();
		String cuit = sesion.empresaOB.cuit.toString();
		String email = sesion.usuarioOB.email.toString();
		String razonSocial = sesion.empresaOB.razonSocial.toString();
		Boolean altaComercioModo;

	    CrearComercioRequest request = new CrearComercioRequest();
		request.setCuit(cuit);
		request.setEsExceptuadoIVA(false);
		request.setEsPersonaJuridica(true);
		request.setSegmento("SMALL");
		request.setEmail(email);
		request.setRazonSocial(razonSocial);
		request.setNombreFantasia(razonSocial);
		try {
			Cliente cliente = obtenerCliente(contexto,sesion.empresaOB.idCobis);
			if(cliente!=null) {
				request.setCodigoActividadAFIP(cliente.actividadAFIP);
			}

			Domicilios listaDomicilios = obtenerDireccionEmpresa(contexto,cuit);
			if(listaDomicilios != null && !listaDomicilios.isEmpty()) {
				Domicilio domicilio = listaDomicilios.get(0);
				CrearComercioRequest.ComercioDomicilio comercioDomicilio = new CrearComercioRequest.ComercioDomicilio();
				comercioDomicilio.setLatitud(domicilio.latitud != null ? Integer.valueOf(domicilio.latitud):0);
				comercioDomicilio.setLongitud(domicilio.longitud != null ? Integer.valueOf(domicilio.longitud):0);
				comercioDomicilio.setAltura(domicilio.numero);
				comercioDomicilio.setCalle(domicilio.calle);
				comercioDomicilio.setCodigoPostal(domicilio.idCodigoPostal);
				comercioDomicilio.setCodigoProvincia(domicilio.getCodigoProvincia());
				comercioDomicilio.setLocalidad(domicilio.ciudad);
				request.setDomicilio(comercioDomicilio);
			}

			ApiEmpresas.crearComercio(contexto, request).get();
			altaComercioModo = true;
		}catch(ApiException e) {
			altaComercioModo = false;
			LogOB.error(contexto, e);
		}
		return respuesta("altaComercioModo",altaComercioModo);
	}

    public static boolean altaComercioModoYActualizacionBandera(ContextoOB contexto, ModoOB modoOB) {
        try {
            Objeto responseAltaComercioModo = (Objeto) crearComercio(contexto);
            boolean altaComercioModo = (boolean) responseAltaComercioModo.get("altaComercioModo");

            if (altaComercioModo) {
                ServicioModoOB servicioModo = new ServicioModoOB(contexto);
                if(modoOB!=null) {
                	modoOB.altaComercio=true;
                	modoOB.fechaModificacion = LocalDateTime.now();
                	servicioModo.actualizarModo(modoOB);
                }else {
                	modoOB = new ModoOB();
                	modoOB.empCodigo = contexto.sesion().empresaOB.emp_codigo;
                	modoOB.altaComercio=true;
                	modoOB.fechaAlta = LocalDateTime.now();
                	servicioModo.create(modoOB);
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private static Domicilios obtenerDireccionEmpresa(ContextoOB contexto, String cuit) {
		Domicilios domicilios = ApiPersonas.domicilios(contexto, cuit, true).get();
		return domicilios;
	}

    private static Cliente obtenerCliente(ContextoOB contexto, String idCliente) {
    	Cliente cliente = ApiPersonas.cliente(contexto, idCliente).get();
    	return cliente;
    }
}