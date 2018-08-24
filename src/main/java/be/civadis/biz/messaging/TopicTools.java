package be.civadis.biz.messaging;

import be.civadis.biz.multitenancy.TenantUtils;

public class TopicTools {

    public static String resolveTopicName(String baseTopicName) {
        return new StringBuilder(baseTopicName).append("_").append(TenantUtils.getTenant()).toString();
    }
}
