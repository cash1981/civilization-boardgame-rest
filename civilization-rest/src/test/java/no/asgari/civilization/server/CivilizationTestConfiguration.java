package no.asgari.civilization.server;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class CivilizationTestConfiguration extends Configuration {

    public static final String CIVILIZATION_TEST = "civ-test";

    @JsonProperty
    public String mongohost = System.getenv("MONGODB_DB_HOST") == null ? "localhost" : System.getenv("MONGODB_DB_HOST");

    @JsonProperty
    public int mongoport = System.getenv("MONGODB_DB_PORT") == null ? 27017 : Integer.parseInt(System.getenv("MONGODB_DB_PORT"));

    @JsonProperty
    public String mongodb = System.getenv("MOGNODB_DB_NAME") == null ? CIVILIZATION_TEST : System.getenv("MOGNODB_DB_NAME");

}
