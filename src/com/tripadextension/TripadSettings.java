package com.tripadextension;

import lunalib.lunaSettings.LunaSettings;
import org.apache.log4j.Logger;

/**
 * Settings wrapper for Tripad Extension.
 * Reads from LunaLib directly (required dependency).
 */
public class TripadSettings {

    private static final Logger log = Logger.getLogger(TripadSettings.class);
    public static final String MOD_ID = "tripad_extension";

    // Manual button toggles
    public static boolean enableNexusUI = true;
    public static boolean enableOperatives = true;

    /** Load settings from LunaLib. */
    public static void loadSettings() {
        try {
            Boolean val;

            val = LunaSettings.getBoolean(MOD_ID, "tripad_enable_nexusui");
            if (val != null) enableNexusUI = val;

            val = LunaSettings.getBoolean(MOD_ID, "tripad_enable_operatives");
            if (val != null) enableOperatives = val;

            log.info("Tripad: LunaLib settings loaded — NexusUI=" + enableNexusUI
                    + ", Operatives=" + enableOperatives);
        } catch (Exception e) {
            log.warn("Tripad: Failed to load LunaLib settings, using defaults", e);
        }
    }
}
