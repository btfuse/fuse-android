
package com.breautek.fuse.plugins.nativeview;

import com.breautek.fuse.FuseContext;
import com.breautek.fuse.FusePlugin;
import com.breautek.fuse.FuseScreenUtils;

import android.annotation.SuppressLint;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;

public class NativeViewPlugin extends FusePlugin {
    private final RelativeLayout $container;
    private final WebView $overlay;

    @SuppressLint("SetJavaScriptEnabled")
    public NativeViewPlugin(FuseContext context) {
        super(context);

        $container = new RelativeLayout(context.getActivityContext());

        FuseScreenUtils screenUtils = context.getScreenUtils();

        final float width = 200.0f;
        final float height = 200.0f;

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                (int) screenUtils.toNativePx(width),
                (int) screenUtils.toNativePx(height)
        );

        $container.setOnClickListener((View view) -> {
            FrameLayout.LayoutParams lparams = new FrameLayout.LayoutParams(
                    (int) screenUtils.toNativePx(width),
                    (int) screenUtils.toNativePx(height)
            );
            lparams.leftMargin = (int) screenUtils.toNativePx(25.0f);
            lparams.topMargin = (int) screenUtils.toNativePx(25.0f);
            $container.setLayoutParams(lparams);
//            $container.setLeft((int) (25.0 * density));
//            $container.setTop((int) (25.0 * density));
        });

        $container.setLayoutParams(params);
        $container.setBackgroundColor(0x80FF0000);

        $overlay = new WebView(context.getActivityContext());
        $overlay.setBackgroundColor(0x0);;
        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(context.getActivityContext()))
                .setHttpAllowed(false)
                .setDomain(context.getHost())
                .build();

        $overlay.setWebViewClient(new WebViewClientCompat() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest((request.getUrl()));
            }
        });

        WebSettings settings = $overlay.getSettings();
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        String testhtml = """
                <!DOCTYPE html>
                <html style="width:100%;height:100%">
                    <head></head>
                    <body style="width:100%;height:100%">
                        <p>OVERLAY</p>
                        <div style="background-color:lightgray;position:absolute;bottom: 5px; left: 10px;" onclick="javascript:console.log('overlay clicked');">CLICK ME</div>
                    </body>
                </html>
                """;

        $overlay.loadData(testhtml, "text/html", "utf-8");

        FrameLayout.LayoutParams webviewLayoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );

        $container.addView($overlay, webviewLayoutParams);

        context.getLayout().addView($container);
    }

    @Override
    protected void _initHandles() {
//        this.attachHandler("/echo", new APIHandler<EchoPlugin>(this) {
//            @Override
//            public void execute(FuseAPIPacket packet, FuseAPIResponse response) throws IOException {
//                response.send(packet.readAsBinary(), packet.getContentType());
//            }
//        });
    }

    @Override
    public String getID() {
        return "nativeview";
    }
}
