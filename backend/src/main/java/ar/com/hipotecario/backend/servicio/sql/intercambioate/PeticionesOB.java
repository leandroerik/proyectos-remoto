package ar.com.hipotecario.backend.servicio.sql.intercambioate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.annotation.PropertyKey;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.intercambioate.PeticionesOB.PeticionOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.TipoCuentaOB.TipoCuentaATE;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.TransferenciaOB;

@SuppressWarnings("serial")
public class PeticionesOB extends SqlObjetos<PeticionOB> {

	public static String CANAL = "OB2";

	/* ========== ATRIBUTOS ========== */
	public static class PeticionOB extends SqlObjeto {
		@PropertyKey
		public String Id;
		public Fecha FecSolicitud;
		public String IdLote;
		public String IdOpe;
		public String IsConsolidado;
		public String CbuOrigen;
		public String DescOrigen;
		public String CuiOrigen;
		public String CtaOrigen;
		public String TipoCtaOrigen;
		public String CbuDest;
		public String DescDestino;
		public String CuiDest;
		public String CtaDestino;
		public String TipoCtaDestino;
		public String CamaDest;
		public String Moneda;
		public BigDecimal Monto;
		public String TipoOp;
		public String FecAplic;
		public Integer LineaLote;
		public Integer TotalLote;
		public String Concepto;
		public String Observaciones;
		public String CbuPozo;
		public String TipCuePozo;
		public String CtaPozo;
		public String Vep;
		public String CanalOrigen;
		public Integer Id_Obe_Operador;
		public Fecha Dt_Obe_Operador;
		public Integer Id_Obe_Supervisor;
		public Fecha Dt_Obe_Supervisor;
		public Integer RefClienteOb_Operacion;
		public String Ref_ClienteOb_Carga;
		public String Id_Cobis_Firmante1;
		public String Id_Cobis_Firmante2;
		public String Id_Cobis_Firmante3;
		public Fecha Dt_Firmante1;
		public Fecha Dt_Firmante2;
		public Fecha Dt_Firmante3;
		public String Tipo_Ident_Benf;
		public String ObsTerceros;
		public PeticionOB() {
			super();
		}

		public PeticionOB(TransferenciaOB transferenciaOB) {
			this.FecSolicitud =  new Fecha(localDateTimeToDate(transferenciaOB.fechaAplicacion));
			//this.FecSolicitud =  Fecha.ahora();
			this.IdLote = transferenciaOB.idDeCarga.id.toString();// Secuencia Es el idCarga de transferencias ()
			this.IdOpe = String.format("%026d", transferenciaOB.id);
			this.IsConsolidado = "N";// No es un debito total, sirve para sueldos
			this.CbuOrigen = transferenciaOB.debito.cbu;
			this.DescOrigen = " "; // TODO NULL - Se enviaba la razon social que estaba asociada a la cuenta
									// BE_Cuenta
			this.CuiOrigen = transferenciaOB.debito.cuit;
			this.CtaOrigen = transferenciaOB.debito.nroCuenta;

			this.TipoCtaOrigen = TipoCuentaATE.getTipo(transferenciaOB.moneda.id.toString(), transferenciaOB.debito.tipoCuenta.toString()).toString();
			this.CbuDest = transferenciaOB.credito.cbu;
			if (transferenciaOB.credito.titular.length() > 24) {
				this.DescDestino = transferenciaOB.credito.titular.substring(0, 25);
			} else {
				this.DescDestino = transferenciaOB.credito.titular;
			}
			this.CuiDest = transferenciaOB.credito.cuit;
			this.CtaDestino = transferenciaOB.credito.nroCuenta;
			this.TipoCtaDestino = TipoCuentaATE.getTipo(transferenciaOB.moneda.id.toString(), transferenciaOB.credito.tipoCuenta.toString()).toString();
			this.CamaDest = transferenciaOB.camara.id.toString();
			this.Moneda = String.format("%02d", transferenciaOB.moneda.id);// Van aa ser las mismas, validar que las dos monedas
			this.Monto = transferenciaOB.monto;
			this.TipoOp = String.format("%02d", transferenciaOB.tipo.id);// TODO Tipo de operacion: propia sueldos terceros
			DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");
			String formatDateTime = transferenciaOB.fechaAplicacion.format(format);
			this.FecAplic = formatDateTime.toString();
			this.LineaLote = 1;// linea de una serie de archivos
			this.TotalLote = 1;// Total de lineas
			this.Concepto = transferenciaOB.concepto.id.toString();
			
			this.Observaciones = "";// Vacio
			this.CbuPozo = "";// Vacio
			this.TipCuePozo = "";// Vacio
			this.CtaPozo = "";// Vacio
			this.Vep = "";// Vacio
			this.CanalOrigen = CANAL;
			this.Id_Obe_Operador = 0;// Fijo 0
			this.Dt_Obe_Operador = Fecha.ahora();
			this.Id_Obe_Supervisor = 0;// Fijo
			this.Dt_Obe_Supervisor = null;// null
			this.RefClienteOb_Operacion = transferenciaOB.usuario.codigo;// usuCodigo
			this.Ref_ClienteOb_Carga = transferenciaOB.id.toString();// TODO Max confirmar ultimaOperacionGenerada.getOpeId().toString() max idOpe
																		// con el ultimo id generado - transaccionFirma
			this.Id_Cobis_Firmante1 = transferenciaOB.usuario.codigo.toString();// TODO Max confirmar idCobis de Firmante 1
			this.Id_Cobis_Firmante2 = "";// TODO Max confirmar idCobis de Firmante 2
			this.Id_Cobis_Firmante3 = "";// TODO Max confirmar idCobis de Firmante 3
			this.Dt_Firmante1 = Fecha.ahora(); // se coloca el DTime de la firma 1
			this.Dt_Firmante2 = null; // se coloca el DTime de la firma 2
			this.Dt_Firmante3 = null; // se coloca el DTime de la firma 3
			this.Tipo_Ident_Benf = "11";// 11 cuit Enum viejo
			this.ObsTerceros = "";// Vacio
//			this.ObsTerceros = transferenciaOB.tipo.id == 63 ?( transferenciaOB.concepto.codigo.isEmpty() ? "VAR" : transferenciaOB.concepto.codigo) : null;// Vacio
		}

		private Date localDateTimeToDate(LocalDate ldt) {
			return Date.from(ldt.atStartOfDay(ZoneId.systemDefault()).toInstant());
		}
	}

	/* ========== SERVICIO ========== */
	public static PeticionesOB get(Contexto contexto, String id) {
		String sql = "SELECT * FROM [IntercambioATE].[dbo].[PeticionesOB2] WHERE id = ?";

		Objeto datos = Sql.select(contexto, "intercambioate", sql, id);
		return map(datos, PeticionesOB.class, PeticionOB.class);
	}

	public static void post(Contexto contexto, PeticionOB peticionOB) {
		String sql = "INSERT INTO [IntercambioATE].[dbo].[PeticionesOB2]";
		Sql.insertGenerico(contexto, "intercambioate", sql, peticionOB);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		String test = "POST";

		Contexto contexto = contexto("HB", "desarrollo");

		if ("GET".equals(test)) {
			PeticionesOB datos = get(contexto, "2");
			imprimirResultado(contexto, datos);
		}

		if ("POST".equals(test)) {
			PeticionOB peticionOB = new PeticionOB();
			peticionOB.FecSolicitud = Fecha.ahora();
			peticionOB.IdLote = "";
			peticionOB.IdOpe = "";
			peticionOB.IsConsolidado = "";
			peticionOB.CbuOrigen = "";
			peticionOB.DescOrigen = "";
			peticionOB.CuiOrigen = "";
			peticionOB.CtaOrigen = "";
			peticionOB.TipoCtaOrigen = "";
			peticionOB.CbuDest = "";
			peticionOB.DescDestino = "";
			peticionOB.CuiDest = "";
			peticionOB.CtaDestino = "";
			peticionOB.TipoCtaDestino = "";
			peticionOB.CamaDest = "";
			peticionOB.Moneda = "";
			peticionOB.Monto = new BigDecimal("0");
			peticionOB.TipoOp = "";
			peticionOB.FecAplic = "";
			peticionOB.LineaLote = 0;
			peticionOB.TotalLote = 0;
			peticionOB.Concepto = "";
			peticionOB.Observaciones = "";
			peticionOB.CbuPozo = "";
			peticionOB.TipCuePozo = "";
			peticionOB.CtaPozo = "";
			peticionOB.Vep = "";
			peticionOB.CanalOrigen = "OB";
			peticionOB.Id_Obe_Operador = 0;
			peticionOB.Dt_Obe_Operador = Fecha.ahora();
			peticionOB.Id_Obe_Supervisor = 0;
			peticionOB.Dt_Obe_Supervisor = Fecha.ahora();
			peticionOB.RefClienteOb_Operacion = 0;
			peticionOB.Ref_ClienteOb_Carga = "";
			peticionOB.Id_Cobis_Firmante1 = "";
			peticionOB.Id_Cobis_Firmante2 = "";
			peticionOB.Id_Cobis_Firmante3 = "";
			peticionOB.Dt_Firmante1 = Fecha.ahora();
			peticionOB.Dt_Firmante2 = Fecha.ahora();
			peticionOB.Dt_Firmante3 = Fecha.ahora();
			peticionOB.Tipo_Ident_Benf = "";
			peticionOB.ObsTerceros = "";
			post(contexto, peticionOB);
		}
	}
}
