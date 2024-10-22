package NetworkApplicationsProject.CustomExceptions;

import jakarta.validation.ValidationException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends ValidationException {

    private final HttpStatus statusCode;

    private final String message;

    public CustomException(String message, HttpStatus statusCode) {
        this.statusCode = statusCode;
        this.message = message;
    }

}