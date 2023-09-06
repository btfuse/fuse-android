
/*
Copyright 2023 Norman Breau

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package ca.nbsolutions.fuse;

import androidx.annotation.NonNull;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import ca.nbsolutions.fuse.plugins.FuseRuntime;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FuseContext {
    private static final String TAG = "FuseContext";

    private final Context $context;

    private WebView $webview;

    /*package private*/ final ReadWriteLock $pluginMapLock;
    private final Map<String, FusePlugin> $pluginMap;

    private final FuseAPIRouter $apiRouter;

    public static final String SCHEME = "https";
    public static final String HOST = "localhost";

    private final Handler $mainThread;

    private final FuseAPIServer $apiServer;

    public FuseContext(Context context) {
        $context = context;
        $mainThread = new Handler(Looper.getMainLooper());
        $pluginMapLock = new ReentrantReadWriteLock();
        $pluginMap = new HashMap<String, FusePlugin>();
        $apiRouter = new FuseAPIRouter(this);

        try {
            $apiServer = new FuseAPIServer(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Log.i(TAG, "API Server Port: " + $apiServer.getPort());

        registerPlugin(new FuseRuntime(this));
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void onCreate(Bundle bundle) {
        $webview = new WebView($context);

        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler($context))
                .setHttpAllowed(false)
                .setDomain(HOST)
                .build();

        $webview.setWebViewClient(new WebViewClientCompat() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }
        });

        WebSettings settings = $webview.getSettings();
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setJavaScriptEnabled(true);
        $webview.setWebChromeClient(new WebChromeClient());
        $webview.addJavascriptInterface(this, "NBSNative");
        $webview.loadUrl("https://localhost/assets/index.html");
    }

    public void onLowMemory() {
        $pluginMapLock.readLock().lock();
        for (FusePlugin plugin : $pluginMap.values()) {
            plugin.onLowMemory();
        }
        $pluginMapLock.readLock().unlock();
    }

    public void onPause() {
        $pluginMapLock.readLock().lock();
        for (FusePlugin plugin : $pluginMap.values()) {
            plugin.onPause();
        }
        $pluginMapLock.readLock().unlock();
    }

    public void onResume() {
        $pluginMapLock.readLock().lock();
        for (FusePlugin plugin : $pluginMap.values()) {
            plugin.onResume();
        }
        $pluginMapLock.readLock().unlock();
    }

    public void onSaveInstanceState(@NonNull Bundle outState) {
        $pluginMapLock.readLock().lock();
        for (FusePlugin plugin : $pluginMap.values()) {
            plugin.onSaveInstanceState(outState);
        }
        $pluginMapLock.readLock().unlock();
    }

    public void onStart() {
        $pluginMapLock.readLock().lock();
        for (FusePlugin plugin : $pluginMap.values()) {
            plugin.onStart();
        }
        $pluginMapLock.readLock().unlock();
    }

    public void onStop() {
        $pluginMapLock.readLock().lock();
        for (FusePlugin plugin : $pluginMap.values()) {
            plugin.onStop();
        }
        $pluginMapLock.readLock().unlock();
    }

    public void onDestroy() {
        $pluginMapLock.readLock().lock();
        for (FusePlugin plugin : $pluginMap.values()) {
            plugin.onDestroy();
        }
        $pluginMapLock.readLock().unlock();
    }

    public WebView getWebview() {
        return $webview;
    }

    public Context getActivityContext() {
        return $context;
    }

    public void registerPlugin(FusePlugin plugin) {
        $pluginMapLock.writeLock().lock();
        $registerPlugin(plugin);
        $pluginMapLock.writeLock().unlock();
    }

    /*package private*/ void $registerPlugin(FusePlugin plugin) {
        if ($pluginMap.containsKey(plugin.getID())) {
            Log.w(TAG, "Plugin \"" + plugin.getID() + "\" is already registered.");
            return;
        }

        $pluginMap.put(plugin.getID(), plugin);
    }

    public FusePlugin getPlugin(String pluginID) {
        return $pluginMap.getOrDefault(pluginID, null);
    }

    public FuseAPIRouter getAPIRouter() {
        return $apiRouter;
    }

    @JavascriptInterface
    public String getAPISecret() {
        return $apiServer.getSecretKey();
    }

    @JavascriptInterface
    public int getAPIPort() {
        return $apiServer.getPort();
    }

    public void execCallback(String callbackID, String payload) {
        $mainThread.post(() -> {
            $webview.evaluateJavascript(String.format("window.__nbsfuse_doCallback(%s,%s);", callbackID, payload), null);
        });
    }

    public void execCallback(String callbackID) {
        $mainThread.post(() -> {
            $webview.evaluateJavascript(String.format("window.__nbsfuse_doCallback(%s);", callbackID), null);
        });
    }
}
