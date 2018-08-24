package be.civadis.biz.web.rest;

import be.civadis.biz.messaging.ArticleQueryService;
import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity getAllArtilces() {
        this.articleQueryService.printAll();
        return ResponseEntity.ok(true);
    }


}
