
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

import android.annotation.SuppressLint;
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

public class FuseContext {
    private static final String TAG = "FuseContext";

    private final Context $context;

    private final WebView $webview;

    private final Map<String, FusePlugin> $pluginMap;

    private final FuseAPIRouter $apiRouter;

    public static final String SCHEME = "https";
    public static final String HOST = "localhost";

    private final Handler $mainThread;

    private final FuseAPIServer $apiServer;

    @SuppressLint("SetJavaScriptEnabled")
    public FuseContext(Context context) {
        $context = context;

        $mainThread = new Handler(Looper.getMainLooper());

        $pluginMap = new HashMap<String, FusePlugin>();

        $webview = new WebView($context);

        $apiRouter = new FuseAPIRouter(this);

        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(context))
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

        try {
            $apiServer = new FuseAPIServer(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Log.i(TAG, "API Server Port: " + $apiServer.getPort());

        registerPlugin(new FuseRuntime(this));

        $webview.loadUrl("https://localhost/assets/index.html");
    }

    public WebView getWebview() {
        return $webview;
    }

    public Context getActivityContext() {
        return $context;
    }

    public void registerPlugin(FusePlugin plugin) {
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
        $mainThread.post(new Runnable() {
            @Override
            public void run() {
                $webview.evaluateJavascript(String.format("window.__nbsfuse_doCallback(%s,%s);", callbackID, payload), null);
            }
        });
    }

//    @JavascriptInterface
//    public void __apiHandler(String packetString) throws JSONException  {
//        // Unlike iOS, JS interfaces can respond back directly so this method will block the JS
//        // since it will be waiting for that return. While this is invoked on a background thread
//        // already, we should move off the thread to let the JS return as quickly as possible.
//
//        JSONObject jpacket = new JSONObject(packetString);
//        String callbackID = jpacket.optString("callbackID");
//        String route = jpacket.getString("route");
//        byte[] data;
//        if (jpacket.has("contentType")) {
//            FuseAPIContentType contentType = FuseAPIContentType.values()[jpacket.getInt("contentType")];
//            String body = jpacket.getString("body");
//
//            switch (contentType) {
//                case BINARY:
//                    data = Base64.decode(body, Base64.DEFAULT);
//                    break;
//                case STRING:
//                case JSON:
//                    data = body.getBytes();
//                    break;
//                default:
//                    Log.w(TAG, "Unknown content type");
//                    data = new byte[0];
//                    break;
//            }
//        }
//        else {
//            data = new byte[0];
//        }
//
//        FuseAPIPacket packet = new FuseAPIPacket(route, data);
//
//        FuseAPIResponse response = $apiRouter.execute(packet);
//        FuseAPIResponseStatus status = response.getStatus();
//
//        JSONObject out = new JSONObject();
//        out.put("callbackID", callbackID);
//
//        int intStatus = 0;
//        switch (status) {
//            case OK:
//                intStatus = 200;
//                break;
//            case ERROR:
//                intStatus = 400;
//                break;
//        }
//
//        out.put("status", intStatus);
//
//        byte[] payload = response.getData();
//        if (payload.length > 0) {
//            out.put("payload", Base64.encodeToString(response.getData(), Base64.NO_WRAP));
//        }
//
//        StringBuilder sb = new StringBuilder();
//        sb.append("window.__nbsfuse_doCallback(");
//        sb.append(out.toString());
//        sb.append(");");
//
//        $mainThread.post(new Runnable() {
//            @Override
//            public void run() {
//                $webview.evaluateJavascript(sb.toString(), null);
//            }
//        });
//    }
}
