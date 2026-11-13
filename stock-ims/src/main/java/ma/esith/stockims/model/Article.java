package ma.esith.stockims.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "article", uniqueConstraints = @UniqueConstraint(
        name = "uk_article_proprietaire_reference", columnNames = {"proprietaire", "reference"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nullable uniquement pendant la reprise des anciennes bases. La migration
    // rend cette colonne obligatoire avant que l'application soit disponible.
    @Column(length = 40, updatable = false)
    private String proprietaire;

    @Column(nullable = false, length = 20)
    private String reference;

    @Column(nullable = false, length = 80)
    private String designation;

    // Nullable pour rester compatible avec les anciennes lignes déjà présentes en base.
    @Column(length = 60)
    private String categorie;

    // Nullable pour la même raison ; l'application utilise 5 si la valeur est absente.
    @Column(name = "seuil_alerte")
    private Integer seuilAlerte;

    public Article(String proprietaire, String reference, String designation, String categorie, Integer seuilAlerte) {
        if (proprietaire == null || proprietaire.isBlank() || proprietaire.length() > 40) {
            throw new IllegalArgumentException("Propriétaire invalide.");
        }
        this.proprietaire = proprietaire;
        appliquer(reference, designation, categorie, seuilAlerte);
    }

    public void modifier(String reference, String designation, String categorie, Integer seuilAlerte) {
        appliquer(reference, designation, categorie, seuilAlerte);
    }

    private void appliquer(String reference, String designation, String categorie, Integer seuilAlerte) {
        if (reference == null || reference.isBlank() || reference.strip().length() > 20) {
            throw new IllegalArgumentException("Référence invalide.");
        }
        if (designation == null || designation.isBlank() || designation.strip().length() > 80) {
            throw new IllegalArgumentException("Désignation invalide.");
        }
        String categorieNettoyee = categorie == null || categorie.isBlank() ? "Non classé" : categorie.strip();
        if (categorieNettoyee.length() > 60) {
            throw new IllegalArgumentException("Catégorie invalide.");
        }
        int seuil = seuilAlerte == null ? 5 : seuilAlerte;
        if (seuil < 0 || seuil > 1_000_000) {
            throw new IllegalArgumentException("Seuil d'alerte invalide.");
        }

        this.reference = reference.strip();
        this.designation = designation.strip();
        this.categorie = categorieNettoyee;
        this.seuilAlerte = seuil;
    }

    public String getCategorieAffichee() {
        return categorie == null || categorie.isBlank() ? "Non classé" : categorie;
    }

    public int getSeuilAlerteValeur() {
        return seuilAlerte == null ? 5 : seuilAlerte;
    }
}
