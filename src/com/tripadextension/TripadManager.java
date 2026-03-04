package com.tripadextension;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Singleton manager for the Tripad floating button framework.
 * Handles button registration, group management, and position persistence.
 *
 * Usage from other mods:
 *   TripadManager.getInstance().registerButton(spec);
 */
public class TripadManager {

    private static final Logger log = Logger.getLogger(TripadManager.class);
    private static final String PERSIST_KEY = "$tripad_positions";
    private static final String GROUPS_KEY = "$tripad_groups";

    private static TripadManager instance;

    private final Map<String, TripadButton> buttons = new LinkedHashMap<String, TripadButton>();
    private final List<TripadButtonGroup> groups = new ArrayList<TripadButtonGroup>();

    private TripadManager() {}

    public static TripadManager getInstance() {
        if (instance == null) {
            instance = new TripadManager();
        }
        return instance;
    }

    /** Reset instance (called on game load to clear stale state). */
    public static void reset() {
        instance = new TripadManager();
    }

    // ========================================================================
    // Button Registration
    // ========================================================================

    /**
     * Register a button with the framework.
     * If a button with the same ID already exists, it is replaced.
     */
    public void registerButton(TripadButtonSpec spec) {
        if (spec == null || spec.getId() == null) return;
        TripadButton btn = new TripadButton(spec);
        buttons.put(spec.getId(), btn);
        log.info("Tripad: Registered button '" + spec.getId() + "'");
    }

    /** Unregister a button by ID. Also removes it from any group. */
    public void unregisterButton(String id) {
        TripadButton btn = buttons.remove(id);
        if (btn != null && btn.getGroup() != null) {
            TripadButtonGroup group = btn.getGroup();
            group.removeButton(btn);
            if (group.size() <= 1) {
                dissolveGroup(group);
            }
        }
    }

    public TripadButton getButton(String id) {
        return buttons.get(id);
    }

    public Collection<TripadButton> getButtons() {
        return buttons.values();
    }

    /** Get buttons sorted by sort order. */
    public List<TripadButton> getButtonsSorted() {
        List<TripadButton> sorted = new ArrayList<TripadButton>(buttons.values());
        Collections.sort(sorted, new Comparator<TripadButton>() {
            public int compare(TripadButton a, TripadButton b) {
                return Integer.compare(a.getSortOrder(), b.getSortOrder());
            }
        });
        return sorted;
    }

    // ========================================================================
    // Group Management
    // ========================================================================

    /**
     * Lock two buttons together into a group.
     * If either button is already in a group, the groups merge.
     */
    public TripadButtonGroup lockButtons(TripadButton a, TripadButton b) {
        if (a == b) return a.getGroup();

        TripadButtonGroup groupA = a.getGroup();
        TripadButtonGroup groupB = b.getGroup();

        if (groupA != null && groupB != null) {
            // Merge B's group into A's group
            if (groupA == groupB) return groupA;
            for (TripadButton btn : new ArrayList<TripadButton>(groupB.getButtons())) {
                groupB.removeButton(btn);
                groupA.addButton(btn);
            }
            groups.remove(groupB);
            groupA.relayout();
            groupA.startLockAnim();
            return groupA;
        } else if (groupA != null) {
            groupA.addButton(b);
            groupA.relayout();
            groupA.startLockAnim();
            return groupA;
        } else if (groupB != null) {
            groupB.addButton(a);
            groupB.relayout();
            groupB.startLockAnim();
            return groupB;
        } else {
            // Create new group
            TripadButtonGroup newGroup = new TripadButtonGroup();
            newGroup.addButton(a);
            newGroup.addButton(b);
            groups.add(newGroup);
            // Position group at midpoint
            float midX = (a.getX() + b.getX()) / 2f;
            float midY = (a.getY() + b.getY()) / 2f;
            newGroup.setAnchorPosition(midX, midY);
            newGroup.startLockAnim();
            return newGroup;
        }
    }

    /** Unlock a button from its group. If group has <=1 button left, dissolve it. */
    public void unlockButton(TripadButton button) {
        TripadButtonGroup group = button.getGroup();
        if (group == null) return;
        group.removeButton(button);
        if (group.size() <= 1) {
            dissolveGroup(group);
        } else {
            group.relayout();
        }
    }

    /** Dissolve an entire group, releasing all buttons. */
    public void dissolveGroup(TripadButtonGroup group) {
        group.dissolve();
        groups.remove(group);
    }

    public List<TripadButtonGroup> getGroups() {
        return groups;
    }

    // ========================================================================
    // Position Persistence
    // ========================================================================

    /** Save button positions and group data to sector persistent data. */
    public void savePositions() {
        try {
            // Save positions
            JSONObject posData = new JSONObject();
            for (TripadButton btn : buttons.values()) {
                if (btn.isPositionInitialized()) {
                    JSONObject pos = new JSONObject();
                    pos.put("x", btn.getX());
                    pos.put("y", btn.getY());
                    posData.put(btn.getId(), pos);
                }
            }
            Global.getSector().getPersistentData().put(PERSIST_KEY, posData.toString());

            // Save groups
            JSONArray groupsArr = new JSONArray();
            for (TripadButtonGroup group : groups) {
                JSONArray ids = new JSONArray();
                for (TripadButton btn : group.getButtons()) {
                    ids.put(btn.getId());
                }
                groupsArr.put(ids);
            }
            Global.getSector().getPersistentData().put(GROUPS_KEY, groupsArr.toString());
        } catch (Exception e) {
            log.warn("Tripad: Failed to save positions", e);
        }
    }

    /** Load button positions and group data from sector persistent data. */
    public void loadPositions() {
        try {
            // Load positions
            Object posObj = Global.getSector().getPersistentData().get(PERSIST_KEY);
            if (posObj instanceof String) {
                JSONObject posData = new JSONObject((String) posObj);
                Iterator<?> keys = posData.keys();
                while (keys.hasNext()) {
                    String id = (String) keys.next();
                    TripadButton btn = buttons.get(id);
                    if (btn != null) {
                        JSONObject pos = posData.getJSONObject(id);
                        btn.setPosition((float) pos.getDouble("x"), (float) pos.getDouble("y"));
                    }
                }
            }

            // Load groups
            Object grpObj = Global.getSector().getPersistentData().get(GROUPS_KEY);
            if (grpObj instanceof String) {
                JSONArray groupsArr = new JSONArray((String) grpObj);
                for (int i = 0; i < groupsArr.length(); i++) {
                    JSONArray ids = groupsArr.getJSONArray(i);
                    if (ids.length() < 2) continue;
                    TripadButtonGroup group = new TripadButtonGroup();
                    boolean valid = true;
                    for (int j = 0; j < ids.length(); j++) {
                        TripadButton btn = buttons.get(ids.getString(j));
                        if (btn != null) {
                            group.addButton(btn);
                        }
                    }
                    if (group.size() >= 2) {
                        groups.add(group);
                        group.relayout();
                    } else {
                        group.dissolve();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Tripad: Failed to load positions", e);
        }
    }
}
