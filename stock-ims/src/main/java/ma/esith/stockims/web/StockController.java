package ma.esith.stockims.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.esith.stockims.model.Article;
import ma.esith.stockims.service.HistoriqueService;
import ma.esith.stockims.service.EtatStock;
import ma.esith.stockims.service.StockService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {
    private final StockService service;
    private final HistoriqueService historiqueService;

    @GetMapping("/{id}")
    public String fiche(@PathVariable("id") Long id, Model model) {
        if (!model.containsAttribute("mouvement")) {
            model.addAttribute("mouvement", new MouvementForm());
        }
        return preparer(id, model);
    }

    private String preparer(Long id, Model model) {
        Article article = service.chercher(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("article", article);
        long stock = service.calculerStock(id);
        model.addAttribute("stock", stock);
        model.addAttribute("etatStock", EtatStock.calculer(stock, article.getSeuilAlerteValeur()));
        model.addAttribute("historique", historiqueService.pourArticle(id).stream().limit(20).toList());
        return "stock/fiche";
    }

    @PostMapping("/{id}/entrees")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String entree(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("mouvement") MouvementForm form,
                         BindingResult erreurs,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (erreurs.hasErrors()) {
            return preparer(id, model);
        }
        try {
            service.entrer(id, form.getQuantite());
        } catch (IllegalArgumentException e) {
            model.addAttribute("erreurMetier", e.getMessage());
            return preparer(id, model);
        }
        redirectAttributes.addFlashAttribute("succes", "Entrée de stock enregistrée.");
        return "redirect:/stock/" + id;
    }

    @PostMapping("/{id}/sorties")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String sortie(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("mouvement") MouvementForm form,
                         BindingResult erreurs,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (erreurs.hasErrors()) {
            return preparer(id, model);
        }
        try {
            service.sortir(id, form.getQuantite());
        } catch (IllegalArgumentException e) {
            model.addAttribute("erreurMetier", e.getMessage());
            return preparer(id, model);
        }
        redirectAttributes.addFlashAttribute("succes", "Sortie de stock enregistrée.");
        return "redirect:/stock/" + id;
    }
}
