package com.project.TeachAids;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class AnalyticsUtil {
    
    public static void LogPageView(Tracker t, String pageName) {
        t.setScreenName(pageName);
        t.send(new HitBuilders.AppViewBuilder().build());
    }
    
    public static void LogEvent(Tracker t, String action, String label) {
        t.send(new HitBuilders.EventBuilder()
        .setCategory("TeachAids")
        .setAction(action)
        .setLabel(label)
        .build());
    }    
}
