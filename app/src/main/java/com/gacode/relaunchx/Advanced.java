package com.gacode.relaunchx;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Advanced extends Activity {
    final static String TAG = "Advanced";

    SharedPreferences prefs;
    ReLaunchApp app;
    Button rebootBtn;
    Button powerOffBtn;
    WebView infoPanelView;
    boolean addSView = true;

    WifiManager wfm;
    Button wifiOnOff;
    Button wifiScan;
    List<NetInfo> wifiNetworks = new ArrayList<NetInfo>();
    WiFiAdapter adapter;
    ListView lv_wifi;
    IntentFilter i1;
    IntentFilter i2;
    BroadcastReceiver b1;
    BroadcastReceiver b2;
    String connectedSSID;

    // I don't like to keep here the static objects but on my device sporadically
    // the process Runtime.getRuntime().exec(cmd) hangs indefinitely so I had no better choice
    // to avoid crash and/or device reboot (the hung thread becomes invincible). In other words
    // I have chosen to keep the "loading..." message when it happens until device reboot.
    static Handler filesystemInfoUpdate;
    static Thread filesystemInfoThread;
    static String filesystemInfoPanelContent;

    private void setEinkController() {
        if (prefs != null) {
            Integer einkUpdateMode = 1;
            try {
                einkUpdateMode = Integer.parseInt(prefs.getString(
                        "einkUpdateMode", "1"));
            } catch (Exception e) {
                einkUpdateMode = 1;
            }
            if (einkUpdateMode < -1 || einkUpdateMode > 2)
                einkUpdateMode = 1;
            if (einkUpdateMode >= 0) {
                EinkScreen.UpdateMode = einkUpdateMode;

                Integer einkUpdateInterval = 10;
                try {
                    einkUpdateInterval = Integer.parseInt(prefs.getString(
                            "einkUpdateInterval", "10"));
                } catch (Exception e) {
                    einkUpdateInterval = 10;
                }
                if (einkUpdateInterval < 0 || einkUpdateInterval > 100)
                    einkUpdateInterval = 10;
                EinkScreen.UpdateModeInterval = einkUpdateInterval;

                EinkScreen.PrepareController(null, false);
            }
        }
    }

    static class NetInfo {
        static int unknownLevel = -5000;
        String SSID;
        String extra;
        int level;
        int netId;
        boolean inrange;
        boolean configured;

        NetInfo(String s, int id, boolean in, boolean conf) {
            SSID = s;
            extra = "";
            level = unknownLevel;
            netId = id;
            inrange = in;
            configured = conf;
        }

        NetInfo(String s, boolean in, boolean conf) {
            SSID = s;
            extra = "";
            level = unknownLevel;
            netId = 0;
            inrange = in;
            configured = conf;
        }

        NetInfo(String s, String e, int id, boolean in, boolean conf) {
            SSID = s;
            extra = e;
            level = unknownLevel;
            netId = id;
            inrange = in;
            configured = conf;
        }

        NetInfo(String s, String e, boolean in, boolean conf) {
            SSID = s;
            extra = e;
            level = unknownLevel;
            netId = 0;
            inrange = in;
            configured = conf;
        }
    }

    public class NetInfoComparator implements java.util.Comparator<NetInfo> {
        public int compare(NetInfo o1, NetInfo o2) {
            if (connectedSSID != null && connectedSSID.equals(o1.SSID)) {
                return -1;
            }
            if (connectedSSID != null && connectedSSID.equals(o2.SSID)) {
                return 1;
            }
            if (o1.inrange && !o2.inrange)
                return -1;
            if (!o1.inrange && o2.inrange)
                return 1;
            if (o1.level < o2.level)
                return 1;
            if (o1.level > o2.level)
                return -1;
            return o1.SSID.compareToIgnoreCase(o2.SSID);
        }
    }

    static class FilesystemVolumeInfo {
        String dev;
        String mpoint;
        String fs;
        String total;
        String used;
        String free;
        boolean ro;
    }

    static class ViewHolder {
        TextView tv1;
        TextView tv2;
        TextView tv3;
        ImageView iv;
    }

    class WiFiAdapter extends BaseAdapter {
        final Context cntx;

        WiFiAdapter(Context context) {
            cntx = context;
        }

        public int getCount() {
            return wifiNetworks.size();
        }

        public Object getItem(int position) {
            return wifiNetworks.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) app.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.adv_wifi_item, null);
                if (v == null) {
                    return null;
                }
                holder = new ViewHolder();
                holder.tv1 = (TextView) v.findViewById(R.id.wf_ssid);
                holder.tv2 = (TextView) v.findViewById(R.id.wf_capabilities);
                holder.tv3 = (TextView) v.findViewById(R.id.wf_other);
                holder.iv = (ImageView) v.findViewById(R.id.wf_icon);
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }
            TextView tv1 = holder.tv1;
            TextView tv2 = holder.tv2;
            TextView tv3 = holder.tv3;
            ImageView iv = holder.iv;
            final WifiInfo winfo = wfm.getConnectionInfo();
            final NetInfo item = wifiNetworks.get(position);
            if (item != null) {
                int backgroundColor;
                int textColor;
                if (item.inrange && item.configured) {
                    backgroundColor = getResources().getColor(R.color.file_reading_bg);
                    textColor = getResources().getColor(R.color.file_reading_fg);
                    iv.setImageDrawable(getResources().getDrawable(R.drawable.file_ok));
                } else {
                    backgroundColor = getResources().getColor(R.color.file_finished_bg);
                    textColor = getResources().getColor(R.color.file_finished_fg);
                    iv.setImageDrawable(getResources().getDrawable(R.drawable.file_notok));
                }
                tv1.setBackgroundColor(backgroundColor);
                tv1.setTextColor(textColor);
                tv2.setBackgroundColor(backgroundColor);
                tv2.setTextColor(textColor);
                tv3.setBackgroundColor(backgroundColor);
                tv3.setTextColor(textColor);

                if (item.SSID.equals(winfo.getSSID())) {
                    SpannableString s1 = new SpannableString(item.SSID);
                    s1.setSpan(Typeface.BOLD, 0, item.SSID.length(), 0);
                    tv1.setText(s1);
                    if (item.extra.equals("")) {
                        tv2.setText("");
                    } else {
                        SpannableString s2 = new SpannableString(item.extra);
                        s2.setSpan(Typeface.BOLD, 0, item.extra.length(), 0);
                        tv2.setText(s2);
                    }
                    int ipAddress = winfo.getIpAddress();
                    // "Connected, IP: %d.%d.%d.%d"
                    String s = String.format(
                            getResources().getString(
                                    R.string.jv_advanced_connected)
                                    + " %d.%d.%d.%d", (ipAddress & 0xff),
                            (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
                            (ipAddress >> 24 & 0xff));
                    int sl1 = s.length();
                    // ", Level: "
                    s += ", "
                            + getResources().getString(
                            R.string.jv_advanced_level) + " "
                            + item.level + "dBm " + levelToString(item.level);
                    SpannableString s3 = new SpannableString(s);
                    s3.setSpan(Typeface.BOLD, 0, sl1, 0);
                    tv3.setText(s3);
                } else {
                    SpannableString s1 = new SpannableString(item.SSID);
                    s1.setSpan(Typeface.BOLD, 0, item.SSID.length(), 0);
                    tv1.setText(s1);
                    tv2.setText(item.extra);
                    String s;
                    if (item.inrange) {
                        // "Level: "
                        s = getResources()
                                .getString(R.string.jv_advanced_level)
                                + " "
                                + item.level
                                + "dBm "
                                + levelToString(item.level);
                    } else {
                        // "Not in range"
                        s = getResources().getString(
                                R.string.jv_advanced_notrange);
                    }
                    if (!item.configured) {
                        // ", not configured"
                        s += ", "
                                + getResources().getString(
                                R.string.jv_advanced_not_configured);
                    }
                    tv3.setText(s);
                }
            }
            return v;
        }
    }

    private String levelToString(int level) {
        if (level >= -56)
            return "[\u25A0\u25A0\u25A0\u25A0\u25A0]";
        if (level >= -63)
            return "[\u25A0\u25A0\u25A0\u25A0\u25A1]";
        if (level >= -70)
            return "[\u25A0\u25A0\u25A0\u25A1\u25A1]";
        if (level >= -77)
            return "[\u25A0\u25A0\u25A1\u25A1\u25A1]";
        if (level >= -84)
            return "[\u25A0\u25A1\u25A1\u25A1\u25A1]";
        return "[\u25A1\u25A1\u25A1\u25A1\u25A1]";
    }

    // Read file and return result as list of strings
    private List<String> readFile(String fname) {
        BufferedReader br;
        String readLine;
        List<String> rc = new ArrayList<String>();
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    fname)), 1000);
        } catch (FileNotFoundException e) {
            return rc;
        }
        try {
            while ((readLine = br.readLine()) != null)
                rc.add(readLine);
        } catch (IOException e) {
            try {
                br.close();
            } catch (IOException e1) {
                //emply
            }
            return rc;
        }
        try {
            br.close();
        } catch (IOException e) {
            //emply
        }

        return rc;
    }

    // Execute foreground command and return result as list of strings
    private List<String> execFg(String cmd) {
        List<String> rc = new ArrayList<String>();
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            try {
                BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
                while (true) {
                    String s = stdout.readLine();
                    if (s == null) break;
                    rc.add(s);
                }
            } finally {
                if (p != null) {
                    p.destroy();
                }
            }
        } catch (IOException e) {
            Log.e("execFg", "Problem executing cmd \"" + cmd + "\"... :" + e);
        }
        return rc;
    }

    private List<FilesystemVolumeInfo> getFilesytemInfo() {
        String[] ingnoreFs = getResources()
                .getStringArray(R.array.filesystems_to_ignore);

        ArrayList<FilesystemVolumeInfo> fsInfo = new ArrayList<FilesystemVolumeInfo>();
        for (String line : readFile("/proc/mounts")) {
            String[] fsMount = line.split("\\s+");
            if (fsMount.length < 4) {
                continue;
            }
            String fs = fsMount[2];
            String flagsString = fsMount[3];
            boolean ignore = false;
            for (String ingnoreF : ingnoreFs) {
                if (ingnoreF.equals(fs)) {
                    ignore = true;
                    break;
                }
            }
            if (ignore)
                continue;
            FilesystemVolumeInfo in = new FilesystemVolumeInfo();
            in.dev = fsMount[0];
            in.mpoint = fsMount[1];
            in.fs = fs;
            String[] flags = flagsString.split(",");
            for (String flag : flags) {
                if (flag.equals("ro")) {
                    in.ro = true;
                    break;
                } else if (flag.equals("rw")) {
                    in.ro = false;
                    break;
                }
            }
            in.total = "-";
            in.used = "-";
            in.free = "-";
            fsInfo.add(in);
        }

        for (FilesystemVolumeInfo in : fsInfo) {
            int totalPos = 0;
            int usedPos = 0;
            int freePos = 0;
            List<String> output = execFg("df " + in.mpoint);
            for (int i = 0; i < output.size(); i++) {
                String[] dfLine = output.get(i).split("\\s+");
                if (i == 0) {
                    //parse the header
                    for (int j = 0; j < dfLine.length; j++) {
                        if (dfLine[j].compareToIgnoreCase("Size") == 0) {
                            totalPos = j;
                        } else if (dfLine[j].compareToIgnoreCase("Used") == 0) {
                            usedPos = j;
                        } else if (dfLine[j].compareToIgnoreCase("Free") == 0) {
                            freePos = j;
                        }
                    }
                    continue;
                }
                in.total = totalPos == 0 ? "-" : dfLine[totalPos];
                in.used = usedPos == 0 ? "-" : dfLine[usedPos];
                in.free = freePos == 0 ? "-" : dfLine[freePos];
            }
        }
        return fsInfo;
    }

    private List<NetInfo> readScanResults(WifiManager w) {
        List<NetInfo> rc = new ArrayList<NetInfo>();
        List<ScanResult> rc1 = w.getScanResults();
        List<WifiConfiguration> rc2 = w.getConfiguredNetworks();
        if (rc2 == null)
            rc2 = new ArrayList<WifiConfiguration>();

        connectedSSID = w.getConnectionInfo().getSSID();

        if (rc1 == null) {
            // No scan results - just copy configured networks to returned value
            for (WifiConfiguration wc : rc2) {
                rc.add(new NetInfo(wc.SSID, wc.networkId, false, true));
            }
            Collections.sort(rc, new NetInfoComparator());
            return rc;
        }

        // Merge uniq scanresult items with configured network info
        for (ScanResult s : rc1) {
            boolean alreadyHere = false;
            for (NetInfo s1 : rc) {
                if (s1.SSID.equals(s.SSID)) {
                    alreadyHere = true;
                    s1.level = s.level;
                    break;
                }
            }
            if (!alreadyHere) {
                boolean in = false;
                for (WifiConfiguration wc : rc2) {
                    String ssid = wc.SSID;
                    if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                        ssid = ssid.substring(1, ssid.length() - 1);
                    }
                    if (ssid.equals(s.SSID)) {
                        rc.add(new NetInfo(ssid, s.capabilities, wc.networkId,
                                true, true));
                        rc.get(rc.size() - 1).level = s.level;
                        in = true;
                        break;
                    }
                }
                if (!in) {
                    // In range but not configured
                    rc.add(new NetInfo(s.SSID, s.capabilities, true, false));
                }
                rc.get(rc.size() - 1).level = s.level;
            }
        }

        // Add confiured but not active networks
        for (WifiConfiguration wc : rc2) {
            String ssid = wc.SSID;
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
            boolean alreadyHere = false;
            for (NetInfo s : rc) {
                if (s.SSID.equals(ssid)) {
                    alreadyHere = true;
                    break;
                }
            }
            if (!alreadyHere) {
                rc.add(new NetInfo(ssid, false, true));
            }
        }
        Collections.sort(rc, new NetInfoComparator());
        return rc;
    }

    private void updateWiFiInfo() {
        wifiOnOff.setEnabled(true);
        if (wfm.isWifiEnabled()) {
            // "Turn WiFi off"
            wifiOnOff.setText(getResources().getString(
                    R.string.jv_advanced_turn_wifi_off));
            wifiOnOff.setCompoundDrawablesWithIntrinsicBounds(getResources()
                    .getDrawable(R.drawable.wifi_off), getResources()
                    .getDrawable(R.drawable.ci_wifi), getResources()
                    .getDrawable(R.drawable.wifi_off), null);
            wifiOnOff.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    wfm.setWifiEnabled(false);
                    // "Turning WiFi off"
                    wifiOnOff.setText(getResources().getString(
                            R.string.jv_advanced_turning_wifi_off));
                    wifiOnOff.setEnabled(false);
                }
            });
            wifiNetworks = readScanResults(wfm);
            setEinkController();
            adapter.notifyDataSetChanged();
            wifiScan.setEnabled(true);
        } else {
            // "Turn WiFi on"
            wifiOnOff.setText(getResources().getString(
                    R.string.jv_advanced_turn_wifi_on));
            wifiOnOff.setCompoundDrawablesWithIntrinsicBounds(getResources()
                    .getDrawable(R.drawable.wifi_on), getResources()
                    .getDrawable(R.drawable.ci_wifi), getResources()
                    .getDrawable(R.drawable.wifi_on), null);
            wifiOnOff.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    wfm.setWifiEnabled(true);
                    // "Turning WiFi on"
                    wifiOnOff.setText(getResources().getString(
                            R.string.jv_advanced_turning_wifi_on));
                    wifiOnOff.setEnabled(false);
                }
            });
            wifiNetworks.clear();
            setEinkController();
            adapter.notifyDataSetChanged();
            wifiScan.setEnabled(false);
        }
    }

    public void startGetVolumesInfoThread() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                List<FilesystemVolumeInfo> fsInfo = getFilesytemInfo();
                final int width = infoPanelView.getWidth();
                final int padding = width > 1000 ? 25 : 5;
                StringBuilder sb = new StringBuilder()
                        .append("<style>")
                        .append("td { padding:0px ").append(padding).append("px; text-align: right;}")
                        .append("th {text-align: center;}")
                        .append("</style>")
                        .append("<h3><center>")
                        .append(getResources().getString(R.string.jv_advanced_mount_diskspartitions))
                        .append("</center></h3>")
                        .append("<table>")
                        .append("<tr>")
                        .append("<th>")
                        .append(getResources().getString(R.string.jv_advanced_mount_mountpoint))
                        .append("</th>")
                        .append("<th>")
                        .append(getResources().getString(R.string.jv_advanced_mount_FS))
                        .append("</th>")
                        .append("<th>")
                        .append(getResources().getString(R.string.jv_advanced_mount_total))
                        .append("</th>")
                        .append("<th>")
                        .append(getResources().getString(R.string.jv_advanced_mount_used))
                        .append("</th>")
                        .append("<th>")
                        .append(getResources().getString(R.string.jv_advanced_mount_free))
                        .append("</th>")
                        .append("<th>")
                        .append(getResources().getString(R.string.jv_advanced_mount_rorw))
                        .append("</th>")
                        .append("</tr>");
                for (FilesystemVolumeInfo i : fsInfo) {
                    sb.append("<tr>")
                            .append("<td style='text-align:left'>")
                            .append(i.mpoint)
                            .append("</td>")
                            .append("<td>")
                            .append(i.fs)
                            .append("</td>")
                            .append("<td>")
                            .append(i.total)
                            .append("</td>")
                            .append("<td>")
                            .append(i.used)
                            .append("</td>")
                            .append("<td>")
                            .append(i.free)
                            .append("</td>")
                            .append("<td>")
                            .append((i.ro ? getResources().getString(R.string.jv_advanced_mount_ro)
                                    : getResources().getString(R.string.jv_advanced_mount_rw)))
                            .append("</td>")
                            .append("</tr>");
                }
                sb.append("</table>");
                filesystemInfoPanelContent = sb.toString();
                filesystemInfoUpdate.sendEmptyMessage(0);
            }
        };

        filesystemInfoThread = new Thread(runnable);
        filesystemInfoThread.setName("GetVolumesInfoThread");
        filesystemInfoThread.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        setEinkController();

        app = ((ReLaunchApp) getApplicationContext());
        if(app == null ) {
            finish();
        }
        app.setFullScreenIfNecessary(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.advanced_layout);

        // "Advanced functions, info, etc."
        ((TextView) findViewById(R.id.results_title)).setText(getResources()
                .getString(R.string.jv_advanced_title));
        findViewById(R.id.results_btn)
                .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        finish();
                    }
                });

        // Wifi info
        lv_wifi = (ListView) findViewById(R.id.wifi_lv);
        adapter = new WiFiAdapter(this);
        lv_wifi.setAdapter(adapter);

        if (prefs.getBoolean("customScroll", app.customScrollDef)) {
            int scrollW;
            try {
                scrollW = Integer
                        .parseInt(prefs.getString("scrollWidth", "25"));
            } catch (NumberFormatException e) {
                scrollW = 25;
            }

            if (addSView) {
                LinearLayout ll = (LinearLayout) findViewById(R.id.wifi_lv_layout);
                final SView sv = new SView(getBaseContext());
                LinearLayout.LayoutParams pars = new LinearLayout.LayoutParams(
                        scrollW, ViewGroup.LayoutParams.FILL_PARENT, 1f);
                sv.setLayoutParams(pars);
                ll.addView(sv);
                lv_wifi.setOnScrollListener(new AbsListView.OnScrollListener() {
                    public void onScroll(AbsListView view,
                                         int firstVisibleItem, int visibleItemCount,
                                         int totalItemCount) {
                        sv.total = totalItemCount;
                        sv.count = visibleItemCount;
                        sv.first = firstVisibleItem;
                        setEinkController();
                        sv.invalidate();
                    }

                    public void onScrollStateChanged(AbsListView view,
                                                     int scrollState) {
                    }
                });
                addSView = false;
            }
        } else {
            lv_wifi.setOnScrollListener(new AbsListView.OnScrollListener() {
                public void onScroll(AbsListView view, int firstVisibleItem,
                                     int visibleItemCount, int totalItemCount) {
                    setEinkController();
                }

                public void onScrollStateChanged(AbsListView view,
                                                 int scrollState) {
                }
            });
        }

        // Wifi
        wfm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiNetworks = readScanResults(wfm);

        wifiScan = (Button) findViewById(R.id.wifi_scan_btn);
        wifiScan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                wfm.startScan();
                wifiScan.setEnabled(false);
            }
        });

        // Receive broadcast when scan results are available
        i1 = new IntentFilter();
        i1.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        b1 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                wifiNetworks = readScanResults(wfm);
                wifiScan.setEnabled(true);
                adapter.notifyDataSetChanged();
            }
        };
        registerReceiver(b1, i1);

        // Receive broadcast when WiFi status changed
        i2 = new IntentFilter();
        i2.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        i2.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        i2.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        i2.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        i2.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        b2 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                wifiNetworks = readScanResults(wfm);
                wifiScan.setEnabled(true);
                adapter.notifyDataSetChanged();
                updateWiFiInfo();
            }
        };
        registerReceiver(b2, i2);

        wifiOnOff = (Button) findViewById(R.id.wifi_onoff_btn);
        updateWiFiInfo();

        Button wifiSetup = (Button) findViewById(R.id.wifi_setup_btn);
        wifiSetup.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (DeviceInfo.EINK_SONY) {
                    final Intent intent = new Intent(Intent.ACTION_MAIN, null);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    // SONY PRS-Tx only!
                    final ComponentName cn = new ComponentName(
                            "com.sony.drbd.ebook.NetworkManagerSettings",
                            "com.sony.drbd.ebook.NetworkManagerSettings.NMWirelessSetting");
                    intent.setComponent(cn);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else if (DeviceInfo.EINK_NOOK) {
                    final Intent intent = new Intent(Intent.ACTION_MAIN, null);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    // NOOK ST only!
                    final ComponentName cn = new ComponentName(
                            "com.android.settings",
                            "com.android.settings.wifi.Settings_Wifi_Settings");
                    intent.setComponent(cn);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    final Intent intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });

        // Lock button
        final Activity parent = this;
        Button lockBtn = (Button) findViewById(R.id.lock_btn);
        lockBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (PowerFunctions.actionLock(parent)) {
                    parent.finish();
                }
            }
        });

        // Reboot button
        rebootBtn = (Button) findViewById(R.id.reboot_btn);
        rebootBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PowerFunctions.actionReboot(parent);
            }
        });

        // Power Off button
        powerOffBtn = (Button) findViewById(R.id.poweroff_btn);
        powerOffBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PowerFunctions.actionPowerOff(parent);
            }
        });

        // Web info view
        infoPanelView = (WebView) findViewById(R.id.info_panel_web_view);
        StringBuilder sb = new StringBuilder()
                .append("<h3><center>")
                .append(getResources().getString(R.string.jv_advanced_mount_loading))
                .append("</center></h3>");
        infoPanelView.loadDataWithBaseURL(null, sb.toString(), "text/html", "utf-8", null);

        filesystemInfoUpdate = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                infoPanelView.loadDataWithBaseURL(null, filesystemInfoPanelContent, "text/html", "utf-8", null);
            }
        };

        if (filesystemInfoThread == null || filesystemInfoThread.isAlive() == false) {
            startGetVolumesInfoThread();
        }

        ScreenOrientation.set(this, prefs);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(b1);
        unregisterReceiver(b2);
        filesystemInfoThread.interrupt();
        filesystemInfoThread = null;
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setEinkController();
        app.generalOnResume(TAG, this);
    }

}
