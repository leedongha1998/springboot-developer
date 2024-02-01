package lee.dongha.springbootdeveloper.config.jwt;

import io.jsonwebtoken.Jwts;
import lee.dongha.springbootdeveloper.domain.User;
import lee.dongha.springbootdeveloper.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class TokenProviderTest {
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 토큰을 생성하는 메서드 테스트
     */
    @DisplayName("generateToken() : 유저 정보와 만료 기간을 전달해 토큰을 만들 수 있다.")
    @Test
    public void generateToken() throws Exception{
        //given
        User testUser = userRepository.save(User.builder()
                        .email("user@email.com")
                        .password("test")
                .build());
        //when
        String token = tokenProvider.generateToken(testUser, Duration.ofDays(14)); // 토큰 생성
        //then jjwt 라이브러리를 사용해 토큰을 복호화. 토큰을 만들 때 클레임으로 넣어둔 id값이 given 절에서 만든 유저 id와 동일한 지 확인한다.
        Long userId = Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())
                .parseClaimsJws(token)
                .getBody()
                .get("id",Long.class);

        assertThat(userId).isEqualTo(testUser.getId());
    }

    @DisplayName("validToken() : 만료된 토큰인 때에 유효성 검증에 실패한다.")
    @Test
    public void validToken_invalidToken() throws Exception{
        //given jjwt 라이브러리를 사용해 토큰 생성, 이때 만료 시간은 1970년1월1일부터 현재 시간을 밀리초 단위로 치환한 값 (new Date().getTime())에 1000을 빼 이미 만료된 토큰 반환받는다.
        String token = JwtFactory.builder()
                .expiration(new Date(new Date().getTime() - Duration.ofDays(7).toMillis()))
                .build().createToken(jwtProperties);
        //when 토큰 제공자의 validToken() 메소드를 호출해 유효한 토큰인지 검증한 뒤 결괏값을 반환받는다.
        boolean result = tokenProvider.validToken(token);
        //then
        assertThat(result).isFalse();
    }

    @DisplayName("validToken() : 유효한 토큰일 때 유효성 검증 성공한다.")
    @Test
    public void validToken_validToken() throws Exception{
        //given
        String token = JwtFactory.withDefaultValues().createToken(jwtProperties);
        //when
        boolean result = tokenProvider.validToken(token);
        //then
        assertThat(result).isTrue();
    }

    /**
     * 인증 정보를 담은 객체 Authentication을 반환하는 메소드인 getAuthentication() 테스트
     */
    @DisplayName("getAuthentication(): 토큰 기반으로 인증 정보를 가져올 수 있다.")
    @Test
    public void getAuthentication() throws Exception{
        //given jjwt 라이브러리를 사용해 토큰을 생성한다. 이때 토큰의 제목인 subject는 "user@email.com"라는 값을 사용한다.
        String userEamil = "user@email.com";
        String token = JwtFactory.builder()
                .subject(userEamil)
                .build()
                .createToken(jwtProperties);
        //when 토큰 제공자의 getAuthentication() 메서드를 호출해 인증 객체를 반환받는다.
        Authentication authentication = tokenProvider.getAuthentication(token);
        //then 반환받은 인증 객체의 유저 이름을 가져와 설정한 subject 값인 "user@email.com"과 같은지 확인
        assertThat(((UserDetails)authentication.getPrincipal()).getUsername()).isEqualTo(userEamil);
    }

    /**
     * 토큰 기반으로 유저 아이디를 가져오는 메소드 테스트
     * @throws Exception
     */
    @DisplayName("getUserId(): 토큰으로 유저 id를 가져올 수 있다.")
    @Test
    public void getUserId() throws Exception{
        //given jjwt 라이브러리로 토큰을 생성하고 이때 클레임을 추가한다. 키는 아이디, 값은 1이라는 유저 아이디이다.
        Long userId = 1L;
        String token = JwtFactory.builder()
                .claims(Map.of("id",userId))
                .build()
                .createToken(jwtProperties);
        //when 토큰 제공자의 getUserId() 를 호출해 유저 아이디를 반환받음
        Long userIdByToken = tokenProvider.getUserId(token);
        //then 반환받은 유저 아이디가 given절에서 설정한 유저 아이디 값인 1과 같은지 확인
        assertThat(userIdByToken).isEqualTo(userId);
    }
}
