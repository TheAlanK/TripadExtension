package com.tripadextension;

import java.awt.Color;

/**
 * Specification for registering a button with the Tripad framework.
 * Use the builder-style setters to configure the button.
 *
 * Example:
 *   TripadButtonSpec spec = new TripadButtonSpec("my_mod_button")
 *       .setLabel("M")
 *       .setTooltip("Open My Mod Panel")
 *       .setHandler(new TripadClickHandler() { ... })
 *       .setIconColor(new Color(80, 180, 255));
 */
public class TripadButtonSpec {

    private String id;
    private String label;
    private String tooltip;
    private TripadClickHandler handler;
    private TripadIconRenderer iconRenderer;
    private Color iconColor;
    private int sortOrder;

    public TripadButtonSpec(String id) {
        this.id = id;
        this.label = id.substring(0, 1).toUpperCase();
        this.tooltip = id;
        this.iconColor = new Color(80, 180, 255);
        this.sortOrder = 100;
    }

    public String getId() { return id; }

    public String getLabel() { return label; }
    public TripadButtonSpec setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getTooltip() { return tooltip; }
    public TripadButtonSpec setTooltip(String tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public TripadClickHandler getHandler() { return handler; }
    public TripadButtonSpec setHandler(TripadClickHandler handler) {
        this.handler = handler;
        return this;
    }

    public TripadIconRenderer getIconRenderer() { return iconRenderer; }
    public TripadButtonSpec setIconRenderer(TripadIconRenderer iconRenderer) {
        this.iconRenderer = iconRenderer;
        return this;
    }

    public Color getIconColor() { return iconColor; }
    public TripadButtonSpec setIconColor(Color iconColor) {
        this.iconColor = iconColor;
        return this;
    }

    /** Lower sort order = further left in button bar. Default 100. */
    public int getSortOrder() { return sortOrder; }
    public TripadButtonSpec setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }
}
