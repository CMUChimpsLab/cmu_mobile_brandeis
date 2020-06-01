package edu.cmu.policymanager.peandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import java.util.concurrent.CountDownLatch;

import edu.cmu.policymanager.application.PolicyManagerApplication;
import edu.cmu.policymanager.application.UIManager;

/**
 * PE Android utility functions.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class PEAndroid {
    private static PEAndroid peandroid = new PEAndroid();
    private static final String PEANDROID_PERM_KEY = "allowPerm";
    private ResultReceiver recv;

    public static final String NOTIFY_UI_COMPLETE = "edu.cmu.policymanager.uiActionComplete";

    public static boolean sUserPromptIsOpen = false;

    /**
     * Set the ResultReceiver from a dangerous permission request.
     *
     * @param recv ResultReceiver passed via onDangerousPermissionRequest
     * @return instance of PEAndroid class
     * */
    public static PEAndroid connectToReceiver(ResultReceiver recv) {
        peandroid.recv = recv;
        return peandroid;
    }

    /**
     * Sends an 'allow' message back to PE Android via the ResultReceiver.
     * */
    public void allowPermission() {
        if(recv != null) {
            Bundle b = new Bundle();
            b.putBoolean(PEANDROID_PERM_KEY, true);
            recv.send(0, b);
        }
    }

    /**
     * Sens a 'deny' message back to PE Android via the ResultReceiver.
     * */
    public void denyPermission() {
        if(recv != null) {
            Bundle b = new Bundle();
            b.putBoolean(PEANDROID_PERM_KEY, false);
            recv.send(0, b);
        }
    }

    public static void returnControl(Context context) {
        Intent controlMessage = new Intent(NOTIFY_UI_COMPLETE);
        context.sendBroadcast(controlMessage);
        sUserPromptIsOpen = false;
    }

    public static void awaitUserInput(Context context) {
            final CountDownLatch latch = new CountDownLatch(1);

            BroadcastReceiver userInputListener = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    latch.countDown();
                }
            };

            context.registerReceiver(userInputListener, new IntentFilter(NOTIFY_UI_COMPLETE));

            try {
                latch.await();
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }

            context.unregisterReceiver(userInputListener);
    }
}