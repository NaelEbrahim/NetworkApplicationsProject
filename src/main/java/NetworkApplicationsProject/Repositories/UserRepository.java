package NetworkApplicationsProject.Repositories;

import NetworkApplicationsProject.Models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserModel,Integer> {

    Optional<UserModel> findByEmail(String email);

    Optional<UserModel> findByUserName(String username);

    Optional<UserModel> findByEmailOrUserName(String email, String username);

}
