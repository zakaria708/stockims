package ma.esith.stockims.web;
import jakarta.validation.constraints.*;
import lombok.*;
@Getter @Setter
public class MouvementForm {
@NotNull(message = "La quantité est obligatoire.")
@Min(value = 1, message = "La quantité doit être positive.")
private Integer quantite;
}