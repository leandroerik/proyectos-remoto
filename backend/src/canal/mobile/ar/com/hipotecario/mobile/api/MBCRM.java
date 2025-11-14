package ar.com.hipotecario.mobile.api;

import java.util.Objects;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.servicio.RestCRM;

public class MBCRM {

	public static RespuestaMB accountOfficer(ContextoMB contexto) {

		Futuro<String> futuroSegmentoComercial = new Futuro<>(() -> MBProducto.obtenerSegmentoComercial(contexto));
		ApiResponseMB response = RestCRM.segmentacionCliente(contexto);
		if (response.hayError()) {
			return RespuestaMB.error();
		}
		RespuestaMB respuesta = new RespuestaMB();
		Objeto accountOfficer = new Objeto();
		if (Objects.nonNull(response) && Objects.nonNull(response.get("Datos")) && !response.toList("Datos").isEmpty()) {
			if (Objects.nonNull(response.objetos("Datos").get(0))) {
				Objeto datos = response.objetos("Datos").get(0);
				if (Objects.nonNull(datos) && Objects.nonNull(datos.string("IdSegmentoRenta"))
						&& ("1".equals(datos.string("IdSegmentoRenta")) || "NEGOCIOS Y PROFESIONALES".equals(futuroSegmentoComercial.get()))
						&& Objects.nonNull(datos.string("OficialAsignado"))
						&& !datos.string("OficialAsignado").isEmpty()) {
					if (Objects.nonNull(datos.string("OficialAsignadoMail")) && !datos.string("OficialAsignadoMail").isEmpty()) {
						accountOfficer.set("showEmail", true);
						accountOfficer.set("oficialAsignadoMail", datos.string("OficialAsignadoMail"));
					} else {
						accountOfficer.set("showEmail", false);
						accountOfficer.set("oficialAsignadoMail", ConfigMB.string("account_officer_defaultMail"));
					}
					accountOfficer.set("imageURL", ConfigMB.string("account_officer_url_img") + datos.string("Id"));
					accountOfficer.set("id", datos.string("Id"));
					accountOfficer.set("OficialAsignado", datos.string("OficialAsignado"));
					accountOfficer.set("esOficialVirtual", datos.bool("OperadorRemoto") != null ? datos.bool("OperadorRemoto"):false);
					respuesta.set("estado", 0);
				} else {
					return RespuestaMB.estado("ACCOUNT_OFFICER_NO_ENCONTRADO");
				}
			} else {
				return RespuestaMB.estado("ACCOUNT_OFFICER_NO_ENCONTRADO");
			}
		}
		if (Objects.nonNull(response) && Objects.nonNull(response.get("Errores")) && !response.toList("Errores").isEmpty()) {
			Objeto Errores = response.objetos("Errores").get(0);
			if(Errores.string("Codigo").equals("409"))
				return RespuestaMB.estado("ACCOUNT_OFFICER_NO_ENCONTRADO");
		}
		respuesta.set("accountOfficer", accountOfficer);
		return respuesta;
	}
}
