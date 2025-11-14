package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.notificaciones.ApiNotificaciones;
import ar.com.hipotecario.backend.servicio.api.notificaciones.EnvioEmail;
import ar.com.hipotecario.backend.servicio.api.personas.ApiPersonas;
import ar.com.hipotecario.backend.servicio.api.personas.Cuils;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.LogOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoInvitacionOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.InvitacionAdministradorOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.InvitacionAdministradorOBRepositorio;

public class ServicioInvitacionAdministradorOB extends ServicioOB {

    private InvitacionAdministradorOBRepositorio repo;

    public ServicioInvitacionAdministradorOB(ContextoOB contexto) {
        super(contexto);
        repo = new InvitacionAdministradorOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<List<InvitacionAdministradorOB>> find(Contexto contexto, EmpresaOB empresa) {
        return futuro(() -> repo.findByField("emp_codigo", empresa.emp_codigo));
    }

    public Futuro<InvitacionAdministradorOB> update(InvitacionAdministradorOB invitacion) {
        return futuro(() -> repo.update(invitacion));
    }

    public Futuro<InvitacionAdministradorOB> enviarInvitacionNuevoAdministrador(ContextoOB contexto, Long numeroDocumento, Long usuCuil, String usuidCobis, String nombre, String apellido, String correo, String usu_telefonoMovil, EmpresaOB empresaOB) {
        EnvioEmail result = ApiNotificaciones.envioInvitacionNuevoUserAdministradorOB(contexto, correo, nombre, apellido, empresaOB).get();

        if (result == null) {
            LogOB.evento_sinSesion(contexto, "ERROR_ENVIO_MAIL_INVITACION_ADMIN", new Objeto().set("correo", correo).set("empresa", empresaOB.cuit), usuCuil.toString());
        }

        if (result.codigoHttp() == 200) {
            List<InvitacionAdministradorOB> invitacionAdminEnviada = this.findEnviadaUsuarioEmpresa(contexto, numeroDocumento, empresaOB).tryGet();
            InvitacionAdministradorOB nueva = new InvitacionAdministradorOB();
            // 	hay invitacion admin pendiente de uso para el DNI y EMPRESA?
            if (invitacionAdminEnviada != null && invitacionAdminEnviada.size() == 0) {
                // DNI con invitacion adm nuevo usuario SIN USO (E
                List<InvitacionAdministradorOB> invitacionPendienteVincular = this.findInvitacionDNIPendiente(contexto, numeroDocumento).get();
                if (invitacionPendienteVincular != null && invitacionPendienteVincular.size() > 0) {
                    // cargamos la nueva invitacion con el estado = VINCULACION_PEND
                    nueva.estado = EnumEstadoInvitacionOB.VINCULACION_PEND;
                }

                nueva.usu_nombre = nombre;
                nueva.usu_apellido = apellido;
                nueva.usu_correo = correo;
                nueva.usu_nro_documento = numeroDocumento;
                nueva.empresa = empresaOB;
                nueva.usu_cuil = usuCuil;
                nueva.usu_idCobis = usuidCobis;
                nueva.usu_telefono_movil = usu_telefonoMovil;
                return futuro(() -> repo.create(nueva));
            } else {
                invitacionAdminEnviada.get(0).usu_nombre = nombre;
                invitacionAdminEnviada.get(0).usu_apellido = apellido;
                invitacionAdminEnviada.get(0).usu_correo = correo;
                invitacionAdminEnviada.get(0).usu_nro_documento = numeroDocumento;
                invitacionAdminEnviada.get(0).empresa = empresaOB;
                invitacionAdminEnviada.get(0).usu_cuil = usuCuil;
                invitacionAdminEnviada.get(0).usu_idCobis = usuidCobis;
                invitacionAdminEnviada.get(0).usu_telefono_movil = usu_telefonoMovil;
                invitacionAdminEnviada.get(0).estado = EnumEstadoInvitacionOB.REENVIADA;
                return futuro(() -> repo.update(invitacionAdminEnviada.get(0)));
            }
        }
        return null;
    }

    public Futuro<InvitacionAdministradorOB> enviarBienvenidaVinculacionEmpresaAdministrador(ContextoOB contexto, Long cuil, String nombre, String apellido, String correo, EmpresaOB empresaOB) {
        EnvioEmail result = ApiNotificaciones.envioBienvenidaVinculacionEmpresaAdministradorOB(contexto, correo, nombre, apellido, empresaOB).get();
        if (result == null) {
            LogOB.evento_sinSesion(contexto, "ERROR_ENVIO_MAIL_BIENVENIDA_ADMIN", new Objeto().set("correo", correo).set("empresa", empresaOB.cuit), cuil.toString());
        }
        return null;
    }

    public Futuro<InvitacionAdministradorOB> cancelarInvitacion(ContextoOB contexto, Long numeroDocumento) {
        List<InvitacionAdministradorOB> enviada = this.findEnviadaUsuarioEmpresa(contexto, numeroDocumento, contexto.sesion().empresaOB).tryGet();
        if (!(enviada != null && enviada.size() == 0)) {
            enviada.get(0).estado = EnumEstadoInvitacionOB.CANCELADA;
            return futuro(() -> repo.update(enviada.get(0)));
        }
        return futuro(() -> null);
    }

    public Futuro<List<InvitacionAdministradorOB>> findEnviadaUsuarioEmpresa(Contexto contexto, Long numeroDocumento, EmpresaOB empresa) {
        return futuro(() -> repo.enviada(numeroDocumento, empresa));
    }

    public Futuro<InvitacionAdministradorOB> findByDNI(Contexto contexto, Long numeroDocumento) {
        return futuro(() -> repo.findByDNI(numeroDocumento));
    }

    public Futuro<List<InvitacionAdministradorOB>> findByDNIEstado(Contexto contexto, Long numeroDocumento) {
        return futuro(() -> repo.findByDNIEstado(numeroDocumento));
    }

    public Futuro<List<InvitacionAdministradorOB>> findByCuilEstado(Contexto contexto, Long cuil) {
        return futuro(() -> repo.findByCuilEstado(cuil));
    }

    public Futuro<List<InvitacionAdministradorOB>> findInvitacionDNIPendiente(Contexto contexto, Long numeroDocumento) {
        return futuro(() -> repo.findInvitacionDNIPendiente(numeroDocumento));
    }

    public Futuro<List<InvitacionAdministradorOB>> findInvitacionesPorVincularDNI(Contexto contexto, Long numeroDocumento) {
        return futuro(() -> repo.findInvitacionesPorVincularDNI(numeroDocumento));
    }

    public Futuro<InvitacionAdministradorOB> editarSuscripcion(ContextoOB contexto, InvitacionAdministradorOB invitacion, String apellido, String nombre, Long numeroDocumento, String email, String telefonoMovil, String usuidCobis, Long cuilNuevo) {

        invitacion.usu_apellido = apellido;
        invitacion.usu_nombre = nombre;
        invitacion.usu_nro_documento = numeroDocumento;
        invitacion.usu_correo = email;
        invitacion.usu_telefono_movil = telefonoMovil;
        invitacion.usu_idCobis = usuidCobis;

        if (cuilNuevo != null) {
            invitacion.usu_cuil = cuilNuevo;
        }
        return futuro(() -> repo.update(invitacion));
    }

    public Objeto obtenerNombre(ContextoOB contexto, Long numeroDocumento) {
        Cuils datosPersona = ApiPersonas.cuils(contexto, String.valueOf(numeroDocumento)).get();
        if (datosPersona.size() > 0) {
            Objeto datos = new Objeto();
            try {
                String nombreApellido = datosPersona.get(0).apellidoYNombre.trim();
                String[] splitStr = nombreApellido.split("\\s", 2);
                if (splitStr.length > 0) {
                    datos.set("nombre", splitStr[1]);
                    datos.set("apellido", splitStr[0]);
                } else {
                    datos.set("nombre", nombreApellido);
                }
            } catch (Exception e) {
                return null;
            }
            return datos;
        }
        return null;
    }

}