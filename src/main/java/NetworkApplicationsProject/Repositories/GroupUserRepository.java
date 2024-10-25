package NetworkApplicationsProject.Repositories;

import NetworkApplicationsProject.Models.GroupUserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupUserRepository extends JpaRepository<GroupUserModel,Integer> {

}
