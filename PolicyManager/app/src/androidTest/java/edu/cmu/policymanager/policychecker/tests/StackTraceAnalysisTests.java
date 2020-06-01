package edu.cmu.policymanager.policychecker.tests;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Stack;

import edu.cmu.policymanager.DataRepository.DataRepository;
import edu.cmu.policymanager.PolicyManager.enforcement.EnforcementStatus;
import edu.cmu.policymanager.PolicyManager.enforcement.PermissionRequest;
import edu.cmu.policymanager.PolicyManager.enforcement.PolicyEnforcement;
import edu.cmu.policymanager.PolicyManager.enforcement.PolicyStub;
import edu.cmu.policymanager.PolicyManager.enforcement.StackTraceAnalysis;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibraries;
import edu.cmu.policymanager.PolicyManager.purposes.Purpose;
import edu.cmu.policymanager.PolicyManager.purposes.Purposes;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class StackTraceAnalysisTests {
    public static final String YELP = "com.android.yelp",
                               FACEBOOK = "com.facebook.katana";
    private static DataRepository repository;

    @BeforeClass
    public static void setup() {
        Context context = InstrumentationRegistry.getTargetContext();
        DataRepository.init(context, DataRepository.StorageType.IN_MEMORY);
        repository = DataRepository.fromMemory();
        repository.syncLogInstallInfo(YELP, ODPFactory.createODPString());
        repository.syncLogInstallInfo(FACEBOOK, ODPFactory.createODPFromManifest());
    }

    @AfterClass
    public static void teardown() {
        repository.syncUninstallApp(YELP);
        TestUtils.pause(100);
    }

    @Test
    public void testStacktraceAnalysis_infersGenericPurposeWhenNoStacktracesAreAvailable() {
        PermissionRequest noStacktraces =
                PermissionRequest.builder()
                                 .setPackageName(YELP)
                                 .setPermission(Manifest.permission.CAMERA)
                                 .build();

        PermissionRequest analyzedRequest =
                StackTraceAnalysis.inferPurposeAndLibrary(noStacktraces);

        assertEquals(Purposes.RUNNING_OTHER_FEATURES, analyzedRequest.purpose);
        assertEquals(
                ThirdPartyLibraries.CATEGORY_APP_INTERNAL_USE,
                analyzedRequest.thirdPartyLibrary
        );
    }

    @Test
    public void testStacktraceAnalysis_infersGenericPurposeWhenPurposeStringIsEmpty() {
        PermissionRequest emptyPurpose =
                PermissionRequest.builder()
                                 .setPackageName(YELP)
                                 .setPermission(Manifest.permission.CAMERA)
                                 .setPurpose("")
                                 .setStacktraces(StackTraceFactory.createNoPurposeStackraces())
                                 .build();

        PermissionRequest analyzedRequest =
                StackTraceAnalysis.inferPurposeAndLibrary(emptyPurpose);

        assertEquals(Purposes.RUNNING_OTHER_FEATURES, analyzedRequest.purpose);
        assertEquals(
                ThirdPartyLibraries.CATEGORY_APP_INTERNAL_USE,
                analyzedRequest.thirdPartyLibrary
        );
    }

    @Test
    public void testStacktraceAnalysis_infersGenericPurposeWhenPurposeIsUndetermined() {
        PermissionRequest noPurpose =
                PermissionRequest.builder()
                                 .setPackageName(YELP)
                                 .setPermission(Manifest.permission.CAMERA)
                                 .setStacktraces(StackTraceFactory.createNoPurposeStackraces())
                                 .build();

        PermissionRequest analyzedRequest = StackTraceAnalysis.inferPurposeAndLibrary(noPurpose);

        assertEquals(Purposes.RUNNING_OTHER_FEATURES, analyzedRequest.purpose);
        assertEquals(
                ThirdPartyLibraries.CATEGORY_APP_INTERNAL_USE,
                analyzedRequest.thirdPartyLibrary
        );
    }

    @Test
    public void testStacktraceAnalysis_infersLibraryWhenLibraryIsFoundButIsNotInODP() {
        PermissionRequest mopubRequest =
                PermissionRequest.builder()
                        .setPackageName(YELP)
                        .setPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                        .setStacktraces(StackTraceFactory.createAdvertisingStacktrace())
                        .build();

        PermissionRequest analyzedRequest = StackTraceAnalysis.inferPurposeAndLibrary(mopubRequest);

        assertEquals(Purposes.DISPLAY_ADVERTISEMENT, analyzedRequest.purpose);
        assertEquals(ThirdPartyLibraries.MOPUB, analyzedRequest.thirdPartyLibrary);
    }

    @Test
    public void testStacktraceAnalysis_infersLibraryFromTopActivityWhenStacktracesAreAbsent() {
        ComponentName topActivity =
                new ComponentName(FACEBOOK, "com.google.android.gms.ads.AdActivity");

        PermissionRequest admobRequest =
                PermissionRequest.builder()
                                 .setPackageName(FACEBOOK)
                                 .setPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                                 .setTopActivity(topActivity)
                                 .build();

        PermissionRequest analyzedRequest =
                StackTraceAnalysis.inferPurposeAndLibrary(admobRequest);

        assertEquals(Purposes.DISPLAY_ADVERTISEMENT, analyzedRequest.purpose);
        assertEquals(ThirdPartyLibraries.ADMOB, analyzedRequest.thirdPartyLibrary);
    }

    @Test
    public void testStacktraceAnalysis_infersLibraryWhenFoundInODP() {
        PermissionRequest mopubRequest =
                PermissionRequest.builder()
                                 .setPackageName(YELP)
                                 .setPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                                 .setStacktraces(StackTraceFactory.createAdvertisingStacktrace())
                                 .build();

        PermissionRequest analyzedRequest = StackTraceAnalysis.inferPurposeAndLibrary(mopubRequest);

        assertEquals(Purposes.DISPLAY_ADVERTISEMENT, analyzedRequest.purpose);
        assertEquals(ThirdPartyLibraries.MOPUB, analyzedRequest.thirdPartyLibrary);
    }

    @Test
    public void testStacktraceAnalysis_matchesAccessToODPBuiltFromManifest() {
        List<StackTraceElement[]> stacktraces =
                StackTraceFactory.createInternalUseMicrophoneStacktrace();

        PermissionRequest request =
                PermissionRequest.builder()
                                 .setPackageName(FACEBOOK)
                                 .setPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                                 .setStacktraces(stacktraces)
                                 .build();

        PermissionRequest analyzedRequest = StackTraceAnalysis.inferPurposeAndLibrary(request);

        assertEquals(Purposes.SECURING_DEVICE, analyzedRequest.purpose);
        assertEquals(
                ThirdPartyLibraries.CATEGORY_APP_INTERNAL_USE,
                analyzedRequest.thirdPartyLibrary
        );
    }

    @Test
    public void testStacktraceAnalysis_doesNotMatchAccessNotFoundInODPBuiltFromManifest() {
        List<StackTraceElement[]> stacktraces =
                StackTraceFactory.createInternalUseMicrophoneStacktrace();

        PermissionRequest request =
                PermissionRequest.builder()
                                 .setPackageName(FACEBOOK)
                                 .setPermission(Manifest.permission.READ_CONTACTS)
                                 .setStacktraces(stacktraces)
                                 .build();

        PermissionRequest analyzedRequest = StackTraceAnalysis.inferPurposeAndLibrary(request);

        assertEquals(Purposes.RUNNING_OTHER_FEATURES, analyzedRequest.purpose);
        assertEquals(
                ThirdPartyLibraries.CATEGORY_APP_INTERNAL_USE,
                analyzedRequest.thirdPartyLibrary
        );
    }

    @Test
    public void testStacktraceAnalysis_usesProgrammedPurposeWhenODPIsNotPresent() {
        PermissionRequest request =
                PermissionRequest.builder()
                                 .setPackageName("com.weather.Weather")
                                 .setPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                                 .setPurpose("Securing Device")
                                 .build();

        PermissionRequest analyzedRequest = StackTraceAnalysis.inferPurposeAndLibrary(request);

        assertEquals(Purposes.SECURING_DEVICE, analyzedRequest.purpose);
        assertEquals(
                ThirdPartyLibraries.CATEGORY_APP_INTERNAL_USE,
                analyzedRequest.thirdPartyLibrary
        );
    }
}