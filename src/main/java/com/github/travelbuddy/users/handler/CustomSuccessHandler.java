package com.github.travelbuddy.users.handler;

import com.github.travelbuddy.users.dto.oauth2.CustomOAuth2User;
import com.github.travelbuddy.users.jwt.JWTUtill;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtill jwtUtill;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        Integer userId = customUserDetails.getUserId();

        String token = jwtUtill.createJwt(userId,5*60*60*1000L);

        response.addCookie(createCookies("Authorization",token,5*60*60*1000));
        response.sendRedirect("http://localhost:3000/oauth2-jwt");
    }
    public Cookie createCookies(String key, String value, Integer expiration) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(expiration);
//        cookie.setSecure(true);  // https만 쿠키 사용가능
        cookie.setPath("/"); // 전역에서 쿠키를 볼 수 있음
        cookie.setHttpOnly(true); // javaScript가 해당 쿠키를 가져가지 못하게 함

        return cookie;
    }
}