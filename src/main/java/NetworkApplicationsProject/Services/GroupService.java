package NetworkApplicationsProject.Services;

import NetworkApplicationsProject.CustomExceptions.CustomException;
import NetworkApplicationsProject.DTO.Requset.GroupRequest;
import NetworkApplicationsProject.DTO.Response.GroupResponse;
import NetworkApplicationsProject.Enums.RolesEnum;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupUserRepository groupUserRepository;

    public GroupResponse createGroup(GroupRequest groupRequest) {
        if (groupRepository.findBySlug(groupRequest.getGroupSlug()).isPresent()) {
            throw new CustomException("This Slug is already used with another group", HttpStatus.CONFLICT);
        }

        // Create and save new group
        GroupModel newGroup = new GroupModel();
        newGroup.setName(groupRequest.getGroupName());
        newGroup.setSlug(groupRequest.getGroupSlug());
        newGroup.setType(groupRequest.getGroupType());
        newGroup.setCreatedAt(LocalDateTime.now());
        newGroup.setGroupOwner(HandleCurrentUserSession.getCurrentUser());
        groupRepository.save(newGroup);

        // Prepare and return response
        GroupResponse groupResponse = new GroupResponse();
        groupResponse.setGroupInfo(newGroup);
        return groupResponse;
    }

    public String deleteGroup(String groupName) {
        Optional<GroupModel> targetGroup = groupRepository.findByName(groupName);
        if (targetGroup.isPresent()) {
            if (targetGroup.get().getGroupOwner().getId().equals(HandleCurrentUserSession.getCurrentUser().getId())) {
                groupRepository.delete(targetGroup.get());
                return "Group deleted successfully";
            } else {
                throw new CustomException("You cannot delete this group because you are not the owner", HttpStatus.UNAUTHORIZED);
            }
        } else {
            throw new CustomException("Group with this name not found", HttpStatus.NOT_FOUND);
        }
    }

    public GroupResponse updateGroup(GroupRequest groupRequest) {
        GroupModel group = groupRepository.findById(groupRequest.getGroupId())
                .orElseThrow(() -> new CustomException("Group not found", HttpStatus.NOT_FOUND));

        if (groupRequest.getGroupName() != null && !groupRequest.getGroupName().isBlank()) {
            group.setName(groupRequest.getGroupName());
        }
        if (groupRequest.getGroupType() != null && !groupRequest.getGroupType().isBlank()) {
            group.setType(groupRequest.getGroupType());
        }

        groupRepository.save(group);

        // Prepare and return response
        GroupResponse groupResponse = new GroupResponse();
        groupResponse.setGroupInfo(group);
        return groupResponse;
    }

    public String addUserToGroup(GroupRequest groupRequest) {
        // Fetch the group and user from the database
        Optional<GroupModel> targetGroup = groupRepository.findById(groupRequest.getGroupId());
        Optional<UserModel> targetUser = userRepository.findByEmailOrUserName(groupRequest.getUserName(), groupRequest.getUserName());

        // Check if the user and group exist
        if (targetUser.isPresent()) {
            throw new CustomException("User not found", HttpStatus.NOT_FOUND);
        }

        if (targetGroup.isPresent()) {
            throw new CustomException("Group not found", HttpStatus.NOT_FOUND);
        }

        // Authorization check
        boolean isAdmin = HandleCurrentUserSession.getCurrentUserRole().equals(RolesEnum.SUPER_ADMIN);
        boolean isGroupOwner = targetGroup.get().getGroupOwner().getId().equals(HandleCurrentUserSession.getCurrentUser().getId());

        if (!isAdmin && !isGroupOwner) {
            throw new CustomException("Unauthorized access", HttpStatus.FORBIDDEN);
        }

        // Check if the user already exists in the group
        List<GroupUserModel> existingGroupUsers = groupUserRepository.findByUserAndGroupModel(targetUser.get(), targetGroup.get());

        if (!existingGroupUsers.isEmpty()) {
            throw new CustomException("User already exists in the group", HttpStatus.CONFLICT);
        }

        // Create new GroupUserModel and save it
        GroupUserModel groupUserModel = new GroupUserModel();
        groupUserModel.setUser(targetUser.get());
        groupUserModel.setGroupModel(targetGroup.get());
        groupUserModel.setJoinDate(LocalDateTime.now());
        groupUserRepository.save(groupUserModel);

        return "User added successfully";
    }

    public String removeUserFromGroup(GroupRequest groupRequest) {
        // Fetch the group and user from the database
        Optional<GroupModel> targetGroup = groupRepository.findById(groupRequest.getGroupId());
        if (targetGroup.isEmpty()) {
            throw new CustomException("Group not found", HttpStatus.NOT_FOUND);
        }

        Optional<UserModel> targetUser = userRepository.findByEmailOrUserName(groupRequest.getUserName(), groupRequest.getUserName());
        if (targetUser.isEmpty()) {
            throw new CustomException("User not found", HttpStatus.NOT_FOUND);
        }

        // Authorization check
        boolean isAdmin = HandleCurrentUserSession.getCurrentUserRole().equals(RolesEnum.SUPER_ADMIN);
        boolean isGroupOwner = targetGroup.get().getGroupOwner().getId().equals(HandleCurrentUserSession.getCurrentUser().getId());

        if (!isAdmin && !isGroupOwner) {
            throw new CustomException("Unauthorized access", HttpStatus.FORBIDDEN);
        }

        // Check if the user exists in the group
        List<GroupUserModel> existingGroupUser = groupUserRepository.findByUserAndGroupModel(targetUser.get(), targetGroup.get());
        if (existingGroupUser.isEmpty()) {
            throw new CustomException("User is not a member of this group", HttpStatus.NOT_FOUND);
        }

        // Remove the user from the group
        groupUserRepository.delete(existingGroupUser.get(0));

        return "User removed successfully from the group";
    }

    public GroupResponse getGroupWithMembers(int groupId) {
        GroupModel group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException("Group not found", HttpStatus.NOT_FOUND));

        boolean isAdmin = HandleCurrentUserSession.getCurrentUserRole().equals(RolesEnum.SUPER_ADMIN);
        boolean isGroupOwner = group.getGroupOwner().getId().equals(HandleCurrentUserSession.getCurrentUser().getId());

        if (!isAdmin && !isGroupOwner) {
            throw new CustomException("Unauthorized access", HttpStatus.FORBIDDEN);
        }

        List<UserModel> members = groupUserRepository.findByGroupModel(group).stream()
                .map(GroupUserModel::getUser) // Map GroupUserModel to UserModel
                .collect(Collectors.toList());

        GroupResponse groupResponse = new GroupResponse();
        groupResponse.setGroupInfo(group);
        groupResponse.setMembers(members); // Set members in the response

        return groupResponse;
    }
}
