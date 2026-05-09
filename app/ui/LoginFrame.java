package app.ui;

import app.Session;
import app.dao.UtilisateurDAO;
import app.model.Utilisateur;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

public class LoginFrame extends JDialog {
    private static final long serialVersionUID = 1L;

    private final JTextField usernameField = new JTextField(35);
    private final JPasswordField passwordField = new JPasswordField(35);
    private final JLabel errorLabel = new JLabel(" ");
    private boolean loginSuccess = false;

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    public LoginFrame(Frame parent) {
        super(parent, "Connexion - Gestion des absences", true);
        buildUI();
        setSize(550, 350);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }

            @Override
            public void windowOpened(WindowEvent e) {
                // set focus to username field when the dialog appears
                usernameField.requestFocusInWindow();
            }
        });
        // Improve field fonts for readability
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);
        usernameField.setFont(fieldFont);
        passwordField.setFont(fieldFont);
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        main.setBackground(UITheme.BG_FRAME);

        // Header
        JLabel title = new JLabel("Systeme de Gestion des Absences", SwingConstants.CENTER);
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.PRIMARY_DARK);
        main.add(title, BorderLayout.NORTH);

        // Form
        JPanel form = UITheme.sectionPanel("Identification");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);

        // Labels on the left, fields expand horizontally
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Identifiant :"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        usernameField.setPreferredSize(new Dimension(300, 30));
        form.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        form.add(new JLabel("Mot de passe :"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        passwordField.setPreferredSize(new Dimension(300, 30));
        form.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        errorLabel.setForeground(UITheme.DANGER);
        errorLabel.setFont(UITheme.FONT_SMALL.deriveFont(Font.ITALIC));
        form.add(errorLabel, gbc);

        main.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);
        JButton loginBtn = UITheme.primaryButton("Se connecter");
        JButton cancelBtn = UITheme.secondaryButton("Annuler");

        loginBtn.setPreferredSize(new Dimension(140, 30));
        cancelBtn.setPreferredSize(new Dimension(100, 30));
        loginBtn.addActionListener(e -> tentativeConnexion());
        cancelBtn.addActionListener(e -> { System.exit(0); });

        // Allow Enter key on password field
        passwordField.addActionListener(e -> tentativeConnexion());

        // Add placeholders
        addPlaceholder(usernameField, "ex: admin", false);
        addPlaceholder(passwordField, "mot de passe", true);

        buttons.add(cancelBtn);
        buttons.add(loginBtn);
        main.add(buttons, BorderLayout.SOUTH);

        setContentPane(main);
        getRootPane().setDefaultButton(loginBtn);
    }

    private void addPlaceholder(final JTextComponent comp, final String placeholder, final boolean isPassword) {
        comp.setText(placeholder);
        comp.setForeground(Color.GRAY);
        if (isPassword && comp instanceof JPasswordField) {
            ((JPasswordField) comp).setEchoChar((char) 0);
        }
        comp.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (comp.getText().equals(placeholder)) {
                    comp.setText("");
                    comp.setForeground(Color.BLACK);
                    if (isPassword && comp instanceof JPasswordField) {
                        ((JPasswordField) comp).setEchoChar('\u2022');
                    }
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (comp.getText().isEmpty()) {
                    comp.setForeground(Color.GRAY);
                    comp.setText(placeholder);
                    if (isPassword && comp instanceof JPasswordField) {
                        ((JPasswordField) comp).setEchoChar((char) 0);
                    }
                }
            }
        });
    }

    private void tentativeConnexion() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        try {
            Utilisateur user = utilisateurDAO.authentifier(username, password);
            if (user != null) {
                if ("ETUDIANT".equals(user.getRole()) && user.getIdEtudiant() <= 0) {
                    errorLabel.setText("Compte etudiant non lie a un ID Etudiant.");
                    return;
                }
                Session.setCurrentUser(user);
                loginSuccess = true;
                dispose();
            } else {
                errorLabel.setText("Identifiant ou mot de passe incorrect.");
                passwordField.setText("");
            }
        } catch (SQLException e) {
            errorLabel.setText("Erreur de connexion : " + e.getMessage());
        }
    }

    public boolean isLoginSuccess() {
        return loginSuccess;
    }
}
