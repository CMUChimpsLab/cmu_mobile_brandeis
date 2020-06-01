package edu.cmu.policymanager.policychecker.tests;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.cmu.policymanager.DataRepository.DataRepository;
import edu.cmu.policymanager.PolicyManager.PolicyManager;
import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;
import edu.cmu.policymanager.PolicyManager.purposes.Purposes;
import edu.cmu.policymanager.PolicyManager.sensitivedata.DangerousPermissions;

import static junit.framework.TestCase.assertTrue;

/**
 * Tests the PolicyManager's interfaces and code for dealing with Runtime UI prompts. Ensure
 * that temporary decisions are enforced, within the given timespan and creates a log
 * if none are already present.
 * */
@RunWith(AndroidJUnit4.class)
public class AskLogicTests {
    public static final String YELP = "com.android.yelp";

    private static DataRepository repository;
    private static UserPolicy askPolicy;

    private static final long ASK_TIMER = 500;

    @BeforeClass
    public static void setup() {
        Context context = InstrumentationRegistry.getContext();

        DataRepository.init(context, DataRepository.StorageType.IN_MEMORY);
        repository = DataRepository.fromMemory();
        repository.syncLogInstallInfo(YELP, ODPFactory.createODPString());

        PolicyManager.getInstance().setTimeframeAskResultIsValid(ASK_TIMER);

        askPolicy = UserPolicy.createAppPolicy(
                YELP,
                DangerousPermissions.FINE_LOCATION,
                Purposes.SECURING_DEVICE,
                null
        );

        askPolicy.ask();
        PolicyManager.getInstance().syncUpdate(askPolicy);
        TestUtils.pause(100);
    }

    @AfterClass
    public static void teardown() {
        repository.syncUninstallApp(YELP);
        TestUtils.pause(100);
    }

    private void denyThenLog() {
        UserPolicy denyPolicy = UserPolicy.createAppPolicy(
                YELP,
                DangerousPermissions.FINE_LOCATION,
                Purposes.SECURING_DEVICE,
                null
        );

        denyPolicy.deny();
        PolicyManager.getInstance().syncLogPolicyForAskPrompt(denyPolicy);
        TestUtils.pause(100);
    }

    @Test
    public void testAskLogic_insertsEntryWhenUserProvidesPolicyDecisionAndReturnsDecision() {
        UserPolicy result = PolicyManager.getInstance().syncRequestEnforcedPolicy(askPolicy);
        assertTrue(result.isAsk());

        denyThenLog();

        result = PolicyManager.getInstance().syncRequestEnforcedPolicy(askPolicy);
        assertTrue(result.isDenied());
    }

    @Test
    public void testAskLogic_persistsTheUsersDecisionAndEnforcesIt() {
        UserPolicy allowPolicy = UserPolicy.createAppPolicy(
                YELP,
                DangerousPermissions.FINE_LOCATION,
                Purposes.SECURING_DEVICE,
                null
        );

        allowPolicy.deny();
        PolicyManager.getInstance().syncUpdate(allowPolicy);
        TestUtils.pause(100);

        UserPolicy result = PolicyManager.getInstance().syncRequestEnforcedPolicy(askPolicy);
        assertTrue(result.isDenied());
    }

    @Test
    public void testAskLogic_presentsTheUserWithPromptWhenTemporaryPolicyExpires() {
        TestUtils.pause(ASK_TIMER);
        PolicyManager.getInstance().syncUpdate(askPolicy);
        TestUtils.pause(100);

        UserPolicy result = PolicyManager.getInstance().syncRequestEnforcedPolicy(askPolicy);
        assertTrue(result.isAsk());

        denyThenLog();

        result = PolicyManager.getInstance().syncRequestEnforcedPolicy(askPolicy);
        assertTrue(result.isDenied());

        TestUtils.pause(ASK_TIMER);

        result = PolicyManager.getInstance().syncRequestEnforcedPolicy(askPolicy);
        assertTrue(result.isAsk());
    }
}