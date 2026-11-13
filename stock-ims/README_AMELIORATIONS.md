# StockIMS — gestion de stock par compte

Application Java 21, Spring Boot, Spring Security, Thymeleaf et MySQL.

## Fonctionnement

- Inscription, connexion et déconnexion.
- Inventaire personnel pour chaque compte, vide lors de sa création.
- Ajout, modification et suppression des articles de son propre compte.
- Entrées et sorties personnelles, avec vérification du stock et verrouillage de l'article.
- Tableau de bord, alertes, catégories, recherche et historique limités au compte connecté.
- Import CSV, Excel (.xlsx/.xls) et TXT limité au compte connecté ; export CSV personnel.
- Même référence possible dans deux comptes différents, sans doublon au sein d'un même compte.
- Protection côté serveur contre les accès aux articles d'un autre compte, même par adresse directe.
- Mots de passe des comptes inscrits hachés avec BCrypt et protection CSRF conservée.

Les utilisateurs USER peuvent gérer leur propre stock, comme les administrateurs. ADMIN n'accorde pas d'accès aux inventaires d'autres comptes.

## Important pour une ancienne base

Lire **LISEZ_MOI_COMPTES_SEPARES.md** avant le premier lancement de cette version. Une sauvegarde MySQL et le réglage `LEGACY_OWNER_USERNAME` sont nécessaires si la base contient des articles de l'ancienne version. Aucun nouveau compte ne récupère automatiquement les anciennes données.

## Configuration et lancement sous Windows

Utiliser un JDK 21 et configurer MySQL avec les variables `DB_URL`, `DB_USERNAME` et `DB_PASSWORD`. Les valeurs par défaut ciblent `jdbc:mysql://localhost:3306/stockims_db` avec l'utilisateur `root` et un mot de passe vide ; adapter ces réglages à votre installation.

Depuis le terminal PowerShell du dossier `stock-ims` :

```powershell
.\mvnw.cmd spring-boot:run
```

Puis ouvrir http://localhost:8080.

Pour les tests, sans utiliser la base MySQL de l'application :

```powershell
.\mvnw.cmd test
```

## Import CSV, Excel et TXT

Les formats `.csv`, `.xlsx`, `.xls` et `.txt` sont acceptés depuis les pages Articles et Tableau de bord. Lire **IMPORT_FICHIERS.md** pour les colonnes, les exemples et les limites.

```csv
reference;designation;categorie;seuilAlerte;stockInitial
TIS-001;Rouleau de coton blanc;Textile;10;50
TIS-002;Fil polyester noir;Fil;20;100
```

`reference` et `designation` sont obligatoires. Les autres colonnes sont facultatives. Le stock initial et l'article sont enregistrés dans la même transaction. Des exemples CSV et TXT sont disponibles dans `src/main/resources/static/exemples/` et via les liens du formulaire. Pour Excel, utiliser les mêmes en-têtes de colonnes sur la première feuille visible non vide.

## Comptes de démonstration

Les comptes d'origine `admin` / `admin123` et `user` / `user123` restent configurables avec `ADMIN_USERNAME`, `ADMIN_PASSWORD`, `VIEWER_USERNAME` et `VIEWER_PASSWORD`. Changer les mots de passe avant une utilisation en ligne. Les nouveaux comptes disposent de leur propre inventaire vide.
