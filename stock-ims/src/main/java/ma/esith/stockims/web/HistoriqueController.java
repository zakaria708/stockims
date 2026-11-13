package ma.esith.stockims.web;

import lombok.RequiredArgsConstructor;
import ma.esith.stockims.service.HistoriqueService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class HistoriqueController {
    private final HistoriqueService historiqueService;

    @GetMapping("/mouvements")
    public String mouvements(@RequestParam(name = "q", defaultValue = "") String q,
                             @RequestParam(name = "type", defaultValue = "TOUS") String type,
                             Model model) {
        model.addAttribute("mouvements", historiqueService.filtrer(q, type));
        model.addAttribute("q", q);
        model.addAttribute("type", type.toUpperCase());
        return "mouvements/liste";
    }
}
