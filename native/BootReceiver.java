package com.namya.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.Map;
import org.json.JSONObject;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        try {
            Map<String, ?> all = PopupCore.sp(c).getAll();
            long now = System.currentTimeMillis();
            for (Map.Entry<String, ?> en : all.entrySet()) {
                if (!en.getKey().startsWith("e_")) continue;
                JSONObject e = new JSONObject((String) en.getValue());
                String key = e.optString("key");
                long at = e.optLong("at");
                if (at <= now) {
                    at = PopupCore.nextAfter(e, at);
                    if (at <= 0) continue;
                    e.put("at", at);
                    PopupCore.put(c, e);
                }
                PopupCore.arm(c, key, false, 0, at);
            }
        } catch (Exception x) {
        }
    }
}
