package com.local.homelauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.media.MediaPlayer;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import com.local.homelauncher.MainActivity;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.json.JSONArray;

/* loaded from: classes3.dex */
public class MainActivity extends Activity {
    private static final int REQ_ICON = 2001;
    private static final int TAG_CATEGORY = 2131361793;
    private TextView clock;
    private LinearLayout rows;
    private VideoView videoWallpaper;
    private ImageView wallpaper;
    private final Handler handler = new Handler();
    private String pendingIconKey = null;
    private String movingKey = null;
    private final List<RowArrows> rowArrows = new ArrayList<>();
    private boolean focusWatcherAdded = false;

    private static class RowArrows {
        final HorizontalScrollView hs;
        final View left;
        final View right;

        RowArrows(HorizontalScrollView hs, View left, View right) {
            this.hs = hs;
            this.left = left;
            this.right = right;
        }
    }
    private final Runnable slideshowTick = new Runnable() { // from class: com.local.homelauncher.MainActivity.1

        @Override // java.lang.Runnable
        public void run() {
            MainActivity.this.nextSlideshowItem();
        }
    };
    private List<Uri> slideshowMedia = new ArrayList();
    private int slideshowIndex = 0;
    private boolean slideshowRandom = false;
    private String currentCategoryWallpaper = null;
    private boolean slideshowVideoMode = false;
    private final java.util.concurrent.ExecutorService wallpaperExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();
    private volatile int wallpaperReq = 0;
    private String lastWallpaperKey = null;
    private boolean rowsLoaded = false;
    private final Runnable clockTick = new Runnable() { // from class: com.local.homelauncher.MainActivity.2

        @Override // java.lang.Runnable
        public void run() {
            MainActivity.this.clock.setText(new SimpleDateFormat("h:mm a EEEE, MMM d", Locale.getDefault()).format(new Date()));
            MainActivity.this.handler.postDelayed(this, 30000L);
        }
    };
    private final BroadcastReceiver pkgReceiver = new BroadcastReceiver() { // from class: com.local.homelauncher.MainActivity.3

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            MainActivity.this.m76lambda$showMoveMenu$23$comlocalhomelauncherMainActivity();
        }
    };

    /* renamed from: com.local.homelauncher.MainActivity$1 */
    /* loaded from: classes3.dex */
    class AnonymousClass1 implements Runnable {

        @Override // java.lang.Runnable
        public void run() {
            MainActivity.this.nextSlideshowItem();
        }
    }

    /* renamed from: com.local.homelauncher.MainActivity$2 */
    /* loaded from: classes3.dex */
    class AnonymousClass2 implements Runnable {

        @Override // java.lang.Runnable
        public void run() {
            MainActivity.this.clock.setText(new SimpleDateFormat("h:mm a EEEE, MMM d", Locale.getDefault()).format(new Date()));
            MainActivity.this.handler.postDelayed(this, 30000L);
        }
    }

    /* renamed from: com.local.homelauncher.MainActivity$3 */
    /* loaded from: classes3.dex */
    class AnonymousClass3 extends BroadcastReceiver {

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            MainActivity.this.m76lambda$showMoveMenu$23$comlocalhomelauncherMainActivity();
        }
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.rows = (LinearLayout) findViewById(R.id.rows);
        this.wallpaper = (ImageView) findViewById(R.id.wallpaper);
        this.videoWallpaper = (VideoView) findViewById(R.id.videoWallpaper);
        this.videoWallpaper.setOnPreparedListener(new MediaPlayer.OnPreparedListener() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda20
            @Override // android.media.MediaPlayer.OnPreparedListener
            public final void onPrepared(MediaPlayer mediaPlayer) {
                MainActivity.lambda$onCreate$0(mediaPlayer);
            }
        });
        this.clock = (TextView) findViewById(R.id.clock);
        View wifiBtn = findViewById(R.id.wifiBtn);
        wifiBtn.setOnClickListener(new View.OnClickListener() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda21
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m62lambda$onCreate$1$comlocalhomelauncherMainActivity(view);
            }
        });
        View settingsBtn = findViewById(R.id.settingsBtn);
        settingsBtn.setOnClickListener(new View.OnClickListener() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda23
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m63lambda$onCreate$2$comlocalhomelauncherMainActivity(view);
            }
        });
        IntentFilter f = new IntentFilter();
        f.addAction("android.intent.action.PACKAGE_ADDED");
        f.addAction("android.intent.action.PACKAGE_REMOVED");
        f.addAction("android.intent.action.PACKAGE_CHANGED");
        f.addDataScheme("package");
        registerReceiver(this.pkgReceiver, f);
    }

    public static /* synthetic */ void lambda$onCreate$0(MediaPlayer mp) {
        mp.setVolume(0.0f, 0.0f);
        mp.setLooping(true);
    }

    /* renamed from: lambda$onCreate$1$com-local-homelauncher-MainActivity */
    public /* synthetic */ void m62lambda$onCreate$1$comlocalhomelauncherMainActivity(View v) {
        try {
            startActivity(new Intent("android.settings.WIFI_SETTINGS"));
        } catch (Exception e) {
            try {
                startActivity(new Intent("android.settings.WIRELESS_SETTINGS"));
            } catch (Exception e2) {
                Toast.makeText(this, "Cannot open Wi-Fi settings", 0).show();
            }
        }
    }

    /* renamed from: lambda$onCreate$2$com-local-homelauncher-MainActivity */
    public /* synthetic */ void m63lambda$onCreate$2$comlocalhomelauncherMainActivity(View v) {
        startActivity(new Intent(this, (Class<?>) SettingsActivity.class));
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        SharedPreferences sync = getSharedPreferences("launcher", 0);
        boolean fromScreensaver = sync.getBoolean("screensaver_was_active", false)
                || sync.getBoolean("screensaver_active", false)
                || sync.getBoolean("screensaver_requested", false);
        long pausedAt = sync.getLong("slideshow_paused_at", 0L);
        SharedPreferences.Editor syncEdit = sync.edit();
        if (fromScreensaver) syncEdit.remove("screensaver_was_active");
        if (pausedAt > 0L) {
            if (!fromScreensaver) {
                long started = sync.getLong("slideshow_item_started_at", 0L);
                if (started > 0L) syncEdit.putLong("slideshow_item_started_at", started + (System.currentTimeMillis() - pausedAt));
            }
            syncEdit.remove("slideshow_paused_at");
        }
        syncEdit.apply();
        this.handler.postDelayed(() -> {
            if (consumeHomePress()) {
                forceStopBackgroundApps();
            }
        }, 150);
        boolean prevent = getSharedPreferences("launcher", 0).getBoolean("prevent_screensaver", false);
        if (prevent) {
            getWindow().addFlags(128);
        } else {
            getWindow().clearFlags(128);
        }
        String wallpaperKey = wallpaperKey();
        if (!wallpaperKey.equals(this.lastWallpaperKey)) {
            loadWallpaper();
        } else {
            resumeWallpaper();
        }
        if (!this.rowsLoaded) {
            m76lambda$showMoveMenu$23$comlocalhomelauncherMainActivity();
            this.rowsLoaded = true;
        }
        this.handler.removeCallbacks(this.clockTick);
        this.clockTick.run();
        try {
            if (this.videoWallpaper.getVisibility() == 0) {
                this.videoWallpaper.start();
            }
        } catch (Exception e) {
        }
    }

    private boolean consumeHomePress() {
        try {
            File f = new File("/data/local/tmp/home_press_flag");
            if (!f.exists()) return false;
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String line = reader.readLine();
            reader.close();
            if (line == null) return false;
            long pressed = Long.parseLong(line.trim());
            long now = System.currentTimeMillis() / 1000;
            if (now - pressed >= 0 && now - pressed <= 3) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    private void forceStopBackgroundApps() {
        new Thread(() -> {
            String script = "rm -f /data/local/tmp/home_press_flag; "
                    + "for p in $(pm list packages -3 | cut -d: -f2); do "
                    + "case \"$p\" in com.local.homelauncher|com.topjohnwu.magisk) continue;; esac; "
                    + "if [ -n \"$(pidof $p)\" ]; then am force-stop \"$p\" && echo \"$p\"; fi; "
                    + "done";
            int count = 0;
            try {
                Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", script});
                BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) count++;
                }
                reader.close();
                proc.waitFor();
            } catch (Exception e) {
                count = -1;
            }
            final int stopped = count;
            runOnUiThread(() -> {
                if (stopped >= 0) {
                    Toast.makeText(MainActivity.this, "Force stopped " + stopped + " apps", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Force stop failed", Toast.LENGTH_SHORT).show();
                }
            });
        }, "home-force-stop").start();
    }

    @Override // android.app.Activity
    protected void onPause() {
        super.onPause();
        SharedPreferences sync = getSharedPreferences("launcher", 0);
        SharedPreferences.Editor syncEdit = sync.edit();
        if (!sync.getBoolean("screensaver_active", false) && !sync.getBoolean("screensaver_requested", false)) {
            syncEdit.remove("screensaver_was_active");
        }
        if (!this.slideshowMedia.isEmpty()) {
            syncEdit.putLong("slideshow_paused_at", System.currentTimeMillis());
        }
        syncEdit.apply();
        stopSlideshow();
        try {
            if (this.videoWallpaper.isPlaying()) {
                this.videoWallpaper.pause();
            }
        } catch (Exception e) {
        }
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(this.pkgReceiver);
        } catch (Exception e) {
        }
        this.handler.removeCallbacks(this.clockTick);
        stopSlideshow();
        try { this.wallpaperExecutor.shutdownNow(); } catch (Exception e) {}
        try {
            this.videoWallpaper.stopPlayback();
        } catch (Exception e2) {
        }
    }

    private String wallpaperKey() {
        SharedPreferences p = getSharedPreferences("launcher", 0);
        String category = this.currentCategoryWallpaper;
        String type;
        String uri;
        String folder;
        boolean random;
        if (category != null) {
            type = p.getString("cat_wallpaper_type_" + category, null);
            uri = p.getString("cat_wallpaper_uri_" + category, null);
            folder = p.getString("cat_wallpaper_folder_" + category, null);
            random = p.getBoolean("cat_wallpaper_random_" + category, p.getBoolean("slideshow_random", true));
        } else {
            type = null;
            uri = null;
            folder = null;
            random = p.getBoolean("slideshow_random", true);
        }
        if (type == null) {
            type = p.getString("wallpaper_type", "image");
            uri = p.getString("wallpaper_uri", null);
            folder = p.getString("slideshow_folder_uri", null);
        }
        return (category == null ? "default" : category) + "|" + type + "|"
                + (uri == null ? "" : uri) + "|" + (folder == null ? "" : folder) + "|" + random;
    }

    private void resumeWallpaper() {
        if (!this.slideshowMedia.isEmpty()) {
            SharedPreferences p = getSharedPreferences("launcher", 0);
            String current = p.getString("current_wallpaper_uri", null);
            int idx = -1;
            if (current != null) {
                for (int i = 0; i < this.slideshowMedia.size(); i++) {
                    if (current.equals(this.slideshowMedia.get(i).toString())) { idx = i; break; }
                }
            }
            if (idx < 0) {
                showSlideshowItem(this.slideshowMedia.get(this.slideshowIndex));
            } else if (idx != this.slideshowIndex) {
                this.slideshowIndex = idx;
                showSlideshowItem(this.slideshowMedia.get(idx), true);
            } else {
                this.handler.removeCallbacks(this.slideshowTick);
                this.handler.postDelayed(this.slideshowTick, slideshowRemainingMs());
            }
        }
        if (this.videoWallpaper.getVisibility() == 0) {
            try { this.videoWallpaper.start(); } catch (Exception e) {}
        }
    }

    private long slideshowRemainingMs() {
        SharedPreferences p = getSharedPreferences("launcher", 0);
        long interval = Math.max(5000L, (long) p.getInt("slideshow_interval_ms", 15000));
        long started = p.getLong("slideshow_item_started_at", 0L);
        if (started <= 0L) return interval;
        long remaining = interval - (System.currentTimeMillis() - started);
        if (remaining < 1000L) remaining = 1000L;
        if (remaining > interval) remaining = interval;
        return remaining;
    }

    private void loadWallpaper() {
        applyWallpaperForCategory(this.currentCategoryWallpaper);
        getSharedPreferences("launcher", 0).edit().putString("last_wallpaper_category",
                this.currentCategoryWallpaper == null ? "" : this.currentCategoryWallpaper).apply();
    }

    private void applyWallpaperForCategory(String category) {
        SharedPreferences p = getSharedPreferences("launcher", 0);
        String type = null;
        String uri = null;
        String folder = null;
        boolean random = p.getBoolean("slideshow_random", true);
        if (category != null) {
            type = p.getString("cat_wallpaper_type_" + category, null);
            uri = p.getString("cat_wallpaper_uri_" + category, null);
            folder = p.getString("cat_wallpaper_folder_" + category, null);
            random = p.getBoolean("cat_wallpaper_random_" + category, random);
        }
        if (type == null) {
            type = p.getString("wallpaper_type", "image");
            uri = p.getString("wallpaper_uri", null);
            folder = p.getString("slideshow_folder_uri", null);
        }
        applyWallpaperConfig(type, uri, folder, random);
    }

    private void applyWallpaperConfig(String type, String uri, String folder, boolean random) {
        stopSlideshow();
        this.slideshowRandom = random;
        SharedPreferences p = getSharedPreferences("launcher", 0);
        if ("slideshow".equals(type) && folder != null) {
            startSlideshow(Uri.parse(folder));
        } else {
            p.edit().remove("slideshow_order").remove("current_wallpaper_uri").apply();
            if ("video".equals(type) && uri != null) {
                showVideoWallpaper(Uri.parse(uri), true);
            } else {
                showImageWallpaper(uri != null ? Uri.parse(uri) : null);
            }
        }
        this.lastWallpaperKey = wallpaperKey();
    }

    private void showImageWallpaper(Uri uri) {
        this.slideshowVideoMode = false;
        try { if (this.videoWallpaper.isPlaying()) this.videoWallpaper.stopPlayback(); } catch (Exception e) {}
        try { this.videoWallpaper.setOnCompletionListener(null); } catch (Exception e) {}
        FrameLayout.LayoutParams vlp = (FrameLayout.LayoutParams) this.videoWallpaper.getLayoutParams();
        vlp.width = -1; vlp.height = -1; vlp.gravity = 0;
        this.videoWallpaper.setLayoutParams(vlp);
        this.videoWallpaper.setVisibility(8);
        this.wallpaper.setVisibility(0);
        if (uri == null) { this.wallpaper.setImageDrawable(null); return; }
        final int req = ++wallpaperReq;
        final Uri u = uri;
        final int tw = Math.max(1, getResources().getDisplayMetrics().widthPixels);
        final int th = Math.max(1, getResources().getDisplayMetrics().heightPixels);
        wallpaperExecutor.execute(() -> {
            android.graphics.Bitmap bmp = decodeWallpaperBitmap(u, tw, th);
            if (req != wallpaperReq) { if (bmp != null) bmp.recycle(); return; }
            handler.post(() -> { if (req == wallpaperReq) wallpaper.setImageBitmap(bmp); else if (bmp != null) bmp.recycle(); });
        });
    }

    private android.graphics.Bitmap decodeWallpaperBitmap(Uri uri, int targetW, int targetH) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            InputStream is1 = getContentResolver().openInputStream(uri);
            try { BitmapFactory.decodeStream(is1, null, bounds); } finally { if (is1 != null) is1.close(); }
            int sw = bounds.outWidth, sh = bounds.outHeight;
            if (sw <= 0 || sh <= 0) return null;
            int sample = 1;
            while (sw / (sample * 2) >= targetW && sh / (sample * 2) >= targetH) sample *= 2;
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = Math.max(1, sample);
            opts.inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888;
            InputStream is2 = getContentResolver().openInputStream(uri);
            try { return BitmapFactory.decodeStream(is2, null, opts); } finally { if (is2 != null) is2.close(); }
        } catch (Exception e) { return null; }
    }

    private void showVideoWallpaper(Uri uri, boolean looping) {
        this.wallpaper.setVisibility(8);
        this.videoWallpaper.setVisibility(0);
        try {
            this.videoWallpaper.stopPlayback();
            this.videoWallpaper.setVideoURI(uri);
            this.videoWallpaper.setOnPreparedListener(new MediaPlayer.OnPreparedListener() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda18
                @Override // android.media.MediaPlayer.OnPreparedListener
                public final void onPrepared(MediaPlayer mediaPlayer) {
                    MainActivity.this.m81lambda$showVideoWallpaper$3$comlocalhomelauncherMainActivity(mediaPlayer, looping);
                }
            });
            if (!looping) {
                this.videoWallpaper.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override public void onCompletion(MediaPlayer mp) {
                        if (MainActivity.this.slideshowVideoMode) MainActivity.this.nextSlideshowItem();
                    }
                });
            } else {
                this.videoWallpaper.setOnCompletionListener(null);
            }
            this.videoWallpaper.start();
        } catch (Exception e) {
            this.videoWallpaper.setVisibility(8);
            this.wallpaper.setVisibility(0);
            if (this.slideshowVideoMode) nextSlideshowItem();
        }
    }

    /* renamed from: lambda$showVideoWallpaper$3$com-local-homelauncher-MainActivity */
    public /* synthetic */ void m81lambda$showVideoWallpaper$3$comlocalhomelauncherMainActivity(MediaPlayer mp, boolean looping) {
        mp.setVolume(0.0f, 0.0f);
        mp.setLooping(looping);
        try { mp.setVideoScalingMode(2); } catch (Exception e) {}
        int vw = mp.getVideoWidth(); int vh = mp.getVideoHeight();
        int sw = this.videoWallpaper.getWidth(); int sh = this.videoWallpaper.getHeight();
        if (vw > 0 && vh > 0 && sw > 0 && sh > 0) {
            float scale = Math.max((float) sw / vw, (float) sh / vh);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) this.videoWallpaper.getLayoutParams();
            lp.width = Math.round(vw * scale); lp.height = Math.round(vh * scale); lp.gravity = 17;
            this.videoWallpaper.setLayoutParams(lp);
        }
    }

    private boolean hasExtension(Uri uri, String[] exts) {
        String s = uri.toString();
        try { s = java.net.URLDecoder.decode(s, "UTF-8"); } catch (Exception ignored) {}
        s = s.toLowerCase(Locale.ROOT);
        for (String ext : exts) { if (s.endsWith("." + ext)) return true; }
        return false;
    }

    private boolean isVideoUri(Uri uri) {
        return hasExtension(uri, new String[]{"mp4","m4v","mov","mkv","webm","3gp","3g2","avi","ts","mts","m2ts","flv","wmv","mpg","mpeg","mpg4","ogv","divx","f4v","rmvb","asf","vob","m2v","h264","h265","hevc"});
    }

    private boolean isImageUri(Uri uri) {
        return hasExtension(uri, new String[]{"jpg","jpeg","jpe","jfif","png","webp","gif","bmp","heic","heif","avif","tif","tiff","ico","apng"});
    }

    private void startSlideshow(Uri folderUri) {
        this.slideshowVideoMode = true;
        this.slideshowMedia = loadSlideshowMedia(folderUri);
        this.slideshowIndex = 0;
        if (this.slideshowRandom && this.slideshowMedia.size() > 1) {
            Collections.shuffle(this.slideshowMedia);
        }
        StringBuilder order = new StringBuilder();
        for (Uri u : this.slideshowMedia) {
            if (order.length() > 0) order.append('\n');
            order.append(u.toString());
        }
        getSharedPreferences("launcher", 0).edit().putString("slideshow_order", order.toString()).apply();
        if (this.slideshowMedia.isEmpty()) {
            this.slideshowVideoMode = false;
            showImageWallpaper(null);
            return;
        }
        showSlideshowItem(this.slideshowMedia.get(0));
    }

    private void showSlideshowItem(Uri uri) {
        showSlideshowItem(uri, false);
    }

    private void showSlideshowItem(Uri uri, boolean preserveTiming) {
        this.handler.removeCallbacks(this.slideshowTick);
        SharedPreferences.Editor e = getSharedPreferences("launcher", 0).edit();
        e.putString("current_wallpaper_uri", uri != null ? uri.toString() : null);
        if (!preserveTiming) e.putLong("slideshow_item_started_at", System.currentTimeMillis());
        e.apply();
        String mime = null;
        try { mime = getContentResolver().getType(uri); } catch (Exception e2) {}
        boolean video = (mime != null && mime.startsWith("video/")) || isVideoUri(uri);
        boolean image = (mime != null && mime.startsWith("image/")) || isImageUri(uri);
        long delay = preserveTiming ? slideshowRemainingMs()
                : Math.max(5000L, (long) getSharedPreferences("launcher", 0).getInt("slideshow_interval_ms", 15000));
        if (video && !image) {
            this.slideshowVideoMode = true;
            showVideoWallpaper(uri, true);
            this.handler.postDelayed(this.slideshowTick, delay);
        } else {
            showImageWallpaper(uri);
            this.slideshowVideoMode = true;
            this.handler.postDelayed(this.slideshowTick, delay);
        }
    }

    private void stopSlideshow() {
        this.slideshowVideoMode = false;
        this.handler.removeCallbacks(this.slideshowTick);
        try { this.videoWallpaper.setOnCompletionListener(null); } catch (Exception e) {}
    }

    public void nextSlideshowItem() {
        if (this.slideshowMedia.isEmpty()) return;
        this.slideshowIndex = (this.slideshowIndex + 1) % this.slideshowMedia.size();
        showSlideshowItem(this.slideshowMedia.get(this.slideshowIndex));
    }

    private List<Uri> loadSlideshowMedia(Uri folderUri) {
        List<Uri> out = new ArrayList<>();
        if (folderUri == null) return out;
        String scheme = folderUri.getScheme();
        if (scheme == null || "file".equals(scheme)) {
            File dir = new File(folderUri.getPath());
            if (dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) for (File f : files) {
                    if (f.isFile()) {
                        Uri u = Uri.fromFile(f);
                        if (isImageUri(u) || isVideoUri(u)) out.add(u);
                    }
                }
            }
        } else {
            try {
                Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(folderUri, DocumentsContract.getTreeDocumentId(folderUri));
                Cursor c = getContentResolver().query(childrenUri, new String[]{"document_id", "mime_type"}, null, null, null);
                if (c != null) {
                    while (c.moveToNext()) {
                        try {
                            String id = c.getString(0); String mime = c.getString(1);
                            if (mime != null && (mime.startsWith("image/") || mime.startsWith("video/"))) {
                                out.add(DocumentsContract.buildDocumentUriUsingTree(folderUri, id));
                            } else {
                                Uri u = DocumentsContract.buildDocumentUriUsingTree(folderUri, id);
                                if (isImageUri(u) || isVideoUri(u)) out.add(u);
                            }
                        } finally {}
                    }
                    c.close();
                }
            } catch (Exception e) {}
        }
        Collections.sort(out, new Comparator() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda4
            @Override // java.util.Comparator
            public final int compare(Object obj, Object obj2) {
                return ((Uri) obj).toString().compareTo(((Uri) obj2).toString());
            }
        });
        return out;
    }

    /* renamed from: loadRows */
    public void m76lambda$showMoveMenu$23$comlocalhomelauncherMainActivity() {
        clearRowArrows();
        this.rows.removeAllViews();
        if (!focusWatcherAdded) {
            focusWatcherAdded = true;
            getWindow().getDecorView().getViewTreeObserver().addOnGlobalFocusChangeListener((oldFocus, newFocus) -> refreshRowArrows());
        }
        List<AppItem> favs = getFavoriteApps();
        if (!favs.isEmpty()) {
            addAppsRow("Favorites", favs);
        }
        addAppsRow("Apps", getAllApps());
        if (this.movingKey == null) {
            this.rows.post(new Runnable() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda29
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.focusFirstApp();
                }
            });
        }
    }

    public void focusFirstApp() {
        View first = findFirstFocusableTile(this.rows);
        if (first != null) {
            first.requestFocus();
        }
    }

    private View findFirstFocusableTile(View v) {
        if (v.isFocusable() && v.isClickable() && (v.getTag() instanceof String)) {
            return v;
        }
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                View r = findFirstFocusableTile(g.getChildAt(i));
                if (r != null) {
                    return r;
                }
            }
            return null;
        }
        return null;
    }

    private List<AppItem> getAllAppsUnsorted() {
        List<AppItem> out = new ArrayList<>();
        LinkedHashMap<String, AppItem> seen = new LinkedHashMap<>();
        int i = 2;
        boolean[] zArr = {true, false};
        int i2 = 0;
        int i3 = 0;
        while (i3 < i) {
            boolean leanback = zArr[i3];
            Intent i4 = new Intent("android.intent.action.MAIN", (Uri) null);
            i4.addCategory(leanback ? "android.intent.category.LEANBACK_LAUNCHER" : "android.intent.category.LAUNCHER");
            List<ResolveInfo> infos = getPackageManager().queryIntentActivities(i4, i2);
            for (ResolveInfo ri : infos) {
                if (ri.activityInfo != null) {
                    String pkg = ri.activityInfo.packageName;
                    if (!pkg.equals(getPackageName())) {
                        String key = pkg + "/" + ri.activityInfo.name;
                        if (!seen.containsKey(key) && (!hiddenApps().contains(key) || showHidden())) {
                            AppItem a = new AppItem();
                            a.label = String.valueOf(ri.loadLabel(getPackageManager()));
                            a.pkg = pkg;
                            a.cls = ri.activityInfo.name;
                            Drawable custom = loadCustomIcon(key);
                            a.icon = custom != null ? custom : ri.activityInfo.loadBanner(getPackageManager());
                            if (a.icon == null) {
                                a.icon = ri.loadIcon(getPackageManager());
                            }
                            a.isLeanback = leanback;
                            seen.put(key, a);
                        }
                    }
                }
            }
            i3++;
            i = 2;
            i2 = 0;
        }
        out.addAll(seen.values());
        return out;
    }

    private List<AppItem> getAllApps() {
        List<AppItem> out = getAllAppsUnsorted();
        final List<String> order = getAppOrder();
        Collections.sort(out, new Comparator() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda28
            @Override // java.util.Comparator
            public final int compare(Object obj, Object obj2) {
                return MainActivity.lambda$getAllApps$5(order, (MainActivity.AppItem) obj, (MainActivity.AppItem) obj2);
            }
        });
        return out;
    }

    public static /* synthetic */ int lambda$getAllApps$5(List order, AppItem a, AppItem b) {
        String ka = a.pkg + "/" + a.cls;
        String kb = b.pkg + "/" + b.cls;
        int ia = order.indexOf(ka);
        int ib = order.indexOf(kb);
        if (ia == -1 && ib == -1) {
            return a.label.toLowerCase(Locale.getDefault()).compareTo(b.label.toLowerCase(Locale.getDefault()));
        }
        if (ia == -1 || ib == -1) {
            return 1;
        }
        return Integer.compare(ia, ib);
    }

    private void addAppsRow(String title, List<AppItem> apps) {
        if (apps.isEmpty()) {
            return;
        }
        if (!"Apps".equals(title)) {
            this.rows.addView(makeTitle(title));
        }
        LinearLayout row = makeRow();
        for (final AppItem a : apps) {
            View tile = makeTile(a.label, a.icon, new View.OnClickListener() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda26
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MainActivity.this.m57lambda$addAppsRow$6$comlocalhomelauncherMainActivity(a, view);
                }
            }, new View.OnLongClickListener() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda27
                @Override // android.view.View.OnLongClickListener
                public final boolean onLongClick(View view) {
                    return MainActivity.this.m58lambda$addAppsRow$7$comlocalhomelauncherMainActivity(a, view);
                }
            });
            tile.setId(View.generateViewId());
            tile.setTag(a.pkg + "/" + a.cls);
            tile.setTag(R.integer.abc_config_activityShortDur, title);
            if (hiddenApps().contains(a.pkg + "/" + a.cls)) {
                tile.setAlpha(0.45f);
            }
            row.addView(tile);
        }
        if (row.getChildCount() > 1) {
            View firstTile = row.getChildAt(0);
            View lastTile = row.getChildAt(row.getChildCount() - 1);
            firstTile.setNextFocusLeftId(lastTile.getId());
            lastTile.setNextFocusRightId(firstTile.getId());
        }
        this.rows.addView(wrapHorizontal(row));
    }

    /* renamed from: lambda$addAppsRow$6$com-local-homelauncher-MainActivity */
    public /* synthetic */ void m57lambda$addAppsRow$6$comlocalhomelauncherMainActivity(AppItem a, View v) {
        launchApp(a.pkg, a.cls);
    }

    /* renamed from: lambda$addAppsRow$7$com-local-homelauncher-MainActivity */
    public /* synthetic */ boolean m58lambda$addAppsRow$7$comlocalhomelauncherMainActivity(AppItem a, View v) {
        showAppMenu(a);
        return true;
    }

    private void addInputsRow() {
        String label;
        LinearLayout row = makeRow();
        boolean any = false;
        try {
            TvInputManager tim = (TvInputManager) getSystemService("tv_input");
            if (tim != null) {
                List<TvInputInfo> inputs = tim.getTvInputList();
                for (TvInputInfo info : inputs) {
                    if (info.isPassthroughInput()) {
                        if (info.isHidden(this)) {
                        }
                        try {
                            label = String.valueOf(info.loadLabel(this));
                        } catch (Exception e) {
                            label = info.getId();
                        }
                        if (label == null || label.trim().isEmpty() || "null".equals(label)) {
                            label = prettyInputName(info);
                        }
                        final String fLabel = label;
                        Drawable icon = null;
                        try {
                            icon = info.loadIcon(this);
                        } catch (Exception e2) {
                        }
                        final String id = info.getId();
                        row.addView(makeTile(fLabel, icon, new View.OnClickListener() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda0
                            @Override // android.view.View.OnClickListener
                            public final void onClick(View view) {
                                MainActivity.this.m59lambda$addInputsRow$8$comlocalhomelauncherMainActivity(id, view);
                            }
                        }, new View.OnLongClickListener() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda11
                            @Override // android.view.View.OnLongClickListener
                            public final boolean onLongClick(View view) {
                                return MainActivity.this.m60lambda$addInputsRow$9$comlocalhomelauncherMainActivity(id, fLabel, view);
                            }
                        }));
                        any = true;
                    }
                }
            }
        } catch (Exception e3) {
            Toast.makeText(this, "Input error: " + e3.getMessage(), 0).show();
        }
        if (any) {
            this.rows.addView(makeTitle("Inputs"));
            this.rows.addView(wrapHorizontal(row));
        }
    }

    /* renamed from: lambda$addInputsRow$8$com-local-homelauncher-MainActivity */
    public /* synthetic */ void m59lambda$addInputsRow$8$comlocalhomelauncherMainActivity(String id, View v) {
        launchInput(id);
    }

    /* renamed from: lambda$addInputsRow$9$com-local-homelauncher-MainActivity */
    public /* synthetic */ boolean m60lambda$addInputsRow$9$comlocalhomelauncherMainActivity(String id, String fLabel, View v) {
        showInputMenu(id, fLabel);
        return true;
    }

    private String prettyInputName(TvInputInfo info) {
        try {
            int type = info.getType();
            switch (type) {
                case 0:
                    return "Tuner";
                case 1001:
                    return "Composite";
                case 1002:
                    return "S-Video";
                case 1003:
                    return "SCART";
                case 1004:
                    return "Component";
                case 1005:
                    return "VGA";
                case 1006:
                    return "DVI";
                case 1007:
                    return "HDMI";
                case 1008:
                    return "DisplayPort";
                default:
                    return "Input";
            }
        } catch (Exception e) {
            return "Input";
        }
    }

    private void launchApp(String pkg, String cls) {
        try {
            Intent i = new Intent("android.intent.action.MAIN");
            i.addCategory("android.intent.category.LAUNCHER");
            i.setClassName(pkg, cls);
            i.addFlags(268435456);
            startActivity(i);
        } catch (Exception e) {
            try {
                Intent i2 = getPackageManager().getLeanbackLaunchIntentForPackage(pkg);
                if (i2 == null) {
                    i2 = getPackageManager().getLaunchIntentForPackage(pkg);
                }
                if (i2 == null) {
                    Toast.makeText(this, "Cannot launch", 0).show();
                } else {
                    startActivity(i2);
                }
            } catch (Exception ex) {
                Toast.makeText(this, "Launch failed: " + ex.getMessage(), 0).show();
            }
        }
    }

    private void launchInput(String inputId) {
        TvInputInfo info;
        Intent i;
        try {
            Intent i2 = new Intent("android.intent.action.VIEW");
            i2.setData(Uri.parse("content://android.media.tv/passthrough/" + inputId));
            i2.putExtra("android.media.tv.extra.INPUT_ID", inputId);
            i2.addFlags(268435456);
            startActivity(i2);
        } catch (Exception e) {
        }
        try {
            Intent i3 = new Intent("android.intent.action.VIEW", Uri.parse("tvinput://" + inputId));
            i3.putExtra("android.media.tv.extra.INPUT_ID", inputId);
            startActivity(i3);
        } catch (Exception e2) {
            try {
                TvInputManager tim = (TvInputManager) getSystemService("tv_input");
                if (tim != null && (info = tim.getTvInputInfo(inputId)) != null && (i = info.createSetupIntent()) != null) {
                    startActivity(i);
                    return;
                }
            } catch (Exception e3) {
            }
            Toast.makeText(this, "Could not switch to input: " + inputId, 0).show();
        }
    }

    private TextView makeTitle(String t) {
        TextView tv = new TextView(this);
        tv.setText(t.toUpperCase(Locale.getDefault()));
        tv.setTextColor(-1711276033);
        tv.setTextSize(13.0f);
        tv.setLetterSpacing(0.15f);
        tv.setPadding(8, 28, 0, 12);
        tv.setTypeface(Typeface.create("sans-serif-medium", 0));
        return tv;
    }

    private LinearLayout makeRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(0);
        row.setGravity(16);
        row.setPadding(16, 0, 16, 0);
        return row;
    }

    private View wrapHorizontal(LinearLayout row) {
        final View leftArrow = makeScrollArrow(true);
        final View rightArrow = makeScrollArrow(false);
        HorizontalScrollView hs = new HorizontalScrollView(this) {
            @Override
            protected void onScrollChanged(int l, int t, int oldl, int oldt) {
                super.onScrollChanged(l, t, oldl, oldt);
                updateRowArrows(this, leftArrow, rightArrow);
            }
        };
        hs.setHorizontalScrollBarEnabled(false);
        hs.addView(row);
        ViewGroup overlayHost = (ViewGroup) getWindow().getDecorView();
        overlayHost.getOverlay().add(leftArrow);
        overlayHost.getOverlay().add(rightArrow);
        this.rowArrows.add(new RowArrows(hs, leftArrow, rightArrow));
        hs.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> updateRowArrows(hs, leftArrow, rightArrow));
        hs.post(() -> updateRowArrows(hs, leftArrow, rightArrow));
        return hs;
    }

    private View makeScrollArrow(boolean left) {
        final boolean pointsLeft = left;
        View v = new View(this);
        v.setBackground(new Drawable() {
            private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            {
                paint.setColor(0xFFFFFFFF);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(MainActivity.this.getResources().getDisplayMetrics().density * 3f);
                paint.setStrokeCap(Paint.Cap.BUTT);
                paint.setStrokeJoin(Paint.Join.MITER);
            }

            @Override
            public void draw(Canvas canvas) {
                Rect b = getBounds();
                int cy = b.centerY();
                int inset = b.width() / 3;
                int tipX = pointsLeft ? b.left + inset : b.right - inset;
                int baseX = pointsLeft ? b.right - 2 : b.left + 2;
                Path path = new Path();
                path.moveTo(baseX, b.top + 6);
                path.lineTo(tipX, cy);
                path.lineTo(baseX, b.bottom - 6);
                canvas.drawPath(path, paint);
            }

            @Override
            public void setAlpha(int alpha) {
                paint.setAlpha(alpha);
            }

            @Override
            public void setColorFilter(ColorFilter cf) {
                paint.setColorFilter(cf);
            }

            @Override
            public int getOpacity() {
                return PixelFormat.TRANSLUCENT;
            }
        });
        v.setVisibility(View.INVISIBLE);
        return v;
    }

    private void refreshRowArrows() {
        for (RowArrows ra : this.rowArrows) {
            updateRowArrows(ra.hs, ra.left, ra.right);
        }
    }

    private void updateRowArrows(HorizontalScrollView hs, View left, View right) {
        if (hs.getWidth() <= 0 || hs.getChildCount() == 0 || hs.getParent() == null) return;
        float density = getResources().getDisplayMetrics().density;
        int w = Math.round(density * 20f);
        int h = Math.round(density * 24f);
        View host = getWindow().getDecorView();
        int[] hsLoc = new int[2];
        hs.getLocationOnScreen(hsLoc);
        int[] hostLoc = new int[2];
        host.getLocationOnScreen(hostLoc);
        int hsLeft = hsLoc[0] - hostLoc[0];
        int hsTop = hsLoc[1] - hostLoc[1];
        int hsRight = hsLeft + hs.getWidth();
        LinearLayout row = (LinearLayout) hs.getChildAt(0);

        int imageCenterY = hsTop + hs.getHeight() / 2;
        if (row.getChildCount() > 0 && row.getChildAt(0) instanceof ViewGroup) {
            ViewGroup tile = (ViewGroup) row.getChildAt(0);
            for (int i = 0; i < tile.getChildCount(); i++) {
                View child = tile.getChildAt(i);
                if (child instanceof FrameLayout) {
                    int[] loc = new int[2];
                    child.getLocationOnScreen(loc);
                    imageCenterY = loc[1] + child.getHeight() / 2;
                    break;
                }
            }
        }
        int y = imageCenterY - hostLoc[1] - h / 2;
        int leftX = Math.max(0, (hsLeft - w) / 2);
        int rightX = hsRight + Math.max(0, (host.getWidth() - hsRight - w) / 2);

        int last = row.getChildCount() - 1;
        int idx = -1;
        View v = getCurrentFocus();
        while (v != null && v.getParent() != row) {
            v = v.getParent() instanceof View ? (View) v.getParent() : null;
        }
        if (v != null) idx = row.indexOfChild(v);
        left.setVisibility(idx > 0 ? View.VISIBLE : View.INVISIBLE);
        right.setVisibility(idx >= 0 && idx < last ? View.VISIBLE : View.INVISIBLE);

        left.layout(leftX, y, leftX + w, y + h);
        right.layout(rightX, y, rightX + w, y + h);
    }

    private void clearRowArrows() {
        if (this.rowArrows.isEmpty()) return;
        ViewGroup overlayHost = (ViewGroup) getWindow().getDecorView();
        for (RowArrows ra : this.rowArrows) {
            overlayHost.getOverlay().remove(ra.left);
            overlayHost.getOverlay().remove(ra.right);
        }
        this.rowArrows.clear();
    }

    private View makeTile(String label, Drawable icon, View.OnClickListener onClick, View.OnLongClickListener onLongClick) {
        boolean bannerLike;
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(6, 6, 6, 10);
        linearLayout.setFocusable(true);
        linearLayout.setClickable(true);
        linearLayout.setOnClickListener(onClick);
        if (onLongClick != null) {
            linearLayout.setOnLongClickListener(onLongClick);
        }
        linearLayout.setBackgroundResource(R.drawable.tile_bg);
        linearLayout.setClipToOutline(true);
        final FrameLayout frameLayout = new FrameLayout(this);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(318, 180);
        frameLayout.setLayoutParams(cp);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(0);
        cardBg.setCornerRadius(14.0f);
        frameLayout.setBackground(cardBg);
        frameLayout.setClipToOutline(true);
        frameLayout.setOutlineProvider(new ViewOutlineProvider() { // from class: com.local.homelauncher.MainActivity.4

            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), MainActivity.this.getResources().getDisplayMetrics().density * 14.0f);
            }
        });
        ImageView iv = new ImageView(this);
        iv.setClipToOutline(true);
        iv.setOutlineProvider(new ViewOutlineProvider() { // from class: com.local.homelauncher.MainActivity.5

            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), MainActivity.this.getResources().getDisplayMetrics().density * 14.0f);
            }
        });
        FrameLayout.LayoutParams ip = new FrameLayout.LayoutParams(-1, -1);
        iv.setLayoutParams(ip);
        Drawable d = icon != null ? icon : getDrawable(android.R.drawable.sym_def_app_icon);
        int dw = d != null ? d.getIntrinsicWidth() : 0;
        int dh = d != null ? d.getIntrinsicHeight() : 0;
        if (dw > 0 && dh > 0 && ((float) dw / (float) dh) > 1.4f) {
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ip.setMargins(0, 0, 0, 0);
        } else {
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            ip.setMargins(0, 0, 0, 0);
        }
        iv.setLayoutParams(ip);
        iv.setImageDrawable(d);
        frameLayout.addView(iv);
        linearLayout.addView(frameLayout);
        final TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextColor(-855638017);
        tv.setTextSize(13.0f);
        tv.setMaxLines(1);
        tv.setEllipsize(TextUtils.TruncateAt.END);
        tv.setGravity(1);
        tv.setWidth(318);
        tv.setPadding(8, 10, 8, 0);
        tv.setTypeface(Typeface.create("sans-serif-medium", 0));
        linearLayout.addView(tv);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.setMargins(15, 8, 15, 8);
        linearLayout.setLayoutParams(lp);
        linearLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda19
            @Override // android.view.View.OnFocusChangeListener
            public final void onFocusChange(View view, boolean z) {
                MainActivity.this.m61lambda$makeTile$10$comlocalhomelauncherMainActivity(tv, frameLayout, view, z);
                MainActivity.this.refreshRowArrows();
            }
        });
        linearLayout.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() { // from class: com.local.homelauncher.MainActivity.6
            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewAttachedToWindow(View v) {
                boolean moving = MainActivity.this.movingKey != null && MainActivity.this.movingKey.equals(v.getTag());
                frameLayout.setForeground((moving || v.isFocused()) ? MainActivity.this.getDrawable(R.drawable.card_focus) : null);
            }

            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewDetachedFromWindow(View v) {
            }
        });
        return linearLayout;
    }

    /* renamed from: com.local.homelauncher.MainActivity$4 */
    /* loaded from: classes3.dex */
    public class AnonymousClass4 extends ViewOutlineProvider {

        @Override // android.view.ViewOutlineProvider
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), MainActivity.this.getResources().getDisplayMetrics().density * 14.0f);
        }
    }

    /* renamed from: com.local.homelauncher.MainActivity$5 */
    /* loaded from: classes3.dex */
    public class AnonymousClass5 extends ViewOutlineProvider {

        @Override // android.view.ViewOutlineProvider
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), MainActivity.this.getResources().getDisplayMetrics().density * 14.0f);
        }
    }

    /* renamed from: lambda$makeTile$10$com-local-homelauncher-MainActivity */
    public /* synthetic */ void m61lambda$makeTile$10$comlocalhomelauncherMainActivity(TextView tv, FrameLayout card, View v, boolean hasFocus) {
        float s = hasFocus ? 1.04f : 1.0f;
        v.animate().scaleX(s).scaleY(s).setDuration(140L).start();
        tv.setTextColor(hasFocus ? -1 : -855638017);
        boolean moving = this.movingKey != null && this.movingKey.equals(v.getTag());
        if (moving) {
            card.setForeground(getDrawable(R.drawable.card_focus));
        } else {
            card.setForeground(hasFocus ? getDrawable(R.drawable.card_focus) : null);
        }
        if (hasFocus) {
            Object cat = v.getTag(R.integer.abc_config_activityShortDur);
            String c = cat instanceof String ? (String) cat : null;
            if ((c == null && this.currentCategoryWallpaper != null) || (c != null && !c.equals(this.currentCategoryWallpaper))) {
                this.currentCategoryWallpaper = c;
                loadWallpaper();
            }
        }
    }


    /* loaded from: classes3.dex */
    public static class AppItem {
        String cls;
        Drawable icon;
        boolean isLeanback;
        String label;
        String pkg;

        AppItem() {
        }
    }

    private Set<String> hiddenApps() {
        return new HashSet(getSharedPreferences("launcher", 0).getStringSet("hidden_apps", new HashSet()));
    }

    private void setHiddenApp(String key, boolean hidden) {
        Set<String> s = hiddenApps();
        if (hidden) {
            s.add(key);
        } else {
            s.remove(key);
        }
        getSharedPreferences("launcher", 0).edit().putStringSet("hidden_apps", s).apply();
    }

    private boolean showHidden() {
        return getSharedPreferences("launcher", 0).getBoolean("show_hidden", false);
    }

    private void setShowHidden(boolean v) {
        getSharedPreferences("launcher", 0).edit().putBoolean("show_hidden", v).apply();
    }

    private Set<String> hiddenInputs() {
        return new HashSet(getSharedPreferences("launcher", 0).getStringSet("hidden_inputs", new HashSet()));
    }

    private void setHiddenInput(String id, boolean hidden) {
        Set<String> s = hiddenInputs();
        if (hidden) {
            s.add(id);
        } else {
            s.remove(id);
        }
        getSharedPreferences("launcher", 0).edit().putStringSet("hidden_inputs", s).apply();
    }

    private List<String> getAppOrder() {
        String raw = getSharedPreferences("launcher", 0).getString("app_order", "[]");
        List<String> out = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                out.add(arr.getString(i));
            }
        } catch (Exception e) {
        }
        return out;
    }

    private void saveAppOrder(List<String> order) {
        JSONArray arr = new JSONArray();
        for (String s : order) {
            arr.put(s);
        }
        getSharedPreferences("launcher", 0).edit().putString("app_order", arr.toString()).apply();
    }

    private void moveApp(String key, int delta) {
        int ni;
        List<String> order = getAppOrder();
        List<AppItem> all = getAllAppsUnsorted();
        for (AppItem a : all) {
            String k = a.pkg + "/" + a.cls;
            if (!order.contains(k)) {
                order.add(k);
            }
        }
        int idx = order.indexOf(key);
        if (idx >= 0 && (ni = Math.max(0, Math.min(order.size() - 1, idx + delta))) != idx) {
            order.remove(idx);
            order.add(ni, key);
            saveAppOrder(order);
        }
    }

    private List<AppItem> getFavoriteApps() {
        Set<String> favs = favorites();
        List<AppItem> out = new ArrayList<>();
        for (AppItem a : getAllAppsUnsorted()) {
            String k = a.pkg + "/" + a.cls;
            if (favs.contains(k)) {
                out.add(a);
            }
        }
        final List<String> order = getAppOrder();
        Collections.sort(out, new Comparator() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda5
            @Override // java.util.Comparator
            public final int compare(Object obj, Object obj2) {
                return MainActivity.lambda$getFavoriteApps$11(order, (MainActivity.AppItem) obj, (MainActivity.AppItem) obj2);
            }
        });
        return out;
    }

    public static /* synthetic */ int lambda$getFavoriteApps$11(List order, AppItem a, AppItem b) {
        String ka = a.pkg + "/" + a.cls;
        String kb = b.pkg + "/" + b.cls;
        int ia = order.indexOf(ka);
        int ib = order.indexOf(kb);
        if (ia == -1 && ib == -1) {
            return a.label.toLowerCase(Locale.getDefault()).compareTo(b.label.toLowerCase(Locale.getDefault()));
        }
        if (ia == -1) {
            return 1;
        }
        if (ib == -1) {
            return -1;
        }
        return Integer.compare(ia, ib);
    }

    private Drawable loadCustomIcon(String key) {
        Bitmap bm;
        String val = customIcon(key);
        if (val == null) {
            return null;
        }
        try {
            if (val.startsWith("/")) {
                bm = BitmapFactory.decodeFile(val);
            } else {
                InputStream is = getContentResolver().openInputStream(Uri.parse(val));
                try {
                    Bitmap bm2 = BitmapFactory.decodeStream(is);
                    if (is != null) {
                        is.close();
                    }
                    bm = bm2;
                } finally {
                }
            }
            if (bm == null) {
                return null;
            }
            return new BitmapDrawable(getResources(), bm);
        } catch (Exception e) {
            return null;
        }
    }

    private String iconFileForKey(String key) {
        String safe = key.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (safe.length() > 120) {
            safe = safe.substring(0, 120);
        }
        File dir = new File(getFilesDir(), "icons");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, safe + ".png").getAbsolutePath();
    }

    private void showAppMenu(final AppItem a) {
        final String key = a.pkg + "/" + a.cls;
        final boolean hidden = hiddenApps().contains(key);
        final Dialog dialog = new Dialog(this, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(36, 28, 36, 28);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(-233170406);
        bg.setCornerRadius(24.0f);
        bg.setStroke(2, 872415231);
        linearLayout.setBackground(bg);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        linearLayout2.setGravity(16);
        linearLayout2.setPadding(0, 0, 0, 20);
        ImageView icon = new ImageView(this);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(72, 72);
        ilp.setMarginEnd(20);
        icon.setLayoutParams(ilp);
        if (a.icon != null) {
            icon.setImageDrawable(a.icon);
        } else {
            icon.setImageResource(android.R.drawable.sym_def_app_icon);
        }
        TextView title = new TextView(this);
        title.setText(a.label);
        title.setTextColor(-1);
        title.setTextSize(22.0f);
        title.setTypeface(Typeface.create("sans-serif-medium", 0));
        linearLayout2.addView(icon);
        linearLayout2.addView(title);
        linearLayout.addView(linearLayout2);
        View line = new View(this);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(-1, 1);
        llp.bottomMargin = 12;
        line.setLayoutParams(llp);
        line.setBackgroundColor(587202559);
        linearLayout.addView(line);

        final View blank = new View(this);
        blank.setFocusable(true);
        blank.setClickable(true);
        blank.setOnClickListener(v -> { });
        LinearLayout.LayoutParams blankLp = new LinearLayout.LayoutParams(
                -1, (int) (getResources().getDisplayMetrics().density * 10));
        blank.setLayoutParams(blankLp);
        linearLayout.addView(blank);

        final boolean isFav = favorites().contains(key);
        addMenuOption(linearLayout, R.drawable.ic_piv_open, "Open", new Runnable() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda8
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m64lambda$showAppMenu$12$comlocalhomelauncherMainActivity(dialog, a);
            }
        });
        addMenuOption(linearLayout, R.drawable.ic_piv_settings, "App settings", new Runnable() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda10
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m66lambda$showAppMenu$14$comlocalhomelauncherMainActivity(dialog, a);
            }
        });
        addMenuOption(linearLayout, R.drawable.ic_piv_settings, "Change icon", new Runnable() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda12
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m67lambda$showAppMenu$15$comlocalhomelauncherMainActivity(dialog, a, key);
            }
        });
        addMenuOption(linearLayout, R.drawable.ic_piv_open, isFav ? "Remove from favorites" : "Add to favorites", new Runnable() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda13
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m68lambda$showAppMenu$16$comlocalhomelauncherMainActivity(dialog, key, isFav);
            }
        });
        addMenuOption(linearLayout, R.drawable.ic_piv_move, "Move", new Runnable() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda14
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m69lambda$showAppMenu$17$comlocalhomelauncherMainActivity(dialog, key);
            }
        });
        addMenuOption(linearLayout, R.drawable.ic_piv_hide, hidden ? "Unhide" : "Hide", new Runnable() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda15
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m70lambda$showAppMenu$18$comlocalhomelauncherMainActivity(dialog, key, hidden);
            }
        });
        addMenuOption(linearLayout, R.drawable.ic_piv_hide, showHidden() ? "Hide hidden apps" : "Show hidden apps", new Runnable() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda16
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m71lambda$showAppMenu$19$comlocalhomelauncherMainActivity(dialog);
            }
        });
        addMenuOption(linearLayout, R.drawable.ic_piv_delete, "Uninstall", new Runnable() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda17
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m72lambda$showAppMenu$20$comlocalhomelauncherMainActivity(dialog, a);
            }
        });
        Objects.requireNonNull(dialog);
        addMenuOption(linearLayout, 0, "Cancel", new Runnable() { @Override public void run() { dialog.dismiss(); } });
        dialog.setContentView(linearLayout);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            WindowManager.LayoutParams wlp = dialog.getWindow().getAttributes();
            wlp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.32d);
            dialog.getWindow().setAttributes(wlp);
        }
        dialog.show();
        blank.post(() -> blank.requestFocus());
    }

    /* renamed from: lambda$showAppMenu$12$com-local-homelauncher-MainActivity */
    public /* synthetic */ void m64lambda$showAppMenu$12$comlocalhomelauncherMainActivity(Dialog dlg, AppItem a) {
        dlg.dismiss();
        launchApp(a.pkg, a.cls);
    }

    /* renamed from: lambda$showAppMenu$14$com-local-homelauncher-MainActivity */
    public /* synthetic */ void m66lambda$showAppMenu$14$comlocalhomelauncherMainActivity(Dialog dlg, AppItem a) {
        dlg.dismiss();
        try {
            startActivity(new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.parse("package:" + a.pkg)));
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open settings", 0).show();
        }
    }

    /* renamed from: lambda$showAppMenu$15$com-local-homelauncher-MainActivity */
    public /* synthetic */ void m67lambda$showAppMenu$15$comlocalhomelauncherMainActivity(Dialog dlg, AppItem a, String key) {
        dlg.dismiss();
        showIconMenu(a, key);
    }

    /* renamed from: lambda$showAppMenu$16$com-local-homelauncher-MainActivity */
    public /* synthetic */ void m68lambda$showAppMenu$16$comlocalhomelauncherMainActivity(Dialog dlg, String key, boolean isFav) {
        dlg.dismiss();
        setFavorite(key, !isFav);
        m76lambda$showMoveMenu$23$comlocalhomelauncherMainActivity();
    }

    /* renamed from: lambda$showAppMenu$17$com-local-homelauncher-MainActivity */
    public /* synthetic */ void m69lambda$showAppMenu$17$comlocalhomelauncherMainActivity(Dialog dlg, String key) {
        dlg.dismiss();
        enterMoveMode(key);
    }

    /* renamed from: lambda$showAppMenu$18$com-local-homelauncher-MainActivity */
    public /* synthetic */ void m70lambda$showAppMenu$18$comlocalhomelauncherMainActivity(Dialog dlg, String key, boolean hidden) {
        dlg.dismiss();
        setHiddenApp(key, !hidden);
        m76lambda$showMoveMenu$23$comlocalhomelauncherMainActivity();
    }

    /* renamed from: lambda$showAppMenu$19$com-local-homelauncher-MainActivity */
    public /* synthetic */ void m71lambda$showAppMenu$19$comlocalhomelauncherMainActivity(Dialog dlg) {
        dlg.dismiss();
        setShowHidden(!showHidden());
        m76lambda$showMoveMenu$23$comlocalhomelauncherMainActivity();
    }

    /* renamed from: lambda$showAppMenu$20$com-local-homelauncher-MainActivity */
    public /* synthetic */ void m72lambda$showAppMenu$20$comlocalhomelauncherMainActivity(Dialog dlg, AppItem a) {
        dlg.dismiss();
        try {
            startActivity(new Intent("android.intent.action.DELETE", Uri.parse("package:" + a.pkg)));
        } catch (Exception e) {
            Toast.makeText(this, "Cannot uninstall", 0).show();
        }
    }

    private void enterMoveMode(String key) {
        this.movingKey = key;
        Toast.makeText(this, "Move mode: Left/Right to move, Select to place, Back to cancel", 1).show();
        m76lambda$showMoveMenu$23$comlocalhomelauncherMainActivity();
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (this.movingKey != null && event.getAction() == 0) {
            int code = event.getKeyCode();
            if (code == 21) {
                moveApp(this.movingKey, -1);
                m76lambda$showMoveMenu$23$comlocalhomelauncherMainActivity();
                focusMoving();
                return true;
            }
            if (code == 22) {
                moveApp(this.movingKey, 1);
                m76lambda$showMoveMenu$23$comlocalhomelauncherMainActivity();
                focusMoving();
                return true;
            }
            if (code == 23 || code == 66 || code == 160) {
                this.movingKey = null;
                Toast.makeText(this, "Placed", 0).show();
                m76lambda$showMoveMenu$23$comlocalhomelauncherMainActivity();
                return true;
            }
            if (code == 4 || code == 111) {
                this.movingKey = null;
                Toast.makeText(this, "Move cancelled", 0).show();
                m76lambda$showMoveMenu$23$comlocalhomelauncherMainActivity();
                return true;
            }
        }
        if (event.getAction() == 0) {
            int code = event.getKeyCode();
            if (code == 21 || code == 22) {
                LinearLayout row = findTileRow(getCurrentFocus());
                if (row != null && row.getChildCount() > 1) {
                    View tile = getCurrentFocus();
                    while (tile != null && tile.getParent() != row) {
                        tile = tile.getParent() instanceof View ? (View) tile.getParent() : null;
                    }
                    int idx = row.indexOfChild(tile);
                    int last = row.getChildCount() - 1;
                    boolean atLeftEdge = code == 21 && idx == 0;
                    boolean atRightEdge = code == 22 && idx == last;
                    if (atLeftEdge || atRightEdge) {
                        if (event.getRepeatCount() == 0) {
                            focusWrappedTile(row, atLeftEdge ? row.getChildAt(last) : row.getChildAt(0), atLeftEdge);
                        }
                        return true;
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private LinearLayout findTileRow(View v) {
        while (v != null && v != this.rows) {
            Object p = v.getParent();
            if (p instanceof LinearLayout && ((View) p).getParent() instanceof HorizontalScrollView) {
                return (LinearLayout) p;
            }
            v = p instanceof View ? (View) p : null;
        }
        return null;
    }

    private void focusWrappedTile(LinearLayout row, View target, boolean toLast) {
        target.requestFocus();
        HorizontalScrollView hs = (HorizontalScrollView) row.getParent();
        hs.post(() -> {
            if (row.getChildCount() == 0) return;
            int left = target.getLeft();
            int right = left + target.getWidth();
            int pad = 8;
            int wanted = toLast ? right + pad - hs.getWidth() : left - pad;
            int max = Math.max(0, row.getWidth() - hs.getWidth());
            hs.scrollTo(Math.max(0, Math.min(wanted, max)), 0);
        });
    }

    private void focusMoving() {
        if (this.movingKey == null) {
            return;
        }
        for (int i = 0; i < this.rows.getChildCount(); i++) {
            View r = this.rows.getChildAt(i);
            if (r instanceof HorizontalScrollView) {
                HorizontalScrollView hs = (HorizontalScrollView) r;
                if (hs.getChildCount() == 0) {
                    continue;
                } else {
                    View child = hs.getChildAt(0);
                    if (child instanceof LinearLayout) {
                        LinearLayout row = (LinearLayout) child;
                        for (int j = 0; j < row.getChildCount(); j++) {
                            View t = row.getChildAt(j);
                            Object tag = t.getTag();
                            if (this.movingKey.equals(tag)) {
                                t.requestFocus();
                                return;
                            }
                        }
                    } else {
                        continue;
                    }
                }
            }
        }
    }

    private void showIconMenu(AppItem a, final String key) {
        final Dialog dialog = new Dialog(this, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(1);
        root.setPadding(36, 28, 36, 28);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(-233170406);
        bg.setCornerRadius(24.0f);
        bg.setStroke(2, 872415231);
        root.setBackground(bg);
        TextView title = new TextView(this);
        title.setText("Change icon");
        title.setTextColor(-1);
        title.setTextSize(20.0f);
        title.setPadding(0, 0, 0, 16);
        title.setTypeface(Typeface.create("sans-serif-medium", 0));
        root.addView(title);
        addMenuOption(root, R.drawable.ic_piv_settings, "Pick from image", new Runnable() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda24
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m73lambda$showIconMenu$21$comlocalhomelauncherMainActivity(dialog, key);
            }
        });
        addMenuOption(root, R.drawable.ic_piv_hide, "Reset icon", new Runnable() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda25
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m74lambda$showIconMenu$22$comlocalhomelauncherMainActivity(dialog, key);
            }
        });
        Objects.requireNonNull(dialog);
        addMenuOption(root, 0, "Cancel", new Runnable() { @Override public void run() { dialog.dismiss(); } });
        dialog.setContentView(root);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            WindowManager.LayoutParams wlp = dialog.getWindow().getAttributes();
            wlp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.28d);
            dialog.getWindow().setAttributes(wlp);
        }
        dialog.show();
    }

    /* renamed from: lambda$showIconMenu$21$com-local-homelauncher-MainActivity */
    public /* synthetic */ void m73lambda$showIconMenu$21$comlocalhomelauncherMainActivity(Dialog dlg, String key) {
        dlg.dismiss();
        this.pendingIconKey = key;
        Intent pi = new Intent("android.intent.action.OPEN_DOCUMENT");
        pi.addCategory("android.intent.category.OPENABLE");
        pi.setType("image/*");
        pi.addFlags(65);
        try {
            startActivityForResult(pi, REQ_ICON);
        } catch (Exception e) {
            try {
                Intent alt = new Intent("android.intent.action.GET_CONTENT");
                alt.addCategory("android.intent.category.OPENABLE");
                alt.setType("image/*");
                startActivityForResult(Intent.createChooser(alt, "Pick icon"), REQ_ICON);
            } catch (Exception e2) {
                Toast.makeText(this, "Cannot open picker", 0).show();
            }
        }
    }

    /* renamed from: lambda$showIconMenu$22$com-local-homelauncher-MainActivity */
    public /* synthetic */ void m74lambda$showIconMenu$22$comlocalhomelauncherMainActivity(Dialog dlg, String key) {
        dlg.dismiss();
        setCustomIcon(key, null);
        m76lambda$showMoveMenu$23$comlocalhomelauncherMainActivity();
    }

    private void showMoveMenu(AppItem a) {
        final String key = a.pkg + "/" + a.cls;
        final Dialog dialog = new Dialog(this, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(1);
        root.setPadding(36, 28, 36, 28);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(-233170406);
        bg.setCornerRadius(24.0f);
        bg.setStroke(2, 872415231);
        root.setBackground(bg);
        TextView title = new TextView(this);
        title.setText("Move " + a.label);
        title.setTextColor(-1);
        title.setTextSize(20.0f);
        title.setPadding(0, 0, 0, 16);
        title.setTypeface(Typeface.create("sans-serif-medium", 0));
        root.addView(title);
        final Runnable refresh = new Runnable() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda30
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m76lambda$showMoveMenu$23$comlocalhomelauncherMainActivity();
            }
        };
        addMenuOption(root, R.drawable.ic_piv_move, "Move left", new Runnable() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda31
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m77lambda$showMoveMenu$24$comlocalhomelauncherMainActivity(key, refresh, dialog);
            }
        });
        addMenuOption(root, R.drawable.ic_piv_move, "Move right", new Runnable() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda32
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m78lambda$showMoveMenu$25$comlocalhomelauncherMainActivity(key, refresh, dialog);
            }
        });
        addMenuOption(root, R.drawable.ic_piv_move, "Move to start", new Runnable() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m79lambda$showMoveMenu$26$comlocalhomelauncherMainActivity(key, refresh, dialog);
            }
        });
        addMenuOption(root, R.drawable.ic_piv_move, "Move to end", new Runnable() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m80lambda$showMoveMenu$27$comlocalhomelauncherMainActivity(key, refresh, dialog);
            }
        });
        Objects.requireNonNull(dialog);
        addMenuOption(root, 0, "Cancel", new Runnable() { @Override public void run() { dialog.dismiss(); } });
        dialog.setContentView(root);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            WindowManager.LayoutParams wlp = dialog.getWindow().getAttributes();
            wlp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.28d);
            dialog.getWindow().setAttributes(wlp);
        }
        dialog.show();
    }

    /* renamed from: lambda$showMoveMenu$24$com-local-homelauncher-MainActivity */
    public /* synthetic */ void m77lambda$showMoveMenu$24$comlocalhomelauncherMainActivity(String key, Runnable refresh, Dialog dlg) {
        moveApp(key, -1);
        refresh.run();
        dlg.dismiss();
    }

    /* renamed from: lambda$showMoveMenu$25$com-local-homelauncher-MainActivity */
    public /* synthetic */ void m78lambda$showMoveMenu$25$comlocalhomelauncherMainActivity(String key, Runnable refresh, Dialog dlg) {
        moveApp(key, 1);
        refresh.run();
        dlg.dismiss();
    }

    /* renamed from: lambda$showMoveMenu$26$com-local-homelauncher-MainActivity */
    public /* synthetic */ void m79lambda$showMoveMenu$26$comlocalhomelauncherMainActivity(String key, Runnable refresh, Dialog dlg) {
        moveAppToEdge(key, true);
        refresh.run();
        dlg.dismiss();
    }

    /* renamed from: lambda$showMoveMenu$27$com-local-homelauncher-MainActivity */
    public /* synthetic */ void m80lambda$showMoveMenu$27$comlocalhomelauncherMainActivity(String key, Runnable refresh, Dialog dlg) {
        moveAppToEdge(key, false);
        refresh.run();
        dlg.dismiss();
    }

    private void moveAppToEdge(String key, boolean start) {
        List<String> order = getAppOrder();
        List<AppItem> all = getAllAppsUnsorted();
        for (AppItem a : all) {
            String k = a.pkg + "/" + a.cls;
            if (!order.contains(k)) {
                order.add(k);
            }
        }
        order.remove(key);
        if (start) {
            order.add(0, key);
        } else {
            order.add(key);
        }
        saveAppOrder(order);
    }

    private Set<String> favorites() {
        return new HashSet(getSharedPreferences("launcher", 0).getStringSet("favorites", new HashSet()));
    }

    private void setFavorite(String key, boolean fav) {
        Set<String> s = favorites();
        if (fav) {
            s.add(key);
        } else {
            s.remove(key);
        }
        getSharedPreferences("launcher", 0).edit().putStringSet("favorites", s).apply();
    }

    private String customIcon(String key) {
        return getSharedPreferences("launcher", 0).getString("icon_" + key, null);
    }

    private void setCustomIcon(String key, String uri) {
        SharedPreferences.Editor e = getSharedPreferences("launcher", 0).edit();
        String old = customIcon(key);
        if (old != null && old.startsWith("/")) {
            try {
                new File(old).delete();
            } catch (Exception e2) {
            }
        }
        if (uri == null) {
            e.remove("icon_" + key);
        } else {
            e.putString("icon_" + key, uri);
        }
        e.apply();
    }

    private void addMenuOption(LinearLayout linearLayout, int iconRes, String text, final Runnable action) {
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
        linearLayout2.setOnClickListener(new View.OnClickListener() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda6
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                action.run();
            }
        });
        linearLayout2.setOnFocusChangeListener(new View.OnFocusChangeListener() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda7
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

    private void showInputMenu(final String id, String label) {
        final boolean hidden = hiddenInputs().contains(id);
        final List<String> opts = new ArrayList<>();
        opts.add("Switch");
        opts.add(hidden ? "Unhide" : "Hide");
        new AlertDialog.Builder(this).setTitle(label).setItems((CharSequence[]) opts.toArray(new String[0]), new DialogInterface.OnClickListener() { // from class: com.local.homelauncher.MainActivity$$ExternalSyntheticLambda22
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.m75lambda$showInputMenu$30$comlocalhomelauncherMainActivity(opts, id, hidden, dialogInterface, i);
            }
        }).show();
    }

    /* renamed from: lambda$showInputMenu$30$com-local-homelauncher-MainActivity */
    public /* synthetic */ void m75lambda$showInputMenu$30$comlocalhomelauncherMainActivity(List opts, String id, boolean hidden, DialogInterface d, int which) {
        String o = (String) opts.get(which);
        if (!"Switch".equals(o)) {
            if ("Hide".equals(o) || "Unhide".equals(o)) {
                setHiddenInput(id, !hidden);
                m76lambda$showMoveMenu$23$comlocalhomelauncherMainActivity();
                return;
            }
            return;
        }
        launchInput(id);
    }

    @Override // android.app.Activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_ICON) {
            if (resultCode == -1 && data != null && data.getData() != null && this.pendingIconKey != null) {
                Uri uri = data.getData();
                try {
                    getContentResolver().takePersistableUriPermission(uri, 1);
                } catch (Exception e) {
                }
                try {
                    InputStream is = getContentResolver().openInputStream(uri);
                    try {
                        Bitmap bm = BitmapFactory.decodeStream(is);
                        if (bm == null) {
                            Toast.makeText(this, "Could not read image", 0).show();
                        } else {
                            String path = iconFileForKey(this.pendingIconKey);
                            FileOutputStream fos = new FileOutputStream(path);
                            try {
                                bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                fos.close();
                                setCustomIcon(this.pendingIconKey, path);
                                m76lambda$showMoveMenu$23$comlocalhomelauncherMainActivity();
                                Toast.makeText(this, "Icon updated", 0).show();
                            } finally {
                            }
                        }
                        if (is != null) {
                            is.close();
                        }
                    } finally {
                    }
                } catch (Exception e2) {
                    Toast.makeText(this, "Icon change failed", 0).show();
                }
            }
            this.pendingIconKey = null;
        }
    }
}
