# Validation — import CSV, Excel et TXT — 30 octobre 2026

Compilation et tests réussis : **30 tests, 0 échec, 0 erreur**.

- 14 tests de lecture et de validation : CSV avec trois séparateurs, TXT et accents en UTF-8/UTF-16/Windows-1252, guillemets et cellules multilignes, véritables classeurs `.xlsx` et `.xls`, feuilles masquées, références avec zéros initiaux, cellules numériques et valeurs calculées enregistrées, refus des fractions, doublons, fichiers invalides et limites de volume.
- 12 tests d'intégration Spring Boot / MockMvc : séparation des comptes et des mouvements, compte nouvellement créé vide, accès direct interdit, opérations personnelles, import HTTP dans chacun des quatre formats sans transfert de propriété et protection CSRF.
- 4 tests de reprise des anciennes données : propriétaire explicite, compte valide, conservation des mouvements et des propriétaires, unicité par compte et base vide.

Les classeurs Excel des tests sont produits et lus avec Apache POI ; les cas d'import HTTP vérifient également l'enregistrement des articles et des entrées de stock en base temporaire.

Les tests s'exécutent sur H2 en mode MySQL, sans connexion à la base réelle de l'utilisateur. Le JDK disponible pour la validation est Java 17 ; la propriété Maven `java.version` a été remplacée temporairement par `17` lors de l'exécution. Le projet livré reste configuré pour Java 21 comme auparavant.

Voir `IMPORT_FICHIERS.md` pour les formats, les limites et les colonnes. La logique de reprise des propriétaires et les paramètres MySQL ne changent pas dans cette mise à jour.
