package ma.esith.stockims.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
@Entity @Table(name = "sortie")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sortie extends MouvementStock {
public Sortie(Article article, int quantite, LocalDate date) {
super(article, quantite, date);
}
@Override
public int quantiteSignee() {
return -getQuantite();
}
}
