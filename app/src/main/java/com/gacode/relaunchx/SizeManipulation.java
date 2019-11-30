package com.gacode.relaunchx;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public final class SizeManipulation {
    static final int TOOLBAR_MIN_HEIGHT_DEF = 60;
    static final int TOOLBAR_TEXT_SIZE_DEF = 20;
    static final int FILE_LIST_LINE_TEXT_SIZE_DEF = 20;
    static final int ICON_SIZE_DEF = 48;
    static final int TEXT_ICON_SIZE_DEF = 50;


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

    public static void AdjustWithPreferencesToolbarText(Application app, SharedPreferences preferences, TextView view) {
        String dpString = preferences.getString("toolbarTextSize", String.valueOf(TOOLBAR_TEXT_SIZE_DEF));
        int dp = Integer.valueOf(dpString);
        if (dp == TOOLBAR_TEXT_SIZE_DEF) return;

        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(app, dp));
    }

    public static void AdjustWithPreferencesFileListLine1(Application app, SharedPreferences preferences, TextView view) {
        String dpString = preferences.getString("fileFontSize", String.valueOf(FILE_LIST_LINE_TEXT_SIZE_DEF));
        int dp = Integer.valueOf(dpString);
        if (dp == FILE_LIST_LINE_TEXT_SIZE_DEF) return;

        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(app, dp));
    }

    public static void AdjustWithPreferencesFileListLine2(Application app, SharedPreferences preferences, TextView view) {
        String dpString = preferences.getString("fileFontSize", String.valueOf(FILE_LIST_LINE_TEXT_SIZE_DEF));
        int dp = Integer.valueOf(dpString);
        if (dp == FILE_LIST_LINE_TEXT_SIZE_DEF) return;

        dp = (int)(dp * (float)4/5);
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(app, dp));
    }

    public static boolean AassignWithPreferencesIcon(Application app, SharedPreferences preferences, ImageView view) {
        boolean show = preferences.getBoolean("showIcon", false);
        if (show == false) {
            view.setVisibility(View.GONE);
        }
        return show;
    }

    public static boolean AassignWithPreferencesIcon(Application app, SharedPreferences preferences, ImageView view, Bitmap bmp) {
        boolean show = AassignWithPreferencesIcon(app, preferences, view);
        if (show) {
            view.setImageBitmap(scaleBitmap(bmp, DpToPx(app, ICON_SIZE_DEF)));
        }
        return show;
    }

    public static boolean AassignWithPreferencesIcon(Application app, SharedPreferences preferences, ImageView view, int resourceId) {
        Bitmap bmp = BitmapFactory.decodeResource(app.getResources(), resourceId);
        return AassignWithPreferencesIcon(app, preferences, view, bmp);
    }

    public static boolean AassignWithPreferencesIcon(Application app, SharedPreferences preferences, ImageView view, Drawable bmp) {
        return AassignWithPreferencesIcon(app, preferences, view, ((BitmapDrawable) bmp).getBitmap());
    }

    public static int AutoColumnsNumber(Application app, SharedPreferences preferences, ArrayList<Integer> lengths) {
        if (lengths.size() == 0) return 1;

        String pattern = preferences.getString("columnsAlgIntensity", "70 3:5 7:4 15:3 48:2"); // default - medium
        String[] spat = pattern.split("[\\s\\:]+");
        int quantile = Integer.parseInt(spat[0]);

        // implementation - via percentiles len
        Collections.sort(lengths);
        Integer index = (lengths.size() * quantile) / 100;
        int avgTextLength = lengths.get(index);

        char[] text = new char[avgTextLength];
        for (int i = 0; i < text.length; i++) text[i] = 'A';

        DisplayMetrics displayMetrics = app.getApplicationContext().getResources().getDisplayMetrics();
        Paint paint = new Paint();
        String dpString = preferences.getString("fileFontSize", String.valueOf(FILE_LIST_LINE_TEXT_SIZE_DEF));

        paint.setTextSize(DpToPx(app, Integer.valueOf(dpString)));
        float textWidthPixels = paint.measureText(text, 0, text.length);
        textWidthPixels += DpToPx(app, TEXT_ICON_SIZE_DEF);
        int columnsNumber = (int)(displayMetrics.widthPixels / textWidthPixels);

        if (columnsNumber > lengths.size()) columnsNumber = lengths.size();
        if (columnsNumber > 4) columnsNumber = 4;
        else if (columnsNumber == 0) columnsNumber = 1;

        return columnsNumber;
    }
}
