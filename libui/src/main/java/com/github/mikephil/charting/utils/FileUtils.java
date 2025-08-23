
package com.github.mikephil.charting.utils;

import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    private static final String LOG = "MPChart-FileUtils";

    public static List<Entry> loadEntriesFromFile(String path) {

        File sdcard = Environment.getExternalStorageDirectory();

        File file = new File(sdcard, path);

        List<Entry> entries = new ArrayList<Entry>();

        try {
            @SuppressWarnings("resource")
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                String[] split = line.split("#");

                if (split.length <= 2) {
                    entries.add(new Entry(Float.parseFloat(split[0]), Integer.parseInt(split[1])));
                } else {

                    float[] vals = new float[split.length - 1];

                    for (int i = 0; i < vals.length; i++) {
                        vals[i] = Float.parseFloat(split[i]);
                    }

                    entries.add(new BarEntry(Integer.parseInt(split[split.length - 1]), vals));
                }
            }
        } catch (IOException e) {
            Log.e(LOG, e.toString());
        }

        return entries;

    }

    public static List<Entry> loadEntriesFromAssets(AssetManager am, String path) {

        List<Entry> entries = new ArrayList<Entry>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(am.open(path), "UTF-8"));

            String line = reader.readLine();

            while (line != null) {
                String[] split = line.split("#");

                if (split.length <= 2) {
                    entries.add(new Entry(Float.parseFloat(split[1]), Float.parseFloat(split[0])));
                } else {

                    float[] vals = new float[split.length - 1];

                    for (int i = 0; i < vals.length; i++) {
                        vals[i] = Float.parseFloat(split[i]);
                    }

                    entries.add(new BarEntry(Integer.parseInt(split[split.length - 1]), vals));
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            Log.e(LOG, e.toString());

        } finally {

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG, e.toString());
                }
            }
        }

        return entries;

    }

    public static void saveToSdCard(List<Entry> entries, String path) {

        File sdcard = Environment.getExternalStorageDirectory();

        File saved = new File(sdcard, path);
        if (!saved.exists())
        {
            try
            {
                saved.createNewFile();
            } catch (IOException e)
            {
                Log.e(LOG, e.toString());
            }
        }
        try
        {
            BufferedWriter buf = new BufferedWriter(new FileWriter(saved, true));

            for (Entry e : entries) {

                buf.append(e.getY() + "#" + e.getX());
                buf.newLine();
            }

            buf.close();
        } catch (IOException e)
        {
            Log.e(LOG, e.toString());
        }
    }

    public static List<BarEntry> loadBarEntriesFromAssets(AssetManager am, String path) {

        List<BarEntry> entries = new ArrayList<BarEntry>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(am.open(path), "UTF-8"));

            String line = reader.readLine();

            while (line != null) {
                String[] split = line.split("#");

                entries.add(new BarEntry(Float.parseFloat(split[1]), Float.parseFloat(split[0])));

                line = reader.readLine();
            }
        } catch (IOException e) {
            Log.e(LOG, e.toString());

        } finally {

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG, e.toString());
                }
            }
        }

        return entries;

    }
}
