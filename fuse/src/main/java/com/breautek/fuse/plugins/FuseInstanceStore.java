
/*
Copyright 2023-2024 Breautek

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

package com.breautek.fuse.plugins;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.breautek.fuse.FuseAPIPacket;
import com.breautek.fuse.FuseAPIResponse;
import com.breautek.fuse.FuseContext;
import com.breautek.fuse.FusePlugin;

import org.json.JSONException;

import java.io.IOException;

public class FuseInstanceStore extends FusePlugin {
    private final Bundle $store;

    public FuseInstanceStore(FuseContext context) {
        super(context);

        $store = new Bundle();
    }

    @Override
    public String getID() {
        return "FuseInstanceStore";
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        synchronized ($store) {
            outState.putBundle("FuseInstanceStore", $store);
        }
    }

    @Override
    protected void _initHandles() {
        this.attachHandler("/set", new APIHandler<FuseInstanceStore>(this) {
            @Override
            public void execute(FuseAPIPacket packet, FuseAPIResponse response) throws IOException, JSONException {
                String data = packet.readAsString();
                synchronized (this.plugin.$store) {
                    this.plugin.$store.putString("data", data);
                }
                response.send();
            }
        });

        this.attachHandler("/get", new APIHandler<FuseInstanceStore>(this) {
            @Override
            public void execute(FuseAPIPacket packet, FuseAPIResponse response) throws IOException, JSONException {
                String data = null;

                synchronized (this.plugin.$store) {
                    data = this.plugin.$store.getString("data");
                }

                if (data == null) {
                    response.send();
                }
                else {
                    response.send(data);
                }
            }
        });
    }
}
