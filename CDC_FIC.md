Cahier des Charges
FIC – Freelance In Connect
1. Présentation du projet

FIC (Freelance In Connect) est une plateforme web B2B spécialisée dans la sous-traitance de projets informatiques. Elle met en relation les entreprises de services numériques (ESN) et les freelances informatiques afin de faciliter la recherche de compétences, la gestion des missions et la collaboration professionnelle.

Contrairement aux plateformes généralistes telles que LinkedIn, FIC est entièrement dédiée au secteur informatique et à la sous-traitance de projets IT.

2. Objectifs du projet

La plateforme vise à :

Faciliter la mise en relation entre entreprises et freelances.
Simplifier la publication et la gestion des missions.
Centraliser les candidatures.
Permettre une communication directe via une messagerie intégrée.
Sécuriser les échanges grâce à une architecture moderne basée sur des microservices.
Intégrer un système de paiement sécurisé via Stripe.
3. Architecture Technique

La plateforme repose sur une architecture microservices composée des éléments suivants :

Frontend
React.js
API Gateway
WSO2 API Manager
Gestion des identités et accès
Keycloak IAM
JWT Access Token
JWT Refresh Token
OTP (One-Time Password)
Backend
Spring Boot
Spring Security
Spring Data JPA
Bases de données
PostgreSQL
MongoDB
Conteneurisation
Docker
Docker Compose
4. Microservices
   Microservice	Description
   auth-service	Authentification, OTP et gestion des tokens
   freelancer-service	Gestion des freelances
   company-service	Gestion des entreprises
   mission-service	Gestion des missions
   application-service	Gestion des candidatures
   messaging-service	Gestion des conversations
   media-service	Gestion des CV et fichiers
   payment-service	Paiements Stripe
   admin-service	Administration de la plateforme
5. Rôles Utilisateurs
   ADMIN

Administrateur unique de la plateforme.

COMPANY

Entreprise cliente publiant des missions.

FREELANCER

Prestataire informatique candidatant aux missions.

6. Processus d'Authentification
   Connexion
   L'utilisateur saisit son email et son mot de passe.
   Les informations sont vérifiées.
   Un code OTP est envoyé par email.
   L'utilisateur saisit le code OTP.
   Après validation :
   Access Token généré.
   Refresh Token généré.
   Rôle utilisateur transmis au frontend.
   Mot de passe oublié
   Génération d'un OTP.
   Vérification du code.
   Définition d'un nouveau mot de passe.
7. Gestion des Entreprises

Lors de l'inscription :

Statut initial : PENDING

L'administrateur peut modifier ce statut :

PENDING
VALIDATED
REJECTED
SUSPENDED

Une entreprise dont le statut est PENDING, REJECTED ou SUSPENDED ne peut pas se connecter.

8. Fonctionnalités Freelancer
   Profil
   Consultation du profil
   Modification des informations
   Gestion du CV (PDF)
   Gestion de la photo de profil
   Configuration du compte Stripe
   Missions
   Consultation des missions
   Recherche multicritère
   Filtres par statut
   Consultation des détails
   Candidature
   Messagerie
   Envoi de messages
   Réception de messages
   Recherche de conversations
   Filtres temporels
   Notifications
   Réception en temps réel des nouvelles missions
9. Fonctionnalités Entreprise
   Profil
   Gestion des informations de l'entreprise
   Gestion du logo
   Missions
   Création
   Modification
   Suppression
   Clôture

Statuts :

PUBLISHED
STARTED
CLOSED
Candidatures
Consultation des candidatures
Validation ou refus
Paiement
Paiement Stripe
Calcul automatique de la commission plateforme
10. Fonctionnalités Administrateur
    Tableau de bord

Affichage des statistiques :

Nombre total d'entreprises
Nombre total de freelances
Nombre total de missions
Nombre total de candidatures
Entreprises en attente
Entreprises suspendues
Entreprises rejetées
Gestion des entreprises
Validation
Suspension
Rejet
Suppression
Gestion des freelances
Consultation
Suppression
Audit
Historique des actions administratives
11. Contraintes Non Fonctionnelles
    Sécurité
    JWT
    OTP
    Spring Security
    Keycloak IAM
    Disponibilité
    Architecture microservices
    Déploiement conteneurisé
    Performance
    API Gateway centralisée
    Communication sécurisée entre services
    Traçabilité
    Journalisation des actions administratives

Cette version serait beaucoup plus professionnelle pour un PFE, tout en restant fidèle à ton projet actuel. Elle est aussi plus facile à transformer ensuite en diagrammes UML, cas d'utilisation et backlog fonctionnel.