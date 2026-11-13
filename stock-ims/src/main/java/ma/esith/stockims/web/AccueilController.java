package ma.esith.stockims.web;

import lombok.RequiredArgsConstructor;
import ma.esith.stockims.MessageAccueil;
import ma.esith.stockims.model.Article;
import ma.esith.stockims.service.ArticleService;
import ma.esith.stockims.service.EtatStock;
import ma.esith.stockims.service.HistoriqueService;
import ma.esith.stockims.service.StockService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AccueilController {
    private final ArticleService articleService;
    private final StockService stockService;
    private final HistoriqueService historiqueService;

    @GetMapping("/")
    public String accueil(Model model) {
        var message = new MessageAccueil("Bienvenue dans StockIMS");
        List<Article> articles = articleService.lister();
        Map<Long, Long> stocks = stockService.calculerStocks(articles);

        Map<Long, EtatStock> etats = new LinkedHashMap<>();
        long stockTotal = 0;
        long faibles = 0;
        long epuises = 0;
        long disponibles = 0;
        for (Article article : articles) {
            long stock = stocks.getOrDefault(article.getId(), 0L);
            stockTotal += stock;
            EtatStock etat = EtatStock.calculer(stock, article.getSeuilAlerteValeur());
            etats.put(article.getId(), etat);
            switch (etat) {
                case EPUISE -> epuises++;
                case FAIBLE -> faibles++;
                case DISPONIBLE -> disponibles++;
            }
        }

        long total = Math.max(1, articles.size());
        model.addAttribute("message", message.getTexte());
        model.addAttribute("nombreArticles", articles.size());
        model.addAttribute("stockTotal", stockTotal);
        model.addAttribute("articlesFaibles", faibles);
        model.addAttribute("articlesEpuises", epuises);
        model.addAttribute("articlesDisponibles", disponibles);
        model.addAttribute("pctDisponibles", Math.round(disponibles * 100.0 / total));
        model.addAttribute("pctFaibles", Math.round(faibles * 100.0 / total));
        model.addAttribute("pctEpuises", Math.round(epuises * 100.0 / total));
        model.addAttribute("stocks", stocks);
        model.addAttribute("etats", etats);
        model.addAttribute("alertes", articles.stream()
                .filter(a -> etats.get(a.getId()) != EtatStock.DISPONIBLE)
                .limit(6)
                .toList());
        model.addAttribute("mouvementsRecents", historiqueService.tous().stream().limit(6).toList());
        return "accueil";
    }
}
