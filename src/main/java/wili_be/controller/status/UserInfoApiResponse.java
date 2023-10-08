package wili_be.controller.status;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@NoArgsConstructor
public class UserInfoApiResponse {
    private String status;
    private Object data;
    private String message;

    public void addStatus(String s) {
        this.status = s;
    }

    public void addMessage(String m) {
        this.message = m;
    }

    public void addData(Object data) {
        this.data = data;
    }
    // /users/{snsId}

    public void success_user_getInfo(Object o) {
        addStatus("success");
        addMessage("user info look up success");
        addData(o);
    }
}
