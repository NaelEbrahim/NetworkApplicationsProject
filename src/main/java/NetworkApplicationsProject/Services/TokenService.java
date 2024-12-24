package NetworkApplicationsProject.Services;

import NetworkApplicationsProject.Models.TokenModel;
import NetworkApplicationsProject.Models.UserModel;
import NetworkApplicationsProject.Repositories.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TokenService {

    @Autowired
    TokenRepository tokenRepository;

    public void saveUserToken(UserModel user, String generatedToken) {
        TokenModel tokenModel = new TokenModel();
        tokenModel.setUser(user);
        tokenModel.setServerToken(generatedToken);
        tokenRepository.save(tokenModel);
    }

    public void revokeOldUserTokens(UserModel user) {
        List<TokenModel> validUserTokens = tokenRepository.findByUserId(user.getId());
        if (!validUserTokens.isEmpty()) {
            validUserTokens.forEach(tokenRepository::delete);
        }
    }

}