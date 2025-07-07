package command;

/**
 * Git 명령어를 처리하기 위한 인터페이스
 */
public interface GitCommand {
    /**
     * 명령어를 실행합니다.
     * 
     * @param args 명령어 인수
     */
    void execute(String[] args);
} 