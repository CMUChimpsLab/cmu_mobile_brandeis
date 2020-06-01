package edu.cmu.policymanager.policychecker.tests;

import android.Manifest;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import edu.cmu.policymanager.DataRepository.DataRepository;
import edu.cmu.policymanager.DataRepository.db.model.PolicyProfile;
import edu.cmu.policymanager.DataRepository.db.model.PolicyProfileSetting;
import edu.cmu.policymanager.PolicyManager.PolicyManager;
import edu.cmu.policymanager.PolicyManager.enforcement.EnforcementStatus;
import edu.cmu.policymanager.PolicyManager.enforcement.PermissionRequest;
import edu.cmu.policymanager.PolicyManager.enforcement.PolicyEnforcement;
import edu.cmu.policymanager.PolicyManager.enforcement.PolicyProfileCheck;
import edu.cmu.policymanager.PolicyManager.enforcement.PolicyStub;
import edu.cmu.policymanager.PolicyManager.enforcement.QuickSettingCheck;
import edu.cmu.policymanager.PolicyManager.enforcement.StackTraceAnalysis;
import edu.cmu.policymanager.PolicyManager.enforcement.UserSettingCheck;
import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.PolicyManager.purposes.Purposes;
import edu.cmu.policymanager.PolicyManager.sensitivedata.DangerousPermissions;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class FullAlgorithmTests {
    public static final String STACKTRACE_TEST_APP = "edu.cmu.chimpslab.stacktracetest";
    private static DataRepository repository;
    private static PolicyManager policyManager;

    @BeforeClass
    public static void startup() {
        Context context = InstrumentationRegistry.getTargetContext();

        DataRepository.init(context, DataRepository.StorageType.IN_MEMORY);
        repository = DataRepository.fromMemory();
        policyManager = PolicyManager.getInstance();

        repository.syncLogInstallInfo(STACKTRACE_TEST_APP, ODPFactory.createODPString());
        policyManager.syncInstallPolicyProfile(PolicyProfile.ORGANIZATIONAL);

        UserPolicy policy = UserPolicy.createAppPolicy(STACKTRACE_TEST_APP,
                                                       DangerousPermissions.RECORD_AUDIO,
                                                       Purposes.RUNNING_OTHER_FEATURES,
                                                       null);

        PolicyManager.getInstance().syncAdd(policy);
    }

    @AfterClass
    public static void teardown() {
        policyManager.syncActivatePolicyProfile(PolicyProfile.DEFAULT);
        repository.syncUninstallApp(STACKTRACE_TEST_APP);
    }

    @Test
    public void testStackTraceAnalysis_findsInconsistentPurposesAndPromptsUser() {
        policyManager.syncActivatePolicyProfile(PolicyProfile.DEFAULT);

        PermissionRequest request =
                PermissionRequest.builder()
                                 .setPackageName(STACKTRACE_TEST_APP)
                                 .setPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                                 .setPurpose(Purposes.ADD_LOCATION_TO_PHOTO.name)
                                 .setStacktraces(StackTraceFactory.createAdvertisingStacktrace())
                                 .build();

        PermissionRequest analyzedRequest = StackTraceAnalysis.inferPurposeAndLibrary(request);

        PolicyEnforcement stacktraceAlgorithm = new PolicyProfileCheck(
                new QuickSettingCheck(
                        new UserSettingCheck(
                                new PolicyStub(analyzedRequest)
                        )
                )
        );

        assertEquals(
                EnforcementStatus.Code.USERSETTING_ASK,
                stacktraceAlgorithm.isAllowed()
        );
    }

    @Test
    public void testUserProfile_isInstalledAndBlocksPermissionRequest() {
        policyManager.syncActivatePolicyProfile(PolicyProfile.ORGANIZATIONAL);

        PermissionRequest request =
                PermissionRequest.builder()
                                 .setPackageName(STACKTRACE_TEST_APP)
                                 .setPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                                 .setStacktraces(StackTraceFactory.createAdvertisingStacktrace())
                                 .build();

        PermissionRequest analyzedRequest = StackTraceAnalysis.inferPurposeAndLibrary(request);

        PolicyEnforcement policyProfileAlgorithm = new PolicyProfileCheck(
                new QuickSettingCheck(
                        new UserSettingCheck(
                                new PolicyStub(analyzedRequest)
                        )
                )
        );

        assertEquals(
                EnforcementStatus.Code.POLICYPROFILE_DENIED,
                policyProfileAlgorithm.isAllowed()
        );
    }

    @Test
    public void testQuickSettings_microphoneIsDisabledAndBlocksPermissionRequest() {
        policyManager.syncActivatePolicyProfile(PolicyProfile.ORGANIZATIONAL);
        QuickSettingCheck.quickSettings.put(Manifest.permission.RECORD_AUDIO, false);

        List<StackTraceElement[]> stacktraces =
                StackTraceFactory.createInternalUseMicrophoneStacktrace();

        PermissionRequest request =
                PermissionRequest.builder()
                                 .setPackageName(STACKTRACE_TEST_APP)
                                 .setPermission(Manifest.permission.RECORD_AUDIO)
                                 .setStacktraces(stacktraces)
                                 .build();

        PermissionRequest analyzedRequest = StackTraceAnalysis.inferPurposeAndLibrary(request);

        PolicyEnforcement quickSettingAlgorithm = new PolicyProfileCheck(
                new QuickSettingCheck(
                        new UserSettingCheck(
                                new PolicyStub(analyzedRequest)
                        )
                )
        );

        assertEquals(
                EnforcementStatus.Code.QUICKSETTING_DISABLED,
                quickSettingAlgorithm.isAllowed()
        );
    }

    @Test
    public void testWholeInspectionChain_requestPropagatesToUserSettingCheckThenAllowsIt() {
        policyManager.syncActivatePolicyProfile(PolicyProfile.ORGANIZATIONAL);
        QuickSettingCheck.quickSettings.put(Manifest.permission.RECORD_AUDIO, true);

        List<StackTraceElement[]> stacktraces =
                StackTraceFactory.createInternalUseMicrophoneStacktrace();

        PermissionRequest request =
                PermissionRequest.builder()
                                 .setPackageName(STACKTRACE_TEST_APP)
                                 .setPermission(Manifest.permission.RECORD_AUDIO)
                                 .setStacktraces(stacktraces)
                                 .build();

        PermissionRequest analyzedRequest = StackTraceAnalysis.inferPurposeAndLibrary(request);

        PolicyEnforcement userSettingAlgorithm = new PolicyProfileCheck(
                new QuickSettingCheck(
                        new UserSettingCheck(
                                new PolicyStub(analyzedRequest)
                        )
                )
        );

        assertEquals(EnforcementStatus.Code.USERSETTING_ALLOWED, userSettingAlgorithm.isAllowed());
    }
}