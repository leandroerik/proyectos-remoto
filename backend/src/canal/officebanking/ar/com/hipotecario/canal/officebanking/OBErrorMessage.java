package ar.com.hipotecario.canal.officebanking;

public class OBErrorMessage {

	private String bundleMessage;
	private Object[] parametros;

	/**
	 * @return the bundleMessage
	 */
	public String getBundleMessage() {
		return bundleMessage;
	}

	/**
	 * @param bundleMessage the bundleMessage to set
	 */
	public void setBundleMessage(String bundleMessage) {
		this.bundleMessage = bundleMessage;
	}

	/**
	 * @return the parametros
	 */
	public Object[] getParametros() {
		return parametros;
	}

	/**
	 * @param parametros the parametros to set
	 */
	public void setParametros(Object[] parametros) {
		this.parametros = parametros;
	}

}
