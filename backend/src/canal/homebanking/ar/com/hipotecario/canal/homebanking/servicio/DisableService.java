package ar.com.hipotecario.canal.homebanking.servicio;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import ar.com.hipotecario.canal.homebanking.api.dto.Contador;
import ar.com.hipotecario.canal.homebanking.base.Objeto;

public class DisableService {

	public static final String CAMBIO_CLAVE_EXPIRACION = "CAMBIO_CLAVE_EXPIRACION";
	public static final Integer UMBRAL_CAMBIOS_NO_PERMITIDOS = 1;

	public Boolean getEnabledToOperator(List<Objeto> registros) {
		List<Contador> listContador = getContadorListFromRegistros(registros).stream().filter(item -> !CAMBIO_CLAVE_EXPIRACION.equals(item.getTipo())).collect(Collectors.toList());
		return listContador.size() < UMBRAL_CAMBIOS_NO_PERMITIDOS;
	}

	public List<Contador> getContadorListFromRegistros(List<Objeto> registros) {
		return registros.stream().map(item -> assemblerContador(item)).collect(Collectors.toList());
	}

	private Contador assemblerContador(Objeto objeto) {
		Integer id = (Integer) objeto.get("id");
		String idcobis = (String) objeto.get("idCobis");
		String razon = (String) objeto.get("tipo");
		LocalDateTime momento = ((Timestamp) objeto.get("momento")).toLocalDateTime();
		return new Contador(id, idcobis, razon, momento);
	}

	public Long calculateHourDelay(LocalDateTime nowTime) {
		long delay = 48L;
		switch (nowTime.getDayOfWeek()) {
		case MONDAY:
		case TUESDAY:
			delay = 96L;
			break;
		case WEDNESDAY:
		case THURSDAY:
		case FRIDAY:
			delay = 48L;
			break;
		case SATURDAY:
			delay = (48L + nowTime.getHour());
			break;
		case SUNDAY:
			delay = (72L + nowTime.getHour());
			break;
		}
		return delay;
	}
}
