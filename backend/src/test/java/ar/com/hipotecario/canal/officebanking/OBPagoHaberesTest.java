package ar.com.hipotecario.canal.officebanking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.servicio.api.recaudaciones.ApiRecaudaciones;
import ar.com.hipotecario.backend.servicio.api.recaudaciones.ConveniosOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;

public class OBPagoHaberesTest {

	@Mock
	private ApiRecaudaciones apiRecaudaciones;
	@Mock
	private List<ConveniosOB> convenios;
	@Mock
	private UsuarioOB usuarioOB;
	@Mock
	private OBCuentas obCuentas;
	@Mock
	private OBPagoHaberes obPagoHaberes;

	@Test
	void testConsultaConvenios() {

		// Configuración de Mockito
		MockitoAnnotations.openMocks(this);

		// Datos de prueba
		ContextoOB contextoOB = new ContextoOB("OB", Config.ambiente(), "1");
		contextoOB.sesion().usuarioOB = usuarioOB;
		// contextoOB.sesion().empresaOB = empresaOB;
		contextoOB.parametros.set("empresa", 30612929455L);
		contextoOB.parametros.set("funcionalidadOB", 4);
		contextoOB.parametros.set("firmante", 20301240040L);

		// Convenios convenios = new Convenios(Arrays.asList(/* proporciona algunos
		// convenios de prueba */));
		// Objeto cuenta = new Objeto(/* proporciona los datos de una cuenta de prueba
		// */);

		// Configuración de comportamiento de Mockito
		// when(apiRecaudaciones.convenios(contextoOB, "cuit",
		// "operacion")).thenReturn(() -> convenios);
		// when(obCuentas.cuenta(contextoOB, "cuenta")).thenReturn(obCuentas);

		// Ejecutar la función que estamos probando

		Object resultado = OBPagoHaberes.consultaCovenios(contextoOB);
		Object datos = new Object();
		// Verificar los resultados esperados
		// Aquí deberías verificar el resultado según la lógica de tu aplicación y las
		// expectativas
		// por ejemplo, asumamos que el resultado es un Map<String, List<Objeto>>
		assertEquals(datos, resultado);
	}
}
