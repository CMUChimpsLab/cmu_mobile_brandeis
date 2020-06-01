package edu.cmu.policymanager.ui.phonespies;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import edu.cmu.policymanager.R;

/**
 * Created by Mike Czapik (Carnegie Mellon University) on 1/9/2019.
 */

public class PhoneSpyNotification {
    private Context context;
    private final String packageName;

    public static final String SPY_CHANNEL_ID = "spyingApps";

    public PhoneSpyNotification(final Context context,
                                final String packageName) {
        this.context = context;
        this.packageName = packageName;
    }

    public RemoteViews createRemoteView() {
        RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.allysiqi_notification_spies);
        Intent intent = new Intent(context, SpyingAppActivity.class);
        intent.putExtra(SpyingAppActivity.KEY_SPY_PACKAGE, packageName);

        PendingIntent viewSpyingApp = PendingIntent.getActivity(context,
                1338,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        view.setOnClickPendingIntent(R.id.allysiqi_notification_spy_view_more, viewSpyingApp);

        return view;

    }

    public Notification create() {
        RemoteViews smallView = new RemoteViews(context.getPackageName(),
                                                R.layout.allysiqi_notification_spies_small);

        Notification.Builder builder = new Notification.Builder(context, SPY_CHANNEL_ID)
                                                       .setSmallIcon(R.drawable.ic_settings_blue_24dp)
                                                       .setCustomContentView(smallView)
                                                       .setCustomBigContentView(createRemoteView());

        return builder.build();
    }
}