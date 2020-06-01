package edu.cmu.policymanager.policychecker.tests;

public class TestUtils {
    /* If you don't pause, then lastUpdated between policies end up being equal
       when the test is actually run. May also be needed to wait on some async
       methods.
     */
    public static void pause(final long millis) {
        try {
            Thread.sleep(millis);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
