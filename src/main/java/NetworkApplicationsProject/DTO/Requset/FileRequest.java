package NetworkApplicationsProject.DTO.Requset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
public class FileRequest {

    private MultipartFile file;

    private Integer groupId;

    private Integer fileId;

    private String contentType;

}