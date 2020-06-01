package edu.cmu.policymanager.PolicyManager.purposes;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Purpose of sensitive data access.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class Purpose implements Parcelable {
    public final CharSequence systemPurpose, name, description;

    protected Purpose(CharSequence systemPurpose,
                      CharSequence name,
                      CharSequence description) {
        this.systemPurpose = systemPurpose;
        this.name = name;
        this.description = description;
    }

    /**
     * Returns the Purpose.name (readable field)
     * */
    public String toString() { return name.toString(); }

    /**
     * Two purposes are equal if their systemPurpose names are the same.
     *
     * @param o the other purpose object
     * @return true if equal, false otherwise
     * */
    public boolean equals(Object o) {
        if(o == null) { return false; }

        Purpose that = (Purpose)o;
        return this.systemPurpose.toString().equalsIgnoreCase(that.systemPurpose.toString());
    }

    public int hashCode() {
        return name.hashCode();
    }

    private Purpose(Parcel in) {
        systemPurpose = in.readString();
        name = in.readString();
        description = in.readString();
    }

    public int describeContents() { return 0; }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(systemPurpose.toString());
        out.writeString(name.toString());
        out.writeString(description.toString());
    }

    public static final Parcelable.Creator<Purpose> CREATOR =
            new Parcelable.Creator<Purpose>() {
                public Purpose createFromParcel(Parcel in) {
                    return new Purpose(in);
                }

                public Purpose[] newArray(int size) {
                    return new Purpose[size];
                }
            };
}