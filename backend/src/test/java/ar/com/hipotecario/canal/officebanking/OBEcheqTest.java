package ar.com.hipotecario.canal.officebanking;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.HttpResponse;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cheques.ApiCheques;
import ar.com.hipotecario.backend.servicio.api.cheques.AvalEcheqOB;
import ar.com.hipotecario.backend.servicio.api.cheques.ListadoChequesOB;
import ar.com.hipotecario.backend.servicio.api.cheques.RepudioAvalEcheqOB;
import ar.com.hipotecario.canal.officebanking.enums.echeq.EnumAccionesEcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EcheqOB;

import com.github.underscore.U;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


import static ar.com.hipotecario.canal.officebanking.utils.OBEcheqTestUtils.getListadoChequesOBCargado1Cheque;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OBEcheqTest {

    ContextoOB contexto;
    ServicioEcheqOB servicioEcheqOB;

    @BeforeEach
    void setup(){
        contexto = new ContextoOB("OB", Config.ambiente(), "1");
        EmpresaOB empresaOB = new EmpresaOB();
        empresaOB.cuit = 3l;
        UsuarioOB usuarioOB = new UsuarioOB();
        contexto.sesion().empresaOB = empresaOB;
        contexto.sesion().usuarioOB = usuarioOB;
        servicioEcheqOB = mock(ServicioEcheqOB.class);
    }

    @Nested
    class repudiar{
        @Nested
        class repudiarAval{
            @Test
            void repudiar_aval_test() {
                ListadoChequesOB listado = getListadoChequesOBCargado1Cheque();
                listado.result.cheques.get(0).estado ="AVAL-PENDIENTE";


                contexto.parametros.set("idCheque","1");
                contexto.parametros.set("motivoRepudio","test");

                RepudioAvalEcheqOB repudioAval = new RepudioAvalEcheqOB();
                repudioAval.result = new RepudioAvalEcheqOB().new result();
                try (MockedStatic<ApiCheques> mockedStatic = Mockito.mockStatic(ApiCheques.class)) {
                    mockedStatic.when(() -> ApiCheques.getChequeById(contexto,"1", "3")).thenReturn(new Futuro<>(()->listado));
                    mockedStatic.when(()->ApiCheques.repudiarAvalEcheq(contexto,"1","3","test")).thenReturn(new Futuro<>(()->repudioAval));
                    Objeto respuesta = (Objeto) OBEcheq.repudiar(contexto);
                    assertNotNull(respuesta);
                    assertEquals("0",respuesta.get("estado"));

                }
            }
            @Test
            void repudiar_aval_test_error_api() {
                ListadoChequesOB listado = getListadoChequesOBCargado1Cheque();
                listado.result.cheques.get(0).estado ="AVAL-PENDIENTE";
                contexto.parametros.set("idCheque","1");
                contexto.parametros.set("motivoRepudio","test");

                RepudioAvalEcheqOB repudioAval = new RepudioAvalEcheqOB();
                repudioAval.result = new RepudioAvalEcheqOB().new result();
                ApiRequest apiRequest = new ApiRequest("","","","",contexto);
                HttpResponse httpResponse = new HttpResponse();
                ApiResponse apiResponse = new ApiResponse(apiRequest, httpResponse);
                apiResponse.set("mensajeAlUsuario","Error API");
                ApiException apiException = new ApiException(apiRequest,apiResponse);
                try (MockedStatic<ApiCheques> mockedStatic = Mockito.mockStatic(ApiCheques.class)) {
                    mockedStatic.when(() -> ApiCheques.getChequeById(contexto,"1", "3")).thenReturn(new Futuro<>(()->listado));
                    mockedStatic.when(()->ApiCheques.repudiarAvalEcheq(contexto,"1","3","test")).thenThrow(apiException);
                    Objeto respuesta = (Objeto) OBEcheq.repudiar(contexto);
                    assertNotNull(respuesta);
                    assertTrue((boolean) respuesta.get("ERROR"));
                    assertEquals("Error API",respuesta.get("datos"));

                }
            }
        }
    }


    @Nested
    class solicitarAval{
        @Test
        void avalarEcheq_test() {
            contexto.parametros.set("idOperacion","123");
            ListadoChequesOB listado = getListadoChequesOBCargado1Cheque();
            ListadoChequesOB.cheques cheque = listado.result.cheques.get(0);

            EcheqOB echeq = new EcheqOB();
            echeq.id = 1;
            echeq.idCheque = "1";
            echeq.documentoBeneficiario = "123";
            echeq.cesionarioDomicilio = "Calle falsa 123";
            echeq.razonSocialBeneficiario = "test";

            AvalEcheqOB aval = new AvalEcheqOB();
            try(MockedStatic<ServicioEcheqOB> servicioMocked = Mockito.mockStatic(ServicioEcheqOB.class)){
                servicioMocked.when(()->ServicioEcheqOB.find(123)).thenReturn(new Futuro<>(()->echeq));

                try (MockedStatic<ApiCheques> mockedStatic = Mockito.mockStatic(ApiCheques.class)) {
                    mockedStatic.when(() -> ApiCheques.getChequeById(contexto,"1", "3")).thenReturn(new Futuro<>(()->listado));
                    mockedStatic.when(()->ApiCheques.avalarCheque(contexto,echeq.documentoBeneficiario,echeq.cesionarioDomicilio,echeq.razonSocialBeneficiario,cheque)).thenReturn(new Futuro<>(()->aval));
                    Objeto respuesta = OBEcheq.avalarEcheq(contexto,servicioEcheqOB);
                    assertEquals("0",respuesta.get("estado"));
                }
            }

        }

        @Test
        void avalarEcheq_test_cheque_no_encontrado() {
            contexto.parametros.set("idOperacion","123");
            ListadoChequesOB listado = new ListadoChequesOB();
            listado.result = listado.new result();

            listado.result.cheques = new ArrayList<>();

            EcheqOB echeq = new EcheqOB();
            echeq.id = 1;
            echeq.idCheque = "1";
            echeq.documentoBeneficiario = "123";
            echeq.cesionarioDomicilio = "Calle falsa 123";
            echeq.razonSocialBeneficiario = "test";

            try(MockedStatic<ServicioEcheqOB> servicioMocked = Mockito.mockStatic(ServicioEcheqOB.class)){
                servicioMocked.when(()->ServicioEcheqOB.find(123)).thenReturn(new Futuro<>(()->echeq));

                try (MockedStatic<ApiCheques> mockedStatic = Mockito.mockStatic(ApiCheques.class)) {
                    mockedStatic.when(() -> ApiCheques.getChequeById(contexto,"1", "3")).thenReturn(new Futuro<>(()->listado));
                    Objeto respuesta = OBEcheq.avalarEcheq(contexto,servicioEcheqOB);
                    assertEquals("DATOS_INVALIDOS",respuesta.get("estado"));
                }
            }

        }
    }

    @Nested
    class precargaSolicitudAval{
        @Test
        void precargaSolicitudAval_test() {
            contexto.parametros.set("cuitAvalista","1");
            contexto.parametros.set("nombreAvalista","Juan Perez");
            contexto.parametros.set("domicilioAvalista","Calle falsa 123");
            contexto.parametros.set("idCheque","1");

            ListadoChequesOB listado = new ListadoChequesOB();
            listado.result = listado.new result();
            ListadoChequesOB.cheques cheque = listado.new cheques();
            cheque.cuenta_emisora = listado.new cuenta_emisora();

            cheque.cheque_id = "cheque123";
            cheque.numero_chequera = "numero123";
            cheque.monto = 1000.00;
            cheque.cheque_modo = "0";
            cheque.cheque_caracter = "a la orden";
            cheque.cheque_motivo_pago = "Pago de servicios";
            cheque.cheque_concepto = "Concepto de prueba";
            cheque.fecha_pago = "2023-10-28T12:00:00";
            cheque.cuenta_emisora.emisor_cuit = "2";
            cheque.cheque_tipo = "TipoCheque";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
            LocalDateTime fechaPago = LocalDateTime.parse(cheque.fecha_pago, formatter);
            listado.result.cheques = new ArrayList<>();
            listado.result.cheques.add(cheque);


            EcheqOB echeq = new EcheqOB();
            echeq.id = 1;

            try (MockedStatic<ApiCheques> mockedStatic = Mockito.mockStatic(ApiCheques.class)) {
                mockedStatic.when(() -> ApiCheques.getChequeById(contexto,"1", "3"))
                        .thenReturn(new Futuro<>(()->listado));
                when(servicioEcheqOB.cargarCreado(cheque.numero_chequera,BigDecimal.valueOf(cheque.monto),"1","cuit",null,true,cheque.cheque_motivo_pago,cheque.cheque_concepto,true,null,"Juan Perez",contexto.sesion().empresaOB, fechaPago,cheque.cheque_tipo, EnumAccionesEcheqOB.SOLICITAR_AVAL,cheque.cuenta_emisora.emisor_cuit,cheque.cheque_id,cheque.cheque_numero,null,null,"Calle falsa 123",null,contexto.sesion().usuarioOB)).thenReturn(new Futuro<>(()->echeq));
                Objeto respuesta = OBEcheq.precargaSolicitudAval(contexto,servicioEcheqOB);

                assertEquals(1,((Objeto)respuesta.get("datos")).get("idOperacion"));
                verify(servicioEcheqOB,times(1)).cargarCreado(cheque.numero_chequera,BigDecimal.valueOf(cheque.monto),"1","cuit",null,true,cheque.cheque_motivo_pago,cheque.cheque_concepto,true,null,"Juan Perez",contexto.sesion().empresaOB, fechaPago,cheque.cheque_tipo, EnumAccionesEcheqOB.SOLICITAR_AVAL,cheque.cuenta_emisora.emisor_cuit,cheque.cheque_id,cheque.cheque_numero,null,null,"Calle falsa 123",null,contexto.sesion().usuarioOB);
            }

        }

        @Test
        void precargaSolicitudAval_test_cheque_no_encontrado(){
            contexto.parametros.set("cuitAvalista","1");
            contexto.parametros.set("nombreAvalista","Juan Perez");
            contexto.parametros.set("domicilioAvalista","Calle falsa 123");
            contexto.parametros.set("idCheque","1");

            ListadoChequesOB listado = new ListadoChequesOB();
            listado.result = listado.new result();
            ListadoChequesOB.cheques cheque = listado.new cheques();
            listado.result.cheques = new ArrayList<>();

            try (MockedStatic<ApiCheques> mockedStatic = Mockito.mockStatic(ApiCheques.class)) {
                mockedStatic.when(() -> ApiCheques.getChequeById(contexto,"1", "3"))
                        .thenReturn(new Futuro<>(()->listado));
                Objeto respuesta = OBEcheq.precargaSolicitudAval(contexto,servicioEcheqOB);

                assertTrue(respuesta.toString().contains("No se encontraron cheques para esta consulta."));
            }
        }
        @Test
        void precargaSolicitudAval_test_error_api(){
            contexto.parametros.set("cuitAvalista","1");
            contexto.parametros.set("nombreAvalista","Juan Perez");
            contexto.parametros.set("domicilioAvalista","Calle falsa 123");
            contexto.parametros.set("idCheque","1");

            ListadoChequesOB listado = new ListadoChequesOB();
            listado.result = listado.new result();
            listado.result.cheques = new ArrayList<>();

            try (MockedStatic<ApiCheques> mockedStatic = Mockito.mockStatic(ApiCheques.class)) {
                mockedStatic.when(() -> ApiCheques.getChequeById(contexto,"1", "3"))
                        .thenThrow(new RuntimeException("error"));
                Objeto respuesta = OBEcheq.precargaSolicitudAval(contexto,servicioEcheqOB);

                assertTrue(respuesta.toString().contains("No se encontraron cheques para esta consulta."));
            }
        }
    }



}