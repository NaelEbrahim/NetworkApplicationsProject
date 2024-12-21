package NetworkApplicationsProject.Services;

import NetworkApplicationsProject.CustomExceptions.CustomException;
import NetworkApplicationsProject.DTO.Requset.GroupRequest;
import NetworkApplicationsProject.DTO.Response.GroupResponse;
import NetworkApplicationsProject.DTO.Response.UserFilesInAllGroups;
import NetworkApplicationsProject.Enums.GroupTypeEnum;
import NetworkApplicationsProject.Enums.RolesEnum;
import NetworkApplicationsProject.Models.GroupModel;
import NetworkApplicationsProject.Models.GroupUserModel;
import NetworkApplicationsProject.Models.UserModel;
import NetworkApplicationsProject.Repositories.GroupRepository;
import NetworkApplicationsProject.Repositories.GroupUserRepository;
import NetworkApplicationsProject.Repositories.UserRepository;
import NetworkApplicationsProject.Tools.FilesManagement;
import NetworkApplicationsProject.Tools.HandleCurrentUserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private FilesService filesService;

    public GroupModel createGroup(GroupRequest groupRequest) {
        // Validate request
        if (groupRequest.getGroupName() == null || groupRequest.getGroupName().isEmpty()) {
            throw new CustomException("Group name must not be null or empty", HttpStatus.BAD_REQUEST);
        }
        if (groupRepository.findBySlug(groupRequest.getGroupSlug()).isPresent()) {
            throw new CustomException("This Slug is already used with another group", HttpStatus.CONFLICT);
        }

        // Create the group
        GroupModel newGroup = new GroupModel();
        newGroup.setName(groupRequest.getGroupName());
        newGroup.setType(groupRequest.getGroupType());
        newGroup.setSlug(groupRequest.getGroupSlug());
        newGroup.setCreatedAt(LocalDateTime.now());
        newGroup.setGroupOwner(HandleCurrentUserSession.getCurrentUser());
        groupRepository.save(newGroup);

        // Add the owner to the group as a member
        GroupUserModel groupOwnerMembership = new GroupUserModel();
        groupOwnerMembership.setGroupModel(newGroup);
        groupOwnerMembership.setUser(HandleCurrentUserSession.getCurrentUser());
        groupOwnerMembership.setUser(HandleCurrentUserSession.getCurrentUser()); // Assuming `OWNER` is a role in your enum
        groupOwnerMembership.setJoinDate(LocalDateTime.now());
        groupUserRepository.save(groupOwnerMembership);

        return newGroup;
    }


    public String deleteGroup(int groupId) {
        Optional<GroupModel> targetGroup = groupRepository.findById(groupId);
        if (targetGroup.isPresent()) {
            GroupModel group = targetGroup.get();
            if (group.getGroupOwner().getId().equals(HandleCurrentUserSession.getCurrentUser().getId())) {
                // Delete group folder
                File groupFolder = new File(FilesManagement.UPLOAD_DIR, group.getSlug());
                FilesManagement.deleteFolder(groupFolder);

                // Delete the group from the database
                groupRepository.delete(group);
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
        if (groupRequest.getGroupType() != null && groupRequest.getGroupType() != null) {
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
        if (!targetUser.isPresent()) {
            throw new CustomException("User not found", HttpStatus.NOT_FOUND);
        }

        if (!targetGroup.isPresent()) {
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

    public List<GroupUserModel> getAllUserGroups() {
        List<GroupUserModel> allUserGroups = groupUserRepository.findByUserId(HandleCurrentUserSession.getCurrentUser().getId());
        if (!allUserGroups.isEmpty()) {
            return allUserGroups;
        } else {
            throw new CustomException("you have no any group yet", HttpStatus.NO_CONTENT);
        }
    }

    public ResponseEntity<List<GroupModel>> getAllPublicGroups() {
        List<GroupModel> allPublicGroups = groupRepository.findByType(GroupTypeEnum.PUBLIC);
        if (!allPublicGroups.isEmpty()) {
            return new ResponseEntity<>(allPublicGroups, HttpStatus.OK);
        } else {
            throw new CustomException("no public groups yet", HttpStatus.NO_CONTENT);
        }
    }

    public List<UserFilesInAllGroups> getUserFilesInAllGroups() {
        List<GroupUserModel> userGroups = getAllUserGroups();
        if (!userGroups.isEmpty()) {
            List<UserFilesInAllGroups> allUserFiles = new ArrayList<>();
            for (GroupUserModel group : userGroups) {
                List<Map<String, Object>> userFilesInGroup = filesService.getUploadUserFiles(group.getId());
                if (!userFilesInGroup.isEmpty()) {
                    allUserFiles.add(new UserFilesInAllGroups(userFilesInGroup, group.getGroupModel()));
                }
            }
            if (!allUserFiles.isEmpty()) {
                return allUserFiles;
            } else {
                throw new CustomException("no files yet", HttpStatus.NO_CONTENT);
            }
        } else {
            throw new CustomException("you have no any groups yet", HttpStatus.NO_CONTENT);
        }
    }

}
