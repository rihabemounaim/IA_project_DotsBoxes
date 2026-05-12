# Pipopipette AI — Dots & Boxes

Projet de **L3 Informatique — IA Symbolique (2025/2026)**  
Université Paris-Saclay — N. Lazaar

Implémentation du jeu **Pipopipette (Dots and Boxes)** avec plusieurs stratégies d'intelligence artificielle :

- automate aléatoire,
- IA gloutonne,
- Minimax profondeur limitée,
- Alpha-Beta pruning,
- expérimentations stratégiques et heuristiques avancées.

---

# Table des matières

- [Présentation](#présentation)
- [Objectifs pédagogiques](#objectifs-pédagogiques)
- [Règles du jeu](#règles-du-jeu)
- [Architecture du projet](#architecture-du-projet)
- [Technologies utilisées](#technologies-utilisées)
- [Installation](#installation)
- [Compilation](#compilation)
- [Lancement du projet](#lancement-du-projet)
- [Tests unitaires](#tests-unitaires)
- [Génération de la Javadoc](#génération-de-la-javadoc)
- [Stratégies IA implémentées](#stratégies-ia-implémentées)
- [Minimax et Alpha-Beta](#minimax-et-alpha-beta)
- [Analyse stratégique du jeu](#analyse-stratégique-du-jeu)
- [Structure du projet](#structure-du-projet)
- [Approche TDD](#approche-tdd)
- [Axes d'amélioration](#axes-damélioration)
- [Commandes Maven utiles](#commandes-maven-utiles)
- [Auteurs](#auteurs)

---

# Présentation

Le projet consiste à développer un moteur de jeu complet pour **Pipopipette**, ainsi qu'un ensemble d'agents intelligents capables de jouer automatiquement.

Le jeu est un environnement classique d'**IA adversariale à information parfaite**. Il met en évidence plusieurs problématiques fondamentales :

- recherche dans les arbres de jeu,
- optimisation sous contraintes,
- heuristiques d'évaluation,
- anticipation stratégique,
- gestion d'un jeu non strictement alterné.

Le projet suit une approche progressive :

1. compréhension du moteur de jeu,
2. implémentation d'agents simples,
3. développement de Minimax,
4. optimisation avec Alpha-Beta,
5. conception d'un agent expert.

---

# Objectifs pédagogiques

Ce projet permet de manipuler concrètement :

- la modélisation formelle d'un jeu,
- les algorithmes de recherche adversariale,
- les heuristiques d'évaluation,
- les arbres de décision,
- le développement piloté par les tests (TDD),
- l'analyse expérimentale des performances IA.

---

# Règles du jeu

## Plateau

Le plateau est constitué d'une grille de points `N × M`.

Les joueurs peuvent tracer des segments horizontalement ou verticalement, uniquement entre points adjacents.

Chaque case fermée rapporte `+1 point` et un tour supplémentaire au joueur ayant fermé la case.

## Fin de partie

La partie se termine lorsque tous les segments ont été tracés. Le gagnant est le joueur possédant le plus de cases.

## Coup invalide

Un coup est invalide si le segment existe déjà, si les points ne sont pas adjacents, ou si le coup sort du plateau. Une pénalité est alors appliquée :

```text
score -= nombre_de_lignes
```

---

# Architecture du projet

| Composant           | Rôle                            |
| ------------------- | ------------------------------- |
| `Board`             | Représentation de l'état du jeu |
| `Action`            | Modélisation d'un coup          |
| `Referee`           | Validation des règles           |
| `DotsBoxesGame`     | Boucle principale du jeu        |
| `Strategy`          | Interface des agents IA         |
| `MinimaxStrategy`   | Recherche adversariale          |
| `AlphaBetaStrategy` | Optimisation du Minimax         |

L'état du jeu contient : segments horizontaux, segments verticaux, propriétaires des cases, joueur actif et score courant.

---

# Technologies utilisées

- Java 17
- Maven
- JUnit 5
- Swing (interface graphique)
- Javadoc

---

# Installation

## Prérequis

```bash
java -version
mvn -version
```

- Java 17+
- Maven 3.8+
- IntelliJ IDEA / VS Code / Eclipse

---

# Compilation

```bash
mvn clean compile
```

Sans tests :

```bash
mvn -DskipTests compile
```

---

# Lancement du projet

## Interface texte

```text
DotsBoxes.DotsBoxesGame
```

## Interface graphique Swing

```text
DotsBoxes.ui.DotsBoxesSwingUI
```

---

# Tests unitaires

## Lancer tous les tests

```bash
mvn test
```

## Lancer un test spécifique

```bash
mvn -Dtest=NomDeLaClasseTest test
```

Exemple :

```bash
mvn -Dtest=MinimaxActionStrategyTest test
```

---

# Génération de la Javadoc

```bash
mvn javadoc:javadoc
```

Disponible dans :

```text
target/site/apidocs/index.html
```

---

# Stratégies IA implémentées

## 1. IA Aléatoire

Choisit un segment valide au hasard parmi les coups possibles.

**Avantages** : très rapide, simple à implémenter.  
**Limites** : aucune anticipation, ouvre souvent des chaînes dangereuses.

## 2. IA Gloutonne

Ferme immédiatement une case lorsqu'elle le peut.

**Avantages** : meilleure que l'aléatoire, exploite les gains immédiats.  
**Limites** : vulnérable aux chaînes, crée fréquemment des cases à trois côtés.

## 3. IA Minimax

Recherche adversariale profondeur limitée. L'algorithme maximise le score du joueur courant et minimise celui de l'adversaire en explorant récursivement l'arbre de jeu.

> Pipopipette n'est pas strictement alterné : un joueur peut rejouer après fermeture d'une case. Le Minimax doit donc gérer des tours consécutifs et des changements conditionnels de joueur.

## 4. IA Alpha-Beta

Optimisation du Minimax via élagage. Objectif : réduire le nombre de nœuds explorés, atteindre des profondeurs plus élevées, tout en conservant exactement les mêmes décisions.

---

# Minimax et Alpha-Beta

## Fonction d'évaluation heuristique

Exemples de critères possibles :

- différence de score,
- nombre de cases à trois côtés,
- chaînes disponibles,
- mobilité restante,
- contrôle du centre.

## Complexité

Facteur de branchement initial :

```text
B = N(M-1) + M(N-1)
```

| Grille | Segments initiaux |
| ------ | ----------------- |
| 3×3    | 12                |
| 4×4    | 24                |
| 5×5    | 40                |

---

# Analyse stratégique du jeu

## Cases à trois côtés

Situation dangereuse : le prochain joueur ferme automatiquement la case.

## Chaînes de cases

Suite de cases connectées permettant des captures multiples et des retournements de partie.

## Sacrifice stratégique

Un bon joueur peut volontairement abandonner une petite chaîne pour contrôler la fin de partie et forcer une chaîne plus importante ensuite.

---

# Structure du projet

```text
src/
├── main/
│   └── java/
│       └── ...
│
├── test/
│   └── java/
│       └── ...
│
pom.xml
README.md
```

---

# Approche TDD

Cycle recommandé :

1. écrire un test,
2. lancer le test,
3. implémenter le minimum,
4. valider,
5. refactoriser.

Objectif : limiter les régressions et garantir la stabilité du moteur.

---

# Axes d'amélioration

- Monte-Carlo Tree Search (MCTS),
- pattern mining,
- apprentissage par auto-play,
- heuristiques adaptatives,
- détection automatique des chaînes,
- IA hybride recherche + apprentissage.

---

# Commandes Maven utiles

```bash
mvn clean compile   # Compiler
mvn test            # Tests
mvn clean           # Nettoyage
mvn javadoc:javadoc # Javadoc
```

---

# Auteurs

**Groupe WarSquare** — L3 MIAGE, Université Paris-Saclay

| Étudiant | Nom |
| -------- | --- |
| Étudiant 1 | MOUNAIM Rihabe |
| Étudiant 2 | OUAIL ZOUINA |
| Étudiant 3 | AGWANIHU CHIBUAKOM Paul Daniel |
| Étudiant 4 | HINDA BEDDERI Monir |

---

# Références

- Projet IA Symbolique — Université Paris-Saclay
- Documentation initiale Maven du projet
