package no.asgari.civilization.server.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlTransient;

public class PlayerExistException extends Exception {

    public PlayerExistException() {
        super("Player already exists");
    }

    @JsonIgnore
    @XmlTransient
    public Response.Status getStatus() {
        return Response.Status.FORBIDDEN;
    }
}
