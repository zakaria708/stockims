package ma.esith.stockims;

import ma.esith.stockims.service.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImportArticlesServiceTest {
    private ArticleService articles;
    private ImportArticlesService imports;

    @BeforeEach
    void preparer() {
        articles = mock(ArticleService.class);
        imports = new ImportArticlesService(articles);
    }

    private ImportResult texte(String nom, String contenu) throws Exception {
        return imports.importer(new MockMultipartFile("fichier", nom, "text/plain",
                contenu.getBytes(StandardCharsets.UTF_8)));
    }

    @ParameterizedTest
    @ValueSource(strings = {";", ",", "\t"})
    void csvAvecLesTroisSeparateurs(String separateur) throws Exception {
        String contenu = "\uFEFF" + String.join(separateur, "Référence", "Désignation", "Catégorie", "Seuil d'alerte", "Stock initial")
                + "\n" + String.join(separateur, "TIS-001", "Étoffe bleue", "Textile", "5", "12") + "\n";
        ImportResult resultat = texte("articles.CSV", contenu);
        assertEquals(1, resultat.ajoutes());
        assertEquals(0, resultat.ignores());
        verify(articles).creer("TIS-001", "Étoffe bleue", "Textile", 5, 12);
    }

    @ParameterizedTest
    @ValueSource(strings = {"UTF-8", "UTF-16", "windows-1252"})
    void txtTabuleAvecAccentsEtEncodagesWindows(String encodage) throws Exception {
        String contenu = "reference\tdesignation\tcategorie\tseuilAlerte\tstockInitial\r\n"
                + "TXT-001\tPièce détachée\tMétal\t2\t8\r\n";
        ImportResult resultat = imports.importer(new MockMultipartFile("fichier", "articles.TXT", "text/plain",
                contenu.getBytes(Charset.forName(encodage))));
        assertEquals(1, resultat.ajoutes());
        verify(articles).creer("TXT-001", "Pièce détachée", "Métal", 2, 8);
    }

    @Test
    void guillemetsRetoursLigneEtDirectiveExcelSontLus() throws Exception {
        ImportResult resultat = texte("articles.csv", "sep=;\r\nreference;designation\r\n"
                + "REF-1;\"Tissu; coton\n\"\"blanc\"\"\"\r\n");
        assertEquals(1, resultat.ajoutes());
        verify(articles).creer("REF-1", "Tissu; coton\n\"blanc\"", "", 5, 0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"xlsx", "xls"})
    void excelLitLaPremiereFeuilleVisibleEtPreserveLesReferences(String extension) throws Exception {
        byte[] fichier;
        try (Workbook classeur = extension.equals("xlsx") ? new XSSFWorkbook() : new HSSFWorkbook()) {
            classeur.createSheet("Donnees cachees").createRow(0).createCell(0).setCellValue("Ne pas importer");
            Sheet feuille = classeur.createSheet("Articles");
            classeur.setActiveSheet(1);
            classeur.setSheetHidden(0, true);
            Row entete = feuille.createRow(2);
            String[] noms = {"stockInitial", "designation", "reference", "seuilAlerte", "categorie"};
            for (int i = 0; i < noms.length; i++) entete.createCell(i).setCellValue(noms[i]);
            Row ligne = feuille.createRow(3);
            ligne.createCell(0).setCellValue(10000);
            CellStyle nombre = classeur.createCellStyle();
            nombre.setDataFormat(classeur.createDataFormat().getFormat("#,##0"));
            ligne.getCell(0).setCellStyle(nombre);
            ligne.createCell(1).setCellValue("Article Excel");
            ligne.createCell(2).setCellValue(7);
            CellStyle reference = classeur.createCellStyle();
            reference.setDataFormat(classeur.createDataFormat().getFormat("00000"));
            ligne.getCell(2).setCellStyle(reference);
            ligne.createCell(3).setCellFormula("2+3");
            classeur.getCreationHelper().createFormulaEvaluator().evaluateFormulaCell(ligne.getCell(3));
            ligne.createCell(4).setCellValue("Équipement");
            ByteArrayOutputStream sortie = new ByteArrayOutputStream();
            classeur.write(sortie);
            fichier = sortie.toByteArray();
        }
        ImportResult resultat = imports.importer(new MockMultipartFile("fichier", "articles." + extension,
                "application/octet-stream", fichier));
        assertEquals(1, resultat.ajoutes());
        verify(articles).creer("00007", "Article Excel", "Équipement", 5, 10000);
    }

    @Test
    void fractionsNeSontPasArrondiesDansLesQuantites() throws Exception {
        ImportResult resultat = texte("articles.txt", "reference;designation;stockInitial\n"
                + "INVALIDE;Fraction;1.5\nVALIDE;Entier;12.0\n");
        assertEquals(1, resultat.ajoutes());
        assertEquals(1, resultat.ignores());
        assertTrue(resultat.erreurs().get(0).contains("nombre entier"));
        verify(articles).creer("VALIDE", "Entier", "", 5, 12);
        verifyNoMoreInteractions(articles);
    }

    @Test
    void doublonIgnoreSansBloquerLesAutresArticles() throws Exception {
        doThrow(new IllegalArgumentException("Cette référence existe déjà dans votre compte."))
                .when(articles).creer("DOUBLON", "Ancien", "", 5, 0);
        ImportResult resultat = texte("articles.csv", "reference;designation\nDOUBLON;Ancien\nNEUF;Nouveau\n");
        assertEquals(1, resultat.ajoutes());
        assertEquals(1, resultat.ignores());
        assertTrue(resultat.erreurs().get(0).startsWith("Ligne 2"));
        verify(articles).creer("NEUF", "Nouveau", "", 5, 0);
    }

    @Test
    void fichierStructureIncorrecteNeCreeRien() {
        assertThrows(IllegalArgumentException.class, () -> texte("a.txt", "code;nom\nA;Article"));
        assertThrows(IllegalArgumentException.class, () -> texte("a.csv", "reference;designation;référence\nA;B;C"));
        assertThrows(IllegalArgumentException.class, () -> texte("a.csv", "reference;designation\nA;Valide\nB;\"non fermé"));
        assertThrows(IllegalArgumentException.class, () -> texte("a.txt", "Un texte sans tableau"));
        assertThrows(IllegalArgumentException.class, () -> texte("a.pdf", "reference;designation\nA;Article"));
        assertThrows(IllegalArgumentException.class, () -> texte("a.xlsx", "reference;designation\nA;Article"));
        assertThrows(IllegalArgumentException.class, () -> texte("a.xls", "reference;designation\nA;Article"));
        assertThrows(IllegalArgumentException.class, () -> texte("a.txt", ""));
        verifyNoInteractions(articles);
    }

    @Test
    void fichierTropGrandOuTropDeLignesRefusesAvantInsertion() {
        assertThrows(IllegalArgumentException.class, () -> imports.importer(new MockMultipartFile(
                "fichier", "grand.csv", "text/csv", new byte[5 * 1024 * 1024 + 1])));
        StringBuilder texte = new StringBuilder("reference;designation\n");
        for (int i = 0; i <= 10000; i++) texte.append("REF-").append(i).append(";Article\n");
        assertThrows(IllegalArgumentException.class, () -> texte("articles.csv", texte.toString()));
        verifyNoInteractions(articles);
    }

    @Test
    void classeurCompresseDemesureRefuseAvantLecturePoi() throws Exception {
        ByteArrayOutputStream octets = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(octets)) {
            zip.putNextEntry(new ZipEntry("xl/worksheets/sheet1.xml"));
            byte[] bloc = new byte[1024 * 1024];
            for (int i = 0; i < 26; i++) zip.write(bloc);
            zip.closeEntry();
        }
        IllegalArgumentException erreur = assertThrows(IllegalArgumentException.class, () -> imports.importer(
                new MockMultipartFile("fichier", "grand.xlsx", "application/octet-stream", octets.toByteArray())));
        assertTrue(erreur.getMessage().contains("décompressé"));
        verifyNoInteractions(articles);
    }
}
