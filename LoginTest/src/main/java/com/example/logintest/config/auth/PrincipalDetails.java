package com.example.logintest.config.auth;

//시큐리티가 /login 주소 요청을 낚아채서 로그인을 진행시킨다.
//로그인을 진행이 완료가 되면 Security Session을 만들어줍니다. (Security ContextHolder)
//오브젝트 타입 => Authentication 타입의 객체
//Authentication 안에 User 정보가 있어야 됨
//User 오브젝트의 타입 => UserDetails 타입의 객체

//Security Session => Authentication => UserDetails(PrincipalDetails)

import com.example.logintest.model.User;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Data
public class PrincipalDetails implements UserDetails, OAuth2User {

    private User user; //콤포지션
    private Map<String, Object> attributes;

    //일반 로그인용 생성자
    public PrincipalDetails(User user) {
        this.user = user;
    }
    
    //OAuth2 로그인용 생성자
    public PrincipalDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    //해당 User의 권한을 리턴하는 곳
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        Collection<GrantedAuthority> collect = new ArrayList<>();
//        collect.add(new GrantedAuthority() {
//            @Override
//            public String getAuthority() {
//                return user.getRole();
//            }
//        });
//        return collect;
//    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collect = new ArrayList<>();
        collect.add((GrantedAuthority) () -> user.getRole());
        return collect;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        //우리 서비스의 사용자가 1년 이상 사용하지 않아 휴면 처리
        //현재시간 - 최종 로그인시간 => 1년을 초과하면 return false;
        return true;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return null;
    }
}
