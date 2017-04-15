package com.gacode.relaunchx;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;

public final class ViewManipulation {
    public static void AdjustViewMinHeightWithPreferences(Application app, SharedPreferences preferences, View view) {
        DisplayMetrics displayMetrics = app.getApplicationContext().getResources().getDisplayMetrics();
        String dpString = preferences.getString("buttonMinHeight", "0");
        int dp = Integer.valueOf(dpString);
        int minHeight = (int)(dp * (displayMetrics.densityDpi / 160));
        view.setMinimumHeight(minHeight);
    }
}
