package NetworkApplicationsProject.Repositories;

import NetworkApplicationsProject.Models.FileModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileModel, Integer> {

    List<FileModel> findByGroupId(Integer groupId);

    List<FileModel> findByOwnerId(Integer ownerId);

}