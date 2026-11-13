package ma.esith.stockims;

import ma.esith.stockims.service.EtatStock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EtatStockTest {
    @ParameterizedTest
    @CsvSource({
            "50, 10, DISPONIBLE",
            "9, 8, DISPONIBLE",
            "5, 10, FAIBLE",
            "7, 8, FAIBLE",
            "8, 8, EPUISE",
            "0, 8, EPUISE",
            "0, 0, EPUISE",
            "3, 0, DISPONIBLE"
    })
    void classifierSuivantLaReglePersonnalisee(long stock, int seuil, EtatStock attendu) {
        assertEquals(attendu, EtatStock.calculer(stock, seuil));
    }

    @Test
    void sousZeroResteEpuise() {
        assertEquals(EtatStock.EPUISE, EtatStock.calculer(-1L, 8));
    }
}
