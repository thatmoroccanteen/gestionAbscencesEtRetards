package app.model;

public class Utilisateur {
    private int id;
    private String username;
    private String password; // SHA-256 hash
    private String role;     // ETUDIANT, ENSEIGNANT, ADMIN
    private int idEtudiant;  // 0 if not a student account

    public Utilisateur() {}

    public Utilisateur(int id, String username, String password, String role, int idEtudiant) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.idEtudiant = idEtudiant;
    }

    public Utilisateur(String username, String password, String role, int idEtudiant) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.idEtudiant = idEtudiant;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getIdEtudiant() { return idEtudiant; }
    public void setIdEtudiant(int idEtudiant) { this.idEtudiant = idEtudiant; }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}
