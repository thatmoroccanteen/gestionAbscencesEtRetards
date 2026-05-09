package app.dao;

import app.DatabaseConnection;
import app.model.Justification;
import app.model.JustificationView;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JustificationDAO {

    public void ajouter(Justification j) throws SQLException {
        // Check if justification already exists for this absence
        String check = "SELECT COUNT(*) FROM Justification WHERE idAbsence = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(check)) {
            stmt.setInt(1, j.getIdAbsence());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Une justification existe deja pour cette absence.");
                }
            }
        }

        String sql = "INSERT INTO Justification (idAbsence, raison, statut) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, j.getIdAbsence());
            stmt.setString(2, j.getRaison());
            stmt.setString(3, "En attente");
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) j.setId(keys.getInt(1));
            }
        }
    }

    public void mettreAJourStatut(int id, String statut) throws SQLException {
        String sql = "UPDATE Justification SET statut = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, statut);
            stmt.setInt(2, id);
            if (stmt.executeUpdate() == 0)
                throw new SQLException("Aucune justification trouvee avec cet ID.");
        }
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM Justification WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0)
                throw new SQLException("Aucune justification trouvee avec cet ID.");
        }
    }

    public List<JustificationView> listerTous() throws SQLException {
        return listerAvecFiltre(null);
    }

    public List<JustificationView> listerParEtudiant(int idEtudiant) throws SQLException {
        return listerAvecFiltre(idEtudiant);
    }

    private List<JustificationView> listerAvecFiltre(Integer idEtudiant) throws SQLException {
        List<JustificationView> list = new ArrayList<>();
        String sql = "SELECT j.id, j.idAbsence, j.raison, j.statut, " +
                "a.date, a.motif, a.idEtudiant, e.nom, e.prenom " +
                "FROM Justification j " +
                "JOIN Absence a ON j.idAbsence = a.id " +
                "LEFT JOIN Etudiant e ON a.idEtudiant = e.id";
        if (idEtudiant != null) sql += " WHERE a.idEtudiant = ?";
        sql += " ORDER BY j.id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (idEtudiant != null) stmt.setInt(1, idEtudiant);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new JustificationView(
                            rs.getInt("id"),
                            rs.getInt("idAbsence"),
                            rs.getString("raison"),
                            rs.getString("statut"),
                            rs.getString("date"),
                            rs.getString("motif"),
                            rs.getInt("idEtudiant"),
                            rs.getString("nom"),
                            rs.getString("prenom")
                    ));
                }
            }
        }
        return list;
    }
}
