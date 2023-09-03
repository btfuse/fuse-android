
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

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class FuseAPIResponse {

    private static class StatusCodes {
        private final HashMap<Integer, String> $statusTextMap;

        private static StatusCodes $instance;

        private StatusCodes() {
            $statusTextMap = new HashMap<>();
            $statusTextMap.put(200, "OK");
            $statusTextMap.put(204, "OK No Content");
            $statusTextMap.put(400, "Bad Request");
            $statusTextMap.put(404, "Not Found");
        }

        private static StatusCodes getInstance() {
            if (StatusCodes.$instance == null) {
                StatusCodes.$instance = new StatusCodes();
            }
            return StatusCodes.$instance;
        }

        public static String getStatusText(int code) {
            return getInstance().$statusTextMap.get(code);
        }
    }

    private FuseAPIResponseStatus $status;
    private Socket $client;
    private boolean $hasSentHeaders;

    private String $contentType;
    private long $contentLength;

    public FuseAPIResponse(Socket client) {
        $hasSentHeaders = false;
        $client = client;
        $status = FuseAPIResponseStatus.OK;
        $contentType = "application/octet-stream";
        $contentLength = 0;
    }

    public FuseAPIResponseStatus getStatus() {
        return $status;
    }


    // Header APIs
    public void setStatus(FuseAPIResponseStatus status) {
        $status = status;
    }

    public void setContentType(String type) {
        $contentType = type;
    }

    public void setContentLength(long size) {
        $contentLength = size;
    }

    public void finishHeaders() throws IOException {
        int status = 200;
        switch ($status) {
            case OK:
                status = 200;
                break;
            case ERROR:
                status = 400;
                break;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ")
                .append(Integer.toString(status))
                .append(" ")
                .append(StatusCodes.getStatusText(status))
                .append("\r\n")
                .append("Access-Control-Allow-Origin: https://localhost\r\n")
                .append("Access-Control-Allow-Headers: X-Fuse-Secret\r\n")
                .append("Cache-Control: no-cache\r\n")
                .append("Content-Type: ").append($contentType).append("\r\n")
                .append("Content-Length: ").append(Long.toString($contentLength)).append("\r\n")
                .append("\r\n");

        $client.getOutputStream().write(sb.toString().getBytes());
        $client.getOutputStream().flush();

        $hasSentHeaders = true;
    }

    public void pushData(byte[] data) throws IOException {
        if (!$hasSentHeaders) {
            throw new RuntimeException("Cannot push data before headers have been sent. Call finishHeaders first!");
        }

        $client.getOutputStream().write(data);
    }

    public void finish() throws IOException {
        $client.getOutputStream().flush();
        $client.close();
    }

    public void kill() {
        try {
            $client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
