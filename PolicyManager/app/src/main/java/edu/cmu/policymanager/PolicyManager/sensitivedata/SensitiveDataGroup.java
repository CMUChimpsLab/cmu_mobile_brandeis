package edu.cmu.policymanager.PolicyManager.sensitivedata;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Groups individual SensitiveData objects into a category, such as all
 * SMS permissions into SMS.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public class SensitiveDataGroup implements Parcelable {
    public final CharSequence groupName, groupDescription;
    public final List<SensitiveData> permissionsInGroup;

    protected SensitiveDataGroup(CharSequence groupName,
                                 CharSequence groupDescription,
                                 List<SensitiveData> permissionsInGroup) {
        if(groupName == null || groupName.length() == 0) {
            throw new IllegalArgumentException("Sensitive data group name cannot be empty.");
        } else if(permissionsInGroup == null || permissionsInGroup.size() == 0) {
            throw new IllegalArgumentException("Cannot have an empty list of permissions.");
        } else if(groupDescription == null) {
            throw new IllegalArgumentException("Group description shouldn't be null.");
        }

        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.permissionsInGroup = permissionsInGroup;
    }

    public boolean equals(Object o) {
        if(o.getClass() != this.getClass()) {
            throw new IllegalArgumentException("Cannot compare SenstiveDataGroup to anything else");
        }

        SensitiveDataGroup that = (SensitiveDataGroup)o;
        return this.groupName.toString().equalsIgnoreCase(that.groupName.toString());
    }

    private SensitiveDataGroup(Parcel in) {
        groupName = in.readString();
        groupDescription = in.readString();
        permissionsInGroup = in.readArrayList(SensitiveData.class.getClassLoader());
    }

    public int describeContents() { return 0; }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(groupName.toString());
        out.writeString(groupDescription.toString());
        out.writeList(permissionsInGroup);
    }

    public static final Parcelable.Creator<SensitiveDataGroup> CREATOR =
            new Parcelable.Creator<SensitiveDataGroup>() {
        public SensitiveDataGroup createFromParcel(Parcel in) {
            return new SensitiveDataGroup(in);
        }

        public SensitiveDataGroup[] newArray(int size) {
            return new SensitiveDataGroup[size];
        }
            };
}