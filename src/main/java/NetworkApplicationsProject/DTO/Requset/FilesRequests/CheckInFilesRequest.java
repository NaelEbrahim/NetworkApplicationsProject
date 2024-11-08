package NetworkApplicationsProject.DTO.Requset.FilesRequests;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CheckInFilesRequest {

    private Integer groupId;

    private List<Integer> fileIds;

}