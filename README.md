# ChordProgressionAppX

コード進行管理アプリの拡張版です。  
実際のユーザーからのフィードバックをもとに機能を大幅拡張しました。

> 安定版は [ChordProgressionApp](https://github.com/juryer/ChordProgressionApp) をご覧ください。

---

## 概要

音楽制作の現場では「このコード進行をメモしておきたい」という場面が多くあります。  
そのニーズを解決するために開発した自作ツールの拡張版です。  
実際に使ってもらったユーザーの意見をもとに、構成音表示・役割表示・オンコード対応など音楽理論的な機能を中心に拡張しました。

---

## 安定版との主な違い

| 機能 | 安定版 | 拡張版(X) |
|------|--------|-----------|
| 構成音の表示 | ✗ | ✅ |
| 役割表示（T/SD/D） | ✗ | ✅ |
| オンコード対応（C/E形式） | ✗ | ✅ |
| 歌詞入力 | ✗ | ✅ |
| PNG・HTML出力 | ✗ | ✅ |
| コード進行単体MIDI出力 | ✗ | ✅ |
| チップD&Dによるコード並び替え | ✗ | ✅ |
| 選択式エクスポート | ✗ | ✅ |
| 9・m9コード対応 | ✗ | ✅ |

---

## 機能一覧

- コード進行の登録・管理・検索・ソート
- 構成音の表示（例: C[C・E・G]）
- 役割表示（T=トニック / SD=サブドミナント / D=ドミナント）
- オンコード対応（C/E形式・ベース音をMIDIで出力）
- レーティング機能（★1〜5）・並び替え（▲▼・ドラッグ&ドロップ）
- コード進行単体のMIDI出力
- 楽曲エディタ（キー設定・自動移調・歌詞入力）
- MIDI出力（BPM・6種拍子・移調対応）
- PNG画像・HTMLファイル・JSONエクスポート
- JSONインポート（重複スキップ対応）
- 3カラーテーマ（ダークパープル / ダークグリーン / ライト）

---

## 動作環境・必要ライブラリ

| 項目 | 内容 |
|------|------|
| Java | 21以上 |
| OS | Windows 10 / 11 |
| ライブラリ | Gson 2.10.1（`lib/gson-2.10.1.jar` に同梱） |

---

## 開発環境・使用技術

| 項目 | 内容 |
|------|------|
| 言語 | Java 21 |
| フレームワーク | Swing / Gson / javax.sound.midi |
| IDE | Eclipse（Pleiades All in One） |
| ビルド | jpackage（exe化） |
| バージョン管理 | GitHub |

---

## 起動方法

### Eclipseで実行する場合

1. このリポジトリをクローンまたはダウンロード
2. Eclipseで「既存プロジェクトをワークスペースへ」でインポート
3. `lib/gson-2.10.1.jar` がビルドパスに含まれていることを確認
4. `src/com/chordapp/Main.java` を右クリック →「実行」→「Javaアプリケーション」

### ビルドパスの確認方法

```
プロジェクトを右クリック
→「プロパティ」
→「Javaのビルド・パス」
→「ライブラリー」タブ
→ gson-2.10.1.jar が含まれているか確認
　含まれていなければ「JARの追加」→ lib/gson-2.10.1.jar を選択
```

---

## データ保存場所

```
C:\Users\ユーザー名\AppData\Roaming\ChordProgressionApp\
　├── progressions.json　← コード進行データ
　└── songs.json　　　　← 楽曲データ
```

アドレスバーに `%APPDATA%\ChordProgressionApp` と入力するとすぐに開けます。

---

## クラス構成

| クラス名 | 役割 |
|---------|------|
| Main | エントリポイント |
| AppSettings | 設定シングルトン |
| AppTheme | カラーテーマ管理 |
| ChordProgression | コード進行モデル |
| ChordProgressionRepository | コード進行の永続化・CRUD・並び替え |
| ChordSelectorPanel | コード選択UI（ダイアトニック / タブ / 2段階・オンコード対応） |
| ChordTransposer | 移調・構成音・機能（T/SD/D）分析 |
| DegreeConverter | コードネーム → ディグリーネーム変換 |
| DataManager | JSON永続化・エクスポート・インポート |
| ExportUtil | PNG・HTML出力ユーティリティ |
| MainMenuWindow | メインメニュー |
| RecentWindow | 最近登録・編集したコード進行（上位5件） |
| RegisterWindow | コード進行登録画面 |
| ManageWindow | コード進行管理・編集・MIDI出力・エクスポート |
| ListWindow | コード進行一覧・検索・ソート |
| MidiExporter | MIDIファイル出力 |
| SettingsDialog | 設定ダイアログ |
| Song / SongSection / SongRepository | 楽曲モデル・永続化 |
| SongEditorWindow | 楽曲エディタ（キー設定・歌詞入力） |
| SongListWindow | 楽曲一覧・MIDI出力 |

---

## ポートフォリオ

https://juryer.github.io/my-web-page/

---
## スクリーンショット

<img src="https://github.com/user-attachments/assets/bbf28adb-2ea9-45cc-9e93-161f0936f5d1" width="50%">
<br>
<img src="https://github.com/user-attachments/assets/e783e129-dceb-41a4-be97-a097dcf9f0d0" width="50%">


