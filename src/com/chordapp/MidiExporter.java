package com.chordapp;

import javax.sound.midi.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * MIDI出力クラス
 * ・コードを小節単位で鳴らす
 * ・ベロシティ・メロディは考慮しない（固定値）
 * ・C / Cm キーの構成音テーブルを使用
 *   それ以外のキーはDAW側でトランスポーズしてもらう前提
 */
public class MidiExporter {

    // ── 定数 ────────────────────────────────────────────────────────────────
    private static final int CHANNEL     = 0;    // MIDIチャンネル0
    private static final int VELOCITY    = 80;   // 固定ベロシティ
    private static final int PROGRAM     = 0;    // Grand Piano
    private static final int RESOLUTION  = 480;  // Ticks per beat

    // ── コード構成音テーブル（C基準・MIDI番号） ───────────────────────────
    private static int[] getChordNotes(String chord) {
        // オンコード（C/E形式）を分離
        String baseNote = null;
        if (chord.contains("/")) {
            String[] parts = chord.split("/");
            chord = parts[0];
            baseNote = parts[1];
        }

        // ルート音とサフィックスを分離
        String root = chord.length() >= 2 &&
            (chord.charAt(1)=='#'||chord.charAt(1)=='b')
            ? chord.substring(0,2) : chord.substring(0,1);
        String suffix = chord.substring(root.length());

        int rootNote = rootToMidi(root);
        if (rootNote < 0) return new int[]{};

        // サフィックス別に構成音を組み立て
        if (suffix.equals("m7"))   return new int[]{rootNote, rootNote+3, rootNote+7, rootNote+10};
        if (suffix.equals("m7b5")) return addBass(new int[]{rootNote, rootNote+3, rootNote+6, rootNote+10}, baseNote);
        if (suffix.equals("M7") || suffix.equals("maj7"))
                                   return addBass(new int[]{rootNote, rootNote+4, rootNote+7, rootNote+11}, baseNote);
        if (suffix.equals("7"))    return addBass(new int[]{rootNote, rootNote+4, rootNote+7, rootNote+10}, baseNote);
        if (suffix.equals("m"))    return addBass(new int[]{rootNote, rootNote+3, rootNote+7}, baseNote);
        if (suffix.equals("dim7")) return addBass(new int[]{rootNote, rootNote+3, rootNote+6, rootNote+9}, baseNote);
        if (suffix.equals("dim"))  return addBass(new int[]{rootNote, rootNote+3, rootNote+6}, baseNote);
        if (suffix.equals("aug"))  return addBass(new int[]{rootNote, rootNote+4, rootNote+8}, baseNote);
        if (suffix.equals("sus4")) return addBass(new int[]{rootNote, rootNote+5, rootNote+7}, baseNote);
        if (suffix.equals("sus2")) return addBass(new int[]{rootNote, rootNote+2, rootNote+7}, baseNote);
        if (suffix.equals("m9"))   return addBass(new int[]{rootNote, rootNote+3, rootNote+7, rootNote+10, rootNote+14}, baseNote);
        if (suffix.equals("9"))    return addBass(new int[]{rootNote, rootNote+4, rootNote+7, rootNote+10, rootNote+14}, baseNote);
        // デフォルト: メジャー3和音
        return addBass(new int[]{rootNote, rootNote+4, rootNote+7}, baseNote);
    }

    /** オンコードのベース音を追加する */
    private static int[] addBass(int[] notes, String bassNote) {
        if (bassNote == null) return notes;
        int bassIdx = rootToMidi(bassNote);
        if (bassIdx < 0) return notes;
        // ベース音は1オクターブ下げて先頭に追加
        int bassLow = bassIdx - 12;
        int[] result = new int[notes.length + 1];
        result[0] = bassLow;
        System.arraycopy(notes, 0, result, 1, notes.length);
        return result;
    }

    /** 音名 → MIDIノート番号（C4=60 基準） */
    private static int rootToMidi(String root) {
        switch (root) {
            case "C":  return 60;
            case "C#": case "Db": return 61;
            case "D":  return 62;
            case "D#": case "Eb": return 63;
            case "E":  return 64;
            case "F":  return 65;
            case "F#": case "Gb": return 66;
            case "G":  return 67;
            case "G#": case "Ab": return 68;
            case "A":  return 69;
            case "A#": case "Bb": return 70;
            case "B":  return 71;
            default:   return -1;
        }
    }

    // ── MIDI出力メイン ───────────────────────────────────────────────────────
    /**
     * @param song           出力対象の楽曲
     * @param bpm            テンポ
     * @param outputFile     出力先ファイル
     * @param rootOctaveDown ルート音を1オクターブ下げるか
     * @param beatsPerBar    1小節の拍数（4=4/4, 3=3/4, 6=6/8）
     */
    public static void export(Song song, int bpm, File outputFile,
                              boolean rootOctaveDown, int beatsPerBar) throws Exception {
        Sequence seq = new Sequence(Sequence.PPQ, RESOLUTION);
        Track track = seq.createTrack();

        // ── テンポ設定 ────────────────────────────────────────────────────
        int microsPerBeat = 60_000_000 / bpm;
        MetaMessage tempoMsg = new MetaMessage();
        byte[] tempoData = {
            (byte)(microsPerBeat >> 16),
            (byte)(microsPerBeat >> 8),
            (byte)(microsPerBeat)
        };
        tempoMsg.setMessage(0x51, tempoData, 3);
        track.add(new MidiEvent(tempoMsg, 0));

        // ── 拍子記号設定 ──────────────────────────────────────────────────
        // 6/8・7/8・8/8 は8分音符基準、それ以外は4分音符基準
        int denominator = (beatsPerBar == 6 || beatsPerBar == 7 || beatsPerBar == 8) ? 8 : 4;
        int denominatorPow = (denominator == 8) ? 3 : 2; // 2^n の n
        MetaMessage timeSigMsg = new MetaMessage();
        byte[] timeSigData = {
            (byte) beatsPerBar,   // 分子
            (byte) denominatorPow, // 分母（2^n）
            (byte) 24,            // MIDIクロック数/4分音符
            (byte) 8              // 32分音符数/4分音符
        };
        timeSigMsg.setMessage(0x58, timeSigData, 4);
        track.add(new MidiEvent(timeSigMsg, 0));

        // ── トラック名 ────────────────────────────────────────────────────
        MetaMessage trackName = new MetaMessage();
        byte[] nameBytes = song.getTitle().getBytes("UTF-8");
        trackName.setMessage(0x03, nameBytes, nameBytes.length);
        track.add(new MidiEvent(trackName, 0));

        // ── プログラムチェンジ（Grand Piano） ─────────────────────────────
        ShortMessage pc = new ShortMessage();
        pc.setMessage(ShortMessage.PROGRAM_CHANGE, CHANNEL, PROGRAM, 0);
        track.add(new MidiEvent(pc, 0));

        // 拍子ごとの1小節tick数を計算
        // 6/8: 8分音符×6 = RESOLUTION/2×6
        // 7/8: 8分音符×7 = RESOLUTION/2×7
        // それ以外: 4分音符×拍数
        long ticksPerBar;
        if (beatsPerBar == 6 || beatsPerBar == 7 || beatsPerBar == 8) {
            ticksPerBar = (long)(RESOLUTION / 2) * beatsPerBar;
        } else {
            ticksPerBar = (long) RESOLUTION * beatsPerBar;
        }
        long tick = 0;
        String songKey = song.getKey() != null ? song.getKey() : "C";

        for (SongSection section : song.getSections()) {
            List<String> chords = section.getChords();
            if (chords.isEmpty()) continue;

            // C基準 → 楽曲キーにトランスポーズ
            List<String> transposedChords = ChordTransposer.transposeList(chords, "C", songKey);

            int repeat = Math.max(1, section.getRepeatCount());

            for (int r = 0; r < repeat; r++) {
                for (String chord : transposedChords) {
                    int[] notes = getChordNotes(chord);
                    if (notes.length == 0) { tick += ticksPerBar; continue; }

                    // ルート音を1オクターブ下げる処理
                    if (rootOctaveDown && notes.length > 0) {
                        int rootNote = notes[0] - 12;
                        if (rootNote >= 0) notes[0] = rootNote;
                    }

                    // Note On
                    for (int note : notes) {
                        ShortMessage on = new ShortMessage();
                        on.setMessage(ShortMessage.NOTE_ON, CHANNEL, note, VELOCITY);
                        track.add(new MidiEvent(on, tick));
                    }
                    // Note Off（1小節後）
                    for (int note : notes) {
                        ShortMessage off = new ShortMessage();
                        off.setMessage(ShortMessage.NOTE_OFF, CHANNEL, note, 0);
                        track.add(new MidiEvent(off, tick + ticksPerBar - 10));
                    }
                    tick += ticksPerBar;
                }
            }
        }

        // ── End of Track ─────────────────────────────────────────────────
        MetaMessage eot = new MetaMessage();
        eot.setMessage(0x2F, new byte[]{}, 0);
        track.add(new MidiEvent(eot, tick));

        // ── ファイル書き出し ──────────────────────────────────────────────
        MidiSystem.write(seq, 1, outputFile);
    }

    /** 後方互換用（rootOctaveDown=false, 4/4拍子） */
    public static void export(Song song, int bpm, File outputFile) throws Exception {
        export(song, bpm, outputFile, false, 4);
    }

    /** 後方互換用（拍子指定なし） */
    public static void export(Song song, int bpm, File outputFile, boolean rootOctaveDown) throws Exception {
        export(song, bpm, outputFile, rootOctaveDown, 4);
    }
}
