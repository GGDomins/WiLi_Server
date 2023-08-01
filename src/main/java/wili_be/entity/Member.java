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
public class Member implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String username;
    private String birthday;
    private String email;
    private String snsId;
    @Enumerated(EnumType.STRING)
    private LoginProvider loginProvider;
    private boolean isBan;
    private boolean isAdmin;
    @OneToMany(mappedBy = "member")
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
        return null;
    }

    @Override
    public String getUsername() {
        return this.email;
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
