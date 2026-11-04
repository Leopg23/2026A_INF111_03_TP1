package gestionnaireBibliotheque;

import java.util.*;

public class GestionnaireBibliotheque {
    private int idConnection;
    private String typeCommande;
    private String argument;
    //-------Attribut privé-------
    private Map<Integer, Utilisateur> utilisateurs;
    /* contient l’ensemble des utilisateurs enregistrés dans le système. La clé
représente l’identifiant de l’utilisateur, tandis que la valeur correspond à l’objet Utilisateur
associé.*/


    private Map<Integer, Utilisateur> utilisateursAuthentifies;
    /* contient les utilisateurs actuellement connectés au
système. La clé représente l’identifiant de connexion du client, tandis que la valeur correspond
à l’utilisateur authentifié sur cette connexion*/


    private ListesLivres livres;
    /*contient la liste des livres présents dans la bibliothèque. Cette structure
permettra notamment de rechercher, afficher, emprunter et réserver les livres.*/


    private FilePrioriteReservations reservationsEnAttente;
    /*contient l’ensemble des réservations actuellement en
attente dans la bibliothèque. Les réservations sont stockées dans une file de priorité afin de
respecter automatiquement l’ordre de priorité défini dans le système : professeur, personnel,
étudiant, puis ordre d’arrivée des réservations.*/


    // AJOUT : constructeur qui initialise les attributs (évite les NullPointerException)
    public GestionnaireBibliotheque() {
        this.utilisateurs = new HashMap<>();
        this.utilisateursAuthentifies = new HashMap<>();
        this.livres = new ListesLivres(new ArrayList<>());
        this.reservationsEnAttente = new FilePrioriteReservations(new PriorityQueue<>());
    }


    // Point d'entrée : redirige chaque commande vers la bonne méthode
    public String traiterCommande(int idConnexion, String typeCommande, String argument) {
        this.idConnection = idConnexion;
        this.typeCommande = typeCommande;
        this.argument = argument;

        if (typeCommande == null) {
            return "BAD_COMMAND_ERROR"; // à adapter selon l'énoncé
        }

        if (typeCommande.equalsIgnoreCase("ID")) {
            return traiterID(idConnexion, argument);
        } else if (typeCommande.equalsIgnoreCase("BOOKS")) {
            return traiterBOOKS();
        } else if (typeCommande.equalsIgnoreCase("BORROW")) {
            return traiterBORROW(idConnexion, argument);
        } else if (typeCommande.equalsIgnoreCase("RETURN")) {
            return traiterRETURN(idConnexion, argument);
        } else if (typeCommande.equalsIgnoreCase("RESERVE")) {
            return traiterRESERVE(idConnexion, argument);
        } else if (typeCommande.equalsIgnoreCase("INFO")) {
            return traiterINFO(idConnexion, argument);
        } else if (typeCommande.equalsIgnoreCase("PENALTY")) {
            return traiterPENALTY(idConnexion, argument);
        }else if (typeCommande.equalsIgnoreCase("REGISTER")) {
            return traiterREGISTER(argument);
        }else if (typeCommande.equalsIgnoreCase("EXIT")) {
            return traiterEXIT(idConnexion);

        }

        return "BAD_COMMAND_ERROR"; // à adapter selon l'énoncé
    }


    // Exercice 1 : commande ID <id utilisateur>
    public String traiterID(int idConnexion, String argument) {
        // 1. L'identifiant est valide ?
        int idUtilisateur;
        try {
            idUtilisateur = Integer.parseInt(argument.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return "BAD_ARGUMENT_ERROR";
        }

        // 2. L'utilisateur existe ?
        Utilisateur utilisateur = utilisateurs.get(idUtilisateur);
        if (utilisateur == null) {
            return "BAD_ARGUMENT_ERROR";
        }

        // 3. La connexion courante est authentifiée ?
        if (utilisateursAuthentifies.containsKey(idConnexion)) {
            return "ALREADY_AUTHENTIFIED_ERROR";
        }

        // 4. L'utilisateur est connecté depuis un autre client ?
        if (utilisateursAuthentifies.containsValue(utilisateur)) {
            return "TOO_MANY_CONNECTIONS_ERROR";
        }

        // 5. Tout est good
        utilisateursAuthentifies.put(idConnexion, utilisateur);
        return "AUTHORIZED " + utilisateur.getId() + " " + utilisateur.getNom();
    }

    // Exercice 2 : commande BOOKS
    public String traiterBOOKS() {
        StringBuilder reponse = new StringBuilder("BOOKS ");
        reponse.append(livres.taille());

        for (Livre livre : livres) {
            reponse.append(" {")
                    .append(livre.getId()).append(" ")
                    .append(livre.getTitre()).append(" ")
                    .append(livre.getAuteur()).append(" ")
                    .append(livre.getCategorie()).append(" ")
                    .append(livre.getStatut())
                    .append("}");
        }
        return reponse.toString();
    }

    // Exercice 3 : commande BORROW <id livre> <jour>
    public String traiterBORROW(int idConnexion, String argument) {
        // 1. L'utilisateur est authentifié sur cette connexion ?
        Utilisateur utilisateur = utilisateursAuthentifies.get(idConnexion);
        if (utilisateur == null) {
            return "AUTHENTICATION_ERROR";
        }

        // 2. Les arguments sont valides ? (2 nombres)
        if (argument == null) {
            return "BAD_ARGUMENT_ERROR";
        }
        String[] parties = argument.trim().split("\\s+");
        if (parties.length != 2) {
            return "BAD_ARGUMENT_ERROR";
        }
        int idLivre;
        int jour;
        try {
            idLivre = Integer.parseInt(parties[0]);
            jour = Integer.parseInt(parties[1]);
        } catch (NumberFormatException e) {
            return "BAD_ARGUMENT_ERROR";
        }
        if (jour < 0) {
            return "BAD_ARGUMENT_ERROR";
        }

        // 3. Le livre existe ?
        Livre livre = livres.rechercher(idLivre);
        if (livre == null) {
            return "BOOK_NOT_FOUND_ERROR";
        }

        // 4. Le livre est disponible ?
        if (livre.getStatut() != StatutLivre.DISPONIBLE) {
            return "BOOK_NOT_AVAILABLE_ERROR";
        }

        // 5. L'utilisateur est à sa limite ?
        if (!utilisateur.peutEmprunter()) {
            return "BORROW_LIMIT_ERROR";
        }

        // 6. Tout est good
        Emprunt emprunt = new Emprunt(livre, utilisateur.getId(), jour);
        utilisateur.ajouterEmpruntEnCours(emprunt);
        livre.setStatut(StatutLivre.EMPRUNTE);

        return "BORROW_OK " + emprunt.getId() + " " + livre.getId() + " " + emprunt.getJourRetourPrevu();
    }

    //Exercice 4 : commande RETURN <id livre> <jour>
    // Exercice 4 : commande RETURN <id emprunt> <jour>
    public String traiterRETURN(int idConnexion, String argument) {
        // 1. L'utilisateur est authentifié ?
        Utilisateur utilisateur = utilisateursAuthentifies.get(idConnexion);
        if (utilisateur == null) {
            return "AUTHENTICATION_ERROR";
        }

        // 2. Les arguments sont valides ?
        if (argument == null || argument.trim().isEmpty()) {
            return "BAD_ARGUMENT_ERROR";
        }
        String[] parties = argument.trim().split("\\s+");
        if (parties.length != 2) {
            return "BAD_ARGUMENT_ERROR";
        }
        int idEmprunt;
        int jourRetour;
        try {
            idEmprunt = Integer.parseInt(parties[0]);
            jourRetour = Integer.parseInt(parties[1]);
        } catch (NumberFormatException e) {
            return "BAD_ARGUMENT_ERROR";
        }
        if (jourRetour < 0) {
            return "BAD_ARGUMENT_ERROR";
        }

        // 3. Vérifier que l'emprunt est dans les emprunts en cours de l'utilisateur
        Emprunt emprunt = utilisateur.supprimerEmpruntEnCours(idEmprunt);
        if (emprunt == null) {
            return "BAD_BORROW_ERROR";
        }

        // 4. Mettre à jour l'emprunt et le déplacer vers les emprunts terminés
        emprunt.retourner(jourRetour);
        utilisateur.ajouterEmpruntTermine(emprunt);

        Livre livre = emprunt.getLivre();

        // 5. Vérifier s'il existe une réservation en attente pour ce livre
        Reservation reservationPrioritaire = reservationsEnAttente.retirerPourLivre(livre.getId());

        if (reservationPrioritaire == null) {
            livre.setStatut(StatutLivre.DISPONIBLE);
            return "RETURN_OK " + emprunt.getId() + " " + livre.getId();
        } else {
            reservationPrioritaire.setStatut(StatutReservation.ATTRIBUEE);
            Utilisateur utilisateurPrioritaire = utilisateurs.get(reservationPrioritaire.getIdUtilisateur());

            Emprunt nouvelEmprunt = new Emprunt(livre, utilisateurPrioritaire.getId(), jourRetour);
            utilisateurPrioritaire.ajouterEmpruntEnCours(nouvelEmprunt);

            return "RESERVATION_ASSIGNED " + nouvelEmprunt.getId() + " " + livre.getId();
        }
    }

    //Exercice 5 : commande RESERVE
    public String traiterRESERVE(int idConnexion, String argument) {
        // 1. L'utilisateur est authentifié sur cette connexion ?
        Utilisateur utilisateur = utilisateursAuthentifies.get(idConnexion);
        if (utilisateur == null) {
            return "AUTHENTICATION_ERROR";
        }

        // 2. L'identifiant est valide ?
        int idLivre;
        try {
            idLivre = Integer.parseInt(argument.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return "BAD_ARGUMENT_ERROR";
        }

        // 3. Le livre existe ?
        Livre livre = livres.rechercher(idLivre);
        if (livre == null) {
            return "BOOK_NOT_FOUND_ERROR";
        }

        //4. Le livre est reserve
        if (!livre.getStatut().equals(StatutLivre.EMPRUNTE)) {
            return "BOOK_ALREADY_AVAILABLE_ERROR";
        }

        // 5. L'utilisateur reserve en doublon
        if (utilisateur.possedeReservationEnAttentePourLivre(idLivre)) {
            return "BAD_ARGUMENT_ERROR";
        }


        //6. Tout est good
        Reservation reservation = new Reservation(livre, utilisateur.getId(), utilisateur.getTypeUtilisateur());
        reservationsEnAttente.ajouter(reservation);
        livre.setStatut(StatutLivre.RESERVE);

        //7. verifier quil n'est pas a la limite de reservation
        if (!utilisateur.ajouterReservation(reservation)) {
            return "RESERVATION_LIMIT_ERROR";
        }
        return "RESERVATION_OK";
    }

    //Exercice 6 : commande INFO
    public String traiterINFO(int idConnexion, String argument) {
        // 1. L'utilisateur est authentifié sur cette connexion ?
        Utilisateur utilisateur = utilisateursAuthentifies.get(idConnexion);
        if (utilisateur == null) {
            return "AUTHENTICATION_ERROR";
        }
        // 2. Verifier si l'utilisateur a entree un argument
        if (argument != "") {
            int idEmprunt;
            try {
                idEmprunt = Integer.parseInt(argument.trim());
            } catch (NumberFormatException | NullPointerException e) {
                return "BAD_ARGUMENT_ERROR";
            }
            if (!utilisateur.getEmpruntsEnCours().contient(idEmprunt)) {
                return "BAD_BORROW_ERROR";
            } else {
                return utilisateur.getEmpruntsEnCours().rechercher(idEmprunt).toString();
            }
        }

        return utilisateur.getEmpruntsEnCours().toString();
    }

    //Exercice 7 : commande PENALTY <id emprunt> <jour>
    public String traiterPENALTY(int idConnexion, String argument) {
        // 1. L'utilisateur est authentifié sur cette connexion ?
        Utilisateur utilisateur = utilisateursAuthentifies.get(idConnexion);
        if (utilisateur == null) {
            return "AUTHENTICATION_ERROR";
        }

        // 2. Les arguments sont valides ? (2 nombres)
        if (argument == null) {
            return "BAD_ARGUMENT_ERROR";
        }
        String[] parties = argument.trim().split("\\s+");
        if (parties.length != 2) {
            return "BAD_ARGUMENT_ERROR";
        }
        int idEmprunt;
        int jour;
        try {
            idEmprunt = Integer.parseInt(parties[0]);
            jour = Integer.parseInt(parties[1]);
        } catch (NumberFormatException e) {
            return "BAD_ARGUMENT_ERROR";
        }

        if (jour < 0) {
            return "BAD_ARGUMENT_ERROR";
        }
        Emprunt emprunt = utilisateur.rechercherEmpruntEnCours(idEmprunt);
        if (emprunt == null) {
            emprunt = utilisateur.rechercherEmpruntTermine(idEmprunt);
        }
        if (emprunt == null) {
            return "BAD_BORROW_ERROR";
        }else {
            emprunt = utilisateur.rechercherEmpruntEnCours(idEmprunt) == null
                    ? utilisateur.rechercherEmpruntTermine(idEmprunt)
                    : utilisateur.rechercherEmpruntEnCours(idEmprunt);
        }
        int nombresDeJoursDeRetard = Math.max(jour - emprunt.getJourRetourPrevu(), 0);
        double montant = utilisateur.calculerPenalite(nombresDeJoursDeRetard);

        return String.format(Locale.US, "PENALTY %.2f %d", montant, nombresDeJoursDeRetard);
    }

    //Exercice 8 : REGISTER <idUtilisateur> <typeUtilisateur> <nomUtilisateur>
    public String traiterREGISTER(String argument){
        // 1. Les arguments sont valides ? (2 nombres)
        if (argument == null) {
            return "BAD_ARGUMENT_ERROR";
        }
        String[] parties = argument.trim().split("\\s+");
        if (parties.length != 3) {
            return "BAD_ARGUMENT_ERROR";
        }
        int idUtilisateur;
        String typeUtilisateur;
        String nomUtilisateur;
        Utilisateur utilisateur = null;
        try {
            idUtilisateur = Integer.parseInt(parties[0]);
            typeUtilisateur = parties[1];
            nomUtilisateur = parties[2];
        } catch (NumberFormatException e) {
            return "BAD_ARGUMENT_ERROR";
        }
        if(utilisateurs.containsKey(idUtilisateur)){
            return "BAD_ARGUMENT_ERROR";
        }

        if (!typeUtilisateur.equals("ETUDIANT") && !typeUtilisateur.equals("PROFESSEUR") && !typeUtilisateur.equals("PERSONNEL")) {
            return "BAD_ARGUMENT_ERROR";
        }

        //2. Tout est good
        else if (typeUtilisateur.equals("ETUDIANT")) {
            utilisateur = new Etudiant(idUtilisateur, nomUtilisateur);
        }
        else if (typeUtilisateur.equals("PROFESSEUR")) {
            utilisateur = new Professeur(idUtilisateur, nomUtilisateur);
        }
        else if (typeUtilisateur.equals("PERSONNEL")) {
            utilisateur = new Personnel(idUtilisateur, nomUtilisateur);
        }
        if(utilisateur != null){
          utilisateurs.put(idUtilisateur, utilisateur);
        }

            return String.format("REGISTERED %d %s", idUtilisateur, nomUtilisateur);
    }

    //Exercice 9 : EXIT
    public String traiterEXIT(int idConnexion){
        // 1. L'utilisateur est authentifié sur cette connexion ?
        Utilisateur utilisateur = utilisateursAuthentifies.get(idConnexion);
        if (utilisateur == null) {
            return "AUTHENTICATION_ERROR";
        }
        utilisateursAuthentifies.remove(idConnexion);


        //2. Tout est good
        return "END";
    };


    public ListesLivres getLivres() {
        return livres;
    }
}