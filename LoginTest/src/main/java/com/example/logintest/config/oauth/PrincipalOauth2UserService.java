package com.example.logintest.config.oauth;

import com.example.logintest.config.auth.PrincipalDetails;
import com.example.logintest.model.User;
import com.example.logintest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

//@Service
//public class PrincipalOauth2UserService extends DefaultOAuth2UserService {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    //구글로 부터 받은 userRequest 데이터에 대한 후처리되는 함수
//    //함수 종료시 @AuthenticationPrincial 어노테이션이 만들어진다
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//        System.out.println("getClientRegistration : " + userRequest.getClientRegistration());
//        System.out.println("getAccessToken : " + userRequest.getAccessToken().getTokenValue());
//
//        OAuth2User oAuth2User = super.loadUser(userRequest);
//        //구글 로그인 클릭 -> 구글 로그인창 -> 로그인 완료 -> code 리턴(OAuth-Client) -> AccessToken 요청
//        //userRequest 정보 -> loadUser 함수 -> 구글로부터 회원 프로필 수령
//        System.out.println("getAttributes : " + oAuth2User.getAttributes());
//
//        //회원가입을 강제로 진행해 볼 예정
//        String provider = userRequest.getClientRegistration().getClientId(); //google
//        String providerId = oAuth2User.getAttribute("sub");
//        String username = provider + "_" + providerId; //google_숫자
//
//        // BCryptPasswordEncoder 인스턴스를 직접 생성하여 사용
//        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//        String password = encoder.encode("겟인데어");
//        String email = oAuth2User.getAttribute("email");
//        String role = "ROLE_USER";
//
//        User userEntity = userRepository.findByUsername(username);
//
//        if(userEntity == null) {
//            userEntity = User.builder()
//                    .username(username)
//                    .password(password)
//                    .email(email)
//                    .role(role)
//                    .provider(provider)
//                    .providerId(providerId)
//                    .build();
//            userRepository.save(userEntity);
//        }
//
//        return new PrincipalDetails(userEntity, oAuth2User.getAttributes());
//    }
//}

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 구글 인증 후 유저 정보 가져오기
        String provider = userRequest.getClientRegistration().getClientId(); // "google"
        String providerId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");

        // 이메일을 통해 기존 사용자 조회
        User userEntity = userRepository.findByEmail(email);
        if (userEntity != null) {
            // 사용자 정보가 존재하고, 구글 정보가 연동되지 않은 경우에만 업데이트
            if (userEntity.getProvider() == null || userEntity.getProviderId() == null) {
                userEntity.setProvider(provider);
                userEntity.setProviderId(providerId);
                userRepository.save(userEntity);
            }
        } else {
            // 사용자 정보가 없으면 예외를 던지거나, 별도의 로직을 처리
            throw new OAuth2AuthenticationException("User not found. Please register first.");
        }

        // 연동된 사용자 정보를 반환
        return new PrincipalDetails(userEntity, oAuth2User.getAttributes());
    }
}

