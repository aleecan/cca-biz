package be.civadis.biz.messaging.streaming;

import be.civadis.biz.messaging.dto.ArticleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
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
    public void consume(@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                        ArticleDTO article) {
        log.info("Article reçu: {} from topic {}, Update article si présent en DB bonCommande", article.getCode(), topic);
        //TODO : appel service ou repo pour maj de l'article si nécessaire
    }

}
