package com.chordapp;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * コードネーム → ディグリーネーム 変換
 *
 * 表示ルール:
 *   メジャー : Ⅰ  Ⅱ  Ⅲ ... （大文字ローマ数字）
 *   マイナー : Ⅰm Ⅱm Ⅲm ... （大文字 + m）
 *   シャープ : Ⅰ# Ⅱ# ... / Ⅰ#m Ⅱ#m ...
 *   フラット  : ♭Ⅱ ♭Ⅲ ...
 *   付加音  : Ⅱm7 ⅣM7 Ⅴsus4 など
 */
public class DegreeConverter {

    // 大文字ローマ数字（Unicode 絵文字ではなく合字）
    private static final String[] ROMAN = {"Ⅰ","Ⅱ","Ⅲ","Ⅳ","Ⅴ","Ⅵ","Ⅶ"};

    // 音名 → 半音インデックス (C=0)
    private static final Map<String, Integer> NOTE_INDEX = new LinkedHashMap<>();
    static {
        NOTE_INDEX.put("C",0);  NOTE_INDEX.put("C#",1); NOTE_INDEX.put("Db",1);
        NOTE_INDEX.put("D",2);  NOTE_INDEX.put("D#",3); NOTE_INDEX.put("Eb",3);
        NOTE_INDEX.put("E",4);  NOTE_INDEX.put("F",5);
        NOTE_INDEX.put("F#",6); NOTE_INDEX.put("Gb",6);
        NOTE_INDEX.put("G",7);  NOTE_INDEX.put("G#",8); NOTE_INDEX.put("Ab",8);
        NOTE_INDEX.put("A",9);  NOTE_INDEX.put("A#",10);NOTE_INDEX.put("Bb",10);
        NOTE_INDEX.put("B",11);
    }

    // メジャースケールの音程（半音数）
    private static final int[] MAJOR_INTERVALS = {0, 2, 4, 5, 7, 9, 11};
    // ナチュラルマイナースケールの音程（半音数）
    private static final int[] MINOR_INTERVALS = {0, 2, 3, 5, 7, 8, 10};

    /**
     * コードネームをディグリー表記に変換する
     *   例: toDegree("Am", "Am") → "Ⅰm"
     *       toDegree("F",  "Am") → "Ⅵ"
     *       toDegree("G",  "Am") → "Ⅶ"
     *       toDegree("C",  "Am") → "Ⅲ"
     *       toDegree("F",  "C")  → "Ⅳ"
     */
    public static String toDegree(String chord, String key) {
        if (chord == null || chord.equals("─")) return chord;

        // キーのルート音とメジャー/マイナーを判定
        boolean keyIsMinor = key.length() > 1 && key.endsWith("m");
        String keyRoot = keyIsMinor ? key.substring(0, key.length() - 1) : key;
        Integer keyIdx = NOTE_INDEX.get(keyRoot);
        if (keyIdx == null) return chord;

        // キーに応じたスケール音程を選択
        int[] scaleIntervals = keyIsMinor ? MINOR_INTERVALS : MAJOR_INTERVALS;

        // コードのルート音とサフィックスを分離
        String root    = extractRoot(chord);
        String suffix  = chord.substring(root.length());
        Integer chordIdx = NOTE_INDEX.get(root);
        if (chordIdx == null) return chord;

        // キーからの半音差 (0-11)
        int interval = (chordIdx - keyIdx + 12) % 12;

        // ── スケール上のディグリーを検索 ──────────────────────
        int degree   = -1;
        boolean sharp = false;
        boolean flat  = false;

        for (int i = 0; i < scaleIntervals.length; i++) {
            if (scaleIntervals[i] == interval) { degree = i; break; }
        }
        if (degree < 0) {
            // スケール外の音 → # か ♭ で近似表示
            for (int i = 0; i < scaleIntervals.length; i++) {
                if ((scaleIntervals[i] - 1 + 12) % 12 == interval) {
                    degree = i; flat = true; break;
                }
                if ((scaleIntervals[i] + 1) % 12 == interval) {
                    degree = i; sharp = true; break;
                }
            }
        }
        if (degree < 0) return chord;

        // ── サフィックス解析 ──────────────────────────────────
        // マイナー判定: suffix が "m" で始まり "maj"/"M" ではない
        boolean chordMinor = suffix.startsWith("m")
                          && !suffix.startsWith("maj")
                          && !suffix.startsWith("M");

        // 付加音部分（m を除いた残り）
        String addon = suffix;
        if (chordMinor) addon = addon.substring(1); // 先頭 "m" を除去
        // M7 → maj7 に統一
        addon = addon.replace("M7", "maj7");

        // ── 組み立て ──────────────────────────────────────────
        // フォーマット: [♭]Ⅰ[#][m][addon]
        StringBuilder sb = new StringBuilder();
        if (flat)  sb.append("♭");
        sb.append(ROMAN[degree]);
        if (sharp) sb.append("#");
        if (chordMinor) sb.append("m");
        sb.append(addon);

        return sb.toString();
    }

    // ルート音を抽出（C# / Db など2文字 or 1文字）
    private static String extractRoot(String chord) {
        if (chord.length() >= 2 &&
            (chord.charAt(1) == '#' || chord.charAt(1) == 'b')) {
            return chord.substring(0, 2);
        }
        return chord.substring(0, 1);
    }

    /** コードリストをまとめて変換 */
    public static java.util.List<String> convertList(
            java.util.List<String> chords, String key) {
        java.util.List<String> result = new java.util.ArrayList<>();
        for (String c : chords) result.add(toDegree(c, key));
        return result;
    }

    /** 現在の設定に従って1コードを表示用に変換 */
    public static String display(String chord, String key) {
        AppSettings s = AppSettings.getInstance();
        if (s.getNoteMode() == AppSettings.NoteMode.DEGREE) return toDegree(chord, key);
        return chord;
    }

    /** 現在の設定に従ってリストを "→" 区切り文字列で返す */
    public static String displayList(java.util.List<String> chords, String key) {
        AppSettings s = AppSettings.getInstance();
        if (s.getNoteMode() == AppSettings.NoteMode.DEGREE) {
            return String.join(" → ", convertList(chords, key));
        }
        return String.join(" → ", chords);
    }
}
