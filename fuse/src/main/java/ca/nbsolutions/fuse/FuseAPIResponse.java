
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

import org.json.JSONObject;

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

    private int $status;
    private Socket $client;
    private boolean $hasSentHeaders;

    private String $contentType;
    private long $contentLength;

    public FuseAPIResponse(Socket client) {
        $hasSentHeaders = false;
        $client = client;
        $status = FuseAPIResponseStatus.OK.getValue();
        $contentType = "application/octet-stream";
        $contentLength = 0;
    }

    public int getStatus() {
        return $status;
    }


    // Header APIs
    public void setStatus(int status) {
        $status = status;
    }

    public void setStatus(FuseAPIResponseStatus status) {
        setStatus(status.getValue());
    }

    public void setContentType(String type) {
        $contentType = type;
    }

    public void setContentLength(long size) {
        $contentLength = size;
    }

    public void sendHeaders(int status, String contentType, long contentLength) throws IOException {
        setStatus(status);
        setContentType(contentType);
        setContentLength(contentLength);
        didFinishHeaders();
    }

    public void sendHeaders(FuseAPIResponseStatus status, String contentType, long contentLength) throws IOException {
        sendHeaders(status.getValue(), contentType, contentLength);
    }

    public void didInternalError() throws IOException {
        byte[] data = "Internal Error. See native logs for more details.".getBytes();
        sendHeaders(FuseAPIResponseStatus.INTERNAL, "text/plain", data.length);
        pushData(data);
        didFinish();
    }

    public void didFinishHeaders() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ")
                .append(Integer.toString($status))
                .append(" ")
                .append(StatusCodes.getStatusText($status))
                .append("\r\n")
                .append("Access-Control-Allow-Origin: https://localhost\r\n")
                .append("Access-Control-Allow-Headers: *\r\n")
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

    public void didFinish() throws IOException {
        $client.getOutputStream().flush();
        $client.close();
    }

    public void send(byte[] data, String contentType) throws IOException {
        sendHeaders(FuseAPIResponseStatus.OK, contentType, data.length);
        pushData(data);
        didFinish();
    }

    public void send(byte[] data) throws IOException {
        send(data, "application/octet-stream");
    }

    public void send(JSONObject json) throws IOException {
        byte[] data = json.toString().getBytes();
        sendHeaders(FuseAPIResponseStatus.OK, "application/json", data.length);
        pushData(data);
        didFinish();
    }

    public void send(String stringData) throws IOException {
        byte[] data = stringData.getBytes();
        sendHeaders(FuseAPIResponseStatus.OK, "text/plain", data.length);
        pushData(data);
        didFinish();
    }

    public void send(FuseError error) throws IOException {
        byte[] data = error.serialize().getBytes();
        sendHeaders(FuseAPIResponseStatus.ERROR, "application/json", data.length);
        pushData(data);
        didFinish();
    }

    public void send() throws IOException {
        sendHeaders(204, "text/plain", 0);
        didFinish();
    }

    public void kill() {
        try {
            $client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
