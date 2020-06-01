package edu.cmu.policymanager.PolicyManager.enforcement;

import android.content.ComponentName;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;

import java.util.Arrays;
import java.util.List;

import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibraries;
import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibrary;
import edu.cmu.policymanager.PolicyManager.purposes.Purpose;
import edu.cmu.policymanager.PolicyManager.purposes.Purposes;
import edu.cmu.policymanager.PolicyManager.sensitivedata.DangerousPermissions;
import edu.cmu.policymanager.PolicyManager.sensitivedata.SensitiveData;
import edu.cmu.policymanager.validation.Precondition;

/**
 * Models an incoming permission request that PE Android trapped, and is
 * passing along to the policy manager. Just about all state in this class can come
 * from a call from onDangerousPermissionRequest.
 *
 * Created by Mike Czapik (Carnegie Mellon University).
 * */
public final class PermissionRequest implements Parcelable {
    public final String packageName;
    public final SensitiveData permission;
    public final Purpose purpose;
    public final ThirdPartyLibrary thirdPartyLibrary;
    public final List<StackTraceElement[]> stacktraces;
    public final ResultReceiver recv;
    public final Context context;
    public final ComponentName topActivity;
    public final CharSequence palModule;
    public final CharSequence palRequestDescription;

    private PermissionRequest(Builder config) {
        packageName = config.mPackageName;
        permission = config.mPermission;
        purpose = config.mPurpose;
        thirdPartyLibrary = config.mThirdPartyLibrary;
        stacktraces = config.stacktraces;
        topActivity = config.mTopActivity;
        recv = config.mRecv;
        context = config.mContext;
        palModule = config.mPalModule;
        palRequestDescription = config.mPalRequestDescription;
    }

    private PermissionRequest(Parcel in) {
        packageName = in.readString();
        permission = in.readParcelable(SensitiveData.class.getClassLoader());
        purpose = in.readParcelable(Purpose.class.getClassLoader());
        thirdPartyLibrary = in.readParcelable(ThirdPartyLibrary.class.getClassLoader());
        recv = in.readParcelable(ResultReceiver.class.getClassLoader());
        topActivity = in.readParcelable(ComponentName.class.getClassLoader());
        stacktraces = null;
        context = null;
        palModule = in.readString();
        palRequestDescription = in.readString();
    }

    @Override
    public String toString() {
        String purposeString = (purpose == null ? "unknown" : purpose.name.toString()),
               libraryString =
                (thirdPartyLibrary == null ? "null" : thirdPartyLibrary.name),
               stacktraceString = (stacktraces == null ? "unknown" : stringify(stacktraces));

        String appString = (packageName == null ? "unknown" : packageName);
        return "App " + appString + " requests " + permission.getDisplayPermission() + " for "
                + purposeString + " used by " +
                libraryString + " from: " + stacktraceString;
    }

    private String stringify(List<StackTraceElement[]> stacktraces) {
        String str = "";

        for(StackTraceElement[] traces : stacktraces) {
            str += Arrays.toString(traces) + "\n";
        }

        return str;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(packageName);
        out.writeParcelable(permission, flags);
        out.writeParcelable(purpose, flags);
        out.writeParcelable(thirdPartyLibrary, flags);
        out.writeParcelable(recv, 0);
        out.writeParcelable(topActivity, flags);
        out.writeString(palModule.toString());
        out.writeString(palRequestDescription.toString());
    }

    public static final Parcelable.Creator<PermissionRequest> CREATOR =
            new Parcelable.Creator<PermissionRequest>() {
        public PermissionRequest createFromParcel(Parcel in) {
            return new PermissionRequest(in);
        }

        public PermissionRequest[] newArray(int size) {
            return new PermissionRequest[size];
        }
            };

    /**
     * Creates Builder for PermissionRequest.
     *
     * @return new Builder instance
     * */
    public static Builder builder() { return new Builder(); }

    /**
     * Creates Builder with a Context for PermissionRequest.
     *
     * @return new Builder instance
     * */
    public static Builder builder(Context context) {
        return new Builder(context);
    }

    /**
     * Builder class for PermissionRequest. Must set at least a package name and permission
     * in order to create an instance.
     * */
    public static class Builder {
        private String mPackageName;
        private SensitiveData mPermission;
        private Purpose mPurpose;
        private ThirdPartyLibrary mThirdPartyLibrary;
        private List<StackTraceElement[]> stacktraces;
        private ResultReceiver mRecv;
        private Context mContext;
        private ComponentName mTopActivity;
        private String mPalModule;
        private String mPalRequestDescription;

        public Builder() {}

        public Builder(Context context) { mContext = context; }

        /**
         * Set the package name that is requesting sensitive data from the user.
         *
         * @param packageName the package name requesting data
         * @return the Builder instance
         * */
        public Builder setPackageName(CharSequence packageName) {
            Precondition.checkEmptyCharSequence(packageName);
            mPackageName = packageName.toString();

            return this;
        }

        /**
         * Set the permission that is being requested by an app.
         *
         * @param permission permission being requested
         * @return the Builder instance
         * */
        public Builder setPermission(CharSequence permission) {
            Precondition.checkEmptyCharSequence(permission);
            mPermission = DangerousPermissions.from(permission);

            return this;
        }

        /**
         * Set the purpose for the data access. This takes a CharSequence because
         * onDangerousPermissionRequest provides this to us as a String.
         *
         * @param purpose name of the purpose - see Purposes.java
         * @return the Builder instance
         * */
        public Builder setPurpose(CharSequence purpose) {
            if(purpose != null && !purpose.toString().isEmpty()) {
                mPurpose = Purposes.from(purpose);
            }

            return this;
        }

        /**
         * Set the third-party library that is responsible for accessing sensitive data.
         *
         * @param library the library making the request
         * @return the Builder instance
         * */
        public Builder setLibrary(ThirdPartyLibrary library) {
            mThirdPartyLibrary = library;
            return this;
        }

        /**
         * Set the stacktraces that were collected as a result of this permission request.
         * PE for Android gives many stacktraces, but if building this for test, you only need
         * a list of size 1, which will provide the stacktraces strictly related to this data
         * access.
         *
         * @param stacktraces the stacktraces
         * @return the Builder instance
         * */
        public Builder setStacktraces(List<StackTraceElement[]> stacktraces) {
            this.stacktraces = stacktraces;
            return this;
        }

        /**
         * Set the name of the uPal (micro PAL) that is accessing this data.
         *
         * @param palModule the uPal making the request
         * @return the Builder instance
         * */
        public Builder setPalModule(CharSequence palModule) {
            Precondition.checkEmptyCharSequence(palModule);

            mPalModule = palModule.toString();
            return this;
        }

        /**
         * Set the description for the data access provided to us from the uPal (micro PAL).
         *
         * @param description the description of the data access
         * @return the Builder instance
         * */
        public Builder setPalRequestDescription(CharSequence description) {
            if(description == null) {
                mPalRequestDescription = "";
            } else {
                mPalRequestDescription = description.toString();
            }

            return this;
        }

        /**
         * Set the ResultReceiver, which is used to communicate back to PE for Android the
         * result of a policy manager's data access checks. When running unit tests, this
         * value should be set to null (if it is set at all).
         *
         * @param recv the ResultReceiver passed by onDangerousPermissionRequest
         * @return the Builder instance
         * */
        public Builder setResultReceiver(ResultReceiver recv) {
            mRecv = recv;
            return this;
        }

        /**
         * Sets the top activity, or the activity that is currently in the foreground when
         * this data was accessed as a ComponentName.
         *
         * @param topActivity the top or foreground activity
         * @return the Builder instance
         * */
        public Builder setTopActivity(ComponentName topActivity) {
            mTopActivity = topActivity;
            return this;
        }

        /**
         * Creates a PermissionRequest instance with the provided data. Must have at least
         * set a package name or permission, or a StateException will be thrown.
         *
         * @return PermissionRequest instance
         * */
        public PermissionRequest build() {
            Precondition.checkState(mPackageName != null, "Package is null");
            Precondition.checkState(mPermission != null, "Permission is null");

            return new PermissionRequest(this);
        }
    }
}