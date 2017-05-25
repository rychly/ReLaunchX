package com.gacode.relaunchx;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LockScreen extends Service implements View.OnClickListener {

    private final String TAG = "ReLaunchX";
    public final static String ACTION_LOCK_SCREEN = "com.gacode.relaunch.ACTION_LOCK_SCREEN";
    public final int MAX_PASSWORD_LEN = 15;

    private LinearLayout lockView;
    private WindowManager.LayoutParams lockViewParams;
    private WindowManager windowManager;
    private SharedPreferences prefs;
    private String password;
    private String lockPassword;
    private TextView passwordView;
    private boolean incorrectPassword;
    private int retryCount;
    private BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            lockPassword = prefs.getString("lockPassword", "");
            if (lockPassword.length() > 0 && lockView == null) {
                if (lockPassword.length() > MAX_PASSWORD_LEN) {
                    lockPassword = lockPassword.substring(0, MAX_PASSWORD_LEN);
                }
                displayLock();
            }
        }
    };

    private void displayLock() {
        password = "";
        retryCount = 3;
        incorrectPassword = false;

        lockView = new LinearLayout(this);
        windowManager.addView(lockView, lockViewParams);
        ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.lock_screen, lockView);
        passwordView = (TextView)lockView.findViewById(R.id.passwordView);
        lockView.findViewById(R.id.unlockPasswordButton).setOnClickListener(this);
        lockView.findViewById(R.id.deletePasswordButton).setOnClickListener(this);
        lockView.findViewById(R.id.passwordButton0).setOnClickListener(this);
        lockView.findViewById(R.id.passwordButton1).setOnClickListener(this);
        lockView.findViewById(R.id.passwordButton2).setOnClickListener(this);
        lockView.findViewById(R.id.passwordButton3).setOnClickListener(this);
        lockView.findViewById(R.id.passwordButton4).setOnClickListener(this);
        lockView.findViewById(R.id.passwordButton5).setOnClickListener(this);
        lockView.findViewById(R.id.passwordButton6).setOnClickListener(this);
        lockView.findViewById(R.id.passwordButton7).setOnClickListener(this);
        lockView.findViewById(R.id.passwordButton8).setOnClickListener(this);
        lockView.findViewById(R.id.passwordButton9).setOnClickListener(this);

//        try {
//            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
//            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void addDigit(String digit) {
        if (password.length() < MAX_PASSWORD_LEN) {
            passwordView.append("*");
            password += digit;
        }
    }

    private void delDigit() {
        CharSequence passwdMask = passwordView.getText();
        if (passwdMask.length() > 0){
            passwdMask = passwdMask.subSequence(1, passwdMask.length());
            passwordView.setText(passwdMask);
            password = password.substring(0, password.length() - 1);
        }
    }


    @Override
    public void onClick(View view) {
        if (incorrectPassword) {
            incorrectPassword = false;
            passwordView.setText("");
            password = "";
            if (--retryCount <= 0) {
                SystemClock.sleep(5000);
                retryCount = 3;
            }
        }
        switch (view.getId()) {
            default:
                addDigit((String)view.getTag());
                break;

            case R.id.deletePasswordButton:
                delDigit();
                break;

            case R.id.unlockPasswordButton:
                if(!lockPassword.equals(password)) {
                    passwordView.setText(getText(R.string.lock_screen_incorrect_passwd));
                    incorrectPassword = true;
                } else {
                    windowManager.removeView(lockView);
                    lockView = null;
                }
                break;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "Create Lock Screen");

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        IntentFilter bootCompleted = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
        registerReceiver(screenReceiver, bootCompleted);

        IntentFilter screenOffFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenReceiver, screenOffFilter);
        IntentFilter appLockFilter = new IntentFilter(ACTION_LOCK_SCREEN);
        registerReceiver(screenReceiver, appLockFilter);

        windowManager = ((WindowManager) getSystemService(WINDOW_SERVICE));
        int lockViewFlags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
        lockViewParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR, lockViewFlags);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(screenReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
