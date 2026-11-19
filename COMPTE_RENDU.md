# Compte Rendu - TP Android Contacts V4

**Étudiant** : Maram Hsoumi  
**Matière** : Développement Mobile Android Kotlin  
**Projet** : TP Android — Gestion des contacts (Version 4 : SQLite & Préférences)  

---

## 1. Schéma de la base et choix des types
La table `contacts` a été modélisée selon les spécifications strictes du TP :
- `_id` (`INTEGER PRIMARY KEY AUTOINCREMENT`) : clé primaire auto-incrémentée mappée sur `id: Long`.
- `nom` (`TEXT NOT NULL`), `prenom` (`TEXT NOT NULL`), `telephone` (`TEXT NOT NULL`). Le téléphone est stocké en `TEXT` pour préserver le zéro initial, les indicatifs internationaux (`+216`, `+33`) et les espacements.
- `email` (`TEXT NULL`), `photo_uri` (`TEXT NULL`).
- `latitude` (`REAL NULL`), `longitude` (`REAL NULL`) : stockage flottant IEEE 754 permettant une précision cartographique maximale, ou `NULL` si non renseignées.

---

## 2. Rôles respectifs du Helper et du DAO
- **`ContactDbHelper`** : Hérite de `SQLiteOpenHelper`. Il gère le cycle de vie physique de la base (création du fichier, exécution de `CREATE TABLE` dans `onCreate` avec peuplement de 5 contacts initiaux, gestion des versions dans `onUpgrade`). Implémenté sous forme de singleton avec `applicationContext` pour éliminer tout risque de fuite mémoire.
- **`ContactDao`** : Unique classe renfermant les requêtes SQL (`insert`, `update`, `delete`, `getAll`, `getById`, `search`). Aucune chaîne SQL n'est dispersée dans les fragments. Utilisation exclusive de `selectionArgs` avec arguments `?` et fermeture automatique des curseurs via `use { }`.

---

## 3. Stratégie de gestion des Threads
Pour respecter scrupuleusement la règle d'or d'Android interdisant tout accès disque bloquant sur le thread d'interface graphique (Main Thread) :
- Les méthodes du `ContactViewModel` déclenchent des coroutines via le `viewModelScope`.
- L'exécution des requêtes DAO est déléguée au thread pool dédié I/O à l'aide de `withContext(Dispatchers.IO)`.
- Les résultats sont propagés au thread UI de façon transparente grâce au pattern réactif `LiveData`.

---

## 4. Préférences utilisateur et rafraîchissement immédiat
- Trois préférences ont été implémentées avec `PreferenceFragmentCompat` :
  1. Taille de la police (`taille_police` : 14sp, 16sp, 20sp).
  2. Couleur du texte (`couleur_preferee` : Noir, Bleu, Vert, Rouge, Violet).
  3. Bouton d'appel rapide dans la liste (`appel_depuis_liste` : booléen).
- L'application immédiate est orchestrée dans la méthode `onResume()` de `ListeContactsFragment` : à chaque réaffichage de la liste, `PreferencesManager` relit les réglages et notifie l'Adapter (`notifyDataSetChanged()`), ce qui met instantanément à jour les tailles, couleurs et la visibilité du bouton d'appel sans aucun besoin de relancer l'application.

---

## 5. Gestion des Photos
Afin d'éviter qu'une URI temporaire provenant de la Galerie ne devienne inaccessible après redémarrage du smartphone, le fichier image sélectionné est recopié physiquement dans le répertoire privé de l'application (`context.filesDir`). Le chemin absolu de ce fichier interne pérenne est ensuite enregistré dans la colonne `photo_uri` de SQLite.
