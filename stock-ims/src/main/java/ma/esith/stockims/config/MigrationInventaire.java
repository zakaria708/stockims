package ma.esith.stockims.config;

import ma.esith.stockims.repository.UtilisateurRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/** Reprise idempotente de la base MySQL existante, sans suppression d'articles. */
@Component
@DependsOn("entityManagerFactory")
public class MigrationInventaire implements InitializingBean {
    private final JdbcTemplate jdbc;
    private final DataSource dataSource;
    private final UtilisateurRepository utilisateurs;
    private final String ancienProprietaire;
    private final String adminUsername;
    private final String viewerUsername;

    public MigrationInventaire(JdbcTemplate jdbc, DataSource dataSource,
                               UtilisateurRepository utilisateurs,
                               @Value("${app.inventory.legacy-owner:}") String ancienProprietaire,
                               @Value("${app.security.admin.username:admin}") String adminUsername,
                               @Value("${app.security.viewer.username:user}") String viewerUsername) {
        this.jdbc = jdbc;
        this.dataSource = dataSource;
        this.utilisateurs = utilisateurs;
        this.ancienProprietaire = ancienProprietaire;
        this.adminUsername = adminUsername;
        this.viewerUsername = viewerUsername;
    }

    @Override
    public void afterPropertiesSet() throws SQLException {
        Long sansProprietaire = jdbc.queryForObject(
                "select count(*) from article where proprietaire is null or trim(proprietaire) = ''", Long.class);
        if (sansProprietaire != null && sansProprietaire > 0) {
            String nom = ancienProprietaire == null ? "" : ancienProprietaire.strip();
            if (nom.isBlank() || nom.length() > 40) {
                throw new IllegalStateException("Des articles de l'ancienne version sont présents. "
                        + "Définissez LEGACY_OWNER_USERNAME avec le nom exact du compte qui doit les conserver. "
                        + "Aucune donnée n'a été attribuée automatiquement. Voir LISEZ_MOI_COMPTES_SEPARES.md.");
            }
            if (!nom.equalsIgnoreCase(adminUsername) && !nom.equalsIgnoreCase(viewerUsername)
                    && !utilisateurs.existsByUsernameIgnoreCase(nom)) {
                throw new IllegalStateException("Le compte indiqué dans LEGACY_OWNER_USERNAME n'existe pas. "
                        + "Indiquez un compte existant de l'ancienne application.");
            }
            // Seules les lignes sans propriétaire sont reprises. Un redémarrage
            // avec un autre réglage ne déplace jamais les inventaires déjà attribués.
            jdbc.update("update article set proprietaire = ? where proprietaire is null or trim(proprietaire) = ''",
                    nom.toLowerCase(Locale.ROOT));
        }

        try (Connection connexion = dataSource.getConnection()) {
            DatabaseMetaData meta = connexion.getMetaData();
            String produit = meta.getDatabaseProductName();
            boolean h2 = "H2".equalsIgnoreCase(produit);
            if (!h2 && !"MySQL".equalsIgnoreCase(produit) && !"MariaDB".equalsIgnoreCase(produit)) {
                throw new IllegalStateException("La migration d'inventaire nécessite MySQL ou MariaDB.");
            }

            String guillemet = meta.getIdentifierQuoteString().strip();
            Map<String, List<String>> index = indexUniques(connexion);
            boolean uniciteParCompte = index.values().stream()
                    .anyMatch(colonnes -> colonnes.size() == 2
                            && new HashSet<>(colonnes).equals(Set.of("proprietaire", "reference")));
            if (!uniciteParCompte) {
                jdbc.execute("alter table article add constraint uk_article_proprietaire_reference "
                        + "unique (proprietaire, reference)");
            }

            // Hibernate update ne retire pas l'ancienne unicité globale de référence.
            // Elle empêcherait deux comptes de choisir la même référence.
            if (h2) {
                List<String> contraintes = jdbc.queryForList("""
                        select k.constraint_name from information_schema.key_column_usage k
                        join information_schema.table_constraints c
                          on c.constraint_name = k.constraint_name
                         and c.table_schema = k.table_schema and c.table_name = k.table_name
                        where lower(k.table_name) = 'article' and k.table_schema = ?
                          and c.constraint_type = 'UNIQUE'
                        group by k.constraint_name
                        having count(*) = 1 and lower(min(k.column_name)) = 'reference'
                        """, String.class, connexion.getSchema());
                for (String contrainte : contraintes) {
                    jdbc.execute("alter table article drop constraint " + citer(contrainte, guillemet));
                }
            }
            for (Map.Entry<String, List<String>> entree : indexUniques(connexion).entrySet()) {
                if (entree.getValue().equals(List.of("reference"))) {
                    String nomIndex = citer(entree.getKey(), guillemet);
                    jdbc.execute(h2 ? "drop index " + nomIndex : "alter table article drop index " + nomIndex);
                }
            }

            try (ResultSet colonnes = meta.getColumns(connexion.getCatalog(), connexion.getSchema(),
                    "article", "proprietaire")) {
                if (colonnes.next() && colonnes.getInt("NULLABLE") != DatabaseMetaData.columnNoNulls) {
                    jdbc.execute(h2 ? "alter table article alter column proprietaire set not null"
                            : "alter table article modify column proprietaire varchar(40) not null");
                }
            }
        }
    }

    private Map<String, List<String>> indexUniques(Connection connexion) throws SQLException {
        Map<String, List<String>> index = new LinkedHashMap<>();
        try (ResultSet lignes = connexion.getMetaData().getIndexInfo(
                connexion.getCatalog(), connexion.getSchema(), "article", true, false)) {
            while (lignes.next()) {
                String nom = lignes.getString("INDEX_NAME");
                String colonne = lignes.getString("COLUMN_NAME");
                if (nom != null && colonne != null) {
                    index.computeIfAbsent(nom, ignore -> new ArrayList<>())
                            .add(colonne.toLowerCase(Locale.ROOT));
                }
            }
        }
        return index;
    }

    private String citer(String identifiant, String guillemet) {
        return guillemet + identifiant.replace(guillemet, guillemet + guillemet) + guillemet;
    }
}
