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

import java.util.List;

import edu.cmu.policymanager.DataRepository.DataRepository;
import edu.cmu.policymanager.DataRepository.db.model.PolicyProfile;
import edu.cmu.policymanager.PolicyManager.PolicyManager;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibraries;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibrary;
import edu.cmu.policymanager.PolicyManager.enforcement.EnforcementStatus;
import edu.cmu.policymanager.PolicyManager.enforcement.PermissionRequest;
import edu.cmu.policymanager.PolicyManager.enforcement.PolicyEnforcement;
import edu.cmu.policymanager.PolicyManager.enforcement.PolicyStub;
import edu.cmu.policymanager.PolicyManager.enforcement.UserSettingCheck;
import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.PolicyManager.purposes.Purposes;
import edu.cmu.policymanager.PolicyManager.sensitivedata.DangerousPermissions;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class UserSettingsTest {
    public static final String YELP = "com.android.yelp";

    private static UserPolicy globalSetting, appSetting, globalLibraryCategorySetting;
    private static PermissionRequest yelpRequest, yelpLibraryRequest;

    private static DataRepository repository;

    @BeforeClass
    public static void setup() {
        Context context = InstrumentationRegistry.getTargetContext();

        DataRepository.init(context, DataRepository.StorageType.IN_MEMORY);
        repository = DataRepository.fromMemory();
        repository.syncLogInstallInfo(YELP, ODPFactory.createODPString());

        globalLibraryCategorySetting = UserPolicy.createGlobalPolicy(
                DangerousPermissions.FINE_LOCATION,
                Purposes.ALL,
                ThirdPartyLibraries.MOPUB
        );

        globalSetting = UserPolicy.createGlobalPolicy(
                DangerousPermissions.FINE_LOCATION,
                Purposes.ALL,
                ThirdPartyLibraries.ALL
        );

        appSetting = UserPolicy.createAppPolicy(
                YELP,
                DangerousPermissions.FINE_LOCATION,
                Purposes.DISPLAY_ADVERTISEMENT,
                ThirdPartyLibraries.FLURRY
        );

        PolicyManager.getInstance().syncAdd(globalSetting);
        PolicyManager.getInstance().syncAdd(appSetting);
        PolicyManager.getInstance().syncAdd(globalLibraryCategorySetting);

        yelpRequest = PermissionRequest.builder()
                                       .setPackageName(YELP)
                                       .setPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                                       .setPurpose(Purposes.DISPLAY_ADVERTISEMENT.name)
                                       .setLibrary(ThirdPartyLibraries.FLURRY)
                                       .build();

        yelpLibraryRequest =
                PermissionRequest.builder()
                                 .setPackageName(YELP)
                                 .setPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                                 .setPurpose(Purposes.DISPLAY_ADVERTISEMENT.name)
                                 .setLibrary(ThirdPartyLibraries.MOPUB)
                                 .build();
    }

    @AfterClass
    public static void teardown() {
        repository.syncUninstallApp(YELP);
        repository.syncRemoveAppPolicy(globalSetting);
        repository.syncRemoveAppPolicy(globalLibraryCategorySetting);
        TestUtils.pause(100);
    }

    @Test
    public void testUserPolicy_enforcesMostRecentSetting() {
        TestUtils.pause(10);
        globalSetting.deny();
        TestUtils.pause(50);
        appSetting.allow();

        PolicyManager.getInstance().syncUpdate(globalSetting);
        PolicyManager.getInstance().syncUpdate(appSetting);

        PolicyEnforcement userSetting = new UserSettingCheck(
                new PolicyStub(yelpRequest)
        );

        assertEquals(EnforcementStatus.Code.USERSETTING_ALLOWED, userSetting.isAllowed());
        assertTrue(userSetting.didTerminate());
    }

    @Test
    public void testUserPolicy_isAllowed() {
        TestUtils.pause(10);
        appSetting.allow();

        PolicyManager.getInstance().syncUpdate(appSetting);

        PolicyEnforcement userSetting = new UserSettingCheck(
                new PolicyStub(yelpRequest)
        );

        assertEquals(EnforcementStatus.Code.USERSETTING_ALLOWED, userSetting.isAllowed());
        assertTrue(userSetting.didTerminate());
    }

    @Test
    public void testUserPolicy_isDenied() {
        TestUtils.pause(10);
        appSetting.deny();

        PolicyManager.getInstance().syncUpdate(appSetting);

        PolicyEnforcement userSetting = new UserSettingCheck(
                new PolicyStub(yelpRequest)
        );

        assertEquals(EnforcementStatus.Code.USERSETTING_DENIED, userSetting.isAllowed());
        assertTrue(userSetting.didTerminate());
    }

    @Test
    public void testUserPolicy_wouldStartsRuntimeUIIfPolicyIsAsk() {
        TestUtils.pause(10);
        appSetting.ask();

        PolicyManager.getInstance().syncUpdate(appSetting);

        PolicyEnforcement userSetting = new UserSettingCheck(
                new PolicyStub(yelpRequest)
        );

        assertEquals(EnforcementStatus.Code.USERSETTING_ASK, userSetting.isAllowed());
        assertTrue(userSetting.didTerminate());
    }

    @Test
    public void testUserPolicy_enforcesGlobalSettingWhenItIsMostRecentlyConfigured() {
        TestUtils.pause(10);
        appSetting.allow();
        TestUtils.pause(50);
        globalSetting.deny();

        PolicyManager.getInstance().syncUpdate(appSetting);
        PolicyManager.getInstance().syncUpdate(globalSetting);

        PolicyEnforcement userSetting = new UserSettingCheck(
                new PolicyStub(yelpRequest)
        );

        assertEquals(EnforcementStatus.Code.USERSETTING_DENIED, userSetting.isAllowed());
        assertTrue(userSetting.didTerminate());
    }

    @Test
    public void testUserPolicy_findsTheLibrarySettingEvenThoughWeUsedACategory() {
        TestUtils.pause(10);
        globalLibraryCategorySetting.deny();

        PolicyManager.getInstance().syncUpdate(globalLibraryCategorySetting);

        PolicyEnforcement userSetting = new UserSettingCheck(
                new PolicyStub(yelpLibraryRequest)
        );

        assertEquals(EnforcementStatus.Code.USERSETTING_DENIED, userSetting.isAllowed());
        assertTrue(userSetting.didTerminate());
    }
}