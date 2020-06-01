package edu.cmu.policymanager.ui.runtime;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;

import java.util.function.Consumer;

import edu.cmu.policymanager.DataRepository.DataRepository;
import edu.cmu.policymanager.PolicyManager.PolicyManager;
import edu.cmu.policymanager.PolicyManager.PolicyNotification;
import edu.cmu.policymanager.PolicyManager.Util;
import edu.cmu.policymanager.PolicyManager.enforcement.PermissionRequest;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibraries;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibrary;
import edu.cmu.policymanager.PolicyManager.policies.OffDevicePolicy;
import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.PolicyManager.purposes.Purpose;
import edu.cmu.policymanager.PolicyManager.purposes.Purposes;
import edu.cmu.policymanager.PolicyManager.sensitivedata.DangerousPermissions;
import edu.cmu.policymanager.PolicyManager.sensitivedata.SensitiveData;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.application.PolicyManagerApplication;
import edu.cmu.policymanager.peandroid.PEAndroid;
import edu.cmu.policymanager.ui.common.UIPlugin;
import edu.cmu.policymanager.util.PolicyManagerDebug;

/**
 * Prompts the user for a policy decision if the policy setting is ASK. The prompt presents
 * three controls:
 *  - Only this time, which will allow access to the requested data for five minutes. Once
 *  five minutes are up, the user will be prompted for another policy decision.
 *
 *  - Always Allow, which will grant access to this data until the user changes the policy setting.
 *
 *  - Always Deny, which will deny access to this data until the user changes the policy setting.
 *
 * Created by Mike Czapik (Carnegie Mellon University) on 12/26/2018.
 */

public class RuntimeUI extends Activity implements UIPlugin {
    private final Context mActivityContext = this;

    private Purpose mPurpose;
    private UserPolicy mRuntimePolicy;
    private PermissionRequest mRequest;
    private String mPackageName, mAppName;

    private AlertDialog mDialog;
    private TextView mShowMoreView,
                     mDescription,
                     mPurposeText,
                     mAppPolicyDisplay;
    private ImageView mPermissionIcon;
    private View mDialogView;
    private LinearLayout mDetails;

    /**
     * Intent string that allows this activity to receive the PermissionRequest object created
     * from the policy enforcement algorithm.
     * */
    public static final String INTENT_KEY_PERMISSION_REQUEST = "permissionRequest";

    private static final String descriptionTemplateNonVerb =
            "Allow <b>@app</b> to access your @permission?",
                                descriptionTemplateVerb =
            "Allow <b>@app</b> to @permission";

    public void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);

        PermissionRequest request =
                PolicyManagerApplication.ui
                                        .getRuntimeData()
                                        .getParcelable(INTENT_KEY_PERMISSION_REQUEST);

        mPackageName = request.packageName;
        mAppName = Util.getAppCommonName(mActivityContext, mPackageName);

        mPurpose = (request.purpose == null ?
                Purposes.RUNNING_OTHER_FEATURES : request.purpose);

        mRequest = PermissionRequest.builder(request.context)
                                    .setPackageName(mPackageName)
                                    .setPermission(request.permission.androidPermission)
                                    .setStacktraces(request.stacktraces)
                                    .setPurpose(mPurpose.name)
                                    .setLibrary(request.thirdPartyLibrary)
                                    .setPalRequestDescription(request.palRequestDescription)
                                    .setResultReceiver(request.recv)
                                    .build();

        int layoutId = R.layout.component_runtime_prompt;

        if(usedInternally(mRequest.thirdPartyLibrary)) {
            layoutId = R.layout.component_runtime_prompt_app_only;
        }

        mDialogView = LayoutInflater.from(mActivityContext)
                                    .inflate(layoutId, null, false);

        mShowMoreView = mDialogView.findViewById(R.id.runtime_show_more);
        mDetails = mDialogView.findViewById(R.id.runtime_from_header);
        mDescription = mDialogView.findViewById(R.id.runtime_description);
        mPurposeText = mDialogView.findViewById(R.id.runtime_usedfor);
        mAppPolicyDisplay = mDialogView.findViewById(R.id.runtime_app_policy);
        mPermissionIcon = mDialogView.findViewById(R.id.runtime_icon);

        mDialog = new AlertDialog.Builder(mActivityContext)
                                 .setCancelable(false)
                                 .setView(mDialogView)
                                 .create();

        mDialog.show();
    }

    private boolean permissionNeedsModified(SensitiveData permission) {
        String displayString = permission.getDisplayPermission().toString();
        String[] words = displayString.split(" ");

        return (words[0].equals("Read") || words[0].equals("Write") ||
                words[0].equals("Record") || words[0].equals("Get"));
    }

    private String makePermissionMoreReadable(SensitiveData permission) {
        String displayString = permission.getDisplayPermission().toString();
        String[] words = displayString.split(" ");

        if(words[0].equals("Read") || words[0].equals("Record") || words[0].equals("Get")) {
            return words[0] + " Your " + words[1];
        }

        return words[0] + " To Your " + words[1];
    }

    private boolean usedInternally(ThirdPartyLibrary library) {
        return (library == null ||
                library.category.equalsIgnoreCase(ThirdPartyLibraries.APP_INTERNAL_USE));
    }

    private Consumer<String> addPolicyDescriptionTo(final TextView display) {
        return new Consumer<String>() {
            @Override
            public void accept(final String odpString) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OffDevicePolicy odp = new OffDevicePolicy(odpString);
                            CharSequence permissionName = mRequest.permission.androidPermission,
                                         purposeName = Purposes.RUNNING_OTHER_FEATURES.name;

                            if(mRequest.purpose != null) {
                                purposeName = mRequest.purpose.name.toString();
                            }

                            CharSequence comment = odp.getPolicyComment(permissionName, purposeName);

                            if(comment != null) {
                                display.setText(comment);
                            } else if(mRequest.palRequestDescription != null &&
                                      mRequest.palRequestDescription.length() > 0){
                                display.setText(mRequest.palRequestDescription);
                            } else {
                                display.setText(mRequest.purpose.description);
                            }
                        } catch(JSONException jse) {
                            PolicyManagerDebug.logException(jse);
                        }
                    }
                });
            }
        };
    }

    public void onStart() {
        super.onStart();

        PermissionRequest request =
                PolicyManagerApplication.ui
                        .getRuntimeData()
                        .getParcelable(INTENT_KEY_PERMISSION_REQUEST);

        String packageName = request.packageName;

        mRuntimePolicy = UserPolicy.createAppPolicy(packageName,
                                                    request.permission,
                                                    mPurpose,
                                                    request.thirdPartyLibrary);

        int imageResource = PolicyManagerApplication.ui
                                                    .getIconManager()
                                                    .getPermissionIcon(mRequest.permission)
                                                    .asResourceId();

        Spanned displayText;

        if(permissionNeedsModified(request.permission)) {
            String moreReadablePermission = makePermissionMoreReadable(request.permission);

            displayText = Html.fromHtml(
                    descriptionTemplateVerb
                            .replace("@app", mAppName)
                            .replace("@permission", moreReadablePermission)
            );
        } else {
            displayText = Html.fromHtml(
                    descriptionTemplateNonVerb
                            .replace("@app", mAppName)
                            .replace("@permission", request.permission.getDisplayPermission())
            );
        }

        mPermissionIcon.setImageResource(imageResource);

        mPurposeText.setText(mRequest.purpose.name);
        mDescription.setText(displayText);

        if(!usedInternally(request.thirdPartyLibrary)) {
            TextView usedByText = mDialogView.findViewById(R.id.runtime_usedby);
            usedByText.setText(request.thirdPartyLibrary.name);
        }
    }

    public void onResume() {
        super.onResume();

        DataRepository.fromDisk()
                      .getODPForPackage(mPackageName)
                      .thenAccept(addPolicyDescriptionTo(mAppPolicyDisplay));
    }

    public void onPause() { super.onPause(); }

    public void onStop() { super.onStop(); }

    public void onDestroy() {
        super.onDestroy();
        mDialog.dismiss();
    }

    public void showMore(View v) {
        mShowMoreView.setVisibility(View.GONE);
        mDetails.setVisibility(View.VISIBLE);
    }

    public void hideDetails(View v) {
        mShowMoreView.setVisibility(View.VISIBLE);
        mDetails.setVisibility(View.GONE);
    }

    /**
     * Handler for the 'Only this time' button.
     * */
    public void allowOnce(View v) {
        mRuntimePolicy.allow();
        PolicyManager.getInstance().logPolicyForAskPrompt(mRuntimePolicy);

        PolicyNotification.sendAllowedNotification(
                mActivityContext,
                "User Settings",
                mRequest
        );

        PEAndroid.connectToReceiver(mRequest.recv).allowPermission();
        dismissRuntime();
    }

    /**
     * Handler for the 'Always allow' button.
     * */
    public void allowPermission(View v) {
        mRuntimePolicy.allow();
        PolicyManager.getInstance().update(mRuntimePolicy);

        PolicyNotification.sendAllowedNotification(
                mActivityContext,
                "User Settings",
                mRequest
        );

        PEAndroid.connectToReceiver(mRequest.recv).allowPermission();
        dismissRuntime();
    }

    /**
     * Handler for the 'Always deny' button.
     * */
    public void denyPermission(View v) {
        mRuntimePolicy.deny();
        PolicyManager.getInstance().update(mRuntimePolicy);

        PolicyNotification.sendDeniedNotification(
                mActivityContext,
                "User Settings",
                mRequest
        );

        PEAndroid.connectToReceiver(mRequest.recv).denyPermission();
        dismissRuntime();
    }

    private void dismissRuntime() {
        PEAndroid.returnControl(mActivityContext);
        mDialog.dismiss();
        finishAndRemoveTask();
        finishAffinity();
    }

    public Class getUI() { return RuntimeUI.class; }
}