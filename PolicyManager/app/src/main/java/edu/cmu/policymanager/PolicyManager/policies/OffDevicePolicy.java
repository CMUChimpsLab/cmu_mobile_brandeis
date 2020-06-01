package edu.cmu.policymanager.PolicyManager.policies;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import edu.cmu.policymanager.PolicyManager.purposes.Purposes;
import edu.cmu.policymanager.PolicyManager.sensitivedata.DangerousPermissions;
import edu.cmu.policymanager.application.PolicyManagerApplication;
import edu.cmu.policymanager.util.PolicyManagerDebug;
import edu.cmu.policymanager.validation.Precondition;

/**
 * Created by Mike Czapik (Carnegie Mellon University) on 11/9/2018.
 *
 * Off-device policies are the same as app polices. Originally we wanted to include an
 * off-device policy when data left the device to enforce a privacy policy. This never came
 * to fruition, but we kept using the term off-device policy.
 *
 * An off-device or app policy as it is implemented, is just a JSON array of JSON objects which
 * state the purpose for a permission being requested, and the location in code where this access
 * occurs. The class + method names are used in stack trace analysis to differentiate sensitive
 * permission requests by their purpose (ex: location is used for nearby places in
 * com.domain.MainActivity onCreate, and location being used for advertising in
 * com.domain.SearchActivity onResume.
 */

public class OffDevicePolicy {

    /**
     * Represents an individual access of data, where it occurs, the purpose of the
     * access and any additional developer comments.
     * */
    public final class SubPolicy {
        public final String purpose, comment, permission, className, method;

        private SubPolicy(final SubPolicyBuilder config) {
            purpose = config.purpose;
            comment = config.comment;
            permission = config.permission;
            className = config.className;
            method = config.method;
        }

        public String toString() {
            return permission + " accessed in " + className + "." + method + " for " + purpose;
        }
    }

    public final List<SubPolicy> SUBPOLICIES = new LinkedList<SubPolicy>();

    /**
     * Create an OffDevicepolicy from the ODP string passed to us by PE Android (if one exists)
     * */
    public OffDevicePolicy(final String policyString) throws JSONException {
        parse(policyString);
    }

    public String toString() {
        String policyAsString = "";

        for(SubPolicy policy : SUBPOLICIES) {
            policyAsString += policy.toString() + "\n";
        }

        return policyAsString;
    }

    public CharSequence getPolicyComment(CharSequence permission,
                                         CharSequence purpose) {
        String permissionName = permission.toString(),
               purposeName = purpose.toString();

        for (final OffDevicePolicy.SubPolicy subPolicy : SUBPOLICIES) {
            if (subPolicy.permission.equalsIgnoreCase(permissionName) &&
                subPolicy.purpose.equalsIgnoreCase(purposeName)) {
                return subPolicy.comment;
            }
        }

        return null;
    }

    /**
     * Creates an off-device policy JSON object as a string from an app's AndroidManifest.xml
     * file. This will be necessary when an app does not already have an off-device policy
     * at install-time.
     *
     * @param packageName the app to create an off-device policy for
     * @param context the context
     * @return String JSON object of off-device policy
     * */
    public static String createODPStringFromManifest(CharSequence packageName,
                                                     Context context) {
        Precondition.checkEmptyCharSequence(packageName);
        Precondition.checkIfNull(context, "cannot create odp with null context");

        try {
            PackageInfo info =
                    context.getPackageManager()
                           .getPackageInfo(packageName.toString(), PackageManager.GET_PERMISSIONS);

            JsonArray appPolicy = new JsonArray();

            addStringPermissionsToPolicy(info.requestedPermissions, appPolicy);
            addPermissionInfoToPolicy(info.permissions, appPolicy);

            return appPolicy.toString();
        } catch(PackageManager.NameNotFoundException notFoundException) {
            PolicyManagerDebug.logException(notFoundException);
        }

        throw new IllegalStateException("Cannot create ODP string for " + packageName);
    }

    private static JsonObject createPolicyObject(String permission) {
        JsonObject policy = new JsonObject();

        policy.addProperty(
                "uses",
                permission
        );
        policy.addProperty(
                "purpose",
                Purposes.RUNNING_OTHER_FEATURES.name.toString()
        );
        policy.addProperty(
                "method",
                PolicyManagerApplication.SYMBOL_ALL
        );
        policy.addProperty(
                "class",
                PolicyManagerApplication.SYMBOL_ALL
        );
        policy.addProperty(
                "for",
                "Used for app functionality"
        );

        return policy;
    }

    private static void addStringPermissionsToPolicy(String[] permissions,
                                                     JsonArray policyList) {
        if(permissions != null && permissions.length > 0) {
            for (final String permission : permissions) {
                if (DangerousPermissions.permissionIsDangerous(permission)) {
                    if (!policyArrayContainsPermission(policyList, permission)) {
                        policyList.add(createPolicyObject(permission));
                    }
                }
            }
        }
    }

    private static void addPermissionInfoToPolicy(PermissionInfo[] permissions,
                                                  JsonArray policyList) {
        if(permissions != null && permissions.length > 0) {
            for (final PermissionInfo permission : permissions) {
                if (DangerousPermissions.permissionIsDangerous(permission.name)) {
                    if (!policyArrayContainsPermission(policyList, permission.name)) {
                        policyList.add(createPolicyObject(permission.name));
                    }
                }
            }
        }
    }

    private static boolean policyArrayContainsPermission(JsonArray policyList,
                                                         String permission) {
        for(JsonElement elem : policyList) {
            JsonObject policy = elem.getAsJsonObject();

            if(policy.get("uses").getAsString().equalsIgnoreCase(permission)) {
                return true;
            }
        }

        return false;
    }

    private void parse(String policyString) throws JSONException {
        if(policyString != null) {
            JSONArray policyList = new JSONArray(policyString);

            for (int i = 0; i < policyList.length(); i++) {
                JSONObject policyObject = policyList.getJSONObject(i);
                SubPolicy policy = extractSubPolicy(policyObject);

                if (policy != null) { SUBPOLICIES.add(policy); }
            }
        }
    }

    private SubPolicy extractSubPolicy(JSONObject jsonPolicy) {
        SubPolicyBuilder policyBuilder = new SubPolicyBuilder();

        policyBuilder.setPermission(extractProperty(jsonPolicy, "uses"));
        policyBuilder.setPurpose(extractProperty(jsonPolicy, "purpose"));
        policyBuilder.setClassName(extractProperty(jsonPolicy, "class"));
        policyBuilder.setComment(extractProperty(jsonPolicy, "for"));
        policyBuilder.setMethod(extractProperty(jsonPolicy, "method"));

        return policyBuilder.build();
    }

    private String extractProperty(JSONObject policyJson,
                                   String property) {
        try {
            return policyJson.getString(property);
        }
        catch(JSONException e) {
            PolicyManagerDebug.logException(e);
        }

        return "";
    }

    private class SubPolicyBuilder {
        String purpose, comment, permission, className, method;

        public SubPolicyBuilder setPurpose(String purpose) {
            Precondition.checkEmptyCharSequence(purpose);
            Purposes.from(purpose);

            this.purpose = purpose;
            return this;
        }

        public SubPolicyBuilder setComment(String comment) {
            this.comment = comment;
            return this;
        }

        public SubPolicyBuilder setClassName(String className) {
            Precondition.checkEmptyCharSequence(className);

            this.className = className;
            return this;
        }

        public SubPolicyBuilder setMethod(String method) {
            Precondition.checkEmptyCharSequence(method);

            this.method = method;
            return this;
        }

        public SubPolicyBuilder setPermission(String permission) {
            Precondition.checkEmptyCharSequence(permission);
            DangerousPermissions.from(permission);

            this.permission = permission;
            return this;
        }

        public SubPolicy build() {
            return new SubPolicy(this);
        }
    }
}