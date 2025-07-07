package command;

import java.util.HashMap;
import java.util.Map;

/**
 * Git 명령어 팩토리 클래스
 * 명령어 문자열과 구현체를 매핑하여 관리합니다.
 */
public class GitCommandFactory {
    
    private static final Map<String, GitCommand> COMMANDS = new HashMap<>();
    
    static {
        COMMANDS.put("init", new InitCommand());
        COMMANDS.put("cat-file", new CatFileCommand());
        COMMANDS.put("hash-object", new HashObjectCommand());
        COMMANDS.put("ls-tree", new LsTreeCommand());
        COMMANDS.put("write-tree", new WriteTreeCommand());
        COMMANDS.put("commit-tree", new CommitTreeCommand());
        // COMMANDS.put("clone", new CloneCommand()); // JGit dependency required
    }
    
    /**
     * 명령어 문자열에 해당하는 GitCommand 구현체를 반환합니다.
     * 
     * @param commandName 명령어 이름
     * @return GitCommand 구현체, 존재하지 않으면 null
     */
    public static GitCommand getCommand(String commandName) {
        return COMMANDS.get(commandName);
    }
    
    /**
     * 지원되는 모든 명령어 이름을 반환합니다.
     * 
     * @return 명령어 이름 배열
     */
    public static String[] getSupportedCommands() {
        return COMMANDS.keySet().toArray(new String[0]);
    }
} 