package be.civadis.biz.messaging.dto;

public class ArticleDTO {

    private Long id;
    private String code;
    private String libelle;

    public ArticleDTO() {
    }
/*
    public ArticleDTO(Long id, String code, String libelle) {
        this.id = id;
        this.code = code;
        this.libelle = libelle;
    }
*/
    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
}
