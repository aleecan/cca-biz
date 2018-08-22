package be.civadis.biz.web.rest;

import com.codahale.metrics.annotation.Timed;
import be.civadis.biz.domain.BonCommande;
import be.civadis.biz.repository.BonCommandeRepository;
import be.civadis.biz.web.rest.errors.BadRequestAlertException;
import be.civadis.biz.web.rest.util.HeaderUtil;
import be.civadis.biz.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing BonCommande.
 */
@RestController
@RequestMapping("/api")
public class BonCommandeResource {

    private final Logger log = LoggerFactory.getLogger(BonCommandeResource.class);

    private static final String ENTITY_NAME = "bonCommande";

    private final BonCommandeRepository bonCommandeRepository;

    public BonCommandeResource(BonCommandeRepository bonCommandeRepository) {
        this.bonCommandeRepository = bonCommandeRepository;
    }

    /**
     * POST  /bon-commandes : Create a new bonCommande.
     *
     * @param bonCommande the bonCommande to create
     * @return the ResponseEntity with status 201 (Created) and with body the new bonCommande, or with status 400 (Bad Request) if the bonCommande has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/bon-commandes")
    @Timed
    public ResponseEntity<BonCommande> createBonCommande(@RequestBody BonCommande bonCommande) throws URISyntaxException {
        log.debug("REST request to save BonCommande : {}", bonCommande);
        if (bonCommande.getId() != null) {
            throw new BadRequestAlertException("A new bonCommande cannot already have an ID", ENTITY_NAME, "idexists");
        }
        BonCommande result = bonCommandeRepository.save(bonCommande);
        return ResponseEntity.created(new URI("/api/bon-commandes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /bon-commandes : Updates an existing bonCommande.
     *
     * @param bonCommande the bonCommande to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated bonCommande,
     * or with status 400 (Bad Request) if the bonCommande is not valid,
     * or with status 500 (Internal Server Error) if the bonCommande couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/bon-commandes")
    @Timed
    public ResponseEntity<BonCommande> updateBonCommande(@RequestBody BonCommande bonCommande) throws URISyntaxException {
        log.debug("REST request to update BonCommande : {}", bonCommande);
        if (bonCommande.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        BonCommande result = bonCommandeRepository.save(bonCommande);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, bonCommande.getId().toString()))
            .body(result);
    }

    /**
     * GET  /bon-commandes : get all the bonCommandes.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of bonCommandes in body
     */
    @GetMapping("/bon-commandes")
    @Timed
    public ResponseEntity<List<BonCommande>> getAllBonCommandes(Pageable pageable) {
        log.debug("REST request to get a page of BonCommandes");
        Page<BonCommande> page = bonCommandeRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/bon-commandes");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /bon-commandes/:id : get the "id" bonCommande.
     *
     * @param id the id of the bonCommande to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the bonCommande, or with status 404 (Not Found)
     */
    @GetMapping("/bon-commandes/{id}")
    @Timed
    public ResponseEntity<BonCommande> getBonCommande(@PathVariable Long id) {
        log.debug("REST request to get BonCommande : {}", id);
        Optional<BonCommande> bonCommande = bonCommandeRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(bonCommande);
    }

    /**
     * DELETE  /bon-commandes/:id : delete the "id" bonCommande.
     *
     * @param id the id of the bonCommande to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/bon-commandes/{id}")
    @Timed
    public ResponseEntity<Void> deleteBonCommande(@PathVariable Long id) {
        log.debug("REST request to delete BonCommande : {}", id);

        bonCommandeRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
