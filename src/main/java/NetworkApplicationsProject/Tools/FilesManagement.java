package NetworkApplicationsProject.Tools;

import NetworkApplicationsProject.CustomExceptions.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FilesManagement {

    public static final String UPLOAD_DIR = "C:\\Users\\NAEL PC\\Desktop\\Learning\\SpringBoot\\Projects\\Language-center-app\\language-center-app\\Files";


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

}