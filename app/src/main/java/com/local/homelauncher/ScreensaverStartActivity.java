package com.local.homelauncher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.lang.reflect.Method;

public class ScreensaverStartActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSharedPreferences("launcher", 0).edit().putBoolean("screensaver_requested", true).apply();
        try {
            Object pm = getSystemService("power");
            Method m = pm.getClass().getMethod("dream");
            m.invoke(pm);
        } catch (Exception e) {
            try {
                startActivity(new Intent("android.intent.action.MAIN")
                        .setClassName("com.android.systemui", "com.android.systemui.Somnambulator"));
            } catch (Exception e2) {
                getSharedPreferences("launcher", 0).edit().remove("screensaver_requested").apply();
                Toast.makeText(this, "Cannot start screensaver", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}
