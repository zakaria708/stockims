package ma.esith.stockims.web;

import jakarta.validation.Valid;
import ma.esith.stockims.service.UtilisateurService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UtilisateurService utilisateurService;

    public AuthController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String inscription(Model model) {
        if (!model.containsAttribute("inscriptionForm")) {
            model.addAttribute("inscriptionForm", new InscriptionForm());
        }
        return "register";
    }

    @PostMapping("/register")
    public String inscrire(@Valid @ModelAttribute("inscriptionForm") InscriptionForm form,
                           BindingResult erreurs,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (!form.getPassword().equals(form.getConfirmationPassword())) {
            erreurs.rejectValue("confirmationPassword", "mismatch", "Les deux mots de passe ne correspondent pas.");
        }
        if (erreurs.hasErrors()) {
            return "register";
        }

        try {
            utilisateurService.inscrire(form);
        } catch (IllegalArgumentException e) {
            model.addAttribute("erreurInscription", e.getMessage());
            return "register";
        }

        redirectAttributes.addFlashAttribute("compteCree", true);
        return "redirect:/login?registered";
    }
}
