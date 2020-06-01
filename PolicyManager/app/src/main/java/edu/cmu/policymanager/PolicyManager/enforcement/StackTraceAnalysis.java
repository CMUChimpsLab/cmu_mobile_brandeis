package edu.cmu.policymanager.PolicyManager.enforcement;

import android.content.ComponentName;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.policymanager.DataRepository.DataRepository;
import edu.cmu.policymanager.DataRepository.db.model.PolicyProfileSetting;
import edu.cmu.policymanager.PolicyManager.PolicyManager;
import edu.cmu.policymanager.PolicyManager.enforcement.stacktracecases.StackTraceCases;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibraries;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibrary;
import edu.cmu.policymanager.PolicyManager.policies.OffDevicePolicy;
import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.PolicyManager.purposes.Purpose;
import edu.cmu.policymanager.PolicyManager.purposes.Purposes;
import edu.cmu.policymanager.PolicyManager.sensitivedata.SensitiveData;
import edu.cmu.policymanager.application.PolicyManagerApplication;
import edu.cmu.policymanager.ui.configure.cards.globalsetting.PermissionGroupCard;
import edu.cmu.policymanager.util.PolicyManagerDebug;

import static edu.cmu.policymanager.ui.runtime.RuntimeUI.INTENT_KEY_PERMISSION_REQUEST;

/**
 * Analyzes stacktraces to determine a purpose of data access.
 *
 * The analysis process looks through the main thread (list element 0) and tries to match
 * class names with known third-party libraries. Purpose is then determined based on what
 * the library is used for.
 *
 * The analysis process also attempts to look through the off-device or app policy for this
 * app to see if it can find a purpose there. This is determined by matching class and method
 * names found in stacktraces and the ODP (off-device policy).
 *
 * It will also compare purposes if there are purposes from multiple sources. If it
 * finds that a library is accessing this data, but the purpose of the library does not
 * match anything we have in an ODP, then the purpose is that of the library. This could
 * then lead to the user being prompted for a policy decision at runtime.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class StackTraceAnalysis {

    /**
     * Infers some purpose of data access from the PermissionRequest from an
     * onDangerousPermissionRequest call.
     *
     * @param permissionRequest the incoming permission request
     * @return updated permission request with purpose or library
     * */
    public static PermissionRequest inferPurposeAndLibrary(PermissionRequest permissionRequest) {
        boolean canPerformPurposeInference =
                permissionRequest.stacktraces != null ||
                permissionRequest.topActivity != null;

        if(canPerformPurposeInference) {
            String odpString = DataRepository.getInstance()
                                             .syncGetODPForPackage(
                                                     permissionRequest.packageName
                                             );

            OffDevicePolicy odp = null;

            try {
                odp = new OffDevicePolicy(odpString);
            } catch(JSONException jse) {
                return updateRequest(
                        permissionRequest,
                        ThirdPartyLibraries.CATEGORY_APP_INTERNAL_USE,
                        Purposes.RUNNING_OTHER_FEATURES
                );
            }

            StackTraceElement[] mainThread =
                    (permissionRequest.stacktraces != null ? permissionRequest.stacktraces.get(0) :
                                                             null);

            ThirdPartyLibrary library = getThirdPartyLibrary(
                    mainThread,
                    permissionRequest.topActivity
            );

            Purpose programmedPurpose = permissionRequest.purpose;
            Purpose odpPurpose = searchStacktraceForPurpose(mainThread,
                                                            permissionRequest.permission,
                                                            odp);

            StackTraceCases analysisCase = StackTraceCases.from(
                    odpPurpose,
                    programmedPurpose,
                    library
            );

            PolicyManagerDebug.debugStacktraceAnalysis(analysisCase);

            if(analysisCase.cannotDetermineAnyPurpose()) {
                return updateRequest(
                        permissionRequest,
                        ThirdPartyLibraries.CATEGORY_APP_INTERNAL_USE,
                        Purposes.RUNNING_OTHER_FEATURES
                );
            }

            if(analysisCase.analysisFoundPurposeButDeveloperDidNotSpecifyOne()) {
                return updateRequest(permissionRequest, library, library.purpose);
            }

            if(analysisCase.programmedPurposeDoesNotMatchODP()) {
                ThirdPartyLibrary category = ThirdPartyLibraries.CATEGORY_APP_INTERNAL_USE;
                Purpose purposeToBeUsed = odpPurpose;

                if(odpPurpose == null) {
                    purposeToBeUsed = programmedPurpose;
                } else {
                    if (Purposes.isThirdPartyUse(odpPurpose)) {
                        category = ThirdPartyLibraries.CATEGORY_THIRD_PARTY_USE;
                    }
                }

                return updateRequest(permissionRequest, category, purposeToBeUsed);
            }

            if(analysisCase.thereIsAThirdPartyLibraryAndItsPurposeDoesNotMatchOurs()) {
                return updateRequest(permissionRequest, library, library.purpose);
            }

            Purpose purposeToUse = odpPurpose;

            if(analysisCase.thereIsAProgrammedPurposeButNotODP()) {
                purposeToUse = programmedPurpose;
            }

            purposeToUse = (purposeToUse == null ? permissionRequest.purpose : purposeToUse);

            return updateRequest(permissionRequest, library, purposeToUse);
        } else {
            PolicyManagerDebug.debugWithMessage("There is nothing to analyze");

            Purpose purpose = permissionRequest.purpose;

            if(purpose == null) {
                purpose = Purposes.RUNNING_OTHER_FEATURES;
            }

            return updateRequest(
                    permissionRequest,
                    ThirdPartyLibraries.CATEGORY_APP_INTERNAL_USE,
                    purpose
            );
        }
    }

    private static PermissionRequest updateRequest(PermissionRequest request,
                                                   ThirdPartyLibrary library,
                                                   Purpose purpose) {
        ThirdPartyLibrary lib = library == null ? ThirdPartyLibraries.CATEGORY_APP_INTERNAL_USE :
                                library;

        return PermissionRequest.builder(request.context)
                                .setPackageName(request.packageName)
                                .setPermission(request.permission.androidPermission)
                                .setPurpose(purpose.name)
                                .setLibrary(lib)
                                .setResultReceiver(request.recv)
                                .build();
    }

    private static ThirdPartyLibrary getThirdPartyLibrary(StackTraceElement[] stacktraces,
                                                          ComponentName topActivity) {
        if(stacktraces != null) {
            for(StackTraceElement code : stacktraces) {
                for(ThirdPartyLibrary library : ThirdPartyLibraries.AS_LIST) {
                    if(code.getClassName().contains(library.qualifiedName)) {
                        return library;
                    }
                }
            }
        }

        if(topActivity != null) {
            for(ThirdPartyLibrary library : ThirdPartyLibraries.AS_LIST) {
                if(topActivity.getClassName().contains(library.qualifiedName)) {
                    return library;
                }
            }
        }

        return null;
    }

    private static Purpose searchStacktraceForPurpose(StackTraceElement[] mainThread,
                                                      SensitiveData permission,
                                                      OffDevicePolicy odp) {
        if(mainThread != null) {
            for (final StackTraceElement offendingCode : mainThread) {
                String inferredPurpose = searchODPForMatchingCall(odp, permission, offendingCode);

                if (inferredPurpose != null) {
                    return Purposes.from(inferredPurpose);
                }
            }
        }

        return null;
    }

    private static String searchODPForMatchingCall(OffDevicePolicy odp,
                                                   SensitiveData permission,
                                                   StackTraceElement codeToInspect) {
        String permissionName = permission.androidPermission.toString();

        String purpose = search(odp, permissionName, codeToInspect.toString());

        if(purpose == null) {
            purpose = search(odp, permissionName, PolicyManagerApplication.SYMBOL_ALL);
        }

        return purpose;
    }

    private static String search(OffDevicePolicy odp,
                                 CharSequence permissionName,
                                 String searchElement) {
        for(OffDevicePolicy.SubPolicy subPolicy : odp.SUBPOLICIES) {
            String knownOffendingClass = subPolicy.className,
                    knownOffendingMethod = subPolicy.method;

            if(subPolicy.permission.equalsIgnoreCase(permissionName.toString()) &&
               matchesODP(searchElement, knownOffendingClass) &&
               matchesODP(searchElement, knownOffendingMethod)) {
                PolicyManagerDebug.debugWithMessage(
                        "Found odp policy at: " + searchElement + " it is: " + subPolicy.toString()
                );

                return subPolicy.purpose;
            }
        }

        return null;
    }

    //This handles the case where we generate an ODP based on an app's manifest
    private static boolean matchesODP(final String codeToCompare,
                                      final String odpElement) {
        return codeToCompare.contains(odpElement);
    }
}