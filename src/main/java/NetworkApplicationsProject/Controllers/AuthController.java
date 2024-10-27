package NetworkApplicationsProject.Controllers;

import NetworkApplicationsProject.CustomExceptions.CustomException;
import NetworkApplicationsProject.DTO.Requset.AuthRequest;
import NetworkApplicationsProject.Models.UserModel;
import NetworkApplicationsProject.Services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> userRegister(@ModelAttribute @Valid AuthRequest authRequest) {
        try {
            return new ResponseEntity<>(userService.register(authRequest), HttpStatus.CREATED);
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }//hiii

    @PostMapping("/login")
    public ResponseEntity<?> userLogin(@ModelAttribute @Valid AuthRequest authRequest) {
        try {
            return new ResponseEntity<>(userService.login(authRequest), HttpStatus.OK);
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        return new ResponseEntity<>(userService.logout(request, response, authentication), HttpStatus.OK);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> displayProfile() {
        try {
            return new ResponseEntity<>(userService.profile(), HttpStatus.OK);
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }

    @PostMapping("/edit/profile")
    public ResponseEntity<?> editProfile(@Param("firstName") String firstName, @Param("lastName") String lastName, @Param("userName") String userName) {
        try {
            return new ResponseEntity<>(userService.edit(firstName,lastName,userName), HttpStatus.CREATED);
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }

}