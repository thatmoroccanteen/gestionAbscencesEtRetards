package app.ui;

import app.dao.EtudiantDAO;
import app.dao.UtilisateurDAO;
import app.model.Utilisateur;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class GestionUtilisateursFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final EtudiantDAO etudiantDAO = new EtudiantDAO();

    private final JTextField usernameField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JComboBox<String> roleCombo = new JComboBox<>(new String[]{"ADMIN","ENSEIGNANT","ETUDIANT"});
    private final JTextField idEtudiantField = new JTextField(8);
    private final JTextField idField = new JTextField(8);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID","Identifiant","Role","ID Etudiant"}, 0) {
        private static final long serialVersionUID = 1L;
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    public GestionUtilisateursFrame() {
        setTitle("Gestion des utilisateurs");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        idField.setEditable(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        UITheme.styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromSelection();
        });

        JPanel form = UITheme.sectionPanel("Formulaire utilisateur");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4,6,4,6);

        addRow(form, gbc, 0, "Identifiant", usernameField);
        addRow(form, gbc, 1, "Mot de passe", passwordField);
        addRow(form, gbc, 2, "Role", roleCombo);
        addRow(form, gbc, 3, "ID Etudiant (0 si aucun)", idEtudiantField);

        JLabel hint = new JLabel("<html><i>Laisser vide pour conserver le mot de passe lors de la modification</i></html>");
        hint.setFont(UITheme.FONT_SMALL.deriveFont(Font.ITALIC));
        hint.setForeground(UITheme.TEXT_SECONDARY);
        gbc.gridx=0; gbc.gridy=4; gbc.gridwidth=2; gbc.anchor=GridBagConstraints.CENTER;
        form.add(hint, gbc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setOpaque(false);
        buttons.add(btn("Ajouter",   e -> ajouter()));
        buttons.add(btn("Modifier",  e -> modifier()));
        buttons.add(btn("Supprimer", e -> supprimer()));
        buttons.add(btn("Actualiser",e -> refresh()));

        JPanel top = new JPanel(new BorderLayout(8,8));
        top.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        top.setBackground(UITheme.BG_PANEL);
        top.add(form, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout(8,8));
        content.setBackground(UITheme.BG_FRAME);
        content.add(top, BorderLayout.NORTH);
        content.add(new JScrollPane(table), BorderLayout.CENTER);
        content.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        add(content);
        refresh();
    }

    private void addRow(JPanel p, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridwidth=1; gbc.gridx=0; gbc.gridy=row; gbc.anchor=GridBagConstraints.EAST;
        p.add(new JLabel(label+" :"), gbc);
        gbc.gridx=1; gbc.anchor=GridBagConstraints.WEST;
        p.add(field, gbc);
    }

    private JButton btn(String text, java.awt.event.ActionListener l) {
        JButton b;
        if ("Supprimer".equals(text)) {
            b = UITheme.dangerButton(text);
        } else if ("Ajouter".equals(text) || "Modifier".equals(text)) {
            b = UITheme.primaryButton(text);
        } else if ("Actualiser".equals(text)) {
            b = UITheme.accentButton(text);
        } else {
            b = UITheme.secondaryButton(text);
        }
        b.addActionListener(l);
        return b;
    }

    private void ajouter() {
        try {
            String username = requireText(usernameField, "Identifiant");
            String password = new String(passwordField.getPassword()).trim();
            if (password.isEmpty()) throw new IllegalArgumentException("Mot de passe obligatoire.");
            String role = (String) roleCombo.getSelectedItem();
            int idEtudiant = parseIdEtudiant();
            validateEtudiantLink(role, idEtudiant);

            if (utilisateurDAO.usernameExiste(username))
                throw new IllegalArgumentException("Cet identifiant existe deja.");

            utilisateurDAO.ajouter(new Utilisateur(username, password, role, idEtudiant));
            clearForm();
            refresh();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    private void modifier() {
        try {
            int id = parseInt(idField, "ID");
            String username = requireText(usernameField, "Identifiant");
            String role = (String) roleCombo.getSelectedItem();
            int idEtudiant = parseIdEtudiant();
            validateEtudiantLink(role, idEtudiant);

            Utilisateur u = new Utilisateur(id, username, "", role, idEtudiant);
            utilisateurDAO.modifier(u);

            String password = new String(passwordField.getPassword()).trim();
            if (!password.isEmpty()) utilisateurDAO.changerMotDePasse(id, password);

            clearForm();
            refresh();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    private void supprimer() {
        try {
            int id = parseInt(idField, "ID");
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Supprimer cet utilisateur ?", "Confirmation",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                utilisateurDAO.supprimer(id);
                clearForm();
                refresh();
            }
        } catch (Exception e) { showError(e.getMessage()); }
    }

    private void refresh() {
        try {
            tableModel.setRowCount(0);
            List<Utilisateur> list = utilisateurDAO.listerTous();
            for (Utilisateur u : list) {
                tableModel.addRow(new Object[]{u.getId(), u.getUsername(), u.getRole(), u.getIdEtudiant()});
            }
        } catch (SQLException e) { showError(e.getMessage()); }
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int mr = table.convertRowIndexToModel(row);
            idField.setText(String.valueOf(tableModel.getValueAt(mr,0)));
            usernameField.setText(String.valueOf(tableModel.getValueAt(mr,1)));
            passwordField.setText("");
            roleCombo.setSelectedItem(String.valueOf(tableModel.getValueAt(mr,2)));
            idEtudiantField.setText(String.valueOf(tableModel.getValueAt(mr,3)));
        }
    }

    private void clearForm() {
        idField.setText(""); usernameField.setText(""); passwordField.setText("");
        roleCombo.setSelectedIndex(0); idEtudiantField.setText("");
        table.clearSelection();
    }

    private int parseIdEtudiant() throws SQLException {
        String text = idEtudiantField.getText().trim();
        if (text.isEmpty() || text.equals("0")) return 0;
        try {
            int id = Integer.parseInt(text);
            if (id > 0 && etudiantDAO.trouverParId(id) == null)
                throw new IllegalArgumentException("Aucun etudiant avec ID=" + id);
            return id;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID Etudiant doit etre un nombre.");
        }
    }

    private void validateEtudiantLink(String role, int idEtudiant) {
        if ("ETUDIANT".equals(role) && idEtudiant <= 0) {
            throw new IllegalArgumentException("Un compte ETUDIANT doit etre lie a un ID Etudiant.");
        }
        if (!"ETUDIANT".equals(role) && idEtudiant != 0) {
            throw new IllegalArgumentException("ID Etudiant doit etre 0 pour ADMIN ou ENSEIGNANT.");
        }
    }

    private int parseInt(JTextField field, String name) {
        try { return Integer.parseInt(field.getText().trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException(name + " invalide."); }
    }

    private String requireText(JTextField field, String name) {
        String v = field.getText().trim();
        if (v.isEmpty()) throw new IllegalArgumentException(name + " obligatoire.");
        return v;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}
