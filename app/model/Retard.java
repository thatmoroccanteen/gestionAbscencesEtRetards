package app.model;

public class Retard {
    private int id;
    private String date;
    private int duree;
    private int idEtudiant;

    public Retard() {
    }

    public Retard(int id, String date, int duree, int idEtudiant) {
        this.id = id;
        this.date = date;
        this.duree = duree;
        this.idEtudiant = idEtudiant;
    }

    public Retard(String date, int duree, int idEtudiant) {
        this.date = date;
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
