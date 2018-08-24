package be.civadis.biz.messaging;

import org.apache.kafka.streams.kstream.KTable;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface ArticleChannel {

    //nom du channel à utiliser dans la config pour le binding avec topic kafka
    String INPUT_CHANNEL = "articleInputChannel";
    String TABLE_CHANNEL = "allArticle";
    String ARTICLE_STATE_STORE = "article_table";

    @Input(value = ArticleChannel.INPUT_CHANNEL)
    SubscribableChannel subscribableChannel();

    @Input(value = ArticleChannel.TABLE_CHANNEL)
    KTable allArticle();

}
