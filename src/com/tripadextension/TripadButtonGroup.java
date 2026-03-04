package com.tripadextension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A group of locked Tripad buttons that move together.
 * Buttons in a group are laid out horizontally with a small gap.
 */
public class TripadButtonGroup {

    private final List<TripadButton> buttons = new ArrayList<TripadButton>();

    // Lock animation state
    private float lockAnimProgress = 0f;
    private boolean lockAnimPlaying = false;

    public TripadButtonGroup() {}

    public void addButton(TripadButton button) {
        if (!buttons.contains(button)) {
            buttons.add(button);
            button.setGroup(this);
            sortButtons();
        }
    }

    public void removeButton(TripadButton button) {
        buttons.remove(button);
        button.setGroup(null);
    }

    public List<TripadButton> getButtons() { return buttons; }
    public int size() { return buttons.size(); }
    public boolean contains(TripadButton button) { return buttons.contains(button); }

    public void dissolve() {
        for (TripadButton btn : new ArrayList<TripadButton>(buttons)) {
            btn.setGroup(null);
        }
        buttons.clear();
    }

    private void sortButtons() {
        Collections.sort(buttons, new Comparator<TripadButton>() {
            public int compare(TripadButton a, TripadButton b) {
                return Integer.compare(a.getSortOrder(), b.getSortOrder());
            }
        });
    }

    /** Reposition all buttons horizontally, using variable widths. */
    public void relayout() {
        if (buttons.isEmpty()) return;
        float anchorX = buttons.get(0).getX();
        float anchorY = buttons.get(0).getY();
        float currentX = anchorX;
        for (TripadButton btn : buttons) {
            btn.setPosition(currentX, anchorY);
            currentX += btn.getWidth() + TripadRenderer.GROUP_GAP;
        }
    }

    public void moveBy(float dx, float dy) {
        for (TripadButton btn : buttons) {
            btn.setPosition(btn.getX() + dx, btn.getY() + dy);
        }
    }

    public void setAnchorPosition(float x, float y) {
        if (buttons.isEmpty()) return;
        buttons.get(0).setPosition(x, y);
        relayout();
    }

    public float getTotalWidth() {
        if (buttons.isEmpty()) return 0;
        float total = 0;
        for (TripadButton btn : buttons) {
            total += btn.getWidth();
        }
        total += (buttons.size() - 1) * TripadRenderer.GROUP_GAP;
        return total;
    }

    // Animation
    public float getLockAnimProgress() { return lockAnimProgress; }
    public void setLockAnimProgress(float p) { this.lockAnimProgress = p; }
    public boolean isLockAnimPlaying() { return lockAnimPlaying; }
    public void startLockAnim() { lockAnimPlaying = true; lockAnimProgress = 0f; }
    public void stopLockAnim() { lockAnimPlaying = false; lockAnimProgress = 1f; }
}
