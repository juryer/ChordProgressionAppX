package com.chordapp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ChordProgressionRepository {

    private static ChordProgressionRepository instance;
    private List<ChordProgression> progressions = new ArrayList<>();

    private ChordProgressionRepository() {
        // JSONから読込。ファイルがなければサンプルデータを使用
        List<ChordProgression> saved = DataManager.loadProgressions();
        if (saved == null) {
            loadSampleData();
        } else {
            progressions = saved;
        }
    }

    public static ChordProgressionRepository getInstance() {
        if (instance == null) instance = new ChordProgressionRepository();
        return instance;
    }

    private void loadSampleData() {
        progressions.add(new ChordProgression("王道ポップ進行", "C", "120",
            List.of("C","Am","F","G"), "定番のI-VI-IV-V進行"));
        progressions.add(new ChordProgression("小室進行", "C", "130",
            List.of("Am","F","G","C"), "Ⅵm-Ⅳ-Ⅴ-Ⅰ、ポップスで多用される進行"));
        progressions.add(new ChordProgression("カノン進行", "C", "90",
            List.of("C","G","Am","F"), "パッヘルベルのカノンをベースにした進行"));
    }

    /** 全データをJSONに保存する */
    public void save() {
        DataManager.saveProgressions(progressions);
    }

    public List<ChordProgression> getAll() { return new ArrayList<>(progressions); }

    public List<ChordProgression> getRecentlyUsed(int limit) {
        return progressions.stream()
            .sorted(Comparator.comparing(ChordProgression::getLastUsed).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }

    public void add(ChordProgression progression) {
        progressions.add(progression);
        save();
    }

    public void delete(String id) {
        progressions.removeIf(p -> p.getId().equals(id));
        save();
    }

    public void update(ChordProgression updated) {
        for (int i = 0; i < progressions.size(); i++) {
            if (progressions.get(i).getId().equals(updated.getId())) {
                progressions.set(i, updated);
                save();
                return;
            }
        }
    }

    public ChordProgression findById(String id) {
        return progressions.stream()
            .filter(p -> p.getId().equals(id))
            .findFirst().orElse(null);
    }

    /** 指定IDのコード進行を1つ上に移動 */
    public void moveUp(String id) {
        int idx = indexOf(id);
        if (idx > 0) {
            ChordProgression cp = progressions.remove(idx);
            progressions.add(idx - 1, cp);
            save();
        }
    }

    /** 指定IDのコード進行を1つ下に移動 */
    public void moveDown(String id) {
        int idx = indexOf(id);
        if (idx >= 0 && idx < progressions.size() - 1) {
            ChordProgression cp = progressions.remove(idx);
            progressions.add(idx + 1, cp);
            save();
        }
    }

    private int indexOf(String id) {
        for (int i = 0; i < progressions.size(); i++) {
            if (progressions.get(i).getId().equals(id)) return i;
        }
        return -1;
    }
}
