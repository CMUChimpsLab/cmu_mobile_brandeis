package edu.cmu.policymanager.ui.configure.cards.globalsetting;

import android.content.Context;
import android.content.Intent;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.policymanager.PolicyManager.sensitivedata.DangerousPermissions;
import edu.cmu.policymanager.PolicyManager.sensitivedata.SensitiveData;
import edu.cmu.policymanager.PolicyManager.sensitivedata.SensitiveDataGroup;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.ui.configure.globalsettings.ActivityGlobalConfigurePurpose;
import edu.cmu.policymanager.validation.Precondition;

import static edu.cmu.policymanager.ui.configure.cards.globalsetting.GroupCardPermissionOption.DISPLAY_PERMISSION_KEY;
import static edu.cmu.policymanager.ui.configure.cards.globalsetting.GroupCardPermissionOption.PERMISSION_KEY;

/**
 * Groups related permissions into a card that, upon expansion, presents the user
 * with navigation options to configure a specific permission for all apps.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class PermissionGroupCard {
    private final Context mContext;

    private boolean mIsExpanded = false;

    private ViewGroup mPermissionContainer;
    private View mDivider;
    private ImageView mDownButton, mUpButton, mNavigateButton;

    private static Map<String, Integer> mPermissionIcons = new HashMap<String, Integer>();

    static {
            mPermissionIcons.put(
                    DangerousPermissions.CALENDAR_GROUP.groupName.toString(),
                    R.drawable.ic_permission_calendar
            );
            mPermissionIcons.put(
                    DangerousPermissions.CALL_LOG_GROUP.groupName.toString(),
                    R.drawable.ic_permission_phone
            );
            mPermissionIcons.put(
                    DangerousPermissions.CAMERA_GROUP.groupName.toString(),
                    R.drawable.ic_permission_camera
            );
            mPermissionIcons.put(
                    DangerousPermissions.CONTACTS_GROUP.groupName.toString(),
                    R.drawable.ic_permission_contacts
            );
            mPermissionIcons.put(
                    DangerousPermissions.LOCATION_GROUP.groupName.toString(),
                    R.drawable.ic_permission_location
            );
            mPermissionIcons.put(
                    DangerousPermissions.MICROPHONE_GROUP.groupName.toString(),
                    R.drawable.ic_permission_audio
            );
            mPermissionIcons.put(
                    DangerousPermissions.SENSOR_GROUP.groupName.toString(),
                    R.drawable.ic_permission_body_sensors
            );
            mPermissionIcons.put(
                    DangerousPermissions.SMS_GROUP.groupName.toString(),
                    R.drawable.ic_permission_sms
            );
            mPermissionIcons.put(
                    DangerousPermissions.STORAGE_GROUP.groupName.toString(),
                    R.drawable.ic_permission_storage
            );
            mPermissionIcons.put(
                    DangerousPermissions.PHONE_GROUP.groupName.toString(),
                    R.drawable.ic_permission_phone
            );
    }

    private PermissionGroupCard(Builder config) {
        mContext = config.mContext;

        View card = LayoutInflater.from(mContext)
                                  .inflate(R.layout.template_global_setting_card,
                                           config.mContainer,
                                          false);

        TextView permissionGroupName = card.findViewById(R.id.global_setting_permission_group_name);
        permissionGroupName.setText(config.mPermissionGroup.groupName);

        ImageView cardIcon = card.findViewById(R.id.global_setting_group_icon);
        cardIcon.setImageResource(mPermissionIcons.get(config.mPermissionGroup.groupName));

        mPermissionContainer = card.findViewById(R.id.global_setting_permission_container);

        mDownButton = card.findViewById(R.id.global_setting_dropdown);
        mUpButton = card.findViewById(R.id.global_setting_upbutton);
        mNavigateButton = card.findViewById(R.id.global_setting_navbutton);
        mDivider = card.findViewById(R.id.global_setting_card_divider);

        final List<SensitiveData> groupPermissions = config.mPermissionGroup.permissionsInGroup;

        if(groupPermissions.size() == 1) {
            SensitiveData permission = groupPermissions.get(0);
            card.setOnClickListener(navigateToPurposeControlScreen(permission));
            mDownButton.setVisibility(View.GONE);
            mNavigateButton.setVisibility(View.VISIBLE);
        } else {
            for (int i = 0; i < groupPermissions.size(); i++) {
                SensitiveData permission = groupPermissions.get(i);

                GroupCardPermissionOption.builder(config.mContext)
                                         .setPermission(permission)
                                         .attachTo(mPermissionContainer);

                if (i < groupPermissions.size() - 1) {
                    mPermissionContainer.addView(createDivider(config.mContext));
                }
            }

            card.setOnClickListener(handlePressExpand());
        }

        config.mContainer.addView(card);
    }

    private View createDivider(Context context) {
        ContextThemeWrapper theme = new ContextThemeWrapper(context, R.style.divider);
        View divider = new View(theme, null, 0);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                2
        );

        params.setMargins(275, 25, 25, 5);
        divider.setLayoutParams(params);

        return divider;
    }

    private View.OnClickListener navigateToPurposeControlScreen(final SensitiveData permission) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent purposeConfigActivity =
                        new Intent(mContext, ActivityGlobalConfigurePurpose.class);

                purposeConfigActivity.putExtra(PERMISSION_KEY, permission);
                purposeConfigActivity.putExtra(
                        DISPLAY_PERMISSION_KEY,
                        permission.getDisplayPermission()
                );

                mContext.startActivity(purposeConfigActivity);
            }
        };
    }

    private View.OnClickListener handlePressExpand() {
        return new View.OnClickListener() {
            public void onClick(View v) {
                expandCard();
            }
        };
    }

    /**
     * Expose navigation options to configure specific permissions when the card
     * is expanded, and hide the options when collapsed.
     * */
    public void expandCard() {
        if(mIsExpanded) {
            mPermissionContainer.setVisibility(View.GONE);
            mDownButton.setVisibility(View.VISIBLE);
            mUpButton.setVisibility(View.GONE);
            mDivider.setVisibility(View.GONE);
        }
        else {
            mPermissionContainer.setVisibility(View.VISIBLE);
            mDownButton.setVisibility(View.GONE);
            mUpButton.setVisibility(View.VISIBLE);
            mDivider.setVisibility(View.VISIBLE);
        }

        mIsExpanded = !mIsExpanded;
    }

    /**
     * Creates a Builder instance for this component.
     *
     * @param context the context
     * @return the Builder instance
     * */
    public static Builder builder(Context context) {
        Precondition.checkIfNull(context, "Cannot create builder from null context");
        return new Builder(context);
    }

    /**
     * Builder class for PermissionGroupCards.
     * */
    public static class Builder {
        private final Context mContext;
        private SensitiveDataGroup mPermissionGroup;
        private ViewGroup mContainer;

        private static final String sErrorInvalidPermissionGroup =
                "Invalid permission group value. Should be one of " +
                mPermissionIcons.keySet().toString() + ".";

        private Builder(final Context context) {
            mContext = context;
        }

        /**
         * Set the permission group name that this card groups permissions by.
         *
         * @param permissionGroup the name of the permission group
         * @return this Builder instance
         * */
        public Builder setPermissionGroup(SensitiveDataGroup permissionGroup) {
            Precondition.checkIfNull(permissionGroup, "permissionGroup cannot be null");

            if(!mPermissionIcons.containsKey(permissionGroup.groupName.toString())) {
                throw new IllegalArgumentException(
                        sErrorInvalidPermissionGroup + " Value is " + permissionGroup
                );
            }

            mPermissionGroup = permissionGroup;
            return this;
        }

        /**
         * Attach the card to the specified container.
         *
         * @param container the container receiving this card
         * @return the PermissionGroupCard instance this Builder created
         * */
        public PermissionGroupCard attachTo(ViewGroup container) {
            Precondition.checkIfNull(container, "Cannot attach to null container");
            Precondition.checkState(mPermissionGroup != null, "Must have group");

            mContainer = container;
            return new PermissionGroupCard(this);
        }
    }
}