package app.model;

public class Absence {
    private int id;
    private String date;
    private String motif;
    private int duree;
    private int idEtudiant;

    public Absence() {
    }

    public Absence(int id, String date, String motif, int duree, int idEtudiant) {
        this.id = id;
        this.date = date;
        this.motif = motif;
        this.duree = duree;
        this.idEtudiant = idEtudiant;
    }

    public Absence(String date, String motif, int duree, int idEtudiant) {
        this.date = date;
        this.motif = motif;
        this.duree = duree;
        this.idEtudiant = idEtudiant;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public int getIdEtudiant() {
        return idEtudiant;
    }

    public void setIdEtudiant(int idEtudiant) {
        this.idEtudiant = idEtudiant;
    }
}
