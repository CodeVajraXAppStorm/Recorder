package com.namya.app;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import org.json.JSONObject;

@CapacitorPlugin(name = "OverlayPopup")
public class OverlayPopupPlugin extends Plugin {

    @PluginMethod
    public void status(PluginCall call) {
        Context c = getContext();
        JSObject o = new JSObject();
        o.put("overlay", Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(c));
        boolean exact = true;
        if (Build.VERSION.SDK_INT >= 31) {
            AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
            exact = am.canScheduleExactAlarms();
        }
        o.put("exact", exact);
        call.resolve(o);
    }

    @PluginMethod
    public void openOverlaySettings(PluginCall call) {
        try {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getContext().getPackageName()));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(i);
        } catch (Exception e) {
        }
        call.resolve();
    }

    @PluginMethod
    public void openExactSettings(PluginCall call) {
        try {
            if (Build.VERSION.SDK_INT >= 31) {
                Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + getContext().getPackageName()));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(i);
            }
        } catch (Exception e) {
        }
        call.resolve();
    }

    @PluginMethod
    public void schedule(PluginCall call) {
        try {
            JSArray arr = call.getArray("entries");
            Context c = getContext();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject e = arr.getJSONObject(i);
                String key = e.getString("key");
                PopupCore.cancelNag(c, key);
                PopupCore.put(c, e);
                PopupCore.arm(c, key, false, 0, e.getLong("at"));
            }
            call.resolve();
        } catch (Exception ex) {
            call.reject(String.valueOf(ex.getMessage()));
        }
    }

    @PluginMethod
    public void cancel(PluginCall call) {
        try {
            JSArray keys = call.getArray("keys");
            Context c = getContext();
            for (int i = 0; i < keys.length(); i++) {
                PopupCore.cancelAll(c, keys.getString(i));
            }
            PopupOverlay.hideAsync(c.getApplicationContext());
            call.resolve();
        } catch (Exception ex) {
            call.reject(String.valueOf(ex.getMessage()));
        }
    }

    @PluginMethod
    public void stopChain(PluginCall call) {
        String key = call.getString("key");
        if (key != null) PopupCore.done(getContext(), key);
        PopupOverlay.hideAsync(getContext().getApplicationContext());
        call.resolve();
    }

    @PluginMethod
    public void snooze(PluginCall call) {
        String key = call.getString("key");
        Integer minutes = call.getInt("minutes", 10);
        if (key != null) PopupCore.snooze(getContext(), key, 1, minutes);
        PopupOverlay.hideAsync(getContext().getApplicationContext());
        call.resolve();
    }
}
