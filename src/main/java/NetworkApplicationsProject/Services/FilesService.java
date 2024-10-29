package NetworkApplicationsProject.Services;

import NetworkApplicationsProject.CustomExceptions.CustomException;
import NetworkApplicationsProject.DTO.Requset.FileRequest;
import NetworkApplicationsProject.Enums.RolesEnum;
import NetworkApplicationsProject.Models.FileModel;
import NetworkApplicationsProject.Models.GroupModel;
import NetworkApplicationsProject.Models.GroupUserModel;
import NetworkApplicationsProject.Models.UserModel;
import NetworkApplicationsProject.Repositories.FileRepository;
import NetworkApplicationsProject.Repositories.GroupRepository;
import NetworkApplicationsProject.Repositories.GroupUserRepository;
import NetworkApplicationsProject.Tools.FilesManagement;
import NetworkApplicationsProject.Tools.HandleCurrentUserSession;
import org.apache.coyote.http11.filters.IdentityOutputFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FilesService {

    @Autowired
    GroupUserRepository groupUserRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    FileRepository fileRepository;

    public String addFile(FileRequest fileRequest) {
        if (fileRequest.getGroupId() == null) {
            throw new CustomException("group id must not be null", HttpStatus.BAD_REQUEST);
        }
        Optional<GroupModel> targetGroup = groupRepository.findById(fileRequest.getGroupId());
        if (targetGroup.isPresent()) {
            if (fileRequest.getFile() != null && !fileRequest.getFile().isEmpty()) {
                // Upload File To Server
                File uploadedFile = FilesManagement.uploadSingleFile(fileRequest.getFile());
                if (uploadedFile == null) {
                    throw new CustomException("something went wrong when store file", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                // Create New File
                FileModel newFile = new FileModel();

                newFile.setIsAvailable(true);
                newFile.setFilePath(uploadedFile.getAbsolutePath());
                newFile.setGroup(targetGroup.get());
                newFile.setCreatedAt(LocalDateTime.now());
                newFile.setFileName(uploadedFile.getName());
                newFile.setContentType(fileRequest.getContentType());
                newFile.setFileOwner(HandleCurrentUserSession.getCurrentUser());
                // Save File In DataBase
                fileRepository.save(newFile);
            } else {
                throw new CustomException("please upload valid file", HttpStatus.BAD_REQUEST);
            }
        } else {
            throw new CustomException("group with this id not found", HttpStatus.NOT_FOUND);
        }
        return "file Added Successfully";
    }

    public String deleteFile(Integer fileId) {
        Optional<FileModel> targetFile = fileRepository.findById(fileId);
        if (targetFile.isPresent()) {
            if (HandleCurrentUserSession.getCurrentUser().getId().equals(targetFile.get().getFileOwner().getId())) {
                fileRepository.delete(targetFile.get());
                return "file Deleted Successfully";
            } else {
                throw new CustomException("can not delete this file because you are not the owner", HttpStatus.UNAUTHORIZED);
            }
        } else {
            throw new CustomException("file with this id not found", HttpStatus.BAD_REQUEST);
        }
    }

    public List<FileModel> getFiles(Integer groupId) {
        Optional<GroupModel> targetGroup = groupRepository.findById(groupId);
        if (targetGroup.isPresent()) {
            UserModel currentUser = HandleCurrentUserSession.getCurrentUser();
            if (currentUser.getRole().equals(RolesEnum.SUPER_ADMIN)
                    || currentUser.getId().equals(targetGroup.get().getGroupOwner().getId())
                    || checkIsUserInGroup(targetGroup.get().getId())
            ) {
                return fileRepository.findByGroupId(targetGroup.get().getId());
            } else {
                throw new CustomException("you don't have permissions to access to this files", HttpStatus.UNAUTHORIZED);
            }
        } else {
            throw new CustomException("group with this id not found", HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public String checkInFilesOptimistically(FileRequest fileRequest) {
        Optional<GroupModel> targetGroup = groupRepository.findById(fileRequest.getGroupId());
        if (targetGroup.isPresent()) {
            if (HandleCurrentUserSession.getCurrentUser().getId().equals(targetGroup.get().getGroupOwner().getId())
                    || checkIsUserInGroup(targetGroup.get().getId())
            ) {
                List<FileModel> files = fileRepository.findAllById(fileRequest.getFileIds());
                // Check that all files are available
                for (FileModel file : files) {
                    if (!file.getIsAvailable()) {
                        throw new IllegalStateException("One or more files are already reserved.");
                    }
                }
                // Reserve files
                files.forEach(file -> file.setIsAvailable(false));
                // Save all to trigger optimistic locking
                fileRepository.saveAll(files);
                return "files Reserved Successfully";
            } else {
                throw new CustomException("you must be Owner or member in group", HttpStatus.UNAUTHORIZED);
            }
        } else {
            throw new CustomException("group with this id not found", HttpStatus.NOT_FOUND);
        }
    }

    private boolean checkIsUserInGroup(int groupId) {
        //find target Group
        Optional<GroupModel> targetGroup = groupRepository.findById(groupId);
        //find Group For target User
        List<GroupUserModel> currentUserGroups = groupUserRepository.findByUserId(HandleCurrentUserSession.getCurrentUser().getId());
        for (GroupUserModel element : currentUserGroups) {
            if (element.getGroupModel().getId().equals(targetGroup.get().getId())) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public String checkOutFilesOptimistically(FileRequest fileRequest) {
        Optional<GroupModel> targetGroup = groupRepository.findById(fileRequest.getGroupId());
        if (targetGroup.isPresent()) {
            if (HandleCurrentUserSession.getCurrentUser().getId().equals(targetGroup.get().getGroupOwner().getId())
                    || checkIsUserInGroup(targetGroup.get().getId())
            ) {
                List<FileModel> files = fileRepository.findAllById(fileRequest.getFileIds());
                // Check that all files are booked
                for (FileModel file : files) {
                    if (file.getIsAvailable()) {
                        throw new IllegalStateException("One or more files is not booked Yet.");
                    }
                }
                if (fileRequest.getUpdatedFiles() != null && !fileRequest.getUpdatedFiles().isEmpty()) {
                    // Upload Files To Server
                    List<String> filesPath = FilesManagement.uploadMultipleFile(fileRequest.getUpdatedFiles());
                    if (filesPath == null || filesPath.isEmpty()) {
                        throw new CustomException("something went wrong when store files", HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                } else {
                    throw new CustomException("please upload valid files", HttpStatus.BAD_REQUEST);
                }
                // Release files & Set new Info
                files.forEach(file -> {
                    String oldFileName = file.getFileName();
                    if (oldFileName.contains("-Version")) {
                        file.setFileName(oldFileName.substring(0, oldFileName.indexOf("-Version/")) + "-Version/" + (file.getVersion() + 1) / 2 + "/");
                    } else {
                        file.setFileName(file.getFileName() + "-Version/" + file.getVersion() + "/");
                    }
                    file.setLastModifiedAt(LocalDateTime.now());
                    file.setIsAvailable(true);
                });
                // Save all to trigger optimistic locking
                fileRepository.saveAll(files);
                return "files Checked-Out Successfully";
            } else {
                throw new CustomException("you must be Owner or member in group", HttpStatus.UNAUTHORIZED);
            }
        } else {
            throw new CustomException("group with this id not found", HttpStatus.NOT_FOUND);
        }
    }

}