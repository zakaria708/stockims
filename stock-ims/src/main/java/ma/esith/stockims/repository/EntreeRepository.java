package ma.esith.stockims.repository;

import ma.esith.stockims.model.Entree;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EntreeRepository extends JpaRepository<Entree, Long> {
    List<Entree> findByArticle_Proprietaire(String proprietaire);
    List<Entree> findByArticle_IdAndArticle_Proprietaire(Long articleId, String proprietaire);
    void deleteByArticle_IdAndArticle_Proprietaire(Long articleId, String proprietaire);
}
