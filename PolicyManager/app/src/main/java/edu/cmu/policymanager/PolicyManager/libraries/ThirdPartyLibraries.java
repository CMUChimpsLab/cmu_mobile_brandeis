package edu.cmu.policymanager.PolicyManager.libraries;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains ThirdPartyLibrary instances that the policy manager tracks when either
 * analyzing stacktraces, or determines privacy policies.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class ThirdPartyLibraries {
    public static final String THIRD_PARTY_USE = "Third Party Use",
                               APP_INTERNAL_USE = "App Internal Usage";

    /**
     * Look-up a third party library based on its class/package name.
     *
     * @param offendingClass a class or package name, likely derived from stacktraces
     * @return a ThirdPartyLibrary (if any) that matches offendingClass
     * */
    public static ThirdPartyLibrary getLibraryByOffendingClass(CharSequence offendingClass) {
        String classString = offendingClass.toString();

        for(ThirdPartyLibrary library : AS_LIST) {
            if(classString.contains(library.qualifiedName)) { return library; }
        }

        return null;
    }

    /**
     * Determines the third party library category or the category of access from some arbitrary
     * sequence of characters. It is assumed that the absence of any library or category
     * means that we are referring to something going on internal to the app.
     *
     * @param libraryOrCategory a library name or category
     * @return the category (examples: App Internal Use, Advertising, Third Party Use...)
     * */
    public static String getLibraryCategory(CharSequence libraryOrCategory) {
        if(libraryOrCategory == null || libraryOrCategory.length() == 0) {
            return APP_INTERNAL_USE;
        }

        String categoryString = libraryOrCategory.toString();

        if(categoryString.contains(ThirdPartyLibraries.APP_INTERNAL_USE)) {
            return APP_INTERNAL_USE;
        } else if(categoryString.contains(ThirdPartyLibraries.THIRD_PARTY_USE)) {
            return THIRD_PARTY_USE;
        }

        if(ThirdPartyLibraries.packageToLibrary.containsKey(categoryString)) {
            ThirdPartyLibrary library = ThirdPartyLibraries.from(categoryString);
            return library.category;
        }

        return null;
    }

    public static volatile Map<String, ThirdPartyLibrary> packageToLibrary =
            new HashMap<String, ThirdPartyLibrary>();

    /**
     * Library wild card, used to represent any third party library
     * */
    public static final ThirdPartyLibrary ALL = ThirdPartyLibrary.createGlobalLibrary();

    /**
     * This is a bit of a cheat, but this is here for the UI so we can configure
     * semi-global policies based on their category (ex: location for all third party use).
     * */
    public static final ThirdPartyLibrary CATEGORY_THIRD_PARTY_USE =
            ThirdPartyLibrary.createThirdPartyUseCategory();

    /**
     * This is a bit of a cheat, but this is here for the UI so we can configure
     * semi-global policies based on their category (ex: location for all third party use).
     * */
    public static final ThirdPartyLibrary CATEGORY_APP_INTERNAL_USE =
            ThirdPartyLibrary.createInternalUseCategory();

    /**
     * Get a ThirdPartyLibrary from some text-based serialization (which should be its
     * class or "qualified" name).
     *
     * @param serialized the class or "qualified" name of this library
     * @return the ThirdPartyLibrary instance of this serialized library
     * */
    public static ThirdPartyLibrary from(CharSequence serialized) {
        if(serialized == null || serialized.length() == 0 ||
           !packageToLibrary.containsKey(serialized.toString())) {
            return null;
        }

        return packageToLibrary.get(serialized.toString());
    }

    /**
     * Google's AdMob advertising library.
     * */
    public static final ThirdPartyLibrary ADMOB = ThirdPartyLibrary.createAdvertisingLibrary(
            "AdMob",
            "com.google.android.gms.ads",
            "Google AdMob is a mobile advertising platform that you can use"+
                      " to generate revenue from your app."
    );

    /**
     * AdColony advertising library. Plays video ads, and is used by gaming and non-gaming
     * publishers.
     * */
    public static final ThirdPartyLibrary ADCOLONY = ThirdPartyLibrary.createAdvertisingLibrary(
            "AdColony",
            "com.adcolony",
            "AdColony is a premium mobile video advertising network & " +
                    "monetization platform."
    );

    /**
     * Chartboost advertising library. Used a lot in gaming apps.
     * */
    public static final ThirdPartyLibrary CHARTBOOST = ThirdPartyLibrary.createAdvertisingLibrary(
            "Chartboost",
            "com.chartboost",
            "Chartboost provides free ad-serving technology for direct" +
                    " deals and cross-promotions."
    );

    /**
     * Twitter's mopub advertising library.
     * */
    public static final ThirdPartyLibrary MOPUB =
            ThirdPartyLibrary.createAdvertisingLibrary(
                    "mopub",
                    "com.mopub",
                    "MoPub is a one-stop ad serving platform designed for " +
                            "mobile application publishers to manage their ad inventory" +
                            " on iOS and Android devices."
            );

    /**
     * Vungle advertising library.
     * */
    public static final ThirdPartyLibrary VUNGLE = ThirdPartyLibrary.createAdvertisingLibrary(
            "Vungle",
            "com.vungle",
            "Vungle is a mobile video ad-network"
    );

    /**
     * Taboola advertising library. Typically used in news and weather apps.
     * */
    public static final ThirdPartyLibrary TABOOLA = ThirdPartyLibrary.createAdvertisingLibrary(
            "Taboola",
            "com.taboola.android",
            "Provides advertisement boxes at the bottom of many online news articles"
    );

    /**
     * AOL's Millennial Media advertising library.
     * */
    public static final ThirdPartyLibrary MILLENNIAL_MEDIA =
            ThirdPartyLibrary.createAdvertisingLibrary(
                    "Millenial Media",
                    "com.millennialmedia",
                    "Allows you to put banner, rich media and interactive video " +
                              "ads in your app."
            );

    /**
     * InMobi advertising library. InMobi is not headquartered in the USA.
     * */
    public static final ThirdPartyLibrary IN_MOBI = ThirdPartyLibrary.createAdvertisingLibrary(
            "InMobi",
            "com.inmobi.monetization",
            "Ad network that attempts to target a global market."
    );

    /**
     * Amazon's advertising library.
     * */
    public static final ThirdPartyLibrary AMAZON_MOBILE_ADS =
            ThirdPartyLibrary.createAdvertisingLibrary(
                    "Amazon Mobile Ads",
                    "com.amazon.device.ads",
                    "The Amazon Mobile Ads API is an in-app display advertising " +
                            "solution to monetize mobile apps and games across platforms."
            );

    /**
     * Startapp advertising library.
     * */
    public static final ThirdPartyLibrary STARTAPP = ThirdPartyLibrary.createAdvertisingLibrary(
            "Startapp",
            "com.startapp",
            "StartApp is a mobile advertising platform founded in late 2010, " +
                    "offering both in and out of app monetization."
    );

    /**
     * Fyber Fairbid advertising library. Fyber is not headquartered in the USA.
     * */
    public static final ThirdPartyLibrary FAIRBID = ThirdPartyLibrary.createAdvertisingLibrary(
            "FairBid",
            "com.fyber",
            "Monetization and App Distribution"
    );

    /**
     * Appnext advertising library. Appnext is not headquartered in the USA.
     * */
    public static final ThirdPartyLibrary APP_NEXT = ThirdPartyLibrary.createAdvertisingLibrary(
            "AppNext",
            "com.appnext.sdk",
            "Mobile monetization and app distribution platform"
    );

    /**
     * Nexage MRAID. Used (or was) for storing ads on a user's device? This library is
     * open-sourced, and the documentation for MRAID is here:
     * https://www.iab.com/guidelines/mobile-rich-media-ad-interface-definitions-mraid/
     * */
    public static final ThirdPartyLibrary MRAID = ThirdPartyLibrary.createAdvertisingLibrary(
            "Nexage SourceKit-MRAID for Android",
            "org.nexage.sourcekit",
            "Nexage SourceKit-MRAID For Android is an open sourced IAB MRAID " +
                    "2.0 compliant rendering engine for HTML ad creatives."
    );

    /**
     * Verizon's Flurry analytics library.
     * */
    public static final ThirdPartyLibrary FLURRY =
            ThirdPartyLibrary.createAnalyticsLibrary(
                    "Flurry Analytics",
                    "com.flurry.android",
                    "Flurry Analytics delivers powerful insight into how consumers " +
                            "interact with your mobile applications in real-time."
            );

    /**
     * AppsFlyer analytics library.
     * */
    public static final ThirdPartyLibrary APPS_FLYER = ThirdPartyLibrary.createAnalyticsLibrary(
            "AppsFlyer",
            "com.appsflyer",
            "SDK to measure, track & optimize mobile user acquisition campaigns"
    );

    /**
     * All third party libraries as an array.
     * */
    public static final ThirdPartyLibrary[] AS_LIST = {
            ADMOB,
            ADCOLONY,
            CHARTBOOST,
            MOPUB,
            VUNGLE,
            MILLENNIAL_MEDIA,
            IN_MOBI,
            AMAZON_MOBILE_ADS,
            STARTAPP,
            FAIRBID,
            MRAID,
            TABOOLA,
            APP_NEXT,
            APPS_FLYER,
            FLURRY
    };

    static {
        for(ThirdPartyLibrary library : AS_LIST) {
            packageToLibrary.putIfAbsent(library.qualifiedName, library);
        }

        packageToLibrary.putIfAbsent(ALL.qualifiedName, ALL);
        packageToLibrary.putIfAbsent(
                CATEGORY_THIRD_PARTY_USE.qualifiedName,
                CATEGORY_THIRD_PARTY_USE
        );
        packageToLibrary.putIfAbsent(
                CATEGORY_APP_INTERNAL_USE.qualifiedName,
                CATEGORY_APP_INTERNAL_USE
        );
    }
}