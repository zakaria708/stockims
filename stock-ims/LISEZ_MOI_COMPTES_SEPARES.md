# StockIMS — inventaires séparés par compte

Chaque compte possède désormais son propre inventaire. Un compte nouvellement créé ne contient aucun article ni mouvement, et son tableau de bord commence à zéro.

La séparation couvre les articles, les catégories, les stocks, les recherches, les alertes, les statistiques, les mouvements, l'import CSV/Excel/TXT et l'export CSV. Chaque utilisateur peut ajouter, modifier et supprimer ses articles, et enregistrer ses entrées et sorties. Le compte administrateur utilise également son propre inventaire : il n'a pas d'accès global aux données des autres comptes.

Deux comptes différents peuvent utiliser la même référence d'article. Dans un même compte, une référence reste unique. Les contrôles sont appliqués côté serveur, y compris lorsqu'un identifiant d'article est modifié dans une adresse ou un formulaire.

## Utiliser une base existante sans perdre les informations

Les anciennes lignes ne contenaient aucun propriétaire. Le programme ne peut donc pas deviner le compte auquel elles appartiennent. Tous les anciens articles doivent être rattachés explicitement à un compte existant lors du premier démarrage de cette version.

1. Arrêter l'ancienne application. Ne pas utiliser les deux versions en même temps.
2. Faire une sauvegarde de la base MySQL avant le premier démarrage de la nouvelle version.
3. Extraire le nouveau ZIP dans un dossier distinct, puis ouvrir son dossier `stock-ims`.
4. Conserver les mêmes paramètres MySQL (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) pour retrouver la base existante.
5. Indiquer le nom exact du compte existant qui doit conserver les anciennes informations.

Dans un terminal **PowerShell**, remplacer `nom_exact_de_l_ancien_compte` par le nom utilisé pour se connecter (pas l'adresse e-mail) :

```powershell
$env:LEGACY_OWNER_USERNAME="nom_exact_de_l_ancien_compte"
```

Puis lancer l'application depuis le dossier contenant `pom.xml` :

```powershell
.\mvnw.cmd spring-boot:run
```

Dans IntelliJ, Eclipse ou une autre configuration de lancement, ajouter la variable d'environnement `LEGACY_OWNER_USERNAME` avec cette même valeur. On peut aussi renseigner temporairement `app.inventory.legacy-owner` dans `src/main/resources/application.properties`.

La migration ajoute le propriétaire aux anciens articles, conserve leurs identifiants et leurs mouvements, puis remplace l'unicité globale des références par une unicité par compte. Elle ne supprime ni les comptes, ni les articles, ni les mouvements.

Si la base contient des anciens articles et que le compte de reprise n'est pas indiqué, ou si le compte indiqué n'existe pas, l'application s'arrête avec un message explicatif. Rien n'est attribué automatiquement à un nouvel inscrit. Corriger le réglage, puis redémarrer. Le compte `admin` et le compte `user` de démonstration peuvent aussi être indiqués s'ils étaient utilisés auparavant.

Après une reprise réussie, la variable peut être retirée. Les redémarrages ne réattribuent pas les articles déjà associés à un compte, même si cette variable change. Si les anciens articles doivent être répartis entre plusieurs propriétaires, ne pas lancer la reprise avec un compte unique : une répartition explicite des lignes sera nécessaire, car l'ancienne version n'a enregistré aucune information permettant de reconstituer cette appartenance.

## Première installation avec une base vide

Aucun réglage `LEGACY_OWNER_USERNAME` n'est nécessaire. Configurer MySQL comme auparavant, utiliser un JDK 21, puis lancer l'application. Les comptes commencent vides et chacun remplit son inventaire.

## Vérification manuelle rapide

1. Se connecter avec l'ancien compte : ses articles et mouvements sont présents après la reprise.
2. Créer un autre compte et s'y connecter : aucun article, aucun mouvement et aucune catégorie provenant de l'ancien compte ne s'affichent.
3. Ajouter un article et du stock dans ce nouveau compte.
4. Revenir à l'ancien compte : ses informations n'ont pas changé, et le nouvel article n'y apparaît pas.
5. Essayer l'adresse d'un article de l'autre compte : une réponse 404 doit être renvoyée.

## Tests fournis

Depuis le dossier `stock-ims`, avec un JDK 21 :

```powershell
.\mvnw.cmd test
```

Les tests utilisent une base H2 temporaire et ne touchent pas à votre base MySQL. Ils couvrent la séparation des pages et des services, l'accès par identifiant, les modifications et suppressions interdites entre comptes, les mouvements personnels, l'import et l'export, les références identiques dans des comptes distincts, l'inscription d'un nouveau compte et la reprise des anciennes données.

Les noms de connexion sont utilisés comme identifiants stables de propriétaire, normalisés en minuscules. L'application ne propose pas de changement de nom de connexion. Ne pas renommer directement un compte dans MySQL : cela nécessite aussi une migration explicite de son propriétaire d'inventaire. Ne pas réutiliser un nom supprimé pour une autre personne. Ne pas changer les noms des comptes de démonstration une fois des données rattachées à ceux-ci sans migrer aussi leurs propriétaires.

Les identifiants de démonstration et leur configuration restent ceux du projet d'origine. Changer leurs mots de passe avant d'exposer l'application sur Internet.
