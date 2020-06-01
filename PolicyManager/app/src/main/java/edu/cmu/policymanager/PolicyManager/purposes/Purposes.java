package edu.cmu.policymanager.PolicyManager.purposes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.policymanager.validation.Precondition;

/**
 * Purpose of sensitive data access. Derived from the unified purpose
 * taxonomy document.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 */
public class Purposes {
    public static final Purpose
            /**
             * To deliver ads targeted to your interests.
             * */
            DISPLAY_ADVERTISEMENT = new Purpose(
                    "android.purpose.DISPLAY_ADVERTISEMENT",
                    "Displaying Advertisement",
                    "To deliver ads targeted to your interests."
            ),

            /**
             * To analyze your demographics and behaviors.
             * */
            ANALYZE_USER_INFO = new Purpose(
                    "android.purpose.ANALYZE_USER_INFORMATION",
                    "Analyzing User Information",
                    "To analyze your demographics and behaviors."
            ),

            /**
             * To track and analyze your health habits.
             * */
            MONITOR_HEALTH = new Purpose(
                    "android.purpose.MONITOR_HEALTH",
                    "Monitoring Health",
                    "To track and analyze your health habits."
            ),

            /**
             * To connect with or find other users.
             * */
            CONNECT_WITH_OTHER_PEOPLE = new Purpose(
                    "android.purpose.CONNECT_WITH_OTHER_PEOPLE",
                    "Connecting With Other People",
                    "To connect with or find other users."
            ),

            /**
             * To gather data for research studies or experiments.
             * */
            CONDUCTING_RESEARCH = new Purpose(
                    "android.purpose.CONDUCTING_RESEARCH",
                    "Conducting Research",
                    "To gather data for research studies or experiments."
            ),

            /**
             * To run some basic or undetermined app feature.
             * */
            RUNNING_OTHER_FEATURES = new Purpose(
                    "android.purpose.RUNNING_OTHER_FEATURES",
                    "Running Other Features",
                    "To run some basic or undetermined app feature."
            ),

            /**
             * To back up or save important data to a remote service.
             * */
            BACKUP_TO_CLOUD_SERVICE = new Purpose(
                    "android.purpose.BACKUP_TO_CLOUD_SERVICE",
                    "Backing-up to Cloud Service",
                    "To back up or save important data to a remote service."
            ),

            /**
             * To provide directions on how to travel somewhere.
             * */
            NAVIGATING_TO_DESTINATION = new Purpose(
                    "android.purpose.NAVIGATING_TO_DESTINATION",
                    "Navigating To Destination",
                    "To provide directions on how to travel somewhere."
            ),

            /**
             * To search for businesses or events near you.
             * */
            SEARCH_NEARBY_PLACES = new Purpose(
                    "android.purpose.SEARCH_NEARBY_PLACES",
                    "Searching Nearby Places",
                    "To search for businesses or events near you."
            ),

            /**
             * To give weather reports for your immediate area.
             * */
            DELIVERING_LOCAL_WEATHER = new Purpose(
                    "android.purpose.DELIVERING_LOCAL_WEATHER",
                    "Delivering Local Weather",
                    "To give weather reports for your immediate area."
            ),

            /**
             * To tag photos with your location.
             * */
            ADD_LOCATION_TO_PHOTO = new Purpose(
                    "android.purpose.ADD_LOCATION_TO_PHOTO",
                    "Adding Location to Photo",
                    "To tag photos with your location."
            ),

            /**
             * To secure your device from unwanted persons.
             * */
            SECURING_DEVICE = new Purpose(
                    "android.purpose.SECURING_DEVICE",
                    "Securing Device",
                    "To secure your device from unwanted persons."
            ),

            /**
             * To communicate with other individuals.
             * */
            MESSAGING_OR_CALLING_PEOPLE = new Purpose(
                    "android.purpose.MESSAGING_OR_CALLING_PEOPLE",
                    "Messaging or Calling People",
                    "To communicate with other individuals."
            ),

            /**
             * To contact emergency services.
             * */
            NOTIFY_EMERGENCY_SERVICES = new Purpose(
                    "android.purpose.NOTIFY_EMERGENCY_SERVICES",
                    "Notifying Emergency Services",
                    "To contact emergency services."
            ),

            /**
             * To implement in-game features.
             * */
            PLAYING_GAMES = new Purpose(
                    "android.purpose.PLAYING_GAMES",
                    "Playing Games",
                    "To implement in-game features."
            ),
            ALL = new Purpose(
                    "android.purpose.ALL",
                    "*",
                    "All Purposes."
            );

    private static final Map<String, Purpose> purposeMap =
            new HashMap<String, Purpose>(){{
                put(DISPLAY_ADVERTISEMENT.name.toString(), DISPLAY_ADVERTISEMENT);
                put(ANALYZE_USER_INFO.name.toString(), ANALYZE_USER_INFO);
                put(MONITOR_HEALTH.name.toString(), MONITOR_HEALTH);
                put(CONNECT_WITH_OTHER_PEOPLE.name.toString(), CONNECT_WITH_OTHER_PEOPLE);
                put(CONDUCTING_RESEARCH.name.toString(), CONDUCTING_RESEARCH);
                put(RUNNING_OTHER_FEATURES.name.toString(), RUNNING_OTHER_FEATURES);
                put(BACKUP_TO_CLOUD_SERVICE.name.toString(), BACKUP_TO_CLOUD_SERVICE);
                put(NAVIGATING_TO_DESTINATION.name.toString(), NAVIGATING_TO_DESTINATION);
                put(SEARCH_NEARBY_PLACES.name.toString(), SEARCH_NEARBY_PLACES);
                put(DELIVERING_LOCAL_WEATHER.name.toString(), DELIVERING_LOCAL_WEATHER);
                put(ADD_LOCATION_TO_PHOTO.name.toString(), ADD_LOCATION_TO_PHOTO);
                put(SECURING_DEVICE.name.toString(), SECURING_DEVICE);
                put(MESSAGING_OR_CALLING_PEOPLE.name.toString(), MESSAGING_OR_CALLING_PEOPLE);
                put(NOTIFY_EMERGENCY_SERVICES.name.toString(), NOTIFY_EMERGENCY_SERVICES);
                put(PLAYING_GAMES.name.toString(), PLAYING_GAMES);
                put(ALL.name.toString(), ALL);
            }};

    /**
     * Create an instance of a Purpose from a serialized string.
     *
     * @param serialized Purpose as a string (specifically Purpose.name)
     * @return purpose name that matches this CharSequence
     * */
    public static Purpose from(CharSequence serialized) {
        Precondition.checkEmptyCharSequence(serialized);

        if(!purposeMap.containsKey(serialized.toString())) {
            throw new IllegalArgumentException(serialized.toString() + " is an invalid purpose");
        }

        return purposeMap.get(serialized.toString());
    }

    /**
     * Determines if this purpose is something that a third-party would use, namely
     * advertising and analytics.
     *
     * @param purpose the purpose to analyze
     * @return true if the purpose would be for third-parties, false if app internal use
     * */
    public static boolean isThirdPartyUse(Purpose purpose) {
        return DISPLAY_ADVERTISEMENT.equals(purpose) || ANALYZE_USER_INFO.equals(purpose);
    }

    /**
     * Given a purpose name, gets the purpose object.
     * */
    public static final List<Purpose> AS_LIST = new ArrayList<Purpose>()
    {{
        add(DISPLAY_ADVERTISEMENT);
        add(ANALYZE_USER_INFO);
        add(MONITOR_HEALTH);
        add(CONNECT_WITH_OTHER_PEOPLE);
        add(CONDUCTING_RESEARCH);
        add(RUNNING_OTHER_FEATURES);
        add(BACKUP_TO_CLOUD_SERVICE);
        add(NAVIGATING_TO_DESTINATION);
        add(SEARCH_NEARBY_PLACES);
        add(DELIVERING_LOCAL_WEATHER);
        add(ADD_LOCATION_TO_PHOTO);
        add(SECURING_DEVICE);
        add(MESSAGING_OR_CALLING_PEOPLE);
        add(NOTIFY_EMERGENCY_SERVICES);
        add(PLAYING_GAMES);
    }};

    /**
     * uPal purposes are different from the set of purposes recognized by the policy
     * manager, so there needs to be a reasonable mapping between those purposes and
     * ours.
     *
     * @param microPalPurpose the purpose set by a uPal
     * @return the Purpose.name CharSequence equivalent, or Running Other Features if none match
     * */
    public static CharSequence convertFromMicroPal(CharSequence microPalPurpose) {
        String purposeString = microPalPurpose.toString();

        if(purposeString.equals("Research: ")) {
            return CONDUCTING_RESEARCH.name;
        } else if(purposeString.equals("Ads: ")) {
            return DISPLAY_ADVERTISEMENT.name;
        } else if(purposeString.equals("Analytics: ")) {
            return ANALYZE_USER_INFO.name;
        } else if(purposeString.equals("Game: ")) {
            return PLAYING_GAMES.name;
        } else if(purposeString.equals("Health: ")) {
            return MONITOR_HEALTH.name;
        } else if(purposeString.equals("Social: ")) {
            return CONNECT_WITH_OTHER_PEOPLE.name;
        }

        return RUNNING_OTHER_FEATURES.name;
    }
}