package ar.com.hipotecario.backend.servicio.api.transmit;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.exception.TransmitException;
import ar.com.hipotecario.backend.util.MapperUtil;
import ar.com.hipotecario.canal.libreriasecurity.application.service.HomeBankingMinoristaUseCaseService;
import ar.com.hipotecario.canal.libreriasecurity.domain.models.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Optional;

public class LibreriaFraudes extends ApiObjeto {

    private static final String FORMATO_LIBRERIA = "yyyy-MM-dd";
    private static final Logger log = LoggerFactory.getLogger(LibreriaFraudes.class);
    private static Optional<GenericResponse<?>> response;

    /* ========== RESPONSE ========== */
    public static class UsuarioLibreriaResponse extends ApiObjeto {
        public String errorCode;
        public String errorMessage;
        public String result;
        public DatosAdicionalesLibreria addicionalData;

        public boolean esError() {
            return !errorCode.equals("0");
        }

        public boolean esOk() {
            return errorCode.equals("0");
        }

        public UsuarioLibreriaResponse() {
        }

        public UsuarioLibreriaResponse(String errorCode) {
            this.errorCode = errorCode;
        }

        public UsuarioLibreriaResponse(String errorCode, String errorMessage, String result, DatosAdicionalesLibreria addicionalData) {
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            this.result = result;
            this.addicionalData = addicionalData;
        }
    }

    public static class DatosAdicionalesLibreria extends ApiObjeto {
        public String idOper;
        public String dni;
        public String expiation;
        public String csmIdAuth;
    }

    public static class DatosAdicionales extends DatosAdicionalesLibreria {
        public String userId;
        public String message;
    }

    public static class DatosAdicionalesCsm extends DatosAdicionalesLibreria {
    }

    /* ========== REQUEST ========== */
    public static class UsuarioLibreriaRequest extends ApiObjeto {

        public String email;
        public String contrasena;
        public String usuario;
        public String dni;
        public String telefono;
        public String canal;
        public String subCanal;
        public String cuit;
        public String ip;
        public JourneyTransmitEnum journey;
        public String sesion;
        public String idCobis;
        public String csmId;
        public String checksum;
        private Date fechaModificacionUsuario;
        private Date fechaModificacionContrasena;
        private Date fechaModificacionTelefono;
        private Date fechaModificacionEmail;
        private boolean biometria;

        public UsuarioLibreriaRequest() {
        }

        public UsuarioLibreriaRequest(String email, String contrasena, String usuario, String dni, String telefono,
                                      String canal, String subCanal, String cuit, String ip, JourneyTransmitEnum journey, String sesion,
                                      String idCobis, String csmId, String checksum, Date fechaModificacionUsuario, Date fechaModificacionContrasena,
                                      Date fechaModificacionTelefono, Date fechaModificacionEmail, boolean biometria) {
            this.email = email;
            this.contrasena = contrasena;
            this.usuario = usuario;
            this.dni = dni;
            this.telefono = telefono;
            this.canal = canal;
            this.subCanal = subCanal;
            this.cuit = cuit;
            this.ip = ip;
            this.journey = journey;
            this.sesion = sesion;
            this.idCobis = idCobis;
            this.csmId = csmId;
            this.checksum = checksum;
            this.fechaModificacionContrasena = fechaModificacionContrasena;
            this.fechaModificacionEmail = fechaModificacionEmail;
            this.fechaModificacionTelefono = fechaModificacionTelefono;
            this.fechaModificacionUsuario = fechaModificacionUsuario;
            this.biometria = biometria;
        }

        public UsuarioLibreriaRequest(String email, String contrasena, String usuario, String dni, String telefono,
                                      String canal, String subCanal, String cuit, String ip, JourneyTransmitEnum journey, String sesion,
                                      String idCobis, String csmId, String checksum, boolean biometria) {
            this.email = email;
            this.contrasena = contrasena;
            this.usuario = usuario;
            this.dni = dni;
            this.telefono = telefono;
            this.canal = canal;
            this.subCanal = subCanal;
            this.cuit = cuit;
            this.ip = ip;
            this.journey = journey;
            this.sesion = sesion;
            this.idCobis = idCobis;
            this.csmId = csmId;
            this.checksum = checksum;
            this.fechaModificacionContrasena = new Date();
            this.fechaModificacionEmail = new Date();
            this.fechaModificacionTelefono = new Date();
            this.fechaModificacionUsuario = new Date();
            this.biometria = biometria;
        }

    }

    private static UsuarioLibreriaResponse generateResponse(Contexto contexto, Class<? extends DatosAdicionalesLibreria> targetClass) {
        if (!contexto.esProduccion())
            log.info("OptionalResponse: {}", response.toString());
        if (response != null && response.isPresent()) {
            GenericResponse<?> genericResponse = response.get();
            if (!contexto.esProduccion())
                log.info("genericResponse: {}", genericResponse);
            if (!genericResponse.isCodeErrorEqualCero()) {
                if (genericResponse.getErrorCode().equals("405")) {
                    if (genericResponse.getErrorMessage().equalsIgnoreCase("Usuario ya Existe"))
                        return new UsuarioLibreriaResponse(ConstantesTransmit.CODIGO_ERROR_USUARIO_YA_EXISTE);
                    return new UsuarioLibreriaResponse(genericResponse.getErrorCode());
                }
                return new UsuarioLibreriaResponse(ConstantesTransmit.CODIGO_ERROR_IS_NOT_PRESENT);
            }
            return new UsuarioLibreriaResponse(
                    genericResponse.getErrorCode(),
                    genericResponse.getErrorMessage(),
                    genericResponse.getResult(),
                    genericResponse.getAddicionalData() != null
                            ? MapperUtil.mapToObject(genericResponse.getAddicionalData(), targetClass)
                            : null);
        }
        return new UsuarioLibreriaResponse(ConstantesTransmit.CODIGO_ERROR_IS_NOT_PRESENT);
    }

    private static BodyForMigrationMin crearBodyForMigration(UsuarioLibreriaRequest usuarioLibreriaRequest) {
        return new BodyForMigrationMin(
                usuarioLibreriaRequest.email,
                usuarioLibreriaRequest.contrasena,
                usuarioLibreriaRequest.biometria,
                usuarioLibreriaRequest.usuario,
                usuarioLibreriaRequest.dni,
                usuarioLibreriaRequest.telefono,
                Util.date(usuarioLibreriaRequest.fechaModificacionUsuario, FORMATO_LIBRERIA),
                Util.date(usuarioLibreriaRequest.fechaModificacionContrasena, FORMATO_LIBRERIA),
                Util.date(usuarioLibreriaRequest.fechaModificacionTelefono, FORMATO_LIBRERIA),
                Util.date(usuarioLibreriaRequest.fechaModificacionEmail, FORMATO_LIBRERIA));
    }

    private static BodyForCsm crearBodyforCsm(UsuarioLibreriaRequest usuarioLibreriaRequest) {
        return new BodyForCsm(usuarioLibreriaRequest.checksum, usuarioLibreriaRequest.csmId);
    }

    private static HbMinoRequest crearRequestLibreria(Contexto contexto, UsuarioLibreriaRequest usuarioLibreriaRequest, Object bodyRequesForBuild) {
        Context context = new Context(
                usuarioLibreriaRequest.canal,
                usuarioLibreriaRequest.subCanal,
                usuarioLibreriaRequest.idCobis,
                Util.idProcesoLibreria(),
                usuarioLibreriaRequest.sesion,
                usuarioLibreriaRequest.journey.descripcion,
                contexto.config.string(ConstantesTransmit.API_AUDITOR),
                usuarioLibreriaRequest.ip);

        DataBaseCredentials dataBaseCredentials = new DataBaseCredentials(
                contexto.config.string(ConstantesTransmit.DRS_DB_URL),
                contexto.config.string(ConstantesTransmit.DRS_DB_USR).concat(ConstantesTransmit.BH_COM_AR),
                contexto.config.string(ConstantesTransmit.DRS_DB_PASS));

        MosaicCredentials mosaicCredentials = new MosaicCredentials(
                contexto.config.string(ConstantesTransmit.SECURITY_SECRET_ID),
                contexto.config.string(ConstantesTransmit.SECURITY_CLIENT_ID));

        return new HbMinoRequest(
                mosaicCredentials,
                dataBaseCredentials,
                context,
                bodyRequesForBuild);
    }

    public static UsuarioLibreriaResponse migrarUsuarioHB(Contexto contexto, UsuarioLibreriaRequest usuarioLibreriaRequest) throws TransmitException {
        HbMinoRequest userForMigration = crearRequestLibreria(contexto, usuarioLibreriaRequest, crearBodyForMigration(usuarioLibreriaRequest));
        loguearInfo(contexto, userForMigration);
        response = new HomeBankingMinoristaUseCaseService().userMigration(userForMigration);
        return generateResponse(contexto, DatosAdicionales.class);
    }

    public static UsuarioLibreriaResponse validarCsmHB(Contexto contexto, UsuarioLibreriaRequest usuarioLibreriaRequest) throws TransmitException {
        HbMinoRequest hbMinoRequest = crearRequestLibreria(contexto, usuarioLibreriaRequest, crearBodyforCsm(usuarioLibreriaRequest));
        loguearInfo(contexto, hbMinoRequest);
        response = new HomeBankingMinoristaUseCaseService().validTokenCsm(hbMinoRequest);
        return generateResponse(contexto, DatosAdicionalesCsm.class);
    }

    public static UsuarioLibreriaResponse migrarUsuarioMB(Contexto contexto, UsuarioLibreriaRequest usuarioLibreriaRequest) throws TransmitException {
        HbMinoRequest userForMigration = crearRequestLibreria(contexto, usuarioLibreriaRequest, crearBodyForMigration(usuarioLibreriaRequest));
        loguearInfo(contexto, userForMigration);
        response = new HomeBankingMinoristaUseCaseService().userMigration(userForMigration);
        return generateResponse(contexto, DatosAdicionales.class);
    }

    public static UsuarioLibreriaResponse validarCsmMB(Contexto contexto, UsuarioLibreriaRequest usuarioLibreriaRequest) throws TransmitException {
        HbMinoRequest hbMinoRequest = crearRequestLibreria(contexto, usuarioLibreriaRequest, crearBodyforCsm(usuarioLibreriaRequest));
        loguearInfo(contexto, hbMinoRequest);
        response = new HomeBankingMinoristaUseCaseService().validTokenCsm(hbMinoRequest);
        return generateResponse(contexto, DatosAdicionalesCsm.class);
    }

    private static void loguearInfo(Contexto contexto, Object objeto) {
        if (!contexto.esProduccion())
            log.info("{}: {}", objeto.getClass().getName(), objeto);
    }
}