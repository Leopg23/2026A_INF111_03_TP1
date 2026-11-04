package gestionnaireBibliotheque;


public class Reservation implements Comparable<Reservation>{
    //Variables
    private static int id=0;
    private Livre livre;
    private int idUtilisateur;
    private TypeUtilisateur typeUtilisateur;
    private static int staticOrdreReservation;
    private int ordreReservation=0;
    private StatutReservation statut = StatutReservation.EN_ATTENTE;

    //Constructeurs
    public Reservation(Livre livre, int idUtilisateur, TypeUtilisateur typeUtilisateur) {
        this.livre=livre;
        this.idUtilisateur=idUtilisateur;
        this.typeUtilisateur=typeUtilisateur;
        prochainID();
        ordreReservation=prochainOrdreReservation();
        statut = StatutReservation.EN_ATTENTE;

    }

    //Methodes
    private int prochainID(){
        return id++ ;
    };
    private int prochainOrdreReservation(){
        return staticOrdreReservation++ ;
    };

    @Override
    public int compareTo(Reservation autre) {
        if(autre.typeUtilisateur==typeUtilisateur){
            if(autre.ordreReservation>ordreReservation){
                return -1;
            }
            else if(autre.ordreReservation<ordreReservation){
                return 1;
            }
        }
        else{
            if(typeUtilisateur.equals(TypeUtilisateur.PROFESSEUR)){
                return -1;
            }
            if(typeUtilisateur.equals(TypeUtilisateur.PERSONNEL) && !autre.typeUtilisateur.equals(TypeUtilisateur.PROFESSEUR)){
                return -1;
            }
            if(typeUtilisateur.equals(TypeUtilisateur.ETUDIANT)){
                return 1;
            };
            if(autre.typeUtilisateur.equals(TypeUtilisateur.PROFESSEUR)){
                return 1;
            }
        }

        return 0;
    }

    @Override
    public String toString() {
        return "id: " + idUtilisateur + "livre_id: " + livre.getId() + ", id_utilisateur: " + idUtilisateur
                + ", typeUtilisateur: " + typeUtilisateur + "ordre_reservation" + ordreReservation
                + ", statut: " + statut + "\n";
    }

    //getter-setters
    public Livre getLivre() {
        return this.livre;
    }
    public void setStatut(StatutReservation statut ) {
        this.statut = statut;
    }
    public StatutReservation getIdUtilisateur() {
        return this.statut;
    }
}
