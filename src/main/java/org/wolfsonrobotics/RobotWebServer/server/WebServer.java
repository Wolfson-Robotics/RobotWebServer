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
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("Web Server running at: http://localhost:" + port);
    }

    @Override
    public Response serve(IHTTPSession session) {

        String uri = session.getUri();
        Method method = session.getMethod();

        if (Method.GET.equals(method)) {
            File fileToServe = new File(webroot + uri);

            if (fileToServe.exists()) {
                try {
                    return newChunkedResponse(
                        Response.Status.OK, 
                        getMimeTypeForFile(fileToServe.getName()), 
                        new FileInputStream(fileToServe)
                    );
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else if (Method.POST.equals(method)) {
            return newFixedLengthResponse("POST request received");
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "File Not Found");
    }

}
