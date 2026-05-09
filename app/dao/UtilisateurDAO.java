package app.dao;

import app.DatabaseConnection;
import app.model.Utilisateur;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 non disponible", e);
        }
    }

    public Utilisateur authentifier(String username, String password) throws SQLException {
        String sql = "SELECT id, username, password, role, idEtudiant FROM Utilisateur WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, hashPassword(password));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extraire(rs);
                }
            }
        }
        return null;
    }

    public void ajouter(Utilisateur u) throws SQLException {
        String sql = "INSERT INTO Utilisateur (username, password, role, idEtudiant) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, u.getUsername());
            stmt.setString(2, hashPassword(u.getPassword()));
            stmt.setString(3, u.getRole());
            stmt.setInt(4, u.getIdEtudiant());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) u.setId(keys.getInt(1));
            }
        }
    }

    public void modifier(Utilisateur u) throws SQLException {
        String sql = "UPDATE Utilisateur SET username = ?, role = ?, idEtudiant = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, u.getUsername());
            stmt.setString(2, u.getRole());
            stmt.setInt(3, u.getIdEtudiant());
            stmt.setInt(4, u.getId());
            if (stmt.executeUpdate() == 0)
                throw new SQLException("Aucun utilisateur trouve avec cet ID.");
        }
    }

    public void changerMotDePasse(int id, String newPassword) throws SQLException {
        String sql = "UPDATE Utilisateur SET password = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hashPassword(newPassword));
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM Utilisateur WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0)
                throw new SQLException("Aucun utilisateur trouve avec cet ID.");
        }
    }

    public List<Utilisateur> listerTous() throws SQLException {
        List<Utilisateur> list = new ArrayList<>();
        String sql = "SELECT id, username, password, role, idEtudiant FROM Utilisateur ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(extraire(rs));
        }
        return list;
    }

    public boolean usernameExiste(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Utilisateur WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private Utilisateur extraire(ResultSet rs) throws SQLException {
        return new Utilisateur(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("role"),
                rs.getInt("idEtudiant")
        );
    }
}
