package be.civadis.biz.repository;

import be.civadis.biz.domain.BonCommande;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the BonCommande entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BonCommandeRepository extends JpaRepository<BonCommande, Long> {

}
