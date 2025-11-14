package ar.com.hipotecario.canal.homebanking.helper;

import java.util.List;
import java.util.concurrent.ExecutorService;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Concurrencia;
import ar.com.hipotecario.canal.homebanking.servicio.RestOmnicanalidad;

public class OmnicanalidadHelper {

	public static Objeto solicitudesPrestamos(List<Object> solicitudes, ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();
		ExecutorService executorService = Concurrencia.executorService(solicitudes);
		for (Object solicitud : solicitudes) {
			executorService.submit(() -> {
				Boolean contieneCajaAhorro = false;
				Boolean contienePaquete = false;
				Boolean contienePrestamo = false;
				Boolean contienePrestamoHipotecario = false;
				String idSolicitud = solicitud.toString();
				ApiResponse response = RestOmnicanalidad.consultarSolicitud(contexto, idSolicitud);
				Objeto item = new Objeto();
				item.set("id", response.objetos("Datos").get(0).string("IdSolicitud"));
				item.set("tipo", "");
				for (Objeto producto : response.objetos("Datos").get(0).objetos("Productos")) {
					contienePaquete |= producto.string("tipoProducto").equals("32");
					contienePrestamo |= producto.string("tipoProducto").equals("2");
					contienePrestamoHipotecario |= producto.string("tipoProducto").equals("1");
					String descripcion = RestOmnicanalidad.descripcionProducto().get(producto.string("tipoProducto"));
					if (!descripcion.isEmpty()) {
						Boolean esPaquetizable = RestOmnicanalidad.productosPaquetizables().contains(producto.string("tipoProducto"));
						Boolean estaPaquetizado = !producto.string("IdPaqueteProductos").isEmpty();
						if (!esPaquetizable || (esPaquetizable && !estaPaquetizado)) {
							contieneCajaAhorro |= producto.string("tipoProducto").equals("8");
							contieneCajaAhorro |= producto.string("tipoProducto").equals("9");

							Objeto subitem = new Objeto();
							subitem.set("id", producto.string("Id"));
							subitem.set("descripcion", descripcion);
							item.add(RestOmnicanalidad.productosOperables().contains(producto.string("tipoProducto")) ? "productos" : "productosExtraordinarios", subitem);
						}
					}
					item.set("tipo", contienePaquete ? "PAQUETE" : item.get("tipo"));
					item.set("tipo", contienePrestamo ? "PRESTAMO" : item.get("tipo"));
					item.set("tipo", contienePrestamo && producto.string("Nemonico").equalsIgnoreCase("PPADELANTO") ? "ADELANTO" : item.get("tipo"));
					item.set("tipo", contienePaquete && contienePrestamo ? "PRESTAMO_PAQUETE" : item.get("tipo"));
				}
				if (item.string("tipo").isEmpty() && contieneCajaAhorro) {
					item.set("tipo", "CAJA_AHORRO");
				}
				if (!contienePrestamoHipotecario) {
					if (!item.string("tipo").isEmpty()) {
						respuesta.add("solicitudes", item);
					}
				}
			});
		}
		Concurrencia.esperar(executorService, null);

		Objeto datos = respuesta.objeto("solicitudes").ordenar("id");

		return datos;
	}

}
