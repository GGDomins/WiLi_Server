package wili_be.entity;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Member implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String username;
    private String birthday;
    private String email;
    private String snsId;
    private String favorites;
    private String password;
    @Enumerated(EnumType.STRING)
    private LoginProvider loginProvider;
    private boolean isBan;
    private boolean isAdmin;
    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE) // member을 지우면 member와 관련된 post들이 모두 삭제가 된다.
    private List<Post> posts = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        if (isAdmin) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.isBan;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.isBan;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.isBan;
    }

    @Override
    public boolean isEnabled() {
        return this.isBan;
    }

}
