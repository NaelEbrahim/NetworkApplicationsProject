package NetworkApplicationsProject.Repositories;

import NetworkApplicationsProject.Models.GroupUserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupUserRepository extends JpaRepository<GroupUserModel, Integer> {

    List<GroupUserModel> findByUserId(Integer userId);

}