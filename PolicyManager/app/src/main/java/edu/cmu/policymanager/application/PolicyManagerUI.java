package edu.cmu.policymanager.application;

import android.content.Context;

import edu.cmu.policymanager.ui.common.IconManager;

/**
 * Created by Mike Czapik (Carnegie Mellon University) on 8/20/2018.
 *
 * Represents one of the UIs the Policy Manager may use. Implement
 * this class for each subpackage of the ui package, and pass an
 * instance of it to the PolicyManagerApplication's ui manager object.
 *
 * This allows the policy manager to easily manage and switch between
 * different UIs you may implement.
 */

public abstract class PolicyManagerUI {
    public final int id;
    private final IconManager iconManager;

    public PolicyManagerUI(final Context context,
                           final int uiResourceId,
                           final IconManager iconManager) {
        id = uiResourceId;
        this.iconManager = iconManager;
    }

    public abstract Class getConfigureUI();
    public abstract Class getInstallUI();
    public abstract Class getRuntimeUI();
    public IconManager getIconManager() { return iconManager; }
}