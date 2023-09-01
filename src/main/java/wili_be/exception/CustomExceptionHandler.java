package wili_be.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static wili_be.exception.CustomExceptions.*;

@ControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponseEntity> handleCustomException(CustomException e) {
        return ErrorResponseEntity.toResponseEntity(e);
    }

    @ExceptionHandler(ExpiredTokenException.class)
    public ResponseEntity<String> handleExpiredTokenException(ExpiredTokenException e) {
        return ErrorResponseEntity.handleExpiredTokenException("토큰이 만료 되었습니다.");
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleBadRequestException(BadRequestException ex) {
        return ErrorResponseEntity.handleBadRequestException("로그인이 안 되어있는 상태");
    }

    @ExceptionHandler(NotLoggedInException.class)
    public ResponseEntity<String> createUnauthorizedResponse(NotLoggedInException e) {
        return ErrorResponseEntity.createUnauthorizedResponse("접근 토큰이 없습니다.");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("파일 크기가 너무 큽니다. 최대 허용 크기는 3MB입니다.");
    }

    @ExceptionHandler(NoProductException.class)
    public ResponseEntity<Map<String, Object>> handleNoproductException(NoProductException e) {
        return ErrorResponseEntity.handleNoProductException();
    }
    @ExceptionHandler(NoUserException.class)
    public ResponseEntity<Map<String, Object>> handleNoUserException(NoUserException e) {
        return ErrorResponseEntity.handleNoUserException();
    }
}
