package wili_be.dto;


import lombok.*;
import wili_be.entity.LoginProvider;
import wili_be.entity.Member;



public class MemberDto {
    @Data
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Member_info_Dto {
        private String name;
        private String email;
        private LoginProvider loginProvider;
        private String snsId;


        public Member to_Entity() {
            return Member.builder()
                    .name(name)
                    .email(email)
                    .loginProvider(loginProvider)
                    .snsId(snsId)
                    .build();
        }
    }
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SocialMemberInfoDto {
        private String id;
        private String nickname;
        private String email;

    }

    @Getter
    @Builder
    public static class AdditionalSignupInfo {
        private String name;
        private String email;
        private LoginProvider loginProvider;
        private String snsId;
        private String username;
        private String birthday;
    }
}
