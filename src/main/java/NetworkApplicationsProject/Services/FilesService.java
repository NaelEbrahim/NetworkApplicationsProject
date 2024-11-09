package NetworkApplicationsProject.Services;

import NetworkApplicationsProject.CustomExceptions.CustomException;
import NetworkApplicationsProject.DTO.Requset.FilesRequests.AddFileRequest;
import NetworkApplicationsProject.DTO.Requset.FilesRequests.CheckInFilesRequest;
import NetworkApplicationsProject.DTO.Requset.FilesRequests.CheckOutFilesRequest;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class FilesService {

    @Autowired
    GroupUserRepository groupUserRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    FileRepository fileRepository;

    public String addFile(AddFileRequest fileRequest) {
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
                    || checkIsUserInGroup(targetGroup.get())
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
    public ResponseEntity<?> checkInFilesOptimistically(CheckInFilesRequest fileRequest) {
        Optional<GroupModel> targetGroup = groupRepository.findById(fileRequest.getGroupId());
        if (targetGroup.isPresent()) {
            if (HandleCurrentUserSession.getCurrentUser().getId().equals(targetGroup.get().getGroupOwner().getId()) // Commit it if need to Testing
                    || checkIsUserInGroup(targetGroup.get())
            ) {
                List<FileModel> files = fileRepository.findAllById(fileRequest.getFileIds());
                // Check if all files are exist
                if (files.size() == fileRequest.getFileIds().size()) {
                    for (FileModel file : files) {
                        // Check that all files are available
                        if (!file.getIsAvailable()) {
                            throw new CustomException("file with name : " + file.getFileName() + " is already reserved.", HttpStatus.LOCKED);
                        }
                    }
                } else {
                    throw new CustomException("one or more entered files Ids Not Found", HttpStatus.NOT_FOUND);
                }
                // Reserve files
                files.forEach(file -> file.setIsAvailable(false));
                // Save all to trigger optimistic locking
                fileRepository.saveAll(files);
                return new ResponseEntity<>(files, HttpStatus.OK);
            } else {
                throw new CustomException("you must be Owner or member in group", HttpStatus.UNAUTHORIZED);
            }
        } else {
            throw new CustomException("group with this id not found", HttpStatus.NOT_FOUND);
        }
    }

    private boolean checkIsUserInGroup(GroupModel targetGroup) {
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
    public String checkOutFilesOptimistically(CheckOutFilesRequest fileRequest) throws IOException, NoSuchAlgorithmException {
        Optional<GroupModel> targetGroup = groupRepository.findById(fileRequest.getGroupId());
        if (targetGroup.isPresent()) {
            if (HandleCurrentUserSession.getCurrentUser().getId().equals(targetGroup.get().getGroupOwner().getId())
                    || checkIsUserInGroup(targetGroup.get())
            ) {
                List<FileModel> files = fileRepository.findAllById(fileRequest.getFileIds());
                // Check if all files are exist
                if (files.size() == fileRequest.getFileIds().size()) {
                    for (FileModel file : files) {
                        // Check if all files are booked
                        if (file.getIsAvailable()) {
                            throw new CustomException("One or more files is not booked Yet.", HttpStatus.BAD_REQUEST);
                        }
                    }
                } else {
                    throw new CustomException("one or more entered files Ids Not Found", HttpStatus.NOT_FOUND);
                }
                if (fileRequest.getUpdatedFiles() != null && !fileRequest.getUpdatedFiles().isEmpty()) {
                    for (MultipartFile element : fileRequest.getUpdatedFiles()) {
                        FileModel currentFile = findElementInList(files, element);
                        if (currentFile != null && !FilesManagement.areFilesIdentical(currentFile.getFilePath(), element)) {
                            // Upload File To Server if File Changed
                            File updatedFile = FilesManagement.uploadSingleFile(element);
                            if (updatedFile == null) {
                                throw new CustomException("something went wrong when store files", HttpStatus.INTERNAL_SERVER_ERROR);
                            }
                            // Set new Info
                            currentFile.setRealVersion(currentFile.getRealVersion() + 1);
                            currentFile.setFilePath(updatedFile.getAbsolutePath());
                            currentFile.setLastModifiedAt(LocalDateTime.now());
                        }
                        // Release file
                        Objects.requireNonNull(currentFile).setIsAvailable(true);
                    }
                    // Save all to trigger optimistic locking
                    fileRepository.saveAll(files);
                    return "files Checked-Out Successfully";
                } else {
                    throw new CustomException("please upload valid files", HttpStatus.BAD_REQUEST);
                }
            } else {
                throw new CustomException("you must be Owner or member in group", HttpStatus.UNAUTHORIZED);
            }
        } else {
            throw new CustomException("group with this id not found", HttpStatus.NOT_FOUND);
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

}