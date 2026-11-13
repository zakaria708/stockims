# Importer des articles depuis CSV, Excel ou TXT

Dans **Articles**, choisir un fichier puis cliquer sur **Importer le fichier**. Le bouton **Importer un fichier** du tableau de bord accepte les mêmes formats.

| Format | Organisation attendue |
| --- | --- |
| CSV `.csv` | Tableau séparé par des points-virgules, des virgules ou des tabulations. |
| Excel `.xlsx` ou `.xls` | Première feuille visible non vide ; première ligne remplie contenant les en-têtes. |
| Texte `.txt` | Tableau séparé par des points-virgules, des virgules ou des tabulations, comme un CSV. |

Un TXT contenant un paragraphe libre n'est pas un tableau d'articles. Pour les fichiers texte, les encodages UTF-8, UTF-16 avec marque BOM et Windows-1252 sont reconnus. Les guillemets CSV et les cellules contenant des retours à la ligne sont pris en charge.

## Colonnes

Les colonnes peuvent être dans un autre ordre. Les majuscules, les accents et les espaces des noms de colonnes sont tolérés, par exemple `Référence`, `Désignation` ou `Stock initial`.

| Colonne | Obligatoire | Valeur si absente ou vide |
| --- | --- | --- |
| `reference` | Oui | Aucune ; référence unique dans votre compte, 20 caractères maximum. |
| `designation` | Oui | Aucune ; 80 caractères maximum. |
| `categorie` | Non | Non classé. |
| `seuilAlerte` | Non | 5. |
| `stockInitial` | Non | 0. |

Pour les valeurs numériques, utiliser des entiers sans séparateurs de milliers dans les cellules texte. Le seuil doit être compris entre 0 et 1 000 000 et le stock initial entre 0 et 100 000 000. Un nombre fractionnaire n'est pas arrondi : sa ligne est ignorée avec une explication. Une cellule numérique Excel est lue avec sa valeur numérique, même si son affichage comporte un séparateur de milliers.

Les références contenant des zéros initiaux peuvent être saisies au format texte dans Excel, ou utiliser un format de cellule qui affiche ces zéros.

## Exemple CSV ou TXT

```text
reference;designation;categorie;seuilAlerte;stockInitial
TIS-001;Rouleau de coton blanc;Textile;10;50
TIS-002;Fil polyester noir;Fil;20;100
```

Pour Excel, placer les cinq en-têtes dans les cellules A1 à E1 et les articles sur les lignes suivantes, puis enregistrer en `.xlsx` ou `.xls`.

## Résultat et limites

- L'import ajoute les nouvelles références dans le compte connecté. Il ne modifie pas les références déjà existantes dans ce compte.
- Une référence utilisée dans un autre compte peut être importée dans le vôtre.
- Les lignes de données invalides ou les doublons de votre compte sont ignorés ; les autres lignes sont importées. Un bilan indique les nombres de lignes ajoutées et ignorées et affiche les premiers détails d'erreur.
- Le stock initial est enregistré comme une entrée de stock, dans la même transaction que l'article.
- Une structure illisible, des en-têtes obligatoires manquants ou des en-têtes répétés font refuser le fichier avant l'ajout d'articles.
- Maximum : 5 Mo par fichier, 10 000 lignes de données, 50 colonnes. La lecture d'Excel limite aussi le volume décompressé à 25 Mo et le nombre d'éléments ZIP à 1 000. Les nombreuses lignes vides mises en forme dans Excel comptent également pour la limite de lecture.
- Pour un fichier Excel contenant des formules, enregistrer le fichier dans Excel avant l'import pour mettre à jour ses valeurs calculées. L'application lit ces valeurs enregistrées sans exécuter de formules ou de macros. Utiliser un fichier non chiffré et non protégé par un mot de passe d'ouverture.

L'export reste au format CSV. Cette mise à jour utilise la même base et les mêmes propriétaires d'articles que la version précédente ; conserver vos paramètres MySQL habituels.

## Dépendances utilisées

Lecture Excel : Apache POI 6.0.0, documentation : https://poi.apache.org/components/spreadsheet/quick-guide.html et https://poi.apache.org/apidocs/6.0/org/apache/poi/ss/usermodel/DataFormatter.html.

Lecture CSV/TXT : Apache Commons CSV 1.14.1, documentation : https://commons.apache.org/proper/commons-csv/.
