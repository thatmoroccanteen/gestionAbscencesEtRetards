package app.model;

public class JustificationView {
    private int id;
    private int idAbsence;
    private String raison;
    private String statut;
    private String dateAbsence;
    private String motifAbsence;
    private int idEtudiant;
    private String nomEtudiant;
    private String prenomEtudiant;

    public JustificationView(int id, int idAbsence, String raison, String statut,
                             String dateAbsence, String motifAbsence,
                             int idEtudiant, String nomEtudiant, String prenomEtudiant) {
        this.id = id;
        this.idAbsence = idAbsence;
        this.raison = raison;
        this.statut = statut;
        this.dateAbsence = dateAbsence;
        this.motifAbsence = motifAbsence;
        this.idEtudiant = idEtudiant;
        this.nomEtudiant = nomEtudiant;
        this.prenomEtudiant = prenomEtudiant;
    }

    public int getId() { return id; }
    public int getIdAbsence() { return idAbsence; }
    public String getRaison() { return raison; }
    public String getStatut() { return statut; }
    public String getDateAbsence() { return dateAbsence; }
    public String getMotifAbsence() { return motifAbsence; }
    public int getIdEtudiant() { return idEtudiant; }
    public String getNomEtudiant() { return nomEtudiant; }
    public String getPrenomEtudiant() { return prenomEtudiant; }
}
