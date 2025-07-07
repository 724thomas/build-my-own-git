package util;

import java.io.File;

/**
 * 파일 관련 유틸리티 클래스
 */
public class FileUtil {
    
    /**
     * 두 바이트 배열을 연결합니다.
     * 
     * @param first 첫 번째 바이트 배열
     * @param second 두 번째 바이트 배열
     * @return 연결된 바이트 배열
     */
    public static byte[] concatenate(byte[] first, byte[] second) {
        byte[] result = new byte[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
    
    /**
     * Git 디렉토리 확인 (파일 필터링에 사용)
     * 
     * @param file 확인할 파일
     * @return .git이 아닌 경우 true
     */
    public static boolean isNotGitDirectory(File file) {
        return !file.getName().equals(".git");
    }
} 