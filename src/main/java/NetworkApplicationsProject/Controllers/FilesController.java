package NetworkApplicationsProject.Controllers;

import NetworkApplicationsProject.CustomExceptions.CustomException;
import NetworkApplicationsProject.DTO.Requset.FileRequest;
import NetworkApplicationsProject.Services.FilesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/files")
public class FilesController {

    @Autowired
    FilesService filesService;

    @PostMapping("/addFile")
    public ResponseEntity<?> addFile(@ModelAttribute FileRequest fileData) {
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

}