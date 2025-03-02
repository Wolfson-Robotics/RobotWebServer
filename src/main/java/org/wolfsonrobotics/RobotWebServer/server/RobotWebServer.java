package org.wolfsonrobotics.RobotWebServer.server;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;
import org.json.JSONObject;
import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;
import org.wolfsonrobotics.RobotWebServer.server.api.CameraFeed;
import org.wolfsonrobotics.RobotWebServer.server.api.RobotAPI;
import org.wolfsonrobotics.RobotWebServer.server.api.RobotInfo;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.APIException;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.BadInputException;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.MalformedRequestException;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.RobotException;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class RobotWebServer extends NanoHTTPD {

    private final String webroot;
    private final int port;

    private final NanoWSD webSocket;
    private final CommunicationLayer comLayer;


    private final Map<String, Class<? extends RobotAPI>> urlHandlerMap = new HashMap<>();


    public RobotWebServer(int port, String webroot, CommunicationLayer comLayer) throws IOException {
        super(port);
        this.port = port;
        this.webroot = webroot;
        this.comLayer = comLayer;

        // Construct map since Map.ofEntries is not supported in Java 8
        this.urlHandlerMap.put("/robot", RobotInfo.class);
        this.urlHandlerMap.put("/camera_feed", CameraFeed.class);

        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("Web Server running at: http://localhost:" + this.port);

        this.webSocket = new HandleSocket(9090);
        try {
            this.webSocket.start(60000);
        } catch (IOException e) {
            System.out.println("Could not start websocket.");
            e.printStackTrace();
        }
        System.out.println("Websocket started");

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
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, method.toString() + " not implemented");
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

        AtomicReference<Response.Status> status = new AtomicReference<>(Response.Status.OK);
        StringWriter output = new StringWriter();
        StringBuilder mimeType = new StringBuilder(MIME_PLAINTEXT);

        urlHandlerMap.forEach((url, handler) -> {
            // TODO: make this stop checking if it get's the correct url
            if (!url.equalsIgnoreCase(session.getUri()) || output.getBuffer().length() > 0) {
                return;
            }

            try {
                RobotAPI handle = handler.getConstructor(IHTTPSession.class, CommunicationLayer.class).newInstance(session, this.comLayer);
                String message = handle.handle();
                if (message != null) {
                    output.append(message);
                    mimeType.setLength(0);
                    mimeType.append(handle.responseType);
                } else {
                    output.flush();
                }

            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException
                     | RobotException e) {
                output.append(e.getMessage());
                status.set(Response.Status.INTERNAL_ERROR);
                e.printStackTrace();

            } catch (MalformedRequestException | BadInputException e) {
                output.append(e.getMessage());
                status.set(Response.Status.BAD_REQUEST);
                e.printStackTrace();

            } catch (APIException e) {
                // Stub caused by the BaseAPI's absorption of exceptions into
                // APIException as per its throws declaration, disregard
                output.append(e.getMessage());
                status.set(Response.Status.INTERNAL_ERROR);
                e.printStackTrace();
            }
        });

        if (output.getBuffer().length() == 0) {
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
                    JSONObject jsonBody = new JSONObject(requestBody.get("postData"));
                    return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "200 OK" + "\n\n\n" +
                            jsonBody);

                default:
                    return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "200 OK" + "\n\n\n" +
                            requestBody.get("postData"));
            }
        }
        return null;

    }

    /*All static methods go below here */

    //Returns query parameters as a TreeMap
    public static TreeMap<String, ArrayList<String>> parseQueryParameters(IHTTPSession session) {
        TreeMap<String, ArrayList<String>> map = new TreeMap<>();
        String query = session.getQueryParameterString();
        if (query == null) { return null; }

        String[] parameters = query.split("&");

        for (String parameter : parameters) {
            String[] arrParameter = parameter.split("=");
            ArrayList<String> parameterValues = map.get(arrParameter[0]);
            parameterValues = (parameterValues == null) ? new ArrayList<String>() : parameterValues;
            parameterValues.add(arrParameter[1]);
            map.put(arrParameter[0], parameterValues);
        }

        return map;
    }

}