package be.civadis.biz.web.rest;

import be.civadis.biz.messaging.streaming.ArticleQueryService;
import be.civadis.biz.messaging.dto.ArticleDTO;
import be.civadis.biz.multitenancy.TenantContext;
import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    //TODO : Utiliser le tenant du token
    @GetMapping("/articles/{tenant}")
    @Timed
    public ResponseEntity<List<ArticleDTO>> getAllArticles(@PathVariable(value = "tenant") String tenant) {
        TenantContext.setCurrentTenant(tenant);
        return ResponseEntity.ok(this.articleQueryService.findAll());
    }


}
