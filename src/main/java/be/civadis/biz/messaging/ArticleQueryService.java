package be.civadis.biz.messaging;

import be.civadis.biz.messaging.dto.ArticleDTO;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.stereotype.Service;

import java.io.IOException;
//import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;

@Service
public class ArticleQueryService extends QueryService{

    public ArticleQueryService() {
    }

    public void printAll() throws IOException {

        ReadOnlyKeyValueStore<String, String> keyValueStore = getStore(ArticleChannel.ARTICLE_STATE_STORE);

        keyValueStore.all().forEachRemaining(it -> {
            System.out.println(it.key);
            System.out.println(it.value);
            try {
                ArticleDTO art = convert(it.value, ArticleDTO.class);
                System.out.println(art.getLibelle());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        //TODO : automatiser conversion json (bug version courante ???)
        //TODO : voir comment filtrer par tenant
        //TODO : filtre du store ?
        // on doit écrire un StreamListener qui maintient un store contenant ce que l'on veut pouvoir retrouver, afin d'éviter de rapatrier trop de données vers le client
        //  puis filtre en mémoire pour affiner
        // ex: https://github.com/spring-cloud/spring-cloud-stream-samples/tree/master/kafka-streams-samples/kafka-streams-interactive-query-advanced/src/main/java/kafka/streams/interactive/query
        //pas encore d'appel direct de KSQL possible, on doit passer par un appel rest à des ressources du server KSQL qui se connecte au topics
        //TODO : attention, si N partitions, on ne récupère qu'une partie des evrnts ! (Besoin de plusieurs appels, ou util de table globale)

    }


}
