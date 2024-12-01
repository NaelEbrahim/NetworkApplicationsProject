package NetworkApplicationsProject.Repositories;

import NetworkApplicationsProject.Models.ActivityModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<ActivityModel,Integer> {


    List<ActivityModel> findByFileModelId(Integer fileId);

    List<ActivityModel> findByUserModelId(Integer userId);

    List<ActivityModel> findByUserModelIdAndFileModelId(Integer id, Integer groupId);
}
