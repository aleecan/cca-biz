package be.civadis.biz.messaging.streaming;

import be.civadis.biz.config.ApplicationProperties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.kafka.streams.properties.KafkaStreamsBinderConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.Properties;

@Service
public class StoreService {

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private KafkaStreamsBinderConfigurationProperties kafkaStreamsBinderConfigurationProperties ;

    private KafkaStreams streams = null;

    private final Logger log = LoggerFactory.getLogger(StoreService.class);

    public StoreService() {
    }

    /**
     * Défini une topologie de streams et exécute celle-ci dans une instance de KafkaStreams
     * Pour chaque tenant récupérer de la config,
     *  -> crée une ktable alimentées par le topic des articles du tenant
     *  -> expose cette ktable dans un state store accessible à la demande par query rest
     */
    @PostConstruct
    public void init() {

        //recup params du fichier de config
        String appId = kafkaStreamsBinderConfigurationProperties.getApplicationId();
        String[] brokers = kafkaStreamsBinderConfigurationProperties.getBrokers();

        if (applicationProperties.getTopicConfig().isEnabled()){
            StringBuilder brokersStr = new StringBuilder();
            for (int i=0; i < brokers.length; i++){
                if (i > 0) brokersStr.append(",");
                brokersStr.append(brokers[i]);
                brokersStr.append(":9092"); //TODO
            }

            Properties config = new Properties();
            config.put(StreamsConfig.APPLICATION_ID_CONFIG, appId);
            config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, brokersStr.toString());
            config.putAll(kafkaStreamsBinderConfigurationProperties.getConfiguration());
            //config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
            //config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

            StreamsBuilder builder = new StreamsBuilder();

            //iter chaque tenant pour créer leur store dans la topology
            applicationProperties.getSchemas().stream().forEach(tenant -> {
                KeyValueBytesStoreSupplier storeSupplier = Stores
                    .persistentKeyValueStore(TopicTools.resolveStoreName(applicationProperties.getTopicConfig().getArticle(), tenant));
                builder.globalTable(TopicTools.resolveTopicName(applicationProperties.getTopicConfig().getArticle(), tenant),
                    Consumed.with(Serdes.String(), Serdes.String()),
                    Materialized.as(storeSupplier));
            });

            //créer et start kafkaStreams selon la topology définie
            streams = new KafkaStreams(builder.build(), config);

            //log error
            streams.setUncaughtExceptionHandler((Thread thread, Throwable throwable) -> {
                log.error(throwable.getMessage(), throwable);
            });

            //streams.cleanUp();
            streams.start();

            // Add shutdown hook to stop the Kafka Streams threads.
            Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        }


    }

    public Optional<ReadOnlyKeyValueStore<String, String>> getStore(String topicBaseName){
        if (streams != null){
            return Optional.of(streams.store(TopicTools.resolveStoreName(topicBaseName), QueryableStoreTypes.<String, String>keyValueStore()));
        } else {
            return Optional.empty();
        }
    }

}
