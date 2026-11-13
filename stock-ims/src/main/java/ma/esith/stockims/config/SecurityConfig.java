package ma.esith.stockims.config;

import ma.esith.stockims.model.Utilisateur;
import ma.esith.stockims.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.security.admin.username:admin}")
    private String adminUsername;

    @Value("${app.security.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.security.viewer.username:user}")
    private String viewerUsername;

    @Value("${app.security.viewer.password:user123}")
    private String viewerPassword;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(UtilisateurRepository utilisateurRepository,
                                          PasswordEncoder encoder) {
        return username -> {
            if (adminUsername.equalsIgnoreCase(username)) {
                return User.withUsername(adminUsername)
                        .password(encoder.encode(adminPassword))
                        .roles("ADMIN", "USER")
                        .build();
            }

            if (viewerUsername.equalsIgnoreCase(username)) {
                return User.withUsername(viewerUsername)
                        .password(encoder.encode(viewerPassword))
                        .roles("USER")
                        .build();
            }

            Utilisateur utilisateur = utilisateurRepository.findByUsernameIgnoreCase(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));

            UserDetails user = User.withUsername(utilisateur.getUsername())
                    .password(utilisateur.getPasswordHash())
                    .roles(utilisateur.getRole())
                    .disabled(!utilisateur.isActif())
                    .build();
            return user;
        };
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/exemples/**", "/login", "/register", "/error").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());
        return http.build();
    }
}
