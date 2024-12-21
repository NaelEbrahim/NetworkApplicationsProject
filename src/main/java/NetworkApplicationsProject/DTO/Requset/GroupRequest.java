package NetworkApplicationsProject.DTO.Requset;

import NetworkApplicationsProject.Enums.GroupTypeEnum;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GroupRequest {

    private Integer groupId;

    private String groupName;

    private String groupSlug;

    private GroupTypeEnum groupType;

    private String userName;

}