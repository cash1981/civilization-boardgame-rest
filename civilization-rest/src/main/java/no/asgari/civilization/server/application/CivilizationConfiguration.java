package no.asgari.civilization.server.application;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

//TODO: Fiks env name when deploying to cloud
public class CivilizationConfiguration extends Configuration {

    //FIXME, during testing, we use another db so to not clutter prod database
    public static final String CIVILIZATION_TEST = "civ-test";

    @JsonProperty
    @NotEmpty
    public String mongohost = System.getenv("MONGODB_DB_HOST") == null ? "localhost" : System.getenv("MONGODB_DB_HOST");

    @JsonProperty
    @Min(1)
    @Max(65535)
    public int mongoport = System.getenv("MONGODB_DB_PORT") == null ? 27017 : Integer.parseInt(System.getenv("MONGODB_DB_PORT"));

    @JsonProperty
    @NotEmpty
    public String mongodb = System.getenv("MOGNODB_DB_NAME") == null ? CIVILIZATION_TEST : System.getenv("MOGNODB_DB_NAME");

}
