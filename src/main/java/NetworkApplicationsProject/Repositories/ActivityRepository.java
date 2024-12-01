package NetworkApplicationsProject.Repositories;

import NetworkApplicationsProject.Models.ActivityModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<ActivityModel,Integer> {


    List<ActivityModel> findByFileModelId(Integer fileId);

    @Query("SELECT a FROM ActivityModel a WHERE a.userModel.id = :userId AND a.fileModel.group.id = :groupId")
    List<ActivityModel> findByUserAndGroup(@Param("userId") Integer userId, @Param("groupId") Integer groupId);


    @Query("SELECT a FROM ActivityModel a WHERE a.userModel.id = :userId AND a.fileModel.id = :fileId AND a.fileModel.group.id = :groupId")
    List<ActivityModel> findByUserAndFileAndGroup(@Param("userId") Integer userId, @Param("fileId") Integer fileId, @Param("groupId") Integer groupId);
}
