package ca.nbsolutions.fuse;

import android.os.Bundle;

import androidx.annotation.ContentView;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import ca.nbsolutions.fuse.plugins.IFusePluginRegistrar;

/**
 * Convenience class that can be extended which provides
 * a FuseContext with all the glue bits already done.
 *
 * If fine control is required, it is possible to create the FuseContext
 * itself, you'll need to pass through the following lifecycle methods:
 *  - onCreate(Bundle)
 *  - onStart
 *  - onResume
 *  - onPause
 *  - onSaveInstanceState(Bundle)
 *  - onLowMemory
 *  - onStop
 *  - onDestroy
 */
public class FuseActivity extends AppCompatActivity {

    private FuseContext $fuseContext;

    public FuseActivity() {
        super();
        $init();
    }

    @ContentView
    public FuseActivity(@LayoutRes int contentLayoutId) {
        super(contentLayoutId);
        $init();
    }

    private void $init() {
        $fuseContext = new FuseContext(this);
        $fuseContext.$pluginMapLock.writeLock().lock();
        _registerFusePlugins(plugin -> $fuseContext.$registerPlugin(plugin));
        $fuseContext.$pluginMapLock.writeLock().unlock();
    }

    /**
     * Can be overwritten by subclasses to register plugins
     *
     * @param registrar
     */
    protected void _registerFusePlugins(IFusePluginRegistrar registrar) {};

    public FuseContext getFuseContext() {
        return $fuseContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        $fuseContext.onCreate(savedInstanceState);
        setContentView($fuseContext.getWebview());
    }

    @Override
    protected void onStart() {
        super.onStart();
        $fuseContext.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        $fuseContext.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        $fuseContext.onLowMemory();
    }

    @Override
    protected void onPause() {
        super.onPause();
        $fuseContext.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        $fuseContext.onResume();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        $fuseContext.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        $fuseContext.onDestroy();
    }
}
