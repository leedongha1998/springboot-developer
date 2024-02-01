package lee.dongha.springbootdeveloper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lee.dongha.springbootdeveloper.domain.Article;
import lee.dongha.springbootdeveloper.domain.User;
import lee.dongha.springbootdeveloper.dto.AddArticleReq;
import lee.dongha.springbootdeveloper.dto.UpdateArticleReq;
import lee.dongha.springbootdeveloper.repository.BlogRepository;
import lee.dongha.springbootdeveloper.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.security.Principal;
import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class BlogApiControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper; // 직렬화, 역직렬화

    @Autowired
    private WebApplicationContext webContext;

    @Autowired
    BlogRepository blogRepository;
    @Autowired
    UserRepository userRepository;

    User user;

    @BeforeEach
    public void mockMvcSetUp(){
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webContext).build();
        blogRepository.deleteAll();
    }

    @BeforeEach
    void setSecurityContext(){
        userRepository.deleteAll();
        user = userRepository.save(User.builder()
                        .email("user@mail.com")
                        .password("test")
                .build());
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user,user.getPassword(),user.getAuthorities()));
    }

    @Test
    @DisplayName("addArticle: 블로그 글 추가에 성공한다.")
    public void addArticle() throws Exception{
        // given
        final String url = "/api/articles";
        final String title = "title";
        final String content = "content";
        final AddArticleReq userRequest = new AddArticleReq(title,content);

        final String requestBody = objectMapper.writeValueAsString(userRequest);

        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn("username");

        // when
        ResultActions result = mockMvc.perform(post(url)
                .contentType(APPLICATION_JSON_VALUE)
                        .principal(principal)
                .content(requestBody));

        // then
        result.andExpect(status().isCreated());

        List<Article> articles = blogRepository.findAll();

        assertThat(articles.size()).isEqualTo(1);
        assertThat(articles.get(0).getTitle()).isEqualTo(title);
        assertThat(articles.get(0).getContent()).isEqualTo(content);
    }

    @Test
    @DisplayName("findAllArticles: 블로그 글 목록 조회에 성공한다.")
    public void findAllArticles() throws Exception{
        //given
        //블로그 글 저장
        final String url = "/api/articles";
        Article savedArticle = createDefaultArticle();


        //when
        // 목록 조회 api 호출
        final ResultActions resultActions = mockMvc.perform(get(url).accept(APPLICATION_JSON_VALUE));

        //then
        // 응답 코드가 200이고 반환값은 값 중에 0번 째 요소의 content,title값이 같은지 확ㅇ니
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value(savedArticle.getContent()))
                .andExpect(jsonPath("$[0].title").value(savedArticle.getTitle()));

    }

    @Test
    @DisplayName("findArticle: 블로그 글 조회에 성공한다.")
    public void findArticle() throws Exception{
        //given 블로그 글 저장
        final String url = "/api/articles/{id}";
        Article savedArticle = createDefaultArticle();

        //when 저장한 블로그 글 id 값으로 api 호출
        final ResultActions resultActions = mockMvc.perform(get(url,savedArticle.getId()));

        //then 응답 코드가 200 ok이고 반환받은 content,title이 저장된 값과 같은지 확인

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(savedArticle.getContent()))
                .andExpect(jsonPath("$.title").value(savedArticle.getTitle()));
    }

    @Test
    public void deleteArticle() throws Exception{
        //given 블로그 글 지정
        final String url = "/api/articles/{id}";
        Article savedArticle = createDefaultArticle();
        //when 블로그 삭제 api
        mockMvc.perform(delete(url,savedArticle.getId())).andExpect(status().isOk());
        //then
        List<Article> articles = blogRepository.findAll();
        assertThat(articles).isEmpty();
    }

    @Test
    public void updateArticle() throws Exception{
        //given
        final String url = "/api/articles/{id}";
        Article saveArticle = createDefaultArticle();

        final String newTitle = "title";
        final String newContent = "content";

        UpdateArticleReq req = new UpdateArticleReq(newTitle,newContent);
        //when
        ResultActions resultActions = mockMvc.perform(put(url,saveArticle.getId()).contentType(APPLICATION_JSON_VALUE).content(objectMapper.writeValueAsString(req)));
        //then
        resultActions.andExpect(status().isOk());
        Article article = blogRepository.findById(saveArticle.getId()).get();

        assertThat(article.getTitle()).isEqualTo(newTitle);
        assertThat(article.getContent()).isEqualTo(newContent);
    }

    private Article createDefaultArticle(){
        return blogRepository.save(Article.builder()
                .title("title")
                .author(user.getUsername())
                .content("content")
                .build());

    }
}