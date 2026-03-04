package com.tripadextension;

import java.awt.Color;

/**
 * Runtime state for a single Tripad floating button.
 * Created from a TripadButtonSpec and managed by TripadManager.
 */
public class TripadButton {

    // Identity
    private final String id;
    private final String label;
    private final String tooltip;
    private final TripadClickHandler handler;
    private final TripadIconRenderer iconRenderer;
    private final Color iconColor;
    private final int sortOrder;

    // Position (screen coordinates)
    private float x;
    private float y;

    // Computed width (based on text label)
    private float width = -1;

    // Visual state
    private boolean hovered;
    private float hoverAlpha;

    // Group membership (null = ungrouped)
    private TripadButtonGroup group;

    public TripadButton(TripadButtonSpec spec) {
        this.id = spec.getId();
        this.label = spec.getLabel();
        this.tooltip = spec.getTooltip();
        this.handler = spec.getHandler();
        this.iconRenderer = spec.getIconRenderer();
        this.iconColor = spec.getIconColor();
        this.sortOrder = spec.getSortOrder();
        this.x = -1;
        this.y = -1;
    }

    public String getId() { return id; }
    public String getLabel() { return label; }
    public String getTooltip() { return tooltip; }
    public TripadClickHandler getHandler() { return handler; }
    public TripadIconRenderer getIconRenderer() { return iconRenderer; }
    public Color getIconColor() { return iconColor; }
    public int getSortOrder() { return sortOrder; }

    public float getX() { return x; }
    public float getY() { return y; }
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setPosition(float x, float y) { this.x = x; this.y = y; }

    /** Get button width. Computed lazily from label text on first call. */
    public float getWidth() {
        if (width < 0) {
            width = TripadRenderer.computeButtonWidth(label);
        }
        return width;
    }

    public float getHeight() { return TripadRenderer.BTN_HEIGHT; }

    public boolean isHovered() { return hovered; }
    public void setHovered(boolean hovered) { this.hovered = hovered; }

    public float getHoverAlpha() { return hoverAlpha; }
    public void setHoverAlpha(float hoverAlpha) { this.hoverAlpha = hoverAlpha; }

    public TripadButtonGroup getGroup() { return group; }
    public void setGroup(TripadButtonGroup group) { this.group = group; }
    public boolean isGrouped() { return group != null; }

    public boolean isPositionInitialized() { return x >= 0 && y >= 0; }
}
