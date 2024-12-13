package NetworkApplicationsProject.Repositories;

import NetworkApplicationsProject.Models.NotificationsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationsRepository extends JpaRepository<NotificationsModel, Integer> {

}
