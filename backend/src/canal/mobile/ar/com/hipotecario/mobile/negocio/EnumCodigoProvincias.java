package ar.com.hipotecario.mobile.negocio;

public enum EnumCodigoProvincias {

	CIUDAD_DE_BUENOS_AIRES(901), PROVINCIA_DE_BUENOS_AIRES(902), CATAMARCA(903), CORDOBA(904), CORRIENTES(905), CHACO(906), CHUBUT(907), ENTRE_RIOS(908), FORMOSA(909), JUJUY(910), LA_PAMPA(911), LA_RIOJA(912), MENDOZA(913), MISIONES(914), NEUQUEN(915), RIO_NEGRO(916), SALTA(917), SAN_JUAN(918), SAN_LUIS(919), SANTA_CRUZ(920), SANTA_FE(921), SANTIAGO_DEL_ESTERO(922), TIERRA_DEL_FUEGO(923), TUCUMAN(924);

	private int codigoProvincia;

	EnumCodigoProvincias(int codigo) {
		codigoProvincia = codigo;
	}

	int getCodigoProvincia() {
		return codigoProvincia;
	}

	public static int getCodigoProvincia(int codigo) {
		int idProvincia = 0;
		switch (codigo) {
		case 1:
			idProvincia = EnumCodigoProvincias.CIUDAD_DE_BUENOS_AIRES.getCodigoProvincia();
			break;
		case 2:
			idProvincia = EnumCodigoProvincias.PROVINCIA_DE_BUENOS_AIRES.getCodigoProvincia();
			break;
		case 3:
			idProvincia = EnumCodigoProvincias.CATAMARCA.getCodigoProvincia();
			break;
		case 4:
			idProvincia = EnumCodigoProvincias.CORDOBA.getCodigoProvincia();
			break;
		case 5:
			idProvincia = EnumCodigoProvincias.CORRIENTES.getCodigoProvincia();
			break;
		case 6:
			idProvincia = EnumCodigoProvincias.CHACO.getCodigoProvincia();
			break;
		case 7:
			idProvincia = EnumCodigoProvincias.CHUBUT.getCodigoProvincia();
			break;
		case 8:
			idProvincia = EnumCodigoProvincias.ENTRE_RIOS.getCodigoProvincia();
			break;
		case 9:
			idProvincia = EnumCodigoProvincias.FORMOSA.getCodigoProvincia();
			break;
		case 10:
			idProvincia = EnumCodigoProvincias.JUJUY.getCodigoProvincia();
			break;
		case 11:
			idProvincia = EnumCodigoProvincias.LA_PAMPA.getCodigoProvincia();
			break;
		case 12:
			idProvincia = EnumCodigoProvincias.LA_RIOJA.getCodigoProvincia();
			break;
		case 13:
			idProvincia = EnumCodigoProvincias.MENDOZA.getCodigoProvincia();
			break;
		case 14:
			idProvincia = EnumCodigoProvincias.MISIONES.getCodigoProvincia();
			break;
		case 15:
			idProvincia = EnumCodigoProvincias.NEUQUEN.getCodigoProvincia();
			break;
		case 16:
			idProvincia = EnumCodigoProvincias.RIO_NEGRO.getCodigoProvincia();
			break;
		case 17:
			idProvincia = EnumCodigoProvincias.SALTA.getCodigoProvincia();
			break;
		case 18:
			idProvincia = EnumCodigoProvincias.SAN_JUAN.getCodigoProvincia();
			break;
		case 19:
			idProvincia = EnumCodigoProvincias.SAN_LUIS.getCodigoProvincia();
			break;
		case 20:
			idProvincia = EnumCodigoProvincias.SANTA_CRUZ.getCodigoProvincia();
			break;
		case 21:
			idProvincia = EnumCodigoProvincias.SANTA_FE.getCodigoProvincia();
			break;
		case 22:
			idProvincia = EnumCodigoProvincias.SANTIAGO_DEL_ESTERO.getCodigoProvincia();
			break;
		case 23:
			idProvincia = EnumCodigoProvincias.TIERRA_DEL_FUEGO.getCodigoProvincia();
			break;
		case 24:
			idProvincia = EnumCodigoProvincias.TUCUMAN.getCodigoProvincia();
			break;
		default:
			idProvincia = 0;
		}

		return idProvincia;
	}

}
