package ar.com.hipotecario.canal.officebanking;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.notificaciones.ApiNotificaciones;
import ar.com.hipotecario.backend.servicio.api.personas.ApiPersonas;
import ar.com.hipotecario.backend.servicio.api.personas.Cuils;
import ar.com.hipotecario.backend.servicio.api.personas.Documentos;
import ar.com.hipotecario.backend.servicio.api.personas.Persona;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoInvitacionOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioInvitacionAdministradorOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioInvitacionOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioPermisosOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioUsuariosEmpresasActivoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.*;
import ar.com.hipotecario.canal.officebanking.util.ValidarCorreoYCelular;

public class OBAdministracion extends ModuloOB {

    public static Object enviarInvitacion(ContextoOB contexto) {
        Long numeroDocumento = contexto.parametros.longer("numeroDocumento");
        String email = contexto.parametros.string("email");
        String nombre = contexto.parametros.string("nombre");
        String apellido = contexto.parametros.string("apellido");

        if (numeroDocumento.toString().length() > 9) {
            numeroDocumento = Long.parseLong(numeroDocumento.toString().substring(2, 10));
        }

        SesionOB sesion = contexto.sesion();

        if (!sesion.esOperadorInicial()) {
            return respuesta("OPERACION_INVALIDA");
        }

        if (!emailValido(email)) {
            return respuesta("EMAIL_INVALIDO");
        }

        UsuarioOB usuarioOB = usuario(contexto, numeroDocumento);
        if (!empty(usuarioOB) && usuarioOB.apellido.equals(apellido) && usuarioOB.nombre.equals(nombre)) {
            return respuesta("USUARIO_EXISTENTE");
        }

        ServicioInvitacionOB servicioInvitacionOB = new ServicioInvitacionOB(contexto);
        servicioInvitacionOB.enviarInvitacion(contexto, numeroDocumento, nombre, apellido, email);

        return respuesta();
    }

    public static Object validarSuscripcionOperadorAdmin(ContextoOB contexto) {
        Long numeroDocumento = contexto.parametros.longer("numeroDocumento");
        //String token = contexto.parametros.string("token");

        ServicioInvitacionAdministradorOB servicioInvitacionesAdmin = new ServicioInvitacionAdministradorOB(contexto);
        List<InvitacionAdministradorOB> listInvitacionAdmin = servicioInvitacionesAdmin.findByDNIEstado(contexto, numeroDocumento).tryGet();

        if (listInvitacionAdmin != null && listInvitacionAdmin.size() == 0) {
            return respuesta("INVITACION_ADM_INVALIDA");
        }

        InvitacionAdministradorOB invitacionPrincipal = listInvitacionAdmin.get(0);

        UsuarioOB usuarioOB = usuario(contexto, numeroDocumento);
        if (!empty(usuarioOB)) {
            return respuesta("USUARIO_EXISTENTE");
        }
//
//		if (invitacion.intentos > 3) {
//			invitacion.estado = EnumEstadoInvitacionOB.BLOQUEADA;
//			servicioInvitaciones.update(invitacion).tryGet();
//			return respuesta("DEMASIADOS_INTENTOS");
//		}

        if (!invitacionPrincipal.usu_nro_documento.equals(numeroDocumento) || !invitacionPrincipal.estado.equals(EnumEstadoInvitacionOB.ENVIADA)) {
//			invitacionAdmin.intentos++;	
            servicioInvitacionesAdmin.update(invitacionPrincipal).tryGet();
            return respuesta("DNI_INVALIDO");
        }

        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, invitacionPrincipal.empresa, usuarioOB, 2);
        if (!empty(empresaUsuario)) {
            return respuesta("OPERADOR_EXISTENTE_ROL2");
        }

        ServicioInvitacionAdministradorOB servicioInvitacionAdministradorOB = new ServicioInvitacionAdministradorOB(contexto);
        Objeto datosPersonales = servicioInvitacionAdministradorOB.obtenerNombre(contexto, numeroDocumento);
        if (empty(datosPersonales)) {
            return respuesta("ERROR_API_PERSONAS");
        }

        LogOB.evento(contexto, "VALIDAR_DNI_OPERADOR", new Objeto().set("dni", numeroDocumento).set("cuit", invitacionPrincipal.empresa.cuit));

        Objeto datos = new Objeto();
        datos.set("nombre", datosPersonales.get("nombre"));
        datos.set("apellido", datosPersonales.get("apellido"));

        return respuesta("datos", datos);

    }

    public static Object validarDniOperador(ContextoOB contexto) {
        Long numeroDocumento = contexto.parametros.longer("numeroDocumento");
        String token = contexto.parametros.string("token");

        UsuarioOB usuarioOB = usuario(contexto, numeroDocumento);
        if (!empty(usuarioOB) && !usuarioOB.apellido.equals(usuarioOB.apellido) && !usuarioOB.nombre.equals(usuarioOB.nombre)) {
            return respuesta("USUARIO_EXISTENTE");
        }

        ServicioInvitacionOB servicioInvitaciones = new ServicioInvitacionOB(contexto);
        InvitacionOB invitacion = servicioInvitaciones.findByToken(contexto, token).tryGet();
        if (empty(invitacion)) {
            return respuesta("TOKEN_INVALIDO");
        }

        if (invitacion.intentos > 3) {
            invitacion.estado = EnumEstadoInvitacionOB.BLOQUEADA;
            servicioInvitaciones.update(invitacion).tryGet();
            return respuesta("DEMASIADOS_INTENTOS");
        }

        if (!invitacion.numeroDocumento.equals(numeroDocumento)) {
            invitacion.intentos++;
            servicioInvitaciones.update(invitacion).tryGet();
            return respuesta("DNI_INVALIDO");
        }

        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, invitacion.empresa, usuarioOB, 2);
        if (!empty(empresaUsuario) && !usuarioOB.apellido.equals(usuarioOB.apellido) && !usuarioOB.nombre.equals(usuarioOB.nombre)) {
            return respuesta("OPERADOR_EXISTENTE");
        }

        LogOB.evento(contexto, "VALIDAR_DNI_OPERADOR", new Objeto().set("dni", numeroDocumento).set("cuit", invitacion.empresa.cuit));

        Objeto datos = new Objeto();
        datos.set("nombre", invitacion.nombre);
        datos.set("apellido", invitacion.apellido);

        return respuesta("datos", datos);
    }

    public static Object validarSuscripcionOperadorAdministrador(ContextoOB contexto) {
        Long numeroDocumento = contexto.parametros.longer("numeroDocumento");

        ServicioInvitacionAdministradorOB servicioInvitacionesAdmin = new ServicioInvitacionAdministradorOB(contexto);
        List<InvitacionAdministradorOB> listInvitacionAdmin = servicioInvitacionesAdmin.findByDNIEstado(contexto, numeroDocumento).tryGet();
        if (listInvitacionAdmin != null && listInvitacionAdmin.size() == 0) {
            return respuesta("INVITACION_ADM_INVALIDA");
        }
        
        InvitacionAdministradorOB invitacionPrincipal = listInvitacionAdmin.get(0); 
        if (listInvitacionAdmin.size() > 1 ) {
        	
        	listInvitacionAdmin.remove(0); //saco Invitacion principal
        	for(InvitacionAdministradorOB invitacion : listInvitacionAdmin ) {
        		//actualizo el resto de invitaciones como vinculaciones pendientes
        		invitacion.estado = EnumEstadoInvitacionOB.VINCULACION_PEND;
        		servicioInvitacionesAdmin.update(invitacion);
        	}
        }
       
        if (!invitacionPrincipal.usu_nro_documento.equals(numeroDocumento) || (!invitacionPrincipal.estado.equals(EnumEstadoInvitacionOB.ENVIADA) && !invitacionPrincipal.estado.equals(EnumEstadoInvitacionOB.REENVIADA) && !invitacionPrincipal.estado.equals(EnumEstadoInvitacionOB.TRANSMIT))) {
            return respuesta("DNI_INVALIDO");
        }

        ServicioInvitacionOB servicioInvitacionOB = new ServicioInvitacionOB(contexto);
        Objeto datosPersonales = servicioInvitacionOB.obtenerNombreMultipleCuils(contexto, numeroDocumento);
        if (empty(datosPersonales)) {
            return respuesta("ERROR_API_PERSONAS");
        }

        LogOB.evento(contexto, "VALIDAR_DNI_OPERADOR", new Objeto().set("dni", numeroDocumento).set("cuit", invitacionPrincipal.empresa.cuit));

        Objeto datos = new Objeto();
        datos.set("nombre", datosPersonales.get("nombre"));
        datos.set("apellido", datosPersonales.get("apellido"));
        datos.set("email", invitacionPrincipal.usu_correo);
        datos.set("movil", invitacionPrincipal.usu_telefono_movil);
        datos.set("estado", invitacionPrincipal.estado.toString());

        return respuesta("datos", datos);
    }

    public static Object obtenerSuscripcionOperadorAdministrador(ContextoOB contexto) {
        Long cuil = contexto.parametros.longer("cuil");

        ServicioInvitacionAdministradorOB servicioInvitacionesAdmin = new ServicioInvitacionAdministradorOB(contexto);
        List<InvitacionAdministradorOB> listInvitacionAdmin = servicioInvitacionesAdmin.findByCuilEstado(contexto, cuil).tryGet();
        if (listInvitacionAdmin != null && listInvitacionAdmin.size() == 0) {
            return respuesta("NO_EXISTE_INVITACION");
        }

        InvitacionAdministradorOB invitacionPrincipal = listInvitacionAdmin.get(0);

        LogOB.evento(contexto, "OBTENER_SUSCRIPCION", new Objeto().set("cuil", cuil).set("cuit", invitacionPrincipal.empresa.cuit));

        Objeto datos = new Objeto();
        datos.set("apellido", invitacionPrincipal.usu_apellido);
        datos.set("nombre", invitacionPrincipal.usu_nombre);
        datos.set("idCobis", invitacionPrincipal.usu_idCobis);
        datos.set("nroDocumento", invitacionPrincipal.usu_nro_documento);
        datos.set("email", invitacionPrincipal.usu_correo);
        datos.set("movil", invitacionPrincipal.usu_telefono_movil);

        return respuesta("datos", datos);
    }

    public static Object cancelarInvitacion(ContextoOB contexto) {
        Long numeroDocumento = contexto.parametros.longer("numeroDocumento");

        SesionOB sesion = contexto.sesion();
        if (!sesion.esOperadorInicial()) {
            return respuesta("OPERACION_INVALIDA");
        }

        ServicioInvitacionOB servicioInvitacionOB = new ServicioInvitacionOB(contexto);
        InvitacionOB invitacion = servicioInvitacionOB.cancelarInvitacion(contexto, numeroDocumento).tryGet();
        if (empty(invitacion)) {
            return respuesta("DNI_INVALIDO");
        }

        LogOB.evento(contexto, "CANCELAR_INVITACION", new Objeto().set("dni", numeroDocumento));

        return respuesta();
    }

    public static Object editarInvitacion(ContextoOB contexto) {
        Long cuil = contexto.parametros.longer("cuil");
        String numeroDocumento = contexto.parametros.string("numeroDocumento");
        String apellido = contexto.parametros.string("apellido");
        String nombre = contexto.parametros.string("nombre");
        String email = contexto.parametros.string("email");
        String telefonoMovil = contexto.parametros.string("telefonoMovil");
        String usuidCobis = contexto.parametros.string("usuidCobis");
        Long cuilNuevo = contexto.parametros.longer("cuilNuevo", null);

        ServicioInvitacionAdministradorOB servicioInvitacionesAdmin = new ServicioInvitacionAdministradorOB(contexto);
        List<InvitacionAdministradorOB> listInvitacionAdmin = servicioInvitacionesAdmin.findByCuilEstado(contexto, cuil).tryGet();
        if (listInvitacionAdmin != null && listInvitacionAdmin.size() == 0) {
            return respuesta("NO_EXISTE_INVITACION");
        }

        if (empty(nombre)) {
            return respuesta("NOMBRE_INVALIDO");
        }
        if (empty(apellido)) {
            return respuesta("APELLIDO_INVALIDO");
        }

        if (empty(numeroDocumento) || !numeroDocumento.trim().matches("\\d+")) {
            return respuesta("NUMERO_DOCUMENTO_INVALIDO");
        }
        Long numeroDocumentoLong = Long.parseLong(numeroDocumento.trim());

        if (empty(email)) {
            return respuesta("EMAIL_INVALIDO");
        }
        if (!emailValido(email)) {
            return respuesta("EMAIL_INVALIDO");
        }
        if (empty(telefonoMovil)) {
            return respuesta("TELEFONO_MOVIL_INVALIDO");
        }
        
        Objeto resultado = ValidarCorreoYCelular.validarCorreoYCelular(contexto, email, telefonoMovil);
        if (!"0".equals(resultado.get("estado"))) {
            return resultado;
        }

        InvitacionAdministradorOB invitacionPrincipal = listInvitacionAdmin.get(0);

        servicioInvitacionesAdmin.editarSuscripcion(contexto, invitacionPrincipal, apellido, nombre, numeroDocumentoLong, email, telefonoMovil, usuidCobis, cuilNuevo);

        return respuesta("0");
    }

    public static Object invitaciones(ContextoOB contexto) {

        SesionOB sesion = contexto.sesion();

        if (!sesion.esOperadorInicial()) {
            return respuesta("OPERACION_INVALIDA");
        }

        ServicioInvitacionOB servicioInvitaciones = new ServicioInvitacionOB(contexto);
        List<InvitacionOB> invitaciones = servicioInvitaciones.find(contexto, sesion.empresaOB).get();

        Objeto lstInvitaciones = new Objeto();
        invitaciones.forEach(i -> {
            Objeto inv = new Objeto();
            inv.set("nombre", i.nombre);
            inv.set("apellido", i.apellido);
            inv.set("fechaCreacion", i.fechaCreacion.toString());
            inv.set("estado", i.estado.name());
            inv.set("numeroDocumento", i.numeroDocumento);
            lstInvitaciones.add(inv);
        });

        Objeto datos = new Objeto();
        datos.add("invitaciones", lstInvitaciones);

        return respuesta("datos", datos);
    }

    public static Object invitacion(ContextoOB contexto) {
        String token = contexto.parametros.string("token");

        ServicioInvitacionOB servicioInvitaciones = new ServicioInvitacionOB(contexto);
        InvitacionOB invitacion = servicioInvitaciones.findByToken(contexto, token).tryGet();

        if (empty(invitacion)) {
            return respuesta("TOKEN_INVALIDO");
        }

        if (!invitacion.enviada()) {
            return respuesta("INVITACION_INVALIDA");
        }

        long horas = ChronoUnit.HOURS.between(invitacion.fechaCreacion, LocalDateTime.now());
        boolean expira = horas > 48 ? true : false;

        if (expira) {
            invitacion.estado = EnumEstadoInvitacionOB.EXPIRADA;
            servicioInvitaciones.update(invitacion).tryGet();
            return respuesta("INVITACION_EXPIRADA");
        }

        Objeto datos = new Objeto();
        datos.set("nombre", invitacion.nombre);
        datos.set("apellido", invitacion.apellido);
        datos.set("documento", invitacion.numeroDocumento);
        datos.set("cuit", invitacion.empresa.cuit);
        datos.set("idCobis", invitacion.empresa.idCobis);
        datos.set("razonSocial", invitacion.empresa.razonSocial);
        datos.set("fechaCreacion", invitacion.fechaCreacion.toString());
        datos.set("estado", invitacion.estado.name());

        return respuesta("datos", datos);
    }

    public static Object validarAltaOperador(ContextoOB contexto) {
        Long numeroDocumento = contexto.parametros.longer("numeroDocumento");

        Objeto datos = new Objeto();

        SesionOB sesion = contexto.sesion();
        if (!sesion.esOperadorInicial()) {
            return respuesta("OPERACION_INVALIDA");
        }

        UsuarioOB usuarioOB = usuario(contexto, numeroDocumento);
        boolean existeUsuario = empty(usuarioOB) ? false : true;
        datos.set("usuarioNuevo", !existeUsuario);

        if (existeUsuario) {
            UsuariosEmpresasActivoOB empresaUsuarioActivo = empresasUsuarioActivo(contexto, usuarioOB, sesion.empresaOB);
            if (!empty(empresaUsuarioActivo) && empresaUsuarioActivo.activo == true) {
                return respuesta("OPERADOR_EXISTENTE");
            }
            if (!empty(empresaUsuarioActivo)) {
                datos.set("activarUsuario", true);
            }
        }

        ServicioInvitacionOB servicioInvitacionOB = new ServicioInvitacionOB(contexto);
        Objeto datosPersonales = servicioInvitacionOB.obtenerNombrePorDocumentoyCuil(contexto, numeroDocumento);
        if (empty(datosPersonales)) {
            return respuesta("ERROR_API_PERSONAS");
        }

        LogOB.evento(contexto, "VALIDAR_DNI_OPERADOR", new Objeto().set("dni", numeroDocumento).set("cuit", sesion.empresaOB.cuit));

        datos.set("nombre", datosPersonales.get("nombre"));
        datos.set("apellido", datosPersonales.get("apellido"));

        return respuesta("datos", datos);

    }

    public static Object altaOperador(ContextoOB contexto) {
        Long numeroDocumento = contexto.parametros.longer("numeroDocumento");
        String cuentas = contexto.parametros.string("cuentas", null);
        String permisos = contexto.parametros.string("permisos");
        SesionOB sesion = contexto.sesion();

        if (!sesion.esOperadorInicial()) {
            return respuesta("OPERACION_INVALIDA");
        }

        UsuarioOB operador = usuario(contexto, numeroDocumento);
        if (empty(operador)) {
            return respuesta("DNI_INVALIDO");
        }

        EmpresaUsuarioOB empresaUsuarioExistente = empresasUsuario(contexto, sesion.empresaOB, operador);
        UsuariosEmpresasActivoOB empresaUsuarioActivo = empresasUsuarioActivo(contexto, operador, sesion.empresaOB);

        if ((!empty(empresaUsuarioExistente) && empty(empresaUsuarioActivo)) || (!empty(empresaUsuarioActivo) && empresaUsuarioActivo.activo == true)) {
            return respuesta("OPERADOR_EXISTENTE");
        }

        if (!empty(cuentas)) {
            for (String cuenta : cuentas.split(",")) {
                if (!OBCuentas.habilitada(contexto, cuenta)) {
                    return respuesta("CUENTA_INVALIDA");
                }
            }
        }

        ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
        EmpresaUsuarioOB empresaUsuario;
        if (empty(empresaUsuarioExistente)) {
            empresaUsuario = servicioEmpresaUsuario.altaOperador(contexto, operador, sesion.empresaOB, cuentas, permisos).tryGet();
        } else {
            empresaUsuario = servicioEmpresaUsuario.altaOperadorExistente(contexto, operador, sesion.empresaOB, empresaUsuarioExistente, cuentas, permisos).tryGet();
        }

        activarUsuarioEmpresa(contexto, empresaUsuario.usuario, empresaUsuario.empresa, empresaUsuario);

        return respuesta("0");
    }

    public static Object editarPermisosOperador(ContextoOB contexto) {
        String permisos = contexto.parametros.string("permisos");
        Long numeroDocumento = contexto.parametros.longer("numeroDocumento");
        SesionOB sesion = contexto.sesion();

        permisos = (permisos.endsWith(",") || permisos.isEmpty()) ?
                permisos.concat("1")
                : permisos.concat(",1");

        if (!sesion.esOperadorInicial()) {
            return respuesta("OPERACION_INVALIDA");
        }

        UsuarioOB usuarioOB = usuario(contexto, numeroDocumento);
        if (empty(usuarioOB)) {
            return respuesta("DNI_INVALIDO");
        }

        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, usuarioOB, 2);
        if (empty(empresaUsuario)) {
            return respuesta("OPERADOR_INVALIDO");
        }

        ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
        servicioEmpresaUsuario.editarPermisosOperador(contexto, empresaUsuario, permisos);

        return respuesta("0");
    }

    public static Object editarCuentasOperador(ContextoOB contexto) {
        String cuentas = contexto.parametros.string("cuentas");
        Long numeroDocumento = contexto.parametros.longer("numeroDocumento");
        SesionOB sesion = contexto.sesion();

        if (!sesion.esOperadorInicial()) {
            return respuesta("OPERACION_INVALIDA");
        }

        UsuarioOB usuarioOB = usuario(contexto, numeroDocumento);
        if (empty(usuarioOB)) {
            return respuesta("DNI_INVALIDO");
        }

        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, usuarioOB, 2);
        if (empty(empresaUsuario)) {
            return respuesta("OPERADOR_INVALIDO");
        }

        ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
        servicioEmpresaUsuario.editarCuentasOperador(contexto, empresaUsuario, cuentas);

        return respuesta("0");
    }

    public static Object bajaOperador(ContextoOB contexto) {
        Long numeroDocumento = contexto.parametros.longer("numeroDocumento");
        SesionOB sesion = contexto.sesion();
        ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
        ServicioUsuariosEmpresasActivoOB servicio = new ServicioUsuariosEmpresasActivoOB(contexto);

        if (!sesion.esOperadorInicial()) {
            return respuesta("OPERACION_INVALIDA");
        }

        UsuarioOB usuarioOB = usuario(contexto, numeroDocumento);
        if (empty(usuarioOB)) {
            return respuesta("DNI_INVALIDO");
        }

        UsuariosEmpresasActivoOB empresaUsuarioActivo = empresasUsuarioActivo(contexto, usuarioOB, sesion.empresaOB);
        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, usuarioOB, 2);

        if (empty(empresaUsuarioActivo)) {
            UsuariosEmpresasActivoOB ue = new UsuariosEmpresasActivoOB();
            ue.empresaUsuario = empresaUsuario;
            ue.activo = false;
            ue.fechaCreacion = LocalDateTime.now();
            servicio.create(ue);
        } else {
            empresaUsuarioActivo.activo = false;
            empresaUsuarioActivo.fechaCreacion = LocalDateTime.now();
            servicio.update(empresaUsuarioActivo);
        }

        if (!empresaUsuario.permisos.isEmpty()) {
            servicioEmpresaUsuario.eliminarPermisosOperador(contexto, empresaUsuario);
        }
        if (!empresaUsuario.cuentas.isEmpty()) {
            servicioEmpresaUsuario.eliminarCuentasOperador(contexto, empresaUsuario);
        }

        return respuesta("0");
    }

    public static Object operadores(ContextoOB contexto) {
        SesionOB sesion = contexto.sesion();

        if (!sesion.esOperadorInicial()) {
            return respuesta("OPERACION_INVALIDA");
        }

        ServicioEmpresaUsuarioOB servicio = new ServicioEmpresaUsuarioOB(contexto);
        List<EmpresaUsuarioOB> empresaUsuarioOB = servicio.findByEmpresa(sesion.empresaOB).tryGet();

        Objeto datos = new Objeto();
        Objeto listaOperadores = new Objeto();
        for (EmpresaUsuarioOB empresaUsuario : empresaUsuarioOB) {

            UsuariosEmpresasActivoOB empresaUsuarioActivo = empresasUsuarioActivo(contexto, empresaUsuario.usuario, sesion.empresaOB);

            if (empty(empresaUsuarioActivo) || empresaUsuarioActivo.activo == true) {
                Objeto op = new Objeto();
                op.set("nombre", empresaUsuario.usuario.nombre);
                op.set("apellido", empresaUsuario.usuario.apellido);
                op.set("numeroDocumento", empresaUsuario.usuario.numeroDocumento);
                op.set("rol", empresaUsuario.rol.nombre);
                op.set("rolId", empresaUsuario.rol.rol_codigo);

                if (empresaUsuario.rol.rol_codigo == 2) {
                    List<PermisoOperadorOB> lstPermisos = empresaUsuario.permisos.stream().filter(p -> p.permiso.padre == null).collect(Collectors.toList());
                    op.set("tienePermisos", lstPermisos.size() > 0 ? true : false);
                    op.set("permisos", lstPermisos.stream().map(p -> p.permiso.objeto(true)).collect(Collectors.toList()));

                    List<CuentaOperadorOB> lstCuentas = empresaUsuario.cuentas;
                    op.set("tieneCuentas", lstCuentas.size() > 0 ? true : false);
                    op.set("cuentas", lstCuentas.stream().map(c -> c.numeroCuenta).collect(Collectors.toList()));
                }

                listaOperadores.add(op);
            }
        }

        datos.add("operadores", listaOperadores);

        return respuesta("datos", datos);
    }

    public static Object permisosOperador(ContextoOB contexto) {
        Long numeroDocumento = contexto.parametros.longer("numeroDocumento", null);
        SesionOB sesion = contexto.sesion();

        if (!sesion.esOperadorInicial()) {
            return respuesta("OPERACION_INVALIDA");
        }

        List<PermisoOperadorOB> permisosOperador = new ArrayList<PermisoOperadorOB>();
        if (!empty(numeroDocumento)) {
            UsuarioOB operador = usuario(contexto, numeroDocumento);
            if (empty(operador)) {
                return respuesta("DNI_INVALIDO");
            }
            EmpresaUsuarioOB empresaOperador = empresasUsuario(contexto, sesion.empresaOB, operador, 2);
            if (empty(empresaOperador)) {
                return respuesta("OPERADOR_INVALIDO");
            }

            permisosOperador = empresaOperador.permisos;
        }

        ServicioPermisosOB servicioPermisos = new ServicioPermisosOB(contexto);
        List<Objeto> listaPermisos = servicioPermisos.seleccionarPermisos(permisosOperador);

        Objeto datos = new Objeto();
        datos.set("permisos", listaPermisos);

        return respuesta("datos", datos);
    }

    public static Object cuentasOperador(ContextoOB contexto) {
        Long numeroDocumento = contexto.parametros.longer("numeroDocumento", null);
        SesionOB sesion = contexto.sesion();

        if (!sesion.esOperadorInicial()) {
            return respuesta("OPERACION_INVALIDA");
        }

        List<CuentaOperadorOB> lstCuentasOperador = new ArrayList<CuentaOperadorOB>();
        if (!empty(numeroDocumento)) {
            UsuarioOB operador = usuario(contexto, numeroDocumento);
            if (empty(operador)) {
                return respuesta("DNI_INVALIDO");
            }

            EmpresaUsuarioOB empresaOperador = empresasUsuario(contexto, sesion.empresaOB, operador, 2);
            if (empty(empresaOperador)) {
                return respuesta("OPERADOR_INVALIDO");
            }
            lstCuentasOperador = empresaOperador.cuentas;
        }

        Objeto filtroCuentas = (Objeto) OBCuentas.cuentas(contexto);
        Objeto listaCuentas = (Objeto) filtroCuentas.get("datos.cuentas");
        List<String> lstCuentas = listaCuentas.objetos().stream().map(c -> c.get("numeroProducto").toString()).collect(Collectors.toList());

        Objeto listaCuentasOP = new Objeto();
        for (String cuenta : lstCuentas) {
            Objeto op = new Objeto();
            Optional<CuentaOperadorOB> existe = lstCuentasOperador.stream().filter(c -> c.numeroCuenta.toString().equals(cuenta)).findFirst();
            op.set("cuenta", cuenta);
            op.set("habilitado", existe.isPresent() ? true : false);

            listaCuentasOP.add(op);
        }

        Objeto datos = new Objeto();
        datos.add("cuentas", listaCuentasOP);

        return respuesta("datos", datos);
    }

    public static void activarUsuarioEmpresa(ContextoOB contexto, UsuarioOB usuario, EmpresaOB empresa, EmpresaUsuarioOB empresaUsuario) {
        ServicioUsuariosEmpresasActivoOB servicioActivo = new ServicioUsuariosEmpresasActivoOB(contexto);
        UsuariosEmpresasActivoOB empresaUsuarioActivo = empresasUsuarioActivo(contexto, usuario, empresa);
        if (empty(empresaUsuarioActivo)) {
            UsuariosEmpresasActivoOB ue = new UsuariosEmpresasActivoOB();
            ue.empresaUsuario = empresaUsuario;
            ue.activo = true;
            ue.fechaCreacion = LocalDateTime.now();
            servicioActivo.create(ue);
        } else {
            empresaUsuarioActivo.activo = true;
            empresaUsuarioActivo.fechaCreacion = LocalDateTime.now();
            servicioActivo.update(empresaUsuarioActivo);
        }
    }

    public static Object nuevoOperador(ContextoOB contexto) {
        Long numeroDocumento = contexto.parametros.longer("numeroDocumento");
        String token = contexto.parametros.string("token");
        String usuario = contexto.parametros.string("usuario");
        String clave = contexto.parametros.string("clave");
        String nombre = contexto.parametros.string("nombre");
        String apellido = contexto.parametros.string("apellido");
        String email = contexto.parametros.string("email");
        String telefono = contexto.parametros.string("telefono");
        SesionOB sesion = contexto.sesion();
        ServicioInvitacionOB servicioInvitacion = new ServicioInvitacionOB(contexto);

        InvitacionOB invitacion = servicioInvitacion.findByToken(contexto, numeroDocumento, token).tryGet();
        /*if (empty(invitacion)) {
            return respuesta("TOKEN_INVALIDO");
        }*/
        if (!invitacion.enviada()) {
            return respuesta("INVITACION_INVALIDA");
        }
        Documentos nroDoc = ApiPersonas.buscarPorDocumento(contexto, String.valueOf(numeroDocumento)).tryGet();
        if (!empty(nroDoc) && nroDoc.size() == 1) {
            Long cuil = Long.valueOf(nroDoc.get(0).numeroIdentificacionTributaria);
            ServicioUsuarioOB servicioUsuarioOB = new ServicioUsuarioOB(contexto);
            EmpresaUsuarioOB empresaUsuario = servicioUsuarioOB.nuevoOperador(contexto, usuario, clave, nombre, apellido, cuil, invitacion, nroDoc.get(0).idCliente,email,telefono).tryGet();
            if (empty(empresaUsuario)) {
                return respuesta("ERROR_CREAR_OPERADOR");
            }

            activarUsuarioEmpresa(contexto, empresaUsuario.usuario, empresaUsuario.empresa, empresaUsuario);
        } else {
            Cuils datosPersona = ApiPersonas.cuils(contexto, String.valueOf(numeroDocumento)).tryGet();

            if (!empty(datosPersona)) {
                Long cuil = Long.valueOf(datosPersona.stream().filter(m -> m.apellidoYNombre.equals(apellido + " " + nombre)).findFirst().get().cuil);
                ServicioUsuarioOB servicioUsuarioOB = new ServicioUsuarioOB(contexto);
                EmpresaUsuarioOB empresaUsuario = servicioUsuarioOB.nuevoOperador(contexto, usuario, clave, nombre, apellido, cuil, invitacion, null,email,telefono).tryGet();
                if (empty(empresaUsuario)) {
                    return respuesta("ERROR_CREAR_OPERADOR");
                }

                activarUsuarioEmpresa(contexto, empresaUsuario.usuario, empresaUsuario.empresa, empresaUsuario);
            } else {
                return respuesta("ERROR_API_PERSONAS");
            }
        }

        return respuesta("0");
    }

    public static Object nuevoOperadorAdministrador(ContextoOB contexto) {
        Long numeroDocumento = contexto.parametros.longer("numeroDocumento");
        String usuario =  contexto.parametros.string("usuario");
        String clave = contexto.parametros.string("clave");

        ServicioInvitacionAdministradorOB servicioInvitacionAdmin = new ServicioInvitacionAdministradorOB(contexto);
        List<InvitacionAdministradorOB> listaInvitacionAdmin = servicioInvitacionAdmin.findInvitacionDNIPendiente(contexto, numeroDocumento).tryGet();

        InvitacionAdministradorOB invitacionAdmin = listaInvitacionAdmin.get(0);

        if (!invitacionAdmin.enviada()) {
            return respuesta("INVITACION_INVALIDA");
        }

        Cuils datosPersona = ApiPersonas.cuils(contexto, String.valueOf(numeroDocumento)).tryGet();
        if (!empty(datosPersona)) {
            Long cuil = Long.valueOf(datosPersona.get(0).cuil);
            ServicioUsuarioOB servicioUsuarioOB = new ServicioUsuarioOB(contexto);
            EmpresaUsuarioOB empresaUsuario = servicioUsuarioOB.nuevoOperadorAdministrador(contexto, usuario, clave, cuil, invitacionAdmin).tryGet();
            if (empty(empresaUsuario)) {
                return respuesta("ERROR_CREAR_OPERADOR");
            }
        } else {
            return respuesta("ERROR_API_PERSONAS");
        }

        UsuarioOB usuarioOB = usuario(contexto, numeroDocumento);
        try {

            if (!empty(usuarioOB)) {

                ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
                List<InvitacionAdministradorOB> listaInvitacionAdminVincular = servicioInvitacionAdmin.findInvitacionesPorVincularDNI(contexto, numeroDocumento).tryGet();

                EmpresaOB empresaOB = null;
                ServicioInvitacionAdministradorOB servicioInvitacionAdministradorOB = new ServicioInvitacionAdministradorOB(contexto);
                if (listaInvitacionAdminVincular != null && listaInvitacionAdminVincular.size() > 0) {
                    for (InvitacionAdministradorOB invitacionAdminVincular : listaInvitacionAdminVincular) {
                        empresaOB = invitacionAdminVincular.empresa;
                        servicioEmpresaUsuario.vinculacionOperadorAdministradorEmpresa(contexto, usuarioOB, empresaOB, empresaOB.idCobis, empresaOB.razonSocial);
                        servicioInvitacionAdministradorOB.enviarBienvenidaVinculacionEmpresaAdministrador(contexto, usuarioOB.cuil, usuarioOB.nombre, usuarioOB.apellido, usuarioOB.email, empresaOB);
                        invitacionAdminVincular.estado = EnumEstadoInvitacionOB.VINCULACION_ACEPTADA;

                        servicioInvitacionAdministradorOB.update(invitacionAdminVincular);
                    }
                }
            }
            return respuesta("0");
        } catch (Exception e) {
            return respuesta("ERROR_VINCULANDOEMPRESA");
        }
    }

    public static Object cambioAdministrador(ContextoOB contexto) {
    	String usu_nombre = contexto.parametros.string("nombre",null);
        String usu_apellido = contexto.parametros.string("apellido",null);
        Long usu_cuil = contexto.parametros.longer("cuil",null);
        Long numeroDocumento = contexto.parametros.longer("numeroDocumento",null);
        String usu_email = contexto.parametros.string("email",null);
        String usu_telefonoMovil = contexto.parametros.string("telefonoMovil",null);
        String usu_idCobis = contexto.parametros.string("usuidCobis",null);
        Long cuitEmpresa = contexto.parametros.longer("cuitEmpresa",null);
        String usuario = contexto.parametros.string("usuarioConsulta",null);
        
        if (empty(usuario)) {
            return respuesta("DATO_USUARIO_INVALIDO");
        }

        ServicioEmpresaOB servicioEmpresa = new ServicioEmpresaOB(contexto);
        ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
        UsuarioOB usuarioOB = usuario(contexto, numeroDocumento);
        EmpresaOB empresaOB =null;
        
        try {
             empresaOB = servicioEmpresa.findByCuit(cuitEmpresa).get();
        }catch (Exception e){
            return respuesta("ERROR_AL_OBTENER_EMPRESA");
        }


       try {
           if (!empty(usuarioOB) && !empty(empresaOB)) {
              EmpresaUsuarioOBLite empresa_usuario = servicioEmpresaUsuario.findByUsuarioEmpresaLite(usuarioOB, empresaOB).tryGet();
              if(!empty(empresa_usuario)){
                  servicioEmpresaUsuario.updateRol(empresaOB,2).get();
                  empresa_usuario.rol.rol_codigo = 1;
                  servicioEmpresaUsuario.updateLite(empresa_usuario).get();
                  LogCrmOB.evento(contexto,empresaOB.emp_codigo,usuarioOB.codigo,"cambio de rol: Admin",usuario);
              }else{
                  servicioEmpresaUsuario.updateRol(empresaOB,2).get();
                  servicioEmpresaUsuario.vinculacionOperadorAdministradorEmpresa(contexto, usuarioOB, empresaOB, empresaOB.idCobis, empresaOB.razonSocial).get();
                  LogCrmOB.evento(contexto,empresaOB.emp_codigo,usuarioOB.codigo,"cambio de rol: Admin y vinculacion empresa",usuario);
              }
           }else {
               if (empty(empresaOB)) {
                   return respuesta("EMPRESA_INVALIDA");
               }
               if (empty(usuarioOB)) {
                   if (empty(usu_nombre)) {
                       return respuesta("NOMBRE_INVALIDO");
                   }
                   if (empty(usu_apellido)) {
                       return respuesta("APELLIDO_INVALIDO");
                   }
                   if (empty(usu_cuil)) {
                       return respuesta("CUIL_INVALIDO");
                   }
                   if (empty(numeroDocumento)) {
                       return respuesta("NUMERO_DOCUMENTO_INVALIDO");
                   }
                   if (empty(usu_email)) {
                       return respuesta("EMAIL_INVALIDO");
                   }
                   if (!emailValido(usu_email)) {
                       return respuesta("EMAIL_INVALIDO");
                   }
                   if (empty(usu_telefonoMovil)) {
                       return respuesta("TELEFONO_MOVIL_INVALIDO");
                   }
                   Objeto resultado = ValidarCorreoYCelular.validarCorreoYCelular(contexto, usu_email, usu_telefonoMovil);
                   if (!"0".equals(resultado.get("estado"))) {
                       return resultado;
                   }
                   if (empty(usu_idCobis)) {
                       usu_idCobis = null;
                       try {
                           Futuro<Persona> futuroPersona = ApiPersonas.persona(contexto, usu_cuil.toString());
                           Persona persona = futuroPersona.get();

                           usu_idCobis = Integer.parseInt(persona.idCliente) < 0 ? null : persona.idCliente;
                       } catch (Exception e) {
                           LogOB.evento(contexto, "SUSCRIPADM - ApiPersona.idCliente", new Objeto().set("Cuil", usu_cuil).set("error", e.getMessage()));
                       }
                   }
                   ServicioInvitacionAdministradorOB servicioInvitacionAdministradorOB = new ServicioInvitacionAdministradorOB(contexto);
                   servicioInvitacionAdministradorOB.enviarInvitacionNuevoAdministrador(contexto, numeroDocumento, usu_cuil, usu_idCobis, usu_nombre, usu_apellido, usu_email, usu_telefonoMovil, empresaOB);
                   servicioEmpresaUsuario.updateRol(empresaOB, 2).get();
                   LogCrmOB.evento(contexto, empresaOB.emp_codigo, 0, "cambio de rol: Admin e invitacion nuevo usuario", usuario);
               }
           }
           return respuesta("0");
       } catch (Exception e) {
           return respuesta("ERROR_CAMBIO_ROL");
       }
    }

    public static Object permisoUsuario(ContextoOB contexto) {
        Long numeroDocumento = contexto.parametros.longer("numeroDocumento", null);
        SesionOB sesion = contexto.sesion();

        List<PermisoOperadorOB> permisosOperador;
        List<Objeto> listaPermisos = new ArrayList<Objeto>();

        if (!empty(numeroDocumento)) {
            UsuarioOB operador = usuario(contexto, numeroDocumento);
            if (empty(operador)) {
                return respuesta("DNI_INVALIDO");
            }
            EmpresaUsuarioOB empresaOperador = empresasUsuario(contexto, sesion.empresaOB, operador);

            permisosOperador = empresaOperador.permisos;

            ServicioPermisosOB servicioPermisos = new ServicioPermisosOB(contexto);
            listaPermisos = servicioPermisos.seleccionarPermisos(permisosOperador, empresaOperador);
        }

        Objeto datos = new Objeto();
        datos.set("permisos", listaPermisos);

        return respuesta("datos", datos);
    }

    public static Object permisosSinAsignar(ContextoOB contexto) {
        Integer idPermiso = contexto.parametros.integer("idPermiso");
        SesionOB sesion = contexto.sesion();

        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);
        if (empty(empresaUsuario) || sesion.esOperadorInicial()) {
            return respuesta("OPERACION_INVALIDA");
        }

        ServicioPermisosOB servicioPermisos = new ServicioPermisosOB(contexto);
        PermisoOB permisoPadre = servicioPermisos.find(idPermiso).tryGet();
        if (empty(permisoPadre)) {
            return respuesta("PERMISO_INVALIDO");
        }

        ServicioPermisosOB servicioPermisoOperador = new ServicioPermisosOB(contexto);
        List<PermisoOB> permisosSinAsignar = servicioPermisoOperador.permisosSinAsignar(permisoPadre, empresaUsuario).get();

        Objeto datos = new Objeto();
        for (PermisoOB per : permisosSinAsignar) {
            Objeto dato = new Objeto();
            dato.set("codigo", per.codigo);
            dato.set("permiso", per.nombre);
            datos.add(dato);
        }

        return respuesta("datos", datos);
    }

    public static Object solicitarNuevoPermiso(ContextoOB contexto) {

        //contexto.parametros.
        Objeto objetoIds = contexto.parametros.objeto("idsPermisos");
        List<Object> idsPermisos = objetoIds.toList();

        SesionOB sesion = contexto.sesion();

        if (!sesion.esOperador()) {
            return respuesta("OPERACION_INVALIDA");
        }

        if (empty(sesion.empresaOB)) {
            return respuesta("EMPRESA_INVALIDA");
        }

        //String permisos = null;
        ArrayList<String> permisos = new ArrayList<String>();

        for (Object id : idsPermisos) {
            ServicioPermisosOB servicioPermisos = new ServicioPermisosOB(contexto);
            PermisoOB permiso = servicioPermisos.find(Integer.parseInt(id.toString())).tryGet();
            if (empty(permiso)) {
                return respuesta("PERMISO_INVALIDO");
            }

            permisos.add(permiso.nombre);
        }

        UsuarioOB operadorInicial = operadorInicial(contexto);
        ApiNotificaciones.envioSolicitudPermisosOB(contexto, operadorInicial.email, operadorInicial.nombreCompleto(), sesion.usuarioOB.nombreCompleto(), permisos, sesion.empresaOB.razonSocial);

        return respuesta();

    }

    public static Object suscripcionAdministrador(ContextoOB contexto) {
        String usu_nombre = contexto.parametros.string("nombre");
        String usu_apellido = contexto.parametros.string("apellido");
        Long usu_cuil = contexto.parametros.longer("cuil");
        Long usu_numeroDocumento = contexto.parametros.longer("numeroDocumento");
        String usu_email = contexto.parametros.string("email");
        String usu_telefonoMovil = contexto.parametros.string("telefonoMovil");
        String usu_idCobis = contexto.parametros.string("usuidCobis");

        Long emp_cuit = contexto.parametros.longer("cuitEmpresa");
        String emp_idCobis = contexto.parametros.string("empidCobis");
        String emp_razonSocial = contexto.parametros.string("empRazonSocial");

        if (empty(usu_nombre)) {
            return respuesta("NOMBRE_INVALIDO");
        }
        if (empty(usu_apellido)) {
            return respuesta("APELLIDO_INVALIDO");
        }
        if (empty(usu_cuil)) {
            return respuesta("CUIL_INVALIDO");
        }
        if (empty(usu_numeroDocumento)) {
            return respuesta("NUMERO_DOCUMENTO_INVALIDO");
        }
        if (empty(usu_email)) {
            return respuesta("EMAIL_INVALIDO");
        }
        if (!emailValido(usu_email)) {
            return respuesta("EMAIL_INVALIDO");
        }
        if (empty(usu_telefonoMovil)) {
            return respuesta("TELEFONO_MOVIL_INVALIDO");
        }
        
        Objeto resultado = ValidarCorreoYCelular.validarCorreoYCelular(contexto, usu_email, usu_telefonoMovil);
        if (!"0".equals(resultado.get("estado"))) {
            return resultado;
        }
        
        if (empty(usu_idCobis)) {
            usu_idCobis = null;
            try {
                Futuro<Persona> futuroPersona = ApiPersonas.persona(contexto, usu_cuil.toString());
                Persona persona = futuroPersona.get();

                usu_idCobis = Integer.parseInt(persona.idCliente) < 0 ? null : persona.idCliente;
            } catch (Exception e) {
                LogOB.evento(contexto, "SUSCRIPADM - ApiPersona.idCliente", new Objeto().set("Cuil", usu_cuil).set("error", e.getMessage()));
            }
        }
        if (empty(emp_cuit)) {
            return respuesta("EMP_CUIT_INVALIDO");
        }
        if (empty(emp_idCobis)) {
            return respuesta("EMP_IDCOBIS_INVALIDO");
        }
        if (empty(emp_razonSocial)) {
            return respuesta("EMP_RAZONSOCIAL_INVALIDO");
        }

        //TODO: DAR DE ALTA EMPRESA
        EmpresaOB empresa = new EmpresaOB();
        empresa.cuit = emp_cuit;
        empresa.idCobis = emp_idCobis;
        empresa.razonSocial = emp_razonSocial;

        ServicioEmpresaOB servicioEmpresa = new ServicioEmpresaOB(contexto);
        EmpresaOB empresaOB = servicioEmpresa.findByCuit(empresa.cuit).tryGet();
        if (empty(empresaOB)) {
            empresaOB = servicioEmpresa.create(empresa).get();
        } else {
            //desvincular usuario administrador y vincular nuevo
            return respuesta("YA_EXISTE_LA_EMPRESA");
        }

        //TODO: GENERAR NUEVA SOLICITUD ADMIN EN OB+ ENVIO MAIL A ADMINISTRADOR
        ServicioInvitacionAdministradorOB servicioInvitacionAdministradorOB = new ServicioInvitacionAdministradorOB(contexto);

        UsuarioOB usuarioOB = usuario(contexto, usu_numeroDocumento);
        try {
            if (!empty(usuarioOB)) {
                ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
                servicioEmpresaUsuario.vinculacionOperadorAdministradorEmpresa(contexto, usuarioOB, empresaOB, emp_idCobis, emp_razonSocial);
                servicioInvitacionAdministradorOB.enviarBienvenidaVinculacionEmpresaAdministrador(contexto, usuarioOB.cuil, usuarioOB.nombre, usuarioOB.apellido, usuarioOB.email, empresaOB);
                LogOB.evento(contexto, "enviarBienvenidaVinculacionEmpresaAdministrador", new Objeto().set("usu_numeroDocumento", usu_numeroDocumento));
                return respuesta();
            } else {
                servicioInvitacionAdministradorOB.enviarInvitacionNuevoAdministrador(contexto, usu_numeroDocumento, usu_cuil, usu_idCobis, usu_nombre, usu_apellido, usu_email, usu_telefonoMovil, empresaOB);
                LogOB.evento(contexto, "enviarInvitacionNuevoAdministrador", new Objeto().set("usu_numeroDocumento", usu_numeroDocumento));
                return respuesta();
            }
        } catch (Exception e) {
            LogOB.evento(contexto, "suscripcionAdministrador", new Objeto().set("usu_numeroDocumento", usu_numeroDocumento).set("error", e.getMessage()));
            return null;
        }
    }
}
