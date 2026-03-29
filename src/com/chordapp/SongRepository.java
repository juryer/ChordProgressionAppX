package com.chordapp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SongRepository {
    private static SongRepository instance;
    private List<Song> songs = new ArrayList<>();

    private SongRepository() {
        songs = DataManager.loadSongs();
    }

    public static SongRepository getInstance() {
        if (instance == null) instance = new SongRepository();
        return instance;
    }

    /** 全データをJSONに保存する */
    public void save() {
        DataManager.saveSongs(songs);
    }

    public List<Song> getAll() { return new ArrayList<>(songs); }

    public List<Song> getRecentlyUpdated() {
        return songs.stream()
            .sorted(Comparator.comparing(Song::getUpdatedAt).reversed())
            .collect(Collectors.toList());
    }

    public void add(Song song) {
        songs.add(song);
        save();
    }

    public void delete(String id) {
        songs.removeIf(s -> s.getId().equals(id));
        save();
    }

    public void update(Song updated) {
        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).getId().equals(updated.getId())) {
                songs.set(i, updated);
                save();
                return;
            }
        }
    }

    public Song findById(String id) {
        return songs.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }
}
