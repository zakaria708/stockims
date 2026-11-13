package ma.esith.stockims.service;

import java.util.List;

public record ImportResult(int ajoutes, int ignores, List<String> erreurs) {
}
