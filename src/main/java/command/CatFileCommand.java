package command;

import util.GitObjectUtil;

/**
 * git cat-file 명령어 구현
 */
public class CatFileCommand implements GitCommand {
    
    @Override
    public void execute(String[] args) {
        if (args.length < 3) {
            System.out.println("cat-file 명령어에는 충분한 인수가 필요합니다.");
            return;
        }
        
        catFile(args);
    }
    
    /**
     * 지정된 객체의 내용을 출력합니다.
     * 
     * @param args 명령어 인수
     */
    private void catFile(String[] args) {
        final String objectHash = args[2];
        
        byte[] data = GitObjectUtil.readGitObjectFile(objectHash);
        String decompressedContent = GitObjectUtil.decompressGitObject(data);
        System.out.print(decompressedContent.substring(decompressedContent.indexOf("\0") + 1));
    }
} 