package NetworkApplicationsProject.DTO.Requset.FilesRequests;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
public class AddFileRequest {

    private MultipartFile file;

    private Integer groupId;

    private String contentType;

}