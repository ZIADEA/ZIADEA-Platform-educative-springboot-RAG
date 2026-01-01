# 🚀 Guide d'Installation - EduForge

Ce guide explique comment installer et exécuter l'application EduForge sur votre PC après avoir décompressé le fichier ZIP.

---

## 📋 Prérequis

Avant de commencer, assurez-vous d'avoir installé les logiciels suivants:

### 1. Java 21 (JDK)

**Téléchargement:** https://adoptium.net/temurin/releases/?version=21

**Vérifier l'installation:**
```powershell
java -version
```
Vous devriez voir quelque chose comme: `openjdk version "21.0.x"`

**Configuration de JAVA_HOME (Windows):**
1. Recherchez "Variables d'environnement" dans Windows
2. Cliquez sur "Variables d'environnement..."
3. Sous "Variables système", cliquez "Nouvelle..."
4. Nom: `JAVA_HOME`
5. Valeur: `C:\Program Files\Eclipse Adoptium\jdk-21.x.x-hotspot` (adaptez le chemin)
6. Ajoutez `%JAVA_HOME%\bin` à la variable `Path`

---

### 2. Docker Desktop

**Téléchargement:** https://www.docker.com/products/docker-desktop/

Docker est nécessaire pour exécuter la base de données PostgreSQL avec l'extension pgvector.

**Vérifier l'installation:**
```powershell
docker --version
docker-compose --version
```

**⚠️ Important:** Assurez-vous que Docker Desktop est **démarré** avant de continuer.

---

### 3. Clé API Google Gemini (pour les fonctionnalités IA)

1. Allez sur: https://makersuite.google.com/app/apikey
2. Connectez-vous avec votre compte Google
3. Cliquez sur "Create API Key"
4. Copiez la clé générée (format: `AIzaSy...`)

---

## 📁 Structure des Fichiers

Après avoir décompressé le ZIP, vous devriez avoir la structure suivante:

```
EduForge/
├── mvnw                    # Maven wrapper (Linux/Mac)
├── mvnw.cmd                # Maven wrapper (Windows)
├── pom.xml                 # Configuration Maven
├── README.md               # Documentation technique
├── INSTALLATION_GUIDE.md   # Ce fichier
├── src/                    # Code source
│   └── main/
│       ├── java/           # Code Java
│       └── resources/      # Configuration
│           ├── application.yml
│           ├── application-dev.yml
│           └── docker/
│               └── docker-compose.yml
└── data/                   # Données (uploads, etc.)
```

---

## 🛠️ Installation Étape par Étape

### Étape 1: Décompresser le ZIP

Décompressez le fichier `EduForge.zip` dans un répertoire de votre choix, par exemple:
```
C:\Projets\EduForge
```

**⚠️ Évitez** les chemins avec des espaces ou des caractères spéciaux.

---

### Étape 2: Ouvrir un Terminal

Ouvrez **PowerShell** ou **Windows Terminal** et naviguez vers le dossier:

```powershell
cd C:\Projets\EduForge
```

---

### Étape 3: Démarrer la Base de Données

La base de données PostgreSQL s'exécute dans Docker. Lancez-la avec:

```powershell
docker-compose -f src/main/resources/docker/docker-compose.yml up -d
```

**Vérifier que la base de données est démarrée:**
```powershell
docker ps
```

Vous devriez voir deux conteneurs:
- `eduforge-postgres` (PostgreSQL avec pgvector)
- `eduforge-pgadmin` (Interface d'administration - optionnel)

**Attendez 10-15 secondes** que PostgreSQL soit complètement initialisé.

---

### Étape 4: Configurer la Clé API Gemini

**Option A: Variable d'environnement temporaire (pour ce terminal uniquement)**
```powershell
$env:GEMINI_API_KEY = "VOTRE_CLE_API_ICI"
```

**Option B: Variable d'environnement permanente (recommandé)**
1. Recherchez "Variables d'environnement" dans Windows
2. Cliquez sur "Variables d'environnement..."
3. Sous "Variables utilisateur", cliquez "Nouvelle..."
4. Nom: `GEMINI_API_KEY`
5. Valeur: Votre clé API Gemini
6. Cliquez OK et **redémarrez votre terminal**

**Option C: Modifier le fichier de configuration**

Éditez `src/main/resources/application-dev.yml` et ajoutez:
```yaml
app:
  ai:
    gemini:
      apiKey: VOTRE_CLE_API_ICI
```

---

### Étape 5: Lancer l'Application

Exécutez la commande suivante depuis le dossier racine du projet:

```powershell
.\mvnw.cmd spring-boot:run
```

**Premier lancement:** Maven va télécharger toutes les dépendances (~100-200 MB). Cela peut prendre **5-10 minutes** selon votre connexion internet.

**Lancement réussi:** Vous verrez un message comme:
```
Started EduForgeApplication in X.XXX seconds
```

---

### Étape 6: Accéder à l'Application

Ouvrez votre navigateur et allez sur:

🌐 **http://localhost:8080**

---

## 👤 Comptes de Test

L'application est livrée avec des comptes de test préchargés:

| Rôle | Email | Mot de passe |
|------|-------|--------------|
| Admin | `admin@eduforge.local` | `password123` |
| Professeur | `prof@eduforge.local` | `password123` |
| Étudiant | `student@eduforge.local` | `password123` |
| Gestionnaire Institution | `manager@eduforge.local` | `password123` |

---

## 🔧 Commandes Utiles

### Arrêter l'Application
Appuyez sur `Ctrl + C` dans le terminal où l'application s'exécute.

### Arrêter la Base de Données
```powershell
docker-compose -f src/main/resources/docker/docker-compose.yml down
```

### Redémarrer la Base de Données (en conservant les données)
```powershell
docker-compose -f src/main/resources/docker/docker-compose.yml restart
```

### Supprimer toutes les données et recommencer à zéro
```powershell
docker-compose -f src/main/resources/docker/docker-compose.yml down -v
docker-compose -f src/main/resources/docker/docker-compose.yml up -d
```

### Nettoyer et Recompiler le Projet
```powershell
.\mvnw.cmd clean compile
```

### Voir les Logs de la Base de Données
```powershell
docker logs eduforge-postgres
```

---

## 🌐 Accès à pgAdmin (Administration Base de Données)

pgAdmin est une interface web pour gérer PostgreSQL:

**URL:** http://localhost:5050

**Connexion pgAdmin:**
- Email: `admin@eduforge.local`
- Mot de passe: `admin`

**Pour ajouter le serveur PostgreSQL dans pgAdmin:**
1. Clic droit sur "Servers" → "Register" → "Server..."
2. Onglet "General": Nom = `EduForge`
3. Onglet "Connection":
   - Host: `postgres` (ou `host.docker.internal` si ça ne marche pas)
   - Port: `5432`
   - Database: `eduforge`
   - Username: `eduforge`
   - Password: `eduforge`
4. Cliquez "Save"

---

## ❗ Résolution des Problèmes

### Problème: "Port 8080 already in use"

**Solution:** Un autre programme utilise le port 8080. Fermez-le ou changez le port:

```powershell
# Trouver et arrêter le processus sur le port 8080
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }

# Ou lancez l'application sur un autre port
.\mvnw.cmd spring-boot:run -Dserver.port=8081
```

---

### Problème: "Connection refused" ou "Cannot connect to database"

**Causes possibles:**
1. Docker n'est pas démarré → Lancez Docker Desktop
2. Le conteneur PostgreSQL n'est pas lancé → `docker-compose up -d`
3. PostgreSQL n'a pas fini de démarrer → Attendez 15 secondes

**Vérification:**
```powershell
# Vérifier que le conteneur est en cours d'exécution
docker ps

# Vérifier les logs PostgreSQL
docker logs eduforge-postgres
```

---

### Problème: "GEMINI_API_KEY manquante"

L'application fonctionne sans clé API, mais les fonctionnalités IA (génération de quiz, notation automatique) ne seront pas disponibles.

**Solution:** Configurez la variable d'environnement comme expliqué à l'Étape 4.

---

### Problème: "JAVA_HOME is not set"

**Solution:**
```powershell
# Vérifier si Java est installé
java -version

# Si Java est installé mais JAVA_HOME n'est pas configuré:
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.5+11"
```

---

### Problème: Maven télécharge des fichiers à chaque lancement

C'est normal au premier lancement. Les dépendances sont ensuite mises en cache dans `~/.m2/repository`.

**Si le problème persiste:**
```powershell
# Forcer le téléchargement des dépendances
.\mvnw.cmd dependency:resolve
```

---

### Problème: Erreur "flyway" ou migration

Les migrations Flyway sont désactivées par défaut. Si vous voyez des erreurs:

1. Vérifiez que `spring.flyway.enabled: false` est dans `application.yml`
2. Ou activez-les si vous voulez initialiser le schéma complet

---

## 📊 Configuration Réseau

| Service | Port Local | Description |
|---------|------------|-------------|
| Application EduForge | 8080 | Interface web principale |
| PostgreSQL | 5433 | Base de données (Docker → 5432) |
| pgAdmin | 5050 | Administration PostgreSQL |

---

## 🔄 Mise à Jour de l'Application

Si vous recevez une nouvelle version du ZIP:

1. **Arrêtez** l'application (`Ctrl + C`)
2. **Sauvegardez** le dossier `data/` (contient vos uploads)
3. **Décompressez** le nouveau ZIP
4. **Restaurez** le dossier `data/` dans le nouveau projet
5. **Redémarrez** l'application

---

## 📞 Support

En cas de problème:
1. Vérifiez les logs de l'application dans le terminal
2. Vérifiez les logs Docker: `docker logs eduforge-postgres`
3. Consultez le fichier `README.md` pour la documentation technique

---

## ✅ Checklist de Démarrage Rapide

```
□ Java 21 installé et JAVA_HOME configuré
□ Docker Desktop installé et démarré
□ ZIP décompressé dans un dossier sans espaces
□ Base de données démarrée (docker-compose up -d)
□ Clé API Gemini configurée (optionnel)
□ Application lancée (mvnw.cmd spring-boot:run)
□ Navigateur ouvert sur http://localhost:8080
```

---

**Bonne utilisation d'EduForge! 🎓**
