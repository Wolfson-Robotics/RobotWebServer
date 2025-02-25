package org.wolfsonrobotics.RobotWebServer.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;

public class WebServer extends NanoHTTPD {

    String webroot;
    int port;

    public WebServer(int port, String webroot) throws IOException {
        super(port);
        this.port = port;
        this.webroot = webroot;
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        
        System.out.println("Web Server running at: http://localhost:" + port);
    }


    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();

        if (Method.GET.equals(method)) {
            return requestGET(session);
        } else if (Method.POST.equals(method)) {
            return requestPOST(session);

        
        // The rest of these will only be implemented when the project will need them
        } else if (Method.PUT.equals(method)) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "PUT not implemented");
        } else if (Method.DELETE.equals(method)) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "DELETE not implemented");
        } else if (Method.CONNECT.equals(method)) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "CONNECT not implemented");
        } else if (Method.OPTIONS.equals(method)) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "OPTIONS not implemented");
        } else if (Method.TRACE.equals(method)) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "TRACE not implemented");
        } else if (Method.PATCH.equals(method)) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "PATCH not implemented");
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "404 Not Found");
    }
    
    private Response requestGET(IHTTPSession session) {

        //Check if it's a websocket GET request
        String websocketHeader = session.getHeaders().get("upgrade");
        if (websocketHeader != null && websocketHeader.equalsIgnoreCase("websocket")) {
            return requestWebsocket(session);
        }

        String uri = session.getUri();
        File fileToServe = new File(webroot + uri);

        // clean urls by auto-routing to index.html
        if (fileToServe.isDirectory()) {

            /*
             * prevent web browsers confusing directories with pages. If a web browser
             * confuses a directory as a page, many GET requests of src="some/local/path" will be
             * screwed up.
             */
            if (!uri.endsWith("/")) {
                return redirect(uri);
            }

            fileToServe = new File(fileToServe, "index.html");
        }

        if (fileToServe.exists() && fileToServe.isFile()) {
            try {
                return newChunkedResponse(
                    Response.Status.OK,
                    getMimeTypeForFile(fileToServe.getName()),
                    new FileInputStream(fileToServe));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "404 Not Found");
    }


    private Response requestPOST(IHTTPSession session) {
        try {

            Map<String, String> requestBody = new HashMap<>();
            session.parseBody(requestBody);

            String contentType = session.getHeaders().getOrDefault("content-type", "");
            switch (contentType) {
                case "application/x-www-form-urlencoded":
                    Map<String, List<String>> formData = session.getParameters();
                    // Parse form data here

                    // Sample print out response for now
                    return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "200 OK" + "\n\n\n" +
                            formData.entrySet().stream().map(e -> "Key: " +
                                    e.getKey() + ", Vals: " + String.join(", ", e.getValue())
                            ).collect(Collectors.joining("\n")));
                case "application/json":
                    JSONObject jsonBody = new JSONObject(requestBody.get("postData"));
                    // Parse JSON here

                    // Sample print out response for now
                    return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "200 OK" + "\n\n\n" +
                            requestBody.get("postData"));
                default:
                    // Parse regular plain text data (assumedly) here

                    // Sample print out response for now
                    return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "200 OK" + "\n\n\n" +
                            requestBody.get("postData"));
            }
            // Print out whatever response here, further work needed
        } catch (IOException | ResponseException e) {
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "400 Bad Request");
        }
    }

    private Response requestWebsocket(IHTTPSession session) {
        String secWebSocketKey = session.getHeaders().get("sec-websocket-key");
        if (secWebSocketKey == null) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Missing Websocket Key");
        }

        String secWebSocketAccept;
        try {
            secWebSocketAccept = NanoWSD.makeAcceptKey(secWebSocketKey);
        } catch (NoSuchAlgorithmException e) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Websocket Key had Invalid Argument");
        }

        NanoWSD ss = new HandleSocket(9090);
        try {
            ss.start();
        } catch (IOException e) {
            return newFixedLengthResponse(Response.Status.CONFLICT, "text/plain", "Websocket on port " + 6090 + " already exists");
        }

        Response response = newFixedLengthResponse(Response.Status.SWITCH_PROTOCOL, "websocket", "");
        response.addHeader("Upgrade", "websocket");
        response.addHeader("Connection", "Upgrade");
        response.addHeader("Sec-WebSocket-Accept", secWebSocketAccept);
        return response;
        
    }

    private Response redirect(String uri) {
        Response r = NanoHTTPD.newFixedLengthResponse(
                Response.Status.REDIRECT,
                MIME_PLAINTEXT,
                "Redirecting...");
        r.addHeader("Location", uri + "/");
        return r;
    }

}
