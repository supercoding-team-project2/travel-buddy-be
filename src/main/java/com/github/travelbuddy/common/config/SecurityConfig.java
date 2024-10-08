package com.github.travelbuddy.common.config;

import com.github.travelbuddy.chat.service.ChatUserService;
import com.github.travelbuddy.users.handler.CustomSuccessHandler;
import com.github.travelbuddy.users.jwt.JWTFilter;
import com.github.travelbuddy.users.jwt.JWTUtill;
import com.github.travelbuddy.users.jwt.LoginFilter;
import com.github.travelbuddy.users.service.CustomOAuth2UserService;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtill jwtUtill;
    private final ChatUserService chatUserService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)throws Exception {
        return configuration.getAuthenticationManager();
    }

    //비밀번호 암호화
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain FilterChain(HttpSecurity http) throws Exception {
            http
                    .cors((cors) -> cors
                            .configurationSource(new CorsConfigurationSource(){
                                @Override
                                public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                                    CorsConfiguration configuration = new CorsConfiguration();

                                    configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
                                    // 웹소켓 테스트를 위해 허용
                                    configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
                                    //허용할 method
                                    configuration.setAllowedMethods(Collections.singletonList("*"));
                                    configuration.setAllowCredentials(true);
                                    configuration.setAllowedHeaders(Collections.singletonList("*"));
                                    configuration.setMaxAge(3600L);

                                    //Authorization에 jwt를 넣어서 보내주기 때문에 허용
//                                configuration.setExposedHeaders(Collections.singletonList("Authorization"));
//                                configuration.setExposedHeaders(Collections.singletonList("Set-Cookie"));
                                    configuration.setExposedHeaders(Arrays.asList("Authorization","Set-Cookie"));

                                    return configuration;
                                }
                            }))
                .csrf((auth) -> auth.disable())
                .formLogin((auth) -> auth.disable())
                .httpBasic((auth) -> auth.disable())
                .oauth2Login((oauth2) -> oauth2
                        .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                                .userService(customOAuth2UserService))
                        .successHandler(customSuccessHandler))
//                .oauth2Login(oauth2 -> oauth2
//                        .redirectionEndpoint(endpoint -> endpoint.baseUri("/login/oauth2/code/*"))
//                        .userInfoEndpoint(endpoint -> endpoint.userService(customOAuth2UserService)))
                .authorizeHttpRequests((auth) -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/login/oauth2/**").permitAll()
                        .requestMatchers("/api/user/signup/sms/send").permitAll()
                        .requestMatchers("/api/user/signup/sms/check").permitAll()
                        .requestMatchers("/api/user/sms-code/check").permitAll()
                        .requestMatchers("/api/user/password/*").permitAll()
                        .requestMatchers("/api/user/*").permitAll()
                        .requestMatchers("/api/user").permitAll()
                        .requestMatchers("/api/boards").permitAll()
                        .requestMatchers("/api/boards/*").permitAll()
                        .requestMatchers("/api/likes/*").permitAll()
                        .requestMatchers("/api/comment/*").permitAll()
                        .requestMatchers("/api/routes/*").permitAll()
                        .requestMatchers("/ws").permitAll()
                        .requestMatchers("/api/attend/*").permitAll()
                        .anyRequest().authenticated())
                .addFilterAfter(new JWTFilter(jwtUtill), OAuth2LoginAuthenticationFilter.class)
//                .addFilterBefore(new JWTFilter(jwtUtill), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        LoginFilter loginFilter = new LoginFilter(authenticationManager(authenticationConfiguration),jwtUtill, chatUserService);
        loginFilter.setFilterProcessesUrl("/api/user/login");

        http
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
