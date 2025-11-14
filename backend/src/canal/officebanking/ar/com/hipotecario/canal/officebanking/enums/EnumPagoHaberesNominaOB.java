package ar.com.hipotecario.canal.officebanking.enums;

import org.apache.commons.lang3.StringUtils;

public class EnumPagoHaberesNominaOB {

	public enum Sexo {
		MASCULINO('M', "MASCULINO"), FEMENINO('F', "FEMENINO");

		private Character codigo;
		private String descripcion;

		private Sexo(Character codigo, String descripcion) {
			this.codigo = codigo;
			this.descripcion = descripcion;

		}

		public static Sexo get(Character code) {
			if (code != null) {
				for (Sexo s : Sexo.values()) {

					if (s.getCodigo().compareTo(code) == 0) {
						return s;
					}
				}
			}
			return null;
		}

		public Character getCodigo() {
			return codigo;
		}

		public String getDescripcion() {
			return descripcion;
		}
	}

	public enum EstadoCivil {
		CASADO("C", "CASADO"), DIVORCIADO("D", "DIVORCIADO"), SEPARADO_JUDICIALMENTE("O", "SEPARADO JUDICIALMENTE"), SOLTERO("S", "SOLTERO"), VIUDO("V", "VIUDO");

		private String codigo;
		private String descripcion;

		private EstadoCivil(String codigo, String descripcion) {
			this.codigo = codigo;
			this.descripcion = descripcion;

		}

		public static EstadoCivil get(String code) {
			if (code != null) {
				for (EstadoCivil s : EstadoCivil.values()) {

					if (s.getCodigo().equalsIgnoreCase(code)) {
						return s;
					}
				}
			}
			return null;
		}

		public String getCodigo() {
			return codigo;
		}

		public String getDescripcion() {
			return descripcion;
		}
	}

	public enum TipoDoc {
		DNI("01", "D.N.I"), LE("02", "L.E"), LC("03", "L.C"), EXTRANJEROS("134", "DOC. PARA EXTRANJ. RES. EN EL PAIS");

		private String codigo;
		private String descripcion;

		private TipoDoc(String codigo, String descripcion) {
			this.codigo = codigo;
			this.descripcion = descripcion;

		}

		public static TipoDoc get(String code) {
			if (code != null) {
				if (code.length() < 2) {
					code = StringUtils.leftPad(code, 2, "0");
				}
				for (TipoDoc s : TipoDoc.values()) {

					if (s.getCodigo().equalsIgnoreCase(code)) {
						return s;
					}
				}
			}
			return null;
		}

		public String getCodigo() {
			return codigo;
		}

		public String getDescripcion() {
			return descripcion;
		}
	}

	public enum SituacionLaboral {

		FIJO("1", "FIJO"), CONTRATADO("2", "CONTRATADO");

		private String codigo;
		private String descripcion;

		private SituacionLaboral(String codigo, String descripcion) {
			this.codigo = codigo;
			this.descripcion = descripcion;

		}

		public static SituacionLaboral get(String code) {

			for (SituacionLaboral s : SituacionLaboral.values()) {

				if (s.getCodigo().equalsIgnoreCase(code)) {
					return s;
				}
			}

			return null;
		}

		public String getCodigo() {
			return codigo;
		}

		public String getDescripcion() {
			return descripcion;
		}

	}

	public enum esPep {
		SI("S", "SI"), NO("N", "NO");

		private String codigo;
		private String descripcion;

		private esPep(String codigo, String descripcion) {
			this.codigo = codigo;
			this.descripcion = descripcion;

		}

		public static esPep get(String code) {
			if (code != null) {
				for (esPep s : esPep.values()) {

					if (s.getCodigo().equalsIgnoreCase(code)) {
						return s;
					}
				}
			}
			return null;
		}

		public String getCodigo() {
			return codigo;
		}

		public String getDescripcion() {
			return descripcion;
		}
	}

	public enum TipoTel {
		LABORAL_ANTERIOR("B", "LABORAL ANTERIOR"), COMERCIAL("C", "COMERCIAL"), LABORAL_CELULAR("CL", "LABORAL CELULAR"), PARTICULAR_CELULAR("E", "PARTICULAR CELULAR"), FAX("F", "FAX"), LABORAL_DE_LINEA("L", "LABORAL DE LINEA"), PARTICULAR_DE_LINEA("P", "PARTICULAR DE LINEA"), REFERENCIA_CELULAR("R", "REFERENCIA CELULAR"), REFERENCIA_DE_LINEA("RL", "REFERENCIA DE LINEA"), CONTACTO_DE_LINEA("T", "CONTACTO DE LINEA");

		private String codigo;
		private String descripcion;

		private TipoTel(String codigo, String descripcion) {
			this.codigo = codigo;
			this.descripcion = descripcion;

		}

		public static TipoTel get(String code) {

			for (TipoTel s : TipoTel.values()) {

				if (s.getCodigo().equalsIgnoreCase(code)) {
					return s;
				}
			}

			return null;
		}

		public String getCodigo() {
			return codigo;
		}

		public String getDescripcion() {
			return descripcion;
		}
	}

	public enum TipoMail {
		DIRECCION_EMAIL_LABORAL("EML", "DIRECCION EMAIL LABORAL"), DIRECCION_EMAIL_PERSONAL("EMP", "DIRECCION EMAIL PERSONAL");

		private String codigo;
		private String descripcion;

		private TipoMail(String codigo, String descripcion) {
			this.codigo = codigo;
			this.descripcion = descripcion;

		}

		public static TipoMail get(String code) {

			for (TipoMail s : TipoMail.values()) {

				if (s.getCodigo().equalsIgnoreCase(code)) {
					return s;
				}
			}

			return null;
		}

		public String getCodigo() {
			return codigo;
		}

		public String getDescripcion() {
			return descripcion;
		}
	}

	public enum TipoCuitCuil {

		CUIT("11", "CUIT"), CUIL("08", "CUIL");

		private String codigo;
		private String descripcion;

		private TipoCuitCuil(String codigo, String descripcion) {
			this.codigo = codigo;
			this.descripcion = descripcion;

		}

		public static TipoCuitCuil get(String code) {
			if (code != null) {
				code = StringUtils.leftPad(code, 2, "0");
				for (TipoCuitCuil s : TipoCuitCuil.values()) {

					if (s.getCodigo().equalsIgnoreCase(code)) {
						return s;
					}
				}
			}
			return null;
		}

		public String getCodigo() {
			return codigo;
		}

		public String getDescripcion() {
			return descripcion;
		}
	}
}