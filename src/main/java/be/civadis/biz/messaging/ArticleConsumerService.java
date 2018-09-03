package be.civadis.biz.messaging;

import be.civadis.biz.messaging.dto.ArticleDTO;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Service;

@Service
public class ArticleConsumerService {

    private final Logger log = LoggerFactory.getLogger(ArticleConsumerService.class);

    /**
     * ecoute sur le channel des articles, ce channel peut être associé à plusieurs topics (voir config)
     *
     * @param article
     */
    @StreamListener(ArticleChannel.INPUT_CHANNEL)
    public void consume(ArticleDTO article) {
        log.info("Article reçu: {}.", article.getCode());
        log.info("Update article si présent en DB bonCommande");
        //TODO : appel service ou repo pour maj de l'article si nécessaire
    }


    //doit être présent pour construire la table, elle sera ensuite matérialisée dans un statestore que l'on pourra interroger
    //@StreamListener
    //public void allArticle(@Input(ArticleChannel.TABLE_CHANNEL) KTable allArticle) {
    //}

}
