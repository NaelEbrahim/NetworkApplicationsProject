package NetworkApplicationsProject.Services;

import NetworkApplicationsProject.Models.TokenModel;
import NetworkApplicationsProject.Models.UserModel;
import NetworkApplicationsProject.Repositories.TokenRepository;
import NetworkApplicationsProject.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenService {

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;


    public void saveUserToken(UserModel user, String accessToken,TokenModel refreshToken) {
        TokenModel tokenModel = new TokenModel();
        tokenModel.setUser(user);
        tokenModel.setAccessToken(accessToken);
        tokenModel.setRefreshToken(refreshToken.getRefreshToken());
        tokenModel.setRefreshTokenExpireDate(refreshToken.getRefreshTokenExpireDate());
        tokenRepository.save(tokenModel);
    }

    public void revokeOldUserTokens(UserModel user) {
        List<TokenModel> validUserTokens = tokenRepository.findByUserId(user.getId());
        if (!validUserTokens.isEmpty()) {
            validUserTokens.forEach(tokenRepository::delete);
        }
    }

    public TokenModel createRefreshToken(UserModel user) {
        TokenModel refreshToken = new TokenModel();
        refreshToken.setUser(user);
        refreshToken.setRefreshToken(UUID.randomUUID().toString());
        refreshToken.setRefreshTokenExpireDate(LocalDateTime.now().plusDays(3));
        return refreshToken;
    }

    public boolean validateRefreshToken(String token) {
        Optional<TokenModel> refreshTokenOpt = tokenRepository.findByRefreshToken(token);
        if (refreshTokenOpt.isPresent()) {
            if (!refreshTokenOpt.get().getRefreshTokenExpireDate().isBefore(LocalDateTime.now())) {
                return true;
            } else {
                revokeOldUserTokens(refreshTokenOpt.get().getUser());
                return false;
            }
        } else {
            return false;
        }
    }

}