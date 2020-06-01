package edu.cmu.policymanager.application;

import android.app.Application;
import android.content.Context;

import java.util.Map;

import edu.cmu.policymanager.DataRepository.DataRepository;
import edu.cmu.policymanager.DataRepository.network.GooglePlayStore;
import edu.cmu.policymanager.PolicyManager.PolicyManager;
import edu.cmu.policymanager.R;

import static edu.cmu.policymanager.DataRepository.network.GooglePlayStore.KEY_ARE_POLICIES_INSTALLED;

/**
 * Created by Mike Czapik (Carnegie Mellon University) on 6/5/2018.
 *
 * Initializes the policy manager application. Anything that should be set up before the default
 * UI loads should be done here.
 */

public class PolicyManagerApplication extends Application {
    public static final String KEY_APPLICATION = "edu.cmu.brandeis.policymanager.application";

    public final Context applicationContext = this;

    public static final String SYMBOL_ALL = "*";
    public static final int CATEGORY_NATIVE = -2;

    public static final UIManager ui = new UIManager();

    @Override public void onCreate() {
        super.onCreate();

        DataRepository.init(applicationContext, DataRepository.StorageType.DISK);
        ui.setContext(applicationContext);

        final long FIVE_MINUTES = (5 * (60 * 1000));
        PolicyManager.getInstance().setTimeframeAskResultIsValid(FIVE_MINUTES);

        Map<String, String> metadata =
                DataRepository.fromDisk().getMetadataByOwner(KEY_APPLICATION);

        if(metadata == null || !metadata.containsKey(KEY_ARE_POLICIES_INSTALLED)) {
            GooglePlayStore.getInstance(applicationContext).installAppPolicies();
        }

        ui.setDefaultUI(new BrandeisUI(applicationContext, R.id.common_ui_allysiqi));
    }

    /*
        https://stackoverflow.com/questions/21367646/how-to-determine-if-android-application-is-started-with-junit-testing-instrument?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
     */
    private boolean isUnderTest() {
        try {
            Class.forName("edu.cmu.policymanager.PolicyManagerTest");
            return true;
        }
        catch(ClassNotFoundException cnf) { }

        return false;
    }
}