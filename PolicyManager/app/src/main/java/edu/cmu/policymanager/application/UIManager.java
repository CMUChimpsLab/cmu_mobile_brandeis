package edu.cmu.policymanager.application;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import edu.cmu.policymanager.PolicyManager.enforcement.PermissionRequest;
import edu.cmu.policymanager.peandroid.PEAndroid;
import edu.cmu.policymanager.ui.common.ConfigureSwitch;
import edu.cmu.policymanager.ui.common.IconManager;
import edu.cmu.policymanager.ui.common.UIPlugin;

import static edu.cmu.policymanager.ui.runtime.RuntimeUI.INTENT_KEY_PERMISSION_REQUEST;

/**
 * Manages the current UI implementation, and allows for UIs to be swapped or launched
 * on-demand. The original case was to support PE for Android and Protect my Privacy on
 * the same device, but switch back-and-forth between them.
 *
 * It does work, and could be useful for testing between different implementations of
 * the policy manager.
 *
 * Created by Mike Czapik (Carnegie Mellon University) on 8/20/2018.
 */

public final class UIManager {
    private final Map<Integer, PolicyManagerUI> uis = new HashMap<Integer, PolicyManagerUI>();
    private Class installUI = null;
    private Class runtimeUI = null;
    private IconManager iconManager = null;
    private static Bundle runtimeData = new Bundle();
    private static Queue<Bundle> mRuntimeQueue = new LinkedBlockingQueue<>();
    private Context mContext;
    private Set<String> trackedDialogs = new HashSet<String>();

    public void setContext(Context context) {
        mContext = context;
    }

    public void setDefaultUI(PolicyManagerUI ui) {
        installUI = ui.getInstallUI();
        runtimeUI = ui.getRuntimeUI();
        iconManager = ui.getIconManager();

        add(ui);
    }

    public void add(PolicyManagerUI ui) {
        uis.put(ui.id, ui);
    }

    /**
     * Change to a different policy manager.
     *
     * @param context the Context
     * @param uiId the identifier for another UI you want to switch to
     * */
    public void switchToUI(Context context,
                           int uiId) {
        if(context != null) {
            PolicyManagerUI currentUI = uis.get(uiId);
            installUI = currentUI.getInstallUI();
            runtimeUI = currentUI.getRuntimeUI();
            iconManager = currentUI.getIconManager();

            Intent activitySwitch = new Intent(context, currentUI.getConfigureUI());
            activitySwitch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activitySwitch);
        }
    }

    /**
     * Launches the install UI
     *
     * @param context the Context
     * */
    public void launchInstallUI(Context context) {
        if(context != null) {
            Intent installLauncher = new Intent(context, installUI);
            installLauncher.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(installLauncher);
        }
    }

    /**
     * Launches the runtime UI
     *
     * @param context the Context
     * */
    public void launchRuntimeUI(Context context) {
        if(context != null) {
            String key = getKeyFromRequestBundle(runtimeData);
/*
            PermissionRequest req = runtimeData.getParcelable(INTENT_KEY_PERMISSION_REQUEST);
            PEAndroid.connectToReceiver(req.recv).denyPermission();
            Log.d("dbg", "Policy manager denied " + req.permission.androidPermission
                                   + " to " + req.packageName);*/

            Intent runtimeLauncher = new Intent(context, runtimeUI);
            runtimeLauncher.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(runtimeLauncher);
            PEAndroid.awaitUserInput(context);
            //runNextRuntimeUI();
        }
    }

    public void runNextRuntimeUI() {
        if(mContext != null) {
            if(!mRuntimeQueue.isEmpty()) {
                setRuntimeData(mRuntimeQueue.remove());
                launchRuntimeUI(mContext);
            }
        }
    }

    private String getKeyFromRequestBundle(Bundle requestBundle) {
        PermissionRequest request = requestBundle.getParcelable(INTENT_KEY_PERMISSION_REQUEST);

        if(request != null) {
            return request.packageName + request.permission.androidPermission +
                   request.purpose.name;
        }

        return "";
    }

    public Class getInstallUI() { return installUI; }
    public Class getRuntimeUI() { return runtimeUI; }

    /**
     * Get the IconManager for the currently selected policy manager UI
     *
     * @return the IconManager
     * */
    public IconManager getIconManager() { return iconManager; }

    public void setRuntimeData(Bundle runtimeData) {
        if(!PEAndroid.sUserPromptIsOpen) {
            UIManager.runtimeData = runtimeData;
        } else {
            mRuntimeQueue.add(runtimeData);
        }
    }

    public Bundle getRuntimeData() { return runtimeData; }
}