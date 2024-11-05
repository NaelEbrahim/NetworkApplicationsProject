package NetworkApplicationsProject.Testing;

import NetworkApplicationsProject.DTO.Requset.FileRequest;
import NetworkApplicationsProject.Services.FilesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class FileServiceTest {

    @Autowired
    private FilesService fileService;



}
