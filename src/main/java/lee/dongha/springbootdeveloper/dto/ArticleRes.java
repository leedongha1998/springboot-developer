package lee.dongha.springbootdeveloper.dto;

import lee.dongha.springbootdeveloper.domain.Article;
import lombok.Getter;

@Getter
public class ArticleRes {
    private final String title;
    private final String content;

    public ArticleRes(Article article) {
        this.title = article.getTitle();
        this.content = article.getContent();
    }
}
