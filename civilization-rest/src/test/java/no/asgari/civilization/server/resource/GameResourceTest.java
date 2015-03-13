package no.asgari.civilization.server.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.ClientResponse;
import io.dropwizard.testing.junit.DropwizardAppRule;
import no.asgari.civilization.server.application.CivilizationApplication;
import no.asgari.civilization.server.application.CivilizationConfiguration;
import no.asgari.civilization.server.dto.CreateNewGameDTO;
import no.asgari.civilization.server.dto.GameDTO;
import no.asgari.civilization.server.dto.PbfDTO;
import no.asgari.civilization.server.model.GameType;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.mongodb.MongoDBBaseTest;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GameResourceTest extends MongoDBBaseTest {

    @ClassRule
    public static final DropwizardAppRule<CivilizationConfiguration> RULE =
            new DropwizardAppRule<CivilizationConfiguration>(CivilizationApplication.class, "civilization-rest/src/main/resources/config.yml");
    private static final String BASE_URL = "http://localhost:%d";

    @Test
    public void shouldGetActiveGames() {
        ClientResponse response = client().resource(
                UriBuilder.fromPath(String.format(BASE_URL + "/game", RULE.getLocalPort()))
                        .build())
                .type(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);

        List<PbfDTO> pbf = response.getEntity(ArrayList.class);
        //By default Jackson creates List<LinkedHashMap<String,String>> with the values
        assertThat(pbf).isNotEmpty();
    }

    @Test
    public void getAllActiveGamesForPlayer() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/game/player", RULE.getLocalPort())).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .get(ClientResponse.class);
        assertEquals(response.getStatus(), HttpStatus.OK_200);
    }

    @Test
    public void createGame() throws Exception {
        CreateNewGameDTO dto = new CreateNewGameDTO();
        dto.setNumOfPlayers(4);
        dto.setName("PBF WaW");
        dto.setType(GameType.WAW);

        ObjectMapper mapper = new ObjectMapper();
        String dtoAsJSon = mapper.writeValueAsString(dto);

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/game", RULE.getLocalPort())).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .entity(dtoAsJSon)
                .post(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED_201);
        URI location = response.getLocation();
        final String id = response.getEntity(String.class);
        assertThat(location.getPath()).isEqualTo("/game/" + id);

        assertThat(pbfCollection.findOneById(id).getPlayers().size()).isEqualTo(1);
    }

    @Test
    public void shouldGetAllAvailableTechs() throws Exception {
        chooseTechForPlayer();

        final ArrayList list = client().resource(
                UriBuilder.fromPath(String.format(BASE_URL + "/game/%s/techs", RULE.getLocalPort(), pbfId))
                        .build())
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .type(MediaType.APPLICATION_JSON)
                .get(ArrayList.class);

        assertThat(list).isNotEmpty();
        PBF pbf = pbfCollection.findOneById(pbfId);
        assertThat(list.size()).isEqualTo(pbf.getTechs().size() - 1);
    }

    @Test
    public void getAllPublicGameLogs() throws Exception {
        chooseTechForPlayer();

        final ArrayList list = client().resource(
                UriBuilder.fromPath(String.format(BASE_URL + "/game/%s/publiclog", RULE.getLocalPort(), pbfId))
                        .build())
                .type(MediaType.APPLICATION_JSON)
                .get(ArrayList.class);
        assertThat(list).isNotEmpty();
    }

    @Test
    public void getAllPrivateGameLogs() throws Exception {
        chooseTechForPlayer();

        final ArrayList list = client().resource(
                UriBuilder.fromPath(String.format(BASE_URL + "/game/%s/privatelog", RULE.getLocalPort(), pbfId))
                        .build())
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .type(MediaType.APPLICATION_JSON)
                .get(ArrayList.class);
        assertThat(list).isNotEmpty();
    }

    private void chooseTechForPlayer() throws JsonProcessingException {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/tech/choose", RULE.getLocalPort(), pbfId)).build();
        ClientResponse response = client().resource(uri)
                .queryParam("name", "Agriculture")
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
        PBF pbf = pbfCollection.findOneById(pbfId);
        Optional<Playerhand> cash1981 = pbf.getPlayers().stream()
                .filter(p -> p.getUsername().equals("cash1981"))
                .findFirst();
        assertTrue(cash1981.isPresent());
        assertThat(cash1981.get().getTechsChosen()).isNotEmpty();
        assertThat(cash1981.get().getTechsChosen().iterator().next().getName()).isEqualTo("Agriculture");
    }

    @Test
    public void checkThatEncodingInFrontendGivesSameAsBackend() {
        String encodingFrontend = "Basic Y2FzaDE5ODE6Zm9v";
        assertEquals(encodingFrontend, getUsernameAndPassEncoded());
    }

    @Test
    public void getGameAsPublicUser() throws Exception {
        PBF pbf = pbfCollection.findOne();

        ClientResponse response = client().resource(
                UriBuilder.fromPath(String.format(BASE_URL + "/game/%s", RULE.getLocalPort(), pbf.getId()))
                        .build())
                .type(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);

        GameDTO game = response.getEntity(GameDTO.class);
        assertThat(game).isNotNull();
    }

    @Test
    public void joinGameAndThenWithdraw() throws Exception {
        ClientResponse response = client().resource(
                UriBuilder.fromPath(String.format(BASE_URL + "/game/%s/join", RULE.getLocalPort(), pbfId_3))
                        .build())
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .type(MediaType.APPLICATION_JSON)
                .put(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(200);

        ClientResponse secondResponse = client().resource(
                UriBuilder.fromPath(String.format(BASE_URL + "/game/%s/withdraw", RULE.getLocalPort(), pbfId_3))
                        .build())
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .type(MediaType.APPLICATION_JSON)
                .put(ClientResponse.class);

        assertThat(secondResponse.getStatus()).isEqualTo(200);
    }

}
