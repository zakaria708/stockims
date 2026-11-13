package ma.esith.stockims.service;

import lombok.RequiredArgsConstructor;
import ma.esith.stockims.model.Article;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CsvService {
    private final ArticleService articleService;
    private final StockService stockService;

    public byte[] exporter() {
        // Les données exportées sont toujours recherchées pour le compte connecté.
        List<Article> articles = articleService.lister();
        Map<Long, Long> stocks = stockService.calculerStocks(articles);
        StringBuilder csv = new StringBuilder();
        // BOM UTF-8 pour une ouverture correcte dans Excel Windows.
        csv.append('\uFEFF');
        csv.append("reference;designation;categorie;seuilAlerte;stockActuel\r\n");
        for (Article article : articles) {
            csv.append(echapper(article.getReference())).append(';')
                    .append(echapper(article.getDesignation())).append(';')
                    .append(echapper(article.getCategorieAffichee())).append(';')
                    .append(article.getSeuilAlerteValeur()).append(';')
                    .append(stocks.getOrDefault(article.getId(), 0L))
                    .append("\r\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String echapper(String s) {
        String valeur = s == null ? "" : s;
        if (valeur.contains(";") || valeur.contains("\"") || valeur.contains("\n") || valeur.contains("\r")) {
            return "\"" + valeur.replace("\"", "\"\"") + "\"";
        }
        return valeur;
    }

}
