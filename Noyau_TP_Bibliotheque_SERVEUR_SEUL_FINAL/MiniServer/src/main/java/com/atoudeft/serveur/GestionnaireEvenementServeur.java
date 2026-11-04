/*
 * ================================================================
 * CODE FOURNI - INF111
 * Noyau client-serveur
 *
 * Auteur  : Professeur Abdelmoumene  Toudeft
 * Documente  par  : Asma Bellili
 *
 *
 * Ce fichier fait partie du noyau client-serveur fourni aux etudiants.
 * Le code de communication est deja fonctionnel et ne doit pas etre
 * modifie, sauf indication contraire dans l'enonce du travail pratique.
 * ================================================================
 */

package com.atoudeft.serveur;

import com.atoudeft.commun.evenement.Evenement;
import com.atoudeft.commun.evenement.GestionnaireEvenement;
import com.atoudeft.commun.net.Connexion;
import gestionnaireBibliotheque.GestionnaireBibliotheque; // a ajuster selon le nom exact de votre package
import gestionnaireBibliotheque.Livre;

/**
 * Cette classe represente un gestionnaire d'evenement d'un serveur. Lorsqu'un serveur recoit un texte d'un client,
 * il cree un evenement a partir du texte recu et alerte ce gestionnaire qui reagit en gerant l'evenement.
 *
 * @author Abdelmoumene Toudeft (Abdelmoumene.Toudeft@etsmtl.ca)
 * @version 1.0
 * @since 2023-09-01
 */
public class GestionnaireEvenementServeur implements GestionnaireEvenement {
    private Serveur serveur;

    /**
     * Gestionnaire du systeme de bibliotheque utilise pour traiter
     * les commandes recues des clients.
     */
    private GestionnaireBibliotheque gestionnaireBibliotheque;

    /**
     * Construit un gestionnaire d'evenements pour un serveur.
     *
     * @param serveur Serveur Le serveur pour lequel ce gestionnaire gere des evenements
     */
    public GestionnaireEvenementServeur(Serveur serveur) {
        this.serveur = serveur;
        this.gestionnaireBibliotheque = new GestionnaireBibliotheque();
        // 1. Pré-chargement
        gestionnaireBibliotheque.traiterCommande(0, "REGISTER", "101 ETUDIANT nomEtudiant");
        gestionnaireBibliotheque.traiterCommande(0, "REGISTER", "102 PROFESSEUR nomProfesseur");
        gestionnaireBibliotheque.traiterCommande(0, "REGISTER", "103 PERSONNEL nomPersonnel");

        Livre livre1 = new Livre("livre1", "auteur1", "categorie1");
        Livre livre2 = new Livre("livre2", "auteur2", "categorie2");
        Livre livre3 = new Livre("livre3", "auteur3", "categorie3");

        gestionnaireBibliotheque.getLivres().ajouter(livre1);
        gestionnaireBibliotheque.getLivres().ajouter(livre2);
        gestionnaireBibliotheque.getLivres().ajouter(livre3);
    }

    /**
     * Methode de gestion d'evenements. Cette methode contiendra le code qui gere les reponses obtenues d'un client.
     *
     * @param evenement L'evenement a gerer.
     */
    @Override
    public void traiter(Evenement evenement) {
        Object source = evenement.getSource();
        Connexion cnx;
        String reponse;

        if (source instanceof Connexion) {
            cnx = (Connexion) source;
            System.out.println("SERVEUR: Recu : " + evenement.getType() + " " + evenement.getArgument());
            if ("EXIT".equals(evenement.getType())) {

                cnx.envoyer("END.");
                cnx.close();
            } else {
                /*
                 * La commande recue (type + argument) est transmise au
                 * GestionnaireBibliotheque, qui retourne la reponse a
                 * envoyer au client (AUTHORIZED, BORROW_OK, RETURN_OK, etc.).
                 */
                String commande = evenement.getType();
                if (evenement.getArgument() != null && !evenement.getArgument().isEmpty()) {
                    commande += " " + evenement.getArgument();
                }
                int idConnexion = cnx.hashCode();
                reponse = gestionnaireBibliotheque.traiterCommande(idConnexion, evenement.getType(), evenement.getArgument());
                cnx.envoyer(reponse);
            }
        }
    }
}