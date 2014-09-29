package no.asgari.civilization.server.exception;

import javax.ws.rs.core.Response;

public class PlayerExistException extends Exception {

    public PlayerExistException() {
        super("Player already exists");
    }

    public Response.Status getStatus() {
        return Response.Status.FORBIDDEN;
    }
}
