/*
 * ================================================================
 * CODE FOURNI - INF111
 * Noyau client-serveur
 *
 * Auteur  : Professeur Abdelmoumene  Toudeft
 *
 *
 * Ce fichier fait partie du noyau client-serveur fourni aux etudiants.
 * Le code de communication est deja fonctionnel et ne doit pas etre
 * modifie.
 * ================================================================
 */

package com.demo;

import com.atoudeft.client.Client;

import java.util.Scanner;

/**
 * Programme simple de démonstration d'un client. Le programme utilise un client pour se connecter à un serveur
 * et lui envoie les textes saisis par l'utilisateur jusqu'à ce que celui-ci saisisse le texte EXIT.
 *
 * @author Abdelmoumène Toudeft (Abdelmoumene.Toudeft@etsmtl.ca)
 * @version 1.0
 * @since 2023-09-01
 */
public class Main {

	/**
	 * Méthode principale du programme.
	 *
	 * @param args Arguments du programme
	 */
	public static void main(String[] args) {

		Scanner clavier = new Scanner(System.in);
		Client client = new Client();
		String saisie;

		if (!client.connecter()) {
			System.out.println("Serveur introuvable a l'adresse " + client.getAdrServeur()
					+ " sur le port " + client.getPortServeur());
			return;
		}
		System.out.println("Vous etes connectes au serveur à l'adresse " + client.getAdrServeur()
				+ " sur le port " + client.getPortServeur());
		System.out.println("Le serveur vous renvoie votre texte converti en majuscules");

		System.out.println("=== Console Bibliothèque prête ===");
		System.out.println("Exemples de commandes :");
		System.out.println("  REGISTER 12 ETUDIANT Leo ");
		System.out.println("  ID 12 ");
		System.out.println("  BOOKS ");
		System.out.println("  BORROW 1 5 ");
		System.out.println("  INFO ");
		System.out.println("  INFO 1 ");
		System.out.println("  RETURN 1 45 ");
		System.out.println("  RESERVE 1 ");
		System.out.println("  PENALTY 1 50 ");
		System.out.println("  EXIT ");
		System.out.println("---------------------------------------------------------------------------------");
		System.out.println("Saisissez vos textes (EXIT pour quitter) :");
		do {
			saisie = clavier.nextLine();
			client.envoyer(saisie);
		}while (!"EXIT".equals(saisie));
		//Lorsque le client envoie "EXIT", le serveur répond "END." et le gestionnaire d'événement déconnecte le client

		//client.deconnecter();
	}
}
