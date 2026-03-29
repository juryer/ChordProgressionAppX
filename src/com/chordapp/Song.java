package com.chordapp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 1曲分のデータモデル（SongSectionのリストを保持）
 */
public class Song {
    private String id;
    private String title;
    private String key; // 楽曲のキー（例: "C", "Am"）
    private List<SongSection> sections;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Song(String title) {
        this.id = java.util.UUID.randomUUID().toString();
        this.title = title;
        this.key = "C"; // デフォルトはC
        this.sections = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String t) { this.title = t; }
    public String getKey() { return key; }
    public void setKey(String k) { this.key = k; }
    public List<SongSection> getSections() { return sections; }
    public void setSections(List<SongSection> s) { this.sections = new ArrayList<>(s); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t) { this.updatedAt = t; }

    public int getSectionCount() { return sections.size(); }

    public String getFormattedUpdatedAt() {
        return updatedAt.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
    }

    /** セクション名の概要（最大3件）を返す */
    public String getSectionSummary() {
        if (sections.isEmpty()) return "（セクションなし）";
        List<String> names = new ArrayList<>();
        for (int i = 0; i < Math.min(3, sections.size()); i++) {
            names.add("#" + sections.get(i).getSectionName());
        }
        String s = String.join("  ", names);
        if (sections.size() > 3) s += "  …";
        return s;
    }
}
