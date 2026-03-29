package com.chordapp;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class MainMenuWindow extends JFrame {

    private ChordProgressionRepository repo = ChordProgressionRepository.getInstance();
    private AppSettings settings = AppSettings.getInstance();
    private JPanel recentPanel;

    public MainMenuWindow() {
        setTitle("🎵 コード進行管理アプリ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        AppSettings.WindowSize ws = settings.getWindowSize();
        setSize(ws.width, ws.height);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));

        // 最大化・最小化ボタンを有効化（デフォルトで有効だが明示）
        setExtendedState(JFrame.NORMAL);

        getContentPane().setBackground(AppTheme.BG_DARK);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(AppTheme.BG_DARK);
        mainContent.setBorder(new EmptyBorder(20, 30, 20, 30));
        mainContent.add(buildMenuSection(), BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);

        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    // ── Header ──────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0,0, AppTheme.headerBgFrom(),
                    getWidth(),0, AppTheme.headerBgTo());
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(1280, 80));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,AppTheme.BORDER),
            new EmptyBorder(16,30,16,30)));

        // Logo + title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        JLabel icon = new JLabel("🎸");
        icon.setFont(new Font("SansSerif", Font.PLAIN, 32));
        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel appName = new JLabel("Chord Progression Manager");
        appName.setFont(AppTheme.titleFont(22));
        appName.setForeground(AppTheme.TEXT_PRIMARY);
        JLabel subTitle = new JLabel("作曲コード進行管理システム");
        subTitle.setFont(AppTheme.bodyFont(12));
        subTitle.setForeground(AppTheme.TEXT_SECONDARY);
        titleBlock.add(appName); titleBlock.add(subTitle);
        left.add(icon); left.add(titleBlock);

        // Right: settings button + version
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        JButton settingsBtn = new JButton("⚙ 設定") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? AppTheme.BG_CARD_HOVER : AppTheme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),7,7));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        settingsBtn.setFont(AppTheme.bodyFont(13));
        settingsBtn.setForeground(AppTheme.TEXT_SECONDARY);
        settingsBtn.setBackground(AppTheme.BG_CARD);
        settingsBtn.setBorderPainted(false);
        settingsBtn.setFocusPainted(false);
        settingsBtn.setContentAreaFilled(false);
        settingsBtn.setOpaque(false);
        settingsBtn.setBorder(new EmptyBorder(6,14,6,14));
        settingsBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        settingsBtn.addActionListener(e -> new SettingsDialog(this).setVisible(true));

        JLabel ver = new JLabel(AppSettings.VERSION);
        ver.setFont(AppTheme.monoFont(12));
        ver.setForeground(AppTheme.ACCENT_LIGHT);

        right.add(settingsBtn); right.add(ver);
        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    // ── Recent Progressions ─────────────────────────────────────────────────
    private JPanel buildRecentSection() {
        JPanel section = new JPanel(new BorderLayout(0, 14));
        section.setBackground(AppTheme.BG_DARK);
        section.setBorder(new EmptyBorder(18, 0, 10, 0));

        JPanel labelRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        labelRow.setOpaque(false);
        JLabel dot = new JLabel("●");
        dot.setForeground(AppTheme.ACCENT2);
        dot.setFont(new Font("SansSerif", Font.PLAIN, 10));
        JLabel label = new JLabel("  最近登録・編集したコード進行");
        label.setFont(AppTheme.titleFont(16));
        label.setForeground(AppTheme.TEXT_PRIMARY);
        labelRow.add(dot); labelRow.add(label);
        section.add(labelRow, BorderLayout.NORTH);

        recentPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        recentPanel.setBackground(AppTheme.BG_DARK);
        refreshRecentCards();
        section.add(recentPanel, BorderLayout.CENTER);
        return section;
    }

    public void refreshRecentCards() {
        if (recentPanel == null) return;
        recentPanel.removeAll();
        List<ChordProgression> recents = repo.getRecentlyUsed(3);
        for (ChordProgression cp : recents) recentPanel.add(buildRecentCard(cp));
        for (int i = recents.size(); i < 3; i++) recentPanel.add(buildEmptyCard());
        recentPanel.revalidate();
        recentPanel.repaint();
    }

    private JPanel buildRecentCard(ChordProgression cp) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),AppTheme.RADIUS,AppTheme.RADIUS));
                g2.setColor(AppTheme.ACCENT);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),4,4,4));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18,20,18,20));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                openRecentWindow();
            }
        });

        JLabel titleLabel = new JLabel(cp.getTitle());
        titleLabel.setFont(AppTheme.titleFont(15));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel keyBpm = new JLabel("BPM: " + cp.getTempo());
        keyBpm.setFont(AppTheme.bodyFont(12));
        keyBpm.setForeground(AppTheme.ACCENT2);

        // コード表示（設定に応じてディグリー変換）
        String chordStr = settings.getNoteMode() == AppSettings.NoteMode.DEGREE
            ? DegreeConverter.displayList(cp.getChords(), cp.getKey())
            : cp.getChordsAsString();
        JLabel chordsLabel = new JLabel(chordStr);
        chordsLabel.setFont(AppTheme.monoFont(13));
        chordsLabel.setForeground(AppTheme.ACCENT_LIGHT);

        JLabel dateLabel = new JLabel("最終使用: " + cp.getFormattedLastUsed());
        dateLabel.setFont(AppTheme.bodyFont(11));
        dateLabel.setForeground(AppTheme.TEXT_MUTED);

        JLabel memoLabel = new JLabel("<html><i>" + (cp.getMemo().isEmpty() ? "" : cp.getMemo()) + "</i></html>");
        memoLabel.setFont(AppTheme.bodyFont(11));
        memoLabel.setForeground(AppTheme.TEXT_SECONDARY);

        JPanel topInfo = new JPanel();
        topInfo.setOpaque(false);
        topInfo.setLayout(new BoxLayout(topInfo, BoxLayout.Y_AXIS));
        topInfo.add(titleLabel);
        topInfo.add(Box.createVerticalStrut(4));
        topInfo.add(keyBpm);

        JPanel bottomInfo = new JPanel();
        bottomInfo.setOpaque(false);
        bottomInfo.setLayout(new BoxLayout(bottomInfo, BoxLayout.Y_AXIS));
        bottomInfo.add(chordsLabel);
        bottomInfo.add(Box.createVerticalStrut(6));
        if (!cp.getMemo().isEmpty()) { bottomInfo.add(memoLabel); bottomInfo.add(Box.createVerticalStrut(4)); }
        bottomInfo.add(dateLabel);

        card.add(topInfo, BorderLayout.NORTH);
        card.add(bottomInfo, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildEmptyCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.emptyCardBg());
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),AppTheme.RADIUS,AppTheme.RADIUS));
                g2.setColor(AppTheme.BORDER);
                g2.setStroke(new BasicStroke(1.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,0,new float[]{6,4},0));
                g2.draw(new RoundRectangle2D.Float(1,1,getWidth()-2,getHeight()-2,AppTheme.RADIUS,AppTheme.RADIUS));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        JLabel empty = new JLabel("データなし", SwingConstants.CENTER);
        empty.setFont(AppTheme.bodyFont(13));
        empty.setForeground(AppTheme.TEXT_MUTED);
        card.add(empty, BorderLayout.CENTER);
        return card;
    }

    // ── Menu Buttons Section ────────────────────────────────────────────────
    private JPanel buildMenuSection() {
        JPanel section = new JPanel(new BorderLayout(0, 14));
        section.setBackground(AppTheme.BG_DARK);
        section.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel labelRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        labelRow.setOpaque(false);
        JLabel dot = new JLabel("●");
        dot.setForeground(AppTheme.ACCENT);
        dot.setFont(new Font("SansSerif", Font.PLAIN, 10));
        JLabel label = new JLabel("  メニュー");
        label.setFont(AppTheme.titleFont(16));
        label.setForeground(AppTheme.TEXT_PRIMARY);
        labelRow.add(dot); labelRow.add(label);
        section.add(labelRow, BorderLayout.NORTH);

        JPanel allBtns = new JPanel(new GridLayout(2, 1, 0, 10));
        allBtns.setBackground(AppTheme.BG_DARK);

        // 上段：コード進行系
        JPanel topBtns = new JPanel(new GridLayout(1, 2, 14, 0));
        topBtns.setBackground(AppTheme.BG_DARK);
        topBtns.add(buildMenuButton("＋", "コード進行登録",
            "新しいコード進行を登録", AppTheme.ACCENT, e -> openRegisterWindow()));
        topBtns.add(buildMenuButton("⚙", "コード進行管理",
            "閲覧・検索・編集・削除・並び替え", new Color(220,140,60), e -> openManageWindow()));

        // 下段：楽曲系
        JPanel bottomBtns = new JPanel(new GridLayout(1, 2, 14, 0));
        bottomBtns.setBackground(AppTheme.BG_DARK);
        bottomBtns.add(buildMenuButton("🎼", "楽曲エディタ",
            "セクションを並べて作曲", new Color(100,200,255), e -> openSongEditor()));
        bottomBtns.add(buildMenuButton("📋", "楽曲一覧",
            "保存した曲を管理・出力", new Color(180,140,255), e -> openSongList()));

        allBtns.add(topBtns);
        allBtns.add(bottomBtns);

        section.add(allBtns, BorderLayout.CENTER);
        return section;
    }

    private JPanel buildMenuButton(String icon, String title, String desc, Color accentColor, ActionListener action) {
        JPanel btn = new JPanel(new BorderLayout(0, 10)) {
            boolean hovered = false;
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? AppTheme.BG_CARD_HOVER : AppTheme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),AppTheme.RADIUS,AppTheme.RADIUS));
                g2.setColor(accentColor);
                g2.fill(new RoundRectangle2D.Float(0,0,5,getHeight(),4,4));
                if (hovered) {
                    g2.setColor(new Color(accentColor.getRed(),accentColor.getGreen(),accentColor.getBlue(),28));
                    g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),AppTheme.RADIUS,AppTheme.RADIUS));
                }
                g2.dispose();
            }
            { addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered=true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovered=false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) { action.actionPerformed(null); }
            });
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
        };
        btn.setOpaque(false);
        btn.setBorder(new EmptyBorder(20,24,20,24));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 30));
        iconLabel.setForeground(accentColor);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel textBlock = new JPanel();
        textBlock.setOpaque(false);
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(AppTheme.titleFont(15));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(AppTheme.bodyFont(11));
        descLabel.setForeground(AppTheme.TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textBlock.add(titleLabel);
        textBlock.add(Box.createVerticalStrut(3));
        textBlock.add(descLabel);

        JLabel arrow = new JLabel("→");
        arrow.setFont(AppTheme.titleFont(16));
        arrow.setForeground(accentColor);
        arrow.setHorizontalAlignment(SwingConstants.RIGHT);

        btn.add(iconLabel, BorderLayout.NORTH);
        btn.add(textBlock, BorderLayout.CENTER);
        btn.add(arrow, BorderLayout.SOUTH);
        return btn;
    }

    // ── Status Bar ──────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(AppTheme.isLight() ? new Color(220, 220, 235) : new Color(15,15,25));
        bar.setBorder(new EmptyBorder(6,20,6,20));

        String modeLabel = settings.getNoteMode() == AppSettings.NoteMode.DEGREE ? "ディグリーモード" : "コードネームモード";
        JLabel info = new JLabel("登録済み: " + repo.getAll().size() + " 件  |  " + modeLabel);
        info.setFont(AppTheme.bodyFont(11));
        info.setForeground(AppTheme.TEXT_MUTED);

        JLabel copy = new JLabel("Chord Progression Manager © 2025");
        copy.setFont(AppTheme.bodyFont(11));
        copy.setForeground(AppTheme.TEXT_MUTED);

        bar.add(info, BorderLayout.WEST);
        bar.add(copy, BorderLayout.EAST);
        return bar;
    }

    // ── Window Openers ──────────────────────────────────────────────────────
    private void openRegisterWindow()  { new RegisterWindow(this).setVisible(true); }
    private void openRecentWindow()    { new RecentWindow(this).setVisible(true); }
    private void openManageWindow()    { new ManageWindow(this).setVisible(true); }
    private void openSongEditor()      { new SongEditorWindow(this).setVisible(true); }
    private void openSongList()        { new SongListWindow(this).setVisible(true); }

    /** テーマ変更などで画面全体を再構築する */
    public void refreshAll() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(AppTheme.BG_DARK);
        mainContent.setBorder(new EmptyBorder(20, 30, 20, 30));
        mainContent.add(buildMenuSection(), BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        getContentPane().setBackground(AppTheme.BG_DARK);
        revalidate();
        repaint();
    }
}
