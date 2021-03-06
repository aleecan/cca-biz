package be.civadis.biz.messaging.feign;

import be.civadis.biz.client.AuthorizedFeignClient;
import be.civadis.biz.messaging.dto.ProcessInstanceDTO;
import be.civadis.biz.messaging.dto.TaskDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@AuthorizedFeignClient(name = "WF", path="/workflow")
public interface WorkflowClient {

    //pour la secu, on devrait empecher l'appel direct par le gateway
    //-> config du server zuul du gateway

    //TODO:transmission des pageable ne marche pas en spring cloud

    /**
     * Start un process workflow
     * @param processName nom du process
     * @param businessKey clé métier pour identifier l'instance du processus
     * @param variables variables d'initialisation
     * @return
     */
    @PostMapping(value = "/processes/{processName}/start")
    public ResponseEntity<ProcessInstanceDTO> startProcess(@PathVariable(value="processName") String processName, @RequestParam(value="businessKey", required = false) String businessKey, @RequestBody Map<String, Object> variables);

    /**
     * Recherche la liste des tasks pouvant être traitées à un user selon ses groupes,
     * @param groups groupes autorisés à traiter les tâches
     * @param processKey
     * @param processInstanceId
     * @return
     */
    @GetMapping(value = "/tasks-claimable")
    public ResponseEntity<List<TaskDTO>> findClaimableTasks(@RequestParam(value="page", required=false) Integer page,
                                                            @RequestParam(value="size", required=false) Integer size,
                                                            @RequestParam(value="sort", required=false) String sort,
                                                            @RequestParam(value="user", required=false) String user,
                                                            @RequestParam(value="groups", required=false) List<String> groups,
                                                            @RequestParam(value="processKey", required=false) String processKey,
                                                            @RequestParam(value="processInstanceId", required=false) String processInstanceId,
                                                            @RequestParam(value="processInstanceBusinessKey", required=false)String processInstanceBusinessKey);

    /**
     * Recherche la liste des tasks pouvant être traitées à le user courant
     * @param processKey
     * @param processInstanceId
     * @return
     */
    @GetMapping(value = "/my-tasks-claimable")
    public ResponseEntity<List<TaskDTO>> findMyClaimableTasks(@RequestParam(value="page", required=false) Integer page,
                                                              @RequestParam(value="size", required=false) Integer size,
                                                              @RequestParam(value="sort", required=false) String sort,
                                                              @RequestParam(value="processKey", required=false) String processKey,
                                                              @RequestParam(value="processInstanceId", required=false) String processInstanceId,
                                                              @RequestParam(value="processInstanceBusinessKey", required=false)String processInstanceBusinessKey);

    /**
     * Recherche la liste des tasks déjà assignées à un user
     * @param user
     * @param processKey
     * @param processInstanceId
     * @return
     */
    @GetMapping(value = "/tasks-assigned")
    public ResponseEntity<List<TaskDTO>> findAssignedTasks(@RequestParam(value="page", required=false) Integer page,
                                                           @RequestParam(value="size", required=false) Integer size,
                                                           @RequestParam(value="sort", required=false) String sort,
                                                           @RequestParam("user") String user,
                                                           @RequestParam(value="processKey", required=false) String processKey,
                                                           @RequestParam(value="processInstanceId", required=false) String processInstanceId,
                                                           @RequestParam(value="processInstanceBusinessKey", required=false)String processInstanceBusinessKey);

    /**
     * Recherche la liste des tasks déjà assignées user courant
     * @param processKey
     * @param processInstanceId
     * @return
     */
    @GetMapping(value = "/my-tasks-assigned")
    public ResponseEntity<List<TaskDTO>> findMyAssignedTasks(@RequestParam(value="page", required=false) Integer page,
                                                             @RequestParam(value="size", required=false) Integer size,
                                                             @RequestParam(value="sort", required=false) String sort,
                                                             @RequestParam(value="processKey", required=false) String processKey,
                                                             @RequestParam(value="processInstanceId", required=false) String processInstanceId,
                                                             @RequestParam(value="processInstanceBusinessKey", required=false)String processInstanceBusinessKey);

    /**
     * Demande l'assignation d'une task à un user
     * @param taskId
     * @param userId
     */
    @PostMapping(value = "/tasks/{taskId}/claim")
    public ResponseEntity<Boolean> claim(@PathVariable("taskId") String taskId, @RequestParam("userId") String userId);

    /**
     * Demande l'assignation d'une task au user courant
     * @param taskId
     */
    @PostMapping(value = "/my-tasks/{taskId}/claim")
    public ResponseEntity<Boolean> myClaim(@PathVariable("taskId") String taskId);

    /**
     * Annulation de l'assignation de la task
     * @param taskId
     */
    @PostMapping(value = "/tasks/{taskId}/unclaim")
    public ResponseEntity<Boolean> unclaim(@PathVariable("taskId") String taskId);

    /**
     * Annulation de l'assignation de la task, check si associée au user courant
     * @param taskId
     */
    @PostMapping(value = "/my-tasks/{taskId}/unclaim")
    public ResponseEntity<Boolean> myUnclaim(@PathVariable("taskId") String taskId);

    /**
     * Complete une task
     * @param taskId
     * @param params
     */
    @PostMapping(value = "/tasks/{taskId}/complete")
    public ResponseEntity<Boolean> completeTask(@PathVariable("taskId") String taskId, @RequestBody Map<String, Object> params);

    /**
     * Complete une task, check si associée au user courant
     * @param taskId
     * @param params
     */
    @PostMapping(value = "/my-tasks/{taskId}/complete")
    public ResponseEntity<Boolean> myCompleteTask(@PathVariable("taskId") String taskId, @RequestBody Map<String, Object> params);


    // pour test

    @GetMapping(value = "/hello")
    public ResponseEntity<Boolean> hello();

}
