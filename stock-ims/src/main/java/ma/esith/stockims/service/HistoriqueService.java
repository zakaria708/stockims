package ma.esith.stockims.service;

import lombok.RequiredArgsConstructor;
import ma.esith.stockims.model.Entree;
import ma.esith.stockims.model.Sortie;
import ma.esith.stockims.repository.EntreeRepository;
import ma.esith.stockims.repository.SortieRepository;
import ma.esith.stockims.repository.ArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoriqueService {
    private final EntreeRepository entrees;
    private final SortieRepository sorties;
    private final ArticleRepository articles;
    private final CompteCourant compteCourant;

    @Transactional(readOnly = true)
    public List<MouvementVue> tous() {
        String proprietaire = compteCourant.identifiant();
        List<MouvementVue> mouvements = new ArrayList<>();
        for (Entree e : entrees.findByArticle_Proprietaire(proprietaire)) {
            mouvements.add(new MouvementVue(
                    e.getId(), e.getArticle().getId(), e.getArticle().getReference(),
                    e.getArticle().getDesignation(), "ENTREE", e.getQuantite(), e.getDateMouvement()));
        }
        for (Sortie s : sorties.findByArticle_Proprietaire(proprietaire)) {
            mouvements.add(new MouvementVue(
                    s.getId(), s.getArticle().getId(), s.getArticle().getReference(),
                    s.getArticle().getDesignation(), "SORTIE", s.getQuantite(), s.getDateMouvement()));
        }
        return trier(mouvements);
    }

    @Transactional(readOnly = true)
    public List<MouvementVue> pourArticle(Long articleId) {
        String proprietaire = compteCourant.identifiant();
        if (!articles.existsByIdAndProprietaire(articleId, proprietaire)) {
            throw new ArticleIntrouvableException();
        }
        List<MouvementVue> mouvements = new ArrayList<>();
        for (Entree e : entrees.findByArticle_IdAndArticle_Proprietaire(articleId, proprietaire)) {
            mouvements.add(new MouvementVue(
                    e.getId(), e.getArticle().getId(), e.getArticle().getReference(),
                    e.getArticle().getDesignation(), "ENTREE", e.getQuantite(), e.getDateMouvement()));
        }
        for (Sortie s : sorties.findByArticle_IdAndArticle_Proprietaire(articleId, proprietaire)) {
            mouvements.add(new MouvementVue(
                    s.getId(), s.getArticle().getId(), s.getArticle().getReference(),
                    s.getArticle().getDesignation(), "SORTIE", s.getQuantite(), s.getDateMouvement()));
        }
        return trier(mouvements);
    }

    @Transactional(readOnly = true)
    public List<MouvementVue> filtrer(String q, String type) {
        String terme = q == null ? "" : q.strip().toLowerCase();
        String typeNormalise = type == null ? "TOUS" : type.strip().toUpperCase();
        return tous().stream()
                .filter(m -> terme.isBlank()
                        || m.reference().toLowerCase().contains(terme)
                        || m.designation().toLowerCase().contains(terme))
                .filter(m -> "TOUS".equals(typeNormalise) || m.type().equals(typeNormalise))
                .toList();
    }

    private List<MouvementVue> trier(List<MouvementVue> mouvements) {
        mouvements.sort(Comparator
                .comparing(MouvementVue::date, Comparator.reverseOrder())
                .thenComparing(MouvementVue::id, Comparator.reverseOrder())
                .thenComparing(MouvementVue::type));
        return mouvements;
    }
}
