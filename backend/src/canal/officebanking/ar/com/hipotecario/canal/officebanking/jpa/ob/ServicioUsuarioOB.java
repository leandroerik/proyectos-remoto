package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.time.LocalDateTime;
import java.util.List;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Encriptador;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.personas.ApiPersonas;
import ar.com.hipotecario.backend.servicio.api.personas.Persona;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.LogOB;
import ar.com.hipotecario.canal.officebanking.OBPagoHaberes;
import ar.com.hipotecario.canal.officebanking.SesionOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoInvitacionOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.ClaveUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.InvitacionAdministradorOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.InvitacionOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.UsuarioOBRepositorio;

public class ServicioUsuarioOB extends ServicioOB {

    private UsuarioOBRepositorio repo;
    private static final String CLASE = ServicioUsuarioOB.class.getSimpleName().toUpperCase();

    public ServicioUsuarioOB(ContextoOB contexto) {
        super(contexto);
        repo = new UsuarioOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<List<UsuarioOB>> findAll() {
        return futuro(() -> repo.findAll());
    }

    public Futuro<UsuarioOB> find(Integer codigo) {
        return futuro(() -> repo.find(codigo));
    }

    public Futuro<UsuarioOB> findByNumeroDocumento(Long numeroDocumento) {
        return futuro(() -> repo.findByFieldUnique("numeroDocumento", numeroDocumento));
    }

    public Futuro<UsuarioOB> findByCuil(Long cuil) {
        return futuro(() -> repo.findByFieldUnique("cuil", cuil));
    }

    public Futuro<UsuarioOB> findByEmail(String email) {
        return futuro(() -> repo.findByFieldUnique("email", email));
    }
    
    public Futuro<List<UsuarioOB>> findByCelular(String telefonoMovil) {
        return futuro(() -> repo.findByField("telefonoMovil", telefonoMovil));
    }

    public Futuro<UsuarioOB> update(UsuarioOB usuarioOB) {
        return futuro(() -> repo.update(usuarioOB));
    }

    public Futuro<UsuarioOB> create(UsuarioOB usuarioOB) {
        return futuro(() -> repo.create(usuarioOB));
    }

    public Futuro<EmpresaUsuarioOB> nuevoOperador(ContextoOB contexto, String usuario, String clave, String nombre, String apellido, Long cuil, InvitacionOB invitacion, String doc, String email, String telefono) {

        SesionOB sesion = contexto.sesion();

        UsuarioOB usuarioOB = new UsuarioOB();
        usuarioOB.nombre = nombre;
        usuarioOB.apellido = apellido;
        usuarioOB.email = email;
        usuarioOB.emailValidado = true;
        usuarioOB.idCobis = null;
        usuarioOB.login = invitacion.estado.ordinal() == 8 ? "" : Config.encriptarAES(usuario) ;
        usuarioOB.numeroDocumento = invitacion.numeroDocumento;
        usuarioOB.telefonoMovil = telefono; //sesion.tokenInvitacionSMS.telefono;
        usuarioOB.cuil = cuil;
        usuarioOB.idCobis = doc;
        usuarioOB.validoOTP = true;
        usuarioOB.migrado= invitacion.estado.ordinal() == 8 ? (byte) 1 : (byte) 2;

        ServicioEstadoUsuarioOB servicioEstado = new ServicioEstadoUsuarioOB(contexto);
        usuarioOB.estado = servicioEstado.find(1).get();

        sesion.tokenInvitacionCorreo = null;
        sesion.tokenInvitacionSMS = null;
        sesion.save();

        UsuarioOB usuarioCreado = repo.create(usuarioOB);

        ServicioClaveUsuarioOB servicioClaveUsuario = new ServicioClaveUsuarioOB(contexto);
        ClaveUsuarioOB nuevaClave = new ClaveUsuarioOB();
        nuevaClave.clave = invitacion.estado.ordinal() == 8 ? "" : Encriptador.sha512(clave);
        nuevaClave.usuario = usuarioCreado;
        nuevaClave.fechaCreacion = LocalDateTime.now();
        servicioClaveUsuario.create(nuevaClave);
        usuarioOB.estado = servicioEstado.find(1).get();

        ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
        EmpresaUsuarioOB altaOperador = servicioEmpresaUsuario.altaOperador(contexto, usuarioCreado, invitacion.empresa, null, null).get();
        if (empty(altaOperador)) {
            return null;
        }

        invitacion.estado = EnumEstadoInvitacionOB.ACEPTADA;

        ServicioInvitacionOB servicioInvitacion = new ServicioInvitacionOB(contexto);
        servicioInvitacion.update(invitacion);

        return futuro(() -> altaOperador);

    }

    public Futuro<EmpresaUsuarioOB> nuevoOperadorAdministrador(ContextoOB contexto, String usuario, String clave, Long cuil, InvitacionAdministradorOB invitacionAdmin) {

        SesionOB sesion = contexto.sesion();

        UsuarioOB usuarioOB = new UsuarioOB();
        usuarioOB.nombre = invitacionAdmin.usu_nombre;
        usuarioOB.apellido = invitacionAdmin.usu_apellido;
        usuarioOB.email = invitacionAdmin.usu_correo;
        usuarioOB.emailValidado = true;
        usuarioOB.idCobis = invitacionAdmin.usu_idCobis;
        usuarioOB.login = invitacionAdmin.estado.ordinal() == 8 ? "" : Config.encriptarAES(usuario);
        usuarioOB.numeroDocumento = invitacionAdmin.usu_nro_documento;
        usuarioOB.telefonoMovil = invitacionAdmin.usu_telefono_movil;
        usuarioOB.cuil = cuil;
        usuarioOB.validoOTP = true;
        usuarioOB.migrado = invitacionAdmin.estado.ordinal() == 8 ? (byte) 1 : (byte) 2;
                
        if (empty(usuarioOB.idCobis)) {
            try {
                Futuro<Persona> futuroPersona = ApiPersonas.persona(contexto, usuarioOB.cuil.toString());
                Persona persona = futuroPersona.get();

                usuarioOB.idCobis = Integer.parseInt(persona.idCliente) < 0 ? null : persona.idCliente;
            } catch (Exception e) {
                LogOB.evento(contexto, "NUEVOADMIN - ApiPersona.idCliente", new Objeto().set("Cuil", usuarioOB.cuil).set("error", e.getMessage()));
            }
        }

        ServicioEstadoUsuarioOB servicioEstado = new ServicioEstadoUsuarioOB(contexto);
        usuarioOB.estado = servicioEstado.find(1).get();

        sesion.tokenInvitacionCorreo = null;
        sesion.tokenInvitacionSMS = null;
        sesion.save();

        UsuarioOB usuarioCreado = repo.create(usuarioOB);

        ServicioClaveUsuarioOB servicioClaveUsuario = new ServicioClaveUsuarioOB(contexto);
        ClaveUsuarioOB nuevaClave = new ClaveUsuarioOB();
        nuevaClave.clave = invitacionAdmin.estado.ordinal() == 8 ? "" : Encriptador.sha512(clave);
        nuevaClave.usuario = usuarioCreado;
        nuevaClave.fechaCreacion = LocalDateTime.now();
        servicioClaveUsuario.create(nuevaClave);
        usuarioOB.estado = servicioEstado.find(1).get();

        ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
        EmpresaUsuarioOB altaOperador = servicioEmpresaUsuario.vinculacionOperadorAdministradorEmpresa(contexto, usuarioCreado, invitacionAdmin.empresa, null, null).get();
        if (empty(altaOperador)) {
            return null;
        }

        invitacionAdmin.estado = EnumEstadoInvitacionOB.ACEPTADA;

        ServicioInvitacionAdministradorOB servicioInvitacionAdministrador = new ServicioInvitacionAdministradorOB(contexto);
        servicioInvitacionAdministrador.update(invitacionAdmin);

        return futuro(() -> altaOperador);
    }

}