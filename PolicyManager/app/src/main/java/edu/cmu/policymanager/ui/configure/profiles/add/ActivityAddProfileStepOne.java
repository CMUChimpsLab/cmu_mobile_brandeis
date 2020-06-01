package edu.cmu.policymanager.ui.configure.profiles.add;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import edu.cmu.policymanager.R;

/**
 * Explains to the user what policy profiles are, and begins
 * the first step in adding a profile.
 * */
public class ActivityAddProfileStepOne extends Activity {
    private Context mActivityContext = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_add_step_one);
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

    public void toStepTwo(View v) {
        Intent nextStep = new Intent(mActivityContext, ActivityAddProfileStepTwo.class);
        startActivity(nextStep);
    }

    public void exitPolicyProfileAdd(View v) { finish(); }
}