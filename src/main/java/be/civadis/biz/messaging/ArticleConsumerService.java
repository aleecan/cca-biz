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
     * va permettre de mettre à jour les articles présent dans la db bonCommande
     *
     * @param article
     */
    @StreamListener(ArticleChannel.INPUT_CHANNEL)
    public void consume(ArticleDTO article) {
        log.info("Article reçu: {}, Update article si présent en DB bonCommande", article.getCode());
        //TODO : appel service ou repo pour maj de l'article si nécessaire
    }

}
