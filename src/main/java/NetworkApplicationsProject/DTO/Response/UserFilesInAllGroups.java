package NetworkApplicationsProject.DTO.Response;

import NetworkApplicationsProject.Models.GroupModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;


@Setter
@Getter
@AllArgsConstructor
public class UserFilesInAllGroups {

    List<Map<String, Object>> groupFiles;

    GroupModel uploadedGroup;

}