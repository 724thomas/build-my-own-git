import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.zip.DataFormatException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("명령어를 입력해주세요.");
            return;
        }

        final String command = args[0];
        if (Objects.equals(command, "init")) {
            initGitRepository();
        } else if (Objects.equals(command, "cat-file")) {
            catFile(args);
        } else if (Objects.equals(command, "hash-object")) {
            hashObject(args);
        } else if (Objects.equals(command, "ls-tree")) {
            lsTree(args);
        } else if (Objects.equals(command, "write-tree")) {
            String treeSha = writeTree(new File("."));
            System.out.println(treeSha);
        } else if (Objects.equals(command, "commit-tree")) {
            commitTree(args);
        } else if (Objects.equals(command, "clone")) {
            String repoUrl = args[1];
            String targetDir = args[2];
            cloneRepo(repoUrl, targetDir);
        } else {
            System.out.println("알 수 없는 명령어: " + command);
        }
    }

    private static void cloneRepo(String repoURL, String targetDir) {
        try {
            Git.cloneRepository().setURI(repoURL).setDirectory(new File(targetDir)).call();
        }
        catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    private static void commitTree(String[] args) {

        String treeSha = args[1];
        List<String> parentShas = new ArrayList<>();
        for (int i = 2; i < args.length - 1; i++) {
            if (args[i].equals("-p")) {
                parentShas.add(args[i + 1]);
            }
        }
        String commitMessage = args[args.length - 1];

        String authorName = "sabya";
        String authorEmail = "sabya@sachi.com";
        String committerName = "Committer Name";
        String committerEmail = "sabya@abcd.com";
        long timestamp = System.currentTimeMillis() / 1000;

        String parentCommitShas = String.join(" ", parentShas);
        String commitContent = String.format("tree %s\n", treeSha);
        if (!parentCommitShas.isEmpty()) {
            commitContent += String.format("parent %s\n", parentCommitShas);
        }
        commitContent += String.format("author %s <%s> %d +0000\n", authorName, authorEmail, timestamp);
        commitContent += String.format("committer %s <%s> %d +0000\n", committerName, committerEmail, timestamp);
        commitContent += "\n" + commitMessage + "\n";

        byte[] commitBytes = commitContent.getBytes(StandardCharsets.UTF_8);
        byte[] header = ("commit " + commitBytes.length + "\0").getBytes(StandardCharsets.UTF_8);
        byte[] commitObject = concatArr(header, commitBytes);
        String commitHash = computeSHA1(commitObject);
        writeGitObject(commitHash, commitObject);

        System.out.println(commitHash);

    }

    private static void writeGitObject(String commitHash, byte[] commitObject) {
        String dir = ".git/objects/" + commitHash.substring(0, 2);
        String filePath = dir + "/" + commitHash.substring(2);
        new File(dir).mkdirs();

        try (FileOutputStream fos = new FileOutputStream(filePath);
             DeflaterOutputStream dos = new DeflaterOutputStream(fos)) {
            dos.write(commitObject);
            dos.finish();
        } catch (IOException e) {
            throw new RuntimeException("Git 객체 파일 쓰기 실패: " + filePath, e);
        }
    }

    private static byte[] concatArr(byte[] header, byte[] commitBytes) {
        byte[] commitObject = new byte[header.length + commitBytes.length];
        System.arraycopy(header, 0, commitObject, 0, header.length);
        System.arraycopy(commitBytes, 0, commitObject, header.length, commitBytes.length);
        return commitObject;
    }

    private static String writeTree(File dir) {
        File[] files = dir.listFiles((d,name) -> !name.equals(".git"));
        if (files == null) return null;

        Arrays.sort(files, (f1, f2) -> f1.getName().compareTo(f2.getName()));
        ByteArrayOutputStream treeContent = new ByteArrayOutputStream();

        try {
            for (File file : files) {
                String mode, sha, name = file.getName();

                if (file.isDirectory()) {
                    mode = "40000";
                    sha = writeTree(file); // Recursive!
                } else {
                    byte[] content = Files.readAllBytes(file.toPath());
                    String header = "blob " + content.length + "\0";
                    byte[] blob = concatenate(header.getBytes(StandardCharsets.UTF_8), content);
                    sha = computeSHA1(blob);

                    String dirName = ".git/objects/" + sha.substring(0, 2);
                    String fileName = sha.substring(2);
                    File objectFile = new File(dirName + "/" + fileName);
                    if (!objectFile.exists()) {
                        new File(dirName).mkdirs();
                        try (FileOutputStream fos = new FileOutputStream(objectFile);
                             DeflaterOutputStream dos = new DeflaterOutputStream(fos)) {
                            dos.write(blob);
                        }
                    }
                    mode = "100644";
                }

                treeContent.write((mode + " " + name).getBytes(StandardCharsets.UTF_8));
                treeContent.write(0);
                treeContent.write(hexToBytes(sha));
            }
        } catch (IOException e) {
            throw new RuntimeException("객체 파일 쓰기 실패", e);
        }

        byte[] raw = treeContent.toByteArray();
        String treeHeader = "tree " + raw.length + "\0";
        byte[] fullTree = concatenate(treeHeader.getBytes(StandardCharsets.UTF_8), raw);
        String treeSha = computeSHA1(fullTree);

        String treeDir = ".git/objects/" + treeSha.substring(0, 2);
        String treeFile = treeSha.substring(2);
        new File(treeDir).mkdirs();
        try (FileOutputStream fos = new FileOutputStream(treeDir + "/" + treeFile);
             DeflaterOutputStream dos = new DeflaterOutputStream(fos)) {
            dos.write(fullTree);
        } catch (IOException e) {
            throw new RuntimeException("트리 객체 파일 쓰기 실패", e);
        }

        return treeSha;
    }

    private static byte[] hexToBytes(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            bytes[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return bytes;
    }

    private static void lsTree(String[] args) {
        String hash = args[2];
        final String folderName = hash.substring(0, 2);
        final String fileName = hash.substring(2);

        File objectFile = new File(".git/objects/" + folderName + "/" + fileName);

        try {
            byte[] compressed = Files.readAllBytes(objectFile.toPath());
            Inflater inflater = new Inflater();

            inflater.setInput(compressed);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            inflater.end();

            byte[] decompressed = outputStream.toByteArray();
            
            // 헤더 스킵 (예: "tree 123\0")
            int headerEnd = 0;
            for (int i = 0; i < decompressed.length; i++) {
                if (decompressed[i] == 0) {
                    headerEnd = i + 1;
                    break;
                }
            }

            List<String> files = new ArrayList<>();
            int offset = headerEnd;
            
            while (offset < decompressed.length) {
                // 모드와 이름 읽기 (null 바이트까지)
                int nullIndex = -1;
                for (int i = offset; i < decompressed.length; i++) {
                    if (decompressed[i] == 0) {
                        nullIndex = i;
                        break;
                    }
                }
                
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

            for (String file : files) {
                System.out.println(file);
            }
        } catch (IOException e) {
            throw new RuntimeException("객체 파일 읽기 실패: " + objectFile.getPath(), e);
        } catch (DataFormatException e) {
            throw new RuntimeException("압축 해제 실패: " + objectFile.getPath(), e);
        }
    }

    private static void hashObject(String[] args) {
        if (args.length < 3 || !args[1].equals("-w")) {
            System.out.println("hash-object 명령어에는 -w 옵션과 파일 경로가 필요합니다.");
        } else {
            try {
                String fileName = args[2];
                File file = new File(fileName);
                if (!file.exists() || !file.isFile()) {
                    System.out.println("파일이 존재하지 않거나 유효하지 않습니다: " + fileName);
                    return;
                }

                byte[] content = Files.readAllBytes(file.toPath());
                String header = "blob " + content.length + "\0";
                byte[] fullContent = concatenate(header.getBytes(), content);

                String sha1 = computeSHA1(fullContent);

                String dir = ".git/objects/" + sha1.substring(0, 2);
                String filePath = dir + "/" + sha1.substring(2);
                new File(dir).mkdirs();

                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    DeflaterOutputStream dos = new DeflaterOutputStream(fos); {
                        dos.write(fullContent);
                        dos.finish();
                    }
                }
                System.out.println(sha1);

            } catch (IOException e) {
                throw new RuntimeException("파일 읽기 실패: " + args[2], e);
            }
        }
    }

    private static byte[] concatenate(byte[] header, byte[] content) {
        byte[] fullContent = new byte[header.length + content.length];
        System.arraycopy(header, 0, fullContent, 0, header.length);
        System.arraycopy(content, 0, fullContent, header.length, content.length);
        return fullContent;
    }

    private static String computeSHA1(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 알고리즘을 찾을 수 없습니다.", e);
        }
    }

    private static void initGitRepository() {
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

    private static void catFile(String[] args) {
        if (args.length < 3) {
            System.out.println("cat-file 명령어에는 충분한 인수가 필요합니다.");
            return;
        }

        final String objectHash = args[2];
        final String objectFolder = objectHash.substring(0, 2);
        final String objectFilename = objectHash.substring(2);
        final String objectPath = ".git/objects/" + objectFolder + "/" + objectFilename;

        try {
            byte[] data = Files.readAllBytes(Paths.get(objectPath));
            String decompressedContent = decompressGitObject(data);
            System.out.print(decompressedContent.substring(decompressedContent.indexOf("\0") + 1));
        } catch (IOException e) {
            throw new RuntimeException("객체 파일 읽기 실패: " + objectPath, e);
        }
    }

    private static String decompressGitObject(byte[] data) {
        Inflater inflater = new Inflater();
        inflater.setInput(data);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            return outputStream.toString("UTF-8");
        } catch (DataFormatException e) {
            throw new RuntimeException("Git 객체 압축 해제 실패", e);
        } catch (IOException e) {
            throw new RuntimeException("압축 해제 중 IO 오류", e);
        } finally {
            inflater.end();
        }
    }
}