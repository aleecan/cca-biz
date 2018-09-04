package be.civadis.biz.messaging.streaming;

import be.civadis.biz.config.ApplicationProperties;
import be.civadis.biz.messaging.dto.ArticleDTO;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.kafka.streams.properties.KafkaStreamsBinderConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;


@Service
public class ArticleQueryService extends QueryService{

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private KafkaStreamsBinderConfigurationProperties kafkaStreamsBinderConfigurationProperties ;

    private KafkaStreams streams;

    private final Logger log = LoggerFactory.getLogger(ArticleQueryService.class);

    public ArticleQueryService() {
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
            builder.globalTable(TopicTools.resolveTopicName(applicationProperties.getTopicConfig().getArticle(), tenant),
                Consumed.with(Serdes.String(), Serdes.String()),
                Materialized.as(TopicTools.resolveStoreName(applicationProperties.getTopicConfig().getArticle(), tenant)));
        });
        //TODO : voir si le store par défault est persistent

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

    /**
     * Recherche les articles à partir du state store des articles du tenant courant
     * @return
     */
    public List<ArticleDTO> findAll(){

        List<ArticleDTO> list = new ArrayList<>();
        ReadOnlyKeyValueStore<String, String> keyValueStore = streams.store(TopicTools.resolveStoreName(applicationProperties.getTopicConfig().getArticle()), QueryableStoreTypes.<String, String>keyValueStore());

        keyValueStore.all().forEachRemaining(it -> {
            try {
                list.add(convert(it.value, ArticleDTO.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return list;
    }

    ////////////// divers essai...


        // on doit écrire un StreamListener qui maintient un store contenant ce que l'on veut pouvoir retrouver, afin d'éviter de rapatrier trop de données vers le client
        //  puis filtre en mémoire pour affiner
        // ex: https://github.com/spring-cloud/spring-cloud-stream-samples/tree/master/kafka-streams-samples/kafka-streams-interactive-query-advanced/src/main/java/kafka/streams/interactive/query
        //pas encore d'appel direct de KSQL possible, on doit passer par un appel rest à des ressources du server KSQL qui se connecte au topics
        //attention, si N partitions, on ne récupère qu'une partie des events ! (Besoin de plusieurs appels, ou util de table globale)


    public void prepareStore() throws Exception {

        //TODO: compléter/corriger params, extraire de la config yml
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, kafkaStreamsBinderConfigurationProperties.getApplicationId());
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        //TODO iter chaque tenant pour créer leur store

        StreamsBuilder builder = new StreamsBuilder();

        GlobalKTable<String, String> articlesTable = builder.globalTable("article_jhipster",
            Consumed.with(Serdes.String(), Serdes.String()),
            Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("store_article_jhipster"));

        streams = new KafkaStreams(builder.build(), config);
        //streams.cleanUp();
        streams.start();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //avec un globalStore, on retrouve les infos, mais lors d'un appel rest par la suite, on ne le retrouve pas, sauf si on interroge le même objet KafkaStreams
        //test du store
        ReadOnlyKeyValueStore<String, String> localStore = streams.store("store_article_jhipster", QueryableStoreTypes.<String, String>keyValueStore());
        localStore.all().forEachRemaining(it -> {
            try {
                System.out.println(convert(it.value, ArticleDTO.class).getLibelle());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        //TODO: voir si on peut faire un register de l'objet Streams dans queryableStoreRegistry pour afin de ne pas devoir mémoriser le kafkaStreams dans la ressource

        /////////////////////
        //recup le kafkaStreams de Spring, mais on ne peut plus le modifier
        /*
        List<String> beans = Arrays.asList(applicationContext.getBeanDefinitionNames());
        List<String> streamBuilders = beans.stream().filter(item ->
            item.length() > 15 && "stream-builder".equalsIgnoreCase(item.substring(0, 14))).collect(Collectors.toList());
        if (!streamBuilders.isEmpty()){
            StreamsBuilderFactoryBean streamsBuilderFactoryBean = applicationContext.getBean("&"+streamBuilders.get(0), StreamsBuilderFactoryBean.class);
            builder = streamsBuilderFactoryBean.getObject();
            streams = streamsBuilderFactoryBean.getKafkaStreams();
        }
        */

        /////////////////////
/*
        //compléter proprios des bindings avec novueau binding
        BindingProperties bindingProperties = new BindingProperties();
        bindingProperties.setDestination("article_jhipster");
        bindingProperties.setContentType("application/json");
        bindingServiceProperties.getBindings().put("allArticleJhipster", bindingProperties); //consumerName

        SubscribableChannel channel = (SubscribableChannel)bindingTargetFactory.createInput("allArticleJhipster");

        //beanFactory.registerSingleton("allArticleJhipster", channel);
        //channel = (SubscribableChannel)beanFactory.initializeBean(channel, "allArticleJhipster");
        //bindingService.bindConsumer(channel, "allArticleJhipster");

        //créer prop à transmettre, avec infos du materializedAs pour créer le store
        Binder binder = binderFactory.getBinder(this.bindingServiceProperties.getBinder("allArticleJhipster"), channel.getClass());
        KafkaStreamsConsumerProperties cons = new KafkaStreamsConsumerProperties();
        cons.setKeySerde("org.apache.kafka.common.serialization.Serdes$StringSerde");
        cons.setValueSerde("org.apache.kafka.common.serialization.Serdes$StringSerde");
        cons.setMaterializedAs("article_table_jhipster");
        ExtendedConsumerProperties prop = new ExtendedConsumerProperties(cons);
        prop.setUseNativeDecoding(true);

        //créer le novueau binding
        Binding binding = bindingService.doBindConsumer(channel, "allArticleJhipster", binder, prop, "article_jhipster");

        //ajouter un message handler
        channel.subscribe(message -> {
            System.out.println("Received message in generic handler : " + message.getPayload().toString());
        });

        //test du binding
        // --> MessageHandler ok, mais store pas retrouvé, comme si l'ajout avait été fait dans un KafkaStream différent !
        ReadOnlyKeyValueStore<String, String> keyValueStore = queryableStoreRegistry.getQueryableStoreType(
            "article_table_jhipster",
            QueryableStoreTypes.keyValueStore());

        keyValueStore.all().forEachRemaining(it -> {
            try {
                System.out.println(convert(it.value, ArticleDTO.class).getLibelle());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        //KTable table = (KTable)bindingTargetFactory.createInput("allArticleJhipster");
        //System.out.println(table.queryableStoreName());
*/
    }

}
