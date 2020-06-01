package edu.cmu.policymanager.ui.configure.cards.homescreen;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import edu.cmu.policymanager.PolicyManager.sensitivedata.DangerousPermissions;
import edu.cmu.policymanager.PolicyManager.sensitivedata.SensitiveDataGroup;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.ui.configure.globalsettings.ActivityGlobalSettings;

/**
 * Contains factory methods to create tiles for global settings of individual permissions. Used
 * on the homescreen.
 *
 * Created by Mike Czapik (Carnegie Mellon University) on 12/26/2018.
 */

public class GlobalSettingCategoryCard {
    private final Context mContext;
    private final View mCard;
    private SensitiveDataGroup mPermissionGroup;

    private GlobalSettingCategoryCard(final Context context,
                                      final ViewGroup parent) {
        mContext = context;

        mCard = LayoutInflater.from(context)
                              .inflate(
                                     R.layout.component_card_global_setting_homescreen,
                                     parent,
                                     false
                             );

        parent.addView(mCard);
    }

    private void setAsLocation() {
        mPermissionGroup = DangerousPermissions.LOCATION_GROUP;

        int id = getImageResource("location");
        configure("Location", id);
    }

    private void setAsContacts() {
        mPermissionGroup = DangerousPermissions.CONTACTS_GROUP;

        int id = getImageResource("contacts");
        configure("Contacts", id);
    }

    private void setAsCalendar() {
        mPermissionGroup = DangerousPermissions.CALENDAR_GROUP;

        int id = getImageResource("calendar");
        configure("Calendar", id);
    }

    private void setAsMicrophone() {
        mPermissionGroup = DangerousPermissions.MICROPHONE_GROUP;

        int id = getImageResource("microphone");
        configure("Microphone", id);
    }

    private void setAsCamera() {
        mPermissionGroup = DangerousPermissions.CAMERA_GROUP;

        int id = getImageResource("camera");
        configure("Camera", id);
    }

    private void setAsCallLogs() {
        mPermissionGroup = DangerousPermissions.CALL_LOG_GROUP;

        int id = getImageResource("call_logs");
        configure("Call Logs", id);
    }

    private void setAsStorage() {
        mPermissionGroup = DangerousPermissions.STORAGE_GROUP;

        int id = getImageResource("storage");
        configure("Storage", id);
    }

    private void setAsSensor() {
        mPermissionGroup = DangerousPermissions.SENSOR_GROUP;

        int id = getImageResource("sensor");
        configure("Sensors", id);
    }

    private void setAsSMS() {
        mPermissionGroup = DangerousPermissions.SMS_GROUP;

        int id = getImageResource("sms");
        configure("SMS", id);
    }

    private int getImageResource(final String name) {
        return mContext.getResources().getIdentifier(name, "drawable", mContext.getPackageName());
    }

    private void configure(final String displayPermission,
                           final int iconId) {
        TextView title = mCard.findViewById(R.id.allysiqi_permission_card_title);
        title.setText(displayPermission);

        ImageView icon = mCard.findViewById(R.id.allysiqi_permission_card_icon);
        icon.setImageResource(iconId);

        mCard.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent globalSettingActivity = new Intent(mContext, ActivityGlobalSettings.class);
                globalSettingActivity.putExtra(
                        ActivityGlobalSettings.KEY_SELECTED_GROUP,
                        mPermissionGroup
                );

                mContext.startActivity(globalSettingActivity);
            }
        });
    }

    /**
     * Creates a small tile for the homescreen that will navigate the user to an expanded
     * location card in global settings when tapped.
     *
     * @param context context
     * @param parent the parent ViewGroup to attach this tile to
     * @return this tile as a View
     * */
    public static View createLocation(final Context context,
                                      final ViewGroup parent) {
        GlobalSettingCategoryCard c = new GlobalSettingCategoryCard(context, parent);
        c.setAsLocation();
        return c.mCard;
    }

    /**
     * Creates a small tile for the homescreen that will navigate the user to an expanded
     * contacts card in global settings when tapped.
     *
     * @param context context
     * @param parent the parent ViewGroup to attach this tile to
     * @return this tile as a View
     * */
    public static View createContacts(final Context context,
                                      final ViewGroup parent) {
        GlobalSettingCategoryCard c = new GlobalSettingCategoryCard(context, parent);
        c.setAsContacts();
        return c.mCard;
    }

    /**
     * Creates a small tile for the homescreen that will navigate the user to a microphone
     * card in global settings when tapped.
     *
     * @param context context
     * @param parent the parent ViewGroup to attach this tile to
     * @return this tile as a View
     * */
    public static View createMicrophone(final Context context,
                                        final ViewGroup parent) {
        GlobalSettingCategoryCard c = new GlobalSettingCategoryCard(context, parent);
        c.setAsMicrophone();
        return c.mCard;
    }

    /**
     * Creates a small tile for the homescreen that will navigate the user to a camera
     * card in global settings when tapped.
     *
     * @param context context
     * @param parent the parent ViewGroup to attach this tile to
     * @return this tile as a View
     * */
    public static View createCamera(final Context context,
                                    final ViewGroup parent) {
        GlobalSettingCategoryCard c = new GlobalSettingCategoryCard(context, parent);
        c.setAsCamera();
        return c.mCard;
    }

    /**
     * Creates a small tile for the homescreen that will navigate the user to an expanded
     * calendar card in global settings when tapped.
     *
     * @param context context
     * @param parent the parent ViewGroup to attach this tile to
     * @return this tile as a View
     * */
    public static View createCalendar(final Context context,
                                      final ViewGroup parent) {
        GlobalSettingCategoryCard c = new GlobalSettingCategoryCard(context, parent);
        c.setAsCalendar();
        return c.mCard;
    }

    /**
     * Creates a small tile for the homescreen that will navigate the user to an expanded
     * call logs card in global settings when tapped.
     *
     * @param context context
     * @param parent the parent ViewGroup to attach this tile to
     * @return this tile as a View
     * */
    public static View createCallLogs(final Context context,
                                      final ViewGroup parent) {
        GlobalSettingCategoryCard c = new GlobalSettingCategoryCard(context, parent);
        c.setAsCallLogs();
        return c.mCard;
    }

    /**
     * Creates a small tile for the homescreen that will navigate the user to an expanded
     * storage card in global settings when tapped.
     *
     * @param context context
     * @param parent the parent ViewGroup to attach this tile to
     * @return this tile as a View
     * */
    public static View createStorage(final Context context,
                                     final ViewGroup parent) {
        GlobalSettingCategoryCard c = new GlobalSettingCategoryCard(context, parent);
        c.setAsStorage();
        return c.mCard;
    }

    /**
     * Creates a small tile for the homescreen that will navigate the user to an expanded
     * SMS card in global settings when tapped.
     *
     * @param context context
     * @param parent the parent ViewGroup to attach this tile to
     * @return this tile as a View
     * */
    public static View createSMS(final Context context,
                                 final ViewGroup parent) {
        GlobalSettingCategoryCard c = new GlobalSettingCategoryCard(context, parent);
        c.setAsSMS();
        return c.mCard;
    }

    /**
     * Creates a small tile for the homescreen that will navigate the user to a sensors
     * card in global settings when tapped.
     *
     * @param context context
     * @param parent the parent ViewGroup to attach this tile to
     * @return this tile as a View
     * */
    public static View createSensor(final Context context,
                                    final ViewGroup parent) {
        GlobalSettingCategoryCard c = new GlobalSettingCategoryCard(context, parent);
        c.setAsSensor();
        return c.mCard;
    }
}