package no.asgari.civilization.server.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.ClientResponse;
import io.dropwizard.testing.junit.DropwizardAppRule;
import no.asgari.civilization.server.application.CivBoardGameRandomizerConfiguration;
import no.asgari.civilization.server.application.CivBoardgameRandomizerApplication;
import no.asgari.civilization.server.dto.ItemDTO;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.mongodb.AbstractMongoDBTest;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PlayerResourceTest extends AbstractMongoDBTest {

    @ClassRule
    public static final DropwizardAppRule<CivBoardGameRandomizerConfiguration> RULE =
            new DropwizardAppRule<CivBoardGameRandomizerConfiguration>(CivBoardgameRandomizerApplication.class, "src/main/resources/config.yml");
    private static final String BASE_URL = "http://localhost:%d";


    @Test
    public void getAllActiveGamesForPlayer() throws Exception {
        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player", RULE.getLocalPort())).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .get(ClientResponse.class);
        assertEquals(response.getStatus(), HttpStatus.OK_200);
    }

    @Test
    public void chooseSpecificTechForLoggedInPlayer() throws Exception {
        PBF pbf = pbfCollection.findOneById(pbfId);
        pbf.getPlayers().forEach(p -> assertThat(p.getTechsChosen()).isEmpty());

        ItemDTO dto = new ItemDTO();
        dto.setName("Agriculture");

        ObjectMapper mapper = new ObjectMapper();
        String dtoAsJSon = mapper.writeValueAsString(dto);

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/tech/choose", RULE.getLocalPort(), pbfId)).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .entity(dtoAsJSon)
                .put(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
        pbf = pbfCollection.findOneById(pbfId);
        Optional<Playerhand> cash1981 = pbf.getPlayers().stream()
                .filter(p -> p.getUsername().equals("cash1981"))
                .findFirst();
        assertTrue(cash1981.isPresent());
        assertThat(cash1981.get().getTechsChosen()).isNotEmpty();
        assertThat(cash1981.get().getTechsChosen().iterator().next().getName()).isEqualTo("Agriculture");
    }

    @Test
    public void endTurnAndChooseNextPlayer() throws Exception {
        PBF pbf = pbfCollection.findOneById(pbfId);

        int i = -1;
        Playerhand nextPlayer = null;
        boolean found = false;
        for(Playerhand p : pbf.getPlayers()) {
            i++;
            if(p.getUsername().equals("cash1981")) {
                nextPlayer = pbf.getPlayers().get(++i);
                assertFalse(nextPlayer.isYourTurn());
                found = true;
                break;
            }
        }
        assertTrue(found);

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/player/%s/endturn", RULE.getLocalPort(), pbfId)).build();
        ClientResponse response = client().resource(uri)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(ClientResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);

        pbf = pbfCollection.findOneById(pbfId);
        found = false;
        for(Playerhand p : pbf.getPlayers()) {
            if(p.equals(nextPlayer)) {
                assertTrue(p.isYourTurn());
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

}
