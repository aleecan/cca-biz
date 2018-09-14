package be.civadis.biz.multitenancy;

import be.civadis.biz.config.ApplicationProperties;
import io.github.jhipster.config.JHipsterConstants;
import io.github.jhipster.config.liquibase.AsyncSpringLiquibase;
import liquibase.integration.spring.MultiTenantSpringLiquibase;
import liquibase.integration.spring.SpringLiquibase;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableTransactionManagement
public class MultitenantConfigDatabase {

    private final Logger log = LoggerFactory.getLogger(MultiTenantConfig.class);

    private final Environment env;
    private JpaProperties jpaProperties;
    private ApplicationProperties applicationProperties;

    @Value("${spring.application.name}")
    private String appName;

    public MultitenantConfigDatabase(Environment env, JpaProperties jpaProperties, ApplicationProperties applicationProperties) {
        this.env = env;
        this.jpaProperties = jpaProperties;
        this.applicationProperties = applicationProperties;
    }

    //liquibase

    @Bean
    public MultiTenantSpringLiquibase liquibaseMt(DataSource dataSource) throws SQLException
    {
        MultiTenantSpringLiquibase multiTenantSpringLiquibase = new MultiTenantSpringLiquibase();
        multiTenantSpringLiquibase.setDataSource(dataSource);

        List<String> schemas = this.applicationProperties.getSchemas().stream()
            .map(sch -> appName + "_" + sch)
            .collect(Collectors.toList());
        schemas.add(appName); //pour init le schéma par defaut en cas d'utilisation d'un db mysql

        for (String schemaName : schemas){
            dataSource.getConnection().createStatement().executeUpdate("CREATE SCHEMA IF NOT EXISTS "+schemaName);
        }

        multiTenantSpringLiquibase.setSchemas(schemas);
        multiTenantSpringLiquibase.setChangeLog("classpath:config/liquibase/master.xml");
        multiTenantSpringLiquibase.setContexts("development, production");
        if (env.acceptsProfiles(JHipsterConstants.SPRING_PROFILE_NO_LIQUIBASE)) {
            multiTenantSpringLiquibase.setShouldRun(false);
        } else {
            multiTenantSpringLiquibase.setShouldRun(true);
            log.debug("Configuring Liquibase");
        }

        return multiTenantSpringLiquibase;
    }

    /**
     * Définir un bean SpringLiquibase qui remplace celui généré par jhipster
     * permet de ne pas exécuter les modifs sur le schéma public
     * les modifs seront exécutées sur les schémas des tenants par MultiTenantSpringLiquibase
     * @param taskExecutor
     * @param dataSource
     * @param liquibaseProperties
     * @return
     */
    @Bean
    @Primary
    public SpringLiquibase liquibase(@Qualifier("taskExecutor") TaskExecutor taskExecutor,
                                     DataSource dataSource, LiquibaseProperties liquibaseProperties) {

        // Use liquibase.integration.spring.SpringLiquibase if you don't want Liquibase to start asynchronously
        SpringLiquibase liquibase = new AsyncSpringLiquibase(taskExecutor, env);
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:config/liquibase/master.xml");
        liquibase.setContexts(liquibaseProperties.getContexts());
        liquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
        liquibase.setDropFirst(liquibaseProperties.isDropFirst());

        //active liquibase sur le schéma public seulement pour les tests
        if (liquibaseProperties.getContexts().contentEquals("test")){
            if (env.acceptsProfiles(JHipsterConstants.SPRING_PROFILE_NO_LIQUIBASE)) {
                liquibase.setShouldRun(false);
            } else {
                liquibase.setShouldRun(liquibaseProperties.isEnabled());
                log.debug("Configuring Liquibase");
            }
        } else {
            liquibase.setShouldRun(false);
        }

        return liquibase;
    }

    //hibernate

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Bean
    public MultiTenantConnectionProvider multiTenantConnectionProvider(){
        return new SchemaMultiTenantConnectionProviderImpl();
    }

    @Bean
    public CurrentTenantIdentifierResolver currentTenantIdentifierResolver(){
        return new MyCurrentTenantIdentifierResolver();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource,
                                                                       MultiTenantConnectionProvider multiTenantConnectionProvider,
                                                                       CurrentTenantIdentifierResolver currentTenantIdentifierResolver) {
        Map<String, Object> properties = new HashMap<>();
        //properties.putAll(jpaProperties.getHibernateProperties(dataSource));
        properties.putAll(jpaProperties.getHibernateProperties(new HibernateSettings().ddlAuto(() -> {return "none";})));
        properties.put(org.hibernate.cfg.Environment.MULTI_TENANT, MultiTenancyStrategy.SCHEMA);
        properties.put(org.hibernate.cfg.Environment.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
        properties.put(org.hibernate.cfg.Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolver);

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("be.civadis");
        em.setJpaVendorAdapter(jpaVendorAdapter());
        em.setJpaPropertyMap(properties);

        return em;
    }

}
