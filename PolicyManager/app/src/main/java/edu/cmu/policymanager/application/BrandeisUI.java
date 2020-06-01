package edu.cmu.policymanager.application;

import android.content.Context;

import edu.cmu.policymanager.ui.BrandeisIconManager;
import edu.cmu.policymanager.ui.common.UIPlugin;

/**
 * Created by Mike Czapik (Carnegie Mellon University) on 12/18/2018.
 *
 * This is the code representation of the screens for the CMU Brandeis policy manager.
 */

public class BrandeisUI extends PolicyManagerUI {
    public BrandeisUI(Context context,
                      int uiResourceId) {
        super(context, uiResourceId, new BrandeisIconManager(context));
    }

    public Class getConfigureUI() {
        UIPlugin configureUI = new edu.cmu.policymanager.ui.configure.MainActivity();
        return configureUI.getUI();
    }

    public Class getInstallUI() {
        UIPlugin installUI = new edu.cmu.policymanager.ui.configure.ActivityAppSettings();
        return installUI.getUI();
    }

    public Class getRuntimeUI() {
        UIPlugin runtimeUI = new edu.cmu.policymanager.ui.runtime.RuntimeUI();
        return runtimeUI.getUI();
    }
}