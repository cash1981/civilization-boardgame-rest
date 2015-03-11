package no.asgari.civilization.server.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Even simple json needs a class
 */
public class CheckUsernameDTO {
    private final String username;

    @JsonCreator
    public CheckUsernameDTO(@JsonProperty("username") String username) {
        this.username = username;
    }

    @JsonProperty("username")
    public String getUsername() {
        return username;
    }
}
