package be.civadis.biz.messaging;

import be.civadis.biz.messaging.dto.ArticleDTO;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.kafka.streams.properties.KafkaStreamsBinderConfigurationProperties;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
//import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;

@Service
public class ArticleQueryService extends QueryService{


    //private QueryableStoreProvider queryableStoreProvider;
    @Autowired
    private KafkaStreamsBinderConfigurationProperties kafkaStreamsBinderConfigurationProperties ;

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
        ReadOnlyKeyValueStore<String, String> keyValueStore = getStore(ArticleChannel.ARTICLE_STATE_STORE); //ArticleChannel.ARTICLE_STATE_STORE
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
    public void prepareStore() {




        //TODO: compléter/corriger params, extraire de la config yml
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, kafkaStreamsBinderConfigurationProperties.getApplicationId());
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        //TODO iter chaque tenant pour créer leur store

        StreamsBuilder builder = new StreamsBuilder();

        KTable<String, String> articlesTable = builder.table("article_jhipster",
            Consumed.with(Serdes.String(), Serdes.String()),
            Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("t5_store_article_jhipster"));


        System.out.println("Store name " + articlesTable.queryableStoreName());

        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.cleanUp();
        streams.start();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ReadOnlyKeyValueStore<String, String> localStore = streams.store(articlesTable.queryableStoreName(), QueryableStoreTypes.<String, String>keyValueStore());
        localStore.all().forEachRemaining(it -> {
            try {
                System.out.println(convert(it.value, ArticleDTO.class).getLibelle());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

}
