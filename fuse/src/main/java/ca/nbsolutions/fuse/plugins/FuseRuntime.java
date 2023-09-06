package ca.nbsolutions.fuse.plugins;

import java.io.IOException;
import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;
import ca.nbsolutions.fuse.FuseAPIPacket;
import ca.nbsolutions.fuse.FuseAPIResponse;
import ca.nbsolutions.fuse.FuseContext;
import ca.nbsolutions.fuse.FusePlugin;
import java.util.ArrayList;

public class FuseRuntime extends FusePlugin {
    private final ArrayList<String> $pauseHandlers;
    private final ArrayList<String> $resumeHandlers;

    public FuseRuntime(FuseContext context) {
        super(context);

        $pauseHandlers = new ArrayList<>();
        $resumeHandlers = new ArrayList<>();
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

        this.attachHandler("/registerPauseHandler", new APIHandler<FusePlugin>(this) {
            @Override
            public void execute(FuseAPIPacket packet, FuseAPIResponse response) throws IOException, JSONException {
                String callbackID = packet.readAsString();
                synchronized ($pauseHandlers) {
                    $pauseHandlers.add(callbackID);
                }

                this.plugin.send(response);
            }
        });

        this.attachHandler("/unregisterPauseHandler", new APIHandler<FusePlugin>(this) {
            @Override
            public void execute(FuseAPIPacket packet, FuseAPIResponse response) throws IOException, JSONException {
                String callbackID = packet.readAsString();
                synchronized ($pauseHandlers) {
                    $pauseHandlers.remove(callbackID);
                }

                this.plugin.send(response);
            }
        });

        this.attachHandler("/registerResumeHandler", new APIHandler<FusePlugin>(this) {
            @Override
            public void execute(FuseAPIPacket packet, FuseAPIResponse response) throws IOException, JSONException {
                String callbackID = packet.readAsString();
                synchronized ($resumeHandlers) {
                    $resumeHandlers.add(callbackID);
                }

                this.plugin.send(response);
            }
        });

        this.attachHandler("/unregisterResumeHandler", new APIHandler<FusePlugin>(this) {
            @Override
            public void execute(FuseAPIPacket packet, FuseAPIResponse response) throws IOException, JSONException {
                String callbackID = packet.readAsString();
                synchronized ($resumeHandlers) {
                    $resumeHandlers.remove(callbackID);
                }

                this.plugin.send(response);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        synchronized ($pauseHandlers) {
            for (String callbackID : $pauseHandlers) {
                getContext().execCallback(callbackID);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        synchronized ($resumeHandlers) {
            for (String callbackID : $resumeHandlers) {
                getContext().execCallback(callbackID);
            }
        }
    }
}
