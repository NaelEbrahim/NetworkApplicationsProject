package NetworkApplicationsProject.Repositories;

import NetworkApplicationsProject.Models.TokenModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<TokenModel,Integer> {

    Optional<TokenModel> findByToken(String token);

    List<TokenModel> findByUserId(Integer userId);

}