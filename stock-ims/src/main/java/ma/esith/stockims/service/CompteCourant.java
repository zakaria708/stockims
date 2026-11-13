package ma.esith.stockims.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class CompteCourant {
    public String identifiant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("Connexion requise.");
        }
        String nom = authentication.getName();
        if (nom == null || nom.isBlank() || nom.length() > 40) {
            throw new AccessDeniedException("Identifiant de compte invalide.");
        }
        // Les noms de connexion sont insensibles à la casse dans SecurityConfig.
        return nom.toLowerCase(Locale.ROOT);
    }
}
