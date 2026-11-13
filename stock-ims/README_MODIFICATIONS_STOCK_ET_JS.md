# StockIMS — version corrigée (états et JavaScript)

Ce ZIP contient **tout le projet Java/Spring Boot d'origine**, avec le correctif et les fonctionnalités JavaScript supplémentaires.

## Nouvelle logique demandée

| Stock actuel | Seuil d'alerte | État |
|---:|---:|---|
| 50 | 10 | Disponible |
| 5 | 10 | Faible |
| 8 | 8 | Épuisé |
| 0 | 8 | Épuisé |

**Attention : il s'agit d'une règle personnalisée.** Le libellé « Épuisé » ne signifie donc pas toujours que les unités physiques sont à zéro. Par exemple, 8 unités restantes avec seuil 8 sont classées « Épuisé ». Si le stock passe à 7 pour un seuil de 8, il devient « Faible » selon la règle explicitement choisie. Les mouvements de stock restent possibles quand la quantité réelle est supérieure à zéro.

La classification est définie dans `src/main/java/ma/esith/stockims/service/EtatStock.java` et utilisée dans : tableau de bord, filtres, liste d'articles, fiches et alertes.

## JavaScript ajouté

Le fichier `src/main/resources/static/js/app.js` comprend :

- Prévisualisation **en temps réel** de l'état d'un nouvel article dès qu'on saisit le stock initial et le seuil ;
- Validation des extensions CSV, TXT, XLS, XLSX et de la taille maximale de 5 Mo avant import ;
- Import rapide depuis le tableau de bord, sans JavaScript intégré dans le HTML ;
- Confirmation avant suppression et menu mobile (fonctionnalités existantes conservées).

L'enregistrement, les permissions et les validations critiques restent côté Java/Spring Boot.

## Démarrage Windows 11 (PowerShell)

1. Extraire le ZIP et ouvrir PowerShell dans le dossier `stock-ims`.
2. S'assurer que **Java 21**, **MySQL** et la base configurée sont disponibles. Vérifier `src/main/resources/application.properties` ou les variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
3. Lancer :

```powershell
.\mvnw.cmd spring-boot:run
```

4. Ouvrir `http://localhost:8080` (ou le port défini localement).
5. Pour tester :

```powershell
.\mvnw.cmd test
```

**Votre base de données existante n'est pas remplacée par le ZIP.** Conserver une sauvegarde avant de modifier l'application ou sa configuration. Pour la migration d'une ancienne base, lire `LISEZ_MOI_COMPTES_SEPARES.md`.

## Tests ajoutés

`src/test/java/ma/esith/stockims/EtatStockTest.java` vérifie la classification, notamment **stock = seuil = 8 -> Épuisé**.
