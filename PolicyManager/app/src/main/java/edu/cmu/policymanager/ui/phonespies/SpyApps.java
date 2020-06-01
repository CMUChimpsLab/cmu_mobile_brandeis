package edu.cmu.policymanager.ui.phonespies;

import java.util.Arrays;

/**
 * Created by Mike Czapik (Carnegie Mellon University) on 1/9/2019.
 */

public final class SpyApps {
    private static final String[] appsThatSpy = {
            "com.hellospy.system"
    };

    public static boolean appIsSpying(final String packageName) {
        return Arrays.asList(appsThatSpy).contains(packageName);
    }

    public static String getRealName(final String packageName) {
        if(Arrays.asList(appsThatSpy).contains(packageName)) {
            return "HelloSpy";
        }

        return "Unknown spy app";
    }
}