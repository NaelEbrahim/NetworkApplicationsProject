package NetworkApplicationsProject.DTO.Requset;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
public class FileRequest {

    private MultipartFile file;

    private Integer groupId;

    private String contentType;

}