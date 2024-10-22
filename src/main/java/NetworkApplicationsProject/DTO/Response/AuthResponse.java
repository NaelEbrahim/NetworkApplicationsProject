package NetworkApplicationsProject.DTO.Response;


import NetworkApplicationsProject.Models.UserModel;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthResponse {

    UserModel userModel;

    String token;

}
