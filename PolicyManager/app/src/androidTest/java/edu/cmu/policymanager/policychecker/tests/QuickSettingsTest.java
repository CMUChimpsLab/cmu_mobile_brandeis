package edu.cmu.policymanager.policychecker.tests;

import android.Manifest;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.cmu.policymanager.DataRepository.DataRepository;
import edu.cmu.policymanager.PolicyManager.enforcement.EnforcementStatus;
import edu.cmu.policymanager.PolicyManager.enforcement.PermissionRequest;
import edu.cmu.policymanager.PolicyManager.enforcement.PolicyEnforcement;
import edu.cmu.policymanager.PolicyManager.enforcement.PolicyStub;
import edu.cmu.policymanager.PolicyManager.enforcement.QuickSettingCheck;

import static edu.cmu.policymanager.PolicyManager.enforcement.QuickSettingCheck.quickSettings;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class QuickSettingsTest {
    public static final String YELP = "com.android.yelp";
    private static DataRepository repository;

    @BeforeClass
    public static void setup() {
        Context context = InstrumentationRegistry.getTargetContext();

        DataRepository.init(context, DataRepository.StorageType.IN_MEMORY);
        repository = DataRepository.fromMemory();
    }

    @AfterClass
    public static void teardown() {
        repository.syncUninstallApp(YELP);
        TestUtils.pause(100);
    }

    @Test
    public void testQuickSetting_allowsAccessOnEnable() {
        quickSettings.put(Manifest.permission.RECORD_AUDIO, true);

        PermissionRequest request = PermissionRequest.builder()
                                                     .setPackageName(YELP)
                                                     .setPermission(Manifest.permission.RECORD_AUDIO)
                                                     .build();

        PolicyEnforcement quickSetting = new QuickSettingCheck(
                new PolicyStub(request)
        );

        assertEquals(EnforcementStatus.Code.SUCCESS, quickSetting.isAllowed());
        assertFalse(quickSetting.didTerminate());
    }

    @Test
    public void testQuickSetting_deniesOnDisable() {
        quickSettings.put(Manifest.permission.CAMERA, false);

        PermissionRequest request = PermissionRequest.builder()
                                                     .setPackageName(YELP)
                                                     .setPermission(Manifest.permission.CAMERA)
                                                     .build();

        PolicyEnforcement quickSetting = new QuickSettingCheck(
                new PolicyStub(request)
        );

        assertEquals(EnforcementStatus.Code.QUICKSETTING_DISABLED, quickSetting.isAllowed());
        assertTrue(quickSetting.didTerminate());
    }
}