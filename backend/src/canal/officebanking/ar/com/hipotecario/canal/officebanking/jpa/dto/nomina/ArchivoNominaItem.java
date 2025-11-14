package ar.com.hipotecario.canal.officebanking.jpa.dto.nomina;

import ar.com.hipotecario.canal.officebanking.OBErrorMessage;
import ar.com.hipotecario.canal.officebanking.OBRegistroImportado;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

public class ArchivoNominaItem extends OBRegistroImportado {

    /**
     * ejemplo
     * <p>
     * 1;0;REARTES
     * BARROS;ROMINA;F;01;12345678;18/12/1981;80;80;CATAMARCA;08;20123456786;S;N;11900;2;01/09/2019;001100000;SALTA;1441;;;3300;3590;5;E;054;011;15;5888;7454;EML;fasdj@gmail.com
     */
    protected String arc;
    protected String numeroCuenta;
    protected String nombreCuenta;
    protected Long cuil;
    protected String multiCausal;

    private Integer secuencial;
    private String sucursal;
    private String apellido;
    private String nombres;
    private String sexo;
    private String tipoDocumento;
    private String numeroDocumento;
    private String fechaNacimiento;
    private Integer paisNacimiento;
    private Integer nacionalidad;
    private String ciudadDeNacimiento;
    private String tipoCuil;
    private String numeroCuil;
    private String esPEP;
    private Integer ocupacion;
    private Integer situacionLaboral;
    private String fechaIngreso;
    private double sueldo;
    private String calle;
    private int altura;
    private String piso;
    private String dpto;
    private String cp;
    private String localidad;
    private Integer provincia;
    private String tipoTel;
    private String DDI;
    private String preFijoCel;
    private String caracteristica;
    private String numeroTel;
    private String tipoCorreoElectronico;
    private String correoElectronico;
    private String estadoCivil;
    private String DDN;

    // Getters y Setters para secuencial
    public Integer getSecuencial() {
        return secuencial;
    }

    public void setSecuencial(Integer secuencial) {
        this.secuencial = secuencial;
    }

    // Getters y Setters para sucursal
    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
    }

    // Getters y Setters para apellido
    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    // Getters y Setters para nombres
    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    // Getters y Setters para sexo
    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    // Getters y Setters para tipoDocumento
    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    // Getters y Setters para numeroDocumento
    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    // Getters y Setters para fechaNacimiento
    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    // Getters y Setters para paisNacimiento
    public Integer getPaisNacimiento() {
        return paisNacimiento;
    }

    public void setPaisNacimiento(Integer paisNacimiento) {
        this.paisNacimiento = paisNacimiento;
    }

    // Getters y Setters para nacionalidad
    public Integer getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(Integer nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    // Getters y Setters para ciudadDeNacimiento
    public String getCiudadDeNacimiento() {
        return ciudadDeNacimiento;
    }

    public void setCiudadDeNacimiento(String ciudadDeNacimiento) {
        this.ciudadDeNacimiento = ciudadDeNacimiento;
    }

    // Getters y Setters para tipoCuil
    public String getTipoCuil() {
        return tipoCuil;
    }

    public void setTipoCuil(String tipoCuil) {
        this.tipoCuil = tipoCuil;
    }

    // Getters y Setters para numeroCuil
    public String getNumeroCuil() {
        return numeroCuil;
    }

    public void setNumeroCuil(String numeroCuil) {
        this.numeroCuil = numeroCuil;
    }

    // Getters y Setters para esPEP
    public String getEsPEP() {
        return esPEP;
    }

    public void setEsPEP(String esPEP) throws Exception {

        if (esPEP.length() > 1) {
            throw new Exception("El largo del campo es mayor al permitdo");
        } else {
            this.esPEP = esPEP;
        }

    }

    // Getters y Setters para ocupacion
    public Integer getOcupacion() {
        return ocupacion;
    }

    public void setOcupacion(Integer ocupacion) {
        this.ocupacion = ocupacion;
    }

    // Getters y Setters para situacionLaboral
    public Integer getSituacionLaboral() {
        return situacionLaboral;
    }

    public void setSituacionLaboral(Integer situacionLaboral) {
        this.situacionLaboral = situacionLaboral;
    }

    // Getters y Setters para fechaIngreso
    public String getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(String fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    // Getters y Setters para sueldo
    public double getSueldo() {
        return sueldo;
    }

    public void setSueldo(double sueldo) {
        this.sueldo = sueldo;
    }

    // Getters y Setters para calle
    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    // Getters y Setters para altura
    public int getAltura() {
        return altura;
    }

    public void setAltura(int altura) {
        this.altura = altura;
    }

    // Getters y Setters para piso
    public String getPiso() {
        return piso;
    }

    public void setPiso(String piso) {
        this.piso = piso;
    }

    // Getters y Setters para dpto
    public String getDpto() {
        return dpto;
    }

    public void setDpto(String dpto) {
        this.dpto = dpto;
    }

    // Getters y Setters para cp
    public String getCp() {
        return cp;
    }

    public void setCp(String cp) {
        this.cp = cp;
    }

    // Getters y Setters para localidad
    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    // Getters y Setters para provincia
    public Integer getProvincia() {
        return provincia;
    }

    public void setProvincia(Integer provincia) {
        this.provincia = provincia;
    }

    // Getters y Setters para tipoTel
    public String getTipoTel() {
        return tipoTel;
    }

    public void setTipoTel(String tipoTel) {
        this.tipoTel = tipoTel;
    }

    // Getters y Setters para DDI
    public String getDDI() {
        return DDI;
    }

    public void setDDI(String DDI) {
        this.DDI = DDI;
    }

    // Getters y Setters para DDN
    public String getDDN() {
        return DDN;
    }

    public void setDDN(String DDN) {
        this.DDN = DDN;
    }

    // Getters y Setters para preFijoCel
    public String getPreFijoCel() {
        return preFijoCel;
    }

    public void setPreFijoCel(String preFijoCel) {
        this.preFijoCel = preFijoCel;
    }

    // Getters y Setters para caracteristica
    public String getCaracteristica() {
        return caracteristica;
    }

    public void setCaracteristica(String caracteristica) {
        this.caracteristica = caracteristica;
    }

    // Getters y Setters para numeroTel
    public String getNumeroTel() {
        return numeroTel;
    }

    public void setNumeroTel(String numeroTel) {
        this.numeroTel = numeroTel;
    }

    // Getters y Setters para tipoCorreoElectronico
    public String getTipoCorreoElectronico() {
        return tipoCorreoElectronico;
    }

    public void setTipoCorreoElectronico(String tipoCorreoElectronico) {
        this.tipoCorreoElectronico = tipoCorreoElectronico;
    }

    // Getters y Setters para correoElectronico
    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getEstadoCivil() {
        return estadoCivil;
    }

    public void setEstadoCivil(String estadoCivil) throws Exception {
        if (estadoCivil.length() > 10) {
            throw new Exception("El largo del campo es mayor al permitdo");
        } else {
            this.estadoCivil = estadoCivil;
        }
    }

    public static List<ArchivoNominaItem> readNominaItemFromFile(Blob archivo) throws Exception {
        List<ArchivoNominaItem> NominaItems = new ArrayList<>();
        int i = 1;
        try (InputStream inputStream = archivo.getBinaryStream(); BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            ArchivoNominaItem item;

            while ((line = br.readLine()) != null) {

                    item = new ArchivoNominaItem();
                    // Divide la cadena en partes usando el punto y coma como delimitador
                    String[] partes = line.split(";");

                    // Asigna cada parte a los atributos correspondientes
                    item.setSecuencial(Integer.parseInt(partes[0]));
                    item.setSucursal(partes[1]);
                    item.setApellido(partes[2]);
                    item.setNombres(partes[3]);
                    item.setSexo(partes[4]);
                    item.setTipoDocumento(partes[5]);
                    item.setNumeroDocumento(partes[6]);
                    item.setFechaNacimiento(partes[7]);
                    item.setPaisNacimiento(Integer.parseInt(partes[8]));
                    item.setNacionalidad(Integer.parseInt(partes[9]));
                    item.setCiudadDeNacimiento(partes[10]);
                    item.setTipoCuil(partes[11]);
                    item.setNumeroCuil(partes[12]);
                    item.setEstadoCivil(partes[13]);
//                item.setEsPEP(partes[13].equalsIgnoreCase("S")); // Convierte la cadena a boolean
                    item.setEsPEP(partes[14]); // Convierte la cadena a boolean
                    item.setOcupacion(Integer.valueOf(partes[15]));
                    item.setSituacionLaboral(Integer.valueOf(partes[16]));
                    item.setFechaIngreso(partes[17]);
                    item.setSueldo(Double.parseDouble(partes[18]));
                    item.setCalle(partes[19]);
                    item.setAltura(Integer.parseInt(partes[20]));
                    item.setPiso(partes[21]);
                    item.setDpto(partes[22]);
                    item.setCp(partes[23]);
                    item.setLocalidad(partes[24]);
                    item.setProvincia(Integer.parseInt(partes[25]));
                    item.setTipoTel(partes[26]);
                    item.setDDI(partes[27]);
                    item.setDDN(partes[28]);
                    item.setPreFijoCel(partes[29]);
                    item.setCaracteristica(partes[30]);
                    item.setNumeroTel(partes[31]);
                    item.setTipoCorreoElectronico(partes[32]);
                    item.setCorreoElectronico(partes[33]);

                    NominaItems.add(item);
                    i++;



            }
        } catch (Exception e) {
            throw new Exception("Error en la linea "+i);
        }

        return NominaItems;
    }

    public void setErrores(List<OBErrorMessage> errores) {
        this.errores = errores;
    }

    private List<OBErrorMessage> errores;

    public List<OBErrorMessage> getErrores() {
        return this.errores;
    }

}