package ma.esith.stockims;

import ma.esith.stockims.config.MigrationInventaire;
import ma.esith.stockims.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MigrationInventaireTest {
    private DriverManagerDataSource dataSource;
    private JdbcTemplate jdbc;
    private UtilisateurRepository utilisateurs;

    @BeforeEach
    void ancienneBase() {
        dataSource = new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID()
                + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1", "sa", "");
        jdbc = new JdbcTemplate(dataSource);
        utilisateurs = mock(UtilisateurRepository.class);
        // État de l'ancienne table après l'ajout de la colonne par Hibernate update.
        jdbc.execute("create table article (id bigint primary key, reference varchar(20) not null, "
                + "designation varchar(80), proprietaire varchar(40), constraint ancienne_reference unique(reference))");
        jdbc.execute("create table entree (id bigint primary key, article_id bigint references article(id), quantite int)");
        jdbc.update("insert into article(id,reference,designation) values (1,'REF-001','Article historique')");
        jdbc.update("insert into entree(id,article_id,quantite) values (1,1,20)");
    }

    private MigrationInventaire migration(String proprietaire) {
        return new MigrationInventaire(jdbc, dataSource, utilisateurs, proprietaire, "admin", "user");
    }

    @Test
    void aucuneAttributionSansChoixExpliciteDuCompte() {
        assertThrows(IllegalStateException.class, () -> migration("").afterPropertiesSet());
        assertNull(jdbc.queryForObject("select proprietaire from article where id=1", String.class));
        assertEquals(20, jdbc.queryForObject("select quantite from entree where id=1", Integer.class));
    }

    @Test
    void fauteDansLeNomNeTransferePasLesDonnees() {
        assertThrows(IllegalStateException.class, () -> migration("compte_inexistant").afterPropertiesSet());
        assertNull(jdbc.queryForObject("select proprietaire from article where id=1", String.class));
    }

    @Test
    void repriseConserveLesMouvementsEtPermetLesReferencesParCompte() throws Exception {
        when(utilisateurs.existsByUsernameIgnoreCase("Ancien")).thenReturn(true);
        migration("Ancien").afterPropertiesSet();
        assertEquals("ancien", jdbc.queryForObject("select proprietaire from article where id=1", String.class));
        assertEquals(20, jdbc.queryForObject("select quantite from entree where id=1", Integer.class));
        jdbc.update("insert into article(id,reference,proprietaire) values (2,'REF-001','nouveau')");
        assertThrows(DataIntegrityViolationException.class, () ->
                jdbc.update("insert into article(id,reference,proprietaire) values (3,'REF-001','nouveau')"));
        assertThrows(DataIntegrityViolationException.class, () ->
                jdbc.update("insert into article(id,reference) values (4,'SANS-COMPTE')"));
        // Même après modification du réglage, aucun ancien propriétaire n'est changé.
        migration("autre").afterPropertiesSet();
        assertEquals("ancien", jdbc.queryForObject("select proprietaire from article where id=1", String.class));
        assertEquals("nouveau", jdbc.queryForObject("select proprietaire from article where id=2", String.class));
    }

    @Test
    void uneNouvelleBaseNeNecessitePasDeCompteDeReprise() throws Exception {
        jdbc.update("delete from entree");
        jdbc.update("delete from article");
        migration("").afterPropertiesSet();
        assertEquals(0, jdbc.queryForObject("select count(*) from article", Integer.class));
    }
}
