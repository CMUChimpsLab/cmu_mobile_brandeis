package edu.cmu.policymanager.PolicyManager;

import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Keeps track of and determines if packages belong to critical or important
 * Android apps. These apps are always granted access to sensitive data, and
 * we do not store any related models in the database. They should never trigger
 * the Install UI, or be visible anywhere in the policy manager.
 *
 * Some critical apps based on: https://support.google.com/a/answer/7292363?hl=en
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class CriticalSystemApps {
    public static final Set<String> set = new HashSet<String>();

    public static final String GOOGLE_PLAY = "com.android.vending";

    public static boolean packageIsSystemApp(final String packageName) {
        for(String systemApp : set) {
            if(packageName.contains(systemApp)) { return true; }
        }

        if(packageName.equalsIgnoreCase("android")) {
            return true;
        }

        return false;
    }

    public static boolean packageIsGoogleAPI(final String packageName) {
        return packageName.equalsIgnoreCase("com.google.android.gms") ||
               packageName.equalsIgnoreCase("com.google.android.gsf");
    }
    
    static {
        set.add("com.android.bluetooth");
        set.add("com.android.contacts");
        set.add("com.android.keychain");
        set.add("com.android.keyguard");
        set.add("com.android.launcher");
        set.add("com.android.traceur");
        set.add("com.android.nfc");
        set.add("com.android.phone");
        set.add("com.android.providers");
        set.add("com.android.deskclock");
        set.add("com.android.location.fused");
        set.add("com.android.settings");
        set.add("com.android.systemui");
        set.add("com.android.vending");
        set.add("com.google.android.apps.enterprise.dmagent");
        set.add("com.google.android.deskclock");
        set.add("com.google.android.dialer");
        set.add("com.google.android.gms");
        set.add("me.twrp.twrpapp");
        set.add("com.google.android.googlequicksearchbox");
        set.add("com.google.android.gsf");
        set.add("com.android.backupconfirm");
        set.add("com.android.mtp");
        set.add("com.android.calendar");
        set.add("com.google.android.feedback");
        set.add("com.google.android.syncadapters.calendar");
        set.add("com.google.android.backuptransport");
        set.add("com.android.camera2");
        set.add("com.google.android.apps.restore");
        set.add("com.google.android.tts");
        set.add("com.google.android.packageinstaller");
        set.add("com.google.android.syncadapters.contacts");
        set.add("com.google.android.configupdater");
        set.add("com.google.android.soundpicker");
        set.add("com.google.android.onetimeinitializer");
        set.add("com.android.wallpaperbackup");
        set.add("com.android.calculator2");
        set.add("com.android.cts.ctsshim");
        set.add("com.android.wallpaper.livepicker");
        set.add("com.android.smspush");
        set.add("com.android.proxyhandler");
        set.add("com.android.calllogbackup");
        set.add("com.android.inputdevices");
        set.add("com.android.webview");
        set.add("com.android.se");
        set.add("com.android.egg");
        set.add("com.android.carrierconfig");
        set.add("com.android.internal.display.cutout.emulation.tall");
        set.add("com.android.simappdialog");
        set.add("com.android.pacprocessor");
        set.add("com.google.ar.core");
        set.add("com.google.pixel.wahoo.gfxdrv");
        set.add("com.google.android.theme.pixel");
        set.add("android.auto_generated_rro__");
        set.add("com.android.wallpapercropper");
        set.add("com.google.android.ext.shared");
        set.add("com.android.internal.display.cutout.emulation.double");
        set.add("com.google.android.carriersetup");
        set.add("com.google.android.ext.services");
        set.add("com.android.internal.display.cutout.emulation.corner");
        set.add("com.android.cts.priv.ctsshim");
        set.add("com.google.android.partnersetup");
        set.add("com.android.captiveportallogin");
        set.add("com.qualcomm.timeservice");
        set.add("com.android.provision");
        set.add("com.android.statementservice");
        set.add("com.android.server.telecom");
        set.add("com.android.bips");
        set.add("com.android.printspooler");
        set.add("com.android.sharedstoragebackup");
        set.add("com.android.cellbroadcastreceiver");
        set.add("com.android.musicfx");
        set.add("com.android.sdksetup");
        set.add("com.android.emergency");
        set.add("com.android.vpndialogs");
        set.add("com.android.gallery3d");
        set.add("com.android.documentsui");
        set.add("com.android.externalstorage");
        set.add("com.android.htmlviewer");
        set.add("com.android.companiondevicemanager");
        set.add("com.android.quicksearchbox");
        set.add("com.android.mms.service");
        set.add("com.android.messaging");
        set.add("com.android.defcontainer");
        set.add("com.android.certinstaller");
        set.add("com.android.printservice.recommendation");
        set.add("com.android.inputmethod.latin");
        set.add("com.android.packageinstaller");
        set.add("com.android.carrierdefaultapp");
        set.add("com.android.storagemanager");
        set.add("com.android.managedprovisioning");
        set.add("com.android.dreams.phototable");
        set.add("com.android.shell");
        set.add("com.android.music");
        set.add("com.android.email");
        set.add("com.google.android.inputmethod.latin");
        set.add("com.google.android.nfcprovision");
        set.add("com.google.android.setupwizard");
        set.add("com.samsung.android.contacts");
        set.add("com.samsung.android.phone");

        //Others I have noticed and added myself
        set.add("com.android.dialer");
        set.add("com.twosix");
        set.add("edu.cmu.policymanager_new");
        set.add("org.chromium");
    }
}