package edu.cmu.policymanager.PolicyManager.enforcement.stacktracecases;

import android.util.Log;

import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibrary;
import edu.cmu.policymanager.PolicyManager.purposes.Purpose;

/**
 * Handles specific things to check for during stack trace analysis. Makes code a bit more
 * readable. 
 * 
 * Note: A programmed purpose is a purpose that was set programmatically in an app's
 * source code via the PE for Android SDK. A programmed purpose is passed in to
 * onDangerousPermissionRequest. If no programmed purpose is present, then stack traces must
 * be analyzed in order to determine a purpose.
 * 
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class StackTraceCases {
    private final Purpose mOdpPurpose, mProgrammingPurpose;
    private final ThirdPartyLibrary mLibrary;

    private StackTraceCases(Purpose odpPurpose,
                            Purpose programmedPurpose,
                            ThirdPartyLibrary library) {
        mOdpPurpose = odpPurpose;
        mProgrammingPurpose = programmedPurpose;
        mLibrary = library;
    }

    /**
     * Creates StackTraceCases instance from relevant params.
     * 
     * @param mOdpPurpose purpose found in the off-device policy file
     * @param programmedPurpose purpose that was passed in from onDangerousPermissionRequest
     * @param mLibrary the mLibrary (if any) found by inspecting stack traces
     * @return instance of StackTraceCases
     * */
    public static StackTraceCases from(Purpose mOdpPurpose,
                                       Purpose programmedPurpose,
                                       ThirdPartyLibrary mLibrary) {
        return new StackTraceCases(mOdpPurpose, programmedPurpose, mLibrary);
    }

    /**
     * Analysis has determined that there is no known purpose for this permission request.
     * 
     * @return true if it cannot determine a purpose, false if it can
     * */
    public boolean cannotDetermineAnyPurpose() {
        final boolean thereIsNoODPPurpose = purposeIsEmpty(mOdpPurpose),
                      thereIsNoProgrammedPurpose = purposeIsEmpty(mProgrammingPurpose),
                      noKnownLibraryIsAccessingData = mLibrary == null;

        return thereIsNoODPPurpose && thereIsNoProgrammedPurpose && noKnownLibraryIsAccessingData;
    }

    /**
     * Analysis has determined a purpose because a mLibrary was found in stack traces, but the 
     * developer did not specify a purpose in the off-device policy or programmatically.
     * 
     * @return true if a purpose was found from only a mLibrary, false otherwise
     * */
    public boolean analysisFoundPurposeButDeveloperDidNotSpecifyOne() {
        final boolean thereIsNoODPPurpose = purposeIsEmpty(mOdpPurpose),
                      thereIsNoProgrammedPurpose = purposeIsEmpty(mProgrammingPurpose),
                      mLibraryIsAccessingData = mLibrary != null;

        return thereIsNoODPPurpose && thereIsNoProgrammedPurpose && mLibraryIsAccessingData;
    }

    /**
     * A purpose was set programmatically and passed from onDangerousPermissionRequest, but there
     * was no matching purpose found in the off-device policy.
     * 
     * @return true if the programmed purpose does not match ODP, false otherwise
     * */
    public boolean programmedPurposeDoesNotMatchODP() {
        if(!purposeIsEmpty(mProgrammingPurpose)) {
            return !mProgrammingPurpose.equals(mOdpPurpose);
        } else if(purposeIsEmpty(mProgrammingPurpose) && purposeIsEmpty(mOdpPurpose)) {
            return true;
        }

        return false;
    }

    /**
     * Analysis found a third-party mLibrary whose purpose does not match the purpose that
     * was found in the off-device policy. For example: an app is requesting fine location and
     * the ODP says it is for nearby places. Stack trace analysis picked up an advertising mLibrary
     * and so the actual purpose is for advertising. This means there is a purpose mismatch.
     * 
     * @return true if there is a purpose mismatch for this request, and false otherwise
     * */
    public boolean thereIsAThirdPartyLibraryAndItsPurposeDoesNotMatchOurs() {
        if(mLibrary == null) { return false; }
        if(mOdpPurpose == null) { return true; }

        return !mOdpPurpose.equals(mLibrary.purpose);
    }

    /**
     * Analysis found a programmed purpose, but no purpose specified in the off-device policy.
     * 
     * @return true if programmed purpose but none in ODP, false if there is no programmed
     * purpose or there is an ODP purpose present.
     * */
    public boolean thereIsAProgrammedPurposeButNotODP() {
        final boolean thereIsAProgrammedPurpose = mProgrammingPurpose != null,
                      thereIsNoODP = mOdpPurpose == null;

        return thereIsAProgrammedPurpose && thereIsNoODP;
    }

    private boolean purposeIsEmpty(Purpose p) {
        return p == null || p.name == null || p.name.length() == 0;
    }
}