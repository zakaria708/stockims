package ma.esith.stockims.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ArticleIntrouvableException extends ResponseStatusException {
    public ArticleIntrouvableException() {
        // Même réponse pour un identifiant absent et pour un article d'un autre compte.
        super(HttpStatus.NOT_FOUND, "Article introuvable.");
    }
}
