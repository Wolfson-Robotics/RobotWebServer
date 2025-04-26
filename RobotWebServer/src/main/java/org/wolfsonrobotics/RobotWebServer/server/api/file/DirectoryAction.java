package org.wolfsonrobotics.RobotWebServer.server.api.file;

import fi.iki.elonen.NanoHTTPD;
import org.wolfsonrobotics.RobotWebServer.robot.FileExplorer;
import org.wolfsonrobotics.RobotWebServer.server.api.FileAPI;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.BadInputException;
import org.wolfsonrobotics.RobotWebServer.util.GsonHelper;

public class DirectoryAction extends FileAPI {

    public DirectoryAction(NanoHTTPD.IHTTPSession session, FileExplorer fileExplorer) {
        super(session, fileExplorer);
    }

    @Override
    protected String fileHandle() throws Exception {

        String path = this.getBody("path");
        if (!this.fileExplorer.isDirectory(path)) throw new BadInputException("The path specified is not a directory.");

        switch (this.getBody("action")) {
            case "exists":
                return GsonHelper.singletonObj("result", this.fileExplorer.fileExists(path)).toString();
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
            default:
                throw new BadInputException("Unsupported action");
        }
        return success();

    }
}
