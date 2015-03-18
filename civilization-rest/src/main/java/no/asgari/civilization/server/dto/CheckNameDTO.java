package no.asgari.civilization.server.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Even simple json needs a class
 */
public class CheckNameDTO {
    private final String name;

    @JsonCreator
    public CheckNameDTO(@JsonProperty("name") String name) {
        this.name = name;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }
}
