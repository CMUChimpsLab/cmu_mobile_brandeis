package edu.cmu.policymanager.ui;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibrary;
import edu.cmu.policymanager.PolicyManager.purposes.Purpose;
import edu.cmu.policymanager.PolicyManager.purposes.Purposes;
import edu.cmu.policymanager.PolicyManager.sensitivedata.SensitiveData;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.application.PolicyManagerApplication;
import edu.cmu.policymanager.ui.common.IconManager;
import edu.cmu.policymanager.validation.Precondition;
import edu.cmu.policymanager.viewmodel.W4PIcon;

/**
 * The IconManager for the CMU Brandeis policy manager.
 *
 * Created by Mike Czapik (Carnegie Mellon University) on 12/18/2018.
 */

public class BrandeisIconManager extends IconManager {
    private final PackageManager packageManager;
    private final Context context;

    public BrandeisIconManager(Context context) {
        super(context);
        packageManager = context.getPackageManager();
        this.context = context;
    }

    public W4PIcon getAppIcon(CharSequence packageName) {
        Precondition.checkEmptyCharSequence(packageName);

        try {
            ApplicationInfo info = packageManager.getApplicationInfo(packageName.toString(),
                    PackageManager.GET_META_DATA);
            if (info != null) {
                Drawable d = info.loadIcon(packageManager);
                return W4PIcon.createDrawableIcon(drawableToBitmap(d));
            }
        } catch (PackageManager.NameNotFoundException e) {
            //name not found exception is common with some system apps
        }

        return W4PIcon.createDrawableIcon(new BitmapDrawable(context.getResources(),
                BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_pmp)));
    }

    public W4PIcon getThirdPartyLibraryIcon(ThirdPartyLibrary library) {
        return W4PIcon.createStaticIcon(library.iconResource);
    }

    public W4PIcon getPermissionIcon(SensitiveData permission) {
        Precondition.checkIfNull(permission, "Cannot get null permission icon");

        int iconId = R.drawable.ic_help_white_24dp;

        String permissionName = permission.androidPermission.toString();
        if(permissionName == null) { return W4PIcon.createStaticIcon(iconId); }

        String caseInsenitiveName = permissionName.toUpperCase();

        if(caseInsenitiveName.contains("CAMERA")) {
            iconId = R.drawable.ic_permission_camera;
        } else if(caseInsenitiveName.contains("LOCATION")) {
            iconId = R.drawable.ic_permission_location;
        } else if(caseInsenitiveName.contains("SENSORS")) {
            iconId = R.drawable.ic_permission_body_sensors;
        } else if(caseInsenitiveName.contains("SMS") ||
                  caseInsenitiveName.contains("WAP")) {
            iconId = R.drawable.ic_permission_sms;
        } else if(caseInsenitiveName.contains("CONTACTS") ||
                  caseInsenitiveName.contains("ACCOUNTS")) {
            iconId = R.drawable.ic_permission_contacts;
        } else if(caseInsenitiveName.contains("STORAGE")) {
            iconId = R.drawable.ic_permission_storage;
        } else if(caseInsenitiveName.contains("AUDIO")) {
            iconId = R.drawable.ic_permission_audio;
        } else if(caseInsenitiveName.contains("DEVICE") ||
                  caseInsenitiveName.contains("PHONE")) {
            iconId = R.drawable.ic_permission_phone_state;
        } else if(caseInsenitiveName.contains("CALL") ||
                  caseInsenitiveName.contains("VOICEMAIL") ||
                  caseInsenitiveName.contains("PROCESS_OUTGOING_CALLS") ||
                  caseInsenitiveName.contains("SIP")) {
            iconId = R.drawable.ic_permission_phone;
        } else if(caseInsenitiveName.contains("CALENDAR")) {
            iconId = R.drawable.ic_permission_calendar;
        } else if(caseInsenitiveName.contains(PolicyManagerApplication.SYMBOL_ALL)) {
            iconId = R.drawable.ic_all;
        } else {
            iconId = R.drawable.arlington_help;
        }

        return W4PIcon.createStaticIcon(iconId);
    }

    public W4PIcon getPurposeIcon(Purpose purpose) {
        Precondition.checkIfNull(purpose, "Cannot get null purpose icon");

        int iconId = R.drawable.arlington_purpose_ic_help;
        String purposeName = purpose.name.toString();

        if(purposeName.equalsIgnoreCase(Purposes.DELIVERING_LOCAL_WEATHER.toString())) {
            iconId = R.drawable.ic_purpose_weather;
        } else if(purposeName.equalsIgnoreCase(Purposes.PLAYING_GAMES.toString())) {
            iconId = R.drawable.ic_purpose_games;
        } else if(purposeName.equalsIgnoreCase(Purposes.CONNECT_WITH_OTHER_PEOPLE.toString())) {
            iconId = R.drawable.ic_purpose_social;
        } else if(purposeName.equalsIgnoreCase(Purposes.MONITOR_HEALTH.toString())) {
            iconId = R.drawable.ic_purpose_monitor_health;
        } else if(purposeName.equalsIgnoreCase(Purposes.NOTIFY_EMERGENCY_SERVICES.toString())) {
            iconId = R.drawable.ic_purpose_emergency;
        } else if(purposeName.equalsIgnoreCase(Purposes.ANALYZE_USER_INFO.toString())) {
            iconId = R.drawable.ic_purpose_analytics;
        } else if(purposeName.equalsIgnoreCase(Purposes.ADD_LOCATION_TO_PHOTO.toString())) {
            iconId = R.drawable.ic_purpose_geotagging;
        } else if(purposeName.equalsIgnoreCase(Purposes.SEARCH_NEARBY_PLACES.toString())) {
            iconId = R.drawable.ic_purpose_nearby_places;
        } else if(purposeName.equalsIgnoreCase(Purposes.BACKUP_TO_CLOUD_SERVICE.toString())) {
            iconId = R.drawable.ic_purpose_backup_cloud;
        } else if(purposeName.equalsIgnoreCase(Purposes.MESSAGING_OR_CALLING_PEOPLE.toString())) {
            iconId = R.drawable.ic_purpose_messaging_or_calling;
        } else if(purposeName.equalsIgnoreCase(Purposes.NAVIGATING_TO_DESTINATION.toString())) {
            iconId = R.drawable.ic_purpose_navigation;
        } else if(purposeName.equalsIgnoreCase(Purposes.DISPLAY_ADVERTISEMENT.toString())) {
            iconId = R.drawable.ic_purpose_ads;
        } else if(purposeName.equalsIgnoreCase(Purposes.SECURING_DEVICE.toString())) {
            iconId = R.drawable.ic_purpose_secure_device;
        } else if(purposeName.equalsIgnoreCase(Purposes.CONDUCTING_RESEARCH.toString())) {
            iconId = R.drawable.ic_purpose_research;
        } else if(purposeName.equalsIgnoreCase(Purposes.RUNNING_OTHER_FEATURES.toString())) {
            iconId = R.drawable.ic_purpose_running_other_features;
        } else if(purposeName.equalsIgnoreCase("All Other Purposes")) {
            iconId = R.drawable.ic_all;
        } else if(purposeName.equalsIgnoreCase(PolicyManagerApplication.SYMBOL_ALL)) {
            iconId = R.drawable.ic_all;
        }

        return W4PIcon.createStaticIcon(iconId);
    }
}