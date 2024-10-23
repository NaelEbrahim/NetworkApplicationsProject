package NetworkApplicationsProject.Controllers;

import NetworkApplicationsProject.CustomExceptions.CustomException;
import NetworkApplicationsProject.Services.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/groups")
public class GroupsController {

    @Autowired
    GroupService groupService;

    @PostMapping("/createGroup")
    public ResponseEntity<?> createGroup(@Param("groupName") String groupName) {
        try {
            return new ResponseEntity<>(groupService.createGroup(groupName), HttpStatus.CREATED);
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }

    @DeleteMapping("/deleteGroup")
    public ResponseEntity<?> deleteGroup(@Param("groupName") String groupName) {
        try {
            return new ResponseEntity<>(groupService.deleteGroup(groupName), HttpStatus.OK);
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }

}
