package ar.com.hipotecario.canal.officebanking.jpa.dto;

import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.TransferenciaOB;

public class PaginaTransferenciaDTO {

	public List<TransferenciaOB> transferencias = new ArrayList<>();
	public int cantidad;
	public int numeroPagina;
	public int registroPorPagina;
}