package no.asgari.civilization.server;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class CivilizationTestConfiguration extends Configuration {

    public static final String CIVILIZATION_TEST = "civ-test";

    @JsonProperty
    public String mongohost = "localhost";

    @JsonProperty
    public int mongoport = 27017;

    @JsonProperty
    public String mongodb = CIVILIZATION_TEST;

}
