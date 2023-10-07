package wili_be.controller.status;

import lombok.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@NoArgsConstructor
public class ApiResponse {
    private Map<String, Object> status = new HashMap<>();
    private Map<String, Object> data = new HashMap<>();
    private Map<String, Object> message = new HashMap<>();

    public ApiResponse(String status, String message, Object data) {
        this.status.put("status", status);
        this.message.put("message", message);
        this.data.put("data", data);
    }

    public void addStatus(String s) {
        this.status.put("status", s);
    }
    public void addMessage(String m) {
        this.message.put("message", m);
    }

    public void addData_WithOutTitle(Object data) {
        this.data.put("data", data);
    }
    public void addNullData() {
        this.data.put("data", null);
    }


    //  /user/auth
    public void success_user_auth(String snsId) {
        addStatus("true");
        Map<String, Object> new_data = new HashMap<>();
        new_data.put("snsId", snsId);
        addData_WithOutTitle(new_data);
        addMessage("Authentication success");
    }

    //  /user/refresh-token
    public void success_user_refresh_Token(String snsId) {
        addStatus("true");
        Map<String, Object> new_data = new HashMap<>();
        new_data.put("snsId", snsId);
        addData_WithOutTitle(new_data);
        addMessage("token refresh success");
    }
    public void fail_user_refresh_Token() {
        addStatus("fail");
        addNullData();
        addMessage("token refresh failed");
    }

    // kakao & naver callback
    public void success_oauth_login(Object memberInfo) {
        addStatus("true");
        addData_WithOutTitle(memberInfo);
        addMessage("Login success");
    }
    public void fail_oauth_login() {
        addStatus("false");
        addMessage("Login failed");
        addNullData();
    }

}
