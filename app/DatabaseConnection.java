package app;

import app.dao.UtilisateurDAO;
import app.model.Utilisateur;

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
            // set a busy timeout to avoid SQLITE_BUSY when DB is briefly locked
            statement.execute("PRAGMA busy_timeout = 5000");
            // use WAL mode to reduce locking contention
            statement.execute("PRAGMA journal_mode = WAL");
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

        String createUtilisateurTable =
                "CREATE TABLE IF NOT EXISTS Utilisateur (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "role TEXT NOT NULL, " +
                "idEtudiant INTEGER DEFAULT 0" +
                ")";

        String createJustificationTable =
                "CREATE TABLE IF NOT EXISTS Justification (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "idAbsence INTEGER NOT NULL, " +
                "raison TEXT NOT NULL, " +
                "statut TEXT NOT NULL DEFAULT 'En attente', " +
                "FOREIGN KEY (idAbsence) REFERENCES Absence(id) ON DELETE CASCADE" +
                ")";

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(createEtudiantTable);
            statement.execute(createAbsenceTable);
            statement.execute(createRetardTable);
            statement.execute(createUtilisateurTable);
            statement.execute(createJustificationTable);
            addColumnIfMissing(connection, "Absence", "duree", "INTEGER DEFAULT 0");
            addColumnIfMissing(connection, "Retard", "duree", "INTEGER DEFAULT 0");

            // Ensure default accounts exist even when the database already has users.
            ensureDefaultAccounts(connection);
            repairStudentAccountLinks(connection);

            System.out.println("Tables creees ou deja existantes.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la creation des tables : " + e.getMessage());
        }
    }

    private static void ensureDefaultAccounts(Connection connection) throws SQLException {
        boolean adminCreated = ensureDefaultAccount(connection,
                new Utilisateur("admin", "admin123", "ADMIN", 0));
        boolean teacherCreated = ensureDefaultAccount(connection,
                new Utilisateur("enseignant", "ens123", "ENSEIGNANT", 0));

        if (adminCreated || teacherCreated) {
            System.out.println("Comptes par defaut disponibles : admin/admin123, enseignant/ens123");
        }
    }

    private static boolean ensureDefaultAccount(Connection connection, Utilisateur utilisateur)
            throws SQLException {
        String existsSql = "SELECT 1 FROM Utilisateur WHERE username = ?";
        String insertSql = "INSERT INTO Utilisateur (username, password, role, idEtudiant) " +
                "VALUES (?, ?, ?, ?)";

        try (java.sql.PreparedStatement existsStatement = connection.prepareStatement(existsSql)) {
            existsStatement.setString(1, utilisateur.getUsername());
            try (ResultSet users = existsStatement.executeQuery()) {
                if (users.next()) {
                    return false;
                }
            }
        }

        try (java.sql.PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
            insertStatement.setString(1, utilisateur.getUsername());
            insertStatement.setString(2, UtilisateurDAO.hashPassword(utilisateur.getPassword()));
            insertStatement.setString(3, utilisateur.getRole());
            insertStatement.setInt(4, utilisateur.getIdEtudiant());
            insertStatement.executeUpdate();
            return true;
        }
    }

    private static void repairStudentAccountLinks(Connection connection) throws SQLException {
        String findUsers = "SELECT id, username FROM Utilisateur WHERE role = 'ETUDIANT' AND idEtudiant = 0";
        String findStudent = "SELECT id FROM Etudiant WHERE lower(nom) = lower(?)";
        String updateUser = "UPDATE Utilisateur SET idEtudiant = ? WHERE id = ?";

        try (Statement userStatement = connection.createStatement();
             ResultSet users = userStatement.executeQuery(findUsers)) {
            while (users.next()) {
                int userId = users.getInt("id");
                String username = users.getString("username");

                try (java.sql.PreparedStatement studentStatement = connection.prepareStatement(findStudent)) {
                    studentStatement.setString(1, username);
                    try (ResultSet students = studentStatement.executeQuery()) {
                        int matchingStudentId = 0;
                        int matchCount = 0;
                        while (students.next()) {
                            matchingStudentId = students.getInt("id");
                            matchCount++;
                        }

                        if (matchCount == 1) {
                            try (java.sql.PreparedStatement updateStatement = connection.prepareStatement(updateUser)) {
                                updateStatement.setInt(1, matchingStudentId);
                                updateStatement.setInt(2, userId);
                                updateStatement.executeUpdate();
                            }
                        }
                    }
                }
            }
        }
    }

    private static void addColumnIfMissing(Connection connection, String tableName,
                                           String columnName, String definition) throws SQLException {
        try (ResultSet columns = connection.getMetaData()
                .getColumns(null, null, tableName, columnName)) {
            if (!columns.next()) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("ALTER TABLE " + tableName +
                            " ADD COLUMN " + columnName + " " + definition);
                }
            }
        }
    }
}
