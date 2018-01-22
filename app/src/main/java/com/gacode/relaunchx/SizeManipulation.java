package com.gacode.relaunchx;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public final class SizeManipulation {
    static final int TOOLBAR_MIN_HEIGHT_DEF = 60;
    static final int FILE_LIST_LINE_1_TEXT_SIZE_DEF = 20;
    static final int FILE_LIST_LINE_2_TEXT_SIZE_DEF = 16;
    static final int ICON_SIZE_DEF = 48;


    private static int DpToPx(Application app, int dp) {
        DisplayMetrics displayMetrics = app.getApplicationContext().getResources().getDisplayMetrics();
        return (int)(dp * ((float)displayMetrics.densityDpi / 160));
    }

    private static Bitmap scaleBitmap(Bitmap bmp, int size) {
        return Bitmap.createScaledBitmap(bmp, size, size, true);
    }

    public static void AdjustWithPreferencesToolbarMinHeight(Application app, SharedPreferences preferences, View view) {
        String dpString = preferences.getString("buttonMinHeight", String.valueOf(TOOLBAR_MIN_HEIGHT_DEF));
        int dp = Integer.valueOf(dpString);
        if (dp == TOOLBAR_MIN_HEIGHT_DEF) return;

        view.setMinimumHeight(DpToPx(app, dp));
    }

    public static void AdjustWithPreferencesFileListLine1(Application app, SharedPreferences preferences, TextView view) {
        String dpString = preferences.getString("firstLineFontSizePx", String.valueOf(FILE_LIST_LINE_1_TEXT_SIZE_DEF));
        int dp = Integer.valueOf(dpString);
        if (dp == FILE_LIST_LINE_1_TEXT_SIZE_DEF) return;

        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(app, dp));
    }

    public static void AdjustWithPreferencesFileListLine2(Application app, SharedPreferences preferences, TextView view) {
        String dpString = preferences.getString("secondLineFontSizePx", String.valueOf(FILE_LIST_LINE_2_TEXT_SIZE_DEF));
        int dp = Integer.valueOf(dpString);
        if (dp == FILE_LIST_LINE_2_TEXT_SIZE_DEF) return;
        int dd = DpToPx(app, dp);
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(app, dp));
    }

    public static boolean AassignWithPreferencesIcon(Application app, SharedPreferences preferences, ImageView view) {
        boolean show = preferences.getBoolean("showIcon", true);
        if (show == false) {
            view.setVisibility(View.GONE);
        }
        return show;
    }

    public static void AassignWithPreferencesIcon(Application app, SharedPreferences preferences, ImageView view, Bitmap bmp) {
        boolean show = AassignWithPreferencesIcon(app, preferences, view);
        if (show) {
            view.setImageBitmap(scaleBitmap(bmp, DpToPx(app, ICON_SIZE_DEF)));
        }
    }

    public static void AassignWithPreferencesIcon(Application app, SharedPreferences preferences, ImageView view, int resourceId) {
        Bitmap bmp = BitmapFactory.decodeResource(app.getResources(), resourceId);
        AassignWithPreferencesIcon(app, preferences, view, bmp);
    }

    public static void AassignWithPreferencesIcon(Application app, SharedPreferences preferences, ImageView view, Drawable bmp) {
        AassignWithPreferencesIcon(app, preferences, view, ((BitmapDrawable) bmp).getBitmap());
    }

}
