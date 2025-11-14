package ar.com.hipotecario.canal.buhobank.negocio;

import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.buhobank.GeneralBB;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TarjetaBeneficio extends Modulo {
    public static String IMAGEN_AEROLINEAS = "https://www.hipotecario.com.ar/media/buhobank/Log_Aero_Plus.png";

    //imagen tarjetas
    public static String IMAGEN_INTERNACIONAL = "https://www.hipotecario.com.ar/media/buhobank/tarjeta-buhopack.png";
    public static String IMAGEN_GOLD = "https://www.hipotecario.com.ar/media/buhobank/tarjeta-gold.png";
    public static String IMAGEN_PLATINUM = "https://www.hipotecario.com.ar/media/buhobank/tarjeta-platinum.png";
    public static String IMAGEN_BLACK = "https://www.hipotecario.com.ar/media/buhobank/tarjeta-signature.png";
    public static String IMAGEN_STANDALONE = "https://www.hipotecario.com.ar/media/buhobank/tarjeta-debito.png";
    public static String IMAGEN_STANDALONE_VIRTUAL = "https://www.hipotecario.com.ar/media/buhobank/tarjeta-virtual.png";

    //iconos beneficios
    public static String ICONO_TARJETA_SIGNO = "https://www.hipotecario.com.ar/media/buhobank/icono-cards.svg";
    public static String ICONO_AHORRO = "https://www.hipotecario.com.ar/media/buhobank/icono-ahorro.svg";
    public static String ICONO_AVION_SIGNO = "https://www.hipotecario.com.ar/media/buhobank/icono-airplane.svg";
    public static String ICONO_SHOP = "https://www.hipotecario.com.ar/media/buhobank/icono-paquete.svg";
    public static String ICONO_BUHO = "https://www.hipotecario.com.ar/media/buhobank/icono-buho.svg";
    public static String ICONO_INVERSOR = "https://www.hipotecario.com.ar/media/buhobank/icono-tabler_trending-up.svg";
    public static String ICONO_MENSAJES = "https://www.hipotecario.com.ar/media/buhobank/icono-dialog.svg";
    public static String ICONO_SEGURO = "https://www.hipotecario.com.ar/media/buhobank/icono-security.svg";
    public static String ICONO_AVION = "https://www.hipotecario.com.ar/media/buhobank/icono-airplane3.svg";
    public static String ICONO_AVION_IZQUIERDA = "https://www.hipotecario.com.ar/media/buhobank/icono-arplane2.svg";
    public static String ICONO_TARJETA = "https://www.hipotecario.com.ar/media/buhobank/icono-cards2.svg";

    public static Objeto ofertaLetra(String nroProducto, String letra, BigDecimal limiteCompra){
        boolean esStandalone = letra.isEmpty();
        boolean esAerolineas = !esStandalone;

        Objeto oferta = respuesta();
        oferta.set("titulo", tituloTarjeta(esStandalone, letra));
        oferta.set("subtitulo", "");
        oferta.add("tarjetas", imagenTarjeta(letra));
        if(!esStandalone){
            oferta.add("tarjetas", IMAGEN_STANDALONE);
        }
        oferta.set("mantenimiento", mantenimiento(letra));
        oferta.set("limiteCompra", esStandalone ? "" : "$ " + Modulo.importe(limiteCompra).replace(",00", ""));
        oferta.set("beneficioEspecial", "");
        oferta.set("beneficios", beneficiosTarjeta(letra, nroProducto));
        oferta.set("checkAerolineas", esAerolineas);
        if(esAerolineas){
            oferta.set("contenidoAerolineas", aerolineasTarjeta(letra));
        }
        oferta.set("nroProducto", nroProducto);
        oferta.set("nombre", nombreTarjeta(letra));
        oferta.set("esStandalone", esStandalone);

        if("4".equals(nroProducto)){
            oferta.set("titulo", "¡Búho Inversor!");
            oferta.set("subtitulo", "Cuentas comitente y cuotapartista");
            oferta.set("mantenimiento", "Operaciones en Títulos y Acciones bonificadas por 1 año.");
        }
        else if("43".equals(nroProducto) || "53".equals(nroProducto)){
            oferta.set("beneficioEspecial", "Acceso a salas VIP en aeropuertos");
        }

        return oferta;
    }

    private static Objeto aerolineasTarjeta(String letra) {
        Objeto contenido = new Objeto();
        contenido.set("imagen", IMAGEN_AEROLINEAS);
        contenido.set("titulo", "Tarjeta " + nombreTarjeta(letra));
        contenido.set("descripcion", "Con tus compras sumá millas al programa de Aerolíneas Argentinas y vola por Argentina y el mundo.");
        contenido.set("textoHtml", aerolineasContenido(letra));
        return contenido;
    }

    private static String aerolineasContenido(String letra){
        return "S".equals(letra) ? "<b>Membresía bonificada</b>  <ul>   <li> Después de diez (10) meses de permanencia y habiendo generado consumos con tu tarjeta de crédito, <b>debitaremos el costo de tu membresía en 3 cuotas y te las reintegraremos.</b></li>   <li> Una vez solicitada la adhesión, el alta se realiza pasados diez (10) días hábiles.</li>  </ul>   <b>Beneficios adicionales</b>  <ul>   <li> Los clientes Signature acumulan <b>un 25% más</b> en millas con sus compras.</li>  </ul>   <b>¿Cómo se calculan las millas?</b>  <ul>   <li> Las millas se calculan por consumos. Por cada dolar (U$S 1,00) gastado o su equivalente en pesos, sumás 1 Milla Aerolíneas Plus.</li>  </ul>"
                : "<b>Costo de membresía</b>  <ul>   <li> <b> 3 cuotas de $42.451,13 + IVA. Total: $127.353,41</b> luego de 10 meses de permanencia en el Programa Ar Plus</li>   <li> Una vez solicitada la adhesión, el alta se realiza pasados diez (10) días hábiles.</li>  </ul>  <b>Beneficios adicionales</b>  <ul>   <li> Los clientes Platinum acumulan <b>un 15% más</b> en millas con sus compras.</li>  </ul>  <b>¿Cómo se calculan las millas?</b>  <ul>   <li> Las millas se calculan por consumos. Por cada dolar (U$S 1,00) gastado o su equivalente en pesos, sumás 1 Milla Aerolíneas Plus.</li>  </ul>";
    }

    private static String tituloTarjeta(boolean esStandalone, String letra){
        return esStandalone ? "¡Tarjeta de Débito Virtual!"
                : "¡Tarjeta de Crédito VISA " + nombreTarjeta(letra) + " y Débito Virtual!";
    }

    private static String nombreTarjeta(String letra){
        return switch (letra) {
            case "I" -> "Internacional";
            case "P" -> "Gold";
            case "L" -> "Platinum";
            case "S" -> "Signature";
            default -> "Standalone";
        };
    }

    private static String imagenTarjeta(String letra){
        return switch (letra) {
            case "I" -> IMAGEN_INTERNACIONAL;
            case "P" -> IMAGEN_GOLD;
            case "L" -> IMAGEN_PLATINUM;
            case "S" -> IMAGEN_BLACK;
            default -> IMAGEN_STANDALONE_VIRTUAL;
        };
    }

    private static String mantenimiento(String letra){
        return switch (letra) {
            case "I", "P" -> "¡Bonificadas por 3 meses!";
            case "L", "S" -> "¡Bonificadas por 3 meses!";
            default -> "¡Sin costo de mantenimiento!";
        };
    }

    private static List<Objeto> beneficiosTarjeta(String letra, String nroProducto) {
        return switch (letra) {
            case "I" -> beneficiosInternacional(letra);
            case "P" -> beneficiosGold(letra);
            case "L" -> beneficiosPlatinum(letra);
            case "S" -> beneficiosBlack(letra);
            default -> beneficiosStandalone(nroProducto);
        };
    }

    public static List<Objeto> beneficiosTarjetaLegacy(String letra) {
        List<Objeto> beneficiosLegacy = new ArrayList<>();

        List<Objeto> beneficios = switch (letra) {
            case "I" -> beneficiosInternacional(letra);
            case "P" -> beneficiosGold(letra);
            case "L" -> beneficiosPlatinum(letra);
            case "S" -> beneficiosBlack(letra);
            default -> beneficiosStandalone(letra);
        };

        for (Objeto beneficio: beneficios){
            String icono = beneficio.string("icono");
            String desc = beneficio.string("descripcion");
            beneficiosLegacy.add(crearBeneficioLegacy(icono, desc));
        }

        return beneficiosLegacy;
    }

    private static List<Objeto> beneficiosInternacional(String letra){
        List<Objeto> beneficios = new ArrayList<>();
        beneficios.add(beneficioTarjeta(letra));
        beneficios.add(beneficioCajaUsd());
        beneficios.add(beneficioInversor());
        beneficios.add(beneficioAerolineas());
        beneficios.add(beneficioPromociones());
        beneficios.add(beneficioBuhoPuntos());
        return beneficios;
    }
    private static List<Objeto> beneficiosGold(String letra){
        return beneficiosInternacional(letra);
    }
    private static List<Objeto> beneficiosPlatinum(String letra){
        List<Objeto> beneficios = new ArrayList<>();
        beneficios.add(beneficioTarjeta(letra));
        beneficios.add(beneficioCajaUsd());
        beneficios.add(beneficioInversor());
        beneficios.add(beneficioAerolineas());
        beneficios.add(beneficioOficial());
        beneficios.add(beneficioSeguro());
        beneficios.add(beneficioViajero());
        beneficios.add(beneficioBuhoPuntos());
        beneficios.add(beneficioPromociones());
        return beneficios;
    }
    private static List<Objeto> beneficiosBlack(String letra){
        List<Objeto> beneficios = new ArrayList<>();
        beneficios.add(beneficioTarjeta( letra));
        beneficios.add(beneficioCajaUsd());
        beneficios.add(beneficioInversor());
        beneficios.add(beneficioAerolineasSignature());
        beneficios.add(beneficioOficial());
        beneficios.add(beneficioPromociones());
        beneficios.add(beneficioViajero());
        beneficios.add(beneficioSeguro());
        beneficios.add(beneficioAdicional());
        return beneficios;
    }

    private static List<Objeto> beneficiosStandalone(String nroProducto){
        List<Objeto> beneficios = new ArrayList<>();
        if("4".equals(nroProducto)){
            beneficios.add(beneficioCajaUsdSinCosto());
            beneficios.add(beneficioTdSinCosto());
            beneficios.add(beneficioPromociones());
        }
        else{
            beneficios.add(beneficioCajaUsd());
            beneficios.add(beneficioPromociones());
            beneficios.add(beneficioInvertirSinCosto());
        }
        beneficios.add(beneficioLimites());
        beneficios.add(beneficioSuscribite());
        return beneficios;
    }

    private static Objeto crearBeneficio(String icono, String desc){
        return new Objeto().set("icono", icono)
                .set("descripcion", desc);
    }

    private static Objeto crearBeneficioLegacy(String icono, String desc){
        return new Objeto().set("icono_id", icono)
                .set("desc_beneficio", desc)
                .set("desc_beneficio_html", desc);
    }

    private static Objeto beneficioTarjeta(String letra){
        return crearBeneficio(ICONO_TARJETA_SIGNO,
                "Tarjeta de Crédito VISA " + nombreTarjeta(letra) + " y Débito.");
    }

    private static Objeto beneficioTarjetaAceptacion(){
        return crearBeneficio(ICONO_TARJETA_SIGNO,
                "Tarjeta de Débito para usar en cualquier momento.");
    }

    private static Objeto beneficioTarjetaAceptacionPaquete(){
        return crearBeneficio(ICONO_TARJETA_SIGNO,
                "Tarjeta de Crédito Visa y Tarjeta de Débito para usar en el momento.");
    }

    private static Objeto beneficioCajaUsd(){
        return crearBeneficio(ICONO_AHORRO,
                "Caja de ahorro en pesos y dólares.");
    }

    private static Objeto beneficioCajaUsdSinCosto(){
        return crearBeneficio(ICONO_AHORRO,
                "Caja de ahorro en pesos y dólares sin costo.");
    }

    private static Objeto beneficioAerolineas(){
        return crearBeneficio(ICONO_AVION_SIGNO,
                "Acceso al programa de beneficios Aerolíneas Plus.");
    }

    private static Objeto beneficioAerolineasSignature(){
        return crearBeneficio(ICONO_AVION_SIGNO,
                "Acceso al programa de beneficios Aerolíneas Plus y membresía bonificada.");
    }

    private static Objeto beneficioPromociones(){
        return crearBeneficio(ICONO_SHOP,
                "Promociones exclusivas y descuentos en todo el país.");
    }

    private static Objeto beneficioBuhoPuntos(){
        return crearBeneficio(ICONO_BUHO,
                "Búho Puntos: premios por compras.");
    }

    private static Objeto beneficioInversor(){
        return crearBeneficio(ICONO_INVERSOR,
                "Cuentas para invertir: comitente y cuotapartista.");
    }

    private static Objeto beneficioInvertirSinCosto(){
        return crearBeneficio(ICONO_INVERSOR,
                "Cuentas para invertir: comitente y cuotapartista sin costo.");
    }

    private static Objeto beneficioOficial(){
        return crearBeneficio(ICONO_MENSAJES,
                "Oficial exclusivo y prioridad de atención.");
    }

    private static Objeto beneficioSeguro(){
        return crearBeneficio(ICONO_SEGURO,
                "Seguro de compra protegida.");
    }

    private static Objeto beneficioViajero(){
        return crearBeneficio(ICONO_AVION,
                "Asistencia al viajero para pasajes abonados con tu tarjeta.");
    }

    private static Objeto beneficioAdicional(){
        return crearBeneficio(ICONO_TARJETA,
                "Tarjetas adicionales sin cargo.");
    }

    private static Objeto beneficioLimites(){
        return crearBeneficio(ICONO_SEGURO,
                "Poné límites, mirá tus consumos, pausa tu tarjeta y activala cuando quieras.");
    }

    private static Objeto beneficioSuscribite(){
        return crearBeneficio(ICONO_TARJETA,
                "Suscribite a membresías digitales para disfrutar de música, series y películas.");
    }

    private static Objeto beneficioTdSinCosto(){
        return crearBeneficio(ICONO_TARJETA_SIGNO,
                "Tarjeta de Débito Virtual sin costo.");
    }

    public static List<Objeto> ofertaAceptadaStandalone(String nroProducto){
        List<Objeto> beneficios = new ArrayList<>();
        if("4".equals(nroProducto)){
            beneficios.add(beneficioInversor());
            beneficios.add(beneficioCajaUsd());
            beneficios.add(beneficioTarjetaAceptacion());
        }
        else{
            beneficios.add(beneficioTarjetaAceptacion());
            beneficios.add(beneficioCajaUsd());
        }
        return beneficios;
    }

    public static List<Objeto> ofertaAceptada(){
        List<Objeto> beneficios = new ArrayList<>();
        beneficios.add(beneficioTarjetaAceptacionPaquete());
        beneficios.add(beneficioCajaUsd());
        return beneficios;
    }

}
