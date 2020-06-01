package edu.cmu.policymanager.ui.phonespies;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import edu.cmu.policymanager.R;
import edu.cmu.policymanager.ui.configure.MainActivity;

/**
 * Created by Mike Czapik (Carnegie Mellon University) on 1/9/2019.
 */

public class SpyingAppActivity extends Activity {
    private final Context activityContext = this;

    private TextView spyAppText;

    public static final String KEY_SPY_PACKAGE = "spyPackage";

    public void onCreate(Bundle savedInstanceData) {
        super.onCreate(savedInstanceData);
        setContentView(R.layout.allysiqi_activity_spies);

        spyAppText = findViewById(R.id.allysiqi_spy_name);
        String spyPackage = getIntent().getStringExtra(KEY_SPY_PACKAGE);
        spyPackage = (spyPackage == null ? "" : spyPackage);

        String template = getString(R.string.allysiqi_spy_name_text);
        String updated = template.replace("@name", SpyApps.getRealName(spyPackage));

        spyAppText.setText(updated);
    }

    public void onStart() { super.onStart(); }
    public void onResume() { super.onResume(); }
    public void onPause() { super.onPause(); }
    public void onStop() { super.onStop(); }
    public void onDestroy() {super.onDestroy(); }

    public void exitScreen(View v) {
        finish();
    }

    public void goToHomescreen(View v) {
        Intent toHomescreen = new Intent(activityContext, MainActivity.class);
        startActivity(toHomescreen);
        finish();
    }
}