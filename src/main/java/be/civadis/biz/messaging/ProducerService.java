package be.civadis.biz.messaging;

import be.civadis.biz.config.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;

import java.nio.charset.StandardCharsets;

public class ProducerService {

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private BinderAwareChannelResolver resolver;

    protected <T> void send(String baseTopicName, T object, String key){
        resolver.resolveDestination(TopicTools.resolveTopicName(baseTopicName)).send(MessageBuilder
            .withPayload(object)
            .setHeader(KafkaHeaders.MESSAGE_KEY, key.getBytes(StandardCharsets.UTF_8))
            .build());
    }

    protected ApplicationProperties.TopicConfig getTopicConfig() {
        return applicationProperties.getTopicConfig();
    }

    protected ApplicationProperties getApplicationProperties() {
        return applicationProperties;
    }

    protected BinderAwareChannelResolver getResolver() {
        return resolver;
    }
}
