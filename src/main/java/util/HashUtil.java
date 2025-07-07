package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 해시 관련 유틸리티 클래스
 */
public class HashUtil {
    
    /**
     * SHA-1 해시를 계산합니다.
     * 
     * @param data 해시를 계산할 데이터
     * @return SHA-1 해시 문자열
     */
    public static String computeSHA1(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 알고리즘을 찾을 수 없습니다.", e);
        }
    }
    
    /**
     * 16진수 문자열을 바이트 배열로 변환합니다.
     * 
     * @param hex 16진수 문자열
     * @return 바이트 배열
     */
    public static byte[] hexToBytes(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            bytes[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return bytes;
    }
} 