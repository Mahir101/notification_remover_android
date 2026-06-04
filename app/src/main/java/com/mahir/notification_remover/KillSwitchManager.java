package com.mahir.notification_remover;

import android.content.Context;
import android.content.SharedPreferences;
import android.service.notification.StatusBarNotification;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class KillSwitchManager {
    private static final String PREFS_NAME = "kill_switch";
    private static final String KEY_ACTIVE = "active";
    private static final String KEY_EXPIRES_AT = "expires_at";
    private static final String KEY_KILLED_KEYS = "killed_keys";

    public static final long DURATION_PERMANENT = 100L * 365 * 24 * 3600 * 1000;
    public static final long DURATION_30_MIN = 30 * 60 * 1000L;
    public static final long DURATION_1_HOUR = 60 * 60 * 1000L;
    public static final long DURATION_2_HOURS = 2 * 60 * 60 * 1000L;
    public static final long DURATION_4_HOURS = 4 * 60 * 60 * 1000L;

    private final SharedPreferences prefs;

    public KillSwitchManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void activate(NotificationListener service, long durationMs) {
        List<String> killedKeys = new ArrayList<>();

        StatusBarNotification[] active = service.getActiveNotifications();
        if (active != null) {
            for (StatusBarNotification sbn : active) {
                service.snoozeOngoingNotification(sbn, durationMs);
                killedKeys.add(sbn.getKey());
            }
        }

        long expiresAt = durationMs == DURATION_PERMANENT ? 0 : System.currentTimeMillis() + durationMs;

        JSONArray arr = new JSONArray();
        for (String key : killedKeys) arr.put(key);

        prefs.edit()
                .putBoolean(KEY_ACTIVE, true)
                .putLong(KEY_EXPIRES_AT, expiresAt)
                .putString(KEY_KILLED_KEYS, arr.toString())
                .apply();
    }

    public void deactivate(NotificationListener service) {
        if (service != null) {
            for (String key : getKilledKeys()) {
                try {
                    service.snoozeNotification(key, 1);
                } catch (Exception ignored) {}
            }
        }
        prefs.edit()
                .putBoolean(KEY_ACTIVE, false)
                .putLong(KEY_EXPIRES_AT, 0)
                .putString(KEY_KILLED_KEYS, "[]")
                .apply();
    }

    public boolean isActive() {
        if (!prefs.getBoolean(KEY_ACTIVE, false)) return false;
        long expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0);
        if (expiresAt != 0 && System.currentTimeMillis() >= expiresAt) {
            // Expired — clean up state (service will have already auto-restored)
            prefs.edit().putBoolean(KEY_ACTIVE, false).putString(KEY_KILLED_KEYS, "[]").apply();
            return false;
        }
        return true;
    }

    public long getExpiresAt() {
        return prefs.getLong(KEY_EXPIRES_AT, 0);
    }

    public List<String> getKilledKeys() {
        String json = prefs.getString(KEY_KILLED_KEYS, "[]");
        List<String> result = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) result.add(arr.getString(i));
        } catch (JSONException ignored) {}
        return result;
    }
}
