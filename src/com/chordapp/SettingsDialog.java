package com.chordapp;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * 設定ダイアログ（ウィンドウサイズ・表示モード・お気に入りBPM）
 */
public class SettingsDialog extends JDialog {

    private final MainMenuWindow mainWindow;
    private final AppSettings settings = AppSettings.getInstance();

    private java.util.Map<AppSettings.WindowSize, JRadioButton> sizeRadios;
    private java.util.Map<AppSettings.ColorTheme, JRadioButton> themeRadios;
    private JRadioButton chordNameRadio;
    private JRadioButton degreeRadio;
    private DefaultListModel<String> bpmListModel;
    private JList<String> bpmList;
    private JTextField newBpmField;

    public SettingsDialog(MainMenuWindow parent) {
        super(parent, "設定", true);
        this.mainWindow = parent;
        setSize(560, 520);
        setLocationRelativeTo(parent);
        setResizable(false);

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
                g2.setColor(new Color(100,180,255));
                g2.fillRect(0,getHeight()-2,getWidth(),2);
                g2.dispose();
            }
        };
        h.setPreferredSize(new Dimension(560,56));
        h.setBorder(new EmptyBorder(12,24,12,24));
        JLabel title = new JLabel("⚙  設定");
        title.setFont(AppTheme.titleFont(17));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        h.add(title, BorderLayout.WEST);
        return h;
    }

    private JScrollPane buildContent() {
        JPanel content = new JPanel();
        content.setBackground(AppTheme.BG_DARK);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20,28,10,28));

        // ── ウィンドウサイズ ─────────────────────────────
        content.add(sectionLabel("🖥  ウィンドウサイズ"));
        content.add(Box.createVerticalStrut(10));
        JPanel sizePanel = new JPanel(new GridLayout(2, 2, 12, 8));
        sizePanel.setOpaque(false);
        sizePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        sizePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ButtonGroup sizeGroup = new ButtonGroup();
        sizeRadios = new java.util.LinkedHashMap<>();
        for (AppSettings.WindowSize ws : AppSettings.WindowSize.values()) {
            JRadioButton rb = styledRadio(ws.label);
            if (ws == settings.getWindowSize()) rb.setSelected(true);
            sizeGroup.add(rb);
            sizeRadios.put(ws, rb);
            sizePanel.add(rb);
        }
        content.add(sizePanel);
        content.add(Box.createVerticalStrut(4));
        JLabel sizeNote = new JLabel("  ※ 適用後にウィンドウサイズが変更されます");
        sizeNote.setFont(AppTheme.bodyFont(11));
        sizeNote.setForeground(AppTheme.TEXT_MUTED);
        sizeNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(sizeNote);
        content.add(Box.createVerticalStrut(20));

        // ── ノート表示モード ──────────────────────────────
        content.add(sectionLabel("🎵  コード表示モード"));
        content.add(Box.createVerticalStrut(8));
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        radioPanel.setOpaque(false);
        radioPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ButtonGroup bg = new ButtonGroup();
        chordNameRadio = styledRadio("コードネーム  ( C, Am, FM7... )");
        degreeRadio    = styledRadio("ディグリーネーム  ( Ⅰ, Ⅵm, ⅣM7... )");
        bg.add(chordNameRadio); bg.add(degreeRadio);
        if (settings.getNoteMode() == AppSettings.NoteMode.CHORD_NAME) chordNameRadio.setSelected(true);
        else degreeRadio.setSelected(true);
        radioPanel.add(chordNameRadio);
        radioPanel.add(degreeRadio);
        content.add(radioPanel);
        content.add(Box.createVerticalStrut(4));
        JLabel modeNote = new JLabel("  ※ ディグリーモードはキーを基準に自動変換されます");
        modeNote.setFont(AppTheme.bodyFont(11));
        modeNote.setForeground(AppTheme.TEXT_MUTED);
        modeNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(modeNote);
        content.add(Box.createVerticalStrut(20));

        // ── カラーテーマ ──────────────────────────────────
        content.add(sectionLabel("🎨  カラーテーマ"));
        content.add(Box.createVerticalStrut(10));
        JPanel themePanel = new JPanel();
        themePanel.setOpaque(false);
        themePanel.setLayout(new BoxLayout(themePanel, BoxLayout.Y_AXIS));
        themePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ButtonGroup themeBg = new ButtonGroup();
        themeRadios = new java.util.LinkedHashMap<>();
        for (AppSettings.ColorTheme t : AppSettings.ColorTheme.values()) {
            JRadioButton rb = styledRadio(t.label);
            rb.setAlignmentX(Component.LEFT_ALIGNMENT);
            if (t == settings.getColorTheme()) rb.setSelected(true);
            themeBg.add(rb);
            themeRadios.put(t, rb);
            themePanel.add(rb);
            themePanel.add(Box.createVerticalStrut(4));
        }
        content.add(themePanel);
        content.add(Box.createVerticalStrut(4));
        JLabel themeNote = new JLabel("  ※ 適用後に再起動すると完全に反映されます");
        themeNote.setFont(AppTheme.bodyFont(11));
        themeNote.setForeground(AppTheme.TEXT_MUTED);
        themeNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(themeNote);
        content.add(Box.createVerticalStrut(20));

        // ── お気に入りBPM ─────────────────────────────────
        content.add(sectionLabel("♩  お気に入りBPM"));
        content.add(Box.createVerticalStrut(8));

        bpmListModel = new DefaultListModel<>();
        for (String bpm : settings.getFavoriteBpms()) bpmListModel.addElement(bpm);
        bpmList = new JList<>(bpmListModel);
        bpmList.setFont(AppTheme.monoFont(13));
        bpmList.setBackground(AppTheme.BG_CARD);
        bpmList.setForeground(AppTheme.TEXT_PRIMARY);
        bpmList.setSelectionBackground(AppTheme.ACCENT);
        bpmList.setSelectionForeground(Color.WHITE);
        bpmList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        bpmList.setVisibleRowCount(2);
        bpmList.setFixedCellWidth(70);
        bpmList.setFixedCellHeight(28);
        JScrollPane bpmScroll = new JScrollPane(bpmList);
        bpmScroll.setPreferredSize(new Dimension(Integer.MAX_VALUE, 72));
        bpmScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        bpmScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        bpmScroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        bpmScroll.setBackground(AppTheme.BG_CARD);
        bpmScroll.getViewport().setBackground(AppTheme.BG_CARD);
        content.add(bpmScroll);
        content.add(Box.createVerticalStrut(8));

        JPanel bpmInputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bpmInputRow.setOpaque(false);
        bpmInputRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        newBpmField = new JTextField(6);
        newBpmField.setFont(AppTheme.monoFont(13));
        newBpmField.setBackground(AppTheme.BG_CARD);
        newBpmField.setForeground(AppTheme.TEXT_PRIMARY);
        newBpmField.setCaretColor(AppTheme.ACCENT_LIGHT);
        newBpmField.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER,1,true),
            new EmptyBorder(5,8,5,8)));
        newBpmField.setPreferredSize(new Dimension(80, 32));

        JButton addBpmBtn = smallBtn("追加", AppTheme.ACCENT, Color.WHITE);
        addBpmBtn.addActionListener(e -> {
            String bpm = newBpmField.getText().trim();
            if (!bpm.isEmpty()) {
                try { Integer.parseInt(bpm); } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "数値を入力してください", "エラー", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (!bpmListModel.contains(bpm)) bpmListModel.addElement(bpm);
                newBpmField.setText("");
            }
        });

        JButton delBpmBtn = smallBtn("削除", new Color(180,60,60), Color.WHITE);
        delBpmBtn.addActionListener(e -> {
            String sel = bpmList.getSelectedValue();
            if (sel != null) bpmListModel.removeElement(sel);
        });

        bpmInputRow.add(new JLabel("BPM値:") {{ setForeground(AppTheme.TEXT_SECONDARY); setFont(AppTheme.bodyFont(12)); }});
        bpmInputRow.add(newBpmField);
        bpmInputRow.add(addBpmBtn);
        bpmInputRow.add(delBpmBtn);
        content.add(bpmInputRow);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBackground(AppTheme.BG_DARK);
        scroll.getViewport().setBackground(AppTheme.BG_DARK);
        scroll.setBorder(null);
        // スクロールバーを細く（約1/6幅）
        JScrollBar vsb = scroll.getVerticalScrollBar();
        vsb.setPreferredSize(new Dimension(8, Integer.MAX_VALUE));
        vsb.setUnitIncrement(16);
        vsb.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = AppTheme.BORDER;
                trackColor = AppTheme.BG_PANEL;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            private JButton zeroBtn() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setMinimumSize(new Dimension(0, 0));
                b.setMaximumSize(new Dimension(0, 0));
                return b;
            }
        });
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(AppTheme.BG_PANEL);
        footer.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0,AppTheme.BORDER),
            new EmptyBorder(12,24,12,24)));
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        JButton cancelBtn = smallBtn("キャンセル", AppTheme.BG_CARD, AppTheme.TEXT_SECONDARY);
        cancelBtn.addActionListener(e -> dispose());
        JButton applyBtn = smallBtn("  適用する  ", AppTheme.ACCENT, Color.WHITE);
        applyBtn.addActionListener(e -> applySettings());
        btns.add(cancelBtn); btns.add(applyBtn);
        footer.add(btns, BorderLayout.EAST);
        return footer;
    }

    private void applySettings() {
        AppSettings.WindowSize ws = settings.getWindowSize();
        for (java.util.Map.Entry<AppSettings.WindowSize, JRadioButton> e : sizeRadios.entrySet()) {
            if (e.getValue().isSelected()) { ws = e.getKey(); break; }
        }
        settings.setWindowSize(ws);
        settings.setNoteMode(chordNameRadio.isSelected()
            ? AppSettings.NoteMode.CHORD_NAME : AppSettings.NoteMode.DEGREE);

        // カラーテーマ適用
        for (java.util.Map.Entry<AppSettings.ColorTheme, JRadioButton> e : themeRadios.entrySet()) {
            if (e.getValue().isSelected()) { settings.setColorTheme(e.getKey()); break; }
        }

        List<String> bpms = new ArrayList<>();
        for (int i = 0; i < bpmListModel.size(); i++) bpms.add(bpmListModel.get(i));
        settings.setFavoriteBpms(bpms);

        // ウィンドウサイズ・テーマ適用
        mainWindow.setSize(ws.width, ws.height);
        mainWindow.setLocationRelativeTo(null);
        mainWindow.refreshAll();

        dispose();
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppTheme.titleFont(13));
        l.setForeground(AppTheme.TEXT_PRIMARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JRadioButton styledRadio(String text) {
        JRadioButton rb = new JRadioButton(text);
        rb.setFont(AppTheme.bodyFont(13));
        rb.setForeground(AppTheme.TEXT_PRIMARY);
        rb.setBackground(AppTheme.BG_DARK);
        rb.setOpaque(false);
        rb.setFocusPainted(false);
        return rb;
    }

    private JButton smallBtn(String text, Color bg, Color fg) {
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
        btn.setBorder(new EmptyBorder(6,14,6,14));
        return btn;
    }
}
