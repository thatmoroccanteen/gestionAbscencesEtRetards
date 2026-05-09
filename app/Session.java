package app;

import app.model.Utilisateur;

public class Session {
    private static Utilisateur currentUser;

    public static void setCurrentUser(Utilisateur user) {
        currentUser = user;
    }

    public static Utilisateur getCurrentUser() {
        return currentUser;
    }

    public static boolean isEnseignant() {
        return currentUser != null && "ENSEIGNANT".equals(currentUser.getRole());
    }

    public static boolean isEtudiant() {
        return currentUser != null && "ETUDIANT".equals(currentUser.getRole());
    }

    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.getRole());
    }

    public static boolean isEnseignantOrAdmin() {
        return isEnseignant() || isAdmin();
    }

    public static void logout() {
        currentUser = null;
    }
}
