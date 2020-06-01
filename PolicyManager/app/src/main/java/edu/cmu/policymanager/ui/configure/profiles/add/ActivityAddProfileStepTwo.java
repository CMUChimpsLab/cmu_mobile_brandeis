package edu.cmu.policymanager.ui.configure.profiles.add;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import edu.cmu.policymanager.R;
import edu.cmu.policymanager.ui.configure.profiles.ActivityPolicyProfileMain;

public class ActivityAddProfileStepTwo extends Activity {
    private Context mActivityContext = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_add_step_two);
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

    public void toStepThree(View v) {
        Intent stepThree = new Intent(mActivityContext, ActivityAddProfileStepThree.class);
        startActivity(stepThree);
    }

    public void back(View v) {
        finish();
    }

    public void cancel(View v) {
        Intent profileMainActivity = new Intent(mActivityContext, ActivityPolicyProfileMain.class);
        startActivity(profileMainActivity);
    }
}