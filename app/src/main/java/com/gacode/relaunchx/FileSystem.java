package com.gacode.relaunchx;

import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class FileSystem {

    public static class MountInfo {
        String dev;
        String mpoint;
        String fs;
        long total;
        long used;
        long free;
        boolean ro;
    }

    /**
     * Read text file
     * @param fname - file path
     * @return file content
     */
    private static List<String> readTextFile(String fname) {
        BufferedReader bufferedReader;
        String line;
        List<String> returnCollection = new ArrayList<String>();
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fname)), 1000);
        } catch (FileNotFoundException e) {
            return returnCollection;
        }

        try {
            while ((line = bufferedReader.readLine()) != null) {
                returnCollection.add(line);
            }
        } catch (IOException e) {
            return returnCollection;
        }
        finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
            }
        }

        return returnCollection;
    }

    public static List<MountInfo> getFilesytemInfo(List<String> fileSystemsToSkip) {
        ArrayList<MountInfo> mountInfoList = new ArrayList<MountInfo>();
        for (String line : readTextFile("/proc/mounts")) {
            String[] fsMount = line.split("\\s+");
            if (fsMount.length < 4) {
                continue;
            }
            String fileSystem = fsMount[2];
            String flagsString = fsMount[3];
            if (fileSystemsToSkip.contains(fileSystem)) {
                continue;
            }
            MountInfo mountInfo = new MountInfo();
            mountInfo.dev = fsMount[0];
            mountInfo.mpoint = fsMount[1];
            mountInfo.fs = fileSystem;
            String[] flags = flagsString.split(",");
            for (String flag : flags) {
                if (flag.equals("ro")) {
                    mountInfo.ro = true;
                    break;
                } else if (flag.equals("rw")) {
                    mountInfo.ro = false;
                    break;
                }
            }
            mountInfo.total = 0;
            mountInfo.used = 0;
            mountInfo.free = 0;
            mountInfoList.add(mountInfo);
        }

        for (MountInfo mountInfo : mountInfoList) {
            try {
                StatFs statFs = new StatFs(mountInfo.mpoint);
                long total = ((long) statFs.getBlockCount() * (long) statFs.getBlockSize());
                long free = (statFs.getAvailableBlocks() * (long) statFs.getBlockSize());
                mountInfo.used = total - free;
                mountInfo.free = free;
                mountInfo.total = total;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mountInfoList;
    }

    /**
     * Convert bytes to x.x human readable string
     * @param size in bytes
     * @return x.x [GMK]B string
     */
    public static String bytesToString(long size)
    {
        final long KB = 1  * 1024;
        final long MB = KB * 1024;
        final long GB = MB * 1024;
        DecimalFormat df = new DecimalFormat("#.#");

        if (size >= GB)    return df.format((double)size / GB) + "GB";
        if (size >= MB)    return df.format((double)size / MB) + "MB";
        if (size >= KB)    return df.format((double)size / KB) + "KB";
        return df.format(size) + "B ";
    }

    /**
     * Get External storage info
     * @return
     */
    public static MountInfo getExternalStorageInfo() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return null;
        };
        try {
            MountInfo mountInfo = new MountInfo();
            mountInfo.mpoint = Environment.getExternalStorageDirectory().getAbsolutePath();
            StatFs statFs = new StatFs(mountInfo.mpoint);
            long total = ((long) statFs.getBlockCount() * (long) statFs.getBlockSize());
            long free = (statFs.getAvailableBlocks() * (long) statFs.getBlockSize());
            mountInfo.used = total - free;
            mountInfo.free = free;
            mountInfo.total = total;
            return mountInfo;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Discover removable SD card storage directory
     * @return
     */
    private static File getRemovableStorage() {
        final String value = System.getenv("SECONDARY_STORAGE");
        if (!TextUtils.isEmpty(value)) {
            final String[] paths = value.split(":");
            for (String path : paths) {
                File file = new File(path);
                if (file.isDirectory() && path.toLowerCase().contains("sd")) {
                    return file;
                }
            }
        }

        //Fallback to guessing, just check if there is anything like
        //  /mnt/extsd
        //  /mnt/extSdCard
        //  /mnt/ext_sd

        File mnt = new File("/mnt");
        if (mnt.isDirectory()) {
            for (String dir : mnt.list()) {
                if (dir.toLowerCase().contains("ext") && dir.toLowerCase().contains("sd")) {
                    return new File("/mnt/" + dir);
                }
            }
        }

        File storage = new File("/storage");
        if (storage.isDirectory()) {
            for (String dir : storage.list()) {
                if (dir.toLowerCase().contains("ext") && dir.toLowerCase().contains("sd")) {
                    return new File("/storage/" + dir);
                }
            }
        }

        return null;
    }

    /**
     * Get removable SD card storage info
     * @return
     */
    public static MountInfo getSecondaryStorageInfo() {
        File sd = getRemovableStorage();
        if (sd == null) {
            return null;
        };
        try {
            MountInfo mountInfo = new MountInfo();
            mountInfo.mpoint = sd.getAbsolutePath();
            StatFs statFs = new StatFs(mountInfo.mpoint);
            long total = ((long) statFs.getBlockCount() * (long) statFs.getBlockSize());
            long free = (statFs.getAvailableBlocks() * (long) statFs.getBlockSize());
            mountInfo.used = total - free;
            mountInfo.free = free;
            mountInfo.total = total;
            return mountInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
