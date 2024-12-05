package NetworkApplicationsProject.Tools;

import NetworkApplicationsProject.CustomExceptions.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class FilesManagement {

    public static final String UPLOAD_DIR = "C:\\Users\\LEGION\\OneDrive\\Desktop\\networkApplicationsProject\\Files";

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

        // Create group-specific directory
    public static File createGroupFolder(String groupSlug) {
            File groupDir = new File(UPLOAD_DIR, groupSlug);
            if (!groupDir.exists() && !groupDir.mkdirs()) {
                throw new CustomException("Failed to create group directory: " + groupSlug, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return groupDir;
    }

    public static void createFileVersion(String groupSlug, String fileName, File oldVersion, MultipartFile newFile, int version) {
        // Create the directory structure for versions: Files/groupName/filename/versions
        File versionsDir = new File(FilesManagement.UPLOAD_DIR, groupSlug + "/" + fileName.substring(0, fileName.lastIndexOf('.')) + "/versions");
        if (!versionsDir.exists()) {
            versionsDir.mkdirs();
        }

        // Create the new file name based on versioning (file_2.txt, file_3.txt, etc.)
        String versionedFileName = fileName.substring(0, fileName.lastIndexOf('.')) + "_" + version + fileName.substring(fileName.lastIndexOf('.'));
        File newVersionFile = new File(versionsDir, versionedFileName);

        try {
            // Save the new version of the file
            newFile.transferTo(newVersionFile);
        } catch (IOException e) {
            throw new CustomException("Error saving file version: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static void cleanupOldVersions(String groupSlug, String fileName, int maxVersions) {
        // Path to the versions directory
        File versionsDir = new File(UPLOAD_DIR, groupSlug + "/" + fileName.substring(0, fileName.lastIndexOf('.')) + "/versions");

        // Check if the directory exists
        if (!versionsDir.exists()) {
            return; // Nothing to clean up
        }

        // Get all files matching the versioned file pattern, including the original file
        File[] versionFiles = versionsDir.listFiles((dir, name) -> {
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
            String extension = fileName.substring(fileName.lastIndexOf('.'));
            return name.equals(fileName) || (name.startsWith(baseName + "_") && name.endsWith(extension));
        });

        if (versionFiles == null || versionFiles.length <= maxVersions) {
            return; // Nothing to clean up
        }

        // Sort files by version number (ascending), treating the original file as version 1
        Arrays.sort(versionFiles, Comparator.comparingInt(file -> {
            String fileNameOnly = file.getName();

            if (fileNameOnly.equals(fileName)) {
                return 1; // Original file is version 1
            }

            // Extract version number for other files
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
            String extension = fileName.substring(fileName.lastIndexOf('.'));
            String versionSuffix = fileNameOnly.replace(baseName, "").replace(extension, "").replace("_", "");

            try {
                return Integer.parseInt(versionSuffix);
            } catch (NumberFormatException e) {
                return Integer.MAX_VALUE; // Put improperly named files at the end
            }
        }));

        // Delete the oldest files to retain only the last `maxVersions`
        for (int i = 0; i < versionFiles.length - maxVersions; i++) {
            if (!versionFiles[i].delete()) {
                throw new CustomException("Failed to delete old version: " + versionFiles[i].getName(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    public static boolean deleteFolder(File folder) {
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteFolder(file); // Recursive call for subfolders
                    } else {
                        if (!file.delete()) {
                            System.err.println("Failed to delete file: " + file.getAbsolutePath());
                            return false;
                        }
                    }
                }
            }
        }
        return folder.delete(); // Delete the empty folder itself
    }




}