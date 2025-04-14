package org.wolfsonrobotics.RobotWebServer.server.api.file;

import fi.iki.elonen.NanoHTTPD;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wolfsonrobotics.RobotWebServer.fakerobot.FileExplorer;
import org.wolfsonrobotics.RobotWebServer.server.api.FileAPI;

import java.util.Arrays;

public class Listing extends FileAPI {

    public Listing(NanoHTTPD.IHTTPSession session, FileExplorer fileExplorer) {
        super(session, fileExplorer);
    }

    @Override
    public String fileHandle() throws Exception {
        JSONObject output = new JSONObject();
        JSONArray dirs = new JSONArray(), files = new JSONArray();

        String path = session.getMethod() == NanoHTTPD.Method.POST ? this.getBody("path") : this.parameters.get("path");

        Arrays.stream(this.fileExplorer.dirListing(path)).forEach(file -> {
            if (file.isDirectory()) dirs.put(file.getName());
            if (file.isFile()) files.put(file.getName());
        });
        output.put("dirs", dirs);
        output.put("files", files);
        return output.toString();
    }
}
