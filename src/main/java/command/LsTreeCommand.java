package command;

import util.GitObjectUtil;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * git ls-tree 명령어 구현
 */
public class LsTreeCommand implements GitCommand {
    
    @Override
    public void execute(String[] args) {
        if (args.length < 3) {
            System.out.println("ls-tree 명령어에는 객체 해시가 필요합니다.");
            return;
        }
        
        lsTree(args);
    }
    
    /**
     * 트리 객체의 내용을 나열합니다.
     * 
     * @param args 명령어 인수
     */
    private void lsTree(String[] args) {
        String hash = args[2];
        
        try {
            byte[] data = GitObjectUtil.readGitObjectFile(hash);
            byte[] decompressed = decompressData(data);
            
            // 헤더 스킵 (예: "tree 123\0")
            int headerEnd = findHeaderEnd(decompressed);
            
            List<String> files = parseTreeEntries(decompressed, headerEnd);
            
            for (String file : files) {
                System.out.println(file);
            }
        } catch (Exception e) {
            throw new RuntimeException("트리 객체 처리 실패", e);
        }
    }
    
    /**
     * 데이터를 압축 해제합니다.
     */
    private byte[] decompressData(byte[] compressed) throws DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(compressed);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        inflater.end();
        
        return outputStream.toByteArray();
    }
    
    /**
     * 헤더의 끝 위치를 찾습니다.
     */
    private int findHeaderEnd(byte[] decompressed) {
        for (int i = 0; i < decompressed.length; i++) {
            if (decompressed[i] == 0) {
                return i + 1;
            }
        }
        return 0;
    }
    
    /**
     * 트리 엔트리들을 파싱합니다.
     */
    private List<String> parseTreeEntries(byte[] decompressed, int offset) {
        List<String> files = new ArrayList<>();
        
        while (offset < decompressed.length) {
            // 모드와 이름 읽기 (null 바이트까지)
            int nullIndex = findNextNull(decompressed, offset);
            if (nullIndex == -1) break;
            
            String modeAndName = new String(decompressed, offset, nullIndex - offset, StandardCharsets.UTF_8);
            int spaceIndex = modeAndName.indexOf(' ');
            if (spaceIndex != -1) {
                String name = modeAndName.substring(spaceIndex + 1);
                files.add(name);
            }
            
            // 20바이트 SHA-1 해시 스킵
            offset = nullIndex + 1 + 20;
        }
        
        return files;
    }
    
    /**
     * 다음 null 바이트의 위치를 찾습니다.
     */
    private int findNextNull(byte[] data, int startIndex) {
        for (int i = startIndex; i < data.length; i++) {
            if (data[i] == 0) {
                return i;
            }
        }
        return -1;
    }
} 