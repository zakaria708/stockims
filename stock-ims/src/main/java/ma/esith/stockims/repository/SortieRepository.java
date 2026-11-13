package ma.esith.stockims.repository;

import ma.esith.stockims.model.Sortie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SortieRepository extends JpaRepository<Sortie, Long> {
    List<Sortie> findByArticle_Proprietaire(String proprietaire);
    List<Sortie> findByArticle_IdAndArticle_Proprietaire(Long articleId, String proprietaire);
    void deleteByArticle_IdAndArticle_Proprietaire(Long articleId, String proprietaire);
}
