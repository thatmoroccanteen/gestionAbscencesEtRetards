package app.dao;

import app.DatabaseConnection;
import app.model.Retard;
import app.model.RetardView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RetardDAO {
    public void ajouter(Retard retard) throws SQLException {
        String sql = "INSERT INTO Retard (date, duree, idEtudiant) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, retard.getDate());
            statement.setInt(2, retard.getDuree());
            statement.setInt(3, retard.getIdEtudiant());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    retard.setId(keys.getInt(1));
                }
            }
        }
    }

    public void modifier(Retard retard) throws SQLException {
        String sql = "UPDATE Retard SET date = ?, duree = ?, idEtudiant = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, retard.getDate());
            statement.setInt(2, retard.getDuree());
            statement.setInt(3, retard.getIdEtudiant());
            statement.setInt(4, retard.getId());
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Aucun retard trouve avec cet ID.");
            }
        }
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM Retard WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Aucun retard trouve avec cet ID.");
            }
        }
    }

    public Retard trouverParId(int id) throws SQLException {
        String sql = "SELECT id, date, duree, idEtudiant FROM Retard WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extraireRetard(resultSet);
                }
            }
        }

        return null;
    }

    public List<Retard> listerTous() throws SQLException {
        List<Retard> retards = new ArrayList<>();
        String sql = "SELECT id, date, duree, idEtudiant FROM Retard ORDER BY id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                retards.add(extraireRetard(resultSet));
            }
        }

        return retards;
    }

    public List<RetardView> listerTousAvecEtudiant() throws SQLException {
        List<RetardView> retards = new ArrayList<>();
        String sql = "SELECT r.id, r.date, r.duree, r.idEtudiant, e.nom, e.prenom " +
                "FROM Retard r " +
                "LEFT JOIN Etudiant e ON r.idEtudiant = e.id " +
                "ORDER BY r.id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                retards.add(new RetardView(
                        resultSet.getInt("id"),
                        resultSet.getString("date"),
                        resultSet.getInt("duree"),
                        resultSet.getInt("idEtudiant"),
                        resultSet.getString("nom"),
                        resultSet.getString("prenom")
                ));
            }
        }

        return retards;
    }

    public int totalMinutesParEtudiant(int idEtudiant) throws SQLException {
        String sql = "SELECT COALESCE(SUM(duree), 0) AS total FROM Retard WHERE idEtudiant = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idEtudiant);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("total");
                }
            }
        }

        return 0;
    }

    public int totalHeuresParEtudiant(int idEtudiant) throws SQLException {
        return totalMinutesParEtudiant(idEtudiant);
    }

    private Retard extraireRetard(ResultSet resultSet) throws SQLException {
        return new Retard(
                resultSet.getInt("id"),
                resultSet.getString("date"),
                resultSet.getInt("duree"),
                resultSet.getInt("idEtudiant")
        );
    }
}
