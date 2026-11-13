package ma.esith.stockims.repository;

import jakarta.persistence.LockModeType;
import ma.esith.stockims.model.Article;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Article a where a.id = :id and a.proprietaire = :proprietaire")
    Optional<Article> verrouiller(@Param("id") Long id, @Param("proprietaire") String proprietaire);

    List<Article> findByProprietaireOrderByReferenceAsc(String proprietaire);

    List<Article> findByProprietaireAndReferenceContainingIgnoreCaseOrProprietaireAndDesignationContainingIgnoreCaseOrderByReferenceAsc(
            String proprietaireReference, String reference, String proprietaireDesignation, String designation);

    Optional<Article> findByIdAndProprietaire(Long id, String proprietaire);

    boolean existsByIdAndProprietaire(Long id, String proprietaire);

    boolean existsByProprietaireAndReferenceIgnoreCase(String proprietaire, String reference);

    boolean existsByProprietaireAndReferenceIgnoreCaseAndIdNot(String proprietaire, String reference, Long id);
}
