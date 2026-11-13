package ma.esith.stockims.web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {
    @ModelAttribute
    public void ajouterUtilisateur(Model model, Authentication authentication) {
        boolean connecte = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
        boolean admin = connecte && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        model.addAttribute("isAdmin", admin);
        model.addAttribute("currentUser", connecte ? authentication.getName() : "");
    }
}
