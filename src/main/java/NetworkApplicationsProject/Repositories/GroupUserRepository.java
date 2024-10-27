package NetworkApplicationsProject.Repositories;

import NetworkApplicationsProject.Models.GroupModel;
import NetworkApplicationsProject.Models.GroupUserModel;
import NetworkApplicationsProject.Models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupUserRepository extends JpaRepository<GroupUserModel,Integer> {


    List<GroupUserModel> findByUserModelAndGroupModel(UserModel user, GroupModel group);

    List<GroupUserModel> findByGroupModel(GroupModel group);

}
