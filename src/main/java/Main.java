import command.GitCommand;
import command.GitCommandFactory;

import java.util.Arrays;

/**
 * Java로 구현한 Git 클론
 * 
 * CodeCrafters Git Challenge 프로젝트입니다.
 * 기본적인 Git 명령어들을 Java로 구현합니다.
 * 
 * 지원하는 명령어:
 * - init: Git 저장소 초기화
 * - cat-file: Git 객체 내용 출력
 * - hash-object: 파일을 Git 객체로 변환하고 해시 출력
 * - ls-tree: 트리 객체의 내용 나열
 * - write-tree: 현재 디렉토리의 트리 객체 생성
 * - commit-tree: 커밋 객체 생성
 * - clone: 원격 저장소 복제
 */
public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        final String commandName = args[0];
        GitCommand command = GitCommandFactory.getCommand(commandName);
        
        if (command != null) {
            command.execute(args);
        } else {
            System.err.println("알 수 없는 명령어: " + commandName);
            printUsage();
        }
    }
    
    /**
     * 사용법을 출력합니다.
     */
    private static void printUsage() {
        System.out.println("사용법: java Main <명령어> [옵션...]");
        System.out.println();
        System.out.println("지원하는 명령어:");
        String[] supportedCommands = GitCommandFactory.getSupportedCommands();
        Arrays.sort(supportedCommands);
        for (String cmd : supportedCommands) {
            System.out.println("  " + cmd);
        }
    }
}