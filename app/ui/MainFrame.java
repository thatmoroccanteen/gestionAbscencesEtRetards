package app.ui;

import app.Session;
import app.dao.AbsenceDAO;
import app.dao.EtudiantDAO;
import app.dao.JustificationDAO;
import app.dao.RetardDAO;
import app.model.*;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final SimpleDateFormat databaseDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private final EtudiantDAO etudiantDAO = new EtudiantDAO();
    private final AbsenceDAO absenceDAO = new AbsenceDAO();
    private final RetardDAO retardDAO = new RetardDAO();
    private final JustificationDAO justificationDAO = new JustificationDAO();

    // ---- Etudiants ----
    private final JTextField etudiantIdField     = new JTextField(8);
    private final JTextField nomField            = new JTextField(18);
    private final JTextField prenomField         = new JTextField(18);
    private final DefaultTableModel etudiantTableModel = nonEditableModel("ID","Nom","Prenom");
    private final JTable etudiantTable = new JTable(etudiantTableModel);

    // ---- Absences ----
    private final JTextField absenceIdField         = new JTextField(8);
    private final JDateChooser absenceDateChooser   = createDateChooser();
    private final JTextField motifField             = new JTextField(18);
    private final JTextField absenceDureeField      = new JTextField(8);
    private final JTextField absenceEtudiantIdField = new JTextField(8);
    private final DefaultTableModel absenceTableModel = nonEditableModel(
            "ID","Date","Motif","Duree","ID etudiant","Nom","Prenom");
    private final JTable absenceTable = new JTable(absenceTableModel);

    // ---- Retards ----
    private final JTextField retardIdField          = new JTextField(8);
    private final JDateChooser retardDateChooser    = createDateChooser();
    private final JTextField dureeField             = new JTextField(8);
    private final JTextField retardEtudiantIdField  = new JTextField(8);
    private final DefaultTableModel retardTableModel = nonEditableModel(
            "ID","Date","Duree","ID etudiant","Nom","Prenom");
    private final JTable retardTable = new JTable(retardTableModel);

    // ---- Justifications ----
    private final JComboBox<AbsenceChoice> justifAbsenceCombo = new JComboBox<>();
    private final JTextArea  justifRaisonArea     = new JTextArea(3, 25);
    private final DefaultTableModel justifTableModel = nonEditableModel(
            "ID Just.","ID Absence","Date Absence","Motif Absence","Etudiant","Raison","Statut");
    private final JTable justifTable = new JTable(justifTableModel);

    public MainFrame() {
        databaseDateFormat.setLenient(false);

        Utilisateur user = Session.getCurrentUser();
        String title = "Gestion des absences";
        if (user != null) title += "  -  " + user.getUsername() + " (" + user.getRole() + ")";
        setTitle(title);
        setSize(950, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        UITheme.styleTabs(tabs);

        if (Session.isEnseignantOrAdmin()) {
            tabs.addTab("Etudiants",  createEtudiantsPanel());
            tabs.addTab("Absences",   createAbsencesPanel());
            tabs.addTab("Retards",    createRetardsPanel());
        }
        if (Session.isEtudiant()) {
            tabs.addTab("Mes Absences", createMesAbsencesPanel());
        }
        tabs.addTab("Justifications", createJustificationsPanel());

        add(tabs, BorderLayout.CENTER);
        add(createTopBar(), BorderLayout.NORTH);

        refreshAllTables();
    }

    // =====================================================================
    // TOP BAR
    // =====================================================================
    private JPanel createTopBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 8, 0, 8));
        panel.setBackground(UITheme.BG_FRAME);

        if (Session.isEnseignantOrAdmin()) {
            JButton totauxBtn = UITheme.secondaryButton("Total par etudiant");
            totauxBtn.addActionListener(e -> new TotauxFrame().setVisible(true));
            panel.add(totauxBtn);
        }

        if (Session.isAdmin()) {
            JButton usersBtn = UITheme.secondaryButton("Gerer utilisateurs");
            usersBtn.addActionListener(e -> new GestionUtilisateursFrame().setVisible(true));
            panel.add(usersBtn);
        }

        JButton logoutBtn = UITheme.dangerButton("Deconnexion");
        logoutBtn.addActionListener(e -> {
            Session.logout();
            dispose();
            LoginFrame login = new LoginFrame(null);
            login.setVisible(true);
            if (login.isLoginSuccess()) {
                new MainFrame().setVisible(true);
            }
        });
        panel.add(logoutBtn);
        return panel;
    }

    // =====================================================================
    // ETUDIANTS
    // =====================================================================
    private JPanel createEtudiantsPanel() {
        etudiantIdField.setEditable(false);
        configureTable(etudiantTable);
        etudiantTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillEtudiantFormFromSelection();
        });

        JPanel form = formPanel();
        addRow(form, 0, "Nom",    nomField);
        addRow(form, 1, "Prenom", prenomField);

        JPanel buttons = buttonPanel(
                btn("Ajouter",    e -> addEtudiant()),
                btn("Modifier",   e -> updateEtudiant()),
                btn("Supprimer",  e -> deleteEtudiant()),
                btn("Actualiser", e -> refreshEtudiants())
        );
        return crudPanel(form, buttons, etudiantTable);
    }

    // =====================================================================
    // ABSENCES (for teacher/admin)
    // =====================================================================
    private JPanel createAbsencesPanel() {
        absenceIdField.setEditable(false);
        configureTable(absenceTable);
        absenceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillAbsenceFormFromSelection();
        });

        JPanel form = formPanel();
        addRow(form, 0, "Date",         absenceDateChooser);
        addRow(form, 1, "Motif",        motifField);
        addRow(form, 2, "Duree (h)",    absenceDureeField);
        addRow(form, 3, "ID etudiant",  absenceEtudiantIdField);

        JPanel buttons = buttonPanel(
                btn("Ajouter",    e -> addAbsence()),
                btn("Modifier",   e -> updateAbsence()),
                btn("Supprimer",  e -> deleteAbsence()),
                btn("Actualiser", e -> refreshAbsences())
        );
        return crudPanel(form, buttons, absenceTable);
    }

    // =====================================================================
    // MES ABSENCES (for student — read-only)
    // =====================================================================
    private JPanel createMesAbsencesPanel() {
        configureTable(absenceTable);

        JPanel buttons = buttonPanel(btn("Actualiser", e -> refreshAbsences()));

        JPanel top = new JPanel(new BorderLayout(8,8));
        top.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));
        top.setBackground(UITheme.BG_PANEL);
        JLabel label = new JLabel("Vos absences enregistrees :");
        label.setFont(UITheme.FONT_HEADER);
        label.setForeground(UITheme.PRIMARY_DARK);
        top.add(label, BorderLayout.NORTH);
        top.add(buttons, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout(8,8));
        content.setBackground(UITheme.BG_FRAME);
        content.add(top, BorderLayout.NORTH);
        content.add(new JScrollPane(absenceTable), BorderLayout.CENTER);
        content.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        return content;
    }

    // =====================================================================
    // RETARDS
    // =====================================================================
    private JPanel createRetardsPanel() {
        retardIdField.setEditable(false);
        configureTable(retardTable);
        retardTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillRetardFormFromSelection();
        });

        JPanel form = formPanel();
        addRow(form, 0, "Date",         retardDateChooser);
        addRow(form, 1, "Duree (min)",  dureeField);
        addRow(form, 2, "ID etudiant",  retardEtudiantIdField);

        JPanel buttons = buttonPanel(
                btn("Ajouter",    e -> addRetard()),
                btn("Modifier",   e -> updateRetard()),
                btn("Supprimer",  e -> deleteRetard()),
                btn("Actualiser", e -> refreshRetards())
        );
        return crudPanel(form, buttons, retardTable);
    }

    // =====================================================================
    // JUSTIFICATIONS
    // =====================================================================
    private JPanel createJustificationsPanel() {
        configureTable(justifTable);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(UITheme.BG_FRAME);

        // Top: submission form (etudiant) or action buttons (enseignant/admin)
        if (Session.isEtudiant()) {
            panel.add(createJustifSubmitPanel(), BorderLayout.NORTH);
        } else {
            panel.add(createJustifActionPanel(), BorderLayout.NORTH);
        }

        panel.add(new JScrollPane(justifTable), BorderLayout.CENTER);
        return panel;
    }

    /** Panel used by student to submit a new justification */
    private JPanel createJustifSubmitPanel() {
        JPanel form = formPanel();
        form.setBorder(BorderFactory.createTitledBorder("Soumettre une justification"));
        justifAbsenceCombo.setPreferredSize(new Dimension(320, 24));
        justifRaisonArea.setLineWrap(true);
        justifRaisonArea.setWrapStyleWord(true);
        addRow(form, 0, "Absence", justifAbsenceCombo);
        addRow(form, 1, "Raison",     new JScrollPane(justifRaisonArea));

        JPanel buttons = buttonPanel(
                btn("Soumettre",  e -> soumettreJustification()),
                btn("Actualiser", e -> refreshJustificationScreen())
        );

        JPanel top = new JPanel(new BorderLayout(4,4));
        top.setOpaque(false);
        top.add(form, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.SOUTH);
        return top;
    }

    /** Panel used by teacher/admin to accept or refuse a justification */
    private JPanel createJustifActionPanel() {
        JLabel hint = new JLabel("Selectionnez une justification puis choisissez une action :");
        hint.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        hint.setForeground(UITheme.TEXT_SECONDARY);

        JPanel buttons = buttonPanel(
                btn("Accepter",   e -> changerStatutJustif("Acceptee")),
                btn("Refuser",    e -> changerStatutJustif("Refusee")),
                btn("Supprimer",  e -> supprimerJustification()),
                btn("Actualiser", e -> refreshJustifications())
        );

        JPanel top = new JPanel(new BorderLayout(4,4));
        top.setOpaque(false);
        top.add(hint, BorderLayout.NORTH);
        top.add(buttons, BorderLayout.CENTER);
        top.setBorder(BorderFactory.createEmptyBorder(6,4,6,4));
        return top;
    }

    private void soumettreJustification() {
        try {
            AbsenceChoice choice = (AbsenceChoice) justifAbsenceCombo.getSelectedItem();
            if (choice == null) throw new IllegalArgumentException("Selectionnez une absence.");
            if (!choice.isSelectable()) throw new IllegalArgumentException(choice.label);
            int idAbsence = choice.id;
            String raison = justifRaisonArea.getText().trim();
            if (raison.isEmpty()) throw new IllegalArgumentException("La raison est obligatoire.");

            // Verify the absence belongs to this student
            Absence abs = absenceDAO.trouverParId(idAbsence);
            if (abs == null) throw new IllegalArgumentException("Absence introuvable.");
            int idEtudiantSession = Session.getCurrentUser().getIdEtudiant();
            if (abs.getIdEtudiant() != idEtudiantSession)
                throw new IllegalArgumentException("Cette absence ne vous appartient pas.");

            justificationDAO.ajouter(new Justification(idAbsence, raison));
            justifRaisonArea.setText("");
            refreshJustificationScreen();
            JOptionPane.showMessageDialog(this,
                    "Justification soumise. En attente de validation.",
                    "Succes", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) { showError(e); }
    }

    private void changerStatutJustif(String statut) {
        int row = justifTable.getSelectedRow();
        if (row < 0) { showError(new IllegalArgumentException("Selectionnez une justification.")); return; }
        int mr = justifTable.convertRowIndexToModel(row);
        int id = (int) justifTableModel.getValueAt(mr, 0);
        try {
            justificationDAO.mettreAJourStatut(id, statut);
            refreshJustifications();
        } catch (Exception e) { showError(e); }
    }

    private void supprimerJustification() {
        int row = justifTable.getSelectedRow();
        if (row < 0) { showError(new IllegalArgumentException("Selectionnez une justification.")); return; }
        int mr = justifTable.convertRowIndexToModel(row);
        int id = (int) justifTableModel.getValueAt(mr, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Supprimer cette justification ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                justificationDAO.supprimer(id);
                refreshJustifications();
            } catch (SQLException e) { showError(e); }
        }
    }

    // =====================================================================
    // CRUD OPERATIONS
    // =====================================================================
    private void addEtudiant() {
        try {
            etudiantDAO.ajouter(new Etudiant(requireText(nomField,"Nom"), requireText(prenomField,"Prenom")));
            clearEtudiantForm(); refreshEtudiants();
        } catch (Exception e) { showError(e); }
    }

    private void updateEtudiant() {
        try {
            etudiantDAO.modifier(new Etudiant(parseInt(etudiantIdField,"ID"),
                    requireText(nomField,"Nom"), requireText(prenomField,"Prenom")));
            clearEtudiantForm(); refreshEtudiants();
        } catch (Exception e) { showError(e); }
    }

    private void deleteEtudiant() {
        try {
            etudiantDAO.supprimer(parseInt(etudiantIdField,"ID etudiant"));
            clearEtudiantForm(); refreshAllTables();
        } catch (Exception e) { showError(e); }
    }

    private void addAbsence() {
        try {
            absenceDAO.ajouter(new Absence(getFormattedDate(absenceDateChooser),
                    requireText(motifField,"Motif"),
                    parsePositiveInt(absenceDureeField,"Duree"),
                    getExistingEtudiantId(absenceEtudiantIdField)));
            clearAbsenceForm(); refreshAbsences();
        } catch (Exception e) { showError(e); }
    }

    private void updateAbsence() {
        try {
            absenceDAO.modifier(new Absence(parseInt(absenceIdField,"ID absence"),
                    getFormattedDate(absenceDateChooser),
                    requireText(motifField,"Motif"),
                    parsePositiveInt(absenceDureeField,"Duree"),
                    getExistingEtudiantId(absenceEtudiantIdField)));
            clearAbsenceForm(); refreshAbsences();
        } catch (Exception e) { showError(e); }
    }

    private void deleteAbsence() {
        try {
            absenceDAO.supprimer(parseInt(absenceIdField,"ID absence"));
            clearAbsenceForm(); refreshAbsences();
        } catch (Exception e) { showError(e); }
    }

    private void addRetard() {
        try {
            retardDAO.ajouter(new Retard(getFormattedDate(retardDateChooser),
                    parsePositiveInt(dureeField,"Duree"),
                    getExistingEtudiantId(retardEtudiantIdField)));
            clearRetardForm(); refreshRetards();
        } catch (Exception e) { showError(e); }
    }

    private void updateRetard() {
        try {
            retardDAO.modifier(new Retard(parseInt(retardIdField,"ID retard"),
                    getFormattedDate(retardDateChooser),
                    parsePositiveInt(dureeField,"Duree"),
                    getExistingEtudiantId(retardEtudiantIdField)));
            clearRetardForm(); refreshRetards();
        } catch (Exception e) { showError(e); }
    }

    private void deleteRetard() {
        try {
            retardDAO.supprimer(parseInt(retardIdField,"ID retard"));
            clearRetardForm(); refreshRetards();
        } catch (Exception e) { showError(e); }
    }

    // =====================================================================
    // REFRESH
    // =====================================================================
    private void refreshAllTables() {
        refreshEtudiants();
        refreshAbsences();
        refreshRetards();
        refreshJustificationScreen();
    }

    private void refreshEtudiants() {
        try {
            etudiantTableModel.setRowCount(0);
            for (Etudiant e : etudiantDAO.listerTous())
                etudiantTableModel.addRow(new Object[]{e.getId(), e.getNom(), e.getPrenom()});
        } catch (Exception e) { showError(e); }
    }

    private void refreshAbsences() {
        try {
            absenceTableModel.setRowCount(0);
            List<AbsenceView> list;
            if (Session.isEtudiant()) {
                int idEtu = Session.getCurrentUser().getIdEtudiant();
                if (idEtu <= 0) {
                    throw new IllegalStateException("Votre compte etudiant n'est lie a aucun ID etudiant.");
                }
                list = absenceDAO.listerParEtudiant(idEtu);
            } else {
                list = absenceDAO.listerTousAvecEtudiant();
            }
            for (AbsenceView a : list)
                absenceTableModel.addRow(new Object[]{a.getId(), a.getDate(), a.getMotif(),
                        a.getDuree(), a.getIdEtudiant(),
                        emptyIfNull(a.getNomEtudiant()), emptyIfNull(a.getPrenomEtudiant())});
        } catch (Exception e) { showError(e); }
    }

    private void refreshRetards() {
        try {
            retardTableModel.setRowCount(0);
            for (RetardView r : retardDAO.listerTousAvecEtudiant())
                retardTableModel.addRow(new Object[]{r.getId(), r.getDate(), r.getDuree(),
                        r.getIdEtudiant(), emptyIfNull(r.getNomEtudiant()), emptyIfNull(r.getPrenomEtudiant())});
        } catch (SQLException e) { showError(e); }
    }

    private void refreshJustifications() {
        try {
            justifTableModel.setRowCount(0);
            List<JustificationView> list;
            if (Session.isEtudiant()) {
                list = justificationDAO.listerParEtudiant(Session.getCurrentUser().getIdEtudiant());
            } else {
                list = justificationDAO.listerTous();
            }
            for (JustificationView j : list) {
                String etudiant = emptyIfNull(j.getNomEtudiant()) + " " + emptyIfNull(j.getPrenomEtudiant());
                justifTableModel.addRow(new Object[]{
                        j.getId(), j.getIdAbsence(), j.getDateAbsence(),
                        j.getMotifAbsence(), etudiant.trim(), j.getRaison(), j.getStatut()
                });
            }
        } catch (SQLException e) { showError(e); }
    }

    private void refreshJustificationScreen() {
        refreshJustificationAbsenceChoices();
        refreshJustifications();
    }

    private void refreshJustificationAbsenceChoices() {
        if (!Session.isEtudiant()) return;

        try {
            justifAbsenceCombo.removeAllItems();

            int idEtudiant = Session.getCurrentUser().getIdEtudiant();
            if (idEtudiant <= 0) {
                justifAbsenceCombo.addItem(AbsenceChoice.placeholder(
                        "Votre compte etudiant n'est lie a aucun ID etudiant."));
                return;
            }

            Set<Integer> justifiedAbsences = new HashSet<>();
            for (JustificationView j : justificationDAO.listerParEtudiant(idEtudiant)) {
                justifiedAbsences.add(j.getIdAbsence());
            }

            int availableCount = 0;
            for (AbsenceView absence : absenceDAO.listerParEtudiant(idEtudiant)) {
                if (!justifiedAbsences.contains(absence.getId())) {
                    justifAbsenceCombo.addItem(new AbsenceChoice(absence));
                    availableCount++;
                }
            }

            if (availableCount == 0) {
                justifAbsenceCombo.addItem(AbsenceChoice.placeholder(
                        "Aucune absence disponible a justifier."));
            }
        } catch (SQLException e) { showError(e); }
    }

    // =====================================================================
    // FORM FILL FROM SELECTION
    // =====================================================================
    private void fillEtudiantFormFromSelection() {
        int row = etudiantTable.getSelectedRow();
        if (row >= 0) {
            int mr = etudiantTable.convertRowIndexToModel(row);
            etudiantIdField.setText(String.valueOf(etudiantTableModel.getValueAt(mr,0)));
            nomField.setText(String.valueOf(etudiantTableModel.getValueAt(mr,1)));
            prenomField.setText(String.valueOf(etudiantTableModel.getValueAt(mr,2)));
        }
    }

    private void fillAbsenceFormFromSelection() {
        int row = absenceTable.getSelectedRow();
        if (row >= 0) {
            int mr = absenceTable.convertRowIndexToModel(row);
            absenceIdField.setText(String.valueOf(absenceTableModel.getValueAt(mr,0)));
            setChooserDate(absenceDateChooser, String.valueOf(absenceTableModel.getValueAt(mr,1)));
            motifField.setText(String.valueOf(absenceTableModel.getValueAt(mr,2)));
            absenceDureeField.setText(String.valueOf(absenceTableModel.getValueAt(mr,3)));
            absenceEtudiantIdField.setText(String.valueOf(absenceTableModel.getValueAt(mr,4)));
        }
    }

    private void fillRetardFormFromSelection() {
        int row = retardTable.getSelectedRow();
        if (row >= 0) {
            int mr = retardTable.convertRowIndexToModel(row);
            retardIdField.setText(String.valueOf(retardTableModel.getValueAt(mr,0)));
            setChooserDate(retardDateChooser, String.valueOf(retardTableModel.getValueAt(mr,1)));
            dureeField.setText(String.valueOf(retardTableModel.getValueAt(mr,2)));
            retardEtudiantIdField.setText(String.valueOf(retardTableModel.getValueAt(mr,3)));
        }
    }

    // =====================================================================
    // CLEAR FORMS
    // =====================================================================
    private void clearEtudiantForm() {
        etudiantIdField.setText(""); nomField.setText(""); prenomField.setText("");
        etudiantTable.clearSelection();
    }

    private void clearAbsenceForm() {
        absenceIdField.setText(""); absenceDateChooser.setDate(null);
        motifField.setText(""); absenceDureeField.setText(""); absenceEtudiantIdField.setText("");
        absenceTable.clearSelection();
    }

    private void clearRetardForm() {
        retardIdField.setText(""); retardDateChooser.setDate(null);
        dureeField.setText(""); retardEtudiantIdField.setText("");
        retardTable.clearSelection();
    }

    // =====================================================================
    // HELPERS
    // =====================================================================
    private JPanel crudPanel(JPanel form, JPanel buttons, JTable table) {
        JPanel top = new JPanel(new BorderLayout(10,10));
        top.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        top.setBackground(UITheme.BG_PANEL);
        top.add(form, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout(10,10));
        content.setBackground(UITheme.BG_FRAME);
        content.add(top, BorderLayout.NORTH);
        content.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BG_FRAME);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = gbc.weighty = 1.0;
        gbc.insets = new Insets(18,18,18,18);
        panel.add(content, gbc);
        return panel;
    }

    private JPanel formPanel() {
        return UITheme.sectionPanel("Formulaire");
    }

    private void addRow(JPanel panel, int row, String label, Component field) {
        GridBagConstraints lc = new GridBagConstraints();
        lc.gridx=0; lc.gridy=row; lc.insets=new Insets(5,5,5,8); lc.anchor=GridBagConstraints.EAST;
        panel.add(new JLabel(label, SwingConstants.RIGHT), lc);

        GridBagConstraints fc = new GridBagConstraints();
        fc.gridx=1; fc.gridy=row; fc.insets=new Insets(5,5,5,5); fc.anchor=GridBagConstraints.WEST;
        panel.add(field, fc);
    }

    private JPanel buttonPanel(JButton... btns) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setOpaque(false);
        for (JButton b : btns) p.add(b);
        return p;
    }

    private JButton btn(String text, ActionListener l) {
        JButton b;
        if ("Supprimer".equals(text) || "Refuser".equals(text)) {
            b = UITheme.dangerButton(text);
        } else if ("Ajouter".equals(text) || "Modifier".equals(text)
                || "Accepter".equals(text) || "Soumettre".equals(text)) {
            b = UITheme.primaryButton(text);
        } else if ("Actualiser".equals(text)) {
            b = UITheme.accentButton(text);
        } else {
            b = UITheme.secondaryButton(text);
        }
        b.addActionListener(l);
        return b;
    }

    private void configureTable(JTable table) {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        UITheme.styleTable(table);
        if (table == absenceTable) {
            table.setDefaultRenderer(Object.class, UITheme.absenceRowRenderer(3));
        } else if (table == justifTable) {
            table.getColumnModel().getColumn(6).setCellRenderer(UITheme.statusRenderer());
        }
    }

    private JDateChooser createDateChooser() {
        JDateChooser dc = new JDateChooser();
        dc.setDateFormatString("dd/MM/yyyy");
        dc.setPreferredSize(new Dimension(150,24));
        return dc;
    }

    private static DefaultTableModel nonEditableModel(String... cols) {
        return new DefaultTableModel(cols, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private static class AbsenceChoice {
        private final int id;
        private final String label;

        AbsenceChoice(AbsenceView absence) {
            id = absence.getId();
            label = "#" + absence.getId() + " - " + absence.getDate() + " - " + absence.getMotif();
        }

        private AbsenceChoice(int id, String label) {
            this.id = id;
            this.label = label;
        }

        static AbsenceChoice placeholder(String label) {
            return new AbsenceChoice(0, label);
        }

        boolean isSelectable() {
            return id > 0;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private String getFormattedDate(JDateChooser dc) {
        Date d = dc.getDate();
        if (d == null) throw new IllegalArgumentException("Veuillez selectionner une date !");
        return databaseDateFormat.format(d);
    }

    private void setChooserDate(JDateChooser dc, String databaseDate) {
        try { dc.setDate(databaseDateFormat.parse(databaseDate)); }
        catch (ParseException e) { dc.setDate(null); }
    }

    private int parseInt(JTextField field, String name) {
        if (field.getText().trim().isEmpty())
            throw new IllegalArgumentException(name + " est obligatoire.");
        try { return Integer.parseInt(field.getText().trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException(name + " doit etre un nombre."); }
    }

    private int parsePositiveInt(JTextField field, String name) {
        int v = parseInt(field, name);
        if (v <= 0) throw new IllegalArgumentException(name + " doit etre superieure a 0.");
        return v;
    }

    private String requireText(JTextField field, String name) {
        String v = field.getText().trim();
        if (v.isEmpty()) throw new IllegalArgumentException(name + " est obligatoire.");
        return v;
    }

    private int getExistingEtudiantId(JTextField field) throws SQLException {
        int id = parseInt(field, "ID etudiant");
        if (etudiantDAO.trouverParId(id) == null)
            throw new IllegalArgumentException("Aucun etudiant trouve avec cet ID.");
        return id;
    }

    private String emptyIfNull(String v) { return v == null ? "" : v; }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this,
                e.getMessage() == null ? "Une erreur est survenue." : e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}
