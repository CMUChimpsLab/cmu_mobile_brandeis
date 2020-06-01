package edu.cmu.policymanager.ui.configure.profiles.add;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ActivityAddProfileStepFour extends Activity {
    private final Activity mActivityReference = this;
    private final Context mActivityContext = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new IntentIntegrator(mActivityReference).initiateScan();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if(result != null) {
            Intent nextStep = new Intent(mActivityContext, ActivityAddProfileStepFive.class);
            startActivity(nextStep);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}