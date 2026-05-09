package app.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Central theming utility for the Gestion des Absences application.
 */
public final class UITheme {

    public static final Color PRIMARY        = new Color(37, 99, 235);
    public static final Color PRIMARY_DARK   = new Color(30, 64, 175);
    public static final Color PRIMARY_LIGHT  = new Color(147, 197, 253);
    public static final Color HEADER_BG      = new Color(30, 41, 59);
    public static final Color ACCENT         = new Color(14, 165, 233);
    public static final Color SUCCESS        = new Color(22, 163, 74);
    public static final Color WARNING        = new Color(230, 126, 34);
    public static final Color DANGER         = new Color(220, 38, 38);
    public static final Color DANGER_DARK    = new Color(185, 28, 28);

    public static final Color BG_FRAME       = new Color(248, 250, 252);
    public static final Color BG_PANEL       = new Color(255, 255, 255);
    public static final Color BG_ROW_ALT     = new Color(241, 245, 249);

    public static final Color TEXT_PRIMARY   = new Color(15, 23, 42);
    public static final Color TEXT_SECONDARY = new Color(71, 85, 105);
    public static final Color TEXT_WHITE     = Color.WHITE;

    public static final Color BORDER_COLOR   = new Color(226, 232, 240);

    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADER  = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);

    private UITheme() {}

    public static void applyGlobalTheme() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }

        UIManager.put("nimbusBase", PRIMARY);
        UIManager.put("nimbusBlueGrey", new Color(203, 213, 225));
        UIManager.put("control", BG_FRAME);
        UIManager.put("text", TEXT_PRIMARY);

        UIManager.put("Label.font", FONT_BODY);
        UIManager.put("Button.font", FONT_BODY);
        UIManager.put("TextField.font", FONT_BODY);
        UIManager.put("TextArea.font", FONT_BODY);
        UIManager.put("PasswordField.font", FONT_BODY);
        UIManager.put("ComboBox.font", FONT_BODY);
        UIManager.put("Table.font", FONT_BODY);
        UIManager.put("TableHeader.font", FONT_HEADER);
        UIManager.put("TitledBorder.font", FONT_HEADER);
        UIManager.put("TabbedPane.font", FONT_HEADER);
        UIManager.put("TabbedPane.tabInsets", new Insets(6, 16, 6, 16));
        UIManager.put("TabbedPane.background", BG_FRAME);
        UIManager.put("TabbedPane.contentAreaColor", BG_FRAME);
        UIManager.put("TabbedPane.selected", BG_PANEL);
        UIManager.put("TabbedPane.foreground", TEXT_SECONDARY);
        UIManager.put("TabbedPane.selectedForeground", PRIMARY);
        UIManager.put("TabbedPane.borderHightlightColor", BORDER_COLOR);
        UIManager.put("TabbedPane.darkShadow", BORDER_COLOR);
        UIManager.put("TabbedPane.focus", PRIMARY);

        UIManager.put("Table.rowHeight", 26);
        UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
        UIManager.put("Table.showHorizontalLines", Boolean.TRUE);
        UIManager.put("Table.showVerticalLines", Boolean.FALSE);
    }

    public static JButton primaryButton(String text) {
        return makeButton(text, PRIMARY, PRIMARY_DARK, TEXT_WHITE);
    }

    public static JButton secondaryButton(String text) {
        return makeButton(text, new Color(241, 245, 249), new Color(226, 232, 240), TEXT_PRIMARY);
    }

    public static JButton dangerButton(String text) {
        return makeButton(text, DANGER, DANGER_DARK, TEXT_WHITE);
    }

    public static JButton accentButton(String text) {
        return makeButton(text, ACCENT, new Color(0, 130, 108), TEXT_WHITE);
    }

    private static JButton makeButton(String text, Color bg, Color bgHover, Color fg) {
        JButton button = new JButton(text) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(FONT_BODY);
        button.setForeground(fg);
        button.setBackground(bg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(7, 18, 7, 18));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(bgHover); button.repaint(); }
            @Override public void mouseExited(MouseEvent e) { button.setBackground(bg); button.repaint(); }
        });
        return button;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(28);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER_COLOR);
        table.setBackground(BG_PANEL);
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(TEXT_WHITE);
        table.setIntercellSpacing(new Dimension(8, 1));

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_HEADER);
        header.setBackground(HEADER_BG);
        header.setForeground(TEXT_WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 34));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, col);
                label.setFont(FONT_HEADER);
                label.setOpaque(true);
                label.setBackground(HEADER_BG);
                label.setForeground(TEXT_WHITE);
                label.setHorizontalAlignment(JLabel.LEFT);
                label.setBorder(new EmptyBorder(0, 12, 0, 12));
                return label;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? BG_PANEL : BG_ROW_ALT);
                    c.setForeground(TEXT_PRIMARY);
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }

    public static void styleTabs(JTabbedPane tabs) {
        tabs.setFont(FONT_HEADER);
        tabs.setBackground(BG_FRAME);
        tabs.setForeground(TEXT_SECONDARY);
        tabs.setOpaque(true);
        tabs.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
    }

    public static DefaultTableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    String v = value == null ? "" : value.toString();
                    if ("Acceptee".equalsIgnoreCase(v)) {
                        c.setBackground(new Color(220, 255, 230));
                        c.setForeground(new Color(20, 130, 50));
                    } else if ("Refusee".equalsIgnoreCase(v)) {
                        c.setBackground(new Color(255, 225, 220));
                        c.setForeground(DANGER);
                    } else {
                        c.setBackground(new Color(255, 248, 210));
                        c.setForeground(new Color(140, 90, 0));
                    }
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                ((JLabel) c).setHorizontalAlignment(CENTER);
                return c;
            }
        };
    }

    public static DefaultTableCellRenderer absenceRowRenderer(int dureeCol) {
        return new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    Object dureeVal = t.getValueAt(row, dureeCol);
                    int duree = 0;
                    try {
                        duree = Integer.parseInt(dureeVal == null ? "0" : dureeVal.toString());
                    } catch (Exception ignored) {
                    }
                    if (duree >= 8) {
                        c.setBackground(new Color(255, 230, 228));
                        c.setForeground(DANGER);
                    } else if (duree >= 4) {
                        c.setBackground(new Color(255, 245, 220));
                        c.setForeground(new Color(140, 80, 0));
                    } else {
                        c.setBackground(row % 2 == 0 ? BG_PANEL : BG_ROW_ALT);
                        c.setForeground(TEXT_PRIMARY);
                    }
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        };
    }

    public static JSeparator separator() {
        JSeparator separator = new JSeparator();
        separator.setForeground(BORDER_COLOR);
        return separator;
    }

    public static JPanel sectionPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_PANEL);
        javax.swing.border.TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true), title);
        border.setTitleFont(FONT_HEADER);
        border.setTitleColor(PRIMARY);
        panel.setBorder(border);
        return panel;
    }

    public static JPanel statBadge(String label, String value, Color bg) {
        JPanel panel = new JPanel(new BorderLayout(2, 2));
        panel.setBackground(bg);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1, true),
                new EmptyBorder(8, 14, 8, 14)));

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(FONT_SMALL);
        labelComponent.setForeground(new Color(40, 40, 40));

        JLabel valueComponent = new JLabel(value, JLabel.CENTER);
        valueComponent.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueComponent.setForeground(bg.darker().darker());

        panel.add(labelComponent, BorderLayout.NORTH);
        panel.add(valueComponent, BorderLayout.CENTER);
        return panel;
    }
}
