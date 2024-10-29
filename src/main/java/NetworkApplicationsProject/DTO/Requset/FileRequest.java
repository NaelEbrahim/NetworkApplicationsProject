package NetworkApplicationsProject.DTO.Requset;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Setter
@Getter
public class FileRequest {

    private String fileName;

    private MultipartFile file;

    private Integer groupId;

    private String contentType;

    private List<Integer> fileIds;

    private List<MultipartFile> updatedFiles;

}