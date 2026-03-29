このフォルダに以下のJARファイルを配置してください。

【必要なファイル】
  gson-2.10.1.jar

【ダウンロード先】
  https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar
  上記URLをブラウザで開くと直接ダウンロードできます。

【Eclipseへの追加手順】
  1. ダウンロードした gson-2.10.1.jar をこの lib フォルダに移動
  2. Eclipse でプロジェクトを右クリック
  3. Build Path → Configure Build Path
  4. Libraries タブ → Add JARs
  5. ChordProgressionApp/lib/gson-2.10.1.jar を選択
  6. OK で完了

【jpackage でexe化する際の注意】
  Runnable JAR でエクスポートする際に
  「Extract required libraries into generated JAR」を選択すれば
  Gson も自動的にJARに含まれます。
