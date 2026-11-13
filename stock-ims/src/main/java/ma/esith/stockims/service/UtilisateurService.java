package ma.esith.stockims.service;

import ma.esith.stockims.model.Utilisateur;
import ma.esith.stockims.repository.UtilisateurRepository;
import ma.esith.stockims.web.InscriptionForm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UtilisateurService {

    private final UtilisateurRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String viewerUsername;

    public UtilisateurService(UtilisateurRepository repository,
                              PasswordEncoder passwordEncoder,
                              @Value("${app.security.admin.username:admin}") String adminUsername,
                              @Value("${app.security.viewer.username:user}") String viewerUsername) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.viewerUsername = viewerUsername;
    }

    @Transactional
    public Utilisateur inscrire(InscriptionForm form) {
        String username = nettoyer(form.getUsername());
        String email = nettoyer(form.getEmail()).toLowerCase();

        if (!form.getPassword().equals(form.getConfirmationPassword())) {
            throw new IllegalArgumentException("Les deux mots de passe ne correspondent pas.");
        }
        if (username.equalsIgnoreCase(adminUsername) || username.equalsIgnoreCase(viewerUsername)) {
            throw new IllegalArgumentException("Ce nom d’utilisateur est réservé. Choisissez-en un autre.");
        }
        if (repository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Ce nom d’utilisateur existe déjà.");
        }
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Cette adresse e-mail est déjà utilisée.");
        }

        String hash = passwordEncoder.encode(form.getPassword());
        return repository.save(new Utilisateur(username, email, hash, "USER"));
    }

    private String nettoyer(String valeur) {
        return StringUtils.hasText(valeur) ? valeur.trim() : "";
    }
}
