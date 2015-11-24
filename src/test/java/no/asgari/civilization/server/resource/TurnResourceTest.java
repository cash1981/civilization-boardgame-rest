package no.asgari.civilization.server.resource;

import no.asgari.civilization.server.dto.TurnDTO;
import no.asgari.civilization.server.model.PBF;
import no.asgari.civilization.server.model.Playerhand;
import no.asgari.civilization.server.mongodb.AbstractCivilizationTest;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TurnResourceTest extends AbstractCivilizationTest {
    protected static String BASE_URL = String.format("http://localhost:%d/api", RULE.getLocalPort());

    @Test
    public void saveSOTThenReveal() throws Exception {
        TurnDTO turnDTO = new TurnDTO();
        turnDTO.setLocked(false);
        turnDTO.setOrder("Create city");
        turnDTO.setPhase("SOT");
        turnDTO.setTurnNumber(1);

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/%s/turn/save", getApp().pbfId)).build();
        Response response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(Entity.json(turnDTO), Response.class);
        assertEquals(HttpStatus.NO_CONTENT_204, response.getStatus());

        uri = UriBuilder.fromPath(String.format(BASE_URL + "/%s/turn/reveal", getApp().pbfId)).build();
        response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(Entity.json(turnDTO), Response.class);
        assertEquals(HttpStatus.OK_200, response.getStatus());
    }

    @Test
    public void saveTradeThenRevealTurn() throws Exception {
        TurnDTO turnDTO = new TurnDTO();
        turnDTO.setLocked(false);
        turnDTO.setOrder("21");
        turnDTO.setPhase("Trade");
        turnDTO.setTurnNumber(1);

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/%s/turn/save", getApp().pbfId)).build();
        Response response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(Entity.json(turnDTO), Response.class);
        assertEquals(HttpStatus.NO_CONTENT_204, response.getStatus());

        uri = UriBuilder.fromPath(String.format(BASE_URL + "/%s/turn/reveal", getApp().pbfId)).build();
        response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(Entity.json(turnDTO), Response.class);
        assertEquals(HttpStatus.OK_200, response.getStatus());
    }

    @Test
    public void saveCMThenRevealTurn() throws Exception {
        TurnDTO turnDTO = new TurnDTO();
        turnDTO.setLocked(false);
        turnDTO.setOrder("foobar");
        turnDTO.setPhase("CM");
        turnDTO.setTurnNumber(1);

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/%s/turn/save", getApp().pbfId)).build();
        Response response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(Entity.json(turnDTO), Response.class);
        assertEquals(HttpStatus.NO_CONTENT_204, response.getStatus());

        uri = UriBuilder.fromPath(String.format(BASE_URL + "/%s/turn/reveal", getApp().pbfId)).build();
        response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(Entity.json(turnDTO), Response.class);
        assertEquals(HttpStatus.OK_200, response.getStatus());
    }

    @Test
    public void saveMovementThenRevealTurn() throws Exception {
        TurnDTO turnDTO = new TurnDTO();
        turnDTO.setLocked(false);
        turnDTO.setOrder("foobar");
        turnDTO.setPhase("Movement");
        turnDTO.setTurnNumber(1);

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/%s/turn/save", getApp().pbfId)).build();
        Response response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(Entity.json(turnDTO), Response.class);
        assertEquals(HttpStatus.NO_CONTENT_204, response.getStatus());

        uri = UriBuilder.fromPath(String.format(BASE_URL + "/%s/turn/reveal", getApp().pbfId)).build();
        response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(Entity.json(turnDTO), Response.class);
        assertEquals(HttpStatus.OK_200, response.getStatus());
    }

    @Test
    public void saveResearchThenRevealTurn() throws Exception {
        TurnDTO turnDTO = new TurnDTO();
        turnDTO.setLocked(false);
        turnDTO.setOrder("research");
        turnDTO.setPhase("research");
        turnDTO.setTurnNumber(1);

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/%s/turn/save", getApp().pbfId)).build();
        Response response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(Entity.json(turnDTO), Response.class);
        assertEquals(HttpStatus.NO_CONTENT_204, response.getStatus());

        uri = UriBuilder.fromPath(String.format(BASE_URL + "/%s/turn/reveal", getApp().pbfId)).build();
        response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(Entity.json(turnDTO), Response.class);
        assertEquals(HttpStatus.OK_200, response.getStatus());
    }

    @Test
    public void lockTurn() throws Exception {
        saveResearchThenRevealTurn();

        TurnDTO turnDTO = new TurnDTO();
        turnDTO.setLocked(true);
        turnDTO.setTurnNumber(1);

        List<PBF> pbfs = getApp().pbfCollection.find().toArray();
        Playerhand playerhand = pbfs.stream()
                .flatMap(p -> p.getPlayers().stream())
                .filter(p -> p.getUsername().equals("cash1981"))
                .findFirst().get();

        assertEquals(1, playerhand.getPlayerTurns().size());

        URI uri = UriBuilder.fromPath(String.format(BASE_URL + "/%s/turn/lock", getApp().pbfId)).build();
        Response response = client().target(uri)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUsernameAndPassEncoded())
                .put(Entity.json(turnDTO), Response.class);
        assertEquals(HttpStatus.OK_200, response.getStatus());

        pbfs = getApp().pbfCollection.find().toArray();
        playerhand = pbfs.stream()
                .flatMap(p -> p.getPlayers().stream())
                .filter(p -> p.getUsername().equals("cash1981"))
                .findFirst().get();

        assertEquals(2, playerhand.getPlayerTurns().size());
    }
    
}
