package command;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;

/**
 * git clone 명령어 구현
 */
public class CloneCommand implements GitCommand {
    
    @Override
    public void execute(String[] args) {
        if (args.length < 3) {
            System.out.println("clone 명령어에는 저장소 URL과 대상 디렉토리가 필요합니다.");
            return;
        }
        
        String repoUrl = args[1];
        String targetDir = args[2];
        cloneRepository(repoUrl, targetDir);
    }
    
    /**
     * 원격 저장소를 복제합니다.
     * 
     * @param repoUrl 저장소 URL
     * @param targetDir 대상 디렉토리
     */
    private void cloneRepository(String repoUrl, String targetDir) {
        try {
            Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(new File(targetDir))
                .call();
            System.out.println("저장소 복제가 완료되었습니다: " + targetDir);
        } catch (GitAPIException e) {
            throw new RuntimeException("저장소 복제 실패: " + e.getMessage(), e);
        }
    }
} 