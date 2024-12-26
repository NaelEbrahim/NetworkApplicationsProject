package NetworkApplicationsProject.DTO.Response;


import NetworkApplicationsProject.Models.UserModel;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthResponse {

    private UserModel userModel;

    private String accessToken;

    private String refreshToken;

}