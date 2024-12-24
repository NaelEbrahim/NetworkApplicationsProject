package NetworkApplicationsProject.Services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;


@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    public void sendNotification(String fcmToken, String title, String body) {
        try {
            Message message = Message.builder()
                    .putData("title", title)
                    .putData("body", body)
                    .setToken(fcmToken)
                    .build();

            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}