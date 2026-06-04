package com.mahir.notification_remover;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RulesManager {
    private static final String PREFS_NAME = "notification_rules";
    private static final String KEY_BLOCKED_PACKAGES = "blocked_packages";
    private static final String KEY_KEYWORDS = "keyword_rules";

    private final SharedPreferences prefs;

    public RulesManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean matches(StatusBarNotification sbn) {
        if (isPackageBlocked(sbn.getPackageName())) return true;

        List<String> keywords = getKeywords();
        if (!keywords.isEmpty()) {
            Bundle extras = sbn.getNotification().extras;
            CharSequence title = extras.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
            String titleStr = title != null ? title.toString().toLowerCase() : "";
            String textStr = text != null ? text.toString().toLowerCase() : "";
            for (String kw : keywords) {
                String kwLower = kw.toLowerCase();
                if (titleStr.contains(kwLower) || textStr.contains(kwLower)) return true;
            }
        }
        return false;
    }

    public void blockPackage(String packageName, String appName) {
        JSONObject obj = getBlockedPackagesJson();
        try {
            obj.put(packageName, appName != null ? appName : packageName);
        } catch (JSONException ignored) {
            return;
        }
        prefs.edit().putString(KEY_BLOCKED_PACKAGES, obj.toString()).apply();
    }

    public void unblockPackage(String packageName) {
        JSONObject obj = getBlockedPackagesJson();
        obj.remove(packageName);
        prefs.edit().putString(KEY_BLOCKED_PACKAGES, obj.toString()).apply();
    }

    public boolean isPackageBlocked(String packageName) {
        return getBlockedPackagesJson().has(packageName);
    }

    public Map<String, String> getBlockedPackages() {
        JSONObject obj = getBlockedPackagesJson();
        Map<String, String> result = new LinkedHashMap<>();
        Iterator<String> keys = obj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                result.put(key, obj.getString(key));
            } catch (JSONException ignored) {}
        }
        return result;
    }

    public void addKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return;
        JSONArray arr = getKeywordsJson();
        for (int i = 0; i < arr.length(); i++) {
            try {
                if (arr.getString(i).equalsIgnoreCase(keyword.trim())) return;
            } catch (JSONException ignored) {}
        }
        arr.put(keyword.trim());
        prefs.edit().putString(KEY_KEYWORDS, arr.toString()).apply();
    }

    public void removeKeyword(String keyword) {
        JSONArray arr = getKeywordsJson();
        JSONArray updated = new JSONArray();
        for (int i = 0; i < arr.length(); i++) {
            try {
                if (!arr.getString(i).equalsIgnoreCase(keyword)) {
                    updated.put(arr.getString(i));
                }
            } catch (JSONException ignored) {}
        }
        prefs.edit().putString(KEY_KEYWORDS, updated.toString()).apply();
    }

    public List<String> getKeywords() {
        JSONArray arr = getKeywordsJson();
        List<String> result = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            try {
                result.add(arr.getString(i));
            } catch (JSONException ignored) {}
        }
        return result;
    }

    private JSONObject getBlockedPackagesJson() {
        String json = prefs.getString(KEY_BLOCKED_PACKAGES, "{}");
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    private JSONArray getKeywordsJson() {
        String json = prefs.getString(KEY_KEYWORDS, "[]");
        try {
            return new JSONArray(json);
        } catch (JSONException e) {
            return new JSONArray();
        }
    }
}
