package wili_be.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import wili_be.security.jwt.JwtAuthenticationFilter;
import wili_be.security.jwt.JwtTokenProvider;


@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate redisTemplate;
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder(); // 비밀번호 암호화를 위한 빈 생성
    }


    @Bean
    @Override    // authenticationManager를 Bean 등록합니다.
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .logout().disable()
                /**
                 * logout().disable(): 이 설정은 로그아웃 기능을 비활성화합니다. 로그아웃 기능이 필요하지 않거나, 다른 인증 방식을 사용하는 경우에 사용할 수 있습니다.
                 * 예를 들어, 애플리케이션에서 토큰 기반 인증을 사용하는 경우에는 세션 기반의 로그아웃 기능을 비활성화할 수 있습니다.
                 * */
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                /**
                 * sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS): 이 설정은 세션의 생성 방식을 설정합니다. STATELESS는 무상태(stateless) 세션을 의미합니다.
                 * 즉, 서버에 클라이언트의 세션 정보를 저장하지 않고, 모든 요청이 독립적으로 처리되는 방식입니다.
                 * 이는 RESTful API와 같이 세션 상태를 유지할 필요가 없는 상황에서 사용될 수 있습니다. 무상태 세션은 서버의 확장성과 보안을 향상시킬 수 있습니다.
                 */
//                .logoutRequestMatcher(new AntPathRequestMatcher("/logout")) // 로그아웃을 수행하기 위한 요청 경로 설정
//                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))
                .and()
                .cors() // CORS 구성을 활성화합니다.
                /**
                 * @CrossOrigin(origins = "https://restful-jwt-project.herokuapp.com") 어노테이션은 Spring MVC에서 CORS를 구성하는 방법 중 하나입니다.
                 *
                 * 그러나 앞서 제안한 방법 중 두 번째 방법을 사용하여 Spring Security 설정에서 CORS를 구성했다면, @CrossOrigin 어노테이션을 사용할 필요는 없습니다. Spring Security의 CORS 구성은 모든 엔드포인트에 대해 적용되기 때문에, @CrossOrigin 어노테이션을 개별적으로 추가할 필요가 없습니다.
                 *
                 * 따라서, 주어진 코드에서 @CrossOrigin(origins = "https://restful-jwt-project.herokuapp.com") 어노테이션을 제거해도 됩니다. Spring Security 설정에 의해 CORS가 이미 구성되었으므로, 해당 도메인에서의 접근이 허용될 것입니다.
                 */
                .and()
                .csrf().disable()
                .httpBasic().disable()
                .authorizeRequests()
                .antMatchers("/test").authenticated()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/user/**").hasRole("USER")
                .antMatchers("/**").permitAll()
                .and()
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, redisTemplate),
                        UsernamePasswordAuthenticationFilter.class);

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }


}