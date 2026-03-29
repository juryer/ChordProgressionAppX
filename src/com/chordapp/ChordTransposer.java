package com.chordapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * コードのトランスポーズ（移調）ユーティリティ
 * C基準のコード進行を任意のキーに移調する
 */
public class ChordTransposer {

    // 音名 → 半音インデックス (C=0)
    private static final Map<String, Integer> NOTE_INDEX = new LinkedHashMap<>();
    // インデックス → 音名（シャープ表記）
    private static final String[] INDEX_TO_NOTE = {
        "C","C#","D","D#","E","F","F#","G","G#","A","A#","B"
    };

    static {
        NOTE_INDEX.put("C",0);  NOTE_INDEX.put("C#",1); NOTE_INDEX.put("Db",1);
        NOTE_INDEX.put("D",2);  NOTE_INDEX.put("D#",3); NOTE_INDEX.put("Eb",3);
        NOTE_INDEX.put("E",4);  NOTE_INDEX.put("F",5);
        NOTE_INDEX.put("F#",6); NOTE_INDEX.put("Gb",6);
        NOTE_INDEX.put("G",7);  NOTE_INDEX.put("G#",8); NOTE_INDEX.put("Ab",8);
        NOTE_INDEX.put("A",9);  NOTE_INDEX.put("A#",10);NOTE_INDEX.put("Bb",10);
        NOTE_INDEX.put("B",11);
    }

    /**
     * コード1つを移調する
     * @param chord  元のコード名（例: "Am7"）
     * @param fromKey 元のキー（例: "C"）
     * @param toKey   移調先のキー（例: "D"）
     * @return 移調後のコード名（例: "Bm7"）
     */
    public static String transpose(String chord, String fromKey, String toKey) {
        if (chord == null || chord.equals("─") || chord.isEmpty()) return chord;

        // キーのルート音を取得
        String fromRoot = extractKeyRoot(fromKey);
        String toRoot   = extractKeyRoot(toKey);

        Integer fromIdx = NOTE_INDEX.get(fromRoot);
        Integer toIdx   = NOTE_INDEX.get(toRoot);
        if (fromIdx == null || toIdx == null) return chord;

        // 移調幅（半音数）
        int shift = (toIdx - fromIdx + 12) % 12;
        if (shift == 0) return chord;

        // コードのルート音とサフィックスを分離
        String root   = extractRoot(chord);
        String suffix = chord.substring(root.length());

        Integer rootIdx = NOTE_INDEX.get(root);
        if (rootIdx == null) return chord;

        // 移調後のルート音
        int newRootIdx = (rootIdx + shift) % 12;
        String newRoot = INDEX_TO_NOTE[newRootIdx];

        return newRoot + suffix;
    }

    /**
     * コードリストをまとめて移調する
     */
    public static List<String> transposeList(List<String> chords, String fromKey, String toKey) {
        List<String> result = new ArrayList<>();
        for (String c : chords) result.add(transpose(c, fromKey, toKey));
        return result;
    }

    /**
     * コードの機能（トニック・サブドミナント・ドミナント）を返す
     * @param chord コード名
     * @param key   キー
     * @return "T"=トニック / "SD"=サブドミナント / "D"=ドミナント / ""=不明
     */
    public static String getChordFunction(String chord, String key) {
        if (chord == null || chord.isEmpty()) return "";

        boolean keyIsMinor = key.length() > 1 && key.endsWith("m");
        String keyRoot = keyIsMinor ? key.substring(0, key.length()-1) : key;
        Integer keyIdx = NOTE_INDEX.get(keyRoot);
        if (keyIdx == null) return "";

        String chordRoot = extractRoot(chord);
        Integer chordIdx = NOTE_INDEX.get(chordRoot);
        if (chordIdx == null) return "";

        int interval = (chordIdx - keyIdx + 12) % 12;
        String suffix = chord.substring(chordRoot.length());
        boolean isMinor = suffix.startsWith("m") && !suffix.startsWith("maj") && !suffix.startsWith("M");
        boolean isDim = suffix.equals("dim") || suffix.equals("dim7");

        if (!keyIsMinor) {
            // メジャーキー
            // T: Ⅰ Ⅲm Ⅵm
            if ((interval == 0 && !isMinor) || (interval == 4 && isMinor) || (interval == 9 && isMinor))
                return "T";
            // SD: Ⅱm Ⅳ
            if ((interval == 2 && isMinor) || (interval == 5 && !isMinor && !isDim))
                return "SD";
            // D: Ⅴ Ⅶdim
            if ((interval == 7 && !isMinor) || (interval == 11 && isDim))
                return "D";
        } else {
            // マイナーキー
            // T: Ⅰm ♭Ⅲ ♭Ⅵ
            if ((interval == 0 && isMinor) || interval == 3 || interval == 8)
                return "T";
            // SD: Ⅳm ♭Ⅱ
            if ((interval == 5 && isMinor) || interval == 2)
                return "SD";
            // D: Ⅴ Ⅶdim
            if ((interval == 7) || (interval == 11 && isDim))
                return "D";
        }
        return "";
    }

    /**
     * コード名から構成音を音名で返す
     * 例: Am7 → A・C・E・G
     */
    public static String getChordTones(String chord) {
        if (chord == null || chord.isEmpty()) return "";

        String root = extractRoot(chord);
        String suffix = chord.substring(root.length());

        Integer rootIdx = NOTE_INDEX.get(root);
        if (rootIdx == null) return "";

        int[] intervals;
        switch (suffix) {
            case "m":     intervals = new int[]{0, 3, 7}; break;
            case "7":     intervals = new int[]{0, 4, 7, 10}; break;
            case "M7": case "maj7": intervals = new int[]{0, 4, 7, 11}; break;
            case "m7":    intervals = new int[]{0, 3, 7, 10}; break;
            case "m7b5":  intervals = new int[]{0, 3, 6, 10}; break;
            case "dim":   intervals = new int[]{0, 3, 6}; break;
            case "dim7":  intervals = new int[]{0, 3, 6, 9}; break;
            case "aug":   intervals = new int[]{0, 4, 8}; break;
            case "sus4":  intervals = new int[]{0, 5, 7}; break;
            case "add9":  intervals = new int[]{0, 4, 7, 14}; break;
            case "9":     intervals = new int[]{0, 4, 7, 10, 14}; break;
            case "m9":    intervals = new int[]{0, 3, 7, 10, 14}; break;
            default:      intervals = new int[]{0, 4, 7}; break;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < intervals.length; i++) {
            int noteIdx = (rootIdx + intervals[i]) % 12;
            if (i > 0) sb.append("・");
            sb.append(INDEX_TO_NOTE[noteIdx]);
        }
        return sb.toString();
    }
    public static String extractKeyRoot(String key) {
        if (key == null) return "C";
        if (key.length() > 1 && key.endsWith("m") &&
            !key.equals("Dm") && key.length() == 2) {
            // 1文字+m のマイナーキー
        }
        // マイナーキー判定（末尾がm かつ 長さ2以上）
        boolean isMinor = key.length() > 1 && key.endsWith("m");
        return isMinor ? key.substring(0, key.length()-1) : key;
    }

    /**
     * コードのルート音を抽出（"C#m7" → "C#"）
     */
    private static String extractRoot(String chord) {
        if (chord.length() >= 2 &&
            (chord.charAt(1) == '#' || chord.charAt(1) == 'b')) {
            return chord.substring(0, 2);
        }
        return chord.substring(0, 1);
    }

    /**
     * 利用可能なキー一覧（移調先として表示する）
     */
    public static final String[] ALL_KEYS = {
        "C","C#","D","D#","E","F","F#","G","G#","A","A#","B",
        "Cm","C#m","Dm","D#m","Em","Fm","F#m","Gm","G#m","Am","A#m","Bm"
    };
}
