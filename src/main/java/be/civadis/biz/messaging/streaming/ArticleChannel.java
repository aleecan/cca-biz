package be.civadis.biz.messaging.streaming;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface ArticleChannel {

    //nom du channel à utiliser dans la config pour le binding avec topic kafka
    String INPUT_CHANNEL = "articleInputChannel";

    @Input(value = ArticleChannel.INPUT_CHANNEL)
    SubscribableChannel subscribableChannel();

}
