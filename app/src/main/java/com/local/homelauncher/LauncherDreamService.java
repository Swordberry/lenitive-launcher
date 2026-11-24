package com.local.homelauncher;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.service.dreams.DreamService;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LauncherDreamService extends DreamService {
    private ImageView iv;
    private VideoView vv;
    private final Handler handler = new Handler();
    private final List<Uri> media = new ArrayList<>();
    private int index = 0;
    private boolean videoMode = false;
    private final Runnable tick = new Runnable() {
        @Override public void run() { nextItem(); }
    };

    @Override public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getSharedPreferences("launcher", 0).edit().putBoolean("screensaver_active", true)
                .remove("screensaver_was_active").apply();
        setInteractive(false);
        setFullscreen(true);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0xFF000000);
        iv = new ImageView(this);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        root.addView(iv, new FrameLayout.LayoutParams(-1, -1));
        vv = new VideoView(this);
        FrameLayout.LayoutParams vlp = new FrameLayout.LayoutParams(-1, -1, Gravity.CENTER);
        vv.setLayoutParams(vlp);
        vv.setVisibility(View.GONE);
        root.addView(vv, vlp);
        TextView clock = new TextView(this);
        clock.setTextColor(-1);
        clock.setTextSize(36f);
        clock.setText(new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date()));
        FrameLayout.LayoutParams cl = new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM | Gravity.END);
        cl.setMargins(32,32,32,32);
        root.addView(clock, cl);
        setContentView(root);

        SharedPreferences p = getSharedPreferences("launcher", 0);
        String category = p.getString("last_wallpaper_category", "");
        if (category == null || category.isEmpty()) category = null;
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
        if ("slideshow".equals(type) && folder != null) {
            loadFolder(Uri.parse(folder));
            String savedOrder = p.getString("slideshow_order", null);
            if (savedOrder != null && !savedOrder.isEmpty() && !media.isEmpty()) {
                List<Uri> ordered = new ArrayList<>();
                List<Uri> remaining = new ArrayList<>(media);
                for (String s : savedOrder.split("\n")) {
                    for (int i = 0; i < remaining.size(); i++) {
                        if (remaining.get(i).toString().equals(s)) {
                            ordered.add(remaining.remove(i));
                            break;
                        }
                    }
                }
                media.clear();
                media.addAll(ordered);
                media.addAll(remaining);
            } else if (random && media.size() > 1) {
                Collections.shuffle(media);
            }
            index = 0;
            String current = p.getString("current_wallpaper_uri", null);
            if (current != null) {
                for (int i = 0; i < media.size(); i++) {
                    if (media.get(i).toString().equals(current)) { index = i; break; }
                }
            }
            if (!media.isEmpty()) {
                long interval = Math.max(5000L, (long) p.getInt("slideshow_interval_ms", 15000));
                long started = p.getLong("slideshow_item_started_at", 0L);
                long delay = interval;
                if (started > 0L) {
                    delay = interval - (System.currentTimeMillis() - started);
                    if (delay < 1000L) delay = 1000L;
                    if (delay > interval) delay = interval;
                }
                showItem(media.get(index), delay, false);
            }
        } else if ("video".equals(type) && uri != null) {
            showVideo(Uri.parse(uri), true);
        } else if (uri != null) {
            showImage(Uri.parse(uri));
        }
    }

    private boolean hasExtension(Uri u, String[] exts) {
        String s = u.toString();
        try { s = java.net.URLDecoder.decode(s, "UTF-8"); } catch (Exception ignored) {}
        s = s.toLowerCase(Locale.ROOT);
        for (String ext : exts) { if (s.endsWith("." + ext)) return true; }
        return false;
    }
    private boolean isVideo(Uri u) {
        return hasExtension(u, new String[]{"mp4","m4v","mov","mkv","webm","3gp","3g2","avi","ts","mts","m2ts","flv","wmv","mpg","mpeg","mpg4","ogv","divx","f4v","rmvb","asf","vob","m2v","h264","h265","hevc"});
    }
    private boolean isImage(Uri u) {
        return hasExtension(u, new String[]{"jpg","jpeg","jpe","jfif","png","webp","gif","bmp","heic","heif","avif","tif","tiff","ico","apng"});
    }

    private void loadFolder(Uri folderUri) {
        media.clear();
        String scheme = folderUri.getScheme();
        if (scheme == null || "file".equals(scheme)) {
            File dir = new File(folderUri.getPath());
            File[] files = dir.isDirectory() ? dir.listFiles() : null;
            if (files != null) for (File f : files) {
                if (f.isFile()) { Uri u = Uri.fromFile(f); if (isImage(u) || isVideo(u)) media.add(u); }
            }
        } else {
            try {
                Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(folderUri, DocumentsContract.getTreeDocumentId(folderUri));
                Cursor c = getContentResolver().query(childrenUri, new String[]{"document_id","mime_type"}, null, null, null);
                if (c != null) {
                    while (c.moveToNext()) {
                        String id = c.getString(0); String mime = c.getString(1);
                        Uri u = DocumentsContract.buildDocumentUriUsingTree(folderUri, id);
                        if ((mime != null && (mime.startsWith("image/") || mime.startsWith("video/"))) || isImage(u) || isVideo(u)) media.add(u);
                    }
                    c.close();
                }
            } catch (Exception ignored) {}
        }
        Collections.sort(media, new Comparator<Uri>() { @Override public int compare(Uri a, Uri b) { return a.toString().compareTo(b.toString()); } });
        index = 0;
    }

    private void nextItem() {
        if (media.isEmpty()) return;
        index = (index + 1) % media.size();
        showItem(media.get(index));
    }

    private void showItem(Uri uri) {
        showItem(uri, -1L, true);
    }

    private void showItem(Uri uri, long delayOverride, boolean resetStartedAt) {
        handler.removeCallbacks(tick);
        SharedPreferences p = getSharedPreferences("launcher", 0);
        long interval = Math.max(5000L, (long) p.getInt("slideshow_interval_ms", 15000));
        long delay = delayOverride >= 0L ? delayOverride : interval;
        SharedPreferences.Editor e = p.edit();
        e.putString("current_wallpaper_uri", uri != null ? uri.toString() : null);
        if (resetStartedAt) e.putLong("slideshow_item_started_at", System.currentTimeMillis());
        e.apply();
        String mime = null;
        try { mime = getContentResolver().getType(uri); } catch (Exception ignored) {}
        boolean video = (mime != null && mime.startsWith("video/")) || isVideo(uri);
        if (video) showVideo(uri, true);
        else showImage(uri);
        handler.postDelayed(tick, delay);
    }

    private void showImage(Uri uri) {
        try { if (vv.isPlaying()) vv.stopPlayback(); } catch (Exception ignored) {}
        vv.setVisibility(View.GONE);
        iv.setVisibility(View.VISIBLE);
        iv.setImageBitmap(uri != null ? decodeBitmap(uri) : null);
    }

    private android.graphics.Bitmap decodeBitmap(Uri uri) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            InputStream is1 = getContentResolver().openInputStream(uri);
            try { BitmapFactory.decodeStream(is1, null, bounds); } finally { if (is1 != null) is1.close(); }
            int sw = bounds.outWidth, sh = bounds.outHeight;
            if (sw <= 0 || sh <= 0) return null;
            int targetW = Math.max(1, getResources().getDisplayMetrics().widthPixels);
            int targetH = Math.max(1, getResources().getDisplayMetrics().heightPixels);
            int sample = 1;
            while (sw / (sample * 2) >= targetW && sh / (sample * 2) >= targetH) sample *= 2;
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = Math.max(1, sample);
            opts.inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888;
            InputStream is2 = getContentResolver().openInputStream(uri);
            try { return BitmapFactory.decodeStream(is2, null, opts); } finally { if (is2 != null) is2.close(); }
        } catch (Exception e) { return null; }
    }

    private void showVideo(Uri uri, boolean looping) {
        iv.setVisibility(View.GONE);
        vv.setVisibility(View.VISIBLE);
        try {
            vv.setVideoURI(uri);
            vv.setOnPreparedListener(mp -> { mp.setVolume(0f,0f); mp.setLooping(looping); try { mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING); } catch (Exception ignored) {} });
            if (looping) vv.setOnCompletionListener(null);
            else vv.setOnCompletionListener(mp -> nextItem());
            vv.start();
        } catch (Exception e) {
            vv.setVisibility(View.GONE);
            iv.setVisibility(View.VISIBLE);
            if (!looping) nextItem();
        }
    }

    @Override public void onDreamingStopped() {
        getSharedPreferences("launcher", 0).edit().putBoolean("screensaver_active", false)
                .putBoolean("screensaver_was_active", true).remove("screensaver_requested").apply();
        handler.removeCallbacks(tick);
        try { vv.stopPlayback(); } catch (Exception ignored) {}
        super.onDreamingStopped();
    }
    @Override public void onDetachedFromWindow() {
        getSharedPreferences("launcher", 0).edit().putBoolean("screensaver_active", false)
                .putBoolean("screensaver_was_active", true).remove("screensaver_requested").apply();
        handler.removeCallbacks(tick);
        try { vv.stopPlayback(); } catch (Exception ignored) {}
        super.onDetachedFromWindow();
    }
}
