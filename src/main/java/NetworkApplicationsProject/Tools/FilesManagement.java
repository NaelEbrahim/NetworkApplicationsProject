package NetworkApplicationsProject.Tools;

import NetworkApplicationsProject.CustomExceptions.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FilesManagement {

    private static final String UPLOAD_DIR = "C:\\Users\\LEGION\\OneDrive\\Desktop\\networkApplicationsProject\\Files";

    public static File uploadSingleFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            File dir = new File(UPLOAD_DIR);
            boolean dirIsCreated = false;
            // Create Directory If Not Exist
            if (!dir.exists()) {
                dirIsCreated = dir.mkdirs();
            }
            if (dir.exists() || dirIsCreated) {
                File uploadedFile = new File(dir, Objects.requireNonNull(file.getOriginalFilename()));
                file.transferTo(uploadedFile);
                return uploadedFile;//.getAbsolutePath();
            } else {
                throw new CustomException("Directory Not exist And Can not Created!", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (IOException e) {
            throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static List<String> uploadMultipleFile(List<MultipartFile> files) {

        List<String> filePaths = new ArrayList<>();

        if (files == null || files.isEmpty()) {
            return null;
        }
        try {
            File dir = new File(UPLOAD_DIR);
            boolean dirIsCreated = false;
            // Create Directory If Not Exist
            if (!dir.exists()) {
                dirIsCreated = dir.mkdirs();
            }
            if (dir.exists() || dirIsCreated) {
                for (MultipartFile element : files) {
                    if (element != null && !element.isEmpty()) {
                        File uploadedFile = new File(dir, Objects.requireNonNull(element.getOriginalFilename()));
                        element.transferTo(uploadedFile);
                        filePaths.add(uploadedFile.getAbsolutePath());
                    }
                }
            } else {
                filePaths.add("Directory Not exist And Can not Created!");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
        return filePaths;
    }

    public static boolean areFilesIdentical(String file1, MultipartFile file2) throws IOException, NoSuchAlgorithmException {
        // Get hashes of both files
        String hash1 = getFileChecksum(file1);
        String hash2 = getFileChecksum(file2);
        // Compare the two hashes
        return hash1.equals(hash2);
    }

    private static String getFileChecksum(String filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] byteArray = new byte[1024];
            int bytesCount;

            // Read the file's data and update the MessageDigest
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }
        // Convert the byte hash to a hexadecimal string
        StringBuilder sb = new StringBuilder();
        for (byte b : digest.digest()) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String getFileChecksum(MultipartFile multipartFile) throws IOException, NoSuchAlgorithmException {
        // Check if the MultipartFile is not null and not empty
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalArgumentException("MultipartFile must not be null or empty");
        }

        // Create a MessageDigest instance for SHA-256
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        // Read the MultipartFile's data and update the MessageDigest
        try (InputStream inputStream = multipartFile.getInputStream()) {
            byte[] byteArray = new byte[1024];
            int bytesCount;

            // Read the file's data and update the MessageDigest
            while ((bytesCount = inputStream.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }

        // Convert the byte hash to a hexadecimal string
        StringBuilder sb = new StringBuilder();
        for (byte b : digest.digest()) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}