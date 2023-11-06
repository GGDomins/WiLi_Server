package wili_be.controller.status;

import lombok.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@NoArgsConstructor
public class ApiResponse {
    private String status;
    private Map<String, Object> data = new HashMap<>();
    private String message;


    public void addStatus(String s) {
        this.status = s;
    }

    public void addMessage(String m) {
        this.message = m;
    }

    public void addData_WithOutTitle(Map<String, Object> data) {
        this.data = data;
    }

    public void addNullData() {
        this.data = null;
    }


    //  /user/auth
    public void success_user_auth(String snsId) {
        addStatus("true");
        Map<String, Object> new_data = new HashMap<>();
        new_data.put("snsId", snsId);
        addData_WithOutTitle(new_data);
        addMessage("Authentication success");
    }

    //  /users/normal-signup
    public void success_user_signupForm() {
        addStatus("true");
        addNullData();
        addMessage("signup success");
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
        Map<String, Object> new_data = new HashMap<>();
        new_data.put("member_info", memberInfo);
        addData_WithOutTitle(new_data);
        addMessage("Login success");
    }

    public void fail_oauth_login() {
        addStatus("false");
        addMessage("Login failed");
        addNullData();
    }

    // user/logout
    public void success_user_logout() {
        addStatus("success");
        addMessage("Logout success");
        addNullData();
    }

    // PATCH user/{snsId}
    public void success_user_editInfo(Object o) {
        addStatus("success");
        addMessage("user info edit success");
        Map<String, Object> new_data = new HashMap<>();
        new_data.put("member_Info", o);
        addData_WithOutTitle(new_data);
    }

    //DELETE user/{snsId}
    public void success_user_delete() {
        addStatus("success");
        addNullData();
        addMessage("withdraw success");
    }

    public void fail_user_delete() {
        addStatus("fail");
        addNullData();
        addMessage("withdraw failed");
    }

    // post/add
    public void success_post_add() {
        addStatus("success");
        addNullData();
        addMessage("item upload success");
    }

    public void fail_post_add() {
        addStatus("false");
        addNullData();
        addMessage("item upload failed");
    }
    // GET /users/product
    public void success_lookup_product(Map<String, Object> data) {
        addStatus("true");
        addData_WithOutTitle(data);
        addMessage("items fetch success");
    }

    public void failed_lookup_product() {
        addStatus("false");
        addNullData();
        addMessage("item fetch failed");
    }

    // products/{PostId}
    public void success_post_lookup(Map<String, Object> data) {
        addStatus("true");
        addMessage("item look up success");
        addData_WithOutTitle(data);
    }

    // PATCH /products/{ID}
    public void success_post_edit() {
        addStatus("true");
        addNullData();
        addMessage("item edit success");
    }

    public void failed_post_edit() {
        addStatus("false");
        addNullData();
        addMessage("item edit failed");
    }

    //DELETE /products/{id}
    public void success_post_delete() {
        addStatus("true");
        addNullData();
        addMessage("item delete success");
    }
    public void failed_post_delete() {
        addStatus("false");
        addNullData();
        addMessage("item delete failed");
    }

    // /random-feed
    public void success_random_feed(Map<String, Object> data) {
        addStatus("true");
        addData_WithOutTitle(data);
        addMessage("items fetch success");
    }

    public void failed_random_feed() {
        addStatus("false");
        addNullData();
        addMessage("item fetch failed");
    }


    // /search?query
    public void success_search_product(Map<String, Object> data) {
        addStatus("true");
        addData_WithOutTitle(data);
        addMessage("search success");
    }
    public void success_search_user(Map<String, Object> data) {
        addStatus("true");
        addData_WithOutTitle(data);
        addMessage("search success");
    }

    public void failed_search_product() {
        addStatus("false");
        addNullData();
        addMessage("no product found");
    }
    public void failed_search_user() {
        addStatus("false");
        addNullData();
        addMessage("no user found");
    }

    public void failed_search() {
        addStatus("false");
        addNullData();
        addMessage("search failed");
    }
}
