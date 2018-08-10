package be.civadis.biz.multitenancy;

import org.hibernate.HibernateException;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.service.spi.ServiceRegistryAwareService;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Permet à Hibernate de créer le une connexion liée au tenant courant
 * L'implémentation choisie utilise 1 schéma par tenant
 */
public class SchemaMultiTenantConnectionProviderImpl  implements MultiTenantConnectionProvider, ServiceRegistryAwareService
{

    private final Logger log = LoggerFactory.getLogger(SchemaMultiTenantConnectionProviderImpl.class);
    private DataSource dataSource;

    @Value("${spring.application.name}")
    private String appName;

    @Override
    public Connection getAnyConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        log.warn("!!!!!!!!!!!!!!!!!!!!!!!!Get Connection for Tenant : " + appName + "_" + tenantIdentifier);
        final Connection connection = getAnyConnection();
        try {
            if (tenantIdentifier == null || tenantIdentifier.isEmpty()){
                connection.createStatement().execute("USE " + appName);
                //            connection.createStatement().execute("SET search_path TO " + "public" + ";");  //postgresql
            } else {
                connection.createStatement().execute("USE " + appName + "_" + tenantIdentifier);
                //            connection.createStatement().execute("SET search_path TO " + tenantIdentifier + ";");  //postgresql
            }
        }
        catch (SQLException e) {
            throw new HibernateException("Could not alter JDBC connection to specified schema [" + appName + "_" + tenantIdentifier + "]", e);
        }
        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        log.warn("!!!!!!!!!!!!!!!!!!!!!!!!Releade Connection");
        try {
            //connection.createStatement().execute("SET search_path TO " + "public" + ";");
            connection.createStatement().execute("USE " + appName);
        }
        catch (SQLException e) {
            throw new HibernateException("Could not release JDBC connection", e);
        }
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }

    @Override
    public void injectServices(ServiceRegistryImplementor serviceRegistry) {
        Map lSettings = serviceRegistry.getService(ConfigurationService.class).getSettings();
        DataSource localDs =  (DataSource) lSettings.get("hibernate.connection.datasource");
        dataSource = localDs;
    }
}
