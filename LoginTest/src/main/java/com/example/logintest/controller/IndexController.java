package com.example.logintest.controller;

import com.example.logintest.config.auth.PrincipalDetails;
import com.example.logintest.model.User;
import com.example.logintest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IndexController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/test/login")
    public @ResponseBody String testLogin(Authentication authentication,
                                          @AuthenticationPrincipal PrincipalDetails userDetails) {
        System.out.println("/test/login =======");
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        System.out.println("authentication : " + principalDetails.getUser());

        System.out.println("userDetails : " + userDetails.getUser());
        return "세션 정보 확인하기";
    }

    @GetMapping("/test/oauth/login")
    public @ResponseBody String testOAuthLogin(Authentication authentication,
                                               @AuthenticationPrincipal OAuth2User oauth) {
        System.out.println("/test/oauth/login =======");
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        System.out.println("authentication : " + oAuth2User.getAttributes()); //아래와 결과가 같음
        System.out.println("oAuth2User : " + oauth.getAttributes()); //위와 결과가 같음

        return "OAuth 세션 정보 확인하기";
    }

    //localhost:3500
    @GetMapping({"", "/"})
    public String index() {
        //Mustache 기본폴더 src/main/resources/
        //뷰 리졸버 설정 : templates (prefix), .mustache(suffix)
        return "index";
    }

    @GetMapping("/user")
    public @ResponseBody String user(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        System.out.println("principalDetails : " + principalDetails.getUser());
        return "user";
    }

    @GetMapping("/admin")
    public @ResponseBody String admin() {
        return "admin";
    }

    @GetMapping("/manager")
    public @ResponseBody String manager() {
        return "manager";
    }

    //스프링시큐리티가 해당주소를 낚아챔 - SecurityConfig 파일 생성하면 작동 안함.
    @GetMapping("/loginForm")
    public String loginForm() {
        return "loginForm";
    }

    @GetMapping("/joinForm")
    public String joinForm() {
        return "joinForm";
    }

    @PostMapping("/join")
    public String join(User user) {
        System.out.println(user);
        user.setRole("USER");
        String rawPassword = user.getPassword();
        String encPassword = bCryptPasswordEncoder.encode(rawPassword);
        user.setPassword(encPassword);

        userRepository.save(user); //회원가입은 잘 되지만, 시큐리티로 로그인을 할 수 없다. Password가 암호화 되지 않았기 때문.
        return "redirect:/loginForm";
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/info")
    public @ResponseBody String info() {
        return "개인정보";
    }

    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    @GetMapping("/data")
    public @ResponseBody String data() {
        return "데이터정보";
    }

    @GetMapping("/user/myPage")
    public String myPage(@AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {
        // 사용자 정보를 모델에 추가
        model.addAttribute("principal", principalDetails);
        return "myPage"; // Mustache 템플릿 파일 이름
    }

    @GetMapping("/link/google")
    public String linkGoogle(Authentication authentication, @AuthenticationPrincipal OAuth2User oauthUser) {
        if (oauthUser != null) {
            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
            User currentUser = principalDetails.getUser();

            String provider = "google";
            String providerId = oauthUser.getAttribute("sub");

            // 현재 로그인된 사용자가 구글 정보와 연동되지 않은 경우에만 처리
            if (currentUser.getProvider() == null && currentUser.getProviderId() == null) {
                currentUser.setProvider(provider);
                currentUser.setProviderId(providerId);
                userRepository.save(currentUser);
            }
        }
        return "redirect:/user/mypage"; // 연동 후 마이페이지로 리다이렉션
    }

}
