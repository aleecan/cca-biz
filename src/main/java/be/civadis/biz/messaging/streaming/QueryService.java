package be.civadis.biz.messaging.streaming;

import be.civadis.biz.config.ApplicationProperties;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.kafka.streams.QueryableStoreRegistry;

import java.io.IOException;

public class QueryService {

    //@Autowired
    //protected QueryableStoreRegistry queryableStoreRegistry;

    @Autowired
    private ApplicationProperties applicationProperties;

    private ObjectMapper objectMapper;

    public QueryService() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * recherche un store dans le kafkaStreams créé par spring cloud stream
     * Attention, différent de celui créé par StoreService,
     *   les stores de l'un ne sont pas visibles dans l'autre
     * @param json
     * @param clazz
     * @param <T>
     * @return
     * @throws IOException
     */
    /*
    protected ReadOnlyKeyValueStore<String, String> getStore(String storeName){
        return queryableStoreRegistry.getQueryableStoreType(
                storeName,  //TODO: Comment filtrer par tenant ???
                QueryableStoreTypes.keyValueStore());
    }
    */

    protected <T> T convert(String json, Class<T> clazz) throws IOException {
        return objectMapper.readValue(json, clazz);
    }

}
