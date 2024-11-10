package NetworkApplicationsProject.Repositories;

import NetworkApplicationsProject.Models.GroupModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<GroupModel, Integer> {

    Optional<GroupModel> findByName(String name);

    Optional<GroupModel> findBySlug(String groupSlug);
}
