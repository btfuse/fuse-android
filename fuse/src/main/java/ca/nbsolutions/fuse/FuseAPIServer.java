package ca.nbsolutions.fuse;

import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.security.SecureRandom;

// TODO: Test Fuse Secret

public class FuseAPIServer {

    public static final String TAG = "FuseAPIServer";

    private final ServerSocket $httpServer;
    private final FuseContext $context;

    String $secret;

    public FuseAPIServer(FuseContext context) throws IOException {
        $context = context;
        $secret = $generateSecret();
        $httpServer = new ServerSocket(0);

        Thread serverThread = new Thread(() -> {
            try {
                while (true) {
                    Socket client = $httpServer.accept();
                    Log.i(TAG, "Received request from: " + client.getInetAddress().getHostAddress());
                    new Thread(() -> {
                        try {
                            $handleConnection(client);
                        } catch (IOException e) {
                            Log.e(TAG, "Client Socket Error: ", e);
                            try {
                                client.close();
                            } catch (IOException ex) {
                                Log.e(TAG, "Client Socket Error: ", e);
                            }
                        }
                    }).start();
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Error:", e);
            }
        });

        serverThread.start();
    }

    public String getSecretKey() {
        return $secret;
    }

    private String $generateSecret() {
        SecureRandom secureRandom = new SecureRandom();

        // Generate a random byte array of the desired length
        int secretLength = 32; // Length in bytes
        byte[] secretBytes = new byte[secretLength];
        secureRandom.nextBytes(secretBytes);

        // Convert the byte array to a hexadecimal string
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : secretBytes) {
            stringBuilder.append(String.format("%02x", b));
        }

        return stringBuilder.toString();
    }

    private static class Header {
        private final String $method;
        private final String $version;
        private final String $path;

        private Map<String, String> $headers;

        public Header(String method, String version, String path) {
            $method = method;
            $version = version;
            $path = path;
            $headers = new HashMap<>();
        }

        public void addHeaderLine(String header) {
            String[] parts = header.split(":");
            $headers.put(parts[0].trim(), parts[1].trim());
        }

        public String getVersion() {
            return $version;
        }

        public String getMethod() {
            return $method;
        }

        public String getPath() {
            return $path;
        }

        public Map<String, String> getHeaders() {
            return $headers;
        }
    }

    private Header $parseHeader(InputStream input) throws IOException {
        byte p = '\0', c = '\0';

        // First need to read the first line:
        StringBuilder sb = new StringBuilder();
        int byteRead = -1;
        while ((byteRead = input.read()) != -1) {
            c = (byte)byteRead;

            if (p == '\r' && c == '\n') {
                break;
            }

            if (c != '\r' && c != '\n') {
                sb.append((char) c);
            }

            p = c;
        }

        String initialLine = sb.toString();

        String[] parts = initialLine.split(" ");
        if (parts.length < 3) {
            throw new IOException("Malformed HTTP header");
        }

        String method = parts[0];
        String url = parts[1];
        String version = parts[2];

        Header header = new Header(method, version, url);

        sb = new StringBuilder();
        byteRead = -1;
        p = '\0';
        c = '\0';
        String line = "";
        while ((byteRead = input.read()) != -1) {
            c = (byte)byteRead;

            if (p == '\r' && c == '\n') {
                p = '\0';
                c = '\0';
                line = sb.toString();

                // First check if we have header content
                if (!line.equals("")) {
                    // add it to our header context
                    header.addHeaderLine(line);
                }
                else {
                    // if line is empty, then this signals the end of header information.
                    // The remaining of the bytes will be the HTTP body
                    break;
                }

                // Replace to start the new line
                sb = new StringBuilder();
                continue;
            }

            if (c != '\r' && c != '\n') {
                sb.append((char)c);
            }

            p = c;
        }

        return header;
    }

    boolean $assertSecret(Header header) {
        String secret = header.getHeaders().getOrDefault("X-Fuse-Secret", null);
        if (secret == null) {
            return false;
        }

        return secret.equals($secret);
    }

    private void $handleConnection(Socket client) throws IOException {
        Header header = $parseHeader(client.getInputStream());

        if (header.getMethod().equals("OPTIONS")) {
            FuseAPIResponse res = new FuseAPIResponse(client);
            res.setStatus(FuseAPIResponseStatus.OK);
            res.setContentType("text/plain");
            res.setContentLength(0);
            res.didFinishHeaders();
            res.didFinish();
            return;
        }

        if (!$assertSecret(header)) {
            client.close();
            return;
        }

        Log.i(TAG, "Method: " + header.getMethod());
        Log.i(TAG, "URL: " + header.getPath());

        FuseAPIResponse response = new FuseAPIResponse(client);
        FuseAPIPacket packet = new FuseAPIPacket(header.getPath(), Long.parseLong(header.getHeaders().getOrDefault("Content-Length", "0").trim()), client.getInputStream());
        $context.getAPIRouter().execute(packet, response);
    }

    public int getPort() {
        return $httpServer.getLocalPort();
    }
}
