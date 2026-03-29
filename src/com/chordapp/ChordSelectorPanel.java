package com.chordapp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

/**
 * コード選択UI共通パネル
 * 修正内容:
 * ・カテゴリタブをキー基準でソートして表示（ディグリー順ズレ修正）
 * ・選択済みプレビューは常にコードネーム表記
 * ・sus2 → add9 に変更
 */
public class ChordSelectorPanel extends JPanel {

    // ── 定数 ────────────────────────────────────────────────────────────────
    private static final String[] ROOTS = {
        "C","C#","D","D#","E","F","F#","G","G#","A","A#","B"
    };
    private static final String[] TYPES = {
        "メジャー","m","7","M7","m7","m7♭5","9","m9","sus4","add9","aug","dim","dim7"
    };
    private static final String[] TYPE_SUFFIX = {
        "","m","7","M7","m7","m7b5","9","m9","sus4","add9","aug","dim","dim7"
    };

    // タブカテゴリ定義
    private static final String[][] TAB_NAMES = {
        {"メジャー"}, {"マイナー"}, {"7th"}, {"M7"}, {"m7"}, {"m7♭5"}, {"9"}, {"m9"}, {"sus4"}, {"aug/dim"}
    };
    private static final String[][] TAB_CHORD_LIST = {
        {"C","D","E","F","G","A","B","C#","D#","F#","G#","A#"},
        {"Cm","Dm","Em","Fm","Gm","Am","Bm","C#m","D#m","F#m","G#m","A#m"},
        {"C7","D7","E7","F7","G7","A7","B7","C#7","D#7","F#7","G#7","A#7"},
        {"CM7","DM7","EM7","FM7","GM7","AM7","BM7","C#M7","D#M7","F#M7","G#M7","A#M7"},
        {"Cm7","Dm7","Em7","Fm7","Gm7","Am7","Bm7","C#m7","D#m7","F#m7","G#m7","A#m7"},
        {"Cm7b5","Dm7b5","Em7b5","Fm7b5","Gm7b5","Am7b5","Bm7b5",
         "C#m7b5","D#m7b5","F#m7b5","G#m7b5","A#m7b5"},
        {"C9","D9","E9","F9","G9","A9","B9","C#9","D#9","F#9","G#9","A#9"},
        {"Cm9","Dm9","Em9","Fm9","Gm9","Am9","Bm9","C#m9","D#m9","F#m9","G#m9","A#m9"},
        {"Csus4","Dsus4","Esus4","Fsus4","Gsus4","Asus4","Bsus4"},
        {"Caug","Daug","Eaug","Faug","Gaug","Aaug","Baug",
         "Cdim","Ddim","Edim","Fdim","Gdim","Adim","Bdim"}
    };

    // 音名→半音インデックス
    private static final java.util.Map<String,Integer> NOTE_MAP = new java.util.HashMap<>();
    static {
        NOTE_MAP.put("C",0); NOTE_MAP.put("C#",1); NOTE_MAP.put("Db",1);
        NOTE_MAP.put("D",2); NOTE_MAP.put("D#",3); NOTE_MAP.put("Eb",3);
        NOTE_MAP.put("E",4); NOTE_MAP.put("F",5);
        NOTE_MAP.put("F#",6); NOTE_MAP.put("Gb",6);
        NOTE_MAP.put("G",7); NOTE_MAP.put("G#",8); NOTE_MAP.put("Ab",8);
        NOTE_MAP.put("A",9); NOTE_MAP.put("A#",10);NOTE_MAP.put("Bb",10);
        NOTE_MAP.put("B",11);
    }

    // ── フィールド ────────────────────────────────────────────────────────────
    private String currentKey = "C";
    private final List<String> selectedChords = new ArrayList<>();
    private final List<Runnable> changeListeners = new ArrayList<>();

    private JPanel diatonicPanel;
    private JPanel selectedPanel;
    private JPanel selectedChipsPanel;
    private String selectedRoot = null;

    // ── コンストラクタ ────────────────────────────────────────────────────────
    public ChordSelectorPanel(String key) {
        this.currentKey = key;
        setBackground(AppTheme.BG_DARK);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(0, 0, 0, 0));
        build();
    }

    private void build() {
        removeAll();
        add(buildSectionLabel("ダイアトニックコード（キー: " + currentKey + "）"));
        add(Box.createVerticalStrut(6));
        diatonicPanel = buildDiatonicPanel();
        add(diatonicPanel);
        add(Box.createVerticalStrut(12));
        add(buildSectionLabel("カテゴリから選択"));
        add(Box.createVerticalStrut(6));
        add(buildTabPanel());
        add(Box.createVerticalStrut(12));
        add(buildSectionLabel("ルート → タイプで選択"));
        add(Box.createVerticalStrut(6));
        add(buildTwoStepPanel());
        add(Box.createVerticalStrut(12));
        add(buildSectionLabel("選択中のコード進行（コードネーム表記）"));
        add(Box.createVerticalStrut(6));
        add(buildSelectedPanel());
        revalidate();
        repaint();
    }

    // ── ① ダイアトニックパネル ───────────────────────────────────────────────
    private JPanel buildDiatonicPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        p.setBackground(AppTheme.BG_PANEL);
        p.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
            new EmptyBorder(8, 8, 8, 8)));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        List<String> diatonic = getDiatonicChords(currentKey);
        for (String chord : diatonic) {
            p.add(buildChordChip(chord, AppTheme.ACCENT, true));
        }
        return p;
    }

    /** キーに基づくダイアトニックコードを返す */
    private List<String> getDiatonicChords(String key) {
        boolean isMinor = key.length() > 1 && key.endsWith("m");
        String root = isMinor ? key.substring(0, key.length()-1) : key;
        String[] majorPattern = {"","m","m","","","m","dim"};
        int[] majorIntervals = {0,2,4,5,7,9,11};
        String[] minorPattern = {"m","dim","","m","m","",""};
        int[] minorIntervals = {0,2,3,5,7,8,10};
        String[] pattern = isMinor ? minorPattern : majorPattern;
        int[] intervals = isMinor ? minorIntervals : majorIntervals;
        int rootIdx = getRootIndex(root);
        List<String> result = new ArrayList<>();
        String[] allRoots = {"C","C#","D","D#","E","F","F#","G","G#","A","A#","B"};
        for (int i = 0; i < 7; i++) {
            int noteIdx = (rootIdx + intervals[i]) % 12;
            result.add(allRoots[noteIdx] + pattern[i]);
        }
        return result;
    }

    private int getRootIndex(String root) {
        Integer idx = NOTE_MAP.get(root);
        return idx != null ? idx : 0;
    }

    /** コードのルート音を抽出 */
    private String extractRoot(String chord) {
        if (chord.length() >= 2 && (chord.charAt(1)=='#'||chord.charAt(1)=='b'))
            return chord.substring(0,2);
        return chord.substring(0,1);
    }

    /** キー基準でコードリストをソート（半音差の昇順） */
    private List<String> sortByKey(String[] chords) {
        boolean isMinor = currentKey.length() > 1 && currentKey.endsWith("m");
        String keyRoot = isMinor ? currentKey.substring(0, currentKey.length()-1) : currentKey;
        int keyIdx = getRootIndex(keyRoot);
        java.util.TreeMap<Integer, String> sorted = new java.util.TreeMap<>();
        for (String chord : chords) {
            String root = extractRoot(chord);
            Integer chordIdx = NOTE_MAP.get(root);
            if (chordIdx != null) {
                int interval = (chordIdx - keyIdx + 12) % 12;
                while (sorted.containsKey(interval)) interval += 100;
                sorted.put(interval, chord);
            }
        }
        return new ArrayList<>(sorted.values());
    }

    // ── ② タブパネル ─────────────────────────────────────────────────────────
    private JPanel buildTabPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        tabBar.setBackground(AppTheme.BG_PANEL);
        tabBar.setBorder(new EmptyBorder(4,6,0,6));

        JPanel chordArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        chordArea.setBackground(AppTheme.BG_PANEL);
        chordArea.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
            new EmptyBorder(6, 6, 6, 6)));
        chordArea.setMinimumSize(new Dimension(0, 52));
        chordArea.setPreferredSize(new Dimension(Integer.MAX_VALUE, 52));

        // 最初のタブ表示（キー基準ソート）
        showTabChords(chordArea, 0);

        JButton[] tabBtns = new JButton[TAB_NAMES.length];
        for (int ti = 0; ti < TAB_NAMES.length; ti++) {
            final int idx = ti;
            JButton tb = new JButton(TAB_NAMES[ti][0]) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    boolean sel = getClientProperty("selected") == Boolean.TRUE;
                    g2.setColor(sel ? AppTheme.ACCENT : AppTheme.BG_CARD);
                    g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),6,6));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            tb.setFont(AppTheme.bodyFont(11));
            tb.setForeground(AppTheme.TEXT_PRIMARY);
            tb.setBackground(AppTheme.BG_CARD);
            tb.setBorderPainted(false); tb.setFocusPainted(false);
            tb.setContentAreaFilled(false); tb.setOpaque(false);
            tb.setBorder(new EmptyBorder(4,10,4,10));
            tb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tabBtns[ti] = tb;
            if (ti == 0) tb.putClientProperty("selected", Boolean.TRUE);
            tb.addActionListener(e -> {
                for (JButton b : tabBtns) b.putClientProperty("selected", Boolean.FALSE);
                tb.putClientProperty("selected", Boolean.TRUE);
                for (JButton b : tabBtns) b.repaint();
                showTabChords(chordArea, idx);
            });
            tabBar.add(tb);
        }

        JPanel tabWrapper = new JPanel(new BorderLayout());
        tabWrapper.setBackground(AppTheme.BG_PANEL);
        tabWrapper.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1, true));
        tabWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
        tabWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        tabWrapper.add(tabBar, BorderLayout.NORTH);
        tabWrapper.add(chordArea, BorderLayout.CENTER);
        wrapper.add(tabWrapper);
        return wrapper;
    }

    /** タブのコードをキー基準でソートして表示 */
    private void showTabChords(JPanel area, int tabIdx) {
        area.removeAll();
        List<String> sorted = sortByKey(TAB_CHORD_LIST[tabIdx]);
        for (String chord : sorted) {
            area.add(buildChordChip(chord, new Color(60,120,180), true));
        }
        area.revalidate();
        area.repaint();
    }

    // ── ③ 2段階選択パネル ────────────────────────────────────────────────────
    private JPanel buildTwoStepPanel() {
        JPanel p = new JPanel();
        p.setBackground(AppTheme.BG_PANEL);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
            new EmptyBorder(8,8,8,8)));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel rootRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        rootRow.setOpaque(false);
        JLabel rootLabel = new JLabel("ルート：");
        rootLabel.setFont(AppTheme.bodyFont(11));
        rootLabel.setForeground(AppTheme.TEXT_SECONDARY);
        rootRow.add(rootLabel);

        JButton[] rootBtns = new JButton[ROOTS.length];
        JPanel typeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        typeRow.setOpaque(false);
        JLabel typeLabel = new JLabel("タイプ：");
        typeLabel.setFont(AppTheme.bodyFont(11));
        typeLabel.setForeground(AppTheme.TEXT_SECONDARY);
        typeRow.add(typeLabel);

        // オンコード用ベース音選択行
        JPanel bassRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        bassRow.setOpaque(false);
        JLabel bassLabel = new JLabel("ベース：");
        bassLabel.setFont(AppTheme.bodyFont(11));
        bassLabel.setForeground(AppTheme.TEXT_SECONDARY);
        bassRow.add(bassLabel);
        JLabel noneLabel = new JLabel("なし");
        noneLabel.setFont(AppTheme.bodyFont(11));
        noneLabel.setForeground(AppTheme.TEXT_MUTED);
        bassRow.add(noneLabel);

        // ベース音の状態
        String[] selectedBass = {null};

        JButton[] bassBtns = new JButton[ROOTS.length];
        for (int bi = 0; bi < ROOTS.length; bi++) {
            final int bidx = bi;
            JButton bb = buildSmallBtn(ROOTS[bi], new Color(160, 100, 200));
            bb.setEnabled(false);
            bb.addActionListener(e -> {
                if (selectedBass[0] != null && selectedBass[0].equals(ROOTS[bidx])) {
                    // 同じボタンを押したらキャンセル
                    selectedBass[0] = null;
                    noneLabel.setForeground(AppTheme.TEXT_MUTED);
                    for (JButton b : bassBtns) b.putClientProperty("selected", Boolean.FALSE);
                } else {
                    selectedBass[0] = ROOTS[bidx];
                    noneLabel.setForeground(new Color(160, 100, 200));
                    for (JButton b : bassBtns) b.putClientProperty("selected", Boolean.FALSE);
                    bb.putClientProperty("selected", Boolean.TRUE);
                }
                for (JButton b : bassBtns) b.repaint();
            });
            bassBtns[bi] = bb;
            bassRow.add(bb);
        }

        JButton[] typeBtns = new JButton[TYPES.length];
        for (int ti = 0; ti < TYPES.length; ti++) {
            final int tidx = ti;
            JButton tb = buildSmallBtn(TYPES[ti], AppTheme.ACCENT2);
            tb.setEnabled(false);
            tb.addActionListener(e -> {
                if (selectedRoot != null) {
                    String chord = selectedRoot + TYPE_SUFFIX[tidx];
                    if (selectedBass[0] != null && !selectedBass[0].equals(selectedRoot)) {
                        chord = chord + "/" + selectedBass[0];
                    }
                    addChord(chord);
                    selectedRoot = null;
                    selectedBass[0] = null;
                    noneLabel.setForeground(AppTheme.TEXT_MUTED);
                    for (JButton rb : rootBtns) rb.putClientProperty("selected", Boolean.FALSE);
                    for (JButton rb : rootBtns) rb.repaint();
                    for (JButton bb : bassBtns) bb.putClientProperty("selected", Boolean.FALSE);
                    for (JButton bb : bassBtns) bb.repaint();
                    for (JButton tb2 : typeBtns) tb2.setEnabled(false);
                    for (JButton bb : bassBtns) bb.setEnabled(false);
                }
            });
            typeBtns[ti] = tb;
            typeRow.add(tb);
        }

        for (int ri = 0; ri < ROOTS.length; ri++) {
            final int ridx = ri;
            JButton rb = buildSmallBtn(ROOTS[ri], new Color(80,140,200));
            rootBtns[ri] = rb;
            rb.addActionListener(e -> {
                selectedRoot = ROOTS[ridx];
                for (JButton b : rootBtns) b.putClientProperty("selected", Boolean.FALSE);
                rb.putClientProperty("selected", Boolean.TRUE);
                for (JButton b : rootBtns) b.repaint();
                for (JButton tb : typeBtns) tb.setEnabled(true);
                for (JButton bb : bassBtns) bb.setEnabled(true);
            });
            rootRow.add(rb);
        }

        p.add(rootRow);
        p.add(typeRow);
        p.add(bassRow);
        return p;
    }

    // ── ④ 選択済みコード表示（チップ形式・個別削除・1つ戻す対応） ────────────

    private JPanel buildSelectedPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 6)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),10,10));
                g2.setColor(AppTheme.ACCENT);
                g2.fill(new RoundRectangle2D.Float(0,0,4,getHeight(),4,4));
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        wrapper.setBorder(new EmptyBorder(8,14,8,10));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        // チップエリア
        selectedChipsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        selectedChipsPanel.setOpaque(false);

        // ボタン行（1つ戻す・クリア）
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnRow.setOpaque(false);

        JButton undoBtn = new JButton("← 1つ戻す");
        undoBtn.setFont(AppTheme.bodyFont(11));
        undoBtn.setForeground(AppTheme.ACCENT_LIGHT);
        undoBtn.setBackground(AppTheme.BG_CARD);
        undoBtn.setBorderPainted(false);
        undoBtn.setFocusPainted(false);
        undoBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        undoBtn.addActionListener(e -> {
            if (!selectedChords.isEmpty()) {
                selectedChords.remove(selectedChords.size() - 1);
                refreshChips();
                fireChangeListeners();
            }
        });

        JButton clearBtn = new JButton("全クリア");
        clearBtn.setFont(AppTheme.bodyFont(11));
        clearBtn.setForeground(AppTheme.TEXT_MUTED);
        clearBtn.setBackground(AppTheme.BG_CARD);
        clearBtn.setBorderPainted(false);
        clearBtn.setFocusPainted(false);
        clearBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> clearChords());

        btnRow.add(undoBtn);
        btnRow.add(clearBtn);

        wrapper.add(selectedChipsPanel, BorderLayout.CENTER);
        wrapper.add(btnRow, BorderLayout.EAST);
        selectedPanel = wrapper;

        refreshChips();
        return wrapper;
    }

    /** チップエリアを再描画する */
    private void refreshChips() {
        selectedChipsPanel.removeAll();

        if (selectedChords.isEmpty()) {
            JLabel empty = new JLabel("コードを選択してください");
            empty.setFont(AppTheme.monoFont(12));
            empty.setForeground(AppTheme.TEXT_SECONDARY);
            selectedChipsPanel.add(empty);
        } else {
            for (int i = 0; i < selectedChords.size(); i++) {
                final int idx = i;
                final String chord = selectedChords.get(i);

                // チップパネル（コード名 + ✕ボタン）
                JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0)) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(AppTheme.chipBg());
                        g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),8,8));
                        g2.dispose();
                    }
                };
                chip.setOpaque(false);
                chip.setBorder(new EmptyBorder(2, 6, 2, 4));
                chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                JLabel chordLabel = new JLabel(chord);
                chordLabel.setFont(AppTheme.monoFont(12));
                chordLabel.setForeground(Color.WHITE);

                JButton removeBtn = new JButton("✕");
                removeBtn.setFont(new Font("SansSerif", Font.PLAIN, 9));
                removeBtn.setForeground(new Color(200, 200, 255));
                removeBtn.setBackground(AppTheme.chipBg());
                removeBtn.setBorderPainted(false);
                removeBtn.setFocusPainted(false);
                removeBtn.setContentAreaFilled(false);
                removeBtn.setOpaque(false);
                removeBtn.setMargin(new Insets(0, 2, 0, 0));
                removeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                removeBtn.addActionListener(e -> {
                    selectedChords.remove(idx);
                    refreshChips();
                    fireChangeListeners();
                });
                removeBtn.addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) {
                        removeBtn.setForeground(new Color(255, 120, 120));
                    }
                    @Override public void mouseExited(MouseEvent e) {
                        removeBtn.setForeground(new Color(200, 200, 255));
                    }
                });

                // ── D&D で並び替え ──────────────────────────────
                chip.setTransferHandler(new TransferHandler("text") {
                    @Override public int getSourceActions(JComponent c) { return MOVE; }
                    @Override protected Transferable createTransferable(JComponent c) {
                        return new java.awt.datatransfer.StringSelection(String.valueOf(idx));
                    }
                });
                chip.addMouseMotionListener(new MouseAdapter() {
                    @Override public void mouseDragged(MouseEvent e) {
                        chip.getTransferHandler().exportAsDrag(chip, e, TransferHandler.MOVE);
                    }
                });
                chip.setDropTarget(new java.awt.dnd.DropTarget(chip,
                    new java.awt.dnd.DropTargetAdapter() {
                        @Override public void drop(java.awt.dnd.DropTargetDropEvent e) {
                            try {
                                e.acceptDrop(java.awt.dnd.DnDConstants.ACTION_MOVE);
                                String data = (String) e.getTransferable()
                                    .getTransferData(java.awt.datatransfer.DataFlavor.stringFlavor);
                                int fromIdx = Integer.parseInt(data);
                                int toIdx = idx;
                                if (fromIdx != toIdx) {
                                    String moved = selectedChords.remove(fromIdx);
                                    selectedChords.add(toIdx, moved);
                                    refreshChips();
                                    fireChangeListeners();
                                }
                                e.dropComplete(true);
                            } catch (Exception ex) { e.dropComplete(false); }
                        }
                    }));

                chip.add(chordLabel);
                chip.add(removeBtn);
                selectedChipsPanel.add(chip);

                // → 区切り（最後以外）
                if (i < selectedChords.size() - 1) {
                    JLabel arrow = new JLabel("→");
                    arrow.setFont(AppTheme.bodyFont(11));
                    arrow.setForeground(AppTheme.TEXT_MUTED);
                    selectedChipsPanel.add(arrow);
                }
            }
            // 残り枠数表示
            JLabel count = new JLabel("(" + selectedChords.size() + "/8)");
            count.setFont(AppTheme.bodyFont(10));
            count.setForeground(AppTheme.TEXT_MUTED);
            selectedChipsPanel.add(count);
        }

        selectedChipsPanel.revalidate();
        selectedChipsPanel.repaint();
        if (selectedPanel != null) {
            selectedPanel.revalidate();
            selectedPanel.repaint();
        }
    }

    // ── コード追加・削除 ─────────────────────────────────────────────────────
    private void addChord(String chord) {
        if (selectedChords.size() >= 8) return;
        selectedChords.add(chord);
        refreshChips();
        fireChangeListeners();
    }

    private void clearChords() {
        selectedChords.clear();
        refreshChips();
        fireChangeListeners();
    }

    /** 後方互換のため残す（refreshChipsに委譲） */
    private void updateSelectedLabel() {
        refreshChips();
    }

    // ── 公開API ──────────────────────────────────────────────────────────────
    public List<String> getSelectedChords() { return new ArrayList<>(selectedChords); }

    public void setKey(String key) {
        this.currentKey = key;
        build();
        updateSelectedLabel();
    }

    public void setChords(List<String> chords) {
        selectedChords.clear();
        selectedChords.addAll(chords);
        if (selectedChipsPanel != null) refreshChips();
    }

    public void addChangeListener(Runnable r) { changeListeners.add(r); }
    private void fireChangeListeners() { changeListeners.forEach(Runnable::run); }

    // ── UI ヘルパー ──────────────────────────────────────────────────────────
    private JButton buildChordChip(String chord, Color color, boolean addOnClick) {
        AppSettings s = AppSettings.getInstance();
        // ボタンの表示はディグリーモードならディグリー表記
        String display = s.getNoteMode() == AppSettings.NoteMode.DEGREE
            ? DegreeConverter.toDegree(chord, currentKey) : chord;

        JButton btn = new JButton(display) {
            boolean hovered = false;
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = hovered ? color : new Color(color.getRed(),color.getGreen(),color.getBlue(),160);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),6,6));
                g2.dispose();
                super.paintComponent(g);
            }
            { addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered=true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovered=false; repaint(); }
            }); }
        };
        btn.setFont(AppTheme.bodyFont(12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setContentAreaFilled(false); btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(4,10,4,10));
        if (addOnClick) btn.addActionListener(e -> addChord(chord));
        return btn;
    }

    private JButton buildSmallBtn(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean sel = getClientProperty("selected") == Boolean.TRUE;
                boolean en = isEnabled();
                Color bg = !en ? AppTheme.BG_CARD
                    : sel ? color : new Color(color.getRed(),color.getGreen(),color.getBlue(),120);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),5,5));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(AppTheme.bodyFont(11));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setContentAreaFilled(false); btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(3,8,3,8));
        return btn;
    }

    private JLabel buildSectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppTheme.bodyFont(12));
        l.setForeground(AppTheme.TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
}
