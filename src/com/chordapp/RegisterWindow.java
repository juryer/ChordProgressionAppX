package com.chordapp;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * コード進行登録ウィンドウ
 * キー設定なし・C/Cm基準でコードを登録する
 */
public class RegisterWindow extends JDialog {

    private MainMenuWindow parent;
    private ChordProgressionRepository repo = ChordProgressionRepository.getInstance();

    private JTextField titleField;
    private JComboBox<String> tempoCombo;
    private JTextField memoField;
    private ChordSelectorPanel chordSelector;
    private int[] currentRating = {3}; // デフォルト3

    public RegisterWindow(MainMenuWindow parent) {
        super(parent, "コード進行登録", true);
        this.parent = parent;
        setSize(1060, 700);
        setLocationRelativeTo(parent);
        setResizable(true);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.BG_DARK);
        setContentPane(root);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildForm(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
    }

    // ── Header ──────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(AppTheme.BG_PANEL);
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(AppTheme.ACCENT);
                g2.fillRect(0,getHeight()-2,getWidth(),2);
                g2.dispose();
            }
        };
        h.setPreferredSize(new Dimension(1060,60));
        h.setBorder(new EmptyBorder(14,24,14,24));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        JLabel title = new JLabel("＋  コード進行登録");
        title.setFont(AppTheme.titleFont(18));
        title.setForeground(AppTheme.TEXT_PRIMARY);

        // C基準の案内
        JLabel keyNote = new JLabel("  ※ C / Cm 基準で登録します。移調は楽曲エディタで行えます。");
        keyNote.setFont(AppTheme.bodyFont(11));
        keyNote.setForeground(AppTheme.TEXT_MUTED);

        left.add(title); left.add(keyNote);
        h.add(left, BorderLayout.WEST);
        return h;
    }

    // ── Form ────────────────────────────────────────────────────────────────
    private JScrollPane buildForm() {
        JPanel form = new JPanel();
        form.setBackground(AppTheme.BG_DARK);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20,36,20,36));

        // タイトル
        form.add(buildLabel("タイトル *"));
        form.add(Box.createVerticalStrut(6));
        titleField = buildTextField("例: 王道ポップ進行");
        form.add(titleField);
        form.add(Box.createVerticalStrut(16));

        // BPMのみ（キー設定なし）
        JPanel tempoBlock = new JPanel();
        tempoBlock.setOpaque(false);
        tempoBlock.setLayout(new BoxLayout(tempoBlock, BoxLayout.Y_AXIS));
        tempoBlock.setMaximumSize(new Dimension(300, 70));
        tempoBlock.setAlignmentX(Component.LEFT_ALIGNMENT);
        tempoBlock.add(buildLabel("お気に入りBPM"));
        tempoBlock.add(Box.createVerticalStrut(5));
        List<String> bpmOptions = new ArrayList<>();
        bpmOptions.add("─ 未設定");
        bpmOptions.addAll(AppSettings.getInstance().getFavoriteBpms());
        tempoCombo = buildComboBox(bpmOptions.toArray(new String[0]));
        tempoBlock.add(tempoCombo);
        form.add(tempoBlock);
        form.add(Box.createVerticalStrut(16));

        // コード選択パネル（C基準固定）
        form.add(buildLabel("コード進行 *  （C / Cm 基準）"));
        form.add(Box.createVerticalStrut(8));
        chordSelector = new ChordSelectorPanel("C");
        chordSelector.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(chordSelector);
        form.add(Box.createVerticalStrut(16));

        // レーティング
        form.add(buildLabel("レーティング"));
        form.add(Box.createVerticalStrut(6));
        JPanel ratingRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        ratingRow.setOpaque(false);
        ratingRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel[] stars = new JLabel[5];
        for (int i = 0; i < 5; i++) {
            final int star = i + 1;
            stars[i] = new JLabel(i < currentRating[0] ? "★" : "☆");
            stars[i].setFont(new Font("SansSerif", Font.PLAIN, 24));
            stars[i].setForeground(new Color(255, 200, 50));
            stars[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            stars[i].addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    currentRating[0] = star;
                    for (int j = 0; j < 5; j++) stars[j].setText(j < star ? "★" : "☆");
                }
                @Override public void mouseEntered(MouseEvent e) {
                    for (int j = 0; j < 5; j++) stars[j].setText(j < star ? "★" : "☆");
                }
                @Override public void mouseExited(MouseEvent e) {
                    for (int j = 0; j < 5; j++) stars[j].setText(j < currentRating[0] ? "★" : "☆");
                }
            });
            ratingRow.add(stars[i]);
        }
        form.add(ratingRow);
        form.add(Box.createVerticalStrut(16));

        // メモ
        form.add(buildLabel("メモ"));
        form.add(Box.createVerticalStrut(6));
        memoField = buildTextField("任意のメモを入力...");
        form.add(memoField);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBackground(AppTheme.BG_DARK);
        scroll.getViewport().setBackground(AppTheme.BG_DARK);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    // ── Footer ──────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(AppTheme.BG_PANEL);
        footer.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0,AppTheme.BORDER),
            new EmptyBorder(12,24,12,24)));
        JLabel req = new JLabel("* は必須項目");
        req.setFont(AppTheme.bodyFont(11));
        req.setForeground(AppTheme.TEXT_MUTED);
        footer.add(req, BorderLayout.WEST);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        JButton cancelBtn = buildButton("キャンセル", AppTheme.BG_CARD, AppTheme.TEXT_SECONDARY);
        cancelBtn.addActionListener(e -> dispose());
        JButton clearBtn = buildButton("クリア", AppTheme.BG_CARD, AppTheme.TEXT_MUTED);
        clearBtn.addActionListener(e -> clearForm());
        JButton saveBtn = buildButton("  保存する  ", AppTheme.ACCENT, Color.WHITE);
        saveBtn.addActionListener(e -> saveProgression());
        btnRow.add(cancelBtn); btnRow.add(clearBtn); btnRow.add(saveBtn);
        footer.add(btnRow, BorderLayout.EAST);
        return footer;
    }

    // ── Actions ─────────────────────────────────────────────────────────────
    private void clearForm() {
        titleField.setText(""); memoField.setText("");
        tempoCombo.setSelectedIndex(0);
        chordSelector.setKey("C");
        currentRating[0] = 3;
    }

    private void saveProgression() {
        String title = titleField.getText().trim();
        if (title.isEmpty() || title.equals("例: 王道ポップ進行")) {
            showError("タイトルを入力してください。"); return;
        }
        List<String> chords = chordSelector.getSelectedChords();
        if (chords.isEmpty()) {
            showError("コードを1つ以上選択してください。"); return;
        }
        String tempoRaw = (String) tempoCombo.getSelectedItem();
        String tempo = (tempoRaw == null || tempoRaw.startsWith("─")) ? "─" : tempoRaw;
        String memoRaw = memoField.getText().trim();
        String memo = memoRaw.equals("任意のメモを入力...") ? "" : memoRaw;
        // キーはC固定で保存
        ChordProgression cp = new ChordProgression(title, "C", tempo, chords, memo);
        cp.setRating(currentRating[0]);
        repo.add(cp);
        parent.refreshRecentCards();
        JOptionPane.showMessageDialog(this, "「" + title + "」を登録しました！",
            "登録完了", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "入力エラー", JOptionPane.WARNING_MESSAGE);
    }

    // ── UI Helpers ──────────────────────────────────────────────────────────
    private JLabel buildLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppTheme.bodyFont(12));
        l.setForeground(AppTheme.TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField buildTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(AppTheme.bodyFont(14));
        tf.setForeground(AppTheme.TEXT_MUTED);
        tf.setBackground(AppTheme.BG_CARD);
        tf.setCaretColor(AppTheme.ACCENT_LIGHT);
        tf.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER,1,true),
            new EmptyBorder(8,12,8,12)));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        tf.setText(placeholder);
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText(""); tf.setForeground(AppTheme.TEXT_PRIMARY);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) {
                    tf.setText(placeholder); tf.setForeground(AppTheme.TEXT_MUTED);
                }
            }
        });
        return tf;
    }

    private JComboBox<String> buildComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(AppTheme.bodyFont(13));
        cb.setBackground(AppTheme.BG_CARD);
        cb.setForeground(AppTheme.TEXT_PRIMARY);
        cb.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER,1));
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
        return cb;
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
