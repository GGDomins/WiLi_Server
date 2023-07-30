package wili_be.controller.status;
/**
 * Response의 Message 필드에 사용되는 static final 변수들입니다.
 */
public class ResponseMessage {
    public static final String LOGIN_SUCCESS = "로그인 성공";
    public static final String LOGIN_FAILED = "로그인 실패";
    public static final String LOGOUT_SUCCESS = "로그아웃 성공";
    public static final String LOGOUT_FAILED = "로그아웃 실패";
    public static final String SIGNIN_SUCCESS = "회원 가입 성공";
    public static final String EMAIL_OK = "사용 가능한 이메일입니다.";
    public static final String EMAIL_EXISTED = "이미 사용 중인 이메일입니다.";
    public static final String NOT_FOUND_USER = "회원을 찾을 수 없습니다.";
    public static final String UNAUTHORIZED = "회원 인증 실패";
    public static final String AUTHORIZED = "회원 인증 성공";
    public static final String EMAIL_NOT_FOUND = "이메일을 잘못 입력하셨습니다.";
    public static final String PASSWORD_ERROR = "비밀번호를 잘못 입력하셨습니다.";
    public static final String REFRESH_TOKEN_SUCCESS = "토큰 재발급 성공";
    public static final String REFRESH_TOKEN_FAIL = "토큰 재발급 실패";
    public static final String PASSWORD_CHANGE_OK = "비밀번호 변경 완료";
}
