package be.civadis.biz.web.rest;

import be.civadis.biz.messaging.dto.ArticleDTO;
import be.civadis.biz.messaging.feign.WorkflowClient;
import be.civadis.biz.messaging.streaming.ArticleQueryService;
import be.civadis.biz.multitenancy.TenantContext;
import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing Department.
 */
@RestController
@RequestMapping("/api")
public class TestWfResource {

    private final Logger log = LoggerFactory.getLogger(TestWfResource.class);

    private final WorkflowClient workflowClient;

    public TestWfResource(WorkflowClient workflowClient){
        this.workflowClient = workflowClient;
    }

    @GetMapping("/hellowf")
    @Timed
    public ResponseEntity<Boolean> hello() {
        return workflowClient.hello();
    }


}
