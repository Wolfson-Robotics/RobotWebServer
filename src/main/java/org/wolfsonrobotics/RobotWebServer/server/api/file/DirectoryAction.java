package org.wolfsonrobotics.RobotWebServer.server.api.file;

import fi.iki.elonen.NanoHTTPD;
import org.wolfsonrobotics.RobotWebServer.fakerobot.FileExplorer;
import org.wolfsonrobotics.RobotWebServer.server.api.FileAPI;

public class DirectoryAction extends FileAPI {

    public DirectoryAction(NanoHTTPD.IHTTPSession session, FileExplorer fileExplorer) {
        super(session, fileExplorer);
    }

    @Override
    protected String fileHandle() throws Exception {

        String path = this.getBody("path");
        switch (this.getBody("action")) {
            case "create":
                this.fileExplorer.createDir(path);
                break;
            case "copy":
                this.fileExplorer.copyDir(path, this.getBody("to"));
                break;
            case "rename":
                this.fileExplorer.renameDir(path, this.getBody("to"));
                break;
            case "delete":
                this.fileExplorer.deleteDir(path);
                break;
        }
        return success();

    }
}
