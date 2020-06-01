package edu.cmu.policymanager.viewmodel;

import edu.cmu.policymanager.DataRepository.db.model.AppInfo;
import edu.cmu.policymanager.DataRepository.db.model.PermissionInfo;
import edu.cmu.policymanager.DataRepository.db.model.PurposeInfo;
import edu.cmu.policymanager.DataRepository.db.model.ThirdPartyLibInfo;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibraries;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibrary;
import edu.cmu.policymanager.PolicyManager.purposes.Purpose;
import edu.cmu.policymanager.PolicyManager.purposes.Purposes;
import edu.cmu.policymanager.PolicyManager.sensitivedata.DangerousPermissions;
import edu.cmu.policymanager.PolicyManager.sensitivedata.SensitiveData;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.application.PolicyManagerApplication;
import edu.cmu.policymanager.util.PermissionUtil;

/**
 * Created by Mike Czapik (Carnegie Mellon University) on 3/27/2018.
 *
 * W4P means:
 * Who - Who is accessing my sensitive data (App)?
 * What - What sensitive data is being accessed ("Dangerous" Permission)?
 * Why - Why is this sensitive data being accessed (Purpose)?
 * Where - Where is this sensitive data being sent (Company/Third Party Library)?
 *
 * W4PData encapsulates each of these concepts with a single data-type, only
 * exposing what is necessary to do visualization or policy configuration.
 *
 * See Android's documention on permission levels for further information:
 * https://developer.android.com/guide/topics/permissions/overview#normal-dangerous
 */

public final class W4PData implements Comparable<W4PData> {
    private final String androidSystemName,
                         displayName,
                         description;

    private final W4PIcon icon;

    private int w4pType = -1;

    public static final int TYPE_WHAT = 0,
                            TYPE_WHY = 1,
                            TYPE_WHO = 2,
                            TYPE_WHERE = 3;

    private W4PData(String androidSystemName,
                    String displayName,
                    String description,
                    W4PIcon icon,
                    int type) {
        this.androidSystemName = (androidSystemName == null ? "" : androidSystemName);
        this.displayName = (displayName == null ? "" : displayName);
        this.description = (description == null ? "" : description);
        this.icon = icon;
        w4pType = type;
    }

    /**
     * Creates a W4PData instance from the PermissionInfo model.
     *
     * @param permission the permission to create this W4PData from
     * @return the W4PData instance
     * */
    public static W4PData createFromPermission(PermissionInfo permission) {
        String permissionName = permission.permissionName;
        SensitiveData dangerousPermission = DangerousPermissions.from(permissionName);

        W4PIcon permissionIcon =
                PolicyManagerApplication.ui.getIconManager().getPermissionIcon(dangerousPermission);

        return new W4PData(permissionName,
                           PermissionUtil.getDisplayPermission(permissionName),
                           permission.permissionDescription,
                           permissionIcon,
                           TYPE_WHAT);
    }

    /**
     * Creates a W4PData instance from the PurposeInfo model.
     *
     * @param purpose the purpose to create this W4PData from
     * @return the W4PData instance
     * */
    public static W4PData createFromPurpose(PurposeInfo purpose) {
        String purposeName = purpose.purposeName;
        Purpose p = Purposes.from(purposeName);

        W4PIcon purposeIcon =
                PolicyManagerApplication.ui.getIconManager().getPurposeIcon(p);

        return new W4PData(purposeName,
                           purposeName,
                           "",
                           purposeIcon,
                           TYPE_WHY);
    }

    /**
     * Creates a W4PData instance from the AppInfo model.
     *
     * @param app the app to create this W4PData from
     * @return the W4PData instance
     * */
    public static W4PData createFromApp(AppInfo app) {
        W4PIcon icon = null;

        if(app.packageName.equals(PolicyManagerApplication.SYMBOL_ALL)) {
            icon = W4PIcon.createStaticIcon(R.drawable.ic_layers_black_24dp);
        }
        else {
            icon = PolicyManagerApplication.ui.getIconManager().getAppIcon(app.packageName);
        }

        return new W4PData(app.packageName, app.appName, "", icon, TYPE_WHO);
    }

    /**
     * Creates a W4PData instance from the ThirdPartyLibInfo model.
     *
     * @param thirdPartyLibrary the library to create this W4PData from
     * @return the W4PData instance
     * */
    public static W4PData createFromThirdPartyLib(ThirdPartyLibInfo thirdPartyLibrary) {
        final String libraryName = thirdPartyLibrary.name;
        W4PIcon libraryIcon = null;

        for(ThirdPartyLibrary library : ThirdPartyLibraries.AS_LIST) {
            if(library.qualifiedName.equalsIgnoreCase(libraryName)) {
                libraryIcon = PolicyManagerApplication.ui
                                                      .getIconManager()
                                                      .getThirdPartyLibraryIcon(library);
            }
        }

        return new W4PData(thirdPartyLibrary.name,
                           thirdPartyLibrary.name,
                           thirdPartyLibrary.description,
                           libraryIcon,
                           TYPE_WHERE);
    }

    /**
     * Get the Android system name for this W4PData. For an app, this is the package name. For
     * a permission, this is the fully-qualified permission name.
     *
     * @return the android system name for this data
     * */
    public String getAndroidSystemName() {
        return androidSystemName;
    }

    /**
     * Get the display name for this W4PData. This would be an app name, or "Fine Location" for
     * permissions.
     *
     * @return the display name
     * */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get description of this W4PData. Could be more detailed permission or purpose description.
     *
     * @return W4PData description
     * */
    public String getDescription() { return description; }

    /**
     * Get the icon of this W4PData as W4PIcon
     *
     * @return W4PIcon
     * */
    public W4PIcon getIcon() {
        return icon;
    }

    /**
     * Is this W4PData a permission or 'what'?
     *
     * @return true if permission, false otherwise
     * */
    public boolean isWhat() { return (w4pType == TYPE_WHAT); }

    /**
     * Is this W4PData a purpose or 'why'?
     *
     * @return true if purpose, false otherwise
     * */
    public boolean isWhy() { return (w4pType == TYPE_WHY); }

    /**
     * Is this W4PData an app or 'who'?
     *
     * @return true if app, false otherwise
     * */
    public boolean isWho() { return (w4pType == TYPE_WHO); }

    /**
     * Is this W4PData a library or 'where' - will this data go somewhere?
     *
     * @return true if library, false otherwise
     * */
    public boolean isWhere() { return (w4pType == TYPE_WHERE); }

    public int compareTo(W4PData data) {
        return this.androidSystemName.compareTo(data.getAndroidSystemName());
    }

    public boolean equals(Object o) {
        W4PData other = (W4PData)o;

        return androidSystemName.equals(other.getAndroidSystemName());
    }

    public String toString() { return displayName; }
}