package NetworkApplicationsProject.Repositories;

import NetworkApplicationsProject.Models.GroupModel;
import NetworkApplicationsProject.Models.GroupUserModel;
import NetworkApplicationsProject.Models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface GroupUserRepository extends JpaRepository<GroupUserModel,Integer> {


    List<GroupUserModel> findByUserAndGroupModel(UserModel user, GroupModel group);

    List<GroupUserModel> findByGroupModel(GroupModel group);

    List<GroupUserModel> findByUserId(Integer userId);

}
