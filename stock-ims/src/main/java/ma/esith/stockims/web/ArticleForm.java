package ma.esith.stockims.web;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import ma.esith.stockims.model.Article;

@Getter
@Setter
public class ArticleForm {
    @NotBlank(message = "La référence est obligatoire.")
    @Size(max = 20, message = "20 caractères au maximum.")
    private String reference;

    @NotBlank(message = "La désignation est obligatoire.")
    @Size(max = 80, message = "80 caractères au maximum.")
    private String designation;

    @Size(max = 60, message = "60 caractères au maximum.")
    private String categorie = "Non classé";

    @NotNull(message = "Le seuil d'alerte est obligatoire.")
    @Min(value = 0, message = "Le seuil doit être positif ou nul.")
    @Max(value = 1000000, message = "Seuil trop élevé.")
    private Integer seuilAlerte = 5;

    @Min(value = 0, message = "Le stock initial doit être positif ou nul.")
    @Max(value = 100000000, message = "Stock initial trop élevé.")
    private Integer stockInitial = 0;

    public static ArticleForm depuis(Article article) {
        ArticleForm form = new ArticleForm();
        form.setReference(article.getReference());
        form.setDesignation(article.getDesignation());
        form.setCategorie(article.getCategorieAffichee());
        form.setSeuilAlerte(article.getSeuilAlerteValeur());
        form.setStockInitial(0);
        return form;
    }
}
