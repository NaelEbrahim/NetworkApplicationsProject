package NetworkApplicationsProject.Services;

import NetworkApplicationsProject.CustomExceptions.CustomException;
import NetworkApplicationsProject.DTO.Requset.GroupRequest;
import NetworkApplicationsProject.DTO.Response.GroupResponse;
import NetworkApplicationsProject.Models.GroupModel;
import NetworkApplicationsProject.Models.GroupUserModel;
import NetworkApplicationsProject.Models.UserModel;
import NetworkApplicationsProject.Repositories.GroupRepository;
import NetworkApplicationsProject.Repositories.GroupUserRepository;
import NetworkApplicationsProject.Repositories.UserRepository;
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

    @Autowired
    UserRepository userRepository;

    @Autowired
    GroupUserRepository groupUserRepository;

    public GroupResponse createGroup(GroupRequest groupRequest) {
        if (groupRepository.findByName(groupRequest.getGroupName()).isPresent()) {
            throw new CustomException("this name already used with another group", HttpStatus.CONFLICT);
        } else {
            // Create New Group & Initialize it
            GroupModel newGroup = new GroupModel();
            newGroup.setName(groupRequest.getGroupName());
            newGroup.setType(groupRequest.getGroupType());
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

    public GroupResponse updateGroup(String groupName, String groupType, int groupId) {
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

    public String addUserToGroup(GroupRequest groupRequest) {
        Optional<GroupModel> targetGroup = groupRepository.findById(groupRequest.getGroupId());
        Optional<UserModel> targetUser = userRepository.findByUserName(groupRequest.getUserName());
        if (targetGroup.isPresent()) {
            if (targetUser.isPresent()) {
                // Create New Object % initialize it
                GroupUserModel groupUserModel = new GroupUserModel();
                groupUserModel.setUserModel(targetUser.get());
                groupUserModel.setGroupModel(targetGroup.get());
                groupUserModel.setJoinDate(LocalDateTime.now());
                // Save In DataBase
                groupUserRepository.save(groupUserModel);
                return "user added Successfully";
            } else {
                throw new CustomException("user with this userName not found", HttpStatus.NOT_FOUND);
            }
        } else {
            throw new CustomException("group with this id not found", HttpStatus.NOT_FOUND);
        }
    }

}