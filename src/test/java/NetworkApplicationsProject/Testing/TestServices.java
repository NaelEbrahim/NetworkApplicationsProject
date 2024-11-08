package NetworkApplicationsProject.Testing;

import NetworkApplicationsProject.DTO.Requset.FilesRequests.CheckInFilesRequest;
import NetworkApplicationsProject.Services.FilesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class TestServices {

    @Autowired
    FilesService filesService;

    @Test
    public void testOptimisticLockingOnFileReservation() {
        // Define Dummy Request Data
        CheckInFilesRequest fileRequest = new CheckInFilesRequest();
        fileRequest.setGroupId(1);
        ArrayList<Integer> fileIds = new ArrayList<>();
        fileIds.add(3);
        fileRequest.setFileIds(fileIds);
        // Set up a latch for simultaneous thread start
        CountDownLatch latch = new CountDownLatch(1);
        // Define the concurrent tasks
        Runnable task = () -> {
            try {
                latch.await();  // Wait for both threads to be ready
                String result = filesService.checkInFilesOptimistically(fileRequest);
                System.out.println("Thread succeeded: " + result);
            } catch (OptimisticLockingFailureException e) {
                System.out.println("Optimistic locking exception caught!");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        };
        // Execute tasks concurrently
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(task);
        executor.execute(task);
        // Start both threads
        latch.countDown();
        // Wait for tasks to complete
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);  // Wait for up to 5 seconds
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}