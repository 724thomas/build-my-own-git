package command;

import util.FileUtil;
import util.GitObjectUtil;
import util.HashUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * git write-tree 명령어 구현
 */
public class WriteTreeCommand implements GitCommand {
    
    @Override
    public void execute(String[] args) {
        String treeSha = writeTree(new File("."));
        System.out.println(treeSha);
    }
    
    /**
     * 디렉토리의 트리 객체를 생성합니다.
     * 
     * @param dir 대상 디렉토리
     * @return 트리 객체의 SHA-1 해시
     */
    private String writeTree(File dir) {
        File[] files = dir.listFiles((d, name) -> !name.equals(".git"));
        if (files == null) return null;

        Arrays.sort(files, (f1, f2) -> f1.getName().compareTo(f2.getName()));
        ByteArrayOutputStream treeContent = new ByteArrayOutputStream();

        try {
            for (File file : files) {
                String mode, sha, name = file.getName();

                if (file.isDirectory()) {
                    mode = "40000";
                    sha = writeTree(file); // 재귀 호출
                } else {
                    mode = "100644";
                    sha = createBlobObject(file);
                }

                treeContent.write((mode + " " + name).getBytes(StandardCharsets.UTF_8));
                treeContent.write(0);
                treeContent.write(HashUtil.hexToBytes(sha));
            }
        } catch (IOException e) {
            throw new RuntimeException("트리 객체 생성 실패", e);
        }

        return createTreeObject(treeContent.toByteArray());
    }
    
    /**
     * 파일로부터 blob 객체를 생성합니다.
     */
    private String createBlobObject(File file) throws IOException {
        byte[] content = Files.readAllBytes(file.toPath());
        String header = "blob " + content.length + "\0";
        byte[] blob = FileUtil.concatenate(header.getBytes(StandardCharsets.UTF_8), content);
        String sha = HashUtil.computeSHA1(blob);

        // 이미 존재하지 않는 경우에만 저장
        String objectPath = ".git/objects/" + sha.substring(0, 2) + "/" + sha.substring(2);
        File objectFile = new File(objectPath);
        if (!objectFile.exists()) {
            GitObjectUtil.writeGitObject(sha, blob);
        }
        
        return sha;
    }
    
    /**
     * 트리 컨텐츠로부터 트리 객체를 생성합니다.
     */
    private String createTreeObject(byte[] treeContent) {
        String treeHeader = "tree " + treeContent.length + "\0";
        byte[] fullTree = FileUtil.concatenate(treeHeader.getBytes(StandardCharsets.UTF_8), treeContent);
        String treeSha = HashUtil.computeSHA1(fullTree);

        GitObjectUtil.writeGitObject(treeSha, fullTree);
        return treeSha;
    }
} 