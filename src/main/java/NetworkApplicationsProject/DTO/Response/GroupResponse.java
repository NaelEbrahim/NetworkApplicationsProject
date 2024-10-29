package NetworkApplicationsProject.DTO.Response;

import NetworkApplicationsProject.Models.GroupModel;
import NetworkApplicationsProject.Models.UserModel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class GroupResponse {

    private GroupModel groupInfo;
    private List<UserModel> members;

}
