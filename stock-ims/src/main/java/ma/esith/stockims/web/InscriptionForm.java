package ma.esith.stockims.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class InscriptionForm {

    @NotBlank(message = "Le nom d’utilisateur est obligatoire.")
    @Size(min = 3, max = 40, message = "Le nom d’utilisateur doit contenir entre 3 et 40 caractères.")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Utilisez uniquement lettres, chiffres, point, tiret ou underscore.")
    private String username;

    @NotBlank(message = "L’adresse e-mail est obligatoire.")
    @Email(message = "L’adresse e-mail n’est pas valide.")
    @Size(max = 120, message = "L’adresse e-mail est trop longue.")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire.")
    @Size(min = 8, max = 72, message = "Le mot de passe doit contenir entre 8 et 72 caractères.")
    private String password;

    @NotBlank(message = "Confirmez le mot de passe.")
    private String confirmationPassword;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmationPassword() {
        return confirmationPassword;
    }

    public void setConfirmationPassword(String confirmationPassword) {
        this.confirmationPassword = confirmationPassword;
    }
}
