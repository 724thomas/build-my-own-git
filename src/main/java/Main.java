import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.DeflaterOutputStream;
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
            case "ls-tree" -> lsTree(args);
            default -> System.out.println("Unknown command: " + command);
        }
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

            byte[] buffer = new byte[8192];
            StringBuilder sb = new StringBuilder();
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                sb.append(new String(buffer, 0, count));
            }

            inflater.end();

            int firstNullIndex = sb.indexOf("\0");
            int offset = firstNullIndex + 1;
            List<String> files = new ArrayList<>();

            while (sb.indexOf("\0", offset) != -1) {
                int nullIndex = sb.indexOf("\0", offset);
                int spaceIndex = sb.indexOf(" ", offset);

                String name = sb.substring(spaceIndex+1, nullIndex);
                files.add(name);
                offset = nullIndex + 1;
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