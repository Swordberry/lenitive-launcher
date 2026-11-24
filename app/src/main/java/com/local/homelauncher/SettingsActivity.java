package com.local.homelauncher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/* loaded from: classes3.dex */
public class SettingsActivity extends Activity {
    private static final int REQ_CAT_FOLDER = 1007;
    private static final int REQ_CAT_IMAGE = 1005;
    private static final int REQ_CAT_VIDEO = 1006;
    private static final int REQ_SLIDESHOW_FOLDER = 1004;
    private static final int REQ_VIDEO_WALLPAPER = 1003;
    private static final int REQ_WALLPAPER = 1001;
    private static final String STREAM_BUTTON_FILE = "stream_buttons.conf";
    private static final String[] STREAM_BUTTON_KEYS = {
            "button_1", "button_2", "button_3", "button_4"
    };
    private static final String[] STREAM_BUTTON_LABELS = {
            "App button 1 (APP_1 / 02e8)",
            "Prime Video button (APP_2 / 02e9)",
            "Disney+ button (APP_3 / 02ea)",
            "App button 4 (APP_4 / 02eb)"
    };
    private LinearLayout panel;
    private String pendingCat = null;
    private int pendingFolderReq = 0;
    private java.io.File browserDir = null;

    private static class AppChoice {
        final String label;
        final String component;

        AppChoice(String label, String component) {
            this.label = label;
            this.component = component;
        }
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildMainMenu();
    }

    public void buildMainMenu() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(1711276032);
        root.setOnClickListener(new View.OnClickListener() { // from class: com.local.homelauncher.SettingsActivity$$ExternalSyntheticLambda3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SettingsActivity.this.m89lambda$buildMainMenu$0$comlocalhomelauncherSettingsActivity(view);
            }
        });
        this.panel = new LinearLayout(this);
        this.panel.setOrientation(1);
        this.panel.setPadding(36, 28, 36, 28);
        this.panel.setClickable(true);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(-233170406);
        bg.setCornerRadius(24.0f);
        bg.setStroke(2, 872415231);
        this.panel.setBackground(bg);
        addTitle(this.panel, "Launcher settings");
        addOption(this.panel, R.drawable.ic_piv_settings, "Pick wallpaper image", new Runnable() { // from class: com.local.homelauncher.SettingsActivity$$ExternalSyntheticLambda15
            @Override // java.lang.Runnable
            public final void run() {
                SettingsActivity.this.m90lambda$buildMainMenu$1$comlocalhomelauncherSettingsActivity();
            }
        });
        addOption(this.panel, R.drawable.ic_piv_settings, "Pick video wallpaper (muted)", new Runnable() { // from class: com.local.homelauncher.SettingsActivity$$ExternalSyntheticLambda16
            @Override // java.lang.Runnable
            public final void run() {
                SettingsActivity.this.m99lambda$buildMainMenu$2$comlocalhomelauncherSettingsActivity();
            }
        });
        addOption(this.panel, R.drawable.ic_piv_delete, "Clear wallpaper/video", new Runnable() { // from class: com.local.homelauncher.SettingsActivity$$ExternalSyntheticLambda17
            @Override // java.lang.Runnable
            public final void run() {
                SettingsActivity.this.m100lambda$buildMainMenu$3$comlocalhomelauncherSettingsActivity();
            }
        });
        addOption(this.panel, R.drawable.ic_piv_settings, "Pick slideshow folder", new Runnable() { // from class: com.local.homelauncher.SettingsActivity$$ExternalSyntheticLambda18
            @Override // java.lang.Runnable
            public final void run() {
                SettingsActivity.this.m101lambda$buildMainMenu$4$comlocalhomelauncherSettingsActivity();
            }
        });
        addOption(this.panel, R.drawable.ic_piv_delete, "Clear slideshow folder", new Runnable() { // from class: com.local.homelauncher.SettingsActivity$$ExternalSyntheticLambda19
            @Override // java.lang.Runnable
            public final void run() {
                SettingsActivity.this.m102lambda$buildMainMenu$5$comlocalhomelauncherSettingsActivity();
            }
        });
        int interval = prefs().getInt("slideshow_interval_ms", 15000);
        String intervalLabel = interval >= 60000 ? (interval / 60000) + " min" : (interval / 1000) + "s";
        addOption(this.panel, R.drawable.ic_piv_settings, "Slideshow interval: " + intervalLabel, new Runnable() { // from class: com.local.homelauncher.SettingsActivity$$ExternalSyntheticLambda20
            @Override // java.lang.Runnable
            public final void run() {
                SettingsActivity.this.buildSlideshowIntervalMenu();
            }
        });
        boolean ssRandom = prefs().getBoolean("slideshow_random", true);
        addOption(this.panel, R.drawable.ic_piv_settings, "Slideshow random: " + (ssRandom ? "ON" : "OFF"), new Runnable() { @Override public void run() {
            boolean v = !prefs().getBoolean("slideshow_random", true);
            prefs().edit().putBoolean("slideshow_random", v).apply();
            Toast.makeText(SettingsActivity.this, "Slideshow random: " + (v ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
            buildMainMenu();
        }});
        addOption(this.panel, R.drawable.ic_piv_settings, "Category wallpapers", new Runnable() { @Override public void run() { SettingsActivity.this.buildCategoryMenu(); } });
        boolean prevent = prefs().getBoolean("prevent_screensaver", false);
        addOption(this.panel, R.drawable.ic_piv_hide, "Prevent system screensaver: " + (prevent ? "ON" : "OFF"), new Runnable() { // from class: com.local.homelauncher.SettingsActivity$$ExternalSyntheticLambda9
            @Override // java.lang.Runnable
            public final void run() {
                SettingsActivity.this.m95lambda$buildMainMenu$14$comlocalhomelauncherSettingsActivity();
            }
        });
        addOption(this.panel, R.drawable.ic_piv_hide, "Unhide all apps", new Runnable() { // from class: com.local.homelauncher.SettingsActivity$$ExternalSyntheticLambda10
            @Override // java.lang.Runnable
            public final void run() {
                SettingsActivity.this.m96lambda$buildMainMenu$15$comlocalhomelauncherSettingsActivity();
            }
        });
        addOption(this.panel, R.drawable.ic_piv_move, "Reset app order", new Runnable() { // from class: com.local.homelauncher.SettingsActivity$$ExternalSyntheticLambda12
            @Override // java.lang.Runnable
            public final void run() {
                SettingsActivity.this.m97lambda$buildMainMenu$16$comlocalhomelauncherSettingsActivity();
            }
        });
        addOption(this.panel, R.drawable.ic_piv_settings, "Streaming button bindings", new Runnable() {
            @Override
            public void run() {
                SettingsActivity.this.buildStreamButtonMenu();
            }
        });
        addOption(this.panel, 0, "Cancel", new Runnable() { // from class: com.local.homelauncher.SettingsActivity$$ExternalSyntheticLambda13
            @Override // java.lang.Runnable
            public final void run() {
                SettingsActivity.this.finish();
            }
        });
        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(false);
        sv.addView(this.panel);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams((int) (getResources().getDisplayMetrics().widthPixels * 0.32d), (int) (getResources().getDisplayMetrics().heightPixels * 0.82d));
        lp.gravity = 17;
        root.removeAllViews();
        root.addView(sv, lp);
        root.setOnClickListener(new View.OnClickListener() { // from class: com.local.homelauncher.SettingsActivity$$ExternalSyntheticLambda14
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SettingsActivity.this.m98lambda$buildMainMenu$17$comlocalhomelauncherSettingsActivity(view);
            }
        });
        sv.setClickable(true);
        setContentView(root);
        focusFirst(this.panel);
    }

    /* renamed from: lambda$buildMainMenu$0$com-local-homelauncher-SettingsActivity */
    public /* synthetic */ void m89lambda$buildMainMenu$0$comlocalhomelauncherSettingsActivity(View v) {
        finish();
    }

    /* renamed from: lambda$buildMainMenu$1$com-local-homelauncher-SettingsActivity */
    public /* synthetic */ void m90lambda$buildMainMenu$1$comlocalhomelauncherSettingsActivity() {
        pick(1001, "image/*");
    }

    /* renamed from: lambda$buildMainMenu$2$com-local-homelauncher-SettingsActivity */
    public /* synthetic */ void m99lambda$buildMainMenu$2$comlocalhomelauncherSettingsActivity() {
        pick(1003, "video/*");
    }

    /* renamed from: lambda$buildMainMenu$3$com-local-homelauncher-SettingsActivity */
    public /* synthetic */ void m100lambda$buildMainMenu$3$comlocalhomelauncherSettingsActivity() {
        prefs().edit().remove("wallpaper_uri").remove("wallpaper_type").apply();
        Toast.makeText(this, "Wallpaper cleared", 0).show();
    }

    /* renamed from: lambda$buildMainMenu$4$com-local-homelauncher-SettingsActivity */
    public /* synthetic */ void m101lambda$buildMainMenu$4$comlocalhomelauncherSettingsActivity() {
        pickFolder(1004);
    }

    /* renamed from: lambda$buildMainMenu$5$com-local-homelauncher-SettingsActivity */
    public /* synthetic */ void m102lambda$buildMainMenu$5$comlocalhomelauncherSettingsActivity() {
        prefs().edit().remove("slideshow_folder_uri").remove("wallpaper_type").apply();
        Toast.makeText(this, "Slideshow cleared", 0).show();
    }

    public void buildSlideshowIntervalMenu() {
        android.widget.FrameLayout root = new android.widget.FrameLayout(this);
        root.setBackgroundColor(0x66000000);
        root.setOnClickListener(v -> buildMainMenu());
        android.widget.LinearLayout p = new android.widget.LinearLayout(this);
        p.setOrientation(android.widget.LinearLayout.VERTICAL);
        p.setPadding(36, 28, 36, 28);
        p.setClickable(true);
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(0xF21A1A1A); bg.setCornerRadius(24f); bg.setStroke(2, 0x33FFFFFF);
        p.setBackground(bg);
        addTitle(p, "Slideshow interval");
        int cur = prefs().getInt("slideshow_interval_ms", 15000);
        for (int min = 1; min <= 60; min++) {
            final int ms = min * 60 * 1000;
            final int m = min;
            String label = m + " min" + (cur == ms ? " ✓" : "");
            addOption(p, 0, label, () -> {
                prefs().edit().putInt("slideshow_interval_ms", ms).apply();
                Toast.makeText(SettingsActivity.this, "Slideshow interval: " + m + " min", Toast.LENGTH_SHORT).show();
                buildMainMenu();
            });
        }
        addOption(p, 0, "Back", this::buildMainMenu);
        android.widget.ScrollView sv = new android.widget.ScrollView(this);
        sv.setVerticalScrollBarEnabled(false);
        sv.addView(p);
        android.widget.FrameLayout.LayoutParams lp = new android.widget.FrameLayout.LayoutParams((int)(getResources().getDisplayMetrics().widthPixels*0.32), (int)(getResources().getDisplayMetrics().heightPixels*0.82));
        lp.gravity = android.view.Gravity.CENTER;
        root.addView(sv, lp);
        sv.setClickable(true);
        setContentView(root);
        focusFirst(p);
    }

    /* renamed from: lambda$buildMainMenu$14$com-local-homelauncher-SettingsActivity */
    public /* synthetic */ void m95lambda$buildMainMenu$14$comlocalhomelauncherSettingsActivity() {
        boolean cur = prefs().getBoolean("prevent_screensaver", false);
        prefs().edit().putBoolean("prevent_screensaver", !cur).apply();
        Toast.makeText(this, "Prevent system screensaver: " + (!cur ? "ON" : "OFF"), 0).show();
        buildMainMenu();
    }

    /* renamed from: lambda$buildMainMenu$15$com-local-homelauncher-SettingsActivity */
    public /* synthetic */ void m96lambda$buildMainMenu$15$comlocalhomelauncherSettingsActivity() {
        prefs().edit().remove("hidden_apps").apply();
        Toast.makeText(this, "All apps unhidden", 0).show();
    }

    /* renamed from: lambda$buildMainMenu$16$com-local-homelauncher-SettingsActivity */
    public /* synthetic */ void m97lambda$buildMainMenu$16$comlocalhomelauncherSettingsActivity() {
        prefs().edit().remove("app_order").apply();
        Toast.makeText(this, "App order reset", 0).show();
    }

    /* renamed from: lambda$buildMainMenu$17$com-local-homelauncher-SettingsActivity */
    public /* synthetic */ void m98lambda$buildMainMenu$17$comlocalhomelauncherSettingsActivity(View v) {
        finish();
    }

    public void buildCategoryMenu() {
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setBackgroundColor(1711276032);
        frameLayout.setOnClickListener(new View.OnClickListener() { // from class: com.local.homelauncher.SettingsActivity$$ExternalSyntheticLambda27
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SettingsActivity.this.m87x35ec714b(view);
            }
        });
        LinearLayout p = new LinearLayout(this);
        p.setOrientation(1);
        p.setPadding(36, 28, 36, 28);
        p.setClickable(true);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(-233170406);
        bg.setCornerRadius(24.0f);
        bg.setStroke(2, 872415231);
        p.setBackground(bg);
        addTitle(p, "Category wallpapers");
        String[] strArr = {"Favorites", "Apps"};
        for (int i = 0; i < 2; i++) {
            final String cat = strArr[i];
            addOption(p, R.drawable.ic_piv_settings, cat, new Runnable() { // from class: com.local.homelauncher.SettingsActivity$$ExternalSyntheticLambda28
                @Override // java.lang.Runnable
                public final void run() {
                    SettingsActivity.this.m88xc28c9c4c(cat);
                }
            });
        }
        addOption(p, 0, "Back", new Runnable() { // from class: com.local.homelauncher.SettingsActivity$$ExternalSyntheticLambda29
            @Override // java.lang.Runnable
            public final void run() {
                SettingsActivity.this.buildMainMenu();
            }
        });
        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(false);
        sv.addView(p);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams((int) (getResources().getDisplayMetrics().widthPixels * 0.32d), (int) (getResources().getDisplayMetrics().heightPixels * 0.82d));
        lp.gravity = 17;
        frameLayout.addView(sv, lp);
        sv.setClickable(true);
        setContentView(frameLayout);
        focusFirst(p);
    }

    /* renamed from: lambda$buildCategoryMenu$18$com-local-homelauncher-SettingsActivity */
    public /* synthetic */ void m87x35ec714b(View v) {
        buildMainMenu();
    }

    /* renamed from: buildCategoryDetail */
    public void m88xc28c9c4c(final String cat) {
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setBackgroundColor(1711276032);
        frameLayout.setOnClickListener(new View.OnClickListener() { // from class: com.local.homelauncher.SettingsActivity$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SettingsActivity.this.m82x6befc7f0(view);
            }
        });
        LinearLayout p = new LinearLayout(this);
        p.setOrientation(1);
        p.setPadding(36, 28, 36, 28);
        p.setClickable(true);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(-233170406);
        bg.setCornerRadius(24.0f);
        bg.setStroke(2, 872415231);
        p.setBackground(bg);
        addTitle(p, cat + " wallpaper");
        addOption(p, R.drawable.ic_piv_settings, "Pick image", new Runnable() { // from class: com.local.homelauncher.SettingsActivity$$ExternalSyntheticLambda11
            @Override // java.lang.Runnable
            public final void run() {
                SettingsActivity.this.m83xf88ff2f1(cat);
            }
        });
        addOption(p, R.drawable.ic_piv_settings, "Pick video (muted)", new Runnable() { // from class: com.local.homelauncher.SettingsActivity$$ExternalSyntheticLambda22
            @Override // java.lang.Runnable
            public final void run() {
                SettingsActivity.this.m84x85301df2(cat);
            }
        });
        addOption(p, R.drawable.ic_piv_settings, "Pick slideshow folder", new Runnable() { // from class: com.local.homelauncher.SettingsActivity$$ExternalSyntheticLambda24
            @Override // java.lang.Runnable
            public final void run() {
                SettingsActivity.this.m85x11d048f3(cat);
            }
        });
        boolean catRandom = prefs().getBoolean("cat_wallpaper_random_" + cat, prefs().getBoolean("slideshow_random", true));
        addOption(p, R.drawable.ic_piv_settings, "Slideshow random: " + (catRandom ? "ON" : "OFF"), new Runnable() { @Override public void run() {
            boolean v = !prefs().getBoolean("cat_wallpaper_random_" + cat, prefs().getBoolean("slideshow_random", true));
            prefs().edit().putBoolean("cat_wallpaper_random_" + cat, v).apply();
            Toast.makeText(SettingsActivity.this, "Slideshow random: " + (v ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
            m88xc28c9c4c(cat);
        }});
        addOption(p, R.drawable.ic_piv_delete, "Clear category wallpaper", new Runnable() { // from class: com.local.homelauncher.SettingsActivity$$ExternalSyntheticLambda25
            @Override // java.lang.Runnable
            public final void run() {
                SettingsActivity.this.m86x9e7073f4(cat);
            }
        });
        addOption(p, 0, "Back", new Runnable() { @Override public void run() { SettingsActivity.this.buildCategoryMenu(); } });
        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(false);
        sv.addView(p);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams((int) (getResources().getDisplayMetrics().widthPixels * 0.32d), -2);
        lp.gravity = 17;
        frameLayout.addView(sv, lp);
        sv.setClickable(true);
        setContentView(frameLayout);
        focusFirst(p);
    }

    /* renamed from: lambda$buildCategoryDetail$20$com-local-homelauncher-SettingsActivity */
    public /* synthetic */ void m82x6befc7f0(View v) {
        buildCategoryMenu();
    }

    /* renamed from: lambda$buildCategoryDetail$21$com-local-homelauncher-SettingsActivity */
    public /* synthetic */ void m83xf88ff2f1(String cat) {
        this.pendingCat = cat;
        pick(REQ_CAT_IMAGE, "image/*");
    }

    /* renamed from: lambda$buildCategoryDetail$22$com-local-homelauncher-SettingsActivity */
    public /* synthetic */ void m84x85301df2(String cat) {
        this.pendingCat = cat;
        pick(1006, "video/*");
    }

    /* renamed from: lambda$buildCategoryDetail$23$com-local-homelauncher-SettingsActivity */
    public /* synthetic */ void m85x11d048f3(String cat) {
        this.pendingCat = cat;
        pickFolder(1007);
    }

    /* renamed from: lambda$buildCategoryDetail$24$com-local-homelauncher-SettingsActivity */
    public /* synthetic */ void m86x9e7073f4(String cat) {
        prefs().edit().remove("cat_wallpaper_type_" + cat).remove("cat_wallpaper_uri_" + cat).remove("cat_wallpaper_folder_" + cat).apply();
        Toast.makeText(this, "Cleared " + cat + " wallpaper", 0).show();
        m88xc28c9c4c(cat);
    }

    public void buildStreamButtonMenu() {
        syncBindingsFromFile();

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0x66000000);
        root.setOnClickListener(v -> buildMainMenu());

        LinearLayout p = new LinearLayout(this);
        p.setOrientation(LinearLayout.VERTICAL);
        p.setPadding(36, 28, 36, 28);
        p.setClickable(true);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xF21A1A1A);
        bg.setCornerRadius(24f);
        bg.setStroke(2, 0x33FFFFFF);
        p.setBackground(bg);

        addTitle(p, "Streaming button bindings");
        for (int i = 0; i < STREAM_BUTTON_KEYS.length; i++) {
            final int index = i;
            String binding = prefs().getString(STREAM_BUTTON_KEYS[i], "");
            addOption(p, R.drawable.ic_piv_settings,
                    STREAM_BUTTON_LABELS[i] + ": " + bindingLabel(binding),
                    () -> buildStreamButtonPicker(index));
        }
        addOption(p, R.drawable.ic_piv_delete, "Clear all bindings", () -> {
            SharedPreferences.Editor editor = prefs().edit();
            for (String key : STREAM_BUTTON_KEYS) {
                editor.putString(key, "");
            }
            editor.apply();
            writeBindings();
            Toast.makeText(SettingsActivity.this, "Streaming button bindings cleared", Toast.LENGTH_SHORT).show();
            buildStreamButtonMenu();
        });
        addOption(p, 0, "Back", this::buildMainMenu);

        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(false);
        sv.addView(p);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.32d),
                (int) (getResources().getDisplayMetrics().heightPixels * 0.82d));
        lp.gravity = android.view.Gravity.CENTER;
        root.addView(sv, lp);
        sv.setClickable(true);
        setContentView(root);
        focusFirst(p);
    }

    public void buildStreamButtonPicker(final int index) {
        syncBindingsFromFile();

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0x66000000);
        root.setOnClickListener(v -> buildStreamButtonMenu());

        LinearLayout p = new LinearLayout(this);
        p.setOrientation(LinearLayout.VERTICAL);
        p.setPadding(36, 28, 36, 28);
        p.setClickable(true);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xF21A1A1A);
        bg.setCornerRadius(24f);
        bg.setStroke(2, 0x33FFFFFF);
        p.setBackground(bg);

        addTitle(p, "Bind " + STREAM_BUTTON_LABELS[index]);
        addOption(p, R.drawable.ic_piv_delete, "Default / no binding", () -> {
            prefs().edit().putString(STREAM_BUTTON_KEYS[index], "").apply();
            writeBindings();
            Toast.makeText(SettingsActivity.this, STREAM_BUTTON_LABELS[index] + " reset to default", Toast.LENGTH_SHORT).show();
            buildStreamButtonMenu();
        });

        List<AppChoice> apps = collectLaunchableApps();
        if (apps.isEmpty()) {
            addOption(p, 0, "No launchable apps found", () -> { });
        } else {
            for (AppChoice app : apps) {
                final String component = app.component;
                addOption(p, 0, app.label, () -> {
                    prefs().edit().putString(STREAM_BUTTON_KEYS[index], component).apply();
                    writeBindings();
                    Toast.makeText(SettingsActivity.this,
                            STREAM_BUTTON_LABELS[index] + " -> " + app.label,
                            Toast.LENGTH_SHORT).show();
                    buildStreamButtonMenu();
                });
            }
        }
        addOption(p, 0, "Back", this::buildStreamButtonMenu);

        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(false);
        sv.addView(p);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.32d),
                (int) (getResources().getDisplayMetrics().heightPixels * 0.82d));
        lp.gravity = android.view.Gravity.CENTER;
        root.addView(sv, lp);
        sv.setClickable(true);
        setContentView(root);
        focusFirst(p);
    }

    private List<AppChoice> collectLaunchableApps() {
        PackageManager pm = getPackageManager();
        Map<String, AppChoice> apps = new LinkedHashMap<>();

        Intent leanback = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        for (ResolveInfo info : pm.queryIntentActivities(leanback, 0)) {
            addAppChoice(pm, apps, info);
        }
        Intent standard = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        for (ResolveInfo info : pm.queryIntentActivities(standard, 0)) {
            addAppChoice(pm, apps, info);
        }

        List<AppChoice> result = new ArrayList<>(apps.values());
        Collections.sort(result, (a, b) -> a.label.compareToIgnoreCase(b.label));
        return result;
    }

    private void addAppChoice(PackageManager pm, Map<String, AppChoice> apps, ResolveInfo info) {
        if (info == null || info.activityInfo == null) {
            return;
        }
        String component = new ComponentName(info.activityInfo.packageName, info.activityInfo.name).flattenToString();
        if (apps.containsKey(component)) {
            return;
        }
        CharSequence label = info.loadLabel(pm);
        String name = label != null ? label.toString() : info.activityInfo.packageName;
        apps.put(component, new AppChoice(name, component));
    }

    private String bindingLabel(String component) {
        if (component == null || component.isEmpty()) {
            return "Default";
        }
        String pkg = component.split("/", 2)[0];
        try {
            PackageManager pm = getPackageManager();
            return pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
        } catch (Exception ignored) {
            return pkg;
        }
    }

    private void syncBindingsFromFile() {
        File file = new File(getFilesDir(), STREAM_BUTTON_FILE);
        if (!file.exists()) {
            return;
        }
        SharedPreferences.Editor editor = prefs().edit();
        boolean changed = false;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int separator = line.indexOf('=');
                if (separator <= 0) {
                    continue;
                }
                String key = line.substring(0, separator).trim();
                String value = line.substring(separator + 1).trim();
                for (String knownKey : STREAM_BUTTON_KEYS) {
                    if (knownKey.equals(key) && !prefs().contains(knownKey)) {
                        editor.putString(knownKey, value);
                        changed = true;
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        if (changed) {
            editor.apply();
        }
    }

    private void writeBindings() {
        StringBuilder data = new StringBuilder();
        for (String key : STREAM_BUTTON_KEYS) {
            data.append(key).append('=').append(prefs().getString(key, "")).append('\n');
        }
        try (FileOutputStream out = new FileOutputStream(new File(getFilesDir(), STREAM_BUTTON_FILE), false)) {
            out.write(data.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) {
            Toast.makeText(this, "Could not save streaming button bindings", Toast.LENGTH_SHORT).show();
        }
    }

    private void addTitle(LinearLayout panel, String text) {
        TextView title = new TextView(this);
        title.setText(text);
        title.setTextColor(-1);
        title.setTextSize(20.0f);
        title.setPadding(0, 0, 0, 16);
        title.setTypeface(Typeface.create("sans-serif-medium", 0));
        panel.addView(title);
    }

    private void addOption(LinearLayout linearLayout, int iconRes, String text, final Runnable action) {
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        linearLayout2.setGravity(16);
        linearLayout2.setPadding(24, 18, 24, 18);
        linearLayout2.setFocusable(true);
        linearLayout2.setClickable(true);
        GradientDrawable n = new GradientDrawable();
        n.setColor(0);
        n.setCornerRadius(14.0f);
        GradientDrawable f = new GradientDrawable();
        f.setColor(872415231);
        f.setCornerRadius(14.0f);
        StateListDrawable sld = new StateListDrawable();
        sld.addState(new int[]{android.R.attr.state_focused}, f);
        sld.addState(new int[]{android.R.attr.state_pressed}, f);
        sld.addState(new int[0], n);
        linearLayout2.setBackground(sld);
        ImageView iv = new ImageView(this);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(48, 48);
        ilp.setMarginEnd(20);
        iv.setLayoutParams(ilp);
        if (iconRes != 0) {
            iv.setImageResource(iconRes);
        } else {
            iv.setVisibility(4);
        }
        iv.setColorFilter(-1118482);
        final TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(-285212673);
        tv.setTextSize(18.0f);
        tv.setTypeface(Typeface.create("sans-serif", 0));
        linearLayout2.addView(iv);
        linearLayout2.addView(tv);
        linearLayout2.setOnClickListener(new View.OnClickListener() { // from class: com.local.homelauncher.SettingsActivity$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                action.run();
            }
        });
        linearLayout2.setOnFocusChangeListener(new View.OnFocusChangeListener() { // from class: com.local.homelauncher.SettingsActivity$$ExternalSyntheticLambda2
            @Override // android.view.View.OnFocusChangeListener
            public final void onFocusChange(View view, boolean z) {
                tv.setTextColor(z ? -1 : -285212673);
            }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = 4;
        linearLayout2.setLayoutParams(lp);
        linearLayout.addView(linearLayout2);
    }

    private void focusFirst(final LinearLayout p) {
        p.post(new Runnable() { // from class: com.local.homelauncher.SettingsActivity$$ExternalSyntheticLambda30
            @Override // java.lang.Runnable
            public final void run() {
                SettingsActivity.lambda$focusFirst$27(p);
            }
        });
    }

    public static /* synthetic */ void lambda$focusFirst$27(LinearLayout p) {
        for (int i = 0; i < p.getChildCount(); i++) {
            View c = p.getChildAt(i);
            if (c.isFocusable()) {
                c.requestFocus();
                return;
            }
        }
    }

    private void pick(int req, String type) {
        Intent i = new Intent("android.intent.action.OPEN_DOCUMENT");
        i.addCategory("android.intent.category.OPENABLE");
        i.setType(type);
        i.addFlags(65);
        try {
            startActivityForResult(i, req);
        } catch (Exception e) {
            try {
                Intent alt = new Intent("android.intent.action.GET_CONTENT");
                alt.addCategory("android.intent.category.OPENABLE");
                alt.setType(type);
                startActivityForResult(Intent.createChooser(alt, "Pick"), req);
            } catch (Exception e2) {
                Toast.makeText(this, "Cannot open picker", 0).show();
            }
        }
    }

    private void pickFolder(int req) {
        this.pendingFolderReq = req;
        java.io.File start = new java.io.File("/sdcard/Documents");
        if (!start.isDirectory()) start = new java.io.File("/sdcard");
        if (!start.isDirectory()) start = android.os.Environment.getExternalStorageDirectory();
        openFolderBrowser(start);
    }

    private void openFolderBrowser(final java.io.File dir) {
        this.browserDir = dir;
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0x66000000);
        root.setOnClickListener(v -> buildMainMenu());
        LinearLayout p = new LinearLayout(this);
        p.setOrientation(LinearLayout.VERTICAL);
        p.setPadding(36, 28, 36, 28);
        p.setClickable(true);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xF21A1A1A); bg.setCornerRadius(24f); bg.setStroke(2, 0x33FFFFFF);
        p.setBackground(bg);
        String pathStr = dir.getAbsolutePath();
        addTitle(p, "Select folder");
        TextView pathTv = new TextView(this);
        pathTv.setText(pathStr);
        pathTv.setTextColor(0xFFB0B0B0);
        pathTv.setTextSize(12f);
        pathTv.setPadding(18, 0, 18, 14);
        pathTv.setSingleLine(true);
        pathTv.setEllipsize(android.text.TextUtils.TruncateAt.START);
        p.addView(pathTv);

        addOption(p, 0, "✓ Use this folder", () -> selectFolder(dir));

        java.io.File parent = dir.getParentFile();
        if (parent != null && parent.canRead()) {
            final java.io.File par = parent;
            addOption(p, 0, "⬆ Up: " + par.getName(), () -> openFolderBrowser(par));
        }

        java.io.File[] files = dir.listFiles();
        java.util.List<java.io.File> dirs = new java.util.ArrayList<>();
        if (files != null) {
            for (java.io.File f : files) {
                if (f.isDirectory() && !f.getName().startsWith(".")) dirs.add(f);
            }
            java.util.Collections.sort(dirs, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        }
        for (java.io.File d : dirs) {
            final java.io.File fd = d;
            addOption(p, 0, "📁 " + fd.getName(), () -> openFolderBrowser(fd));
        }
        if (dirs.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No subfolders");
            empty.setTextColor(0xFF888888);
            empty.setTextSize(14f);
            empty.setPadding(18, 10, 18, 10);
            p.addView(empty);
        }
        addOption(p, 0, "System picker (fallback)", () -> openSystemFolderPicker(pendingFolderReq));
        addOption(p, 0, "Cancel", this::buildMainMenu);

        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(false);
        sv.addView(p);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams((int)(getResources().getDisplayMetrics().widthPixels*0.38), (int)(getResources().getDisplayMetrics().heightPixels*0.86));
        lp.gravity = android.view.Gravity.CENTER;
        root.addView(sv, lp);
        sv.setClickable(true);
        setContentView(root);
        focusFirst(p);
    }

    private void selectFolder(java.io.File dir) {
        Uri uri = Uri.fromFile(dir);
        int req = this.pendingFolderReq;
        handleFolderSelection(req, uri);
    }

    private void openSystemFolderPicker(int req) {
        Intent i = new Intent("android.intent.action.OPEN_DOCUMENT_TREE");
        i.addFlags(65);
        try { startActivityForResult(i, req); return; } catch (Exception e) {}
        try {
            Intent alt = new Intent("android.intent.action.OPEN_DOCUMENT");
            alt.addCategory("android.intent.category.OPENABLE");
            alt.setType("*/*");
            alt.addFlags(65);
            startActivityForResult(alt, req);
        } catch (Exception e2) {
            try {
                Intent alt2 = new Intent("android.intent.action.GET_CONTENT");
                alt2.addCategory("android.intent.category.OPENABLE");
                alt2.setType("*/*");
                alt2.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(Intent.createChooser(alt2, "Pick a file in folder"), req);
            } catch (Exception e3) {
                Toast.makeText(this, "Cannot open folder picker", 0).show();
            }
        }
    }

    private void handleFolderSelection(int requestCode, Uri uri) {
        if (uri == null) { Toast.makeText(this, "Could not use selected folder", Toast.LENGTH_SHORT).show(); return; }
        uri = normalizeFolderUri(uri);
        if (uri == null) { Toast.makeText(this, "Could not use selected folder", Toast.LENGTH_SHORT).show(); return; }
        try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (Exception ignored) {}
        try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION); } catch (Exception ignored) {}
        SharedPreferences.Editor e = prefs().edit();
        if (requestCode == 1004) {
            e.putString("wallpaper_type", "slideshow").putString("slideshow_folder_uri", uri.toString()).apply();
            Toast.makeText(this, "Slideshow folder set", Toast.LENGTH_SHORT).show();
        } else if (requestCode == 1007 && pendingCat != null) {
            e.putString("cat_wallpaper_type_" + pendingCat, "slideshow").putString("cat_wallpaper_folder_" + pendingCat, uri.toString()).apply();
            Toast.makeText(this, pendingCat + " slideshow set", Toast.LENGTH_SHORT).show();
        }
        buildMainMenu();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;
        Uri uri = data.getData();
        if (uri == null) return;
        boolean isFolderReq = requestCode == 1004 || requestCode == 1007;
        if (isFolderReq) {
            handleFolderSelection(requestCode, uri);
            return;
        }
        try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (Exception ignored) {}
        SharedPreferences.Editor e = prefs().edit();
        if (requestCode == 1001) {
            e.putString("wallpaper_type", "image").putString("wallpaper_uri", uri.toString()).apply();
            Toast.makeText(this, "Wallpaper image set", Toast.LENGTH_SHORT).show();
        } else if (requestCode == 1003) {
            e.putString("wallpaper_type", "video").putString("wallpaper_uri", uri.toString()).apply();
            Toast.makeText(this, "Video wallpaper set (muted)", Toast.LENGTH_SHORT).show();
        } else if (requestCode == 1005 && pendingCat != null) {
            e.putString("cat_wallpaper_type_" + pendingCat, "image").putString("cat_wallpaper_uri_" + pendingCat, uri.toString()).apply();
            Toast.makeText(this, pendingCat + " image set", Toast.LENGTH_SHORT).show();
        } else if (requestCode == 1006 && pendingCat != null) {
            e.putString("cat_wallpaper_type_" + pendingCat, "video").putString("cat_wallpaper_uri_" + pendingCat, uri.toString()).apply();
            Toast.makeText(this, pendingCat + " video set (muted)", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri normalizeFolderUri(Uri uri) {
        if (uri == null) return null;
        String s = uri.toString();
        if (s.startsWith("content://") && s.contains("/tree/")) return uri;
        // file:// or content uri pointing to file - use parent directory
        String path = null;
        if ("file".equals(uri.getScheme())) path = uri.getPath();
        if (path == null) path = uri.getPath();
        if (path != null) {
            // sometimes content://com.estrongs.../tree/storage/emulated/0/... or has /storage/
            int idx = path.indexOf("/storage/");
            if (idx >= 0) path = path.substring(idx);
            java.io.File f = new java.io.File(path);
            if (f.isFile()) f = f.getParentFile();
            if (f != null && f.isDirectory()) return Uri.fromFile(f);
            // if it doesn't exist as dir, still try parent
            if (f != null && f.getParentFile() != null) return Uri.fromFile(f.getParentFile());
        }
        return uri;
    }

    private SharedPreferences prefs() {
        return getSharedPreferences("launcher", 0);
    }
}
