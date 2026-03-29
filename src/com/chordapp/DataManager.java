package com.chordapp;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * JSONファイルへのデータ保存・読込を担当するクラス
 * 保存先: %APPDATA%\ChordProgressionApp\
 */
public class DataManager {

    private static final String APP_DIR_NAME = "ChordProgressionApp";
    private static final String PROGRESSIONS_FILE = "progressions.json";
    private static final String SONGS_FILE = "songs.json";

    private static final DateTimeFormatter DT_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // LocalDateTime のカスタムシリアライザ（Gsonはデフォルトで非対応）
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(LocalDateTime.class,
            (JsonSerializer<LocalDateTime>) (src, t, ctx) ->
                new JsonPrimitive(src.format(DT_FMT)))
        .registerTypeAdapter(LocalDateTime.class,
            (JsonDeserializer<LocalDateTime>) (json, t, ctx) ->
                LocalDateTime.parse(json.getAsString(), DT_FMT))
        .create();

    // ── 保存先ディレクトリ ────────────────────────────────────────────────
    private static Path getAppDir() {
        String appData = System.getenv("APPDATA");
        if (appData == null) appData = System.getProperty("user.home");
        return Paths.get(appData, APP_DIR_NAME);
    }

    private static Path getProgressionsPath() { return getAppDir().resolve(PROGRESSIONS_FILE); }
    private static Path getSongsPath()        { return getAppDir().resolve(SONGS_FILE); }

    private static void ensureAppDir() throws IOException {
        Files.createDirectories(getAppDir());
    }

    // ── コード進行 保存・読込 ─────────────────────────────────────────────
    public static void saveProgressions(List<ChordProgression> list) {
        try {
            ensureAppDir();
            String json = GSON.toJson(list);
            Files.writeString(getProgressionsPath(), json);
        } catch (IOException e) {
            System.err.println("[DataManager] コード進行の保存に失敗: " + e.getMessage());
        }
    }

    public static List<ChordProgression> loadProgressions() {
        Path path = getProgressionsPath();
        if (!Files.exists(path)) return null; // ファイルがない = 初回起動
        try {
            String json = Files.readString(path);
            Type type = new TypeToken<List<ChordProgression>>(){}.getType();
            List<ChordProgression> list = GSON.fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("[DataManager] コード進行の読込に失敗: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ── 楽曲データ 保存・読込 ─────────────────────────────────────────────
    public static void saveSongs(List<Song> list) {
        try {
            ensureAppDir();
            String json = GSON.toJson(list);
            Files.writeString(getSongsPath(), json);
        } catch (IOException e) {
            System.err.println("[DataManager] 楽曲の保存に失敗: " + e.getMessage());
        }
    }

    public static List<Song> loadSongs() {
        Path path = getSongsPath();
        if (!Files.exists(path)) return new ArrayList<>();
        try {
            String json = Files.readString(path);
            Type type = new TypeToken<List<Song>>(){}.getType();
            List<Song> list = GSON.fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("[DataManager] 楽曲の読込に失敗: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** 保存先フォルダのパスを返す（設定画面等で表示用） */
    public static String getAppDirPath() {
        return getAppDir().toString();
    }

    /** コード進行を任意のJSONファイルにエクスポート */
    public static void exportProgressions(List<ChordProgression> progressions, java.io.File file) throws IOException {
        String json = GSON.toJson(progressions);
        Files.writeString(file.toPath(), json);
    }

    /** 任意のJSONファイルからコード進行をインポート */
    public static List<ChordProgression> importProgressions(java.io.File file) throws IOException {
        String json = Files.readString(file.toPath());
        Type type = new TypeToken<List<ChordProgression>>(){}.getType();
        List<ChordProgression> list = GSON.fromJson(json, type);
        return list != null ? list : new ArrayList<>();
    }
}
