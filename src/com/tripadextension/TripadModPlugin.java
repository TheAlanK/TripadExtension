package com.tripadextension;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.nexusui.core.NexusModPlugin;
import com.nexusui.overlay.NexusFrame;
import org.apache.log4j.Logger;

import javax.swing.SwingUtilities;
import java.awt.Color;
import java.util.List;

/**
 * Tripad Extension mod plugin.
 * Initializes the button framework, loads settings, registers built-in buttons,
 * and registers the overlay.
 */
public class TripadModPlugin extends BaseModPlugin {

    private static final Logger log = Logger.getLogger(TripadModPlugin.class);

    @Override
    public void onApplicationLoad() throws Exception {
        log.info("Tripad Extension v1.0.0 - Floating button framework loaded");
    }

    @Override
    public void onGameLoad(boolean newGame) {
        log.info("Tripad Extension: onGameLoad called, newGame=" + newGame);
        TripadManager.reset();
        TripadSettings.loadSettings();

        TripadManager mgr = TripadManager.getInstance();

        if (TripadSettings.enableNexusUI) {
            registerNexusUIButton(mgr);
        }
        if (TripadSettings.enableOperatives) {
            registerOperativesButton(mgr);
        }

        mgr.loadPositions();

        TripadOverlay overlay = new TripadOverlay();
        Global.getSector().getListenerManager().addListener(overlay, true);

        log.info("Tripad Extension: " + mgr.getButtons().size() + " button(s) registered");
    }

    /**
     * NexusUI button: opens the NexusUI dashboard via NexusFrame.toggle().
     * Must be called on the Swing EDT (same as NexusOverlay.openDashboard()).
     */
    private void registerNexusUIButton(TripadManager mgr) {
        if (!Global.getSettings().getModManager().isModEnabled("nexus_ui")) return;

        try {
            TripadButtonSpec spec = new TripadButtonSpec("nexus_ui")
                .setLabel("NexusUI")
                .setTooltip("Open NexusUI Dashboard")
                .setIconColor(new Color(80, 180, 255))
                .setSortOrder(10)
                .setHandler(new TripadClickHandler() {
                    public void onClick() {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                try {
                                    NexusFrame.toggle(NexusModPlugin.DEFAULT_PORT);
                                } catch (Exception e) {
                                    log.error("Tripad: Failed to toggle NexusUI dashboard", e);
                                }
                            }
                        });
                    }
                });
            mgr.registerButton(spec);
            log.info("Tripad: NexusUI button registered");
        } catch (Exception e) {
            log.warn("Tripad: Failed to register NexusUI button", e);
        }
    }

    /**
     * Operatives button: opens the Intel tab and selects the first agent intel,
     * which takes the player directly to the Nexerelin operatives panel.
     */
    private void registerOperativesButton(TripadManager mgr) {
        if (!Global.getSettings().getModManager().isModEnabled("nexerelin")) return;

        try {
            TripadButtonSpec spec = new TripadButtonSpec("nex_operatives")
                .setLabel("Operatives")
                .setTooltip("Open Operatives Panel")
                .setIconColor(new Color(255, 160, 60))
                .setSortOrder(20)
                .setHandler(new TripadClickHandler() {
                    public void onClick() {
                        try {
                            Class<?> agentIntelClass = Class.forName(
                                "exerelin.campaign.intel.agents.AgentIntel");
                            List<IntelInfoPlugin> agents = Global.getSector()
                                .getIntelManager().getIntel(agentIntelClass);

                            if (agents != null && !agents.isEmpty()) {
                                Global.getSector().getCampaignUI().showCoreUITab(
                                    CoreUITabId.INTEL, agents.get(0));
                            } else {
                                Global.getSector().getCampaignUI().showCoreUITab(
                                    CoreUITabId.INTEL);
                            }
                        } catch (Exception e) {
                            try {
                                Global.getSector().getCampaignUI().showCoreUITab(
                                    CoreUITabId.INTEL);
                            } catch (Exception e2) {
                                log.error("Tripad: Failed to open Intel tab", e2);
                            }
                        }
                    }
                });
            mgr.registerButton(spec);
            log.info("Tripad: Operatives button registered");
        } catch (Exception e) {
            log.warn("Tripad: Failed to register Operatives button", e);
        }
    }
}
