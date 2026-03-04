package com.tripadextension;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.listeners.CampaignInputListener;
import com.fs.starfarer.api.campaign.listeners.CampaignUIRenderingListener;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import org.apache.log4j.Logger;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Main rendering and input handler for Tripad floating buttons.
 *
 * Renders game-style rectangular buttons with full text labels.
 * Supports drag-and-drop, snap-to-lock grouping, right-click to unlock.
 */
public class TripadOverlay implements CampaignUIRenderingListener, CampaignInputListener {

    private static final Logger log = Logger.getLogger(TripadOverlay.class);

    private static final float DRAG_THRESHOLD = 5f;
    private static final float SNAP_DISTANCE = 60f;
    private static final float LOCK_ANIM_SPEED = 4f;
    private static final float HOVER_FADE_SPEED = 8f;

    // Default position: just above the game's bottom bar, after the built-in buttons
    private static final float DEFAULT_START_X = 310f;
    private static final float DEFAULT_Y = 46f;

    // Input state
    private int mouseX, mouseY;
    private boolean mouseDown = false;
    private boolean dragging = false;
    private float dragStartX, dragStartY;
    private float dragOffsetX, dragOffsetY;
    private TripadButton dragButton = null;
    private TripadButton snapTarget = null;

    // ========================================================================
    // Rendering
    // ========================================================================

    public void renderInUICoordsBelowUI(ViewportAPI viewport) {}
    public void renderInUICoordsAboveUIBelowTooltips(ViewportAPI viewport) {}

    public void renderInUICoordsAboveUIAndTooltips(ViewportAPI viewport) {
        TripadManager mgr = TripadManager.getInstance();
        List<TripadButton> buttons = mgr.getButtonsSorted();
        if (buttons.isEmpty()) return;

        float screenW = Global.getSettings().getScreenWidth();
        float screenH = Global.getSettings().getScreenHeight();
        float dt = 0.016f;

        // Refresh dynamic colors from game settings
        TripadRenderer.refreshColors();

        // Initialize positions for new buttons
        initializePositions(buttons, screenW, screenH);

        // Advance lock animations
        for (TripadButtonGroup group : new ArrayList<TripadButtonGroup>(mgr.getGroups())) {
            if (group.isLockAnimPlaying()) {
                float p = group.getLockAnimProgress() + dt * LOCK_ANIM_SPEED;
                if (p >= 1f) {
                    group.stopLockAnim();
                } else {
                    group.setLockAnimProgress(p);
                }
            }
        }

        // Update hover alpha (smooth fade)
        for (TripadButton btn : buttons) {
            float target = btn.isHovered() ? 1f : 0f;
            float current = btn.getHoverAlpha();
            if (current < target) {
                btn.setHoverAlpha(Math.min(target, current + dt * HOVER_FADE_SPEED));
            } else if (current > target) {
                btn.setHoverAlpha(Math.max(target, current - dt * HOVER_FADE_SPEED));
            }
        }

        // --- GL rendering ---
        TripadRenderer.beginRender();

        // Snap indicator
        if (dragging && snapTarget != null) {
            renderSnapIndicator(snapTarget);
        }

        // Group connectors (lock icons between locked buttons)
        for (TripadButtonGroup group : mgr.getGroups()) {
            renderGroupConnector(group);
        }

        // Draw each button (background + border)
        for (TripadButton btn : buttons) {
            renderButtonBg(btn);
        }

        TripadRenderer.endRender();

        // Draw text labels (LazyFont needs textures enabled)
        for (TripadButton btn : buttons) {
            renderButtonText(btn);
        }

        // Draw tooltip for hovered button
        TripadButton hovered = null;
        for (TripadButton btn : buttons) {
            if (btn.isHovered() && !dragging) hovered = btn;
        }
        if (hovered != null && hovered.getTooltip() != null && !hovered.getTooltip().equals(hovered.getLabel())) {
            renderTooltip(hovered, screenW, screenH);
        }
    }

    private void initializePositions(List<TripadButton> buttons, float screenW, float screenH) {
        float currentX = DEFAULT_START_X;
        int uninitCount = 0;

        for (TripadButton btn : buttons) {
            if (!btn.isPositionInitialized()) {
                btn.setPosition(currentX, DEFAULT_Y);
                currentX += btn.getWidth() + 4f;
                uninitCount++;
            }
        }
    }

    private void renderButtonBg(TripadButton btn) {
        float x = btn.getX();
        float y = btn.getY();
        float w = btn.getWidth();
        float h = btn.getHeight();
        float hover = btn.getHoverAlpha();
        boolean isDragged = (dragging && btn == dragButton);
        float alpha = isDragged ? 0.85f : 1f;
        float c = TripadRenderer.CHAMFER;

        // 1) Outer metallic gray frame (2px outset)
        float fx = x - 2f;
        float fy = y - 2f;
        float fw = w + 4f;
        float fh = h + 4f;

        // Frame bright fill (top-left half feel)
        TripadRenderer.setColor(TripadRenderer.getFrameBright(), alpha * 0.7f);
        TripadRenderer.drawChamferedRect(fx, fy, fw, fh, c + 1f);

        // Frame dark fill overlay (bottom-right half) — draw dark rect slightly offset
        TripadRenderer.setColor(TripadRenderer.getFrameDark(), alpha * 0.5f);
        TripadRenderer.drawChamferedRect(fx + 1f, fy, fw - 1f, fh - 1f, c + 1f);

        // 2) Inner background fill (chamfered)
        Color bg;
        if (isDragged) {
            bg = TripadRenderer.getBgPressed();
        } else if (hover > 0.5f) {
            bg = TripadRenderer.getBgHover();
        } else {
            bg = TripadRenderer.getBgNormal();
        }
        TripadRenderer.setColor(bg, alpha);
        TripadRenderer.drawChamferedRect(x, y, w, h, c);

        // 3) Inner border outline (teal/cyan, chamfered)
        Color border = hover > 0.3f ? TripadRenderer.getBorderHover() : TripadRenderer.getBorderNormal();
        TripadRenderer.setColor(border, alpha);
        TripadRenderer.drawChamferedRectOutline(x, y, w, h, c, 1f);

        // 4) Outer frame outline (metallic gray, chamfered)
        TripadRenderer.setColor(TripadRenderer.getFrameBright(), alpha * 0.9f);
        TripadRenderer.drawChamferedRectOutline(fx, fy, fw, fh, c + 1f, 1f);
    }

    private void renderButtonText(TripadButton btn) {
        float x = btn.getX();
        float y = btn.getY();
        float w = btn.getWidth();
        float h = btn.getHeight();
        float hover = btn.getHoverAlpha();
        boolean isDragged = (dragging && btn == dragButton);

        Color textColor = hover > 0.3f ? TripadRenderer.getTextHover() : TripadRenderer.getTextNormal();
        if (isDragged) {
            textColor = new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 180);
        }
        TripadRenderer.drawCenteredText(btn.getLabel(), x, y, w, h, textColor);
    }

    private void renderGroupConnector(TripadButtonGroup group) {
        List<TripadButton> buttons = group.getButtons();
        if (buttons.size() < 2) return;
        float alpha = group.isLockAnimPlaying() ? group.getLockAnimProgress() : 1f;

        for (int i = 0; i < buttons.size() - 1; i++) {
            TripadButton a = buttons.get(i);
            TripadButton b = buttons.get(i + 1);

            float ax = a.getX() + a.getWidth();
            float ay = a.getY() + a.getHeight() / 2f;
            float bx = b.getX();
            float by = b.getY() + b.getHeight() / 2f;

            float midX = (ax + bx) / 2f;
            float midY = (ay + by) / 2f;
            TripadRenderer.drawLockIcon(midX, midY, 4f, 0.6f * alpha);
        }
    }

    private void renderSnapIndicator(TripadButton target) {
        float x = target.getX();
        float y = target.getY();
        float w = target.getWidth();
        float h = target.getHeight();
        float c = TripadRenderer.CHAMFER + 1f;
        TripadRenderer.setColor(TripadRenderer.SNAP_INDICATOR, 0.5f);
        TripadRenderer.drawChamferedRect(x - 3, y - 3, w + 6, h + 6, c);
        TripadRenderer.setColor(TripadRenderer.LOCK_ICON_COLOR, 0.5f);
        TripadRenderer.drawChamferedRectOutline(x - 3, y - 3, w + 6, h + 6, c, 1f);
    }

    private void renderTooltip(TripadButton btn, float screenW, float screenH) {
        String text = btn.getTooltip();
        if (text == null || text.isEmpty()) return;

        float tipW = TripadRenderer.computeButtonWidth(text) + 8f;
        float tipH = 22f;
        float tipX = btn.getX() + btn.getWidth() / 2f - tipW / 2f;
        float tipY = btn.getY() + btn.getHeight() + 4f;

        if (tipX < 2) tipX = 2;
        if (tipX + tipW > screenW - 2) tipX = screenW - 2 - tipW;
        if (tipY + tipH > screenH - 2) tipY = btn.getY() - tipH - 4f;

        // Background + border (GL)
        TripadRenderer.beginRender();
        TripadRenderer.setColor(TripadRenderer.getTooltipBg(), 1f);
        TripadRenderer.drawRect(tipX, tipY, tipW, tipH);
        TripadRenderer.setColor(TripadRenderer.getTooltipBorder(), 0.8f);
        TripadRenderer.drawRectOutline(tipX, tipY, tipW, tipH, 1f);
        TripadRenderer.endRender();

        // Text (LazyFont, needs textures)
        TripadRenderer.drawCenteredText(text, tipX, tipY, tipW, tipH,
                TripadRenderer.getTextHover());
    }

    // ========================================================================
    // Input Handling
    // ========================================================================

    public int getListenerInputPriority() { return 110; }

    public void processCampaignInputPreCore(List<InputEventAPI> events) {
        TripadManager mgr = TripadManager.getInstance();
        List<TripadButton> buttons = mgr.getButtonsSorted();

        for (InputEventAPI event : events) {
            if (event.isConsumed()) continue;

            if (event.isMouseMoveEvent()) {
                mouseX = event.getX();
                mouseY = event.getY();
                handleMouseMove(buttons, mgr, event);
            }
            if (event.isLMBDownEvent()) {
                handleLMBDown(buttons, event);
            }
            if (event.isLMBUpEvent()) {
                handleLMBUp(buttons, mgr, event);
            }
            if (event.isRMBUpEvent()) {
                handleRMBUp(buttons, mgr, event);
            }
        }

        // Update hover
        for (TripadButton btn : buttons) {
            btn.setHovered(isOverButton(mouseX, mouseY, btn));
        }
    }

    public void processCampaignInputPreFleetControl(List<InputEventAPI> events) {}
    public void processCampaignInputPostCore(List<InputEventAPI> events) {}

    private void handleMouseMove(List<TripadButton> buttons, TripadManager mgr, InputEventAPI event) {
        if (mouseDown && !dragging && dragButton != null) {
            float dx = mouseX - dragStartX;
            float dy = mouseY - dragStartY;
            if (dx * dx + dy * dy > DRAG_THRESHOLD * DRAG_THRESHOLD) {
                dragging = true;
            }
        }

        if (dragging && dragButton != null) {
            if (dragButton.isGrouped()) {
                TripadButtonGroup group = dragButton.getGroup();
                float newX = mouseX - dragOffsetX;
                float newY = mouseY - dragOffsetY;
                float oldX = dragButton.getX();
                float oldY = dragButton.getY();
                group.moveBy(newX - oldX, newY - oldY);
            } else {
                dragButton.setPosition(mouseX - dragOffsetX, mouseY - dragOffsetY);
                snapTarget = findSnapTarget(dragButton, buttons);
            }
            event.consume();
        }
    }

    private void handleLMBDown(List<TripadButton> buttons, InputEventAPI event) {
        for (int i = buttons.size() - 1; i >= 0; i--) {
            TripadButton btn = buttons.get(i);
            if (isOverButton(event.getX(), event.getY(), btn)) {
                mouseDown = true;
                dragging = false;
                dragButton = btn;
                dragStartX = event.getX();
                dragStartY = event.getY();
                dragOffsetX = event.getX() - btn.getX();
                dragOffsetY = event.getY() - btn.getY();
                snapTarget = null;
                event.consume();
                return;
            }
        }
    }

    private void handleLMBUp(List<TripadButton> buttons, TripadManager mgr, InputEventAPI event) {
        if (!mouseDown) return;

        if (dragging && dragButton != null) {
            if (snapTarget != null && !dragButton.isGrouped()) {
                mgr.lockButtons(dragButton, snapTarget);
                log.info("Tripad: Locked '" + dragButton.getId() + "' with '" + snapTarget.getId() + "'");
            }
            mgr.savePositions();
        } else if (dragButton != null && isOverButton(event.getX(), event.getY(), dragButton)) {
            if (dragButton.getHandler() != null) {
                try {
                    dragButton.getHandler().onClick();
                } catch (Exception e) {
                    log.error("Tripad: Click handler error for '" + dragButton.getId() + "'", e);
                }
            }
        }

        mouseDown = false;
        dragging = false;
        dragButton = null;
        snapTarget = null;
        event.consume();
    }

    private void handleRMBUp(List<TripadButton> buttons, TripadManager mgr, InputEventAPI event) {
        for (TripadButton btn : buttons) {
            if (isOverButton(event.getX(), event.getY(), btn) && btn.isGrouped()) {
                mgr.dissolveGroup(btn.getGroup());
                mgr.savePositions();
                event.consume();
                return;
            }
        }
    }

    private TripadButton findSnapTarget(TripadButton dragged, List<TripadButton> buttons) {
        float dragCX = dragged.getX() + dragged.getWidth() / 2f;
        float dragCY = dragged.getY() + dragged.getHeight() / 2f;
        float bestDist = SNAP_DISTANCE;
        TripadButton best = null;

        for (TripadButton btn : buttons) {
            if (btn == dragged) continue;
            if (btn.getGroup() != null && btn.getGroup() == dragged.getGroup()) continue;

            float cx = btn.getX() + btn.getWidth() / 2f;
            float cy = btn.getY() + btn.getHeight() / 2f;
            float dx = dragCX - cx;
            float dy = dragCY - cy;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            if (dist < bestDist) {
                bestDist = dist;
                best = btn;
            }
        }
        return best;
    }

    private boolean isOverButton(int mx, int my, TripadButton btn) {
        return mx >= btn.getX() && mx <= btn.getX() + btn.getWidth()
            && my >= btn.getY() && my <= btn.getY() + btn.getHeight();
    }
}
