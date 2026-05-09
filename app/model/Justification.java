package app.model;

public class Justification {
    private int id;
    private int idAbsence;
    private String raison;
    private String statut; // "En attente", "Acceptee", "Refusee"

    public Justification() {}

    public Justification(int id, int idAbsence, String raison, String statut) {
        this.id = id;
        this.idAbsence = idAbsence;
        this.raison = raison;
        this.statut = statut;
    }

    public Justification(int idAbsence, String raison) {
        this.idAbsence = idAbsence;
        this.raison = raison;
        this.statut = "En attente";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdAbsence() { return idAbsence; }
    public void setIdAbsence(int idAbsence) { this.idAbsence = idAbsence; }

    public String getRaison() { return raison; }
    public void setRaison(String raison) { this.raison = raison; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
