
package ar.com.hipotecario.canal.officebanking;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Servidor;
import ar.com.hipotecario.backend.servicio.api.inversiones.CuentaCuotapartistaResumenRequest;
import ar.com.hipotecario.canal.officebanking.cron.CronOBAProcesarTRN;
import ar.com.hipotecario.canal.officebanking.cron.CronOBAperturaYCierreCamara;
import ar.com.hipotecario.canal.officebanking.cron.CronOBLeeNovedadesInterbanking;
import ar.com.hipotecario.canal.officebanking.cron.CronOBProgramadasTRN;
import ar.com.hipotecario.canal.officebanking.cron.CronOBRechazarAcreditacionesSinFirma;
import ar.com.hipotecario.canal.officebanking.cron.CronOBRechazarCobranzaIntegralSinFirma;
import ar.com.hipotecario.canal.officebanking.cron.CronOBRechazarDebitoDirectoSinFirma;
import ar.com.hipotecario.canal.officebanking.cron.CronOBRechazarEcheqDescuentoSinFirma;
import ar.com.hipotecario.canal.officebanking.cron.CronOBRechazarEcheqEmitidoSinFirma;
import ar.com.hipotecario.canal.officebanking.cron.CronOBRechazarPagoDeServiciosSinFirma;
import ar.com.hipotecario.canal.officebanking.cron.CronOBRechazarPagosMasivosSinFirma;
import ar.com.hipotecario.canal.officebanking.cron.CronOBRechazarPagosVepSinFirma;
import ar.com.hipotecario.canal.officebanking.cron.CronOBRechazarTransferenciasYFCIAEjecutarSinFirma;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioTransferenciaOB;
import ar.com.hipotecario.canal.officebanking.cron.*;

public class ApiOfficeBanking extends CanalOfficeBanking {

    private static Config config = new Config();

    public static void main(String[] args) throws Exception {
        Servidor.main(args);
    }

    public static void iniciar() {
        if (Config.esOpenShift()) {
            try {
                // CRON - Apertura/cierre de camara Interbanking
                cron(config.string("ob_cron_apertura_camara", "*/1 0-23 * * MON-FRI"), new CronOBAperturaYCierreCamara());

                // CRON - Envío de Transferencias al core Programadas -> En Proceso
                cron(config.string("ob_cron_trn_programadas", "* * * * *"), new CronOBProgramadasTRN());

                // CRON - Envío de transferencias al core A Procesar -> En Proceso Inserta en --> PeticionesOB
                cron(config.string("ob_cron_trn_a_procesar", "*/1 * * * *"), new CronOBAProcesarTRN());

                // CRON - Lee novedades
                cron(config.string("ob_cron_trn_leer_novedades", "* 6-18 * * MON-FRI"), new CronOBLeeNovedadesInterbanking());

                // CRON - Rechaza transferencias sin firma a la fecha de ejecución
                cron(config.string("ob_cron_rechazar_trn_y_fci_sin_firma", "59 23 * * MON-FRI"), new CronOBRechazarTransferenciasYFCIAEjecutarSinFirma());

                //CRON - Rechaza las solicitudes de pago de servicios sin firma cuando ya no estan en el servicio de pendientes (se vencieron)
                cron(config.string("ob_cron_rechazar_pago_de_servicios_sin_firma", "59 23 * * MON-FRI"), new CronOBRechazarPagoDeServiciosSinFirma());

                //CRON - Rechaza las solicitudes de acreditaciones sin firma al final del día de carga del archivo
                cron(config.string("ob_cron_rechazar_acreditaciones_sin_firma", "59 23 * * MON-FRI"), new CronOBRechazarAcreditacionesSinFirma());

                //CRON - Rechaza las solicitudes de pago vep sin firma al vencimiento
                cron(config.string("ob_cron_rechazar_pago_vep_sin_firma", "59 23 * * MON-FRI"), new CronOBRechazarPagosVepSinFirma());

                //CRON - Rechaza las solicitudes de pagos masivos sin firma al vencimiento
                cron(config.string("ob_cron_rechazar_plan_sueldo_sin_firma", "59 23 * * MON-FRI"), new CronOBRechazarPagosMasivosSinFirma());

                //CRON - Rechaza las solicitudes de debito directo sin firma
                cron(config.string("ob_cron_rechazar_debito_directo_sin_firma", "59 23 * * MON-FRI"), new CronOBRechazarDebitoDirectoSinFirma());

                //CRON - Rechaza las solicitudes de cobranza integral sin firma
                cron(config.string("ob_cron_rechazar_cobranza_integral_sin_firma", "59 23 * * MON-FRI"), new CronOBRechazarCobranzaIntegralSinFirma());

                cron(config.string("ob_cron_rechazar_echeq_emitido_sin_firma", "59 23 * * MON-FRI"), new CronOBRechazarEcheqEmitidoSinFirma());

                cron(config.string("ob_cron_rechazar_echeq_descuento_sin_firma", "59 15 * * MON-FRI"), new CronOBRechazarEcheqDescuentoSinFirma());

                cron(config.string("ob_cron_cargar_convenios_rendiciones","30 7,8,10 * * MON-FRI"),new CronOBCargarConveniosConRendiciones());

            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        //TRANSMIT
        post("/ob/api/generar-csmIdAuth", contexto -> OBTransmit.generarCsmIdAuth(contexto));

        // 00. APLICACION
        get("/ob/api/health", contexto -> OBAplicacion.health(contexto), ROL_INVITADO);
        post("/ob/api/config", contexto -> OBAplicacion.config(contexto), ROL_INVITADO);
        get("/ob/api/version", contexto -> OBAplicacion.version(contexto), ROL_INVITADO);

        // 01. LOGIN
        post("/ob/api/prelogin", contexto -> OBLogin.prelogin(contexto), ROL_INVITADO);
        post("/ob/api/login", contexto -> OBLogin.login(contexto), ROL_INVITADO);
        post("/ob/api/pseudo-login", contexto -> OBLogin.pseudoLogin(contexto), ROL_INVITADO);
        get("/ob/api/login-viejo-ob", contexto -> OBLogin.loginViejoOB(contexto));
        get("/ob/api/login-nuevo-ob", contexto -> OBLogin.loginNuevoOB(contexto), ROL_INVITADO);
        post("/ob/api/logout", contexto -> OBLogin.logout(contexto), ROL_INVITADO);
        get("/ob/api/auto-login-ob", contexto -> OBLogin.autoLogin(contexto), ROL_INVITADO);

        // LOGS
        //post("/ob/api/log", contexto -> OBLogin.log(contexto), ROL_INVITADO);
        post("/ob/api/get-log", contexto -> OBLogin.getLog(contexto), ROL_INVITADO);

        // 02. USUARIOS
        post("/ob/api/usuario", contexto -> OBUsuarios.usuario(contexto));
        post("/ob/api/modificar-datos-personales", contexto -> OBUsuarios.modificarDatosPersonales(contexto));
        post("/ob/api/modificar-telefono-movil", contexto -> OBUsuarios.modificarTelefonoMovil(contexto));
        post("/ob/api/modificar-nombre", contexto -> OBUsuarios.modificarNombre(contexto));
        post("/ob/api/modificar-usuario", contexto -> OBUsuarios.modificarUsuario(contexto));
        post("/ob/api/datos-personales", contexto -> OBUsuarios.datosPersonales(contexto));
        post("/ob/api/cambiar-empresa", contexto -> OBUsuarios.cambiarEmpresa(contexto));
        post("/ob/api/cambiar-clave", contexto -> OBUsuarios.cambiarClave(contexto));//-------------------
        post("/ob/api/validar-acceso", contexto -> OBUsuarios.validarAcceso(contexto));
        get("/ob/api/consulta-persona-fisica", contexto -> OBUsuarios.consultaPersonaFisica(contexto), ROL_INVITADO);
        post("/ob/api/modificar-adhesion-gire", contexto -> OBUsuarios.updateAdheridoGire(contexto));
        post("/ob/api/inhabilitar", contexto -> OBUsuarios.inhabilitar(contexto),ROL_INVITADO);

        // 03. seguridad
        get("/ob/api/captcha", contexto -> OBSeguridad.captcha(contexto), ROL_INVITADO);
        post("/ob/api/validar-captcha", contexto -> OBSeguridad.validarCaptcha(contexto), ROL_INVITADO);
        post("/ob/api/validar-correo-celular", contexto -> OBSeguridad.validarCorreoYCelular(contexto), ROL_INVITADO);
        post("/ob/api/enviar-token-invitacion", contexto -> OBSeguridad.enviarTokenInvitacion(contexto), ROL_INVITADO);
        post("/ob/api/enviar-token-invitacion-admin", contexto -> OBSeguridad.enviarTokenInvitacionAdministrador(contexto), ROL_INVITADO);
        post("/ob/api/validar-token-invitacion", contexto -> OBSeguridad.validarTokenInvitacion(contexto), ROL_INVITADO);
        post("/ob/api/enviar-sms-invitacion", contexto -> OBSeguridad.enviarSmsInvitacion(contexto), ROL_INVITADO);
        post("/ob/api/enviar-sms-invitacion-admin", contexto -> OBSeguridad.enviarSmsInvitacionAdministrador(contexto), ROL_INVITADO);
        post("/ob/api/validar-sms-invitacion", contexto -> OBSeguridad.validarSmsInvitacion(contexto), ROL_INVITADO);
        post("/ob/api/recuperar-usuario", contexto -> OBSeguridad.recuperarUsuario(contexto), ROL_INVITADO);
        post("/ob/api/recuperar-usuario-clave", contexto -> OBSeguridad.recuperarUsuarioClave(contexto), ROL_INVITADO);
        post("/ob/api/alta-usuario-gire", contexto -> OBSeguridad.usuarioGirePost(contexto), ROL_INVITADO);
        post("/ob/api/login-gire", contexto -> OBSeguridad.loginGire(contexto), ROL_INVITADO);

        post("/ob/api/recuperar-clave", contexto -> OBSeguridad.recuperarClave(contexto), ROL_IDENTIFICADO);
        post("/ob/api/validadores", contexto -> OBSeguridad.validadores(contexto), ROL_IDENTIFICADO);
        post("/ob/api/v1/validadores", contexto -> OBSeguridad.V1validadores(contexto), ROL_IDENTIFICADO);
        post("/ob/api/enviar-token", contexto -> OBSeguridad.enviarToken(contexto), ROL_IDENTIFICADO);
        post("/ob/api/validar-token", contexto -> OBSeguridad.validarToken(contexto), ROL_IDENTIFICADO);
        post("/ob/api/validar-factores", contexto -> OBSeguridad.validarFactores(contexto), ROL_IDENTIFICADO);

        get("/ob/api/alta-softtoken", contexto -> OBSeguridad.altaSoftToken(contexto));
        post("/ob/api/enviar-alta-softtoken", contexto -> OBSeguridad.enviarAltaSoftToken(contexto));
        post("/ob/api/validar-alta-softtoken", contexto -> OBSeguridad.validarAltaSoftToken(contexto));
        post("/ob/api/baja-softtoken", contexto -> OBSeguridad.bajaSoftToken(contexto));
        post("/ob/api/enviar-token-correo", contexto -> OBSeguridad.enviarTokenCorreo(contexto));
        post("/ob/api/validar-token-correo", contexto -> OBSeguridad.validarTokenCorreo(contexto));

        get("/ob/api/enviar-actualizar-sms", contexto -> OBSeguridad.enviarActualizarSms(contexto), ROL_INVITADO);
        post("/ob/api/validar-actualizar-sms", contexto -> OBSeguridad.validarActualizarSms(contexto));
        post("/ob/api/validar-token-actualizar-sms", contexto -> OBSeguridad.validarTokenActualizarSms(contexto));

        // 04. SEGURIDAD OB ANTERIOR
        post("/oba/api/login", contexto -> OBSeguridadAnterior.login(contexto), ROL_INVITADO);
        post("/oba/api/enviar-token-correo", contexto -> OBSeguridadAnterior.enviarTokenCorreo(contexto), ROL_OB_ANTERIOR);
        post("/oba/api/validar-token-correo", contexto -> OBSeguridadAnterior.validarTokenCorreo(contexto), ROL_OB_ANTERIOR);
        post("/oba/api/validar-dni", contexto -> OBSeguridadAnterior.validarDNI(contexto), ROL_OB_ANTERIOR);
        post("/oba/api/validadores", contexto -> OBSeguridadAnterior.validadores(contexto), ROL_OB_ANTERIOR);
        post("/oba/api/enviar-token", contexto -> OBSeguridadAnterior.enviarToken(contexto), ROL_OB_ANTERIOR);
        post("/oba/api/validar-token", contexto -> OBSeguridadAnterior.validarToken(contexto), ROL_OB_ANTERIOR);
        post("/oba/api/migrar-usuario", contexto -> OBSeguridadAnterior.migrarUsuario(contexto), ROL_OB_ANTERIOR);

        // 05. ADMINISTRACION
        post("/ob/api/invitacion", contexto -> OBAdministracion.invitacion(contexto), ROL_INVITADO);
        post("/ob/api/validar-dni-operador", contexto -> OBAdministracion.validarDniOperador(contexto), ROL_INVITADO);
        post("/ob/api/nuevo-operador", contexto -> OBAdministracion.nuevoOperador(contexto), ROL_INVITADO);
        post("/ob/api/nuevo-operador-admin", contexto -> OBAdministracion.nuevoOperadorAdministrador(contexto), ROL_INVITADO);

        post("/ob/api/enviar-invitacion", contexto -> OBAdministracion.enviarInvitacion(contexto));
        post("/ob/api/invitaciones", contexto -> OBAdministracion.invitaciones(contexto));
        post("/ob/api/operadores", contexto -> OBAdministracion.operadores(contexto));
        post("/ob/api/validar-alta-operador", contexto -> OBAdministracion.validarAltaOperador(contexto));

        post("/ob/api/alta-operador", contexto -> OBAdministracion.altaOperador(contexto));
        post("/ob/api/baja-operador", contexto -> OBAdministracion.bajaOperador(contexto));
        post("/ob/api/editar-permisos-operador", contexto -> OBAdministracion.editarPermisosOperador(contexto));
        post("/ob/api/editar-cuentas-operador", contexto -> OBAdministracion.editarCuentasOperador(contexto));

        post("/ob/api/permisos-operador", contexto -> OBAdministracion.permisosOperador(contexto));
        post("/ob/api/cuentas-operador", contexto -> OBAdministracion.cuentasOperador(contexto));
        post("/ob/api/permiso-usuario", contexto -> OBAdministracion.permisoUsuario(contexto));
        post("/ob/api/cancelar-invitacion", contexto -> OBAdministracion.cancelarInvitacion(contexto));

        post("/ob/api/permisos-sin-asignar", contexto -> OBAdministracion.permisosSinAsignar(contexto));
        post("/ob/api/solicitar-nuevo-permiso", contexto -> OBAdministracion.solicitarNuevoPermiso(contexto));

        // 00. CRM - SUSCRIPCION JDC
        post("/ob/api/crm/suscripcion", contexto -> OBAdministracion.suscripcionAdministrador(contexto), ROL_INVITADO);
        post("/ob/api/crm/valida-suscripcion", contexto -> OBAdministracion.validarSuscripcionOperadorAdministrador(contexto), ROL_INVITADO);
        post("/ob/api/crm/nuevo-administrador", contexto -> OBAdministracion.nuevoOperadorAdministrador(contexto), ROL_INVITADO);

        // 00. CRM - SUSCRIPCION JDC
        post("/crm/api/suscripcion", contexto -> OBAdministracion.suscripcionAdministrador(contexto), ROL_INVITADO);
        post("/crm/api/valida-suscripcion", contexto -> OBAdministracion.validarSuscripcionOperadorAdministrador(contexto), ROL_INVITADO);
        post("/crm/api/alta-administrador", contexto -> OBAdministracion.nuevoOperadorAdministrador(contexto), ROL_INVITADO);
        get("/crm/api/obtener-suscripcion", contexto -> OBAdministracion.obtenerSuscripcionOperadorAdministrador(contexto), ROL_INVITADO);
        post("/crm/api/editar-suscripcion", contexto -> OBAdministracion.editarInvitacion(contexto), ROL_INVITADO);
        
        get("/ob/api/crm/empresas-con-admin", contexto -> OBEmpresas.empresasConAdmin(contexto), ROL_INVITADO);
        get("/ob/api/crm/consulta-persona-fisica", contexto -> OBUsuarios.consultaPersonaFisica(contexto), ROL_INVITADO);
        get("/ob/api/crm/admins-de-empresa", contexto -> OBEmpresas.adminEmpresa(contexto), ROL_INVITADO);

        get("/crm/api/empresas-con-admin", contexto -> OBEmpresas.empresasConAdmin(contexto), ROL_INVITADO);
        get("/crm/api/consulta-persona-fisica", contexto -> OBUsuarios.consultaPersonaFisica(contexto), ROL_INVITADO);
        get("/crm/api/admins-de-empresa", contexto -> OBEmpresas.adminEmpresa(contexto), ROL_INVITADO);
        get("/ob/crm/api/empresa/:cuitempresa/usuarios", contexto -> OBEmpresas.usuariosEmpresa(contexto), ROL_CRM);
        patch("/ob/crm/api/empresa/:cuitempresa/usuario/:codigo", contexto -> OBUsuarios.modificarDatosUsuario(contexto),ROL_CRM);
        post("/ob/crm/api/cambio-administrador", contexto -> OBAdministracion.cambioAdministrador(contexto), ROL_CRM);

        // 06. TRANSFERENCIAS
        post("/ob/api/validar-cbu-alias", contexto -> OBTransferencias.validarCBUAlias(contexto));

        post("/ob/api/beneficiarios", contexto -> OBBeneficiarios.beneficiarios(contexto));
        post("/ob/api/beneficiario", contexto -> OBBeneficiarios.beneficiario(contexto));
        post("/ob/api/alta-beneficiario", contexto -> OBBeneficiarios.alta(contexto));
        post("/ob/api/editar-beneficiario", contexto -> OBBeneficiarios.editar(contexto));
        post("/ob/api/baja-beneficiario", contexto -> OBBeneficiarios.baja(contexto));
        post("/ob/api/validar-cbu-beneficiario", contexto -> OBBeneficiarios.validarCbuBeneficiario(contexto));

        post("/ob/api/monedas", contexto -> OBTransferencias.monedas(contexto));

        post("/ob/api/trn/conceptos", contexto -> OBTransferencias.conceptos(contexto));
        post("/ob/api/trn/estadosHistorialTrn", contexto -> OBTransferencias.estadosHistorialTrn(contexto));
        post("/ob/api/trn/horarioCamara", contexto -> OBTransferencias.horarioCamara(contexto));

        post("/ob/api/trn/cargar-transferencia", contexto -> OBTransferencias.cargarTransferencia(contexto));
        post("/ob/api/trn/detalle", contexto -> OBTransferencias.detalle(contexto));
        post("/ob/api/trn/ultimas", contexto -> OBTransferencias.ultimas(contexto));
        post("/ob/api/trn/listar", contexto -> OBTransferencias.listar(contexto));
        post("/ob/api/trn/validar-monto", contexto -> OBTransferencias.validarMonto(contexto));

        post("/ob/api/trn/validar-dia-habil", contexto -> OBTransferencias.validarDiaHabil(contexto));
        post("/ob/api/trn/puede-avanzar",contexto ->OBTransferencias.puedeAvanzarTransferencia(contexto));
        get("/ob/api/trn/limite-unipersonal",contexto->OBTransferencias.limiteTransferenciaNuevoUniPersonal(contexto));
        post("/ob/api/trn/anular-programada",contexto->OBTransferencias.anularTransferenciaProgramada(contexto,new ServicioTransferenciaOB(contexto)));
        post("/ob/api/credin",contexto->OBTransferencias.pruebaCredin(contexto));

        
        //CONTROL DUAL
        post("/ob/api/cd/aprobar", contexto -> OBControlDual.aprobar(contexto));
        post("/ob/api/cd/rechazar", contexto -> OBControlDual.rechazar(contexto));

        // 07. FIRMAS
        post("/ob/api/trn/firmas/tipofirma", contexto -> OBFirmas.tipoFirma(contexto));
        post("/ob/api/trn/firmas/firmar", contexto -> OBFirmas.firmar(contexto));
        post("/ob/api/trn/firmas/rechazar", contexto -> OBFirmas.rechazar(contexto));
        post("/ob/api/trn/firmas/pendientes", contexto -> OBFirmas.pendientesDeFirma(contexto));
        post("/ob/api/trn/firmas/pendiente-perfil-inversor", contexto -> OBFirmas.pendientePerfilInversor(contexto));
        post("/ob/api/trn/firmas/productos-pendientes", contexto -> OBFirmas.productosPendientesDeFirma(contexto));
        post("/ob/api/trn/firmas/operaciones", contexto -> OBFirmas.listarOperacionesActivas(contexto));
        post("/ob/api/trn/firmas/completa-firma", contexto -> OBFirmas.completaFirma(contexto));
        post("/ob/api/trn/firmas/habilita-bandeja", contexto -> OBFirmas.habilitaBandeja(contexto));
        post("/ob/api/trn/firmas/cuentas-en-dictamen", contexto -> OBFirmas.listarCuentasEnDictamen(contexto));
        post("/ob/api/trn/firmas/puedo-firmar", contexto -> OBFirmas.puedeFirmar(contexto));
        post("/ob/api/trn/firmas/puedo-firmar-comex", contexto -> OBFirmas.puedeFirmarComex(contexto));
        post("/ob/api/trn/firmas/puedo-firmar-multiple", contexto -> OBFirmas.puedeFirmarMultiple(contexto));
        post("/ob/api/trn/firmas/puedo-firmar-perfil-inversor", contexto -> OBFirmas.puedeFirmarPerfilInversor(contexto));
        post("/ob/api/trn/firmas/tipos-productos", contexto -> OBFirmas.tiposProductos(contexto));
        post("/ob/api/ec/firmas/puedo-firmar-echeq", contexto -> OBFirmas.puedeFirmarEcheq(contexto));
        post("/ob/api/trn/firmas/tiene-fondos",contexto-> OBFirmas.tieneFondos(contexto));

        // 08. CUENTAS
        post("/ob/api/cuentas", contexto -> OBCuentas.cuentas(contexto));
        post("/ob/api/cuentas-por_moneda", contexto -> OBCuentas.cuentasPorMoneda(contexto));
        post("/ob/api/cuentasHabilitadas", contexto -> OBCuentas.cuentasHabilitadas(contexto));
        put("/ob/api/cuentas/alias", contexto -> OBCuentas.modificarAliasCuenta(contexto));
        get("/ob/api/cuentas/alias", contexto -> OBCuentas.obtenerInfoAliasTyC(contexto));
        get("/ob/api/cuentas/qr", contexto -> OBCuentas.obtenerQRCuenta(contexto));
        post("/ob/api/validarCuentaUnipersonal", contexto -> OBCuentas.validarCuentaUnipersonal(contexto));
        post("/ob/api/compartir/cbu-alias", contexto -> OBCuentas.CompartirCbuAlias(contexto));
        post("/ob/api/ultimos-movimientos", contexto -> OBCuentas.ultimosMovimientos(contexto));
        post("/ob/api/filtro-movimientos", contexto -> OBCuentas.filtroMovimientos(contexto));
        post("/ob/api/cuenta-corriente", contexto -> OBCuentas.cuentaCorriente(contexto));
        post("/ob/api/caja-ahorro", contexto -> OBCuentas.cajaAhorro(contexto));
        get("/ob/api/descarga-movimientos", contexto -> OBCuentas.descargaMovimientos(contexto));
        get("/ob/api/descarga-resumen", contexto -> OBCuentas.descargaResumen(contexto));
        post("/ob/api/tyc/qr", contexto -> OBCuentas.aceptarTerminosYCondiucionesQR(contexto));

        // 09. EMPRESAS
        post("/ob/api/posicion-consolidada", contexto -> OBEmpresas.posicionConsolidada(contexto));
        get("/ob/api/empresas-con-admin", contexto -> OBEmpresas.empresasConAdmin(contexto), ROL_INVITADO);
        get("/ob/api/admin-de-empresa", contexto -> OBEmpresas.adminEmpresa(contexto), ROL_INVITADO);

        // 10. INVERSIONES
        
        //Cuentas Comitentes
        post("/ob/api/inversiones/cuentas-comitentes-total-invertido", contexto -> OBInversiones.cuentasComitentesTotalInvertido(contexto));
        post("/ob/api/inversiones/obtener-tenencia-especies", contexto -> OBInversiones.obtenerTenenciaEspecies(contexto));
        post("/ob/api/inversiones/historial-comitente", contexto -> OBInversiones.historialComitente(contexto));        
        post("/ob/api/inversiones/detalle-tenencia", contexto -> OBInversiones.detalleTenenciaEspecies(contexto));
        post("/ob/api/inversiones/detalle-comitente", contexto -> OBInversiones.detalleComitente(contexto));
        post("/ob/api/cuentas-comitentesActivas", contexto -> OBInversiones.cuentasComitentesActivas(contexto));        
        post("/ob/api/inversiones/extracto-tenencia-comitente", contexto -> OBInversiones.extractoTenencias(contexto));
        post("/ob/api/inversiones/descargar-historial-comitente", contexto -> OBInversiones.descargarHistorialComitente(contexto));
        post("/ob/api/inversiones/comprobante-movimiento", contexto -> OBInversiones.comprobanteMovimientos(contexto));
        post("/ob/api/cuentas-comitentes", contexto -> OBInversiones.cuentasComitentes(contexto));
        post("/ob/api/plazos-fijos", contexto -> OBInversiones.plazosFijos(contexto));
        get("ob/api/inversiones/boleto-movimientos-habilitado", contexto -> OBInversiones.nuevoBoletoHabilitado(contexto));
        get("ob/api/inversiones/habilita-prueba-piloto", contexto -> OBInversiones.nuevoBoletoHabilitado(contexto));

        //CEDIP
        get("/ob/api/cedips/:cuit", contexto -> OBInversiones.cedips(contexto));
        get("/ob/api/cedipsRecibidos/:cuit", contexto -> OBInversiones.cedipsRecibidos(contexto));
        get("/ob/api/detalleCedip/:cedipId/:cuit/:fraccion", contexto -> OBInversiones.detalleCedip(contexto));
        get("/ob/api/plazoFijos/simulacion", contexto -> OBInversiones.simularPlazoFijo(contexto));
        post("/ob/api/cedip/enviar-cedip-bandeja/:accion", contexto -> OBInversiones.cargarCedip(contexto));
        post("/ob/api/nuevo-cedip", contexto -> OBInversiones.cedipNuevo(contexto));
        post("/ob/api/cedip/transmitir", contexto -> OBInversiones.transmitir(contexto));
        post("/ob/api/cedip/admitir", contexto -> OBInversiones.admitir(contexto));
        post("/ob/api/cedip/repudiar", contexto -> OBInversiones.repudiar(contexto));
        post("/ob/api/cedip/anular", contexto -> OBInversiones.anularTransmision(contexto));
        put("/ob/api/cedip/modificar", contexto -> OBInversiones.modificarAcreditacionCbu(contexto));
        post("/ob/api/cedip/depositar", contexto -> OBInversiones.depositarCedip(contexto));
        get("/ob/api/cedip/empresa/:cuit", contexto -> OBInversiones.consultaCuit(contexto));
        //FIRMA CEDIP
        post("/ob/api/cedip/firmas/pendientes", contexto -> OBFirmas.pendientesDeFirma(contexto));

        //PLAZO FIJO
        get("/ob/api/get-plazos-fijos/:idcliente", contexto -> OBInversiones.getPlazosFijos(contexto));
        get("/ob/api/get-plazos-fijos-detalle/:numeroBanco", contexto -> OBInversiones.getPlazosFijosDetalle(contexto));
        get("/ob/api/get-plazos-fijos-comprobante/:numeroBanco", contexto -> OBInversiones.getComprobantePlazoFijo(contexto));
        get("/ob/api/get-plazos-fijos-validacion-comprobante", contexto -> OBInversiones.comprobantePlazoFijoHabilitado(contexto));
        get("/ob/api/get-plazos-fijos-dolar", contexto -> OBInversiones.plazoFijoDolarHabilitado(contexto));
        get("/ob/api/tasas/:idcliente", contexto -> OBInversiones.getTasas(contexto));
        post("/ob/api/nuevo-plazofijo", contexto -> OBInversiones.plazoFijoNuevo(contexto));
        post("/ob/api/inversiones/enviar-plazofijo-bandeja", contexto -> OBInversiones.bandejaPlazoFijo(contexto));
        post("/ob/api/inversiones/detalle-plazofijo-bandeja", contexto -> OBInversiones.detallePlazoFijoBandeja(contexto));
        get("/ob/api/plazo-fijo/cuentas-habilitadas", contexto -> OBInversiones.cuentasPFHabilitadas(contexto));

        post("/ob/api/cuentas-cuotapartistas-resumen", contexto -> OBInversiones.obtenerResumenCuentaCuotapartista(contexto));
        get("/ob/api/habilita-fci-informes", contexto -> OBInversiones.habilitaFCIInformes(contexto));
        post("/ob/api/cuentas-cuotapartistas", contexto -> OBInversiones.cuentasCuotapartistas(contexto));
        post("/ob/api/inversiones/posicion-cuotapartista", contexto -> OBInversiones.posicionCuotapartista(contexto));
        post("/ob/api/inversiones/posicion-cuotapartista-consolidada", contexto -> OBInversiones.posicionCuotapartistaConsolidada(contexto));
        post("/ob/api/inversiones/fondos", contexto -> OBInversiones.fondosParaOperar(contexto));
        post("/ob/api/inversiones/invertir", contexto -> OBInversiones.suscribir(contexto));
        post("/ob/api/inversiones/rescatar", contexto -> OBInversiones.rescatar(contexto));
        post("/ob/api/inversiones/detalle", contexto -> OBInversiones.detalle(contexto));

        post("/ob/api/inversiones/perfil", contexto -> OBInversiones.perfil(contexto));
        post("/ob/api/inversiones/seleccionar-perfil", contexto -> OBInversiones.seleccionarPerfil(contexto));
        post("/ob/api/inversiones/preguntasPI", contexto -> OBInversiones.preguntasPerfilInversor(contexto));
        post("/ob/api/inversiones/formulario", contexto -> OBInversiones.formulario(contexto));
        post("/ob/api/inversiones/detalle-perfil-inversor", contexto -> OBInversiones.detallePerfilInversor(contexto));
        post("/ob/api/inversiones/ya-existe-perfil-inversor-bandeja", contexto -> OBInversiones.yaExisteSolicitudPerfilInversorEnBandeja(contexto));
        post("/ob/api/inversiones/validar-monto-inversion", contexto -> OBInversiones.validarMontoInversion(contexto));
        post("/ob/api/inversiones/aceptar-fondo", contexto -> OBInversiones.aceptarFondo(contexto));
        post("/ob/api/inversiones/fondo-aceptado", contexto -> OBInversiones.fondoAceptado(contexto));
        post("/ob/api/inversiones/dia-habil", contexto -> OBInversiones.diaHabil(contexto));
        post("/ob/api/inversiones/historial", contexto -> OBInversiones.historial(contexto));
        post("/ob/api/inversiones/historial-fci", contexto -> OBInversiones.historialFci(contexto));
        post("/ob/api/inversiones/obtener-detalle-solicitud", contexto -> OBInversiones.obtenerDetallesSolicitud(contexto));
        post("/ob/api/inversiones/extracto-fci", contexto -> OBInversiones.extractoFci(contexto));
        post("/ob/api/inversiones/obtenerToken-VFNet", contexto -> OBInversiones.obtenerTokenVFNet(contexto));
        
        
        // 10. PAGO DE SERVICIOS
        post("/ob/api/ps/obtener-tarjeta-virtual", contexto -> OBTarjetaVirtual.obtenerTarjetasVirtuales(contexto));
        post("/ob/api/ps/rubros", contexto -> OBPagos.rubros(contexto));
        post("/ob/api/ps/entes", contexto -> OBPagos.entes(contexto));
        post("/ob/api/ps/ente", contexto -> OBPagos.ente(contexto));

        post("/ob/api/ps/adhesiones", contexto -> OBPagos.adhesiones(contexto));
        post("/ob/api/ps/crear-adhesion", contexto -> OBPagos.crearAdhesion(contexto));
        post("/ob/api/ps/eliminar-adhesion", contexto -> OBPagos.eliminarAdhesion(contexto));
        post("/ob/api/ps/entes-adheridos", contexto -> OBPagos.entesAdheridos(contexto));
        
        post("/ob/api/ps/pendientes", contexto -> OBPagos.pagosPendientes(contexto));
        post("/ob/api/ps/cargar-pago", contexto -> OBPagos.cargarPago(contexto));

        post("/ob/api/ps/validar-pago-bandeja", contexto -> OBPagos.validarPagoEnBandeja(contexto));

        post("/ob/api/ps/elegir-ente-comprobantes", contexto -> OBPagos.elegirEnteComprobantes(contexto));
        post("/ob/api/ps/historial-pagos-servcios", contexto -> OBPagos.HistorialPagosServicios(contexto));
        post("/ob/api/ps/detalle-solicitud", contexto -> OBPagos.detalleSolicitud(contexto));
        post("/ob/api/ps/datos-pago", contexto -> OBPagos.obtenerDatosPago(contexto));
        post("/ob/api/ps/detalle-comprobante", contexto -> OBPagos.detalleComprobante(contexto));
        post("/ob/api/ps/detalle-comprobante-servicios-veps", contexto -> OBPagos.detalleComprobanteServiciosVeps(contexto));
        post("/ob/api/ps/conceptos", contexto -> OBPagos.conceptos(contexto));
        post("/ob/api/ps/filtro-entes", contexto -> OBPagos.filtroEnte(contexto));
        post("/ob/api/ps/editar-referencia",contexto->OBPagos.editarReferencia(contexto));

        // 12. LOGS
        post("/ob/api/saveLog", contexto -> OBSeguridad.saveLog(contexto), ROL_INVITADO);
        post("/ob/api/extraerContenido", contexto -> OBSeguridad.extraerContenido(contexto), ROL_INVITADO);
        get("/ob/api/logs/:params", contexto -> OBReportes.Logs(contexto), ROL_INVITADO);
        get("/ob/api/logs/:params/:empresa", contexto -> OBReportes.Logs(contexto), ROL_INVITADO);

        // 13. NUEVO OB - VEPS
        post("/ob/api/pv/cargar-pago-vep", contexto -> OBPagosVeps.cargarPagoVep(contexto));
        post("/ob/api/pv/pagar-vep-propio", contexto -> OBPagosVeps.pagarVepPropio(contexto));
        post("/ob/api/pv/pagar-tercero", contexto -> OBPagosVeps.pagarTercero(contexto));
        post("/ob/api/pv/pagar-otro-contribuyente", contexto -> OBPagosVeps.pagarOtroContribuyente(contexto));
        post("/ob/api/pv/detalle-comprobante", contexto -> OBPagosVeps.detalleComprobante(contexto));
        post("/ob/api/pv/detalle-pago", contexto -> OBPagosVeps.detallePago(contexto));
        post("/ob/api/pv/tokenAFIP", contexto -> OBPagosVeps.tokenAFIP(contexto));

        post("/ob/api/pv/alta-contribuyente", contexto -> OBPagosVeps.altaContribuyente(contexto));
        post("/ob/api/pv/contribuyentes", contexto -> OBPagosVeps.consultaContribuyente(contexto));
        delete("/ob/api/pv/baja-contribuyente", contexto -> OBPagosVeps.bajaContribuyente(contexto));

        // 14. VIEJO OB - VEPS
        post("/ob/api/veps", contexto -> OBVeps.pagarVep(contexto), ROL_API_OB_ANTERIOR);
        get("/ob/api/veps/:idtributariocliente/pendientes", contexto -> OBVeps.vepsPendiente(contexto), ROL_API_OB_ANTERIOR);
        get("/ob/api/veps/:idtributariocliente/pagados", contexto -> OBVeps.vepsPagados(contexto), ROL_API_OB_ANTERIOR);
        delete("/ob/api/veps", contexto -> OBVeps.eliminarVep(contexto), ROL_API_OB_ANTERIOR);
        post("/ob/api/tokenAFIP", contexto -> OBVeps.tokenAFIP(contexto), ROL_API_OB_ANTERIOR);
        get("/ob/api/veps/contribuyentes/:idtributariocliente", contexto -> OBVeps.consultaContribuyente(contexto), ROL_API_OB_ANTERIOR);
        post("/ob/api/veps/contribuyentes", contexto -> OBVeps.altaContribuyente(contexto), ROL_API_OB_ANTERIOR);
        delete("/ob/api/veps/contribuyentes", contexto -> OBVeps.bajaContribuyente(contexto), ROL_API_OB_ANTERIOR);

        // 15. PAGO DE HABERES
        get("/ob/api/ph/convenios", contexto -> OBPagoHaberes.consultaCovenios(contexto));
        get("/ob/api/ph/descargar-template", contexto -> OBPagoHaberes.descargaTemplate(contexto));
        get("/ob/api/ph/lotes", contexto -> OBPagoHaberes.consultaLotes(contexto));
        post("/ob/api/ph/precarga-haberes", contexto -> OBPagoHaberes.precargaAcreditaciones(contexto));
        post("/ob/api/ph/cargar-haberes", contexto -> OBPagoHaberes.cargarHaberes(contexto));
        post("/ob/api/ph/acreditacion-detalle", contexto -> OBPagoHaberes.acreditacionDetalle(contexto));
        post("/ob/api/ph/historial-acreditaciones", contexto -> OBPagoHaberes.historialAcreditaciones(contexto));
        post("/ob/api/ph/historial-nominas", contexto -> OBPagoHaberes.historialNominas(contexto));
        post("/ob/api/ph/detalle-solicitud", contexto -> OBPagoHaberes.detalleSolicitudAcreditaciones(contexto));
        post("/ob/api/ph/ver-tabla", contexto -> {
            try {
                return OBPagoHaberes.verTablaAcreditaciones(contexto);
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        });
        get("/ob/api/ph/estados", contexto -> OBPagoHaberes.consultaEstados(contexto));
        get("/ob/api/ph/estados-nomina", contexto -> OBPagoHaberes.consultaEstadosNomina(contexto));

        post("/ob/api/ph/precarga-nomina", contexto -> OBPagoHaberes.precargaNomina(contexto));
        post("/ob/api/ph/cargar-nomina", contexto -> {
            try {
                return OBPagoHaberes.cargarNomina(contexto);
            } catch (ServletException | SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        });
        post("/ob/api/ph/ver-tabla-nomina", contexto -> {
            try {
                return OBPagoHaberes.verTablaNomina(contexto);
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        });
        get("/ob/api/ph/obtener-operadores", contexto -> OBPagoHaberes.obenerOperadores(contexto));
        post("/ob/api/ph/guardar-nomina-confidencial", contexto -> OBPagoHaberes.guardarNominaConfidencial(contexto));
        post("/ob/api/ph/descargar-nomina", contexto -> OBPagoHaberes.descargarNomina(contexto));
        post("/ob/api/ph/descargar-acreditaciones", contexto -> {
            try {
                return OBPagoHaberes.descargarAcreditaciones(contexto);
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        });
        post("/ob/api/ph/historial-fcl", contexto -> OBPagoHaberes.historiaFCL(contexto));
        post("/ob/api/ph/detalle-fcl", contexto -> OBPagoHaberes.detalleMovimeintoFCL(contexto));
        
        // 16. PAGO A PROVEEDORES
        get("/ob/api/pp/convenios", contexto -> OBPagoProveedores.consultaCovenios(contexto));
        post("/ob/api/pp/cargar-pap", contexto -> {
            try {
                return OBPagoProveedores.cargarPap(contexto);
            } catch (SQLException | IOException | ServletException e) {
                throw new RuntimeException(e);
            }
        });
        get("/ob/api/pp/descargar-template", contexto -> OBPagoProveedores.descargaTemplate(contexto));
        get("ob/api/pp/listarArchivos", contexto -> OBPagoProveedores.listarArchivos(contexto));
        post("ob/api/pp/listarArchivosEnviados", contexto -> OBPagoProveedores.listarArchivosEnviados(contexto));
        post("ob/api/pp/listarArchivosComprobantes", contexto -> OBPagoProveedores.listarArchivosComprobantes(contexto));
        post("ob/api/pp/descargaArchivosEnviados", contexto -> OBPagoProveedores.descargaArchivosEnviados(contexto));
        post("ob/api/pp/ver-tabla-CR", contexto -> OBPagoProveedores.verTablaComprobantes(contexto));
        post("/ob/api/pp/precarga-pap", contexto -> OBPagoProveedores.precargaPap(contexto));
        post("/ob/api/pp/precarga-comprobantes",contexo->OBPagoProveedores.precargaComprobanteRetenciones(contexo));
        post("/ob/api/pp/precarga-pap-pb", contexto -> OBPagoProveedores.precargaPapPB(contexto));
        post("/ob/api/pp/cargar-pap-pb", contexto -> {
            try {
                return OBPagoProveedores.cargarPapPB(contexto);
            } catch (SQLException | IOException | ServletException e) {
                throw new RuntimeException(e);
            }
        });
        post("ob/api/pp/ver-tabla-pb", contexto -> {
            try {
                return OBPagoProveedores.verTablaPB(contexto);
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        });
        get("/ob/api/pp/estados", contexto -> OBPagoProveedores.consultaEstados(contexto));
        get("/ob/api/pp/detallesPAP", contexto -> OBPagoProveedores.detallesPAP(contexto));
        post("/ob/api/pp/historial-pago-proveedores", contexto -> OBPagoProveedores.historialPagoProveedores(contexto));
        post("ob/api/pp/ver-tabla", contexto -> {
            try {
                return OBPagoProveedores.verTablaPAP(contexto);
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        });
        post("/ob/api/pp/historial-ordenes-pago", contexto -> {
            try {
                return OBPagoProveedores.historialOrdenesPago(contexto);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        post("/ob/api/pp/detalle-orden-pago", contexto -> {
            try {
                return OBPagoProveedores.detalleOrdenPago(contexto);
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        });
        get("ob/api/pp/actualizar-estado", contexto -> OBPagoProveedores.actualizarEstado(contexto));
        get("/ob/api/pp/estados-ordenes-pago", contexto -> OBPagoProveedores.consultaEstadosOrdenes(contexto));
        post("/ob/api/pp/descargar-ordenes-pago", contexto -> {
            try {
                return OBPagoProveedores.descargarOrdenesDePago(contexto);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        post("/ob/api/pp/beneficiarios", contexto -> OBPagoProveedores.consultabeneficiarios(contexto),ROL_INVITADO);
        post("/ob/api/pp/carga-comprobantes",contexto-> {
            try {
                return OBPagoProveedores.cargarComprobantes(contexto);
            } catch (ServletException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        post("/ob/api/pp/stop-payment",contexto->OBPagoProveedores.stopPayment(contexto));
        post("/ob/api/archivos/encode", contexto -> {
			try {
				return OBPagoProveedores.getEncodingTest(contexto);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		},ROL_INVITADO);

        // 17. TERMINOS Y CONDICIONES
        get("/ob/api/tyc", contexto -> OBTyc.buscarTycByEmpresaAndProducto(contexto));
        post("/ob/api/tyc/empresa-producto", contexto -> OBTyc.insertEmpresaProductoTyc(contexto));

        // 18. RECAUDACIONES - DEBITO DIRECTO
        get("/ob/api/dd/convenios", contexto -> OBDebitoDirecto.consultaCovenios(contexto));
        post("/ob/api/dd/precarga", contexto -> OBDebitoDirecto.precargaDebitoDirecto(contexto));
        post("/ob/api/dd/cargar-dd", contexto -> {
            try {
                return OBDebitoDirecto.cargarDebitoDirecto(contexto);
            } catch (SQLException | IOException | ServletException e) {
                throw new RuntimeException(e);
            }
        });
        get("/ob/api/dd/descargar-template", contexto -> OBDebitoDirecto.descargaTemplate(contexto));
        post("/ob/api/dd/ver-tabla", contexto -> {
            try {
                return OBDebitoDirecto.verTablaDD(contexto);
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        });
        get("/ob/api/dd/detallesDD", contexto -> OBDebitoDirecto.verDetalles(contexto));
        get("/ob/api/dd/estados", contexto -> OBDebitoDirecto.consultaEstados(contexto));
        post("/ob/api/dd/historial-debito-directo", contexto -> OBDebitoDirecto.historialDebitoDirecto(contexto));
        get("/ob/api/descargar-rendiciones", contexto -> {
            try {
            	
                return OBDebitoDirecto.descargaRendiciones(contexto);
                
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });



        // 19. RECAUDACIONES - COBRANZA INTEGRAL
        get("/ob/api/ci/convenios", contexto -> OBCobranzaIntegral.consultaConveniosDetalleHabilitacion(contexto));
        post("/ob/api/ci/precarga", contexto -> OBCobranzaIntegral.precargaCobranzaIntegral(contexto));
        post("/ob/api/ci/carga", contexto -> {
            try {
                return OBCobranzaIntegral.cargaCobranzaIntegral(contexto);
            } catch (ServletException | IOException | SQLException e) {
                throw new RuntimeException(e);
            }
        });
        get("ob/api/ci/descargar-template", contextoOB -> OBDebitoDirecto.descargaTemplate(contextoOB));
        get("/ob/api/ci/productos", contexto -> OBCobranzaIntegral.consultaProductos(contexto));
        post("/ob/api/ci/historial-cobranza-integral", contexto -> OBCobranzaIntegral.historialCobranzaIntegral(contexto));
        post("/ob/api/ci/habilitacion-convenios", contexto -> OBCobranzaIntegral.habilitarDeshabilitarConvenios(contexto));
        get("/ob/api/ci/detallesCI", contexto -> OBCobranzaIntegral.verDetalles(contexto));
        post("ob/api/ci/ver-tabla", contexto -> OBCobranzaIntegral.verTablaCI(contexto));
        post("/ob/api/ci/reporte-cobranza-detalle", contexto -> OBCobranzaIntegral.reporteCobranzas(contexto));
        get("/ob/api/ci/estados", contexto -> OBCobranzaIntegral.consultaEstados(contexto));
        get("/ob/api/ci/tipoMovimiento", contexto -> OBCobranzaIntegral.tipoMovimiento(contexto));
        post("/ob/api/ci/descargarCobranzaIntegral", contexto -> {
            try {
                return OBCobranzaIntegral.descargarCobranzaIntegral(contexto);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        get("/ob/api/ci/convenios-todos", contexto -> OBCobranzaIntegral.consultaConveniosTodosDetalle(contexto));
        post("/ob/api/dl/carga-debin-por-lote", contexto -> {
            try {
                return OBCobranzaIntegral.cargaDebinLote(contexto);
            } catch (ServletException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        post("ob/api/dl/precarga",contexto -> OBCobranzaIntegral.precargaDebinLote(contexto));
        get("ob/api/dl/detalles",contexto->OBCobranzaIntegral.detallesDebinLote(contexto));
        post("/ob/api/dl/ver-tabla",contexto -> OBCobranzaIntegral.verTablaDL(contexto));

        // 20. ECHEQ
        get("ob/api/ec/cuentas-corriente", contexto -> OBEcheq.listadoCuentasCtes(contexto));
        post("ob/api/ec/razon-social", contexto -> OBEcheq.obtenerRazonSocial(contexto));
        post("ob/api/ec/guardar-beneficiario", contexto -> OBEcheq.guardarBeneficiario(contexto));
        get("ob/api/ec/obtener-beneficiarios", contexto -> OBEcheq.obtenerBeneficiarios(contexto));
        post("ob/api/ec/detalle-chequeras", contexto -> OBEcheq.chequerasDisponibles(contexto));
        post("ob/api/ec/solicitud-chequera", contexto -> OBEcheq.solicitudChequera(contexto));
        post("ob/api/ec/solicitud-emision-cheque", contexto -> OBEcheq.solicitarEmisionEcheq(contexto));
        post("ob/api/ec/listado-cheques", contexto -> OBEcheq.listadoCheques(contexto));
        post("ob/api/ec/detalles-cheque", contexto -> OBEcheq.detalleCheque(contexto));
        post("ob/api/ec/puedo-emitir-chequera", contexto -> OBEcheq.puedoEmitirchequera(contexto));
        post("ob/api/ec/admitir-cheque", contexto -> OBEcheq.admitir(contexto));
        post("ob/api/ec/repudiar-cheque", contexto -> OBEcheq.repudiar(contexto));
        post("ob/api/ec/anular-cheque", contexto -> OBEcheq.solicitarAnulacion(contexto));
        post("ob/api/ec/solicitar-devolucion-cheque", contexto -> OBEcheq.precargaDevolucionEcheq(contexto));
        post("ob/api/ec/solicitud-endoso", contexto -> OBEcheq.solicitudEndosoEcheq(contexto));
        post("ob/api/ec/validar-entidades-mercado", contexto -> OBEcheq.validarEntidadesMercado(contexto));
        post("ob/api/ec/solicitud-cesion-cheque", contexto -> OBEcheq.solicitudCesionEcheq(contexto));
        post("ob/api/ec/depositar-cheque", contexto -> OBEcheq.solicitudDepositoCheque(contexto));
        post("ob/api/ec/solicitar-aceptacion-devolucion", contexto -> OBEcheq.solicitarAceptacionDevolucionEcheq(contexto));
        post("ob/api/ec/historial", contexto -> OBEcheq.historialEcheq(contexto));
        post("ob/api/ec/detalles-bandeja", contexto -> OBEcheq.detallesBandeja(contexto));
        post("ob/api/ec/detalles-bandeja-chequera", contexto -> OBEcheq.detallesBandejaChequera(contexto));
        post("ob/api/ec/solicitud-custodiar-cheque", contexto -> OBEcheq.solicitudCustodiarEcheq(contexto));
        post("ob/api/ec/consulta-convenios-sugeridos-echeq", contexto -> OBEcheq.consultaConveniosSugeridosEcheq(contexto));
        post("ob/api/ec/solicitud-rescatar-cheque", contexto -> OBEcheq.solicitudRescatarEcheq(contexto));
        post("ob/api/ec/detalle-firmantes", contexto -> OBEcheq.detalleFirmantes(contexto));
        get("ob/api/ec/listado-cheques-a-descontar", contexto -> OBEcheq.listadoChequesADescontar(contexto));
        post("ob/api/ec/simular-descuento-factoring", contexto -> OBEcheq.simularDescuento(contexto));
        post("ob/api/ec/eliminar-solicitud-descuento-factoring", contexto -> OBEcheq.eliminarDescuentoFactoring(contexto));
        post("ob/api/ec/enviar-echeq-descuento-bandeja", contexto -> OBEcheq.bandejaDescuentoCheque(contexto));
        post("ob/api/ec/detalles-descuento-bandeja", contexto -> OBEcheq.detallesDescuentoBandeja(contexto));

        post("ob/api/ec/solicitar-aval",contexto-> OBEcheq.precargaSolicitudAval(contexto,new ServicioEcheqOB(contexto)));
        post("ob/api/ec/admitir-aval",contexto->OBEcheq.solicitarAdmisionAvalEcheq(contexto,new ServicioEcheqOB(contexto)));
        post("ob/api/ec/solicitud-mandato",contexto->OBEcheq.solicitudMandatoEcheq(contexto));
        post("ob/api/ec/solicitar-aceptacion-mandato",contexto->OBEcheq.solicitudAceptacionMandato(contexto));
        post("ob/api/ec/razon-social-imf",contexto -> OBEcheq.obtenerRazonSocialIMF(contexto));
        post("ob/api/ec/revocar-mandato-negociacion",contexto->OBEcheq.solicitudRevocacionMandatoNegociacionEcheq(contexto));
        get("/ob/api/ec/horarioDescuento", contexto -> OBEcheq.horarioDescuento(contexto));
        get("/ob/api/ec/descuento-habilitado", contexto -> OBEcheq.descuentoHabilitado(contexto));

        // 21. DEBIN
        post("/ob/api/db/validarCbuAlias", contexto -> OBDebin.validarInfoDebito(contexto));
        get("/ob/api/db/listar-cuentas", contexto -> OBDebin.listarCuentas(contexto));
        post("/ob/api/db/solicitar-debin", contexto -> OBDebin.solicitarDebin(contexto));
        post("/ob/api/db/habilitar-cuenta", contexto -> OBDebin.habilitarCuenta(contexto));
        delete("/ob/api/db/deshabilitar-cuenta", contexto -> OBDebin.deshabilitarCuenta(contexto));
        post("/ob/api/db/rechazar", contexto -> OBDebin.rechazarDebin(contexto));
        get("/ob/api/db/ver-detalles", contexto -> OBDebin.verDetalles(contexto));
        get("/ob/api/db/vencimientos", contexto -> OBDebin.vencimientos(contexto));
        get("ob/api/db/conceptos",contexto->OBDebin.listadoConceptos(contexto));
        post("/ob/api/db/listar-recibidos-generados", contexto -> OBDebin.listarRecibidosGenerados(contexto));
        post("/ob/api/db/listar-recibidos-programados", contexto -> OBDebin.listarRecibidosGeneradosProgramados(contexto));
        post("/ob/api/db/baja-debin-programado", contexto -> OBDebin.bajaDebinProgramado(contexto));
        post("/ob/api/db/aceptar", contexto -> OBDebin.aceptarDebin(contexto));
        post("/ob/api/db/aceptarDebinProgramado", contexto -> OBDebin.aceptarDebinProgramado(contexto));
        post("/ob/api/db/rechazarDebinProgramado", contexto -> OBDebin.rechazarDebinProgramado(contexto));
        post("/ob/api/db/tiene-fondos",contexto ->OBDebin.tieneFondosDebin(contexto));

		// 21. TARJETAS
        post("/ob/api/consolidada-tarjetas-debito", contexto -> OBTarjetas.consolidadaTarjetasDebito(contexto));
        get("/ob/api/tracking-tarjeta-debito", contexto -> OBDelivery.agregarTrackeoTarjetaDebito(contexto));
        put("/ob/api/habilitar-tarjeta-debito", contexto -> OBTarjetas.habilitarTarjetaDebito(contexto));
        post("/ob/api/limites-tarjeta-debito", contexto -> OBTarjetas.limitesTarjetaDebito(contexto));
        put("/ob/api/modificar-limite-tarjeta-debito", contexto -> OBTarjetas.modificarLimiteTarjetaDebito(contexto));
        put("/ob/api/blanquear-pin-tarjeta-debito", contexto -> OBTarjetas.blanquearPin(contexto));
        
        //22. TARJETA CREDITO - PRISMA
        get("/ob/api/tarjetas/descarga-eresumen-pdf", contexto -> OBTarjetaEmpresa.descargaResumenPDF(contexto), ROL_IDENTIFICADO);
        get("/ob/api/tarjetas/tarjetascredito", contexto -> OBTarjetaEmpresa.obtenerTarjetasCredito(contexto), ROL_IDENTIFICADO);
        get("/ob/api/tarjetas/listar-cuentas", contexto -> OBTarjetaEmpresa.obtenerCuentas(contexto), ROL_IDENTIFICADO);
        get("/ob/api/tarjetas/vencimientos", contexto -> OBTarjetaEmpresa.obtenerVencimientos(contexto), ROL_IDENTIFICADO);
        get("/ob/api/tarjetas/listar-tarjetas", contexto -> OBTarjetaEmpresa.obtenerListadoTarjetas(contexto), ROL_IDENTIFICADO);
        post("/ob/api/tarjetas/transacciones", contexto -> OBTarjetaEmpresa.obtenerTransacciones(contexto), ROL_IDENTIFICADO);
        get("/ob/api/tarjetas/datos-para-pagar", contexto -> OBTarjetaEmpresa.obtenerDatosPago(contexto), ROL_IDENTIFICADO);
        post("ob/api/tarjetas/enviar-pago-tarjeta-bandeja", contexto -> OBTarjetaEmpresa.bandejaPagoTarjeta(contexto));
        post("ob/api/tarjetas/detalles-bandeja-pagos", contexto -> OBTarjetaEmpresa.detallesPagoTarjetaBandeja(contexto)); // Detalle en bandeja con un id
        get("/ob/api/tarjetas/pago-habilitado", contexto -> OBTarjetaEmpresa.pagoHabilitado(contexto));
        post("/ob/api/tarjetas/stopdebit", contexto -> OBTarjetaEmpresa.postStopDebit(contexto));
        post("ob/api/tarjetas/enviar-stopdebit-bandeja", contexto -> OBTarjetaEmpresa.bandejaStopDebit(contexto));


        //23. COMEX
        get("ob/api/cx/categorias", contexto -> OBComex.categorias(contexto));
        get("ob/api/cx/conceptos", contexto -> OBComex.conceptos(contexto));
        post("ob/api/cx/historial-op", contexto -> OBComex.historialOrdenesDePago(contexto));
        post("ob/api/cx/validar-relacion-porcentual", contexto -> OBComex.ValidarRelacionPorcentual(contexto));
        post("ob/api/cx/cargar-op", contexto -> OBComex.CargarOrdenDePago(contexto));
        post("ob/api/cx/detalle", contexto -> OBComex.detalle(contexto));
        post("ob/api/cx/cuentas", contexto -> OBComex.cuentas(contexto));
        get("ob/api/cx/validar-trr", contexto -> OBComex.validarTTRYConcepto(contexto));
        get("ob/api/cx/descargar-archivo", contexto -> OBComex.descargarArchivo(contexto));
        post("ob/api/cx/rectificar-op",contexto->OBComex.rectificarOrdenPago(contexto));
        delete("ob/api/cx/eliminar-archivo",contexto -> OBComex.eliminarArchivo(contexto));
        post("ob/api/cx/editar-op",contexto-> OBComex.editarOrdenPago(contexto));

        //24 Textos
        get("ob/api/ob-textos", contexto -> OBTextos.obtenerTextos(contexto));

        //PRESTAMOS
        get("/ob/api/prestamos/obtener-prestamo", contexto -> OBPrestamos.obtenerPrestamo(contexto), ROL_IDENTIFICADO);
        get("/ob/api/prestamos/obtener-posicion-consolidada", contexto -> OBPrestamos.obtenerPosicionConsolidada(contexto), ROL_IDENTIFICADO);
        get("/ob/api/prestamos/cuotas", contexto -> OBPrestamos.obtenercuotas(contexto), ROL_IDENTIFICADO);
        get("/ob/api/prestamos/cuotas-por-fecha", contexto -> OBPrestamos.obtenercuotasPorFecha(contexto), ROL_IDENTIFICADO);
        post("/ob/api/prestamos/generar-comprobante", contexto -> OBPrestamos.generarComprobantePrestamoPDF(contexto), ROL_IDENTIFICADO);
        get("ob/api/prestamos/listado-pagos", contexto -> OBPrestamos.obtenerlistadoPagosRealizados(contexto), ROL_IDENTIFICADO);
    }    
}

