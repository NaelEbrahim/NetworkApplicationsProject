package NetworkApplicationsProject.Services;

import NetworkApplicationsProject.CustomExceptions.CustomException;
import NetworkApplicationsProject.DTO.Response.GroupResponse;
import NetworkApplicationsProject.Models.GroupModel;
import NetworkApplicationsProject.Repositories.GroupRepository;
import NetworkApplicationsProject.Tools.HandleCurrentUserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class GroupService {

    @Autowired
    GroupRepository groupRepository;

    public GroupResponse createGroup(String groupName, String groupType) {
        if (groupRepository.findByName(groupName).isPresent()) {
            throw new CustomException("this name already used with another group", HttpStatus.CONFLICT);
        } else {
            // Create New Group & Initialize it
            GroupModel newGroup = new GroupModel();
            newGroup.setName(groupName);
            newGroup.setType(groupType);
            newGroup.setCreatedAt(LocalDateTime.now());
            newGroup.setGroupOwner(HandleCurrentUserSession.getCurrentUser());
            // Save In DataBase
            groupRepository.save(newGroup);
            // Return Response
            GroupResponse groupResponse = new GroupResponse();
            groupResponse.setGroupInfo(newGroup);
            return groupResponse;
        }
    }

    public String deleteGroup(String groupName) {
        Optional<GroupModel> targetGroup = groupRepository.findByName(groupName);
        if (targetGroup.isPresent()) {
            if (targetGroup.get().getGroupOwner().getId().equals(HandleCurrentUserSession.getCurrentUser().getId())) {
                groupRepository.delete(targetGroup.get());
                return "Group Deleted Successfully";
            } else {
                throw new CustomException("you can not delete this group because you are not the owner", HttpStatus.UNAUTHORIZED);
            }
        } else {
            throw new CustomException("group with this name Not Found", HttpStatus.NOT_FOUND);
        }
    }


    public Object updateGroup(String groupName, String groupType, int groupId) {
        Optional<GroupModel> group = groupRepository.findById(groupId);
        if (group.isPresent()) {
            group.get().setName(groupName);
            group.get().setType(groupType);
            groupRepository.save(group.get());
            // Return Response
            GroupResponse groupResponse = new GroupResponse();
            groupResponse.setGroupInfo(group.get());
            return groupResponse;
        } else {
            throw new CustomException("group not found", HttpStatus.NOT_FOUND);
        }
    }

    public Object addUser(String userName_email, int groupId) {
        return userName_email;
    }
}
