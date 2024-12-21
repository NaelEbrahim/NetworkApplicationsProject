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

    @PostMapping("/updateGroup")
    public ResponseEntity<?> updateGroup(@ModelAttribute GroupRequest groupRequest) {
        try {
            return new ResponseEntity<>(groupService.updateGroup(groupRequest), HttpStatus.OK);
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }

    @DeleteMapping("/deleteGroup")
    public ResponseEntity<?> deleteGroup(@Param("groupId") Integer groupId) {
        try {
            return new ResponseEntity<>(groupService.deleteGroup(groupId), HttpStatus.OK);
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

    @PostMapping("/removeUser")
    public ResponseEntity<?> removeUser(@ModelAttribute GroupRequest groupRequest) {
        try {
            return new ResponseEntity<>(groupService.removeUserFromGroup(groupRequest), HttpStatus.OK);
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }

    @GetMapping("/{groupId}/details")
    public ResponseEntity<?> getGroupDetailsWithMembers(
            @PathVariable int groupId) { // Get the email of the requesting user from the request or session
        try {
            return new ResponseEntity<>(groupService.getGroupWithMembers(groupId), HttpStatus.OK);
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }

    @GetMapping("/getAllUserGroups")
    public ResponseEntity<?> getUserGroups() {
        try {
            return new ResponseEntity<>(groupService.getAllUserGroups(), HttpStatus.OK);
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }

    @GetMapping("/getAllPublicGroups")
    public ResponseEntity<?> getPublicGroups() {
        try {
            return groupService.getAllPublicGroups();
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }

    @GetMapping("getUserFilesInAllGroups")
    public ResponseEntity<?> getUserFilesInAllGroups() {
        try {
            return new ResponseEntity<>(groupService.getUserFilesInAllGroups(), HttpStatus.OK);
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }


}
