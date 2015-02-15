package no.asgari.civilization.server.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class NoMoreItemsException extends WebApplicationException {
    public NoMoreItemsException(String name) {
        super(Response.status(Response.Status.GONE)
                .entity("No more " + name + " to draw!")
                .type(MediaType.APPLICATION_JSON)
                .build());
    }

}
