package lee.dongha.springbootdeveloper.config;

import jakarta.servlet.Filter;
import lee.dongha.springbootdeveloper.config.jwt.TokenProvider;
import lee.dongha.springbootdeveloper.config.oauth.OAuth2AuthorizationRequestBasedOnCookieRepository;
import lee.dongha.springbootdeveloper.config.oauth.OAuth2SuccessHandler;
import lee.dongha.springbootdeveloper.config.oauth.OAuth2UserCustomService;
import lee.dongha.springbootdeveloper.repository.RefreshTokenRepository;
import lee.dongha.springbootdeveloper.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@RequiredArgsConstructor
@Configuration
public class WebOAuthSecurityConfig {
    private final OAuth2UserCustomService oAuth2UserCustomService;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;

    @Bean
    public WebSecurityCustomizer configure(){
        return web -> web.ignoring()
                .requestMatchers(toH2Console())
                .requestMatchers("/img/**","/css/**","/js/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(login->login.disable())
                .logout(logout->logout.disable());

        http
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 헤더를 확인할 커스텀 필터 추가
        http
                .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        // 토큰 재발급 url은 인증 없이 접근 가능하도록 설정, 나머지 api url은 인증 필요
        http
                .authorizeHttpRequests(ah -> ah
                                .requestMatchers(
                        "/api/token"
                ).permitAll()
                                .requestMatchers(
                                        "/api/**"
                                ).authenticated()
                                .anyRequest().permitAll()
                );

        http
                .oauth2Login(ol-> ol.loginPage("/login")
                        .authorizationEndpoint(endPoint -> endPoint.authorizationRequestRepository(oAuth2AuthorizationRequestBaseOnCookieRepository()))
                        .successHandler(oAuth2SuccessHandler()) // 인증 성공시 실행할 핸들러
                        .userInfoEndpoint(endPoint -> endPoint.userService(oAuth2UserCustomService)));

        http
                .logout(logout -> logout.logoutSuccessUrl("/login"));

        // /api로 시작하는 url인 경우 401 상태 코드를 반환하도록 예외 처리
        http
                .exceptionHandling(e -> e.defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),new AntPathRequestMatcher("/api/**")));

        return http.build();
    }

    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler(){
        return new OAuth2SuccessHandler(tokenProvider,refreshTokenRepository,oAuth2AuthorizationRequestBaseOnCookieRepository(),userService);
    }

    @Bean
    public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBaseOnCookieRepository() {
        return new OAuth2AuthorizationRequestBasedOnCookieRepository();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider);
    }
}
