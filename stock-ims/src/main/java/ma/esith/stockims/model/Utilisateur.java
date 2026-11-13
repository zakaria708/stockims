package ma.esith.stockims.model;

import jakarta.persistence.*;

@Entity
@Table(name = "utilisateur",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_utilisateur_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_utilisateur_email", columnNames = "email")
        })
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String username;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 20)
    private String role = "USER";

    @Column(nullable = false)
    private boolean actif = true;

    protected Utilisateur() {
    }

    public Utilisateur(String username, String email, String passwordHash, String role) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.actif = true;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public boolean isActif() {
        return actif;
    }
}
