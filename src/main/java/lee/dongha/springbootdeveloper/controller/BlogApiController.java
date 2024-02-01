package lee.dongha.springbootdeveloper.controller;

import lee.dongha.springbootdeveloper.domain.Article;
import lee.dongha.springbootdeveloper.dto.AddArticleReq;
import lee.dongha.springbootdeveloper.dto.ArticleRes;
import lee.dongha.springbootdeveloper.dto.UpdateArticleReq;
import lee.dongha.springbootdeveloper.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class BlogApiController {
    private final BlogService blogService;

    @PostMapping("/api/articles")
    public ResponseEntity<Article> addArticle(@RequestBody AddArticleReq req, Principal principal){
        Article saveArticle = blogService.save(req,principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(saveArticle);
    }

    @GetMapping("/api/articles")
    public ResponseEntity<List<ArticleRes>> findAllArticles(){
        List<ArticleRes> artcles = blogService.findAll()
                                                .stream()
                                                .map(ArticleRes::new)
                                                .toList();

        return ResponseEntity.ok().body(artcles);
    }

    @GetMapping("/api/articles/{id}")
    public ResponseEntity<ArticleRes> findArticle(@PathVariable(name = "id") long id){
        Article article = blogService.findById(id);
        return ResponseEntity.ok().body(new ArticleRes(article));
    }

    @DeleteMapping("/api/articles/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable(name = "id") long id){
        blogService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/api/articles/{id}")
    public ResponseEntity<Article> updateArticle(@PathVariable(name = "id") long id, @RequestBody UpdateArticleReq req){
        Article updateArticle = blogService.update(id, req);
        return ResponseEntity.ok().body(updateArticle);
    }

}
