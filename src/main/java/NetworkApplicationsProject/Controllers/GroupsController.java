package NetworkApplicationsProject.Controllers;

import NetworkApplicationsProject.CustomExceptions.CustomException;
import NetworkApplicationsProject.DTO.Requset.GroupRequest;
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
    public ResponseEntity<?> createGroup(@ModelAttribute GroupRequest groupRequest) {
        try {
            return new ResponseEntity<>(groupService.createGroup(groupRequest), HttpStatus.CREATED);
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

    @PostMapping("/addUser")
    public ResponseEntity<?> addUser(@ModelAttribute GroupRequest groupRequest) {
        try {
            return new ResponseEntity<>(groupService.addUserToGroup(groupRequest), HttpStatus.OK);
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }

}
