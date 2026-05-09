package app.dao;

import app.DatabaseConnection;
import app.model.Absence;
import app.model.AbsenceView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AbsenceDAO {
    public void ajouter(Absence absence) throws SQLException {
        String sql = "INSERT INTO Absence (date, motif, duree, idEtudiant) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, absence.getDate());
            statement.setString(2, absence.getMotif());
            statement.setInt(3, absence.getDuree());
            statement.setInt(4, absence.getIdEtudiant());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    absence.setId(keys.getInt(1));
                }
            }
        }
    }

    public void modifier(Absence absence) throws SQLException {
        String sql = "UPDATE Absence SET date = ?, motif = ?, duree = ?, idEtudiant = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, absence.getDate());
            statement.setString(2, absence.getMotif());
            statement.setInt(3, absence.getDuree());
            statement.setInt(4, absence.getIdEtudiant());
            statement.setInt(5, absence.getId());
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Aucune absence trouvee avec cet ID.");
            }
        }
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM Absence WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Aucune absence trouvee avec cet ID.");
            }
        }
    }

    public Absence trouverParId(int id) throws SQLException {
        String sql = "SELECT id, date, motif, duree, idEtudiant FROM Absence WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extraireAbsence(resultSet);
                }
            }
        }

        return null;
    }

    public List<Absence> listerTous() throws SQLException {
        List<Absence> absences = new ArrayList<>();
        String sql = "SELECT id, date, motif, duree, idEtudiant FROM Absence ORDER BY id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                absences.add(extraireAbsence(resultSet));
            }
        }

        return absences;
    }

    public List<AbsenceView> listerTousAvecEtudiant() throws SQLException {
        return listerAvecEtudiant(null);
    }

    public List<AbsenceView> listerParEtudiant(int idEtudiant) throws SQLException {
        return listerAvecEtudiant(idEtudiant);
    }

    private List<AbsenceView> listerAvecEtudiant(Integer idEtudiant) throws SQLException {
        List<AbsenceView> absences = new ArrayList<>();
        String sql = "SELECT a.id, a.date, a.motif, a.duree, a.idEtudiant, e.nom, e.prenom " +
                "FROM Absence a " +
                "LEFT JOIN Etudiant e ON a.idEtudiant = e.id";
        if (idEtudiant != null) {
            sql += " WHERE a.idEtudiant = ?";
        }
        sql += " ORDER BY a.id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (idEtudiant != null) {
                statement.setInt(1, idEtudiant);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    absences.add(new AbsenceView(
                            resultSet.getInt("id"),
                            resultSet.getString("date"),
                            resultSet.getString("motif"),
                            resultSet.getInt("duree"),
                            resultSet.getInt("idEtudiant"),
                            resultSet.getString("nom"),
                            resultSet.getString("prenom")
                    ));
                }
            }
        }

        return absences;
    }

    public int totalHeuresParEtudiant(int idEtudiant) throws SQLException {
        String sql = "SELECT COALESCE(SUM(duree), 0) AS total FROM Absence WHERE idEtudiant = ?";

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

    private Absence extraireAbsence(ResultSet resultSet) throws SQLException {
        return new Absence(
                resultSet.getInt("id"),
                resultSet.getString("date"),
                resultSet.getString("motif"),
                resultSet.getInt("duree"),
                resultSet.getInt("idEtudiant")
        );
    }
}
