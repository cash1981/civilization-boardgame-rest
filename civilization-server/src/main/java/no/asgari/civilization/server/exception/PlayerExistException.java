package no.asgari.civilization.server.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class PlayerExistException extends WebApplicationException {
    public PlayerExistException() {
        super(Response.status(Response.Status.FORBIDDEN)
                .entity("Player already exists")
                .type(MediaType.TEXT_PLAIN)
                .build());
    }

}
