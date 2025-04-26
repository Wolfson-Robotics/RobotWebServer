package org.wolfsonrobotics.RobotWebServer.server.api;

import fi.iki.elonen.NanoHTTPD;
import org.wolfsonrobotics.RobotWebServer.robot.FileExplorer;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.APIException;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.ExceptionWrapper;

public abstract class FileAPI extends BaseAPI {

    protected final FileExplorer fileExplorer;

    protected FileAPI(NanoHTTPD.IHTTPSession session, FileExplorer fileExplorer) {
        super(session);
        this.fileExplorer = fileExplorer;
    }

    @Override
    public String handle() throws APIException {
        super.commonHandle();
        try {
            return this.fileHandle();
        } catch (Exception e) { throw new ExceptionWrapper(e); }
    }

    protected abstract String fileHandle() throws Exception;

}
