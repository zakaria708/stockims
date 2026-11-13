package ma.esith.stockims.service;

import lombok.RequiredArgsConstructor;
import ma.esith.stockims.model.*;
import ma.esith.stockims.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StockService {
    private final ArticleRepository articles;
    private final EntreeRepository entrees;
    private final SortieRepository sorties;
    private final CompteCourant compteCourant;

    @Transactional(readOnly = true)
    public long calculerStock(Long articleId) {
        String proprietaire = compteCourant.identifiant();
        if (!articles.existsByIdAndProprietaire(articleId, proprietaire)) {
            throw new ArticleIntrouvableException();
        }
        List<MouvementStock> mouvements = new ArrayList<>();
        mouvements.addAll(entrees.findByArticle_IdAndArticle_Proprietaire(articleId, proprietaire));
        mouvements.addAll(sorties.findByArticle_IdAndArticle_Proprietaire(articleId, proprietaire));
        return mouvements.stream().mapToLong(MouvementStock::quantiteSignee).sum();
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> calculerStocks(Collection<Article> listeArticles) {
        String proprietaire = compteCourant.identifiant();
        Map<Long, Long> resultat = new LinkedHashMap<>();
        for (Article article : listeArticles) {
            if (!proprietaire.equals(article.getProprietaire())) {
                throw new ArticleIntrouvableException();
            }
            resultat.put(article.getId(), calculerStock(article.getId()));
        }
        return resultat;
    }

    public Optional<Article> chercher(Long id) {
        return articles.findByIdAndProprietaire(id, compteCourant.identifiant());
    }

    private Article verrouiller(Long id) {
        return articles.verrouiller(id, compteCourant.identifiant())
                .orElseThrow(ArticleIntrouvableException::new);
    }

    @Transactional
    public void entrer(Long id, int quantite) {
        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantité doit être supérieure à zéro.");
        }
        Article article = verrouiller(id);
        entrees.save(new Entree(article, quantite, LocalDate.now()));
    }

    @Transactional
    public void sortir(Long id, int quantite) {
        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantité doit être supérieure à zéro.");
        }
        Article article = verrouiller(id);
        long stock = calculerStock(id);
        if (quantite > stock) {
            throw new IllegalArgumentException(
                    "Stock insuffisant : " + stock + " disponible(s).");
        }
        sorties.save(new Sortie(article, quantite, LocalDate.now()));
    }
}
