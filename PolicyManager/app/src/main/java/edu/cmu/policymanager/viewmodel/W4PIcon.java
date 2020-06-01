package edu.cmu.policymanager.viewmodel;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

import static android.graphics.Color.rgb;

/**
 * Models various icons as just one simple data type.
 *
 * Created by Mike Czapik (Carnegie Mellon University) on 3/28/2018.
 */

public class W4PIcon implements Parcelable {
    private int resourceId = -1;
    private Drawable drawableIcon = null;
    private boolean isSubIcon = false;

    private W4PIcon(final int resourceId) {
        this.resourceId = resourceId;
    }
    private W4PIcon(final Drawable drawableIcon) {
        this.drawableIcon = drawableIcon;
    }

    public static W4PIcon createStaticIcon(final int resourceId) {
        return new W4PIcon(resourceId);
    }

    public static W4PIcon createDrawableIcon(final Drawable drawableIcon) {
        return new W4PIcon(drawableIcon);
    }

    public void setAsSubIcon() {
        isSubIcon = true;
    }

    public int asResourceId() { return resourceId; }
    public Drawable asDrawable() { return drawableIcon; }

    public void addIconToView(ImageView viewToAddImage) {
        boolean isStaticResource = resourceId > 0;
        boolean isDrawable = drawableIcon != null;

        if(isStaticResource) {
            viewToAddImage.setImageResource(resourceId);

            if(isSubIcon) {
                viewToAddImage.setBackgroundColor(Color.rgb(0,0, 255));
                viewToAddImage.setColorFilter(Color.rgb(255,255,255));
            }
        }
        else if(isDrawable) {
            viewToAddImage.setBackground(drawableIcon);
        }
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<W4PIcon> CREATOR = new Parcelable.Creator<W4PIcon>() {
        public W4PIcon createFromParcel(Parcel in) {
            return new W4PIcon(in);
        }

        public W4PIcon[] newArray(int size) { return new W4PIcon[size]; }
    };

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(resourceId);

        if(drawableIcon != null) {
            Bitmap bitmap = (Bitmap) ((BitmapDrawable) drawableIcon).getBitmap();
            out.writeParcelable(bitmap, flags);
        }
    }

    private W4PIcon(final Parcel in) {
        resourceId = in.readInt();
        Bitmap bmp = in.readParcelable(Bitmap.class.getClassLoader());
        drawableIcon = new BitmapDrawable(bmp);
    }
}