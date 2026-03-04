package com.tripadextension;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

/**
 * OpenGL drawing utilities for Tripad buttons.
 * Renders game-style buttons with chamfered corners, metallic gray frame,
 * and teal fill — matching the Starsector campaign bar aesthetic.
 */
public class TripadRenderer {

    // Dynamic colors — refreshed each frame via refreshColors()
    private static Color bgNormal;
    private static Color bgHover;
    private static Color bgPressed;
    private static Color borderNormal;
    private static Color borderHover;
    private static Color frameBright;    // metallic gray frame highlight
    private static Color frameDark;      // metallic gray frame shadow
    private static Color textNormal;
    private static Color textHover;
    private static Color tooltipBg;
    private static Color tooltipBorder;

    // Lock/snap colors (gold tint, kept static)
    public static final Color LOCK_ICON_COLOR = new Color(200, 180, 80, 200);
    public static final Color SNAP_INDICATOR = new Color(200, 180, 80, 80);

    // Corner chamfer size in pixels
    public static final float CHAMFER = 4f;

    // Button dimensions matching game style
    public static final float BTN_HEIGHT = 28f;
    public static final float BTN_PAD_X = 14f;
    public static final float BTN_MIN_WIDTH = 80f;
    public static final float BTN_MARGIN = 10f;
    public static final float GROUP_GAP = 2f;

    // Font
    private static LazyFont gameFont;
    private static boolean fontLoaded = false;

    private TripadRenderer() {}

    /**
     * Refresh dynamic colors from game settings.
     * Call once per frame before rendering any buttons.
     */
    public static void refreshColors() {
        // Fetch game's actual button colors from settings.json
        Color buttonBg = Global.getSettings().getColor("buttonBg");           // [70, 222, 255] bright cyan fill
        Color buttonBgDark = Global.getSettings().getColor("buttonBgDark");   // [31, 94, 112, 175] darker state
        Color btnText = Misc.getButtonTextColor();                            // [170, 222, 255] text
        Color highlight = Misc.getHighlightColor();                           // [255, 210, 0] gold

        // Background: filled teal/cyan like game's bottom bar buttons
        bgNormal = new Color(buttonBgDark.getRed(), buttonBgDark.getGreen(), buttonBgDark.getBlue(), 175);
        bgHover = new Color(
                (buttonBg.getRed() + buttonBgDark.getRed()) / 2,
                (buttonBg.getGreen() + buttonBgDark.getGreen()) / 2,
                (buttonBg.getBlue() + buttonBgDark.getBlue()) / 2,
                200);
        bgPressed = new Color(buttonBgDark.getRed() * 2 / 3, buttonBgDark.getGreen() * 2 / 3, buttonBgDark.getBlue() * 2 / 3, 210);

        // Inner border: buttonBg color
        borderNormal = new Color(buttonBg.getRed(), buttonBg.getGreen(), buttonBg.getBlue(), 140);
        borderHover = new Color(buttonBg.getRed(), buttonBg.getGreen(), buttonBg.getBlue(), 220);

        // Metallic gray frame (outer)
        frameBright = new Color(160, 170, 180, 200);
        frameDark = new Color(60, 65, 75, 180);

        // Text
        textNormal = btnText;
        textHover = new Color(255, 255, 255, 255);

        // Tooltip
        tooltipBg = new Color(10, 18, 30, 235);
        tooltipBorder = new Color(buttonBg.getRed(), buttonBg.getGreen(), buttonBg.getBlue(), 150);
    }

    // Color accessors
    public static Color getBgNormal() { return bgNormal; }
    public static Color getBgHover() { return bgHover; }
    public static Color getBgPressed() { return bgPressed; }
    public static Color getBorderNormal() { return borderNormal; }
    public static Color getBorderHover() { return borderHover; }
    public static Color getFrameBright() { return frameBright; }
    public static Color getFrameDark() { return frameDark; }
    public static Color getTextNormal() { return textNormal; }
    public static Color getTextHover() { return textHover; }
    public static Color getTooltipBg() { return tooltipBg; }
    public static Color getTooltipBorder() { return tooltipBorder; }

    /** Lazily load the game font. Call from rendering context. */
    public static LazyFont getFont() {
        if (!fontLoaded) {
            fontLoaded = true;
            try {
                gameFont = LazyFont.loadFont("graphics/fonts/insignia15LTaa.fnt");
            } catch (Exception e) {
                try {
                    gameFont = LazyFont.loadFont("graphics/fonts/insignia12LTaa.fnt");
                } catch (Exception e2) {
                    // Font not available - text won't render
                }
            }
        }
        return gameFont;
    }

    /** Compute button width based on text label. */
    public static float computeButtonWidth(String label) {
        LazyFont font = getFont();
        if (font == null) {
            return Math.max(BTN_MIN_WIDTH, label.length() * 8f + BTN_PAD_X * 2);
        }
        float textW = font.createText(label).getWidth();
        return Math.max(BTN_MIN_WIDTH, textW + BTN_PAD_X * 2);
    }

    /** Draw text centered in the given rectangle area. */
    public static void drawCenteredText(String text, float x, float y, float w, float h, Color color) {
        LazyFont font = getFont();
        if (font == null) return;

        LazyFont.DrawableString ds = font.createText(text, color, 15f);
        float textW = ds.getWidth();
        float textH = ds.getHeight();

        float tx = x + (w - textW) / 2f;
        float ty = y + h - (h - textH) / 2f;
        ds.draw(tx, ty);
    }

    /** Draw text left-aligned with padding. */
    public static void drawText(String text, float x, float y, Color color) {
        LazyFont font = getFont();
        if (font == null) return;
        LazyFont.DrawableString ds = font.createText(text, color, 15f);
        ds.draw(x, y + ds.getHeight());
    }

    // ========================================================================
    // GL Primitives
    // ========================================================================

    public static void setColor(Color c, float alphaMult) {
        GL11.glColor4f(
            c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f,
            (c.getAlpha() / 255f) * alphaMult
        );
    }

    /**
     * Draw a filled chamfered rectangle (octagonal shape with cut corners).
     * The chamfer cuts each corner at 45 degrees by 'c' pixels.
     */
    public static void drawChamferedRect(float x, float y, float w, float h, float c) {
        GL11.glBegin(GL11.GL_POLYGON);
        // Bottom edge, left to right
        GL11.glVertex2f(x + c, y);
        GL11.glVertex2f(x + w - c, y);
        // Bottom-right chamfer
        GL11.glVertex2f(x + w, y + c);
        // Right edge
        GL11.glVertex2f(x + w, y + h - c);
        // Top-right chamfer
        GL11.glVertex2f(x + w - c, y + h);
        // Top edge, right to left
        GL11.glVertex2f(x + c, y + h);
        // Top-left chamfer
        GL11.glVertex2f(x, y + h - c);
        // Left edge
        GL11.glVertex2f(x, y + c);
        GL11.glEnd();
    }

    /**
     * Draw a chamfered rectangle outline.
     */
    public static void drawChamferedRectOutline(float x, float y, float w, float h, float c, float lineW) {
        GL11.glLineWidth(lineW);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(x + c, y);
        GL11.glVertex2f(x + w - c, y);
        GL11.glVertex2f(x + w, y + c);
        GL11.glVertex2f(x + w, y + h - c);
        GL11.glVertex2f(x + w - c, y + h);
        GL11.glVertex2f(x + c, y + h);
        GL11.glVertex2f(x, y + h - c);
        GL11.glVertex2f(x, y + c);
        GL11.glEnd();
        GL11.glLineWidth(1f);
    }

    // Keep regular rect for tooltips etc.
    public static void drawRect(float x, float y, float w, float h) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x, y + h);
        GL11.glVertex2f(x + w, y + h);
        GL11.glVertex2f(x + w, y);
        GL11.glEnd();
    }

    public static void drawRectOutline(float x, float y, float w, float h, float lineW) {
        GL11.glLineWidth(lineW);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x, y + h);
        GL11.glVertex2f(x + w, y + h);
        GL11.glVertex2f(x + w, y);
        GL11.glEnd();
        GL11.glLineWidth(1f);
    }

    public static void drawLine(float x1, float y1, float x2, float y2, float lineW) {
        GL11.glLineWidth(lineW);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x2, y2);
        GL11.glEnd();
        GL11.glLineWidth(1f);
    }

    public static void drawLockIcon(float cx, float cy, float size, float alpha) {
        setColor(LOCK_ICON_COLOR, alpha);
        float s = size * 0.5f;
        drawRect(cx - s, cy - s * 0.3f, s * 2f, s * 1.3f);
        GL11.glLineWidth(2f);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i = 0; i <= 10; i++) {
            float angle = (float) Math.toRadians(180 + 180 * i / 10f);
            GL11.glVertex2f(
                cx + (float) Math.cos(angle) * s * 0.6f,
                cy - s * 0.3f + (float) Math.sin(angle) * s * 0.7f
            );
        }
        GL11.glEnd();
        GL11.glLineWidth(1f);
    }

    public static void beginRender() {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
    }

    public static void endRender() {
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
