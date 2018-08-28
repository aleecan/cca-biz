package be.civadis.biz.messaging;

import be.civadis.biz.messaging.dto.ArticleDTO;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.kafka.core.StreamsBuilderFactoryBean;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
//import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;

@Service
public class ArticleQueryService extends QueryService{

    @Autowired
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;


    public ArticleQueryService() {
    }

    public void printAll() throws IOException {

        ReadOnlyKeyValueStore<String, String> keyValueStore = getStore(ArticleChannel.ARTICLE_STATE_STORE);

        keyValueStore.all().forEachRemaining(it -> {
            System.out.println(it.key);
            System.out.println(it.value);
            try {
                ArticleDTO art = convert(it.value, ArticleDTO.class);
                System.out.println(art.getLibelle());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        //TODO : automatiser conversion json (bug version courante ???)
        //TODO : voir comment filtrer par tenant
        //TODO : filtre du store ?
        // on doit écrire un StreamListener qui maintient un store contenant ce que l'on veut pouvoir retrouver, afin d'éviter de rapatrier trop de données vers le client
        //  puis filtre en mémoire pour affiner
        // ex: https://github.com/spring-cloud/spring-cloud-stream-samples/tree/master/kafka-streams-samples/kafka-streams-interactive-query-advanced/src/main/java/kafka/streams/interactive/query
        //pas encore d'appel direct de KSQL possible, on doit passer par un appel rest à des ressources du server KSQL qui se connecte au topics
        //TODO : attention, si N partitions, on ne récupère qu'une partie des evrnts ! (Besoin de plusieurs appels, ou util de table globale)

    }

    public List<ArticleDTO> findAll(){
        List<ArticleDTO> list = new ArrayList<>();
        ReadOnlyKeyValueStore<String, String> keyValueStore = getStore(ArticleChannel.ARTICLE_STATE_STORE);
        keyValueStore.all().forEachRemaining(it -> {
            try {
                list.add(convert(it.value, ArticleDTO.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return list;
    }

    //TODO : voir comment lancer foo au start du service
    /**
     * Create a {@link SubscribableChannel} and register in the
     * {@link org.springframework.context.ApplicationContext}
     */
    private void foo() {
        //TODO: compléter/corriger params
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-application");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-broker1:9092");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        //TODO iter chaque tenant pour créer leur store

        StreamsBuilder builder = new StreamsBuilder();
        KTable<String, ArticleDTO> articlesTable = builder.table("article_jhipster", Materialized.as("article_table_jhipster"));


        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.start();

    }

}
