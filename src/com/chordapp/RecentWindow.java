package com.chordapp;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * 最近登録・編集したコード進行を表示するウィンドウ（上位5件）
 */
public class RecentWindow extends JDialog {

    private MainMenuWindow parent;
    private ChordProgressionRepository repo = ChordProgressionRepository.getInstance();
    private AppSettings settings = AppSettings.getInstance();
    private JPanel listPanel;

    public RecentWindow(MainMenuWindow parent) {
        super(parent, "最近登録・編集したコード進行", true);
        this.parent = parent;
        setSize(860, 560);
        setLocationRelativeTo(parent);
        setResizable(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.BG_DARK);
        setContentPane(root);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildContent(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(AppTheme.BG_PANEL);
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(AppTheme.ACCENT2);
                g2.fillRect(0,getHeight()-2,getWidth(),2);
                g2.dispose();
            }
        };
        h.setPreferredSize(new Dimension(860, 64));
        h.setBorder(new EmptyBorder(14, 24, 14, 24));

        JLabel title = new JLabel("♬  最近登録・編集したコード進行（上位5件）");
        title.setFont(AppTheme.titleFont(18));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        h.add(title, BorderLayout.WEST);
        return h;
    }

    private JScrollPane buildContent() {
        listPanel = new JPanel();
        listPanel.setBackground(AppTheme.BG_DARK);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(new EmptyBorder(16, 24, 16, 24));
        refreshList();

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBackground(AppTheme.BG_DARK);
        scroll.getViewport().setBackground(AppTheme.BG_DARK);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    public void refreshList() {
        listPanel.removeAll();
        List<ChordProgression> recents = repo.getRecentlyUsed(5);

        if (recents.isEmpty()) {
            JLabel empty = new JLabel("登録されたコード進行がありません", SwingConstants.CENTER);
            empty.setFont(AppTheme.bodyFont(14));
            empty.setForeground(AppTheme.TEXT_MUTED);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(Box.createVerticalGlue());
            listPanel.add(empty);
            listPanel.add(Box.createVerticalGlue());
        } else {
            for (ChordProgression cp : recents) {
                listPanel.add(buildCard(cp));
                listPanel.add(Box.createVerticalStrut(10));
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel buildCard(ChordProgression cp) {
        JPanel card = new JPanel(new BorderLayout(16, 0)) {
            boolean hovered = false;
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? AppTheme.BG_CARD_HOVER : AppTheme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),AppTheme.RADIUS,AppTheme.RADIUS));
                g2.setColor(AppTheme.ACCENT2);
                g2.fill(new RoundRectangle2D.Float(0,0,5,getHeight(),4,4));
                g2.dispose();
            }
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                });
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 20, 14, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Info
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(cp.getTitle());
        titleLabel.setFont(AppTheme.titleFont(15));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);

        String chordsDisplay = settings.getNoteMode() == AppSettings.NoteMode.DEGREE
            ? String.join(" → ", DegreeConverter.convertList(cp.getChords(), cp.getKey()))
            : cp.getChordsAsString();
        JLabel chordsLabel = new JLabel(chordsDisplay);
        chordsLabel.setFont(AppTheme.monoFont(12));
        chordsLabel.setForeground(AppTheme.ACCENT_LIGHT);

        // 役割表示
        StringBuilder funcSb = new StringBuilder("役割: ");
        List<String> chordListF = cp.getChords();
        for (int i = 0; i < chordListF.size(); i++) {
            if (i > 0) funcSb.append("  ");
            String func = ChordTransposer.getChordFunction(chordListF.get(i), cp.getKey());
            String color = func.equals("T") ? "#64c8ff" : func.equals("SD") ? "#64e096" : func.equals("D") ? "#ff9664" : "#888888";
            funcSb.append("<font color='").append(color).append("'>")
                  .append(chordListF.get(i)).append("[").append(func.isEmpty() ? "?" : func).append("]")
                  .append("</font>");
        }
        JLabel funcLabel = new JLabel("<html>" + funcSb + "</html>");
        funcLabel.setFont(AppTheme.bodyFont(11));

        // 星
        StringBuilder starSb = new StringBuilder();
        for (int i = 1; i <= 5; i++) starSb.append(i <= cp.getRating() ? "★" : "☆");
        JLabel starLabel = new JLabel(starSb.toString());
        starLabel.setFont(AppTheme.bodyFont(12));
        starLabel.setForeground(new Color(255, 200, 50));

        JLabel metaLabel = new JLabel("BPM: " + cp.getTempo() + "  |  " + cp.getFormattedLastUsed());
        metaLabel.setFont(AppTheme.bodyFont(11));
        metaLabel.setForeground(AppTheme.TEXT_MUTED);

        info.add(titleLabel);
        info.add(Box.createVerticalStrut(2));
        info.add(starLabel);
        info.add(Box.createVerticalStrut(2));
        info.add(chordsLabel);
        info.add(Box.createVerticalStrut(2));
        info.add(funcLabel);
        info.add(Box.createVerticalStrut(2));
        info.add(metaLabel);

        // ボタン
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btns.setOpaque(false);

        JButton editBtn = buildBtn("編集", AppTheme.ACCENT, Color.WHITE);
        editBtn.addActionListener(e -> {
            dispose();
            new ManageWindow(parent).setVisible(true);
        });

        btns.add(editBtn);

        card.add(info, BorderLayout.CENTER);
        card.add(btns, BorderLayout.EAST);
        return card;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(AppTheme.BG_PANEL);
        footer.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0,AppTheme.BORDER),
            new EmptyBorder(12,24,12,24)));
        JLabel count = new JLabel("最近の " + Math.min(repo.getAll().size(), 5) + " 件を表示");
        count.setFont(AppTheme.bodyFont(12));
        count.setForeground(AppTheme.TEXT_MUTED);
        footer.add(count, BorderLayout.WEST);
        JButton closeBtn = buildBtn("閉じる", AppTheme.ACCENT2, Color.WHITE);
        closeBtn.addActionListener(e -> dispose());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(closeBtn);
        footer.add(btnRow, BorderLayout.EAST);
        return footer;
    }

    private JButton buildBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),6,6));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(AppTheme.bodyFont(12));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(new EmptyBorder(6,14,6,14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
