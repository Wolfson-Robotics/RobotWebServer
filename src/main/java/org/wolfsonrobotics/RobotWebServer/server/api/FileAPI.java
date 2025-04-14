package org.wolfsonrobotics.RobotWebServer.server.api;

import fi.iki.elonen.NanoHTTPD;
import org.wolfsonrobotics.RobotWebServer.fakerobot.FileExplorer;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.APIException;

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
        } catch (Exception e) { throw APIException.wrap(e); }
    }

    protected abstract String fileHandle() throws Exception;

}
