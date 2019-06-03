package com.android.countdowntimer;

import android.content.Context;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

public class Utils {
    public static Set<String> getEvents(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getStringSet("EVENTS", new HashSet<String>());
    }

    public static void saveEvents(Context context, Set<String> events) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putStringSet("EVENTS", events)
                .apply();
    }
}
