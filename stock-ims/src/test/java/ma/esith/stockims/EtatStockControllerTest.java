package ma.esith.stockims;

import ma.esith.stockims.model.Article;
import ma.esith.stockims.service.*;
import ma.esith.stockims.web.AccueilController;
import ma.esith.stockims.web.ArticleController;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EtatStockControllerTest {
    @Test
    void tableauDeBordCompteUnStockEgalAuSeuilCommeEpuise() {
        ArticleService articles = mock(ArticleService.class);
        StockService stock = mock(StockService.class);
        HistoriqueService historique = mock(HistoriqueService.class);
        Article article = article(1L, "TIS-003", 8);
        when(articles.lister()).thenReturn(List.of(article));
        when(stock.calculerStocks(List.of(article))).thenReturn(Map.of(1L, 8L));
        when(historique.tous()).thenReturn(List.of());
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("accueil", new AccueilController(articles, stock, historique).accueil(model));
        assertEquals(1L, model.get("articlesEpuises"));
        assertEquals(0L, model.get("articlesFaibles"));
        assertEquals(0L, model.get("articlesDisponibles"));
        assertEquals(100L, model.get("pctEpuises"));
        assertEquals(EtatStock.EPUISE, ((Map<?, ?>) model.get("etats")).get(1L));
    }

    @Test
    void filtreEpuiseContientStockEgalAuSeuilMaisPasStockFaible() {
        ArticleService articles = mock(ArticleService.class);
        StockService stock = mock(StockService.class);
        Article ep = article(1L, "TIS-003", 8);
        Article faible = article(2L, "TIS-004", 8);
        List<Article> articlesListe = List.of(ep, faible);
        when(articles.rechercher("")).thenReturn(articlesListe);
        when(articles.categories()).thenReturn(List.of("Maintenance"));
        when(stock.calculerStocks(articlesListe)).thenReturn(Map.of(1L, 8L, 2L, 7L));
        ArticleController controller = new ArticleController(articles, stock,
                mock(CsvService.class), mock(ImportArticlesService.class));

        ExtendedModelMap modeleEpuise = new ExtendedModelMap();
        assertEquals("articles/liste", controller.liste("", "EPUISE", "", modeleEpuise));
        assertEquals(List.of(ep), modeleEpuise.get("articles"));
        assertEquals(EtatStock.EPUISE, ((Map<?, ?>) modeleEpuise.get("etats")).get(1L));

        ExtendedModelMap modeleFaible = new ExtendedModelMap();
        assertEquals("articles/liste", controller.liste("", "FAIBLE", "", modeleFaible));
        assertEquals(List.of(faible), modeleFaible.get("articles"));
        assertEquals(EtatStock.FAIBLE, ((Map<?, ?>) modeleFaible.get("etats")).get(2L));
    }

    private static Article article(Long id, String reference, int seuil) {
        Article article = new Article("mon-compte", reference, "Pièce de test", "Maintenance", seuil);
        ReflectionTestUtils.setField(article, "id", id);
        return article;
    }
}
