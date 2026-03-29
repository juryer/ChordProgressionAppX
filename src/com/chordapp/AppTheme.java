package com.chordapp;

import java.awt.*;

/**
 * アプリのカラーテーマ管理
 * AppSettings.ColorTheme に応じて色を切り替える
 */
public class AppTheme {

    // ── テーマ定義 ────────────────────────────────────────────────────────────
    public static Color BG_DARK;
    public static Color BG_PANEL;
    public static Color BG_CARD;
    public static Color BG_CARD_HOVER;
    public static Color ACCENT;
    public static Color ACCENT_LIGHT;
    public static Color ACCENT2;
    public static Color TEXT_PRIMARY;
    public static Color TEXT_SECONDARY;
    public static Color TEXT_MUTED;
    public static Color BORDER;
    public static Color BTN_HOVER;

    static { applyTheme(AppSettings.ColorTheme.DARK_PURPLE); }

    public static void applyTheme(AppSettings.ColorTheme theme) {
        switch (theme) {
            case DARK_PURPLE:
            default:
                BG_DARK       = new Color(18, 18, 28);
                BG_PANEL      = new Color(26, 26, 42);
                BG_CARD       = new Color(34, 34, 54);
                BG_CARD_HOVER = new Color(44, 44, 70);
                ACCENT        = new Color(112, 72, 232);
                ACCENT_LIGHT  = new Color(150, 110, 255);
                ACCENT2       = new Color(72, 200, 182);
                TEXT_PRIMARY  = new Color(235, 235, 245);
                TEXT_SECONDARY= new Color(155, 155, 185);
                TEXT_MUTED    = new Color(100, 100, 130);
                BORDER        = new Color(55, 55, 80);
                BTN_HOVER     = new Color(130, 90, 255);
                break;

            case DARK_GREEN:
                BG_DARK       = new Color(14, 22, 18);
                BG_PANEL      = new Color(20, 32, 26);
                BG_CARD       = new Color(28, 44, 36);
                BG_CARD_HOVER = new Color(36, 58, 46);
                ACCENT        = new Color(46, 160, 90);
                ACCENT_LIGHT  = new Color(80, 200, 120);
                ACCENT2       = new Color(60, 200, 160);
                TEXT_PRIMARY  = new Color(225, 245, 230);
                TEXT_SECONDARY= new Color(140, 185, 155);
                TEXT_MUTED    = new Color(90, 130, 105);
                BORDER        = new Color(40, 70, 54);
                BTN_HOVER     = new Color(60, 180, 100);
                break;

            case LIGHT:
                BG_DARK       = new Color(245, 245, 250);
                BG_PANEL      = new Color(255, 255, 255);
                BG_CARD       = new Color(255, 255, 255);
                BG_CARD_HOVER = new Color(235, 235, 245);
                ACCENT        = new Color(90, 60, 200);
                ACCENT_LIGHT  = new Color(110, 80, 220);
                ACCENT2       = new Color(30, 160, 140);
                TEXT_PRIMARY  = new Color(30, 30, 50);
                TEXT_SECONDARY= new Color(90, 90, 120);
                TEXT_MUTED    = new Color(150, 150, 180);
                BORDER        = new Color(210, 210, 225);
                BTN_HOVER     = new Color(110, 80, 220);
                break;
        }
    }

    // Fonts
    public static Font titleFont(float size) {
        return new Font("SansSerif", Font.BOLD, (int) size);
    }
    public static Font bodyFont(float size) {
        return new Font("SansSerif", Font.PLAIN, (int) size);
    }
    public static Font monoFont(float size) {
        return new Font("Monospaced", Font.PLAIN, (int) size);
    }

    public static final int RADIUS = 14;

    /** ライトモード判定 */
    public static boolean isLight() {
        return AppSettings.getInstance().getColorTheme() == AppSettings.ColorTheme.LIGHT;
    }

    /** ボタン文字色（アクセント背景上） */
    public static Color btnFg() {
        return Color.WHITE;
    }

    /** カード背景色 */
    public static Color cardBg() {
        return isLight() ? new Color(248, 248, 255) : BG_CARD;
    }

    /** ヘッダー背景グラデーション開始色 */
    public static Color headerBgFrom() {
        return isLight() ? new Color(230, 225, 255) : new Color(30, 20, 60);
    }

    /** ヘッダー背景グラデーション終了色 */
    public static Color headerBgTo() {
        return isLight() ? new Color(215, 230, 255) : new Color(20, 40, 70);
    }

    /** テキスト入力フィールド背景色 */
    public static Color inputBg() {
        return isLight() ? new Color(245, 245, 255) : new Color(40, 40, 64);
    }

    /** プレビューエリア背景色 */
    public static Color previewBg() {
        return isLight() ? new Color(250, 250, 255) : new Color(14, 14, 22);
    }

    /** 空カード（点線）の背景色 */
    public static Color emptyCardBg() {
        return isLight() ? new Color(240, 240, 248) : new Color(26, 26, 42);
    }

    /** チップ（コード選択済み）の背景色 */
    public static Color chipBg() {
        return isLight() ? new Color(90, 60, 200) : new Color(60, 80, 140);
    }
}
