package ar.com.hipotecario.canal.officebanking;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import ar.com.hipotecario.canal.officebanking.jpa.ob.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.ArchivosComexOB;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.CategoriaComexOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.ConceptoComexOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.OrdenPagoComexOB;

public class OBComexTest extends Modulo {

	public static final ContextoOB CONTEXTO_OB = new ContextoOB("OB", Config.ambiente(), "1");
	@Mock
    private ServicioConceptoComexOB servicioConcepto;
    @Mock
    private ServicioCategoriaComexOB servicioCategoria;
    @Mock
    private ServicioOPComexOB servicioOPComexOB;
	@Mock
	private ServicioArchivosComexOB servicioArchivosComexOB;
    @Mock
    private CategoriaComexOB categoria;
    @Mock
    private ConceptoComexOB concepto;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        CONTEXTO_OB.parametros.set("idCategoria", 1);
    }
            
	@Test
	void categoriasMokito() {
		try (
				MockedConstruction<ServicioCategoriaComexOB> mockConstruction = Mockito.mockConstruction(ServicioCategoriaComexOB.class,
						(mock, context) -> {
							when(mock.findAll()).thenReturn(new Futuro<List<CategoriaComexOB>>(() -> DataProviderComex.categoriaComexListMock()));
						})) {
			Objeto respuesta = (Objeto) OBComex.categorias(CONTEXTO_OB);
			assertAll(
					()-> assertNotNull(respuesta, () -> "sin datos"),
					()-> assertEquals("Categoria Bienes", ((Objeto) respuesta.get("categorias")).objetos().get(0).get("descripcion")),
					()-> assertEquals(2,((Objeto) respuesta.get("categorias")).objetos().size()));
		}
	}
	    
    @Test
    public void conceptosMokito() {	
    	String expectedMessage = "USD";
    	
    	try (MockedConstruction<ServicioConceptoComexOB> mockConstruction = Mockito.mockConstruction(ServicioConceptoComexOB.class)) {
    		when(servicioCategoria.find(DataProviderComex.categoriaComexMock().id)).thenReturn(new Futuro<CategoriaComexOB>(() -> DataProviderComex.categoriaComexMock()));
            when(servicioConcepto.findByCategoria(categoria)).thenReturn(new Futuro<List<ConceptoComexOB>>(() -> DataProviderComex.conceptosComexListMock()));

            Futuro<List<ConceptoComexOB>> futuroResultado = servicioConcepto.findByCategoria(categoria);
            List<ConceptoComexOB> lstConceptos = futuroResultado.tryGet();
   
            assertAll(
					()-> assertNotNull(lstConceptos),
					()-> assertEquals(lstConceptos.get(0).descripcion, "Cobro de exportaciones"),
					()-> assertTrue(lstConceptos.get(0).moneda.simbolo.contains(expectedMessage)));    
        }
    }
    
    @Test
    public void historialOrdenesDePagoMokito() throws Exception { 
    	String nroTRR = "TRR00030167558";
    	String expectedMessage = "PENDIENTE FIRMA";
    	EmpresaOB empresa = DataProviderComex.empresa();
    	    	
    	try (MockedConstruction<ServicioOPComexOB> mockConstruction = Mockito.mockConstruction(ServicioOPComexOB.class)) {
    		when(servicioOPComexOB.filtrarOrdenesPagosHistorial(empresa, null, null, null, true))
            .thenReturn(new Futuro<List<OrdenPagoComexOB>>(() -> DataProviderComex.historialComexListMock()));

    		Futuro<List<OrdenPagoComexOB>> futuroResultado = servicioOPComexOB.filtrarOrdenesPagosHistorial(empresa, null, null, null, true);
    		List<OrdenPagoComexOB> resultado = futuroResultado.get();
    		
            assertAll(
					()-> assertNotNull(resultado),
					()-> assertEquals(nroTRR, resultado.get(1).numeroTRR),
					()-> assertTrue(resultado.get(0).estadoBandeja.descripcion.contains(expectedMessage)));            
        }
    }
    
    @Test
    public void detalleMokito() {
    	Integer idOrdenPago = 1334;
    	String nroTRR = "TRR00024035043";
    	String expectedMessage = "EXITO";
    	
    	ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(CONTEXTO_OB);
    	BandejaOB bandeja = DataProviderComex.bandeja(idOrdenPago);
    	OrdenPagoComexOB ordenPago = DataProviderComex.ordenPago(idOrdenPago, nroTRR, bandeja);
		
    	try (MockedConstruction<ServicioOPComexOB> mockConstruction = Mockito.mockConstruction(ServicioOPComexOB.class)) {
            when(servicioBandeja.find(idOrdenPago)).thenReturn(new Futuro<BandejaOB>(() -> bandeja));
            when(servicioOPComexOB.find(idOrdenPago)).thenReturn(new Futuro<OrdenPagoComexOB>(() -> ordenPago));

            Futuro<OrdenPagoComexOB> futuroResultado = servicioOPComexOB.find(idOrdenPago);
            OrdenPagoComexOB resultado = futuroResultado.get();
            
            assertAll(
					()-> assertNotNull(resultado),
					()-> assertEquals(nroTRR, resultado.numeroTRR),
					()-> assertTrue(resultado.estado.descripcion.contains(expectedMessage)));            
        }
    }
        
	@Test
	void TextoOB_Buscar_Por_IDfront_Test() {
	    try (
	        MockedConstruction<ServicioTextosOB> mockConstruction = Mockito.mockConstruction(
	            ServicioTextosOB.class,
	            (mock, context) -> {
	                when(mock.find("id_001"))
	                    .thenReturn(new Futuro<>(() -> DataProviderComex.TextoOBComexMock()));
	            }
	        )) {
	    	CONTEXTO_OB.parametros.set("idFront", "id_001");
	    	Objeto respuesta = (Objeto) OBTextos.obtenerTextos(CONTEXTO_OB);
	    	Objeto datos = ((Objeto) respuesta.get("datos")).objetos().get(0);
	        assertAll(
	            () -> assertNotNull(respuesta, "El servicio retonar null, sin manejo de errores"),
	            () -> assertEquals("id_001", datos.get("idFront")),
	            () -> assertEquals("Activos en el exterior", datos.get("titulo")),
	            () -> assertEquals("", datos.get("subtitulo")),
	            () -> assertEquals(
	                "{\"texto\": [\"texto1\", \"texto2\", \"texto3\"]}",
	                datos.get("descripcion")
	            ),
	            () -> assertEquals(101, datos.get("TipoProductoFirma"))
	        );
	    }
	}
	
	@Test
	void TextoOB_Buscar_Por_IDfront_Sin_Resultado_Test() {
	    try (
	        MockedConstruction<ServicioTextosOB> mockConstruction = Mockito.mockConstruction(
	            ServicioTextosOB.class,
	            (mock, context) -> {when(mock.find("id_002")).thenReturn(new Futuro<>(() -> null));}
	        )) {
	        CONTEXTO_OB.parametros.set("idFront", "id_002");
	        Objeto respuesta = (Objeto) OBTextos.obtenerTextos(CONTEXTO_OB);

	        assertAll(
	            () -> assertNotNull(respuesta, "El servicio retorna null, sin manejo de errores"),
	            () -> assertEquals("ERROR", respuesta.get("estado")),
	            () -> assertEquals("No existe el IdFront", respuesta.get("descripcion"))
	        );
	    }
	}

	@Test
	public void obtenerArchivosBD_Mokito() throws Exception {
		int idOp = 1334;
		try (MockedConstruction<ServicioOPComexOB> mockOP = Mockito.mockConstruction(ServicioOPComexOB.class,
				(mock, context) -> {
					when(mock.find(idOp)).thenReturn(new Futuro<>(() -> DataProviderComex.ordenPago(idOp, "00000002", DataProviderComex.bandeja(idOp))));
				});
			 MockedConstruction<ServicioArchivosComexOB> mockArchivos = Mockito.mockConstruction(ServicioArchivosComexOB.class,
					 (mock, context) -> {
						 when(mock.findByOP(Mockito.any(OrdenPagoComexOB.class))).thenReturn(new Futuro<>(() -> DataProviderComex.archivosOBComex()));
					 })
		) {
			List<ArchivosComexOB> archivos = OBComex.obtenerArchivosBD(idOp);

			assertAll(
					() -> assertNotNull(archivos),
					() -> assertEquals(3, archivos.size()),
					() -> assertEquals("nombre1.pdf", archivos.get(0).nombreArchivo),
					() -> assertEquals("carpeta/archivo1", archivos.get(0).url)
			);
		}
	}
}
