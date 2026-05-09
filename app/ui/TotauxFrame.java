package app.ui;

import app.dao.AbsenceDAO;
import app.dao.EtudiantDAO;
import app.dao.RetardDAO;
import app.model.Etudiant;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;

public class TotauxFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final EtudiantDAO etudiantDAO = new EtudiantDAO();
    private final AbsenceDAO absenceDAO = new AbsenceDAO();
    private final RetardDAO retardDAO = new RetardDAO();

    private final JTextField idEtudiantField = new JTextField(10);
    private final JLabel etudiantLabel = new JLabel("-");
    private final JLabel heuresRetardsLabel = new JLabel("0");
    private final JLabel heuresAbsencesLabel = new JLabel("0");
    private final JLabel totalLabel = new JLabel("0");

    public TotauxFrame() {
        setTitle("Total par etudiant");
        setSize(430, 260);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(UITheme.BG_FRAME);
        content.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        addRow(content, 0, "ID etudiant", idEtudiantField);
        addRow(content, 1, "Etudiant", etudiantLabel);
        addRow(content, 2, "Heures de retards", heuresRetardsLabel);
        addRow(content, 3, "Heures d'absences", heuresAbsencesLabel);

        totalLabel.setFont(UITheme.FONT_TITLE);
        totalLabel.setForeground(UITheme.PRIMARY_DARK);
        addRow(content, 4, "Total", totalLabel);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setBackground(UITheme.BG_FRAME);
        JButton calculerButton = UITheme.primaryButton("Calculer");
        calculerButton.addActionListener(e -> calculer());
        buttons.add(calculerButton);

        add(content, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void addRow(JPanel panel, int row, String label, java.awt.Component component) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.insets = new Insets(6, 6, 6, 12);
        labelConstraints.anchor = GridBagConstraints.EAST;
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(UITheme.FONT_BODY);
        labelComponent.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(labelComponent, labelConstraints);

        GridBagConstraints componentConstraints = new GridBagConstraints();
        componentConstraints.gridx = 1;
        componentConstraints.gridy = row;
        componentConstraints.insets = new Insets(6, 6, 6, 6);
        componentConstraints.anchor = GridBagConstraints.WEST;
        componentConstraints.fill = GridBagConstraints.HORIZONTAL;
        componentConstraints.weightx = 1.0;
        panel.add(component, componentConstraints);
        component.setFont(row == 4 ? UITheme.FONT_TITLE : UITheme.FONT_BODY);
        component.setForeground(row == 4 ? UITheme.PRIMARY_DARK : Color.BLACK);
    }

    private void calculer() {
        try {
            if (idEtudiantField.getText().trim().isEmpty()) {
                throw new IllegalArgumentException("ID etudiant est obligatoire.");
            }

            int idEtudiant = Integer.parseInt(idEtudiantField.getText().trim());
            Etudiant etudiant = etudiantDAO.trouverParId(idEtudiant);

            if (etudiant == null) {
                etudiantLabel.setText("Etudiant introuvable");
                heuresRetardsLabel.setText("0");
                heuresAbsencesLabel.setText("0");
                totalLabel.setText("0");
                return;
            }

            int heuresRetards = retardDAO.totalHeuresParEtudiant(idEtudiant);
            int heuresAbsences = absenceDAO.totalHeuresParEtudiant(idEtudiant);

            etudiantLabel.setText(etudiant.getNom() + " " + etudiant.getPrenom());
            heuresRetardsLabel.setText(String.valueOf(heuresRetards));
            heuresAbsencesLabel.setText(String.valueOf(heuresAbsences));
            totalLabel.setText(String.valueOf(heuresRetards + heuresAbsences));
        } catch (NumberFormatException e) {
            showError("ID etudiant doit etre un nombre.");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}
