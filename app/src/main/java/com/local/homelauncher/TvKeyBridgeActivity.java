package com.local.homelauncher;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TvKeyBridgeActivity extends Activity {
    private static final String CONFIG_FILE = "tv_bridge.conf";
    private static final long EXIT_AFTER_MS = 180000L;
    private static final String EXIT_FLAG = "/data/local/tmp/vizio_tv_bridge_exit";
    private static final String BACK_FLAG = "/data/local/tmp/vizio_tv_bridge_back";
    private static final String BRIDGE_ACTIVE_KEY = "tv_bridge_active";

    private volatile String tvBase;
    private volatile String tvAuth;
    private volatile boolean closing = false;

    private final Handler handler = new Handler();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Runnable exitRunnable = () -> finish();
    private final Runnable openMenu = () -> sendKey(4, 8);
    private final Runnable pollExit = new Runnable() {
        @Override
        public void run() {
            try {
                File flag = new File(EXIT_FLAG);
                if (flag.exists() && flag.length() > 0) {
                    new FileOutputStream(flag).close();
                    closeMenuAndFinish();
                    return;
                }
                File backFlag = new File(BACK_FLAG);
                if (backFlag.exists() && backFlag.length() > 0) {
                    new FileOutputStream(backFlag).close();
                    closeMenuAndFinish();
                }
            } catch (Exception ignored) {
            }
            handler.postDelayed(this, 100);
        }
    };

    static {
        try {
            TrustManager[] managers = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }};
            SSLContext ssl = SSLContext.getInstance("TLS");
            ssl.init(null, managers, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(ssl.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.TRANSPARENT);
        root.setFocusable(true);
        root.setFocusableInTouchMode(true);
        setContentView(root);
        root.requestFocus();
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        String[] config = loadConfig();
        tvBase = config[0];
        tvAuth = config[1];

        getSharedPreferences("launcher", 0).edit().putBoolean(BRIDGE_ACTIVE_KEY, true).apply();
        try {
            new FileOutputStream("/data/local/tmp/vizio_bridge_opening").close();
        } catch (Exception ignored) {
        }
        scheduleExit();
        handler.post(pollExit);

        // Open the Vizio quick menu only; navigation is left to the user.
        handler.postDelayed(openMenu, 150);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int code = event.getKeyCode();
        int action = event.getAction();

        if (code == KeyEvent.KEYCODE_HOME) {
            return super.dispatchKeyEvent(event);
        }

        if (isHandledKey(code)) {
            if (action == KeyEvent.ACTION_DOWN) {
                switch (code) {
                    case KeyEvent.KEYCODE_DPAD_UP:
                        sendKey(3, 8);
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        sendKey(3, 0);
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        sendKey(3, 1);
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        sendKey(3, 7);
                        break;
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                        sendKey(3, 2);
                        break;
                    case KeyEvent.KEYCODE_BACK:
                    case KeyEvent.KEYCODE_MENU:
                        closeMenuAndFinish();
                        break;
                }
                scheduleExit();
            }
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    private static boolean isHandledKey(int code) {
        return code == KeyEvent.KEYCODE_DPAD_UP
                || code == KeyEvent.KEYCODE_DPAD_DOWN
                || code == KeyEvent.KEYCODE_DPAD_LEFT
                || code == KeyEvent.KEYCODE_DPAD_RIGHT
                || code == KeyEvent.KEYCODE_DPAD_CENTER
                || code == KeyEvent.KEYCODE_ENTER
                || code == KeyEvent.KEYCODE_BACK
                || code == KeyEvent.KEYCODE_MENU;
    }

    private void scheduleExit() {
        handler.removeCallbacks(exitRunnable);
        handler.postDelayed(exitRunnable, EXIT_AFTER_MS);
    }

    private void closeMenuAndFinish() {
        if (closing) {
            return;
        }
        closing = true;
        handler.removeCallbacks(openMenu);
        executor.execute(() -> {
            postKey(4, 8);
            runOnUiThread(this::finish);
        });
    }

    private void sendKey(int codeSet, int keyCode) {
        executor.execute(() -> postKey(codeSet, keyCode));
    }

    private void postKey(int codeSet, int keyCode) {
        if (tvBase == null || tvAuth == null) {
            return;
        }
        HttpsURLConnection conn = null;
        try {
            JSONObject key = new JSONObject();
            key.put("CODESET", codeSet);
            key.put("CODE", keyCode);
            key.put("ACTION", "KEYPRESS");
            JSONArray list = new JSONArray();
            list.put(key);
            JSONObject body = new JSONObject();
            body.put("KEYLIST", list);

            byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
            conn = (HttpsURLConnection) new URL(tvBase + "/key_command/").openConnection();
            conn.setRequestMethod("PUT");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setDoOutput(true);
            conn.setFixedLengthStreamingMode(payload.length);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("AUTH", tvAuth);
            try (OutputStream out = conn.getOutputStream()) {
                out.write(payload);
            }
            conn.getResponseCode();
        } catch (Exception ignored) {
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String[] loadConfig() {
        File config = new File(getFilesDir(), CONFIG_FILE);
        String base = null;
        String auth = null;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(config), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int separator = line.indexOf('=');
                if (separator <= 0) {
                    continue;
                }
                String key = line.substring(0, separator).trim();
                String value = line.substring(separator + 1).trim();
                if ("base_url".equals(key)) {
                    base = value;
                } else if ("auth".equals(key)) {
                    auth = value;
                }
            }
        } catch (Exception ignored) {
        }
        return new String[]{base, auth};
    }

    @Override
    protected void onDestroy() {
        getSharedPreferences("launcher", 0).edit().putBoolean(BRIDGE_ACTIVE_KEY, false).apply();
        handler.removeCallbacks(pollExit);
        handler.removeCallbacksAndMessages(null);
        executor.shutdownNow();
        super.onDestroy();
    }
}
