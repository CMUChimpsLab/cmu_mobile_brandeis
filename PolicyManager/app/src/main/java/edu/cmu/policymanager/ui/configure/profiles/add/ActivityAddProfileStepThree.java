package edu.cmu.policymanager.ui.configure.profiles.add;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import edu.cmu.policymanager.R;
import edu.cmu.policymanager.ui.configure.profiles.ActivityPolicyProfileMain;

public class ActivityAddProfileStepThree extends Activity {
    private Context mActivityContext = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_profile_step_three);
    }

    @Override
    public void onStart() { super.onStart(); }

    @Override
    public void onResume() { super.onResume(); }

    @Override
    public void onPause() { super.onPause(); }

    @Override
    public void onStop() { super.onStop(); }

    @Override
    public void onDestroy() { super.onDestroy(); }

    public void back(View v) {
        finish();
    }

    public void toStepFour(View v) {
        Intent stepFour = new Intent(mActivityContext, ActivityAddProfileStepFour.class);
        startActivity(stepFour);
    }

    public void cancel(View v) {
        Intent profileMainActivity = new Intent(mActivityContext, ActivityPolicyProfileMain.class);
        startActivity(profileMainActivity);
    }
}