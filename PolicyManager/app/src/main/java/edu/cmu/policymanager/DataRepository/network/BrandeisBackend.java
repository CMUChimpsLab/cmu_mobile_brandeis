package edu.cmu.policymanager.DataRepository.network;

import android.Manifest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.cmu.policymanager.DataRepository.db.model.PermissionInfo;
import edu.cmu.policymanager.DataRepository.db.model.PolicyProfileSetting;
import edu.cmu.policymanager.DataRepository.db.model.PurposeInfo;
import edu.cmu.policymanager.DataRepository.db.model.ThirdPartyLibInfo;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibraries;
import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.PolicyManager.purposes.Purposes;
import edu.cmu.policymanager.PolicyManager.sensitivedata.DangerousPermissions;
import edu.cmu.policymanager.application.PolicyManagerApplication;
import edu.cmu.policymanager.viewmodel.W4PData;
import edu.cmu.policymanager.viewmodel.W4PGraph;

/**
 * Emulates some server back-end that has things like policy profiles or any
 * crowd-sourced data etc.
 *
 * Created by Mike Czapik (Carnegie Mellon University) on 8/15/2018.
 */

public class BrandeisBackend {
    public BrandeisBackend() {}

    public List<PolicyProfileSetting> getSampleProfileSettings() {
        List<PolicyProfileSetting> settings = new ArrayList<>(5);

        final String PROFILE_NAME = "Organizational Profile";

        PolicyProfileSetting noFineLocation = new PolicyProfileSetting();
        noFineLocation.profileName = PROFILE_NAME;
        noFineLocation.app = PolicyManagerApplication.SYMBOL_ALL;
        noFineLocation.permission = DangerousPermissions.FINE_LOCATION.androidPermission.toString();
        noFineLocation.purpose = Purposes.DISPLAY_ADVERTISEMENT.name.toString();
        noFineLocation.thirdPartyLibrary = ThirdPartyLibraries.THIRD_PARTY_USE;
        noFineLocation.policyAction = UserPolicy.Policy.DENY.name();
        settings.add(noFineLocation);

        PolicyProfileSetting noCoarseLocation = new PolicyProfileSetting();
        noCoarseLocation.profileName = PROFILE_NAME;
        noCoarseLocation.app = PolicyManagerApplication.SYMBOL_ALL;
        noCoarseLocation.permission =
                DangerousPermissions.COARSE_LOCATION.androidPermission.toString();
        noCoarseLocation.purpose = Purposes.DISPLAY_ADVERTISEMENT.toString();
        noCoarseLocation.thirdPartyLibrary = ThirdPartyLibraries.THIRD_PARTY_USE;
        noCoarseLocation.policyAction = UserPolicy.Policy.DENY.name();
        settings.add(noCoarseLocation);

        return settings;
    }
}