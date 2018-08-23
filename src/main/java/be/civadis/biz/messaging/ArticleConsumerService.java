package be.civadis.biz.messaging;

import be.civadis.biz.messaging.dto.ArticleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Service;

@Service
public class ArticleConsumerService {

    private final Logger log = LoggerFactory.getLogger(ArticleConsumerService.class);

    /**
     * ecoute sur le channel des articles, ce channel peut être associé à plusieurs topics (voir config)
     * @param article
     */
    @StreamListener(ArticleChannel.INPUT_CHANNEL)
    public void consume(ArticleDTO article) {
        log.info("Received article: {}.", article.getCode());
    }
}
