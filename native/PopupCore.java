package com.namya.app;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import java.util.Calendar;
import org.json.JSONObject;

public class PopupCore {
    static final long NAG = 5 * 60 * 1000L;

    static SharedPreferences sp(Context c) {
        return c.getSharedPreferences("yd_popup_store", Context.MODE_PRIVATE);
    }

    static void put(Context c, JSONObject e) {
        try {
            sp(c).edit().putString("e_" + e.getString("key"), e.toString()).apply();
        } catch (Exception x) {
        }
    }

    static JSONObject get(Context c, String key) {
        try {
            String s = sp(c).getString("e_" + key, null);
            return s == null ? null : new JSONObject(s);
        } catch (Exception x) {
            return null;
        }
    }

    static PendingIntent pi(Context c, String key, boolean nag, int phase) {
        Intent i = new Intent(c, ReminderReceiver.class);
        i.setAction("com.namya.app.POPUP." + (nag ? "NAG." : "MAIN.") + key);
        i.putExtra("key", key);
        i.putExtra("nag", nag);
        i.putExtra("phase", phase);
        int req = ((nag ? "n" : "m") + key).hashCode();
        return PendingIntent.getBroadcast(c, req, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static void arm(Context c, String key, boolean nag, int phase, long at) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        PendingIntent p = pi(c, key, nag, phase);
        try {
            if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, p);
            } else if (Build.VERSION.SDK_INT >= 23) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, p);
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, at, p);
            }
        } catch (Exception ex) {
            try {
                am.set(AlarmManager.RTC_WAKEUP, at, p);
            } catch (Exception ignore) {
            }
        }
    }

    static void cancelNag(Context c, String key) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi(c, key, true, 0));
    }

    static void cancelAll(Context c, String key) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi(c, key, true, 0));
        am.cancel(pi(c, key, false, 0));
        sp(c).edit().remove("e_" + key).apply();
    }

    static long nextAfter(JSONObject e, long at) {
        String rep = e.optString("repeat", "none");
        int field;
        if ("daily".equals(rep)) field = Calendar.DAY_OF_YEAR;
        else if ("yearly".equals(rep)) field = Calendar.YEAR;
        else return -1;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(at);
        long now = System.currentTimeMillis();
        int guard = 0;
        do {
            cal.add(field, 1);
            guard++;
        } while (cal.getTimeInMillis() <= now + 1000 && guard < 4000);
        return cal.getTimeInMillis();
    }

    static void done(Context c, String key) {
        cancelNag(c, key);
        JSONObject e = get(c, key);
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (e != null) nm.cancel(e.optInt("mainId", -1));
        nm.cancel(("fb" + key).hashCode());
    }

    static void snooze(Context c, String key, int phase, int minutes) {
        cancelNag(c, key);
        arm(c, key, true, Math.max(phase, 1), System.currentTimeMillis() + minutes * 60000L);
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(("fb" + key).hashCode());
        JSONObject e = get(c, key);
        if (e != null) nm.cancel(e.optInt("mainId", -1));
    }
}
