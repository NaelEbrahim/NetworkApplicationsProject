package NetworkApplicationsProject.Services;

import NetworkApplicationsProject.CustomExceptions.CustomException;
import NetworkApplicationsProject.DTO.Requset.FilesRequests.AddFileRequest;
import NetworkApplicationsProject.DTO.Requset.FilesRequests.CheckInFilesRequest;
import NetworkApplicationsProject.DTO.Requset.FilesRequests.CheckOutFilesRequest;
import NetworkApplicationsProject.Enums.ActionsEnum;
import NetworkApplicationsProject.Enums.RolesEnum;
import NetworkApplicationsProject.Models.*;
import NetworkApplicationsProject.Repositories.*;
import NetworkApplicationsProject.Tools.FilesManagement;
import NetworkApplicationsProject.Tools.HandleCurrentUserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FilesService {

    @Autowired
    GroupUserRepository groupUserRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    FileRepository fileRepository;

    @Autowired
    ActivityRepository activityRepository;

    @Autowired
    NotificationService notificationService;

    @Autowired
    NotificationsRepository notificationsRepository;


    public String addFile(AddFileRequest fileRequest) {
        if (fileRequest.getGroupId() == null) {
            throw new CustomException("Group ID must not be null", HttpStatus.BAD_REQUEST);
        }

        Optional<GroupModel> targetGroup = groupRepository.findById(fileRequest.getGroupId());
        if (targetGroup.isEmpty()) {
            throw new CustomException("Group with this ID not found", HttpStatus.NOT_FOUND);
        }

        if (fileRequest.getFile() == null || fileRequest.getFile().isEmpty()) {
            throw new CustomException("Please upload a valid file", HttpStatus.BAD_REQUEST);
        }

        // Permission check: User must be admin or owner to upload files
        boolean isAdmin = HandleCurrentUserSession.getCurrentUserRole().equals(RolesEnum.SUPER_ADMIN);
        boolean isGroupOwner = targetGroup.get().getGroupOwner().getId().equals(HandleCurrentUserSession.getCurrentUser().getId());

        if (!isAdmin && !isGroupOwner) {
            if (!checkIsCurrentUserInGroup(targetGroup.get())) {
                throw new CustomException("You are not a member of this group", HttpStatus.FORBIDDEN);
            }
        }

        // Validate file content type
        String contentType = fileRequest.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            throw new CustomException("File content type must not be null or empty", HttpStatus.BAD_REQUEST);
        }

        String groupSlug = targetGroup.get().getSlug();
        String fileName = fileRequest.getFile().getOriginalFilename();

        // Get the version number for the original file (first version)
        int version = getNextVersion(groupSlug, fileName); // Use getNextVersion here

        // Create the directory structure for the file: Files/groupName/filename/versions
        File versionsDir = new File(FilesManagement.UPLOAD_DIR, groupSlug + "/" + fileName.substring(0, fileName.lastIndexOf('.')) + "/versions");
        if (!versionsDir.exists()) {
            versionsDir.mkdirs();
        }

        // Upload the original file as the first version (filename.txt)
        File uploadedFile = new File(versionsDir, fileName);
        try {
            fileRequest.getFile().transferTo(uploadedFile);
        } catch (IOException e) {
            throw new CustomException("Error saving file", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Save the file path in the database (pointing to Files/groupName/filename/versions/filename.txt)
        String filePath = uploadedFile.getAbsolutePath();

        // Save the file in the database (first version)
        FileModel newFile = new FileModel();
        newFile.setIsAvailable(true);  // File is available
        newFile.setFilePath(filePath);  // Path to the original file
        newFile.setGroup(targetGroup.get());
        newFile.setCreatedAt(LocalDateTime.now());
        newFile.setFileName(fileName);
        newFile.setContentType(contentType);
        newFile.setOwner(HandleCurrentUserSession.getCurrentUser());
        newFile.setRealVersion(1);  // Set version to 1 for the original file
        fileRepository.save(newFile);

        return "File added successfully...";
    }

    private int getNextVersion(String groupSlug, String fileName) {
        // Look inside the versions folder for the given group and file
        File versionsDir = new File(FilesManagement.UPLOAD_DIR, groupSlug + "/" + fileName.substring(0, fileName.lastIndexOf('.')) + "/versions");

        // Create the versions directory if it doesn't exist
        if (!versionsDir.exists()) {
            versionsDir.mkdirs();
        }

        // Get all files matching the base file name
        File[] versionFiles = versionsDir.listFiles((dir, name) -> {
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
            String extension = fileName.substring(fileName.lastIndexOf('.'));
            return name.equals(fileName) || (name.startsWith(baseName + "_") && name.endsWith(extension));
        });

        int maxVersion = 1; // Default to version 1 if no versions exist

        if (versionFiles != null) {
            for (File file : versionFiles) {
                String fileNameOnly = file.getName();

                if (fileNameOnly.equals(fileName)) {
                    // Original file, treat as version 1
                    maxVersion = Math.max(maxVersion, 1);
                } else {
                    // Extract version suffix from files like `filename_2.txt`, `filename_3.txt`
                    String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                    String extension = fileName.substring(fileName.lastIndexOf('.'));
                    String versionSuffix = fileNameOnly.replace(baseName, "").replace(extension, "").replace("_", "");

                    try {
                        int version = Integer.parseInt(versionSuffix);
                        maxVersion = Math.max(maxVersion, version);
                    } catch (NumberFormatException e) {
                        // Ignore files that don't follow the versioning convention
                    }
                }
            }
        }

        return maxVersion + 1; // Return the next available version number
    }

    public String deleteFile(Integer fileId) {
        Optional<FileModel> targetFile = fileRepository.findById(fileId);
        if (targetFile.isPresent()) {
            FileModel file = targetFile.get();

            // Check if the current user is authorized to delete the file
            if (HandleCurrentUserSession.getCurrentUser().getId().equals(file.getOwner().getId())) {
                // Determine the folder for the file
                File fileFolder = new File(file.getFilePath()).getParentFile().getParentFile(); // Get the folder of the file

                // Delete the folder and its contents
                if (fileFolder.exists()) {
                    boolean deleted = FilesManagement.deleteFolder(fileFolder);
                    if (!deleted) {
                        throw new CustomException("Failed to delete the folder and its contents: " + fileFolder.getAbsolutePath(),
                                HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                } else {
                    throw new CustomException("File folder not found: " + fileFolder.getAbsolutePath(), HttpStatus.NOT_FOUND);
                }

                // Delete the file record from the database
                fileRepository.delete(file);
                return "File and its folder deleted successfully.";
            } else {
                throw new CustomException("You cannot delete this file because you are not the owner", HttpStatus.UNAUTHORIZED);
            }
        } else {
            throw new CustomException("File with this ID not found", HttpStatus.BAD_REQUEST);
        }
    }

    public List<Map<String, Object>> getFiles(Integer groupId) {
        Optional<GroupModel> targetGroup = groupRepository.findById(groupId);
        if (targetGroup.isPresent()) {
            UserModel currentUser = HandleCurrentUserSession.getCurrentUser();
            if (currentUser.getRole().equals(RolesEnum.SUPER_ADMIN)
                    || currentUser.getId().equals(targetGroup.get().getGroupOwner().getId())
                    || checkIsCurrentUserInGroup(targetGroup.get())) {

                List<FileModel> files = fileRepository.findByGroupId(targetGroup.get().getId());
                List<Map<String, Object>> result = new ArrayList<>();

                for (FileModel file : files) {
                    Map<String, Object> fileData = new HashMap<>();
                    fileData.put("file", file); // Include the main file details
                    fileData.put("versions", getFileVersions(file)); // Add versions
                    result.add(fileData);
                }

                return result;
            } else {
                throw new CustomException("You don't have permissions to access these files", HttpStatus.UNAUTHORIZED);
            }
        } else {
            throw new CustomException("Group with this ID not found", HttpStatus.NOT_FOUND);
        }
    }

    private List<String> getFileVersions(FileModel file) {
        List<String> versions = new ArrayList<>();

        // Construct the base path for the file's versions directory
        String groupSlug = file.getGroup().getSlug();
        String fileName = file.getFileName();
        String baseName = fileName.contains(".")
                ? fileName.substring(0, fileName.lastIndexOf('.'))
                : fileName;

        String versionsPath = FilesManagement.UPLOAD_DIR + "/" + groupSlug + "/" + baseName + "/versions";
        File versionsDir = new File(versionsPath);

        if (versionsDir.exists() && versionsDir.isDirectory()) {
            File[] versionFiles = versionsDir.listFiles();
            if (versionFiles != null) {
                for (File versionFile : versionFiles) {
                    versions.add(versionFile.getAbsolutePath());
                }
            }
        }

        return versions;
    }

    @Transactional
    public ResponseEntity<List<FileModel>> checkInFilesOptimistically(CheckInFilesRequest fileRequest) {
        Optional<GroupModel> targetGroup = groupRepository.findById(fileRequest.getGroupId());
        if (targetGroup.isPresent()) {
            UserModel currentUser = HandleCurrentUserSession.getCurrentUser();
            if (currentUser.getId().equals(targetGroup.get().getGroupOwner().getId())
                    || checkIsCurrentUserInGroup(targetGroup.get())) {

                List<FileModel> files = fileRepository.findAllById(fileRequest.getFileIds());
                if (files.size() == fileRequest.getFileIds().size()) {
                    for (FileModel file : files) {
                        // Check that all files are available (not reserved)
                        if (!file.getIsAvailable()) {
                            throw new CustomException("File with name : " + file.getFileName() + " is already reserved.", HttpStatus.LOCKED);
                        }
                    }
                    // Reserve files
                    files.forEach(file -> {
                        file.setIsAvailable(false);
                        file.setReserver(currentUser);
                        // handle Notification
                        String notification = currentUser.getUserName() + " has been " + ActionsEnum.CHECKED_IN + " file with name: " + file.getFileName();
                        handleNotification(targetGroup.get(), notification);
                    });
                    // Save all to trigger optimistic locking
                    fileRepository.saveAll(files);
                    return new ResponseEntity<>(files, HttpStatus.OK);
                } else {
                    throw new CustomException("One or more entered file IDs not found", HttpStatus.NOT_FOUND);
                }
            } else {
                throw new CustomException("You must be owner or member in the group", HttpStatus.UNAUTHORIZED);
            }
        } else {
            throw new CustomException("Group with this ID not found", HttpStatus.NOT_FOUND);
        }
    }

    private boolean checkIsCurrentUserInGroup(GroupModel targetGroup) {
        //find Groups For target User
        List<GroupUserModel> currentUserGroups = groupUserRepository.findByUserId(HandleCurrentUserSession.getCurrentUser().getId());
        for (GroupUserModel element : currentUserGroups) {
            if (element.getGroupModel().getId().equals(targetGroup.getId())) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public ResponseEntity<List<FileModel>> checkOutFilesOptimistically(CheckOutFilesRequest fileRequest) throws IOException, NoSuchAlgorithmException {
        Optional<GroupModel> targetGroup = groupRepository.findById(fileRequest.getGroupId());
        if (targetGroup.isPresent()) {
            UserModel currentUser = HandleCurrentUserSession.getCurrentUser();
            if (currentUser.getId().equals(targetGroup.get().getGroupOwner().getId())
                    || checkIsCurrentUserInGroup(targetGroup.get())) {

                List<FileModel> files = fileRepository.findAllById(fileRequest.getFileIds());
                if (files.size() == fileRequest.getFileIds().size()) {
                    for (FileModel file : files) {
                        if (file.getIsAvailable()) {
                            throw new CustomException("One or more files are not booked yet.", HttpStatus.BAD_REQUEST);
                        }
                        if (!file.getReserver().getId().equals(currentUser.getId())) {
                            throw new CustomException("you are not the reserver for one or more requested files", HttpStatus.UNAUTHORIZED);
                        }
                    }

                    if (fileRequest.getUpdatedFiles() != null && !fileRequest.getUpdatedFiles().isEmpty()) {
                        for (MultipartFile element : fileRequest.getUpdatedFiles()) {
                            FileModel currentFile = findElementInList(files, element);
                            boolean isFileChanged = false;
                            if (currentFile != null && !FilesManagement.areFilesIdentical(currentFile.getFilePath(), element)) {
                                isFileChanged = true;
                                // Use getNextVersion to determine the next version number
                                int newVersion = getNextVersion(targetGroup.get().getSlug(), currentFile.getFileName());

                                // Create the new version
                                FilesManagement.createFileVersion(
                                        targetGroup.get().getSlug(),
                                        currentFile.getFileName(),
                                        new File(currentFile.getFilePath()), // Old version
                                        element,                             // New file content
                                        newVersion                            // Increment version
                                );

                                // Call cleanup to maintain only the last 10 versions
                                FilesManagement.cleanupOldVersions(
                                        targetGroup.get().getSlug(),
                                        currentFile.getFileName(),
                                        10 // Retain only the last 10 versions
                                );

                                // Update file model with the new version path
                                String newFileName = currentFile.getFileName().substring(0, currentFile.getFileName().lastIndexOf('.'))
                                        + "_" + newVersion
                                        + currentFile.getFileName().substring(currentFile.getFileName().lastIndexOf('.'));

                                File updatedFile = new File(FilesManagement.UPLOAD_DIR, targetGroup.get().getSlug() + "/" + currentFile.getFileName().substring(0, currentFile.getFileName().lastIndexOf('.')) + "/versions/" + newFileName);
                                currentFile.setRealVersion(newVersion); // Update the real version
                                currentFile.setFilePath(updatedFile.getAbsolutePath()); // Update the path to the new file version
                                currentFile.setLastModifiedAt(LocalDateTime.now()); // Update the modified time
                                // handle Notification if file content changed
                                String notification = currentUser.getUserName() + " has been " + ActionsEnum.CHECKED_OUT + " file with name: " + currentFile.getFileName() + " with change file content";
                                handleNotification(targetGroup.get(), notification);
                                //
                            }
                            // Release the file (mark it as available)
                            Objects.requireNonNull(currentFile).setIsAvailable(true);
                            currentFile.setReserver(null);
                            if (!isFileChanged) {
                                // handle Notification if file content not changed
                                String notification = currentUser.getUserName() + " has been " + ActionsEnum.CHECKED_OUT + " file with name: " + currentFile.getFileName() + " withOut change file content";
                                handleNotification(targetGroup.get(), notification);
                                //
                            }
                        }
                        // Save all updated files to trigger optimistic locking
                        fileRepository.saveAll(files);
                        return new ResponseEntity<>(files, HttpStatus.OK);
                    } else {
                        throw new CustomException("Please upload valid files", HttpStatus.BAD_REQUEST);
                    }
                } else {
                    throw new CustomException("One or more entered file IDs not found", HttpStatus.NOT_FOUND);
                }
            } else {
                throw new CustomException("You must be owner or member in the group", HttpStatus.UNAUTHORIZED);
            }
        } else {
            throw new CustomException("Group with this ID not found", HttpStatus.NOT_FOUND);
        }
    }

    private FileModel findElementInList(List<FileModel> files, MultipartFile targetFile) {
        for (FileModel element : files) {
            if (element.getFileName().equals(targetFile.getOriginalFilename())) {
                return element;
            }
        }
        return null;
    }

    private void handleNotification(GroupModel groupModel, String notification) {
        // send Notification
        notificationService.sendNotification(groupModel.getId().toString(), notification);
        // store in DataBase
        notificationsRepository.save(NotificationsModel.builder().group(groupModel).content(notification).date(LocalDateTime.now()).build());
    }

    public List<Map<String, Object>> getUploadUserFiles(Integer groupId) {
        Optional<GroupModel> targetGroup = groupRepository.findById(groupId);
        if (targetGroup.isPresent()) {
            if (HandleCurrentUserSession.getCurrentUser().getRole().equals(RolesEnum.SUPER_ADMIN) ||
                    HandleCurrentUserSession.getCurrentUser().getId().equals(targetGroup.get().getGroupOwner().getId())
                    || checkIsCurrentUserInGroup(targetGroup.get())) {

                List<FileModel> files = fileRepository.findByOwnerId(HandleCurrentUserSession.getCurrentUser().getId());
                List<Map<String, Object>> result = new ArrayList<>();

                for (FileModel file : files) {
                    Map<String, Object> fileData = new HashMap<>();
                    fileData.put("file", file); // Include the main file details
                    fileData.put("versions", getFileVersions(file)); // Add versions
                    result.add(fileData);
                }

                return result;
            } else {
                throw new CustomException("You are not a member of this group", HttpStatus.FORBIDDEN);
            }
        } else {
            throw new CustomException("Group with this ID not found", HttpStatus.NOT_FOUND);
        }
    }

    public List<ActivityModel> getLogsByFile(Integer fileId, Integer groupId) {
        Optional<GroupModel> targetGroup = groupRepository.findById(groupId);
        Optional<FileModel> targetFile = fileRepository.findById(fileId);
        if (targetGroup.isPresent()) {
            UserModel currentUser = HandleCurrentUserSession.getCurrentUser();
            if (targetFile.isPresent()) {
                if (currentUser.getRole().equals(RolesEnum.SUPER_ADMIN)
                        || currentUser.getId().equals(targetGroup.get().getGroupOwner().getId())
                        || checkIsCurrentUserInGroup(targetGroup.get())
                ) {
                    return activityRepository.findByFileModelId(fileId);
                } else {
                    throw new CustomException("you don't have permissions to access to this logs", HttpStatus.UNAUTHORIZED);
                }
            } else {
                throw new CustomException("group with this id not found", HttpStatus.NOT_FOUND);
            }
        } else {
            throw new CustomException("group with this id not found", HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public List<ActivityModel> getLogsByUserAndGroup(Integer groupId) {
        // Fetch the target group
        Optional<GroupModel> targetGroup = groupRepository.findById(groupId);
        if (!targetGroup.isPresent()) {
            throw new CustomException("Group not found", HttpStatus.NOT_FOUND);
        }

        // Get the current user
        UserModel currentUser = HandleCurrentUserSession.getCurrentUser();

        // Check if the user has access to the group
        if (currentUser.getRole().equals(RolesEnum.SUPER_ADMIN)
                || currentUser.getId().equals(targetGroup.get().getGroupOwner().getId())
                || checkIsCurrentUserInGroup(targetGroup.get())) {
            // Fetch all activities related to the user and the group
            return activityRepository.findByUserAndGroup(currentUser.getId(), groupId);
        } else {
            throw new CustomException("You don't have permission to access these logs", HttpStatus.UNAUTHORIZED);
        }
    }

    @Transactional
    public List<ActivityModel> getLogsByUserAndFileInGroup(Integer fileId, Integer groupId) {
        // Fetch the target group and file
        Optional<GroupModel> targetGroup = groupRepository.findById(groupId);
        Optional<FileModel> targetFile = fileRepository.findById(fileId);

        if (!targetGroup.isPresent()) {
            throw new CustomException("Group not found", HttpStatus.NOT_FOUND);
        }

        if (!targetFile.isPresent()) {
            throw new CustomException("File not found", HttpStatus.NOT_FOUND);
        }

        // Get the current user
        UserModel currentUser = HandleCurrentUserSession.getCurrentUser();

        // Check if the current user has permission to access the group and file
        if (currentUser.getRole().equals(RolesEnum.SUPER_ADMIN)
                || currentUser.getId().equals(targetGroup.get().getGroupOwner().getId())
                || checkIsCurrentUserInGroup(targetGroup.get())) {

            // Fetch the activity logs for the user, file, and group
            return activityRepository.findByUserAndFileAndGroup(currentUser.getId(), fileId, groupId);
        } else {
            throw new CustomException("You don't have permission to access these logs", HttpStatus.UNAUTHORIZED);
        }
    }

    public List<String> getFileAndVersionsPaths(Integer groupId, Integer fileId) {
        // Fetch the target file
        Optional<FileModel> targetFile = fileRepository.findById(fileId);
        if (targetFile.isEmpty()) {
            throw new CustomException("File with this ID not found", HttpStatus.NOT_FOUND);
        }

        FileModel file = targetFile.get();

        // Validate group ID
        if (!file.getGroup().getId().equals(groupId)) {
            throw new CustomException("File does not belong to the specified group", HttpStatus.BAD_REQUEST);
        }
        boolean isAdmin = HandleCurrentUserSession.getCurrentUserRole().equals(RolesEnum.SUPER_ADMIN);
        boolean isGroupOwner = file.getGroup().getGroupOwner().getId().equals(HandleCurrentUserSession.getCurrentUser().getId());

        if (!isAdmin && !isGroupOwner) {
            if (!checkIsCurrentUserInGroup(file.getGroup())) {
                throw new CustomException("You don't have permission to access", HttpStatus.FORBIDDEN);
            }
        }
        // Construct the versions directory path
        String groupSlug = file.getGroup().getSlug();
        String fileName = file.getFileName();

        // Safely handle file name without extensions
        String baseName = fileName.contains(".")
                ? fileName.substring(0, fileName.lastIndexOf('.'))
                : fileName;

        String versionsPath = FilesManagement.UPLOAD_DIR + "/" + groupSlug + "/" + baseName + "/versions";

        // Collect all file paths from the versions directory
        File versionsDir = new File(versionsPath);
        List<String> filePaths = new ArrayList<>();

        if (versionsDir.exists() && versionsDir.isDirectory()) {
            File[] versionFiles = versionsDir.listFiles();
            if (versionFiles != null) {
                for (File versionFile : versionFiles) {
                    filePaths.add(versionFile.getAbsolutePath());
                }
            }
        } else {
            // Return an empty list if the directory doesn't exist
            return filePaths;
        }

        return filePaths;
    }

    public ResponseEntity<Resource> downloadFile(Integer fileId) {
        Optional<FileModel> targetFile = fileRepository.findById(fileId);
        if (targetFile.isPresent()) {
            FileModel fileModel = targetFile.get();
            String filePath = fileModel.getFilePath();

            // Ensure the file exists
            Path path = Paths.get(filePath).normalize();
            File file = path.toFile();
            if (!file.exists()) {
                throw new CustomException("File not found", HttpStatus.NOT_FOUND);
            }

            // Create a resource from the file
            Resource resource = new FileSystemResource(file);

            // Set headers for download
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } else {
            throw new CustomException("File not found", HttpStatus.NOT_FOUND);
        }
    }


}