package edu.cmu.policymanager.PolicyManager.sensitivedata;

import android.Manifest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.policymanager.PolicyManager.purposes.Purpose;
import edu.cmu.policymanager.PolicyManager.purposes.Purposes;
import edu.cmu.policymanager.validation.Precondition;

/**
 * Models all dangerous permissions that either PE Android traps, or
 * the policy manager supports/exposes controls for.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public final class DangerousPermissions {
    private static volatile List<Purpose> locationPurposes = new ArrayList<Purpose>() {{
        add(Purposes.DISPLAY_ADVERTISEMENT);
        add(Purposes.ANALYZE_USER_INFO);
        add(Purposes.MONITOR_HEALTH);
        add(Purposes.CONNECT_WITH_OTHER_PEOPLE);
        add(Purposes.CONDUCTING_RESEARCH);
        add(Purposes.RUNNING_OTHER_FEATURES);
        add(Purposes.NAVIGATING_TO_DESTINATION);
        add(Purposes.SEARCH_NEARBY_PLACES);
        add(Purposes.DELIVERING_LOCAL_WEATHER);
        add(Purposes.NOTIFY_EMERGENCY_SERVICES);
        add(Purposes.SECURING_DEVICE);
        add(Purposes.PLAYING_GAMES);
    }};

    private static volatile List<Purpose> calendarPurposes = new ArrayList<Purpose>() {{
        add(Purposes.CONDUCTING_RESEARCH);
        add(Purposes.RUNNING_OTHER_FEATURES);
        add(Purposes.MONITOR_HEALTH);
    }};

    private static final List<Purpose> contactPurposes = new ArrayList<Purpose>() {{
        add(Purposes.CONDUCTING_RESEARCH);
        add(Purposes.RUNNING_OTHER_FEATURES);
        add(Purposes.MONITOR_HEALTH);
        add(Purposes.CONNECT_WITH_OTHER_PEOPLE);
        add(Purposes.SECURING_DEVICE);
        add(Purposes.MESSAGING_OR_CALLING_PEOPLE);
        add(Purposes.NOTIFY_EMERGENCY_SERVICES);
    }};

    private static final List<Purpose> audioPurposes = new ArrayList<Purpose>() {{
        add(Purposes.MONITOR_HEALTH);
        add(Purposes.CONDUCTING_RESEARCH);
        add(Purposes.PLAYING_GAMES);
        add(Purposes.SECURING_DEVICE);
        add(Purposes.MESSAGING_OR_CALLING_PEOPLE);
        add(Purposes.NOTIFY_EMERGENCY_SERVICES);
        add(Purposes.RUNNING_OTHER_FEATURES);
    }};

    private static final List<Purpose> cameraPurposes = new ArrayList<Purpose>() {{
        add(Purposes.CONDUCTING_RESEARCH);
        add(Purposes.PLAYING_GAMES);
        add(Purposes.ADD_LOCATION_TO_PHOTO);
        add(Purposes.SECURING_DEVICE);
        add(Purposes.CONNECT_WITH_OTHER_PEOPLE);
        add(Purposes.RUNNING_OTHER_FEATURES);
    }};

    private static final List<Purpose> phoneStatePurposes = new ArrayList<Purpose>() {{
        add(Purposes.CONNECT_WITH_OTHER_PEOPLE);
        add(Purposes.CONDUCTING_RESEARCH);
        add(Purposes.PLAYING_GAMES);
        add(Purposes.MESSAGING_OR_CALLING_PEOPLE);
        add(Purposes.ANALYZE_USER_INFO);
        add(Purposes.NOTIFY_EMERGENCY_SERVICES);
        add(Purposes.RUNNING_OTHER_FEATURES);
    }};

    private static final List<Purpose> bodySensorPurposes = new ArrayList<Purpose>() {{
        add(Purposes.MONITOR_HEALTH);
        add(Purposes.CONDUCTING_RESEARCH);
        add(Purposes.RUNNING_OTHER_FEATURES);
    }};

    private static final List<Purpose> smsPurposes = new ArrayList<Purpose>() {{
        add(Purposes.MESSAGING_OR_CALLING_PEOPLE);
        add(Purposes.CONDUCTING_RESEARCH);
        add(Purposes.NOTIFY_EMERGENCY_SERVICES);
        add(Purposes.BACKUP_TO_CLOUD_SERVICE);
        add(Purposes.CONNECT_WITH_OTHER_PEOPLE);
        add(Purposes.RUNNING_OTHER_FEATURES);
    }};

    private static final List<Purpose> storagePurposes = new ArrayList<Purpose>() {{
        add(Purposes.BACKUP_TO_CLOUD_SERVICE);
        add(Purposes.PLAYING_GAMES);
        add(Purposes.DISPLAY_ADVERTISEMENT);
        add(Purposes.CONDUCTING_RESEARCH);
        add(Purposes.RUNNING_OTHER_FEATURES);
    }};

    /*
    * Group descriptions are either pulled from or are a mix of:
    * adb shell pm list permissions -s
    * https://developer.android.com/reference/android/Manifest.permission_group.html
    * */
    public static final SensitiveData FINE_LOCATION =
            new SensitiveData(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    "Determines a precise location using " +
                              "GPS, WiFi and mobile cell data.",
                    locationPurposes
            );
    public static final SensitiveData COARSE_LOCATION =
            new SensitiveData(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    "Location will have an accuracy approximately " +
                              "equivalent to a city block.",
                    locationPurposes
            );
    public static final SensitiveData READ_CALENDAR =
            new SensitiveData(
                    Manifest.permission.READ_CALENDAR,
                    "This app can read all calendar events stored on your phone and " +
                              "share or save your calendar data",
                    calendarPurposes
            );
    public static final SensitiveData WRITE_CALENDAR =
            new SensitiveData(
                    Manifest.permission.WRITE_CALENDAR,
                    "This app can add, remove, or change calendar events on your " +
                              "phone.",
                    calendarPurposes
            );

    public static final SensitiveData READ_CONTACTS =
            new SensitiveData(
                    Manifest.permission.READ_CONTACTS,
                    "Allows the app to read data about your contacts stored on your " +
                              "phone",
                    contactPurposes
            );
    public static final SensitiveData WRITE_CONTACTS =
            new SensitiveData(
                    Manifest.permission.WRITE_CONTACTS,
                    "Allows the app to modify the data about your contacts stored on " +
                              "your phone.",
                    contactPurposes
            );
    public static final SensitiveData GET_ACCOUNTS =
            new SensitiveData(
                    Manifest.permission.GET_ACCOUNTS,
                    "Allows the app to get the list of accounts known by the phone. ",
                    contactPurposes
            );
    public static final SensitiveData RECORD_AUDIO =
            new SensitiveData(
                    Manifest.permission.RECORD_AUDIO,
                    "This app can record audio using the microphone at any time.",
                    audioPurposes
            );
    public static final SensitiveData CAMERA =
            new SensitiveData(
                    Manifest.permission.CAMERA,
                    "This app can take pictures and record videos using the camera at " +
                              "any time.",
                    cameraPurposes
            );
    public static final SensitiveData READ_PHONE_STATE =
            new SensitiveData(
                    Manifest.permission.READ_PHONE_STATE,
                    "This permission allows the app to determine the phone number and " +
                              "device IDs, if a call is active etc.",
                    phoneStatePurposes
            );
    public static final SensitiveData READ_PHONE_NUMBERS =
            new SensitiveData(
                    Manifest.permission.READ_PHONE_NUMBERS,
                    "Allows the app to access the phone numbers of the device.",
                    phoneStatePurposes
            );
    public static final SensitiveData CALL_PHONE =
            new SensitiveData(
                    Manifest.permission.CALL_PHONE,
                    "Allows the app to call phone numbers without your intervention.",
                    phoneStatePurposes
            );
    public static final SensitiveData ANSWER_PHONE_CALLS =
            new SensitiveData(
                    Manifest.permission.ANSWER_PHONE_CALLS,
                    "Allows the app to answer an incoming phone call.",
                    phoneStatePurposes
            );
    public static final SensitiveData READ_CALL_LOG =
            new SensitiveData(
                    Manifest.permission.READ_CALL_LOG,
                    "This app can read your call history.",
                    phoneStatePurposes
            );
    public static final SensitiveData WRITE_CALL_LOG =
            new SensitiveData(
                    Manifest.permission.WRITE_CALL_LOG,
                    "Allows the app to modify your phone's call log.",
                    phoneStatePurposes
            );
    public static final SensitiveData ADD_VOICEMAIL =
            new SensitiveData(
                    Manifest.permission.ADD_VOICEMAIL,
                    "Allows the app to add messages to your voicemail inbox.",
                    phoneStatePurposes
            );
    public static final SensitiveData USE_SIP =
            new SensitiveData(
                    Manifest.permission.USE_SIP,
                    "Allows the app to make and receive SIP calls.",
                    phoneStatePurposes
            );
    public static final SensitiveData PROCESS_OUTGOING_CALLS =
            new SensitiveData(
                    Manifest.permission.PROCESS_OUTGOING_CALLS,
                    "Allows the app to see the number being dialed during an " +
                              "outgoing call and possibly redirect the call.",
                    phoneStatePurposes
            );
    public static final SensitiveData BODY_SENSORS =
            new SensitiveData(
                    Manifest.permission.BODY_SENSORS,
                    "Allows the app to access data from sensors that monitor your " +
                              "physical condition, such as your heart rate.",
                    bodySensorPurposes
            );
    public static final SensitiveData SEND_SMS =
            new SensitiveData(
                    Manifest.permission.SEND_SMS,
                    "Allows the app to send SMS messages. This may result in " +
                              "unexpected charges.",
                    smsPurposes
            );
    public static final SensitiveData READ_SMS =
            new SensitiveData(
                    Manifest.permission.READ_SMS,
                    "This app can read all SMS (text) messages stored on your phone.",
                    smsPurposes
            );
    public static final SensitiveData RECEIVE_SMS =
            new SensitiveData(
                    Manifest.permission.RECEIVE_SMS,
                    "Allows the app to receive and process SMS messages.",
                    smsPurposes
            );
    public static final SensitiveData RECEIVE_WAP_PUSH =
            new SensitiveData(
                    Manifest.permission.RECEIVE_WAP_PUSH,
                    "Allows the app to receive and process WAP messages.",
                    smsPurposes
            );
    public static final SensitiveData RECEIVE_MMS =
            new SensitiveData(
                    Manifest.permission.RECEIVE_MMS,
                    "Allows the app to receive and process MMS messages.",
                    smsPurposes
            );
    public static final SensitiveData READ_EXTERNAL_STORAGE =
            new SensitiveData(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    "Allows the app to read the contents of your SD card.",
                    storagePurposes
            );
    public static final SensitiveData WRITE_EXTERNAL_STORAGE =
            new SensitiveData(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    "Allows the app to write to the SD card.",
                    storagePurposes
            );

    static {
        FINE_LOCATION.setDisplayPermission("Precise Location");
        COARSE_LOCATION.setDisplayPermission("Approximate Location");
        BODY_SENSORS.setDisplayPermission("Body Sensors");
    }

    public static final SensitiveDataGroup LOCATION_GROUP =
            new SensitiveDataGroup(
                    "Location",
                    "Access precise location (GPS and network-based), access " +
                                  "approximate location (network-based)",
                    new ArrayList<SensitiveData>(){{
                        add(FINE_LOCATION);
                        add(COARSE_LOCATION);
                    }}
            );

    public static final SensitiveDataGroup CALENDAR_GROUP =
            new SensitiveDataGroup(
                    "Calendar",
                    "Read calendar events and details, add or modify calendar " +
                                   "events and send email to guests without owners' knowledge",
                    new ArrayList<SensitiveData>() {{
                        add(READ_CALENDAR);
                        add(WRITE_CALENDAR);
                    }}
            );

    public static final SensitiveDataGroup CONTACTS_GROUP =
            new SensitiveDataGroup(
                    "Contacts",
                    "Modify your contacts, find accounts on the device, read your " +
                                   "contacts",
                    new ArrayList<SensitiveData>(){{
                        add(READ_CONTACTS);
                        add(WRITE_CONTACTS);
                        add(GET_ACCOUNTS);
                    }}
            );

    public static final SensitiveDataGroup MICROPHONE_GROUP =
            new SensitiveDataGroup(
                    "Microphone",
                    "Accessing microphone audio from the device",
                    new ArrayList<SensitiveData>(){{
                        add(RECORD_AUDIO);
                    }}
            );

    public static final SensitiveDataGroup CAMERA_GROUP =
            new SensitiveDataGroup(
                    "Camera",
                    "Take pictures and videos",
                    new ArrayList<SensitiveData>(){{
                        add(CAMERA);
                    }}
            );

    public static final SensitiveDataGroup CALL_LOG_GROUP =
            new SensitiveDataGroup(
                    "Call Log",
                    "Permissions that are associated telephony features.",
                    new ArrayList<SensitiveData>(){{
                        add(READ_CALL_LOG);
                        add(WRITE_CALL_LOG);
                    }}
            );

    public static final SensitiveDataGroup PHONE_GROUP =
            new SensitiveDataGroup(
                    "Phone",
                    "Answer phone calls, read phone numbers, read phone status " +
                                  "and identity, directly call phone numbers etc.",
                    new ArrayList<SensitiveData>(){{
                        add(READ_PHONE_STATE);
                        add(READ_PHONE_NUMBERS);
                        add(CALL_PHONE);
                        add(ANSWER_PHONE_CALLS);
                        add(ADD_VOICEMAIL);
                        add(USE_SIP);
                        add(PROCESS_OUTGOING_CALLS);
                    }}
            );

    public static final SensitiveDataGroup SENSOR_GROUP =
            new SensitiveDataGroup(
                    "Sensors",
                    "Access body sensors (like heart rate monitors), use " +
                                  "fingerprint hardware, use biometric hardware",
                    new ArrayList<SensitiveData>(){{
                        add(BODY_SENSORS);
                    }}
            );

    public static final SensitiveDataGroup SMS_GROUP =
            new SensitiveDataGroup(
                    "SMS",
                    "Permissions related to user's SMS messages such as " +
                                   "read your text messages (SMS or MMS), receive text messages " +
                                   "(WAP), receive text messages (MMS).",
                    new ArrayList<SensitiveData>(){{
                        add(READ_SMS);
                        add(RECEIVE_SMS);
                        add(SEND_SMS);
                        add(RECEIVE_WAP_PUSH);
                        add(RECEIVE_MMS);
                    }}
            );

    public static final SensitiveDataGroup STORAGE_GROUP =
            new SensitiveDataGroup(
                    "Storage",
                    "Read the contents of your SD card, modify or delete the " +
                                   "contents of your SD card",
                    new ArrayList<SensitiveData>(){{
                        add(READ_EXTERNAL_STORAGE);
                        add(WRITE_EXTERNAL_STORAGE);
                    }}
            );

    public static volatile List<SensitiveDataGroup> ALL_SENSITIVE_DATA_GROUPS =
            new ArrayList<SensitiveDataGroup>(){{
                add(LOCATION_GROUP);
                add(CALENDAR_GROUP);
                add(CAMERA_GROUP);
                add(CONTACTS_GROUP);
                add(MICROPHONE_GROUP);
                add(CALL_LOG_GROUP);
                add(PHONE_GROUP);
                add(SENSOR_GROUP);
                add(SMS_GROUP);
                add(STORAGE_GROUP);
            }};

    private static volatile Map<String, SensitiveData> permissionMap =
            new HashMap<String, SensitiveData>(){{
                put(Manifest.permission.ACCESS_FINE_LOCATION, FINE_LOCATION);
                put(Manifest.permission.ACCESS_COARSE_LOCATION, COARSE_LOCATION);
                put(Manifest.permission.WRITE_CALENDAR, WRITE_CALENDAR);
                put(Manifest.permission.READ_CALENDAR, READ_CALENDAR);
                put(Manifest.permission.READ_CONTACTS, READ_CONTACTS);
                put(Manifest.permission.WRITE_CONTACTS, WRITE_CONTACTS);
                put(Manifest.permission.GET_ACCOUNTS, GET_ACCOUNTS);
                put(Manifest.permission.RECORD_AUDIO, RECORD_AUDIO);
                put(Manifest.permission.CAMERA, CAMERA);
                put(Manifest.permission.CALL_PHONE, CALL_PHONE);
                put(Manifest.permission.READ_CALL_LOG, READ_CALL_LOG);
                put(Manifest.permission.ADD_VOICEMAIL, ADD_VOICEMAIL);
                put(Manifest.permission.READ_PHONE_STATE, READ_PHONE_STATE);
                put(Manifest.permission.READ_PHONE_NUMBERS, READ_PHONE_NUMBERS);
                put(Manifest.permission.ANSWER_PHONE_CALLS, ANSWER_PHONE_CALLS);
                put(Manifest.permission.WRITE_CALL_LOG, WRITE_CALL_LOG);
                put(Manifest.permission.USE_SIP, USE_SIP);
                put(Manifest.permission.RECEIVE_SMS, RECEIVE_SMS);
                put(Manifest.permission.RECEIVE_MMS, RECEIVE_MMS);
                put(Manifest.permission.READ_SMS, READ_SMS);
                put(Manifest.permission.SEND_SMS, SEND_SMS);
                put(Manifest.permission.PROCESS_OUTGOING_CALLS, PROCESS_OUTGOING_CALLS);
                put(Manifest.permission.BODY_SENSORS, BODY_SENSORS);
                put(Manifest.permission.RECEIVE_WAP_PUSH, RECEIVE_WAP_PUSH);
                put(Manifest.permission.READ_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE);
                put(Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE);
            }};

    /**
     * Creates a SensitiveData object from a serialized CharSequence representation
     * if one exists.
     *
     * @param serialized CharSequence serialization of the SensitiveData as an Android permission
     *                   (ex android.permission.ACCESS_FINE_LOCATION)
     * @return SensitiveData representation of the serialization
     * */
    public static SensitiveData from(CharSequence serialized) {
        Precondition.checkEmptyCharSequence(serialized);

        if(!permissionMap.containsKey(serialized.toString())) {
            throw new IllegalArgumentException(serialized.toString() + " is not valid permission.");
        }

        return permissionMap.get(serialized.toString());
    }

    public static boolean permissionIsDangerous(final CharSequence permissionName) {
        if(permissionName == null || permissionName.length() == 0) {
            throw new IllegalArgumentException("Cannot check empty permission");
        }

        String permission = permissionName.toString();

        boolean isNotAndroidPermission = !permission.contains("android.permission.");

        if(isNotAndroidPermission) { return false; }

        return permissionMap.containsKey(permission);
    }
}