package edu.cmu.policymanager.PolicyManager.libraries;

import android.os.Parcel;
import android.os.Parcelable;

import edu.cmu.policymanager.PolicyManager.purposes.Purpose;
import edu.cmu.policymanager.PolicyManager.purposes.Purposes;
import edu.cmu.policymanager.R;

/**
 * Models third party libraries such as advertising or analytics libraries, which
 * make use of any personal sensitive data and send it off-device.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class ThirdPartyLibrary implements Parcelable {
    public final String name, category, qualifiedName;
    public final Purpose purpose;
    public final int iconResource;
    public final String description;

    private ThirdPartyLibrary(String name,
                              String category,
                              String qualifiedName,
                              Purpose purpose,
                              String description) {
        this.name = name;
        this.category = category;
        this.qualifiedName = qualifiedName;
        this.purpose = purpose;
        this.description = description;

        if(category.equalsIgnoreCase(ThirdPartyLibraries.THIRD_PARTY_USE)) {
            iconResource = R.drawable.ic_purpose_ads;
        } else {
            iconResource = R.drawable.arlington_android;
        }
    }

    @Override
    public boolean equals(Object o) {
        ThirdPartyLibrary other = (ThirdPartyLibrary)o;

        return qualifiedName.equalsIgnoreCase(other.qualifiedName);
    }

    protected static ThirdPartyLibrary createOther() {
        return new ThirdPartyLibrary(
                "All Other Libraries",
                "ALL",
                "All Other Libraries",
                Purposes.ALL,
                "Third party libraries not already listed or known about."
        );
    }

    protected static ThirdPartyLibrary createGlobalLibrary() {
        return new ThirdPartyLibrary(
                "All Libraries",
                "ALL",
                "*",
                Purposes.ALL,
                ""
        );
    }

    protected static ThirdPartyLibrary createAdvertisingLibrary(final String name,
                                                                final String qualifiedName,
                                                                final String description) {
        return new ThirdPartyLibrary(
                name,
                ThirdPartyLibraries.THIRD_PARTY_USE,
                qualifiedName,
                Purposes.DISPLAY_ADVERTISEMENT,
                description
        );
    }

    protected static ThirdPartyLibrary createDevelopmentLibrary(final String name,
                                                                final String qualifiedName,
                                                                final String description) {
        return new ThirdPartyLibrary(
                name,
                ThirdPartyLibraries.APP_INTERNAL_USE,
                qualifiedName,
                Purposes.RUNNING_OTHER_FEATURES,
                description
        );
    }

    protected static ThirdPartyLibrary createThirdPartyUseCategory() {
        return new ThirdPartyLibrary(
                ThirdPartyLibraries.THIRD_PARTY_USE,
                ThirdPartyLibraries.THIRD_PARTY_USE,
                ThirdPartyLibraries.THIRD_PARTY_USE,
                Purposes.ALL,
                ""
        );
    }

    protected static ThirdPartyLibrary createInternalUseCategory() {
        return new ThirdPartyLibrary(
                ThirdPartyLibraries.APP_INTERNAL_USE,
                ThirdPartyLibraries.APP_INTERNAL_USE,
                ThirdPartyLibraries.APP_INTERNAL_USE,
                Purposes.ALL,
                ""
        );
    }

    protected static ThirdPartyLibrary createAnalyticsLibrary(final String name,
                                                              final String qualifiedName,
                                                              final String description) {
        return new ThirdPartyLibrary(
                name,
                ThirdPartyLibraries.THIRD_PARTY_USE,
                qualifiedName,
                Purposes.ANALYZE_USER_INFO,
                description
        );
    }

    private ThirdPartyLibrary(Parcel in) {
        name = in.readString();
        category = in.readString();
        qualifiedName = in.readString();
        purpose = in.readParcelable(Purpose.class.getClassLoader());
        iconResource = in.readInt();
        description = in.readString();
    }

    public int describeContents() { return 0; }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeString(category);
        out.writeString(qualifiedName);
        out.writeParcelable(purpose, flags);
        out.writeInt(iconResource);
        out.writeString(description);
    }

    public static final Parcelable.Creator<ThirdPartyLibrary> CREATOR =
            new Parcelable.Creator<ThirdPartyLibrary>() {
                public ThirdPartyLibrary createFromParcel(Parcel in) {
                    return new ThirdPartyLibrary(in);
                }

                public ThirdPartyLibrary[] newArray(int size) {
                    return new ThirdPartyLibrary[size];
                }
            };
}