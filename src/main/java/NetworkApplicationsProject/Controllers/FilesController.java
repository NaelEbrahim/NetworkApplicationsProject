package NetworkApplicationsProject.Controllers;

import NetworkApplicationsProject.CustomExceptions.CustomException;
import NetworkApplicationsProject.DTO.Requset.FilesRequests.AddFileRequest;
import NetworkApplicationsProject.DTO.Requset.FilesRequests.CheckInFilesRequest;
import NetworkApplicationsProject.DTO.Requset.FilesRequests.CheckOutFilesRequest;
import NetworkApplicationsProject.Models.ActivityModel;
import NetworkApplicationsProject.Services.FilesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("api/files")
public class FilesController {

    @Autowired
    FilesService filesService;

    @PostMapping("/addFile")
    public ResponseEntity<?> addFile(@ModelAttribute AddFileRequest fileData) {
        try {
            return new ResponseEntity<>(filesService.addFile(fileData), HttpStatus.OK);
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }

    @DeleteMapping("/deleteFile")
    public ResponseEntity<?> deleteFile(@Param("fileId") Integer fileId) {
        try {
            return new ResponseEntity<>(filesService.deleteFile(fileId), HttpStatus.OK);
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }

    @GetMapping("/getGroupFiles")
    public ResponseEntity<?> getFiles(@Param("groupId") Integer groupId) {
        try {
            return new ResponseEntity<>(filesService.getFiles(groupId), HttpStatus.OK);
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }

    @PostMapping("/checkInFiles")
    public ResponseEntity<?> checkInFiles(@ModelAttribute CheckInFilesRequest fileRequest) {
        try {
            return filesService.checkInFilesOptimistically(fileRequest);
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exception.getMessage());
        }
    }

    @PostMapping("/checkOutFiles")
    public ResponseEntity<?> checkOutFiles(@ModelAttribute CheckOutFilesRequest fileRequest) {
        try {
            //return filesService.checkOutFilesOptimistically(fileRequest);
            return filesService.checkOutFilesOptimistically(fileRequest);
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exception.getMessage());
        }
    }

    @GetMapping("/getUserFilesInGroup")
    public ResponseEntity<?> getUploadedUserFilesInGroup(@Param("groupId") Integer groupId) {
        try {
            return new ResponseEntity<>(filesService.getUploadUserFiles(groupId), HttpStatus.OK );
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }

    // Get all logs for a file in a specific group
    @GetMapping("/logs/file")
    public ResponseEntity<?> getLogsByFileId(
            @Param("fileId")  Integer fileId, @Param("groupId") Integer groupId) {
        try {
            return new ResponseEntity<>(filesService.getLogsByFile(fileId,groupId), HttpStatus.OK );
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }

    // Get all logs for a user in a specific group
    @GetMapping("/logs/user")
    public ResponseEntity<?> getLogsByUserId(@Param("groupId") Integer groupId) {
        try {
            return new ResponseEntity<>(filesService.getLogsByUser(groupId), HttpStatus.OK );
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }


}