package NetworkApplicationsProject.DTO.Requset.FilesRequests;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Setter
@Getter
public class CheckOutFilesRequest {

    private Integer groupId;

    private List<Integer> fileIds;

    private List<MultipartFile> updatedFiles;

}