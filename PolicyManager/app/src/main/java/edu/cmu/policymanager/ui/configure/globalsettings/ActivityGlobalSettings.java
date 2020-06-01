package edu.cmu.policymanager.ui.configure.globalsettings;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import java.util.LinkedList;
import java.util.List;

import edu.cmu.policymanager.PolicyManager.sensitivedata.DangerousPermissions;
import edu.cmu.policymanager.PolicyManager.sensitivedata.SensitiveDataGroup;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.ui.configure.cards.globalsetting.PermissionGroupCard;

/**
 * Presents the user with a set of permission groups that when expanded,
 * will show a list of individual permissions. Pressing on an individual
 * permission will navigate to another screen where the user can configure
 * permission settings for all apps and/or purposes.
 *
 * Created by Mike Czapik (Carnegie Mellon University)
 * */
public class ActivityGlobalSettings extends Activity {
    private final Context mActivityContext = this;
    private LinearLayout mSettingContainer;

    public static final String KEY_SELECTED_GROUP = "selectedGroup";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_settings);

        mSettingContainer = findViewById(R.id.activity_global_setting_container);
    }

    @Override
    public void onStart() {
        super.onStart();

        SensitiveDataGroup selectedGroup = getIntent().getParcelableExtra(KEY_SELECTED_GROUP);

        if(selectedGroup != null) {
            PermissionGroupCard group = PermissionGroupCard.builder(mActivityContext)
                                                           .setPermissionGroup(selectedGroup)
                                                           .attachTo(mSettingContainer);

            if(selectedGroup.permissionsInGroup.size() > 1) {
                group.expandCard();
            }
        }

        List<SensitiveDataGroup> groupsWithMoreThanOnePermission =
                new LinkedList<SensitiveDataGroup>();
        List<SensitiveDataGroup> groupsWithOnlyOnePermission =
                new LinkedList<SensitiveDataGroup>();

        for(SensitiveDataGroup sdg : DangerousPermissions.ALL_SENSITIVE_DATA_GROUPS) {
            if(sdg.permissionsInGroup.size() > 1) {
                groupsWithMoreThanOnePermission.add(sdg);
            } else {
                groupsWithOnlyOnePermission.add(sdg);
            }
        }

        //Add cards that can expand, first; otherwise, the visualization will look weird...
        for(SensitiveDataGroup dataGroup : groupsWithMoreThanOnePermission) {
            if(dataGroupWasNotSelectedViaHomescreen(dataGroup, selectedGroup)) {
                addCard(dataGroup);
            }
        }

        for(SensitiveDataGroup dataGroup : groupsWithOnlyOnePermission) {
            if(dataGroupWasNotSelectedViaHomescreen(dataGroup, selectedGroup)) {
                addCard(dataGroup);
            }
        }
    }

    @Override
    public void onResume() { super.onResume(); }

    @Override
    public void onPause() { super.onPause(); }

    @Override
    public void onStop() { super.onStop(); }

    @Override
    public void onDestroy() { super.onDestroy(); }

    private boolean dataGroupWasNotSelectedViaHomescreen(SensitiveDataGroup currentGroup,
                                                         SensitiveDataGroup selected) {
        return selected == null || !selected.equals(currentGroup);
    }

    private PermissionGroupCard addCard(SensitiveDataGroup dataGroup) {
        return PermissionGroupCard.builder(mActivityContext)
                                  .setPermissionGroup(dataGroup)
                                  .attachTo(mSettingContainer);
    }

    public void goBack(View v) {
        finish();
    }
}