package be.civadis.biz.messaging;

import be.civadis.biz.domain.BonCommande;
import org.springframework.stereotype.Service;

@Service
public class BonCommandeProducerService extends ProducerService{

    public void sendBonCommandeCreated(BonCommande bc) {
        super.send(super.getTopicConfig().getBonCommandeCreated(),
            bc, bc.getId().toString());
    }

    public void sendBonCommandeValidated(BonCommande bc) {
        super.send(super.getTopicConfig().getBonCommandeValidated(),
            bc, bc.getId().toString());
    }

}
