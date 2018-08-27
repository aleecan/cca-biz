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
        // ou doit-on écrire un StreamListener qui maitient un store contenant ce que l'on veut pouvoir retrouver ?
        //  et filtre en mémoire pour affiner

    }


}
