# ZIADEA-Platform-educative-springboot-RAG

DEMO : https://youtu.be/niIE1IVkrNQ?si=wR6zZcL30yrTMMtj
# 📚 EduForge - Documentation Technique Complète

## Vue d'ensemble

**EduForge** est une plateforme pédagogique complète développée avec **Spring Boot 3.x**, intégrant l'intelligence artificielle pour la génération de quiz, la notation automatique et la recherche sémantique (RAG - Retrieval-Augmented Generation). L'application supporte plusieurs rôles utilisateurs et offre un écosystème éducatif complet avec cours, quiz, examens, salles de classe virtuelles et institutions.

**Stack Technique:**
- Backend: Spring Boot 3.4.0, Java 21
- Base de données: PostgreSQL avec extension pgvector
- Template Engine: Thymeleaf
- IA: Google Gemini API
- Build Tool: Maven
- Sécurité: Spring Security 6

---

## 📁 Architecture en Couches

L'application suit une architecture en couches classique Spring Boot:

```
com.eduforge.platform
├── domain/              → Couche Domaine (Entités métier)
├── repository/          → Couche DAL (Data Access Layer)
├── service/             → Couche Métier (Business Logic)
├── web/                 → Couche Présentation (Controllers, DTOs)
├── security/            → Couche Sécurité
├── config/              → Configuration applicative
└── util/                → Utilitaires
```

---

## 📦 Détail des Fichiers Java

### 🎯 Point d'Entrée de l'Application

#### `EduForgeApplication.java`
**Couche:** Application (Main)  
**Fonction:** Point d'entrée Spring Boot avec configuration async et scanning des propriétés. Lance l'application web sur le port configuré (8080 par défaut).

---

### 🏢 Couche Domain (Entités Métier JPA)

Les entités représentent le modèle de données et sont mappées directement aux tables PostgreSQL via Hibernate.

#### 📁 `domain.auth` - Authentification & Autorisation

##### `User.java`
**Fonction:** Entité centrale des utilisateurs avec rôles (ADMIN, PROF, ETUDIANT, INSTITUTION_MANAGER), gestion de l'authentification, affiliation institutionnelle, et profil.  
**Relations:** Many-to-One avec Institution, One-to-Many avec Course/Classroom/Quiz attempts

##### `Role.java` (enum)
**Fonction:** Énumération des rôles système - ADMIN, PROF, ETUDIANT, INSTITUTION_MANAGER

##### `AccountStatus.java` (enum)
**Fonction:** Statut du compte - ACTIVE, PENDING, DISABLED

##### `AffiliationStatus.java` (enum)
**Fonction:** Statut d'affiliation institutionnelle - INDEPENDENT, PENDING, AFFILIATED, REJECTED, SUSPENDED

#### 📁 `domain.course` - Gestion des Cours

##### `Course.java`
**Fonction:** Entité cours avec contenu, matériaux, statut de publication (DRAFT/PUBLIC/CLASSE), et support d'indexation RAG pour génération de quiz IA.  
**Relations:** Many-to-One avec User (propriétaire), One-to-Many avec Chapter/Material/Quiz

##### `CourseChapter.java`
**Fonction:** Chapitres de cours supportant multiples formats (PDF, PPTX, vidéo, texte) avec ordre d'affichage. Permet la division structurée du contenu.  
**Relations:** Many-to-One avec Course

##### `CourseMaterial.java`
**Fonction:** Matériaux uploadés (PDF/PPTX/TEXT) avec extraction de texte pour indexation RAG.  
**Relations:** Many-to-One avec Course, Optional Many-to-One avec Chapter

##### `CourseEnrollment.java`
**Fonction:** Inscription des étudiants aux cours publics avec suivi de progression et chapitres complétés.  
**Relations:** Many-to-One avec User et Course, Many-to-Many avec CourseChapter (via completed_chapters)

##### `CourseStatus.java` (enum)
**Fonction:** Statut de publication - DRAFT (brouillon), PUBLIC (accessible à tous), CLASSE (réservé à une classe), PUBLISHED (alias de PUBLIC)

##### `ClassroomCourse.java`
**Fonction:** Table de jonction liant cours aux salles de classe spécifiques (relation Many-to-Many).  
**Relations:** Many-to-One avec Classroom et Course

#### 📁 `domain.classroom` - Salles de Classe Virtuelles

##### `Classroom.java`
**Fonction:** Classe virtuelle avec code de jointure unique, propriétaire (professeur), liaison optionnelle à une institution, configuration de notation.  
**Relations:** Many-to-One avec User (owner) et Institution, One-to-Many avec ClassroomEnrollment/Post

##### `ClassroomEnrollment.java`
**Fonction:** Inscription des étudiants dans les classes avec horodatage.  
**Relations:** Many-to-One avec Classroom et User

##### `ClassroomPost.java`
**Fonction:** Annonces et ressources postées dans la classe par le professeur.  
**Relations:** Many-to-One avec Classroom et User (auteur)

##### `ClassroomLevel.java`
**Fonction:** Niveaux de gamification pour progression des étudiants (système XP).  
**Relations:** Many-to-One avec Classroom

##### `StudentProgress.java`
**Fonction:** Suivi complet de progression étudiant - points XP, activités, statistiques quiz/examens, achievements.  
**Relations:** Many-to-One avec User et Classroom

#### 📁 `domain.quiz` - Système de Quiz IA

##### `Quiz.java`
**Fonction:** Quiz généré par IA lié à un cours avec niveaux de difficulté (EASY/MEDIUM/HARD), nombre de questions configurable.  
**Relations:** Many-to-One avec Course, Optional Many-to-One avec CourseChapter, One-to-Many avec QuizQuestion/QuizAttempt

##### `QuizQuestion.java`
**Fonction:** Questions de quiz supportant QCM (4 choix A-D) et questions ouvertes avec critères de notation IA.  
**Relations:** Many-to-One avec Quiz

##### `QuizAttempt.java`
**Fonction:** Tentative de quiz d'un étudiant avec score, statut (PASSED/FAILED), horodatage.  
**Relations:** Many-to-One avec Quiz et User, One-to-Many avec QuizAttemptAnswer

##### `QuizAttemptAnswer.java`
**Fonction:** Réponses individuelles aux questions avec correction automatique (QCM) ou notation IA (questions ouvertes).  
**Relations:** Many-to-One avec QuizAttempt et QuizQuestion

##### `QuestionType.java` (enum)
**Fonction:** Type de question - MCQ (choix multiples) ou OPEN_ENDED (question ouverte)

##### `Difficulty.java` (enum)
**Fonction:** Difficulté du quiz - EASY, MEDIUM, HARD

##### `AttemptStatus.java` (enum)
**Fonction:** Résultat de tentative - PASSED, FAILED

#### 📁 `domain.exam` - Système d'Examens

##### `Exam.java`
**Fonction:** Examens planifiés pour classes avec types (QUIZ/TEST/MIDTERM/FINAL), durée, fenêtre temporelle, configuration de notation.  
**Relations:** Many-to-One avec Classroom, One-to-Many avec ExamQuestion/ExamAttempt

##### `ExamQuestion.java`
**Fonction:** Questions d'examen (QCM ou ouvertes) avec critères de notation et barème.  
**Relations:** Many-to-One avec Exam

##### `ExamAttempt.java`
**Fonction:** Soumission d'examen étudiant avec timing, score, statut (IN_PROGRESS/SUBMITTED/GRADED/PASSED/FAILED).  
**Relations:** Many-to-One avec Exam et User, One-to-Many avec ExamAnswer

##### `ExamAnswer.java`
**Fonction:** Réponses étudiants aux questions d'examen avec correction et notation IA.  
**Relations:** Many-to-One avec ExamAttempt et ExamQuestion

##### `ExamType.java` (enum)
**Fonction:** Type d'examen - QUIZ, TEST, MIDTERM, FINAL, RETAKE

##### `ExamStatus.java` (enum)
**Fonction:** Cycle de vie examen - DRAFT, PUBLISHED, SCHEDULED, IN_PROGRESS, CLOSED, ARCHIVED

##### `AttemptStatus.java` (enum)
**Fonction:** Statut de tentative - IN_PROGRESS, SUBMITTED, GRADED, PASSED, FAILED

#### 📁 `domain.institution` - Institutions Éducatives

##### `Institution.java`
**Fonction:** Établissement éducatif (université, école) avec propriétaire, configuration de notation (échelles, méthodes), catalogue académique.  
**Relations:** Many-to-One avec User (owner), One-to-Many avec Program/Membership

##### `InstitutionMembership.java`
**Fonction:** Adhésion utilisateur aux institutions avec workflow d'approbation (PENDING/APPROVED/REJECTED).  
**Relations:** Many-to-One avec Institution et User

##### `InstitutionType.java` (enum)
**Fonction:** Type d'établissement - ECOLE, UNIVERSITE

##### `MembershipStatus.java` (enum)
**Fonction:** Statut d'adhésion - PENDING, APPROVED, REJECTED

##### `GradingMethod.java` (enum)
**Fonction:** Méthode de notation - STANDARD, CANADIAN (avec points négatifs)

##### `GradingScale.java` (enum)
**Fonction:** Échelles de notation - SCALE_20, SCALE_100, SCALE_10, SCALE_5, LETTER_GRADE

#### 📁 `domain.catalog` - Catalogue Académique

##### `Program.java`
**Fonction:** Programmes académiques au sein des institutions (ex: Génie Informatique, Médecine).  
**Relations:** Many-to-One avec Institution, One-to-Many avec AcademicLevel

##### `AcademicLevel.java`
**Fonction:** Niveaux académiques dans les programmes (L1, L2, L3, Master 1, etc.).  
**Relations:** Many-to-One avec Program, One-to-Many avec Subject

##### `Subject.java`
**Fonction:** Matières/cours offerts à des niveaux académiques spécifiques.  
**Relations:** Many-to-One avec AcademicLevel

#### 📁 `domain.messaging` - Messagerie & Notifications

##### `Message.java`
**Fonction:** Messages directs, messages de classe, annonces avec threading et statuts de lecture.  
**Relations:** Many-to-One avec User (sender/receiver), Optional Many-to-One avec Classroom

##### `Notification.java`
**Fonction:** Notifications utilisateur avec types (INFO/SUCCESS/WARNING/ERROR/EXAM/GRADE/MESSAGE) et statut de lecture.  
**Relations:** Many-to-One avec User

##### `MessageType.java` (enum)
**Fonction:** Catégories de messages - DIRECT, CLASSROOM, ANNOUNCEMENT, SYSTEM

##### `NotificationType.java` (enum)
**Fonction:** Types de notifications - INFO, SUCCESS, WARNING, ERROR, EXAM, GRADE, MESSAGE, ENROLLMENT, SYSTEM

#### 📁 `domain.library` - Bibliothèque Institutionnelle

##### `LibraryResource.java`
**Fonction:** Ressources de bibliothèque institutionnelle (PDFs, livres, documents) avec catégorisation, extraction de texte OCR.  
**Relations:** Many-to-One avec Institution

#### 📁 `domain.reviewbook` - Fiches de Révision Personnelles

##### `ReviewBook.java`
**Fonction:** Documents uploadés par étudiants pour révision personnalisée avec OCR, extraction de texte, génération de quiz IA.  
**Relations:** Many-to-One avec User

##### `ReviewBookStatus.java` (enum)
**Fonction:** Statut de traitement - PENDING, PROCESSING, READY, FAILED

##### `ReviewBookFileType.java` (enum)
**Fonction:** Types de fichiers supportés - PDF, TXT, IMAGE (avec support OCR)

#### 📁 `domain.rag` - Recherche Sémantique RAG

##### `CourseChunk.java`
**Fonction:** Morceaux de texte extraits des cours avec termes TF-IDF et embeddings vectoriels (768 dimensions) pour recherche sémantique et génération de quiz contextuels.  
**Relations:** Many-to-One avec Course

#### 📁 `domain.certificate` - Certificats

##### `Certificate.java`
**Fonction:** Certificats de complétion de cours/classe avec codes de vérification uniques et scores de réussite.  
**Relations:** Many-to-One avec User, Optional Many-to-One avec Course/Classroom

---

### 🗄️ Couche Repository (DAL - Data Access Layer)

Tous les repositories étendent `JpaRepository<Entity, Long>` et fournissent des méthodes d'accès aux données avec requêtes JPQL/Query Methods.

#### Repositories Principaux

##### `UserRepository.java`
**Fonction:** Accès données utilisateurs avec recherche par email, comptages par rôle.  
**Méthodes clés:** `findByEmail()`, `existsByEmail()`, `countByRole()`

##### `CourseRepository.java`
**Fonction:** Requêtes cours par propriétaire, statut, classe avec support de recherche textuelle.  
**Méthodes clés:** `findByOwnerId()`, `findByStatus()`, `findByTargetClassroomId()`

##### `CourseChunkRepository.java`
**Fonction:** Accès aux chunks RAG avec recherche de similarité vectorielle (cosine similarity).  
**Méthodes clés:** `findByCourseId()`, `findTopKBySimilarity()` (requête native pgvector)

##### `ClassroomRepository.java`
**Fonction:** Requêtes classes avec recherche par code de jointure unique.  
**Méthodes clés:** `findByCode()`, `findByOwnerId()`, `existsByCode()`

##### `QuizRepository.java`
**Fonction:** Accès quiz par cours, chapitre, classe.  
**Méthodes clés:** `findByCourseId()`, `findByChapterId()`, `findByClassroomId()`

##### `QuizAttemptRepository.java`
**Fonction:** Tentatives de quiz par étudiant avec statistiques.  
**Méthodes clés:** `findByUserIdAndQuizId()`, `countByUserIdAndStatus()`

##### `ExamRepository.java`
**Fonction:** Requêtes examens par classe, statut, dates.  
**Méthodes clés:** `findByClassroomId()`, `findByStatus()`, `findUpcoming()`

##### `InstitutionRepository.java`
**Fonction:** Recherche institutions par propriétaire.  
**Méthodes clés:** `findByOwnerId()`

##### `InstitutionMembershipRepository.java`
**Fonction:** Adhésions institutionnelles avec filtres par statut.  
**Méthodes clés:** `findByInstitutionIdAndStatus()`, `findByUserId()`

##### `MessageRepository.java`
**Fonction:** Messages par classe/expéditeur/destinataire avec recherche textuelle.  
**Méthodes clés:** `findByClassroomId()`, `findBySenderOrReceiver()`, `searchByContent()`

##### `NotificationRepository.java`
**Fonction:** Notifications utilisateur avec comptage non lues.  
**Méthodes clés:** `findByUserIdOrderByCreatedAtDesc()`, `countByUserIdAndReadFalse()`

##### `ReviewBookRepository.java`
**Fonction:** Fiches de révision par étudiant avec filtres par statut.  
**Méthodes clés:** `findByUserId()`, `findByUserIdAndStatus()`

##### Autres Repositories
- `CourseChapterRepository`, `CourseMaterialRepository`, `CourseEnrollmentRepository`
- `ClassroomEnrollmentRepository`, `ClassroomPostRepository`, `ClassroomLevelRepository`
- `StudentProgressRepository`, `QuizQuestionRepository`, `QuizAttemptAnswerRepository`
- `ExamQuestionRepository`, `ExamAttemptRepository`, `ExamAnswerRepository`
- `ProgramRepository`, `AcademicLevelRepository`, `SubjectRepository`
- `LibraryResourceRepository`, `CertificateRepository`

---

### 🧠 Couche Service (Business Logic Layer)

Les services contiennent la logique métier et orchestrent les interactions entre repositories.

#### 📁 `service.auth` - Authentification

##### `RegistrationService.java`
**Fonction:** Gestion de l'inscription utilisateur avec hashage de mot de passe (BCrypt), validation email unique, assignation de rôle par défaut (ETUDIANT).

#### 📁 `service.course` - Gestion des Cours

##### `CourseService.java`
**Fonction:** CRUD cours, publication (changement de statut), coordination indexation RAG, contrôle d'accès par rôle.  
**Méthodes clés:** `create()`, `update()`, `publish()`, `enrollStudent()`, `canAccess()`

##### `CourseChapterService.java`
**Fonction:** Gestion des chapitres avec upload de fichiers (PDF/PPTX), extraction de contenu, ordonnancement.  
**Méthodes clés:** `createChapter()`, `uploadFile()`, `reorderChapters()`

##### `CourseEnrollmentService.java`
**Fonction:** Gestion inscriptions étudiants, suivi de progression, marquage de complétion de chapitres.  
**Méthodes clés:** `enroll()`, `markChapterCompleted()`, `getProgress()`, `isCompleted()`

##### `TextExtractionService.java`
**Fonction:** Extraction de texte depuis PDF (Apache PDFBox) et PPTX (Apache POI) pour indexation RAG.  
**Méthodes clés:** `extractFromPdf()`, `extractFromPptx()`

#### 📁 `service.classroom` - Classes Virtuelles

##### `ClassroomService.java`
**Fonction:** Création de classes, génération de codes de jointure uniques (6 caractères alphanumériques), inscription étudiants, assignation de cours.  
**Méthodes clés:** `create()`, `generateCode()`, `enrollStudent()`, `assignCourse()`

##### `StudentProgressService.java`
**Fonction:** Calcul points XP, progression de niveaux, tracking d'achievements, statistiques globales de performance.  
**Méthodes clés:** `addXp()`, `updateLevel()`, `recordActivity()`, `getStatistics()`

#### 📁 `service.quiz` - Quiz IA

##### `QuizAgentService.java`
**Fonction:** Génération de quiz par IA depuis contenu de cours via RAG et Gemini API. Utilise recherche sémantique pour extraire contexte pertinent.  
**Méthodes clés:** `generateQuiz()`, `extractRelevantContext()`, `parseAiResponse()`

##### `QuizService.java`
**Fonction:** Gestion tentatives de quiz, soumission de réponses, notation automatique (QCM) et notation IA (questions ouvertes via Gemini).  
**Méthodes clés:** `startAttempt()`, `submitAnswer()`, `gradeAttempt()`, `gradeOpenEnded()`

#### 📁 `service.exam` - Examens

##### `ExamService.java`
**Fonction:** Création d'examens, planification, génération de questions IA, gestion tentatives, notation hybride (auto + IA).  
**Méthodes clés:** `createExam()`, `schedule()`, `generateQuestions()`, `submitAttempt()`, `grade()`

#### 📁 `service.institution` - Institutions

##### `InstitutionService.java`
**Fonction:** Gestion profils institutionnels, maintenance du catalogue académique (programmes/niveaux/matières).  
**Méthodes clés:** `getOrCreateByOwner()`, `updateProfile()`, `addProgram()`, `addLevel()`, `addSubject()`

##### `ApprovalService.java`
**Fonction:** Workflow d'approbation des demandes d'adhésion institutionnelle.  
**Méthodes clés:** `submitRequest()`, `approve()`, `reject()`

#### 📁 `service.library` - Bibliothèque

##### `LibraryService.java`
**Fonction:** Upload de ressources bibliothèque, extraction de texte OCR, catégorisation, recherche.  
**Méthodes clés:** `upload()`, `extractText()`, `search()`, `delete()`

#### 📁 `service.messaging` - Messagerie

##### `MessageService.java`
**Fonction:** Envoi de messages, threading, diffusion de messages de classe (broadcast).  
**Méthodes clés:** `send()`, `sendToClassroom()`, `markAsRead()`, `search()`

##### `NotificationService.java`
**Fonction:** Création de notifications, livraison, gestion du statut de lecture.  
**Méthodes clés:** `create()`, `notifyUser()`, `markAsRead()`, `markAllAsRead()`

#### 📁 `service.ai` - Intégration IA

##### `AiGateway.java` (interface)
**Fonction:** Abstraction pour interactions avec providers IA (permet changement de provider).  
**Méthodes:** `chat()`, `complete()`, `embed()`

##### `GeminiAiGateway.java`
**Fonction:** Implémentation Google Gemini API pour génération de texte et chat completions.  
**Configuration:** API key, modèle (gemini-1.5-flash), timeouts

##### `MockAiGateway.java`
**Fonction:** Implémentation mock pour tests sans appels API réels (retourne réponses prédéfinies).

##### `AiProvider.java` (enum)
**Fonction:** Providers IA supportés - GEMINI, MOCK

#### 📁 `service.rag` - RAG (Retrieval-Augmented Generation)

##### `RagIndexService.java`
**Fonction:** Chunking de contenu de cours, indexation TF-IDF, génération d'embeddings, recherche sémantique (similarité cosinus).  
**Méthodes clés:** `indexCourse()`, `chunkText()`, `calculateTfIdf()`, `searchSimilar()`

##### `GeminiEmbeddingService.java`
**Fonction:** Génération d'embeddings vectoriels (768 dimensions) via Gemini text-embedding-004.  
**Méthodes clés:** `embed()`, `embedBatch()`

##### `TextExtractor.java`
**Fonction:** Extraction de texte depuis multiples formats de documents pour traitement RAG.  
**Formats supportés:** PDF, PPTX, TXT, DOCX

#### 📁 `service.reviewbook` - Fiches de Révision

##### `ReviewBookService.java`
**Fonction:** Upload de fiches de révision, traitement OCR, extraction de texte.  
**Méthodes clés:** `upload()`, `processOcr()`, `extractText()`

##### `ReviewBookQuizService.java`
**Fonction:** Génération de quiz personnalisés par IA depuis contenu des fiches de révision.  
**Méthodes clés:** `generateQuiz()`, `createAttempt()`

#### 📁 `service.certificate` - Certificats

##### `CertificateService.java`
**Fonction:** Génération de certificats, création de codes uniques de vérification, rendu PDF.  
**Méthodes clés:** `generate()`, `generateCode()`, `verify()`, `renderPdf()`

#### 📁 `service.catalog` - Catalogue Académique

##### `CatalogService.java`
**Fonction:** Gestion du catalogue académique (programmes, niveaux, matières).  
**Méthodes clés:** `getPrograms()`, `getLevels()`, `getSubjects()`, `addProgram()`

---

### 🌐 Couche Web (Presentation Layer)

#### 📁 `web.controller` - Contrôleurs MVC

##### `AuthController.java`
**Routes:** `/login`, `/register`  
**Fonction:** Authentification, inscription, logout

##### `DashboardController.java`
**Routes:** `/`, `/dashboard`  
**Fonction:** Tableau de bord avec redirection basée sur le rôle (admin/prof/étudiant)

##### `ProfileController.java`
**Routes:** `/profile/*`  
**Fonction:** Affichage et édition du profil utilisateur

##### `AdminController.java`
**Routes:** `/admin/*`  
**Fonction:** Panneau d'administration, gestion utilisateurs, configuration système

##### `InstitutionController.java`
**Routes:** `/institution/*`  
**Fonction:** Gestion profil institutionnel, administration du catalogue académique

##### `MyInstitutionsController.java`
**Routes:** `/my-institutions/*`  
**Fonction:** Affiliations institutionnelles de l'utilisateur, bibliothèques partagées

##### `AffiliationController.java`
**Routes:** `/institution/affiliation/*`  
**Fonction:** Demandes d'affiliation institutionnelle et approbations

##### `ProfController.java`
**Routes:** `/prof/*`  
**Fonction:** Tableau de bord professeur, vue d'ensemble classes/cours

##### `ProfCourseController.java`
**Routes:** `/prof/courses/*`  
**Fonction:** Création de cours, édition, gestion de chapitres, upload de matériaux, indexation RAG

##### `ProfExamController.java`
**Routes:** `/prof/exam/*`  
**Fonction:** Création d'examens, planification, génération de questions IA, notation

##### `StudentController.java`
**Routes:** `/student/*`  
**Fonction:** Tableau de bord étudiant, cours inscrits, accès aux classes

##### `StudentCourseController.java`
**Routes:** `/student/course/*`  
**Fonction:** Affichage de cours, navigation dans les chapitres, inscription, suivi de progression

##### `StudentQuizController.java`
**Routes:** `/student/quiz/*`  
**Fonction:** Passage de quiz, soumission de réponses, affichage de résultats

##### `StudentExamController.java`
**Routes:** `/student/exam/*`  
**Fonction:** Passage d'examens, soumission, affichage de résultats

##### `LibraryController.java`
**Routes:** `/institution/library/*`  
**Fonction:** Navigation bibliothèque, uploads (gestionnaires institutionnels)

##### `MessageController.java`
**Routes:** `/messages/*`  
**Fonction:** Interface de messagerie, conversations, annonces de classe

##### `ReviewBookController.java`
**Routes:** `/student/reviewbook/*`  
**Fonction:** Upload de fiches, génération de quiz, suivi de progression

##### `StatsController.java`
**Routes:** `/stats/*`  
**Fonction:** Tableaux de bord statistiques (étudiants, professeurs, institutions)

##### `PublicController.java`
**Routes:** `/public/*`, `/`  
**Fonction:** Pages publiques, catalogue de cours

#### 📁 `web.api` - API REST

##### `ApiController.java`
**Routes:** `/api/*`  
**Fonction:** Endpoints REST pour intégrations mobiles/externes

#### 📁 `web.dto` - Data Transfer Objects

##### DTOs de Transfert
- `QuizAttemptDTO` - Données tentative de quiz avec scoring
- `ExamAttemptDTO` - Données tentative d'examen avec timing
- `MembershipRequestDTO` - Informations demande d'adhésion

##### Objets de Formulaires (`web.dto.forms`)
- `RegisterForm` - Validation inscription utilisateur
- `UserProfileForm` - Modification de profil
- `InstitutionProfileForm` - Édition profil institutionnel
- `AffiliationRequestForm` - Soumission demande d'affiliation
- `CourseCreateForm` / `CourseUpdateForm` - Création/édition cours
- `ClassroomCreateForm` - Création classe
- `JoinClassForm` - Jointure classe avec code
- `QuizStartForm` / `QuizSubmitForm` - Paramètres/soumission quiz
- `ProgramForm` / `LevelForm` / `SubjectForm` - Formulaires catalogue

#### 📁 `web.exception` - Gestion des Exceptions

##### `GlobalExceptionHandler.java`
**Fonction:** Gestion centralisée des exceptions, rendu de pages d'erreur, formatage des erreurs de validation

---

### 🔒 Couche Security

#### `SecurityConfig.java`
**Fonction:** Configuration Spring Security avec authentification par formulaire, contrôle d'accès basé sur les rôles, protection CSRF.  
**Routes protégées:**
- `/admin/**` → ADMIN
- `/prof/**` → PROF
- `/student/**` → ETUDIANT
- `/institution/**` → INSTITUTION_MANAGER

#### `UserDetailsServiceImpl.java`
**Fonction:** Service personnalisé UserDetailsService pour authentification Spring Security (chargement utilisateur depuis DB)

#### `UserDetailsImpl.java`
**Fonction:** Implémentation UserDetails encapsulant l'entité User pour Spring Security

#### `AuthSuccessHandler.java`
**Fonction:** Redirection post-login basée sur le rôle utilisateur (admin → /admin, prof → /prof, étudiant → /student)

---

### ⚙️ Couche Configuration

#### `AppProperties.java`
**Fonction:** Propriétés de configuration globales (chemins de stockage, paramètres RAG, configuration IA)  
**Annotation:** `@ConfigurationProperties(prefix="app")`

#### `AiProperties.java`
**Fonction:** Configuration provider IA (clés API Gemini, modèles, timeouts)  
**Annotation:** `@ConfigurationProperties(prefix="app.ai")`

#### `QuizProperties.java`
**Fonction:** Valeurs par défaut génération de quiz (seuil de réussite, nombre de questions)  
**Annotation:** `@ConfigurationProperties(prefix="app.quiz")`

#### `StorageConfig.java`
**Fonction:** Initialisation du stockage de fichiers et configuration des chemins (data/uploads/courses, data/uploads/library)

#### `WebMvcConfig.java`
**Fonction:** Personnalisation Spring MVC (gestionnaires de ressources, CORS, contrôleurs de vues)

#### `PgvectorType.java`
**Fonction:** Type Hibernate personnalisé pour support pgvector PostgreSQL (embeddings 768 dimensions)

---

### 🛠️ Couche Utilitaires

#### `SecurityUtil.java`
**Fonction:** Méthodes helper pour extraction de l'ID et nom utilisateur courant depuis le contexte Authentication de Spring Security  
**Méthodes:** `userId(Authentication)`, `userName(Authentication)`

---

## 🗃️ Migrations SQL (Flyway)

Les migrations Flyway gèrent l'évolution du schéma de base de données de manière versionnée.

### `V1__schema.sql`
**Objectif:** Schéma initial de la plateforme  
**Tables créées:** app_user, institution, institution_membership, program, academic_level, subject, classroom, classroom_enrollment, classroom_post  
**Extensions:** Aucune

### `V2__courses_rag.sql`
**Objectif:** Ajout du système de cours et RAG  
**Tables créées:** course, course_material, classroom_course, course_chunk  
**Fonctionnalités:** Support de contenu de cours avec indexation RAG pour génération de quiz IA

### `V3__quiz_ai_stats_seed.sql`
**Objectif:** Système de quiz IA  
**Tables créées:** quiz, quiz_question, quiz_attempt, quiz_attempt_answer  
**Colonnes ajoutées:** course.pass_threshold (défaut 70%)

### `V4__seed_admin.sql`
**Objectif:** Données de test pour développement  
**Données:** 4 utilisateurs test (admin/prof/étudiant/manager institution), institution "Université Ibn Tofail", programme "Génie Informatique", niveau L3, matières, classe "IA2025ML", cours ML

### `V5__extended_features.sql`
**Objectif:** Extension majeure de fonctionnalités  
**Tables créées (13):** classroom_level, student_progress, library_resource, exam + questions/attempts/answers, message, notification, course_enrollment, course_chapter, chapter_progress, certificate, activity_log, course_tag, favorite  
**Colonnes ajoutées:** Extensions massives à institution/user/classroom/course pour support complet de la plateforme

### `V6__course_chapter_columns.sql`
**Objectif:** Affinage structure des chapitres  
**Modifications:** Renommage chapter_index → chapter_order, estimated_minutes → duration_minutes  
**Ajouts:** content_type, content_path, text_content, video_url, is_published, updated_at

### `V7__entity_column_fixes.sql`
**Objectif:** Alignement colonnes DB avec entités Java  
**Modifications:** course_enrollment.completed → is_completed, last_accessed → last_accessed_at  
**Ajouts:** last_accessed_chapter_id, table enrollment_completed_chapters

### `V8__pgvector_embeddings.sql`
**Objectif:** Activation recherche sémantique vectorielle  
**Extensions:** pgvector  
**Colonnes ajoutées:** course_chunk.embedding (vector(768)), has_embedding  
**Index:** Index HNSW pour recherche rapide de similarité cosinus

### `V9__course_target_classroom.sql`
**Objectif:** Support publication de cours spécifique à une classe  
**Colonnes ajoutées:** course.target_classroom_id  
**Cas d'usage:** Cours réservés à une classe spécifique

### `V10__quiz_open_ended_support.sql`
**Objectif:** Ajout questions ouvertes aux quiz  
**Colonnes ajoutées à quiz_question:** question_type (MCQ/OPEN_ENDED), expected_answer, grading_criteria, max_points  
**Colonnes ajoutées à quiz_attempt_answer:** text_answer, ai_score, ai_feedback  
**Impact:** Support notation IA pour réponses textuelles

### `V11__reviewbook_table.sql`
**Objectif:** Fiches de révision personnelles pour étudiants  
**Tables créées:** review_book  
**Fonctionnalités:** Upload de documents (PDF/TXT/IMAGE), extraction OCR, génération de quiz personnalisés

### `V12__open_ended_questions.sql`
**Objectif:** Questions ouvertes pour examens (miroir de V10)  
**Colonnes ajoutées à exam_question:** question_type, expected_answer, grading_rubric  
**Colonnes ajoutées à exam_answer:** text_answer, ai_score, ai_feedback

**Total tables:** 40+ tables couvrant utilisateurs, institutions, cours, quiz, examens, messagerie, certificats, et fonctionnalités IA

---

## 🎨 Templates HTML (Thymeleaf)

### 📁 `templates/admin/` (3 fichiers)
- `dashboard.html` - Panneau de contrôle admin avec statistiques globales (utilisateurs, cours, institutions)
- `courses.html` - Interface de modération des cours avec recherche et tableau
- `users.html` - Gestion utilisateurs (affichage, édition, modération)

### 📁 `templates/auth/` (2 fichiers)
- `login.html` - Page d'authentification avec formulaire de connexion
- `register.html` - Formulaire d'inscription nouveaux utilisateurs

### 📁 `templates/course/` (1 fichier)
- `view.html` - Affichage détaillé d'un cours (titre, description, contenu, bouton quiz)

### 📁 `templates/error/` (3 fichiers)
- `403.html` - Page d'erreur accès refusé
- `404.html` - Page non trouvée
- `500.html` - Erreur serveur interne

### 📁 `templates/institution/` (11 fichiers)
- `dashboard.html` - Tableau de bord institution (statistiques programmes, classes, membres, bibliothèque)
- `profile.html` - Profil institutionnel (informations, paramètres)
- `structure.html` - Gestion structure académique (programmes, départements, niveaux)
- `members.html` - Gestion des membres et attribution de rôles
- `approvals.html` - Interface d'approbation des demandes d'affiliation
- `classrooms.html` - Liste des classes de l'institution
- `classroom_create.html` - Formulaire création de classe
- `classroom_view.html` - Vue détaillée classe (étudiants, cours)
- `library.html` - Bibliothèque institutionnelle (ressources partagées, recherche/filtres)
- `library_upload.html` - Formulaire upload de ressources
- `library_resource_view.html` - Vue détaillée d'une ressource

### 📁 `templates/layout/` (3 fichiers)
- `base.html` - Template de base avec structure HTML, includes CSS/JS, fragments Thymeleaf
- `navbar.html` - Barre de navigation supérieure (menu utilisateur, liens par rôle)
- `sidebar.html` - Menu latéral de navigation (adapté au rôle)

### 📁 `templates/messages/` (6 fichiers)
- `inbox.html` - Boîte de réception (messages reçus avec compteur non lus)
- `sent.html` - Messages envoyés
- `compose.html` - Formulaire de composition de message
- `view.html` - Lecture d'un message individuel
- `notifications.html` - Liste des notifications système
- `search.html` - Interface de recherche de messages

### 📁 `templates/my-institutions/` (2 fichiers)
- `list.html` - Institutions auxquelles l'utilisateur est affilié
- `library.html` - Vue agrégée des bibliothèques des institutions de l'utilisateur

### 📁 `templates/prof/` (9 fichiers)
- `dashboard.html` - Tableau de bord professeur (cours créés, classes, examens, étudiants)
- `courses.html` - Liste des cours du professeur (création, gestion)
- `course_edit.html` - Formulaire d'édition de cours (détails, contenu)
- `classrooms.html` - Classes du professeur
- `classroom_view.html` - Vue détaillée classe (étudiants inscrits, cours assignés)
- `exams.html` - Liste des examens créés
- `exam_create.html` - Création d'examen (QCM/ouvert, génération IA optionnelle)
- `exam_edit.html` - Édition d'examen
- `exam_view.html` - Détails examen (questions, soumissions étudiants)

### 📁 `templates/profile/` (2 fichiers)
- `view.html` - Affichage du profil utilisateur
- `affiliation_request.html` - Formulaire demande d'affiliation institutionnelle

### 📁 `templates/public/` (2 fichiers)
- `home.html` - Page d'accueil publique (hero section, introduction plateforme)
- `catalog.html` - Catalogue public de cours

### 📁 `templates/stats/` (2 fichiers)
- `prof_course.html` - Statistiques de cours pour professeurs (tentatives quiz, scores moyens, performance)
- `admin.html` - Analytics et statistiques globales de la plateforme (pour admins)

### 📁 `templates/student/` (11 fichiers + sous-dossier reviewbook)
- `dashboard.html` - Tableau de bord étudiant (cours inscrits, quiz complétés, classes, examens à venir)
- `courses.html` - Liste des cours disponibles
- `classes.html` - Classes auxquelles l'étudiant est inscrit
- `classroom_view.html` - Vue détaillée classe (cours, camarades)
- `profile.html` - Profil étudiant
- `quiz_start.html` - Page d'initialisation quiz (sélection difficulté, nombre de questions)
- `quiz_take.html` - Interface de passage de quiz (QCM)
- `quiz_result.html` - Résultats de quiz (score, réponses correctes)
- `exam_view.html` - Informations et démarrage d'examen
- `exam_take.html` - Interface de passage d'examen (QCM et/ou questions ouvertes)
- `exam_result.html` - Résultats et feedback d'examen

### 📁 `templates/student/reviewbook/` (5 fichiers)
- `list.html` - Bibliothèque de documents de révision personnels
- `upload.html` - Formulaire upload de documents
- `generate.html` - Interface de génération de documents via IA
- `view.html` - Affichage d'un document de révision
- `quiz.html` - Interface de quiz basé sur le contenu de la fiche

**Total:** 62 templates HTML organisés en 13 répertoires, supportant multiples rôles (Admin, Professeur, Étudiant, Gestionnaire Institution) avec interfaces distinctes pour gestion de cours, évaluations, messagerie, bibliothèques et outils d'étude IA.

---

## 📜 Fichiers JavaScript

### `static/js/app.js`
**Fonction:** JavaScript applicatif principal pour l'interface utilisateur  
**Fonctionnalités:**
- Auto-dismiss des alertes Bootstrap après 5 secondes
- Smooth scroll pour liens d'ancrage (avec exclusion des composants Bootstrap - tabs, modals, accordions, etc.)
- Initialisation globale au chargement du DOM
- Logging de démarrage console

**Note:** L'application utilise principalement Bootstrap 5 pour l'interactivité côté client. Ce fichier fournit des améliorations UX supplémentaires.

---

## 📦 Fichiers de Configuration

### `pom.xml`
**Type:** Maven Project Object Model  
**Fonction:** Définition des dépendances, plugins de build, configuration du projet  
**Dépendances clés:**
- Spring Boot Starters (web, data-jpa, security, thymeleaf, validation, actuator)
- PostgreSQL driver
- Flyway (migrations DB)
- Apache PDFBox 3.0.1 (traitement PDF)
- Apache POI 5.2.5 (traitement PPTX)
- Jackson (traitement JSON)

### `application.yml` / `application-dev.yml` / `application-prod.yml`
**Type:** Configuration Spring Boot (YAML)  
**Fonction:** Configuration applicative par environnement
- **application.yml** - Configuration de base
- **application-dev.yml** - Profil développement (logging verbose, H2 console, etc.)
- **application-prod.yml** - Profil production (optimisations, sécurité renforcée)

**Propriétés typiques:**
- Connexion PostgreSQL (URL, credentials)
- Configuration Flyway
- Paramètres JPA/Hibernate
- Paramètres serveur (port, context-path)
- Configuration de stockage de fichiers
- API keys (Gemini)

### `docker-compose.yml`
**Type:** Docker Compose  
**Fonction:** Orchestration des conteneurs pour développement local  
**Services:**
- PostgreSQL avec extension pgvector
- Potentiellement pgAdmin pour administration DB

### `mvnw` / `mvnw.cmd`
**Type:** Maven Wrapper  
**Fonction:** Scripts permettant d'exécuter Maven sans installation globale (Linux/Mac et Windows)



---

## 🚀 Résumé de l'Architecture

### Organisation en Couches (Top-Down)
1. **Web** (Controllers, DTOs) → Gère les requêtes HTTP, validation, rendu de vues
2. **Service** (Business Logic) → Logique métier, orchestration, transactions
3. **Repository** (DAL) → Accès aux données, requêtes JPA
4. **Domain** (Entities) → Modèle de données, règles métier de base
5. **Security** → Authentification, autorisation, contrôle d'accès
6. **Configuration** → Paramétrage applicatif, beans Spring

### Flux Typique d'une Requête
```
User Request (Browser)
  ↓
Controller (@GetMapping/@PostMapping)
  ↓
Service (@Transactional)
  ↓
Repository (JPA)
  ↓
Database (PostgreSQL)
  ↓
Response (Thymeleaf Template + Model)
  ↓
User (HTML Rendered)
```

### Fonctionnalités Clés
- ✅ Système multi-rôles (Admin, Professeur, Étudiant, Gestionnaire Institution)
- ✅ Génération de quiz IA via RAG (Retrieval-Augmented Generation)
- ✅ Notation hybride (automatique QCM + IA pour questions ouvertes)
- ✅ Gamification (points XP, niveaux, achievements)
- ✅ Support multi-tenant (institutions avec catalogues académiques)
- ✅ Bibliothèques institutionnelles avec OCR
- ✅ Fiches de révision personnelles avec génération de quiz IA
- ✅ Publication flexible de cours (public, brouillon, spécifique à une classe)
- ✅ Messagerie complète (messages directs, annonces de classe, notifications)
- ✅ Recherche sémantique vectorielle (pgvector + embeddings Gemini 768D)

---

## 🧠 Comprendre les Technologies Clés

### 🐳 Docker - Le "Conteneur" d'Applications

#### C'est quoi ?
Docker est comme une **boîte isolée** qui contient tout ce qu'il faut pour exécuter un logiciel (PostgreSQL dans notre cas), sans l'installer directement sur votre PC.

#### Pourquoi l'utiliser ?
```
SANS Docker:
- Télécharger PostgreSQL
- L'installer manuellement
- Configurer les chemins, ports, utilisateurs...
- Risque de conflits avec d'autres versions
- Difficile à désinstaller proprement

AVEC Docker:
- Une seule commande: docker-compose up -d
- PostgreSQL tourne dans une "bulle" isolée
- Facile à supprimer: docker-compose down
- Même configuration pour tout le monde
```

#### Analogie 🍱
Docker = **boîte bento japonaise**. Tout est pré-emballé et prêt à consommer, sans avoir à cuisiner vous-même.

---

### 🐘 PostgreSQL - La Base de Données

#### C'est quoi ?
PostgreSQL est une **base de données relationnelle** qui stocke toutes les données de l'application:

```
┌─────────────────────────────────────────────────────┐
│                    PostgreSQL                        │
├─────────────────────────────────────────────────────┤
│  Tables:                                            │
│  • app_user (utilisateurs)                          │
│  • course (cours)                                   │
│  • quiz, quiz_question (quiz)                       │
│  • classroom (classes)                              │
│  • message, notification (messagerie)              │
│  • course_chunk (morceaux de texte pour IA) ← RAG! │
│  • ... (40+ tables)                                 │
└─────────────────────────────────────────────────────┘
```

#### Dans le docker-compose:
```yaml
postgres:
  image: pgvector/pgvector:pg16  # PostgreSQL 16 + extension pgvector
  ports:
    - "5433:5432"  # Port 5433 sur votre PC → Port 5432 dans Docker
```

---

### 🧮 pgvector - L'Extension pour Vecteurs

#### C'est quoi ?
pgvector est une **extension PostgreSQL** qui permet de stocker et rechercher des **vecteurs** (tableaux de nombres).

#### Pourquoi des vecteurs ?
C'est là que ça devient intéressant pour l'IA ! 

```
Texte normal:     "Le machine learning est une branche de l'IA"
                           ↓
                    [Transformation par IA]
                           ↓
Vecteur (embedding): [0.023, -0.156, 0.892, 0.045, ..., 0.331]
                     └──────────── 768 nombres ─────────────┘
```

#### À quoi ça sert ?
Les vecteurs permettent de **comparer des textes par similarité sémantique** :

```
Question étudiant: "Comment fonctionne l'apprentissage automatique ?"

Le cours contient:
  Chunk 1: "Le machine learning permet aux ordinateurs d'apprendre"
  Chunk 2: "L'histoire des bases de données relationnelles"
  Chunk 3: "Les réseaux de neurones sont inspirés du cerveau"

pgvector calcule la similarité:
  Chunk 1: 0.92 (très similaire!) ✅
  Chunk 2: 0.23 (pas du tout)
  Chunk 3: 0.78 (assez similaire)

→ On utilise Chunk 1 et 3 pour générer le quiz!
```

---

### 🔢 Embeddings - La Magie de l'IA

#### C'est quoi ?
Un **embedding** est la transformation d'un texte en vecteur de nombres. Cette transformation capture le **sens** du texte.

#### Comment ça marche dans EduForge ?

```
┌─────────────────────────────────────────────────────────────────┐
│                    PROCESSUS D'INDEXATION                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. Professeur uploade un PDF de cours                          │
│                    ↓                                            │
│  2. EduForge extrait le texte (Apache PDFBox)                   │
│                    ↓                                            │
│  3. Le texte est découpé en "chunks" (morceaux de ~900 chars)   │
│     "Le machine learning..."  "Les réseaux de neurones..."      │
│                    ↓                                            │
│  4. Chaque chunk est envoyé à Gemini API                        │
│     pour générer un embedding (768 dimensions)                  │
│                    ↓                                            │
│  5. Les embeddings sont stockés dans PostgreSQL + pgvector      │
│     Table: course_chunk                                         │
│     ┌─────────┬────────────────┬─────────────────────────┐     │
│     │ id      │ text           │ embedding               │     │
│     ├─────────┼────────────────┼─────────────────────────┤     │
│     │ 1       │ "Le ML..."     │ [0.02, -0.15, 0.89...]  │     │
│     │ 2       │ "Les RN..."    │ [0.11, 0.45, -0.32...]  │     │
│     └─────────┴────────────────┴─────────────────────────┘     │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

#### Lors de la génération de quiz (RAG):

```
┌─────────────────────────────────────────────────────────────────┐
│                    GÉNÉRATION DE QUIZ (RAG)                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. Étudiant demande un quiz sur le cours                       │
│                    ↓                                            │
│  2. EduForge prend le titre/sujet du cours                      │
│     "Introduction au Machine Learning"                          │
│                    ↓                                            │
│  3. Génère un embedding de cette requête                        │
│     → [0.05, -0.12, 0.91, ...]                                  │
│                    ↓                                            │
│  4. pgvector cherche les chunks les plus similaires             │
│     SELECT * FROM course_chunk                                  │
│     ORDER BY embedding <=> query_embedding  -- Similarité cosinus│
│     LIMIT 5;                                                    │
│                    ↓                                            │
│  5. Les 5 meilleurs chunks sont envoyés à Gemini                │
│     avec un prompt: "Génère 6 questions QCM basées sur ce texte"│
│                    ↓                                            │
│  6. Gemini retourne les questions, EduForge les stocke          │
│                    ↓                                            │
│  7. L'étudiant voit le quiz!                                    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

### 🔄 Résumé du Flux Complet

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   PROFESSEUR │    │   EDUFORGE   │    │   GEMINI API │
└──────┬───────┘    └──────┬───────┘    └──────┬───────┘
       │                   │                   │
       │ Upload PDF        │                   │
       │──────────────────>│                   │
       │                   │                   │
       │                   │ Extrait texte     │
       │                   │ Découpe en chunks │
       │                   │                   │
       │                   │ Envoie chunks     │
       │                   │──────────────────>│
       │                   │                   │
       │                   │   Embeddings      │
       │                   │<──────────────────│
       │                   │                   │
       │                   │ Stocke dans       │
       │                   │ PostgreSQL+pgvector
       │                   │                   │
       │  Cours indexé ✅  │                   │
       │<──────────────────│                   │
       │                   │                   │

┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   ÉTUDIANT   │    │   EDUFORGE   │    │   GEMINI API │
└──────┬───────┘    └──────┬───────┘    └──────┬───────┘
       │                   │                   │
       │ Demande quiz      │                   │
       │──────────────────>│                   │
       │                   │                   │
       │                   │ Cherche chunks    │
       │                   │ similaires (pgvector)
       │                   │                   │
       │                   │ Envoie contexte + │
       │                   │ "Génère quiz"     │
       │                   │──────────────────>│
       │                   │                   │
       │                   │   Questions QCM   │
       │                   │<──────────────────│
       │                   │                   │
       │   Quiz prêt! 🎯   │                   │
       │<──────────────────│                   │
```

---

### 📊 Pourquoi c'est puissant ?

| Approche Traditionnelle | Approche RAG (EduForge) |
|------------------------|--------------------------|
| Questions écrites manuellement | Questions générées par IA |
| Recherche par mots-clés exacts | Recherche par **sens** sémantique |
| "machine learning" ≠ "apprentissage automatique" | "machine learning" ≈ "apprentissage automatique" ✅ |
| Limité au vocabulaire exact | Comprend les synonymes, reformulations |

---

### 🎯 En une phrase

> **Docker** fait tourner **PostgreSQL** qui, grâce à **pgvector**, peut stocker des **embeddings** (vecteurs générés par Gemini) pour permettre une **recherche sémantique intelligente** et générer des quiz pertinents basés sur le contenu réel des cours.

C'est ce qu'on appelle **RAG** (Retrieval-Augmented Generation) - l'IA génère du contenu en se basant sur vos propres documents plutôt que sur ses connaissances générales !

---

## 📞 Contact & Support

Pour toute question sur l'architecture ou les composants, référez-vous à cette documentation ou consultez les JavaDocs des classes individuelles.

**Version:** 0.0.1-SNAPSHOT  
**Java:** 21  
**Spring Boot:** 3.4.0  
**Database:** PostgreSQL 15+ (avec pgvector)

---

*Documentation - EduForge Platform*
