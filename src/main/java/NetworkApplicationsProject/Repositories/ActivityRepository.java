package NetworkApplicationsProject.Repositories;

import NetworkApplicationsProject.Models.ActivityModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends JpaRepository<ActivityModel,Integer> {



}
