package com.chordapp;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class ListWindow extends JDialog {

    private ChordProgressionRepository repo = ChordProgressionRepository.getInstance();
    private AppSettings settings = AppSettings.getInstance();
    private JPanel listPanel;
    private JTextField searchField;
    private JComboBox<String> sortCombo;
    private JRadioButton sortAscRadio;

    public ListWindow(MainMenuWindow parent) {
        super(parent, "コード進行表示", true);
        setSize(900, 660);
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
        JPanel h = new JPanel(new BorderLayout(16, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(AppTheme.BG_PANEL);
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(AppTheme.ACCENT2);
                g2.fillRect(0,getHeight()-2,getWidth(),2);
                g2.dispose();
            }
        };
        h.setPreferredSize(new Dimension(900,64));
        h.setBorder(new EmptyBorder(12,24,12,24));

        JLabel title = new JLabel("♬  コード進行一覧");
        title.setFont(AppTheme.titleFont(18));
        title.setForeground(AppTheme.TEXT_PRIMARY);

        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBar.setOpaque(false);

        // ソートコンボ + 昇順・降順
        sortCombo = new JComboBox<>(new String[]{"登録順", "レーティング順", "最終使用順"});
        sortCombo.setFont(AppTheme.bodyFont(12));
        sortCombo.setBackground(AppTheme.BG_CARD);
        sortCombo.setForeground(AppTheme.TEXT_PRIMARY);
        sortCombo.setPreferredSize(new Dimension(130, 34));
        sortCombo.addActionListener(e -> refreshList());

        JRadioButton ascRadio = new JRadioButton("昇順");
        JRadioButton descRadio = new JRadioButton("降順");
        ascRadio.setFont(AppTheme.bodyFont(12));
        descRadio.setFont(AppTheme.bodyFont(12));
        ascRadio.setForeground(AppTheme.TEXT_PRIMARY);
        descRadio.setForeground(AppTheme.TEXT_PRIMARY);
        ascRadio.setOpaque(false);
        descRadio.setOpaque(false);
        ascRadio.setFocusPainted(false);
        descRadio.setFocusPainted(false);
        descRadio.setSelected(true);
        ButtonGroup orderGroup = new ButtonGroup();
        orderGroup.add(ascRadio);
        orderGroup.add(descRadio);
        ascRadio.addActionListener(e -> refreshList());
        descRadio.addActionListener(e -> refreshList());

        // sortOrderAsc フィールドとして保持
        this.sortAscRadio = ascRadio;

        // 検索バー
        JPanel searchBar = new JPanel(new BorderLayout());
        searchBar.setOpaque(false);
        searchField = new JTextField();
        searchField.setFont(AppTheme.bodyFont(13));
        searchField.setForeground(AppTheme.TEXT_MUTED);
        searchField.setBackground(AppTheme.BG_CARD);
        searchField.setCaretColor(AppTheme.ACCENT_LIGHT);
        searchField.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
            new EmptyBorder(6, 10, 6, 10)));
        searchField.setText("検索（タイトル・キー・コード）");
        searchField.setPreferredSize(new Dimension(260, 36));
        searchField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (searchField.getText().startsWith("検索")) { searchField.setText(""); searchField.setForeground(AppTheme.TEXT_PRIMARY); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) { searchField.setText("検索（タイトル・キー・コード）"); searchField.setForeground(AppTheme.TEXT_MUTED); }
            }
        });
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { refreshList(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { refreshList(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshList(); }
        });
        searchBar.add(searchField, BorderLayout.CENTER);

        rightBar.add(sortCombo);
        rightBar.add(ascRadio);
        rightBar.add(descRadio);
        rightBar.add(searchBar);

        h.add(title, BorderLayout.WEST);
        h.add(rightBar, BorderLayout.EAST);
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
        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        boolean searching = !query.isEmpty() && !query.startsWith("検索");

        List<ChordProgression> all = repo.getAll();

        // ソート
        int sortIdx = sortCombo != null ? sortCombo.getSelectedIndex() : 0;
        boolean asc = sortAscRadio != null && sortAscRadio.isSelected();
        if (sortIdx == 0 && asc) {
            java.util.Collections.reverse(all);
        } else if (sortIdx == 1) {
            all.sort((a, b) -> asc
                ? Integer.compare(a.getRating(), b.getRating())
                : Integer.compare(b.getRating(), a.getRating()));
        } else if (sortIdx == 2) {
            all.sort((a, b) -> asc
                ? a.getLastUsed().compareTo(b.getLastUsed())
                : b.getLastUsed().compareTo(a.getLastUsed()));
        }

        int shown = 0;
        for (ChordProgression cp : all) {
            if (searching) {
                boolean match = cp.getTitle().toLowerCase().contains(query)
                    || cp.getKey().toLowerCase().contains(query)
                    || cp.getChordsAsString().toLowerCase().contains(query)
                    || cp.getMemo().toLowerCase().contains(query);
                if (!match) continue;
            }
            listPanel.add(buildListCard(cp));
            listPanel.add(Box.createVerticalStrut(10));
            shown++;
        }
        if (shown == 0) {
            JLabel empty = new JLabel(searching ? "検索結果がありません" : "登録されたコード進行がありません", SwingConstants.CENTER);
            empty.setFont(AppTheme.bodyFont(14));
            empty.setForeground(AppTheme.TEXT_MUTED);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(Box.createVerticalGlue());
            listPanel.add(empty);
            listPanel.add(Box.createVerticalGlue());
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    /** 星表示ラベルを生成 */
    private JLabel buildStarLabel(int rating) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) sb.append(i <= rating ? "★" : "☆");
        JLabel l = new JLabel(sb.toString());
        l.setFont(AppTheme.bodyFont(13));
        l.setForeground(new Color(255, 200, 50));
        l.setAlignmentX(Component.RIGHT_ALIGNMENT);
        return l;
    }

    private JPanel buildListCard(ChordProgression cp) {
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

        // Left: title + chords
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(cp.getTitle());
        titleLabel.setFont(AppTheme.titleFont(15));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);

        String chordsDisplay = settings.getNoteMode() == AppSettings.NoteMode.DEGREE
            ? String.join(" → ", DegreeConverter.convertList(cp.getChords(), cp.getKey()))
            : cp.getChordsAsString();
        JLabel chordsLabel = new JLabel(chordsDisplay);
        chordsLabel.setFont(AppTheme.monoFont(13));
        chordsLabel.setForeground(AppTheme.ACCENT_LIGHT);

        // 役割表示（T/SD/D）
        StringBuilder funcSb = new StringBuilder("役割: ");
        List<String> chordList0 = cp.getChords();
        for (int i = 0; i < chordList0.size(); i++) {
            if (i > 0) funcSb.append("  ");
            String func = ChordTransposer.getChordFunction(chordList0.get(i), cp.getKey());
            String color = func.equals("T") ? "#64c8ff" : func.equals("SD") ? "#64e096" : func.equals("D") ? "#ff9664" : "#888888";
            funcSb.append("<font color='").append(color).append("'>")
                  .append(chordList0.get(i)).append("[").append(func.isEmpty() ? "?" : func).append("]")
                  .append("</font>");
        }
        JLabel funcLabel = new JLabel("<html>" + funcSb + "</html>");
        funcLabel.setFont(AppTheme.bodyFont(11));
        funcLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String memoText = cp.getMemo().isEmpty() ? "" : "  " + cp.getMemo();
        JLabel memoLabel = new JLabel(memoText);
        memoLabel.setFont(AppTheme.bodyFont(11));
        memoLabel.setForeground(AppTheme.TEXT_MUTED);

        left.add(titleLabel);
        left.add(Box.createVerticalStrut(3));
        left.add(chordsLabel);
        left.add(Box.createVerticalStrut(2));
        left.add(funcLabel);
        List<String> chords = cp.getChords();
        if (!chords.isEmpty()) {
            StringBuilder tones = new StringBuilder("構成音: ");
            for (int i = 0; i < chords.size(); i++) {
                if (i > 0) tones.append("  ");
                tones.append(chords.get(i)).append("[").append(ChordTransposer.getChordTones(chords.get(i))).append("]");
            }
            JLabel tonesLabel = new JLabel(tones.toString());
            tonesLabel.setFont(AppTheme.bodyFont(10));
            tonesLabel.setForeground(AppTheme.TEXT_MUTED);
            left.add(Box.createVerticalStrut(2));
            left.add(tonesLabel);
        }

        if (!cp.getMemo().isEmpty()) { left.add(Box.createVerticalStrut(2)); left.add(memoLabel); }

        // Right: key + BPM + date
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        JLabel keyBpm = new JLabel("BPM: " + cp.getTempo());
        keyBpm.setFont(AppTheme.bodyFont(12));
        keyBpm.setForeground(AppTheme.ACCENT2);
        keyBpm.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel dateLabel = new JLabel(cp.getFormattedLastUsed());
        dateLabel.setFont(AppTheme.bodyFont(11));
        dateLabel.setForeground(AppTheme.TEXT_MUTED);
        dateLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        right.add(buildStarLabel(cp.getRating()));
        right.add(Box.createVerticalStrut(2));
        right.add(keyBpm);
        right.add(Box.createVerticalStrut(4));
        right.add(dateLabel);

        card.add(left, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(AppTheme.BG_PANEL);
        footer.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0,AppTheme.BORDER),
            new EmptyBorder(12,24,12,24)));

        JLabel count = new JLabel("全 " + repo.getAll().size() + " 件");
        count.setFont(AppTheme.bodyFont(12));
        count.setForeground(AppTheme.TEXT_MUTED);
        footer.add(count, BorderLayout.WEST);

        JButton closeBtn = buildButton("閉じる", AppTheme.ACCENT2, Color.WHITE);
        closeBtn.addActionListener(e -> dispose());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(closeBtn);
        footer.add(btnRow, BorderLayout.EAST);
        return footer;
    }

    private JButton buildButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),8,8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(AppTheme.bodyFont(13));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8,18,8,18));
        return btn;
    }
}
