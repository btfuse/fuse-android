package ca.nbsolutions.fuse.plugins;

import java.io.IOException;

import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

import ca.nbsolutions.fuse.FuseAPIPacket;
import ca.nbsolutions.fuse.FuseAPIResponse;
import ca.nbsolutions.fuse.FuseContext;
import ca.nbsolutions.fuse.FusePlugin;

public class FuseRuntime extends FusePlugin {
    public FuseRuntime(FuseContext context) {
        super(context);
    }

    @Override
    public String getID() {
        return "FuseRuntime";
    }

    @Override
    protected void _initHandles() {
        this.attachHandler("/info", new APIHandler<FuseRuntime>(this) {
            @Override
            public void execute(FuseAPIPacket packet, FuseAPIResponse response) throws IOException, JSONException {
                int api = Build.VERSION.SDK_INT;

                String version = Integer.toString(api) + ".0.0";

                JSONObject obj = new JSONObject();
                obj.put("version", version);

                this.plugin.send(response, obj);
            }
        });
    }
}
