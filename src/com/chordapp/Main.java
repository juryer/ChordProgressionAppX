package com.chordapp;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // 起動時にデータを読み込む（リポジトリのコンストラクタで自動実行）
        ChordProgressionRepository.getInstance();
        SongRepository.getInstance();

        SwingUtilities.invokeLater(() -> {
            MainMenuWindow window = new MainMenuWindow();
            window.setVisible(true);
        });
    }
}
