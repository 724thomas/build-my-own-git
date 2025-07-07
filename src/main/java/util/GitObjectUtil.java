package util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.DataFormatException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

/**
 * Git 객체 관련 유틸리티 클래스
 */
public class GitObjectUtil {
    
    /**
     * Git 객체를 압축 해제합니다.
     * 
     * @param data 압축된 데이터
     * @return 압축 해제된 문자열
     */
    public static String decompressGitObject(byte[] data) {
        Inflater inflater = new Inflater();
        inflater.setInput(data);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            return outputStream.toString("UTF-8");
        } catch (DataFormatException e) {
            throw new RuntimeException("Git 객체 압축 해제 실패", e);
        } catch (IOException e) {
            throw new RuntimeException("압축 해제 중 IO 오류", e);
        } finally {
            inflater.end();
        }
    }
    
    /**
     * Git 객체를 파일 시스템에 저장합니다.
     * 
     * @param hash 객체 해시
     * @param content 객체 내용
     */
    public static void writeGitObject(String hash, byte[] content) {
        String dirPath = ".git/objects/" + hash.substring(0, 2);
        String filePath = dirPath + "/" + hash.substring(2);
        new File(dirPath).mkdirs();

        try (FileOutputStream fos = new FileOutputStream(filePath);
             DeflaterOutputStream dos = new DeflaterOutputStream(fos)) {
            dos.write(content);
            dos.finish();
        } catch (IOException e) {
            throw new RuntimeException("Git 객체 파일 쓰기 실패: " + filePath, e);
        }
    }
    
    /**
     * Git 객체 파일을 읽어 압축된 데이터를 반환합니다.
     * 
     * @param hash 객체 해시
     * @return 압축된 바이트 데이터
     */
    public static byte[] readGitObjectFile(String hash) {
        String objectPath = ".git/objects/" + hash.substring(0, 2) + "/" + hash.substring(2);
        try {
            return Files.readAllBytes(Paths.get(objectPath));
        } catch (IOException e) {
            throw new RuntimeException("객체 파일 읽기 실패: " + objectPath, e);
        }
    }
} 