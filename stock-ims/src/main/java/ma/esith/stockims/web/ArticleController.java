package ma.esith.stockims.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.esith.stockims.model.Article;
import ma.esith.stockims.service.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;
    private final StockService stockService;
    private final CsvService csvService;
    private final ImportArticlesService importArticlesService;

    @GetMapping
    public String liste(@RequestParam(name = "q", defaultValue = "") String q,
                        @RequestParam(name = "statut", defaultValue = "TOUS") String statut,
                        @RequestParam(name = "categorie", defaultValue = "") String categorie,
                        Model model) {
        List<Article> candidats = articleService.rechercher(q);
        Map<Long, Long> stocksCandidats = stockService.calculerStocks(candidats);

        List<Article> articles = candidats.stream()
                .filter(a -> categorie.isBlank() || a.getCategorieAffichee().equalsIgnoreCase(categorie))
                .filter(a -> correspondStatut(a, stocksCandidats.getOrDefault(a.getId(), 0L), statut))
                .toList();

        Map<Long, Long> stocks = new LinkedHashMap<>();
        for (Article article : articles) {
            stocks.put(article.getId(), stocksCandidats.getOrDefault(article.getId(), 0L));
        }

        Map<Long, EtatStock> etats = new LinkedHashMap<>();
        for (Article article : articles) {
            etats.put(article.getId(), EtatStock.calculer(
                    stocks.getOrDefault(article.getId(), 0L), article.getSeuilAlerteValeur()));
        }

        model.addAttribute("articles", articles);
        model.addAttribute("stocks", stocks);
        model.addAttribute("etats", etats);
        model.addAttribute("categories", articleService.categories());
        model.addAttribute("q", q);
        model.addAttribute("statut", statut.toUpperCase(Locale.ROOT));
        model.addAttribute("categorieSelectionnee", categorie);
        return "articles/liste";
    }

    @GetMapping("/nouveau")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String formulaire(Model model) {
        model.addAttribute("article", new ArticleForm());
        model.addAttribute("modeEdition", false);
        return "articles/formulaire";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String ajouter(@Valid @ModelAttribute("article") ArticleForm form,
                          BindingResult erreurs,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        model.addAttribute("modeEdition", false);
        if (erreurs.hasErrors()) {
            return "articles/formulaire";
        }
        try {
            articleService.creer(form.getReference(), form.getDesignation(), form.getCategorie(),
                    form.getSeuilAlerte(), form.getStockInitial() == null ? 0 : form.getStockInitial());
        } catch (DataIntegrityViolationException e) {
            erreurs.rejectValue("reference", "doublon", "Cette référence existe déjà.");
            return "articles/formulaire";
        } catch (IllegalArgumentException e) {
            erreurs.reject("metier", e.getMessage());
            return "articles/formulaire";
        }
        redirectAttributes.addFlashAttribute("succes", "Article ajouté avec succès.");
        return "redirect:/articles";
    }

    @GetMapping("/{id}/modifier")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String modifierForm(@PathVariable Long id, Model model) {
        Article article = articleService.chercher(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("article", ArticleForm.depuis(article));
        model.addAttribute("articleId", id);
        model.addAttribute("modeEdition", true);
        return "articles/formulaire";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String modifier(@PathVariable Long id,
                           @Valid @ModelAttribute("article") ArticleForm form,
                           BindingResult erreurs,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        model.addAttribute("articleId", id);
        model.addAttribute("modeEdition", true);
        if (erreurs.hasErrors()) {
            return "articles/formulaire";
        }
        try {
            articleService.modifier(id, form.getReference(), form.getDesignation(),
                    form.getCategorie(), form.getSeuilAlerte());
        } catch (DataIntegrityViolationException e) {
            erreurs.rejectValue("reference", "doublon", "Cette référence existe déjà dans votre compte.");
            return "articles/formulaire";
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("référence")) {
                erreurs.rejectValue("reference", "doublon", e.getMessage());
            } else {
                erreurs.reject("metier", e.getMessage());
            }
            return "articles/formulaire";
        }
        redirectAttributes.addFlashAttribute("succes", "Article modifié avec succès.");
        return "redirect:/articles";
    }

    @PostMapping("/{id}/supprimer")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            articleService.supprimer(id);
            redirectAttributes.addFlashAttribute("succes", "Article et son historique supprimés.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/articles";
    }

    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String importer(@RequestParam("fichier") MultipartFile fichier,
                           RedirectAttributes redirectAttributes) {
        try {
            ImportResult result = importArticlesService.importer(fichier);
            redirectAttributes.addFlashAttribute("succes",
                    result.ajoutes() + " article(s) importé(s), " + result.ignores() + " ligne(s) ignorée(s).");
            if (!result.erreurs().isEmpty()) {
                redirectAttributes.addFlashAttribute("importErreurs", result.erreurs().stream().limit(8).toList());
            }
        } catch (IOException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erreur", "Import impossible : " + e.getMessage());
        }
        return "redirect:/articles";
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exporter() {
        byte[] contenu = csvService.exporter();
        String nom = "stockims-articles-" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nom + "\"")
                .contentType(new MediaType("text", "csv"))
                .body(contenu);
    }

    private boolean correspondStatut(Article article, long stock, String statut) {
        String s = statut == null ? "TOUS" : statut.toUpperCase(Locale.ROOT);
        EtatStock etat = EtatStock.calculer(stock, article.getSeuilAlerteValeur());
        return switch (s) {
            case "EPUISE" -> etat == EtatStock.EPUISE;
            case "FAIBLE" -> etat == EtatStock.FAIBLE;
            case "DISPONIBLE" -> etat == EtatStock.DISPONIBLE;
            default -> true;
        };
    }
}
