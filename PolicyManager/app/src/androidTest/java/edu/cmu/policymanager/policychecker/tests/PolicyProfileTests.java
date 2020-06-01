package edu.cmu.policymanager.policychecker.tests;

import android.Manifest;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.cmu.policymanager.DataRepository.DataRepository;
import edu.cmu.policymanager.DataRepository.db.model.PolicyProfile;
import edu.cmu.policymanager.PolicyManager.PolicyManager;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibraries;
import edu.cmu.policymanager.PolicyManager.enforcement.EnforcementStatus;
import edu.cmu.policymanager.PolicyManager.enforcement.PermissionRequest;
import edu.cmu.policymanager.PolicyManager.enforcement.PolicyEnforcement;
import edu.cmu.policymanager.PolicyManager.enforcement.PolicyProfileCheck;
import edu.cmu.policymanager.PolicyManager.enforcement.PolicyStub;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibrary;
import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.PolicyManager.purposes.Purposes;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class PolicyProfileTests {
    public static final String YELP = "com.android.yelp",
                               ORGANIZATIONAL_PROFILE = "Organizational Profile";

    private static PermissionRequest enforceableRequest = null,
                                     unenforceableRequest = null;

    private static DataRepository repository;
    private static PolicyManager policyManager;

    @BeforeClass
    public static void setup() {
        Context context = InstrumentationRegistry.getTargetContext();

        DataRepository.init(context, DataRepository.StorageType.IN_MEMORY);
        repository = DataRepository.fromMemory();
        repository.logInstallInfo(YELP, ODPFactory.createODPString());

        policyManager = PolicyManager.getInstance();
        policyManager.syncInstallPolicyProfile(ORGANIZATIONAL_PROFILE);

        enforceableRequest =
                PermissionRequest.builder()
                                 .setPackageName(YELP)
                                 .setPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                                 .setPurpose(Purposes.DISPLAY_ADVERTISEMENT.name)
                                 .setLibrary(ThirdPartyLibraries.MOPUB)
                                 .build();

        unenforceableRequest =
                PermissionRequest.builder()
                                 .setPackageName(YELP)
                                 .setPermission(Manifest.permission.READ_CALENDAR)
                                 .setPurpose(Purposes.RUNNING_OTHER_FEATURES.name)
                                 .build();
    }

    @AfterClass
    public static void teardown() {
        policyManager.syncActivatePolicyProfile(PolicyProfile.DEFAULT);
        TestUtils.pause(100);
        repository.syncUninstallApp(YELP);
        TestUtils.pause(100);
    }

    @Test
    public void testProfile_yieldsWhenNotActive() {
        policyManager.syncActivatePolicyProfile(PolicyProfile.DEFAULT);

        PolicyEnforcement policyProfile =
                new PolicyProfileCheck(new PolicyStub(enforceableRequest));

        assertEquals(EnforcementStatus.Code.SUCCESS, policyProfile.isAllowed());
        assertFalse(policyProfile.didTerminate());
    }

    @Test
    public void testProfile_enforcesProfilePolicyWhenActive() {
        policyManager.syncActivatePolicyProfile(ORGANIZATIONAL_PROFILE);
        
        UserPolicy thisPolicyWillBeDeniedIfThisCheckWorks =
                UserPolicy.createAppPolicy(enforceableRequest.packageName,
                                           enforceableRequest.permission,
                                           enforceableRequest.purpose,
                                           enforceableRequest.thirdPartyLibrary);

        thisPolicyWillBeDeniedIfThisCheckWorks.allow();

        PolicyManager.getInstance()
                     .syncUpdate(thisPolicyWillBeDeniedIfThisCheckWorks);

        PolicyEnforcement policyProfile =
                new PolicyProfileCheck(new PolicyStub(enforceableRequest));

        assertEquals(EnforcementStatus.Code.POLICYPROFILE_DENIED, policyProfile.isAllowed());
        assertTrue(policyProfile.didTerminate());
    }

    @Test
    public void testProfile_yieldsWhenItDoesNotHaveAPolicyForThisRequest() {
        policyManager.syncActivatePolicyProfile(ORGANIZATIONAL_PROFILE);

        PolicyEnforcement policyProfile =
                new PolicyProfileCheck(new PolicyStub(unenforceableRequest));

        assertEquals(EnforcementStatus.Code.SUCCESS, policyProfile.isAllowed());
        assertFalse(policyProfile.didTerminate());
    }
}