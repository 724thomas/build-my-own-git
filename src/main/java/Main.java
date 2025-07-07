import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("명령어를 입력해주세요.");
            return;
        }

        final String command = args[0];

        switch (command) {
            case "init" -> initGitRepository();
            case "cat-file" -> catFile(args);
            case "hash-object" -> hashObject(args);
            default -> System.out.println("Unknown command: " + command);
        }
    }

    private static void hashObject(String[] args) {
        if (args.length < 2) {
            System.out.println("hash-object 명령어에는 충분한 인수가 필요합니다.");
            return;
        }

        final String filePath = args[1];
        try {
            byte[] content = Files.readAllBytes(Paths.get(filePath));
            String objectHash = hashContent(content);
            System.out.println("객체 해시: " + objectHash);
        } catch (IOException e) {
            throw new RuntimeException("파일 읽기 실패: " + filePath, e);
        }
    }

    private static String hashContent(byte[] content) {
        // Git 객체 해시 계산 로직
        String header = "blob " + content.length + "\0";
        byte[] headerBytes = header.getBytes();
        byte[] combined = new byte[headerBytes.length + content.length];
        System.arraycopy(headerBytes, 0, combined, 0, headerBytes.length);
        System.arraycopy(content, 0, combined, headerBytes.length, content.length);

        // SHA-1 해시 계산
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(combined);
            return bytesToHex(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 알고리즘을 찾을 수 없습니다.", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
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