package com.project.TeachAids;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import android.app.Application;

public class TeachAidsApplication extends Application {
    private static final String PROPERTY_ID = "UA-55005707-1";
    Tracker mTracker;
    
    synchronized Tracker getTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            mTracker = analytics.newTracker(PROPERTY_ID);
        }
        return mTracker;
    }
}
