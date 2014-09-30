package no.asgari.civilization.server.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import io.dropwizard.testing.junit.DropwizardAppRule;
import no.asgari.civilization.server.application.CivBoardGameRandomizerConfiguration;
import no.asgari.civilization.server.application.CivBoardgameRandomizerApplication;
import no.asgari.civilization.server.dto.CreateNewGameDTO;
import no.asgari.civilization.server.model.GameType;
import no.asgari.civilization.server.mongodb.MongoDBBaseTest;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class GameResourceTest extends MongoDBBaseTest {

    @ClassRule
    public static final DropwizardAppRule<CivBoardGameRandomizerConfiguration> RULE =
            new DropwizardAppRule<CivBoardGameRandomizerConfiguration>(CivBoardgameRandomizerApplication.class, "src/main/resources/config.yml");
    private static final String BASE_URL = "http://localhost:%d/civilization";

    @Test
    public void shouldGetActiveGames() {
        Client client = Client.create();

        List<LinkedHashMap> pbf = client.resource(
                UriBuilder.fromPath(String.format(BASE_URL + "/game", RULE.getLocalPort()))
                        .build()
        ).get(List.class);

        //By default Jackson creates List<LinkedHashMap<String,String>> with the values
        assertThat(pbf).isNotEmpty();
    }

    @Test
    public void createGame() throws Exception {
        Client client = Client.create();

        CreateNewGameDTO dto = new CreateNewGameDTO();
        dto.setNumOfPlayers(4);
        dto.setUsername("Morthai");
        dto.setName("PBF WaW");
        dto.setType(GameType.WAW);

        ObjectMapper mapper = new ObjectMapper();
        String dtoAsJSon = mapper.writeValueAsString(dto);

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/game", RULE.getLocalPort())).build();
        ClientResponse response = client.resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .entity(dtoAsJSon)
                .post(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED_201);
        URI location = response.getLocation();
        assertTrue(location.getPath().matches(".*civilization/game/.*"));
    }
}
