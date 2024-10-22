package NetworkApplicationsProject.DTO.Requset;

import NetworkApplicationsProject.Enums.GenderEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthRequest {

    private String firstName;

    private String lastName;

    private String userName;

    private GenderEnum gender;

    @NotBlank
    @Email(message = "not Valid email!", regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")
    private String email;

    @NotBlank
    @Size(min = 8, max = 255, message = "password must be 8 characters at Least")
    private String password;

}