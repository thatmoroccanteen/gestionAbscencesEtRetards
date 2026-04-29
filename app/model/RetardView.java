package app.model;

public class RetardView {
    private final int id;
    private final String date;
    private final int duree;
    private final int idEtudiant;
    private final String nomEtudiant;
    private final String prenomEtudiant;

    public RetardView(int id, String date, int duree, int idEtudiant, String nomEtudiant, String prenomEtudiant) {
        this.id = id;
        this.date = date;
        this.duree = duree;
        this.idEtudiant = idEtudiant;
        this.nomEtudiant = nomEtudiant;
        this.prenomEtudiant = prenomEtudiant;
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public int getDuree() {
        return duree;
    }

    public int getIdEtudiant() {
        return idEtudiant;
    }

    public String getNomEtudiant() {
        return nomEtudiant;
    }

    public String getPrenomEtudiant() {
        return prenomEtudiant;
    }
}
