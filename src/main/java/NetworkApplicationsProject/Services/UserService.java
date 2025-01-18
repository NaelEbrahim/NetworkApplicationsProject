package NetworkApplicationsProject.Services;

import NetworkApplicationsProject.CustomExceptions.CustomException;
import NetworkApplicationsProject.DTO.Requset.AuthRequests.LoginRequest;
import NetworkApplicationsProject.DTO.Requset.AuthRequests.RegisterRequest;
import NetworkApplicationsProject.DTO.Requset.ProfileRequest;
import NetworkApplicationsProject.DTO.Response.AuthResponse;
import NetworkApplicationsProject.Enums.RolesEnum;
import NetworkApplicationsProject.Models.TokenModel;
import NetworkApplicationsProject.Models.UserModel;
import NetworkApplicationsProject.Repositories.TokenRepository;
import NetworkApplicationsProject.Repositories.UserRepository;
import NetworkApplicationsProject.Services.SecurityServices.EncryptionService;
import NetworkApplicationsProject.Services.SecurityServices.JWTService;
import NetworkApplicationsProject.Tools.HandleCurrentUserSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private EncryptionService encryptionService;


    public AuthResponse register(RegisterRequest authRequest) {
        if (userRepository.findByEmail(authRequest.getEmail()).isPresent()) {
            throw new CustomException("email already used", HttpStatus.CONFLICT);
        } else if (userRepository.findByUserName(authRequest.getUserName()).isPresent()) {
            throw new CustomException("userName already used", HttpStatus.CONFLICT);
        } else {
            // Create New User & Initialize it
            UserModel userModel = new UserModel();
            userModel.setFirstName(authRequest.getFirstName());
            userModel.setLastName(authRequest.getLastName());
            userModel.setUserName(authRequest.getUserName());
            userModel.setEmail(authRequest.getEmail());
            userModel.setGender(authRequest.getGender());
            userModel.setCreatedAt(LocalDateTime.now());
            userModel.setLastModified(LocalDateTime.now());
            userModel.setRole(RolesEnum.USER);
            userModel.setPassword(encryptionService.encryptPassword(authRequest.getPassword()));
            // Save New User In DataBase
            UserModel savedUser = userRepository.save(userModel);
            // Generate JWT Token & Save it
            String generatedToken = jwtService.generateJWT(savedUser);
            // Generate Refresh Token
            TokenModel refreshToken = tokenService.createRefreshToken(savedUser);
            // save tokens in DB
            tokenService.saveUserToken(savedUser, generatedToken, refreshToken);
            // return Response Object
            AuthResponse authResponse = new AuthResponse();
            authResponse.setUserModel(savedUser);
            authResponse.setAccessToken(generatedToken);
            authResponse.setRefreshToken(refreshToken.getRefreshToken());
            return authResponse;
        }
    }

    public AuthResponse login(LoginRequest authRequest) {
        Optional<UserModel> currentUser = userRepository.findByEmail(authRequest.getEmail());
        if (currentUser.isPresent()) {
            UserModel user = currentUser.get();
            if (encryptionService.verifyPassword(authRequest.getPassword(), user.getPassword())) {
                // Generate JWT Access Token - Revoke Old one & Save the new
                String generatedToken = jwtService.generateJWT(user);
                tokenService.revokeOldUserTokens(user);
                // Generate Refresh Token
                TokenModel refreshToken = tokenService.createRefreshToken(user);
                // save tokens in DB
                tokenService.saveUserToken(user, generatedToken, refreshToken);
                // return Response Object
                AuthResponse authResponse = new AuthResponse();
                authResponse.setUserModel(user);
                authResponse.setAccessToken(generatedToken);
                authResponse.setRefreshToken(refreshToken.getRefreshToken());
                return authResponse;
            } else {
                throw new CustomException("wrong password", HttpStatus.BAD_REQUEST);
            }
        } else {
            throw new CustomException("wrong email on Not registered yet", HttpStatus.NOT_FOUND);
        }
    }

    public String logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // Revoke All User Tokens
        tokenService.revokeOldUserTokens(HandleCurrentUserSession.getCurrentUser());
        // Clear Session And Security Context Holder
        SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
        securityContextLogoutHandler.logout(request, response, authentication);
        // Initialize And return Response
        return "Logout Successfully";
    }

    public Object profile() {
        return HandleCurrentUserSession.getCurrentUser();
    }

    public Object edit(ProfileRequest profileRequest) {
        UserModel user = HandleCurrentUserSession.getCurrentUser();
        if (!profileRequest.getFirstName().isEmpty()) {
            user.setFirstName(profileRequest.getFirstName());
        }

        if (!profileRequest.getLastName().isEmpty()) {
            user.setLastName(profileRequest.getLastName());
        }
        
        userRepository.save(user);
        return user;
    }

    public String saveFcmToken(String fcmToken) {
        if (fcmToken != null && !fcmToken.isBlank()) {
            TokenModel newToken = new TokenModel();
            newToken.setUser(HandleCurrentUserSession.getCurrentUser());
            newToken.setFCM_Token(fcmToken);
            // save in DB
            tokenRepository.save(newToken);
            return "FCM token saved successfully";
        } else {
            throw new CustomException("token is not valid", HttpStatus.BAD_REQUEST);
        }
    }

    public Map<String,String> refreshAccessToken(String refreshToken) {
        if (tokenService.validateRefreshToken(refreshToken)) {
            TokenModel updatedUserToken = tokenRepository.findByRefreshToken(refreshToken).get();
            // generate new Access Token
            String newAccessToken = jwtService.generateJWT(updatedUserToken.getUser());
            // update access token in DB
            updatedUserToken.setAccessToken(newAccessToken);
            tokenRepository.save(updatedUserToken);
            // return response
            Map<String,String> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            return response;
        } else {
            throw new CustomException("Invalid or expired refresh token.", HttpStatus.UNAUTHORIZED);
        }
    }


}