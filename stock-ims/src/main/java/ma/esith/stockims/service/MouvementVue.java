package ma.esith.stockims.service;

import java.time.LocalDate;

public record MouvementVue(
        Long id,
        Long articleId,
        String reference,
        String designation,
        String type,
        int quantite,
        LocalDate date
) {
}
