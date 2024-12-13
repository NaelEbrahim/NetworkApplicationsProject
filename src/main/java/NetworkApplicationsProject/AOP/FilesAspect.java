package NetworkApplicationsProject.AOP;

import NetworkApplicationsProject.Models.ActivityModel;
import NetworkApplicationsProject.Models.FileModel;
import NetworkApplicationsProject.Repositories.ActivityRepository;
import NetworkApplicationsProject.Tools.HandleCurrentUserSession;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Aspect
@Component
public class FilesAspect {

    @Autowired
    private ActivityRepository activityRepository;

    private static final Logger logger = LoggerFactory.getLogger(FilesAspect.class);

    // capture this methods
    @Pointcut("execution(* NetworkApplicationsProject.Services.FilesService.checkInFilesOptimistically(..)) || " +
            "execution(* NetworkApplicationsProject.Services.FilesService.checkOutFilesOptimistically(..))")
    public void fileActions() {
    }

    @Around("fileActions()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = null;
        long elapsedTime = 0;
        //
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        logger.info("Around (Before): Method {} started with arguments: {}", methodName, args);

        try {
            long startTime = System.currentTimeMillis();
            result = joinPoint.proceed(); // Proceed to the target method
            elapsedTime = System.currentTimeMillis() - startTime;

        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }

        logger.info("Around (After): Method {} completed in {} ms", methodName, elapsedTime);

        // print on terminal
        System.out.println(result);
        // save Tracing in Database
        if (methodName.equals("checkInFilesOptimistically") || methodName.equals("checkOutFilesOptimistically")) {
            storeTracingInDataBase((ResponseEntity<List<FileModel>>) result, methodName);
        }
        // return result to caller
        return result;
    }

    private void storeTracingInDataBase(ResponseEntity<List<FileModel>> executionResult, String tracingType) {
        List<FileModel> fileModels = executionResult.getBody();
        if (fileModels != null && !fileModels.isEmpty()) {
            for (FileModel temp : fileModels) {
                if (temp != null) {
                    ActivityModel activityModel = new ActivityModel();
                    activityModel.setFileModel(temp);
                    activityModel.setActivityType(tracingType);
                    activityModel.setUserModel(HandleCurrentUserSession.getCurrentUser());
                    activityModel.setActivityDate(LocalDateTime.now());
                    activityRepository.save(activityModel);
                }
            }
        }
    }

}