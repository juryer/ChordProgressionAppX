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
        // ポップス・J-POP系
        progressions.add(new ChordProgression("王道ポップ進行 (4356)", "C", "120",
            List.of("F","Am","G","C"), "IV-VI-V-I、定番のポップス進行"));
        progressions.add(new ChordProgression("小室進行 (6451)", "C", "130",
            List.of("Am","F","C","G"), "VI-IV-I-V、ポップスで多用される進行"));
        progressions.add(new ChordProgression("Jポップ進行 (4361)", "C", "120",
            List.of("F","Am","Em","C"), "IV-VI-III-I、J-POPの定番進行"));
        progressions.add(new ChordProgression("カノン進行", "C", "90",
            List.of("C","G","Am","Em","F","C","F","G"), "パッヘルベルのカノンをベースにした進行"));
        progressions.add(new ChordProgression("Let It Be進行 (1564)", "C", "80",
            List.of("C","G","Am","F"), "I-V-VI-IV、洋楽ポップスの定番"));
        progressions.add(new ChordProgression("枯葉進行 (1625)", "C", "100",
            List.of("C","Am","Dm","G"), "I-VI-II-V、スタンダードに多い進行"));

        // ジャズ・スタンダード系
        progressions.add(new ChordProgression("ツーファイブワン (251)", "C", "120",
            List.of("Dm","G","C"), "II-V-I、ジャズの基本進行"));
        progressions.add(new ChordProgression("循環コード (36251)", "C", "120",
            List.of("Em","Am","Dm","G","C"), "III-VI-II-V-I、ジャズの循環進行"));
        progressions.add(new ChordProgression("上昇進行 (1234)", "C", "110",
            List.of("C","Dm","Em","F"), "I-II-III-IV、上昇感のある進行"));

        // ブルース系
        progressions.add(new ChordProgression("12小節ブルース", "A", "100",
            List.of("A","A","A","A","D","D","A","A","E","D","A","E"), "ロック・ブルースの定番12小節"));
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
