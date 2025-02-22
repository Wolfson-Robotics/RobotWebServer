package org.wolfsonrobotics.RobotWebServer.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

public class WebServer extends NanoHTTPD {

    String webroot;

    public WebServer(int port, String webroot) throws IOException {
        super(port);
        this.webroot = webroot;
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("Web Server running at: http://localhost:" + port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();

        if (Method.GET.equals(method)) {
            return requestGET(uri);
        } else if (Method.POST.equals(method)) {
            return newFixedLengthResponse("POST request received");
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "404 Not Found");
    }

    private Response requestGET(String uri) {
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

    private Response redirect(String uri) {
        Response r = NanoHTTPD.newFixedLengthResponse(
                Response.Status.REDIRECT,
                MIME_PLAINTEXT,
                "Redirecting...");
        r.addHeader("Location", uri + "/");
        return r;
    }

}
