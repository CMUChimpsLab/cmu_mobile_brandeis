package edu.cmu.policymanager.policychecker.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        StackTraceAnalysisTests.class,
        PolicyProfileTests.class,
        QuickSettingsTest.class,
        UserSettingsTest.class,
        AskLogicTests.class,
        FullAlgorithmTests.class
})
public class PolicyEnforcementTestSuite { }