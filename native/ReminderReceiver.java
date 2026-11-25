package com.namya.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.json.JSONObject;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        String key = i.getStringExtra("key");
        boolean nag = i.getBooleanExtra("nag", false);
        int phase = i.getIntExtra("phase", 0);
        if (key == null) return;
        JSONObject e = PopupCore.get(c, key);
        if (e == null) return;
        int n = e.optInt("n", 0);
        try {
            if (!nag) {
                long nx = PopupCore.nextAfter(e, e.optLong("at"));
                if (nx > 0) {
                    e.put("at", nx);
                    PopupCore.put(c, e);
                    PopupCore.arm(c, key, false, 0, nx);
                }
                phase = 0;
            }
            if (phase < n) {
                PopupCore.arm(c, key, true, phase + 1, System.currentTimeMillis() + PopupCore.NAG);
            }
        } catch (Exception ex) {
        }
        PopupOverlay.show(c.getApplicationContext(), e, phase);
    }
}
