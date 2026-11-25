package com.namya.app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.app.NotificationCompat;
import org.json.JSONObject;

public class PopupOverlay {
    private static View view;
    private static MediaPlayer mp;
    private static Vibrator vib;

    static void hideAsync(final Context c) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                hide(c);
            }
        });
    }

    static void hide(Context c) {
        try {
            if (view != null) {
                WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
                wm.removeView(view);
            }
        } catch (Exception x) {
        }
        view = null;
        stopFx();
    }

    private static TextView tv(Context c, String t, int size, int color, boolean bold) {
        TextView v = new TextView(c);
        v.setText(t);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setGravity(Gravity.CENTER);
        if (bold) v.setTypeface(Typeface.DEFAULT_BOLD);
        return v;
    }

    private static LinearLayout.LayoutParams lp(int w, int h, int top) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h);
        p.topMargin = top;
        return p;
    }

    static void show(final Context c, final JSONObject e, final int phase) {
        boolean can = Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(c);
        if (!can) {
            if (phase >= 1) fallback(c, e);
            return;
        }
        try {
            hide(c);
            wake(c);
            final String key = e.optString("key");
            final int ph = phase;
            final float d = c.getResources().getDisplayMetrics().density;

            FrameLayout root = new FrameLayout(c);
            root.setBackgroundColor(0x99000000);
            root.setClickable(true);

            LinearLayout card = new LinearLayout(c);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setGravity(Gravity.CENTER_HORIZONTAL);
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(0xFF15120D);
            float r = 26 * d;
            bg.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
            bg.setStroke((int) d, 0x55D4AF37);
            card.setBackground(bg);
            int pad = (int) (22 * d);
            card.setPadding(pad, (int) (14 * d), pad, (int) (26 * d));

            View grab = new View(c);
            GradientDrawable gb = new GradientDrawable();
            gb.setColor(0x55FFFFFF);
            gb.setCornerRadius(2 * d);
            grab.setBackground(gb);
            card.addView(grab, lp((int) (38 * d), (int) (4 * d), 0));

            TextView brand = tv(c, "NAMYA", 12, 0xFFD4AF37, true);
            brand.setLetterSpacing(0.25f);
            card.addView(brand, lp(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, (int) (16 * d)));

            card.addView(tv(c, e.optString("title", "Reminder"), 22, 0xFFFFFFFF, true),
                    lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, (int) (8 * d)));

            String bt = e.optString("body", "");
            if (phase >= 1) bt = bt + "\nAbhi tak Done nahi kiya";
            card.addView(tv(c, bt, 15, 0xFFB9B09C, false),
                    lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, (int) (6 * d)));

            TextView done = tv(c, "Done", 17, 0xFF1A1405, true);
            GradientDrawable db = new GradientDrawable();
            db.setColor(0xFFD4AF37);
            db.setCornerRadius(16 * d);
            done.setBackground(db);
            done.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    PopupCore.done(c, key);
                    hide(c);
                }
            });
            card.addView(done, lp(ViewGroup.LayoutParams.MATCH_PARENT, (int) (56 * d), (int) (22 * d)));

            TextView snz = tv(c, "10 minute baad yaad dilao", 15, 0xFFD4AF37, true);
            snz.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    PopupCore.snooze(c, key, ph, 10);
                    hide(c);
                }
            });
            card.addView(snz, lp(ViewGroup.LayoutParams.MATCH_PARENT, (int) (48 * d), (int) (6 * d)));

            root.addView(card, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));

            int type = Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
            WindowManager.LayoutParams wp = new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, type,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                    PixelFormat.TRANSLUCENT);
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            wm.addView(root, wp);
            view = root;
            card.setTranslationY(700 * d);
            card.animate().translationY(0).setDuration(280).start();
            if (phase >= 1) fx(c);
        } catch (Exception ex) {
            if (phase >= 1) fallback(c, e);
        }
    }

    private static void wake(Context c) {
        try {
            PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "namya:popup");
            wl.acquire(8000);
        } catch (Exception x) {
        }
    }

    private static void fx(Context c) {
        try {
            int id = c.getResources().getIdentifier("biwa_roll", "raw", c.getPackageName());
            if (id != 0) {
                AudioAttributes aa = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
                mp = MediaPlayer.create(c, id, aa, AudioManager.AUDIO_SESSION_ID_GENERATE);
                if (mp != null) mp.start();
            }
        } catch (Exception x) {
        }
        try {
            vib = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
            long[] pat = {0, 500, 200, 500, 200, 500};
            if (Build.VERSION.SDK_INT >= 26) vib.vibrate(VibrationEffect.createWaveform(pat, -1));
            else vib.vibrate(pat, -1);
        } catch (Exception x) {
        }
    }

    private static void stopFx() {
        try {
            if (mp != null) {
                mp.stop();
                mp.release();
            }
        } catch (Exception x) {
        }
        mp = null;
        try {
            if (vib != null) vib.cancel();
        } catch (Exception x) {
        }
    }

    private static void fallback(Context c, JSONObject e) {
        try {
            NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent li = c.getPackageManager().getLaunchIntentForPackage(c.getPackageName());
            PendingIntent pi = PendingIntent.getActivity(c, 1, li, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            NotificationCompat.Builder b = new NotificationCompat.Builder(c, "yd_alarm_v2")
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)
                    .setContentTitle(e.optString("title", "Reminder"))
                    .setContentText("Abhi tak Done nahi kiya")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pi);
            nm.notify(("fb" + e.optString("key")).hashCode(), b.build());
        } catch (Exception x) {
        }
    }
}
