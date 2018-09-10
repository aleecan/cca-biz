package be.civadis.biz.web.rest;

import be.civadis.biz.domain.Applicant;
import be.civadis.biz.messaging.dto.ArticleDTO;
import be.civadis.biz.messaging.dto.ProcessInstanceDTO;
import be.civadis.biz.messaging.dto.TaskDTO;
import be.civadis.biz.messaging.feign.WorkflowClient;
import be.civadis.biz.messaging.streaming.ArticleQueryService;
import be.civadis.biz.multitenancy.TenantContext;
import com.codahale.metrics.annotation.Timed;
import com.sun.org.apache.regexp.internal.RE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/counttask")
    @Timed
    public ResponseEntity<Integer> test(){


        // Create test applicant
        Applicant applicant = new Applicant("John Doe", "john@workflow.org", "12344");
        //applicantRepository.save(applicant);

        // Start process instance
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("applicant", applicant);
        variables.put("saison", "ete");
        //variables.put("saison", "printemps");
        variables.put("service", "csd");
        variables.put("salaire", 4000.0);
        variables.put("salaireMax", 5000.0);

        ResponseEntity<ProcessInstanceDTO> resp = this.workflowClient.startProcess("hireProcessWithJpa", "JohnDoe", variables);
        ProcessInstanceDTO process = resp.getBody();

        ResponseEntity<List<TaskDTO>> tasks = workflowClient.findClaimableTasks(0, 10, null, null, Arrays.asList("dev-managers"), null, null, null);

        return ResponseEntity.ok(tasks.getBody().size());
    }


}
