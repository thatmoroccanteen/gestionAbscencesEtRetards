package app.dao;

import app.DatabaseConnection;
import app.model.Etudiant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EtudiantDAO {
    public void ajouter(Etudiant etudiant) throws SQLException {
        String sql = "INSERT INTO Etudiant (nom, prenom) VALUES (?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, etudiant.getNom());
            statement.setString(2, etudiant.getPrenom());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    etudiant.setId(keys.getInt(1));
                }
            }
        }
    }

    public void modifier(Etudiant etudiant) throws SQLException {
        String sql = "UPDATE Etudiant SET nom = ?, prenom = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, etudiant.getNom());
            statement.setString(2, etudiant.getPrenom());
            statement.setInt(3, etudiant.getId());
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Aucun etudiant trouve avec cet ID.");
            }
        }
    }

    public void supprimer(int id) throws SQLException {
        String deleteAbsences = "DELETE FROM Absence WHERE idEtudiant = ?";
        String deleteRetards = "DELETE FROM Retard WHERE idEtudiant = ?";
        String deleteEtudiant = "DELETE FROM Etudiant WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement absencesStatement = connection.prepareStatement(deleteAbsences);
                 PreparedStatement retardsStatement = connection.prepareStatement(deleteRetards);
                 PreparedStatement etudiantStatement = connection.prepareStatement(deleteEtudiant)) {

                absencesStatement.setInt(1, id);
                absencesStatement.executeUpdate();

                retardsStatement.setInt(1, id);
                retardsStatement.executeUpdate();

                etudiantStatement.setInt(1, id);
                if (etudiantStatement.executeUpdate() == 0) {
                    connection.rollback();
                    throw new SQLException("Aucun etudiant trouve avec cet ID.");
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public Etudiant trouverParId(int id) throws SQLException {
        String sql = "SELECT id, nom, prenom FROM Etudiant WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extraireEtudiant(resultSet);
                }
            }
        }

        return null;
    }

    public List<Etudiant> listerTous() throws SQLException {
        List<Etudiant> etudiants = new ArrayList<>();
        String sql = "SELECT id, nom, prenom FROM Etudiant ORDER BY id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                etudiants.add(extraireEtudiant(resultSet));
            }
        }

        return etudiants;
    }

    private Etudiant extraireEtudiant(ResultSet resultSet) throws SQLException {
        return new Etudiant(
                resultSet.getInt("id"),
                resultSet.getString("nom"),
                resultSet.getString("prenom")
        );
    }
}
