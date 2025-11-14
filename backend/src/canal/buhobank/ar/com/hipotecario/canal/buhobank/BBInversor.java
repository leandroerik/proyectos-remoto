package ar.com.hipotecario.canal.buhobank;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.servicio.api.catalogo.ApiCatalogo;
import ar.com.hipotecario.backend.servicio.api.catalogo.Ciudades;
import ar.com.hipotecario.backend.servicio.api.catalogo.Ciudades.Ciudad;
import ar.com.hipotecario.backend.servicio.api.catalogo.Paises;
import ar.com.hipotecario.backend.servicio.api.catalogo.Paises.Pais;
import ar.com.hipotecario.backend.servicio.api.catalogo.Provincias;
import ar.com.hipotecario.backend.servicio.api.catalogo.Provincias.Provincia;
import ar.com.hipotecario.backend.servicio.api.cuentas.ApiCuentas;
import ar.com.hipotecario.backend.servicio.api.cuentas.ApiCuentasBB;
import ar.com.hipotecario.backend.servicio.api.cuentas.CajasAhorrosV1.CajaAhorroV1;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentasBB;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentasBB.CuentaBB;
import ar.com.hipotecario.backend.servicio.api.personas.ApiPersonas;
import ar.com.hipotecario.backend.servicio.api.personas.Domicilios;
import ar.com.hipotecario.backend.servicio.api.personas.Domicilios.Domicilio;
import ar.com.hipotecario.backend.servicio.api.personas.Persona;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsales.SesionEsales;

public class BBInversor extends Modulo {

	public static class DescripcionDomicilio {
		public String nacionalidadId;
		public String nacionalidadDesc;
		public String paisNacimientoId;
		public String paisNacimientoDesc;
		public String provinciaId;
		public String provinciaDesc;
		public String localidadId;
		public String localidadDesc;
		public String ciudadNacimientoDesc;
	}

	public static void ejecutarBBInversor(ContextoBB contexto) {
		logProceso(contexto, "inicio proceso de inversiones");

		Fecha fechaDesde = Fecha.ahora().restarDias(GeneralBB.DIAS_ATRAS_ALTA_BB_INVERSOR_PAQUETES);
		SesionesEsales sesionesEsales = SqlEsales.sesionesBBInversorAceptada(contexto, fechaDesde).tryGet();
		if (sesionesEsales == null || sesionesEsales.isEmpty()) {
			logProceso(contexto, "Sin sesiones de proceso buho inversor para depurar");
			LogBB.evento(contexto, GeneralBB.PROCESO_BUHO_INVERSOR, "nombreProceso: proceso buho inversor, cant. de solicitudes enviadas: 0");
			return;
		}

		int count = 0;
		for (SesionEsales sesionEsales : sesionesEsales) {

			try {

				Persona persona = ApiPersonas.persona(contexto, sesionEsales.cuil, false).tryGet();
				if (persona == null) {
					continue;
				}

				CuentasBB cuentas = ApiCuentasBB.get(contexto, persona.idCliente).tryGet();
				if (cuentas == null) {
					continue;
				}

				CuentaBB cuentaPesos = cuentas.obtenerUltimaCajaDeAhorro("80");
				if (cuentaPesos == null) {
					continue;
				}

				String numeroCuenta = cuentaPesos.numeroProducto;
				String idSucursal = cuentaPesos.sucursal;
				String numeroCuentaUsd = null;

				CuentaBB cuentaDolares = cuentas.obtenerUltimaCajaDeAhorro("2");
				if (cuentaDolares != null) {
					numeroCuentaUsd = cuentaDolares.numeroProducto;
				}

				if (empty(numeroCuenta)) {
					continue;
				}

				CajaAhorroV1 cajaAhorro = ApiCuentas.cajaAhorroV1(contexto, numeroCuenta, fechaDesde).tryGet();
				if (cajaAhorro == null) {
					continue;
				}

				String cbu = cajaAhorro.cbu;
				String cbuUsd = null;

				if (!empty(numeroCuentaUsd)) {
					CajaAhorroV1 cajaAhorroUsd = ApiCuentas.cajaAhorroV1(contexto, numeroCuentaUsd, fechaDesde).tryGet();
					cbuUsd = cajaAhorroUsd.cbu;
				}

				DescripcionDomicilio domicilio = obtenerDescripcionDomicilio(contexto, sesionEsales);

				Boolean solicitudCTAPART = SqlEsales.crearSolicitudBuhoInversor(contexto, persona.idCliente, sesionEsales, "CTAPART", numeroCuentaUsd, cbuUsd, numeroCuenta, idSucursal, cbu, "N", domicilio).tryGet();
				Boolean solicitudUNI = SqlEsales.crearSolicitudBuhoInversor(contexto, persona.idCliente, sesionEsales, "UNI", numeroCuentaUsd, cbuUsd, numeroCuenta, idSucursal, cbu, "N", domicilio).tryGet();

				if (solicitudCTAPART && solicitudUNI) {
					SqlEsales.actualizarEstadoBBInversor(contexto, sesionEsales.id, GeneralBB.SOLICITUD_INVERSOR_ENVIADA).tryGet();
					LogBB.evento(contexto, "INVERSOR_" + GeneralBB.SOLICITUD_INVERSOR_ENVIADA, null, sesionEsales.cuil);
					count++;
				}

			} catch (Exception e) {
				LogBB.evento(contexto, ErroresBB.ERROR_PROCESO_BB_INVERSOR, null, sesionEsales.cuil);
			}
		}

		LogBB.evento(contexto, GeneralBB.PROCESO_BUHO_INVERSOR, "nombreProceso: proceso buho inversor cant. de solicitudes enviadas: " + count + ", cant. de casos procesados: " + sesionesEsales.size());
		
		logProceso(contexto, "Sesiones enviadas para alta proceso buho inversor, depuradas: " + count);
		logProceso(contexto, "finaliza proceso de inversiones");
	}

	public static DescripcionDomicilio obtenerDescripcionDomicilio(ContextoBB contexto, SesionEsales sesion) {

		DescripcionDomicilio domicilio = new DescripcionDomicilio();

		domicilio.paisNacimientoId = GeneralBB.DEFAULT_PAIS_NACIMIENTO;
		domicilio.paisNacimientoDesc = GeneralBB.DEFAULT_PAIS_NACIMIENTO_DESC;

		domicilio.nacionalidadId = sesion.nacionalidad_id;
		domicilio.nacionalidadDesc = obtenerNacionalidadDesc(contexto, sesion.nacionalidad_id);

		String localidadId = sesion.dom_localidad_envio;
		String localidadDesc = !empty(sesion.localidad_descripcion) ? sesion.localidad_descripcion : obtenerLocalidadDescById(contexto, sesion.dom_localidad_envio, sesion.domicilio_prov_id);
		if (empty(localidadDesc)) {
			localidadDesc = sesion.dom_barrio_envio;
		}

		String provinciaId = sesion.domicilio_prov_id;
		String provinciaDesc = obtenerProvinciaDesc(contexto, sesion.domicilio_prov_id);
		if (empty(localidadDesc) || empty(provinciaDesc)) {
			Domicilios domicilios = ApiPersonas.domicilios(contexto, sesion.cuil, false).tryGet();
			if (domicilios != null) {
				Domicilio domicilioLegal = domicilios.legal();
				if (domicilioLegal != null) {

					if (empty(localidadDesc)) {
						localidadId = domicilioLegal.idCiudad;
						localidadDesc = domicilioLegal.ciudad;
					}

					if (empty(provinciaDesc)) {
						provinciaId = domicilioLegal.idProvincia;
						provinciaDesc = domicilioLegal.provincia;
					}
				}
			}
		}

		domicilio.localidadId = localidadId;
		domicilio.localidadDesc = localidadDesc;

		domicilio.provinciaId = provinciaId;
		domicilio.provinciaDesc = provinciaDesc;
		domicilio.ciudadNacimientoDesc = provinciaDesc;

		return domicilio;
	}

	public static String obtenerLocalidadDescById(Contexto contexto, String idLocalidad, String idProvincia) {
		if (empty(idLocalidad) || empty(idProvincia))
			return null;

		Ciudades ciudades = ApiCatalogo.ciudades(contexto, idProvincia).tryGet();
		for (Ciudad ciudad : ciudades) {
			if (ciudad.id.equals(idLocalidad)) {
				return ciudad.descripcion;
			}
		}

		return null;
	}

	public static String obtenerProvinciaDesc(Contexto contexto, String idProvincia) {
		Provincias provincias = ApiCatalogo.provincias(contexto).tryGet();
		Provincia provincia = provincias.buscarProvinciaById(idProvincia);
		return !empty(provincia) ? provincia.descripcion : null;
	}

	public static String obtenerNacionalidadDesc(Contexto contexto, String idNacionalidad) {
		Paises paises = ApiCatalogo.paises(contexto).tryGet();
		Pais pais = paises.buscarPaisById(idNacionalidad);
		return !empty(pais) ? pais.nacionalidad : null;
	}

}
