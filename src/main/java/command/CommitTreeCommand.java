package command;

import util.FileUtil;
import util.GitObjectUtil;
import util.HashUtil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * git commit-tree 명령어 구현
 */
public class CommitTreeCommand implements GitCommand {
    
    private static final String AUTHOR_NAME = "sabya";
    private static final String AUTHOR_EMAIL = "sabya@sachi.com";
    private static final String COMMITTER_NAME = "Committer Name";
    private static final String COMMITTER_EMAIL = "sabya@abcd.com";
    
    @Override
    public void execute(String[] args) {
        if (args.length < 3) {
            System.out.println("commit-tree 명령어에는 트리 해시가 필요합니다.");
            return;
        }
        
        commitTree(args);
    }
    
    /**
     * 커밋 객체를 생성합니다.
     * 
     * @param args 명령어 인수
     */
    private void commitTree(String[] args) {
        String treeSha = args[1];
        List<String> parentShas = extractParentShas(args);
        String commitMessage = args[args.length - 1];

        long timestamp = System.currentTimeMillis() / 1000;
        String commitContent = buildCommitContent(treeSha, parentShas, commitMessage, timestamp);

        byte[] commitBytes = commitContent.getBytes(StandardCharsets.UTF_8);
        byte[] header = ("commit " + commitBytes.length + "\0").getBytes(StandardCharsets.UTF_8);
        byte[] commitObject = FileUtil.concatenate(header, commitBytes);
        
        String commitHash = HashUtil.computeSHA1(commitObject);
        GitObjectUtil.writeGitObject(commitHash, commitObject);

        System.out.println(commitHash);
    }
    
    /**
     * 부모 커밋 SHA들을 추출합니다.
     */
    private List<String> extractParentShas(String[] args) {
        List<String> parentShas = new ArrayList<>();
        for (int i = 2; i < args.length - 1; i++) {
            if (args[i].equals("-p")) {
                parentShas.add(args[i + 1]);
            }
        }
        return parentShas;
    }
    
    /**
     * 커밋 내용을 구성합니다.
     */
    private String buildCommitContent(String treeSha, List<String> parentShas, String commitMessage, long timestamp) {
        StringBuilder content = new StringBuilder();
        
        content.append(String.format("tree %s\n", treeSha));
        
        for (String parentSha : parentShas) {
            content.append(String.format("parent %s\n", parentSha));
        }
        
        content.append(String.format("author %s <%s> %d +0000\n", AUTHOR_NAME, AUTHOR_EMAIL, timestamp));
        content.append(String.format("committer %s <%s> %d +0000\n", COMMITTER_NAME, COMMITTER_EMAIL, timestamp));
        content.append("\n").append(commitMessage).append("\n");
        
        return content.toString();
    }
} 