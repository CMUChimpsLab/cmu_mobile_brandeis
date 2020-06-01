package edu.cmu.policymanager.ui.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import edu.cmu.policymanager.PolicyManager.libraries.ThirdPartyLibrary;
import edu.cmu.policymanager.PolicyManager.purposes.Purpose;
import edu.cmu.policymanager.PolicyManager.sensitivedata.SensitiveData;
import edu.cmu.policymanager.R;
import edu.cmu.policymanager.viewmodel.W4PIcon;

/**
 * Abstract class for managing icons for a specific user-interface implementation.
 *
 * Created by Mike Czapik (Carnegie Mellon University) on 8/30/2018.
 */

public abstract class IconManager {
    private final Context context;

    public IconManager(Context context) { this.context = context; }

    /**
     * Get the icon of an app from its package name
     *
     * @param packageName the package name of the app to get an icon for
     * @return the app's icon as a W4PIcon
     * */
    public abstract W4PIcon getAppIcon(CharSequence packageName);

    /**
     * Get the icon of the given third-party library.
     *
     * @param library the third-party library to get an icon for
     * @return the library's icon as a W4PIcon
     * */
    public abstract W4PIcon getThirdPartyLibraryIcon(ThirdPartyLibrary library);

    /**
     * Get the icon of the given Android permission.
     *
     * @param permission the permission to get an icon for
     * @return the permission's icon as a W4PIcon
     * */
    public abstract W4PIcon getPermissionIcon(SensitiveData permission);

    /**
     * Get the icon of the given purpose.
     *
     * @param purpose the purpose to get an icon for
     * @return the purpose's icon as a W4PIcon
     * */
    public abstract W4PIcon getPurposeIcon(Purpose purpose);

    /**
     * Converts a Drawable to a BitmapDrawable.
     *
     * @param drawable the drawable to convert
     * @return the BitmapDrawable
     * */
    public BitmapDrawable drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return (BitmapDrawable) drawable;
        }

        Bitmap bmp = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return new BitmapDrawable(context.getResources(), bmp);
    }
}