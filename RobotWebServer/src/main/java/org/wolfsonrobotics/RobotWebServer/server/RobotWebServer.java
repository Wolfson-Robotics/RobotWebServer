package org.wolfsonrobotics.RobotWebServer.server;


import android.webkit.MimeTypeMap;

import org.wolfsonrobotics.RobotWebServer.ServerConfig;
import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;
import org.wolfsonrobotics.RobotWebServer.robot.FileExplorer;
import org.wolfsonrobotics.RobotWebServer.server.api.BaseAPI;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.APIException;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.ExceptionWrapper;
import org.wolfsonrobotics.RobotWebServer.util.GsonHelper;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;

public class RobotWebServer extends NanoHTTPD {

    private NanoWSD webSocket;
    private final CommunicationLayer commLayer;
    private final FileExplorer robotStorage;



    public RobotWebServer(CommunicationLayer commLayer) {
        super(ServerConfig.PORT);
        this.commLayer = commLayer;

        FileExplorer robotStorage;
        try {
            robotStorage = new FileExplorer(ServerConfig.CONTROL_HUB_STORAGE);
        } catch (IOException e) {
            robotStorage = null;
            System.out.println("WARNING: An error occurred instantiating the storage interface");
        }
        this.robotStorage = robotStorage;

    }
    public RobotWebServer(ServerOpMode instance) {
        this(new CommunicationLayer(
                instance,
                ServerConfig.COMM_METHODS,
                Stream.concat(
                        Arrays.stream(ServerConfig.EXCLUDED_COMM_METHODS),
                        Arrays.stream(instance.getExcludedMethods())
                ).toArray(String[]::new)
        ));
    }



    public void start() throws IOException {
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("Web Server running at: http://localhost:" + ServerConfig.PORT);

        this.webSocket = new HandleSocket(ServerConfig.SOCKET_PORT, this.commLayer);
        try {
            this.webSocket.start(ServerConfig.SOCKET_START_TIMEOUT);
        } catch (IOException e) {
            System.out.println("Could not start websocket.");
            e.printStackTrace();
        }
        System.out.println("Websocket started");
    }

    @Override
    public void stop() {
        super.stop();
        this.webSocket.stop();
    }




    @Override
    public Response serve(IHTTPSession session) {

        System.out.println(session.getUri());

        if (session.getUri().equalsIgnoreCase("/req_testing")) {
            return testing(session);
        }
        Method method = session.getMethod();

        switch (method) {
            case GET:
                return requestGET(session);
            case POST:
                return requestPOST(session);

            // The rest of these will only be implemented when the project will need them
            default:
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, method + " not implemented");
        }

    }



    private Response requestGET(IHTTPSession session) {

        // Check if it's a websocket GET request
        String websocketHeader = session.getHeaders().get("upgrade");
        if (websocketHeader != null && websocketHeader.equalsIgnoreCase("websocket")) {
            return requestWebsocket(session);
        }
        //Check if it's a server side URL
        //session.getQueryParameterString()

        String uri = session.getUri();
        File fileToServe = new File(ServerConfig.WEBROOT + uri);

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
            String extension = MimeTypeMap.getFileExtensionFromUrl(fileToServe.getPath());
            if (extension == null) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "File extension returned null");
            }
            String mime = Optional.ofNullable(MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension))
                    .orElse(ServerConfig.mimeTypes.get(extension));

            try {
                return newChunkedResponse(
                        Response.Status.OK,
                        mime,
                        new FileInputStream(fileToServe));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        return handleAPI(session);
    }


    // Still encapsulate the method call in "requestPOST" for the mere sake
    // of communicating intent and permitting for possibility of other
    // POST request handling here not appropiate for the "handleAPI"
    // function
    private Response requestPOST(IHTTPSession session) {
        return handleAPI(session);
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




    private Response handleAPI(IHTTPSession session) {

        AtomicReference<Response.Status> status = new AtomicReference<>();
        StringWriter output = new StringWriter();
        StringBuilder mimeType = new StringBuilder(MIME_PLAINTEXT);


        Map<Map<String, ? extends Class<?>>, Object> mapConstructors = new HashMap<>();
        mapConstructors.put(ServerConfig.robotAPIMap, this.commLayer);
        mapConstructors.put(ServerConfig.fileAPIMap, this.robotStorage);

        mapConstructors.forEach((apiMap, arg) ->
            apiMap.entrySet().stream()
                    .filter(e -> uriEquals(e.getKey(), session.getUri()))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .ifPresent(handlerType -> {

                        try {
                            BaseAPI handler = (BaseAPI) handlerType.getConstructor(IHTTPSession.class, arg.getClass()).newInstance(session, arg);
                            String message = handler.handle();
                            if (message != null) {
                                output.append(message);
                                status.set(Response.Status.OK);
                                mimeType.setLength(0);
                                mimeType.append(handler.getResponseType());
                            } else { output.flush(); }
                        } catch (APIException e) {
                            output.append(e.getMessage());
                            status.set(e.getStatus());
                            e.printStackTrace();
                        } catch (Exception rawE) {
                            ExceptionWrapper e =  new ExceptionWrapper(rawE);
                            output.append(e.getMessage());
                            status.set(e.getStatus());
                            e.printStackTrace();
                        }

                    }));

        if (status.get() == null) {
            output.flush();
            output.append("404 Not Found");
            status.set(Response.Status.NOT_FOUND);
        }
        return newFixedLengthResponse(status.get(), mimeType.toString(), output.toString());

    }



    private Response testing(IHTTPSession session) {

        if (session.getMethod().equals(Method.POST)) {

            Map<String, String> requestBody = new HashMap<>();
            try {
                session.parseBody(requestBody);
            } catch (ResponseException | IOException e) {
                e.printStackTrace();
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "400 Bad Request");
            }

            switch (session.getHeaders().getOrDefault("content-type", "")) {
                case "application/x-www-form-urlencoded":
                    Map<String, List<String>> formData = session.getParameters();
                    return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "200 OK" + "\n\n\n" +
                            formData.entrySet().stream().map(e -> "Key: " +
                                    e.getKey() + ", Vals: " + String.join(", ", e.getValue())
                            ).collect(Collectors.joining("\n")));

                case "application/json":
                    return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "200 OK" + "\n\n\n" +
                            GsonHelper.getPostPayloadJSON(requestBody));

                default:
                    return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "200 OK" + "\n\n\n" +
                            requestBody.get("postData"));
            }
        }
        return null;

    }


    private String fixPath(String url) {
        String intermediateURL;
        try {
            intermediateURL = new URI(url).normalize().toString();
        } catch (URISyntaxException e) {
            intermediateURL = url;
        }
        return Arrays.stream(intermediateURL.trim().split("/")).filter(s -> !s.isEmpty()).collect(Collectors.joining("/"));
    }

    private boolean uriEquals(String url1, String url2) {
        return fixPath(url1).equalsIgnoreCase(fixPath(url2));
    }


}