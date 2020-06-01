package edu.cmu.policymanager.DataRepository.network;

import android.content.Context;
import android.util.JsonReader;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import edu.cmu.policymanager.DataRepository.DataRepository;
import edu.cmu.policymanager.R;

import static edu.cmu.policymanager.application.PolicyManagerApplication.KEY_APPLICATION;

/**
 * Emulates the Google Play store. Ideally, we would pull app policies (or off-device policies
 * alternatively) from a trusted and verified source. Since we don't have direct access to
 * the Play store, we emulate it here.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class GooglePlayStore {
    private static GooglePlayStore playStore;
    private final Context context;

    public static String KEY_ARE_POLICIES_INSTALLED = "arePoliciesInstalled";

    private GooglePlayStore(final Context context) {
        this.context = context;
    }

    public static GooglePlayStore getInstance(final Context context) {
        if(playStore == null) { playStore = new GooglePlayStore(context); }

        return playStore;
    }

    /**
     * Grab app policies from external resource file, and add each JSON string to the
     * DataRepository.
     * */
    public void installAppPolicies() {
        InputStream stream = context.getResources().openRawResource(R.raw.play_store_app_policies);
        JsonReader streamReader = new JsonReader(new InputStreamReader(stream));

        try {
            streamReader.beginArray();

            while(streamReader.hasNext()) {
                JsonObject appPolicy = readAppPolicyFromStream(streamReader);

                String packageName = appPolicy.get("package").getAsString();
                String jsonString = appPolicy.get("policies").toString();

                DataRepository.getInstance().addODP(packageName, jsonString);
            }

            streamReader.endArray();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        Map<String, String> installCompleted = new HashMap<String, String>();
        installCompleted.putIfAbsent(KEY_ARE_POLICIES_INSTALLED, "true");

        DataRepository.getInstance().addMetadata(KEY_APPLICATION, installCompleted);
    }

    private JsonObject readAppPolicyFromStream(final JsonReader reader) {
        JsonObject appPolicy = new JsonObject();
        JsonArray policies = new JsonArray();

        try {
            reader.beginObject();
            String keyPolicies = reader.nextName();

            reader.beginArray();

            while(reader.hasNext()) {
                reader.beginObject();
                JsonObject policy = new JsonObject();

                while(reader.hasNext()) {
                    policy.addProperty(reader.nextName(), reader.nextString());
                }

                policies.add(policy);
                reader.endObject();
            }

            appPolicy.add(keyPolicies, policies);
            reader.endArray();

            String keyPackageName = reader.nextName(),
                   valuePackageName = reader.nextString();
            appPolicy.addProperty(keyPackageName, valuePackageName);

            String keyCategoryName = reader.nextName(),
                   valueCategoryName = reader.nextString();

            reader.endObject();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        return appPolicy;
    }
}