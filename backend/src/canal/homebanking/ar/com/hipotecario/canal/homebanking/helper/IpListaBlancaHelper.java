package ar.com.hipotecario.canal.homebanking.helper;

import ar.com.hipotecario.canal.homebanking.base.ConfigHB;

import java.math.BigInteger;
import java.net.InetAddress;

public class IpListaBlancaHelper {
    private static BigInteger startIp;
    private static BigInteger endIp;
    private static BigInteger startIpVpn;
    private static BigInteger endIpVpn;

    public IpListaBlancaHelper(String startIP, String endIP) {
        try {
            startIp = convertirIpBigInteger(InetAddress.getByName(startIP));
            endIp = convertirIpBigInteger(InetAddress.getByName(endIP));
        } catch (Exception e) {
        }
    }

    public IpListaBlancaHelper() {
        try {
            startIp = convertirIpBigInteger(InetAddress.getByName(ConfigHB.string("rango_start_ip")));
            endIp = convertirIpBigInteger(InetAddress.getByName(ConfigHB.string("rango_end_ip")));
            startIpVpn = convertirIpBigInteger(InetAddress.getByName(ConfigHB.string("rango_start_vpn_ip")));
            endIpVpn = convertirIpBigInteger(InetAddress.getByName(ConfigHB.string("rango_end_vpn_ip")));
        } catch (Exception e) {
        }
    }

    /**
     * Agregar un rango de Ip a la lista blanca
     *
     * @param startIP
     * @param endIP
     */
    public void agregarRangoIp(String startIP, String endIP) {
        try {
            startIp = convertirIpBigInteger(InetAddress.getByName(startIP));
            endIp = convertirIpBigInteger(InetAddress.getByName(endIP));
        } catch (Exception e) {
        }
    }

    /**
     * Verificar si una IP está en alguno de los rangos
     *
     * @param ip ip a verificar
     * @return si está en rango o no
     */
    public boolean estaIpEnRango(String ip) {
        try {
            BigInteger ipNumeric = convertirIpBigInteger(InetAddress.getByName(ip.split(":")[0]));
            return (ipNumeric.compareTo(startIp) >= 0 && ipNumeric.compareTo(endIp) <= 0) || ipNumeric.compareTo(startIpVpn) >= 0 && ipNumeric.compareTo(endIpVpn) <= 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Convertir una IP a un valor numérico
     *
     * @param ipAddress
     * @return Respuesta
     */
    private BigInteger convertirIpBigInteger(InetAddress ipAddress) {
        BigInteger ipNumeric = BigInteger.ZERO;
        for (byte b : ipAddress.getAddress())
            ipNumeric = ipNumeric.shiftLeft(8).add(BigInteger.valueOf(b & 0xFF));
        return ipNumeric;
    }

}
