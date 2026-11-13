package ma.esith.stockims.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
@Entity @Table(name = "entree")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Entree extends MouvementStock {
public Entree(Article article, int quantite, LocalDate date) {
super(article, quantite, date);
}
@Override
public int quantiteSignee() {
return getQuantite();
}
}