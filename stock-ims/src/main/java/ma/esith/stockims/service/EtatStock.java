package ma.esith.stockims.service;

/**
 * Règle métier demandée : un stock égal au seuil est considéré « Épuisé ».
 * Pour conserver la distinction « Faible », un stock positif inférieur
 * au seuil est classé « Faible ». Un stock nul reste « Épuisé ».
 */
public enum EtatStock {
    DISPONIBLE("Disponible", "badge-ok"),
    FAIBLE("Faible", "badge-low"),
    EPUISE("Épuisé", "badge-empty");

    private final String libelle;
    private final String classeCss;

    EtatStock(String libelle, String classeCss) {
        this.libelle = libelle;
        this.classeCss = classeCss;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getClasseCss() {
        return classeCss;
    }

    public static EtatStock calculer(long stock, int seuilAlerte) {
        if (stock <= 0 || stock == seuilAlerte) {
            return EPUISE;
        }
        if (stock < seuilAlerte) {
            return FAIBLE;
        }
        return DISPONIBLE;
    }
}
