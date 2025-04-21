package org.wolfsonrobotics.RobotWebServer.server.api.file;

import fi.iki.elonen.NanoHTTPD;
import org.wolfsonrobotics.RobotWebServer.fakerobot.FileExplorer;
import org.wolfsonrobotics.RobotWebServer.server.api.FileAPI;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.BadInputException;

public class FileAction extends FileAPI {

    public FileAction(NanoHTTPD.IHTTPSession session, FileExplorer fileExplorer) {
        super(session, fileExplorer);
    }

    @Override
    protected String fileHandle() throws Exception {

        String path = this.getBody("path");
        if (!this.fileExplorer.isFile(path)) throw new BadInputException("The path specified is not a file.");

        switch (this.getBody("action")) {
            case "get":
                return this.fileExplorer.getFile(path);
            case "exists":
                return singletonObj("result", this.fileExplorer.fileExists(path)).toString();
            case "write":
                this.fileExplorer.writeFile(path, this.getBody("content"), false);
                break;
            case "create":
                this.fileExplorer.createFile(path, body.optString("content", ""));
                break;
            case "copy":
                this.fileExplorer.copyFile(path, this.getBody("to"));
                break;
            case "rename":
                this.fileExplorer.renameFile(path, this.getBody("to"));
                break;
            case "delete":
                this.fileExplorer.deleteFile(path);
                break;
            default:
                throw new BadInputException("Unsupported action");
        }
        return success();

    }
}
