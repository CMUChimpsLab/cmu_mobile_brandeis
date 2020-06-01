package edu.cmu.policymanager.util;

import android.util.Log;

import java.util.Arrays;
import java.util.List;

import edu.cmu.policymanager.PolicyManager.enforcement.PermissionRequest;
import edu.cmu.policymanager.PolicyManager.enforcement.stacktracecases.StackTraceCases;

/**
 * Debugging tools.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public final class PolicyManagerDebug {
    private static final boolean DEBUG = true;

    public static void logException(Throwable error) {
        Log.e("policymanager-error", error.getMessage());
        Log.d("policymanager-error", Arrays.toString(error.getStackTrace()));
    }

    public static void debugWithMessage(String message) {
        if(DEBUG) {
            Log.d("pm-debug", message);
        }
    }

    public static void debugOnDangerousPermissionRequest(String packageName,
                                                         String permission,
                                                         String purpose) {
        if(DEBUG) {
            Log.d("cmu-policy-manager", packageName + " requests " + permission +
                    " for " + ((purpose == null || purpose.isEmpty()) ?
                    "unknown purpose " : purpose));
        }
    }

    public static void debugMainThreadStacktraces(List<StackTraceElement[]> stacktraces) {
        if(DEBUG) {
            if (stacktraces != null) {
                StackTraceElement[] main = stacktraces.get(0);

                for (final StackTraceElement elem : main) {
                    Log.d("stacktrace-dbg", elem.toString());
                }
            } else {
                Log.d("stacktrace-dbg", "No stacktraces available");
            }
        }
    }

    public static void debugStacktraceAnalysis(StackTraceCases analysisResult) {
        if(DEBUG) {
            if(analysisResult.analysisFoundPurposeButDeveloperDidNotSpecifyOne()) {
                Log.d("stacktrace-analysis", "We found a purpose, but the developer " +
                                                       "did not specify one");
            } else if(analysisResult.cannotDetermineAnyPurpose()) {
                Log.d("stacktrace-analysis", "We cannot find any purposes at all");
            } else if(analysisResult.programmedPurposeDoesNotMatchODP()) {
                Log.d("stacktrace-analysis", "Programmed purpose mismatches ODP");
            } else if(analysisResult.thereIsAThirdPartyLibraryAndItsPurposeDoesNotMatchOurs()) {
                Log.d("stacktrace-analysis", "There is a library, and its purpose " +
                                                       "is inconsistent with the developer " +
                                                       "stated purpose");
            } else {
                Log.d("stacktrace-analysis", "Everything checks out, commence the " +
                                                       "next step of policy enforcement");
            }
        }
    }

    public static void debugAnalysisResult(PermissionRequest request) {
        if(DEBUG) {
            String thirdPartyUse =
                    (request.thirdPartyLibrary == null ?
                            "the app" :
                            request.thirdPartyLibrary.qualifiedName
                    );

            Log.d("analysis-result",
                    "Stack trace analysis concludes that " + request.packageName + " is " +
                          "requesting " + request.permission.androidPermission.toString() +
                          " for " + request.purpose.name.toString() +
                          " and it is used by " + thirdPartyUse);
        }
    }
}