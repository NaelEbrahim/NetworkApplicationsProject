package NetworkApplicationsProject.DTO.Response;


import NetworkApplicationsProject.Models.FileModel;
import NetworkApplicationsProject.Models.UserModel;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FileResponse {

    private UserModel fileOwner;

    private FileModel fileInfo;

}