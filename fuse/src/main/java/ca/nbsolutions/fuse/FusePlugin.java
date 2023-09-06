
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

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

public abstract class FusePlugin {

    private static final String TAG = "FusePlugin";

    protected abstract static class APIHandler<TPlugin extends FusePlugin> {
        public final TPlugin plugin;
        public APIHandler(TPlugin plugin) {
            this.plugin = plugin;
        }

        public abstract void execute(FuseAPIPacket packet, FuseAPIResponse response) throws IOException, JSONException;
    }

    private final FuseContext $context;

    private final Map<String, APIHandler<? extends FusePlugin>> $handles;

    public abstract String getID();

    public FusePlugin(FuseContext context) {
        $context = context;
        $handles = new HashMap<>();
        _initHandles();
    }

    protected void _initHandles() {}

    public FuseContext getContext() {
        return $context;
    }

    public void attachHandler(String path, APIHandler<? extends FusePlugin> handler) {
        $handles.put(path, handler);
    }

    public void route(String path, FuseAPIPacket packet, FuseAPIResponse response) throws IOException {
        APIHandler<? extends FusePlugin> handler = $handles.getOrDefault(path, null);
        if (handler == null) {
            byte[] content = new FuseError("FuseAPI", 1, "No Plugin Handler for " + packet.getRoute()).serialize().getBytes();
            response.setStatus(FuseAPIResponseStatus.ERROR);
            response.setContentType("application/json");
            response.setContentLength(content.length);
            response.didFinishHeaders();
            response.pushData(content);
            response.didFinish();
            return;
        }

        try {
            handler.execute(packet, response);
        }
        catch (JSONException ex) {
            Log.e(TAG, "JSON Exception", ex);
            response.kill();
        }
        catch (IOException ex) {
            Log.e(TAG, "IO Error", ex);
            response.kill();
        }
    }

    public void send(FuseAPIResponse response, byte[] data, String contentType) throws IOException {
        response.sendHeaders(FuseAPIResponseStatus.OK, contentType, data.length);
        response.pushData(data);
        response.didFinish();
    }

    public void send(FuseAPIResponse response, byte[] data) throws IOException {
        send(response, data, "application/octet-stream");
    }

    public void send(FuseAPIResponse response, JSONObject json) throws IOException {
        byte[] data = json.toString().getBytes();
        response.sendHeaders(FuseAPIResponseStatus.OK, "application/json", data.length);
        response.pushData(data);
        response.didFinish();
    }

    public void send(FuseAPIResponse response, String stringData) throws IOException {
        byte[] data = stringData.getBytes();
        response.sendHeaders(FuseAPIResponseStatus.OK, "text/plain", data.length);
        response.pushData(data);
        response.didFinish();
    }

    public void send(FuseAPIResponse response, FuseError error) throws IOException {
        byte[] data = error.serialize().getBytes();
        response.sendHeaders(FuseAPIResponseStatus.ERROR, "application/json", data.length);
        response.pushData(data);
        response.didFinish();
    }

    public void send(FuseAPIResponse response) throws IOException {
        response.sendHeaders(204, "text/plain", 0);
        response.didFinish();
    }

    public void onLowMemory() {

    }

    public void onPause() {

    }

    public void onResume() {

    }

    public void onSaveInstanceState(@NonNull Bundle outState) {

    }

    public void onStart() {

    }

    public void onStop() {

    }

    public void onDestroy() {

    }
}
