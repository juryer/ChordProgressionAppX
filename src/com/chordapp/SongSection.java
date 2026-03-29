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

    public SongSection(String sectionName) {
        this.id = java.util.UUID.randomUUID().toString();
        this.sectionName = sectionName;
        this.chords = new ArrayList<>();
        this.repeatCount = 1;
        this.lyrics = "";
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

    public String getChordsAsString() {
        return String.join("  ", chords);
    }
}
