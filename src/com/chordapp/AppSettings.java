package com.chordapp;

import java.util.ArrayList;
import java.util.List;

public class AppSettings {
    private static AppSettings instance;

    public static final String VERSION = "v4.0";

    // ウィンドウサイズ
    public enum WindowSize {
        SMALL("小 (1024×576)", 1024, 576),
        MEDIUM("中 (1280×720)", 1280, 720),
        LARGE("大 (1440×900)", 1440, 900),
        XLARGE("特大 (1920×1080)", 1920, 1080);
        public final String label;
        public final int width;
        public final int height;
        WindowSize(String label, int w, int h) { this.label = label; this.width = w; this.height = h; }
        @Override public String toString() { return label; }
    }

    // 表示モード
    public enum NoteMode { CHORD_NAME, DEGREE }

    // カラーテーマ
    public enum ColorTheme {
        DARK_PURPLE("ダークパープル（デフォルト）"),
        DARK_GREEN("ダークグリーン"),
        LIGHT("ライトモード");
        public final String label;
        ColorTheme(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    private WindowSize windowSize = WindowSize.MEDIUM;
    private NoteMode noteMode = NoteMode.CHORD_NAME;
    private ColorTheme colorTheme = ColorTheme.DARK_PURPLE;
    private List<String> favoriteBpms = new ArrayList<>();

    private AppSettings() {
        favoriteBpms.add("60");
        favoriteBpms.add("80");
        favoriteBpms.add("100");
        favoriteBpms.add("120");
        favoriteBpms.add("140");
        favoriteBpms.add("160");
    }

    public static AppSettings getInstance() {
        if (instance == null) instance = new AppSettings();
        return instance;
    }

    public WindowSize getWindowSize() { return windowSize; }
    public void setWindowSize(WindowSize ws) { this.windowSize = ws; }

    public NoteMode getNoteMode() { return noteMode; }
    public void setNoteMode(NoteMode m) { this.noteMode = m; }

    public ColorTheme getColorTheme() { return colorTheme; }
    public void setColorTheme(ColorTheme t) {
        this.colorTheme = t;
        AppTheme.applyTheme(t);
    }

    public List<String> getFavoriteBpms() { return favoriteBpms; }
    public void setFavoriteBpms(List<String> list) { this.favoriteBpms = new ArrayList<>(list); }
    public void addFavoriteBpm(String bpm) { if (!favoriteBpms.contains(bpm)) favoriteBpms.add(bpm); }
    public void removeFavoriteBpm(String bpm) { favoriteBpms.remove(bpm); }
}
