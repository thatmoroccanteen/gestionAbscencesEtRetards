package app.model;

public class AbsenceView {
    private final int id;
    private final String date;
    private final String motif;
    private final int duree;
    private final int idEtudiant;
    private final String nomEtudiant;
    private final String prenomEtudiant;

    public AbsenceView(int id, String date, String motif, int duree, int idEtudiant, String nomEtudiant, String prenomEtudiant) {
        this.id = id;
        this.date = date;
        this.motif = motif;
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

    public String getMotif() {
        return motif;
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
