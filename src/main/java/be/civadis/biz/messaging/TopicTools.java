package be.civadis.biz.messaging;

import be.civadis.biz.multitenancy.TenantUtils;

public class TopicTools {

    public static String resolveTopicName(String conceptName) {
        return new StringBuilder(conceptName)
            .append("_")
            .append(TenantUtils.getTenant()).toString();
    }

    public static String resolveTableName(String conceptName) {
        return new StringBuilder(conceptName)
            .append("_table_")
            .append(TenantUtils.getTenant()).toString();
    }

    public static String resolveStoreName(String conceptName) {
        return new StringBuilder(conceptName)
            .append("_store_")
            .append(TenantUtils.getTenant()).toString();
    }

    public static String resolveTopicName(String conceptName, String tenant) {
        return new StringBuilder(conceptName)
            .append("_")
            .append(tenant).toString();
    }

    public static String resolveTableName(String conceptName, String tenant) {
        return new StringBuilder(conceptName)
            .append("_table_")
            .append(tenant).toString();
    }

    public static String resolveStoreName(String conceptName, String tenant) {
        return new StringBuilder(conceptName)
            .append("_store_")
            .append(tenant).toString();
    }
}
