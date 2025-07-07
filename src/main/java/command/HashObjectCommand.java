package command;

import util.FileUtil;
import util.GitObjectUtil;
import util.HashUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * git hash-object 명령어 구현
 */
public class HashObjectCommand implements GitCommand {
    
    @Override
    public void execute(String[] args) {
        if (args.length < 3 || !args[1].equals("-w")) {
            System.out.println("hash-object 명령어에는 -w 옵션과 파일 경로가 필요합니다.");
            return;
        }
        
        hashObject(args);
    }
    
    /**
     * 파일의 해시를 계산하고 Git 객체로 저장합니다.
     * 
     * @param args 명령어 인수
     */
    private void hashObject(String[] args) {
        try {
            String fileName = args[2];
            File file = new File(fileName);
            if (!file.exists() || !file.isFile()) {
                System.out.println("파일이 존재하지 않거나 유효하지 않습니다: " + fileName);
                return;
            }

            byte[] content = Files.readAllBytes(file.toPath());
            String header = "blob " + content.length + "\0";
            byte[] fullContent = FileUtil.concatenate(header.getBytes(StandardCharsets.UTF_8), content);

            String sha1 = HashUtil.computeSHA1(fullContent);
            GitObjectUtil.writeGitObject(sha1, fullContent);
            
            System.out.println(sha1);

        } catch (IOException e) {
            throw new RuntimeException("파일 읽기 실패: " + args[2], e);
        }
    }
} 