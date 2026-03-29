package com.chordapp;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * 1曲まるごとのコード構成エディタ
 * セクション（Aメロ・サビ等）を並べて管理し、テキスト出力も可能
 */
public class SongEditorWindow extends JDialog {

    private MainMenuWindow parent;
    private ChordProgressionRepository repo = ChordProgressionRepository.getInstance();
    private SongRepository songRepo = SongRepository.getInstance();
    private AppSettings settings = AppSettings.getInstance();

    private JTextField songTitleField;
    private JComboBox<String> songKeyCombo;
    private JPanel sectionsPanel;
    private List<SongSection> sections = new ArrayList<>();
    private Song currentSong = null;

    // プリセットセクション名
    private static final String[] SECTION_PRESETS = {
        "Aメロ", "Bメロ", "サビ", "Cメロ", "Dメロ",
        "イントロ", "アウトロ", "ブリッジ", "間奏", "大サビ", "カスタム..."
    };

    public SongEditorWindow(MainMenuWindow parent) {
        this(parent, null);
    }

    public SongEditorWindow(MainMenuWindow parent, Song songToEdit) {
        super(parent, "楽曲エディタ", false);
        this.parent = parent;
        this.currentSong = songToEdit;
        if (songToEdit != null) sections = new ArrayList<>(songToEdit.getSections());
        setSize(1060, 700);
        setLocationRelativeTo(parent);
        setResizable(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.BG_DARK);
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildMainArea(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        if (songToEdit != null) {
            songTitleField.setText(songToEdit.getTitle());
            songKeyCombo.setSelectedItem(songToEdit.getKey());
            refreshSections();
        }
    }

    // ── Header ──────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout(16, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0,0, AppTheme.headerBgFrom(),getWidth(),0, AppTheme.headerBgTo());
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(100,220,200));
                g2.fillRect(0,getHeight()-2,getWidth(),2);
                g2.dispose();
            }
        };
        h.setPreferredSize(new Dimension(1060,70));
        h.setBorder(new EmptyBorder(12,24,12,24));

        // 左：タイトル
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        JLabel icon = new JLabel("🎼");
        icon.setFont(new Font("SansSerif", Font.PLAIN, 28));
        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel heading = new JLabel("楽曲エディタ");
        heading.setFont(AppTheme.titleFont(17));
        heading.setForeground(AppTheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("セクションを並べて1曲を構成する");
        sub.setFont(AppTheme.bodyFont(11));
        sub.setForeground(AppTheme.TEXT_SECONDARY);
        titleBlock.add(heading); titleBlock.add(sub);
        left.add(icon); left.add(titleBlock);

        // 中央：曲名 + キー設定
        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        center.setOpaque(false);

        JLabel songLabel = new JLabel("曲名:");
        songLabel.setFont(AppTheme.bodyFont(13));
        songLabel.setForeground(AppTheme.TEXT_SECONDARY);
        songTitleField = new JTextField("新しい楽曲", 18);
        songTitleField.setFont(AppTheme.titleFont(14));
        songTitleField.setForeground(AppTheme.TEXT_PRIMARY);
        songTitleField.setBackground(AppTheme.inputBg());
        songTitleField.setCaretColor(AppTheme.ACCENT_LIGHT);
        songTitleField.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER,1,true),
            new EmptyBorder(5,10,5,10)));
        songTitleField.setPreferredSize(new Dimension(200,36));
        songTitleField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updatePreview(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updatePreview(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updatePreview(); }
        });

        JLabel keyLabel = new JLabel("キー:");
        keyLabel.setFont(AppTheme.bodyFont(13));
        keyLabel.setForeground(AppTheme.TEXT_SECONDARY);
        songKeyCombo = new JComboBox<>(ChordTransposer.ALL_KEYS);
        songKeyCombo.setFont(AppTheme.bodyFont(13));
        songKeyCombo.setBackground(AppTheme.inputBg());
        songKeyCombo.setForeground(AppTheme.TEXT_PRIMARY);
        songKeyCombo.setPreferredSize(new Dimension(80,36));
        songKeyCombo.addActionListener(e -> {
            refreshSections(); // キー変更時にセクション表示を更新
            updatePreview();
        });

        center.add(songLabel); center.add(songTitleField);
        center.add(Box.createHorizontalStrut(8));
        center.add(keyLabel); center.add(songKeyCombo);

        h.add(left, BorderLayout.WEST);
        h.add(center, BorderLayout.CENTER);
        return h;
    }

    // ── Main Area ────────────────────────────────────────────────────────────
    private JSplitPane buildMainArea() {
        // Left: Section list
        JPanel sectionListPanel = buildSectionListPanel();
        // Right: Preview
        JPanel previewPanel = buildPreviewPanel();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sectionListPanel, previewPanel);
        split.setDividerLocation(680);
        split.setDividerSize(4);
        split.setBackground(AppTheme.BG_DARK);
        split.setBorder(null);
        return split;
    }

    // ── Section List (left) ──────────────────────────────────────────────────
    private JPanel buildSectionListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppTheme.BG_DARK);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setBackground(AppTheme.BG_PANEL);
        toolbar.setBorder(BorderFactory.createMatteBorder(0,0,1,0,AppTheme.BORDER));

        JButton addBtn = toolBtn("＋ セクション追加", AppTheme.ACCENT, Color.WHITE);
        addBtn.addActionListener(e -> showAddSectionDialog());
        toolbar.add(addBtn);

        JButton clearBtn = toolBtn("全クリア", new Color(160,60,60), Color.WHITE);
        clearBtn.addActionListener(e -> {
            if (sections.isEmpty()) return;
            int r = JOptionPane.showConfirmDialog(this, "全セクションを削除しますか？", "確認",
                JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) { sections.clear(); refreshSections(); }
        });
        toolbar.add(clearBtn);

        panel.add(toolbar, BorderLayout.NORTH);

        // Sections scroll area
        sectionsPanel = new JPanel();
        sectionsPanel.setBackground(AppTheme.BG_DARK);
        sectionsPanel.setLayout(new BoxLayout(sectionsPanel, BoxLayout.Y_AXIS));
        sectionsPanel.setBorder(new EmptyBorder(12,16,12,16));

        JScrollPane scroll = new JScrollPane(sectionsPanel);
        scroll.setBackground(AppTheme.BG_DARK);
        scroll.getViewport().setBackground(AppTheme.BG_DARK);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ── Preview Panel (right) ────────────────────────────────────────────────
    private JTextArea previewArea;

    private JPanel buildPreviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppTheme.BG_DARK);

        JPanel previewHeader = new JPanel(new BorderLayout());
        previewHeader.setBackground(AppTheme.BG_PANEL);
        previewHeader.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(0,1,1,0,AppTheme.BORDER),
            new EmptyBorder(8,16,8,16)));
        JLabel previewTitle = new JLabel("テキストプレビュー");
        previewTitle.setFont(AppTheme.titleFont(13));
        previewTitle.setForeground(AppTheme.TEXT_SECONDARY);
        JButton copyBtn = toolBtn("📋 コピー", new Color(60,120,180), Color.WHITE);
        copyBtn.addActionListener(e -> {
            String text = previewArea.getText();
            if (!text.isEmpty()) {
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new java.awt.datatransfer.StringSelection(text), null);
                JOptionPane.showMessageDialog(this, "クリップボードにコピーしました！", "コピー完了", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        previewHeader.add(previewTitle, BorderLayout.WEST);
        previewHeader.add(copyBtn, BorderLayout.EAST);

        previewArea = new JTextArea();
        previewArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        previewArea.setBackground(AppTheme.previewBg());
        previewArea.setForeground(AppTheme.TEXT_PRIMARY);
        previewArea.setCaretColor(AppTheme.ACCENT_LIGHT);
        previewArea.setBorder(new EmptyBorder(14,14,14,14));
        previewArea.setEditable(false);
        previewArea.setLineWrap(true);
        previewArea.setWrapStyleWord(true);

        JScrollPane previewScroll = new JScrollPane(previewArea);
        previewScroll.setBorder(BorderFactory.createMatteBorder(0,1,0,0,AppTheme.BORDER));
        previewScroll.getViewport().setBackground(AppTheme.previewBg());

        panel.add(previewHeader, BorderLayout.NORTH);
        panel.add(previewScroll, BorderLayout.CENTER);
        return panel;
    }

    // ── Footer ──────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(AppTheme.BG_PANEL);
        footer.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0,AppTheme.BORDER),
            new EmptyBorder(10,20,10,20)));

        JLabel hint = new JLabel("セクションをクリックして編集 | 矢印ボタンで並び替え");
        hint.setFont(AppTheme.bodyFont(11));
        hint.setForeground(AppTheme.TEXT_MUTED);
        footer.add(hint, BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);

        JButton saveBtn = toolBtn("💾 曲を保存", AppTheme.ACCENT, Color.WHITE);
        saveBtn.addActionListener(e -> saveSong());

        JButton exportBtn = toolBtn("📄 テキスト出力", new Color(60,160,100), Color.WHITE);
        exportBtn.addActionListener(e -> exportText());

        JButton closeBtn = toolBtn("閉じる", AppTheme.BG_CARD, AppTheme.TEXT_SECONDARY);
        closeBtn.addActionListener(e -> dispose());

        btns.add(saveBtn); btns.add(exportBtn); btns.add(closeBtn);
        footer.add(btns, BorderLayout.EAST);
        return footer;
    }

    private void saveSong() {
        String title = songTitleField.getText().trim();
        if (title.isEmpty() || title.equals("新しい楽曲")) {
            JOptionPane.showMessageDialog(this, "曲名を入力してください。", "入力エラー", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (sections.isEmpty()) {
            JOptionPane.showMessageDialog(this, "セクションを1つ以上追加してください。", "入力エラー", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (currentSong == null) {
            currentSong = new Song(title);
        } else {
            currentSong.setTitle(title);
        }
        String songKey = (String) songKeyCombo.getSelectedItem();
        currentSong.setKey(songKey != null ? songKey : "C");
        currentSong.setSections(sections);
        currentSong.setUpdatedAt(java.time.LocalDateTime.now());

        if (songRepo.findById(currentSong.getId()) == null) {
            songRepo.add(currentSong);
        } else {
            songRepo.update(currentSong);
        }
        JOptionPane.showMessageDialog(this,
            "「" + title + "」を保存しました！", "保存完了", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Section Management ───────────────────────────────────────────────────
    private void refreshSections() {
        sectionsPanel.removeAll();

        if (sections.isEmpty()) {
            JLabel empty = new JLabel("「＋ セクション追加」でセクションを追加してください", SwingConstants.CENTER);
            empty.setFont(AppTheme.bodyFont(13));
            empty.setForeground(AppTheme.TEXT_MUTED);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            sectionsPanel.add(Box.createVerticalGlue());
            sectionsPanel.add(empty);
            sectionsPanel.add(Box.createVerticalGlue());
        } else {
            for (int i = 0; i < sections.size(); i++) {
                sectionsPanel.add(buildSectionCard(sections.get(i), i));
                sectionsPanel.add(Box.createVerticalStrut(8));
            }
        }
        sectionsPanel.revalidate();
        sectionsPanel.repaint();
        updatePreview();
    }

    private JPanel buildSectionCard(SongSection section, int idx) {
        JPanel card = new JPanel(new BorderLayout(8, 0)) {
            boolean hovered = false;
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? AppTheme.BG_CARD_HOVER : AppTheme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),AppTheme.RADIUS,AppTheme.RADIUS));
                // left color bar by index
                Color[] bars = {AppTheme.ACCENT, AppTheme.ACCENT2, new Color(220,140,60),
                                new Color(200,80,140), new Color(80,180,255)};
                g2.setColor(bars[idx % bars.length]);
                g2.fill(new RoundRectangle2D.Float(0,0,5,getHeight(),4,4));
                g2.dispose();
            }
            { addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered=true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovered=false; repaint(); }
            }); }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10,16,10,12));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- Left: Section info
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        // Section name badge
        JPanel nameBadge = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        nameBadge.setOpaque(false);
        JLabel hashLabel = new JLabel("#");
        hashLabel.setFont(AppTheme.titleFont(13));
        hashLabel.setForeground(AppTheme.TEXT_MUTED);
        JLabel nameLabel = new JLabel(section.getSectionName());
        nameLabel.setFont(AppTheme.titleFont(15));
        nameLabel.setForeground(AppTheme.TEXT_PRIMARY);
        if (section.getRepeatCount() > 1) {
            JLabel repeatLabel = new JLabel("×" + section.getRepeatCount());
            repeatLabel.setFont(AppTheme.bodyFont(11));
            repeatLabel.setForeground(AppTheme.ACCENT2);
            nameBadge.add(hashLabel); nameBadge.add(nameLabel); nameBadge.add(repeatLabel);
        } else {
            nameBadge.add(hashLabel); nameBadge.add(nameLabel);
        }
        nameBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Chords display
        String chordDisplay = buildChordDisplay(section);
        JLabel chordsLabel = new JLabel(chordDisplay.isEmpty() ? "（コードなし）" : chordDisplay);
        chordsLabel.setFont(AppTheme.monoFont(13));
        chordsLabel.setForeground(chordDisplay.isEmpty() ? AppTheme.TEXT_MUTED : AppTheme.ACCENT_LIGHT);
        chordsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        info.add(nameBadge);
        info.add(Box.createVerticalStrut(4));
        info.add(chordsLabel);

        // --- Right: control buttons
        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));

        JPanel arrowRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        arrowRow.setOpaque(false);

        JButton upBtn = iconBtn("▲");
        upBtn.addActionListener(e -> { if (idx > 0) { swap(idx, idx-1); refreshSections(); } });
        upBtn.setEnabled(idx > 0);

        JButton downBtn = iconBtn("▼");
        downBtn.addActionListener(e -> { if (idx < sections.size()-1) { swap(idx, idx+1); refreshSections(); } });
        downBtn.setEnabled(idx < sections.size()-1);

        arrowRow.add(upBtn); arrowRow.add(downBtn);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actionRow.setOpaque(false);

        JButton editBtn = iconBtn("✏");
        editBtn.addActionListener(e -> openSectionEditor(section, idx));

        JButton deleteBtn = iconBtn("✕");
        deleteBtn.setForeground(new Color(220,80,80));
        deleteBtn.addActionListener(e -> {
            sections.remove(idx);
            refreshSections();
        });

        actionRow.add(editBtn); actionRow.add(deleteBtn);

        controls.add(arrowRow);
        controls.add(Box.createVerticalStrut(4));
        controls.add(actionRow);

        card.add(info, BorderLayout.CENTER);
        card.add(controls, BorderLayout.EAST);

        // Double-click to edit
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openSectionEditor(section, idx);
            }
        });

        return card;
    }

    private String buildChordDisplay(SongSection section) {
        List<String> chords = section.getChords();
        if (chords.isEmpty()) return "";
        String songKey = (String) songKeyCombo.getSelectedItem();
        if (songKey == null) songKey = "C";

        // C基準 → 楽曲キーにトランスポーズ
        List<String> transposed = ChordTransposer.transposeList(chords, "C", songKey);

        if (settings.getNoteMode() == AppSettings.NoteMode.DEGREE) {
            return String.join("  ", DegreeConverter.convertList(transposed, songKey));
        }
        return String.join("  ", transposed);
    }

    // ── Add Section Dialog ───────────────────────────────────────────────────
    private void showAddSectionDialog() {
        JDialog dlg = new JDialog(this, "セクション追加", true);
        dlg.setSize(580, 500);
        dlg.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.BG_DARK);
        root.setBorder(new EmptyBorder(20,24,16,24));
        dlg.setContentPane(root);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        // Section name
        form.add(fieldLabel("セクション名 *"));
        form.add(Box.createVerticalStrut(6));
        JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        nameRow.setOpaque(false);
        nameRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JComboBox<String> presetCombo = new JComboBox<>(SECTION_PRESETS);
        presetCombo.setFont(AppTheme.bodyFont(13));
        presetCombo.setBackground(AppTheme.BG_CARD);
        presetCombo.setForeground(AppTheme.TEXT_PRIMARY);
        presetCombo.setPreferredSize(new Dimension(140, 34));
        JTextField customNameField = new JTextField();
        customNameField.setFont(AppTheme.bodyFont(13));
        customNameField.setBackground(AppTheme.BG_CARD);
        customNameField.setForeground(AppTheme.TEXT_PRIMARY);
        customNameField.setCaretColor(AppTheme.ACCENT_LIGHT);
        customNameField.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER,1,true),
            new EmptyBorder(5,8,5,8)));
        customNameField.setPreferredSize(new Dimension(200, 34));
        customNameField.setEnabled(false);
        customNameField.setBackground(AppTheme.BG_DARK);
        presetCombo.addActionListener(e -> {
            String sel = (String) presetCombo.getSelectedItem();
            boolean custom = "カスタム...".equals(sel);
            customNameField.setEnabled(custom);
            customNameField.setBackground(custom ? AppTheme.BG_CARD : AppTheme.BG_DARK);
        });
        nameRow.add(presetCombo); nameRow.add(customNameField);
        form.add(nameRow);
        form.add(Box.createVerticalStrut(14));

        // Repeat count
        form.add(fieldLabel("繰り返し回数"));
        form.add(Box.createVerticalStrut(6));
        SpinnerNumberModel spinModel = new SpinnerNumberModel(1, 1, 16, 1);
        JSpinner repeatSpinner = new JSpinner(spinModel);
        repeatSpinner.setFont(AppTheme.bodyFont(13));
        repeatSpinner.setBackground(AppTheme.BG_CARD);
        ((JSpinner.DefaultEditor) repeatSpinner.getEditor()).getTextField().setBackground(AppTheme.BG_CARD);
        ((JSpinner.DefaultEditor) repeatSpinner.getEditor()).getTextField().setForeground(AppTheme.TEXT_PRIMARY);
        repeatSpinner.setMaximumSize(new Dimension(100, 34));
        repeatSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(repeatSpinner);
        form.add(Box.createVerticalStrut(14));

        // Source: registered progression or manual
        form.add(fieldLabel("コードソース"));
        form.add(Box.createVerticalStrut(6));
        JRadioButton fromRegBtn = styledRadio("登録済みコード進行から選択");
        JRadioButton manualBtn  = styledRadio("手動でコードを入力");
        ButtonGroup bg = new ButtonGroup();
        bg.add(fromRegBtn); bg.add(manualBtn);
        fromRegBtn.setSelected(true);
        form.add(fromRegBtn);
        form.add(Box.createVerticalStrut(4));
        form.add(manualBtn);
        form.add(Box.createVerticalStrut(10));

        // Registered progression selector
        List<ChordProgression> all = repo.getAll();
        String[] progNames = all.stream().map(p -> p.getTitle() + "  [" + p.getKey() + "]  " + p.getChordsAsString())
                               .toArray(String[]::new);
        JComboBox<String> progCombo = new JComboBox<>(progNames.length > 0 ? progNames : new String[]{"(登録なし)"});
        progCombo.setFont(AppTheme.bodyFont(12));
        progCombo.setBackground(AppTheme.BG_CARD);
        progCombo.setForeground(AppTheme.TEXT_PRIMARY);
        progCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        progCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(progCombo);
        form.add(Box.createVerticalStrut(10));

        // Manual chord input area
        JLabel manualLabel = fieldLabel("コード入力（スペース区切り）  例: Cm Dm Em Gm");
        JTextField manualField = new JTextField();
        manualField.setFont(AppTheme.monoFont(13));
        manualField.setBackground(AppTheme.BG_CARD);
        manualField.setForeground(AppTheme.TEXT_PRIMARY);
        manualField.setCaretColor(AppTheme.ACCENT_LIGHT);
        manualField.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER,1,true),
            new EmptyBorder(6,10,6,10)));
        manualField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        manualField.setAlignmentX(Component.LEFT_ALIGNMENT);
        manualLabel.setVisible(false);
        manualField.setVisible(false);

        form.add(manualLabel);
        form.add(Box.createVerticalStrut(4));
        form.add(manualField);

        fromRegBtn.addActionListener(e -> { progCombo.setVisible(true); manualLabel.setVisible(false); manualField.setVisible(false); });
        manualBtn.addActionListener(e -> { progCombo.setVisible(false); manualLabel.setVisible(true); manualField.setVisible(true); });

        root.add(form, BorderLayout.CENTER);

        // Footer buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(12,0,0,0));
        JButton cancelB = toolBtn("キャンセル", AppTheme.BG_CARD, AppTheme.TEXT_SECONDARY);
        cancelB.addActionListener(e -> dlg.dispose());
        JButton addB = toolBtn("  追加  ", AppTheme.ACCENT, Color.WHITE);
        addB.addActionListener(e -> {
            String selPreset = (String) presetCombo.getSelectedItem();
            String name = "カスタム...".equals(selPreset)
                ? customNameField.getText().trim()
                : selPreset;
            if (name == null || name.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "セクション名を入力してください", "エラー", JOptionPane.WARNING_MESSAGE);
                return;
            }
            SongSection sec = new SongSection(name);
            sec.setRepeatCount((Integer) repeatSpinner.getValue());
            if (fromRegBtn.isSelected() && !all.isEmpty()) {
                int pidx = progCombo.getSelectedIndex();
                if (pidx >= 0 && pidx < all.size()) {
                    ChordProgression cp = all.get(pidx);
                    sec.setProgressionId(cp.getId());
                    sec.setChords(new ArrayList<>(cp.getChords()));
                }
            } else {
                String raw = manualField.getText().trim();
                if (!raw.isEmpty()) {
                    List<String> chords = new ArrayList<>();
                    for (String c : raw.split("\\s+")) if (!c.isEmpty()) chords.add(c);
                    sec.setChords(chords);
                }
            }
            sections.add(sec);
            refreshSections();
            dlg.dispose();
        });
        footer.add(cancelB); footer.add(addB);
        root.add(footer, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ── Section Editor ────────────────────────────────────────────────────────
    private void openSectionEditor(SongSection section, int idx) {
        JDialog dlg = new JDialog(this, "セクション編集: #" + section.getSectionName(), true);
        dlg.setSize(780, 580);
        dlg.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.BG_DARK);
        root.setBorder(new EmptyBorder(16,24,12,24));
        dlg.setContentPane(root);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        // セクション名
        form.add(fieldLabel("セクション名"));
        form.add(Box.createVerticalStrut(5));
        JTextField nameF = styledTextField(section.getSectionName());
        form.add(nameF);
        form.add(Box.createVerticalStrut(10));

        // 繰り返し回数
        form.add(fieldLabel("繰り返し回数"));
        form.add(Box.createVerticalStrut(5));
        SpinnerNumberModel sm = new SpinnerNumberModel(section.getRepeatCount(), 1, 16, 1);
        JSpinner repeatSpin = new JSpinner(sm);
        repeatSpin.setFont(AppTheme.bodyFont(13));
        repeatSpin.setBackground(AppTheme.BG_CARD);
        ((JSpinner.DefaultEditor)repeatSpin.getEditor()).getTextField().setBackground(AppTheme.BG_CARD);
        ((JSpinner.DefaultEditor)repeatSpin.getEditor()).getTextField().setForeground(AppTheme.TEXT_PRIMARY);
        repeatSpin.setMaximumSize(new Dimension(100, 34));
        repeatSpin.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(repeatSpin);
        form.add(Box.createVerticalStrut(10));

        // キー選択（コードセレクタのキー基準）
        String sectionKey = "C";
        if (section.getProgressionId() != null) {
            ChordProgression cp = repo.findById(section.getProgressionId());
            if (cp != null) sectionKey = cp.getKey();
        }
        form.add(fieldLabel("コード選択"));
        form.add(Box.createVerticalStrut(6));
        ChordSelectorPanel selector = new ChordSelectorPanel(sectionKey);
        selector.setChords(section.getChords());
        selector.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(selector);
        form.add(Box.createVerticalStrut(10));

        // 歌詞入力
        form.add(fieldLabel("歌詞（任意）"));
        form.add(Box.createVerticalStrut(6));
        JTextArea lyricsArea = new JTextArea(section.getLyrics(), 4, 30);
        lyricsArea.setFont(AppTheme.bodyFont(13));
        lyricsArea.setForeground(AppTheme.TEXT_PRIMARY);
        lyricsArea.setBackground(AppTheme.BG_CARD);
        lyricsArea.setCaretColor(AppTheme.ACCENT_LIGHT);
        lyricsArea.setLineWrap(true);
        lyricsArea.setWrapStyleWord(true);
        lyricsArea.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
            new EmptyBorder(6, 8, 6, 8)));
        JScrollPane lyricsScroll = new JScrollPane(lyricsArea);
        lyricsScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        lyricsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        lyricsScroll.setBorder(null);
        form.add(lyricsScroll);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBackground(AppTheme.BG_DARK);
        scroll.getViewport().setBackground(AppTheme.BG_DARK);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10,0,0,0));
        JButton cancelB = toolBtn("キャンセル", AppTheme.BG_CARD, AppTheme.TEXT_SECONDARY);
        cancelB.addActionListener(e -> dlg.dispose());
        JButton saveB = toolBtn("  保存  ", AppTheme.ACCENT, Color.WHITE);
        saveB.addActionListener(e -> {
            String newName = nameF.getText().trim();
            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(dlg,"セクション名を入力してください","エラー",JOptionPane.WARNING_MESSAGE);
                return;
            }
            section.setSectionName(newName);
            section.setRepeatCount((Integer)repeatSpin.getValue());
            List<String> newChords = selector.getSelectedChords();
            section.setChords(newChords);
            section.setLyrics(lyricsArea.getText().trim());
            refreshSections();
            dlg.dispose();
        });
        footer.add(cancelB); footer.add(saveB);
        root.add(footer, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ── Preview / Export ─────────────────────────────────────────────────────
    private void updatePreview() {
        previewArea.setText(buildTextOutput());
    }

    private String buildTextOutput() {
        StringBuilder sb = new StringBuilder();
        String songTitle = songTitleField.getText().trim();
        if (songTitle.isEmpty()) songTitle = "（無題）";
        String songKey = (String) songKeyCombo.getSelectedItem();
        if (songKey == null) songKey = "C";

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("  ").append(songTitle).append("  [ Key: ").append(songKey).append(" ]\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        if (sections.isEmpty()) {
            sb.append("（セクションがありません）\n");
        } else {
            for (SongSection sec : sections) {
                sb.append("# ").append(sec.getSectionName());
                if (sec.getRepeatCount() > 1) sb.append("  ×").append(sec.getRepeatCount());
                sb.append("\n");
                if (sec.getChords().isEmpty()) {
                    sb.append("  （コードなし）\n");
                } else {
                    // C基準 → 楽曲キーにトランスポーズ
                    List<String> transposed = ChordTransposer.transposeList(sec.getChords(), "C", songKey);
                    List<String> displayChords;
                    if (settings.getNoteMode() == AppSettings.NoteMode.DEGREE) {
                        displayChords = DegreeConverter.convertList(transposed, songKey);
                    } else {
                        displayChords = transposed;
                    }
                    sb.append("  ").append(String.join("  ", displayChords)).append("\n");
                }
                // 歌詞があれば表示
                if (!sec.getLyrics().isEmpty()) {
                    sb.append("  ♪ ").append(sec.getLyrics().replace("\n", "\n  　")).append("\n");
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private void exportText() {
        String text = buildTextOutput();
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File(songTitleField.getText().trim() + ".txt"));
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("テキストファイル (*.txt)", "txt"));
        int result = fc.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fc.getSelectedFile();
            if (!file.getName().endsWith(".txt")) file = new java.io.File(file.getPath() + ".txt");
            try (java.io.PrintWriter pw = new java.io.PrintWriter(
                    new java.io.OutputStreamWriter(new java.io.FileOutputStream(file), "UTF-8"))) {
                pw.print(text);
                JOptionPane.showMessageDialog(this, "保存しました:\n" + file.getAbsolutePath(),
                    "エクスポート完了", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "保存エラー: " + ex.getMessage(),
                    "エラー", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── Utilities ─────────────────────────────────────────────────────────────
    private void swap(int i, int j) {
        SongSection tmp = sections.get(i);
        sections.set(i, sections.get(j));
        sections.set(j, tmp);
    }

    private JLabel fieldLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(AppTheme.bodyFont(12));
        l.setForeground(AppTheme.TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField styledTextField(String value) {
        JTextField tf = new JTextField(value);
        tf.setFont(AppTheme.bodyFont(13));
        tf.setForeground(AppTheme.TEXT_PRIMARY);
        tf.setBackground(AppTheme.BG_CARD);
        tf.setCaretColor(AppTheme.ACCENT_LIGHT);
        tf.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER,1,true),
            new EmptyBorder(6,10,6,10)));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tf;
    }

    private JRadioButton styledRadio(String text) {
        JRadioButton rb = new JRadioButton(text);
        rb.setFont(AppTheme.bodyFont(13));
        rb.setForeground(AppTheme.TEXT_PRIMARY);
        rb.setBackground(AppTheme.BG_DARK);
        rb.setOpaque(false);
        rb.setFocusPainted(false);
        rb.setAlignmentX(Component.LEFT_ALIGNMENT);
        return rb;
    }

    private JButton toolBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),7,7));
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
        btn.setBorder(new EmptyBorder(6,12,6,12));
        return btn;
    }

    private JButton iconBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(AppTheme.bodyFont(11));
        btn.setForeground(AppTheme.TEXT_SECONDARY);
        btn.setBackground(AppTheme.BG_CARD);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(3,6,3,6));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setForeground(AppTheme.ACCENT_LIGHT); }
            @Override public void mouseExited(MouseEvent e) { btn.setForeground(AppTheme.TEXT_SECONDARY); }
        });
        return btn;
    }
}
