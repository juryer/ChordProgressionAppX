package com.chordapp;

import java.util.ArrayList;
import java.util.List;

/**
 * 1曲のセクション（verse/chorus等）を表すモデル
 */
public class SongSection {
    private String id;
    private String sectionName;
    private String progressionId;
    private List<String> chords;
    private int repeatCount;
    private String lyrics; // 歌詞
    private String sectionKey; // null = 楽曲キーを引き継ぐ、非null = このセクションで転調

    public SongSection(String sectionName) {
        this.id = java.util.UUID.randomUUID().toString();
        this.sectionName = sectionName;
        this.chords = new ArrayList<>();
        this.repeatCount = 1;
        this.lyrics = "";
        this.sectionKey = null;
    }

    public String getId() { return id; }
    public String getSectionName() { return sectionName; }
    public void setSectionName(String name) { this.sectionName = name; }
    public String getProgressionId() { return progressionId; }
    public void setProgressionId(String pid) { this.progressionId = pid; }
    public List<String> getChords() { return chords; }
    public void setChords(List<String> chords) { this.chords = new ArrayList<>(chords); }
    public int getRepeatCount() { return repeatCount; }
    public void setRepeatCount(int r) { this.repeatCount = r; }
    public String getLyrics() { return lyrics != null ? lyrics : ""; }
    public void setLyrics(String lyrics) { this.lyrics = lyrics != null ? lyrics : ""; }
    /** セクション固有のキー。nullなら楽曲のキーを引き継ぐ */
    public String getSectionKey() { return sectionKey; }
    public void setSectionKey(String key) { this.sectionKey = (key == null || key.isEmpty()) ? null : key; }
    /** 実際に使うキーを返す（sectionKeyがあればそれ、なければ楽曲キー） */
    public String getEffectiveKey(String songKey) {
        return (sectionKey != null && !sectionKey.isEmpty()) ? sectionKey : songKey;
    }

    public String getChordsAsString() {
        return String.join("  ", chords);
    }
}
