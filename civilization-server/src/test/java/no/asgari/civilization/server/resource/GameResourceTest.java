package no.asgari.civilization.server.resource;

import com.sun.jersey.api.client.Client;
import io.dropwizard.testing.junit.DropwizardAppRule;
import no.asgari.civilization.server.application.CivBoardGameRandomizerConfiguration;
import no.asgari.civilization.server.application.CivBoardgameRandomizerApplication;
import no.asgari.civilization.server.mongodb.MongoDBBaseTest;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;
import java.util.LinkedHashMap;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class GameResourceTest extends MongoDBBaseTest {

    @ClassRule
    public static final DropwizardAppRule<CivBoardGameRandomizerConfiguration> RULE =
            new DropwizardAppRule<CivBoardGameRandomizerConfiguration>(CivBoardgameRandomizerApplication.class, "src/main/resources/config.yml");
    private static final String BASE_URL = "http://localhost:%d/civilization";

    @Test
    public void shouldGetActiveGames() {
        Client client = Client.create();

        List<LinkedHashMap> pbf = client.resource(
                UriBuilder.fromPath(String.format(BASE_URL + "/games", RULE.getLocalPort()))
                        .build()
        ).get(List.class);

        //By default Jackson creates List<LinkedHashMap<String,String>> with the values
        assertThat(pbf).isNotEmpty();
    }
}
