package NetworkApplicationsProject.Controllers;

import NetworkApplicationsProject.CustomExceptions.CustomException;
import NetworkApplicationsProject.Services.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/groups")
public class GroupsController {

    @Autowired
    GroupService groupService;

    @PostMapping("/createGroup")
    public ResponseEntity<?> createGroup(@Param("groupName") String groupName, @Param("groupType") String groupType) {
        try {
            return new ResponseEntity<>(groupService.createGroup(groupName, groupType), HttpStatus.CREATED);
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }

    @PostMapping("/updateGroup/{groupId}")
    public ResponseEntity<?> updateGroup(@Param("groupName") String groupName, @Param("groupType") String groupType, @PathVariable int groupId) {
        try {
            return new ResponseEntity<>(groupService.updateGroup(groupName, groupType, groupId), HttpStatus.OK);
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

    @PostMapping("/addUser/{groupId}")
    public ResponseEntity<?> addUser(@Param("userName_email") String userName_email, @PathVariable int groupId) {
        try {
            return new ResponseEntity<>(groupService.addUser(userName_email, groupId), HttpStatus.OK);
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }

}
