
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FuseAPIPacket {
    private final String $route;
    private final InputStream $inputStream;
    private final long $contentLength;

    public FuseAPIPacket(String route, long contentLength, InputStream io) {
        $route = route;
        $inputStream = io;
        $contentLength = contentLength;
    }

    public long getContentLength() {
        return $contentLength;
    }

    public String readAsString() throws IOException  {
        byte[] buffer = new byte[(int)$contentLength];
        $inputStream.read(buffer);
        return new String(buffer, StandardCharsets.UTF_8);
    }

    public JSONObject readAsJSON() throws IOException, JSONException {
        return new JSONObject(readAsString());
    }

    public String getRoute() {
        return $route;
    }

    public InputStream getInputStream() {
        return $inputStream;
    }
}
