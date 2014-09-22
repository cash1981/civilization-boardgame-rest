package no.asgari.civilization.server.application;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

//TODO: Fiks openshift env name
public class CivBoardGameRandomizerConfiguration extends Configuration {

    //FIXME, during testing, we use another db so to not clutter prod database
    public String CIVILIZATION = "civ-test";

    @JsonProperty
    @NotEmpty
    public String mongohost = System.getenv("OPENSHIFT_MONGODB_DB_HOST") == null ? "localhost" : System.getenv("OPENSHIFT_MONGODB_DB_HOST");

    @JsonProperty
    @Min(1)
    @Max(65535)
    public int mongoport = System.getenv("OPENSHIFT_MONGODB_DB_PORT") == null ? 27017 : Integer.parseInt(System.getenv("OPENSHIFT_MONGODB_DB_PORT"));

    @JsonProperty
    @NotEmpty
    public String mongodb = System.getenv("OPENSHIFT_APP_NAME") == null ? CIVILIZATION : System.getenv("OPENSHIFT_APP_NAME");

}
