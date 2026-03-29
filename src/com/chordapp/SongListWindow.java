package com.chordapp;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * 保存済み楽曲の一覧画面（編集・削除・テキスト出力対応）
 */
public class SongListWindow extends JDialog {

    private final MainMenuWindow parent;
    private final SongRepository songRepo = SongRepository.getInstance();
    private final ChordProgressionRepository repo = ChordProgressionRepository.getInstance();
    private final AppSettings settings = AppSettings.getInstance();

    private JPanel listPanel;
    private JLabel countLabel;

    public SongListWindow(MainMenuWindow parent) {
        super(parent, "楽曲一覧", true);
        this.parent = parent;
        setSize(920, 660);
        setLocationRelativeTo(parent);
        setResizable(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.BG_DARK);
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildContent(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
    }

    // ── Header ──────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(AppTheme.BG_PANEL);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(100, 200, 255));
                g2.fillRect(0, getHeight() - 2, getWidth(), 2);
                g2.dispose();
            }
        };
        h.setPreferredSize(new Dimension(920, 64));
        h.setBorder(new EmptyBorder(14, 24, 14, 24));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel icon = new JLabel("🎼");
        icon.setFont(new Font("SansSerif", Font.PLAIN, 24));
        JLabel title = new JLabel("楽曲一覧");
        title.setFont(AppTheme.titleFont(18));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        left.add(icon); left.add(title);

        JButton newSongBtn = accentBtn("＋ 新しい曲を作成", AppTheme.ACCENT, Color.WHITE);
        newSongBtn.addActionListener(e -> {
            dispose();
            new SongEditorWindow(parent).setVisible(true);
        });

        h.add(left, BorderLayout.WEST);
        h.add(newSongBtn, BorderLayout.EAST);
        return h;
    }

    // ── Content ──────────────────────────────────────────────────────────────
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
        List<Song> songs = songRepo.getRecentlyUpdated();

        if (songs.isEmpty()) {
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setOpaque(false);
            JLabel empty = new JLabel("保存された楽曲がありません", SwingConstants.CENTER);
            empty.setFont(AppTheme.bodyFont(14));
            empty.setForeground(AppTheme.TEXT_MUTED);
            JLabel hint = new JLabel("楽曲エディタで曲を作成し「💾 曲を保存」してください", SwingConstants.CENTER);
            hint.setFont(AppTheme.bodyFont(12));
            hint.setForeground(AppTheme.TEXT_MUTED);
            JPanel msgs = new JPanel();
            msgs.setOpaque(false);
            msgs.setLayout(new BoxLayout(msgs, BoxLayout.Y_AXIS));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            hint.setAlignmentX(Component.CENTER_ALIGNMENT);
            msgs.add(empty);
            msgs.add(Box.createVerticalStrut(8));
            msgs.add(hint);
            emptyPanel.add(msgs, BorderLayout.CENTER);
            listPanel.add(Box.createVerticalGlue());
            listPanel.add(emptyPanel);
            listPanel.add(Box.createVerticalGlue());
        } else {
            for (Song song : songs) {
                listPanel.add(buildSongCard(song));
                listPanel.add(Box.createVerticalStrut(10));
            }
        }

        listPanel.revalidate();
        listPanel.repaint();

        if (countLabel != null) countLabel.setText("全 " + songs.size() + " 曲");
    }

    // ── Song Card ────────────────────────────────────────────────────────────
    private JPanel buildSongCard(Song song) {
        JPanel card = new JPanel(new BorderLayout(12, 0)) {
            boolean hovered = false;
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? AppTheme.BG_CARD_HOVER : AppTheme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), AppTheme.RADIUS, AppTheme.RADIUS));
                g2.setColor(new Color(100, 200, 255));
                g2.fill(new RoundRectangle2D.Float(0, 0, 5, getHeight(), 4, 4));
                g2.dispose();
            }
            { addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
            }); }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 20, 14, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Left info ────────────────────────────────────────
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(song.getTitle());
        titleLabel.setFont(AppTheme.titleFont(16));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel sectionSummary = new JLabel(song.getSectionSummary());
        sectionSummary.setFont(AppTheme.monoFont(12));
        sectionSummary.setForeground(AppTheme.ACCENT_LIGHT);

        JLabel meta = new JLabel(
            "Key: " + song.getKey() + "  |  " + song.getSectionCount() + " セクション  |  最終更新: " + song.getFormattedUpdatedAt());
        meta.setFont(AppTheme.bodyFont(11));
        meta.setForeground(AppTheme.TEXT_MUTED);

        info.add(titleLabel);
        info.add(Box.createVerticalStrut(4));
        info.add(sectionSummary);
        info.add(Box.createVerticalStrut(4));
        info.add(meta);

        // ── Right buttons ─────────────────────────────────────
        JPanel btns = new JPanel();
        btns.setOpaque(false);
        btns.setLayout(new BoxLayout(btns, BoxLayout.Y_AXIS));

        JPanel topBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        topBtns.setOpaque(false);

        JButton editBtn = accentBtn("編集", AppTheme.ACCENT, Color.WHITE);
        editBtn.addActionListener(e -> openEditor(song));

        JButton exportBtn = accentBtn("📄 出力", new Color(60, 160, 100), Color.WHITE);
        exportBtn.addActionListener(e -> exportSong(song));

        topBtns.add(editBtn);
        topBtns.add(exportBtn);

        JPanel botBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        botBtns.setOpaque(false);

        JButton deleteBtn = accentBtn("削除", new Color(200, 60, 60), Color.WHITE);
        deleteBtn.addActionListener(e -> confirmDelete(song));

        botBtns.add(deleteBtn);

        btns.add(topBtns);
        btns.add(Box.createVerticalStrut(4));
        btns.add(botBtns);

        card.add(info, BorderLayout.CENTER);
        card.add(btns, BorderLayout.EAST);
        return card;
    }

    // ── Actions ──────────────────────────────────────────────────────────────
    private void openEditor(Song song) {
        dispose();
        SongEditorWindow editor = new SongEditorWindow(parent, song);
        editor.setVisible(true);
    }

    private void confirmDelete(Song song) {
        int result = JOptionPane.showConfirmDialog(this,
            "「" + song.getTitle() + "」を削除しますか？\nこの操作は元に戻せません。",
            "削除の確認", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            songRepo.delete(song.getId());
            refreshList();
        }
    }

    private void exportSong(Song song) {
        String text = buildTextOutput(song);
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File(song.getTitle() + ".txt"));
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("テキストファイル (*.txt)", "txt"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fc.getSelectedFile();
            if (!file.getName().endsWith(".txt")) file = new java.io.File(file.getPath() + ".txt");
            try (java.io.PrintWriter pw = new java.io.PrintWriter(
                    new java.io.OutputStreamWriter(new java.io.FileOutputStream(file), "UTF-8"))) {
                pw.print(text);
                JOptionPane.showMessageDialog(this,
                    "保存しました:\n" + file.getAbsolutePath(), "出力完了", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "保存エラー: " + ex.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String buildTextOutput(Song song) {
        StringBuilder sb = new StringBuilder();
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("  ").append(song.getTitle()).append("\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        for (SongSection sec : song.getSections()) {
            sb.append("# ").append(sec.getSectionName());
            if (sec.getRepeatCount() > 1) sb.append("  ×").append(sec.getRepeatCount());
            sb.append("\n");
            if (sec.getChords().isEmpty()) {
                sb.append("  （コードなし）\n");
            } else {
                String key = "C";
                if (sec.getProgressionId() != null) {
                    ChordProgression cp = repo.findById(sec.getProgressionId());
                    if (cp != null) key = cp.getKey();
                }
                List<String> display = settings.getNoteMode() == AppSettings.NoteMode.DEGREE
                    ? DegreeConverter.convertList(sec.getChords(), key)
                    : sec.getChords();
                sb.append("  ").append(String.join("  ", display)).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // ── Footer ──────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(AppTheme.BG_PANEL);
        footer.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER),
            new EmptyBorder(12, 24, 12, 24)));

        countLabel = new JLabel("全 " + songRepo.getAll().size() + " 曲");
        countLabel.setFont(AppTheme.bodyFont(12));
        countLabel.setForeground(AppTheme.TEXT_MUTED);
        footer.add(countLabel, BorderLayout.WEST);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        // MIDI出力ボタン
        JButton midiBtn = accentBtn("🎹 MIDI出力", new Color(180, 100, 220), Color.WHITE);
        midiBtn.addActionListener(e -> showMidiExportDialog());

        JButton closeBtn = accentBtn("閉じる", AppTheme.BG_CARD, AppTheme.TEXT_SECONDARY);
        closeBtn.addActionListener(e -> dispose());

        btnRow.add(midiBtn);
        btnRow.add(closeBtn);
        footer.add(btnRow, BorderLayout.EAST);
        return footer;
    }

    // ── MIDI出力ダイアログ ────────────────────────────────────────────────────
    private void showMidiExportDialog() {
        List<Song> songs = songRepo.getRecentlyUpdated();
        if (songs.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "保存された楽曲がありません。\n先に楽曲エディタで曲を作成・保存してください。",
                "楽曲なし", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dlg = new JDialog(this, "🎹 MIDI出力", true);
        dlg.setSize(520, 380);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.BG_DARK);
        setContentPane(root);

        // ── ヘッダー
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(AppTheme.BG_PANEL);
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(180,100,220));
                g2.fillRect(0,getHeight()-2,getWidth(),2);
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(520, 56));
        header.setBorder(new EmptyBorder(12,24,12,24));
        JLabel title = new JLabel("🎹  MIDI出力  （おまけ機能）");
        title.setFont(AppTheme.titleFont(16));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        // ── フォーム
        JPanel form = new JPanel();
        form.setBackground(AppTheme.BG_DARK);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 28, 10, 28));

        // 注意書き
        JLabel note = new JLabel("<html><div style='color:#6464a0;font-size:11px'>"
            + "※ C / Cm キー基準で構成音を生成します。<br>"
            + "他のキーへの移調はDAW側でお願いします。</div></html>");
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(note);
        form.add(Box.createVerticalStrut(16));

        // 曲選択
        form.add(fieldLabel2("出力する楽曲"));
        form.add(Box.createVerticalStrut(6));
        String[] songNames = songs.stream().map(Song::getTitle).toArray(String[]::new);
        JComboBox<String> songCombo = new JComboBox<>(songNames);
        songCombo.setFont(AppTheme.bodyFont(13));
        songCombo.setBackground(AppTheme.BG_CARD);
        songCombo.setForeground(AppTheme.TEXT_PRIMARY);
        songCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        songCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(songCombo);
        form.add(Box.createVerticalStrut(16));

        // BPM設定
        form.add(fieldLabel2("テンポ（BPM）"));
        form.add(Box.createVerticalStrut(6));
        JPanel bpmRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bpmRow.setOpaque(false);
        bpmRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        SpinnerNumberModel bpmModel = new SpinnerNumberModel(120, 20, 300, 1);
        JSpinner bpmSpinner = new JSpinner(bpmModel);
        bpmSpinner.setFont(AppTheme.monoFont(14));
        bpmSpinner.setBackground(AppTheme.BG_CARD);
        bpmSpinner.setPreferredSize(new Dimension(90, 34));
        ((JSpinner.DefaultEditor)bpmSpinner.getEditor()).getTextField().setBackground(AppTheme.BG_CARD);
        ((JSpinner.DefaultEditor)bpmSpinner.getEditor()).getTextField().setForeground(AppTheme.TEXT_PRIMARY);
        ((JSpinner.DefaultEditor)bpmSpinner.getEditor()).getTextField().setFont(AppTheme.monoFont(14));

        // お気に入りBPMボタン
        JPanel favBpms = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        favBpms.setOpaque(false);
        for (String bpm : AppSettings.getInstance().getFavoriteBpms()) {
            JButton b = new JButton(bpm) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isRollover() ? AppTheme.ACCENT : AppTheme.BG_CARD);
                    g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),5,5));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            b.setFont(AppTheme.monoFont(11));
            b.setForeground(AppTheme.TEXT_SECONDARY);
            b.setBackground(AppTheme.BG_CARD);
            b.setBorderPainted(false); b.setFocusPainted(false);
            b.setContentAreaFilled(false); b.setOpaque(false);
            b.setBorder(new EmptyBorder(3,8,3,8));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(e2 -> bpmSpinner.setValue(Integer.parseInt(bpm)));
            favBpms.add(b);
        }
        bpmRow.add(bpmSpinner); bpmRow.add(favBpms);
        form.add(bpmRow);
        form.add(Box.createVerticalStrut(16));

        // 拍子設定
        form.add(fieldLabel2("拍子"));
        form.add(Box.createVerticalStrut(6));

        // 拍子定義（ラベル・拍数・説明）
        String[] timeSigs   = {"4/4", "3/4", "6/8", "5/4", "7/8", "8/8"};
        int[]    timeSigBeats = {4,     3,     6,     5,     7,     8};
        String[] timeSigDesc  = {
            "ポップス・ロック（標準）",
            "ワルツ・バラード系",
            "シャッフル・バラード系",
            "プログレッシブメタルなど",
            "変拍子・プログレ系",
            "変拍子・ファンク・フュージョン系"
        };

        ButtonGroup timeSigGroup = new ButtonGroup();
        JRadioButton[] timeSigBtns = new JRadioButton[timeSigs.length];

        JPanel timeSigPanel = new JPanel();
        timeSigPanel.setOpaque(false);
        timeSigPanel.setLayout(new BoxLayout(timeSigPanel, BoxLayout.Y_AXIS));
        timeSigPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int i = 0; i < timeSigs.length; i++) {
            final int idx = i;
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            row.setOpaque(false);

            JRadioButton rb = new JRadioButton(timeSigs[i]);
            rb.setFont(AppTheme.bodyFont(13));
            rb.setForeground(AppTheme.TEXT_PRIMARY);
            rb.setBackground(AppTheme.BG_DARK);
            rb.setOpaque(false);
            rb.setFocusPainted(false);
            if (i == 0) rb.setSelected(true);
            timeSigGroup.add(rb);
            timeSigBtns[i] = rb;

            JLabel desc = new JLabel("─  " + timeSigDesc[i]);
            desc.setFont(AppTheme.bodyFont(11));
            desc.setForeground(AppTheme.TEXT_MUTED);

            row.add(rb);
            row.add(desc);
            timeSigPanel.add(row);
        }
        form.add(timeSigPanel);

        // ルート音オクターブ下げ設定
        form.add(fieldLabel2("オプション"));
        form.add(Box.createVerticalStrut(6));
        JCheckBox rootDownCheck = new JCheckBox("ルート音を1オクターブ下げる");
        rootDownCheck.setFont(AppTheme.bodyFont(13));
        rootDownCheck.setForeground(AppTheme.TEXT_PRIMARY);
        rootDownCheck.setBackground(AppTheme.BG_DARK);
        rootDownCheck.setOpaque(false);
        rootDownCheck.setFocusPainted(false);
        rootDownCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(rootDownCheck);
        form.add(Box.createVerticalStrut(4));
        JLabel rootDownNote = new JLabel("  例: Dm → D音のみ1オクターブ下がります");
        rootDownNote.setFont(AppTheme.bodyFont(11));
        rootDownNote.setForeground(AppTheme.TEXT_MUTED);
        rootDownNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(rootDownNote);

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBackground(AppTheme.BG_DARK);
        formScroll.getViewport().setBackground(AppTheme.BG_DARK);
        formScroll.setBorder(null);

        // ── フッター
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(AppTheme.BG_PANEL);
        footer.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0,AppTheme.BORDER),
            new EmptyBorder(12,24,12,24)));
        JLabel hint = new JLabel("出力後はDAWに読み込んでご使用ください");
        hint.setFont(AppTheme.bodyFont(11));
        hint.setForeground(AppTheme.TEXT_MUTED);
        footer.add(hint, BorderLayout.WEST);

        JPanel footerBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footerBtns.setOpaque(false);
        JButton cancelB = accentBtn("キャンセル", AppTheme.BG_CARD, AppTheme.TEXT_SECONDARY);
        cancelB.addActionListener(e -> dlg.dispose());
        JButton exportB = accentBtn("  出力する  ", new Color(180,100,220), Color.WHITE);
        exportB.addActionListener(e -> {
            int idx = songCombo.getSelectedIndex();
            if (idx < 0) return;
            Song song = songs.get(idx);
            int bpm = (Integer) bpmSpinner.getValue();

            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new java.io.File(song.getTitle() + ".mid"));
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "MIDIファイル (*.mid)", "mid"));
            if (fc.showSaveDialog(dlg) == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fc.getSelectedFile();
                if (!file.getName().endsWith(".mid"))
                    file = new java.io.File(file.getPath() + ".mid");
                try {
                    int beats = 4;
                    for (int i = 0; i < timeSigBtns.length; i++) {
                        if (timeSigBtns[i].isSelected()) { beats = timeSigBeats[i]; break; }
                    }
                    MidiExporter.export(song, bpm, file, rootDownCheck.isSelected(), beats);
                    JOptionPane.showMessageDialog(dlg,
                        "保存しました:\n" + file.getAbsolutePath(),
                        "MIDI出力完了", JOptionPane.INFORMATION_MESSAGE);
                    dlg.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg,
                        "出力エラー: " + ex.getMessage(),
                        "エラー", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        footerBtns.add(cancelB); footerBtns.add(exportB);
        footer.add(footerBtns, BorderLayout.EAST);

        JPanel dlgRoot = new JPanel(new BorderLayout());
        dlgRoot.setBackground(AppTheme.BG_DARK);
        dlgRoot.add(header, BorderLayout.NORTH);
        dlgRoot.add(formScroll, BorderLayout.CENTER);
        dlgRoot.add(footer, BorderLayout.SOUTH);
        dlg.setContentPane(dlgRoot);
        dlg.setVisible(true);
    }

    private JLabel fieldLabel2(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppTheme.bodyFont(12));
        l.setForeground(AppTheme.TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // ── UI Helper ───────────────────────────────────────────────────────────
    private JButton accentBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 7, 7));
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
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        return btn;
    }
}
