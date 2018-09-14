package be.civadis.biz.messaging.streaming;

import be.civadis.biz.config.ApplicationProperties;
import be.civadis.biz.messaging.dto.ArticleDTO;
import org.apache.kafka.streams.state.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;


@Service
public class ArticleQueryService extends QueryService{

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private StoreService storeService;

    private final Logger log = LoggerFactory.getLogger(ArticleQueryService.class);

    public ArticleQueryService() {
    }

    /**
     * Recherche les articles à partir du state store des articles du tenant courant
     * @return
     */
    public List<ArticleDTO> findAll(){

        List<ArticleDTO> list = new ArrayList<>();

        Optional<ReadOnlyKeyValueStore<String, String>> keyValueStore =
            storeService.getStore(applicationProperties.getTopicConfig().getArticle());

        if (keyValueStore.isPresent()){
            keyValueStore.get().all().forEachRemaining(it -> {
                try {
                    list.add(convert(it.value, ArticleDTO.class));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        return list;
    }

}
