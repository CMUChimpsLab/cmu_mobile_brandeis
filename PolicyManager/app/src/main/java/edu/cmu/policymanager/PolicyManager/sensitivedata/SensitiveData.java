package edu.cmu.policymanager.PolicyManager.sensitivedata;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.policymanager.PolicyManager.purposes.Purpose;
import edu.cmu.policymanager.util.PermissionUtil;

/**
 * Models a single dangerous permission.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public final class SensitiveData implements Parcelable {
    public final CharSequence androidPermission, description;
    private CharSequence displayPermission;
    public final List<Purpose> purposes;

    protected SensitiveData(CharSequence androidPermission,
                            CharSequence description,
                            List<Purpose> purposes) {
        if(androidPermission == null || androidPermission.length() == 0) {
            throw new IllegalArgumentException("Permission cannot be empty.");
        } else if(description == null) {
            throw new IllegalArgumentException("Permission descriptions cannot be null.");
        } else if(purposes == null || purposes.size() == 0) {
            throw new IllegalArgumentException("Purposes for permission cannot be empty.");
        }

        this.androidPermission = androidPermission;
        this.description = description;
        displayPermission = PermissionUtil.getDisplayPermission(androidPermission.toString());
        this.purposes = purposes;
    }

    @Override
    public boolean equals(Object o) {
        SensitiveData other = (SensitiveData)o;
        return androidPermission.toString().equalsIgnoreCase(other.androidPermission.toString());
    }

    protected void setDisplayPermission(String displayPermission) {
        this.displayPermission = displayPermission;
    }

    public CharSequence getDisplayPermission() { return displayPermission; }

    private SensitiveData(Parcel in) {
        androidPermission = in.readString();
        description = in.readString();
        displayPermission = in.readString();
        Object[] arr = in.readArray(Purpose.class.getClassLoader());
        purposes = new ArrayList<Purpose>(arr.length);

        for(Object o : arr) {
            purposes.add((Purpose)o);
        }
    }

    public int describeContents() { return 0; }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(androidPermission.toString());
        out.writeString(description.toString());
        out.writeString(displayPermission.toString());
        out.writeArray(purposes.toArray());
    }

    public static final Parcelable.Creator<SensitiveData> CREATOR =
            new Parcelable.Creator<SensitiveData>() {
                public SensitiveData createFromParcel(Parcel in) {
                    return new SensitiveData(in);
                }

                public SensitiveData[] newArray(int size) {
                    return new SensitiveData[size];
                }
            };
}