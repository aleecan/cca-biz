package be.civadis.biz.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Properties specific to Biz.
 * <p>
 * Properties are configured in the application.yml file.
 * See {@link io.github.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private List<String> schemas;
    private TopicConfig topicConfig = new TopicConfig();

    public class TopicConfig{

        private boolean enabled = true;
        private String article;
        private String bonCommandeCreated;
        private String bonCommandeValidated;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getArticle() {
            return article;
        }

        public void setArticle(String article) {
            this.article = article;
        }

        public String getBonCommandeCreated() {
            return bonCommandeCreated;
        }

        public void setBonCommandeCreated(String bonCommandeCreated) {
            this.bonCommandeCreated = bonCommandeCreated;
        }

        public String getBonCommandeValidated() {
            return bonCommandeValidated;
        }

        public void setBonCommandeValidated(String bonCommandeValidated) {
            this.bonCommandeValidated = bonCommandeValidated;
        }
    }

    //getters & setters

    public List<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }

    public TopicConfig getTopicConfig() {
        return topicConfig;
    }

    public void setTopicConfig(TopicConfig topicConfig) {
        this.topicConfig = topicConfig;
    }
}
