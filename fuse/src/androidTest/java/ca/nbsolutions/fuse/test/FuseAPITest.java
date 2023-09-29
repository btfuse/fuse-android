
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Rule;

import static org.junit.Assert.*;

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

@RunWith(AndroidJUnit4.class)
public class FuseAPITest {

    private static OkHttpClient $httpClient;
    private static ExecutorService $bgThread;

    private static final String API_ENDPOINT_BASE = "http://localhost";
    private static final String SECRET_HEADER = "X-Fuse-Secret";

    @Rule
    public ActivityScenarioRule<TestFuseActivity> activityRule = new ActivityScenarioRule<>(TestFuseActivity.class);

    @BeforeClass
    public static void setUp() {
        $httpClient = new OkHttpClient();
        $bgThread = Executors.newSingleThreadExecutor();
    }

    @AfterClass
    public static void tearDown() {
        $httpClient = null;
        $bgThread.shutdown();
    }

    private String $getEndpoint(int port, String pluginID, String api) {
        return API_ENDPOINT_BASE + ":" + port + "/api/" + pluginID + "/" + api;
    }

    @Test
    public void shouldHaveAPort() {
        activityRule.getScenario().onActivity(activity -> {
            int port = activity.getFuseContext().getAPIPort();
            assertTrue(port >= 1 && port <= 65535);
        });
    }

    @Test
    public void shouldHaveASecret() {
        activityRule.getScenario().onActivity(activity -> {
            String secret = activity.getFuseContext().getAPISecret();
            assertNotNull(secret);
        });
    }

    @Test
    public void canDoSimpleEchoRequest() {
        activityRule.getScenario().onActivity(activity -> {
            int port = activity.getFuseContext().getAPIPort();
            String secret = activity.getFuseContext().getAPISecret();

            String endpoint = $getEndpoint(port, "echo", "echo");

            Request request = new Request.Builder()
                    .addHeader(SECRET_HEADER, secret)
                    .url(endpoint)
                    .post(RequestBody.create("Hello Test!", MediaType.parse("text/plain")))
                    .build();

            Future<Response> future = $bgThread.submit((Callable<Response>) () -> {
                return $httpClient.newCall(request).execute();
            });

            Response response = null;
            try {
                response = future.get();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            int code = response.code();
            assertEquals(200, code);

            Response finalResponse = response;
            Future<String> contentFuture = $bgThread.submit((Callable<String>) () -> {
                return finalResponse.body().string();
            });

            try {
                String body = contentFuture.get();
                assertTrue(body.contentEquals("Hello Test!"));
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
