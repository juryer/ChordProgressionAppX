package com.chordapp;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * コード進行をPNG画像またはPDFライクなHTMLとして出力するユーティリティ
 * 外部ライブラリ不要・Java標準機能のみ使用
 */
public class ExportUtil {

    private static final int WIDTH = 900;
    private static final int PADDING = 40;
    private static final int LINE_HEIGHT = 28;
    private static final int SECTION_GAP = 20;

    /**
     * コード進行リストをPNG画像として出力
     */
    public static void exportToPng(List<ChordProgression> progressions, String title, File file) throws Exception {
        BufferedImage img = renderImage(progressions, title);
        ImageIO.write(img, "PNG", file);
    }

    /**
     * コード進行リストをHTMLファイルとして出力（印刷・PDF変換用）
     */
    public static void exportToHtml(List<ChordProgression> progressions, String title, File file) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        sb.append("<title>").append(title).append("</title>");
        sb.append("<style>");
        sb.append("body{font-family:sans-serif;padding:40px;max-width:800px;margin:0 auto;}");
        sb.append("h1{color:#333;border-bottom:2px solid #7048e8;padding-bottom:8px;}");
        sb.append(".card{border:1px solid #ddd;border-radius:8px;padding:16px;margin:12px 0;");
        sb.append("border-left:4px solid #7048e8;}");
        sb.append(".title{font-size:16px;font-weight:bold;color:#333;}");
        sb.append(".chords{font-family:monospace;font-size:14px;color:#5050cc;margin:6px 0;}");
        sb.append(".meta{font-size:11px;color:#999;}");
        sb.append(".stars{color:#ffc800;font-size:13px;}");
        sb.append("@media print{body{padding:20px;}}");
        sb.append("</style></head><body>");
        sb.append("<h1>").append(title).append("</h1>");
        sb.append("<p style='color:#999;font-size:12px;'>").append(progressions.size()).append(" 件のコード進行</p>");

        AppSettings settings = AppSettings.getInstance();
        for (ChordProgression cp : progressions) {
            String chordsStr = settings.getNoteMode() == AppSettings.NoteMode.DEGREE
                ? String.join(" → ", DegreeConverter.convertList(cp.getChords(), cp.getKey()))
                : cp.getChordsAsString();

            StringBuilder stars = new StringBuilder();
            for (int i = 1; i <= 5; i++) stars.append(i <= cp.getRating() ? "★" : "☆");

            sb.append("<div class='card'>");
            sb.append("<div class='title'>").append(cp.getTitle()).append("</div>");
            sb.append("<div class='chords'>").append(chordsStr).append("</div>");
            sb.append("<div class='stars'>").append(stars).append("</div>");
            if (!cp.getMemo().isEmpty()) {
                sb.append("<div class='meta'>").append(cp.getMemo()).append("</div>");
            }
            sb.append("<div class='meta'>BPM: ").append(cp.getTempo())
              .append(" | ").append(cp.getFormattedLastUsed()).append("</div>");
            sb.append("</div>");
        }
        sb.append("</body></html>");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(sb.toString().getBytes("UTF-8"));
        }
    }

    /**
     * 画像描画ロジック
     */
    private static BufferedImage renderImage(List<ChordProgression> progressions, String title) {
        // まず高さを計算
        int height = PADDING * 2 + 60 + progressions.size() * (LINE_HEIGHT * 4 + SECTION_GAP);

        BufferedImage img = new BufferedImage(WIDTH, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 日本語対応フォントを取得
        String[] fontCandidates = {"Yu Gothic", "Meiryo", "MS Gothic", "SansSerif"};
        String fontName = "SansSerif";
        for (String f : fontCandidates) {
            Font test = new Font(f, Font.PLAIN, 12);
            if (!test.getFamily().equals("Dialog")) { fontName = f; break; }
        }
        final String FONT = fontName;

        // 背景
        g2.setColor(new Color(245, 245, 250));
        g2.fillRect(0, 0, WIDTH, height);

        // タイトル
        g2.setColor(new Color(50, 50, 80));
        g2.setFont(new Font(FONT, Font.BOLD, 22));
        g2.drawString(title, PADDING, PADDING + 30);

        // 区切り線
        g2.setColor(new Color(112, 72, 232));
        g2.fillRect(PADDING, PADDING + 38, WIDTH - PADDING * 2, 2);

        int y = PADDING + 70;
        AppSettings settings = AppSettings.getInstance();

        for (ChordProgression cp : progressions) {
            // カード背景
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(PADDING, y, WIDTH - PADDING * 2, LINE_HEIGHT * 4, 10, 10);
            g2.setColor(new Color(112, 72, 232));
            g2.fillRoundRect(PADDING, y, 4, LINE_HEIGHT * 4, 4, 4);

            // タイトル
            g2.setColor(new Color(30, 30, 50));
            g2.setFont(new Font(FONT, Font.BOLD, 15));
            g2.drawString(cp.getTitle(), PADDING + 16, y + 22);

            // コード
            String chordsStr = settings.getNoteMode() == AppSettings.NoteMode.DEGREE
                ? String.join("  →  ", DegreeConverter.convertList(cp.getChords(), cp.getKey()))
                : String.join("  →  ", cp.getChords());
            g2.setColor(new Color(80, 80, 200));
            g2.setFont(new Font("Monospaced", Font.PLAIN, 13));
            g2.drawString(chordsStr, PADDING + 16, y + 22 + LINE_HEIGHT);

            // 星
            g2.setColor(new Color(255, 200, 50));
            StringBuilder stars = new StringBuilder();
            for (int i = 1; i <= 5; i++) stars.append(i <= cp.getRating() ? "★" : "☆");
            g2.setFont(new Font(FONT, Font.PLAIN, 12));
            g2.drawString(stars.toString(), PADDING + 16, y + 22 + LINE_HEIGHT * 2);

            // メタ情報
            g2.setColor(new Color(150, 150, 180));
            g2.setFont(new Font(FONT, Font.PLAIN, 11));
            g2.drawString("BPM: " + cp.getTempo() + "  |  " + cp.getFormattedLastUsed(),
                PADDING + 16, y + 22 + LINE_HEIGHT * 3);

            y += LINE_HEIGHT * 4 + SECTION_GAP;
        }

        g2.dispose();
        return img;
    }
}
