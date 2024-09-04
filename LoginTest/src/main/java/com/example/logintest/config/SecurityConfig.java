//package com.example.logintest.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//
//@Configuration
//@EnableWebSecurity //스프링 시큐리티 필터가 스프링 필터체인에 등록이 됨
//public class SecurityConfig extends WebSecurityConfigurerAdapter {
//
//    @Bean
//    public BCryptPasswordEncoder encodePwd() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.csrf().disable();
//        http.authorizeRequests()
//                .andMatchers("/user/**").authenticated()
//                .antMatchers("/manager/**").access("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
//                .andMatchers("/admin/**").aceess("hasRole('ROLE_ADMIN')")
//                .anyRequest().permitAll()
//                .and()
//                .formLogin()
//                .loginPage("/loginForm");
//                .loginProcessingURL("/login")
//                .defaultSuccessUrl("/");
//                .and()
//                .oauth2Login()
//                .loginPage("/loginForm")
//                .userInfoEndPoint()
//                .userService(null);
//    }
//
//}

//스프링부트 3.3.3, 스프링시큐리티6 기준 코드
package com.example.logintest.config;

import com.example.logintest.config.oauth.PrincipalOauth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity //스프링 시큐리티 필터가 스프링 필터체인에 등록이 됨
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true) // Secured 어노테이션 활성화, PreAuthorized 어노테이션 활성화
public class SecurityConfig {

    @Autowired
    private PrincipalOauth2UserService principalOauth2UserService;

    //Password 암호화
    @Bean
    public BCryptPasswordEncoder encodePwd() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // CSRF 보호 비활성화
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/user/**").authenticated()  // "/user/**" URL은 인증된 사용자만 접근 가능
                        .requestMatchers("/manager/**").hasAnyRole("ADMIN", "MANAGER") // 관리자 또는 매니저 역할만 접근 가능
                        .requestMatchers("/admin/**").hasRole("ADMIN") // 관리자 역할만 접근 가능
                        .anyRequest().permitAll() // 그 외의 모든 요청은 허용
                )
                .formLogin(form -> form
                        .loginPage("/loginForm")  // 커스텀 로그인 페이지
                        .loginProcessingUrl("/login") // /login 주소가 호출이 되면 시큐리티가 낚아채서 대신 로그인을 진행
                        .defaultSuccessUrl("/user/myPage")  // 로그인 성공 시 리다이렉트할 기본 URL
                        .permitAll()  // 로그인 페이지는 모든 사용자에게 접근 허용
                )
                //OAuth2 인증 후처리 과정 => 1.코드받기(인증), 2.액세스토큰(권한), 3.사용자프로필정보 가져오기, 4.그 정보를 토대로 회원가입 진행
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/loginForm")  // OAuth2 로그인 페이지 설정, 구글 로그인이 완료된 뒤의 후처리가 필요함.
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(principalOauth2UserService) // OAuth2 사용자 서비스 설정 (필요시 구현체 설정)
                        )
                        .defaultSuccessUrl("/user/myPage")
                );

        return http.build();
    }
}

