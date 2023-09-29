
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

@RunWith(AndroidJUnit4.class)
public class FuseAPITest {

    @Rule
    public ActivityScenarioRule<TestFuseActivity> activityRule = new ActivityScenarioRule<>(TestFuseActivity.class);

    @BeforeClass
    public static void setUp() {}

    @AfterClass
    public static void tearDown() {}

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

            FuseAPITestClient client = new FuseAPITestClient.Builder()
                    .setAPIPort(port)
                    .setAPISecret(secret)
                    .setPluginID("echo")
                    .setType("text/plain")
                    .setEndpoint("echo")
                    .setContent("Hello Test!")
                    .build();

            FuseAPITestClient.FuseAPITestResponse response = client.execute();
            assertEquals(200, response.getStatus());
            assertTrue(response.readAsString().contains("Hello Test!"));
        });
    }
}
