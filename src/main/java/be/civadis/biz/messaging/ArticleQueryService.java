package be.civadis.biz.messaging;

import be.civadis.biz.config.ApplicationProperties;
import be.civadis.biz.messaging.dto.ArticleDTO;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.kafka.streams.QueryableStoreRegistry;
import org.springframework.stereotype.Service;
//import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;

@Service
public class ArticleQueryService {

    @Autowired
    private QueryableStoreRegistry queryableStoreRegistry;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private ArticleConsumerService articleConsumerService;

    public ArticleQueryService() {
    }

    public void printAll(){

        ReadOnlyKeyValueStore<String, byte[]> keyValueStore =
            queryableStoreRegistry.getQueryableStoreType(
                ArticleChannel.ARTICLE_STATE_STORE,  //TODO: Comment filtrer par tenant ???
                QueryableStoreTypes.keyValueStore());

        keyValueStore.all().forEachRemaining(it -> System.out.println(new String(it.value)));
        //TODO : compléter la config pour serial / deserial auto
        
    }


}
