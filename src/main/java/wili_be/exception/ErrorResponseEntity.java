package wili_be.exception;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static wili_be.exception.CustomExceptions.*;

@Data
@Builder
public class ErrorResponseEntity {
    private int status;
    private String message;

    public static ResponseEntity<ErrorResponseEntity> toResponseEntity(CustomException e) {
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(ErrorResponseEntity.builder()
                        .status(e.getHttpStatus().value())
                        .message(e.getInfo())
                        .build());
    }
    public static ResponseEntity<String> handleExpiredTokenException(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("WWW-Authenticate", "Bearer error=\"invalid_token\"")
                .body(message);
    }
    public static ResponseEntity<String> handleBadRequestException(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(message);
    }
    public static ResponseEntity<String> createUnauthorizedResponse(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("WWW-Authenticate", "not-logged-in")
                .body(message);
    }
}

