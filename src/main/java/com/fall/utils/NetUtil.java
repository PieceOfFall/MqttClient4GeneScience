package com.fall.utils;

import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.List;

/**
 * @author FAll
 * @date 2024年12月12日 11:31
 */
@Slf4j
public class NetUtil {
    private final static char[] digits = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] buf = new char[bytes.length * 2];
        int c = 0;
        for (byte b : bytes) {
            buf[c++] = digits[(b >> 4) & 0x0F];
            buf[c++] = digits[b & 0x0F];
        }
        return new String(buf);
    }

    public static byte[] hexStringToByteArray(String s) {
        s = s.replace(" ", "").toUpperCase();
        // 创建 BigInteger，并确保它不带符号扩展
        BigInteger bigInt = new BigInteger(s, 16);
        byte[] byteArray = bigInt.toByteArray();

        // 如果数组的第一个字节是0，且其余字节符合预期，则去掉前面的0
        if (byteArray.length > 1 && ( s.charAt(0) != '0' &&  byteArray[0] == 0)) {
            byte[] trimmedArray = new byte[byteArray.length - 1];
            System.arraycopy(byteArray, 1, trimmedArray, 0, trimmedArray.length);
            return trimmedArray;
        }
        return byteArray;
    }

    public static Boolean validateAddress(String ip, Integer port) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        return port >= 1 && port <= 65535;
    }

    public static Boolean validateAddress(List<String> addressList) {
        try {
            for (String address : addressList) {
                String[] splitAddress = address.split(":");
                if (splitAddress.length != 2) {
                    log.error("Invalid Address: {}", address);
                    return false;
                }
                if (!validateAddress(splitAddress[0], Integer.parseInt(splitAddress[1])))
                    return false;
            }
            return true;
        } catch (NumberFormatException e) {
            log.error("Invalid Port {}",e.getMessage());
            return false;
        }
    }
}
