package be.civadis.biz.web.rest;

import be.civadis.biz.messaging.ArticleQueryService;
import be.civadis.biz.messaging.dto.ArticleDTO;
import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * REST controller for managing Department.
 */
@RestController
@RequestMapping("/test")
public class TestResource {

    private final Logger log = LoggerFactory.getLogger(TestResource.class);

    private final ArticleQueryService articleQueryService;

    public TestResource(ArticleQueryService articleQueryService){
        this.articleQueryService = articleQueryService;
    }

    @GetMapping("/articles")
    @Timed
    public ResponseEntity<List<ArticleDTO>> getAllArticles() {
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(this.articleQueryService.findAll());
    }

    @GetMapping("/init")
    @Timed
    public ResponseEntity<Boolean> init() {
        this.articleQueryService.prepareStore();
        return ResponseEntity.ok(true);
    }


}
