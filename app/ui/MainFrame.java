package app.ui;

import app.dao.AbsenceDAO;
import app.dao.EtudiantDAO;
import app.dao.RetardDAO;
import app.model.Absence;
import app.model.AbsenceView;
import app.model.Etudiant;
import app.model.Retard;
import app.model.RetardView;
import com.toedter.calendar.JDateChooser;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final SimpleDateFormat databaseDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private final EtudiantDAO etudiantDAO = new EtudiantDAO();
    private final AbsenceDAO absenceDAO = new AbsenceDAO();
    private final RetardDAO retardDAO = new RetardDAO();

    private final JTextField etudiantIdField = new JTextField(8);
    private final JTextField nomField = new JTextField(18);
    private final JTextField prenomField = new JTextField(18);
    private final DefaultTableModel etudiantTableModel = new DefaultTableModel(
            new Object[]{"ID", "Nom", "Prenom"}, 0
    ) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable etudiantTable = new JTable(etudiantTableModel);

    private final JTextField absenceIdField = new JTextField(8);
    private final JDateChooser absenceDateChooser = createDateChooser();
    private final JTextField motifField = new JTextField(18);
    private final JTextField absenceDureeField = new JTextField(8);
    private final JTextField absenceEtudiantIdField = new JTextField(8);
    private final DefaultTableModel absenceTableModel = new DefaultTableModel(
            new Object[]{"ID", "Date", "Motif", "Duree", "ID etudiant", "Nom", "Prenom"}, 0
    ) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable absenceTable = new JTable(absenceTableModel);

    private final JTextField retardIdField = new JTextField(8);
    private final JDateChooser retardDateChooser = createDateChooser();
    private final JTextField dureeField = new JTextField(8);
    private final JTextField retardEtudiantIdField = new JTextField(8);
    private final DefaultTableModel retardTableModel = new DefaultTableModel(
            new Object[]{"ID", "Date", "Duree", "ID etudiant", "Nom", "Prenom"}, 0
    ) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable retardTable = new JTable(retardTableModel);

    public MainFrame() {
        databaseDateFormat.setLenient(false);

        setTitle("Gestion des absences");
        setSize(850, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Etudiants", createEtudiantsPanel());
        tabs.addTab("Absences", createAbsencesPanel());
        tabs.addTab("Retards", createRetardsPanel());
        add(tabs, BorderLayout.CENTER);
        add(createTopBar(), BorderLayout.NORTH);

        refreshAllTables();
    }

    private JPanel createTopBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(6, 8, 0, 8));

        JButton totauxButton = new JButton("Total par etudiant");
        totauxButton.addActionListener(e -> {
            TotauxFrame frame = new TotauxFrame();
            frame.setVisible(true);
        });
        panel.add(totauxButton);

        return panel;
    }

    private JPanel createEtudiantsPanel() {
        etudiantIdField.setEditable(false);
        configureTable(etudiantTable);
        etudiantTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillEtudiantFormFromSelection();
            }
        });

        JPanel form = createFormPanel();
        addRow(form, 0, "ID", etudiantIdField);
        addRow(form, 1, "Nom", nomField);
        addRow(form, 2, "Prenom", prenomField);

        JPanel buttons = createButtonPanel(
                button("Ajouter", e -> addEtudiant()),
                button("Modifier", e -> updateEtudiant()),
                button("Supprimer", e -> deleteEtudiant()),
                button("Effacer", e -> clearEtudiantForm()),
                button("Actualiser", e -> refreshEtudiants())
        );

        return createCrudPanel(form, buttons, etudiantTable);
    }

    private JPanel createAbsencesPanel() {
        absenceIdField.setEditable(false);
        configureTable(absenceTable);
        absenceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillAbsenceFormFromSelection();
            }
        });

        JPanel form = createFormPanel();
        addRow(form, 0, "ID", absenceIdField);
        addRow(form, 1, "Date", absenceDateChooser);
        addRow(form, 2, "Motif", motifField);
        addRow(form, 3, "Duree", absenceDureeField);
        addRow(form, 4, "ID etudiant", absenceEtudiantIdField);

        JPanel buttons = createButtonPanel(
                button("Ajouter", e -> addAbsence()),
                button("Modifier", e -> updateAbsence()),
                button("Supprimer", e -> deleteAbsence()),
                button("Effacer", e -> clearAbsenceForm()),
                button("Actualiser", e -> refreshAbsences())
        );

        return createCrudPanel(form, buttons, absenceTable);
    }

    private JPanel createRetardsPanel() {
        retardIdField.setEditable(false);
        configureTable(retardTable);
        retardTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillRetardFormFromSelection();
            }
        });

        JPanel form = createFormPanel();
        addRow(form, 0, "ID", retardIdField);
        addRow(form, 1, "Date", retardDateChooser);
        addRow(form, 2, "Duree", dureeField);
        addRow(form, 3, "ID etudiant", retardEtudiantIdField);

        JPanel buttons = createButtonPanel(
                button("Ajouter", e -> addRetard()),
                button("Modifier", e -> updateRetard()),
                button("Supprimer", e -> deleteRetard()),
                button("Effacer", e -> clearRetardForm()),
                button("Actualiser", e -> refreshRetards())
        );

        return createCrudPanel(form, buttons, retardTable);
    }

    private JPanel createCrudPanel(JPanel form, JPanel buttons, JTable table) {
        JPanel top = new JPanel(new BorderLayout(10, 10));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        top.add(form, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.add(top, BorderLayout.NORTH);
        content.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(18, 18, 18, 18);
        panel.add(content, constraints);
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Formulaire"));
        return form;
    }

    private void addRow(JPanel panel, int row, String label, Component field) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.insets = new Insets(5, 5, 5, 8);
        labelConstraints.anchor = GridBagConstraints.EAST;

        JLabel jLabel = new JLabel(label, SwingConstants.RIGHT);
        panel.add(jLabel, labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.insets = new Insets(5, 5, 5, 5);
        fieldConstraints.anchor = GridBagConstraints.WEST;
        panel.add(field, fieldConstraints);
    }

    private JDateChooser createDateChooser() {
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd/MM/yyyy");
        dateChooser.setPreferredSize(new Dimension(150, 24));
        return dateChooser;
    }

    private JPanel createButtonPanel(JButton... buttons) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (JButton button : buttons) {
            panel.add(button);
        }
        return panel;
    }

    private JButton button(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        return button;
    }

    private void configureTable(JTable table) {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
    }

    private void addEtudiant() {
        try {
            String nom = requireText(nomField, "Nom");
            String prenom = requireText(prenomField, "Prenom");

            etudiantDAO.ajouter(new Etudiant(nom, prenom));
            clearEtudiantForm();
            refreshEtudiants();
        } catch (SQLException | IllegalArgumentException e) {
            showError(e);
        }
    }

    private void updateEtudiant() {
        try {
            int id = parseInt(etudiantIdField, "ID etudiant");
            String nom = requireText(nomField, "Nom");
            String prenom = requireText(prenomField, "Prenom");

            etudiantDAO.modifier(new Etudiant(id, nom, prenom));
            clearEtudiantForm();
            refreshEtudiants();
        } catch (SQLException | IllegalArgumentException e) {
            showError(e);
        }
    }

    private void deleteEtudiant() {
        try {
            etudiantDAO.supprimer(parseInt(etudiantIdField, "ID etudiant"));
            clearEtudiantForm();
            refreshAllTables();
        } catch (SQLException | IllegalArgumentException e) {
            showError(e);
        }
    }

    private void addAbsence() {
        try {
            String dateFormatted = getFormattedDate(absenceDateChooser);
            Absence absence = new Absence(
                    dateFormatted,
                    requireText(motifField, "Motif"),
                    parsePositiveInt(absenceDureeField, "Duree"),
                    getExistingEtudiantId(absenceEtudiantIdField)
            );
            absenceDAO.ajouter(absence);
            clearAbsenceForm();
            refreshAbsences();
        } catch (SQLException | IllegalArgumentException e) {
            showError(e);
        }
    }

    private void updateAbsence() {
        try {
            String dateFormatted = getFormattedDate(absenceDateChooser);
            Absence absence = new Absence(
                    parseInt(absenceIdField, "ID absence"),
                    dateFormatted,
                    requireText(motifField, "Motif"),
                    parsePositiveInt(absenceDureeField, "Duree"),
                    getExistingEtudiantId(absenceEtudiantIdField)
            );
            absenceDAO.modifier(absence);
            clearAbsenceForm();
            refreshAbsences();
        } catch (SQLException | IllegalArgumentException e) {
            showError(e);
        }
    }

    private void deleteAbsence() {
        try {
            absenceDAO.supprimer(parseInt(absenceIdField, "ID absence"));
            clearAbsenceForm();
            refreshAbsences();
        } catch (SQLException | IllegalArgumentException e) {
            showError(e);
        }
    }

    private void addRetard() {
        try {
            String dateFormatted = getFormattedDate(retardDateChooser);
            Retard retard = new Retard(
                    dateFormatted,
                    parsePositiveInt(dureeField, "Duree"),
                    getExistingEtudiantId(retardEtudiantIdField)
            );
            retardDAO.ajouter(retard);
            clearRetardForm();
            refreshRetards();
        } catch (SQLException | IllegalArgumentException e) {
            showError(e);
        }
    }

    private void updateRetard() {
        try {
            String dateFormatted = getFormattedDate(retardDateChooser);
            Retard retard = new Retard(
                    parseInt(retardIdField, "ID retard"),
                    dateFormatted,
                    parsePositiveInt(dureeField, "Duree"),
                    getExistingEtudiantId(retardEtudiantIdField)
            );
            retardDAO.modifier(retard);
            clearRetardForm();
            refreshRetards();
        } catch (SQLException | IllegalArgumentException e) {
            showError(e);
        }
    }

    private void deleteRetard() {
        try {
            retardDAO.supprimer(parseInt(retardIdField, "ID retard"));
            clearRetardForm();
            refreshRetards();
        } catch (SQLException | IllegalArgumentException e) {
            showError(e);
        }
    }

    private void refreshAllTables() {
        refreshEtudiants();
        refreshAbsences();
        refreshRetards();
    }

    private void refreshEtudiants() {
        try {
            etudiantTableModel.setRowCount(0);
            List<Etudiant> etudiants = etudiantDAO.listerTous();
            for (Etudiant etudiant : etudiants) {
                etudiantTableModel.addRow(new Object[]{
                        etudiant.getId(),
                        etudiant.getNom(),
                        etudiant.getPrenom()
                });
            }
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void refreshAbsences() {
        try {
            absenceTableModel.setRowCount(0);
            List<AbsenceView> absences = absenceDAO.listerTousAvecEtudiant();
            for (AbsenceView absence : absences) {
                absenceTableModel.addRow(new Object[]{
                        absence.getId(),
                        absence.getDate(),
                        absence.getMotif(),
                        absence.getDuree(),
                        absence.getIdEtudiant(),
                        emptyIfNull(absence.getNomEtudiant()),
                        emptyIfNull(absence.getPrenomEtudiant())
                });
            }
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void refreshRetards() {
        try {
            retardTableModel.setRowCount(0);
            List<RetardView> retards = retardDAO.listerTousAvecEtudiant();
            for (RetardView retard : retards) {
                retardTableModel.addRow(new Object[]{
                        retard.getId(),
                        retard.getDate(),
                        retard.getDuree(),
                        retard.getIdEtudiant(),
                        emptyIfNull(retard.getNomEtudiant()),
                        emptyIfNull(retard.getPrenomEtudiant())
                });
            }
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void fillEtudiantFormFromSelection() {
        int row = etudiantTable.getSelectedRow();
        if (row >= 0) {
            int modelRow = etudiantTable.convertRowIndexToModel(row);
            etudiantIdField.setText(String.valueOf(etudiantTableModel.getValueAt(modelRow, 0)));
            nomField.setText(String.valueOf(etudiantTableModel.getValueAt(modelRow, 1)));
            prenomField.setText(String.valueOf(etudiantTableModel.getValueAt(modelRow, 2)));
        }
    }

    private void fillAbsenceFormFromSelection() {
        int row = absenceTable.getSelectedRow();
        if (row >= 0) {
            int modelRow = absenceTable.convertRowIndexToModel(row);
            absenceIdField.setText(String.valueOf(absenceTableModel.getValueAt(modelRow, 0)));
            setChooserDate(absenceDateChooser, String.valueOf(absenceTableModel.getValueAt(modelRow, 1)));
            motifField.setText(String.valueOf(absenceTableModel.getValueAt(modelRow, 2)));
            absenceDureeField.setText(String.valueOf(absenceTableModel.getValueAt(modelRow, 3)));
            absenceEtudiantIdField.setText(String.valueOf(absenceTableModel.getValueAt(modelRow, 4)));
        }
    }

    private void fillRetardFormFromSelection() {
        int row = retardTable.getSelectedRow();
        if (row >= 0) {
            int modelRow = retardTable.convertRowIndexToModel(row);
            retardIdField.setText(String.valueOf(retardTableModel.getValueAt(modelRow, 0)));
            setChooserDate(retardDateChooser, String.valueOf(retardTableModel.getValueAt(modelRow, 1)));
            dureeField.setText(String.valueOf(retardTableModel.getValueAt(modelRow, 2)));
            retardEtudiantIdField.setText(String.valueOf(retardTableModel.getValueAt(modelRow, 3)));
        }
    }

    private void clearEtudiantForm() {
        etudiantIdField.setText("");
        nomField.setText("");
        prenomField.setText("");
        etudiantTable.clearSelection();
    }

    private void clearAbsenceForm() {
        absenceIdField.setText("");
        absenceDateChooser.setDate(null);
        motifField.setText("");
        absenceDureeField.setText("");
        absenceEtudiantIdField.setText("");
        absenceTable.clearSelection();
    }

    private void clearRetardForm() {
        retardIdField.setText("");
        retardDateChooser.setDate(null);
        dureeField.setText("");
        retardEtudiantIdField.setText("");
        retardTable.clearSelection();
    }

    private String getFormattedDate(JDateChooser dateChooser) {
        Date selectedDate = dateChooser.getDate();

        if (selectedDate == null) {
            throw new IllegalArgumentException("Veuillez selectionner une date !");
        }

        return databaseDateFormat.format(selectedDate);
    }

    private void setChooserDate(JDateChooser dateChooser, String databaseDate) {
        try {
            dateChooser.setDate(databaseDateFormat.parse(databaseDate));
        } catch (ParseException e) {
            dateChooser.setDate(null);
        }
    }

    private int parseInt(JTextField field, String fieldName) {
        if (field.getText().trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " est obligatoire.");
        }

        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " doit etre un nombre.");
        }
    }

    private int parsePositiveInt(JTextField field, String fieldName) {
        int value = parseInt(field, fieldName);
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " doit etre superieure a 0.");
        }
        return value;
    }

    private String requireText(JTextField field, String fieldName) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " est obligatoire.");
        }
        return value;
    }

    private int getExistingEtudiantId(JTextField field) throws SQLException {
        int idEtudiant = parseInt(field, "ID etudiant");

        if (etudiantDAO.trouverParId(idEtudiant) == null) {
            throw new IllegalArgumentException("Aucun etudiant trouve avec cet ID.");
        }

        return idEtudiant;
    }

    private String emptyIfNull(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(
                this,
                e.getMessage() == null ? "Une erreur est survenue." : e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
