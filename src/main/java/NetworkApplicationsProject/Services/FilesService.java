package NetworkApplicationsProject.Services;


import NetworkApplicationsProject.CustomExceptions.CustomException;
import NetworkApplicationsProject.DTO.Requset.FileRequest;
import NetworkApplicationsProject.Models.FileModel;
import NetworkApplicationsProject.Models.GroupModel;
import NetworkApplicationsProject.Repositories.FileRepository;
import NetworkApplicationsProject.Repositories.GroupRepository;
import NetworkApplicationsProject.Tools.FilesManagement;
import NetworkApplicationsProject.Tools.HandleCurrentUserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FilesService {

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
                String filepath = FilesManagement.uploadSingleFile(fileRequest.getFile());
                if (filepath == null) {
                    throw new CustomException("something went wrong when store file", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                // Create New File
                FileModel newFile = new FileModel();
                newFile.setIsAvailable(true);
                newFile.setFilePath(filepath);
                newFile.setGroupModel(targetGroup.get());
                newFile.setCreatedAt(LocalDateTime.now());
                newFile.setLastModifiedAt(LocalDateTime.now());
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

    public String deleteFile(FileRequest fileRequest) {
        Optional<FileModel> targetFile = fileRepository.findById(fileRequest.getFileId());
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

}
