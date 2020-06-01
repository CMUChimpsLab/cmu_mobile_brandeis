package edu.cmu.policymanager.validation;

import android.os.Looper;

import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.PolicyManager.purposes.Purposes;
import edu.cmu.policymanager.PolicyManager.sensitivedata.DangerousPermissions;

/**
 * Precondition tests for input validation.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class Precondition {

    /**
     * Check if the provided object is null, and throw IllegalArgumentException if it is.
     *
     * @param o the object to check for null
     * @param errorMessage custom error message to display if o is null
     * */
    public static void checkIfNull(Object o,
                                   CharSequence errorMessage) {
        String message = (errorMessage == null ? "" : errorMessage.toString());

        if(o == null) { throw new IllegalArgumentException(message); }
    }

    /**
     * Check if the provided CharSequence is empty, and throw IllegalArgumentException if it is.
     *
     * @param sequence the CharSequence to check
     * */
    public static void checkEmptyCharSequence(CharSequence sequence) {
        if(sequence == null || sequence.length() == 0) {
            throw new IllegalArgumentException("CharSequence is empty");
        }
    }

    /**
     * Check to see if the current method is being executed in the UI thread. Throws an
     * IllegalStateException if it is not.
     * */
    public static void checkUiThread() {
        if(!Looper.getMainLooper().equals(Looper.myLooper())) {
            throw new IllegalStateException("Component must be rendered in the UI thread.");
        }
    }

    /**
     * Checks to see if some condition holds, and throws IllegalStateException if it does not.
     *
     * @param expression boolean expression to test
     * @param message the error message to include
     * */
    public static void checkState(boolean expression,
                                  CharSequence message) {
        if(!expression) {
            throw new IllegalStateException(message.toString());
        }
    }

    /**
     * Checks the given UserPolicy for validity. A valid UserPolicy:
     * - Does not have an empty app name
     * - Has a permission and the Android name is not empty
     * - Has a purpose and the purpose name is not empty
     * - The permission name can be mapped to one of the SensitiveData types in DangerousPermission
     * - The purpose name can be mapped to one of the Purpose types in Purposes
     *
     * If any requirements are not met, an IllegalArgumentException is thrown.
     * */
    public static void checkIfPolicyIsValid(UserPolicy policy) {
        boolean appIsMissing = policy.app == null || policy.app.isEmpty(),
                permissionIsMissing = policy.permission == null ||
                                      policy.permission.androidPermission == null ||
                                      policy.permission.androidPermission.length() == 0,
                purposeIsMissing = policy.purpose == null ||
                                   policy.purpose.name == null ||
                                   policy.purpose.name.length() == 0;

        if(appIsMissing || permissionIsMissing || purposeIsMissing) {
            throw new IllegalArgumentException(policy.toString() + " is invalid");
        }

        DangerousPermissions.from(policy.permission.androidPermission);
        Purposes.from(policy.purpose.name);
    }
}