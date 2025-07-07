package command;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * git init 명령어 구현
 */
public class InitCommand implements GitCommand {
    
    @Override
    public void execute(String[] args) {
        initGitRepository();
    }
    
    /**
     * Git 저장소를 초기화합니다.
     */
    private void initGitRepository() {
        final File root = new File(".git");
        new File(root, "objects").mkdirs();
        new File(root, "refs").mkdirs();
        final File head = new File(root, "HEAD");

        try {
            head.createNewFile();
            Files.write(head.toPath(), "ref: refs/heads/main\n".getBytes());
            System.out.println("Initialized git directory");
        } catch (IOException e) {
            throw new RuntimeException("Git 저장소 초기화 실패", e);
        }
    }
} 