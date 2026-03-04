package com.tripadextension;

/**
 * Optional custom icon renderer for Tripad buttons.
 * If not provided, the button renders the first character of its label.
 */
public interface TripadIconRenderer {
    /**
     * Draw the button icon centered at (cx, cy).
     * Called within a GL context with textures disabled and blending enabled.
     *
     * @param cx     center X in screen coordinates
     * @param cy     center Y in screen coordinates
     * @param size   half-size of the icon area (icon should fit within +-size)
     * @param alpha  current alpha multiplier (0-1), use for hover/drag effects
     */
    void renderIcon(float cx, float cy, float size, float alpha);
}
