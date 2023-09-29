
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

package ca.nbsolutions.fuse.test;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FuseAPITestClient {
    private String $pluginID;
    private String $apiSecret;
    private String $api;
    private int $port;

    private byte[] $content;
    private String $type;
    private final ExecutorService $bgThread;

    private static OkHttpClient $httpClient;

    public static class Builder {
        private String $pluginID;
        private String $apiSecret;
        private String $api;
        private int $port;

        private byte[] $content;
        private String $type;


        public Builder() {}

        public Builder setPluginID(String id) {
            $pluginID = id;
            return this;
        }

        public Builder setAPIPort(int port) {
            $port = port;
            return this;
        }

        public Builder setAPISecret(String secret) {
            $apiSecret = secret;
            return this;
        }

        public Builder setEndpoint(String endpoint) {
            $api = endpoint;
            return this;
        }

        public Builder setContent(String content) {
            $content = content.getBytes();
            return this;
        }

        public Builder setType(String type) {
            $type = type;
            return this;
        }

        public FuseAPITestClient build() {
            return new FuseAPITestClient($pluginID, $port, $apiSecret, $api, $content, $type);
        }
    }

    public static class FuseAPITestResponse {
        private int $status;
        private byte[] $data;

        public FuseAPITestResponse(int status, byte[] data) {
            $status = status;
            $data = data;
        }

        public int getStatus() {
            return $status;
        }

        public byte[] readAsBinary() {
            return $data;
        }

        public String readAsString() {
            return new String($data);
        }
    }

    private static final String API_ENDPOINT_BASE = "http://localhost";
    private static final String SECRET_HEADER = "X-Fuse-Secret";

    public FuseAPITestClient(String pluginID, int port, String secret, String endpoint, byte[] content, String type) {
        $pluginID = pluginID;
        $port = port;
        $apiSecret = secret;
        $api = endpoint;
        $content = content;
        $type = type;
        $bgThread = Executors.newSingleThreadExecutor();

        if ($httpClient == null) {
            $httpClient = new OkHttpClient();
        }
    }

    public FuseAPITestResponse execute() {
        Future<FuseAPITestResponse> future = $bgThread.submit(new Callable<FuseAPITestResponse>() {
            @Override
            public FuseAPITestResponse call() throws Exception {
                Request request = new Request.Builder()
                        .url($getEndpoint())
                        .addHeader(SECRET_HEADER, $apiSecret)
                        .post(RequestBody.create($content, MediaType.parse($type)))
                        .build();

                Response httpResponse = $httpClient.newCall(request).execute();

                return new FuseAPITestResponse(httpResponse.code(), httpResponse.body().bytes());
            }
        });

        FuseAPITestResponse response = null;

        try {
            response = future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return response;
    }

    private String $getEndpoint() {
        return API_ENDPOINT_BASE + ":" + $port + "/api/" + $pluginID + "/" + $api;
    }
}
