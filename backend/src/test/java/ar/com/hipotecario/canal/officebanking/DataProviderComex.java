package ar.com.hipotecario.canal.officebanking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EstadoUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.TextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;

public class DataProviderComex {

	
	public static TextoOB TextoOBComexMock() {
		 TipoProductoFirmaOB tipoProducto = new TipoProductoFirmaOB(1, true, "Comercio Exterior", 101, false);
		 TextoOB textoOB = new TextoOB(1, tipoProducto, "Activos en el exterior", "", "{\"texto\": [\"Mercadería\"]}", "id_001");

		 return textoOB;
	}
	
	public static List<CategoriaComexOB> categoriaComexListMock() {
		 TipoProductoFirmaOB tipoProducto = new TipoProductoFirmaOB(1, true, "Comercio Exterior", 101, false);
		 TextoOB textoOB = new TextoOB(1, tipoProducto, "Activos en el exterior", "", "{\"texto\": [\"Mercadería\"]}", "id_001");

		 CategoriaComexOB categoria1 = new CategoriaComexOB(1, "Categoria Bienes", textoOB);
		 CategoriaComexOB categoria2 = new CategoriaComexOB(2, "Categoria Servicios", textoOB);

		 return Arrays.asList(categoria1, categoria2);
	 }
	
	public static CategoriaComexOB categoriaComexMock() {
		TipoProductoFirmaOB tipoProducto = new TipoProductoFirmaOB(1, true, "Comercio Exterior", 101, false);
		TextoOB textoOB = new TextoOB(1, tipoProducto, "Activos en el exterior", "", "{\"texto\": [\"Mercadería\"]}", "id_001");

		 CategoriaComexOB categoria1 = new CategoriaComexOB(1, "Categoria Bienes", textoOB);

		 return categoria1;
	 }
	
	public static List<ConceptoComexOB> conceptosComexListMock() {
		TipoProductoFirmaOB tipoProducto = new TipoProductoFirmaOB(1, true, "Comercio Exterior", 101, false);
		TextoOB textoOB = new TextoOB(1, tipoProducto, "Activos en el exterior", "", "{\"texto\": [\"Mercadería\"]}", "id_001");

		CategoriaComexOB categoria1 = new CategoriaComexOB(1, "Categoria Bienes", textoOB);
		 
		MonedaOB moneda = new MonedaOB(2, "USD", "Dolares EEUU", "USD");
		ItemsConceptoOB item = new ItemsConceptoOB(2, 
				"{ itemsConceptos: [{'item1': 'Formulario nº 3090', 'item2': 'Formulario nº 3244', 'item3': '<b>Factura E</b> (*) con la siguiente información: \n Condición de pago \n Mercadería \n Monto \n Incoterm \n (*) REQUISITOS MINIMOS EN FC' }]}");

		ConceptoComexOB conceptos1 = new ConceptoComexOB(1, "A01", "Cobro de exportaciones", categoria1, moneda, item);
		ConceptoComexOB conceptos2 = new ConceptoComexOB(1, "A02", "Cobro anticipado de exportaciones", categoria1, moneda, item);

		return Arrays.asList(conceptos1, conceptos2);
	 }

	public static List<OrdenPagoComexOB> historialComexListMock() {
		TipoProductoFirmaOB tipoProducto = new TipoProductoFirmaOB(1, true, "Comercio Exterior", 101, false);
		TextoOB textoOB = new TextoOB(1, tipoProducto, "Activos en el exterior", "", "{\"texto\": [\"Mercadería\"]}", "id_001");
		CategoriaComexOB categoria1 = new CategoriaComexOB(1, "Categoria Bienes", textoOB);
		MonedaOB moneda = new MonedaOB(2, "USD", "Dolares EEUU", "USD");
		ItemsConceptoOB item = new ItemsConceptoOB(2, 
				"{ itemsConceptos: [{'item1': 'Formulario nº 3090', 'item2': 'Formulario nº 3244', 'item3': '<b>Factura E</b> (*) con la siguiente información: \n Condición de pago \n Mercadería \n Monto \n Incoterm \n (*) REQUISITOS MINIMOS EN FC' }]}");
		ConceptoComexOB conceptos1 = new ConceptoComexOB(1, "A01", "Cobro de exportaciones", categoria1, moneda, item);
		EstadoOPComexOB estadoOPComex_bandeja = new EstadoOPComexOB(1, "EN BANDEJA");
		EstadoOPComexOB estadoOPComex_exito = new EstadoOPComexOB(2, "EXITO");
		EstadoUsuarioOB estadoUsuario = new EstadoUsuarioOB(1, "Habilitado");
		UsuarioOB usuario = new UsuarioOB(60, 29699440L, 27296994401L, "Sofia", "Gomez", "qwfesw", "1803161", "sg@gmail.com", LocalDateTime.now(), 
    			estadoUsuario, LocalDateTime.now(), LocalDateTime.now(), (short) 0, "42220290", "1153876047", "Movistar", true, false, true, true, (short) 1);
		EmpresaOB empresa = new EmpresaOB(1, 27296994401L, "1803190", "Negro SA");
		
		EstadoBandejaOB estadoBandeja = new EstadoBandejaOB();
	    estadoBandeja.id = 3;
	    estadoBandeja.descripcion = "PENDIENTE FIRMA";
	    EstadoBandejaOB estadoBandeja1 = new EstadoBandejaOB();
	    estadoBandeja1.id = 6;
	    estadoBandeja1.descripcion = "FIRMADO COMPLETO";
	    BigDecimal monto = new BigDecimal("15000");
	    	
		List<OrdenPagoComexOB> ordenes = new ArrayList<>();
		OrdenPagoComexOB orden1 = new OrdenPagoComexOB(1200, categoria1, conceptos1, "Verde SA", "TRR00024046156", 'A', true, "402500001005197", "402500001005197", "url1", monto, moneda, monto, moneda, 
	    		estadoOPComex_bandeja, LocalDateTime.now(), LocalDateTime.now(), usuario,
	    		tipoProducto, empresa, estadoBandeja, "300000001090190", LocalDateTime.now());
		ordenes.add(orden1);
		
		OrdenPagoComexOB orden2 = new OrdenPagoComexOB(1201, categoria1, conceptos1, "Bordo SA", "TRR00030167558", 'A', true, "402500001005197", "402500001005197", "url1", monto, moneda, monto, moneda, 
				estadoOPComex_exito, LocalDateTime.now(), LocalDateTime.now(), usuario,
	    		tipoProducto, empresa, estadoBandeja, "300000001090190", LocalDateTime.now());
		ordenes.add(orden2);
	    
		OrdenPagoComexOB orden3 = new OrdenPagoComexOB(1201, categoria1, conceptos1, "Bordo SA", "TRR00019338614", 'A', true, "402500001005197", "402500001005197", "url1", monto, moneda, monto, moneda, 
				estadoOPComex_exito, LocalDateTime.now(), LocalDateTime.now(), usuario,
	    		tipoProducto, empresa, estadoBandeja, "300000001090190", LocalDateTime.now());
		ordenes.add(orden3);
	        
		return ordenes;
	 }
	
	public static BandejaOB bandeja(Integer id) {
		TipoProductoFirmaOB tipoProducto = new TipoProductoFirmaOB(1, true, "Comercio Exterior", 101, false);
		EmpresaOB empresa = new EmpresaOB(1, 27296994401L, "1803190", "Negro SA");
		EstadoBandejaOB estadoBandeja = new EstadoBandejaOB();
		estadoBandeja.id = 6;
    	estadoBandeja.descripcion = "FIRMADO COMPLETO";
    	MonedaOB moneda = new MonedaOB(80, "ARS", "Pesos", "$");
    	BigDecimal monto = new BigDecimal("10000");
    	LocalDateTime fecha = LocalDateTime.parse("2024-11-04T18:14:01.184");
    	
		BandejaOB bandeja = new BandejaOB();
    	bandeja.id = id;    	
    	bandeja.tipoProductoFirma = tipoProducto;
    	bandeja.empresa = empresa;
    	bandeja.estadoBandeja = estadoBandeja;
    	bandeja.cuentaOrigen = "400000010959363";
    	bandeja.monto = monto;    	
    	bandeja.moneda = moneda;    	
    	bandeja.fechaUltActulizacion = fecha;

    	return bandeja;
	}

	public static EmpresaOB empresa() {
		EmpresaOB empresa = new EmpresaOB(65, 20265134406L, "1607564", "Lila SA");
    	return empresa;
	}
	
	public static OrdenPagoComexOB ordenPago(Integer id, String nroTRR, BandejaOB bandeja) {
		TipoProductoFirmaOB tipoProducto = new TipoProductoFirmaOB(1, true, "Comercio Exterior", 101, false);
		TextoOB textoOB = new TextoOB(1, tipoProducto, "Activos en el exterior", "", "{\"texto\": [\"Mercadería\"]}", "id_001");
 	 	
		CategoriaComexOB categoria1 = new CategoriaComexOB(1, "Categoria Bienes", textoOB);
		ItemsConceptoOB item = new ItemsConceptoOB(2, 
				"{ itemsConceptos: [{'item1': 'Formulario nº 3090', 'item2': 'Formulario nº 3244', 'item3': '<b>Factura E</b> (*) con la siguiente información: \n Condición de pago \n Mercadería \n Monto \n Incoterm \n (*) REQUISITOS MINIMOS EN FC' }]}");

		ConceptoComexOB conceptos1 = new ConceptoComexOB(1, "A01", "Cobro de exportaciones", categoria1, bandeja.moneda, item);
		EstadoOPComexOB estadoOPComex = new EstadoOPComexOB(2, "EXITO");
		EstadoUsuarioOB estadoUsuario = new EstadoUsuarioOB(1, "Habilitado");
		
		LocalDateTime fechaCreacionUsr = LocalDateTime.parse("2023-01-10T18:14:01.184");
		LocalDateTime ultimoAcceso = LocalDateTime.parse("2024-11-04T18:14:01.184");
		LocalDateTime accesoFecha = LocalDateTime.parse("2024-11-04T18:14:01.184");
		
		UsuarioOB usuario = new UsuarioOB(60, 29699440L, 27296994401L, "Sofia", "Gomez", "qwfesw", "1803161", "sg@gmail.com", fechaCreacionUsr, 
    			estadoUsuario, ultimoAcceso, accesoFecha, (short) 0, "42220290", "1153876047", "Movistar", true, false, true, true, (short) 1);
		LocalDateTime fechaCreacion = LocalDateTime.parse("2024-10-30T18:14:01.184");
		LocalDateTime fechaModificacion = LocalDateTime.parse("2024-10-31T18:14:01.184");
		
		OrdenPagoComexOB ordenPago = new OrdenPagoComexOB(id, categoria1, conceptos1, "Razón Social Prueba", nroTRR, null, 
    			false, "402500001005197", "402500001005197", "url1", bandeja.monto, bandeja.moneda, bandeja.monto, bandeja.moneda, estadoOPComex, 
    			fechaCreacion, fechaModificacion, usuario,
    			bandeja.tipoProductoFirma, bandeja.empresa, bandeja.estadoBandeja, bandeja.cuentaOrigen, bandeja.fechaUltActulizacion);
		
		return ordenPago;
	}

	public static List<ArchivosComexOB> archivosOBComex() {
		BandejaOB bandeja = DataProviderComex.bandeja(1334);
		OrdenPagoComexOB op = ordenPago(1334, "00000002", bandeja);
					ArchivosComexOB archivosComexOB = new ArchivosComexOB
				(
				1,
				op,
				LocalDateTime.now(),
				"nombre1.pdf",
				"carpeta/archivo1"
				);
		ArchivosComexOB archivosComexOB2 = new ArchivosComexOB
				(
				2,
				op,
				LocalDateTime.now(),
				"nombre2.pdf",
				"carpeta/archivo1"
				);
		ArchivosComexOB archivosComexOB3 = new ArchivosComexOB
				(
				3,
				op,
				LocalDateTime.now(),
				"nombre3.pdf",
				"carpeta/archivo1"
				);

		return Arrays.asList(archivosComexOB, archivosComexOB2, archivosComexOB3);
	}
}

