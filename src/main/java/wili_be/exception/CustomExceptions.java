package wili_be.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


public class CustomExceptions{
    @AllArgsConstructor
    @Getter
    public static class CustomException extends RuntimeException  {
        private HttpStatus httpStatus;
        private String info;
    }
    @Getter
    public static class ExpiredTokenException extends RuntimeException  {
        private HttpStatus httpStatus;
        private String info;
    }
    @Getter
    public static class BadRequestException extends RuntimeException  {
        private HttpStatus httpStatus;
        private String info;
    }
    @Getter
    public static class NotLoggedInException extends RuntimeException  {
        private HttpStatus httpStatus;
        private String info;
    }

}
