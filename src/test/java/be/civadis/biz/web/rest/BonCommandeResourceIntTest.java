package be.civadis.biz.web.rest;

import be.civadis.biz.BizApp;

import be.civadis.biz.domain.BonCommande;
import be.civadis.biz.repository.BonCommandeRepository;
import be.civadis.biz.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;


import static be.civadis.biz.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the BonCommandeResource REST controller.
 *
 * @see BonCommandeResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BizApp.class)
public class BonCommandeResourceIntTest {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_LIBELLE = "AAAAAAAAAA";
    private static final String UPDATED_LIBELLE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final Long DEFAULT_MONTANT = 1L;
    private static final Long UPDATED_MONTANT = 2L;

    @Autowired
    private BonCommandeRepository bonCommandeRepository;


    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restBonCommandeMockMvc;

    private BonCommande bonCommande;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final BonCommandeResource bonCommandeResource = new BonCommandeResource(bonCommandeRepository);
        this.restBonCommandeMockMvc = MockMvcBuilders.standaloneSetup(bonCommandeResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BonCommande createEntity(EntityManager em) {
        BonCommande bonCommande = new BonCommande()
            .code(DEFAULT_CODE)
            .libelle(DEFAULT_LIBELLE)
            .description(DEFAULT_DESCRIPTION)
            .montant(DEFAULT_MONTANT);
        return bonCommande;
    }

    @Before
    public void initTest() {
        bonCommande = createEntity(em);
    }

    @Test
    @Transactional
    public void createBonCommande() throws Exception {
        int databaseSizeBeforeCreate = bonCommandeRepository.findAll().size();

        // Create the BonCommande
        restBonCommandeMockMvc.perform(post("/api/bon-commandes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(bonCommande)))
            .andExpect(status().isCreated());

        // Validate the BonCommande in the database
        List<BonCommande> bonCommandeList = bonCommandeRepository.findAll();
        assertThat(bonCommandeList).hasSize(databaseSizeBeforeCreate + 1);
        BonCommande testBonCommande = bonCommandeList.get(bonCommandeList.size() - 1);
        assertThat(testBonCommande.getCode()).isEqualTo(DEFAULT_CODE);
        assertThat(testBonCommande.getLibelle()).isEqualTo(DEFAULT_LIBELLE);
        assertThat(testBonCommande.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testBonCommande.getMontant()).isEqualTo(DEFAULT_MONTANT);
    }

    @Test
    @Transactional
    public void createBonCommandeWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = bonCommandeRepository.findAll().size();

        // Create the BonCommande with an existing ID
        bonCommande.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restBonCommandeMockMvc.perform(post("/api/bon-commandes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(bonCommande)))
            .andExpect(status().isBadRequest());

        // Validate the BonCommande in the database
        List<BonCommande> bonCommandeList = bonCommandeRepository.findAll();
        assertThat(bonCommandeList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllBonCommandes() throws Exception {
        // Initialize the database
        bonCommandeRepository.saveAndFlush(bonCommande);

        // Get all the bonCommandeList
        restBonCommandeMockMvc.perform(get("/api/bon-commandes?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(bonCommande.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].libelle").value(hasItem(DEFAULT_LIBELLE.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].montant").value(hasItem(DEFAULT_MONTANT.intValue())));
    }
    

    @Test
    @Transactional
    public void getBonCommande() throws Exception {
        // Initialize the database
        bonCommandeRepository.saveAndFlush(bonCommande);

        // Get the bonCommande
        restBonCommandeMockMvc.perform(get("/api/bon-commandes/{id}", bonCommande.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(bonCommande.getId().intValue()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE.toString()))
            .andExpect(jsonPath("$.libelle").value(DEFAULT_LIBELLE.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.montant").value(DEFAULT_MONTANT.intValue()));
    }
    @Test
    @Transactional
    public void getNonExistingBonCommande() throws Exception {
        // Get the bonCommande
        restBonCommandeMockMvc.perform(get("/api/bon-commandes/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateBonCommande() throws Exception {
        // Initialize the database
        bonCommandeRepository.saveAndFlush(bonCommande);

        int databaseSizeBeforeUpdate = bonCommandeRepository.findAll().size();

        // Update the bonCommande
        BonCommande updatedBonCommande = bonCommandeRepository.findById(bonCommande.getId()).get();
        // Disconnect from session so that the updates on updatedBonCommande are not directly saved in db
        em.detach(updatedBonCommande);
        updatedBonCommande
            .code(UPDATED_CODE)
            .libelle(UPDATED_LIBELLE)
            .description(UPDATED_DESCRIPTION)
            .montant(UPDATED_MONTANT);

        restBonCommandeMockMvc.perform(put("/api/bon-commandes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedBonCommande)))
            .andExpect(status().isOk());

        // Validate the BonCommande in the database
        List<BonCommande> bonCommandeList = bonCommandeRepository.findAll();
        assertThat(bonCommandeList).hasSize(databaseSizeBeforeUpdate);
        BonCommande testBonCommande = bonCommandeList.get(bonCommandeList.size() - 1);
        assertThat(testBonCommande.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testBonCommande.getLibelle()).isEqualTo(UPDATED_LIBELLE);
        assertThat(testBonCommande.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testBonCommande.getMontant()).isEqualTo(UPDATED_MONTANT);
    }

    @Test
    @Transactional
    public void updateNonExistingBonCommande() throws Exception {
        int databaseSizeBeforeUpdate = bonCommandeRepository.findAll().size();

        // Create the BonCommande

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restBonCommandeMockMvc.perform(put("/api/bon-commandes")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(bonCommande)))
            .andExpect(status().isBadRequest());

        // Validate the BonCommande in the database
        List<BonCommande> bonCommandeList = bonCommandeRepository.findAll();
        assertThat(bonCommandeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteBonCommande() throws Exception {
        // Initialize the database
        bonCommandeRepository.saveAndFlush(bonCommande);

        int databaseSizeBeforeDelete = bonCommandeRepository.findAll().size();

        // Get the bonCommande
        restBonCommandeMockMvc.perform(delete("/api/bon-commandes/{id}", bonCommande.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<BonCommande> bonCommandeList = bonCommandeRepository.findAll();
        assertThat(bonCommandeList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(BonCommande.class);
        BonCommande bonCommande1 = new BonCommande();
        bonCommande1.setId(1L);
        BonCommande bonCommande2 = new BonCommande();
        bonCommande2.setId(bonCommande1.getId());
        assertThat(bonCommande1).isEqualTo(bonCommande2);
        bonCommande2.setId(2L);
        assertThat(bonCommande1).isNotEqualTo(bonCommande2);
        bonCommande1.setId(null);
        assertThat(bonCommande1).isNotEqualTo(bonCommande2);
    }
}
