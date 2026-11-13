package ma.esith.stockims.service;

import lombok.RequiredArgsConstructor;
import ma.esith.stockims.model.Article;
import ma.esith.stockims.model.Entree;
import ma.esith.stockims.repository.ArticleRepository;
import ma.esith.stockims.repository.EntreeRepository;
import ma.esith.stockims.repository.SortieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository repository;
    private final EntreeRepository entrees;
    private final SortieRepository sorties;
    private final CompteCourant compteCourant;

    @Transactional(readOnly = true)
    public List<Article> lister() {
        return repository.findByProprietaireOrderByReferenceAsc(compteCourant.identifiant());
    }

    @Transactional(readOnly = true)
    public List<Article> rechercher(String q) {
        if (q == null || q.isBlank()) {
            return lister();
        }
        String terme = q.strip();
        String proprietaire = compteCourant.identifiant();
        return repository.findByProprietaireAndReferenceContainingIgnoreCaseOrProprietaireAndDesignationContainingIgnoreCaseOrderByReferenceAsc(
                proprietaire, terme, proprietaire, terme);
    }

    @Transactional(readOnly = true)
    public Optional<Article> chercher(Long id) {
        return repository.findByIdAndProprietaire(id, compteCourant.identifiant());
    }

    @Transactional(readOnly = true)
    public boolean referenceExiste(String reference) {
        return reference != null && repository.existsByProprietaireAndReferenceIgnoreCase(
                compteCourant.identifiant(), reference.strip());
    }

    @Transactional
    public Article creer(String reference, String designation, String categorie, Integer seuilAlerte) {
        return creer(reference, designation, categorie, seuilAlerte, 0);
    }

    @Transactional
    public Article creer(String reference, String designation, String categorie, Integer seuilAlerte, int stockInitial) {
        if (stockInitial < 0 || stockInitial > 100_000_000) {
            throw new IllegalArgumentException("Stock initial invalide.");
        }
        String proprietaire = compteCourant.identifiant();
        if (referenceExiste(reference)) {
            throw new IllegalArgumentException("Cette référence existe déjà dans votre compte.");
        }
        Article article = repository.save(new Article(proprietaire, reference, designation, categorie, seuilAlerte));
        // L'article et son stock initial sont enregistrés dans la même transaction.
        if (stockInitial > 0) {
            entrees.save(new Entree(article, stockInitial, LocalDate.now()));
        }
        return article;
    }

    @Transactional
    public Article modifier(Long id, String reference, String designation, String categorie, Integer seuilAlerte) {
        String proprietaire = compteCourant.identifiant();
        Article article = repository.verrouiller(id, proprietaire)
                .orElseThrow(ArticleIntrouvableException::new);
        if (reference != null && repository.existsByProprietaireAndReferenceIgnoreCaseAndIdNot(
                proprietaire, reference.strip(), id)) {
            throw new IllegalArgumentException("Cette référence existe déjà.");
        }
        article.modifier(reference, designation, categorie, seuilAlerte);
        return article;
    }

    @Transactional
    public void supprimer(Long id) {
        String proprietaire = compteCourant.identifiant();
        Article article = repository.verrouiller(id, proprietaire)
                .orElseThrow(ArticleIntrouvableException::new);
        // Suppression explicite de l'historique lié pour éviter une erreur de clé étrangère.
        entrees.deleteByArticle_IdAndArticle_Proprietaire(id, proprietaire);
        sorties.deleteByArticle_IdAndArticle_Proprietaire(id, proprietaire);
        repository.delete(article);
    }

    @Transactional(readOnly = true)
    public List<String> categories() {
        return repository.findByProprietaireOrderByReferenceAsc(compteCourant.identifiant()).stream()
                .map(Article::getCategorieAffichee)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }
}
