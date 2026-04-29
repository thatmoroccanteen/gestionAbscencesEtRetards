package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:gestion_absences.db";

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(URL);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    public static void createTables() {
        String createEtudiantTable =
                "CREATE TABLE IF NOT EXISTS Etudiant (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nom TEXT, " +
                "prenom TEXT" +
                ")";

        String createAbsenceTable =
                "CREATE TABLE IF NOT EXISTS Absence (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, " +
                "motif TEXT, " +
                "duree INTEGER DEFAULT 0, " +
                "idEtudiant INTEGER, " +
                "FOREIGN KEY (idEtudiant) REFERENCES Etudiant(id) ON DELETE CASCADE" +
                ")";

        String createRetardTable =
                "CREATE TABLE IF NOT EXISTS Retard (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, " +
                "duree INTEGER, " +
                "idEtudiant INTEGER, " +
                "FOREIGN KEY (idEtudiant) REFERENCES Etudiant(id) ON DELETE CASCADE" +
                ")";

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(createEtudiantTable);
            statement.execute(createAbsenceTable);
            statement.execute(createRetardTable);
            addColumnIfMissing(connection, "Absence", "duree", "INTEGER DEFAULT 0");
            addColumnIfMissing(connection, "Retard", "duree", "INTEGER DEFAULT 0");

            System.out.println("Tables creees ou deja existantes.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la creation des tables : " + e.getMessage());
        }
    }

    private static void addColumnIfMissing(Connection connection, String tableName, String columnName, String definition)
            throws SQLException {
        try (ResultSet columns = connection.getMetaData().getColumns(null, null, tableName, columnName)) {
            if (!columns.next()) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
                }
            }
        }
    }
}
