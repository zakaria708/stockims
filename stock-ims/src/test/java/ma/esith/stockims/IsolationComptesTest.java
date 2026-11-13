package ma.esith.stockims;

import ma.esith.stockims.model.Article;
import ma.esith.stockims.repository.*;
import ma.esith.stockims.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:isolation;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop", "app.inventory.legacy-owner="
})
class IsolationComptesTest {
    @Autowired ArticleService articles;
    @Autowired StockService stock;
    @Autowired HistoriqueService historique;
    @Autowired CsvService csv;
    @Autowired ImportArticlesService imports;
    @Autowired ArticleRepository articleRepository;
    @Autowired EntreeRepository entrees;
    @Autowired SortieRepository sorties;
    @Autowired UtilisateurRepository utilisateurs;
    @Autowired WebApplicationContext contexte;
    private MockMvc mvc;

    @BeforeEach
    void preparer() {
        SecurityContextHolder.clearContext();
        sorties.deleteAll();
        entrees.deleteAll();
        articleRepository.deleteAll();
        utilisateurs.deleteAll();
        mvc = MockMvcBuilders.webAppContextSetup(contexte).apply(springSecurity()).build();
    }

    @AfterEach
    void deconnecter() {
        SecurityContextHolder.clearContext();
    }

    private void connecter(String nom) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                nom, "inutilise", List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    private Article ancienArticle() {
        connecter("ancien");
        return articles.creer("REF-001", "Article confidentiel", "Categorie privee", 5, 20);
    }

    @Test
    void nouveauCompteVideSurToutesLesPages() throws Exception {
        ancienArticle();
        mvc.perform(get("/").with(user("nouveau")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("nombreArticles", 0))
                .andExpect(model().attribute("stockTotal", 0L))
                .andExpect(model().attribute("alertes", empty()))
                .andExpect(model().attribute("mouvementsRecents", empty()));
        mvc.perform(get("/articles").with(user("nouveau")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("articles", empty()))
                .andExpect(model().attribute("categories", empty()));
        mvc.perform(get("/articles").param("q", "confidentiel").with(user("nouveau")))
                .andExpect(status().isOk()).andExpect(model().attribute("articles", empty()));
        mvc.perform(get("/mouvements").with(user("nouveau")))
                .andExpect(status().isOk()).andExpect(model().attribute("mouvements", empty()));
        mvc.perform(get("/articles/export").with(user("nouveau")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("confidentiel"))));
    }

    @Test
    void identifiantsDevinesNePermettentNiLectureNiEcriture() throws Exception {
        Long id = ancienArticle().getId();
        mvc.perform(get("/stock/" + id).with(user("nouveau"))).andExpect(status().isNotFound());
        mvc.perform(get("/articles/" + id + "/modifier").with(user("nouveau")))
                .andExpect(status().isNotFound());
        mvc.perform(post("/articles/" + id).with(user("nouveau")).with(csrf())
                        .param("reference", "PIRATE").param("designation", "Modification interdite")
                        .param("seuilAlerte", "5"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/articles/" + id + "/supprimer").with(user("nouveau")).with(csrf()))
                .andExpect(status().isNotFound());
        mvc.perform(post("/stock/" + id + "/entrees").with(user("nouveau")).with(csrf()).param("quantite", "3"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/stock/" + id + "/sorties").with(user("nouveau")).with(csrf()).param("quantite", "3"))
                .andExpect(status().isNotFound());
        connecter("nouveau");
        assertThrows(ArticleIntrouvableException.class, () -> stock.calculerStock(id));
        assertThrows(ArticleIntrouvableException.class, () -> historique.pourArticle(id));
        connecter("ancien");
        assertEquals("REF-001", articles.chercher(id).orElseThrow().getReference());
        assertEquals(20, stock.calculerStock(id));
    }

    @Test
    void memeReferenceAutoriseeDansDeuxComptesMaisPasDeuxFoisDansLeMeme() {
        Article ancien = ancienArticle();
        connecter("nouveau");
        Article nouveau = articles.creer("REF-001", "Mon article", "Mes outils", 2, 7);
        assertNotEquals(ancien.getId(), nouveau.getId());
        assertEquals(List.of(nouveau.getId()), articles.lister().stream().map(Article::getId).toList());
        assertThrows(IllegalArgumentException.class, () -> articles.creer("REF-001", "Doublon", "X", 2));
        stock.entrer(nouveau.getId(), 3);
        stock.sortir(nouveau.getId(), 2);
        assertEquals(8, stock.calculerStock(nouveau.getId()));
        articles.supprimer(nouveau.getId());
        assertTrue(articles.lister().isEmpty());
        connecter("ancien");
        assertEquals(20, stock.calculerStock(ancien.getId()));
        assertEquals(1, historique.tous().size());
    }

    @Test
    void importExportEtCategoriesRestentPersonnels() throws Exception {
        ancienArticle();
        connecter("nouveau");
        String contenu = "reference;designation;categorie;seuilAlerte;stockInitial\n"
                + "REF-001;Mon import;Mes outils;2;9\n";
        var resultat = imports.importer(new MockMultipartFile("fichier", "articles.csv", "text/csv",
                contenu.getBytes(StandardCharsets.UTF_8)));
        assertEquals(1, resultat.ajoutes());
        assertEquals(0, resultat.ignores());
        assertEquals(List.of("Mes outils"), articles.categories());
        String export = new String(csv.exporter(), StandardCharsets.UTF_8);
        assertTrue(export.contains("Mon import"));
        assertFalse(export.contains("confidentiel"));
        assertEquals(9, stock.calculerStock(articles.lister().get(0).getId()));
        connecter("ancien");
        assertEquals(1, articles.lister().size());
        assertEquals(List.of("Categorie privee"), articles.categories());
    }

    @Test
    void utilisateurPeutGererSesPropresMouvementsEtLeProprietaireNeVientPasDuFormulaire() throws Exception {
        mvc.perform(post("/articles").with(user("nouveau")).with(csrf())
                        .param("reference", "PERSO").param("designation", "Mon article")
                        .param("seuilAlerte", "2").param("stockInitial", "5")
                        .param("proprietaire", "ancien"))
                .andExpect(status().is3xxRedirection());
        connecter("nouveau");
        Article article = articles.lister().get(0);
        assertEquals("nouveau", article.getProprietaire());
        mvc.perform(post("/stock/" + article.getId() + "/entrees").with(user("nouveau")).with(csrf())
                        .param("quantite", "4"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(post("/stock/" + article.getId() + "/sorties").with(user("nouveau")).with(csrf())
                        .param("quantite", "2"))
                .andExpect(status().is3xxRedirection());
        connecter("nouveau");
        assertEquals(7, stock.calculerStock(article.getId()));
        assertThrows(IllegalArgumentException.class, () -> stock.sortir(article.getId(), 100));
        connecter("ancien");
        assertTrue(articles.lister().isEmpty());
    }

    @Test
    void casseDuNomEtRoleAdminNeContournentPasLaSeparation() throws Exception {
        Article article = ancienArticle();
        connecter("ANCIEN");
        assertEquals(article.getId(), articles.lister().get(0).getId());
        mvc.perform(get("/stock/" + article.getId()).with(user("admin").roles("ADMIN", "USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void creationDeCompteNeCopieAucunArticle() throws Exception {
        ancienArticle();
        mvc.perform(post("/register").with(csrf()).param("username", "nouveau")
                        .param("email", "nouveau@example.com").param("password", "MotDePasse123")
                        .param("confirmationPassword", "MotDePasse123"))
                .andExpect(status().is3xxRedirection());
        assertTrue(utilisateurs.existsByUsernameIgnoreCase("nouveau"));
        connecter("nouveau");
        assertTrue(articles.lister().isEmpty());
        assertTrue(historique.tous().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"csv", "txt", "xlsx", "xls"})
    void importHttpDeChaqueFormatResteDansLeCompteConnecte(String extension) throws Exception {
        Article ancien = ancienArticle();
        byte[] contenu;
        if (extension.equals("xlsx") || extension.equals("xls")) {
            try (Workbook classeur = extension.equals("xlsx") ? new XSSFWorkbook() : new HSSFWorkbook()) {
                Sheet feuille = classeur.createSheet("Articles");
                Row entete = feuille.createRow(0);
                String[] noms = {"reference", "designation", "stockInitial", "proprietaire"};
                for (int i = 0; i < noms.length; i++) entete.createCell(i).setCellValue(noms[i]);
                Row ligne = feuille.createRow(1);
                ligne.createCell(0).setCellValue("REF-001");
                ligne.createCell(1).setCellValue("Import personnel");
                ligne.createCell(2).setCellValue(9);
                ligne.createCell(3).setCellValue("ancien");
                ByteArrayOutputStream sortie = new ByteArrayOutputStream();
                classeur.write(sortie);
                contenu = sortie.toByteArray();
            }
        } else {
            contenu = "reference;designation;stockInitial;proprietaire\nREF-001;Import personnel;9;ancien\n"
                    .getBytes(StandardCharsets.UTF_8);
        }
        mvc.perform(multipart("/articles/import").file(new MockMultipartFile("fichier", "articles." + extension,
                        "application/octet-stream", contenu)).with(user("nouveau")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("succes", "1 article(s) importé(s), 0 ligne(s) ignorée(s)."));
        connecter("nouveau");
        Article personnel = articles.lister().get(0);
        assertEquals("nouveau", personnel.getProprietaire());
        assertEquals(9, stock.calculerStock(personnel.getId()));
        assertEquals(1, historique.tous().size());
        connecter("ancien");
        assertEquals(1, articles.lister().size());
        assertEquals("Article confidentiel", articles.chercher(ancien.getId()).orElseThrow().getDesignation());
        assertEquals(20, stock.calculerStock(ancien.getId()));
    }

    @Test
    void erreurImportLisibleEtCsrfToujoursObligatoire() throws Exception {
        MockMultipartFile fichier = new MockMultipartFile("fichier", "invalide.txt", "text/plain",
                "code;nom\nA;Article".getBytes(StandardCharsets.UTF_8));
        mvc.perform(multipart("/articles/import").file(fichier).with(user("nouveau")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("erreur", containsString("reference et designation")));
        mvc.perform(multipart("/articles/import").file(fichier).with(user("nouveau")))
                .andExpect(status().isForbidden());
        connecter("nouveau");
        assertTrue(articles.lister().isEmpty());
    }
}
