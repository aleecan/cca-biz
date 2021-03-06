package be.civadis.biz.multitenancy;

import be.civadis.biz.security.SecurityUtils;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;

/**
 * Indique à Hibernate le tenant courant
 */
public class MyCurrentTenantIdentifierResolver implements CurrentTenantIdentifierResolver
{

    private final Logger log = LoggerFactory.getLogger(MyCurrentTenantIdentifierResolver.class);

    @Override
    public String resolveCurrentTenantIdentifier() {
        String user = SecurityUtils.getCurrentUserLogin().orElse("notLogged");
        log.debug("*********************** user : " + user);
        String tenant = Optional.ofNullable(TenantUtils.getTenant()).orElse("");
        log.debug("*********************** resolve tenant for connection : " + tenant);
        return tenant;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}

