package NetworkApplicationsProject.Controllers;

import NetworkApplicationsProject.CustomExceptions.CustomException;
import NetworkApplicationsProject.DTO.Requset.AuthRequests.LoginRequest;
import NetworkApplicationsProject.DTO.Requset.AuthRequests.RegisterRequest;
import NetworkApplicationsProject.DTO.Requset.ProfileRequest;
import NetworkApplicationsProject.Services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
public class AuthController {
    //test

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> userRegister(@ModelAttribute @Valid RegisterRequest authRequest) {
        try {
            return new ResponseEntity<>(userService.register(authRequest), HttpStatus.CREATED);
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> userLogin(@ModelAttribute @Valid LoginRequest authRequest) {
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

    @PostMapping("/editProfile")
    public ResponseEntity<?> editProfile(ProfileRequest profile) {
        try {
            return new ResponseEntity<>(userService.edit(profile), HttpStatus.CREATED);
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }

    @PostMapping("/saveFcmToken")
    public ResponseEntity<?> saveFcmToken(@RequestParam("fcm_token") String fcmToken) {
        try {
            return new ResponseEntity<>(userService.saveFcmToken(fcmToken), HttpStatus.OK);
        } catch (CustomException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getMessage());
        }
    }

}